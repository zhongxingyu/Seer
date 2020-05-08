 /*******************************************************************************
  * Copyright (c) 2010 protos software gmbh (http://www.protos.de).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *******************************************************************************/
 
 package org.eclipse.etrice.runtime.java.modelbase;
 
 import java.util.concurrent.Semaphore;
 
 import org.eclipse.etrice.runtime.java.debugging.DebuggingService;
 import org.eclipse.etrice.runtime.java.messaging.IRTObject;
 import org.eclipse.etrice.runtime.java.messaging.MessageService;
 import org.eclipse.etrice.runtime.java.messaging.RTObject;
 import org.eclipse.etrice.runtime.java.messaging.RTServices;
 import org.eclipse.etrice.runtime.java.messaging.RTSystemServicesProtocol.RTSystemServicesProtocolConjPortRepl;
 
 /**
  * The base class for all SubSystems.
  * It and its derived classes take care of the instantiation, binding (connection) and complete lifecycle of all Actor Classes of a SubSystem
  * 
  * @author Henrik Rentz-Reichert
  *
  */
 public abstract class SubSystemClassBase extends RTObject implements IEventReceiver{
 
 	//--------------------- ports
 	protected RTSystemServicesProtocolConjPortRepl RTSystemPort = null;
 	//--------------------- interface item IDs
 	protected static final int IFITEM_RTSystemPort = 0;
 	protected ActorClassBase[] instances = null;
 	private Semaphore testSem=null;
 	private int testErrorCode;
 	
 	public SubSystemClassBase(IRTObject parent, String name) {
 		super(parent, name);
 
 		DebuggingService.getInstance().getAsyncLogger()
 				.setMSC(name + "_Async", "");
 		DebuggingService.getInstance().getAsyncLogger().open();
 		DebuggingService.getInstance().getSyncLogger()
 				.setMSC(name + "_Sync", "");
 		DebuggingService.getInstance().getSyncLogger().open();
 		
 		RTServices.getInstance().setSubSystem(this);
 		
 	}
 
 	public void init() {
 
 		System.out.println("*** MainComponent "+getInstancePath()+"::init ***");
 
 		// MessageService
 		instantiateMessageServices();
 		
 		// this is the place to connect the message services if necessary
 		// normaly the ports will address the correct target message service directly
 		// it is just for test purposes 
 		// RTServices.getInstance().getMsgSvcCtrl().connectAll();
 		
 		instantiateActors();
 
 		// initialize all actor instances
 		if (instances!=null)
 			for (int i = 0; i < instances.length; i++) {
 				instances[i].init();
 			}
 	}
 
 	public abstract void instantiateMessageServices();
 	public abstract void instantiateActors();
 	
 	
 	public void start() {
 		// start all actors instances
 		RTSystemPort.executeInitialTransition();
 		
 		// start all message services
 		RTServices.getInstance().getMsgSvcCtrl().start();
 		
 	}
 	
 	public void stop() {
 		RTServices.getInstance().getMsgSvcCtrl().stop();
 
 		// stop all actor instances
 		if (instances!=null)
 			for (int i = 0; i < instances.length; i++) {
 				instances[i].stop();
 			}
 	}
 	
 	public void destroy() {
 		if (instances!=null)
 			for (int i = 0; i < instances.length; i++) {
 				instances[i].destroy();
 			}
 
 		DebuggingService.getInstance().getAsyncLogger().close();
 		DebuggingService.getInstance().getSyncLogger().close();
 
 		RTServices.getInstance().destroy();
 		System.out.println("*** MainComponent "+getInstancePath()+"::destroy ***");
 	}
 	
 	public MessageService getMsgService(int idx) {
 		return RTServices.getInstance().getMsgSvcCtrl().getMsgSvc(idx);
 	}
 	
 	public ActorClassBase getInstance(int i) {
 		if (instances==null || i<0 || i>= instances.length)
 			return null;
 		
 		return instances[i];
 	}
 	
 	public ActorClassBase getInstance(String path) {
 		if (instances!=null)
 			for (int i = 0; i < instances.length; i++) {
 				if (instances[i].getInstancePath().equals(path))
 					return instances[i];
 			}
 		
 		return null;
 	}
 	
 	// this is to run integration tests
 	public synchronized void setTestSemaphore(Semaphore sem){
 		testErrorCode = -1;
 		testSem=sem;
 	}
 	
 	public synchronized int getTestErrorCode(){
 		return testErrorCode;
 	}
 	
 	public synchronized void testFinished(int errorCode){
 		if (testSem != null) {
 			testErrorCode = errorCode;
 			testSem.release(1);
 			
 			// making this thread sleep seems to fix a problem that the tester thread doesn't
 			// acquire control before the test timeout is reached
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
 		}
 	}
 }
