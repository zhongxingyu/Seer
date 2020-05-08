 package com.evervoid.server;
 
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.evervoid.json.Json;
 import com.evervoid.network.ChatMessage;
 import com.evervoid.network.EverMessage;
 import com.evervoid.network.EverMessageHandler;
 import com.evervoid.network.EverMessageListener;
 import com.evervoid.network.ServerChatMessage;
 import com.evervoid.network.ServerInfoMessage;
 import com.evervoid.network.StartingGameMessage;
 import com.evervoid.network.lobby.LobbyPlayer;
 import com.evervoid.network.lobby.LobbyState;
 import com.evervoid.network.lobby.LobbyStateMessage;
 import com.evervoid.state.data.GameData;
 import com.jme3.network.connection.Client;
 import com.jme3.network.connection.Server;
 import com.jme3.network.events.ConnectionListener;
 
 /**
  * everVoid Server allowing communication from and to clients.
  */
 public class EVServerEngine implements ConnectionListener, EverMessageListener
 {
 	private static EVServerEngine sInstance = null;
 	public static final Logger sServerLog = Logger.getLogger(EVServerEngine.class.getName());
 	private static String[] sValidLobbyMessages = { "requestserverinfo", "handshake", "lobbyplayer", "startgame" };
 
 	public static EVServerEngine getInstance()
 	{
 		if (sInstance == null) {
 			sInstance = new EVServerEngine();
 		}
 		return sInstance;
 	}
 
 	private final EverMessageHandler aDiscoveryMessageHandler;
 	private Server aDiscoveryServer;
 	public final Set<EVServerMessageObserver> aGameMessagesObservers;
 	private boolean aInGame = false;
 	private final LobbyState aLobby;
 	private final EverMessageHandler aMessageHandler;
 	private Server aSpiderMonkeyServer;
 	private final int aTCPport;
 	private final int aUDPport;
 
 	/**
 	 * Constructor for the EverVoidServer using default ports.
 	 */
 	public EVServerEngine()
 	{
 		this(51255, 51255);
 	}
 
 	/**
 	 * Constructor with specified UDP and TCP ports.
 	 * 
 	 * @param pTCPport
 	 *            TCP port to use.
 	 * @param pUDPport
 	 *            UDP port to use.
 	 */
 	public EVServerEngine(final int pTCPport, final int pUDPport)
 	{
 		sInstance = this;
 		aGameMessagesObservers = new HashSet<EVServerMessageObserver>();
 		// The game data is loaded from the default JSON file here; might want to load it from the real game state, but they
 		// should match anyway
 		aLobby = new LobbyState(new GameData(), "My cool everVoid server");
 		sServerLog.setLevel(Level.ALL);
 		sServerLog.info("Creating server on ports " + pTCPport + "; " + pUDPport);
 		aTCPport = pTCPport;
 		aUDPport = pUDPport;
 		try {
 			aSpiderMonkeyServer = new Server(aTCPport, aUDPport);
 		}
 		catch (final IOException e) {
 			sServerLog.severe("Could not initialise the server. Caught IOException.");
 		}
 		try {
 			aDiscoveryServer = new Server(51256, 51256);
 		}
 		catch (final IOException e) {
 			sServerLog.warning("Could not initialise discovery server. Caught IOException.");
 			// No big deal, just won't be discovery
 		}
 		sServerLog.info("Server created: " + aSpiderMonkeyServer);
 		aMessageHandler = new EverMessageHandler(aSpiderMonkeyServer);
 		aMessageHandler.addMessageListener(this);
 		aSpiderMonkeyServer.addConnectionListener(this);
 		aDiscoveryMessageHandler = new EverMessageHandler(aDiscoveryServer);
 		aDiscoveryMessageHandler.addMessageListener(this);
 		sServerLog.info("Set connection listener and message listener, initializing game engine.");
 		new EVGameEngine(this); // Will register itself (bad?)
 		try {
 			aSpiderMonkeyServer.start();
 		}
 		catch (final IOException e) {
 			sServerLog.info("Cannot start server: " + e.getStackTrace());
 		}
 		try {
 			aDiscoveryServer.start();
 		}
 		catch (final IOException e) {
 			sServerLog.info("Cannot start discovery server: " + e.getStackTrace());
 		}
 		sServerLog.info("Server up and waiting for connections.");
 	}
 
 	@Override
 	public void clientConnected(final Client client)
 	{
 		sServerLog.info("Client connected: " + client);
 	}
 
 	@Override
 	public void clientDisconnected(final Client client)
 	{
 		sServerLog.info("Client disconnected: " + client);
 		aLobby.removePlayer(client);
 		refreshLobbies();
 	}
 
 	public void deregisterObserver(final EVServerMessageObserver observer)
 	{
 		aGameMessagesObservers.remove(observer);
 	}
 
 	/**
 	 * Handles an EverMessage
 	 * 
 	 * @param message
 	 *            An EverMessage; can be a lobby or a non-lobby one
 	 * @return True if the message is a lobby one and was handled, false otherwise
 	 */
 	private boolean handleLobbyMessage(final EverMessage message)
 	{
 		final String messageType = message.getType();
 		// Handle only lobby messages
 		boolean isLobby = false;
 		for (final String s : sValidLobbyMessages) {
 			if (s.equals(messageType)) {
 				isLobby = true;
 			}
 		}
 		if (!isLobby) {
 			return false;
 		}
 		final Json content = message.getJson();
 		if (messageType.equals("requestserverinfo")) {
 			try {
 				aDiscoveryMessageHandler.send(message.getClient(), new ServerInfoMessage(aLobby, aInGame));
 			}
 			catch (final EverMessageSendingException e) {
 				// No big deal, client just won't see us in server list
 			}
 			aLobby.removePlayer(message.getClient()); // If it was in the lobby somehow, remove it
 			return true;
 		}
 		if (messageType.equals("handshake")) {
 			if (aLobby.getPlayerByClient(message.getClient()) != null || aInGame) {
 				// Some guy is trying to handshake twice or handshaking in-game -> DENIED
 				return true;
 			}
 			final String nickname = content.getStringAttribute("nickname");
 			sServerLog.info("Adding player " + nickname + " at Client " + message.getClient() + " to lobby.");
 			aLobby.addPlayer(message.getClient(), nickname);
 			refreshLobbies();
 			return true;
 		}
 		// It's not a handshake, so it must be a legit lobby message from this point on
 		if (aInGame || aLobby.getPlayerByClient(message.getClient()) == null) {
 			return true; // We're in-game or client is not authenticated
 		}
 		if (messageType.equals("lobbyplayer")) {
 			if (aLobby.updatePlayer(message.getClient(), content)) {
 				refreshLobbies();
 			}
 		}
 		else if (messageType.equals("startgame")) {
 			if (!readyToStart()) {
 				send(message.getClient(), new ServerChatMessage("Cannot start game yet, some players are not ready."));
 			}
 			else {
 				// Starting game! Build list of lobby players and pass it to game observers
 				sendAll(new ServerChatMessage("Game starting."));
 				sendAll(new StartingGameMessage());
 				aInGame = true;
 				final Json players = aLobby.toJson();
 				for (final EVServerMessageObserver observer : aGameMessagesObservers) {
 					observer.messageReceived("startgame", message.getClient(), players);
 				}
 			}
 		}
 		return true;
 	}
 
 	@Override
 	public void messageReceived(final EverMessage message)
 	{
 		final String messageType = message.getType();
 		final Json messageContents = message.getJson();
 		// Handle global messages first
 		if (messageType.equals("chat")) {
 			final LobbyPlayer fromPlayer = aLobby.getPlayerByClient(message.getClient());
 			sendAll(new ChatMessage(fromPlayer.getNickname(), fromPlayer.getColor(),
 					messageContents.getStringAttribute("message")));
 			return;
 		}
 		// Else, handle lobby messages
 		if (handleLobbyMessage(message)) {
 			// If it's a lobby message, intercept it and don't send it to the observers
 			return;
 		}
 		// Else, it's a game message, so forward to game observers
 		for (final EVServerMessageObserver observer : aGameMessagesObservers) {
 			observer.messageReceived(messageType, message.getClient(), messageContents);
 		}
 	}
 
 	/**
 	 * @return Whether we can start the game right now
 	 */
 	private boolean readyToStart()
 	{
 		for (final LobbyPlayer player : aLobby.getPlayers()) {
 			if (!player.isReady()) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Send a message to all clients containing the current lobby info
 	 */
 	private void refreshLobbies()
 	{
 		sendAll(new LobbyStateMessage(aLobby));
 	}
 
 	public void registerListener(final EVServerMessageObserver listener)
 	{
 		aGameMessagesObservers.add(listener);
 	}
 
 	public void send(final Client client, final EverMessage message)
 	{
 		try {
 			if (!aMessageHandler.send(client, message)) {
 				sServerLog.severe("Could not send message " + message + " to client " + client);
 			}
 		}
 		catch (final EverMessageSendingException e) {
 			aLobby.removePlayer(e.getClient());
 			refreshLobbies();
 		}
 	}
 
 	public void sendAll(final EverMessage message)
 	{
 		for (final LobbyPlayer player : aLobby) {
 			send(player.getClient(), message);
 		}
 	}
 
 	public void stop()
 	{
 		try {
 			aSpiderMonkeyServer.stop();
 		}
		catch (final IOException e) {
			sServerLog.severe("Could not stop the server. Caught IOException.");
 		}
 		try {
 			aDiscoveryServer.stop();
 		}
		catch (final IOException e) {
			sServerLog.warning("Could not stop discovery server. Caught IOException.");
 		}
 	}
 }
