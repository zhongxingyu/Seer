 
 
 package bohnanza.standard.server;
 
 
 
 
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 

 import bohnanza.standard.core.Game;
 import bohnanza.standard.core.Player;
 public class Server implements Runnable {
 
 	// The server socket.
 	private static ServerSocket serverSocket = null;
 	// The client socket.
 	private static Socket clientSocket = null;
 
 	public Game game = new Game();
 
 	// This chat server can accept up to maxClientsCount clients' connections.
 	private static final int maxClientsCount = 10;
 	private static final ServerThread[] threads = new ServerThread[maxClientsCount];
 
 	public static void main(String args[]) {
 
 		// The default port number.
 		int portNumber = 2222;
 		if (args.length < 1) {
 			System.out.println("Usage: java Server <portNumber>\n"
 					+ "Now using port number=" + portNumber);
 		} else {
 			portNumber = Integer.valueOf(args[0]).intValue();
 		}
 
 		try {
 			serverSocket = new ServerSocket(portNumber);
 		} catch (IOException e) {
 			System.out.println(e);
 		}
 
 		Server server = new Server();
 		new Thread(server).start();
 
 		/*
 		 * Create a client socket for each connection and pass it to a new client
 		 * thread.
 		 */
 		while (true) {
 			try {
 				clientSocket = serverSocket.accept();
 				int i = 0;
 				for (i = 0; i < maxClientsCount; i++) {
 					if (threads[i] == null) {
 						(threads[i] = new ServerThread(clientSocket, server, i)).start();
 						break;
 					}
 				}
 				if (i == maxClientsCount) {
 					PrintStream os = new PrintStream(clientSocket.getOutputStream());
 					os.println("Server too busy. Try later.");
 					os.close();
 					clientSocket.close();
 				}
 			} catch (IOException e) {
 				System.out.println(e);
 			}
 		}
 	}
 
 	public void sendUpdate(int from){
		if(game.getCurrentState().getActions(game.getActivePlayer()).size() == 1) {
 			if(game.getCurrentState().getActions(game.getActivePlayer()).contains(Protocol.NEXTPLAYER)){
				
 			}
 		}
 		Protocol protocol = new Protocol(game);
 		String message;
 		if(game.getCurrentState() == null){
 			message = Protocol.waitingForPlayers();
 		} else {
 			message = protocol.toJSON();
 		}
 			
 		for (int i = 0; i < maxClientsCount; i++) {
 			if (threads[i] != null /*&& i != from*/) {
 				threads[i].sendMessage(message);
 			}
 		}
 	}
 
 	public void broadcast(int from, String message) {
 		for (int i = 0; i < maxClientsCount; i++) {
 			if (threads[i] != null /*&& i != from*/) {
 				threads[i].sendMessage(message);
 			}
 		}
 	}
 
 	public void run() {
 		/*
 		 * Keep on reading from the socket till we receive "Bye" from the
 		 * server. Once we received that then we want to break.
 		 */
 		String responseLine;
 		BufferedReader is = new BufferedReader(new InputStreamReader(System.in));
 		try {
 			while ((responseLine = is.readLine()) != null) {
 				System.out.println(responseLine);
 				if(responseLine.equals("start")){
 					game.start();
 					sendUpdate(0);
 				}
 				if (responseLine.indexOf("end") != -1)
 					break;
 			}
 			//closed = true;
 		} catch (IOException e) {
 			System.err.println("IOException:  " + e);
 		}
 	}
 
 	public void sendToPlayer(Player player, String message) {
 		for (int i = 0; i < maxClientsCount; i++) {
 			if (threads[i] != null && threads[i].getPlayer() == player) {
 				threads[i].sendMessage(message);
 				break;
 			}
 		}
 
 	}
 }
