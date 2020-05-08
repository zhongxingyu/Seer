 /**
  * 
  */
 package com.vzw.hackthon.scheduler;
 
 /**
  * @author hud
  *
  */
 public class Main {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		EventReminder er = new EventReminder();
 		MemberWatcher mw = new MemberWatcher();
 		
 		er.start();
 		mw.start();
 		
 		// will wait for a day or interrupt it
		er.await();
		mw.await();
 		
 
 	}
 
 }
