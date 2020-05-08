 package com.test9.irc.engine;
 
import java.util.concurrent.ArrayBlockingQueue;

public class QueueProcessing implements Runnable {
 	
 	private IRCConnection owner;
 	
 	QueueProcessing(IRCConnection owner) {
 		this.owner = owner;
 	}
 	
 	public void run() {
		ArrayBlockingQueue<String> queue = owner.getQueue();
		while(true) {
 			try {
				owner.get(queue.take());
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 }
