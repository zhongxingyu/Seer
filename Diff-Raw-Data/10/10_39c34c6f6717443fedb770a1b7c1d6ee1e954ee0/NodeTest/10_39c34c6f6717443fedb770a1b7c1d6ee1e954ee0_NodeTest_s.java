 package org.cipango.diameter;
 
import java.net.InetAddress;

import org.cipango.diameter.base.Base;
import org.cipango.diameter.ims.IMS;

 import junit.framework.TestCase;
 
 public class NodeTest extends TestCase
 {
 	public void testNull()
 	{
 		Object o = null;
 		System.out.println(o instanceof String);
 	}
	
 	public void testCnx() throws Exception
 	{
 		Node server = new Node(3869);
 		server.setIdentity("test");
 		
 		Peer peer = new Peer("cipango");
 		peer.setAddress(InetAddress.getLocalHost());
 		peer.setPort(3868);
 		
 		server.addPeer(peer);	
 		
 		server.start();
 		//client.start();
 		
 		Thread.sleep(10000);
 		
 		DiameterRequest request = new DiameterRequest(server, IMS.MAR, IMS.CX_APPLICATION_ID, "toto");
 		request.getAVPs().addString(Base.DESTINATION_HOST, "cipango");
 		request.send();
 		
 		peer.getConnection().close();
 		
 		Thread.sleep(5000);
 	}
 }
