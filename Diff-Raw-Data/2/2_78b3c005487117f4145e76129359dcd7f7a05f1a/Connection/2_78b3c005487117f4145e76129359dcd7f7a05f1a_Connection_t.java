 package ch.bfh.bti7301.w2013.battleship.network;
 
 import java.io.*;
 import java.net.*;
 
 import ch.bfh.bti7301.w2013.battleship.game.Missile;
 
 public class Connection extends Thread {
 
 	final static int GAMEPORT = 42423;
 
 	public ConnectionState connectionState;
 
 	private static Connection instance;
 	private ServerSocket listener;
 	private String opponentIP;
 	private Socket connection;
 	private ObjectInputStream in;
 	private ObjectOutputStream out;
 
 	private ConnectionHandler handler;
 
 	/**
 	 * 
 	 * @throws IOException
 	 */
 	private Connection() throws IOException {
 		// listener = new ServerSocket(GAMEPORT);
 		// opponentIP = null;
 		start();
 		new ConnectionListener(this).start();;
 	}
 
 	/**
 	 * 
 	 * @param opponentIP
 	 */
 	// private Connection(String opponentIP) {
 	// listener = null;
 	// this.opponentIP = opponentIP;
 	// start();
 	// }
 	public void setClientSocket(Socket socket) {
 		// TODO
 	}
 
 	public String connectOpponet(String Ip) {
 		opponentIP = Ip;
 
 		return Ip;
 
 	}
 
 	/**
 	 * Try to connect with an opponent.
 	 */
 	@Override
 	public void run() { // run the service
 		try {
 			while (true) {
 				if (opponentIP == null) {
 					connection = listener.accept();
 					opponentIP = connection.getInetAddress().getHostAddress();
 					connectionState = ConnectionState.LISTENING;
 
 				} else {
 					connection = new Socket(opponentIP, GAMEPORT);
 				}
 
 				out = new ObjectOutputStream(connection.getOutputStream());
 				in = new ObjectInputStream(connection.getInputStream());
 				handler = new ConnectionHandler(in, instance);
 				connectionState = ConnectionState.CONNECTED;
 				listener.close();
 			}
 
 		} catch (IOException e) {
 			e.printStackTrace();
 			cleanUp();
 		}
 	}
 
 	/**
 	 * 
 	 * @param opponentIP
 	 * @return
 	 * @throws IOException
 	 */
 	public static Connection getInstance() throws IOException {
 
 		if (instance != null) {
 			return instance;
 			// } else if (opponentIP == null) {
 			// instance = new Connection();
 			// return instance;
 		} else {
			instance = new Connection();
 			return instance;
 		}
 	}
 
 	/**
 	 * 
 	 * @param missile
 	 */
 	public void placeShot(Missile missile) {
 		handler.sendObject(out, missile);
 	}
 
 	/**
 	 * 
 	 * @param ready
 	 */
 
 	public void sendReady(String ready) {
 		handler.sendObject(out, ready);
 	}
 
 	/**
 	 * 
 	 * @param end
 	 */
 	public void sendEnd(String end) {
 		handler.sendObject(out, end);
 
 	}
 
 	/**
 	 * 
 	 * @param start
 	 */
 	public void sendStart(String start) {
 		handler.sendObject(out, start);
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	public Object receiveObjectToGame(Object object) {
 
 		// simon: wohin damit?
 		return null;
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	public ConnectionState getConnectionState() {
 		return connectionState;
 	}
 	
 	public void setConnectionState(ConnectionState connectionState) {
 		this.connectionState = connectionState;
 //		if (connectionStateListener!=null){
 //			connectionStateListener.stateChanged(connectionState);
 //		}
 	}
 
 	/**
 	 * 
 	 */
 	private void cleanUp() {
 		if (connection != null && !connection.isClosed()) {
 			// Make sure that the socket, if any, is closed.
 			try {
 				connection.close();
 			} catch (IOException e) {
 			}
 		}
 		connectionState = ConnectionState.CLOSED;
 		instance = null;
 		opponentIP = null;
 		connection = null;
 		in = null;
 		out = null;
 
 	}
 }
