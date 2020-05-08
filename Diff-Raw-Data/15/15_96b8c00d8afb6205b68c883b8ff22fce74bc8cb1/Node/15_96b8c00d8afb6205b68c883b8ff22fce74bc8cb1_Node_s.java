 package manager.dht;
 
 import java.util.TreeMap;
 
 import manager.CommunicationInterface;
 import manager.LookupServiceInterface;
 import manager.Message;
 import manager.dht.messages.broadcast.BroadcastMessage;
 import manager.dht.messages.broadcast.NotifyJoinBroadcastMessage;
 import manager.dht.messages.unicast.DuplicateNodeIdMessage;
 import manager.dht.messages.unicast.FindPredecessorMessage;
 import manager.dht.messages.unicast.JoinMessage;
 import manager.dht.messages.unicast.JoinResponseMessage;
 import manager.dht.messages.unicast.NotifyJoinMessage;
 
 public class Node extends Thread implements LookupServiceInterface {
 	//Communication
 	private CommunicationInterface communication;
 	private String bootstrapAddress;
 
 	//Own state in the DHT
 	private TreeMap<FingerEntry,FingerEntry> finger;
 	private FingerEntry identity;
 	private boolean bConnected = false;
 
 	//This is the node count of the DHT
 	//At the beginning it's one (we)
 	//After every change call checkFingerTable
 	private int nodeCount = 1; 
 
 	public Node(CommunicationInterface communication,String bootstrapAddress) {
 		this.communication = communication;
 
 		//Generate hash from the local network address
 		//TODO ask stefan if inclusion of port address is reasonable
 		byte[] hash = SHA1Generator.SHA1(communication.getLocalIp());
 
 		//Set identity
 		setIdentity(hash);
 		
 		//Save bootstrap address
 		//No bootstrap means, WE are the beginning of the DHT
 		//If we are a bootstrapping node, that means bootstrapping address is null or is our address,
 		//we are always connected !!
 		if(bootstrapAddress == null || bootstrapAddress.equals(communication.getLocalIp())) {
 			bConnected = true;
 		}
 		else {
 			this.bootstrapAddress = bootstrapAddress;
 		}
 		
 		//Start thread
 		this.start();
 	}
 
 	@Override
 	public void resolve(String uci) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void register(String uci) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void shutdown() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void handleMessage(Message message) {
 		//Don't process message if it was not for us!!
 		//TODO probably not necessary 
 		//TODO check message for null ?!?!?!?!
 		if(!message.toIp.equals(identity.getNetworkAddress())) return;
 		
 		//Safe performance for node
 		//if(finger.))
 		
 		switch (message.type) {
 			//react on a Join message
 			case Message.JOIN:
 				JoinMessage join_msg = (JoinMessage) message;
 				Message answer = null;
 
 				FingerEntry predecessor = findPredecessorOf(join_msg.getKey());
 				FingerEntry successor = getSuccessor(identity.getNodeID());
 				
 				//Forward or answer?
 				if(predecessor.equals(identity)) {
 					//Its us => reply on JOIN
 
 					//Check if it exists
 					FingerEntry newFingerEntry = new FingerEntry(new NodeID(join_msg.getKey().getID()),join_msg.getOriginatorAddress());
 					FingerEntry tempFinger = finger.get(newFingerEntry);
 					
 					//If another node tried to enter the DHT with the same key
 					//Skip if the same node tried again!
 					if(tempFinger != null) {
 						if(!tempFinger.getNetworkAddress().equals(newFingerEntry.getNetworkAddress())) {
 							//Key not allowed message
 							answer = new DuplicateNodeIdMessage(identity.getNetworkAddress(), join_msg.fromIp,join_msg.getKey());
 						}
 					}
 					else {
 						//Prepare answer
 						nodeCount++;
 						answer = new JoinResponseMessage(identity.getNetworkAddress(), join_msg.getOriginatorAddress(),join_msg.getKey(), successor.getNetworkAddress(),successor.getNodeID(),nodeCount);
 
 						
 						
 						sendBroadcast(new NotifyJoinBroadcastMessage(join_msg.getOriginatorAddress(),join_msg.getKey()));
 						
 						//TODO figure out if this works!!!
 						//Change our successor (Only if it's not us!)
 						//if(!successor.equals(identity))
 						//	finger.remove(successor);
 						finger.put(newFingerEntry,newFingerEntry);
 						
 						//Repair finger count
 						checkFingerTable();
 					}
 				}
 				else {
 					//Forward to the best fitting predecessor
 					message.fromIp = identity.getNetworkAddress();
 					message.toIp = getPredecessor(join_msg.getKey()).getNetworkAddress();
 					answer = message;
 				}
 				
 				//Send message
 				if(answer != null) communication.sendMessage(answer);
 				break;
 			case Message.JOIN_RESPONSE:
 				JoinResponseMessage jrm = (JoinResponseMessage) message;
 
 				//Ignore JOIN_RESPONSE message if the node is already connected!
 				if(!bConnected) {
 					if(jrm.getJoinKey().equals(identity.getNodeID())) {
 						//Add finger
 						FingerEntry newFingerEntry = new FingerEntry(jrm.getSuccessor(), jrm.getSuccessorAddress());
 						finger.put(newFingerEntry,newFingerEntry);
 						bConnected = true;
 														
 						//Repair finger table
 						nodeCount = jrm.getNodeCount();
 						checkFingerTable();
 					}
 					else {
 						//Ignore this because the key does not match!!!
 						//TODO react on this
 					}
 				}
 				
 				break;
 			case Message.DUPLICATE_NODE_ID:
 				DuplicateNodeIdMessage dupMsg = (DuplicateNodeIdMessage)message;
 				
 				//If the node is not connected allow the change of the identity
 				//Check the duplicate id also
 				if(!bConnected && dupMsg.getDuplicateKey().equals(identity.getNodeID())) {
 					//TODO what shall we do here?????
 					assert(true);
 				}
 
 				break;
 			case Message.BROADCAST:
 				BroadcastMessage bcast_msg = (BroadcastMessage)message;
 				
 				//Forward broadcast
 				if(bcast_msg.getTTL() > 0) {
 					for(FingerEntry fingerEntry: finger.keySet()) {
 						if(!fingerEntry.equals(identity)) {
 							//Decrement TTL
 							bcast_msg.setTTL(bcast_msg.getTTL());
 							communication.sendMessage(bcast_msg);
 						}
 					}
 				}
 				
 				//Process broadcast
 				handleMessage(bcast_msg.extractMessage());
 				break;
 			case Message.KEEPALIVE:
 				//Handle keep-alive message 
 				break;
 			case Message.NODE_JOIN_NOTIFY:
 				NotifyJoinMessage njm = (NotifyJoinMessage)message;
 				
 				//New node!
 				nodeCount++;
 				
 				//Check finger table
 				checkFingerTable();
 				
 				break;
 			default:
 				//TODO Throw a Exception for a unsupported message?!
 		}
 		
 	}
 	
 	@Override
 	public void run() {
 		//Connect DHT node
 		while(bConnected == false) {
 			//Try to connect to DHT
 			communication.sendMessage(new JoinMessage(identity.getNetworkAddress(),bootstrapAddress,identity.getNetworkAddress(),identity.getNodeID()));
 			
 			try {
 				//Wait for connection and try again
 				Thread.sleep(5000);
 			}
 			catch (InterruptedException e) {
 				//Exit thread
 				break;
 			}
 		}
 		
 		//Wait for nothing
 		while(true) {
 			try {
 				//Wait for connection and try again
 				Thread.sleep(5000);
 			}
 			catch (InterruptedException e) {
 				//Exit thread
 				break;
 			}
 		}
 	}
 	
 	public FingerEntry getIdentity() {
 		return identity;
 	}
 	
 	private FingerEntry getPredecessor(NodeID startHash) {
 		FingerEntry predecessor;
 		
 		//Get successor of us
 		predecessor = finger.lowerKey(new FingerEntry(startHash,null));
 		if(predecessor == null) 
 			predecessor = finger.lastKey();
 		return predecessor;
 	}
 	
 	public FingerEntry getSuccessor(NodeID startHash) {
 		FingerEntry successor;
 		
 		//Get successor of us
 		successor = finger.higherKey(new FingerEntry(startHash,null));
 		if(successor == null) 
 			successor = finger.firstKey();
 		return successor;
 	}
 	
 	private FingerEntry findPredecessorOf(NodeID nodeID) {
 		FingerEntry precessor;
 		
 		//Find predecessor of a node
 		precessor = finger.lowerKey(new FingerEntry(nodeID,null));
 		if(precessor == null) 
 			precessor = finger.lastKey();
 		
 		return precessor;
 	}
 	
 	private void setIdentity(byte[] hash) {
 		//Set identity
 		identity = new FingerEntry(new NodeID(hash),communication.getLocalIp());
 		
 		//(Re-)initialize finger table
 		//Always add ourselves to the finger table
 		finger = new TreeMap<FingerEntry,FingerEntry>();
 		finger.put(identity,identity);
 	}
 	
 	private void checkFingerTable() {
 		//Calculate the nominal amount of finger-table entries;
 		int nominalCount = new Double(Math.ceil(Math.log10(nodeCount) / Math.log10(2))).intValue() + 1;
 		FingerEntry fingerEntry;
 		NodeID hash;
 		
 		if(nominalCount > finger.size()) {
 			FindPredecessorMessage msg;
 			
 			for(int n = finger.size(); n < nominalCount; n++) {
 				//Calculate hash of the Node that we want to have...
 				hash = identity.getNodeID().add(NodeID.powerOfTwo(n));
 				
 				//... and find its predecessor
				msg = new FindPredecessorMessage(identity.getNetworkAddress(), getSuccessor(new FingerEntry(hash,null)).getNetworkAddress(), hash);
 				
 				//TODO send message!
 				communication.sendMessage(msg);
 			}
 		}
 		else if(nominalCount < finger.size()) {
 			//Drop some fingers
 			for(int n = 0; n < finger.size() - nominalCount; n++) {
				fingerEntry = getPredecessor(identity);
 				finger.remove(fingerEntry);
 			}
 		}
 	}
 	
 	private void sendBroadcast(BroadcastMessage bcast_msg) {
 		//Forward broadcast
 		FingerEntry startFinger = finger.get(identity);
 		FingerEntry currentFinger = startFinger;
 		
 		for(int i = 0; i < finger.size() - 1; i++) {
 			//Get next finger
			currentFinger = getSuccessor(currentFinger);
 			if(currentFinger == startFinger) {
 				//Too less fingers !
 				break;
 			}
 			
 			//Send broadcast message
 			bcast_msg.fromIp = identity.getNetworkAddress();
 			bcast_msg.toIp = currentFinger.getNetworkAddress();
 			bcast_msg.setTTL(i);
 			communication.sendMessage(bcast_msg);
 		}
 	}
 }
