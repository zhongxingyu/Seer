 package com.buglabs.bug.jni.common;
 
 import java.io.IOException;
 
 /**
  * Utility methods for CharDevice objects
  * 
  * @author Angel Roman - angel@buglabs.net
  */
 public class CharDeviceUtils {
 	
 	public static void openDeviceWithRetry(CharDevice d, String devnode, int attempts) throws Exception {
 		int attempt_number = 0;
 		int retval = 0;
 		
		while(retval <= 0 && attempt_number < 2) {
 			attempt_number++;
			retval = d.open(devnode, FCNTL_H.O_RDWR);
 			if(retval < 0) {
 				String errormsg = "Unable to open " + devnode + "retrying";
 				Thread.sleep(2000);
 			}
 		}
 		
 		if(retval < 0) {
 			throw new IOException("Unable to open device: " + devnode);
 		}
 	}
 }
