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
 * @author Calle Lejdbrandt, <a href="mailto:callel@kth.se">callel@kth.se</a>
 */
 public class ConnectionNetworking
 {
 	/**
 	* Default Constructor
 	* <p>
 	* The default constructor will try to start a new cloud and act server.
 	*</p>
 	* 
 	* @param gameNetworking the GameNetworking instance to send objects to
 	*/
 	public ConnectionNetworking(GameNetworking gameNetworking, String playerName)
 	{
 	
 		this.gameNetworking = gameNetworking;
 	
 		Blackboard.broadcastMessage(new SystemMessage(null, "Starting upp sockets..."));
 
 		// Check if public ip is same as internal ip
 		if (!getExternalIP().equals(getInternalIP()))
 		{
 			Blackboard.broadcastMessage(new SystemMessage(null, "Public and Local ip differ. Trying UPnP"));
 			if(createPortForward())
 			{
 				startTCP();
 			} else 
 			{
 				// We are trying to start a cloud, but couldn't. Game exits
 				Blackboard.broadcastMessage(new GameOver());
 			}
 		} else
 			startTCP();
 
 		this.createPlayer(playerName);
 	}
 	
 	/**
 	* Constructor connecting to a cloud
 	* <p>
 	* Constructor which tries to connect to an already existing cloud.
 	*
 	* @param gameNetworking the gamenetworking instance to send objects to
 	*
 	* @param playerName name of local player
 	*
 	* @param foreignHost string with DNS name or IPv4 address to connect to
 	*
 	* @param port the port to make the connection to
 	*/
 	public ConnectionNetworking(GameNetworking gameNetworking, String playerName, String foreignHost, int port) 
 	{
 
 		this.gameNetworking = gameNetworking;
 
 		// Check if public ip is same as internal ip
 		if (!getExternalIP().equals(getInternalIP()))
 		{
 			if(createPortForward())
 			{
 				startTCP();
 			} else 
 			{
 				startLocalTCP();
 			}
 		} else
 			startTCP();
 
 		try 
 		{
 			Socket connection = this.connect(foreignHost, port, false);
 			ObjectInputStream input = new ObjectInputStream(new BufferedInputStream(connection.getInputStream()));
 			ObjectOutputStream output = new ObjectOutputStream(new BufferedOutputStream(connection.getOutputStream()));
 
 			// Handshake(ID (0< if asking), urgent?, ObjectOutputStream)
 			this.send( new Handshake(-1), true, output );
 	
 			// Get answer
 			HandshakeAnswer answer = null;
 			try
 			{
 				answer = input.readObject();
 			} catch (Exception err) {
 				if (this.isServer)
 					Blackboard.broadcastMessage(new SystemMessage(null, "Unable to conact friend."));
 				else
 				{
 					Blackboard.broadcastMessage(new SystemMessage(null, "Unable to contact friend, and we are local. Game Over"));
 					Blackboard.broadcastMessage(new GameOver());
 				}
 			}
 			
 			
 			// By now we should have our ID and the ID from the host we connected to
 			if (this.foreignID != null)
 			{
 				this.outputs.put(this.foreignID, output);
 				TCPReceiver receiver = new TCPReceiver(connection, input, this.gameNetworking, this);
 				Thread t = new Thread(receiver);
 				t.start();
 			}
 			
 		} catch (IOException ioe)
 		{
 			// blackboardmessage!
 		}
 		
 		// Create the local player
 		this.createPlayer(playerName);
 		
 	}
 
 	/**
 	* ObjectNetworker to communicate with
 	*/
 	public final GameNetworking gameNetworking;
 
 	/**
 	* Local player instance
 	*/
 	public final Player localPlayer;
 	
 	/**
 	* Local UUID
 	*/
 	public final UUID localUUID;
 	/**
 	* Local players ID, needed before local Player can be created
 	*/
 	public final int localID;
 
 	/**
 	* Foreign players ID, needed before local Player can be created
 	*/
 	public final int foreignID;
 
 	/**
 	* The randomly picked port the client is running on
 	*/
 	public final int port;
 
 	/**
 	* Public ip. 
 	*/
 	public Inet4Address externalIP;
 
 	/**
 	* Local ip. 
 	*/
 	public Inet4Address internalIP;
 
 	/**
 	* Flag to set wheter we can act as a server. I.e. do we have access on public ip?
 	*/
 	public boolean isServer;
 
 	/**
 	* Map of current threaded connections to use to store sockets
 	*/
 	final HashMap<Integer, Socket> sockets = new HashMap<Integer, Socket>();
 
 	/**
 	* The output stream to write to
 	*/
 	final HashMap<Integer, ObjectOutputStream> outputs = new HashMap<Integer, ObjectOutputStream>();
 
 	/**
 	* Map of current threaded connections to use to store ObjectInputStremas
 	*/
 	final HashMap<Integer, ObjectInputStream> inputs = new HashMap<Integer, ObjectInputStream>();
 
 	/**
 	* The UPnP RemoteService being used for UPnP devices. 
 	*/
 	private UpnpService upnpService;
 
 	/**
 	* Make a TCP serversocket to listen on incoming sockets
 	*
 	*/
 	private void startTCP() 
 	{
 		ServerSocket _server = null;
 		try {
 			if (this.port != null) 
 			{
 				_server = new ServerSocket(this.port);
 			} else 
 			{
 				_server = new ServerSocket(0);
 				this.port = _server.getLocalPort();
 			}
 		} catch (IOException err) 
 		{
 			Blackboard.broadcastMessage(new SystemMessage(null, "Error: Cannot start ServerSocket. Something is wrong."));
 			return;
 		}
 		
 		this.isServer = true;
 		
 		final Thread serverThread = new Thread(new TCPServer(_server, this.gameNetworking, this));
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
 	* @param host an Inet4Address to connect to
 	* @param port an portnumber to connect to
 	*
 	* @return <code>Socket</code> on successfull connection. <code>null</code> otherwise.
 	*/
 	public final Socket connect(Inet4Address host, int port, boolean save)
 	{
 		Blackboard.broadcastMessage(new SystemMessage(null, "Initiating connection to ["+ host + ":" + port + "]"));
 		Socket connection = null;
 		try {
 			connection = new Socket(host, port);
 		} catch (IOException err) 
 		{
 			Blackboard.broadcastMessage(new SystemMessage(null, "Connection Failed"));
 			return null;
 		}
 		
 		if (save) //This is mainly for the first connection out, we don't want to start a listener until we now what ID to map it to 
 		{
 			TCPReceiver receiver = new TCPReceiver(connection, this.gameNetworking, this);
 			Thread t = new Thread(receiver);
 			t.start();
 		}
 
 		Blackboard.broadcastMessage(new SystemMessage(null, "Number of open sockets: " + this.sockets.size()));
 
 		return connection;
 	}
 	
 	/**
 	* Sends message that originated on local client
 	*
 	* @param message Message that should be sent
 	* @param urgent true if it is an urgentmessage, false if it is normal priority
 	*/
 	public void send(NetworkMessage message, boolean urgent)
 	{ 
 		if (!this.isConnected()) {
 		    return;
 		}
 		// Get the players we have connections to so we know who we send to
 		int[] playerIDs = this.outputs.keySet().toArray(new int[0]);
 		
 		int[] sentTo = Array.copyOf(playerIDs, playerIDs.length + 1);
 		sentTo[sentTo.length - 1] = this.localID;
 		
 		
 		Packet packet = new Packet(message, urgent, sentTo);
 
 		for (int id : playerIDs)
 		{
 			try
 			{
 				this.outputs.get(id).writeObject(packet);
 				this.outputs.get(id).flush();
 			} catch (IOException ioe) {
 				
 				System.err.println("\n\nError sending message to [" + id + "]: Skipping, he will get it in the full update he gets when he reconnects\n");
 				if (id < this.localID)
 					this.reconnect(id);
 			}
 		}
 	}
 
 	/**
 	* Sends a message that came from somewhere and should be routed on
 	*
 	* @param message Message that should be sent
 	* @param urgent true if it is an urgentmessage, false if it is normal priority
 	* @param sentTo list of player ID who already gotten the message
 	*/
 	public void send(NetworkMessage message, boolean urgent, int[] sentTo)
 	{
 		if (!this.isConnected()) {
 			return;
 		}
		
		final Set<Integer> keySet = this.outputs.keySet();
		final Integer[] _playerIDs = new Integer[keySet.size()];
		this.outputs.keySet().toArray(_playerIDs);
		final int[] playerIDs = new int[_playerIDs.length];
		for (int i = 0, n = playerIDs.length; i < n; i++)
		    playerIDs[i] = _playerIDs[i];
 
 		ArrayList<Integer> sendList = new ArrayList<Integer>();
 
 		sendList.addAll(Arrays.asList(sentTo));
 		if (!sendList.contains(Integer.valueOf(this.localID)))
 			sendList.add(Integer.valueOf(this.localID));
 
 		for (int id : playerIDs)
 		{
 			if (!sendList.contains(Integer.valueOf(id)))
 				sendList.add(Integer.valueOf(id));
 		}
 		
 		Packet packet = new Packet(message, urgent, 0);
 
 		for (int id : playerIDs)
 		{
 			if (!sentTo.contains(id)) 
 			{
 				try
 				{
 					this.outputs.get(id).writeObject(packet);
 					this.outputs.flush();
 				} catch (IOException ioe)
 				{
 					System.err.println("\n\nErrer routing message to [" + id +"]: Skipping, he will get it in the full update he gets when he reconnects\n");
 					if (id < this.localID)
 						this.reconnect(id);
 				}
 			}
 		}
 	}
 
 	/**
 	* Sends a message thrue a specefied socket 
 	*
 	* @param message Message that should be sent
 	* @param urgent true if it is an urgentmessage, false if it is normal priority
 	* @param connection Socket to use to send thrue
 	*/
 	public void send(NetworkMessage message, boolean urgent, ObjectOutputStream output)
 	{
 		
 		// Get the players we have connections to so we know who we send to
		final Set<Integer> keySet = this.outputs.keySet();
		final Integer[] _playerIDs = new Integer[keySet.size()];
		this.outputs.keySet().toArray(_playerIDs);
		final int[] playerIDs = new int[_playerIDs.length];
		for (int i = 0, n = playerIDs.length; i < n; i++)
		    playerIDs[i] = _playerIDs[i];
 		
 		int[] sentTo = Array.copyOf(playerIDs, playerIDs.length + 1);
 		sentTo[sentTo.length - 1] = this.localID;
 
 		Packet packet = new Packet(message, urgent, sentTo);
 		try 
 		{
 			output.writeObject(packet);
 			output.flush();
 		
 		} catch (IOException ioe)
 		{
 			Blackboard.broadcastMessage(new SystemMessage(null, "Special Send failed!"));
 		}	
 
 	}
 
 	/**
 	* Adds the ID that needs to be reconnected to the Reconnector
 	*
 	* @param id the player ID that lost connection
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
 		if (this.sockets.isEmpty()) {
 			Blackboard.broadcastMessage(new SystemMessage(null, "No connections found"));
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
 	private Inet4Address getInternalIP()
 	{
 
 		this.internalIP = Toolkit.getLocalIP();
 		return this.internalIP;
 	}
 	
 	/**
 	* Retrives the external ip adress by adress lookup.
 	*
 	* Lookups the external ip for the host. Uses a cache and only does lookups every 5min.
 	*
 	* @return external ip
 	*/
 	private Inet4Address getExternalIP()
 	{
 		this.externalIP = (Inet4Address)Inet4Address.getByName(Toolkit.getPublicIP());
 		return this.externalIP;	
 	}
 
 	/**
 	* Tries to find any UPNP enabled device that is a Internet Gateway Device.
 	* If found a portforward, try to make a portforward.
 	*
 	* @param port the port to be forwarded
 	*
 	* @return returns <code>true</code> on succes, <code>false</code> other whise.
 	*/
 	private boolean createPortForward(int port)
 	{
 		// NOTE: This UPNP implementation is multithreaded. 
 		// Any Device is found asynchronously.
 
 		// monitor object to be used for the search for a device
 		Object monitor = new Object();
 
 		this.upnpService = new UpnpServiceImpl();
 
 		//The IGDListener class is custom made and resides in util
 		this.upnpService.getRegistry().addListener(new IGDListener(upnpService, monitor, port));
 		// Set the service we want to search for (makes MUCH less network congestion if network has many UPnP devices)
 		ServiceType _type = new UDAServiceType("WANIPConnection");
 
 		//Initiate a standard search
 		//this.upnpService.getControlPoint().search(new ServiceTypeHeader(_type));
 		// Testing default serach
 		this.upnpService.getControlPoint().search(new STAllHeader());
 		try
 		{
 			// Devices are discovered asynchronously, but should be faster then 5 sec.
 		        synchronized (monitor)
 			{
 			    monitor.wait(5000);
 			}
 		} catch (InterruptedException ie)
 		{
 			// {ignore and continue}
 		}
 		
 		// If we found the correct device, we have also made a PortForardd. 
 		// If no PortForward could be done, device is removed before this check. 
 		// Probably... depending on timeout and asymchronous behaviour.
 		if (this.upnpService.getRegistry().getDevices(_type).size() > 0)
 		{
 			Blackboard.broadcastMessage(new SystemMessage(null, "We have " + this.upnpService.getRegistry().getDevices(_type).size() + " IGD(s) "));
 			return true;
 		} else
 		{
 			Blackboard.broadcastMessage(new SystemMessage(null, "We have 0 IGDs"));
 			return false;
 		}
 	}
 	
 	/**
 	* Removes the portforwaring on any UPnP device that was discovered during startup connecting
 	*
 	*/
 	private void removePortForward()
 	{
 		// Monitor object - see createPortForward
 		Object monitor = new Object();
 
 		// RemoteService we are interested in - see createPortForward
 		ServiceType _type = new UDAServiceType("WANIPConnection");
 		
 		// Se if we actually have a UpnpService and devices
 		if (this.upnpService != null && this.upnpService.getRegistry().getDevices(_type).size() > 0)
 		{
 			// For all IGDs, remove portmappings
 			for (RemoteDevice device : (RemoteDevice[])this.upnpService.getRegistry().getDevices(_type).toArray())
 			{
 				RemoteService portMap = device.findService(_type);
 				// This retrives the IGDListener instances and executes demapPort()
 				// the iterator is needed as it's unknown what kind of collection is retrived from Cling
 				
 				IGDListener _listener = (IGDListener)this.upnpService.getRegistry().getListeners().iterator().next();
 				_listener.demapPort(portMap, this.upnpService.getRegistry(), device);
 			}
 		}
 	}	
 }
