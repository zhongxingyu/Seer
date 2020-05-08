 package toyThreads.primeFactors;
 
 import toyThreads.util.Debug;
 import toyThreads.primeFactors.Results;
 
 public class Factorizer implements Runnable {
   private int myID;
   private int numberToFactorize;
   private int startValue;
   private int endValue;
 
   // FIXME: define an explicit value constructor that initializes all the data members
 
   public void run() {
    if(Debug.DEBUG_VALUE > 5) {
       System.out.println("Work started by factorizer thread: " + this);
     }
 
     // FIXME: check for factors from startValue to endValue for numberToFactorize
     // FIXME: if a factor is found, add it the ArrayList in the Results class
   }
 
   public String toString() {
     String objectState = null;
     // FIXME: add all the data members to a String, and return that string
     return objectState;
   }
 }
