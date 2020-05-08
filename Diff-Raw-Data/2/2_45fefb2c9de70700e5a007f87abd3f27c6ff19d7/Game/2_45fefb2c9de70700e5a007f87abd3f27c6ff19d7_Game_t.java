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
 import android.view.View;
 import android.widget.RelativeLayout;
 import fr.eurecom.messaging.Client;
 import fr.eurecom.util.CardDeck;
 import fr.eurecom.util.CardPlayerHand;
 
 public class Game extends Activity {
 
 	private CardDeck deck;
 	private CardPlayerHand playerHand;
 	private Client client;
 	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_game);
 		
 		String[] receiverAddresses = getIntent().getExtras().get("receivers").toString().split(",");
 		this.client = new Client(this);
 		for (String inetAddr : receiverAddresses){
 			try {
				client.addReceiver(InetAddress.getByName(inetAddr.substring(1)));
 			} catch (UnknownHostException e) {
 				
 				Log.e("Game:onCreate", e.getMessage());
 			}
 		}
 		initGame();
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
 	
 	private void initGame(){
 		this.deck = new CardDeck(this);
 		deck.shuffle();
 		
 		playerHand = new CardPlayerHand(this);
 		playerHand.dealInitialCards(deck.draw(13));
 	}
 	
 	public void addView(View v){
 		RelativeLayout layout = (RelativeLayout) findViewById(R.id.rootGameLayout);
 		layout.addView(v);
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
 	
 }
