 // Copyright © 2012 Steve McCoy under the MIT license.
 package edu.unh.cs.tact;
 
 import java.util.concurrent.*;
 
 class BreakMe implements Runnable{
 	public int someField;
 	public double piDiv = 1, piEst = 0;
 
 	public static void codeToBreak(){
 		BreakMe bm = new BreakMe();
 		bm.someField = 666;
 
 		new Thread(bm, "that's broken!").start();
 	}
 
 	public static void codeThatWorks(){
 		BreakMe bm = new BreakMe();
 		bm.someField = 111;
		Checker.release(bm);
 
 		Thread t = new Thread(bm, "that's good!");
 		t.start();
 		try{
 			t.join();
 		}catch(InterruptedException e){
 			// TODO: whatever
 			System.err.println("Join was interrupted, try again");
 		}
 
 		bm.someField = 666;
 	}
 
 	public void run(){
 		someField = 42;
 		Checker.release(this);
 	}
 
 	public static void pi(){
 /*
 		BreakMe bm = new BreakMe();
 
 		for(int i = 0; i < 1e9; i++){
 			double t = bm.piEst + 4/bm.piDiv;
 			bm.piEst = t;
 			double d = Math.abs(bm.piDiv) + 2;
 			double dd = bm.piDiv < 0 ? d : -d;
 			bm.piDiv = dd;
 		}
 
 		System.out.printf("π ≅ %f\n", bm.piEst);
 */
 	}
 }
