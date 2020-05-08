 package ch.bfh.bti7301.w2013.battleship.network;
 
 	import java.io.*;
 import java.net.*;
 import java.util.ArrayList;
 
 import ch.bfh.bti7301.w2013.battleship.game.Game;
 import ch.bfh.bti7301.w2013.battleship.game.GameRule;
 import ch.bfh.bti7301.w2013.battleship.game.GameState;
 import ch.bfh.bti7301.w2013.battleship.game.Missile;
 import ch.bfh.bti7301.w2013.battleship.game.players.GenericPlayer.PlayerState;
 
 public class Connection extends Thread {
 
 	final static int GAMEPORT = 49768;
 
 	private static Connection instance;
 
 	private ConnectionState connectionState;
 	private String connectionStateMessage;
 
 	private ArrayList<DiscoveryListener> discoveryListeners = new ArrayList<DiscoveryListener>();
 	private ArrayList<ConnectionStateListener> connectionStateListeners = new ArrayList<ConnectionStateListener>();
 	private ConnectionListener listener;
 	private ConnectionHandler handler;
 	private static Game game = Game.getInstance();
 	private static GameState gameState = GameState.getInstance();
 
 
 	private Connection() {
 		listener = new ConnectionListener(this);
 		listener.start();
 		
 		setConnectionState(ConnectionState.LISTENING,
 				"the listener is set up");
 		
 		findOpponent();			//TODO: Should this be somwhere else?? For test only!!
 	}
 
 	public static Connection getInstance() {
 		if (instance == null) {
 			instance = new Connection();
 		}
 		return instance;
 	}
 
 	public void acceptOpponent(Socket socket) {
 
 		if (getConnectionState() == ConnectionState.CONNECTED) {
 			setConnectionState(ConnectionState.CONNECTED, "already connected!");
 			// TODO: GUI Should show this Error
 		}
 		try {
 			handler = new ConnectionHandler(this, socket);
 			listener.closeListener();
 			setConnectionState(ConnectionState.CONNECTED,
 					"connection established");
 
 		} catch (IOException e) {
 			e.printStackTrace();
 			catchAndReestablish(ConnectionState.CONNECTIONERROR,
 					"couldn't create connectionHandler");
 		}
 		
 		gameState.setOpponentIp(handler.getOpponentIp());
 		gameState.setLocalIp(handler.getLocalIp());
 		
 	}
 
 	public void connectOpponent(String Ip) {
 
 		if (getConnectionState() == ConnectionState.CONNECTED) {
 			setConnectionState(ConnectionState.CONNECTED, "already connected!");
 			// TODO: GUI Should show this Error
 		}
 
 		try {
 			Socket socket = new Socket(Ip, GAMEPORT);
 			handler = new ConnectionHandler(this, socket);
 			listener.closeListener();
 			
 			setConnectionState(ConnectionState.CONNECTED,
 					"connection established");
 
 		} catch (IOException e) {
 			e.printStackTrace();
 			setConnectionState(ConnectionState.CONNECTIONERROR,
 					"couldn't connect, please try again");
 			// TODO: GUI Should show this Error and ask the user to connect
 			// again.
 		}
 
 		gameState.setOpponentIp(handler.getOpponentIp());
 		gameState.setLocalIp(handler.getLocalIp());
 		
 		
 	}
 
 	public void sendMissile(Missile missile) {
 		if (instance.connectionState != ConnectionState.CONNECTED) {
 			throw new RuntimeException("Cannot shoot an imaginary Opponent");
 			// TODO: GUI Should show this Error and ask the user to connect
 			// again.
 		}
 		handler.sendObject(missile);
 
 		setGameStateOut(missile);
 
 	}
 
 	public void sendStatus(PlayerState state) {
 
 		if (instance.connectionState != ConnectionState.CONNECTED) {
 			throw new RuntimeException("No Conncetion yet");
 			// TODO: GUI Should show this Error
 		}
 		handler.sendObject(state);
 
 		setGameStateOut(state);
 	}
 
 	public void sendCounter(int counter) {
 		if (instance.connectionState != ConnectionState.CONNECTED) {
 			throw new RuntimeException("No Conncetion yet");
 			// TODO: GUI Should show this Error
 		}
 		handler.sendObject(counter);
 
 	}
 	
 	public void sendGameRule(GameRule rule) {
 		if (instance.connectionState != ConnectionState.CONNECTED) {
 			throw new RuntimeException("No Connection yet");
 		// TODO: GUI Should show this Error 
 		}
 		handler.sendObject(rule);
 	}
 
 	public static void receiveObjectToGame(Object object) {
 
 		if (object instanceof PlayerState) {
 			PlayerState received = (PlayerState) object;
 			Game.getInstance().handlePlayerState(received);
 		} else if (object instanceof Missile) {
 			Missile received = (Missile) object;
 			Game.getInstance().handleMissile(received);
 		} else if (object instanceof GameRule) {
 			System.out.println("GameRule received: " + object);
 			GameRule received = (GameRule) object;
 			// This method checks if the gamerules are equal and corrects any differences
 			Game.getInstance().checkGameRule(received);
 		} else if (object instanceof Integer) {
 			Integer tiger = (Integer) object;
 			System.out.println("Tiger received, yay: " + tiger);
 		}
 
 		else if (object instanceof Integer) {
 			gameState.restoreGame(object);
 		}
 
 		setGameStateIn(object);
 	}
 
 	public ConnectionState getConnectionState() {
 		return connectionState;
 	}
 
 	public String getConnectionStateMessage() {
 		return connectionStateMessage;
 	}
 
 	public void setConnectionState(ConnectionState connectionState, String msg) {
 		this.connectionState = connectionState;
 		this.connectionStateMessage = msg;
 		for(ConnectionStateListener listener : connectionStateListeners) {
 			listener.stateChanged(connectionState, msg);
 		}
 	}
 
 	public void addConnectionStateListener(ConnectionStateListener csl) {
 		connectionStateListeners.add(csl);
 	}
 
 	public void closeConnection() {
 		instance.cleanUp();
 	}
 
 	public void catchAndReestablish(ConnectionState errorType, String errorMsg) {
 
 		game.getLocalPlayer().setPlayerState(PlayerState.BLOCCKED);
 		
 		if (GameState.getInstance().getReestablishCount() > 0)
 			instance.setConnectionState(errorType, errorMsg);
 
 		else {
 			Connection.getInstance().setConnectionState(errorType,
 					errorMsg + " the connection will try to reset itself");
 			instance.reestablishConnection();
 		}
 
 	}
 
 	public void reestablishConnection() {
 		gameState.addReestablishCount();
 		getInstance().cleanUp();
 		Connection.getInstance();
 		instance.connectOpponent(gameState.getOpponentIp());
 		// Do something to make user Wait for the Recovery.
 		instance.sendCounter(gameState.getCounter());
 	}
 
 	private void setGameStateOut(Object out) {
 		gameState.addCounter();
 		gameState.setLastOut(out);
 		gameState.setLocalPlayerState(game.getLocalPlayer().getPlayerState());
 		gameState.setOpponentPlayerState(game.getOpponent().getPlayerState());
 	}
 
 	private static void setGameStateIn(Object in) {
 		gameState.addCounter();
 		gameState.setLastIn(in);
 		gameState.setLocalPlayerState(game.getLocalPlayer().getPlayerState());
 		gameState.setOpponentPlayerState(game.getOpponent().getPlayerState());
 	}
  
 	
 	public void addDiscoveryListener(DiscoveryListener listener){
 		discoveryListeners.add(listener);
 	}
 	
 	public void findOpponent(){
 		NetworkScanner.getInstance();
 		System.out.println("Connection: NetworkScanner startet");
 	}
 	
 	public void foundOpponent(String ip, String name){
 		System.out.println("Connection: method foundOpponent on Connection is called");
 		int i = 0;
 		for(DiscoveryListener listener : discoveryListeners) {
 			listener.foundOpponent(ip, name);
 			System.out.println("Connection: listener-" + i);
 			i++;
 		}
 		
 	}
 	
 	public ConnectionHandler getHandler() {
 		return handler;
 	}
 
 	private void cleanUp() {
 		listener = null;
 		handler.closeHandler();
 		handler = null;
 		setConnectionState(ConnectionState.CLOSED, "the connection was closed");
 		instance = null;
 	}
 }
