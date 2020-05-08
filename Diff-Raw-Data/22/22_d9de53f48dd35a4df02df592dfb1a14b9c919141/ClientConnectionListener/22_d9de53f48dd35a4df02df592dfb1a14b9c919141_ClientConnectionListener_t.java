 package server;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import server.model.ServerActiveUserModel;
 import server.model.ServerUserModel;
 import client.model.TransferableModel;
 
 /**
  * ClientConnectionListener
  * 
  * The client connection listener is responsable for opening up a tcp port
  * on the server side and listen for incoming connects. Upon recieving a
  * connection it will attempt to log the incoming connection in. If the login
  * fails the client is dropped. Upon successfull login a {@see ClientConnection}
  * Thread is spawned to take care of request for that connection and this class
  * returns to listening mode.
  * 
  * @author Runar B. Olsen <runar.b.olsen@gmail.com>
  */
 public class ClientConnectionListener {
 
 	private static Logger LOGGER = Logger.getLogger("ClientConnectionListener");
 	
 	/**
 	 * Maps username and ClientConnection objects
 	 */
 	private Map<String, ClientConnection> clients;
 	
 	/**
 	 * Listening port
 	 */
 	private final int port;
 	
 	/**
 	 * Create a new ClientConnectionListener on the given port number
 	 * 
 	 * @param port
 	 */
 	public ClientConnectionListener(int port) {
 		this.port = port;
 		clients = Collections.synchronizedMap(new HashMap<String, ClientConnection>());
 	}
 
 	/**
 	 * Broadcast a message to all connected clients
 	 * 
 	 * @param line
 	 */
 	/*public void broadcastLine(String line) {
 		for(ClientConnection c : clients.values()) {
 			c.writeLine(line);
 		}
 	}*/
 	
 	/**
 	 * Removes a client, called by the client it self after a logout or if the
 	 * connection dropped.
 	 * 
 	 * @param user
 	 */
 	public void removeClient(ServerActiveUserModel user) {
 		if(clients.containsKey(user.getUsername())) {
 			clients.remove(user.getUsername());
 			LOGGER.info(String.format("%s is gone", user));
 			LOGGER.info(String.format("%d client(s) in total", clients.size()));
 		}
 	}
 	
 	/**
 	 * Listen for incoming client connections
 	 * 
 	 * Attempts to open up a port on the portnumber given in the constructor,
 	 * there after it enters a infinite loop accepting connections.
 	 * 
 	 * @throws IOException if port number is taken
 	 */
 	public void listen() throws IOException {
 		
 		ServerSocket ss = new ServerSocket(this.port);		
 		LOGGER.info("Server open for requests on "+this.port);
 		
 		while(true) {
 			Socket s = null;
 			try {
 				// Wait for new clients
 				s = ss.accept();
 				
 				LOGGER.info("Accepted client from "+s.getInetAddress().toString());
 				
 				// login sequence				
 				BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
 				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
 				
 				writer.write("Welcome to "+ServerMain.VERSION+"\r\n");
 				writer.flush();
 				String login = reader.readLine();
 				String[] parts = login.split("\\s+");
 				
 				if(!login.startsWith("LOGIN ") || parts.length != 3) {
 					LOGGER.info("Invalid login packet, dropping client");
 					writer.write("Invalid login packet, disconnection");
 					writer.flush();
 					s.close();
 					continue;
 				}
 				
 				// Authenicate user in database
 				String username = parts[1], password = parts[2];					
 				ServerUserModel user = ServerUserModel.findByUsernameAndPassword(
 						username, password, ServerMain.dbConnection);
 				
 				if(user != null) {
 					writer.write("OK Welcome "+user.getFullName()+"\r\n");
 					writer.flush();
 					
 					// Store and start separate handler thread
 					ClientConnection cc = new ClientConnection(
 							s, reader, writer, 
 							new ServerActiveUserModel(user), this
 					);
 					
 					clients.put(user.getUsername(), cc);
 					LOGGER.info(String.format("Client from %s authenticated as %s", s.getInetAddress().toString(), username));
 					LOGGER.info(String.format("%d client(s) in total", clients.size()));
 					new Thread(cc).start();
 				} else {
 					writer.write("ERROR Bad login\r\n");
 					writer.flush();
 					s.close();
 				}
 			} catch(IOException e) {
 				LOGGER.severe("Client dropped due to IOException during authentication phase");
 				LOGGER.severe(e.toString());
 				
 				// Make sure the socket has been closed
 				try {
 					if(s != null)
 						s.close();
 				} catch(IOException ioe) {}
 			}
 		}
 		
 	}
 	
 	/**
 	 * Broadcast a model to all connected client
 	 */
 	public void broadcastModel(TransferableModel model) throws IOException {
 		for(ClientConnection c : clients.values()) {
 			c.broadcastModel(model);
 		}
 	}
 
 	/**
 	 * Broadcast a model that has either been created or updated to the 
 	 * specified user
 	 * 
 	 * @param model
 	 * @param username
 	 */
 	public void broadcastModel(TransferableModel model,	
 			String username) throws IOException {
		LOGGER.info("Broadcasting model "+model.getUMID()+" to "+username);
 		if(clients.containsKey(username)) {
 			clients.get(username).broadcastModel(model);
 		}
 	}
 	
 }
