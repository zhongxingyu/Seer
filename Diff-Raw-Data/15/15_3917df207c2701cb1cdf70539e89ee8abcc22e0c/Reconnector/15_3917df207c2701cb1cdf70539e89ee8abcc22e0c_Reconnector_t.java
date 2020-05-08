 /**
  * Coop Network Tetris — A cooperative tetris over the Internet.
  * 
  * Copyright © 2012  Calle Lejdbrandt, Mattias Andrée, Peyman Eshtiagh
  * 
  * Project for prutt12 (DD2385), KTH.
  */
 package cnt.network;
 
 import cnt.Blackboard;
 import cnt.Blackboard.*;
 import cnt.messages.*;
 import cnt.game.*;
 import cnt.util.*;
 import cnt.*; //Blackboard class to send messages with
 
 import java.util.*;
 import java.net.*;
 import java.io.*;
 
 
 public class Reconnector
 {
 	
 	/**
 	* Constructor using Singleton pattern
 	*/
 	private Reconnector(ConnectionNetworking connectionNetworking)
 	{
 		this.connectionNetworking = connectionNetworking;
 		this.reconnect();
 	}
 	
     
     
 	/**
 	* The instance of the Reconnector
 	*/
 	private static Reconnector instance;
 
 	/**
 	* ConnectionNetworking instance to use
 	*/
 	private final ConnectionNetworking connectionNetworking;
     
 	/**
 	 * Monitor object to use for internal monitoring of PlayerRejoins
 	 */
 	private final Object joined = new Object();
 
 	/**
 	* Set of IDs for the connections that are dead and we need to reconnect,
 	*/
 	private HashSet<Integer> deadIDs = new HashSet<Integer>();
 
 	/**
 	 * Checks if a player has joined in a specefic timeframe
 	 */
 	private boolean playerJoined = false;
     
     
     
 	/**
 	* Method to get the instance of Reconnector
 	*
 	* @return the Reconnector instance
 	*/
 	public static Reconnector getInstance(ConnectionNetworking connectionNetworking)
 	{
 	    if (instance == null)
 		synchronized (Reconnector.class)
 		{
 		    if (instance == null)
 			instance = new Reconnector(connectionNetworking);
 		}
 	    
 	    return instance;
 	}
 
 	/**
 	 * Initializer
 	 */
 	{
 		Blackboard.registerObserver(this);
 	}
 
 	/**
 	* Add an ID discovered to be dead and send notify to monitor to start reconnecting
 	*
 	* @param id Player id to add to the set
 	*/
 	public void addID(int id)
 	{
 		synchronized (this.deadIDs)
 		{   	
 			this.deadIDs.add(id);
 			this.deadIDs.notify();
 		}
 	}
 
 	/**
 	* Remove an ID discovered to be connected again.
 	*/
 	public void removeID(int id)
 	{
 		synchronized (this.deadIDs)
 		{
 			this.deadIDs.remove(id);
 		}
 	}
 
 	/**
 	* Start trying to reconnect to IDs in the list.
 	*/
 	protected synchronized void reconnect()
 	{
 		while (true)
 		{
 			if (this.deadIDs.isEmpty())
 			    synchronized (this.deadIDs)
 			    {
 				try
 				{   this.deadIDs.wait();
 				}
 				catch (final InterruptedException err)
 				{   return;
 				}
 			    }
 			/* Check if selfdead */
 			try
 			{
 				Toolkit.getPublicIP();
 			} catch (IOException err)
 			{
 				Blackboard.broadcastMessage(new GameOver());
 				Blackboard.broadcastMessage(new PlayerDropped(Player.getInstance(this.connectionNetworking.localID)));
 				return;
 			}
 
 			if (this.deadIDs.contains(this.connectionNetworking.foreignID))
 			{
 				int id = this.connectionNetworking.foreignID; // less to type
 				
 				Socket dead = this.connectionNetworking.sockets.get(id);
 				Socket connection = this.connectionNetworking.connect((Inet4Address)(dead.getInetAddress()), dead.getPort(), true);
 				if (connection != null && handleConnection(connection, id))
 					continue;
 			
 			
 				ArrayList<Player> players = new ArrayList<Player>();
 				for (int playerID : this.connectionNetworking.connectedIDs)
 				{
 					Player player = Player.getInstance(playerID);
 					if (this.deadIDs.contains(playerID) == false && 
 					    this.connectionNetworking.localID != playerID && 
 					    playerID < this.connectionNetworking.localID && 
 					    player.getConnectedTo() == this.connectionNetworking.foreignID && 
 					    player.getPublicIP() == Toolkit.getPublicIP())
 					{
 						players.add(player);
 					}
 				}
 				
 				if (players.isEmpty() == false)
 				{
 					Collections.sort(players);
 					Player player = players.get(0);
 					Socket connection = this.connectionNetworking.connect((Inet4Address)(InetAddress.getByName(player.getLocalIP())), player.getPort(), false);
 					if (connection != null && handleConnection(connection, player.getID()))
 						continue;
 					else
 					{
 						Socket connection = this.connectionNetworking.connect((Inet4Address)(InetAddress.getByName(player.getPublicIP())), player.getPort(), false);
 						if (connection != null)
 						{
 							handleConnection(connection, player.getID());
 							continue;
 						}
 					}
 				}
 				
 				for (int playerID : this.connectionNetworking.connectedIDs)
 				{
 					Player player = Player.getInstance(playerID);
 					if (this.deadIDs.contains(playerID) == false &&
 					    this.connectionNetworking.localID != playerID &&
 					    playerID < this.connectionNetworking.localID &&
 					    player.getConnectedTo() == this.connectionNetworking.connectionNetworking.foreignID &&
 					    player.getPublicIP() != Toolkit.getPublicIP())
 					{
 						players.add(player);
 					}
 				}
 
 				if (players.isEmpty() == false)
 				{
 					Collections.sort(players);
 					Player player = players.get(0);
					Socket connection = this.connectionNetworking.connect((Inet4Address)(InetAddress.getByName(player.getLocalIP())), player.getPort(), false);
 					if (connection != null)
 					{
 						handleConnection(connection, plauer.getID());
 						continue;
 					}
 				}
 
 				for (int playerID : this.connectionNetworking.connectedIDs)
 				{
 					Player player = Player.getInstance(playerID);
 					if (this.deadIDs.contains(playerID) == false && 
 					    this.connectionNetworking.localID != playerID && 
 					    playerID < this.connectionNetworking.localID && 
 					    player.getConnectedTo() != this.connectionNetworking.foreignID && 
 					    player.getPublicIP() == Toolkit.getPublicIP())
 					{
 						players.add(player);
 					}
 				}
 				
 				if (players.isEmpty() == false)
 				{
 					Collections.sort(players);
 					Player player = players.get(0);
 					Socket connection = this.connectionNetworking.connect((Inet4Address)(InetAddress.getByName(player.getLocalIP())), player.getPort(), false);
 					if (connection != null && handleConnection(connection, player.getID()))
 						continue;
 					else
 					{
 						Socket connection = this.connectionNetworking.connect((Inet4Address)(InetAddress.getByName(player.getPublicIP())), player.getPort(), false);
 						if (connection != null)
 						{
 							handleConnection(connection, player.getID());
 							continue;
 						}
 					}
 				}
 
 				for (int playerID : this.connectionNetworking.connectedIDs)
 				{
 					Player player = Player.getInstance(playerID);
 					if (this.deadIDs.contains(playerID) == false &&
 					    this.connectionNetworking.localID != playerID &&
 					    playerID < this.connectionNetworking.localID &&
 					    player.getConnectedTo() != this.connectionNetworking.connectionNetworking.foreignID &&
 					    player.getPublicIP() != Toolkit.getPublicIP())
 					{
 						players.add(player);
 					}
 				}
 
 				if (players.isEmpty() == false)
 				{
 					Collections.sort(players);
 					Player player = players.get(0);
					Socket connection = this.connectionNetworking.connect((Inet4Address)(InetAddress.getByName(player.getLocalIP())), player.getPort(), false);
 					if (connection != null)
 					{
 						handleConnection(connection, plauer.getID());
 						continue;
 					}
 				}
 			}
 			
 			for(;;)
 			{
 				synchronized (joined)
 				{
 					this.playerJoined = false;
 					joined.wait(2000);
 					if (this.playerJoined == false)
 					{
 						synchronized (this.deadIDS)
 						{
 							for (int playerID : this.deadIDs)
 							{
 								Blackboard.broadcaseMessage(new PlayerDropped(Player.getInstance(playerID)));
 							}
 
 							this.deadIDs.clear();
 						}
 						break;
 					}
 				}
 			}
 
 		}
 	}
 
 	private boolean handeConnection(Socket connection, int id)
 	{
 		try
 		{	
 	    		ObjectOutputStream output = new ObjectOutputStream(new BufferedOutputStream(connection.getOutputStream()));
 	    		ObjectInputStream input = new ObjectInputStream(new BufferedInputStream(connection.getInputStream()));
 				    		
 	    		this.connectionNetworking.send(PacketFactory.createReconnectionHandshake(id), output);
 				    		
 	    		HandshakeAnswer answer = null;
 	    		answer = (HandshakeAnswer)(input.readObject());
 
 			FullUpdate update = (FullUpdate)(input.readObject());
 			Blackboard.broadcastMessage(update);
 
 			this.connectionNetworking.sockets.put(id, connection);
 			this.connectionNetworking.inputs.put(id, input);
 			this.connectionNetworking.outputs.put(id, output);
 			this.connectionNetworking.foreignID = id;
 
 			TCPReceiver receiver = new TCPReceiver(connection, input, this.connectionNetworking, id);
 	    		Thread t = new Thread(receiver);
 	    		t.start();
 
 			Blackboard.broadcastMessage(new PlayerRejoined(Player.getInstance(id)));
 
 			this.deadIDs.remove(id);
 			return true;
 
 		} catch (Exception err) {
 			Blackboard.broadcastMessage(new PlayerDropped(Player.getInstance(id)));
 			if (deadIDs.contains(id) == false)
 				deadIDs.add(id);
 		}
 
 		return false;
 	}
 
 	public void messageBroadcasted(final BlackboardMessage message)
 	{
 		if (message instanceof PlayerRejoined)
 		{
			synchronized (joined)
 			{
 				try
 				{
 					joined.notifyAll();
 				} catch (InterruptedException ie)
 				{
 					return;
 				}
 			}
 		}
 	}
 
 }
