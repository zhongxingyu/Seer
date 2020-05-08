 /**
  * Coop Network Tetris — A cooperative tetris over the Internet.
  * 
  * Copyright © 2012  Calle Lejdbrandt, Mattias Andrée, Peyman Eshtiagh
  * 
  * Project for prutt12 (DD2385), KTH.
  */
 package cnt.network;
 import cnt.Blackboard;
 import cnt.messages.*;
 import cnt.game.Player;
 
 // Classes needed for UPnP
 import org.teleal.cling.*;
 import org.teleal.cling.model.message.header.*;
 import org.teleal.cling.model.message.*;
 import org.teleal.cling.model.meta.*;
 import org.teleal.cling.model.types.*;
 import org.teleal.cling.model.action.*;
 import org.teleal.cling.registry.*;
 import org.teleal.cling.support.igd.*;
 import org.teleal.cling.support.igd.callback.*;
 import org.teleal.cling.support.model.*;
 import org.teleal.cling.controlpoint.*;
 import cnt.util.IGDListener;
 
 import java.util.*;
 import java.io.*;
 import java.net.*;
 
 
 /**
  * <p>Connection Networking layer</p>
  * <p>
  *   This class sets up the sockets to be used by the client. 
  *   Provides socket sockets for higher levels of networking.
  * </p>
  *
  * @author  Calle Lejdbrandt, <a href="mailto:callel@kth.se">callel@kth.se</a>
  * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
  */
 public class ConnectionNetworking implements Blackboard.BlackboardObserver
 {
     /**
      * Constructor starting a cloud
      * 
      * @param  playerName  The local player's display bane
      */
     public ConnectionNetworking(final String playerName) 
     {
 	final Thread thread = new Thread()
 	        {
 		    /**
 		     * {@inheritDoc}
 		     */
 		    @Override
 		    public void run()
 		    {
 			synchronized (ConnectionNetworking.this.startMonitor)
 			{   try
 			    {   ConnectionNetworking.this.startMonitor.wait();
 			    }
 			    catch (final InterruptedException err)
 			    {   //Do nothing
 			}   }
 			try
 			{
 				ConnectionNetworking.this.startCreate(playerName);
 			} catch (IOException ioe)
 			{
 				Blackboard.broadcastMessage(new GameOver());
 			}
 		    }
 	        };
 	
 	thread.start();
 	try
 	{   Thread.sleep(75);
 	}
 	catch (final InterruptedException err)
 	{   //Do nothing
 	}
     }
     
     
     /**
      * Constructor connecting to a cloud
      * 
      * @param  playerName   The local player's display bane
      * @param  foreignHost  String with DNS name or IPv4 address to connect to
      * @param  port         The port to make the connection to
      */
     public ConnectionNetworking(final String playerName, final String foreignHost, final int port) 
     {
 	final Thread thread = new Thread()
 	        {
 		    /**
 		     * {@inheritDoc}
 		     */
 		    @Override
 		    public void run()
 		    {
 			synchronized (ConnectionNetworking.this.startMonitor)
 			{   
 				try
 			    	{   
 					System.err.print("\033[1;33mStarting upp ConnectionNetworking...\033[0m");
 					ConnectionNetworking.this.startMonitor.wait();
 					System.err.print("\033[1;33mDone\033[0m");
 			    	}
 			    catch (final InterruptedException err)
 			    {   
 				    System.err.println("\033[1;33mError starting Networking\033[0m");
 			}   }
 			
 			try
 			{
 				System.err.print("\033[1;33mTrying startJoin...\033[0m");
 				ConnectionNetworking.this.startJoin(playerName, foreignHost, port);
 				System.err.println("\033[1;33mDone\033[0m");
 			} catch (IOException ioe)
 			{
 			    System.err.println("\033[1;33mError on startJoin\033[0m");
 				Blackboard.broadcastMessage(new GameOver());
 			}
 		    }
 	        };
 	
 	thread.start();
 	try
 	{   Thread.sleep(75);
 	}
 	catch (final InterruptedException err)
 	{   //Do nothing
 	}
     }
     
     
     
     /**
      * Initialiser
      */
     {
 	Blackboard.registerObserver(this);
 
 	Runtime.getRuntime().addShutdownHook(new Thread()
 	        {
 		    /**
 		     * {@inheritDoc}
 		     */
 		    public void run()
 		    {
 			if (ConnectionNetworking.this.upnpKit == null)
 			    ConnectionNetworking.this.upnpKit.removePortForward();
 		    }
 	        });
     }
 
 
     /**
      * {@link GameNetworking} to communicate with
      */
     public GameNetworking gameNetworking = null;
 
     /**
      * Local player instance
      */
     public Player localPlayer;
     
     /**
      * The highest known ID
      */
     public int highestID = 0;
     
     /**
      * Local players ID, needed before local Player can be created
      */
     public int localID;
 
     /**
      * Foreign players ID, needed before local Player can be created
      */
     public int foreignID = -1;
 
     /**
      * The randomly picked port the client is running on
      */
     public int port;
 
     /**
      * Public ip. 
      */
     public Inet4Address externalIP;
 
     /**
      * Local ip. 
      */
     public Inet4Address internalIP;
 
     /**
      * Flag to set wheter we can act as a server; i.e. do we have access on public ip?
      */
     public boolean isServer;
 
     /**
      * Map of current threaded connections to use to store sockets
      */
     public final HashMap<Integer, Socket> sockets = new HashMap<Integer, Socket>();
     
     /**
      * The output stream to write to
      */
     public final HashMap<Integer, ObjectOutputStream> outputs = new HashMap<Integer, ObjectOutputStream>();
     
     /**
      * Map of current threaded connections to use to store ObjectInputStremas
      */
     public final HashMap<Integer, ObjectInputStream> inputs = new HashMap<Integer, ObjectInputStream>();
     
     /**
      * The UPnP RemoteService being used for UPnP devices
      */
     private UpnpService upnpService;
     
     /**
      * UPnP tool kit
      */
     UPnPKit upnpKit = null;
     
     /**
      * Set of all used IDs
      */
     public final HashSet<Integer> joinedIDs = new HashSet<Integer>();
     
     /**
      * Set of currently joined IDs
      */
     public final HashSet<Integer> connectedIDs = new HashSet<Integer>();
     
     /**
      * Start monitor
      */
     public final Object startMonitor = new Object();
 
     /**
      * HashSet keeping track of which messages has already been sent
      */
     public final HashSet<UUID> oldMessages = new HashSet<UUID>();
     
     
     
     /**
      * Starts a cloud
      * 
      * @param  playerName  The local player's display bane
      */
     void startCreate(final String playerName) throws IOException
     {
 	final int localPort = this.port = Toolkit.getRandomPort();
 	this.upnpKit = new UPnPKit();
 	
 	Blackboard.broadcastMessage(new SystemMessage(null, "Starting up sockets..."));
 	
 	// Check if public ip is same as internal ip
 	if (getExternalIP().equals(getInternalIP()) == false)
 	{
 	    Blackboard.broadcastMessage(new SystemMessage(null, "Public and Local ip differ. Trying UPnP"));
 	    if(this.upnpKit.createPortForward(localPort))
 		startTCP();
 	    else 
 	    {
 		// We are trying to start a cloud, but couldn't. Game exits
 		Blackboard.broadcastMessage(new GameOver());
 	    }
 	}
 	else
 	    startTCP();
 	
 	this.localPlayer = new Player(playerName, this.localID = 0, getExternalIP().getHostAddress(), getInternalIP().getHostAddress(), localPort, this.foreignID = 0);
 	Blackboard.broadcastMessage(new LocalPlayer(this.localPlayer));
 	Blackboard.broadcastMessage(new PlayerJoined(this.localPlayer));
     }
     
     /**
      * Connects to a cloud
      * 
      * @param  playerName   The local player's display bane
      * @param  foreignHost  String with DNS name or IPv4 address to connect to
      * @param  port         The port to make the connection to
      */
     void startJoin(final String playerName, final String foreignHost, final int port) throws IOException
     {
 	final int localPort = this.port = Toolkit.getRandomPort();
 	this.upnpKit = new UPnPKit();
 	
 	// Check if public ip is same as internal ip
 	if (getExternalIP().equals(getInternalIP()) == false)
 	    if (this.upnpKit.createPortForward(localPort))
 		startTCP();
 	    else
 		startLocalTCP();
 	else
 	    startTCP();
 	
 	TCPReceiver receiver = null;
 	try 
 	{
 	    System.err.print("\033[1;33mTrying to establish connections and streams...\033[0m");
 	    Socket connection = this.connect((Inet4Address)(InetAddress.getByName(foreignHost)), port, false);
 	    System.err.println(connection == null ? "\033[1;31mnull check\033[0m" : "\033[1;32mnull check\033[0m");
 	    ObjectOutputStream output = new ObjectOutputStream(new BufferedOutputStream(connection.getOutputStream()));
 	    output.flush();
 	    ObjectInputStream input = new ObjectInputStream(new BufferedInputStream(connection.getInputStream()));
 	    System.err.println("\033[1;33mDone\033[0m");
 	    
 	    System.err.print("\033[1;33mTrying to send Handshake...\033[0m");
 	    this.send(PacketFactory.createConnectionHandshake(), output);
 	    System.err.println("\033[1;33mAfter handshake\nOutputstream: " + (output == null ? "\033[1;31mNull\033[0m" : "\033[1;32mOK\033[0m"));
 	    
 	    // Get answer
 	    HandshakeAnswer answer = null;
 	    try
 	    {  
 		System.err.print("\033[1;33mGetting HandshakeAnswer...\033[0m");
 		answer = (HandshakeAnswer)(input.readObject());
 		System.err.println("\033[1;33mDone\033[0m");
 		this.foreignID = answer.server;
 		PacketFactory.setID(this.localID = answer.client);
 		FullUpdate update = (FullUpdate)(input.readObject());
 		this.gameNetworking.receive(PacketFactory.createBroadcast(update, true));
 	    }
 	    catch (Exception err)
 	    {   if (this.isServer)
 		    Blackboard.broadcastMessage(new SystemMessage(null, "Unable to contact friend."));
 		else
 		{
 		    Blackboard.broadcastMessage(new SystemMessage(null, "Unable to contact friend, and we are local. Game Over"));
 		    Blackboard.broadcastMessage(new GameOver());
 	    }   }
 	    
 	    System.err.print("\033[1;33mStarting listening thread...\033[0m");
 	    // By now we should have our ID and the ID from the host we connected to
 	    if (this.foreignID != -1)
 	    {
 		System.err.println("\033[1;33mSaving outputstreams\033[0m");
 		this.outputs.put(this.foreignID, output);
 		System.err.println("\033[1;33mOutputstream: " + (this.outputs.get(this.foreignID) == null ? "\033[1;31mNull\033[0m" : "\033[1;32mOK\033[0m"));
 		Thread.sleep(300);
 		receiver = new TCPReceiver(connection, input, this, this.foreignID);
 		Thread t = new Thread(receiver);
 		t.start();
 	    }
 	}
 	catch (IOException | InterruptedException ioe)
 	{
 		System.err.println("\033[1;33mError starting connection: " + ioe.getMessage() + "\033[0m");
 	}
 	
 	if (this.foreignID == -1)
 	{
 	    System.err.println("\033[1;31mFOREIGN ID IS -1\033[21;39m");
 	    return;
 	}
 	
 	
 	if (receiver != null)
 	{
 	    final TCPReceiver _receiver = receiver;
 	    synchronized (_receiver)
 	    {   try
 		{
 		    _receiver.wait();
 		}
 		catch (final InterruptedException err)
 		{
 		    //Do nothing
 	    }   }
 	}
 	
 	// Create the local player
 	this.localPlayer = new Player(playerName, this.localID, getExternalIP().getHostAddress(), getInternalIP().getHostAddress(), localPort, this.foreignID);
 	Blackboard.broadcastMessage(new LocalPlayer(this.localPlayer));
 	Blackboard.broadcastMessage(new PlayerJoined(this.localPlayer));
     }
     
     
     /**
      * Set {@link GameNetworking} instance to use
      */
     public void setGameNetworking(GameNetworking gameNetworking)
     {
 	this.gameNetworking = gameNetworking;
 	synchronized (this.startMonitor)
 	{   this.startMonitor.notify();
 	}
     }
     
     
     /**
      * Make a TCP serversocket to listen on incoming sockets
      */
     private void startTCP() 
     {
 	ServerSocket server = null;
 	try
 	{
 	    server = new ServerSocket(this.port);
 	}
 	catch (IOException err) 
 	{
 	    Blackboard.broadcastMessage(new SystemMessage(null, "Error: Cannot start ServerSocket. Something is wrong."));
 	    return;
 	}
 		
 	this.isServer = true;
 	
 	final Thread serverThread = new Thread(new TCPServer(server, this));
 	serverThread.setDaemon(true);
 	serverThread.start();
     }
     
     
     /**
      * Setup the client to be running locally. Without PublicIP or NAT.
      */
     private void startLocalTCP()
     {
 	this.isServer = false;
 	
 	Blackboard.broadcastMessage(new SystemMessage(null, "Running in local mode. Needs access to at least one server in cloud."));
 	
 	// Do nothing else, we can only use outgoing sockets
     }
     
     
     /**
      * Makes a connection to specefied IP and port
      *
      * @param  host  An {@link Inet4Address} to connect to
      * @param  port  A port number to connect to
      *
      * @return <code>Socket</code> on successfull connection. <code>null</code> otherwise.
      */
     public final Socket connect(Inet4Address host, int port, boolean save)
     {
 	Blackboard.broadcastMessage(new SystemMessage(null, "Initiating connection to ["+ host + ":" + port + "]"));
 	Socket connection = null;
 	try
 	{
 	    connection = new Socket(host, port);
 	}
 	catch (IOException err) 
 	{
 	    Blackboard.broadcastMessage(new SystemMessage(null, "Connection Failed"));
 	    return null;
 	}
 	
 	if (save) //This is mainly for the first connection out, we don't want to start a listener until we now what ID to map it to 
         {
 	    TCPReceiver receiver = new TCPReceiver(connection, this, this.foreignID);
 	    Thread t = new Thread(receiver);
 	    t.start();
 	}
 	
 	Blackboard.broadcastMessage(new SystemMessage(null, "Opened Socket"));
 	
 	return connection;
     }
     
     
     /**
      * Sends a message that came from somewhere and should be routed on
      */
     public void send(final Packet packet)
     {
 	send(packet, null);
     }
     
     
     /**
      * Sends a message thrue a specefied socket 
      *
      * @param connection Socket to use to send thrue
      */
     public void send(final Packet packet, final ObjectOutputStream output)
     {
 	System.out.println("\033[1;33msending: " + packet.getMessage().getClass() + "#" + packet.getMessage().getMessage() + "\033[0m");
 	this.oldMessages.add(packet.getUUID());
 	final int[] playerIDs;
 	//if (packet.getMessage() instanceof Broadcast)
 	if (packet.getMessage() instanceof Anycast == false)
 	{
 	    if (this.isConnected() == false)
 		return;
 		
 	    final Set<Integer> keySet = this.outputs.keySet();
 	    final Integer[] _playerIDs = new Integer[keySet.size()];
 	    this.outputs.keySet().toArray(_playerIDs);
 	    playerIDs = new int[_playerIDs.length];
 	    for (int i = 0, n = playerIDs.length; i < n; i++)
 		playerIDs[i] = _playerIDs[i];
 	}
 	else
 	    playerIDs = new int[0];
 	//else if (packet.getMessage() instanceof Whisper)
 	//{
 	//	playerIDs = new int[((Whisper)(packet.getMessage())).getReceiver()]; //may not be connected to us
 	//}
 	    
 	    
 	final ObjectOutputStream[] sendTo;
 	final int[] sendToID;
 	int ptr = 0;
 	if (packet.getMessage() instanceof Anycast)
 	{
 	    sendTo = new ObjectOutputStream[] { output };
 	    sendToID = new int[] { -1 };
 	    ptr = 1;
 	}
 	else
 	{
 	    sendTo = new ObjectOutputStream[playerIDs.length];
 	    sendToID = new int[playerIDs.length];
 	    for (final int player : playerIDs)
 		if (packet.addHasGotPacket(player))
 		{
 		    sendToID[ptr] = player;
 		    System.out.println("\033[1;33msendTo[" + ptr + "] := outputs[" + player + "] = " + this.outputs.get(player) + "\033[0m");
 		    sendTo[ptr++] = this.outputs.get(player);
 		}
 	}
 	    
 	for (int i = 0; i < ptr; i++)
 	    try
 	    {
 		sendTo[i].writeObject(packet);
 		sendTo[i].flush();
 	    }
 	    catch (IOException ioe)
 	    {
 		if (output != null)
 		    Blackboard.broadcastMessage(new SystemMessage(null, "Special Send failed!"));
 		else
 		{
 		    System.out.println("\033[1;31mError routing message to [" + sendToID[i] + "]: Skipping, he will get it in the full update he gets when he reconnects\033[21;39m");
		    if (sendToID[i] < this.localID)
			this.reconnect(sendToID[i]);
 		}
 	    }
     }
     
     
     /**
      * Adds the ID that needs to be reconnected to the Reconnector
      *
      * @param  id  the player ID that lost connection
      */
     public synchronized void reconnect(int id)
     {
 	Reconnector.getInstance(this).addID(id);
     }
     
     
     /**
      * Removes ID that connected to us from Reconnector
      *
      * @param id the player ID the connected
      */
     public synchronized void connected(int id)
     {
 	Reconnector.getInstance(this).removeID(id);
     }
 
     
     /**
      * Check to see if there is any connections to send to
      *
      * @return true if there is connections false otherwhise
      */
     private boolean isConnected()
     {
 	if (this.sockets.isEmpty())
 	{
 	    System.err.println("\033[1;33mChecking to see if we have connections to send to in ConectionNetworking, no connections\033[0m");
 	    return false;
 	}
 	return true;
     }
     
     
     /**
      * Retrive the internatl IP from the local host.
      *
      * Sets the local ip, and also returns it as a Inet4Address object.
      *
      * @return internal ip
      */
     private Inet4Address getInternalIP() throws UnknownHostException
     {
 	this.internalIP = (Inet4Address)(InetAddress.getByName(Toolkit.getLocalIP()));
 	return this.internalIP;
     }
 
     
     /**
      * Retrives the external ip adress by adress lookup.
      *
      * Lookups the external ip for the host. Uses a cache and only does lookups every 5min.
      *
      * @return external ip
      */
     private Inet4Address getExternalIP() throws IOException
     {
 	this.externalIP = (Inet4Address)(Inet4Address.getByName(Toolkit.getPublicIP()));
 	return this.externalIP;	
     }
     
     
     /**
      * Returns the highest known ID
      * 
      * @return  The highest known ID
      */
     public int getHighestID()
     {
 	return this.highestID;
     }
     
     
     /**
      * {@inheritDoc}
      */
     public void messageBroadcasted(final Blackboard.BlackboardMessage message)
     {
 	if (message instanceof PlayerRejoined)
 	{
 	    final Player player = ((PlayerRejoined)message).player;
 	    System.err.println("\033[33mPlayer rejoined: " + player + "\033[39m");
 
 	    this.joinedIDs.add(Integer.valueOf(player.getID()));
 	    this.connectedIDs.add(Integer.valueOf(player.getID()));
 	}
 	else if (message instanceof PlayerJoined)
         {
 	    final Player player = ((PlayerJoined)message).player;
 	    System.err.println("\033[33mPlayer joined: " + player + "\033[39m");
 	    
 	    if ((this.joinedIDs.contains(Integer.valueOf(player.getID()))) == false && (player.equals(this.localPlayer) == false))
 		this.highestID++;
 	    
 	    this.joinedIDs.add(Integer.valueOf(player.getID()));
 	    this.connectedIDs.add(Integer.valueOf(player.getID()));
 	} 
 	else if (message instanceof PlayerDropped)
 	{
 	    final Player player = ((PlayerDropped)message).player;
 	    System.err.println("\033[33mPlayer dropped: " + player + "\033[39m");
 	    this.connectedIDs.remove(Integer.valueOf(player.getID()));
 	}
 	
     }
     
 }
 
