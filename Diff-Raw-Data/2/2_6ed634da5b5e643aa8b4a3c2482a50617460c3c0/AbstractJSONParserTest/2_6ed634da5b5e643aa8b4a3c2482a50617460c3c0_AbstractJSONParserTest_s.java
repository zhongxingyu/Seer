 package com.karhatsu.suosikkipysakit.datasource.parsers.test;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Scanner;
 
 import junit.framework.TestCase;
 
 public abstract class AbstractJSONParserTest extends TestCase {
 
	protected static final String TEST_FILES_DIRECTORY = "src/com/karhatsu/suosikkipysakit/datasource/parsers/test/";
 
 	protected String readTestJson(String fileName) throws IOException {
 		FileInputStream fis = null;
 		try {
 			fis = new FileInputStream(new File(TEST_FILES_DIRECTORY + fileName));
 			Scanner scanner = new Scanner(fis).useDelimiter("\\A");
 			return scanner.hasNext() ? scanner.next() : "";
 		} finally {
 			if (fis != null) {
 				fis.close();
 			}
 		}
 	}
 }
