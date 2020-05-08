 package org.saseros.cleanstorms.test;
 
 import lejos.nxt.*;
 
 public class Alarm {
 
	public static void createAlarm(String error) {
 		LCD.clear();
 		LCD.drawString(error, 0, 0);
 		playAlarm();
 	}
 	
 	public static void playAlarm() {
 		Sound.setVolume(Sound.VOL_MAX);
 		Sound.systemSound(true, 3);
 		Button.waitForAnyPress();
 	}
 }
