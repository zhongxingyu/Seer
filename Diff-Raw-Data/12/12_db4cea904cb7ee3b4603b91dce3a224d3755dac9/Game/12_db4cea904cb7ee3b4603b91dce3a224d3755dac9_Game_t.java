 package fr.eurecom.cardify;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.graphics.Point;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Display;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import fr.eurecom.messaging.Client;
 import fr.eurecom.util.CardDeck;
 import fr.eurecom.util.CardPlayerHand;
 import fr.eurecom.util.CardSortingRule;
 
 public class Game extends Activity {
 
 	private CardDeck deck;
 	private CardPlayerHand playerHand;
 	private Client client;
 	private TextView messageStream;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_game);
 		
 		messageStream = (TextView) findViewById(R.id.messageStream);
 		// If client, we receive cards from host at a later stage
 		deck = null;
		playerHand = new CardPlayerHand(this);
 		
 		// Set up client
 		String[] receiverAddresses = getIntent().getExtras().get("receivers").toString().split(",");
 		Boolean isHost = getIntent().getExtras().getBoolean("isHost");
 		this.client = new Client(this);
 		if (isHost) client.changeToHost();
 		
 		for (String inetAddr : receiverAddresses){
 			try {
 				client.addReceiver(InetAddress.getByName(inetAddr.substring(1)));
 			} catch (UnknownHostException e) {
 				
 				Log.e("Game:onCreate", e.getMessage());
 			}
 		} 
 
 		if (client.isHost()){
 			initGame();
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
 	private void initGame(){
 		//int numPlayers = client.getReceivers().size() + 1;
 		this.deck = new CardDeck(this);
 		deck.shuffle();
 		
 		int cardsPerPlayer = 6;
 		playerHand.dealCards(deck.draw(cardsPerPlayer));
 		
 		client.pushInitialCards(deck, cardsPerPlayer);
 	}
 	
 	public void addView(View v){
 		RelativeLayout layout = (RelativeLayout) findViewById(R.id.rootGameLayout);
 		layout.addView(v);
 	}
 	
 	public void removeView(View v){
 		RelativeLayout layout = (RelativeLayout) findViewById(R.id.rootGameLayout);
 		// Must recycle image resource!
 		layout.removeView(v);
 	}
 	
 	@Override
 	public void onBackPressed() {
 		//TODO: Special dialog for host?
 		new AlertDialog.Builder(this)
 			.setTitle("Are you sure you want to exit?")
 			.setMessage("This game will be abandoned")
 			.setNegativeButton(android.R.string.no, null)
 			.setPositiveButton(android.R.string.yes, new OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					Game.super.onBackPressed();
 					
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
 		
 	public Client getClient() {
 		return this.client;
 	}
 	
 	public CardPlayerHand getPlayerHand() {
 		return this.playerHand;
 	}
 	
 	public void printMessage(String message) {
 		messageStream.append(message);
 	}
 }
