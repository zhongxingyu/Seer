 package com.slauson.dasher.game;
 
 import com.slauson.dasher.R;
 import com.slauson.dasher.menu.GameOverMenu;
 import com.slauson.dasher.menu.OptionsMenu;
 import com.slauson.dasher.other.GameBaseActivity;
 import com.slauson.dasher.status.Achievements;
 import com.slauson.dasher.status.LocalStatistics;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.LinearLayout;
 
 /**
  * Game activity
  * @author Josh Slauson
  *
  */
 public class GameActivity extends GameBaseActivity  {
 	
 	// bundle flags
 	public static final String BUNDLE_FLAG_GAME_MODE = "game_mode";
 	
 	/** Pause menu **/
 	private LinearLayout pauseMenu;
 
 	/** True if game is being quit out of **/
 	private boolean quitting;
 	/** True if game is paused **/
 	private boolean paused;
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		setContentView(R.layout.game_menu);
 		
 		// get game mode
 		int gameMode = Game.GAME_MODE_NORMAL;
 		Bundle extras = getIntent().getExtras();
 		if (extras != null) {
 			gameMode = extras.getInt(BUNDLE_FLAG_GAME_MODE);
 		}
 		
 		// setup game
 		Game.reset();
 		Game.gameMode = gameMode;
 		game = new Game(this);
     	
     	// setup game view
 		gameView = (GameView)findViewById(R.id.gameView);
 		gameView.setGame(game);
 
 		quitting = false;
 		paused = false;
 
 		// reset state
 		LocalStatistics.getInstance().reset();
 		Achievements.resetLocalAchievements();
 		
 		// setup pause menu
 		pauseMenu = (LinearLayout)findViewById(R.id.gamePauseMenu);
 		pauseMenu.setVisibility(View.GONE);
 		
 		// resume button
 		Button pauseMenuResumeButton = (Button)findViewById(R.id.gamePauseMenuResumeButton);
 		pauseMenuResumeButton.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				unpauseGame();
 			}
 		});
 		
 		// options button
 		Button pauseMenuOptionsButton = (Button)findViewById(R.id.gamePauseMenuOptionsButton);
 		pauseMenuOptionsButton.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				Intent intent = new Intent(GameActivity.this, OptionsMenu.class);
 				startActivity(intent);
 			}
 		});
 
 		// quit button
 		Button pauseMenuQuitButton = (Button)findViewById(R.id.gamePauseMenuQuitButton);
 		pauseMenuQuitButton.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				game.resetPlayer();
 				game.cleanup();
 				gameOver();
 			}
 		});
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		
 		quitting = false;
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		
 		if (!quitting) {
 			pauseGame();
 		}
 	}
 
 	@Override
 	public boolean onKeyUp(int keyCode, KeyEvent event) {
 		
 		switch(keyCode) {
 		// pause game when menu/back/search is pressed
 		case KeyEvent.KEYCODE_BACK:
 			if (paused) {
 				unpauseGame();
 				break;
 			}
 			// no break here so that we have the ability to pause game too
 		case KeyEvent.KEYCODE_MENU:
 		case KeyEvent.KEYCODE_SEARCH:
 			if (!paused) {
 				pauseGame();
 			}
 			break;
 		}
 
 		return super.onKeyUp(keyCode, event);
 	}
 	
 	@Override
 	public void init() {
 		// do nothing
 	}
 	
 	@Override
 	public void gameOver() {
 		// Transition to game over menu
 		quitting = true;
 		Intent intent = new Intent(GameActivity.this, GameOverMenu.class);
 		intent.putExtra(BUNDLE_FLAG_GAME_MODE, Game.gameMode);
 		startActivity(intent);
 	}
 
 	/**
 	 * Pauses game
 	 */
 	private void pauseGame() {
 		
 		if (game.togglePause(true)) {
 			pauseMenu.setVisibility(View.VISIBLE);
 			paused = true;
 		}
 	}
 
 	/**
 	 * Unpauses game
 	 */
 	private void unpauseGame() {
 		
 		if (game.togglePause(false)) {
 			pauseMenu.setVisibility(View.GONE);
 			paused = false;
 		}
 	}
 }
