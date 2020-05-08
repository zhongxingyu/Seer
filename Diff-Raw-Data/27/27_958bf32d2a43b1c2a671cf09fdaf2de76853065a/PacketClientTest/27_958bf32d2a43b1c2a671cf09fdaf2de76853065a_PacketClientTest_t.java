 /**
  * PacketClientTest.java
  *
  * Copyright 2011 Niolex, Inc.
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
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.doAnswer;
 import static org.mockito.Mockito.spy;
 import static org.mockito.Mockito.verify;
 
 import java.net.InetSocketAddress;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import org.apache.niolex.network.CoreRunner;
 import org.apache.niolex.network.IPacketHandler;
 import org.apache.niolex.network.IPacketWriter;
 import org.apache.niolex.network.PacketData;
 import org.apache.niolex.network.client.PacketClient;
 import org.apache.niolex.network.example.EchoPacketHandler;
 import org.apache.niolex.network.server.NioServer;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Mock;
 import org.mockito.invocation.InvocationOnMock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.mockito.stubbing.Answer;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
  * @version 1.0.0
  * @Date: 2012-5-28
  */
 @RunWith(MockitoJUnitRunner.class)
 public class PacketClientTest {
 	private static final Logger LOG = LoggerFactory.getLogger(PacketClientTest.class);
 
 	@Mock
 	private IPacketHandler packetHandler;
 	private PacketClient packetClient;
 
 	private IPacketHandler serverHandler;
 	private NioServer nioServer;
 
 	private int port = CoreRunner.PORT;
 	private int received = 0;
 
 	@Before
 	public void createPacketClient() throws Exception {
 		packetClient = new PacketClient();
 		InetSocketAddress ina = new InetSocketAddress("localhost", port);
 		packetClient.setServerAddress(ina);
 		assertEquals(packetClient.getServerAddress(), ina);
 		packetClient.setPacketHandler(packetHandler);
 
 		nioServer = new NioServer();
 		serverHandler = spy(new EchoPacketHandler());
 		nioServer.setPacketHandler(serverHandler);
 		nioServer.setPort(port);
 		nioServer.start();
 	}
 
 	@After
 	public void stopNioServer() throws Exception {
 		nioServer.stop();
 	}
 
 	/**
 	 * Test method for
 	 * {@link org.apache.niolex.network.client.PacketClient#connect()}.
 	 */
 	@Test
 	public void testConnect() throws Exception {
 		assertEquals(false, packetClient.isWorking());
 		packetClient.setConnectTimeout(1234);
 		assertEquals(1234, packetClient.getConnectTimeout());
 		packetClient.connect();
 		PacketData sc = new PacketData();
 		sc.setCode((short) 4);
 		sc.setVersion((byte) 8);
 		sc.setLength(1024 * 1024 + 6);
 		sc.setData(new byte[1024 * 1024 + 6]);
 		sc.getData()[9] = (byte) 145;
 		sc.getData()[145] = (byte) 63;
 		packetClient.handleWrite(sc);
 		Thread.sleep(5 * CoreRunner.CO_SLEEP);
 		packetClient.stop();
 
 		ArgumentCaptor<PacketData> argument = ArgumentCaptor
 				.forClass(PacketData.class);
 		verify(packetHandler).handleRead(argument.capture(),
 				any(IPacketWriter.class));
 		assertEquals((short) 4, argument.getValue().getCode());
 		assertEquals((byte) 8, argument.getValue().getVersion());
 		assertEquals(1024 * 1024 + 6, argument.getValue().getLength());
 		assertEquals(1024 * 1024 + 6, argument.getValue().getData().length);
 		assertEquals((byte) 145, argument.getValue().getData()[9]);
 		assertEquals((byte) 63, argument.getValue().getData()[145]);
 	}
 
 	/**
 	 * Test method for
 	 * {@link org.apache.niolex.network.client.PacketClient#getRemoteName()}
 	 * .
 	 */
 	@Test
 	public void testGetRemoteName() throws Exception {
		assertEquals("localhost/127.0.0.1:8809-0000", packetClient.getRemoteName());
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
 		packetClient.connect();
		packetClient.handleWrite(PacketData.getHeartBeatPacket());
		Thread.sleep(CoreRunner.CO_SLEEP);
 		nioServer.stop();
		Thread.sleep(CoreRunner.CO_SLEEP);
 
 		verify(packetHandler).handleClose(packetClient);
 	}
 
 	private byte[] generateRandom(int len, Random r) {
 		byte[] ret = new byte[len];
 		r.nextBytes(ret);
 		return ret;
 	}
 
 	private void assertArrayEquals(byte[] a, byte[] b) {
 		if (a.length == b.length) {
 			for (int k = 0; k < a.length; k += a.length / 100 + 1) {
 				if (a[k] != b[k]) {
 					assertFalse("Index at " + k, true);
 					return;
 				}
 			}
 		} else {
 			assertFalse("Invalid length", true);
 		}
 	}
 
 	/**
 	 * Test method for
 	 * {@link org.apache.niolex.network.client.PacketClient#handleWrite(org.apache.niolex.network.PacketData)}
 	 * .
 	 */
 	@Test
	public void testHandleWriteLarge() throws Exception {
 		final PacketData sc0 = new PacketData();
 		final PacketData sc1 = new PacketData();
 		final PacketData sc2 = new PacketData();
 		final PacketData sc3 = new PacketData();
 		final PacketData sc4 = new PacketData();
 		final PacketData sc5 = new PacketData();
 
 		doAnswer(new Answer<String>() {
 			public String answer(InvocationOnMock invocation) {
 				Object[] args = invocation.getArguments();
 				PacketData sc = (PacketData) args[0];
 				switch (sc.getCode()) {
 				case 6:
 					assertArrayEquals(sc0.getData(), sc.getData());
 					break;
 				case 1:
 					assertArrayEquals(sc1.getData(), sc.getData());
 					break;
 				case 2:
 					assertArrayEquals(sc2.getData(), sc.getData());
 					break;
 				case 3:
 					assertArrayEquals(sc3.getData(), sc.getData());
 					break;
 				case 4:
 					assertArrayEquals(sc4.getData(), sc.getData());
 					break;
 				case 5:
 					assertArrayEquals(sc5.getData(), sc.getData());
 					break;
 				default:
 					System.out.println("Code: " + sc.getCode());
 					break;
 				}
 				IPacketWriter ip = (IPacketWriter)args[1];
 				String s = "called with arguments: " + args.length + ", code: " + sc.getCode()
 						+ ", client: " + ip.getRemoteName();
 				LOG.info(s);
 				++received;
 				return s;
 			}
 		}).when(packetHandler).handleRead(any(PacketData.class),
 				any(IPacketWriter.class));
 
 		packetClient.connect();
 		List<PacketData> list = new ArrayList<PacketData>();
 		list.add(sc0);
 		list.add(sc1);
 		list.add(sc2);
 		list.add(sc3);
 		list.add(sc4);
 		list.add(sc5);
 		Random r = new Random(System.nanoTime());
 		for (int i = 0; i < 6; ++i) {
 			PacketData sc = list.get(i);
 			if (i == 0) {
 				sc.setCode((short) 6);
 			} else {
 				sc.setCode((short) i);
 			}
 			sc.setVersion((byte) 8);
 			int len = (r.nextInt(1024) + 1) * 10240;
 			sc.setLength(len);
 			sc.setData(generateRandom(len, r));
 			packetClient.handleWrite(sc);
 		}
 		int i = 30;
 		while (i-- > 0) {
 			if (received == 6)
 				break;
			Thread.sleep(CoreRunner.CO_SLEEP);
 		}
 		packetClient.stop();
 		assertEquals(6, received);
 	}
 
 }
