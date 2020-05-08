 package server.connection;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.Vector;
 
 /**
  * 
  * @author D059270
  * 
  *         Der Connection Listerner wartet auf neue Clients und legt fr diese
  *         neue Verbindungen an. Er sollte hierfr in einem neuen Thread
  *         gestartet werden.
  * 
  */
 public class ConnectionListener extends Thread {
 
 	private int port = 0;
 	private Server server = null;
 	private ServerSocket serverSocket = null;
 	private Vector<ServerConnection> connections = new Vector<ServerConnection>();
 	private Boolean serverClosed=false;
 
 	/**
 	 * @param port
 	 * @param server
 	 *            Hier kann ein ConnectionListerner auf einem Port gestartet
 	 *            werden. Der Konstruktor bergibt den Server und den Port an
 	 *            die jeweiligen Attribute.
 	 */
 	ConnectionListener(int port, Server server) {
 		this.server = server;
 		this.port = port;
 	}
 
 	/**
 	 * Die run Mehtode wir in der Thread.start()-Methode aufgerufen. Ein Socket
 	 * auf den ber den Konstruktor bergebenen Port wird wird erstellt. Sollte
 	 * der Server nicht gestartet werden knnen, wird dieser nun beendet. Hier
 	 * wird auf neue Clients gewartet und eine neue Verbindung in einem neuen
 	 * Thread hergestellt.
 	 */
 	public void run() {
 
 		try {
 			serverSocket = new ServerSocket(port);
 
 		} catch (IOException e) {
 			System.out.println("Server kann nicht gestartet werden!");
 			System.exit(-1);
 
 		}
 
 		int count = 1;
 		while (!serverClosed) { // wartet auf neue Clients
 
 			
 			Socket socket=waitForClient(serverSocket);
 			if(socket==null || socket.isClosed() || serverSocket.isClosed()){
 				continue;
 			}
 			ServerConnection serverConnection = new ServerConnection(socket, count ,server);
 			serverConnection.start();
 			connections.add(serverConnection);
 			count++;
 		}
 	}
 
 	/**
 	 * 
 	 * @param serverSocket
 	 * @return ClientSocket. Blockiert solange, bis sich ein Client an dem
 	 *         ServerSocket angemeldet hat und erstellt eine Verbindung mit dem
 	 *         Client und liefert den dazugehrigen Socket zurck. Sollte eine
 	 *         Verbindung mit dem Client nicht mglich sein. wird dies ber die
 	 *         Console dokumentiert.
 	 */
 	private Socket waitForClient(ServerSocket serverSocket) {
 		Socket socket = null;
 		try {
 			socket = serverSocket.accept();
 		} catch (IOException e) {
 			System.out.println("Verbindung mit Client kann nicht aufgebaut werden!");
 			
 		}
 		return socket;
 	}
 
 	/**
 	 * Schliet jede einzelne Verbindung mit den Clients und schliet danach
 	 * sich selbst.
 	 */
 	public void close() {
 		serverClosed=true;
 		for (ServerConnection connection : connections) {
 			connection.close();
 			// connection.interrupt();
 		}
 
 		try {

 			serverSocket.close();
 
 		} catch (IOException e) {
 			System.out.println("Server kann nicht beendet werden!");
 		}
 	}
 }
