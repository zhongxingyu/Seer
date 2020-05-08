 package fr.eurecom.cardify;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.graphics.Point;
 import android.net.wifi.p2p.WifiP2pManager;
 import android.net.wifi.p2p.WifiP2pManager.Channel;
 import android.os.Bundle;
 import android.text.method.ScrollingMovementMethod;
 import android.util.Log;
 import android.view.Display;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import fr.eurecom.messaging.Client;
 import fr.eurecom.util.CardDeck;
 import fr.eurecom.util.CardPlayerHand;
 import fr.eurecom.util.CardSolitaireHand;
 import fr.eurecom.util.CardSortingRule;
 
 public class Game extends Activity {
 
 	private CardDeck deck;
 	private CardPlayerHand playerHand;
 	private CardSolitaireHand solitaireHand;
 	private Client client;
 	private TextView messageStream;
 	private ProgressDialog progressDialog;
 	private int cardsPerPlayer;
 	private boolean isSolitaire;
 	public static Point screenSize;
 	private WifiP2pManager mManager;
 	private Channel mChannel;
 	private String deviceName;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_game);
 		
 		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
 		Display display = wm.getDefaultDisplay();
 		screenSize = new Point();
 		display.getSize(screenSize);
 		
 		//Keeps screen on
 		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 		
 		progressDialog = new ProgressDialog(this);
 		
 		if((Boolean) getIntent().getExtras().get("isSolitaire")) {
 			isSolitaire = true;
 			initSolitaire();
 		} else {
 			isSolitaire = false;
 			
 			mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
 			mChannel = mManager.initialize(this, getMainLooper(), null);
 			String[] receiverAddresses = getIntent().getExtras().get("receivers").toString().split(",");
 			Boolean isHost = getIntent().getExtras().getBoolean("isHost");
 			deviceName = getIntent().getExtras().get("deviceName").toString();
 			
 			if (!isHost) {
 				showProgressDialog("Loading...", "Waiting for all players");
 			} else {
 				cardsPerPlayer = getIntent().getExtras().getInt("cardsPerPlayer");
 			}
 			
 			messageStream = (TextView) findViewById(R.id.messageStream);
 			messageStream.setMovementMethod(new ScrollingMovementMethod());
 			// If client, we receive cards from host at a later stage
 			deck = null;
 			playerHand = new CardPlayerHand(this);
 			
 			// Set up client
 			this.client = new Client(this);
 			if (isHost) {
 				client.changeToHost();
 			} 
 			
 			for (String inetAddr : receiverAddresses){
 				try {
 					client.addReceiver(InetAddress.getByName(inetAddr.substring(1)));
 				} catch (UnknownHostException e) {
 					Log.e("Game:onCreate", e.getMessage());
 				} catch (StringIndexOutOfBoundsException e) {
 					Log.e("Game:onCreate", e.getMessage());
 				}
 			}
 
 			if (client.isHost()){
 				initGame();
 			} else {
 				Log.e("Game", "Notifying game is initialized");
 				client.publishGameInitialized();
 			}
 		}
 	}
 	
 	public void disconnectFromDevices() {
 		mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
 			
 			@Override
 			public void onSuccess() {
 				
 			}
 			
 			@Override
 			public void onFailure(int reason) {
 				
 			}
 		});
 	}
 	
 	private void showProgressDialog(String title, String message) {
 		this.progressDialog.setTitle(title);
 		this.progressDialog.setMessage(message);
 		this.progressDialog.setCancelable(false);
 		this.progressDialog.show();
 	}
 	
 	public void dismissProgressDialog() {
 		if (progressDialog.isShowing()) {
 			progressDialog.dismiss();
 		}
 	}
 	
 	public Point getDisplaySize(){
 		Display display = getWindowManager().getDefaultDisplay();
 		Point displaySize = new Point();
 		display.getSize(displaySize);
 		return displaySize;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.game, menu);
 		return true;
 	}
 	
 	// Set up game if the this.client is host
 	private void initGame() {
 		this.deck = new CardDeck(this);
 		deck.shuffle();
 		
 		playerHand.dealCards(deck.draw(cardsPerPlayer));
 		playerHand.setGhost();
 		addView(deck);
 		
 		client.pushInitialCards(deck, cardsPerPlayer);
 		client.pushRemainingDeck(deck);
 	}
 	
 	private void initSolitaire() {
 		this.deck = new CardDeck(this);
 		deck.shuffle();
 		
 		this.solitaireHand = new CardSolitaireHand(this);
 		solitaireHand.dealCards(deck.draw(5));
 		solitaireHand.setGhost();
 		addView(deck);		
 	}
 	
 	public void addView(View v){
 		RelativeLayout layout = (RelativeLayout) findViewById(R.id.rootGameLayout);
 		layout.addView(v);
 	}
 	
 	public void removeView(View v){
 		RelativeLayout layout = (RelativeLayout) findViewById(R.id.rootGameLayout);
 		layout.removeView(v);
 	}
 	
 	@Override
 	public void onBackPressed() {
 		
 		new AlertDialog.Builder(this)
 			.setTitle("Are you sure you want to exit?")
 			.setMessage("This game will be abandoned")
 			.setNegativeButton(android.R.string.no, null)
 			.setPositiveButton(android.R.string.yes, new OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					exitGame();
 				}
 			}).create().show();
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id.NO_SUIT_ACE_HIGH:
 				playerHand.sortCards(CardSortingRule.NO_SUIT_ACE_HIGH);
 				return true;
 			case R.id.NO_SUIT_ACE_LOW:
 				playerHand.sortCards(CardSortingRule.NO_SUIT_ACE_LOW);
 				return true;
 			case R.id.S_D_H_C_ACE_HIGH:
 				playerHand.sortCards(CardSortingRule.S_H_D_C_ACE_HIGH);
 				return true;
 			case R.id.S_D_H_C_ACE_LOW:
 				playerHand.sortCards(CardSortingRule.S_H_D_C_ACE_LOW);
 				return true;
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 	}
 	
 	public void exitGame() {
 		if (isSolitaire) {
 			exitSolitaire();
 		} else {
 			exitMultiplayerGame();
 		}
 	}
 		
 	public void exitMultiplayerGame() {
 		client.publishDisconnect();
 		client.disconnect();
 		disconnectFromDevices();
 		finish();
 		startActivity(new Intent(Game.this, MainMenu.class));
 	}
 	
 	private void exitSolitaire() {
 		finish();
 	}
 		
 	public Client getClient() {
 		return this.client;
 	}
 	
 	public CardPlayerHand getPlayerHand() {
 		return this.playerHand;
 	}
 	
 	public TextView getMessageStream() {
 		return messageStream;
 	}
 	
 	public CardDeck getDeck() {
 		return deck;
 	}
 	
 	public void setDeck(CardDeck deck) {
 		this.deck = deck;
 	}
 	
 	public String getDeviceName() {
 		return deviceName;
 	}
 	
 	@Override
 	protected void onDestroy() {
		exitGame();
 		super.onDestroy();
 	}
 }
