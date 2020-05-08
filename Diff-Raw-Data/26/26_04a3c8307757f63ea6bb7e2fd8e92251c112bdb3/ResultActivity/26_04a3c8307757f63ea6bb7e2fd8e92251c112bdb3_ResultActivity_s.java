 package org.yitznewton.rockpaperscissors;
 
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 
 import org.yitznewton.rockpaperscissors.gesture.Gesture;
 import org.yitznewton.rockpaperscissors.gesture.Paper;
 import org.yitznewton.rockpaperscissors.gesture.Rock;
 import org.yitznewton.rockpaperscissors.gesture.Scissors;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class ResultActivity extends Activity {
 	public static final String EXTRA_PLAYER_CHOICE = "player_choice";
 	
 	static final String STATE_PLAYER_SCORE = "player_score";
 	static final String STATE_COMPUTER_SCORE = "computer_score";
 	static final String STATE_HISTORY = "history";
 	
 	static final String PREFERENCE_PLAYER_SCORE = "player_score";
 	static final String PREFERENCE_COMPUTER_SCORE = "computer_score";
 	
 	private Gesture playerGesture;
 	private Gesture computerGesture;
 	private int playerScore = 0;
 	private int computerScore = 0;
 	private ArrayList<int[]> history;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		setContentView(R.layout.activity_result);
 		
 		if (savedInstanceState == null) {
 			loadHistory();
 			loadScore();
 			playerGesture = retrievePlayerGesture();
 			computerGesture = new ComputerChooser().get(history);
 			
 			int outcome = playerGesture.playAgainst(computerGesture);
 			
 			TextView winnerView = (TextView) findViewById(R.id.result_winner);
 
 			switch (outcome) {
 			case 1:
 				winnerView.setText(getString(R.string.you_win));
 				playerScore++;
 				saveScore();
 				break;
 			case -1:
 				winnerView.setText(getString(R.string.i_win));
 				computerScore++;
 				saveScore();
 				break;
 			case 0:
 				winnerView.setText(getString(R.string.draw));
 				break;
 			}
 			
 			history.add(new int[] {playerGesture.toInt(),
 				computerGesture.toInt()});
 			
			drawChoices();
 			drawScore();
 			saveHistory();
 		}
 	}
 	
 	public void playAgain(View v)
 	{
 		Intent i = new Intent(this, ChoiceActivity.class);
 		startActivity(i);
 	}
 	
 	public void resetScore(View v)
 	{
 		playerScore = 0;
 		computerScore = 0;
 		history = new ArrayList<int[]>();
 		
 		saveHistory();
 		saveScore();
 		drawScore();
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_result, menu);
 		return true;
 	}
 	
 	private Gesture retrievePlayerGesture()
 	{
 		return Gesture.fromButtonId(getIntent()
 				.getIntExtra(ResultActivity.EXTRA_PLAYER_CHOICE, -1));
 	}
 	
 	private void loadScore()
 	{
 		SharedPreferences preferences = getPreferences(MODE_PRIVATE);
 		playerScore = preferences.getInt(PREFERENCE_PLAYER_SCORE, 0);
 		computerScore = preferences.getInt(PREFERENCE_COMPUTER_SCORE, 0);
 	}
 	
 	private void saveScore()
 	{
 		SharedPreferences preferences = getPreferences(MODE_PRIVATE);
 		SharedPreferences.Editor editor = preferences.edit();
 		
 		editor.putInt(PREFERENCE_PLAYER_SCORE, playerScore);
 		editor.putInt(PREFERENCE_COMPUTER_SCORE, computerScore);
 		editor.commit();
 	}
 	
 	private void loadHistory()
 	{
 		try {
 			FileInputStream fis = openFileInput("history");
 			ObjectInputStream ois = new ObjectInputStream(fis);
 			history = (ArrayList<int[]>) ois.readObject();	
 			ois.close();
 		}
 		catch (IOException e) {
 			
 		}
 		catch (ClassNotFoundException e) {
 			
 		}
 		finally {
 			if (history == null) {
 				history = new ArrayList<int[]>();
 			}
 		}
 	}
 
 	private void saveHistory()
 	{
 		try {
 			FileOutputStream fos
 				= openFileOutput("history", Context.MODE_PRIVATE);
 			ObjectOutputStream oos = new ObjectOutputStream(fos);
 			oos.writeObject(history);
 			fos.getFD().sync();
 			oos.close();
 		}
 		catch (IOException e) {
 			
 		}
 	}
 
 	private void drawScore()
 	{
 		TextView playerScoreView = (TextView) findViewById(R.id.score_you);
 		playerScoreView.setText(Integer.toString(playerScore));
 		
 		TextView computerScoreView = (TextView) findViewById(R.id.score_me);
 		computerScoreView.setText(Integer.toString(computerScore));
 	}
 	
	private void drawChoices()
 	{
 		ImageView pv = (ImageView) findViewById(R.id.choice_you);
 		pv.setImageDrawable(resourceForGesture(playerGesture));
 		
 		ImageView cv = (ImageView) findViewById(R.id.choice_me);
 		cv.setImageDrawable(resourceForGesture(computerGesture));
 	}
 	
 	private Drawable resourceForGesture(Gesture g)
 	{
 		if (g instanceof Rock)
 			return getResources().getDrawable(R.drawable.rock);
 		if (g instanceof Paper)
 			return getResources().getDrawable(R.drawable.paper);
 		if (g instanceof Scissors)
 			return getResources().getDrawable(R.drawable.scissors);
 
 		throw new RuntimeException("Invalid gesture");
 	}
 }
