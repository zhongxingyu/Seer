 package nl.rug.peerbox.middleware;
 
 import java.net.DatagramPacket;
 import java.util.Queue;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import org.apache.log4j.Logger;
 
 final class Receiver {
 
 	private final static Logger logger = Logger.getLogger(Receiver.class);
 
 	private final Queue<Message> holdbackQueue = new ConcurrentLinkedQueue<Message>();
 	private final BlockingQueue<DatagramPacket> receivedDataQueue = new ArrayBlockingQueue<DatagramPacket>(
 			1024);
 
 	private RMulticastGroup group;
 
 	public Receiver(RMulticastGroup group) {
 		this.group = group;
 	}
 
 	private volatile boolean alive = true;
 	private Thread thread;
 
 	void start() {
 		thread = new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 				while (alive) {
 					try {
 						DatagramPacket data = receivedDataQueue.take();
 						processMessage(data.getData());
 					} catch (InterruptedException e) {
 						logger.debug("Receiver interrupted");
 					}
 				}
 				logger.debug("Receiver stopped");
 			}
 
 		});
 		thread.setDaemon(true);
 		thread.setName("Receiver");
 		thread.start();
 	}
 
 	void shutdown() {
 		logger.debug("Stop Receiver Thread");
 		alive = false;
 		thread.interrupt();
 	}
 
 	void pushDataPacket(DatagramPacket dp) {
 		if (!receivedDataQueue.offer(dp)) {
 			logger.equals("Incoming Blockingqueue is full");
 		}
 	}
 
 	private void processMessage(byte[] bytes) {
 		Message m = null;
 		try {
 			m = Message.fromByte(bytes);
 			receiveMessage(m);
 		} catch (ChecksumFailedException e) {
 			logger.warn("Checksum failed");
 		}
 	}
 
 	private void receiveMessage(Message m) {
 
 		switch (m.getCommand()) {
 		case Message.MESSAGE:
 
 			if (m.getSource() == group.getId())
 				return;
 
 			Peer p = null;
 			if (!group.getPeers().containsKey(m.getSource())) {
 				logger.debug("Detected group " + m.getSource()
 						+ " with piggyback " + (m.getMessageID() - 1));
 				p = new Peer();
 				p.setHostID(m.getSource());
 				p.setSeenMessageID(m.getMessageID() - 1);
 				p.setReceivedMessageID(m.getMessageID() - 1);
 				group.getPeers().put(m.getSource(), p);
 			} else {
 				p = group.getPeers().get(m.getSource());
 			}
 
 			int r = p.getReceivedMessageID();
 			int s = m.getMessageID();
 			if (s > p.getSeenMessageID()) {
 				p.setSeenMessageID(s);
 			}
 
 			if (s == r + 1) {
 				logger.debug("Received: " + m.toString());
 				p.setReceivedMessageID(++r);
 				sendAck(m);
 				
 				group.rdeliver(m);
 
 				Message stored = findMessageInHoldbackQueue(p.getHostID(),
 						s + 1);
 				if (stored != null) {
 					holdbackQueue.remove(stored);
 					receiveMessage(stored);
 				}
 
 			} else if (s > r + 1) {
 				logger.debug("Received: " + m.toString());
 				logger.debug("Missed message " + (r + 1)
 						+ " detected from group " + m.getSource());
 				holdbackQueue.add(m);
 				for (int missedID = r + 1; missedID < p.getSeenMessageID(); missedID++) {
 					if (findMessageInHoldbackQueue(p.getHostID(), missedID) == null) {
 						sendMiss(m.getSource(), missedID);
 					}
 				}
 			} else if (s <= r) {
 				logger.debug("Discarded duplicate: " + m.toString());
 			}
 			break;
 
 		case Message.NACK:
 			if (m.getSource() != group.getId())
 				return;
 			logger.debug(m.toString());
 			group.getSender().resendMessage(m.getMessageID());
 		}
 	}
 
 	private void sendAck(Message m) {
 		Message ack = Message.ack(m.getSource(), m.getMessageID(), group.getId());
 		group.sendMessage(ack);
 	}
 
 	void sendMiss(int peer, int message_id) {
		Message miss = Message.nack(peer, message_id);
 		group.sendMessage(miss);
 	}
 	
 	private Message findMessageInHoldbackQueue(int host, int messageID) {
 		for (Message m : holdbackQueue) {
 			if (m.getSource() == host && m.getMessageID() == messageID) {
 				return m;
 			}
 		}
 		return null;
 	}
 }
