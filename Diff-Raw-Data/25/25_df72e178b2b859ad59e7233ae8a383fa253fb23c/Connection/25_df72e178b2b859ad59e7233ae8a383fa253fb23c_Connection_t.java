 package ch.bfh.bti7301.w2013.battleship.network;
 
 import java.io.*;
 import java.net.*;
 
 import ch.bfh.bti7301.w2013.battleship.game.Missile;
 
 public class Connection extends Thread {
 
 	final static int GAMEPORT = 42423;
 
 	private ConnectionState connectionState;
 	private ConnectionStateListener connectionStateListener;
	private static Connection instance;
 	
 	private ConnectionHandler handler;
 
 
 	private Connection() throws IOException {
 		new ConnectionListener(this).start();
 		setConnectionState(ConnectionState.LISTENING);
 		;
 	}
 
 	public void acceptOpponent(Socket socket) {
 
 		if (getConnectionState() == ConnectionState.CONNECTED) {
 			throw new RuntimeException("Already connected");
 		}
 		try {
 			handler = new ConnectionHandler(this, socket);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		// TODO Set Opponent Status
 	}
 
 	public void connectOpponet(String Ip) {
 
 		if (getConnectionState() == ConnectionState.CONNECTED) {
 			throw new RuntimeException("Already connected");
 		}
 
 		try {
 			Socket socket = new Socket(Ip, GAMEPORT);
 			handler = new ConnectionHandler(this, socket);
 			// TODO set Opponent State
 
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
	public static Connection getInstance() {
 		return instance;
 	}
 
 
 	public void placeShot(Missile missile) {
 		handler.sendObject(missile);
 	}
 
 	public void sendReady(String ready) {
 		handler.sendObject(ready);
 	}
 
 
 	public void sendEnd(String end) {
 		handler.sendObject(end);
 
 	}
 
 
 	public void sendStart(String start) {
 		handler.sendObject(start);
 	}
 
 
 	public Object receiveObjectToGame(Object object) {
 
 		// simon: wohin damit?
 		return null;
 	}
 
 
 	public ConnectionState getConnectionState() {
 		return connectionState;
 	}
 
 	public void setConnectionState(ConnectionState connectionState) {
 		this.connectionState = connectionState;
 		if (connectionStateListener != null) {
 			connectionStateListener.stateChanged(connectionState);
 		}
 	}
 
 	public void setConnectionStateListener(
 			ConnectionStateListener connectionStateListener) {
 		this.connectionStateListener = connectionStateListener;
 	}
 	
 	private void cleanUp() {
 		//TODO 
 	}
 }
