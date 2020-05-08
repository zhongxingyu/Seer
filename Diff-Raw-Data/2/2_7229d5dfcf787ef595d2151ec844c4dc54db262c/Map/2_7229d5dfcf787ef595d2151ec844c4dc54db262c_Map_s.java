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
 
 import no.rkkc.bysykkel.LongpressHelper;
 import no.rkkc.bysykkel.OsloCityBikeAdapter;
 import no.rkkc.bysykkel.R;
 import no.rkkc.bysykkel.Toaster;
 import no.rkkc.bysykkel.Constants.FindRackCriteria;
 import no.rkkc.bysykkel.OsloCityBikeAdapter.OsloCityBikeException;
 import no.rkkc.bysykkel.db.FavoritesDbAdapter;
 import no.rkkc.bysykkel.db.RackDbAdapter;
 import no.rkkc.bysykkel.model.Rack;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.DialogInterface.OnClickListener;
 import android.graphics.Canvas;
 import android.graphics.drawable.Drawable;
 import android.location.Location;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Looper;
 import android.os.SystemClock;
 import android.text.method.LinkMovementMethod;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.Window;
 import android.widget.ImageButton;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.ItemizedOverlay;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 import com.google.android.maps.OverlayItem;
 
 public class Map extends MapActivity {
 	private MapView mapView;
 	private MyLocationOverlay myLocation;
 	private MapController mapController;
 	private RackDbAdapter rackDb;
 	private FavoritesDbAdapter favoritesDb;
 	private OsloCityBikeAdapter osloCityBikeAdapter;
 	private ViewHolder viewHolder = new ViewHolder();
 	private RacksOverlay rackOverlay; 
 	
 	private GeoPoint contextMenuGeoPoint = null;
 	private LongpressHelper contextMenuHelper = new LongpressHelper();
 	
 	GeoPoint savedLocation;
 	int savedZoomLevel;
 	
 	static final int DIALOG_RACKSYNC = 0; // Progressbar when initializing the database the first time the app is run.
 	static final int DIALOG_SEARCHING_BIKE = 1; // Progressbar when searching for ready bikes
 	static final int DIALOG_SEARCHING_SLOT = 2; // Progressbar when searching for free slots
 	static final int DIALOG_COMMUNICATION_ERROR = 3; // Something has failed during communication with servers
 	static final int DIALOG_ABOUT = 4;
 	
 	private static final String TAG = "Bysyklist-Map";
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         rackDb = new RackDbAdapter(Map.this).open();
         favoritesDb = new FavoritesDbAdapter(Map.this).open();
         osloCityBikeAdapter = new OsloCityBikeAdapter();
         requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 
         setContentView(R.layout.map);
         setupMapView();
         setupInfoPanel();
         setupMyLocation(savedInstanceState);
 
         if (isFirstRun()) {
     		new RackSyncTask().execute((Void[])null);
     		Log.v("Test", "bla");
         	showOsloOverview();
         } else {
         	setProgressBarIndeterminateVisibility(true);
         	initializeMap();
         	setProgressBarIndeterminateVisibility(false);
         }
     }
 
 	@Override
     protected void onRestart() {
     	super.onRestart();
     }
 	
 	@Override
     protected void onStart() {
     	super.onStart();
     	rackDb.open();
     	favoritesDb.open();
     }
     
     @Override
     protected void onResume() {
     	super.onResume();
     	myLocation.enableMyLocation();
     }
     
     @Override
     protected void onPause() {
     	super.onPause();
     	savedLocation = mapView.getMapCenter();
     	savedZoomLevel = mapView.getZoomLevel();
     	myLocation.disableMyLocation();
     }
     
     @Override
     protected void onStop() {
     	super.onStop();
     	rackDb.close();
     	favoritesDb.close();
     }
     
     @Override
     public void onSaveInstanceState(Bundle savedInstanceState) {
     	savedInstanceState.putInt("ZoomLevel",  mapView.getZoomLevel());
     	savedInstanceState.putFloat("Latitude", mapView.getMapCenter().getLatitudeE6());
     	savedInstanceState.putFloat("Longitude", mapView.getMapCenter().getLongitudeE6());
     	
     	super.onSaveInstanceState(savedInstanceState);
     }
     
     @Override
     public void onRestoreInstanceState(Bundle savedInstanceState) {
     	super.onRestoreInstanceState(savedInstanceState);
     }
     
     @Override
 	protected Dialog onCreateDialog(int id) {
 		switch (id) {
 			case DIALOG_RACKSYNC:
 				ProgressDialog initDialog = new ProgressDialog(this);
 				initDialog.setMessage(getString(R.string.syncdialog_message));
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
 			case DIALOG_ABOUT:
 				View view = View.inflate(Map.this, R.layout.scrollable_textview, null);
 				TextView textView = (TextView) view.findViewById(R.id.message);
 				textView.setMovementMethod(LinkMovementMethod.getInstance());
 				textView.setText(R.string.content_about);
 				
 				AlertDialog.Builder builder = new AlertDialog.Builder(this);
 				builder.setView(view)
 						.setTitle(getString(R.string.about_app))
 						.setNeutralButton("Lukk", new OnClickListener() {
 						public void onClick(DialogInterface dialog, int which) {
 							dialog.cancel();
 						}
 					   });
				return builder.create().show();
 		}
 		
 		return super.onCreateDialog(id);
 	}
 
     @Override
 	public void onCreateContextMenu(ContextMenu  menu, View  v, ContextMenu.ContextMenuInfo menuInfo) {
 		menu.add(Menu.NONE, Menu.FIRST, Menu.NONE, getString(R.string.menu_nearest_bike));
 		menu.add(Menu.NONE, Menu.FIRST+1, Menu.NONE, getString(R.string.menu_nearest_slot));
 	}
 
 	public boolean onContextItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case Menu.FIRST:
 				new ShowNearestRackTask(FindRackCriteria.ReadyBike, contextMenuGeoPoint).execute();
 				return true;
 			case Menu.FIRST+1:
 				new ShowNearestRackTask(FindRackCriteria.FreeSlot, contextMenuGeoPoint).execute();
 				return true;
 		}
 			
 		return super.onContextItemSelected(item);
 	}
 
 	/* Menu */
 	public boolean onCreateOptionsMenu(Menu menu) {
 	    MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.map_menu, menu);
 	    return true;
 	}
 
 	/* Handles menu item selections */
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    switch (item.getItemId()) { 
 		    case R.id.menuitem_my_location:
 		    	animateToMyLocation();
 		        return true;
 		    case R.id.menuitem_rack_sync:
 		    	new RackSyncTask().execute((Void[])null);
 		    	return true;
 		    case R.id.menuitem_nearest_bike:
 				new ShowNearestRackTask(FindRackCriteria.ReadyBike).execute();
 				return true;
 		    case R.id.menuitem_nearest_slot:
 		    	new ShowNearestRackTask(FindRackCriteria.FreeSlot).execute();
 				return true;
 		    case R.id.menuitem_favorites:
 		    	startActivity(new Intent(this, Favorites.class));
 		    	return true;
 		    case R.id.menuitem_about:
 		    	showDialog(DIALOG_ABOUT);
 		    	return true;
 	    }
 	    return false;
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 	
 	/**
 	 * Override dispatchTouchEvent to catch a longpress anywhere on the map and display a 
 	 * context menu.
 	 * 
 	 */
 	@Override
 	public boolean dispatchTouchEvent(MotionEvent event) {
 		catchLongPress(event);
 		
 		if (event.getAction() == MotionEvent.ACTION_DOWN) {
 			hideRackInfo();
 		}
 		
 		return super.dispatchTouchEvent(event);
 	}
 
 	private boolean isFirstRun() {
 		SharedPreferences settings = getPreferences(MODE_PRIVATE);
 		if (settings.getLong("racksUpdatedTime", -1) == -1) {
 			return true;
 		} 
 		
 		return false;
 	}
 
 	private void setupMapView() {
 	    // Set up map view
 	    mapView = (MapView)findViewById(R.id.mapview);
 	    mapView.setBuiltInZoomControls(true);
 	    registerForContextMenu(mapView);
 	    mapController = mapView.getController();
 	}
 
 	private void setupInfoPanel() {
 		viewHolder.infoPanel = (RackInfoPanel) findViewById(R.id.infoPanel);
 		viewHolder.infoPanel.setVisibility(View.GONE);
 	    viewHolder.name = (TextView) viewHolder.infoPanel.findViewById(R.id.name);
 	    viewHolder.information = (TextView) findViewById(R.id.information);
 	}
 
 	private void setupMyLocation(Bundle savedInstanceState) {
 		myLocation = new MyLocationOverlay(this, mapView);
 		myLocation.enableMyLocation();
 		mapView.getOverlays().add(myLocation);
 	
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
 	 * Display overview of Oslo. Used when no fix before GPS/GSM has been acquired.
 	 */
 	private void showOsloOverview() {
 		// Show standard location (Overview of Oslo) 
 		mapController.setZoom(13);
 		mapController.setCenter(new GeoPoint((int)(59.924653*1E6), (int) (10.731071*1E6)));
 	}
 
 	/**
 	 * Set up the map with the overlay containing the bike rack represantions
 	 */
 	private void initializeMap() {
 		rackOverlay = initializeRackOverlay(rackDb.getRacks());
 		mapView.getOverlays().add(rackOverlay);  
 		mapView.postInvalidate();
 	}
 
 	/**
 	 * Initial setup of the overlay, defining the pin. 
 	 * 
 	 * @return {@link RacksOverlay}
 	 */
 	private RacksOverlay initializeRackOverlay(ArrayList<Rack> racks) {
 		Drawable default_marker = getResources().getDrawable(R.drawable.bubble);
 		default_marker.setBounds(0, 0, default_marker.getIntrinsicWidth(), default_marker
 				.getIntrinsicHeight());
 		Drawable highlighted_marker = getResources().getDrawable(R.drawable.bubble_highlighted);
 		highlighted_marker.setBounds(0, 0, highlighted_marker.getIntrinsicWidth(), highlighted_marker
 				.getIntrinsicHeight());
 		
 	    RacksOverlay rackOverlay = new RacksOverlay(default_marker, highlighted_marker, racks);
 		return rackOverlay;
 	}
 
 	/**
 	 * Handles calling of the context menu, if longpress is detected on map.
 	 * 
 	 * Takes a MotionEvent as argument, and if method is not called again with an event that
 	 * indicates that this is anything but a longpress, a message is sent to display the context
 	 * menu. Any event except MotionEvent.ACTION_DOWN will reset the longpress detection.
 	 * 
 	 * @param event	as passed into dispatchTouchEvent.
 	 */
 	private void catchLongPress(MotionEvent event) {
 		if (event.getAction() == MotionEvent.ACTION_DOWN) { // New touch has been detected
 			final MotionEvent touchEvent = event;
 			new Thread(new LongpressDetector(touchEvent)).start();
 		} else {
 			contextMenuHelper.handleMotionEvent(event);
 		}
 	}
 
 	/**
 	 * Stores event location for usage by the context menu
 	 * 
 	 * @param event
 	 */
 	private void storeEventLocationForContextMenu(MotionEvent event) {
 		contextMenuGeoPoint = mapView.getProjection().fromPixels((int)event.getX(), 
 				(int)event.getY());
 	
 		
 	    
 	}
 	
 	/**
 	 * @param closestRackWithSlotOrBike
 	 */
 	private void animateToRack(Rack closestRackWithSlotOrBike) {
 		mapController.animateTo(closestRackWithSlotOrBike.getLocation());
 	}
 
 	/**
 	 * 
 	 */
 	private void animateToMyLocation() {
 		new Thread(new Runnable(){
 			public void run() {
 				 Looper.prepare();
 				
 				GeoPoint location = getMyCurrentLocation();
 				
 				if (location != null) {
 					mapController.animateTo(location);
 				}
 				
 			}
 			}).start();
 	}
 	
 	
 	/**
 	 * @return
 	 */
 	private GeoPoint getMyCurrentLocation() {
 
 		GeoPoint location = myLocation.getMyLocation();
 		
 		// Times in seconds
 		int retryTime = 10;
 		int retryTimeElapsed = 0;
 
 		// If we don't have a location, try for retryTime seconds before giving up
 		while (location == null && retryTimeElapsed/20 < retryTime) {
 			if (retryTimeElapsed == 0) {
 				Toaster.toast(Map.this, R.string.location_waiting, Toast.LENGTH_SHORT);
 			}
 	
 			SystemClock.sleep(200);
 			
 			location = myLocation.getMyLocation();
 			retryTimeElapsed++;
 		}
 		
 		if (location == null) {
 			Toaster.toast(Map.this, R.string.location_not_found, Toast.LENGTH_SHORT);
 		}
 		
 		return location;
 	}
 
 	public Rack getClosestRack(GeoPoint searchPoint, FindRackCriteria criteria) {
 		if (searchPoint == null) {
 			// No location to search from was specified.
 			return null;
 		}
 		
 		List<LocationAndDistance> sortedStationLocations = new ArrayList<LocationAndDistance>();
 		for (Rack rack : rackDb.getRacks()) {
 			Log.v(Map.TAG, rack.toString());
 			
 			Location rackLocation = new Location("Bysyklist");
 			rackLocation.setLatitude(rack.getLocation().getLatitudeE6() / 1E6);
 			rackLocation.setLongitude(rack.getLocation().getLongitudeE6() / 1E6);
 			
 			Location searchLocation = new Location("Bysyklist");
 			searchLocation.setLatitude(searchPoint.getLatitudeE6() / 1E6);
 			searchLocation.setLongitude(searchPoint.getLongitudeE6() / 1E6);
 			
 			sortedStationLocations.add(new LocationAndDistance(rack, searchLocation.distanceTo(rackLocation)));
 		}
 		Collections.sort(sortedStationLocations);
 
 		// Find first matching station
 		Rack foundRack = null;
 		Rack rack = null;
 		for (LocationAndDistance lad : sortedStationLocations) {
 			try {
 				rack = osloCityBikeAdapter.getRack(lad.getStationIndex());
 				
 				if (!rack.hasBikeAndSlotInfo()) continue; // Sometimes we get no information from the rack, so just skip it.
 				
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
 	
 	public void showRackInfo(Rack rack) {
 		viewHolder.name.setText(rack.getDescription());
 		viewHolder.information.setText(R.string.rackdialog_fetching);
 		viewHolder.infoPanel.setRackId(rack.getId());
 		viewHolder.infoPanel.setVisibility(View.VISIBLE);
 		viewHolder.infoPanel.getStatusInfo();
 		
 		favoritesDb.incrementCounter(rack.getId());
 	}
 	
 	public void hideRackInfo() {
 		if (viewHolder.infoPanel != null) {
 			rackOverlay.resetHighlighting();
 			viewHolder.infoPanel.setVisibility(View.GONE);
 		}
 	}
 	
 	/**
 	 * @param nearestRackWithSlotOrBike
 	 * @return
 	 */
 	public void highlightRack(Integer rackId, final Integer duration) {
 		rackOverlay.highlightRack(rackId);
 		
 		if (duration == null) {
 			return;
 		}
 
 		new Thread(new Runnable() {
 			public void run() {
 				try {
 					Thread.sleep(duration);
 				} catch (InterruptedException e) {
 					// Don't do anything. The finally-clause will revert to previous state anyway.
 				} finally {
 					rackOverlay.resetHighlighting();
 					mapView.postInvalidate();
 				}
 			}
 		});
 		
 		favoritesDb.incrementCounter(rackId);
 	}
 	
 	public void highlightRack(Integer rackId) {
 		highlightRack(rackId, null);
 	}
 	
 	private class LongpressDetector implements Runnable {
 		private final MotionEvent touchEvent;
 
 		private LongpressDetector(MotionEvent touchEvent) {
 			this.touchEvent = touchEvent;
 		}
 
 		public void run() {
 			Looper.prepare();
 			if (contextMenuHelper.isLongPressDetected()) {
 				// Store event location for usage by context menu actions
 				storeEventLocationForContextMenu(touchEvent);
 				
 				// Show the context menu
 				runOnUiThread(new Runnable() {
 					public void run() {
 						mapView.showContextMenu();								
 					}
 				});
 			}
 		}
 	}
 
 	private class LocationAndDistance implements Comparable<LocationAndDistance> {
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
 		private List<OverlayItem> items = new ArrayList<OverlayItem>();
 		private ArrayList<Rack> racks;
 		private Integer highlightedIndex;
 		private Drawable default_marker;
 		private Drawable highlight_marker;
 		
 		public RacksOverlay(Drawable default_marker, Drawable highlight_marker, ArrayList<Rack> racks) {
 			super(default_marker);
 			this.racks = racks;
 			
 			setupMarkers(default_marker, highlight_marker);
 			
 			populate();
 		}
 		
 		private void setupMarkers(Drawable default_marker, Drawable highlight_marker) {
 			boundCenterBottom(default_marker);
 			boundCenterBottom(highlight_marker);
 			
 			this.default_marker = default_marker;
 			this.highlight_marker = highlight_marker;
 		}
 		
 		public void highlightRack(int rackId) {
 			highlightIndex(findOverlayIndex(rackId));
 		}
 		
 		public void highlightIndex(int overlayIndex) {
 			highlightedIndex = overlayIndex;
 			getItem(overlayIndex).setMarker(highlight_marker);
 		}
 
 		public void resetHighlighting() {
 			if (highlightedIndex != null) {
 				getItem(highlightedIndex).setMarker(default_marker);
 				highlightedIndex = null;
 			}
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
 			
 			highlightRack(rack.getId());
 			showRackInfo(rack);
 			
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
 	
 		private class ShowNearestRackTask extends AsyncTask<Object, Void, Void> {
 		int dialogId;
 		FindRackCriteria criteria;
 		GeoPoint geoPoint;
 		Rack nearestRack;
 		
 		public ShowNearestRackTask(FindRackCriteria criteria, GeoPoint geoPoint) {
 			super();
 			
 			if (criteria == FindRackCriteria.ReadyBike) {
 				this.dialogId = DIALOG_SEARCHING_BIKE;
 			} else {
 				this.dialogId = DIALOG_SEARCHING_SLOT;
 			}
 			this.criteria = criteria;
 			this.geoPoint = geoPoint;
 			
 		}
 		
 		public ShowNearestRackTask(FindRackCriteria criteria) {
 			this(criteria, null);
 		}
 
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 			
 			hideRackInfo();
 			rackOverlay.resetHighlighting();
 			showDialog(dialogId);
 		}
 		
 		@Override
 		protected void onPostExecute(Void result) {
 			super.onPostExecute(result);
 			
 			dismissDialog(dialogId);
 			
 			if (nearestRack == null) {
 				// No rack found. Inform user and exit.
 				Toaster.toast(Map.this, R.string.error_search_failed, Toast.LENGTH_SHORT);
 			} else {
 				highlightRack(nearestRack.getId(), 3000);
 				animateToRack(nearestRack);
 				
 				// Show stats for the closest rack
 				Toaster.toast(Map.this, getRackInfoText(nearestRack, criteria), Toast.LENGTH_SHORT);
 			}
 		}
 
 		@Override
 		protected Void doInBackground(Object... params) {
 			// Establish location to search from
 			GeoPoint searchPoint;
 			if (geoPoint == null) {
 				searchPoint = getMyCurrentLocation();
 			} else {
 				searchPoint = geoPoint;
 			}
 
 			if (searchPoint == null) {
 				return null;
 			}
 			
 			nearestRack = getClosestRack(searchPoint, criteria);
 			return null;
 		}
 		
 		/**
 		 * Constructs string that is to be displayed to user when rack with bikes or free locks has been found
 		 * 
 		 * @param rack
 		 * @param criteria
 		 * @return
 		 */
 		private String getRackInfoText(Rack rack, FindRackCriteria criteria) {
 			final int noOfFreeItems;;
 			final String itemType;
 			if (criteria == FindRackCriteria.FreeSlot) {
 				noOfFreeItems = rack.getNumberOfEmptySlots();
 				if (noOfFreeItems > 1) {
 					itemType = getText(R.string.word_slots).toString();
 				} else {
 					itemType = getText(R.string.word_slot).toString();
 				}
 			} else {
 				noOfFreeItems = rack.getNumberOfReadyBikes();
 				if (noOfFreeItems > 1) {
 					itemType = getText(R.string.word_bikes).toString();
 				} else {
 					itemType = getText(R.string.word_bike).toString();
 				}
 			}
 			
 			return Integer.toString(noOfFreeItems) + " " + itemType;
 		}
 		
 	}
 	
 	/**
 	 * Task responsible for inserting/updating all racks according to information retrieved
 	 * from the Clear Channel servers. 
 	 *
 	 */
 	private class RackSyncTask extends AsyncTask<Void, Integer, Boolean> {
 		private ArrayList<Integer> failedRackIds = new ArrayList<Integer>();
 		
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 			showDialog(DIALOG_RACKSYNC);
 		}
 
 		@Override
 		protected void onPostExecute(Boolean result) {
 			super.onPostExecute(result);
 			initializeMap();
 			dismissDialog(DIALOG_RACKSYNC);
 			if (!result) {
            		AlertDialog.Builder builder = new AlertDialog.Builder(Map.this);
     			builder.setMessage("En feil oppsto under oppdateringen. Du kan oppdatere stativene senere, eller forsøke på nytt nå.")
     			       .setCancelable(false)
     			       .setPositiveButton("Forsøk igjen", new DialogInterface.OnClickListener() {
     			           public void onClick(DialogInterface dialog, int id) {
     			                new RackSyncTask().execute((Void [])null);
     			           }
     			       })
     			       .setNeutralButton("Senere", new DialogInterface.OnClickListener() {
     			           public void onClick(DialogInterface dialog, int id) {
     			                dialog.cancel();
     			           }
     			       });
     			builder.create().show();
 			}
 			saveRackUpdatePreference();
 		}
 
 		@Override
 		protected Boolean doInBackground(Void... arg0) {
 			OsloCityBikeAdapter osloCityBikeAdapter = new OsloCityBikeAdapter();
 			ArrayList<Integer> localRackIds = rackDb.getRackIds();
 
 			try {
 				ArrayList<Integer> remoteRackIds = osloCityBikeAdapter.getRacks();
 				
 				// Delete racks in DB that are not returned from server
 				if (remoteRackIds.size() > 100) {// Safeguard, in case Clear Channel returns empty list
 					for (Integer rackId : localRackIds) {
 						if (!remoteRackIds.contains(rackId)) {
 							Log.v(TAG, "Deleting rack with ID ".concat(Integer.toString(rackId)).concat(", as it was not returned by server."));
 							favoritesDb.deleteFavorite(rackId);
 							rackDb.deleteRack(rackId);
 						}
 					}
 				}
 				
 				// Update or insert racks returned from server
 				Rack remoteRack;
 				Rack localRack;
 				for (int rackId: remoteRackIds) {
 					try {
 						remoteRack = osloCityBikeAdapter.getRack(rackId);
 					} catch (OsloCityBikeException e) {
 						failedRackIds.add(rackId);
 						continue;
 					}
 					if (rackDb.hasRack(rackId)) {
 						// Update
 						localRack = rackDb.getRack(rackId);
 						localRack.setDescription(remoteRack.getDescription());
 						localRack.setLocation(remoteRack.getLocation());
 						
 						rackDb.updateOrInsertRack(localRack);
 					} else {
 						// Insert
 						rackDb.updateOrInsertRack(remoteRack);
 					}
 				}
 			} catch (OsloCityBikeAdapter.OsloCityBikeCommunicationException e) {
 				return false;
 			}
 			
 			if (failedRackIds.size() > 0) {
 				Log.v(TAG, "test");
 				return false;
 			}
 			
 			return true;
 		}
 		
 		/**
 		 * Saves current time in the preferences, to keep track of when the racks list was last
 		 * updated
 		 */
 		private void saveRackUpdatePreference() {
 			SharedPreferences settings = getPreferences(MODE_PRIVATE);
 			settings.edit().putLong("racksUpdatedTime", System.currentTimeMillis()).commit();
 		}
 	}
 	
 
     static class ViewHolder {
         ImageButton list;
 
         RackInfoPanel infoPanel;
         TextView name;
         TextView information;
     }
 	
 }
