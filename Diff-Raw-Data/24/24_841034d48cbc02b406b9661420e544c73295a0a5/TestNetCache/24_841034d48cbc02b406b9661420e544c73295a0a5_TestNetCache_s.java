 package de.tr0llhoehle.buschtrommel.test;
 
 import static org.junit.Assert.*;
 
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.UnknownHostException;
 
 import org.junit.Test;
 
 import de.tr0llhoehle.buschtrommel.models.FileAnnouncementMessage;
 import de.tr0llhoehle.buschtrommel.models.Host;
 import de.tr0llhoehle.buschtrommel.models.LocalShare;
 import de.tr0llhoehle.buschtrommel.models.PeerDiscoveryMessage;
 import de.tr0llhoehle.buschtrommel.models.RemoteShare;
 import de.tr0llhoehle.buschtrommel.network.NetCache;
 
 public class TestNetCache {
 
 	@Test
 	public void testHostExists() throws UnknownHostException, InterruptedException {
 		NetCache tmp = new NetCache(null, null, null);
 		Host host = new Host(InetAddress.getByName("localhost"), "troll", 1234);
 		PeerDiscoveryMessage message = new PeerDiscoveryMessage(PeerDiscoveryMessage.DiscoveryMessageType.HI, "troll",
 				1234);
 
 		assertFalse(tmp.hostExists(host.getAddress()));
 
 		message.setSource(new InetSocketAddress(InetAddress.getByName("localhost"), 4747));
 		
 		tmp.receiveMessage(message);
 
 		Thread.currentThread().sleep(100);
 		
 		assertTrue(tmp.hostExists(host.getAddress()));
 
 		tmp.removeHost(host);
 		
 		Thread.currentThread().sleep(100);
 
 		assertFalse(tmp.hostExists(host.getAddress()));
 	}
 
 	@Test
 	public void testFileAnnouncment() throws UnknownHostException, InterruptedException {
 		NetCache tmp = new NetCache(null, null, null);
 		
 		PeerDiscoveryMessage peermessage = new PeerDiscoveryMessage(PeerDiscoveryMessage.DiscoveryMessageType.HI, "troll",
 				1234);
 		peermessage.setSource(new InetSocketAddress(InetAddress.getByName("localhost"), 4747));
 		
 		Host host = new Host(InetAddress.getByName("localhost"), "troll", 1234);
 		
 		tmp.receiveMessage(peermessage);
 		
 		Thread.currentThread().sleep(100);
 		
 		LocalShare share = new LocalShare("testhash", 42, 600, "katze", "katzenbilder!!!", "/home/katze.jpg");
 		
 		FileAnnouncementMessage message = new FileAnnouncementMessage(share);
 		
 		message.setSource(new InetSocketAddress(InetAddress.getByName("localhost"), 4747));
 		
 		tmp.receiveMessage(message);
 		
 		Thread.currentThread().sleep(100);
 
 		assertTrue(tmp.hostExists(host.getAddress()));
 		
 		assertTrue(tmp.shareExists("testhash"));
 	}
 
 	@Test
 	public void testTTLExpiration() throws UnknownHostException, InterruptedException {
		NetCache tmp = new NetCache(null, null, null);
		FileAnnouncementMessage message = new FileAnnouncementMessage(new LocalShare("testhash", 42, 10, "katze",
				"katzenbilder!!!", "/home/katze.jpg"));
 		message.setSource(new InetSocketAddress(InetAddress.getByName("localhost"), 4747));
 		tmp.receiveMessage(message);
 
 		assertTrue(tmp.shareExists("testhash"));
 
 		Thread.currentThread().sleep(11000);
 
 		assertFalse(tmp.shareExists("testhash"));
 	}
 
 }
