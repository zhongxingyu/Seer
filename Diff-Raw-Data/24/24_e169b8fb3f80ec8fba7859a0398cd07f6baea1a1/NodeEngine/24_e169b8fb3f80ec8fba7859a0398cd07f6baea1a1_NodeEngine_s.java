 package org.publicmain.nodeengine;
 
 import java.awt.Dimension;
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 import java.net.SocketTimeoutException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import javax.swing.JFrame;
 import javax.swing.JScrollPane;
 import javax.swing.JTree;
 import javax.swing.tree.TreeNode;
 
 import org.publicmain.chatengine.ChatEngine;
 import org.publicmain.common.Config;
 import org.publicmain.common.ConfigData;
 import org.publicmain.common.FileTransferData;
 import org.publicmain.common.LogEngine;
 import org.publicmain.common.MSG;
 import org.publicmain.common.MSGCode;
 import org.publicmain.common.NachrichtenTyp;
 import org.publicmain.common.Node;
 import org.publicmain.gui.GUI;
 import org.publicmain.sql.DatabaseEngine;
 import org.resources.Help;
 
 /**
  * Die NodeEngine ist fr die Verbindungen zu anderen Nodes zustndig. Sie
  * verwaltet die bestehenden Verbindungen, sendet Nachichten und Dateien und ist
  * fr das Routing zustndig.
  * 
  * @author ATRM
  * 
  */
 
 public class NodeEngine {
 	// Zeitspanne nach der ein Node die Suche nach anderen Nodes aufgibt und
 	// sich selbst zur Root erklrt.
 	private final long DISCOVER_TIMEOUT = Config.getConfig().getDiscoverTimeout();
 	// Zeitspanne die ein Root auf ROOT_ANNOUNCES wartet um zu entscheiden wer
 	// Root bleibt.
 	private final long ROOT_CLAIM_TIMEOUT = Config.getConfig().getRootClaimTimeout();
 	// Statischer Zeiger auf die einzige Instanz der NodeEngine.
 	private static volatile NodeEngine ne;
 	private final long nodeID;
 	// Die Node-Reprsentation dieser NodeEngine.
 	private Node meinNode;
 	// Zeiger auf die ChatEngine.
 	private ChatEngine ce;
 	// Haken zum fangen von Nachrichten.
 	private Hook angler = new Hook();
 	// Server-Socket fr eingehende Verbindungen (Passiv/Childs).
 	private ServerSocket server_socket;
 	// TCP-Socket zur Verbindung mit anderen Nodes (Aktiv/Parent/Root).
 	private ConnectionHandler root_connection;
 	// Multicast/Broadcat UDP-Socket zur Verbindungsaushandlung.
 	private MulticastConnectionHandler multi_socket;
 	// Liste bestehender Child-Verbindungenin eigener Hll-Klasse.
 	public List<ConnectionHandler> connections;
 	// Set, vom Typ String, aller Gruppen.
 	private Set<String> allGroups = new HashSet<String>();
 	// Set, vom Typ String, aller <u>abonierten</u> Gruppen und aller
 	// untergeordneter Nodes.
 	private Set<String> myGroups = new HashSet<String>();
 	// Warteschlange (Queue) fr Bewerberpakete bei Neuverhandelung der Root.
 	private BlockingQueue<MSG> root_claims_stash;
 	// Set, vom Typ Node, aller dieser NodeEngine bekannten Nodes.
 	private Set<Node> allNodes;
 	// Dieser Knoten mchte die Root sein und benimmt sich dementsprechend.
 	private volatile boolean rootMode;
 	// Dieser Node mchte an sein und verbunden bleiben, alle Threads werden
 	// informiert wenn die Anwednung beendet wird.
 	private volatile boolean online;
 	// Dieser Node sammelt gerade ROOT_ANNOUNCES um einen neuen Root zu
 	// bestimmen.
 	private volatile boolean rootDiscovering;
 	// Dieser Thread akzeptiert und schachelt eingehende Verbindungen auf dem
 	// Server-Socket.
 	private Thread connectionsAcceptBot = new Thread(new ConnectionsAccepter());
 	// Dieser Thread bestimmt nach einer Verzgerung diesen
 	// Node zum Root, er wird durch einen Discover gestartet.
 	private Thread rootMe;
 	// Der Thread sammelt und wertet ROOT_ANNOUNCES aus, er wird beim Empfang
 	// oder Versand eines ROOT_ANNOUNCE gestartet.
 	private Thread rootClaimProcessor;
 	private BestNodeStrategy myStrategy; 
 	
 	/**
 	 * Konstruktor fr die NodeEngine
 	 * 
 	 * @param parent
 	 * @throws IOException
 	 */
 	public NodeEngine(ChatEngine parent) throws IOException {
 
		allNodes = new HashSet<Node>();
 		connections = new CopyOnWriteArrayList<ConnectionHandler>();
 
 		root_claims_stash = new LinkedBlockingQueue<MSG>();
 
 		nodeID = ((long) (Math.random() * Long.MAX_VALUE));
 		ne = this;
 		ce = parent;
 		online = true;
 
 		server_socket = new ServerSocket(0);
 		multi_socket = MulticastConnectionHandler.getMC();
 		multi_socket.registerNodeEngine(this);
 
 		meinNode = new Node(server_socket.getLocalPort(), nodeID,
 				ce.getUserID(), System.getProperty("user.name"), ce.getAlias());
 		allNodes.add(meinNode);
 		DatabaseEngine.getDatabaseEngine().put(meinNode);
 
 		myStrategy = new WeightedDistanceStrategy(1, 0, 0);
 
 		connectionsAcceptBot.start();
 
 		LogEngine.log(this, "Multicast Socket geffnet", LogEngine.INFO);
 
 		discover();
 	}
 	
 	/**
 	 * Getter fr die laufende Instanz der NodeEngine.
 	 * 
 	 * @return NodeEngine
 	 */
 	public static NodeEngine getNE() {
 		return ne;
 	}
 
 	/**
 	 * Getter fr diese NodeEngine.
 	 * 
 	 * @return this
 	 */
 	public Node getMe() {
 		return meinNode;
 	}
 	
 	/**
 	 * Getter fr den optimalen Node zum anfgen eines weiteren, abhngig von
 	 * der gewhlten Strategie.
 	 * 
 	 * @return
 	 */
 	private Node getBestNode() {
 		return myStrategy.getBestNode();
 	}
 
 	/**
 	 * Gibt <code>true</code> zurck wenn die laufende NodeEngine hochgefahren
 	 * und mit anderen Nodes verbunden oder Root ist, <code>false</code> wenn
 	 * nicht.
 	 */
 	private boolean hasChildren() {
 		return connections.size() > 0;
 	}
 
 	/**
 	 * Gibt <code>true</code> zurck wenn die laufende NodeEngine Root ist und
 	 * <code>false</code> wenn nicht.
 	 * 
 	 * @return boolean
 	 */
 	public boolean isRoot() {
 		return rootMode && !hasParent();
 	}
 
 	/**
 	 * Gibt <code>true</code> zurck wenn die Anwendung den Status online hat
 	 * und <code>false</code> wenn nicht.
 	 * 
 	 * @return boolean
 	 */
 	public boolean isOnline() {
 		return online;
 	}
 
 	/**
 	 * Prft ob dieser Node einen bergeordneten Node hat. Gibt
 	 * <code>true</code> zurck wenn ja und <code>false</code> wenn nicht.
 	 * 
 	 * @return boolean
 	 */
 	public boolean hasParent() {
 		return (root_connection != null && root_connection.isConnected());
 	}
 	
 	/**
 	 * Getter fr ein Set von allen verbundenen Nodes.
 	 * 
 	 * @return boolean
 	 */
 	public Set<Node> getNodes() {
 		synchronized (allNodes) {
 			return allNodes;
 		}
 	}
 
 	/**
 	 * Liefert das NodeObjekt zu einer NodeID. Sollte der Node nicht bekannt
 	 * sein wird <code>null</code> zurckgeliefert. Befindet sich der Knoten der
 	 * die Abfrage ausfhrt im RootMode versucht er den Knoten ber ein Lookup
 	 * finden. Schlgt dieser Versuch ebenfalls fehl wird ein Befehl an den
 	 * Knoten schicken sich neu zu verbinden (noch nicht implementiert).
 	 * 
 	 * @param nid
 	 *            NodeID
 	 * @return Node-Objekt zu angegebenem NodeID oder null wenn
 	 */
 	public Node getNode(long nid) {
 		synchronized (allNodes) {
 			for (Node x : getNodes()) {
 				if (x.getNodeID() == nid) {
 					return x;
 				}
 			}
 			if (isRoot()) {
 				return retrieve(nid);
 			} else {
 				return null;
 			}
 		}
 	}	
 	
 	/**
 	 * Liefert eine Node fr eine bestimmte <code>uid</code>. 
 	 * 
 	 * @param uid
 	 * @return
 	 */
 	public Node getNodeForUID(long uid){
 		synchronized (allNodes) {
 			for (Node x : getNodes()) {
 				if (x.getUserID() == uid) {
 					return x;
 				}
 			}
 			return null;
 		}
 	}
 	
 	/**
 	 * Getter fr ein Set, vom Typ String, fr alle Gruppen.
 	 * 
 	 * @return
 	 */
 	public Set<String> getGroups() {
 		return allGroups;
 	}
 	
 	/**
 	 * Sendet eine Nachricht an den bergeordneten Node, wenn einer existiert.
 	 * 
 	 * @param msg
 	 */
 	private void sendroot(MSG msg) {
 		if (hasParent()) {
 			root_connection.send(msg);
 		}
 	}
 
 	/**
 	 * Versendet Daten vom Typ MSG an bestimmte Nodes oder gibt diese an
 	 * send_file() weiter. Hier wird geprft ob die Dateigre < 5MB (Aufruf der
 	 * send_file()) anderenfalls als Msg-Type Data
 	 * 
 	 * @param nachricht
 	 */
 	private void sendmutlicast(MSG nachricht) {
		DatabaseEngine.getDatabaseEngine().put(nachricht);
 		multi_socket.sendmutlicast(nachricht);
 	}
 
 	/**
 	 * Sendet auf dem Multicast-Socket ein Unicast-Paket.
 	 * 
 	 * @param msg
 	 *            ist die Nachricht die verschickt werden soll (darf nicht
 	 *            grer 64KB sein)
 	 * @param target
 	 */
 	private void sendunicast(MSG msg, Node target) {
		DatabaseEngine.getDatabaseEngine().put(msg);
 		multi_socket.sendunicast(msg, target);
 	}
 	
 	/**
 	 * Sendet eine Nachricht an alle angeschlossenen Verbindungen.
 	 * 
 	 * @param nachricht
 	 */
 	public void sendtcp(MSG nachricht) {
		DatabaseEngine.getDatabaseEngine().put(nachricht);

 		if (hasParent()) {
 			sendroot(nachricht);
 		}
 		if (hasChildren()) {
 			for (ConnectionHandler x : connections) {
 				x.send(nachricht);
 			}
 		}
 	}
 
 	/**
 	 * Sendet eine Nachricht an alle angeschlossenen Verbindungen, auer an die
 	 * mitgelieferte Verbindung.
 	 * 
 	 * @param msg
 	 * @param ch
 	 */
 	private void sendtcpexcept(MSG msg, ConnectionHandler ch) {

 		if (hasParent() && root_connection != ch) {
 			root_connection.send(msg);
 		}
 		if (hasChildren()) {
 			sendchild(msg, ch);
 		}
 	}
 	
 	/**
 	 * Sendet eine Nachricht an alle angeschlossenen Child-Verbindungen, auer
 	 * an die mitgelieferte Verbindung.
 	 * 
 	 * @param msg
 	 * @param ch
 	 */
 	private void sendchild(MSG msg, ConnectionHandler ch) {
		DatabaseEngine.getDatabaseEngine().put(msg);

 		for (ConnectionHandler x : connections) {
 			if (x != ch || ch == null) {
 				x.send(msg);
 			}
 		}
 	}
 
 	/**
 	 * Versendet Datein ber eine TCP-Direktverbindung wird nur von send()
 	 * aufgerufen nachdem festgestellt wurde, dass nachicht > 5MB
 	 * 
 	 * @param datei
 	 * @param receiver
 	 */
 	public void send_file(final File datei, final long receiver) {
 		if (datei.isFile() && datei.exists() && datei.canRead()
 				&& datei.length() > 0) {
 			new Thread(new Runnable() {
 				public void run() {
 					// Erstelle das Parameter Objekt fr die Dateibertragung
 					final FileTransferData tmp_FR = new FileTransferData(datei,
 							datei.length(), meinNode, getNode(receiver));
 					// Wenn Datei unterhalb des Schwellwerts liegt: Als
 					// Nachricht verschicken...
 					if (datei.length() < Config.getConfig().getMaxFileSize()) {
 						try {
 							routesend(new MSG(tmp_FR));
 						} catch (IOException e1) {
 							LogEngine.log(e1);
 						}
 					} else {
 						// Sonst: Direkt bertragen...
 						// Server Thread
 						new Thread(new Runnable() {
 							public void run() {
 								// Datei holen, Socket ffnen
 								try (final BufferedInputStream bis = new BufferedInputStream(
 										new FileInputStream(datei));
 										final ServerSocket f_server = new ServerSocket(
 												0)) {
 									// Warten
 									Socket client = null;
 									f_server.setSoTimeout((int) Config
 											.getConfig()
 											.getFileTransferTimeout());
 									synchronized (tmp_FR) {
 										tmp_FR.server_port = f_server
 												.getLocalPort();
 										tmp_FR.notify();
 									}
 									
 									// Server Close Thread
 									new Thread(new Runnable() {
 										public void run() {
 											MSG tmp_msg = angler
 													.fishfor(
 															NachrichtenTyp.SYSTEM,
 															MSGCode.FILE_TCP_ABORT,
 															tmp_FR.getReceiver_nid(),
 															tmp_FR.hashCode(),
 															true,
 															Config.getConfig()
 																	.getFileTransferTimeout());
 											if (tmp_msg != null) {
 												try {
 													GUI.getGUI()
 															.info("User "
 																	+ tmp_FR.receiver
 																			.getAlias()
 																	+ "has denied recieving the file: "
 																	+ tmp_FR.datei
 																			.getName(),
 																	tmp_FR.receiver
 																			.getUserID(),
 																	0);
 													f_server.close();
 												} catch (IOException e) {
 												}
 											}
 										}
 									}).start();
 									
 									// Verbindung anbieten
 									client = f_server.accept();
 									try {
 										f_server.close();
 									} catch (Exception e) {
 									}
 									// bertragen
 									if (client != null && client.isConnected()
 											&& !client.isClosed()) {
 										BufferedOutputStream bos = new BufferedOutputStream(
 												client.getOutputStream());
 										long infoupdate = System
 												.currentTimeMillis()
 												+ Config.getConfig()
 														.getFileTransferInfoInterval();
 										long transmitted = 0;
 										byte[] cup = new byte[65535];
 										int len = -1;
 										while ((len = bis.read(cup)) != -1) {
 											bos.write(cup, 0, len);
 											transmitted += len;
 											if (System.currentTimeMillis() > infoupdate) {
 												infoupdate = System
 														.currentTimeMillis()
 														+ Config.getConfig()
 																.getFileTransferInfoInterval();
 												GUI.getGUI()
 														.info(tmp_FR.datei
 																.getName()
 																+ "("
 																+ ((transmitted * 100) / tmp_FR.size)
 																+ "%)",
 																tmp_FR.sender
 																		.getUserID(),
 																0);
 											}
 										}
 										bos.flush();
 										bos.close();
 
 										GUI.getGUI().info(
 												tmp_FR.datei.getName()
 														+ " Done",
 												tmp_FR.sender.getUserID(), 0);
 									}
 									// Ergebnis melden
 								} catch (FileNotFoundException e) {
 									LogEngine.log("FileTransfer",
 											e.getMessage(), LogEngine.ERROR);
 								} catch (SocketTimeoutException e) {
 									LogEngine.log("FileTransfer", "Timed Out",
 											LogEngine.ERROR);
 									GUI.getGUI()
 											.info("User "
 													+ tmp_FR.receiver
 															.getAlias()
 													+ " has not answered in time. Connection Timedout",
 													tmp_FR.receiver.getUserID(),
 													0);
 								} catch (SocketException e) {
 									LogEngine.log("FileTransfer", "Aborted",
 											LogEngine.ERROR);
 								} catch (IOException e) {
 									LogEngine.log("FileTransfer", e);
 									GUI.getGUI()
 											.info("Transmission-Error, if this keeps happening buy a USB-Stick",
 													tmp_FR.receiver.getUserID(),
 													0);
 								}
 							}
 						}).start();
 
 						// Warten bis der Server-Thread fertig ist...
 						synchronized (tmp_FR) {
 							try {
 								if (tmp_FR.server_port == -2)
 									tmp_FR.wait();
 							} catch (InterruptedException e) {
 							}
 						}
 						// ... send FileTransferRequest
 						MSG request = new MSG(tmp_FR, MSGCode.FILE_TCP_REQUEST,
 								tmp_FR.getReceiver_nid());
 						routesend(request);
 					}
 				}
 			}).start();
 		}
 	}
 
 	/**
 	 * Private Klasse zum Dateiempfang, wenn der Dateiempfang aktiviert ist.
 	 * 
 	 * @param data_paket
 	 */
 	private void recieve_file(final MSG data_paket) {
 		Object[] tmp = (Object[]) data_paket.getData();
 		FileTransferData tmp_file = (FileTransferData) tmp[0];
 		final File destination;
 		if (!Config.getConfig().getDisableFileTransfer()
 				&& ((destination = ce.request_File(tmp_file)) != null)) {
 			tmp_file.accepted = true;
 			new Thread(new Runnable() {
 				public void run() {
 					try {
 						data_paket.save(destination);
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 				}
 			}).start();
 		} else {
 			tmp_file.accepted = false;
 		}
 		MSG reply = new MSG(tmp_file, MSGCode.FILE_RECIEVED,
 				data_paket.getSender());
 		routesend(reply);
 	}
 
 	/**
 	 * ROOT_DISCOVERY senden.
 	 */
 	private void discover() {
 		new Thread(new Runnable() {
 			public void run() {
 				sendmutlicast(new MSG(meinNode, MSGCode.ROOT_DISCOVERY));
 				rootMe = new Thread(new RootMe());
 				rootMe.start();
 			}
 		}).start();
 	}
 
 	/**
 	 * Antwort auf ROOT_DISCOVERY senden.
 	 * 
 	 * @param quelle
 	 */
 	private void sendDiscoverReply(Node quelle) {
 		LogEngine.log(this, "sending Replay to " + quelle.toString(),
 				LogEngine.INFO);
 		sendunicast(new MSG(getBestNode(), MSGCode.ROOT_REPLY), quelle);
 	}
 
 	/**
 	 * Alle verbundenen Nodes benachrichtigen.
 	 */
 	private void updateNodes() {
 		synchronized (allNodes) {
 			allNodes.clear();
 			allNodes.add(getMe());
 			allNodes.addAll(getChilds());
 			allNodes.notifyAll();
 		}
 		if (hasParent()) {
 			sendroot(new MSG(getNodes(), MSGCode.REPORT_ALLNODES));
 		}
 	}
 
 // Ggf. fr die weitere Entwicklung bentigt.
 //	/**
 //	 * Kommentar!
 //	 */
 //	private void pollChilds() {
 //		for (ConnectionHandler x : connections) {
 //			x.send(new MSG(null, MSGCode.POLL_CHILDNODES));
 //		}
 //	}
 
 	/**
 	 * Getter fr ein Set, vom Typ Nodes, aller ChildNodes.
 	 * 
 	 * @return
 	 */
 	private Set<Node> getChilds() {
 		Set<Node> rck = new HashSet<Node>();
 		for (ConnectionHandler x : connections)
 			rck.addAll(x.getChildren());
 		return rck;
 	}
 
 	/**
 	 * Stelle Verbindung mit diesem <code>NODE</code> her!!!!
 	 * 
 	 * @param knoten
 	 *            der Knoten
 	 * @throws IOException
 	 *             Wenn der hergestellte Socket
 	 */
 	private void connectTo(Node knoten) {
 		try {
 			root_connection = ConnectionHandler.connectTo(knoten);
 			if (root_connection != null) {
 				setRootMode(false);
 				setGroup(myGroups);
 				sendroot(new MSG(getMe()));
 				sendroot(new MSG(myGroups, MSGCode.GROUP_REPLY));
 				sendroot(new MSG(null, MSGCode.POLL_ALLNODES));
 				sendroot(new MSG(null, MSGCode.GROUP_POLL));
 			}
 		} catch (IOException e) {
 			LogEngine.log(e);
 		}
 	}
 
 	/**
 	 * Informiert alle angeschlossenen Nodes ber das unmittelbar bevorstehende
 	 * Herunterfahren, meldet sich bei den abonierten Gruppen ab und schliesst
 	 * den Multicast-Socket.
 	 */
 	@SuppressWarnings("deprecation")
 	public void disconnect() {
 		online = false;
 		connectionsAcceptBot.stop();
 //		multicastRecieverBot.stop();
 		sendtcp(new MSG(meinNode, MSGCode.NODE_SHUTDOWN));
 		sendroot(new MSG(myGroups,MSGCode.GROUP_LEAVE));
 		if(root_connection!=null)root_connection.disconnect();
 		for (final ConnectionHandler con : connections)
 			(new Thread(new Runnable() {
 				public void run() {
 					con.disconnect();
 				}
 			})).start();// Threadded Disconnect fr jede Leitung
 		multi_socket.close();
 		try {
 			server_socket.close();
 		}
 		catch (IOException e) {
 			LogEngine.log(e);
 		}
 	}
 
 	/**
 	 * Entfernt eine Verbindung wieder.
 	 * 
 	 * @param conn
 	 */
 	public void remove(ConnectionHandler conn) {
 		LogEngine.log(conn, "removing");
 		if (conn == root_connection) {
 			LogEngine.log(this, "Lost Root", LogEngine.INFO);
 			root_connection = null;
 			if (online) {
 				updateNodes();
 				//setGroup(myGroups); //FIXME: prfen wo myGroups=allGroups den meisten macht.
 				discover();
 			}
 		}
 		else {
 			LogEngine.log(this, "Lost Child", LogEngine.INFO);
 			connections.remove(conn);
 			sendtcp(new MSG(conn.getChildren(), MSGCode.CHILD_SHUTDOWN));
 			updateMyGroups();
 			allnodes_remove(conn.getChildren());
 		}
 		//updateNodes();
 	}
 
 	/**
 	 * Sendet eine ROOT_ANNOUNCE und initiert so die Neuverhandlung der Root.
 	 */
 	private void sendRA() {
 		MSG ra= new MSG(meinNode, MSGCode.ROOT_ANNOUNCE,getNodes().size());
 		sendmutlicast(ra);
 		root_claims_stash.add(ra);
 	}
 
 	/**
 	 * TODO: Kommentar! 
 	 * 
 	 * @param paket
 	 */
 	private void handleRootClaim(MSG paket) {
 		if(paket!=null) {
 			// ndert den Zeitstempel auf die tatschliche Empfangszeit
 			paket.reStamp();
 			root_claims_stash.offer(paket);
 		}
 		claimRoot();
 	}
 
 	/**
 	 * Hier wird das Paket verarbeitet und weitergeleitet. Diese Methode wird
 	 * ausschlielich vom MulticastSocketHandler aufgerufen.
 	 * 
 	 * @param paket
 	 *            Das empfangene MulticastPaket
 	 */
 	public void handleMulticast(MSG paket) {
 		LogEngine.log(this, "handling [MC]", paket);
 		if (angler.check(paket)) {
 			return;
 		}
 		if (online && (paket.getTyp() == NachrichtenTyp.SYSTEM)) {
 			switch (paket.getCode()) {
 			case ROOT_REPLY:
 				if (!hasParent()) {
 					connectTo((Node) paket.getData());
 				}
 				break;
 			case ROOT_DISCOVERY:
 				if (isRoot()) {
 					sendDiscoverReply((Node) paket.getData());
 				}
 				break;
 			case ROOT_ANNOUNCE:
 				if (!hasParent()) {
 					handleRootClaim(paket);
 				}
 				break;
 			case NODE_LOOKUP:
 				if ((long) paket.getData() == meinNode.getNodeID()) {
 					sendroot(new MSG(meinNode));
 				}
 				break;
 			case ALIAS_UPDATE:
 				updateAlias((String) paket.getData(), paket.getSender());
 				break;
 			case CMD_RECONNECT:
 				long payload = (Long) paket.getData();
 				if ((payload == nodeID) || (payload == -1337)) {
 					if (root_connection != null)
 						root_connection.close();
 				}
 				break;
 			case BACKUP_SERVER_OFFER:
 				ConfigData tmp = (ConfigData) paket.getData();
 				Config.getConfig().setBackupDBIP(tmp.getBackupDBIP());
 				Config.getConfig().setBackupDBPort(tmp.getBackupDBPort());
 				Config.getConfig().setBackupDBUser(tmp.getBackupDBUser());
 				Config.getConfig().setBackupDBPw(tmp.getBackupDBPw());
 				Config.getConfig().setBackupDBDatabasename(
 						tmp.getBackupDBDatabasename());
 				Config.write();
 				break;
 			default:
 				LogEngine.log(this, "handling [MC]:undefined", paket);
 			}
 		}
 	}
 
 	/**
 	 * Hier wird das Paket verarbeitet und weitergeleitet. Diese Methode wird
 	 * ausschlielich von den ConnectionHandlern aufgerufen um empfangene Pakete
 	 * verarbeiten zu lassen.
 	 * 
 	 * @param paket
 	 *            zu verarbeitendes Paket
 	 * @param quelle
 	 *            Quelle des Pakets
 	 */
 	@SuppressWarnings("unchecked")
 	public void handle(MSG paket, ConnectionHandler quelle) {
 		LogEngine.log(this, "handling[" + quelle + "]", paket);
 		if (angler.check(paket)) {
 			return;
 		}
 		if((paket.getEmpfnger() != -1) && (paket.getEmpfnger() != nodeID)) {
 			routesend(paket);
 		}
 		else {
 			switch (paket.getTyp()) {
 			case PRIVATE:
 				if(!ce.is_ignored(paket.getSender())) ce.put(paket);
 				break;
 			case GROUP:
 				groupRouteSend(paket,quelle);
 				if(!ce.is_ignored(paket.getSender())) ce.put(paket);
 				break;
 			case SYSTEM:
 				DatabaseEngine.getDatabaseEngine().put(paket);
 				switch (paket.getCode()) {
 				case NODE_UPDATE:
 					allnodes_add((Node) paket.getData());
 					sendtcpexcept(paket, quelle);
 					break;
 
 				case POLL_ALLNODES:
 					if (quelle != root_connection) {
 						quelle.send(new MSG(getNodes(), MSGCode.REPORT_ALLNODES));
 					}
 					break;
 				case REPORT_ALLNODES:
 					allnodes_set((Set<Node>) paket.getData());
 					break;
 				case POLL_CHILDNODES:
 					if (quelle == root_connection) {
 						Set<Node> tmp = new HashSet<Node>();
 						for (ConnectionHandler x : connections)
 							tmp.addAll(x.getChildren());
 						sendroot(new MSG(tmp, MSGCode.REPORT_CHILDNODES));
 					}
 					break;
 				case REPORT_CHILDNODES:
 					if (quelle != root_connection) {
 						quelle.setChildren((Collection<Node>) paket.getData());
 					}
 					break;
 				case NODE_SHUTDOWN:
 					allnodes_remove(quelle.getChildren());
 					quelle.close();
 					break;
 				case CHILD_SHUTDOWN:
 					if (quelle != root_connection) {
 						quelle.removeChildren((Collection<Node>) paket.getData());
 					}
 					allnodes_remove((Collection<Node>) paket.getData());
 					sendtcpexcept(paket, quelle);
 					break;
 				case GROUP_JOIN:
 					quelle.add((Collection<String>) paket.getData());
 					joinGroup((Collection<String>) paket.getData(), quelle);
 					break;
 				case GROUP_LEAVE:
 					quelle.remove((Collection<String>) paket.getData());
 					leaveGroup((Collection<String>) paket.getData(), quelle);
 					break;
 				case GROUP_ANNOUNCE:
 					if (addGroup((Collection<String>) paket.getData())) {
 						sendchild(paket, null);
 					}
 					break;
 				case GROUP_EMPTY:
 					if (removeGroup((Collection<String>) paket.getData())) {
 						sendchild(paket, null);
 					}
 					break;
 				case GROUP_POLL:
 					quelle.send(new MSG(allGroups, MSGCode.GROUP_REPLY));
 					break;
 				case GROUP_REPLY:
 					Set<String> groups = (Set<String>) paket.getData();
 					quelle.add(groups);
 					updateMyGroups();
 					addGroup(groups);
 					break;
 				case FILE_TCP_REQUEST:
 					FileTransferData tmp = (FileTransferData) paket.getData();
 					if(!ce.is_ignored(paket.getSender())) recieve_file(tmp);
 					break;
 				case FILE_RECIEVED:
 					if(!ce.is_ignored(paket.getSender())) ce.inform((FileTransferData) paket.getData());
 					break;
 				case NODE_LOOKUP:
 					Node tmp_node = null;
 					if ((tmp_node = getNode((long) paket.getData())) != null) {
 						quelle.send(new MSG(tmp_node));
 					} else {
 						sendroot(paket);
 					}
 					break;
 				case PATH_PING_REQUEST:
 					if (paket.getEmpfnger() == nodeID) {
 						routesend(new MSG(paket.getData(),
 								MSGCode.PATH_PING_RESPONSE, paket.getSender()));
 					} else {
 						routesend(paket);
 					}
 					break;
 				case PATH_PING_RESPONSE:
 					if (paket.getEmpfnger() == nodeID) {
 						routesend(new MSG(paket.getData(),
 								MSGCode.PATH_PING_RESPONSE, paket.getSender()));
 					} else {
 						routesend(paket);
 					}
 					break;
 				case TREE_DATA_POLL:
 					sendroot(new MSG(getTree(), MSGCode.TREE_DATA));
 					break;
 				case CMD_SHUTDOWN:
 					System.exit(0);
 					break;
 				default:
 					LogEngine.log(this, "handling[" + quelle + "]:undefined",
 							paket);
 					break;
 				}
 				break;
 			case DATA:
 				if (paket.getEmpfnger() != nodeID) {
 					routesend(paket);
 				} else {
 					if(!ce.is_ignored(paket.getSender())) recieve_file(paket);
 				}
 				break;
 			default:
 			}
 		}
 	}
 	
 	/**
 	 * TODO: berprfen, ausformulieren!
 	 * Verwaltung des Dateiempfang, abhngig von der Konfiguration des Benutzers.  
 	 * 
 	 * @param tmp
 	 */
 	private void recieve_file(final FileTransferData tmp) {
 		if(!Config.getConfig().getDisableFileTransfer()) {
 		new Thread(new Runnable() {
 			public void run() {
 				long until = System.currentTimeMillis()+Config.getConfig().getFileTransferTimeout()-1000;
 				final File destination = ce.request_File(tmp);
 				if(System.currentTimeMillis()<until) {
 				tmp.accepted = (destination != null);
 				MSG reply;
 				if(tmp.accepted) {
 					reply = new MSG(tmp, MSGCode.FILE_RECIEVED,tmp.getSender_nid());
 				} else {
 					reply = new MSG(tmp.hashCode(), MSGCode.FILE_TCP_ABORT,tmp.getSender_nid());
 				}
 				routesend(reply);
 				if (destination != null) {
 					Socket data_con = null;
 					for (InetAddress ip : tmp.sender.getSockets()) {
 						if (!meinNode.getSockets().contains(ip)) {
 							try {
 								data_con = new Socket(ip, tmp.server_port);
 							} catch (IOException e) {
 								e.printStackTrace();
 							}
 						}
 					}
 					if (data_con != null) {
 						try (final BufferedInputStream bis = new BufferedInputStream(data_con.getInputStream()); final BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(destination))) {
 							long infoupdate = System.currentTimeMillis()+Config.getConfig().getFileTransferInfoInterval();
 							long transmitted=0;
 							byte[] cup = new byte[65535];
 							int len = -1;
 							while ((len = bis.read(cup)) != -1) {
 								bos.write(cup, 0, len);
 								transmitted+=len;
 								if(System.currentTimeMillis()>infoupdate) {
 									infoupdate = System.currentTimeMillis()+Config.getConfig().getFileTransferInfoInterval();
 									GUI.getGUI().info(tmp.datei.getName()+"("+((transmitted*100)/tmp.size)+"%)", tmp.sender.getUserID(), 0);
 								}
 							}
 							bos.flush();
 							bos.close();
 							data_con.close();
 							GUI.getGUI().info(tmp.datei.getName()+" Done", tmp.sender.getUserID(), 0);
 						}
 						catch (SocketException e) {
 							GUI.getGUI().info(tmp.datei.getName()+" Done", tmp.sender.getUserID(), 0);
 						}
 						catch (IOException e) {
 							e.printStackTrace();
 						}
 					}
 				}
 			}}
 		}).start();
 		}
 		else {
 			tmp.accepted=false;
 			routesend(new MSG(tmp.hashCode(), MSGCode.FILE_TCP_ABORT,tmp.getSender_nid()));
 		}
 	}
 
 	/**
 	 * TODO: Kommentar!
 	 * 
 	 * @param paket
 	 */
 	public void routesend(MSG paket) {
 		long empfnger = paket.getEmpfnger();
 		for (ConnectionHandler con : connections) {
 			if(con.hasChild(empfnger)) {
 				con.send(paket);
 				return;
 			}
 		}
 		if(hasParent()) {
 			sendroot(paket);
 		} 
 		// Dieser Node ist Root des Baums und ihr ist nicht bekannt wo der
 		// Empfnger ist.
 		else if(isRoot()) {
 			// Versuche dem Empfnger zu finden...
 			Node tmp = retrieve(empfnger);
 			// ...und das Paket zuzustellen.
 			if(tmp!=null) {
 				routesend(paket);
 			}
 			else {
 				// Weist alle Nodes an diesen Empfnger zu entfernen und alle
 				// offenen Fenster zu schliessen.
 				sendchild(new MSG(empfnger,MSGCode.NODE_SHUTDOWN),null);
 			}
 		}
 	}
 	
 	/**
 	 * TODO: Kommentar!
 	 * 
 	 * @param paket
 	 * @param quelle
 	 */
 	public void groupRouteSend(MSG paket,ConnectionHandler quelle) {
 		String gruppe = paket.getGroup();
 		for (ConnectionHandler con : connections) {
 			if((quelle!=con)&&con.getGroups().contains(gruppe)) {
 				con.send(paket);
 			}
 		}
 		if(hasParent()&&(root_connection!=quelle)) {
 			sendroot(paket);
 		}
 	}
 
 	/**
 	 * Die eigenen abonierten Gruppen aktualisieren, hinzufgen oder entfernen.
 	 * 
 	 * @return
 	 */
 	private boolean updateMyGroups() {
 		Set<String> aktuell = computeGroups();
 		synchronized (myGroups) {
 
 			if (aktuell.hashCode() != myGroups.hashCode()) {
 				Set<String> dazu = new HashSet<String>(aktuell);
 				dazu.removeAll(myGroups);
 				if(dazu.size()>0) {
 					sendroot(new MSG(dazu, MSGCode.GROUP_JOIN));
 					if(addGroup(dazu)) {
 						sendchild(new MSG(dazu, MSGCode.GROUP_ANNOUNCE),null);
 					}
 				}
 				Set<String> weg = new HashSet<String>(myGroups);
 				weg.removeAll(aktuell);
 				if(weg.size()>0) {
 					sendroot(new MSG(weg, MSGCode.GROUP_LEAVE));
 					if(isRoot()) {
 						if(removeGroup(weg)) {
 							sendchild(new MSG(weg, MSGCode.GROUP_EMPTY),null);
 						}
 					}
 				}
 				myGroups.clear();
 				myGroups.addAll(aktuell);
 				return true;
 			}
 			return false;
 		}
 	}
 
 	/**
 	 * TODO: Kommentar!
 	 * 
 	 * @param data
 	 */
 	private void allnodes_remove(Collection<Node> data) {
 		synchronized (allNodes) {
 			int hash = allNodes.hashCode();
 			allNodes.removeAll(data);
 			allNodes.add(meinNode);
 			if (allNodes.hashCode() != hash) {
 				allNodes.notifyAll();
 			}
 		}
 	}
 
 // Ggf. fr die weitere Entwicklung bentigt.
 // 
 //	/**
 //	 * Entfernt einen Benutzer aus der Liste.
 //	 * 
 //	 * @param data
 //	 */
 //	private void allnodes_remove(Node data) {
 //		synchronized (allNodes) {
 //			int hash = allNodes.hashCode();
 //			allNodes.remove(data);
 //			if (allNodes.hashCode() != hash) {
 //				allNodes.notifyAll();
 //			}
 //		}
 //	}
 
 // Ggf. fr die weitere Entwicklung bentigt.
 //	/**
 //	 * Fgt einen Benutzer der Liste hinzu.
 //	 * 
 //	 * @param data
 //	 */
 //	private void allnodes_add(Collection<Node> data) {
 //		synchronized (allNodes) {
 //			int hash = allNodes.hashCode();
 //			allNodes.addAll(data);
 //			if (allNodes.hashCode() != hash) {
 //				allNodes.notifyAll();
 //			}
 //		}
 //	}
 	
 	/**
 	 * TODO: Kommentar!
 	 * 
 	 * @param data
 	 */
 	private void allnodes_add(Node data) {
 		synchronized (allNodes) {
 			int hash = allNodes.hashCode();
 			allNodes.add(data);
 			if (allNodes.hashCode() != hash) {
 				allNodes.notifyAll();
 			}
 			DatabaseEngine.getDatabaseEngine().put(data);
 		}
 	}
 	
 	/**
 	 * TODO: Kommentar!
 	 * 
 	 * @param data
 	 */
 	private void allnodes_set(Collection<Node> data) {
 		synchronized (allNodes) {
 			int hash = allNodes.hashCode();
 			allNodes.clear();
 			allNodes.addAll(data);
 			allNodes.add(meinNode);
 			if (allNodes.hashCode() != hash) {
 				allNodes.notifyAll();
 			}
 		}
 		DatabaseEngine.getDatabaseEngine().put(data);
 	}
 	
 	/**
 	 * Starte Lookup fr {@link Node} mit der NodeID <code>nid</code>. Und
 	 * versucht ihn neu zu verbinden zu lassen falls die Verbindung fehl
 	 * schlgt.
 	 * 
 	 * @param nid
 	 *            ID des Nodes
 	 * @return das {@link Node}-Objekt oder <code>null</code> wenn der Knoten
 	 *         nicht gefunden wurde.
 	 */
 	private Node retrieve(long nid) {
 			sendmutlicast(new MSG(nid, MSGCode.NODE_LOOKUP));
 			MSG x = angler.fishfor(NachrichtenTyp.SYSTEM,MSGCode.NODE_UPDATE,nid,null,false,1000);
 			if (x != null) {
 				return (Node) x.getData();
 			} else {
 				LogEngine.log("retriever", "NodeID:["+nid+"] konnte nicht aufgesprt werden und sollte neu Verbinden!!!",LogEngine.ERROR);
 				sendmutlicast(new MSG(nid, MSGCode.CMD_RECONNECT));
 				return null;
 			}
 	}
 
 	/**
 	 * Setter fr den RootMode.
 	 * 
 	 * @param rootmode
 	 */
 	private void setRootMode(boolean rootmode) {
 		this.rootMode = rootmode;
 		GUI.getGUI().setTitle("publicMAIN"+((rootmode)?"[ROOT]":"" ));
 		if(rootmode) setGroup(myGroups) ;
 	}
 	
 	/**
 	 * Getter fr die eigene <code>nid</code>.
 	 * 
 	 * @return
 	 */
 	public long getNodeID() {
 		return nodeID;
 	}
 
 	/**
 	 * Diese Methode tritt einer Gruppe auf einer bestimmten Verbindung bei um
 	 * die Nachrichten der abonierten Gruppe zu empfangen.
 	 * 
 	 * @param gruppen_namen
 	 * @param con
 	 */
 	public void joinGroup(Collection<String> gruppen_namen, ConnectionHandler con) {
 		updateMyGroups();
 	}
 
 	/**
 	 * Verlsst eine Gruppe wieder auf einer bestimmten Verbindung wieder und
 	 * kann die Nachrichten der vorher abonierten Gruppe nicht mehr erhalten.
 	 * 
 	 * @param gruppen_namen
 	 * @param con
 	 */
 	public void leaveGroup(Collection<String> gruppen_namen, ConnectionHandler con) {
 		updateMyGroups();
 	}
 	
 	/**
 	 * TODO: Kommentar!
 	 * 
 	 * @param gruppen_name
 	 * @return
 	 */
 	public boolean removeGroup(Collection<String> gruppen_name) {
 		synchronized (allGroups) {
 			boolean x = allGroups.removeAll(gruppen_name);
 			allGroups.notifyAll();
 			return x;
 		}
 	}
 	
 	/**
 	 * TODO: Kommentar! 
 	 * 
 	 * @param groups
 	 */
 	public void setGroup(Collection<String> groups) {
 		synchronized (allGroups) {
 			allGroups.clear();
 			allGroups.addAll(groups);
 			allGroups.notifyAll();
 		}
 	}
 	
 	/**
 	 * TODO: Kommentar! 
 	 * 
 	 * @param groups
 	 * @return
 	 */
 	public boolean addGroup(Collection<String> groups) {
 		synchronized (allGroups) {
 		boolean x = allGroups.addAll(groups);
 		allGroups.notifyAll();
 		return x;
 		}
 	}
 
 	/**
 	 * TODO: Kommentar! 
 	 * 
 	 * @param gruppen_name
 	 * @return
 	 */
 	public boolean removeMyGroup(String gruppen_name) {
 		synchronized (myGroups) {
 			return myGroups.remove(gruppen_name);
 		}
 	}
 	
 	/**
 	 * TODO: Kommentar! 
 	 * 
 	 * @param gruppen_name
 	 * @return
 	 */
 	public boolean addMyGroup(String gruppen_name) {
 		synchronized (myGroups) {
 			return myGroups.add(gruppen_name);
 		}
 	}
 	
 	/**
 	 * TODO: Kommentar! 
 	 * 
 	 * @return
 	 */
 	public Set<String>computeGroups(){
 		Set<String> tmpGroups = new HashSet<String>();
 		for (ConnectionHandler cur : connections) {
 			tmpGroups.addAll(cur.getGroups());
 		}
 		tmpGroups.addAll(ce.getMyGroups());
 		return tmpGroups;
 	}
 
 	/**
 	 * Den eigenen Alias ndern und dies allen anderen Nodes mitteilen.
 	 */
 	public void updateAlias() {
 		String alias = ce.getAlias();
 		if(online&&(!alias.equals(meinNode.getAlias()))) {
 			sendmutlicast(new MSG(alias, MSGCode.ALIAS_UPDATE));
 			updateAlias(alias,nodeID);
 		}
 	}
 	
 	/**
 	 * TODO: Kommentar! 
 	 */
 	public void pathPing(){
 		for (Node cur : getNodes()) {
 			GUI.getGUI().info(cur.toString() + ":" +pathPing(cur), null, 0);
 			
 		}
 	}
 	
 	/**
 	 * TODO: Kommentar! 
 	 * 
 	 * @param remote
 	 * @return
 	 */
 	public long pathPing(Node remote) {
 		if (remote.equals(meinNode))return 0;
 		else {
 			long currentTimeMillis = System.currentTimeMillis();
 			MSG paket = new MSG(currentTimeMillis, MSGCode.PATH_PING_REQUEST,remote.getNodeID());
 			MSG response = angler.fishfor(NachrichtenTyp.SYSTEM, MSGCode.PATH_PING_RESPONSE, remote.getNodeID(),currentTimeMillis, true, 1000,paket);
 			if(response==null)return -1;
 			else {
 				return (System.currentTimeMillis()-currentTimeMillis);
 			}
 			
 		}
 	}
 	
 	/**
 	 * Genderten Alias der GUI mitteilen, dass der Alias korrekt auf der GUI
 	 * dargestellt wird.
 	 * 
 	 * @param newAlias
 	 * @param nid
 	 * 
 	 * @return
 	 */
 	private boolean updateAlias(String newAlias, long nid) {
 		Node tmp;
 		synchronized (allNodes) {
 			if ((tmp = getNode(nid)) != null) {
 				if (!tmp.getAlias().equals(newAlias)) {
 					tmp.setAlias(newAlias);
 					allNodes.notifyAll();
 					GUI.getGUI().notifyGUI();
 					// Zur Aktualisierung der Datenbank wegschreiben.
 					DatabaseEngine.getDatabaseEngine().put(allNodes);
 					LogEngine.log(this,"User " +tmp.getAlias() + " has changed ALIAS to " + newAlias,LogEngine.INFO);
 					return true;
 				}
 			}
 			return false;
 		}
 	}
 
 	/**
 	 * Debug-Parameter (/debug) fr die Eingabe auf der GUI werden hier
 	 * interpretiert.
 	 * 
 	 * @param command
 	 * @param parameter
 	 */
 	public void debug(String command, String parameter) {
 		switch (command) {
 		case "play":
 			Help.playSound(parameter);
 			break;
 		case "stop":
 			Help.stopSound();
 		case "gc":
 			System.gc();
 			break;
 		case "poll":
 			sendroot(new MSG(null, MSGCode.POLL_ALLNODES));
 			break;
 		case "nup":
 			sendtcp(new MSG(meinNode, MSGCode.NODE_UPDATE));
 			break;
 		case "pingall":
 			pathPing();
 			break;
 		case "_kick":
 			Node tmp = ce.getNodeforAlias(parameter);
 			if (tmp != null) {
 				routesend(new MSG(null, MSGCode.CMD_SHUTDOWN, tmp.getNodeID()));
 			}
 			break;
 		case "maxcon":
 			Config.getConfig().setMaxConnections(Integer.parseInt(parameter));
 			break;
 		case "bestnode":
 			long time = System.currentTimeMillis();
 			GUI.getGUI().info(
 					"Strategie:" + myStrategy.getClass().getSimpleName()
 							+ " MaxConnections:"
 							+ Config.getConfig().getMaxConnections(), null, 0);
 			GUI.getGUI().info(getBestNode().toString(), null, 0);
 			GUI.getGUI().info(
 					"took " + (System.currentTimeMillis() - time)
 							+ " ms to evaluate", null, 0);
 			break;
 		case "strategy":
 			if (parameter.equals("random")) {
 				myStrategy = new RandomStrategy(nodeID);
 			} else if (parameter.equals("breadth")) {
 				myStrategy = new BreadthFirstStrategy();
 			} else if (parameter.startsWith("weighted")
 					&& parameter.split(" ").length == 4) {
 				myStrategy = new WeightedDistanceStrategy(
 						Double.parseDouble(parameter.split(" ")[1]),
 						Integer.parseInt(parameter.split(" ")[2]),
 						Integer.parseInt(parameter.split(" ")[3]));
 			} else {
 				GUI.getGUI().info(
 						"unknown Strategy [random, breadth, weighted 0.5 0 1]",
 						null, 1);
 			}
 			break;
 		case "update":
 			GUI.getGUI().notifyGUI();
 			break;
 		case "conf":
 			Config.writeSystemConfiguration();
 			break;
 		case "tree":
 			showTree();
 			break;
 		case "reconnect_all":
 			sendmutlicast(new MSG(-1337l, MSGCode.CMD_RECONNECT));
 			break;
 		case "poll_bus":
 			multi_socket.discoverBUS();
 		default:
 			LogEngine.log(this, "debug command not found", LogEngine.ERROR);
 			break;
 		}
 	}
 	
 	/**
 	 * Erstellt den Topologie-Baum fr das Debug-Kommando (/debug tree).
 	 * 
 	 * @return
 	 */
 	public Node getTree() {
 		Node root = (Node) meinNode.clone();
 		// Zuerst werden alle Knoten hergestellt...
 		for (final ConnectionHandler con : connections) {
 			Runnable tmp = new Runnable() {
 				public void run() {
 					con.send(new MSG(null, MSGCode.TREE_DATA_POLL));
 				}
 			};
 			MSG polled_tree = angler.fishfor(NachrichtenTyp.SYSTEM,
 					MSGCode.TREE_DATA, null, null, true, Config.getConfig()
 							.getTreeBuildTime(), tmp);
 			if (polled_tree != null) {
 				root.add((Node) polled_tree.getData());
 			}
 		}
 		return root;
 	}
 	
 	/**
 	 * Liefert die <code>uid</code> fr eine <code>nid</code>. 
 	 * 
 	 * @param nid
 	 * @return
 	 */
 	public long getUIDforNID(long nid){
 		Node node = getNode(nid);
 		if (node!=null) {
 			return node.getUserID();
 		} else {
 			return -1;
 		}
 	}
 	
 	/**
 	 * Visualisiert den Topologie-Baum fr das Debug-Kommando (/debug tree).
 	 */
 	public void showTree() {
 	        TreeNode root = getTree();
 	        // Der Wurzelknoten wird dem neuen JTree im Konstruktor bergeben
 	        JTree tree = new JTree( root );
 	        // Ein Frame herstellen, um den Tree anzuzeigen
 	        JFrame frame = new JFrame( "publicMAIN - Topology" );
 	        frame.add( new JScrollPane( tree ));
 	        frame.setIconImage(Help.getIcon("pM_Logo.png").getImage());
 	        frame.setMinimumSize(new Dimension(250, 400));
 	        frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
 	        frame.pack();
 	        frame.setLocationRelativeTo( null );
 	        frame.setVisible( true );
 	}
 
 	/**
 	 * Definiert diesen Node nach einem Timeout als Root wenn bis dahin keine
 	 * Verbindung aufgebaut wurde.
 	 */
 	private final class RootMe implements Runnable {
 		public void run() {
 			if (!online||isRoot()||rootDiscovering) {
 				return;
 			}
 			long until = System.currentTimeMillis() + DISCOVER_TIMEOUT;
 			while (System.currentTimeMillis() < until) {
 				try {
 					Thread.sleep(DISCOVER_TIMEOUT);
 				}
 				catch (InterruptedException e) {
 				}
 			}
 			if (online&&!hasParent()) {
 				LogEngine.log("RootMe", "no Nodes detected: claiming Root", LogEngine.INFO);
 				claimRoot();
 			}
 		}
 	}
 	
 	/**
 	 * TODO: berprfen!
 	 * 
 	 * Selbst die Root-Position beanspruchen.
 	 */
 	private synchronized void claimRoot() {
 		if (rootDiscovering == false) {
 			rootClaimProcessor = new Thread(new RootClaimProcessor());
 			rootClaimProcessor.start();
 		}
 	}
 
 	/**
 	 * TODO: berprfen!
 	 * 
 	 * Wartet eine gewisse Zeit und wertet dann alle gesammelten ROOT_ANNOUNCES
 	 * aus und fordert anschlieend von der neuen Root einen Node zum verbinden
 	 * an, wenn der Node selbst die neue Root ist.
 	 */
 	private final class RootClaimProcessor implements Runnable {
 		public void run() {
 			rootDiscovering = true;
 			LogEngine.log("DiscoverGame", "started", LogEngine.INFO);
 			sendRA();
 			long until = System.currentTimeMillis() + ROOT_CLAIM_TIMEOUT;
 			while (System.currentTimeMillis() < until) {
 				try {
 					Thread.sleep(ROOT_CLAIM_TIMEOUT);
 				} catch (InterruptedException e) {
 				}
 			}
 			List<MSG> ra_replies = new ArrayList<MSG>();
 			ra_replies.addAll(root_claims_stash);
 			Collections.sort(ra_replies);
 			long deadline = ra_replies.get(0).getTimestamp() + 2
 					* ROOT_CLAIM_TIMEOUT;
 
 			Node toConnectTo = meinNode;
 			long maxPenunte = getNodes().size();
 			for (MSG x : root_claims_stash) {
 				if (x.getTimestamp() <= deadline) {
 					long tmp_size = x.getEmpfnger();
 					Node tmp_node = (Node) x.getData(); // Cast Payload in ein
 														// Object Array und das
 														// 2. Object dieses
 														// Arrays in einen Node
 					if (tmp_size > maxPenunte
 							|| ((tmp_size == maxPenunte) && (tmp_node
 									.getNodeID() > toConnectTo.getNodeID()))) {
 						toConnectTo = tmp_node;
 						maxPenunte = tmp_size;
 					}
 				}
 			}
 			LogEngine.log("DiscoverGame", "Finished:"
 					+ ((toConnectTo != meinNode) ? "lost" : "won") + "("
 					+ root_claims_stash.size() + " participants)",
 					LogEngine.INFO);
 			if (toConnectTo == meinNode) {
 				setRootMode(true);
 			} else {
 				discover(); // another root won and should be
 							// answeringconnectTo(toConnectTo);
 			}
 			root_claims_stash.clear();
 			rootDiscovering = false;
 		}
 	}
 
 	/**
 	 *	TODO: Kommentieren! 
 	 */
 	private final class ConnectionsAccepter implements Runnable {
 		public void run() {
 			if(connections==null||server_socket==null) {
 				return;
 			}
 			while (online) {
 				LogEngine.log("ConnectionsAccepter", "Listening on Port:" + server_socket.getLocalPort(), LogEngine.INFO);
 				try {
 					ConnectionHandler tmp = new ConnectionHandler(server_socket.accept());
 					connections.add(tmp);
 					tmp.isConnected();
 				}
 				catch (IOException e) {
 					LogEngine.log(e);
 				}
 			}
 		}
 	}
 }
