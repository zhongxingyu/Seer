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
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 
 import org.apache.commons.io.IOUtils;
 import org.osgi.service.log.LogService;
 
 /**
  * This class listens to a pipe for events from Hotplug. These events are passed
  * to the BMIManager.
  * 
  * @author kgilmer
  * 
  */
 public class PipeReader extends Thread {
 	/**
 	 * Special input message that will cause the reader Thread to shutdown.
 	 */
 	private static final String POISON_PILL = "exit\n";
 
 	private volatile FileInputStream stream = null;
 
 	private final String pipeFilename;
 
 	private final BMIModuleEventHandler eventHandler;
 
 	private final LogService logService;
 
 	/**
 	 * @param pipeFilename
 	 *            absolute path to pipe file
 	 * @param eventHandler
 	 *            reference to event handler
 	 * @param logService
 	 *            reference to log service
 	 */
 	public PipeReader(String pipeFilename, BMIModuleEventHandler eventHandler, LogService logService) {
 		this.pipeFilename = pipeFilename;
 		this.eventHandler = eventHandler;
 		this.logService = logService;
 	}
 
 	/**
 	 * Shutdown reader thread.
 	 */
 	public void shutdown() {
 
 		try {
 			FileOutputStream fos = new FileOutputStream(pipeFilename);
 			IOUtils.write(POISON_PILL, fos);
 			IOUtils.closeQuietly(fos);			
 		} catch (IOException e) {
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Thread#run()
 	 */
 	public void run() {
 		logService.log(LogService.LOG_INFO, "Listening to event pipe. " + pipeFilename);
 		while (!Thread.currentThread().isInterrupted()) {
 			try {
 
 				BufferedReader reader = new BufferedReader(new FileReader(pipeFilename));
 				String line = null;
 				
 				while ((line = reader.readLine()) != null) {
 					if (line.equals(POISON_PILL)) {
 						return;
 					}
 					
 					if (logService != null) {
 						logService.log(LogService.LOG_DEBUG
 								, "Received message from event pipe: " + line);
 					}
 					
 					try {
 						BMIModuleEvent m = new BMIModuleEvent(line);
 						eventHandler.handleEvent(m);
 					} catch (IOException e) {
 						logService.log(LogService.LOG_ERROR
 								, "Unable to parse message from event pipe: " + line);
 					}
 				}
 			} catch (FileNotFoundException e) {
 				logService.log(LogService.LOG_ERROR, e.getMessage());
 			} catch (IOException e) {
 				logService.log(LogService.LOG_ERROR, e.getMessage());
 			} finally {
 				IOUtils.closeQuietly(stream);				
 			}
 		}
 	}
 }
