 package aidancbrady.server;
 
 import java.net.BindException;
 import java.net.ServerSocket;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.swing.JOptionPane;
 
 public class ServerCore
 {
 	private static ServerCore instance;
 	
 	public int port = -1;
 	
 	public final long TIMEOUT = 300000;
 	
 	public boolean serverRunning = false;
 	
 	public ServerSocket serverSocket;
 	
 	public int usersConnected = 0;
 	
 	public String discussion;
 	
 	public Map<Integer, ServerConnection> connections = new HashMap<Integer, ServerConnection>();
 	public Map<String, User> cachedUsers = new HashMap<String, User>();
 	
 	public GuiServer theGui;
 	
 	public static void main(String[] args)
 	{
 		(instance = new ServerCore()).init();
 	}
 	
 	/**
 	 * Initiates the server.
 	 */
 	public void init()
 	{
 		Runtime.getRuntime().addShutdownHook(new ShutdownHook());
 		
 		try {
 			System.setProperty("com.apple.mrj.application.apple.menu.about.name", "ServerCore");
 		} catch(Exception e) {}
 		
 		try {
 			FileHandler.read();
 			
 			System.out.println("Initializing...");
 			
 			theGui = new GuiServer();
 			
 			synchronized(this)
 			{
 				wait();
 			}
 			
 			System.out.println("Shutting down...");
 			
 			for(ServerConnection connection : connections.values())
 			{
 				connection.socketConnection.socket.close();
 			}
 			
 			if(serverSocket != null)
 			{
 				serverSocket.close();
 			}
 			
 			connections.clear();
 			
 			FileHandler.write();
 			System.out.println("Goodbye!");
 			System.exit(0);
 		} catch(Exception e) {
 			System.err.println("Error: " + e.getMessage());
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Gets the primary ServerCore instance.
 	 * @return ServerCore instance
 	 */
 	public static ServerCore instance()
 	{
 		return instance;
 	}
 	
 	public void updateDiscussion(String name)
 	{
 		if(name == null || name == "")
 		{
 			return;
 		}
 		
 		discussion = name;
 		theGui.discussionLabel.setText("Discussion: " + name);
 		syncDiscussionName(name);
 	}
 	
 	public void syncChat()
 	{
 		for(ServerConnection connection : connections.values())
 		{
 			if(connection.isAuthenticated())
 			{
				connection.socketConnection.printWriter.println("/chatlog:" + Util.convertForSync(theGui.chatBox.getText()));
 			}
 		}
 	}
 	
 	public void syncDiscussionName(String name)
 	{
 		for(ServerConnection connection : connections.values())
 		{
 			if(connection.isAuthenticated())
 			{
 				connection.socketConnection.printWriter.println("/discname:" + name);
 			}
 		}
 	}
 	
 	/**
 	 * Increments the usersConnected and returns a new ID for the newly-connected user.
 	 * @return new user ID
 	 */
 	public int newConnection()
 	{
 		usersConnected++;
 		
 		int idToUse = 0;
 		
 		while(true)
 		{
 			boolean hasThisLoop = false;
 			idToUse++;
 			
 			for(ServerConnection connection : connections.values())
 			{
 				if(connection.getUserID() == idToUse)
 				{
 					hasThisLoop = true;
 					break;
 				}
 			}
 			
 			if(hasThisLoop)
 			{
 				continue;
 			}
 			
 			return idToUse;
 		}
 	}
 	
 	/**
 	 * Decrements the usersConnected and removes the connection associated with the defined user ID from the server.
 	 * @param userId
 	 */
 	public void removeConnection(int userId)
 	{
 		usersConnected--;
 		connections.remove(userId);
 	}
 	
 	/**
 	 * Distributes a message to all connected users other than the one with the specified ID.
 	 * @param id - user ID to ignore
 	 * @param message - message to send
 	 */
 	public void distributeMessageIgnore(int id, String message)
 	{
 		for(ServerConnection connection : connections.values())
 		{
 			if(connection.getUserID() != id)
 			{
 				try {
 					connection.socketConnection.printWriter.println(message);
 				} catch(Exception e) {
 					connection.socketConnection.kick();
 					theGui.appendChat("An error occured while notifying other users.");
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Distributes a message to all connected users.
 	 * @param message - message to send
 	 */
 	public void distributeMessage(String message)
 	{
 		for(ServerConnection connection : connections.values())
 		{
 			try {
 				connection.socketConnection.printWriter.println(message);
 			} catch(Exception e) {
 				connection.socketConnection.kick();
 				theGui.appendChat("An error occured while notifying other users.");
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public void start()
 	{
 		if(port == -1)
 		{
 			return;
 		}
 		
 		if(discussion == null || discussion.equals(""))
 		{
 			JOptionPane.showMessageDialog(theGui, "Please define a discussion name before starting.", "Warning", JOptionPane.WARNING_MESSAGE);
 			return;
 		}
 		
 		try {
 			serverSocket = new ServerSocket(port);
 			
 			serverRunning = true;
 			
 			new ScheduledTimer().start();
 			new SocketListener().start();
 			
 			System.out.println("Server initialized on port " + port);
 			
 			theGui.activeLabel.setText("Active -");
 			
 			theGui.setPortButton.setEnabled(false);
 			theGui.portEntry.setEnabled(false);
 			theGui.startServerButton.setEnabled(false);
 			theGui.stopServerButton.setEnabled(true);
 		} catch(BindException e) {
 			JOptionPane.showMessageDialog(theGui, "Permission denied.", "Warning", JOptionPane.WARNING_MESSAGE);
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void stop()
 	{
 		try {
 			for(ServerConnection connection : connections.values())
 			{
 				connection.socketConnection.socket.close();
 			}
 			
 			if(serverSocket != null)
 			{
 				serverSocket.close();
 			}
 			
 			serverRunning = false;
 			
 			System.out.println("Server deinitialized");
 			
 			theGui.activeLabel.setText("Inactive -");
 			
 			theGui.setPortButton.setEnabled(true);
 			theGui.portEntry.setEnabled(true);
 			theGui.startServerButton.setEnabled(true);
 			theGui.stopServerButton.setEnabled(false);
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 		
 		connections.clear();
 	}
 }
 
 class ShutdownHook extends Thread
 {
 	@Override
 	public void run()
 	{
 		try {
 			if(ServerCore.instance().serverSocket != null)
 			{
 				ServerCore.instance().serverSocket.close();
 			}
 			
 			finalize();
 		} catch (Throwable t) {
 			System.err.println("Error: " + t.getMessage());
 			t.printStackTrace();
 		}
 	}
 }
