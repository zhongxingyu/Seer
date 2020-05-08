 package com.socmaps.ui;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.PixelFormat;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Debug;
 import android.os.Handler;
 import android.os.Handler.Callback;
 import android.os.Message;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.facebook.android.AsyncFacebookRunner;
 import com.facebook.android.DialogError;
 import com.facebook.android.Facebook;
 import com.facebook.android.Facebook.DialogListener;
 import com.facebook.android.FacebookError;
 import com.google.android.gcm.GCMRegistrar;
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesUtil;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
 import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
 import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
 import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
 import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.CameraPosition;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.socmaps.entity.CirclesAndFriends;
 import com.socmaps.entity.Event;
 import com.socmaps.entity.GeoTag;
 import com.socmaps.entity.MeetupRequest;
 import com.socmaps.entity.People;
 import com.socmaps.entity.Place;
 import com.socmaps.entity.PushData;
 import com.socmaps.entity.SearchResult;
 import com.socmaps.entity.SecondDegreePeople;
 import com.socmaps.fb.BaseDialogListener;
 import com.socmaps.fb.FBUtility;
 import com.socmaps.fb.SessionEvents;
 import com.socmaps.fb.SessionEvents.AuthListener;
 import com.socmaps.fb.SessionEvents.LogoutListener;
 import com.socmaps.gps.GpsService;
 import com.socmaps.gps.ILocationUpdateIndicator;
 import com.socmaps.images.singly.ImageCacheListener;
 import com.socmaps.images.singly.ImageInfo;
 import com.socmaps.images.singly.RemoteImageCache;
 import com.socmaps.notificationBroadcast.BroadcastListener;
 import com.socmaps.notificationBroadcast.NotificationCountBroadcastReciever;
 import com.socmaps.pushNotification.CommonUtilities;
 import com.socmaps.pushNotification.ServerUtilities;
 import com.socmaps.service.LocationUpdateService;
 import com.socmaps.util.Constant;
 import com.socmaps.util.Constant.Permission;
 import com.socmaps.util.DialogsAndToasts;
 import com.socmaps.util.RestClient;
 import com.socmaps.util.ServerResponseParser;
 import com.socmaps.util.SharedPreferencesHelper;
 import com.socmaps.util.StaticValues;
 import com.socmaps.util.Utility;
 import com.socmaps.widget.CustomInfoWindowAdapter;
 import com.socmaps.widget.ListComparator;
 import com.socmaps.widget.MultiDirectionSlidingDrawer;
 import com.socmaps.widget.PermissionRadioGroupLess;
 import com.socmaps.widget.PermissionRadioGroupListener;
 
 /**
  * HomeActivity class responsible for displaying all users, places, geo tags on
  * map and some user interaction.
  * 
  */
 public class HomeActivity extends FragmentActivity implements ILocationUpdateIndicator, OnCheckedChangeListener, OnClickListener, OnMarkerClickListener, OnCameraChangeListener, OnMapClickListener,
 		OnMapLongClickListener, OnInfoWindowClickListener, BroadcastListener, PermissionRadioGroupListener, LogoutListener, AuthListener, DialogListener {
 
 	private NotificationCountBroadcastReciever broadcastReceiver;
 
 	private Button btnNotification;
 	private static Button topCloseButton, bottomCloseButton;
 	private static MultiDirectionSlidingDrawer topDrawer, bottomDrawer;
 
 	private TopDrawerListener topDrawerListener;
 	private BottomDrawerListener bottomDrawerListener;
 
 	private Context context;
 	private Handler handler;
 	private Runnable runnable;
 	private GpsService gpsService;
 	private LinearLayout btnDirections, btnMeetup, btnMyPlaces;
 	private double myLat, myLng;
 	private int requestCode;
 	private String responseString;
 	private int responseStatus = 0;
 
 	private ImageView avatar, placeIconImage;
 	private Dialog d, msgDialog, frndRequestDialog;
 
 	private ProgressDialog m_ProgressDialog;
 
 	private String responseStringUpdateLocation;
 	private int responseStatusUpdateLocation = 0;
 
 	private String friendRequestFriendId = "";
 	private String friendRequestMessage = "";
 	private String friendRequestResponse = "";
 	private int friendRequestStatus = 0;
 
 	private String sendMessageFriendId = "";
 	private String sendMessageSubject = "";
 	private String sendMessageContent = "";
 	private String sendMessageResponse = "";
 	private int sendMessageStatus = 0;
 	private AsyncTask<Void, Void, Void> mRegisterTask;
 
 	private CheckBox peopleCheckBox, placeCheckBox, eventCheckBox, dealCheckBox;
 	private Button btnListView, btnMapView;
 	private TextView tvListView, tvMapView;
 	private Button btnCircle;
 	private Button btnUpdateLocation, btnCircleMenu;
 
 	// MyGeoPoint point;
 	private boolean isFirstLocationUpdate = true;
 	private boolean isLocationFound = false;
 
 	private List<String> friendRequestSentList = new ArrayList<String>();
 	private Dialog currentBubbleDialog;
 
 	private PermissionRadioGroupLess permissionRadioGroupView;
 	private LinearLayout shareWithRadioGroupContainer;
 	private Permission locationSharingPermission;
 	private String shareWithPickerName = "sharewith";
 
 	private RelativeLayout circleMenu;
 	private LinearLayout btnCloseCircleMenu;
 
 	private Button btnCircleMenuItemPeople, btnCircleMenuItemProfile, btnCircleMenuItemMessages, btnCircleMenuItemFriends, btnCircleMenuItemDeals, btnCircleMenuItemPlaces, btnCircleMenuItemNewsfeed,
 			btnCircleMenuItemSettings;
 
 	private Button btnToggleSearchPanel, btnDoSearch, btnClearSearch;
 	private RelativeLayout searchPanel;
 	private EditText etSearchField;
 
 	private boolean isSearchEnabled = false;
 
 	private List<Object> listMasterContent;
 	private List<Object> listContent;
 
 	private int responseEventStatus = 0;
 	private String responseEventString;
 
 	private int responseGeotagStatus = 0;
 	private String responseGeotagString;
 
 	private List<Event> eventList;
 	private List<GeoTag> geotagList;
 
 	// ImageDownloader imageDownloader;
 	private Dialog isFirstTimeFbdialog;
 	private Button btnFBLogin;
 
 	private SessionEvents sessionEvents;
 
 	private TimerTask task;
 	private Timer timer;
 	private boolean isRunning = true;
 	private long updateInterval = 60000; // 1 min
 
 	private GoogleMap mapView;
 	private Timer mapUpdateTimer;
 	private float intialZoomLevel = 16.0f;
 	private int mapUpdateInterval = 2000; // milliseconds
 
 	// private Marker lastClickedMarker;
 	private HashMap<String, Object> visibleItemsOnMap = new HashMap<String, Object>();
 	private HashMap<String, Marker> visibleMarkers = new HashMap<String, Marker>();
 	private HashMap<String, Boolean> markerUpdateList = new HashMap<String, Boolean>();
 	private HashMap<String, Object> objectList = new HashMap<String, Object>();
 
 	private CustomInfoWindowAdapter customInfoWindowAdapter;
 
 	private boolean isMoveToAnotherPoint = false;
 	private LinearLayout markerLayout;
 
 	private boolean isNewSearch = false;
 
 	// private ImageFetcher imageFetcher;
 
 	private ListComparator listComparator;
 
 	public RemoteImageCache remoteImageCache;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.home_layout);
 
 		initialize();
 
 		setCheckBoxSelection();
 		setOnCheckChangeListener();
 
 		addPermissionRadioGroup();
 
 		try {
 			registerPushNotification();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (Exception e) {
 			// TODO: handle exception
 		}
 
 		// facebookAuthentication();
 
 		handleNavigationFromNotificationBar(getIntent());
 		initializeNotificationBroadcast();
 
 		centerToMyPosition();
 
 	}
 
 	private void initialize() {
 
 		context = HomeActivity.this;
 		gpsService = new GpsService(context, this);
 		// mapView = (TapControlledMapView) findViewById(R.id.myGMap);
 		// imageFetcher = new ImageFetcher(context,
 		// getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size),
 		// true);
 
 		remoteImageCache = new RemoteImageCache(context, 20, null, 50);
 
 		listComparator = new ListComparator();
 
 		FragmentManager myFragmentManager = getSupportFragmentManager();
 		SupportMapFragment myMapFragment = (SupportMapFragment) myFragmentManager.findFragmentById(R.id.map);
 		mapView = myMapFragment.getMap();
 		mapView.setMyLocationEnabled(true);
 		// mapView.setMapType(GoogleMap.MAP_TYPE_HYBRID);
 		mapView.setMapType(GoogleMap.MAP_TYPE_NORMAL);
 		// mapView.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
 		// mapView.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
 		mapView.setOnMapClickListener(this);
 		mapView.setOnMapLongClickListener(this);
 		mapView.setOnCameraChangeListener(this);
 		mapView.setOnMarkerClickListener(this);
 		mapView.setIndoorEnabled(true);
 		mapView.setOnInfoWindowClickListener(this);
 		mapView.getUiSettings().setZoomControlsEnabled(false);
 
 		markerLayout = (LinearLayout) HomeActivity.this.getLayoutInflater().inflate(R.layout.marker, null);
 
 		topDrawerListener = new TopDrawerListener();
 		bottomDrawerListener = new BottomDrawerListener();
 
 		topDrawer = (MultiDirectionSlidingDrawer) findViewById(R.id.topDrawer);
 		topDrawer.setOnDrawerOpenListener(topDrawerListener);
 		topDrawer.setOnDrawerCloseListener(topDrawerListener);
 
 		bottomDrawer = (MultiDirectionSlidingDrawer) findViewById(R.id.bottomDrawer);
 		bottomDrawer.setOnDrawerOpenListener(bottomDrawerListener);
 		bottomDrawer.setOnDrawerCloseListener(bottomDrawerListener);
 
 		View vTop = topDrawer.findViewById(R.id.topSliderContent);
 		peopleCheckBox = (CheckBox) vTop.findViewById(R.id.chkMenuItemPeople);
 		placeCheckBox = (CheckBox) vTop.findViewById(R.id.chkMenuItemPlaces);
 		eventCheckBox = (CheckBox) vTop.findViewById(R.id.chkMenuItemEvents);
 		dealCheckBox = (CheckBox) vTop.findViewById(R.id.chkMenuItemDeals);
 
 		btnDirections = (LinearLayout) vTop.findViewById(R.id.btnDirections);
 		btnDirections.setOnClickListener(this);
 
 		btnMyPlaces = (LinearLayout) vTop.findViewById(R.id.btnMyPlaces);
 		btnMyPlaces.setOnClickListener(this);
 
 		btnMeetup = (LinearLayout) vTop.findViewById(R.id.btnMeetup);
 		btnMeetup.setOnClickListener(this);
 
 		shareWithRadioGroupContainer = (LinearLayout) vTop.findViewById(R.id.shareWithRadioGroupContainer);
 
 		View vBottom = bottomDrawer.findViewById(R.id.bottomContent);
 
 		btnMapView = (Button) vBottom.findViewById(R.id.btnMapView);
 		btnMapView.setBackgroundDrawable(getResources().getDrawable(R.drawable.icon_map_view_selected));
 
 		btnListView = (Button) vBottom.findViewById(R.id.btnListView);
 		btnListView.setOnClickListener(this);
 
 		tvListView = (TextView) vBottom.findViewById(R.id.tvListView);
 		tvListView.setOnClickListener(this);
 
 		btnCircle = (Button) vBottom.findViewById(R.id.btnCircle);
 		btnCircle.setOnClickListener(this);
 
 		topCloseButton = (Button) topDrawer.findViewById(R.id.topHandle);
 		bottomCloseButton = (Button) bottomDrawer.findViewById(R.id.bottomHandle);
 
 		btnNotification = (Button) findViewById(R.id.btnNotification);
 		btnNotification.setOnClickListener(this);
 
 		// btnUpdateLocation = (Button) findViewById(R.id.btnUpdateLocation);
 		// btnUpdateLocation.setOnClickListener(this);
 
 		btnCircleMenu = (Button) findViewById(R.id.btnCircleMenu);
 		btnCircleMenu.setOnClickListener(this);
 
 		circleMenu = (RelativeLayout) findViewById(R.id.circleMenu);
 		circleMenu.setOnClickListener(this);
 
 		btnCloseCircleMenu = (LinearLayout) circleMenu.findViewById(R.id.btnCloseCircleMenu);
 		btnCloseCircleMenu.setOnClickListener(this);
 
 		btnCircleMenuItemPeople = (Button) findViewById(R.id.btnCircleMenuItemPeople);
 		btnCircleMenuItemPeople.setOnClickListener(this);
 		btnCircleMenuItemProfile = (Button) findViewById(R.id.btnCircleMenuItemProfile);
 		btnCircleMenuItemProfile.setOnClickListener(this);
 		btnCircleMenuItemMessages = (Button) findViewById(R.id.btnCircleMenuItemMessages);
 		btnCircleMenuItemMessages.setOnClickListener(this);
 		btnCircleMenuItemFriends = (Button) findViewById(R.id.btnCircleMenuItemFriends);
 		btnCircleMenuItemFriends.setOnClickListener(this);
 		btnCircleMenuItemDeals = (Button) findViewById(R.id.btnCircleMenuItemDeals);
 		btnCircleMenuItemDeals.setOnClickListener(this);
 		btnCircleMenuItemPlaces = (Button) findViewById(R.id.btnCircleMenuItemPlaces);
 		btnCircleMenuItemPlaces.setOnClickListener(this);
 		btnCircleMenuItemNewsfeed = (Button) findViewById(R.id.btnCircleMenuItemNewsfeed);
 		btnCircleMenuItemNewsfeed.setOnClickListener(this);
 		btnCircleMenuItemSettings = (Button) findViewById(R.id.btnCircleMenuItemSettings);
 		btnCircleMenuItemSettings.setOnClickListener(this);
 
 		btnToggleSearchPanel = (Button) findViewById(R.id.btnSearch);
 		btnToggleSearchPanel.setOnClickListener(this);
 
 		searchPanel = (RelativeLayout) findViewById(R.id.searchPanel);
 		etSearchField = (EditText) findViewById(R.id.etSearchField);
 
 		btnDoSearch = (Button) findViewById(R.id.btnDoSearch);
 		btnDoSearch.setOnClickListener(this);
 
 		btnClearSearch = (Button) findViewById(R.id.btnClearSearch);
 		btnClearSearch.setOnClickListener(this);
 
 		listMasterContent = new ArrayList<Object>();
 		listContent = new ArrayList<Object>();
 
 		sessionEvents = new SessionEvents();
 
 	}
 
 	private void centerToMyPosition() {
 		TimerTask myLocUpdateTask = new TimerTask() {
 			public void run() {
 
 				runOnUiThread(displayMyPosition);
 
 			}
 		};
 		Timer myLocUpdateTimer = new Timer();
 		myLocUpdateTimer.schedule(myLocUpdateTask, 1000);
 
 	}
 
 	private Runnable displayMyPosition = new Runnable() {
 
 		@Override
 		public void run() {
 			// TODO Auto-generated method stub
 			if (mapView.getMyLocation() != null) {
 
 				Log.i("HomeActivity", "CenterToMyPosition");
 
 				if (!isMoveToAnotherPoint) {
 					mapView.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mapView.getMyLocation().getLatitude(), mapView.getMyLocation().getLongitude()), intialZoomLevel));
 
 					if (timer == null) {
 						Location location = new Location(LocationManager.NETWORK_PROVIDER);
 						location.setLatitude(mapView.getMyLocation().getLatitude());
 						location.setLongitude(mapView.getMyLocation().getLongitude());
 						catchLocationUpdate(location);
 					}
 				}
 
 				isMoveToAnotherPoint = false;
 
 			} else {
 				centerToMyPosition();
 				// Toast.makeText(context, "No location found yet.",
 				// Toast.LENGTH_SHORT).show();
 			}
 		}
 	};
 
 	private void getMapDataFromServer() {
 		getSearchResult();
 		getEventList();
 		getGeotagList();
 	}
 
 	private void initTimeTask() {
 		task = new TimerTask() {
 			public void run() {
 				if (isLocationFound && isRunning) {
 					getMapDataFromServer();
 				}
 			}
 		};
 		if (timer != null) {
 			timer.cancel();
 		}
 		timer = new Timer();
 		timer.schedule(task, 500, updateInterval);
 	}
 
 	private void checkNotNull(Object reference, String name) {
 		if (reference == null) {
 			throw new NullPointerException(getString(R.string.error_config, name));
 		}
 	}
 
 	private void registerPushNotification() throws IOException {
 		// TODO Auto-generated method stub
 		checkNotNull(CommonUtilities.SERVER_URL, "SERVER_URL");
 		checkNotNull(CommonUtilities.SENDER_ID, "SENDER_ID");
 		// Make sure the device has the proper dependencies.
 		GCMRegistrar.checkDevice(this);
 		// Make sure the manifest was properly set - comment out this line
 		// while developing the app, then uncomment it when it's ready.
 		GCMRegistrar.checkManifest(this);
 
 		final String regId = GCMRegistrar.getRegistrationId(this);
 
 		// boolean forceRegister = true;
 
 		Log.i("GCM RegID", "inside registerPushNotification");
 
 		if (regId == null || regId.equals("")) {
 			Log.i("GCM RegID", "regId is null");
 			// Automatically registers application on startup.
 			GCMRegistrar.register(getApplicationContext(), CommonUtilities.SENDER_ID);
 		} else {
 			registerDeviceOnServer(regId);
 		}
 
 	}
 
 	private void registerDeviceOnServer(final String regId) {
 		// TODO Auto-generated method stub
 
 		Log.i("GCM RegId", regId);
 		final Context context = this;
 		mRegisterTask = new AsyncTask<Void, Void, Void>() {
 
 			@Override
 			protected Void doInBackground(Void... params) {
 				boolean registered = ServerUtilities.register(context, regId);
 				// At this point all attempts to register with the app
 				// server failed, so we need to unregister the device
 				// from GCM - the app will try to register again when
 				// it is restarted. Note that GCM will send an
 				// unregistered callback upon completion, but
 				// GCMIntentService.onUnregistered() will ignore it.
 				if (!registered) {
 					GCMRegistrar.unregister(context);
 				}
 				return null;
 			}
 
 			@Override
 			protected void onPostExecute(Void result) {
 				mRegisterTask = null;
 			}
 
 		};
 		mRegisterTask.execute(null, null, null);
 	}
 
 	private void startGpsService() {
 		gpsService.StartListener();
 
 		handler = new Handler(new Callback() {
 			@Override
 			public boolean handleMessage(Message msg) {
 				handler.removeCallbacks(runnable);
 				return false;
 			}
 		});
 		runnable = new Runnable() {
 			Message msg;
 			Location location = null;
 
 			@Override
 			public void run() {
 				location = gpsService.getLastLocation();
 				if (location != null) {
 					catchLocationUpdate(location);
 					msg = new Message();
 					msg.what = 1;
 					handler.sendMessage(msg);
 					return;
 				}
 				handler.postDelayed(this, 2000);
 			}
 		};
 		handler.post(runnable);
 	}
 
 	@Override
 	protected void onDestroy() {
 		if (mRegisterTask != null) {
 			mRegisterTask.cancel(true);
 		}
 
 		try {
 			unregisterReceiver(broadcastReceiver);
 		} catch (IllegalArgumentException e) {
 
 		} catch (Exception e) {
 			// TODO: handle exception
 		}
 
 		GCMRegistrar.onDestroy(getApplicationContext());
 
 		SessionEvents.removeAuthListener(this);
 		SessionEvents.removeLogoutListener(this);
 
 		// imageFetcher.closeCache();
 		remoteImageCache.shutdown();
 
 		System.gc();
 
 		super.onDestroy();
 	}
 
 	@Override
 	public synchronized void catchLocationUpdate(Location location) {
 
 		isLocationFound = true;
 		// point =
 		// ServerResponseParser.getGeoPointByLatLong(location.getLatitude(),
 		// location.getLongitude());
 		StaticValues.myPoint = new LatLng(location.getLatitude(), location.getLongitude());
 		myLat = location.getLatitude();
 		myLng = location.getLongitude();
 
 		Log.e("location.getLatitude():" + location.getLatitude(), "location.getLongitude():" + location.getLongitude());
 
 		// Log.e("Size mapoverlay self", mapOverlays.size() + "");
 
 		// itemizedOverlaySelf = (CustomItemizedOverlay) mapOverlays.get(0);
 
 		// itemizedOverlaySelf.removeAllItem();
 		// CustomOverlayItem overlayItem = new CustomOverlayItem(point,
 		// "self","", StaticValues.myInfo);
 		// itemizedOverlaySelf.addOverlay(overlayItem);
 
 		if (isFirstLocationUpdate) {
 			// relocationCurrentPosition();
 			isFirstLocationUpdate = false;
 
 			initTimeTask();
 		}
 		// mapView.invalidate();
 
 		sendSelfLocationToServer();
 	}
 
 	private void getSearchResult() {
 		Thread userFetchThread = new Thread(null, getSearchResultThread, "Start geting searchResult");
 		userFetchThread.start();
 	}
 
 	private Runnable getSearchResultThread = new Runnable() {
 
 		@Override
 		public void run() {
 			// TODO Auto-generated method stub
 			sendServerRequestToGetSearchResult();
 		}
 	};
 
 	private void sendServerRequestToGetSearchResult() {
 		RestClient restClient = new RestClient(Constant.smGetUserUrl);
 		restClient.AddHeader(Constant.authTokenParam, Utility.getAuthToken(context));
 		restClient.AddParam("lat", myLat + "");
 		restClient.AddParam("lng", myLng + "");
 		try {
 			restClient.Execute(RestClient.RequestMethod.POST);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		responseString = restClient.getResponse();
 
 		responseStatus = restClient.getResponseCode();
 
 		runOnUiThread(returnResGetSearchResult);
 	}
 
 	private Runnable returnResGetSearchResult = new Runnable() {
 
 		@Override
 		public void run() {
 			// TODO Auto-generated method stub
 			handleGetSearchResultResponse(responseStatus, responseString);
 		}
 	};
 
 	private void handleGetSearchResultResponse(int status, String response) {
 		Log.d("Get Search result", status + ":" + response);
 		switch (status) {
 		case Constant.STATUS_SUCCESS:
 
 			SearchResult tempSearchResult = ServerResponseParser.parseSeachResult(response);
 
 			if (tempSearchResult != null) {
 				if (tempSearchResult.getPeoples() != null || tempSearchResult.getPlaces() != null || tempSearchResult.getSecondDegreePeoples() != null) {
 
 					StaticValues.searchResult = tempSearchResult;
 
 					populateMasterList();
 
 					if (isSearchEnabled == false) {
 
 						Log.e("UpdateMap", "Inside it");
 						updateContentList(listMasterContent);
 						updateMapDisplay(listContent);
 					}
 				}
 			}
 
 			break;
 
 		case Constant.STATUS_BADREQUEST:
 			Toast.makeText(getApplicationContext(), Utility.getJSONStringFromServerResponse(response), Toast.LENGTH_LONG).show();
 
 			break;
 
 		case Constant.STATUS_NOTFOUND:
 			Toast.makeText(getApplicationContext(), Utility.getJSONStringFromServerResponse(response), Toast.LENGTH_LONG).show();
 
 			break;
 		default:
 			break;
 
 		}
 	}
 
 	private void getGeotagList() {
 		Thread userFetchThread = new Thread(null, getGeotagListThread, "Start geting geotag list");
 		userFetchThread.start();
 	}
 
 	private Runnable getGeotagListThread = new Runnable() {
 
 		@Override
 		public void run() {
 			// TODO Auto-generated method stub
 			sendServerRequestToGetGeotagList();
 		}
 	};
 
 	private void sendServerRequestToGetGeotagList() {
 		RestClient restClient = new RestClient(Constant.smGeoTag);
 		restClient.AddHeader(Constant.authTokenParam, Utility.getAuthToken(context));
 
 		try {
 			restClient.Execute(RestClient.RequestMethod.GET);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		responseGeotagString = restClient.getResponse();
 
 		responseGeotagStatus = restClient.getResponseCode();
 
 		runOnUiThread(returnResGetGeotagList);
 	}
 
 	private Runnable returnResGetGeotagList = new Runnable() {
 
 		@Override
 		public void run() {
 			// TODO Auto-generated method stub
 			handleGetGeotagListResponse(responseGeotagStatus, responseGeotagString);
 			responseGeotagString = null;
 		}
 	};
 
 	private void handleGetGeotagListResponse(int status, String response) {
 		switch (status) {
 		case Constant.STATUS_SUCCESS:
 
 			geotagList = ServerResponseParser.parseGeotags(response);
 
 			populateMasterList();
 
 			if (isSearchEnabled == false) {
 
 				updateContentList(listMasterContent);
 				// updateMapDisplay(listContent);
 			}
 
 			break;
 
 		case Constant.STATUS_BADREQUEST:
 			Toast.makeText(getApplicationContext(), Utility.getJSONStringFromServerResponse(response), Toast.LENGTH_LONG).show();
 
 			break;
 
 		case Constant.STATUS_NOTFOUND:
 			Toast.makeText(getApplicationContext(), Utility.getJSONStringFromServerResponse(response), Toast.LENGTH_LONG).show();
 
 			break;
 		default:
 			break;
 
 		}
 	}
 
 	private void getEventList() {
 		Thread userFetchThread = new Thread(null, getEventListThread, "Start geting event list");
 		userFetchThread.start();
 	}
 
 	private Runnable getEventListThread = new Runnable() {
 
 		@Override
 		public void run() {
 			// TODO Auto-generated method stub
 			sendServerRequestToGetEventList();
 		}
 	};
 
 	private void sendServerRequestToGetEventList() {
 		RestClient getUserClient = new RestClient(Constant.smEventUrl);
 		getUserClient.AddHeader(Constant.authTokenParam, Utility.getAuthToken(context));
 
 		try {
 			getUserClient.Execute(RestClient.RequestMethod.GET);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		responseEventString = getUserClient.getResponse();
 
 		responseEventStatus = getUserClient.getResponseCode();
 
 		runOnUiThread(returnResGetEventList);
 	}
 
 	private Runnable returnResGetEventList = new Runnable() {
 
 		@Override
 		public void run() {
 			// TODO Auto-generated method stub
 			handleGetEventListResponse(responseEventStatus, responseEventString);
 		}
 	};
 
 	private void handleGetEventListResponse(int status, String response) {
 
 		switch (status) {
 		case Constant.STATUS_SUCCESS:
 			eventList = ServerResponseParser.parseGetEventListResult(response);
 
 			populateMasterList();
 
 			if (isSearchEnabled == false) {
 
 				Log.e("UpdateEventMap", "Inside it");
 				updateContentList(listMasterContent);
 				// updateMapDisplay(listContent);
 			}
 
 			break;
 
 		case Constant.STATUS_BADREQUEST:
 			Toast.makeText(getApplicationContext(), Utility.getJSONStringFromServerResponse(response), Toast.LENGTH_LONG).show();
 
 			break;
 
 		case Constant.STATUS_NOTFOUND:
 			Toast.makeText(getApplicationContext(), Utility.getJSONStringFromServerResponse(response), Toast.LENGTH_LONG).show();
 
 			break;
 		default:
 			break;
 
 		}
 	}
 
 	private synchronized void populateMasterList() {
 
 		if (StaticValues.searchResult != null) {
 
 			listMasterContent.clear();
 
 			addPeoplesToMasterList();
 			addPlacesToMasterList();
 			addFacebbokPeoplesToMasterList();
 
 			if (eventList != null) {
 				addEventsToMasterList();
 			}
 
 			if (geotagList != null) {
 				addGeotagsToMasterList();
 			}
 		}
 
 		sortMasterListData();
 
 		validateExistingMarkers();
 	}
 
 	private void addPlacesToMasterList() {
 		if (StaticValues.searchResult.getPlaces() != null) {
 			for (int i = 0; i < StaticValues.searchResult.getPlaces().size(); i++) {
 				listMasterContent.add(StaticValues.searchResult.getPlaces().get(i));
 			}
 		}
 
 	}
 
 	private void addPeoplesToMasterList() {
 		if (StaticValues.searchResult.getPeoples() != null) {
 			for (int i = 0; i < StaticValues.searchResult.getPeoples().size(); i++) {
 				listMasterContent.add(StaticValues.searchResult.getPeoples().get(i));
 			}
 		}
 
 	}
 
 	private void addFacebbokPeoplesToMasterList() {
 		if (StaticValues.searchResult.getSecondDegreePeoples() != null) {
 			for (int i = 0; i < StaticValues.searchResult.getSecondDegreePeoples().size(); i++) {
 				listMasterContent.add(StaticValues.searchResult.getSecondDegreePeoples().get(i));
 			}
 		}
 
 	}
 
 	private void addEventsToMasterList() {
 		for (int i = 0; i < eventList.size(); i++) {
 			listMasterContent.add(eventList.get(i));
 		}
 	}
 
 	private void addGeotagsToMasterList() {
 		for (int i = 0; i < geotagList.size(); i++) {
 			listMasterContent.add(geotagList.get(i));
 		}
 	}
 
 	private void updateContentList(List<Object> list) {
 		listContent.clear();
 		listContent.addAll(list);
 
 	}
 
 	private void sortMasterListData() {
 		listComparator.setLatLng(null);
 		Collections.sort(this.listMasterContent, listComparator);
 	}
 
 	private MarkerOptions getMarkerOptions(Object item) {
 		MarkerOptions markerOptions = new MarkerOptions();
 		String title = Utility.getItemTitle(item);
 		markerOptions.title(title);
 		markerOptions.icon(null);
 
 		try {
 
 			Bitmap bitmap = Utility.generateMarker(markerLayout, item, remoteImageCache, imageCacheListener);
 			markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
 			// markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon));
 			bitmap.recycle();
 			bitmap = null;
 
 		} catch (OutOfMemoryError e) {
 			markerOptions.icon(null);
 			// TODO: handle exception
 		} catch (Exception e) {
 			markerOptions.icon(null);
 			// TODO: handle exception
 		}
 
 		LatLng latLng = Utility.getLatLngFromObject(item);
 		if (latLng != null) {
 			markerOptions.position(latLng);
 
 			return markerOptions;
 		}
 
 		return null;
 	}
 
 	private MarkerOptions getMarkerOptions(Object item, Drawable profilePic) {
 		MarkerOptions markerOptions = new MarkerOptions();
 		String title = Utility.getItemTitle(item);
 		markerOptions.title(title);
 		markerOptions.icon(null);
 		try {
 
 			Bitmap bitmap = Utility.generateMarker(markerLayout, item, profilePic);
 			markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
 			// markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon));
 			bitmap.recycle();
 			bitmap = null;
 		} catch (OutOfMemoryError e) {
 			markerOptions.icon(null);
 			// TODO: handle exception
 		} catch (Exception e) {
 			markerOptions.icon(null);
 			// TODO: handle exception
 		}
 
 		LatLng latLng = Utility.getLatLngFromObject(item);
 		if (latLng != null) {
 			markerOptions.position(latLng);
 
 			return markerOptions;
 		}
 
 		return null;
 	}
 
 	private synchronized void updateMapDisplay(final List<Object> list) {
 
 		Log.i("HomeActivity", "updateMapDisplay");
 		if (list != null) {
 
 			int totalItemDisplayed = 0;
 			String firstItemIdOnList = null;
 
 			if (!isSearchEnabled || !isNewSearch) {
 				listComparator.setLatLng(mapView.getCameraPosition().target);
 				Collections.sort(list, listComparator);
 			}
 
 			boolean isPeople = SharedPreferencesHelper.getInstance(context).getBoolean(Constant.PEOPLE, true);
 
 			boolean isPlace = SharedPreferencesHelper.getInstance(context).getBoolean(Constant.PLACE, false);
 
 			boolean isEvent = SharedPreferencesHelper.getInstance(context).getBoolean(Constant.EVENT, true);
 
 			for (int i = 0; i < list.size(); i++) {
 
 				Object item = list.get(i);
 				String itemId = Utility.getItemId(item);
 
 				LatLng latLng = Utility.getLatLngFromObject(item);
 
 				// boolean isSelectedMarker = false;
 
 				if (firstItemIdOnList == null && visibleMarkers.containsKey(itemId)) {
 					firstItemIdOnList = itemId;
 				}
 
 				if ((Utility.isLocationVisibleOnMap(mapView, latLng) || isSearchEnabled) && !visibleMarkers.containsKey(itemId)) {
 
 					MarkerOptions markerOptions = null;
 
 					if (item instanceof People && isPeople) {
 
 						markerOptions = getMarkerOptions(item);
 
 					} else if (item instanceof Place && isPlace) {
 
 						markerOptions = getMarkerOptions(item);
 
 						// displayedItemCounter++;
 					} else if (item instanceof SecondDegreePeople && isPeople) {
 
 						markerOptions = getMarkerOptions(item);
 
 					} else if (item instanceof Event && isEvent) {
 						markerOptions = getMarkerOptions(item);
 
 					} else if (item instanceof GeoTag && isPeople) {
 						markerOptions = getMarkerOptions(item);
 					}
 
 					if (markerOptions != null) {
 
 						try {
 							Marker marker = mapView.addMarker(markerOptions);
 							visibleItemsOnMap.put(marker.getId(), item);
 							visibleMarkers.put(itemId, marker);
 							objectList.put(itemId, item);
 
 							markerUpdateList.put(Utility.getItemId(item), false);
 
 							totalItemDisplayed++;
 
 							if (firstItemIdOnList == null) {
 								firstItemIdOnList = itemId;
 							}
 
 						} catch (NullPointerException e) {
 
 						} catch (Exception e) {
 							// TODO: handle exception
 						}
 
 					}
 
 				}
 
 				if (totalItemDisplayed == 10) {
 					break;
 				}
 			}
 
 			// list.clear();
 
 			// customInfoWindowAdapter = new
 			// CustomInfoWindowAdapter(context,getLayoutInflater(),
 			// visibleItemsOnMap, imageFetcher,HomeActivity.this);
 			customInfoWindowAdapter = new CustomInfoWindowAdapter(context, getLayoutInflater(), visibleItemsOnMap, remoteImageCache, imageCacheListener);
 
 			mapView.setInfoWindowAdapter(customInfoWindowAdapter);
 
 			Marker markerToHighlight = null;
 
 			if (isSearchEnabled && isNewSearch) {
 
 				if (visibleMarkers.size() > 0) {
 					markerToHighlight = visibleMarkers.get(firstItemIdOnList);
 
 				}
 				if (markerToHighlight == null) {
 					Toast.makeText(context, "No search result found.", Toast.LENGTH_SHORT).show();
 				}
 				isNewSearch = false;
 			} else if (StaticValues.isHighlightAnnotation && StaticValues.highlightAnnotationItem != null) {
 
 				if (visibleMarkers.size() > 0) {
 					markerToHighlight = visibleMarkers.get(Utility.getItemId(StaticValues.highlightAnnotationItem));
 				}
 
 				StaticValues.isHighlightAnnotation = false;
 				StaticValues.highlightAnnotationItem = null;
 
 			}
 
 			if (markerToHighlight != null) {
 				mapView.animateCamera(CameraUpdateFactory.newLatLng(markerToHighlight.getPosition()));
 				markerToHighlight.showInfoWindow();
 			}
 
 			/*
 			 * if (!isTimerRunning) { markerUpdateTimer = new
 			 * Timer("MarkerUpdate", true); markerUpdateTimer .schedule(new
 			 * MarkerUpdateTask(), 10000, 15000); isTimerRunning = true; }
 			 */
 
 			updateMarkerVisibility();
 
 			System.gc();
 		}
 
 	}
 
 	private void setOnCheckChangeListener() {
 		// TODO Auto-generated method stub
 		peopleCheckBox.setOnCheckedChangeListener(this);
 		placeCheckBox.setOnCheckedChangeListener(this);
 		eventCheckBox.setOnCheckedChangeListener(this);
 		dealCheckBox.setOnCheckedChangeListener(this);
 	}
 
 	private void addPermissionRadioGroup() {
 		// TODO Auto-generated method stub
 
 		locationSharingPermission = Permission.PUBLIC;
 		try {
 			locationSharingPermission = Permission.values()[StaticValues.myInfo.getSettings().getShareLocation()];
 		} catch (NullPointerException e) {
 
 		} catch (Exception e) {
 			// TODO: handle exception
 		}
 
 		if (locationSharingPermission.ordinal() < 1) {
 			locationSharingPermission = Permission.PUBLIC;
 		}
 
 		permissionRadioGroupView = new PermissionRadioGroupLess(context, this, locationSharingPermission);
 		shareWithRadioGroupContainer.addView(permissionRadioGroupView);
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	public void onContentChanged() {
 		super.onContentChanged();
 	}
 
 	/** Stop the updates when Activity is paused */
 	@Override
 	protected void onPause() {
 		super.onPause();
 
 		// imageFetcher.setExitTasksEarly(true);
 		// imageFetcher.flushCache();
 
 		isRunning = false;
 		Log.i("Home:onPause memory before", "" + Debug.getNativeHeapAllocatedSize());
 
 		gpsService.stopListener();
 
 		StaticValues.highlightAnnotationItem = null;
 		StaticValues.isHighlightAnnotation = false;
 
 		// listMasterContent.clear();
 		// listContent.clear();
 		// listDisplayableContent.clear();
 
 		System.gc();
 
 		Log.i("Home:onPause memory after", "" + Debug.getNativeHeapAllocatedSize());
 
 		/*
 		 * if (markerUpdateTimer != null) { markerUpdateTimer.cancel(); }
 		 * isTimerRunning = false;
 		 */
 
 		// Start background service
 		startService(new Intent(HomeActivity.this, LocationUpdateService.class));
 
 	}
 
 	private void handleNavigationFromNotificationBar(Intent intent) {
 
 		Bundle extras = intent.getExtras();
 		Log.i("dbg", "onNewIntent");
 
 		if (StaticValues.myInfo == null) {
 			if (Utility.getUserData(context) != null) {
 				StaticValues.myInfo = ServerResponseParser.parseUserProfileInfo(Utility.getUserData(context), false);
 			}
 
 			if (StaticValues.myInfo == null) {
 				// Toast.makeText(context, "An unknown error occurred.",
 				// Toast.LENGTH_SHORT).show();
 				extras = null;
 				finish();
 				startActivity(new Intent(context, LoginActivity.class));
 			}
 
 		}
 
 		if (extras != null) {
 			if (extras.containsKey("pushData")) {
 				Log.i("dbg", "containsKey(PushData)");
 				PushData pushData = (PushData) extras.get("pushData");
 				if (pushData != null) {
 
 					if (pushData.getReceiverId().equals(StaticValues.myInfo.getId())) {
 						Log.i("Home:PushData:Type", pushData.getObjectType());
 						if (pushData.getObjectType().equals(Constant.PUSH_NOTIFICATION_MESSAGE_NEW)) {
 							Intent i = new Intent(context, MessageConversationFromNotificationActivity.class);
 							i.putExtra("itemThreadId", pushData.getObjectId());
 							i.putExtra("itemMessageId", pushData.getObjectId());
 
 							startActivity(i);
 
 						} else if (pushData.getObjectType().equals(Constant.PUSH_NOTIFICATION_MESSAGE_REPLY)) {
 
 							Intent i = new Intent(context, MessageConversationFromNotificationActivity.class);
 							i.putExtra("itemThreadId", pushData.getObjectId());
 							i.putExtra("itemMessageId", pushData.getObjectId());
 							i.putExtra("status", true);
 							startActivity(i);
 						} else if (pushData.getObjectType().equals(Constant.PUSH_NOTIFICATION_EVENT)) {
 							Intent intent2 = new Intent(context, EventListActivity.class);
 							startActivity(intent2);
 						} else if (pushData.getObjectType().equals(Constant.PUSH_NOTIFICATION_FRIEND_REQUEST)) {
 							Intent intent2 = new Intent(context, NotificationActivity.class);
 							intent2.putExtra("selectedTab", Constant.PUSH_NOTIFICATION_FRIEND_REQUEST);
 							startActivity(intent2);
 						} else if (pushData.getObjectType().equals(Constant.PUSH_NOTIFICATION_FRIEND_REQUEST_ACCEPT)) {
 
 							updateFriendList();
 							Intent intent2 = new Intent(context, ProfileActivity2.class);
 							intent2.putExtra("peopleId", pushData.getObjectId());
 							startActivity(intent2);
 
 						} else if (pushData.getObjectType().equals(Constant.PUSH_NOTIFICATION_MEETUP)) {
 							Intent intent2 = new Intent(context, MessageActivity.class);
 							intent2.putExtra("selectedTab", Constant.PUSH_NOTIFICATION_MEETUP);
 							startActivity(intent2);
 						} else if (pushData.getObjectType().equals(Constant.PUSH_NOTIFICATION_PROXIMITY_ALERT)) {
 							if (StaticValues.searchResult != null) {
 								if (isSearchEnabled) {
 									disableSearch();
 									doSearch();
 								}
 
 								People people = Utility.getPeopleById(pushData.getObjectId(), StaticValues.searchResult.getPeoples());
 								if (people != null) {
 									StaticValues.isHighlightAnnotation = true;
 									StaticValues.highlightAnnotationItem = people;
 									// highlightAnnotation();
 
 									LatLng selectedLatLng = Utility.getLatLngFromObject(StaticValues.highlightAnnotationItem);
 									mapView.animateCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, intialZoomLevel));
 
 								}
 							}
 
 						}
 					}
 				} else {
 					Log.i("Home:PushData:Type", "PushData is null");
 				}
 			}
 
 		}
 	}
 
 	private void updateFriendList() {
 
 		Thread thread = new Thread(friendListUpdateRunnable);
 
 		thread.start();
 	}
 
 	Runnable friendListUpdateRunnable = new Runnable() {
 
 		@Override
 		public void run() {
 			// TODO Auto-generated method stub
 			RestClient restClient = new RestClient(Constant.smFriendList);
 			restClient.AddHeader(Constant.authTokenParam, Utility.getAuthToken(context));
 
 			try {
 				restClient.Execute(RestClient.RequestMethod.GET);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 
 			if (restClient.getResponseCode() == Constant.STATUS_SUCCESS) {
 
 				if (restClient.getResponse() != null && !restClient.getResponse().equals("")) {
 					CirclesAndFriends circlesAndFriends = ServerResponseParser.parseCircleAndFriends(restClient.getResponse());
 
 					if (circlesAndFriends != null) {
 						StaticValues.myInfo.setCircleList(circlesAndFriends.getCircles());
 						StaticValues.myInfo.setFriendList(circlesAndFriends.getFriends());
 					}
 
 				}
 			}
 
 		}
 	};
 
 	@Override
 	public void onNewIntent(Intent intent) {
 
 		if (Utility.isLoggedIn(getApplicationContext())) {
 			handleNavigationFromNotificationBar(intent);
 		}
 
 	}
 
 	/** Register for the updates when Activity is in foreground */
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		validateFacebookSession();
 
 		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
 
 		if (resultCode == ConnectionResult.SUCCESS) {
 
 		} else {
 			GooglePlayServicesUtil.getErrorDialog(resultCode, this, 1);
 		}
 
 		isRunning = true;
 		Log.i("Home:onResume memory before", "" + Debug.getNativeHeapAllocatedSize());
 
 		if (getIntent().getBooleanExtra("LOGOUT", false)) {
 			finish();
 			StaticValues.myInfo = null;
 			StaticValues.searchResult = null;
 			System.gc();
 			Intent intent = new Intent(context, LoginActivity.class);
 			startActivity(intent);
 		}
 
 		disableSearch();
 
 		/*
 		 * if (StaticValues.isHighlightAnnotation &&
 		 * StaticValues.highlightAnnotationItem != null &&
 		 * (StaticValues.searchResult != null || eventList.size() > 0 ||
 		 * geotagList .size() > 0)) {
 		 * 
 		 * populateMasterList(); updateContentList(listMasterContent);
 		 * updateMapDisplay(listContent); }
 		 */
 
 		if (StaticValues.isHighlightAnnotation && StaticValues.highlightAnnotationItem != null) {
 
 			circleMenu.setVisibility(View.GONE);
 
 			isMoveToAnotherPoint = true;
 
 			LatLng selectedLatLng = Utility.getLatLngFromObject(StaticValues.highlightAnnotationItem);
 			mapView.animateCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, intialZoomLevel));
 
 		}
 
 		startGpsService();
 
 		Log.i("Home:onResume memory after", "" + Debug.getNativeHeapAllocatedSize());
 		Utility.updateNotificationBubbleCounter(btnNotification);
 
 		// Stop background service
 		stopService(new Intent(this, LocationUpdateService.class));
 
 		// imageFetcher.setExitTasksEarly(false);
 
 	}
 
 	private void initializeNotificationBroadcast() {
 		// TODO Auto-generated method stub
 
 		Log.i("inside", "initializeNotificationBroadcast");
 
 		broadcastReceiver = NotificationCountBroadcastReciever.getInstance();
 		broadcastReceiver.setCallback(this);
 		registerReceiver(broadcastReceiver, new IntentFilter(CommonUtilities.DISPLAY_MESSAGE_ACTION));
 	}
 
 	@Override
 	public void updateNotificationCountBuble(Intent intent) {
 
 		Log.i("inside updateNotificationCountBuble", "");
 
 		PushData pushData = (PushData) intent.getSerializableExtra("pushData");
 
 		Utility.updateNotificationCountFromPush(pushData);
 
 		Utility.updateNotificationBubbleCounter(btnNotification);
 
 	}
 
 	/*
 	 * private void highlightAnnotation() {
 	 * 
 	 * circleMenu.setVisibility(View.GONE);
 	 * 
 	 * if (StaticValues.highlightAnnotationItem instanceof People) {
 	 * 
 	 * People selectedPeople = (People) StaticValues.highlightAnnotationItem;
 	 * 
 	 * } else if (StaticValues.highlightAnnotationItem instanceof Place) { Place
 	 * place = (Place) StaticValues.highlightAnnotationItem;
 	 * 
 	 * } else if (StaticValues.highlightAnnotationItem instanceof
 	 * SecondDegreePeople) {
 	 * 
 	 * SecondDegreePeople selectedPeople = (SecondDegreePeople)
 	 * StaticValues.highlightAnnotationItem;
 	 * 
 	 * } else if (StaticValues.highlightAnnotationItem instanceof Event) {
 	 * 
 	 * Event event = (Event) StaticValues.highlightAnnotationItem;
 	 * 
 	 * } else if (StaticValues.highlightAnnotationItem instanceof GeoTag) {
 	 * GeoTag geoTag = (GeoTag) StaticValues.highlightAnnotationItem;
 	 * 
 	 * }
 	 * 
 	 * // mapView.getController().animateTo(selectedGeoPoint);
 	 * 
 	 * StaticValues.highlightAnnotationItem = null;
 	 * StaticValues.isHighlightAnnotation = false; }
 	 */
 
 	private void disableSearch() {
 		etSearchField.setText("");
 		showSearchPanel(false);
 		isSearchEnabled = false;
 	}
 
 	@Override
 	public void onAttachedToWindow() {
 		super.onAttachedToWindow();
 		// this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
 		Window window = getWindow();
 		window.setFormat(PixelFormat.RGBA_8888);
 
 	}
 
 	private void setCheckBoxSelection() {
 		// TODO Auto-generated method stub
 
 		peopleCheckBox.setChecked(SharedPreferencesHelper.getInstance(context).getBoolean(Constant.PEOPLE, true));
 		placeCheckBox.setChecked(SharedPreferencesHelper.getInstance(context).getBoolean(Constant.PLACE, false));
 		eventCheckBox.setChecked(SharedPreferencesHelper.getInstance(context).getBoolean(Constant.EVENT, false));
 		dealCheckBox.setChecked(SharedPreferencesHelper.getInstance(context).getBoolean(Constant.DEAL, false));
 
 	}
 
 	private void facebookAuthentication() {
 
 		Log.i("HomeActivity", "facebookAuthentication");
 
 		if (StaticValues.myInfo != null && !Utility.getFacebookInvitationDisplayStatus(context) && StaticValues.myInfo.getLogInCount() == 1) {
 
 			Utility.setFacebookInvitationDisplayStatus(context, true);
 
 			Log.i("HomeActivity", "inside facebookAuthentication");
 
 			if (StaticValues.myInfo.getRegMedia().equalsIgnoreCase(Constant.sourceFacebook)) {
 				initInviteFriends();
 			} else {
 				askForFacebookAccount();
 			}
 
 		}
 
 	}
 
 	private void initInviteFriends() {
 		Log.d("Facebook", "Have to invite");
 
 		if (Utility.isConnectionAvailble(context)) {
 
 			showInvitationDialog();
 
 		} else {
 			Toast.makeText(getApplicationContext(), "Internet Connection Unavailable", Toast.LENGTH_SHORT).show();
 		}
 	}
 
 	private void showInvitationDialog() {
 		Bundle params = new Bundle();
 		params.putString("message", "Checkout the app.");
 		FBUtility.mFacebook.dialog(context, "apprequests", params, new AppRequestsListener());
 	}
 
 	private void askForFacebookAccount() {
 		// have to implement later
 
 		if (!Utility.getFacebookInvitationDisplayStatus(context)) {
 
 			showFirstTimeDialog(context);
 
 		}
 
 	}
 
 	/*
 	 * callback for the apprequests dialog which sends an app request to user's
 	 * friends.
 	 */
 	private class AppRequestsListener extends BaseDialogListener {
 		// @Override
 		@Override
 		public void onComplete(Bundle values) {
 		}
 
 		@Override
 		public void onFacebookError(FacebookError error) {
 		}
 
 		@Override
 		public void onCancel() {
 		}
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 
 			if (circleMenu.getVisibility() == View.VISIBLE) {
 				circleMenu.setVisibility(View.GONE);
 			} else if (topDrawer.isOpened()) {
 				topDrawer.animateClose();
 			} else if (bottomDrawer.isOpened()) {
 				bottomDrawer.animateClose();
 			} else if (searchPanel.isShown()) {
 				showSearchPanel(false);
 			} else {
 				AlertDialog.Builder adb = new AlertDialog.Builder(this);
 				adb.setTitle("Exit");
 				adb.setIcon(R.drawable.icon_alert);
 				adb.setMessage("Are you sure you want to exit from this application?");
 				adb.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int id) {
 						finish();
 					}
 				});
 				adb.setNegativeButton("No", new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int id) {
 						dialog.cancel();
 					}
 				});
 				adb.show();
 			}
 
 		}
 
 		return false;
 
 	}
 
 	private class FetchImageTask extends AsyncTask<String, Integer, Bitmap> {
 
 		@Override
 		protected Bitmap doInBackground(String... arg0) {
 			Bitmap b = null;
 			try {
 				b = BitmapFactory.decodeStream((InputStream) new URL(arg0[0]).getContent());
 			} catch (MalformedURLException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			return b;
 		}
 	}
 
 	/*
 	 * @Override public void catchBubleTap(CustomOverlayItem item, int typeFlag)
 	 * { // TODO Auto-generated method stub
 	 * 
 	 * if (typeFlag == Constant.FLAG_PEOPLE) {
 	 * 
 	 * showPeoplePopUpDetails(item); }
 	 * 
 	 * else if (typeFlag == Constant.FLAG_PLACE) {
 	 * 
 	 * // temporarily off for beta
 	 * 
 	 * showPlacePopUpDetails(item); } if (typeFlag == Constant.FLAG_SELF) {
 	 * 
 	 * Intent profileIntent = new Intent(context, ProfileActivity.class);
 	 * startActivity(profileIntent); } else if (typeFlag ==
 	 * Constant.FLAG_SECOND_DEGREE) { } else { // do nothing } }
 	 */
 
 	private void showPlacePopUpDetails(final Place item) {
 		// TODO Auto-generated method stub
 		d = DialogsAndToasts.showExtendedInfoDialog(context, R.layout.dialog_on_map_place_extended);
 
 		TextView titleText = (TextView) d.findViewById(R.id.title_text);
 		String title = item.getName();
 		if (title != null && !title.equals("")) {
 			titleText.setText(title);
 		}
 
 		TextView addressText = (TextView) d.findViewById(R.id.address_text);
 		String address = item.getVicinity();
 		if (address != null && !address.equals("")) {
 			addressText.setText(address);
 		}
 
 		TextView distance = (TextView) d.findViewById(R.id.distance_text);
 		double distanceValue = item.getDistance();
 		distance.setText(Utility.getFormatedDistance(distanceValue, StaticValues.myInfo.getSettings().getUnit()) + " away");
 
 		ImageView closeBtn = (ImageView) d.findViewById(R.id.close_btn);
 		closeBtn.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				// TODO Auto-generated method stub
 				d.dismiss();
 			}
 		});
 
 		placeIconImage = (ImageView) d.findViewById(R.id.place_icon_image);
 		String iconUrl = item.getIconUrl();
 		if (Utility.isValidString(iconUrl)) {
 
 			// imageFetcher.loadImage(iconUrl, placeIconImage, this,
 			// item.getId());
 			Utility.setImage(item.getId(), iconUrl, placeIconImage, remoteImageCache);
 
 		}
 
 		Button save_place_btn = (Button) d.findViewById(R.id.save_place_btn);
 		save_place_btn.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				// TODO Auto-generated method stub
 
 				Place place = item;
 				Intent peopleIntent = new Intent(context, PlaceEditSaveActivity.class);
 				peopleIntent.putExtra("ISHOME", true);
 				peopleIntent.putExtra("PLACE_OBJECT", place);
 
 				startActivity(peopleIntent);
 			}
 		});
 
 		Button meet_up = (Button) d.findViewById(R.id.meet_up_btn);
 		meet_up.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				// TODO Auto-generated method stub
 
 				Log.d("PLACE MEET UP", String.valueOf(item.getLatitude()) + " " + String.valueOf(item.getLongitude()));
 
 				Intent intentForMeetup = new Intent(context, MeetupRequestNewActivity.class);
 				intentForMeetup.putExtra("destLat", item.getLatitude());
 				intentForMeetup.putExtra("destLng", item.getLongitude());
 				intentForMeetup.putExtra("destAddress", item.getVicinity());
 				intentForMeetup.putExtra("selectedPlace", item);
 				startActivity(intentForMeetup);
 			}
 		});
 
 		Button event_btn = (Button) d.findViewById(R.id.event_btn);
 		event_btn.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				// TODO Auto-generated method stub
 
 				Log.d("PLACE MEET UP", String.valueOf(item.getLatitude()) + " " + String.valueOf(item.getLongitude()));
 
 				Intent intentForEvent = new Intent(context, EventNewActivity.class);
 				intentForEvent.putExtra("flag", 1);
 				intentForEvent.putExtra("selectedPlace", item);
 				startActivity(intentForEvent);
 			}
 		});
 
 		Button recommend_btn = (Button) d.findViewById(R.id.recommend_btn);
 		recommend_btn.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				// TODO Auto-generated method stub
 
 				Place place = item;
 
 				Intent intent = new Intent(context, RecommendationActivity.class);
 				intent.putExtra("place", place);
 				startActivity(intent);
 			}
 		});
 
 		Button plan_btn = (Button) d.findViewById(R.id.plan_btn);
 		plan_btn.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				// TODO Auto-generated method stub
 
 				Place place = item;
 
 				Intent intent = new Intent(context, PlanCreateActivity.class);
 
 				intent.putExtra("selectedPlace", place);
 
 				startActivity(intent);
 			}
 		});
 
 		Button review_btn = (Button) d.findViewById(R.id.review_btn);
 		review_btn.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				// TODO Auto-generated method stub
 				Toast.makeText(context, "Review feature is coming soon", Toast.LENGTH_SHORT).show();
 			}
 		});
 
 		Button direction_btn = (Button) d.findViewById(R.id.direction_btn);
 		direction_btn.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				// TODO Auto-generated method stub
 
 				Intent intent = new Intent(context, DirectionActivity.class);
 				intent.putExtra("selectedItem", item);
 				Log.d("Place Check", item.getName() + " " + item.getVicinity() + " " + item.getLatitude() + " " + item.getLongitude());
 				startActivity(intent);
 			}
 		});
 
 		Button check_in_btn = (Button) d.findViewById(R.id.check_in_btn);
 		check_in_btn.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				// TODO Auto-generated method stub
 				Toast.makeText(context, "Check In feature is coming soon", Toast.LENGTH_SHORT).show();
 			}
 		});
 
 		d.show();
 	}
 
 	private void showPeoplePopUpDetails(final People people) {
 		// TODO Auto-generated method stub
 		d = DialogsAndToasts.showExtendedInfoDialog(context, R.layout.dialog_on_map_extended);
 
 		currentBubbleDialog = d;
 
 		final String userId = people.getId();
 
 		TextView firstName = (TextView) d.findViewById(R.id.first_name_text);
 		firstName.setText(Utility.getItemTitle(people));
 
 		LinearLayout genderInfoContainer = (LinearLayout) d.findViewById(R.id.genderInfoContainer);
 		LinearLayout relationshipInfoContainer = (LinearLayout) d.findViewById(R.id.relationshipInfoContainer);
 		LinearLayout cityInfoContainer = (LinearLayout) d.findViewById(R.id.cityInfoContainer);
 		LinearLayout workInfoContainer = (LinearLayout) d.findViewById(R.id.workInfoContainer);
 
 		TextView tvStatus = (TextView) d.findViewById(R.id.status_text);
 		TextView age = (TextView) d.findViewById(R.id.age_text);
 
 		if (Utility.isValidString(people.getStatusMsg())) {
 			tvStatus.setText(people.getStatusMsg());
 			tvStatus.setVisibility(View.VISIBLE);
 		} else
 			tvStatus.setVisibility(View.GONE);
 
 		int ageValue = people.getAge();
 		if (ageValue != 0) {
 			age.setText("-Age: " + ageValue);
 			age.setVisibility(View.VISIBLE);
 		} else
 			age.setVisibility(View.GONE);
 
 		TextView distance = (TextView) d.findViewById(R.id.distance_text);
 		double distanceValue = people.getDistance();
 
 		distance.setText(Utility.getFormatedDistance(distanceValue, StaticValues.myInfo.getSettings().getUnit()) + " away");
 
 		TextView street = (TextView) d.findViewById(R.id.street_text);
 		String sName = people.getCurrentAddress();
 		if (sName != null && !sName.equals("")) {
 			street.setText(sName);
 		} else {
 		}
 
 		TextView gender = (TextView) d.findViewById(R.id.gender_text);
 		String genderValue = people.getGender();
 		if (genderValue != null && !genderValue.equals("")) {
 			gender.setText(": " + genderValue);
 		} else {
 			genderInfoContainer.setVisibility(View.GONE);
 		}
 
 		TextView relationshipSatus = (TextView) d.findViewById(R.id.relationship_text);
 		String relationshipValue = people.getRelationshipStatus();
 		if (relationshipValue != null && !relationshipValue.equals("") && !relationshipValue.equalsIgnoreCase("select...")) {
 			relationshipSatus.setText(relationshipValue);
 		} else {
 			relationshipInfoContainer.setVisibility(View.GONE);
 		}
 
 		TextView city = (TextView) d.findViewById(R.id.city_text);
 		String cityValue = people.getCity();
 		if (cityValue != null && !cityValue.equals("")) {
 			city.setText(cityValue);
 		} else {
 			cityInfoContainer.setVisibility(View.GONE);
 		}
 
 		TextView work = (TextView) d.findViewById(R.id.work_text);
 		String workValue = people.getWorkStatus();
 		if (workValue != null && !workValue.equals("")) {
 			work.setText(workValue);
 		} else {
 			workInfoContainer.setVisibility(View.GONE);
 		}
 
 		ImageView ivOnline = (ImageView) d.findViewById(R.id.ivOnline);
 		if (people.isOnline()) {
 			ivOnline.setImageResource(R.drawable.online);
 		}
 
 		avatar = (ImageView) d.findViewById(R.id.avater_image);
 		avatar.setImageResource(R.drawable.img_blank);
 		String avatarUrl = people.getAvatar();
 		if (Utility.isValidString(avatarUrl)) {
 
 			// imageFetcher.loadImage(avatarUrl, avatar, this, people.getId());
 			Utility.setImage(people.getId(), avatarUrl, avatar, remoteImageCache);
 		}
 
 		avatar.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				// TODO Auto-generated method stub
 				Intent intent = new Intent(context, ProfileActivity2.class);
 				intent.putExtra("otherUser", people);
 				startActivity(intent);
 			}
 		});
 
 		Button meetupBtn = (Button) d.findViewById(R.id.meet_up_btn);
 		Button meetupBtnDisabled = (Button) d.findViewById(R.id.meet_up_btn_disabled);
 		Button sendMessageBtn = (Button) d.findViewById(R.id.message_btn);
 		ImageView closeBtn = (ImageView) d.findViewById(R.id.close_btn);
 		Button addFrndBtn = (Button) d.findViewById(R.id.add_frnd_btn);
 		Button directionBtn = (Button) d.findViewById(R.id.directions_btn);
 
 		TextView tvFriendshipStatus = (TextView) d.findViewById(R.id.tvFriendshipStatus);
 
 		String friendshipStatus = people.getFriendshipStatus();
 
 		Log.e("friendshipStatus", friendshipStatus);
 
 		if (friendRequestSentList.contains(userId)) {
 			tvFriendshipStatus.setText(getString(R.string.status_friend_request_pending));
 			meetupBtnDisabled.setVisibility(View.VISIBLE);
 		} else {
 			if (friendshipStatus.equalsIgnoreCase(Constant.STATUS_FRIENDSHIP_NONE)) {
 				tvFriendshipStatus.setVisibility(View.GONE);
 				addFrndBtn.setVisibility(View.VISIBLE);
 			} else if (friendshipStatus.equalsIgnoreCase(Constant.STATUS_FRIENDSHIP_FRIEND)) {
 				tvFriendshipStatus.setVisibility(View.GONE);
 				meetupBtn.setVisibility(View.VISIBLE);
 			} else {
 				tvFriendshipStatus.setVisibility(View.VISIBLE);
 				meetupBtnDisabled.setVisibility(View.VISIBLE);
 
 				String status = "";
 				if (friendshipStatus.equalsIgnoreCase(Constant.STATUS_FRIENDSHIP_FRIEND)) {
 					status = getString(R.string.status_friend_request_friend);
 					meetupBtnDisabled.setVisibility(View.GONE);
 					meetupBtn.setVisibility(View.VISIBLE);
 				} else if (friendshipStatus.equalsIgnoreCase(Constant.STATUS_FRIENDSHIP_PENDING)) {
 					status = getString(R.string.status_friend_request_pending);
 				} else if (friendshipStatus.equalsIgnoreCase(Constant.STATUS_FRIENDSHIP_REQUESTED)) {
 					status = getString(R.string.status_friend_request_sent);
 				} else if (friendshipStatus.equalsIgnoreCase(Constant.STATUS_FRIENDSHIP_REJECTED_BY_ME)) {
 					status = getString(R.string.status_friend_request_declined_by_me);
 				} else if (friendshipStatus.equalsIgnoreCase(Constant.STATUS_FRIENDSHIP_REJECTED_BY_HIM)) {
 					status = getString(R.string.status_friend_request_declined_by_him);
 				} else {
 					status = "";
 					meetupBtnDisabled.setVisibility(View.GONE);
 					addFrndBtn.setVisibility(View.VISIBLE);
 
 				}
 				tvFriendshipStatus.setText(status);
 			}
 		}
 
 		closeBtn.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				// TODO Auto-generated method stub
 				d.dismiss();
 			}
 		});
 
 		addFrndBtn.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				// TODO Auto-generated method stub
 				showFrndRequestDialog(people);
 			}
 		});
 
 		sendMessageBtn.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				// TODO Auto-generated method stub
 
 				// showMessageDialog(item);
 
 				Intent messageDetailsIntent = new Intent(context, MessageConversationFromNotificationActivity.class);
 
 				messageDetailsIntent.putExtra("recipientId", userId);
 
 				startActivity(messageDetailsIntent);
 
 			}
 
 		});
 
 		meetupBtn.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				// TODO Auto-generated method stub
 				// d.dismiss();
 				Log.d("LAT LNG Meetup People", String.valueOf(people.getCurrentLat()) + " " + String.valueOf(people.getCurrentLng()));
 
 				// People people = people;
 
 				Intent intent = new Intent(context, MeetupRequestNewActivity.class);
 				intent.putExtra("selectedPeople", people);
 				startActivity(intent);
 			}
 
 		});
 
 		meetupBtnDisabled.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				Toast.makeText(context, "To send Meet-up request, recipient should be your friend.", Toast.LENGTH_SHORT).show();
 			}
 		});
 
 		directionBtn.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				Intent intent = new Intent(context, DirectionActivity.class);
 				intent.putExtra("selectedItem", people);
 				startActivity(intent);
 			}
 		});
 
 		d.show();
 	}
 
 	private void showSecondDegreePeoplePopUpDetails(final SecondDegreePeople item) {
 		// TODO Auto-generated method stub
 		d = DialogsAndToasts.showExtendedInfoDialog(context, R.layout.dialog_on_map_second_degree_extended);
 
 		currentBubbleDialog = d;
 
 		SecondDegreePeople secondDegreePeople = item;
 
 		TextView firstName = (TextView) d.findViewById(R.id.first_name_text);
 		firstName.setText(Utility.getItemTitle(secondDegreePeople));
 
 		TextView distance = (TextView) d.findViewById(R.id.distance_text);
 		double distanceValue = secondDegreePeople.getDistance();
 
 		distance.setText(Utility.getFormatedDistance(distanceValue, StaticValues.myInfo.getSettings().getUnit()) + " away");
 
 		TextView street = (TextView) d.findViewById(R.id.street_text);
 		String sName = secondDegreePeople.getCurrentAddress();
 		if (sName != null && !sName.equals("")) {
 			street.setText(sName);
 		} else {
 		}
 
 		avatar = (ImageView) d.findViewById(R.id.avater_image);
 		String avatarUrl = secondDegreePeople.getAvatar();
 		if (Utility.isValidString(avatarUrl)) {
 			// imageFetcher.loadImage(avatarUrl, avatar,
 			// this,secondDegreePeople.getRefId());
 			Utility.setImage(secondDegreePeople.getRefId(), avatarUrl, avatar, remoteImageCache);
 		}
 
 		ImageView closeBtn = (ImageView) d.findViewById(R.id.close_btn);
 		closeBtn.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				// TODO Auto-generated method stub
 				d.dismiss();
 			}
 		});
 
 		Button meet_up_btn_2 = (Button) d.findViewById(R.id.meet_up_btn_2);
 		meet_up_btn_2.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				// TODO Auto-generated method stub
 				Intent intent = new Intent(context, MeetupRequestNewActivity.class);
 				startActivity(intent);
 			}
 		});
 
 		Button directions_btn = (Button) d.findViewById(R.id.directions_btn);
 		directions_btn.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				// TODO Auto-generated method stub
 				Intent intent = new Intent(context, DirectionActivity.class);
 				intent.putExtra("selectedItem", item);
 				startActivity(intent);
 			}
 		});
 
 		d.show();
 	}
 
 	private void showEventPopUpDetails(final Event event) {
 		// TODO Auto-generated method stub
 		d = DialogsAndToasts.showExtendedInfoDialog(context, R.layout.dialog_on_map_event_extended);
 
 		currentBubbleDialog = d;
 
 		TextView tvName = (TextView) d.findViewById(R.id.tvName);
 		TextView tvAddress = (TextView) d.findViewById(R.id.tvAddress);
 		TextView tvDate = (TextView) d.findViewById(R.id.tvDate);
 		Button btnDetails = (Button) d.findViewById(R.id.btnDetails);
 		Button btnDirection = (Button) d.findViewById(R.id.btnDirection);
 		ImageView close_btn = (ImageView) d.findViewById(R.id.close_btn);
 
 		if (event.getEventTitle() != null) {
 			tvName.setText(event.getEventTitle());
 		} else {
 			tvName.setText("");
 		}
 
 		if (event.getAddress() != null) {
 			tvAddress.setText("at " + event.getAddress());
 		} else {
 			tvAddress.setText("");
 			tvAddress.setVisibility(View.GONE);
 		}
 
 		if (event.getEventTime() != null) {
 			tvDate.setText(Utility.getFormattedDisplayDateForMap(event.getEventTime()));
 		} else {
 			tvDate.setVisibility(View.GONE);
 		}
 
 		btnDetails.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				Intent eventDetailsIntent = new Intent(context, EventDetailsActivity.class);
 				eventDetailsIntent.putExtra("selectedEvent", event);
 				eventDetailsIntent.putExtra("source", "map");
 				context.startActivity(eventDetailsIntent);
 			}
 		});
 
 		btnDirection.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				Intent intent = new Intent(context, DirectionActivity.class);
 				intent.putExtra("selectedItem", event);
 				context.startActivity(intent);
 
 			}
 		});
 
 		close_btn.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				d.dismiss();
 			}
 		});
 
 		d.show();
 	}
 
 	private void showFrndRequestDialog(final People item) {
 		// TODO Auto-generated method stub
 		frndRequestDialog = DialogsAndToasts.showAddFrnd(context);
 		final EditText msgEditText = (EditText) frndRequestDialog.findViewById(R.id.message_body_text);
 
 		Button send = (Button) frndRequestDialog.findViewById(R.id.btnSend);
 		Button cancel = (Button) frndRequestDialog.findViewById(R.id.btnCancel);
 		send.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				// TODO Auto-generated method stub
 
 				sendFriendRequest(item.getId(), msgEditText.getText().toString().trim());
 
 			}
 		});
 		cancel.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				frndRequestDialog.dismiss();
 			}
 		});
 		frndRequestDialog.show();
 	}
 
 	private void showMessageDialog(final People item) {
 		// TODO Auto-generated method stub
 		msgDialog = DialogsAndToasts.showSendMessage(context);
 		final EditText msgEditText = (EditText) msgDialog.findViewById(R.id.message_body_text);
 		Button send = (Button) msgDialog.findViewById(R.id.btnSend);
 		Button cancel = (Button) msgDialog.findViewById(R.id.btnCancel);
 		send.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				// TODO Auto-generated method stub
 				if (!msgEditText.getText().toString().trim().equals("")) {
 					sendMessage(item.getId(), "Message", msgEditText.getText().toString().trim());
 
 				} else {
 					msgEditText.setError("Please enter your message!!");
 				}
 			}
 		});
 		cancel.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				msgDialog.dismiss();
 			}
 		});
 		msgDialog.show();
 	}
 
 	private void sendSelfLocationToServer() {
 		if (Utility.isConnectionAvailble(getApplicationContext())) {
 
 			Thread thread = new Thread(null, updateLocationThread, "Start update location");
 			thread.start();
 
 			// show progress dialog if needed
 
 		} else {
 
 			Log.e("Location update", "No internet connection available.");
 		}
 	}
 
 	private Runnable updateLocationThread = new Runnable() {
 		@Override
 		public void run() {
 			// TODO Auto-generated method stub
 			RestClient restClient = new RestClient(Constant.smUpdateLocationUrl);
 			restClient.AddHeader(Constant.authTokenParam, Utility.getAuthToken(context));
 
 			restClient.AddParam("lat", "" + myLat);
 			restClient.AddParam("lng", "" + myLng);
 
 			try {
 				restClient.Execute(RestClient.RequestMethod.PUT);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			responseStringUpdateLocation = restClient.getResponse();
 
 			responseStatusUpdateLocation = restClient.getResponseCode();
 
 			runOnUiThread(returnResponseUpdateLocation);
 		}
 	};
 
 	private Runnable returnResponseUpdateLocation = new Runnable() {
 
 		@Override
 		public void run() {
 			// TODO Auto-generated method stub
 			handleResponseUpdateLocation(responseStatusUpdateLocation, responseStringUpdateLocation);
 
 			responseStringUpdateLocation = null;
 
 			// dismiss progress dialog if needed
 		}
 
 	};
 
 	private void handleResponseUpdateLocation(int responseStatusUpdateLocation, String responseStringUpdateLocation) {
 		// TODO Auto-generated method stub
 		Log.e("Update Location", responseStatusUpdateLocation + " " + responseStringUpdateLocation);
 	}
 
 	private void sendFriendRequest(String friendId, String message) {
 		if (Utility.isConnectionAvailble(getApplicationContext())) {
 
 			friendRequestFriendId = friendId;
 			friendRequestMessage = message;
 
 			Thread thread = new Thread(null, friendRequestThread, "Start send message");
 			thread.start();
 
 			// show progress dialog if needed
 			m_ProgressDialog = ProgressDialog.show(context, getResources().getString(R.string.please_wait_text), getResources().getString(R.string.sending_request_text), true, true);
 
 		} else {
 
 			DialogsAndToasts.showNoInternetConnectionDialog(context);
 		}
 	}
 
 	private Runnable friendRequestThread = new Runnable() {
 		@Override
 		public void run() {
 			// TODO Auto-generated method stub
 			RestClient restClient = new RestClient(Constant.smFriendRequestUrl + "/" + friendRequestFriendId);
 			restClient.AddHeader(Constant.authTokenParam, Utility.getAuthToken(context));
 
 			restClient.AddParam("message", friendRequestMessage);
 
 			try {
 				restClient.Execute(RestClient.RequestMethod.POST);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 
 			friendRequestResponse = restClient.getResponse();
 			friendRequestStatus = restClient.getResponseCode();
 
 			runOnUiThread(friendRequestReturnResponse);
 		}
 	};
 
 	private Runnable friendRequestReturnResponse = new Runnable() {
 
 		@Override
 		public void run() {
 			// TODO Auto-generated method stub
 			handleResponseFriendRequest(friendRequestStatus, friendRequestResponse);
 
 			// dismiss progress dialog if needed
 			if (m_ProgressDialog != null) {
 				m_ProgressDialog.dismiss();
 			}
 		}
 	};
 
 	private void handleResponseFriendRequest(int status, String response) {
 		// show proper message through Toast or Dialog
 
 		Log.d("Send Frnd Request", status + ":" + response);
 		switch (status) {
 		case Constant.STATUS_SUCCESS:
 			handleSuccssfulFriendRequest();
 
 			Toast.makeText(context, "Request sent successfully.", Toast.LENGTH_SHORT).show();
 			frndRequestDialog.dismiss();
 
 			break;
 
 		case Constant.STATUS_BADREQUEST:
 			Toast.makeText(context, "Friend request already sent to this user.", Toast.LENGTH_SHORT).show();
 			frndRequestDialog.dismiss();
 
 			break;
 		default:
 			Toast.makeText(getApplicationContext(), "An unknown error occured. Please try again!!", Toast.LENGTH_SHORT).show();
 			break;
 
 		}
 	}
 
 	private void handleSuccssfulFriendRequest() {
 		currentBubbleDialog.dismiss();
 		if (!friendRequestSentList.contains(friendRequestFriendId)) {
 			friendRequestSentList.add(friendRequestFriendId);
 		}
 
 		ArrayList<People> peopleList = StaticValues.searchResult.getPeoples();
 
 		for (int i = 0; i < peopleList.size(); i++) {
 			String userId = peopleList.get(i).getId();
 			if (friendRequestSentList.contains(userId)) {
 				peopleList.get(i).setFriendshipStatus(Constant.STATUS_FRIENDSHIP_PENDING);
 			}
 		}
 
 		StaticValues.searchResult.setPeoples(peopleList);
 
 	}
 
 	private void sendMessage(String friendId, String subject, String content) {
 
 		if (Utility.isConnectionAvailble(getApplicationContext())) {
 
 			sendMessageFriendId = friendId;
 			sendMessageSubject = subject;
 			sendMessageContent = content;
 
 			Thread thread = new Thread(null, sendMessageThread, "Start send message");
 			thread.start();
 
 			// show progress dialog if needed m_ProgressDialog =
 			ProgressDialog.show(context, getResources().getString(R.string.please_wait_text), getResources().getString(R.string.sending_request_text), true, true);
 
 		} else {
 
 			DialogsAndToasts.showNoInternetConnectionDialog(context);
 		}
 
 	}
 
 	private Runnable sendMessageThread = new Runnable() {
 		@Override
 		public void run() {
 			// TODO Auto-generated method stub
 			RestClient restClient = new RestClient(Constant.smMessagesUrl);
 			restClient.AddHeader(Constant.authTokenParam, Utility.getAuthToken(context));
 
 			restClient.AddParam("recipients[]", sendMessageFriendId);
 			restClient.AddParam("subject", sendMessageSubject);
 			restClient.AddParam("content", sendMessageContent);
 
 			try {
 				restClient.Execute(RestClient.RequestMethod.POST);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 
 			sendMessageResponse = restClient.getResponse();
 			sendMessageStatus = restClient.getResponseCode();
 
 			runOnUiThread(sendMessageReturnResponse);
 		}
 	};
 
 	private Runnable sendMessageReturnResponse = new Runnable() {
 
 		@Override
 		public void run() {
 			// TODO Auto-generated method stub
 			handleResponseSendMessage(sendMessageStatus, sendMessageResponse);
 
 			// dismiss progress dialog if needed
 			if (m_ProgressDialog != null) {
 				m_ProgressDialog.dismiss();
 			}
 		}
 	};
 
 	private void handleResponseSendMessage(int status, String response) {
 		// show proper message through Toast or Dialog
 		Log.d("Send Message", status + ":" + response);
 		switch (status) {
 		case Constant.STATUS_CREATED:
 			Toast.makeText(context, "Message sent successfully.", Toast.LENGTH_SHORT).show();
 			msgDialog.dismiss();
 			break;
 
 		default:
 			Toast.makeText(getApplicationContext(), "An unknown error occured. Please try again!!", Toast.LENGTH_SHORT).show();
 			break;
 
 		}
 
 	}
 
 	private static final class TopDrawerListener implements MultiDirectionSlidingDrawer.OnDrawerOpenListener, MultiDirectionSlidingDrawer.OnDrawerCloseListener {
 
 		@Override
 		public void onDrawerClosed() {
 			// TODO Auto-generated method stub
 
 			topCloseButton.setBackgroundResource(R.drawable.btn_slider_open);
 		}
 
 		@Override
 		public void onDrawerOpened() {
 			// TODO Auto-generated method stub
 
 			if (bottomDrawer.isOpened()) {
 				bottomDrawer.animateClose();
 			}
 
 			topCloseButton.setBackgroundResource(R.drawable.btn_slider_close);
 		}
 
 	}
 
 	private static class BottomDrawerListener implements MultiDirectionSlidingDrawer.OnDrawerOpenListener, MultiDirectionSlidingDrawer.OnDrawerCloseListener {
 		@Override
 		public void onDrawerClosed() {
 			// TODO Auto-generated method stub
 
 			bottomCloseButton.setBackgroundResource(R.drawable.btn_footer_slider_open);
 		}
 
 		@Override
 		public void onDrawerOpened() {
 			// TODO Auto-generated method stub
 
 			if (topDrawer.isOpened()) {
 				topDrawer.animateClose();
 			}
 
 			bottomCloseButton.setBackgroundResource(R.drawable.btn_footer_slider_close);
 		}
 
 	}
 
 	@Override
 	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 		// TODO Auto-generated method stub
 
 		if (buttonView == peopleCheckBox) {
 			SharedPreferencesHelper.getInstance(context).setBoolean(Constant.PEOPLE, isChecked);
 
 			updateMapDisplay(listContent);
 			// setMarkerVisibility(Constant.FLAG_PEOPLE, isChecked);
 		}
 		if (buttonView == placeCheckBox) {
 
 			SharedPreferencesHelper.getInstance(context).setBoolean(Constant.PLACE, isChecked);
 
 			updateMapDisplay(listContent);
 			// setMarkerVisibility(Constant.FLAG_PLACE, isChecked);
 		}
 		if (buttonView == eventCheckBox) {
 
 			SharedPreferencesHelper.getInstance(context).setBoolean(Constant.EVENT, isChecked);
 
 			updateMapDisplay(listContent);
 			// setMarkerVisibility(Constant.FLAG_EVENT, isChecked);
 		}
 		if (buttonView == dealCheckBox) {
 			SharedPreferencesHelper.getInstance(context).setBoolean(Constant.DEAL, isChecked);
 		}
 	}
 
 	@Override
 	public void onClick(View v) {
 		// TODO Auto-generated method stub
 
 		Utility.hideKeyboardContext(context);
 
 		if (v == btnListView || v == tvListView) {
 			if (StaticValues.searchResult != null) {
 				Intent showListIntent = new Intent(context, ListViewActivity.class);
 				startActivity(showListIntent);
 			} else
 				Toast.makeText(context, "No data found yet.", Toast.LENGTH_SHORT).show();
 		} else if (v == btnCircle) {
 			if (bottomDrawer.isOpened()) {
 				bottomDrawer.animateClose();
 			}
 			circleMenu.setVisibility(View.VISIBLE);
 		} else if (v == btnNotification) {
 			Intent i = new Intent(HomeActivity.this, NotificationActivity.class);
 			startActivity(i);
 		} else if (v == btnDirections) {
 			Intent i = new Intent(context, DirectionActivity.class);
 			startActivity(i);
 		} else if (v == btnMyPlaces) {
 			Intent i = new Intent(context, PlacesListActivity.class);
 			startActivity(i);
 		} else if (v == btnMeetup) {
 			Intent i = new Intent(context, MeetupRequestNewActivity.class);
 			startActivity(i);
 		}
 
 		else if (v == btnUpdateLocation) {
 			// relocationCurrentPosition();
 		} else if (v == btnCircleMenu) {
 			circleMenu.setVisibility(View.VISIBLE);
 		}
 
 		else if (v == circleMenu) {
 			// do nothing. but keep it.
 		}
 
 		else if (v == btnCloseCircleMenu) {
 			circleMenu.setVisibility(View.GONE);
 		}
 
 		else if (v == btnCircleMenuItemDeals) {
 
 			Toast.makeText(context, "Coming soon.", Toast.LENGTH_SHORT).show();
 
 		} else if (v == btnCircleMenuItemFriends) {
 
 			Intent messageIntent = new Intent(getApplicationContext(), FriendListActivity.class);
 			startActivity(messageIntent);
 
 		} else if (v == btnCircleMenuItemMessages) {
 			Intent messageIntent = new Intent(getApplicationContext(), MessageActivity.class);
 			startActivity(messageIntent);
 		} else if (v == btnCircleMenuItemNewsfeed) {
 
 			Intent messageIntent = new Intent(getApplicationContext(), NewsFeedActivity.class);
 			startActivity(messageIntent);
 		} else if (v == btnCircleMenuItemPeople) {
 
 			Intent peopleIntent = new Intent(getApplicationContext(), PeopleListActivity.class);
 			startActivity(peopleIntent);
 
 		} else if (v == btnCircleMenuItemPlaces) {
 
 			Intent placeIntent = new Intent(getApplicationContext(), PlacesListActivity.class);
 			startActivity(placeIntent);
 		} else if (v == btnCircleMenuItemProfile) {
 			Intent profileIntent = new Intent(getApplicationContext(), ProfileActivity.class);
 			startActivity(profileIntent);
 		} else if (v == btnCircleMenuItemSettings) {
 			Intent settingsIntent = new Intent(getApplicationContext(), SettingsActivity.class);
 			startActivity(settingsIntent);
 		}
 
 		else if (v == btnToggleSearchPanel) {
 			toggleSearchPanel();
 		} else if (v == btnDoSearch) {
 
 			// Log.i("btnDoSearch", "pressed");
 
 			if (listMasterContent.size() > 0 && !etSearchField.getText().toString().equalsIgnoreCase("")) {
 				isSearchEnabled = true;
 				doSearch();
 			}
 
 		} else if (v == btnClearSearch) {
 			etSearchField.setText("");
 			if (isSearchEnabled) {
 				isSearchEnabled = false;
 
 				if (listMasterContent.size() > 0) {
 					doSearch();
 				}
 
 			}
 
 		}
 
 	}
 
 	private void doSearch() {
 
 		// Log.i("doSearch", "entered");
 
 		isNewSearch = true;
 
 		List<Object> list = Utility.getSearchResult(listMasterContent, etSearchField.getText().toString());
 
 		listContent.clear();
 		listContent.addAll(list);
 
 		updateMapDisplay(listContent);
 	}
 
 	private void toggleSearchPanel() {
 		if (!searchPanel.isShown())
 			showSearchPanel(true);
 		else
 			showSearchPanel(false);
 	}
 
 	private void showSearchPanel(boolean display) {
 		if (display) {
 			searchPanel.setVisibility(View.VISIBLE);
 
 		} else {
 			searchPanel.setVisibility(View.GONE);
 		}
 	}
 
 	@Override
 	public void onPermissionChanged(RadioGroup group, RadioButton radio, Permission selectedItem) {
 		// TODO Auto-generated method stub
 
 		if (locationSharingPermission != selectedItem) {
 			locationSharingPermission = selectedItem;
 
 			updateLocationSharingPermission();
 		}
 	}
 
 	private void updateLocationSharingPermission() {
 
 		Thread thread = new Thread(sharingUpdateRunnable);
 
 		thread.start();
 	}
 
 	Runnable sharingUpdateRunnable = new Runnable() {
 
 		@Override
 		public void run() {
 			// TODO Auto-generated method stub
 			RestClient restClient = new RestClient(Constant.smLocationSharingUrl);
 			restClient.AddHeader(Constant.authTokenParam, Utility.getAuthToken(context));
 
 			restClient.AddParam("shareLocation", "" + locationSharingPermission.ordinal());
 
 			try {
 				restClient.Execute(RestClient.RequestMethod.PUT);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 
 			if (restClient.getResponseCode() == Constant.STATUS_SUCCESS) {
 				StaticValues.myInfo.getSettings().setShareLocation(locationSharingPermission.ordinal());
 			}
 
 			Log.i("LocationSharing update", restClient.getResponseCode() + ":" + restClient.getResponse());
 		}
 	};
 
 	private void showFirstTimeDialog(final Context c) {
 
 		isFirstTimeFbdialog = new Dialog(c, R.style.CustomDialogTheme);
 		isFirstTimeFbdialog.setContentView(R.layout.first_time_dialog);
 
 		isFirstTimeFbdialog.getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
 
 		btnFBLogin = (Button) isFirstTimeFbdialog.findViewById(R.id.btnFBLoginDialog);
 
 		// Create the Facebook Object using the app id.
 		FBUtility.mFacebook = new Facebook(Constant.FB_APP_ID);
 		FBUtility.mAsyncRunner = new AsyncFacebookRunner(FBUtility.mFacebook);
 
 		// restore session if one exists
 		// SessionStore.restore(FBUtility.mFacebook, c);
 		SessionEvents.addAuthListener(this);
 		SessionEvents.addLogoutListener(this);
 
 		btnFBLogin.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				// TODO Auto-generated method stub
 				FBUtility.mFacebook.authorize(HomeActivity.this, Constant.facebookPermissions, HomeActivity.this);
 			}
 		});
 
 		Button btnCrossFb = (Button) isFirstTimeFbdialog.findViewById(R.id.btnCrossFb);
 		// if button is clicked, close the custom dialog
 		btnCrossFb.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 
 				isFirstTimeFbdialog.dismiss();
 			}
 		});
 
 		isFirstTimeFbdialog.show();
 
 	}
 
 	@Override
 	public void onAuthSucceed() {
 
 		initInviteFriends();
 		isFirstTimeFbdialog.dismiss();
 
 	}
 
 	// @Override
 	@Override
 	public void onAuthFail(String error) {
 		Log.e("HomeActivity", "Login Failed: " + error);
 	}
 
 	@Override
 	public void onLogoutBegin() {
 		Log.e("HomeActivity", "Logging out...");
 	}
 
 	// @Override
 	@Override
 	public void onLogoutFinish() {
 		Log.e("HomeActivity", "You have logged out! ");
 
 		Utility.setFacebookImage(context, null);
 
 		FBUtility.mFacebook.authorize(HomeActivity.this, Constant.facebookPermissions, Facebook.FORCE_DIALOG_AUTH, this);
 	}
 
 	@Override
 	public void onComplete(Bundle values) {
 		Log.e("LoginDialogListener", "onComplete");
 		SessionEvents.onLoginSuccess();
 	}
 
 	// @Override
 	@Override
 	public void onFacebookError(FacebookError error) {
 		Log.e("LoginDialogListener", "onFacebookError");
 		SessionEvents.onLoginError(error.getMessage());
 	}
 
 	// @Override
 	@Override
 	public void onError(DialogError error) {
 		Log.e("LoginDialogListener", "onError");
 		SessionEvents.onLoginError(error.getMessage());
 	}
 
 	// @Override
 	@Override
 	public void onCancel() {
 		Log.e("LoginDialogListener", "onCancel");
 		SessionEvents.onLoginError("Action Canceled");
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 
 		FBUtility.mFacebook.authorizeCallback(requestCode, resultCode, data);
 	}
 
 	private void updateMapDisplayOnRegionChange() {
 
 		if (mapUpdateTimer != null) {
 			mapUpdateTimer.cancel();
 		}
 
 		TimerTask timerTask = new TimerTask() {
 			public void run() {
 
 				runOnUiThread(mapUpdateTimerTask);
 
 			}
 		};
 		mapUpdateTimer = new Timer();
 		mapUpdateTimer.schedule(timerTask, mapUpdateInterval);
 
 	}
 
 	private Runnable mapUpdateTimerTask = new Runnable() {
 
 		@Override
 		public void run() {
 			// TODO Auto-generated method stub
 			updateMapDisplay(listContent);
 		}
 	};
 
 	@Override
 	public void onCameraChange(CameraPosition position) {
 		// TODO Auto-generated method stub
 		Log.i("HomeActivity", "onCameraChange");
 
 		updateMapDisplayOnRegionChange();
 
 	}
 
 	@Override
 	public boolean onMarkerClick(Marker marker) {
 		// TODO Auto-generated method stub
 		// lastClickedMarker = marker;
 
 		if (visibleItemsOnMap != null) {
 
 			Object item = visibleItemsOnMap.get(marker.getId());
 			if (!marker.isVisible()) {
 				return true;
 			} else if (item instanceof Event) {
 				// lastClickedMarker = null;
 
 				showEventPopUpDetails((Event) item);
 
 				return true;
 			}
 
 			Log.i("onMarkerClick", Utility.getItemTitle(item));
 		}
 
 		return false;
 	}
 
 	@Override
 	public void onMapLongClick(LatLng arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onMapClick(LatLng arg0) {
 		// TODO Auto-generated method stub
 		// lastClickedMarker = null;
 
 	}
 
 	@Override
 	public void onInfoWindowClick(Marker marker) {
 		// TODO Auto-generated method stub
 		Object item = visibleItemsOnMap.get(marker.getId());
 		// contents = inflater.inflate(R.layout.content_party, null);
 
 		if (item != null) {
 			getCustomInfoWindow(item);
 			// lastClickedMarker = null;
 			marker.hideInfoWindow();
 		}
 	}
 
 	private void getCustomInfoWindow(Object item) {
 
 		if (item instanceof People) {
 
 			showPeoplePopUpDetails((People) item);
 
 		} else if (item instanceof SecondDegreePeople) {
 
 			// showSecondDegreePeoplePopUpDetails((SecondDegreePeople) item);
 
 		} else if (item instanceof Place) {
 
 			showPlacePopUpDetails((Place) item);
 
 		} else if (item instanceof Event) {
 
 		} else if (item instanceof GeoTag) {
 
 		} else if (item instanceof MeetupRequest) {
 
 		}
 
 	}
 
 	public synchronized void updateMarker(String itemId, Bitmap bitmap) {
 
 		Object item = objectList.get(itemId);
 		if (item != null) {
 
 			boolean isInfoWindowShown = false;
 			boolean isVisible = false;
 
 			if (visibleItemsOnMap.containsValue(item)) {
 				visibleItemsOnMap.values().remove(item);
 			}
 			if (visibleMarkers.containsKey(itemId)) {
 				Marker markerOld = visibleMarkers.get(itemId);
 				if (markerOld != null) {
 					isInfoWindowShown = markerOld.isInfoWindowShown();
 					isVisible = markerOld.isVisible();
 					markerOld.remove();
 				}
 
 			}
 
 			Drawable profilePic = new BitmapDrawable(bitmap);
 			MarkerOptions markerOptions = getMarkerOptions(item, profilePic);
 
 			if (markerOptions != null) {
 				Marker marker = mapView.addMarker(markerOptions);
 				visibleItemsOnMap.put(marker.getId(), item);
 				visibleMarkers.put(itemId, marker);
 				markerUpdateList.put(itemId, true);
 
 				// customInfoWindowAdapter = new
 				// CustomInfoWindowAdapter(context,getLayoutInflater(),
 				// visibleItemsOnMap, imageFetcher, this);
 
 				customInfoWindowAdapter = new CustomInfoWindowAdapter(context, getLayoutInflater(), visibleItemsOnMap, remoteImageCache, imageCacheListener);
 
 				mapView.setInfoWindowAdapter(customInfoWindowAdapter);
 				if (!isVisible) {
 					marker.setVisible(isVisible);
 				} else if (isInfoWindowShown) {
 					marker.showInfoWindow();
 				}
 			}
 
 		}
 
 	}
 
 	private void updateMarkerVisibility() {
 
 		boolean isPeople = SharedPreferencesHelper.getInstance(context).getBoolean(Constant.PEOPLE, true);
 
 		boolean isPlace = SharedPreferencesHelper.getInstance(context).getBoolean(Constant.PLACE, false);
 
 		boolean isEvent = SharedPreferencesHelper.getInstance(context).getBoolean(Constant.EVENT, false);
 
 		if (objectList != null) {
 			for (String itemId : objectList.keySet()) {
 				Object item = objectList.get(itemId);
 				if (item instanceof People || item instanceof SecondDegreePeople) {
 					visibleMarkers.get(itemId).setVisible(isPeople);
 				} else if (item instanceof Place) {
 					visibleMarkers.get(itemId).setVisible(isPlace);
 				} else if (item instanceof Event) {
 					Marker marker = visibleMarkers.get(itemId);
 					if (marker != null) {
 						marker.setVisible(isEvent);
 					}
 				}
 			}
 		}
 
 	}
 
 	private void setMarkerVisibility(int flag, boolean isChecked) {
 		if (objectList != null) {
 			for (String itemId : objectList.keySet()) {
 				Object item = objectList.get(itemId);
 				if ((flag == Constant.FLAG_PEOPLE) && (item instanceof People || item instanceof SecondDegreePeople)) {
 					visibleMarkers.get(itemId).setVisible(isChecked);
 				} else if ((flag == Constant.FLAG_PLACE) && item instanceof Place) {
 					visibleMarkers.get(itemId).setVisible(isChecked);
 				} else if ((flag == Constant.FLAG_EVENT) && item instanceof Event) {
 					Marker marker = visibleMarkers.get(itemId);
 					if (marker != null) {
 						marker.setVisible(isChecked);
 					}
 				}
 			}
 		}
 	}
 
 	private void validateExistingMarkers() {
 		// TODO Auto-generated method stub
 
 		List<String> itemIdList = new ArrayList<String>();
 
 		for (Object listItem : listMasterContent) {
 			String itemId = Utility.getItemId(listItem);
 			itemIdList.add(itemId);
 
 			if (objectList.containsKey(itemId)) {
 				Object oldItem = objectList.get(itemId);
 
 				LatLng newLatLng = Utility.getLatLngFromObject(oldItem);
 				LatLng oldLatLng = Utility.getLatLngFromObject(listItem);
				
				boolean isStatusChanged = false;
				
				if(listItem instanceof People)
				{
					try {
						People peopleOld = (People) oldItem;
						People peopleNew = (People) listItem;
						if(peopleOld.isOnline() != peopleNew.isOnline())
						{
							isStatusChanged = true;
						}
					} catch (Exception e) {
						// TODO: handle exception
					}
					
					
				}
 
				if (newLatLng.latitude != oldLatLng.latitude || newLatLng.longitude != oldLatLng.longitude || isStatusChanged) {
 					// update visibleItemsOnMap
 					// update visibleMarkers
 					// update objectList
 
 					Marker oldMarker = visibleMarkers.get(itemId);
 					String oldMarkerId = null;
 					if (oldMarker != null) {
 						oldMarkerId = oldMarker.getId();
 						oldMarker.remove();
 						visibleItemsOnMap.remove(oldMarkerId);
 					}
 
 					visibleMarkers.remove(itemId);
 					objectList.remove(itemId);
 
 					MarkerOptions markerOptions = getMarkerOptions(listItem);
 					if (markerOptions != null) {
 						Marker marker = mapView.addMarker(markerOptions);
 						visibleItemsOnMap.put(marker.getId(), listItem);
 						visibleMarkers.put(itemId, marker);
 						objectList.put(itemId, listItem);
 						markerUpdateList.put(itemId, false);
 					}
 
 				}
 
 			}
 		}
 
 		for (String itemId : ((HashMap<String, Object>) objectList.clone()).keySet()) {
 			if (!itemIdList.contains(itemId)) {
 				Marker oldMarker = visibleMarkers.get(itemId);
 				String oldMarkerId = null;
 				if (oldMarker != null) {
 					oldMarkerId = oldMarker.getId();
 					oldMarker.remove();
 					visibleItemsOnMap.remove(oldMarkerId);
 				}
 				visibleMarkers.remove(itemId);
 				objectList.remove(itemId);
 				markerUpdateList.remove(itemId);
 			}
 		}
 
 	}
 
 	/*
 	 * boolean isTimerRunning = false; Timer markerUpdateTimer = new Timer();
 	 * 
 	 * private class MarkerUpdateTask extends TimerTask {
 	 * 
 	 * @Override public void run() { // TODO Auto-generated method stub
 	 * runOnUiThread(new Runnable() {
 	 * 
 	 * @Override public void run() { // TODO Auto-generated method stub
 	 * validateMarkers(); } }); } }
 	 * 
 	 * private void validateMarkers() {
 	 * 
 	 * Log.i("HomeActivity", "validateMarkers"); HashMap<String, Boolean>
 	 * tempMap = (HashMap<String, Boolean>) markerUpdateList .clone();
 	 * 
 	 * ImageView imageView = new ImageView(context);
 	 * imageView.setVisibility(View.GONE); for (String itemId :
 	 * tempMap.keySet()) { if (!tempMap.get(itemId)) { Object object =
 	 * objectList.get(itemId); if (object != null) { String imageUrl =
 	 * Utility.getItemImageUrl(object); if (Utility.isValidString(imageUrl)) {
 	 * Log.i("HomeActivity:validateMarkers", "retrying:" + imageUrl);
 	 * imageFetcher.loadImage(imageUrl, imageView, HomeActivity.this, itemId); }
 	 * }
 	 * 
 	 * } }
 	 * 
 	 * }
 	 */
 
 	private void validateFacebookSession() {
 		if (StaticValues.myInfo.getRegMedia().equals(Constant.sourceFacebook) && !Utility.isFacebookSessionValid(context)) {
 
 			finish();
 			startActivity(new Intent(context, LoginActivity.class));
 
 		}
 
 	}
 
 	/*
 	 * @Override public void onDownloadComplete(final String itemId, final
 	 * Bitmap bitmap, final String imageUrl) { // TODO Auto-generated method
 	 * stub Log.i("ImageDownloaded:" + itemId, imageUrl);
 	 * 
 	 * // Log.i("ImageDownloaded:>>"+itemId, imageUrl); Timer timer = new
 	 * Timer(); timer.schedule(new TimerTask() {
 	 * 
 	 * @Override public void run() { Log.i("ImageDownloaded:>>" + itemId,
 	 * imageUrl); if (markerUpdateList.containsKey(itemId)) { if
 	 * (!markerUpdateList.get(itemId)) { runOnUiThread(new Runnable() {
 	 * 
 	 * @Override public void run() { // TODO Auto-generated method stub
 	 * updateMarker(itemId, bitmap); } });
 	 * 
 	 * } } } }, 1000);
 	 * 
 	 * }
 	 */
 
 	ImageCacheListener imageCacheListener = new ImageCacheListener() {
 
 		@Override
 		public void onSuccess(final ImageInfo imageInfo, final Bitmap bitmap) {
 			Log.i("imageCacheListener:onSuccess", imageInfo.imageUrl);
 
 			Timer timer = new Timer();
 			timer.schedule(new TimerTask() {
 
 				@Override
 				public void run() {
 
 					if (markerUpdateList.containsKey(imageInfo.id)) {
 						if (!markerUpdateList.get(imageInfo.id)) {
 							runOnUiThread(new Runnable() {
 
 								@Override
 								public void run() {
 									// TODO Auto-generated method stub
 									updateMarker(imageInfo.id, bitmap);
 								}
 							});
 
 						}
 					}
 				}
 			}, 1000);
 
 		}
 
 		@Override
 		public void onFailure(Throwable error, ImageInfo imageInfo) {
 			Log.i("imageCacheListener:onFailure", imageInfo.id + ":" + imageInfo.imageUrl);
 			// remoteImageCache.getImage(imageInfo);
 		}
 	};
 
 }
