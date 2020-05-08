 package com.banzz.lifecounter.activities;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.banzz.lifecounter.R;
 import com.banzz.lifecounter.commons.Game;
 import com.banzz.lifecounter.commons.TournamentAdapter;
 import com.banzz.lifecounter.commons.TournamentPlayer;
 import com.banzz.lifecounter.utils.Utils.Constants;
 
 public class TournamentActivity extends Activity {
 
 	private ArrayList<TournamentPlayer> playerList = new ArrayList<TournamentPlayer>();
 	private ListView mPlayers;
 	private int mRound = 0;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_tournament);
 		
 		TournamentPlayer Alex = new TournamentPlayer(0, "Alex", new ArrayList<Game>());  
 		playerList.add(Alex);
		TournamentPlayer Andrew = new TournamentPlayer(1, "Andrew", new ArrayList<Game>());  
		playerList.add(Andrew);
 		TournamentPlayer Basile = new TournamentPlayer(2, "Basile", new ArrayList<Game>());  
 		playerList.add(Basile);
 		TournamentPlayer David = new TournamentPlayer(3, "David", new ArrayList<Game>());  
 		playerList.add(David);
 		TournamentPlayer Flo = new TournamentPlayer(4, "Flo", new ArrayList<Game>());  
 		playerList.add(Flo);
 		TournamentPlayer Greg = new TournamentPlayer(5, "Greg", new ArrayList<Game>());  
 		playerList.add(Greg);
		TournamentPlayer Olivier = new TournamentPlayer(6, "Olivier", new ArrayList<Game>());  
		playerList.add(Olivier);
		TournamentPlayer Tanisha = new TournamentPlayer(7, "Tanisha", new ArrayList<Game>());  
		playerList.add(Tanisha);
 		
 		mPlayers = (ListView) findViewById(R.id.player_list);
 		Collections.sort(playerList, new PlayerComparator(true));
 		mPlayers.setAdapter(new TournamentAdapter(this, playerList));
 		
 		TextView roundText = (TextView) findViewById(R.id.tournament_round);
 		roundText.setText(getText(R.string.Round).toString() + " " + mRound);
  		
 		Button nextRound = (Button) findViewById(R.id.next_round);
 		nextRound.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(TournamentActivity.this, RoundActivity.class);
 				intent.putExtra(Constants.KEY_TOURNAMENT_PLAYERS, playerList);
 				startActivityForResult(intent, Constants.REQUEST_NEXT_ROUND);
 			}
 		});
 		
 		if (mRound >= playerList.size() - 1) {
 			nextRound.setVisibility(View.GONE);
 		}
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
 		switch (requestCode) {
 	        case Constants.REQUEST_NEXT_ROUND:
 	        	if (resultCode == Activity.RESULT_OK) {
 	        		playerList = intent.getParcelableArrayListExtra(Constants.KEY_TOURNAMENT_PLAYERS);
 	        		
 	        		Collections.sort(playerList, new PlayerComparator(true));
 	        		mPlayers.setAdapter(new TournamentAdapter(this, playerList));
 	        		
 	        		mRound++;
 	        		
 	        		TextView roundText = (TextView) findViewById(R.id.tournament_round);
 	        		roundText.setText(getText(R.string.Round).toString() + " " + mRound);
 	        	}
 	        	break;
 		}
 	}
 }
