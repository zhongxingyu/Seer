 package skatgame;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * 
  * The game class contains all the logic for running a game of Skat. It manages
  * the code flow, sets up new games and validates user input to prevent invalid
  * user input or attempts at cheating.<br>
  * 
  */
 public class Game {
 	private static final int PLAYER_COUNT = 3;
 	private static final int[] LEGAL_BID_VALUES = { 18, 20, 22, 23, 24, 27, 30,
 			33, 35, 36, 40, 44, 45, 46, 48, 50, 54, 55, 59, 60, 63, 66, 70, 72,
 			77, 81, 84, 88, 90, 96, 99, 100, 108, 110, 120, 121, 132, 144, 160,
 			168, 192 };
 
 	private GameStats gameStats;
 	private GameStats.RoundStats roundStats;
 	
 	private int dealerIndex;
 	private int firstToPlayIndex;
 	private int highestBid;
 	private int declarerIndex; // Once bidding ends, highestBidder is the
 								// declarer. No need to save that twice.
 	private PlayerInfo[] players;
 	private Pile cardsPlayed;
 	private Pile skat;
 	private Deck deck;
 	private GameTypeOptions gameType;
 	private int multiplier;
 	
 	private int indentationLevel = 0; // Used to format the GameStats, should be incremented/decremented.
 
 	public Game() {
 		// Initialize our player array.
 		this.players = new PlayerInfo[PLAYER_COUNT];
 	}
 
 	/**
 	 * Initializes new instances of our player information for our players.
 	 */
 	private void resetPlayers(IPlayer player1, IPlayer player2, IPlayer player3) {
 		// Initialize our player instances.
 		players[0] = new PlayerInfo(player1);
 		players[1] = new PlayerInfo(player2);
 		players[2] = new PlayerInfo(player2);
 	}
 
 	/**
 	 * Tells each player their index.
 	 */
 	private void assignIndexes() {
 		// Loop through our player information, grab our player and set their
 		// index.
 		for (int i = 0; i < players.length; i++)
 			players[i].getPlayer().assignIndex(i);
 	}
 
 	/**
 	 * Create an empty skat pile and an empty pile for cards that have been
 	 * played, and create a populated deck.
 	 */
 	private void createGamePiles() {
 		skat = new Pile();
 		cardsPlayed = new Pile();
 		deck = new Deck();
 	}
 
 	/**
 	 * Deal three consecutive cards to each player, two cards to the skat, four
 	 * consecutive cards to each player, three consecutive cards to each player.
 	 */
 	private void dealCards() {
 		// Give three cards to each player.
 		for (int i = 0; i < players.length; i++)
 			deck.dealCards(players[i].getHandPile(), 3);
 
 		// Put two cards in the skat.
 		deck.dealCards(skat, 2);
 
 		// Give four cards to each player again.
 		for (int i = 0; i < players.length; i++)
 			deck.dealCards(players[i].getHandPile(), 4);
 
 		// Give three cards to each player again.
 		for (int i = 0; i < players.length; i++)
 			deck.dealCards(players[i].getHandPile(), 3);
 
 		// Note, we could've just dealt 10 cards straight up to the player..
 		// We didn't do this to maintain the traditional conventions, in-case
 		// it would've ended up with deducted marks on our part.
 	}
 
 	/**
 	 * Given a current bid integer, grabs the next possible bid, -1 if it's the last bid already.
 	 * @param currentBid The current bid we had, used to obtain the next bid.
 	 * @return Returns the next bid following currentBid.
 	 */
 	private int nextBid(int currentBid) {
 		// Loop for all of our bid values.
 		for (int i = 0; i < LEGAL_BID_VALUES.length; i++) {
 			// If we found the bid
 			if (LEGAL_BID_VALUES[i] == currentBid) {
 				// If it's the last bid, return -1.
 				if (LEGAL_BID_VALUES[i] == LEGAL_BID_VALUES[LEGAL_BID_VALUES.length - 1])
 					return -1;
 				// Otherwise return the next bid
 				return LEGAL_BID_VALUES[i + 1];
 			}
 		}
 		return -2; // Shouldn't ever happen
 	}
 
 	/**
 	 * Begins bidding between the three players. Assigns a declarer when bidding
 	 * ends.
 	 */
 	private void initiateBidding() {
 		int frontHand = (dealerIndex + 1) % PLAYER_COUNT;
 		int middleHand = (dealerIndex + 2) % PLAYER_COUNT;
 		int rearHand = (dealerIndex + 3) % PLAYER_COUNT;
 
 		boolean frontHandWonFirst = true; // set these defaults to the most
 											// likely (least likely to have to
 											// change them)
 		boolean rearHandWon = false;
 
 		int currentBid = 18; //
 		int who = -1; // the person who made the highest bid so far. -1 means no
 						// bid yet.
 
 		// bid between middleHand and frontHand, until one of them passes.
 		while (true) {
 			PlayerInfo pi = players[middleHand];
 			IPlayer player = pi.getPlayer();
 			boolean canMatch = !(who == frontHand); // we can match so long as
 													// frontHand doesn't hold
 													// the highest bid
 
 			if (player.bid(currentBid, pi.getHandPile(), canMatch)) {
 				if (canMatch)
 					who = middleHand;
 				else {
 					int t = nextBid(currentBid);
 					if (t == -1) {
 						// Report non-critical error
 						this.roundStats.log("Player " + (middleHand + 1) +
 						 " forced to pass, as they cannot bid that high", this.indentationLevel);
 						break; // frontHand won this bidding, and it's defaulted
 								// to true
 					} else {
 						currentBid = t;
 						who = middleHand;
 					}
 				}
 
 			} else
 				break; // frontHand won this bidding, and it's defaulted to true
 
 			pi = players[frontHand];
 			player = pi.getPlayer();
 			canMatch = true;
 
 			if (player.bid(currentBid, pi.getHandPile(), canMatch))
 				who = frontHand;
 			else {
 				frontHandWonFirst = false; // tell the next loop that front hand
 											// lost
 				break;
 			}
 		}
 		// one person has passed
 		int otherBidder = (frontHand
 				+ ((!frontHandWonFirst ? 1 : 0)))  % PLAYER_COUNT;
 
 		while (true) {
 			PlayerInfo pi = players[rearHand];
 			IPlayer player = pi.getPlayer();
 			boolean canMatch = !(who == otherBidder);
 
 			if (player.bid(currentBid, pi.getHandPile(), canMatch)) {
 				if (canMatch)
 					who = rearHand;
 				else {
 					int t = nextBid(currentBid);
 					if (t == -1) {
 						// Report non-critical error
 						this.roundStats.log("Player " + (middleHand + 1) +
 						 " forced to pass, as they cannot bid that high", this.indentationLevel);
 						break; // frontHand won this bidding, and it's defaulted
 								// to true
 					} else {
 						currentBid = t;
 						who = rearHand;
 					}
 				}
 			} else
 				break; // rearHandWon=false;
 
 			pi = players[otherBidder];
 			player = pi.getPlayer();
 			canMatch = true;
 
 			if (player.bid(currentBid, pi.getHandPile(), canMatch))
 				who = otherBidder;
 			else {
 				rearHandWon = true;
 				break;
 			}
 		}
 
 		if (rearHandWon) {
 			declarerIndex = rearHand;
 		} else {
 			if (frontHandWonFirst) {
 				declarerIndex = frontHand;
 			} else {
 				declarerIndex = middleHand;
 			}
 		}
 
 	}
 
 	/**
 	 * Defines the type of game to be played, including all applicable
 	 * variables. Sets the base multiplier until later we can find out if schwarz
 	 * or schneider was achieved.
 	 */
 	private void setGameType() {
 		// Ask the declarer (highestBidder) if they want to see the skat.
 		PlayerInfo declarerInfo = this.players[this.declarerIndex];
 		IPlayer declarerPlayer = declarerInfo.getPlayer();
 		Pile declarerHand = declarerInfo.getHandPile();
 		boolean viewSkat = declarerPlayer.decideTakeSkat(declarerHand.copy());
 
 		this.roundStats.log("Declarer looked at skat = " + viewSkat, this.indentationLevel);
 		
 		// If they want to view the skat..
 		if (viewSkat) {
 			// Let them decide on what cards to take.
 			Pile newSkat = declarerPlayer.giveSkat(skat.copy(), declarerHand.copy());
 
 			// Verify skat length.
 			if (newSkat.getNumCards() != 2) {
 				// Skat is invalid size, report critical error to stats.
 				this.roundStats.logError("Declarer did not return the correct number of cards to the skat", true, this.indentationLevel);
 			}
 
 			// Loop through the cards of the old skat
 			int z = 0;
 			for (int i = 0; i < skat.getNumCards(); i++) {
 				// If the new skat doesn't contain an old skat card.
 				if (!newSkat.containsCard(skat.getCard(i))) {
 					Card cardReplaced = skat.getCard(i);
 					Card replacedBy = null;
 					// There must've been a card we replaced it with.
 					for (int x = z; x < newSkat.getNumCards(); x++, z++) {
 						if (!skat.containsCard(newSkat.getCard(i))) {
 							replacedBy = newSkat.getCard(i);
 							break;
 						}
 					}
 
 					// Error checking, make sure the replacedBy card is in
 					// declarerHand..
 					if (!declarerHand.containsCard(replacedBy)) {
 						// The new card in the skat wasn't in declarer
 						// hand.
 						this.roundStats.logError("Declarer returned a card to the skat that was not in their hand", true, this.indentationLevel);
 					}
 
 					// Swap the cards in skat with the hand.
 					skat.replaceCards(cardReplaced, replacedBy);
 					declarerHand.replaceCards(replacedBy, cardReplaced);
 					
 					this.roundStats.log("Declarer replaced card " + cardReplaced.toString() + " in skat with " + replacedBy.toString() + ".", this.indentationLevel);
 				}
 			}
 		}
 
 		// Ask the declarer which type of game they want to play.
 		GameTypeOptions chosenGameType = declarerPlayer
 				.decideGameType(declarerHand.copy());
 
 		// Validate the gametype, fix up if necessary.
 		this.gameType = this.validateGameType(chosenGameType, viewSkat);
 		
 		// Spit out our gametype information.
 		this.roundStats.log("Starting (" + this.gameType.getHandType() + ", " + this.gameType.getGameType() + ")-game.", this.indentationLevel);
 		this.roundStats.log("(Trump Suit = " + this.gameType.getTrumpSuit() + " / Ouvert = " + this.gameType.getOuvert() + 
 				" / Schwarz = " + this.gameType.getSchwarz() + " / Schneider = " + this.gameType.getSchneider() + ")", this.indentationLevel);
 
 		
 		// Update multiplier (we'll later need to add a point each if the user achieved schwarz or schneider)
		this.multiplier = this.countMatadors() + 1; // Min multiplier for non null is 2.
 		
 		// Add to our multiplier if it's a hand game.
 		if(this.gameType.getHandType() == GameTypeOptions.SkatHandType.Hand)
 			multiplier++;
 		
 		// If it's ouvert, add to multiplier
 		if(this.gameType.getOuvert())
 			multiplier++;
 		
 		// If it's schneider, add to multiplier
 		if(this.gameType.getSchneider())
 			multiplier++;
 		
 		// If it's schwarz, add to multiplier
 		if(this.gameType.getSchwarz())
 			multiplier++;
 		
 		// NOTE: Base values will be calculated in gameValue() and multiplied by multiplier.
 
 		// Push GameTypeOptions object to each player.
 		for (PlayerInfo pi : this.players)
 			pi.getPlayer().setGameType(this.gameType, this.declarerIndex);
 	}
 
 	/**
 	 * Determines if the given game type is valid based on the rules of Skat.
 	 * 
 	 * @param gameType
 	 *            the type of game to be evaluated
 	 * @param tookSkat
 	 *            indicates if the user took the skat.
 	 * @return Returns the game-type options, fixed if required.
 	 */
 	private GameTypeOptions validateGameType(GameTypeOptions gameType, boolean tookSkat) {
 		// Get our options
 		GameTypeOptions.TrumpSuit trump = gameType.getTrumpSuit();
 		GameTypeOptions.SkatHandType skathandType = gameType.getHandType();
 		GameTypeOptions.GameType actualGameType = gameType.getGameType();
 		boolean ouvert = gameType.getOuvert();
 		boolean schwarz = gameType.getSchwarz();
 		boolean schneider = gameType.getSchneider();
 
 		// Can't declare it as a hand game if you took the skat
 		if (skathandType == GameTypeOptions.SkatHandType.Hand && tookSkat) {
 			// Report non-critical error (SHOULDN'T DECLARE HAND GAME IF YOU TOOK SKAT)
 			this.roundStats.logError("Declared hand game when declarer took skat. Setting to skat game instead", this.indentationLevel);
 			skathandType = GameTypeOptions.SkatHandType.Skat;
 			
 		} else if (skathandType == GameTypeOptions.SkatHandType.Skat
 				&& !tookSkat) {
 			// Report non-critical error (SHOULDN'T DECLARE SKAT GAME IF YOU DIDN'T TAKE SKAT)
 			this.roundStats.logError("Declared skat game when declarer didn't take skat. Setting to hand game instead", this.indentationLevel);
 			skathandType = GameTypeOptions.SkatHandType.Hand;
 		}
 
 		// Can't be a suit game without a suit.
 		if (actualGameType == GameTypeOptions.GameType.Suit
 				&& trump == GameTypeOptions.TrumpSuit.None) {
 			// Report critical error (CAN'T BE SUIT GAME WITH NO SUIT)
 			this.roundStats.logError("Cannot have a suit game without a trump suit.. Setting trump suit to Clubs", this.indentationLevel);
 			trump = GameTypeOptions.TrumpSuit.Clubs;
 		}
 
 		// Can't be a null or grand game with a suit.
 		if (((actualGameType == GameTypeOptions.GameType.Grand) | (actualGameType == GameTypeOptions.GameType.Null))
 				&& trump != GameTypeOptions.TrumpSuit.None) {
 			// Report critical error (CAN'T BE GRAND OR NULL GAME WITH A TRUMP SUIT)
 			this.roundStats.logError("Cannot have a " + actualGameType.toString() + " game with a trump suit. Setting trump suit to None", this.indentationLevel);
 			trump = GameTypeOptions.TrumpSuit.None;
 		}
 
 		// If it's a skat game, and not null, you can't declare anything of
 		// schwarz/schneider/ouvert.
 		if ((skathandType == GameTypeOptions.SkatHandType.Skat && actualGameType != GameTypeOptions.GameType.Null)
 				&& (schwarz | schneider | ouvert)) {
 			// Report non-critical error (CAN'T DECLARE SCHWARZ/SCHNEIDER/OUVERT IN SKAT GAME)
 			this.roundStats.logError("Cannot have a Skat, non-Null game with schwarz, schneider or ouvert. Setting all to false", this.indentationLevel);
 			schwarz = false;
 			schneider = false;
 			ouvert = false;
 		}
 
 		// If it's a null game, you can't declare schwarz and schneider.
 		if (actualGameType == GameTypeOptions.GameType.Null
 				&& (schwarz | schneider)) {
 			// Report non-critical error (CAN'T DECLARE NULL GAME AND SCHWARZ/SCHNEIDER)
 			this.roundStats.logError("Cannot have a null game and schwarz or schneider. Setting both to false", this.indentationLevel);
 			schwarz = false;
 			schneider = false;
 		}
 
 		// If they have ouvert true, you can't have schwarz false
 		if (actualGameType != GameTypeOptions.GameType.Null && ouvert && !schwarz) {
 			// Report non-critical error (CAN'T DECLARE OUVERT AND NOT SCHWARZ) (we fix it up).
 			this.roundStats.logError("Cannot have a ouvert game without schwarz (unless null). Setting Schwarz to true", this.indentationLevel);
 			schwarz = true;
 		}
 
 		// If they have schwarz true, you can't have schneider false
 		if (actualGameType != GameTypeOptions.GameType.Null && schwarz && !schneider) {
 			// Report non-critical error (CAN'T DECLARE SCHWARZ AND NOT SCHNEIDER) (we fix it up).
 			this.roundStats.logError("Cannot have a schwarz game without schneider (unless null). Setting schneider to true", this.indentationLevel);
 			schneider = true;
 		}
 		
 		return new GameTypeOptions(skathandType, actualGameType, trump, ouvert, schneider, schwarz);
 	}
 
 	/**
 	 * Our helper function used to count the declarers with or without matadors.
 	 * @return Returns the count of with or without matadors.
 	 */
 	private int countMatadors() {
 		// If it's a null game, we don't have multipliers, skip this.
 		if(this.gameType.getGameType() == GameTypeOptions.GameType.Null)
 			return 0;
 		
 		// Create our trump card list.
 		List<Card> trumpList = new ArrayList<Card>();
 		trumpList.add(new Card(Card.CARD_SUIT.CLUBS, Card.FACE_VALUE.JACK));
 		trumpList.add(new Card(Card.CARD_SUIT.SPADES, Card.FACE_VALUE.JACK));
 		trumpList.add(new Card(Card.CARD_SUIT.HEARTS, Card.FACE_VALUE.JACK));
 		trumpList.add(new Card(Card.CARD_SUIT.DIAMONDS, Card.FACE_VALUE.JACK));
 		
 		// If it's a Suit game, we have more trump cards.
 		if(this.gameType.getGameType() == GameTypeOptions.GameType.Suit) {
 			// Get our trump suit (this enum is the same, except the None option in the beginning, so we subtract 1 to index it)
 			Card.CARD_SUIT trumpSuit = Card.CARD_SUIT.values()[this.gameType.getTrumpSuit().ordinal() - 1];
 			trumpList.add(new Card(trumpSuit, Card.FACE_VALUE.ACE));
 			trumpList.add(new Card(trumpSuit, Card.FACE_VALUE.TEN));
 			trumpList.add(new Card(trumpSuit, Card.FACE_VALUE.KING));
 			trumpList.add(new Card(trumpSuit, Card.FACE_VALUE.QUEEN));
 			trumpList.add(new Card(trumpSuit, Card.FACE_VALUE.NINE));
 			trumpList.add(new Card(trumpSuit, Card.FACE_VALUE.EIGHT));
 			trumpList.add(new Card(trumpSuit, Card.FACE_VALUE.SEVEN));
 		}
 		
 		// Get our game hand and game options to determine what to check.
 		GameTypeOptions.SkatHandType handGameType = this.gameType.getHandType();
 		Pile declarerHand = this.players[this.declarerIndex].getHandPile();
 		
 		// Create our matador count and our boolean to check if we are counting with or without.
 		int matadorCount = 0;
 		boolean countingWith = true;
 		
 		// Loop through all the trump cards we're checking if we have or don't have.
 		for(Card curTrumpCard : trumpList) {
 			// Check our declarers hand.
 			boolean foundCard = declarerHand.containsCard(curTrumpCard);
 			
 			// Check our skat pile if it's a skat game, and we haven't found the card
 			if(handGameType == GameTypeOptions.SkatHandType.Skat && !foundCard)
 				// Check our skat pile
 				foundCard = this.skat.containsCard(curTrumpCard);
 			
 			// If it's the first time we're here, we should set if we're counting with or without.
 			if(matadorCount == 0)
 				countingWith = foundCard;
 			
 			// Check our with/without status
 			if(countingWith == foundCard)
 				matadorCount++;
 			else
 				break;
 		}
 		return matadorCount;
 	}
 	
 	/**
 	 * Each player plays a card into the cardsPlayed Pile.
 	 */
 	private void playTrick() {
 		// Create a player index
 		int playerIndex = firstToPlayIndex;
 
 		// Loop for each player..
 		for (int i = 0; i < PLAYER_COUNT; i++) {
 			// Get the player
 			PlayerInfo curPlayerInfo = players[playerIndex];
 			IPlayer curPlayer = curPlayerInfo.getPlayer();
 			Pile curHand = curPlayerInfo.getHandPile();
 			Card playedCard = null;
 
 			// We'll let our user try to play a valid card as many times as
 			// cards they have..
 			for (int x = 0; x < curHand.getNumCards(); x++) {
 				playedCard = curPlayer.playTurn(curHand.copy(),
 						cardsPlayed.copy(), firstToPlayIndex);
 
 				// Check if the card is valid, if it isn't, we make them try
 				// again. (If it's not in their hand, this function will critical error).
 				if (!isValidCard(playedCard)) {
 					// Report non-critical error (played invalid card, we'll let them try again).
 					this.roundStats.logError("Player " + (playerIndex + 1) + " played an invalid card (" + playedCard.toString() + ")", this.indentationLevel);
 					playedCard = null;
 				} else
 					break;
 			}
 
 			// If our player somehow didn't pick a card by now, our strategy is
 			// a total failure..
 			if (playedCard == null) {
 				// Report critical error (player couldn't pick a valid card after multiple tries).
 				this.roundStats.logError("Player " + (playerIndex + 1) + " repetitively could not play a valid card", true, this.indentationLevel);
 			}
 
 			// Remove the card from their hand pile, add it to cardsPlayed
 			curHand.removeCard(playedCard);
 			cardsPlayed.addCard(playedCard);
 			
 			// Log our played card.
 			this.roundStats.log("Player " + (playerIndex + 1) + " played " + playedCard.toString(), this.indentationLevel);
 
 			// Increment our player index.
 			playerIndex = (playerIndex + 1) % PLAYER_COUNT;
 		}
 	}
 
 	/**
 	 * Determines if the given card is valid.
 	 * 
 	 * @param card
 	 *            the Card to evaluate.
 	 * @return True if the card is value, and False otherwise.
 	 */
 	private boolean isValidCard(Card playedCard) {
 		// Grab our player index who wishes to play this card.
 		int curPlayerIndex = (this.firstToPlayIndex + this.cardsPlayed.getNumCards()) % PLAYER_COUNT;
 		Pile curPlayerHand = this.players[curPlayerIndex].getHandPile();
 		
 		// If our card isn't even in our hand, there's a real problem
 		if (!curPlayerHand.containsCard(playedCard)) {
 		    // Report critical error (player chose a card they didn't even have to play for their trick...)
 		    this.roundStats.logError("Player " + (curPlayerIndex + 1) + " chose to play a card they didn't have in their hand", true, this.indentationLevel);
 		}
 		
 		// If leading suit hasn't been played, we can play anything.
 		if(this.cardsPlayed.getNumCards() == 0)
 			return true;
 		
 		// Leading suit has been played.. Get the suit
 		Card.CARD_SUIT leadingSuit = this.cardsPlayed.getCard(0).getSuit();
 		
 		// Check if the user's hand has a card of this suit.
 		boolean hasSuit = false;
 		for(int i = 0; i < curPlayerHand.getNumCards(); i++)
 			if(curPlayerHand.getCard(i).getSuit() == leadingSuit) {
 				hasSuit = true;
 				break;
 			}
 		
 		// If we don't have the suit, play anything.
 		if(!hasSuit)
 			return true;
 		
 		// We do have the suit, so lets make sure they played a card of it..
 		return playedCard.getSuit() == leadingSuit;
 	}
 
 
 	/**
 	 * Determine which player wins the trick, move the cards they won to their
 	 * TricksWon pile, and let each player know who won the trick and which
 	 * cards they won.
 	 */
 	private void winTrick() {
 		// Examine cardsPlayed pile and determine which player won the trick.
 		int winnerIndex = determineWinner();
 		
 		// Change firstToPlay to be the index of the player who won the trick.
 		this.firstToPlayIndex = winnerIndex;
 		
 		// Show each player who won the trick and which cards were played in the
 		// trick (call endTrick on each player).
 		for(int i = 0; i < PLAYER_COUNT; i++) {
 			IPlayer playerInstance = this.players[i].getPlayer();
 			playerInstance.endTrick(this.cardsPlayed, winnerIndex);
 		}
 		
 		// Move cards in cardsPlayed pile to the TricksWon pile of the player
 		// who won.
 		Pile winnerTricksWonPile = this.players[winnerIndex].getTricksWonPile();
 		while(this.cardsPlayed.getNumCards() > 0) {
 			Card removedCard = this.cardsPlayed.removeCard(0);
 			winnerTricksWonPile.addCard(removedCard);
 		}
 		
 		// Log our winner
 		this.roundStats.log("Trick won by Player " + (winnerIndex + 1), this.indentationLevel);
 	}
 
 	/**
 	 * Given a pile of played cards, determines the index of the winner.
 	 * @param cardsPlayedPile The pile of cards that has been played for this trick.
 	 * @return The index of the winner for these pile of cards played.
 	 */
 	private int determineWinner() {
 		// Create our winning card variable.
 		Card winningCard = null;
 		
 		// Lets make a list to store our strongest cards till we eliminate all but one.
 		List<Card> list = new ArrayList<Card>();
 		Card.CARD_SUIT leadingSuit = this.cardsPlayed.getCard(0).getSuit();
 		boolean notNull = this.gameType.getGameType() != GameTypeOptions.GameType.Null;
 		
 		// Loop through all the cards..
 		for(int i = 0; i < this.cardsPlayed.getNumCards(); i++) {
 			// Get this card
 			Card curCard = this.cardsPlayed.getCard(i);
 			
 			// Only add special cards to our initial list.
 			boolean isLeadingSuit = curCard.getSuit() == leadingSuit;
 			boolean specialJack = (curCard.getFaceValue() == Card.FACE_VALUE.JACK && this.gameType.getGameType() != GameTypeOptions.GameType.Null);
 			boolean isTrumpSuit = false;
 			// If it's not null gametype, check for trump suit.
 			if(notNull)
 				if(this.gameType.getTrumpSuit() != GameTypeOptions.TrumpSuit.None)
 					if(curCard.getSuit() == Card.CARD_SUIT.values()[this.gameType.getTrumpSuit().ordinal() - 1])
 						isTrumpSuit = true;
 			
 			if(isLeadingSuit || specialJack  || isTrumpSuit)
 				list.add(curCard);
 		}
 		
 		// If we have other cards we weren't able to eliminate as non-leading
 		// suit, we'll continue eliminating.
 		if(list.size() != 1)
 		{
 			// If it's not null, check for jacks..
 			if(notNull) 
 			{
 				// Check for jacks..
 				// -----------------------------------------------------
 				boolean jackExists = false;
 				for(Card curCard : list)
 					if(curCard.getFaceValue() == Card.FACE_VALUE.JACK) {
 						jackExists = true;
 						break;
 					}
 				// If it exists, find the strongest.
 				if(jackExists)
 					for(Card.CARD_SUIT suitCheck : Card.CARD_SUIT.values()) {
 						for(Card curCard : list)
 							// If it's of the stronger jack suits (enum values represent the order of strongest).
 							if(curCard.getSuit() == suitCheck && curCard.getFaceValue() == Card.FACE_VALUE.JACK) {
 								// Set the winning card and break out of here to save time.
 								winningCard = curCard;
 								break;
 							}
 						// If we found our card, break out of here too.
 						if(winningCard != null)
 							break;
 					}
 				
 				// If we didn't find our card as a jack, let's remove any non trump cards if we have any.
 				// ----------------------------------------------------
 				// Check for trump suit if it's a suit game and we haven't found our winner.
 				if(this.gameType.getGameType() == GameTypeOptions.GameType.Suit && winningCard == null) {
 					// Check for trump suit.
 					boolean trumpExists = false;
 					for(Card curCard : list)
 						if(curCard.getSuit() == Card.CARD_SUIT.values()[this.gameType.getTrumpSuit().ordinal() - 1]) {
 							trumpExists = true;
 							break;
 						}
 					
 					// If it exists, eliminate all in the list which aren't trump.
 					if(trumpExists)
 					{
 						Card.CARD_SUIT trumpSuit = Card.CARD_SUIT.values()[this.gameType.getTrumpSuit().ordinal() - 1];
 						for(int i = 0; i < list.size(); ) {
 							if(list.get(i).getSuit() != trumpSuit) {
 								list.remove(i);
 							}
 							else
 								i++;
 						}
 					}
 					// If we only have one card, set it as our winning card..
 					if(list.size() == 1)
 						winningCard = list.get(0);
 				}
 				
 				// If we still didn't find the card, we check by card strength..
 				// ---------------------------------------------------------------
 				if(winningCard == null) {
 					// Check by card strength using our array of strongest to weakest.
 					Card.FACE_VALUE[] strongFacesList = { Card.FACE_VALUE.ACE, Card.FACE_VALUE.TEN, Card.FACE_VALUE.KING, 
 							Card.FACE_VALUE.QUEEN, Card.FACE_VALUE.NINE, Card.FACE_VALUE.EIGHT,
 							Card.FACE_VALUE.SEVEN };
 					
 					// Loop till we find the first card in our list..
 					for(Card.FACE_VALUE strongFace : strongFacesList) {
 						for(Card curCard : list)
 							if(curCard.getFaceValue() == strongFace) {
 								// Set it was the winning card and break out of here.
 								winningCard = curCard;
 								break;
 							}
 						// If the winning card was found, break out of here too
 						if(winningCard != null)
 							break;
 					}
 				}
 			}
 			else
 			{
 				// If it is a null game, we simply find the strongest card according to this array of strongest-weakest..
 				//Ace, King, Queen, Jack, 10, 9, 8, 7
 				Card.FACE_VALUE[] strongFacesList = { Card.FACE_VALUE.ACE, Card.FACE_VALUE.KING, Card.FACE_VALUE.QUEEN, 
 						Card.FACE_VALUE.JACK, Card.FACE_VALUE.TEN, Card.FACE_VALUE.NINE, Card.FACE_VALUE.EIGHT,
 						Card.FACE_VALUE.SEVEN };
 				
 				// Loop till we find the first card in our list..
 				for(Card.FACE_VALUE strongFace : strongFacesList) {
 					for(Card curCard : list)
 						if(curCard.getFaceValue() == strongFace) {
 							// Set it was the winning card and break out of here.
 							winningCard = curCard;
 							break;
 						}
 					// If the winning card was found, break out of here too
 					if(winningCard != null)
 						break;
 				}
 			}
 		}
 		else
 			winningCard = list.get(0);
 		
 		// By now we should have the winning card.. lets find out the index..
 		int indexOfWinningCard = -1;
 		for(int i = 0; i < this.cardsPlayed.getNumCards(); i++)
 			if(this.cardsPlayed.getCard(i) == winningCard) {
 				indexOfWinningCard = i;
 				break;
 			}
 		
 		// Shouldn't be -1.. Will likely never happen but to be safe we'll include it here.
 		if(indexOfWinningCard == -1) {
 			// Report error.
 			this.roundStats.logError("Could not determine winner somehow.. This is a Game error, not Player", true, this.indentationLevel);
 		}
 		
 		// We now can just add this index on-top of whoever played first to find out who wins..
 		return (this.firstToPlayIndex + indexOfWinningCard) % PLAYER_COUNT;
 	}
 
 	/**
 	 * Determine how many card points a given player has won.
 	 * 
 	 * @param playerIndex
 	 *            The player whose card points to count.
 	 * @return The total card points the player has won.
 	 */
 	private int countCardPoints(int playerIndex) {
 		// Get our number of cards
 		int sum = 0;
 		int numCards = players[playerIndex].getTricksWonPile().getNumCards();
 
 		// Loop through each card for this player.
 		for (int i = 0; i < numCards; i++) {
 			// Add to our sum
 			sum += players[playerIndex].getTricksWonPile().getCard(i)
 					.getPointValue();
 		}
 
 		// Return our sum
 		return sum;
 	}
 
 	/**
 	 * Determine the value of the game.
 	 * 
 	 * @param baseVal
 	 *            The base value.
 	 * @param multiplier
 	 *            The multiplier.
 	 * @return The game value.
 	 */
 	private int gameValue() {
 		// Calculate the base value for the game
 		int baseVal = 24; // start off with grand game value.
 		
 		// Grab our gametype, and if it's not grand, calculate the values.
 		GameTypeOptions.GameType actualGameType = this.gameType.getGameType();
 		if(actualGameType != GameTypeOptions.GameType.Grand) {
 			// If it's a suit game, set the values
 			if(actualGameType == GameTypeOptions.GameType.Suit) {
 				// Get the index of our option, and subtract 1 since we won't have the first None option.
 				int trumpSuitIndex = this.gameType.getTrumpSuit().ordinal() - 1; 
 				// Our enum options are descending in terms of base value from 12.
 				baseVal = 12 - trumpSuitIndex;
 			}
 			else
 			{
 				// Grab some options used to calculate base value for Null.
 				boolean optHand = (this.gameType.getHandType() == GameTypeOptions.SkatHandType.Hand);
 				boolean optOuvert = this.gameType.getOuvert();
 				
 				// Calculate our base value based off these options..
 				if(!optHand && !optOuvert)
 					// Null
 					baseVal = 23;
 				else if(optHand && !optOuvert)
 					// Null Hand
 					baseVal = 35;
 				else if(!optHand && optOuvert)
 					// Null Ouvert
 					baseVal = 46;
 				else
 					// Null Ouvert Hand
 					baseVal = 59;
 			}
 		}
 		// Return the game value.
 		return baseVal * multiplier;
 	}
 
 	/**
 	 * Add or subtract points from the declarer's GameScore.
 	 */
 	private void assignGamePoints() {
 		// Get our card points and game value.
 		int curCardPoints = countCardPoints(declarerIndex);
 		int curGameValue = gameValue();
 		
 		// Grab our player and tricks won pile for them.
 		PlayerInfo player = players[declarerIndex];
 		Pile pile = player.getTricksWonPile();
 		
 		// There are 4 possible win conditions:
 		boolean wonGame = false;
 		if (gameType.getGameType() == GameTypeOptions.GameType.Null) {
 			// If it's null, and declarer won no tricks.
 			wonGame = pile.getNumCards() == 0;
 		} else if (gameType.getSchwarz()) {
 			// If schwarz and declarer won all tricks.
 			wonGame = (pile.getNumCards() == 30);
 		} else if (gameType.getSchneider()) {
 			// If schneider and declarer won at-least 90 card points.
 			wonGame = (curCardPoints >= 90 && curGameValue >= highestBid);
 		} else {
 			// If any other case, declarer must've won over 60 points.
 			wonGame = (curCardPoints > 60 && curGameValue >= highestBid);
 		}
 		
 		// If we won the game, add to score, otherwise subtract
 		if(wonGame) {
 			player.setGameScore(player.getGameScore() + curGameValue);
 			this.roundStats.log("The declarer has won " + curGameValue + " points this round.", this.indentationLevel);
 		} else {
 			player.setGameScore(player.getGameScore() - (2 * curGameValue));
 			this.roundStats.log("The declarer has lost " + (2 * curGameValue) + " points this round.", this.indentationLevel);
 		}
 		
 		// Let our players know the round stats (we used to not have rounds, so this was called game stats, whoops..)
 		int[] endGameScores = new int[PLAYER_COUNT];
 		for(int i = 0; i < endGameScores.length; i++)
 			endGameScores[i] = this.players[i].getGameScore();
 		for(PlayerInfo playerInfo : this.players)
 			playerInfo.getPlayer().endGameInfo(wonGame, endGameScores);
 	}
 
 	/**
 	 * Performs general setup necessary to start a game of skat.
 	 */
 	public void setupGame(IPlayer player1, IPlayer player2, IPlayer player3) {
 		
 		// Initialize our game stats
 		this.gameStats = new GameStats();
 		
 		// Setup our players
 		this.resetPlayers(player1, player2, player3);
 		this.assignIndexes();
 
 		// When we start a round, we will increment dealer, and we want the
 		// first round to be with dealer = 0..
 		dealerIndex = -1;
 	}
 
 	/**
 	 * The main playing aspect of the game. Manages bidding, and all ten tricks
 	 * of a game of skat. Performs end-of-game calculations and cleanup.
 	 */
 	public void playRound() {
 		// Reset our players piles (hand and won pile) -- necessary if this is
 		// not the first game played with these Players.
 		for (int i = 0; i < players.length; i++)
 			players[i].resetPiles();
 
 		// Create the game piles
 		this.createGamePiles();
 
 		// Increment our dealer.
 		dealerIndex = (dealerIndex + 1) % PLAYER_COUNT;
 
 		// Shuffle and deal cards
 		this.deck.shuffle();
 		this.dealCards();
 
 		// Mark the start of a new round
 		this.roundStats = this.gameStats.createNewRound();
 		this.roundStats.setRoundStart();
 		
 		// Manage bidding and game type declaration.
 		this.initiateBidding();
 		this.setGameType();
 
 		// Play the 10 tricks of a game of skat.
 		for (int i = 0; i < 10; i++) {
 			// Log our header for our trick.
 			this.roundStats.log("Trick " + (i + 1) + ": ", this.indentationLevel);
 			
 			// Play the trick and determine who won.
 			this.indentationLevel++;
 			this.playTrick();
 			this.winTrick();
 			this.indentationLevel--;
 			
 			// If it's a null game, if declarer wins a trick, it's game over for them.
 			if(this.gameType.getGameType() == GameTypeOptions.GameType.Null)
 				if(firstToPlayIndex == declarerIndex)
 				{
 					this.roundStats.log("Declarer won a trick in a null game. It's an early game over.", this.indentationLevel);
 					break;
 				}
 		}
 
 		// Now that the tricks have been played, check if the declarer achieved schwarz or schneider.
 		
 		// If it's a null game, reset our multiplier.
 		if(this.gameType.getGameType() == GameTypeOptions.GameType.Null)
 			multiplier=1;
 		else {
 			// Check if schneider was achieved
 			if(this.countCardPoints(this.declarerIndex) >= 90) {
 				multiplier++;
 				this.roundStats.log("Schneider was achieved in a non-null game!", this.indentationLevel);
 			}
 			
 			// Check if schwarz was achieved
 			if(this.players[this.declarerIndex].getTricksWonPile().getNumCards() == 30) {
 				multiplier++;
 				this.roundStats.log("Schwarz was achieved in a non-null game!", this.indentationLevel);
 			}
 		}
 		
 		// Once 10 tricks have been played, count card points, determine whether
 		// or not the declarer won, update game scores.
 		this.assignGamePoints();
 		
 		// Mark the end of a round
 		this.roundStats.setRoundEnd();
 	}
 	
 	/**
 	 * Concludes our Game, and gives us statistics for it.
 	 * @return Returns the game statistics corresponding to the games played.
 	 */
 	public GameStats concludeGame() {
 		// Set end of game player statistics
 		this.gameStats.setEndGamePlayerInfo(this.players);
 		
 		// Now we just give the 
 		return this.gameStats;
 	}
 }
