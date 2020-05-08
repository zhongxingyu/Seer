 package com.evervoid.server;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.evervoid.json.Json;
 import com.evervoid.network.ChatMessage;
 import com.evervoid.network.EverMessage;
 import com.evervoid.network.EverMessageHandler;
 import com.evervoid.network.EverMessageListener;
 import com.evervoid.network.HandshakeMessage;
 import com.evervoid.network.JoinErrorMessage;
 import com.evervoid.network.LoadGameRequest;
 import com.evervoid.network.PingMessage;
 import com.evervoid.network.RequestServerInfo;
 import com.evervoid.network.ServerChatMessage;
 import com.evervoid.network.ServerInfoMessage;
 import com.evervoid.network.ServerQuit;
 import com.evervoid.network.StartGameMessage;
 import com.evervoid.network.StartingGameMessage;
 import com.evervoid.network.lobby.LobbyPlayer;
 import com.evervoid.network.lobby.LobbyPlayerUpdate;
 import com.evervoid.network.lobby.LobbyState;
 import com.evervoid.network.lobby.LobbyStateMessage;
 import com.evervoid.state.BadSaveFileException;
 import com.evervoid.state.EVGameState;
 import com.evervoid.state.data.BadJsonInitialization;
 import com.evervoid.state.data.GameData;
 import com.evervoid.state.player.Player;
 import com.jme3.network.connection.Client;
 import com.jme3.network.connection.Server;
 import com.jme3.network.events.ConnectionListener;
 
 /**
  * everVoid Server allowing communication from and to clients.
  */
 public class EVServerEngine implements ConnectionListener, EverMessageListener
 {
 	public static final int sPingTime = 15000;
 	public static final Logger sServerLog = Logger.getLogger(EVServerEngine.class.getName());
 	private static String[] sValidLobbyMessages = { RequestServerInfo.class.getName(), HandshakeMessage.class.getName(),
 			LobbyPlayerUpdate.class.getName(), StartGameMessage.class.getName(), LoadGameRequest.class.getName() };
 	private EverMessageHandler aDiscoveryMessageHandler;
 	private Server aDiscoveryServer;
 	private final Set<EVServerMessageObserver> aGameMessagesObservers;
 	private boolean aInGame = false;
 	private LobbyState aLobby;
 	private long aMaxPingTime;
 	private final EverMessageHandler aMessageHandler;
 	private Timer aPingTimer;
 	private Server aSpiderMonkeyServer;
 
 	/**
 	 * Constructor for the EverVoidServer using default ports.
 	 */
 	public EVServerEngine()
 	{
 		aGameMessagesObservers = new HashSet<EVServerMessageObserver>();
 		// The game data is loaded from the default JSON file here; might want to load it from the real game state, but they
 		// should match anyway
 		try {
 			aLobby = new LobbyState(new GameData(), "My cool everVoid server");
 		}
 		catch (final BadJsonInitialization e2) {
 			sServerLog.info("Cannot read game data: " + e2.getStackTrace());
 			aLobby = null;
 		}
 		sServerLog.setLevel(Level.ALL);
 		sServerLog.info("Creating server on ports " + EverVoidServer.sDiscoveryPortTCP + "; "
 				+ EverVoidServer.sDiscoveryPortUDP);
 		try {
 			aSpiderMonkeyServer = new Server(EverVoidServer.sGamePortTCP, EverVoidServer.sGamePortUDP);
 		}
 		catch (final IOException e) {
 			sServerLog.severe("Could not initialise the server. Caught IOException.");
 		}
 		try {
 			aDiscoveryServer = new Server(EverVoidServer.sDiscoveryPortTCP, EverVoidServer.sDiscoveryPortUDP);
 			aDiscoveryMessageHandler = new EverMessageHandler(aDiscoveryServer);
 			aDiscoveryMessageHandler.addMessageListener(this);
 		}
 		catch (final IOException e) {
 			sServerLog.warning("Could not initialise discovery server. Caught IOException.");
 			// No big deal, just won't be available for discovery
 		}
 		sServerLog.info("Server created: " + aSpiderMonkeyServer);
 		aMessageHandler = new EverMessageHandler(aSpiderMonkeyServer);
 		aMessageHandler.addMessageListener(this);
 		aSpiderMonkeyServer.addConnectionListener(this);
 		sServerLog.info("Set connection listener and message listener, initializing game engine.");
 		try {
 			new EVGameEngine(this); // Will register itself (bad?)
 		}
 		catch (final BadJsonInitialization e1) {
 			sServerLog.info("Cannot start game engine: " + e1.getStackTrace());
 		}
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
 		if (isGameRunning()) {
 			for (final EVServerMessageObserver listener : aGameMessagesObservers) {
 				listener.clientQuit(client);
 			}
 		}
 		else {
 			refreshLobbies();
 		}
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
 		if (messageType.equals(RequestServerInfo.class.getName())) {
 			try {
 				aDiscoveryMessageHandler.send(message.getClient(), new ServerInfoMessage(aLobby, aInGame));
 			}
 			catch (final EverMessageSendingException e) {
 				// No big deal, client just won't see us in server list
 			}
 			aLobby.removePlayer(message.getClient()); // If it was in the lobby somehow, remove it
 			return true;
 		}
 		if (messageType.equals(HandshakeMessage.class.getName())) {
 			if (aLobby.getPlayerByClient(message.getClient()) != null || aInGame) {
 				// Some guy is trying to handshake twice or handshaking in-game -> DENIED
 				return true;
 			}
 			String nickname = content.getStringAttribute("nickname");
 			if (nickname.equals(EVGameState.sNeutralPlayerName)) {
 				send(message.getClient(), new JoinErrorMessage("Nickname \"" + EVGameState.sNeutralPlayerName
 						+ "\" is reserved."));
 				return true;
 			}
 			for (final LobbyPlayer p : aLobby) {
 				if (p.getNickname().equalsIgnoreCase(nickname)) {
 					nickname = "Player " + aLobby.getNumOfPlayers();
 				}
 			}
 			sServerLog.info("Adding player " + nickname + " at Client " + message.getClient() + " to lobby.");
 			aLobby.addPlayer(message.getClient(), nickname);
 			refreshLobbies();
 			return true;
 		}
 		// It's not a handshake, so it must be a legit lobby message from this point on
 		if (aInGame || aLobby.getPlayerByClient(message.getClient()) == null) {
 			return true; // We're in-game or client is not authenticated
 		}
 		if (messageType.equals(LobbyPlayerUpdate.class.getName())) {
 			if (aLobby.updatePlayer(message.getClient(), content)) {
 				refreshLobbies();
 			}
 		}
 		else if (messageType.equals(StartGameMessage.class.getName())) {
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
 					observer.messageReceived(messageType, aLobby, message.getClient(), players);
 				}
 				// ping all to get a feel for ping times
 				pingAll();
 			}
 		}
 		else if (messageType.equals(LoadGameRequest.class.getName())) {
 			EVGameState loaded;
 			sServerLog.info("Attempting to load game from Client " + message.getClient() + ".");
 			try {
 				loaded = loadGame(content);
 				// Start game, no errors
 				sendAll(new ServerChatMessage("Loaded game starting."));
 				sendAll(new StartingGameMessage());
 				aInGame = true;
 				sServerLog.info("Successfully loaded game from Client " + message.getClient() + ".");
 				for (final EVServerMessageObserver observer : aGameMessagesObservers) {
 					observer.messageReceived(messageType, aLobby, message.getClient(), loaded.toJson());
 				}
 			}
 			catch (final BadSaveFileException e) {
 				sServerLog.info("Eror while loading game from Client " + message.getClient() + ": " + e.getMessage());
 				sendAll(new ServerChatMessage("Error while loading game: " + e.getMessage()));
 			}
 		}
 		return true;
 	}
 
 	public boolean isGameRunning()
 	{
 		return aInGame;
 	}
 
 	/**
 	 * Attempt to load a game from a save file
 	 * 
 	 * @param state
 	 *            The Json'd game state to load
 	 * @return The loaded game state
 	 * @throws BadSaveFileException
 	 *             When an error happens during loading.
 	 */
 	private EVGameState loadGame(final Json state) throws BadSaveFileException
 	{
 		EVGameState loadedState;
 		try {
 			loadedState = new EVGameState(state);
 		}
 		catch (final BadJsonInitialization e) {
 			throw new BadSaveFileException("Invalid save file.");
 		}
 		// First, match all players; don't modify them yet until we're sure we have everyone
 		String missingPlayers = "";
 		int missingCount = 0;
 		for (final Player p : loadedState.getPlayers()) {
 			if (p.equals(loadedState.getNullPlayer())) {
 				continue;
 			}
 			final LobbyPlayer lobbyP = aLobby.getPlayerByNickname(p.getNickname());
 			if (lobbyP == null) {
 				missingPlayers += ", " + p.getNickname();
 				missingCount++;
 			}
 		}
 		if (missingCount != 0) {
 			throw new BadSaveFileException("Missing player" + (missingCount == 1 ? "" : "s") + ": "
 					+ missingPlayers.substring(2) + ".");
 		}
 		// Second, check if everyone is ready
 		if (!readyToStart()) {
 			throw new BadSaveFileException("Players not ready.");
 		}
 		// Third, modify players to match loaded state
 		for (final Player p : loadedState.getPlayers()) {
 			if (p.equals(loadedState.getNullPlayer())) {
 				continue;
 			}
 			final LobbyPlayer lobbyP = aLobby.getPlayerByNickname(p.getNickname());
 			lobbyP.setColor(p.getColor().name);
 			lobbyP.setRace(p.getRaceData().getType());
 		}
 		// All good, start the damn game already
 		return loadedState;
 	}
 
 	public double maxPingTime()
 	{
 		return aMaxPingTime;
 	}
 
 	@Override
 	public void messageReceived(final EverMessage message)
 	{
 		final String messageType = message.getType();
 		final Json messageContents = message.getJson();
 		if (messageType.equals(PingMessage.class.getName())) {
 			// it's just a ping, set maxPingTime if you must
 			final long timeDiff = System.currentTimeMillis() - messageContents.getLong();
 			aMaxPingTime = Math.max(aMaxPingTime, timeDiff);
 			return;
 		}
 		// Handle global messages first
 		if (messageType.equals(ChatMessage.class.getName())) {
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
 			observer.messageReceived(messageType, aLobby, message.getClient(), messageContents);
 		}
 	}
 
 	private void ping(final Client client)
 	{
 		send(client, new PingMessage());
 	}
 
 	public void pingAll()
 	{
 		sServerLog.info("Previous max ping time was " + aMaxPingTime + ". Starting another round of pinging");
 		// reset max ping time so that it's up do date
 		aMaxPingTime = 0;
 		// ping all players
 		for (final LobbyPlayer player : new ArrayList<LobbyPlayer>(aLobby.getPlayers())) {
 			ping(player.getClient());
 		}
 		// schedule a new round of pinging
 		if (aPingTimer != null) {
 			aPingTimer.cancel();
 		}
 		aPingTimer = new Timer();
 		aPingTimer.schedule(new TimerTask()
 		{
 			@Override
 			public void run()
 			{
 				pingAll();
 			}
 		}, sPingTime);
 		// ping every once in a while
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
		for (final LobbyPlayer lp : new ArrayList<LobbyPlayer>(aLobby.getPlayers())) {
 			send(lp.getClient(), new LobbyStateMessage(aLobby, lp.getNickname()));
 		}
 	}
 
 	public void registerListener(final EVServerMessageObserver listener)
 	{
 		aGameMessagesObservers.add(listener);
 	}
 
 	protected void send(final Client client, final EverMessage message)
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
 
 	protected void sendAll(final EverMessage message)
 	{
 		// clone list to deal with potential concurrent modification
 		for (final LobbyPlayer player : new ArrayList<LobbyPlayer>(aLobby.getPlayers())) {
 			send(player.getClient(), message);
 		}
 	}
 
 	void stop()
 	{
 		// if there's a ping timer floating around, kill it
 		if (aPingTimer != null) {
 			aPingTimer.cancel();
 			aPingTimer = null;
 		}
 		for (final Client client : new ArrayList<Client>(aSpiderMonkeyServer.getConnectors())) {
 			try {
 				if (client != null) {
 					send(client, new ServerQuit());
 					client.kick("Server shutting down");
 				}
 			}
 			catch (final Exception e) {
 				sServerLog.warning("Could not kick client " + client);
 			}
 		}
 		for (final EVServerMessageObserver observer : aGameMessagesObservers) {
 			observer.stop();
 		}
 		try {
 			aSpiderMonkeyServer.stop();
 		}
 		catch (final Exception e) {
 			sServerLog.severe("Could not stop the server. Caught " + e.getClass().getName());
 			e.printStackTrace();
 			System.exit(0);
 		}
 		try {
 			aDiscoveryServer.stop();
 		}
 		catch (final Exception e) {
 			sServerLog.warning("Could not stop discovery server. Caught " + e.getClass().getName());
 			e.printStackTrace();
 			System.exit(0);
 		}
 		aInGame = false;
 		try {
 			Thread.sleep(500); // Server needs some time to actually unbind
 		}
 		catch (final InterruptedException e) {
 			// Never gonna happen
 		}
 		sServerLog.info("Server stopped.");
 	}
 }
