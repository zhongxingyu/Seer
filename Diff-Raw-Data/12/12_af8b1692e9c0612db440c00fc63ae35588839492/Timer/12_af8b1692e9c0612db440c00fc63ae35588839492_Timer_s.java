 package com.github.rocketsurgery;
 
 public abstract class Timer {
 
 	private static long time;
 	
 	public static void decrease(long delta) {
 		time = (time > 0) ? time - delta : 0;
 	}
 	
 	public static void increase(long delta) {
 		time += delta;
 	}
 	
 	public static boolean isDone() {
 		return time <= 0;
 	}
 	
 	public static long getTime() {
 		return time;
 	}
 	
 	public static void set(long t) {
		
 	}
 }
