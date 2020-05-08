 package com.project.gemswapper;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.view.WindowManager.LayoutParams;
 import android.widget.ImageButton;
 
 public class MainActivity extends Activity implements OnClickListener {
 	
 	public static Typeface typeface;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         requestWindowFeature(Window.FEATURE_NO_TITLE); // Hide title
         getWindow().setFlags(0xFFFFFFFF,  LayoutParams.FLAG_FULLSCREEN | LayoutParams.FLAG_KEEP_SCREEN_ON);
     	
     	super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         typeface = Typeface.createFromAsset(getAssets(), "IMPACT.TTF");
         
         ImageButton startGameButton = (ImageButton) findViewById(R.id.start_game_button);
         ImageButton howToPlayButton = (ImageButton) findViewById(R.id.howToPlay_button);
         ImageButton achievementsButton = (ImageButton) findViewById(R.id.achievements_button);
         ImageButton highScoreButton = (ImageButton) findViewById(R.id.high_score_button);
         ImageButton creditsButton = (ImageButton) findViewById(R.id.credits_button);
         ImageButton quitButton = (ImageButton) findViewById(R.id.quit_game_button);
         
         startGameButton.setOnClickListener(this);
         howToPlayButton.setOnClickListener(this);
         achievementsButton.setOnClickListener(this);
         highScoreButton.setOnClickListener(this);
         creditsButton.setOnClickListener(this);
         quitButton.setOnClickListener(this);
         
 //        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
 //        SharedPreferences.Editor editor = preferences.edit();
 //        editor.putString("Name","Garlov");
 //        editor.commit();
         
         SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
         String name = preferences.getString("Name","");
         String id = preferences.getString("Id","");
         
        if(name.equalsIgnoreCase("") || id.equalsIgnoreCase(""))
         {
     		Intent GetUserIntent = new Intent(this, GetUser.class);
     		startActivity(GetUserIntent);
         }
 
         //DatabaseHelper gemswapperDatabase = new DatabaseHelper(this);
     }
     
     public void onClick(View v)
     {
     	switch(v.getId())
     	{
     	case R.id.start_game_button:
     		Intent startGameIntent = new Intent(this, Game.class);
     		startActivity(startGameIntent);
     		break;
     		
     	case R.id.howToPlay_button:
     		Intent howToIntent = new Intent(this, HowToActivity.class);
     		startActivity(howToIntent);
     		break;
     		
     	case R.id.achievements_button:
     		Intent achievementIntent = new Intent(this, Achievements.class);
     		startActivity(achievementIntent);
     		break;
     		
     	case R.id.high_score_button:
     		Intent highScoreIntent = new Intent(this, HighscoreActivity.class);
     		startActivity(highScoreIntent);
     		break;
     		
     	case R.id.credits_button:
     		Intent creditsIntent = new Intent(this, CreditsActivity.class);
     		startActivity(creditsIntent);
     		break;
     		
     	case R.id.quit_game_button:
     		finish();
     		break;
     	}
     }
 
     
     
     
     
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 }
