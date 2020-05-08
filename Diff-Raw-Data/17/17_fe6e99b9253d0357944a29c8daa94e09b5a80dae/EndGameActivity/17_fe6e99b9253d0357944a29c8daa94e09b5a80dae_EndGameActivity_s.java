 package ch.unibe.scg.team3.wordfinder;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 import ch.unibe.scg.team3.game.SavedGame;
 import ch.unibe.scg.team3.localDatabase.SavedGamesHandler;
 
 /**
  * @author lukas
  * @author nils
  */
 
 public class EndGameActivity extends Activity {
 
 	protected SavedGamesHandler handler;
 	private SavedGame game;
 	protected String board;
 	protected int score;
 	protected String time;
 	protected int guesses;
 	protected int found;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_end_game);
 
 		handler = new SavedGamesHandler(this.getApplicationContext());
 
 		Intent intent = getIntent();
 		game = (SavedGame) intent.getSerializableExtra("saved_game");
 		handler.saveGame(game);
 		String labels = "Your Score: %s\nFound Words: %s\nAttempts Words: %s\nElapsed Time: %s\n";
 
 		String text = String.format(labels, game.getScore(), game.getNumberOfFoundWords(),
 				game.getNumberOfAttempts(), game.getTime());
 
 		TextView stats = (TextView) findViewById(R.id.display_Stats);
 		stats.setText(text);
 	}
 
 	public void newGame(View view) {
 		GameActivity.activity.finish();
 		Intent intent = new Intent(this, GameActivity.class);
 		startActivity(intent);
 		finish();
 	}
 
 	public void replayGame(View view) {
 		GameActivity.activity.finish();
 		Intent intent = new Intent(this, GameActivity.class);
 		intent.putExtra("saved_game", game);
 		startActivity(intent);
 		finish();
 	}
 	public void resumeGame(View view){
 		
		Intent intent = new Intent(this, GameActivity.class);
    	startActivity(intent);
 
 		finish();
 	}
 
 	public void goHome(View view) {
 		GameActivity.activity.finish();
 		Intent intent = new Intent(this, HomeActivity.class);
 		startActivity(intent);
 		finish();
 	}
 
 	public void enterTitle(final View view) {
 		AlertDialog.Builder alert = new AlertDialog.Builder(this);
 
 		alert.setTitle("Save Game");
 		alert.setMessage("Please enter a title for your game.");
 
 		final EditText input = new EditText(this);
 		alert.setView(input);
 
 		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				String value = input.getText().toString();
 				game.setName(value);
 				if (handler.saveGame(game)) {
 					goHome(null);
 				} else
 					reenterTitle(view);
 			}
 		});
 
 		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				// Canceled.
 			}
 		});
 
 		alert.show();
 	}
 
 	public void reenterTitle(final View view) {
 		AlertDialog.Builder alert = new AlertDialog.Builder(this);
 
 		alert.setTitle("Game already in Database");
 		alert.setMessage("Please choose another Title for your game.");
 
 		final EditText input = new EditText(this);
 		alert.setView(input);
 
 		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				String value = input.getText().toString();
 				game.setName(value);
 				if (handler.saveGame(game)) {
 					goHome(null);
 				} else
 					enterTitle(view);
 			}
 		});
 
 		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				// Canceled.
 			}
 		});
 
 		alert.show();
 	}
	@Override
    public void onBackPressed() {
		super.onBackPressed();
		Intent intent = new Intent(this, GameActivity.class);
    	startActivity(intent);   
    }
 
 }
