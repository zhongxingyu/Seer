 package at.fhv.audioracer.server.game;
 
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.ConcurrentModificationException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import at.fhv.audioracer.communication.player.message.FreeCarsMessage;
 import at.fhv.audioracer.communication.player.message.PlayerConnectedMessage;
 import at.fhv.audioracer.communication.player.message.PlayerDisconnectedMessage;
 import at.fhv.audioracer.communication.player.message.PlayerMessage;
 import at.fhv.audioracer.communication.player.message.PlayerMessage.MessageId;
 import at.fhv.audioracer.communication.player.message.ReconnectRequestResponse;
 import at.fhv.audioracer.communication.player.message.SelectCarResponseMessage;
 import at.fhv.audioracer.communication.player.message.SetPlayerNameResponseMessage;
 import at.fhv.audioracer.communication.player.message.StartGameMessage;
 import at.fhv.audioracer.communication.player.message.UpdateCheckPointDirectionMessage;
 import at.fhv.audioracer.communication.player.message.UpdateGameStateMessage;
 import at.fhv.audioracer.communication.world.CarClientManager;
 import at.fhv.audioracer.communication.world.ICarClient;
 import at.fhv.audioracer.communication.world.ICarManagerListener;
 import at.fhv.audioracer.core.model.Car;
 import at.fhv.audioracer.core.model.Checkpoint;
 import at.fhv.audioracer.core.util.Direction;
 import at.fhv.audioracer.core.util.Position;
 import at.fhv.audioracer.network.reconnect.IPlayerTimeoutEvent;
 import at.fhv.audioracer.network.reconnect.PlayerTimeoutScheduler;
 import at.fhv.audioracer.server.PlayerConnection;
 import at.fhv.audioracer.server.PlayerServer;
 import at.fhv.audioracer.server.WorldZigbeeMediator;
 import at.fhv.audioracer.server.model.IWorldZigbeeConnectionCountChanged;
 import at.fhv.audioracer.server.model.Player;
 
 public class GameModerator implements ICarManagerListener, IWorldZigbeeConnectionCountChanged,
 		IPlayerTimeoutEvent {
 	
 	private static Logger _logger = LoggerFactory.getLogger(GameModerator.class);
 	// TODO: player server is not threat safe at all
 	private PlayerServer _playerServer;
 	private CheckpointUtil _checkpointUtil = new CheckpointUtil();
 	private int _checkpointStartCount = 5;
 	private int _playersInGameCount = 0;
 	
 	private HashMap<Integer, Player> _playerList = new HashMap<Integer, Player>();
 	private int _plrId = 0;
 	
 	private Map<Byte, Car<Player>> _carList = Collections
 			.synchronizedMap(new HashMap<Byte, Car<Player>>());
 	private Map<Byte, ArrayDeque<Position>> _checkpoints = Collections
 			.synchronizedMap(new HashMap<Byte, ArrayDeque<Position>>());
 	
 	private Thread _worldZigbeeThread = null;
 	private WorldZigbeeMediator _worldZigbeeRunnable = new WorldZigbeeMediator();
 	
 	private Object _lockObject = new Object();
 	private boolean _gameRunning = false;
 	private long _gameStartTimeInMillis;
 	
 	// next conditions must be true for game start
 	private boolean _mapConfigured = false;
 	private boolean _detectionFinished = false;
 	
 	// next types used for handling different kinds of possible network-problems
 	private Set<Byte> _awaitingCarClientReconnectSet = Collections
 			.synchronizedSet(new HashSet<Byte>());
 	private PlayerTimeoutScheduler _playerTimeoutScheduler = new PlayerTimeoutScheduler(2);
 	
 	private at.fhv.audioracer.core.model.Map _map = null;
 	private static GameModerator _gameModerator = null;
 	
 	private GameModerator() {
 		_playerServer = PlayerServer.getInstance();
 		CarClientManager.getInstance().getCarClientListenerList().add(this);
 		_worldZigbeeThread = new Thread(_worldZigbeeRunnable);
		_worldZigbeeRunnable.getWorldZigbeeConnectionCountListenerList().add(this);
 		CarClientManager.getInstance().getCarClientListenerList().add(_worldZigbeeRunnable);
 		_playerTimeoutScheduler.registerEvent(this);
 	}
 	
 	public static GameModerator getInstance() {
 		if (_gameModerator == null) {
 			_gameModerator = new GameModerator();
 		}
 		return _gameModerator;
 	}
 	
 	/**
 	 * called on Player tries to set his player name
 	 * 
 	 * @param playerConnection
 	 *            the socket connection of this player
 	 * @param playerName
 	 *            name of player
 	 */
 	public void setPlayerName(PlayerConnection playerConnection, String playerName) {
 		int id = -1;
 		
 		// TODO: player can set his name twice, this should probably not allowed
 		
 		if (playerName != null) {
 			Player player = playerConnection.getPlayer();
 			player.setName(playerName);
 			synchronized (_playerList) {
 				id = ++_plrId;
 				player.setPlayerId(_plrId);
 				_playerList.put(_plrId, player);
 				_logger.debug("added {} to playerList", player);
 			}
 		} else {
 			_logger.warn("player name received is null! This is not allowed!");
 		}
 		SetPlayerNameResponseMessage resp = new SetPlayerNameResponseMessage();
 		resp.playerId = id;
 		_playerServer.sendToTCP(playerConnection.getID(), resp);
 		_broadcastFreeCars();
 	}
 	
 	/**
 	 * called on Camera "carDetected" request
 	 * 
 	 * @param newCar
 	 *            the detected car
 	 */
 	public void carDetected(Car<Player> newCar) {
 		
 		synchronized (_lockObject) {
 			if (_gameRunning || _detectionFinished) {
 				_logger.warn("carDetected not allowed!"
 						+ " Game running: {}, car detection finished: {}", _gameRunning,
 						_detectionFinished);
 			} else {
 				if (!_carList.containsKey(newCar.getCarId())) {
 					_logger.debug("carDetected - id: {}", newCar.getCarId());
 					
 					_carList.put(newCar.getCarId(), newCar);
 					_checkpoints.put(newCar.getCarId(), new ArrayDeque<Position>());
 					if (_map != null) {
 						_map.addCar(newCar);
 					}
 					newCar.getCarListenerList().add(_worldZigbeeRunnable);
 					// comment in next 2 lines for "test run"
 					// byte cId = 0;
 					// newCar.setCarClientId(cId);
 				} else {
 					_logger.warn("Car with id: {} allready known!", newCar.getCarId());
 				}
 			}
 		}
 		
 		_broadcastFreeCars();
 	}
 	
 	public void configureMap(int sizeX, int sizeY) {
 		_logger.debug("configureMap with sizeX: {} and sizeY: {} called", sizeX, sizeY);
 		
 		synchronized (_lockObject) {
 			if (_gameRunning) {
 				_logger.warn("configureMap not allowed while game is running!");
 			} else {
 				_mapConfigured = true;
 				_checkpointUtil.setMapSize(sizeX, sizeY);
 				_worldZigbeeRunnable.setMapSize(sizeX, sizeY);
 				if (_map != null) {
 					_map.setSizeX(sizeX);
 					_map.setSizeY(sizeY);
 				}
 				_checkPreconditionsAndStartGameIfAllFine();
 			}
 		}
 	}
 	
 	public void detectionFinished() {
 		_logger.debug("detectionFinished called");
 		
 		synchronized (_lockObject) {
 			
 			if (_gameRunning) {
 				_logger.warn("detectionFinished cannot be called during game is running!");
 			} else if (_carList.size() > 0) {
 				_detectionFinished = true;
 				
 				// camera detection of cars finished, connect ZigBee within a background thread and let
 				// this method return instantly so kryonet listener can continue to do its work
 				if (_worldZigbeeThread.isAlive() == false) {
 					// comment out next line for "test run"
 					_worldZigbeeThread.start();
 				}
 				_checkPreconditionsAndStartGameIfAllFine();
 			} else {
 				_logger.warn("server will not accept detection finished "
 						+ "upon at least one car ist detected!");
 			}
 		}
 	}
 	
 	public void updateCar(byte carId, float posX, float posY, float direction) {
 		// _logger.debug(
 		// "updateCar called for carId: {} game started: {} posX: {} posY: {} direction: {}",
 		// new Object[] { carId, _gameRunning, posX, posY, direction });
 		Car<Player> car = _carList.get(carId);
 		Position currentPosition = new Position(posX, posY);
 		if (_gameRunning) {
 			
 			Position nextCheckpointPosition = _checkpoints.get(carId).peekFirst();
 			if (nextCheckpointPosition != null) {
 				
 				// handle checkpoint reached first
 				if (_checkpointUtil.checkpointMatch(currentPosition, nextCheckpointPosition)) {
 					
 					if (_map != null) {
 						int cpNum = ((_checkpointStartCount - _checkpoints.get(carId).size()) + 1);
 						Checkpoint reachedCP = new Checkpoint(carId, nextCheckpointPosition,
 								_checkpointUtil.getCheckpointRadius(), cpNum);
 						
 						_logger.info(
 								"Checkpoint nr: {} car-id: {} pos: {} reached. Moderate and remove.",
 								new Object[] { cpNum, carId, nextCheckpointPosition.getPosX(),
 										nextCheckpointPosition.getPosY() });
 						_map.removeCheckpoint(reachedCP);
 					}
 					
 					long currentTime = System.currentTimeMillis();
 					int timeSinceGameStart = (int) (currentTime - _gameStartTimeInMillis);
 					_checkpoints.get(carId).pollFirst();
 					
 					UpdateGameStateMessage updateGameStateMsg = new UpdateGameStateMessage();
 					updateGameStateMsg.coinsLeft = _checkpoints.get(carId).size();
 					updateGameStateMsg.time = timeSinceGameStart;
 					Player player = car.getPlayer();
 					int playerId = -1;
 					if (player != null) {
 						player.setCoinsLeft(updateGameStateMsg.coinsLeft);
 						playerId = player.getPlayerId();
 					}
 					updateGameStateMsg.playerId = playerId;
 					_logger.info("Player: {} coins left: {}", car.getPlayer().getName(),
 							updateGameStateMsg.coinsLeft);
 					_playerServer.sendToAllTCP(updateGameStateMsg);
 					
 				}
 				
 				// handle distance and direction to next checkpoint
 				nextCheckpointPosition = _checkpoints.get(carId).peekFirst();
 				if (nextCheckpointPosition != null) {
 					
 					if (_map != null) {
 						int cpNum = ((_checkpointStartCount - _checkpoints.get(carId).size()) + 1);
 						Checkpoint nextCP = new Checkpoint(carId, nextCheckpointPosition,
 								_checkpointUtil.getCheckpointRadius(), cpNum);
 						_map.addCheckpoint(nextCP);
 					}
 					
 					float transform = -car.getDirection().getDirection();
 					Position carPosTransformed = _checkpointUtil.rotatePosition(currentPosition,
 							transform);
 					Position cpTransformed = _checkpointUtil.rotatePosition(nextCheckpointPosition,
 							transform);
 					
 					UpdateCheckPointDirectionMessage updateCheckpointDirMsg = new UpdateCheckPointDirectionMessage();
 					updateCheckpointDirMsg.posY = cpTransformed.getPosX()
 							- carPosTransformed.getPosX();
 					updateCheckpointDirMsg.posX = carPosTransformed.getPosY()
 							- cpTransformed.getPosY();
 					
 					// _logger.debug("UpdateCheckpoint direction x: {} y: {} transform: {}",
 					// updateCheckpointDirMsg.posX, updateCheckpointDirMsg.posY, transform);
 					car.getPlayer().getPlayerConnection().sendUDP(updateCheckpointDirMsg);
 					
 				} else {
 					synchronized (_lockObject) {
 						_playersInGameCount--;
 						car.getPlayer().setReady(false);
 						_logger.info(
 								"Player {} finished game. No checkpoints left. Currently {} player(s) in game.",
 								car.getPlayer().getName(), _playersInGameCount);
 						if (_playersInGameCount == 0) {
 							_gameRunning = false;
 							_resetAllPlayerReadyFlags();
 						}
 					}
 					if (!_gameRunning) {
 						PlayerMessage endMsg = new PlayerMessage(MessageId.GAME_END);
 						_playerServer.sendToAllTCP(endMsg);
 						_logger.info("Game End. Notify all players.");
 					}
 				}
 			}
 			
 		}
 		car.updatePosition(new Position(posX, posY), new Direction(direction));
 	}
 	
 	private void _resetAllPlayerReadyFlags() {
 		synchronized (_playerList) {
 			for (Player p : _playerList.values()) {
 				p.setReady(false);
 			}
 		}
 	}
 	
 	public void selectCar(PlayerConnection playerConnection, byte carId) {
 		_logger.debug("selectCar called from {}", playerConnection.getPlayer());
 		
 		SelectCarResponseMessage selectResponse = new SelectCarResponseMessage();
 		selectResponse.successfull = false;
 		
 		if (playerConnection.getPlayer().getName() == null) {
 			_logger.warn("selectCar - player has to set player name first before selecting a car!");
 		} else {
 			synchronized (_lockObject) {
 				if (_gameRunning) {
 					_logger.warn("selectCar not allowed while game is running!");
 				} else {
 					if (_carList.containsKey(carId) && _carList.get(carId).getPlayer() == null) {
 						Car<Player> carToSelect = _carList.get(carId);
 						carToSelect.setPlayer(playerConnection.getPlayer());
 						playerConnection.getPlayer().setCar(carToSelect);
 						selectResponse.successfull = true;
 						_logger.info("Player with id: {} successfully selected car with id: {}",
 								playerConnection.getPlayer().getPlayerId(), carId);
 					} else {
 						// for development purposes only
 						if (_carList.containsKey(carId) == false) {
 							_logger.warn("car with id: {} doesn't exist!", carId);
 						} else {
 							Player player = _carList.get(carId).getPlayer();
 							_logger.warn("car with id: {} allready owned by: {}", carId,
 									player.getName());
 						}
 					}
 				}
 			}
 		}
 		
 		_playerServer.sendToTCP(playerConnection.getID(), selectResponse);
 		
 		// tell new player about all other players currently connected to cars
 		_sendCurrentPlayersConnectedToPassedConnection(playerConnection);
 		
 		if (selectResponse.successfull) {
 			
 			PlayerConnectedMessage plrConnectedMsg = new PlayerConnectedMessage();
 			plrConnectedMsg.id = playerConnection.getPlayer().getPlayerId();
 			plrConnectedMsg.playerName = playerConnection.getPlayer().getName();
 			_playerServer.sendToAllExceptTCP(playerConnection.getID(), plrConnectedMsg);
 			
 			_broadcastFreeCars();
 		}
 	}
 	
 	private void _sendCurrentPlayersConnectedToPassedConnection(PlayerConnection toThisConnection) {
 		ArrayList<Integer> allreadySentPlrIds = new ArrayList<>();
 		Iterator<Entry<Integer, Player>> it = _playerList.entrySet().iterator();
 		Entry<Integer, Player> entry = null;
 		Player plr = null;
 		int plrId = -1;
 		PlayerConnectedMessage connectedMsg = new PlayerConnectedMessage();
 		while (true)
 			try {
 				while (it.hasNext()) {
 					entry = it.next();
 					plr = entry.getValue();
 					plrId = plr.getPlayerId();
 					if (plr.equals(toThisConnection.getPlayer()) == false
 							&& allreadySentPlrIds.contains(plrId) == false && plr.getCar() != null) {
 						connectedMsg.id = plrId;
 						connectedMsg.playerName = plr.getName();
 						_playerServer.sendToTCP(toThisConnection.getID(), connectedMsg);
 					}
 				}
 				break;
 			} catch (ConcurrentModificationException e) {
 				_logger.info(
 						"ConcurrentModificationException caught in sendCurrentPlayersToConnection!",
 						e);
 			}
 	}
 	
 	/**
 	 * Send currently free cars to all Players.
 	 */
 	private void _broadcastFreeCars() {
 		_logger.debug("entered _broadcastFreeCars()");
 		Iterator<Entry<Byte, Car<Player>>> it = _carList.entrySet().iterator();
 		ArrayList<Byte> freeCars = new ArrayList<Byte>();
 		Entry<Byte, Car<Player>> entry = null;
 		Car<Player> car = null;
 		try {
 			while (it.hasNext()) {
 				entry = it.next();
 				car = entry.getValue();
 				if (car.getPlayer() == null) {
 					freeCars.add(entry.getKey());
 				}
 			}
 		} catch (ConcurrentModificationException e) {
 			// _carList has changed, next broad cast will come fore sure
 			// don't care
 			_logger.warn("ConcurrentModificationException caught in broadcastFreeCars!", e);
 			return;
 		}
 		
 		FreeCarsMessage freeCarsMessage = new FreeCarsMessage();
 		byte free[] = new byte[freeCars.size()];
 		for (int i = 0; i < free.length; i++) {
 			free[i] = freeCars.get(i).byteValue();
 		}
 		_logger.debug("sending freeCarsMessage: {}", free.length);
 		freeCarsMessage.freeCars = free;
 		_playerServer.sendToAllTCP(freeCarsMessage);
 		_logger.debug("free cars sent.");
 	}
 	
 	/**
 	 * Send GameStates of all Players to all Players
 	 */
 	private void _broadcastCurrentGameStates() {
 		Iterator<Entry<Byte, ArrayDeque<Position>>> it = _checkpoints.entrySet().iterator();
 		Entry<Byte, ArrayDeque<Position>> entry = null;
 		ArrayDeque<Position> queue = null;
 		Byte carId = null;
 		int coinsLeft = -1;
 		UpdateGameStateMessage msg = new UpdateGameStateMessage();
 		try {
 			while (it.hasNext()) {
 				entry = it.next();
 				queue = entry.getValue();
 				carId = entry.getKey();
 				int playerId = -1;
 				Player player = _carList.get(carId).getPlayer();
 				if (player != null) {
 					playerId = player.getPlayerId();
 				}
 				coinsLeft = queue.size();
 				msg.playerId = playerId;
 				msg.coinsLeft = coinsLeft;
 				msg.time = 0;
 				_playerServer.sendToAllTCP(msg);
 			}
 		} catch (ConcurrentModificationException e) {
 			_logger.warn("ConcurrentModificationException caught in broadcastCurrentGameStates!", e);
 		}
 		
 	}
 	
 	private void _checkPreconditionsAndStartGameIfAllFine() {
 		
 		if (_gameRunning == false && _mapConfigured && _detectionFinished) {
 			
 			// check all cars available have a player connected (=selectCar)
 			// and this players are all in ready state (=setPlayerReady)
 			// at this state we have at least one Car in _carList
 			Iterator<Entry<Byte, Car<Player>>> it = _carList.entrySet().iterator();
 			Entry<Byte, Car<Player>> entry = null;
 			Car<Player> car = null;
 			
 			@SuppressWarnings("unchecked")
 			Car<Player> cars[] = new Car[_carList.size()];
 			int carsCount = -1;
 			try {
 				while (it.hasNext()) {
 					entry = it.next();
 					car = entry.getValue();
 					carsCount++;
 					cars[carsCount] = car;
 					if (car.getPlayer() == null) {
 						return;
 					} else if (!car.getPlayer().isReady()) {
 						return;
 					}
 				}
 			} catch (ConcurrentModificationException e) {
 				return;
 			}
 			
 			// check that as much zigBee connections are established as cars available
 			if (_worldZigbeeRunnable.getConnectionCount() != _carList.size()) {
 				_logger.info(
 						"Expected zigBeeConnection count {} currently not fulfill expectation count {} ",
 						_worldZigbeeRunnable.getConnectionCount(), _carList.size());
 				return;
 			}
 			
 			_logger.info("Game preconditions are all given.");
 			_logger.info("Generate checkpoints ....");
 			
 			Position previousCheckpoint = null;
 			for (int i = 0; i < _checkpointStartCount; i++) {
 				for (int y = 0; y < cars.length; y++) {
 					car = cars[y];
 					if (i == 0) {
 						previousCheckpoint = car.getPosition();
 					} else {
 						previousCheckpoint = _checkpoints.get(car.getCarId()).getLast();
 					}
 					_logger.debug("generate checkpoint number: {} for carId: {}", i, car.getCarId());
 					Position nextP = _checkpointUtil.generateNextCheckpoint(previousCheckpoint);
 					
 					if (_map != null && i == 0) {
 						Checkpoint nextCP = new Checkpoint(car.getCarId(), nextP,
 								_checkpointUtil.getCheckpointRadius(), i + 1);
 						_map.addCheckpoint(nextCP);
 					}
 					_checkpoints.get(car.getCarId()).addLast(nextP);
 					
 					car.getPlayer().setCoinsLeft(_checkpoints.get(car.getCarId()).size());
 				}
 			}
 			
 			_logger.info("Checkpoints generated ....");
 			_logger.info("Game will start now.");
 			
 			_gameRunning = true;
 			
 			if (_worldZigbeeThread.isAlive()) {
 				_worldZigbeeThread.interrupt();
 			}
 			
 			// broadcast initial game state
 			_broadcastCurrentGameStates();
 			
 			// TODO: good idea to send in synchronized block?
 			StartGameMessage startGameMsg = new StartGameMessage();
 			startGameMsg.gameWillStartInMilliseconds = 0;
 			_gameStartTimeInMillis = System.currentTimeMillis();
 			_playerServer.sendToAllTCP(startGameMsg);
 		}
 	}
 	
 	/**
 	 * @param playerConnection
 	 *            Socket connection of player who send request of type PlayerMessage with PlayerMessage.MessageId = DISCONNECT
 	 */
 	public void carPlayerDisconnect(PlayerConnection playerConnection) {
 		boolean playerHasBeenDecoubled = false;
 		synchronized (_lockObject) {
 			if (playerConnection.getPlayer().getCar() != null) {
 				
 				_logger.info("Decouple player from car - {}.", playerConnection.getPlayer());
 				
 				// decouple car and player instance
 				Car<?> car = playerConnection.getPlayer().getCar();
 				Player plr = playerConnection.getPlayer();
 				
 				if (plr.isInGame()) {
 					if (_gameRunning) {
 						Position nextCP = _checkpoints.get(car.getCarId()).peekFirst();
 						if (_map != null) {
 							int cpNum = ((_checkpointStartCount - _checkpoints.get(car.getCarId())
 									.size()) + 1);
 							Checkpoint cp = new Checkpoint(car.getCarId(), nextCP,
 									_checkpointUtil.getCheckpointRadius(), cpNum);
 							_map.removeCheckpoint(cp);
 							
 						}
 						_checkpoints.get(car.getCarId()).clear();
 					}
 					_playersInGameCount--;
 				}
 				
 				plr.setCar(null);
 				car.setPlayer(null);
 				playerHasBeenDecoubled = true;
 				
 				_logger.info(
 						"Player-id: {} decoupled successfully. Currently {} player(s) in game.",
 						plr.getPlayerId(), _playersInGameCount);
 				
 				// during a game in progress and this was the last player in game we have to send
 				// game over message
 				if (_gameRunning == true && _playersInGameCount == 0) {
 					_logger.info("Game over, last player-car disconnected.");
 					PlayerMessage gameOverMsg = new PlayerMessage(MessageId.GAME_END);
 					_playerServer.sendToAllTCP(gameOverMsg);
 					_resetAllPlayerReadyFlags();
 					_gameRunning = false;
 				}
 			}
 		}
 		if (playerHasBeenDecoubled) {
 			PlayerDisconnectedMessage playerDisconnectedMsg = new PlayerDisconnectedMessage();
 			playerDisconnectedMsg.playerId = playerConnection.getPlayer().getPlayerId();
 			_playerServer.sendToAllExceptTCP(playerConnection.getID(), playerDisconnectedMsg);
 			_broadcastFreeCars();
 		}
 	}
 	
 	public void setPlayerReady(PlayerConnection playerConnection) {
 		
 		synchronized (_lockObject) {
 			Player player = playerConnection.getPlayer();
 			if (player.isReady() == false && player.getCar() != null) {
 				player.setReady(true);
 				_playersInGameCount++;
 				_logger.debug("setPlayerReady -> {}", player);
 				
 				_checkPreconditionsAndStartGameIfAllFine();
 			}
 		}
 	}
 	
 	@Override
 	public void onCarClientConnect(ICarClient carClient) {
 		_awaitingCarClientReconnectSet.remove(carClient.getCarClientId());
 	}
 	
 	@Override
 	public void onCarClientDisconnect(ICarClient carClient) {
 		_awaitingCarClientReconnectSet.add(carClient.getCarClientId());
 	}
 	
 	@Override
 	public void onWorldZigbeeConnectionCountChanged(int oldValue, int newValue) {
 		synchronized (_lockObject) {
 			_checkPreconditionsAndStartGameIfAllFine();
 		}
 	}
 	
 	public void updateVelocity(PlayerConnection playerConnection, float speed, float direction) {
 		if (_detectionFinished == false) {
 			return; // suppress all user interactions until camera finished car detection
 		}
 		
 		// if at least one Car has no ZigBee-Connection available
 		// suppress all user interactions during a game is in progress
 		if (_awaitingCarClientReconnectSet.size() > 0 && _gameRunning == true) {
 			return;
 		}
 		
 		// if at least one Player connected to a Car currently in reconnection state
 		// suppress all user interactions during a game is in progress
 		if (_playerTimeoutScheduler.getPlayersToTimeout() > 0 && _gameRunning == true) {
 			return;
 		}
 		
 		ICarClient c = null;
 		try {
 			c = CarClientManager.getInstance().get(
 					playerConnection.getPlayer().getCar().getCarClientId());
 			
 		} catch (NullPointerException e) {
 			// Car can be null after network reconnect, and player has not sent it's player id yet
 			// e.g. simulator sends repeatedly updateVelocity
 		}
 		if (c != null) {
 			c.updateVelocity(speed, direction);
 		} else {
 			// _logger.warn("ICarClient for player with id: {} is null!", playerConnection.getPlayer()
 			// .getPlayerId());
 		}
 	}
 	
 	public void setMap(at.fhv.audioracer.core.model.Map map) {
 		_map = map;
 	}
 	
 	public void trim(PlayerConnection playerConnection) {
 		if (_detectionFinished == false)
 			return;
 		
 		ICarClient c = CarClientManager.getInstance().get(
 				playerConnection.getPlayer().getCar().getCarClientId());
 		if (c != null) {
 			c.trim();
 		} else {
 			_logger.debug("ICarClient for carId: {} is null!", playerConnection.getPlayer()
 					.getCar().getCarId());
 		}
 	}
 	
 	/**
 	 * Called from player on network reconnect.
 	 * 
 	 * @param playerId
 	 */
 	public void networkPlayerReconnect(PlayerConnection playerConnection, int playerId) {
 		
 		// TODO: avoid that everybody can do this
 		
 		_logger.debug(
 				"network reconnect for player id: {} stop timeout first. ---------------------",
 				playerId);
 		_playerTimeoutScheduler.stopTimeout(playerId);
 		Player oldPlrToCopy = _playerList.get(playerId);
 		if (oldPlrToCopy == null) {
 			_logger.warn("old Player for id: {} ist null!", playerId);
 		}
 		playerConnection.setPlayer(new Player(oldPlrToCopy));
 		Player copied = playerConnection.getPlayer();
 		copied.setPlayerConnection(playerConnection);
 		Car<Player> oldCar = _carList.get(copied.getCar().getCarId());
 		oldCar.setPlayer(copied);
 		
 		_playerList.put(playerId, copied);
 		
 		_logger.debug("Player info copied: {} --------------------------- ", copied);
 		
 		ReconnectRequestResponse resp = new ReconnectRequestResponse();
 		resp.reconnectSuccess = true;
 		_playerServer.sendToTCP(playerConnection.getID(), resp);
 	}
 	
 	/**
 	 * Called from player on network reconnect.
 	 * 
 	 * @param playerId
 	 */
 	public void networkPlayerDisconnect(int playerId) {
 		synchronized (_lockObject) {
 			Player p = _playerList.get(playerId);
 			if (p != null) {
 				if (p.getCar() != null) {
 					_playerTimeoutScheduler.startTimeout(playerId);
 				}
 			}
 		}
 	}
 	
 	@Override
 	public void playerTimeout(int playerId) {
 		Player p = _playerList.get(playerId);
 		_logger.debug("Timeout for {} called. Disconnect him from his car.", p);
 		carPlayerDisconnect(p.getPlayerConnection());
 	}
 }
