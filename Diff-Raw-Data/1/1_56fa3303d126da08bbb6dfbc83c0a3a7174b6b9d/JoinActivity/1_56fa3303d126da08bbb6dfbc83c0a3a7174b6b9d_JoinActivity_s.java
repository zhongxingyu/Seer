 package edu.rit.cs.distrivia;
 
 import org.json.JSONException;
 
 import edu.rit.cs.distrivia.api.DistriviaAPI;
 import edu.rit.cs.distrivia.model.GameData;
 import edu.rit.cs.distrivia.api.JSON;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.SystemClock;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * Activity for players to answer questions during a round.
  */
 public class JoinActivity extends Activity {
 	
 	GameData gd;
 	
     /** Called when the activity is first created. */
 	@Override
     public void onCreate(final Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.join);
         gd = (GameData) getIntent().getExtras().getSerializable("game_data");
 
         final Button joinPublicButton = (Button) findViewById(R.id.join_public_button);
         final Button joinPrivateButton = (Button) findViewById(R.id.join_private_button);
         final Button viewLeaderboardButton = (Button) findViewById(R.id.view_leaderboard_button);
 
         final TextView playersLabel = (TextView) findViewById(R.id.num_players_text);
 
         final EditText privateName = (EditText) findViewById(R.id.private_name_text);
         final EditText privatePass = (EditText) findViewById(R.id.private_pass_text);
 
         final LinearLayout publicLayout = (LinearLayout) findViewById(R.id.public_layout);
         final LinearLayout privateLayout = (LinearLayout) findViewById(R.id.private_layout);
 
         joinPublicButton.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(final View v) {
             	privateLayout.setVisibility(View.GONE);
             	viewLeaderboardButton.setVisibility(View.GONE);
                 v.setEnabled(false);
                 playersLabel.setText("Waiting to join public game...");
             	joinPublic();
             }
         });
         
         joinPrivateButton.setOnClickListener(new OnClickListener() {
         	@Override
         	public void onClick(final View v) {
         		String name = privateName.getText().toString().trim();
         		String pass = privatePass.getText().toString().trim();
         		if (!name.equals("") && !pass.equals("")) {
         			publicLayout.setVisibility(View.GONE);
         			v.setEnabled(false);
         			playersLabel.setText("Players: 10/20");
         			Intent roundIntent = new Intent();
                     roundIntent.setClassName("edu.rit.cs.distrivia",
                             "edu.rit.cs.distrivia.RoundActivity");
                     startActivity(roundIntent);
         		}
         		else {
         			Toast.makeText(v.getContext(), "Enter name and pass", 10)
         				.show();
         		}
         	}
         });
         
         viewLeaderboardButton.setOnClickListener(new OnClickListener() {
         	@Override
         	public void onClick(final View v) {
         		Intent leaderboardIntent = new Intent();
         		leaderboardIntent.setClassName("edu.rit.cs.distrivia",
                 "edu.rit.cs.distrivia.LeaderboardActivity");
         		startActivity(leaderboardIntent);
         	}
         });
 
     }
 	
 	private void joinPublic() {
 		Context context = getApplicationContext();
 
         try {
             gd = DistriviaAPI.join(gd);
         } catch (Exception e) {
             Toast.makeText(context, "Service is down, please try again later",
                     10).show();
             return;
         }
         boolean joinSuccessful = gd.getGameID() != null;
         joinSuccessful &= (!gd.equals(DistriviaAPI.API_ERROR));
 
         if (joinSuccessful) {       	
             //Toast.makeText(context, "Joining Public", 10).show();
         	try {
         		while ( DistriviaAPI.status(gd).isWaiting()) {
         			SystemClock.sleep(5000);
         		}
         	}
         	catch (Exception e) {
         		Toast.makeText(context, "Service is down, please try again later",
         			10).show();
         	}
             Intent roundIntent = new Intent();
             roundIntent.setClassName("edu.rit.cs.distrivia",
                     "edu.rit.cs.distrivia.RoundActivity");
          // Make sure to pass session/game data to the next view
             roundIntent.putExtra("game_data", gd);
             startActivity(roundIntent);
 
         } else {
             Toast.makeText(getApplicationContext(), "Join failure", 10).show();
         }
 	}
 }
