  package com.ui;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.util.Date;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.ActionBar;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.BitmapFactory.Options;
 import android.graphics.drawable.BitmapDrawable;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.SystemClock;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.animation.Interpolator;
 import android.view.animation.LinearInterpolator;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.bugsense.trace.BugSenseHandler;
 import com.core.CacheManager;
 import com.core.Constants;
 import com.core.Time2FlyApp;
 import com.core.Utils;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
 import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
 import com.google.android.gms.maps.GoogleMap.SnapshotReadyCallback;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.CameraPosition;
 import com.google.android.gms.maps.model.GroundOverlayOptions;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.LatLngBounds;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.lazylist.ImageLoader;
 import com.listeners.T2FClickListener;
 import com.modules.Tab;
 import com.network.GetDataTask;
 import com.network.GetWeatherOvelay;
 import com.network.MyLocationListener;
 import com.network.QueriedImageTask;
 import com.shared.SharedLayouts;
 import com.shared.SharedResources;
 
 public class Home extends FragmentActivity {
 	Time2FlyApp appInstance;
 	Context mContext = this;	
 	GoogleMap googleMap;
 	CacheManager cache = CacheManager.getInstance();
 	T2FClickListener listener;
 	ImageLoader imgLoader = new ImageLoader(mContext);
 	
 	Runnable refreshValsRunnable = new Runnable() {
 		@Override
 		public void run() {
 			NetworkTask task = new NetworkTask();
 			task.execute();
 		}
 	};
 
 	TimerTask weatherTask = new TimerTask() {
 		@Override
 		public void run() {
 			WeatherTask task = new WeatherTask();
 			task.execute();
 		}
 	};
 	
 	
 	private void initUI(){
 		setContentView(R.layout.activity_home);
 		SharedLayouts.initLayouts(this);
 		SharedResources.mAnimations = new MyAnimations(mContext);
 		listener = new T2FClickListener(mContext);
 		
 		int width = getWindowManager().getDefaultDisplay().getWidth();
 		SharedLayouts.leftSection.getLayoutParams().width = (int)(width / 3.5) ;
 		
 		//SharedLayouts.sideTray.setOnClickListener(listener);
 		SharedLayouts.searchButton.setOnClickListener(listener);
 		SharedLayouts.back.setOnClickListener(listener);
 	
 		
 		cache.tabs_hash.clear();
 		SharedLayouts.drawer.removeAllViews();
 
 		
 		
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		BugSenseHandler.initAndStartSession(mContext, "c417ebfa");
 		initUI();
 		initActionBar();
 		appInstance = (Time2FlyApp) getApplication();
 		
 		initGoogleMap();
 		cache.cyclesCount = 0;
 		Toast.makeText(mContext, "Loading flights data", Toast.LENGTH_LONG).show();
 
 
 		// Call the 1st JSON Update
 		runOnUiThread(refreshValsRunnable);
 
 		// Init Weather Task
 		if (appInstance.isWeatheroverlayEnabled()) {
 			WeatherTask task = new WeatherTask();
 			task.execute();
 		}
 
 		
 	}
 
 	@Override
 	public void onBackPressed() {
 		weatherTask.cancel();
 		SharedResources.timer.cancel();
 		SharedResources.timer.purge();
 		BugSenseHandler.closeSession(this);
 		finish();
 		android.os.Process.killProcess(android.os.Process.myPid());
 	};
 	
 
 	private void initGoogleMap() {
 		googleMap = ((SupportMapFragment) (getSupportFragmentManager()
 				.findFragmentById(R.id.map))).getMap();
 		googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
 		googleMap.getUiSettings().setCompassEnabled(true);
 		googleMap.getUiSettings().setZoomControlsEnabled(true);
 		googleMap.setMyLocationEnabled(true);
 		googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(SharedResources.hkLatLng, 9));
 
 		googleMap.addMarker(new MarkerOptions()
 				.icon(BitmapDescriptorFactory.fromResource(R.drawable.hk_flag))
 				.anchor((float) 0.5, (float) 0.5).position(SharedResources.hkLatLng)
 				.title("Hong Kong International Airport"));
 
 		cache.currentLoc.setLatitude(SharedResources.hkLatLng.latitude);
 		cache.currentLoc.setLongitude(SharedResources.hkLatLng.longitude);
 
 		Location myLoc = getCurrentLocation();
 		if (myLoc != null && !appInstance.isHomeHK()) {
 			Log.d(Constants.TAG,
 					myLoc.getLatitude() + " - " + myLoc.getLongitude());
 			cache.currentLoc = myLoc;
 		}
 
 		googleMap.setOnCameraChangeListener(new OnCameraChangeListener() {
 			@Override
 			public void onCameraChange(CameraPosition position) {
 				int new_bearing_angle = (int) position.bearing;
 				if (new_bearing_angle != SharedResources.bearing_angle) {
 					SharedResources.bearing_angle = new_bearing_angle;
 					updateBearing();
 				}
 
 				if (position.zoom != cache.zoom) {
 					{
 						// do stuff
 					}
 					cache.zoom = position.zoom;
 				}
 
 			}
 		});
 
 		googleMap.setOnMarkerClickListener(new OnMarkerClickListener() {
 			@Override
 			public boolean onMarkerClick(Marker marker) {
 				String title = marker.getTitle();
 				for (int i = 0; i < SharedLayouts.drawer.getChildCount(); i++)
 					SharedLayouts.drawer.getChildAt(i).setBackgroundResource(
 							R.drawable.rounded_border);
 
 				int layoutIndex = cache.tabs_hash.searchByTitle(title);
 				if (layoutIndex > -1) {
 					LinearLayout selectedLayout = (LinearLayout) SharedLayouts.drawer
 							.getChildAt(layoutIndex);
 					if (selectedLayout != null) {
 						selectedLayout
 								.setBackgroundResource(R.drawable.rounded_border_yellow);
 						String addr = (String) selectedLayout.getTag();
 						cache.selectedReg = addr;
 						Tab t = cache.tabs_hash.get(addr);
 						updateDetailedView(t, "");
 						LatLng latLng = new LatLng(t.lat, t.lon);
 						googleMap.animateCamera(CameraUpdateFactory
 								.newLatLngZoom(latLng, 11));
 					}
 				}
 				return false;
 			}
 		});
 
 	}
 
 	private void updateBearing() {
 		
 		//COMMENT HERE
 		
 		Object[] tabs = cache.tabs_hash.exportSortedList(cache.tabs_hash.values().toArray());
 		for (int i = 0; i < tabs.length; i++) {
 			Tab tab = (Tab) tabs[i];
 			Bitmap bmp = BitmapFactory.decodeResource(getResources(), Utils
 					.getInstance().getResourceID(tab));
 			bmp = Utils.getInstance()
 					.rotateImage(bmp, tab.track, SharedResources.bearing_angle);
 			tab.marker.setIcon(BitmapDescriptorFactory.fromBitmap(bmp));
 		}
 	}
 	
 	
 	
 	public void renderAfterSearch()
 	{
 		clearMap();
 		renderTargets();
 	}
 	private void clearMap(){
 		Object[] tabs = cache.tabs_hash.values().toArray();
 		for(int i = 0 ; i < tabs.length ; i ++)
 		{
 			Tab t = (Tab)tabs[i];
 			if(t.marker != null){
 				t.marker.remove();
 				t.marker = null;
 			}
 		}
 	}
 
 	private void renderTargets() {
 		SharedResources.localTime = new Date();
 		String txt = "Time2Fly " + SharedResources.localTime.toLocaleString();
 		SharedLayouts.timeLabel.setText(txt);
 		cache.num_targets = 0;
 		Object[] tabs;
 		if(!SharedResources.searchFilter)
 			tabs = cache.tabs_hash.exportSortedList(cache.tabs_hash.values().toArray());
 		else
 		{
 			// CLEAR GOOGLE MAP HERE
 			String regex = SharedLayouts.searchField.getEditableText().toString();
 			tabs = cache.tabs_hash.search(regex);
 			tabs = cache.tabs_hash.exportSortedList(tabs);
 		}
 		
 		Tab selectedTab = null;
 		String selectedDest = "";
 
 		SharedLayouts.drawer.removeAllViews();
 		for (int i = 0; i < tabs.length; i++) {
 			Tab tab = (Tab) tabs[i];
 			tab.isActive = ((cache.cyclesCount - tab.cycles) < 2);
 			if ((cache.cyclesCount - tab.cycles) > 3) {
 				if (tab.marker != null)
 					tab.marker.remove();
 				cache.tabs_hash.remove(tab.addr);
 				continue;
 			}
 
 			LatLng latLng = new LatLng(tab.lat, tab.lon);
 			Bitmap bmp = BitmapFactory.decodeResource(getResources(), Utils
 					.getInstance().getResourceID(tab));
 			bmp = Utils.getInstance()
 					.rotateImage(bmp, tab.track, SharedResources.bearing_angle);
 
 			float alt = ((int) tab.alt) / 100;
 			int altitude = Math.round(alt);
 			String flightLevel = "";
 			if (alt >= 100) {
 				flightLevel = "FL" + altitude;
 			} else {
 				if (altitude > 10)
 					flightLevel = "A0" + altitude;
 				else
 					flightLevel = "A00" + altitude;
 			}
 
 			Location loc = new Location("t2f");
 			loc.setLatitude(latLng.latitude);
 			loc.setLongitude(latLng.longitude);
 			// loc.setBearing(t.track);
 
 			float bearingAngle = cache.currentLoc.bearingTo(loc);
 			if (bearingAngle < 0)
 				bearingAngle = bearingAngle + 360;
 
 			String direction = Utils.getInstance().getDirectionFromAngle(
 					bearingAngle);
 
 			float distance = loc.distanceTo(cache.currentLoc) / 1000;
 			distance = (float) (Math.round(distance * 20.0) / 20.0);
 			String snippet = tab.type + "  " + tab.spd + "Kts" ;
 
 			String dist = distance + "Km  " + direction;
 
 			if (tab.marker == null) {
 				tab.marker = googleMap.addMarker(new MarkerOptions()
 						.position(latLng).anchor((float) 0.5, (float) 0.5)
 						.title(tab.callSign + " " + flightLevel + " " + Utils.getInstance().getVSPD(tab.vspd))
 						.snippet(snippet)
 						.icon(BitmapDescriptorFactory.fromBitmap(bmp)));
 			} else {
 				LatLng org = new LatLng(tab.xLat, tab.xLon);
 				LatLng dest = new LatLng(tab.lat, tab.lon);
 				animateMarker(tab, org, dest, flightLevel, snippet);
 			}
 			if (cache.selectedReg.equalsIgnoreCase(tab.addr)) {
 				tab.marker.showInfoWindow();
 			}
 
 			Tab returnedTab = addFlightTab(tab, dist);
 			if (returnedTab != null) {
 				selectedTab = returnedTab;
 				selectedDest = dist;
 			}
 		}
 
 		if (selectedTab != null)
 			updateDetailedView(selectedTab, selectedDest);
 
 	}
 
 	private void animateMarker(final Tab tab, final LatLng org,
 			final LatLng dest, String flightLevel, String snippet) {
 
 		final Marker marker = tab.marker;
 		Bitmap bmp = BitmapFactory.decodeResource(getResources(), Utils
 				.getInstance().getResourceID(tab));
 		bmp = Utils.getInstance().rotateImage(bmp, tab.track, SharedResources.bearing_angle);
 		marker.setTitle(tab.callSign + " " + flightLevel + " " +Utils.getInstance().getVSPD(tab.vspd));
 		marker.setSnippet(snippet);
 		marker.setIcon(BitmapDescriptorFactory.fromBitmap(bmp));
 
 		final Handler handler = new Handler();
 		final long start = SystemClock.uptimeMillis();
 		final long duration = cache.update_rate;
 		final Interpolator interpolator = new LinearInterpolator();
 		handler.post(new Runnable() {
 
 			@Override
 			public void run() {
 				long elapsed = SystemClock.uptimeMillis() - start;
 				float t = interpolator.getInterpolation((float) elapsed
 						/ duration);
 				double lng = t * dest.longitude + (1 - t) * org.longitude;
 				double lat = t * dest.latitude + (1 - t) * org.latitude;
 				marker.setPosition(new LatLng(lat, lng));
 				if (t < 1.0) {
 					handler.postDelayed(this, 16);
 				}
 			}
 		});
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_home, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 
 		
 //		case R.id.share:
 //			Intent intent = new Intent(mContext, Share.class);
 //			File SD = Environment.getExternalStorageDirectory();
 //			File dir = new File(SD,"Time2Fly");
 //			dir.mkdir();
 //			File exportDIR = new File(dir, "exports");
 //			exportDIR.mkdir();
 //			File file = new File(exportDIR, "t2f.jpg");
 //			intent.putExtra("path", file.getPath());
 //			startActivity(intent);
 //			break;
 		
 		case R.id.normal:
 			googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
 			break;
 
 		case R.id.sat:
 			googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
 			break;
 
 		case R.id.ter:
 			googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
 			break;
 
 		case R.id.settings:
 		
 			SharedResources.timer.cancel();
 			SharedResources.timer.purge();
 			
 			SharedResources.weatherTimer.cancel();
 			SharedResources.weatherTimer.purge();
 			
 //			finish();
 			Intent settings = new Intent(mContext, Settings.class);
 			startActivity(settings);
 			break;
 
 		case R.id.play:
 			if (SharedResources.weatherPlayed) {
 				SharedResources.weatherPlayed = false;
 				Bitmap b = BitmapFactory.decodeResource(getResources(),
 						R.drawable.play);
 				BitmapDrawable d = new BitmapDrawable(b);
 				item.setIcon(d);
 
 			} else {
 				SharedResources.weatherPlayed = true;
 				Bitmap b = BitmapFactory.decodeResource(getResources(),
 						R.drawable.pause);
 				BitmapDrawable d = new BitmapDrawable(b);
 				item.setIcon(d);
 			}
 			break;
 			
 			
 		case R.id.search:
 			if(SharedLayouts.searchBar.getVisibility() == View.VISIBLE){
 				SharedLayouts.searchBar.setVisibility(View.GONE);
 				SharedResources.searchFilter = false;
 				renderTargets();
 			}
 			else
 			{
 				int width = getWindowManager().getDefaultDisplay().getWidth();
 				SharedLayouts.searchBar.setVisibility(View.VISIBLE);
 				SharedLayouts.searchField.getLayoutParams().width = (int)(width / 6.5);
 				
 			}
 			break;
 			
 		case R.id.show:
 			if (SharedLayouts.leftSection.getVisibility() == View.VISIBLE)
 			{
 				SharedLayouts.leftSection.setVisibility(View.GONE);
 				SharedLayouts.leftSection.startAnimation(MyAnimations.mSlideOutTop);
 				item.setIcon(R.drawable.down);
 			}
 			else
 			{
 				SharedLayouts.leftSection.setVisibility(View.VISIBLE);
 				SharedLayouts.leftSection.startAnimation(MyAnimations.mSlideInTop);
 				item.setIcon(R.drawable.up);
 
 			}
 			break;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	private Tab addFlightTab(final Tab t, final String distance) {
 		Tab selectedTab = null;
 		cache.num_targets++;
 		LayoutInflater inflater = LayoutInflater.from(mContext);
 		LinearLayout list_item = (LinearLayout) inflater.inflate(
 				R.layout.custom_list_item, null);
 
 		if (t.addr.equalsIgnoreCase(cache.selectedReg)) {
 			list_item.setBackgroundResource(R.drawable.rounded_border_yellow);
 			selectedTab = t;
 		}
 		LinearLayout container = (LinearLayout) list_item.getChildAt(0);
 		TextView tv0 = (TextView) container.getChildAt(0);
 		tv0.setText(t.callSign);
 
 		TextView tv1 = (TextView) container.getChildAt(1);
 		tv1.setText(distance);
 
 		list_item.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View view) {
 
 				LinearLayout item = (LinearLayout) view;
 				if (cache.selectedReg.equalsIgnoreCase(t.addr)) {
 					item.setBackgroundResource(R.drawable.rounded_border);
 					cache.selectedReg = "";
 					t.marker.hideInfoWindow();
 					return;
 
 				}
 
 				for (int i = 0; i < SharedLayouts.drawer.getChildCount(); i++) {
 					LinearLayout childLayout = (LinearLayout) SharedLayouts.drawer
 							.getChildAt(i);
 					childLayout
 							.setBackgroundResource(R.drawable.rounded_border);
 				}
 				LinearLayout clickedLayout = (LinearLayout) view;
 				clickedLayout
 						.setBackgroundResource(R.drawable.rounded_border_yellow);
 
 				LatLng latLng = new LatLng(t.lat, t.lon);
 				googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
 						latLng, 11));
 
 				if (t.marker != null)
 					t.marker.showInfoWindow();
 				else
 					Toast.makeText(mContext, "NULL", 3000).show();
 
 				cache.selectedReg = t.addr;
 
 			}
 		});
 
 		TextView arrow = (TextView) list_item.getChildAt(1);
 		arrow.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				/*ScrollView sv = (ScrollView) SharedLayouts.drawer.getParent();
 				sv.setVisibility(View.GONE);*/
 				SharedLayouts.drawer1.setVisibility(View.GONE);
 				SharedLayouts.drawer2.setVisibility(View.VISIBLE);
 				updateDetailedView(t, distance);
 				LatLng latLng = new LatLng(t.lat, t.lon);
 				googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
 						latLng, 11));
 			}
 		});
 		list_item.setTag(t.addr);
 		SharedLayouts.drawer.addView(list_item);
 		return selectedTab;
 	}
 
 	private void initActionBar() {
 		ActionBar bar = getActionBar();
 		bar.setDisplayShowHomeEnabled(true);
 	}
 
 	private Location getCurrentLocation() {
 
 		LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		LocationListener mlocListener = new MyLocationListener();
 		mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0,
 				0, mlocListener);
 		Location loc = mlocManager
 				.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 
 		return loc;
 	}
 
 	private void addWeatherOverlay(Bitmap bmp) {
 		if (!appInstance.isWeatheroverlayEnabled())
 			return;
 		if (cache.weatherOverlay != null)
 			cache.weatherOverlay.remove();
 		LatLng southwest = null;
 		LatLng northeast = null;
 
 		if (cache.zoom <= 9) {
 			southwest = new LatLng(20.00107, 111.68321);
 			northeast = new LatLng(24.60560, 116.66013);
 		}
 
 		if (cache.zoom <= 11) {
 			southwest = new LatLng(21.15220, 112.92745);
 			northeast = new LatLng(23.45446, 115.41589);
 		}
 
 		else {
 			southwest = new LatLng(21.72777, 113.54956);
 			northeast = new LatLng(22.87890, 114.79378);
 		}
 
 		float transparency = appInstance.getWeatherOverlayTransparency();
 		LatLngBounds bounds = new LatLngBounds(southwest, northeast);
 		cache.weatherOverlay = googleMap
 				.addGroundOverlay(new GroundOverlayOptions()
 						.positionFromBounds(bounds).transparency(transparency)
 						.image(BitmapDescriptorFactory.fromBitmap(bmp)));
 	}
 
 	// ======= Network Opertaions =======
 	private class NetworkTask extends GetDataTask {
 		@Override
 		protected void onPostExecute(Boolean result) {
 			SharedResources.timePassed = 0;
 			SharedResources.started = true;
 			renderTargets();
 			TimerTask task = new TimerTask() {
 				@Override
 				public void run() {
 					runOnUiThread(refreshValsRunnable);
 				}
 			};
 			SharedResources.timer = new Timer();
 			SharedResources.timer.schedule(task, cache.update_rate);
 		}
 	}
 
 	private class WeatherTask extends GetWeatherOvelay {
 		@Override
 		protected void onPostExecute(Void result) {
 			File root = Environment.getExternalStorageDirectory();
 			root.mkdir();
 			final File appDir = new File(root, "Time2Fly");
 			appDir.mkdir();
 			
 			TimerTask task = new TimerTask() {
 				@Override
 				public void run() {
 					if (!SharedResources.weatherPlayed) {
 						return;
 					}
 					
 					String dirName = Utils.getInstance().getWeatherOverlayDir(cache.zoom);
 					Log.d("helal", "ZOOM :" + cache.zoom +" -- " + dirName);
 					File dir = new File(appDir, dirName);
 					dir.mkdir();
 					final File[] files = dir.listFiles();
 					SharedResources.round_robin++;
 					if (files == null || files.length == 0)
 						return;
 					SharedResources.round_robin =SharedResources. round_robin % files.length;
 					BitmapFactory.Options opts = new Options();
 					opts.inSampleSize = 2;
 					SharedResources.weather_bmp = BitmapFactory.decodeFile(
 							files[SharedResources.round_robin].getPath(), opts);
 					
 					runOnUiThread(playWeatherRunnable);
 				}
 			};
 			SharedResources.weatherTimer = new Timer();
 			SharedResources.weatherTimer.scheduleAtFixedRate(task, 0, 2000);
 		}
 	}
 
 	Runnable playWeatherRunnable = new Runnable() {
 		@Override
 		public void run() {
 			if (cache.weatherOverlay != null)
 				cache.weatherOverlay.remove();
 
 			float[] boundsArr = Utils.getInstance().getWeatherOverlayBounds(cache.zoom);
 			LatLng southwest = new LatLng(boundsArr[0], boundsArr[3]);
 			LatLng northeast = new LatLng(boundsArr[2], boundsArr[1]);
 			
 			float transparency = appInstance.getWeatherOverlayTransparency();
 			LatLngBounds bounds = new LatLngBounds(southwest, northeast);
 			cache.weatherOverlay = googleMap
 					.addGroundOverlay(new GroundOverlayOptions()
 							.positionFromBounds(bounds)
 							.transparency(transparency)
 							.image(BitmapDescriptorFactory
 									.fromBitmap(SharedResources.weather_bmp)));
 		}
 	};
 
 	// ===== Update Detailed View ======
 	private void updateDetailedView(final Tab t, String distance) {
 		SharedLayouts.back.setText(cache.num_targets + " Flights Tracked");
 
 		
 		float speed_in_km = t.spd * (float)1.852;
 		Location loc = new Location("t2f");
 		loc.setAltitude(t.alt);
 		loc.setLatitude(t.lat);
 		loc.setLongitude(t.lon);
 		loc.setSpeed(speed_in_km);
 		float dist = loc.distanceTo(cache.currentLoc)/1000;
 		
 		float time = dist/speed_in_km;
 		float mins = (time % 1);		
 		mins = mins * 60;
 
 		int minutes = (int)Math.floor(mins);
 		
 		String ETA = "ETA : " + minutes + " minutes";
 		
 		TextView tv = (TextView) SharedLayouts.drawer2.getChildAt(1);
 		String info = t.callSign + "\n" + t.spd + " Kts" + "\n" + t.vspd
 				+ " ft/min \n" + t.track + "°" + "\n" + t.owner + "\n"
 				+ distance + "\n" + "SQ : " + t.sqw + "\n" + "Radar ID : "
 				+ t.user_id + "\n"
 				+ETA;
 		tv.setText(info);
 
 		// LatLng latLng = new LatLng(t.lat, t.lon);
 		// googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
 		// latLng, 11));
 
 		if (t.marker != null)
 			t.marker.showInfoWindow();
 		else
 			Toast.makeText(mContext, "NULL", 3000).show();
 
 		LinearLayout share = (LinearLayout) SharedLayouts.drawer2.getChildAt(3);
 //		if(!SharedResources.facebookLogin)
 //			share.setVisibility(View.INVISIBLE);
 		share.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				if(t.marker!= null)
 					t.marker.showInfoWindow();
 				
 				LatLng latLng = new LatLng(t.lat, t.lon);
				googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));				
 				export();
				
				/*File SD = Environment.getExternalStorageDirectory();
				File dir = new File(SD, "Time2Fly");
				File exports = new File(dir, "exports");
				exports.mkdirs();
				File file = new File(exports, "t2f.jpg");
				Intent intent = new Intent(mContext, Share.class);
				intent.putExtra("path", file.getPath());
				startActivity(intent);*/
 			}
 		});
 
 		
 		// GET URL HERE
 		ImageView imgV = (ImageView)findViewById(R.id.queried_image);
 		if(!t.imageLoaded){
 			String url = "http://www.airliners.net/search/photo.search?regsearch="+t.reg+"&distinct_entry=true";
 			QueriedImageTask task = new QueriedImageTask();
 			task.imgLoader = imgLoader;
 			task.imgV = imgV;
 			task.url = url;
 			task.execute();
 			t.imageLoaded = true;
 		}
 		cache.selectedReg = t.addr;
 	}
 
 
 	// ====== Exporter =====
 	File newImage;	
 	private void export(){
 		File dir = new File(Environment.getExternalStorageDirectory(),"Time2Fly");
 		dir.mkdir();
 		File PhotoDir = new File(dir, "exports");
 		PhotoDir.mkdir();
 		PhotoDir.setWritable(true);
 		newImage = new File(PhotoDir, "t2f.jpg");
 		googleMap.snapshot(callBack);
 		
 	}
 	SnapshotReadyCallback callBack = new SnapshotReadyCallback() {
 		@Override
 		public void onSnapshotReady(Bitmap snapshot) {
 			try{
 			
 			FileOutputStream out;
 			out = new FileOutputStream(newImage);
 			snapshot.compress(Bitmap.CompressFormat.JPEG, 100, out);			
 			//timer.cancel();
 			//timer.purge();
 			Intent intent = new Intent(mContext, Share.class);
 			intent.putExtra("path", newImage.getPath());
 			startActivity(intent);
 		
 			
 			}
 			catch (Exception e) {
 				Log.d(Constants.TAG, "Snapshot fialed");
 			}
 		}
 	};
 
 	@Override
 	public void onConfigurationChanged(android.content.res.Configuration newConfig) {
 		super.onConfigurationChanged(newConfig);
 		int width = getWindowManager().getDefaultDisplay().getWidth();
 		LinearLayout leftSection = (LinearLayout)findViewById(R.id.left_section);
 		leftSection.getLayoutParams().width = width / 4 ;
 		SharedLayouts.searchField.getLayoutParams().width = (int)(width / 6.5);
 	};
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		SharedResources.timer = new Timer();
 		SharedResources.weatherTimer = new Timer();
 	}
 
 }
