 package com.android_prod;
 
 //~--- non-JDK imports --------------------------------------------------------
 
 import android.app.Activity;
 import android.app.AlertDialog;
 
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 
 import android.graphics.drawable.Drawable;
 
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 
 import android.util.Log;
 import android.util.SparseBooleanArray;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.SlidingDrawer;
 import android.widget.SlidingDrawer.OnDrawerCloseListener;
 import android.widget.SlidingDrawer.OnDrawerOpenListener;
 import android.widget.Toast;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
 import org.osmdroid.util.GeoPoint;
 import org.osmdroid.views.MapController;
 import org.osmdroid.views.MapView;
 import org.osmdroid.views.overlay.ItemizedIconOverlay;
 import org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener;
 import org.osmdroid.views.overlay.MinimapOverlay;
 import org.osmdroid.views.overlay.OverlayItem;
 import org.osmdroid.views.overlay.ScaleBarOverlay;
 
 //~--- JDK imports ------------------------------------------------------------
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class MapViewClass<Overlay> extends Activity implements LocationListener {
 
     /** ************************************************* */
 
     /** ****************** Listener ********************** */
 
     /** ******* Listener for the Locate me Button ******** */
     private OnClickListener locateMeListener = new OnClickListener() {
         @Override
         public void onClick(View arg0) {
         	// clean overlay stack if an old position is in the stack
         	if(mapView.getOverlays().contains(myLocationItemizedIconOverlay))
         	   mapView.getOverlays().remove(myLocationItemizedIconOverlay);
         	   
             // Map centrelize in your place.
             GeoPoint point2 = new GeoPoint(myLat, myLng);
 
             mapController.setCenter(point2);
 
             /** ************* this overlay  prsent your location ***************** */
 
             // Create a geopoint marker
             myLocationOverlayItemArray = new ArrayList<OverlayItem>();
             myLocationOverlayItemArray.add(new OverlayItem("Hello", "Here I am", point2));
 
             // Copy the marker array into another table
             myLocationItemizedIconOverlay = new ItemizedIconOverlay<OverlayItem>(getApplicationContext(),
                     myLocationOverlayItemArray, null);
 
             // Add the overlays on the map
             mapView.getOverlays().add(myLocationItemizedIconOverlay);
         
           
         }
     };
 
     /** ************************************************** */
 
     /** **********Listener fir the valider button********** */
     private OnClickListener validerListener = new OnClickListener() {
         @Override
         public void onClick(View v) {
 
             // Close the sliding drawer
             slidingMenu.close();
 
        
         }
     };
 
     /** **************************************** */
     /** handler use when GPS was not enable*/
     /** *************************************** */
     private final Handler handler = new Handler() {
         public void handleMessage(Message msg) {
             if (msg.arg1 == 1) {
                 if (!isFinishing()) {    // Without this in certain cases application will show ANR
                     AlertDialog.Builder builder = new AlertDialog.Builder(MapViewClass.this);
 
                     builder.setMessage("Your GPS is disabled! Would you like to enable it?").setCancelable(
                         false).setPositiveButton("Enable GPS", new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int id) {
                             Intent gpsOptionsIntent =
                                 new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
 
                             startActivity(gpsOptionsIntent);
                         }
                     });
                     builder.setNegativeButton("Do nothing", new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int id) {
                             dialog.cancel();
                         }
                     });
 
                     AlertDialog alert = builder.create();
 
                     alert.show();
                 }
             }
         }
     };
 
     /** ************ Global variable declaration ******* */
 
     //context
     Context mcontext;
 
     // Map Variable
     private MapController mapController;
     private MapView       mapView;
 
     // Layout element
     ImageButton   locateMe;
     Button        valider, handle;
     SlidingDrawer slidingMenu;
 
     // Geolocation variable
     double myLat, myLng;
 
     // Location listener
     private LocationManager lm;
 
     // Marker variable
     public ArrayList<OverlayItem>            myLocationOverlayItemArray;
     private ArrayList<OverlayItem>           bikeOverlayItemArray;
     private ItemizedIconOverlay<OverlayItem> bikeItemizedIconOverlay;
     private Drawable                         bikeMarker;
     private ArrayList<OverlayItem>           busOverlayItemArray;
     private ItemizedIconOverlay<OverlayItem> busItemizedIconOverlay;
     private Drawable                         busMarker;
 
     // Intent value
     private JSONObject                      bikeData;
     private String                          bikeIntent;
     private JSONObject                      busData;
     private String                          busIntent;
     private ItemizedIconOverlay<OverlayItem> myLocationItemizedIconOverlay;
     
     // for layers
     
     final CharSequence[] items = {"Bus", "metro", "velo"};
     private boolean[] states = {true, true, true};
 
     /** ************************************************* */
 
     /** ******* On create Mehtod First launch *********** */
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.map_view);
 
         // Initiate the mcontext variable
         mcontext = this;
 
         // We get the data from the intent
         // dynamic informations
         Intent intent = getIntent();
         String lng    = intent.getStringExtra("longitude");
         String lat    = intent.getStringExtra("latitude");
 
         bikeIntent = intent.getStringExtra("bikeData");
       // busIntent = intent.getStringExtra("busData");
         
 
         // Location listner
         lm = (LocationManager) this.getSystemService(LOCATION_SERVICE);
 
         if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
             lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, (LocationListener) this);
         }
 
         lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0, (LocationListener) this);
 
         // And the String into Json
         try {
             bikeData = new JSONObject(bikeIntent);
            //busData = new JSONObject(busIntent);
         } catch (JSONException e) {
             Toast.makeText(getApplicationContext(), "Error json" + e.toString(), Toast.LENGTH_LONG).show();
         }
       // Log.i("json Object", busIntent);
 
         // We get the layout elements
         mapView     = (MapView) findViewById(R.id.mapview);
         handle      = (Button) findViewById(R.id.slideButton);
         slidingMenu = (SlidingDrawer) findViewById(R.id.drawer);
         locateMe    = (ImageButton) findViewById(R.id.locateMe);
         valider     = (Button) findViewById(R.id.valider);
 
         // Define the marker
         bikeMarker = this.getResources().getDrawable(R.drawable.velo);
       // busMarker = this.getResources().getDrawable(R.drawable.bus);
 
         // Set listener on the layout elements
         locateMe.setOnClickListener(locateMeListener);
         valider.setOnClickListener(validerListener);
 
         /** ************* Sliding Drawer Listener ************* */
 
         // Set the sliding drawer
         handle.setBackgroundResource(R.drawable.downarrow);
 
         // Listener on open event of sliding drawer
         slidingMenu.setOnDrawerOpenListener(new OnDrawerOpenListener() {
             @Override
             public void onDrawerOpened() {
                 handle.setBackgroundResource(R.drawable.uparrow);
             }
         });
 
         // Listener on Close event of Sliding drawer
         slidingMenu.setOnDrawerCloseListener(new OnDrawerCloseListener() {
             @Override
             public void onDrawerClosed() {
                 handle.setBackgroundResource(R.drawable.downarrow);
             }
         });
 
         /** ************************************************* */
 
         // Controle the map
         mapView.setTileSource(TileSourceFactory.MAPNIK);
         mapView.setBuiltInZoomControls(true);
         mapController = mapView.getController();
         mapController.setZoom(13);
       //coordonne mairie de rennes
     	myLat=48.1115579;
     	myLng=-1.6799608999999691;
         GeoPoint point2 = new GeoPoint(myLat, myLng);
 
         mapController.setCenter(point2);
 
         /** ************************************************* */
         
         
         /** ******* This display all the bike station ******* */
 
         // Call the method that create a item array
          displayBikePoint(bikeData);
         //displayBusPoint(busData);
        
 
         /** ************************************************** */
     }
 
     /** ************************************************* */
 
     /** ************ Is route display method ************ */
     protected boolean isRouteDisplayed() {
 
         // TODO Auto-generated method stub
         return false;
     }
 
     /** ************************************************* */
 
     /** * Method launch on start. Create the action Bar * */
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);    // Get the action Bar from the menu
         getActionBar().setDisplayShowTitleEnabled(false);         // Hide the Title of the app in the action bar
         getActionBar().setDisplayShowHomeEnabled(true);          // display the Icon of the app in the action bar
 
         return true;
     }
 
     /** ************************************************* */
 
     /** * Manage the Tap on buttons of the Action Bar *** */
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.menu_search :
 
             // behaviour of reserch menu
             return true;
 
        // case R.id.menu_settings : // not usfull
             // Change activity to settings menu
         case R.id.userLevel :
         	// if the user is trained
         {}
         case R.id.layers :
         {
         
         	final boolean velo =states[2];
         	final boolean metro =states[1];
         	final boolean bus =states[0];
         	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
         	    builder.setTitle("Quel layer vous intreresse?");
         	    builder.setMultiChoiceItems(items, states, new DialogInterface.OnMultiChoiceClickListener(){
         	        public void onClick(DialogInterface dialogInterface, int item, boolean state) {
         	        }
         	    });
         	    builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
         	        public void onClick(DialogInterface dialog, int id) {
         	            SparseBooleanArray CheCked = ((AlertDialog)dialog).getListView().getCheckedItemPositions();
         	            if(CheCked.get(CheCked.keyAt(0)) == true){
         	            	
         	                
         	            }
         	            if(CheCked.get(CheCked.keyAt(1)) == true){
         	            	
         	                
         	            }
         	            if(CheCked.get(CheCked.keyAt(2)) == true){
         	            	if(!mapView.getOverlays().contains(bikeItemizedIconOverlay))
         	            	{
         	            		 // Call the method that create a item array
         	                    displayBikePoint(bikeData);
         	                    mapView.invalidate(); // refresh map
         	                   
         	            	}
 
         	            		
         	            }else
         	            {
         	            	if(mapView.getOverlays().contains(bikeItemizedIconOverlay))
         	            	{
         	            		mapView.getOverlays().remove(bikeItemizedIconOverlay);
         	            		 mapView.invalidate(); // refresh mapp
         	            	}
         	            }
         	        }
         	    });
         	    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
         	        public void onClick(DialogInterface dialog, int id) {
         	             // cache gestion
         	        	states[2]=velo;
         	        	states[1]=metro;
         	        	states[0]=bus;
         	        	dialog.cancel();
         	        }
         	    });
         	    builder.create().show();
         
         }
         default :
             return super.onOptionsItemSelected(item);
         }
     }
 
     /** ************************************************** */
 
     /** ******** Display a list of point into marker ***** */
 
     
      @SuppressWarnings("unused")
 	private void displayBikePoint(JSONObject dataJson) {
     	 try {
         // Declare variables
         JSONObject openData;
         JSONArray  station = null;
         OverlayItem marker=null;
         // Create the overlay and add it to the array
         bikeOverlayItemArray = new ArrayList<OverlayItem>();
      
         // Then we get the part of the JSON that we want
        
             openData = dataJson.getJSONObject("opendata");
      
             JSONObject answer = openData.getJSONObject("answer");
             JSONObject data   = answer.getJSONObject("data");
      
             station = data.getJSONArray("station");
      
      
         // Loop the Array
         for (int i = 0; i < station.length(); i++) {
      
             // We get the data we want
             JSONObject tmpStation;
             String     name          = null,
             			bikeAvailable = null,
                        slotAvailable = null;
             double     lng           = 0,
                       lat           = 0;
      
             // Initiate the variable we need
            
             	tmpStation = station.getJSONObject(i);
      
                 // get the value
                 lng           = tmpStation.getDouble("longitude");
                 lat           = tmpStation.getDouble("latitude");
                 name          = tmpStation.getString("name");
                 bikeAvailable = tmpStation.getString("bikesavailable");
                 slotAvailable = tmpStation.getString("slotsavailable");
                 
                 // Create a overlay for a special position
                  marker = new OverlayItem(name, "Velo Dispo :"+bikeAvailable+"\nEmplacement Dispo :"+slotAvailable, new GeoPoint(lat, lng));
           
                  // Add the graphics to the marker
                  marker.setMarker(bikeMarker);
                  
           
                  // Add the marker into the list
                  bikeOverlayItemArray.add(marker);
                  
      
      		}
         
         
       /// overlays gestion for bike
         
         OnItemGestureListener<OverlayItem> myOnItemGestureListener
             = new OnItemGestureListener<OverlayItem>(){
 
           // dsiplay  bike information when  user click
           @Override
           public boolean onItemSingleTapUp(int index, OverlayItem item) {
            Toast.makeText(MapViewClass.this, 
              item.mTitle + "\n"
              + item.mDescription,
              Toast.LENGTH_LONG).show();
            return true;
           }
           	// none used
 		@Override
 		public boolean onItemLongPress(int arg0, OverlayItem arg1) {
 			// TODO Auto-generated method stub
 			return false;
 		}
              
             };
         
      // Add the array into another array with some parameters
        
         
         bikeItemizedIconOverlay = new ItemizedIconOverlay<OverlayItem>(mcontext, bikeOverlayItemArray, myOnItemGestureListener);
        
 
        
     	
         // Add the overlays into the map
         mapView.getOverlays().add(bikeItemizedIconOverlay);
     	   } catch (JSONException e) {
     		     
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
         
       }
      
      
      @SuppressWarnings({ "unused", "null" })
 	
     /**private void displayBusPoint(JSONObject dataJson) {
     	 try {
         // Declare variables
         JSONObject openData;
         JSONArray  station = null;
         OverlayItem marker=null;
         // Create the overlay and add it to the array
         busOverlayItemArray = new ArrayList<OverlayItem>();
      
         // Then we get the part of the JSON that we want
        
             openData = dataJson.getJSONObject("opendata");
      
             JSONObject answer = openData.getJSONObject("answer");
             JSONObject data   = answer.getJSONObject("data");
      
             station = data.getJSONArray("station");
      
      
         // Loop the Array
         for (int i = 0; i < station.length(); i++) {
      
             // We get the data we want
             JSONObject tmpStation;
             String     name          = null;
             double     lng           = 0,
                       lat           = 0;
      
             // Initiate the variable we need
            
             	tmpStation = station.getJSONObject(i);
      
                 // get the value
                 lng           = tmpStation.getDouble("longitude");
                 lat           = tmpStation.getDouble("latitude");
                 name          = tmpStation.getString("name");
 
                 
                 // Create a overlay for a special position
                 // marker = new OverlayItem(next departure);
                  // Add the graphics to the marker
                  marker.setMarker(busMarker);
                  
           
                  // Add the marker into the list
                  busOverlayItemArray.add(marker);
                  
      
      		}
         
         
       /// overlays gestion for bike
         
         OnItemGestureListener<OverlayItem> myOnItemGestureListener
             = new OnItemGestureListener<OverlayItem>(){
 
           // dsiplay  bike information when  user click
           @Override
           public boolean onItemSingleTapUp(int index, OverlayItem item) {
            Toast.makeText(MapViewClass.this, 
              item.mTitle + "\n"
              + item.mDescription,
              Toast.LENGTH_LONG).show();
            return true;
           }
           	// none used
 		@Override
 		public boolean onItemLongPress(int arg0, OverlayItem arg1) {
 			// TODO Auto-generated method stub
 			return false;
 		}
              
             };
         
      // Add the array into another array with some parameters
        
         
         busItemizedIconOverlay = new ItemizedIconOverlay<OverlayItem>(mcontext, busOverlayItemArray, myOnItemGestureListener);
        
 
        
     	
         // Add the overlays into the map
         mapView.getOverlays().add(busItemizedIconOverlay);
     	   } catch (JSONException e) {
     		     
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
         
       }
     */
 
     /** ************************************************* */
 
     /** ************Location Listener****************** */
 
     /** ****** Called when the location change********* */
     public void onLocationChanged(Location location) {
 
         // Copy the longitude and latitude in global variable
         myLat = location.getLatitude();
         myLng = location.getLongitude();
  
     }
 
     /** ************************************************* */
 
     /** *********Called when the GPS is Disable********** */
     @Override
     public void onProviderDisabled(String arg0) {
         Message msg = handler.obtainMessage();
 
         msg.arg1 = 1;
         handler.sendMessage(msg);
     }
 
     /** ************************************************* */
 
     /** *********Called when the GPS is Enable ********** */
     public void onProviderEnabled(String arg0) {
 
         // TODO Auto-generated method stub
     }
 
     /** ************************************************* */
 
     /** *****Called when the GPS status change ********** */
     public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
 
         // TODO Auto-generated method stub
     }
 
     /** ************************************************* */
 }
 
 
 //~ Formatted by Jindent --- http://www.jindent.com
