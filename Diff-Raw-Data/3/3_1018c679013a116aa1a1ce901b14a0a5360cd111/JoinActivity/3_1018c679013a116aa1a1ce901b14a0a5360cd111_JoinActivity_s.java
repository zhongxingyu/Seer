 package edu.rit.cs.distrivia;
 
 import android.os.Bundle;
 import android.os.SystemClock;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import edu.rit.cs.distrivia.api.DistriviaAPI;
 
 /**
  * Activity for players to answer questions during a round.
  */
 public class JoinActivity extends GameActivityBase {
 
     private final int UPDATE_MS = 5000;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(final Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.join);
 
         final Button pubButton = (Button) findViewById(R.id.join_public_button);
         final Button priButton = (Button) findViewById(R.id.join_private_button);
         final Button lbButton = (Button) findViewById(R.id.view_leaderboard_button);
         final TextView playersLabel = (TextView) findViewById(R.id.num_players_text);
         final EditText privateName = (EditText) findViewById(R.id.private_name_text);
         final EditText privatePass = (EditText) findViewById(R.id.private_pass_text);
         final LinearLayout publicLayout = (LinearLayout) findViewById(R.id.public_layout);
         final LinearLayout privateLayout = (LinearLayout) findViewById(R.id.private_layout);
 
         pubButton.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(final View v) {
                 privateLayout.setVisibility(View.GONE);
                 lbButton.setVisibility(View.GONE);
                 v.setEnabled(false);
                 playersLabel.setText("Waiting to join public game...");
                 joinPublic();
             }
         });
 
         priButton.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(final View v) {
                 String name = privateName.getText().toString().trim();
                 String pass = privatePass.getText().toString().trim();
                 if (!name.equals("") && !pass.equals("")) {
                     publicLayout.setVisibility(View.GONE);
                     v.setEnabled(false);
                     v.invalidate();
                     playersLabel.setText("Players: 10/20");
                     startActivity(ROUND_ACTIVITY);
                 } else {
                     makeToast("Enter name and pass");
                 }
             }
         });
 
         lbButton.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(final View v) {
                 startActivity(LEADERBOARD_ACTIVITY);
                 finish();
             }
         });
     }
 
     /**
      * Join public game, start's a new thread to do network IO
      */
     private void joinPublic() {
         new Thread() {
             @Override
             public void run() {
                 try {
                     setGameData(DistriviaAPI.join(gameData()));
                 } catch (Exception e) {
                     makeToast("Service is down, please try again later");
                     return;
                 }
 
                 boolean joinSuccessful = gameData().getGameID() != null;
                 joinSuccessful &= (!gameData().getGameID().equals(
                         DistriviaAPI.API_ERROR));
                 if (!joinSuccessful) {
                     makeToast("Join failure");
                     return;
                 }
 
                 try {
                     while (true) {
                         setGameData(DistriviaAPI.status(gameData()));
                         if (!gameData().isWaiting()) {
                             break;
                         }
                         SystemClock.sleep(UPDATE_MS);
                     }
                 } catch (Exception e) {
                     makeToast("Service is down, please try again later");
                     return;
                 }
                 startActivity(ROUND_ACTIVITY);
             }
         }.start();
     }
 
 }
