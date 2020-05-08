 package game.gameclient;
 
 import game.common.IGameClient;
 import game.common.IGameServer;
 import game.communication.action.InconsistentActionTypeException;
 import game.communication.action.control.CreateGameCtrlAction;
 import game.communication.action.gamecreation.SendGameConfigurationGameCrAction;
 import game.communication.action.gamecreation.SendPlayerConfigurationGameCrAction;
 import game.communication.action.gamectrl.AddAICrAction;
 import game.communication.action.gamectrl.JoinGameCrAction;
 import game.communication.event.ControlEventType;
 import game.communication.event.IControlEvent;
 import game.communication.event.IEvent;
 import game.communication.event.IGameCreationEvent;
 import game.communication.event.IGameCtrlEvent;
 import game.communication.event.IGameEvent;
 import game.communication.event.InconsistentEventTypeException;
 import game.communication.event.control.GameCreationStartedCtrlEvent;
 import game.communication.event.control.GameJoinedCtrlEvent;
 import game.communication.event.control.IControlEventHandler;
 import game.communication.event.control.ServerStateCtrlEvent;
 import game.config.IGameConfiguration;
 import game.config.IPlayerConfiguration;
 import game.gameserver.IServerGameCreator;
 import game.gameserver.IServerSidePlayer;
 
 import java.util.Observable;
 import java.util.concurrent.ConcurrentSkipListMap;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Object describing the local GameClient. It receives the Event from all the
  * GameServer, and dispatch them to the different Players.
  * 
  * @author benobiwan
  * 
  */
 public final class LocalGameClient extends Observable implements IGameClient,
 		IControlEventHandler
 {
 	/**
 	 * Logger object.
 	 */
 	private static final Logger LOGGER = LoggerFactory
 			.getLogger(LocalGameClient.class);
 
 	/**
 	 * The name used locally by this client.
 	 */
 	private final String _strLocalName;
 
 	/**
 	 * Id which will be used for the next player.
 	 */
 	private int _iNextPlayerId = 1;
 
 	/**
 	 * Lock to protect the use of _iNextPlayerId.
 	 */
 	private final Object _lockNextPlayer = new Object();
 
 	/**
 	 * The {@link ILocalClientUI} for this game client.
 	 */
 	private ILocalClientUI _clientUI;
 
 	/**
 	 * Lock to protect the use of _clientUI.
 	 */
 	private final Object _lockUI = new Object();
 
 	/**
 	 * List of server side player on this client.
 	 */
 	private final ConcurrentSkipListMap<Integer, IServerSidePlayer<?>> _serverSidePlayerList = new ConcurrentSkipListMap<>();
 
 	/**
 	 * List of client side player on this client.
 	 */
 	private final ConcurrentSkipListMap<Integer, IClientSidePlayer<?, ?, ?, ?, ?>> _clientSidePlayerList = new ConcurrentSkipListMap<>();
 
 	/**
 	 * List of AI on this client.
 	 */
 	private final ConcurrentSkipListMap<Integer, String> _aiPlayerList = new ConcurrentSkipListMap<>();
 
 	/**
 	 * Creates a new LocalGameClient.
 	 * 
 	 * @param strLocalName
 	 *            the name of the local client.
 	 */
 	public LocalGameClient(final String strLocalName)
 	{
 		_strLocalName = strLocalName;
 	}
 
 	@Override
 	public String getName()
 	{
 		return _strLocalName;
 	}
 
 	@Override
 	public void handleEvent(final IGameServer server, final IEvent evt)
 			throws InconsistentEventTypeException
 	{
 		if (LOGGER.isDebugEnabled())
 		{
 			LOGGER.debug(evt.getClass().toString());
 		}
 		if (evt instanceof IControlEvent)
 		{
 			handleControlEvent(server, (IControlEvent) evt);
 		}
 		else if (evt instanceof IGameCreationEvent)
 		{
 			final IGameCreationEvent event = (IGameCreationEvent) evt;
 			final IClientSidePlayer<?, ?, ?, ?, ?> player = _clientSidePlayerList
 					.get(Integer.valueOf(event.getPlayerId()));
 			if (player == null)
 			{
 				LOGGER.error("Received an IGameCreationEvent type '"
 						+ event.getType() + "' from Server '"
 						+ server.getName() + "' concerning game id '"
 						+ event.getGameId() + "' concerning player id '"
 						+ event.getPlayerId() + "' which doesn't exits.");
 			}
 			else if (!player.isGameInCreation())
 			{
 				LOGGER.error("Received an IGameCreationEvent type '"
 						+ event.getType() + "' from Server '"
 						+ server.getName() + "' concerning game id '"
 						+ event.getGameId() + "' concerning player id '"
 						+ event.getPlayerId() + "' but game isn't in creation.");
 			}
 			else if (event.getGameId() != player.getGameId())
 			{
 				LOGGER.error("Received an IGameCreationEvent type '"
 						+ event.getType() + "' from Server '"
 						+ server.getName() + "' concerning game id '"
 						+ event.getGameId() + "' concerning player id '"
 						+ event.getPlayerId()
 						+ "' but the game id associated with the player is '"
 						+ player.getGameId() + "'.");
 			}
 			else if (!server.equals(player.getServer()))
 			{
 				LOGGER.error("Received an IGameCreationEvent type '"
 						+ event.getType() + "' from Server '"
 						+ server.getName() + "' concerning game id '"
 						+ event.getGameId() + "' concerning player id '"
 						+ event.getPlayerId()
 						+ "' but the server associated with the player is '"
 						+ player.getServer().getName() + "'.");
 			}
 			else
 			{
 				player.handleGameCreationEvent(event);
 			}
 		}
 		else if (evt instanceof IGameCtrlEvent)
 		{
 			final IGameCtrlEvent event = (IGameCtrlEvent) evt;
 			final IClientSidePlayer<?, ?, ?, ?, ?> player = _clientSidePlayerList
 					.get(Integer.valueOf(event.getPlayerId()));
 			if (player == null)
 			{
 				LOGGER.error("Received an IGameCtrlEvent type '"
 						+ event.getType() + "' from Server '"
 						+ server.getName() + "' concerning game id '"
 						+ event.getGameId() + "' concerning player id '"
 						+ event.getPlayerId() + "' which doesn't exits.");
 			}
 			else if (event.getGameId() != player.getGameId())
 			{
 				LOGGER.error("Received an IGameCtrlEvent type '"
 						+ event.getType() + "' from Server '"
 						+ server.getName() + "' concerning game id '"
 						+ event.getGameId() + "' concerning player id '"
 						+ event.getPlayerId()
 						+ "' but the game id associated with the player is '"
 						+ player.getGameId() + "'.");
 			}
 			else if (!server.equals(player.getServer()))
 			{
 				LOGGER.error("Received an IGameCtrlEvent type '"
 						+ event.getType() + "' from Server '"
 						+ server.getName() + "' concerning game id '"
 						+ event.getGameId() + "' concerning player id '"
 						+ event.getPlayerId()
 						+ "' but the server associated with the player is '"
 						+ player.getServer().getName() + "'.");
 			}
 			else
 			{
 				player.handleGameCtrlEvent(event);
 			}
 		}
 		else if (evt instanceof IGameEvent)
 		{
 			final IGameEvent event = (IGameEvent) evt;
 			final IClientSidePlayer<?, ?, ?, ?, ?> player = _clientSidePlayerList
 					.get(Integer.valueOf(event.getPlayerId()));
 			if (player == null)
 			{
 				LOGGER.error("Received an IGameEvent class '"
 						+ event.getClass() + "' from Server '"
 						+ server.getName() + "' concerning game id '"
 						+ event.getGameId() + "' concerning player id '"
 						+ event.getPlayerId() + "' which doesn't exits.");
 			}
 			else if (player.isGameInCreation())
 			{
 				LOGGER.error("Received an IGameEvent class '"
 						+ event.getClass() + "' from Server '"
 						+ server.getName() + "' concerning game id '"
 						+ event.getGameId() + "' concerning player id '"
 						+ event.getPlayerId()
 						+ "' but the game is in creation.");
 			}
 			else if (event.getGameId() != player.getGameId())
 			{
 				LOGGER.error("Received an IGameEvent class '"
 						+ event.getClass() + "' from Server '"
 						+ server.getName() + "' concerning game id '"
 						+ event.getGameId() + "' concerning player id '"
 						+ event.getPlayerId()
 						+ "' but the game id associated with the player is '"
 						+ player.getGameId() + "'.");
 			}
 			else if (!server.equals(player.getServer()))
 			{
 				LOGGER.error("Received an '" + event.getClass()
 						+ "' event from Server '" + server.getName()
 						+ "' concerning game id '" + event.getGameId()
 						+ "' concerning player id '" + event.getPlayerId()
 						+ "' but the server associated with the player is '"
 						+ player.getServer().getName() + "'.");
 			}
 			else
 			{
 				player.handleGameEvent(event);
 			}
 		}
 		else
 		{
 			LOGGER.error("Unknown event type.");
 		}
 	}
 
 	@Override
 	public void registerGameServer(final IGameServer server)
 	{
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public int compareTo(final IGameClient o)
 	{
 		return _strLocalName.compareTo(o.getName());
 	}
 
 	@Override
 	public int hashCode()
 	{
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((_strLocalName == null) ? 0 : _strLocalName.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(final Object obj)
 	{
 		if (this == obj)
 		{
 			return true;
 		}
 		if (obj == null)
 		{
 			return false;
 		}
 		if (getClass() != obj.getClass())
 		{
 			return false;
 		}
 		final LocalGameClient other = (LocalGameClient) obj;
 		if (_strLocalName == null)
 		{
 			if (other._strLocalName != null)
 			{
 				return false;
 			}
 		}
 		else if (!_strLocalName.equals(other._strLocalName))
 		{
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public void handleControlEvent(final IGameServer server,
 			final IControlEvent evt) throws InconsistentEventTypeException
 	{
 		switch (evt.getType())
 		{
 		case GAME_CREATION_STARTED:
 			if (evt instanceof GameCreationStartedCtrlEvent)
 			{
 				handleControlEvent(server, (GameCreationStartedCtrlEvent) evt);
 			}
 			else
 			{
 				throw new InconsistentEventTypeException(
 						ControlEventType.GAME_CREATION_STARTED, evt.getClass());
 			}
 			break;
 		case GAME_JOINED:
 			if (evt instanceof GameJoinedCtrlEvent)
 			{
 				handleControlEvent(server, (GameJoinedCtrlEvent) evt);
 			}
 			else
 			{
 				throw new InconsistentEventTypeException(
 						ControlEventType.GAME_JOINED, evt.getClass());
 			}
 			break;
 		case SERVER_STATE:
 			if (evt instanceof ServerStateCtrlEvent)
 			{
 				handleControlEvent(server, (ServerStateCtrlEvent) evt);
 			}
 			else
 			{
 				throw new InconsistentEventTypeException(
 						ControlEventType.SERVER_STATE, evt.getClass());
 			}
 			break;
 		}
 	}
 
 	@Override
 	public void handleControlEvent(final IGameServer server,
 			final GameJoinedCtrlEvent evt)
 	{
 		joinGame(server, evt.getClientGameCreator(), evt.getGameId(),
 				evt.getPlayerId(), false);
 		// send player configuration
 		final IPlayerConfiguration playerConf = evt.getClientGameCreator()
 				.createPlayerConfiguration();
 		sendPlayerConf(server, evt.getGameId(), evt.getPlayerId(), playerConf);
 		setChanged();
 		notifyObservers();
 		// TODO handleControlEvent GameJoinedEvent
 	}
 
 	@Override
 	public void handleControlEvent(final IGameServer server,
 			final GameCreationStartedCtrlEvent evt)
 	{
 		joinGame(server, evt.getClientGameCreator(), evt.getGameId(),
 				evt.getPlayerId(), true);
 		// send player configuration
 		final IPlayerConfiguration playerConf = evt.getClientGameCreator()
 				.createPlayerConfiguration();
 		sendPlayerConf(server, evt.getGameId(), evt.getPlayerId(), playerConf);
 		// send game configuration
 		final IGameConfiguration<?> gameConf = evt.getClientGameCreator()
				.createGameConfiguration();
 		sendGameConf(server, evt.getGameId(), evt.getPlayerId(), gameConf);
 		setChanged();
 		notifyObservers();
 		// TODO handleControlEvent GameCreationStartedCtrlEvent
 	}
 
 	@Override
 	public void handleControlEvent(final IGameServer server,
 			final ServerStateCtrlEvent evt)
 	{
 		server.updateServerState(evt.getServerState());
 		setChanged();
 		notifyObservers();
 		// TODO update game list
 	}
 
 	/**
 	 * Creates a new game on the specified {@link IGameServer}.
 	 * 
 	 * @param gameCreator
 	 *            the {@link IServerGameCreator} to use on the server to create
 	 *            the game.
 	 * @param server
 	 *            the {@link IGameServer} on which to create the game.
 	 */
 	public void sendCreateGame(
 			final IServerGameCreator<?, ?, ?, ?, ?> gameCreator,
 			final IGameServer server)
 	{
 		final CreateGameCtrlAction act = new CreateGameCtrlAction(gameCreator,
 				getNextPlayerId());
 		try
 		{
 			server.handleAction(this, act);
 		}
 		catch (final InconsistentActionTypeException e)
 		{
 			LOGGER.error(e.getLocalizedMessage(), e);
 		}
 	}
 
 	/**
 	 * Join a game on the specified {@link IGameServer}.
 	 * 
 	 * @param server
 	 *            the {@link IGameServer} on which to join a game.
 	 * @param iGameId
 	 *            the id of the game to join.
 	 */
 	public void sendJoinGame(final IGameServer server, final int iGameId)
 	{
 		final JoinGameCrAction act = new JoinGameCrAction(iGameId,
 				getNextPlayerId());
 		try
 		{
 			server.handleAction(this, act);
 		}
 		catch (final InconsistentActionTypeException e)
 		{
 			LOGGER.error(e.getLocalizedMessage(), e);
 		}
 	}
 
 	/**
 	 * Creates an AI in the specified game on the specified {@link IGameServer}.
 	 * 
 	 * @param server
 	 *            the {@link IGameServer} on which to join a game.
 	 * @param iGameId
 	 *            the id of the game to join.
 	 * @param iPlayerId
 	 *            the id of the player sending the message.
 	 * @param strAIName
 	 *            the name of the AI which want to join.
 	 */
 	public void sendAddAI(final IGameServer server, final int iGameId,
 			final int iPlayerId, final String strAIName)
 	{
 		final int iAIID = getNextPlayerId();
 		_aiPlayerList.put(Integer.valueOf(iAIID), strAIName);
 		if (LOGGER.isDebugEnabled())
 		{
 			LOGGER.debug("Added AI : " + iAIID);
 		}
 		final AddAICrAction act = new AddAICrAction(iGameId, iPlayerId, iAIID,
 				strAIName);
 		try
 		{
 			server.handleAction(this, act);
 		}
 		catch (final InconsistentActionTypeException e)
 		{
 			LOGGER.error(e.getLocalizedMessage(), e);
 		}
 	}
 
 	/**
 	 * Sends a {@link SendPlayerConfigurationGameCrAction} message to the
 	 * specified {@link IGameServer}.
 	 * 
 	 * @param server
 	 *            the {@link IGameServer} to which sending the
 	 *            {@link IPlayerConfiguration}.
 	 * @param iGameId
 	 *            the id of the game concerned by the
 	 *            {@link IPlayerConfiguration}.
 	 * @param iPlayerId
 	 *            the id of the player sending the message.
 	 * @param playerConf
 	 *            the {@link IPlayerConfiguration} to send.
 	 */
 	public void sendPlayerConf(final IGameServer server, final int iGameId,
 			final int iPlayerId, final IPlayerConfiguration playerConf)
 	{
 		final SendPlayerConfigurationGameCrAction act = new SendPlayerConfigurationGameCrAction(
 				iGameId, iPlayerId, playerConf);
 		try
 		{
 			server.handleAction(this, act);
 		}
 		catch (final InconsistentActionTypeException e)
 		{
 			LOGGER.error(e.getLocalizedMessage(), e);
 		}
 	}
 
 	/**
 	 * Sends a {@link SendGameConfigurationGameCrAction} message to the
 	 * specified {@link IGameServer}.
 	 * 
 	 * @param server
 	 *            the {@link IGameServer} to which sending the
 	 *            {@link IGameConfiguration}.
 	 * @param iGameId
 	 *            the id of the game concerned by the {@link IGameConfiguration}
 	 *            .
 	 * @param iPlayerId
 	 *            the id of the player sending the message.
 	 * @param gameConf
 	 *            the {@link IGameConfiguration} to send.
 	 */
 	public void sendGameConf(final IGameServer server, final int iGameId,
 			final int iPlayerId, final IGameConfiguration<?> gameConf)
 	{
 		final SendGameConfigurationGameCrAction act = new SendGameConfigurationGameCrAction(
 				iGameId, iPlayerId, gameConf);
 		try
 		{
 			server.handleAction(this, act);
 		}
 		catch (final InconsistentActionTypeException e)
 		{
 			LOGGER.error(e.getLocalizedMessage(), e);
 		}
 	}
 
 	/**
 	 * Get the id which will be used for the next player.
 	 * 
 	 * @return the id which will be used for the next player.
 	 */
 	private int getNextPlayerId()
 	{
 		synchronized (_lockNextPlayer)
 		{
 			return _iNextPlayerId++;
 		}
 	}
 
 	@Override
 	public IServerSidePlayer<?> getServerSidePlayer(final int iPlayerId)
 	{
 		return _serverSidePlayerList.get(Integer.valueOf(iPlayerId));
 	}
 
 	@Override
 	public void addServerSidePlayer(final IServerSidePlayer<?> player)
 	{
 		_serverSidePlayerList.put(Integer.valueOf(player.getId()), player);
 	}
 
 	@Override
 	public void removeServerSidePlayer(final IServerSidePlayer<?> player)
 	{
 		_serverSidePlayerList.remove(Integer.valueOf(player.getId()));
 	}
 
 	@Override
 	public boolean containServerSidePlayer(final IServerSidePlayer<?> player)
 	{
 		return _serverSidePlayerList
 				.containsKey(Integer.valueOf(player.getId()));
 	}
 
 	/**
 	 * Remove an {@link IClientSidePlayer} from the game client.
 	 * 
 	 * @param player
 	 *            the {@link IClientSidePlayer} to remove.
 	 */
 	public void removeClientSidePlayer(
 			final IClientSidePlayer<?, ?, ?, ?, ?> player)
 	{
 		_clientSidePlayerList.remove(Integer.valueOf(player.getId()));
 		setChanged();
 		notifyObservers();
 	}
 
 	/**
 	 * Get the name of the AI corresponding to the given id. Returns null if
 	 * this client isn't an AI.
 	 * 
 	 * @param iAIId
 	 *            the id of the AI.
 	 * @return the name of the AI.
 	 */
 	public String getAIName(final int iAIId)
 	{
 		return _aiPlayerList.get(Integer.valueOf(iAIId));
 	}
 
 	/**
 	 * Check if the give player is an AI.
 	 * 
 	 * @param iPlayerId
 	 *            the id of the player to test.
 	 * @return true if the player is an AI.
 	 */
 	private boolean isAI(final int iPlayerId)
 	{
 		return _aiPlayerList.containsKey(Integer.valueOf(iPlayerId));
 	}
 
 	/**
 	 * Change the {@link ILocalClientUI} of this {@link LocalGameClient}.
 	 * 
 	 * @param clientUI
 	 *            the new {@link ILocalClientUI}.
 	 */
 	public void setClientUI(final ILocalClientUI clientUI)
 	{
 		synchronized (_lockUI)
 		{
 			_clientUI = clientUI;
 		}
 	}
 
 	/**
 	 * Join the specified game.
 	 * 
 	 * @param server
 	 *            the {@link IGameServer} on which the game to join is.
 	 * @param gameCreator
 	 *            the {@link IClientGameCreator} of the game to join.
 	 * @param iGameId
 	 *            the id of the game to join.
 	 * @param iPlayerId
 	 *            the player id which the local client is going to use on this
 	 *            game.
 	 * @param bCreator
 	 *            whether on not this client is the creator of the game.
 	 */
 	private void joinGame(final IGameServer server,
 			final IClientGameCreator<?, ?, ?, ?, ?, ?> gameCreator,
 			final int iGameId, final int iPlayerId, final boolean bCreator)
 	{
 		gameCreator.initialize(bCreator, this, server, iGameId, iPlayerId);
 		final IClientSidePlayer<?, ?, ?, ?, ?> player = gameCreator
 				.createPlayer(this, iPlayerId);
 		_clientSidePlayerList.put(Integer.valueOf(iPlayerId), player);
 		if (LOGGER.isDebugEnabled())
 		{
 			LOGGER.debug("Player joined, id : " + iPlayerId);
 		}
 		if (!isAI(iPlayerId))
 		{
 			synchronized (_lockUI)
 			{
 				if (_clientUI != null)
 				{
 					_clientUI.createGameCreationUI(gameCreator);
 				}
 			}
 		}
 	}
 }
