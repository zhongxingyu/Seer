 package server;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 
 /**
  * Runs a new thread that detects any new clients and creates a Connector for
  * them. Also handles all input messages, and responds to them. Stores the
  * entire game in a World. Essentially is the master class that runs all
  * server-client communication.
  * 
  * @author Dylan
  * 
  */
 public class ClientManager extends Thread {
 	ServerSocket server;
 
 	ArrayList<Connector> connections;
 
 	public ClientManager(ServerSocket server) {
 		this.server = server;
 
 		connections = new ArrayList<Connector>();
 	}
 
 	public void run() {
 		while (true) {
 			try {
 				Socket newClient = server.accept();
 				Connector newConnector = new Connector(newClient, this);
 				newConnector.start();
 				connections.add(newConnector);
 				
 				newConnector.sendMessage(MessageType.SERVER_MESSAGE, "Welcome to the server!");
 			} catch (IOException e) {
 			}
 		}
 	}
 
 	public void cleanConnections() {
 		for (Connector client : connections) {
 			if (client.reciever.isClosed()) {
 				connections.remove(client);
 				System.out.println("Closed connection");
 			}
 		}
 	}
 
 	public void sendMessage(MessageType type, String message) {
 		for (Connector client : connections) {
 			client.sendMessage(type, message);
 		}
 		cleanConnections();
 	}
 
 	public void input(MessageType type, String message, Connector client) {
 		switch (type) {
 		case SERVER_MESSAGE:
 			Init.sendServerMessage("[USER " + client.id + "]: " + message);
 			System.out.println("[USER " + client.id + "]: " + message);
 			break;
 		case CONNECT:
 			Init.sendServerMessage("New client connected.");
 			client.sendMessage(MessageType.CONNECT, Integer.toString(client.id));
 			break;
 		case ADD_TOWER:
 			int x = message.charAt(0);
 			int y = message.charAt(1);
 			Init.sendServerMessage("[USER " + client.id + "] adding tower at " + x + ", " + y);
			if (Init.map.addTower(x, y)){
				sendMessage(MessageType.ADD_TOWER, "" + (char) x + (char) y);
			} else {
				Init.sendServerMessage("[USER " + client.id + "] Oops, there's already a tower there!");
			}
			
 		case REQUEST_ID:
 			int id = Init.currentId;
 			Init.currentId++;
 			sendMessage(MessageType.PROVIDE_ID, "" + id);
 
			
 
 		}
 	}
 }
