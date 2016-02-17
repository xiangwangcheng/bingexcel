package com.bing.excel.reader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.sql.SQLException;

import org.apache.poi.POIXMLDocument;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.IOUtils;

import com.bing.excel.reader.hssf.DefaultHSSFHandler;
import com.bing.excel.reader.sax.DefaultXSSFSaxHandler;

public class ExcelReaderFactory {
	/**
	 * @param file
	 * @param excelReader
	 * @param ignoreNumFormat  是否忽略数据格式  (default=false，按照格式读取)
	 * @param maxReturnLines 可为null，当null时候，不限制
	 * @return
	 * @throws Exception
	 */
	public static SaxHandler create(File file, ExcelReadListener excelReader,
			boolean ignoreNumFormat, Integer maxReturnLines) throws Exception {
		if (!file.exists()) {
			throw new FileNotFoundException(file.toString());
		}
		try {
			POIFSFileSystem fs = new POIFSFileSystem(file);
			return create(fs, excelReader, ignoreNumFormat, maxReturnLines);
		} catch (OfficeXmlFileException e) {
			OPCPackage pkg = OPCPackage.open(file, PackageAccess.READ);
			try {
				return create(pkg, excelReader, ignoreNumFormat, maxReturnLines);
			} catch (IllegalArgumentException | IOException e1) {
				pkg.revert();
				throw e1;
			}
		}

	}

	/**
	 * @param file
	 * @param excelReader
	 * @return
	 * @throws Exception
	 */
	public static SaxHandler create(File file, ExcelReadListener excelReader)
			throws Exception {
		return create(file, excelReader, false, null);

	}
	/**
	 * @param file
	 * @param excelReader
	 * @param ignoreNumFormat 是否忽略数据格式  (default=false，按照格式读取)
	 * @return
	 * @throws Exception
	 */
	public static SaxHandler create(File file, ExcelReadListener excelReader,boolean ignoreNumFormat)
			throws Exception {
		return create(file, excelReader, ignoreNumFormat, null);
		
	}
	public static SaxHandler create(File file, ExcelReadListener excelReader,Integer maxReturnLines)
			throws Exception {
		return create(file, excelReader, false, null);
		
	}

	/**
	 * @param inp
	 * @param excelReader
	 * @param ignoreNumFormat 是否忽略数据格式  (default=false，按照格式读取) 
	 * @param maxReturnLines
	 * @return
	 * @throws InvalidFormatException
	 * @throws IOException
	 * @throws SQLException
	 */
	public static SaxHandler create(InputStream inp,
			ExcelReadListener excelReader, boolean ignoreNumFormat,
			Integer maxReturnLines) throws InvalidFormatException, IOException, SQLException {
		 // If clearly doesn't do mark/reset, wrap up
        if (! inp.markSupported()) {
            inp = new PushbackInputStream(inp, 8);
        }

        // Ensure that there is at least some data there
        byte[] header8 = IOUtils.peekFirst8Bytes(inp);

        // Try to create
        if (POIFSFileSystem.hasPOIFSHeader(header8)) {
            POIFSFileSystem fs = new POIFSFileSystem(inp);
            return create(fs, excelReader, ignoreNumFormat, maxReturnLines);
        }
        if (POIXMLDocument.hasOOXMLHeader(inp)) {
             OPCPackage pkg = OPCPackage.open(inp);
             return create(pkg, excelReader, ignoreNumFormat, maxReturnLines);
        }
        throw new InvalidFormatException("Your InputStream was neither an OLE2 stream, nor an OOXML stream");
    

	}

	

	/**
	 * @param inp
	 * @param excelReader
	 * @param maxReturnLines <code>null</code> 不限制，
	 * @return
	 * @throws InvalidFormatException
	 * @throws IOException
	 * @throws SQLException
	 */
	public static SaxHandler create(InputStream inp,
			ExcelReadListener excelReader, Integer maxReturnLines) throws InvalidFormatException, IOException, SQLException {
		  return create(inp, excelReader, false, maxReturnLines);
	}

	/**
	 * @param inp
	 * @param excelReader
	 * @param ignoreNumFormat 是否忽略数据格式  (default=false，按照格式读取)
	 * @return
	 * @throws InvalidFormatException
	 * @throws IOException
	 * @throws SQLException
	 */
	public static SaxHandler create(InputStream inp,
			ExcelReadListener excelReader, boolean ignoreNumFormat) throws InvalidFormatException, IOException, SQLException {
		 return create(inp, excelReader, ignoreNumFormat, null);
	}

	/**
	 * @param pkg
	 * @param excelReader
	 * @return
	 * @throws SQLException
	 * @throws InvalidFormatException
	 * @throws IOException
	 */
	public static SaxHandler create(OPCPackage pkg,
			ExcelReadListener excelReader) throws SQLException,
			InvalidFormatException, IOException {
		return create(pkg, excelReader, false, null);
	}

	/*
	 * public static SaxHandler create(OPCPackage pkg,ExcelReadListener
	 * excelReader,Integer maxReturnLines) throws SQLException,
	 * InvalidFormatException, IOException{ return
	 * create(pkg,excelReader,false,maxReturnLines); } public static SaxHandler
	 * create(OPCPackage pkg,ExcelReadListener excelReader,boolean
	 * ignoreNumFormat) throws SQLException, InvalidFormatException,
	 * IOException{ return create(pkg,excelReader,ignoreNumFormat,null); }
	 */
	public static SaxHandler create(OPCPackage pkg,
			ExcelReadListener excelReader, boolean ignoreNumFormat,
			Integer maxReturnLines) throws SQLException,
			InvalidFormatException, IOException {
		DefaultXSSFSaxHandler handler = new DefaultXSSFSaxHandler(pkg,
				excelReader, ignoreNumFormat);
		if (maxReturnLines != null) {
			handler.setMaxReturnLine(maxReturnLines);
		}
		return handler;
	}

	public static SaxHandler create(POIFSFileSystem fs,
			ExcelReadListener excelReader) throws SQLException {
		return create(fs, excelReader, false, null);
	}

	/*
	 * public static SaxHandler create(POIFSFileSystem fs,ExcelReadListener
	 * excelReader,Integer maxReturnLines) throws SQLException{ return
	 * create(fs,excelReader,false,maxReturnLines); } public static SaxHandler
	 * create(POIFSFileSystem fs,ExcelReadListener excelReader,boolean
	 * ignoreNumFormat) throws SQLException{ return
	 * create(fs,excelReader,ignoreNumFormat,null); }
	 */
	public static SaxHandler create(POIFSFileSystem fs,
			ExcelReadListener excelReader, boolean ignoreNumFormat,
			Integer maxReturnLines) throws SQLException {
		DefaultHSSFHandler handler = new DefaultHSSFHandler(fs, excelReader,
				ignoreNumFormat);
		if (maxReturnLines != null) {
			handler.setMaxReturnLine(maxReturnLines);
		}
		return handler;
	}
}
