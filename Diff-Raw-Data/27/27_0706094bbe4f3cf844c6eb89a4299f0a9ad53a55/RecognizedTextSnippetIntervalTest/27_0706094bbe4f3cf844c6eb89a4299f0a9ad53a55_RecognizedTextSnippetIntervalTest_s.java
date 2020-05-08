 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ru.urbancamper.audiobookmarker.text;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.TreeMap;
 import junit.framework.Test;
 import junit.framework.TestCase;
 
 /**
  *
  * @author pozpl
  */
 public class RecognizedTextSnippetIntervalTest extends TestCase {
 
     private RecognizedTextSnippetInterval recognizedTextSnippetInterval;
 
     public RecognizedTextSnippetIntervalTest(String testName) {
         super(testName);
     }
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         this.recognizedTextSnippetInterval = new RecognizedTextSnippetInterval();
     }
 
     @Override
     protected void tearDown() throws Exception {
         super.tearDown();
     }
 
     /**
      * Test of calculateFullTextBoundsForRecognizedSnippet method, of class RecognizedTextSnippetInterval.
      */
    public void testCalculateFullTextBoundsForRecognizedSnippet() {
        System.out.println("calculateFullTextBoundsForRecognizedSnippet");
        Integer[] fullText = null;
        Integer[] subText = null;
        RecognizedTextSnippetInterval instance = new RecognizedTextSnippetInterval();
        Integer[] expResult = null;
        Integer[] result = instance.calculateFullTextBoundsForRecognizedSnippet(fullText, subText);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
 
     public void testWordsFrequencesForTextSnippet() throws NoSuchMethodException,
             InvocationTargetException, IllegalAccessException {
 
         Integer[] snippetWords = {1, 2, 3, 1, 3};
 
         Method method = RecognizedTextSnippetInterval.class.getDeclaredMethod("wordsFrequencesForTextSnippet",
                 Integer[].class, Integer.class, Integer.class);
         method.setAccessible(true);
        TreeMap<Integer, Integer> output = (TreeMap<Integer, Integer>) method.invoke(recognizedTextSnippetInterval, snippetWords, 0, 5);
 
         assertEquals(3, output.size());
 //        assertEquals(2, output.get(1));
 //        assertEquals(1, output.get(2));
 //        assertEquals(2, output.get(3));
     }
 }
