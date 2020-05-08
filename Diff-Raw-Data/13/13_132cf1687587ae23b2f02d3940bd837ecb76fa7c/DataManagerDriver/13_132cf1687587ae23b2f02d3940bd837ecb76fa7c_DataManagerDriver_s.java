 package edu.nyu.cs.adb;
 
 public class DataManagerDriver {
 	/**
 	 * Test function. Do not call in actual program.
 	 * @param args
 	 */
 	public static void main(String args[]) {
		TransactionManager transactionManager 
			= new TransactionManager(System.in, System.out);
		DataManager manager = new DataManager(transactionManager, 1);
 		System.out.print(manager.dump());
 		System.out.println(manager.dump("x1"));
 		System.out.println(manager.dump("x2"));
 	}
 }
