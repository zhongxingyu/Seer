 package com.brennan.dartscorecard;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import com.brennan.gamelogic.Player;
 
 public class SettingsActivity extends Activity {
 	private EditText[] mPlayerNames;
 	private Button mAddMorePlayersButton, mLessPlayersButton, mStartButton;
 	private int numPlayers;
 	
 	private static final String TAG = "GameActivity"; 
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		numPlayers = 1;
 		setContentView(R.layout.activity_settings);
 		
 		//The edit texts of the players name
 		//There is probably a better way to do this than creating and hiding all edit texts
 		mPlayerNames = new EditText[4]; 
 		mPlayerNames[0] = (EditText) findViewById(R.id.playerOneInput);
 		mPlayerNames[1] = (EditText) findViewById(R.id.playerTwoInput);
 		mPlayerNames[1].setVisibility(View.GONE);
 		mPlayerNames[2] = (EditText) findViewById(R.id.playerThreeInput);
 		mPlayerNames[2].setVisibility(View.GONE);
 		mPlayerNames[3] = (EditText) findViewById(R.id.playerFourInput);
 		mPlayerNames[3].setVisibility(View.GONE);
 
 		//Set Buttons
 		mStartButton = (Button) findViewById(R.id.startButton);
 		mStartButton.setOnClickListener(startHandler);			
 		
 		mAddMorePlayersButton = (Button) findViewById(R.id.addMorePlayersButton);
 		mAddMorePlayersButton.setOnClickListener(addMorePlayersHandler);
 		
 		mLessPlayersButton = (Button) findViewById(R.id.lessPlayersButton);
 		mLessPlayersButton.setOnClickListener(lessPlayersHandler);
 		mLessPlayersButton.setVisibility(View.INVISIBLE);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_settings, menu);
 		return true;
 	}
 	
 	/*
 	 * Shows the next edit text for the next players
 	 */
 	View.OnClickListener addMorePlayersHandler = new View.OnClickListener() {
 		
 		@Override
 		public void onClick(View v) {
 			showNextPlayerField();
 			numPlayers++;
 			if (numPlayers >= 4){
 				mAddMorePlayersButton.setVisibility(View.INVISIBLE);
 			}
 			mLessPlayersButton.setVisibility(View.VISIBLE);
 		}
 	};
 	
 	/*
 	 * Removes the most recent edit text for players
 	 */
 	View.OnClickListener lessPlayersHandler = new View.OnClickListener() {
 		
 		@Override
 		public void onClick(View v) {
 			numPlayers--;
 			removePreviousPlayerField();
 			if (numPlayers <= 1){
 				mLessPlayersButton.setVisibility(View.INVISIBLE);
 			}
 			mAddMorePlayersButton.setVisibility(View.VISIBLE);		
 		}
 	};
 	
 	 /* 
 	 * This is triggered after the number of users has been selected
 	 * On return it starts this same intent with new params (number of player)
 	 */
 	View.OnClickListener startHandler = new View.OnClickListener() {
 		
 		@Override
 		public void onClick(View v) {
 			
 			Log.i(TAG, "Creating " + numPlayers + " players");
 			
 			ArrayList<Player> players = new ArrayList<Player>();
 			for(int i = 0; i < numPlayers; i++){
 				Player p = new Player(mPlayerNames[i].getText().toString());
 				if(p.getName().equals(""))
					p.setName("Player " + numPlayers);
 				players.add(p);
 			}
 			
 			Log.i(TAG, "Created players and added them to game");
 			Intent intent = new Intent(getBaseContext(), HammerGameActivity.class);
 			intent.putParcelableArrayListExtra("players", players);
 			startActivity(intent);
 		}
 	};
 
 	private void removePreviousPlayerField(){
 		mPlayerNames[numPlayers].setVisibility(View.GONE);
 	}
 	private void showNextPlayerField(){
 		mPlayerNames[numPlayers].setVisibility(View.VISIBLE);
 	}
 }
