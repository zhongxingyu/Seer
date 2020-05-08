 package com.worthwhilegames.cardgames.euchre;
 
 import static com.worthwhilegames.cardgames.euchre.EuchreConstants.BET;
 import static com.worthwhilegames.cardgames.euchre.EuchreConstants.FIRST_ROUND_BETTING;
 import static com.worthwhilegames.cardgames.euchre.EuchreConstants.GO_ALONE;
 import static com.worthwhilegames.cardgames.euchre.EuchreConstants.PICK_IT_UP;
 import static com.worthwhilegames.cardgames.euchre.EuchreConstants.PLAY_LEAD_CARD;
 import static com.worthwhilegames.cardgames.euchre.EuchreConstants.ROUND_OVER;
 import static com.worthwhilegames.cardgames.euchre.EuchreConstants.SECOND_ROUND_BETTING;
 import static com.worthwhilegames.cardgames.euchre.EuchreConstants.TRUMP;
 import static com.worthwhilegames.cardgames.shared.Constants.MSG_PLAY_CARD;
 import static com.worthwhilegames.cardgames.shared.Constants.PREFERENCES;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.annotation.SuppressLint;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 
 import com.worthwhilegames.cardgames.gameboard.activities.GameboardActivity;
 import com.worthwhilegames.cardgames.shared.Card;
 import com.worthwhilegames.cardgames.shared.Constants;
 import com.worthwhilegames.cardgames.shared.GameController;
 import com.worthwhilegames.cardgames.shared.Player;
 import com.worthwhilegames.cardgames.shared.Util;
 import com.worthwhilegames.cardgames.shared.activities.RoundScoresActivity;
 import com.worthwhilegames.cardgames.shared.connection.ConnectionConstants;
 import com.worthwhilegames.cardgames.shared.connection.ConnectionServer;
 
 public class EuchreGameController extends GameController {
 
 	/**
 	 * request code to allow the gameboard to choose which player to connect
 	 */
 	private static final int DECLARE_ROUND_SCORES = Math.abs("DECLARE_ROUND_SCORES".hashCode());
 
 	/**
 	 * This game object will keep track of the current state of the game and be
 	 * used to manage player hands and draw and discard piles
 	 * 
 	 * specific for Euchre since there are a lot of specific methods
 	 */
 	protected static EuchreTabletGame euchreGame = null;
 
 	/**
 	 * This is the current state of the game
 	 * Corresponds to the first round of betting second round of betting
 	 * trick leading etc constants in euchre constants and shared constants
 	 */
 	private int currentState;
 
 	/**
 	 * This is how to tell if the gameboard is in the waiting phase to clear the
 	 * cards played on the last trick
 	 */
 	private boolean isWaitingToClearCards = false;
 
 	/**
 	 * Computer player that makes moves for the computer players
 	 */
 	private EuchreComputerPlayer computerPlayer;
 
 	/**
 	 * Handler to handle showing all 4 cards played
 	 */
 	@SuppressLint("HandlerLeak")
 	private Handler cardPlayingHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			if (Util.isDebugBuild()) {
 				Log.d(TAG, "handleMessage: waiting to remove cards");
 			}
 
 			if (!isPaused && isWaitingToClearCards) {
 				isWaitingToClearCards = false;
 				endTurnRound();
 			} else {
 				if (Util.isDebugBuild()) {
 					Log.d(TAG, "handleMessage: game paused. not going to play now");
 				}
 			}
 		}
 	};
 
 	/**
 	 * @param context
 	 * @param connectionGiven
 	 * @param playersGiven
 	 */
 	public EuchreGameController(GameboardActivity context, ConnectionServer connectionGiven) {
 		super.initGameController(context, connectionGiven);
 
 		// Euchre specific setup
 		ct = new EuchreCardTranslator();
 		gameRules = new EuchreGameRules();
 		SharedPreferences sharedPreferences = gameContext.getSharedPreferences(PREFERENCES, 0);
 		String difficulty = sharedPreferences.getString(Constants.PREF_DIFFICULTY, Constants.EASY);
 		computerPlayer = new EuchreComputerPlayer(difficulty);
 
 		euchreGame = EuchreTabletGame.getInstance();
 		game = euchreGame;
 
 		euchreGame.setup();
 		players = euchreGame.getPlayers();
 		startRound();
 	}
 
 	private void startRound() {
 		euchreGame.startRound();
 		whoseTurn = euchreGame.getTrickLeader();
 
 		for (Player p : players) {
 			if (Util.isDebugBuild()) {
 				Log.d(TAG, p.getName() + ": " + p);
 			}
 
 			server.write(Constants.MSG_SETUP, p, p.getId());
 		}
 
 		mySM.shuffleCardsSound();
 		gameContext.updateUi();
 		// Update the indicator on the gameboard with the current suit
 		gameContext.updateSuit(game.getDiscardPileTop().getSuit());
 
 		//unbold the players names
 		gameContext.unboldAllPlayerText();
 
 		// If this is a computer, start having the computer play
 		currentState = FIRST_ROUND_BETTING;
 
 		// tell first person to bet
 		sendNextTurn(currentState, euchreGame.getCardLead());
 	}
 
 	/* (non-Javadoc)
 	 * @see com.worthwhilegames.cardgames.shared.GameController#handleBroadcastReceive(android.content.Context, android.content.Intent)
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
 				case FIRST_ROUND_BETTING:
 					handleBetting(FIRST_ROUND_BETTING, object);
 					break;
 				case SECOND_ROUND_BETTING:
 					handleBetting(SECOND_ROUND_BETTING, object);
 					break;
 				case PLAY_LEAD_CARD:
 					Card tmpCard = playReceivedCard(object);
 					euchreGame.setCardLead(tmpCard);
 					advanceTurn();
 					break;
 				case PICK_IT_UP:
 					//Get which card they discarded and discard it.
 					currentState = PLAY_LEAD_CARD;
 					playReceivedCard(object);
 					euchreGame.clearCardsPlayed();
 
 					//start the first turn of the round by going one person the the right of the dealer.
 					whoseTurn = euchreGame.getTrickLeader();
 
 					refreshPlayers();
 					sendNextTurn(currentState, euchreGame.getCardLead());
 					break;
 				case MSG_PLAY_CARD:
 					playReceivedCard(object);
 					advanceTurn();
 					break;
 				case Constants.MSG_REFRESH:
 					refreshPlayers();
 					break;
 				}
 			} else {
 				Log.d(TAG, "It isn't " + messageSender + "'s turn - ignoring message");
 				Log.w(TAG, "messageSender: " + messageSender + " whoseTurn: " + whoseTurn);
				// refresh players to get everyone to the same state.
				refreshPlayers();
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see com.worthwhilegames.cardgames.shared.GameController#handleActivityResult(int, int, android.content.Intent)
 	 */
 	@Override
 	public boolean handleActivityResult(int requestCode, int resultCode, Intent data) {
 		if (super.handleActivityResult(requestCode, resultCode, data)){
 			// This activity result was handled by GameController
 			return true;
 
 			// addition of Euchre specific handling
 		} else if (requestCode == DECLARE_ROUND_SCORES){
 			//They have seen the score move on with game.
 			startRound();
 			return true;
 		}
 
 		return false;
 	}
 
 
 
 	/* (non-Javadoc)
 	 * @see cs309.a1.shared.GameController#unpause()
 	 */
 	@Override
 	public void unpause() {
 		// send message to players
 		super.unpause();
 
 		// If the game is paused while the cards were waiting to
 		// be removed then we should remove them now
 		if(isWaitingToClearCards){
 			cardPlayingHandler.sendEmptyMessage(0);
 		}
 	}
 
 	/**
 	 * This function will be called when a players turn is over It will change
 	 * whoseTurn to the next player and send them the message that it is their
 	 * turn
 	 */
 	private void advanceTurn() {
 		incrementWhoseTurn();
 
 		if(whoseTurn == euchreGame.getTrickLeader()){
 			gameContext.updateUi();
 			startTrickEndWait();
 			return;
 		}
 
 		currentState = MSG_PLAY_CARD;
 
 		//tell the next person to play
 		sendNextTurn(currentState, euchreGame.getCardLead());
 
 		// Update the UI
 		gameContext.updateUi();
 	}
 
 
 	/**
 	 * This will end a trick, determine who won the trick,
 	 * add scores if round over, and clear off the cards played.
 	 */
 	private void endTurnRound(){
 		euchreGame.determineTrickWinner();
 
 		// Round is over if a player from both teams is out of cards
 		if(players.get(0).getCards().size() == 0 && players.get(1).getCards().size() == 0){
 			euchreGame.endRound();
 			if(euchreGame.isGameOver(players.get(0))){
 				declareWinner(euchreGame.getWinningTeam());
 				return;
 			}
 			declareRoundScores();
 		} else {
 			//round not over just the current trick
 
 			//TODO update scores on gameboard
 			gameContext.updateUi();
 
 			//make the trick leader to play next
 			whoseTurn = euchreGame.getTrickLeader();
 			currentState = PLAY_LEAD_CARD;
 
 			sendNextTurn(currentState, euchreGame.getCardLead());
 		}
 	}
 
 	/**
 	 * This method will send players info about the score
 	 */
 	private void declareRoundScores() {
 		Intent intent = new Intent(gameContext, RoundScoresActivity.class);
 		gameContext.startActivityForResult(intent, DECLARE_ROUND_SCORES);
 
 		for (int i = 0; i < game.getNumPlayers(); i++) {
 			server.write(ROUND_OVER, null, players.get(i).getId());
 		}
 	}
 
 	/**
 	 * This will send winner and loser messages to all the players depending on
 	 * if they won or not
 	 * 
 	 * @param whoWon
 	 *            The player that won
 	 */
 	private void declareWinner(int teamWhoWon) {
 		if (Util.isDebugBuild()) {
 			Log.d(TAG, "Sending winner and loser info");
 		}
 
 		// Let each player know whether they won or lost
 		for (int i = 0; i < game.getNumPlayers(); i++) {
 			if (i % 2 == teamWhoWon) {
 				server.write(Constants.MSG_WINNER, null, players.get(i).getId());
 			} else {
 				server.write(Constants.MSG_LOSER, null, players.get(i).getId());
 			}
 		}
 
 		String winner1Name = players.get(teamWhoWon).getName();
 		String winner2Name = players.get(teamWhoWon+2).getName();
 
 		String winningTeam = winner1Name + " and " + winner2Name;
 
 		super.declareWinner(winningTeam);
 	}
 
 
 	/* (non-Javadoc)
 	 * @see com.worthwhilegames.cardgames.shared.GameController#refreshPlayers()
 	 */
 	@Override
 	protected void refreshPlayers() {
 		Player pTurn = players.get(whoseTurn);
 
 		Card leadingCard = euchreGame.getCardLead();
 
 		JSONObject cardLeadObj = leadingCard.toJSONObject();
 
 		for (Player p : players) {
 			if (Util.isDebugBuild()) {
 				Log.d(TAG, p.getName() + " refreshed : " + p);
 			}
 
 			try {
 				JSONArray arr = new JSONArray();
 
 				// Create the base refresh info object
 				JSONObject refreshInfo = new JSONObject();
 				refreshInfo.put(Constants.KEY_TURN, pTurn.equals(p));
 				refreshInfo.put(Constants.KEY_CURRENT_STATE, currentState);
 				refreshInfo.put(Constants.KEY_PLAYER_NAME, p.getName());
 				refreshInfo.put(TRUMP, euchreGame.getTrump());
 				arr.put(refreshInfo);
 
 				// send the card on the discard pile
 				arr.put(cardLeadObj);
 
 				// send all the cards in the players hand
 				for (Card c : p.getCards()) {
 					arr.put(c.toJSONObject());
 				}
 
 				server.write(Constants.MSG_REFRESH, arr.toString(), p.getId());
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 		}
 
 
 		// If the next player is a computer, and the computer isn't currently
 		// playing, have the computer initiate a move
 		// TODO: test this vs method used in crazy eights?
 		if (players.get(whoseTurn).getIsComputer()) {
 			if (isComputerPlaying) {
 				// If a computer was playing before the game was refreshed
 				// let them know that they can play now
 				computerHandler.sendEmptyMessage(0);
 			} else {
 				//the computer turn has not even begun
 				startComputerTurn();
 			}
 		}
 	}
 
 
 	/**
 	 * Start a computer's turn.
 	 * 
 	 * Starts another thread that waits, and then posts a message to the
 	 * mHandler letting it know it can play.
 	 */
 	private void startTrickEndWait() {
 		isWaitingToClearCards = true;
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				try {
 					Thread.sleep(Constants.COMPUTER_WAIT_TIME);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 
 				if (Util.isDebugBuild()) {
 					Log.d(TAG, "startTrickEndWait: showing the cards that have been played at the end of a trick");
 				}
 
 				cardPlayingHandler.sendEmptyMessage(0);
 			}
 		}).start();
 	}
 
 	/**
 	 * This will play the computer's turn based on computer difficulty in preferences
 	 */
 	@Override
 	protected void playComputerTurn() {
 		if( currentState == FIRST_ROUND_BETTING || currentState == SECOND_ROUND_BETTING){
 			EuchreBet compBet = computerPlayer.getComputerBet(whoseTurn, currentState, euchreGame.getCardLead().getSuit());
 			this.handleBetting(currentState, compBet.toString());
 		} else {
 			Card cardSelected = null;
 
 			switch(currentState){
 			case PICK_IT_UP:
 				cardSelected = computerPlayer.pickItUp(whoseTurn);
 				break;
 			case PLAY_LEAD_CARD:
 				cardSelected = computerPlayer.getLeadCard(whoseTurn);
 				break;
 			case MSG_PLAY_CARD:
 				cardSelected = computerPlayer.getCardOnTurn(whoseTurn);
 				break;
 			default:
 				cardSelected = null;
 				break;
 			}
 
 			if( currentState == PICK_IT_UP ) {
 				//Get which card they discarded and discard it.
 				currentState = PLAY_LEAD_CARD;
 				euchreGame.discard(players.get(whoseTurn), cardSelected);
 				euchreGame.clearCardsPlayed();
 
 				//start the first turn of the round
 				whoseTurn = euchreGame.getTrickLeader();
 				sendNextTurn(currentState, euchreGame.getCardLead());
 				return;
 			}
 
 			if( currentState == PLAY_LEAD_CARD ) {
 				//set card lead
 				euchreGame.setCardLead(cardSelected);
 			}
 			mySM.playCardSound();
 			euchreGame.discard(players.get(whoseTurn), cardSelected);
 			advanceTurn();
 		}
 	}
 
 
 
 	/**
 	 * This will handle the betting that is done by the players
 	 * @param round - which round of betting it is
 	 * @param object - JSON object that represents the betting
 	 */
 	private void handleBetting(int round, String object) {
 		EuchreBet bet = null;
 		try {
 			JSONObject obj = new JSONObject(object);
 			int trumpSuit = obj.getInt(TRUMP);
 			boolean placeBet = obj.getBoolean(BET);
 			boolean goAlone = obj.getBoolean(GO_ALONE);
 
 			bet = new EuchreBet(trumpSuit, placeBet, goAlone);
 		} catch (JSONException ex) {
 			ex.printStackTrace();
 		}
 
 		//first round of betting
 		if(round == FIRST_ROUND_BETTING){
 			if(bet.getPlaceBet()){
 				if(bet.getTrumpSuit() == euchreGame.getCardLead().getSuit()){
 					//set trump and trump caller
 					euchreGame.setTrump(bet.getTrumpSuit());
 					euchreGame.setPlayerCalledTrump(whoseTurn);
 					this.boldBettingTeam();
 
 					// Update the Game board display with an indication of the current suit
 					gameContext.updateSuit(euchreGame.getTrump());
 
 					euchreGame.setPlayerGoingAlone(bet.getGoAlone());
 					if(euchreGame.isPlayerGoingAlone() && euchreGame.getTrickLeader() == euchreGame.getPlayerBeingSkipped() ){
 						euchreGame.setTrickLeader(euchreGame.getTrickLeader() + 1);
 					}
 					if( !euchreGame.isPlayerGoingAlone() || euchreGame.getDealer() != euchreGame.getPlayerBeingSkipped()){
 						whoseTurn = euchreGame.getDealer();
 						currentState = PICK_IT_UP;
 						players.get(euchreGame.getDealer()).addCard(euchreGame.getCardLead());
 					} else {
 						whoseTurn = euchreGame.getTrickLeader();
 						currentState = PLAY_LEAD_CARD;
 					}
 
 					//tell the dealer to "pick it up"
 					mySM.drawCardSound();
 					euchreGame.clearCardsPlayed();
 				} else {
 					// Don't let player choose suit other than card lead suit
 					refreshPlayers();
 					return;
 				}
 			} else {
 				if(whoseTurn == euchreGame.getDealer()){
 					//start betting round 2
 					currentState = SECOND_ROUND_BETTING;
 				}
 				incrementWhoseTurn();
 			}
 
 			//second round of betting
 		} else if(round == SECOND_ROUND_BETTING) {
 			if(bet.getPlaceBet()){
 				euchreGame.setTrump(bet.getTrumpSuit());
 				euchreGame.setPlayerCalledTrump(whoseTurn);
 				this.boldBettingTeam();
 				gameContext.updateSuit(euchreGame.getTrump());
 
 				euchreGame.setPlayerGoingAlone(bet.getGoAlone());
 				if(euchreGame.isPlayerGoingAlone() && euchreGame.getTrickLeader() == euchreGame.getPlayerBeingSkipped() ){
 					euchreGame.setTrickLeader(euchreGame.getTrickLeader() + 1);
 				}

				//set the turn to the first player to play and go
				whoseTurn = euchreGame.getTrickLeader();

 				euchreGame.clearCardsPlayed();
 
 				currentState = PLAY_LEAD_CARD;
 				refreshPlayers();
 			} else {
 				if(whoseTurn != euchreGame.getDealer()){
 					incrementWhoseTurn();
 				}
 				//if it is the second round and the dealer
 				//	passes we just keep telling him/her to bet. they must.
 				//  AKA "stick it to the dealer."
 			}
 		}
 
 		//all of the cases above should come to this statement.
 		sendNextTurn(currentState, euchreGame.getCardLead());
 	}
 
 
 	/**
 	 * Sends the next turn information to either the computer or player.
 	 * @param state the message type to send to the player or computer
 	 * @param card the card to give the player or computer
 	 */
 	private void sendNextTurn(int state, Card card){
 		currentState = state;
 		if (players.get(whoseTurn).getIsComputer()) {
 			if (!isComputerPlaying) {
 				startComputerTurn();
 			}
 		} else {
 			server.write(state, card, players.get(whoseTurn).getId());
 		}
 
 		// Highlight the name of the current player
 		gameContext.highlightPlayer(whoseTurn+1);
 
 		gameContext.updateUi();
 	}
 
 	/**
 	 * This function will change whose turn it is to the next player.
 	 */
 	private void incrementWhoseTurn(){
 		if (whoseTurn < game.getNumPlayers() - 1) {
 			whoseTurn++;
 		} else {
 			whoseTurn = 0;
 		}
 
 		if( euchreGame.isPlayerGoingAlone() && whoseTurn == euchreGame.getPlayerBeingSkipped() ){
 			incrementWhoseTurn();
 		}
 	}
 
 	/**
 	 * Bolds the betting team's names
 	 */
 	private void boldBettingTeam(){
 		gameContext.unboldAllPlayerText();
 		for(int i = 0; i < 4; i++){
 			if(euchreGame.getPlayerCalledTrump() % 2 == i %2){
 				gameContext.boldPlayerText(i);
 			}
 		}
 	}
 
 
 }
