 /*
  * Created on Oct 1, 2003
  */
 package games.batoru.client;
 
 import java.io.IOException;
 import java.net.Socket;
 
 import org.codejive.world3d.Shape;
 import org.codejive.world3d.Universe;
 import org.codejive.world3d.net.ConnectedMessagePort;
 import org.codejive.world3d.net.MessagePacket;
 import org.codejive.world3d.net.MessagePort;
 import org.codejive.world3d.net.MessageReader;
 import org.codejive.world3d.net.MessageStream;
 import org.codejive.world3d.net.NetworkClassCache;
 
 import games.batoru.net.ClientMessageHelper;
 import games.batoru.net.ServerFinder;
 import games.batoru.net.ServerInfo;
 import games.batoru.net.ServerMessageHelper;
 
 /**
  * @author Tako
  */
 public class Client implements Runnable, ServerFinder.ServerlistChangedListener {
 	private Thread m_thread;
 	private boolean m_bRunning;
 
 	private ServerFinder m_finder;
 	private MessagePort m_client;
 	private MessagePacket m_message;
 	private Socket m_tcpSocket;
 	private int m_nState;
 	private Universe m_universe;
 	private Shape m_avatar;
 	private ClientView3d m_view;
 
 	public static final int STATE_DISCONNECTED = 0;
 	public static final int STATE_CONNECTING = 1;
 	public static final int STATE_CONNECTED = 2;
 	public static final int STATE_DISCONNECTING = 3;
 	
 	public Client() {
 		m_message = new MessagePacket();
 		m_nState = STATE_DISCONNECTED;
 	}
 	
 	public Universe getUniverse() {
 		return m_universe;
 	}
 	
 	public Shape getAvatar() {
 		return m_avatar;
 	}
 	
 	public void start() {
 		m_thread = new Thread(this);
 		m_thread.start();
 	}
 	
 	public void stop() {
 		m_bRunning = false;
 		if (m_thread != null) {
 			m_thread.interrupt();
 			m_thread = null;
 		}
 	}
 	
 	public void run() {
 		m_finder = new ServerFinder();
 		m_finder.setServerlistChangedListener(this);
 		if (m_finder.start()) {
 			m_bRunning = true;
 			try {
 				synchronized(m_finder) {
 					m_finder.wait();
 				}
 			} catch (InterruptedException e) {
 				m_bRunning = false;
 			}
 
 			// Any servers found?
 			if (m_finder.getServers().size() > 0) {
 				// We're going to connect so we don't need to know about other servers anymore
 				m_finder.stop();
 			
 				// Just connect to the first server available for now
 				ServerInfo si = (ServerInfo)m_finder.getServers().get(0);
 			
 				// Create client message port 
 				m_client = new ConnectedMessagePort("Unconnected Client", si.getAddress(), si.getPort());
 				if (m_client.start()) {
 					try {
 						// Let's connect to the server
 						connect();
 				
 						// Now wait for the reply
 						MessagePacket msg = m_client.receivePacket();
 						int cookie = msg.readInt();
 						byte packetType = msg.readByte();
 						switch (packetType) {
 							case ServerMessageHelper.MSG_CONNECT_ACCEPT:
 								short nServerPort = msg.readShort();
 								Universe.log(this, "Server accepted connect request, remote port: " + nServerPort);
 								int port = m_client.getPort();
 								m_client.stop();
 								m_client = new ConnectedMessagePort("Client #" + nServerPort, port, msg.getAddress(), nServerPort);
 								m_client.start();
 								m_nState = STATE_CONNECTED;
 								ClientMessageHelper.sendReady(m_message, m_client);
 //								m_client.setName("Client #" + nServerPort);
 //								m_client.bind(msg.getAddress(), nServerPort);
 								break;
 							case ServerMessageHelper.MSG_CONNECT_DENY:
 								String sReason = msg.readString();
 								m_client.stop();
 								Universe.log(this, "Server denied connect request: " + sReason);
 								break;
 							default:
 								Universe.log(this, "unknown packet type (" + packetType + ")");
 								break;
 						}
 	
 						// And this is the actual command dispatch loop for the rest of the game
 						while (m_bRunning && !m_client.isClosed()) {
 							msg = m_client.receivePacket();
 							boolean bOk = true;
 							while (bOk && msg.hasMoreData()) {
 								bOk = handleMessage(msg);
 							}
 						}
 					} catch (InterruptedException e) {
 						// TODO: Gracefully handle shutdown of client
 						System.err.println(e);
 					}
 				}
 			}
 
 			if (m_view != null) {
 				m_view.stop();
 				m_view = null;
 			}
 			if (m_client != null) {
 				disconnect();
 				m_client.stop();
 				m_client = null;
 			}
 			if (m_finder != null) {
 				m_finder.stop();
 				m_finder = null;
 			}
 		} else {
 			m_finder = null;
 		}
 	}
 	
 	public void serverlistChanged(ServerFinder _finder) {
 		synchronized(m_finder) {
 			m_finder.notify();
 		}
 	}
 
 	public MessagePort getMessagePort() {
 		return m_client;
 	}
 	
 	public void connect() {
 		// Send connect request
 		m_nState = STATE_CONNECTING;
 		ClientMessageHelper.sendConnectRequest(m_message, m_client);
 	}
 	
 	public void disconnect() {
 		if (m_nState == STATE_CONNECTED) {
 			// Send a disconnect message to the server
 			ClientMessageHelper.sendDisconnectRequest(m_message, m_client);
 		}
 		_disconnect();
 	}
 	
 	private void _disconnect() {
 		m_nState = STATE_DISCONNECTED;
 		Universe.log(this, "disconnected");
 		m_client.setName("Unconnected client");
 	}
 
 	protected boolean handleMessage(MessageReader _reader) {
 		boolean bOk = true;
 		NetworkClassCache cache = NetworkClassCache.getClientCache(); 
 		byte packetType = _reader.readByte();
 		// Special packet
 		switch (packetType) {
 			case ServerMessageHelper.MSG_DISCONNECT:
 				String sReason = _reader.readString();
 				Universe.log(this, "DISCONNECT message received: '" + sReason + "'");
 				_disconnect();
 				stop();
 				break;
 			case ServerMessageHelper.MSG_OPEN_TCP:
 				Universe.log(this, "OPEN TCP message received");
 				try {
 					m_tcpSocket = new Socket(m_client.getDestinationAddress(), m_client.getDestinationPort(), m_client.getAddress(), m_client.getPort());
 					MessageStream msg = new MessageStream(m_tcpSocket);
 					boolean bContinue = true;
 					while (bContinue && msg.hasMoreData()) {
 						bContinue = handleMessage(msg);
 					}
 					m_tcpSocket.close();
 					System.err.println("Hohoho!!!!");
 				} catch (IOException e) {
 					System.err.println(e);
 					// TODO: Bail out gracefully
 				}
 				break;
 			case ServerMessageHelper.MSG_CLASS_LIST:
 				Universe.log(this, "CLASS LIST message received");
 				// Reading the list of classes we're going to use
 				cache.clearRegisteredInstances();
 				cache.clearRegisteredClasses();
 				String sClass;
 				while ((sClass = _reader.readString()).length() > 0) {
 					Universe.log(this, "adding class: " + sClass);
 					cache.registerClass(sClass, sClass);
 				}
 				// Load all the classes
 				for (int i = 0; i < cache.getRegisteredClasses().size(); i++) {
 					Class cls = cache.getClientClass(i);
 					if (cls == null) {
 						// Missing class, disconnect from the server
						System.err.println("Could not load class: " + cache.getClientClassName(i));
 						stop();
 					}
 				}
 				break;
 			case ServerMessageHelper.MSG_SPAWN_ENTITY:
 				Universe.log(this, "SPAWN ENTITY message received");
 				ClientMessageHelper.spawn(_reader);
 				break;
 			case ServerMessageHelper.MSG_UPDATE_ENTITY:
 				Universe.log(this, "UPDATE ENTITY message received");
 				ClientMessageHelper.update(_reader);
 				break;
 			case ServerMessageHelper.MSG_KILL_ENTITY:
 				Universe.log(this, "KILL ENTITY message received");
 				ClientMessageHelper.kill(_reader);
 				break;
 			case ServerMessageHelper.MSG_START_3D:
 				Universe.log(this, "START 3D message received");
 				short nUniverseId = _reader.readShort();
 				short nAvatarId = _reader.readShort();
 				Universe universe = (Universe)cache.getInstance(nUniverseId);
 				Shape avatar = (Shape)cache.getInstance(nAvatarId);
 				startRendering(universe, avatar);
 				break;
 			default:
 				Universe.log(this, "unknown message type");
 				bOk = false;
 				break;
 		}
 		return bOk;
 	}
 
 	public void startRendering(Universe _universe, Shape _avatar) {
 		m_universe = _universe;
 		m_avatar = _avatar;
 		m_view = new ClientView3d(this, "Batoru Client", false);
 		m_view.start();
 	}
 }
