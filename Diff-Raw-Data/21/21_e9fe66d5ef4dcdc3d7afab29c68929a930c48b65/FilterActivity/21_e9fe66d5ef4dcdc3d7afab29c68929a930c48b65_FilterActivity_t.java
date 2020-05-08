 package edu.gatech.oad.rocket.findmythings;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.Spinner;
 import edu.gatech.oad.rocket.findmythings.list.ItemFilterConstraint;
 import edu.gatech.oad.rocket.findmythings.model.Category;
 import edu.gatech.oad.rocket.findmythings.util.EnumHelper;
 
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 /**
  * CS 2340 - FindMyStuff Android App
  * Activity that takes care of filtering items according to certain criteria
  *
  * @author TeamRocket
  * */
 public class FilterActivity extends Activity implements OnItemSelectedListener {
 
 	public static final int FILTER_REQUEST = 9231;
 	public static final String FILTER_RESPONSE = "filterOutput";
 
 	/**
 	 * Stores spinner information as int
 	 */
	public static ItemFilterConstraint constraint = new ItemFilterConstraint();
 
 	/**
 	 * creates new window with correct layout
 	 * @param savedInstanceState Initializing bundle
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_filter);
 
 		Spinner mStatus = (Spinner) findViewById(R.id.status_spinner);
 		mStatus.setOnItemSelectedListener(this);
 
 		Spinner mCat = (Spinner) findViewById(R.id.cat_spinner);
 		mCat.setOnItemSelectedListener(this);
 
 		Spinner mDate = (Spinner) findViewById(R.id.date_spinner);
 		mDate.setOnItemSelectedListener(this);
 	}
 
 	/**
 	 * creates the options menu
 	 * @param menu System action bar menu
 	 * @return true when done
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.filter, menu);
 		return true;
 	}
 
 	/**
 	 * deals with action when an options button is selected
 	 * @param item selected menu item
 	 * @return boolean
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    switch (item.getItemId()) {
 	        case R.id.filter_ok:
 				Intent output = new Intent();
 				output.putExtra(FILTER_RESPONSE, constraint);
 				setResult(RESULT_OK, output);
				finish();
 				return true;
 	        case R.id.filter_cancel:
 				setResult(RESULT_CANCELED);
 				finish();
 			return true;
 	    }
 	    return super.onOptionsItemSelected(item);
 	}
 
 	/**
 	 * deals with action when an item is selected
 	 * @param parent The layout XML spinner causing this result
 	 * @param view The view that actually represents the spinner
 	 * @param pos The selected value
 	 * @param id row ID of selected item
 	 */
 	@Override
 	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
 	switch(parent.getId()) {
 		case R.id.status_spinner:
 			switch (pos) {
 				case 1:
 					constraint.setOpen(true);
 					break;
 				case 2:
 					constraint.setOpen(false);
 					break;
 				default:
 					constraint.setOpen(null);
 					break;
 			}
 			break;
 		case R.id.cat_spinner: //category
 			if (pos == 0) {
 				constraint.setCategory(null);
 			} else {
 				constraint.setCategory(EnumHelper.forInt(pos-1, Category.class));
 			}
 			break;
 		case R.id.date_spinner: //date
 			if (pos == 0) {
 				constraint.setDateAfter(null);
 			} else {
 				Calendar today = new GregorianCalendar(); //get current date
 				Calendar date2 = (Calendar)today.clone(); //will be change to yesterday, 14dayago or 30daysago
 				switch(pos) {
 					case 1:
 						date2.add(Calendar.DATE, -2);
 						break;
 					case 2:
 						date2.add(Calendar.DATE, -15);
 						break;
 					case 3:
 						date2.add(Calendar.DATE, -31);
 						break;
 				}
 
 				constraint.setDateAfter(date2.getTime());
 			}
 			break;
 		}
 	}
 
 	/**
 	 * deals with what to do when nothing is selected
 	 * @param arg0 The layout XML spinner causing this result
 	 */
 	@Override
 	public void onNothingSelected(AdapterView<?> arg0) {}
 
 	/**
 	 * Called to pop the login window from the navigation stack
 	 */
 	@Override
 	public void finish() {
 		super.finish();
 		overridePendingTransition(R.anim.hold, R.anim.slide_down_modal);
 	}
 
 }
