 
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
 	
 	private void testfetchArguments1 (String[] param)
 	{
 		boolean ret;
 		try
 		{
 			ret = WC.fetchArguments(param);
 			if (ret == false)
 			{
 				fail("Wc.fetchArguments() hit an exception.");
 			}
 		}
 		catch(Exception e)
 		{
 			fail("Wc.fetchArguments() hit an exception.");
 		}
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
 	
     public void testAudioFile() {   	
     	System.out.println("Audio");
     	wordCountTest("res/mp3file.mp3", 72428);
 	}
     
 	public void testOneDelim()
 	{
     	System.out.println("1Delim");
     	WC.strDelimiters = "f";
 		wordCountTest("res/testFileOneDelim.doc", 14);
 	}
 	
 	public void testaabbcc()
 	{
     	System.out.println("aabbcc");
 //    	WC.strDelimiters = "d";
     	//WC.iThreshold = 9;
 		wordCountTest("res/testaabbcc.txt", 0);
 	}
 	
 	public void testFetchnormal() {
 //		String delims = "d";
 //		String threshold = "30";
 //		WC.fetchArguments(delims, threshold);
 //		
 //		assertEquals(WC.threshold, 30);
 		String param[] = {"ajay.txt", "-l", "123", "-c", "1ef" };
 		testfetchArguments1(param);
 		//fail("Not yet implemented");
 		
 	}
 
 	public void testFetchWrongThreshold() {
 		String param[] = {"ajay1.txt", "-l", "abc", "-c", "1ef" };
 		
 		
 		boolean exceptionCaught = false;
 		try
 		{
 			WC.fetchArguments(param);
 		}
 		catch(Exception e)
 		{
 			exceptionCaught = true;
 		}
 		
 		assert(exceptionCaught);
 		
 	}	
 
 }
