 package csci498.tonnguye.lunchlist;
 
 import android.support.v4.app.Fragment;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.EditText;
 import android.widget.RadioGroup;
 import android.widget.TextView;
 import android.widget.Toast;
 
 
 public class DetailFragment extends Fragment {
 	EditText name = null;
 	EditText address = null;
 	EditText notes = null;
 	EditText feed = null;
 	RadioGroup types = null;
 	RestaurantHelper helper = null;
 	String restaurantId = null;
 	TextView location = null;
 	LocationManager locMgr = null;
 	double latitude = 0.0d;
 	double longitude = 0.0d;
 	private static final String ARG_REST_ID = "csci498.tonnguye.lunchlist.ARG_REST_ID";
 	
	public static DetailFragment newInstance(long id) {
		DetailFragment result = new DetailFragment();
		Bundle args = new Bundle();
		
		args.putString(ARG_REST_ID, String.valueOf(id));
		result.setArguments(args);
		
		return result;
	}
	
 	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);
 		
 		setHasOptionsMenu(true);
 	}
 	
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		
 		locMgr = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
 		
 		location = (TextView)getView().findViewById(R.id.location);
 		name = (EditText)getView().findViewById(R.id.name);
 		address = (EditText)getView().findViewById(R.id.addr);
 		notes = (EditText)getView().findViewById(R.id.notes);
 		feed = (EditText)getView().findViewById(R.id.feed);
 		types = (RadioGroup)getView().findViewById(R.id.types);
 		
 		Bundle args = getArguments();
 		
 		if (args != null) {
 			loadRestaurant(args.getString(ARG_REST_ID));
 		}
 	}
 	
 	public void loadRestaurant(String restaurantId) {
 		this.restaurantId = restaurantId;
 		
 		if (restaurantId != null) {
 			load();
 		}
 	}
 	
 	@Override 
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		return inflater.inflate(R.layout.detail_form, container, false);
 	}
 	
 	private void load() {
 		Cursor c = getHelper().getById(restaurantId);
 		
 		c.moveToFirst();
 		name.setText(getHelper().getName(c));
 		address.setText(getHelper().getAddress(c));
 		notes.setText(getHelper().getNotes(c));
 		feed.setText(getHelper().getFeed(c));
 		
 		if (getHelper().getType(c).equals("sit_down")) {
 			types.check(R.id.sit_down);
 		}
 		else if (getHelper().getType(c).equals("take_out")) {
 			types.check(R.id.take_out);
 		}
 		else {
 			types.check(R.id.delivery);
 		}
 		
 		latitude = getHelper().getLatitude(c);
 		longitude = getHelper().getLatitude(c);
 		
		location.setText(String.valueOf(latitude) + ", " + String.valueOf(longitude));
 		
 		c.close();
 	}
 	
 	private void save() {
 		if (name.getText().toString().length() > 0) {
 			String type = null;
 			
 			switch (types.getCheckedRadioButtonId()) {
 				case R.id.sit_down:
 					type="sit_down";
 					break;
 				case R.id.take_out:
 					type="take_out";
 					break;
 				case R.id.delivery:
 					type="delivery";
 					break;
 			}
 			
 			if (restaurantId == null) {
 				getHelper().insert(name.getText().toString(),
 						address.getText().toString(), type,
 						notes.getText().toString(),
 						feed.getText().toString());
 			}
 			else {
 				getHelper().update(restaurantId, name.getText().toString(),
 						address.getText().toString(), type,
 						notes.getText().toString(),
 						feed.getText().toString());
 			}
 		}
 	}
 	
 	@Override
 	public void onPause() {
 		save();
 		getHelper().close();
 		locMgr.removeUpdates(onLocationChange);
 		
 		super.onPause();
 	}
 	
 	private RestaurantHelper getHelper() {
 		if (helper == null) {
 			helper = new RestaurantHelper(getActivity());
 		}
 		
 		return getHelper();
 	}
 	
 	@Override
 	public void onSaveInstanceState(Bundle state) {
 		super.onSaveInstanceState(state);
 		//Can move getActivity() into it's own function!
 		state.putString("name", name.getText().toString());
 		state.putString("address", address.getText().toString());
 		state.putString("notes", notes.getText().toString());
 		state.putInt("types", types.getCheckedRadioButtonId());
 		
 	}
 	
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.details_option, menu);
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item.getItemId() == R.id.feed) {
 			if (isNetworkAvailable()) {
 				Intent i = new Intent(getActivity(), FeedActivity.class);
 				
 				i.putExtra(FeedActivity.FEED_URL, feed.getText().toString());
 				startActivity(i);
 			}
 			else {
 				Toast.makeText(getActivity(), "Sorry, the internet is not available", Toast.LENGTH_LONG).show();
 			}
 			return true;
 		}
 		else if (item.getItemId() == R.id.location) {
 			locMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, onLocationChange);
 			
 			return true;
 		}
 		else if (item.getItemId() == R.id.map) {
 			Intent i = new Intent(getActivity(), RestaurantMap.class);
 			
 			i.putExtra(RestaurantMap.EXTRA_LATITUDE, latitude);
 			i.putExtra(RestaurantMap.EXTRA_LONGITUDE, longitude);
 			i.putExtra(RestaurantMap.EXTRA_NAME, name.getText().toString());
 			
 			startActivity(i);
 			
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	LocationListener onLocationChange = new LocationListener() {
 		public void onLocationChanged(Location fix) {
 			getHelper().updateLocation(restaurantId, fix.getLatitude(), fix.getLongitude());
 			location.setText(String.valueOf(fix.getLatitude()) + ", " + String.valueOf(fix.getLongitude()));
 			locMgr.removeUpdates(onLocationChange);
 			
 			Toast.makeText(getActivity(), "Location saved", Toast.LENGTH_LONG).show();
 		}
 		
 		public void onProviderDisabled(String provider) {
 			//required for interface, not used
 		}
 		
 		public void onProviderEnabled(String provider) {
 			//required for interface, not used
 		}
 		
 		public void onStatusChanged(String provider, int status, Bundle extras) {
 			//required for interface, not used
 		}
 	};
 	
 	@Override
 	public void onPrepareOptionsMenu(Menu menu) {
 		if (restaurantId == null) {
 			menu.findItem(R.id.location).setEnabled(false);
 			menu.findItem(R.id.map).setEnabled(false);
 		}
 	}
 	
 	private boolean isNetworkAvailable() {
 		ConnectivityManager cm = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo info = cm.getActiveNetworkInfo();
 		
 		return(info!=null);
 	}
 			
 }
