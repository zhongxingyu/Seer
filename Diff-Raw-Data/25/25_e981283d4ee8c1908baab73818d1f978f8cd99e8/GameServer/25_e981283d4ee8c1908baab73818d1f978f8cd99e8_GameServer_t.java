 package server;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.webbitserver.BaseWebSocketHandler;
 import org.webbitserver.WebSocketConnection;
 
 import com.google.gson.Gson;
 
 public class GameServer extends BaseWebSocketHandler {
 
 	private final Gson json = new Gson();
 
 	public static final String ID_KEY = "id";
 	private static int CURRENT_ID = 0;
 	public GameHandler gameHandler;
 
 	static class Incoming {
 		enum Action {
 			LOGIN, ROOM, SAY, KEYS_PRESSED
 		}
 
 		Action action;
 		String loginUsername;
 		String roomJoin;
 		String message;
 		List<Integer> keysPressed;
 	}
 
 	static class Outgoing {
 		enum Action {
 			JOIN, LEAVE, SAY, UPDATE, MAP, ROOMS, NEW_ROOM
 		}
 
 		Action action;
 		String username;
 		String message;
 		List<Player> players;
 		int[][] map;
 		String[] rooms;
 		String newRoom;
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
 			joinRoom(connection, incoming.roomJoin);
 			break;
 		// case SAY:
 		// say(connection, incoming.message);
 		// break;
 		case KEYS_PRESSED:
 			this.gameHandler.setKeysPressed((Integer) connection.data(ID_KEY),
 					incoming.keysPressed);
 			break;
 		}
 	}
 
 	private int getCurrentId() {
 		CURRENT_ID++;
 		return CURRENT_ID;
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
 		this.sendMap(findConnection(id), gameHandler.getGameMap(roomJoin));
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
 
 	public void update(List<Player> players) {
 		Outgoing outgoing = new Outgoing();
 		outgoing.action = Outgoing.Action.UPDATE;
 		outgoing.players = players;
 		broadcast(outgoing, gameHandler.idsFromPlayers(players));
 	}
 
 	public void sendMap(WebSocketConnection connection, int[][] map) {
 		Outgoing outgoing = new Outgoing();
 		outgoing.action = Outgoing.Action.MAP;
 		outgoing.map = map;
 		String jsonStr = this.json.toJson(outgoing);
 		connection.send(jsonStr);
 	}
 
 	private void broadcast(Outgoing outgoing, Collection<Integer> playerIds) {
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
 		int id = (Integer) connection.data(ID_KEY);
 		gameHandler.playerDisconnected(id);
 	}
 
 	public void sendDisconnectMessage(int id, String user,
 			List<Integer> receipants) {
 		Outgoing outgoing = new Outgoing();
 		outgoing.action = Outgoing.Action.LEAVE;
 		outgoing.username = user;
 		broadcast(outgoing, receipants);
 	}
 
 	public void disconnect(int id) {
 		WebSocketConnection connection = findConnection(id);
 		connections.remove(connection);
 
 	}
 
 	public void sendRoomList(Collection<String> rooms,
 			Collection<Integer> receivers) {
 		Outgoing outgoing = new Outgoing();
 		outgoing.action = Outgoing.Action.ROOMS;
 		outgoing.rooms = rooms.toArray(new String[0]);
 		broadcast(outgoing, receivers);
 	}
 
	public void sendNewRoomInfo(String newRoomName,
			Collection<Integer> receipants) {
 		Outgoing outgoing = new Outgoing();
 		outgoing.action = Outgoing.Action.NEW_ROOM;
 		outgoing.newRoom = newRoomName;
 		broadcast(outgoing, receipants);
 	}
 }
