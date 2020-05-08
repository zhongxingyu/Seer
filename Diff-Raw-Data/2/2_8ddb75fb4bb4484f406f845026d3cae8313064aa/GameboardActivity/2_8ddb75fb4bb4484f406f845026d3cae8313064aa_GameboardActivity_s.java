 package com.worthwhilegames.cardgames.gameboard.activities;
 
 import static com.worthwhilegames.cardgames.shared.Constants.PREFERENCES;
 import static com.worthwhilegames.cardgames.shared.Constants.fourthCard;
 import static com.worthwhilegames.cardgames.shared.Constants.fullCard;
 import static com.worthwhilegames.cardgames.shared.Constants.halfCard;
 import static com.worthwhilegames.cardgames.shared.Constants.halfCardVertCut;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Matrix;
 import android.os.Bundle;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.ImageView.ScaleType;
 import android.widget.LinearLayout;
 
 import com.worthwhilegames.cardgames.R;
 import com.worthwhilegames.cardgames.shared.Card;
 import com.worthwhilegames.cardgames.shared.Constants;
 import com.worthwhilegames.cardgames.shared.Game;
 import com.worthwhilegames.cardgames.shared.GameController;
 import com.worthwhilegames.cardgames.shared.GameFactory;
 import com.worthwhilegames.cardgames.shared.Player;
 import com.worthwhilegames.cardgames.shared.TextView;
 import com.worthwhilegames.cardgames.shared.Util;
 import com.worthwhilegames.cardgames.shared.activities.QuitGameActivity;
 import com.worthwhilegames.cardgames.shared.connection.ConnectionConstants;
 import com.worthwhilegames.cardgames.shared.connection.ConnectionServer;
 
 /**
  * The Activity that the user will spend the most time in.  This
  * is where the Game logic happens, and each player will be able to
  * play a turn.
  */
 public class GameboardActivity extends Activity {
 
 	/**
 	 * The Logcat Debug tag
 	 */
 	private static final String TAG = GameboardActivity.class.getName();
 
 	/**
 	 * The request code to keep track of the Pause Menu activity
 	 */
 	private static final int PAUSE_GAME = Math.abs("PAUSE_GAME".hashCode());
 
 	/**
 	 * The request code to keep track of the "Are you sure you want to quit"
 	 * activity
 	 */
 	private static final int QUIT_GAME = Math.abs("QUIT_GAME".hashCode());
 
 	/**
 	 * The request code to keep track of the "You have been disconnected"
 	 * activity
 	 */
 	public static final int DISCONNECTED = Math.abs("DISCONNECTED".hashCode());
 
 	/**
 	 * The request code to keep track of the "Player N Won!" activity
 	 */
 	private static final int DECLARE_WINNER = Math.abs("DECLARE_WINNER".hashCode());
 
 	/**
 	 * LayoutParams for adding a card to a player on the long edge of the screen
 	 * 
 	 * width  = WRAP_CONTENT
 	 * height = cardHeight
 	 */
 	private static LinearLayout.LayoutParams cardParams;
 
 	/**
 	 * The height of each card
 	 */
 	private static int cardHeight;
 
 	/**
 	 * The height of each button
 	 */
 	private static int buttonHeight;
 
 	/**
 	 * Represents the resource id to use for the back of the cards
 	 */
 	private static int CARD_BACK;
 
 	/**
 	 * The maximum number of cards displayed for each player
 	 */
 	private static int[] maxDisplayed = new int[] { Constants.MAX_DISPLAYED, Constants.MAX_DIS_SIDES, Constants.MAX_DISPLAYED, Constants.MAX_DIS_SIDES };
 
 	/**
 	 * Holds the scaled Bitmaps of the suit images
 	 */
 	private static Bitmap[] scaledSuitImages = new Bitmap[4];
 
 	/**
 	 * The ConnectionServer that sends and receives messages from other devices
 	 */
 	private ConnectionServer connection;
 
 	/**
 	 * This will handle the specific logic of the game chosen, it will follow
 	 * the turn logic and Connection communication with players and also control
 	 * the current game board state
 	 */
 	private GameController gameController;
 
 	/**
 	 * The game instance
 	 */
 	private Game mGame;
 
 	/**
 	 * These are the TextViews for all the player names
 	 */
 	private TextView[] playerTextViews = new TextView[4];
 
 	/**
 	 * These are the LinearLayouts for all the player cards
 	 */
 	private LinearLayout[] playerLinearLayouts = new LinearLayout[4];
 
 	/**
 <<<<<<< HEAD
 	 * These are the TextViews for the count of remaining cards not being displayed
 	 */
 	private TextView[] playerRemainingCards = new TextView[4];
 
 	/**
 	 * The discard pile ImageView
 =======
 	 * The card position 1 ImageView
 >>>>>>> made 4 cards on the gameboard it is not pretty. also made the cards on
 	 */
 	private ImageView cardPosition1;
 
 	/**
 	 * The card position 2 (discard pile) ImageView
 	 */
 	private ImageView cardPosition2;
 
 	/**
 	 * The card position 3 ImageView
 	 */
 	private ImageView cardPosition3;
 
 	/**
 	 * The card position 4 (draw pile) ImageView
 	 */
 	private ImageView cardPosition4;
 
 	/**
 	 * The current suit ImageView
 	 */
 	private ImageView suitView;
 
 	/**
 	 * The SharedPreferences used to store preferences for the game
 	 */
 	private SharedPreferences sharedPreferences;
 
 	/**
 	 * The BroadcastReceiver for handling messages from the Connection connection
 	 */
 	private BroadcastReceiver receiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			String action = intent.getAction();
 
 			if (Util.isDebugBuild()) {
 				Log.d(TAG, "onReceive: " + action);
 			}
 
 			if (ConnectionConstants.STATE_CHANGE_INTENT.equals(action)) {
 				// Handle a state change
 				int newState = intent.getIntExtra(ConnectionConstants.KEY_STATE_MESSAGE, ConnectionConstants.STATE_NONE);
 
 				// If the new state is anything but connected, display the
 				// "You have been disconnected" screen
 				if (newState != ConnectionConstants.STATE_CONNECTED) {
 					String deviceId = intent.getStringExtra(ConnectionConstants.KEY_DEVICE_ID);
 					for (Player p : mGame.getPlayers()) {
 						if (p.getId().equalsIgnoreCase(deviceId)) {
 							p.clearName();
 							p.setDisconnected(true);
 						}
 					}
 					Intent i = new Intent(GameboardActivity.this, ConnectionFailActivity.class);
 					startActivityForResult(i, DISCONNECTED);
 
 					// Pause the players
 					gameController.pause();
 				}
 			} else {
 				// We didn't handle the Broadcast message here, so pass it on to
 				// the GameController
 				gameController.handleBroadcastReceive(context, intent);
 			}
 		}
 	};
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onCreate(android.os.Bundle)
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.gameboard);
 		initUIElements();
 
 		sharedPreferences = getSharedPreferences(PREFERENCES, 0);
 
 		// Get the image to use for the back of a card
 		CARD_BACK = sharedPreferences.getInt(Constants.CARD_BACK, R.drawable.back_blue_1);
 
 		// Update the refresh button image
 		ImageView refresh = (ImageView) findViewById(R.id.gameboard_refresh);
 		refresh.setImageBitmap(scaleButton(R.drawable.refresh_button));
 
 		// Register the BroadcastReceiver to handle all
 		// messages from the Connection module
 		registerReceiver();
 
 		connection = ConnectionServer.getInstance(this);
 		mGame = GameFactory.getGameInstance(this);
 
 		// Setup the rest of the Computer players based on the preferences
 		int currentNumPlayers = mGame.getNumPlayers();
 		int numComputers = sharedPreferences.getInt(Constants.NUMBER_OF_COMPUTERS, 3);
 		String computerDifficulty = sharedPreferences.getString(Constants.DIFFICULTY_OF_COMPUTERS, Constants.EASY);
 		for (int j = currentNumPlayers; j < 4 && (j - currentNumPlayers < numComputers); j++) {
 			Player p = new Player();
 			p.setName("Computer " + (j - currentNumPlayers + 1));
 			p.setId("Computer" + (j - currentNumPlayers + 1));
 			p.setPosition(j + 1);
 			p.setIsComputer(true);
 			p.setComputerDifficulty(computerDifficulty);
 
 			mGame.addPlayer(p);
 		}
 
 		// the GameController now handles the setup of the game.
 		gameController = GameFactory.getGameControllerInstance(this, connection, refresh);
 		mGame.setComputerDifficulty(computerDifficulty);
 
 		// Draw the names from the Game on the gameboard
 		updateNamesOnGameboard();
 	}
 
 	/**
 	 * Set up all the references to UI elements
 	 */
 	private void initUIElements() {
 		// Get references to commonly used UI elements
 		playerTextViews[0] = (TextView) findViewById(R.id.player1text);
 		playerTextViews[1] = (TextView) findViewById(R.id.player2text);
 		playerTextViews[2] = (TextView) findViewById(R.id.player3text);
 		playerTextViews[3] = (TextView) findViewById(R.id.player4text);
 
 		playerLinearLayouts[0] = (LinearLayout) findViewById(R.id.player1ll);
 		playerLinearLayouts[1] = (LinearLayout) findViewById(R.id.player2ll);
 		playerLinearLayouts[2] = (LinearLayout) findViewById(R.id.player3ll);
 		playerLinearLayouts[3] = (LinearLayout) findViewById(R.id.player4ll);
 
 		playerRemainingCards[0] = (TextView) findViewById(R.id.player1RemainingCount);
 		playerRemainingCards[1] = (TextView) findViewById(R.id.player2RemainingCount);
 		playerRemainingCards[2] = (TextView) findViewById(R.id.player3RemainingCount);
 		playerRemainingCards[3] = (TextView) findViewById(R.id.player4RemainingCount);
 
 		cardPosition1 = (ImageView) findViewById(R.id.cardPosition1);
 		cardPosition2 = (ImageView) findViewById(R.id.cardPosition2);
 		cardPosition3 = (ImageView) findViewById(R.id.cardPosition3);
 		cardPosition4 = (ImageView) findViewById(R.id.cardPosition4);
 
 		suitView = (ImageView) findViewById(R.id.gameboard_suit);
 
 		// Set up the scale factors for the card images
 		int screenHeight = getApplicationContext().getResources().getDisplayMetrics().heightPixels;
 		cardHeight = screenHeight / 4;
 		buttonHeight = screenHeight / 6;
 
 		// Update the size of the text in the name TextViews
 		for (TextView tv : playerTextViews) {
 			tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, screenHeight / 15);
 		}
 
 		//TODO set as a fourth of a card?
 		// Set up the layout params for the cards
		cardParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, cardHeight);
 
 		// Create the scaled suit images
 		scaledSuitImages[0] = scaleButton(R.drawable.clubsuitimage);
 		scaledSuitImages[1] = scaleButton(R.drawable.diamondsuitimage);
 		scaledSuitImages[2] = scaleButton(R.drawable.heartsuitimage);
 		scaledSuitImages[3] = scaleButton(R.drawable.spadesuitimage);
 
 		// Add the handler for the pause button
 		ImageView pause = (ImageView) findViewById(R.id.gameboard_pause);
 		pause.setImageBitmap(scaleButton(R.drawable.pause_button));
 		pause.setOnClickListener(new OnClickListener() {
 			/* (non-Javadoc)
 			 * @see android.view.View.OnClickListener#onClick(android.view.View)
 			 */
 			@Override
 			public void onClick(View v) {
 				gameController.pause();
 				Intent pauseButtonClick = new Intent(GameboardActivity.this, PauseMenuActivity.class);
 				startActivityForResult(pauseButtonClick, PAUSE_GAME);
 			}
 		});
 
 		// If this is a Google TV, rotate the text of player 3 so that it isn't upside down
 		if (Util.isGoogleTv(this)) {
 			if (null != playerTextViews[2]) {
 				playerTextViews[2].setRotation(180);
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onBackPressed()
 	 */
 	@Override
 	public void onBackPressed() {
 		Intent intent = new Intent(this, QuitGameActivity.class);
 		startActivityForResult(intent, QUIT_GAME);
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onDestroy()
 	 */
 	@Override
 	protected void onDestroy() {
 		// Disconnect Connection
 		if (connection != null) {
 			connection.disconnect();
 		}
 
 		// Unregister the receiver
 		try {
 			unregisterReceiver(receiver);
 		} catch (IllegalArgumentException e) {
 			// We didn't get far enough to register the receiver
 		}
 
 		super.onDestroy();
 	}
 
 	/**
 	 * Register the BroadcastReceiver for MESSAGE_RX_INTENTs and
 	 * STATE_CHANGE_INTENTs.
 	 */
 	public void registerReceiver() {
 		// Register the receiver for message/state change intents
 		registerReceiver(receiver, new IntentFilter(ConnectionConstants.MESSAGE_RX_INTENT));
 		registerReceiver(receiver, new IntentFilter(ConnectionConstants.STATE_CHANGE_INTENT));
 	}
 
 	/**
 	 * Unregister the BroadcastReceiver from all messages
 	 */
 	public void unregisterReceiver() {
 		// Unregister the receiver
 		try {
 			unregisterReceiver(receiver);
 		} catch (IllegalArgumentException e) {
 			// We didn't get far enough to register the receiver
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
 	 */
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == QUIT_GAME && resultCode == RESULT_OK) {
 			// Let the users know that the game is over
 			unregisterReceiver();
 			gameController.sendGameEnd();
 
 			// Finish this activity
 			setResult(RESULT_OK);
 			finish();
 		} else if (requestCode == PAUSE_GAME) {
 			if (resultCode == RESULT_CANCELED) {
 				// On the Pause Menu, they selected something that will end
 				// the game, so Finish this activity
 				unregisterReceiver();
 
 				// Let the users know that the game is over
 				gameController.sendGameEnd();
 
 				// And finish this activity
 				setResult(RESULT_OK);
 				finish();
 			} else {
 				gameController.unpause();
 			}
 		} else if (requestCode == DECLARE_WINNER) {
 			// We are coming back from the winner screen, so just go back to the
 			// main menu no matter what the result is.
 			setResult(RESULT_OK);
 			finish();
 		} else {
 			// If we didn't handle the result here, try handling it in the
 			// GameController
 			gameController.handleActivityResult(requestCode, resultCode, data);
 		}
 
 		super.onActivityResult(requestCode, resultCode, data);
 	}
 
 	/**
 	 * Update the names that are displayed on the Gameboard.
 	 *
 	 * This data is pulled from the Game instance
 	 */
 	public void updateNamesOnGameboard() {
 		List<Player> players = GameFactory.getGameInstance(this).getPlayers();
 		for (int i = 0; i < 4; i++) {
 			if (i < players.size()) {
 				playerTextViews[i].setVisibility(View.VISIBLE);
 				playerTextViews[i].setText(players.get(i).getName());
 			} else {
 				playerTextViews[i].setVisibility(View.INVISIBLE);
 			}
 		}
 	}
 
 	/**
 	 * This method will update the suit on the gameboard message center to show the player
 	 * the current suit of the last card played
 	 * 
 	 * @param suit the suit of the card in which to change the picture to
 	 */
 	public void updateSuit(int suit) {
 		if (suit >= 0 && suit < 4) {
 			suitView.setImageBitmap(scaledSuitImages[suit]);
 			suitView.setVisibility(View.VISIBLE);
 		} else {
 			suitView.setVisibility(View.INVISIBLE);
 		}
 	}
 
 	/**
 	 * Updates the User Interface
 	 * 
 	 * Places all cards in the users' hands
 	 * Updates the discard image
 	 * Updates the draw card image
 	 */
 	public void updateUi() {
 		Game game = GameFactory.getGameInstance(this);
 		List<Player> players = game.getPlayers();
 		int i = 0;
 
 		// Place images for all player's cards
 		for (Player p : players) {
 			List<Card> cards = p.getCards();
 			playerLinearLayouts[i].removeAllViews();
 
 			for (int j = 0; j < cards.size(); j++) {
 				Card c = cards.get(j);
 				ImageView image = new ImageView(this);
 				image.setId(c.getIdNum());
 				image.setScaleType(ScaleType.FIT_CENTER);
 
 				int resId = CARD_BACK;
 
 				// If we are in debug mode, show the face
 				// Otherwise stick with the back of the card
 				if (Util.isDebugBuild()) {
 					resId = c.getResourceId();
 				}
 
 				int cardsToDisplay = cards.size();
 				if (cardsToDisplay > maxDisplayed[i]) {
 					cardsToDisplay = maxDisplayed[i];
 				}
 
 				// Scale card
 				Bitmap scaledCard = scaleCard(resId, (j < (cardsToDisplay - 1)) ? fourthCard : halfCard);
 				image.setImageBitmap(scaledCard);
 
 				// Check for max displayed
 				if (j < maxDisplayed[i]) {
 					playerLinearLayouts[i].addView(image, cardParams);
 					playerRemainingCards[i].setVisibility(View.INVISIBLE);
 				} else {
 					// Display how many cards are remaining that aren't displayed
 					playerRemainingCards[i].setText("+" + Math.abs(maxDisplayed[i] - cards.size()));
 					playerRemainingCards[i].setVisibility(View.VISIBLE);
 					break;
 				}
 			}
 
 			i++;
 		}
 
 		// Card Position 1
 		Card card1 = game.getCardAtPosition(1);
 		if( card1 != null ){
 			Bitmap position1 = scaleCard(card1.getResourceId(), fullCard);
 			cardPosition1.setImageBitmap(position1);
 			cardPosition1.setVisibility(View.VISIBLE);
 		} else {
 			cardPosition1.setVisibility(View.INVISIBLE);
 		}
 
 		// Card Position 2 (discard)
 		Card card2 = game.getCardAtPosition(2);
 		if( card2 != null ){
 			Bitmap position2 = scaleCard(card2.getResourceId(), fullCard);
 			cardPosition2.setImageBitmap(position2);
 			cardPosition2.setVisibility(View.VISIBLE);
 		} else {
 			cardPosition2.setVisibility(View.INVISIBLE);
 		}
 
 		// Card Position 3
 		Card card3 = game.getCardAtPosition(3);
 		if( card3 != null ){
 			Bitmap position3 = scaleCard(card3.getResourceId(), fullCard);
 			cardPosition3.setImageBitmap(position3);
 			cardPosition3.setVisibility(View.VISIBLE);
 		} else {
 			cardPosition3.setVisibility(View.INVISIBLE);
 		}
 
 		// Card Position 4 (draw)
 		Card card4 = game.getCardAtPosition(4);
 		if( card4 != null ){
 			Bitmap position4 = scaleCard(card4.getResourceId(), fullCard);
 			cardPosition4.setImageBitmap(position4);
 			cardPosition4.setVisibility(View.VISIBLE);
 		} else {
 			cardPosition4.setVisibility(View.INVISIBLE);
 		}
 	}
 
 	/**
 	 * Scale a card image with the given resource
 	 * 
 	 * @param resId the resource id of the card to scale
 	 * @return a scaled card image
 	 */
 	private Bitmap scaleCard(int resId, int cardPortion) {
 		Bitmap fullCard = BitmapFactory.decodeResource(getResources(), resId);
 		float scaleFactor = (cardHeight + 0.0f) / fullCard.getHeight();
 		Matrix tempMatrix = new Matrix();
 		tempMatrix.setScale(scaleFactor, scaleFactor);
 
 		// Draw fourth card
 		if (cardPortion == fourthCard) {//TODO display 1/4 of a card
 			return Bitmap.createBitmap(fullCard, 0, 0,
 					fullCard.getWidth() / 2, fullCard.getHeight() / 2, tempMatrix, true);
 		} else if (cardPortion == halfCard) {
 			return Bitmap.createBitmap(fullCard, 0, 0,
 					fullCard.getWidth(), fullCard.getHeight() / 2, tempMatrix, true);
 		} else if (cardPortion == halfCardVertCut) {
 			return Bitmap.createBitmap(fullCard, 0, 0,
 					fullCard.getWidth() / 2, fullCard.getHeight(), tempMatrix, true);
 		} else {
 			return Bitmap.createBitmap(fullCard, 0, 0,
 					fullCard.getWidth(), fullCard.getHeight(), tempMatrix, true);
 		}
 	}
 
 	/**
 	 * Scale a button image with the given resource
 	 * 
 	 * @param resId the resource id of the card to scale
 	 * @return a scaled button image
 	 */
 	private Bitmap scaleButton(int resId) {
 		Bitmap fullImage = BitmapFactory.decodeResource(getResources(), resId);
 		float scaleFactor = (buttonHeight + 0.0f) / fullImage.getHeight();
 		Matrix tempMatrix = new Matrix();
 		tempMatrix.setScale(scaleFactor, scaleFactor);
 
 		return Bitmap.createBitmap(fullImage, 0, 0,
 				fullImage.getWidth(), fullImage.getHeight(), tempMatrix, true);
 	}
 
 	/**
 	 * Highlight the name of the person whose turn it is
 	 *
 	 * @param playerNumber the player whose turn it is
 	 */
 	public void highlightPlayer(int playerNumber) {
 		for (int i = 0; i < 4; i++) {
 			if ((i + 1) == playerNumber) {
 				playerTextViews[i].setTextColor(getResources().getColor(R.color.gold));
 			} else {
 				playerTextViews[i].setTextColor(getResources().getColor(android.R.color.black));
 			}
 		}
 	}
 }
