 package ch.unibe.scg.team3.wordfinder;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.View;
 import ch.unibe.scg.team3.game.*;
 import ch.unibe.scg.team3.gameui.*;
 import ch.unibe.scg.team3.localDatabase.WordlistHandler;
 
 /**
  * @author faerber
  * @author adrian
  * @author nils
  */
 
 @SuppressLint("NewApi")
 public class GameActivity extends Activity implements IGameObserver {
 
 	private Game game;
 	private WordlistHandler wordlistHandler;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_game);
 
 		wordlistHandler = new WordlistHandler(this);
 
 		int wordlistId = getSelectedWordlistId();
 
 		game = new Game(wordlistHandler, wordlistId);
 
 		BoardUI boardUI = (BoardUI) findViewById(R.id.tableboardUI);
 		FoundWordsView found = (FoundWordsView) findViewById(R.id.foundWordsField);
 		ScoreView scoreView = (ScoreView) findViewById(R.id.score_view);
 		WordCounterView wordCounter = (WordCounterView) findViewById(R.id.foundCounter);
 		CountDownView countDownView = (CountDownView) findViewById(R.id.timer_field);
 
 		boardUI.setOnTouchListener(new BoardOnTouchListener(this, game));
 
 		game.addObserver(boardUI);
 		game.addObserver(found);
 		game.addObserver(scoreView);
 		game.addObserver(wordCounter);
 		game.addObserver(countDownView);
 		game.addObserver(this);
 
 		game.notifyObservers();
 		
 		game.startTime();
 
 	}
 
 	private int getSelectedWordlistId() {
 		SharedPreferences preferences = PreferenceManager
 				.getDefaultSharedPreferences(this);
 
 		String selectedWordlist = preferences
 				.getString("choose_wordlist", null);
 
 		int id = Integer.parseInt(selectedWordlist);
 		return id;
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		try {
 			  PhoneCallListener PhoneCallingState = new PhoneCallListener();
 			  if(PhoneCallingState.isCalling()){
				 //Do anything
 			  }
 		} catch(Exception e) {
 			  //Log.e("callMonitor", "Exception: "+e.toString());
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 	}
 
 	public void quit(View view) {
 		//remainingTime = timer.getRemainingTime();
 		finishGameSession();
 	}
 
 	public void finishGameSession() {
 		game.stopTime();
 		
 		Intent intent = new Intent(this, EndGameActivity.class);
 
 		intent.putExtra("score", game.getScore());
 		intent.putExtra("words_found", game.getFoundWords().size());
 		
 		long remainingTime = game.getTimer().getRemainingTime();
 		long elapsed = Game.TIME_LIMIT - remainingTime;
 		
 		String time = Timer.format(elapsed);
 		
 		intent.putExtra("time", time);
 		intent.putExtra("guesses", game.getNumberOfGuesses());
 		intent.putExtra("board", game.getBoard().toString());
 
 		startActivity(intent);
 		finish();
 	}
 
 	@Override
 	public void update(AbstractGame game) {
 		if (game.isOver()) {
 			finishGameSession();
 		}
 	}
 
 }
