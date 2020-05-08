 /**
  * SocketClientTest.java
  *
  * Copyright 2012 Niolex, Inc.
  *
  * Niolex licenses this file to you under the Apache License, version 2.0
  * (the "License"); you may not use this file except in compliance with the
  * License.  You may obtain a copy of the License at:
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
  * License for the specific language governing permissions and limitations
  * under the License.
  */
 package org.apache.niolex.network.client;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.RejectedExecutionException;
 
 import org.apache.niolex.network.CoreRunner;
 import org.apache.niolex.network.PacketData;
 import org.apache.niolex.network.example.SavePacketHandler;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 /**
  * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
  * @version 1.0.0
  * @Date: 2012-6-13
  */
 
 public class SocketClientTest {
 
 	@BeforeClass
 	public static void run() throws Exception {
 		CoreRunner.createServer();
 	}
 
 	@AfterClass
 	public static void down() throws Exception {
 		CoreRunner.shutdown();
 	}
 
 	/**
 	 * Test method for {@link org.apache.niolex.network.client.SocketClient#SocketClient()}.
 	 */
 	@Test
 	public void testSocketClient() {
 		SocketClient sc = new SocketClient();
 		InetSocketAddress inn = new InetSocketAddress("localhost", CoreRunner.PORT);
 		sc.setServerAddress(inn);
 		assertFalse(sc.isWorking());
 		sc.setConnectTimeout(90821);
 		sc.setPacketHandler(null);
 		assertEquals(inn, sc.getServerAddress());
 	}
 
 	/**
 	 * Test method for {@link org.apache.niolex.network.client.SocketClient#getRemoteName()}.
 	 */
 	@Test
 	public void testGetRemoteName() {
 		SocketClient packetClient = new SocketClient(new InetSocketAddress("localhost", 8808));
 		assertEquals("localhost/127.0.0.1:8808-0000", packetClient.getRemoteName());
 		try {
 			packetClient.attachData("adsfasdf", "adsfasdf");
 			assertTrue("Should not attache.", false);
 		} catch (Exception e) {
 			;
 		}
 		try {
 			packetClient.getAttached("adsfasdf");
 			assertTrue("Should not attache.", false);
 		} catch (Exception e) {
 			;
 		}
 		try {
 			packetClient.addEventListener(null);
 			assertTrue("Should not listen.", false);
 		} catch (Exception e) {
 			;
 		}
 	}
 
 	@Test
 	public void testConn() throws IOException {
 		SocketClient sc = new SocketClient();
 		InetSocketAddress inn = new InetSocketAddress("localhost", CoreRunner.PORT);
 		sc.setServerAddress(inn);
 		List<PacketData> list = new ArrayList<PacketData>();
 		sc.setPacketHandler(new SavePacketHandler(list));
 		sc.connect();
 		sc.handleWrite(new PacketData(6, "Good."));
 		assertEquals(list.size(), 1);
 		sc.stop();
 		sc.stop();
 		boolean flag = false;
 		try {
 			sc.handleWrite(new PacketData(6, "Morning."));
 		} catch (RejectedExecutionException e) {
 			flag = true;
 		}
 		assertTrue(flag);
 	}
 
 }
