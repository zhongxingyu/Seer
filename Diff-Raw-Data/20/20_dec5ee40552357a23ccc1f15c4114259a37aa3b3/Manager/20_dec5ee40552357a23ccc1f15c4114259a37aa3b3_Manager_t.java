 package edu.ntnu.ttk4145.recs.manager;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import edu.ntnu.ttk4145.recs.DoOrderMessage;
 import edu.ntnu.ttk4145.recs.Elevator;
 import edu.ntnu.ttk4145.recs.Message;
 import edu.ntnu.ttk4145.recs.Order;
 import edu.ntnu.ttk4145.recs.OrderMessage;
 import edu.ntnu.ttk4145.recs.Peer;
 import edu.ntnu.ttk4145.recs.UpdateStateMessage;
 import edu.ntnu.ttk4145.recs.Util;
 import edu.ntnu.ttk4145.recs.Elevator.Direction;
 import edu.ntnu.ttk4145.recs.driver.Driver;
 import edu.ntnu.ttk4145.recs.driver.Driver.Call;
 import edu.ntnu.ttk4145.recs.network.Radio;
 
 public class Manager {
 	
 	private final static String MULTICAST_GROUP = "224.0.2.1";
 	private final static int SEND_PORT = 7001;
 	private final static int RECEIVE_PORT = 7002;
 	
 	
 	private static Manager instance;
 	
 	public static Manager getInstance() {
 		if(instance == null){
 			instance = new Manager();
 		}
 		return instance;
 	}
 	
 	private final long myId = Util.makeLocalId();
 
 	public long[][] orders = new long[2][Driver.NUMBER_OF_FLOORS];
 	
 	SortedMap<Long,Peer> peers;
 	private Peer master;
 	
 	private Manager(){
 		peers = new TreeMap<Long,Peer>();
 	}
 	
 	/**
 	 * Starts the manager instance. The manager will sent and listen to alive messages.
 	 * Send and receive orders to and from the local elevator.
 	 */
 	public void startManager(){
 		Radio radio = new Radio(MULTICAST_GROUP, SEND_PORT, RECEIVE_PORT);
 		radio.start();
 		MessageListener peerListener = new MessageListener();
 		peerListener.listen();
 		discoverMaster();		
 	}
 	
 	/**
 	 * Handle a newly received alive message.
 	 * 
 	 * @param peerId The ID of the peer who sent the alive message.
 	 * @param timeOfLastAlive The time the alive message was received.
 	 * @param address The address of the peer.
 	 */
 	public void handleAliveMessage(long peerId, long timeOfLastAlive, InetAddress address) {
 		if(peers.containsKey(peerId)) {
 			peers.get(peerId).updateAliveTime(timeOfLastAlive);
 		}
 		else {
 			Peer newPeer = new Peer(address, peerId);
 			peers.put(peerId, newPeer);
 			if(newPeer.getId() < master.getId()) {
 				master = newPeer;
 			}
 		}
 	}
 	
 	/**
 	 * Update the local representation peer's state.
 	 * @param peerId The Id of the peer.
 	 * @param newState The peer's new state.
 	 */
 	public void updatePeerState(long peerId, Elevator.State newState) {
 		peers.get(peerId).updateState(newState);
 	}
 	
 	/**
 	 * Add order to the global order queue.
 	 * @param order A order to service.
 	 */
 	private void addOrder(Order order) {
 		// TODO
 	}
 	
 	/**
 	 * 
 	 * @param order Remove an order from the local instance of the global order queue.
 	 */
 	private void removeOrder(Order order) {
 		// TODO
 	}
 	
 	/**
 	 * 
 	 * @return The multicast group to listen to for alive messages over UDP.
 	 */
 	public static String getMulticastgroup() {
 		return MULTICAST_GROUP;
 	}
 
 	/**
 	 * 
 	 * @return The port used to send TCP and UDP messages.
 	 */
 	public static int getSendport() {
 		return SEND_PORT;
 	}
 	
 	/**
 	 * 
 	 * @return The port used to receive TCP and UDP messages.
 	 */
 	public static int getReciveport() {
 		return RECEIVE_PORT;
 	}
 
 	/**
 	 * Register a local call for elevator. Notice every peer that a call has been made.
 	 * 
 	 * @param button The button that was pressed.
 	 * @param floor The floor where the button was pressed.
 	 */
 	public void registerCall(Call call, int floor) {
 		orders[call.ordinal()][floor] = myId;
 		//Elevator.getLocalElevator().addOrder(order);
 		for (Peer peer : peers.values()) {
 			// TODO
 //			peer.sendMessage(new OrderMessage(order));
 		}
 	}
 	
 	/**
 	 * Notify the peers about a change in elevator state.
 	 * @param state The new elevator state.
 	 */
 	public void sendUpdateStateMessages(Elevator.State state){
 		UpdateStateMessage updateStateMessage = new UpdateStateMessage(myId, state);
 		for(Peer peer : peers.values()){
 			 peer.sendMessage(updateStateMessage);
 		}
 	}
 	
 	/**
 	 * Removes a peer from the list of all active peers.
 	 * @param peer The peer who has timed out.
 	 */
 	public void removePeer(Peer peer) {
 		peers.remove(peer);
 		if(peer == master) {
 			setMaster();
 			if(master.getId() == myId) {
 				redistributeOrders(peer);
 			}
 		}
 	}
 	
 	/**
 	 * This peer has died and his orders are distributed to the other peers.
 	 * @param peer Dead peer.
 	 */
 	private void redistributeOrders(Peer peer) {
 		
 	}
 	
 	/**
 	 * 
 	 * Finds the peer best suited to perform Order and dispatches the order.
 	 * @param order An order to perform
 	 */
 	private void dispatchOrder(Order order) {
 		Peer bestSuited = null;
 		double maxOrderRating = 0;
 		for(Peer peer : peers.values()) {
 			if(maxOrderRating < peer.getOrderRating(order)) {
 				bestSuited = peer;
 			}
 		bestSuited.sendMessage(new DoOrderMessage(order));	
 		}
 	}
 	
 	/**
 	 * Listens to alive messages to register peers and finds out who's master.
 	 */
 	private void discoverMaster() {
 		try {
 			//Wait to receive a few alive messages before we search list of peers for a master.
 			Thread.sleep(Radio.getAliveInterval());
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		setMaster();
 	}
 	
 	private void setMaster() {
 		master = peers.get(peers.firstKey());
 	}
 
 	public void orderDone(long orderId) {
 		System.out.printf("Order done: %d%n",orderId);
 	}
 	
 	/**
 	 * 
 	 * A class for listening to messages from the other peers on the network.
 	 * Spawns new threads to handle incoming messages.
 	 *
 	 */
 	private class MessageListener {
 		
 		private ServerSocket server = null;
 		
 		public MessageListener() {
 			try {
 				server = new ServerSocket(RECEIVE_PORT);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		private void listen() {
 			while(true) {
 				MessageHandler handler;
 				try {
 					handler = new MessageHandler(server.accept());
 					Thread t = new Thread(handler);
 					t.start();
 				} catch (Exception e) {
 				}
 			}
 		}
 	}
 	
 	/**
 	 * 
 	 * A class to handle the messages received from other peers.
 	 * The message types handled are of types ORDER, DO_ORDER,
 	 * DONE and STATE.
 	 *
 	 */
 	private class MessageHandler implements Runnable{
 		private Socket socket;
 		
 		public MessageHandler(Socket socket) {
 			this.socket = socket;
 		}
 		@Override
 		public void run() {
 			ObjectInputStream ois = null;
 			Message message = null;
 			try {
 				ois = new ObjectInputStream(socket.getInputStream());
 				message = (Message)ois.readObject();
 			} catch (IOException e) {
 				e.printStackTrace();
 			} catch (ClassNotFoundException e) {
 				e.printStackTrace();
 			}
 			handleMessage(message);
 		}
 		private void handleMessage(Message message) {
 			switch(message.getType()) {
 				case ORDER: 
 					Manager.getInstance().addOrder(((OrderMessage) message).getOrder());
 					break;
 				case DO_ORDER:
 					DoOrderMessage doOrderMessage = (DoOrderMessage) message;
 //					Elevator.getLocalElevator().addOrder(doOrderMessage.getOrder());
 					break;
 				case DONE:
 					Manager.getInstance().removeOrder(((OrderMessage) message).getOrder());
 					break;
 				case STATE:
 					UpdateStateMessage stateMessage = (UpdateStateMessage) message;
 					peers.get(stateMessage.getElevatorId()).updateState(stateMessage.getState());
 					break;
 				default:
 					throw new RuntimeException("Unpossible!");
 			}	
 		}
 	}
 
 	public synchronized long[][] getOrders() {
 		return Util.copyOf(orders);
 	}
 
 	public void orderDone(Direction dir, int floor) {
 		orders[dir.ordinal()][floor] = Order.NO_ORDER;
 	}

	public long getId() {
		return myId;
	}
 }
