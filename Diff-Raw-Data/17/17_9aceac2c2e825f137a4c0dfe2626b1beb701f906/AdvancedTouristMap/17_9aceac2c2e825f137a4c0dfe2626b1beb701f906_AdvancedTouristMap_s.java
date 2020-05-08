 /*
  * Copyright 2010, 2011 mapsforge.org
  *
  *	This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.muxe.advancedtouristmap;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.Iterator;
 
 import org.mapsforge.android.maps.ArrayCircleOverlay;
 import org.mapsforge.android.maps.ArrayItemizedOverlay;
 import org.mapsforge.android.maps.ArrayWayOverlay;
 import org.mapsforge.android.maps.CircleOverlay;
 import org.mapsforge.android.maps.GeoPoint;
 import org.mapsforge.android.maps.ItemizedOverlay;
 import org.mapsforge.android.maps.MapActivity;
 import org.mapsforge.android.maps.MapController;
 import org.mapsforge.android.maps.MapDatabase;
 import org.mapsforge.android.maps.MapView;
 import org.mapsforge.android.maps.MapView.TextField;
 import org.mapsforge.android.maps.MapViewMode;
 import org.mapsforge.android.maps.Overlay;
 import org.mapsforge.android.maps.OverlayCircle;
 import org.mapsforge.android.maps.OverlayItem;
 import org.mapsforge.core.GeoCoordinate;
 import org.mapsforge.core.Vertex;
 import org.mapsforge.poi.PointOfInterest;
 import org.muxe.advancedtouristmap.overlay.GenericOverlay;
 import org.muxe.advancedtouristmap.overlay.PoiOverlayItem;
 import org.muxe.advancedtouristmap.overlay.PositionOverlayItem;
 import org.muxe.advancedtouristmap.overlay.WikiOverlayItem;
 import org.muxe.advancedtouristmap.poi.PoiBrowserActivity;
 import org.muxe.advancedtouristmap.routing.DecisionOverlay;
 import org.muxe.advancedtouristmap.routing.Route;
 import org.muxe.advancedtouristmap.routing.RouteCalculator;
 import org.muxe.advancedtouristmap.routing.RouteList;
 import org.muxe.advancedtouristmap.sourcefiles.FileManagerActivity;
 import org.muxe.advancedtouristmap.sourcefiles.MapBundle;
 import org.muxe.advancedtouristmap.wikipedia.ArticleRetriever;
 import org.muxe.advancedtouristmap.wikipedia.ArticleRetrieverFactory;
 import org.muxe.advancedtouristmap.wikipedia.WikiArticleInterface;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.CompressFormat;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.graphics.drawable.AnimationDrawable;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.location.LocationProvider;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.PowerManager;
 import android.os.PowerManager.WakeLock;
 import android.util.Log;
 import android.util.TimingLogger;
 import android.view.GestureDetector;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.SubMenu;
 import android.view.View;
 import android.view.View.MeasureSpec;
 import android.view.View.OnClickListener;
 import android.view.WindowManager;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ProgressBar;
 import android.widget.SeekBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * A map application which uses the features from the mapsforge library. The map
  * can be centered to the current GPS coordinate. A simple file browser for
  * selecting the map file is also included. Some preferences can be adjusted via
  * the EditPreferences activity and screenshots of the map may be taken in
  * different image formats.
  */
 public class AdvancedTouristMap extends MapActivity {
 	private static final int DIALOG_ENTER_COORDINATES = 0;
 	private static final int DIALOG_GPS_DISABLED = 1;
 	private static final int DIALOG_INFO_MAP_FILE = 2;
 	private static final String SCREENSHOT_DIRECTORY = "Pictures";
 	private static final String SCREENSHOT_FILE_NAME = "Map screenshot";
 	private static final int SCREENSHOT_QUALITY = 90;
 	private static final int SELECT_MAP_FILE = 0;
 	private static final int INTENT_SEARCH = 1;
 	static final String TAG = AdvancedTouristMap.class.getSimpleName();
 
 	/**
 	 * The default size of the memory card cache.
 	 */
 	static final short MEMORY_CARD_CACHE_SIZE_DEFAULT = 250;
 
 	/**
 	 * The maximum size of the memory card cache.
 	 */
 	static final short MEMORY_CARD_CACHE_SIZE_MAX = 500;
 
 	/**
 	 * The default move speed of the map.
 	 */
 	static final int MOVE_SPEED_DEFAULT = 10;
 
 	/**
 	 * The maximum move speed of the map.
 	 */
 	static final int MOVE_SPEED_MAX = 30;
 
 	AdvancedTouristMapApplication advancedMapViewerApplication;
 
 	private InfoSetterAsync infoSetter;
 
 	ArrayCircleOverlay positionCircleOverlay;
 	private Paint circleOverlayFill;
 	private Paint circleOverlayOutline;
 	private ArrayWayOverlay routeOverlay;
 	private DecisionOverlay decisionPointOverlay;
 	private GenericOverlay genericOverlay;
 	SelectionOverlay selectionOverlay;
 	// Drawable selectionDrawable;
 	OverlayItem selectionOverlayItem;
 	private boolean displayRoute;
 	private PositionOverlayItem positionOverlayItem;
 	private ProgressBar progressBar;
 
 	// routing menu
 	LinearLayout routeMenu;
 	private ImageButton nextDecisionPointButton;
 	private ImageButton previousDecisionPointButton;
 	private ImageButton showRoutingListButton;
 
 	// private boolean followGpsEnabled;
 	boolean centerGpsEnabled;
 	GeoPoint lastPosition;
 	Location currentBestLocation;
 	private LocationListener locationListener;
 	private LocationManager locationManager;
 	private MapViewMode mapViewMode;
 	private PowerManager powerManager;
 	SharedPreferences preferences;
 	private Toast toast;
 	private WakeLock wakeLock;
 	ImageView gpsView;
 	ImageView centerView;
 	MapController mapController;
 	MapView mapView;
 	OverlayCircle overlayCircle;
 	boolean locationPickerMode;
 
 	private GestureDetector mGestureDetector;
 	private Paint fillPaint;
 
 	void setInfosAsync(GeoPoint gp) {
 		this.infoSetter = new InfoSetterAsync();
 		this.infoSetter.execute(gp);
 	}
 
 	@Override
 	public boolean dispatchTouchEvent(MotionEvent ev) {
 		// disable auto following position on scrolling the map
 		if (this.advancedMapViewerApplication.positioningEnabled
 				&& this.centerGpsEnabled) {
 			this.mGestureDetector.onTouchEvent(ev);
 		}
 		return super.dispatchTouchEvent(ev);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.options_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_info:
 			return true;
 
 		case R.id.menu_info_map_file:
 			showDialog(DIALOG_INFO_MAP_FILE);
 			return true;
 
 		case R.id.menu_info_about:
 			startActivity(new Intent(this, InfoView.class));
 			return true;
 
 		case R.id.menu_position:
 			return true;
 
 		case R.id.menu_position_gps_toggle:
 			if (this.advancedMapViewerApplication.positioningEnabled) {
 				disableFollowGPS(true);
 			} else {
 				if (this.locationManager
 						.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
 						|| this.locationManager
 								.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
 					enableFollowGPS();
 				} else {
 					showDialog(DIALOG_GPS_DISABLED);
 				}
 			}
 			return true;
 
 		case R.id.menu_position_enter_coordinates:
 			showDialog(DIALOG_ENTER_COORDINATES);
 			return true;
 
 		case R.id.menu_position_map_center:
 			// disable GPS follow mode if it is enabled
 			disableFollowGPS(true);
 			this.mapController.setCenter(this.mapView.getMapDatabase()
 					.getMapCenter());
 			return true;
 
 		case R.id.menu_screenshot:
 			return true;
 
 		case R.id.menu_screenshot_jpeg:
 			captureScreenshotAsync(CompressFormat.JPEG);
 			return true;
 
 		case R.id.menu_screenshot_png:
 			captureScreenshotAsync(CompressFormat.PNG);
 			return true;
 
 		case R.id.menu_preferences:
 			startActivity(new Intent(this, EditPreferences.class));
 			return true;
 
 			// case R.id.menu_mapfile:
 			// startFileBrowser();
 			// return true;
 		case R.id.menu_mapfile:
 			startBundleBrowser();
 			return true;
 		case R.id.menu_route_clear:
 			disableShowRoute();
 			return true;
 		case R.id.menu_route_new:
 			startActivity(new Intent(this, RouteCalculator.class));
 			return true;
 		case R.id.menu_route_list:
 			startActivity(new Intent(this, RouteList.class));
 			return true;
 		case R.id.menu_poi:
 			startActivity(new Intent(this, PoiBrowserActivity.class).putExtra(
 					"lat", this.mapView.getMapCenter().getLatitude()).putExtra(
 					"lon", this.mapView.getMapCenter().getLongitude()));
 			return true;
 		case R.id.menu_clear_map:
 			this.clearAllOverlays();
 			return true;
 		case R.id.menu_wiki:
 			new WikiSetterAsync().execute(this.mapView.getMapCenter());
 			showToast("Loading Wikipedia Articles");
 			return true;
 		default:
 			return false;
 		}
 	}
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		if (this.mapView.getMapViewMode().requiresInternetConnection()) {
 			menu.findItem(R.id.menu_info_map_file).setEnabled(false);
 		} else {
 			menu.findItem(R.id.menu_info_map_file).setEnabled(true);
 		}
 
 		MenuItem toggleGps = menu.findItem(R.id.menu_position_gps_toggle);
 		if (this.locationManager == null) {
 			toggleGps.setEnabled(false);
 		} else if (this.advancedMapViewerApplication.positioningEnabled) {
 			toggleGps.setTitle(R.string.menu_position_gps_disable);
 		} else {
 			toggleGps.setTitle(R.string.menu_position_gps_enable);
 		}
 
 		if (this.mapView.getMapViewMode().requiresInternetConnection()) {
 			menu.findItem(R.id.menu_position_map_center).setEnabled(false);
 		} else {
 			menu.findItem(R.id.menu_position_map_center).setEnabled(true);
 		}
 
 		if (this.advancedMapViewerApplication.getCurrentMapBundle()
 				.isRoutable()) {
 			menu.findItem(R.id.menu_route).setEnabled(true);
 			menu.findItem(R.id.menu_route).setVisible(true);
 		} else {
 			menu.findItem(R.id.menu_route).setEnabled(false);
 			menu.findItem(R.id.menu_route).setVisible(false);
 		}
 
 		if (this.advancedMapViewerApplication.getCurrentMapBundle().isPoiable()) {
 			menu.findItem(R.id.menu_poi).setEnabled(true);
 			menu.findItem(R.id.menu_poi).setVisible(true);
 		} else {
 			menu.findItem(R.id.menu_poi).setEnabled(false);
 			menu.findItem(R.id.menu_poi).setVisible(false);
 		}
 
 		// http://code.google.com/p/android/issues/detail?id=8359
 		SubMenu routingSubmenu = menu.findItem(R.id.menu_route).getSubMenu();
 		if (!this.displayRoute) {
 			Log.d("Application", "set group invisible");
 			// routingSubmenu.setGroupVisible(R.id.menu_route_group_visible,
 			// false);
 			routingSubmenu
 					.setGroupEnabled(R.id.menu_route_group_visible, false);
 		} else {
 			// routingSubmenu.setGroupVisible(R.id.menu_route_group_visible,
 			// true);
 			routingSubmenu.setGroupEnabled(R.id.menu_route_group_visible, true);
 		}
 
 		return true;
 	}
 
 	@Override
 	public boolean onTrackballEvent(MotionEvent event) {
 		// forward the event to the MapView
 		return this.mapView.onTrackballEvent(event);
 	}
 
 	private void captureScreenshotAsync(final CompressFormat format) {
 		new Thread() {
 			@Override
 			public void run() {
 				try {
 					File path = new File(
 							Environment.getExternalStorageDirectory(),
 							SCREENSHOT_DIRECTORY);
 					// make sure the Pictures directory exists
 					if (!path.exists() && !path.mkdirs()) {
 						showToastOnUiThread("Could not create target directory");
 						return;
 					}
 
 					// assemble the complete name for the screenshot file
 					String fileName = path.getAbsolutePath()
 							+ File.separatorChar + SCREENSHOT_FILE_NAME + "."
 							+ format.name().toLowerCase();
 
 					if (AdvancedTouristMap.this.mapView.makeScreenshot(format,
 							SCREENSHOT_QUALITY, fileName)) {
 						// success
 						showToastOnUiThread(fileName);
 					} else {
 						// failure
 						showToastOnUiThread("Screenshot could not be saved");
 					}
 				} catch (IOException e) {
 					showToastOnUiThread(e.getLocalizedMessage());
 				}
 			}
 
 			private void showToastOnUiThread(final String message) {
 				AdvancedTouristMap.this.runOnUiThread(new Runnable() {
 					@Override
 					public void run() {
 						showToast(message);
 					}
 				});
 			}
 		}.start();
 	}
 
 	private void configureMapView() {
 		// configure the MapView and activate the zoomLevel buttons
 		this.mapView.setClickable(true);
 		this.mapView.setBuiltInZoomControls(true);
 		this.mapView.setFocusable(true);
 
 		// set the localized text fields
 		this.mapView.setText(TextField.KILOMETER,
 				getString(R.string.unit_symbol_kilometer));
 		this.mapView.setText(TextField.METER,
 				getString(R.string.unit_symbol_meter));
 
 		// get the map controller for this MapView
 		this.mapController = this.mapView.getController();
 	}
 
 	private void enableFollowGPS() {
 		this.positionCircleOverlay = new ArrayCircleOverlay(
 				this.circleOverlayFill, this.circleOverlayOutline);
 		this.overlayCircle = new OverlayCircle();
 		this.positionCircleOverlay.addCircle(this.overlayCircle);
 		// this.mapView.getOverlays().add(this.positionOverlay);
 		this.insertOverlayOrdered(this.positionCircleOverlay);
 		this.positionOverlayItem = new PositionOverlayItem();
 		this.positionOverlayItem.setMarker(ItemizedOverlay
 				.boundCenter(getResources().getDrawable(
 						R.drawable.ic_maps_indicator_current_position)));
 		refreshGenericOverlay();
 
 		this.advancedMapViewerApplication.positioningEnabled = true;
 		this.centerGpsEnabled = true;
 
 		// get cached locations first
 		Location currentLocation;
 		for (String provider : this.locationManager.getProviders(true)) {
 			currentLocation = this.locationManager
 					.getLastKnownLocation(provider);
 			if (currentLocation != null) {
 				if (Utility.isBetterLocation(currentLocation,
 						this.currentBestLocation)) {
 					this.currentBestLocation = currentLocation;
 					setNewPosition(currentLocation);
 					Log.d(TAG, "got better cached location from: " + provider
 							+ " (" + currentLocation.getAccuracy() + ")");
 				} else {
 					Log.d(TAG, "dismissed location from: " + provider + " ("
 							+ currentLocation.getAccuracy() + ")");
 				}
 			}
 		}
 
 		this.locationListener = new LocationListener() {
 			@Override
 			public void onLocationChanged(Location location) {
 				if (Utility.isBetterLocation(location,
 						AdvancedTouristMap.this.currentBestLocation)) {
 					setNewPosition(location);
 				}
 			}
 
 			@Override
 			public void onProviderDisabled(String provider) {
 				// we can have multiple location sources
 				// disableFollowGPS(false);
 				// showDialog(DIALOG_GPS_DISABLED);
 			}
 
 			@Override
 			public void onProviderEnabled(String provider) {
 				// do nothing
 			}
 
 			@Override
 			public void onStatusChanged(String provider, int status,
 					Bundle extras) {
 				if (status == LocationProvider.AVAILABLE) {
 					AdvancedTouristMap.this.gpsView
 							.setImageResource(R.drawable.stat_sys_gps_on);
 				} else if (status == LocationProvider.OUT_OF_SERVICE) {
 					AdvancedTouristMap.this.gpsView
 							.setImageResource(R.drawable.stat_sys_gps_acquiring);
 				} else {
 					// must be TEMPORARILY_UNAVAILABLE
 					AdvancedTouristMap.this.gpsView
 							.setImageResource(R.anim.gps_animation);
 					((AnimationDrawable) AdvancedTouristMap.this.gpsView
 							.getDrawable()).start();
 				}
 			}
 		};
 
 		this.locationManager.requestLocationUpdates(
 				LocationManager.GPS_PROVIDER, 1000, 0, this.locationListener);
 		this.locationManager.requestLocationUpdates(
 				LocationManager.NETWORK_PROVIDER, 1000, 0,
 				this.locationListener);
 
 		// if (this.locationManager
 		// .isProviderEnabled(LocationManager.GPS_PROVIDER)) {
 		// this.locationManager.requestLocationUpdates(
 		// LocationManager.GPS_PROVIDER, 1000, 0,
 		// this.locationListener);
 		// } else {
 		// this.locationManager.requestLocationUpdates(
 		// LocationManager.NETWORK_PROVIDER, 1000, 0,
 		// this.locationListener);
 		// }
 
 		this.gpsView.setImageResource(R.drawable.stat_sys_gps_acquiring);
 		this.gpsView.setVisibility(View.VISIBLE);
 		this.gpsView.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				disableFollowGPS(true);
 			}
 		});
 
 		// the icon to center the view to position
 		this.centerView.setImageResource(android.R.drawable.ic_secure);
 		this.centerView.setVisibility(View.VISIBLE);
 		this.centerView.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				AdvancedTouristMap.this.centerGpsEnabled = true;
 				AdvancedTouristMap.this.centerView
 						.setImageResource(android.R.drawable.ic_secure);
 				if (AdvancedTouristMap.this.lastPosition != null) {
 					AdvancedTouristMap.this.mapController
 							.setCenter(AdvancedTouristMap.this.lastPosition);
 				}
 			}
 		});
 	}
 
 	private void setNewPosition(Location location) {
 		GeoPoint point = new GeoPoint(location.getLatitude(),
 				location.getLongitude());
 		this.lastPosition = point;
 		this.overlayCircle.setCircleData(point, location.getAccuracy());
 		this.positionOverlayItem.setPoint(point);
 		if (this.centerGpsEnabled) {
 			this.mapController.setCenter(point);
 		} else {
 			// refresh position "circle"
 			this.positionCircleOverlay.requestRedraw();
 			// refresh position "point"
 			this.genericOverlay.requestRedraw();
 		}
 	}
 
 	private void startBundleBrowser() {
 		startActivity(new Intent(this, FileManagerActivity.class));
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == SELECT_MAP_FILE) {
 			if (resultCode == RESULT_OK) {
 				disableFollowGPS(true);
 				if (data != null && data.getStringExtra("selectedFile") != null) {
 					this.mapView
 							.setMapFile(data.getStringExtra("selectedFile"));
 				}
 			} else if (resultCode == RESULT_CANCELED
 					&& !this.mapView.getMapViewMode()
 							.requiresInternetConnection()
 					&& !this.mapView.hasValidMapFile()) {
 				finish();
 			}
 		} else if (requestCode == INTENT_SEARCH) {
 			if (resultCode == RESULT_OK) {
 				if (data != null && data.hasExtra("lon")
 						&& data.hasExtra("lat")) {
 					double lon = data.getDoubleExtra("lon", 0.0);
 					double lat = data.getDoubleExtra("lat", 0.0);
 					GeoPoint point = new GeoPoint(lat, lon);
 					this.mapView.getController().setCenter(point);
 					// this.mapView.getController().setZoom(16);
 					this.selectionOverlay.setLabel(point);
 				}
 			}
 		}
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// just visible if title bar is visible
 		// requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 		// requestWindowFeature(Window.FEATURE_NO_TITLE);
 
 		TimingLogger timings = new TimingLogger("timing", "onCreate");
 		Log.d("lifecycle", "amv onCreate");
 
 		this.advancedMapViewerApplication = (AdvancedTouristMapApplication) getApplication();
 
 		// set up the layout views
 		this.advancedMapViewerApplication.setViewWithHelp(this,
 				R.layout.activity_advanced_map_viewer);
 		this.mapView = (MapView) findViewById(R.id.mapView);
 		this.gpsView = (ImageView) findViewById(R.id.gpsView);
 		this.centerView = (ImageView) findViewById(R.id.centerView);
 		this.routeMenu = (LinearLayout) findViewById(R.id.routeMenu);
 		this.progressBar = (ProgressBar) findViewById(R.id.mapview_progress_bar);
 
 		configureMapView();
 
 		// get the pointers to different system services
 		this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		this.powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
 		this.wakeLock = this.powerManager.newWakeLock(
 				PowerManager.SCREEN_DIM_WAKE_LOCK, "AMV");
 
 		// set up the paint objects for the location overlay
 		this.circleOverlayFill = new Paint(Paint.ANTI_ALIAS_FLAG);
 		this.circleOverlayFill.setStyle(Paint.Style.FILL);
 		this.circleOverlayFill.setColor(Color.BLUE);
 		this.circleOverlayFill.setAlpha(64);
 
 		this.circleOverlayOutline = new Paint(Paint.ANTI_ALIAS_FLAG);
 		this.circleOverlayOutline.setStyle(Paint.Style.STROKE);
 		this.circleOverlayOutline.setColor(Color.BLUE);
 		this.circleOverlayOutline.setAlpha(128);
 		this.circleOverlayOutline.setStrokeWidth(3);
 
 		// initialize route overlay
 		this.fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
 		this.fillPaint.setStyle(Paint.Style.STROKE);
 		this.fillPaint.setColor(Color.BLUE);
 		this.fillPaint.setAlpha(160);
 		this.fillPaint.setStrokeWidth(6);
 		this.fillPaint.setStrokeCap(Paint.Cap.ROUND);
 		this.fillPaint.setStrokeJoin(Paint.Join.ROUND);
 
 		// selection overlay to display labels with additional information
 		this.selectionOverlay = new SelectionOverlay(null, false);
 		// this.mapView.getOverlays().add(this.selectionOverlay);
 		this.insertOverlayOrdered(this.selectionOverlay);
 
 		if (this.advancedMapViewerApplication.positioningEnabled
 				|| (savedInstanceState != null && savedInstanceState
 						.getBoolean("locationListener"))) {
 			enableFollowGPS();
 		}
 
 		this.mGestureDetector = new GestureDetector(this, new ScrollListener());
 
 		/*
 		 * POI STUFF EXPERIMENTAL
 		 */
 
 		// Log.d("Restaurant", "achtung jetzt:");
 		// IPersistenceManager perstManager = PersistenceManagerFactory
 		// .getPerstPersistenceManager("/sdcard/perstPoi.dbs");
 		// PoiCategory restCat = new CategoryBuilder("Restaurant").build();
 		// long id = 1337;
 		// PointOfInterest poi = new PoiBuilder(id, 52.455667, 13.298357,
 		// "My Test", "",
 		// restCat)
 		// .build();
 		// perstManager.insertPointOfInterest(poi);
 		// TODO: isnt permanent
 		// LinkedList<PointOfInterest> restaurants = new
 		// LinkedList<PointOfInterest>(
 		// perstManager.findNearPosition(new GeoCoordinate(52.4561009222,
 		// 13.297641277),
 		// 1000, "Restaurant", 50));
 		// perstManager.close();
 		//
 		// // create the default paint objects for overlay circles
 		// Paint circleDefaultPaintFill = new Paint(Paint.ANTI_ALIAS_FLAG);
 		// circleDefaultPaintFill.setStyle(Paint.Style.FILL);
 		// circleDefaultPaintFill.setColor(Color.BLUE);
 		// circleDefaultPaintFill.setAlpha(64);
 		//
 		// Paint circleDefaultPaintOutline = new Paint(Paint.ANTI_ALIAS_FLAG);
 		// circleDefaultPaintOutline.setStyle(Paint.Style.STROKE);
 		// circleDefaultPaintOutline.setColor(Color.BLUE);
 		// circleDefaultPaintOutline.setAlpha(128);
 		// circleDefaultPaintOutline.setStrokeWidth(3);
 		//
 		// // create the CircleOverlay and add the circles
 		// ArrayCircleOverlay circleOverlay2 = new
 		// ArrayCircleOverlay(circleDefaultPaintFill,
 		// circleDefaultPaintOutline, this);
 		// // OverlayCircle circle1 = new
 		// OverlayCircle(restaurants.get(0).getGeoPoint(), 10,
 		// // restaurants.get(0).getName());
 		// // circleOverlay2.addCircle(circle1);
 		//
 		// for (int i = 0; i < restaurants.size(); i++) {
 		// Log.d("Restaurant", restaurants.get(i).getName());
 		// circleOverlay2.addCircle(new
 		// OverlayCircle(restaurants.get(i).getGeoPoint(), 50,
 		// restaurants.get(i).getName()));
 		// }
 		//
 		// this.mapView.getOverlays().add(circleOverlay2);
 
 		timings.dumpToLog();
 	}
 
 	/**
 	 * This Listener checks if the map gets moved/scrolled a given distance and
 	 * if so, disables the auto following of the position.
 	 */
 	class ScrollListener extends GestureDetector.SimpleOnGestureListener {
 		@Override
 		public boolean onScroll(MotionEvent e1, MotionEvent e2,
 				float distanceX, float distanceY) {
 			double delta = Math.sqrt(Math.pow((e1.getX() - e2.getX()), 2)
 					+ Math.pow((e1.getY() - e2.getY()), 2));
 			// only disable, if scrolled for a certain distance
 			if (delta > 50) {
 				AdvancedTouristMap.this.centerGpsEnabled = false;
 				AdvancedTouristMap.this.centerView
 						.setImageResource(android.R.drawable.ic_lock_idle_lock);
 			}
 			return true;
 		}
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		if (id == DIALOG_ENTER_COORDINATES) {
 			builder.setIcon(android.R.drawable.ic_menu_mylocation);
 			builder.setTitle(R.string.menu_position_enter_coordinates);
 			LayoutInflater factory = LayoutInflater.from(this);
 			final View view = factory.inflate(
 					R.layout.dialog_enter_coordinates, null);
 			builder.setView(view);
 			builder.setPositiveButton(R.string.go_to_position,
 					new DialogInterface.OnClickListener() {
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							// disable GPS follow mode if it is enabled
 							disableFollowGPS(true);
 
 							// set the map center and zoom level
 							AdvancedTouristMap.this.mapController.setCenter(new GeoPoint(
 									Double.parseDouble(((EditText) view
 											.findViewById(R.id.latitude))
 											.getText().toString()),
 									Double.parseDouble(((EditText) view
 											.findViewById(R.id.longitude))
 											.getText().toString())));
 							AdvancedTouristMap.this.mapController
 									.setZoom(((SeekBar) view
 											.findViewById(R.id.zoomlevel))
 											.getProgress());
 						}
 					});
 			builder.setNegativeButton(R.string.cancel, null);
 			return builder.create();
 		} else if (id == DIALOG_GPS_DISABLED) {
 			builder.setIcon(android.R.drawable.ic_menu_info_details);
 			builder.setTitle(R.string.error);
 			builder.setMessage(R.string.gps_disabled);
 			builder.setPositiveButton(R.string.ok, null);
 			return builder.create();
 		} else if (id == DIALOG_INFO_MAP_FILE) {
 			builder.setIcon(android.R.drawable.ic_menu_info_details);
 			builder.setTitle(R.string.menu_info_map_file);
 			LayoutInflater factory = LayoutInflater.from(this);
 			builder.setView(factory
 					.inflate(R.layout.dialog_info_map_file, null));
 			builder.setPositiveButton(R.string.ok, null);
 			return builder.create();
 		} else {
 			// do dialog will be created
 			return null;
 		}
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		Log.d("lifecycle", "amv onDestroy");
 
 		// remove the toast messageText if visible
 		if (this.toast != null) {
 			this.toast.cancel();
 			this.toast = null;
 		}
 
 		// switch off location updates
 		if (this.locationManager != null) {
 			if (this.locationListener != null) {
 				this.locationManager.removeUpdates(this.locationListener);
 				this.locationListener = null;
 			}
 			this.locationManager = null;
 		}
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		Log.d("lifecycle", "amv onPause");
 		// release the wake lock if necessary
 		if (this.wakeLock.isHeld()) {
 			this.wakeLock.release();
 		}
 		// disable info setter thread
 		if (this.infoSetter != null) {
 			this.infoSetter.cancel(false);
 		}
 		// disable (pause) location updates
 		if (!this.preferences.getBoolean("keep_location_updates", false)) {
 			pauseFollowGps();
 		}
 	}
 
 	@Override
 	protected void onPrepareDialog(int id, final Dialog dialog) {
 		if (id == DIALOG_ENTER_COORDINATES) {
 			// latitude
 			EditText editText = (EditText) dialog.findViewById(R.id.latitude);
 			GeoPoint mapCenter = this.mapView.getMapCenter();
 			editText.setText(Double.toString(mapCenter.getLatitude()));
 
 			// longitude
 			editText = (EditText) dialog.findViewById(R.id.longitude);
 			editText.setText(Double.toString(mapCenter.getLongitude()));
 
 			// zoom level
 			SeekBar zoomlevel = (SeekBar) dialog.findViewById(R.id.zoomlevel);
 			zoomlevel.setMax(this.mapView.getMaxZoomLevel());
 			zoomlevel.setProgress(this.mapView.getZoomLevel());
 
 			// zoom level value
 			final TextView textView = (TextView) dialog
 					.findViewById(R.id.zoomlevelValue);
 			textView.setText(String.valueOf(zoomlevel.getProgress()));
 			zoomlevel
 					.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
 						@Override
 						public void onProgressChanged(SeekBar seekBar,
 								int progress, boolean fromUser) {
 							textView.setText(String.valueOf(progress));
 						}
 
 						@Override
 						public void onStartTrackingTouch(SeekBar seekBar) {
 							// do nothing
 						}
 
 						@Override
 						public void onStopTrackingTouch(SeekBar seekBar) {
 							// do nothing
 						}
 					});
 		} else if (id == DIALOG_INFO_MAP_FILE) {
 			// map file name
 			TextView textView = (TextView) dialog
 					.findViewById(R.id.infoMapFileViewName);
 			textView.setText(this.mapView.getMapFile());
 
 			// map file name
 			textView = (TextView) dialog
 					.findViewById(R.id.infoMapFileViewDebug);
 			MapDatabase mapDatabase = this.mapView.getMapDatabase();
 			if (mapDatabase.isDebugFile()) {
 				textView.setText(R.string.info_map_file_debug_yes);
 			} else {
 				textView.setText(R.string.info_map_file_debug_no);
 			}
 
 			// map file date
 			textView = (TextView) dialog.findViewById(R.id.infoMapFileViewDate);
 			Date date = new Date(mapDatabase.getMapDate());
 			textView.setText(DateFormat.getDateTimeInstance().format(date));
 
 			// map file area
 			textView = (TextView) dialog.findViewById(R.id.infoMapFileViewArea);
 			Rect mapArea = mapDatabase.getMapBoundary();
 			textView.setText(mapArea.top / 1000000d + ", " + mapArea.left
 					/ 1000000d + " – \n" + mapArea.bottom / 1000000d + ", "
 					+ mapArea.right / 1000000d);
 
 			// map file start position
 			textView = (TextView) dialog
 					.findViewById(R.id.infoMapFileViewStart);
 			GeoPoint startPosition = mapDatabase.getStartPosition();
 			if (startPosition == null) {
 				textView.setText(null);
 			} else {
 				textView.setText(startPosition.getLatitude() + ", "
 						+ startPosition.getLongitude());
 			}
 
 			// map file comment text
 			textView = (TextView) dialog
 					.findViewById(R.id.infoMapFileViewComment);
 			String commentText = mapDatabase.getCommentText();
 			if (commentText == null) {
 				textView.setText(null);
 			} else {
 				textView.setText(mapDatabase.getCommentText());
 			}
 		} else {
 			super.onPrepareDialog(id, dialog);
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		TimingLogger timings = new TimingLogger("timing", "onResume");
 		Log.d("lifecycle", "amv onResume");
 		// TODO: still needed?
 		// check if mapview was started, just to return a position
 		Intent startingIntent = getIntent();
 		if (startingIntent.hasExtra("mode")
 				&& startingIntent.getStringExtra("mode").equals(
 						"LOCATION_PICKER")) {
 			this.locationPickerMode = true;
 		}
 		// TODO: use the one from application?
 		// Read the default shared preferences
 		// this.preferences =
 		// PreferenceManager.getDefaultSharedPreferences(this);
 		this.preferences = this.advancedMapViewerApplication.prefs;
 
 		// set the map settings
 		this.mapView.setScaleBar(this.preferences.getBoolean("showScaleBar",
 				false));
 		if (this.preferences.contains("mapViewMode")) {
 			this.mapViewMode = Enum.valueOf(MapViewMode.class, this.preferences
 					.getString("mapViewMode", MapView.getDefaultMapViewMode()
 							.name()));
 			this.mapView.setMapViewMode(this.mapViewMode);
 		}
 		try {
 			this.mapView.setTextScale(Float.parseFloat(this.preferences
 					.getString("textScale", "1")));
 		} catch (NumberFormatException e) {
 			this.mapView.setTextScale(1);
 		}
 		timings.addSplit("set map settings");
 
 		// set the general settings
 		if (this.preferences.getBoolean("fullscreen", false)) {
 			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
 			getWindow().clearFlags(
 					WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
 		} else {
 			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
 			getWindow().addFlags(
 					WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
 		}
 		if (this.preferences.getBoolean("wakeLock", false)) {
 			if (!this.wakeLock.isHeld()) {
 				this.wakeLock.acquire();
 			}
 		}
 		this.mapView.setMemoryCardCachePersistence(this.preferences.getBoolean(
 				"cachePersistence", false));
 		this.mapView.setMemoryCardCacheSize(Math.min(this.preferences.getInt(
 				"cacheSize", MEMORY_CARD_CACHE_SIZE_DEFAULT),
 				MEMORY_CARD_CACHE_SIZE_MAX));
 		this.mapView.setMoveSpeed(Math.min(
 				this.preferences.getInt("moveSpeed", MOVE_SPEED_DEFAULT),
 				MOVE_SPEED_MAX) / 10f);
 
 		// set the debug settings
 		this.mapView.setFpsCounter(this.preferences.getBoolean(
 				"showFpsCounter", false));
 		this.mapView.setTileFrames(this.preferences.getBoolean(
 				"showTileFrames", false));
 		this.mapView.setTileCoordinates(this.preferences.getBoolean(
 				"showTileCoordinates", false));
 		this.mapView.setWaterTiles(this.preferences.getBoolean(
 				"showWaterTiles", false));
 
 		timings.addSplit("set general settings");
 		// check if the file browser needs to be displayed
 		// if (!this.mapView.getMapViewMode().requiresInternetConnection()
 		// && !this.mapView.hasValidMapFile()) {
 		// startFileBrowser();
 		// }
 
 		// check for valid map bundle and start bundle browser, if not present
 		MapBundle mapBundle = this.advancedMapViewerApplication
 				.getCurrentMapBundle();
 		if (mapBundle == null) {
 			showToast(getString(R.string.select_a_valid_mapfile));
 			this.startBundleBrowser();
 		} else {
 
 			// String mapBinary = this.preferences.getString("mapBinary", null);
 			String mapBinary = this.advancedMapViewerApplication
 					.getBaseBundlePath()
 					+ File.separator
 					+ mapBundle.getMapFile().getRelativePath();
 			Log.d("Application", mapBinary);
 			// if offlinemap AND (map isnt set yet OR current set map differs
 			// from map to set)
 			if (!this.mapView.getMapViewMode().requiresInternetConnection()
 					&& (this.mapView.getMapFile() == null || !this.mapView
 							.getMapFile().equals(mapBinary))) {
 				Log.d("Application", "Had to reset mapfile! old: "
 						+ this.mapView.getMapFile() + ", new: " + mapBinary);
 				this.mapView.setMapFile(mapBinary);
 			}
 		}
 		timings.addSplit("set map file");
 
 		// check if positioning is requested but was paused
 		if (this.locationListener == null
 				&& this.advancedMapViewerApplication.positioningEnabled) {
 			enableFollowGPS();
 		}
 
 		// draw the route, if there is any
 		if (this.advancedMapViewerApplication.currentRoute != null) {
 			this.displayRouteOverlay(
 					this.advancedMapViewerApplication.currentRoute,
 					startingIntent);
 		} else {
 			this.displayRoute = false;
 			this.routeMenu.setVisibility(View.GONE);
 		}
 
 		if (this.advancedMapViewerApplication.getCurrentPois().size() > 0
 				|| this.advancedMapViewerApplication.getCurrentWikiArticles()
 						.size() > 0) {
 			refreshGenericOverlay();
 			// only one poi and requested to center
 			if (this.advancedMapViewerApplication.getCurrentPois().size() == 1
 					&& startingIntent.getBooleanExtra("CENTER_POI", false)) {
 				this.mapController
 						.setCenter(Utility
 								.geoCoordinateToGeoPoint(this.advancedMapViewerApplication
 										.getCurrentPois().get(0)
 										.getGeoCoordinate()));
 				// this.mapController.setZoom(16);
 				startingIntent.removeExtra("CENTER_POI");
 			}
 		}
 
 		timings.addSplit("set route");
 		timings.dumpToLog();
 	}
 
 	private void displayRouteOverlay(Route route, Intent startingIntent) {
 		if (this.routeOverlay == null) {
 			this.routeOverlay = new ArrayWayOverlay(this.fillPaint, null);
 			// this.mapView.getOverlays().add(this.routeOverlay);
 			this.insertOverlayOrdered(this.routeOverlay);
 		} else {
 			this.routeOverlay.clear();
 		}
 
 		if (this.decisionPointOverlay == null) {
 			this.decisionPointOverlay = new DecisionOverlay(this,
 					getResources().getDrawable(R.drawable.jog_tab_target_gray));
 			// this.mapView.getOverlays().add(this.decisionPointOverlay);
 			this.insertOverlayOrdered(this.decisionPointOverlay);
 		} else {
 			this.decisionPointOverlay.clear();
 		}
 
 		this.displayRoute = true;
 		this.routeOverlay.addWay(route.getOverlayWay());
 		this.decisionPointOverlay.addItems(Arrays.asList(route
 				.getOverlayItems()));
 		this.setupRoutingMenu(route);
 		if (startingIntent.getBooleanExtra("ROUTE_OVERVIEW", false)) {
 			// TODO: better centering of the route
 			this.mapController.setCenter(route.getGeoPoints()[0]);
 			this.mapController.setZoom(16);
 			// only center on first onResume
 			startingIntent.removeExtra("ROUTE_OVERVIEW");
 		}
 		if (startingIntent.getBooleanExtra("CENTER_DP", false)) {
			this.mapController.setCenter(route.currentDecisionPoint
					.getGeoPoint());
 			// this.mapController.setZoom(16);
 		}
 	}
 
 	private void refreshGenericOverlay() {
 		TimingLogger timings = new TimingLogger("timing",
 				"refreshGenericOverlay");
 		if (this.genericOverlay == null) {
 			this.genericOverlay = new GenericOverlay(this, null);
 			this.insertOverlayOrdered(this.genericOverlay);
 		} else {
 			this.genericOverlay.clear();
 		}
 
 		if (this.advancedMapViewerApplication.getCurrentPois() != null) {
 			Log.d("Overlay", "inserting pois");
 			for (PointOfInterest poi : this.advancedMapViewerApplication
 					.getCurrentPois()) {
 				this.genericOverlay.addItem(new PoiOverlayItem(poi, poi
 						.getCategory().getTitle(), poi.getName(),
 						ItemizedOverlay.boundCenterBottom(getResources()
 								.getDrawable(R.drawable.marker_poi))));
 				// TODO BOUNDCENTER USW?
 			}
 		}
 
 		if (this.advancedMapViewerApplication.getCurrentWikiArticles() != null) {
 			for (WikiArticleInterface wikiArticle : this.advancedMapViewerApplication
 					.getCurrentWikiArticles()) {
 				this.genericOverlay.addItem(new WikiOverlayItem(wikiArticle,
 						wikiArticle.getTitle(), null, ItemizedOverlay
 								.boundCenter(getResources().getDrawable(
 										R.drawable.wikipedia_30))));
 				// TODO BOUNDCENTER USW?
 			}
 		}
 
 		if (this.positionOverlayItem != null) {
 			this.genericOverlay.addItem(this.positionOverlayItem);
 		}
 		timings.dumpToLog();
 	}
 
 	private void clearAllOverlays() {
 		// clear route
 		this.disableShowRoute();
 		// if position is shown, generic overlay is still needed
 		if (this.positionOverlayItem != null) {
 			// clear pois
 			this.disablePoiOverlay();
 			// clear wikipedia
 			this.disableWikipediaOverlay();
 			// and refresh the overlay
 			this.refreshGenericOverlay();
 		} else {
 			// no position, so remove the whole generic overlay
 			this.disableGenericOverlay();
 		}
 		// clear info
 		this.selectionOverlay.clear();
 	}
 
 	private void disableShowRoute() {
 		this.advancedMapViewerApplication.currentRoute = null;
 		this.mapView.getOverlays().remove(this.decisionPointOverlay);
 		this.mapView.getOverlays().remove(this.routeOverlay);
 		this.decisionPointOverlay = null;
 		this.routeOverlay = null;
 		this.displayRoute = false;
 		this.routeMenu.setVisibility(View.GONE);
 	}
 
 	private void disableWikipediaOverlay() {
 		this.advancedMapViewerApplication.getCurrentWikiArticles().clear();
 	}
 
 	private void disablePoiOverlay() {
 		this.advancedMapViewerApplication.getCurrentPois().clear();
 	}
 
 	private void disableGenericOverlay() {
 		this.disableWikipediaOverlay();
 		this.disablePoiOverlay();
 		if (this.genericOverlay != null) {
 			this.mapView.getOverlays().remove(this.genericOverlay);
 			this.genericOverlay = null;
 		}
 	}
 
 	private void insertOverlayOrdered(Overlay overlay) {
 		if (overlay instanceof CircleOverlay) {
 			// insert position overlay as first, always
 			this.mapView.getOverlays().add(0, overlay);
 		} else if (overlay instanceof SelectionOverlay) {
 			// insert selection overlay as last, always
 			this.mapView.getOverlays().add(overlay);
 		} else if (overlay instanceof ArrayWayOverlay) {
 			int positionIndex = this.mapView.getOverlays().indexOf(
 					this.positionCircleOverlay);
 			if (positionIndex >= 0) {
 				// positionOverlay is in, so insert this one behind
 				this.mapView.getOverlays().add(positionIndex + 1, overlay);
 			} else {
 				// no positionOverlay, so this is first
 				this.mapView.getOverlays().add(0, overlay);
 			}
 		} else if (overlay instanceof DecisionOverlay) {
 			int routeIndex = this.mapView.getOverlays().indexOf(
 					this.routeOverlay);
 			int positionIndex = this.mapView.getOverlays().indexOf(
 					this.positionCircleOverlay);
 			if (routeIndex >= 0) {
 				// insert behind routeOverlay
 				this.mapView.getOverlays().add(routeIndex + 1, overlay);
 			} else if (positionIndex >= 0) {
 				// or insert behind position overlay
 				this.mapView.getOverlays().add(positionIndex + 1, overlay);
 			}
 		} else if (overlay instanceof GenericOverlay) {
 			int selectionIndex = this.mapView.getOverlays().indexOf(
 					this.selectionOverlay);
 			if (selectionIndex >= 0) {
 				// insert before selection overlay
 				this.mapView.getOverlays().add(selectionIndex, overlay);
 			} else {
 				// insert as last
 				this.mapView.getOverlays().add(overlay);
 			}
 		} else {
 			// not handled, so just insert last
 			this.mapView.getOverlays().add(overlay);
 		}
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		Log.d("lifecycle", "amv onSaveInstanceState");
 		outState.putBoolean("locationListener", this.locationListener != null);
 	}
 
 	/**
 	 * Disables the "Follow GPS mode" and removes the GPS icon.
 	 * 
 	 * @param showToastMessage
 	 *            if a toast message should be displayed or not.
 	 */
 	void disableFollowGPS(boolean showToastMessage) {
 		// remove the position "circle"
 		if (this.positionCircleOverlay != null) {
 			this.mapView.getOverlays().remove(this.positionCircleOverlay);
 			// this.positionOverlay.clear();
 			this.positionCircleOverlay = null;
 		}
 		// remove the position "dot"
 		if (this.genericOverlay != null && this.positionOverlayItem != null) {
 			this.genericOverlay.removeItem(this.positionOverlayItem);
 			this.positionOverlayItem = null;
 			this.genericOverlay.requestRedraw();
 		}
 		if (this.advancedMapViewerApplication.positioningEnabled) {
 			if (this.locationListener != null) {
 				this.locationManager.removeUpdates(this.locationListener);
 				this.locationListener = null;
 			}
 			this.lastPosition = null;
 			this.gpsView.setVisibility(View.GONE);
 			this.centerView.setVisibility(View.GONE);
 			if (showToastMessage) {
 				showToast(getString(R.string.follow_gps_disabled));
 			}
 			this.advancedMapViewerApplication.positioningEnabled = false;
 		}
 	}
 
 	/**
 	 * Removes updates on the locationListener but keeps an info that the app
 	 * still wants location updates, when it gets resumed
 	 */
 	void pauseFollowGps() {
 		if (this.locationListener != null) {
 			this.locationManager.removeUpdates(this.locationListener);
 			this.locationListener = null;
 		}
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		super.onKeyDown(keyCode, event);
 		if (keyCode == KeyEvent.KEYCODE_SEARCH) {
 			startSearch();
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Show the search activity.
 	 */
 	private void startSearch() {
 		if (!this.advancedMapViewerApplication.getCurrentMapBundle()
 				.isSearchable()) {
 			showToast(getString(R.string.addressfile_not_avaiable));
 		} else {
 			startActivityForResult(new Intent(this, Search.class),
 					INTENT_SEARCH);
 		}
 	}
 
 	/**
 	 * Displays a text message via the toast notification system. If a previous
 	 * message is still visible, the previous message is first removed.
 	 * 
 	 * @param text
 	 *            the text message to display
 	 */
 	void showToast(String text) {
 		if (this.toast == null) {
 			this.toast = Toast.makeText(this, text, Toast.LENGTH_LONG);
 		} else {
 			this.toast.cancel();
 			this.toast.setText(text);
 		}
 		this.toast.show();
 	}
 
 	/**
 	 * Sets up the Routing menu for a given Route and makes it visible. Has a
 	 * button to display the decision point list and two buttons to jump to the
 	 * next/previous decision point in list
 	 * 
 	 * @param route
 	 *            the route to set the menu for
 	 */
 	private void setupRoutingMenu(final Route route) {
 		if (this.nextDecisionPointButton == null) {
 			this.nextDecisionPointButton = (ImageButton) findViewById(R.id.route_menu_next);
 		}
 		if (this.previousDecisionPointButton == null) {
 			this.previousDecisionPointButton = (ImageButton) findViewById(R.id.route_menu_prev);
 		}
 		if (this.showRoutingListButton == null) {
 			this.showRoutingListButton = (ImageButton) findViewById(R.id.route_menu_list);
 		}
 		this.nextDecisionPointButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
				AdvancedTouristMap.this.mapController.setCenter(route
						.getNextDP().getGeoPoint());
 			}
 		});
 		this.previousDecisionPointButton
 				.setOnClickListener(new OnClickListener() {
 					@Override
 					public void onClick(View v) {
						AdvancedTouristMap.this.mapController.setCenter(route
								.getPreviousDP().getGeoPoint());
 					}
 				});
 		this.showRoutingListButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				startActivity(new Intent(AdvancedTouristMap.this, RouteList.class));
 			}
 		});
 		this.routeMenu.setVisibility(View.VISIBLE);
 	}
 
 	private class InfoSetterAsync extends AsyncTask<GeoPoint, String, Void> {
 
 		/**
 		 * maximum distance in meters of Pois to the location to be displayed in
 		 * this overlay
 		 */
 		private static final int POI_MAX_DISTANCE = 100;
 
 		public InfoSetterAsync() {
 			super();
 		}
 
 		@Override
 		protected Void doInBackground(GeoPoint... params) {
 			Log.d("Application", "doInBackground");
 			if (params.length != 1) {
 				return null;
 			}
 			GeoPoint geoPoint = params[0];
 			GeoCoordinate geoCoordinate = new GeoCoordinate(
 					geoPoint.getLatitude(), geoPoint.getLongitude());
 
 			String streetName = "";
 			String poiName = "";
 
 			publishProgress(getString(R.string.loading_message));
 			// TODO: maybe animation while loading
 			// try {
 			// Thread.sleep(1000);
 			// } catch (InterruptedException e) {
 			// e.printStackTrace();
 			// }
 
 			// if map is routable, set street name from router
 			if (!this.isCancelled()
 					&& AdvancedTouristMap.this.advancedMapViewerApplication
 							.getCurrentMapBundle().isRoutable()) {
 				// TODO: not very nice yet
 				if (!AdvancedTouristMap.this.advancedMapViewerApplication
 						.getRouter()
 						.getBoundingBox()
 						.includes(geoPoint.getLatitudeE6(),
 								geoPoint.getLongitudeE6())) {
 					publishProgress("out of box");
 					return null;
 				}
 				Vertex vertex = AdvancedTouristMap.this.advancedMapViewerApplication
 						.getRouter().getNearestVertex(geoCoordinate);
 				if (vertex != null) {
 					String info = PositionInfo.edgesToStringInfo(vertex
 							.getOutboundEdges());
 					if (info != null && !info.equals("")) {
 						streetName = info;
 						publishProgress(info);
 					} else {
 						// no info found, show default message
 						publishProgress(getString(R.string.selection_this_point));
 					}
 				}
 			} else if (!this.isCancelled()) {
 				// mapBundle is not routable, so set default message
 				publishProgress(getString(R.string.selection_this_point));
 			}
 
 			// set info about the closest POI
 			if (!this.isCancelled()
 					&& AdvancedTouristMap.this.advancedMapViewerApplication
 							.getCurrentMapBundle().isPoiable()) {
 				Iterator<PointOfInterest> iterator = AdvancedTouristMap.this.advancedMapViewerApplication
 						.getPerstManager().neighborIterator(geoCoordinate,
 								"Root");
 
 				// find the next *named* POI near the current point inside the
 				// given distance
 				while (iterator.hasNext()) {
 					PointOfInterest nearestPoi = iterator.next();
 					int distance = (int) nearestPoi.getGeoCoordinate()
 							.sphericalDistance(geoCoordinate);
 					if (distance > POI_MAX_DISTANCE) {
 						break;
 					}
 					if (nearestPoi.getName() != null) {
 						poiName = nearestPoi.getName() + " (" + distance
 								+ " m)";
 						if (!streetName.equals("")) {
 							publishProgress(streetName + "\n" + poiName);
 						} else {
 							publishProgress(poiName);
 						}
 						break;
 					}
 				}
 			}
 			return null;
 		}
 
 		@Override
 		protected void onProgressUpdate(String... values) {
 			// build the bubbleView
 			TextView bubbleView = new TextView(AdvancedTouristMap.this);
 			bubbleView.setBackgroundDrawable(getResources().getDrawable(
 					R.drawable.map_label));
 			bubbleView.setGravity(Gravity.CENTER);
 			bubbleView.setMaxEms(20);
 			// bubbleView
 			// .setMaxWidth((int) (AdvancedMapViewer.this.mapView.getWidth() *
 			// (4.0 / 5.0)));
 
 			// set text size according to textScale setting
 			float textSize = 15;
 			try {
 				textSize *= Float
 						.parseFloat(AdvancedTouristMap.this.preferences
 								.getString("textScale", "1"));
 			} catch (NumberFormatException e) {
 				e.printStackTrace();
 			}
 			bubbleView.setTextSize(textSize);
 			bubbleView.setTextColor(Color.WHITE);
 			bubbleView.setCompoundDrawablesWithIntrinsicBounds(0, 0,
 					R.drawable.ic_menu_info_details, 0);
 			// set the text
 			if (values.length == 1 && values[0] != null) {
 				Log.d("Application", "set text to: " + values[0]);
 				bubbleView.setText(values[0]);
 			}
 			// build the drawable from the bubbleView
 			Drawable drawable = ItemizedOverlay
 					.boundCenterBottom(viewToDrawable(bubbleView));
 			AdvancedTouristMap.this.selectionOverlayItem.setMarker(drawable);
 			AdvancedTouristMap.this.selectionOverlay.requestRedraw();
 		}
 
 		@Override
 		protected void onPreExecute() {
 			// just visible if title bar is visible
 			// setProgressBarIndeterminateVisibility(true);
 		}
 	}
 
 	private class WikiSetterAsync extends
 			AsyncTask<GeoPoint, Void, ArrayList<WikiArticleInterface>> {
 
 		public WikiSetterAsync() {
 		}
 
 		@Override
 		protected ArrayList<WikiArticleInterface> doInBackground(
 				GeoPoint... params) {
 			if (params.length != 1) {
 				return null;
 			}
 			try {
 				GeoPoint geoPoint = params[0];
 				ArticleRetriever retriever = ArticleRetrieverFactory
 						.getGeonamesReceiver(AdvancedTouristMap.this.advancedMapViewerApplication
 								.getWikiLocale());
 				ArrayList<WikiArticleInterface> articles = retriever
 						.getArticles(geoPoint, 20000, 50, 0);
 
 				AdvancedTouristMap.this.advancedMapViewerApplication
 						.getCurrentWikiArticles().clear();
 				AdvancedTouristMap.this.advancedMapViewerApplication
 						.getCurrentWikiArticles().addAll(articles);
 				return articles;
 			} catch (IOException e) {
 				e.printStackTrace();
 				return null;
 			}
 		}
 
 		@Override
 		protected void onPostExecute(ArrayList<WikiArticleInterface> result) {
 			// AdvancedTouristMap.this.displayWikiOverlay(result);
 			if (result != null) {
 				refreshGenericOverlay();
 			} else {
 				// something went wrong, let the user know
 				Toast.makeText(AdvancedTouristMap.this,
 						getString(R.string.wikipedia_no_connection),
 						Toast.LENGTH_LONG);
 			}
 			// remove the ProgressBar
 			AdvancedTouristMap.this.progressBar.setVisibility(View.GONE);
 
 		}
 
 		@Override
 		protected void onPreExecute() {
 			AdvancedTouristMap.this.progressBar.setVisibility(View.VISIBLE);
 		}
 
 		@Override
 		protected void onCancelled() {
 			// remove the ProgressBar
 			AdvancedTouristMap.this.progressBar.setVisibility(View.GONE);
 		}
 
 	}
 
 	Drawable viewToDrawable(View view) {
 		view.measure(MeasureSpec.getSize(view.getMeasuredWidth()),
 				MeasureSpec.getSize(view.getMeasuredHeight()));
 		view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
 		view.setDrawingCacheEnabled(true);
 		Drawable drawable = new BitmapDrawable(Bitmap.createBitmap(view
 				.getDrawingCache()));
 		view.setDrawingCacheEnabled(false);
 		return drawable;
 	}
 
 	private class SelectionOverlay extends ArrayItemizedOverlay {
 
 		public SelectionOverlay(Drawable defaultMarker, boolean alignMarker) {
 			super(defaultMarker, alignMarker);
 		}
 
 		@Override
 		public boolean onLongPress(GeoPoint geoPoint, MapView overlayMapView) {
 			setLabel(geoPoint);
 			return true;
 		}
 
 		public void setLabel(GeoPoint geoPoint) {
 			// clear last label
 			this.clear();
 			if (AdvancedTouristMap.this.selectionOverlayItem == null) {
 				AdvancedTouristMap.this.selectionOverlayItem = new OverlayItem(
 						geoPoint, null, null,
 						ItemizedOverlay.boundCenterBottom(getResources()
 								.getDrawable(R.drawable.map_label)));
 			} else {
 				AdvancedTouristMap.this.selectionOverlayItem.setPoint(geoPoint);
 			}
 			addItem(AdvancedTouristMap.this.selectionOverlayItem);
 			// asynchronously set the text
 			AdvancedTouristMap.this.runOnUiThread(new myRunnable(geoPoint));
 		}
 
 		private class myRunnable implements Runnable {
 			private GeoPoint geoPoint;
 
 			public myRunnable(GeoPoint geoPoint) {
 				this.geoPoint = geoPoint;
 			}
 
 			@Override
 			public void run() {
 				// new InfoSetterAsync().execute(this.geoPoint);
 				AdvancedTouristMap.this.setInfosAsync(this.geoPoint);
 			}
 		}
 
 		@Override
 		protected boolean onTap(int index) {
 			OverlayItem item = createItem(index);
 			if (item != null) {
 				if (AdvancedTouristMap.this.locationPickerMode) {
 					Log.d("RouteCalculator", "location picker mode on");
 					AdvancedTouristMap.this.setResult(
 							RESULT_OK,
 							new Intent().putExtra("LONGITUDE",
 									item.getPoint().getLongitude()).putExtra(
 									"LATITUDE", item.getPoint().getLatitude()));
 					AdvancedTouristMap.this.finish();
 					return true;
 				}
 				startActivity(new Intent(AdvancedTouristMap.this,
 						PositionInfo.class).putExtra("LATITUDE",
 						item.getPoint().getLatitude()).putExtra("LONGITUDE",
 						item.getPoint().getLongitude()));
 			}
 			return true;
 		}
 
 		// @Override
 		// public boolean onTap(GeoPoint geoPoint, MapView mv) {
 		// // TODO: label disappears on pinch zooming
 		// if (!super.onTap(geoPoint, mv)) {
 		// AdvancedMapViewer.this.selectionOverlay.clear();
 		// }
 		// return true;
 		// }
 
 	}
 }
