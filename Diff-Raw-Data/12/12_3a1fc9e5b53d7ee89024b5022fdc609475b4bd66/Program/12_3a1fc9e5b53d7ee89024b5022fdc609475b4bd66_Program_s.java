 package driverProg;
 
 import emoAlgorithms.*;
 
 public class Program {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		
 		// first define a problem to solve;
		DTLZ1 myProb = new DTLZ1(3,2);
 		
 		
		//Creating an instance of algorithm to solve the problem
 		NSGA2 myAlg = new NSGA2(myProb);
 		myAlg.run();
 
 		System.out.println("Hello World~");
 	}
 
 }
