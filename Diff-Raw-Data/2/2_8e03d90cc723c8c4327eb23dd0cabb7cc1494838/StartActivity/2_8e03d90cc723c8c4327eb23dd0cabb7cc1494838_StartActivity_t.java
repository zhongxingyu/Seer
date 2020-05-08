 package org.ubicompforall.CityExplorer.gui;
 
 import java.util.ArrayList;
 
 import org.ubicompforall.CityExplorer.data.DBFactory;
 import org.ubicompforall.CityExplorer.data.DatabaseInterface;
 import org.ubicompforall.CityExplorer.data.IntentPassable;
 import org.ubicompforall.CityExplorer.data.Poi;
 import org.ubicompforall.CityExplorer.map.MapsActivity;
 
 import org.ubicompforall.CityExplorer.R;
 
 import android.app.Activity;
 import android.widget.Button;
 import android.widget.Toast;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 
 public class StartActivity extends Activity implements OnClickListener, LocationListener{
 
 	/**
 	 * The buttons in this activity.
 	 */
 	private Button buttonPlan, buttonExplore, buttonImport;
 	
 	/**
 	 * The user's current location.
 	 */
 	private Location userLocation;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.startlayout);
 
 		buttonPlan	 = (Button) findViewById(R.id.startButtonPlan);
 		buttonPlan.setOnClickListener(this);
 		buttonExplore = (Button) findViewById(R.id.startButtonExplore);
 		buttonExplore.setOnClickListener(this);
 		buttonImport = (Button) findViewById(R.id.startButtonImport);
 		buttonImport.setOnClickListener(this);
 		
 		initGPS();
		//startActivity(new Intent(StartActivity.this, ImportActivity.class)); 
 	}//onCreate
 
 	@Override
 	public void onClick(View v) {
 		if (v == buttonPlan){ 
 			startActivity(new Intent(StartActivity.this, PlanActivity.class)); 
 
 		}else if (v == buttonImport){ 
 			startActivity(new Intent(StartActivity.this, ImportActivity.class)); 
 
 		}else if (v == buttonExplore){
 			if(userLocation != null){
 				Intent showInMap = new Intent(StartActivity.this, MapsActivity.class);
 
 				DatabaseInterface db = DBFactory.getInstance(this);
 				ArrayList<Poi> poiList = db.getAllPois();
 				ArrayList<Poi> poiListNearBy = new ArrayList<Poi>();
 
 	//			ExportImport.send(this, poiList);
 	//			startActivity(new Intent(StartActivity.this, ExportImport.class));
 				for (Poi p : poiList) {
 					double dlon = p.getGeoPoint().getLongitudeE6()/1E6;
 					double dlat = p.getGeoPoint().getLatitudeE6()/1E6;
 					
 					Location dest = new Location("dest");
 					dest.setLatitude(dlat);
 					dest.setLongitude(dlon);
 					
 					if ( userLocation.distanceTo(dest) <= 5000 ){
 						poiListNearBy.add(p);
 					}
 				}//for POIs
 				if(poiListNearBy.size()>0){
 					showInMap.putParcelableArrayListExtra(IntentPassable.POILIST, poiListNearBy);
 					startActivity(showInMap); 
 				}//if POIsNearBy
 			}else{
 				Toast.makeText(this, R.string.map_gps_disabled_toast, Toast.LENGTH_LONG).show();
 				Log.d("CityExplorer", " did you forget to activate GPS??! ");
 			}//if userLocation, else improvise!
 
 		}//if v== button-Plan|Explore|Import 
 	}//onClick
 
 	/**
 	 * Initializes the GPS on the device.
 	 */
 	void initGPS()
 	{
 		// Acquire a reference to the system Location Manager
 		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
 
 		Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);//TODO: change to gps
 		onLocationChanged(lastKnownLocation);
 
 		// Register the listener with the Location Manager to receive location updates
 		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
 	}
 	
 	@Override
 	public void onLocationChanged(Location location) {
 		this.userLocation = location;
 	}
 
 	@Override
 	public void onProviderDisabled(String provider) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void onProviderEnabled(String provider) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		// TODO Auto-generated method stub
 	}
 }
