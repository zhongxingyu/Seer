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
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import javax.swing.JFrame;
 import javax.swing.JScrollPane;
 import javax.swing.JTree;
 import javax.swing.WindowConstants;
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
  * verwaltet die bestehenden Verbindungen, sendet Nachrichten und Dateien und ist
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
 	private final Node meinNode;
 	// Zeiger auf die ChatEngine.
 	private final ChatEngine ce;
 	// Haken zum fangen von Nachrichten.
 	private final Hook angler = new Hook();
 	// Server-Socket fr eingehende Verbindungen (Passiv/Childs).
 	private final ServerSocket server_socket;
 	// TCP-Socket zur Verbindung mit anderen Nodes (Aktiv/Parent/Root).
 	private ConnectionHandler root_connection;
 	// Multicast/Broadcat UDP-Socket zur Verbindungsaushandlung.
 	private final MulticastConnectionHandler multi_socket;
 	// Liste bestehender Child-Verbindungenin eigener Hll-Klasse.
 	public List<ConnectionHandler> connections;
 	// Set, vom Typ String, aller Gruppen.
 	private final Set<String> allGroups = new HashSet<String>();
 	// Set, vom Typ String, aller <u>abonierten</u> Gruppen und aller
 	// untergeordneter Nodes.
 	private final Set<String> myGroups = new HashSet<String>();
 	// Warteschlange (Queue) fr Bewerberpakete bei Neuverhandelung der Root.
 	private final BlockingQueue<MSG> root_claims_stash;
 	// Set, vom Typ Node, aller dieser NodeEngine bekannten Nodes.
 	private final Set<Node> allNodes;
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
 	private final Thread connectionsAcceptBot = new Thread(new ConnectionsAccepter());
 	// Dieser Thread bestimmt nach einer Verzgerung diesen
 	// Node zum Root, er wird durch einen Discover gestartet.
 	private Thread rootMe;
 	// Der Thread sammelt und wertet ROOT_ANNOUNCES aus, er wird beim Empfang
 	// oder Versand eines ROOT_ANNOUNCE gestartet.
 	private Thread rootClaimProcessor;
 	private final Thread neMaintainer;
 	private BestNodeStrategy myStrategy;
 
 	/**
 	 * Konstruktor fr die NodeEngine.
 	 * 
 	 * @param parent
 	 * 		die dazugehrige ChatEngine.
 	 * @throws IOException
 	 * 		wirft IOExceptions, wenn der Serverport nicht geffnet werden kann.
 	 */
 	public NodeEngine(final ChatEngine parent) throws IOException {
 
 		this.allNodes = Collections.synchronizedSet(new HashSet<Node>());
 		this.connections = new CopyOnWriteArrayList<ConnectionHandler>();
 
 
 		this.root_claims_stash = new LinkedBlockingQueue<MSG>();
 
 		this.nodeID = ((long) (Math.random() * Long.MAX_VALUE));
 		ne = this;
 		this.ce = parent;
 		this.online = true;
 
 		this.server_socket = new ServerSocket(0);
 		this.multi_socket = MulticastConnectionHandler.getMC();
 		this.multi_socket.registerNodeEngine(this);
 
 		this.meinNode = new Node(this.server_socket.getLocalPort(), this.nodeID,this.ce.getUserID(), System.getProperty("user.name"), this.ce.getAlias());
 		this.allNodes.add(this.meinNode);
 		DatabaseEngine.getDatabaseEngine().put(this.meinNode);
 
 		this.myStrategy = new BreadthFirstStrategy();
 
 		this.connectionsAcceptBot.start();
 
 		this.neMaintainer = new Thread(new Maintainer());;
 		this.neMaintainer.start();
 
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
 		return this.meinNode;
 	}
 
 	/**
 	 * Getter fr den optimalen Node zum anfgen eines weiteren, abhngig von
 	 * der gewhlten Strategie.
 	 * 
 	 * @return Node
 	 */
 	private Node getBestNode() {
 		return this.myStrategy.getBestNode();
 	}
 
 	/**
 	 * Gibt an, ob dieser Knoten Vater von anderen Knoten im topologischen Baum ist.
 	 * 
 	 * @return
 	 *  <code>true</code> Wenn dieser Knoten verbundene Kindknoten hat
 	 */
 	private boolean hasChildren() {
 		return this.connections.size() > 0;
 	}
 
 	/**
 	 * Gibt <code>true</code> zurck, wenn die laufende NodeEngine Root ist und
 	 * <code>false</code> wenn nicht.
 	 * 
 	 * @return boolean
 	 */
 	public boolean isRoot() {
 		return this.rootMode && !hasParent();
 	}
 
 	/**
 	 * Gibt <code>true</code> zurck, wenn die Anwendung den Status online hat
 	 * und <code>false</code> wenn nicht.
 	 * 
 	 * @return boolean
 	 */
 	public boolean isOnline() {
 		return this.online;
 	}
 
 	/**
 	 * Prft, ob dieser Node einen bergeordneten Node hat. Gibt
 	 * <code>true</code> zurck wenn ja und <code>false</code> wenn nicht.
 	 * 
 	 * @return boolean
 	 */
 	public boolean hasParent() {
 		return ((this.root_connection != null) && this.root_connection.isConnected());
 	}
 
 	/**
 	 * Getter fr ein Set von allen verbundenen Nodes.
 	 * 
 	 * @return boolean
 	 */
 	public Set<Node> getNodes() {
 		synchronized (this.allNodes) {
 			return this.allNodes;
 		}
 	}
 
 	/**
 	 * Liefert das NodeObjekt zu einer NodeID. Sollte der Node nicht bekannt
 	 * sein, wird <code>null</code> zurckgeliefert. Befindet sich der Knoten, der
 	 * die Abfrage ausfhrt, im RootMode, versucht er den Knoten ber ein Lookup
 	 * zu finden. Schlgt dieser Versuch ebenfalls fehl, wird ein Befehl an den
 	 * Knoten geschickt, sich neu zu verbinden (noch nicht implementiert).
 	 * 
 	 * @param nid
 	 *            NodeID
 	 * @return Node-Objekt zu angegebenem NodeID oder null wenn
 	 */
 	public Node getNode(final long nid) {
 		synchronized (this.allNodes) {
 			for (final Node x : getNodes()) {
 				if (x.getNodeID() == nid)
 					return x;
 			}
 			if (isRoot())
 				return retrieve(nid);
 			else
 				return null;
 		}
 	}
 
 	/**
 	 * Liefert eine Node fr eine bestimmte <code>uid</code>.
 	 * 
 	 * @param uid UserID des zu suchenden Nutzers
 	 * @return zur UserID zugehriges NodeObjekt
 	 */
 	public Node getNodeForUID(final long uid){
 		synchronized (this.allNodes) {
 			for (final Node x : getNodes()) {
 				if (x.getUserID() == uid)
 					return x;
 			}
 			return null;
 		}
 	}
 
 	/**
 	 * Getter fr ein Set, vom Typ String, fr alle Gruppen.
 	 * 
 	 * @return Set von Gruppennamen
 	 */
 	public Set<String> getGroups() {
 		return this.allGroups;
 	}
 
 	/**
 	 * Sendet eine Nachricht an den bergeordneten Node, wenn einer existiert.
 	 * 
 	 * @param nachricht das zu versende Nachrichtenobjekt
 	 */
 	private void sendroot(final MSG nachricht) {
 		if (hasParent()) {
 			this.root_connection.send(nachricht);
 		}
 	}
 
 	/**
 	 * Versendet Daten vom Typ MSG an bestimmte Nodes oder gibt diese an
 	 * send_file() weiter. Hier wird geprft, ob die Dateigre < 5MB (Aufruf der
 	 * send_file()) anderenfalls als Msg-Type Data
 	 * 
 	 * @param nachricht das zu versende Nachrichtenobjekt
 	 */
 	private void sendmutlicast(final MSG nachricht) {
 		this.multi_socket.sendmutlicast(nachricht);
 	}
 
 	/**
 	 * Sendet auf dem Multicast-Socket ein Unicast-Paket.
 	 * 
 	 * @param msg
 	 *            ist die Nachricht, die verschickt werden soll (darf nicht
 	 *            grer 64KB sein)
 	 * @param target Zielnode fr Unicastpaket
 	 */
 	private void sendunicast(final MSG msg, final Node target) {
 		this.multi_socket.sendunicast(msg, target);
 	}
 
 	/**
 	 * Sendet eine Nachricht an alle angeschlossenen Verbindungen.
 	 * 
 	 * @param nachricht Zu sendendes Nachrichtenobjekt
 	 */
 	public void sendtcp(final MSG nachricht) {
 		if (hasParent()) {
 			sendroot(nachricht);
 		}
 		if (hasChildren()) {
 			for (final ConnectionHandler x : this.connections) {
 				x.send(nachricht);
 			}
 		}
 	}
 
 	/**
 	 * Sendet eine Nachricht an alle angeschlossenen Verbindungen, auer an die
 	 * mitgelieferte Verbindung.
 	 * 
 	 * @param msg zu versendendes Nachrichtenobjekt
 	 * @param ch Beim Versand auszuschlieende Verbindung
 	 */
 	private void sendtcpexcept(final MSG msg, final ConnectionHandler ch) {
 		if (hasParent() && (this.root_connection != ch)) {
 			this.root_connection.send(msg);
 		}
 		if (hasChildren()) {
 			sendchild(msg, ch);
 		}
 	}
 
 	/**
 	 * Sendet eine Nachricht an alle angeschlossenen Child-Verbindungen, auer
 	 * an die mitgelieferte Verbindung.
 	 * 
 	 * @param msg zu versendendes Nachrichtenobjekt
 	 * @param ch Beim Versand auszuschlieende Verbindung
 	 */
 	private void sendchild(final MSG msg, final ConnectionHandler ch) {
 		for (final ConnectionHandler x : this.connections) {
 			if ((x != ch) || (ch == null)) {
 				x.send(msg);
 			}
 		}
 	}
 
 	/**
 	 * Versendet Datein ber eine TCP-Direktverbindung. Wird nur von send()
 	 * aufgerufen, nachdem festgestellt wurde, dass Nachricht > 5MB
 	 * 
 	 * @param datei Das zu versendende Fileobjekt
 	 * @param receiver NodeID des Empfngers
 	 */
 	public void send_file(final File datei, final long receiver) {
 		if (datei.isFile() && datei.exists() && datei.canRead()
 				&& (datei.length() > 0)) {
 			new Thread(new Runnable() {
 				@Override
 				public void run() {
 					// Erstelle das Parameter Objekt fr die Dateibertragung
 					final FileTransferData tmp_FR = new FileTransferData(datei,
 							datei.length(), NodeEngine.this.meinNode, NodeEngine.this.getNode(receiver));
 					// Wenn Datei unterhalb des Schwellwerts liegt: Als
 					// Nachricht verschicken...
 					if (datei.length() < Config.getConfig().getMaxFileSize()) {
 						try {
 							NodeEngine.this.routesend(new MSG(tmp_FR));
 						} catch (final IOException e1) {
 							LogEngine.log(e1);
 						}
 					} else {
 						// Sonst: Direkt bertragen...
 						// Server Thread
 						new Thread(new Runnable() {
 							@Override
 							public void run() {
 								// Datei holen, Socket ffnen
 								try (final BufferedInputStream bis = new BufferedInputStream(new FileInputStream(datei));
 										final ServerSocket f_server = new ServerSocket(0)) {
 									// Warten
 									Socket client = null;
 									f_server.setSoTimeout((int) Config.getConfig().getFileTransferTimeout());
 									synchronized (tmp_FR) {
 										tmp_FR.server_port = f_server.getLocalPort();
 										tmp_FR.notify();
 									}
 
 									// Server Close Thread
 									new Thread(new Runnable() {
 										@Override
 										public void run() {
 											final MSG tmp_msg = NodeEngine.this.angler.fishfor(NachrichtenTyp.SYSTEM, MSGCode.FILE_TCP_ABORT, tmp_FR.getReceiver_nid(), tmp_FR.hashCode(), true, Config.getConfig().getFileTransferTimeout());
 											if (tmp_msg != null) {
 												try {
 													GUI.getGUI().info("User " + tmp_FR.receiver.getAlias() + "has denied recieving the file: " + tmp_FR.datei.getName(), tmp_FR.receiver.getUserID(), 0);
 													f_server.close();
 												} catch (final IOException e) {
 												}
 											}
 										}
 									}).start();
 
 									// Verbindung anbieten
 									client = f_server.accept();
 									try {
 										f_server.close();
 									} catch (final Exception e) {
 									}
 									// bertragen
 									if ((client != null) && client.isConnected() && !client.isClosed()) {
 										final BufferedOutputStream bos = new BufferedOutputStream(client.getOutputStream());
 										long infoupdate = System.currentTimeMillis() + Config.getConfig().getFileTransferInfoInterval();
 										long transmitted = 0;
 										final byte[] cup = new byte[65535];
 										int len = -1;
 										while ((len = bis.read(cup)) != -1) {
 											bos.write(cup, 0, len);
 											transmitted += len;
 											if (System.currentTimeMillis() > infoupdate) {
 												infoupdate = System.currentTimeMillis() + Config.getConfig().getFileTransferInfoInterval();
 												GUI.getGUI().info(tmp_FR.datei.getName() + "(" + ((transmitted * 100) / tmp_FR.size) + "%)", tmp_FR.sender.getUserID(), 0);
 											}
 										}
 										bos.flush();
 										bos.close();
 
 										GUI.getGUI().info(tmp_FR.datei.getName() + " Done", tmp_FR.sender.getUserID(), 0);
 									}
 									// Ergebnis melden
 								} catch (final FileNotFoundException e) {
 									LogEngine.log("FileTransfer", e.getMessage(), LogEngine.ERROR);
 								} catch (final SocketTimeoutException e) {
 									LogEngine.log("FileTransfer", "Timed Out", LogEngine.ERROR);
 									GUI.getGUI().info("User " + tmp_FR.receiver.getAlias() + " has not answered in time. Connection Timedout", tmp_FR.receiver.getUserID(), 0);
 								} catch (final SocketException e) {
 									LogEngine.log("FileTransfer", "Aborted", LogEngine.ERROR);
 								} catch (final IOException e) {
 									LogEngine.log("FileTransfer", e);
 									GUI.getGUI().info("Transmission-Error, if this keeps happening buy a USB-Stick", tmp_FR.receiver.getUserID(), 0);
 								}
 							}
 						}).start();
 
 						// Warten bis der Server-Thread fertig ist...
 						synchronized (tmp_FR) {
 							try {
 								if (tmp_FR.server_port == -2) {
 									tmp_FR.wait();
 								}
 							} catch (final InterruptedException e) {
 							}
 						}
 						// ... send FileTransferRequest
 						final MSG request = new MSG(tmp_FR, MSGCode.FILE_TCP_REQUEST,
 								tmp_FR.getReceiver_nid());
 						NodeEngine.this.routesend(request);
 					}
 				}
 			}).start();
 		}
 	}
 
 	/**
 	 * Behandelt Dateitransferanfragen
 	 * 
 	 * @param data_paket Das FileRequest Paket des Senders
 	 */
 	private void recieve_file(final MSG data_paket) {
 		final Object[] tmp = (Object[]) data_paket.getData();
 		final FileTransferData tmp_file = (FileTransferData) tmp[0];
 		final File destination;
 		if (!Config.getConfig().getDisableFileTransfer()
 				&& ((destination = this.ce.request_File(tmp_file)) != null)) {
 			tmp_file.accepted = true;
 			new Thread(new Runnable() {
 				@Override
 				public void run() {
 					try {
 						data_paket.save(destination);
 					} catch (final IOException e) {
 						e.printStackTrace();
 					}
 				}
 			}).start();
 		} else {
 			tmp_file.accepted = false;
 		}
 		final MSG reply = new MSG(tmp_file, MSGCode.FILE_RECIEVED,
 				data_paket.getSender());
 		routesend(reply);
 	}
 
 	/**
 	 * ROOT_DISCOVERY senden.
 	 */
 	private void discover() {
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				NodeEngine.this.sendmutlicast(new MSG(NodeEngine.this.meinNode, MSGCode.ROOT_DISCOVERY));
 				NodeEngine.this.rootMe = new Thread(new RootMe());
 				NodeEngine.this.rootMe.start();
 			}
 		}).start();
 	}
 
 	/**
 	 * Antwort auf ROOT_DISCOVERY senden.
 	 * 
 	 * @param quelle Ziel fr gerichtete (Unicast) Antwort
 	 */
 	private void sendDiscoverReply(final Node quelle) {
 		LogEngine.log(this, "sending Replay to " + quelle.toString(),LogEngine.INFO);
 		sendunicast(new MSG(getBestNode(), MSGCode.ROOT_REPLY), quelle);
 	}
 
 	/**
 	 * Alle verbundenen Nodes benachrichtigen.
 	 */
 	private void updateNodes() {
 		synchronized (this.allNodes) {
 			this.allNodes.clear();
 			this.allNodes.add(getMe());
 			this.allNodes.addAll(getChilds());
 			pollChilds();
 			this.allNodes.notifyAll();
 		}
 		if (hasParent()) {
 			sendroot(new MSG(getNodes(), MSGCode.REPORT_ALLNODES));
 		}
 	}
 
 	// Ggf. fr die weitere Entwicklung bentigt.
 	
 	private boolean updateAllNodes(Collection<Node> report) {
 		if(!(report.containsAll(getConnected()) && report.contains(meinNode))) {
 			pollChilds();
 			LogEngine.log(this, "Unvollstndige NodeListe erhalten",LogEngine.ERROR);
 		}
 		return allnodes_set(report);
 	}
 
 	private void pollChilds() {
 		sendchild(new MSG(null, MSGCode.POLL_CHILDNODES), null);
 	}
 
 	/**
 	 * Getter fr ein Set, vom Typ Node, aller ChildNodes.
 	 * 
 	 * @return Set aller ChildNodes
 	 */
 	private Set<Node> getChilds() {
 		final Set<Node> rck = new HashSet<Node>();
 		for (final ConnectionHandler x : this.connections) {
 			rck.addAll(x.getChildren());
 		}
 		return rck;
 	}
 
 	private Set<Node> getConnected(){
 		final Set<Node> tmp= new HashSet<Node>();
 		for (final ConnectionHandler cons : this.connections) {
 			tmp.add(cons.host_node);
 		}
 		if(this.root_connection!=null) {
 			tmp.add(this.root_connection.host_node);
 		}
 		return tmp;
 	}
 
 	/**
 	 * Stelle Verbindung mit diesem <code>NODE</code> her!!!!
 	 * 
 	 * @param knoten
 	 *            der Knoten
 	 * @throws IOException
 	 *             Wenn der hergestellte Socket
 	 */
 	private void connectTo(final Node knoten) {
 		try {
 			this.root_connection = ConnectionHandler.connectTo(knoten);
 			if (this.root_connection != null) {
 
 				final List<Node> newALLnodes = WeightedDistanceStrategy.returnAllNodes(getTree());
 				allnodes_set(newALLnodes);
 				setRootMode(false);
 				setGroup(this.myGroups);
 				sendroot(new MSG(getMe()));
 				sendroot(new MSG(this.myGroups, MSGCode.GROUP_REPLY));
 				sendroot(new MSG(getNodes(), MSGCode.REPORT_CHILDNODES));
 				sendroot(new MSG(null, MSGCode.POLL_ALLNODES));
 				sendroot(new MSG(null, MSGCode.GROUP_POLL));
 			}
 		} catch (final IOException e) {
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
 		this.online = false;
 		this.connectionsAcceptBot.stop();
 		//		multicastRecieverBot.stop();
 		sendtcp(new MSG(this.meinNode, MSGCode.NODE_SHUTDOWN));
 		sendroot(new MSG(this.myGroups,MSGCode.GROUP_LEAVE));
 		if(this.root_connection!=null) {
 			this.root_connection.disconnect();
 		}
 		for (final ConnectionHandler con : this.connections)
 		{
 			(new Thread(new Runnable() {
 				@Override
 				public void run() {
 					con.disconnect();
 				}
 			})).start();// Threadded Disconnect fr jede Leitung
 		}
 		this.multi_socket.close();
 		try {
 			this.server_socket.close();
 		}
 		catch (final IOException e) {
 			LogEngine.log(e);
 		}
 	}
 
 	/**
 	 * Entfernt eine Verbindung wieder.
 	 * 
 	 * @param conn Die zu Entfernende Verbindung
 	 */
 	public void remove(final ConnectionHandler conn) {
 		LogEngine.log(conn, "removing");
 		if (conn == this.root_connection) {
 			LogEngine.log(this, "Lost Root", LogEngine.INFO);
 			this.root_connection = null;
 			if (this.online) {
 				updateNodes();
 				discover();
 			}
 		}
 		else {
 			LogEngine.log(this, "Lost Child", LogEngine.INFO);
 			this.connections.remove(conn);
 			sendtcp(new MSG(conn.getChildren(), MSGCode.CHILD_SHUTDOWN));
 			updateMyGroups();
 			allnodes_remove(conn.getChildren());
 		}
 	}
 
 	/**
 	 * Sendet eine ROOT_ANNOUNCE und initiert so die Neuverhandlung der Root.
 	 */
 	private void sendRA() {
 		final MSG ra= new MSG(this.meinNode, MSGCode.ROOT_ANNOUNCE,getNodes().size());
 		sendmutlicast(ra);
 		this.root_claims_stash.add(ra);
 	}
 
 	/**
 	 * Methode zur Behandlung eingehender ROOT_CLAIM Nachrichten
 	 * 
 	 * @param paket ROOT_CLAIM Paket
 	 */
 	private void handleRootClaim(final MSG paket) {
 		if(paket!=null) {
 			// ndert den Zeitstempel auf die tatschliche Empfangszeit
 			paket.reStamp();
 			this.root_claims_stash.offer(paket);
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
 	public void handleMulticast(final MSG paket) {
 		LogEngine.log(this, "handling [MC]", paket);
 		if (this.angler.check(paket))
 			return;
 		if (this.online && (paket.getTyp() == NachrichtenTyp.SYSTEM)) {
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
 				if ((long) paket.getData() == this.meinNode.getNodeID()) {
 					sendroot(new MSG(this.meinNode));
 				}
 				break;
 			case ALIAS_UPDATE:
 				this.updateAlias((String) paket.getData(), paket.getSender());
 				break;
 			case CMD_RECONNECT:
 				final long payload = (Long) paket.getData();
 				if ((payload == this.nodeID) || (payload == -1337)) {
 					if (this.root_connection != null) {
 						this.root_connection.close();
 					}
 				}
 				break;
 			case BACKUP_SERVER_OFFER:
 				final ConfigData tmp = (ConfigData) paket.getData();
 				Config.getConfig().setBackupDBIP(tmp.getBackupDBIP());
 				Config.getConfig().setBackupDBPort(tmp.getBackupDBPort());
 				Config.getConfig().setBackupDBUser(tmp.getBackupDBUser());
 				Config.getConfig().setBackupDBPw(tmp.getBackupDBPw());
 				Config.getConfig().setBackupDBDatabasename(
 						tmp.getBackupDBDatabasename());
 				Config.write();
 				break;
 			case BACKUP_SERVER_DISCOVER:
 				break;
 
 			default:
 				LogEngine.log(this, "handling [MC]:undefined", paket);
 			}
 		}
 	}
 
 	/**
 	 * Hier wird das Paket verarbeitet und weitergeleitet. Diese Methode wird
 	 * ausschlielich von den ConnectionHandlern aufgerufen, um empfangene Pakete
 	 * verarbeiten zu lassen.
 	 * 
 	 * @param paket
 	 *            zu verarbeitendes Paket
 	 * @param quelle
 	 *            Quelle des Pakets
 	 */
 	@SuppressWarnings("unchecked")
 	public void handle(final MSG paket, final ConnectionHandler quelle) {
 		LogEngine.log(this, "handling[" + quelle + "]", paket);
 		if (this.angler.check(paket))
 			return;
 		if((paket.getEmpfnger() != -1) && (paket.getEmpfnger() != this.nodeID)) {
 			routesend(paket);
 		}
 		else {
 			switch (paket.getTyp()) {
 			case PRIVATE:
 				if(!this.ce.is_ignored(paket.getSender())) {
 					this.ce.put(paket);
 				}
 				break;
 			case GROUP:
 				groupRouteSend(paket,quelle);
 				if(!this.ce.is_ignored(paket.getSender())) {
 					this.ce.put(paket);
 				}
 				break;
 			case SYSTEM:
 				DatabaseEngine.getDatabaseEngine().put(paket);
 				switch (paket.getCode()) {
 				case NODE_UPDATE:
 
 					allnodes_add((Node) paket.getData());
 					sendtcpexcept(paket, quelle);
 					break;
 
 				case POLL_ALLNODES:
 					if (quelle != this.root_connection) {
 						quelle.send(new MSG(getNodes(), MSGCode.REPORT_ALLNODES));
 					}
 					break;
 				case REPORT_ALLNODES:
 					Collection<Node> report =(Collection<Node>) paket.getData();
 					if(!report.contains(meinNode))sendroot(new MSG(meinNode));
 					updateAllNodes(report);
 					sendchild(new MSG(report,MSGCode.REPORT_ALLNODES), null);
 
 					break;
 				case POLL_CHILDNODES:
 					if (quelle == this.root_connection) {
 //						final Set<Node> tmp = getChilds();
 						final Collection<Node> tmp = BestNodeStrategy.returnAllNodes(getTree());
 //						tmp.add(this.meinNode);
 
 						sendroot(new MSG(tmp, MSGCode.REPORT_CHILDNODES));
 						pollChilds();
 					}
 					break;
 				case REPORT_CHILDNODES:
 					if (quelle != this.root_connection) {
 						quelle.setChildren((Collection<Node>) paket.getData());
 					}
 					break;
 				case NODE_SHUTDOWN:
 					allnodes_remove(quelle.getChildren());
 					quelle.close();
 					break;
 				case CHILD_SHUTDOWN:
 					final Collection<Node> shutted = (Collection<Node>) paket.getData();
 					if (quelle != this.root_connection) {
 						quelle.removeChildren(shutted);
 					}
 					if(shutted.contains(this.meinNode)){
 						shutted.remove(this.meinNode);
 						quelle.send(new MSG(this.meinNode));
 					}
 					allnodes_remove(shutted);
 					sendtcpexcept(new MSG(shutted,MSGCode.CHILD_SHUTDOWN), quelle);
 					break;
 				case GROUP_JOIN:
 					quelle.add((Collection<String>) paket.getData());
 					updateMyGroups();
 					break;
 				case GROUP_LEAVE:
 					quelle.remove((Collection<String>) paket.getData());
 					updateMyGroups();
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
 					quelle.send(new MSG(this.allGroups, MSGCode.GROUP_REPLY));
 					break;
 				case GROUP_REPLY:
 					final Set<String> groups = (Set<String>) paket.getData();
 					quelle.add(groups);
 					updateMyGroups();
 					addGroup(groups);
 					break;
 				case FILE_TCP_REQUEST:
 					final FileTransferData tmp = (FileTransferData) paket.getData();
 					if(!this.ce.is_ignored(paket.getSender())) {
 						this.recieve_file(tmp);
 					}
 					break;
 				case FILE_RECIEVED:
 					if(!this.ce.is_ignored(paket.getSender())) {
 						this.ce.inform((FileTransferData) paket.getData());
 					}
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
 					if (paket.getEmpfnger() == this.nodeID) {
 						routesend(new MSG(paket.getData(),
 								MSGCode.PATH_PING_RESPONSE, paket.getSender()));
 					} else {
 						routesend(paket);
 					}
 					break;
 				case PATH_PING_RESPONSE:
 					if (paket.getEmpfnger() == this.nodeID) {
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
 				if (paket.getEmpfnger() != this.nodeID) {
 					routesend(paket);
 				} else {
 					if(!this.ce.is_ignored(paket.getSender())) {
 						this.recieve_file(paket);
 					}
 				}
 				break;
 			default:
 			}
 		}
 	}
 
 	/**
 	 * Verwaltung des Dateiempfangs, abhngig von der Konfiguration des Benutzers.
 	 * 
 	 * @param tmp Das {@link FileTransferData} Objekt mit allen Informationen ber den Dateitransfer
 	 */
 	private void recieve_file(final FileTransferData tmp) {
 		if(!Config.getConfig().getDisableFileTransfer()) {
 			new Thread(new Runnable() {
 				@Override
 				public void run() {
 					final long until = (System.currentTimeMillis()+Config.getConfig().getFileTransferTimeout())-1000;
 					final File destination = NodeEngine.this.ce.request_File(tmp);
 					if(System.currentTimeMillis()<until) {
 						tmp.accepted = (destination != null);
 						MSG reply;
 						if(tmp.accepted) {
 							reply = new MSG(tmp, MSGCode.FILE_RECIEVED,tmp.getSender_nid());
 						} else {
 							reply = new MSG(tmp.hashCode(), MSGCode.FILE_TCP_ABORT,tmp.getSender_nid());
 						}
 						NodeEngine.this.routesend(reply);
 						if (destination != null) {
 							Socket data_con = null;
 							for (final InetAddress ip : tmp.sender.getSockets()) {
 								if (!NodeEngine.this.meinNode.getSockets().contains(ip)) {
 									try {
 										data_con = new Socket(ip, tmp.server_port);
 									} catch (final IOException e) {
 										e.printStackTrace();
 									}
 								}
 							}
 							if (data_con != null) {
 								try (final BufferedInputStream bis = new BufferedInputStream(data_con.getInputStream()); final BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(destination))) {
 									long infoupdate = System.currentTimeMillis()+Config.getConfig().getFileTransferInfoInterval();
 									long transmitted=0;
 									final byte[] cup = new byte[65535];
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
 								catch (final SocketException e) {
 									GUI.getGUI().info(tmp.datei.getName()+" Done", tmp.sender.getUserID(), 0);
 								}
 								catch (final IOException e) {
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
 	 * Versendet ein Paket geroutet
 	 * 
 	 * Das Paket wird in abhngigkeit von seiner ZielnodeID auf der passenden Verbindung weitergeleitet
 	 * 
 	 * @param paket Zu versendendes Paketobjekt
 	 */
 	public void routesend(final MSG paket) {
 		final long empfnger = paket.getEmpfnger();
 		for (final ConnectionHandler con : this.connections) {
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
 			final Node tmp = retrieve(empfnger);
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
 	 * Versendet eine Gruppennachricht geroutet
 	 * 
 	 * Das Paket wird in abhngigkeit von seiner Gruppe auf der passenden Verbindung weitergeleitet
 	 * 
 	 * @param paket Zu versendendes Paketobjekt
 	 * 
 	 * @param quelle Verbindung auf der das Paket empfangen wurde
 	 */
 	public void groupRouteSend(final MSG paket,final ConnectionHandler quelle) {
 		final String gruppe = paket.getGroup();
 		for (final ConnectionHandler con : this.connections) {
 			if((quelle!=con)&&con.getGroups().contains(gruppe)) {
 				con.send(paket);
 			}
 		}
 		if(hasParent()&&(this.root_connection!=quelle)) {
 			sendroot(paket);
 		}
 	}
 
 	/**
 	 * Die eigenen abonierten Gruppen aktualisieren, hinzufgen oder entfernen.
 	 * 
 	 * Berechnet einen neue Schnittmenge von Gruppenmitgliedschaften und ermittelt die Vernderungen zum alten Zustand.
 	 * Alle Vernderungen werden mit dem Vaterknoten kommuniziert.
 	 * 
 	 * @return <code>true</code>wenn sich die Gruppenzugehrigkeit aus sicht des Vaterknoten gendert hat, sonst <code>false</code>
 	 */
 	public boolean updateMyGroups() {
 		final Set<String> aktuell = computeGroups();
 		synchronized (this.myGroups) {
 
 			if (aktuell.hashCode() != this.myGroups.hashCode()) {
 				final Set<String> dazu = new HashSet<String>(aktuell);
 				dazu.removeAll(this.myGroups);
 				if(dazu.size()>0) {
 					sendroot(new MSG(dazu, MSGCode.GROUP_JOIN));
 					if(addGroup(dazu)) {
 						sendchild(new MSG(dazu, MSGCode.GROUP_ANNOUNCE),null);
 					}
 				}
 				final Set<String> weg = new HashSet<String>(this.myGroups);
 				weg.removeAll(aktuell);
 				if(weg.size()>0) {
 					sendroot(new MSG(weg, MSGCode.GROUP_LEAVE));
 					if(isRoot()) {
 						if(removeGroup(weg)) {
 							sendchild(new MSG(weg, MSGCode.GROUP_EMPTY),null);
 						}
 					}
 				}
 				this.myGroups.clear();
 				this.myGroups.addAll(aktuell);
 				return true;
 			}
 			return false;
 		}
 	}
 
 	/**
 	 * Entfernt alle Knoten einer Kollektion aus der Gesamtliste
 	 * 
 	 * @param data Zu entfernende Nodes
 	 */
 	private void allnodes_remove(final Collection<Node> data) {
 		synchronized (this.allNodes) {
 			final int hash = this.allNodes.hashCode();
 			this.allNodes.removeAll(data);
 			this.allNodes.add(this.meinNode);
 			if (this.allNodes.hashCode() != hash) {
 				this.allNodes.notifyAll();
 			}
 		}
 	}
 
 	/**
 	 * Fgt Nodes zur Gesamtliste hinzu
 	 * 
 	 * @param data neue Nodes
 	 */
 	private void allnodes_add(final Node data) {
 		synchronized (this.allNodes) {
 			final int hash = this.allNodes.hashCode();
 			this.allNodes.add(data);
 			if (this.allNodes.hashCode() != hash) {
 				this.allNodes.notifyAll();
 			}
 			DatabaseEngine.getDatabaseEngine().put(data);
 		}
 	}
 
 	/**
 	 * Setz die Gesamtliste der Nodes neu
 	 * 
 	 * @param data neue Gesamtliste
 	 */
 	private boolean allnodes_set(final Collection<Node> data) {
 		Set<Node> tmp = new HashSet<Node>(data);
 		synchronized (this.allNodes) {
 			if(tmp.hashCode()!=allNodes.hashCode()) {
 			this.allNodes.clear();
 			this.allNodes.addAll(data);
 			this.allNodes.add(meinNode);
 			DatabaseEngine.getDatabaseEngine().put(data);
 			this.allNodes.notifyAll();
 			return true;
 			}
 			else return false;
 			
 
 		}
 	}
 
 	/**
 	 * Startet Lookup fr {@link Node} mit der NodeID <code>nid</code>.
 	 * Versucht ihn neu verbinden zu lassen, falls die Verbindung fehl
 	 * schlgt.
 	 * 
 	 * @param nid
 	 *            ID des Nodes
 	 * @return das {@link Node}-Objekt oder <code>null</code>, wenn der Knoten
 	 *         nicht gefunden wurde.
 	 */
 	private Node retrieve(final long nid) {
 		sendmutlicast(new MSG(nid, MSGCode.NODE_LOOKUP));
 		final MSG x = this.angler.fishfor(NachrichtenTyp.SYSTEM,MSGCode.NODE_UPDATE,nid,null,false,1000);
 		if (x != null)
 			return (Node) x.getData();
 		else {
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
 	private void setRootMode(final boolean rootmode) {
 		this.rootMode = rootmode;
 		GUI.getGUI().setTitle("publicMAIN"+((rootmode)?"[ROOT]":"" ));
 		if(rootmode) {
 			setGroup(this.myGroups) ;
 		}
 	}
 
 	/**
 	 * Getter fr die eigene <code>nid</code>.
 	 * 
 	 * @return Die eigene NodeID
 	 */
 	public long getNodeID() {
 		return this.nodeID;
 	}
 
 	/**
 	 * Entfernt Gruppen aus der Gruppenbersicht
 	 * 
 	 * @param gruppen_name die zu entfernenden Gruppennamen
 	 * @return <code>true</code> Wenn sich die Gruppenliste durch die aktion gendert hat, sonst <code>false</code>
 	 */
 	public boolean removeGroup(final Collection<String> gruppen_name) {
 		synchronized (this.allGroups) {
 			final boolean x = this.allGroups.removeAll(gruppen_name);
 			this.allGroups.notifyAll();
 			return x;
 		}
 	}
 
 	/**
 	 * Setzt Liste verfgbarer Gruppen neu
 	 * 
 	 * @param groups Neue Gruppenliste
 	 */
 	public void setGroup(final Collection<String> groups) {
 		synchronized (this.allGroups) {
 			this.allGroups.clear();
 			this.allGroups.addAll(groups);
 			this.allGroups.notifyAll();
 		}
 	}
 
 	/**
 	 * Fgt Gruppen der Liste hinzu
 	 * 
 	 * @param groups Neue Gruppen
 	 * @return  <code>true</code> Wenn sich die Gruppenliste durch die aktion gendert hat, sonst <code>false</code>
 	 */
 	public boolean addGroup(final Collection<String> groups) {
 		synchronized (this.allGroups) {
 			final boolean x = this.allGroups.addAll(groups);
 			this.allGroups.notifyAll();
 			return x;
 		}
 	}
 
 	/**
 	 * Verlsst eine Gruppe
 	 * 
 	 * @param gruppen_name Zu verlassende Gruppe
 	 * @return  <code>true</code> Wenn sich die Gruppenliste durch die aktion gendert hat, sonst <code>false</code>
 	 */
 	public boolean removeMyGroup(final String gruppen_name) {
 		synchronized (this.myGroups) {
 			return this.myGroups.remove(gruppen_name);
 		}
 	}
 
 	/**
 	 * Tritt einer Gruppe bei
 	 * 
 	 * @param gruppen_name Zu betretende Gruppe
 	 * @return  <code>true</code> Wenn sich die Gruppenliste durch die aktion gendert hat, sonst <code>false</code>
 	 */
 	public boolean addMyGroup(final String gruppen_name) {
 		synchronized (this.myGroups) {
 			return this.myGroups.add(gruppen_name);
 		}
 	}
 
 	/**
 	 * Errechnet die Mitglidschaften alle Kindknoten zusammengenommen
 	 * 
 	 * @return Vereinigung aller Gruppenmitgliedschaften
 	 */
 	public Set<String>computeGroups(){
 		final Set<String> tmpGroups = new HashSet<String>();
 		for (final ConnectionHandler cur : this.connections) {
 			tmpGroups.addAll(cur.getGroups());
 		}
 		tmpGroups.addAll(this.ce.getMyGroups());
 		return tmpGroups;
 	}
 
 	/**
 	 * Den eigenen Alias ndern und dies allen anderen Nodes mitteilen.
 	 */
 	public void updateAlias() {
 		final String alias = this.ce.getAlias();
 		if(this.online&&(!alias.equals(this.meinNode.getAlias()))) {
 			sendmutlicast(new MSG(alias, MSGCode.ALIAS_UPDATE));
 			this.updateAlias(alias,this.nodeID);
 		}
 	}
 
 	/**
 	 * Zeigt die Latenz von mir zu allen mir bekannten Knoten ber den Baum im aktuellen Chatfenster an
 	 */
 	public void pathPing(){
 		for (final Node cur : getNodes()) {
 			GUI.getGUI().info(cur.toString() + ":" +this.pathPing(cur), null, 0);
 
 		}
 	}
 
 	/**
 	 * Sendet ein Ping Paket an einen Knoten und registriert einen Hook um die Antwort auszuwerten
 	 * 
 	 * @param remote Der zu pingende Knoten
 	 * @return Latenz in Millisekunden oder -1 wenn keine Antwort innerhalt 1 Sekunde empfangen wurde
 	 */
 	public long pathPing(final Node remote) {
 		if (remote.equals(this.meinNode))return 0;
 		else {
 			final long currentTimeMillis = System.currentTimeMillis();
 			final MSG paket = new MSG(currentTimeMillis, MSGCode.PATH_PING_REQUEST,remote.getNodeID());
 			final MSG response = this.angler.fishfor(NachrichtenTyp.SYSTEM, MSGCode.PATH_PING_RESPONSE, remote.getNodeID(),currentTimeMillis, true, 1000,paket);
 			if(response==null)return -1;
 			else
 				return (System.currentTimeMillis()-currentTimeMillis);
 
 		}
 	}
 
 	/**
 	 * Genderten Alias der GUI mitteilen, damit der Alias korrekt auf der GUI
 	 * dargestellt wird.
 	 * 
 	 * @param newAlias Neuer Anzeigename des Knoten
 	 * @param nid Der zu aktualisierende Knoten
 	 * 
 	 * @return boolean
 	 */
 	private boolean updateAlias(final String newAlias, final long nid) {
 		Node tmp;
 		synchronized (this.allNodes) {
 			if ((tmp = getNode(nid)) != null) {
 				if (!tmp.getAlias().equals(newAlias)) {
 					tmp.setAlias(newAlias);
 					this.allNodes.notifyAll();
 					GUI.getGUI().notifyGUI();
 					// Zur Aktualisierung der Datenbank wegschreiben.
 					DatabaseEngine.getDatabaseEngine().put(this.allNodes);
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
 	 * @param command Debug-Befehl
 	 * @param parameter Debug-Befehl-Parameter
 	 */
 	public void debug(final String command, final String parameter) {
 		switch (command) {
 		case "ra":
 			sendRA();
 			break;
 		case "proute":
 			System.out.println(getRoutes());
 			break;
 		case "wroute":
 			final Map<Long, Long> routes = getRoutes();
 			for (final long x : routes.keySet()) {
 				DatabaseEngine.getDatabaseEngine().put(x, routes.get(x));
 			}
 			break;
 		case "conns":
 			System.out.println(this.connections.size());
 			for (final ConnectionHandler con : this.connections) {
 				System.out.println(con.toString2());
 
 			}
 			if(this.root_connection!=null) {
 				System.out.println("root:\n"+this.root_connection.toString2());
 			}
 
 			break;
 
 		case "play":
 			Help.playSound(parameter);
 			break;
 		case "sound":
 //			if("on".equals(parameter.toLowerCase())){
 //				Config.getConfig().setSoundActivated(true);
 //			}else Config.getConfig().setSoundActivated(false);
 			Config.getConfig().setSoundActivated("on".equals(parameter));
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
 			sendtcp(new MSG(this.meinNode, MSGCode.NODE_UPDATE));
 			break;
 		case "pingall":
 			this.pathPing();
 			break;
 		case "_kick":
 			final Node tmp = this.ce.getNodeforAlias(parameter);
 			if (tmp != null) {
 				routesend(new MSG(null, MSGCode.CMD_SHUTDOWN, tmp.getNodeID()));
 			}
 			break;
 		case "maxcon":
 			Config.getConfig().setMaxConnections(Integer.parseInt(parameter));
 			break;
 		case "bestnode":
 			final long time = System.currentTimeMillis();
 			GUI.getGUI().info("Strategie:" + this.myStrategy.getClass().getSimpleName()+ " MaxConnections:"+ Config.getConfig().getMaxConnections(), null, 0);
 			GUI.getGUI().info(getBestNode().toString(), null, 0);
 			GUI.getGUI().info("took " + (System.currentTimeMillis() - time)+ " ms to evaluate", null, 0);
 			break;
 		case "strategy":
 			if (parameter.equals("random")) {
 				this.myStrategy = new RandomStrategy(this.nodeID);
 			} else if (parameter.equals("breadth")) {
 				this.myStrategy = new BreadthFirstStrategy();
 			} else if (parameter.startsWith("weighted")
 					&& (parameter.split(" ").length == 4)) {
 				this.myStrategy = new WeightedDistanceStrategy(
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
 			showTree(getTree());
 			break;
 		case "reconnect_all":
 			sendmutlicast(new MSG(-1337l, MSGCode.CMD_RECONNECT));
 			break;
 		case "poll_bus":
 			this.multi_socket.discoverBUS();
 			break;
 		default:
 			LogEngine.log(this, "debug command not found", LogEngine.ERROR);
 			break;
 		}
 	}
 
 	//	/**
 	//	 * Erstellt den Topologie-Baum fr das Debug-Kommando (/debug tree).
 	//	 *
 	//	 * @return TreeNode der die Baumtopologie reprsentiert
 	//	 */
 	//	public Node getTree() {
 	//		Node root = (Node) meinNode.clone();
 	//		// Zuerst werden alle Knoten hergestellt...
 	//		for (final ConnectionHandler con : connections) {
 	//			Runnable tmp = new Runnable() {
 	//				public void run() {
 	//					con.send(new MSG(null, MSGCode.TREE_DATA_POLL));
 	//				}
 	//			};
 	//			MSG polled_tree = angler.fishfor(NachrichtenTyp.SYSTEM,
 	//					MSGCode.TREE_DATA, null, null, true, Config.getConfig()
 	//					.getTreeBuildTime(), tmp);
 	//			if (polled_tree != null) {
 	//				root.add((Node) polled_tree.getData());
 	//			}
 	//		}
 	//		return root;
 	//	}
 	//
 	/**
 	 * Erstellt den Topologie-Baum fr das Debug-Kommando (/debug tree).
 	 * 
 	 * @return TreeNode der die Baumtopologie reprsentiert
 	 */
 	public Node getTree() {
 		final Node root = (Node) this.meinNode.clone();
 		final MiniMonitor mon = new MiniMonitor();
 		for (final ConnectionHandler con : this.connections) {
 			final Runnable runa = new Runnable() {
 				@Override
 				public void run() {
 					con.send(new MSG(null, MSGCode.TREE_DATA_POLL));
 				}};
 				// Zuerst werden fr alle connections hooks registrier
 				mon.add();
 				final MSG tmp =con.getHook().fishfor(NachrichtenTyp.SYSTEM,MSGCode.TREE_DATA, null, null, true, Config.getConfig().getTreeBuildTime(),runa);
 				if(tmp!=null) {
 					mon.dec((Node) tmp.getData());
 				} else {
 					mon.dec(null);
 				}
 		}
 		//wenn alle hooks abgelaufen sind
 		final List<Node> tmp = mon.check();
 		for (final Node node : tmp) {
 			root.add(node);
 		}
 
 		//tree bauen und zurckgeben
 		return root;
 	}
 
 
 
 	/**
 	 * Liefert die <code>uid</code> fr eine <code>nid</code>.
 	 * 
 	 * @param nid NodeID des gesuchten Users
 	 * @return UserID des Nodes
 	 */
 	public long getUIDforNID(final long nid){
 		final Node node = getNode(nid);
 		if (node!=null)
 			return node.getUserID();
 		else
 			return -1;
 	}
 
 	/**
 	 * Visualisiert den Topologie-Baum fr das Debug-Kommando (/debug tree).
 	 * 
 	 * @param root Wurzel des Baums der dargestellt werden soll.
 	 */
 	public void showTree(final TreeNode root) {
 		// Der Wurzelknoten wird dem neuen JTree im Konstruktor bergeben
 		final JTree tree = new JTree( root );
 		// Ein Frame herstellen, um den Tree anzuzeigen
 		final JFrame frame = new JFrame( "publicMAIN - Topology" );
 		frame.add( new JScrollPane( tree ));
 		frame.setIconImage(Help.getIcon("pM_Logo.png").getImage());
 		frame.setMinimumSize(new Dimension(250, 400));
 		frame.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
 		frame.pack();
 		frame.setLocationRelativeTo( null );
 		frame.setVisible( true );
 	}
 
 
 	/**
 	 * @author tkessels
 	 *
 	 * Fhrt einige Tasks periodisch aus um den Betrieb des Netzes zu gewhrleisten
 	 *
 	 */
 	private final class Maintainer implements Runnable {
 		@Override
 		public void run() {
 			while(NodeEngine.this.online) {
 				if(isRoot()) {
 					sendRA();
 					if(updateAllNodes(BestNodeStrategy.returnAllNodes(getTree()))){
 						sendchild(new MSG(getNodes(),MSGCode.REPORT_ALLNODES), null);
 					}
 				} else {
 					sendroot(new MSG(NodeEngine.this.meinNode));
 				}
 				try {
 					Thread.sleep(5000);
 				} catch (final InterruptedException e) {
 				}
 
 
 			}
 		}
 	}
 
 	/**
 	 * Definiert diesen Node nach einem Timeout als Root, wenn bis dahin keine
 	 * Verbindung aufgebaut wurde.
 	 */
 	private final class RootMe implements Runnable {
 		@Override
 		public void run() {
 			if (!NodeEngine.this.online||isRoot()||NodeEngine.this.rootDiscovering)
 				return;
 			final long until = System.currentTimeMillis() + NodeEngine.this.DISCOVER_TIMEOUT;
 			while (System.currentTimeMillis() < until) {
 				try {
 					Thread.sleep(NodeEngine.this.DISCOVER_TIMEOUT);
 				}
 				catch (final InterruptedException e) {
 				}
 			}
 			if (NodeEngine.this.online&&!hasParent()) {
 				LogEngine.log("RootMe", "no Nodes detected: claiming Root", LogEngine.INFO);
 				claimRoot();
 			}
 		}
 	}
 
 	/**
 	 * Selbst die Root-Position beanspruchen.
 	 */
 	private synchronized void claimRoot() {
 		if (this.rootDiscovering == false) {
 			this.rootClaimProcessor = new Thread(new RootClaimProcessor());
 			this.rootClaimProcessor.start();
 		}
 	}
 
 
 	/**
 	 * Eine aktuelle Routingtabelle zusammenstellen
 	 * 
 	 * @return aktuelle Routingtabelle
 	 */
 	public Map<Long,Long> getRoutes(){
 		final Map<Long,Long> rueck = new HashMap<Long, Long>();
 		for (final ConnectionHandler con : this.connections) {
 			if (con.host_node != null) {
 				final long gw = con.host_node.getNodeID();
 				for (final Node curchi : con.getChildren()) {
 					rueck.put(curchi.getNodeID(), gw);
 				}
 			}
 		}
 		final Set<Node> tmp = new HashSet<Node>();
 		tmp.addAll(getNodes());
 		tmp.removeAll(getChilds());
 
 		if (this.root_connection!=null) {
 			final long gw = this.root_connection.host_node.getNodeID();
 			for (final Node x : tmp) {
 				rueck.put(x.getNodeID(), gw);
 			}
 		}
 		return rueck;
 	}
 
 	/**
 	 * 
 	 * Wartet eine gewisse Zeit und wertet dann alle gesammelten ROOT_ANNOUNCES
 	 * aus und fordert anschlieend von der neuen Root einen Node zum verbinden
 	 * an, wenn der Node selbst die neue Root ist.
 	 */
 	private final class RootClaimProcessor implements Runnable {
 		@Override
 		public void run() {
			NodeEngine.this.rootDiscovering = true;
 			LogEngine.log("DiscoverGame", "started", LogEngine.INFO);
 			sendRA();
 			final long until = System.currentTimeMillis() + NodeEngine.this.ROOT_CLAIM_TIMEOUT;
 			while (System.currentTimeMillis() < until) {
 				try {
 					Thread.sleep(NodeEngine.this.ROOT_CLAIM_TIMEOUT);
 				} catch (final InterruptedException e) {
 				}
 			}
 			final List<MSG> ra_replies = new ArrayList<MSG>();
 			ra_replies.addAll(NodeEngine.this.root_claims_stash);
 			Collections.sort(ra_replies);
			final long deadline = ra_replies.get(0).getTimestamp() + (2
					* NodeEngine.this.ROOT_CLAIM_TIMEOUT);
 
 			Node toConnectTo = NodeEngine.this.meinNode;
 			long maxPenunte = getNodes().size();
 			for (final MSG x : NodeEngine.this.root_claims_stash) {
 				if (x.getTimestamp() <= deadline) {
 					final long tmp_size = x.getEmpfnger();
 					final Node tmp_node = (Node) x.getData(); // Cast Payload in ein
 					// Object Array und das
 					// 2. Object dieses
 					// Arrays in einen Node
 					if ((tmp_size > maxPenunte)
 							|| ((tmp_size == maxPenunte) && (tmp_node
 									.getNodeID() > toConnectTo.getNodeID()))) {
 						toConnectTo = tmp_node;
 						maxPenunte = tmp_size;
 					}
 				}
 			}
 			LogEngine.log("DiscoverGame", "Finished:"
 					+ ((toConnectTo != NodeEngine.this.meinNode) ? "lost" : "won") + "("
					+ NodeEngine.this.root_claims_stash.size() + " participants)",
 					LogEngine.INFO);
 			if (toConnectTo == NodeEngine.this.meinNode) {
 				setRootMode(true);
 			} else {
 				discover(); // another root won and should be
 				// answeringconnectTo(toConnectTo);
 			}
 			NodeEngine.this.root_claims_stash.clear();
 			NodeEngine.this.rootDiscovering = false;
 		}
 	}
 
 	/**
 	 *	Server-Thread der eingehende Verbindungen annimmt und in {@link ConnectionHandler} einhllt und registriert
 	 */
 	private final class ConnectionsAccepter implements Runnable {
 		@Override
 		public void run() {
 			if((NodeEngine.this.connections==null)||(NodeEngine.this.server_socket==null))
 				return;
 			while (NodeEngine.this.online) {
 				LogEngine.log("ConnectionsAccepter", "Listening on Port:" + NodeEngine.this.server_socket.getLocalPort(), LogEngine.INFO);
 				try {
 					final ConnectionHandler tmp = new ConnectionHandler(NodeEngine.this.server_socket.accept());
 					NodeEngine.this.connections.add(tmp);
 					tmp.isConnected();
 				}
 				catch (final IOException e) {
 					LogEngine.log(e);
 				}
 			}
 		}
 	}
 
 	/**
 	 * @author tkessels
 	 *
 	 * Fr Multithreaded Tree konstruktion
 	 */
 	private final class MiniMonitor{
 		private  int anzahl;
 		private final  List<Node> mynodes;
 
 
 		public MiniMonitor() {
 			this.anzahl=0;
 			this.mynodes = new ArrayList<Node>();
 		}
 
 		public synchronized void add() {
 			this.anzahl++;
 		}
 		public synchronized  List<Node> check() {
 			while(this.anzahl>0) {
 				try {
 					this.wait();
 				} catch (final InterruptedException e) {
 				}
 			}
 			return this.mynodes;
 		}
 		public synchronized void dec(final Node tmp) {
 			this.anzahl--;
 			if(tmp!=null) {
 				this.mynodes.add(tmp);
 			}
 			notifyAll();
 		}
 
 
 	}
 }
