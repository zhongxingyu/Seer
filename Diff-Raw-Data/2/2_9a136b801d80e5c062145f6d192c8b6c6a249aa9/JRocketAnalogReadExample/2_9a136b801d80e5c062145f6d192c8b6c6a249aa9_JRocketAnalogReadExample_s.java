 /* 
  * This file is part of the JRocket Library project
  *
  * Copyright (C) 2012 Stefan Wendler <sw@kaltpost.de>
  *
  * The JRocket Library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * JRocket Library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with the JRocket firmware; if not, write to the Free
  * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
  * 02111-1307 USA.  
  */
 
 package rocketuc.jrocket.examples;
 
 import rocketuc.jrocket.JRocket;
 import rocketuc.jrocket.JRocketException;
 import rocketuc.jrocket.JRocketSerial;
 
 /**
  * This example shows: 
  *
  * how to connect to the MCU through serial line, 
  * configure a pin as analog input, and perform a 
  * single read on that pin. 
  */
 public class JRocketAnalogReadExample {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		
 		try {						 
 			// connect through serial line to MCU
			JRocket jr = new JRocketSerial("/dev/ttyACM0");
 			
 			// configure pin 1.5 for analog read
  			System.out.print("Set P1.5 to ANALOG: ");
 			jr.pinMode(JRocket.PIN_1_5, JRocket.ANALOG);
 			System.out.println("OK");
 
  			System.out.print("Read P1.5 ANALOG: ");
  			
 			// perform analog read on pin 1.5
 			short a = jr.analogRead(JRocket.PIN_1_5);
 
 			// convert value from analog read to volts: 
 			// - assuming Vmax is 3.3V
 			// - assuming max value from analog read is 1024
 			float v = (float) ((3.3 / 1024.0) * (float)a);
 			
 			System.out.println("OK");
 			System.out.println(" -  value : " + a + " (" + Integer.toHexString(a) + ")");
 			System.out.println(" - ~volts : " + v);
 			
 			// reset MCU
 			System.out.print("RESET: ");
 			jr.reset();
 			System.out.println("OK");
 
 			// call destructor to terminate physical connection correctely
 			jr.finalize();
 			
 			System.out.println("DONE");
 		} catch (JRocketException e) {
 			// communication on physical or protocol layer failed
 			e.printStackTrace();
 		} catch (Throwable e) {
 			// destructor (finalize) failed 
 			e.printStackTrace();
 		}			
 	}
 }
