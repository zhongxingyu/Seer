 // Copyright © 2012 Steve McCoy under the MIT license.
 package edu.unh.cs.tact;
 
 import java.util.concurrent.*;
 
 class BreakMe implements Runnable{
 	public int someField;
 	public double piDiv = 1, piEst = 0;
 	public double[] someThings;
 	@ReadOnly public int readMe = 515;
 
 	@GuardedBy("this") public int princess;
 
	private class Worker extends Thread{
		public Worker(String name){
			super(name);
		}
	}

	public BreakMe(){
		Thread t = new Worker("work it");
	}

 	public static void codeToBreak(){
 		BreakMe bm = new BreakMe();
 		bm.someField = 666;
 		bm.someThings = new double[4];
 		int n = bm.readMe;
 
		bm.princess = 666;
 	//	new Thread(bm, "that's broken!").start();
 	}
 
 	public static void codeThatWorks(){
 		BreakMe bm = new BreakMe();
 		bm.someField = 111;
 		Checker.release(bm);
 
 		synchronized(bm){
 			bm.princess = 99;
 		}
 
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
 		//someField = 42;
 		//someThings[0] = 3;
 		readMe = 666;
 		Checker.release(this);
 	}
 
 	public static void pi(){
 		BreakMe bm = new BreakMe();
 
 		for(int i = 0; i < 1e9; i++){
 			bm.piEst = bm.piEst + 4/bm.piDiv;
 			double d = Math.abs(bm.piDiv) + 2;
 			bm.piDiv = bm.piDiv < 0 ? d : -d;
 		}
 
 		System.out.printf("π ≅ %f\n", bm.piEst);
 	}
 }
