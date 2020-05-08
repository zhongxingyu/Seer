 package week6;
 import point_to_point_queue.*;
 import java.io.IOException;
 import java.math.BigInteger;
 import java.util.HashMap;
 import java.net.InetSocketAddress;
 import java.util.HashSet;
 import multicastqueue.MulticastMessage;
 import multicast.MulticastQueue;
 import multicastqueue.MulticastQueue.DeliveryGuarantee;
 import multicastqueue.MulticastQueueTotalOnly;
 import multicastqueue.Timestamp;
 import replicated_calculator.*;
 import week4.multicast.messages.AbstractLamportMessage;
 import week6.multicast.messages.ClientEventMessage;
 
 /**
  * 
  * A stand-alone implementation of a server. It simply keeps the variables in 
  * a hash map and executes the commands in the natural way on this hash map.
  * 
  * @author Jesper Buus Nielsen, Aarhus University, 2012.
  *
  */
 
 public class ServerReplicated extends ServerStandalone implements ClientEventVisitor, Server {
 
 	protected MulticastQueueTotalOnly<ClientEvent> queue;
 	protected ServerListener listener;
 	protected HashSet<String> allClients = new HashSet<String>();
 
 	@Override
 	protected void acknowledgeEvent(ClientEvent event) {
 		event.timestamp.compareTimeStamp(queue.timestamp);
 		synchronized(clients){
 			if(clients.containsKey(event.clientName))
 				super.acknowledgeEvent(event);
 		}
 	}
 	
 	
     
     public void createGroup(int serverPort, int clientPort) {
 		operationsFromClients = null;
 		try {
 			operationsFromClients = new PointToPointQueueReceiverEndNonRobust<ClientEvent>();
 			operationsFromClients.listenOnPort(clientPort);
 			queue = new MulticastQueueTotalOnly<ClientEvent>();
 			queue.createGroup(serverPort, DeliveryGuarantee.TOTAL);
 		} catch (IOException e) {
 			System.err.println("Cannot start server!");
 			System.err.println("Check your network connection!");
 			System.err.println("Check that no other service runs on port " + clientPort + " or " + serverPort + "!");
 			System.err.println();
 			System.err.println(e);
 			System.exit(-1);
 		}		
 		this.start();
     }
     
     public void joinGroup(int serverPort, InetSocketAddress knownPeer, int clientPort) {
 		operationsFromClients = null;
 		try {
 			operationsFromClients = new PointToPointQueueReceiverEndNonRobust<ClientEvent>();
 			operationsFromClients.listenOnPort(clientPort);
 			queue = new MulticastQueueTotalOnly<ClientEvent>();
 			queue.joinGroup(serverPort, knownPeer, DeliveryGuarantee.TOTAL);
 			
 		} catch (IOException e) {
 			System.err.println("Cannot start server!");
 			System.err.println("Check your network connection!");
 			System.err.println("Check that no other service runs on port " + Parameters.serverPortForClients + "!");
 			System.err.println();
 			System.err.println(e);
 			System.exit(-1);
 		}		
 		this.start();
     }
 	
 	public void visit(ClientEventRemoteConnect event){
 		synchronized(allClients){
 			allClients.add(event.clientName);
 		}
 	}
 	
 	public void visit(ClientEventRemoteDisconnect event){
 		synchronized(allClients){
 			allClients.remove(event.clientName);
 		}
 	}
 	
 	public void visit(ClientEventConnect event){
 		synchronized(allClients){
 			if(event instanceof ClientEventRemoteConnect) {
 				allClients.add(event.clientName);
 				return;
 			}
 			
 			//System.out.println("Acknowledging event: "+event+"\nAll clients: "+allClients);
 			
 			if(event instanceof ClientEventConnect && allClients.contains(event.clientName)){
 				
 				// Get info for connection
 				final String clientName = event.clientName;
 				final InetSocketAddress clientAddress = event.clientAddress;
 				
 				// Create the Point to Point connection
 				PointToPointQueueSenderEndNonRobust<ClientEvent> queueToClient = new PointToPointQueueSenderEndNonRobust<ClientEvent>(); 
 				
 				// Set the receiver to the client that just tried connecting
 				queueToClient.setReceiver(clientAddress);
 				
 				// Send the connect denied event
 				queueToClient.put(new ClientEventConnectDenied(event.clientName, event.eventID, queue.timestamp));
 				return;
 			}
 
 			allClients.add(event.clientName);
 		}
 		//synchronized(allClients){
 		//	if(!allClients.contains(event.clientName)){
 		super.visit(event);
 		//	}
 		//}
 	}	
     
     /**
      * No group to leave, so simply shutsdown.
      */
 	@Override
     public void leaveGroup() {
 		queue.leaveGroup();
 		operationsFromClients.shutdown();
 		for (String client : clients.keySet()) {
 			clients.remove(client).shutdown();
 		}
 		listener.run = false;
 		listener.interrupt();
     }
     
     /** 
      * Keeps getting the next operation from a server and then visits the operation.
      * Sub-classes have to implement the visiting methods.
      */
 	@Override
     public void run() {
 		ClientEvent nextOperation = null;
 		listener = new ServerListener(queue,this);
 		listener.start();
 		while ((nextOperation = operationsFromClients.get())!=null) {
 			synchronized(queue.timestamp){
 				try{
 					while(queue.timestamp.getTime() < nextOperation.timestamp.getTime())
 					{
 						System.out.println("Catching up..." + queue.timestamp + " < " + nextOperation.timestamp);
 						sleep(5);
 					}
 				} catch(InterruptedException e){
 					
 				}
 				
 				if(nextOperation != null){
 					queue.put(nextOperation);
 				}
 			}
 		}
     }
     
 }
