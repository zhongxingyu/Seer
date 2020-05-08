 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ru.urbancamper.audiobookmarker.text;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import junit.framework.TestCase;
 
 /**
  *
  * @author pozpl
  */
 public class BitapSubtextFindingTest extends TestCase {
 
     private BitapSubtextFinding bitapSubtextFinding;
 
     public BitapSubtextFindingTest(String testName) {
         super(testName);
     }
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         this.bitapSubtextFinding = new BitapSubtextFinding();
     }
 
     @Override
     protected void tearDown() throws Exception {
         super.tearDown();
     }
 
     public void testFillByteArrayFromWordsNumbersArray() throws NoSuchMethodException,
             InvocationTargetException, IllegalAccessException {
         Integer[] textWords = {1, 2, 3, 4, 5};
         Integer wordToFind = 3;
         Method method = BitapSubtextFinding.class.getDeclaredMethod("fillByteArrayFromWordsNumbersArray",
                 Integer[].class, Integer.class);
         method.setAccessible(true);
         Byte[] output;
         output = (Byte[]) method.invoke(bitapSubtextFinding,
                  textWords, wordToFind);
        Byte idealValue = 8;
         assertEquals(idealValue, output[0]);
     }
 }
