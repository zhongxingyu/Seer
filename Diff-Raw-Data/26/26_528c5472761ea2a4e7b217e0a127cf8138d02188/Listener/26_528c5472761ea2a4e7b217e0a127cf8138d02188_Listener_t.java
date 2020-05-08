 /*
  * ConnectionListener.java
  * Version 1.0 (2013-05-29)
  */
 
 package battleships.server;
 
 import battleships.message.Message;
 import battleships.message.NameMessage;
 import battleships.network.ConnectionException;
 import battleships.network.Socket;
 
 /**
  * Listens for new connections.
  * 
  * @author Christopher Nilsson
  */
 public class Listener
 {	
 	/**
 	 * Raw listener.
 	 */
 	private java.net.ServerSocket listener;
 	
 	/**
 	 * Players that have not been named yet are stored here in the form of sockets.
 	 */
 	private java.util.ArrayList<Socket> sockets;
 	
 	/**
 	 * Highest ID reached.
 	 */
 	private int idCounter;
 	
 	/**
 	 * Initializes the object for listening.
 	 * 
 	 * @param port					Specific port to listen at.
 	 * @throws ConnectionException	Could not listen at the specified port.
 	 */
 	public void start(int port) throws ConnectionException
 	{
 		// Start listening
 		try
 		{
 		    listener = new java.net.ServerSocket(port);
 		    listener.setSoTimeout(100);
 		} 
 		catch(java.io.IOException e)
 		{
 		    throw new ConnectionException("Could not listen for connections on port: " + port);
 		}
 		
 		// Storing sockets that haven't been fully initialized yet
 		sockets = new java.util.ArrayList<Socket>();
 		
 		// No ID:s have been given away yet
 		idCounter = 0;
 	}	
 	
 	/**
 	 * Closes the listening socket.
 	 */
 	public void stop()
 	{
 		// Closing only if it isn't already closed
 		if(listener != null)
 		{
 			// Make sure to properly abort the listener
 			try
 			{
 				listener.close();
 			} 
 			catch (java.io.IOException e)
 			{
 				// Nothing to do here
 			}
 			
 			// Make room for new listeners in the future
 			listener = null;
 			sockets = null;
 		}
 	}
 	
 	/**
 	 * Retrieves a new Player instance if a successful connection could
 	 * be recognized. For a player to be detected, the corresponding client
 	 * must have sent its desired name to the server first.
 	 * 
 	 * @return	A new player.
 	 */
 	public Player listen()
 	{
 		// Find a new socket and store it if one could be found
 		addNewConnection();
 		
 		// Create players that have received names from the clients
 		return generatePlayer();
 	}
 	
 	/**
 	 * Scans for client sockets.
 	 */
 	private void addNewConnection()
 	{
 		// Add the newly found socket
 		try
 		{
 		    java.net.Socket raw = listener.accept();
 		    Socket connection = new Socket();
 		    connection.connect(raw);
 		    sockets.add(connection);
		    System.out.println("New socket found! Waiting for name...");
 		}
 		
 		// No socket could be found at this time
 		catch (Exception e)
 		{
 			// Do nothing
 		}
 	}
 	
 	/**
 	 * Looks for messages related to giving players a name.
 	 * Once one has been found, a new player is created.
 	 * 
 	 * @return	A new player.
 	 */
 	private Player generatePlayer()
 	{
 		// Try to retrieve names from all connected clients
 		for(int i = 0; i < sockets.size(); ++i)
 		{
 			// Remove invalid sockets
 			Socket socket = sockets.get(i);
 			if(!socket.isConnected())
 			{
 				sockets.remove(i--);
 			}
 			
 			// Retrieve the message
 			else
 			{
 				// Message object
 				Message message = socket.read();
 				
 				// Check if it is a message with a name
 				if(message != null && message.getType() != null && message.getType().equals("NameMessage")) 
 				{
					System.out.println("Name received!");
					
 					// Downcast
 					NameMessage castedMessage = (NameMessage) message;
 					
 					// Remove the socket from the listener
 					sockets.remove(i);
 					
 					// Setup a player
 					String name = castedMessage.getName();
 					return new Player(socket, ++idCounter, name);						
 				}
 			}
 		}
 		
 		// No player could be generated at this time
 		return null;
 	}
 }
