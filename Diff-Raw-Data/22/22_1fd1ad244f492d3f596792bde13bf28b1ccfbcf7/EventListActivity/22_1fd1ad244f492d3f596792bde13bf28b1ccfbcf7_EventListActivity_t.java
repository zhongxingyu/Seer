 package uk.ac.dur.duchess.ui.activity;
 
 import java.util.Calendar;
 import java.util.List;
 
 import uk.ac.dur.duchess.GlobalApplicationData;
 import uk.ac.dur.duchess.R;
 import uk.ac.dur.duchess.io.provider.DataProvider;
 import uk.ac.dur.duchess.model.EventLocation;
 import uk.ac.dur.duchess.ui.view.EventListView;
 import android.app.AlertDialog;
 import android.app.DatePickerDialog;
 import android.app.DatePickerDialog.OnDateSetListener;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.widget.DatePicker;
 
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 
 public class EventListActivity extends BaseActivity
 {
 	private ProgressDialog locationProgress;
 	
 	private EventListView listView;
 	
 	private LocationManager lm;
 	
 
 	private static final int REQUEST_DATEFRAME = 1;
 	private static final int REQUEST_CATEG0RY = 2;
 	private static final int DATE_DIALOG_ID = 1;
 	private static final int LOCATION_DIALOG_ID = 2;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 //		android.os.Debug.startMethodTracing("duchess");
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.event_list_layout);
 		
 		listView = (EventListView) findViewById(R.id.eventListView);
 		listView.loadAllEvents(this, null);
 		listView.setEmptyView(findViewById(R.id.eventListEmpty));
 	}
 
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		getSupportMenuInflater().inflate(R.menu.event_list_menu, menu);
 		return true;
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		switch (item.getItemId())
 		{
 		case R.id.submenuEventListByDate:
 		{
 			showDialog(DATE_DIALOG_ID);
 			return true;
 		}
 		case R.id.submenuEventListByDateRange:
 		{
 			Intent i = new Intent(this, DateFrameActivity.class);
 			startActivityForResult(i, REQUEST_DATEFRAME);
 			return true;
 		}
 		case R.id.submenuEventListByLocation:
 		{
 			showDialog(LOCATION_DIALOG_ID);
 			return true;
 		}
 		case R.id.submenuEventListAZ:
 			listView.sortAlphabetically();
 			return true;
 		case R.id.submenuEventListChronological:
 			listView.sortChronologically();
 			return true;
 		case R.id.submenuEventListRatings:
 			listView.sortByHighestReview();
 			return true;
 		case R.id.submenuEventListDistance:
 		{
 			locationProgress = ProgressDialog.show(this, "Sorting By Distance",
 					"Retrieving Location...");
 			lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 			LocationListener locationListener = new NearEventsListener();
 			lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
 			return true;
 		}
 		case R.id.menuCategoryBrowse:
 		{
 			Intent i = new Intent(this, CategoryGridActivity.class);
 			startActivityForResult(i, REQUEST_CATEG0RY);
 			return true;
 		}
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int responseCode, Intent data)
 	{
 		switch (requestCode)
 		{
 		case REQUEST_DATEFRAME:
 		{
 			if (responseCode == RESULT_OK)
 				listView.filterByDateRange(
 						data.getStringExtra("from_date"), data.getStringExtra("to_date"));
 			break;
 		}
 		case REQUEST_CATEG0RY:
 		{
 			if (responseCode == RESULT_OK)
 				listView.filterByCategory(data.getStringExtra("category_filter"));
 			break;
 		}
 
 		default: break;
 		}
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id)
 	{
 		switch (id)
 		{
 			case DATE_DIALOG_ID:
 			{
 				Calendar c = Calendar.getInstance();
 				int year = c.get(Calendar.YEAR);
 				int month = c.get(Calendar.MONTH);
 				int day = c.get(Calendar.DATE);
 	
 				return new DatePickerDialog(this, dateSetListener, year, month, day);
 			}
 			case LOCATION_DIALOG_ID:
 			{
 				GlobalApplicationData delegate = GlobalApplicationData.getInstance();
 				DataProvider dataPro = delegate.getDataProvider();
 				
 				List<EventLocation> locations = dataPro.getLocations(this);
 				final CharSequence[] items = new CharSequence[locations.size()];
 				for(int i = 0; i < items.length; i++) items[i] = locations.get(i).getAddress1();
 	
 				AlertDialog.Builder builder = new AlertDialog.Builder(this);
 				builder.setTitle("Pick a location");
 				builder.setItems(items, new DialogInterface.OnClickListener()
 				{
 				    public void onClick(DialogInterface dialog, int item)
 				    {
 				        listView.filterByLocation(items[item].toString());
 				    }
 				});
 				
 				return builder.create();
 			}
 		}
 		return null;
 	}
 
 	private OnDateSetListener dateSetListener = new OnDateSetListener()
 	{
 		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
 		{
 			String date = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
 			String nextDay = year + "-" + (monthOfYear + 1) + "-" + (dayOfMonth + 1);
 
 			listView.filterByDateRange(date, nextDay);
 		}
 	};
 
 	private class NearEventsListener implements LocationListener
 	{
 		private static final int WALKING_DISTANCE = 800;
 
 		@Override
 		public void onLocationChanged(final Location newLocation)
 		{
 			if (newLocation != null)
 			{
 				locationProgress.dismiss();
 
 				listView.sortByDistance(newLocation);
 				
 				lm.removeUpdates(this);
 			}
 		}
 
 		@Override
 		public void onProviderDisabled(String provider)
 		{
 		}
 
 		@Override
 		public void onProviderEnabled(String provider)
 		{
 		}
 
 		@Override
 		public void onStatusChanged(String provider, int status, Bundle extras)
 		{
 		}
 	}
 }
