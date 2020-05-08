 package org.net;
 
import static org.junit.Assert.assertEquals;

 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.util.Date;
 import java.util.Map;
 
 import org.junit.Test;
 import org.util.Util;
 import org.util.Util.Transformer;
 
 public class ClusterTest {
 
 	@Test
 	public void testCluster() throws IOException {
 		InetAddress localHost = InetAddress.getLocalHost();
 
 		Map<String, InetSocketAddress> peers = Util.createHashMap();
 		peers.put("NODE0", new InetSocketAddress(localHost, 3000));
 		peers.put("NODE1", new InetSocketAddress(localHost, 3001));
 
 		Cluster cluster0 = new Cluster("NODE0", peers);
 		Cluster cluster1 = new Cluster("NODE1", peers);
 
 		cluster1.setOnReceive(Integer.class,
 				new Transformer<Integer, Integer>() {
 					public Integer perform(Integer i) {
 						return i + 1;
 					}
 				});
 
 		cluster0.start();
 		cluster1.start();
 
 		Util.sleep(2 * 1000);
 
 		System.out.println("=== CLUSTER FORMED (" + new Date() + ") ===\n");
 
		assertEquals(12346, cluster0.requestForResponse("NODE1", 12345));
 
 		cluster0.stop();
 		cluster1.stop();
 
 		Util.sleep(1 * 1000);
 
 		System.out.println("=== CLUSTER STOPPED (" + new Date() + ") ===\n");
 	}
 
 }
