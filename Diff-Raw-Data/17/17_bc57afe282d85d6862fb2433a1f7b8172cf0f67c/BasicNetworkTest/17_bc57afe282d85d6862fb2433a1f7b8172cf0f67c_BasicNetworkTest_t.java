 package a43.lan.tests;
 
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import a43.lan.core.*;
 
 public class BasicNetworkTest {
 
 	protected Network net;
	protected Node mac, pc1, pc2, lpr, hub, alone;
 
 	@Before
 	public void setUp() throws Exception {
		// un nœud seul
		alone = new Node("isolé");

		// le réseau en étoile de l'énoncé
 		net = new Network();
 
 		hub = new Node("hub");
 		net.addNode(hub);
 		
 		mac = new Node("Mac");
 		net.addNode(mac);
 		net.connect(mac, hub);
 
 		pc1 = new Node("PC1");
 		net.addNode(pc1);
 		net.connect(pc1, hub);
 
 		pc2 = new Node("PC2");
 		net.addNode(pc2);
 		net.connect(pc2, hub);
 
 		lpr = new Node("lpr");
 		net.addNode(lpr);
 		net.connect(lpr, hub);
 	}
 
 	@Test
 	public void testScenarioSelfSend() {
		// un nœud peut s'envoyer des paquets,
		// même sans être dans un réseau (local loopback)
		Packet p = alone.originatePacket(alone, "Hello myself!");
 		
		assertTrue(p.isAddressedTo(alone));
		assertTrue(p.originatesFrom(alone));
 		assertTrue(p.wasReceived());
 	}
 
 	@Test
 	public void testScenarioHelloMacPc() {
 		Packet p1 = mac.originatePacket(pc1, "Hi, I'm a Mac!");
 		Packet p2 = pc1.originatePacket(mac, "And I'm a PC.");
 		
 		assertTrue(p1.isAddressedTo(pc1));
 		assertTrue(p1.originatesFrom(mac));
 		assertTrue(p1.wasReceived());
 		
 		assertTrue(p2.isAddressedTo(mac));
 		assertTrue(p2.originatesFrom(pc1));
 		assertTrue(p2.wasReceived());
 	}
 	
 	@Test
 	public void testScenarioLprDisconnected() {
 		net.disconnect(hub, lpr);
 		Packet p = mac.originatePacket(lpr, "Document à imprimer");
 		
 		assertTrue(p.isAddressedTo(lpr));
 		assertTrue(p.originatesFrom(mac));
 		assertFalse(p.wasReceived());
 	}
 }
