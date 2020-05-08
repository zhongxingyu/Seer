 package com.cg.trashman;
 
 public class CountDownTimer {
	private long endTime;
 
 	public CountDownTimer() {
 	}
 
 	public void start() {
		endTime = System.currentTimeMillis() + 62000;
 	}
 
	public long getTime() {
		return (endTime - System.currentTimeMillis())/1000;
 	}
 }
