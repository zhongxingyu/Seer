 package org.dstricks;
 
 
 import static org.junit.Assert.*;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import org.junit.Test;
 
 
 public class TesseractTest {
 
 	@Test
 	public void digitsTest() throws Exception {
 		String expected = "0123456789";
 		String actual = Tesseract.process("src/test/resources/digits.jpg").trim();
 		assertEquals("digits.jpg was not read properly",  expected, actual);
 	}
 	
 	@Test
 	public void asciiSentenceTest() throws Exception {
 		String expected = "Happy New Year 2003!";
 		String actual = Tesseract.process("src/test/resources/asciiSentence.png").trim();
		assertEquals("digits.jpg was not read properly",  expected, actual);
 	}
 	
 	@Test
 	public void digitsInputStreamTest() throws IOException, InterruptedException {
 		String expected = "0123456789";
 		FileInputStream fis = new FileInputStream(new File("src/test/resources/digits.jpg"));
 		String actual = Tesseract.process(fis).trim();
 		assertEquals("digits.jpg was not read properly",  expected, actual);
 	}
 	
 	@Test
	public void noTextImageTest() throws IOException, InterruptedException {
 		String expected = "";
		String actual = Tesseract.process("src/test/resources/testJPEG.jpg").trim();
		
 		assertEquals("testJPEG.jpg was not read properly",  expected, actual);
 	}
 
 }
