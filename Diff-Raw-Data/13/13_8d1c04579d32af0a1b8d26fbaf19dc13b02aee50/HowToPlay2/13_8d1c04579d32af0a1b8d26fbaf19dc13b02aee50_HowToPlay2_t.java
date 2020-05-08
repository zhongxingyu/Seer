 package uw.cse403.nonogramfun;
 /**
  * CSE 403 AA
  * Project Nonogram: Frontend
  * @author  Renhao Xie, Zhe Shi
  * @version v1.0, University of Washington 
  * @since   Spring 2013 
  */
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 
 /**
  * The second page of HowToPlay screen, give tutorial to user about how to play the game
  */
 public class HowToPlay2 extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.how_to_play2);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.how_to_play2, menu);
 		return true;
 	}
 
 	/**
 	 * go to the next screen
 	 * @param view
 	 */
 	public void next(View view) {
 		Intent i = new Intent(this, HowToPlay3.class);
 		startActivity(i);
 	}
 	
 	/**
 	 * go to the previous screen
 	 * @param view
 	 */
 	public void prev(View view) {
 		Intent i = new Intent(this, HowToPlay.class);
 		startActivity(i);
 	}
 	
 	/**
 	 * go back to the main screen
 	 * @param view
 	 */
 	public void skip(View view) {
 		Intent i = new Intent(this, MainActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		startActivity(i);
 	}
 }
