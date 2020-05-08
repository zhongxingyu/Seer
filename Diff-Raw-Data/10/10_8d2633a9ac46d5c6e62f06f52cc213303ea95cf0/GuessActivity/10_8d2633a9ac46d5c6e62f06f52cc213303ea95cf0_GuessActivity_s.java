 package edu.msu.cse.monopoly.scribbles;
 
 import android.os.Bundle;
 import android.os.CountDownTimer;
import android.os.Parcel;
import android.os.Parcelable;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.view.KeyEvent;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class GuessActivity extends Activity {
     private static final String PLAYER1 = "player1";
     private static final String PLAYER2 = "player2";
     private static final String PLAYER1SCORE = "player1Score";
     private static final String PLAYER2SCORE = "player2Score";
     private static final String HINT = "hint";
     private static final String TOPIC = "topic";
     private static final String ANSWER = "answer";
     private static final String TIMER = "timer";
     private static final String GUESS = "guess";
     private static final String WHOSDRAWING = "whosDrawing";
    private static final String LINES = "lines";
     
     /**
      * The drawing view object
      */
     private DrawingView guessingView = null;
     
     private int player1Score;
     private int player2Score;
     
     private String player1Name;
     private String player2Name;
     private String Hint;
     private String Answer;
     private String Category;
     private long currentTime;
     
     private int whosDrawing;
     
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_guess);
 		
 		Bundle bundle = getIntent().getExtras();
 		
 		player1Score = bundle.getInt(PLAYER1SCORE);
         player2Score = bundle.getInt(PLAYER2SCORE);
         player1Name = bundle.getString(PLAYER1);
         player2Name = bundle.getString(PLAYER2);
         whosDrawing = bundle.getInt(WHOSDRAWING);
         Hint = bundle.getString(HINT);
         Answer = bundle.getString(ANSWER);
         Category = bundle.getString(TOPIC);
         whosDrawing = bundle.getInt(WHOSDRAWING);
         
         guessingView = (DrawingView) findViewById(R.id.guessingView);
 
         guessingView.getFromBundle(bundle);
         guessingView.setMoveFlag(true); // Always moving in guessing activity
         
 		final TextView myTimer = (TextView) findViewById(R.id.theTimer);
 		final TextView hintText = (TextView) findViewById(R.id.Hint);
 		
 		TextView categoryText = (TextView) findViewById(R.id.Category);
 		categoryText.setText(Category);
 		long myBegin = 130000;
 		
         if(player1Name != null)
         {
             TextView player1ScoreText = (TextView) findViewById(R.id.player1ScoreText);
             
             	player1ScoreText.setText(player1Name + ": " + Integer.toString(player1Score));
         }
         
         if(player2Name != null){
         	
             TextView player2ScoreText = (TextView) findViewById(R.id.player2ScoreText);
 
             player2ScoreText.setText(player2Name + ": " + Integer.toString(player2Score));
 
         }
 		
 		if(savedInstanceState != null) { // We're rotating, load the relevent strings
 			myBegin = savedInstanceState.getLong(TIMER);
 			EditText guessBox = (EditText) findViewById(R.id.guessBox);
 			guessBox.setText(savedInstanceState.getString(GUESS));	
 		}
 		
 		// Set the guesser text view
     	TextView whosDrawingText = (TextView) findViewById(R.id.whosGuessingText);
 		if (whosDrawing == 1){
         	whosDrawingText.setText(whosDrawingText.getText().toString() + " " + player2Name);
 		}
 		else{
         	whosDrawingText.setText(whosDrawingText.getText().toString() + " " + player1Name);
 		}
 		
 		new CountDownTimer(myBegin, 1000) {
 		     public void onTick(long msTillDone) {
 		    	 currentTime = msTillDone;
 		    	 long temp = msTillDone/1000;
 		    	 if ((msTillDone / 60000) == 2) {
 		    		 temp -= 120;
 		    		 myTimer.setText("2:" + (temp < 10 ? ("0"+temp) : temp));
 		    	 } else if ((msTillDone / 60000 ) == 1){
 		    		 temp -= 60;
 		    		 myTimer.setText("1:" + (temp < 10 ? ("0"+temp) : temp));
 		    	 } else {
 		    		 myTimer.setText("0:" + (temp < 10 ? ("0"+temp) : temp));
 		    	 }
 		    	 
 		    	 if (msTillDone<=70000) {
 		    		 hintText.setText("Hint: "+ Hint);
 		    	 } 
 		     }
 
 		     public void onFinish() {
 		         myTimer.setText("Out of Time!");
 		     }
 		  }.start();
 	}
 	
 	public void onDone(View view) {
 		
 		EditText guessBox = (EditText) findViewById(R.id.guessBox);
 		
 		// Correct guess - score it
 		if (guessBox.getText().toString().equals(Answer)){
 			if (whosDrawing == 1){
 				player2Score += 50;
 			}
 			if (whosDrawing == 2){
 				player1Score += 50;
 			}
 		}
 		
 		Intent intent;
 		if (player1Score >= 500 || player2Score >= 500){ // Someone won!!
 			intent = new Intent(this, FinalScoreActivity.class);
 			
 			// Put the player's scores in the bundle
 			intent.putExtra(PLAYER1SCORE, player1Score);
 			intent.putExtra(PLAYER2SCORE, player2Score);
 			
 			// Put the player's names in the bundle
 			intent.putExtra(PLAYER1, player1Name);
 			intent.putExtra(PLAYER2, player2Name);
 			
 		}else{
 			intent = new Intent(this, DrawActivity.class);
 			
 			// Put the player's scores in the bundle
 			intent.putExtra(PLAYER1SCORE, player1Score);
 			intent.putExtra(PLAYER2SCORE, player2Score);
 			
 			// Put the player's names in the bundle
 			intent.putExtra(PLAYER1, player1Name);
 			intent.putExtra(PLAYER2, player2Name);
 			
 			// Switch the drawer
 			if (whosDrawing == 1){
 				intent.putExtra(WHOSDRAWING, 2);
 			}
 			else{
 				intent.putExtra(WHOSDRAWING, 1);
 			}
 		}
 		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		startActivity(intent);
     }
 	
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         
 		// Put the artist's hint in the bundle
 		outState.putString(HINT, Hint);
 		
 		// Put the artist's answer in the bundle
 		outState.putString(ANSWER, Answer);
 		
 		// Put the player's scores in the bundle
 		
 		outState.putInt(PLAYER1SCORE, player1Score);
 		outState.putInt(PLAYER2SCORE, player2Score);
 		
 		// Put the topic string into the bundle
 		outState.putString(TOPIC, Category);
 		
 		// Put the player's names in the bundle
 		outState.putString(PLAYER1, player1Name);
 		outState.putString(PLAYER2, player2Name);
 		
 		// Put the Timer in the bundle
 		outState.putLong(TIMER, currentTime);
 		
 		EditText guessBox = (EditText) findViewById(R.id.guessBox);
 		String Guess = guessBox.getText().toString();
 		outState.putString(GUESS, Guess);
     }
     
     /** Handles key presses
      * @param keycode The code of the key
      * @param event The key event.
      * 
      * Used to disable back button
      */
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event)  {
         if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {	
         	/* Create a warning dialog box. User can't go back to draw, but they can exit
         	 * to the home screen.
         	 */
         	AlertDialog.Builder builder = new AlertDialog.Builder(this);
 
         	builder.setMessage(R.string.dialog_box_warning);
         	builder.setTitle(R.string.dialog_box_title);
 
         	AlertDialog warningDialog = builder.create();
         	
             builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {
                 	// User wants to go back to the welcome screen
             		Intent intent = new Intent(GuessActivity.this, WelcomeScreen.class);
             		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
             		startActivity(intent);
                 }
             });
             builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {
                     // User wants to stay here, do nothing.
                 }
             });
         			
             builder.show();
             warningDialog.dismiss();
             return true;
         }
 
         return super.onKeyDown(keyCode, event);
     }
 
 }
