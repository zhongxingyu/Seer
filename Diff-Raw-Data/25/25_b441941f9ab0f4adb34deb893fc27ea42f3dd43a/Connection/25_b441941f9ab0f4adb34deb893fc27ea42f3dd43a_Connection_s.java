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
 
 	private ArrayList<ConnectionStateListener> connectionStateListeners = new ArrayList<ConnectionStateListener>();
 	private ConnectionListener listener;
 	private ConnectionHandler handler;
 
 	private static Game game = Game.getInstance();
 	private static GameState gameState = GameState.getInstance();
 
 	private Connection() {
 		listener = new ConnectionListener(this);
 		listener.start();
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
 
 		} catch (IOException e) {
 			e.printStackTrace();
 			catchAndReestablish(ConnectionState.CONNECTIONERROR,
 					"couldn't create connectionHandler");
 
 			gameState.setOpponentIp(handler.getOpponentIp());
 			gameState.setLocalIp(handler.getLocalIp());
 
 		}
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
 			switch (received) {
 			case READY:
 				if (game.getLocalPlayer().getPlayerState() == PlayerState.READY) {
 					game.getLocalPlayer().setPlayerState(PlayerState.PLAYING);
 					game.getOpponent().setPlayerState(PlayerState.WAITING);
 				} else {
 					game.getOpponent().setPlayerState(PlayerState.READY);
 				}
 				break;
 			case WAITING:
 				game.getOpponent().setPlayerState(PlayerState.WAITING);
 				game.getLocalPlayer().setPlayerState(PlayerState.PLAYING);
 				break;
 			case GAME_LOST:
 				game.getOpponent().setPlayerState(PlayerState.GAME_LOST);
 				game.getLocalPlayer().setPlayerState(PlayerState.GAME_WON);
 				break;
 			case GAME_WON:
 				game.getOpponent().setPlayerState(PlayerState.GAME_WON);
 				game.getLocalPlayer().setPlayerState(PlayerState.GAME_LOST);
 				break;
 			case GAME_STARTED:
 				// TODO
 				break;
 			case PLAYING:
 				// TODO
 				break;
 			default:
 				throw new RuntimeException("Invalid PlayerState received:"
 						+ received);
 			}
 
 		} else if (object instanceof Missile) {
 			Missile received = (Missile) object;
 			instance.handleMissile(received);
 		} else if (object instanceof GameRule) {
 			GameRule received = (GameRule) object;
 			// This method checks if the gamerules are equal and corrects any differences
 			Game.getInstance().checkGameRule(received);
 		}
 
 		else if (object instanceof Integer) {
 			gameState.restoreGame(object);
 		}
 
 		setGameStateIn(object);
 	}
 
 	private void handleMissile(Missile missile) {
 
 		switch (missile.getMissileState()) {
 		case FIRED:
 			Missile feedback = game.getLocalPlayer().placeMissile(missile);
 			sendMissile(feedback);
 			break;
 		case MISS:
 			game.getOpponent().setPlayerState(PlayerState.PLAYING);
 			game.getLocalPlayer().setPlayerState(PlayerState.WAITING);
 			game.getOpponent().getBoard().updateMissile(missile);
 			break;
 		case HIT:
 		case SUNK:
 			game.getOpponent().setPlayerState(PlayerState.WAITING);
 			game.getLocalPlayer().setPlayerState(PlayerState.PLAYING);
 			game.getOpponent().getBoard().updateMissile(missile);
 			break;
 		case GAME_WON:
 			game.getOpponent().setPlayerState(PlayerState.GAME_LOST);
 			game.getLocalPlayer().setPlayerState(PlayerState.GAME_WON);
 			game.getOpponent().getBoard().updateMissile(missile);
 			break;
 		}
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
 
 	public ArrayList<String> findOpponents() {
 		// ToDO
 
 		return null;
 
 	}
 
 	private void cleanUp() {
 		listener = null;
 		handler.closeHandler();
 		handler = null;
 		setConnectionState(ConnectionState.CLOSED, "the connection was closed");
 		instance = null;
 	}
 }
