 package duke.cs.fall2012.catseverywhere;
 import com.google.android.maps.GeoPoint;
 
 import android.content.Context;
 import android.location.Location;
 import android.location.LocationListener;
 import android.os.Bundle;
 import android.widget.Toast;
 
 public class CustomLocationListener implements LocationListener {
 	private Context myParentContext;
 	private double myLatitude;
 	private double myLongitude;
 	private GeoPoint myGeoPoint;
 	public CustomLocationListener(Context context)
 	{
 		myParentContext = context;
 		myGeoPoint = new GeoPoint(0,0);
 	}
 
 	@Override
 	public void onLocationChanged(Location location) {
 		 myLatitude = location.getLatitude();
 		 myLongitude = location.getLongitude();
 		String displayMessage = "Current location: Lat: " + myLatitude +
 				                 "\nLong: " + myLongitude;
		//Toast.makeText(myParentContext, displayMessage, Toast.LENGTH_SHORT).show();
 		myGeoPoint = new GeoPoint( (int) (myLatitude * 1E6), (int) (myLongitude * 1E6));
 		animateToCurrLoc();
 		
 	}
 
 	private void animateToCurrLoc() {
 		GoogleMapsActivity.animateToLoc(myGeoPoint);
 	}
 	
 	public GeoPoint getCurrentGeoP()
 	{
 		return myGeoPoint;
 	}
 
 	@Override
 	public void onProviderDisabled(String provider) {
 		Toast.makeText(myParentContext,"GPS Disabled", Toast.LENGTH_SHORT).show();
 		
 	}
 
 	@Override
 	public void onProviderEnabled(String provider) {
 		Toast.makeText(myParentContext,"GPS Enabled", Toast.LENGTH_SHORT).show();
 		
 	}
 
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		
 	}
 
 }
