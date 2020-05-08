 /*
  * CS352 Example Chat Client
  * Copyright (C) 2011 Rutgers University and Robert Moore
  * 
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *  
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *  
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc.,
  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  */
 
 package edu.rutgers.cs.chat;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketTimeoutException;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import edu.rutgers.cs.chat.messaging.ChatMessage;
 import edu.rutgers.cs.chat.messaging.ClientExchangeMessage;
 import edu.rutgers.cs.chat.messaging.MessageListener;
 import edu.rutgers.cs.chat.ui.ConsoleUI;
import edu.rutgers.cs.chat.ui.UIAdapter;
 import edu.rutgers.cs.chat.ui.UserInputListener;
 
 
 /**
  * Main class of the chat client. It handles incoming connections and exchanged
  * client information.
  * 
  * @author Robert Moore
  * 
  */
 public class ChatClient extends Thread implements MessageListener,
 		UserInputListener {
 
 	/**
 	 * Copyright license notification message printed at startup.
 	 */
 	public static final String LICENSE_NOTIFICATION = 
 		"CS352 Example Chat Client version 1.0\n"+
 		"Copyright (C) 2011 Robert Moore and Rutgers University\n" +
 		"CS352 Example Chat Client comes with ABSOLUTELY NO WARRANTY.\n" +
 		"This is free software, and you are welcome to redistribute it\n" + 
 		"under certain conditions; see the included file LICENSE for details.";
 	
 	/**
 	 * Port number for incoming connections.
 	 */
 	protected final int listenPort;
 
 	/**
 	 * Local username. This is sent along with chat messages.
 	 */
 	protected final String username;
 
 	/**
 	 * Socket for accepting incoming connections.
 	 */
 	protected ServerSocket listenSocket;
 
 	/**
 	 * Flag to shutdown the client.
 	 */
 	protected boolean keepRunning = true;
 
 	/**
 	 * List of currently-connected clients.
 	 */
 	protected final Collection<Client> clients = new ConcurrentLinkedQueue<Client>();
 
 	/**
 	 * Thread pool for handling incoming connections and new client information.
 	 */
 	protected final ExecutorService workers = Executors.newCachedThreadPool();
 
 	/**
 	 * Basic console-based user interface.
 	 */
 	protected ConsoleUI userInterface = new ConsoleUI();
 
 	/**
 	 * Parse command-line arguments and start a single instance of the
 	 * ChatClient class.
 	 * 
 	 * @param args
 	 *            local port, username, remote host (optional), remote port
 	 *            (optional)
 	 */
 	public static void main(String[] args) {
 
 		// Empty line for formatting
 		System.out.println();
 		// Print license info
 		System.out.println(ChatClient.LICENSE_NOTIFICATION);
 		// Empty line for formatting
 		System.out.println();
 		
 		
 		// Check to make sure we have at least 2 arguments (local port,
 		// username)
 		if (args == null || args.length < 2) {
 			printUsage();
 			return;
 		}
 
 		// Set the listen port for incoming client connections
 		int listenPort = Integer.MIN_VALUE;
 		try {
 			listenPort = Integer.parseInt(args[0]);
 		} catch (NumberFormatException nfe) {
 			System.err
 					.println("Invalid or missing port number.  Client cannot start.");
 			return;
 		}
 
 		// Don't even mess with ports outside userspace.
 		if (listenPort <= 1024 || listenPort > 65535) {
 			System.err
 					.println("Port number is outside the valid range: 1024 < port < 65535");
 			return;
 		}
 
 		// Any string is fine, will be encoded in UTF-16 to clients
 		String username = args[1];
 
 		// Create the application with the listen port and username.
 		ChatClient ourClient = new ChatClient(listenPort, username);
 		// Start the client, ensure that incoming connections will be handled if
 		// we
 		// add a bootstrap peer.
 		ourClient.start();
 
 		/*
 		 * If the remote host and port were provided, try to connect. Shouldn't
 		 * be fatal if it fails.
 		 */
 		if (args.length == 4) {
 
 			// Parse the port, pass args[2] in as the remote hostname.
 			int remotePort = Integer.MIN_VALUE;
 			try {
 				remotePort = Integer.parseInt(args[3]);
 
 				// Userspace ports onlys
 				if (remotePort <= 1024 || remotePort > 65535) {
 					System.err
 							.println("Remote port number is out of valid range, won't connect.");
 				} else {
 					// Don't know the remote username, so pass null
 					ourClient.addClient(args[2], remotePort, null);
 				}
 			} catch (NumberFormatException nfe) {
 				System.err
 						.println("Invalid port number specified for bootstrap client, won't connect.");
 			}
 		}
 
 	}
 
 	/**
 	 * Creates a new chat client listening on the specified port and with the
 	 * provided username.
 	 * 
 	 * @param listenPort
 	 *            the port number for incoming client connections.
 	 * @param username
 	 *            the username to send to other clients.
 	 */
 	public ChatClient(final int listenPort, final String username) {
 		this.listenPort = listenPort;
 		this.username = username;
 
 		this.userInterface.addUserInputListener(this);
 		this.userInterface.start();
 	}
 
 	/**
 	 * Called when remote clients exchange information about other clients with
 	 * this client. Will check for a duplicate connection first, test it with a
 	 * keep-alive, and ignore this client if the connection is viable. If the
 	 * client is not matched or the old connection is closed, then it will
 	 * connect to the client.
 	 * 
 	 * @param remoteHost
 	 *            the hostname/IP address of the new client
 	 * @param port
 	 *            the listen port number for the new client.
 	 * @param username
 	 *            the username expected from the remote client.
 	 */
 	protected synchronized void addClient(final String remoteHost,
			final int port, final String username) {
 		// Build a new client object
 		Client newClient = this.makeClient(remoteHost, port, username);
 
 		// If null, then an exception was thrown, probably couldn't resolve the
 		// hostname.
 		if (newClient == null) {
 			return;
 		}
 
 		// Check to see if this client is already known
 		Client oldClient = this.findDuplicate(newClient);
 
 		// If we already have this client in our list, then check to make sure
 		// it's still live
 		if (oldClient != null) {
 			if (this.testClient(oldClient)) {
 				// Old client is fine, so discard the new one
 				return;
 			}
 		}
 		// Connect the socket to the remote client, discard the client on errors
 		try {
 			newClient.connect();
 		} catch (IOException ioe) {
 			System.err.println("Unable to connect to " + newClient + ": " + ioe.getMessage());
 			return;
 		}
 		// Try to handshake, if it succeeds then notify the UI
 		if (newClient.performHandshake()) {
 			this.registerClient(newClient);
 			this.clients.add(newClient);
 		}
 
 	}
 
 	/**
 	 * Registers a new client with the local client. Notifies the UI.
 	 * 
 	 * @param client
 	 *            the client to register.
 	 */
 	protected void registerClient(Client client) {
 		client.addMessageListener(this);
 		client.start();
 		this.userInterface.clientConnected(client);
 	}
 
 	/**
 	 * Tests a client for liveness by sending a Keep-Alive message. If the send
 	 * fails, then the client is disconnected, removed from the list of clients,
 	 * and the UI is notified.
 	 * 
 	 * @param client
 	 *            the client to test
 	 * @return true if the test succeeds, else false
 	 */
 	protected boolean testClient(Client client) {
 		try {
 			client.sendKeepAliveMessage();
 			return true;
 		} catch (IOException ioe) {
 			this.clients.remove(client);
 			client.removeMessageListener(this);
 			client.disconnect();
 			this.userInterface.clientDisconnected(client, ioe.getMessage());
 		}
 		return false;
 	}
 
 	/**
 	 * Creates a new client from the supplied arguments. If an IOException is
 	 * thrown, then null is returned.
 	 * 
 	 * @param remoteHost
 	 *            the hostname/IP address of the client.
 	 * @param port
 	 *            the listen port of the client.
 	 * @param username
 	 *            the username expected from the client
 	 * @return the newly-created Client, or null if an exception was thrown.
 	 */
 	protected Client makeClient(String remoteHost, int port,
			String username) {
 		Client newClient = new Client(remoteHost, port, username,
 				this.username, this.listenPort);
 		return newClient;
 	}
 
 	/**
 	 * Called when a remote client connects to the local client on the listen
 	 * socket. Will check for duplicates (just in case the remote peer made a
 	 * mistake), test it with a keep-alive, and close the new connection if the
 	 * old one is still viable. If the new client is not matched or the old
 	 * connection is closed, then it will register the new client.
 	 * 
 	 * @param socket
 	 *            the socket of the newly-connected client
 	 */
 	protected synchronized void addClient(final Socket socket) {
 		Client newClient = new Client(socket, this.username, this.listenPort);
 
 		// Need to handshake first since we need to get the remote port info
 		// before checking for duplicates
 		if (!newClient.performHandshake()) {
 			newClient.disconnect();
 			return;
 		}
 
 		// Try to find an old version of this client (same IP/port)
 		Client oldClient = findDuplicate(newClient);
 
 		// If we already have this client in our list, then check to make sure
 		// it's still live
 		if (oldClient != null) {
 			if (this.testClient(oldClient)) {
 				// Old client is fine, so discard the new one
 				return;
 			}
 		}
 
 		// Register the new client
 		this.registerClient(newClient);
 
 		// Notify other clients of the new client
 		this.notifyClients(newClient);
 
 		// Add the client to the list of known clients
 		this.clients.add(newClient);
 	}
 
 	/**
 	 * Sends client exchange messages to currently-connected clients.
 	 * 
 	 * @param newClient
 	 *            the newly-added client.
 	 */
 	protected void notifyClients(Client newClient) {
 		// Go through each remote client and send a ClientMessage.
 		for (Iterator<Client> clientIter = this.clients.iterator(); clientIter
 				.hasNext();) {
 			Client client = clientIter.next();
 			try {
 				// Exchange the client information
 				client.sendClient(newClient);
 			} catch (IOException e) {
 				// Remove the client from the list of clients
 				clientIter.remove();
 				// Stop listening to messages from the client
 				client.removeMessageListener(this);
 				// Disconnect the client
 				client.disconnect();
 				// Notify the UI of the disconnect
 				this.userInterface.clientDisconnected(client, e.getMessage());
 			}
 		}
 	}
 
 	/**
 	 * Checks to see if client is a duplicate of an already-connected client. If
 	 * it is, returns the client that matches, else returns null.
 	 * 
 	 * @param client
 	 *            the client to search for.
 	 * @return the currently-connected duplicate client, or null if no match is
 	 *         found.
 	 */
 	protected Client findDuplicate(Client client) {
 		for (Client otherClient : this.clients) {
 			if (client.equals(otherClient)) {
 				return otherClient;
 			}
 
 		}
 		return null;
 	}
 
 	/**
 	 * Prints out the basic usage string to System error.
 	 */
 	protected static final void printUsage() {
 		StringBuffer usageString = new StringBuffer();
 		usageString
 				.append("Usage: <Listen Port> <Username> [<Remote IP> <Remote Port>]");
 		System.err.println(usageString.toString());
 	}
 
 	/**
 	 * Passes the received chat message to the user interface.
 	 */
 	@Override
 	public void chatMessageArrived(final Client client,
 			final ChatMessage message) {
 		this.workers.execute(new Runnable() {
			@Override
 			public void run() {
 
 				ChatClient.this.userInterface.chatMessageReceived(client,
 						message.getTimestamp(), message.getMessage());
 			}
 		});
 
 	}
 
 	/**
 	 * Adds the exchange chat client if it is not already connected to the local
 	 * client.
 	 */
 	@Override
 	public void clientMessageArrived(final Client client,
 			final ClientExchangeMessage message) {
 		this.workers.execute(new Runnable() {
			@Override
 			public void run() {
 				ChatClient.this.addClient(message.getIpAddress(), message
 						.getPort(), message.getUsername());
 			}
 		});
 
 	}
 
 	/**
 	 * Deregisters the client from the local client, disconnects it, and
 	 * notifies the user interface.
 	 */
 	@Override
 	public void disconnectMessageArrived(final Client client) {
 		this.workers.execute(new Runnable() {
			@Override
 			public void run() {
 				client.removeMessageListener(ChatClient.this);
 				client.disconnect();
 				ChatClient.this.clients.remove(client);
 				ChatClient.this.userInterface.clientDisconnected(client,
 						"User quit.");
 			}
 		});
 
 	}
 
 	/**
 	 * Listens for incoming connections, checking every 250ms for a user request
 	 * to exit the chat client. Incoming connections are handled by worker
 	 * threads.
 	 */
 	@Override
 	public void run() {
 		try {
 			// Bind to the local listen port
 			this.listenSocket = new ServerSocket(this.listenPort);
 			// Wait for 250ms at a time
 			this.listenSocket.setSoTimeout(250);
 		} catch (IOException e) {
 			// An exception here is likely to be a bind failure.
 			System.err.println(e.getMessage());
 			e.printStackTrace();
 			System.exit(1);
 		}
 
 		System.out.println("Listening on port " + this.listenPort);
 
 		while (this.keepRunning) {
 			try {
 				// This will block for 250ms to allow checking for user exit
 				// conditions.
 				final Socket clientSocket = this.listenSocket.accept();
 				/*
 				 * Pass the actual work of adding the client to another thread,
 				 * freeing this thread to accept new clients.
 				 */
 				this.workers.execute(new Runnable() {
 
 					@Override
 					public void run() {
 						addClient(clientSocket);
 					}
 				});
 			} catch (SocketTimeoutException ste) {
 				// Ignored
 			} catch (IOException e) {
 				// Left in for debugging
 				e.printStackTrace();
 			}
 		}
 		this.doShutdown();
 	}
 
 	/**
 	 * Disconnects all currently-connected clients, shuts down thread pools, and
 	 * exits the application.
 	 */
 	protected void doShutdown() {
 		// Close down connections to all clients.
 		for (Client client : this.clients) {
 			try {
 				client.sendDisconnectMessage();
 			} catch (IOException ioe) {
 				// Ignored for now, who cares if we're shutting down. :)
 			}
 			client.disconnect();
 		}
 		// Shut down the threadpool
 		this.workers.shutdown();
 	}
 
 	/**
 	 * Sends the specified message to all currently-connected clients. If any
 	 * exception is thrown while sending the message, then that client is
 	 * disconnected. Actual work is handled by a worker thread. Will notify the
 	 * user interface after all clients have been sent the message (or failed to
 	 * send).
 	 * 
 	 * @see UIAdapter#chatMessageSent(long, String)
 	 */
 	@Override
 	public void broadcastChatMessage(final String input) {
 		this.workers.execute(new Runnable() {
			@Override
 			public void run() {
 				for (Iterator<Client> clientIter = ChatClient.this.clients
 						.iterator(); clientIter.hasNext();) {
 					Client client = clientIter.next();
 					try {
 						client.sendMessage(input);
 					} catch (IOException e) {
 						// Remove the client from the list of clients
 						clientIter.remove();
 						// Disconnect the client
 						client.disconnect();
 						// Notify the UI of the disconnect
 						ChatClient.this.userInterface.clientDisconnected(
 								client,
 								"Failed to send broadcast chat message/"
 										+ e.getMessage());
 					}
 				}
 				ChatClient.this.userInterface.chatMessageSent(System
 						.currentTimeMillis(), input);
 
 			}
 		});
 	}
 
 	/**
 	 * Sends the message to the specified client. If the message cannot be sent
 	 * due to an exception, then the client is disconnected.
 	 */
 	@Override
 	public void privateChatMessage(final Client client, final String message) {
 		this.workers.execute(new Runnable() {
			@Override
 			public void run() {
 				try {
 					client.sendMessage(message);
 				} catch (IOException e) {
 					ChatClient.this.clients.remove(client);
 					client.removeMessageListener(ChatClient.this);
 					client.disconnect();
 					ChatClient.this.userInterface.clientDisconnected(client,
 							"Failed to send private chat message/"
 									+ e.getMessage());
 				}
 			}
 
 		});
 	}
 
 	/**
 	 * Sets the run flag to false, which should occur within 250ms.
 	 * 
 	 * @see ChatClient#run()
 	 */
 	@Override
 	public void userRequestedShutdown() {
 		this.keepRunning = false;
 	}
 }
