 /**
  * Copyright (c) 2008 Andrew Rapp. All rights reserved.
  *  
  * This file is part of XBee-API.
  *  
  * XBee-API is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *  
  * XBee-API is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *  
  * You should have received a copy of the GNU General Public License
  * along with XBee-API.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.rapplogic.xbee.examples;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 
 import com.nfc.ryanjfahsel.DBConnection;
 import com.rapplogic.xbee.api.RemoteAtRequest;
 import com.rapplogic.xbee.api.RemoteAtResponse;
 import com.rapplogic.xbee.api.XBee;
 import com.rapplogic.xbee.api.XBeeAddress64;
 import com.rapplogic.xbee.api.XBeeException;
 import com.rapplogic.xbee.api.XBeeTimeoutException;
 
 //This code is being modified by Ryan Fahsel for use in a Georgia Tech Computer Science Class.
 /** 
  * This example uses Remote AT to turn on/off I/O pins.  
  * This example is more interesting if you connect a LED to pin 20 on your end device.  
  * Remember to use a resistor to limit the current flow.  I used a 215 Ohm resistor.
  * <p/>
  * Note: if your coordinator is powered on and receiving I/O samples, make sure you power off/on to drain 
  * the traffic before running this example.
  * 
  * @author andrew
  *
  */
 public class RemoteAtExample {
 
 	private final static Logger log = Logger.getLogger(RemoteAtExample.class);
 	
 	private RemoteAtExample(int mode) throws XBeeException, InterruptedException {
 		
 		XBee xbee = new XBee();
 		
 		try {
 			// Initialize xbee
 			String SerialPortID="/dev/ttyAMA0";
 			System.setProperty("gnu.io.rxtx.SerialPorts", SerialPortID);
 			xbee.open(SerialPortID, 9600);
 			
 			// Broadcast address. Send to all on same PAN
 			XBeeAddress64 addr64 = new XBeeAddress64(0, 0x00, 0x00, 0, 0x00, 0x00, 0xFF, 0xFF);
 			
 			// turn on end device (pin 20) D0 (Digital output high = 5) 
 			RemoteAtRequest request = new RemoteAtRequest(addr64, "D1", new int[] {mode});
 			
 			RemoteAtResponse response = (RemoteAtResponse) xbee.sendSynchronous(request, 10000);
 			
 			if (response.isOk()) {
 				log.info("successfully turned on pin 20 (D0)");	
 			} else {
 				throw new RuntimeException("failed to turn on pin 20.  status is " + response.getStatus());
 			}
 	
 			System.exit(0);
 			
 			// wait a bit
 			Thread.sleep(5000);
 //			
 //			// now turn off end device D0
 			request.setValue(new int[] {4});
 			
 			response = (RemoteAtResponse) xbee.sendSynchronous(request, 10000);
 			
 			if (response.isOk()) {
 				log.info("successfully turned off pin 20 (D0)");	
 			} else {
 				throw new RuntimeException("failed to turn off pin 20.  status is " + response.getStatus());
 			}
 			
 		} catch (XBeeTimeoutException e) {
 			log.error("request timed out. make sure you remote XBee is configured and powered on");
 		} catch (Exception e) {
 			log.error("unexpected error", e);
 		} finally {
 			xbee.close();
 		}
 	}
 	
 	public static void main(String[] args) throws XBeeException, InterruptedException {
 		PropertyConfigurator.configure("log4j.properties");
 		DBConnection conn=new DBConnection("admin","admin");
 		int result;
 		int prevResult;
 		result=Integer.parseInt(conn.Connect());
 		prevResult=Integer.parseInt(conn.Connect());
 		new RemoteAtExample(result);
 		while(true)	{
 			result=Integer.parseInt(conn.Connect()); 
 			if(result==prevResult)	{
 				//Do nothing
 			}
 			else	{
				new RemoteAtExample(result+4);
 				prevResult=result;
 			}
 			result=Integer.parseInt(conn.Connect());
 		}
 	}
 }
