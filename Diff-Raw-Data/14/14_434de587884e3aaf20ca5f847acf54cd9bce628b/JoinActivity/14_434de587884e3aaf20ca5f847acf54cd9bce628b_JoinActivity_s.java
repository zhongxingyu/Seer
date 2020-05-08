 package edu.rit.cs.distrivia;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 /**
  * Activity for players to answer questions during a round.
  */
 public class JoinActivity extends Activity {
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(final Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.join);
 
         final Button joinPublicButton = (Button) findViewById(R.id.join_public_button);
         final Button joinPrivateButton = (Button) findViewById(R.id.join_private_button);
 
         final TextView playersLabel = (TextView) findViewById(R.id.num_players_text);
 
         final EditText privateName = (EditText) findViewById(R.id.private_name_text);
         final EditText privatePass = (EditText) findViewById(R.id.private_pass_text);
 
         final LinearLayout publicLayout = (LinearLayout) findViewById(R.id.public_layout);
         final LinearLayout privateLayout = (LinearLayout) findViewById(R.id.private_layout);
 
         joinPublicButton.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(final View v) {
                 privateLayout.setVisibility(View.INVISIBLE);
                 playersLabel.setText("Players: 10/20");
                 Intent roundIntent = new Intent();
                 roundIntent.setClassName("edu.rit.cs.distrivia",
                         "edu.rit.cs.distrivia.RoundActivity");
                 // SystemClock.sleep(2000);
                 startActivity(roundIntent);
             }
         });
         
         joinPrivateButton.setOnClickListener(new OnClickListener() {
         	@Override
         	public void onClick(final View v) {
         		String name = privateName.getText().toString().trim();
         		String pass = privatePass.getText().toString().trim();
         		if (!name.equals("") && !pass.equals("")) {
         			publicLayout.setVisibility(View.GONE);
         		}
         	}
         });
 
     }
 }
