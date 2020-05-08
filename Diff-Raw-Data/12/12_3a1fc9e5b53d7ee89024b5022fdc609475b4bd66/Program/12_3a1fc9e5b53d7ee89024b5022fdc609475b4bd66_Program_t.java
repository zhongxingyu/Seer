 package driverProg;
 
 import emoAlgorithms.*;
 
 public class Program {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		
 		// first define a problem to solve;
		ZDT myProb = new ZDT1(3);
 		
 		
		// Creating an instance of algorithm to solve the problem
 		NSGA2 myAlg = new NSGA2(myProb);
 		myAlg.run();
 
 		System.out.println("Hello World~");
 	}
 
 }
