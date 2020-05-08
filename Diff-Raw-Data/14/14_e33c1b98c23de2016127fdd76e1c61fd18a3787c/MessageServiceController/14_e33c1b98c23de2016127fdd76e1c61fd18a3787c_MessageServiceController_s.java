 /*******************************************************************************
  * Copyright (c) 2011 protos software gmbh (http://www.protos.de).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * CONTRIBUTORS:
  * 		Thomas Schuetz
  * 
  *******************************************************************************/
 
 package org.eclipse.etrice.runtime.java.messaging;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * The MessageServiceController controls lifecycle of and access to all MessageServices in one SubSystem
  * 
  * @author Thomas Schuetz
  *
  */
 
 public class MessageServiceController {
 
 	public MessageServiceController(/*IRTObject parent*/){
 		// TODOTS: Who is parent of MessageServices and Controller?
 		// this.parent = parent;
 		messageServiceList = new ArrayList<MessageService>();
 	}
 
 	public void addMsgSrv(MessageService msgSrv){
 		// TODOTS: Who is parent of MessageServices ?
 		assert(msgSrv.getAddress().threadID == messageServiceList.size());
 		messageServiceList.add(msgSrv);
 	}
 	
 	public MessageService getMessageService(int threadID){
 		assert(threadID < messageServiceList.size());
 		return messageServiceList.get(threadID);
 	}
 
 
 	
 	public void start() {
 		// start all message services
 		for (MessageService msgSrv : messageServiceList){
 			msgSrv.start();
 			// TODOTS: start in order of priorities
 		}
 		running = true;
 	}
 
 	public void stop() {
 		terminate();
 		waitTerminate();
 	}
 
 	private void terminate() {
 		if (!running){
 			return;
 		}
 		running = false;
 		
 		// terminate all message services
 		for (MessageService msgSrv : messageServiceList){
 			msgSrv.terminate();
 			// TODOTS: stop in order of priorities
 		}
 	}
 
 	/**
 	 * waitTerminate waits blocking for all MessageServices to terminate 
 	 */
	private void waitTerminate() {
 		for (MessageService msgSrv : messageServiceList){
 			try {
 				msgSrv.join();
 				} 
 			catch (InterruptedException e1) {
 			}
 		}
 	}
 	
 	private List<MessageService> messageServiceList = null;
 //	private IRTObject parent = null;
 	private boolean running = false;
 }
