 package ch.bfh.bti7301.w2013.battleship.network;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 
 
 public class ConnectionListener extends Thread {
 
 	private ServerSocket listener;
 	private Connection connection;
 	private boolean isClosed = false;
 
 	public ConnectionListener(Connection connection) {
 		try {
 			listener = new ServerSocket(Connection.GAMEPORT);
 			this.connection = connection;
 		} catch (IOException e) {
 			e.printStackTrace();
 			connection.setConnectionState(ConnectionState.LISTENERERROR,
 					"listener on port" + Connection.GAMEPORT
 							+ "could not be started.");
 			//TODO: Maybe try an alternative Port: GUI Interaction
 		}
 
 	}
 
 	public void run() { 
 		try {
 			while (!listener.isClosed()) {
 				connection.acceptOpponent(listener.accept());	
 			}
 
 		} catch (IOException e) {
 			if (isClosed) {
 				return;
 			}
 			e.printStackTrace();
 			connection.catchAndReestablish(ConnectionState.LISTENERERROR, "opponent could not be accepted the connection");
 		}
 	}
 
 	public void closeListener() {
 		if (!listener.isClosed())
 		try {
 			isClosed = true;
 			listener.close();
 		} catch (IOException e) {
 			if (isClosed) {
 				return;
 			}
 			e.printStackTrace();
 			connection.setConnectionState(ConnectionState.CONNECTIONERROR,
 					"the connection is stuck");
 			//TODO: Some GUI Interaction is asked for.
 		}
 	}
 
 }
