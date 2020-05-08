 /*******************************************************************************
  * Copyright (c) 2010 protos software gmbh (http://www.protos.de).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *******************************************************************************/
 
 package org.eclipse.etrice.runtime.java.modelbase;
 
 import org.eclipse.etrice.runtime.java.messaging.Address;
 import org.eclipse.etrice.runtime.java.messaging.IRTObject;
 import org.eclipse.etrice.runtime.java.messaging.Message;
 import org.eclipse.etrice.runtime.java.messaging.MessageService;
 import org.eclipse.etrice.runtime.java.messaging.RTServices;
 import org.eclipse.etrice.runtime.java.modelbase.ActorClassBase;
 import org.eclipse.etrice.runtime.java.modelbase.InterfaceItemBase;
 
 import junit.framework.TestCase;
 
 
 
 public class ActorClassBaseTest extends TestCase {
 
 	
 	
 	public class MockRTObject implements IRTObject {
 
 		@Override
 		public String getInstancePath() {
 			return "TOP_Path";
 		}
 
 		@Override
 		public String getInstancePathName() {
 			return "TOP_PathName";
 		}
 		
 	}
 	
 	public class MockActor extends ActorClassBase {
 		public MockActor(IRTObject parent, String name, Address system_port_addr,Address peer_system_port_addr) {
 			super(parent, name, system_port_addr, peer_system_port_addr);
 		}
 		public void receive(Message msg) {
 			
 		}
 		public Address getAddress() {
 			return null;
 		}
 		public void receiveEvent(InterfaceItemBase ifitem, int evt, Object data) {
 		}
 		
 		@Override
 		public void init() {}
 		@Override
 		public void start() {}
 		@Override
 		public void stop() {}
 		@Override
 		public void destroy() {}
 		@Override
 		public void executeInitTransition() {
 		}
 	}
 
 	public void testActorClassBase() {
 		MockRTObject topRTObject = new MockRTObject();
 		RTServices.getInstance().getMsgSvcCtrl().addMsgSvc(
				new MessageService(topRTObject, new Address(0, 0, 0),"MessageService_Main", Thread.NORM_PRIORITY));
 
 		Address peer_system_port_addr = new Address(0, 0, 0);
 		Address system_port_addr = new Address(0, 0, 1);
 
 		// PathNames
 		ActorClassBase actor = new MockActor(topRTObject, "MockActor1", system_port_addr, peer_system_port_addr);
 		assertEquals("TOP_Path/MockActor1", actor.getInstancePath());
 		assertEquals("TOP_PathName_MockActor1", actor.getInstancePathName());
 		
 		// ClassName
 		actor.setClassName("MockActor");
 		assertEquals("MockActor", actor.getClassName());
 
 		actor.setClassName("MockActor");
 		assertEquals("ActorClass(className=MockActor, instancePath=TOP_Path/MockActor1)", actor.toString());
 		
 	}
 
 
 }
