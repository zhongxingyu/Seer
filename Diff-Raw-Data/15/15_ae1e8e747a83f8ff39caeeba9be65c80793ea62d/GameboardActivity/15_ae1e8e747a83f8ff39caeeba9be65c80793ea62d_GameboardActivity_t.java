 package cs309.a1.gameboard.activities;
 
 import static cs309.a1.crazyeights.Constants.ID;
 import static cs309.a1.crazyeights.Constants.SUIT;
 import static cs309.a1.crazyeights.Constants.VALUE;
 import static cs309.a1.shared.CardGame.CRAZY_EIGHTS;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Matrix;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.Toast;
 import cs309.a1.crazyeights.Constants;
 import cs309.a1.crazyeights.CrazyEightGameRules;
 import cs309.a1.crazyeights.CrazyEightsCardTranslator;
 import cs309.a1.crazyeights.CrazyEightsTabletGame;
 import cs309.a1.gameboard.R;
 import cs309.a1.shared.Card;
 import cs309.a1.shared.CardTranslator;
 import cs309.a1.shared.Deck;
 import cs309.a1.shared.Game;
 import cs309.a1.shared.Player;
 import cs309.a1.shared.Rules;
 import cs309.a1.shared.Util;
 import cs309.a1.shared.bluetooth.BluetoothConstants;
 import cs309.a1.shared.bluetooth.BluetoothServer;
 
 public class GameboardActivity extends Activity {
 	
 	private static final int EXIT_GAME = "EXIT_GAME".hashCode();
 	
 	/**
 	 * The Logcat Debug tag
 	 */
 	private static final String TAG = GameboardActivity.class.getName();
 
 	/**
 	 * The request code to keep track of the "Are you sure you want to quit" activity
 	 */
 	private static final int QUIT_GAME = Math.abs("QUIT_GAME".hashCode());
 
 	/**
 	 * The request code to keep track of the "You have been disconnected" activity
 	 */
 	private static final int DISCONNECTED = Math.abs("DISCONNECTED".hashCode());
 	
 	private BluetoothServer bts;
 	
 	List<Player> players;
 
 	private static Game game = null;
 	
 	CardTranslator ct = new CrazyEightsCardTranslator();
 	
 	private int suitChosen = 0;
 	
 	
 	/**
 	 * This will be 0 to 3 to indicate the spot in the players array for the player currently taking their turn
 	 */
 	private int whoseTurn = 0; 
 
 	/**
 	 * The BroadcastReceiver for handling messages from the Bluetooth connection
 	 */
 	private BroadcastReceiver receiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			String action = intent.getAction();
 
 			if (BluetoothConstants.MESSAGE_RX_INTENT.equals(action)) {
 				String object = intent.getStringExtra(BluetoothConstants.KEY_MESSAGE_RX);
 				int messageType = intent.getIntExtra(BluetoothConstants.KEY_MESSAGE_TYPE, -1);
 				
 				switch(messageType){
 					case Constants.PLAY_CARD: 
 						discardReceivedCard(object);
 						advanceTurn();
 						break;
 					case Constants.PLAY_EIGHT_C:
 						suitChosen = Constants.SUIT_CLUBS;
 						discardReceivedCard(object);
 						advanceTurn();
 						break;
 					case Constants.PLAY_EIGHT_D:
 						suitChosen = Constants.SUIT_DIAMONDS;
 						discardReceivedCard(object);
 						advanceTurn();
 						break;
 					case Constants.PLAY_EIGHT_H:
 						suitChosen = Constants.SUIT_HEARTS;
 						discardReceivedCard(object);
 						advanceTurn();
 						break;
 					case Constants.PLAY_EIGHT_S:
 						suitChosen = Constants.SUIT_SPADES;
 						discardReceivedCard(object);
 						advanceTurn();
 						break;
 					case Constants.DRAW_CARD:
 						drawCard();
 						advanceTurn();
 						break; 
 				}
 				
 				// TODO: handle the message
 			} else if (BluetoothConstants.STATE_CHANGE_INTENT.equals(action)) {
 				// Handle a state change
 				int newState = intent.getIntExtra(BluetoothConstants.KEY_STATE_MESSAGE, BluetoothConstants.STATE_NONE);
 
 				// If the new state is anything but connected, display the "You have been disconnected" screen
 				if (newState != BluetoothConstants.STATE_CONNECTED) {
 					Intent i = new Intent(GameboardActivity.this, ConnectionFailActivity.class);
 					startActivityForResult(i, DISCONNECTED);
 				}
 			}
 		}
 	};
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.gameboard);
 		
 		ImageButton pause = (ImageButton) findViewById(R.id.gameboard_pause);
 		
 		pause.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent pauseButtonClick = new Intent(GameboardActivity.this, PauseMenuActivity.class);
 				startActivityForResult(pauseButtonClick, EXIT_GAME);
 			}
 		});
 
 		// Register the receiver for message/state change intents
 		registerReceiver(receiver, new IntentFilter(BluetoothConstants.MESSAGE_RX_INTENT));
 		registerReceiver(receiver, new IntentFilter(BluetoothConstants.STATE_CHANGE_INTENT));
 
 		bts = BluetoothServer.getInstance(this);
 
 		int numOfConnections = bts.getConnectedDeviceCount();
 		players = new ArrayList<Player>(); 
 		List<String> devices = bts.getConnectedDevices();
 
 		for (int i = 0; i < numOfConnections; i++){
 			Player p = new Player();
 			p.setId(devices.get(i));
 			p.setName("Player "+i);
 			players.add(p);
 		}
 
 		Rules rules = new CrazyEightGameRules();
 		Deck deck = new Deck(CRAZY_EIGHTS);
 		game = CrazyEightsTabletGame.getInstance(players, deck, rules);
 		game.setup();
 
		for (Player p : players) {
 			if (Util.isDebugBuild()) {
				Log.d(TAG, p.getName() + ": " + p);
 			}
 
			bts.write(Constants.SETUP, p, p.getId());
 		}
 
 		// If it is a debug build, show the cards face up so that we can
 		// verify that the tablet has the same cards as the player
 		if (Util.isDebugBuild()) {
 			int i = 1;
 			for (Player p : players) {
 				for (Card c : p.getCards()){
 					placeCard(i, c);
 				}
 
 				i++;
 			}
 
 			for (; i < 5; i++) {
 				placeCard(i, new Card(5, 0, R.drawable.back_blue_1, 54));
 				placeCard(i, new Card(5, 0, R.drawable.back_blue_1, 54));
 				placeCard(i, new Card(5, 0, R.drawable.back_blue_1, 54));
 				placeCard(i, new Card(5, 0, R.drawable.back_blue_1, 54));
 				placeCard(i, new Card(5, 0, R.drawable.back_blue_1, 54));
 			}
 
 			placeCard(0, game.getDiscardPileTop());
 		} else {
 			// Otherwise just show the back of the cards for all players
 			for (int i = 1; i < 5 * 5; i++) {
 				placeCard(i % 5, new Card(5, 0, R.drawable.back_blue_1, 54));
 			}
 
 			placeCard(0, game.getDiscardPileTop());
 		}
 		
 		advanceTurn();
 	}
 
 	@Override
 	public void onBackPressed() {
 		Intent intent = new Intent(this, QuitGameActivity.class);
 		startActivityForResult(intent, QUIT_GAME);
 	}
 
 	@Override
 	protected void onDestroy() {
 		// Disconnect Bluetooth connection
 		BluetoothServer.getInstance(this).disconnect();
 
 		// Unregister the receiver
 		unregisterReceiver(receiver);
 
 		super.onDestroy();
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == QUIT_GAME) {
 			if (resultCode == RESULT_OK) {
 				// Finish this activity
 				setResult(RESULT_OK);
 				finish();
 			}
 		} else if (requestCode == DISCONNECTED) {
 			if (requestCode == RESULT_OK) {
 				// TODO: DROP PLAYER
 			} else {
 				// TODO: CONNECT DIFFERENT PLAYER
 			}
 		}
 		
 		if (requestCode == EXIT_GAME) {
 			if (resultCode == RESULT_OK) {
 				// Finish this activity
 				setResult(RESULT_OK);
 				finish();
 			}
 		}
 
 		super.onActivityResult(requestCode, resultCode, data);
 	}
 
 	void placeCard(int location, Card newCard) {
 
 		LinearLayout ll;
 		LinearLayout.LayoutParams lp;
 
 		// convert dip to pixels
 		final float dpsToPixScale = getApplicationContext().getResources().getDisplayMetrics().density;
 		int pixels = (int) (125 * dpsToPixScale + 0.5f);
 
 		// place in discard pile
 		if(location == 0) {
 			ImageView discard = (ImageView) findViewById(R.id.discardpile);
 			discard.setImageResource(newCard.getResourceId());
 		}
 
 		// if Player 1 or Player 3
 		else if(location == 1 || location == 3) {
 
 			if(location == 1) ll = (LinearLayout) findViewById(R.id.player1ll);
 			else ll = (LinearLayout) findViewById(R.id.player3ll);
 
 			lp = new LinearLayout.LayoutParams(pixels, LinearLayout.LayoutParams.WRAP_CONTENT);
 
 			ImageView toAdd = new ImageView(this);
 			toAdd.setImageResource(newCard.getResourceId());
 			toAdd.setId(newCard.getIdNum());
 			toAdd.setAdjustViewBounds(true);
 			ll.addView(toAdd, lp);
 		}
 
 		// if Player 2 or Player 4
 		else if(location == 2 || location == 4) {
 
 			if(location == 2) ll = (LinearLayout) findViewById(R.id.player2ll);
 			else ll = (LinearLayout) findViewById(R.id.player4ll);
 
 			// rotate vertical card image 90 degrees
 			Bitmap verticalCard = BitmapFactory.decodeResource(getResources(), newCard.getResourceId());
 			Matrix tempMatrix = new Matrix();
 			tempMatrix.postRotate(90);
 			Bitmap horCard = Bitmap.createBitmap(verticalCard, 0, 0, verticalCard.getWidth(), verticalCard.getHeight(), tempMatrix, true);
 
 			ImageView toAdd = new ImageView(this);
 			toAdd.setImageBitmap(horCard);
 
 			lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, pixels);
 			toAdd.setAdjustViewBounds(true);
 			ll.addView(toAdd, lp);
 		}
 
 		else {
 			ImageView draw = (ImageView) findViewById(R.id.drawpile);
 			draw.setImageResource(newCard.getResourceId());
 		}
 	}
 	
 	private void advanceTurn(){
 		if(game.isGameOver(players.get(whoseTurn))){
 			declareWinner(whoseTurn);
 		}
 		
 		int numPlayers = game.getNumPlayers();
 		
 		if(whoseTurn<numPlayers-1){
 			whoseTurn++;
 		}else{
 			whoseTurn = 0;
 		}
 		
 		Card onDiscard = game.getDiscardPileTop();
 		if(onDiscard.getValue() == 7){
 			onDiscard = new Card(suitChosen, onDiscard.getValue(), onDiscard.getResourceId(), onDiscard.getIdNum());			
 		}
 		bts.write(Constants.IS_TURN, onDiscard, players.get(whoseTurn).getId());
 	}
 	
 	private void declareWinner(int whoWon){
 		
 		for(int i = 0; i<game.getNumPlayers(); i++){
 			if(i==whoWon){
 				bts.write(Constants.WINNER, null, players.get(i).getId());
 			}else{
 				bts.write(Constants.LOSER, null, players.get(i).getId());
 			}
 		}
 		//TODO start ending activity
 	}
 	
 	/**
 	 * This draws a card in the tablet game instance and sends that card to the player
 	 */
 	private void drawCard(){
 		Card tmpCard = game.draw(players.get(whoseTurn));
 		bts.write(Constants.CARD_DRAWN, tmpCard, players.get(whoseTurn).getId());		
 	}
 	
 	/**
 	 * This will take in the received card and discard it
 	 * @param object
 	 */
 	private void discardReceivedCard(String object){
 		Card tmpCard = new Card(0,0,0,0);
 		try {
 			JSONObject obj = new JSONObject(object);
 			int suit = obj.getInt(SUIT);
 			int value = obj.getInt(VALUE);
 			int id = obj.getInt(ID);
 			tmpCard = new Card(suit, value, ct.getResourceForCardWithId(id), id);
 			Toast.makeText(getApplicationContext(), "playing : " + tmpCard.getValue(), Toast.LENGTH_SHORT).show();
 			game.discard(players.get(whoseTurn), tmpCard);
 		} catch (JSONException ex) {
 			ex.printStackTrace();
 		}
 		placeCard(0, tmpCard);
 		
 	}
 
 }
