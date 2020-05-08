 package edu.ua.moundville;
 
 import java.util.ArrayList;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import edu.ua.moundville.DBHandler.DBResult;
 
 public class ProximityList extends ListActivity implements DBResult, LocationListener {
 	
 	private static final String DBCASE = "1";
 	private static final long MIN_UPDATE_TIME = 5000;
 	private static final float MIN_UPDATE_DISTANCE = 10;
 	private static Location location = null;
 	protected ArrayList<NameValuePair> queryArgs = new ArrayList<NameValuePair>();
 	protected final ArrayList<String> listText = new ArrayList<String>();
 	protected final ArrayList<String> listImages = new ArrayList<String>();
 	protected final ArrayList<String> listLinks = new ArrayList<String>();
 	protected static DBHandler db = new DBHandler();
 	private static final String TAG = "ProximityList";
 	private LocationManager locationManager;
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    setTitle("Explore Nearby");
 	    
 	    locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
 	    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 	    if (location == null) {
 	    	location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 	    }
 	    if (location == null) {
 	    	// ToDo: error
 	    	return;
 	    }
 	    
 	    setupQuery();
 	    
 	    db.sendQuery(this, queryArgs);
 	    
 	    getListView().setOnItemClickListener(new OnItemClickListener() {
 	    	 
 	    	Intent launchActivity;
 	        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 	        	Log.d(TAG, "Clicked position: " + position + " listLinks size: " + String.valueOf(listLinks.size()));
	        	launchActivity = new Intent(view.getContext(), ArtifactArticle.class);
 	        	launchActivity.putExtra("artifact", listLinks.get(position));
 	        	startActivity(launchActivity);
 	        }
 	    });
 	    
 	    locationManager.requestLocationUpdates(
 	    		LocationManager.GPS_PROVIDER, MIN_UPDATE_TIME, MIN_UPDATE_DISTANCE, this);
 	}
 	
 	private void setupQuery() {
 	    queryArgs.clear();
 	    queryArgs.add(new BasicNameValuePair("case", DBCASE));
 	    queryArgs.add(new BasicNameValuePair("lat", String.valueOf(location.getLatitude())));
 	    queryArgs.add(new BasicNameValuePair("lon", String.valueOf(location.getLongitude())));
 	}
 	
 	public void onLocationChanged(Location newLocation) {
 		location = newLocation;
 		setupQuery();
 	}
 
 	public void onProviderDisabled(String provider) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void onProviderEnabled(String provider) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void receiveResult(JSONArray jArray) {
 		if (jArray == null) {
 			listText.add("I failed :(");
 		} else {
 			Log.d(TAG, jArray.toString());
 
 			for (int i=0; i<jArray.length(); i++) {
 				JSONObject obj = null;
 				try {
 					obj = (JSONObject) jArray.get(i);
 					listText.add(obj.getString("ak_Art_Title"));
 					listLinks.add(obj.getString("pk_Art_ArtID"));
 					listImages.add(obj.getString("Img_ImageThumb"));
 				} catch (JSONException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 
 		prepareList();
 	}
 	
 	private void prepareList() {
 	    setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listText));
 	}
 	protected void onPause() {
 		super.onPause();
 		locationManager.removeUpdates(this);
 	}
 	
 	protected void onResume() {
 		super.onResume();
 	    locationManager.requestLocationUpdates(
 	    		LocationManager.GPS_PROVIDER, MIN_UPDATE_TIME, MIN_UPDATE_DISTANCE, this);
 	}
 }
