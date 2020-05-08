 /*author: Christopher Farm*/
 
 package com.appordinance.ImagePuzzle;
 
 import com.appordinance.ImagePuzzle.R;
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.Toast;
 
 
 public class YouWin extends Activity implements OnClickListener{
 	//set shared preferences settings
 	final String GAME_SETTINGS = "mySettings";
 	
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		
 		//set the view
 		setContentView(R.layout.congrats);
 		
 		//get resources for the preferences
 		SharedPreferences settings = getSharedPreferences(GAME_SETTINGS, 0);
 		
 		//show the number of times it took to win
 		Toast.makeText(this, "You have moved "+Integer.toString(settings.getInt("numMoves", 0))+" times to win.", Toast.LENGTH_LONG).show();
 		
 		//set the click listener on the button
 		Button reset = (Button) findViewById(R.id.button1);
 		reset.setOnClickListener(this);
 	}
 
 	@Override
 	public void onClick(View v) {
 		
 		//when the button is fired reset the played button to false and finish the activity
 		switch(v.getId()){
 		case R.id.button1:
 			finish();
 			SharedPreferences settings = getSharedPreferences(GAME_SETTINGS, 0);
 			SharedPreferences.Editor editor = settings.edit();
 			editor.putBoolean("hasPlayed", false);
 			editor.commit();
 		}
 		
 	}
 
 }
