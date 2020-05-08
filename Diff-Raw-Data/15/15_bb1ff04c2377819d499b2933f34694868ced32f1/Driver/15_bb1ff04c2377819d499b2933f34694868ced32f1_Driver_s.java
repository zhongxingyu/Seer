 package toyThreads.driver;
 
 import toyThreads.primeFactors.Factorizer;
 import toyThreads.util.Debug;
 import toyThreads.primeFactors.Results;
 
 public class Driver {
   public static void main(String[] args) {
     int primeToTest = 0;
     int numberOfThreads = 0;
     int debugValue = 0;
 
    if (args.length != 3) {
       System.err.println("Usage is: ant compile [PRIME_NUMBER] [NUMBER_OF_THREADS] [DEBUG_LEVEL} \n");
       System.exit(-1);
     }
 
     try {
         primeToTest = Integer.parseInt(args[0]);
         numberOfThreads = Integer.parseInt(args[1]);
         debugValue = Integer.parseInt(args[2]);
    } catch (NumberFormatException e) {
         System.err.println("All argument must be integers");
         System.exit(1);
     }
 
     // FIXME: set the DEBUG_VALUE in Debug.java
 
     // FIXME: if DEBUG_LEVEL is greater than 2, print all the argument received
 
     // FIXME: create numberOfThreads and to each thread pass an instance of Factorizer as argument
 
     try {
       // FIXME: wait for the threads to get done
       // adjust this sleep duration as needed
       Thread.sleep(10);
    } catch (InterruptedException ie) {
       // interruption of the main thread is fatal, so exit
       ie.printStackTrace();
       System.exit(-1);
     }
 
     // FIXME; print the factors using the printFactors() method of the Results class
 
     System.out.println("\n Good Bye \n");
   }
 }
