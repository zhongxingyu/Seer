 package com.tarea.pubrundan;
 
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.provider.Settings;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.TabHost.OnTabChangeListener;
 import android.widget.Toast;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 import com.tarea.pubrundan.Pubs.JAPripps;
 
 //### This class follows some code examples from http://eagle.phys.utk.edu/guidry/android/index.html  ###//
 
 public class TheMap extends MapActivity {
 
 	private boolean startCampus = true; // false = Lindholmen, true =
 										// Johanneberg (default campus)
 	private int zoomLevel = 17;
 	private MapController mc;
 	private GeoPoint gp;
 	private MapView mapView;
 	private Button getThePositionButton, changeCampusButton, goToPubListButton; // Buttons
 																				// shown
 																				// on
 																				// top
 																				// of
 																				// theMap
 	private MyLocationOverlay myLocationOverlay; // An Overlay showing a "point"
 													// on the map aka your
 													// current position
 
 	private List<Overlay> mapOverlays;
 	private Drawable drawable1, drawable2;
 	private OverlayClass itemizedOverlay1, itemizedOverlay2;
 
 	private final CharSequence[] differentViews = { "Street", "Satellite",
 			"Traffic" }; // selectable map views
 
 	// Define an array containing the access overlay items for all of the pubs
 	// of Johanneberg and Lindholmen
 	// Coordinates need to be converted into integers, by default they are
 	// displayed in microdegrees
 	
 	// The pubs in the array are listed and hardcoded from coordinates_of_the_pubs.txt
 	private OverlayItem[] allPubsArray = {
 			// J.A. Pripps
 			new OverlayItem(new GeoPoint((int) (57.688984 * 1E6),
 					(int) (11.974389 * 1E6)), "J.A. Pripps", "Johanneberg"),
 			// Gasquen
 			new OverlayItem(new GeoPoint((int) (57.688725 * 1E6),
 					(int) (11.975156 * 1E6)), "Gasquen", "Johanneberg"),
 			// Bulten
 			new OverlayItem(new GeoPoint((int) (57.689148 * 1E6),
 					(int) (11.978496 * 1E6)), "Bulten", "Johanneberg"),
 			// Winden
 			new OverlayItem(new GeoPoint((int) (57.688961 * 1E6),
 					(int) (11.978665 * 1E6)), "Winden", "Johanneberg"),
 			// Zaloonen
 			new OverlayItem(new GeoPoint((int) (57.689097 * 1E6),
 					(int) (11.979126 * 1E6)), "Zaloonen", "Johanneberg"),
 			// Club Avancez
 			new OverlayItem(new GeoPoint((int) (57.692219 * 1E6),
 					(int) (11.976862 * 1E6)), "Club Avancez", "Johanneberg"),
 			// GoldenI
 			new OverlayItem(new GeoPoint((int) (57.692939 * 1E6),
 					(int) (11.975242 * 1E6)), "GoldenI", "Johanneberg"),
 			// Hubben 2.1
 			new OverlayItem(new GeoPoint((int) (57.688331 * 1E6),
 					(int) (11.979217 * 1E6)), "Hubben 2.1", "Johanneberg"),
 			// Basen
 			new OverlayItem(new GeoPoint((int) (57.688776 * 1E6),
 					(int) (11.978852 * 1E6)), "Basen", "Johanneberg"),
 			// Kajsabaren
 			new OverlayItem(new GeoPoint((int) (57.688196 * 1E6),
 					(int) (11.978573 * 1E6)), "Kajsabaren", "Johanneberg"),
 			// Jrnvgspub
 			new OverlayItem(new GeoPoint((int) (57.688317 * 1E6),
 					(int) (11.975757 * 1E6)), "Jrnvgspub", "Johanneberg"),
 			// GasTown
 			new OverlayItem(new GeoPoint((int) (57.687935 * 1E6),
 					(int) (11.975918 * 1E6)), "GasTown", "Johanneberg"),
 			// FortNOx
 			new OverlayItem(new GeoPoint((int) (57.687302 * 1E6),
 					(int) (57.687302 * 1E6)), "FortNOx", "Johanneberg"),
 			// Spritkket
 			new OverlayItem(new GeoPoint((int) (57.689587 * 1E6),
 					(int) (11.977978 * 1E6)), "Spritkket", "Johanneberg"),
 			// Focus
 			new OverlayItem(new GeoPoint((int) (57.691001 * 1E6),
 					(int) (11.977591 * 1E6)), "Focus", "Johanneberg"),
 			// Rda rummet
 			new OverlayItem(new GeoPoint((int) (57.689977 * 1E6),
 					(int) (11.976862 * 1E6)), "Rda rummet", "Johanneberg"),
 			// Sigurd/A-fiket
 			new OverlayItem(new GeoPoint((int) (57.687563 * 1E6),
 					(int) (11.976717 * 1E6)), "Sigurd/A-fiket", "Johanneberg"),
 			// Pub P
 			new OverlayItem(new GeoPoint((int) (57.706463 * 1E6),
 					(int) (11.939596 * 1E6)), "Pub P", "Lindholmen"),
 			// 11:an
 			new OverlayItem(new GeoPoint((int) (57.706085 * 1E6),
 					(int) (11.936675 * 1E6)), "11:an", "Johanneberg"),
 
 	};
 
 	// standard onCreate method
 	public void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE); // Suppress title bar for
 														// more space
 		setContentView(R.layout.showthemap); // Use xml-layout showtomap.xml
 		// Add map controller with zoom controls
 		mapView = (MapView) findViewById(R.id.mv);
 		mapView.setSatellite(true); // Satellite is set by default
 		mapView.setBuiltInZoomControls(true); // Set android:clickable=true in
 												// main.xml
 
 		// create an overlay that shows our current location
 		myLocationOverlay = new MyLocationOverlay(this, mapView); // Creates a
 																	// new
 																	// MyLocationOverlay
 																	// for later
 																	// use in
 																	// the code
 		checkIfGpsIsEnabled(); // check if gps is enabled
		changeToCampusJohanneberg(); // change the position to Johanneberg
		showThePubs(); // Displaying all pubs as overlay in maps, see method for
						// more info
		// showTheCurrentPosition(); // navigate to users current location
		// inactive during development
 
 		// Button for get my location
 		getThePositionButton = (Button) findViewById(R.id.getPosition);
 		getThePositionButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				showTheCurrentPosition();
 			}
 		});
 
 		// Button to control access overlay
 		changeCampusButton = (Button) findViewById(R.id.changeCampus);
 		changeCampusButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				if (startCampus) {
 					changeToCampusLindholmen();
 					startCampus = false;
 				} else if (!startCampus) {
 
 					changeToCampusJohanneberg();
 					startCampus = true;
 				}
 			}
 		});
 
 		// Button to goto PubList.java
 		goToPubListButton = (Button) findViewById(R.id.pubList);
 		goToPubListButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				startPubListActivity();
 			}
 		});
 
 	}
 
 	// The map will navigate to your current position
 	public void showTheCurrentPosition() {
 		myLocationOverlay = new MyLocationOverlay(this, mapView);
 		myLocationOverlay.enableMyLocation();
 		mapView.getOverlays().add(myLocationOverlay);
 
 		mc = mapView.getController();
 		myLocationOverlay.runOnFirstFix(new Runnable() {
 			public void run() {
 				mc.setZoom(zoomLevel);
 				mc.animateTo(myLocationOverlay.getMyLocation());
 			}
 		});
 	}
 
 	// display all pubs in the allPubsArray as an overlay onto the map
 	public void showThePubs() {
 
 		// Create itemizedOverlay2 if it doesn't exist
 		if (itemizedOverlay2 == null) {
 			mapOverlays = mapView.getOverlays();
 			drawable2 = this.getResources().getDrawable(R.drawable.icon_pub_location);
 			itemizedOverlay2 = new OverlayClass(drawable2, this);
 		}
 		for (int i = 0; i < allPubsArray.length; i++) {
 
 			itemizedOverlay2.addOverlay(allPubsArray[i]);
 			mapOverlays.add(itemizedOverlay2);
 		}
 		// Added symbols will be displayed when map is redrawn so force redraw
 		// now
 		mapView.postInvalidate();
 	}
 
 	// navigate to campus Lindholmen if users clicks changeCampusButton
 	public void changeToCampusLindholmen() {
 
 		mc = mapView.getController();
 		gp = new GeoPoint((int) (57.705947 * 1e6), (int) (11.936642 * 1e6));
 		mc.animateTo(gp);
 		mc.setZoom(zoomLevel);
 	}
 
 	// navigate to campus Lindholmen if users clicks changeCampusButton
 	public void changeToCampusJohanneberg() {
 
 		mc = mapView.getController();
 		gp = new GeoPoint((int) (57.689034 * 1e6), (int) (11.976468 * 1e6));
 		mc.animateTo(gp);
 		mc.setZoom(zoomLevel);
 	}
 
 	// starting new activity( PubList.java ) if user clicks goToPubListButton
 	public void startPubListActivity() {
 
 		// default PubList.class in development test using PubInfo.class
 		Intent i = new Intent(this, PubList.class); // context = this ,
 													// PubList.class =
 													// ClassToNavigateTo.class
 		startActivity(i);
 	}
 	
 	// starting new activity( PubInfo.java ) if user clicks goToPubListButton
 		public void startPubInfoActivity() {
 
 			// default PubList.class in development test using PubInfo.class
 			Intent i = new Intent(this, JAPripps.class); // context = this ,
 														// PubInfo.class =
 														// ClassToNavigateTo.class
 			startActivity(i);
 		}
 
 	// Required method since class extends MapActivity
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false; // Don't display a route
 	}
 
 	// Initiating Menu XML file (menu.xml)
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		MenuInflater oMenu = getMenuInflater();
 		oMenu.inflate(R.menu.menu, menu);
 		return true;
 	}
 
 	// onOptionsMenu with three items, "Street", "Satellite" & Traffic... code
 	// refactor is needed!
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.change_map:
 			new AlertDialog.Builder(this).setTitle("ndra vy")
 					.setSingleChoiceItems(differentViews, -1, // -1 === fulhack
 																// :|
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int item) {
 									if (item == 0) {
 										mapView.setTraffic(false);
 										mapView.setSatellite(false);
 										mapView.setStreetView(true);
 									}
 
 									else if (item == 1) {
 										mapView.setStreetView(false);
 										mapView.setTraffic(false);
 										mapView.setSatellite(true);
 									}
 
 									else if (item == 2) {
 										mapView.setStreetView(false);
 										mapView.setSatellite(false);
 										mapView.setTraffic(true);
 									}
 									dialog.dismiss();
 									Toast.makeText(TheMap.this,
 											differentViews[item] + " r valt",
 											Toast.LENGTH_SHORT).show();
 								}
 							}).create().show();
 		case R.id.settings:
 			return true;
 
 		case R.id.share:
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		// when our activity resumes, we want to register for location updates
 		myLocationOverlay.enableMyLocation();
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		// when our activity pauses, we want to remove listening for location
 		// updates
 		// onPause, pressing home-button for example the GPS will be turned off,
 		// onResme will start it again
 		// LocationManager manager = (LocationManager)
 		// getSystemService(Context.LOCATION_SERVICE);
 		// manager.removeUpdates(this);
 		// manager = null;
 		myLocationOverlay.disableMyLocation();
 	}
 
 	public void onTabChanged(String tabId) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/** Called when the user press back button in the TheMap */
 	@Override
 	public void onBackPressed() {
 		new AlertDialog.Builder(this).setTitle("Avsluta")
 				// Title: "Avsluta"
 				.setIcon(R.drawable.icon_warning)
 				.setMessage("Sker p att du vill avsluta?") // Message:
 																// "Sker p att du vill avsluta?"
 				.setNegativeButton("Nej", null) // Negative answer button: "Nej"
 												// -> return null aka close
 												// dialog
 				.setNeutralButton("Ja, avsluta!", // Neutral answer button:
 													// "Ja, avsluta" -> exit
 													// current activity
 						new android.content.DialogInterface.OnClickListener() {
 
 							public void onClick(DialogInterface arg0, int arg1) {
 								TheMap.super.onBackPressed();
 								System.exit(0); // if "Ja, avsluta" exit and
 												// remove location/network
 												// services for this app
 							}
 						}).create().show();
 	}
 
 	private void checkIfGpsIsEnabled() {
 		final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 
 		if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
 			NoGpsDialog();
 		}
 	}
 
 	private void NoGpsDialog() {
 		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle("GPS avstngd")
 				.setMessage("Din GPS r avstngd, vill du starta den?")
 				.setIcon(R.drawable.icon_warning)
 				.setCancelable(false)
 				.setNeutralButton("Ja",
 						new android.content.DialogInterface.OnClickListener() {
 							public void onClick(final DialogInterface dialog,
 									final int id) {
 								startActivity(new Intent(
 										Settings.ACTION_LOCATION_SOURCE_SETTINGS));
 							}
 						})
 				.setNegativeButton("Nej",
 						new DialogInterface.OnClickListener() {
 							public void onClick(final DialogInterface dialog,
 									final int id) {
 								dialog.cancel();
 							}
 						});
 		final AlertDialog ad = builder.create();
 		ad.show();
 	}
 
 	public void onLocationChanged(Location location) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void onProviderDisabled(String provider) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void onProviderEnabled(String provider) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
