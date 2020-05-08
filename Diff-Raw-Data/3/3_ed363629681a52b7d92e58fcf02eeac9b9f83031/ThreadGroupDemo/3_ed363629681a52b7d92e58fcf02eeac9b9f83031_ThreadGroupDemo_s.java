 package com.MyTestProjects.MultyThreadDemo;
 
 public class ThreadGroupDemo {
 
 	public static void main(String[] args) {
 		Thread ob1, ob2, ob3, ob4;
 		ThreadGroup groupA = new ThreadGroup("Group A");
 		ThreadGroup groupB = new ThreadGroup("Group B");
 
 		ob1 = new NewThread("First", groupA);
 		ob2 = new NewThread("Second", groupA);
 		ob3 = new NewThread("Third", groupB);
 		ob4 = new NewThread("Fourd", groupB);
 
 		Thread[] thr = new Thread[groupA.activeCount()];
 		groupA.enumerate(thr);
 
 		System.out.println("Now we suspend the group A work!");
 
 		for (int i = 0; i < thr.length; i++) {
 			((NewThread) thr[i]).mySuspend();
 		}
 
 		try {
 			Thread.sleep(4000);
 
 			System.out.println("Now we resume the group A work!");
 
 			for (int i = 0; i < thr.length; i++) {
 				((NewThread) thr[i]).myResume();
 			}
 
 			System.out.println("Wait for threads finish");
 
 			ob1.join();
 			ob2.join();
 			ob3.join();
 			ob4.join();
 
 			System.out.println("The main thread was finished!");
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
