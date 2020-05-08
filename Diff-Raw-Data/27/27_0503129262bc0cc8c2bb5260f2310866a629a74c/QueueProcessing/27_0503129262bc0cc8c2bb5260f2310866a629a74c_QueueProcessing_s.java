 package com.test9.irc.engine;
 
public class QueueProcessing extends Thread{
 	
 	private IRCConnection owner;
 	
 	QueueProcessing(IRCConnection owner) {
 		this.owner = owner;
		start();
 	}
 	
 	public void run() {
		while(!isInterrupted()) {
 			try {
				owner.get(owner.getQueue().take());
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 }
