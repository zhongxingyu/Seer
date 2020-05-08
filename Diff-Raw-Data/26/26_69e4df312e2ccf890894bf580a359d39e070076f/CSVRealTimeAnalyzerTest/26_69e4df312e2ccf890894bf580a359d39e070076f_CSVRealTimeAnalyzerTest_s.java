 package com.lt.loadtest.loadtestagent.domain.bl.impl;
 
 import static org.junit.Assert.*;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class CSVRealTimeAnalyzerTest {
 
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test
 	public void test() {
 		String logFilePath = "/Users/marsliutao/Blur/cloudpi/sample1.csv";
 		logFilePath = "/tmp/118_http_C200_L30000_20130516102132.csv";
 		File file= new File(logFilePath);
 		long size = file.length();
 		String str = CSVRealTimeAnalyzer.getAnalyzableLines(file, 0, size);
 		System.out.println("str="+str);
 	}
 
 	@Test
 	public void testGetLastEOFPos(){
 		String logFilePath = "/Users/marsliutao/Blur/cloudpi/sample1.csv";
 		logFilePath = "/tmp/118_http_C200_L30000_20130516102132.csv";
 		File file= new File(logFilePath);
 		long startPos = 0;
 		long fileSize = file.length();
 		long lastEofPos = CSVRealTimeAnalyzer.getLastEOFPos(file, startPos, fileSize);
 		
 		startPos = 0;
 		long endPos = lastEofPos;
 		String str = CSVRealTimeAnalyzer.getAnalyzableLines(file, startPos, endPos);
 		//System.out.println("str1="+str);
 		
 		str = CSVRealTimeAnalyzer.getAnalyzableLines(file, lastEofPos+1, fileSize);
 		System.out.println("str2="+str);
 		assert(str.equals(""));
 	}
 	
 	@Test
 	public void testGetLastEOF(){
 		String logFilePath = "/Users/marsliutao/Blur/cloudpi/sample1.csv";
 		File file= new File(logFilePath);
 		long startPos = 0;
 		long size = file.length();
 		try {
 			FileInputStream fis = new FileInputStream(file);
 			int i = 0;
 			int index=0;
 			while((i = fis.read())!=-1){
 				char ch = (char)i;
 				System.out.println(String.format("%s,ch=%s",index, (char)ch));
 				if(ch=='\n'){
 					System.out.println("ch is EOF at "+ index);
 				}
 				index++;
 			}
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		System.out.println("size="+size);
 		long pos = CSVRealTimeAnalyzer.getLastEOFPos(file, startPos, size-1);
 		System.out.println("LastEofPos="+pos);
 	}
 	
 	
 	@Test
 	public void testAnalyzeCSV(){
 		//first verify that the start and end pos are all right
 		
 	    //second verify lines get between start and end pos are all right
 		
 		
 	}
 }
