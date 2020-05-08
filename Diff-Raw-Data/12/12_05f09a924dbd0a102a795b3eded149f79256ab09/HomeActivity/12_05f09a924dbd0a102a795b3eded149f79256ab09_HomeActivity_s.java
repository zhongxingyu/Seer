 package com.example.myfirstapp;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 
 /**
  * HomeActivity is the activity that provides the user with the home page for the application.
  * This page is displayed after all login activities have been completed or bypassed. This page
  * also provides the means to get the the character management page, group management page, settings
  * page and the logout option.
  * @author Kevin Dong (kevinxd3)
  *
  */
 public class HomeActivity extends Activity {
 
 	/**
 	 * Displays the home page.
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_home);
 		// Show the Up button in the action bar.
 		//setupActionBar();
 	}
 
 	/**
 	 * Set up the {@link android.app.ActionBar}, if the API is available.
 	 */
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void setupActionBar() {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 		}
 	}
 
 	/**
 	 * Creates Options Menu
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.sqlite_test, menu);
 		return true;
 	}
 
 	/**
 	 * Sets up the Up button
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 
 	/**
 	 * Responds to the manage characters button click and goes to the manage
 	 * characters page.
 	 * @param view The current view
 	 */
 	public void gotoCharacters(View view) {
 		Intent intent = new Intent(this, CharactersActivity.class);
 		startActivity(intent);
 	}
 	
 	/**
 	 * Responds to the logout button click, logs the user out, and goes to the login page.
 	 * @param view The current view
 	 */
 	public void logout(View view) {
 		Intent intent = new Intent(this, LoginActivity.class);
 		SaveSharedPreference.setUserName(HomeActivity.this, "");
 		startActivity(intent);
 	}
 	
 	/**
 	 * Responds to the manage groups button click and goes to the manage
 	 * groups page. 
 	 * @param view The current view
 	 */
 	public void gotoGroups(View view) {
 		Intent intent = new Intent(this, GroupsActivity.class);
 		startActivity(intent);
 	}
 
 	/**
 	 * Responds to the settings button click and goes to the manage
 	 * groups page. (Settings currently unimplemented so directs to
 	 * HomeActivity instead)
 	 * @param view The current view
 	 */
 	public void goToSettings(View view) {
 		Intent intent = new Intent(this, HomeActivity.class);
 		startActivity(intent);

 	public void gotoTesting(View view) {
 		Intent intent = new Intent(this, SQLiteTestActivity.class);

 
 }
