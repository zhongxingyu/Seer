 package com.rushdevo.twittaddict;
 
 import java.io.InputStream;
 import java.net.URL;
 
 import android.app.Activity;
 import android.database.Cursor;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.util.TypedValue;
 import android.view.Gravity;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TabHost;
 import android.widget.TabHost.TabSpec;
 import android.widget.TabHost.OnTabChangeListener;
 import android.widget.TextView;
 
 import com.rushdevo.twittaddict.data.TwittaddictData;
 
 public class GameOverScreen extends Activity implements OnClickListener {
 	
 	LinearLayout highScoreContainer;
 	Button playAgainButton;
 	TwittaddictData db;
 		
 	Drawable selectedTab;
 	Drawable deselectedTab;
 	
 	TabHost tabHost;
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		selectedTab = getResources().getDrawable(R.drawable.selected_tab);
 		deselectedTab = getResources().getDrawable(R.drawable.deselected_tab);
 		setContentView(R.layout.game_over);
 		Bundle bundle = getIntent().getExtras();
 		// Show current score
 		int score = bundle.getInt("score");
 		String user = bundle.getString("user");
 		String bff = bundle.getString("bff");
 		String bffAvatarUrl = bundle.getString("bffAvatar");
 		Drawable bffAvatar = null;
 		try {
     		URL url = new URL(bffAvatarUrl);
     		InputStream is = (InputStream)url.getContent();
     		bffAvatar = Drawable.createFromStream(is, bff);
     	} catch (Exception e) {
     		// NOOP
     	}
     	
 		TextView scoreView = (TextView)findViewById(R.id.score);
 		scoreView.setText(Integer.toString(score));
 		// Add the high scores list
 		highScoreContainer = (LinearLayout)findViewById(R.id.high_score_container);
 		db = new TwittaddictData(this);
 		Cursor highScores = db.getHighScores();
 		int count = 0;
 		boolean matched = false;
 		TextView nextScoreContainer;
 		while (highScores.moveToNext()) {
 			count++;
 			int nextScore = highScores.getInt(3);
 			nextScoreContainer = new TextView(this);
			nextScoreContainer.setTextSize(TypedValue.COMPLEX_UNIT_PX, 15);
 			if (score == nextScore && !matched) {
 				// Color the first instance of a matched score in blue
 				matched = true;
 				nextScoreContainer.setTextColor(getResources().getColor(R.color.medium_blue));
 			} else {
 				nextScoreContainer.setTextColor(getResources().getColor(R.color.grey));
 			}
 			nextScoreContainer.setText(count+". "+nextScore);
 			highScoreContainer.addView(nextScoreContainer);
 		}
 		highScores.close();
 		// Setup the BFF tab
 		TextView bffLabelView = (TextView)findViewById(R.id.bff_label);
 		ImageView bffAvatarView = (ImageView)findViewById(R.id.bff_avatar);
 		TextView bffValueView = (TextView)findViewById(R.id.bff_value);
 		// TODO: Hit Twitter and get the most recent info for this user to display "diminish7's BFF is..." and "shanfu!"
 		bffAvatarView.setImageDrawable(bffAvatar);
 		bffLabelView.setText(user + getString(R.string.bff_label));
 		bffValueView.setText(bff + "!");
 		// Setup play-again button
 		playAgainButton = (Button)findViewById(R.id.play_again_button);
 		playAgainButton.setOnClickListener(this);
 		// Setup high-score and bff tabs
 		tabHost = (TabHost)findViewById(R.id.tab_host);
         tabHost.setup();
         tabHost.setOnTabChangedListener(new OnTabChangeListener() {
 			@Override
 			public void onTabChanged(String tabId) {
 				for (int i=0;i < tabHost.getTabWidget().getChildCount(); i++)
 			    {
 					tabHost.getTabWidget().getChildAt(i).setBackgroundDrawable(deselectedTab); //unselected
 			    }
 				tabHost.getTabWidget().getChildAt(tabHost.getCurrentTab()).setBackgroundDrawable(selectedTab); // selected
 			}
 		});
         
         String highScoreLabel = getString(R.string.high_score_tab);
         TabSpec highScoreSpec = tabHost.newTabSpec(highScoreLabel);
         highScoreSpec.setContent(R.id.high_score_tab);
         TextView tab = new TextView(this);
         tab.setHeight(20);
         tab.setBackgroundDrawable(selectedTab);
         tab.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM);
         tab.setText(getString(R.string.high_score_tab));
         highScoreSpec.setIndicator(tab);
         
         String bffLabel= getString(R.string.bff_tab);
         TabSpec bffSpec = tabHost.newTabSpec(bffLabel);
         bffSpec.setContent(R.id.bff_tab);
         tab = new TextView(this);
         tab.setHeight(20);
         tab.setBackgroundDrawable(deselectedTab);
         tab.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM);
         tab.setText(getString(R.string.high_score_tab));
         bffSpec.setIndicator(tab);
         
         tabHost.addTab(highScoreSpec);
         tabHost.addTab(bffSpec);
         tabHost.setCurrentTab(0);
 	}
 	
 	@Override
 	public void onClick(View v) {
 		switch(v.getId()) {
 		case R.id.play_again_button:
 			finish();
 			break;
 		}
 	}
 }
