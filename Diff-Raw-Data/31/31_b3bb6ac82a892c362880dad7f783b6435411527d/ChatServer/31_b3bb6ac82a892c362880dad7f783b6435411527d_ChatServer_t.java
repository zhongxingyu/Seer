 package pw.svn.server;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * A useless chat server.
  * @author Martin
  * @author Waidmann
  * @version 1.0
  */
 public class ChatServer implements Runnable {
 	
 	private transient List<ClientConnection> clients;
	private transient int port;
 
 	public ChatServer(String ip, int port) {
 		this.clients = new ArrayList<ClientConnection>();
		this.port = port;
 		new Thread(this).start();
 	}
 
 	public void run() {
 		this.startServer();
 	}
 	
 	private void startServer() {
		ServerSocket serverSocket = null;
 		try {
			serverSocket = new ServerSocket(this.port);
 
 			for (;;) {
 
 				final Socket clientSocket = serverSocket.accept();
 
 				ClientConnection connection = new ClientConnection(clientSocket);
 				this.clients.add(connection);
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
		} finally {
 		}
 	}
 }
