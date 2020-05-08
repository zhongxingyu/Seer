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
 
 public class LobbyManager {
 	private NetworkPlayer localPlayer;
 	private ArrayList<AvailableGame> availableGames;
 	private Client client;
 	
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
 
 	public void setPlayerName(String name) {
 		localPlayer.setUsername(name);
 	}
 
 	public void hostNewGame(AvailableGame game) {
 		hostedGame = game;
 		game.setHostName(localPlayer.getUsername());
 		createServer();
 	}
 	
 	public void stopHostingGame() {
 		hostedGame = null;
 		shutdownServer();
 	}
 	
 	public void quit() {
 		GameNegotiationMessage quitMessage = new GameNegotiationMessage();
 		quitMessage.type = GameNegotiationMessage.Type.QUIT_GAME;
 		
 		if (client.isConnected()) {
 			client.sendTCP(quitMessage);
 		}
 		
 		if (server != null) {
 			server.sendToAllTCP(quitMessage);
 			stopHostingGame();
 		}
 	}
 	
 	private void createServer() {
 		server = new Server(NetworkConstants.bufferSize, NetworkConstants.bufferSize);
 		NetworkConstants.registerKryoClasses(server.getKryo());
 		server.start();
 		try {
 			server.bind(NetworkConstants.tcpPort, NetworkConstants.udpPort);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		initializeServerListener();
 	}
 
 	private void shutdownServer() {
 		if (server != null) {
 			boot();
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
 							controller.playerAttemptedToJoin((String) m.data);
 							break;
 						case QUIT_GAME:
 							connection.close();
 							controller.opponentDisconnected();
 							break;
 					}
 				}
 			}
 
 			public void disconnected(Connection c) {
 				controller.opponentDisconnected();
 			}
 		});
 	}
 	
 	public void boot() {
 		GameNegotiationMessage response = new GameNegotiationMessage();
 		response.type = GameNegotiationMessage.Type.ATTEMPT_TO_JOIN_RESPONSE;
 		response.data = null;
 		
 		server.sendToAllTCP(response);
 	}
 	
 	public NetworkGame acceptPlayer() {
 		GameNegotiationMessage response = new GameNegotiationMessage();
 		response.type = GameNegotiationMessage.Type.ATTEMPT_TO_JOIN_RESPONSE;
 		response.data = hostedGame.getMapName();
 		
 		NetworkGame ng = new NetworkGame(server.getConnections()[0]);
 		ng.setMap(Map.getMapByName(hostedGame.getMapName()));
 		
 		server.sendToAllTCP(response);
 		
 		return ng;
 	}
 	
 	private void initializeClientListener() {
 		client.addListener(new Listener() {
 			public void received(Connection connection, Object object) {
 				if (object instanceof GameNegotiationMessage) {
 					GameNegotiationMessage m = (GameNegotiationMessage)object;
 					
 					switch (m.type) {
 						case GAME_DISCOVER_RESPONSE:
 							AvailableGame ag = (AvailableGame) m.data;
 							availableGames.add(ag);
 							
 							synchronized (client) {
 								client.close();
 							}
 							
 							break;
 						case ATTEMPT_TO_JOIN_RESPONSE:
 							String mapName = (String) m.data;
 							
 							if (mapName == null) {
 								System.out.println("You've been kicked");
 								client.close();
 							} else {
 								NetworkGame game = new NetworkGame(connection);
 								game.setMap(Map.getMapByName(mapName));
 								controller.startNetworkGame(game);
 							}
 						case QUIT_GAME:
 							client.close();
 							controller.opponentDisconnected();
 					}
 				}
 			}
 		});
 	}
 	
 	public void refreshGameList() {
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
