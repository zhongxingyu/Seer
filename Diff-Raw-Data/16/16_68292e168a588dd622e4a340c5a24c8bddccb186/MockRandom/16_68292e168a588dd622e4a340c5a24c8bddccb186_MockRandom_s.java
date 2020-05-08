 package to.richard;
 
 import java.util.ArrayList;
 
 /**
  * Author: Richard To
  * Date: 1/29/13
  */
 
 /**
  * Mocks random number generation with a known sequence. Ideal for unit testing.
  */
 public class MockRandom implements IRandom {
 
     ArrayList<Double> _doubleSequence;
     ArrayList<Integer> _integerSequence;
 
     /**
      * If using getDouble, make sure to set the sequence of doubles.
      * @param doubleSequence
      */
     public MockRandom setDoubleSequence(ArrayList<Double> doubleSequence) {
         this._doubleSequence = doubleSequence;
         return this;
     }
 
     /**
      * If using getInt(int n), make sure to set the sequence of integers first.
      * @param integerSequence
      */
     public MockRandom setIntegerSequence(ArrayList<Integer> integerSequence) {
         this._integerSequence = integerSequence;
         return this;
     }
 
     @Override
     /**
      * Make sure to set a list of integer with at least the amount of numbers
      * needed for your test.
      *
      * Additionally, the n parameter is ignored. Make sure your numbers make sense.
      */
    public double nextInt(int n) {
         return this._integerSequence.remove(0);
     }
 
     @Override
     /**
      * Make sure to set a list of doubles with at least the amount of numbers
      * needed for your test.
      */
     public double nextDouble() {
         return this._doubleSequence.remove(0);
     }
 }
