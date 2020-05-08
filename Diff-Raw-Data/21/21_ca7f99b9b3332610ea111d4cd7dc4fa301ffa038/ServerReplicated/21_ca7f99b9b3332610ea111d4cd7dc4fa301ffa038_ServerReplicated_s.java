 package week6;
 import point_to_point_queue.*;
 import java.io.IOException;
 import java.math.BigInteger;
 import java.util.HashMap;
 import java.net.InetSocketAddress;
 import java.util.HashSet;
 import multicast.MulticastQueue;
 import multicast.MulticastQueue.DeliveryGuarantee;
 import replicated_calculator.*;
 import week6.multicast.CalculatorQueue;
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
 
 	protected CalculatorQueue queue;
 	protected ServerListener listener;
 	protected HashSet<String> allClients = new HashSet<String>();
     
     public void createGroup(int serverPort, int clientPort) {
 		operationsFromClients = null;
 		try {
 			operationsFromClients = new PointToPointQueueReceiverEndNonRobust<ClientEvent>();
 			operationsFromClients.listenOnPort(clientPort);
 			queue = new CalculatorQueue();
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
 			queue = new CalculatorQueue();
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
 			if(!allClients.contains(event.clientName)){
 				super.visit(event);
 			}
 		}
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
 			if(nextOperation != null){
 				queue.put(nextOperation);
 			}
 		}
     }
     
 }
