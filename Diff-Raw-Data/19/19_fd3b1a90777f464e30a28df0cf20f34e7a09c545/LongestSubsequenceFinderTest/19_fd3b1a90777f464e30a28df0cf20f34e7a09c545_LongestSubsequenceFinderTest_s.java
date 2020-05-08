 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ru.urbancamper.audiobookmarker.text;
 
 import java.util.TreeMap;
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 /**
  *
  * @author pozpl
  */
 public class LongestSubsequenceFinderTest extends TestCase {
 
     private Integer[] fullText;
     private Integer[] partialText;
     private WordsToNumsMap wordsToNums;
 
     public LongestSubsequenceFinderTest(String testName) {
         super(testName);
     }
 
     public static Test suite() {
         TestSuite suite = new TestSuite(LongestSubsequenceFinderTest.class);
         return suite;
     }
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         this.wordsToNums = new WordsToNumsMap();
         this.fullText = this.createFullTextArray();
         this.partialText = this.createSubsequentTextArray();
     }
 
     @Override
     protected void tearDown() throws Exception {
         super.tearDown();
     }
 
     public Integer[] createFullTextArray(){
         String[] textInTokens = {"word1",  "loast", "word2", "word1", "word3", "word4"};
         Integer[] textInNumbers = this.wordsToNums.getNumbersFromWords(textInTokens);
         return textInNumbers;
     }
 
     public Integer[] createSubsequentTextArray(){
 
         String[] textInTokens = {"wor", "word1", "word2", "word3", "word4"};
         Integer[] textInNumbers = this.wordsToNums.getNumbersFromWords(textInTokens);
         return textInNumbers;
     }
 
 
     /**
      * Test of findLogestSubsequence method, of class LongestSubsequenceFinder.
      */
     public void testGetLongestSubsequenceWithMinDistance() {
 
         LongestSubsequenceFinder instance = new LongestSubsequenceFinder();
         TreeMap<Integer, Integer> expResult = new TreeMap<Integer, Integer>();
         expResult.put(Integer.valueOf(0), Integer.valueOf(1));
         expResult.put(Integer.valueOf(2), Integer.valueOf(2));
         expResult.put(Integer.valueOf(4), Integer.valueOf(3));
         expResult.put(Integer.valueOf(5), Integer.valueOf(4));
 
         TreeMap<Integer, Integer> result = instance.getLongestSubsequenceWithMinDistance(this.fullText, this.partialText);
 //        for(Integer wordsCounter = 0; wordsCounter < partialText.length; wordsCounter++){
 //            assertEquals(wordsCounter * Integer.valueOf(3), result.get(wordsCounter).intValue());
 //        }
         assertEquals(expResult, result);
     }
 
   
     /**
      * Test of longestSubsequenceLengthWithDistanceCorrection method, of class LongestSubsequenceFinder.
      */
     public void testLongestSubsequenceLengthWithDistanceCorrection() {
         System.out.println("longestSubsequenceLengthWithDistanceCorrection");
        Integer[] fullArray = null;
        Integer[] subArray = null;
         LongestSubsequenceFinder instance = new LongestSubsequenceFinder();
        TreeMap expResult = null;
        TreeMap result = instance.longestSubsequenceLengthWithDistanceCorrection(fullArray, subArray);
         assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
     }
 
 }
