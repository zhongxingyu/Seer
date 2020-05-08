 package consistent;
 
 import java.io.*;
 import java.net.*;
 import java.util.*;
 
 public class ChatServer {
 
 	public enum Status {
 		GOOD, BAD_ARGUMENTS;
 	}
 
 	private static final int DEFAULT_SERVER_SOCKET_TIMEOUT = 500;
 
 	private ServerSocket listenSocket;
 	private List<Connection> clients;
 
 	public ChatServer(int serverPort, int serverSocketTimeout) throws IOException {
 		listenSocket = new ServerSocket(serverPort);
 		listenSocket.setSoTimeout(serverSocketTimeout);
 
 		clients = new LinkedList<Connection>();
 	}
 
 	public boolean acceptNewConnection() throws IOException {
 		try {
 			Socket clientSocket = listenSocket.accept();
 			try {
 				Connection c = new Connection(clientSocket);
 				clients.add(c);
 				return true;
 			} catch (IOException e) {
 				clientSocket.close();
 			}
 		} catch (SocketTimeoutException e) { }
 		return false;
 	}
 
 	public void processMessages() {
 		for (Connection source : clients) {
 			if (source.hasMessage()) {
 				String message = source.readMessage();
				for (Connection dest : clients) {
					if (source != dest) {
						dest.writeMessage(message);
 					}
 				}
 			}
 		}
 	}
 
 	private void closeBadConnections() throws IOException {
 		ListIterator<Connection> clientIt = clients.listIterator();
 		while(clientIt.hasNext()) {
 			Connection client = clientIt.next();
 			if (client.error()) {
 				clientIt.remove();
 				client.close();
 			}
 		}
 	}
 	
 	private void close() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public static void main(String args[]) {
 		ChatServer server = null;
 		try {
 			int serverPort = Integer.parseInt(args[0]);
 			server = new ChatServer(serverPort, DEFAULT_SERVER_SOCKET_TIMEOUT);
 
 			while (true) {
 				server.acceptNewConnection();
 				server.processMessages();
 				server.closeBadConnections();
 			}
 		} catch (IndexOutOfBoundsException e) {
 			System.err.println("Syntax: java ChatServer <port> ");
 			System.exit(Status.BAD_ARGUMENTS.ordinal());
 		} catch (IOException e) {
 			System.err.println("Listen :" + e.getMessage());
 		} catch (NumberFormatException e) {
 			System.err.println("Bad Server Port!");
 		} finally {
 			if (server != null)
 				server.close();
 		}
 	}
 }
