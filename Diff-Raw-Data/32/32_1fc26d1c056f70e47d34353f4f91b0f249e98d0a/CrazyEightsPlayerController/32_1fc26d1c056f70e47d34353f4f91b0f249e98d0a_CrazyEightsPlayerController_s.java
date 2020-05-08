 package cs309.a1.crazyeights;
 
 import static cs309.a1.shared.Constants.CARD_DRAWN;
 import static cs309.a1.shared.Constants.ID;
 import static cs309.a1.shared.Constants.IS_TURN;
 import static cs309.a1.shared.Constants.LOSER;
 import static cs309.a1.shared.Constants.REFRESH;
 import static cs309.a1.shared.Constants.SETUP;
 import static cs309.a1.shared.Constants.SUIT;
 import static cs309.a1.shared.Constants.VALUE;
 import static cs309.a1.shared.Constants.WINNER;
 
 import java.util.ArrayList;
 
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
 import cs309.a1.R;
 import cs309.a1.player.activities.GameResultsActivity;
 import cs309.a1.player.activities.SelectSuitActivity;
 import cs309.a1.player.activities.ShowCardsActivity;
 import cs309.a1.shared.Card;
 import cs309.a1.shared.CardTranslator;
 import cs309.a1.shared.Constants;
 import cs309.a1.shared.PlayerController;
 import cs309.a1.shared.Rules;
 import cs309.a1.shared.SoundManager;
 import cs309.a1.shared.Util;
 import cs309.a1.shared.connection.ConnectionClient;
 import cs309.a1.shared.connection.ConnectionConstants;
 
 /**
  * The PlayerController implementation for Crazy Eights
  */
 public class CrazyEightsPlayerController implements PlayerController {
 
 	/**
 	 * The Logcat Debug tag
 	 */
 	private static final String TAG = CrazyEightsPlayerController.class.getName();
 
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
 	private ArrayList<Card> cardHand;
 
 	/**
 	 * An instance of the ShowCardsActivity that can be used to display cards
 	 * and do other things as if this class was the ShowCardsActivity.
 	 */
 	private ShowCardsActivity playerContext;
 
 	/**
 	 * The current selected Card
 	 */
 	private Card cardSelected;
 
 	/**
 	 * The card that is on the discard pile
 	 */
 	private Card cardOnDiscard;
 
 	/**
 	 * An instance of the GameRules that is used to check if a card can be
 	 * played
 	 */
 	private Rules gameRules;
 
 	/**
 	 * This is true if it is the players turn
 	 */
 	private boolean isTurn = false;
 
 	/**
 	 * The play button on the layout
 	 */
 	private Button play;
 
 	/**
 	 * The draw button on the layout
 	 */
 	private Button draw;
 
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
 	 * The LinearLayout holding all card images
 	 */
 	private LinearLayout playerHandLayout;
 
 	/**
 	 * This will initialize an instance of a CrazyEightsPlayerController
 	 * 
 	 * @param context This is an instance of the ShowCardsActivity
 	 * @param playGiven The Play button
 	 * @param drawGiven The Draw button
 	 * @param connectionGiven The connection client
 	 * @param cardHandGiven The list of cards that this player has
 	 */
 	public CrazyEightsPlayerController(Context context, Button playGiven,
 			Button drawGiven, ConnectionClient connectionGiven,	ArrayList<Card> cardHandGiven) {
 		playerContext = (ShowCardsActivity) context;
 		play = playGiven;
 		draw = drawGiven;
 		play.setOnClickListener(getPlayOnClickListener());
 		draw.setOnClickListener(getDrawOnClickListener());
 		setButtonsEnabled(false);
 		mySM = new SoundManager(context);
 		cardHand = cardHandGiven;
 		playerName = "";
 		playerHandLayout = (LinearLayout)  playerContext.findViewById(R.id.playerCardContainer);
 
 		gameRules = new CrazyEightGameRules();
 		ct = new CrazyEightsCardTranslator();
 		connection = connectionGiven;
 	}
 
 	/* (non-Javadoc)
 	 * @see cs309.a1.shared.PlayerController#handleBroadcastReceive(android.content.Context, android.content.Intent)
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
 				// Parse the Message if it was the original setup
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
 			case IS_TURN:
 				mySM.sayTurn(playerName);
 				try {
 					JSONObject obj = new JSONObject(object);
 					int suit = obj.getInt(SUIT);
 					int value = obj.getInt(VALUE);
 					int id = obj.getInt(ID);
 					cardOnDiscard = new Card(suit, value, ct.getResourceForCardWithId(id), id);
 				} catch (JSONException ex) {
 					ex.printStackTrace();
 				}
 				setButtonsEnabled(true);
 				isTurn = true;
 				break;
 			case CARD_DRAWN:
 				try {
 					JSONObject obj = new JSONObject(object);
 					int suit = obj.getInt(SUIT);
 					int value = obj.getInt(VALUE);
 					int id = obj.getInt(ID);
 					playerContext.addCard(new Card(suit, value, ct.getResourceForCardWithId(id), id));
 				} catch (JSONException ex) {
 					ex.printStackTrace();
 				}
 				break;
 			case REFRESH:
 				// Parse the refresh Message
 				try {
 					JSONArray arr = new JSONArray(object);
 					JSONObject refreshInfo = arr.getJSONObject(0);
 					isTurn = refreshInfo.getBoolean(Constants.TURN);
 					playerName = refreshInfo.getString(Constants.PLAYER_NAME);
 					// add more refresh info here
 
 					playerContext.removeAllCards();
 
 					JSONObject obj = arr.getJSONObject(1);
 					int suit = obj.getInt(SUIT);
 					int value = obj.getInt(VALUE);
 					int id = obj.getInt(ID);
 					cardOnDiscard = new Card(suit, value, ct.getResourceForCardWithId(id), id);
 
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
 				setButtonsEnabled(isTurn);
 				cardSelected = null;
 				break;
 			case WINNER:
 				playerContext.unregisterReceiver();
 				Intent winner = new Intent(playerContext, GameResultsActivity.class);
 				winner.putExtra(GameResultsActivity.IS_WINNER, true);
 				playerContext.startActivityForResult(winner, QUIT_GAME);
 				break;
 			case LOSER:
 				playerContext.unregisterReceiver();
 				Intent loser = new Intent(playerContext, GameResultsActivity.class);
 				loser.putExtra(GameResultsActivity.IS_WINNER, false);
 				playerContext.startActivityForResult(loser, QUIT_GAME);
 				break;
 			}
 		}
 
 	}
 
 	/* (non-Javadoc)
 	 * @see cs309.a1.shared.PlayerController#getPlayOnClickListener()
 	 */
 	@Override
 	public View.OnClickListener getPlayOnClickListener() {
 		return new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				if (isTurn && gameRules.checkCard(cardSelected, cardOnDiscard) && cardHand.size() != 0) {
 					// play card
 					if (cardSelected.getValue() == 7) {
 						Intent selectSuit = new Intent(playerContext, SelectSuitActivity.class);
 						playerContext.startActivityForResult(selectSuit, CHOOSE_SUIT);
 						// go to the onActivityResult to finish this turn
 					} else {
 						connection.write(Constants.PLAY_CARD, cardSelected);
 
 						playerContext.removeFromHand(cardSelected.getIdNum());
 
 						cardSelected = null;
 						setButtonsEnabled(false);
 						isTurn = false;
 					}
 				}
 			}
 		};
 	}
 
 	/* (non-Javadoc)
 	 * @see cs309.a1.shared.PlayerController#getDrawOnClickListener()
 	 */
 	@Override
 	public View.OnClickListener getDrawOnClickListener() {
 		return new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				if (isTurn) {
 					connection.write(Constants.DRAW_CARD, null);
 					setButtonsEnabled(false);
 					isTurn = false;
 				}
 			}
 		};
 	}
 
 	/* (non-Javadoc)
 	 * @see cs309.a1.shared.PlayerController#getCardClickListener()
 	 */
 	@Override
 	public OnClickListener getCardClickListener() {
 		return new CardSelectionClickListener();
 	}
 
 	/* (non-Javadoc)
 	 * @see cs309.a1.shared.PlayerController#handleActivityResult(int, int, android.content.Intent)
 	 */
 	@Override
 	public void handleActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == CHOOSE_SUIT) {
 			boolean isSuitChosen = true;
 			switch (resultCode) {
 			case Constants.SUIT_CLUBS:
 				connection.write(C8Constants.PLAY_EIGHT_C, cardSelected);
 				break;
 			case Constants.SUIT_DIAMONDS:
 				connection.write(C8Constants.PLAY_EIGHT_D, cardSelected);
 				break;
 			case Constants.SUIT_HEARTS:
 				connection.write(C8Constants.PLAY_EIGHT_H, cardSelected);
 				break;
 			case Constants.SUIT_SPADES:
 				connection.write(C8Constants.PLAY_EIGHT_S, cardSelected);
 				break;
 			case Activity.RESULT_OK:
 				isSuitChosen = false;
 				break;
 			}
 
 			if (isSuitChosen) {
 				playerContext.removeFromHand(cardSelected.getIdNum());
 				cardSelected = null;
 				setButtonsEnabled(false);
 				isTurn = false;
 			}
 		}
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
 		draw.setEnabled(isEnabled);
 		if (isEnabled) {
			// it is your turn grey out cards			
			for (int i = 0; i < playerHandLayout.getChildCount(); i++) {
			    ImageView v = (ImageView) playerHandLayout.getChildAt(i);
			    Card tmpCard = null;
			    for (int j = 0; i < cardHand.size(); i++) {
			    	if (cardHand.get(j).getIdNum() == v.getId()) {
			    		tmpCard = cardHand.get(j);
			    		break;
			    	}
			    }
			    
			    boolean isPlayable = gameRules.checkCard(tmpCard, cardOnDiscard); 
			    playerContext.setCardPlayable(v.getId(), isPlayable);
 			}
 		} else {
 			// it is not your turn make cards normal
			for (int i = 0; i < playerHandLayout.getChildCount(); i++) {
			    ImageView v = (ImageView) playerHandLayout.getChildAt(i);
			    playerContext.setCardPlayable(v.getId(), true);
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
