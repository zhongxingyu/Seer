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
 import java.util.Map;
 
 /**
  * The MessageServiceController controls lifecycle of and access to all MessageServices in one SubSystem
  * 
  * @author Thomas Schuetz
  * @author Thomas Jung
  *
  */
 
 public class MessageServiceController {
 
 	public MessageServiceController(/*IRTObject parent*/){
 		// TODOTS: Who is parent of MessageServices and Controller?
 		// this.parent = parent;
 		messageServiceList = new ArrayList<MessageService>();
 	}
 
 	public void addMsgSvc(MessageService msgSvc){
 		// TODOTS: Who is parent of MessageServices ?
 		assert(msgSvc.getAddress().threadID == messageServiceList.size());
 		messageServiceList.add(msgSvc);
 	}
 	
 	public MessageService getMsgSvc(int threadID){
 		assert(threadID < messageServiceList.size());
 		return messageServiceList.get(threadID);
 	}
 	
 	//the connectAll method connects all messageServices 
 	//it is included for test purposes
 	//currently it is not called
 	public void connectAll(){
 		for (int i=0; i < messageServiceList.size(); i++){
 			MessageDispatcher dispatcher = getMsgSvc(i).getMessageDispatcher();
 			for (int j=0;j < messageServiceList.size();j++){
 				if(i!=j){
 				     dispatcher.addMessageReceiver(RTServices.getInstance().getMsgSvcCtrl().getMsgSvc(j));
 				}
 			}
 		}
 	}
 	
 	public void start() {
 		// start all message services
 		for (MessageService msgSvc : messageServiceList){
 			msgSvc.start();
 			// TODOTS: start in order of priorities
 		}
 		running = true;
 	}
 
 	public void stop() {
 		//dumpThreads("org.eclipse.etrice.runtime.java.messaging.MessageServiceController.stop()");
 		terminate();
 		waitTerminate();
 	}
 
 	/**
 	 * @param msg 
 	 * 
 	 */
	private void dumpThreads(String msg) {
 		System.out.println("<<< begin dump threads <<<");
 		System.out.println("=== "+msg);
 		Map<Thread, StackTraceElement[]> traces = Thread.getAllStackTraces();
 		for (Thread thread : traces.keySet()) {
 			System.out.println("thread "+thread.getName());
 			StackTraceElement[] elements = traces.get(thread);
 			int n = 2;
 			if (elements.length<n)
 				n = elements.length;
 			for (int i = 0; i < n; i++) {
 				System.out.println(" "+elements[i].toString());
 			}
 		}
 		System.out.println(">>> end dump threads >>>");
 	}
 
 	private void terminate() {
 		if (!running){
 			return;
 		}
 		running = false;
 		
 		// terminate all message services
 		for (MessageService msgSvc : messageServiceList){
 			msgSvc.terminate();
 			// TODOTS: stop in order of priorities
 		}
 	}
 
 	/**
 	 * waitTerminate waits blocking for all MessageServices to terminate 
 	 * ! not threadsafe !
 	 */
 	public void waitTerminate() {
 		for (MessageService msgSvc : messageServiceList){
 			try {
 				msgSvc.join();
 				} 
 			catch (InterruptedException e1) {
 			}
 		}
 	}
 	
 	private List<MessageService> messageServiceList = null;
 //	private IRTObject parent = null;
 	private boolean running = false;
 }
