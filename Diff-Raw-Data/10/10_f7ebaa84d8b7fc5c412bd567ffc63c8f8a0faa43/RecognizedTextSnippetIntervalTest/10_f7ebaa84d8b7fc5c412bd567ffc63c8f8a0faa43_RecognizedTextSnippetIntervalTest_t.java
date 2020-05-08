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
 //    public void testCalculateFullTextBoundsForRecognizedSnippet() {
 //        System.out.println("calculateFullTextBoundsForRecognizedSnippet");
 //        Integer[] fullText = {1, 2, 3};
 //        Integer[] subText = {1, 2, 3};
 //        RecognizedTextSnippetInterval instance = new RecognizedTextSnippetInterval();
 //        Integer[] expResult = null;
 //        Integer[] result = instance.calculateFullTextBoundsForRecognizedSnippet(fullText, subText);
 //        assertEquals(expResult, result);
 //        // TODO review the generated test code and remove the default call to fail.
 //        fail("The test case is a prototype.");
 //    }
 
     @SuppressWarnings("unchecked")
     public void testWordsFrequencesForTextSnippet() throws NoSuchMethodException,
             InvocationTargetException, IllegalAccessException {
 
         Integer[] snippetWords = {1, 2, 3, 1, 3};
 
         Method method = RecognizedTextSnippetInterval.class.getDeclaredMethod("wordsFrequencesForTextSnippet",
                 Integer[].class, Integer.class, Integer.class);
         method.setAccessible(true);
         TreeMap<Integer, Integer> output;
         output = (TreeMap<Integer, Integer>) method.invoke(recognizedTextSnippetInterval,
                  snippetWords, Integer.valueOf(0), Integer.valueOf(5));
 
         assertEquals(3, output.size());
         assertEquals(Integer.valueOf(2), output.get(1));
         assertEquals(Integer.valueOf(1), output.get(2));
         assertEquals(Integer.valueOf(2), output.get(3));
     }
     public void testAggregatedSummForWorsFrequenciesVectors() throws NoSuchMethodException,
             InvocationTargetException, IllegalAccessException {
 
 
         TreeMap<Integer, Integer> fullTestSnippetFreqs  = new TreeMap<Integer, Integer>();
         TreeMap<Integer, Integer> subTestSnippetFreqs =  new TreeMap<Integer, Integer>();
 
         fullTestSnippetFreqs.put(1, 1);
         fullTestSnippetFreqs.put(2, 2);
         fullTestSnippetFreqs.put(3, 3);
         fullTestSnippetFreqs.put(4, 4);
 
         subTestSnippetFreqs.put(2, 2);
         subTestSnippetFreqs.put(3, 3);
         subTestSnippetFreqs.put(4, 4);
         subTestSnippetFreqs.put(5, 5);
 
         Method method = RecognizedTextSnippetInterval.class.getDeclaredMethod("aggregatedSummForWorsFrequenciesVectors",
                 TreeMap.class, TreeMap.class);
         method.setAccessible(true);
         Integer output;
         output = (Integer) method.invoke(recognizedTextSnippetInterval,
                  fullTestSnippetFreqs, subTestSnippetFreqs);
 
         assertEquals(Integer.valueOf(26), output);
     }
     public void testupdateAggregatedSum() throws NoSuchMethodException,
             InvocationTargetException, IllegalAccessException {
 
 
         TreeMap<Integer, Integer> fullTestSnippetFreqs  = new TreeMap<Integer, Integer>();
         TreeMap<Integer, Integer> subTestSnippetFreqs =  new TreeMap<Integer, Integer>();
 
         fullTestSnippetFreqs.put(1, 1);
         fullTestSnippetFreqs.put(2, 2);
         fullTestSnippetFreqs.put(3, 3);
         fullTestSnippetFreqs.put(4, 4);
        fullTestSnippetFreqs.put(6, 1);
 
         subTestSnippetFreqs.put(2, 2);
         subTestSnippetFreqs.put(3, 3);
         subTestSnippetFreqs.put(4, 4);
         subTestSnippetFreqs.put(5, 4);

 
         Method method = RecognizedTextSnippetInterval.class.getDeclaredMethod("updateAggregatedSum",
                 TreeMap.class, TreeMap.class, Integer.class, Integer.class, Integer.class, Integer.class);
         method.setAccessible(true);
         Integer output;
         output = (Integer) method.invoke(recognizedTextSnippetInterval,
                  fullTestSnippetFreqs, subTestSnippetFreqs, 5, 5, 6, 26);
//        Integer output = this.recognizedTextSnippetInterval.updateAggregatedSum(
//                fullTestSnippetFreqs, subTestSnippetFreqs, 5, 5, 6, 26);
 
        assertEquals(Integer.valueOf(26), output);
     }
 }
