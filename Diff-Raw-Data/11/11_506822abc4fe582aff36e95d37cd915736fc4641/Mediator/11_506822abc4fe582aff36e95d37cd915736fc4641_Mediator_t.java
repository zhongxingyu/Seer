 // TODO: force repush
 
 package co.shoutbreak.core;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.google.android.maps.GeoPoint;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.graphics.drawable.AnimationDrawable;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.net.ConnectivityManager;
 import android.os.Message;
 import android.os.SystemClock;
 import android.text.InputFilter;
 import android.view.View;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 import co.shoutbreak.R;
 import co.shoutbreak.core.utils.DataChangeReceiver;
 import co.shoutbreak.core.utils.DataChangeListener;
 import co.shoutbreak.core.utils.DialogBuilder;
 import co.shoutbreak.core.utils.Flag;
 import co.shoutbreak.core.utils.Notifier;
 import co.shoutbreak.core.utils.SBLog;
 import co.shoutbreak.polling.CrossThreadPacket;
 import co.shoutbreak.polling.PollingAlgorithm;
 import co.shoutbreak.polling.ThreadLauncher;
 import co.shoutbreak.storage.Database;
 import co.shoutbreak.storage.DeviceInformation;
 import co.shoutbreak.storage.LocationTracker;
 import co.shoutbreak.storage.PreferenceManager;
 import co.shoutbreak.storage.RadiusCacheCell;
 import co.shoutbreak.storage.Storage;
 import co.shoutbreak.storage.User;
 import co.shoutbreak.storage.inbox.InboxListViewAdapter;
 import co.shoutbreak.storage.inbox.InboxViewHolder;
 import co.shoutbreak.storage.noticetab.MultiDirectionSlidingDrawer;
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
 	private DataChangeListener _dataListener;
 	private ThreadLauncher _threadLauncher;
 	private PollingAlgorithm _pollingAlgorithm;
 
 	private PendingIntent _intervalAlarmIntent;
 	private IUiGateway _uiGateway;
 	
 	private DataChangeReceiver _connectivityReceiver;
 	
 	// state flags
 	private Flag _isUIAlive = new Flag("m:_isUIAlive");
 	private Flag _isUiInForeground = new Flag("m:_isUIInForeground");
 	private Flag _isPollingAlive = new Flag("m:_isPollingAlive");
 	private Flag _isServiceConnected = new Flag("m:_isServiceConnected");
 	private Flag _isServiceStarted = new Flag("m:_isServiceStarted");
 	private Flag _isLocationEnabled = new Flag("m:_isLocationEnabled");
 	private Flag _isDataEnabled = new Flag("m:_isDataEnabled");
 	private Flag _isPowerPreferenceEnabled = new Flag("m:_isPowerPreferenceEnabled"); // is power preference set to on
 
 	/* Mediator Lifecycle */
 
 	public Mediator(ShoutbreakService service) {
 		SBLog.lifecycle(TAG, "Mediator()");
 		SBLog.constructor(TAG);
 
 		// add colleagues
 		_service = service;
 		_preferences = new PreferenceManager(Mediator.this, _service.getSharedPreferences(C.PREFERENCE_FILE, Context.MODE_PRIVATE));
 		_device = new DeviceInformation(_service);
 		_notifier = new Notifier(Mediator.this, _service);
 		_location = new LocationTracker(Mediator.this);
 		_dataListener = new DataChangeListener(Mediator.this);
 		_db = new Database(_service);
 		_storage = new Storage(Mediator.this, _db);
 		_pollingAlgorithm = new PollingAlgorithm();
 
 		_connectivityReceiver = new DataChangeReceiver(_dataListener);
 		_service.registerReceiver(_connectivityReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
 		_threadLauncher = new ThreadLauncher(Mediator.this);
 		_uiGateway = new UiOffGateway();
 
 		// Initialize State.
 
 		// Polling has already been set and launched from DataListener calling
 		// onDataEnabled.
 		// Make sure we don't launch a second one.
 		if (!_isPollingAlive.isInitialized()) {
 			_isPollingAlive.set(false);
 		}
 		_isUIAlive.set(false);
 		_isUiInForeground.set(false);
 
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
 			onPowerPreferenceEnabled(true);
 		} else {
 			onPowerPreferenceDisabled(true);
 		}
 	}
 
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	// LIFECYCLE ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 	
 	private void enableIntervalAlarm() {
 		Intent alarmIntent = new Intent(_service, ShoutbreakService.class);
 		alarmIntent.putExtra(C.NOTIFICATION_LAUNCHED_FROM_ALARM, true);
 		alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		_intervalAlarmIntent = PendingIntent.getService(_service, 0, alarmIntent, 0);
 		AlarmManager am = (AlarmManager) _service.getSystemService(Service.ALARM_SERVICE);
 		am.cancel(_intervalAlarmIntent);
 		am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_FIFTEEN_MINUTES,
 				AlarmManager.INTERVAL_FIFTEEN_MINUTES, _intervalAlarmIntent);
 	}
 
 	private void disableIntervalAlarm() {
 		if (_intervalAlarmIntent != null) {
 			AlarmManager am = (AlarmManager) _service.getSystemService(Service.ALARM_SERVICE);
 			am.cancel(_intervalAlarmIntent);
 		}
 	}
 
 	public void registerUI(Shoutbreak ui) {
 		// ui is created before the mediator exists
 		// it must be added once the mediator is created
 		SBLog.lifecycle(TAG, "registerUI()");
 		_isUIAlive.set(true);
 		_isUiInForeground.set(true);
 		ui.setMediator(Mediator.this);
 		_uiGateway = new UiOnGateway(ui);
 		_storage.initializeUiComponents();
 
 	}
 
 	public void unregisterUI(boolean forceKillUI) {
 		// called by ui's onDestroy() method
 		SBLog.lifecycle(TAG, "unregisterUI()");
 		if (_isUIAlive.get()) {
 			_isUIAlive.set(false);
 			_uiGateway.unsetUiMediator();
 			if (forceKillUI) {
 				// forces UI to destroy itself if the mediator / service is killed off
 				SBLog.error(TAG, "force killed ui, service shutdown while ui running");
 				_uiGateway.finishUi();
 			}
 		} else {
 			SBLog.error(TAG, "ui is not alive, unable to unregister");
 		}
 		_uiGateway = new UiOffGateway();
 	}
 
 	public void kill() {
 		// removes all colleague references to the mediator
 		// called by service's onDestroy() method
 		SBLog.lifecycle(TAG, "kill()");
 		_service.unregisterReceiver(_connectivityReceiver);
 		_threadLauncher.stopLaunchingPollingThreads();
 		_service = null;
 		unregisterUI(true);
 		_preferences = null;
 		_device = null;
 		_notifier = null;
 		_location.unsetMediator();
 		_location = null;
 		_dataListener.unsetMediator();
 		_dataListener = null;
 		_db = null;
 		_storage.unsetMediator();
 		_storage = null;
 		_threadLauncher.unsetMediator();
 		_threadLauncher = null;
 	}
 
 	/* Mediator Commands */
 
 	public void onServiceConnected() {
 		// called when service handler binds ui and service
 		SBLog.lifecycle(TAG, "onServiceConnected()");
 		_isServiceConnected.set(true);
 	}
 
 	public void onServiceDisconnected() {
 		// called when ui unbinds from the service
 		// shouldn't ever be called
 		SBLog.lifecycle(TAG, "onServiceDisconnected()");
 		_isServiceConnected.set(false);
 	}
 
 	public void onServiceStart() {
 		SBLog.lifecycle(TAG, "onServiceStart()");
 		_isServiceStarted.set(true);
 	}
 
 	public void appLaunchedFromUI() {
 		SBLog.lifecycle(TAG, "appLaunchedFromUI()");
 		_isServiceStarted.set(true);
 		startPolling(true);
 	}
 
 	public void appLaunchedFromAlarm() {
 		SBLog.lifecycle(TAG, "appLaunchedFromAlarm()");
 		_isServiceStarted.set(true);
 		startPolling(true);
 	}
 
 	public void setIsUiInForeground(boolean isUiInForeground) {
 		_isUiInForeground.set(isUiInForeground);
 	}
 
 	public boolean getIsUiInForeground() {
 		return _isUiInForeground.get();
 	}
 	
 	public void clearNotifications() {
 		_notifier.clearNotifications();
 	}
 
 	public void startPolling(boolean onUiThread) {
 		SBLog.lifecycle(TAG, "startPolling()");
 		if (!_isPollingAlive.get()) {
 			if (_isPowerPreferenceEnabled.get() && _isLocationEnabled.get() && _isDataEnabled.get() && _isServiceStarted.get()) {
 				SBLog.logic("startPolling - app fully functional");
 				_isPollingAlive.set(true);
 				_threadLauncher.startPolling();
 				if (onUiThread) {
 					_uiGateway.enableInputs();
 				}
 			} else {
 				if (!_isPowerPreferenceEnabled.get()) {
 					SBLog.error(TAG, "unable to start service because power preference is set to off");
 				}
 				if (!_isLocationEnabled.get()) {
 					SBLog.error(TAG, "unable to start service because location is unavailable");
 				}
 				if (!_isDataEnabled.get()) {
 					SBLog.error(TAG, "unable to start service because data unavailable");
 				}
 			}
 		} else {
 			SBLog.logic("startPolling - service is already polling, unable to call startPolling()");
 		}
 	}
 
 	public void stopPolling() {
 		SBLog.lifecycle(TAG, "stopPolling()");
 		if (_isPollingAlive.get()) {
 			// This must be called before setPowerPreferenceToOff or infinite loop occurs.
 			_isPollingAlive.set(false);
 			_threadLauncher.stopLaunchingPollingThreads();
 		} else {
 			SBLog.logic("stopPolling was called, but isPollingAlive is already false.");
 		}
 	}
 
 	// TODO: This should call something better than stopPolling()
 	private void possiblyStopPolling(Message message) {
 		SBLog.logic("possiblyStopPolling()");
 		if (message != null && message.obj != null) {
 			CrossThreadPacket xPacket = (CrossThreadPacket) message.obj;
 			if (xPacket.purpose == C.PURPOSE_LOOP_FROM_UI || xPacket.purpose == C.PURPOSE_LOOP_FROM_UI_DELAYED) {
 				if (PollingAlgorithm.isDropCountAtLimit()) {
 					// The Polling loop just crashed.
 					_storage.handleForcedPollingStop();
 					setPowerPreferenceToOff(true);
 				}
 			}
 		} else {
 			// Something really bad happened
 			setPowerPreferenceToOff(true);
 		}
 	}
 
 	public void resetPollingToNow() {
 		_threadLauncher.resetPollingToNow();
 	}
 	
 	public long getPollingDelay() {
 		return _pollingAlgorithm.getPollingDelay(Mediator.this);
 	}
 
 	public void setPowerPreferenceToOn(boolean onUiThread) {
 		_preferences.setPowerPreferenceToOn(onUiThread);
 	}
 
 	public void setPowerPreferenceToOff(boolean onUiThread) {
 		_preferences.setPowerPreferenceToOff(onUiThread);
 	}
 
 	public boolean isPowerPreferenceEnabled() {
 		return _preferences.isPowerPreferenceSetToOn();
 	}
 
 	public void onPowerPreferenceEnabled(boolean onUiThread) {
 		SBLog.method(TAG, "onPowerEnabled()");
 		_isPowerPreferenceEnabled.set(true);
 		enableIntervalAlarm();
 		_service.enableOnBootAlarmReceiver();
 		if (_isUIAlive.get()) {
 			_uiGateway.onPowerPreferenceEnabled(onUiThread);
 		}
 		startPolling(onUiThread);
 	}
 
 	public void onPowerPreferenceDisabled(boolean onUiThread) {
 		SBLog.method(TAG, "onPowerDisabled()");
 		_isPowerPreferenceEnabled.set(false);
 		disableIntervalAlarm();
 		_service.disableOnBootAlarmReceiver();
 		if (_isUIAlive.get()) {
 			_uiGateway.onPowerPreferenceDisabled(onUiThread);
 		}
 		stopPolling();
 	}
 
 	public boolean isLocationEnabled() {
 		return _location.isLocationEnabled();
 	}
 
 	public void onLocationEnabled() {
 		SBLog.method(TAG, "onLocationEnabled()");
 		_isLocationEnabled.set(true);
 		if (_isUIAlive.get()) {
 			_uiGateway.onLocationEnabled();
 		}
 		_storage.initializeRadiusAtCell(getCurrentCell());
 		startPolling(true);
 	}
 
 	public void onLocationDisabled() {
 		SBLog.method(TAG, "onLocationDisabled()");
 		_isLocationEnabled.set(false);
 		if (_isUIAlive.get()) {
 			_uiGateway.onLocationDisabled();
 		}
 		stopPolling();
 	}
 
 	public boolean isDataEnabled() {
 		return _dataListener.isDataEnabled();
 	}
 
 	public void onDataEnabled() {
 		SBLog.method(TAG, "onDataEnabled()");
 		_isDataEnabled.set(true);
 		if (_isUIAlive.get()) {
 			_uiGateway.onDataEnabled();
 		}
 		_location.refreshBestProvider();
 		startPolling(true);
 	}
 
 	public void onDataDisabled() {
 		SBLog.method(TAG, "onDataDisabled()");
 		_isDataEnabled.set(false);
 		if (_isUIAlive.get()) {
 			_uiGateway.onDataDisabled();
 		}
 		stopPolling();
 	}
 
 	public boolean isFirstRun() {
 		SBLog.method(TAG, "isFirstRun()");
 		boolean isFirstRun = _preferences.getBoolean(C.PREFERENCE_IS_FIRST_RUN, true);
 		_preferences.putBoolean(C.PREFERENCE_IS_FIRST_RUN, false);
 		return isFirstRun;
 	}
 
 	public void checkLocationProviderStatus() {
 		SBLog.method(TAG, "checkLocationProviderStatus()");
 		if (_location.isLocationEnabled()) {
 			onLocationEnabled();
 		} else {
 			onLocationDisabled();
 		}
 	}
 
 	public Object getSystemService(String name) {
 		return _service.getSystemService(name);
 	}
 
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	// LOGIC STUFF /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 	
 	public RadiusCacheCell getCurrentCell() {
 		// TODO: should this be moved to ThreadSafeMediator?
 		SBLog.method(TAG, "getCurrentCell()");
 		if (!_isLocationEnabled.get()) {
 			SBLog.error(TAG, "location is unavailable, unable to get current cell");
 		}
		RadiusCacheCell currentCell = _location.getCurrentCell();
		currentCell.level = _storage.getUserLevel();
		return currentCell;
 	}
 
 	public void deleteShout(String shoutId) {
 		SBLog.method(TAG, "deleteShout()");
 		_uiGateway.toast("Shout deleted.", Toast.LENGTH_SHORT);
 		_storage.deleteShout(shoutId);
 	}
 
 	public ThreadSafeMediator getAThreadSafeMediator() {
 		SBLog.method(TAG, "getAThreadSafeMediator()");
 		return new ThreadSafeMediator();
 	}
 
 	public void markAllNoticesRead() {
 		// TODO Auto-generated method stub
 		_storage.markAllNoticesRead();
 		_uiGateway.clearNoticeTab();
 		_storage.refreshNoticeTab();
 	}
 	
 	public void handlePollingResponse(Message message) {
 		SBLog.method(TAG, "handlePollingResponse()");
 		if (_isPollingAlive.get()) {
 			_threadLauncher.spawnNextPollingThread(message);
 		}
 		// Else polling thread dies.
 	}
 
 	public void handleShoutStart(String text, int power, String replyingTo) {
 		SBLog.method(TAG, "shout()");
 		_threadLauncher.handleShoutStart(text, power, _storage.getRadiusAtCell(getCurrentCell()).radius, replyingTo);
 	}
 
 	// Triggered from a Shout close. Have user save earned points.
 	public void handlePointsForShout(int pointsType, int pointsValue, String shoutId) {
 		SBLog.method(TAG, "pointsChange()");
 		_storage.handlePointsForShout(pointsType, pointsValue, shoutId);
 		_uiGateway.handlePointsChange(_storage.getUserPoints());
 	}
 
 	public void handeVoteStart(String shoutId, int vote) {
 		SBLog.method(TAG, "voteStart()");
 		_threadLauncher.handleVoteStart(shoutId, vote);
 	}
 	
 	public String getSignature() {
 		return _storage.getSignature();
 	}
 	
 	public boolean getIsSignatureEnabled() {
 		return _storage.getIsSignatureEnabled();
 	}
 
 	public void saveUserSignature(String sigText, boolean sigEnabled) {
 		// Don't change checkbox here or event loop will occur.
 		_preferences.putBoolean(C.PREFERENCE_SIGNATURE_ENABLED, sigEnabled);
 		_preferences.setString(C.PREFERENCE_SIGNATURE_TEXT, sigText);
 		_uiGateway.refreshSignature(sigText, sigEnabled);
 		_uiGateway.toast(_service.getString(R.string.sigSaved), Toast.LENGTH_LONG);
 	}
 	
 	public void refreshUiComponents(MultiDirectionSlidingDrawer noticeTabSlidingDrawer) {
 		noticeTabSlidingDrawer.setMediator(Mediator.this);
 		_uiGateway.refreshUiComponents();
 	}
 
 	public IUiGateway getUiGateway() {
 		return _uiGateway;
 	}
 
 	public void createReplyDialog(Shout shout) {
 		_uiGateway.createReplyDialog(shout);
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	// THREAD SAFE MEDIATOR ///////////////////////////////////////////////////
 
 	public class ThreadSafeMediator {
 		// Methods of any other classes called from here should be synchronized or
 		// read only.
 
 		public ThreadSafeMediator() {
 			SBLog.method(TAG, "ThreadSafeMediator()");
 		}
 
 		// /////////////////////////////////////////////////////////////////////////
 		// HANDLE STUFF ///////////////////////////////////////////////////////////
 		// /////////////////////////////////////////////////////////////////////////
 
 		public void handleCreateAccountStarted() {
 			_storage.handleCreateAccountStarted();
 		}
 
 		public void handleCreateAccountFailed(Message message) {
 			_storage.handleCreateAccountFailed();
 			_uiGateway.handleCreateAccountFailed();
 			possiblyStopPolling(message);
 		}
 
 		public void handleShoutsReceived(JSONArray shouts) {
 			SBLog.method(TAG, "shoutsReceived()");
 			_storage.handleShoutsReceived(shouts);
 			if (!_isUiInForeground.get()) {
 				_notifier.handleShoutsReceived(shouts.length());
 			}
 		}
 
 		public void handleRadiusChange(long radius) {
 			SBLog.method(TAG, "densityChange()");
 			// Note: The order in these matters.
 			_storage.handleRadiusChange(radius, getCurrentCell());
 			_uiGateway.handleRadiusChange(true, radius, _storage.getUserLevel());
 		}
 
 		public void handleScoresReceived(JSONArray scores) {
 			SBLog.method(TAG, "scoresReceived()");
 			_storage.handleScoresReceived(scores);
 		}
 
 		public void handleLevelUp(JSONObject levelInfo) {
 			SBLog.method(TAG, "handleLevelUp()");
 			try {
 				int newLevel = (int) levelInfo.getLong(C.JSON_LEVEL);
 				int levelAt = (int) levelInfo.getLong(C.JSON_LEVEL_AT);
 				int nextLevelAt = (int) levelInfo.getLong(C.JSON_NEXT_LEVEL_AT);
 				_storage.handleLevelUp(newLevel, levelAt, nextLevelAt);
 				_uiGateway.handleLevelUp(_storage.getRadiusAtCell(getCurrentCell()).radius, _storage.getUserLevel());
 			} catch (JSONException e) {
 				SBLog.error(TAG, e.getMessage());
 			}
 		}
 		
 		public void handlePointsSync(int currentPoints) {
 			_storage.handlePointsSync(currentPoints);
 			_uiGateway.handlePointsChange(_storage.getUserPoints());
 		}
 
 		public void handleShoutSent() {
 			SBLog.method(TAG, "handleShoutSent()");
 			_storage.handleShoutSent();
 			_uiGateway.handleShoutSent();
 			resetPollingToNow();
 		}
 
 		public void handleShoutFailed(Message message) {
 			_storage.handleShoutFailed();
 			_uiGateway.handleShoutFailed();
 			possiblyStopPolling(message);
 		}
 
 		public void handleVoteFinish(String shoutId, int vote) {
 			SBLog.method(TAG, "handleVoteFinish()");
 //			if (vote == 1) {
 //				_uiGateway.toast("You helped the sender get louder.", Toast.LENGTH_LONG);
 //			} else if (vote == -1) {
 //				_uiGateway.toast("You made the sender quieter.", Toast.LENGTH_LONG);
 //			}
 			_storage.handleVoteFinish(shoutId, vote);
 			_uiGateway.handlePointsChange(_storage.getUserPoints());
 		}
 
 		public void handleVoteFailed(Message message, String shoutId, int vote) {
 			SBLog.method(TAG, "handleVoteFailed()");
 			_storage.handleVoteFailed(shoutId, vote);
 			// _uiGateway.handleServerFailure();
 			// possiblyStopPolling(message);
 		}
 
 		public void handleAccountCreated(String uid, String password) {
 			SBLog.method(TAG, "handleAccountCreated()");
 			_storage.handleAccountCreated(uid, password);
 			// Maybe we should do something in the UI?
 		}
 
 		public void handlePingFailed(Message message) {
 			SBLog.method(TAG, "handlePingFailed()");
 			// This has proven to be really damn annoying since every once in a while
 			// network access dissappears.
 			// Users will think our app sucks.
 			// For now let's just ignore ping failure.
 			// createNotice(C.NOTICE_PING_FAILED, C.STRING_PING_FAILED, null);
 			_uiGateway.handleInvalidServerResponse();
 			possiblyStopPolling(message);
 		}
 
 		public void handlePingSuccess() {
 			PollingAlgorithm.resetDropCount();
 		}
 
 		// /////////////////////////////////////////////////////////////////////////
 		// /////////////////////////////////////////////////////////////////////////
 		// /////////////////////////////////////////////////////////////////////////
 
 		public boolean isUiInForeground() {
 			return getIsUiInForeground();
 		}
 		
 		public boolean isResponseClean(Message message) {
 			boolean isClean = true;
 			if (message != null) {
 				CrossThreadPacket xPacket = (CrossThreadPacket) message.obj;
 				if (xPacket != null) {
 					String code = xPacket.json.optString(C.JSON_CODE);
 					String text = xPacket.json.optString(C.JSON_SHOUT_TEXT);
 					if (code != null && !code.equals("")) {
 						if (code.equals(C.JSON_CODE_ANNOUNCEMENT)) {
 							isClean = false;
 							_uiGateway.handleServerAnnouncementCode(text);
 						} else if (code.equals(C.JSON_CODE_ERROR)) {
 							isClean = false;
 							_uiGateway.handleServerDowntimeCode(text);
 						}
 					} else {
 						isClean = false;
 					}
 				} else {
 					isClean = false;
 				}
 			}
 			return isClean;
 		}
 
 		public void handleServerHttpError() {
 			_uiGateway.handleServerHttpError();
 		}
 		
 		public void resetPollingDelay() {
 			_pollingAlgorithm.resetPollingDelay(Mediator.this);
 		}
 
 		public boolean userHasAccount() {
 			SBLog.method(TAG, "userHasAccount()");
 			return _storage.getUserHasAccount();
 		}
 
 		public String getUserId() {
 			SBLog.method(TAG, "getUserId()");
 			return _storage.getUserId();
 		}
 
 		public ArrayList<String> getScoreRequestIds() {
 			SBLog.method(TAG, "getScoreRequestIds()");
 			return _storage.getScoreRequestIds();
 		}
 
 		public String getAuth() {
 			SBLog.method(TAG, "getAuth()");
 			return _storage.getUserAuth();
 		}
 
 		public RadiusCacheCell getRadiusAtCell() {
 			SBLog.method(TAG, "getCellDensity()");
 			return _storage.getRadiusAtCell(getCurrentCell());
 		}
 
 		public double getLongitude() {
 			SBLog.method(TAG, "getLongitude()");
 			return _location.getLongitude();
 		}
 
 		public double getLatitude() {
 			SBLog.method(TAG, "getLatitude()");
 			return _location.getLatitude();
 		}
 
 		public GeoPoint getLocationAsGeoPoint() {
 			SBLog.method(TAG, "getLocation()");
 			return LocationTracker.locationToGeoPoint(_location.getLocation());
 		}
 
 		public boolean getUserLevelUpOccurred() {
 			SBLog.method(TAG, "getLevelUpOccurred()");
 			return _storage.getLevelUpOccured();
 		}
 		
 		public void setUserLevelUpOccured(boolean b) {
 			_storage.setLevelUpOccured(b);
 		}
 
 		public int getUserLevel() {
 			SBLog.method(TAG, "getLevel()");
 			return _storage.getUserLevel();
 		}
 
 		public void updateAuth(String nonce) {
 			SBLog.method(TAG, "updateAuth()");
 			_storage.updateAuth(nonce);
 		}
 
 		public final String getAndroidId() {
 			SBLog.method(TAG, "getAndroidId()");
 			return _device.getAndroidId();
 		}
 
 		public final String getDeviceId() {
 			SBLog.method(TAG, "getDeviceId()");
 			return _device.getDeviceId();
 		}
 
 		public final String getPhoneNumber() {
 			SBLog.method(TAG, "getPhoneNumber()");
 			return _device.getPhoneNumber();
 		}
 
 		public final String getNetworkOperator() {
 			SBLog.method(TAG, "getNetworkOperator()");
 			return _device.getNetworkOperator();
 		}
 
 	}
 
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	// UI GATEWAY /////////////////////////////////////////////////////////////
 
 	private class UiOnGateway implements IUiGateway {
 
 		private Shoutbreak _ui;
 
 		public UiOnGateway(Shoutbreak ui) {
 			_ui = ui;
 		}
 
 		// /////////////////////////////////////////////////////////////////////////
 		// HANDLE STUFF ///////////////////////////////////////////////////////////
 		// /////////////////////////////////////////////////////////////////////////
 
 		public void handleCreateAccountFailed() {
 			handleInvalidServerResponse();
 		}
 
 		public void handleShoutSent() {
 			SBLog.method(TAG, "handleShoutSent()");
 			Drawable d = _ui.shoutBtn.getDrawable();
 			if (d.getClass().equals(BitmapDrawable.class)) {
 				// Shout was a reply
 				_ui.dialogBuilder.handleReplySent();
 			} else if (d.getClass().equals(AnimationDrawable.class)) {
 				// Shout was a normal shout
 				AnimationDrawable shoutButtonAnimation = (AnimationDrawable) _ui.shoutBtn.getDrawable();
 				shoutButtonAnimation.stop();
 				_ui.shoutBtn.setImageResource(R.drawable.shout_button_up);
 				_ui.shoutInputEt.setText("");
 			}
 		}
 
 		public void handleShoutFailed() {
 			SBLog.method(TAG, "handleShoutFailed()");
 			Drawable d = _ui.shoutBtn.getDrawable();
 			if (d.getClass().equals(BitmapDrawable.class)) {
 				// Shout was a reply
 				
 			} else if (d.getClass().equals(AnimationDrawable.class)) {
 				// Shout was a normal shout
 				AnimationDrawable shoutButtonAnimation = (AnimationDrawable) _ui.shoutBtn.getDrawable();
 				shoutButtonAnimation.stop();
 				_ui.shoutBtn.setImageResource(R.drawable.shout_button_up);
 				_ui.shoutInputEt.setText("");
 			}
 			handleInvalidServerResponse();			
 		}
 
 		public void handleRadiusChange(boolean isRadiusSet, long newRadius, int level) {
 			SBLog.method(TAG, "handleRadiusChange()");
 			_ui.userLocationOverlay.handleShoutreachRadiusChange(isRadiusSet, newRadius, level);
 		}
 
 		public void handleLevelUp(long cellRadius, int newLevel) {
 			SBLog.method(TAG, "handleLevelUp()");
 			_uiGateway.refreshProfile(_storage.getUserLevel(), _storage.getUserLevelAt(), _storage.getUserPoints(), _storage.getUserNextLevelAt());
 		}
 
 		public void handlePointsChange(int newPoints) {
 			SBLog.method(TAG, "handlePointsChange()");
 			_uiGateway.refreshProfile(_storage.getUserLevel(), _storage.getUserLevelAt(), _storage.getUserPoints(), _storage.getUserNextLevelAt());
 		}
 
 		public void handleInvalidServerResponse() {
 			SBLog.method(TAG, "handleInvalidServerResponse()");
 			if (PollingAlgorithm.isDropCountAtLimit()) {
 				_ui.dialogBuilder.showDialog(DialogBuilder.DIALOG_SERVER_INVALID_RESPONSE, "");
 			}
 		}
 		
 		public void handleServerHttpError() {
 			SBLog.method(TAG, "handleServerErrorCode()");
 			_ui.dialogBuilder.showDialog(DialogBuilder.DIALOG_SERVER_HTTP_ERROR, "");
 		}
 		
 		public void handleScoreDetailsRequest(int ups, int downs, int score) {
 			_ui.dialogBuilder.showScoreDetailsDialog(ups, downs, score);
 		}
 		
 		public void createReplyDialog(Shout shout) {
 			_ui.dialogBuilder.showReplyDialog(shout);
 		}
 
 		// /////////////////////////////////////////////////////////////////////////
 		// /////////////////////////////////////////////////////////////////////////
 		// /////////////////////////////////////////////////////////////////////////
 
 		public void refreshUiComponents() {
 			// TODO: make inbox & profile more like noticeTabSystem
 			_storage.refreshUiComponents();
 			refreshProfile(_storage.getUserLevel(), _storage.getUserLevelAt(), _storage.getUserPoints(), _storage.getUserNextLevelAt());
 			loadUserSignature();	
 		}
 
 		public void refreshProfile(int level, int levelBeginPoints, int currentPoints, int levelEndPoints) {
 			SBLog.method(TAG, "refreshProfile()");
 			_ui.userCurrentShoutreachTv.setText(Integer.toString(User.calculateMaxShoutreach(level)));
 			_ui.userNextShoutreachTv.setText(Integer.toString(User.calculateMaxShoutreach(level + 1)));
 			_ui.userPointsTv.setText(Integer.toString(currentPoints));
 			_ui.userNextLevelAtTv.setText(Integer.toString(levelEndPoints));
 			String infoParagraph = "You have earned " + currentPoints + " points.\nYour shouts can reach " + User.calculateMaxShoutreach(level)
 					+ " people.\nOnce you earn " + levelEndPoints + " points, you will be able to reach " + User.calculateMaxShoutreach(level + 1) + " people.";
 			if (currentPoints >= levelEndPoints) {
 				infoParagraph = "Congratulations!\nYou have earned enough points to become level " + (level + 1) + ".\nWe are leveling up your account,\nand it will take effect within 30 minutes.";
 			}
 			_ui.userStatsParagraphTv.setText(infoParagraph);
 			_ui.userLevelUpProgessRp.setMax(levelEndPoints - levelBeginPoints);
 			_ui.userLevelUpProgessRp.setProgress(currentPoints - levelBeginPoints);
 		}
 
 		@Override
 		public void refreshSignature(String signature, boolean isSignatureEnabled) {
 			// Signature preference setup
 			int maxLength = _ui.getResources().getInteger(R.integer.shoutMaxLength) - signature.length();
 			_storage.setSignature(signature, isSignatureEnabled);
 			_ui.shoutInputEt.setFilters(new InputFilter[] { new InputFilter.LengthFilter(maxLength) });
 		}
 		
 		@Override
 		public void loadUserSignature() {
 			// Signature preference setup
 			boolean sigEnabled = _preferences.getBoolean(C.PREFERENCE_SIGNATURE_ENABLED, false);
 			String sigText = _preferences.getString(C.PREFERENCE_SIGNATURE_TEXT);
 			if (sigText.length() > 0) {
 				_ui.sigCheckboxCb.setChecked(sigEnabled);
 			} else {
 				_ui.sigCheckboxCb.setChecked(false);
 			}
 			_ui.sigInputEt.setText(sigText);
 			refreshSignature(sigText, sigEnabled);
 		}
 
 		public void enableInputs() {
 			SBLog.method(TAG, "enableInputs()");
 			_ui.shoutBtn.setEnabled(true);
 			_ui.shoutInputEt.setEnabled(true);
 			_storage.enableInputs();
 		}
 
 		public void disableInputs() {
 			SBLog.method(TAG, "disableInputs()");
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
 		public void onPowerPreferenceDisabled(boolean onUiThread) {
 			_ui.onPowerPreferenceDisabled(onUiThread);
 		}
 
 		@Override
 		public void onPowerPreferenceEnabled(boolean onUiThread) {
 			_ui.onPowerPreferenceEnabled(onUiThread);
 		}
 
 		@Override
 		public void unsetUiMediator() {
 			_ui.unsetMediator();
 		}
 
 		@Override
 		public void clearNoticeTab() {
 			_ui.noticeTabPointsTv.setVisibility(View.INVISIBLE);
 			_ui.noticeTabShoutsTv.setVisibility(View.INVISIBLE);
 			//_ui.noticeTabPointsIv.setVisibility(View.INVISIBLE);
 			//_ui.noticeTabShoutsIv.setVisibility(View.INVISIBLE);
 		}
 		
 		@Override
 		public void showPointsNotice(int newPoints) {
 			//_ui.noticeTabPointsIv.setVisibility(View.VISIBLE);
 			_ui.noticeTabPointsTv.setVisibility(View.VISIBLE);
 			String noticeText = "";
 			if (newPoints == C.LEVEL_UP_NOTICE) {
 				noticeText = "you leveled up!  ";
 			} else {
 				noticeText = (newPoints > 0) ? "+" : "-";
 				noticeText += Integer.toString(newPoints) + " point" + ((Math.abs(newPoints) > 1) ? "s  " : "  ");
 			}
 			_ui.noticeTabPointsTv.setText(noticeText);	
 		}
 
 		@Override
 		public void showShoutNotice(String noticeText) {
 			//_ui.noticeTabShoutsIv.setVisibility(View.VISIBLE);
 			_ui.noticeTabShoutsTv.setVisibility(View.VISIBLE);
 			_ui.noticeTabShoutsTv.setText(noticeText);
 		}
 
 		@Override
 		public void setupNoticeTabListView(NoticeTabListViewAdapter listAdapter, boolean itemsCanFocus, OnItemClickListener listViewItemClickListener) {
 			_ui.noticeTabListView.setAdapter(listAdapter);
 			_ui.noticeTabListView.setItemsCanFocus(itemsCanFocus);
 			_ui.noticeTabListView.setOnItemClickListener(listViewItemClickListener);
 		}
 
 		@Override
 		public void setupInboxListView(InboxListViewAdapter listAdapter, boolean itemsCanFocus, OnItemClickListener inboxItemClickListener) {
 			_ui.inboxListView.setAdapter(listAdapter);
 			_ui.inboxListView.setItemsCanFocus(itemsCanFocus);
 			_ui.inboxListView.setOnItemClickListener(inboxItemClickListener);
 		}
 
 		@Override
 		public void showTopNotice() {
 			if (_ui.noticeTabListView.getCount() > 0) {
 				_ui.noticeTabListView.requestFocusFromTouch();
 				_ui.noticeTabListView.setSelection(0);
 			}
 			_ui.noticeTabSd.showOneLine();
 		}
 
 		@Override
 		public void jumpToShoutInInbox(String shoutId) {
 			// TODO Auto-generated method stub
 			if (shoutId != null && !shoutId.equals("")) {
 				_ui.showInbox();
 				_ui.noticeTabSd.animateClose();
 				_storage.jumpToShoutInInbox(shoutId);
 			} else {
 				toast("Sorry, shout not found in inbox.", Toast.LENGTH_SHORT);
 			}
 		}
 
 		@Override
 		public void scrollInboxToPosition(int position) {
 			_ui.inboxListView.requestFocusFromTouch();
 			_ui.inboxListView.setSelection(position);
 		}
 
 		@Override
 		public void toast(String text, int duration) {
 			Toast.makeText(_ui, text, duration).show();
 		}
 
 		@Override
 		public void hideNoticeTab() {
 			_ui.noticeTabSd.animateClose();
 		}
 
 		@Override
 		public void jumpToProfile() {
 			_ui.showProfile();
 		}
 
 		@Override
 		public void handleServerAnnouncementCode(String text) {
 			_ui.dialogBuilder.showDialog(DialogBuilder.DIALOG_SERVER_ANNOUNCEMENT, text);
 		}
 
 		@Override
 		public void handleServerDowntimeCode(String text) {
 			_ui.dialogBuilder.showDialog(DialogBuilder.DIALOG_SERVER_DOWNTIME, text);
 		}
 
 	}
 }
