 package edu.pugetsound.vichar;
 
 
 
 
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 
 
 /**
  * An activity that allows the user to log into the game through the Vi-Char
  * server instead of twitter. 
  * @author Kirah Taylor
  * @version 10/24/12
  */
 
 public class LoginActivity extends Activity {
 	
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    setContentView(R.layout.activity_login);
 	    Button button = (Button)findViewById(R.id.button1);
 		button.setOnClickListener(startListener);
 	}
 	
 	private OnClickListener startListener = new OnClickListener() {
     	public void onClick(View v) {
         	EditText text  = (EditText)findViewById(R.id.editText1);
         	String words;
 			words = text.getText().toString();
 			storeUsername(words);
 			Intent next = new Intent(LoginActivity.this, MainMenuActivity.class);
 			startActivity(next);
         }
     };
     
 	
   /**
     * Stores the entered string to persistent storage
     * @param String username The string being saved
     */ 
     private void storeUsername(String username)
     {
     	PreferenceUtility prefs = new PreferenceUtility();
    	prefs.saveString(getString(R.string.guest_name), username, this);
     }
 
 }
