 package org.publicmain.nodeengine;
 
 import java.awt.Dimension;
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.InetAddress;
 import java.net.MulticastSocket;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 import java.net.SocketTimeoutException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Random;
 import java.util.Set;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JScrollPane;
 import javax.swing.JTree;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.MutableTreeNode;
 import javax.swing.tree.TreeNode;
 
 import org.publicmain.chatengine.ChatEngine;
 import org.publicmain.common.Config;
 import org.publicmain.common.FileTransferData;
 import org.publicmain.common.LogEngine;
 import org.publicmain.common.MSG;
 import org.publicmain.common.MSGCode;
 import org.publicmain.common.NachrichtenTyp;
 import org.publicmain.common.Node;
 import org.publicmain.gui.GUI;
 import org.publicmain.sql.DatabaseEngine;
 import org.publicmain.sql.LocalDBConnection;
 import org.resources.Help;
 
 /**
  * Die NodeEngine ist fr die Verbindungen zu anderen Nodes zustndig. Sie verwaltet die bestehenden Verbindungen, sendet Nachichten und Datein und ist fr das Routing zustndig
  */
 public class NodeEngine {
  private final long DISCOVER_TIMEOUT = Config.getConfig().getDiscoverTimeout(); 			//Timeout bis der Node die Suche nach anderen Nodes aufgibt und sich zum Root erklrt
  private final long ROOT_CLAIM_TIMEOUT = Config.getConfig().getRootClaimTimeout(); 			//Zeitspanne die ein Root auf Root_Announces wartet um zu entscheiden wer ROOT bleibt. 
  private final InetAddress MULTICAST_GROUP = InetAddress.getByName(Config.getConfig().getMCGroup()); 	//Default MulticastGruppe fr Verbindungsaushandlung
  private final int MULTICAST_PORT = Config.getConfig().getMCPort(); 						//Default Port fr MulticastGruppe fr Verbindungsaushandlung
  private final int MULTICAST_TTL = Config.getConfig().getMCTTL(); 						//Default Port fr MulticastGruppe fr Verbindungsaushandlung
  private final int MAX_CLIENTS = Config.getConfig().getMaxConnections();					//Maximale Anzahl anzunehmender Verbindungen
  private final int MAX_FILE_SIZE = Config.getConfig().getMaxFileSize();
 
  private static volatile NodeEngine ne; 	//Statischer Zeiger auf einzige Instanz der NodeEngine
  private final long nodeID;
  private Node meinNode; 					//die NodeReprsentation dieser NodeEngine
  private ChatEngine ce; 					//Zeiger auf parent ChatEngine
  private Hook angler = new Hook();		//Hookobjekt zum abfangen von Nachrichten		
 
  private ServerSocket server_socket; 				//Server Socket fr eingehende Verbindungen (Passiv/Childs)
  private ConnectionHandler root_connection;	//TCP Socket zur Verbindung mit anderen Knoten (Aktiv/Parent/Root)
  private MulticastSocket multi_socket;			//Multicast/Broadcast UDP-Socket zu Verbindungsaushandlung
  public List<ConnectionHandler> connections; 	//Liste bestehender Childverbindungen in eigener HllKlasse
  
  private Set<String> allGroups=new HashSet<String>();  //Liste aller Gruppen 
 private Set<String> myGroups=new HashSet<String>(); //Liste aller abonierten Gruppen dieses und aller untergeordneter Knoten
  
 
  private BlockingQueue<MSG> root_claims_stash; //Queue fr Bewerberpakete bei Neuaushandlung vom Root-Status 
  private Set<Node> allNodes; 						//Alle dieser Nodenginge bekannten Knotten (sollten alle sein)
 
  private volatile boolean rootMode; 			//Dieser Knoten mchte Wurzel sein (und benimmt sich auch so)
  private volatile boolean online; 				//Dieser Knoten mchte an sein und verbunden bleiben (signalisiert allen Threads wenn die Anwendung beendet wird)
  private volatile boolean rootDiscovering;	//Dieser Knoten ist gerade dabei ROOT_ANNOUNCES zu sammeln um einen neuen ROOT zu whlen
 
  private Thread multicastRecieverBot		= new Thread(new MulticastReciever());			//Thread zum annehmen und verarbeiten der Multicast-Pakete
  private Thread connectionsAcceptBot 	= new Thread(new ConnectionsAccepter()); 	//Thread akzeptiert und schachtelt eingehen Verbindungen auf dem ServerSocket
  private Thread rootMe	;																			//Thread der nach einem Delay Antrag auf RootMode stellt wird mit einem Discover gestartet
  private Thread rootClaimProcessor;												 				//Thread zum Sammeln und Auswerten von Root_Announces (Ansprche auf Rootmode) wird beim empfang/versand eines RootAnnounce getstatet.
  
  private BestNodeStrategy myStrategy;
  
 
 	public NodeEngine(ChatEngine parent) throws IOException {
 		
 		allNodes = new HashSet<Node>();
 		connections = new CopyOnWriteArrayList<ConnectionHandler>();
 		
 		root_claims_stash = new LinkedBlockingQueue<MSG>();
 		
 		nodeID=((long) (Math.random()*Long.MAX_VALUE));
 		ne = this;
 		ce = parent;
 		online = true;
 
 		server_socket = new ServerSocket(0);
 		multi_socket = new MulticastSocket(MULTICAST_PORT);
 		multi_socket.joinGroup(MULTICAST_GROUP);
 		multi_socket.setLoopbackMode(true);
 		multi_socket.setTimeToLive(MULTICAST_TTL);
 
 		meinNode = new Node(server_socket.getLocalPort(),nodeID,ce.getUserID(),System.getProperty("user.name"),ce.getAlias());
 		allNodes.add(meinNode);
 		
 		myStrategy=new WeightedDistanceStrategy(1, 0, 0);
 
 
 		connectionsAcceptBot.start();
 		multicastRecieverBot.start();
 
 		LogEngine.log(this, "Multicast Socket geffnet", LogEngine.INFO);
 
 		discover();
 		
 		
 
 	}
 
 	public static NodeEngine getNE() {
 		return ne;
 	}
 
 	/**
 	 * getMe() gibt das eigene NodeObjekt zurck
 	 */
 	public Node getMe() {
 		return meinNode; 
 	}
 
 	
 	
 	
 	private Node getBestNode() {
 		return myStrategy.getBestNode();
 	}
 
 	/**
 	 * isConnected() gibt "true" zurck wenn die laufende Nodeengin hochgefahren und mit anderen Nodes verbunden oder root ist, "false" wenn nicht.
 	 */
 	/*
 	 * public boolean isOnline(){ return online; }
 	 */
 
 	private boolean hasChildren() {
 		return connections.size() > 0;
 	}
 
 	/**
 	 * isRoot() gibt "true" zurck wenn die laufende Nodeengin Root ist und "false" wenn nicht.
 	 */
 	public boolean isRoot() {
 		return rootMode && !hasParent();
 	}
 	
 	public boolean isOnline() {
 		return online;
 	}
 
 	public boolean hasParent() {
 		return (root_connection != null && root_connection.isConnected());
 	}
 
 //	public int getServer_port() {
 //		return server_socket.getLocalPort();
 //	}
 	
 	/**
 	 * getNodes() gibt ein NodeArray zurck welche alle verbundenen Nodes beinhaltet.
 	 */
 	public Set<Node> getNodes() {
 		synchronized (allNodes) {
 			return allNodes;
 		}
 	}
 
 
 	/**Findet zu NodeID zugehrigen Node in der Liste
 	 * 
 	 * Liefert das NodeObjekt zu einer NodeID solte der Knoten nicht bekannt sein wird <code>null</code> zurck geliefert.
 	 * Befindet sich der Knoten der die Abfrage ausfhr im RootMode dann wird er versuchen den Knoten ber ein Lookup aufzuspren und ihn nachzutragen.
 	 * Schlgt dieser Versuch auch Fehl wird er einen Befehl an den Knoten schicken sich neu zu verbinden. (not yet implemented)
 	 * 
 	 * @param nid NodeID
 	 * @return Node-Objekt zu angegebenem NodeID oder null wenn
 	 */
 	public Node getNode(long nid){
 		synchronized (allNodes) {
 			for (Node x : getNodes()) {
 				if (x.getNodeID() == nid)
 					return x;
 			}
 			if (isRoot()) return retrieve(nid);
 			else return null;
 		}
 	}
 	
 	
 	public Node getNodeForUID(long uid){
 		synchronized (allNodes) {
 			for (Node x : getNodes()) {
 				if (x.getUserID() == uid)
 					return x;
 			}
 			return null;
 		}
 	}
 	
 
 
 	/**
 	 * Gibt ein StringArray aller vorhandenen Groups zurck
 	 * 
 	 */
 	public Set<String> getGroups() {
 		return allGroups;
 	}
 
 	private void sendroot(MSG msg) {
 		if (hasParent()) root_connection.send(msg);
 	}
 
 	/**
 	 * versendet Daten vom Typ MSG an ZielNodes oder git diese an send_file() weiter. prft Dateigre (wenn < 5MB aufruf der send_file() sonst als Msg-Type Data) send_file() wird sowohl das Ziel als auch die Daten mitgegeben. D.h., dass das
 	 * MSG-Paket hier in File und destination geteilt.
 	 */
 	private void sendmutlicast(MSG nachricht) {
 		byte[] buf = MSG.getBytes(nachricht);
 		try {
 			if (buf.length < 65000) {
				multi_socket.send(new DatagramPacket(buf, buf.length, MULTICAST_GROUP, 6789));
 				LogEngine.log(this, "sende [MC]", nachricht);
 			}
 			else LogEngine.log(this, "MSG zu gro fr UDP-Paket", LogEngine.ERROR);
 		}
 		catch (IOException e) {
 			LogEngine.log(e);
 		}
 	}
 
 	private void sendunicast(MSG msg, Node newRoot) {
 		byte[] data = MSG.getBytes(msg);
 		if (data.length < 65000) {
 			for (InetAddress x : newRoot.getSockets()) {
 				if (!meinNode.getSockets().contains(x)) {
 					DatagramPacket unicast = new DatagramPacket(data,
 							data.length, x, MULTICAST_PORT);//ber Unicast
 					try {
 						multi_socket.send(unicast);
 						LogEngine
 								.log(this, "sende [" + x.toString() + "]", msg);
 					} catch (IOException e) {
 						LogEngine.log(e);
 					}
 				}
 			}
 		}
 	}
 
 	public void sendtcp(MSG nachricht) {
 		if (hasParent()) 		sendroot(nachricht); 
 		//FIXME Concurrent Modification Exception beim disconnecten
 		if (hasChildren()) 	for (ConnectionHandler x : connections) x.send(nachricht);
 	}
 
 	private void sendtcpexcept(MSG msg, ConnectionHandler ch) {
 //		if (!isRoot() && root_connection != ch) root_connection.send(msg);
 		if (hasParent()&&root_connection != ch) root_connection.send(msg);
 		if (hasChildren())sendchild(msg, ch);
 	}
 	
 	private void sendchild(MSG msg, ConnectionHandler ch) {
 		for (ConnectionHandler x : connections)if (x != ch||ch==null) x.send(msg);
 	}
 
 	/**
 	 * versendet Datein ber eine TCP-Direktverbindung wird nur von send() aufgerufen nachdem festgestellt wurde, dass nachicht > 5MB
 	 */
 
 	public void send_file(final File datei, final long receiver) {
 		if (datei.isFile() && datei.exists() && datei.canRead() && datei.length() > 0) {
 			new Thread(new Runnable() 
 			{
 				public void run() 
 				{
 					//Erstelle das Parameter Objekt fr die Dateibertragung
 					final FileTransferData tmp_FR = new FileTransferData(datei, datei.length(), meinNode, getNode(receiver));
 					//Wenn Datei unterhalb des Schwellwerts liegt: als Nachricht verschicken .....
 					if (datei.length() < Config.getConfig().getMaxFileSize()) 
 					{
 						try {
 							routesend(new MSG(tmp_FR));
 						}
 						catch (IOException e1) 
 						{
 							LogEngine.log(e1);
 						}
 					}else {			//.......sonst: direkt bertragen
 						
 						
 						//Server Thread
 						new Thread(new Runnable() {
 							public void run() {
 								//datei holen
 								//soket ffnen
 								try (final BufferedInputStream bis = new BufferedInputStream(new FileInputStream(datei)); final ServerSocket f_server = new ServerSocket(0)) {
 									//warten
 									Socket client = null;
 									f_server.setSoTimeout((int) Config.getConfig().getFileTransferTimeout());
 									synchronized (tmp_FR) {
 										tmp_FR.server_port=f_server.getLocalPort();
 										tmp_FR.notify();
 									}
 									
 									
 									//Server Close Thread
 									new Thread(new Runnable() {
 										public void run() {
 											MSG tmp_msg = angler.fishfor(NachrichtenTyp.SYSTEM, MSGCode.FILE_TCP_ABORT, tmp_FR.getReceiver_nid(), tmp_FR.hashCode(), true, Config.getConfig().getFileTransferTimeout());
 											if (tmp_msg != null) {
 												try {
 													GUI.getGUI().info("User " + tmp_FR.receiver.getAlias()+ "has denied recieving the file: "+ tmp_FR.datei.getName(), tmp_FR.receiver.getUserID(), 0);
 													f_server.close();
 												} catch (IOException e) {
 												}
 											}
 										}
 									}).start();
 									
 									
 									//Verbindung anbieten
 									client = f_server.accept();
 									try {
 										f_server.close();
 									} catch (Exception e) {
 									}
 									//betragen
 									if (client != null && client.isConnected() && !client.isClosed()) {
 										BufferedOutputStream bos = new BufferedOutputStream(client.getOutputStream());
 										long infoupdate=System.currentTimeMillis()+Config.getConfig().getFileTransferInfoInterval();
 										long transmitted =0;
 										byte[] cup = new byte[65535];
 										int len = -1;
 										while ((len = bis.read(cup)) != -1) {
 											bos.write(cup, 0, len);
 											transmitted+=len;
 											if(System.currentTimeMillis()>infoupdate) {
 												infoupdate = System.currentTimeMillis()+Config.getConfig().getFileTransferInfoInterval();
 												GUI.getGUI().info(tmp_FR.datei.getName()+"("+((transmitted*100)/tmp_FR.size)+"%)", tmp_FR.sender.getUserID(), 0);
 											}
 										}
 										bos.flush();
 										bos.close();
 										
 										GUI.getGUI().info(tmp_FR.datei.getName()+" Done", tmp_FR.sender.getUserID(), 0);
 									}
 									//Ergebnis melden
 								} catch (FileNotFoundException e) {
 									LogEngine.log("FileTransfer", e.getMessage(), LogEngine.ERROR);
 								} catch (SocketTimeoutException e) {
 									LogEngine.log("FileTransfer", "Timed Out", LogEngine.ERROR);
 									GUI.getGUI().info("User " + tmp_FR.receiver.getAlias()+ " has not answered in time. Connection Timedout", tmp_FR.receiver.getUserID(), 0);
 								} catch (SocketException e) {
 									LogEngine.log("FileTransfer", "Aborted", LogEngine.ERROR);
 								} catch (IOException e) {
 									LogEngine.log("FileTransfer", e);
 									GUI.getGUI().info("Transmission-Error, if this keeps happening buy a USB-Stick", tmp_FR.receiver.getUserID(), 0);
 								}
 							}
 						}).start();
 						
 						//Wait until ServerThread is Ready and...
 						synchronized (tmp_FR) {
 							try {
 								if(tmp_FR.server_port==-2)tmp_FR.wait();
 							} catch (InterruptedException e) {
 							}
 						}
 						//... send FileTransferRequest
 						MSG request = new MSG(tmp_FR,MSGCode.FILE_TCP_REQUEST,tmp_FR.getReceiver_nid());
 						routesend(request);
 
 						
 						
 					}
 				}
 			}).start();
 			}
 		}
 
 	private void recieve_file(final MSG data_paket) {
 		Object[] tmp = (Object[]) data_paket.getData();
 		FileTransferData tmp_file = (FileTransferData) tmp[0];
 		final File destination = ce.request_File(tmp_file);
 		
 		tmp_file.accepted=(destination!=null);
 		MSG reply = new MSG(tmp_file,MSGCode.FILE_RECIEVED,data_paket.getSender());
 //		reply.setEmpfnger(data_paket.getSender());
 		routesend(reply);
 		if(destination!=null) {
 		new Thread(new Runnable() {
 			public void run() {
 				try {
 					data_paket.save(destination);
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}).start();
 		
 	}
 	}
 	/*private void discover(Node newRoot) {
 		sendunicast(new MSG(meinNode, MSGCode.ROOT_DISCOVERY), newRoot);
 	}*/
 
 	private void discover() {
 		new Thread(new Runnable() {
 			public void run() {
 				sendmutlicast(new MSG(meinNode, MSGCode.ROOT_DISCOVERY));
 				rootMe = new Thread(new RootMe());
 				rootMe.start();
 			}
 		}).start();
 	}
 
 	private void sendDiscoverReply(Node quelle) {
 		LogEngine.log(this, "sending Replay to " + quelle.toString(), LogEngine.INFO);
 		sendunicast(new MSG(getBestNode(), MSGCode.ROOT_REPLY), quelle);
 	}
 
 	private void updateNodes() {
 		/*
 		 * if (isRoot()) { synchronized (allNodes) { allNodes.clear(); allNodes.add(getME()); allNodes.addAll(getChilds()); allNodes.notifyAll(); } } else sendroot(new MSG(null, MSGCode.POLL_ALLNODES));
 		 */
 		synchronized (allNodes) {
 			allNodes.clear();
 			allNodes.add(getMe());
 			allNodes.addAll(getChilds());
 			allNodes.notifyAll();
 		}
 		if (hasParent()) sendroot(new MSG(getNodes(), MSGCode.REPORT_ALLNODES));
 
 	}
 
 	private void pollChilds() {
 		for (ConnectionHandler x : connections) {
 			x.send(new MSG(null, MSGCode.POLL_CHILDNODES));
 		}
 	}
 
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
 		Socket tmp_socket = null;
 		for (InetAddress x : knoten.getSockets()) {
 			if (!meinNode.getSockets().contains(x)) {
 				try {
 					tmp_socket = new Socket(x.getHostAddress(),knoten.getServer_port());
 				} catch (UnknownHostException e) {
 					LogEngine.log(e);
 				} catch (IOException e) {
 					LogEngine.log(e);
 				}
 				if (tmp_socket != null && tmp_socket.isConnected())
 					break; // wenn eine Verbindung mit einer der IPs des
 							// Knotenaufgebaut wurden konnte. Hr auf
 			}
 		}
 		if (tmp_socket != null) {
 			try {
 				root_connection = new ConnectionHandler(tmp_socket);
 				setRootMode(false);
 				setGroup(myGroups);// FIXME:Bleibt das hier
 				sendroot(new MSG(getMe()));
 				sendroot(new MSG(myGroups, MSGCode.GROUP_REPLY));
 				sendroot(new MSG(null, MSGCode.POLL_ALLNODES));
 				sendroot(new MSG(null, MSGCode.GROUP_POLL));
 			} catch (IOException e) {
 				LogEngine.log(e);
 			}
 		}
 	}
 	
 
 	public void disconnect() {
 		online = false;
 		connectionsAcceptBot.stop();
 		multicastRecieverBot.stop();
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
 	 * Entfernt eine Verbindung wieder
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
 
 
 	private void sendRA() {
 		MSG ra= new MSG(meinNode, MSGCode.ROOT_ANNOUNCE,getNodes().size());
 //		ra.setEmpfnger(getNodes().size());
 		sendmutlicast(ra);
 		root_claims_stash.add(ra);
 	}
 
 	private void handleRootClaim(MSG paket) {
 		if(paket!=null) {
 			paket.reStamp(); //change timestamp to recieved time
 			root_claims_stash.offer(paket);
 		}
 		claimRoot();
 	}
 
 	/**
 	 * Hier wird das Paket verarbeitet und weitergeleitet. Diese Methode wird ausschlielich vom MulticastSocketHandler aufegrufen.
 	 * 
 	 * @param paket
 	 *            Das empfangene MulticastPaket
 	 */
 	public void handleMulticast(MSG paket) {
 		LogEngine.log(this, "handling [MC]", paket);
 		if (angler.check(paket)) return;
 		if (online && (paket.getTyp() == NachrichtenTyp.SYSTEM)) {
 			switch (paket.getCode()) {
 				case ROOT_REPLY:
 					if (!hasParent())connectTo((Node) paket.getData());
 					break;
 				case ROOT_DISCOVERY:
 					if (isRoot()) sendDiscoverReply((Node) paket.getData());
 					break;
 				case ROOT_ANNOUNCE:
 					if (!hasParent()) handleRootClaim(paket);
 					break;
 				case NODE_LOOKUP:
 					if((long)paket.getData()==meinNode.getNodeID())sendroot(new MSG(meinNode));
 					break;
 				case ALIAS_UPDATE:
 					updateAlias((String) paket.getData(), paket.getSender());
 					break;
 				case CMD_RECONNECT:
 					long payload = (Long)paket.getData();
 					if((payload==nodeID)||(payload==-1337)) {
 						if (root_connection!=null)root_connection.close();
 					}
 					
 					break;
 				default:
 					LogEngine.log(this, "handling [MC]:undefined", paket);
 			}
 		}
 	}
 
 	/**
 	 * Hier wird das Paket verarbeitet und weitergeleitet. Diese Methode wird ausschlielich von den ConnectionHandlern aufgerufen um empfange Pakete verarbeiten zu lassen.
 	 * 
 	 * @param paket
 	 *            Zu verarbeitendes Paket
 	 * @param quelle
 	 *            Quelle des Pakets
 	 */
 	@SuppressWarnings("unchecked")
 	public void handle(MSG paket, ConnectionHandler quelle) {
 		LogEngine.log(this, "handling[" + quelle + "]", paket);
 		
 		if (angler.check(paket)) return;
 		if((paket.getEmpfnger() != -1) && (paket.getEmpfnger() != nodeID))routesend(paket);
 		else {
 			switch (paket.getTyp()) {
 			case PRIVATE:
 				ce.put(paket);
 				break;
 			case GROUP:
 				groupRouteSend(paket,quelle);
 				ce.put(paket);
 				break;
 			case SYSTEM:
 				DatabaseEngine.createDatabaseEngine().put(paket);
 				switch (paket.getCode()) {
 				case NODE_UPDATE:
 					allnodes_add((Node) paket.getData());
 					sendtcpexcept(paket, quelle);
 					break;
 
 				case POLL_ALLNODES:
 					if (quelle != root_connection)
 						quelle.send(new MSG(getNodes(), MSGCode.REPORT_ALLNODES));
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
 					if (quelle != root_connection) quelle.removeChildren((Collection<Node>) paket.getData());
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
 					if (addGroup((Collection<String>) paket.getData()))
 						sendchild(paket, null);
 					break;
 				case GROUP_EMPTY:
 					if (removeGroup((Collection<String>) paket.getData()))
 						sendchild(paket, null);
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
 					recieve_file(tmp);
 					break;
 				case FILE_RECIEVED:
 					ce.inform((FileTransferData) paket.getData());
 					break;
 				case NODE_LOOKUP:
 					Node tmp_node = null;
 					if ((tmp_node = getNode((long) paket.getData())) != null)quelle.send(new MSG(tmp_node));
 					else
 						sendroot(paket);
 					break;
 				case PATH_PING_REQUEST:
 					if(paket.getEmpfnger()==nodeID)routesend(new MSG(paket.getData(),MSGCode.PATH_PING_RESPONSE,paket.getSender()));
 					else routesend(paket);
 					break;
 				case PATH_PING_RESPONSE:
 					if(paket.getEmpfnger()==nodeID)routesend(new MSG(paket.getData(),MSGCode.PATH_PING_RESPONSE,paket.getSender()));
 					else routesend(paket);
 					break;
 				case TREE_DATA_POLL:
 					sendroot(new MSG(getTree(), MSGCode.TREE_DATA));
 					break;
 				case CMD_SHUTDOWN:
 					System.exit(0);
 					break;
 				default:
 					LogEngine.log(this, "handling[" + quelle + "]:undefined", paket);
 					break;
 				}
 				break;
 			case DATA:
 				if(paket.getEmpfnger()!=nodeID)routesend(paket);
 				else recieve_file(paket);
 				break;
 			default:
 			}
 		}
 	}
 	
 
 
 	private void recieve_file(final FileTransferData tmp) {
 		new Thread(new Runnable() {
 			public void run() {
 				long until = System.currentTimeMillis()+Config.getConfig().getFileTransferTimeout()-1000;
 				final File destination = ce.request_File(tmp);
 				if(System.currentTimeMillis()<until) {
 				tmp.accepted = (destination != null);
 				MSG reply;
 				if(tmp.accepted)reply = new MSG(tmp, MSGCode.FILE_RECIEVED,tmp.getSender_nid());
 				else reply = new MSG(tmp.hashCode(), MSGCode.FILE_TCP_ABORT,tmp.getSender_nid());
 				routesend(reply);
 				if (destination != null) {
 					Socket data_con = null;
 					for (InetAddress ip : tmp.sender.getSockets()) {
 						if (!meinNode.getSockets().contains(ip))
 							try {
 								data_con = new Socket(ip, tmp.server_port);
 							} catch (IOException e) {
 								e.printStackTrace();
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
 
 	public void routesend(MSG paket) {
 		long empfnger = paket.getEmpfnger();
 		for (ConnectionHandler con : connections) {
 			if(con.hasChild(empfnger)) {
 				con.send(paket);
 				return;
 			}
 		}
 		if(hasParent())sendroot(paket);
 		else if(isRoot()) {//Node ist Wurzel des Baums und weiss nicht wo der Empfnger ist
 			Node tmp = retrieve(empfnger); //versuche Empfnger aufszuspren
 			if(tmp!=null) routesend(paket); //und Paket zuzustellen
 			else sendchild(new MSG(empfnger,MSGCode.NODE_SHUTDOWN),null); //weiss alle Clients an diesen Empfnger zu entfernen und alle offnen Fenster zu deaktivieren
 		}
 	}
 	
 	public void groupRouteSend(MSG paket,ConnectionHandler quelle) {
 		String gruppe = paket.getGroup();
 		for (ConnectionHandler con : connections) {
 			if((quelle!=con)&&con.getGroups().contains(gruppe)) {
 				con.send(paket);
 			}
 		}
 		if(hasParent()&&(root_connection!=quelle))sendroot(paket);
 	}
 
 	private boolean updateMyGroups() {
 		Set<String> aktuell = computeGroups();
 		synchronized (myGroups) {
 
 			if (aktuell.hashCode() != myGroups.hashCode()) {
 				Set<String> dazu = new HashSet<String>(aktuell);
 				dazu.removeAll(myGroups);
 				if(dazu.size()>0) {
 					sendroot(new MSG(dazu, MSGCode.GROUP_JOIN));
 					if(addGroup(dazu))sendchild(new MSG(dazu, MSGCode.GROUP_ANNOUNCE),null);
 				}
 
 				Set<String> weg = new HashSet<String>(myGroups);
 				weg.removeAll(aktuell);
 				if(weg.size()>0) {
 					sendroot(new MSG(weg, MSGCode.GROUP_LEAVE));
 					if(isRoot()) {
 						if(removeGroup(weg))sendchild(new MSG(weg, MSGCode.GROUP_EMPTY),null);
 					}
 				}
 
 				myGroups.clear();
 				myGroups.addAll(aktuell);
 				return true;
 			}
 			return false;
 		}
 	}
 
 	private void allnodes_remove(Collection<Node> data) {
 		synchronized (allNodes) {
 			int hash = allNodes.hashCode();
 			allNodes.removeAll(data);
 			allNodes.add(meinNode);
 			if(allNodes.hashCode()!=hash)allNodes.notifyAll();
 		}
 	}
 	
 	private void allnodes_remove(Node data) {
 		synchronized (allNodes) {
 			int hash = allNodes.hashCode();
 			allNodes.remove(data);
 			if(allNodes.hashCode()!=hash)allNodes.notifyAll();
 		}
 	}
 	
 	private void allnodes_add(Collection<Node> data) {
 		synchronized (allNodes) {
 			int hash = allNodes.hashCode();
 			allNodes.addAll(data);
 			if(allNodes.hashCode()!=hash)allNodes.notifyAll();
 		}
 	}
 	
 	private void allnodes_add(Node data) {
 		synchronized (allNodes) {
 			int hash = allNodes.hashCode();
 			allNodes.add(data);
 			if(allNodes.hashCode()!=hash)allNodes.notifyAll();
 			DatabaseEngine.createDatabaseEngine().put(data);
 		}
 	}
 	
 	private void allnodes_set(Collection<Node> data) {
 		synchronized (allNodes) {
 			int hash = allNodes.hashCode();
 			allNodes.clear();
 			allNodes.addAll(data);
 			allNodes.add(meinNode);
 			if(allNodes.hashCode()!=hash)allNodes.notifyAll();
 		}
 		DatabaseEngine.createDatabaseEngine().put(data);
 	}
 	
 	/** Starte Lookup fr {@link Node} mit der NodeID <code>nid</code>. Und versucht ihn neu Verbinden zu lassen bei Misserfolg.
 	 * @param nid ID des Nodes
 	 * @return das {@link Node}-Objekt oder <code>null</code> wenn der Knoten nicht gefunden wurde.
 	 */
 	private Node retrieve(long nid) {
 			sendmutlicast(new MSG(nid, MSGCode.NODE_LOOKUP));
 			MSG x = angler.fishfor(NachrichtenTyp.SYSTEM,MSGCode.NODE_UPDATE,nid,null,false,1000);
 			if (x != null) return (Node) x.getData();
 			else {
 				LogEngine.log("retriever", "NodeID:["+nid+"] konnte nicht aufgesprt werden und sollte neu Verbinden!!!",LogEngine.ERROR);
 				sendmutlicast(new MSG(nid, MSGCode.CMD_RECONNECT));
 				return null;
 			}
 	}
 
 	
 
 	private void setRootMode(boolean rootmode) {
 		this.rootMode = rootmode;
 		GUI.getGUI().setTitle("publicMAIN"+((rootmode)?"[ROOT]":"" ));
 		if(rootmode) setGroup(myGroups) ;
 	}
 
 	public long getNodeID() {
 		return nodeID;
 	}
 
 	/*public void setNodeID(long nodeID) {
 		this.nodeID = nodeID;
 	}*/
 
 	public void joinGroup(Collection<String> gruppen_namen, ConnectionHandler con) {
 		updateMyGroups();
 	}
 
 	public void leaveGroup(Collection<String> gruppen_namen, ConnectionHandler con) {
 		updateMyGroups();
 	}
 	
 	
 	public boolean removeGroup(Collection<String> gruppen_name) {
 		synchronized (allGroups) {
 			boolean x = allGroups.removeAll(gruppen_name);
 			allGroups.notifyAll();
 			return x;
 		}
 	}
 	
 	public void setGroup(Collection<String> groups) {
 		synchronized (allGroups) {
 			allGroups.clear();
 			allGroups.addAll(groups);
 			allGroups.notifyAll();
 		}
 	}
 	
 	
 	public boolean addGroup(Collection<String> groups) {
 		synchronized (allGroups) {
 		boolean x = allGroups.addAll(groups);
 		allGroups.notifyAll();
 		return x;
 		}
 	}
 
 	public boolean removeMyGroup(String gruppen_name) {
 		synchronized (myGroups) {
 			return myGroups.remove(gruppen_name);
 		}
 	}
 	
 	public boolean addMyGroup(String gruppen_name) {
 		synchronized (myGroups) {
 			return myGroups.add(gruppen_name);
 		}
 	}
 	
 	public Set<String>computeGroups(){
 		Set<String> tmpGroups = new HashSet<String>();
 		for (ConnectionHandler cur : connections) {
 			tmpGroups.addAll(cur.getGroups());
 		}
 		tmpGroups.addAll(ce.getMyGroups());
 		return tmpGroups;
 	}
 
 	public void updateAlias() {
 		String alias = ce.getAlias();
 		if(online&&(!alias.equals(meinNode.getAlias()))) {
 			sendmutlicast(new MSG(alias, MSGCode.ALIAS_UPDATE));
 			updateAlias(alias,nodeID);
 			
 		}
 	}
 	
 	public void pathPing(){
 		for (Node cur : getNodes()) {
 			GUI.getGUI().info(cur.toString() + ":" +pathPing(cur), null, 0);
 			
 		}
 	}
 	
 	public long pathPing(Node remote) {
 		if (remote.equals(meinNode))return 0;
 		else {
 			long currentTimeMillis = System.currentTimeMillis();
 			MSG paket = new MSG(currentTimeMillis, MSGCode.PATH_PING_REQUEST,remote.getNodeID());
 //			routesend(paket);
 			MSG response = angler.fishfor(NachrichtenTyp.SYSTEM, MSGCode.PATH_PING_RESPONSE, remote.getNodeID(),currentTimeMillis, true, 1000,paket);
 			if(response==null)return -1;
 			else {
 				return (System.currentTimeMillis()-currentTimeMillis);
 			}
 			
 		}
 	}
 	
 	private boolean updateAlias(String newAlias, long nid) {
 		Node tmp;
 		
 		synchronized (allNodes) {
 			if ((tmp = getNode(nid)) != null) {
 				if (!tmp.getAlias().equals(newAlias)) {
 					tmp.setAlias(newAlias);
 					allNodes.notifyAll();
 					GUI.getGUI().notifyGUI();
 					DatabaseEngine.createDatabaseEngine().put(allNodes); //TODO: nur aktualisierung wegschreiben
 					LogEngine.log(this,"User " +tmp.getAlias() + " has changed ALIAS to " + newAlias,LogEngine.INFO);
 					return true;
 				}
 			}
 			return false;
 		}
 	}
 
 	public void debug(String command, String parameter) {
 		switch (command) {
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
 			if(tmp!=null)routesend(new MSG(null, MSGCode.CMD_SHUTDOWN, tmp.getNodeID()));
 			break;
 		case "maxcon":
 			Config.getConfig().setMaxConnections(Integer.parseInt(parameter));
 			break;
 		case "bestnode":
 			long time=System.currentTimeMillis();
 			GUI.getGUI().info("Strategie:" +myStrategy.getClass().getSimpleName() + " MaxConnections:" + Config.getConfig().getMaxConnections(), null, 0);
 			GUI.getGUI().info(getBestNode().toString(), null, 0);
 			GUI.getGUI().info("took "+ (System.currentTimeMillis()-time) +" ms to evaluate"  , null, 0);
 			break;
 		case "strategy":
 			if(parameter.equals("random")) myStrategy=new RandomStrategy(nodeID);
 			else if(parameter.equals("breadth")) myStrategy=new BreadthFirstStrategy();
 			else if(parameter.startsWith("weighted")&&parameter.split(" ").length==4) {
 				myStrategy=new WeightedDistanceStrategy(Double.parseDouble(parameter.split(" ")[1]), Integer.parseInt(parameter.split(" ")[2]), Integer.parseInt(parameter.split(" ")[3]));
 			}
 			else GUI.getGUI().info("unknown Strategy [random, breadth, weighted 0.5 0 1]"  , null, 1); 
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
 		default:
 			LogEngine.log(this, "debug command not found", LogEngine.ERROR);
 			break;
 		}
 		
 	}
 	
 	public Node getTree() {
 		Node root = (Node) meinNode.clone();
 		        // Zuerst werden alle Knoten hergestellt...
 		for (final ConnectionHandler con : connections) {
 			Runnable tmp = new Runnable() {
 				public void run() {
 					con.send(new MSG(null, MSGCode.TREE_DATA_POLL));
 				}
 			};
 			MSG polled_tree = angler.fishfor(NachrichtenTyp.SYSTEM, MSGCode.TREE_DATA, null, null, true, Config.getConfig().getTreeBuildTime(), tmp);
 			if(polled_tree!=null) root.add((Node) polled_tree.getData());
 		}
 		        return root;
 		    
 	}
 	
 	public long getUIDforNID(long nid){
 		
 		Node node = getNode(nid);
 		if (node!=null)return node.getUserID();
 		else return -1;
 	}
 	
 	public void showTree() {
 	        TreeNode root = getTree();
 	        
 	        // Der Wurzelknoten wird dem neuen JTree im Konstruktor bergeben
 	        JTree tree = new JTree( root );
 	        
 	        // Ein Frame herstellen, um den Tree auch anzuzeigen
 	        JFrame frame = new JFrame( "publicMAIN - Topology" );
 	        frame.add( new JScrollPane( tree ));
 	        
 	        frame.setIconImage(new ImageIcon(Help.class.getResource("pM_Logo2.png")).getImage());
 	        frame.setMinimumSize(new Dimension(250, 400));
 	        frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
 	        frame.pack();
 	        frame.setLocationRelativeTo( null );
 	        frame.setVisible( true );
 	}
 	
 	
 	
 
 	/**
 	 * Definiert diesen Node nach einem Timeout als Wurzelknoten falls bis dahin keine Verbindung aufgebaut wurde.
 	 */
 	private final class RootMe implements Runnable {
 		public void run() {
 //			if (!online&&!isRoot()&&!rootDiscovering)	return;
 			if (!online||isRoot()||rootDiscovering) return;
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
 
 		/**
 		 * 
 		 */
 		
 	}
 	private synchronized void claimRoot() {
 			if(rootDiscovering==false) {
 				rootClaimProcessor=new Thread(new RootClaimProcessor());
 				rootClaimProcessor.start();
 			}
 	}
 	/**
 	 * Warte eine gewisse Zeit und Wertet dann alle gesammelten RoOt_AnOunCEs aus forder anschlieend vom Gewinner einen Knoten zum Verbinden an. Wenn der Knoten selber Gewonnen hat
 	 */
 	private final class RootClaimProcessor implements Runnable {
 		public void run() {
 			rootDiscovering=true;
 			LogEngine.log("DiscoverGame","started",LogEngine.INFO);
 			sendRA();
 			long until = System.currentTimeMillis() + ROOT_CLAIM_TIMEOUT;
 			while (System.currentTimeMillis() < until) {
 				try {
 					Thread.sleep(ROOT_CLAIM_TIMEOUT);
 				}
 				catch (InterruptedException e) {
 				}
 			}
 			
 			List <MSG> ra_replies=new ArrayList<MSG>();
 			ra_replies.addAll(root_claims_stash);
 			Collections.sort(ra_replies);
 			long deadline  = ra_replies.get(0).getTimestamp()+2* ROOT_CLAIM_TIMEOUT;
 			
 			Node toConnectTo = meinNode;
 			long maxPenunte = getNodes().size();
 			for (MSG x : root_claims_stash) {
 				if (x.getTimestamp() <= deadline) {
 					long tmp_size = x.getEmpfnger();
 					Node tmp_node = (Node) x.getData(); //	Cast Payload in ein Object Array und das 2. Object dieses Arrays in einen Node
 					if (tmp_size > maxPenunte || ((tmp_size == maxPenunte) && (tmp_node.getNodeID() > toConnectTo.getNodeID()))) {
 						toConnectTo = tmp_node;
 						maxPenunte = tmp_size;
 					}
 				}
 			}
 			
 			LogEngine.log("DiscoverGame","Finished:" + ((toConnectTo != meinNode)?"lost":"won")+"(" + root_claims_stash.size() +" participants)",LogEngine.INFO);
 			
 			if (toConnectTo == meinNode) setRootMode(true);
 			else discover(); //another root won and should be answeringconnectTo(toConnectTo);
 			root_claims_stash.clear();
 			rootDiscovering=false;
 		}
 	}
 
 	private final class MulticastReciever implements Runnable {
 		public void run() {
 			if(multi_socket==null)return;
 			while (true) {
 				byte[] buff = new byte[65535];
 				DatagramPacket tmp = new DatagramPacket(buff, buff.length);
 				try {
 					multi_socket.receive(tmp);
 					MSG nachricht = MSG.getMSG(tmp.getData());
 					LogEngine.log("multicastRecieverBot", "multicastRecieve", nachricht);
 					if (nachricht != null) handleMulticast(nachricht);
 				}
 				catch (IOException e) {
 					LogEngine.log(e);
 				}
 			}
 		}
 	}
 
 	private final class ConnectionsAccepter implements Runnable {
 		public void run() {
 			if(connections==null||server_socket==null)return;
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
