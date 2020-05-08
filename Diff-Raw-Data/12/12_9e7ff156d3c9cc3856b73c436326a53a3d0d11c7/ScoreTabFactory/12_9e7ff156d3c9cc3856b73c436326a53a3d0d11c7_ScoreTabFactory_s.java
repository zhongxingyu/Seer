 package com.geeksong.agricolascorer;
 
 import java.util.Dictionary;
 
 import com.geeksong.agricolascorer.model.Score;
 
 import android.app.Activity;
import android.util.Log;
 import android.view.View;
import android.widget.RadioButton;
 import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
 import android.widget.TabHost;
 import android.widget.TextView;
 
 public class ScoreTabFactory implements TabHost.TabContentFactory {
 	private Dictionary<String, Score> scores;
 	private Activity tabHost;
 	
 	public ScoreTabFactory(Activity tabHost, Dictionary<String, Score> scores) {
 		this.scores = scores;
 		this.tabHost = tabHost;
 	}
 	
 	public View createTabContent(String playerName) {
 		View scorePlayer = tabHost.getLayoutInflater().inflate(R.layout.score_player, null);
 		Score score = new Score();
 		
 		TextView totalScoreView = (TextView) scorePlayer.findViewById(R.id.score);
 		totalScoreView.setText(Integer.toString(score.getTotalScore()));
 		
 		addCheckedChangeListener(scorePlayer, totalScoreView, score, R.id.fields);
 		scores.put(playerName, score);
 		
 		return scorePlayer;
 	}
 	
 	private void addCheckedChangeListener(View scorePlayer, TextView totalScoreView, Score score, int id) {
 		ScoreManager manager = new ScoreManager(score, totalScoreView);
 		
 		RadioGroup group = (RadioGroup) scorePlayer.findViewById(id);
 		group.setOnCheckedChangeListener(manager);
 	}
 }
