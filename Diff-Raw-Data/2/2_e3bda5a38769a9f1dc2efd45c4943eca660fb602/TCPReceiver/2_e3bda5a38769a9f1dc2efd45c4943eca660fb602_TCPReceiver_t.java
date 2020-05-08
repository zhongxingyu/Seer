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
 import cnt.game.Player;
 
 import java.util.*;
 import java.io.*;
 import java.net.*;
 
 
 /**
 * The TCP handler for an incoming connection
 * 
 * @author  Calle Lejdbrandt, <a href="mailto:callel@kth.se">callel@kth.se</a>
 */
 public class TCPReceiver implements Runnable
 {
     /**
      * Constructor 
      *
      * @param  connection            The incoming connection as a socket
      * @param  connectionNetworking  The {@link ConnectionNetworking} instace to map peer and socket in
      * @param foreignID The ID of the remote player
      */
     public TCPReceiver(Socket connection, ConnectionNetworking connectionNetworking, int foreignID)
     {
 	
 	    this(connection, null, connectionNetworking, foreignID);
     }
 
     /**
      * Constructor 
      *
      * @param  connection            The incoming connection as a socket
      * @param  connectionNetworking  The {@link ConnectionNetworking} instace to map peer and socket in
      */
     public TCPReceiver(Socket connection, ConnectionNetworking connectionNetworking)
     {
 	
 	    this(connection, null, connectionNetworking, -1);
     }
 
     /**
      * Constructor
      *
      * @param  connection            The incoming connection as a socket
      * @param  stream                The {@link ObjectInputStream} to use
      * @param  connectionNetworking  The {@link ConnectionNetworking} instace to map peer and socket in
      * @param foreignID The ID of the remote player
      */
     public TCPReceiver(Socket connection, ObjectInputStream stream, ConnectionNetworking connectionNetworking, int foreignID)
     {
 	this.connection = connection;
 	this.input = stream;
 	this.gameNetworking = connectionNetworking.gameNetworking;
 	this.connectionNetworking = connectionNetworking;
 	this.foreignID = foreignID;
 
     }
     
     
     
     /**
      * the Socket to use for incoming streams
      */
     private final Socket connection;
 	
     /**
      * the GameNetworking instance to send objects to
      */
     private final GameNetworking gameNetworking;
 	
     /**
      * the ConnectionNetworking instance to map incoming connections to
      */
     private final ConnectionNetworking connectionNetworking;
 
     /**
      * the ObjectIntputStream to use
      */
     private ObjectInputStream input;
 
     /**
      * ID of connecting player
      */
     private int foreignID;
     
     
     
     /**
      * {@inheritDoc}
      */
 	public void run()
 	{
 		
 		/* prepair outgoing stream */
 		ObjectOutputStream output = null;
 
 		/* prepair id to map sockets and streams to */
 		int peer = 0;
 		
 		Packet packet = null;
 		System.err.println("\033[1;33mStarting TCPReceiver\033[0m");
 		try
 		{
 			if (this.input == null)
 			{
 				output = new ObjectOutputStream(new BufferedOutputStream(this.connection.getOutputStream()));
 				output.flush();
 				System.err.println("\033[1;33mTCPReceiver: Getting new conection from outside\033[0m");
 				this.input = new ObjectInputStream(new BufferedInputStream(this.connection.getInputStream()));
 	
 				packet = (Packet)(this.input.readObject());
 	
 				/* Start sorting the packet */
 				if (packet.getMessage().getMessage() instanceof Handshake)
 				{
 				
 					Handshake message = (Handshake)packet.getMessage().getMessage();
 					System.err.println("\033[1;33mTCPReceiver: connecting player ID: " + message.getID() + "\033[0m");
 					if (message.getID() < 0)
 					{
 						peer = this.connectionNetworking.getHighestID() + 1;
 						output.writeObject(new HandshakeAnswer(peer, this.connectionNetworking.localID));
 						output.flush();
 						System.err.println("\033[1;33mTCPReceiver: Sent HandshakeAnswer\033[0m");
 						
 					} else
 						peer = message.getID();
 
 					this.foreignID = peer;
 					this.connectionNetworking.outputs.put(peer, output);
 					System.err.println("\033[1;33mTCPReceiver: Prepairing for FullUpdate\033[0m");
 					FullUpdate update = new FullUpdate();
 					Blackboard.broadcastMessage(update);
 					System.err.println("\033[1;33mTCPReceiver: FullUpdate object done ==> " + update + "\033[0m");
 					output.writeObject(update.getDistributable());
 					output.flush();
 					System.err.println("\033[1;33mTCPReceiver: Sent FullUpdate to client\033[0m");
 				
 				} else {
 					this.connection.close();
 					return;
 				}
 			}
 
 		} catch (Exception ioe)
 		{
 			return;
 		}
 		
 		
 		// Take ID and map the connection and peer in ConnectionNetworking
 		this.connectionNetworking.sockets.put(peer, this.connection);
 		this.connectionNetworking.inputs.put(peer, input);
 
 		synchronized (this)
 		{   this.notify();
 		}
 
		System.err.println("\033[1;33mTCPReceiver: Streams are:\nInput:  " + (this.connectionNetworking.inputs.get(peer) == null ? "\033[1;31mNull\033[1;33m" : "\033[1;32mOK\033[1;33m") + "\nOutput: " + (this.connectionNetworking.outputs.get(peer) == null ?  "\033[1;31mNull\033[1;33m" : "\033[1;32mOK\033[1;33m") + "\033[0m");
 		try 
 		{
 			while(true)
 			{
 				packet = (Packet)this.input.readObject();
 				if (packet.getMessage() instanceof Broadcast)
 				{
 					this.connectionNetworking.send(packet);
 					if (packet.getMessage().getMessage() instanceof BlackboardMessage)
 						this.gameNetworking.receive(packet);
 					else if (packet.getMessage().getMessage() instanceof ConnectionMessage)
 						System.err.println("\n\nGot a ConnectionMessage in a Broadcast while being connected, shouldn't happen\n");
 
 				} else if (packet.getMessage() instanceof Whisper)
 				{
 					Whisper message = (Whisper)packet.getMessage();
 					if (message.getReceiver() != this.connectionNetworking.localID)
 						this.connectionNetworking.send(packet);
 					else
 					{
 						if (packet.getMessage().getMessage() instanceof BlackboardMessage)
 							this.gameNetworking.receive(packet);
 						else if (packet.getMessage().getMessage() instanceof ConnectionMessage)
 							System.err.println("\n\nGot a ConnectionMessage in a Whisper while being connected, shouldn't happen\n");
 					}
 				}
 			}
 			
 		} catch (IOException ioe)
 		{
 			if (this.foreignID < this.connectionNetworking.localID)
 				this.connectionNetworking.reconnect(this.foreignID);
 		} catch (Exception err)
 		{
 			Blackboard.broadcastMessage(new PlayerDropped(Player.getInstance(this.foreignID)));
 			try
 			{
 				this.connection.close();
 			} catch (Exception ierr)
 			{
 				// Do nothing
 			}
 			return;
 		}
 	}
 }
