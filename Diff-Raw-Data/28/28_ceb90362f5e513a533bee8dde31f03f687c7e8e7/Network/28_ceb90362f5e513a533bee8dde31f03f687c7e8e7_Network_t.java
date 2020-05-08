 package manager;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TreeMap;
 
 import manager.dht.FingerEntry;
 import manager.dht.Node;
 import manager.dht.NodeID;
 import manager.listener.FingerChangeListener;
 import manager.listener.KeepAliveListener;
 import manager.listener.NodeListener;
 import manager.listener.NodeMessageListener;
 
 public class Network {
 	private static Network instance = null;
 	public static int msg_delay = 250;
 	
 	private Timer timer;
 	
 	//Listener lists
 	private HashMap<Integer,Set<NodeMessageListener>> nodeMessageListener;
 	private Set<FingerChangeListener> fingerChangeListener;
 	private Set<NodeListener> nodeListener;
 	private Set<KeepAliveListener> keepAliveListener;
 
 	//Client list
 	private TreeMap<String,Communication> clients;
 	
 	private Network() {
 		//Singleton class
 		instance = this;
 		clients = new TreeMap<String,Communication>();
 		
 		this.timer = new Timer();
 		
 		nodeMessageListener = new HashMap<Integer, Set<NodeMessageListener>>();
 		fingerChangeListener = new HashSet<FingerChangeListener>();
 		nodeListener = new HashSet<NodeListener>();
 		keepAliveListener = new HashSet<KeepAliveListener>();
 	}
 	
 	public static Network getInstance() {
 		//Return singleton object
 		if(instance == null) instance = new Network();
 		return instance;
 	}
 	
 	public Collection<Communication> getClients() {
 		return this.clients.values();
 	}
 	
	public void sendMessage(Message m) {
 		
		//Get sender and receiver of the message
		Communication sender = null;
		sender = clients.get(m.getFromIp());
 		Communication receiver = null;
 		receiver = clients.get(m.getToIp());
 		
 		//Send the message to the receiver
 		if(receiver != null) {
			timer.schedule(new MessageForwarder(receiver, m, nodeMessageListener), msg_delay+sender.getMessageDelay()+receiver.getMessageDelay());
 		}
 		else {
 			System.out.println("!!!!! UNKNOWN DESTINATION !!!!!");
 		}
 	}
 
 	public boolean addNode(Communication comm, Node node) {
 		//Add node to list
 		if(!clients.containsKey(comm.getLocalIp())) {
 			clients.put(comm.getLocalIp(), comm);
 			//start the Communication object
 			comm.start(node);
 			//Inform listeners
 			for(NodeListener nl: nodeListener) nl.onNodeAdd(comm);
 			return true;
 		} else 
 			return false;
 	}
 	
 	public void removeNode(String networkAddress) {
 		Communication com = clients.remove(networkAddress);
 		//Inform listeners
 		for(NodeListener nl: nodeListener) nl.onNodeRemove(com);
 	}
 	
 	public void addNodeMessageListener(int msgType,NodeMessageListener listener) {
 		//Check if list exists, otherwise create it
 		if(!nodeMessageListener.containsKey(msgType)) {
 			nodeMessageListener.put(msgType,new HashSet<NodeMessageListener>());
 		}
 		
 		//Get listener list
 		Set<NodeMessageListener> nml = nodeMessageListener.get(msgType);
 		nml.add(listener);
 	}
 	
 	public void removeNodeMessageListener(int msgType,NodeMessageListener listener) {
 		Set<NodeMessageListener> listeners = nodeMessageListener.get(msgType);
 		if(listeners!=null) {
 			listeners.remove(listener);
 		}
 	}
 	
 	public boolean setMessageDelay(int delay,String networkAddress) {
 		if(networkAddress == null) {
 			msg_delay = delay;
 			return true;
 		}
 		else {
 			//Find node and set delay
 			Communication comm = clients.get(networkAddress);
 			
 			if(comm != null) {
 				comm.setMessageDelay(delay);
 				return true;
 			}
 			else return false;
 		}
 	}
 	
 	/**
 	 * Returns a String representing detailed information about all nodes 
 	 * in the network 
 	 * @return A String representing detailed information about all nodes in
 	 * the network
 	 * @see public String showNodeInfo(String networkAddress)
 	 */
 	public String showNodeInfo() {
 		String result = "";
 		
 		//For each node
 		for(Communication comm: clients.values()) {
 			result += comm.showNodeInfo() + "\n";
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * This method returns a String representing detailed information about the 
 	 * node that belongs to the network address given as parameter
 	 * @param networkAddress from the Node to show
 	 * @return A String representing detailed Node information
 	 */
 	public String showNodeInfo(String networkAddress) {
 		//forward to the communication
 		Communication comm = clients.get(networkAddress);
 		if(comm == null) {
 			return "There is no node with networkAddress {" + networkAddress + "}";
 		}
 		else {
 			return comm.showNodeInfo();
 		}
 	}
 	
 	/**
 	 * This method return a String representing the whole circle structure
 	 * starting at the Node which belongs to the Network address given as
 	 * parameter
 	 * @param startNodeName Entry point for the circle
 	 * @return A string that represents the circle if it is ok, or shows
 	 * error information if not
 	 */
 	public String showCircle(String startNodeName) {
 		HashSet<Communication> alreadyShown = new HashSet<Communication>();
 		List<Integer> intersections = new ArrayList<Integer>();
 
 		Communication startClient = clients.get(startNodeName);
 		Communication currentClient = startClient;
 
 		NodeID start,end;
 		int counter = 0;
 
 		//Test if node exists
 		if(startClient == null) {
 			return "Cannot find node " + startNodeName + "\n"; 
 		}
 		
 		//Set start and end region on DHT circle
 		start  = currentClient.getNodeID();
 		end = start;
 		
 		//Header
 		StringBuffer result = new StringBuffer("Pos\tNetworkAddress\t||  NodeID\n");
 
 		//Loop through the circle
 		while(true) {
 			//Get next node
 			result.append(new Integer(counter).toString() + "\t" + currentClient.showNodeInfo()+"\n");
 			alreadyShown.add(currentClient);
 			currentClient = clients.get(currentClient.getSuccessorAddress());
 			
 			//Check for loop
 			if(alreadyShown.contains(currentClient)) break;
 			
 			//Test for loop intersections
 			if(start.compareTo(end) > 0) {
 				if(currentClient.getNodeID().compareTo(start) >= 0 || currentClient.getNodeID().compareTo(end) <= 0) {
 					//Intersection detected!!
 					result.append(">>> Intersection detected <<<\n");
 					intersections.add(counter);
 				}
 			}
 			else {
 				if(currentClient.getNodeID().compareTo(start) >= 0 && currentClient.getNodeID().compareTo(end) <= 0) {
 					//Intersection detected!!
 					result.append(">>> Intersection detected <<<\n");
 					intersections.add(counter);
 				}
 			}
 			
 			//Shift end forward
 			end = currentClient.getNodeID();
 			counter++;
 		};
 		
 		if(currentClient == startClient) {
 			//Circle does not contain side-loop
 			if(alreadyShown.size() < clients.size()) {
 				result.append("DHT has orphaned nodes!\nIterated over " + alreadyShown.size() + " of " + clients.size());
 			}
 			else {
 				result.append("DHT is OK!\nIterated over all " + alreadyShown.size() + " nodes!");
 			}
 		}
 		else {
 			//Circle contains a side-loop! 
 			result.append("Aborting iteration! DHT contains side-loop!\nLoop destination is: " + currentClient.showNodeInfo() + "\nIterated over " + alreadyShown.size() + " Nodes of " + clients.size());
 		}
 		
 		//Show intersections
 		if(intersections.size() > 0) {
 			result.append("\nDHT contains " + new Integer(intersections.size()).toString() + " intersections @ ");
 			for(int i: intersections) {
 				result.append(new Integer(i).toString() + ",");
 			}
 		}
 		
 		return result.toString();
 	}
 
 	public void addFingerChangeListener(FingerChangeListener listener) {
 		//Add listener to list
 		fingerChangeListener.add(listener);
 	}
 	
 	public void removeFingerChangeListener(FingerChangeListener listener) {
 		//Remove listener
 		fingerChangeListener.remove(listener);
 	}
 	
 	public void fireFingerChangeEvent(int eventType,FingerEntry node,FingerEntry finger) {
 		//Inform all listener
 		for(FingerChangeListener l: fingerChangeListener) 
 			l.OnFingerChange(eventType, node, finger);
 	}
 	
 	public String showFinger(String nodeAddress) {
 		TreeMap<FingerEntry,FingerEntry> fingerTable;
 		TreeMap<Integer,FingerEntry> localTable;
 		
 		FingerEntry finger;
 		
 		Communication client;
 		String result = "";
 		int log2;
 		
 		//Get and check node
 		client = clients.get(nodeAddress);
 		if(client == null) return "Node " + nodeAddress + " not found!";
 		
 		//Get list
 		fingerTable = client.getNode().getFingerTable();
 		
 		//Transform table
 		localTable = new TreeMap<Integer,FingerEntry>();
 
 		//Successor
 		finger = client.getNode().getSuccessor(client.getNode().getIdentity().getNodeID());
 		log2 = NodeID.logTwoFloor(finger.getNodeID().sub(client.getNode().getIdentity().getNodeID()));
 		localTable.put(log2,finger);
 
 		//For each finger
 		for(FingerEntry fingerEntry: fingerTable.keySet()) {
 			log2 = NodeID.logTwoFloor(fingerEntry.getNodeID().sub(client.getNodeID()));
 			localTable.put(log2, fingerEntry);
 		}
 		
 		//Print list
 		for(int log2temp: localTable.keySet()) {
 			finger = localTable.get(log2temp);
 			result = result + "Addr: " + finger.getNetworkAddress() + " | hash:{" + finger.getNodeID().toString() + "} | log2: " + new Integer(log2temp).toString() + "\n";
 		}
 		
 		return result;
 	}
 	
 	public void addNodeListener(NodeListener nl) {
 		nodeListener.add(nl);
 	}
 	
 	public void removeNodeListener(NodeListener nl) {
 		nodeListener.remove(nl);
 	}
 	
 	public void addKeepAliveListener(KeepAliveListener listener) {
 		keepAliveListener.add(listener);
 	}
 		
 	public void removeKeepAliveListener(KeepAliveListener listener) {
 		keepAliveListener.remove(listener);
 	}
 	
 	public void fireKeepAliveEvent(NodeID key,String networkAddress) {
 		//Call each handler
 		for(KeepAliveListener kal: keepAliveListener) {
 			kal.OnKeepAliveEvent(new Date(), key, networkAddress);
 		}
 	}
 
 	public double calculateHealthOfDHT(boolean listMissingFinger) {
 		TreeMap<FingerEntry,FingerEntry> fingerTable;
 		TreeMap<FingerEntry,FingerEntry> DHT;
 		
 		FingerEntry currentSuccessor;
 		FingerEntry bestSuccessor;
 		NodeID hash_log2;
 		
 		int count_max = 0;
 		int count_ok = 0;
 		
 		//Print caption
 		if(listMissingFinger) System.out.println("Printing missing finger list...");
 
 		//Copy DHT into a map accessible through the NodeID  
 		DHT = new TreeMap<FingerEntry,FingerEntry>();
 
 		for(Communication client: clients.values()) {
 			FingerEntry newFinger;
 			newFinger = new FingerEntry(client.getNodeID(),client.getLocalIp());
 			DHT.put(newFinger,newFinger);
 		}
 		
 		//Check the quality of each client's finger table
 		for(Communication client: clients.values()) {
 			//Get finger table
 			fingerTable = client.getNode().getFingerTable();
 			
 			//Check each finger
 			for(int i = 0; i < NodeID.ADDRESS_SIZE * 8; i++) {
 				//Get current finger, if any, of the DHT region specified by log2 
 				hash_log2 = NodeID.powerOfTwo(i).add(client.getNodeID());
 				currentSuccessor = getSuccessor(fingerTable,hash_log2);
 				bestSuccessor = getSuccessor(DHT,hash_log2);
 				
 				//Compare
 				if(!bestSuccessor.equals(client.getNode().getIdentity())) {
 					//If a node exists there must be finger
 					count_max++;
 				
 					if(currentSuccessor.equals(bestSuccessor)) {
 						//Current successor is best successor
 						count_ok++;
 					}
 					else {
 						if(listMissingFinger) {
 							//Show missing finger
 							System.out.println("Node: (" + client.getLocalIp() + ") MISSING: (" + bestSuccessor.getNetworkAddress() + ")" );
 						}
 					}
 
 					//Set new log2 to skip unnecessary ranges
 					i = NodeID.logTwoFloor(bestSuccessor.getNodeID().sub(client.getNodeID()));
 				}
 				else {
 					//Finished all nodes
 					break;
 				}
 			}
 		}
 		
 		//Return rate of DHT health#
 		//If there is only one node there can't be a finger, therefore the DHT is OK
 		return count_max > 0 ? (double)count_ok / count_max : 1.0;
 	}
 
 	public FingerEntry getSuccessor(TreeMap<FingerEntry,FingerEntry> table,NodeID nodeID) {
 		FingerEntry hash = new FingerEntry(nodeID,null);
 		FingerEntry result;
 
 		synchronized(this) {
 			//Get successor of us
 			result = table.higherKey(hash);
 			if(result == null) { 
 				//There is no higher key in the finger tree
 				result = table.firstKey();
 			}
 		}
 		
 		return result;
 	}
 }
