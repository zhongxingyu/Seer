 package org.jclarity.training;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.*;
 
 public class SaferCountersTest {
 
 	private final int THREADWAIT = 10;
 	
 	public class CounterTesterThread extends Thread {
 		SaferCounters _sc = null;		
 		int[] _counters;
 		
 		public CounterTesterThread(SaferCounters sc_, int[] counters_) {
 			_counters = counters_;
 			_sc = sc_;
 		}
 		
		@Override
 		public void run() {
 			List<SaferCounters.SaferCountListener> sclist = new ArrayList<SaferCounters.SaferCountListener>();
 			for (int i : _counters) {
 				sclist.add(_sc.new SaferCountListener("key"+ i));
 			}
 			
 			while (true) {
 				try {
 					for (int i=0; i < _counters.length; ++i) {						
 						long curr = sclist.get(i).waitForIncrement();
 						System.out.println("Thread "+ Thread.currentThread().getId() +", key "+ _counters[i] +" is: "+ curr);
 					}
 					Thread.sleep(THREADWAIT);
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		} 
 
 	}
 
 	@Test 
 	public void testArr() {
 		try {
 			final SaferCounters sc = new SaferCounters();
 			
 			int[] all = {1, 2, 3, 4, 5};
 			for (int i : all) {					
 				sc.increment("key" + i);
 			}
 			
 			int[] a1 = {1};
 			Thread t1 = new CounterTesterThread(sc, a1);
 
 			int[] a2 = {2, 3};
 			Thread t2 = new CounterTesterThread(sc, a2);
 
 			int[] a3 = {3};
 			Thread t3 = new CounterTesterThread(sc, a3);
 
 			int[] a4 = {4};
 			Thread t4 = new CounterTesterThread(sc, a4);
 
 			int[] a5 = {1, 2, 3, 4, 5};
 			Thread t5 = new CounterTesterThread(sc, a5);
 
 			t1.start();
 			t2.start();
 			t3.start();
 			t4.start();
 			t5.start();
 			
 			while (true) {
 				Thread.sleep(1000);
 				for (int i : all) {					
 					sc.increment("key" + i);
 				}
 			}
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 
 		}
 		
 			
 	}
 	
 
 	
 	@Test
 	public void testN() {
 		try {
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 	}
 
 	
 }
