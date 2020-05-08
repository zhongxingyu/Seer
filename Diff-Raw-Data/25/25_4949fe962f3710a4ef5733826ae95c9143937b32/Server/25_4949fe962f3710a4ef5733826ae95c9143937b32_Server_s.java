 package pps.et.server;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 
 import pps.et.logic.ConnectionInterface;
 import pps.et.logic.GameHandler;
 import pps.et.logic.Player;
 
 public class Server implements ConnectionInterface {
 	static int port = 4711;
 	ServerSocket server;
 	ArrayList<ServerConnectionHandler> connections;
 	GameHandler game;
 	int connectionCount = 0;
 	TaskHandler taskHandler;
 	
 	public static void main(String[] args) throws IOException {
 		Server s = new Server();
 		
 	}
 	
 	public Server() throws IOException {
 		System.out.println("Starting server");
 		
 		connections = new ArrayList<ServerConnectionHandler>();
 		game = new GameHandler(this);
 		
 		try {
 			server = new ServerSocket(port);
 			System.out.println("Connected to port: " + port);
 			
 			startTaskHandler();
 			listen();
 			
 		} catch (IOException e) {
 			System.out.println("Could not connect on port");
 		} finally {
 			server.close();
 		}
 	}
 	
 	/** starts a thread to consume the bag of tasks */
 	private void startTaskHandler() {
 		taskHandler = new TaskHandler(game, this);
 		
 		Thread t = new Thread(taskHandler);
 		
 		t.start();
 		
 	}
 
 	// Listens to connections and add it to the arraylist
 	public void listen(){
 		System.out.println("Waiting for connections..");
 		
 		Socket tempSocket = null;
 		boolean RUNNING = true;
 		
 		try {
 			while (RUNNING) {
 				if((tempSocket = server.accept()) != null){
 					System.out.println("Accepted: " + tempSocket.getInetAddress());
 					Player p = new Player(connectionCount, "unknown", 0, 0);
 					
 					ServerConnectionHandler c = new ServerConnectionHandler(tempSocket, taskHandler, p);
 					connections.add(c);
 					connectionCount++;
 					
 					Thread t = new Thread(c);
 					
 					t.start();
 				}
 			}
 			
 			System.out.println("Shutdown server..");
 		} catch (Exception e) {
 			System.err.println("Error while listening for new connections.");
 		}
 	}
 	
 	public ArrayList<ServerConnectionHandler> getConnections() {
 		return connections;
 	}
 
 	@Deprecated
 	public void disconnectedUser(Player p) {
 		//connections.remove(p.getConnection());
 	}
 
 	public void sendToAll(String text) {
 		System.out.println("ALL: " + text);
 		for (ServerConnectionHandler h : connections) {
 			h.send(text);
 		}
 	}
 	
 	public void sendToAllBut(Player player, String text) {
 		System.out.println("ALL -" + player.getID() + ": " + text);
 		for (ServerConnectionHandler h : connections) {
 			if (player == null)
 				System.out.println("null");
 			if (h.player.getID() != player.getID()) {
 				h.send(text);
 			}
 		}
 	}
 
 	public void sendToPlayer(Player player, String string) {
		System.out.println("TO: " + player.getID() + " " + string);
 		for (ServerConnectionHandler h : connections) {
 			if (h.player.getID() == player.getID()) {
 				h.send(string);
 				break;
 			}				
 		}
 	}
 
 	@Override
 	public void send(String text) {
 		for (ServerConnectionHandler h : connections) {
 			h.send(text);
 		}
 	}
 }
