 package at.fhv.audioracer.client.player;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.util.HashMap;
 import java.util.concurrent.Executor;
 import java.util.concurrent.Executors;
 
 import at.fhv.audioracer.communication.player.IPlayerClient;
 import at.fhv.audioracer.communication.player.IPlayerClientManager;
 import at.fhv.audioracer.communication.player.PlayerNetwork;
 import at.fhv.audioracer.core.model.Car;
 import at.fhv.audioracer.core.model.Player;
import at.fhv.audioracer.core.model.Position;
 import at.fhv.audioracer.core.util.ListenerList;
 
 import com.esotericsoftware.kryonet.Client;
 import com.esotericsoftware.kryonet.Connection;
 import com.esotericsoftware.kryonet.rmi.ObjectSpace;
 import com.esotericsoftware.kryonet.rmi.RemoteObject;
 
 public class PlayerClient extends Connection implements IPlayerClient {
 	
 	private static class PlayerClientListenerList extends ListenerList<IPlayerClientListener> implements IPlayerClientListener {
 		
 		@Override
 		public void onUpdateGameState(int playerId) {
 			
 			for (IPlayerClientListener listener : listeners()) {
 				listener.onUpdateGameState(playerId);
 			}
 			
 		}
 		
 		@Override
 		public void onUpdateCheckpointDirection() {
 			
 			for (IPlayerClientListener listener : listeners()) {
 				listener.onUpdateCheckpointDirection();
 			}
 			
 		}
 		
 		@Override
 		public void onUpdateFreeCars() {
 			
 			for (IPlayerClientListener listener : listeners()) {
 				listener.onUpdateFreeCars();
 			}
 			
 		}
 		
 		@Override
 		public void onPlayerConnected(int playerId) {
 			
 			for (IPlayerClientListener listener : listeners()) {
 				listener.onPlayerConnected(playerId);
 			}
 			
 		}
 		
 		@Override
 		public void onPlayerDisconnected(int playerId) {
 			
 			for (IPlayerClientListener listener : listeners()) {
 				listener.onPlayerDisconnected(playerId);
 			}
 			
 		}
 		
 		@Override
 		public void onGameStarts() {
 			
 			for (IPlayerClientListener listener : listeners()) {
 				listener.onGameStarts();
 			}
 			
 		}
 		
 	}
 	
 	/*
 	 * holds known connected players
 	 */
 	private HashMap<Integer, Player> _players;
 	
 	/*
 	 * holds known cars
 	 */
 	private HashMap<Integer, Car> _cars;
 	
 	/*
 	 * ids of free cars
 	 */
 	private int[] _freeCarIds;
 	
 	/*
 	 * player of this client
 	 */
 	private Player _player;
 	
 	/*
 	 * speed between -1 (reverse) and 1 (forward)
 	 */
 	private float speed;
 	
 	/*
 	 * Connection client
 	 */
 	private Client _client;
 	
 	/*
 	 * 
 	 */
 	boolean _connected;
 	
 	public float getSpeed() {
 		return speed;
 	}
 	
 	public void setSpeed(float speed) {
 		this.speed = speed;
 	}
 	
 	public float getDirection() {
 		return direction;
 	}
 	
 	public void setDirection(float direction) {
 		this.direction = direction;
 	}
 	
 	/*
 	 * direction between -1 (left) and 1 (right)
 	 */
 	private float direction;
 	
 	/*
 	 * direction of next checkpoint
 	 */
 	private Position _nextCheckpoint;
 	
 	/*
 	 * list of PlayerClientListener
 	 */
 	private PlayerClientListenerList _listenerList;
 	
 	/*
 	 * PlayerClientManager of this Client
 	 */
 	private IPlayerClientManager _playerClientManager;
 	
 	public PlayerClient() {
 		super();
 		_players = new HashMap<>();
 		_cars = new HashMap<>();
 		_player = new Player();
 		_listenerList = new PlayerClientListenerList();
 		_connected = false;
 		
 	}
 	
 	@Override
 	public void updateGameState(int playerId, int coinsLeft, int time) {
 		
 		if (_players.containsKey(Integer.valueOf(playerId))) {
 			Player p = _players.get(Integer.valueOf(playerId));
 			p.setCoinsLeft(coinsLeft);
 			p.setTime(time);
 		}
 		
 		_listenerList.onUpdateGameState(playerId);
 		
 	}
 	
 	@Override
 	public void playerConnected(int playerId, String playerName) {
 		
 		if (!_players.containsKey(Integer.valueOf(playerId))) {
 			Player p = new Player();
 			p.setPlayerId(playerId);
 			p.setLoginName(playerName);
 			_players.put(playerId, p);
 		}
 		
 		_listenerList.onPlayerConnected(playerId);
 		
 	}
 	
 	@Override
 	public void playerDisconnected(int playerId) {
 		
 		if (_players.containsKey(Integer.valueOf(playerId))) {
 			Player p = _players.get(Integer.valueOf(playerId));
 			_players.remove(p);
 		}
 		
 		_listenerList.onPlayerDisconnected(playerId);
 		
 	}
 	
 	@Override
 	public void updateCheckpointDirection(float directionX, float directionY) {
 		
 		_listenerList.onUpdateCheckpointDirection();
 		
 	}
 	
 	@Override
 	public void updateFreeCars(int[] carIds) {
 		
 		// check list for unknown free cars and add them
 		for (int i = 0; i < carIds.length; i++) {
 			if (!_cars.containsKey(carIds[i])) {
 				_cars.put(carIds[i], new Car(carIds[i]));
 			}
 		}
 		
 		_freeCarIds = carIds;
 		
 		_listenerList.onUpdateFreeCars();
 		
 	}
 	
 	@Override
 	public void gameOver() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	@Override
 	public void invalidCommand() {
 		// TODO Auto-generated method stub
 		System.out.println("invalid command");
 	}
 	
 	@Override
 	public void gameStarts() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	public HashMap<Integer, Player> getPlayers() {
 		return _players;
 	}
 	
 	public HashMap<Integer, Car> getCars() {
 		return _cars;
 	}
 	
 	public int[] getFreeCarIds() {
 		return _freeCarIds;
 	}
 	
 	public Player getPlayer() {
 		return _player;
 	}
 	
 	public Position getNextCheckpoint() {
 		return _nextCheckpoint;
 	}
 	
 	public IPlayerClientManager getPlayerClientManager() {
 		return _playerClientManager;
 	}
 	
 	public void setPlayerClientManager(IPlayerClientManager playerClientManager) {
 		_playerClientManager = playerClientManager;
 	}
 	
 	public ListenerList<IPlayerClientListener> getListenerList() {
 		return _listenerList;
 	}
 	
 	public void startClient(String playerName) throws IOException {
 		// All method calls from kryonet will be made through this executor.
 		// We are using a single threaded executor to ensure that everything is done on the same
 		// thread we won't run in any cross threading problems.
 		final Executor executor = Executors.newSingleThreadExecutor();
 		
 		_client = new Client();
 		_client.start();
 		
 		// Register the classes that will be sent over the network.
 		PlayerNetwork.register(_client);
 		
 		// get the PlayerClientManager from the server
 		IPlayerClientManager _playerClientManager = ObjectSpace.getRemoteObject(_client, PlayerNetwork.PLAYER_MANAGER, IPlayerClientManager.class);
 		RemoteObject obj = (RemoteObject) _playerClientManager;
 		obj.setTransmitExceptions(false); // disable exception transmitting
 		
 		// register the PlayerClient to kryonet
 		ObjectSpace objectSpace = new ObjectSpace(_client);
 		objectSpace.setExecutor(executor);
 		objectSpace.register(PlayerNetwork.PLAYER_CLIENT, this);
 		
 		_client.connect(1000, InetAddress.getLoopbackAddress(), PlayerNetwork.PLAYER_SERVICE_PORT);
 		_connected = true;
 		
 		setPlayerClientManager(_playerClientManager);
 		getPlayer().setPlayerId(_playerClientManager.connect(playerName));
 	}
 	
 	public void stopClient() {
 		_client.close();
 		_connected = false;
 	}
 	
 	public boolean hasConnection() {
 		return _connected;
 	}
 	
 }
