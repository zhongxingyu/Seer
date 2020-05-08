 package server.networking;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.Collections;
import java.util.HashMap;
 import java.util.List;
import java.util.Map;
 
 import chronos.Singleton;
 
 import server.ServerController;
 
 /**
  * Handles all incoming client connections and delegates them on a new thread
  */
 public class Server implements Runnable {
 	private ServerController sController;
 	private int port;
 	private boolean listening;
 	private List<ClientConnection> clientConnections;
 
 	public Server(int port, ServerController sController) {
 		this.port = port;
 		this.sController = sController;
 		listening = true;
 
 		clientConnections = Collections.synchronizedList(new ArrayList<ClientConnection>());
 	}
 
 	private ServerSocket setUpServer(int port) {
 		Singleton.log("Setting up server on port: " + port);
 		ServerSocket serverSocket = null;
 		try {
 			serverSocket = new ServerSocket(port);
 		} catch (IOException e) {
 			Singleton.log("Server already running! Skipping...");
 		}
 		return serverSocket;
 	}
 
 	private Socket listenForIncomfingConnections(ServerSocket serverSocket) {
 		Singleton.log("Listening for connections on port: " + port);
 		Socket socket = null;
 		try {
 			socket = serverSocket.accept();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return socket;
 	}
 
 	@Override
 	public void run() {
 
 		ServerSocket serverSocket;
 		Socket clientSocket;
 
 		if ((serverSocket = setUpServer(port)) == null)
 			return;
 
 		while (listening) {
 			clientSocket = listenForIncomfingConnections(serverSocket);
 			startNetClientThread(clientSocket);
 			String clientIp = clientSocket.getRemoteSocketAddress().toString().split("[/:]")[1];
 			String localPort = clientSocket.getRemoteSocketAddress().toString().split("[/:]")[2];
 			Singleton.log("New client with IP: " + clientIp + " and local port: " + localPort);
 		}
 	}
 
 	private void startNetClientThread(Socket clientSocket) {
 		ServerConnection serverConnection = new ServerConnection(clientSocket, this);
 		Thread serverConnectionThread = new Thread(serverConnection);
 		serverConnectionThread.start();
 	}
 
 	public List<ClientConnection> getClientConnections() {
 		return clientConnections;
 
 	}
 
 	public ServerController getServerController() {
 		return sController;
 	}
 }
