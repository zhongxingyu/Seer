 package manager;
 
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import manager.dht.Node;
 import manager.dht.NodeID;
 
 
 /**
  * The communication simulates the communication layer between the nodes. To test our implementation of the DHT on its own this class
  * has been built. 
  * @author florianrueter
  *
  */
 public class Communication extends Thread implements CommunicationInterface{
 	private Network network;
 	private Node node = null;
 	private BlockingQueue<Message> queue;
 	private String networkAddress;
 	private Integer delay = 0;
 	
 	public Communication(Network network,String networkAddress) {
 		this.network = network;
 		this.networkAddress = networkAddress;
 		
 		//Create message queue
 		queue = new LinkedBlockingQueue<Message>();
 	}
 	
 	public void start(Node node) {
 		//Start
 		if(node == null) return;
 		this.node = node;
 		this.start();
 	}
 	
 	/**
 	 * Search the right client and trigger the handle event
 	 * @param m message to the client
 	 */
 	public void sendMessage(Message m) {
 		//Get the receiver of the message
 		network.sendMessage(m);
 	}
 	
 	public void handleMessage(Message msg) {
 		//Add message to queue
 		queue.add(msg);
 	}
 	
 	@Override
 	public void run() {
 		Message msg;
 		
 		while(true) {
 			try {
 				//Receive messages and forward them
 				msg = queue.take();
 				//Simulate the time that the message takes over the network
 				Integer totalDelay = Network.msg_delay + delay;
 				if(totalDelay>0) Thread.sleep(totalDelay);
 				node.handleMessage(msg);
 			}
 			catch (InterruptedException e) {
 				//Aborted!! => delete queue and abort thread
 				queue.clear();
 				break;
 			}
 		}
 	}
 	
 	public void setMessageDelay(Integer delay) {
 		this.delay = delay;
 	}
 
 	@Override
 	public String getLocalIp() {
 		return networkAddress;
 	}
 	
 	public String showNodeInfo() {
 		if(node!=null) {
 			return "Node_info{" + networkAddress + "}\t||  NodeID{0x" + node.getIdentity().getNodeID().toString() + "}";
 		} else {
 			return "Node_info{" + networkAddress + "}: Node not started";
 		}
 	}
 	
 	public String getSuccessorAddress() {
		return node.getSuccessor(node.getIdentity().getNodeID()).getNetworkAddress();
 	}
 	
 	public NodeID getNodeID() {
 		return node.getIdentity().getNodeID();
 	}
 }
