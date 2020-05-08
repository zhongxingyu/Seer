 package game;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import game.cards.Card;
 import game.cards.Cards;
 import game.cards.Rank;
 import game.cards.SpitzerDeck;
 import game.cards.Suit;
 import game.player.SpitzerPlayer;
 
 import models.Game;
 import models.User;
 
 import org.codehaus.jackson.JsonNode;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 
 import play.libs.Json;
 import play.mvc.Content;
 import util.ErrorUtils;
 import util.GameError;
 
 public class SpitzerGameState
 {
 	public List<SpitzerPlayer> players;
 
 	public Integer maxPlayers;
 	public Integer currentDealer;
 	public Integer currentPlayer;
 	public Set<Integer> blackTeam;
 	public Set<Integer> otherTeam;
 	public Map<Card, Integer> trickPointsPerCard;
 	public GameStage stage;
 	public Integer trickNumber;
 	public Map<Card, Integer> trickCards;
 	public List<Card> trickCardsOrdered;
 	public List<List<Card>> trickCardsHistory;
 	public List<Card> trickWinningCardHistory;
 	public List<Integer> trickPointHistory;
 	public List<Integer> trickWinnerHistory;
 	public Set<Integer> playerCheckins;
 	public SpitzerDeclaration zolaDeclaration;
 	public Integer zolaPlayer;
 	public SpitzerDeclaration publicDeclaration;
 	public Integer declarePlayer;
 	
 	private void moveToStage(GameStage newStage)
 	{
 		switch(newStage)
 		{
 		case TRICK:
 			this.newTrick();
 			// If there was no winner in the last trick (ie, first trick), go to left of deal, else use the last winner
 			if(this.trickWinnerHistory.isEmpty())
 				this.currentPlayer = getNextPlayer(this.currentDealer).userId;
 			else
 				this.currentPlayer = this.trickWinnerHistory.get(trickWinnerHistory.size() - 1);
 			
 			break;
 			
 		case POST_TRICK:
 			this.completeTrick();
 			break;
 		case POST_ROUND:
 			this.completeTrick();
 			this.distributeGamePoints();
 			break;
 		case DECLARATION:
 			evaluateDeclarations();
 			break;
		case WAITING_TO_DEAL:
 			this.currentPlayer = this.currentDealer;
 			break;
 		}
 		
 		this.stage = newStage;
 	}
 
 	private void distributeGamePoints()
 	{
 		// Using the point history, distribute the game points accordingly
 		Integer totalTrickPoints = getTotalTrickPointsForPlayers(this.blackTeam);
 		Integer totalTricksWon = getTotalTricksWonByPlayers(this.blackTeam);
 
 		// TODO
 	}
 
 	private void increasePlayersGamePoints(Collection<Integer> userIds, Integer points)
 	{
 		for(SpitzerPlayer player : players)
 		{
 			if(userIds.contains(player.userId))
 				player.gamePoints += points;
 		}
 	}
 
 	private Integer getTotalTricksWonByPlayers(Collection<Integer> userIds)
 	{
 		Integer total = 0;
 		
 		for(Integer winnerId : this.trickWinnerHistory)
 		{
 			if(userIds.contains(winnerId))
 				total++;
 		}
 
 		return total;
 	}
 
 	private Integer getTotalTrickPointsForPlayers(Collection<Integer> userIds)
 	{
 		Integer total = 0;
 
 		for(int i = 0; i < this.trickWinnerHistory.size(); i++)
 		{
 			Integer trickWinner = this.trickWinnerHistory.get(i);
 
 			if(userIds.contains(trickWinner))
 				total += this.trickPointHistory.get(i);
 		}
 
 		return total;
 	}
 
 	// Called after a trick is over, or a round is over
 	private void completeTrick()
 	{
 		Card highestCard = SpitzerDeck.getWinningCard(trickCards.keySet());
 		Integer winner = trickCards.get(highestCard);
 		getPlayerByUser(winner).trickPoints = SpitzerDeck.getPointsForCards(trickCards.keySet());
 		this.trickPointsPerCard = SpitzerDeck.getPointsPerCards(trickCards.keySet());
 		this.trickWinnerHistory.add(trickCards.get(highestCard));
 		this.trickPointHistory.add(SpitzerDeck.getPointsForCards(trickCards.keySet()));
 		this.trickCardsHistory.add(trickCardsOrdered);
 		this.trickWinningCardHistory.add(highestCard);
 		this.playerCheckins = Sets.newHashSet();
 	}
 	
 	public JsonNode playCard(User user, Card card)
 	{
 		SpitzerPlayer player = getPlayerByUser(user);
 		
 		if(player == null)
 			return ErrorUtils.error(GameError.PLAYER_NOT_IN_GAME, user);
 		if(!this.stage.equals(GameStage.TRICK))
 			return ErrorUtils.error(GameError.INVALID_GAME_STAGE, this.stage);
 		if(!player.hand.hasCard(card))
 			return ErrorUtils.error(GameError.INVALID_CARD, player.hand,  card);
 		if(!currentPlayer.equals(player.userId))
 			return ErrorUtils.error(GameError.OUT_OF_TURN, player.userId, currentPlayer);
 		
 		// Validate the card placement
 		if(!validatePlayCard(player, card))
 			return ErrorUtils.error(GameError.INVALID_TRICK_PLAY, card);
 		
 		// Add the card to the trick Cards
 		player.hand.remove(card);
 		trickCards.put(card, player.userId);
 		trickCardsOrdered.add(card);
 		
 		this.currentPlayer = getNextPlayer(player).userId;
 		
 		// Have all the players played a card?
 		if(trickCards.size() >= players.size())
 			this.moveToStage(GameStage.POST_TRICK);
 		
 		return null;
 	}
 	
 	public JsonNode handleCheckin(User user)
 	{
 		if(this.stage == GameStage.POST_TRICK || this.stage == GameStage.POST_GAME)
 			this.playerCheckins.add(user.id);
 		else
 			return ErrorUtils.error(GameError.INVALID_GAME_STAGE, stage);
 		
 		// Is everyone checked in?
 		if(this.playerCheckins.size() == this.players.size())
 		{
 			// If we're out of cards, then the entire round is over
 			if(this.players.get(0).hand.isEmpty())
 				this.moveToStage(GameStage.POST_ROUND);
 			else
 				this.moveToStage(GameStage.TRICK);
 		}
 		
 		return null;
 	}
 	
 	public boolean validatePlayCard(SpitzerPlayer player, Card card)
 	{
 		return SpitzerDeck.getValidCardsForTrick(this.publicDeclaration, this.declarePlayer, player.userId, player.hand, trickCardsOrdered).contains(card);
 	}
 	
 	public JsonNode handleDeclaration(User user, SpitzerDeclaration declaration)
 	{
 		SpitzerPlayer player = getPlayerByUser(user);
 		
 		if(player == null)
 			return ErrorUtils.error(GameError.PLAYER_NOT_IN_GAME, user);
 		if(!this.stage.equals(GameStage.DECLARATION))
 			return ErrorUtils.error(GameError.INVALID_GAME_STAGE, this.stage);
 		if(!player.declarations.contains(declaration))
 			return ErrorUtils.error(GameError.INVALID_DECLARATION, declaration);
 		
 		// If there was already a zola call, and it conflicts, then invalid declaration
 		if(this.zolaDeclaration != null && SpitzerDeclaration.isZola(declaration))
 		{
 			// If the new zola declaration outranks the old, then it becomes the zola declaration
 			if(this.zolaDeclaration.isOutrankedBy(declaration))
 			{
 				this.zolaDeclaration = declaration;
 				this.zolaPlayer = user.id;
 			}
 			else
 			{
 				// This is an invalid declaration if it doesn't outrank the current one
 				return ErrorUtils.error(GameError.OUTRANKED_ZOLA, declaration);
 			}
 		}
 		else if(this.zolaDeclaration == null && SpitzerDeclaration.isZola(declaration))
 		{
 			// No previous zola declaration was made
 			this.zolaDeclaration = declaration;
 			this.zolaPlayer = user.id;
 		}
 		
 		// Valid declaration, assign it as active and remove options
 		player.activeDeclaration = declaration;
 		player.declarations.clear();
 
 		
 		// If everyone has declarations, progress the stage to in trick
 		if(allDeclarationsMade())
 		{
 			this.determineTeams();
 			this.moveToStage(GameStage.TRICK);
 		}
 		
 		return null;
 	}
 	
 
 	
 	private void determineTeams()
 	{
 		this.publicDeclaration = null;
 		this.blackTeam = Sets.newHashSet();
 		this.otherTeam = Sets.newHashSet();
 		
 		// If a zola declaration was made, then that player is on the black team
 		if(this.zolaDeclaration != null)
 		{
 			// Zolas are always public
 			this.publicDeclaration = this.zolaDeclaration;
 			this.declarePlayer = this.zolaPlayer;
 			this.blackTeam.add(this.zolaPlayer);
 		}
 		else
 		{
 			// If no zola declaration was made, then a sneaker could be played by 
 			// the queens player
 			for(SpitzerPlayer player : players)
 			{
 				if(player.activeDeclaration.equals(SpitzerDeclaration.SNEAKER))
 				{
 					this.blackTeam.add(player.userId);
 					break;
 				}
 			}
 			
 			// If no sneaker was played, check for an ace call
 			if(this.blackTeam.isEmpty())
 			{
 				for(SpitzerPlayer player : players)
 				{
 					Suit suit = SpitzerDeclaration.getSuitOfDeclaration(player.activeDeclaration);
 					if(suit != null)
 					{
 						// Find player with the suit
 						SpitzerPlayer acePlayer = null;
 						for(SpitzerPlayer playerAce : players)
 						{
 							if(playerAce.hand.hasCard(Rank.ACE, suit))
 							{
 								acePlayer = playerAce;
 								break;
 							}
 						}
 						
 						this.blackTeam.add(player.userId);
 						this.blackTeam.add(acePlayer.userId);
 						this.declarePlayer = player.userId;						
 						this.publicDeclaration = player.activeDeclaration;	// Calls are public						
 						break;
 					}
 				}
 			}
 			
 			// If no ace was called for, check for call for winner of first trick
 			if(this.blackTeam.isEmpty())
 			{
 				for(SpitzerPlayer player : players)
 				{
 					if(player.activeDeclaration.equals(SpitzerDeclaration.CALL_FOR_FIRST_TRICK_WINNER))
 					{
 						this.blackTeam.add(player.userId);
 						this.blackTeam.add(trickWinnerHistory.get(0));
 						this.declarePlayer = player.userId;						
 						this.publicDeclaration = player.activeDeclaration;
 						break;
 					}
 				}
 			}
 			
 			// If no calls were made at all, then the queens must be separated
 			if(this.blackTeam.isEmpty())
 			{
 				for(SpitzerPlayer player : players)
 				{
 					if(player.hand.hasCard(Card.QUEEN_OF_CLUBS) || player.hand.hasCard(Card.QUEEN_OF_SPADES))
 						this.blackTeam.add(player.userId);
 				}
 			}
 		}
 		
 		this.otherTeam.addAll(getOtherPlayers(this.blackTeam));
 	}
 	
 	private Collection<Integer> getOtherPlayers(Collection<Integer> playerIds)
 	{
 		Collection<Integer> allPlayers = Sets.newHashSet();
 		
 		for(SpitzerPlayer player : players)
 		{
 			allPlayers.add(player.userId);
 		}
 		
 		allPlayers.removeAll(playerIds);
 		
 		return allPlayers;
 	}
 	
 	public JsonNode dealCards(User user)
 	{
 		SpitzerPlayer userPlayer = getPlayerByUser(user);
 		
 		if(userPlayer == null)
 			return ErrorUtils.error(GameError.PLAYER_NOT_IN_GAME, user);
 		
 		if(!userPlayer.userId.equals(currentDealer))
 			return ErrorUtils.error(GameError.PLAYER_NOT_DEALER, Lists.newArrayList(currentDealer, userPlayer));
 		
 		if(!this.stage.equals(GameStage.WAITING_FOR_DEAL))
 			return ErrorUtils.error(GameError.GAME_NOT_FULL, user);
 		
 		SpitzerDeck deck = new SpitzerDeck();
 		deck.shuffle();
 		
 		// Deal all cards
 		while(!deck.cards.isEmpty())
 		{
 			for(SpitzerPlayer player : players)
 			{
 				if(deck.cards.isEmpty())
 					break;
 				player.addCardToHand(deck.draw());
 			}
 		}
 		
 		
 		this.moveToStage(GameStage.DECLARATION);
 		
 		return null;
 	}
 	
 	public SpitzerPlayer getNextPlayer(Integer userId)
 	{
 		return getNextPlayer(getPlayerByUser(userId));
 	}
 	
 	public SpitzerPlayer getNextPlayer(SpitzerPlayer player)
 	{
 		int index = players.indexOf(player);
 		if(index == players.size() - 1)
 			index = 0;
 		else
 			index++;
 		return players.get(index);
 	}
 	
 	public boolean allDeclarationsMade()
 	{
 		for(SpitzerPlayer player : players)
 		{
 			if(player.activeDeclaration == null)
 				return false;
 		}
 		
 		return true;
 	}
 	
 	public void evaluateDeclarations()
 	{
 		// Build each player's possible declarations, given their hand
 		for(SpitzerPlayer player : players)
 		{
 			// If ZOLA SCHNEIDER SCHWARTZ was declared, it is permanent for the entire game
 			if(player.activeDeclaration != null && player.activeDeclaration.equals(SpitzerDeclaration.ZOLA_SCHNEIDER_SCHWARTZ))
 			{
 				player.declarations = Lists.newArrayList(player.activeDeclaration);
 				continue;
 			}
 			
 			player.declarations = SpitzerDeclaration.buildFromHand(player.hand, this);
 		}
 	}
 	
 	private void newTrick()
 	{
 		this.trickNumber++;
 		this.trickCards = Maps.newHashMap();
 		this.trickCardsOrdered = Lists.newArrayList();
 		
 		for(SpitzerPlayer player : players)
 		{
 			player.trickPoints = 0;
 		}
 	}
 	
 	// Called when all tricks have been played for a dealing, set up a new dealing
 	public void newRound()
 	{
 		this.trickNumber = 0;
 		
 		// ZOLA SCHNEIDER SCHWARTZ runs through all games
 		if(zolaDeclaration == null || !zolaDeclaration.equals(SpitzerDeclaration.ZOLA_SCHNEIDER_SCHWARTZ))
 		{
 			this.zolaDeclaration = null;
 			this.zolaPlayer = null;
 		}
 		
 		this.publicDeclaration = null;
 		this.declarePlayer = null;
 		
 		// Reset all trick histories
 		this.trickWinnerHistory = Lists.newArrayList();
 		this.trickPointHistory = Lists.newArrayList();
 		this.trickCardsHistory = Lists.newArrayList();
 		this.trickWinningCardHistory = Lists.newArrayList();
 		
 		// Prepare the trick data structures;
 		newTrick();
 	}
 	
 	// Called when an entirely new game is created
 	public void startGame(Game game)
 	{
 		this.players = Lists.newArrayList();
 		this.stage = GameStage.WAITING_FOR_PLAYERS;
 		this.maxPlayers = Game.NUM_PLAYERS;
 
 		newRound();
 		
 		for(User userPlayer : game.players)
 		{
 			addPlayer(userPlayer);
 		}
 
 		// Host will be dealer to begin
 		this.currentDealer = game.hostUserId;
 	}
 	
 	public SpitzerPlayer getPlayerByUser(Integer id)
 	{
 		for(SpitzerPlayer player : players)
 		{
 			if(player.userId.equals(id))
 				return player;
 		}
 		
 		return null;
 	}
 	
 	public SpitzerPlayer getPlayerByUser(User user)
 	{
 		return getPlayerByUser(user.id);
 	}
 	
 	public void addPlayer(User user)
 	{
 		if(getPlayerByUser(user) != null)
 			return;
 		
 		SpitzerPlayer player = new SpitzerPlayer();
 		player.userId = user.id;
 		player.name = user.name;
 		this.players.add(player);
 		
 		if(this.players.size() >= Game.NUM_PLAYERS)
 			this.moveToStage(GameStage.WAITING_FOR_DEAL);
 	}
 
 	public static SpitzerGameState fromJson(String jsonString)
 	{
 		JsonNode node = Json.parse(jsonString);
 		return Json.fromJson(node, SpitzerGameState.class);
 	}
 	
 	public static enum GameStage
 	{
 		WAITING_FOR_PLAYERS(1),
 		WAITING_FOR_DEAL(2),
 		DECLARATION(3),
 		TRICK(4),
 		POST_TRICK(5),
 		POST_GAME(6),	// After a winner is found
 		POST_ROUND(7);	// After all tricks are played for a single deal
 		
 		private int id;
 		
 		private GameStage(int id)
 		{
 			this.id = id;
 		}
 		
 		public int getId()
 		{
 			return id;
 		}
 		
 		public static GameStage fromId(int id)
 		{
 			for(GameStage state : GameStage.values())
 				if(state.getId() == id)
 					return state;
 			
 			return null;
 		}
 	}
 }
