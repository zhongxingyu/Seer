 package common;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Observable;
 
 import javax.swing.SwingWorker;
 import javax.vecmath.Vector2f;
 
 
 @SuppressWarnings("restriction")
 public class Peer extends Observable {
 	/**
 	 * The default port number for incoming network connections.
 	 */
 	public static final int DEFAULT_SERVER_PORT = 5507;
 	
 	private PeerInformation myInfo = new PeerInformation();
 	private ArrayList<PeerInformation> peers = new ArrayList<PeerInformation>();
 	private ServerSocket serverSocket;
 	private SwingWorker<Object,Object> worker;
 	
 	/**
 	 * A flag indicating whether internal operation messages (useful for
 	 * debugging) are logged to the console.  This field may be modified at any
 	 * time.
 	 */
 	public boolean logEnabled;
 
 	/**
 	 * Initializes a new <code>Peer</code> object.  The <code>Peer</code> is
 	 * not connected to a network and sends a log of internal operation
 	 * messages to the console.   
 	 */
 	public Peer() {
 		this(true);
 	}
 
 	/**
 	 * Initializes a new <code>Peer</code> object with message logging
 	 * controlled by the <code>logEnabled</code> parameter.  The
 	 * <code>Peer</code> is not connected to a network.
 	 * 
 	 * @param logEnabled Initial value for the <code>logEnabled</code> field. 
 	 */
 	public Peer(boolean logEnabled) {
 		myInfo = new PeerInformation();
 		this.logEnabled = logEnabled;
 	}
 
 	/**
 	 * Establishes a new peer-to-peer network with this <code>Peer</code>
 	 * as the sole member.  The logical coordinates of this
 	 * <code>Peer</code> are chosen randomly.
 	 * 
 	 * @return <code>true</code> if the new network was established
 	 * successfully, or <code>false</code> otherwise.
 	 */
 	public boolean createNetwork() {
 		return createNetwork(new PeerCoordinates());
 	}
 	
 	/**
 	 * Establishes a new peer-to-peer network with this <code>Peer</code>
 	 * as the sole member.  The logical coordinates of this
 	 * <code>Peer</code> are specified by parameter <code>location</code>.
 	 * 
 	 * @param location Logical coordinates for this <code>Peer</code>
 	 * within the new network.
 	 * 
 	 * @return <code>true</code> if the new network was established
 	 * successfully, or <code>false</code> otherwise.
 	 */
 	public boolean createNetwork(PeerCoordinates location) {
 		if (serverSocket == null)
 			if (!startServer())
 				return false;
 		peers = new ArrayList<PeerInformation>();
 		myInfo.location = location;
 		logMessage("Established network @ " + myInfo.location);
 		return true;
 	}	
 
 	/**
 	 * Connects this <code>Peer</code> to an existing peer-to-peer network. The
 	 * port number of the known <code>Peer</code> must be
 	 * {@link #DEFAULT_SERVER_PORT DEFAULT_SERVER_PORT}. The logical coordinates
 	 * of this <code>Peer</code> are chosen randomly.
 	 * 
 	 * @param host
 	 *            The domain name or IP address of a <code>Peer</code> within
 	 *            the network.
 	 * 
 	 * @return <code>true</code> if this <code>Peer</code> successfully
 	 *         connected to the network, or <code>false</code> otherwise.
 	 */
 	public boolean connectToNetwork(String host) {
 		return connectToNetwork(host, DEFAULT_SERVER_PORT);
 	}
 	
 	/**
 	 * Connects this <code>Peer</code> to an existing peer-to-peer network. The
 	 * port number of the known <code>Peer</code> must be
 	 * {@link #DEFAULT_SERVER_PORT DEFAULT_SERVER_PORT}. The preferred logical
 	 * coordinates of this <code>Peer</code> are specified by parameter
 	 * <code>location</code>, but the actual logical coordinates may be chosen
 	 * randomly to avoid collision with other <code>Peer</code>s.
 	 * 
 	 * @param host
 	 *            The domain name or IP address of a <code>Peer</code> within
 	 *            the network.
 	 * @param location
 	 *            Preferred logical coordinates for this <code>Peer</code>.
 	 * 
 	 * @return <code>true</code> if this <code>Peer</code> successfully
 	 *         connected to the network, or <code>false</code> otherwise.
 	 */
 	public boolean connectToNetwork(String host, PeerCoordinates location) {
 		return connectToNetwork(host, DEFAULT_SERVER_PORT, location);
 	}
 	
 	/**
 	 * Connects this <code>Peer</code> to an existing peer-to-peer network. The
 	 * logical coordinates of this <code>Peer</code> are chosen randomly.
 	 * 
 	 * @param host
 	 *            The domain name or IP address of a <code>Peer</code> within
 	 *            the network.
 	 * @param port
 	 *            The port number of a <code>Peer</code> within the network.
 	 * 
 	 * @return <code>true</code> if this <code>Peer</code> successfully
 	 *         connected to the network, or <code>false</code> otherwise.
 	 */
 	public boolean connectToNetwork(String host, int port) {
 		try {
 			return connectToNetwork(InetAddress.getByName(host), port);
 		} catch (UnknownHostException e) {
 			System.err.println(e);
 			return false;
 		}
 	}
 	
 	/**
 	 * Connects this <code>Peer</code> to an existing peer-to-peer network. The
 	 * preferred logical coordinates of this <code>Peer</code> are specified by
 	 * parameter <code>location</code>, but the actual logical coordinates may
 	 * be chosen randomly to avoid collision with other <code>Peer</code>s.
 	 * 
 	 * @param host
 	 *            The domain name or IP address of a <code>Peer</code> within
 	 *            the network.
 	 * @param port
 	 *            The port number of a <code>Peer</code> within the network.
 	 * @param location
 	 *            Preferred logical coordinates for this <code>Peer</code>.
 	 * 
 	 * @return <code>true</code> if this <code>Peer</code> successfully
 	 *         connected to the network, or <code>false</code> otherwise.
 	 */
 	public boolean connectToNetwork(String host, int port, PeerCoordinates location) {
 		try {
 			return connectToNetwork(InetAddress.getByName(host), port, location);
 		} catch (UnknownHostException e) {
 			System.err.println(e);
 			return false;
 		}
 	}
 
 	/**
 	 * Connects this <code>Peer</code> to an existing peer-to-peer network. The
 	 * port number of the known <code>Peer</code> must be
 	 * {@link #DEFAULT_SERVER_PORT DEFAULT_SERVER_PORT}. The logical coordinates
 	 * of this <code>Peer</code> are chosen randomly.
 	 * 
 	 * @param host
 	 *            The IP address of a <code>Peer</code> within the network.
 	 * 
 	 * @return <code>true</code> if this <code>Peer</code> successfully
 	 *         connected to the network, or <code>false</code> otherwise.
 	 */
 	public boolean connectToNetwork(InetAddress host) {
 		return connectToNetwork(host, DEFAULT_SERVER_PORT);
 	}
 	
 	/**
 	 * Connects this <code>Peer</code> to an existing peer-to-peer network. The
 	 * port number of the known <code>Peer</code> must be
 	 * {@link #DEFAULT_SERVER_PORT DEFAULT_SERVER_PORT}. The preferred logical
 	 * coordinates of this <code>Peer</code> are specified by parameter
 	 * <code>location</code>, but the actual logical coordinates may be chosen
 	 * randomly to avoid collision with other <code>Peer</code>s.
 	 * 
 	 * @param host
 	 *            The IP address of a <code>Peer</code> within the network.
 	 * @param location
 	 *            Preferred logical coordinates for this <code>Peer</code>.
 	 * 
 	 * @return <code>true</code> if this <code>Peer</code> successfully
 	 *         connected to the network, or <code>false</code> otherwise.
 	 */
 	public boolean connectToNetwork(InetAddress host, PeerCoordinates location) {
 		return connectToNetwork(host, DEFAULT_SERVER_PORT, location);
 	}
 	
 	/**
 	 * Connects this <code>Peer</code> to an existing peer-to-peer network. The
 	 * logical coordinates of this <code>Peer</code> are chosen randomly.
 	 * 
 	 * @param host
 	 *            The IP address of a <code>Peer</code> within the network.
 	 * @param port
 	 *            The port number of a <code>Peer</code> within the network.
 	 * 
 	 * @return <code>true</code> if this <code>Peer</code> successfully
 	 *         connected to the network, or <code>false</code> otherwise.
 	 */
 	public boolean connectToNetwork(InetAddress host, int port) {
 		return connectToNetwork(host, port, new PeerCoordinates());
 	}
 
 	/**
 	 * Connects this <code>Peer</code> to an existing peer-to-peer network. The
 	 * preferred logical coordinates of this <code>Peer</code> are specified by
 	 * parameter <code>location</code>, but the actual logical coordinates may
 	 * be chosen randomly to avoid collision with other <code>Peer</code>s.
 	 * 
 	 * @param host
 	 *            The IP address of a <code>Peer</code> within the network.
 	 * @param port
 	 *            The port number of a <code>Peer</code> within the network.
 	 * @param location
 	 *            Preferred logical coordinates for this <code>Peer</code>.
 	 * 
 	 * @return <code>true</code> if this <code>Peer</code> successfully connects
 	 *         to the network, or <code>false</code> otherwise.
 	 */
 	public boolean connectToNetwork(InetAddress host, int port, PeerCoordinates location) {
 		if (serverSocket == null)
 			if (!startServer())
 				return false;
 		try {
 			Socket socket = new Socket();
 			try {
 				socket.connect(new InetSocketAddress(host, port), 10000);
 			} catch (IOException e) {
 				System.out.println("Unable to connect to " + host + ":" + port);
 				return false;
 			}
 			ObjectOutputStream socketOut = new ObjectOutputStream(socket.getOutputStream());
 			socketOut.writeObject(createJoinMessage(location));
 			socket.close();
 			while (myInfo.location == null)
 				Thread.sleep(1000);
 			logMessage("Joined network @ " + myInfo.location);
 			return true;
 		} catch (Exception e) {
 			System.err.println(e);
 			return false;
 		}
 	}
 	
 	/**
 	 * Disconnects this <code>Peer</code> from the peer-to-peer network.
 	 */
 	public synchronized void disconnectFromNetwork() {
 		PeerMessage mesg = createAddPeersMessage();
 		mesg.peers = peers;
 		for (PeerInformation peer : peers)
 			sendMessage(mesg, peer);
 		mesg = createRemovePeersMessage();
 		for (PeerInformation peer : peers)
 			sendMessage(mesg, peer);
 		worker.cancel(true);
 		try {
 			serverSocket.close();
 		} catch (IOException e) {
 		} catch (NullPointerException e) {
 		}
 		serverSocket = null;
 		System.out.println(myInfo + " disconnected");
 	}
 
 	/**
 	 * Identifies the <code>Peer</code> in the network adjacent to this
 	 * <code>Peer</code> in the direction (<code>x</code>,<code>y</code>).
 	 * 
 	 * @param x
 	 *            X component of a direction relative to the logical coordinates
 	 *            of this <code>Peer</code>.
 	 * @param y
 	 *            Y component of a direction relative to the logical coordinates
 	 *            of this <code>Peer</code>.
 	 * 
 	 * @return A <code>PeerInformation</code> object representing the adjacent
 	 *         <code>Peer</code>, or <code>null</code> if no adjacent
 	 *         <code>Peer</code> exists.
 	 */
 	public synchronized PeerInformation getPeerInDirection(float x, float y) {
 		float minDistance = Float.POSITIVE_INFINITY;
 		PeerInformation minPeer = null;
 		
 		Vector2f startPoint = new Vector2f(myInfo.location.getX(), myInfo.location.getY());
 		Vector2f direction = new Vector2f(x, y);
 		for (PeerInformation peer : peers) {
 			Vector2f normal = new Vector2f(peer.location.getX() - startPoint.x, peer.location.getY() - startPoint.y);
 			Vector2f midpoint = new Vector2f((peer.location.getX() + startPoint.x) / 2f, (peer.location.getY() + startPoint.y) / 2f);
 			
 			float denominator = direction.dot(normal);
 			if (denominator == 0)
 				continue;
 			midpoint.scaleAdd(-1, startPoint, midpoint);
 			float distance = midpoint.dot(normal) / denominator;
 			if (distance > 0 && distance < minDistance) {
 				minDistance = distance;
 				minPeer = peer;
 			}
 		}
 
 		// Tuples for fixed boundaries: normal.x, normal.y, point.x, point.y
 		float boundaries[] = {1, 0, PeerCoordinates.MIN_X, PeerCoordinates.MIN_Y,
 		                      0, 1, PeerCoordinates.MIN_X, PeerCoordinates.MIN_Y,
 		                      -1, 0, PeerCoordinates.MAX_X, PeerCoordinates.MAX_Y,
 		                      0, -1, PeerCoordinates.MAX_X, PeerCoordinates.MAX_X};
 		for (int i = 0; i < boundaries.length; i += 4) {
 			Vector2f normal = new Vector2f(boundaries[i], boundaries[i+1]);
 			Vector2f point = new Vector2f(boundaries[i+2], boundaries[i+3]);
 			
 			float denominator = direction.dot(normal);
 			if (denominator == 0)
 				continue;
 			point.scaleAdd(-1, startPoint, point);
 			float distance = point.dot(normal) / denominator;
 			if (distance > 0 && distance < minDistance)
 				return null;
 		}
 		
 		return minPeer;
 	}
 	
 	/**
 	 * Gets the size of peers in the list
 	 * 
 	 * @param returns an int representing the number of peers
 	 */
 	public int getPeerSize() {
 		return peers.size();
 	}
 	
 	/**
 	 * Get the PeerInformation.
 	 */
 	public PeerInformation getPeerInformation() {
 		return this.myInfo;
 	}
 
 	/**
 	 * A method that sends a CollidableObject to all
 	 * <code>Peer</code>s in the network.
 	 * 
 	 * @param payload
 	 *            A CollidableObject.
 	 * 
 	 * @return <code>true</code> if the message was successfully sent to all
 	 *         <code>Peer</code>s, or <code>false</code> otherwise.
 	 */
 	public synchronized boolean sendToAllPeers(CollidableObject payload) {
 		PeerMessage message = createPayloadMessage(payload);
 		boolean success = true;
 		for (PeerInformation peer : peers)
 			success = sendMessage(message, peer) && success;
 		return success;		
 	}
 	
 	/**
 	 * A method that sends a CollidableObject to a specific
 	 * <code>Peer</code> in the network.
 	 * 
 	 * @param payload
 	 *            A CollidableObject.
 	 * 
 	 * @return <code>true</code> if the message was successfully sent to the
 	 *         <code>Peer</code>, or <code>false</code> otherwise.
 	 */
 	public synchronized boolean sendPayloadToPeer(PeerInformation peer, CollidableObject payload) {
 		PeerMessage message = createPayloadMessage(payload);
 		return sendMessage(message, peer);
 	}
 	
 	/**
 	 * A method that sends an object to all
 	 * <code>Peer</code>s in the network.
 	 * 
 	 * @param payload
 	 *            An object.
 	 * 
 	 * @return <code>true</code> if the message was successfully sent to all
 	 *         <code>Peer</code>s, or <code>false</code> otherwise.
 	 */
 	public synchronized boolean sendExtraToAllPeers(Object payload) {
 		PeerMessage message = createExtraMessage(payload);
 		boolean success = true;
 		for (PeerInformation peer : peers)
 			success = sendMessage(message, peer) && success;
 		return success;
 	}
 
 	private boolean startServer() {
 		return startServer(DEFAULT_SERVER_PORT);
 	}
 	
 	private boolean startServer(int port) {
 		while (true) {
 			try {
 				serverSocket = new ServerSocket(port);
 			} catch (IOException e) {
 				port++;
 				continue;
 			} catch (SecurityException e) {
 				System.err.println(e);
 				return false;
 			}
 			try {
 				myInfo.address = InetAddress.getLocalHost();
 			} catch (UnknownHostException e) {
 				System.err.println(e);
 				try {
 					serverSocket.close();
 				} catch (IOException e1) {
 				}
 				serverSocket = null;
 				return false;
 			}
 			myInfo.port = port;
 			break;
 		}
 		
 		System.out.println("Listening on " + myInfo.address + ":" + myInfo.port);
 		
 		worker = new SwingWorker<Object,Object>() {
 			protected Object doInBackground() throws Exception {
 				while (!isCancelled()) {
 					logMessage("Waiting for peer connection...");
 					try {
 						Socket socket = serverSocket.accept();
 						processConnection(socket);
 					} catch (SocketException e) {
 					} catch (IOException e) {
 					    System.err.println(e);
 					}
 				}
 				return null;
 			}
 		};
 		worker.execute();
 		
 		return true;
 	}
 
 	private synchronized void processConnection(Socket socket) {
 		ObjectInputStream socketIn;
 		PeerMessage mesg;
 		try {
 			socketIn = new ObjectInputStream(socket.getInputStream());
 			mesg = (PeerMessage)socketIn.readObject();
 		} catch (Exception e) {
 			System.err.println(e);
 			return;
 		}
 		if (mesg.sender.address == null || mesg.type != PeerMessage.Type.JOIN)
 			mesg.sender.address = socket.getInetAddress();
 		switch (mesg.type) {
 		case JOIN:
 			logMessage("Received JOIN message from " + mesg.sender);
 			while (true) {
 				PeerInformation peer = lookup(mesg.sender.location);
 				if (peer != myInfo) {
 					sendMessage(mesg, peer);
 					break;
 				} else if (myInfo.location.equals(mesg.sender.location))
 					mesg.sender.location.setToRandomCoordinate();
 				else {
 					sendMessage(createJoinResultMessage(mesg.sender.location), mesg.sender);
 					break;
 				}
 			}
 			break;
 		case JOIN_RESULT:
 			logMessage("Received JOIN_RESULT message from " + mesg.sender);
 			assert(peers == null);
 			peers = mesg.peers;
 			assert(myInfo.location == null);
 			myInfo.location = mesg.location;
 			PeerMessage newMesg = createAddPeersMessage();
 			for (PeerInformation peer : peers) {
 				logMessage("Adding peer " + peer);
 				sendMessage(newMesg, peer);
 			}
 			logMessage("Adding peer " + mesg.sender);
 			sendMessage(newMesg, mesg.sender);
 			peers.add(mesg.sender);
 			break;
 		case ADD_PEERS:
 			logMessage("Received ADD_PEERS message from " + mesg.sender);
 			if (!peers.contains(mesg.sender)) {
 				logMessage("Adding peer " + mesg.sender);
 				peers.add(mesg.sender);
 			}
 			for (PeerInformation peer : mesg.peers)
 				if (!peers.contains(peer) && !myInfo.equals(peer)) {
 					logMessage("Adding peer " + peer);
 					peers.add(peer);
 				}
 			break;
 		case REMOVE_PEERS:
 			logMessage("Received REMOVE_PEERS message from " + mesg.sender);
 			if (peers.contains(mesg.sender)) {
 				logMessage("Removing peer " + mesg.sender);
 				peers.remove(mesg.sender);
 			}
 			for (PeerInformation peer : mesg.peers)
 				if (peers.contains(peer)) {
 					logMessage("Removing peer " + peer);
 					peers.remove(peer);
 				}
 			break;
 			
 		case PAYLOAD:
 			logMessage("Received PAYLOAD message from " + mesg.sender);
 			
 			setChanged();
 			notifyObservers(mesg.payload);
 			
 			break;
 			
 		case EXTRA:
 			logMessage("Received EXTRA message from " + mesg.sender);
 			
			if (mesg.id != null && mesg.id.equals(PeerMessage.DEFAULT_ID)) {
 				setChanged();
 				notifyObservers(mesg);
 			}
 			
 			break;
 		}
 	}
 
 	private synchronized PeerInformation lookup(PeerCoordinates location) {
 		double minDistance = myInfo.location.distanceTo(location);
 		int min = -1;
 		for (int i = 0; i < peers.size(); i++) {
 			double distance = peers.get(i).location.distanceTo(location);
 			if (distance < minDistance) {
 				minDistance = distance;
 				min = i;
 			}
 		}
 		if (min == -1)
 			return myInfo;
 		return peers.get(min);
 	}
 
 	private boolean sendMessage(PeerMessage message, PeerInformation destination) {
 		try {
 			Socket socket = new Socket(destination.address, destination.port);
 			ObjectOutputStream socketOut = new ObjectOutputStream(socket.getOutputStream());
 			socketOut.writeObject(message);
 			socket.close();
 		} catch (IOException e) {
 			System.err.println(e);
 			return false;
 		}
 		return true;
 	}
 
 	private PeerMessage createJoinMessage(PeerCoordinates location) {
 		PeerInformation tmp = new PeerInformation(null, myInfo.port, location);
 		return new PeerMessage(PeerMessage.Type.JOIN, tmp);
 	}
 	
 	private PeerMessage createJoinResultMessage(PeerCoordinates location) {
 		PeerMessage mesg = new PeerMessage(PeerMessage.Type.JOIN_RESULT, myInfo);
 		mesg.peers = peers;
 		mesg.location = location;
 		return mesg;
 	}
 	
 	private PeerMessage createAddPeersMessage() {
 		return new PeerMessage(PeerMessage.Type.ADD_PEERS, myInfo);
 	}
 	
 	private PeerMessage createRemovePeersMessage() {
 		return new PeerMessage(PeerMessage.Type.REMOVE_PEERS, myInfo);
 	}
 	
 	private PeerMessage createPayloadMessage(CollidableObject payload) {
 		PeerMessage mesg = new PeerMessage(PeerMessage.Type.PAYLOAD, myInfo);
 		mesg.payload = payload;
 		return mesg;
 	}
 	
 	private PeerMessage createExtraMessage(Object payload) {
 		PeerMessage mesg = new PeerMessage(PeerMessage.Type.EXTRA, myInfo);
 		mesg.extra = payload;
 		return mesg;
 	}
 	
 	private void logMessage(String text) {
 		if (logEnabled)
 			System.out.println(new Date() + " -- " + text);
 	}
 	
 	public String getMyName() {
 		if (myInfo == null) {
 			return "Not Connected";
 			
 		} else {
 			return myInfo.toString();
 		}
 	}
 }
