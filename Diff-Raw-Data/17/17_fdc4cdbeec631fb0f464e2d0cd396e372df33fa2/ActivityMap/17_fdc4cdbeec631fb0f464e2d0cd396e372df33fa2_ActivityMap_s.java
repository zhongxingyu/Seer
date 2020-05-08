 package com.coffeeandpower.tab.activities;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
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
 import com.coffeeandpower.cache.CacheMgrService;
 import com.coffeeandpower.cache.CachedDataContainer;
 import com.coffeeandpower.cont.DataHolder;
 import com.coffeeandpower.cont.UserSmart;
 import com.coffeeandpower.cont.VenueSmart;
 import com.coffeeandpower.inter.TabMenu;
 import com.coffeeandpower.inter.UserMenu;
 import com.coffeeandpower.location.LocationDetectionService;
 import com.coffeeandpower.location.LocationDetectionStateMachine;
 import com.coffeeandpower.maps.BalloonItemizedOverlay;
 import com.coffeeandpower.maps.MyItemizedOverlay;
 import com.coffeeandpower.maps.MyOverlayItem;
 import com.coffeeandpower.maps.PinDrawable;
 import com.coffeeandpower.utils.Executor;
 import com.coffeeandpower.utils.Executor.ExecutorInterface;
 import com.coffeeandpower.utils.UserAndTabMenu;
 import com.coffeeandpower.utils.UserAndTabMenu.OnUserStateChanged;
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
 
 public class ActivityMap extends RootActivity implements TabMenu, UserMenu {
 	
 	private final String TAG = "ActivityMap";
 	
 	private static final int SCREEN_SETTINGS = 0;
 	private static final int SCREEN_MAP = 1;
 
 	private static final int ACTIVITY_ACCOUNT_SETTINGS = 1888;
 	public static final int ACCOUNT_CHANGED = 1900;
 	
 	private UserAndTabMenu menu;
 
 	// Views
 	private CustomFontView textNickName;
 	private HorizontalPagerModified pager;
 	private ImageView imageRefresh;
     
     private double[] pinScales = { 0.326 , // for 1 person
             0.57, 0.74, 0.855, 0.932, 0.976, 
             1.0 // for 7 or more people
     };
 
 	// Map items
 	private MapView mapView;
 	private MapController mapController;
 	private MyLocationOverlay myLocationOverlay;
 	private MyItemizedOverlay itemizedoverlay;
 
    // Views
    private CustomFontView textNickName;
    private HorizontalPagerModified pager;
    private ImageView imageRefresh;
 
 	private ProgressDialog progress;
 
 	// Current user
 	private UserSmart loggedUser;
 
 	private DataHolder result;
 
 	private Executor exe;
 	
 	float firstX = 0;
 	float firstY = 0;
 	
 	private MyCachedDataObserver myCachedDataObserver = new MyCachedDataObserver();
 	private MyAutoCheckinTriggerObserver myAutoCheckinObserver = new MyAutoCheckinTriggerObserver();
 	
 	// Scheduler - create a custom message handler for use in passing venue data from background API call to main thread
 	private Handler mainThreadTaskHandler = new Handler() {
 
 		@Override
 		public void handleMessage(Message msg) {
 
 			// Determine which message type is being sent
 			String type = msg.getData().getString("type");
 			
 			if (type.equalsIgnoreCase("AutoCheckinTrigger")) {
 				checkUserState();
 			}
 			else { // if the message isn't an autocheckin trigger, assume its a cached data update
         			// pass message data along to venue update method
             ArrayList<VenueSmart> venueArray = msg.getData()
                     .getParcelableArrayList("venues");
             ArrayList<UserSmart> userArray = msg.getData()
                     .getParcelableArrayList("users");
         			updateVenuesAndCheckinsFromApiResult(venueArray, userArray);
         
         			progress.dismiss();
 			}
 			super.handleMessage(msg);
 		}
 	};
 
 	
 	
 	//====================================================================
 	// Lifecycle Management
 	//====================================================================
     
     
     /**
      * Check if user is checked in or not
      */
     private void checkUserState() {
         if (AppCAP.isUserCheckedIn()) {
             ((TextView) findViewById(R.id.textview_check_in)).setText("Check Out");
             //((ImageView) findViewById(R.id.imageview_check_in_clock_hand)).setAnimation(AnimationUtils.loadAnimation(ActivityMap.this,
             //        R.anim.rotate_indefinitely));
         } else {
             ((TextView) findViewById(R.id.textview_check_in)).setText("Check In");
             //((ImageView) findViewById(R.id.imageview_check_in_clock_hand)).clearAnimation();
         }
     }
 
 	@Override
 	protected void onCreate(Bundle icicle) {
 
 		super.onCreate(icicle);
 
 		if (Constants.debugLog)
 			Log.d(TAG,"Creating ActivityMap...");
 		
 		setContentView(R.layout.tab_activity_map);
 
 		// start services
 		AppCAP.mainActivityDidStart(this);		
 
 		progress = new ProgressDialog(this);
 		progress.setMessage("Loading...");
 		progress.show();
 
 		// Executor
 		exe = new Executor(ActivityMap.this);
         // We need this to get the user Id
 		exe.setExecutorListener(new ExecutorInterface() {
 			@Override
 			public void onErrorReceived() {
 				//errorReceived();
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
         Drawable drawable = this.getResources().getDrawable(
                 R.drawable.pin_checkedout);
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
                 startActivity(new Intent(ActivityMap.this,
                         ActivityLoginPage.class));
 			}
 		});
 
         ((RelativeLayout) findViewById(R.id.rel_map))
                 .setBackgroundResource(R.drawable.bg_tabbar_selected);
         ((ImageView) findViewById(R.id.imageview_map))
                 .setImageResource(R.drawable.tab_places_pressed);
 
 		// Set others
 		mapView.getOverlays().add(myLocationOverlay);
 		myLocationOverlay.runOnFirstFix(new Runnable() {
 			public void run() {
 				RootActivity.log("ActivityMap First Fix Hit");
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
         // Hardcoded to US until we get a fix
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
                     if (event.getX() > firstX + 10
                             || event.getX() < firstX - 10
                             || event.getY() > firstY + 10
 							|| event.getY() < firstY - 10) {
 						refreshMapDataSet();
 						firstX = event.getX();
 						firstY = event.getY();
 					}
 
 					break;
 
 				case MotionEvent.ACTION_UP:
                     if (event.getX() > firstX + 10
                             || event.getX() < firstX - 10
                             || event.getY() > firstY + 10
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
 		
 		//ProximityManager.onStart(this);
 	}
 
 	
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		if (Constants.debugLog)
             Log.d("ActivityMap",
                     "ActivityMap.onStart(): " + AppCAP.isUserCheckedIn());
 
 		checkUserState();
 
         if (AppCAP.isFirstStart() && AppCAP.getEnteredInviteCode() == false) {
             startActivity(new Intent(ActivityMap.this,
                     ActivityEnterInviteCode.class));
         } else if (AppCAP.shouldShowInfoDialog()
                 && AppCAP.getEnteredInviteCode() == false) {
             CustomDialog cd = new CustomDialog(
                     ActivityMap.this,
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
                 startActivity(new Intent(ActivityMap.this,
                         ActivityLoginPage.class));
 				onBackPressed();
 			} else {
 				myLocationOverlay.enableMyLocation();
 
 				// Refresh Data
 				refreshMapDataSet();
 			}
 		}
 	}
 	
 	
 	@Override
 	protected void onPause() {
 		super.onPause();
 	}
     
     private double getScaleFactor(int number) {       
         if (number <= 0) {
             return pinScales[0];
         } else if (number >= pinScales.length) {
             return pinScales[pinScales.length - 1];
         } else {
             return pinScales[number - 1];
         }
 
 	@Override
 	protected void onDestroy() {
 		myLocationOverlay.disableMyLocation();
 
 		//CacheMgrService.stopPeriodicTimer();
 		
 		if (AppCAP.shouldFinishActivities() && AppCAP.shouldStartLogIn()) {
 			startActivity(new Intent(ActivityMap.this, ActivityLoginPage.class));
 			AppCAP.setShouldStartLogIn(false);
 		}
 
 		super.onDestroy();
 	}
 	
 	
 	
 	@Override
 	protected void onStart() {
 		if (Constants.debugLog)
 			Log.d(TAG,"ActivityMap.onStart()");
 		super.onStart();
 		checkUserState();
 		UAirship.shared().getAnalytics().activityStarted(this);
 		CacheMgrService.startObservingAPICall("venuesWithCheckins",myCachedDataObserver);
 		LocationDetectionStateMachine.startObservingAutoCheckinTrigger(myAutoCheckinObserver);
 	}
 
 	@Override
 	public void onStop() {
 		if (Constants.debugLog)
 			Log.d(TAG,"ActivityMap.onStop()");
 		super.onStop();
 		UAirship.shared().getAnalytics().activityStopped(this);
 		
 		
 		CacheMgrService.stopObservingAPICall("venuesWithCheckins",myCachedDataObserver);
 		LocationDetectionStateMachine.stopObservingAutoCheckinTrigger(myAutoCheckinObserver);
 		
 		//Lets turn off the GPS when we exit the map screen
 		if (Constants.debugLog)
 			Log.d(TAG,"Disabling location updates");
 		myLocationOverlay.disableMyLocation();
 	}
 	
 	
 	// Capture the user pressing the back button in the map view and exit the app
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 	    if (keyCode == KeyEvent.KEYCODE_BACK) {
 		    if (Constants.debugLog)
 				Log.d(TAG,"User exit detected.");
 	        
 	        AppCAP.applicationWillExit(this);
 	        
 	    }
 	    return super.onKeyDown(keyCode, event);
 	}
 	
 		
 	
 	//====================================================================
 	// Map View Management
 	//====================================================================
 	
 
	/**
	 * Create point on Map with data from MapUserdata
	 * 
	 * @param point
	 * @param foursquareIdKey
	 * @param checkinsSum
	 * @param venueName
	 * @param isList
	 */
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
     private void createMarker(GeoPoint point, VenueSmart currVenueSmart,
             int checkinsSum, String venueName, boolean isPin) {
         Drawable drawable;
         if (currVenueSmart != null) {
             String checkStr = "";
             if (!isPin) {
                 checkStr = checkinsSum == 1 ? " checkin in the last week"
                         : " checkins in the last week";
 			} else {
                 checkStr = checkinsSum == 1 ? " person here now"
                         : " persons here now";
 			}
 			venueName = AppCAP.cleanResponseString(venueName);
 
             MyOverlayItem overlayitem = new MyOverlayItem(point, venueName,
                     checkinsSum + checkStr);
             // overlayitem.setMapUserData(foursquareIdKey);
 			overlayitem.setVenueSmartData(currVenueSmart);
 
 			if (myLocationOverlay.getMyLocation() != null) {
                 overlayitem.setMyLocationCoords(myLocationOverlay
                         .getMyLocation().getLatitudeE6(), myLocationOverlay
                         .getMyLocation().getLongitudeE6());
 			}
 
             // Pin or marker
             if (isPin) {
                 overlayitem.setPin(true);
                 overlayitem.setMarker(getPinDrawable(checkinsSum, point));
             } else {
                 if (currVenueSmart.getSpecialVenueType().compareTo("solar") == 0) {
                     drawable = this.getResources().getDrawable(R.drawable.pin_solar);
                 } else {
                     drawable = this.getResources().getDrawable(R.drawable.pin_checkedout);
                 }
                 Bitmap d = ((BitmapDrawable)drawable).getBitmap();
                 double scaleFactor = getScaleFactor(checkinsSum);
                 Bitmap bitmapOrig = Bitmap.createScaledBitmap(d, (int) (scaleFactor * drawable.getIntrinsicWidth()), 
                         (int) (scaleFactor * drawable.getIntrinsicHeight()), false);
                 BitmapDrawable newDrawable = new BitmapDrawable(this.getResources(), bitmapOrig);
                 newDrawable.setBounds(-newDrawable.getIntrinsicWidth() / 2, -newDrawable.getIntrinsicHeight() , 
                         newDrawable.getIntrinsicWidth() / 2, 0 );
                 overlayitem.setMarker(newDrawable);
             }
 
 			itemizedoverlay.addOverlay(overlayitem);
 		}
 	}
 
 	private Drawable getPinDrawable(int checkinsNum, GeoPoint gp) {
 		PinDrawable icon = new PinDrawable(this, checkinsNum);
         icon.setBounds(0, -icon.getIntrinsicHeight(), icon.getIntrinsicWidth(),
                 0);
 		return icon;
 	}
 
 	// We have user data from logged user, use it now...
 	/*public void useUserData() {
 		AppCAP.setLoggedInUserId(loggedUser.getUserId());
 		AppCAP.setLoggedInUserNickname(loggedUser.getNickName());
 		textNickName.setText(loggedUser.getNickName());
 	}*/
 	
 	
 
 	public void onClickMenu(View v) {
 		CustomFontView textInvite = (CustomFontView) findViewById(R.id.text_invite_codes);
         // Detect whether we already have an C&P invite or not
         if (AppCAP.getEnteredInviteCode()) {
             // We need to set to invite
 			textInvite.setText("Invite");
         } else {
             // We need to set to enter invite code
 			textInvite.setText("Enter invite code");
 
 		}
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
 
         Animation anim = AnimationUtils
                 .loadAnimation(this, R.anim.refresh_anim);
 		imageRefresh.setAnimation(anim);
 
 		hideBaloons();
 
         // exe.getVenuesAndUsersWithCheckinsInBoundsDuringInterval(getSWAndNECoordinatesBounds(mapView),
         // false);
         // AppCAP.getCounter().manualTrigger();
 
 		// For every refresh save Map coordinates
 		AppCAP.setUserCoordinates(getSWAndNECoordinatesBounds(mapView));
         MapView map = (MapView) findViewById(R.id.mapview);
         GeoPoint pointCenterMap = map.getMapCenter();
         int lngSpan = pointCenterMap.getLongitudeE6();
         int latSpan = pointCenterMap.getLatitudeE6();
         AppCAP.setMapCenterCoordinates(lngSpan, latSpan);
 
 		// Get Notification settings from shared prefs
         ((ToggleButton) findViewById(R.id.toggle_checked_in)).setChecked(AppCAP
                 .getNotificationToggle());
         ((Button) findViewById(R.id.btn_from)).setText(AppCAP
                 .getNotificationFrom());
 
 		// Check and Set Notification settings
         menu.setOnNotificationSettingsListener(
                 (ToggleButton) findViewById(R.id.toggle_checked_in),
                 (Button) findViewById(R.id.btn_from), true);
 
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
 
         GeoPoint sw = new GeoPoint(
                 pointCenterMap.getLatitudeE6() - latSpan / 2,
                 pointCenterMap.getLongitudeE6() - lngSpan / 2);
         GeoPoint ne = new GeoPoint(
                 pointCenterMap.getLatitudeE6() + latSpan / 2,
                 pointCenterMap.getLongitudeE6() + lngSpan / 2);
 
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
 
 	/*
 	@Override
 	public void onBackPressed() {
 		super.onBackPressed();
 	}*/
 
     
 
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
 
     @Override
     public void onClickSupport(View v) {
         menu.onClickSupport(v);
 	}
 	
 	@Override
 	public void onClickNotifications(View v) {
 		menu.onClickNotifications(v);
 		
 	}
 
 	@Override
 	public void onClickLogout(View v) {
 		menu.onClickLogout(v);
 	}
 
   
 
 
 	private void actionFinished(int action) {
 		result = exe.getResult();
 
 		switch (action) {
 		case Executor.HANDLE_GET_USER_DATA:
 			if (result.getObject() != null) {
 				if (result.getObject() instanceof UserSmart) {
 					loggedUser = (UserSmart) result.getObject();
 					
 					AppCAP.setLoggedInUserId(loggedUser.getUserId());
 					AppCAP.setLoggedInUserNickname(loggedUser.getNickName());
 					textNickName.setText(loggedUser.getNickName());
 				}
 			}
 		}
 
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	private class MyAutoCheckinTriggerObserver implements Observer {
 
 		@Override
 		public void update(Observable arg0, Object arg1) {
 
 			Message message = new Message();
 			Bundle bundle = new Bundle();
 			bundle.putCharSequence("type", "AutoCheckinTrigger");
 			
 			message.setData(bundle);
 			
 			Log.d(TAG,"Received Autocheckin Observable...");
 			mainThreadTaskHandler.sendMessage(message);
 			
 		}
 		
 	}
 	
 	
 	//====================================================================
 	// Cached Data Management
 	//====================================================================
 	
 	private class MyCachedDataObserver implements Observer {
 		
 		@Override
 		public void update(Observable observable, Object data) {
 
 			
 			if (data instanceof CachedDataContainer) {
 				CachedDataContainer counterdata = (CachedDataContainer) data;
 				DataHolder venuesWithCheckins = counterdata.getData();
 
 				Object[] obj = (Object[]) venuesWithCheckins.getObject();
 				@SuppressWarnings("unchecked")
 				List<VenueSmart> arrayVenues = (List<VenueSmart>) obj[0];
 				@SuppressWarnings("unchecked")
 				List<UserSmart> arrayUsers = (List<UserSmart>) obj[1];
 
 				Message message = new Message();
 				Bundle bundle = new Bundle();
 				bundle.putCharSequence("type", counterdata.type);
 				bundle.putParcelableArrayList("venues", new ArrayList<VenueSmart>(arrayVenues));
 				bundle.putParcelableArrayList("users", new ArrayList<UserSmart>(arrayUsers));
 				message.setData(bundle);
 
 				if (Constants.debugLog)
 					Log.d(TAG,"ActivityMap: Received cached data, processing...");
 
 				
 				mainThreadTaskHandler.sendMessage(message);
 				
 				
 			}
 		}
 	}
 	
 	private void updateVenuesAndCheckinsFromApiResult(ArrayList<VenueSmart> venueArray, ArrayList<UserSmart> arrayUsers) {
 		
 		if (Constants.debugLog)
 			Log.d(TAG,"updateVenuesAndCheckinsFromApiResult()");
 		itemizedoverlay.clear();
 
 		for (VenueSmart venue : venueArray) {
             GeoPoint gp = new GeoPoint((int) (venue.getLat() * 1E6),
                     (int) (venue.getLng() * 1E6));
 
 			if (venue.getCheckins() > 0) {
                 createMarker(gp, venue, venue.getCheckins(), venue.getName(),
                         true);
 			} else if (venue.getCheckinsForWeek() > 0) {
                 createMarker(gp, venue, venue.getCheckinsForWeek(),
                         venue.getName(), false); // !!!
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
 
    
 
 }
