 package com.worthwhilegames.cardgames.euchre;
 
 import static com.worthwhilegames.cardgames.euchre.EuchreConstants.ADJUSTED_ACE_VALUE;
 import static com.worthwhilegames.cardgames.euchre.EuchreConstants.ADJUSTED_L_VALUE;
 import static com.worthwhilegames.cardgames.euchre.EuchreConstants.ADJUSTED_R_VALUE;
 import static com.worthwhilegames.cardgames.euchre.EuchreConstants.EASY_COMP_DEALER_TEAM_ADVANTAGE;
 import static com.worthwhilegames.cardgames.euchre.EuchreConstants.EASY_COMP_GO_ALONE_THRESHOLD;
 import static com.worthwhilegames.cardgames.euchre.EuchreConstants.EASY_COMP_R1_BET_THRESHOLD;
 import static com.worthwhilegames.cardgames.euchre.EuchreConstants.EASY_COMP_R2_BET_THRESHOLD;
 import static com.worthwhilegames.cardgames.euchre.EuchreConstants.FIRST_ROUND_BETTING;
 import static com.worthwhilegames.cardgames.euchre.EuchreConstants.HARD_COMP_DEALER_TEAM_ADVANTAGE;
 import static com.worthwhilegames.cardgames.euchre.EuchreConstants.HARD_COMP_GO_ALONE_THRESHOLD;
 import static com.worthwhilegames.cardgames.euchre.EuchreConstants.HARD_COMP_R1_BET_THRESHOLD;
 import static com.worthwhilegames.cardgames.euchre.EuchreConstants.HARD_COMP_R2_BET_THRESHOLD;
 import static com.worthwhilegames.cardgames.euchre.EuchreConstants.MEDIUM_COMP_DEALER_TEAM_ADVANTAGE;
 import static com.worthwhilegames.cardgames.euchre.EuchreConstants.MEDIUM_COMP_GO_ALONE_THRESHOLD;
 import static com.worthwhilegames.cardgames.euchre.EuchreConstants.MEDIUM_COMP_R1_BET_THRESHOLD;
 import static com.worthwhilegames.cardgames.euchre.EuchreConstants.MEDIUM_COMP_R2_BET_THRESHOLD;
 import static com.worthwhilegames.cardgames.euchre.EuchreConstants.SECOND_ROUND_BETTING;
 import static com.worthwhilegames.cardgames.euchre.EuchreConstants.TRUMP_CARD_VALUE_FACTOR;
 import static com.worthwhilegames.cardgames.shared.Constants.ACE_VALUE;
 import static com.worthwhilegames.cardgames.shared.Constants.JACK_VALUE;
 import static com.worthwhilegames.cardgames.shared.Constants.SUIT_CLUBS;
 import static com.worthwhilegames.cardgames.shared.Constants.SUIT_DIAMONDS;
 import static com.worthwhilegames.cardgames.shared.Constants.SUIT_HEARTS;
 import static com.worthwhilegames.cardgames.shared.Constants.SUIT_SPADES;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.worthwhilegames.cardgames.shared.Card;
 import com.worthwhilegames.cardgames.shared.Constants;
 import com.worthwhilegames.cardgames.shared.Player;
 
 public class EuchreComputerPlayer {
 
 	/**
 	 * the list of current active players
 	 */
 	private List<Player> players;
 
 	/**
 	 * This game object will keep track of the current state of the game and be
 	 * used to manage player hands and draw and discard piles
 	 */
 	private static EuchreTabletGame game = null;
 
 	/**
 	 * The implementation of the Game Rules
 	 */
 	private EuchreGameRules gameRules = new EuchreGameRules();
 
 	/**
 	 * The difficulty of this computer player
 	 */
 	private String difficulty;
 
 	private int r1BetThreshold;
 
 	private int r2BetThreshold;
 
 	private int goAloneThreshold;
 
 	private int dealerTeamAdvantage;
 
 	/**
 	 * This will initialize a computer player
 	 * @param difficulty this is the difficulty of this euchre computer
 	 */
 	public EuchreComputerPlayer(String difficulty){
 		this.difficulty = difficulty;
 		this.game = EuchreTabletGame.getInstance();
 		this.players = game.getPlayers();
 
 		//set up constants based on difficulty
 		if(this.difficulty.equals(Constants.EASY)){
 			r1BetThreshold = EASY_COMP_R1_BET_THRESHOLD;
 			r2BetThreshold = EASY_COMP_R2_BET_THRESHOLD;
 			goAloneThreshold = EASY_COMP_GO_ALONE_THRESHOLD;
 			dealerTeamAdvantage = EASY_COMP_DEALER_TEAM_ADVANTAGE;
 		} else if(this.difficulty.equals(Constants.MEDIUM)){
 			r1BetThreshold = MEDIUM_COMP_R1_BET_THRESHOLD;
 			r2BetThreshold = MEDIUM_COMP_R2_BET_THRESHOLD;
 			goAloneThreshold = MEDIUM_COMP_GO_ALONE_THRESHOLD;
 			dealerTeamAdvantage = MEDIUM_COMP_DEALER_TEAM_ADVANTAGE;
 		} else { //Hard
 			r1BetThreshold = HARD_COMP_R1_BET_THRESHOLD;
 			r2BetThreshold = HARD_COMP_R2_BET_THRESHOLD;
 			goAloneThreshold = HARD_COMP_GO_ALONE_THRESHOLD;
 			dealerTeamAdvantage = HARD_COMP_DEALER_TEAM_ADVANTAGE;
 		}
 	}
 
 	/**
 	 * This will get what the computer bets
 	 * @param whoseTurn
 	 * @param round
 	 * @param tempTrump
 	 * @return
 	 */
 	public EuchreBet getComputerBet( int whoseTurn, final int round, int tempTrump){
 		Card cardLead = game.getCardLead();
 
 		List<Card> cards = players.get(whoseTurn).getCards();
 		Card cardSelected = null;
 		EuchreBet compBet = new EuchreBet(game.getCardLead().getSuit(), false, false);
 
 
 		int[] suitScores = new int[4];
 		int bestSuitToBetOn = 0;
 		int maxSuitScore = 0;
 
 		suitScores = this.computeSuitScores(cards);
 
 		for(int i = 0; i < 4; i++){
 			if(suitScores[i] > maxSuitScore){
 				maxSuitScore = suitScores[i];
 				bestSuitToBetOn = i;
 			}
 		}
 
 		switch(round){
 		case FIRST_ROUND_BETTING:
 			if( whoseTurn % 2 == game.getDealer() % 2){
 				if( suitScores[game.getCardLead().getSuit()] > r1BetThreshold - dealerTeamAdvantage){
 					if( suitScores[game.getCardLead().getSuit()] > r1BetThreshold + goAloneThreshold) {
 						compBet = new EuchreBet(game.getCardLead().getSuit(), true, true);
 					} else {
 						compBet = new EuchreBet(game.getCardLead().getSuit(), true, false);
 					}
 				}
 			} else {
 				if( suitScores[game.getCardLead().getSuit()] > r1BetThreshold){
 					if( suitScores[game.getCardLead().getSuit()] > r1BetThreshold + goAloneThreshold) {
 						compBet = new EuchreBet(game.getCardLead().getSuit(), true, true);
 					} else {
 						compBet = new EuchreBet(game.getCardLead().getSuit(), true, false);
 					}
 				}
 			}
 			break;
 		case SECOND_ROUND_BETTING:
 			if( maxSuitScore > r2BetThreshold ){
 				if( maxSuitScore > r2BetThreshold + goAloneThreshold) {
 					compBet = new EuchreBet(bestSuitToBetOn, true, true);
 				} else {
 					compBet = new EuchreBet(bestSuitToBetOn, true, false);
 				}
 			} else if( whoseTurn == game.getDealer() ){
 				//Stick it to the Dealer situation must bet
 				compBet = new EuchreBet(bestSuitToBetOn, true, false);
 			}
 			break;
 		}
 
 		return compBet;
 	}
 
 	/**
 	 * This method will chose which card a computer player should discard after picking up the "up card"
 	 * @param whoseTurn whose turn it is
 	 * @return the card to discard
 	 */
 	public Card pickItUp(int whoseTurn){
 		Card cardSelected = players.get(whoseTurn).getCards().get(0);
 
 		if(this.difficulty.equals(Constants.EASY)){
 			//Easy, randomly take the first card.
 		}else { //Hard and Medium
 			for(Card c: players.get(whoseTurn).getCards()){
 				if(this.computeCardScore(c, game.getTrump()) < this.computeCardScore(c, game.getTrump())){
 					cardSelected = c;
 				}
 			}
 		}
 		return cardSelected;
 	}
 
 	public Card getLeadCard(int whoseTurn){
 		Card cardSelected = players.get(whoseTurn).getCards().get(0);
 
 		if(this.difficulty.equals(Constants.EASY)){
 			//Easy, randomly choose the first card.
 		}else if(this.difficulty.equals(Constants.MEDIUM)){
 			//TODO medium
 
 		} else if(this.difficulty.equals(Constants.HARD)){
 
 			//TODO hard
 
 		}
 
 
 		return cardSelected;
 	}
 
 	public Card getCardOnTurn(int whoseTurn){
 		Card cardSelected = players.get(whoseTurn).getCards().get(0);
 
 		List<Card> playable = this.getPlayableCards(players.get(whoseTurn).getCards());
 
 		if( this.difficulty.equals(Constants.EASY)){
 			//choose the first playable card
 			if(playable.size() > 0){
 				cardSelected = playable.get(0);
 			}
 		} else if (this.difficulty.equals(Constants.MEDIUM)){
 			Card possibleWinner = canBeatWhatIsPlayed(whoseTurn);
 			if( possibleWinner != null){
 				//go for the win
 				cardSelected = possibleWinner;
 			} else {
 				//can't win, low ball it.
 				for( Card c: playable){
 					if( compareCards(cardSelected, c)){
 						cardSelected = c;
 					}
 				}
 			}
 		} else if (this.difficulty.equals(Constants.HARD)){
 			//TODO hard
 			//make function to see if my card can beat all possible cards

 		}
 
 
 
 		return cardSelected;
 	}
 
 	private List<Card> getPlayableCards(List<Card> cards){
 		List<Card> playableCards = new ArrayList<Card>();
 
 		for (Card c : cards) {
 			if (gameRules.checkCard(c, this.game.getTrump(), this.game.getCardLead() , cards)) {
 				playableCards.add(c);
 				break;
 			}
 		}
 
 		return playableCards;
 	}
 
 	private int incrementWhoseTurn(int whoseTurn){
 		if(whoseTurn == 3){
 			return 0;
 		}
 		whoseTurn++;
 		return whoseTurn;
 	}
 
 	/**
 	 * If the player can beat what has already been played then this will return the card they should play
 	 * else it will return null
 	 * @param whoseTurn - this is whose turn it is
 	 * @return card to beat what has been played or null if the player can't beat the played cards
 	 */
 	private Card canBeatWhatIsPlayed(int whoseTurn){
 		List<Card> playerCards = getPlayableCards(players.get(whoseTurn).getCards());
 		Card playerWinningCard = null;
 		Card otherPlayerCard;
 		Card teammateCard = null;
 		int tempTurn = game.getTrickLeader();
 
 		while(tempTurn != whoseTurn){
 			if(whoseTurn % 2 == tempTurn % 2){
 				//we are looking at players teammate
 				teammateCard = game.getCardAtPosition(tempTurn);
 			} else {
 				otherPlayerCard = game.getCardAtPosition(tempTurn);
 
 				if(teammateCard != null && compareCards(teammateCard, otherPlayerCard)){
 					//teammateCard beats the other teams card here
 				} else {
 					if( playerWinningCard != null && compareCards(playerWinningCard, otherPlayerCard)){
 						//this card already will win
 					} else{
 						playerWinningCard = null;
 						//need to find a card that can win
 						for(Card c: playerCards){
 							if(compareCards(c, otherPlayerCard)){
 								if(playerWinningCard == null) {
 									playerWinningCard = c;
 								} else if( compareCards(playerWinningCard, c)){
 									//winning card is greater than current card but current card will still win
 									playerWinningCard = c;
 								}
 							}
 						}
 					}
 				}
 			}
 
 			tempTurn = incrementWhoseTurn(tempTurn);
 		}
 
 		//TODO test!!
 		return playerWinningCard;
 	}
 
 	private Card canBeatAllCardsPlayable(int whoseTurn){
 		List<Card> playerCards = getPlayableCards(players.get(whoseTurn).getCards());
 		Card playerWinningCard = null;
 		Card otherPlayerCard;
 		List<Card> otherPlayerCards;
 		Card teammateCard = null;
 		int tempTurn = game.getTrickLeader();
 
 		do {
 			if(tempTurn != whoseTurn){
 				if(game.getCardAtPosition(tempTurn) != null){
 					//player has already played their card for this trick
 					otherPlayerCard = game.getCardAtPosition(tempTurn);
 
 					if(teammateCard != null && compareCards(teammateCard, otherPlayerCard)){
 						//teammateCard beats the other teams card here
 					} else {
 						if( playerWinningCard != null && compareCards(playerWinningCard, otherPlayerCard)){
 							//this card already will win
 						} else{
 							playerWinningCard = null;
 							//need to find a card that can win
 							for(Card c: playerCards){
 								if(compareCards(c, otherPlayerCard)){
 									if(playerWinningCard == null) {
 										playerWinningCard = c;
 									} else if( compareCards(playerWinningCard, c)){
 										//winning card is greater than current card but current card will still win
 										playerWinningCard = c;
 									}
 								}
 							}
 						}
 					}
 				} else {
 					//player has not played their card for this trick so we look at their hand.
 
 				}
 			}
 
 			tempTurn = incrementWhoseTurn(tempTurn);
 		}while (tempTurn != game.getTrickLeader());
 
 
 		return playerWinningCard;
 	}
 
 	/**
 	 * This will try to decide which suit has the best chance for you to win.
 	 * Tries to quantify the value of the hand based on the trump decided.
 	 * 
 	 * @param cards - input cards for the method to analyze
 	 * @return an array of scores that should quantify which trump suit
 	 * 		   would be best for a player to call
 	 */
 	private int[] computeSuitScores( List<Card> cards ){
 		int[] scores = new int[4];
 
 		//go through each suit as a possible trump suit and calculate the score
 		for( int tempTrump = 0; tempTrump < 4; tempTrump ++){
 			for(Card c: cards){
 				scores[tempTrump] += this.computeCardScore(c, tempTrump);
 			}
 		}
 
 		return scores;
 	}
 
 
 	/**
 	 * If the first card is greater than the second card then return true else return false
 	 * @param c1 first card
 	 * @param c2 second card
 	 * @return if the first card is greater return true else false
 	 */
 	private boolean compareCards( Card c1, Card c2 ) {
 		if( c1 == null){
 			return false;
 		} else if( c2 == null){
 			return true;
 		}
 		//TODO test this adjust card lead
 		game.adjustCards(game.getCardLead());
 		return c1.equals(game.compareCards(c1, c1, game.getCardLead().getSuit()));
 	}
 
 	/**
 	 * This method will compute the card value based on trump and Ace value
 	 * @param c the card to find the value for
 	 * @param tempTrump the trump suit
 	 * @return an integer that is representative of the card's score
 	 * 0 means that the card given is null
 	 */
 	private int computeCardScore( Card c, int tempTrump ) {
 		if(c == null){
 			return 0;
 		}
 
 		if(c.getValue() == JACK_VALUE){
 			if( c.getSuit() == tempTrump ){
 				//this is the right bower
 				return ADJUSTED_R_VALUE * ( TRUMP_CARD_VALUE_FACTOR );
 			} else {
 				if( (c.getSuit() == SUIT_CLUBS && tempTrump == SUIT_SPADES) ||
 						(c.getSuit() == SUIT_SPADES && tempTrump == SUIT_CLUBS) ||
 						(c.getSuit() == SUIT_DIAMONDS && tempTrump == SUIT_HEARTS) ||
 						(c.getSuit() == SUIT_HEARTS && tempTrump == SUIT_DIAMONDS) ){
 					//this is the left bower
 					return ADJUSTED_L_VALUE * ( TRUMP_CARD_VALUE_FACTOR );
 				} else {
 					//this is a regular card of non trump suit
 					return c.getValue() ;
 				}
 			}
 		} else {
 			int tempValue = c.getValue();
 			if( tempValue == ACE_VALUE ) {
 				tempValue = ADJUSTED_ACE_VALUE;
 			}
 			if(c.getSuit() == tempTrump){
 				//this is a trump suit card, not a Jack
 				return tempValue * TRUMP_CARD_VALUE_FACTOR;
 			} else {
 				//this is a regular card
 				return tempValue;
 			}
 		}
 	}
 }
