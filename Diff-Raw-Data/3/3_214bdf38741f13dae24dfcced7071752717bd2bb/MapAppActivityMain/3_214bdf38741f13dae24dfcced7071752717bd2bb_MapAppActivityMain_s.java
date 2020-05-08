 package fi.dy.esav.MapApp;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.util.Log;
 
 public class MapAppActivityMain extends Activity {
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         getStatus();
         
     }
     
     int getStatus() {
     	LocationManager manager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
         List<String> providers = manager.getAllProviders();
         for(String provider : providers) {
             Log.e("fi.dy.esav.MapApp", "Available location provider: " + provider);
         	Location loc = manager.getLastKnownLocation(provider);
             if(loc != null) {
                 Log.e("fi.dy.esav.MapApp", "Accuracy is: " + loc.getAccuracy());
             } else {
                 Log.e("fi.dy.esav.MapApp", "No location");
             }
         	
         }
         
         return 0;
     }
 }
