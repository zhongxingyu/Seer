 package com.angrykings.activities;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 import com.angrykings.R;
 import com.angrykings.ServerConnection;
 import com.angrykings.ServerConnection.OnMessageHandler;
 
 public class EndGameActivity extends Activity {
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		
 		Boolean hasWon = false;
 		
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_endgame);
 		Bundle extras = getIntent().getExtras();
 		if (extras != null) {
 			hasWon = extras.getBoolean("hasWon");
 		}
 		
 		final TextView winLoseText = (TextView) findViewById(R.id.winLoseText);
 		
 		if(hasWon) {
 			winLoseText.setText(R.string.hasWon);
 		} else {
 			winLoseText.setText(R.string.hasLost);
 		}
 		
 		ServerConnection.getInstance().setHandler(new OnMessageHandler() {
 			
 			@Override
 			public void onMessage(String payload) {
 			}
 		});
 		
 		final Button bLobby = (Button) findViewById(R.id.backToLobby);
 		bLobby.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				Intent intent = new Intent(getApplicationContext(),
						LobbyActivity.class);
 				startActivity(intent);
 			}
 
 		});
 		
 	}
 
 }
