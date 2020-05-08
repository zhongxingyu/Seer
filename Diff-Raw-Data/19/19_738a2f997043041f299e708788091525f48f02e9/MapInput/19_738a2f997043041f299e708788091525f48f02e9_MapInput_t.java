 package com.swindells.map;
 
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 import com.google.android.maps.*;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.database.Cursor;
 import android.location.Location;
 import android.location.LocationManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.Menu;
 import android.view.MenuItem;
 
 public class MapInput extends MapActivity implements Observer
 {
 	
 	private static final int GO_ID = Menu.FIRST;
 	private static final int STOP_ID = Menu.FIRST;
 	private static final int CENTER_ID = Menu.FIRST + 1;
 	private static final int OPTIONS_ID = Menu.FIRST + 2;
 	
 	private static final int SCAN_ID = Menu.FIRST + 3;
 	
 	public static final int ACTIVITY_ADD = 0;
 
 	private MapController mapController;
 	private PositionOverlay positionOverlay;
 	
 	private SharedPreferences prefs;
 	private LocationManager locationManager;
 	
 	private int zoomLevel = 17;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		String serviceString = Context.LOCATION_SERVICE;
 		locationManager = (LocationManager) getSystemService(serviceString);
 		
 		prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		super.onCreate(savedInstanceState);
 		
 		setContentView(R.layout.map);
 		
 		setUpMap();
 	}
 
 	private void setUpMap()
 	{
 		MapView myMapView = (MapView) findViewById(R.id.myMapView);
 		mapController = myMapView.getController();
 		List<Overlay> overlays = myMapView.getOverlays();
 
 		positionOverlay = new PositionOverlay();
 		overlays.add(positionOverlay);
 		myMapView.setSatellite(false);
 		
 		myMapView.setBuiltInZoomControls(true);
 		mapController.setZoom(zoomLevel);
 		
 		mapController.animateTo(new GeoPoint((int) (55.86015 * 1E6), (int) (-4.25236 * 1E6)));
 		
 		addVisitables(overlays);
 		
 		myMapView.postInvalidate();
 	}
 
 	private void addVisitables(List<Overlay> overlayList)
 	{
 		VisitableOverlay places = new VisitableOverlay(getResources().getDrawable(
 				R.drawable.marker), this);
 
 
 		Location lastKnownLocation = locationManager
 				.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 		updateLocation(lastKnownLocation);
 		
 
 		LocationList db = new LocationList(this);
 		db.open();
 		Cursor c = db.fetchAll();
 		
 		if (c.getCount() > 0)
 		{
 			c.moveToFirst();
 			
 			while (!c.isAfterLast())
 			{
 				int lat = c.getInt(c.getColumnIndex(LocationList.KEY_LATITUDE));
 				int lng = c.getInt(c.getColumnIndex(LocationList.KEY_LONGITUDE));
 				
 				String name = c.getString(c.getColumnIndex(LocationList.KEY_NAME));
 				String desc = c.getString(c.getColumnIndex(LocationList.KEY_DESC));
 				
 				GeoPoint location = new GeoPoint(lat, lng);
 				OverlayItem i = new OverlayItem(location, name, desc);
 				places.addOverlay(i);				
 				
 				c.moveToNext();
 			}
 			
 			overlayList.add(places);
 		}
 		
 	}
 
 	public void updateLocation(Location location)
 	{
 		if (location != null)
 		{
 			positionOverlay.setLocation(location);
 
 			MapView myMapView = (MapView) findViewById(R.id.myMapView);
 			
 			if (serviceRunning())
 			{
 				GeoPoint gp = new GeoPoint((int) (location.getLatitude() * 1E6), (int) (location.getLongitude() * 1E6));
 				mapController.animateTo(gp);
 			}
 			else
 				myMapView.postInvalidate();
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.google.android.maps.MapActivity#onDestroy()
 	 */
 	@Override
 	protected void onDestroy()
 	{
 		unsubscribe();
 		zoomLevel = ((MapView) findViewById(R.id.myMapView)).getZoomLevel();
 		super.onDestroy();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.google.android.maps.MapActivity#onPause()
 	 */
 	@Override
 	protected void onPause()
 	{
 		unsubscribe();
 		zoomLevel = ((MapView) findViewById(R.id.myMapView)).getZoomLevel();
 		super.onPause();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.google.android.maps.MapActivity#onResume()
 	 */
 	@Override
 	protected void onResume()
 	{
 		subscribe();
 		
 		super.onResume();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		super.onCreateOptionsMenu(menu);
 		menu.add(0, CENTER_ID, 0, R.string.menu_map_center).setIcon(android.R.drawable.ic_menu_compass);
 		menu.add(0, OPTIONS_ID, 0, R.string.options).setIcon(android.R.drawable.ic_menu_preferences);
 		//menu.add(0, LIST_ID, 0, R.string.visitablelist);
 		return true;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
 	 */
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu)
 	{
 		boolean serviceRunning = prefs.getBoolean(SerendipitousService.RUNNING_PREF, false);
 
 		SelectedLocationList sldba = new SelectedLocationList(this);
 		int c = sldba.count();
 		
		menu.removeItem(GO_ID);
		
		if (c > 0)
 		{
			if (!serviceRunning)
 			{
 				menu.add(0, GO_ID, 1, R.string.menu_start_service).setIcon(android.R.drawable.ic_media_play);
 			}
			else
			{
				menu.add(0, STOP_ID, 1, R.string.menu_stop_service).setIcon(android.R.drawable.ic_media_pause);
			}
 		}
 	
 		menu.removeItem(SCAN_ID);
 		final android.content.pm.PackageManager packageManager = getPackageManager();
 		Intent intent = new Intent("com.google.zxing.client.android.SCAN");
 		if (packageManager.queryIntentActivities(intent, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY).size() > 0)
 			menu.add(0, SCAN_ID, 0, R.string.menu_scan).setIcon(R.drawable.qrcode);
 
 		return super.onPrepareOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item)
 	{
 		
 		int id = item.getItemId();
 		if (id == CENTER_ID)
 		{
 			
 			Location lastKnownLocation = locationManager
 					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 			
 			if (lastKnownLocation != null)
 			{
 				updateLocation(lastKnownLocation);
 				
 				GeoPoint p = new GeoPoint((int) (lastKnownLocation.getLatitude() * 1E6), (int) (lastKnownLocation.getLongitude() * 1E6));
 				mapController.animateTo(p);
 			}
 		}
 		else if (id == GO_ID && !serviceRunning())
 		{
 			startService(new Intent(this, SerendipitousService.class));
 		}
 		else if (id == STOP_ID && serviceRunning())
 		{
 			stopService(new Intent(this, SerendipitousService.class));
 			
 			Editor e = prefs.edit();
 			e.putBoolean(SerendipitousService.RUNNING_PREF, false);
 			e.commit();
 		}
 		else if (id == OPTIONS_ID)
 		{
 			Intent i = new Intent(this, Preferences.class);
 			startActivity(i);
 		}
 		else if (id == SCAN_ID)
 		{
 			Intent intent = new Intent("com.google.zxing.client.android.SCAN");
 			intent.setPackage("com.google.zxing.client.android");
 			intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
 			
 			startActivityForResult(intent, 0);
 		}/*
 		else if (id == LIST_ID)
 		{
 			startActivity(new Intent(this, LocationList.class));
 		}*/
 		
 		return super.onMenuItemSelected(featureId, item);
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
 	 */
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data)
 	{
 		if (requestCode == 0)
 		{
 			if (resultCode == RESULT_OK)
 			{
 				String content = data.getStringExtra("SCAN_RESULT");
 				
 				String action = "com.swindells.map.QRInput";
 				Uri u = Uri.parse(content);
 				Intent intent = new Intent(action, u);
 				startActivity(intent);
 			}
 		}
 	}
 
 	@Override
 	protected boolean isRouteDisplayed()
 	{
 		return false;
 	}
 	
 	@Override
 	protected boolean isLocationDisplayed()
 	{
 		return true;
 	}
 
 	@Override
 	public void update(Observable observable, Object data)
 	{
 		updateLocation ((Location) data);		
 	}
 	
 	public void subscribe()
 	{
 		LocationNotifier lo = new NotifierFactory().setContext(this).getInstance();
 		lo.addObserver(this);
 	}
 	
 	public void unsubscribe()
 	{
 		LocationNotifier lo = new NotifierFactory().setContext(this).getInstance();
 		lo.deleteObserver(this);
 	}
 	
 	public boolean serviceRunning()
 	{
 		return prefs.getBoolean(SerendipitousService.RUNNING_PREF, false);
 	}
 
 }
