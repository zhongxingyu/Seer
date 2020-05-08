 package a43.lan.extensions.tests;
 
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import a43.lan.core.*;
 import a43.lan.extensions.buffering.*;
 import a43.lan.nodes.*;
 
 public class BufferingNetworkTest {
 
 	protected BufferingNetwork net;
 	protected Node mac, pc1, pc2, lpr, hub;
 
 	@Before
 	public void setUp() throws Exception {
 		buildNetwork();
 	}
 
 	public void buildNetwork() {
 		net = new BufferingNetwork();
 
 		hub = new Hub("hub");
 		net.addNode(hub);
 
 		mac = new Workstation("Mac");
 		net.addNode(mac);
 		net.connect(mac, hub);
 
 		pc1 = new Workstation("PC1");
 		net.addNode(pc1);
 		net.connect(pc1, hub);
 
 		pc2 = new Workstation("PC2");
 		net.addNode(pc2);
 		net.connect(pc2, hub);
 
 		lpr = new Printer("lpr", 2);
 		net.addNode(lpr);
 		net.connect(lpr, hub);
 	}
 
 	public Network network() {
 		if (net == null) { buildNetwork(); }
 		return net;
 	}
 
 	@Test
 	public void testScenarioHelloMacPc() {
 		Packet p1 = mac.originatePacket(pc1, "Hi, I'm a Mac!");
 		Packet p2 = pc1.originatePacket(mac, "And I'm a PC.");
 
 		assertTrue(p1.isAddressedTo(pc1));
 		assertTrue(p1.originatesFrom(mac));
 		assertTrue(p2.isAddressedTo(mac));
 		assertTrue(p2.originatesFrom(pc1));
 
 		assertFalse(p1.wasReceived());
 		assertFalse(p2.wasReceived());
 
 		net.timePasses();
 		net.timePasses();
 		net.timePasses();
 		assertTrue(p1.wasReceived());
 		assertTrue(p2.wasReceived());
 		assertEquals(1, ((Workstation) pc1).getNbConsumed());
 		assertEquals(1, ((Workstation) mac).getNbConsumed());
 	}
 
 	@Test
 	public void testScenarioSelfSend() {
 		Packet p = pc2.originatePacket(pc2, "Hello myself!");
 
 		assertTrue(p.isAddressedTo(pc2));
 		assertTrue(p.originatesFrom(pc2));
		assertFalse(p.wasReceived());
 
 		net.timePasses();
 		net.timePasses();
 		assertTrue(p.wasReceived());
 		assertEquals(1, ((Workstation) pc2).getNbConsumed());
 	}
 
 	@Test
 	public void testScenarioLprDisconnected() {
 		net.disconnect(hub, lpr);
 		Packet p = mac.originatePacket(lpr, "Document à imprimer");
 
 		assertTrue(p.isAddressedTo(lpr));
 		assertTrue(p.originatesFrom(mac));
 
 		net.timePasses();
 		net.timePasses();
 		net.timePasses();
 		net.timePasses();
 
 		assertFalse(p.wasReceived());
 	}
 }
