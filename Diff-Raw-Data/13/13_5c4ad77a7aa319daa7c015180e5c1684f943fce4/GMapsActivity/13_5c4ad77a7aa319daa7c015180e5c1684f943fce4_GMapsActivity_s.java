 package com.doLast.doGRT;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.graphics.drawable.Drawable;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.provider.Settings;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockMapActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.doLast.doGRT.database.DatabaseSchema;
 import com.doLast.doGRT.database.DatabaseSchema.StopsColumns;
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 import com.readystatesoftware.mapviewballoons.BalloonItemizedOverlay;
 
 public class GMapsActivity extends SherlockMapActivity implements LocationListener {
 	/**
 	 * Simple itemized overlay item class
 	 */	
 	public class PinItemizedOverlay extends BalloonItemizedOverlay {
 	   	Context mContext = null;
 	   	String stop_id = null;
 	   	String stop_name = null;
 	   	
 		private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
 		
 		public PinItemizedOverlay(Drawable defaultMarker) {
 			super(boundCenterBottom(defaultMarker), mapView);
 		}
 		
 		public PinItemizedOverlay(Drawable defaultMarker, Context context) {
 			  super(boundCenterBottom(defaultMarker), mapView);
 			  mContext = context;
 			}		
 		
 		@Override
 		protected OverlayItem createItem(int i) {
 			return mOverlays.get(i);
 		}
 
 		@Override
 		public int size() {
 			return mOverlays.size();
 		}
 		
 		@Override
 		protected boolean onBalloonTap(int index, OverlayItem item) {
 			if (item.getTitle() != CURRENT_LOCATION) {
 				stop_id = item.getSnippet();
 				stop_name = item.getTitle();
 				// Switch to route display
 	        	Intent routes_intent = new Intent(mContext, RoutesActivity.class);
 	        	// Pack stop id with the intent
 	        	routes_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 	        	routes_intent.putExtra(RoutesActivity.SCHEDULE_TYPE, RoutesActivity.SCHEDULE_MIXED);
 	        	routes_intent.putExtra(RoutesActivity.STOP_ID, stop_id);
 	        	routes_intent.putExtra(RoutesActivity.STOP_NAME, stop_name);
 	        	routes_intent.putExtra(RoutesActivity.STOP_TITLE, stop_name);
 	        	startActivity(routes_intent);
 			}			
 			return super.onBalloonTap(index, item);
 		}		
 		
 		@Override
 		public boolean onTouchEvent(MotionEvent event, MapView mapView) {
 			switch(event.getAction()){
 			case MotionEvent.ACTION_UP:
 				// Actively update the stops
 				dropPins(mapView.getMapCenter(), true);
 			default:
 				return super.onTouchEvent(event, mapView);
 			}
 		}
 
 		public void addOverlay(OverlayItem overlay) {
 		    mOverlays.add(overlay);
 		    populate();
 		}				
 		
 		// Clear all overlay items
 		public void clearOverlayItems() {
 			mOverlays.clear();
 			populate();
 		}
 	}
 	
 	// Number of stops to be displayed
     private final int MAX_STOPS_IN_MAP = 50;
     // Dialog IDs
 	private final int GPS_ALERT_DIALOG_ID = 0;
 	private final int BUS_STOPS_DIALOG_ID = 1;
 	// For locating stop
 	public static final String LOCATE = "locate";
 	private String stop_id = null;
 	
 	
     private MapView mapView;
     // Location manager
     private LocationManager location_manager = null;
     private String location_provider = null;
     
     // Map controller
     private MapController map_controller = null;
     
     // GPS
     private final String ASK_GPS = "ask_gps";
     private boolean ask_gps;
     private GeoPoint waterloo = new GeoPoint(43468798,-80539179); // University of Waterloo
     private int zoom_level = 17;
     private int stop_delta = 7000;
     private Location cur_location = null;
     private GeoPoint cur_location_point = null;
     
     // Overlay items
     private List<Overlay> mapOverlays = null;
     private Drawable red_drawable = null;
     private Drawable green_drawable = null;
     private PinItemizedOverlay itemized_overlay = null;
     private MyLocationOverlay cur_overlay = null;
     private final String CURRENT_LOCATION = "Current Location";
     public static int BALLOON_PLACE_OFFSET = 30;
     
     // Perference setting
     public static final String PREFS_NAME = "map_preference";
     private SharedPreferences settings = null;
     
     // Cursors
     //private Cursor stop = null;
     //private Cursor stops = null;    		
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.map_view);
         
         mapView = (MapView) findViewById(R.id.map_view);
         mapView.setBuiltInZoomControls(true);        
         
         // Use the "navigate up" button
         ActionBar action_bar = getSupportActionBar();
         action_bar.setDisplayHomeAsUpEnabled(true);
                 
         // Get the current location
         location_manager = (LocationManager) getSystemService(LOCATION_SERVICE);
         Criteria criteria = new Criteria();
         location_provider = location_manager.getBestProvider(criteria, false);
         if (location_provider != null) {
         	//Log.v("Found provider", location_provider);
         } else {
         	Toast.makeText(this, "Unable to find a proper provider", Toast.LENGTH_SHORT).show();
         }
         
         // Setting center to current location or University of Waterloo
         cur_overlay = new MyLocationOverlay(this, mapView);
         map_controller = mapView.getController();
 	    // Move to current location, if one exist
 	    cur_location = location_manager.getLastKnownLocation(location_provider);
 	    if (cur_location != null) {
 	    	cur_location_point = new GeoPoint((int)(cur_location.getLatitude() * 1e6), 
 					  							(int)(cur_location.getLongitude() * 1e6));
 		    map_controller.setCenter(cur_location_point);
 	    } else {
 	    	cur_location_point = new GeoPoint(waterloo.getLatitudeE6(), waterloo.getLongitudeE6());
 	    	map_controller.setCenter(waterloo);
 	    }
         map_controller.setZoom(zoom_level);
         
         /* If no data is given in the Intent that started this Activity, then this Activity
          * was started when the intent filter matched a MAIN action. We should use the default
          * provider URI.
          */
         // Gets the intent that started this Activity.
         Intent intent = getIntent();
 
         // If there is no data associated with the Intent, sets the data to the default URI, which
         // accesses a list of notes.
         if (intent.getData() == null) {
             intent.setData(DatabaseSchema.StopsColumns.CONTENT_URI);
         }           
         
         // Setup overlay items
         mapOverlays = mapView.getOverlays();        
         red_drawable = this.getResources().getDrawable(R.drawable.red_marker);
         green_drawable = this.getResources().getDrawable(R.drawable.green_marker);
         itemized_overlay = new PinItemizedOverlay(red_drawable, this);
         itemized_overlay.setBalloonBottomOffset(BALLOON_PLACE_OFFSET);        
         
         // Restore preference
         settings = getSharedPreferences(PREFS_NAME, 0);
         ask_gps = settings.getBoolean(ASK_GPS, true);
         
         // Check if user enabled GPS
         checkGPS();               
 
         // Retrieve stop id
         Bundle extras = intent.getExtras();
         GeoPoint center = null;
                 
         if (extras != null) {
         	// Move to the location of given stop id
         	stop_id = extras.getString(LOCATE);
             String[] projection = { StopsColumns.STOP_LAT, StopsColumns.STOP_LON };
             String selection = StopsColumns.STOP_ID + " = " + stop_id;
             Cursor stop = managedQuery(StopsColumns.CONTENT_URI, projection, selection, null, null);
             stop.moveToFirst();
             center = new GeoPoint((int)(stop.getDouble(0) * 1e6), (int)(stop.getDouble(1) * 1e6));
             map_controller.setCenter(center);
         } else {          	
         	center = mapView.getMapCenter();
         }
         
         // Drop pins
         dropPins(center, true);     
        if (extras != null) itemized_overlay.onTap(center, mapView); // Tap the center stop if trying to locate        
         mapView.postInvalidate();
     }
         
     @Override
 	protected void onPause() {
 		super.onPause();
 		/* Remove the locationlistener updates when Activity is paused */
 		location_manager.removeUpdates(this);
 		cur_overlay.disableMyLocation();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		/* Request updates at startup */
 	    location_manager.requestLocationUpdates(location_provider, 400, 1, this);
 		cur_overlay.enableMyLocation();
 	}		
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 	}
 
 	@Override
     protected boolean isRouteDisplayed() {
         return false;
     }
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		new MenuInflater(this).inflate(R.menu.map_option_menu, menu);
 		
 		return super.onCreateOptionsMenu(menu);
 	}    
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {        
         case R.id.current_location:
         	// Request current location
         	if (cur_overlay.getMyLocation() != null) cur_location_point = cur_overlay.getMyLocation();
         	map_controller.animateTo(cur_location_point);
         	dropPins(cur_location_point, false);
         	return true;
         case android.R.id.home:
         	// Go back to favourite list
         	Intent main_intent = new Intent(this, MainActivity.class);
         	main_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
         	startActivity(main_intent);
         	return true;
         }
  
         return super.onOptionsItemSelected(item);
     }
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 	    Dialog dialog = null;
 	    switch(id) {
 	    case GPS_ALERT_DIALOG_ID:
 	    	// Create alert dialog for asking user to enable gps
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			LayoutInflater inflater = LayoutInflater.from(this);
 			View layout = inflater.inflate(R.layout.gps_alert_dialog, null);
 			// Register checkbox listener
 			final CheckBox do_not_display = (CheckBox)layout.findViewById(R.id.gps_check_box);
 			do_not_display.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 				@Override
 				public void onCheckedChanged(CompoundButton buttonView,
 						boolean isChecked) {
     				if (isChecked) {
     					SharedPreferences.Editor editor = settings.edit();
     					editor.putBoolean(ASK_GPS, false);
     					editor.commit();
     				}
 				}			
 			});
 			
 			builder.setView(layout)
 					.setTitle(R.string.enable_gps)					
 					.setMessage(R.string.enable_gps_dialog)					
 	        		.setNegativeButton(R.string.enable_gps, new DialogInterface.OnClickListener() {
 	        			@Override
 	        			public void onClick(DialogInterface dialog, int which) {
 
 	        				enableLocationSettings();              	
 	        			}
 	        		})
 	        		.setPositiveButton(R.string.no, new DialogInterface.OnClickListener() {
 	        			@Override
 	        			public void onClick(DialogInterface dialog, int which) {
 	        				// Do nothing
 	        			}
 	        		});
 	        dialog = builder.create();
 	        break;
 	    default:
 	        dialog = null;
 	    }
 	    return dialog;
 	}
 
 	/**
      * Drop pins at bus stops on the area around the center point
      */
     private void dropPins(GeoPoint center, boolean clearPins) {
     	// Update the delta value
     	stop_delta = stop_delta + 1000 * ((int)(Math.pow(2, zoom_level - mapView.getZoomLevel())) - 1);
     	zoom_level = mapView.getZoomLevel();
     	
     	// Setup query projection and selection
         String[] projection = { DatabaseSchema.StopsColumns.STOP_ID + " as _id", DatabaseSchema.StopsColumns.STOP_NAME,
         		DatabaseSchema.StopsColumns.STOP_LAT, DatabaseSchema.StopsColumns.STOP_LON };
         String selection = new String(
         		DatabaseSchema.StopsColumns.STOP_LAT + " >= " + ((center.getLatitudeE6() - stop_delta ) / 1E6) + " and " +
         		DatabaseSchema.StopsColumns.STOP_LAT + " <= " + ((center.getLatitudeE6() + stop_delta ) / 1E6) + " and " +
         		DatabaseSchema.StopsColumns.STOP_LON + " >= " + ((center.getLongitudeE6() - stop_delta ) / 1E6) + " and " +
         		DatabaseSchema.StopsColumns.STOP_LON + " <= " + ((center.getLongitudeE6() + stop_delta ) / 1E6)
         		);
         
         Cursor stops = managedQuery(
         		DatabaseSchema.StopsColumns.CONTENT_URI, projection, selection, null, null);
         
         // Overlay items
         if (clearPins) {
             itemized_overlay.clearOverlayItems();
             if (stops.getCount() > 0) {
     	        stops.moveToFirst();        
     	        // Display all bus stops up to MAX_STOPS_IN_MAP stops
     	        for(int i = 0; i < stops.getCount() && i < MAX_STOPS_IN_MAP; i += 1) {
     	        	GeoPoint point = new GeoPoint((int)(stops.getDouble(2) * 1E6), (int)(stops.getDouble(3) * 1E6));
     	            OverlayItem overlayitem = new OverlayItem(point, stops.getString(1), stops.getString(0));            
     	            itemized_overlay.addOverlay(overlayitem);
     	        	stops.moveToNext();
     	        }
             }
         	mapOverlays.clear();
 	        mapOverlays.add(itemized_overlay);
             // Always add the current location to the map
         	mapOverlays.add(cur_overlay);
         }
         //stops.close();
     }
     
     /**
      * Method for checking whether user's GPS is on 
      */
     private void checkGPS() {
     	boolean enabled = location_manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
     	// Check if enabled and if not send user to the GSP settings
     	// Better solution would be to display a dialog and suggesting to 
     	// go to the settings
     	if (!enabled && ask_gps) {
             // Build an alert dialog here that requests that the user enable
             // the location services, then when the user clicks the "OK" button,
             // call enableLocationSettings()
     		showDialog(GPS_ALERT_DIALOG_ID);
     	}
     }
     
     // Method to launch Settings
     private void enableLocationSettings() {
         Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
         startActivity(settingsIntent);
     }
     
 	@Override
 	public void onLocationChanged(Location location) {
         // Drop pins if location changes
         //dropPins(mapView.getMapCenter()); 
         //mapView.postInvalidate();
 	}
 
 	@Override
 	public void onProviderDisabled(String provider) {
 		Toast.makeText(this, "Disabled provider " + provider,
 				Toast.LENGTH_SHORT).show();
 	}
 
 	@Override
 	public void onProviderEnabled(String provider) {
 	    Toast.makeText(this, "Enabled new provider " + provider,
 	            Toast.LENGTH_SHORT).show();
 	}
 
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		// Nothing to do here
 	}
 }
