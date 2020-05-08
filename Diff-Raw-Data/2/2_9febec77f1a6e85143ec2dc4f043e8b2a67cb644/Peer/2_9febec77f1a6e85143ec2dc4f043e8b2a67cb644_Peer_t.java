 package nl.rug.ds.reliable;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.InetAddress;
 import java.net.MulticastSocket;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.Random;
 import java.util.concurrent.atomic.AtomicInteger;
 
 //TODO what happens in case of an socket exception, close socket etc.
 public class Peer implements Observer {
 
 	static final int MAX_MESSAGE_SIZE = 4069;
 	static final int MAX_PAYLOAD_SIZE = MAX_MESSAGE_SIZE - Message.HEADER_SIZE;
 
 	private static final int id = new Random().nextInt(Integer.MAX_VALUE);
 	private final MulticastSocket socket;
 	private MulticastListener listener;
 	private final AtomicInteger messageCounter = new AtomicInteger(-1);
 	private final int port;
 	private final InetAddress group;
 
 	private HashMap<Integer, Integer> peers = new HashMap<Integer, Integer>();
 	private List<Message> deliveryQueue = new ArrayList<Message>();
 
 	private Peer(InetAddress group, int port, MulticastSocket socket) {
 		this.port = port;
 		this.group = group;
 		this.socket = socket;
 	}
 
 	public static Peer createPeer(InetAddress group, int port) {
 		Peer peer = null;
 		try {
 			MulticastSocket socket = new MulticastSocket(port);
 			socket.setTimeToLive(5);
 			socket.joinGroup(group);
 
 			peer = new Peer(group, port, socket);
 
 			MulticastListener listener = MulticastListener
 					.createListener(socket);
 			peer.setListener(listener);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return peer;
 	}
 
 	MulticastSocket getSocket() {
 		return socket;
 	}
 
 	public void sendMessage(byte[] payload) {
 
 		if (payload.length > MAX_PAYLOAD_SIZE) {
 			throw new RuntimeException("Payload too large");
 		}
 
 		Message outgoing = Message.send(id, messageCounter.incrementAndGet(),
 				payload);
 		sendMessage(outgoing);
 	}
 
 	private void sendMessage(Message outgoing) {
 		byte[] data = outgoing.toByte();
 		DatagramPacket outgoingPacket = new DatagramPacket(data, data.length,
 				group, port);
 
 		try {
 			socket.send(outgoingPacket);
 			if (outgoing.getCommand() == Message.SEND) {
 				deliveryQueue.add(outgoing);
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void receiveMessage(byte[] bytes) {
 		Message m = null;
 		try {
 			m = Message.fromByte(bytes);
 			System.out.println(m);
 			if (id != m.getSource()) {
 
 				switch (m.getCommand()) {
 				case Message.SEND:
 					if (!peers.containsKey(m.getSource())) {
 						System.out.println("I am not alone :)");
						peers.put(m.getSource(),-1);
 					}
 					int r = peers.get(m.getSource());
 					int s = m.getS_piggyback();
 					if (s > r + 1) {
 						System.out.println("I think I missed something");
 						Message miss = Message.miss(id, m.getSource(),
 								messageCounter.get(), r + 1);
 						sendMessage(miss);
 						// put message into hold back queue until I am happy
 						return;
 					} else if (s <= r) {
 						// discard
 						return;
 					} else {
 						peers.put(m.getSource(), ++r);
 						Message ack = Message.ack(id, m.getSource(),
 								messageCounter.get(), s);
 						sendMessage(ack);
 						return;
 
 					}
 					// received a send message
 				case Message.MISS:
 					sendMessage(deliveryQueue.get(m.getR_piggyback()));
 					break;
 				}
 			}
 		} catch (ChecksumFailedException e) {
 			e.printStackTrace();
 			// miss
 		}
 
 		// inform listeners
 	}
 
 	@Override
 	public void update(Observable o, Object arg) {
 		if (o == listener) {
 			if (arg instanceof byte[]) {
 				byte[] bytes = (byte[]) arg;
 				receiveMessage(bytes);
 			}
 		}
 	}
 
 	private void setListener(MulticastListener listener) {
 		if (this.listener != null) {
 			this.listener.deleteObserver(this);
 		}
 		listener.addObserver(this);
 		this.listener = listener;
 	}
 
 }
