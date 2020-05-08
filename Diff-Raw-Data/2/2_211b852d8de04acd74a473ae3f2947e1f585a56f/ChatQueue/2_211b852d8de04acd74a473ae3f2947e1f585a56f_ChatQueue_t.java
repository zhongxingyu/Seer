 package week4.multicast;
 
 
 import week4.multicast.*;
 import java.io.IOException;
 import java.io.Serializable;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Random;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import multicast.*;
 import week4.multicast.messages.*;
 import week4.multicast.messages.AbstractLamportMessage;
 
 
 public class ChatQueue extends Thread implements MulticastQueue<Serializable>{
 	/**
 	 * Lamport Clock
 	 */
 	private int clock;
 	
 	/**
      * The address on which we listen for incoming messages.
      */      
     private InetSocketAddress myAddress;
 
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
     private HashSet<InetSocketAddress> hasConnectionToUs;
 	
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
     private ConcurrentLinkedQueue<AbstractLamportMessage> pendingGets;
     
     /**
      * Objects pending sending.
      */
     private ConcurrentLinkedQueue<String> pendingSends;
 	
 	private ArrayList<AbstractLamportMessage> backlog;
 	
 	public ChatQueue(){
 		clock = 0;
 		incoming = new PointToPointQueueReceiverEndNonRobust<AbstractLamportMessage>();
 		pendingGets = new ConcurrentLinkedQueue<AbstractLamportMessage>();
 		pendingSends = new ConcurrentLinkedQueue<String>();
 		outgoing = new ConcurrentHashMap<InetSocketAddress,PointToPointQueueSenderEnd<AbstractLamportMessage>>();
 		hasConnectionToUs = new HashSet<InetSocketAddress>();
 		backlog = new ArrayList<AbstractLamportMessage>();
 		
 		sendingThread = new SendingThread();
 		sendingThread.start();
 	}
 	
 	@Override
 	public void createGroup(int port, DeliveryGuarantee deliveryGuarantee) throws IOException {
 		assert (deliveryGuarantee==DeliveryGuarantee.NONE || deliveryGuarantee==DeliveryGuarantee.FIFO) : "Can at best implement FIFO";
 		
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
         assert (deliveryGuarantee==DeliveryGuarantee.NONE || deliveryGuarantee==DeliveryGuarantee.FIFO) : "Can at best implement FIFO";
 
 		// Try to listen on the given port. Exceptions are propagated
 		// out of the method.
 		incoming.listenOnPort(port);
 
         // Record our address.
 		InetAddress localhost = InetAddress.getLocalHost();
 		String localHostAddress = localhost.getCanonicalHostName();
 		myAddress = new InetSocketAddress(localHostAddress, port);
 
 		// Make an outgoing connection to the known peer.
 		PointToPointQueueSenderEnd<AbstractLamportMessage> out = connectToPeerAt(knownPeer);	
 
 		// Send the known peer our address. 
 		JoinRequestMessage joinRequestMessage = new JoinRequestMessage(myAddress);
 		out.put(joinRequestMessage);
 		
 		// When the known peer receives the join request it will
 		// connect to us, so let us remember that she has a connection
 		// to us.
 		hasConnectionToUs.add(knownPeer);	
 
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
			clock = Math.max(msg.getClock(), clock)+1;
 			
 			if (msg instanceof ChatMessage) {
 				ChatMessage cmsg = (ChatMessage)msg;
 				handle(cmsg);
 			} else if (msg instanceof JoinRequestMessage) {
 				JoinRequestMessage jrmsg = (JoinRequestMessage)msg;
 				handle(jrmsg);
 			} else if (msg instanceof JoinRelayMessage) {
 				JoinRelayMessage jmsg = (JoinRelayMessage)msg;
 				handle(jmsg);
 			} else if (msg instanceof LeaveGroupMessage) {
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
 			
 			// Save the backlog
 			backlog.add(msg);
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
 	
 	private void handle(BacklogMessage msg){
 		// We have joined the group
 		backlog = msg.getBacklog();
 		printBacklog();
 	}
 	
 	private void handle(LeaveGroupMessage msg){
 		InetSocketAddress address = msg.getSender();
 		if (!address.equals(myAddress)){
 			addAndNotify(pendingGets, msg);
 			disconnectFrom(address);
 		}else{
 			// That was my own leave message. If I'm the only one left
 			// in the group, then this means that I can safely shut
 			// down.
 			if (hasConnectionToUs.isEmpty())
 				incoming.shutdown();
 			
 			System.out.println("[You have left the group]");
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
 		}
 		connectToPeerAt(msg.getSender());
 	}
 	
 	private void handle(JoinRelayMessage msg){
 		if(!msg.getSender().equals(myAddress))
 			addAndNotify(pendingGets, msg);
 		
 		// Connect to the new peer and bid him welcome. 
 		PointToPointQueueSenderEnd<AbstractLamportMessage> out = connectToPeerAt(msg.getAddressOfJoiner());
 		out.put(new WelcomeMessage(myAddress));
 		// When this peer receives the wellcome message it will
 		// connect to us, so let us remember that she has a connection
 		// to us.
 
 		synchronized(hasConnectionToUs) {
 			hasConnectionToUs.add(msg.getAddressOfJoiner());
 		}
 	}
 	
 	private void handle(JoinRequestMessage msg){
 		// When the joining peer sent the join request it connected to
 		// us, so let us remember that she has a connection to us. 
 		synchronized(hasConnectionToUs) {
 			hasConnectionToUs.add(msg.getSender()); 
 		}
 		
 		// Buffer a join message so it can be gotten. 
 		addAndNotify(pendingGets, msg);
 		addAndNotify(pendingGets, new JoinRelayMessage(myAddress, msg.getSender()));
 
 		// Then we tell the rest of the group that we have a new member.
 		sendToAllExceptMe(new JoinRelayMessage(myAddress, msg.getSender()));
 		
 		// Then we connect to the new peer. 
 		PointToPointQueueSenderEnd<AbstractLamportMessage> out = connectToPeerAt(msg.getSender());
 		out.put(new BacklogMessage(myAddress, backlog));
 	}
 	
 	private void handle(ChatMessage msg){
 		addAndNotify(pendingGets, msg);
 	}
 
 	private void printBacklog(){
 		for(AbstractLamportMessage msg : backlog)
 			//if(msg instanceof JoinRequestMessage){
 				// Do nothing
 			//}else if(msg instanceof JoinRelayMessage){
 			//	if(!msg.getSender().equals(myAddress))
 			//		addAndNotify(pendingGets, msg);
 			//}else{
 				addAndNotify(pendingGets, msg);
 			//}
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
 			waitForPendingGetsOrReceivedAll();
 			if (pendingGets.isEmpty()) {
 				return null;
 				// By contract we signal shutdown by returning null.
 			} else {
 				return pendingGets.poll();
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
 				clock++;
 				AbstractLamportMessage lmsg = new ChatMessage(myAddress, msg);
 				lmsg.setClock(clock);
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
 				//out.put(new GoodbuyMessage(myAddress));
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
 			/* Send to self. */
 			incoming.put(msg);
 			/* Then send to the others. */
 			sendToAllExceptMe(msg);
 		}
     }
     private void sendToAllExceptMe(AbstractLamportMessage msg) {
 		if (isLeaving!=true) {
 			for (PointToPointQueueSenderEnd<AbstractLamportMessage> out : outgoing.values()) {
 			out.put(msg);
 			}
 		}
     }
 	
 	private void sendToRandom(AbstractLamportMessage msg){
 		Random generator = new Random();
 		Object[] values = outgoing.values().toArray();
 		PointToPointQueueSenderEnd<AbstractLamportMessage> randomPeer = (PointToPointQueueSenderEnd<AbstractLamportMessage>) values[generator.nextInt(values.length)];
 		
 		randomPeer.put(msg);
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
 			// Notify that there is a new message. 
 			coll.notify();
 		}
     }
 }
