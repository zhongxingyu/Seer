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
 	
 	ArrayList<LocationObserver> list;
 	GeoPoint current_loc;
 	
 	public ObservableLocationListener(Location last_known) {
 		list = new ArrayList<LocationObserver>();
 		
 		if (last_known != null) {                
             current_loc = new GeoPoint(
                         (int) (last_known.getLatitude() * 1E6), 
                         (int) (last_known.getLongitude() * 1E6));
         }
 	}
 	
 	public void subscribe(LocationObserver lo) {
 		if (lo == null)
 			return;
		list.add(lo);
		lo.locationChanged(current_loc);
 	}
 	public void unsubscribe(LocationObserver lo) {
 		list.remove(lo);
 	}
 	
 	public GeoPoint getLastKnown() {
 		return current_loc;
 	}
     @Override
     public void onLocationChanged(Location loc) {
     	
         if (loc != null) {                
             current_loc = new GeoPoint(
                         (int) (loc.getLatitude() * 1E6), 
                         (int) (loc.getLongitude() * 1E6));
             for (LocationObserver l : list)
             	l.locationChanged(current_loc);
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
 
 	public void informLastKnownLocation(Location lastKnownLocation) {
 		
 	}
 }
