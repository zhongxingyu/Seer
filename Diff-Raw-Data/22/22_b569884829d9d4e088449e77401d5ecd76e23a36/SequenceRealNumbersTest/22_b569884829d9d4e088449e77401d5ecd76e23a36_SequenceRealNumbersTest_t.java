 package com.github.ivanshchitov.sequencerealnumbers;
 
 import org.junit.Test;
 
 import static org.junit.Assert.assertEquals;
 
 
 /**
  * Test class for SequenceRealNumbers class.
  *
  * @author Ivan Shchitov
  */
 public class SequenceRealNumbersTest {
 
     /**
      * Tests getMaximum() method with one element.
      */
     @Test
     public void getMaximumTest1() {
         SequenceRealNumbers sequenceTest = new SequenceRealNumbers();
         sequenceTest.add(1.0);
         assertEquals(
                 Double.doubleToLongBits(1),
                 Double.doubleToLongBits(sequenceTest.getMaximum())
         );
         System.out.println("Finished test on getMaximum() method with one element.");
     }
 
     /**
      * Tests getMaximum() method with same elements.
      */
     @Test
     public void getMaximumTest2() {
         SequenceRealNumbers sequenceTest = new SequenceRealNumbers();
         sequenceTest.add(1.0);
         sequenceTest.add(1.0);
         sequenceTest.add(1.0);
         assertEquals(
                 Double.doubleToLongBits(1),
                 Double.doubleToLongBits(sequenceTest.getMaximum())
         );
         System.out.println("Finished test on getMaximum() method with same elements.");
     }
 
     /**
      * Tests getMaximum() method with negative elements.
      */
     @Test
     public void getMaximumTest3() {
         SequenceRealNumbers sequenceTest = new SequenceRealNumbers();
         sequenceTest.add(-1.0);
         sequenceTest.add(-2.0);
         assertEquals(
                 Double.doubleToLongBits(-1),
                 Double.doubleToLongBits(sequenceTest.getMaximum())
         );
         System.out.println("Finished test on getMaximum() method with negative elements.");
     }
 
     /**
      * Tests getMinimum() method with one element.
      */
     @Test
     public void getMinimumTest1() {
         SequenceRealNumbers sequenceTest = new SequenceRealNumbers();
         sequenceTest.add(1.0);
         assertEquals(
                 Double.doubleToLongBits(1),
                 Double.doubleToLongBits(sequenceTest.getMinimum())
         );
         System.out.println("Finished test on getMinimum() method with one element.");
     }
 
     /**
      * Tests getMinimum() method with same element.
      */
     @Test
     public void getMinimumTest2() {
         SequenceRealNumbers sequenceTest = new SequenceRealNumbers();
         sequenceTest.add(1.0);
         sequenceTest.add(1.0);
         sequenceTest.add(1.0);
         assertEquals(
                 Double.doubleToLongBits(1),
                 Double.doubleToLongBits(sequenceTest.getMinimum())
         );
         System.out.println("Finished test on getMinimum() method with same element.");
     }
 
     /**
      * Tests getMinimum() method with negative element.
      */
     @Test
     public void getMinimumTest3() {
         SequenceRealNumbers sequenceTest = new SequenceRealNumbers();
         sequenceTest.add(-1.0);
         sequenceTest.add(-2.0);
         assertEquals(
                 Double.doubleToLongBits(-2),
                 Double.doubleToLongBits(sequenceTest.getMinimum())
         );
         System.out.println("Finished test on getMinimum() method with negative element.");
     }
 
     /**
      * Tests getAverage() method with sum elements = 0.
      */
     @Test
     public void getAverageTest1() {
         SequenceRealNumbers sequenceTest = new SequenceRealNumbers();
         sequenceTest.add(1.0);
         sequenceTest.add(0);
         sequenceTest.add(-1);
         assertEquals(
                 Double.doubleToLongBits(0),
                 Double.doubleToLongBits(sequenceTest.getAverage())
         );
         System.out.println("Finished test on getAverage() method with sum elements = 0.");
     }
 
     /**
      * Tests getAverage() method with same elements.
      */
     @Test
     public void getAverageTest2() {
         SequenceRealNumbers sequenceTest = new SequenceRealNumbers();
         sequenceTest.add(1.0);
         sequenceTest.add(1.0);
         sequenceTest.add(1);
         assertEquals(
                 Double.doubleToLongBits(1),
                 Double.doubleToLongBits(sequenceTest.getAverage())
         );
         System.out.println("Finished test on getAverage() method with same elements.");
     }
 
     /**
      * Tests getMedian() with an even size of sequence.
      */
     @Test
     public void evenSizeSequenceMedian() {
         SequenceRealNumbers sequenceTest = new SequenceRealNumbers();
         sequenceTest.add(2.0);
         sequenceTest.add(0.2);
         assertEquals(
                 Double.doubleToLongBits(1.1),
                 Double.doubleToLongBits(sequenceTest.getMedian())
         );
         System.out.println("Finished test on getMedian() method with even size.");
     }
 
     /**
      * Tests getMedian() with an even size of sequence.
      */
     @Test
     public void unevenSizeSequenceMedian() {
         SequenceRealNumbers sequenceTest = new SequenceRealNumbers();
         sequenceTest.add(1.0);
         assertEquals(
                 Double.doubleToLongBits(1),
                 Double.doubleToLongBits(sequenceTest.getMedian())
         );
         System.out.println("Finished test on getMedian() method with uneven size.");
     }
 
     /**
      * Tests getSize() method.
      */
     @Test
     public void getSizeTest() {
         SequenceRealNumbers sequenceTest = new SequenceRealNumbers();
         sequenceTest.add(1.0);
         sequenceTest.add(0.2);
         sequenceTest.add(3);
         assertEquals(
                 Double.doubleToLongBits(3),
                 Double.doubleToLongBits(sequenceTest.getSize())
         );
         System.out.println("Finished test on getSize() method.");
     }
 
     /**
      * Tests getElement() method.
      */
     @Test
     public void getElementTest() {
         SequenceRealNumbers sequenceTest = new SequenceRealNumbers();
         sequenceTest.add(1.0);
        try {
            sequenceTest.getElement(-1);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Finished test on getElement() method.\n"
                    + "We caught ArrayIndexOutOfBoundsException.");
        }
     }
 }
