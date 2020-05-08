 package network;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.net.InetSocketAddress;
 import java.nio.ByteBuffer;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.Selector;
 import java.nio.channels.SocketChannel;
 import java.util.Iterator;
 import java.util.Random;
 import java.util.Vector;
 
 import app.Mediator;
 import app.model.User;
 import app.states.RequestTypes;
 
 public class NetworkMockup implements NetworkMediator {
 	
 	public static final int LOGIN_PENDING = 0;
 	public static final int LOGIN_SUCCESS = 1;
 	public static final int LOGIN_FAILED = 2;
 	
 	public static boolean running = true;
 	private static int loginStatus = LOGIN_PENDING;
 	
 	public static final String LOGIN_SERVER_IP = "127.0.0.1";
 	public static final int LOGIN_SERVER_PORT = 11011;
 
 
 	private final int MIN_FILE_SIZE = 256;
 	private final int MAX_FILE_SIZE = 2048;
 	private final int CHUNK_SIZE = 32;
	private final long NETWORK_DELAY = 175;
 
 	private Mediator med;
 
 	public NetworkMockup(Mediator med) {
 		this.med = med;
 	}
 
 	@Override
 	public boolean sendItem(String name, int value) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/**
 	 * Initiates the connection to the remote socket
 	 */
 	private void connect(SelectionKey key) throws IOException {
 
 		SocketChannel socketChannel = (SocketChannel) key.channel();
 		try {
 			socketChannel.finishConnect();
 		} catch (IOException e) {
 			System.out.println("[NETWORK]: Login server is offline!");
 			running = false;
 			
 			return;
 		}
 
 		key.interestOps(SelectionKey.OP_WRITE);
 	}
 
 	private void write(SelectionKey key) throws IOException {
 
 		ByteBuffer buf = (ByteBuffer) key.attachment();
 		SocketChannel socketChannel = (SocketChannel) key.channel();
 
 		while (socketChannel.write(buf) > 0)
 			;
 
 		if (!buf.hasRemaining()) {
 			socketChannel.close();
 			running = false;
 		}
 	}
 
 	/**
 	 * Serializes a generic network status update class
 	 * 
 	 * @param nn
 	 *            Network status update class
 	 * @return serialized form of the class
 	 */
 	private byte[] marshal(NetworkNotification nn) {
 		try {
 			ByteArrayOutputStream baos = new ByteArrayOutputStream();
 			ObjectOutputStream oos = new ObjectOutputStream(baos);
 			oos.writeObject(nn);
 			oos.flush();
 			oos.close();
 			return baos.toByteArray();
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		throw new RuntimeException(
 				"[NETWORK]: Unable to convert event into raw byte data: " + nn);
 	}
 
 	public byte[] makeChunk() {
 		byte[] chunk = new byte[CHUNK_SIZE];
 		new Random().nextBytes(chunk);
 		
 		return chunk;
 	}
 
 	/**
 	 * Generates a randomly sized ByteBuffer and sends it
 	 * to the buyer, CHUNK_SIZE at a time
 	 */
 	public boolean doTransferProduct(String product, User user) {
 		
 		NetworkNotification nn;
 		Random rnd = new Random();
 		String sender = med.getUserName();
 
 		int noOfChunks = rnd.nextInt((MAX_FILE_SIZE - MIN_FILE_SIZE) / CHUNK_SIZE) +
 										MIN_FILE_SIZE / CHUNK_SIZE;
 
		med.updateGui(RequestTypes.REQUEST_INITIAL_TRANSFER, user.getName(),
				product, noOfChunks);
		
 		/* first send the initial request, with total file size */
 		nn = new NetworkNotification(RequestTypes.REQUEST_INITIAL_TRANSFER,
 						sender, product, noOfChunks);
 		
 		if (!doNetworkSend(user.getIp(), user.getPort(), nn)) {
 			System.out.println("[NETWORK]: Failed to send chunk to " + user);
 			return false;
 		}
 
 		try {
 			Thread.sleep(NETWORK_DELAY);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 
		for (int i = 1; i <= noOfChunks; i++) {
 
			med.updateGui(RequestTypes.REQUEST_TRANSFER, user.getName(),
					product, i);
			
 			nn = new NetworkNotification(RequestTypes.REQUEST_TRANSFER,
						sender, product, i, makeChunk());
 
 			if (!doNetworkSend(user.getIp(), user.getPort(), nn)) {
 				System.out.println("[NETWORK]: Failed to send chunk to " + user);
 				return false;
 			}
 
 			try {
 				Thread.sleep(NETWORK_DELAY);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 
 		return true;
 	}
 
 	public void setLoginFailed() {
 		loginStatus = LOGIN_FAILED;
 	}
 	
 	public void setLoginSuccess() {
 		loginStatus = LOGIN_SUCCESS;
 	}
 
 	public boolean fetchRelevantUsers(User user) {
 		
 		NetworkNotification nn = new NetworkNotification(
 				RequestTypes.REQUEST_RELEVANT_USERS, user);
 		
 		nn.setProduct(user.getProducts().get(0));
 		
 		System.out.println("[NETWORK]: Fetching users...");
 		
 		if (!doNetworkSend(LOGIN_SERVER_IP, LOGIN_SERVER_PORT, nn)) {
 			System.out.println("[NETWORK]: Failed to send login request");
 			return false;
 		}
 		
 		return true;
 	}
 
 	/**
 	 * 
 	 */
 	public boolean validateUsername(String username, String password, String type,
 									String ip, int port, Vector<String> products) {
 		
 		NetworkNotification nn = new NetworkNotification(
 				RequestTypes.REQUEST_LOGIN,
 				username,
 				password,
 				type,
 				ip,
 				port,
 				products);
 		
 		if (!doNetworkSend(LOGIN_SERVER_IP, LOGIN_SERVER_PORT, nn)) {
 			System.out.println("[NETWORK]: Failed to send login request");
 			return false;
 		}
 		
 		System.out.println("[NETWORK]: Waiting for login confirmation...");
 		
 		/* wait for a confirmation */
 		while (loginStatus == LOGIN_PENDING) {
 			try {
 				Thread.sleep(100);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 
 		if (loginStatus == LOGIN_FAILED)
 			return false;
 
 		System.out.println("[NETWORK]: Login successful!");
 
 		return true;
 	}
 	
 	/**
 	 * Handles a generic PopUp menu request
 	 * 
 	 * Sends a notification over the network to each of the given destinations
 	 */
 	public boolean sendNotifications(int action, String userName, String product,
 			int price, Vector<User> destinations) {
 		
 		for (User user : destinations) {
 			
 			if (action == RequestTypes.REQUEST_INITIAL_TRANSFER) {
 				return doTransferProduct(product, user);
 			}
 			
 			System.out.println("[NETWORK]: Sending to dest: " + user);
 			
 			NetworkNotification nn = new NetworkNotification(action, userName,
 					product, price);
 			
 			if (!doNetworkSend(user.getIp(), user.getPort(), nn)) {
 				System.out.println("[NETWORK]: Network send failed!");
 				return false;
 			}
 		}
 	
 		return true;
 	}
 	
 	public boolean sendNotifications(int action, String userName, String ip, int port,
 							String product, Vector<User> destinations) {
 		
 		for (User user : destinations) {
 			
 			System.out.println("[NETWORK]: Sending to dest: " + user);
 			
 			NetworkNotification nn = new NetworkNotification(action, userName,
 					product, -1);
 
 			nn.setIp(ip);
 			nn.setPort(port);
 			nn.setProduct(product);
 
 			if (!doNetworkSend(user.getIp(), user.getPort(), nn)) {
 				System.out.println("[NETWORK]: Network send failed!");
 				return false;
 			}
 		}
 	
 		return true;	
 	}
 	
 	/**
 	 * Submits a login notification (only called in Login Server mode)
 	 */
 	public boolean sendLoginNotification(int action, String ip, int port, User user) {
 		
 		NetworkNotification nn = new NetworkNotification(action, user);
 		
 		if (!doNetworkSend(ip, port, nn)) {
 			System.out.println("[Login Server]: Send Login ACK failed!");
 			return false;
 		}
 
 		return true;
 	}
 	
 	/**
 	 * Sends a serialized NetworkNotification class to the specified destination
 	 * 
 	 * @param ip destination IP
 	 * @param port destination port
 	 * @param nn the notification to be serialized and sent
 	 * 
 	 * @return true on success, false on failure
 	 */
 	private boolean doNetworkSend(String ip, int port, NetworkNotification nn) {
 		
 		Selector selector = null;
 		SocketChannel socketChannel = null;
 
 		try {
 			selector = Selector.open();
 
 			socketChannel = SocketChannel.open();
 			socketChannel.configureBlocking(false);
 			socketChannel.connect(new InetSocketAddress(ip, port));
 
 			byte[] classBytes;
 
 			try {
 				classBytes = marshal(nn);
 
 			} catch (RuntimeException e) {
 				e.printStackTrace();
 				return false;
 			}
 
 			ByteBuffer buf = ByteBuffer.allocate(classBytes.length);
 
 			buf.clear();
 			buf.put(classBytes);
 			buf.flip();
 
 			socketChannel.register(selector, SelectionKey.OP_CONNECT, buf);
 
 			System.out.println("[NETWORK]: Sending object");
 
 			while (running) {
 				selector.select();
 
 				for (Iterator<SelectionKey> it = selector.selectedKeys()
 						.iterator(); it.hasNext();) {
 					SelectionKey key = it.next();
 					it.remove();
 
 					if (key.isConnectable())
 						connect(key);
 					else if (key.isWritable())
 						write(key);
 				}
 			}
 
 		} catch (IOException e) {
 			e.printStackTrace();
 			
 			running = true;
 			return false;
 
 		} finally {
 			if (selector != null)
 				try {
 					selector.close();
 				} catch (IOException e) {
 				}
 
 			if (socketChannel != null)
 				try {
 					socketChannel.close();
 				} catch (IOException e) {
 				}
 		}
 
 		running = true;
 		return true;
 	}
 
 	public void startLoginServer(String ip, int port) {
 		new NetworkServer(ip, port, med, true).start();
 	}
 	
 	public void startServer(String ip, int port) {
 		new NetworkServer(ip, port, med, false).start();
 	}
 }
