 package network;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.net.InetSocketAddress;
 import java.nio.ByteBuffer;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.Selector;
 import java.nio.channels.ServerSocketChannel;
 import java.nio.channels.SocketChannel;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Vector;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import app.Mediator;
 import app.model.Seller;
 import app.model.User;
 import app.states.RequestTypes;
 
 public class NetworkServer extends Thread {
 
 	private static final int LISTENER_THREADS = 2;
 	private static final int BUFFER_SIZE = 8192;
 	
 	private HashMap<String, User> systemUsers;
 	
 	private final boolean loginServerMode;
 
 	private static ExecutorService pool = Executors
 			.newFixedThreadPool(LISTENER_THREADS);
 
 	private Mediator med;
 
 	private String ip;
 	private int port;
 
 	/* 
 	 * set @mode to true if you want to enable Login Server Mode,
 	 * making the application act as a Central User Login entity
 	 */
 	public NetworkServer(String ip, int port, Mediator med, boolean mode) {
 		this.loginServerMode = mode;
 		this.ip = ip;
 		this.port = port;
 
 		this.med = med;
 		systemUsers = new HashMap<String, User>();
 	}
 
 	public boolean handleLoginServerRequest(NetworkNotification nn) {
 		
 		Vector<User> dests = new Vector<User>();
 		
 		String userName = nn.getName();
 		
 		dests.add(new Seller("", nn.getIp(), nn.getPort(), null));
 		
 		System.out.println("[Login Server]: Processing login request...");
 		
 		switch (nn.getAction()) {
 		case RequestTypes.REQUEST_LOGIN:
 
 			/* already logged in from another place! */
 			if (systemUsers.get(userName) != null) {
 				
 				med.getNetMed().sendNotifications(RequestTypes.SYSTEM_LOGIN_FAILURE,
 						"", "", 0, dests);
 
 				return false;
 			}
 			
 			System.out.println("[Login Server]: Sending ACK back to " + nn.getIp() + " " + nn.getPort());
 			
 			med.getNetMed().sendLoginNotification(RequestTypes.REQUEST_LOGIN,
 					nn.getIp(), nn.getPort(), null);
 
 			Vector<String> userProducts = nn.getProducts();
 
 			systemUsers.put(userName,
 							new Seller(userName, nn.getIp(), nn.getPort(), userProducts));
 
 			break;
 			
 		case RequestTypes.REQUEST_RELEVANT_USERS:
 			userName = nn.getName();
 			userProducts = nn.getProducts();
 			
 			for (Map.Entry<String, User> e : systemUsers.entrySet()) {
 				
 				User user = e.getValue();
 				Vector<String> commonProducts = new Vector<String>();
 				
 				for (String s : user.getItems()) {
 					if (userProducts.contains(s)) {
 						commonProducts.add(s);
 					}
 				}
 				
 				if (commonProducts.size() > 0) {
 					
 					med.getNetMed().sendLoginNotification(RequestTypes.SYSTEM_NEW_LOGIN_EVENT,
 							nn.getIp(), nn.getPort(),
 							new Seller(e.getKey(), user.getIp(), user.getPort(), commonProducts));
 					
 					med.getNetMed().sendLoginNotification(RequestTypes.SYSTEM_NEW_LOGIN_EVENT,
 							user.getIp(), user.getPort(),
 							new Seller(userName, nn.getIp(), nn.getPort(), commonProducts));
 				}
 			}
 			break;
 			
 		case RequestTypes.REQUEST_LOGOUT:
 			
 			userProducts = systemUsers.get(userName).getItems();
 		
 			for (Map.Entry<String, User> e : systemUsers.entrySet()) {
 			
 				User user = e.getValue();
 				
 				for (String s : user.getItems()) {
 					if (userProducts.contains(s)) {
 						
 						med.getNetMed().sendLoginNotification(RequestTypes.REQUEST_LOGOUT,
 								user.getIp(), user.getPort(),
 								new Seller(userName, nn.getIp(), nn.getPort(), null));
 						
 						break;
 					}
 				}
 			}
 			
 			systemUsers.remove(userName);
 			
 			break;
 		default:
 			System.out.println("[LOGIN SERVER]: Invalid request type: " + nn.getAction());
 			return false;
 		}
 		
 		return true;
 	}
 	
 	public void accept(SelectionKey key) throws IOException {
 
 		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key
 				.channel();
 		SocketChannel socketChannel = serverSocketChannel.accept();
 		socketChannel.configureBlocking(false);
 		ByteBuffer buf = ByteBuffer.allocate(BUFFER_SIZE);
 		socketChannel.register(key.selector(), SelectionKey.OP_READ, buf);
 
 		System.out.println("[NETWORK]: Accepted connection from: "
 				+ socketChannel.socket().getRemoteSocketAddress());
 	}
 
 	public void read(final SelectionKey key) throws IOException {
 
 		// remove all interests
 		key.interestOps(0);
 
 		try {
 			pool.execute(new Runnable() {
 				public void run() {
 					int bytes;
 					ByteBuffer buf = (ByteBuffer) key.attachment();
 					SocketChannel socketChannel = (SocketChannel) key.channel();
 
 					buf.clear();
 					try {
 						while (true) {
 
 							if (!buf.hasRemaining())
 								break;
 
 							bytes = socketChannel.read(buf);
 
 							// check for EOF
 							if (bytes == -1) {
 								buf.flip();
 								socketChannel.close();
 								break;
 							}
 						}
 
 						NetworkNotification nn = unmarshal(buf);
 
 						System.out.println("[NETWORK]: Received notification: " + nn);
 						
 						if (loginServerMode) {
 							handleLoginServerRequest(nn);
 							
 							return;
 						}
 
 						if (nn.getAction() == RequestTypes.SYSTEM_LOGIN_FAILURE) {
 							med.getNetMed().setLoginFailed();
 							return;
 						}
 						
 						if (nn.getAction() == RequestTypes.SYSTEM_NEW_LOGIN_EVENT) {
 							
 							med.handleLoginEvent(nn.getIp(), nn.getPort(), nn.getName(),
 									nn.getProducts());
 							
 							return;
 						}
 						
 						if (nn.getAction() == RequestTypes.REQUEST_LOGIN) {
 							med.getNetMed().setLoginSuccess();
 							
 							return;
 						}
 
 						med.updateGui(nn.getAction(), nn.getName(),
 								nn.getProduct(), nn.getPrice());
 						
 					} catch (IOException e) {
 						System.out.println("[NETWORK]: Connection closed: "
 								+ e.getMessage());
 
 						try {
 							socketChannel.close();
 						} catch (IOException exc) {
 
 						}
 					}
 				}
 			});
 		} catch (Exception ex) {
 			pool.shutdown();
 		}
 
 	}
 
 	public NetworkNotification unmarshal(ByteBuffer data) {
 		try {
 			byte[] classBytes = new byte[data.remaining()];
 			data.get(classBytes, 0, classBytes.length);
 
 			ByteArrayInputStream bais = new ByteArrayInputStream(classBytes);
 			ObjectInputStream ois = new ObjectInputStream(bais);
 			NetworkNotification nn = (NetworkNotification) ois.readObject();
 
 			return nn;
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		throw new RuntimeException(
 				"[NETWORK]: Unable to unmarshal raw byte data!");
 	}
 
 	public void run() {
 
 		ServerSocketChannel serverSocketChannel = null;
 		Selector selector = null;
 
 		try {
 			selector = Selector.open();
 
 			serverSocketChannel = ServerSocketChannel.open();
 			serverSocketChannel.configureBlocking(false);
 			serverSocketChannel.socket().bind(new InetSocketAddress(ip, port));
 			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
 
 			while (true) {
 				selector.select();
 
 				for (Iterator<SelectionKey> it = selector.selectedKeys()
 						.iterator(); it.hasNext();) {
 					SelectionKey key = it.next();
 					it.remove();
 
 					if (key.isAcceptable())
 						accept(key);
 					else if (key.isReadable())
 						read(key);
 				}
 			}
 
 		} catch (IOException e) {
 			e.printStackTrace();
 
 		} finally {
 			if (selector != null)
 				try {
 					selector.close();
 				} catch (IOException e) {
 				}
 
 			if (serverSocketChannel != null)
 				try {
 					serverSocketChannel.close();
 				} catch (IOException e) {
 				}
 		}
 	}
 }
