 package com.dbstar.service;
 
 import java.io.File;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import com.dbstar.util.*;
 import com.dbstar.util.upgrade.RebootUtils;
 import com.dbstar.DbstarDVB.DbstarServiceApi;
 import com.dbstar.app.settings.GDSettings;
 import com.dbstar.guodian.data.EPCConstitute;
 import com.dbstar.guodian.data.ElectricityPrice;
 import com.dbstar.guodian.data.LoginData;
 import com.dbstar.guodian.engine.GDClientObserver;
 import com.dbstar.guodian.engine.GDEngine;
 import com.dbstar.model.ColumnData;
 import com.dbstar.model.ContentData;
 import com.dbstar.model.EventData;
 import com.dbstar.model.GDCommon;
 import com.dbstar.model.GDDVBDataContract;
 import com.dbstar.model.GDSystemConfigure;
 import com.dbstar.model.GDDataModel;
 import com.dbstar.model.GuideListItem;
 import com.dbstar.model.PreviewData;
 import com.dbstar.model.ReceiveData;
 import com.dbstar.model.TDTTimeController;
 import com.dbstar.service.client.GDDBStarClient;
 
 import android.app.Service;
 import android.app.Instrumentation;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.graphics.Bitmap;
 import android.media.AudioManager;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.os.Process;
 import android.os.Looper;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.HandlerThread;
 import android.os.IBinder;
 import android.os.Binder;
 import android.os.Message;
 import android.util.Log;
 import android.util.EventLog;
 import android.view.KeyEvent;
 
 public class GDDataProviderService extends Service {
 
 	private static final String TAG = "GDDataProviderService";
 
 	public static final int POWERMANAGER_MSG_POWERKEY = 0x1;
 	public static final int POWERMANAGER_MSG_SIMULATEKEY = 0x2;
 	public static final int POWERMANAGER_MODE_RUNNING = 0x101;
 	public static final int POWERMANAGER_MODE_SCREENOFF = 0x102;
 	public static final int POWERMANAGER_MODE_SLEEP = 0x103;
 	public static final int POWERMANAGER_UPDATE_INTERVAL = 600000;
 
 	public static final int REQUESTTYPE_GETCOLUMNS = 0x1001;
 	public static final int REQUESTTYPE_GETPUBLICATION = 0x1002;
 	public static final int REQUESTTYPE_GETPUBLICATIONSET = 0x1003;
 	public static final int REQUESTTYPE_GETPUBLICATIONS_OFSET = 0x1004;
 	public static final int REQUESTTYPE_GETIMAGE = 0x1005;
 	public static final int REQUESTTYPE_GETDETAILSDATA = 0x1006;
 	public static final int REQUESTTYPE_GETGUIDELIST = 0x1007;
 	public static final int REQUESTTYPE_GETPREVIEWS = 0x1009;
 
 	public static final int REQUESTTYPE_UPDATEGUIDELIST = 0x1008;
 
 	public static final int REQUESTTYPE_STARTGETTASKINFO = 0x2001;
 	public static final int REQUESTTYPE_STOPGETTASKINFO = 0x2002;
 	public static final int REQUESTTYPE_GETTSSIGNALSTATUS = 0x2003;
 	public static final int REQUESTTYPE_GETDOWNLOADSTATUS = 0x2004;
 	public static final int REQUESTTYPE_GETSMARTCARDINFO = 0x2005;
 	public static final int REQUESTTYPE_MANAGECA = 0x2006;
 	public static final int REQUESTTYPE_GETMAILCONTENT = 0x2007;
 	public static final int REQUESTTYPE_GETPUBLICATIONDRMINFO = 0x2008;
 	public static final int REQUESTTYPE_GETMOVIECOUNT = 0x2009;
 	public static final int REQUESTTYPE_GETTVCOUNT = 0x2010;
 
 	public static final int REQUESTTYPE_SETSETTINGS = 0x4001;
 	public static final int REQUESTTYPE_GETSETTINGS = 0x4002;
 
 	public static final int REQUESTTYPE_GETFAVORITEMOVIE = 0x5001;
 	public static final int REQUESTTYPE_GETFAVORITETV = 0x5002;
 	public static final int REQUESTTYPE_GETFAVORITERECORD = 0x5003;
 	public static final int REQUESTTYPE_GETFAVORITEENTERTAINMENT = 0x5004;
 
 	public static final int REQUESTTYPE_GETDEVICEINFO = 0x6001;
 
 	private static final String PARAMETER_COLUMN_ID = "column_id";
 	private static final String PARAMETER_SET_ID = "set_id";
 	private static final String PARAMETER_PAGENUMBER = "page_number";
 	private static final String PARAMETER_PAGESIZE = "page_size";
 	private static final String PARAMETER_CONTENTDATA = "content_data";
 
 	private static final String PARAMETER_CCID = "cc_id";
 	private static final String PARAMETER_DATESTART = "date_start";
 	private static final String PARAMETER_DATEEND = "date_end";
 	private static final String PARAMETER_CHAREGTYPE = "charge_type";
 	private static final String PARAMETER_KEYS = "keys";
 	private static final String PARAMETER_KEY = "key";
 	private static final String PARAMETER_VALUE = "value";
 
 	private Object mTaskQueueLock = new Object();
 	private LinkedList<RequestTask> mTaskQueue = null;
 
 	private Object mFinishedTaskQueueLock = new Object();
 	private LinkedList<RequestTask> mFinishedTaskQueue = null;
 
 	private int mThreadCount = 2;
 	private List<WorkerThread> mThreadPool = new LinkedList<WorkerThread>();
 
 	private int mMainThreadId;
 	private int mMainThreadPriority;
 
 	private GDSystemConfigure mConfigure = null;
 
 	private GDDataModel mDataModel = null;
 
 	private boolean mIsWirelessConnected = false, mIsEthernetConnected = false;
 	private ConnectivityManager mConnectManager;
 	//private GDDiskSpaceMonitor mDiskMonitor;
 
 	private GDDBStarClient mDBStarClient;
 	private GDApplicationObserver mApplicationObserver = null;
 	private ClientObserver mPageOberser = null;
 	private NetworkController mEthernetController = null;
 	private GDAudioController mAudioController = null;
 	private final IBinder mBinder = new DataProviderBinder();
 	private SystemEventHandler mHandler = null;
 
 	boolean mIsSmartHomeServiceStarted = false;
 	boolean mIsDbServiceStarted = false;
 	boolean mIsStorageReady = false;
 	boolean mIsNetworkReady = false;
 	
 	boolean mIsGuodianEngineStarted = false;
 
 	private int mSmartcardState = GDCommon.SMARTCARD_STATE_NONE;
 
 	private String mMacAddress = "";
 
 	private String mUpgradePackageFile;
 	private boolean mNeedUpgrade = false;
 
 	private String mDefaultColumnIconFile = null;
 
 	private PeripheralController mPeripheralController;
 	private GDPowerManager mPowerManager;
     private HandlerThread mPowerThread = null;
 	private Handler mPowerHandler = null;
     private PowerTask mPowerTask = null;
 
     private int mSleepMode = 0;
 
 	private GDEngine mGuodianEngine;
 
 	private class RequestTask {
 		public static final int INVALID = 0;
 		public static final int ACTIVE = 1;
 
 		long Id;
 		int Type;
 		int Flag;
 
 		ClientObserver Observer;
 		int Index;
 
 		int PageNumber;
 		int PageSize;
 
 		int ColumnLevel;
 
 		Map<String, Object> Parameters;
 
 		Object Key;
 		Object Data;
 
 		public RequestTask() {
 			Flag = ACTIVE;
 		}
 	};
 
 	public void registerAppObserver(GDApplicationObserver observer) {
 		mApplicationObserver = observer;
 	}
 
 	public void unRegisterAppObserver(GDApplicationObserver observer) {
 		if (mApplicationObserver == observer) {
 			mApplicationObserver = null;
 		}
 	}
 
 	public void registerPageObserver(ClientObserver observer) {
 		mPageOberser = observer;
 	}
 
 	public void unRegisterPageObserver(ClientObserver observer) {
 		if (mPageOberser == observer) {
 			mPageOberser = null;
 		}
 	}
 
 	public class DataProviderBinder extends Binder {
 		public GDDataProviderService getService() {
 			return GDDataProviderService.this;
 		}
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return mBinder;
 	}
 
 	public void onCreate() {
 		super.onCreate();
 		Log.d(TAG, "onCreate");
 
 		mMainThreadId = Process.myTid();
 		mMainThreadPriority = Process.getThreadPriority(mMainThreadId);
 
 		// create power manager thread
 		mSleepMode = POWERMANAGER_MODE_RUNNING;
 		mPowerManager = new GDPowerManager();
 		mPowerThread = new HandlerThread("PowerManager", Process.THREAD_PRIORITY_BACKGROUND);
 		mPowerThread.start();
 		mPowerHandler = new PowerEventHandler(mPowerThread.getLooper());
 		mPowerTask = new PowerTask();
 
 		mHandler = new SystemEventHandler();
 
 		mConfigure = new GDSystemConfigure();
 		mDataModel = new GDDataModel();
 		//mDiskMonitor = new GDDiskSpaceMonitor(mHandler);
 		mDBStarClient = new GDDBStarClient(this);
 
 		mGuodianEngine = new GDEngine(this);
 
 		mTaskQueue = new LinkedList<RequestTask>();
 		mFinishedTaskQueue = new LinkedList<RequestTask>();
 
 		mPeripheralController = new PeripheralController();
 		mEthernetController = new NetworkController(this, mHandler);
 		mAudioController = new GDAudioController(this, mHandler);
 		
 		// initialize smartcard state
 		if (isSmartcardPlugIn()) {
 			mSmartcardState = GDCommon.SMARTCARD_STATE_INSERTING;
 		} else {
 			mSmartcardState = GDCommon.SMARTCARD_STATE_REMOVED;
 		}
 
 		if (mPeripheralController.isHdmiIn()) {
 			Log.d(TAG, "Hdmi: IN");
 			mPeripheralController.setAudioOutputOff();
 		} else {
 			Log.d(TAG, "Hdmi: OUT");
 			mPeripheralController.setAudioOutputOn();
 		}
 		mPeripheralController.setPowerLedOff();
 
 		for (int i = 0; i < mThreadCount; i++) {
 			WorkerThread thread = new WorkerThread();
 			thread.start();
 
 			mThreadPool.add(thread);
 		}
 
 		registerUSBReceiver();
 		reqisterConnectReceiver();
 		reqisterSystemMessageReceiver();
 
 		// read configure
 		mConfigure.configureSystem();
 
 		// check storage
 		// the disk is not mounted at this point,
 		// so wait for mount event.
 		if (mConfigure.configureStorage()) {
 			String disk = mConfigure.getStorageDisk();
 			Log.d(TAG, "monitor disk " + disk);
 
 			if (!disk.isEmpty()) {
 				mIsStorageReady = true;
 				Log.d(TAG, "disk is ready " + disk);
 
 				//mDiskMonitor.addDiskToMonitor(disk);
 			}
 		}
 
 		String channel = FileOperation.read(GDCommon.ChannelFile);
 		Log.d(TAG, "channel ="+channel);
 
 		if (channel != null && channel.length() > 0) {
 			mChannelMode = channel.equals(GDCommon.ChannelEthernet)? GDCommon.EthernetMode: GDCommon.WirelessMode;
 		}
 
 		// check network
 		mIsNetworkReady = isNetworkConnected();
 		Log.d(TAG, "network is connected " + mIsNetworkReady);
 
 		// initialize engine
 		initializeDataEngine();
 
 		//queryDiskGuardSize();
 
 		if (mIsStorageReady) {
 			mDataModel.setPushDir(mConfigure.getStorageDir());
 		}
 
 		// start smart home service
 		mIsSmartHomeServiceStarted = false;
 
 		if (mIsNetworkReady) {
 			startDbStarService();
 			startGuodianEngine();
 		}
 
 		// start Dbstar service
 		mIsDbServiceStarted = false;
 		mDBStarClient.start();
 
 	}
 
 	void startGuodianEngine() {
 		Log.d(TAG, "========== startGuodianEngine ==========");
 		String serverIP = mDataModel
 				.getSettingValue(GDSettings.PropertyGuodianServerIP);
 		String serverPort = mDataModel
 				.getSettingValue(GDSettings.PropertyGuodianServerPort);
 		if (serverIP == null || serverIP.isEmpty() || serverPort == null
 				|| serverPort.isEmpty())
 			return;
 
 		mGuodianEngine.setReconnectTime(mConfigure.getGuodianReconnectTime());
 		mGuodianEngine
 				.start(serverIP, Integer.valueOf(serverPort), mGDObserver);
 		
 		mIsGuodianEngineStarted = true;
 	}
 
 	void stopGuodianEngine() {
 		Log.d(TAG, "========== stopGuodianEngine ==========");
 		mGuodianEngine.stop();
 		mIsGuodianEngineStarted = false;
 	}
 
 	void destroyGuodianEngine() {
 		Log.d(TAG, "========== destroyGuodianEngine ==========");
 		mGuodianEngine.destroy();
 		mIsGuodianEngineStarted = false;
 	}
 
 	void initializeDataEngine() {
 		mDataModel.initialize(mConfigure);
 		// mDataModel.setPushDir(mConfigure.getStorageDir());
 	}
 
 	void deinitializeDataEngine() {
 		mDataModel.deInitialize();
 	}
 
 	public void onDestroy() {
 		super.onDestroy();
 		Log.d(TAG, "onDestroy");
 
 		stopDbStarService();
 		mDBStarClient.stop();
 		//mDiskMonitor.stopMonitor();
 
 		deinitializeDataEngine();
 
 		unregisterReceiver(mNetworkReceiver);
 		unregisterReceiver(mUSBReceiver);
 		unregisterReceiver(mSystemMessageReceiver);
 
 		cancelAllRequests();
 
 		for (int i = 0; i < mThreadCount; i++) {
 			WorkerThread thread = mThreadPool.get(i);
 			thread.setExit(true);
 		}
 
 		synchronized (mTaskQueueLock) {
 			mTaskQueueLock.notifyAll();
 		}
 
 		destroyGuodianEngine();
 	}
 
 	private void startDbStarService() {
 		Log.d(TAG, "++++++++++++++++++startDbStarService++++++++++++++++++++");
 
 		if (mIsSmartHomeServiceStarted)
 			return;
 
 		mIsSmartHomeServiceStarted = true;
 
 		// SharedPreferences settings = null;
 		// SharedPreferences.Editor editor = null;
 		//
 		// settings = getSharedPreferences(SmartHomePrepertyName, 0);
 		// editor = settings.edit();
 		// editor.putInt(SmartHomePrepertyName, 1);
 		// editor.commit();
 
 		SystemUtils.startSmartHomeServer();
 	}
 
 	private void stopDbStarService() {
 		if (!mIsSmartHomeServiceStarted)
 			return;
 
 		mIsSmartHomeServiceStarted = false;
 
 		Log.d(TAG, "stopDbStarService");
 
 		// SharedPreferences settings = null;
 		// SharedPreferences.Editor editor = null;
 		//
 		// settings = getSharedPreferences(SmartHomePrepertyName, 0);
 		// editor = settings.edit();
 		// editor.putInt(SmartHomePrepertyName, 0);
 		// editor.commit();
 
 		SystemUtils.stopSmartHomeServer();
 	}
 
 	private void startPowerTask() {
 		if (mPowerHandler != null) {
 			mPowerHandler.postDelayed(mPowerTask, POWERMANAGER_UPDATE_INTERVAL);
 		}
 	}
 
 	private void stopPowerTask() {
 		if (mPowerHandler != null) {
 			mPowerHandler.removeCallbacks(mPowerTask);
 		}
 	}
 
 	class PowerEventHandler extends Handler {
 		public PowerEventHandler(Looper looper) {
 			super(looper);
 		}
 		public void handleMessage(Message msg) {
 			int msgId = msg.what;
 			switch (msgId) {
 			case POWERMANAGER_MSG_POWERKEY: {
 				Log.d(TAG, "sendKey(KEYCODE_POWER)");
 				Instrumentation inst = new Instrumentation();
 				inst.sendKeyDownUpSync(KeyEvent.KEYCODE_POWER);
 				break;
 			}
 			case POWERMANAGER_MSG_SIMULATEKEY: {
 				Log.d(TAG, "sendKey()" + msg.arg1);
 				Instrumentation inst = new Instrumentation();
                 inst.sendKeyDownUpSync(msg.arg1);
 				break;
 			}
 			default:
 				break;
 			}
 		}
 	}
 
 	class PowerTask implements Runnable {
 		public void run() {
 			Log.d(TAG, "--- PowerTask() ---");
 			if (mPowerHandler == null) {
 				return;
 			}
 
 			if (mSleepMode == POWERMANAGER_MODE_SCREENOFF) {
 				int secs = getWakeupTime();
 				if (secs > 0) {
 					Log.d(TAG, "--- PowerMode: SCREENOFF-->SLEEP, alarm=" + secs);
 					mSleepMode = POWERMANAGER_MODE_SLEEP;
 					mPowerManager.setAlarm(secs);
 					mPowerManager.releaseWakeLock();
 				}
 			}
 			mPowerHandler.postDelayed(mPowerTask, POWERMANAGER_UPDATE_INTERVAL);
 		}
 	}
 
 	class SystemEventHandler extends Handler {
 		public void handleMessage(Message msg) {
 			int msgId = msg.what;
 			switch (msgId) {
 			case GDCommon.MSG_TASK_FINISHED: {
 				// RequestTask task = dequeueFinishedTask();
 				RequestTask task = (RequestTask) msg.obj;
 
 				if (task != null) {
 					handleTaskFinished(task);
 				}
 
 				break;
 			}
 
 			case GDCommon.MSG_MEDIA_MOUNTED: {
 				Bundle data = msg.getData();
 				String disk = data.getString("disk");
 				// Log.d(TAG, "mount storage = " + disk);
 				Log.d(TAG, "++++++++ mount storage ++++++++" + disk);
 
 				// check whether the disk is mounted.
 				mConfigure.configureStorage();
 				String storage = mConfigure.getStorageDisk();
 				
 				if (disk.equals(storage)) {
 					// disk is mounted
 					mIsStorageReady = true;
 					String dir = mConfigure.getStorageDir();
 					Log.d(TAG, "11111111111111111  dir === " + dir);
 					mDataModel.setPushDir(dir);
 
 					Log.d(TAG, " +++++++++++ monitor disk ++++++++" + disk);
 					//mDiskMonitor.removeDiskFromMonitor(disk);
 					//mDiskMonitor.addDiskToMonitor(disk);
 
 					notifyDbstarServiceStorageStatus(disk);
 
 					if (mApplicationObserver != null) {
 						// restart application
 						mApplicationObserver.initializeApp();
 					}
 				} else {
 					// U disk inserted
 					if (mIsDbServiceStarted) {
 						mDBStarClient.notifyDbServer(DbstarServiceApi.CMD_DISK_MOUNT, disk);
 					}
 				}
 
 				break;
 			}
 
 			case GDCommon.MSG_MEDIA_REMOVED: {
 				Bundle data = msg.getData();
 				String disk = data.getString("disk");
 
 				//mDiskMonitor.removeDiskFromMonitor(disk);
 
 				String storage = mConfigure.getStorageDisk();
 				if (disk.equals(storage)) {
 					mIsStorageReady = false;
 
 					notifyDbstarServiceStorageStatus(disk);
 
 					if (mApplicationObserver != null) {
 						mApplicationObserver.deinitializeApp();
 					}
 				} else {
 					// U disk removed
 					if (mIsDbServiceStarted) {
 						mDBStarClient.notifyDbServer(DbstarServiceApi.CMD_DISK_UNMOUNT, disk);
 					}
 				}
 				break;
 			}
 
 			case GDCommon.MSG_NETWORK_CONNECT: {
 				Log.d(TAG, " +++++++++++++ network connected +++++++++++++");
 				if (mChannelMode == GDCommon.EthernetMode) {
 					// single card
 					// ethernet connected
 					mIsNetworkReady = true;
 					mPeripheralController.setNetworkLedOn();
 					startDbStarService();
 					startGuodianEngine();
 				} else {
 					// dual card
 					int type = msg.arg1;
 					if (type == GDCommon.TypeEthernet) {
 						//ethernet connected
 						if (mIsEthernetConnected) {
 							Log.d(TAG, "ethernet is connected already!");
 							return;
 						}
 						
 						mIsEthernetConnected = true;
 						if (mIsWirelessConnected) {
 							// remove Ethernet gateway.
 							NativeUtil.shell("ip route del dev eth0");
 						}
 						
 					} else {
 						// wifi connected
 						if (mIsWirelessConnected) {
 							Log.d(TAG, "ethernet is connected already!");
 							return;
 						}
 						
 						mIsWirelessConnected = true;
 						if (mIsEthernetConnected) {
 							// remove Ethernet gateway.
 							NativeUtil.shell("ip route del dev eth0");
 						}
 
 						mIsNetworkReady = true;
 
 						startDbStarService();
 						startGuodianEngine();
 					}
 					
 					if (mIsWirelessConnected || mIsEthernetConnected) {
 						mPeripheralController.setNetworkLedOn();
 					}
 				}
 
 				break;
 			}
 			case GDCommon.MSG_NETWORK_DISCONNECT: {
 				Log.d(TAG, "++++++++++++ network disconnected +++++++++++");
 				if (mChannelMode == GDCommon.EthernetMode) {
 					mIsNetworkReady = false;
 					mPeripheralController.setNetworkLedOff();
 					stopGuodianEngine();
 				} else {
 					int type = msg.arg1;
 					if (type == GDCommon.TypeEthernet) {
 						//ethernet connected
 						if (!mIsEthernetConnected) {
 							Log.d(TAG, "ethernet is disconnected already!");
 							return;
 						}
 						
 						mIsEthernetConnected = false;
 					} else {
 						// wifi connected
 						if (!mIsWirelessConnected) {
 							Log.d(TAG, "ethernet is disconnected already!");
 							return;
 						}
 						
 						mIsWirelessConnected = false;
 
 						mIsNetworkReady = false;
 
 						stopGuodianEngine();
 					}
 					
 					if (!mIsWirelessConnected && !mIsEthernetConnected) {
 						mPeripheralController.setNetworkLedOff();
 					}
 					
 				}
 
 				break;
 			}
 
 			case GDCommon.MSG_ETHERNET_PHYCONECTED: {
 				notifyDbstarServiceNetworkStatus();
 				break;
 			}
 
 			case GDCommon.MSG_ETHERNET_PHYDISCONECTED: {
 				ethernetDisconnected();
 				notifyDbstarServiceNetworkStatus();
 				break;
 			}
 			
 			case GDCommon.MSG_ETHERNET_CONNECTED: {
 				ethernetConnected();
 				break;
 			}
 			
 			case GDCommon.MSG_DISK_SPACEWARNING: {
 				Bundle data = msg.getData();
 				String disk = (String) data.get(GDCommon.KeyDisk);
 				if (mApplicationObserver != null) {
 					mApplicationObserver.handleNotifiy(
 							GDCommon.MSG_DISK_SPACEWARNING, disk);
 				}
 
 				mDBStarClient
 						.notifyDbServer(DbstarServiceApi.CMD_DISK_FOREWARNING, null);
 				break;
 			}
 
 			case GDCommon.MSG_SYSTEM_FORCE_UPGRADE:
 			case GDCommon.MSG_SYSTEM_UPGRADE: {
 				String packageFile = (String) msg.obj;
 				mApplicationObserver.handleNotifiy(msgId, packageFile);
 
 				break;
 			}
 
 			case GDCommon.MSG_USER_UPGRADE_CANCELLED: {
 				mNeedUpgrade = true;
 				mUpgradePackageFile = String.valueOf(msg.obj);
 				mDBStarClient
 						.notifyDbServer(DbstarServiceApi.CMD_UPGRADE_CANCEL, null);
 				break;
 			}
 
 			case GDCommon.MSG_ADD_TO_FAVOURITE: {
 				String publicationId = msg.getData().getString(
 						GDCommon.KeyPublicationID);
 
 				String publicationSetId = msg.getData().getString(
 						GDCommon.KeyPublicationSetID);
 
 				if (mDataModel != null) {
 					mDataModel.addPublicationToFavourite(publicationSetId,
 							publicationId);
 				}
 				break;
 			}
 			case GDCommon.MSG_DELETE: {
 				String publicationId = msg.getData().getString(
 						GDCommon.KeyPublicationID);
 
 				String publicationSetId = msg.getData().getString(
 						GDCommon.KeyPublicationSetID);
 
 				if (mDataModel != null) {
 					mDataModel.deletePublication(publicationSetId,
 							publicationId);
 
 					if (mPageOberser != null) {
 						EventData.DeleteEvent event = new EventData.DeleteEvent();
 						event.PublicationId = publicationId;
 						event.PublicationSetId = publicationSetId;
 						mPageOberser.notifyEvent(EventData.EVENT_DELETE, event);
 					}
 				}
 				break;
 			}
 			case GDCommon.MSG_SAVE_BOOKMARK: {
 				String publicationId = msg.getData().getString(
 						GDCommon.KeyPublicationID);
 
 				int bookmark = msg.getData().getInt(GDCommon.KeyBookmark);
 				if (mDataModel != null) {
 					mDataModel.savePublicationBookmark(publicationId, bookmark);
 				}
 
 				if (mPageOberser != null) {
 					EventData.UpdatePropertyEvent event = new EventData.UpdatePropertyEvent();
 					event.PublicationId = publicationId;
 					event.PropertyName = GDCommon.KeyBookmark;
 					event.PropertyValue = new Integer(bookmark);
 					mPageOberser.notifyEvent(EventData.EVENT_UPDATE_PROPERTY,
 							event);
 				}
 
 				break;
 			}
 			
 			case GDCommon.MSG_PLAY_COMPLETED: {
 				if (mPageOberser != null) {
 					EventData.PlaybackEvent event = new EventData.PlaybackEvent();
 					event.Event = GDCommon.PLAYBACK_COMPLETED;
 					mPageOberser.notifyEvent(EventData.EVENT_PLAYBACK,
 							event);
 				}
 				break;
 			}
 			case GDCommon.MSG_GET_NETWORKINFO: {
 				String[] keys = new String[5];
 				keys[0] = GDSettings.PropertyMulticastIP;
 				keys[1] = GDSettings.PropertyMulticastPort;
 				keys[2] = GDSettings.PropertyGatewaySerialNumber;
 				keys[3] = GDSettings.PropertyGatewayIP;
 				keys[4] = GDSettings.PropertyGatewayPort;
 
 				Intent intent = new Intent();
 				intent.setAction(GDCommon.ActionUpateNetworkInfo);
 
 				for (int i = 0; i < 2; i++) {
 					String value = mDataModel.queryGlobalProperty(keys[i]);
 					Log.d(TAG, "+++++++++++ queryGlobalProperty key=" + keys[i]
 							+ " value=" + value);
 					intent.putExtra(keys[i], value);
 				}
 
 				for (int i = 2; i < keys.length; i++) {
 					String value = mDataModel.getSettingValue(keys[i]);
 					Log.d(TAG, "+++++++++++ getSettingValue key=" + keys[i]
 							+ " value=" + value);
 					intent.putExtra(keys[i], value);
 				}
 
 				sendNetworkInfo(intent);
 				break;
 			}
 
 			case GDCommon.MSG_SET_NETWORKINFO: {
 				String[] keys = new String[5];
 				keys[0] = GDSettings.PropertyGatewaySerialNumber;
 				keys[1] = GDSettings.PropertyGatewayIP;
 				keys[2] = GDSettings.PropertyGatewayPort;
 				keys[3] = GDSettings.PropertyMulticastIP;
 				keys[4] = GDSettings.PropertyMulticastPort;
 
 				String[] values = new String[5];
 				Bundle data = msg.getData();
 				values[0] = data
 						.getString(GDSettings.PropertyGatewaySerialNumber);
 				values[1] = data.getString(GDSettings.PropertyGatewayIP);
 				values[2] = data.getString(GDSettings.PropertyGatewayPort);
 				values[3] = data.getString(GDSettings.PropertyMulticastIP);
 				values[4] = data.getString(GDSettings.PropertyMulticastPort);
 
 				for (int i = 0; i < 3; i++) {
 					mDataModel.setSettingValue(keys[i], values[i]);
 				}
 
 				for (int i = 3; i < keys.length; i++) {
 					mDataModel.updateGlobalProperty(keys[i], values[i]);
 				}
 				break;
 			}
 
 			case GDCommon.MSG_DATA_SIGNAL_STATUS: {
 				int hasSignal = msg.arg1;
 				if (mPageOberser != null) {
 					EventData.DataSignalEvent event = new EventData.DataSignalEvent();
 					event.hasSignal = hasSignal == GDCommon.STATUS_HASSIGNAL ? true
 							: false;
 					mPageOberser.notifyEvent(EventData.EVENT_DATASIGNAL, event);
 				}
 				break;
 			}
 
 			case GDCommon.SYNC_STATUS_TODBSERVER: {
 				syncStatusToDbServer();
 				break;
 			}
 
 			case GDCommon.MSG_UPDATE_COLUMN:
 			case GDCommon.MSG_UPDATE_PREVIEW:
 			case GDCommon.MSG_UPDATE_UIRESOURCE: {
 				mApplicationObserver.handleNotifiy(msgId, null);
 				break;
 			}
 
 			case GDCommon.MSG_SMARTCARD_IN: {
 				mSmartcardState = GDCommon.SMARTCARD_STATE_INSERTING;
 
 				notifyDbstarServiceSDStatus();
 				// notifySmartcardStatusChange(mSmartcardState);
 				break;
 			}
 			case GDCommon.MSG_SMARTCARD_OUT: {
 				mSmartcardState = GDCommon.SMARTCARD_STATE_REMOVING;
 				notifyDbstarServiceSDStatus();
 				notifySmartcardStatusChange(mSmartcardState);
 				break;
 			}
 
 			case GDCommon.MSG_SMARTCARD_INSERT_OK: {
 				mSmartcardState = GDCommon.SMARTCARD_STATE_INSERTED;
 				Log.d(TAG, "===========Smartcard========== reset ok! ");
 				notifySmartcardStatusChange(mSmartcardState);
 				break;
 			}
 
 			case GDCommon.MSG_SMARTCARD_INSERT_FAILED: {
 				mSmartcardState = GDCommon.SMARTCARD_STATE_INVALID;
 				Log.d(TAG, "===========Smartcard========== invalid!");
 				notifySmartcardStatusChange(mSmartcardState);
 				break;
 			}
 
 			case GDCommon.MSG_SMARTCARD_REMOVE_OK: {
 				mSmartcardState = GDCommon.SMARTCARD_STATE_REMOVED;
 				Log.d(TAG, "===========Smartcard========== remove ok!");
 				// notifySmartcardStatusChange(mSmartcardState);
 				break;
 			}
 
 			case GDCommon.MSG_SMARTCARD_REMOVE_FAILED: {
 				mSmartcardState = GDCommon.SMARTCARD_STATE_INVALID;
 				Log.d(TAG, "===========Smartcard========== remove failed!");
 				// notifySmartcardStatusChange(mSmartcardState);
 				break;
 			}
 
 			case GDCommon.MSG_NEW_MAIL: {
 				notifyNewMail();
 				break;
 			}
 
 			case GDCommon.MSG_DISP_NOTIFICATION: {
 				diplayNotification((String) msg.obj);
 				break;
 			}
 			
 			case GDCommon.MSG_HIDE_NOTIFICATION: {
 				hideNotification();
 				break;
 			}
 			
 			case GDCommon.MSG_GET_ETHERNETINFO: {
 				getEthternetInfo();
 				break;
 			}
 			
 			case GDCommon.MSG_MUTE_AUDIO: {
 				mAudioController.muteAudio(msg.arg1);
 				break;
 			}
 			
 			case GDCommon.MSG_BOOT_COMPLETED: {
 				bootCompleted();
 				break;
 			}
 			
 			case GDCommon.MSG_SYSTEM_RECOVERY: {
 				handleRecoveryAction(msg.arg1);
 				break;
 			}
 			
 			case GDCommon.MSG_DISK_FORMAT_FINISHED: {
 				boolean successed = msg.arg1 == GDCommon.VALUE_SUCCESSED ? true : false;
 				String info = null;
 				if (!successed) {
 					info = (String) msg.obj;
 				}
 		
 				handleDiskFormatResult(successed, info);
 				break;
 			}
 			
 			case GDCommon.MSG_DISK_INITIALIZE: {
 				handleDiskInitMessage(msg.arg1, (String)msg.obj);
 				break;
 			}
 			
 			case GDCommon.MSG_HOMEKEY_PRESSED: {
 				handleHomeKeyPressed();
 				break;
 			}
 			
 			case GDCommon.MSG_DEVICE_INIT_FINISHED: {
 				handleDeviceInitFinished();
 				break;
 			}
 
 			case GDCommon.MSG_SYSTEM_REBOOT: {
 				handleSystemReboot();
 				break;
 			}
 
 			default:
 				break;
 			}
 		}
 
 	}
 
 	private void getEthternetInfo() {
 		if (mIsDbServiceStarted) {
 			String info = mDBStarClient.getEthernetInfo();
 			Intent intent = new Intent(GDCommon.ActionSetEthernetInfo);
 			intent.putExtra("ethernet_info", info);
 			sendBroadcast(intent);
 		}
 	}
 	
 	boolean mIsDisplaySet = false;
 	public boolean isDisplaySet() {
 		return mIsDisplaySet;
 	}
 
 	private void bootCompleted() {
 		SystemUtils.setVideoSettings();
 		mIsDisplaySet = true;
 		/* when system reboot from screen off mode, donot screen on. */
 		if (SystemUtils.getSystemStatus().equals("screenoff")) {
 			Log.d(TAG, "--- BootCompleted: AUTOREBOOT-->SCREENOFF");
 			SystemUtils.setSystemStatus("running");
 			Message msg = mPowerHandler.obtainMessage(POWERMANAGER_MSG_POWERKEY);
 			msg.sendToTarget();
 		}
 	}
 	
 	private void handleHomeKeyPressed() {
 		if (mApplicationObserver != null) {
 			mApplicationObserver.handleNotifiy(GDCommon.MSG_HOMEKEY_PRESSED, null);
 		}
 	}
 	
 	private void handleDeviceInitFinished() {
 		if (mApplicationObserver != null) {
 			mApplicationObserver.handleNotifiy(GDCommon.MSG_DEVICE_INIT_FINISHED, null);
 		}
 	}
 	
 	private void handleSystemReboot() {
 		if (mSleepMode == POWERMANAGER_MODE_SCREENOFF) {
 			Log.d(TAG, "+++++++++++ set system.status=screenoff");
 			SystemUtils.setSystemStatus("screenoff");
 		}
 		RebootUtils.rebootNormal(this);
 	}
 
 	private void notifySmartcardStatusChange(int state) {
 		EventData.SmartcardStatus event = new EventData.SmartcardStatus();
 		event.State = state;
 		if (mPageOberser != null) {
 			mPageOberser.notifyEvent(EventData.EVENT_SMARTCARD_STATUS, event);
 		}
 	}
 
 	private void notifyNewMail() {
 		if (mPageOberser != null) {
 			mPageOberser.notifyEvent(EventData.EVENT_NEWMAIL, null);
 		}
 	}
 
 	private void diplayNotification(String message) {
 
 		Log.d(TAG, "======= diplayNotification ==== observer = " + mPageOberser
 				+ " message " + message);
 
 		if (mPageOberser != null) {
 			mPageOberser.notifyEvent(EventData.EVENT_NOTIFICATION, message);
 		}
 	}
 	
 	private void hideNotification() {
 
 		if (mPageOberser != null) {
 			mPageOberser.notifyEvent(EventData.EVENT_HIDE_NOTIFICATION, null);
 		}
 	}
 	
 	private void handleDiskInitMessage(int type, String msg) {
 		if (mPageOberser != null) {
 			EventData.DiskInitEvent event = new EventData.DiskInitEvent();
 			event.Type = type;
 			event.Message = msg;
 			mPageOberser.notifyEvent(EventData.EVENT_DISK_INIT, event);
 		}
 	}
 
 	private void sendNetworkInfo(Intent intent) {
 		sendBroadcast(intent);
 	}
 
 	private void handleTaskFinished(RequestTask task) {
 		Log.d(TAG, "handleTaskFinished type [" + task.Type + "]");
 
 		switch (task.Type) {
 		case REQUESTTYPE_GETCOLUMNS: {
 			if (task.Observer != null) {
 				task.Observer.updateData(task.Type, task.ColumnLevel,
 						task.Index, task.Data);
 			}
 			break;
 		}
 
 		case REQUESTTYPE_GETFAVORITEMOVIE:
 		case REQUESTTYPE_GETFAVORITETV:
 		case REQUESTTYPE_GETFAVORITERECORD:
 		case REQUESTTYPE_GETFAVORITEENTERTAINMENT:
 		case REQUESTTYPE_GETPUBLICATION:
 		case REQUESTTYPE_GETPUBLICATIONSET: {
 			if (task.Observer != null) {
 				task.Observer.updateData(task.Type, null, task.Data);
 			}
 			break;
 		}
 		
 		case REQUESTTYPE_GETMOVIECOUNT:
 		case REQUESTTYPE_GETTVCOUNT: {
 			if (task.Observer != null) {
 				task.Observer.updateData(task.Type, task.Key, task.Data);
 			}
 			break;
 		}
 
 		case REQUESTTYPE_GETPUBLICATIONS_OFSET:
 		case REQUESTTYPE_GETDETAILSDATA:
 		case REQUESTTYPE_GETIMAGE: {
 			if (task.Observer != null) {
 				task.Observer.updateData(task.Type, task.PageNumber,
 						task.Index, task.Data);
 			}
 			break;
 		}
 		case REQUESTTYPE_GETPREVIEWS:
 		case REQUESTTYPE_GETGUIDELIST:
 		case REQUESTTYPE_GETDEVICEINFO:
 		case REQUESTTYPE_GETTSSIGNALSTATUS:
 		case REQUESTTYPE_GETDOWNLOADSTATUS: {
 			if (task.Observer != null) {
 				// task.Observer.updateData(task.Type, task.PageNumber,
 				// task.PageSize, task.Data);
 				task.Observer.updateData(task.Type, null, task.Data);
 			}
 			break;
 		}
 
 		case REQUESTTYPE_GETSMARTCARDINFO:
 		case REQUESTTYPE_MANAGECA:
 		case REQUESTTYPE_GETMAILCONTENT:
 		case REQUESTTYPE_GETPUBLICATIONDRMINFO:
 		case REQUESTTYPE_GETSETTINGS: {
 			if (task.Observer != null) {
 				task.Observer.updateData(task.Type, task.Key, task.Data);
 			}
 			break;
 		}
 
 		default:
 			break;
 		}
 	}
 
 	private void enqueueTask(RequestTask task) {
 		synchronized (mTaskQueueLock) {
 			mTaskQueue.add(task);
 			if (mTaskQueue.size() == 1) {
 				mTaskQueueLock.notifyAll();
 			}
 		}
 	}
 
 	private void enqueueTaskHightPrority(RequestTask task) {
 		synchronized (mTaskQueueLock) {
 			mTaskQueue.addFirst(task);
 			if (mTaskQueue.size() == 1) {
 				mTaskQueueLock.notifyAll();
 			}
 		}
 	}
 
 	private RequestTask dequeueTask() {
 		synchronized (mTaskQueueLock) {
 
 			RequestTask task = mTaskQueue.poll();
 
 			if (task != null && task.Flag == RequestTask.INVALID) {
 				task = null;
 			}
 
 			return task;
 		}
 	}
 
 	private void enqueueFinishedTask(RequestTask task) {
 
 		if (task != null && task.Flag == RequestTask.ACTIVE) {
 			// synchronized (mFinishedTaskQueueLock) {
 			// mFinishedTaskQueue.add(task);
 			// }
 			//
 			// mHandler.sendEmptyMessage(GDCommon.MSG_TASK_FINISHED);
 
 			Message msg = mHandler.obtainMessage(GDCommon.MSG_TASK_FINISHED);
 			msg.obj = task;
 			msg.sendToTarget();
 
 		} else {
 			Log.d(TAG, "taskFinished : invalide task, dropped!");
 		}
 	}
 
 	private RequestTask dequeueFinishedTask() {
 
 		synchronized (mFinishedTaskQueueLock) {
 			RequestTask task = mFinishedTaskQueue.poll();
 
 			if (task != null && task.Flag == RequestTask.INVALID) {
 				task = null;
 			}
 
 			return task;
 		}
 	}
 
 	private class WorkerThread extends Thread {
 
 		private int mThreadId = -1;
 		private int mThreadPriority = -100;
 		private Object mProcessingTaskLock = new Object();
 		private Object mExitLock = new Object();
 		private RequestTask mProcessingTask;
 
 		public boolean mExit = false;
 
 		public void cancelProcessingTask(ClientObserver observer) {
 			synchronized (mProcessingTaskLock) {
 				if (mProcessingTask != null) {
 					if (mProcessingTask.Observer == observer) {
 						mProcessingTask.Flag = RequestTask.INVALID;
 					}
 				}
 			}
 		}
 
 		public void cancelAllProcessingTask() {
 			synchronized (mProcessingTaskLock) {
 				if (mProcessingTask != null) {
 					mProcessingTask.Flag = RequestTask.INVALID;
 				}
 			}
 		}
 
 		private void setProcessingTask(RequestTask task) {
 			synchronized (mProcessingTaskLock) {
 				mProcessingTask = task;
 			}
 		}
 
 		private void taskFinished(RequestTask task) {
 			Log.d(TAG, "Task [" + task.Id + "] Finished - Thread Id ["
 					+ mThreadId + "]");
 
 			enqueueFinishedTask(task);
 			setProcessingTask(null);
 		}
 
 		private boolean checkExit() {
 			synchronized (mExitLock) {
 				return mExit;
 			}
 		}
 
 		public void setExit(boolean exit) {
 			synchronized (mExitLock) {
 				mExit = exit;
 			}
 		}
 
 		public void run() {
 			mThreadId = Process.myTid();
 			mThreadPriority = Process.getThreadPriority(mThreadId);
 			Log.d(TAG, "Worker Thread [" + mThreadId + "] Priority ["
 					+ mThreadPriority + "]");
 
 			Process.setThreadPriority(Process.THREAD_PRIORITY_MORE_FAVORABLE);
 			mThreadPriority = Process.getThreadPriority(mThreadId);
 			Log.d(TAG, "Worker Thread [" + mThreadId + "] Priority ["
 					+ mThreadPriority + "]");
 
 			while (true) {
 				Log.d(TAG, "@@@ 1 Thread [" + mThreadId + "]-- Begin Run");
 
 				if (checkExit()) {
 					break;
 				}
 
 				synchronized (mTaskQueueLock) {
 					if (mTaskQueue.size() == 0) {
 						try {
 							mTaskQueueLock.wait();
 						} catch (Exception e) {
 							e.printStackTrace();
 						}
 					}
 				}
 
 				RequestTask task = dequeueTask();
 
 				if (task == null) {
 					// in this case, we call notify and try to wake up this
 					// thread to let it exit.
 					continue;
 				}
 
 				setProcessingTask(task);
 
 				switch (task.Type) {
 				case REQUESTTYPE_GETCOLUMNS: {
 					Object value = null;
 					value = task.Parameters.get(PARAMETER_COLUMN_ID);
 					String columnId = String.valueOf(value);
 
 					ColumnData[] coloumns = mDataModel.getColumns(columnId);
 
 					for (int i = 0; coloumns != null && i < coloumns.length; i++) {
 						ColumnData column = coloumns[i];
 						String iconRootPath = mConfigure.getIconRootDir();
 						coloumns[i].IconNormal = mDataModel
 								.getImage(iconRootPath + "/"
 										+ column.IconNormalPath);
 						coloumns[i].IconFocused = mDataModel
 								.getImage(iconRootPath + "/"
 										+ column.IconFocusedPath);
 
 						if (coloumns[i].IconNormal == null) {
 							if (mDefaultColumnIconFile == null) {
 								mDefaultColumnIconFile = mDataModel
 										.queryGlobalProperty(GDDVBDataContract.PropertyDefaultColumnIcon);
 							}
 
 							coloumns[i].IconNormal = mDataModel
 									.getImage(iconRootPath + "/"
 											+ mDefaultColumnIconFile);
 						}
 					}
 
 					task.Data = coloumns;
 
 					taskFinished(task);
 					break;
 				}
 				
 				case REQUESTTYPE_GETMOVIECOUNT: {
 					String columnId = (String) task.Key;
 					int count = mDataModel.getPublicationCount(columnId);
 					task.Data = count;
 					taskFinished(task);
 					break;
 				}
 				
 				case REQUESTTYPE_GETTVCOUNT: {
 					String columnId = (String) task.Key;
 					int count = mDataModel.getPublicationSetCount(columnId);
 					task.Data = count;
 					taskFinished(task);
 					break;
 				}
 
 				case REQUESTTYPE_GETPUBLICATION: {
 					Object value = null;
 					value = task.Parameters.get(PARAMETER_COLUMN_ID);
 					String columnId = String.valueOf(value);
 
 					ContentData[] datas = mDataModel.getPublications(columnId,
 							null);
 					task.Data = datas;
 
 					taskFinished(task);
 					break;
 				}
 
 				case REQUESTTYPE_GETPUBLICATIONSET: {
 					Object value = null;
 					value = task.Parameters.get(PARAMETER_COLUMN_ID);
 					String columnId = String.valueOf(value);
 
 					ContentData[] contents = mDataModel.getPublicationSets(
 							columnId, null);
 
 					task.Data = contents;
 
 					taskFinished(task);
 					break;
 				}
 
 				case REQUESTTYPE_GETPUBLICATIONS_OFSET: {
 					Object value = null;
 					value = task.Parameters.get(PARAMETER_SET_ID);
 					String setId = String.valueOf(value);
 
 					ContentData[] contents = mDataModel.getPublicationsBySetId(
 							setId, null);
 
 					if (contents != null && contents.length > 0) {
 						for (int i = 0; i < contents.length; i++) {
 							// String xmlFile = getDetailsDataFile(contents[i]);
 							// mDataModel.getDetailsData(xmlFile, contents[i]);
 
 							mDataModel.getPublicationVAInfo(contents[i]);
 						}
 					}
 
 					task.Data = contents;
 
 					taskFinished(task);
 					break;
 				}
 
 				case REQUESTTYPE_GETFAVORITEMOVIE:
 				case REQUESTTYPE_GETFAVORITETV:
 				case REQUESTTYPE_GETFAVORITERECORD:
 				case REQUESTTYPE_GETFAVORITEENTERTAINMENT: {
 					break;
 				}
 
 				case REQUESTTYPE_GETDETAILSDATA: {
 					Object value = null;
 					value = task.Parameters.get(PARAMETER_CONTENTDATA);
 					ContentData content = (ContentData) value;
 					// String xmlFile = getDetailsDataFile(content);
 					// mDataModel.getDetailsData(xmlFile, content);
 
 					mDataModel.getPublicationVAInfo(content);
 					task.Data = content;
 
 					taskFinished(task);
 					break;
 				}
 
 				case REQUESTTYPE_GETIMAGE: {
 					Object value = null;
 					value = task.Parameters.get(PARAMETER_CONTENTDATA);
 					ContentData content = (ContentData) value;
 					String file = getThumbnailFile(content);
 
 					if (file != null && !file.isEmpty()) {
 						Bitmap image = mDataModel.getImage(file);
 						task.Data = image;
 
 						taskFinished(task);
 					}
 					break;
 				}
 
 				case REQUESTTYPE_STARTGETTASKINFO: {
 					mDBStarClient.startTaskInfo();
 					break;
 				}
 
 				case REQUESTTYPE_STOPGETTASKINFO: {
 					mDBStarClient.stopTaskInfo();
 					break;
 				}
 
 				case REQUESTTYPE_GETDOWNLOADSTATUS: {
 					ReceiveData entries = mDBStarClient.getTaskInfo();
 					task.Data = entries;
 					taskFinished(task);
 					break;
 				}
 
 				case REQUESTTYPE_GETSMARTCARDINFO: {
 					int type = (Integer) task.Key;
 
 					Object data = mDBStarClient.getSmartcardInfo(type);
 
 					task.Data = data;
 					taskFinished(task);
 					break;
 				}
 
 				case REQUESTTYPE_GETMAILCONTENT: {
 					String id = (String) task.Key;
 
 					Object data = mDBStarClient.getEMailContent(id);
 
 					task.Data = data;
 					taskFinished(task);
 					break;
 				}
 				
 				case REQUESTTYPE_GETPUBLICATIONDRMINFO: {
 					String id = (String) task.Key;
 
 					Object data = mDBStarClient.getPublicationDrmInfo(id);
 
 					task.Data = data;
 					taskFinished(task);
 					break;
 				}
 
 				case REQUESTTYPE_MANAGECA: {
 					int type = (Integer) task.Key;
 
 					String result = mDBStarClient.manageCA(type);
 
 					task.Data = result;
 					taskFinished(task);
 					break;
 				}
 
 				case REQUESTTYPE_GETTSSIGNALSTATUS: {
 					String status = mDBStarClient.getTSSignalStatus();
 					task.Data = status;
 					taskFinished(task);
 					break;
 				}
 
 				case REQUESTTYPE_GETSETTINGS: {
 					task.Data = mDataModel.getSettingValue((String) task.Key);
 					taskFinished(task);
 					break;
 				}
 
 				case REQUESTTYPE_SETSETTINGS: {
 					Object value = null;
 					value = task.Parameters.get(PARAMETER_KEY);
 					String key = (String) value;
 					value = task.Parameters.get(PARAMETER_VALUE);
 					String sValue = (String) value;
 
 					mDataModel.setSettingValue(key, sValue);
 					break;
 				}
 
 				case REQUESTTYPE_GETGUIDELIST: {
 					// GuideListItem[] items = mDataModel.getGuideList();
 					GuideListItem[] items = mDataModel.getLatestGuideList();
 					task.Data = items;
 					taskFinished(task);
 					break;
 				}
 
 				case REQUESTTYPE_UPDATEGUIDELIST: {
 					mDataModel.updateGuideList((GuideListItem[]) task.Data);
 					// taskFinished(task);
 					notifyDbstarService(DbstarServiceApi.CMD_PUSH_SELECT);
 					break;
 				}
 
 				case REQUESTTYPE_GETPREVIEWS: {
 					PreviewData[] data = mDataModel.getPreviews();
 
 					ArrayList<PreviewData> previews = new ArrayList<PreviewData>();
 					if (data != null && data.length > 0) {
 
 						for (int i = 0; i < data.length; i++) {
 							String uri = getPreviewFile(data[i]);
 
 							if (uri != null && !uri.isEmpty()) {
 								data[i].FileURI = uri;
 								previews.add(data[i]);
 							}
 						}
 
 						task.Data = previews.toArray(new PreviewData[previews
 								.size()]);
 
 						taskFinished(task);
 					}
 					break;
 				}
 
 				case REQUESTTYPE_GETDEVICEINFO: {
 					Object value = null;
 					value = task.Parameters.get(PARAMETER_KEYS);
 					String[] keys = (String[]) value;
 
 					Map<String, String> data = new HashMap<String, String>();
 					for (int i = 0; i < keys.length; i++) {
 						String propertyValue = mDataModel
 								.queryGlobalProperty(keys[i]);
 						data.put(keys[i], propertyValue);
 					}
 
 					task.Data = data;
 					taskFinished(task);
 					break;
 				}
 
 				default:
 					break;
 				}
 			}
 
 			Log.d(TAG, "Thread [" + mThreadId + "] exit!");
 		}
 	};
 
 	public void getColumns(ClientObserver observer, int level, int index,
 			String columnId) {
 		RequestTask task = new RequestTask();
 		// task.Id = System.currentTimeMillis();
 		task.Observer = observer;
 		task.Type = REQUESTTYPE_GETCOLUMNS;
 		task.Parameters = new HashMap<String, Object>();
 		task.Parameters.put(PARAMETER_COLUMN_ID, columnId);
 		task.Index = index;
 		task.ColumnLevel = level;
 		enqueueTask(task);
 	}
 	
 	public void getMovieCount(ClientObserver observer, String columnId) {
 		RequestTask task = new RequestTask();
 		task.Observer = observer;
 		task.Type = REQUESTTYPE_GETMOVIECOUNT;
 		task.Key = columnId;
 
 		enqueueTask(task);
 	}
 	
 	public void getTVCount(ClientObserver observer, String columnId) {
 		RequestTask task = new RequestTask();
 		task.Observer = observer;
 		task.Type = REQUESTTYPE_GETTVCOUNT;
 		task.Key = columnId;
 
 		enqueueTask(task);
 	}
 
 	public void getPublications(ClientObserver observer, String columnId) {
 		RequestTask task = new RequestTask();
 		// task.Id = System.currentTimeMillis();
 		task.Observer = observer;
 		task.Type = REQUESTTYPE_GETPUBLICATION;
 		task.Parameters = new HashMap<String, Object>();
 		task.Parameters.put(PARAMETER_COLUMN_ID, columnId);
 
 		enqueueTask(task);
 	}
 
 	public void getPublicationSets(ClientObserver observer, String columnId) {
 		RequestTask task = new RequestTask();
 		// task.Id = System.currentTimeMillis();
 		task.Observer = observer;
 		task.Type = REQUESTTYPE_GETPUBLICATIONSET;
 		task.Parameters = new HashMap<String, Object>();
 		task.Parameters.put(PARAMETER_COLUMN_ID, columnId);
 
 		enqueueTask(task);
 	}
 
 	public void getPublicationsOfSet(ClientObserver observer, String setId,
 			int pageNumber, int index) {
 		RequestTask task = new RequestTask();
 		// task.Id = System.currentTimeMillis();
 		task.Observer = observer;
 		task.Type = REQUESTTYPE_GETPUBLICATIONS_OFSET;
 		task.Parameters = new HashMap<String, Object>();
 		task.Parameters.put(PARAMETER_SET_ID, setId);
 		task.PageNumber = pageNumber;
 		task.Index = index;
 
 		enqueueTask(task);
 	}
 
 	public void getDetailsData(ClientObserver observer, int pageNumber,
 			int index, ContentData content) {
 		RequestTask task = new RequestTask();
 		// task.Id = System.currentTimeMillis();
 		task.Observer = observer;
 		task.Type = REQUESTTYPE_GETDETAILSDATA;
 		task.PageNumber = pageNumber;
 		task.Index = index;
 		task.Parameters = new HashMap<String, Object>();
 		task.Parameters.put(PARAMETER_CONTENTDATA, content);
 		enqueueTask(task);
 	}
 
 	public void getImage(ClientObserver observer, int pageNumber, int index,
 			ContentData content) {
 		RequestTask task = new RequestTask();
 		// task.Id = System.currentTimeMillis();
 		task.Observer = observer;
 		task.Type = REQUESTTYPE_GETIMAGE;
 		task.PageNumber = pageNumber;
 		task.Index = index;
 		task.Parameters = new HashMap<String, Object>();
 		task.Parameters.put(PARAMETER_CONTENTDATA, content);
 		// enqueueTask(task);
 		enqueueTaskHightPrority(task);
 	}
 
 	public void getSettingsValue(ClientObserver observer, String key) {
 		RequestTask task = new RequestTask();
 		// task.Id = System.currentTimeMillis();
 		task.Observer = observer;
 		task.Type = REQUESTTYPE_GETSETTINGS;
 		task.Key = key;
 
 		enqueueTask(task);
 	}
 
 	public void setSettingsValue(String key, String value) {
 		RequestTask task = new RequestTask();
 		// task.Id = System.currentTimeMillis();
 		task.Type = REQUESTTYPE_SETSETTINGS;
 
 		task.Parameters = new HashMap<String, Object>();
 		task.Parameters.put(PARAMETER_KEY, key);
 		task.Parameters.put(PARAMETER_VALUE, value);
 
 		enqueueTask(task);
 	}
 
 	public void getDeviceInfo(ClientObserver observer, String[] keys) {
 		RequestTask task = new RequestTask();
 		task.Observer = observer;
 		task.Type = REQUESTTYPE_GETDEVICEINFO;
 
 		task.Parameters = new HashMap<String, Object>();
 		task.Parameters.put(PARAMETER_KEYS, keys);
 
 		enqueueTask(task);
 	}
 
 	public void startGetTaskInfo() {
 
 		RequestTask task = new RequestTask();
 		task.Observer = null;
 		// task.Id = System.currentTimeMillis();
 		task.Type = REQUESTTYPE_STARTGETTASKINFO;
 
 		enqueueTask(task);
 	}
 
 	public void getDownloadStatus(ClientObserver observer) {
 		RequestTask task = new RequestTask();
 		// task.Id = System.currentTimeMillis();
 		task.Observer = observer;
 		task.Type = REQUESTTYPE_GETDOWNLOADSTATUS;
 
 		// task.PageNumber = pageNumber;
 		// task.PageSize = pageSize;
 		enqueueTask(task);
 	}
 
 	public void getTSSignalStatus(ClientObserver observer) {
 		RequestTask task = new RequestTask();
 		task.Observer = observer;
 		task.Type = REQUESTTYPE_GETTSSIGNALSTATUS;
 
 		enqueueTask(task);
 	}
 
 	public void stopGetTaskInfo() {
 		RequestTask task = new RequestTask();
 		task.Observer = null;
 		// task.Id = System.currentTimeMillis();
 		task.Type = REQUESTTYPE_STOPGETTASKINFO;
 
 		enqueueTask(task);
 	}
 
 	public void getSmartcardInfo(ClientObserver observer, int type) {
 		RequestTask task = new RequestTask();
 		task.Observer = observer;
 		task.Type = REQUESTTYPE_GETSMARTCARDINFO;
 		task.Key = new Integer(type);
 
 		enqueueTask(task);
 	}
 
 	public void getMailContent(ClientObserver observer, String id) {
 		RequestTask task = new RequestTask();
 		task.Observer = observer;
 		task.Type = REQUESTTYPE_GETMAILCONTENT;
 		task.Key = id;
 
 		enqueueTask(task);
 	}
 	
 	public void getPublicationDrmInfo(ClientObserver observer, String id) {
 		RequestTask task = new RequestTask();
 		task.Observer = observer;
 		task.Type = REQUESTTYPE_GETPUBLICATIONDRMINFO;
 		task.Key = id;
 
 		enqueueTask(task);
 	}
 
 	public void manageCA(ClientObserver observer, int cmd) {
 		RequestTask task = new RequestTask();
 		task.Observer = observer;
 		task.Type = REQUESTTYPE_MANAGECA;
 		task.Key = new Integer(cmd);
 
 		enqueueTask(task);
 	}
 
 	// favorite
 	public void getFavoriteMovie(ClientObserver observer) {
 		RequestTask task = new RequestTask();
 		// task.Id = System.currentTimeMillis();
 		task.Observer = observer;
 		task.Type = REQUESTTYPE_GETFAVORITEMOVIE;
 
 		enqueueTask(task);
 	}
 
 	public void getFavoriteTV(ClientObserver observer) {
 		RequestTask task = new RequestTask();
 		// task.Id = System.currentTimeMillis();
 		task.Observer = observer;
 		task.Type = REQUESTTYPE_GETFAVORITETV;
 
 		enqueueTask(task);
 	}
 
 	public void getAllGuideList(ClientObserver observer) {
 		RequestTask task = new RequestTask();
 		// task.Id = System.currentTimeMillis();
 		task.Observer = observer;
 		task.Type = REQUESTTYPE_GETGUIDELIST;
 
 		enqueueTask(task);
 	}
 
 	public void updateGuideList(ClientObserver observer, GuideListItem[] items) {
 
 		RequestTask task = new RequestTask();
 		// task.Id = System.currentTimeMillis();
 		task.Observer = observer;
 		task.Data = items;
 		task.Type = REQUESTTYPE_UPDATEGUIDELIST;
 
 		enqueueTask(task);
 	}
 
 	public void getPreviews(ClientObserver observer) {
 		RequestTask task = new RequestTask();
 		task.Observer = observer;
 		task.Type = REQUESTTYPE_GETPREVIEWS;
 
 		enqueueTask(task);
 	}
 	
 	public void deletePublicationSet(String publicationSetId) {
 		mDataModel.deletePublicationSet(publicationSetId);
 	}
 
 	public boolean isSmartcardPlugIn() {
 		return mPeripheralController.isSmartCardIn();
 	}
 	
 	public boolean isSmartcardReady() {
 		return mSmartcardState == GDCommon.SMARTCARD_STATE_INSERTED;
 	}
 
 	public int getSmartcardState() {
 		return mSmartcardState;
 	}
 	
 	public boolean isMute() {
 		return mAudioController.isMute();
 	}
 
 	private String getThumbnailFile(ContentData content) {
 		return mConfigure.getThumbnailFile(content);
 	}
 
 	private String getDetailsDataFile(ContentData content) {
 		return mConfigure.getDetailsDataFile(content);
 	}
 
 	public String getMediaFile(ContentData content) {
 		return mConfigure.getMediaFile(content);
 	}
 
 	public String getPreviewFile(PreviewData data) {
 		return mConfigure.getPreviewFile(data);
 	}
 
 	public String getDRMFile(ContentData content) {
 		return mConfigure.getDRMFile(content);
 	}
 
 	public String getEBookFile(String category) {
 		return mConfigure.getEbookFile(category);
 	}
 
 	public String getStorageDisk() {
 		return mConfigure.getStorageDisk();
 	}
 
 	public PreviewData[] getPreviewFiles() {
 		PreviewData[] previews = null;
 		String path = mDataModel.getPreviewPath();
 		if (path == null || path.isEmpty()) {
 			return previews;
 		}
 
 		Log.d(TAG, "path= " + path);
 		File dir = new File(path);
 		if (dir == null || !dir.exists()) {
 			return previews;
 		}
 
 		File[] files = dir.listFiles();
 		if (files != null && files.length > 0) {
 			PreviewData[] data = new PreviewData[files.length];
 			for (int i = 0; i < data.length; i++) {
 				PreviewData item = new PreviewData();
 				item.URI = files[i].getAbsolutePath();
 				item.Type = PreviewData.TypeVideo;
 				data[i] = item;
 			}
 
 			previews = data;
 		}
 
 		return previews;
 	}
 
 	public void sendSimulateKey(int keyCode) {
 		Message msg = mPowerHandler.obtainMessage(POWERMANAGER_MSG_SIMULATEKEY);
 		msg.arg1 = keyCode;
         msg.sendToTarget();
 	}
 
 	// cancel the requests from this observer
 	public void cancelRequests(ClientObserver observer) {
 		synchronized (mTaskQueueLock) {
 			for (int i = 0; i < mTaskQueue.size(); i++) {
 				RequestTask task = mTaskQueue.get(i);
 				if (task.Observer == observer) {
 					task.Flag = RequestTask.INVALID;
 				}
 			}
 		}
 
 		for (int i = 0; i < mThreadPool.size(); i++) {
 			WorkerThread thread = mThreadPool.get(i);
 			thread.cancelProcessingTask(observer);
 		}
 
 		synchronized (mFinishedTaskQueueLock) {
 			for (int i = 0; i < mFinishedTaskQueue.size(); i++) {
 				RequestTask task = mFinishedTaskQueue.get(i);
 				if (task.Observer == observer) {
 					task.Flag = RequestTask.INVALID;
 				}
 			}
 		}
 	}
 
 	public String getCategoryContent(String category) {
 		return mConfigure.getCategoryContent(category);
 	}
 
 	public void getPushedMessage(List<String> retMessages) {
 		mConfigure.getPushedMessage(retMessages);
 	}
 
 	void queryDiskGuardSize() {
 		String value = mDataModel
 				.queryGlobalProperty(GDDVBDataContract.PropertyDiskGuardSize);
 		if (value != null && !value.isEmpty()) {
 			long guardSize = Integer.valueOf(value).longValue();
 			if (guardSize > 0) {
 				guardSize = guardSize * 1024 * 1024;
 				//mDiskMonitor.setGuardSize(guardSize);
 			}
 		}
 	}
 
 	public void cancelAllRequests() {
 		Log.d(TAG, "cancelAllRequests!");
 
 		synchronized (mTaskQueueLock) {
 			mTaskQueue.clear();
 		}
 
 		for (int i = 0; i < mThreadPool.size(); i++) {
 			WorkerThread thread = mThreadPool.get(i);
 			thread.cancelAllProcessingTask();
 		}
 
 		synchronized (mFinishedTaskQueueLock) {
 			mFinishedTaskQueue.clear();
 		}
 	}
 
 	public boolean isNetworkConnected() {
 		NetworkInfo networkInfo = mConnectManager.getActiveNetworkInfo();
 
 		if (networkInfo != null) {
 			Log.d(TAG,
 					" === connected netwrok === type = "
 							+ networkInfo.getType());
 		}
 
 		return networkInfo != null && networkInfo.isConnected();
 	}
 
 	private void registerUSBReceiver() {
 		IntentFilter usbFilter = new IntentFilter();
 		/* receive USB status change messages */
 		usbFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
 		usbFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
 		// usbFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
 		usbFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
 		usbFilter.addDataScheme("file");
 
 		registerReceiver(mUSBReceiver, usbFilter);
 	}
 
 	private void reqisterConnectReceiver() {
 		mConnectManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		IntentFilter networkFilter = new IntentFilter();
 		/* receive connection change messages */
 		networkFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
 		registerReceiver(mNetworkReceiver, networkFilter);
 	}
 
 	private void reqisterSystemMessageReceiver() {
 		IntentFilter filter = new IntentFilter();
 		filter.addAction(DbstarServiceApi.ACTION_NOTIFY);
 		filter.addAction(GDCommon.ActionAddFavourite);
 		filter.addAction(GDCommon.ActionDelete);
 		filter.addAction(GDCommon.ActionBookmark);
 		filter.addAction(GDCommon.ActionUpgradeCancelled);
 		filter.addAction(GDCommon.ActionPlayCompleted);
 
 		filter.addAction(GDCommon.ActionGetNetworkInfo);
 		filter.addAction(GDCommon.ActionSetNetworkInfo);
 		filter.addAction(GDCommon.ActionGetEthernetInfo);
 
 		filter.addAction(GDCommon.ActionScreenOn);
 		filter.addAction(GDCommon.ActionScreenOff);
 
 		filter.addAction(DbstarServiceApi.ACTION_HDMI_IN);
 		filter.addAction(DbstarServiceApi.ACTION_HDMI_OUT);
 
 		filter.addAction(DbstarServiceApi.ACTION_SMARTCARD_IN);
 		filter.addAction(DbstarServiceApi.ACTION_SMARTCARD_OUT);
 		filter.addAction(GDCommon.ACTION_BOOT_COMPLETED);
 		
 		filter.addAction(GDCommon.ActionClearSettings);
 		filter.addAction(GDCommon.ActionSystemRecovery);
 		filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
 		filter.addAction(GDCommon.ActionChannelModeChange);
 
 		registerReceiver(mSystemMessageReceiver, filter);
 	}
 
 	private BroadcastReceiver mUSBReceiver = new BroadcastReceiver() {
 
 		public void onReceive(Context context, Intent intent) {
 			String action = intent.getAction();
 			Uri uri = intent.getData();
 			Log.d(TAG, "---- USB device " + action);
 			Log.d(TAG, "---- URI:" + uri.toString());
 
 			if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
 				Message message = mHandler
 						.obtainMessage(GDCommon.MSG_MEDIA_MOUNTED);
 				Bundle data = new Bundle();
 				data.putString("disk", uri.getPath());
 				message.setData(data);
 
 				mHandler.sendMessage(message);
 			} else if (action.equals(Intent.ACTION_MEDIA_REMOVED)
 					|| action.equals(Intent.ACTION_MEDIA_BAD_REMOVAL)
 			/* ||action.equals(Intent.ACTION_MEDIA_UNMOUNTED) */) {
 
 				Message message = mHandler
 						.obtainMessage(GDCommon.MSG_MEDIA_REMOVED);
 				Bundle data = new Bundle();
 				data.putString("disk", uri.getPath());
 				message.setData(data);
 
 				mHandler.sendMessage(message);
 			}
 		}
 
 	};
 
 	// In dual network device (wifi and ethernet) mode,
 	// when ethernet is connected, this callback is called.
 	private void ethernetConnected() {
 		Log.d(TAG, "ethernetConnected");
 	
 //		if (mIsWirelessConnected) {
 //			// remove Ethernet gateway.
 //			NativeUtil.shell("ip route del dev eth0");
 //		}
 		
 		if (mChannelMode == GDCommon.EthernetMode) {
 			return;
 		}
 
 		Message msg = mHandler.obtainMessage(GDCommon.MSG_NETWORK_CONNECT);
 		msg.arg1 = GDCommon.TypeEthernet;
 		msg.sendToTarget();
 	}
 	
 	void ethernetDisconnected() {
 		Log.d(TAG, "ethernetDisconnected");
 		
 		if (mChannelMode == GDCommon.EthernetMode) {
 			return;
 		}
 		
		Message msg = mHandler.obtainMessage(GDCommon.MSG_NETWORK_DISCONNECT);
 		msg.arg1 = GDCommon.TypeEthernet;
 		msg.sendToTarget();
 	}
 
 	private int mChannelMode = 0;
 
 	private BroadcastReceiver mNetworkReceiver = new BroadcastReceiver() {
 
 		public void onReceive(Context context, Intent intent) {
 			String action = intent.getAction();
 
 			Log.d(TAG, "ConnectivityManager Action: " + action);
 
 			if (!action.equals(ConnectivityManager.CONNECTIVITY_ACTION))
 				return;
 
 			int channelMode = mChannelMode;
 			
 			//if (ethernetConnected && wirelessConnected) {
 				// remove Ethernet gateway.
 			//	NativeUtil.shell("ip route del dev eth0");
 			//}
 			
 			if (channelMode == GDCommon.EthernetMode) {
 				// single card
 				boolean noConnectivity = intent.getBooleanExtra(
 						ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
 				Log.d(TAG, "noConnectivity = " + noConnectivity);
 
 				if (noConnectivity) {
 					// There are no connected networks at all
 					int msgId = GDCommon.MSG_NETWORK_DISCONNECT;
 					Message msg = mHandler.obtainMessage(msgId);
 					msg.arg1 = GDCommon.TypeEthernet;
 					msg.sendToTarget();
 					return;
 				}
 
 				// case 1: attempting to connect to another network, just wait for
 				// another broadcast
 				// case 2: connected
 
 				NetworkInfo networkInfo = mConnectManager.getActiveNetworkInfo();				
 				if (networkInfo != null) {
 					if (networkInfo.getType() == ConnectivityManager.TYPE_ETHERNET
 							&& networkInfo.isConnected()) {
 						int msgId = GDCommon.MSG_NETWORK_CONNECT;
 						Message msg = mHandler.obtainMessage(msgId);
 						msg.arg1 = GDCommon.TypeEthernet;
 						msg.sendToTarget();
 					}
 				}
 				
 			} else {
 				// dual card
 				boolean wirelessConnected = false;
 				NetworkInfo networkInfo = null;
 
 				networkInfo = mConnectManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
 				if (networkInfo != null && networkInfo.isConnected()) {
 					Log.d(TAG, "wifi connected");
 					wirelessConnected = true;
 				} else {
 					Log.d(TAG, "wifi disconnected");
 					wirelessConnected = false;
 				}
 
 				int msgId = GDCommon.MSG_NETWORK_DISCONNECT;
 				if (wirelessConnected) {
 					msgId = GDCommon.MSG_NETWORK_CONNECT;
 				}
 
 				Message msg = mHandler.obtainMessage(msgId);
 				msg.arg1 = GDCommon.TypeWifi;
 				msg.sendToTarget();
 			}
 
 		}
 
 	};
 	
 	static String getStringData (Intent intent, String charset) {
 		String info = null;
 
 		byte[] bytes = intent.getByteArrayExtra("message");
 		if (bytes != null) {
 			try {
 				info = new String(bytes, charset);
 			} catch (UnsupportedEncodingException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		return info;
 	}
 
 	private BroadcastReceiver mSystemMessageReceiver = new BroadcastReceiver() {
 
 		public void onReceive(Context context, Intent intent) {
 			String action = intent.getAction();
 
 			Log.d(TAG, "onReceive System msg " + action);
 
 			if (action.equals(DbstarServiceApi.ACTION_NOTIFY)) {
 
 				int type = intent.getIntExtra("type", 0);
 				Log.d(TAG, "onReceive type " + type);
 
 				switch (type) {
 				case DbstarServiceApi.UPGRADE_NEW_VER_FORCE:
 				case DbstarServiceApi.UPGRADE_NEW_VER: {
 					String packageFile = getStringData(intent, "utf-8");
 					Log.d(TAG, "onReceive packageFile " + packageFile);
 					
 					if (packageFile != null) {
 						int msgId = 0;
 						if (type == DbstarServiceApi.UPGRADE_NEW_VER) {
 							msgId = GDCommon.MSG_SYSTEM_UPGRADE;
 						} else if (type == DbstarServiceApi.UPGRADE_NEW_VER_FORCE) {
 							msgId = GDCommon.MSG_SYSTEM_FORCE_UPGRADE;
 						} else {
 							;
 						}
 
 						Message msg = mHandler.obtainMessage(msgId);
 						msg.obj = packageFile;
 						mHandler.sendMessage(msg);
 					}
 					break;
 				}
 
 				case DbstarServiceApi.DIALOG_NOTICE: {
 					byte[] bytes = intent.getByteArrayExtra("message");
 
 					Log.d(TAG, "=======receive notification " + bytes);
 
 					if (bytes != null) {
 						String info = StringUtil.getString(bytes, "utf-8");
 
 						Log.d(TAG, "======= notification " + info);
 
 						Message msg = mHandler
 								.obtainMessage(GDCommon.MSG_DISP_NOTIFICATION);
 						msg.obj = info;
 						mHandler.sendMessage(msg);
 					}
 					break;
 				}
 				
 				case DbstarServiceApi.DRM_OSD_SHOW: {
 					byte[] bytes = intent.getByteArrayExtra("message");
 
 					if (bytes != null) {
 						String data = StringUtil.getString(bytes, "gb2312");
 						Message msg = mHandler
 								.obtainMessage(GDCommon.MSG_DISP_NOTIFICATION);
 						msg.obj = data;
 						mHandler.sendMessage(msg);
 					}
 					break;
 				}
 				
 				case DbstarServiceApi.DRM_OSD_HIDE: {
 					mHandler.sendEmptyMessage(GDCommon.MSG_HIDE_NOTIFICATION);
 					break;
 				}
 
 				case DbstarServiceApi.STATUS_DVBPUSH_INIT_SUCCESS: {
 					mIsDbServiceStarted = true;
 
 					Log.d(TAG,
 							" ========== DbstarServer init success ===========");
 					if (mDBStarClient.isBoundToServer()) {
 						mHandler.sendEmptyMessage(GDCommon.SYNC_STATUS_TODBSERVER);
 					}
 					break;
 				}
 
 				case DbstarServiceApi.STATUS_DVBPUSH_INIT_FAILED: {
 					mIsDbServiceStarted = false;
 
 					Log.d(TAG,
 							" ========== DbstarServer init failed ===========");
 					break;
 				}
 
 				case DbstarServiceApi.STATUS_DATA_SIGNAL_ON:
 				case DbstarServiceApi.STATUS_DATA_SIGNAL_OFF: {
 					Message msg = mHandler
 							.obtainMessage(GDCommon.MSG_DATA_SIGNAL_STATUS);
 					int hasSignal = type == DbstarServiceApi.STATUS_DATA_SIGNAL_ON ? GDCommon.STATUS_HASSIGNAL
 							: GDCommon.STATUS_NOSIGNAL;
 					msg.arg1 = hasSignal;
 					mHandler.sendMessage(msg);
 					break;
 				}
 
 				case DbstarServiceApi.STATUS_COLUMN_REFRESH: {
 					mHandler.sendEmptyMessage(GDCommon.MSG_UPDATE_COLUMN);
 					break;
 				}
 
 				case DbstarServiceApi.STATUS_PREVIEW_REFRESH: {
 					mHandler.sendEmptyMessage(GDCommon.MSG_UPDATE_PREVIEW);
 					break;
 				}
 
 				case DbstarServiceApi.STATUS_INTERFACE_REFRESH: {
 					mHandler.sendEmptyMessage(GDCommon.MSG_UPDATE_UIRESOURCE);
 					break;
 				}
 
 				case DbstarServiceApi.DRM_SC_INSERT_OK: {
 					mHandler.sendEmptyMessage(GDCommon.MSG_SMARTCARD_INSERT_OK);
 					break;
 				}
 				case DbstarServiceApi.DRM_SC_INSERT_FAILED: {
 					mHandler.sendEmptyMessage(GDCommon.MSG_SMARTCARD_INSERT_FAILED);
 					break;
 				}
 				case DbstarServiceApi.DRM_SC_REMOVE_OK: {
 					mHandler.sendEmptyMessage(GDCommon.MSG_SMARTCARD_REMOVE_OK);
 					break;
 				}
 				case DbstarServiceApi.DRM_SC_REMOVE_FAILED: {
 					mHandler.sendEmptyMessage(GDCommon.MSG_SMARTCARD_REMOVE_FAILED);
 					break;
 				}
 
 				case DbstarServiceApi.DRM_EMAIL_NEW: {
 					mHandler.sendEmptyMessage(GDCommon.MSG_NEW_MAIL);
 					break;
 				}
 
 				case DbstarServiceApi.TDT_TIME_SYNC: {
 					byte[] bytes = intent.getByteArrayExtra("message");
 					if (bytes != null) {
 						try {
 							String time = new String(bytes, "utf-8");
 							Log.d(TAG, "==========handle TDT time ======== "
 									+ time);
 							long tdtTime = Long.parseLong(time);
 							TDTTimeController.handleTDTTime(tdtTime * 1000L);
 						} catch (UnsupportedEncodingException e) {
 							e.printStackTrace();
 						}
 					}
 
 					break;
 				}
 
 				case DbstarServiceApi.MOTHER_DISK_INITIALIZE_START:
 				case DbstarServiceApi.MOTHER_DISK_INITIALIZE_PROCESS:
 				case DbstarServiceApi.MOTHER_DISK_INITIALIZE_FAILED:
 				case DbstarServiceApi.MOTHER_DISK_INITIALIZE_SUCCESS: {
 					String info = getStringData(intent, "utf-8");
 					Message msg = mHandler.obtainMessage(GDCommon.MSG_DISK_INITIALIZE);
 					msg.arg1 = type;
 					msg.obj = info;
 					msg.sendToTarget();
 					break;
 				}
 				
 				case DbstarServiceApi.DEVICE_INIT_SUCCESS:
 				case DbstarServiceApi.DEVICE_INIT_FAILED: {
 					mHandler.sendEmptyMessage(GDCommon.MSG_DEVICE_INIT_FINISHED);
 					break;
 				}
 				case DbstarServiceApi.SYSTEM_REBOOT: {
 					Log.d(TAG, " ========== DbstarServer request SYSTEM_REBOOT ===========");
 					mHandler.sendEmptyMessage(GDCommon.MSG_SYSTEM_REBOOT);
 					break;
 				}
 
 				default:
 					break;
 				}
 
 			} else if (action.equals(GDCommon.ActionAddFavourite)) {
 				String publicationSetId = intent
 						.getStringExtra(GDCommon.KeyPublicationSetID);
 				String publicationId = intent
 						.getStringExtra(GDCommon.KeyPublicationID);
 
 				Message msg = mHandler
 						.obtainMessage(GDCommon.MSG_ADD_TO_FAVOURITE);
 				Bundle data = new Bundle();
 				data.putString(GDCommon.KeyPublicationID, publicationId);
 				data.putString(GDCommon.KeyPublicationSetID, publicationSetId);
 				msg.setData(data);
 				mHandler.sendMessage(msg);
 
 			} else if (action.equals(GDCommon.ActionDelete)) {
 				String publicationSetId = intent
 						.getStringExtra(GDCommon.KeyPublicationSetID);
 				String publicationId = intent
 						.getStringExtra(GDCommon.KeyPublicationID);
 
 				Message msg = mHandler.obtainMessage(GDCommon.MSG_DELETE);
 				Bundle data = new Bundle();
 				data.putString(GDCommon.KeyPublicationID, publicationId);
 				data.putString(GDCommon.KeyPublicationSetID, publicationSetId);
 				msg.setData(data);
 				mHandler.sendMessage(msg);
 			} else if (action.equals(GDCommon.ActionUpgradeCancelled)) {
 				String packageFile = intent
 						.getStringExtra(GDCommon.KeyPackgeFile);
 				Message msg = mHandler
 						.obtainMessage(GDCommon.MSG_USER_UPGRADE_CANCELLED);
 				msg.obj = packageFile;
 				mHandler.sendMessage(msg);
 			} else if (action.equals(GDCommon.ActionBookmark)) {
 				String publicationId = intent
 						.getStringExtra(GDCommon.KeyPublicationID);
 				int bookmark = intent.getIntExtra(GDCommon.KeyBookmark, 0);
 
 				Message msg = mHandler
 						.obtainMessage(GDCommon.MSG_SAVE_BOOKMARK);
 				Bundle data = new Bundle();
 				data.putString(GDCommon.KeyPublicationID, publicationId);
 				data.putInt(GDCommon.KeyBookmark, bookmark);
 				msg.setData(data);
 
 				mHandler.sendMessage(msg);
 			} else if (action.equals(GDCommon.ActionPlayCompleted)) {
 				mHandler.sendEmptyMessage(GDCommon.MSG_PLAY_COMPLETED);
 			} else if (action.equals(GDCommon.ActionGetNetworkInfo)) {
 				mHandler.sendEmptyMessage(GDCommon.MSG_GET_NETWORKINFO);
 			} else if (action.equals(GDCommon.ActionSetNetworkInfo)) {
 				String gatewaySerialNumber = intent
 						.getStringExtra(GDSettings.PropertyGatewaySerialNumber);
 				String gatewayIP = intent
 						.getStringExtra(GDSettings.PropertyGatewayIP);
 				String gatewayPort = intent
 						.getStringExtra(GDSettings.PropertyGatewayPort);
 				String multicastIP = intent
 						.getStringExtra(GDSettings.PropertyMulticastIP);
 				String multicastPort = intent
 						.getStringExtra(GDSettings.PropertyMulticastPort);
 
 				Bundle data = new Bundle();
 				data.putString(GDSettings.PropertyGatewaySerialNumber,
 						gatewaySerialNumber);
 				data.putString(GDSettings.PropertyGatewayIP, gatewayIP);
 				data.putString(GDSettings.PropertyGatewayPort, gatewayPort);
 				data.putString(GDSettings.PropertyMulticastIP, multicastIP);
 				data.putString(GDSettings.PropertyMulticastPort, multicastPort);
 
 				Message msg = mHandler
 						.obtainMessage(GDCommon.MSG_SET_NETWORKINFO);
 				msg.setData(data);
 				mHandler.sendMessage(msg);
 			} else if (action.equals(GDCommon.ActionScreenOn)) {
 				setSleepMode(true);
 				upgradeAfterSleep();
 			} else if (action.equals(GDCommon.ActionScreenOff)) {
 				setSleepMode(false);
 				mPeripheralController.setPowerLedOff();
 			} else if (action.equals(DbstarServiceApi.ACTION_HDMI_IN)) {
 				mPeripheralController.setAudioOutputOff();
 			} else if (action.equals(DbstarServiceApi.ACTION_HDMI_OUT)) {
 				mPeripheralController.setAudioOutputOn();
 			} else if (action.equals(DbstarServiceApi.ACTION_SMARTCARD_IN)) {
 				Log.d(TAG, "######: " + action);
 				mHandler.sendEmptyMessage(GDCommon.MSG_SMARTCARD_IN);
 			} else if (action.equals(DbstarServiceApi.ACTION_SMARTCARD_OUT)) {
 				Log.d(TAG, "######: " + action);
 				mHandler.sendEmptyMessage(GDCommon.MSG_SMARTCARD_OUT);
 			} else if (action.equals(GDCommon.ActionGetEthernetInfo)) {
 				mHandler.sendEmptyMessage(GDCommon.MSG_GET_ETHERNETINFO);
 			} else if (action.equals(GDCommon.ActionSystemRecovery)) {
 				int type = intent.getIntExtra(GDCommon.KeyRecoveryType, 0);
 				Message msg = mHandler.obtainMessage(GDCommon.MSG_SYSTEM_RECOVERY);
 				msg.arg1 = type;
 				msg.sendToTarget();
 			} else if (action.equals(GDCommon.ActionClearSettings)) {
 				DeviceInitController.clearSettings();
 			} else if (action.equals(GDCommon.ACTION_BOOT_COMPLETED)) {
 				mHandler.sendEmptyMessage(GDCommon.MSG_BOOT_COMPLETED);
 			} else if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
 				mHandler.sendEmptyMessage(GDCommon.MSG_HOMEKEY_PRESSED);
 			} else if (action.equals(GDCommon.ActionChannelModeChange)) {
 				String channel = intent.getStringExtra(GDCommon.KeyChannel);
 				if (channel != null && channel.length() > 0) {
 					mChannelMode = channel.equals(GDCommon.ChannelEthernet) ? GDCommon.EthernetMode : GDCommon.WirelessMode;
 				}
 			}
 		}
 	};
 	
 	void handleRecoveryAction(int type) {
 		switch(type) {
 		case GDCommon.RecoveryTypeClearPush: {
 			notifyDbstarService(DbstarServiceApi.CMD_FACTORY_RESET);
 			break;
 		}
 		case GDCommon.RecoveryTypeClearDrmInfo: {
 			notifyDbstarService(DbstarServiceApi.CMD_DRM_RESET);
 			break;
 		}
 		case GDCommon.RecoveryTypeFormatDisk: {
 			notifyDbstarService(DbstarServiceApi.CMD_DISK_FORMAT);
 			formatDisk();
 			break;
 		}
 
 		}
 	}
 	
 	private DiskFormatter mFormatter;
 	
 	void formatDisk() {
 		mFormatter = new DiskFormatter();
 //		mFormatter.startFormat(mConfigure.getStorageDisk(), mHandler);
 		mFormatter.startFormatDisk(mConfigure.getStorageDisk(), mHandler);
 	}
 	
 	// if format failed, errorMsg is the message.
 	void handleDiskFormatResult(boolean successed, String errorMsg) {
 		if (mPageOberser != null) {
 			EventData.DiskFormatEvent event = new EventData.DiskFormatEvent();
 			event.Successed = successed;
 			event.ErrorMessage = errorMsg;
 			mPageOberser.notifyEvent(EventData.EVENT_DISK_FORMAT, event);			
 		}
 		
 		mFormatter.finishFormatDisk();
 	}
 
 	int getWakeupTime() {
 		int secs = 0;
         secs = mDBStarClient.getWakeupTime();
         Log.d(TAG, "-------- getWakeupTime(), secs= " + secs);
 		return secs;
 	}
 
 	void setSleepMode(boolean on) {
 		Log.d(TAG, "SLEEPMODE --- screenOn: " + on);
 		if (on) {
 			if (mSleepMode == POWERMANAGER_MODE_SCREENOFF) {
 				Log.d(TAG, "--- PowerMode: SCREENOFF-->RUNNING");
 				mSleepMode = POWERMANAGER_MODE_RUNNING;
 				mPowerManager.clearAlarm();
 				stopPowerTask();
 			} else if (mSleepMode == POWERMANAGER_MODE_SLEEP) { 
 				if (mPowerManager.getAlarm() > 0) {
 					Log.d(TAG, "--- PowerMode: SLEEP-->RUNNING");
 					mSleepMode = POWERMANAGER_MODE_RUNNING;
 					mPowerManager.clearAlarm();
 					stopPowerTask();
 				} else {
 					Log.d(TAG, "--- PowerMode: SLEEP-->SCREENOFF");
 					Message msg = mPowerHandler.obtainMessage(POWERMANAGER_MSG_POWERKEY);
 					msg.sendToTarget();
 				}
 			}
 		} else {
 			if (mSleepMode == POWERMANAGER_MODE_RUNNING) {
 				Log.d(TAG, "--- PowerMode: RUNNING-->SCREENOFF");
 				mSleepMode = POWERMANAGER_MODE_SCREENOFF;
 				mPowerManager.acquirePartialWakeLock(this);
 				startPowerTask();
 			} else if (mSleepMode == POWERMANAGER_MODE_SLEEP) {
 				Log.d(TAG, "--- PowerMode: SLEEP-->SCREENOFF");
 				mSleepMode = POWERMANAGER_MODE_SCREENOFF;
 				mPowerManager.acquirePartialWakeLock(this);
 			}
 		}
 	}
 
 	void upgradeAfterSleep() {
 		if (mNeedUpgrade) {
 			RebootUtils.rebootInstallPackage(this, mUpgradePackageFile);
 		}
 	}
 
 	boolean notifyDbstarServiceNetworkStatus() {
 		Log.d(TAG, "NETWORK --- notifyDbstarServiceNetworkStatus: dvb started "
 				+ mIsDbServiceStarted);
 
 		if (!mIsDbServiceStarted)
 			return false;
 
 		if (mEthernetController.isEthernetPhyConnected()) {
 			mDBStarClient.notifyDbServer(DbstarServiceApi.CMD_NETWORK_CONNECT, null);
 		} else {
 			mDBStarClient
 					.notifyDbServer(DbstarServiceApi.CMD_NETWORK_DISCONNECT, null);
 		}
 
 		return true;
 	}
 
 	boolean notifyDbstarServiceStorageStatus(String disk) {
 
 		Log.d(TAG, "STORAGE -- notifyDbstarServiceStorageStatus: dvb started "
 				+ mIsDbServiceStarted);
 
 		if (!mIsDbServiceStarted)
 			return false;
 
 		// TODO: At this point, the disk maybe not mount now.
 		if (mIsStorageReady) {
 			mDBStarClient.notifyDbServer(DbstarServiceApi.CMD_DISK_MOUNT, disk);
 		} else {
 			mDBStarClient.notifyDbServer(DbstarServiceApi.CMD_DISK_UNMOUNT, disk);
 		}
 
 		return true;
 	}
 	
 	boolean notifyDbstarServiceDeviceInit() {
 
 		Log.d(TAG, "STORAGE -- notifyDbstarServiceStorageStatus: dvb started "
 				+ mIsDbServiceStarted);
 
 		if (!mIsDbServiceStarted)
 			return false;
 
 		mDBStarClient.notifyDbServer(DbstarServiceApi.CMD_DEVICE_INIT, null);
 
 		return true;
 	}
 	
 
 	boolean notifyDbstarServiceSDStatus() {
 
 		Log.d(TAG, "SMARTCARD --- notifyDbstarServiceSDStatus: dvb started "
 				+ mIsDbServiceStarted);
 
 		if (!mIsDbServiceStarted)
 			return false;
 
 		if (mPeripheralController.isSmartCardIn()) {
 			mDBStarClient.notifyDbServer(DbstarServiceApi.CMD_DRM_SC_INSERT, null);
 		} else {
 			mDBStarClient.notifyDbServer(DbstarServiceApi.CMD_DRM_SC_REMOVE, null);
 		}
 
 		return true;
 	}
 
 	// Call this when:
 	// 1. dbstarDVB init ok;
 	// 2. sdcard, network, or storage state changed.
 	private void syncStatusToDbServer() {
 		Log.d(TAG, "syncStatusToDbServer ");
 
 		notifyDbstarServiceSDStatus();
 		notifyDbstarServiceNetworkStatus();
 		
 		String disk = mConfigure.getStorageDisk();
 		notifyDbstarServiceStorageStatus(disk);
 		
 		if (DeviceInitController.isBootFirstTime()) {
 			notifyDbstarServiceDeviceInit();
 		}
 	}
 
 	private boolean notifyDbstarService(int command) {
 		if (!mIsDbServiceStarted) {
 			return false;
 		}
 
 		mDBStarClient.notifyDbServer(command, null);
 		return true;
 	}
 
 	// Guodian related code
 	GDClientObserver mGDObserver = new GDClientObserver() {
 
 		public void notifyEvent(int type, Object event) {
 
 			Log.d(TAG, " == notifyEvent == " + type);
 
 			switch (type) {
 			case EventData.EVENT_LOGIN_SUCCESSED: {
 				if (mApplicationObserver != null) {
 					mApplicationObserver.handleEvent(
 							EventData.EVENT_LOGIN_SUCCESSED, event);
 				}
 				
 				if (mPageOberser != null) {
 					mPageOberser.notifyEvent(EventData.EVENT_LOGIN_SUCCESSED,
 							event);
 				}
 				
 				break;
 			}
 
 			case EventData.EVENT_GUODIAN_DATA: {
 				if (mPageOberser != null) {
 					mPageOberser.notifyEvent(EventData.EVENT_GUODIAN_DATA,
 							event);
 				}
 				break;
 			}
 
 			case EventData.EVENT_GUODIAN_DATA_ERROR: {
 			    if (mPageOberser != null) {
                     mPageOberser.notifyEvent(EventData.EVENT_GUODIAN_DATA_ERROR,
                             event);
                 }
 			    break;
 			}
 			
 			// connection related event
 			default: {
 				if (mPageOberser != null) {
                     mPageOberser.notifyEvent(type, event);
                 }
 				break;
 			}
 			}
 		}
 
 	};
 
 	public void disconnect() {
 		if (!mIsGuodianEngineStarted) {
 			Log.d(TAG, "engine is not started!");
 			return;
 		}
 
 		mGuodianEngine.stop();
 	}
 	
 	public void reconnect() {
 		if (!mIsGuodianEngineStarted) {
 			Log.d(TAG, "engine is not started!");
 			return;
 		}
 
 		mGuodianEngine.reconnect();
 	}
 
 	// Guodian Related interface
 	public void requestPowerData(int type, Object args) {
 		if (!mIsGuodianEngineStarted) {
 			Log.d(TAG, "engine is not started!");
 			return;
 		}
 		
 		mGuodianEngine.requestData(type, args);
 	}
 
 	// query cached data
 	public ElectricityPrice getElecPrice() {
 		return mGuodianEngine.getElecPrice();
 	}
 	
 	public LoginData getLoginData(){
 	    return mGuodianEngine.getLoginData();
 	}
 	public EPCConstitute getEDimension(){
 	    return mGuodianEngine.getElectriDimension();
 	}
 }
