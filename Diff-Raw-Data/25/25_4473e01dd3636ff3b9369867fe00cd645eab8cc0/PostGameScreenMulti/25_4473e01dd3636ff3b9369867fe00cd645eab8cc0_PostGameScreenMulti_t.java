 package com.example.zootypers.ui;
 
 import android.annotation.SuppressLint;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.ImageButton;
 import android.widget.TextView;
 
 import com.example.zootypers.R;
 import com.example.zootypers.core.MultiLeaderBoardModel;
 import com.example.zootypers.util.InternetConnectionException;
 
 /**
  * Post game activity / UI for a multiplayer game.
  */
 public class PostGameScreenMulti extends PostGameScreen {
 
   String username;
 
   @SuppressLint("NewApi")
   @Override
   protected void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
 
     // Get & display background
     setContentView(R.layout.activity_pregame_selection_multi);
     Drawable background = ((ImageButton) 
         findViewById(getIntent().getIntExtra("bg", 0))).getDrawable();
 
     setContentView(R.layout.activity_post_game_screen_multi);
     findViewById(R.id.postgame_layout).setBackground(background);
 
     // Get and display the player's score
     score = getIntent().getIntExtra("score", 0);
     TextView finalScore = (TextView) findViewById(R.id.final_score);
     finalScore.setText(score.toString());
     
     opponentDisplay();
 
     // Get and store the username
     username = getIntent().getStringExtra("username");
   }
   
   /**
    * Display opponent's score & result of the game.
    */
   protected void opponentDisplay() {
     // Get and display the opponent's score
     Integer oppScore = getIntent().getIntExtra("oppScore", 0);
     TextView oppFinalScore = (TextView) findViewById(R.id.opp_final_score);
     oppFinalScore.setText(oppScore.toString());
 
     // Determine & display result of the game
     TextView resultMessage = (TextView) findViewById(R.id.game_result);
     int result = getIntent().getIntExtra("result", 0);
     if (result == 1) {
       resultMessage.setText("You Won!");
     } else if (result == 0) {
       resultMessage.setText("You Tied!");
     } else {
       resultMessage.setText("You Lost.");
     }    
   }
 
 	@Override
 	public final void saveScore(final View view) {
 		MultiLeaderBoardModel ml;
 		try {
 			ml = new MultiLeaderBoardModel();
 			ml.setPlayer(username);
 		} catch (InternetConnectionException e) {
 			Log.i("Leaderboard", "triggering internet connection error screen");
 			Intent intent = new Intent(this, ErrorScreen.class);
			intent.putExtra("error", R.layout.activity_connection_error_lb);
 			startActivity(intent);
 			return;
 		}
     	ml.addEntry(score);
 		final String title = "Saved Score";
 		final String message = "Your score has been successfully saved!";
 		buildAlertDialog(title, message);
 	}
 
   @Override
   public final void goToPreGameSelection(final View view) {
     Intent intent = new Intent(this, PreGameSelectionMulti.class);
     intent.putExtra("username", username);
     startActivity(intent);
   }
 
     
     // TODO remove repetition from title page / options
 	/**
 	 * builds an AlertDialog popup with the given title and message
 	 * @param title String representing title of the AlertDialog popup
 	 * @param message String representing the message of the AlertDialog
 	 * popup
 	 */
 	private void buildAlertDialog(String title, String message) {
 		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
 
 		// set title
 		alertDialogBuilder.setTitle(title);
 
 		// set dialog message
 		alertDialogBuilder
 		.setMessage(message)
 		.setCancelable(false)
 		.setPositiveButton("Close", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				// if this button is clicked, close the dialog box
 				dialog.cancel();
 			}
 		});
 
 		// create alert dialog
 		AlertDialog alertDialog = alertDialogBuilder.create();
 
 		// show the message
 		alertDialog.show();
 	}
 
 
 }
