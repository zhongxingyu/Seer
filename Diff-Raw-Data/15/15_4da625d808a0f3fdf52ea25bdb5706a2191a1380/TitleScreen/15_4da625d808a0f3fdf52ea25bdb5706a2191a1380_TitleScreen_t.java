 package edu.psu.ktwok.pocketplayer;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 
 public class TitleScreen extends Activity {
 
 	Intent i;
 	private static final int SETTINGS_ACTIVITY_REQUEST_CODE = 200;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_title_screen);
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.title_screen, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch(item.getItemId()) {
 			case R.id.tab1:
 				AboutDialogFrag newFrag = new AboutDialogFrag();
 				newFrag.show(getFragmentManager(), "sdfTest");
 				return true;
 			case R.id.menu_settings:
 				i = new Intent(this, SettingsActivity.class);
 				startActivityForResult(i, SETTINGS_ACTIVITY_REQUEST_CODE);
 				return true;
 				
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 	}
 	@Override
 	public void onBackPressed() {
 		i = new Intent(this, SplashScreen.class);
 		startActivity(i);
 		finish();
 		
 	}
 	
 	public void goToLogin(View v) {
 		i = new Intent(this, LoginActivity.class);
 		startActivity(i);	
		
 	}
 	
 	public void exitApp(View v) {
		TitleScreen.this.finish();
 	}
 
 }
