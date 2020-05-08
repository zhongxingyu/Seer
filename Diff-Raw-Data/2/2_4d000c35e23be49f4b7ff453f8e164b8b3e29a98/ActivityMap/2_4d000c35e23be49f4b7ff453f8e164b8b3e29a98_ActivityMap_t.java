 package com.coffeeandpower.tab.activities;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.graphics.drawable.Drawable;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.view.animation.RotateAnimation;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 
 import com.coffeeandpower.AppCAP;
 import com.coffeeandpower.Constants;
 import com.coffeeandpower.R;
 import com.coffeeandpower.RootActivity;
 import com.coffeeandpower.activity.ActivityEnterInviteCode;
 import com.coffeeandpower.activity.ActivityLoginPage;
 import com.coffeeandpower.cont.DataHolder;
 import com.coffeeandpower.cont.User;
 import com.coffeeandpower.cont.UserSmart;
 import com.coffeeandpower.cont.VenueSmart;
 import com.coffeeandpower.datatiming.CounterData;
 import com.coffeeandpower.inter.TabMenu;
 import com.coffeeandpower.inter.UserMenu;
 import com.coffeeandpower.maps.BalloonItemizedOverlay;
 import com.coffeeandpower.maps.MyItemizedOverlay;
 import com.coffeeandpower.maps.MyOverlayItem;
 import com.coffeeandpower.maps.PinDrawable;
 import com.coffeeandpower.utils.Executor;
 import com.coffeeandpower.utils.Executor.ExecutorInterface;
 import com.coffeeandpower.utils.UserAndTabMenu;
 import com.coffeeandpower.utils.UserAndTabMenu.OnUserStateChanged;
 import com.coffeeandpower.utils.Utils;
 import com.coffeeandpower.views.CustomDialog;
 import com.coffeeandpower.views.CustomDialog.ClickListener;
 import com.coffeeandpower.views.CustomFontView;
 import com.coffeeandpower.views.HorizontalPagerModified;
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 import com.google.android.maps.Overlay;
 import com.urbanairship.UAirship;
 
 public class ActivityMap extends RootActivity implements TabMenu, UserMenu, Observer {
 	private static final int SCREEN_SETTINGS = 0;
 	private static final int SCREEN_MAP = 1;
 
 	private static final int ACTIVITY_ACCOUNT_SETTINGS = 1888;
 	public static final int ACCOUNT_CHANGED = 1900;
 
 	private UserAndTabMenu menu;
 
 	// Views
 	private CustomFontView textNickName;
 	private HorizontalPagerModified pager;
 	private ImageView imageRefresh;
 
 	// Map items
 	private MapView mapView;
 	private MapController mapController;
 	private MyLocationOverlay myLocationOverlay;
 	private MyItemizedOverlay itemizedoverlay;
 	private LocationManager locationManager;
 	
 	private ProgressDialog progress;
 
 	// Current user
 	private User loggedUser;
 
 	private DataHolder result;
 
 	private Executor exe;
 	
 	// Scheduler - create a custom message handler for use in passing venue data from background API call to main thread
 	protected Handler taskHandler = new Handler() {
 		
 		@Override
 		public void handleMessage(Message msg) {
 			
 			// pass message data along to venue update method
 			ArrayList<VenueSmart> venueArray = msg.getData().getParcelableArrayList("venues");
 			ArrayList<UserSmart> userArray = msg.getData().getParcelableArrayList("users");
 			updateVenuesAndCheckinsFromApiResult(venueArray, userArray);
 
 			progress.dismiss();
 			super.handleMessage(msg);
 		}
 	};
 
 	/**
 	 * Check if user is checked in or not
 	 */
 	private void checkUserState() {
 		if (AppCAP.isUserCheckedIn()) {
 			((TextView) findViewById(R.id.textview_check_in)).setText("Check Out");
 			((ImageView) findViewById(R.id.imageview_check_in_clock_hand)).setAnimation(AnimationUtils.loadAnimation(ActivityMap.this,
 					R.anim.rotate_indefinitely));
 		} else {
 			((TextView) findViewById(R.id.textview_check_in)).setText("Check In");
 			((ImageView) findViewById(R.id.imageview_check_in_clock_hand)).clearAnimation();
 		}
 	}
 
 	@Override
 	protected void onCreate(Bundle icicle) {
 		
 		super.onCreate(icicle);
 		
 		if (Constants.debugLog)
 			Log.d("Coffee","Creating ActivityMap...");
 		
 		setContentView(R.layout.tab_activity_map);
 		
 		AppCAP.startCounter();
 		
 		
 		
 
 		progress = new ProgressDialog(this);
 		progress.setMessage("Loading...");
 		progress.show();
 
 		// Executor
 		exe = new Executor(ActivityMap.this);
 		//We need this to get the user Id
 		exe.setExecutorListener(new ExecutorInterface() {
 			@Override
 			public void onErrorReceived() {
 				errorReceived();
 			}
 
 			@Override
 			public void onActionFinished(int action) {
 				actionFinished(action);
 			}
 		});
 		
 
 		// Views
 		pager = (HorizontalPagerModified) findViewById(R.id.pager);
 		mapView = (MapView) findViewById(R.id.mapview);
 		textNickName = (CustomFontView) findViewById(R.id.text_nick_name);
 		imageRefresh = (ImageView) findViewById(R.id.imagebutton_map_refresh_progress);
 		myLocationOverlay = new MyLocationOverlay(this, mapView);
 		Drawable drawable = this.getResources().getDrawable(R.drawable.people_marker_turquoise_circle);
 		itemizedoverlay = new MyItemizedOverlay(drawable, mapView);
 
 		// Views states
 		pager.setCurrentScreen(SCREEN_MAP, false);
 
 		// User and Tab Menu
 		menu = new UserAndTabMenu(this);
 		menu.setOnUserStateChanged(new OnUserStateChanged() {
 			@Override
 			public void onCheckOut() {
 				checkUserState();
 				refreshMapDataSet();
 			}
 
 			@Override
 			public void onLogOut() {
 				onBackPressed();
 				// Map Activity is root, so start Login Activity
 				// from here
 				startActivity(new Intent(ActivityMap.this, ActivityLoginPage.class));
 			}
 		});
 
 		((RelativeLayout) findViewById(R.id.rel_map)).setBackgroundResource(R.drawable.bg_tabbar_selected);
 		((ImageView) findViewById(R.id.imageview_map)).setImageResource(R.drawable.tab_map_pressed);
 		((TextView) findViewById(R.id.text_map)).setTextColor(Color.WHITE);
 
 		// Set others
 		mapView.getOverlays().add(myLocationOverlay);
 		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		try {
 			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new GeoUpdateHandler());
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 			new CustomDialog(ActivityMap.this, "Info", "Location Manager error").show();
 		}
 		myLocationOverlay.runOnFirstFix(new Runnable() {
 			public void run() {
 				mapView.getController().animateTo(myLocationOverlay.getMyLocation());
 				AppCAP.setUserCoordinates(getSWAndNECoordinatesBounds(mapView));
 				runOnUiThread(new Runnable() {
 					@Override
 					public void run() {
 						mapController.setZoom(17);
 						refreshMapDataSet();
 					}
 				});
 			}
 		});
 
 		mapController = mapView.getController();
 		mapController.setZoom(12);
		//Hardcoded to US until we get a fix
		mapController.zoomToSpan(100448195, 94921874);
 
 		// User is logged in, get user data
 		if (AppCAP.isLoggedIn()) {
 			exe.getUserData();
 		}
 
 		// Listener for autorefresh map
 		mapView.setOnTouchListener(new OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 
 				switch (event.getAction()) {
 
 				case MotionEvent.ACTION_DOWN:
 					firstX = event.getX();
 					firstY = event.getY();
 					break;
 
 				case MotionEvent.ACTION_CANCEL:
 					if (event.getX() > firstX + 10 || event.getX() < firstX - 10 || event.getY() > firstY + 10
 							|| event.getY() < firstY - 10) {
 						refreshMapDataSet();
 						firstX = event.getX();
 						firstY = event.getY();
 					}
 
 					break;
 
 				case MotionEvent.ACTION_UP:
 					if (event.getX() > firstX + 10 || event.getX() < firstX - 10 || event.getY() > firstY + 10
 							|| event.getY() < firstY - 10) {
 						refreshMapDataSet();
 						firstX = event.getX();
 						firstY = event.getY();
 					}
 					hideBaloons();
 					break;
 				}
 				return false;
 			}
 		});
 	}
 
 	float firstX = 0;
 	float firstY = 0;
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		
 		if (Constants.debugLog)
 			Log.d("ActivityMap","ActivityMap.onStart(): " + AppCAP.isUserCheckedIn());
 		
 		checkUserState();
 		
 		
 
 		if (AppCAP.isFirstStart()) {
 			startActivity(new Intent(ActivityMap.this, ActivityEnterInviteCode.class));
 		} else if (AppCAP.shouldShowInfoDialog()) {
 			CustomDialog cd = new CustomDialog(ActivityMap.this,
 					"Coffee & Power requires an invite for full membership but you have 30 days of full access to try us out.",
 					"If you get an invite from another C&P user you can enter it anytime by going to the Account page/Enter invite code tab.");
 			cd.setOnClickListener(new ClickListener() {
 				@Override
 				public void onClick() {
 					AppCAP.dontShowInfoDialog();
 					myLocationOverlay.enableMyLocation();
 					refreshMapDataSet();
 				}
 			});
 			cd.show();
 		} else {
 			if (AppCAP.shouldFinishActivities()) {
 				startActivity(new Intent(ActivityMap.this, ActivityLoginPage.class));
 				onBackPressed();
 			} else {
 				myLocationOverlay.enableMyLocation();
 
 				// Refresh Data
 				refreshMapDataSet();
 			}
 		}
 		
 	}
 
 	/**
 	 * Create point on Map with data from MapUserdata
 	 * 
 	 * @param point
 	 * @param foursquareIdKey
 	 * @param checkinsSum
 	 * @param venueName
 	 * @param isList
 	 */
 	private void createMarker(GeoPoint point, VenueSmart currVenueSmart, int checkinsSum, String venueName, boolean isPin) {
 		if (currVenueSmart != null) {
 			String checkStr = "";
 			if (!isPin) {
 				checkStr = checkinsSum == 1 ? " checkin in the last week" : " checkins in the last week";
 			} else {
 				checkStr = checkinsSum == 1 ? " person here now" : " persons here now";
 			}
 			venueName = AppCAP.cleanResponseString(venueName);
 
 			MyOverlayItem overlayitem = new MyOverlayItem(point, venueName, checkinsSum + checkStr);
 			//overlayitem.setMapUserData(foursquareIdKey);
 			overlayitem.setVenueSmartData(currVenueSmart);
 
 			if (myLocationOverlay.getMyLocation() != null) {
 				overlayitem.setMyLocationCoords(myLocationOverlay.getMyLocation().getLatitudeE6(), myLocationOverlay.getMyLocation()
 						.getLongitudeE6());
 			}
 
 			// Pin or marker
 			if (isPin) {
 				overlayitem.setPin(true);
 				overlayitem.setMarker(getPinDrawable(checkinsSum, point));
 			}
 
 			itemizedoverlay.addOverlay(overlayitem);
 		}
 	}
 
 	private Drawable getPinDrawable(int checkinsNum, GeoPoint gp) {
 		PinDrawable icon = new PinDrawable(this, checkinsNum);
 		icon.setBounds(0, -icon.getIntrinsicHeight(), icon.getIntrinsicWidth(), 0);
 		return icon;
 	}
 
 	// We have user data from logged user, use it now...
 	public void useUserData() {
 		AppCAP.setLoggedInUserId(loggedUser.getUserId());
 		AppCAP.setLoggedInUserNickname(loggedUser.getNickName());
 		textNickName.setText(loggedUser.getNickName());
 	}
 
 	public class GeoUpdateHandler implements LocationListener {
 		@Override
 		public void onLocationChanged(Location location) {
 			// int lat = (int) (location.getLatitude() * 1E6);
 			// int lng = (int) (location.getLongitude() * 1E6);
 			// GeoPoint point = new GeoPoint(lat, lng);
 
 			// if (Constants.debugLog)
 			//	Log.d("LOG", "ActivityMap locationChanged: " +
 			// location.getLatitude()+":"+location.getLongitude());
 		}
 
 		@Override
 		public void onProviderDisabled(String provider) {
 			RootActivity.log("ActivityMap provider: " + provider);
 		}
 
 		@Override
 		public void onProviderEnabled(String provider) {
 			RootActivity.log("ActivityMap providerEnabled");
 		}
 
 		@Override
 		public void onStatusChanged(String provider, int status, Bundle extras) {
 			RootActivity.log("ActivityMap statusChanged");
 		}
 	}
 
 	public void onClickMenu(View v) {
 		if (pager.getCurrentScreen() == SCREEN_MAP) {
 			pager.setCurrentScreen(SCREEN_SETTINGS, true);
 		} else {
 			pager.setCurrentScreen(SCREEN_MAP, true);
 		}
 
 	}
 
 	public void onClickLocateMe(View v) {
 		if (myLocationOverlay != null) {
 			if (myLocationOverlay.getMyLocation() != null) {
 				mapController.animateTo(myLocationOverlay.getMyLocation());
 				mapController.setZoom(17);
 			}
 		}
 	}
 
 	public void onClickRefresh(View v) {
 		refreshMapDataSet();
 	}
 
 	public void hideBaloons() {
 		List<Overlay> mapOverlays = mapView.getOverlays();
 		for (Overlay overlay : mapOverlays) {
 			if (overlay instanceof BalloonItemizedOverlay<?>) {
 				((BalloonItemizedOverlay<?>) overlay).hideBalloon();
 			}
 		}
 	}
 
 	private void refreshMapDataSet() {
 		checkUserState();
 		
 		int iconSize = Utils.getScreenDependentItemSize(Utils.REFRESH_ICON_SIZE);
 
 		Animation anim = new RotateAnimation(360.0f, 0.0f, iconSize / 2, iconSize / 2);
 		anim.setDuration(1000);
 		anim.setRepeatCount(0);
 		anim.setRepeatMode(Animation.REVERSE);
 		anim.setFillAfter(true);
 		imageRefresh.setAnimation(anim);
 
 		hideBaloons();
 
 		//exe.getVenuesAndUsersWithCheckinsInBoundsDuringInterval(getSWAndNECoordinatesBounds(mapView), false);
 		//AppCAP.getCounter().manualTrigger();
 
 		// For every refresh save Map coordinates
 		AppCAP.setUserCoordinates(getSWAndNECoordinatesBounds(mapView));
 
 		// Get Notification settings from shared prefs
 		((ToggleButton) findViewById(R.id.toggle_checked_in)).setChecked(AppCAP.getNotificationToggle());
 		((Button) findViewById(R.id.btn_from)).setText(AppCAP.getNotificationFrom());
 
 		// Check and Set Notification settings
 		menu.setOnNotificationSettingsListener((ToggleButton) findViewById(R.id.toggle_checked_in), (Button) findViewById(R.id.btn_from),
 				true);
 
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 
 		switch (requestCode) {
 
 		case ACTIVITY_ACCOUNT_SETTINGS:
 			if (resultCode == ACCOUNT_CHANGED) {
 				exe.getUserData();
 			}
 			break;
 		}
 	}
 
 	/**
 	 * [0]sw_lat; [1]sw_lng; [2]ne_lat; [3]ne_lng;
 	 * 
 	 * @param map
 	 * @return
 	 */
 	private double[] getSWAndNECoordinatesBounds(MapView map) {
 		double[] data = new double[6];
 
 		GeoPoint pointCenterMap = map.getMapCenter();
 		int lngSpan = map.getLongitudeSpan();
 		int latSpan = map.getLatitudeSpan();
 
 		GeoPoint sw = new GeoPoint(pointCenterMap.getLatitudeE6() - latSpan / 2, pointCenterMap.getLongitudeE6() - lngSpan / 2);
 		GeoPoint ne = new GeoPoint(pointCenterMap.getLatitudeE6() + latSpan / 2, pointCenterMap.getLongitudeE6() + lngSpan / 2);
 
 		data[0] = sw.getLatitudeE6() / 1E6; // sw_lat
 		data[1] = sw.getLongitudeE6() / 1E6; // sw_lng
 		data[2] = ne.getLatitudeE6() / 1E6; // ne_lat
 		data[3] = ne.getLongitudeE6() / 1E6; // ne_lng
 		data[4] = 0;
 		data[5] = 0;
 
 		if (myLocationOverlay.getMyLocation() != null) {
 			data[4] = myLocationOverlay.getMyLocation().getLatitudeE6() / 1E6;
 			data[5] = myLocationOverlay.getMyLocation().getLongitudeE6() / 1E6;
 		}
 		return data;
 	}
 
 	@Override
 	public void onBackPressed() {
 		super.onBackPressed();
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 	}
 
 	@Override
 	protected void onDestroy() {
 		myLocationOverlay.disableMyLocation();
 
 		//if (Constants.debugLog)
 		//	Log.d("ActivityMap","onDestroy(): stopping counter...");
 		//AppCAP.getCounter().stop();
 		
 		if (AppCAP.shouldFinishActivities() && AppCAP.shouldStartLogIn()) {
 			startActivity(new Intent(ActivityMap.this, ActivityLoginPage.class));
 			AppCAP.setShouldStartLogIn(false);
 		}
 
 		super.onDestroy();
 	}
 
 	@Override
 	public void onClickEnterInviteCode(View v) {
 		menu.onClickEnterInviteCode(v);
 	}
 
 	@Override
 	public void onClickMap(View v) {
 		// menu.onClickMap(v);
 	}
 
 	@Override
 	public void onClickPlaces(View v) {
 		menu.onClickPlaces(v);
 		// finish();
 	}
 
 	@Override
 	public void onClickPeople(View v) {
 		menu.onClickPeople(v);
 		// finish();
 	}
 
 	@Override
 	public void onClickContacts(View v) {
 		menu.onClickContacts(v);
 		// finish();
 	}
 
 	@Override
 	public void onClickSettings(View v) {
 		menu.onClickSettings(v);
 	}
 
 	@Override
 	public void onClickCheckIn(View v) {
 		if (AppCAP.isLoggedIn()) {
 			menu.onClickCheckIn(v);
 		} else {
 			showDialog(DIALOG_MUST_BE_A_MEMBER);
 		}
 	}
 
 	public void onClickWallet(View v) {
 		menu.onClickWallet(v);
 	}
 
 	public void onClickLogout(View v) {
 		menu.onClickLogout(v);
 	}
 
 	@Override
 	protected void onStart() {
 		if (Constants.debugLog)
 			Log.d("ActivityMap","ActivityMap.onStart()");
 		super.onStart();
 		checkUserState();
 		UAirship.shared().getAnalytics().activityStarted(this);
 		AppCAP.getCounter().getCachedDataForAPICall("venuesWithCheckins",this);
 	}
 
 	@Override
 	public void onStop() {
 		if (Constants.debugLog)
 			Log.d("ActivityMap","ActivityMap.onStop()");
 		super.onStop();
 		UAirship.shared().getAnalytics().activityStopped(this);
 		
 		AppCAP.getCounter().stoppedObservingAPICall("venuesWithCheckins",this);
 	}
 
 	private void errorReceived() {
 
 	}
 
 	private void actionFinished(int action) {
 		result = exe.getResult();
 
 		switch (action) {
 		case Executor.HANDLE_GET_USER_DATA:
 			if (result.getObject() != null) {
 				if (result.getObject() instanceof User) {
 					loggedUser = (User) result.getObject();
 					useUserData();
 				}
 			}
 		}
 
 	}
 	@Override
 	public void update(Observable observable, Object data) {
 		/*
 		 * verify that the data is really of type CounterData, and log the
 		 * details
 		 */
 		if (Constants.debugLog)
 			Log.d("ActivityMap","update()");
 		
 		if (data instanceof CounterData) {
 			CounterData counterdata = (CounterData) data;
 			DataHolder venuesWithCheckins = counterdata.getData();
 						
 			Object[] obj = (Object[]) venuesWithCheckins.getObject();
 			@SuppressWarnings("unchecked")
 			ArrayList<VenueSmart> arrayVenues = (ArrayList<VenueSmart>) obj[0];
 			@SuppressWarnings("unchecked")
 			ArrayList<UserSmart> arrayUsers = (ArrayList<UserSmart>) obj[1];
 			
 			Message message = new Message();
 			Bundle bundle = new Bundle();
 			bundle.putCharSequence("type", counterdata.type);
 			bundle.putParcelableArrayList("venues", arrayVenues);
 			bundle.putParcelableArrayList("users", arrayUsers);
 			message.setData(bundle);
 			
 			if (Constants.debugLog)
 				Log.d("Map","ActivityMap.update: Sending handler message...");
 			taskHandler.sendMessage(message);
 			
 			
 		}
 	}
 	
 	
 	private void updateVenuesAndCheckinsFromApiResult(ArrayList<VenueSmart> venueArray, ArrayList<UserSmart> arrayUsers) {
 		
 		if (Constants.debugLog)
 			Log.d("Map","updateVenuesAndCheckinsFromApiResult()");
 		itemizedoverlay.clear();
 		
 		for (VenueSmart venue : venueArray) {
 			GeoPoint gp = new GeoPoint((int) (venue.getLat() * 1E6), (int) (venue.getLng() * 1E6));
 
 			if (venue.getCheckins() > 0) {
 				createMarker(gp, venue, venue.getCheckins(), venue.getName(), true);
 			} else if (venue.getCheckinsForWeek() > 0) {
 				createMarker(gp, venue, venue.getCheckinsForWeek(), venue.getName(), false); // !!!
 															       // getCheckinsForWeek
 			}
 		}
 		
 		for (UserSmart user : arrayUsers) {
 			if (user.getUserId() == AppCAP.getLoggedInUserId()) {
 				if (user.getCheckedIn() == 1) {
 					AppCAP.setUserCheckedIn(true);
 				} else {
 					AppCAP.setUserCheckedIn(false);
 				}
 			}
 		}
 		
 		if (itemizedoverlay.size() > 0) {
 			mapView.getOverlays().add(itemizedoverlay);
 		}
 		checkUserState();
 		mapView.invalidate();
 		
 	}
 	
 	
 	// Capture the user pressing the back button in the map view and exit the app
 	// Move this to a separate function
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 	    if (keyCode == KeyEvent.KEYCODE_BACK) {
 		    if (Constants.debugLog)
 				Log.d("Coffee","User exit detected.");
 	        
 	        UAirship.land();
 	        AppCAP.getCounter().stop();
 	        
 	    }
 	    return super.onKeyDown(keyCode, event);
 	}
 
 	
 	
 	
 }
