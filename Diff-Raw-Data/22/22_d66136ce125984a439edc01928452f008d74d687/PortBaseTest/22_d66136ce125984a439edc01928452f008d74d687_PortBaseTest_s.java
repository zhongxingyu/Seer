 /*******************************************************************************
  * Copyright (c) 2010 protos software gmbh (http://www.protos.de).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *******************************************************************************/
 
 package org.eclipse.etrice.runtime.java.modelbase;
 
 import org.eclipse.etrice.runtime.java.messaging.Address;
 import org.eclipse.etrice.runtime.java.messaging.IMessageReceiver;
 import org.eclipse.etrice.runtime.java.messaging.IRTObject;
 import org.eclipse.etrice.runtime.java.messaging.Message;
 import org.eclipse.etrice.runtime.java.messaging.MessageService;
 import org.eclipse.etrice.runtime.java.modelbase.ActorClassBase;
 import org.eclipse.etrice.runtime.java.modelbase.IEventReceiver;
 import org.eclipse.etrice.runtime.java.modelbase.InterfaceItemBase;
 import org.eclipse.etrice.runtime.java.modelbase.PortBase;
 
 import junit.framework.TestCase;
 
 public class PortBaseTest extends TestCase {
 
 	public class TestActor extends ActorClassBase {
 		public TestActor(IRTObject parent, String name) {
 			super(parent, name, null);
 		}
 		public void receive(Message msg) {
 			
 		}
 		public Address getAddress() {
 			return null;
 		}
 		public void receiveEvent(InterfaceItemBase ifitem, int evt, Object... data) {
 		}
 		@Override
 		public void init() {}
 		@Override
 		public void start() {}
 		@Override
 		public void stop() {}
 		@Override
 		public void destroy() {}
 	}
 	
 	class MyPortBase extends PortBase {
 		public MyPortBase(IEventReceiver parent, String name, int localId, int idx, Address address,
 				Address peerAddress, IMessageReceiver msgReceiver) {
			super(parent, name, localId, idx, address, peerAddress/*, msgReceiver*/);
 		}
 
 		public void receive(Message msg) {
 		}
 	}
 
 	protected void setUp() throws Exception {
 		super.setUp();
 	}
 
 	protected void tearDown() throws Exception {
 		super.tearDown();
 	}
 
 	public void testPortBase() {
 		MessageService msgService = new MessageService(null, new Address(4, 5, 6), "MessageService1");
 		Address portAddress = new Address(1, 2, 3);
 		Address peerAddress = new Address(3, 2, 1);
 		PortBase port = new MyPortBase(null, "Port1", 0, 0, portAddress, peerAddress, msgService);
 		assertEquals(msgService, port.getMsgReceiver());
 		assertEquals(portAddress, port.getAddress());
 		assertEquals(peerAddress, port.getPeerAddress());
 	}
 
 	public void testSetGetPeerAddress() {
 		Address peerAddress = new Address(3, 2, 1);
 		PortBase port = new MyPortBase(null, "", 0, 0, null, peerAddress, null);
 		assertEquals(peerAddress, port.getPeerAddress());
 	}
 
 	public void testSetGetMsgService() {
 		MessageService msgService = new MessageService(null, new Address(4, 5, 6), "MessageService1");
 		Address portAddress = new Address(1, 2, 3);
 		PortBase port = new MyPortBase(null, "", 0, 0, portAddress, null, msgService);
 		assertEquals(msgService, port.getMsgReceiver());
 	}
 	
 	public void testSetInstanceId() {
 		PortBase port = new MyPortBase(null, "Port1", 0, 0, null, null, null);
 		assertEquals("/Port1", port.getInstancePath());
 	}
 	
 	public void testGetLocalId() {
 		PortBase port = new MyPortBase(null, "Port1", 123, 0, null, null, null);
 		assertEquals(123, port.getLocalId());
 	}
 	
 	public void testGetIdx() {
 		PortBase port = new MyPortBase(null, "Port1", 0, 456, null, null, null);
 		assertEquals(456, port.getIdx());
 	}
 
 	public void testSetGetParentObject(){
 		ActorClassBase actor = new TestActor(null, "Actor1");
 		PortBase port = new MyPortBase(actor, "Port1", 0, 0, null, null, null);
 		
 		assertEquals("/Actor1/Port1", port.getInstancePath());
 		assertEquals(actor, port.getActor());
 	}
 }
