 package edu.pugetsound.vichar;
 
 import edu.pugetsound.vichar.ar.ARGameActivity; // MB: so we can start the ARGameActivity
 import android.os.Bundle;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.graphics.PixelFormat;
 import android.widget.Button;
 import android.view.animation.AlphaAnimation;
 import android.view.animation.Animation;
 import android.view.animation.LinearInterpolator;
 import android.content.Context;
 
 /**
  * Activity for the main menu screen, contains buttons to navigate to other 
  * important screens.
  * @author Davis Shurbert
  *
  */
 public class MainMenuActivity extends WifiRequiredActivity {
 	final Context context = this;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		//help with banding on gradients
 		getWindow().setFormat(PixelFormat.RGBA_8888);
 
 		setContentView(R.layout.activity_main_menu);
 
 		//set title to screenname        
 		setTitle(getScreenname());
 		createButtons();            
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_main_menu, menu);
 		return true;
 	}
 
 	/**
 	 * Returns current user's screenname
 	 * @return Username of current user
 	 */
 	private String getScreenname() {
 		PreferenceUtility pu = new PreferenceUtility();
 		return pu.returnSavedString(getString(R.string.screenname_key), getString(R.string.prefs_error), this);
 	}    
 
 	/**
 	 * "Logs out" of twitter. Doesn't actually erase any OAuth information
 	 */
 	public void logoutTwitter() {
 		PreferenceUtility pu = new PreferenceUtility();
 		pu.saveBoolean(getString(R.string.tw_login_key), false, this);
 		startActivity(new Intent(this, MainActivity.class)); //redirect to login screen 
 	}
     
     private void createButtons() {
     	Button anib = (Button)findViewById(R.id.button_animation); //declaring the button
         anib.setOnClickListener(animationListener); //making the thing that checks if the button's been pushed
        anib.setOnTouchListener(animationTouchListener);
         
         Button gameb = (Button)findViewById(R.id.game_button); //declaring the button
         gameb.setOnClickListener(gameListener); //making the thing that checks if the button's been pushed
         
         
             //animation on play button
             final AlphaAnimation animation = new AlphaAnimation(1, (float) .2); // Change alpha from fully visible to invisible
             animation.setDuration(1000); // duration - half a second
             animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
             animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
             animation.setRepeatMode(Animation.REVERSE); // Reverse animation at the end so the button will fade back in
             anib.startAnimation(animation);
             
 
         
         Button mainb = (Button)findViewById(R.id.about_button); //declaring the button
         mainb.setOnClickListener(aboutListener); //making the thing that checks if the button's been pushed
         
         Button leaderboardb = (Button)findViewById(R.id.leaderboard_button); 
         leaderboardb.setOnClickListener(leaderboardListener);
         
         Button rulesb = (Button)findViewById(R.id.rules_button); 
         rulesb.setOnClickListener(rulesListener);
         
         Button settingsb = (Button)findViewById(R.id.settings_button); 
         settingsb.setOnClickListener(settingsListener);
     }
     
     
    public OnTouchListener animationTouchListener = new OnTouchListener() { //sets what happens when the button is pushed
        public boolean onTouch(View v, MotionEvent event) { 
            v.clearAnimation();
            v.setPressed(true);
            startActivity(new Intent(context, ARGameActivity.class));
            return true;
        }
       };
 
     
    	private OnClickListener settingsListener = new OnClickListener() { //sets what happens when the button is pushed
 		public void onClick(View v) { 
 			logoutTwitter();
 			startActivity(new Intent(context, MainActivity.class));
 		}
 	};
        
     private OnClickListener rulesListener = new OnClickListener() { //sets what happens when the button is pushed
     	public void onClick(View v) { 
         	
     		startActivity(new Intent(context, RulesActivity.class));
         }
        };
        
     private OnClickListener leaderboardListener = new OnClickListener() { //sets what happens when the button is pushed
     	public void onClick(View v) { 
         	
     		startActivity(new Intent(context, LeaderboardActivity.class));
         }
        };
        
     private OnClickListener animationListener = new OnClickListener() { //sets what happens when the button is pushed
     	public void onClick(View v) { 
     	    v.clearAnimation();
     		startActivity(new Intent(context, ARGameActivity.class));
         }
        };
        
        private OnClickListener gameListener = new OnClickListener() { //sets what happens when the button is pushed
            public void onClick(View v) { 
                startActivity(new Intent(context, ARGameActivity.class));
            }
           };
     private OnClickListener aboutListener = new OnClickListener() { //sets what happens when the button is pushed
     	public void onClick(View v) { 
         	
     		startActivity(new Intent(context, AboutActivity.class));
         }
        };
 }
