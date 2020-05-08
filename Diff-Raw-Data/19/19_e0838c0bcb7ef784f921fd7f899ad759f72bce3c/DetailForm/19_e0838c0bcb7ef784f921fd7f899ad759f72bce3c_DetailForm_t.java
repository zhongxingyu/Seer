 package edu.mines.csci498.bwisdom.lunchlist;
 
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
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.RadioGroup;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.util.Log;
 
 public class DetailForm extends Activity {
 	EditText name = null;
 	EditText address = null;
 	EditText notes = null;
 	EditText feed = null;
 	TextView location = null;
 	RadioGroup types = null;
 	RestaurantHelper helper = null; 	
 	String restaurantId = null; 
 	
 	LocationManager locMgr = null;
 	
 	LocationListener onLocationChange = new LocationListener() {
 		public void onLocationChanged(Location fix) {
 			helper.updateLocation(restaurantId, fix.getLatitude(), fix.getLongitude());
			Log.d("DETAILFORM", "Location updated in Database!");
			location.setText(String.valueOf(fix.getLatitude()) + ", " + String.valueOf(fix.getLongitude()));
			Log.d("DETAILFORM", "Location textview updated!");
 			locMgr.removeUpdates(onLocationChange);
 			
 			Toast.makeText(DetailForm.this, "Location Saved", Toast.LENGTH_LONG).show();
 			
 		}
 
 		@Override
 		public void onProviderDisabled(String provider) {
 			// Required method for interface (NOT USED)
 			
 		}
 
 		@Override
 		public void onProviderEnabled(String provider) {
 			// Required method for interface (NOT USED)
 			
 		}
 
 		@Override
 		public void onStatusChanged(String provider, int status, Bundle extras) {
 			// Required method for interface (NOT USED)
 			
 		}
 		
 	};
 	
 	@Override 
 	public void onCreate(Bundle savedInstanceState) {
 		Log.d("DETAILFORM", "activity created!");
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.detail_form);
 		
		locMgr = (LocationManager) getSystemService(LOCATION_SERVICE);
 		helper = new RestaurantHelper(this);
 		
 		name = (EditText) findViewById(R.id.name);
 		address = (EditText) findViewById(R.id.addr);
 		types = (RadioGroup) findViewById(R.id.types);
 		notes = (EditText) findViewById(R.id.notes);
 		feed = (EditText) findViewById(R.id.feed);
 		location = (TextView) findViewById(R.id.location);

 		
 		restaurantId = getIntent().getStringExtra(MainActivity.ID_EXTRA);
 		
 		if(restaurantId != null){
 			load();
 		}
 	}
 	
 	@Override
 	public void onSaveInstanceState(Bundle state) {
 		super.onSaveInstanceState(state);
 		
 		state.putString("name", name.getText().toString());
 		state.putString("address", address.getText().toString());
 		state.putInt("type", types.getCheckedRadioButtonId());
 		state.putString("notes", notes.getText().toString());
 	}
 	
 	@Override 
 	public void onRestoreInstanceState(Bundle state) {
 		super.onRestoreInstanceState(state);
 		
 		name.setText(state.getString("name"));
 		address.setText(state.getString("address"));
 		types.check(state.getInt("type"));
 		notes.setText(state.getString("notes"));
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		new MenuInflater(this).inflate(R.menu.details_option, menu);
 		Log.d("DetailForm","Menu INflated");
 		return super.onCreateOptionsMenu(menu);
 	}
 	
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		if(restaurantId == null ) {
 			menu.findItem(R.id.location).setEnabled(false);
 		}
 		
 		return super.onPrepareOptionsMenu(menu);
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		Log.d("DetailForm","MenuItem Selected");
 		if(item.getItemId() == R.id.menufeed) {
 			if( isNetworkAvailable() ) {
 				Intent i = new Intent(this, FeedActivity.class);
 				Log.d("DetailForm", "NetworkAvailalbe Starting intent");
 				i.putExtra(FeedActivity.FEED_URL, feed.getText().toString());
 				startActivity(i);
 			} else {
 				Toast.makeText(this, "Sorry, The Internets are not available", Toast.LENGTH_LONG).show();
 			}
 			
 			return true;
 		} else if(item.getItemId() == R.id.location) {
 			locMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, onLocationChange);
 			return true;
 		}
 		
 		return super.onOptionsItemSelected(item);
 	}
 	
     private boolean isNetworkAvailable() {
     	ConnectivityManager cm = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
     	
     	NetworkInfo info = cm.getActiveNetworkInfo();
     	
     	return (info != null);
     }
 	
 	public void save() {
 		if(name.getText().toString().length() > 0 ) {
 			String type = null;
 
 			switch (types.getCheckedRadioButtonId()) {
 			case R.id.sit_down:
 				type = "sit_down";
 				break;
 			case R.id.takeout:
 				type = "takeout";
 				break;
 			case R.id.delivery:
 				type = "delivery";
 				break;
 			}
 			
 			if(restaurantId == null ){
 				helper.insert(name.getText().toString(),
 							  address.getText().toString(),
 							  type, notes.getText().toString(),
 							  feed.getText().toString());
 			} else {
 				helper.update(restaurantId,name.getText().toString(),
 						  address.getText().toString(),
 						  type, notes.getText().toString(),
 						  feed.getText().toString());
 			}
 		}
 	}
 	
 	private void load() {
 		Cursor c = helper.getById(restaurantId);
 		
 		c.moveToFirst();
 		name.setText(helper.getName(c));
 		address.setText(helper.getAddress(c));
 		notes.setText(helper.getName(c));
 		feed.setText(helper.getFeed(c));
 		
 		if(helper.getType(c).equals("sit_down")){
 			types.check(R.id.sit_down);
 		}else if(helper.getType(c).equals("takeout")){
 			types.check(R.id.takeout);
 		}else{
 			types.check(R.id.delivery);
 		}
 
 		location.setText(String.valueOf(helper.getLatitude(c)) + ", " + String.valueOf(helper.getLongitude(c)));
 		
 		c.close();
 	}
 	
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		
 		helper.close();
 	}
 	
 	@Override
 	public void onPause() {
 		
 		save();
 		locMgr.removeUpdates(onLocationChange);
 		super.onPause();
 	}
 	
 }
