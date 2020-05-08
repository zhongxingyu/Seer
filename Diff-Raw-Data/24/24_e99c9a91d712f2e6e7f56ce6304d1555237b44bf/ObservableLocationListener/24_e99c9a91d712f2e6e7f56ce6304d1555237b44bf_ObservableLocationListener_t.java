 package cs585_hw3.team33.lib;
 
 import java.util.ArrayList;
 
 import android.location.Location;
 import android.location.LocationListener;
 import android.os.Bundle;
 
 import com.google.android.maps.GeoPoint;
 
 public class ObservableLocationListener implements LocationListener 
 {
 	public interface LocationObserver {
 		public void locationChanged(GeoPoint p);
 	}
 	public ArrayList<LocationObserver> list;
 	public ObservableLocationListener() {
 		list = new ArrayList<LocationObserver>();
 	}
 	
     @Override
     public void onLocationChanged(Location loc) {
     	
         if (loc != null) {                
             GeoPoint new_loc = new GeoPoint(
                         (int) (loc.getLatitude() * 1E6), 
                         (int) (loc.getLongitude() * 1E6));
             for (LocationObserver l : list)
             	l.locationChanged(new_loc);
         }
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
     public void onStatusChanged(String provider, int status, 
         Bundle extras) {
         // TODO Auto-generated method stub
     }
 }
