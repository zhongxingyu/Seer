 package com.banzz.lifecounter.activities;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 import com.banzz.lifecounter.R;
 import com.banzz.lifecounter.commons.TournamentAdapter;
 import com.banzz.lifecounter.commons.TournamentPlayer;
 import com.banzz.lifecounter.utils.Utils.Constants;
 import com.google.gson.Gson;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 
 public class TournamentActivity extends Activity {
 
 	private ArrayList<TournamentPlayer> playerList = new ArrayList<TournamentPlayer>();
 	private ListView mPlayers;
 	private int mRound = 0;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_tournament);
         playerList = getIntent().getParcelableArrayListExtra(Constants.KEY_TOURNAMENT_PLAYERS);
         saveAsLastPlayerList();
 
 //        TournamentPlayer Alex = new TournamentPlayer(0, "Alex", new ArrayList<Game>());
 //		playerList.add(Alex);
 //		TournamentPlayer Olivier = new TournamentPlayer(1, "Olivier", new ArrayList<Game>());
 //		playerList.add(Olivier);
 //		TournamentPlayer Basile = new TournamentPlayer(2, "Basile", new ArrayList<Game>());
 //		playerList.add(Basile);
 //		TournamentPlayer David = new TournamentPlayer(3, "David", new ArrayList<Game>());
 //		playerList.add(David);
 //		TournamentPlayer Flo = new TournamentPlayer(4, "Flo", new ArrayList<Game>());
 //		playerList.add(Flo);
 //		TournamentPlayer Greg = new TournamentPlayer(5, "Greg", new ArrayList<Game>());
 //		playerList.add(Greg);
 //		TournamentPlayer Olivier = new TournamentPlayer(6, "Olivier", new ArrayList<Game>());
 //		playerList.add(Olivier);
 //		TournamentPlayer Tanisha = new TournamentPlayer(7, "Tanisha", new ArrayList<Game>());
 //		playerList.add(Tanisha);
 		
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
 
         Button saveRound = (Button) findViewById(R.id.save_round);
         saveRound.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 saveRound();
             }
         });
 
         Button loadRound = (Button) findViewById(R.id.load_round);
         loadRound.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 loadRound();
             }
         });
 		
 		if (mRound >= playerList.size() - 1) {
 			nextRound.setVisibility(View.GONE);
 		}
 	}
 
    private void saveARound(int key)
     {
         Gson gson = new Gson();
         String json = gson.toJson(playerList);
         SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
         SharedPreferences.Editor edit = preferences.edit();
         edit.putString(getString(key), json);
         edit.commit();
     }
 
     private void saveRound()
     {
        saveARound(R.string.key_current_tournament);
     }
 
     private void saveAsLastPlayerList()
     {
        saveARound(R.string.key_last_tournament);
     }
 
     private void loadRound()
     {
         SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
         String load = preferences.getString(getString(R.string.key_current_tournament),
                 null);
 
         if (load != null)
         {
             Gson gson = new Gson();
             TournamentPlayer[] bob = gson.fromJson(load, (Class<TournamentPlayer[]>) TournamentPlayer[].class);
             playerList = new ArrayList<TournamentPlayer>(Arrays.asList(bob));
             mPlayers.setAdapter(new TournamentAdapter(this, playerList));
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
