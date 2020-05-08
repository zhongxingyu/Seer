 package ch.bfh.bti7301.w2013.battleship.network;
 
 import java.io.*;
 import java.net.*;
 
 import ch.bfh.bti7301.w2013.battleship.game.Game;
 import ch.bfh.bti7301.w2013.battleship.game.Missile;
 import ch.bfh.bti7301.w2013.battleship.game.players.GenericPlayer.PlayerState;
 
 public class Connection extends Thread {
 
 	final static int GAMEPORT = 49768;
 
 	private ConnectionState connectionState;
 	private ConnectionStateListener connectionStateListener;
 	private static Connection instance;
 	private ConnectionListener listener;
 
 	private ConnectionHandler handler;
 	private static Game game = Game.getInstance();
 
 	private Connection() throws IOException {
 		listener = new ConnectionListener(this);
 		listener.start();
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
 
 	public void connectOpponent(String Ip) {
 
 		if (getConnectionState() == ConnectionState.CONNECTED) {
 			throw new RuntimeException("Already connected");
 		}
 
 		try {
 			Socket socket = new Socket(Ip, GAMEPORT);
 			handler = new ConnectionHandler(this, socket);
 			listener.closeListener();
 
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public static Connection getInstance() {
 		if (instance == null) {
 			try {
 				instance = new Connection();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		return instance;
 	}
 
 	public void sendMissile(Missile missile) {
 		if (instance.connectionState != ConnectionState.CONNECTED) {
 			throw new RuntimeException("Cannot shoot an imaginary Opponent");
 		}
 		handler.sendObject(missile);
 	}
 
 	public void sendStatus(PlayerState state) {
 
 		if (instance.connectionState != ConnectionState.CONNECTED) {
 			throw new RuntimeException("No Conncetion yet");
 		}
 		handler.sendObject(state);
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
 					// TODO: Set button to "Start"
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
 		}
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
 
 	public void closeConnection() {
 		instance.cleanUp();
 	}
 
 	private void cleanUp() {
 		//TODO: think about the whole closing game thing
 		handler.cleanUp();
 		setConnectionState(ConnectionState.CLOSED);
 	}
 }
