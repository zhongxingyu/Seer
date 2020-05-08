 package edu.brown.cs32.bweedon.random;
 
 import java.util.Random;
 
 /**
  * a class created because Java random stuff is bad
  *
  * @author Ben Weedon (bweedon)
  */
 public class BweedonRandom {
 
     /**
      * produces a random int in the range <i>min</i> to <i>max</i>
      * inclusive.
      *
      * @param min the minimum value
      * @param max the maximum value
      * @return a random int
      */
     public static int randomInt(int min, int max) {
         assert max >= min;
         
         Random generator = new Random(System.currentTimeMillis());
         int firstInt = generator.nextInt(max - min + 1);
         return firstInt + min;
     }
 
     /**
      * produces a random double in the range <i>min</i> to <i>max</i> inclusive.
      *
      * @param min the minimum value
      * @param max the maximum value
      * @return a random double
      */
     public static double randomDouble(double min, double max) {
         assert max >= min;
         
         Random generator = new Random(System.currentTimeMillis());
         double firstDouble = generator.nextDouble();
        return (firstDouble * (max - min + 1)) + min;
     }
 }
