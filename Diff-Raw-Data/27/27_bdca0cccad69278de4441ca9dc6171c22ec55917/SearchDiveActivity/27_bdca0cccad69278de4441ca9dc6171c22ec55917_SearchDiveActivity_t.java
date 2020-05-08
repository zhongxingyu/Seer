 package org.subsurface;
 
 import java.util.Calendar;
 
 import org.subsurface.ui.DatePickerButton;
 import org.subsurface.ui.DiveArrayAdapter;
 import org.subsurface.ui.TimePickerButton;
 
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 
 /**
  * Activity for dive search.
  * @author Aurelien PRALONG
  *
  */
 public class SearchDiveActivity extends HomeActivity {
 
 	private static final long ONE_DAY_MS = 24 * 60 * 60 * 1000;
 
 	private View dateFilterLayout = null;
 	private String currentName = null;
 	private long startDate;
 	private int startTime;
 	private long endDate;
 	private int endTime;
 
 	private void updateSearch() {
 		if (dateFilterLayout.getVisibility() == View.VISIBLE) {
 			((DiveArrayAdapter) getListAdapter()).filter(currentName, startDate + (startTime * 60000), endDate + (endTime * 60000));
 		} else {
 			((DiveArrayAdapter) getListAdapter()).filter(currentName, Long.MIN_VALUE, Long.MAX_VALUE);
 		}
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         getSupportActionBar().setDisplayHomeAsUpEnabled(true);
 
         // Retrieve location service
     	setContentView(R.layout.dive_list_search);
     	DiveArrayAdapter adapter = new DiveArrayAdapter(this);
     	setListAdapter(adapter);
     	adapter.setListener(this);
     	getListView().setItemsCanFocus(false);
 
     	// Date filter initialization
     	dateFilterLayout = findViewById(R.id.dateFilterLayout);
     	dateFilterLayout.setVisibility(View.GONE);

    	// Initial times / dates initializations
     	Calendar cal = Calendar.getInstance();
    	cal.set(Calendar.SECOND, 0);
    	cal.set(Calendar.MILLISECOND, 0);
    	this.startTime = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
    	this.endTime = startTime;
    	cal.set(Calendar.HOUR_OF_DAY, 0);
    	cal.set(Calendar.MINUTE, 0);
    	this.startDate = cal.getTimeInMillis() - ONE_DAY_MS;
    	this.endDate = cal.getTimeInMillis();

    	// Start date / hour
     	DatePickerButton.initButton((Button) findViewById(R.id.buttonFromDate), startDate, new DatePickerButton.DateSetListener() {
 			@Override
 			public void onDateSet(Button button, long date) {
 				startDate = date;
 				updateSearch();
 			}
 		});
     	TimePickerButton.initButton((Button) findViewById(R.id.buttonFromHour), startTime, new TimePickerButton.TimeSetListener() {
 			
 			@Override
 			public void onTimeSet(Button button, int minutes) {
 				startTime = minutes;
 				updateSearch();
 			}
 		});

    	// End date / hour
     	DatePickerButton.initButton((Button) findViewById(R.id.buttonToDate), endDate, new DatePickerButton.DateSetListener() {
 			@Override
 			public void onDateSet(Button button, long date) {
				endDate = date;
 				updateSearch();
 			}
 		});
     	TimePickerButton.initButton((Button) findViewById(R.id.buttonToHour), endTime, new TimePickerButton.TimeSetListener() {
 			
 			@Override
 			public void onTimeSet(Button button, int minutes) {
 				endTime = minutes;
 				updateSearch();
 			}
 		});
 	}
 
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
 		getSupportMenuInflater().inflate(R.menu.dives_search, menu);
 		MenuItem item = menu.findItem(R.id.menu_search);
 		((EditText) item.getActionView()).addTextChangedListener(new TextWatcher() {
 			
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before, int count) {}
 			
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
 			
 			@Override
 			public void afterTextChanged(Editable s) {
 				currentName = s.toString();
 				updateSearch();
 			}
 		});
 		item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
 			
 			@Override
 			public boolean onMenuItemActionExpand(MenuItem item) {
 				updateSearch();
 				return true;
 			}
 			
 			@Override
 			public boolean onMenuItemActionCollapse(MenuItem item) {
 				finish();
 				return true;
 			}
 		});
 		item.expandActionView();
         return true;
     }
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item.getItemId() == android.R.id.home) {
 			finish();
 			return true;
 		} else if (item.getItemId() == R.id.menu_time) {
 			if (dateFilterLayout.getVisibility() == View.VISIBLE) {
 				dateFilterLayout.setVisibility(View.GONE);
 				updateSearch();
 			} else {
 				dateFilterLayout.setVisibility(View.VISIBLE);
 				updateSearch();
 			}
 		} else if (item.getItemId() == R.id.menu_search) {
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	public boolean onSearchRequested() {
 		return true;
 	}
 }
