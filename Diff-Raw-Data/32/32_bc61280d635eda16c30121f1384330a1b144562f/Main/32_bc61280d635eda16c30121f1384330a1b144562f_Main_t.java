 package edu.buffalo.cse605.main;
 
 import edu.buffalo.cse605.list.fine.CursorFine;
 import edu.buffalo.cse605.list.fine.FDListFine;
 import edu.buffalo.cse605.test.testThread2;
 
 public class Main {
 	public static void main(String[] args) {
 		FDListFine<String> f;		
 		f = new FDListFine<String> ("hi");
 		long ttime = 0;
 	
 //		FDListCoarse<String>  = new FDListCoarse<String> ("hi");
		int nt = 64;//Integer.parseInt(args[0]);
 //		CursorFine<String> c;
 //		c = f.reader( f.head() );
 //		
 //		for ( int i = 0; i < 10000; i++ ) {
 //			c.writer().insertBefore(i+"");
 //		}
 //		c.prev();
 		
 
 		System.out.println("=========== No.of Threads => " + nt + "=========");
 		for (int i = 0; i < 10; i++) {
 			long startTime = System.currentTimeMillis();
 			Thread[] threads = new Thread[nt];
 			for (int j = 0; j < nt ; j++) {
 				threads[j] = new Thread(new testThread2(f, nt));
 				threads[j].start();
 			}
 			
 			for (int j = 0; j < nt; j++) {
 				try {
 					threads[j].join();
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 
 			long endTime   = System.currentTimeMillis();
 			long totalTime = endTime - startTime;
 			ttime += totalTime;
 		}
		System.out.println("Total running time => " + ttime/10);
 
 		
 //		System.out.println("count => " + f.count.get());
 //		System.out.println("current element pointed by cursor..." + c.curr().toString());
 //		
 //		if (f.head() == c.curr()) {
 //			System.out.println("true");
 //		} else {
 //			System.out.println("false");
 //		}		
 //		
 //		System.out.println("current element pointed by cursor..." + c.curr().toString());
 //		c.next();
 //		System.out.println("head in list..." + f.head().value());
 //		
 //		// #Test 1
 //		c.writer().insertAfter("after");
 //		c.writer().delete();
 //		c.writer().insertBefore("before");
 //		c.writer().delete();
 //		
 //		// # Test2
 //		// Check whether list is circular
 //		if (f.head() == c.curr()) {
 //			System.out.println("true");
 //		} else {
 //			System.out.println("false");
 //		}
 //
 //		// # Test 3
 //		// Insert new element AFTER
 //		c.writer().insertAfter("1a");
 //		c.writer().insertAfter("2a");
 //		c.writer().insertAfter("3a");
 //		c.writer().insertAfter("4a");
 //		// # Validate Test 3;
 //		System.out.println("current element pointed by cursor..." + c.curr().toString());
 //		c.prev();
 //		System.out.println("current element pointed by cursor..." + c.curr().toString());
 //		c.prev();
 //		System.out.println("current element pointed by cursor..." + c.curr().toString());
 //		c.prev();
 //		System.out.println("current element pointed by cursor..." + c.curr().toString());
 //		c.prev();
 //		System.out.println("current element pointed by cursor..." + c.curr().toString());
 //		// # Test 4
 //		// Insert new element BEFORE
 //		c.writer().insertBefore("b4");
 //		c.writer().insertBefore("b3");
 //		c.writer().insertBefore("b2");
 //		c.writer().insertBefore("b1");
 //		// # Validate Test 3;
 //		System.out.println("current element pointed by cursor..." + c.curr().toString());
 //		c.next();
 //		System.out.println("current element pointed by cursor..." + c.curr().toString());
 //		c.next();
 //		System.out.println("current element pointed by cursor..." + c.curr().toString());
 //		c.next();
 //		System.out.println("current element pointed by cursor..." + c.curr().toString());
 //		c.next();
 //		System.out.println("current element pointed by cursor..." + c.curr().toString());
 //		// # Test 5
 //		// Delete all keys
 //		c.writer().delete();
 //		c.writer().delete();
 //		c.writer().delete();
 //		c.writer().delete();
 //		c.writer().delete();
 //		c.writer().delete();
 //		c.writer().delete();
 //		c.writer().delete();
 //		c.writer().delete();
 //		
 //		// deleted last element ?
 //		if (c.curr() == null) {
 //			System.out.println("no elements left");
 //		} else {
 //			System.out.println("elements left");
 //		}
 		
 	}
 
 }
