	package ch.bfh.bti7301.w2013.battleship.network;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 public class ConnectionListener extends Thread {
 
 	private ServerSocket listener;
 	private Connection connection;
 
 	public ConnectionListener(Connection connection) {
 		try {
 			listener = new ServerSocket(Connection.GAMEPORT);
 			this.connection = connection;
			connection.setConnectionState( ConnectionState.LISTENING, "the listener is set up");
 		} catch (IOException e) {
 			e.printStackTrace();
			connection.setConnectionState(ConnectionState.LISTENERERROR, "listener on port" + Connection.GAMEPORT + "could not be started.");
 		}
		
		
 	}
 
 	public void run() { // run the service
 		try {
			while (true) {
 				connection.acceptOpponent(listener.accept());
 				listener.close();
 			}
 
 		} catch (IOException e) {
			
 			e.printStackTrace();
			connection.setConnectionState(ConnectionState.LISTENERERROR, "opponent could not be accepted");
 		}
 	}
	
 	public void closeListener() {
 		try {
 			listener.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
			connection.setConnectionState(ConnectionState.CONNECTIONERROR, "connection could not be closed");
 		}
 	}
 
 }
