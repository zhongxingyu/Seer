 package co.shoutbreak.ui;
 
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.graphics.drawable.AnimationDrawable;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.provider.Settings;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.animation.AnimationUtils;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 import co.shoutbreak.R;
 import co.shoutbreak.core.C;
 import co.shoutbreak.core.Colleague;
 import co.shoutbreak.core.Mediator;
 import co.shoutbreak.core.ServiceBridgeInterface;
 import co.shoutbreak.core.ShoutbreakService;
 import co.shoutbreak.core.utils.DialogBuilder;
 import co.shoutbreak.core.utils.Flag;
 import co.shoutbreak.core.utils.SBLog;
 import co.shoutbreak.storage.noticetab.NoticeTabView;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 
 public class Shoutbreak extends MapActivity implements Colleague {
 	
 	private static String TAG = "Shoutbreak";
 
 	private Mediator _m;
 	private Intent _serviceIntent;
 	private ServiceBridgeInterface _serviceBridge;
 	
 	public NoticeTabView noticeTab;
 	public UserLocationOverlay overlay;
 	public ImageButton shoutBtn;
 	public EditText shoutInputEt;
 	public DialogBuilder dialogBuilder;
 	public ImageView noticeTabShoutsIv;
 	public ImageView noticeTabPointsIv;
 	public TextView noticeTabShoutsTv;
 	public TextView noticeTabPointsTv;	
 	public ListView noticeTabListView;
 	public ListView inboxListView;
 	public TextView levelTv;
 	public TextView pointsTv;
 	public TextView nextLevelAtTv;
 	public ProgressBar progressPb;
 	
 	private ImageButton _powerBtn;
 	private ImageButton _composeTabBtn;
 	private ImageButton _inboxTabBtn;
 	private ImageButton _profileTabBtn;
 	private ImageButton _enableLocationBtn;
 	private ImageButton _turnOnBtn;
 	private LinearLayout _splashLl;
 	private LinearLayout _composeViewLl;
 	private LinearLayout _inboxViewLl;
 	private LinearLayout _profileViewLl;
 	private CustomMapView _map;
 		
 	private Flag _isComposeShowing = new Flag("ui:_isComposeShowing");
 	private Flag _isInboxShowing = new Flag("ui:_isInboxShowing");
 	private Flag _isProfileShowing = new Flag("ui:_isProfileShowing");
 	private Flag _isTurnedOn = new Flag("ui:_isTurnedOn");
 	private Flag _isLocationEnabled = new Flag("ui:_isLocationEnabled");
 	private Flag _isDataEnabled = new Flag("ui:_isDataEnabled");
 	private Flag _isPowerPreferenceEnabled = new Flag("ui:_isPowerPreferenceEnabled");		// is power preference set to on
 	
 	@Override
 	public void onCreate(Bundle extras) {
 		SBLog.i(TAG, "onCreate()");
 		super.onCreate(extras);
 		setContentView(R.layout.main);
 
 		shoutBtn = (ImageButton) findViewById(R.id.shoutBtn);
 		shoutInputEt = (EditText) findViewById(R.id.shoutInputEt);
 		inboxListView = (ListView) findViewById(R.id.inboxLv);
 		noticeTabListView = (ListView) findViewById(R.id.noticeLv);
 		noticeTab = (NoticeTabView) findViewById(R.id.noticeTab);
 		noticeTabShoutsIv = (ImageView) findViewById(R.id.noticeTabShoutsIv);
 		noticeTabPointsIv = (ImageView) findViewById(R.id.noticeTabPointsIv);
 		noticeTabShoutsTv = (TextView) findViewById(R.id.noticeTabShoutsTv);
 		noticeTabPointsTv = (TextView) findViewById(R.id.noticeTabPointsTv);
 		levelTv = (TextView) findViewById(R.id.userLevelTv);
 		pointsTv = (TextView) findViewById(R.id.userPointsTv);
 		nextLevelAtTv = (TextView) findViewById(R.id.userNextLevelAtTv);
 		progressPb = (ProgressBar) findViewById(R.id.userProgressPb);
 		
 		_composeTabBtn = (ImageButton) findViewById(R.id.composeTabBtn);
 		_inboxTabBtn = (ImageButton) findViewById(R.id.inboxTabBtn);
 		_profileTabBtn = (ImageButton) findViewById(R.id.profileTabBtn);
 		_powerBtn = (ImageButton) findViewById(R.id.powerBtn);
 		_splashLl = (LinearLayout) findViewById(R.id.splashLl);
 		_composeViewLl = (LinearLayout) findViewById(R.id.composeViewLl);
 		_inboxViewLl = (LinearLayout) findViewById(R.id.inboxViewLl);
 		_profileViewLl = (LinearLayout) findViewById(R.id.profileViewLl);
 		_enableLocationBtn = (ImageButton) findViewById(R.id.enableLocationBtn);
 		_turnOnBtn = (ImageButton) findViewById(R.id.turnOnBtn);
 		_map = (CustomMapView) findViewById(R.id.mapCmv);
 		
 		shoutBtn.setOnClickListener(_shoutButtonListener);
 		_composeTabBtn.setOnClickListener(_composeTabListener);
 		_inboxTabBtn.setOnClickListener(_inboxTabListener);
 		_profileTabBtn.setOnClickListener(_profileTabListener);
 		_powerBtn.setOnClickListener(_powerButtonListener);
 		_enableLocationBtn.setOnClickListener(_enableLocationListener);
 		_turnOnBtn.setOnClickListener(_turnOnListener);
 		
 		noticeTabShoutsIv.setVisibility(View.INVISIBLE);
 		noticeTabPointsIv.setVisibility(View.INVISIBLE);
 		noticeTabShoutsTv.setVisibility(View.INVISIBLE);
 		noticeTabPointsTv.setVisibility(View.INVISIBLE);
 		
 		// bind to service, initializes mediator
 		_serviceIntent = new Intent(Shoutbreak.this, ShoutbreakService.class);
 		bindService(_serviceIntent, _serviceConnection, Context.BIND_AUTO_CREATE);
 	}
 	
 	@Override
 	public void onResume() {
 		SBLog.i(TAG, "onResume()");
 		super.onResume();
 		//refreshFlags();
 	}
 	
 	public void setMediator(Mediator mediator) {
 		SBLog.i(TAG, "setMediator()");
 		_m = mediator;
 	}
 
 	@Override
 	public void unsetMediator() {
 		SBLog.i(TAG, "unsetMediator()");
 		_m = null;
 	}
 	
 	private ServiceConnection _serviceConnection = new ServiceConnection() {
 
 		@Override
 		public void onServiceConnected(ComponentName name, IBinder service) {
 			SBLog.i(TAG, "onServiceConnected()");
 
 			_serviceBridge = (ServiceBridgeInterface) service;
 			_serviceBridge.registerUIWithMediator(Shoutbreak.this);
 			_m.onServiceConnected();
 			
 			_serviceIntent.putExtra(C.NOTIFICATION_LAUNCHED_FROM_UI, true);
 			startService(_serviceIntent);
 			
 			overlay = new UserLocationOverlay(Shoutbreak.this, _map);
 			dialogBuilder = new DialogBuilder(Shoutbreak.this);
 			
 			refreshFlags();			
 			_m.refreshUiComponents();
 			
 			hideSplash();
 			checkForReferral();
 		}
 
 		@Override
 		public void onServiceDisconnected(ComponentName name) {
 			SBLog.i(TAG, "onServiceDisconnected()");
 			_m.onServiceDisconnected();
 		}
 	};
 	
 	@Override
 	public void onNewIntent(Intent intent) {
 		SBLog.i(TAG, "onNewIntent()");
 		super.onNewIntent(intent);
 		// triggered when a notification tries to launch a new intent
 		// and app is set to SINGLE_TOP
 		checkForReferral();
 	}
 	
 	
 	private void refreshFlags() {
 		SBLog.i(TAG, "refreshFlags()");
 		_isDataEnabled.set(_m.isDataEnabled());
 		_isLocationEnabled.set(_m.isLocationEnabled());
 		_isPowerPreferenceEnabled.set(_m.isPowerPreferenceEnabled());
 		_isTurnedOn.set(false);
 		
 		if (_isDataEnabled.get()) {
 			_m.onDataEnabled();
 		} else {
 			_m.onDataDisabled();
 		}
 		
 		if (_isLocationEnabled.get()) {
 			_m.onLocationEnabled();
 		} else {
 			_m.onLocationDisabled();
 		}
 		
 		if (_isPowerPreferenceEnabled.get()) {
 			_m.onPowerPreferenceEnabled();
 		} else {
 			_m.onPowerPreferenceDisabled();
 		}
 	}
 	
 	private void hideSplash() {
 		SBLog.i(TAG, "hideSplash()");
 		Handler splashHandler = new Handler() {
 			@Override
 			public void handleMessage(Message message) {
 		        _splashLl.startAnimation(AnimationUtils.loadAnimation(Shoutbreak.this, android.R.anim.fade_out));
 				_splashLl.setVisibility(View.GONE);
 				handleFirstRun();
 				super.handleMessage(message);
 			}
 		};
 		splashHandler.sendMessageDelayed(new Message(), 2000);
 	}
 	
 	private void checkForReferral() {
 		SBLog.i(TAG, "checkForReferral()");
 		Bundle extras = getIntent().getExtras();
 		if (extras != null && extras.getBoolean(C.NOTIFICATION_LAUNCHED_FROM_NOTIFICATION)) {
 			showInbox();	// app launched from notification
 		} else {
 			showCompose();
 		}
 	}
 	
 	private void handleFirstRun() {
 		if (_m.isFirstRun()) {
 			// TODO: tutorial goes here
 			TutorialDialog tut = new TutorialDialog(this);
 	        tut.show();
 		}
 	}
 	
 	public void onLocationEnabled() {
 		SBLog.i(TAG, "onLocationEnabled()");
 		_isLocationEnabled.set(true);
 		turnOn();
 	}
 	
 	public void onLocationDisabled() {
 		SBLog.i(TAG, "onLocationDisabled()");
 		_isLocationEnabled.set(false);
 		turnOff();
 	}
 	
 	public void onDataEnabled() {
 		SBLog.i(TAG, "onDataEnabled()");
 		_isDataEnabled.set(true);
 		turnOn();
 	}
 	
 	public void onDataDisabled() {
 		SBLog.i(TAG, "onDataDisabled()");
 		_isDataEnabled.set(false);
 		turnOff();
 	}
 	
 	public void onPowerPreferenceEnabled() {
 		SBLog.i(TAG, "onPowerPreferenceEnabled()");
 		_isPowerPreferenceEnabled.set(true);
 		turnOn();
 	}
 	
 	public void onPowerPreferenceDisabled() {
 		SBLog.i(TAG, "onPowerPreferenceDisabled()");
 		_isPowerPreferenceEnabled.set(false);
 		turnOff();
 	}
 	
 	private void enableComposeView() {
 		SBLog.i(TAG, "enableComposeView()");
 		boolean removeBlanket = true;
 		if (!_isLocationEnabled.get()) {
 			removeBlanket = false;
 		} else {
 			// TODO: hide blanket location button
 			findViewById(R.id.enableLocationBtn).setVisibility(View.GONE);
 		}
 		if (!_isDataEnabled.get()) {
 			removeBlanket = false;
 		} else {
 			// TODO: hide blanket data button
 		}
 		if (!_isPowerPreferenceEnabled.get()) {
 			removeBlanket = false;
 		} else {
 			// TODO: hide blanket power button
 			findViewById(R.id.turnOnBtn).setVisibility(View.GONE);
 		}
 		
 		if (removeBlanket) {
 			SBLog.i(TAG, "compose view successfully re-enabled");
 			hideComposeBlanket();
 			enableMapAndOverlay();
 		} else {
 			SBLog.i(TAG, "unable to enable compose view");
 		}
 	}
 	
 	private void disableComposeView() {
 		SBLog.i(TAG, "disableComposeView()");
 		showComposeBlanket();
 		disableMapAndOverlay();
 		
 		if (!_isLocationEnabled.get()) {
 			// TODO: show location blanket button
 			findViewById(R.id.enableLocationBtn).setVisibility(View.VISIBLE);
 			
 		}
 		if (!_isDataEnabled.get()) {
 			// TODO: show data blanket button
 		}
 		if (!_isPowerPreferenceEnabled.get()) {
 			// TODO: show power blanket button
 			findViewById(R.id.turnOnBtn).setVisibility(View.VISIBLE);
 		}		
 	}
 	
 	private boolean turnOn() {
 		SBLog.i(TAG, "turnOn()");
 		if (_isPowerPreferenceEnabled.get() && _isLocationEnabled.get() && _isDataEnabled.get()) {
 			if (!_isTurnedOn.get()) {
 				setPowerSwitchButtonToOn();
 				enableComposeView();
 				//_m.startPolling();
 				_isTurnedOn.set(true);
 			}
 			return true;
 		} else {
 			turnOff();
 			return false;
 		}
 	}
 	
 	private void turnOff() {
 		SBLog.i(TAG, "turnOff()");
 		setPowerSwitchButtonToOff();
 		disableComposeView();
 		//_m.stopPolling();
 		_isTurnedOn.set(false);
 	}
 	
 	private void setPowerSwitchButtonToOn() {
 		SBLog.i(TAG, "setPowerSwitchButtonToOn()");
 		_powerBtn.setImageResource(R.drawable.power_button_on);
 	}
 	
 	private void setPowerSwitchButtonToOff() {
 		SBLog.i(TAG, "setPowerSwitchButtonToOff()");
 		_powerBtn.setImageResource(R.drawable.power_button_off);
 	}
 	
 	public void showCompose() {
 		SBLog.i(TAG, "showCompose()");
 		_isComposeShowing.set(true);
 		_isInboxShowing.set(false);
 		_isProfileShowing.set(false);
 		_composeTabBtn.setImageResource(R.drawable.tab_shouting_on);
 		_inboxTabBtn.setImageResource(R.drawable.tab_inbox);
 		_profileTabBtn.setImageResource(R.drawable.tab_user);
 		_composeViewLl.setVisibility(View.VISIBLE);
 		_inboxViewLl.setVisibility(View.GONE);
 		_profileViewLl.setVisibility(View.GONE);
 	}
 	
 	private void showComposeBlanket() {
 		SBLog.i(TAG, "showComposeBlanket()");
 		findViewById(R.id.mapRl).setVisibility(View.GONE);
 		findViewById(R.id.inputRl).setVisibility(View.GONE);
 	}
 	
 	private void hideComposeBlanket() {
 		SBLog.i(TAG, "hideComposeBlanket()");
 		findViewById(R.id.mapRl).setVisibility(View.VISIBLE);
 		findViewById(R.id.inputRl).setVisibility(View.VISIBLE);
 		findViewById(R.id.enableLocationBtn).setVisibility(View.GONE);
 	}
 
 	public void showInbox() {
 		SBLog.i(TAG, "showInbox()");
 		_isComposeShowing.set(false);
 		_isInboxShowing.set(true);
 		_isProfileShowing.set(false);
 		_composeTabBtn.setImageResource(R.drawable.tab_shouting);
 		_inboxTabBtn.setImageResource(R.drawable.tab_inbox_on);
 		_profileTabBtn.setImageResource(R.drawable.tab_user);
 		_composeViewLl.setVisibility(View.GONE);
 		_inboxViewLl.setVisibility(View.VISIBLE);
 		_profileViewLl.setVisibility(View.GONE);
 	}
 
 	public void showProfile() {
 		SBLog.i(TAG, "showProfile()");
 		_isComposeShowing.set(false);
 		_isInboxShowing.set(false);
 		_isProfileShowing.set(true);
 		_composeTabBtn.setImageResource(R.drawable.tab_shouting);
 		_inboxTabBtn.setImageResource(R.drawable.tab_inbox);
 		_profileTabBtn.setImageResource(R.drawable.tab_user_on);
 		_composeViewLl.setVisibility(View.GONE);
 		_inboxViewLl.setVisibility(View.GONE);
 		_profileViewLl.setVisibility(View.VISIBLE);
 	}
 
 	private void enableMapAndOverlay() {
 		SBLog.i(TAG, "enableMapAndOverlay()");
 		if (_map.getOverlays().size() == 0) {
 			_map.getOverlays().add(overlay);
 		}
 		MapController mapController = _map.getController();
 		mapController.setZoom(C.DEFAULT_ZOOM_LEVEL);
 		_map.setClickable(true);
 		_map.setEnabled(true);
 		_map.setUserLocationOverlay(overlay);
 		_map.postInvalidate();
 		overlay.enableMyLocation();
 		overlay.runOnFirstFix(new Runnable() {
 			// may take some time if location provider was just enabled 
 			public void run() {
 				GeoPoint loc = overlay.getMyLocation();
 				MapController mapController = _map.getController();
 				mapController.animateTo(loc);
 			}
 		});
 	}
 	
 	private void disableMapAndOverlay() {
 		SBLog.i(TAG, "disableMapAndOverlay()");
 		if (_map != null) {
 			_map.setEnabled(false);
 			overlay.disableMyLocation();
 			// TODO: disable overlay
 		}
 	}
 	
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		menu.clear();
 	    MenuInflater inflater = getMenuInflater();
 	    if (_isComposeShowing.get()) {
 	    	inflater.inflate(R.menu.compose_menu, menu);
 	    } else if (_isInboxShowing.get()) {
 	    	inflater.inflate(R.menu.inbox_menu, menu);
 	    } else if (_isProfileShowing.get()) {
 	    	inflater.inflate(R.menu.profile_menu, menu);
 	    }
 	    return super.onPrepareOptionsMenu(menu);
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    switch (item.getItemId()) {
 	    case R.id.clear_notices:
 	        // TODO: clear notices
 	        return true;
 	    case R.id.empty_inbox:
 	        // TODO: empty inbox
 	        return true;
 	    default:
 	        return super.onOptionsItemSelected(item);
 	    }
 	}
 	
 	@Override
 	public void onDestroy() {
 		SBLog.i(TAG, "onDestroy()");
 		if (_m != null) {
 			_m.unregisterUI(false);
 			_m = null;
 		}
 		super.onDestroy();
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		SBLog.i(TAG, "isRouteDisplayed()");
 		return false;
 	}
 	
 	public int getMapHeight() {
 		SBLog.i(TAG, "getMapHeight()");
 		return _map.getMeasuredHeight();
 	}
 	
	public void setPeopleCountText(String text) {
		SBLog.i(TAG, "setPeopleCountText()");
		// TODO: semi transparent text box on map
		//_titleBarTv.setText(text);
	}

 	public void hideKeyboard() {
 		SBLog.i(TAG, "hideKeyboard()");
 		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 		imm.hideSoftInputFromWindow(shoutInputEt.getWindowToken(), 0);
 	}
 	
 	private OnClickListener _composeTabListener = new OnClickListener() {
 		public void onClick(View v) {
 			SBLog.i(TAG, "_composeTabListener.onClick()");
 			showCompose();
 		}
 	};
 
 	private OnClickListener _inboxTabListener = new OnClickListener() {
 		public void onClick(View v) {
 			SBLog.i(TAG, "_inboxTabListener.onClick()");
 			showInbox();
 		}
 	};
 
 	private OnClickListener _profileTabListener = new OnClickListener() {
 		public void onClick(View v) {
 			SBLog.i(TAG, "_profileTabListener.onClick()");
 			showProfile();
 		}
 	};
 
 	private OnClickListener _powerButtonListener = new OnClickListener() {
 		public void onClick(View v) {
 			SBLog.i(TAG, "_powerButtonListener.onClick()");
 			// only change the power preference when they press the on/off switch
 			if (!_isTurnedOn.get()) {
 				_m.setPowerPreferenceToOn();
 				if (!turnOn()) {
 					String text = "unable to turn on app, ";
 					if (!_isLocationEnabled.get() && !_isDataEnabled.get()) {
 						text += "location and data connection unavailable";
 					} else if (!_isLocationEnabled.get()) {
 						text += "location unavailable";
 					} else if (!_isDataEnabled.get()) {
 						text += "data unavailable";
 					}
 					Toast.makeText(Shoutbreak.this, text, Toast.LENGTH_SHORT).show();
 					SBLog.i(TAG, text);
 				}
 			} else {
 				_m.setPowerPreferenceToOff();
 				turnOff();
 			}
 		}
 	};
 
 	private OnClickListener _shoutButtonListener = new OnClickListener() {
 		public void onClick(View v) {
 			SBLog.i(TAG, "_shoutButtonListener.onClick()");
 			CharSequence text = shoutInputEt.getText();
 
 			if (text.length() == 0) {
 				Toast.makeText(Shoutbreak.this, "cannot shout blanks",
 						Toast.LENGTH_SHORT).show();
 			} else {
 				// TODO: filter all text going to server
 
 				shoutBtn.setImageResource(R.anim.shout_button_down);
 				AnimationDrawable shoutButtonAnimation = (AnimationDrawable) shoutBtn
 						.getDrawable();
 				shoutButtonAnimation.start();
 
 				_m.handleShoutStart(text.toString(), overlay.getCurrentPower());
 				hideKeyboard();
 			}
 		}
 	};
 	
 	private OnClickListener _enableLocationListener = new OnClickListener() {
 		public void onClick(View v) {
 			SBLog.i(TAG, "_enableLocationListener.onClick()");
 			Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);  
             startActivityForResult(intent, C.ACTIVITY_RESULT_LOCATION);
 		}
 	};
 	
 	private OnClickListener _turnOnListener = new OnClickListener() {
 		public void onClick(View v) {
 			SBLog.i(TAG, "_turnOnListener.onClick()");
 			_m.setPowerPreferenceToOn();
 		}
 	};
 	
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		switch (requestCode) {
 			case C.ACTIVITY_RESULT_LOCATION: {		// returning from location provider activity
 				if (_m.isLocationEnabled()) {		// update location status
 					_m.onLocationEnabled();
 				} else {
 					_m.onLocationDisabled();
 				}
 				break;
 			}
 		}
 	}
 	
 }
