 package dk.au.cs.skatespots;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Location;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.Toast;
 
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
 import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
 import com.google.android.gms.location.LocationClient;
 import com.google.android.gms.location.LocationClient.OnAddGeofencesResultListener;
 import com.google.android.gms.location.LocationListener;
 import com.google.android.gms.maps.CameraUpdate;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.MapFragment;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 import com.google.gson.JsonPrimitive;
 import com.loopj.android.http.AsyncHttpResponseHandler;
 
 public class MainActivity extends Activity implements ConnectionCallbacks, 
 OnConnectionFailedListener,
 LocationListener,
 OnAddGeofencesResultListener 
 {
 	LocationClient locationClient;
	//Map'et kan ikke vre statisk, da det ellers ikke kan bibeholde markers ved rotation.
	private GoogleMap map;
 	private Location location;	
 	private JsonParser parser;
 	private JsonElement jsonElement;
 
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		setUpMapIfNeeded();
 		locationClient = new LocationClient(this, this, this);
 		locationClient.connect();
 	}
 
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	private void setUpMapIfNeeded() {
 		if (map == null) {
 			map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
 			// Check if we were successful in obtaining the map.
 			if (map != null) {
 				map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
 			}
 		}
 	}
 
 
 	@Override
 	public void onConnected(Bundle arg0) {
 		location = locationClient.getLastLocation();
 		
 		//Zooms in on our current position
 		LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
 		CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
 		map.animateCamera(cameraUpdate);
 
 		//Adds a marker of our current position to our map.
 		map.addMarker(new MarkerOptions()
 		.position(latLng)
		.title(LoginActivity.selectedUser));
 		
 		getAllLocations();
 		sendMyLocation();
 	}
 
 
 	public void sendMyLocation(){
 		String email = LoginActivity.selectedUser;
 
 		double latitude = location.getLatitude();
 		double longitude = location.getLongitude();
 
 		JsonObject obj = new JsonObject();
 		obj.add("key", new JsonPrimitive("ourKey")); // TODO create a secret key
 		obj.add("type", new JsonPrimitive(2));
 		obj.add("email", new JsonPrimitive(email));
 		obj.add("latitude", new JsonPrimitive(latitude));
 		obj.add("longitude", new JsonPrimitive(longitude));
 
		AsyncHttpResponseHandler responseHandler = new AsyncHttpResponseHandler();	
 		SkateSpotsHttpClient.post(getApplicationContext(), obj, responseHandler);
 	}
 
 	
 	//Takes a location and displayname as input, and puts a marker down for that.
 	public void addMarker(long latitude, long longitude, String displayname) {
 		map.addMarker(new MarkerOptions()
 		.position(new LatLng(latitude, longitude)) 	//Position of user
 		.title(displayname));
 	}
 
 	@Override
 	public void onLocationChanged(Location arg0) {
 		getAllLocations();
 		sendMyLocation();
 	}
 
 
 	public void getAllLocations(){
 	String email = LoginActivity.selectedUser;	
 		
 	JsonObject obj = new JsonObject();
 	obj.add("email", new JsonPrimitive(email));
 	obj.add("key", new JsonPrimitive("ourKey")); // TODO create a secret key
 	obj.add("type", new JsonPrimitive(3));
 
 	AsyncHttpResponseHandler responseHandler = new AsyncHttpResponseHandler() {
 		public void onSuccess(String response) {
 			parser = new JsonParser();	
 			jsonElement = parser.parse(response);
 			JsonArray jsonArray = jsonElement.getAsJsonArray();
 
 			for (int i = 0; i < jsonArray.size(); i++) {
 				
 				JsonElement jsonEle = jsonArray.get(i);
 				JsonObject jsonObject = jsonEle.getAsJsonObject(); 
 				JsonPrimitive jDisplayname = jsonObject.getAsJsonPrimitive("displayname");
 				JsonPrimitive jLatitude = jsonObject.getAsJsonPrimitive("latitude");
 				JsonPrimitive jLongitude = jsonObject.getAsJsonPrimitive("longitude");
 				
 				String displayname = jDisplayname.getAsString();
 				long latitude = jLatitude.getAsLong();
 				long longitude = jLongitude.getAsLong();
 				
 				addMarker(latitude, longitude, displayname);				
 			}			
 		}
 
 		public void onFailure(Throwable e, String response) {
 			// TODO 
 			Context context = getApplicationContext();
 			CharSequence text = e.toString();
 			int duration = Toast.LENGTH_LONG;
 
 			Toast toast = Toast.makeText(context, text, duration);
 			toast.show();
 			sendFailureMessage(e, response);
 		}
 	};
 	SkateSpotsHttpClient.post(getApplicationContext(), obj, responseHandler);	
 	
 	
 }
 	
 	
 	//METHOD FOR DISABLING BACK BUTTON
 	/*@Override
 	public void onBackPressed() {
 	}*/	
 
 	//METHOD FOR HANDLING MENU ITEMS
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.action_settings:
 			createUserActivity();
 			return true;
 		case R.id.menu_create:
 			//TODO Specify create in the menu
 			return true;
 		case R.id.menu_modify:
 			//TODO Specify modify in the menu
 			return true;
 		case R.id.menu_delete:
 			//TODO Specify delete in the menu
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	//FOR TESTING PURPOSES: SEE onOptionsItemSelected -> Action_settings
 	private void createUserActivity() {
 		Intent intent = new Intent(this, CreateUserActivity.class);
 		startActivity(intent);
 	}
	
	
	
 
 	//NOT CURRENTLY USED METHODS:
 	@Override
 	public void onDisconnected() {
 	}
 
 	@Override
 	public void onConnectionFailed(ConnectionResult arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 
 	@Override
 	public void onAddGeofencesResult(int arg0, String[] arg1) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 }
