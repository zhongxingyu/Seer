 package com.adaptiweb.utils.poi;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 
 public class PoiUtils {
 
 	public static HSSFWorkbook loadWorkbookFromFile(String filePath) {
 		try {
 			return loadWorkbookFromStream(new FileInputStream(filePath));
 		} catch (FileNotFoundException e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
	public static HSSFWorkbook loadWorkbookFromResource(String resourcePath) {
		return loadWorkbookFromStream(InputStream.class.getResourceAsStream(resourcePath));
 	}
 
 	public static HSSFWorkbook loadWorkbookFromStream(InputStream stream) {
 		HSSFWorkbook wb;
 		try {
 			wb = new HSSFWorkbook(stream);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		} finally {
 			IOUtils.closeQuietly(stream);
 		}
 		return wb;
 	}
 	
 }
