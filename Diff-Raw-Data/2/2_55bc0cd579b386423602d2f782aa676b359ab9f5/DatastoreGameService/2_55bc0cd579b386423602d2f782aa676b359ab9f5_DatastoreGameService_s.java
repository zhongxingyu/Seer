 package com.github.dansmithy.sanjuan.game;
 
 import java.util.Arrays;
 import java.util.List;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import com.github.dansmithy.sanjuan.dao.GameDao;
 import com.github.dansmithy.sanjuan.exception.IllegalGameStateException;
 import com.github.dansmithy.sanjuan.exception.NotResourceOwnerAccessException;
 import com.github.dansmithy.sanjuan.exception.SanJuanUnexpectedException;
 import com.github.dansmithy.sanjuan.game.aspect.ProcessGame;
 import com.github.dansmithy.sanjuan.model.Deck;
 import com.github.dansmithy.sanjuan.model.Game;
 import com.github.dansmithy.sanjuan.model.GameState;
 import com.github.dansmithy.sanjuan.model.Phase;
 import com.github.dansmithy.sanjuan.model.Play;
 import com.github.dansmithy.sanjuan.model.Player;
 import com.github.dansmithy.sanjuan.model.Role;
 import com.github.dansmithy.sanjuan.model.builder.CardFactory;
 import com.github.dansmithy.sanjuan.model.builder.TariffBuilder;
 import com.github.dansmithy.sanjuan.model.input.PlayChoice;
 import com.github.dansmithy.sanjuan.model.input.PlayCoords;
 import com.github.dansmithy.sanjuan.model.input.PlayOffered;
 import com.github.dansmithy.sanjuan.model.input.RoleChoice;
 import com.github.dansmithy.sanjuan.model.update.GameUpdater;
 import com.github.dansmithy.sanjuan.security.AuthenticatedSessionProvider;
 
 @Named
 public class DatastoreGameService implements GameService {
 
 	private TariffBuilder tariffBuilder;
 	private CardFactory cardFactory;
 	private final CalculationService calculationService;
 	private final GameDao gameDao;
 	private final AuthenticatedSessionProvider userProvider;
 	
 	@Inject
 	public DatastoreGameService(GameDao gameDao, AuthenticatedSessionProvider userProvider, TariffBuilder tariffBuilder, CardFactory cardFactory, CalculationService calculationService) {
 		super();
 		this.gameDao = gameDao;
 		this.userProvider = userProvider;
 		this.tariffBuilder = tariffBuilder;
 		this.cardFactory = cardFactory;
 		this.calculationService = calculationService;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.github.dansmithy.sanjuan.game.GameService#doDanny(java.lang.Long)
 	 */
 	@Override
 	@ProcessGame
 	public Game getGame(Long gameId) {
 		return gameDao.getGame(gameId);
 	}
 	
 	/* (non-Javadoc)
 	 * @see com.github.dansmithy.sanjuan.game.GameService#getGamesForPlayer(java.lang.String)
 	 */
 	@Override
 	public List<Game> getGamesForPlayer(String playerName) {
 		return gameDao.getGamesForPlayer(playerName);
 	}	
 	
 	/* (non-Javadoc)
 	 * @see com.github.dansmithy.sanjuan.game.GameService#getGamesInState(com.github.dansmithy.sanjuan.model.GameState)
 	 */
 	@Override
 	public List<Game> getGamesInState(GameState gameState) {
 		return gameDao.getGamesInState(gameState);
 	}	
 
 	/* (non-Javadoc)
 	 * @see com.github.dansmithy.sanjuan.game.GameService#createNewGame(java.lang.String)
 	 */
 	@Override
 	@ProcessGame
 	public Game createNewGame(String ownerName) {
 		Player owner = new Player(ownerName);
 		Game game = new Game(owner);
 		gameDao.createGame(game);
 		return game;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.github.dansmithy.sanjuan.game.GameService#addPlayerToGame(java.lang.Long, java.lang.String)
 	 */
 	@Override
 	public Player addPlayerToGame(Long gameId, String playerName) {
 		Game game = getGame(gameId);
 
 		if (game.hasPlayer(playerName)) {
 			throw new IllegalGameStateException(String.format("%s is already a player for this game.", playerName));
 		}
 		// TODO check game state
 		Player player = new Player(playerName);
 		game.addPlayer(player);
 		gameDao.saveGame(game);
 		return player;
 	}
 
 	
 	/* (non-Javadoc)
 	 * @see com.github.dansmithy.sanjuan.game.GameService#startGame(java.lang.Long)
 	 */
 	@Override
 	@ProcessGame
 	public Game startGame(Long gameId) {
 		Game game = gameDao.getGame(gameId);
 		
 		String loggedInUser = userProvider.getAuthenticatedUsername();
 		if (!loggedInUser.equals(game.getOwner())) {
 			throw new NotResourceOwnerAccessException(String.format("Must be game owner to start game."));
 		}
 		
 		if (game.getState().equals(GameState.PLAYING)) {
 			return game;
 		}
 		
 		if (!game.getState().equals(GameState.RECRUITING)) {
 			throw new IllegalStateException(String.format("Can't change state from %s to %s.", game.getState(), GameState.PLAYING));
 		}
 
 		game.startPlaying(cardFactory, tariffBuilder);
 		gameDao.saveGame(game);
 		return game;
 	}
 	
 	/* (non-Javadoc)
 	 * @see com.github.dansmithy.sanjuan.game.GameService#selectRole(com.github.dansmithy.sanjuan.model.input.PlayCoords, com.github.dansmithy.sanjuan.model.input.RoleChoice)
 	 */
 	@Override
 	@ProcessGame
 	public Game selectRole(PlayCoords playCoords, RoleChoice choice) {
 		
 		Game game = getGame(playCoords.getGameId());
 		GameUpdater gameUpdater = new GameUpdater(playCoords);
 		Phase phase = gameUpdater.getCurrentPhase(game);
 
 		String loggedInUser = userProvider.getAuthenticatedUsername();
 		if (!loggedInUser.equals(phase.getLeadPlayer())) {
 			throw new NotResourceOwnerAccessException(String.format("It is not your turn to choose role."));
 		}
 
 		Role role = choice.getRole();
 		Play play = new Play(phase.getLeadPlayer(), true);
 		
 		if (Role.BUILDER.equals(role)) {
 			// do nothing
 		} else if (Role.PROSPECTOR.equals(role)) {
 			// do nothing
 		} else if (Role.COUNCILLOR.equals(role)) {
 			Deck deck = game.getDeck();
 			PlayOffered offered = new PlayOffered();
 			offered.setCouncilOptions(deck.take(5));
 			play.setOffered(offered);
 			gameUpdater.updateDeck(deck);			
 		}
 
 		phase.selectRole(choice.getRole(), play);
 		gameUpdater.updatePhase(phase);
 		return gameDao.gameUpdate(game.getGameId(), gameUpdater);
 	}	
 	
 	/* (non-Javadoc)
 	 * @see com.github.dansmithy.sanjuan.game.GameService#makePlay(com.github.dansmithy.sanjuan.model.input.PlayCoords, com.github.dansmithy.sanjuan.model.input.PlayChoice)
 	 */
 	@Override
 	@ProcessGame
 	public Game makePlay(PlayCoords coords, PlayChoice playChoice) {
 		
 		Game game = getGame(coords.getGameId());
 		
 		// TODO verify coords is current
 		// TODO verify play is current one
 		
 		if (playChoice.getSkip() != null && playChoice.getSkip()) {
 			return playSkip(game, coords, playChoice);
 		}
 		
 		Role role = game.getCurrentRound().getCurrentPhase().getRole();
 		
 		if (Role.BUILDER.equals(role)) {
 			return playBuild(game, coords, playChoice);
 		} else if (Role.PROSPECTOR.equals(role)) {
 			return doProspector(game, coords, playChoice);
 		} else if (Role.COUNCILLOR.equals(role)) {
 			return doCouncillor(game, coords, playChoice);		
 		} else {
 			return null;
 		}
 	}		
 	
 	private Game playSkip(Game game, PlayCoords coords, PlayChoice playChoice) {
 		
 		GameUpdater gameUpdater = new GameUpdater(coords);
 		Play play = gameUpdater.getCurrentPlay(game);
 		play.makePlay(playChoice);
 		gameUpdater.completedPlay(play);
 		gameUpdater.createNextStep(game);
 		return gameDao.gameUpdate(game.getGameId(), gameUpdater);
 	}
 	
 	
 	private Game playBuild(Game game, PlayCoords coords, PlayChoice playChoice) {
 		
 		GameUpdater gameUpdater = new GameUpdater(coords);
 		
 		Play play = gameUpdater.getCurrentPlay(game);
 		play.makePlay(playChoice);
 		
 		gameUpdater.completedPlay(play);
 		
 		Player player = getCurrentPlayer(game);
 		int playerIndex = game.getPlayerIndex(player.getName());
 		player.moveToBuildings(playChoice.getBuild());
 		player.removeHandCards(playChoice.getPaymentAsArray());
 		gameUpdater.updatePlayer(playerIndex, player);
 
 		game.getDeck().discard(playChoice.getPaymentAsArray());
 		gameUpdater.updateDeck(game.getDeck());
 		
 		gameUpdater.createNextStep(game);
 		
 		return gameDao.gameUpdate(game.getGameId(), gameUpdater);
 		
 	}	
 	
 	private Game doProspector(Game game, PlayCoords playCoords, PlayChoice playChoice) {
 		GameUpdater gameUpdater = new GameUpdater(playCoords);
 		Play play = gameUpdater.getCurrentPlay(game);
 		
 		Player player = getCurrentPlayer(game);
 		
 		if (gameUpdater.getCurrentPhase(game).getLeadPlayer().equals(player.getName())) {
 			
 			int playerIndex = game.getPlayerIndex(player.getName());
 			
 			Integer prospectedCard = game.getDeck().takeOne();
 			player.addToHand(prospectedCard);
 			PlayOffered offered = new PlayOffered();
 			play.setOffered(offered);
 			offered.setProspected(Arrays.asList(prospectedCard));
 			gameUpdater.updateDeck(game.getDeck());
 			gameUpdater.updatePlayer(playerIndex, player);
 		}
 		
 		play.makePlay(playChoice);		
 		gameUpdater.completedPlay(play);
 		gameUpdater.createNextStep(game);
 		return gameDao.gameUpdate(game.getGameId(), gameUpdater);
 	}
 	
 	private Game doCouncillor(Game game, PlayCoords playCoords,
 			PlayChoice playChoice) {
 		
 		// TODO validate playChoice
 		
 		GameUpdater gameUpdater = new GameUpdater(playCoords);
 		Play play = gameUpdater.getCurrentPlay(game);
		game.getDeck().discard(play.getPlayChoice().getCouncilDiscardedAsArray());
 		gameUpdater.updateDeck(game.getDeck());
 		
 		String playerName = play.getPlayer();
 		Player player = game.getPlayer(playerName);
 		int playerIndex = game.getPlayerIndex(player.getName());
 
 		for (Integer offeredCard : play.getOffered().getCouncilOptions()) {
 			if (!playChoice.getCouncilDiscarded().contains(offeredCard)) {
 				player.addToHand(offeredCard);
 			}
 		}
 		gameUpdater.updatePlayer(playerIndex, player);
 		
 		play.makePlay(playChoice);		
 		gameUpdater.completedPlay(play);
 		gameUpdater.createNextStep(game);
 		return gameDao.gameUpdate(game.getGameId(), gameUpdater);
 	}	
 
 	private Player getCurrentPlayer(Game game) {
 		for (Player player : game.getPlayers()) {
 			if (userProvider.getAuthenticatedUsername().equals(player.getName())) {
 				return player;
 			}
 		}
 		throw new SanJuanUnexpectedException(String.format("Current user %s not one of the players in this game", userProvider.getAuthenticatedUsername()));
 	}
 
 	/* (non-Javadoc)
 	 * @see com.github.dansmithy.sanjuan.game.GameService#doCalculations(com.github.dansmithy.sanjuan.model.Game)
 	 */
 	@Override
 	public void doCalculations(Game game) {
 		for (Player player : game.getPlayers()) {
 			calculationService.processPlayer(player);
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see com.github.dansmithy.sanjuan.game.GameService#deleteGame(java.lang.Long)
 	 */
 	@Override
 	public void deleteGame(Long gameId) {
 		Game game = gameDao.getGame(gameId);
 		
 		String loggedInUser = userProvider.getAuthenticatedUsername();
 		if (!loggedInUser.equals(game.getOwner())) {
 			throw new NotResourceOwnerAccessException(String.format("Must be game owner to delete game."));
 		}
 		
 		gameDao.deleteGame(gameId);
 	}
 
 
 }
