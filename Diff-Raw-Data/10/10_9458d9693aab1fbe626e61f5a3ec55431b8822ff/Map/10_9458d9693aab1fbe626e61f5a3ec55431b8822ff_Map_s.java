 /**
  *   Copyright (C) 2010, Roger Kind Kristiansen <roger@kind-kristiansen.no>
  *
  *   This program is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   This program is distributed in the hope that it will be useful,
  *   
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package no.rkkc.bysykkel.views;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.NoSuchElementException;
 
 import no.rkkc.bysykkel.OsloCityBikeAdapter;
 import no.rkkc.bysykkel.R;
 import no.rkkc.bysykkel.RackInfoDialog;
 import no.rkkc.bysykkel.Constants.FindRackCriteria;
 import no.rkkc.bysykkel.OsloCityBikeAdapter.OsloCityBikeException;
 import no.rkkc.bysykkel.db.DbAdapter;
 import no.rkkc.bysykkel.db.RackDbAdapter;
 import no.rkkc.bysykkel.model.Rack;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.graphics.Canvas;
 import android.graphics.drawable.Drawable;
 import android.location.Location;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Looper;
 import android.os.Message;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.Window;
 import android.widget.Toast;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.ItemizedOverlay;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 import com.google.android.maps.OverlayItem;
 
 public class Map extends MapActivity {
 	MapView map;
 	MyLocationOverlay myLocation;
 	MapController mapController;
 	RackDbAdapter db;
 	OsloCityBikeAdapter osloCityBikeAdapter;
 	
 	GeoPoint savedLocation;
 	int savedZoomLevel;
 	
 	static final int DIALOG_DBINIT = 0; // Progressbar when initializing the database the first time the app is run.
 	static final int DIALOG_SEARCHING_BIKE = 1; // Progressbar when searching for ready bikes
 	static final int DIALOG_SEARCHING_SLOT = 2; // Progressbar when searching for free slots
 	static final int DIALOG_COMMUNICATION_ERROR = 3; // Something has failed during communication with servers
 	
 	private static final String TAG = "Bysyklist-Map";
 	
 	Handler toastHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			Toast.makeText(getBaseContext(), msg.getData().getString("text"), msg.getData().getInt("time")).show();
 		}
 	};
 	
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         db = (RackDbAdapter) new RackDbAdapter(Map.this).open();
         osloCityBikeAdapter = new OsloCityBikeAdapter();
 
         if (!isFirstRun()) {
         	requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
         	setProgressBarIndeterminateVisibility(true);
         }
         
         setContentView(R.layout.main);
         
         map = (MapView)findViewById(R.id.map);
         map.setBuiltInZoomControls(true);
         mapController = map.getController();
 
         setupMyLocation(savedInstanceState);
     	
         if (isFirstRun()) {
         	setupFirstRun();
         } else {
         	initializeMap();
         	setProgressBarIndeterminateVisibility(false);
         }
     }
 
     /**
      * Checks if this is the first time the app has been run.
      * 
      * @return boolean
      */
     private boolean isFirstRun() {
     	if (!db.hasRackData()) {
     		return true;
     	} else {
     		return false;
     	}
     }
     
 	/**
 	 * Initialize default location + zoom for user interface, and retrieve info on all racks and save in database. 
 	 */
 	private void setupFirstRun() {
 		showDialog(DIALOG_DBINIT);
 			
 		new Thread(new Runnable(){
 			public void run() {
 				Looper.prepare();
 
 				showOsloOverview();
 					
 				initializeDb(db);
 				dismissDialog(DIALOG_DBINIT);
 				
         		initializeMap();
 			}
 
 			}).start();
 	}
 
 	/**
 	 * Display overview of Oslo. Used when no fix before GPS/GSM has been acquired.
 	 */
 	private void showOsloOverview() {
 		// Show standard location (Overview of Oslo) 
 		mapController.setZoom(13);
 		mapController.setCenter(new GeoPoint((int)(59.924653*1E6), (int) (10.731071*1E6)));
 	}
 	
 	/**
 	 * 
 	 */
 	private void setupMyLocation(Bundle savedInstanceState) {
 		myLocation = new MyLocationOverlay(this, map);
 		myLocation.enableMyLocation();
 		map.getOverlays().add(myLocation);
 
 		if (savedInstanceState != null) {
 			GeoPoint point = new GeoPoint((int)savedInstanceState.getFloat("Latitude"), (int)savedInstanceState.getFloat("Longitude"));
 			mapController.setZoom(savedInstanceState.getInt("ZoomLevel"));
 			mapController.setCenter(point);
 		} else {
 	        GeoPoint recentLocation = myLocation.getMyLocation();
 			if (recentLocation != null) {
 				mapController.animateTo(recentLocation);
 			} else {
 				showOsloOverview();
 			}
 	        myLocation.runOnFirstFix(new Runnable() {
 				public void run() {
 					mapController.setZoom(16);
 					animateToMyLocation();
 				}
 	        });
 		}
 	}
     
 	/**
 	 * Set up the map with the overlay containing the bike rack represantions
 	 */
     private void initializeMap() {
         RacksOverlay rackOverlay = initializeRackOverlay(db.getRacks());
 		map.getOverlays().add(rackOverlay);  
 		map.postInvalidate();
     }
 
 	/**
 	 * Initial setup of the overlay, defining the pin. 
 	 * 
 	 * @return {@link RacksOverlay}
 	 */
 	private RacksOverlay initializeRackOverlay(ArrayList<Rack> racks) {
 		Drawable icon = getResources().getDrawable(R.drawable.bubble);
 		icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon
 				.getIntrinsicHeight());
         RacksOverlay rackOverlay = new RacksOverlay(icon, racks);
 		return rackOverlay;
 	}
 
 	/**
 	 * On first run, populate the database with rack information
 	 * 
 	 * @param db
 	 * @param ocbAdapter
 	 */
 	private void initializeDb(DbAdapter db) {
 		ArrayList<Integer> rackIds = new ArrayList<Integer>();
 		try {
 			rackIds = osloCityBikeAdapter.getRacks();
 			addRacksToDb(rackIds);
 		} catch (OsloCityBikeException e) {
 			Log.e("FetchRackData", "Communication with ClearChannel failed.", e);
 			// TODO: Show error dialog to user?		
 		}
 	}
     
     @Override
     protected void onRestart() {
     	super.onRestart();
     	db.open();
     }
 	
 	@Override
     protected void onStart() {
     	super.onStart();
     }
     
     @Override
     protected void onResume() {
     	super.onResume();
     	myLocation.enableMyLocation();
 //    	animateToMyLocation();
     }
     
     @Override
     protected void onPause() {
     	super.onPause();
     	savedLocation = map.getMapCenter();
     	savedZoomLevel = map.getZoomLevel();
     	myLocation.disableMyLocation();
     }
     
     @Override
     protected void onStop() {
     	super.onStop();
     	db.close();
     }
     
     @Override
     public void onSaveInstanceState(Bundle savedInstanceState) {
     	savedInstanceState.putInt("ZoomLevel",  map.getZoomLevel());
     	savedInstanceState.putFloat("Latitude", map.getMapCenter().getLatitudeE6());
     	savedInstanceState.putFloat("Longitude", map.getMapCenter().getLongitudeE6());
     	
     	super.onSaveInstanceState(savedInstanceState);
     }
     
     @Override
     public void onRestoreInstanceState(Bundle savedInstanceState) {
     	super.onRestoreInstanceState(savedInstanceState);
     }
     
     @Override
     protected Dialog onCreateDialog(int id) {
     	switch (id) {
     		case DIALOG_DBINIT:
     			ProgressDialog initDialog = new ProgressDialog(this);
     			initDialog.setTitle(getString(R.string.initdialog_title));
     			initDialog.setMessage(getString(R.string.initdialog_message));
     			initDialog.setIndeterminate(true);
     			initDialog.setCancelable(false);
     			
     			return initDialog;
     		case DIALOG_SEARCHING_BIKE:
     			ProgressDialog bikeSearchDialog = new ProgressDialog(this);
     			String message = String.format(getString(R.string.searchdialog_message_first), 
     					getString(R.string.word_bike));
     			bikeSearchDialog.setMessage(message);
     			bikeSearchDialog.setIndeterminate(true);
     			bikeSearchDialog.setCancelable(true);
     			
     			return bikeSearchDialog;
     		case DIALOG_SEARCHING_SLOT:
     			ProgressDialog slotSearchDialog = new ProgressDialog(this);
     			String slotMessage = String.format(getString(R.string.searchdialog_message_first), 
     					getString(R.string.word_slot));
     			slotSearchDialog.setMessage(slotMessage);
     			slotSearchDialog.setIndeterminate(true);
     			slotSearchDialog.setCancelable(true);
     			
     			return slotSearchDialog;
 //    			
 //    		case DIALOG_COMMUNICATION_ERROR:
 //    			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 //    			builder.setTitle("Kommunikasjonsfeil");
 //    			builder.setMessage("Kommunikasjon med ClearChannels servere har feilet. Forsøk igjen.");
 //    			builder.setPositiveButton("OK", null);
 //    			builder.setCancelable(false);
 //    			
 //    			return builder.create();
     	}
     	
     	return super.onCreateDialog(id);
     }
     
 
 	/**
 	 * @param db
 	 * @param ocbAdapter
 	 * @param rackIds
 	 * @throws Exception 
 	 */
 	private void addRacksToDb(ArrayList<Integer> rackIds) throws OsloCityBikeException {
 		// Add racks to database
 		for (Integer rackId : rackIds) {
 			Rack rack;
 			try {
 				Log.v(TAG, "Inserting rack into DB. ID: ".concat(rackId.toString()));
 				rack = osloCityBikeAdapter.getRack(rackId);
 				db.insertRack(rack);
 			} catch (OsloCityBikeException e) {
 				Log.e(TAG, e.getStackTrace().toString());
 				db.clearRacks(); // Remove rack data from database, so that db-initialization is retried on next startup.
 				// TODO: Show a dialog informing the user of the error in stead of rethrowing?
 				throw e;
 			}
 		}
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 	
 	/* Menu */
 	public boolean onCreateOptionsMenu(Menu menu) {
 	    MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.icon_menu, menu);
 	    return true;
 	}
 	
 	/* Handles menu item selections */
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    switch (item.getItemId()) { 
 		    case R.id.my_location:
 		    	animateToMyLocation();
 		        return true;
 //		    case R.id.favorites:
 //		    	startActivity(new Intent(this, Favorites.class));
 //		    	return true;
 		    case R.id.nearest_bike:
 		    	searchForClosestRack(FindRackCriteria.ReadyBike);
 				return true;
 		    case R.id.nearest_slot:
 		    	searchForClosestRack(FindRackCriteria.FreeSlot);
 				return true;
 	    }
 	    return false;
 	}
 
 	/**
 	 * 
 	 */
 	private void animateToMyLocation() {
 		GeoPoint point = myLocation.getMyLocation();
 		if (point != null) {
 			mapController.animateTo(point);
 		}
 	}
 	
 	/**
 	 * Retrieves the closest rack according to criteria and modifies UI accordingly
 	 * 
 	 * @param criteria
 	 */
 	public void searchForClosestRack(final FindRackCriteria criteria) {
 		
 		final Handler mDialogHandler = new Handler() {
 			@Override
 			public void handleMessage(Message msg) {
 				final int dialog;
 				if (criteria == FindRackCriteria.FreeSlot) {
 					dialog = DIALOG_SEARCHING_SLOT;
 				} else {
 					dialog = DIALOG_SEARCHING_BIKE;
 				}
 				
 				if (msg.getData().getBoolean("show")) {
 					showDialog(dialog);
 				} else {
 					dismissDialog(dialog);
 				}
 			}
 		};
 		
 		new Thread(new Runnable(){
 			public void run() {
 				Looper.prepare();
 				
 				// Show progress dialog
 				Message msg = mDialogHandler.obtainMessage();
 				Bundle bundle = new Bundle();
 				bundle.putBoolean("show", true);
 				msg.setData(bundle);
 				mDialogHandler.sendMessage(msg);
 				
 				Rack nearestRackWithSlotOrBike = findClosestRack(criteria);
 				
 				// Hide progress dialog
 				msg = mDialogHandler.obtainMessage();
 				bundle = new Bundle();
 				bundle.putBoolean("hide", true);
 				msg.setData(bundle);
 				mDialogHandler.sendMessage(msg);
 				
 				if (nearestRackWithSlotOrBike == null) {
 					Log.w(Map.TAG, "Could not find nearest rack during search");
 					
 					// Show toast informing of the error
 					msg = toastHandler.obtainMessage();
 					bundle = new Bundle();
 					bundle.putString("text", getText(R.string.error_search_failed).toString());
 					bundle.putInt("time", Toast.LENGTH_SHORT);
 					msg.setData(bundle);
 					toastHandler.sendMessage(msg);
 					
 					return;
 				}
 
 				RacksOverlay overlay = ((RacksOverlay) map.getOverlays().get(1));
 				int overlayIndex = overlay.findOverlayIndex(nearestRackWithSlotOrBike.getId());
 				
 				Drawable marker_normal = getResources().getDrawable(R.drawable.bubble);
 				marker_normal.setBounds(-marker_normal.getIntrinsicWidth()/2, 
 						-marker_normal.getIntrinsicHeight(), 
 						marker_normal.getIntrinsicWidth()/2, 0);
 				Drawable marker_highlighted = getResources().getDrawable(R.drawable.bubble_highlighted);
 				marker_highlighted.setBounds(-marker_highlighted.getIntrinsicWidth()/2, 
 						-marker_highlighted.getIntrinsicHeight(), 
 						marker_highlighted.getIntrinsicWidth()/2, 0);
 				
 				overlay.getItem(overlayIndex).setMarker(marker_highlighted);
 			
 				mapController.animateTo(nearestRackWithSlotOrBike.getLocation());
 				
 				int noOfFreeItems;;
 				String itemType;
 				if (criteria == FindRackCriteria.FreeSlot) {
 					noOfFreeItems = nearestRackWithSlotOrBike.getNumberOfEmptySlots();
 					if (noOfFreeItems > 1) {
 						itemType = getText(R.string.word_slots).toString();
 					} else {
 						itemType = getText(R.string.word_slot).toString();
 					}
 				} else {
 					noOfFreeItems = nearestRackWithSlotOrBike.getNumberOfReadyBikes();
 					if (noOfFreeItems > 1) {
 						itemType = getText(R.string.word_bikes).toString();
 					} else {
 						itemType = getText(R.string.word_bike).toString();
 					}
 				}
 				
 				// Show toast informing of number of free bikes/slots
 				msg = toastHandler.obtainMessage();
 				bundle = new Bundle();
 				bundle.putString("text", Integer.toString(noOfFreeItems) + " " + itemType); // A message like "x free slot(s)"
 				bundle.putInt("time", Toast.LENGTH_SHORT);
 				msg.setData(bundle);
 				toastHandler.sendMessage(msg);
 
 				try {
 					Thread.sleep(5000);
 				} catch (InterruptedException e) {
 					// Don't do anything. The finally-clause will revert to previous state anyway.
 				} finally {
 					overlay.getItem(overlayIndex).setMarker(marker_normal);
 					map.postInvalidate();
 				}
 				Looper.loop();
 			}
 			}).start();		
 	}
 	
 	public Rack findClosestRack(FindRackCriteria criteria) {
 		// TODO: Do we want to provide closest station from currently centered, or perhaps from a longpress?
 		
 		Location center = myLocation.getLastFix(); 
 		if (center == null) {
 			// No location has been set.
 			return null;
 		}
 		
 		List<LocationAndDistance> sortedStationLocations = new ArrayList<LocationAndDistance>();
 		for (Rack rack : db.getRacks()) {
 			Log.v(Map.TAG, rack.toString());
 			Location loc = new Location("Bysyklist");
 			loc.setLatitude(rack.getLocation().getLatitudeE6() / 1E6);
 			loc.setLongitude(rack.getLocation().getLongitudeE6() / 1E6);
 			sortedStationLocations.add(new LocationAndDistance(rack, center.distanceTo(loc)));
 		}
 		Collections.sort(sortedStationLocations);
 
 		// Find first matching station
 		Rack foundRack = null;
 		Rack rack = null;
 		for (LocationAndDistance lad : sortedStationLocations) {
 			try {
 				rack = osloCityBikeAdapter.getRack(lad.getStationIndex());
 				if ((criteria == FindRackCriteria.ReadyBike && rack.getNumberOfReadyBikes() > 0)
 						|| (criteria == FindRackCriteria.FreeSlot && 
 								rack.getNumberOfEmptySlots() > 0)) {
 					foundRack = rack;
                     Log.v(Map.TAG, "Found station:" + foundRack);
 					break;
 				}
 			} catch (OsloCityBikeException e) {
 				// TODO: find a way to display the fact that some nearer stations don't have status information available
 				Log.w(Map.TAG, "Didn't get info on number of ready bikes and free locks");
 				Log.w(Map.TAG, e.getStackTrace().toString());
 				continue;
 			}
 		}
 		
 		return foundRack;
 	}
 	
 	private static class LocationAndDistance implements Comparable<LocationAndDistance> {
 		private Rack rack;
 		// private Location location;
 		private float distanceInMeters;
 		
 		public LocationAndDistance(Rack rack, float distanceInMeters) {
 			this.rack = rack;
 			// this.location = location;
 			this.distanceInMeters = distanceInMeters;
 		}
 		
 		public int compareTo(LocationAndDistance another) {
 			return (int) (this.distanceInMeters - another.distanceInMeters);
 		}
 		
 		public int getStationIndex() {
 			return rack.getId();
 		}
 	}
 	
 	private class RacksOverlay extends ItemizedOverlay<OverlayItem> {
 		private Drawable marker=null;
 		private List<OverlayItem> items = new ArrayList<OverlayItem>();
 		private ArrayList<Rack> racks;
 		
 		public RacksOverlay(Drawable marker, ArrayList<Rack> racks) {
 			super(marker);
 			this.marker = marker;
 			this.racks = racks;
 			boundCenterBottom(marker);
 			
 			populate();
 		}
 		
 		@Override
 		protected OverlayItem createItem(int i) {
 			Rack rack = racks.get(i);
 			Log.v(Map.TAG, "Adding rack "+rack.getId() + " to overlay");
 			OverlayItem item = new OverlayItem(rack.getLocation(), rack.getDescription(), Integer.toString(rack.getId()));
 			items.add(item);
 
 			return item;
 		}
 
 		@Override
 		public void draw(Canvas canvas, MapView mapView, boolean shadow) {
 			super.draw(canvas, mapView, shadow);
 		}
 
 		@Override
 		public int size() {
 			return racks.size();
 		}
 		
 		@Override
 		protected boolean onTap(int i) {
 			Rack rack = findRack(i);
 			Dialog dialog = new RackInfoDialog(Map.this, rack.getDescription(), rack.getId());
 			dialog.show();
 			
 			return true;
 		}
 		
 		private Rack findRack(int overlayIndex) {
 			Rack rack = null;
 			for (int i = 0; i < racks.size(); i++) {
 				rack = racks.get(i);
 				int rackId = rack.getId();
 				int overlayIndexRackId = Integer.parseInt(items.get(overlayIndex).getSnippet());
 				
 				if (rackId == overlayIndexRackId) {
 					return rack;
 				}
 			}
 			
 			// This should never occur
 			throw new NoSuchElementException("Overlay with index " + overlayIndex
 					+ " doesn't exists as rack");
 		}
 		
 		private int findOverlayIndex(int rackId) {
 			for (int i = 0; i < this.size(); i++) {
 				if (rackId == Integer.parseInt(items.get(i).getSnippet())) {
 					return i;
 				}
 			}
 			
 			// This should never occur
 			throw new NoSuchElementException("Overlay with rack " + rackId
 					+ " doesn't exists");
 		}
 	}
 }
