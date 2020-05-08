 package org.saseros.cleanstorms.test;
 
 import lejos.nxt.*;
 
 public class Alarm {
 
	public static void createAlarmSoft(String error) {
		LCD.clear();
		LCD.drawString(error, 0, 0);
		playBeep();
	}
	
	public static void createAlarmHard(String error) {
 		LCD.clear();
 		LCD.drawString(error, 0, 0);
 		playAlarm();
		
 	}
 	
 	public static void playAlarm() {
 		Sound.setVolume(Sound.VOL_MAX);
 		Sound.systemSound(true, 3);
 		Button.waitForAnyPress();
 	}
	
	public static void playBeep(){
		Sound.setVolume(Sound.VOL_MAX);
		Sound.beep();
	}
	
 }
