 package src.net;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 
 import src.core.Map;
 import src.ui.controller.MultiplayerController;
 
 import com.esotericsoftware.kryonet.Client;
 import com.esotericsoftware.kryonet.Connection;
 import com.esotericsoftware.kryonet.Listener;
 import com.esotericsoftware.kryonet.Server;
 
 /**
  * Handles all network communications related to setting up / managing a game.
  * Is capable of producing NetworkGame objects, which can then be played.
  */
 public class LobbyManager {
 	private NetworkPlayer localPlayer;
 	private ArrayList<AvailableGame> availableGames;
 	private Client client;
 	
 	// if we're currently playing with someone this is the connection over which we're doing it
 	private Connection opponentConnection;
 	
 	private AvailableGame hostedGame;
 	private Server server;
 	
 	private MultiplayerController controller;
 	
 	public LobbyManager(MultiplayerController multiController) {
 		this.controller = multiController;
 		
 		localPlayer = new NetworkPlayer();
 		availableGames = new ArrayList<AvailableGame>();
 		
 		client = new Client(NetworkConstants.bufferSize, NetworkConstants.bufferSize);
 		NetworkConstants.registerKryoClasses(client.getKryo());
 		client.start();
 		
 		initializeClientListener();
 	}
 
 	// Set the public name of our player (the local player)
 	public void setPlayerName(String name) {
 		localPlayer.setUsername(name);
 	}
 	
 	public void quit() {
 		GameNegotiationMessage quitMessage = new GameNegotiationMessage();
 		quitMessage.type = GameNegotiationMessage.Type.QUIT_GAME;
 		
 		if (client.isConnected()) {
 			client.sendTCP(quitMessage);
 		}
 		
 		if (server != null) {
 			opponentConnection.sendTCP(quitMessage);
 			stopHostingGame();
 		}
 
 		opponentConnection = null;
 	}
 	
 	/*
 	 * Methods related to functioning as a host
 	 */
 	public void hostNewGame(AvailableGame game) {
 		hostedGame = game;
 		game.setHostName(localPlayer.getUsername());
 		createServer();
 	}
 	
 	public void stopHostingGame() {
 		shutdownServer();
 		hostedGame = null;
 	}
 	
 	private void createServer() {
 		server = new Server(NetworkConstants.bufferSize, NetworkConstants.bufferSize);
 		NetworkConstants.registerKryoClasses(server.getKryo());
 		server.start();
 		try {
 			server.bind(NetworkConstants.tcpPort, NetworkConstants.udpPort);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		initializeServerListener();
 	}
 
 	private void shutdownServer() {
 		if (server != null) {
 			boot(opponentConnection);
 			server.close();
 			server = null;
 		}
 	}
 
 	private void initializeServerListener() {
 		server.addListener(new Listener() {
 			public void received(Connection connection, Object object) {
 				if (object instanceof GameNegotiationMessage) {
 					GameNegotiationMessage m = (GameNegotiationMessage)object;
 					GameNegotiationMessage response = new GameNegotiationMessage();
 
 					switch (m.type) {
 						case GAME_DISCOVER:
 							if (opponentConnection != null) {
 								response.type = GameNegotiationMessage.Type.GAME_DISCOVER_RESPONSE;
 								response.data = null;
 								
 								connection.sendTCP(response);
 								break;
 							}
 							
 							try {
 								hostedGame.setHostAddress(InetAddress.getLocalHost().getCanonicalHostName());
 							} catch (UnknownHostException e) {
 								// pretty sure localhost always exists
 							}
 							
 							response.type = GameNegotiationMessage.Type.GAME_DISCOVER_RESPONSE;
 							response.data = hostedGame;
 							
 							connection.sendTCP(response);
 							break;
 						case ATTEMPT_TO_JOIN:
 							if (opponentConnection == null) {
 								opponentConnection = connection;
 							} else {
 								boot(connection);
 								break;
 							}
 								
 							controller.playerAttemptedToJoin((String) m.data);
 							break;
 						case QUIT_GAME:
 							controller.opponentDisconnected();
 							break;
 					}
 				}
 			}
 
 			public void disconnected(Connection c) {
 				if (opponentConnection != null && c.getID() == opponentConnection.getID())
 					controller.opponentDisconnected();
 			}
 		});
 	}
 	
 	public void resetOpponentConnection() {
 		opponentConnection = null;
 	}
 	
 	private void boot(Connection c) {
 		if (c == null) return;
 		if (c.getID() == opponentConnection.getID()) resetOpponentConnection();
 		
 		GameNegotiationMessage response = new GameNegotiationMessage();
 		response.type = GameNegotiationMessage.Type.ATTEMPT_TO_JOIN_RESPONSE;
 		response.data = null;
 		
 		c.sendTCP(response);
 	}
 	
 	public void boot() {
 		boot(opponentConnection);
 	}
 	
 	public NetworkGame acceptPlayer() {
 		GameNegotiationMessage response = new GameNegotiationMessage();
 		response.type = GameNegotiationMessage.Type.ATTEMPT_TO_JOIN_RESPONSE;
 		response.data = hostedGame.getMapName();
 		
 		NetworkGame ng = new NetworkGame(server.getConnections()[0]);
 		ng.setMap(Map.getMapByName(hostedGame.getMapName()));
 		
 		opponentConnection = server.getConnections()[0];
 		opponentConnection.sendTCP(response);
 		
 		hostedGame = null;
 		
 		return ng;
 	}
 	
 	/*
 	 * Methods related to functioning as a client
 	 */
 	private void initializeClientListener() {
 		client.addListener(new Listener() {
 			public void received(Connection connection, Object object) {
 				if (object instanceof GameNegotiationMessage) {
 					GameNegotiationMessage m = (GameNegotiationMessage)object;
 					
 					switch (m.type) {
 						case GAME_DISCOVER_RESPONSE:
 							AvailableGame ag = (AvailableGame) m.data;
 							
 							if (ag != null)
 								availableGames.add(ag);
 							
 							synchronized (client) {
 								client.close();
 							}
 							
 							break;
 						case ATTEMPT_TO_JOIN_RESPONSE:
 							String mapName = (String) m.data;
 							
 							if (mapName == null) {
 								controller.wasBootedFromGame();
 							} else {
 								opponentConnection = connection;
 								NetworkGame game = new NetworkGame(connection);
 								game.setMap(Map.getMapByName(mapName));
 								controller.startNetworkGame(game);
 							}
 							break;
 						case QUIT_GAME:
 							controller.opponentDisconnected();
 					}
 				}
 			}
 			
 			public void disconnected(Connection c) {
 				if (opponentConnection != null && c.getID() == opponentConnection.getID())
 					controller.opponentDisconnected();
 			}
 		});
 	}
 	
 	public void refreshGameList() {
 		synchronized (availableGames) {
 			availableGames.clear();
 
 			for (InetAddress addr : SunlabAutodiscoverHack.getSunlabAddresses()) {
 				try {
 					client.connect(20, addr, NetworkConstants.tcpPort, NetworkConstants.udpPort);
 
 					// send query to server
 					GameNegotiationMessage discoveryMessage = new GameNegotiationMessage();
 					discoveryMessage.type = GameNegotiationMessage.Type.GAME_DISCOVER;
 					client.sendTCP(discoveryMessage);
 
 					while (true) {
 						synchronized (client) {
 							if (!client.isConnected()) break;
 						}
 					}
 				} catch (IOException e) {
 					// There is no server at this address, ignore
 				}
 			}
 		}
 	}
 	
 	public void joinGame(AvailableGame ag) {
 		try {
 			client.connect(NetworkConstants.gameConnectTimeout, 
 						   InetAddress.getByName(ag.getHostAddress()), 
 						   NetworkConstants.tcpPort, 
 						   NetworkConstants.udpPort);
 		} catch (UnknownHostException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		controller.waitToJoinGame();
 		
 		GameNegotiationMessage joinMessage = new GameNegotiationMessage();
 		joinMessage.type = GameNegotiationMessage.Type.ATTEMPT_TO_JOIN;
 		joinMessage.data = localPlayer.getUsername();
 
 		client.sendTCP(joinMessage);
 	}
 	
 	public ArrayList<AvailableGame> getAvailableGames(){
 		return availableGames;
 	}
 
 	public Server getServer() {
 		return server;
 	}
 
 	public AvailableGame getHostedGame() {
 		return hostedGame;
 	}
 }
