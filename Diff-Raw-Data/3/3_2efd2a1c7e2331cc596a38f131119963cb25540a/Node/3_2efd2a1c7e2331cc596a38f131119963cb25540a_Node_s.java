 package manager.dht;
 
 import java.util.Random;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.TreeMap;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import manager.CommunicationInterface;
 import manager.LookupServiceInterface;
 import manager.Message;
 import manager.dht.messages.broadcast.BroadcastMessage;
 import manager.dht.messages.broadcast.KeepAliveBroadcastMessage;
 import manager.dht.messages.broadcast.NodeSuspiciousBroadcastMessage;
 import manager.dht.messages.broadcast.NotifyJoinBroadcastMessage;
 import manager.dht.messages.broadcast.NotifyLeaveBroadcastMessage;
 import manager.dht.messages.unicast.DuplicateNodeIdMessage;
 import manager.dht.messages.unicast.FindPredecessorMessage;
 import manager.dht.messages.unicast.FindPredecessorResponseMessage;
 import manager.dht.messages.unicast.JoinAckMessage;
 import manager.dht.messages.unicast.JoinBusyMessage;
 import manager.dht.messages.unicast.JoinFinalizeMessage;
 import manager.dht.messages.unicast.JoinMessage;
 import manager.dht.messages.unicast.JoinResponseMessage;
 import manager.dht.messages.unicast.KeepAliveMessage;
 import manager.dht.messages.unicast.NodeSuspiciousMessage;
 import manager.dht.messages.unicast.NotifyJoinMessage;
 import manager.dht.messages.unicast.NotifyLeaveMessage;
 import manager.listener.FingerChangeListener;
 
 public class Node extends Thread implements LookupServiceInterface {
 	//Communication
 	private CommunicationInterface communication;
 	private String bootstrapAddress;
 
 	//Own state in the DHT
 	private TreeMap<FingerEntry,FingerEntry> finger;
 	private FingerEntry identity;
 	private FingerEntry successor;
 	private FingerEntry predecessor;
 	
 	//Keep alive
 	private static final int CONNECT_PERIOD = 5000;
 	private static final int KEEP_ALIVE_PERIOD = 10000;
 	private static final int KEEP_ALIVE_RANDOM_PERIOD = 10000;
 	private static final int JOIN_BLOCK_PERIOD = 15000;
 	private static final int JOIN_FINALIZE_PERIOD = 15000;
 	
 	private static final int CHECK_PREDECESSOR_SHORT_PERIOD = 5000;
 	private static final int CHECK_PREDECESSOR_LONG_PERIOD = 50000;
 	
 	//Actions
 	private static final int ACTION_CONNECT = 1;
 	private static final int ACTION_SHUTDOWN = 2;
 	private static final int ACTION_KEEP_ALIVE = 3;
 	private static final int ACTION_CHECK_PREDECESSOR = 4;
 	private static final int ACTION_CHECK_SUCCESSOR = 5;
 	
 	//TODO remove
 	private static final int ACTION_KILL = 6;
 	
 	//Action-queue
 	BlockingQueue<Integer> actionQueue;
 	
 	//The timer and his tasks
 	private Timer timer = null;
 	private TimerTask connectTask;
 	private TimerTask keepAlive;
 	private TimerTask blockTask;
 	private TimerTask finalizeTask;
 	private TimerTask findPredecessorTask;
 	
 	//Connection state
 	private boolean connected = false;
 	
 	private FingerEntry blockJoinFor = null;
 	private FingerEntry futureSuccessor = null;
 	private FingerEntry futurePredecessor = null;
 
 	public Node(CommunicationInterface communication,String bootstrapAddress) {
 		this.communication = communication;
 		
 		//Init fingertable
 		finger = new TreeMap<FingerEntry, FingerEntry>();
 		
 		//Generate hash from the local network address
 		//TODO ask stefan if inclusion of port address is reasonable
 		byte[] hash = SHA1Generator.SHA1(communication.getLocalIp());
 		
 		//Init timer
 		this.timer = new Timer();
 
 		//Set identity
 		setIdentity(hash);
 		successor = identity;
 		predecessor = identity;
 		
 		//Save bootstrap address
 		this.bootstrapAddress = bootstrapAddress;
 		
 		//Create blocking queue for action forwarding to worker thread
 		actionQueue = new LinkedBlockingQueue<Integer>(1);
 		
 		//Notify connect
 		notify(ACTION_CONNECT);
 		
 		//start Precessedessor refresh and keepalive
 		findPredecessorTask = startTask(findPredecessorTask, ACTION_CHECK_PREDECESSOR, CHECK_PREDECESSOR_LONG_PERIOD);
 		keepAlive = startTask(keepAlive, ACTION_KEEP_ALIVE, KEEP_ALIVE_PERIOD + new Random().nextInt(KEEP_ALIVE_RANDOM_PERIOD));
 		
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
 	public synchronized void shutdown() {
 		//Schedule immediately
 		notify(ACTION_SHUTDOWN);
 	}
 
 	@Override
 	public void handleMessage(Message message) {
 		//Don't process message if it was not for us!!
 		if(!message.getToIp().equals(identity.getNetworkAddress())) return;
 
 		//Analyze message
 		switch (message.getType()) {
 			case Message.JOIN:
 				if(connected) handleJoinMessage((JoinMessage)message);
 				break;
 			case Message.JOIN_RESPONSE:
 				handleJoinResponseMessage((JoinResponseMessage)message);
 				break;
 			case Message.JOIN_ACK:
 				handleJoinAckMessage((JoinAckMessage)message);
 				break;
 			case Message.JOIN_BUSY:
 				//TODO react on this !?
 				break;
 			case Message.JOIN_FINALIZE:
 				handleJoinFinalizeMessage((JoinFinalizeMessage)message);
 				break;
 			case Message.DUPLICATE_NODE_ID:
 				handleDuplicateNodeIDMessage((DuplicateNodeIdMessage)message);
 				break;
 			case Message.NODE_JOIN_NOTIFY:
 				handleNotifyJoinMessage((NotifyJoinMessage)message);
 				break;
 			case Message.KEEPALIVE:
 				handleKeepAliveMessage((KeepAliveMessage)message);
 				break;
 			case Message.NODE_LEAVE_NOTIFY:
 				handleNotifyLeaveMessage((NotifyLeaveMessage)message);
 				break;
 			case Message.FIND_PREDECESSOR:
 				handleFindPredecessorMessage((FindPredecessorMessage)message);
 				break;
 			case Message.FIND_PREDECESSOR_RESPONSE:
 				handleFindPredecessorResponseMessage((FindPredecessorResponseMessage)message);
 				break;
 			case Message.BROADCAST:
 				BroadcastMessage bcast_msg = (BroadcastMessage)message;
 				
 				//Process broadcast content
 				handleMessage(bcast_msg.extractMessage());
 				
 				//Forward broadcast
 				sendBroadcast(bcast_msg,bcast_msg.getStartKey(),bcast_msg.getEndKey());
 				break;
 			default:
 				//TODO Throw a Exception for a unsupported message?!
 		}
 	}
 	
 	@Override
 	public void run() {
 		int currentAction;
 		
 		//Main loop
 		while(true) {
 			//Get next element from queue
 			try {
 				currentAction = actionQueue.take();
 			}
 			catch (InterruptedException e) {
 				//Thread was interrupted
 				break;
 			}
 			
 			switch(currentAction) {
 				case ACTION_CONNECT:
 					if(!connected) {
 						connect(bootstrapAddress);
 						
 						//Restart task
 						connectTask = startTask(connectTask,ACTION_CONNECT,CONNECT_PERIOD);
 					}
 					break;
 				case ACTION_SHUTDOWN:
 					//TODO copy behind the running loop
 					//Close gently by send a NOTIFY_LEAVE_BROADCAST and than exiting the thread
 					sendBroadcast(new NotifyLeaveBroadcastMessage(null, null, null, null,identity.getNodeID(),successor.getNodeID(),successor.getNetworkAddress()),identity.getNodeID(),identity.getNodeID().sub(1));
 					
 					//Discontinue thread
 					this.interrupt();
 					break;
 				case ACTION_CHECK_PREDECESSOR:
 					//Send FIND_PREDECESSOR message to get a better predecessor
 					synchronized(this) {
 						FingerEntry dst = getPredecessor(null);
 						
 						sendMessage(new FindPredecessorMessage(identity.getNetworkAddress(), dst.getNetworkAddress(),identity.getNodeID(),identity.getNetworkAddress()),dst.getNodeID());
 					}
 					
 					//Reschedule task
 					findPredecessorTask = startTask(findPredecessorTask,ACTION_CHECK_PREDECESSOR,CHECK_PREDECESSOR_SHORT_PERIOD);
 					break;
 				case ACTION_CHECK_SUCCESSOR:
 					break;
 				case ACTION_KEEP_ALIVE:
 					//trigger the keepalive and start period for the next one
 					triggerKeepAliveTimer();
 					keepAlive = startTask(keepAlive, ACTION_KEEP_ALIVE, KEEP_ALIVE_PERIOD + new Random().nextInt(KEEP_ALIVE_RANDOM_PERIOD));
 					//resetKeepAliveTimer();
 					break;
 					
 				case ACTION_KILL:
 					this.interrupt();
 					break;
 			}
 		}
 
 		//Shutdown timer
 		timer.cancel();
 		timer.purge();
 		
 		//TODO here we can send the leave broadcast later
 	}
 	
 	
 	public FingerEntry getIdentity() {
 		return identity;
 	}
 	
 	//TODO private later !
 	public FingerEntry getPredecessor(NodeID nodeID) {
 		FingerEntry hash;
 		FingerEntry result;
 		
 		//null means identity
 		if(nodeID == null) nodeID = identity.getNodeID();
 		hash = new FingerEntry(nodeID,null);
 
 		//Add identity and successor to the finger-table - IMPORTANT: remove them before return
 		synchronized(finger) {
 			finger.put(identity, identity);
 			finger.put(successor, successor);
 			//finger.put(predecessor, predecessor);
 
 			//Find predecessor of a node
 			result = finger.lowerKey(hash);
 			if(result == null) {
 				//There is no lower key in the finger tree
 				result = finger.lastKey();
 			}
 			
 			//Remove identity and successor from the finger-table
 			finger.remove(identity);
 			finger.remove(successor);
 			//finger.remove(predecessor);
 		}
 		
 		//Temporary check
 		//TODO remove later
 		if(nodeID.equals(identity.getNodeID()) && !result.equals(predecessor)) {
 			assert(false);
 		}
 
 		return result;
 	}
 	
 	//TODO private later !
 	public FingerEntry getSuccessor(NodeID nodeID) {
 		FingerEntry hash;
 		FingerEntry result;
 
 		//null means identity
 		if(nodeID == null) nodeID = identity.getNodeID();
 		hash = new FingerEntry(nodeID,null);
 
 		//Our successor
 		//if(nodeID.equals(identity)) return successor;
 		
 		synchronized(finger) {
 			//Add identity and successor to the finger-table - IMPORTANT: remove them before return
 			finger.put(identity, identity);
 			finger.put(successor, successor);
 			//finger.put(predecessor, predecessor);
 
 			//Get successor of us
 			result = finger.higherKey(hash);
 			if(result == null) { 
 				//There is no higher key in the finger tree
 				result = finger.firstKey();
 			}
 			
 			//Remove identity and successor from the finger-table
 			finger.remove(identity);
 			finger.remove(successor);
 			//finger.remove(predecessor);
 		}
 		
 		//Temporary check
 		//TODO remove later
 		if(nodeID.equals(identity.getNodeID()) && !result.equals(successor)) {
 			assert(false);
 		}
 		
 		return result;
 	}
 	
 	private void setIdentity(byte[] hash) {
 		//Set identity
 		identity = new FingerEntry(new NodeID(hash),communication.getLocalIp());
 		
 		//(Re-)initialize finger table
 		//Always add ourselves to the finger table
 		//finger = new TreeMap<FingerEntry,FingerEntry>();
 		//finger.put(identity,identity);
 	}
 	
 	//Check if new node can be inserted into finger table
 	public void updateFingerTableEntry(FingerEntry newFinger) {
 		FingerEntry suc;
 		NodeID hash_finger;
 		NodeID hash_suc;
 		NodeID hash_log2;
 		int log2floor;
 		
 		if(newFinger.getNetworkAddress().equals("") && identity.getNetworkAddress().equals("4")) {
 			System.out.println("break");
 		}
 		
 		//Check for dont's
 		synchronized(finger) {
 			if(newFinger.equals(identity)) return;
 			if(newFinger.equals(successor)) return;
 			if(finger.containsKey(newFinger)) return;
 		}
 		
 		//1 - Rotate hash to the "origin"
 		//2 - Then get the logarithm of base 2, rounded down (floor)
 		//3 - Calculate the new hash
 		
 		hash_finger = newFinger.getNodeID().sub(identity.getNodeID());
 		log2floor = NodeID.logTwoFloor(hash_finger);
 		hash_log2 = NodeID.powerOfTwo(log2floor);
 
 		//Get previous successor - Shift to original position first
 		synchronized(finger) {
 			suc = getSuccessor(hash_log2.add(identity.getNodeID()));
 		}
 		hash_suc = suc.getNodeID().sub(identity.getNodeID());
 		
 		if(suc.equals(identity)) {
 			//In this case, there is no successor => just add the new finger
 			synchronized(finger) {
 				finger.put(newFinger,newFinger);
 			}
 			
 			//Fire event
 			fireFingerChangeEvent(FingerChangeListener.FINGER_CHANGE_ADD, identity, newFinger);
 		}
 		//Check if the new finger is smaller than the successor
 		else if(hash_finger.compareTo(hash_suc) < 0) {
 			//Also add the new node in this case...
 			synchronized(finger) {
 				finger.put(newFinger,newFinger);
 			}
 
 			//...but also check if the successor was the old successor
 			//and, if so, remove it
 			//Old successor means, that it is between [log2floor,log2floor + 1)
 			if(log2floor == ((NodeID.ADDRESS_SIZE * 8) - 1) || hash_suc.compareTo(NodeID.powerOfTwo(log2floor + 1)) < 0) {
 				synchronized(finger) {
 					finger.remove(suc);
 				}
 				
 				//Fire events
 				fireFingerChangeEvent(FingerChangeListener.FINGER_CHANGE_REMOVE_WORSE, identity, suc);
 				fireFingerChangeEvent(FingerChangeListener.FINGER_CHANGE_ADD_BETTER, identity, newFinger);
 			}
 			else {
 				//Only fire ADD event, because nothing was removed in change
 				fireFingerChangeEvent(FingerChangeListener.FINGER_CHANGE_ADD, identity, newFinger);
 			}
 		}
 	}
 	
 	public void removeFingerTableEntry(FingerEntry remove,FingerEntry suc) {
 		//TODO create this :-)
 	}
 
 	private void sendMessage(Message message,NodeID dstNode) {
 		//Don't send to ourselves
 		if(identity.getNodeID().equals(dstNode)) {
 			return;
 		}
 		
 		try {
 			communication.sendMessage(message);
 		} catch(DestinationNotReachableException e) {
 			//Handle failing node if we now him
 			if(dstNode != null) {
 				//handle the failing node
 				handleFailingNode(dstNode);
 				
 				//inform the network
 				sendBroadcast(new NodeSuspiciousBroadcastMessage(null, null, null, null, dstNode), identity.getNodeID(), identity.getNodeID().sub(1));
 			}
 		}
 	}
 	
 	private void sendBroadcast(BroadcastMessage bcast_msg, NodeID startKey,NodeID endKey) {
 		FingerEntry suc,next;
 		String from,to;
 		NodeID newStartKey,newEndKey;
 		BroadcastMessage new_bcast_msg;
 		
 		//Don't do...
 		if(!connected) {
 			System.out.println("NOT CONNECTED " + identity.getNetworkAddress());
 			return;
 		}
 		
 		//Prepare packet
 		from = identity.getNetworkAddress();
 
 		//Get first successor
 		suc = getSuccessor(identity.getNodeID());
 		if(suc.equals(identity)) return;
 		next = getSuccessor(suc.getNodeID());
 
 		//For each finger
 		do {
 			//Check and set range
 			if(suc.getNodeID().between(startKey,endKey)) {
 				newStartKey = suc.getNodeID();
 				
 				//Set endKey to next NodeID or the end of the range, whatever is smallest
 				if(next.getNodeID().between(startKey,endKey)) {
 					newEndKey = next.getNodeID().sub(1);
 				}
 				else {
 					newEndKey = endKey;
 				}
 				
 				//Send message
 				to = suc.getNetworkAddress();
 				new_bcast_msg = bcast_msg.cloneWithNewAddresses(from, to,newStartKey,newEndKey);
 				
 				sendMessage(new_bcast_msg,suc.getNodeID());
 				
 			}
 
 			//Move to next range
 			suc = next;
 			next = getSuccessor(suc.getNodeID());
 		} while(!suc.equals(identity));
 	}
 	
 	//TODO for DEBUG
 	private void fireFingerChangeEvent(int eventType,FingerEntry node,FingerEntry finger) {
 		communication.fireFingerChangeEvent(eventType,node,finger);
 	}
 	
 	//TODO for DEBUG
 	//Remove later
 	public TreeMap<FingerEntry,FingerEntry> getFingerTable() {
 		TreeMap<FingerEntry,FingerEntry> newMap;
 		
 		//We need to clone the map and synchronize this operation!
 		synchronized(finger) {
 			newMap = new TreeMap<FingerEntry,FingerEntry>(finger);
 			newMap.put(successor, successor);
 			newMap.put(identity,identity);
 //			if(predecessor != null) newMap.put(predecessor,predecessor);
 		}
 		
 		return newMap;
 	}
 	
 	private void triggerKeepAliveTimer() {
 		KeepAliveBroadcastMessage msg;
 		
 		//Fire event at network layer
 		//TODO remove! only for debugging
 		communication.fireKeepAliveEvent(identity.getNodeID(),identity.getNetworkAddress());
 		
 		//Send broadcast
 		msg = new KeepAliveBroadcastMessage(null,null,null,null,identity.getNodeID(),identity.getNetworkAddress());
 		sendBroadcast(msg, identity.getNodeID(),identity.getNodeID().sub(1));
 		
 		//Reset time
 		//resetKeepAliveTimer();
 	}
 
 	/*private void resetKeepAliveTimer() {
 		int time = KEEP_ALIVE_PERIOD + new Random().nextInt(KEEP_ALIVE_RANDOM_PERIOD);
 		
 		//Cancel and reschedule timer
 		//TODO create a new Timer is not good!!!!!!!!! It every time creates a new Thread! One Timer thread should be enough!
 		if(keepAlive != null) keepAlive.cancel();
 		keepAlive = new TimerTask() {
 
 			@Override
 			public void run() {
 				//Trigger keep alive
 				//triggerKeepAliveTimer();
 			}
 		};
 		timer.schedule(keepAlive, time);
 	}*/
 	
 	//TODO remove debug function
 	public boolean getStateConnected() {
 		return connected;
 	}
 
 	//TODO remove debug function
 	public FingerEntry getStateBlockJoinFor() {
 		return blockJoinFor;
 	}
 	
 	private void triggerUnblockJoinBlock() {
 		//Unblock node if new successor did not answer
 		synchronized(this) {
 			if(blockJoinFor != null) {
 				blockJoinFor = null;
 			}
 		}
 	}
 	
 	private void triggerFinalizeTimeout() {
 		//The finalize timed out => Try to reconnect
 		synchronized(this) {
 			futurePredecessor = null;
 			futureSuccessor = null;
 			connected = false;
 		}
 		
 		//start to reconnect
 		//TODO think about a senseful address to reconnect
 		notify(ACTION_CONNECT);
 	}
 
 	synchronized private FingerEntry updatePredecessor(FingerEntry newFinger) {
 		//Nothing to do
 		if(newFinger.equals(identity) || newFinger.equals(predecessor)) return null;
 		
 		//Check first if the new one really is a better  predecessor
 		if(predecessor == null || newFinger.getNodeID().sub(identity.getNodeID()).compareTo(predecessor.getNodeID().sub(identity.getNodeID())) > 0) {
 			FingerEntry oldPredecessor = predecessor;
 
 			//It is a better predecessor
 			predecessor = newFinger;
 						
 			//Fire fingerChange event
 			if(oldPredecessor == null || oldPredecessor.equals(identity)) {
 				fireFingerChangeEvent(FingerChangeListener.FINGER_CHANGE_ADD,identity, predecessor);
 			}
 			else {
 				fireFingerChangeEvent(FingerChangeListener.FINGER_CHANGE_REMOVE_WORSE,identity, oldPredecessor);
 				fireFingerChangeEvent(FingerChangeListener.FINGER_CHANGE_ADD_BETTER,identity, predecessor);
 			}
 			
 			//Check if it might be a finger
 			//updateFingerTableEntry(oldPredecessor);
 			//return the old predecessor, maybe we can still use it
 			return oldPredecessor;
 		} 
 		else {
 			//we did not update anything
 			return null;
 		}
 	}
 	
 	private void connect(String address) {
 		synchronized(this) {
 			//If the futureSuccessor (or predecessor) is null, then we are not
 			//currently in the process of connection
 			if(!connected && (futureSuccessor == null || futurePredecessor == null)) {
 				
 				//Reset node!!
 				this.successor = identity;
 				this.predecessor = identity;
 				finger.clear();
 				
 				//No address means, WE are the beginning of the DHT
 				//If we are a bootstrapping node, that means bootstrapping address is null or is our address,
 				//we are always connected !!
 				if(address == null || address.equals(communication.getLocalIp())) {
 					//We are connected and we are our own successor
 					connected = true;
 					
 					//Cancel timer-task
 					if(connectTask != null) connectTask.cancel();
 				}
 				else {			
 					//Try to connect to DHT
 					sendMessage(new JoinMessage(identity.getNetworkAddress(),address,identity.getNetworkAddress(),identity.getNodeID()),null);
 				}
 			}
 		}
 	}
 	
 	private void handleJoinMessage(JoinMessage join_msg) {
 		Message answer = null;
 
 		FingerEntry predecessorOfJoiningNode = getPredecessor(join_msg.getKey());
 		
 		//Forward or answer?
 		if(predecessorOfJoiningNode.equals(identity)) {
 			//It's us => reply on JOIN
 
 			//Check if it exists
 			FingerEntry newFingerEntry = new FingerEntry(join_msg.getKey(),join_msg.getOriginatorAddress());
 			FingerEntry tempFinger = null;
 			
 			//Do we know the finger already? (As successor or finger entry)
 			synchronized(finger) {
 				if(successor.equals(newFingerEntry)) {
 					tempFinger = successor;
 				}
 				else {
 					tempFinger = finger.get(newFingerEntry);
 				}
 			}
 			
 			//If another node tried to enter the DHT with the same key, send duplicate message
 			if(tempFinger != null) {
 				//Skip, if the same node tried again!
 				if(!tempFinger.getNetworkAddress().equals(newFingerEntry.getNetworkAddress())) {
 					//Key not allowed message
 					answer = new DuplicateNodeIdMessage(identity.getNetworkAddress(), join_msg.getFromIp(),join_msg.getKey());
 				}
 			}
 			else {
 				synchronized(this) {
 					if(blockJoinFor != null) {
 						//Send busy message
 						answer = new JoinBusyMessage(identity.getNetworkAddress(),join_msg.getOriginatorAddress());
 					}
 					else {
 						//Prepare answer
 						answer = new JoinResponseMessage(identity.getNetworkAddress(), join_msg.getOriginatorAddress(),join_msg.getKey(), successor.getNetworkAddress(),successor.getNodeID(),identity.getNodeID());
 						blockJoinFor = newFingerEntry;
 						
 						//Start timer for node unblocking
 						timer.schedule(blockTask = new TimerTask() {
 
 							@Override
 							public void run() {
 								triggerUnblockJoinBlock();
 							}
 							
 						}, JOIN_BLOCK_PERIOD);
 					}
 				}
 				
 				//Send
 				sendMessage(answer,join_msg.getKey());
 			}
 		}
 		else {
 			//Forward to the best fitting predecessor
 			FingerEntry dst = getPredecessor(join_msg.getKey());
 			answer = new JoinMessage(identity.getNetworkAddress(),dst.getNetworkAddress(), join_msg.getOriginatorAddress(), join_msg.getKey());
 			sendMessage(answer,dst.getNodeID());
 		}
 	}
 	
 	private void handleJoinResponseMessage(JoinResponseMessage jrm) {
 		//Ignore JOIN_RESPONSE message if the node is already connected!
 		synchronized(this) {
 			if(!connected) {
 				//Check if the join id is really us!
 				if(jrm.getJoinKey().equals(identity.getNodeID())) {
 					//Add finger
 					futureSuccessor = new FingerEntry(jrm.getSuccessor(), jrm.getSuccessorAddress());
 					futurePredecessor = new FingerEntry(jrm.getPredecessor(),jrm.getFromIp());
 					
 					//Inform the node that we got the message
 					sendMessage(new JoinAckMessage(identity.getNetworkAddress(), futurePredecessor.getNetworkAddress(), identity.getNodeID()),futurePredecessor.getNodeID());
 					
 					//Start finalizeTask timer
 					timer.schedule(finalizeTask = new TimerTask() {
 
 						@Override
 						public void run() {
 							triggerFinalizeTimeout();
 						}
 						
 					}, JOIN_FINALIZE_PERIOD);
 					
 					//Check
 					//updateFingerTableEntry(new FingerEntry(jrm.getPredecessor(),jrm.getFromIp()));
 					
 					//Create finger table the first time
 					//buildFingerTable();
 					//checkFingerTable();
 				}
 				else {
 					//Ignore this because the key does not match!!!
 					//TODO react on this
 				}
 			}
 		}
 	}
 	
 	private void handleJoinAckMessage(JoinAckMessage jam) {
 		//Skip if not blocked or it is a faked message
 		if(blockJoinFor != null && jam.getJoinKey().equals(blockJoinFor.getNodeID())) {
 			//Set successor to new node and update finger-table with old successor
 			
 			//Cancel block timer first!!
 			if(blockTask != null)  {
 				blockTask.cancel();
 			}
 			
 			//Notify everybody of the new node
 			//Do it before the new node is integrated in our structure, so we dont rely on
 			//its capability to forward the broadcast
 			sendBroadcast(new NotifyJoinBroadcastMessage(null,null,null,null,blockJoinFor.getNetworkAddress(),blockJoinFor.getNodeID()),identity.getNodeID(),identity.getNodeID().sub(1));
 			
 			//Update successor and predecessor
 			FingerEntry oldSuccessor = updateSuccessor(blockJoinFor);
 			FingerEntry oldPredecessor = updatePredecessor(successor);
 			
 			//check if the old ones are good as fingers
 			if(oldSuccessor != null) updateFingerTableEntry(oldSuccessor);
 			if(oldPredecessor != null) updateFingerTableEntry(oldPredecessor);
 			
 			//Notify the new node, that it is connected
 			sendMessage(new JoinFinalizeMessage(identity.getNetworkAddress(), jam.getFromIp(), jam.getJoinKey()),jam.getJoinKey());
 
 			//unblock
 			synchronized(this) {
 				blockJoinFor = null;
 			}
 		}
 	}
 	
 	private void handleJoinFinalizeMessage(JoinFinalizeMessage jfm) {
 		synchronized(this) {
 			//Only if we are not connected
 			if(connected || futurePredecessor == null || futureSuccessor == null) return;
 			
 			//First, cancel timeout-timer
 			finalizeTask.cancel();
 			
 			//If the joinKey equals our hash
 			if(jfm.getJoinKey().equals(identity.getNodeID())) {
 				synchronized (finger) {
 					successor = futureSuccessor;
 					predecessor = futurePredecessor;
 					futurePredecessor = null;
 					futureSuccessor = null;
 				}
 			
 				//TODO remove
 				fireFingerChangeEvent(FingerChangeListener.FINGER_CHANGE_ADD, identity, predecessor);						
 				fireFingerChangeEvent(FingerChangeListener.FINGER_CHANGE_ADD, identity, successor);						
 				
 				connected = true;
 			}
 		}
 	}
 	
 	private void handleDuplicateNodeIDMessage(DuplicateNodeIdMessage dupMsg) {
 		if(dupMsg.getDuplicateKey().equals(identity.getNodeID())) {
 			//it is okay, a join that I send before has reached my predecessor
 		} else {
 			//If the node is not connected allow the change of the identity
 			//Check the duplicate id also
 			if(!connected && dupMsg.getDuplicateKey().equals(identity.getNodeID())) {
 				//TODO what shall we do here?????
 				assert(false);
 			}
 		}
 	}
 	
 	private void handleNotifyJoinMessage(NotifyJoinMessage njm) {
 		FingerEntry newFinger;
 		
 		//Check if this node can use the newly added node
 		//for the finger table
 		newFinger = new FingerEntry(njm.getHash(),njm.getNetworkAddress());
 		updateFingerTableEntry(newFinger);
 		FingerEntry oldPredecessor = updatePredecessor(newFinger);
 		
 		//if the predecessor was changed check the old one for the finger table
 		if(oldPredecessor != null) updateFingerTableEntry(oldPredecessor);
 		
 		//Send advertisement if we probably are a finger of the joining node
 		int log2_pre = NodeID.logTwoFloor(predecessor.getNodeID().sub(newFinger.getNodeID()));
 		int log2_this = NodeID.logTwoFloor(identity.getNodeID().sub(newFinger.getNodeID()));
 		
 		if(log2_pre < log2_this) {
 			//Send advertisement
 			sendMessage(new KeepAliveMessage(identity.getNetworkAddress(),newFinger.getNetworkAddress(),identity.getNodeID(),identity.getNetworkAddress()),newFinger.getNodeID());
 		}
 	}
 	
 	private void handleKeepAliveMessage(KeepAliveMessage kam) {
 		FingerEntry advertisedFinger;
 		
 		//Reset timer
 		//resetKeepAliveTimer();
 		keepAlive = startTask(keepAlive, ACTION_KEEP_ALIVE, KEEP_ALIVE_PERIOD + new Random().nextInt(KEEP_ALIVE_RANDOM_PERIOD));
 
 		//Handle keep-alive message
 		advertisedFinger = new FingerEntry(kam.getAdvertisedID(),kam.getAdvertisedNetworkAddress());
 		updateFingerTableEntry(advertisedFinger);
 		FingerEntry oldPredecessor = updatePredecessor(advertisedFinger);
 		
 		//if the predecessor was changed check the old one for the finger table
 		if(oldPredecessor != null) updateFingerTableEntry(oldPredecessor);
 	}
 	
 	private void handleNotifyLeaveMessage(NotifyLeaveMessage nlm) {
 		FingerEntry leavingNodeSuccessor = new FingerEntry(nlm.getSuccessorHash(),nlm.getSuccessorNetworkAddress());
 		
 		//Check if our successor is leaving
 		if(nlm.getHash().equals(successor.getNodeID()))  {
 			//Change the successor to the next one
 			updateSuccessor(leavingNodeSuccessor);
 		}
 		//If predecessor
 		else if (predecessor != null && nlm.getHash().equals(predecessor.getNodeID())) {
 			synchronized(this) {
 				predecessor = null;
 				notify(ACTION_CHECK_PREDECESSOR);
 			}
 		}
 		else { 
 			//Remove leaving finger
 			FingerEntry removedFinger = finger.remove(new FingerEntry(nlm.getHash(),null));
 			
 			if(removedFinger != null) {
 				//TODO remove debug
 				fireFingerChangeEvent(FingerChangeListener.FINGER_CHANGE_REMOVE, identity, removedFinger);
 			}
 			
 			//Check if the successor of the leaving node's successor is a possible finger for us
 			updateFingerTableEntry(leavingNodeSuccessor);
 		}
 		
 	}
 	
 	private void handleNodeSuspiciousMessage(NodeSuspiciousMessage nsm) {
 		handleFailingNode(nsm.getHash());
 	}
 	
 	private synchronized FingerEntry updateSuccessor(FingerEntry newSuccessor) {
 		//TODO first check for don'ts?????
 		
 		FingerEntry oldSuccessor;
 		
 		//It should not be in the finger table
 		finger.remove(newSuccessor);
 
 		//Take the new successor
 		oldSuccessor = successor;
 		successor = newSuccessor;
 
 		//TODO remove debug stuff
 		//Fire events
 		if(!oldSuccessor.equals(identity)) {
 			fireFingerChangeEvent(FingerChangeListener.FINGER_CHANGE_REMOVE_WORSE, identity, oldSuccessor);
 			fireFingerChangeEvent(FingerChangeListener.FINGER_CHANGE_ADD_BETTER, identity, newSuccessor);
 		}
 		else {
 			fireFingerChangeEvent(FingerChangeListener.FINGER_CHANGE_ADD, identity, successor);
 		}
 		
 		//Update finger table
 		//updateFingerTableEntry(oldSuccessor);
 		//return the old Successor, maybe we can still use it
 		return oldSuccessor;
 	}
 
 	private class NotifyTask extends TimerTask {
 		
 		private int action;
 		private Node notifyObj;
 		
 		public NotifyTask(int action,Node notifyObj) {
 			this.action = action;
 			this.notifyObj = notifyObj;
 		}
 		
 		@Override
 		public void run() {
 			notifyObj.notify(action);
 		}
 	}
 	
 	private void notify(int action) {
 		//Queue current action
 		try {
 			actionQueue.put(action);
 		}
 		catch (InterruptedException e) {
 			//TODO ?!?!
 		}
 	}
 	
 	public void killMe() {
 		notify(ACTION_KILL);
 	}
 	
 	private TimerTask startTask(TimerTask timerTask,int action,int period) {
 		//Stop if appropriate
 		if(timerTask != null) {
 			timerTask.cancel();
 		}
 		
 		//Schedule new event
 		timerTask = new NotifyTask(action,this);
 		timer.schedule(timerTask, period);
 		
 		//Return new task
 		return timerTask;
 	}
 	
 	private void handleFindPredecessorMessage(FindPredecessorMessage fpm) {
 		FingerEntry findFinger;
 		
 		//Get responsible finger
 		findFinger = getPredecessor(fpm.getHash());
 		
 		if(findFinger.equals(identity)) {
 			//Its us => reply
 			sendMessage(new FindPredecessorResponseMessage(identity.getNetworkAddress(),fpm.getOrigAddress(),identity.getNodeID(),fpm.getHash()),null);
 		}
 		else {
 			//Forward to the best finger
 			sendMessage(new FindPredecessorMessage(identity.getNetworkAddress(),findFinger.getNetworkAddress(),fpm.getHash(),fpm.getOrigAddress()),findFinger.getNodeID());
 		}
 	}
 
 	private void handleFindPredecessorResponseMessage(FindPredecessorResponseMessage fprm) {
 		//Check if hash fits; ignore otherwise
 		if(identity.getNodeID().equals(fprm.getOrigHash())) {
 			//Update predecessor
 			updatePredecessor((new FingerEntry(fprm.getPredecessorHash(),fprm.getFromIp())));
 			
 			//Restart task
 			findPredecessorTask = startTask(findPredecessorTask,ACTION_CHECK_PREDECESSOR,CHECK_PREDECESSOR_LONG_PERIOD);
 		}
 	}
 	
 	private synchronized void handleFailingNode(NodeID dst) {
 		if(successor.getNodeID().equals(dst)) {
 			//Successor failed
 			successor = getSuccessor(successor.getNodeID());
 		}
 		else if(predecessor != null && predecessor.getNodeID().equals(dst)) {
 			//Predecessor failed
 			predecessor = null;
 			
 			//Start recovery task
 			findPredecessorTask = startTask(findPredecessorTask,ACTION_CHECK_PREDECESSOR,CHECK_PREDECESSOR_SHORT_PERIOD);
 		}
 		else if(finger.containsKey(new FingerEntry(dst, null))) {
 			//Finger failed
 			FingerEntry removedFinger = finger.remove(new FingerEntry(dst, null));
 			fireFingerChangeEvent(FingerChangeListener.FINGER_CHANGE_REMOVE,identity, removedFinger);
 		}
 	}
 }
