 package server.network;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.webbitserver.BaseWebSocketHandler;
 import org.webbitserver.WebSocketConnection;
 
 import server.GameHandler;
 import server.model.GameEvent;
 import server.model.entities.Entity;
 import server.util.IdFactory;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 public class GameServer extends BaseWebSocketHandler {
 
 	Gson json = new GsonBuilder().setExclusionStrategies(
 			new GsonExclusionStrategy()).create();
 	public static final String ID_KEY = "id";
 	public GameHandler gameHandler;
 
 	static class Incoming {
 		enum Action {
 			LOGIN, ROOM, SAY, KEYS_PRESSED, COLOR
 		}
 
 		Action action;
 		String loginUsername;
 		String roomJoin;
 		String message;
 		List<Integer> keysPressed;
 		String[] colors;
 	}
 
 	static class Outgoing {
 		enum Action {
 			JOIN, LEAVE, SAY, UPDATE, INIT_GAME, ROOMS, NEW_ROOM, COLOR
 		}
 
 		Action action;
 		String username;
 		String message;
 		Map<Integer, Entity> entities;
 		String map;
 		int playerId;
 		String[] rooms;
 		String newRoom;
 		GameEvent[] events;
 		public boolean gameRunning;
 		Map<Integer, String[]> colors;
 	}
 
 	public GameServer(GameHandler gameHandler) {
 		super();
 		this.gameHandler = gameHandler;
 	}
 
 	private Set<WebSocketConnection> connections = new HashSet<WebSocketConnection>();
 
 	@Override
 	public void onOpen(WebSocketConnection connection) {
 		connections.add(connection);
 	}
 
 	@Override
 	public void onMessage(WebSocketConnection connection, String msg) {
 		Incoming incoming = json.fromJson(msg, Incoming.class);
 		switch (incoming.action) {
 		case LOGIN:
 			login(connection, incoming.loginUsername);
 			break;
 		case ROOM:
 			leaveCurrentRoom(connection);
 			if (incoming.roomJoin != null)
 				joinRoom(connection, incoming.roomJoin);
 			break;
 		// case SAY:
 		// say(connection, incoming.message);
 		// break;
 		case KEYS_PRESSED:
 			this.gameHandler.setKeysPressed((Integer) connection.data(ID_KEY),
 					incoming.keysPressed);
 			break;
 		case COLOR:
 			this.gameHandler.setPlayerColor((Integer) connection.data(ID_KEY),
 					incoming.colors);
 			break;
 		default:
 			System.err
 					.println("Warning: Unknown incoming action action (see GameServer.onMessage)");
 		}
 	}
 
 	private int getCurrentId() {
 		return IdFactory.getNextId();
 	}
 
 	private void login(WebSocketConnection connection, String username) {
 		int id = this.getCurrentId();
 		connection.data(ID_KEY, id); // associate username with connection
 		gameHandler.playerLogin(id, username);
 	}
 
 	private void joinRoom(WebSocketConnection connection, String roomJoin) {
 		int id = (Integer) connection.data().get(ID_KEY);
 		gameHandler.joinRoom(id, roomJoin);
 	}
 
 	private void leaveCurrentRoom(WebSocketConnection connection) {
 		int id = (Integer) connection.data().get(ID_KEY);
 		gameHandler.leaveCurrentRoom(id);
 	}
 
 	public void sendJoinMessage(int id, String user, String roomJoin,
 			List<Integer> receipants) {
 		Outgoing outgoing = new Outgoing();
 		outgoing.action = Outgoing.Action.JOIN;
 		outgoing.username = user;
 		this.sendGameInfo(findConnection(id), gameHandler.getGameRoom(roomJoin)
 				.getMapName());
 		broadcast(outgoing, receipants);
 	}
 
 	private WebSocketConnection findConnection(int id) {
 		WebSocketConnection connection = null;
 		for (WebSocketConnection c : connections) {
 			if (new Integer(id).equals(c.data(ID_KEY))) {
 				connection = c;
 			}
 		}
 		if (connection == null)
 			throw new RuntimeException("No connection for specified ID");
 		return connection;
 	}
 
 	// private void say(WebSocketConnection connection, String message) {
 	// String username = (String) connection.data(ID_KEY);
 	// if (username != null) {
 	// Outgoing outgoing = new Outgoing();
 	// outgoing.action = Outgoing.Action.SAY;
 	// outgoing.username = username;
 	// outgoing.message = message;
 	// broadcast(outgoing, receipants);
 	// }
 	// }
 
 	public void update(boolean gameRunning, Map<Integer, Entity> entities,
 			GameEvent[] events) {
 		Outgoing outgoing = new Outgoing();
 		if (gameRunning)
 			outgoing.gameRunning = gameRunning;
 		outgoing.action = Outgoing.Action.UPDATE;
 		outgoing.entities = entities;
 		outgoing.events = events;
 		broadcast(outgoing, entities.keySet());
 	}
 
 	public void sendGameInfo(WebSocketConnection connection, String mapName) {
 		Outgoing outgoing = new Outgoing();
 		outgoing.action = Outgoing.Action.INIT_GAME;
 		outgoing.playerId = (Integer) connection.data().get(ID_KEY);
 		outgoing.map = mapName;
 		String jsonStr = this.json.toJson(outgoing);
 		connection.send(jsonStr);
 	}
 
 	private synchronized void broadcast(Outgoing outgoing,
 			Collection<Integer> playerIds) {
 		String jsonStr = this.json.toJson(outgoing);
 		for (WebSocketConnection connection : connections) {
 			if (playerIds.contains(connection.data(ID_KEY))) {
 				// only broadcast to those who have completed login
 				connection.send(jsonStr);
 			}
 		}
 	}
 
 	@Override
 	public void onClose(WebSocketConnection connection) {
 		// due concurrency issues the connection may already have been lost
 		// strange but true (maybe).
 		if (connection == null)
 			return;
 		int id = (Integer) connection.data(ID_KEY);
 		gameHandler.playerDisconnected(id);
 	}
 
 	public void sendDisconnectMessage(int id, String user,
 			List<Integer> recipients) {
 		Outgoing outgoing = new Outgoing();
 		outgoing.action = Outgoing.Action.LEAVE;
 		outgoing.username = user;
 		broadcast(outgoing, recipients);
 	}
 
 	public void disconnect(int id) {
 		WebSocketConnection connection = findConnection(id);
 		connections.remove(connection);
 
 	}
 
 	public void sendRoomList(Collection<String> rooms,
 			Collection<Integer> receipent) {
 		Outgoing outgoing = new Outgoing();
 		outgoing.action = Outgoing.Action.ROOMS;
 		outgoing.rooms = rooms.toArray(new String[0]);
 		broadcast(outgoing, receipent);
 	}
 
 	public void sendNewRoomInfo(String newRoomName,
 			Collection<Integer> recipients) {
 		Outgoing outgoing = new Outgoing();
 		outgoing.action = Outgoing.Action.NEW_ROOM;
 		outgoing.newRoom = newRoomName;
 		broadcast(outgoing, recipients);
 	}
 
 	public void sendPlayerColors(Map<Integer, String[]> colors,
 			Collection<Integer> recipients) {
 		Outgoing outgoing = new Outgoing();
 		outgoing.action = Outgoing.Action.COLOR;
 		outgoing.colors = colors;
 		broadcast(outgoing, recipients);
 	}
 }
