 /**
  * Copyright (c) 2009, Coral Reef Project
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  *  * Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation
  *    and/or other materials provided with the distribution.
  *  * Neither the name of the Coral Reef Project nor the names of its
  *    contributors may be used to endorse or promote products derived from this
  *    software without specific prior written permission.
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
  */
 package pl.graniec.coralreef.network.stream;
 
 
 import static org.junit.Assert.*;
 
 import java.io.NotSerializableException;
 
 import org.jmock.Mockery;
 import org.jmock.integration.junit4.JUnit4Mockery;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import pl.graniec.coralreef.network.PacketListener;
 import pl.graniec.coralreef.network.client.Client;
 import pl.graniec.coralreef.network.exceptions.NetworkException;
 import pl.graniec.coralreef.network.server.ConnectionListener;
 import pl.graniec.coralreef.network.server.RemoteClient;
 import pl.graniec.coralreef.network.server.Server;
 import pl.graniec.coralreef.network.stream.client.StreamClient;
 import pl.graniec.coralreef.network.stream.server.StreamServer;
 
 /**
  * @author Piotr Korzuszek <piotr.korzuszek@gmail.com>
  *
  */
 public class NetworkStreamModuleTest {
 
 	final Mockery context = new JUnit4Mockery();
 	
 	Server server;
 	Client client;
 	
 	RemoteClient remoteClient;
 	
 	boolean dataSendingDone = false;
 	
 	@Before
 	public void setUp() throws Exception {
 		server = new StreamServer();
 		client = new StreamClient();
 		
 		server.open(0);
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		if (client.isConnected()) {
 			client.disconnect();
 		}
 		
 		if (server.isOpen()) {
 			server.close();
 		}
 	}
 	
 	@Test
 	public void testDataSending() throws InterruptedException, NetworkException, NotSerializableException {
 		ConnectionListener serverConnectionListener = new ConnectionListener() {
 
 			public void clientConnected(RemoteClient client) {
 				remoteClient = client;
 			}
 
 			public void clientDisconnected(RemoteClient client,
 					int reason, String reasonString) {
 			}
 			
 		};
 		
 		server.addConnectionListener(serverConnectionListener);
 		
 		client.connect("localhost", server.getPort());
 		
 		Thread.sleep(10);
 		
 		assertNotNull(remoteClient);
 		
 		PacketListener remoteClientPacketListener = new PacketListener() {
 
 			public void packetReceived(Object data) {
 				assertEquals("from-client", data);
 				try {
 					remoteClient.send("from-server");
 				} catch (NotSerializableException e) {
 					fail(e.getMessage());
 				} catch (NetworkException e) {
 					fail(e.getMessage());
 				}
 			}
 			
 		};
 		
 		remoteClient.addPacketListener(remoteClientPacketListener);
 		
 		PacketListener clientPacketListener = new PacketListener() {
 
 			public void packetReceived(Object data) {
 				assertEquals("from-server", data);
 				dataSendingDone = true;
 			}
 			
 		};
 		
 		client.addPacketListener(clientPacketListener);
 		
 		client.send("from-client");
 		
 		Thread.sleep(10);
 		
 		assertTrue(dataSendingDone);
 	}
 
 }
