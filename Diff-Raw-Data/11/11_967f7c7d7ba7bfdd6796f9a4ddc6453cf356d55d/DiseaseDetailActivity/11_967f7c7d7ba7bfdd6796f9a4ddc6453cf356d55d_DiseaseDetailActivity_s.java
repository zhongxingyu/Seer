 package com.example.epidemicapp;
 
 import java.util.LinkedList;
 import com.skp.Tmap.TMapMarkerItem;
 import com.skp.Tmap.TMapPoint;
 import com.skp.Tmap.TMapView;
 
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.app.Activity;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.util.Log;
 import android.view.Menu;
 import android.view.Window;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 public class DiseaseDetailActivity extends Activity {
 	private TMapView tmapview = null;
 	private LinkedList<TMapMarkerItem> markers = new LinkedList<TMapMarkerItem>();
 
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         Log.v("DEBUG", "created window features");
 		setContentView(R.layout.activity_disease_detail);
 		Log.v("DEBUG", "set content view");
 		
 		String diseaseName = getIntent().getExtras().getString("disease name");
 		String diseasePercent = getIntent().getExtras().getString("percentage");
		int pid = Integer.parseInt(getIntent().getExtras().getString("picture id"));
 		
 		ImageView iview = (ImageView)findViewById(R.id.diseaseSmallImg);
 		iview.setImageResource(pid);
 		TextView nameview = (TextView)findViewById(R.id.diseaseDetailTxt);
 		nameview.setText(diseaseName);
 		TextView perview = (TextView)findViewById(R.id.diseaseDetailPercent);
 		perview.setText(diseasePercent);
 
         RelativeLayout mapRelativeLayout = (RelativeLayout)findViewById(R.id.mapRelativeLayout);
         Log.v("DEBUG", "mapRelativeLayout");
         
         tmapview = new TMapView(this);
         Log.v("DEBUG", "created TMapView");
         
         this._getLocation();
         Log.v("DEBUG", "got gps location");
         
         tmapview.setLocationPoint(SplashScreenActivity.DB.myUser.getLongitude(), SplashScreenActivity.DB.myUser.getLatitude());
         Log.v("DEBUG", "set map location point");
         mapRelativeLayout.addView(tmapview);
         Log.v("DEBUG", "mapRelativeLayout addView");
         
         String selectQuery = "SELECT * FROM " + Consts.USER_TABLE;
         SQLiteDatabase db = SplashScreenActivity.DB.myDbHelper.getReadableDatabase();
         Cursor cursor = db.rawQuery(selectQuery, null);
         cursor.moveToFirst();
         while (cursor.isAfterLast() == false) {
         	//TODO: display on map
         	int howsick = Integer.parseInt(cursor.getString(cursor.getColumnIndex(Consts.USER_HOW_SICK)));
         	if (howsick > 0) {
 	        	double lat = Double.parseDouble(cursor.getString(cursor.getColumnIndex(Consts.USER_LAST_LAT)));
 	            double lon = Double.parseDouble(cursor.getString(cursor.getColumnIndex(Consts.USER_LAST_LON)));
 	            markers.addFirst(new TMapMarkerItem());
 	            markers.getFirst().setTMapPoint(new TMapPoint(lat, lon));
 	            markers.getFirst().setName(lat+","+"lon");
 	            markers.getFirst().setPosition(0.5f, 1.0f);
 	            markers.getFirst().setVisible(markers.getFirst().VISIBLE);
 	            tmapview.addMarkerItem(lat+","+lon, markers.getFirst());
         	}
         	cursor.moveToNext();
         }
         
         configureTMapView();
         
     }
 
     private void configureTMapView() {
     	new Thread() {
 			@Override
 			public void run() {
 				tmapview.setSKPMapApiKey("68fd1c9f-3a66-30bb-8934-990e8beb01f6"); //TODO: check if this is valid APIkey
 		        tmapview.setLanguage(tmapview.LANGUAGE_KOREAN);
 		        tmapview.setIconVisibility(true);
 		        tmapview.setZoomLevel(10);
 		        tmapview.setMapType(tmapview.MAPTYPE_STANDARD);
 		        tmapview.setCompassMode(true);
 		        tmapview.setTrackingMode(true);
 			}
 		}.start();
 	}
 
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 	
 	
 	private void _getLocation() {
 	    // Get the location manager
 	    LocationManager locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
 	    Log.v("DEBUG", "getLocationManager");
 	    Criteria criteria = new Criteria();
 	    Log.v("DEBUG", "new criteria");
 	    String bestProvider = locationManager.getBestProvider(criteria, false);
 	    Log.v("DEBUG", "get best provider");
 	    Location location = locationManager.getLastKnownLocation(bestProvider);
 	    Log.v("DEBUG", "get last known location");
 	    LocationListener loc_listener = new LocationListener() {
 
 	        public void onLocationChanged(Location l) {}
 
 	        public void onProviderEnabled(String p) {}
 
 	        public void onProviderDisabled(String p) {}
 
 	        public void onStatusChanged(String p, int status, Bundle extras) {}
 	    };
 	    locationManager.requestLocationUpdates(bestProvider, 0, 0, loc_listener);
 	    Log.v("DEBUG", "request location updates");
 	    location = locationManager.getLastKnownLocation(bestProvider);
 	    Log.v("DEBUG", "get last known location");
 	    try {
 	        SplashScreenActivity.DB.myUser.setLatitude(location.getLatitude());
 	        Log.v("DEBUG", "set latitude");
 	        SplashScreenActivity.DB.myUser.setLongitude(location.getLongitude());
 	        Log.v("DEBUG", "set longitude");
 	    } catch (NullPointerException e) {
 	    	SplashScreenActivity.DB.myUser.setLatitude(-1.0);
 	        SplashScreenActivity.DB.myUser.setLongitude(-1.0);
 	    }
 	}
 
 }
