 package ch.unibe.scg.team3.wordfinder;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 import ch.unibe.scg.team3.game.SavedGame;
 import ch.unibe.scg.team3.localDatabase.SavedGamesHandler;
 
 /**
  * @author lukas
  * @author nils
  * @author adrian
  */
 public class GameInterruptActivity extends Activity {
 
 	protected SavedGamesHandler handler;
 	private SavedGame game;
 	private SavedGame oldGame;
 	protected String board;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_end_game);
 
 		handler = new SavedGamesHandler(this);
 		Intent intent = getIntent();
 
 		long old_id = intent.getLongExtra("old_saved_game_id", -1);
 		long id = intent.getLongExtra("saved_game_id", -1);
 
 		if (!handler.isGameInDatabase(id)) {
 			makePauseInterface();
 
 		} else if (!handler.isGameInDatabase(old_id)) {
 			game = handler.getSavedGame(id);
 			oldGame = game;
 			makeEndGameInterface();
 		} else {
 			game = handler.getSavedGame(id);
 			oldGame = handler.getSavedGame(old_id);
 			makeCompareInterface();
 		}
 	}
 
 	private void makePauseInterface() {
 		TextView header = (TextView) findViewById(R.id.end_of_game_title);
 		Button resume = (Button) findViewById(R.id.resume_button);
 
 		header.setText("Pause");
 		resume.setVisibility(View.VISIBLE);
 	}
 
 	private void makeEndGameInterface() {
 
 		displayGameStats(game);
 		Button replay = (Button) findViewById(R.id.replay_button);
 		Button save = (Button) findViewById(R.id.save_button);
 
 		replay.setVisibility(View.VISIBLE);
 		save.setVisibility(View.VISIBLE);
 	}
 
 	private void makeCompareInterface() {
 
 //		displayComparedStats(game, oldGame);
 
 		Button replay = (Button) findViewById(R.id.replay_button);
 		Button update_save;
 
 		if (handler.isTaggedGame(oldGame)) {
 			update_save = (Button) findViewById(R.id.update_button);
 			displayComparedStats(game, oldGame);
 
 		} else {
 			update_save = (Button) findViewById(R.id.save_button);
 			displayGameStats(game);
 		}
 
 		update_save.setVisibility(View.VISIBLE);
 		replay.setVisibility(View.VISIBLE);
 
 	}
 
 	private void displayGameStats(SavedGame game) {
 		TextView stats = (TextView) findViewById(R.id.display_Stats);
 		stats.append(game.toString());
 	}
 
 	private void displayComparedStats(SavedGame game, SavedGame oldGame) {
 		displayGameStats(game);
 
 		TextView stats = (TextView) findViewById(R.id.display_Stats);
 		stats.append("\n\nYour old stats on this board:\n\n");
 
 		displayGameStats(oldGame);
 	}
 
 	public void newGame(View view) {
 		GameActivity.instance.finish();
 		Intent intent = new Intent(this, GameActivity.class);
 		startActivity(intent);
 		finish();
 	}
 
 	public void replayGame(View view) {
 		GameActivity.instance.finish();
 		Intent intent = new Intent(this, GameActivity.class);
 		intent.putExtra("saved_game_id", oldGame.getId());
 		startActivity(intent);
 		finish();
 	}
 
 	public void updateGame(View view) {
 		long tmp = game.getId();
 		game.setId(oldGame.getId());
 		handler.updateGame(game);
 		game.setId(tmp);
 		Toast msg = Toast.makeText(this, "Game updated.", Toast.LENGTH_SHORT);
 		msg.show();
 	}
 
 	public void resumeGame(View view) {
 		Intent intent = new Intent(this, GameActivity.class);
 		startActivity(intent);
 		finish();
 	}
 
 	public void goHome(View view) {
 		GameActivity.instance.finish();
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
 
 		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				
 				String value = input.getText().toString();
 				
 				if (handler.tagSavedGame(value, game.getId())) {
 					goHome(null);
 				} 
 			}
 		});
 
 		alert.setNegativeButton("Cancel", null);
 
 		alert.show();
 	}
 
 //	public void reenterTitle(final View view) {
 //		AlertDialog.Builder alert = new AlertDialog.Builder(this);
 //
 //		alert.setTitle("Game already in Database");
 //		alert.setMessage("Please choose another Title for your game.");
 //
 //		final EditText input = new EditText(this);
 //		alert.setView(input);
 //
 //		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 //			public void onClick(DialogInterface dialog, int whichButton) {
 //				String value = input.getText().toString();
 //				// game.setName(value);
 //				if (handler.tagSavedGame(value, game.getId())) {
 //					goHome(null);
 //				} else
 //					enterTitle(view);
 //			}
 //		});
 //
 //		alert.setNegativeButton("Cancel", null);
 //
 //		alert.show();
 //	}
 	
 	/**
 	 * BugFixing : Deactivates Button.
 	 */
 	@Override
 	public void onBackPressed() {
 	}
	
 }
