 package org.blockout.network.dht.chord;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.blockout.network.ConnectionManager;
 import org.blockout.network.INodeAddress;
 import org.blockout.network.NodeInfo;
 import org.blockout.network.dht.IDistributedHashTable;
 import org.blockout.network.dht.IHash;
 import org.blockout.network.dht.chord.messages.DHTFirstConnectMsg;
 import org.blockout.network.dht.chord.messages.DHTLookupMsg;
 import org.blockout.network.dht.chord.messages.DHTLookupResponse;
 import org.blockout.network.discovery.DiscoveryListener;
 import org.blockout.network.discovery.NodeDiscovery;
 import org.blockout.network.message.IMessagePassing;
 import org.blockout.network.message.MessageReceiver;
 import org.jboss.netty.channel.Channel;
 
 
 @Named
 public class Chord extends MessageReceiver implements IDistributedHashTable, DiscoveryListener {
 	private IMessagePassing mp;
 	private NodeDiscovery discover;
 	private INodeAddress ownAddress;
 	private ConnectionManager connectionManager;
 
 	@Inject
 	public void setDiscover(NodeDiscovery discover, ConnectionManager mgr) {
 		this.discover = discover;
 		this.connectionManager = mgr;
 	}
 	
 	@Inject
 	public void setMp(IMessagePassing mp) {
 		this.mp = mp;
		this.ownAddress = this.mp.getOwnAddress();
 	}
 
 	@Override
 	public Channel connectTo(IHash nodeId) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public void setUp() {
 		// Get Ready to receive Answers
 		this.mp.addReceiver(this, DHTFirstConnectMsg.class, DHTLookupMsg.class,
 				DHTLookupResponse.class
 				);
 		
 		discover.addDiscoveryListener(this);
 		
 		// Send out message with my own port
		this.discover.sendDiscoveryMessage(ownAddress.getInetAddress());	
 		// Start Listener for Discover Requests
 		this.discover.startDiscoveryServer();
 	}
 
 	@Override
 	public void nodeDiscovered(NodeInfo info) {
 		// This Method is called when a new Node has Broadcasted its arrival
 		this.mp.send(new DHTFirstConnectMsg(), info);
 	}
 	
 	public void receive(DHTFirstConnectMsg msg, INodeAddress origin){
 		// This is the first Contact with the Chord Ring
 		this.mp.send(new DHTLookupMsg(this.ownAddress.getNodeId().getNext()),
 				origin);
 	}
 	
 	public void receive(DHTLookupMsg msg, INodeAddress origin){
 		System.out.println(msg.getHash());
 	}
 
 	public void receive(DHTLookupResponse msg, INodeAddress origin) {
 		System.out.println(msg);
 	}
 
 }
