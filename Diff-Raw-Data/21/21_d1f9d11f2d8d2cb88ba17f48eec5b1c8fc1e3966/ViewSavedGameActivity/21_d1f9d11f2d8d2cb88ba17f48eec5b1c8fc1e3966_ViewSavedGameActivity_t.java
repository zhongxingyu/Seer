 package ch.unibe.scg.team3.wordfinder;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.TextView;
 import ch.unibe.scg.team3.game.*;
 import ch.unibe.scg.team3.gameui.BoardUI;
 import ch.unibe.scg.team3.localDatabase.SavedGamesHandler;
 import ch.unibe.scg.team3.localDatabase.WordlistHandler;
 
 public class ViewSavedGameActivity extends Activity {
 
 	private SavedGame savedGame;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_view_saved_game);
 
 		SavedGamesHandler handler = new SavedGamesHandler(this);
 		Intent intent = getIntent();
 
 		long id = intent.getLongExtra("saved_game_id", -1);
 		savedGame = handler.getSavedGame(id);
 
 		WordlistHandler wh = new WordlistHandler(this);
 		BoardUI boardUI = (BoardUI) findViewById(R.id.tableboardUI);
 		boardUI.update(savedGame, new Event(Event.BOARD_UPDATED));
 
 		String labels = "Title: %s\nLast played: %s\nTimes played: %s\nFound words: %s\nAttempted words: %s\nElapsed time: %s\nScore: %s\nWordlist: %s";
 
 		String text = String.format(labels, savedGame.getName(), savedGame.getDate(),
 				savedGame.getTimesPlayed(), savedGame.getNumberOfFoundWords(),
 				savedGame.getNumberOfAttempts(), Timer.format(savedGame.getElapsedTime()),
 				savedGame.getScore(), wh.getWordlistNameById(savedGame.getWordlistId()));
 
 		TextView gameOverview = (TextView) findViewById(R.id.game_overview_text);
 		gameOverview.setText(text);
 	}
 
 	public void replaySavedGame(View view) {
 		Intent intent = getIntent();
 		intent.setClass(this, GameActivity.class);
 		intent.putExtra("saved_game_id", savedGame.getId());
 		startActivity(intent);
 		finish();
 	}
 	/**
 	 * BugFixing : Deactivates Button.
 	 */
 	@Override
 	public void onBackPressed() {
 	}
	public void onBack(View view){
		Intent intent = new Intent(getApplicationContext(), SavedGamesActivity.class);
		startActivity(intent);
		finish();
	}
 
 }
