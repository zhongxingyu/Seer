 package manager.dht;
 
 import java.util.Random;
 import java.util.TreeSet;
 
 import manager.CommunicationInterface;
 import manager.LookupServiceInterface;
 import manager.Message;
 
 public class Node extends Thread implements LookupServiceInterface {
 	private CommunicationInterface communication;
 	private FingerEntry identity;
 	
 	private String bootstrapAddress;
 
 	private TreeSet<FingerEntry> finger;
 	private boolean bConnected = false;
 
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
 		if(!message.toIp.equals(identity.getNetworkAddress())) return;
 		
 		//Safe performance for node
 		//if(finger.))
 		
 		switch (message.type) {
 			//react on a Join message
 			case Message.JOIN:
 				JoinMessage join_msg = (JoinMessage) message;
 				Message answer;
 
 				FingerEntry predecessor = findPredecessorOf(join_msg.getKey());
 				FingerEntry successor = getSuccessor();
 				
 				//Forward or answer?
 				if(predecessor.equals(identity)) {
 					//Its us => reply on JOIN
 					answer = new JoinResponseMessage(identity.getNetworkAddress(), join_msg.getOriginatorAddress(),join_msg.getKey(), successor.getNetworkAddress(),successor.getNodeID());
 					
 					//Check if it exists
 					FingerEntry newNode = new FingerEntry(new NodeID(join_msg.getKey().getID()),join_msg.fromIp);
 					//if(finger.contains(newNode)) {
 					if(newNode.equals(identity)) {
 						//Key not allowed msg
 						answer = new DuplicateNodeIdMessage(identity.getNetworkAddress(), join_msg.fromIp,join_msg.getKey());
 					}
 					else {
 						//Change our successor (Only if it's not us!)
 						if(!successor.equals(identity)) finger.remove(successor);
 						finger.add(newNode);
 					}
 				}
 				else {
 					//Forward to successor
 					message.toIp = getSuccessor().getNetworkAddress();
 					answer = message;
 				}
 				
 				//Send
 				communication.sendMessage(answer);
 				break;
 			case Message.JOIN_RESPONSE:
 				JoinResponseMessage jrm = (JoinResponseMessage) message;
 
 				//Ignore JOIN_RESPONSE message if the node is already connected!
 				if(!bConnected) {
 					if(jrm.getJoinKey().equals(identity.getNodeID())) {
 						//Add finger
 						finger.add(new FingerEntry(jrm.getSuccessor(), jrm.getSuccessorAddress()));
 						bConnected = true;
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
 				
 				//TODO Forward broadcast
 				
 				//Process broadcast
 				handleMessage(bcast_msg.extractMessage());
 				break;
 			case Message.KEEPALIVE:
 				//Handle keep-alive message 
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
 	
 	public NodeID getIdentity() {
 		return identity.getNodeID();
 	}
 	
 	public FingerEntry getSuccessor() {
 		FingerEntry successor;
 		
 		//Get successor of us
 		successor = finger.higher(identity);
 		if(successor == null) 
 			successor = finger.first();
 		return successor;
 	}
 	
 	private FingerEntry findPredecessorOf(NodeID nodeID) {
 		FingerEntry precessor;
 		
 		//Find predecessor of a node
 		precessor = finger.lower(new FingerEntry(nodeID,null));
 		if(precessor == null) 
 			precessor = finger.last();
 					
 		return precessor;
 	}
 	
 	private void setIdentity(byte[] hash) {
 		//Set identity
 		identity = new FingerEntry(new NodeID(hash),communication.getLocalIp());
 		
 		//(Re-)initialize finger table
 		//Always add ourselves to the finger table
 		finger = new TreeSet<FingerEntry>();
 		finger.add(identity);
 	}
 }
