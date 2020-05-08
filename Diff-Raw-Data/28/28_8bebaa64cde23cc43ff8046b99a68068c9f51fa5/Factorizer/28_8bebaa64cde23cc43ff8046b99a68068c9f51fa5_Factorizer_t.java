 
 package toyThreads.primeFactors;
 
 import toyThreads.util.Debug;
 import toyThreads.primeFactors.Results;
 
 public class Factorizer implements Runnable {
 
     private int myID;
     private int numberToFactorize;
     private int startValue;
     private int endValue;
 
 
     // FIXME: define an explicit value constructor that initializes all the data members
     public Factorizer(int id, int factNum, int start, int end)
     {
       myID = id;
       numberToFactorize = factNum;
       startValue = start;
       endValue = end;
     }
 
    public void run()
    {
 
      // no need to catch InterruptedException as there is no Threads.sleep(100) call here;
 
      if (Debug.DEBUG_VALUE > 5) {
    // the toString(...) method will be called to print "this"
        System.out.println("Work started by factorizer thread: " + this);
      }
 
      // FIXME: check for factors from startValue to endValue for numberToFactorize
      // FIXME: if a factor is found, add it the ArrayList in the Results class
 
     } // end run(...)
 
     public String toString() {
  String objectState = "myID = " + myID + "\nNumber im working on is " + numberToFactorize +
      "\nStarting value " + startValue + "\nEnd value " + endValue;
  // FIXME: add all the data members to a String, and return that string
  return objectState;
     }
 
 } // end class WorkerThread
 
