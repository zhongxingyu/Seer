 package week4.multicast;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.PriorityBlockingQueue;
 import javax.swing.DefaultListModel;
 import javax.swing.JList;
 import javax.swing.ListModel;
 import multicast.*;
 import week4.LamportClock;
 import week4.multicast.messages.*;
 
 public class ChatQueue extends Thread implements MulticastQueue<Serializable>{
 	/**
 	 * Lamport Clock
 	 */
 	private LamportClock clock;
 	
 	private boolean debug = false;
 	
 	/**
      * The address on which we listen for incoming messages.
      */      
    public InetSocketAddress myAddress;
 
     /**
      * Used to signal that the queue is leaving the peer group. 
      */
     private boolean isLeaving = false;
 	
 	/**
      * Used to signal that no more elements will be added to the queue
      * of pending gets.
      */
     private boolean noMoreGetsWillBeAdded = false;
 	
     /**
      * The thread which handles outgoing traffic.
      */
     private SendingThread sendingThread;
 
     /**
      * The peers who have a connection to us. Used to make sure that
      * we do not close down the receiving end of the queue before all
      * sending to the queue is done. Not strictly needed, but nicer.
      */
    public HashSet<InetSocketAddress> hasConnectionToUs;
 	
 	/**
      * The incoming message queue. All other peers send their messages
      * to this queue.
      */
     private PointToPointQueueReceiverEnd<AbstractLamportMessage> incoming;
 
     /**
      * Keeping track of the outgoing message queues, stored under the
      * corresponding internet address.
      */
     private ConcurrentHashMap<InetSocketAddress,PointToPointQueueSenderEnd<AbstractLamportMessage>> outgoing;
 
     /**
      * Objects pending delivering locally.
      */
    public PriorityQueue<AbstractLamportMessage> pendingGets;
     
     /**
      * Objects pending sending.
      */
     private ConcurrentLinkedQueue<String> pendingSends;
 	
 	private ArrayList<AbstractLamportMessage> backlog;
 	
 	private static int QUEUE_CAP = 10000;
 	
 	private DefaultListModel userlist;
 	
 	/**
 	 * Acknowledgement map
 	 */
 	private ConcurrentHashMap<Double,HashSet<InetSocketAddress>> acknowledgements;
 	
 	public ChatQueue(){
 		clock = new LamportClock();
 		incoming = new PointToPointQueueReceiverEndNonRobust<AbstractLamportMessage>();
 		pendingGets = new PriorityQueue<AbstractLamportMessage>(QUEUE_CAP, new LamportMessageComparator());
 		pendingSends = new ConcurrentLinkedQueue<String>();
 		outgoing = new ConcurrentHashMap<InetSocketAddress,PointToPointQueueSenderEnd<AbstractLamportMessage>>();
 		hasConnectionToUs = new HashSet<InetSocketAddress>();
 		backlog = new ArrayList<AbstractLamportMessage>();
 		acknowledgements = new ConcurrentHashMap<Double,HashSet<InetSocketAddress>>();
 		
 		sendingThread = new SendingThread();
 		sendingThread.start();
 	}
 	
 	public void setUserList(DefaultListModel list){
 		userlist = list;
 	}
 	
 	private void addToUserList(String user){
 		if(userlist != null){
 			userlist.addElement(user);
 		}
 	}
 	
 	private void removeFromUserList(String user){
 		if(userlist != null){
 			if(userlist.contains(user)){
 				userlist.removeElement(user);
 			}
 		}
 	}
 	
 	@Override
 	public void createGroup(int port, DeliveryGuarantee deliveryGuarantee) throws IOException {
 		assert (deliveryGuarantee==DeliveryGuarantee.NONE || deliveryGuarantee==DeliveryGuarantee.TOTAL) : "Can at best implement TOTAL";
 		
 		// Try to listen on the given port. Exception are propagated out.
 		incoming.listenOnPort(port);
 
 		// Record our address
 		InetAddress localhost = InetAddress.getLocalHost();
 		String localHostAddress = localhost.getCanonicalHostName();
 		myAddress = new InetSocketAddress(localHostAddress, port);
 
 		// Buffer a message that we have joined the group.
 		//addAndNotify(pendingGets, new MulticastMessageJoin(myAddress));
 
 		// Start the receiveing thread.
 		this.start();
     }
 
 	@Override
     public void joinGroup(int port, InetSocketAddress knownPeer, DeliveryGuarantee deliveryGuarantee) throws IOException {
         assert (deliveryGuarantee==DeliveryGuarantee.NONE || deliveryGuarantee==DeliveryGuarantee.TOTAL) : "Can at best implement TOTAL";
 
 		// Try to listen on the given port. Exceptions are propagated
 		// out of the method.
 		incoming.listenOnPort(port);
 
         // Record our address.
 		InetAddress localhost = InetAddress.getLocalHost();
 		String localHostAddress = localhost.getCanonicalHostName();
 		
 		myAddress = new InetSocketAddress(localHostAddress, port);
 
 		// Make an outgoing connection to the known peer.
 		PointToPointQueueSenderEnd<AbstractLamportMessage> out = connectToPeerAt(knownPeer);
 		
 		if(out == null)
 			return;
 
 		// Send the known peer our address. 
 		JoinRequestMessage joinRequestMessage = new JoinRequestMessage(myAddress);
 		joinRequestMessage.setClock(clock.tick());
 		out.put(joinRequestMessage);
 		
 		// When the known peer receives the join request it will
 		// connect to us, so let us remember that she has a connection
 		// to us.
 		hasConnectionToUs.add(knownPeer);
 		//debug(myAddress.getPort() + " added " + knownPeer.getPort() + " to connections");
 
 		// Buffer a message that we have joined the group.
 		//addAndNotify(pendingGets, new AbstractLamportMessageJoin(myAddress));
 
 		// Start the receiving thread
 		this.start();
     }
 	
 	@Override
 	public void run(){
 		AbstractLamportMessage msg;
 		
 		/* By contract we know that msg == null only occurs if
 		* incoming is shut down, which we are the only ones that can
 		* do, so we use that as a way to kill the receiving thread
 		* when that is needed. We shut down the incoming queue when
 		* it happens that we are leaving down and all peers notified
 		* us that they shut down their connection to us, at which
 		* point no more message will be added to the incoming
 		* queue.
 		*/
 		while ((msg = incoming.get()) != null) {
 			// Update the lamport clock
 			clock.tick(msg);
 			
 			//debug("(my: "+myAddress.getPort()+" - sender: "+msg.getSender().getPort()+") Got message of type " + msg.getClass().getName() + ":  ("+msg.getClock()+")");
 			
 	
 			if( shouldHandleMessage(msg) ) {
 				addMsgToAcknowledgements(msg);
 
 				// Send acknowledgement
 				AbstractLamportMessage ack = new AcknowledgeMessage(myAddress, msg.getClock());
 				sendToAllExceptMe(ack);
 				//debug("(my: "+myAddress.getPort()+" - sender: " + msg.getSender().getPort() + ") sending ack: ("+clock+") "+msg.hashCode());
 			}
 			
 			if (msg instanceof ChatMessage) {
 				ChatMessage cmsg = (ChatMessage)msg;
 				handle(cmsg);
 			} else if(msg instanceof AcknowledgeMessage){
 				AcknowledgeMessage amsg = (AcknowledgeMessage) msg;
 				handle(amsg);
 			} else if (msg instanceof JoinRequestMessage) {
 				JoinRequestMessage jrmsg = (JoinRequestMessage)msg;
 				handle(jrmsg);
 			} else if (msg instanceof JoinRelayMessage) {
 				JoinRelayMessage jmsg = (JoinRelayMessage)msg;
 				handle(jmsg);
 			} else if (msg instanceof LeaveGroupMessage) {
 				//debug("(my: "+myAddress.getPort()+" - sender: " + msg.getSender().getPort() + ") Got leave group message");
 				LeaveGroupMessage lmsg = (LeaveGroupMessage)msg;
 				handle(lmsg);
 			} else if (msg instanceof WelcomeMessage) {
 				WelcomeMessage wmsg = (WelcomeMessage)msg;
 				handle(wmsg);
 			} else if (msg instanceof GoodbyeMessage) {
 				GoodbyeMessage gmsg = (GoodbyeMessage)msg;
 				handle(gmsg);
 			} else if (msg instanceof BacklogMessage){
 				BacklogMessage bmsg = (BacklogMessage) msg;
 				handle(bmsg);
 			}
 			
 			
 			// Save the backlog without joinRequests
 			if(!(msg instanceof JoinRequestMessage) && !(msg instanceof AcknowledgeMessage)){
 				backlog.add(msg);
 			}
 		}
 		
 		/* Before we terminate we notify callers who are blocked in
 		* out get() method that no more gets will be added to the
 		* buffer pendingGets. This allows them to return with a null
 		* in case no message are in that buffer. */	
 		noMoreGetsWillBeAdded = true;
 		synchronized (pendingGets) {
 			pendingGets.notifyAll();
 		}
 	}
 	
 	public boolean shouldHandleMessage(AbstractLamportMessage msg){
 		return (msg instanceof ChatMessage); //!(msg instanceof LeaveGroupMessage) && !(msg instanceof JoinRelayMessage) && !(msg instanceof AcknowledgeMessage) && !(msg instanceof JoinRequestMessage) && !(msg instanceof BacklogMessage) && !(msg instanceof WelcomeMessage);
 	}
 	
 	private void addMsgToAcknowledgements(AbstractLamportMessage msg){
 		synchronized(acknowledgements){
 			if(acknowledgements.containsKey(msg.getClock())) {
 				return;
 			}
 			
 			// Add current peers to acknowledge map if this is not an AcknowledgeMessage
 			HashSet<InetSocketAddress> ackList = (HashSet<InetSocketAddress>) hasConnectionToUs;
 			debug("Added: " + msg);
 			acknowledgements.put(msg.getClock(),ackList);
 			//debug("Added message (" + msg.getClass().getName() + ") to ack: "+msg.getClock());
 		}
 	}
 	
 	private void handle(BacklogMessage msg){
 		// We have joined the group
 		backlog = msg.getBacklog();
 		//printBacklog();
 	}
 	
 	private void handle(LeaveGroupMessage msg){
 		InetSocketAddress address = msg.getSender();
 		if (!address.equals(myAddress)){
 			//debug("(my: "+myAddress.getPort()+" - sender: " + msg.getSender().getPort() + ") Leaving and DCing");
 			addAndNotify(pendingGets, msg);
 			disconnectFrom(address);
 			removeFromUserList(address.getHostName());
 		}else{
 			// That was my own leave message. If I'm the only one left
 			// in the group, then this means that I can safely shut
 			// down.
 			if (hasConnectionToUs.isEmpty()){
 				incoming.shutdown();
 				//debug("------Shutdown~");
 			}else{
 				//debug(hasConnectionToUs.toString());
 			}
 			
 			//debug(String.format("[You (%s) have left the group]", myAddress.getPort()));
 		}
 	}
 	
 	/**
      * A goodbuy message is produced as response to a leave message
      * and is handled by closing the connection to the existing peer
      * who sent the goodbuy message. After this, SendToAll will not
      * send a copy to the peer who sent us this goodbuy message.
      */
     private void handle(GoodbyeMessage msg) {
 		// When the peer sent us the goodbuy message, it closed its
 		// connection to us, so let us remember that.
 		synchronized(hasConnectionToUs) {
 			hasConnectionToUs.remove(msg.getSender());
 			// If we are leaving and that was the last goodbuy
 			// message, then we can shut down the incoming queue and
 			// terminate the receving thread.
 			if (hasConnectionToUs.isEmpty() && isLeaving) {
 				// If the receiving thread is blocked on the incoming
 				// queue, it will be woken up and receive a null when
 				// the queue is empty, which will tell it that we have
 				// received all messages.
 				incoming.shutdown();
 			}
 		}
     }
 	
 	private void handle(WelcomeMessage msg){
 		// When the sender sent us the wellcome message it connect to
 		// us, so let us remember that she has a connection to us.
 		
 		synchronized(hasConnectionToUs) {
 			hasConnectionToUs.add(msg.getSender());
 			debug(myAddress.getPort()+ " added " + msg.getSender().getPort() + " to connections1");
 		}
 		connectToPeerAt(msg.getSender());
 	}
 	
 	private void handle(JoinRelayMessage msg){
 		if(!msg.getSender().equals(myAddress))
 			addAndNotify(pendingGets, msg);
 		
 		// Connect to the new peer and bid him welcome. 
 		PointToPointQueueSenderEnd<AbstractLamportMessage> out = connectToPeerAt(msg.getAddressOfJoiner());
 		
 		if(out == null)
 			return;
 		
 		WelcomeMessage wmsg = new WelcomeMessage(myAddress);
 		wmsg.setClock(clock.tick());
 		//sendToAllExceptMe(wmsg);
 		out.put(wmsg);
 		// When this peer receives the wellcome message it will
 		// connect to us, so let us remember that she has a connection
 		// to us.
 
 		synchronized(hasConnectionToUs) {
 			hasConnectionToUs.add(msg.getAddressOfJoiner());
 			debug(myAddress.getPort()+ " added " + msg.getAddressOfJoiner().getPort() + " to connections2 from " + msg.getSender().getPort());
 		}
 		addToUserList(msg.getAddressOfJoiner().getHostName());
 	}
 	
 	private void handle(JoinRequestMessage msg){
 		// When the joining peer sent the join request it connected to
 		// us, so let us remember that she has a connection to us. 
 		synchronized(hasConnectionToUs) {
 			hasConnectionToUs.add(msg.getSender()); 
 			debug(myAddress.getPort()+ " added " + msg.getSender().getPort() + " to connections3");
 		}
 		
 		addToUserList(msg.getSender().getHostName());
 		
 		JoinRelayMessage jrmsg = new JoinRelayMessage(myAddress, msg.getSender());
 		jrmsg.setClock(clock.tick());
 		
 		// Buffer a join message so it can be gotten. 
 		addAndNotify(pendingGets, msg);
 		addAndNotify(pendingGets, jrmsg);
 
 		// Then we tell the rest of the group that we have a new member.
 		sendToAllExceptMe(jrmsg);
 		
 		// Then we connect to the new peer. 
 		PointToPointQueueSenderEnd<AbstractLamportMessage> out = connectToPeerAt(msg.getSender());
 		
 		if(out == null)
 			return;
 		
 		BacklogMessage bmsg = new BacklogMessage(myAddress, backlog);
 		bmsg.setClock(clock.tick());
 		out.put(bmsg);
 	}
 	
 	private void handle(ChatMessage msg){
 		addAndNotify(pendingGets, msg);
 	}
 	
 	private void handle(AcknowledgeMessage msg){
 		addAndNotify(acknowledgements,msg);
 	}
 
 	private void printBacklog(){
 		for(AbstractLamportMessage msg : backlog){
 			//if(msg instanceof JoinRequestMessage){
 				// Do nothing
 			//}else if(msg instanceof JoinRelayMessage){
 			//	if(!msg.getSender().equals(myAddress))
 			//		addAndNotify(pendingGets, msg);
 			//}else{
 				addAndNotify(pendingGets, msg);
 			//}
 		}
 	}
 	
 	@Override
 	public void put(Serializable object) {
 		String msg = (String)object;
 		synchronized(pendingSends) {
 			assert (isLeaving==false) : "Cannot put objects after calling leaveGroup()";
 			addAndNotify(pendingSends, msg);
 		}
 	}
 
 	@Override
 	public AbstractLamportMessage get() {
 		// Now an object is ready in pendingObjects, unless we are
 		// shutting down. 
 		synchronized (pendingGets) {
 			//debug(myAddress.getPort()+" Before normal sleep");
 			waitForPendingGetsOrReceivedAll();
 			//debug(myAddress.getPort()+" After normal sleep");
 			if (pendingGets.isEmpty()) {
 				return null;
 				// By contract we signal shutdown by returning null.
 			} else {
 				AbstractLamportMessage msg = pendingGets.peek();
 				if (shouldHandleMessage(msg)) {
 					addMsgToAcknowledgements(msg);
 					AbstractLamportMessage ack = new AcknowledgeMessage(myAddress, msg.getClock());
 					sendToAllExceptMe(ack);
 				}
 				//debug(myAddress.getPort()+" Before sick sleep");
 				waitForAcknowledgementsOrReceivedAll(msg);
 				//debug(myAddress.getPort()+" After sick sleep");
 				// Acknowledgement for this message is now done, so remove the entry in the map
 				//synchronized (acknowledgements) {
 				//	acknowledgements.remove(msg.getClock());
 				//}
 				msg = pendingGets.poll();
 				
 				debug(String.format("polled: %s (%s)", msg, acknowledgements.size()));
 				//debug(myAddress.getPort()+": " + msg);
 				return msg;
 			}
 		}
 	}
 
 	@Override
 	public void leaveGroup() {
 		synchronized (pendingSends) {
 			assert (isLeaving != true): "Already left the group!"; 
 			sendToAll(new LeaveGroupMessage(myAddress));
 			isLeaving = true;
 			
 			// We wake up the sending thread. If pendingSends happen
 			// to be empty now, the sending thread will know that we
 			// are shutting down, so it will not starting waiting on
 			// pendingSends again.
 			pendingSends.notify();
 		}
 		
 		try{
 			while(!outgoing.isEmpty()) Thread.sleep(5);
 		}catch(InterruptedException e){
 			// Interrupted
 		}
 	}
 	
 	
 	/**
      * Will take objects from pendingSends and send them to all peers.
      * If the queue empties and leaveGroup() was called, then the
      * queue will remain empty, so we can terminate.
      */
     private class SendingThread extends Thread {
 		public void run() {	    
 			// As long as we are not leaving or there are objects to
 			// send, we will send them.
 			waitForPendingSendsOrLeaving();
 			String msg;
 			while ((msg = pendingSends.poll()) != null) {
 				AbstractLamportMessage lmsg = new ChatMessage(myAddress, msg);
 				sendToAll(lmsg);
 				waitForPendingSendsOrLeaving();
 			}
 			synchronized (outgoing) {
 				for (InetSocketAddress address : outgoing.keySet()) 
 					disconnectFrom(address);
 			}
 		}
     }
 	
 	/**
      * Used to create an outgoing queue towards the given address,
      * including the addition of that queue to the set of queues.
      *
      * @param address The address of the peer we want to connect
      *        to. Returns null when attempting to make connection to
      *        self.
      */
     private PointToPointQueueSenderEnd<AbstractLamportMessage> connectToPeerAt(InetSocketAddress address) {
 		assert (!address.equals(myAddress)) : "Cannot connect to self.";
 		
 		// Do we have a connection already?
 		PointToPointQueueSenderEnd<AbstractLamportMessage> out = outgoing.get(address);
 		
 		if (outgoing.containsKey(address))
 			return null;
 		
 		assert (out == null) : "Cannot connect twice to same peer!";
 		
 		out = new PointToPointQueueSenderEndNonRobust<AbstractLamportMessage>();
 		out.setReceiver(address);
 		
 		outgoing.put(address, out);
 		
 		return out;
     }
 	
 	private void disconnectFrom(InetSocketAddress address) {
 		synchronized (outgoing) {
 			PointToPointQueueSenderEnd<AbstractLamportMessage> out = outgoing.get(address);
 			if (out != null) {
 				outgoing.remove(address);
 				out.put(new GoodbyeMessage(myAddress));
 				out.shutdown();
 			}
 		}
     }
 	
 	/**
      * Used by the sending thread to wait for objects to enter the
      * collection or us having left the group. When the method
      * returns, then either the collection is non-empty, or the
      * multicast queue was called in leaveGroup();
      */
     private void waitForPendingSendsOrLeaving() {
 		synchronized (pendingSends) {
 			while (pendingSends.isEmpty() && !isLeaving) {
 				try {
 					// We will be woken up if an object arrives or we
 					// are leaving the group. Both might be the case
 					// at the same time.
 					pendingSends.wait();
 				} catch (InterruptedException e) {
 					// Probably leaving. The while condition will
 					// ensure proper behavior in case of some other
 					// interruption.
 				}
 			}
 			// Now: pendingSends is non empty or we are leaving the group.
 		}	
     }
 	
 	/**
      * Will send a copy of the message to all peers who at some point
      * sent us a wellcome message and who did not later send us a
      * goodbuy message, unless we are leaving the peer group.
      */
     private void sendToAll(AbstractLamportMessage msg) {
 		if (isLeaving!=true) {
 			int c = clock.tick();
 			// Set the message clock to the current clock + 1 since the clock will be incremented in sendToAllExceptMe.
 			msg.setClock(c);
 			/* Send to self. */
 			incoming.put(msg);
 			/* Then send to the others. */
 			sendToAllExceptMe(msg, c);
 		}
     }
     private void sendToAllExceptMe(AbstractLamportMessage msg) {
 		if (isLeaving!=true) {			
 			if (!(msg instanceof AcknowledgeMessage))
 				msg.setClock(clock.tick());
 			
 			
 			if (shouldHandleMessage(msg))
 				addMsgToAcknowledgements(msg);
 			
 			// Send messages
 			for (PointToPointQueueSenderEnd<AbstractLamportMessage> out : outgoing.values()){
 				out.put(msg);
 			}
 		}
     }
 	
     private void sendToAllExceptMe(AbstractLamportMessage msg, int c) {
 		if (isLeaving!=true) {			
 			if (!(msg instanceof AcknowledgeMessage))
 				msg.setClock(c);
 			
 			
 			if (shouldHandleMessage(msg))
 				addMsgToAcknowledgements(msg);
 			
 			// Send messages
 			for (PointToPointQueueSenderEnd<AbstractLamportMessage> out : outgoing.values()){
 				out.put(msg);
 			}
 		}
     }
 	
 	
 	/**
 	 * Used by callers to wait for acknowledgements
 	 */
 	private void waitForAcknowledgementsOrReceivedAll(AbstractLamportMessage msg){
 		//debug("Waiting..." + msg.getClass().getName() + "\r\n" + msg);
 		if(!shouldHandleMessage(msg))
 			return;
 			
 		synchronized(acknowledgements){
 			// Clone our HashSet of missing acknowledgements to get intersection with connected peers
 			HashSet<InetSocketAddress> ackClone = new HashSet<InetSocketAddress>();
 			//debug("(my: "+myAddress.getPort()+" - sender: "+msg.getSender().getPort()+") Ack wait (" + msg.getClass().getName() + "): - "+msg.getClock());
 			//debug(msg.toString());
 			if(acknowledgements.contains(msg.getClock())){
 				System.out.println("what");
 				ackClone = (HashSet<InetSocketAddress>)acknowledgements.get(msg.getClock()).clone();
 				ackClone.retainAll(hasConnectionToUs);
 			}
 			while(!noMoreGetsWillBeAdded && !(ackClone.isEmpty())){
 				try {
 					System.out.println("wot");
 					acknowledgements.wait();
 					// Update our clone
 					ackClone = (HashSet<InetSocketAddress>)acknowledgements.get(msg.getClock()).clone();
 					ackClone.retainAll(hasConnectionToUs);
 				}catch(InterruptedException e) {}
 			}
 			
 			acknowledgements.remove(msg.getClock());
 		}
 	}
 	
 	/**
      * Used by callers to wait for objects to enter pendingGets. When
      * the method returns, then either the collection is non-empty, or
      * the multicast queue has seen its own leave message arrive on
      * the incoming stream.
      */
     private void waitForPendingGetsOrReceivedAll() {
 		synchronized (pendingGets) {
 			while (pendingGets.isEmpty() && !noMoreGetsWillBeAdded) {
 				try {
 					// We will be woken up if an object arrives or the
 					// we received all.		     
 					pendingGets.wait();
 				} catch (InterruptedException e) {
 					// Probably shutting down. The while condition
 					// will ensure proper behavior in case of some
 					// other interruption.
 				}
 			}
 			// Now: pendingGets is non empty or we received all there
 			// is to receive.
 		}	
     }
 
     /**
      * Used to add an element to a collection and wake up one thread
      * waiting for elements on the collection.
      */
     protected <T> void addAndNotify(Collection<T> coll, T msg) {
 		synchronized (coll) {
 			coll.add(msg);
 			//debug("Added message of type " + msg.getClass().getName());
 			// Notify that there is a new message. 
 			coll.notify();
 		}
     }
 	
 	/**
 	 * Used to add acknowledgement messages to map.
 	 */
 	protected void addAndNotify(ConcurrentHashMap<Double,HashSet<InetSocketAddress>> map, AcknowledgeMessage msg){
 		//debug(String.format("(my: %s - sender: %s) Adding and notifying acknowledgement - %s (%s)", myAddress.getPort(), msg.getSender().getPort(), msg, msg.getClock()));
 		double key = msg.getClock();
 		InetSocketAddress value = msg.getSender();
 		
 		synchronized(map){
 			if(!map.containsKey(key))
 				return;
 			
 			HashSet<InetSocketAddress> ackList = map.get(key);
 			if(ackList == null)
 				throw new NullPointerException("AckList is null, FGT! "+key);
 			
 			if(ackList.contains(value)){
 				ackList.remove(value);
 				//debug(myAddress.getPort() + ": " + key + " removed: " + value.getPort() + " for message "+ msg);
 			}
 			
 			map.put(key,ackList);
 			
 			map.notify();
 		}
 	}
 	
 	private void debug(String msg) {
 		if(!debug) return;
 		
 		DateFormat format = new SimpleDateFormat("[HH:mm:ss] ");
 		Date date = new Date();
 		
 		System.err.println(format.format(date) + msg);
 	}
 }
