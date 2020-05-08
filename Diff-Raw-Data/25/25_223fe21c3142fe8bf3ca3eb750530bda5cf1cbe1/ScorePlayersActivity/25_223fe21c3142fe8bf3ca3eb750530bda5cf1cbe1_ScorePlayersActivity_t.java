 package com.geeksong.agricolascorer;
 
 import java.util.ArrayList;
 
 import com.geeksong.agricolascorer.model.Player;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.TabHost;
 import android.widget.TabHost.TabSpec;
 import android.widget.TextView;
 
 public class ScorePlayersActivity extends Activity {
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_score_players);
 
         TabHost tabs = (TabHost) findViewById(android.R.id.tabhost);
         tabs.setup();
         
         ScoreTabFactory factory = new ScoreTabFactory(this);
         LayoutInflater inflater = getLayoutInflater();
         
         ArrayList<Player> playerList = GameCache.getInstance().getPlayerList();
         for(int i = 0; i < playerList.size(); i++ ) {
         	String playerName = playerList.get(i).getName();
             TabSpec tab = tabs.newTabSpec(playerName);
             
             View tabButtonView = inflater.inflate(R.layout.score_tab_button, null);
             TextView tabText = (TextView) tabButtonView.findViewById(R.id.tabText);
             tabText.setText(playerName);
             
             tab.setContent(factory);
             tab.setIndicator(tabButtonView);
             tabs.addTab(tab);
         }
     }
     
    @Override
    public void onBackPressed(){
        finish();
    }
    
 	public void finishScoring(View source) {
 		Intent finishGameIntent = new Intent(this, FinishedActivity.class);
 		startActivity(finishGameIntent);
 	}
 }
