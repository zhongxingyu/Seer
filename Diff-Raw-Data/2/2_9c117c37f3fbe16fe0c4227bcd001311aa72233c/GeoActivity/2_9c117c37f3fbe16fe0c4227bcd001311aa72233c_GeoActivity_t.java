 package com.ecn.urbapp.activities;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentSender;
 import android.location.Address;
 import android.location.Geocoder;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ProgressBar;
 import android.widget.Toast;
 
 import com.ecn.urbapp.R;
 import com.ecn.urbapp.utils.MarkerPos;
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesClient;
 import com.google.android.gms.common.GooglePlayServicesUtil;
 import com.google.android.gms.location.LocationClient;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
 import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
 import com.google.android.gms.maps.MapFragment;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.google.android.gms.maps.model.Polygon;
 import com.google.android.gms.maps.model.PolygonOptions;
 
 
 /**
 * Implements the Google Maps in a fragment.
 * User can select up to 2 or 4 points, linked by line.
 * Each point is georeferenced in GPS and a marker shows the real address.
 * Can be called from an other activity (so no more markers)
 *
 * @author Sébastien
 *
 */
 public class GeoActivity extends Activity implements
 GooglePlayServicesClient.ConnectionCallbacks,
 GooglePlayServicesClient.OnConnectionFailedListener,
 OnClickListener{
 
         /**
          * The button for switching to satellite view
          */
         private Button satellite = null;
         
         /**
          * The button for switching to plan view
          */
         private Button plan = null;
         
         /**
          * The button for switching to hybrid view
          */
         private Button hybrid = null;
         
         /**
          * The button for validating the selection
          */
         private Button validate = null;
         
         /**
          * The google map object
          */
         private GoogleMap map = null;
         
         /**
          * Contains the GPS position of the user
          */
         private LocationClient mLocationClient = null;
         
         /**
          * number of markers to display (4 for a zone, 2 for a facade)
          */
         private int nbPoints=4;
         
         /**
          * Centrale Nantes GPS centered
          */
         public static final LatLng defaultPos=new LatLng(47.249069, -1.54820);
         
         /**
          * For the localisation of tablets
          */
         private Boolean needCurrentPos;
         
     /**
 * Global constants
 * Define a request code to send to Google Play services
 * This code is returned in Activity.onActivityResult
 */
     private final static int
             CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
 
     /**
 * for addresses loading
 */
     private ProgressBar mActivityIndicator;
     
     /**
 * Contains the list of markers defined by user
 */
     private ArrayList<Marker> markers = new ArrayList<Marker>();
     
     /**
 * polygone/line option to display the selected area
 */
     public Polygon polygon;
     /**
 * polygone/line options
 */
     public PolygonOptions rectOptions;
     
 
     /**
 * Define a DialogFragment that displays the error dialog
 * @author Sebastien
 *
 */
     public static class ErrorDialogFragment extends DialogFragment {
         // Global field to contain the error dialog
         private Dialog mDialog;
         // Default constructor. Sets the dialog field to null
         public ErrorDialogFragment() {
             super();
             mDialog = null;
         }
         // Set the dialog to display
         public void setDialog(Dialog dialog) {
             mDialog = dialog;
         }
         // Return a Dialog to the DialogFragment.
         @Override
         public Dialog onCreateDialog(Bundle savedInstanceState) {
             return mDialog;
         }
     }
     
     /**
 * Show a dialog returned by Google Play services for the
 * connection error code
 *
 * @param errorCode An error code returned from onConnectionFailed
 */
     private void showErrorDialog(int errorCode) {
 
         // Get the error dialog from Google Play services
         Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
             errorCode,
             this,
             CONNECTION_FAILURE_RESOLUTION_REQUEST);
 
         // If Google Play services can provide an error dialog
         if (errorDialog != null) {
 
             // Create a new DialogFragment in which to show the error dialog
             ErrorDialogFragment errorFragment = new ErrorDialogFragment();
 
             // Set the dialog in the DialogFragment
             errorFragment.setDialog(errorDialog);
 
             // Show the error dialog in the DialogFragment
             errorFragment.show(getFragmentManager(), "Localisation");
         }
     }
 
     /**
 * Handle results returned to the FragmentActivity
 * by Google Play services
 */
     @Override
     protected void onActivityResult(
             int requestCode, int resultCode, Intent data) {
         // Decide what to do based on the original request code
         switch (requestCode) {
             case CONNECTION_FAILURE_RESOLUTION_REQUEST :
             /*
 * If the result code is Activity.RESULT_OK, try
 * to connect again
 */
                 switch (resultCode) {
                     case Activity.RESULT_OK :
                     /*
 * Try the request again
 */
                     break;
                 }
 
         }
      }
 
     /**
 * To check if Google Play Services is installed and up to date ! Propose to update if needed
 * @return boolean if is really connected
 */
     private boolean servicesConnected() {
         // Check that Google Play services is available
         int resultCode =
                 GooglePlayServicesUtil.
                         isGooglePlayServicesAvailable(MainActivity.baseContext);
         // If Google Play services is available
         if (ConnectionResult.SUCCESS == resultCode) {
             // In debug mode, log the status
             Log.d("Location Updates",
                     "Google Play services is available.");
             // Continue
             return true;
         // Google Play services was not available for some reason
         } else {
                 // Get the error code
             // Get the error dialog from Google Play services
             Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                     resultCode,
                     this,
                     CONNECTION_FAILURE_RESOLUTION_REQUEST);
 
             // If Google Play services can provide an error dialog
             if (errorDialog != null) {
                 // Create a new DialogFragment for the error dialog
                 ErrorDialogFragment errorFragment =
                         new ErrorDialogFragment();
                 // Set the dialog in the DialogFragment
                 errorFragment.setDialog(errorDialog);
                 // Show the error dialog in the DialogFragment
                 errorFragment.show(getFragmentManager(),
                         "Location Updates");
             }
         }
                 return false;
     }
 
     /**
      * For intern implementation
      */
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.layout_geo);
         needCurrentPos=true;
         
         satellite = (Button)findViewById(R.id.satellite);
         plan = (Button)findViewById(R.id.plan);
         hybrid = (Button)findViewById(R.id.hybrid);
         validate = (Button)findViewById(R.id.validate);
         
         //Listeners on switch button
         satellite.setOnClickListener(toSatellite);
         plan.setOnClickListener(toPlan);
         hybrid.setOnClickListener(toHybrid);
         validate.setOnClickListener(this);
         
       //for reverse adresses
         mActivityIndicator =
                 (ProgressBar) findViewById(R.id.address_progress);
         
         // Get a handle to the Map Fragment
         map = ((MapFragment) getFragmentManager()
                 .findFragmentById(R.id.map)).getMap();
         geoActivityInit(true, defaultPos, map);
         
         map.setOnMapClickListener(ajoutPoints);
         map.setOnMarkerDragListener(markerDrag);
     }
     
     /**
      * Constructor of GeoActivity (needed in case of extern implementation)
      * @param needCurrentPos
      * @param pos
      * @param map
      */
     public GeoActivity(Boolean needCurrentPos, LatLng pos, GoogleMap map){
     	this.map = map;
     	this.needCurrentPos = needCurrentPos;
     	geoActivityInit(needCurrentPos, pos, map);
     }
     
     public GeoActivity(){
     	
     }
     
     /**
      * 
      * @param needCurrentPos
      * @param pos
      * @param map
      */
     public void geoActivityInit(Boolean needCurrentPos, LatLng pos, GoogleMap map){
         
         if (needCurrentPos) {
          /*
          * Create a new location client, using the enclosing class to
          * handle callbacks.
          */
                 
          mLocationClient = new LocationClient(MainActivity.baseContext, this, this);
          // Connect the client.
          mLocationClient.connect();
          
         }
                 
         if (servicesConnected()){ //check
         map.setMyLocationEnabled(true);
         map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));
         
 
         }
     }
     
     @Override
 	protected void onStart() {
         super.onStart();
     }
    
     /**
 * Called when the Activity is no longer visible.
 */
     @Override
     protected void onStop() {
             if (needCurrentPos) {
          // Disconnecting the client invalidates it.
          mLocationClient.disconnect();
             }
         super.onStop();
     }
 
     /**
 * Listener for switching to Satellite map, if click on the button for.
 */
     public OnClickListener toSatellite = new OnClickListener() {
                 
                 @Override
                 public void onClick(View v) {
 
                  // Other supported types include: MAP_TYPE_NORMAL,
          // MAP_TYPE_TERRAIN, MAP_TYPE_HYBRID and MAP_TYPE_NONE
          map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
          Toast.makeText(MainActivity.baseContext, "Passage à la carte Satellite", Toast.LENGTH_SHORT).show();
                         
                 }
         };
         /**
 * Listener for switching to Plan (normal) map, if click on the button for.
 */
      public OnClickListener toPlan = new OnClickListener() {
                 
                 @Override
                 public void onClick(View v) {
 
          map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
          Toast.makeText(MainActivity.baseContext, "Passage à la carte Plan", Toast.LENGTH_SHORT).show();
                 }
         };
         /**
 * Listener for switching to hybrid map, if click on the button for.
 */
      public OnClickListener toHybrid = new OnClickListener() {
                 
                 @Override
                 public void onClick(View v) {
 
          map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
          Toast.makeText(MainActivity.baseContext, "Passage à la carte Hybride", Toast.LENGTH_SHORT).show();
                 }
         };
         /**
 * Listener for validation the selection (very simple version, just for the demonstration)
 */
         public void onClick(View v) {
         	Intent i = new Intent(this, MainActivity.class);
         	try {
         	i.putExtra("addr", markers.get(markers.size()-1).getSnippet());
         	}
         	catch (ArrayIndexOutOfBoundsException e) {
         		Log.e(getLocalClassName(), "Pas de points !");
         	}
         	i.putExtra("fragment", 2);
 			startActivity(i);
         }
         
         /**
 * Listener for adding points, gps referenced. Make a polyline with it.
 */
         private OnMapClickListener ajoutPoints = new OnMapClickListener(){
                 
                 @Override
                 public void onMapClick(LatLng point) {
                         /**
                          * We prevents to pu more than the max nb of markers
                          */
                         if(markers.size()<nbPoints) {
                                 Marker marker = map.addMarker(new MarkerOptions()
          .position(point)
          .title("Adresse postale")
          .draggable(true));
                 
                                 markers.add(marker);
                                 
                                 MarkerPos markerpos = new MarkerPos(marker, point);
                                 getAddress(markerpos);
                                 
                                 if (markers.size()>=2) {
                                         
                                          // Instantiates a new Polygon object and adds points to define a rectangle
                          rectOptions = new PolygonOptions();
                         
                          for (Marker mark : markers) {
                                  rectOptions = rectOptions.add(mark.getPosition());
                          }
                          // Remove the Polygon if exists
                                         if(polygon != null) {
                                                 polygon.remove();
                                         }
                          // Get back the mutable Polygon
                          polygon = map.addPolygon(rectOptions);
                                 }
                         }
                         else {
                                 Toast.makeText(getApplicationContext(), "Nombre de points maximum atteint", Toast.LENGTH_SHORT).show();
                         }
                 }
         };
         
         /**
 * Called by Location Services when the request to connect the
 * client finishes successfully. At this point, you can
 * request the current location or start periodic updates
 */
         
     @Override
     public void onConnected(Bundle dataBundle) {
         Location mLastLocalisation = mLocationClient.getLastLocation();
         if (mLastLocalisation != null) {
          // Getting latitude of the current location
          double latitude = mLastLocalisation.getLatitude();
         
          // Getting longitude of the current location
          double longitude = mLastLocalisation.getLongitude();
         
          // Creating a LatLng object for the current location
          LatLng latLng = new LatLng(latitude, longitude);
         
          map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
          Toast.makeText(MainActivity.baseContext, "Position ok", Toast.LENGTH_LONG).show();
         }
         else {
                final LocationManager manager = (LocationManager) getSystemService( MainActivity.baseContext.LOCATION_SERVICE );
 
             if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
                 buildAlertMessageNoGps();
             }
         }
 
     }
     /**
 * Displays a message if user has disabled GPS. Invite to put it on (shortcut to settings)
 */
     private void buildAlertMessageNoGps() {
         final AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setMessage("Votre GPS semble désactivé, souhaitez-vous l'activer ?")
                .setCancelable(false)
                .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                    @Override
 				public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("Non", new DialogInterface.OnClickListener() {
                    @Override
 				public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                         dialog.cancel();
                    }
                });
         final AlertDialog alert = builder.create();
         alert.show();
     }
 
     /**
 * Called by Location Services if the connection to the
 * location client drops because of an error.
 */
     @Override
     public void onDisconnected() {
         // Display the connection status
         Toast.makeText(this, "Disconnected. Please re-connect.",
                 Toast.LENGTH_SHORT).show();
     }
 
     /**
 * Called by Location Services if the attempt to
 * Location Services fails.
 */
     @Override
     public void onConnectionFailed(ConnectionResult connectionResult) {
         /*
 * Google Play services can resolve some errors it detects.
 * If the error has a resolution, try sending an Intent to
 * start a Google Play services activity that can resolve
 * error.
 */
         if (connectionResult.hasResolution()) {
             try {
                 // Start an Activity that tries to resolve the error
                 connectionResult.startResolutionForResult(
                         this,
                         CONNECTION_FAILURE_RESOLUTION_REQUEST);
                 /*
 * Thrown if Google Play services canceled the original
 * PendingIntent
 */
             } catch (IntentSender.SendIntentException e) {
                 // Log the error
                 e.printStackTrace();
             }
         } else {
             /*
 * If no resolution is available, display a dialog to the
 * user with the error.
 */
             showErrorDialog(connectionResult.getErrorCode());
         }
     }
 
     
     //for adresses
     /**
 * A subclass of AsyncTask that calls getFromLocation() in the
 * background. The class definition has these generic types:
 * MarkerPos - Contain the current position, the marker to
 * change and space for the address (String)
 * Void - indicates that progress units are not used
 * MarkerPos - THe whole thing passed to onPostExecute()
 */
      private class GetAddressTask extends
              AsyncTask<MarkerPos, Void, MarkerPos> {
          Context mContext;
          public GetAddressTask(Context context) {
              super();
              mContext = context;
          }
 
 
          /**
 * Get a Geocoder instance, get the latitude and longitude
 * look up the address, and return it
 *
 * @params params One or more Latlng objects
 * @return A string containing the address of the current
 * location, or an empty string if no address can be found,
 * or an error message
 */
          @Override
 		protected MarkerPos doInBackground(MarkerPos... params) {
              Geocoder geocoder =
                      new Geocoder(mContext, Locale.getDefault());
              // Get the current location from the input parameter list
              MarkerPos markpos = new MarkerPos(params[0]);
              LatLng loc = markpos.getPosition();
              // Create a list to contain the result address
              List<Address> addresses = null;
              try {
                 /*
 				* Return 1 address.
 				*/
                  addresses = geocoder.getFromLocation(loc.latitude,
                          loc.longitude, 1);
              } catch (IOException e1) {
              Log.e("LocationSampleActivity",
                      "IO Exception in getFromLocation()");
              e1.printStackTrace();
              markpos.setAdresse("Impossible d'avoir l'adresse. Vérifier connexion réseau");
              return markpos;
              } catch (IllegalArgumentException e2) {
              // Error message to post in the log
              String errorString = "Illegal arguments " +
                      Double.toString(loc.latitude) +
                      " , " +
                      Double.toString(loc.longitude) +
                      " passed to address service";
              Log.e("LocationSampleActivity", errorString);
              e2.printStackTrace();
              markpos.setAdresse(errorString);
              return markpos;
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
                  markpos.setAdresse(addressText);
                  return markpos;
              } else {
                      markpos.setAdresse("No address found");
                      return markpos;
              }
          }
          
          /**
                  * A method that's called once doInBackground() completes. Turn
                  * off the indeterminate activity indicator and set
                  * the text of the UI element that shows the address. If the
                  * lookup failed, display the error message.
                  */
                  @Override
                  protected void onPostExecute(MarkerPos markpos) {
                  // Set activity indicator visibility to "gone"
                  mActivityIndicator.setVisibility(View.GONE);
                  // Register the results of the lookup.
                  markpos.getMarker().setSnippet(markpos.getAdresse());
                  markpos.getMarker().showInfoWindow();
                 
                  if(markers.size()>=2){
                          
                           // Instantiates a new Polygon object and adds points to define a rectangle
                  rectOptions = new PolygonOptions();
                 
                  for (Marker mark : markers) {
                          rectOptions = rectOptions.add(mark.getPosition());
                  }
                  polygon.remove();
                                  // Get back the mutable Polygon
                                  polygon = map.addPolygon(rectOptions);
                  }
                  }
                 
      }
      /**
 *
 * @param v The view object associated with this method,
 * in this case a Button.
 */
      public void getAddress(MarkerPos markpos) {
          // Ensure that a Geocoder services is available
          if (Build.VERSION.SDK_INT >=
                  Build.VERSION_CODES.GINGERBREAD
                              &&
                  Geocoder.isPresent()) {
              // Show the activity indicator
              mActivityIndicator.setVisibility(View.VISIBLE);
              /*
 			* Reverse geocoding is long-running and synchronous.
 			* Run it on a background thread.
 			* Pass the current location to the background task.
 			* When the task finishes,
 			* onPostExecute() displays the address.
 			*/
              (new GetAddressTask(this)).execute(markpos);
          }
      }
 
      /**
 * Dragging markers listeners.
 * Force the rerending of address and of the polygone
 */
      public OnMarkerDragListener markerDrag = new OnMarkerDragListener() {
                 
                 @Override
                 public void onMarkerDragStart(Marker marker) {
                         marker.hideInfoWindow();
                         
                 }
                 
                 @Override
                 public void onMarkerDragEnd(Marker marker) {
                         MarkerPos markpos = new MarkerPos(marker, marker.getPosition());
                         getAddress(markpos);                        
                 }
                 
                 @Override
                 public void onMarkerDrag(Marker marker) {
                          if(markers.size()>=2){
                                  polygon.remove();
                                  
                                   // Instantiates a new Polygon object and adds points to define a rectangle
                          rectOptions = new PolygonOptions();
                         
                          for (Marker mark : markers) {
                                  rectOptions = rectOptions.add(mark.getPosition());
                          }
                         
                          polygon.remove();
                          // Get back the mutable Polygon
                          polygon = map.addPolygon(rectOptions);
                          }
                         
                 }
         };
 }
