 package edu.ncue.test.jls;
 
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.Bundle;
 import java.util.List;
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import com.google.android.maps.*;
 
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.*;
 import android.location.LocationListener;
 
public class JLSTutorialActivity extends MapActivity{
     /** Called when the activity is first created. */
 	private static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATE = 1;	//meter
 	private static final long MINIMUM_TIME_BETWEEN_UPDATE = 1000; 	//milesecond
 	
 	protected LocationManager locator;	
 	protected Button gpsButton;
 	protected MapView mv;
 	protected MapController mc;
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);	//]wlayout
         
        gpsButton = (Button) findViewById(R.id.retrieve_Location_Button);	//ͦbutton&View
         locator = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
         mv = (MapView) findViewById(R.id.mapview);
         mc = mv.getController();
         mv.setBuiltInZoomControls(true);
         
         locator.requestLocationUpdates(
         		LocationManager.GPS_PROVIDER, 
         		MINIMUM_DISTANCE_CHANGE_FOR_UPDATE,
         		MINIMUM_TIME_BETWEEN_UPDATE, 
         		new MyLocationListener());
         
         gpsButton.setOnClickListener(new OnClickListener(){
 
 			public void onClick(View v) {
 	        		showCurrentLocation();
 	        		//Toast.makeText(JLSTutorialActivity.this, "CLICKED", Toast.LENGTH_SHORT).show();
 			}
         
         });
         
         List<Overlay> mapOverlays = mv.getOverlays();
         Drawable drawable = this.getResources().getDrawable(R.drawable.ic_launcher);
         HelloItemizedOverlay itemizedOverlay = new HelloItemizedOverlay(drawable,this);
         GeoPoint point = new GeoPoint(30443769,-91158458);
         OverlayItem overlayitem = new OverlayItem(point, "Laisses Rouler!","I'm in Louisiana");
         
         GeoPoint p2 = new GeoPoint(17385812, 78480667);
         OverlayItem overlayitem2 = new OverlayItem(p2, "Namashkaar!","Im in Hyderabad");
         
         itemizedOverlay.addOverlay(overlayitem);
         itemizedOverlay.addOverlay(overlayitem2);
         
         mapOverlays.add(itemizedOverlay);
     }
     
     @Override
     protected boolean isRouteDisplayed()
     {
     	return false;
     }
     
     protected void showCurrentLocation(){
     	Location location = locator.getLastKnownLocation(LocationManager.GPS_PROVIDER);
     	
     	if (location != null)
     	{
     		String message = String.format("CurrentLocation \n Longitude: %1$s \n Latitude: %2$s ", location.getLongitude(),location.getLatitude());
     		Toast.makeText(JLSTutorialActivity.this, message, Toast.LENGTH_LONG).show();
     		//retrieve where device is and zoom to that position
     		GeoPoint cp = new GeoPoint((int)(location.getLatitude()*1E6),(int)(location.getLongitude()*1E6));
     		mc.animateTo(cp);
     		mc.setZoom(16);
     		mv.invalidate();
     	}
     }
 
     //inner Class
     private class MyLocationListener implements LocationListener{
 
     	public void onLocationChanged(Location location) {
     		String message = String.format("NEW LOCATION Dectected! \n %1$s \n %2$s", location.getLongitude(),location.getLatitude());
     		Toast.makeText(JLSTutorialActivity.this, message, Toast.LENGTH_LONG).show();
 
     	}
 
     	public void onProviderDisabled(String provider) {
     		Toast.makeText(JLSTutorialActivity.this,"Provider disabled by the user. GPS turned off",Toast.LENGTH_LONG).show();
 		
     	}
 
     	public void onProviderEnabled(String provider) {
     		Toast.makeText(JLSTutorialActivity.this,"Provider enabled by the user. GPS turned ON",Toast.LENGTH_LONG).show();
 		
     	}
 
     	public void onStatusChanged(String provider, int i, Bundle extras) {
     		Toast.makeText(JLSTutorialActivity.this, "Provider StatusChanged", Toast.LENGTH_LONG).show();
 		
     	}
 	
     }
 }
