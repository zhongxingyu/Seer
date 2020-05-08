 package com.partyrock.tools.test;
 
 import com.partyrock.comm.serial.SerialCommunicator;
 import com.partyrock.tools.PartyToolkit;
 
 /**
  * This makes the laser go from 0,0 to 180,180 repeatedly
  * @author Matthew
  */
 public class LaserSerialDemo {
 	public static void main(String... args) {
 		String port = "/dev/tty.usbmodemfa131";
 		SerialCommunicator comm = new SerialCommunicator(port);
 		comm.connect();
 		System.out.println("starting");
		int delay = 700;
 		PartyToolkit.delay(2000);
 		while (true) {
 			comm.sendMsg(".1A0.2A0");
 			PartyToolkit.delay(delay);
 			comm.sendMsg(".1S0.2S0");
 			PartyToolkit.delay(delay);
 		}
 	}
 }
