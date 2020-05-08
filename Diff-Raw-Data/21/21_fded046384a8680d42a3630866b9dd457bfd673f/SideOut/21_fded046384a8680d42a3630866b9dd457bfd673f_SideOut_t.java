 package com.lsbapps.sideout;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.TextView;
 
 public class SideOut extends Activity implements OnClickListener {
 	private static final String TAG = "SideOut";
 	
 	private static final String PREF_US = "us";
 	private int usScore = 0;
 	
 	private static final String PREF_THEM = "them";
 	private int themScore = 0;
 	
 	private static final String PREF_RESUME = "resume";
 	private int RESUME = 0;
 	
 	private TextView displayScoreUs;
 	private TextView displayScoreThem;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	Log.d(TAG, "SideOut:onCreate()");
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         View scoreUsButton = findViewById(R.id.score_us_button); 
         scoreUsButton.setOnClickListener(this); 
         View scoreThemButton = findViewById(R.id.score_them_button); 
         scoreThemButton.setOnClickListener(this);
         
        getPreferences();
         updateScoreDisplays();
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	super.onCreateOptionsMenu(menu);
     	MenuInflater inflater = getMenuInflater();
     	inflater.inflate(R.menu.menu, menu);
     	return true;
     }
     
     public void onClick(View v) {
     	Log.d(TAG, "SideOut:onClick()");
     	switch (v.getId()) {
     	case R.id.score_us_button:
     		weScore();
     		break;
     	case R.id.score_them_button:
     		theyScore();
     		break;
     	}
     	updateScoreDisplays();
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	switch (item.getItemId()) {
     	case R.id.about_menu:
     		Intent i = new Intent(this, About.class);
     		startActivity(i);
     		return true;
     	case R.id.reset_menu:
     		startGame();
     		updateScoreDisplays();
     		return true;
     	case R.id.exit_menu:
     		finish();
     		return true;
     	}
     	return false;
     }
     
     @Override
     protected void onPause() {
     	Log.d(TAG, "SideOut:onPause()");
     	super.onPause();
     	
     	setPreferences();
     }
     
     @Override
     protected void onResume() {
     	Log.d(TAG, "SideOut:onResume()");
     	super.onResume();
     	
     	RESUME = getPreferences(MODE_PRIVATE).getInt(PREF_RESUME, RESUME);
     	
     	switch(RESUME) {
     	case 1:
     		getPreferences();
     		break;
     	case 0:
     		startGame();
     		break;
     	}
 		updateScoreDisplays();
     }
     
     private void startGame() {
     	Log.d(TAG, "SideOut:startGame()");
     	usScore = 0;
     	themScore = 0;
     	RESUME = 0;
     	setPreferences();
     }
     
     private void getPreferences() {
     	Log.d(TAG, "SideOut:getPreferences()");
     	usScore = getPreferences(MODE_PRIVATE).getInt(PREF_US, usScore);
 		themScore = getPreferences(MODE_PRIVATE).getInt(PREF_THEM, themScore);
     }
     
     private void setPreferences() {
     	Log.d(TAG, "SideOut:setPreferences()");
     	getPreferences(MODE_PRIVATE).edit().putInt(PREF_US, usScore).commit();
     	getPreferences(MODE_PRIVATE).edit().putInt(PREF_THEM, themScore).commit();
     	getPreferences(MODE_PRIVATE).edit().putInt(PREF_RESUME, RESUME).commit();
     }
     
     private void updateScoreDisplays() {
     	Log.d(TAG, "SideOut:updateScoreDisplays()");
     	displayScoreUs = (TextView) findViewById(R.id.us_score_label);
     	displayScoreUs.setText( new StringBuilder().append(usScore));
     	
     	displayScoreThem = (TextView) findViewById(R.id.them_score_label);
     	displayScoreThem.setText( new StringBuilder().append(themScore));
     }
  
     private void theyScore() {
     	themScore++;
     	setResume();
     }
     
     private void weScore() {
     	usScore++;
     	setResume();
     }
     
     private void setResume() {
     	RESUME = 1;
     }
 }
