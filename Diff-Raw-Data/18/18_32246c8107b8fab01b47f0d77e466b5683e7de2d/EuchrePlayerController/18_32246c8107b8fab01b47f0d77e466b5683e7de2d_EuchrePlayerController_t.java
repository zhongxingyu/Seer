 package com.worthwhilegames.cardgames.euchre;
 
 import static com.worthwhilegames.cardgames.euchre.EuchreConstants.FIRST_ROUND_BETTING;
 import static com.worthwhilegames.cardgames.euchre.EuchreConstants.PICK_IT_UP;
 import static com.worthwhilegames.cardgames.euchre.EuchreConstants.PLAY_LEAD_CARD;
 import static com.worthwhilegames.cardgames.euchre.EuchreConstants.ROUND_OVER;
 import static com.worthwhilegames.cardgames.euchre.EuchreConstants.SECOND_ROUND_BETTING;
 import static com.worthwhilegames.cardgames.euchre.EuchreConstants.TRUMP;
 import static com.worthwhilegames.cardgames.shared.Constants.CURRENT_STATE;
 import static com.worthwhilegames.cardgames.shared.Constants.ID;
 import static com.worthwhilegames.cardgames.shared.Constants.IS_TURN;
 import static com.worthwhilegames.cardgames.shared.Constants.LOSER;
 import static com.worthwhilegames.cardgames.shared.Constants.PLAY_CARD;
 import static com.worthwhilegames.cardgames.shared.Constants.REFRESH;
 import static com.worthwhilegames.cardgames.shared.Constants.SETUP;
 import static com.worthwhilegames.cardgames.shared.Constants.SUIT;
 import static com.worthwhilegames.cardgames.shared.Constants.SUIT_CLUBS;
 import static com.worthwhilegames.cardgames.shared.Constants.SUIT_DIAMONDS;
 import static com.worthwhilegames.cardgames.shared.Constants.SUIT_HEARTS;
 import static com.worthwhilegames.cardgames.shared.Constants.SUIT_SPADES;
 import static com.worthwhilegames.cardgames.shared.Constants.VALUE;
 import static com.worthwhilegames.cardgames.shared.Constants.WINNER;
 
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.animation.ScaleAnimation;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 
 import com.worthwhilegames.cardgames.R;
 import com.worthwhilegames.cardgames.player.activities.GameResultsActivity;
 import com.worthwhilegames.cardgames.player.activities.SelectSuitActivity;
 import com.worthwhilegames.cardgames.player.activities.ShowCardsActivity;
 import com.worthwhilegames.cardgames.shared.Card;
 import com.worthwhilegames.cardgames.shared.CardTranslator;
 import com.worthwhilegames.cardgames.shared.Constants;
 import com.worthwhilegames.cardgames.shared.PlayerController;
 import com.worthwhilegames.cardgames.shared.SoundManager;
 import com.worthwhilegames.cardgames.shared.Util;
 import com.worthwhilegames.cardgames.shared.connection.ConnectionClient;
 import com.worthwhilegames.cardgames.shared.connection.ConnectionConstants;
 
 public class EuchrePlayerController implements PlayerController {
 
 
 	/**
 	 * The Logcat Debug tag
 	 */
 	private static final String TAG = EuchreGameController.class.getName();
 
 	/**
 	 * intent code for choosing suit
 	 */
 	private static final int CHOOSE_SUIT = Math.abs("CHOOSE_SUIT".hashCode());
 
 	/**
 	 * The request code to keep track of the "Are you sure you want to quit"
 	 * activity
 	 */
 	private static final int QUIT_GAME = Math.abs("QUIT_GAME".hashCode());
 
 	/**
 	 * The cards of this player
 	 */
 	private List<Card> cardHand;
 
 	/**
 	 * An instance of the ShowCardsActivity that can be used to display cards
 	 * and do other things as if this class was the ShowCardsActivity.
 	 */
 	private ShowCardsActivity playerContext;
 
 	/**
 	 * The card that is on the discard pile
 	 */
 	private Card cardLead;
 
 	/**
 	 * An instance of the GameRules that is used to check if a card can be
 	 * played
 	 */
 	private EuchreGameRules gameRules;
 
 	/**
 	 * The play button on the layout also the pass and discard button
 	 */
 	private Button play;
 
 	/**
 	 * The Bet button on the layout
 	 */
 	private Button bet;
 
 	/**
 	 * The Go Alone button on the layout
 	 */
 	private Button goAlone;
 
 	/**
 	 * The choose suit imageview that is used as a button
 	 */
 	private ImageView chooseSuit;
 
 	/**
 	 * This is true if it is the players turn
 	 */
 	private boolean isTurn = false;
 
 	/**
 	 * This is to keep track if a betting activity is open now or not
 	 */
 	private boolean isBettingNow = false;
 
 	/**
 	 * The current selected Card
 	 */
 	private Card cardSelected;
 
 	/**
 	 * The client that is used to send messages to the GameBoard
 	 */
 	private ConnectionClient connection;
 
 	/**
 	 * This is how we can make sure that the card resource IDs are correct
 	 */
 	private CardTranslator ct;
 
 	/**
 	 * This is a SoundManager instance that can do text to speech and other
 	 * sounds.
 	 */
 	private SoundManager mySM;
 
 	/**
 	 * The player's name
 	 */
 	private String playerName;
 
 	/**
 	 * The suit that is currently Trump
 	 */
 	private int trumpSuit;
 
 	/**
 	 * current state of play
 	 */
 	private int currentState;
 
 	/**
 	 * The LinearLayout holding all card images
 	 */
 	private LinearLayout playerHandLayout;
 
 	public EuchrePlayerController(Activity context, List<Card> cardHandGiven) {
 		playerContext = (ShowCardsActivity) context;
 		play = (Button) context.findViewById(R.id.passOption);
 		bet = (Button) context.findViewById(R.id.betOption);
 		goAlone = (Button) context.findViewById(R.id.goAloneOption);
 		chooseSuit = (ImageView) context.findViewById(R.id.betTrumpSuit);
 
 		play.setOnClickListener(getPlayOnClickListener());
 		bet.setOnClickListener(getBetOnClickListener());
 		goAlone.setOnClickListener(getGoAloneOnClickListener());
 		chooseSuit.setOnClickListener(getChooseSuitOnClickListener());
 		setButtonsEnabled(false);
 		mySM = SoundManager.getInstance(context);
 		cardHand = cardHandGiven;
 		playerName = "";
 		playerHandLayout = (LinearLayout)  playerContext.findViewById(R.id.playerCardContainer);
 
 		gameRules = new EuchreGameRules();
 		ct = new EuchreCardTranslator();
 		connection = ConnectionClient.getInstance(context);
 	}
 
 	/* (non-Javadoc)
 	 * @see com.worthwhilegames.cardgames.shared.PlayerController#handleBroadcastReceive(android.content.Context, android.content.Intent)
 	 */
 	@Override
 	public void handleBroadcastReceive(Context context, Intent intent) {
 		String action = intent.getAction();
 
 		if (ConnectionConstants.MESSAGE_RX_INTENT.equals(action)) {
 			String object = intent.getStringExtra(ConnectionConstants.KEY_MESSAGE_RX);
 			int messageType = intent.getIntExtra(ConnectionConstants.KEY_MESSAGE_TYPE, -1);
 
 			if (Util.isDebugBuild()) {
 				Log.d(TAG, "message: " + object);
 			}
 
 			switch (messageType) {
 			case SETUP:
 				// Parse the Message if it was to start the game over
 				cardHand.removeAll(cardHand);
 				playerContext.removeAllCards();
 				try {
 					JSONArray arr = new JSONArray(object);
 					for (int i = 0; i < arr.length(); i++) {
 						JSONObject obj = arr.getJSONObject(i);
 						int suit = obj.getInt(SUIT);
 						int value = obj.getInt(VALUE);
 						int id = obj.getInt(ID);
 						playerContext.addCard(new Card(suit, value, ct.getResourceForCardWithId(id), id));
 					}
 				} catch (JSONException ex) {
 					ex.printStackTrace();
 				}
 				setButtonsEnabled(false);
 				isTurn = false;
 				break;
 			case FIRST_ROUND_BETTING: /* purposely have both here */
 			case SECOND_ROUND_BETTING:
 				mySM.sayBet(playerName);
 				isTurn = true;
 				this.setCardLead(object);
 				currentState = messageType;
 				if(currentState == SECOND_ROUND_BETTING){
 					trumpSuit = -1;
 				} else {
 					trumpSuit = cardLead.getSuit();
 				}
 
 				//start select bet activity for round 1
 				// start select bet activity to let the player bet
 				isBettingNow = true;
 				bettingView();
 				setButtonsEnabled(true);
 				break;
 			case PLAY_LEAD_CARD:
 				mySM.sayTurn(playerName);
 				currentState = PLAY_LEAD_CARD;
 				setButtonsEnabled(true);
 				isTurn = true;
 				break;
 			case PICK_IT_UP:
 				currentState = PICK_IT_UP;
 				mySM.sayPickItUp(playerName);
 				try {
 					JSONObject obj = new JSONObject(object);
 					int suit = obj.getInt(SUIT);
 					int value = obj.getInt(VALUE);
 					int id = obj.getInt(ID);
 					playerContext.addCard(new Card(suit, value, ct.getResourceForCardWithId(id), id));
 				} catch (JSONException ex) {
 					ex.printStackTrace();
 				}
				isTurn = true;
 				dealerDiscardView();
 				break;
 
 			case IS_TURN:
 				mySM.sayTurn(playerName);
 				this.setCardLead(object);
 				currentState = PLAY_CARD;
 
 				setButtonsEnabled(true);
 				isTurn = true;
 				break;
 			case REFRESH:
 				// Parse the refresh Message
 				try {
 					JSONArray arr = new JSONArray(object);
 					JSONObject refreshInfo = arr.getJSONObject(0);
 					isTurn = refreshInfo.getBoolean(Constants.TURN);
 					currentState = refreshInfo.getInt(CURRENT_STATE);
 					playerName = refreshInfo.getString(Constants.PLAYER_NAME);
 					trumpSuit = refreshInfo.getInt(TRUMP);
 					// add more refresh info here
 
 					playerContext.removeAllCards();
 
 					JSONObject obj = arr.getJSONObject(1);
 					int suit = obj.getInt(SUIT);
 					int value = obj.getInt(VALUE);
 					int id = obj.getInt(ID);
 					cardLead = new Card(suit, value, ct.getResourceForCardWithId(id), id);
 
 					//the 2nd through however many are the cards of the player
 					for (int i = 2; i < arr.length(); i++) {
 						obj = arr.getJSONObject(i);
 						suit = obj.getInt(SUIT);
 						value = obj.getInt(VALUE);
 						id = obj.getInt(ID);
 						playerContext.addCard(new Card(suit, value, ct.getResourceForCardWithId(id), id));
 					}
 				} catch (JSONException ex) {
 					ex.printStackTrace();
 				}
 
 
 				if(isTurn && (currentState == FIRST_ROUND_BETTING ||  currentState == SECOND_ROUND_BETTING)){
 					if(isBettingNow){
 						//don't want to open 2 betting windows
 						break;
 					}
 					bettingView();
 					setButtonsEnabled(true);
 					isBettingNow = true;
 					break;
				} else if(isTurn && currentState == PICK_IT_UP){
					dealerDiscardView();
				} else {
					playingView();
 				}
 				setButtonsEnabled(isTurn);
 				cardSelected = null;
 				break;
 			case ROUND_OVER:
 				currentState = ROUND_OVER;
 				break;
 			case WINNER://TODO don't disconnect
 
 				Intent winner = new Intent(playerContext, GameResultsActivity.class);
 				winner.putExtra(GameResultsActivity.IS_WINNER, true);
 				playerContext.startActivityForResult(winner, QUIT_GAME);
 				break;
 			case LOSER://TODO don't disconnect
 				Intent loser = new Intent(playerContext, GameResultsActivity.class);
 				loser.putExtra(GameResultsActivity.IS_WINNER, false);
 				playerContext.startActivityForResult(loser, QUIT_GAME);
 				break;
 			}
 		}
 
 	}
 
 	@Override
 	public void handleActivityResult(int requestCode, int resultCode,
 			Intent data) {
 		if (requestCode == CHOOSE_SUIT) {
 			trumpSuit = resultCode;
 			updateTrumpSuit();
 		} else if (requestCode == QUIT_GAME && requestCode == Activity.RESULT_CANCELED) {
 			setButtonsEnabled(false);
 			cardHand.removeAll(cardHand);
 		}
 	}
 
 	@Override
 	public OnClickListener getPlayOnClickListener() {
 		return new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				if ( (isTurn && cardSelected != null) &&
 						( currentState == PICK_IT_UP || currentState == PLAY_LEAD_CARD ||
 						gameRules.checkCard(cardSelected, trumpSuit, cardLead, cardHand) ) && cardHand.size() != 0) {
 					// play card or discard if it is pick_it_up mode
 
 					connection.write(currentState, cardSelected);
 
 					playerContext.removeFromHand(cardSelected.getIdNum());
 
 					cardSelected = null;
 					if( currentState == PICK_IT_UP ){
 						play.setText(R.string.Play);
 						playingView();
 					}
 
 					setButtonsEnabled(false);
 					isTurn = false;
 				} else if (currentState == FIRST_ROUND_BETTING || currentState == SECOND_ROUND_BETTING){
 					EuchreBet bet = new EuchreBet(trumpSuit, false, false);
 
 					connection.write(currentState, bet.toString());
 
 					isBettingNow = false;
 
 					playingView();
 					setButtonsEnabled(false);
 					isTurn = false;
 				}
 
 			}
 		};
 	}
 
 
 	public OnClickListener getBetOnClickListener() {
 		return new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				if( currentState == FIRST_ROUND_BETTING || currentState == SECOND_ROUND_BETTING ) {
 					if(currentState == SECOND_ROUND_BETTING &&  trumpSuit == cardLead.getSuit() ){
 						trumpSuit = -1;
 						updateTrumpSuit();
 						//TODO tell player they can't choose the suit that was offered to bet on in the first round
 					}
 					if(trumpSuit == -1){
 						ScaleAnimation scale = new ScaleAnimation((float) 1.1, (float) 1.1,	(float) 1.2, (float) 1.2);
 						scale.scaleCurrentDuration(5);
 						chooseSuit.startAnimation(scale);
 						return;
 					}
 					EuchreBet bet = new EuchreBet(trumpSuit, true, false);
 
 					connection.write(currentState, bet.toString());
 
 					isBettingNow = false;
 
 					playingView();
 					setButtonsEnabled(false);
 					isTurn = false;
 				}
 			}
 		};
 	}
 
 	public OnClickListener getGoAloneOnClickListener() {
 		return new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				if( currentState == FIRST_ROUND_BETTING || currentState == SECOND_ROUND_BETTING ) {
 					if(currentState == SECOND_ROUND_BETTING &&  trumpSuit == cardLead.getSuit() ){
 						trumpSuit = -1;
 						updateTrumpSuit();
 						//TODO tell player they can't choose the suit that was offered to bet on in the first round
 					}
 					if(trumpSuit == -1){
 						ScaleAnimation scale = new ScaleAnimation((float) 1.1, (float) 1.1,	(float) 1.2, (float) 1.2);
 						scale.scaleCurrentDuration(5);
 						chooseSuit.startAnimation(scale);
 						return;
 					}
 					EuchreBet bet = new EuchreBet(trumpSuit, true, true);
 
 					connection.write(currentState, bet.toString());
 
 					isBettingNow = false;
 
 					playingView();
 					setButtonsEnabled(false);
 					isTurn = false;
 				}
 			}
 		};
 	}
 
 	public OnClickListener getChooseSuitOnClickListener() {
 		return new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				if( currentState == FIRST_ROUND_BETTING ){
 					//do nothing
 				} else if ( currentState == SECOND_ROUND_BETTING){
 					v.setEnabled(false);
 					Intent selectSuit = new Intent(playerContext, SelectSuitActivity.class);
 					playerContext.startActivityForResult(selectSuit, CHOOSE_SUIT);
 					v.setEnabled(true);
 				}
 			}
 		};
 	}
 
 	@Override
 	public OnClickListener getDrawOnClickListener() {
 		return null;
 	}
 
 	@Override
 	public OnClickListener getCardClickListener() {
 		return new CardSelectionClickListener();
 	}
 
 	/**
 	 * Used to set the play and draw buttons to enable or disabled
 	 * Also if it is the player's turn then set the cards to be greyed
 	 * out if they are not playable. if it is not the player's turn then
 	 * do not grey out any cards
 	 * 
 	 * @param isEnabled
 	 */
 	private void setButtonsEnabled(boolean isEnabled) {
 		play.setEnabled(isEnabled);
 		bet.setEnabled(isEnabled);
 		goAlone.setEnabled(isEnabled);
 		if (isEnabled && currentState == PLAY_CARD) {
 			// it is your turn grey out cards
 			for (Card c : cardHand) {
 				boolean isPlayable = gameRules.checkCard(c, trumpSuit, cardLead, cardHand) ;
 				playerContext.setCardPlayable(c.getIdNum(), isPlayable || currentState == PICK_IT_UP || currentState == PLAY_LEAD_CARD);
 			}
 		} else {
 			// it is not your turn make cards normal
 			if (playerHandLayout != null) {
 				for (int i = 0; i < playerHandLayout.getChildCount(); i++) {
 					ImageView v = (ImageView) playerHandLayout.getChildAt(i);
 					playerContext.setCardPlayable(v.getId(), true);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Sets the player's name
 	 * 
 	 * @param name - the player's name
 	 */
 	@Override
 	public void setPlayerName(String name) {
 		playerName = name;
 	}
 
 	private void dealerDiscardView(){
 		play.setText(R.string.Discard);
 		play.setVisibility(View.VISIBLE);
 		play.setEnabled(true);
 		bet.setVisibility(View.INVISIBLE);
 		bet.setEnabled(false);
 		goAlone.setVisibility(View.INVISIBLE);
 		goAlone.setEnabled(false);
 		chooseSuit.setVisibility(View.INVISIBLE);
 		chooseSuit.setEnabled(false);
 		isTurn = true;
 	}
 
 	private void bettingView(){
 		play.setText(R.string.pass_option);
 		play.setVisibility(View.VISIBLE);
 		play.setEnabled(true);
 		bet.setVisibility(View.VISIBLE);
 		bet.setEnabled(true);
 		goAlone.setVisibility(View.VISIBLE);
 		goAlone.setEnabled(true);
 		chooseSuit.setVisibility(View.VISIBLE);
 		chooseSuit.setEnabled(currentState == SECOND_ROUND_BETTING);
 		updateTrumpSuit();
 		isTurn = true;
 	}
 
 	private void playingView(){
 		play.setText(R.string.Play);
 		play.setVisibility(View.VISIBLE);
 		play.setEnabled(true);
 		bet.setVisibility(View.INVISIBLE);
 		bet.setEnabled(false);
 		goAlone.setVisibility(View.INVISIBLE);
 		goAlone.setEnabled(false);
 		chooseSuit.setVisibility(View.INVISIBLE);
 		chooseSuit.setEnabled(false);
 
 		isTurn = true;
 	}
 
 	/**
 	 * takes the JSON object and sets that as the CardLead for the round
 	 * @param object the card lead
 	 */
 	private void setCardLead(String object){
 		try {
 			JSONObject obj = new JSONObject(object);
 			int suit = obj.getInt(SUIT);
 			int value = obj.getInt(VALUE);
 			int id = obj.getInt(ID);
 			cardLead = new Card(suit, value, ct.getResourceForCardWithId(id), id);
 		} catch (JSONException ex) {
 			ex.printStackTrace();
 		}
 	}
 
 	/**
 	 * 
 	 */
 	private void updateTrumpSuit(){
 		switch(trumpSuit){
 
 		case SUIT_CLUBS:
 			chooseSuit.setBackgroundResource(R.drawable.clubsuitimage);
 			break;
 		case SUIT_DIAMONDS:
 			chooseSuit.setBackgroundResource(R.drawable.diamondsuitimage);
 			break;
 		case SUIT_HEARTS:
 			chooseSuit.setBackgroundResource(R.drawable.heartsuitimage);
 			break;
 		case SUIT_SPADES:
 			chooseSuit.setBackgroundResource(R.drawable.spadesuitimage);
 			break;
 		case (-1):
 		default:
 			//TODO set this to  ? image
 			chooseSuit.setBackgroundResource(R.drawable.spadesuitimage);
 			break;
 		}
 	}
 
 	/**
 	 * This will be used for each card ImageView and will allow the card to be
 	 * selected when it is Clicked
 	 */
 	private class CardSelectionClickListener implements View.OnClickListener {
 		@Override
 		public void onClick(View v) {
 			// Show an animation indicating the card was selected
 			ScaleAnimation scale = new ScaleAnimation((float) 1.2, (float) 1.2,	(float) 1.2, (float) 1.2);
 			scale.scaleCurrentDuration(5);
 			v.startAnimation(scale);
 
 			// Let the UI know which card was selected
 			playerContext.setSelected(v.getId());
 
 			for (int i = 0; i < cardHand.size(); i++) {
 				if (cardHand.get(i).getIdNum() == v.getId()) {
 					cardSelected = cardHand.get(i);
 				}
 			}
 		}
 	}
 
 }
