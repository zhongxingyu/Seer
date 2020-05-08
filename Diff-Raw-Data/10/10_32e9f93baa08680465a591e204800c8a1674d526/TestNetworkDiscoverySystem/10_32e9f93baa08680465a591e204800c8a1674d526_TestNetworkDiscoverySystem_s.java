 package de.tr0llhoehle.buschtrommel.test;
 
 import static org.junit.Assert.*;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import de.tr0llhoehle.buschtrommel.models.PeerDiscoveryMessage;
 import de.tr0llhoehle.buschtrommel.models.PeerDiscoveryMessage.DiscoveryMessageType;
 import de.tr0llhoehle.buschtrommel.network.NetCache;
 import de.tr0llhoehle.buschtrommel.network.UDPAdapter;
 import de.tr0llhoehle.buschtrommel.test.mockups.GuiCallbackMock;
 import de.tr0llhoehle.buschtrommel.test.mockups.MessageObserverMock;
 
 public class TestNetworkDiscoverySystem {
 
 	UDPAdapter nodeA, nodeB;
 	MessageObserverMock observerA, observerB;
 	NetCache cacheA, cacheB;
 	GuiCallbackMock guiA, guiB;
 
 	@Before
 	public void setUp() throws Exception {
 		nodeA = new UDPAdapter(8000, 8001);
 		observerA = new MessageObserverMock();
 		guiA = new GuiCallbackMock();
 		cacheA = new NetCache(nodeA, null, guiA);
 		nodeA.registerObserver(observerA);
 		nodeA.registerObserver(cacheA);
 
 		nodeB = new UDPAdapter(8001, 8000);
 		observerB = new MessageObserverMock();
 		guiB = new GuiCallbackMock();
 		cacheB = new NetCache(nodeB, null, guiB);
 		nodeB.registerObserver(observerB);
 		nodeB.registerObserver(cacheB);
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		nodeA.closeConnection();
 		nodeB.closeConnection();
 	}
 
 	@Test
 	public void testInitialNetworkExploration() throws IOException, InterruptedException {
 		nodeA.sendMulticast(new PeerDiscoveryMessage(DiscoveryMessageType.HI, "nodeA", 8080));
 		Thread.sleep(1000);
 		assertEquals(2, observerB.getMessages().size()); //2 because IPv4 and IPv6
 		assertTrue(observerB.getMessages().get(0) instanceof PeerDiscoveryMessage);
 		PeerDiscoveryMessage msg = ((PeerDiscoveryMessage) observerB.getMessages().get(0));
 		assertEquals("nodeA", msg.getAlias());
 		assertEquals(8080, msg.getPort());
 		assertNotNull(msg.getSource());
 		
 		assertEquals(2, cacheB.getHosts().size());
 		
 		//nodeA should NOT receive, because there is no Buschtrommel-Instance to respond
 	}
 
 }
