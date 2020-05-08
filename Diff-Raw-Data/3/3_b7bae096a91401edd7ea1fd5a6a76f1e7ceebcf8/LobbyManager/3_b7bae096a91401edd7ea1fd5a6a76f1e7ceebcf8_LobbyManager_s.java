 package src.net;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 import src.core.Map;
 import src.ui.controller.MultiplayerController;
 
 import com.esotericsoftware.kryonet.Client;
 import com.esotericsoftware.kryonet.Connection;
 import com.esotericsoftware.kryonet.Listener;
 import com.esotericsoftware.kryonet.Server;
 
 public class LobbyManager {
 	private NetworkPlayer localPlayer;
 	private ArrayList<AvailableGame> availableGames;
 	private ArrayList<InetAddress> testAddresses; // part of hack, see below
 	private Client client;
 	
 	private AvailableGame hostedGame;
 	private Server server;
 	
 	private MultiplayerController controller;
 	
 	public LobbyManager(MultiplayerController multiController) {
 		this.controller = multiController;
 		
 		localPlayer = new NetworkPlayer();
 		
 		// hack for testing, make a list of hosts to test
 		testAddresses = new ArrayList<InetAddress>();
 		
 		try {
 			testAddresses.add(InetAddress.getByName("cslab8h"));
 			//testAddresses.add(InetAddress.getByName("cslab8f"));
 		} catch (UnknownHostException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		availableGames = new ArrayList<AvailableGame>();
 		
 		client = new Client();
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
 	
 	private void createServer() {
 		server = new Server();
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
 		boot();
 		server.close();
 		server = null;
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
 					}
 				}
 			}
 		});
 	}
 	
 	public void boot() {
 		GameNegotiationMessage response = new GameNegotiationMessage();
 		response.type = GameNegotiationMessage.Type.ATTEMPT_TO_JOIN_RESPONSE;
 		response.data = false;
 		
 		server.sendToAllTCP(response);
 	}
 	
 	public NetworkGame acceptPlayer() {
 		GameNegotiationMessage response = new GameNegotiationMessage();
 		response.type = GameNegotiationMessage.Type.ATTEMPT_TO_JOIN_RESPONSE;
 		response.data = true;
 		
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
 							System.out.println("response");
 							boolean booted = !((Boolean) m.data);
 							
 							if (booted) {
 								System.out.println("You've been kicked");
 								client.close();
 							} else {
								controller.joinAccepted();
 							}
 					}
 				}
 			}
 		});
 	}
 	
 	public void refreshGameList() {
 		availableGames.clear();
 		
 		for (InetAddress addr : testAddresses) {
 			try {
 				client.connect(NetworkConstants.gameDisoveryTimeout, addr, NetworkConstants.tcpPort, NetworkConstants.udpPort);
 				
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
 }
