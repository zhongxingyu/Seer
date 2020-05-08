 package org.blockout.network.message;
 
 import java.lang.reflect.InvocationTargetException;
 import java.net.InetSocketAddress;
 import java.util.Collection;
 import java.util.Set;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.blockout.network.ConnectionManager;
 import org.blockout.network.INodeAddress;
 import org.blockout.network.NodeInfo;
 import org.blockout.network.dht.IDistributedHashTable;
 import org.blockout.network.dht.IHash;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.MessageEvent;
 
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.Multimap;
 
 @Named
 public class MessageBroker implements IMessagePassing {
 	public InetSocketAddress address;
 	public INodeAddress nodeAddress;
 	protected Multimap<Class<? extends IMessage>, IMessageReceiver> filtredReceivers;
 	
 	private IDistributedHashTable dht;
 	private ConnectionManager connectionManager;
 	
 	public MessageBroker() {
 		this.filtredReceivers = HashMultimap.create();
 	}
 	
 	@Inject
 	public void setDHT(IDistributedHashTable dht) {
 		this.dht = dht;
 	}
 
 	@Override
 	@Inject
 	public void setConnectionManager(ConnectionManager mgr) {
 		this.connectionManager = mgr;
 	}
 
 	public void setUp() {
 		this.address = this.connectionManager.getAddress();
 		this.nodeAddress = new NodeInfo(this.address);
 	}
 
 	public Set<INodeAddress> listNodes() {
 		return this.connectionManager.getAllConnections();
 	}
 	
 	@Override
 	public void send(final IMessage msg, final INodeAddress recipient) {
 		final IMessageEnvelope<IMessage> envelope = new MessageEnvelope<IMessage>(msg, recipient, this.nodeAddress);
 		final Channel chan = this.connectionManager.getConnection(recipient);
 		chan.write(envelope);
 	}
 
 	@Override
 	public void send(IMessage msg, IHash nodeId) {
 		Channel chan = this.connectionManager.getConnection(nodeId);
 		if (chan == null) {
 			this.dht.connectTo(nodeId);
 		}
 		// TODO: Get Address of Node with nodeID
		throw new RuntimeException("Not Implemented");
 	}
 
 	@Override
 	public INodeAddress getOwnAddress() {
 		return this.nodeAddress;
 	}
 
 	// Message Handling
 	@Override
 	public void messageReceived(MessageEvent e) {
 		IMessageEnvelope envelope = (IMessageEnvelope) e.getMessage();
 		INodeAddress actualSender = new NodeInfo(((InetSocketAddress) e
 				.getChannel().getRemoteAddress()).getHostName(), envelope
 				.getSender().getInetAddress().getPort());
 		envelope.setSender(actualSender);
 		this.connectionManager.addConnection(envelope.getSender(),
 				e.getChannel());
 		try {
 			this.notify(envelope.getMessage(), envelope.getSender());
 		} catch (Exception e1) {
 			e1.printStackTrace();
 			this.connectionManager.closeConnection(e.getChannel());
 		}
 	}
 
 	private void notify(IMessage message, INodeAddress sender)
 			throws IllegalArgumentException, SecurityException,
 			IllegalAccessException, InvocationTargetException,
 			NoSuchMethodException {
 		Collection<IMessageReceiver> receiverList = this.getReceiver(message
 				.getClass());
 		for (IMessageReceiver receiver : receiverList) {
 			receiver.receive(message, sender);
 		}
 	}
 
 	// Receiver Handling
 	private Collection<IMessageReceiver> getReceiver(Class<? extends IMessage> filterClass){
 		Collection<IMessageReceiver> currentList;
 		currentList = filtredReceivers.get(filterClass);
 		return currentList;
 	}
 	
 	@Override
 	public void addReceiver(IMessageReceiver receiver, Class<? extends IMessage>... filterClasses) {
 		for(Class<? extends IMessage> clazz: filterClasses){
 			this.getReceiver(clazz).add(receiver);
 		}
 	}
 
 	@Override
 	public void addReceiver(Set<IMessageReceiver> receiver, Class<? extends IMessage>... filterClasses) {
 		for(Class<? extends IMessage> clazz: filterClasses){
 			this.getReceiver(clazz).addAll(receiver);
 		}
 	}
 	
 	@Override
 	public void removeReceiver(IMessageReceiver receiver, Class<? extends IMessage>... filterClasses) {
 		for(Class<? extends IMessage> clazz: filterClasses){
 			this.filtredReceivers.remove(clazz, receiver);
 		}		
 	}
 }
