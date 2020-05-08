 /*******************************************************************************
  * Copyright (c) 2010 protos software gmbh (http://www.protos.de).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * CONTRIBUTORS:
  * 		Thomas Schuetz and Henrik Rentz-Reichert (initial contribution)
  * 
  *******************************************************************************/
 
 
 package org.eclipse.etrice.runtime.java.messaging;
 
 /**
  * The MessageService the backbone of the asynchroneous communication inside a SubSystem
  * It usually contains a thread a message queue and a dispatcher
  * 
  * @author Thomas Schuetz
  *
  */
 public class MessageService extends Thread implements IMessageReceiver,
 		IRTObject {
 
 	private IRTObject parent = null;
 	private String name = NO_NAME;
 	private boolean running = false;
 	
 	// TODO: add internal message queue for less locks (faster thread internal
 	// messaging)
 	private MessageSeQueue messageQueue = null;
 	private MessageDispatcher messageDispatcher = null;
 	private Address address = null;
 	private long lastMessageTimestamp;
 
 	public MessageService(IRTObject parent, Address addr, String name) {
 		this.parent = parent;
 		address = addr;
 		this.name = name;
 		messageDispatcher = new MessageDispatcher(this, new Address(addr.nodeID,addr.threadID, addr.objectID + 1), "Dispatcher");
 		messageQueue = new MessageSeQueue(this, "Queue");
 	}
 
 	@Override
 	public Address getAddress() {
 		return address;
 	}
 
 	public void run() {
 		running = true;
 		while (running) {
 			pollOneMessage();
 		}
 	}
 
 	@Override
 	public synchronized void receive(Message msg) {
 		if (msg!=null) {
 			messageQueue.push(msg);
 			notifyAll(); // wake up thread to compute message
 		}
 	}
 
 	private synchronized void pollOneMessage() {
 		Message msg = messageQueue.pop(); // get next Message from Queue
 		if (msg == null) {
 			// no message in queue -> wait till Thread is notified
 			try {
 				wait();
 			}
 			catch (InterruptedException e) {
 			}
 		}
 		else {
 			lastMessageTimestamp = System.currentTimeMillis();
 			messageDispatcher.receive(msg);
 		}
 
 	}
 
 	// protected methods for sole use by test cases
 	protected MessageSeQueue getMessageQueue() {
 		return messageQueue;
 	}
 
 	public synchronized MessageDispatcher getMessageDispatcher() {
 		return messageDispatcher;
 	}
 
 	protected synchronized long getLastMessageTimestamp() {
 		return lastMessageTimestamp;
 	}
 
 	@Override
 	public String getInstancePath() {
 		String path = PATH_DELIM + name;
 		
 		if (parent!=null)
 			path = parent.getInstancePath()+path;
 		
 		return path;
 	}
 
 	@Override
 	public String getInstancePathName() {
 		String path = PATHNAME_DELIM + name;
 		
 		if (parent!=null)
 			path = parent.getInstancePathName()+path;
 		
 		return path;
 	}
 	
 	public synchronized void terminate() {
 		if (running) {
 			running = false;
 			notifyAll();
 		}
 	}
 }
