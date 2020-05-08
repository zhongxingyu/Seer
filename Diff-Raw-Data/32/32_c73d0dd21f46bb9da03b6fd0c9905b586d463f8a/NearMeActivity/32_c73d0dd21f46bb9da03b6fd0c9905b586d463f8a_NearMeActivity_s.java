 package com.parse.starter;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.Hashtable;
 import java.util.List;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.ItemizedOverlay;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 import com.parse.ParseGeoPoint;
 import com.parse.ParseObject;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.Toast;
 
 public class NearMeActivity extends MapActivity {
 
 	private static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 1; // in Meters
 	private static final long MINIMUM_TIME_BETWEEN_UPDATES = 1000; // in Milliseconds
 	ProgressDialog loader;
 	FoodEventItemizedOverlay itemizedOverlay;
 	private MapView theMap;
 	private boolean gpsEnabled;
 	private boolean networkEnabled;
 	private Hashtable<String,Integer> event_locs;
 	private Handler locFailHandler = new Handler();
 	private Handler doneHandler = new Handler();
 
 	protected LocationManager locationManager;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		//Location code
 		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 
 		LocationListener locationListener = new LocationListener() {
 			public void onLocationChanged(Location location) {
 
 			}
 
 			public void onStatusChanged(String s, int i, Bundle b) {
 
 			}
 
 			public void onProviderDisabled(String s) {
 
 			}
 
 			public void onProviderEnabled(String s) {
 
 			}
 		};
 
 		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) { 
 			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MINIMUM_TIME_BETWEEN_UPDATES, MINIMUM_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
 			gpsEnabled = true;
 			if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
 				networkEnabled = true;
 		}
 		else {
 			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MINIMUM_TIME_BETWEEN_UPDATES, MINIMUM_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
 			Toast.makeText(this, "GPS not enabled. Attempting rough estimate", Toast.LENGTH_LONG).show();
 			gpsEnabled = false;
 			networkEnabled = true;
 		}
 
 		setContentView(R.layout.map_overlay);
 		theMap = (MapView) findViewById(R.id.map_overlay);
 		theMap.setBuiltInZoomControls(true);
 		loader = ProgressDialog.show(this, "", "Loading food events...", true);
 
 
 		new Thread(new Runnable() {
 			public void run() {
 				GeoPoint referencePoint;
 				Location location;
 				if (gpsEnabled) {
 					location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 					if (location == null) {
 						if (networkEnabled)
 							location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 					}
 				}
 				else
 					location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 
 				if (location != null) {
 					referencePoint = new GeoPoint((int)(location.getLatitude() * 1000000),(int)(location.getLongitude() * 1000000));
 				}
 
 				else {
 					//Default to rotunda (could be anywhere) if we fail
 					referencePoint = new GeoPoint(38035681, -78503323);
 					locFailHandler.post(new Runnable() {
 						public void run() {
 							printFail();
 						}
 					});
 				}
 
 
 				MapController myMapController = theMap.getController();
 				myMapController.setCenter(referencePoint);
 				myMapController.setZoom(17);
 
 
 				List<Overlay> mapOverlays = theMap.getOverlays();
 				Drawable drawable = NearMeActivity.this.getResources().getDrawable(R.drawable.map_marker);
 				Drawable me_mark = NearMeActivity.this.getResources().getDrawable(R.drawable.me_marker);
 				itemizedOverlay = new FoodEventItemizedOverlay(drawable, NearMeActivity.this);
 
 				MeItemizedOverlay meOverlay = new MeItemizedOverlay(me_mark);
 				OverlayItem me = new OverlayItem(referencePoint, "", "");
 				meOverlay.addOverlay(me);
 
 
 				event_locs = new Hashtable<String,Integer>();
 				ArrayList<ParseObject> foodEvents;
 				if (ParseApplication.checkConn(NearMeActivity.this)) {
 					foodEvents = ParseApplication.getFoodItems();
 				}
 				else {
 					foodEvents = new ArrayList<ParseObject> ();
 					Toast.makeText(NearMeActivity.this, "HoosEating needs an internet connection. Please enable wifi/data!", Toast.LENGTH_LONG).show();
 				}
 
 				for (ParseObject oneEvent : foodEvents) {
 					ParseGeoPoint coords = (ParseGeoPoint) oneEvent.get("coordinates");
 					double lat = coords.getLatitude();
 					double lon = coords.getLongitude();
 					String formatted_coords = ""+lat+","+lon;
 					//Do we already have events at this location?
 					if (event_locs.containsKey(formatted_coords)) {
 						int num_prev = event_locs.get(formatted_coords);
 						if (num_prev == 1) {
 							lat = lat - .000030;
 							lon = lon - .000030;
 						}
 						else if (num_prev == 2) {
 							lat = lat - .000030;
 							lon = lon + .000030;
 						}
 						else if (num_prev == 3) {
 							lat = lat + .000030;
 							lon = lon + .000030;
 						}
 						else if (num_prev == 4) {
 							lat = lat + .000030;
 							lon = lon - .000030;
 						}
 						event_locs.put(formatted_coords, ++num_prev);
 					}
 					else {
 						event_locs.put(formatted_coords, 1);
 					}
 
 
 					GeoPoint point = new GeoPoint((int)(lat * 1000000),(int)(lon * 1000000));
 					String title = oneEvent.getString("name");
 					String loc = oneEvent.getString("location");
 					String start = oneEvent.getString("start_time");
 					String end = oneEvent.getString("end_time");
 					
 					GregorianCalendar curr = (GregorianCalendar) Calendar.getInstance();
 
 					HoosEatingDatetime startDate = new HoosEatingDatetime(start, curr);
 					HoosEatingDatetime endDate = new HoosEatingDatetime(end, curr);
 
 					String descrip;
 					if (startDate.getMonthDay().equals(endDate.getMonthDay())) {
 						descrip = loc + "\n" + startDate.getMonthDay() + "\n" + startDate.getTime() + " - " + endDate.getTime();
 					}
 					else {
 						descrip = loc + "\n" + startDate.getMonthDay() + " " + startDate.getTime() + " to\n" + endDate.getMonthDay() + " " + endDate.getTime();
 					}
 
 					FoodEventOverlayItem overlayitem = new FoodEventOverlayItem(point, title, descrip, oneEvent);
 					itemizedOverlay.addOverlay(overlayitem);
 				}
 
 
 				mapOverlays.add(itemizedOverlay);
 				mapOverlays.add(meOverlay);
 
 				loader.dismiss();
 				doneHandler.post(new Runnable() {
 					public void run () {
 						theMap.invalidate();
 					}
 				});
 
 			}
 		}).start();
 	}
 
 	protected void printFail() {
 		Toast.makeText(this, "Could not retrieve your location. Defaulting to The Rotunda", Toast.LENGTH_LONG).show();
 	}
 
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 
 }
