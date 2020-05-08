 /**
  * Coop Network Tetris — A cooperative tetris over the Internet.
  * 
  * Copyright © 2012  Calle Lejdbrandt, Mattias Andrée, Peyman Eshtiagh
  * 
  * Project for prutt12 (DD2385), KTH.
  */
 package cnt.network;
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
 	* Monitor object to use for internal monitoring
 	*/
 	private final Object monitor = new Object();
 
 	/**
 	* Set of IDs for the connections that are dead and we need to reconnect,
 	*/
 	private HashSet<Integer> deadIDs = new HashSet<Integer>();
     
     
     
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
 	* Add an ID discovered to be dead and send notify to monitor to start reconnecting
 	*
 	* @param id Player id to add to the set
 	*/
 	public synchronized void addID(int id)
 	{
 		this.deadIDs.add(id);
 		synchronized (monitor)
 		{   monitor.notify();
 		}
 	}
 
 	/**
 	* Remove an ID discovered to be connected again.
 	*/
 	public synchronized void removeID(int id)
 	{
 		if (this.deadIDs.contains(id))
 			this.deadIDs.remove(id);
 	}
 
 	/**
 	* Start trying to reconnect to IDs in the list.
 	*/
 	protected synchronized void reconnect()
 	{
 		while (true)
 		{
 			if (this.deadIDs.isEmpty())
 			    synchronized (monitor)
 			    {
 				try
 				{   monitor.wait();
 				}
 				catch (final InterruptedException err)
 				{   return;
 				}
 			    }
			
 			if (this.deadIDs.contains(this.connectionNetowkring.foreignID))
 			{
 				int id = this.connectionNetworking.foreignID; // less to type
 				
 				Socket dead = this.connectionNetworking.connections.get(id);
 				Socket connection = this.connectionNetworking.connect(dead.getLocalHostAddress(), dead.getLocalPort(), true);
 				if (connection == null)
 				{
 					//* We couldn't connect so we dop the player
				        Blackboard.broadcastMessage(new PlayerDropped(Player.getInstance(id)));
 					this.deadIDs.remove(id);
 					continue;
 				}
 				
 				ObjectOutputStream output = new ObjectOutputStream(new BufferedOutputStream(connection.getOutputStream()));
 				ObjectInputStream input = new ObjectInputStream(new BufferedInputStream(connection.getInputStream()));
 
 				this.connectionNetworking.send(new Handshake(this.connectionNetworking.localID), true, output);
 				
 				HandshakeAnswer answer = null;
 				try
 				{
 					answer = input.readObject();
 				} catch (Exception err) {
 					if (this.connectionNetworking.isServer)
 					{
						Blackboard.broadcastMessage(new PlayerDropped(Player.getInstance(id)));
 						this.deadIDs.remove(id);
 					} else
 					{
 						
 }}}}}}
