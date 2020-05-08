 package com.operativus.senacrs.audit.testutils;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 
 import org.junit.Assert;
 
 
 public class CompareTextUtils {
 	
 	public static void compareReaders(BufferedReader expected, BufferedReader actual) throws IOException {
 
 		if (expected == actual) {
 			Assert.assertEquals(expected, actual);
 		} else if (expected == null) {
 			Assert.assertEquals(expected, actual);
 		} else if (actual == null) {
 			Assert.assertEquals(expected, actual);			
 		} else {
 			compareValidReaders(expected, actual);
 		}
 	}
 
 	private static void compareValidReaders(BufferedReader expected, BufferedReader actual) throws IOException {
 
 		String[] lines = null;
 		int lineNumber = 0;
 		
 		lines = readLines(expected, actual);
 		compareLines(lines, lineNumber);
 		while ((lines[0] != null) && (lines[1] != null)) {
 			lineNumber++;
 			lines = readLines(expected, actual);
 			compareLines(lines, lineNumber);
 		}
 	}
 
 	private static String[] readLines(BufferedReader expected, BufferedReader actual) throws IOException {
 
 		String[] lines = null;
 
 		lines = new String[2];
 		lines[0] = expected.readLine();
 		lines[1] = actual.readLine();
 		
 		return lines;
 	}
 
 	private static void compareLines(String[] lines, int lineNumber)
 			throws IOException {
 
 		String msg = null;
 		
		msg = "Reading line " + String.valueOf(lineNumber);
 		Assert.assertEquals(msg, lines[0], lines[1]);
 	}
 }
