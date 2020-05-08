 package com.evervoid.client;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.evervoid.client.EVViewManager.ViewType;
 import com.evervoid.client.interfaces.EVGameMessageListener;
 import com.evervoid.client.interfaces.EVGlobalMessageListener;
 import com.evervoid.client.interfaces.EVLobbyMessageListener;
 import com.evervoid.client.views.game.GameView;
 import com.evervoid.client.views.lobby.LobbyView;
 import com.evervoid.json.Json;
 import com.evervoid.network.ChatMessage;
 import com.evervoid.network.EverMessage;
 import com.evervoid.network.EverMessageHandler;
 import com.evervoid.network.EverMessageListener;
 import com.evervoid.network.GameStateMessage;
 import com.evervoid.network.HandshakeMessage;
 import com.evervoid.network.JoinErrorMessage;
 import com.evervoid.network.LoadGameRequest;
 import com.evervoid.network.PlayerDefeatedMessage;
 import com.evervoid.network.PlayerVictoryMessage;
 import com.evervoid.network.RequestGameState;
 import com.evervoid.network.StartGameMessage;
 import com.evervoid.network.StartingGameMessage;
 import com.evervoid.network.TurnMessage;
 import com.evervoid.network.lobby.LobbyPlayer;
 import com.evervoid.network.lobby.LobbyPlayerUpdate;
 import com.evervoid.network.lobby.LobbyState;
 import com.evervoid.network.lobby.LobbyStateMessage;
 import com.evervoid.server.EVServerMessageObserver;
 import com.evervoid.server.EverMessageSendingException;
 import com.evervoid.server.EverVoidServer;
 import com.evervoid.state.Color;
 import com.evervoid.state.EVGameState;
 import com.evervoid.state.action.Turn;
 import com.evervoid.state.data.BadJsonInitialization;
 import com.jme3.network.connection.Client;
 
 public class EVClientEngine implements EverMessageListener
 {
 	public static final Logger sConnectionLog = Logger.getLogger(EVClientEngine.class.getName());
 	private static EVClientEngine sInstance;
 
 	public static void connect(final String pServerIP)
 	{
 		getInstance().doConnect(pServerIP);
 	}
 
 	/**
 	 * Disconnects the EVClientEngine
 	 */
 	public static void disconnect()
 	{
 		getInstance().doDisconnect();
 	}
 
 	public static EVClientEngine getInstance()
 	{
 		if (sInstance == null) {
 			sInstance = new EVClientEngine();
 		}
 		return sInstance;
 	}
 
 	public static void registerGameListener(final EVGameMessageListener listener)
 	{
 		sInstance.aGameObservers.add(listener);
 	}
 
 	public static void registerGlobalListener(final EVGlobalMessageListener listener)
 	{
 		sInstance.aGlobalObservers.add(listener);
 	}
 
 	public static void registerLobbyListener(final EVLobbyMessageListener listener)
 	{
 		sInstance.aLobbyObservers.add(listener);
 	}
 
 	public static void requestGameState(final EVGameState reference, final Runnable callback)
 	{
 		sInstance.aRequestGameStateCallback = callback;
 		try {
 			sInstance.aMessageHandler.send(new RequestGameState(reference));
 		}
 		catch (final EverMessageSendingException e) {
 			sConnectionLog.severe("Could not send lobbyplayer message.");
 		}
 	}
 
 	public static void sendChatMessage(final String message)
 	{
 		try {
 			sInstance.aMessageHandler.send(new ChatMessage(message));
 		}
 		catch (final EverMessageSendingException e) {
 			// Not really critical
 		}
 	}
 
 	public static void sendLoadGame(final File saveFile, final Runnable onFailure)
 	{
 		new Thread()
 		{
 			@Override
 			public void run()
 			{
 				EVGameState state;
 				try {
 					state = new EVGameState(Json.fromFile(saveFile));
 					sInstance.aMessageHandler.send(new LoadGameRequest(state));
 				}
 				catch (final Exception e) {
 					if (onFailure != null) {
 						EVViewManager.schedule(onFailure);
 					}
 				}
 			}
 		}.start();
 	}
 
 	public static void sendLobbyPlayer(final LobbyPlayer player)
 	{
 		try {
 			sInstance.aMessageHandler.send(new LobbyPlayerUpdate(player));
 		}
 		catch (final EverMessageSendingException e) {
 			sConnectionLog.severe("Could not send lobbyplayer message.");
 		}
 	}
 
 	public static void sendStartGame()
 	{
 		try {
 			sInstance.aMessageHandler.send(new StartGameMessage());
 		}
 		catch (final EverMessageSendingException e) {
 			sConnectionLog.severe("Could not send startgame message.");
 		}
 	}
 
 	public static void sendTurn(final Turn turn)
 	{
 		try {
 			sInstance.aMessageHandler.send(new TurnMessage(turn));
 		}
 		catch (final EverMessageSendingException e) {
 			// TODO: Show this on the UI somehow
 			sConnectionLog.severe("Could not send turn: " + turn);
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Launches a server locally.
 	 */
 	public static void startLocalServer()
 	{
 		try {
 			EverVoidServer.ensureStarted();
 			sConnectionLog.info("Local server started.");
 		}
 		catch (final Exception e) {
 			sConnectionLog.severe("Couldn't launch local server.");
 			e.printStackTrace();
 		}
 		// Sleep a bit; server takes a while to bind itself
 		try {
 			Thread.sleep(500);
 		}
 		catch (final InterruptedException e) {
 			// Like this is ever going to happen
 		}
 	}
 
 	public static void stopLocalServer()
 	{
 		EverVoidServer.stop();
 	}
 
 	private Client aClient;
 	private boolean aConnected = false;
 	private final Set<EVGameMessageListener> aGameObservers = new HashSet<EVGameMessageListener>();
 	private final Set<EVGlobalMessageListener> aGlobalObservers = new HashSet<EVGlobalMessageListener>();
 	private boolean aInLobby = false;
 	private final Set<EVLobbyMessageListener> aLobbyObservers = new HashSet<EVLobbyMessageListener>();
 	private EverMessageHandler aMessageHandler;
 	private Runnable aRequestGameStateCallback = null;
 	private String aServerIP;
 
 	public EVClientEngine()
 	{
 		sConnectionLog.setLevel(Level.ALL);
 	}
 
 	public void deregisterObserver(final EVServerMessageObserver observer)
 	{
 		aGlobalObservers.remove(observer);
 	}
 
 	private void doConnect(final String pServerIP)
 	{
 		sConnectionLog.info("Client connecting to " + pServerIP + " on ports " + EverVoidServer.sGamePortTCP + "; "
 				+ EverVoidServer.sGamePortUDP);
 		aServerIP = new String(pServerIP);
 		try {
 			aClient = new Client(aServerIP, EverVoidServer.sGamePortTCP, EverVoidServer.sGamePortUDP);
 		}
 		catch (final IOException e) {
 			sConnectionLog.severe("Could not establish connection to server. IOException caught.");
 		}
 		sConnectionLog.info("Client connected to " + pServerIP + ": " + aClient);
 		aMessageHandler = new EverMessageHandler(aClient);
 		aMessageHandler.addMessageListener(this);
 		aClient.start();
 		try {
 			Thread.sleep(500);
 		}
 		catch (final InterruptedException e1) {
 			// Like this is ever going to happen
 		}
 		sConnectionLog.info("Client started.");
 		try {
 			aMessageHandler.send(new HandshakeMessage(EverVoidClient.getSettings().getNickname()));
 		}
 		catch (final EverMessageSendingException e) {
 			sConnectionLog.severe("Could not contact server.");
 		}
 		sConnectionLog.info("Client sent handshake to server.");
 	}
 
 	private void doDisconnect()
 	{
 		if (aConnected) {
 			sConnectionLog.info("Disconnecting client.");
 			try {
 				aClient.disconnect();
 			}
 			catch (final Exception e) {
 				sConnectionLog.severe("Failed to disconnect client!");
 			}
 			aConnected = false;
 			aInLobby = false;
 		}
 	}
 
 	/**
 	 * Called on the next frame after receiving a message, in the UI thread.
 	 * 
 	 * @param message
 	 *            The message that was received
 	 */
 	private void guiMessageReceived(final EverMessage message)
 	{
 		final String messageType = message.getType();
 		final Json messageContents = message.getJson();
 		if (messageType.equals(GameStateMessage.class.getName())) {
 			try {
 				if (!messageContents.isNull()) {
 					for (final EVGlobalMessageListener observer : aGlobalObservers) {
 						observer.receivedGameState(new EVGameState(messageContents));
 					}
 				}
 				if (aRequestGameStateCallback != null) {
 					aRequestGameStateCallback.run();
 					aRequestGameStateCallback = null;
 				}
 			}
 			catch (final BadJsonInitialization e) {
 				// we got a bad state from the Server, not a very good sign
 				// TODO - warn the server
 				Logger.getLogger(EverVoidClient.class.getName()).log(Level.SEVERE, "The Server has sent a bad game state", e);
 			}
 		}
 		else if (messageType.equals(LobbyStateMessage.class.getName())) {
 			aConnected = true;
 			LobbyState lobbyState = null;
 			try {
 				lobbyState = new LobbyState(messageContents);
 			}
 			catch (final BadJsonInitialization e) {
 				// we got a bad state from the Server, not a very good sign
 				// TODO - warn the server
 				Logger.getLogger(EverVoidClient.class.getName()).log(Level.SEVERE, "The Server has sent bad Lobby State", e);
 			}
 			if (!aInLobby) {
 				aInLobby = true;
 				EVViewManager.registerView(ViewType.LOBBY, new LobbyView(lobbyState));
 				EVViewManager.switchTo(ViewType.LOBBY);
 			}
 			for (final EVLobbyMessageListener observer : aLobbyObservers) {
 				observer.receivedLobbyData(lobbyState);
 			}
 		}
 		else if (messageType.equals(JoinErrorMessage.class.getName())) {
 			EVViewManager.displayError(messageContents.getString());
 		}
 		else if (messageType.equals(ChatMessage.class.getName())) {
 			for (final EVGlobalMessageListener observer : aGlobalObservers) {
 				observer.receivedChat(messageContents.getStringAttribute("player"),
 						new Color(messageContents.getAttribute("color")), messageContents.getStringAttribute("message"));
 			}
 		}
 		else if (messageType.equals(StartingGameMessage.class.getName())) {
 			EVViewManager.switchTo(ViewType.LOADING);
 		}
 		else if (messageType.equals(TurnMessage.class.getName())) {
 			for (final EVGameMessageListener observer : aGameObservers) {
 				observer.receivedTurn(new Turn(messageContents, GameView.getGameState()));
 			}
 		}
 		else if (messageType.equals(PlayerDefeatedMessage.class.getName())) {
 			for (final EVGameMessageListener observer : aGameObservers) {
 				observer.playerLost(GameView.getGameState().getPlayerByName(messageContents.getString()));
 			}
 		}
 		else if (messageType.equals(PlayerVictoryMessage.class.getName())) {
 			for (final EVGameMessageListener observer : aGameObservers) {
 				observer.playerWon(GameView.getGameState().getPlayerByName(messageContents.getString()));
 			}
 		}
 	}
 
 	public boolean isConnected()
 	{
 		return aConnected;
 	}
 
 	@Override
 	public void messageReceived(final EverMessage message)
 	{
 		sConnectionLog.info("Client received: " + message + " | " + message.getJson().toPrettyString());
 		EVViewManager.schedule(new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				guiMessageReceived(message);
 			}
 		});
 	}
 }
