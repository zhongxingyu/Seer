 // TODO: force repush
 
 package co.shoutbreak.core;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.graphics.drawable.AnimationDrawable;
 import android.os.Message;
 import android.view.View;
 import android.widget.AdapterView.OnItemClickListener;
 import co.shoutbreak.R;
 import co.shoutbreak.core.utils.DataListener;
 import co.shoutbreak.core.utils.DialogBuilder;
 import co.shoutbreak.core.utils.Flag;
 import co.shoutbreak.core.utils.Notifier;
 import co.shoutbreak.core.utils.SBLog;
 import co.shoutbreak.polling.CrossThreadPacket;
 import co.shoutbreak.polling.ThreadLauncher;
 import co.shoutbreak.storage.CellDensity;
 import co.shoutbreak.storage.Database;
 import co.shoutbreak.storage.DeviceInformation;
 import co.shoutbreak.storage.LocationTracker;
 import co.shoutbreak.storage.PreferenceManager;
 import co.shoutbreak.storage.Storage;
 import co.shoutbreak.storage.inbox.InboxListViewAdapter;
 import co.shoutbreak.storage.noticetab.NoticeTabListViewAdapter;
 import co.shoutbreak.ui.IUiGateway;
 import co.shoutbreak.ui.Shoutbreak;
 import co.shoutbreak.ui.UiOffGateway;
 
 public class Mediator {
 	
 	private static final String TAG = "Mediator";
 	
 	// colleagues
 	private ShoutbreakService _service;
 	private Database _db;
 	private Storage _storage;
 	private PreferenceManager _preferences;
 	private DeviceInformation _device;
 	private Notifier _notifier;
 	private LocationTracker _location;
 	private DataListener _data;
 	private ThreadLauncher _threadLauncher;
 	
 	private IUiGateway _uiGateway;
 	
 	// state flags
 	private Flag _isUIAlive = new Flag("m:_isUIAlive");
 	private Flag _isPollingAlive = new Flag("m:_isPollingAlive");
 	private Flag _isServiceConnected = new Flag("m:_isServiceConnected");
 	private Flag _isServiceStarted = new Flag("m:_isServiceStarted");
 	private Flag _isLocationEnabled = new Flag("m:_isLocationEnabled");
 	private Flag _isDataEnabled = new Flag("m:_isDataEnabled");
 	private Flag _isPowerPreferenceEnabled = new Flag("m:_isPowerPreferenceEnabled");		// is power preference set to on
 		
 	/* Mediator Lifecycle */
 	
 	public Mediator(ShoutbreakService service) {
     	SBLog.i(TAG, "new Mediator()");
     	
 		// add colleagues
 		_service = service;
 		_preferences = new PreferenceManager(this, _service.getSharedPreferences(C.PREFERENCE_FILE, Context.MODE_PRIVATE));
 		_device = new DeviceInformation(_service);
 		_notifier = new Notifier(this, _service);
 		_location = new LocationTracker(this);
 		_data = new DataListener(this);
 		_db = new Database(_service);
 		_storage = new Storage(this, _db);
 
 		_threadLauncher = new ThreadLauncher(this);
 		_uiGateway = new UiOffGateway();
 		
 		// Initialize State.
 		
 		// Polling has already been set and launched from DataListener calling onDataEnabled.
 		// Make sure we don't launch a second one.
 		if (!_isPollingAlive.isInitialized()) {
 			_isPollingAlive.set(false);
 		}
 		_isUIAlive.set(false);
 		
 		_isDataEnabled.set(isDataEnabled());
 		_isLocationEnabled.set(isLocationEnabled());
 		_isPowerPreferenceEnabled.set(isPowerPreferenceEnabled());
 		
 		if (_isDataEnabled.get()) {
 			onDataEnabled();
 		} else {
 			onDataDisabled();
 		}
 		
 		if (_isLocationEnabled.get()) {
 			onLocationEnabled();
 		} else {
 			onLocationDisabled();
 		}
 		
 		if (_isPowerPreferenceEnabled.get()) {
 			onPowerPreferenceEnabled();
 		} else {
 			onPowerPreferenceDisabled();
 		}
 	}
 	
 	public void registerUI(Shoutbreak ui) {
 		// ui is created before the mediator exists
 		// it must be added once the mediator is created
 		SBLog.i(TAG, "registerUI()");
 		_isUIAlive.set(true);
 		ui.setMediator(this);	
 		_uiGateway = new UiOnGateway(ui);
 		_storage.initializeUiComponents();
 		
 	}
 	
 	public void unregisterUI(boolean forceKillUI) {
 		// called by ui's onDestroy() method
 		SBLog.i(TAG, "unregisterUI()");
 		if (_isUIAlive.get()) {
 			_isUIAlive.set(false);
 			_uiGateway.unsetUiMediator();
 			if (forceKillUI) {
 				// forces UI to destroy itself if the mediator / service is killed off
 				SBLog.e(TAG, "force killed ui, service shutdown while ui running");
 				_uiGateway.finishUi();
 			}
 		} else {
 			SBLog.e(TAG, "ui is not alive, unable to unregister");
 		}
 		_uiGateway = new UiOffGateway();
 	}
 	
 	public void kill() {
 		// removes all colleague references to the mediator
 		// called by service's onDestroy() method
     	SBLog.i(TAG, "kill()");
 		_service = null;
 		unregisterUI(true);
 		_preferences = null;
 		_device = null;
 		_notifier = null;
 		_location.unsetMediator();
 		_location = null;
 		_data.unsetMediator();
 		_data = null;
 		_db = null;
 		_storage.unsetMediator();
 		_storage = null;
 		_threadLauncher.unsetMediator();
 		_threadLauncher = null;
 	}
 	
 	/* Mediator Commands */
 	
 	public void onServiceConnected() {
 		// called when service handler binds ui and service
 		SBLog.i(TAG, "onServiceConnected()");
 		_isServiceConnected.set(true);
 	}
 	
 	public void onServiceDisconnected() {
 		// called when ui unbinds from the service
 		// shouldn't ever be called
 		SBLog.i(TAG, "onServiceDisconnected()");
 		_isServiceConnected.set(false);
 	}
 	
 	public void onServiceStart() {
 		SBLog.i(TAG, "onServiceStart()");
 		_isServiceStarted.set(true);
 	}
 	
 	public void appLaunchedFromUI() {
 		SBLog.i(TAG, "appLaunchedFromUI()");
 		_isServiceStarted.set(true);
 		startPolling();
 	}
 	
 	public void appLaunchedFromAlarm() {
 		SBLog.i(TAG, "appLaunchedFromAlarm()");
 		_isServiceStarted.set(true);
 		startPolling();
 	}
 	
 	public void startPolling() {
 		SBLog.i(TAG, "startPolling()");
 		if (!_isPollingAlive.get()) {
 			if (_isPowerPreferenceEnabled.get() && _isLocationEnabled.get() && _isDataEnabled.get() && _isServiceStarted.get()) {
 				SBLog.i(TAG, "app fully functional");
 				_isPollingAlive.set(true);
 				_threadLauncher.startPolling();
 				_uiGateway.enableInputs();
 			} else {
 				if (!_isPowerPreferenceEnabled.get()) {
 					SBLog.e(TAG, "unable to start service, power preference set to off");
 				}
 				if (!_isLocationEnabled.get()) {
 					SBLog.e(TAG, "unable to start service, location unavailable");
 				}
 				if (!_isDataEnabled.get()) {
 					SBLog.e(TAG, "unable to start service, data unavailable");
 				}
 			}
 		} else {
 			SBLog.i(TAG, "service is already polling, unable to call startPolling()");
 		}
 	}
 	
 	public void stopPolling() {
 		SBLog.i(TAG, "stopPolling()");
 		if (_isPollingAlive.get()) {
 			_uiGateway.disableInputs();
 			_isPollingAlive.set(false);
 			_threadLauncher.stopPolling();
 		} else {
 			SBLog.i(TAG, "service is not polling, unable to call stopPolling()");
 		}
 	}
 	
 	// TODO: This should call something better than stopPolling()
 	private void possiblyStopPolling(Message message) {
 		if (message != null && message.obj != null) {
 			CrossThreadPacket xPacket = (CrossThreadPacket)message.obj;
 			if (xPacket.purpose == C.PURPOSE_LOOP_FROM_UI || xPacket.purpose == C.PURPOSE_LOOP_FROM_UI_DELAYED) {
 				// The Polling loop just crashed.
 				stopPolling();
 			}
 		} else {
 			// Something really bad happened
 			stopPolling();
 		}
 	}
 	
 	public void setPowerPreferenceToOn() {
 		_preferences.setPowerPreferenceToOn();
 	}
 	
 	public void setPowerPreferenceToOff() {
 		_preferences.setPowerPreferenceToOff();
 	}
 	
 	public boolean isPowerPreferenceEnabled() {
 		return _preferences.isPowerPreferenceSetToOn();
 	}
 	
 	public void onPowerPreferenceEnabled() {
 		SBLog.i(TAG, "onPowerEnabled()");
 		_isPowerPreferenceEnabled.set(true);
 		_service.enableAlarmReceiver();
 		if (_isUIAlive.get()) {
 			_uiGateway.onPowerPreferenceEnabled();
 		}
 		startPolling();
 	}
 	
 	public void onPowerPreferenceDisabled() {
 		SBLog.i(TAG, "onPowerDisabled()");
 		_isPowerPreferenceEnabled.set(false);
 		_service.disableAlarmReceiver();
 		if (_isUIAlive.get()) {
 			_uiGateway.onPowerPreferenceDisabled();
 		}
 		stopPolling();
 	}
 	
 	public boolean isLocationEnabled() {
 		return _location.isLocationEnabled();
 	}
 	
 	public void onLocationEnabled() {
 		SBLog.i(TAG, "onLocationEnabled()");
 		_isLocationEnabled.set(true);
 		if (_isUIAlive.get()) {
 			_uiGateway.onLocationEnabled();
 		}
 		_storage.initializeDensity(getCurrentCell());
 		startPolling();
 	}
 	
 	public void onLocationDisabled() {
 		SBLog.i(TAG, "onLocationDisabled()");
 		_isLocationEnabled.set(false);
 		if (_isUIAlive.get()) {
 			_uiGateway.onLocationDisabled();
 		}
 		stopPolling();
 	}
 	
 	public boolean isDataEnabled() {
 		return _data.isDataEnabled();
 	}
 	
 	public void onDataEnabled() {
 		SBLog.i(TAG, "onDataEnabled()");
 		_isDataEnabled.set(true);
 		if (_isUIAlive.get()) {
 			_uiGateway.onDataEnabled();
 		}
 		startPolling();
 	}
 	
 	public void onDataDisabled() {
 		SBLog.i(TAG, "onDataDisabled()");
 		_isDataEnabled.set(false);
 		if (_isUIAlive.get()) {
 			_uiGateway.onDataDisabled();
 		}
 		stopPolling();
 	}
 	
 	public boolean isFirstRun() {
 		SBLog.i(TAG, "isFirstRun()");
 		boolean isFirstRun = _preferences.getBoolean(C.PREFERENCE_IS_FIRST_RUN, true);
 		_preferences.putBoolean(C.PREFERENCE_IS_FIRST_RUN, false);
 		return isFirstRun;
 	}	
 	
 	public void checkLocationProviderStatus() {
 		SBLog.i(TAG, "checkLocationProviderStatus()");	
 		if (_location.isLocationEnabled()) {
 			onLocationEnabled();
 		} else {
 			onLocationDisabled();
 		}
 	}
 	
 	public Object getSystemService(String name) {
 		SBLog.i(TAG, "getSystemService()");
 		return _service.getSystemService(name);
 	}
 	
 	public CellDensity getCurrentCell() {
 		// TODO: should this be moved to ThreadSafeMediator?
 		SBLog.i(TAG, "getCurrentCell()");
 		if (!_isLocationEnabled.get()) {
 			SBLog.e(TAG, "location is unavailable, unable to get current cell");
 		}
 		return _location.getCurrentCell();
 	}
 	
 	public void deleteShout(String shoutId) {
 		SBLog.i(TAG, "deleteShout()");
 		_storage.deleteShout(shoutId);
 	}
 	
 	//public void launchPollingThread(Message message) {
 	//	SBLog.i(TAG, "launchPollingThread()");
 	//	_threadLauncher.launchPollingThread(message, false);
 	//}
 		
 	public ThreadSafeMediator getAThreadSafeMediator() {
 		SBLog.i(TAG, "getAThreadSafeMediator()");
 		return new ThreadSafeMediator();
 	}
 	
 	public void markAllNoticesRead() {
 		// TODO Auto-generated method stub
 		_storage.markAllNoticesRead();
 		_uiGateway.clearNoticeTab();
 		_storage.refreshNoticeTab();
 	}
 	
 	///////////////////////////////////////////////////////////////////////////
 	// HANDLE STUFF ///////////////////////////////////////////////////////////
 	///////////////////////////////////////////////////////////////////////////
 	
 	public void handlePollingResponse(Message message) {
 		SBLog.i(TAG, "handlePollingResponse()");
 		if (_isPollingAlive.get()) {
 			_threadLauncher.spawnNextPollingThread(message);
 		}
 		// Else polling thread dies.
 	}
 	
 	public void handleShoutStart(String text, int power) {
 		SBLog.i(TAG, "shout()");
 		_threadLauncher.handleShoutStart(text, power);
 	}
 	
 	// Triggered from a Shout close.  Have user save earned points.
 	public void handlePointsChange(int pointsType, int pointsValue) {
 		SBLog.i(TAG, "pointsChange()");
 		_storage.handlePointsChange(pointsType, pointsValue);
 		_uiGateway.handlePointsChange(_storage.getUserPoints());
 	}
 	
 	public void handeVoteStart(String shoutId, int vote) {
 		SBLog.i(TAG, "voteStart()");
 		_threadLauncher.handleVoteStart(shoutId, vote);
 	}
 	
 	///////////////////////////////////////////////////////////////////////////
 	///////////////////////////////////////////////////////////////////////////
 	///////////////////////////////////////////////////////////////////////////
 		
 	public void refreshUiComponents() {
 		_uiGateway.refreshUiComponents();
 	}
 	
 	public IUiGateway getUiGateway() {
 		return _uiGateway;
 	}
 	
 	// THREAD SAFE MEDIATOR ///////////////////////////////////////////////////
 	
 	public class ThreadSafeMediator {
 		// Methods of any other classes called from here should be synchronized or read only.
 		
 		public ThreadSafeMediator() {
 			SBLog.i(TAG, "new ThreadSafeMediator()");
 		}
 		
 		///////////////////////////////////////////////////////////////////////////
 		// HANDLE STUFF ///////////////////////////////////////////////////////////
 		///////////////////////////////////////////////////////////////////////////
 		
 		public void handleCreateAccountStarted() {
 			_storage.handleCreateAccountStarted();			
 		}
 		
 		public void handleCreateAccountFailed(Message message) {
 			_storage.handleCreateAccountFailed();
 			_uiGateway.handleCreateAccountFailed();
 			possiblyStopPolling(message);
 		}
 		
 		public void handleShoutsReceived(JSONArray shouts) {
 			SBLog.i(TAG, "shoutsReceived()");
 			_storage.handleShoutsReceived(shouts);			
 			if (!_isUIAlive.get()) {
 				_notifier.handleShoutsReceived(shouts.length());				
 			}
 		}
 		
 		public void handleDensityChange(double density) {
 			SBLog.i(TAG, "densityChange()");
 			// Note: The order in these matters.
 			_storage.handleDensityChange(density, getCurrentCell());
 			_uiGateway.handleDensityChange(density, _storage.getUserLevel());
 		}
 		
 		public void handleScoresReceived(JSONArray scores) {
 			SBLog.i(TAG, "scoresReceived()");
 			_storage.handleScoresReceived(scores);
 		}
 		
 		public void handleLevelUp(JSONObject levelInfo) {
 			try {
 				int newLevel = (int) levelInfo.getLong(C.JSON_LEVEL);
 				int newPoints = (int) levelInfo.getLong(C.JSON_POINTS);
 				int nextLevelAt = (int) levelInfo.getLong(C.JSON_NEXT_LEVEL_AT);
 				_storage.handleLevelUp(newLevel, newPoints, nextLevelAt);
 				_uiGateway.handleLevelUp(_storage.getCellDensity(getCurrentCell()).density, _storage.getUserLevel());
 				_uiGateway.handlePointsChange(_storage.getUserPoints());
 			} catch (JSONException e) {
 				SBLog.e(TAG, e.getMessage());
 			}
 			SBLog.i(TAG, "levelUp()");
 		}
 		
 		public void handleShoutSent() {
 			SBLog.i(TAG, "shoutSent()");
 			_storage.handleShoutSent();
 			_uiGateway.handleShoutSent();
 		}
 		
 		public void handleShoutFailed(Message message) {
 			_storage.handleShoutFailed();
 			_uiGateway.handleShoutFailed();
 			possiblyStopPolling(message);
 		}
 		
 
 		public void handleVoteFinish(String shoutId, int vote) {
 			SBLog.i(TAG, "voteFinish()");
 			_storage.handleVoteFinish(shoutId, vote);
 		}
 		
 		public void handleVoteFailed(Message message, String shoutId, int vote) {
 			_storage.handleVoteFailed(shoutId, vote);
 			_uiGateway.handleServerFailure();
 			possiblyStopPolling(message);
 		}
 		
 		public void handleAccountCreated(String uid, String password) {
 			SBLog.i(TAG, "accountCreated()");
 			_storage.handleAccountCreated(uid, password);
 			// Maybe we should do something in the UI?
 		}
 		
 		public void handlePingFailed(Message message) {
 			// This has proven to be really damn annoying since every once in a while network access dissappears.
 			// Users will think our app sucks.
 			// For now let's just ignore ping failure.
 			//createNotice(C.NOTICE_PING_FAILED, C.STRING_PING_FAILED, null);
 			_uiGateway.handleServerFailure();
 			possiblyStopPolling(message);
 		}
 		
 		///////////////////////////////////////////////////////////////////////////
 		///////////////////////////////////////////////////////////////////////////
 		///////////////////////////////////////////////////////////////////////////
 			
 		public boolean userHasAccount() {
 			SBLog.i(TAG, "userHasAccount()");
 			return _storage.getUserHasAccount();
 		}
 		
 		public String getUserId() {
 			SBLog.i(TAG, "getUserId()");
 			return _storage.getUserId();
 		}
 		
 		public ArrayList<String> getOpenShoutIds() {
 			SBLog.i(TAG, "getOpenShoutIds()");
 			return _storage.getOpenShoutIDs();
 		}
 		
 		public String getAuth() {
 			SBLog.i(TAG, "getAuth()");
 			return _storage.getUserAuth();
 		}
 		
 		public CellDensity getCellDensity() {
 			SBLog.i(TAG, "getCellDensity()");
 			return _storage.getCellDensity(getCurrentCell());
 		}
 		
 		public double getLongitude() {
 			SBLog.i(TAG, "getLongitude()");
 			return _location.getLongitude();
 		}
 		
 		public double getLatitude() {
 			SBLog.i(TAG, "getLatitude()");
 			return _location.getLatitude();
 		}
 		
 		public boolean getUserLevelUpOccurred() {
 			SBLog.i(TAG, "getLevelUpOccurred()");
 			return _storage.getLevelUpOccured();
 		}
 		
 		public int getUserLevel() {
 			SBLog.i(TAG, "getLevel()");
 			return _storage.getUserLevel();
 		}
 		
 		public void updateAuth(String nonce) {
 			SBLog.i(TAG, "updateAuth()");
 			_storage.updateAuth(nonce);
 		}
 		
 		public final String getAndroidId() {
 			SBLog.i(TAG, "getAndroidId()");
 			return _device.getAndroidId();
 		}
 		
 		public final String getDeviceId() {
 			SBLog.i(TAG, "getDeviceId()");
 			return _device.getDeviceId();
 		}
 		
 		public final String getPhoneNumber() {
 			SBLog.i(TAG, "getPhoneNumber()");
 			return _device.getPhoneNumber();
 		}
 		
 		public final String getNetworkOperator() {
 			SBLog.i(TAG, "getNetworkOperator()");
 			return _device.getNetworkOperator();
 		}
 
 	}
 	
 	// UI GATEWAY /////////////////////////////////////////////////////////////
 	
 	private class UiOnGateway implements IUiGateway {
 		
 		private Shoutbreak _ui;
 		
 		public UiOnGateway(Shoutbreak ui) {
 			_ui = ui;
 		}
 		
 		///////////////////////////////////////////////////////////////////////////
 		// HANDLE STUFF ///////////////////////////////////////////////////////////
 		///////////////////////////////////////////////////////////////////////////
 		
 		public void handleCreateAccountFailed() {
 			this.handleServerFailure();			
 		}
 
 		public void handleShoutSent() {
 			SBLog.i(TAG, "handleShoutSent()");
 			AnimationDrawable shoutButtonAnimation = (AnimationDrawable) _ui.shoutBtn.getDrawable();
 			shoutButtonAnimation.stop();
 			_ui.shoutBtn.setImageResource(R.drawable.shout_button_up);
 			_ui.shoutInputEt.setText("");
 		}
 		
 		public void handleShoutFailed() {
 			SBLog.i(TAG, "handleShoutFailed()");
 			AnimationDrawable shoutButtonAnimation = (AnimationDrawable) _ui.shoutBtn.getDrawable();
 			shoutButtonAnimation.stop();
 			_ui.shoutBtn.setImageResource(R.drawable.shout_button_up);
 			this.handleServerFailure();
 		}
 		
 		public void handleDensityChange(double newDensity, int level) {
 			SBLog.i(TAG, "handleDensityChange()");
 			_ui.overlay.handleDensityChange(newDensity, level);
 		}
 
 		public void handleLevelUp(double cellDensity, int newLevel) {
 			SBLog.i(TAG, "handleLevelUp()");
 			_ui.overlay.handleLevelUp(cellDensity, newLevel);
 			_uiGateway.refreshProfile(_storage.getUserLevel(), _storage.getUserPoints(), _storage.getUserNextLevelAt());
 		}
 
 		public void handlePointsChange(int newPoints) {
 			SBLog.i(TAG, "handlePointsChange()");
 			_uiGateway.refreshProfile(_storage.getUserLevel(), _storage.getUserPoints(), _storage.getUserNextLevelAt());
 		}
 		
 		public void handleServerFailure() {
 			_ui.dialogBuilder.showDialog(DialogBuilder.DIALOG_SERVER_DOWN);
 		}
 				
 		///////////////////////////////////////////////////////////////////////////
 		///////////////////////////////////////////////////////////////////////////
 		///////////////////////////////////////////////////////////////////////////		
 		
 		public void refreshUiComponents() {	
 			// TODO: make inbox & profile more like noticeTabSystem
 			_storage.refreshUiComponents();
 			this.refreshProfile(_storage.getUserLevel(), _storage.getUserPoints(), _storage.getUserNextLevelAt());
 		}
 		
 		public void refreshProfile(int level, int points, int nextLevelAt) {
 			SBLog.i(TAG, "refreshProfile()");
 			_ui.levelTv.setText("Your level: " + level);
 			_ui.pointsTv.setText("Points earned: " + points);
 			_ui.nextLevelAtTv.setText("Next level at: " + nextLevelAt);
 			//_progressPb.setMax(100);
 			_ui.progressPb.setProgress(50);
 			if (nextLevelAt > 0) {
 				_ui.progressPb.setProgress(points / nextLevelAt);
 			}			
 		}
 		
 		public void enableInputs() {
 			_ui.shoutBtn.setEnabled(true);
 			_ui.shoutInputEt.setEnabled(true);
 			_storage.enableInputs();
 		}
 		
 		
 		public void disableInputs() {
 			_ui.shoutBtn.setEnabled(false);
 			_ui.shoutInputEt.setEnabled(false);
 			_storage.disableInputs();
 		}
 
 		@Override
 		public void finishUi() {
 			_ui.finish();			
 		}
 
 		@Override
 		public void onDataDisabled() {
 			_ui.onDataDisabled();			
 		}
 
 		@Override
 		public void onDataEnabled() {
 			_ui.onDataEnabled();			
 		}
 
 		@Override
 		public void onLocationDisabled() {
 			_ui.onLocationDisabled();			
 		}
 
 		@Override
 		public void onLocationEnabled() {
 			_ui.onLocationEnabled();			
 		}
 
 		@Override
 		public void onPowerPreferenceDisabled() {
 			_ui.onPowerPreferenceDisabled();			
 		}
 
 		@Override
 		public void onPowerPreferenceEnabled() {
 			_ui.onPowerPreferenceEnabled();
 			
 		}
 
 		@Override
 		public void unsetUiMediator() {
 			_ui.unsetMediator();			
 		}
 
 		@Override
 		public void clearNoticeTab() {
 			_ui.noticeTabPointsTv.setVisibility(View.INVISIBLE);
 			_ui.noticeTabShoutsTv.setVisibility(View.INVISIBLE);
 			_ui.noticeTabPointsIv.setVisibility(View.INVISIBLE);
 			_ui.noticeTabShoutsIv.setVisibility(View.INVISIBLE);			
 		}
 		
 		
 
 		@Override
 		public void showPointsNotice(String noticeText) {
 			_ui.noticeTabPointsIv.setVisibility(View.VISIBLE);
 			_ui.noticeTabPointsTv.setVisibility(View.VISIBLE);
 			_ui.noticeTabPointsTv.setText(noticeText);
 		}
 
 		@Override
 		public void showShoutNotice(String noticeText) {
 			_ui.noticeTabShoutsIv.setVisibility(View.VISIBLE);
 			_ui.noticeTabShoutsTv.setVisibility(View.VISIBLE);
 			_ui.noticeTabShoutsTv.setText(noticeText);			
 		}
 
 		@Override
 		public void setupNoticeTabListView(NoticeTabListViewAdapter listAdapter, boolean itemsCanFocus,
 				OnItemClickListener listViewItemClickListener) {
 			_ui.noticeTabListView.setAdapter(listAdapter);
 			_ui.noticeTabListView.setItemsCanFocus(itemsCanFocus);
 			_ui.noticeTabListView.setOnItemClickListener(listViewItemClickListener);			
 		}
 		
 		@Override
 		public void setupInboxListView(InboxListViewAdapter listAdapter, boolean itemsCanFocus,
 				OnItemClickListener inboxItemClickListener) {
 			_ui.inboxListView.setAdapter(listAdapter);
 			_ui.inboxListView.setItemsCanFocus(itemsCanFocus);
 			_ui.inboxListView.setOnItemClickListener(inboxItemClickListener);		
 		}
 
 		@Override
 		public void showTopNotice() {
			_ui.noticeTabListView.setSelection(0);
 			_ui.noticeTab.showOneLine();
 		}
 
 		@Override
 		public void jumpToShoutInInbox(String shoutId) {
 			// TODO Auto-generated method stub
 			_ui.showInbox();
 			_ui.noticeTab.hide();
 			_storage.jumpToShoutInInbox(shoutId);
 		}
 
 	}
 	
 	
 	
 }
