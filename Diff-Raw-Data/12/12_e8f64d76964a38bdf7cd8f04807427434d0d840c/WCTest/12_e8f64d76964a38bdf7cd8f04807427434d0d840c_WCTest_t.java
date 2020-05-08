 package wordCounterTest;
 
 import wordCounter.WC;
 import junit.framework.TestCase;
 import java.io.*;
 import java.net.URL;
 
 public class WCTest extends TestCase {
 
 	public void setUp()
 	{
 		WC.iThreshold = WC.DEFAULT_THRESHOLD;
 		WC.strDelimiters = WC.DEFAULT_DELIMITERS;
 	}
 	
 	private void wordCountTest(String testFilename, int expectedCount) {
 
 		try {			
 			Class<WCTest> c = WCTest.class;
 			ClassLoader cl = c.getClassLoader();
 			URL url = cl.getResource(testFilename);
 			String fullPath = url.getPath();
 			String replacedPath = fullPath.replaceAll("%20", " ");
 			
 			FileReader r = new FileReader(replacedPath);
 			WC.fileInput = r;
 		} catch (FileNotFoundException e){
 			fail("Test file " + testFilename + " not found.");
 		}		
 		
 		int result = -1;
 		try {
 			result = WC.countWords();
 		} catch (Exception e)
 		{
 			fail("Wc.countWords() hit an exception.");
 		}
 		
 		assertEquals(expectedCount, result);
 	}
 	
 	public void testCountWordsEmptyFile() {
 		
 		wordCountTest("res/testFileEmpty.txt", 0);
 		
 	}
     
     public void testCountWordsOneWordFile1() {   	
     	wordCountTest("res/testFileOneWord1.txt", 1);
 	}
     
     public void testCountWordsOneWordFile2() {
     	wordCountTest("res/testFileOneWord2.txt", 1);
 	}
 	
 	public void testCountWordsNormalWordFile() {
 		wordCountTest("res/testFileNormal.txt", 8);
 	}
 	
 	public void testCountWordsNormalWordFileAsJpeg() {
 		wordCountTest("res/testFileNormalAsJpeg.jpeg", 8);
 	}
 	
 	public void testCountWordsJpeg()
 	{
 		wordCountTest("res/testFileCatJpeg.jpeg", 15133);
 	}
 	
 	public void testCountWordsCustomThresholdLength() {
 		WC.iThreshold = 4;
 		wordCountTest("res/testFileNormal.txt", 5);
 	}
 	
 	public void testCountWordsCustomDelimiters() {
 		WC.strDelimiters = "d";
 		wordCountTest("res/testFileDC.txt", 3);
 	}
 	
 	public void testCountWordsNoDelimitersTest() {
 		WC.strDelimiters = "";
 		wordCountTest("res/testFileNormal.txt", 0);
 	}
 
 	public void testFetch() {
 		String delims = "d";
 		String threshold = "30";
 		
 		String[] args = {"irrelevantFilename", "-c", delims, "l", threshold };
 		WC.fetchArguments( args );
 		
 		int expected = 30;
 		assertEquals(expected, WC.iThreshold);
 	}
 	
 	public void testCountWordsThresholdNine() {
 		WC.iThreshold = 9;
 		wordCountTest("res/testFileSDP.txt", 1);
 	}
 
 }
