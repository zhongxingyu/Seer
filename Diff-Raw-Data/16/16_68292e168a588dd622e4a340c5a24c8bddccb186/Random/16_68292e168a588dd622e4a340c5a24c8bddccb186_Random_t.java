 package to.richard;
 
 /**
  * Author: Richard To
  * Date: 1/29/13
  */
 
 /**
  * Wrapper class for java.util.Random.
  *
  * This class only provides a subset of the functionality
  * and implements the IRandom interface.
  */
 public class Random implements IRandom {
 
     private java.util.Random _random;
 
     public Random() {
         _random = new java.util.Random();
     }
 
     /**
      * Construct a random number generator with specific seed.
      * @param seed
      */
     public Random(long seed) {
         _random = new java.util.Random(seed);
     }
 
     @Override
    public int nextInt(int n) {
         return _random.nextInt(n);
     }
 
     @Override
     public double nextDouble() {
         return _random.nextDouble();
     }
 }
