 package com.github.uberroot.NCJBot;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Random;
 
 /**
  * <p>This class keeps track of other nodes within the network. The nodes within a network only know of each
  * others status through direct communication. Network updates are not currently relayed to other nodes; rather a node
  * figures out the status of a remote node when it needs something from that remote node. The network manager maintains a timer
  * to rebroadcast its presence to the rest of the network. This allows the network to recover this node
  * in the event a connection error resulted in this node being removed from the active list of other nodes.
  * Currently, this timer is set to announce the presence of the node once per hour (non-configurable).</p>
  * 
  * <p>Currently, there is little functionality regarding monitoring node network health; remote node health is implemented using a lazy algorithm.
  * Nodes alert all other nodes when they join the network. Any time one node needs something from another node, it will try to connect. If
  * it is unsuccessful, it will be assumed to be down. This only changes the internal state of the requesting node; other nodes will need to
  * learn of such failures on their own. In order to remedy situations in which nodes are falsely assumed down (i.e. due to an unstable network),
  * all nodes broadcast their presence to all nodes known.</p>
  * <br>
  * 
  * <b>NOTE: THIS NEXT SECTION HAS NOT YET BEEN IMPLEMENTED / MAY NOT BE IMPLEMENTED AS DESCRIBED</b>
  * <p>The NetworkManager allows for two modes of operation: "Full" and "Gossip".</p>
  * 
  * <p>In full mode, this node will establish its presence with ALL other nodes. All network change messages will be broadcast
  * to the entire network. Each node running under full mode that receives a change message will either repeat the message to
  * the rest of the network, or discard the message if it is already aware of the change (preventing a flood).
  * This mode of operation is intended for smaller networks, as it can create a great deal of traffic. The primary
  * objective of this mode is to create a minimal delay between the time a network change occurs and the time
  * the entire network compensates for the event. This mode may not be implemented.</p>
  * 
  * <p>Under gossip mode, the initial presence announcement will only be made to a random subset of nodes, the number of which is configurable (or may be fixed at 2).
  * Message broadcasts will then be relayed to a random subset of nodes within the network, the size of which is configurable (or may be fixed at 2).
  * Each receiving node running in gossip mode will then relay the message to another random subset of the network,
  * or discard the message if it is already aware of the event. A more structured implementation may be used to form links between the
  * nodes that are always used rather than select a new random subset on each relay, creating a more consistent network structure.
  * A variation of this mode will likely be implemented.</p>
  * 
  * 
  * <p>Under either mode, messages are relayed at a configurable rate. Propagation may either stop after a TTL expiration or utilize an event resolution technique that may work as follows:
  * <ul>
  * 	<li>If a node receives information on a node and has never received the type of data before, assume it's correct and propagate.</li>
  * 	<li>If a node receives information on a node and has identical information, assume it's correct and ignore.</li>
  * 	<li>If a node receives information on a node and the information conflicts with current information and the new information has a later timestamp, assume it's correct and propagate.</li>
  * 	<li>If a node receives information on a node and the information conflicts with current information and the new information has an earlier timestamp, assume it's incorrect and propagate the correct information only to the node that sent the incorrect information.</li> 
  * </ul>
  * A method for synchronization still needs to be developed.</p>
  * 
  * @author Carter Waxman
  *
  */
 //TODO: This should be a singleton class
 //TODO: This functionality will be moved away from it's own thread and into centralized timing
 public class NetworkManager extends Thread{
 	/**
 	 * <p>The list of all nodes known to be active on the network.</p>
 	 */
 	private ArrayList<RemoteNode> activeNodes;
 	
 	/**
 	 * <p>Node that have been discovered and who's discovery needs to be relayed to other nodes.</p>
 	 */
 	private ArrayList<RemoteNode> addedNodes; 
 	
 	/**
 	 * <p>Initializes the NetworkManager with the given seed nodes. Each of the seed nodes will be contacted and queried for a
 	 * list of known nodes. The results of the queries will be combined into the active node list, including those
 	 * seeds that responded.</p>
 	 * @param seedNodes The initial list of nodes to query. These should be considered the entry points for the network.
 	 */
 	public NetworkManager(ArrayList<RemoteNode> seedNodes){
 		activeNodes = new ArrayList<RemoteNode>();
 		addedNodes = new ArrayList<RemoteNode>();
 		
 		//Find which seed nodes are active
 		System.out.println("Attempting to connect to seed nodes...");
 		boolean hasList = false;
 		for(RemoteNode n : seedNodes){
 			System.out.print("Attempting " + n.getIpAddress() + ":" + n.getListeningPort() + "...\t");
 			
 			//Try to create socket
 			Socket s = null;
 			try {
 				s = new Socket(n.getIpAddress(), n.getListeningPort());
 			} catch (IOException e) {
 				//If here, either host doesn't exist, or is not listening on the port
 				System.out.println("Unable to connect");
 				continue;
 			}
 			
 			//See if node is active
 			try {
 					char buffer[] = new char[1500];
 					BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
 					s.getOutputStream().write("Are you alive?".getBytes());
 					in.read(buffer);
 					
 					if(String.valueOf(buffer).trim().equals("I'm not dead yet.")){
 						System.out.println("Node is active");
 						activeNodes.add(n);
 						if(!hasList){
 							System.out.print("Retreiving node list...\t");
 							s.getOutputStream().write("Who do you know?".getBytes());
 							buffer = new char[1500];
 							in.read(buffer);
 							String[] nodeStrings = String.valueOf(buffer).trim().split("\n");
 							
 							//Parse the node list from this node
 							for(String ns : nodeStrings){
 								if(!ns.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}:\\d+"))
 									continue;
 								String[] pair = ns.split(":");
 								RemoteNode rn = new RemoteNode(pair[0], Integer.valueOf(pair[1]));
 								
 								//Add the new node to this node's active node list
 								if(!activeNodes.contains(rn)){
 									activeNodes.add(rn);
 									ProcessorNode.announceFoundNode(rn);
 								}
 							}
 							hasList = true;
 							System.out.println("Done");
 						}
 					}
 					else if(String.valueOf(buffer).trim().equals("I'm bleeding out.")){
 						System.out.println("Node is shutting down");
 					}
 					else
 						System.out.println("Unknown node state: " + String.valueOf(buffer).trim());
 					
 					//Allow the server to close the connection
 					s.getOutputStream().write("Goodbye.".getBytes());
 			} catch (IOException e) {
 				System.out.println("Unable to determine node state");
 			}
 			 
 			//Close the socket
 			try {
 				s.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 				System.exit(-1);
 			}
 		}
 		
 		//Are there nodes?
 		if(activeNodes.size() == 0)
 			System.out.println("No active nodes found. Starting as lone node.");
 		
 		setName("Network Manager");
 	}
 	
 	/**
 	 * <p>This method runs an hourly beacon for alerting nodes in the network of this node's presence. </p>
 	 * <b>THIS IMPLEMENTATION WILL BE REPLACED WITH A CALLBACK-BASED CENTRALIZED TIMING THREAD.</b>
 	 */
 	@Override
 	public void run(){
 		int minute = 0;
 		while(true){
 			try {
 				//Run once per hour
 				if(minute % 60 == 0){
 					if(minute >= 60)
 						minute -= 60;
 					System.out.println("Announcing presence...");
 					for(int i = 0; i < activeNodes.size(); i++){
 						RemoteNode n = activeNodes.get(i);
 						
 						//Try to create socket
 						Socket s = null;
 						try {
 							s = new Socket(n.getIpAddress(), n.getListeningPort());
 						} catch (IOException e) {
 							//If here, either host doesn't exist, or is not listening on the port
 							System.out.println("Unable to connect");
 							ProcessorNode.announceNodeFailure(activeNodes.remove(i--));
 							continue;
 						}
 						
 						//See if node is active
 						try {
 								char buffer[] = new char[1500];
 								BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
 								s.getOutputStream().write("Are you alive?".getBytes());
 								in.read(buffer);
 								
 								//Announce presence
 								if(String.valueOf(buffer).trim().equals("I'm not dead yet.")){
 									String toSend = "I'm here.\n" + ProcessorNode.getListenPort();
 									//System.out.println("..." + toSend + "...");
 									s.getOutputStream().write(toSend.getBytes());
 									
 									//TODO: should this actually be read? It tells whether the other node knew of this one.
 									in.read(buffer); //To ensure flow control
 								}
 								else if(String.valueOf(buffer).trim().equals("I'm bleeding out.")){
 									//TODO: This isn't a failure, but should this be announced via ProcessorNode?
 									activeNodes.remove(i--);
 								}
 								else
 									//TODO: This isn't a failure, but should this be announced via ProcessorNode?
 									System.out.println("Unknown node state: " + String.valueOf(buffer).trim());
 								s.getOutputStream().write("Goodbye.".getBytes());
 						} catch (IOException e) {
 							//The connection was interrupted for some reason...
 							System.out.println("Unable to determine node state");
 							ProcessorNode.announceNodeFailure(activeNodes.remove(i--));	
 						}
 						 
 						//Close the socket
 						try {
 							s.close();
 						} catch (IOException e) {
 							//This shouldn't happen. Assume the worst.
 							e.printStackTrace();
 							System.exit(-1);
 						}
 					}
 				}
 				Thread.sleep(60000); //Wait a minute
 			} catch (InterruptedException e) { //This is used to kill the thread by calling interrupt().
 				break;
 			}
 			minute++; //TODO: This minute counter is a relic of an older implementation, but will be included in a new one so it remains.
 		}
 		System.out.println("The network manager has stopped");
 	}
 
 	/**
 	 * <p>Retrieves a copy of the known active nodes for the network.</p>
 	 * 
 	 * @return The known active nodes for the network.
 	 */
 	//TODO: Randomization for load balancing (this could be done in ProcessorNode.getNodes() instead)
 	public List<RemoteNode> getActiveNodes() {
 		return Collections.unmodifiableList(activeNodes);
 	}
 	
 	/**
 	 * <p>Directly adds a new node to the active node list. If the node was not previously known,
 	 * its presence will be announced to all running jobs and services of this node.</p>
 	 * 
 	 * @param rn the node to add
 	 * @return True if the node was an addition to the list, false if the node was already known.
 	 */
 	public boolean addDiscoveredNode(RemoteNode rn){
 		if(!activeNodes.contains(rn)){
 			activeNodes.add(rn);
 			addedNodes.add(rn);
 			ProcessorNode.announceFoundNode(rn);
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * <p>Finds a replacement node for the node given. In the event the given node cannot be reached, it will be
	 * removed from the active node list. Otherwise, a different node will be randomly selected and returned.
	 * If there are no nodes available to replace the one provided, this method will return null.</p>
 	 * 
 	 * @param r The node to replace.
 	 * @return A randomly selected node from the active node list.
 	 */
 	public RemoteNode getReplacement(RemoteNode r){
 		//See if r is offline
 		Socket s = null;
 		try {
 			s = new Socket(r.getIpAddress(), r.getListeningPort());
 		} catch (IOException e) {
 			//If here, either host doesn't exist, or is not listening on the port
 			System.out.println("Removing unresponsive node");
 			activeNodes.remove(r);
 		}
 		try {
			if(s != null)
				s.close();
 		} catch (IOException e) {
 			//This shouldn't happen. Assume the worst.
 			e.printStackTrace();
			ProcessorNode.quit();
 		}
 		
 		//Select a new node
 		Random rand = new Random();
		ArrayList<RemoteNode> pool = new ArrayList<RemoteNode>(activeNodes);
		pool.remove(r);
		if(pool.size() > 0)
			return pool.get(rand.nextInt(pool.size()));
		else
			return null;
 	}
 }
