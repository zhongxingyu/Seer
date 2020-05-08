 package com.redvex.byteme;
 
 import java.util.ArrayList;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.MenuItem;
 import com.redvex.byteme.gamelogic.GameLogicFragment;
 import com.redvex.byteme.ui.GameLostDialogFragment;
 import com.redvex.byteme.ui.GameWonDialogFragment;
 import com.redvex.byteme.ui.InGameFragment;
 import com.redvex.byteme.ui.OutOfGameFragment;
 import com.redvex.byteme.ui.UIRow;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.view.View;
 import android.widget.Button;
 import android.widget.LinearLayout;
 
 /**
  * An activity representing just the Game field screen. This activity is only
  * used on handset devices. On tablet-size devices, the Game field is presented
  * side-by-side with the Game type selection in a {@link GameActivity}.
  * <p>
  * This activity is mostly just a 'shell' activity containing nothing more than
  * a {@link OutOfGameFragment/InGameFragment}.
  */
 public class HandsetDeviceGameActivity extends SherlockFragmentActivity implements
 		OutOfGameFragment.GameInit, InGameFragment.GameLogic, GameLogicFragment.UI,
 		GameLostDialogFragment.GameLostListener, GameWonDialogFragment.GameWonListener {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_game_field);
 
 		// Show the Up button in the action bar.
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 
 		// savedInstanceState is non-null when there is fragment state
 		// saved from previous configurations of this activity
 		// (e.g. when rotating the screen from portrait to landscape).
 		// In this case, the fragment will automatically be re-added
 		// to its container so we don't need to manually add it.
 		// For more information, see the Fragments API guide at:
 		//
 		// http://developer.android.com/guide/components/fragments.html
 		//
 		if (savedInstanceState == null) {
 			// Create the detail fragment and add it to the activity
 			// using a fragment transaction.
 			Bundle arguments = new Bundle();
 			arguments.putString(OutOfGameFragment.ARG_ITEM_ID,
 					getIntent().getStringExtra(OutOfGameFragment.ARG_ITEM_ID));
 			OutOfGameFragment fragment = new OutOfGameFragment();
 			fragment.setArguments(arguments);
 			getSupportFragmentManager().beginTransaction().add(R.id.game_field_container, fragment)
 					.commit();
 		}
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		// Removes the pause/resume Button if the activity is not visible.
 		removePauseButtonFromActionBar();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		// The pause and resume button has to be added to the top action bar
 		// again.
 		LinearLayout gameField = (LinearLayout) findViewById(R.id.game_field);
 		if (gameField != null) {
 			// The pause and resume button is just added if the game field can
 			// be found => an InGameFragment is currently displayed.
 			// Display the button with "Resume".
 			addPauseButtonToActionBar(true);
 		}
 	}
 
 	/**
 	 * Adds the pause and resume button to the top action bar.
 	 * 
 	 * @param paused
 	 *            If the game is paused the value should be true. If the game is
 	 *            started and running afterwards the value should be false
 	 */
 	private void addPauseButtonToActionBar(boolean paused) {
 		// On handset devices a custom layout is added to the actionbar to pause
 		// and resume the game.
 		// The rest of the actionbar layout is inflated to the bottom actionbar
 		// in the fragment itself.
 		ActionBar actionBar = getSupportActionBar();
 		actionBar.setCustomView(R.layout.actionbar_pause_resume);
 
 		// Getting the pause and resume Button from the actionbar.
 		Button pauseResume = (Button) actionBar.getCustomView().findViewById(
 				R.id.actionbar_pause_resume);
 
 		// Setting up an OnClickListener for the pause and resume Button.
 		pauseResume.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 				// Getting the pause and resume Button from the actionbar.
 				Button pauseResume = (Button) getSupportActionBar().getCustomView().findViewById(
 						R.id.actionbar_pause_resume);
 				// Getting the InGameFragment.
 				InGameFragment fragment = (InGameFragment) getSupportFragmentManager()
 						.findFragmentById(R.id.game_field_container);
 
 				if (pauseResume.getText().toString().equals(getString(R.string.actionbar_pause))) {
 					// Pause game.
 					pauseResume.setText(getString(R.string.actionbar_resume));
 					pauseGameLogic();
					findViewById(R.id.game_field).setVisibility(View.INVISIBLE);
 					fragment.setKeyboardsInvisible();
 				} else {
 					// Resume game.
 					pauseResume.setText(getString(R.string.actionbar_pause));
 					startGameLogic(fragment.getArguments().getString(InGameFragment.GAME_TYPE));
 					findViewById(R.id.game_field).setVisibility(View.VISIBLE);
 				}
 			}
 		});
 
 		if (paused) {
 			// If the game is paused the "Resume" text is shown.
 			pauseResume.setText(getString(R.string.actionbar_resume));
 		} else {
 			// Otherwise the "Pause" text.
 			pauseResume.setText(getString(R.string.actionbar_pause));
 		}
 
 		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE
 				| ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_HOME_AS_UP);
 	}
 	
 	/**
 	 * Removes the pause and resume button from the top action bar.
 	 */
 	private void removePauseButtonFromActionBar() {
 		ActionBar actionBar = getSupportActionBar();
 		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE
 				| ActionBar.DISPLAY_HOME_AS_UP);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpTo(this, new Intent(this, GameActivity.class));
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	/**
 	 * Callback method from {@link OutOfGameFragment.GameInit} which is used to
 	 * check if the game of the running GameLogic is won or lost.
 	 * 
 	 * @return true if the running GameLogic is set to game won or lost.
 	 *         Otherwise, also if no GameLogicFragment exists, it's set to
 	 *         false.
 	 */
 	public boolean isGameLostOrWon() {
 		// Getting the GameLogicFragment if one exists.
 		GameLogicFragment gameLogic = (GameLogicFragment) getSupportFragmentManager()
 				.findFragmentByTag("gameLogic");
 		boolean gameWonOrLost = false;
 
 		if (gameLogic != null) {
 			if (gameLogic.isGameWon() || gameLogic.isGameLost()) {
 				gameWonOrLost = true;
 			}
 		}
 
 		return gameWonOrLost;
 	}
 	
 	/**
 	 * Callback method from {@link OutOfGameFragment.GameInit} which is used to
 	 * get the game type of the running GameLogic.
 	 */
 	public String getGameLogicGameType() {
 		// Getting the GameLogicFragment if one exists.
 		GameLogicFragment gameLogic = (GameLogicFragment) getSupportFragmentManager().findFragmentByTag("gameLogic");
 		String gameLogicGameType = "null";
 
 		if (gameLogic != null) {
 			gameLogicGameType = gameLogic.getArguments().getString(GameLogicFragment.GAME_TYPE);
 		}
 
 		return gameLogicGameType;
 	}
 
 	/**
 	 * Callback method from {@link OutOfGameFragment.GameInit} which is used to
 	 * start the InGameDetailFragment and initiates the top actionbar.
 	 */
 	public void startGame(String gameType) {
 		Bundle arguments = new Bundle();
 		arguments.putString(InGameFragment.GAME_TYPE, gameType);
 		InGameFragment fragment = new InGameFragment();
 		fragment.setArguments(arguments);
 		getSupportFragmentManager().beginTransaction().replace(R.id.game_field_container, fragment)
 				.addToBackStack(null).commit();
 
 		addPauseButtonToActionBar(false);
 	}
 
 	/**
 	 * Callback method from {@link GameLogicFragment.UI} which is used to add a
 	 * new row to the User Interface.
 	 */
 	@Override
 	public void addUIRow(UIRow row) {
 		InGameFragment fragment = (InGameFragment) getSupportFragmentManager().findFragmentById(
 				R.id.game_field_container);
 		fragment.addUIRow(row);
 	}
 
 	/**
 	 * Callback method from {@link GameLogicFragment.UI} which is used to remove
 	 * a single row at a specific position of the User Interface.
 	 */
 	@Override
 	public void removeUIRow(int index) {
 		InGameFragment fragment = (InGameFragment) getSupportFragmentManager().findFragmentById(
 				R.id.game_field_container);
 		fragment.removeUIRow(index);
 	}
 
 	/**
 	 * Callback method from {@link GameLogicFragment.UI} which is used to remove
 	 * all rows from the User Interface.
 	 */
 	@Override
 	public void clearUIBoard() {
 		InGameFragment fragment = (InGameFragment) getSupportFragmentManager().findFragmentById(
 				R.id.game_field_container);
 		fragment.clearUIBoard();
 	}
 
 	/**
 	 * Callback method from {@link GameLogicFragment.UI} which is used to
 	 * display the current level.
 	 */
 	@Override
 	public void displayLevel(int level) {
 		InGameFragment fragment = (InGameFragment) getSupportFragmentManager().findFragmentById(
 				R.id.game_field_container);
 		fragment.updateActionbarLevel(level);
 	}
 
 	/**
 	 * Callback method from {@link GameLogicFragment.UI} which is used to
 	 * display the current score.
 	 */
 	@Override
 	public void displayScore(int score) {
 		InGameFragment fragment = (InGameFragment) getSupportFragmentManager().findFragmentById(
 				R.id.game_field_container);
 		fragment.updateActionbarScore(score);
 	}
 
 	/**
 	 * Callback method from {@link GameLogicFragment.UI} which is used to
 	 * display the lines which still has to be solved at the current level.
 	 */
 	@Override
 	public void displayLinesLeft(int linesLeft) {
 		InGameFragment fragment = (InGameFragment) getSupportFragmentManager().findFragmentById(
 				R.id.game_field_container);
 		fragment.updateActionbarLinesLeft(linesLeft);
 	}
 
 	/**
 	 * Callback method from {@link GameLogicFragment.UI} which is used to
 	 * display the win screen. On handset devices the win screen is displayed in
 	 * full screen.
 	 */
 	@Override
 	public void displayWinScreen(int totalScore, int rowScore, ArrayList<Integer> levelScore) {
 		GameWonDialogFragment winScreen = GameWonDialogFragment.newInstance(totalScore, rowScore,
 				levelScore);
 		getSupportFragmentManager().beginTransaction()
 				.replace(R.id.game_field_container, winScreen).commit();
 
 		removePauseButtonFromActionBar();
 	}
 
 	/**
 	 * Callback method from {@link GameLogicFragment.UI} which is used to
 	 * display the lost screen. On handset devices the lost screen is displayed
 	 * in full screen.
 	 */
 	@Override
 	public void displayLostScreen(int level, int totalScore, int rowScore,
 			ArrayList<Integer> levelScore) {
 		GameLostDialogFragment lostScreen = GameLostDialogFragment.newInstance(level, totalScore,
 				rowScore, levelScore);
 		getSupportFragmentManager().beginTransaction()
 				.replace(R.id.game_field_container, lostScreen).commit();
 
 		removePauseButtonFromActionBar();
 	}
 
 	/**
 	 * Callback method from {@link GameLostDialogFragment.GameLostListener}
 	 * which is used to display the lost screen.
 	 */
 	@Override
 	public void onGameLostDialogContinueClick() {
 		this.finish();
 	}
 
 	/**
 	 * Callback method from {@link GameWonDialogFragment.GameWonListener} which
 	 * is used to display the win screen.
 	 */
 	@Override
 	public void onGameWonDialogContinueClick() {
 		this.finish();
 	}
 
 	/**
 	 * Callback method from {@link InGameFragment.GameLogic} which is used to
 	 * pass the new values row to the GameLogicFragment after the user
 	 * interacted with this row.
 	 */
 	@Override
 	public void updateRow(int index, UIRow row) {
 		GameLogicFragment fragment = (GameLogicFragment) getSupportFragmentManager()
 				.findFragmentByTag("gameLogic");
 		fragment.updateRow(index, row);
 	}
 
 	/**
 	 * Callback method from {@link InGameFragment.GameLogic} which is used to
 	 * start or resume the GameLogicFragment.
 	 */
 	@Override
 	public void startGameLogic(String gameType) {
 		GameLogicFragment gameLogic = (GameLogicFragment) getSupportFragmentManager()
 				.findFragmentByTag("gameLogic");
 		String gameLogicGameType = "null";
 
 		if (gameLogic != null) {
 			gameLogicGameType = gameLogic.getArguments().getString(GameLogicFragment.GAME_TYPE);
 		}
 
 		// If there hasn't been a game started yet, or the paused game type
 		// isn't equal to the game type which is going to be started or the last
 		// game is not resumed a new game gets started.
 		if (gameLogic == null || !gameLogicGameType.equals(gameType)) {
 			Bundle arguments = new Bundle();
 			arguments.putString(GameLogicFragment.GAME_TYPE, gameType);
 			gameLogic = new GameLogicFragment();
 			gameLogic.setArguments(arguments);
 			getSupportFragmentManager().beginTransaction().add(gameLogic, "gameLogic").commit();
 		} else {
 			gameLogic.resumeGame();
 		}
 	}
 
 	/**
 	 * Callback method from {@link InGameFragment.GameLogic} which is used to
 	 * pause the GameLogicFragment.
 	 */
 	@Override
 	public void pauseGameLogic() {
 		GameLogicFragment fragment = (GameLogicFragment) getSupportFragmentManager()
 				.findFragmentByTag("gameLogic");
 		fragment.pauseGame();
 	}
 }
