 package com.example.gameapp;
 
 //Names: Joel Murphy & Chris Bentley
 //Purpose: Game activity: Player has to click on the right button before it disappears
 //If the player clicks on the wrong button they lose points. If they click anywhere
 //else or don't click the right button before it disappears the game finishes.
 //they type their initials and their score gets inserted into the database.
 
 import java.util.Random;
 
 import com.example.gameapp.SQLiteAdapter;
 
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.view.Menu;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class GameActivity extends Activity {
 	
 	//Declare Constants
 	private final long roundTime = 3000;	//the time in miliseconds that the Round display is shown
 	private final long START_TIME = 2000;	//the first time in miliseconds that the Buttons are displayed for
 	private final long MIN_RANDOM_TIME = 500;	//The minimum amount of time inbetween the round display and the buttons showing up
 	private final long countBy = 10;	//what the timer counts down by
 	private final int E_BUTTON_INCREMENT = 75;	//the easy timer and score increments
 	private final int N_BUTTON_INCREMENT = 100; //the medium one
 	private final int H_BUTTON_INCREMENT = 150; //the hard one
 	private final int MIN_BUTTON = 100;	//the minimum amount of time the button can be up for
 	private final int TOUCH_GOOD_VISIBLE = 1;	//value given when the right button is touched and the value for visible objects
 	private final int TOUCH_BAD = 5;	//value given when the bad button is touched
 	private final int TOUCH_LOSE_INVISIBLE = -1;	//value given when the right button isn't touched and the value for invisible objects
 	private final int TOUCH_RESET = 0;	//value given to reset the win condition
 	private final int NUM_BUTTONS = 9;	//the number of buttons
 	private final int BAD_MULTIPLIER = 2;	//the value multiplied with the increment and subtracted from the score
 	
 	//Declare variables
 	private MediaPlayer gameMusic; //the game Music
 	private EditText input;	//the edit text for entering your initials
 	private Context context = this;
 	private long startTime;	//the the amount of time the buttons are on the screen for
 	private long randomTime;	//the amount of time the player needs to wait after the round display before the buttons show up
 	private int difficultyIncrement;	//the decrement time value and the increment score value determined by difficulty
 	private ImageButton[] button = new ImageButton[9];	//the array for the buttons
 	private int randButton;	//the random value for the right button
 	private int randBadButton;	//the random value for the wrong button
 	private int score;	//the value of the score
 	private int roundNum;	//the value of the round number
 	private TextView timerTextView;	//the timer textview
 	private TextView scoreTextView;	//the score textview
 	private TextView roundTextView;	//the round textview
 	private TextView winLoseTextView;	//the winlose textview
 	private LinearLayout gameLayout;	//the layout of the activity
 	private MyCountDownTimer roundTimer;	//the timer for how long the round is displayed, as well as the time between button and round display
 	private MyCountDownTimer randomTimer;	//the timer for how long is inbetween the round and the button
 	private MyCountDownTimer buttonTimer;	//the timer for how long the buttons are displayed
 	private boolean roundDisplay;	//flags whether the round is displayed or not
 	private boolean inbetween;	//flags whether the space between round and button is happening
 	private boolean waiting;	//flags whether the space between button and round is happening
 	private boolean buttonHere;	//flags whether the button is displayed
 	private int newWin;	//flags for different win or lose conditions
 	private String difficulty;	//gets assigned the difficulty intent
 	private boolean musicState;	//gets assigned the musicstate intent
 	private SQLiteAdapter mySQLiteAdapter;	//sqlite adapter for the scores database
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.game_activity);
 		
 		//Get the values sent by intent
 		Bundle extras = getIntent().getExtras();
 		if (extras != null) {
 			difficulty = extras.getString("difficulty");
 			musicState = extras.getBoolean("music");
 		}
 		//assign the correct increment for the difficulty chosen
 		if (difficulty.equals(getResources().getString(R.string.d_easy))) {
 			difficultyIncrement = E_BUTTON_INCREMENT;
 		} else if (difficulty.equals(getResources().getString(R.string.d_hard))) {
 			difficultyIncrement = H_BUTTON_INCREMENT;
 		} else {
 			difficultyIncrement = N_BUTTON_INCREMENT;
 		}
 		startTime = START_TIME;
 		newWin = TOUCH_RESET;
 		score = TOUCH_RESET;
 		//set the round number to 1
 		roundNum = TOUCH_GOOD_VISIBLE;
 		waiting = false;
 		buttonHere = false;
 		roundDisplay = true;
 		inbetween = false;
 		timerTextView = (TextView) findViewById(R.id.timerTextView);
 		scoreTextView = (TextView) findViewById(R.id.scoreTextView);
 		roundTextView = (TextView) findViewById(R.id.roundTextView);
 		winLoseTextView = (TextView) findViewById(R.id.winLoseTextView);
 		//initialize the round and button timers
 		roundTimer = new MyCountDownTimer(roundTime, countBy);
 		buttonTimer = new MyCountDownTimer(startTime, countBy);
 		gameLayout = (LinearLayout) findViewById(R.id.gameLayout);
 		gameLayout.setOnTouchListener(buttonTouchListener);
 
 		button[0] = (ImageButton) findViewById(R.id.button0);
 		button[1] = (ImageButton) findViewById(R.id.button1);
 		button[2] = (ImageButton) findViewById(R.id.button2);
 		button[3] = (ImageButton) findViewById(R.id.button3);
 		button[4] = (ImageButton) findViewById(R.id.button4);
 		button[5] = (ImageButton) findViewById(R.id.button5);
 		button[6] = (ImageButton) findViewById(R.id.button6);
 		button[7] = (ImageButton) findViewById(R.id.button7);
 		button[8] = (ImageButton) findViewById(R.id.button8);
 
 		for (int i = 0; i < NUM_BUTTONS; i++) {
 			button[i].setOnTouchListener(buttonTouchListener);
 			button[i].setVisibility(TOUCH_LOSE_INVISIBLE);
 		}
 		//start the round timer
 		roundTimer.start();
 
 	}
 	
 	//create the on touch listener so the player can win when they tap the button
 	OnTouchListener buttonTouchListener = new OnTouchListener() {
 
 		@Override
 		public boolean onTouch(View v, MotionEvent event) {
 			int action = event.getAction();
 			if (action == MotionEvent.ACTION_DOWN) {
 				if ((!roundDisplay) && (!waiting)) {
 					if (v.getId() == button[randButton].getId()) {
 						if (newWin != TOUCH_LOSE_INVISIBLE) {
 				//if the player touches the screen, it's neither the round display time or the waiting before it,
 							//the player touched the right button and they haven't previously touched
 							//the rest of the layout then assign the win variable
 							newWin = TOUCH_GOOD_VISIBLE;
 							winLoseTextView.setText(getResources().getString(
 									R.string.win));
 						}
 					} else if (v.getId() == button[randBadButton].getId()) {
 						//if the player touched the bad button then decrease the score
 						if (newWin == TOUCH_RESET) {
 							score -= BAD_MULTIPLIER * difficultyIncrement;
 							newWin = TOUCH_BAD;
 						}
 
 					} else {
 						//if the player touches the layout then assign a lose
 						//as long as they didn't already touch the right button
 						if (newWin == TOUCH_RESET) {
 							newWin = TOUCH_LOSE_INVISIBLE;
 							buttonsNotVisible();
 						}
 					}
 				}
 			} else if (action == MotionEvent.ACTION_UP) {
 				//if the player has touched the right button, make the buttons
 				//disappear when the remove their finger from the screen
 				if (newWin == TOUCH_GOOD_VISIBLE) {
 					buttonsNotVisible();
 				}
 			}
 			return false;
 		}
 	};
 
 	//creates the timer class
 	public class MyCountDownTimer extends CountDownTimer {
 
 		public MyCountDownTimer(long startTime, long countBy) {
 			super(startTime, countBy);
 		}
 
 		@Override
 		public void onFinish() {
 			//flags are for the onFinish
 			//when the timer finishes...
 			if (newWin != TOUCH_LOSE_INVISIBLE) {
 				//and the player hasn't received a lose statement
 				if (roundDisplay) {
 					//and it was the round display
 					//set up the placements of the right and wrong buttons
 					randButton = randomButton();
 					randBadButton = randomButton();
 					do {
 						randBadButton = randomButton();
 					} while (randBadButton == randButton);
 					//set up the next timer
 					randomTime = MIN_RANDOM_TIME + randomTime();
 					randomTimer = null;
 					randomTimer = new MyCountDownTimer(randomTime, countBy);
 					//make the round textview invisible
 					roundTextView.setVisibility(TOUCH_LOSE_INVISIBLE);
 					//remove the round display flag
 					roundDisplay = false;
 					//start the random timer
 					randomTimer.start();
 					//flag the inbetween part
 					inbetween = true;
 				} else if (waiting) {
 					//and it was the waiting period between the buttons and round parts
 					//set the button timer start time
 					if (startTime - difficultyIncrement >= MIN_BUTTON) {
 						startTime -= difficultyIncrement;
 					} else {
 						startTime = MIN_BUTTON;
 					}
 					//reset the button timer
 					buttonTimer = null;
 					buttonTimer = new MyCountDownTimer(startTime, countBy);
 					//make the buttons invisible incase they weren't
 					buttonsNotVisible();
 					//make the win lose textview invisible
 					winLoseTextView.setVisibility(TOUCH_LOSE_INVISIBLE);
 					//set the round number and make it visible
 					roundTextView.setText(getResources().getString(
 							R.string.round)
 							+ " " + String.valueOf(roundNum));
 					roundTextView.setVisibility(TOUCH_GOOD_VISIBLE);
 					//remove the waiting flag
 					waiting = false;
 					//start the round display timer
 					roundTimer.start();
 					//flag the round display part
 					roundDisplay = true;
 
 				} else if (inbetween) {
 					//and it was the inbetween period between the round and button parts
 					//remove the inbetween flag
 					inbetween = false;
 					//start the button timer
 					buttonTimer.start();
 					//flag the button part
 					buttonHere = true;
 					//set the previously picked buttons to their respective button images
 					button[randButton].setImageDrawable(getResources()
 							.getDrawable(R.drawable.image_button));
 					button[randBadButton].setImageDrawable(getResources()
 							.getDrawable(R.drawable.bad_button_selector));
 					//make the buttons visible
 					buttonsVisible();
 				} else if (buttonHere) {
 					//and it was the button part
 					//if the player has the win statement
 					if (newWin == TOUCH_GOOD_VISIBLE) {
 						//increase the score and continue game for win condition
 						score += difficultyIncrement;
 						endOfButtonTimer();
 					} else {
 						//if the player did not get the win statement
 						//then set the lose condition
 						winLoseTextView.setText(getResources().getString(
 								R.string.timesup));
 						loseCondition();
 					}
 				}
 			} else {
 				//and the player has received the lose statement
 				//activate the lose condition
 				winLoseTextView
 						.setText(getResources().getString(R.string.lose));
 				loseCondition();
 			}
 		}
 
 		// tick the timer down
 		@Override
 		public void onTick(long millisUntilFinished) {
 			timerTextView.setText(String.valueOf(millisUntilFinished));
 			//only show the timer when the buttons are displayed
 			if (buttonHere) {
 				timerTextView.setVisibility(TOUCH_GOOD_VISIBLE);
 			} else {
 				timerTextView.setVisibility(TOUCH_LOSE_INVISIBLE);
 			}
 		}
 	}
 	
 	//the win condition resets win statement, updates score, and begins the waiting part
 	public void endOfButtonTimer() {
 		winLoseTextView.setVisibility(TOUCH_GOOD_VISIBLE);
 		if (score < TOUCH_RESET) {
 			score = TOUCH_RESET;
 		}
 		scoreTextView.setText(String.valueOf(score));
 		buttonHere = false;
 		roundNum += TOUCH_GOOD_VISIBLE;
 		newWin = TOUCH_RESET;
 		roundTimer.start();
 		waiting = true;
 	}
 	
 	//generate the random time between round and button
 	public int randomTime() {
 		int rTime;
 		Random random = new Random();
 		rTime = random.nextInt(2000);
 		return rTime;
 	}
 	
 	//generate the random position value for the buttons
 	public int randomButton() {
 		int rButton;
 		Random random = new Random();
 		rButton = random.nextInt(NUM_BUTTONS);
 		return rButton;
 	}
 	
 	//make the buttons invisible
 	public void buttonsNotVisible() {
 		button[randBadButton].setVisibility(TOUCH_LOSE_INVISIBLE);
 		button[randButton].setVisibility(TOUCH_LOSE_INVISIBLE);
 	}
 	
 	//make the buttons visible
 	public void buttonsVisible() {
 		button[randBadButton].setVisibility(TOUCH_GOOD_VISIBLE);
 		button[randButton].setVisibility(TOUCH_GOOD_VISIBLE);
 	}
 	
 	//lose condition
 	@SuppressLint("DefaultLocale") public void loseCondition() {
 		buttonsNotVisible();
 		input = new EditText(this);
 		button[randButton].setEnabled(false);
 		button[randBadButton].setEnabled(false);
 		winLoseTextView.setVisibility(TOUCH_GOOD_VISIBLE);
 		roundTextView.setText(getResources().getString(R.string.finished));
 		roundTextView.setVisibility(TOUCH_GOOD_VISIBLE);
 		AlertDialog.Builder builder = new AlertDialog.Builder(context);
 		input.setSingleLine(true);
 		input.setHint(getResources().getString(R.string.default_initials));
 		if (score < TOUCH_RESET) {
 			score = TOUCH_RESET;
 		}
 		//create an alertdialog to insert the score and initials into the databse
 		builder.setTitle(getResources().getString(R.string.lose));
 		builder.setMessage(getResources().getString(R.string.score) + " "
 				+ String.valueOf(score));
 		builder.setView(input);
 		builder.setCancelable(false);
 		builder.setPositiveButton("Back to Menu",
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						String enteredInitials;
 						//if the no initials are entered enter AAA
 						if (input.getText().length() != 0) {
 							if (input.getText().length() > 3) {
 								enteredInitials = input.getText().toString()
 										.substring(0, 3);
 							} else {
 								enteredInitials = input.getText().toString().toUpperCase();
 							}
 						} else {
 							enteredInitials = getResources().getString(R.string.default_initials);
 						}
 
 						mySQLiteAdapter = new SQLiteAdapter(context);
 						mySQLiteAdapter.openToWrite();
 						mySQLiteAdapter.scoreInsert(enteredInitials, score);
 						mySQLiteAdapter.close();
 						finish();
 
 					}
 				});
 		//show the dialog
 		AlertDialog finishedDialog = builder.create();
 		finishedDialog.show();
 
 	}
 
 	@Override
 	public void finish() {
 		Intent passData = new Intent(GameActivity.this, Menu.class);
 		passData.putExtra("difficulty", difficulty);
 		passData.putExtra("music", musicState);
 		setResult(RESULT_OK, passData);
 		super.finish();
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 		startMedia();
 	}
 	
 	
 	@Override
 	protected void onStop() {
 		gameMusic.release();
 		gameMusic = null;
 		super.onStop();
 	}
 	
 	//stuff for music
 	public void openMedia() {
 		gameMusic = MediaPlayer.create(getApplicationContext(),
 				R.raw.game_music);
 		gameMusic.start();
 		gameMusic.setLooping(true);
 	}
 
 	public void closeMedia() {
 		gameMusic.release();
 		gameMusic = null;
 	}
 
 	public void startMedia() {
 		if (musicState) {
 			openMedia();
 		}
 	}
 }
