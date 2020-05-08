 package com.android_prod;
 
 //~--- non-JDK imports --------------------------------------------------------
 
 import android.app.Activity;
 import android.app.AlertDialog;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 
 import android.graphics.drawable.Drawable;
 
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 
 import android.net.sip.SipSession.Listener;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 
 import android.util.Log;
 import android.util.SparseBooleanArray;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.SlidingDrawer;
 import android.widget.SlidingDrawer.OnDrawerCloseListener;
 import android.widget.SlidingDrawer.OnDrawerOpenListener;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
 import org.osmdroid.util.GeoPoint;
 import org.osmdroid.views.MapController;
 import org.osmdroid.views.MapView;
 import org.osmdroid.views.overlay.ItemizedIconOverlay;
 import org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener;
 import org.osmdroid.views.overlay.OverlayItem;
 
 
 //~--- JDK imports ------------------------------------------------------------
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * map view acitivity
  * @author ronan
  *
  * @param <Overlay>
  */
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
 
            
             //sent information
             
             Intent sent=new Intent(TrajetServerRequest.BROADCAST_ACTION_SEND);
             sent.putExtra("Dep", stopList.get("PONT DE STRASBOURG").getName()+":"+stopList.get("PONT DE STRASBOURG").getValue());
             sent.putExtra("Arr", stopList.get("REPUBLIQUE").getName()+":"+stopList.get("REPUBLIQUE").getValue());
             sent.putExtra("bus", true);
             sent.putExtra("bike", true);
             sent.putExtra("metro", true);
            startService(sent);
             
           
             
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
 
     static //context
     Context mcontext;
 
     // Map Variable
     private MapController mapController;
     private static  MapView       mapView;
 
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
     private static ArrayList<OverlayItem>           bikeOverlayItemArray;
     private static  ItemizedIconOverlay<OverlayItem> bikeItemizedIconOverlay;
     private static  Drawable                         bikeMarker;
     // to have <Name, Type:id>
     private static  Map<String, NameValuePair> stopList=new HashMap<String, NameValuePair>();
     
     private ArrayList<OverlayItem>           busOverlayItemArray;
     private ItemizedIconOverlay<OverlayItem> busItemizedIconOverlay;
     private Drawable                         busMarker;
     private static  JSONArray 						 busArray ;
     
     private static  ArrayList<OverlayItem>           metroOverlayItemArray;
     private static ItemizedIconOverlay<OverlayItem> metroItemizedIconOverlay;
     private static  Drawable                         metroMarker;
     private static  JSONArray 						 metroArray ;
 
     private static  ArrayList<OverlayItem>           borneOverlayItemArray;
     private static  ItemizedIconOverlay<OverlayItem> borneItemizedIconOverlay;
     private static  Drawable                         borneMarker;
     private static  JSONArray 						 borneArray ;
     
     
 
     private static  ArrayList<OverlayItem>           trainOverlayItemArray;
     private static  ItemizedIconOverlay<OverlayItem> trainItemizedIconOverlay;
     private static  Drawable                         trainMarker;
     private static  JSONArray 						 trainArray ;
     // Intent value
     private static  JSONObject                      bikeData;
     private ItemizedIconOverlay<OverlayItem> myLocationItemizedIconOverlay;
    // Intent intent = new Intent();
 	// Get the string array
    private  static String[] stop;
    static RelativeLayout rl;
    
     
    
     // for layers
     
     final CharSequence[] itemLayers = {"Bus", "metro", "velo"};
     private boolean[] statesLayers = {false, true, true};
 
     private static AutoCompleteTextView dep;
     private static AutoCompleteTextView Arr;
     static BMARequestReciver broadcastReceiver;
     
  // for bike level
     
     final CharSequence[] itemsVelo = {"biker", "have fun", "be cool"};
     private boolean[] statesVelo = {false, false, true};
 
     /** ************************************************* */
 
     /** ******* On create Mehtod First launch *********** */
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
          broadcastReceiver = new BMARequestReciver();
          
        /** {
 			@Override
 			public void onReceive(Context context, Intent intent) {
 				Log.i("PUTIN", "MERDE");
 				majData(intent.getStringExtra("bikeData"),intent.getStringExtra("busData"),intent.getStringExtra("metroData"),intent.getStringExtra("borneData"));
 			}
         };*/
       this.registerReceiver(broadcastReceiver, new IntentFilter(HttpRequestClass.BROADCAST_ACTION));
         setContentView(R.layout.map_view);
         
         
      
      
 
         // Initiate the mcontext variable
         mcontext = this;
 
         // We get the data from the intent
         // dynamic informations
         Intent intent = getIntent();
         String lng    = intent.getStringExtra("longitude");
         String lat    = intent.getStringExtra("latitude");
 
       
     
 
         // Location listner
         lm = (LocationManager) this.getSystemService(LOCATION_SERVICE);
 
         if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
             lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, (LocationListener) this);
         }
 
         lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0, (LocationListener) this);
 
        
        
 
         // We get the layout elements
         mapView     = (MapView) findViewById(R.id.mapview);
         handle      = (Button) findViewById(R.id.slideButton);
         slidingMenu = (SlidingDrawer) findViewById(R.id.drawer);
         locateMe    = (ImageButton) findViewById(R.id.locateMe);
        valider     = (Button) findViewById(R.id.valider);
         // Get a reference to the AutoCompleteTextView in the layout AutoCompleteTextView
        dep= (AutoCompleteTextView) findViewById(R.id.dep);
        Arr= (AutoCompleteTextView) findViewById(R.id.arrival);
   
         
         
         // Define the marker
         bikeMarker = this.getResources().getDrawable(R.drawable.velo);
        busMarker = this.getResources().getDrawable(R.drawable.bus);
        metroMarker = this.getResources().getDrawable(R.drawable.icon_subway);
        borneMarker = this.getResources().getDrawable(R.drawable.greenpoint);
        trainMarker = this.getResources().getDrawable(R.drawable.train);
 
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
         
         
 	
 	        	
 
 	        
         
 
     }
 
     
     
     /**/
    
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
            // return true;
 
        // case R.id.menu_settings : // not usfull
             // Change activity to settings menu
         case R.id.userLevel :
         {
             
         	final boolean rapid =statesVelo[2];
         	final boolean havefun =statesVelo[1];
         	final boolean balande =statesVelo[0];
         	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
         	    builder.setTitle("Quel est votre niveau en velo?");
         	    builder.setMultiChoiceItems(itemLayers, statesLayers, new DialogInterface.OnMultiChoiceClickListener(){
         	    	
         	        public void onClick(DialogInterface dialogInterface, int item, boolean state) {
         	        }
         	    });
         	    
         	    builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
         	    	
         	        public void onClick(DialogInterface dialog, int id) {
         	        	
         	           
         	        }
         	           
         	    });
         	    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
         	        public void onClick(DialogInterface dialog, int id) {
         	        	 SparseBooleanArray CheCked = ((AlertDialog)dialog).getListView().getCheckedItemPositions();
         	        	 if(CheCked.get(CheCked.keyAt(0)) == false && CheCked.get(CheCked.keyAt(2)) == false || CheCked.get(CheCked.keyAt(0)) == false && CheCked.get(CheCked.keyAt(1)) == false || CheCked.get(CheCked.keyAt(1)) == false && CheCked.get(CheCked.keyAt(1)) == false){
          	            	Toast.makeText(mcontext, "une seul choix possible", Toast.LENGTH_LONG).show();
          	                
          	            }
          	          
         	        	 else{
         	        		 
         	        		    // cache gestion
              	        	dialog.cancel();
         	        		 
         	        	 }
          	            	
          	            	
          	          
          	        
          	        
         	        
         	         
         	        }
         	    });
         	    builder.create().show();
         
         }
         	// if the user is trained
         
         case R.id.layers :
         {
         
         	final boolean velo =statesLayers[2];
         	final boolean metro =statesLayers[1];
         	final boolean bus =statesLayers[0];
         	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
         	    builder.setTitle("Quel layer vous intreresse?");
         	    builder.setMultiChoiceItems(itemLayers, statesLayers, new DialogInterface.OnMultiChoiceClickListener(){
         	    	
         	        public void onClick(DialogInterface dialogInterface, int item, boolean state) {
         	        }
         	    });
         	    
         	    builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
         	    	
         	        public void onClick(DialogInterface dialog, int id) {
         	        	
         	            SparseBooleanArray CheCked = ((AlertDialog)dialog).getListView().getCheckedItemPositions();
         	            
         	            if(CheCked.get(CheCked.keyAt(0)) == true){
         	            	if(!mapView.getOverlays().contains(busItemizedIconOverlay))
         	            	{
         	            		 // Call the method that create a item array
         	                    displayPoint(busArray, "","Bus:", busMarker,busOverlayItemArray,busItemizedIconOverlay);
         	                    mapView.invalidate(); // refresh map
         	                   
         	            	}
         	            }else
         	            {
         	            	if(mapView.getOverlays().contains(busItemizedIconOverlay))
         	            	{
         	            		mapView.getOverlays().remove(busItemizedIconOverlay);
         	            		 mapView.invalidate(); // refresh mapp
         	            	}
         	                
         	            }
         	            
         	            if(CheCked.get(CheCked.keyAt(1)) == true)
         	            {
         	            	if(!mapView.getOverlays().contains(metroItemizedIconOverlay))
         	            	{
         	            		 // Call the method that create a item array
         	                    displayPoint(metroArray, "Metro","Metro:", metroMarker,metroOverlayItemArray,metroItemizedIconOverlay);
         	                    mapView.invalidate(); // refresh map
         	                   
         	            	}
         	            }else
         	            {
         	            	if(mapView.getOverlays().contains(metroItemizedIconOverlay))
         	            	{
         	            		mapView.getOverlays().remove(metroItemizedIconOverlay);
         	            		 mapView.invalidate(); // refresh mapp
         	            	}
         	       
         	        
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
         	        	statesLayers[2]=velo;
         	        	statesLayers[1]=metro;
         	        	statesLayers[0]=bus;
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
 	private static  void displayBikePoint(JSONObject dataJson) {
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
             	String	id	=null;
          
             // Initiate the variable we need
            
             	tmpStation = station.getJSONObject(i);
      
                 // get the value
                 lng           = tmpStation.getDouble("longitude");
                 lat           = tmpStation.getDouble("latitude");
                 name          = tmpStation.getString("name");
                 bikeAvailable = tmpStation.getString("bikesavailable");
                 slotAvailable = tmpStation.getString("slotsavailable");
                 id=tmpStation.getString("number");
                 
                 stopList.put(name, new BasicNameValuePair("Bike",id));
                 
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
            Toast.makeText(mcontext, 
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
 	
      private static void displayPoint(JSONArray dataJson,String transportName,String type, Drawable makerTransport,ArrayList<OverlayItem>  overlayItemArray, ItemizedIconOverlay<OverlayItem>  itemizedIconOverlay) {
     	 try {
        
         OverlayItem marker=null;
         // Create the overlay and add it to the array
         overlayItemArray = new ArrayList<OverlayItem>();
      
      
         // Loop the Array
         for (int i = 0; i < dataJson.length(); i++) {
      
             // We get the data we want
             JSONObject tmpStation;
             String     name          = null;
             double     lng           = 0,
                       lat           = 0;
             String id =null;
      
             // Initiate the variable we need
            
             	tmpStation = dataJson.getJSONObject(i);
      
                 // get the value
             	String lngtmp =tmpStation.getString(transportName+"Stop_lon");
             	if(!lngtmp.isEmpty())
             	{
             	
                 lng           = Double.valueOf(lngtmp).doubleValue();
                 lat           = Double.valueOf(tmpStation.getString(transportName+"Stop_lat")).doubleValue();
                 name          = tmpStation.getString(transportName+"Stop_name");
                 id = tmpStation.getString(transportName+"Stop_id");
                 
                 //complet stop List
                 stopList.put(name, new BasicNameValuePair(type,id));
                 // Create a overlay for a special position
                 marker = new OverlayItem(name,name, new GeoPoint(lat, lng));
                  // Add the graphics to the marker
                  marker.setMarker(makerTransport);
                  
           
                  // Add the marker into the list
                  overlayItemArray.add(marker);
                  
             	}
      		}
         
         
       /// overlays gestion for bike
         
         OnItemGestureListener<OverlayItem> myOnItemGestureListener
             = new OnItemGestureListener<OverlayItem>(){
           // dsiplay  bus information when  user click
           @Override
           public boolean onItemSingleTapUp(int index, OverlayItem item) {
            Toast.makeText(mcontext, 
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
        
         
         itemizedIconOverlay = new ItemizedIconOverlay<OverlayItem>(mcontext, overlayItemArray, myOnItemGestureListener);
        
 
        
     	
         // Add the overlays into the map
         mapView.getOverlays().add(itemizedIconOverlay);
     	   } catch (JSONException e) {
     		     
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
         
       }
      
 
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
      // live maj data
 
     /** ************************************************* */
     public static  void majData(String bike, String bus, String metro, String borne,  String train)
     {
     
     	Log.d("MAJ INFO","new information");
     	
     	 // And the String into Json
         try {
             bikeData = new JSONObject(bike);
             metroArray = new JSONArray(metro);
             busArray = new JSONArray(bus);
             borneArray = new JSONArray(borne);
             trainArray = new JSONArray(train);
            
         } catch (JSONException e) {
             Log.i("ERROR JSON" , e.toString());
         }
        
         /** ************************************************* */
         
         
         /** ******* This display all the bike station ******* */
 
         // Call the method that create a item array
         if(mapView.getOverlays().contains(bikeItemizedIconOverlay))
         mapView.getOverlays().remove(bikeItemizedIconOverlay);
          displayBikePoint(bikeData);
          mapView.invalidate();
          
          //metro
          if(mapView.getOverlays().contains(metroItemizedIconOverlay))
          mapView.getOverlays().remove(metroItemizedIconOverlay);
          displayPoint(metroArray, "Metro","Metro:", metroMarker,metroOverlayItemArray,metroItemizedIconOverlay);
          mapView.invalidate();
          
          //borne
          if(mapView.getOverlays().contains(borneItemizedIconOverlay))
          mapView.getOverlays().remove(borneItemizedIconOverlay);
          displayPoint(borneArray, "Borne","Borne:", borneMarker,borneOverlayItemArray,borneItemizedIconOverlay);
          mapView.invalidate();
          
          
          //Train
          if(mapView.getOverlays().contains(trainItemizedIconOverlay))
          mapView.getOverlays().remove(trainItemizedIconOverlay);
          displayPoint(trainArray, "","Train:" ,trainMarker,trainOverlayItemArray,trainItemizedIconOverlay);
          mapView.invalidate();
          
          
          // remplissage
          stop=new String[stopList.size()];
          stopList.keySet().toArray(stop);
          
          for(int i=0;i<stop.length;i++)
          Log.i("Test","stop"+stop[i]);
          // Create the adapter and set it to the AutoCompleteTextView 
          ArrayAdapter<String> adapterDep = 
                  new ArrayAdapter<String>(mcontext,R.id.content,stop );
          
          adapterDep.setDropDownViewResource(R.id.content);
          dep.setThreshold(1);
          dep.setAdapter(adapterDep);
         
          ArrayAdapter<String> adapterArr = 
                  new ArrayAdapter<String>(mcontext,R.id.content,stop );
          adapterArr.setDropDownViewResource(R.id.content);
          Arr.setThreshold(1);
          Arr.setAdapter(adapterArr);
 
          
        
     }
 
     /** ************************************************* */
 
     
    
 }
 
 
 //~ Formatted by Jindent --- http://www.jindent.com
