 package com.example.zootypers.ui;
 
 import java.util.concurrent.TimeUnit;
 
 import android.annotation.SuppressLint;
 import android.app.ActionBar.LayoutParams;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.PopupWindow;
 
 import com.example.zootypers.R;
 import com.example.zootypers.core.SinglePlayerModel;
 import com.example.zootypers.util.States;
 import com.example.zootypers.util.States.difficulty;
 
 @SuppressLint("NewApi")
 /**
  *
  * UI / Activity and controller for single player game screen.
  * @author cdallas, littlpunk, kobyran
  */
 public class SinglePlayer extends Player {
 
 	// used for the communicating with model
 	private SinglePlayerModel model;
 
 	// the popup window
 	private PopupWindow ppw;
 
 	// the popup parameters
 	public LayoutParams popUpParams;
 
 	// the popup layout
 	public LinearLayout popUpLayout;
 
 	// the time in which the game was paused
 	private long pausedTime = START_TIME;
 
 	// the game timer that will give a time limit
 	protected GameTimer gameTimer;
 
 	// keeps track of if the game is paused or not
 	public static boolean paused = false;
 
 	/*
 	 *  Called when the activity is starting. uses the information that was picked
 	 *  in the pre selection screen and sets a background, animal, and difficulty
 	 *  also creates a single player model and does the initial display.
 	 *  
 	 *  @params savedInstanceState, the state of the intent before exiting
 	 *  this activity before
 	 */
 	@Override
 	protected void onCreate(final Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 
 		// Get animal & background selected by user
 		setContentView(R.layout.activity_pregame_selection);
 		Drawable animal = ((ImageButton) findViewById
 		(getIntent().getIntExtra("anm", 0))).getDrawable();
 		bg = getIntent().getIntExtra("bg", 0);
 		Drawable background = ((ImageButton) findViewById(bg)).getDrawable();
 
 		// Get difficulty
 		int diff = getIntent().getIntExtra("diff", 2);
 		difficulty d = States.difficulty.MEDIUM;
 		if (diff == 1) {
 			d = States.difficulty.EASY;
 		} else if (diff == 3) {
 			d = States.difficulty.HARD;
 		}
 
 		paused = false; 
 		
 		// start model
 		model = new SinglePlayerModel(d, this.getAssets(), NUM_WORDS);
 		model.addObserver(this);
 
 		// change screen view
 		setContentView(R.layout.activity_single_player);
 		initialDisplay(animal, background);
 
 		// create and start timer
 		gameTimer = new GameTimer(START_TIME, INTERVAL);
 		gameTimer.start();
 
 		Log.i("SinglePlayer", "game has begun");
 	}
 
 
 	/**
 	 * Initialize the contents of the Activity's standard options menu. 
 	 * 
 	 * @params menu, that uses added to the action bar if it is present.
 	 */
 	@Override
 	public final boolean onCreateOptionsMenu(final Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.single_player, menu);
 		return true;
 	}
 
 	/**
 	 * When the user types a letter, this listens for it.
 	 */
 	@Override
 	public final boolean onKeyDown(final int key, final KeyEvent event){
 		if ((key == KeyEvent.KEYCODE_BACK) && !paused) {
 			pauseGame(findViewById(R.id.pause_button));
 			return true;
 		}
 
 		char charTyped = event.getDisplayLabel();
 		charTyped = Character.toLowerCase(charTyped);
 		model.typedLetter(charTyped);
 		return true;
 	}
 
	/**
	 * This is called when the activity is on pause
	 */
 	@Override
 	public void onPause() {
 		super.onPause();
		if (!paused)
		pauseGame(findViewById(R.id.pause_button));
 	}
 
 	/**
 	 * Displays the initial screen of the single player game. This includes
 	 * the background, animal, as well as the initial words displayed on the screen.
 	 * 
 	 * @param animalID Drawable referring to the id of the selected animal image,
 	 * e.g. R.drawable.elephant_color.
 	 * @param backgroudID Drawable referring to the id of the selected background image.
 	 */
 	public void initialDisplay(Drawable animalID, Drawable backgroundID) {
 		// display animal
 		ImageView animalImage = (ImageView) findViewById(R.id.animal_image);
 		animalImage.setImageDrawable(animalID);
 
 		// display background
 		ViewGroup layout = (ViewGroup) findViewById(R.id.game_layout);
 		layout.setBackground(backgroundID);
 
 		model.populateDisplayedList();
 
 		displayTime(START_TIME / INTERVAL);
 
 		displayScore(0);
 	}
 
 	/**
 	 * Called when the timer runs out; goes to the post game screen.
 	 */
 	public void goToPostGame() {
 		Log.i("SinglePlayer", "Ending game");
 
 		paused = true;
 		Intent intent = new Intent(this, PostGameScreen.class);
 		// pass score
 		intent.putExtra("score", model.getScore());
 		intent.putExtra("bg", bg);
 		startActivity(intent);
 	}
 
 	/**
 	 * When the pause button is pressed, pauses the game and shows a pop-up window.
 	 * 
 	 * @param view The button clicked.
 	 */
 	public void pauseGame(View view) {
 		Log.i("SinglePlayer", "game is paused");
 
 		// save & stop time
 		pausedTime = currentTime;
 		gameTimer.cancel();
 
 		// disable buttons & keyboard
 		findViewById(R.id.keyboard_open_button).setEnabled(false);
 		findViewById(R.id.pause_button).setEnabled(false);
 
 		// create popup window
 		LayoutInflater layoutInflater =
 		(LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
 		View popupView = layoutInflater.inflate(R.layout.pause_popup, null);
 		ppw = new PopupWindow(popupView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 		ViewGroup parentLayout = (ViewGroup) findViewById(R.id.game_layout);
 		ppw.showAtLocation(parentLayout, Gravity.CENTER, 10, 20);
 		ppw.update(350, 500);
 
 		paused = true;
 	}
 
 	/**
 	 * When the user clicks the continue button while paused, continues the game.
 	 * 
 	 * @param view The button clicked.
 	 */
 	public void pausedContinue(View view){
 		Log.i("SinglePlayer", "continue is selected from pause");
 
 		// re-enable buttons & keyboard
 		findViewById(R.id.keyboard_open_button).setEnabled(true);
 		findViewById(R.id.pause_button).setEnabled(true);
 		keyboardButton(findViewById(R.id.keyboard_open_button));
 		gameTimer = new GameTimer(pausedTime, INTERVAL);
 		gameTimer.start();
 		ppw.dismiss();
 		paused = false;
 	}
 
 	/**
 	 * When the user clicks the new game button while paused, starts a new game.
 	 * 
 	 * @param view The button clicked.
 	 */
 	public void pausedNewGame(View view) {
 		Log.i("SinglePlayer", "new game is selected from pause");
 
 		final Intent restartIntent = new Intent(this, PreGameSelection.class);
 		paused = false;
 		startActivity(restartIntent);
 	}
 
 	/**
 	 * When the user clicks the main menu button while paused, goes to the title screen.
 	 * 
 	 * @param view The button clicked.
 	 */
 	public void pausedMainMenu(View view) {
 		Log.i("SinglePlayer", "main menu is selected from pause");
 
 		final Intent mainMenuIntent = new Intent(this, TitlePage.class);
 		paused = false;
 		startActivity(mainMenuIntent);
 	}
 
 	/**
 	 * Timer for game.
 	 */
 	public class GameTimer extends CountDownTimer {
 		/**
 		 * @param startTime Amount of time player starts with.
 		 * @param interval Amount of time between ticks.
 		 */
 		public GameTimer(final long startTime, final long interval) {
 			super(startTime, interval);
 		}
 
 		@Override
 		public final void onFinish() {
 			goToPostGame();
 		}
 
 		@Override
 		public final void onTick(final long millisUntilFinished) {
 			// edge case because 0 does not tick, in order to show 0 we have to -1000
 			currentTime = millisUntilFinished - 1000;
 			displayTime(TimeUnit.MILLISECONDS.toSeconds(currentTime));
 		}
 	}
 
 	@Override
 	public void error(com.example.zootypers.util.States.error err) {
 		// TODO Auto-generated method stub
 	}
 
 	/**
 	 * 
 	 * @return single player model
 	 */
 	public final SinglePlayerModel getModel() {
 		return model;
 	}
 }
