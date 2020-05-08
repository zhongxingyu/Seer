 package se.lanzen.LunchToday;
 
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.json.JSONArray;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Color;
 import android.graphics.drawable.Drawable;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 
 
 public class ResturantMap extends MapActivity {
 	private MapView mMapView; 
 	private MapController mMapController;
 	private LocationManager mLocationManager;
 	private boolean mStateShowMeOnMap = false;
 	List<Overlay> mMapOverlays;
 	private List<GeoPoint> mPolygon = new ArrayList<GeoPoint>();
 	private String mResturantName = ""; 
 	private static final int ACTIVITY_SEARCH = 1;
 	public static final String COMPANY = "COMPANY";
 	private MyLocationOverlay mMyLocationOverlay = null;
 	
 	
 //	private final LocationListener mLocationListener=new LocationListener() { 
 //		public void onLocationChanged(Location location) { 
 //			updateWithNewLocation(location); 
 //		}
 //		public void onProviderDisabled(String provider) { 
 //			updateWithNewLocation(null); 
 //		} 
 //		
 //		public void onProviderEnabled(String provider) {} 
 //		
 //		public void onStatusChanged(String provider,int status, Bundle extras) {} 
 //	};
 	//private boolean mGPSavailable = false;
 	private Menu mMenu = null;
 	private GeoPoint mCenterOfPolygon;
 	
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.resturant_map_view);
         mMapView = (MapView)findViewById(R.id.mapview);
         mMapController=mMapView.getController();
         mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
         //mGPSavailable  = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
         readPolygon();
         initMap();
         drawPolygon();
     }
 
 	private void readPolygon() {
 		String p = getIntent().getStringExtra((String)getResources().getText(R.string.map_polygon));
 		if(p == null) { 
 			return;
 		}
 		mResturantName = getIntent().getStringExtra((String)getResources().getText(R.string.map_resturant_name));
 		//Log.i("readPolygon",p);
 		try {
 			JSONArray polygon = new JSONArray(p);
 			for(int i = 0; i < polygon.length(); i++) {
 				JSONArray point = polygon.getJSONArray(i);
 				mPolygon.add(new GeoPoint(point.getInt(0), point.getInt(1)));
 				//Log.i("readPolygon","GeoPoint="+new GeoPoint(point.getInt(0), point.getInt(1)));
 			}
 			
 		} catch (Exception e) {
 			Log.e("readPolygon()",e.toString());
 			mPolygon.clear();
 		}
 		
 	}
 
 	private void findCenterOfPolygon() {
 		if(mPolygon.size() <= 0) {
 			mCenterOfPolygon = null;
 			return;
 		}
 		long latitude = 0;
 		long longitude = 0;
 		for(GeoPoint gp : mPolygon) {
 			latitude  += gp.getLatitudeE6();
 			longitude += gp.getLongitudeE6();
 		}
 		mCenterOfPolygon = new GeoPoint((int)(latitude / mPolygon.size()), (int)(longitude / mPolygon.size()));
 	}
 
 	private void drawPolygon() {
     	mMapOverlays = mMapView.getOverlays();
     	mMapOverlays.clear();
     	// Add MyLocationOverlay
 		mMyLocationOverlay = new MyLocationOverlay(this, mMapView);
 		mMyLocationOverlay.disableMyLocation();
 		mMapOverlays.add(mMyLocationOverlay);
 		//Log.i("drawPolygon","ska lgga till oi");
 
 		findCenterOfPolygon();
 		if(mCenterOfPolygon == null) {
 			Log.e("drawPolygon","mCenterOfPolygon = null");
 			return;
 		}
 		Drawable drawable = this.getResources().getDrawable(R.drawable.restaurant);
 		PlacemarkOverlay itemizedOverlay = new PlacemarkOverlay(drawable, this);
 		itemizedOverlay.setPolygon(mPolygon);
 		itemizedOverlay.setText(mResturantName);
 		itemizedOverlay.setPolygonColor(Color.RED);
 		itemizedOverlay.setCenter(mCenterOfPolygon);
 		OverlayItem oi = new OverlayItem(mCenterOfPolygon, mResturantName, "");
 		itemizedOverlay.addOverlay(oi);
 		mMapOverlays.add(itemizedOverlay);
 		mMapView.invalidate();
 		mMapController.setCenter(mCenterOfPolygon);
 		
 	}
 
 	private void initMap() {
         mMapController.setZoom(17); 
         mMapView.setSatellite(false); 
         mMapView.setStreetView(true); 
         mMapView.setBuiltInZoomControls(true); 
          
         Location location= mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
         //updateWithNewLocation(location); 
         //mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,2000,10, mLocationListener);
 	}
 
 //	private void updateWithNewLocation(Location location) {
 //		if(location!=null) { 
 //			//Update the map location. 
 //			GeoPoint point = locationToGeoPoint(location); 
 //			setCenter(point); 
 //		} 
 //	}
 
 	private void setCenter(GeoPoint point) {
 		if(point != null) {
 //			mMapController.setCenter(point);
 			mMapController.animateTo(point);
 		}
 	}
 
 	private GeoPoint locationToGeoPoint(Location location) {
 		if(location == null) {
 			return null;
 		}
 		Double geoLat=location.getLatitude()*1E6; 
 		Double geoLng=location.getLongitude()*1E6; 
 		GeoPoint point = new GeoPoint(geoLat.intValue(), geoLng.intValue());
 		return point;
 	}
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	// Log.e("ShowResturantsActivity:onOptionsItemSelected","item = " + item.getItemId());
         // Handle item selection
         switch (item.getItemId()) {
         case R.id.show_me:
         	menuShowMe();
         	return true;
 //        case R.id.find:
 //        	// Log.e("Menu area","Preference for area");
 //        	Intent intent = new Intent(this, SearchActivity.class);
 //        	String geoData = mCompany.toJSONstring(); 
 //        	// Log.e("Menu area","Array of names = " + listOfResturantNames);
 //        	intent.putExtra(COMPANY, geoData);
 //        	startActivityForResult(intent, ACTIVITY_SEARCH);
 //            return true;
 //        case R.id.preference:
 //        	startActivityForResult(new Intent(this, AppPreference.class), SHOW_PREFERENCES);            
 //        	return true;
         default:
             return super.onOptionsItemSelected(item);
         }
     }
     
     private void menuShowMe() {
     	if(mStateShowMeOnMap) {
     		// Stop showing me on map
     		if(mMyLocationOverlay != null) {
     			mMyLocationOverlay.disableMyLocation();
     			setCenter(mCenterOfPolygon);
     	        mMapController.setZoom(17); 
     			Log.i("menuShowMe","disable");
     		}
     	} else {
     		// Start showing me on map
     		mMyLocationOverlay.enableMyLocation();
 			Log.i("menuShowMe","enable");
 			if(! zoomToShowMeAndResturant()) {
 				return;
 			}
     	}
     	mStateShowMeOnMap = ! mStateShowMeOnMap;
     	setShowMeMenuItem();
 	}
 
 	private boolean zoomToShowMeAndResturant() {
 		Location location= mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 		if(location == null) {
 			showDialogNoGPS();
 			return false;
 		}
 		GeoPoint me = locationToGeoPoint(location);
 		int minLatitude = me.getLatitudeE6();
 		int maxLatitude = me.getLatitudeE6();
 		int minLongitude = me.getLongitudeE6();
 		int maxLongitude = me.getLongitudeE6();
 
 		// Find the boundaries of the item set
 		for (GeoPoint item : mPolygon) { //item Contain list of Geopints
 			int lat = item.getLatitudeE6();
 			int lon = item.getLongitudeE6();
 	
 			maxLatitude = Math.max(lat, maxLatitude);
 			minLatitude = Math.min(lat, minLatitude);
 			maxLongitude = Math.max(lon, maxLongitude);
 			minLongitude = Math.min(lon, minLongitude);
 		}
 		mMapController.zoomToSpan(Math.abs(maxLatitude - minLatitude), 
 								  Math.abs(maxLongitude - minLongitude));
 		mMapController.animateTo(new GeoPoint( 
 				(maxLatitude + minLatitude)/2, 
 				(maxLongitude + minLongitude)/2 ));
 		return true;
 	}
 
 
 	private void showDialogNoGPS() {
 		AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
 		alt_bld.setMessage("Det krvs en GPS-signal fr att kunna visa din " +
 				"position. Du kanske har GPSen avstngd.");
 		alt_bld.setCancelable(false);
 		alt_bld.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				// Do nothing
 			}
 		});
 		AlertDialog alert = alt_bld.create();
 		// Title for AlertDialog
 		alert.setTitle("GPS-position saknas");
 		// Icon for AlertDialog
 //		alert.setIcon(R.drawable.ic_launcher);
 		alert.show();
 	}
 
 
 	private void setShowMeMenuItem() {
         MenuItem showMeOnMap = mMenu.findItem(R.id.show_me);
         if (mStateShowMeOnMap) {
             showMeOnMap.setTitle(getResources().getString(R.string.menu_hide_me));
         } else {
             showMeOnMap.setTitle(getResources().getString(R.string.menu_show_me));
         }
 	}
 
 	@Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         // If the request went well (OK) and the request was PICK_CONTACT_REQUEST
         if (resultCode == Activity.RESULT_OK && requestCode == ACTIVITY_SEARCH) {
 
         }
     }
 	
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.map_menu, menu);
 		mMenu  = menu;
 		return true;
 	}
 	
 	@Override
 	protected boolean isRouteDisplayed() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 }
