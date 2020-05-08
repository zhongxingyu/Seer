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
  * The first page of HowToPlay screen, give tutorial to user about how to play the game
  */
 public class HowToPlay extends Activity {
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.how_to_play);
 		setTitle(getString(R.string.howtoplay_button));
 	}
 	 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 	
     /**
      * go to next page of HowToPlay
      * @param view
      */
 	public void next(View view) {
 		Intent i = new Intent(this, HowToPlay2.class);
 		startActivity(i);
 	}
 	
 	/**
 	 * back to the main menu
 	 * @param view
 	 */
 	public void skip(View view) {
 		Intent i = new Intent(this, MainActivity.class);
 		startActivity(i);
 	}
 }
