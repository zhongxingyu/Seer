 package pl.pamieciprzyszlosc.app;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.support.v4.app.FragmentActivity;
 import android.view.Menu;
 import android.app.ActionBar;
 import android.view.MenuItem;
 import android.widget.Toast;
 import android.content.Context;
 import java.io.IOException;
 import java.util.List;
 import java.util.Locale;
 
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesClient;
 import com.google.android.gms.location.LocationClient;
 import android.location.LocationManager;
 import android.location.LocationListener;
 import android.content.Context;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 import android.location.Address;
 import android.location.Geocoder;
 import android.location.Location;
 import android.os.AsyncTask;
 
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 import android.app.Fragment;
 
 public class MainActivity extends FragmentActivity implements
         GooglePlayServicesClient.ConnectionCallbacks,
         GooglePlayServicesClient.OnConnectionFailedListener {
 
 
     private GoogleMap mMap;
     private LocationClient mLocationClient;
     private TextView addressLabel;
     private TextView locationLabel;
     private Button getLocationBtn;
     private Button disconnectBtn;
     private Button connectBtn;
     private LocationManager mLocationManager;
     private LocationListener mMyLocationListener;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         this.setContentView(R.layout.activity_main);
         locationLabel = (TextView) findViewById(R.id.locationLabel);
         addressLabel = (TextView) findViewById(R.id.addressLabel);
         getLocationBtn = (Button) findViewById(R.id.getLocation);
 
         getLocationBtn.setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
                 displayCurrentLocation();
             }
         });
         disconnectBtn = (Button) findViewById(R.id.disconnect);
         disconnectBtn.setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
                 mLocationClient.disconnect();
                 locationLabel.setText("Got disconnected....");
             }
         });
         connectBtn = (Button) findViewById(R.id.connect);
         connectBtn.setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
                 mLocationClient.connect();
                 locationLabel.setText("Got connected....");
             }
         });
         // Create the LocationRequest object
         mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
         mMyLocationListener = new MyLocationListener();
        mLocationManager.requestLocationUpdates(mLocationManager.GPS_PROVIDER,500, (float) 150,mMyLocationListener);
 
         mLocationClient = new LocationClient(this, this, this);
         setUpMapIfNeeded();
     }
 
 
 
     private void setUpMapIfNeeded() {
         // Do a null check to confirm that we have not already instantiated the map.
         if (mMap == null) {
             // Try to obtain the map from the SupportMapFragment.
             mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                     .getMap();
             // Check if we were successful in obtaining the map.
             if (mMap != null) {
                 setUpMap();
             }
         }
     }
 
     private void setUpMap() {
         mMap.clear();
         if (mLocationClient.isConnected()){
             Location location = mLocationClient.getLastLocation();
             LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
             mMap.addMarker(new MarkerOptions().position(latLng).title("Lokalizacja"));
         }
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
 
         Context context = getApplicationContext();
         int duration = Toast.LENGTH_SHORT;
         CharSequence text="";
 
 
         switch(item.getItemId()){
             case R.id.action_map:
                 text = "Mapa!";
                 break;
             case R.id.action_gallery:
                 text = "Galeria!";
                 break;
             case R.id.action_about:
                 text = "Info!";
                 break;
         }
 
 
 
         Toast toast = Toast.makeText(context, text, duration);
         toast.show();
         return super.onOptionsItemSelected(item);
     }
 
     @Override
     protected void onStart() {
         super.onStart();
         // Connect the client.
         mLocationClient.connect();
         locationLabel.setText("Got connected....");
     }
     @Override
     protected void onStop() {
         // Disconnect the client.
         mLocationClient.disconnect();
         super.onStop();
         locationLabel.setText("Got disconnected....");
     }
     @Override
     public void onConnected(Bundle dataBundle) {
         // Display the connection status
         Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
     }
     @Override
     public void onDisconnected() {
         // Display the connection status
         Toast.makeText(this, "Disconnected. Please re-connect.",
                 Toast.LENGTH_SHORT).show();
     }
     @Override
     public void onConnectionFailed(ConnectionResult connectionResult) {
         // Display the error code on failure
         Toast.makeText(this, "Connection Failure : " +
                 connectionResult.getErrorCode(),
                 Toast.LENGTH_SHORT).show();
     }
     public void displayCurrentLocation() {
         // Get the current location's latitude & longitude
         Location currentLocation = mLocationClient.getLastLocation();
         setUpMap();
         String msg = "Current Location: " +
                 Double.toString(currentLocation.getLatitude()) + "," +
                 Double.toString(currentLocation.getLongitude());
 
         // Display the current location in the UI
         locationLabel.setText(msg);
 
         // To display the current address in the UI
         (new GetAddressTask(this.getApplicationContext())).execute(currentLocation);
     }
     /*
      * Following is a subclass of AsyncTask which has been used to get
      * address corresponding to the given latitude & longitude.
      */
     private class GetAddressTask extends AsyncTask<Location, Void, String>{
         Context mContext;
         public GetAddressTask(Context context) {
             super();
             mContext = context;
         }
 
         /*
          * When the task finishes, onPostExecute() displays the address.
          */
         @Override
         protected void onPostExecute(String address) {
             // Display the current address in the UI
             addressLabel.setText(address);
         }
         @Override
         protected String doInBackground(Location... params) {
             Geocoder geocoder =
                     new Geocoder(mContext, Locale.getDefault());
             // Get the current location from the input parameter list
             Location loc = params[0];
             // Create a list to contain the result address
             List<Address> addresses = null;
             try {
                 addresses = geocoder.getFromLocation(loc.getLatitude(),
                         loc.getLongitude(), 1);
             } catch (IOException e1) {
                 Log.e("LocationSampleActivity",
                         "IO Exception in getFromLocation()");
                 Log.e("LocationSampleActivity",e1.toString());
                 return ("IO Exception trying to get address");
             } catch (IllegalArgumentException e2) {
                 // Error message to post in the log
                 String errorString = "Illegal arguments " +
                         Double.toString(loc.getLatitude()) +
                         " , " +
                         Double.toString(loc.getLongitude()) +
                         " passed to address service";
                 Log.e("LocationSampleActivity", errorString);
                 e2.printStackTrace();
                 return errorString;
             }
             // If the reverse geocode returned an address
             if (addresses != null && addresses.size() > 0) {
                 // Get the first address
                 Address address = addresses.get(0);
             /*
             * Format the first line of address (if available),
             * city, and country name.
             */
                 String addressText = String.format(
                         "%s, %s, %s",
                         // If there's a street address, add it
                         address.getMaxAddressLineIndex() > 0 ?
                                 address.getAddressLine(0) : "",
                         // Locality is usually a city
                         address.getLocality(),
                         // The country of the address
                         address.getCountryName());
                 // Return the text
                 return addressText;
             } else {
                 return "No address found";
             }
         }
     }// AsyncTask class
 
 
     public class MyLocationListener implements LocationListener{
 
         @Override
         public void onLocationChanged(Location location) {
 
             String toastText = "Current location updated!\n"
                     + "Latitude: "+ location.getLatitude() +"\n"
                     + "Longitude: "+ location.getLongitude();
             Toast.makeText(getApplicationContext(),toastText,Toast.LENGTH_SHORT).show();
 
         }
 
         @Override
         public void onStatusChanged(String s, int i, Bundle bundle) {
             //dummy function unused so far
         }
 
         @Override
         public void onProviderEnabled(String s) {
                 Toast.makeText(getApplicationContext(),"Provider enabled", Toast.LENGTH_SHORT).show();
         }
 
         @Override
         public void onProviderDisabled(String s) {
             Toast.makeText(getApplicationContext(),"Provider disabled", Toast.LENGTH_SHORT).show();
         }
     }
 
 
 }
