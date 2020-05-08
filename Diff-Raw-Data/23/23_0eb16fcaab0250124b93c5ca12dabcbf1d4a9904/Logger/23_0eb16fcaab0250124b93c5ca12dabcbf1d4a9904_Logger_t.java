 package com.marakana.concurrency;
 
 import java.util.LinkedList;
 import java.util.Queue;
 
 public class Logger implements Runnable {
 
 	Queue<String> queue = new LinkedList<String>();
 
 	public void log(String message) {
		synchronized (queue) {
			queue.add(message);
		}
 	}
 
 	@Override
 	public void run() {
 		while (true) {
			if (queue.isEmpty()) {
				Thread.yield();
			} else {
				synchronized (queue) {
					System.out.println(queue.remove());
				}
 			}
 		}
 	}
 
 }
