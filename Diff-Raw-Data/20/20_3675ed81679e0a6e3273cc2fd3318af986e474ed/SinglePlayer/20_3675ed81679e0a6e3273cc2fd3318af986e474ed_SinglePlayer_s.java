 package com.example.zootypers;
 
 import java.util.Observable;
 import java.util.Observer;
 import java.util.concurrent.TimeUnit;
 import android.annotation.SuppressLint;
 import android.app.ActionBar.LayoutParams;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.text.Html;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.PopupWindow;
 import android.widget.TextView;
 import com.example.zootypers.States.difficulty;
 
 @SuppressLint("NewApi")
 /**
 *
 * UI / Activity and controller for single player game screen.
 * @author cdallas, littlpunk, kobyran
 *
 */
 public class SinglePlayer extends Activity implements Observer {
 
     // used for the communicating with model
     private SinglePlayerModel model;
     private final int NUM_WORDS = 5;  
     private int bg;
     
     // for the popup window
     private PopupWindow ppw;
     public LayoutParams popUpParams;
     public LinearLayout popUpLayout;
     private long currentTime;
     private long pausedTime = START_TIME;
 
     // for the game timer
     private GameTimer gameTimer;
     private final long INTERVAL = 1000; // 1 second
     public final static long START_TIME = 60000; // 1 minute
 
     @Override
     protected final void onCreate(final Bundle savedInstanceState) {
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
     
         // start model
         model = new SinglePlayerModel(d, this.getAssets(), NUM_WORDS);
         model.addObserver(this);
         
         // change screen view
         setContentView(R.layout.activity_single_player);
         initialDisplay(animal, background);
         
         // create and start timer
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
      * When the user types a letter, this listens for it.
      */
     public final boolean onKeyDown(final int key, final KeyEvent event){
         char charTyped = event.getDisplayLabel();
         charTyped = Character.toLowerCase(charTyped);
         model.typedLetter(charTyped);
         return true;
     }
     
     @Override 
     public void onPause() {
         super.onPause();
         // TODO trigger pause screen to pause when Home is pressed
     }
   
     @Override
     public void onBackPressed() {
         // TODO trigger pause screen!
     }
 
     /**
      * Displays the initial screen of the single player game.
      * @param animalID Drawable referring to the id of the selected animal image,
      * e.g. R.drawable.elephant_color.
      * @param backgroudID Drawable referring to the id of the selected background image.
      * @param words An array of the words to display. Must have a length of 5.
      */
     public void initialDisplay(Drawable animalID, Drawable backgroundID) {
         // display animal
         ImageView animalImage = (ImageView) findViewById(R.id.animal_image);
         animalImage.setImageDrawable(animalID);
 
         // display background
         ViewGroup layout = (ViewGroup) findViewById(R.id.single_game_layout);
         layout.setBackground(backgroundID);
 
         model.populateDisplayedList();
 
         // TODO figure out how to change milliseconds to seconds. it skips numbers
         displayTime(START_TIME / INTERVAL);
 
         displayScore(0);
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
     * Observer for model.
     * @param arg0 Thing being observes.
     * @param arg1 State.
     */
     public final void update(final Observable arg0, final Object arg1) {
         SinglePlayerModel spM;
         if (arg0 instanceof SinglePlayerModel) {
             spM = (SinglePlayerModel) arg0;
 
             if (arg1 instanceof States.update) {
                 States.update change = (States.update) arg1;

                 if (change == States.update.FINISHED_WORD) {
                     displayScore(spM.getScore());
                     displayWord(spM.getCurrWordIndex(), spM.getCurrWord());
                 } else if (change == States.update.HIGHLIGHT) {
                     highlightWord(spM.getCurrWordIndex(), spM.getCurrWord(), 
                             spM.getCurrLetterIndex());
                 } else if (change == States.update.WRONG_LETTER) {
                    // TODO print an error message?
                 }
 
             }
         }
     }
   
     /**
     * Reopens keyboard when it is closed
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
         Intent intent = new Intent(this, PostGameScreen.class);
         // pass score
         intent.putExtra("score", ((TextView) findViewById(R.id.score)).getText().toString());
         intent.putExtra("bg", bg);
         startActivity(intent);
     }
 
     /**
     * @param id The id of the View to get as a String.
     * @return The View object with that id
     */
     public final View getByStringId(final String id) {
         return findViewById(getResources().getIdentifier(id, "id", getPackageName()));
     }
 
     /**
     * When the pause button is pressed, pauses the game and shows a pop-up window.
     * @param view The button clicked.
     */
     public void pauseGame(View view) {
         // save & stop time
         pausedTime = currentTime;
         gameTimer.cancel();
 	  
         // disable buttons & keyboard
         findViewById(R.id.keyboard_open_button).setEnabled(false);
         findViewById(R.id.pause_button).setEnabled(false);
 
         // create popup window
         LayoutInflater layoutInflater = 
                 (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
         View popupView = layoutInflater.inflate(R.layout.pause_layout, null);
         ppw = new PopupWindow(popupView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
         ViewGroup parentLayout = (ViewGroup) findViewById(R.id.single_game_layout);
         ppw.showAtLocation(parentLayout, Gravity.CENTER, 10, 20);
         ppw.update(350, 500);
     }
   
     /**
     * When the user clicks the continue button while paused, continues the game.
     * @param view The button clicked.
     */
     public void pausedContinue(View view){
         // re-enable buttons & keyboard
         findViewById(R.id.keyboard_open_button).setEnabled(true);
         findViewById(R.id.pause_button).setEnabled(true);
         keyboardButton(findViewById(R.id.keyboard_open_button));
 	  
         gameTimer = new GameTimer(pausedTime, INTERVAL);
         gameTimer.start();
         ppw.dismiss();
     }
 
     /**
     * When the user clicks the new game button while paused, starts a new game.
     * @param view The button clicked.
     */
     public void pausedNewGame(View view) {
         final Intent restartIntent = new Intent(this, PreGameSelection.class);
         startActivity(restartIntent);
     }
   
     /**
     * When the user clicks the main menu button while paused, goes to the title screen. 
     * @param view The button clicked.
     */
     public void pausedMainMenu(View view) {
         final Intent mainMenuIntent = new Intent(this, TitlePage.class);
         startActivity(mainMenuIntent);
     }
   
     /**
     * Timer for game.
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
