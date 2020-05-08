 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package week6.multicast;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import point_to_point_queue.PointToPointQueueReceiverEndNonRobust;
 import point_to_point_queue.PointToPointQueueSenderEndNonRobust;
 import replicated_calculator.ClientEvent;
 import replicated_calculator.ClientEventConnect;
 import replicated_calculator.ClientNonRobust;
 import replicated_calculator.Parameters;
 
 /**
  *
  * @author larss
  */
 public class ProperConnectorClient extends ClientNonRobust{
 	
 	/**
      * Connects to the server and sends a connect event to the server.
      * Opens a point-to-point queue for receiving acknowledgements from the server.
      * Then starts a thread (this) which polls the acknowledgements and treats them.
      */
     synchronized public boolean connect(InetSocketAddress addressOfServer, int clientPortForServer, String clientName) {
 		this.clientName = clientName;
		initTimestamp();
 		this.toServer = new PointToPointQueueSenderEndNonRobust<ClientEvent>();
 		this.toServer.setReceiver(addressOfServer);	
 		
 		try {
 			final String myAddress = InetAddress.getLocalHost().getCanonicalHostName();
 			this.fromServer = new PointToPointQueueReceiverEndNonRobust<ClientEvent>();
 			this.fromServer.listenOnPort(clientPortForServer);
 			toServer.put(new ClientEventConnect(clientName,eventID++,new InetSocketAddress(myAddress,clientPortForServer),timestamp));
 		} catch (IOException e) {
 			return false;
 		}
 		
 		this.start();
 		return true;
     }
 }
