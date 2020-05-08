 package com.example.handsoncentralohio;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.MenuItem;
import android.widget.TextView;
 import android.support.v4.app.NavUtils;
 
 public class ProfileActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_profile);
 		// Show the Up button in the action bar.
 		setupActionBar();
		
		
//		for (int i = 0; i < 3; i++)
//		{
//				eventDate = (TextView) findViewById(R.id.imageView1);
//				eventDate.setText(list.get(i).getStartDate().toString());
//				eventName = (TextView) findViewById(R.id.event1name);
//				eventName.setText(list.get(i).getName().toString());
//				eventDesc = (TextView) findViewById(R.id.event1date);
//				eventDesc.setText(list.get(i).getDescr().toString());
//		}
			
 	}
 
 	/**
 	 * Set up the {@link android.app.ActionBar}.
 	 */
 	private void setupActionBar() {
 
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.profile, menu);
 		return true;
 	}
 
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
 
 }
