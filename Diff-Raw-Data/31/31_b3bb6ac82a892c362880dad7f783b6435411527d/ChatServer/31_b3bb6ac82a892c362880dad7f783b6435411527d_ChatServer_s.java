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
 
 	public ChatServer(String ip, int port) {
 		this.clients = new ArrayList<ClientConnection>();
 		new Thread(this).start();
 	}
 
 	public void run() {
 		this.startServer();
 	}
 	
 	private void startServer() {
 		try {

			final ServerSocket serverSocket = new ServerSocket(8000);
 
 			for (;;) {
 
 				final Socket clientSocket = serverSocket.accept();
 
 				ClientConnection connection = new ClientConnection(clientSocket);
 				this.clients.add(connection);
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
