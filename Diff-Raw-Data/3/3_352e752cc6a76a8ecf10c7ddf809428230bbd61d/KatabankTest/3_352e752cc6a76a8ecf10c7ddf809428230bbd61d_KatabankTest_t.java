 package org.katabank.app;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import junit.framework.Assert;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.katabank.exception.InvalidFileException;
 
 public class KatabankTest {
 
 	private Katabank katabank;
 	private List<String> lines;
 
 	private BufferedReader testFileBufferReader;
 	private BufferedReader testFile2BufferReader;
 	
 	private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
 	private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
 
 	@Before
 	public void setup() throws FileNotFoundException {
 		this.katabank = new Katabank();
 
 		this.testFileBufferReader = new BufferedReader(new FileReader("files/testFile.txt"));
 		this.testFile2BufferReader = new BufferedReader(new FileReader("files/testFile2.txt"));
 
 		lines = new ArrayList<String>();
 		lines.add("    _  _     _  _  _  _  _   ");
 		lines.add("  | _| _||_||_ |_   ||_||_|  ");
 		lines.add("  ||_  _|  | _||_|  ||_| _|  ");
 		lines.add("                             ");
 	}
 	
 	@Before
 	public void setUpStreams() {
 	    System.setOut(new PrintStream(outContent));
 	    System.setErr(new PrintStream(errContent));
 	}
 
 	@After
 	public void cleanUpStreams() {
 	    System.setOut(null);
 	    System.setErr(null);
 	}
 
 
 	@Test
 	public void testReadOneEntry() {
 		try{
 			List<String> linesFromFile = katabank.readOneEntry(testFileBufferReader);
 
 			if(linesFromFile == null || linesFromFile.isEmpty()) {
 				Assert.fail("Could not read an entry");
 			}
 
 			Assert.assertEquals(3, linesFromFile.size());
 
 			for(String line : linesFromFile) {
 				Assert.assertEquals(27, line.length());
 			}
 
 		}
 		catch(IOException e) {
 			Assert.fail(e.getMessage());
 		} 
 		catch (InvalidFileException e) {
 			Assert.fail(e.getMessage());
 		}
 	}
 
 	@Test
 	public void test3LinesPerEntry() {
 		try{
 			List<String> linesFromFile = katabank.readOneEntry(testFileBufferReader);
 
 			Assert.assertEquals(3, linesFromFile.size());
 
 		}
 		catch(IOException e) {
 			Assert.fail(e.getMessage());
 		} 
 		catch (InvalidFileException e) {
 			Assert.fail(e.getMessage());
 		}
 	}
 
 	@Test
 	public void test27CharactersPerLine() {
 		try{
 			List<String> linesFromFile = katabank.readOneEntry(testFileBufferReader);
 
 			for(String line : linesFromFile) {
 				Assert.assertEquals(27, line.length());
 			}			
 		}
 		catch(IOException e) {
 			Assert.fail(e.getMessage());
 		} 
 		catch (InvalidFileException e) {
 			Assert.fail(e.getMessage());
 		}
 	}
 	
 	@Test
 	public void testInvalidNumberOfCharactersPerLine() {
 		try{
 			@SuppressWarnings("unused")
 			List<String> linesFromFile = katabank.readOneEntry(testFile2BufferReader);
 
 			Assert.fail("File with wrong number of characters, an exception should be thrown");			
 		}
 		catch (InvalidFileException e) {
 			
 		} 
 		catch (IOException e) {
 			Assert.fail(e.getMessage());
 		}
 	}
 	
 	@Test
 	public void testeTranslateOneEntry() {
 		String numInt = katabank.translateOneEntry(lines);
 		Assert.assertEquals("123456789", numInt);
 	}
 	
 	@Test
 	public void testTranslateFile() {
 		try {
			katabank.translateFile("files/testFile.txt");
			Assert.assertEquals("123456789\n123456789\n", outContent.toString());
 		} 
 		catch (InvalidFileException e) {
 			Assert.fail(e.getMessage());
 		} 
 		catch (IOException e) {
 			Assert.fail(e.getMessage());
 		}
 	}
 }
