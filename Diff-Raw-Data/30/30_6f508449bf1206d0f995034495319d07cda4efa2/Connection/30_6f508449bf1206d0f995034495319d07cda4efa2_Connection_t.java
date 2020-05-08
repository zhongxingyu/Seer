 package ch.bfh.bti7301.w2013.battleship.network;
 
 import java.io.*;
 import java.net.*;
 
 import ch.bfh.bti7301.w2013.battleship.game.Game;
 import ch.bfh.bti7301.w2013.battleship.game.Missile;
 import ch.bfh.bti7301.w2013.battleship.game.players.GenericPlayer;
 import ch.bfh.bti7301.w2013.battleship.game.players.GenericPlayer.PlayerState;
 
 public class Connection extends Thread {
 
 	final static int GAMEPORT = 42423;
 
 	private ConnectionState connectionState;
 	private ConnectionStateListener connectionStateListener;
 	private static Connection instance;
 	
 	private ConnectionHandler handler;
 	private static Game game;
 
 
 	private Connection(Game game) throws IOException {
 		new ConnectionListener(this).start();
 		setGame(game);
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
 		
 	}
 
 	public void connectOpponet(String Ip) {
 
 		if (getConnectionState() == ConnectionState.CONNECTED) {
 			throw new RuntimeException("Already connected");
 		}
 
 		try {
 			Socket socket = new Socket(Ip, GAMEPORT);
 			handler = new ConnectionHandler(this, socket);
 			
 
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
 
 	public void sendReady() {
 		if (game.getLocalPlayer().getPlayerState() != GenericPlayer.PlayerState.GAME_STARTED && game.getOpponent().getPlayerState() != GenericPlayer.PlayerState.GAME_STARTED){
 		throw new RuntimeException("Something is out of order here");
 		}
 		
 		handler.sendObject(GenericPlayer.PlayerState.READY);
 		game.getOpponent().setPlayerState(GenericPlayer.PlayerState.WAITING); 
 	}
 
 
 	public void sendEnd(PlayerState end) {
 		handler.sendObject(end);
 		cleanUp();
 	}
 
 
 	public void sendStart() {
 		if (game.getLocalPlayer().getPlayerState() != GenericPlayer.PlayerState.GAME_STARTED && game.getOpponent().getPlayerState() != GenericPlayer.PlayerState.READY){
 		throw new RuntimeException("Something is out of order here");
 		}
 		
 		handler.sendObject(GenericPlayer.PlayerState.WAITING);
 		game.getOpponent().setPlayerState(GenericPlayer.PlayerState.PLAYING);
 	}
 
 
 	public static void receiveObjectToGame(Object object) {
 
 		if (object == GenericPlayer.PlayerState.READY){
 			game.getOpponent().setPlayerState(GenericPlayer.PlayerState.READY);
 			game.getLocalPlayer().setPlayerState(GenericPlayer.PlayerState.WAITING);
 		}
 		
 		else if (object == GenericPlayer.PlayerState.WAITING){
 			game.getOpponent().setPlayerState(GenericPlayer.PlayerState.WAITING);
 			game.getLocalPlayer().setPlayerState(GenericPlayer.PlayerState.PLAYING);
 		}
 		
 		else if (object == GenericPlayer.PlayerState.WAITING){
 			game.getOpponent().setPlayerState(GenericPlayer.PlayerState.WAITING);
 			game.getLocalPlayer().setPlayerState(GenericPlayer.PlayerState.PLAYING);
 		}
 		
 		else if (object == GenericPlayer.PlayerState.GAME_LOST){
 			game.getOpponent().setPlayerState(GenericPlayer.PlayerState.GAME_LOST);
 			game.getLocalPlayer().setPlayerState(GenericPlayer.PlayerState.GAME_WON);
 		}
 		
 		else if (object == GenericPlayer.PlayerState.GAME_WON){
 			game.getOpponent().setPlayerState(GenericPlayer.PlayerState.GAME_WON);
 			game.getLocalPlayer().setPlayerState(GenericPlayer.PlayerState.GAME_LOST);
 		}
 		
 		
 		else  {
 			
			game.getLocalPlayer().placeMissile((Missile)object);
 				
 			}
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
 	
 	public void setGame(Game game){
 		this.game = game;
 	}
 	
 	private void cleanUp() {
 		handler.cleanUp();
 		setConnectionState(ConnectionState.CLOSED);
 	}
 }
