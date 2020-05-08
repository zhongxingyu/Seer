 package game.gameclient;
 
 import game.common.IGameServer;
 import game.common.IPlayerDescription;
 import game.communication.action.InconsistentActionTypeException;
 import game.communication.action.gamecreation.UpdateStatusCrAction;
 import game.communication.action.gamectrl.LeaveGameCrAction;
 import game.communication.event.GameCreationEventType;
 import game.communication.event.GameCtrlEventType;
 import game.communication.event.IGameCreationEvent;
 import game.communication.event.IGameCtrlEvent;
 import game.communication.event.IGameEvent;
 import game.communication.event.InconsistentEventTypeException;
 import game.communication.event.gamecreation.ConfigurationUpdateCrEvent;
 import game.communication.event.gamecreation.GameCreatedCrEvent;
 import game.communication.event.gamectrl.GameDestroyedCrEvent;
 import game.communication.event.gamectrl.GameFullCrEvent;
 import game.communication.event.gamectrl.GameLeftCrEvent;
 import game.communication.event.gamectrl.KickedFromGameCrEvent;
 import game.communication.event.gamectrl.PlayerListUpdateCrEvent;
 import game.config.IGameConfiguration;
 import game.config.IPlayerConfiguration;
 
 import java.util.Collections;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.eventbus.EventBus;
 
 /**
  * Abstract implementation of the {@link IClientGameCreator} interface. Used on
  * the client side to join a game.
  * 
  * @author benobiwan
  * 
  * @param <CLIENT_GAME_TYPE>
  *            the type of game to create.
  * @param <CONF_TYPE>
  *            the type of {@link IGameConfiguration} used to configure the game.
  * @param <EVENT_TYPE>
  *            the type of {@link IGameEvent} handled by the game.
  * @param <PLAYER_TYPE>
  *            the type of {@link IClientSidePlayer} playing this game.
  * @param <PLAYER_CONF>
  *            the type of player configuration.
  * @param <CLIENT_CHANGE_LISTENER>
  *            the type of {@link IClientSidePlayerChangeListener} for this game
  *            creator.
  */
 public abstract class AbstractClientGameCreator<CONF_TYPE extends IGameConfiguration<PLAYER_CONF>, EVENT_TYPE extends IGameEvent, CLIENT_GAME_TYPE extends IClientSideGame<EVENT_TYPE, PLAYER_CONF, CONF_TYPE>, PLAYER_CONF extends IPlayerConfiguration, PLAYER_TYPE extends IClientSidePlayer<CONF_TYPE, EVENT_TYPE, CLIENT_GAME_TYPE, PLAYER_CONF, CLIENT_CHANGE_LISTENER>, CLIENT_CHANGE_LISTENER extends IClientSidePlayerChangeListener>
 		implements
 		IClientGameCreator<CONF_TYPE, EVENT_TYPE, CLIENT_GAME_TYPE, PLAYER_CONF, PLAYER_TYPE, CLIENT_CHANGE_LISTENER>
 {
 	/**
 	 * Logger object.
 	 */
 	private static transient final Logger LOGGER = LoggerFactory
 			.getLogger(AbstractClientGameCreator.class);
 
 	/**
 	 * Lock to protect the access to the other parameters of the object.
 	 */
 	protected transient final Object _lock = new Object();
 
 	/**
 	 * The game server on which this game is created.
 	 */
 	protected transient IGameServer _gameServer;
 
 	/**
 	 * Boolean telling whether the local client is the creator of this game.
 	 */
 	protected transient boolean _bIsCreator;
 
 	/**
 	 * The local game client.
 	 */
 	protected transient LocalGameClient _gameClient;
 
 	/**
 	 * Id of the game.
 	 */
 	protected transient int _iGameId;
 
 	/**
 	 * Id of the player.
 	 */
 	protected transient int _iPlayerId;
 
 	/**
 	 * Configuration of the game.
 	 */
 	protected transient CONF_TYPE _conf;
 
 	/**
 	 * List of player playing in this game.
 	 */
 	protected transient Set<IPlayerDescription> _playerList;
 
 	/**
 	 * Boolean telling whether of not this player is ready.
 	 */
 	protected transient boolean _bReady;
 
 	/**
 	 * {@link EventBus} used to propagates events to all change listener of this
 	 * game creator.
 	 */
 	protected final EventBus _eventBus = new EventBus();
 
 	@Override
 	public boolean isCreator()
 	{
 		synchronized (_lock)
 		{
 			return _bIsCreator;
 		}
 	}
 
 	@Override
 	public IGameServer getGameServer()
 	{
 		synchronized (_lock)
 		{
 			return _gameServer;
 		}
 	}
 
 	@Override
 	public void initialize(final boolean bCreator,
 			final LocalGameClient locGameClient, final IGameServer server,
 			final int iGameId, final int iPlayerId)
 	{
 		synchronized (_lock)
 		{
 			_gameServer = server;
 			_bIsCreator = bCreator;
 			_iGameId = iGameId;
 			_gameClient = locGameClient;
 			_iPlayerId = iPlayerId;
 		}
 	}
 
 	@Override
 	public int getGameId()
 	{
 		synchronized (_lock)
 		{
 			return _iGameId;
 		}
 	}
 
 	@Override
 	public int getPlayerId()
 	{
 		synchronized (_lock)
 		{
 			return _iPlayerId;
 		}
 	}
 
 	@Override
 	public CONF_TYPE getConfiguration()
 	{
 		synchronized (_lock)
 		{
			if (_conf == null && isCreator())
			{
				_conf = createGameConfiguration();
			}
 			return _conf;
 		}
 	}
 
 	@Override
 	public void setConfiguration(final CONF_TYPE gameConfiguration)
 	{
 		synchronized (_lock)
 		{
 			_conf = gameConfiguration;
 		}
 		if (LOGGER.isDebugEnabled())
 		{
 			LOGGER.debug("setConfiguration");
 		}
 		_eventBus.post(gameConfiguration);
 	}
 
 	@Override
 	public void setClientSidePlayerList(final Set<IPlayerDescription> playerList)
 	{
 		synchronized (_lock)
 		{
 			_playerList = playerList;
 		}
 		if (LOGGER.isDebugEnabled())
 		{
 			LOGGER.debug("setClientSidePlayerList");
 		}
 		_eventBus.post(playerList);
 	}
 
 	@Override
 	public Set<IPlayerDescription> getClientSidePlayerList()
 	{
 		synchronized (_lock)
 		{
 			return Collections.unmodifiableSet(_playerList);
 		}
 	}
 
 	@Override
 	public void updateReadyStatus(final boolean bReadyStatus)
 	{
 		synchronized (_lock)
 		{
 			if (_bReady != bReadyStatus)
 			{
 				_bReady = bReadyStatus;
 				final UpdateStatusCrAction act = new UpdateStatusCrAction(
 						_iGameId, _iGameId, _bReady);
 				try
 				{
 					_gameServer.handleAction(_gameClient, act);
 				}
 				catch (final InconsistentActionTypeException e)
 				{
 					LOGGER.error(e.getLocalizedMessage(), e);
 				}
 			}
 		}
 	}
 
 	@Override
 	public void registerGameCreatorChangeListener(
 			final IGameCreatorChangeListener o)
 	{
 		if (LOGGER.isDebugEnabled())
 		{
 			LOGGER.debug("registerGameCreatorChangeListener");
 		}
 		_eventBus.register(o);
 	}
 
 	@Override
 	public void handleGameCtrlEvent(final IGameCtrlEvent evt)
 			throws InconsistentEventTypeException
 	{
 		switch (evt.getType())
 		{
 		case PLAYER_LIST_UPDATE:
 			if (evt instanceof PlayerListUpdateCrEvent)
 			{
 				handlePlayerListUpdateCrEvent((PlayerListUpdateCrEvent) evt);
 			}
 			else
 			{
 				throw new InconsistentEventTypeException(
 						GameCtrlEventType.PLAYER_LIST_UPDATE, evt.getClass());
 			}
 			break;
 		case KICKED_FROM_GAME:
 			if (evt instanceof KickedFromGameCrEvent)
 			{
 				handleKickedFromGameCrEvent((KickedFromGameCrEvent) evt);
 			}
 			else
 			{
 				throw new InconsistentEventTypeException(
 						GameCtrlEventType.KICKED_FROM_GAME, evt.getClass());
 			}
 			break;
 		case GAME_LEFT:
 			if (evt instanceof GameLeftCrEvent)
 			{
 				handleGameLeftCrEvent((GameLeftCrEvent) evt);
 			}
 			else
 			{
 				throw new InconsistentEventTypeException(
 						GameCtrlEventType.GAME_LEFT, evt.getClass());
 			}
 			break;
 		case GAME_DESTROYED:
 			if (evt instanceof GameDestroyedCrEvent)
 			{
 				handleGameDestroyedCrEvent((GameDestroyedCrEvent) evt);
 			}
 			else
 			{
 				throw new InconsistentEventTypeException(
 						GameCtrlEventType.GAME_DESTROYED, evt.getClass());
 			}
 			break;
 		case GAME_FULL:
 			if (evt instanceof GameFullCrEvent)
 			{
 				handleGameFullCrEvent((GameFullCrEvent) evt);
 			}
 			else
 			{
 				throw new InconsistentEventTypeException(
 						GameCtrlEventType.GAME_FULL, evt.getClass());
 			}
 			break;
 		}
 	}
 
 	@Override
 	public void handleGameCreationEvent(final IGameCreationEvent evt)
 			throws InconsistentEventTypeException
 	{
 		switch (evt.getType())
 		{
 		case CONFIGURATION_UPDATE:
 			if (evt instanceof ConfigurationUpdateCrEvent)
 			{
 				handleConfigurationUpdateCrEvent((ConfigurationUpdateCrEvent) evt);
 			}
 			else
 			{
 				throw new InconsistentEventTypeException(
 						GameCreationEventType.CONFIGURATION_UPDATE,
 						evt.getClass());
 			}
 			break;
 		case GAME_CREATED:
 			if (evt instanceof GameCreatedCrEvent)
 			{
 				handleGameCreatedCrEvent((GameCreatedCrEvent) evt);
 			}
 			else
 			{
 				throw new InconsistentEventTypeException(
 						GameCreationEventType.GAME_CREATED, evt.getClass());
 			}
 			break;
 		}
 	}
 
 	@Override
 	public void handleKickedFromGameCrEvent(final KickedFromGameCrEvent evt)
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void handleGameLeftCrEvent(final GameLeftCrEvent evt)
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void handleGameDestroyedCrEvent(final GameDestroyedCrEvent evt)
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void handleGameFullCrEvent(final GameFullCrEvent evt)
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void handlePlayerListUpdateCrEvent(final PlayerListUpdateCrEvent evt)
 	{
 		if (LOGGER.isDebugEnabled())
 		{
 			LOGGER.debug("setClientSidePlayerList");
 		}
 		setClientSidePlayerList(evt.getPlayerList());
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void handleConfigurationUpdateCrEvent(
 			final ConfigurationUpdateCrEvent evt)
 	{
 		try
 		{
 			if (LOGGER.isDebugEnabled())
 			{
 				LOGGER.debug("setConfiguration");
 			}
 			setConfiguration((CONF_TYPE) evt.getGameConfiguration());
 		}
 		catch (final ClassCastException e)
 		{
 			LOGGER.error("Error casting "
 					+ evt.getGameConfiguration().getClass() + " into "
 					+ _conf.getClass());
 		}
 	}
 
 	@Override
 	public void handleGameCreatedCrEvent(final GameCreatedCrEvent evt)
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public boolean tryLaunchGame()
 	{
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean isGameReady()
 	{
 		// check game configuration
 		if (_conf == null)
 		{
 			return false;
 		}
 		// check if there are enough player
 		if (_playerList.size() < _conf.getMinNumberOfPlayers())
 		{
 			return false;
 		}
 		// check if each player is ready
 		for (final IPlayerDescription desc : _playerList)
 		{
 			if (!desc.isReady())
 			{
 				return false;
 			}
 		}
 		return true;
 	}
 
 	@Override
 	public void leaveGameCreation()
 	{
 		final LeaveGameCrAction act = new LeaveGameCrAction(_iGameId, _iGameId);
 		try
 		{
 			_gameServer.handleAction(_gameClient, act);
 		}
 		catch (final InconsistentActionTypeException e)
 		{
 			LOGGER.error(e.getLocalizedMessage(), e);
 		}
 	}
 
 	@Override
 	public void addAI(final String strAIName)
 	{
 		_gameClient.sendAddAI(_gameServer, _iGameId, _iPlayerId, strAIName);
 	}
 }
