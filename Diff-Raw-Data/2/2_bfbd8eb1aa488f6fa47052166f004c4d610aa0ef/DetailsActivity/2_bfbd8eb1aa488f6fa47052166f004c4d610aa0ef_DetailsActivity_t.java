 package com.HuskySoft.metrobike.ui;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 
 import com.HuskySoft.metrobike.R;
 
 /**
  * @author mengwan
  *
  */
 public class DetailsActivity extends Activity {
 
 	/**
 	 * 
 	 * onCreate function of DetailsActivity class
 	 * Display the details of metroBike search
 	 * 
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.setDetails();
 		ActionBar actionBar = this.getActionBar();
 		actionBar.setTitle("Details");
 		setContentView(R.layout.activity_details);
 	}
 	
 	/**
 	 * 
 	 * onCreate menu option of DetailsActivity
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_details, menu);
 		return true;
 	}
 
 	/**
 	 * 
 	 * @param view: the view of the button
 	 * onClick function of the go to Navigate button
 	 */
 	public void goToNavigate(View view) {
         // Do something in response to button
     	Intent intent = new Intent(this, NavigateActivity.class);
     	startActivity(intent);
     }
 	
 	/**
 	 * 
 	 * @param view: the view of the button
 	 * onClick function of the return to search page button
 	 */
 	public void goToSearchPage(View view) {
         // Do something in response to button
     	Intent intent = new Intent(this, SearchActivity.class);
     	startActivity(intent);
     }
 	
 	/**
 	 * 
 	 * @param view: the view of the button
 	 * onClick function for the return to result page button
 	 */
 	
 	public void goToResults(View view) {
 		// Do something in response to button
    	Intent intent = new Intent(this, ResultsActivity.class);
     	startActivity(intent);
 	}
 	
 	/**
 	 * 
 	 * helper function that sets up the details of a route
 	 */
 	private void setDetails() {
 		//sets up details
 	}
 }
