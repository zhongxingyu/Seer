 package com.cg.trashman;
 
 public class CountDownTimer {
	private float endTime = 0f;
 
 	public CountDownTimer() {
 	}
 
 	public void start() {
		endTime = System.currentTimeMillis() + 6000f;
 	}
 
	public int getTime() {
		return (int) (endTime - System.currentTimeMillis());
 	}
 }
