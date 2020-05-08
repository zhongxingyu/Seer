 package to.richard;
 
 /**
  * Author: Richard To
  * Date: 1/29/13
  */
 
 /**
  * Simplified interface for generating random numbers.
  *
  * This interface implements what is needed for my SGA.
  * This interface is needed to allow mock random objects for testing.
  */
 public interface IRandom {
 
     /**
      * Returns the next pseudorandom, uniformly distributed int value from this random number generator's sequence.
      * @param n
      * @return
      */
    public double nextInt(int n);
 
     /**
      * Returns the next pseudorandom, uniformly distributed double value between 0.0 and 1.0 from this random number generator's sequence.
      * @return
      */
     public double nextDouble();
 }
