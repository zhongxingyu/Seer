 package com.atreceno.it.javanese.concurrency;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class SchedulerRunnable {
 
 	private static final int NUM_OF_THREADS = 3;
 	private static final int SLEEP_MILLIS = 1000;
 	private static List<Thread> threadList = new ArrayList<Thread>();
 	private static Object objectToSync = new Object();
 
 	public static void main(String[] args) {
 		System.out.println("Start");
 		SchedulerRunnable scheduler = new SchedulerRunnable();
 
 		for (int i = 0; i < NUM_OF_THREADS; i++) {
 			scheduler.createThread(i);
 		}
 
 		int i = 0;
 		while (true) {
 
 			System.out.println(Thread.currentThread().getName()
 					+ ":  \tNotifying thread " + i);
			Thread thread = threadList.get(i);
 			synchronized (objectToSync) {
 				objectToSync.notify();
 			}
 			try {
 				Thread.sleep(SLEEP_MILLIS);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			i = ++i % NUM_OF_THREADS;
 		}
 
 	}
 
 	public void createThread(final int value) {
 		Thread thread = new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 				while (true) {
 					synchronized (objectToSync) {
 						try {
 							objectToSync.wait();
 						} catch (InterruptedException e) {
 							e.printStackTrace();
 						}
 						System.out.println(Thread.currentThread().getName()
 								+ ":\t" + value + " ");
 					}
 				}
 			}
 		}, "tango" + value);
 		threadList.add(thread);
 		thread.start();
 	}
 
 }
