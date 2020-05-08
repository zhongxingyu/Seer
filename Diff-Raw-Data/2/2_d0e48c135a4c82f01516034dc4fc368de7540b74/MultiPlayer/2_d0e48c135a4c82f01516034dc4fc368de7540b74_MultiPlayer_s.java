 package com.example.zootypers;
 
 import java.util.Observable;
 import java.util.Observer;
 import java.util.concurrent.TimeUnit;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.text.Html;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.parse.Parse;
 
 
 /**
  * Activity / UI for MultiPlayer screen.
  * @author cdallas
  *
  */
 @SuppressLint("NewApi")
 public class MultiPlayer extends Activity implements Observer {
 	private MultiPlayerModel model;
 	protected final int NUM_WORDS = 5;  
 	protected int bg;
    
 	// for the game timer
 	protected GameTimer gameTimer;
 	protected final long INTERVAL = 1000; // 1 second
	public final static long START_TIME = 20000; // 1 minute
 	public static boolean paused = false;
 	private long currentTime;
 	
 
   /**
    * @param id The id of the View to get as a String.
    * @return The View object with that id
    */
   private final View getByStringId(final String id) {
     return findViewById(getResources().getIdentifier(id, "id", getPackageName()));
   }
   
   /**
    * @param id The id of an animal ImageButton on the pregame screen.
    * @return The resource if of the drawable image facing the opposite way
    * (i.e. the opponent's version of the animal).
    */
   private int reverseDrawable(int id) {
     if (id == R.id.giraffe_button) {
       return R.drawable.animal_giraffe_opp;
     } else if (id == R.id.kangaroo_button) {
       return R.drawable.animal_kangaroo_opp;
     } else if (id == R.id.lion_button) {
       return R.drawable.animal_lion_opp;
     } else if (id == R.id.monkey_button) {
       return R.drawable.animal_monkey_opp;
     } else if (id == R.id.panda_button) {
       return R.drawable.animal_panda_opp;
     } else if (id == R.id.penguin_button) {
       return R.drawable.animal_penguin_opp;
     } else if (id == R.id.turtle_button) {
       return R.drawable.animal_turtle_opp;
     } else {
       return R.drawable.animal_elephant_opp;
     }
   }
 
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 	  super.onCreate(savedInstanceState);
 
 		// Get animal & background selected by user
 		setContentView(R.layout.activity_pregame_selection_multi);
 		int anmID = getIntent().getIntExtra("anm", 0);
 		Drawable animal = ((ImageButton) findViewById(anmID)).getDrawable();
 		bg = getIntent().getIntExtra("bg", 0);
 		Drawable background = ((ImageButton) findViewById(bg)).getDrawable();
 
 		// Initialize the database
 		Parse.initialize(this, "Iy4JZxlewoSxswYgOEa6vhOSRgJkGIfDJ8wj8FtM", "SVlq5dqYQ4FemgUfA7zdQvdIHOmKBkc5bXoI7y0C"); 
 		
 		// Get the user name
 		String uname = getIntent().getStringExtra("username");
 
 		// Start model, passing number of words, user name, and selected animal
 		model = new MultiPlayerModel(NUM_WORDS, uname, anmID);
 		model.addObserver(this);
     
 		// Get the opponent's animal from the model
     int oppAnimal = reverseDrawable(model.getOpponentAnimal());
 		
 		// Display the multiplayer screen
 		setContentView(R.layout.activity_multi_player);
 		initialDisplay(animal, background, oppAnimal);
 
 		// Create and start timer
 		gameTimer = new GameTimer(START_TIME, INTERVAL);
 		gameTimer.start();
 	}
 	
 	@Override
 	public final boolean onCreateOptionsMenu(final Menu menu) {
 	  // Inflate the menu; this adds items to the action bar if it is present.
 	  getMenuInflater().inflate(R.menu.single_player, menu);
 	  return true;
 	}
 	
 	@Override
 	/**
 	 * Called when the user types a letter; passes the letter to the model.
 	 */
 	public final boolean onKeyDown(final int key, final KeyEvent event){ 	  
 	  char charTyped = event.getDisplayLabel();
 	  charTyped = Character.toLowerCase(charTyped);
 	  model.typedLetter(charTyped);
 	  return true;
 	}
     
 	/**
 	 * @param wordIndex The index of the word to display; 0 <= wordIndex < 5.
    * @param word The word to display.
    */
 	public final void displayWord(final int wordIndex, final String word) {
 	  if ((wordIndex < 0) || (wordIndex >= NUM_WORDS)) {
 	    // error!
 	  }
 	  TextView wordBox = (TextView) getByStringId("word" + wordIndex);
 	  wordBox.setText(word);
 	}
 
   /**
    * Updates the timer on the screen.
    * @param secondsLeft The number of seconds to display.
    */
 	public final void displayTime(final long secondsLeft) {
 	  TextView timerBox = (TextView) findViewById(R.id.time_text);
 	  timerBox.setText(Long.toString(secondsLeft));
 	}
 
 	/**
 	 * Updates the score on the screen.
    * @param score The score to display.
    */
 	public final void displayScore(final int score) {
 	  TextView currentScore = (TextView) findViewById(R.id.score);
 	  currentScore.setText(Integer.toString(score));
 	}
 
   /**
    * Highlights the letterIndex letter of the wordIndex word. letterIndex must
    * not be beyond the scope of the word.
    * @param wordIndex The index of the word to highlight; 0 <= wordIndex < 5.
    * @param letterIndex The index of the letter in the word to highlight.
    */
 	public void highlightWord(final int wordIndex, final String word, final int letterIndex) {
 	  TextView wordBox = (TextView) getByStringId("word" + wordIndex);
 	  String highlighted  = word.substring(0, letterIndex);
 	  String rest = word.substring(letterIndex);
 	  wordBox.setText(Html.fromHtml("<font color=#00FF00>" + highlighted + "</font>" + rest));
 	}
 
   /**
    * Displays the initial screen of the single player game.
    * @param animal Drawable referring to the id of the selected animal image,
    * e.g. R.drawable.elephant_color.
    * @param backgroudID Drawable referring to the id of the selected background image.
    * @param words An array of the words to display. Must have a length of 5.
    */
 	public void initialDisplay(Drawable animal, Drawable background, int oppAnimal) {
 	  // display animal
 	  ImageView animalImage = (ImageView) findViewById(R.id.animal_image);
 	  animalImage.setImageDrawable(animal);
 	  
 	  // display opponent's animal
 	  ImageView oppAnimalImage = (ImageView) findViewById(R.id.opp_animal_image);
 	  oppAnimalImage.setBackgroundResource(oppAnimal);
 
 	  // display background
 	  ViewGroup layout = (ViewGroup) findViewById(R.id.game_layout);
 	  layout.setBackground(background);
 
 	  model.populateDisplayedList();
 
 	  // TODO figure out how to change milliseconds to seconds. it skips numbers
 	  displayTime(START_TIME / INTERVAL);
 
 	  displayScore(0);
 	}
     
 	/**
 	 * Updates the oppenent's score on the screen.
 	 * @param score The score to display.
 	 */
 	public final void displayOpponentScore(final int score) {
 		TextView currentScore = (TextView) findViewById(R.id.opp_score);
 		currentScore.setText(Integer.toString(score));
 	}
 
 	/**
 	 * Observer for model.
 	 * @param arg0 Thing being observes.
 	 * @param arg1 State.
 	 */
 	@Override
 	public void update(final Observable arg0, final Object arg1) {
 		MultiPlayerModel mpM;
 		if (arg0 instanceof MultiPlayerModel) {
 			mpM = (MultiPlayerModel) arg0;
 
 			if (arg1 instanceof States.update) {
 				States.update change = (States.update) arg1;
 				TextView tv = (TextView)findViewById(R.id.typedError_prompt);
 				if (change == States.update.FINISHED_WORD) {
 					displayScore(mpM.getScore());
 					displayWord(mpM.getCurrWordIndex(), mpM.getCurrWord());
 					tv.setVisibility(TextView.INVISIBLE);
 				} else if (change == States.update.HIGHLIGHT) {
 					highlightWord(mpM.getCurrWordIndex(), mpM.getCurrWord(), 
 								  mpM.getCurrLetterIndex());
 					tv.setVisibility(TextView.INVISIBLE);
 				} else if (change == States.update.OPPONENT_SCORE) {
 					displayOpponentScore(mpM.getOpponentScore());
 					tv.setVisibility(TextView.INVISIBLE);
 				} else if (change == States.update.WRONG_LETTER) {
 					//final ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
 					//final RelativeLayout rl = (RelativeLayout) findViewById(R.id.single_game_layout);
 					//tg.startTone(ToneGenerator.TONE_CDMA_ONE_MIN_BEEP);
 					tv.setVisibility(TextView.VISIBLE);
 
 				}
 
 			}
 		}
 	}
 
 	/**
 	 * Reopens keyboard when it is closed (or closes it when it's opened).
 	 * @param view The button clicked.
 	 * @author oaknguyen
 	 */
 	public final void keyboardButton(final View view) {
 	  InputMethodManager inputMgr = (InputMethodManager) 
 	      getSystemService(Context.INPUT_METHOD_SERVICE);
 	  inputMgr.toggleSoftInput(0, 0);
 	}
 
 	/**
 	 * Called when the timer runs out; goes to the post game screen.
 	 */
 	public final void goToPostGame() {
 	  model.setUserFinish();
 	  	  
 	  Intent intent = new Intent(this, PostGameScreenMulti.class);
 
 	  // Pass scores and if you won to post game screen
     int score = model.getScore();
 	  int oppScore = model.getOpponentScore();
 	  intent.putExtra("score", score);
 	  intent.putExtra("oppScore", oppScore);
 	  intent.putExtra("won", (score > oppScore));
 
 	  // Pass if opponent completed the game
 	  intent.putExtra("discon", !model.isOpponentFinished());
 
 	  // Pass background to post game screen
 	  intent.putExtra("bg", bg);
 
 	  startActivity(intent);		
 	}
 
 
 	/**
 	 * Timer for the game.
 	 * @author ZooTypers
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
 	    // TODO add game over message before going to post game
 	    goToPostGame();
 	  }
 
 	  @Override
 	  public final void onTick(final long millisUntilFinished) {
 	    currentTime = millisUntilFinished;
 	    displayTime(TimeUnit.MILLISECONDS.toSeconds(currentTime));
 	  }
 	}
 }
