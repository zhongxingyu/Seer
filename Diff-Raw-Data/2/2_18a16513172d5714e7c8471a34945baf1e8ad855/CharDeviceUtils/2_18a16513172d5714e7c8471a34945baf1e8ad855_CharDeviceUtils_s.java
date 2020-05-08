 /*******************************************************************************
  * Copyright (c) 2008, 2009 Bug Labs, Inc.
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *    - Redistributions of source code must retain the above copyright notice,
  *      this list of conditions and the following disclaimer.
  *    - Redistributions in binary form must reproduce the above copyright
  *      notice, this list of conditions and the following disclaimer in the
  *      documentation and/or other materials provided with the distribution.
  *    - Neither the name of Bug Labs, Inc. nor the names of its contributors may be
  *      used to endorse or promote products derived from this software without
  *      specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  *******************************************************************************/
 package com.buglabs.bug.jni.common;
 
 import java.io.IOException;
 
 /**
  * Utility methods for CharDevice objects
  * 
  * @author Angel Roman - angel@buglabs.net
  */
 public class CharDeviceUtils {
 
 	public static void openDeviceWithRetry(CharDevice d, String devnode, int attempts) throws Exception {
 		openDeviceWithRetry(d, devnode, FCNTL_H.O_RDWR, attempts);
 	}
 
 	public static void openDeviceWithRetry(CharDevice d, String devnode, int flags, int attempts) throws Exception {
 		int attempt_number = 0;
 		int retval = 0;
 
 		while (retval <= 0 && attempt_number < attempts) {
 			attempt_number++;
 			retval = d.open(devnode, flags);
 			if (retval < 0) {
 				//String errormsg = "Unable to open " + devnode + "retrying";
 				Thread.sleep(2000);
 			}
 		}
 
 		if (retval < 0) {
			throw new IOException("Unable to open device: " + devnode);
 		}
 	}
 }
