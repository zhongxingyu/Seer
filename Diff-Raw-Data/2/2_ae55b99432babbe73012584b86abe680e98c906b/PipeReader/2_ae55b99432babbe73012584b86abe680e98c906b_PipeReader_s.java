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
 package com.buglabs.bug.bmi;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import org.osgi.service.log.LogService;
 
 import com.buglabs.bug.bmi.pub.BMIMessage;
 import com.buglabs.bug.bmi.pub.Manager;
 import com.buglabs.bug.module.pub.BMIModuleProperties;
 
 /**
  * This class listens to a pipe for events from Hotplug.  These events are passed to the BMIManager.
  * @author kgilmer
  *
  */
 public class PipeReader extends Thread {
 	private static final String POISON_PILL = "exit\n";
 
 	private volatile FileInputStream ifs = null;
 
 	private final String pipeFilename;
 
 	private final Manager manager;
 
 	private final LogService logService;
 
 	public PipeReader(String pipeFilename, Manager manager, LogService logService) {
 		this.pipeFilename = pipeFilename;
 		this.manager = manager;
 		this.logService = logService;
 	}
 
 	public void cancel() {
 
 		try {
 			FileOutputStream fos = new FileOutputStream(pipeFilename);
 			fos.write(POISON_PILL.getBytes());
 			fos.close();
 		} catch (IOException e) {
 		}
 
 	}
 
 	public void run() {
 		while(!Thread.currentThread().isInterrupted()) {
 			try {
 
 				ifs = new FileInputStream(pipeFilename);
 
 				int c = 0;
 
 				StringBuffer sb = new StringBuffer();
 
 				while((c = ifs.read()) != -1) {
 					sb.append((char) c);
 					if(c == '\n' || c == '\r') {
 						if(sb.toString().equals(POISON_PILL)) {
 							return;
 						}
 						if (logService != null) {
 							logService.log(LogService.LOG_DEBUG, "Received message from event pipe: " + sb.toString());
 						}
 						BMIMessage m = new BMIMessage(sb.toString());
 						if (m.parse()) {
 							if (m.getEvent() == BMIMessage.EVENT_INSERT) {
 								m.setBMIModuleProperties(BMIModuleProperties.createFromSYSDirectory(sysDirectory(m.getSlot())));
 							}
 							manager.processMessage(m);
 						} else {
 							logService.log(LogService.LOG_ERROR, "Unable to parse message from event pipe: " + sb.toString());
 						}
 						
 						break;
 					}
 				}
 			} catch (FileNotFoundException e) {
 				logService.log(LogService.LOG_ERROR, e.getMessage());
 			} catch (IOException e) {
 				logService.log(LogService.LOG_ERROR, e.getMessage());
 			} finally {
 				try {
 					if (ifs != null) {
 						ifs.close();
 					}
 				} catch (IOException e) {
 				}
 			}
 		}
 	}
 
 	/**
 	 * Return a file represented the root directory of a BMI module given it's slot #.
 	 * @param slot
 	 * @return
 	 */
 	private File sysDirectory(int slot) {
		
		/sys/class/bmi/bmi-1/bmi-dev-1/
 		return new File("/sys/class/bmi/bmi-"+slot+"/bmi-dev-"+slot);
 	}
 }
