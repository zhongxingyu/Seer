 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ru.urbancamper.audiobookmarker.text;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.BitSet;
 import java.util.List;
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
         Integer[] textWords = {1, 2, 3, 4, 5, 1, 2, 5, 1, 1, 1, 1,1, 1, 1,1,
         1,5,5, 6, 7};
         Integer wordToFind = 5;
         Method method = BitapSubtextFinding.class.getDeclaredMethod("fillByteArrayFromWordsNumbersArray",
                 Integer[].class, Integer.class);
         method.setAccessible(true);
         Byte[] output;
         output = (Byte[]) method.invoke(bitapSubtextFinding,
                  textWords, wordToFind);
 
         assertEquals(3, output.length);
 
         Byte idealValue = -112;
         assertEquals(idealValue, output[0]);
         idealValue = 0;
         assertEquals(idealValue, output[1]);
         idealValue = 6;
         assertEquals(idealValue, output[2]);
     }
 
 //    public void testShiftBitsRight(){
 //        Byte[] bytesArray = new Byte[]{1, 4, 8, 16};
 //
 //        this.bitapSubtextFinding.shiftBitsRight(bytesArray, 1);
 //        Byte[] idealResult = new Byte[]{0, 2, 8, 16};
 //        for(Integer elemCounter = 0; elemCounter < idealResult.length; elemCounter++){
 //            assertEquals(idealResult[elemCounter], bytesArray[elemCounter]);
 //        }
 //    }
     public void testShiftBitsLeft() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
         Byte[] bytesArray = new Byte[]{1, 4, 8, 16};
 
         Method method = BitapSubtextFinding.class.getDeclaredMethod("shiftBitsLeft",
                 new Class[]{Byte[].class});
         method.setAccessible(true);
 
         bytesArray  = (Byte[])method.invoke(bitapSubtextFinding, new Object[]{ bytesArray});
 
 //        this.bitapSubtextFinding.shiftBitsLeft(bytesArray);
         Byte[] idealResult = new Byte[]{2, 8, 16, 32};
         for(Integer elemCounter = 0; elemCounter < idealResult.length; elemCounter++){
             assertEquals(idealResult[elemCounter], bytesArray[elemCounter]);
         }
 
         bytesArray = new Byte[]{-128, 4, 8, 16};
         idealResult = new Byte[]{0, 9, 16, 32};
 
         bytesArray  = (Byte[])method.invoke(bitapSubtextFinding, new Object[]{ bytesArray});
 
 //        this.bitapSubtextFinding.shiftBitsLeft(bytesArray);
         for(Integer elemCounter = 0; elemCounter < idealResult.length; elemCounter++){
             assertEquals(idealResult[elemCounter], bytesArray[elemCounter]);
         }
     }
 
     public void testByteArrayAnd() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
         Byte[] firstArray = new Byte[]{1, 4, -1, -1};
         Byte[] secondArray = new Byte[]{1, 5, -128, -3};
 
         Method method = BitapSubtextFinding.class.getDeclaredMethod("byteArrayAnd",
                 Byte[].class, Byte[].class);
         method.setAccessible(true);
 
         Byte[] bytesArray =  (Byte[])method.invoke(bitapSubtextFinding, firstArray, secondArray);
 
 //        Byte[] bytesArray =  this.bitapSubtextFinding.byteArrayAnd(firstArray, secondArray);
         Byte[] idealResult = new Byte[]{1, 4, -128, -3};
         for(Integer elemCounter = 0; elemCounter < idealResult.length; elemCounter++){
             assertEquals(idealResult[elemCounter], bytesArray[elemCounter]);
         }
 
     }
 
     /**
      * Test of fillBitSetFromWordsNumberArray method, of class BitapSubtextFinding.
      */
     public void testFillBitSetFromWordsNumberArray() {
         System.out.println("fillBitSetFromWordsNumberArray");
         Integer[] textWords = {1, 2, 3, 4, 5, 1, 2, 5, 1, 1, 1, 1,1, 1, 1,1,
         1,5,5, 6, 7};
         Integer wordToFind = 5;
         BitSet result =
                 this.bitapSubtextFinding.fillBitSetFromWordsNumberArray(textWords, wordToFind);
 
         assertTrue(result.get(4));
         assertTrue(result.get(7));
         assertTrue(result.get(17));
         assertTrue(result.get(18));
         assertFalse(result.get(textWords.length -1));
     }
 
     /**
      * Test of shiftBitSetLeft method, of class BitapSubtextFinding.
      */
     public void testShiftBitSetLeft() {
         System.out.println("shiftBitSetLeft");
         BitSet bitSet = new BitSet(10);
         bitSet.set(0);
         bitSet.set(1);
         bitSet.set(5);
         bitSet.set(9);
         bitSet.set(8);
 
 
         BitSet result = this.bitapSubtextFinding.shiftBitSetLeft(bitSet);
 
         assertFalse(result.get(0));
         assertTrue(result.get(1));
         assertTrue(result.get(2));
         assertFalse(result.get(3));
         assertTrue(result.get(6));
         assertTrue(result.get(9));
         assertFalse(result.get(8));
     }
 
     public void testFind(){
         Integer[] textWords = {1, 2, 3, 4, 5, 1, 2, 5, 1, 1, 1, 1,1, 1, 1,1,
         1,2 , 3, 6, 7};
         Integer[] subTextWords = {1, 2, 3, 4};
 
         List<Integer> foundResults= this.bitapSubtextFinding.find(textWords, subTextWords, subTextWords.length/2);
         List<Integer> idealResults = new ArrayList<Integer>();
         idealResults.add(0);
         idealResults.add(5);
         idealResults.add(16);
 
         assertEquals(idealResults, foundResults);
     }
 
     /**
      * Test of findWithReducedError method, of class BitapSubtextFinding.
      */
     public void testFindWithReducedError() {
         Integer[] textWords = {1, 2, 3, 4, 5, 1, 2, 5, 1, 1, 1, 1,1, 1, 1,1,
         1,2 , 3, 6, 7};
         Integer[] subTextWords = {1, 2, 3, 4};
 
         Integer foundResult= this.bitapSubtextFinding.findWithReducedError(
                textWords, subTextWords, subTextWords.length/2);
         Integer idealResult =  0;
         assertEquals(idealResult, foundResult);
     }
 }
