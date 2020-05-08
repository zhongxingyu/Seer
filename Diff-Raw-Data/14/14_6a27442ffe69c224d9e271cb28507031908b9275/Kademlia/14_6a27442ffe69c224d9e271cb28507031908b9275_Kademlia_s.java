 package com.github.bcap.dht;
 
 import java.math.BigInteger;
 import java.net.InetAddress;
 
 import com.github.bcap.dht.client.ConcurrentMessageSender;
 import com.github.bcap.dht.client.ResponseHandler;
 import com.github.bcap.dht.message.request.FindNodeRequest;
 import com.github.bcap.dht.message.response.Response;
 import com.github.bcap.dht.node.Contact;
 import com.github.bcap.dht.node.Identifier;
 import com.github.bcap.dht.node.Node;
 import com.github.bcap.dht.server.Server;
 
 public class Kademlia {
 	public static void main(String[] args) throws Exception {
 		BigInteger id = BigInteger.ONE.shiftLeft(3);
 		InetAddress ip = InetAddress.getLocalHost();
 		Integer port = 5000;
 		
		Node serverNode = new Node(id);
		Contact serverContact = new Contact(id, ip, port);
 		Contact clientContact = new Contact(id.shiftLeft(1), ip, port);
 
 		final Server server = new Server(ip, port);
 		server.addDefaultHandlers();
 		server.addNode(serverNode);
 		server.start();
 		
 		final ConcurrentMessageSender sender = new ConcurrentMessageSender(3);
 		FindNodeRequest request = new FindNodeRequest();
 		request.setDestination(serverContact);
 		request.setSource(clientContact);
 		request.setIdentifier(new Identifier(BigInteger.ONE));
 		sender.send(request, new ResponseHandler() {
 			public void handleResponse(Response response) {
 				System.out.println(response);
 				server.shutdown();
 				sender.shutdown();
 			}
 		});
		
 	}
}
