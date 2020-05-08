 package com.ohhaiku;
 
 import android.app.Activity;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 /*
  * Main activity: shows the Haiku composition window.
  */
 
 
 public class HaikuCompositionActivity extends Activity implements OnClickListener{
     /** Called when the activity is first created. */
 	  Button checkHaikuButton;
 	    Button tweetHaikuButton;
 	    Button menuButton;
 	    Button saveButton;
 	    EditText textRow1;
 	    EditText textRow2;
 	    EditText textRow3;
 	
 	@Override
     
   
     public void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
         TextView tw = new TextView(this);
         tw.setText("Skriv in din Haiku!");
         setContentView(R.layout.main);
 //        checkHaikuButton = (Button) findViewById(R.id.CheckHaikuButton);
 //        checkHaikuButton.setOnClickListener((android.view.View.OnClickListener) this);
 //        tweetHaikuButton = (Button) findViewById(R.id.TweetHaikuButton); 
 //        tweetHaikuButton.setOnClickListener((android.view.View.OnClickListener) this);
 //        menuButton = (Button) findViewById(R.id.MenuButton); 
 //        menuButton.setOnClickListener((android.view.View.OnClickListener) this);
 //        saveButton = (Button) findViewById(R.id.SaveButton); 
 //        saveButton.setOnClickListener((android.view.View.OnClickListener) this);
     	
     }
 	
 	public void goToMenu(View view) {
     startActivity(new Intent(this, MenuActivity.class));
 	}
 
 	public void onClick(DialogInterface arg0, int buttonID) {
 		if ( buttonID== R.id.CheckHaikuButton){
 			//check if every haiku-row has text, then start check-haiku-activity
			if ((textRow1.getText().toString().length() <1) && (textRow2.getText().toString().length() <1) && (textRow2.getText().toString().length() <1) ) {
 				//startActivity(new Intent(this, ...)); 
 			}
 		}
 		if ( buttonID== R.id.TweetHaikuButton){
 			//if certified is true (or maybe certifiedhaiku-textview =VISIBLE??)
 			startActivity(new Intent(this, TweetAHaikuActivity.class));  
 			
 		}
 		if ( buttonID== R.id.MenuButton){
 			startActivity(new Intent(this, MenuActivity.class));
 			
 		}
 		if ( buttonID== R.id.SaveButton){
 			//save in DB, do we need save-activity?
 			
 		}
 	}
 	}//HaikuCompositionActivity
