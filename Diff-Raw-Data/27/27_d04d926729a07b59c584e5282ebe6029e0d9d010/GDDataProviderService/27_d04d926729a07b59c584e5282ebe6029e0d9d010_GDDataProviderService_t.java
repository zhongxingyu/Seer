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
 import com.dbstar.model.ColumnData;
 import com.dbstar.model.ContentData;
 import com.dbstar.model.EventData;
 import com.dbstar.model.GDCommon;
 import com.dbstar.model.GDDVBDataContract;
 import com.dbstar.model.GDSystemConfigure;
 import com.dbstar.model.GDDataModel;
 import com.dbstar.model.GDNetModel;
 import com.dbstar.model.GuideListItem;
 import com.dbstar.model.PreviewData;
 import com.dbstar.model.ReceiveEntry;
 import com.dbstar.service.client.DbServiceObserver;
 import com.dbstar.service.client.GDDBStarClient;
 
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Binder;
 import android.os.Message;
 import android.util.Log;
 import android.os.Process;
 
 public class GDDataProviderService extends Service implements DbServiceObserver {
 
 	private static final String TAG = "GDDataProviderService";
 
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
 
 	public static final int REQUESTTYPE_GETPOWERCONSUMPTION = 0x3001;
 	public static final int REQUESTTYPE_GETTOTALCOSTBYCHARGETYPE = 0x3002;
 
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
 
 	private static final String SmartHomePrepertyName = "dbstar.smarthome.started";
 
 	private Object mTaskQueueLock = new Object();
 	private LinkedList<RequestTask> mTaskQueue = null;
 
 	private Object mFinishedTaskQueueLock = new Object();
 	private LinkedList<RequestTask> mFinishedTaskQueue = null;
 
 	private int mThreadCount = 2;
 	private List<WorkerThread> mThreadPool = new LinkedList<WorkerThread>();
 
 	private int mMainThreadId;
 	private int mMainThreadPriority;
 
 	GDSystemConfigure mConfigure = null;
 
 	GDDataModel mDataModel = null;
 	GDNetModel mNetModel = null;
 
 	ConnectivityManager mConnectManager;
 	GDDiskSpaceMonitor mDiskMonitor;
 
 	GDDBStarClient mDBStarClient;
 	GDApplicationObserver mApplicationObserver = null;
 	ClientObserver mPageOberser = null;
 
 	private final IBinder mBinder = new DataProviderBinder();
 	SystemEventHandler mHandler = null;
 
 	boolean mIsSmartHomeServiceStarted = false;
 	boolean mIsDbServiceStarted = false;
 	boolean mIsStorageReady = false;
 	boolean mIsNetworkReady = false;
 
 	boolean mIsSmartcardIn = false;
 
 	String mMacAddress = "";
 
 	String mUpgradePackageFile;
 	boolean mNeedUpgrade = false;
 
 	private PeripheralController mPeripheralController;
 	GDPowerManager mPowerManger;
 
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
 
 		mPowerManger = new GDPowerManager();
 		mPowerManger.acquirePartialWakeLock(this);
 
 		mHandler = new SystemEventHandler();
 
 		mConfigure = new GDSystemConfigure();
 		mDataModel = new GDDataModel();
 		mNetModel = new GDNetModel();
 		mDiskMonitor = new GDDiskSpaceMonitor(mHandler);
 		mDBStarClient = new GDDBStarClient(this);
 
 		mTaskQueue = new LinkedList<RequestTask>();
 		mFinishedTaskQueue = new LinkedList<RequestTask>();
 		mPeripheralController = new PeripheralController();
 
 		if (mPeripheralController.isHdmiIn()) {
 			Log.d(TAG, "Hdmi: IN");
 			mPeripheralController.setAudioOutputOff();
 		} else {
 			Log.d(TAG, "Hdmi: OUT");
 			mPeripheralController.setAudioOutputOn();
 		}
 
 		if (mPeripheralController.isSmartCardIn()) {
 			Log.d(TAG, "SmartCard: IN");
 		} else {
 			Log.d(TAG, "SmartCard: OUT");
 		}
 
 		for (int i = 0; i < mThreadCount; i++) {
 			WorkerThread thread = new WorkerThread();
 			thread.start();
 
 			mThreadPool.add(thread);
 		}
 
 		registerUSBReceiver();
 		reqisterConnectReceiver();
 		reqisterSystemMessageReceiver();
 
 		// read configure
 		mConfigure.readConfigure();
 
 		// check storage
 		if (mConfigure.configureStorage()) {
 			String disk = mConfigure.getStorageDisk();
 			Log.d(TAG, "monitor disk " + disk);
 
 			if (!disk.isEmpty()) {
 				mIsStorageReady = true;
 				Log.d(TAG, "disk is ready " + disk);
 
 				mDiskMonitor.addDiskToMonitor(disk);
 			}
 		}
 
 		// check network
 		mIsNetworkReady = isNetworkConnected();
 		Log.d(TAG, "network is connected " + mIsNetworkReady);
 
 		// start Dbstar service
 		mIsDbServiceStarted = false;
 		mDBStarClient.setObserver(this);
 		mDBStarClient.start();
 
 		// start smart home service
 		mIsSmartHomeServiceStarted = false;
 		if (mIsStorageReady && mIsNetworkReady) {
 			startDbStarService();
 		}
 
 		// initialize engine
 		initializeDataEngine();
 		initializeNetEngine();
 
 		queryDiskGuardSize();
 
 		mIsSmartcardIn = mPeripheralController.isSmartCardIn();
 	}
 
 	void initializeDataEngine() {
 		mDataModel.initialize(mConfigure);
 		mDataModel.setPushDir(mConfigure.getStorageDir());
 	}
 
 	void deinitializeDataEngine() {
 		mDataModel.deInitialize();
 	}
 
 	void initializeNetEngine() {
 		mNetModel.initialize();
 	}
 
 	void deinitializeNetEngine() {
 		mNetModel.deinitialize();
 	}
 
 	public void onDestroy() {
 		super.onDestroy();
 		Log.d(TAG, "onDestroy");
 
 		mPowerManger.releaseWakeLock();
 
 		stopDbStarService();
 		mDBStarClient.stop();
 		mDiskMonitor.stopMonitor();
 
 		deinitializeDataEngine();
 		deinitializeNetEngine();
 
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
 	}
 
 	private void startDbStarService() {
 		Log.d(TAG, "++++++++++++++++++startDbStarService++++++++++++++++++++");
 
 		if (mIsSmartHomeServiceStarted)
 			return;
 
 		mIsSmartHomeServiceStarted = true;
 
 		SharedPreferences settings = null;
 		SharedPreferences.Editor editor = null;
 
 		settings = getSharedPreferences(SmartHomePrepertyName, 0);
 		editor = settings.edit();
 		editor.putInt(SmartHomePrepertyName, 1);
 		editor.commit();
 	}
 
 	private void stopDbStarService() {
 		if (!mIsSmartHomeServiceStarted)
 			return;
 
 		mIsSmartHomeServiceStarted = false;
 
 		Log.d(TAG, "stopDbStarService");
 
 		SharedPreferences settings = null;
 		SharedPreferences.Editor editor = null;
 
 		settings = getSharedPreferences(SmartHomePrepertyName, 0);
 		editor = settings.edit();
 		editor.putInt(SmartHomePrepertyName, 0);
 		editor.commit();
 	}
 
 	class SystemEventHandler extends Handler {
 		public void handleMessage(Message msg) {
 			int msgId = msg.what;
 			switch (msgId) {
 			case GDCommon.MSG_TASK_FINISHED: {
 				RequestTask task = dequeueFinishedTask();
 
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
 
 				String storage = "";
 				if (mConfigure.configureStorage()) {
 					storage = mConfigure.getStorageDisk();
 				}
 
 				if (disk.equals(storage) && mApplicationObserver != null) {
 					mIsStorageReady = true;
 					mConfigure.readConfigure();
 					mConfigure.configureStorage();
 
 					initializeDataEngine();
 
 					Log.d(TAG, " +++++++++++ monitor disk ++++++++" + disk);
 					mDiskMonitor.removeDiskFromMonitor(disk);
 					mDiskMonitor.addDiskToMonitor(disk);
 
 					if (mIsNetworkReady) {
 						startDbStarService();
 					}
 
 					notifyDbstarServiceStorageStatus();
 
 					// restart application
 					mApplicationObserver.initializeApp();
 				}
 				break;
 			}
 
 			case GDCommon.MSG_MEDIA_REMOVED: {
 				Bundle data = msg.getData();
 				String disk = data.getString("disk");
 
 				mDiskMonitor.removeDiskFromMonitor(disk);
 
 				String storage = mConfigure.getStorageDisk();
 				if (disk.equals(storage)) {
 					mIsStorageReady = false;
 
 					notifyDbstarServiceStorageStatus();
 
 					deinitializeDataEngine();
 					mApplicationObserver.deinitializeApp();
 				}
 				break;
 			}
 
 			case GDCommon.MSG_NETWORK_CONNECT:
 				Log.d(TAG, " +++++++++++++ network connected +++++++++++++");
 				mIsNetworkReady = true;
 				mPeripheralController.setNetworkLedOn();
 				if (mIsStorageReady) {
 					startDbStarService();
 				}
 
 				notifyDbstarServiceNetworkStatus();
 
 				break;
 			case GDCommon.MSG_NETWORK_DISCONNECT:
 				Log.d(TAG, "++++++++++++ network disconnected +++++++++++");
 				mIsNetworkReady = false;
 				mPeripheralController.setNetworkLedOff();
 				notifyDbstarServiceNetworkStatus();
 				stopDbStarService();
 
 				break;
 
 			case GDCommon.MSG_DISK_SPACEWARNING: {
 				Bundle data = msg.getData();
 				String disk = (String) data.get(GDCommon.KeyDisk);
 				if (mApplicationObserver != null) {
 					mApplicationObserver.handleNotifiy(
 							GDCommon.MSG_DISK_SPACEWARNING, disk);
 				}
 
 				mDBStarClient
 						.notifyDbServer(DbstarServiceApi.CMD_DISK_FOREWARNING);
 				break;
 			}
 
 			case GDCommon.MSG_SYSTEM_FORCE_UPGRADE:
 			case GDCommon.MSG_SYSTEM_UPGRADE: {
 				String packageFile = msg.getData().getString(
 						GDCommon.KeyPackgeFile);
 				mApplicationObserver.handleNotifiy(msgId, packageFile);
 
 				break;
 			}
 
 			case GDCommon.MSG_USER_UPGRADE_CANCELLED: {
 				mNeedUpgrade = true;
 				mUpgradePackageFile = String.valueOf(msg.obj);
 				mDBStarClient
 						.notifyDbServer(DbstarServiceApi.CMD_UPGRADE_CANCEL);
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
 				mIsSmartcardIn = true;
 				notifyDbstarServiceSDStatus();
 				notifySmartcardStatusChange(mIsSmartcardIn);
 				break;
 			}
 			case GDCommon.MSG_SMARTCARD_OUT: {
 				mIsSmartcardIn = false;
 				notifyDbstarServiceSDStatus();
 				notifySmartcardStatusChange(mIsSmartcardIn);
 				break;
 			}
 
 			default:
 				break;
 			}
 		}
 
 	}
 
 	private void notifySmartcardStatusChange(boolean plugIn) {
 		EventData.SmartcardStatus event = new EventData.SmartcardStatus();
 		event.isPlugIn = plugIn;
 		if (mPageOberser != null) {
 			mPageOberser.notifyEvent(EventData.EVENT_SMARTCARD_STATUS, event);
 		} else if (mApplicationObserver != null) {
 			mApplicationObserver.handleNotifiy(
 					plugIn ? GDCommon.MSG_SMARTCARD_IN
 							: GDCommon.MSG_SMARTCARD_OUT, null);
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
 
 		case REQUESTTYPE_GETTOTALCOSTBYCHARGETYPE:
 		case REQUESTTYPE_GETPOWERCONSUMPTION: {
 			if (task.Observer != null) {
 				task.Observer.updateData(task.Type, 0, 0, task.Data);
 			}
 			break;
 		}
 
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
 			synchronized (mFinishedTaskQueueLock) {
 				mFinishedTaskQueue.add(task);
 			}
 
 			mHandler.sendEmptyMessage(GDCommon.MSG_TASK_FINISHED);
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
 
 			Process.setThreadPriority(Process.THREAD_PRIORITY_LESS_FAVORABLE);
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
 						// coloumns[i].IconClicked =
 						// mDataModel.getImage(iconRootPath + "/" +
 						// column.IconClickedPath);
 					}
 
 					task.Data = coloumns;
 
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
 
 					ContentData[] contents = mDataModel.getPublicationsEx(
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
 
 					Bitmap image = mDataModel.getImage(file);
 					task.Data = image;
 
 					taskFinished(task);
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
 					ReceiveEntry[] entries = mDBStarClient.getTaskInfo();
 					task.Data = entries;
 					taskFinished(task);
 					break;
 				}
 
 				case REQUESTTYPE_GETTSSIGNALSTATUS: {
 					String status = mDBStarClient.getTSSignalStatus();
 					task.Data = status;
 					taskFinished(task);
 					break;
 				}
 
 				case REQUESTTYPE_GETPOWERCONSUMPTION: {
 					Object value = null;
 					value = task.Parameters.get(PARAMETER_CCID);
 					String cc_id = (String) value;
 					value = task.Parameters.get(PARAMETER_DATESTART);
 					String date_start = (String) value;
 					value = task.Parameters.get(PARAMETER_DATEEND);
 					String date_end = (String) value;
 
 					String powerConsumption = mNetModel.getPowerConsumption(
 							cc_id, date_start, date_end);
 					task.Data = powerConsumption;
 
 					taskFinished(task);
 					break;
 				}
 
 				case REQUESTTYPE_GETTOTALCOSTBYCHARGETYPE: {
 					Object value = null;
 					value = task.Parameters.get(PARAMETER_CCID);
 					String cc_id = (String) value;
 					value = task.Parameters.get(PARAMETER_DATESTART);
 					String date_start = (String) value;
 					value = task.Parameters.get(PARAMETER_DATEEND);
 					String date_end = (String) value;
 					value = task.Parameters.get(PARAMETER_CHAREGTYPE);
 					String charge_type = (String) value;
 
 					String totalCost = mNetModel.getTotalCostByChargeType(
 							cc_id, date_start, date_end, charge_type);
 					task.Data = totalCost;
 
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
 
 					boolean ret = mDataModel.setSettingValue(key, sValue);
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
 
 	public void getPowerConsumption(ClientObserver observer, String cc_id,
 			String date_start, String date_end) {
 		if (!mIsNetworkReady)
 			return;
 
 		RequestTask task = new RequestTask();
 		// task.Id = System.currentTimeMillis();
 		task.Observer = observer;
 		task.Type = REQUESTTYPE_GETPOWERCONSUMPTION;
 
 		task.Parameters = new HashMap<String, Object>();
 		task.Parameters.put(PARAMETER_CCID, cc_id);
 		task.Parameters.put(PARAMETER_DATESTART, date_start);
 		task.Parameters.put(PARAMETER_DATEEND, date_end);
 
 		enqueueTask(task);
 	}
 
 	public void getTotalCostByChargeType(ClientObserver observer, String cc_id,
 			String date_start, String date_end, String charge_type) {
 		if (!mIsNetworkReady)
 			return;
 
 		RequestTask task = new RequestTask();
 		// task.Id = System.currentTimeMillis();
 		task.Observer = observer;
 		task.Type = REQUESTTYPE_GETTOTALCOSTBYCHARGETYPE;
 
 		task.Parameters = new HashMap<String, Object>();
 		task.Parameters.put(PARAMETER_CCID, cc_id);
 		task.Parameters.put(PARAMETER_DATESTART, date_start);
 		task.Parameters.put(PARAMETER_DATEEND, date_end);
 		task.Parameters.put(PARAMETER_CHAREGTYPE, charge_type);
 
 		enqueueTask(task);
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
 	
 	public boolean isSmartcardPlugIn() {
 		return mIsSmartcardIn;
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
 				mDiskMonitor.setGuardSize(guardSize);
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
 
 		filter.addAction(GDCommon.ActionGetNetworkInfo);
 		filter.addAction(GDCommon.ActionSetNetworkInfo);
 
 		filter.addAction(GDCommon.ActionScreenOn);
 		filter.addAction(GDCommon.ActionScreenOff);
 
 		filter.addAction(DbstarServiceApi.ACTION_HDMI_IN);
 		filter.addAction(DbstarServiceApi.ACTION_HDMI_OUT);
 
 		filter.addAction(DbstarServiceApi.ACTION_SMARTCARD_IN);
 		filter.addAction(DbstarServiceApi.ACTION_SMARTCARD_OUT);
 
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
 
 	private BroadcastReceiver mNetworkReceiver = new BroadcastReceiver() {
 
 		public void onReceive(Context context, Intent intent) {
 			String action = intent.getAction();
 
 			if (!action.equals(ConnectivityManager.CONNECTIVITY_ACTION))
 				return;
 
 			boolean noConnectivity = intent.getBooleanExtra(
 					ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
 
 			Log.d(TAG, "noConnectivity = " + noConnectivity);
 			if (noConnectivity) {
 				// There are no connected networks at all
 				mHandler.sendEmptyMessage(GDCommon.MSG_NETWORK_DISCONNECT);
 				return;
 			}
 
 			// case 1: attempting to connect to another network, just wait for
 			// another broadcast
 			// case 2: connected
 			// NetworkInfo networkInfo = (NetworkInfo) intent
 			// .getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
 
 			NetworkInfo networkInfo = mConnectManager.getActiveNetworkInfo();
 
 			if (networkInfo != null) {
 				Log.d(TAG, "getTypeName() = " + networkInfo.getTypeName());
 				Log.d(TAG, "isConnected() = " + networkInfo.isConnected());
 
 				if (networkInfo.isConnected()) {
 					mHandler.sendEmptyMessage(GDCommon.MSG_NETWORK_CONNECT);
 				}
 			}
 		}
 
 	};
 
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
 					byte[] bytes = intent.getByteArrayExtra("message");
 					if (bytes != null) {
 						String packageFile = "";
 
 						try {
 							packageFile = new String(bytes, "utf-8");
 						} catch (UnsupportedEncodingException e) {
 							e.printStackTrace();
 						}
 
 						Log.d(TAG, "onReceive packageFile " + packageFile);
 
 						Bundle data = new Bundle();
 						data.putString(GDCommon.KeyPackgeFile, packageFile);
 
 						int msgId = 0;
 						if (type == DbstarServiceApi.UPGRADE_NEW_VER) {
 							msgId = GDCommon.MSG_SYSTEM_UPGRADE;
 						} else if (type == DbstarServiceApi.UPGRADE_NEW_VER_FORCE) {
 							msgId = GDCommon.MSG_SYSTEM_FORCE_UPGRADE;
 						} else {
 							;
 						}
 
 						Message msg = mHandler.obtainMessage(msgId);
 						msg.setData(data);
 						mHandler.sendMessage(msg);
 
 					}
 					break;
 
 				}
 
 				case DbstarServiceApi.STATUS_DVBPUSH_INIT_SUCCESS: {
 					mIsDbServiceStarted = true;
 
 					Log.d(TAG,
 							" ========== DbstarServer init success ===========");
 					if (mDBStarClient.isBoundToServer()) {
 						// syncStatusToDbServer();
 						mHandler.sendEmptyMessage(GDCommon.SYNC_STATUS_TODBSERVER);
 					} else {
 						mStatusIsSynced = false;
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
 				upgradeAfterSleep();
 			} else if (action.equals(GDCommon.ActionScreenOff)) {
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
 			}
 		}
 	};
 
 	void upgradeAfterSleep() {
 		if (mNeedUpgrade) {
 			RebootUtils.rebootInstallPackage(this, mUpgradePackageFile);
 		}
 	}
 
 	boolean notifyDbstarServiceNetworkStatus() {
		Log.d(TAG, "NETWORK --- notifyDbstarServiceNetworkStatus: dvb started " + mIsDbServiceStarted);
		
 		if (!mIsDbServiceStarted)
 			return false;
 
 		if (isNetworkConnected()) {
 			mDBStarClient.notifyDbServer(DbstarServiceApi.CMD_NETWORK_CONNECT);
 		} else {
 			mDBStarClient
 					.notifyDbServer(DbstarServiceApi.CMD_NETWORK_DISCONNECT);
 		}
 
 		return true;
 	}
 
 	boolean notifyDbstarServiceStorageStatus() {
		
		Log.d(TAG, "STORAGE -- notifyDbstarServiceStorageStatus: dvb started " + mIsDbServiceStarted);
		
 		if (!mIsDbServiceStarted)
 			return false;
 
		//TODO: At this point, the disk maybe not mount now.
 		if (mIsStorageReady) {
 			mDBStarClient.notifyDbServer(DbstarServiceApi.CMD_DISK_MOUNT);
 		} else {
 			mDBStarClient.notifyDbServer(DbstarServiceApi.CMD_DISK_UNMOUNT);
 		}
 
 		return true;
 	}
 
 	boolean notifyDbstarServiceSDStatus() {
		
		Log.d(TAG, "SMARTCARD --- notifyDbstarServiceSDStatus: dvb started " + mIsDbServiceStarted);
		
 		if (!mIsDbServiceStarted)
 			return false;
 
 		if (mIsSmartcardIn) {
 			mDBStarClient.notifyDbServer(DbstarServiceApi.CMD_DRM_SC_INSERT);
 		} else {
 			mDBStarClient.notifyDbServer(DbstarServiceApi.CMD_DRM_SC_REMOVE);
 		}
 
 		return true;
 	}
 
 	boolean notifyDbstarService(int command) {
 		if (!mIsDbServiceStarted) {
 			return false;
 		}
 
 		mDBStarClient.notifyDbServer(command);
 		return true;
 	}
 
 	boolean mStatusIsSynced = false;
 
 	@Override
 	public void onServerStarted() {
 		syncStatusToDbServer();
 	}
 
 	@Override
 	public void onServerRestarted() {
 		syncStatusToDbServer();
 	}
 
 	@Override
 	public void onServerStopped() {
 		mStatusIsSynced = false;
 	}
 
 	private void syncStatusToDbServer() {
 		Log.d(TAG, "syncStatusToDbServer " + mStatusIsSynced);
 		if (mStatusIsSynced)
 			return;
 
 		notifyDbstarServiceSDStatus();
 		mStatusIsSynced = notifyDbstarServiceNetworkStatus();
 		mStatusIsSynced = mStatusIsSynced && notifyDbstarServiceStorageStatus();
 	}
 
 }
