 package csci498.gnanda.lunchList;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.EditText;
 import android.widget.RadioGroup;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class DetailForm extends Activity {
 	
 	private EditText name;
 	private EditText address;
 	private EditText notes;
 	private EditText feed;
 	private RadioGroup types;
 	private RestaurantHelper helper;
 	private String restaurantId;		
 	private TextView location;
 	private double latitude = 0.0d;
 	private double longitude = 0.0d;
 	
 	private LocationManager locMgr;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.detail_form);
		helper=new RestaurantHelper(this);
 		getWidgetsFromXML();
 		locMgr = (LocationManager) getSystemService(LOCATION_SERVICE);
 		restaurantId = getIntent().getStringExtra(LunchList.ID_EXTRA);
 		if (restaurantId != null) {
			load();
 		}
 	}
 			
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		helper.close();
 	}
 
 	@Override
 	protected void onRestoreInstanceState(Bundle savedInstanceState) {
 		super.onRestoreInstanceState(savedInstanceState);
 		
 		name.setText(savedInstanceState.getString("name"));
 		address.setText(savedInstanceState.getString("address"));
 		notes.setText(savedInstanceState.getString("notes"));
 		types.check(savedInstanceState.getInt("type"));
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		
 		outState.putString("name", name.getText().toString());
 		outState.putString("address", address.getText().toString());
 		outState.putString("notes", notes.getText().toString());
 		outState.putInt("type", types.getCheckedRadioButtonId());		
 	}
 
 	@Override
 	public void onPause() {
 		save();
 		locMgr.removeUpdates(onLocationChange);
 		super.onPause();
 	}
 	
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		if (restaurantId == null) {
 			menu.findItem(R.id.location).setEnabled(false);
 			menu.findItem(R.id.map).setEnabled(false);
 		}
 		
 		return super.onPrepareOptionsMenu(menu);
 	}
 	
 	private void getWidgetsFromXML() {
 		name = (EditText) findViewById(R.id.name);
 		address = (EditText) findViewById(R.id.addr);
 		types = (RadioGroup) findViewById(R.id.types);
 		notes = (EditText) findViewById(R.id.notes);
 		feed = (EditText) findViewById(R.id.feed);
 		location = (TextView) findViewById(R.id.location);
 	}
 	
 	private void load() {
 		Cursor c = helper.getById(restaurantId);
 		
 		c.moveToFirst();
 		name.setText(helper.getName(c));
 		address.setText(helper.getAddress(c));
 		notes.setText(helper.getNotes(c));
 		feed.setText(helper.getFeed(c));
 		
 		if (helper.getType(c).equals("sit_down")) {
 			types.check(R.id.sit_down);
 		}
 		else if (helper.getType(c).equals("take_out")) {
 			types.check(R.id.take_out);
 		}
 		else {
 			types.check(R.id.delivery);
 		}
 		
		latitude = Double.valueOf(helper.getLatitude(c));
		longitude = Double.valueOf(helper.getLongitude(c));
 		
 		location.setText(getLocationOutput(helper.getLatitude(c), helper.getLongitude(c)));
 		c.close();
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		new MenuInflater(this).inflate(R.menu.details_options, menu);		
 		return super.onCreateOptionsMenu(menu);
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item.getItemId() == R.id.feed) {
 			if(isNetworkAvailable()) {
 				Intent i = new Intent(this, FeedActivity.class);
 				i.putExtra(FeedActivity.FEED_URL, feed.getText().toString());
 				startActivity(i);
 			}
 			else {
 				Toast.makeText(this, "Sorry, the Internet is not available", Toast.LENGTH_LONG).show();
 			}
 			return true;
 		}
 		else if (item.getItemId() == R.id.location) {
 			locMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, onLocationChange);
 			return true;
 		}
 		else if (item.getItemId() == R.id.map) {
 			Intent i = new Intent(this, RestaurantMap.class);
 			
 			i.putExtra(RestaurantMap.EXTRA_LATITUDE, latitude);
 			i.putExtra(RestaurantMap.EXTRA_LONGITUDE, longitude);
 			i.putExtra(RestaurantMap.EXTRA_NAME, name.getText().toString());
 			
 			startActivity(i);
 			return true;
 		}
 		
 		return super.onOptionsItemSelected(item);
 	}
 	
 	private LocationListener onLocationChange = new LocationListener() {
 		
 		@Override
 		public void onLocationChanged(Location fix) {
 			helper.updateLocation(restaurantId, fix.getLatitude(), fix.getLongitude());
 			location.setText(getLocationOutput(fix.getLatitude(), fix.getLongitude()));
 			locMgr.removeUpdates(onLocationChange);
 			Toast.makeText(DetailForm.this, "Location saved", Toast.LENGTH_LONG).show();
 		}
 		
 		@Override
 		public void onStatusChanged(String provider, int status, Bundle extras) {
 			// Not used		
 		}
 		
 		@Override
 		public void onProviderEnabled(String provider) {
 			// Not used				
 		}
 		
 		@Override
 		public void onProviderDisabled(String provider) {
 			// Not used			
 		}		
 
 	};
 	
 	private String getLocationOutput(Object latitude, Object longitude) {
 		return String.valueOf(latitude) + ", " + String.valueOf(longitude);
 	}
 	
 	private boolean isNetworkAvailable() {
 		ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
 		NetworkInfo info = cm.getActiveNetworkInfo();
 		return info != null;
 	}
 
 	private void save() {
 		String type = null;
 
 		switch (types.getCheckedRadioButtonId()) {
 		case R.id.sit_down:
 			type = "sit_down";
 			break;
 		case R.id.take_out:
 			type = "take_out";
 			break;
 		case R.id.delivery:
 			type = "delivery";
 			break;
 		}
 
 		if (restaurantId == null) {
 			helper.insert(name.getText().toString(), address.getText().toString(), type, notes.getText().toString(), feed.getText().toString());
 		}
 		else {
 			helper.update(restaurantId, name.getText().toString(), address.getText().toString(), type, notes.getText().toString(), feed.getText().toString());
 		}
 
 		finish();
 	}
 	
 }
