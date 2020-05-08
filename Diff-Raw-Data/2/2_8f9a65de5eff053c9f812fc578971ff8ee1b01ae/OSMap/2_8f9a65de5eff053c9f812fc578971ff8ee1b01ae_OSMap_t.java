 package edu.bonn.mobilegaming.geoquest.mission;
 
 import java.util.Iterator;
 import java.util.List;
 
 import org.dom4j.DocumentException;
 import org.dom4j.Element;
 import org.osmdroid.api.IMapController;
 import org.osmdroid.api.IMapView;
 import org.osmdroid.tileprovider.tilesource.XYTileSource;
 import org.osmdroid.views.MapView;
 import org.osmdroid.views.overlay.MyLocationOverlay;
 import org.osmdroid.views.overlay.Overlay;
 
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.LinearLayout;
 
 import com.qeevee.util.location.MapHelper;
 import com.qeevee.util.locationmocker.LocationSource;
 
 import edu.bonn.mobilegaming.geoquest.GeoQuestApp;
 import edu.bonn.mobilegaming.geoquest.GeoQuestMapActivity;
 import edu.bonn.mobilegaming.geoquest.HotspotListener;
 import edu.bonn.mobilegaming.geoquest.HotspotOld;
 import edu.bonn.mobilegaming.geoquest.R;
 
 /**
  * OpenStreetMap-based Map Navigation.
  */
 public class OSMap extends MapNavigation implements HotspotListener {
 
    private static String TAG = "OSMap";
 
     // set theese two parameters to use Cloudmade Style
     private static String APIKey = null; // eg
 					 // "6f218baf0ee44fdc9a9563c37e55851e"
     private static String CmStyleId = null; // eg "63694"
 
     // Menu IDs:
     static final private int FIRST_LOCAL_MENU_ID = GeoQuestMapActivity.MENU_ID_OFFSET;
     static final private int LOCATION_MOCKUP_SWITCH_ID = FIRST_LOCAL_MENU_ID;
     static final private int START_AR_VIEW_ID = FIRST_LOCAL_MENU_ID + 1;
     static final private int CENTER_MAP_ON_CURRENT_LOCATION_ID = FIRST_LOCAL_MENU_ID + 2;
 
     private MapView myMapView;
     private MyLocationOverlay myLocationOverlay;
 
     private LinearLayout startMissionPanel;
 
     /**
      * used by the android framework
      * 
      * @return false
      */
     @Override
     protected boolean isRouteDisplayed() {
 	return false;
     }
 
     /**
      * Called when the activity is first created. Setups google mapView, the map
      * overlays and the listeners
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
 	super.onCreate(savedInstanceState);
 	setContentView(R.layout.osmap);
 
 	// Setup Google Map0View
 	myMapView = (MapView) findViewById(R.id.osmapview);
 	myMapView.setBuiltInZoomControls(false);
 	if (APIKey != null
 		&& CmStyleId != null) {
 	    myMapView.setTileSource(new XYTileSource("cmMap", null, 0, 15, 256,
 		    ".png", "http://tile.cloudmade.com/"
 			    + APIKey
 			    + "/"
 			    + CmStyleId
 			    + "/256/"));
 	}
 
 	// myMapView.displayZoomControls(false);
 
 	mapHelper = new MapHelper(this);
 	mapHelper.centerMap();
 
 	initZoom();
 	initGPSMock();
 
 	// startMissionsList
 	startMissionPanel = (LinearLayout) findViewById(R.id.startMissionPanel);
 
 	// Players Location Overlay
 	myLocationOverlay = new MyLocationOverlay(this, myMapView);
 	myLocationOverlay.enableCompass(); // doesn't work in the emulator?
 	myLocationOverlay.enableMyLocation();
 	myMapView.getOverlays().add(myLocationOverlay);
 
 	GeoQuestApp.getInstance().setOSMap(myMapView);
 
 	// Show loading screen to Parse the Game XML File
 	// indirectly calls onCreateDialog() and initializes hotspots
 	showDialog(READXML_DIALOG);
 
 	mission.applyOnStartRules();
 
     }
 
     /**
      * called by the android framework when the activity gets inactive. Disables
      * the myLocationOverlay listeners.
      */
     @Override
     protected void onPause() {
 	super.onPause();
 	myLocationOverlay.disableCompass();
 	myLocationOverlay.disableMyLocation();
     }
 
     /**
      * called by the android framework when the activity gets active. Registers
      * the myLocationOverlay listeners.
      */
     @Override
     protected void onDestroy() {
 	if (myLocationManager != null)
 	    myLocationManager.removeUpdates(mapHelper.getLocationListener());
 	GeoQuestApp.getInstance().setGoogleMap(null);
 	super.onDestroy();
     }
 
     @Override
     protected void onResume() {
 	super.onResume();
 	myLocationOverlay.enableCompass();
 	myLocationOverlay.enableMyLocation();
     }
 
     /**
      * Called when the activity's options menu needs to be created.
      */
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
 	super.onCreateOptionsMenu(menu);
 
 	menu.add(0,
 		 LOCATION_MOCKUP_SWITCH_ID,
 		 0,
 		 R.string.map_menu_mockGPS);
 	menu.add(0,
 		 START_AR_VIEW_ID,
 		 0,
 		 R.string.startARViewMenu);
 	menu.add(0,
 		 CENTER_MAP_ON_CURRENT_LOCATION_ID,
 		 0,
 		 R.string.map_menu_centerMap);
 	return true;
     }
 
     /**
      * Called right before your activity's option menu is displayed.
      */
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
 	super.onPrepareOptionsMenu(menu);
 	menu.getItem(LOCATION_MOCKUP_SWITCH_ID - 1)
 		.setEnabled(locationSource != null
 			&& LocationSource.canBeUsed(getApplicationContext()));
 	return true;
     }
 
     /**
      * Called when a menu item is selected.
      */
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
 	switch (item.getItemId()) {
 	case LOCATION_MOCKUP_SWITCH_ID:
 	    if (locationSource.getMode() == LocationSource.REAL_MODE) {
 		// From REAL mode to MOCK mode:
 		locationSource.setMode(LocationSource.MOCK_MODE);
 		item.setTitle(R.string.map_menu_realGPS);
 	    } else {
 		// From MOCK mode to REAL mode:
 		locationSource.setMode(LocationSource.REAL_MODE);
 		item.setTitle(R.string.map_menu_mockGPS);
 	    }
 	    break;
 	case CENTER_MAP_ON_CURRENT_LOCATION_ID:
 	    mapHelper.centerMap();
 	    break;
 	}
 
 	return super.onOptionsItemSelected(item);
     }
 
     /**
      * On click listener to start the mission from a hotspot when the user taps
      * on the corresponding button
      */
     public class StartMissionOnClickListener implements OnClickListener {
 
 	public void onClick(View v) {
 	    HotspotOld h = (HotspotOld) v.getTag();
 	    h.runOnTapEvent();
 	}
 
     }
 
     /**
      * Hotspot listener method. Is called when the player enters a hotspots
      * interaction circle. A button to start the mission from the hotspot is
      * shown.
      */
     public void onEnterRange(HotspotOld h) {
 	Log.d(TAG,
 	      "Enter Hotspot with id: "
 		      + h.id);
     }
 
     /**
      * Hotspot listener method. Is called when the player leaves a hotspots
      * interaction circle. The button to start the mission of the hotspot
      * dislodged from the view.
      */
     public void onLeaveRange(HotspotOld h) {
 	Log.d(TAG,
 	      "Leave Hotspot with id: "
 		      + h.id);
 	// Find the Child, which equals the given hotspot:
 	int numButtons = startMissionPanel.getChildCount();
 	View childView = null;
 	for (int i = 0; i < numButtons; i++) {
 	    if (startMissionPanel.getChildAt(i).getTag().equals(h)) {
 		childView = startMissionPanel.getChildAt(i);
 		break;
 	    }
 	}
 	// Remove this child:
 	if (childView != null) {
 	    startMissionPanel.removeView(childView);
 	}
 
     }
 
     static final int READXML_DIALOG = 0;
     ProgressDialog readxmlDialog;
     ReadxmlThread readxmlThread;
     boolean readxml_completed = false;// true when xml is parsed completely.
 
     // while false main thread may not
     // access 'hotspots'
 
     protected Dialog onCreateDialog(int id) {
 	switch (id) {
 	case READXML_DIALOG:
 	    readxmlDialog = new ProgressDialog(this);
 	    readxmlDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 	    readxmlDialog.setMessage(getString(R.string.map_loading));
 	    readxmlThread = new ReadxmlThread(readxmlHandler);
 	    readxmlThread.start();
 	    return readxmlDialog;
 	default:
 	    return null;
 	}
     }
 
     /**
      * Define the Handler that receives messages from the thread and update the
      * progressbar
      */
     final Handler readxmlHandler = new Handler() {
 	public void handleMessage(Message msg) {
 	    int progress = msg.getData().getInt("progress");
 	    int max = msg.getData().getInt("max");
 	    boolean finish = msg.getData().getBoolean("finish");
 
 	    if (progress != 0)
 		readxmlDialog.setProgress(progress);
 	    if (max != 0)
 		readxmlDialog.setMax(max);
 
 	    if (finish) {
 		// new hotspots were not added to myMapView.getOverlays();
 		// this would cause a crash in nonmain thread; so this is done
 		// here
 		List<Overlay> mapOverlays = myMapView.getOverlays();
 		for (Iterator<HotspotOld> iterator = getHotspots().iterator(); iterator
 			.hasNext();) {
 		    HotspotOld hotspot = (HotspotOld) iterator.next();
 		    mapOverlays.add(hotspot.getOSMOverlay());
 		}
 		// mapOverlays.addAll(hotspots);
 
 		dismissDialog(READXML_DIALOG);
 		readxml_completed = true;
 	    }
 	}
     };
 
     /** Nested class that performs reading xml */
     private class ReadxmlThread extends Thread {
 	Handler mHandler;
 
 	ReadxmlThread(Handler h) {
 	    mHandler = h;
 	}
 
 	public void run() {
 
 	    try {
 		readXML();
 	    } catch (DocumentException e) {
 		e.printStackTrace();
 		Log.e("Error",
 		      "XML Error");
 	    }
 
 	    Message msg = mHandler.obtainMessage();
 	    Bundle b = new Bundle();
 	    b.putBoolean("finish",
 			 true);
 	    msg.setData(b);
 	    mHandler.sendMessage(msg);
 
 	    if (locationSource != null)
 		locationSource.setMode(LocationSource.REAL_MODE);
 
 	}
 
 	/**
 	 * Gets the child Hotspots data from the XML file.
 	 */
 	@SuppressWarnings("unchecked")
 	private synchronized void readXML() throws DocumentException {
 	    List<Element> list = mission.xmlMissionNode
 		    .selectNodes("hotspots/hotspot");
 
 	    int j = 0;
 	    for (Iterator<Element> i = list.iterator(); i.hasNext();) {
 		Element hotspot = i.next();
 		try {
 		    HotspotOld newHotspot = HotspotOld.create(mission,
 							      hotspot);
 		    newHotspot.addHotspotListener(OSMap.this);
 		    getHotspots().add(newHotspot);
 		    // new hotspots are not added to myMapView.getOverlays();
 		    // this would course a crash in nonmain thread;
 		    // readxmlHandler will add them later
 		} catch (HotspotOld.IllegalHotspotNodeException exception) {
 		    Log.e("MapOverview.readXML",
 			  exception.toString());
 		}
 
 		Message msg = mHandler.obtainMessage();
 		Bundle b = new Bundle();
 		b.putInt("progress",
 			 ++j);
 		b.putInt("max",
 			 list.size());
 		msg.setData(b);
 		mHandler.sendMessage(msg);
 	    }
 	}
     }
 
     /** Intent used to return values to the parent mission */
     protected Intent result;
 
     @Override
     public IMapView getMapView() {
 	return myMapView;
     }
 
     @Override
     public IMapController getMapController() {
 	return myMapView.getController();
     }
 }
