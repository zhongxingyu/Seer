 package com.NFCGeo;
 
 /*NOTE: To be able to import the com.google.android libraries
  * you need to add this build path to the project:
  * C:\Users\Kaitlin\AppData\Local\Android\android-sdk\add-ons\addon-google_apis-google-8\libs\maps.jar
  * */
 import java.util.List;
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 
 import android.annotation.TargetApi;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 
 public class ViewMapActivity extends MapActivity implements LocationListener
 {    
     /* Called when the activity is first created. */
 	@TargetApi(9)
 	@Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_view_map);
         
         LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
         final boolean gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
         Location loc = null;
         
         MapView mapView = (MapView) findViewById(R.id.mapview);
         MapController mapController = mapView.getController();
         
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
     	builder.setPositiveButton("OK", null);
     	
         if (!gpsEnabled)
         {
         	builder.setMessage("GPS is not enabled.");
         	builder.show();
         }
         else
         {
         	lm.requestSingleUpdate(new Criteria(), this, null);
         	loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
         }
         GeoPoint p;
         if (loc != null)
         {
         	p = new GeoPoint((int)(1000000*loc.getLatitude()), (int)(1000000*loc.getLongitude()));
         }
         else
         {
         	p = new GeoPoint(42023350,-93625622);
         }
         
         mapController.setCenter(p);
         
         mapController.setZoom(15);
         mapView.setBuiltInZoomControls(true);
         
         // http://itouchmap.com/latlong.html
         
         try {
         	//Sets marker image
         	List<Overlay> mapOverlays = mapView.getOverlays();
             Drawable drawable = this.getResources().getDrawable(R.drawable.x);
             MapOverlay itemizedoverlay = new MapOverlay(drawable, this);
         	
        	Cache[] caches = Caching.GetLocation(42023350, -93625622, 1000000);
         	
         	if (caches != null)
         	{
 	            for (int i = 0; i < caches.length; i++)
 	            {
 	            	OverlayItem overlayitem = new OverlayItem(new GeoPoint(caches[i].getLoc_lat(), caches[i].getLoc_long()), caches[i].getName(), "" + caches[i].getId());
 	                itemizedoverlay.addOverlay(overlayitem);
 	            }
 	            
 	            //Adds the overlay to the map
 	            mapOverlays.add(itemizedoverlay);
         	}
         	else
         	{
         		builder.setMessage("Not connected to the database.");
         		builder.show();
         	}
         	Drawable drawable2 = this.getResources().getDrawable(R.drawable.dot);
             MapOverlay itemizedoverlay2 = new MapOverlay(drawable2, this);
             OverlayItem overlayitem = new OverlayItem(new GeoPoint(42023350, -93625622), "", "0");
             itemizedoverlay2.addOverlay(overlayitem);
             mapOverlays.add(itemizedoverlay2);
 		} catch (Exception e) {
 			builder.setMessage("ERROR: " + e.toString());
 			builder.show();
 		}
     }
  
     @Override
     protected boolean isRouteDisplayed() {
         return false;
     }
 
 	@Override
 	public void onLocationChanged(Location arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onProviderDisabled(String arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onProviderEnabled(String arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
 		// TODO Auto-generated method stub
 		
 	}
 }
