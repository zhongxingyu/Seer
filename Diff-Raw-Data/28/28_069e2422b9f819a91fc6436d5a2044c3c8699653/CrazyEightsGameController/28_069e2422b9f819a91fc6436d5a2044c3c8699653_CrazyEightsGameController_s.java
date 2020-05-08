 package com.worthwhilegames.cardgames.crazyeights;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 
 import com.worthwhilegames.cardgames.gameboard.activities.GameboardActivity;
 import com.worthwhilegames.cardgames.shared.Card;
 import com.worthwhilegames.cardgames.shared.Constants;
 import com.worthwhilegames.cardgames.shared.GameController;
 import com.worthwhilegames.cardgames.shared.Player;
 import com.worthwhilegames.cardgames.shared.Util;
 import com.worthwhilegames.cardgames.shared.connection.ConnectionConstants;
 import com.worthwhilegames.cardgames.shared.connection.ConnectionServer;
 
 /**
  * This is the GameController for the game of Crazy Eights.
  * 
  * Responsible for communicating game info, advancing turns, and handling game
  * state
  */
 public class CrazyEightsGameController extends GameController {
 
 	/**
 	 * This represents the suit chosen when an 8 is played
 	 */
 	private int suitChosen = -1;
 
 	/**
 	 * Calculates card scores for the computer
 	 */
 	private CardScoreCalculator csc;
 
 	/**
 	 * This will initialize a CrazyEightsGameController
 	 * 
 	 * @param context
 	 *            Context of the GameBoardActivity
 	 * @param connectionGiven
 	 *            The ConnectionServer that will be used
 	 */
 	public CrazyEightsGameController(GameboardActivity context,	ConnectionServer connectionGiven) {
 		super.initGameController(context, connectionGiven);
 
 		// Crazy Eights specific setup
 		ct = new CrazyEightsCardTranslator();
 		gameRules = new CrazyEightGameRules();
 
 		game = CrazyEightsTabletGame.getInstance();
 		game.setup();
 		players = game.getPlayers();
 		gameContext.highlightPlayer(1);
 
 		for (Player p : players) {
 			if (Util.isDebugBuild()) {
 				Log.d(TAG, p.getName() + ": " + p);
 			}
 
 			server.write(Constants.MSG_SETUP, p, p.getId());
 		}
 
 		Card onDiscard = game.getDiscardPileTop();
 
 		mySM.shuffleCardsSound();
 		gameContext.updateUi();
 		// Update the indicator on the gameboard with the current suit
 		gameContext.updateSuit(onDiscard.getSuit());
 
 		server.write(Constants.MSG_IS_TURN, onDiscard, players.get(whoseTurn).getId());
 	}
 
 	/* (non-Javadoc)
 	 * @see cs309.a1.shared.GameController#handleBroadcastReceive(android.content.Context, android.content.Intent)
 	 */
 	@Override
 	public void handleBroadcastReceive(Context context, Intent intent) {
 		String action = intent.getAction();
 
 		if (ConnectionConstants.MESSAGE_RX_INTENT.equals(action)) {
 			int messageSender = getWhoSentMessage(context, intent);
 			String object = intent.getStringExtra(ConnectionConstants.KEY_MESSAGE_RX);
 			int messageType = intent.getIntExtra(ConnectionConstants.KEY_MESSAGE_TYPE, -1);
 
 			// Only perform actions if it is the sender's turn
 			if (messageSender == whoseTurn) {
 				switch (messageType) {
 				case Constants.MSG_PLAY_CARD:
 					playReceivedCard(object);
 					advanceTurn();
 					break;
 				case C8Constants.PLAY_EIGHT_C:
 					suitChosen = Constants.SUIT_CLUBS;
 					playReceivedCard(object);
 					advanceTurn();
 					break;
 				case C8Constants.PLAY_EIGHT_D:
 					suitChosen = Constants.SUIT_DIAMONDS;
 					playReceivedCard(object);
 					advanceTurn();
 					break;
 				case C8Constants.PLAY_EIGHT_H:
 					suitChosen = Constants.SUIT_HEARTS;
 					playReceivedCard(object);
 					advanceTurn();
 					break;
 				case C8Constants.PLAY_EIGHT_S:
 					suitChosen = Constants.SUIT_SPADES;
 					playReceivedCard(object);
 					advanceTurn();
 					break;
 				case Constants.MSG_DRAW_CARD:
 					drawCard();
 					advanceTurn();
 					break;
 				case Constants.MSG_REFRESH:
 					refreshPlayers();
 					break;
 				}
 			} else {
 				Log.d(TAG, "It isn't " + messageSender + "'s turn - ignoring message");
 				Log.w(TAG, "messageSender: " + messageSender + " whoseTurn: " + whoseTurn);
 			}
 		}
 	}
 
 	/**
 	 * This function will be called when a players turn is over It will change
 	 * whoseTurn to the next player and send them the message that it is their
 	 * turn
 	 */
 	private void advanceTurn() {
 		// If the game is over, proceed to the declare winner section of the
 		// code
 		if (game.isGameOver(players.get(whoseTurn))) {
 			declareWinner(whoseTurn);
 			return;
 		}
 
 		// Figure out whose turn it is now
 		if (whoseTurn < game.getNumPlayers() - 1) {
 			whoseTurn++;
 		} else {
 			whoseTurn = 0;
 		}
 
 		// Highlight the name of the current player
 		gameContext.highlightPlayer(whoseTurn + 1);
 
 		// Get the top discard pile card, so that we can tell the user which
 		// cards
 		// are valid moves
 		Card onDiscard = game.getDiscardPileTop();
 
 		// If the top card is an 8, we need to do some special logic
 		// to figure out the actual suit of the card
 		if (onDiscard.getValue() == C8Constants.EIGHT_CARD_NUMBER) {
 			// If this is the first card to be chosen, just set the chosen
 			// suit to be the suit of the 8. Otherwise the suitChosen variable
 			// should already have the value of the new suit
 			if (suitChosen == -1) {
 				suitChosen = onDiscard.getSuit();
 			}
 
 			// Create a temporary card for sending to the players
 			onDiscard = new Card(suitChosen, onDiscard.getValue(),
 					onDiscard.getResourceId(), onDiscard.getIdNum());
 		}
 
 		// Update the Game board display with an indication of the current suit
 		gameContext.updateSuit(onDiscard.getSuit());
 
 		// If this is a computer, start having the computer play
 		if (players.get(whoseTurn).getIsComputer()) {
 			// play turn for computer player if not already
 			if (!isComputerPlaying) {
 				startComputerTurn();
 			}
 		} else {
 			// tell the player it is their turn
 			server.write(Constants.MSG_IS_TURN, onDiscard, players.get(whoseTurn).getId());
 		}
 
 		// Update the UI
 		gameContext.updateUi();
 	}
 
 	/**
 	 * This will send winner and loser messages to all the players depending on
 	 * if they won or not
 	 * 
 	 * @param whoWon
 	 *            The player that won
 	 */
 	private void declareWinner(int whoWon) {
 		if (Util.isDebugBuild()) {
 			Log.d(TAG, "Sending winner and loser info");
 		}
 
 		// Let each player know whether they won or lost
 		for (int i = 0; i < game.getNumPlayers(); i++) {
 			if (i == whoWon) {
 				server.write(Constants.MSG_WINNER, null, players.get(i).getId());
 			} else {
 				server.write(Constants.MSG_LOSER, null, players.get(i).getId());
 			}
 		}
 
 		String winnerName = players.get(whoWon).getName();
 
 		super.declareWinner(winnerName);
 	}
 
 	/**
 	 * This draws a card in the tablet game instance and sends that card to the
 	 * player
 	 */
 	private void drawCard() {
 		// Play draw card sound
 		mySM.drawCardSound();
 
 		Card tmpCard = game.draw(players.get(whoseTurn));
 
 		if (tmpCard != null) {
 			// And send the card to the player
 			server.write(Constants.MSG_CARD_DRAWN, tmpCard, players.get(whoseTurn).getId());
 		} else {
 			// there are no cards to draw so make it no longer that players turn
 			// and refresh the players
 			advanceTurn();
 			refreshPlayers();
 		}
 	}
 
 
 	/* (non-Javadoc)
 	 * @see com.worthwhilegames.cardgames.shared.GameController#refreshPlayers()
 	 */
 	@Override
 	protected void refreshPlayers() {
 		// Unpause the game
 		unpause();
 
 		// Send users information
 		Player pTurn = players.get(whoseTurn);
 
 		// send the card on the discard pile
 		Card discard = game.getDiscardPileTop();
 		JSONObject discardObj = discard.toJSONObject();
 
 		for (Player p : players) {
 			if (Util.isDebugBuild()) {
 				Log.d(TAG, p.getName() + " refreshed : " + p);
 			}
 
 			try {
 				// Create the base refresh info object
 				JSONObject refreshInfo = new JSONObject();
 				refreshInfo.put(Constants.KEY_TURN, pTurn.equals(p));
 				refreshInfo.put(Constants.KEY_PLAYER_NAME, p.getName());
 
 				// send the card on the discard pile
 				refreshInfo.put(Constants.KEY_DISCARD_CARD, discardObj);
 
 				// send all the cards in the players hand
 				JSONArray arr = new JSONArray();
 				for (Card c : p.getCards()) {
 					arr.put(c.toJSONObject());
 				}
 
 				// send the card in their hand
 				refreshInfo.put(Constants.KEY_CURRENT_HAND, arr);
 
 				server.write(Constants.MSG_REFRESH, refreshInfo.toString(), p.getId());
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 		}
 
 		// If the next player is a computer, and the computer isn't currently
 		// playing, have the computer initiate a move
 		if (players.get(whoseTurn).getIsComputer() && !isComputerPlaying) {
 			startComputerTurn();
 		}
 	}
 
 	/**
 	 * This will play for a computer player based on the difficulty level. either play or draw a card.
 	 * This will be called after the PlayComputerTurnActivity has waited for the appropriate amount of time.
 	 * level 0 	should just loop through the cards to find one that it is allowed to play
 	 * 			very basic, randomly play a card if able or draw if not able
 	 * level 1 	chooses first the cards of the same suit, then cards of the same index of a different suit,
 	 * 			then a special card as a last resort. if a suit of the same index
 	 *
 	 * level 2 	nothing yet
 	 */
 	@Override
 	protected void playComputerTurn() {
 		Card onDiscard = game.getDiscardPileTop();
 		if (onDiscard.getValue() == C8Constants.EIGHT_CARD_NUMBER) {
 			onDiscard = new Card(suitChosen, onDiscard.getValue(), onDiscard.getResourceId(), onDiscard.getIdNum());
 		}
 		List<Card> cards = players.get(whoseTurn).getCards();
 		Card cardSelected = null;
 
 		//computer with difficulty Easy
 		if (players.get(whoseTurn).getComputerDifficulty().equals(Constants.EASY)) {
 			for (Card c : cards) {
 				if (gameRules.checkCard(c, onDiscard)) {
 					cardSelected = c;
 					break;
 				}
 			}
 
 
 			if (cardSelected != null && cardSelected.getValue() == C8Constants.EIGHT_CARD_NUMBER) {
 				int[] suits = new int[5];
 				int maxSuitIndex = 0;
 				for (Card c : cards) {
 					if (!c.equals(cardSelected)) {
 						suits[c.getSuit()]++;
 						if (suits[c.getSuit()] > suits[maxSuitIndex]) {
 							maxSuitIndex = c.getSuit();
 						}
 					}
 				}
 				suitChosen = maxSuitIndex;
 			}
 
 			//computer difficulty Medium
 		} else if (players.get(whoseTurn).getComputerDifficulty().equals(Constants.MEDIUM) ) {
 
 			List<Card> sameSuit = new ArrayList<Card>();
 			List<Card> sameNum = new ArrayList<Card>();
 			List<Card> special = new ArrayList<Card>();
 
 			int suits[] = new int[5];
 			int maxSuitIndex = 0;
 
 			for (Card c : cards) {
 				//checks for 8s and jokers
 				if( (c.getValue() == C8Constants.EIGHT_CARD_NUMBER || c.getSuit() == Constants.SUIT_JOKER) && gameRules.checkCard(c, onDiscard) ){
 					special.add(c);
 					continue;
 				}
 
 				//this gets the number of cards of each suit
 				suits[c.getSuit()]++;
 				if (suits[c.getSuit()] > suits[maxSuitIndex] && c.getSuit() != Constants.SUIT_JOKER) {
 					maxSuitIndex = c.getSuit();
 				}
 
 				//checks for cards of the same suit then cards of the same index
 				if (c.getSuit() == onDiscard.getSuit() && gameRules.checkCard(c, onDiscard) ) {
 					sameSuit.add(c);
 				} else if (c.getValue() == onDiscard.getValue() && gameRules.checkCard(c, onDiscard) ) {
 					sameNum.add(c);
 				}
 			}
 
 
 
 			//see if there is more of another suit that the computer can change it to.
 			boolean moreOfOtherSuit = false;
 			for (Card c : sameNum) {
 				if (suits[c.getSuit()] > suits[onDiscard.getSuit()]){
 					moreOfOtherSuit = true;
 					break;
 				}
 			}
 
 
 			if (onDiscard.getSuit() == Constants.SUIT_JOKER){ //for a joker
 				for (Card c : cards){
 					if (c.getSuit() == maxSuitIndex && c.getValue() != Constants.EIGHT_VALUE){
 						cardSelected = c;
 						break;
 					}
 				}
 				if(cardSelected == null){
 					for (Card c : cards){
 						if (c.getSuit() == maxSuitIndex){
 							cardSelected = c;
 							break;
 						}
 					}
 				}
 			} else if (moreOfOtherSuit && sameNum.size() > 0 ) { //choose a card of the same number that we can change the suit with
 				cardSelected = sameNum.get(0);
 				for (Card c : sameNum) {
 					if (suits[c.getSuit()] > suits[cardSelected.getSuit()]){
 						cardSelected = c;
 					}
 				}
 			} else if (sameSuit.size() > 0) { //choose a card of the same suit
 				cardSelected = sameSuit.get(0);
 				boolean hasAnotherCardWithIndex = false;
 				for (Card c : sameSuit) {
 					for (Card c1 : cards) {
 						if (!c.equals(c1) && c.getValue() == c1.getValue() && suits[c.getSuit()] <= suits[c1.getSuit()] ){
 							cardSelected = c;
 							hasAnotherCardWithIndex = true;
 							break;
 						}
 					}
 					if (hasAnotherCardWithIndex) {
 						break;
 					}
 				}
 			} else if (special.size() > 0){ //play a special card as last resort
 				cardSelected = special.get(0);
 				if (cardSelected != null && cardSelected.getValue() == C8Constants.EIGHT_CARD_NUMBER) {
 					suitChosen = maxSuitIndex;
 				}
 			} // else { no card selected }
 
 			//computer difficulty Hard
 		} else if (players.get(whoseTurn).getComputerDifficulty().equals(Constants.HARD)) {
 
 			//get game state, clone it, send to recursive function
 			List<List<Card>> cardsClone = new ArrayList<List<Card>>();
 			for(Player p : players){
 				cardsClone.add(new ArrayList<Card>(p.getCards()));
 			}
 			csc = new CardScoreCalculator(whoseTurn, cardsClone);
 			Card firstOnDiscard = game.getDiscardPileTop();
 			if(firstOnDiscard.getValue() == C8Constants.EIGHT_CARD_NUMBER){
 				firstOnDiscard = new Card(suitChosen, firstOnDiscard.getValue(), firstOnDiscard.getResourceId(), firstOnDiscard.getIdNum());
 			}
 			Card curOnDiscard = game.getDiscardPileTop();
 			int suitToChoose = findMaxSuitIndex(cardsClone.get(whoseTurn));
 
 			int nextTurnIndex=whoseTurn;
 			// Figure out whose turn it is next
 			if (nextTurnIndex < game.getNumPlayers() - 1) {
 				nextTurnIndex++;
 			} else {
 				nextTurnIndex =0;
 			}
 
 			List<Card> drawPile = new ArrayList<Card>(game.getShuffledDeck());
 			double tmpScore = 0;
 			Card cardDrawn = null;
 			int movesArraySize = cardsClone.get(whoseTurn).size() +1;
 			double moves[] = new double[movesArraySize];
 			int recDepth = 6 + players.size();
 
 			int minIndex=0;
 
 			//recursive call
 			for(int i = 0; i<cardsClone.get(whoseTurn).size(); i++){
 				curOnDiscard = firstOnDiscard;
 				Card tmpCard = cardsClone.get(whoseTurn).get(0);
 				cardsClone.get(whoseTurn).remove(0);
 				if(gameRules.checkCard(tmpCard, curOnDiscard)){
 					tmpScore = csc.calculateScorePlayed(tmpCard, curOnDiscard, whoseTurn);
 					curOnDiscard = tmpCard;
 					if(curOnDiscard.getValue() == C8Constants.EIGHT_CARD_NUMBER){
 						curOnDiscard = new Card(suitToChoose, curOnDiscard.getValue(), curOnDiscard.getResourceId(), curOnDiscard.getIdNum());
 					}
 					tmpScore += findBestMove(nextTurnIndex, cardsClone, curOnDiscard, drawPile, recDepth);
 					moves[i] = tmpScore;
 					if(moves[i] < moves[minIndex]){
 						minIndex = i;
 					}
 				} else {
 					//very high number so it is not chosen
 					moves[i] = 30000;
 				}
 				cardsClone.get(whoseTurn).add(tmpCard);
 			}
 
 			//see how we do if we draw
 			if(!drawPile.isEmpty() && moves[minIndex]>= 30000){
 				cardDrawn = drawPile.get(0);
 				cardsClone.get(whoseTurn).add(cardDrawn);
 				drawPile.remove(0);
 				tmpScore = csc.calculateScoreDrawn(cardDrawn, whoseTurn);
 				tmpScore += findBestMove(nextTurnIndex, cardsClone, curOnDiscard, drawPile, recDepth);
 				drawPile.add(0,cardDrawn);
 				cardsClone.get(whoseTurn).remove(cardDrawn);
 				moves[movesArraySize-1] = tmpScore;
 				//if there is no card to play then draw.
 				if(moves[movesArraySize-1] < moves[minIndex]){
 					minIndex = movesArraySize-1;
 				}
 			}
 
 			if(minIndex < movesArraySize-1){
 				cardSelected = players.get(whoseTurn).getCards().get(minIndex);
 
 				if(!gameRules.checkCard(cardSelected, onDiscard)){
 					//should never get here, this would be an error.
 					cardSelected = null;
 				} else if(cardSelected.getValue() == C8Constants.EIGHT_CARD_NUMBER){
 					suitChosen = suitToChoose;
 				}
 			} else {
 				cardSelected = null;
 			}
 
 		}
 
 
 		if (cardSelected != null) {
 			//Play Card
 			mySM.playCardSound();
 
 			game.discard(players.get(whoseTurn), cardSelected);
 		} else {
 			// Draw Card
 			Card tmpCard = game.draw(players.get(whoseTurn));
 
 			// If card is null then there are no cards to draw so just move on and allow the turn to advance
 			if (tmpCard != null) {
 				mySM.drawCardSound();
 			}
 		}
 		//the computer has finished, advance the turn
 		advanceTurn();
 	}
 
 	/**
 	 * Finds the suit of the list with the maximum number of cards
 	 * 
 	 * @param cards
 	 * @return
 	 */
 	private int findMaxSuitIndex(List<Card> cards) {
 		int suits[] = new int[5];
 		int maxSuitIndex = 0;
 
 		for (Card c : players.get(whoseTurn).getCards()) {
 			// checks for 8s and jokers
 			if (c.getValue() == C8Constants.EIGHT_CARD_NUMBER
 					|| c.getSuit() == Constants.SUIT_JOKER) {
 				continue;
 			}
 			// this gets the number of cards of each suit
 			suits[c.getSuit()]++;
 			if (suits[c.getSuit()] > suits[maxSuitIndex]) {
 				maxSuitIndex = c.getSuit();
 			}
 		}
 
 		return maxSuitIndex;
 	}
 
 	/**
 	 * This function will find the best move for any player and return the score of how good of a move it is for that player
 	 * @param playerIndex
 	 * @param players
 	 * @param onDiscard
 	 * @param drawPile
 	 * @param recursionDepth
 	 * @return
 	 */
 	private double findBestMove(int playerIndex, List<List<Card>> cardsClone, Card curOnDiscard, List<Card> drawPile, int recDepth) {
 		if (recDepth == 0) {
 			return 0;
 		}
 		int suitToChoose = findMaxSuitIndex(cardsClone.get(playerIndex));
 		Card firstOnDiscard = curOnDiscard;
 		double tmpScore = 0;
 		Card cardDrawn = null;
 		int movesArraySize = cardsClone.get(playerIndex).size() +1;
 		double[] moves = new double[movesArraySize];
 
 		int nextTurnIndex = playerIndex;
 		// Figure out whose turn it is next
 		if (nextTurnIndex < game.getNumPlayers() - 1) {
 			nextTurnIndex++;
 		} else {
 			nextTurnIndex = 0;
 		}
 
 		int maxIndex = 0;
 		//rec call
 		for (int i = 0; i < cardsClone.get(playerIndex).size(); i++) {
 			curOnDiscard = firstOnDiscard;
 			Card tmpCard = cardsClone.get(playerIndex).get(0);
 			cardsClone.get(playerIndex).remove(0);
 			if (gameRules.checkCard(tmpCard, curOnDiscard)) {
 				tmpScore = csc.calculateScorePlayed(tmpCard, curOnDiscard, playerIndex);
 				if (tmpScore >= 10000 || ((whoseTurn == playerIndex) && tmpScore <= -10000)){
 					//we can win with this player so game over.
 					cardsClone.get(playerIndex).add(tmpCard);
 					return tmpScore;
 				}
 				curOnDiscard = tmpCard;
 				if (curOnDiscard.getValue() == C8Constants.EIGHT_CARD_NUMBER) {
 					curOnDiscard = new Card(suitToChoose, curOnDiscard.getValue(), curOnDiscard.getResourceId(), curOnDiscard.getIdNum());
 				}
 				tmpScore += findBestMove(nextTurnIndex, cardsClone, curOnDiscard, drawPile, recDepth-1);
 				moves[i] = tmpScore;
 				if (moves[i] > moves[maxIndex]) {
 					maxIndex = i;
 				}
 			} else {
 				if(whoseTurn == playerIndex){
 					//very high number so it is never chosen by current player
 					moves[i] = 30000;
 				} else {
 					//very low number so it is never chosen by another player
 					moves[i] = -30000;
 				}
 			}
 			cardsClone.get(playerIndex).add(tmpCard);
 		}
 
 		// try drawing a card, only if there is a draw pile and there is not another card that can be played
 		if (!drawPile.isEmpty() && (moves[maxIndex] >=30000 || moves[maxIndex] <= -30000)){
 			cardDrawn = drawPile.get(0);
 			cardsClone.get(playerIndex).add(cardDrawn);
 			drawPile.remove(0);
 			tmpScore = csc.calculateScoreDrawn(cardDrawn, playerIndex);
 			tmpScore += findBestMove(nextTurnIndex, cardsClone, curOnDiscard, drawPile, recDepth - 1);
 			drawPile.add(0,cardDrawn);
 			cardsClone.get(playerIndex).remove(cardDrawn);
 			moves[movesArraySize-1] = tmpScore;
 			if (moves[movesArraySize-1] > moves[maxIndex]) {
 				maxIndex = movesArraySize-1;
 			}
 		} else {
 			if (whoseTurn == playerIndex) {
 				//very high number so it is never chosen by current player
 				moves[movesArraySize-1] = 30000;
 			} else {
 				//very low number so it is never chosen by another player
 				moves[movesArraySize-1] = -30000;
 			}
 		}
 
 		if (whoseTurn == playerIndex) {
 			// if this is the current player then we want the minimum not maximum.
 			int minIndex = 0;
 			for (int i = 0; i< movesArraySize; i++) {
 				if (moves[i] < moves[minIndex]) {
 					minIndex = i;
 				}
 			}
 
 			return moves[minIndex];
 		}
 
 		return moves[maxIndex];
 	}
 }
