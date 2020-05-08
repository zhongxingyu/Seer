 package com.brennan.dartscorecard;
 
 import java.util.ArrayList;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.graphics.Color;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.FrameLayout;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 import com.brennan.dartscorecard.R;
 import com.brennan.gamelogic.HammerGame;
 import com.brennan.gamelogic.Player;
 
 public class HammerGameActivity extends Activity {
 
 
 	private static final String TAG = "HammerGameActivity";
 	private TextView round_mark, dart2_text, dart3_text;
 	private RelativeLayout mRelativeLayout;
 	ArrayList<Player> players;
 	private Button nextTurnButton, prevTurnButton;
 	HammerGame game;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		//Retrieve extras
 		Bundle extras = getIntent().getExtras();
 		if(extras != null){
 			players = extras.getParcelableArrayList("players");
 		}
 
		Log.v(TAG,"Recived players in intent");
 
 		game = new HammerGame();
 		game.addPlayers(players);
 		//game.setCurrentRound(game.getMarks().get(0));
 		setContentView(R.layout.activity_hammer_game);
 		
 		mRelativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
 		round_mark = (TextView) findViewById(R.id.round_mark);
 		dart2_text = (TextView) findViewById(R.id.dart2_text);
 		dart3_text = (TextView) findViewById(R.id.dart3_text);
 		
 		nextTurnButton = (Button) findViewById(R.id.next_turn_button);
 		nextTurnButton.setOnClickListener(nextTurnHandler);
 		
 		prevTurnButton = (Button) findViewById(R.id.prev_turn_button);
 		prevTurnButton.setEnabled(false);
 		prevTurnButton.setOnClickListener(prevTurnHandler);
 		
 		//		for(int i = 0; i < players.size(); i++){
 		//			Log.v(TAG, "Player " + i + ": " + players.get(i).getName());
 		//			FrameLayout f = new FrameLayout(this);
 		//			
 		//			View v = new View(this);
 		//			LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, 10);
 		//			
 		//			v.setBackgroundColor(Color.BLUE);
 		//			v.setLayoutParams(params);
 		//			
 		//			View v2 = new View(this);
 		//			LayoutParams params2 = new LayoutParams(10, LayoutParams.MATCH_PARENT);
 		//			v2.setBackgroundColor(Color.GREEN);
 		//			v2.setLayoutParams(params2);
 		//			
 		//			f.addView(v);
 		//			f.addView(v2);
 		//			
 		//			FrameLayout.LayoutParams fParams = new FrameLayout.LayoutParams(100, 100);
 		//			f.setLayoutParams(fParams);
 		//			
 		//			RelativeLayout.LayoutParams rParams = new RelativeLayout.LayoutParams(200, 100);
 		//			rParams.addRule(RelativeLayout.BELOW, R.id.textView1);
 		//			
 		//			mRelativeLayout.addView(f, rParams);
 		//		}
 
 		//startGame();
 	}
 
 	/*
 	 * Shows the next edit text for the next players
 	 */
 	View.OnClickListener nextTurnHandler = new View.OnClickListener() {
 		
 		@Override
 		public void onClick(View v) {
 			game.setCurrentRound(game.getCurrentRound() + 1);
 			prevTurnButton.setEnabled(true);
 			if(!(game.getCurrentRound() >= game.getMarks().size())){
 				round_mark.setText(game.getMarks().get(game.getCurrentRound()).toString());
 			}
 			if(game.getCurrentRound() + 1 == game.getMarks().size())
 				nextTurnButton.setEnabled(false);
 			
 			checkMultiplers();
 		}
 	};
 	
 	View.OnClickListener prevTurnHandler = new View.OnClickListener() {
 		
 		@Override
 		public void onClick(View v) {
 			game.setCurrentRound(game.getCurrentRound() - 1);
 			nextTurnButton.setEnabled(true);
 			if(!(game.getCurrentRound() >= game.getMarks().size())){
 				round_mark.setText(game.getMarks().get(game.getCurrentRound()).toString());
 			}
 			if(game.getCurrentRound() == 0)
 				prevTurnButton.setEnabled(false);
 			
 			checkMultiplers();
 		}
 	};
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_hammer_game, menu);
 		return true;
 	}
 
 	
 	public void checkMultiplers(){
 		if(game.getCurrentRound() == 7){
 			dart2_text.setText("x3");
 			dart3_text.setText("x5");
 		}else{
 			dart2_text.setText("x2");
 			dart3_text.setText("x3");
 		}
 	}
 	
 	public void startGame(){
 		ArrayList<Integer> marks = game.getMarks();
 		for(int i = 0; i < marks.size(); i++){
 			for(int j = 0; j < players.size(); j++){
 				players.get(j);
 			}
 		}
 	}
 }
