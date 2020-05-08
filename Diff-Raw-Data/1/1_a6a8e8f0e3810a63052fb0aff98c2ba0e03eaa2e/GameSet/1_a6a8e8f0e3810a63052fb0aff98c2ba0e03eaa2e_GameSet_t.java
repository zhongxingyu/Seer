 /*
  * 
  */
 package org.opendarts.core.model.session.impl;
 
 import java.text.MessageFormat;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.CopyOnWriteArraySet;
 
 import org.opendarts.core.model.game.IGame;
 import org.opendarts.core.model.game.IGameDefinition;
 import org.opendarts.core.model.game.impl.GameDefinition;
 import org.opendarts.core.model.player.IPlayer;
 import org.opendarts.core.model.session.ISession;
 import org.opendarts.core.model.session.ISet;
 import org.opendarts.core.model.session.ISetListener;
 import org.opendarts.core.model.session.SetEvent;
 import org.opendarts.core.service.game.IGameService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * The Class GameSet.
  */
 public class GameSet extends GameContainer<IGame> implements ISet {
 
 	/** The logger. */
 	private static final Logger LOG = LoggerFactory.getLogger(GameSet.class);
 
 	/** The session. */
 	private final ISession session;
 
 	/** The game definition. */
 	private final GameDefinition gameDefinition;
 
 	/** The player games. */
 	private final Map<IPlayer, Integer> playerGames;
 
 	/** The listeners. */
 	private final CopyOnWriteArraySet<ISetListener> listeners;
 
 	/** The game service. */
 	private final IGameService gameService;
 
 	/**
 	 * Instantiates a new game set.
 	 *
 	 * @param session the session
 	 * @param gameDefinition the game definition
 	 */
 	public GameSet(ISession session, GameDefinition gameDefinition) {
 		super();
 		this.session = session;
 		this.gameDefinition = gameDefinition;
 		this.listeners = new CopyOnWriteArraySet<ISetListener>();
 		this.playerGames = new HashMap<IPlayer, Integer>();
 		this.gameService = gameDefinition.getGameService();
 		for (IPlayer player : gameDefinition.getPlayers()) {
 			this.playerGames.put(player, 0);
 		}
 
 		// create the first game
 		IGame game = this.gameService.createGame(this,
 				this.gameDefinition.getPlayers());
 		this.getInternalsGame().add(game);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.opendarts.prototype.model.session.ISet#initSet()
 	 */
 	@Override
 	public void initSet() {
 		this.setStart(Calendar.getInstance());
 
 		this.fireSetEvent(SetEvent.Factory.newSetInitializedEvent(this));
 		this.setCurrentGame(this.getInternalsGame().get(0));
 	}
 
 	/* (non-Javadoc)
 	 * @see org.opendarts.prototype.model.session.ISet#getWinningMessage()
 	 */
 	@Override
 	public String getWinningMessage() {
 		IPlayer player = this.getWinner();
 
 		StringBuffer sb = new StringBuffer();
 		boolean isFirst = true;
 		for (IPlayer p : this.gameDefinition.getPlayers()) {
 			if (!p.equals(player)) {
 				if (isFirst) {
 					isFirst = false;
 				} else {
 					sb.append(", ");
 				}
 				sb.append(p);
 				sb.append(": ");
 				sb.append(this.getWinningGames(p));
 			}
 		}
 		return MessageFormat.format(
 				"{0} win the set with {1} games against  {2}", player,
 				this.getWinningGames(player), sb);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.opendarts.prototype.model.session.ISet#getName()
 	 */
 	@Override
 	public String getName() {
 		StringBuffer result = new StringBuffer("Set: ");
 		boolean isFirst = true;
 		for (IPlayer player : this.gameDefinition.getPlayers()) {
 			if (isFirst) {
 				result.append(", ");
 			}
 			result.append(player);
 			result.append(" ");
 			result.append(this.getWinningGames(player));
 		}
 		result.append(" - ");
 		result.append(this.gameDefinition);
 		return result.toString();
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return this.getName();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.opendarts.prototype.model.session.ISet#getDescription()
 	 */
 	@Override
 	public String getDescription() {
 		return this.getName();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.opendarts.prototype.model.session.ISet#getWinningGame(org.opendarts.prototype.model.player.IPlayer)
 	 */
 	@Override
 	public int getWinningGames(IPlayer player) {
 		return this.playerGames.get(player);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.opendarts.prototype.model.session.ISet#getParentSession()
 	 */
 	@Override
 	public ISession getParentSession() {
 		return this.session;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.opendarts.prototype.model.session.ISet#getGameDefinition()
 	 */
 	@Override
 	public IGameDefinition getGameDefinition() {
 		return this.gameDefinition;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.opendarts.prototype.model.session.ISet#addListener(org.opendarts.prototype.model.session.ISetListener)
 	 */
 	@Override
 	public void addListener(ISetListener listener) {
 		this.listeners.add(listener);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.opendarts.prototype.model.session.ISet#removeListener(org.opendarts.prototype.model.session.ISetListener)
 	 */
 	@Override
 	public void removeListener(ISetListener listener) {
 		this.listeners.remove(listener);
 	}
 
 	/**
 	 * Fire set event.
 	 *
 	 * @param event the event
 	 */
 	protected void fireSetEvent(final SetEvent event) {
 		for (final ISetListener listener : this.listeners) {
 			try {
 				listener.notifySetEvent(event);
 			} catch (Throwable t) {
 				LOG.error("Error when sending game event: " + event, t);
 			}
 		}
 	}
 
 	/**
 	 * Handle finished game.
 	 *
 	 * @param game the game
 	 */
 	public void handleFinishedGame(IGame game) {
 		// update player score
 		IPlayer player = game.getWinner();
 		int score = this.getWinningGames(player);
 		score++;
 		this.playerGames.put(player, score);
 
 		// check if player win
 		if (this.getGameDefinition().isPlayerWin(this, player)) {
 			this.setWinner(player);
			this.setEnd(Calendar.getInstance());
 		}
 
 		// check end
 		if (this.getGameDefinition().isSetFinished(this)) {
 			LOG.info("Set win by {}", player);
 			this.fireSetEvent(SetEvent.Factory.newSetFinishedEvent(this,
 					this.getWinner(), game));
 		} else {
 			// create a new game
 			IGame newGame = this.createNewGame(game.getFirstPlayer());
 			this.setCurrentGame(newGame);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.opendarts.prototype.internal.model.session.GameContainer#setCurrentGame(java.lang.Object)
 	 */
 	@Override
 	protected void setCurrentGame(IGame game) {
 		super.setCurrentGame(game);
 		this.fireSetEvent(SetEvent.Factory.newSetGameEvent(this, game));
 	}
 
 	/**
 	 * Creates the new game.
 	 *
 	 * @param player the player
 	 * @return the i game
 	 */
 	private IGame createNewGame(IPlayer player) {
 		IGame game = this.gameService.createGame(this,
 				this.gameDefinition.getNextPlayers(this));
 		this.getInternalsGame().add(game);
 		return game;
 	}
 
 	/**
 	 * Cancel.
 	 */
 	public void cancelSet() {
 		this.fireSetEvent(SetEvent.Factory.newSetCanceledEvent(this));
 	}
 
 	/* (non-Javadoc)
 	 * @see org.opendarts.prototype.model.session.ISet#getGameService()
 	 */
 	@Override
 	public IGameService getGameService() {
 		return this.gameService;
 	}
 }
