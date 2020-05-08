 package com.dbstar.service.client;
 
 import java.io.UnsupportedEncodingException;
 
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.util.Log;
 
 import com.dbstar.DbstarDVB.DbstarServiceApi;
 import com.dbstar.DbstarDVB.IDbstarService;
 import com.dbstar.model.ReceiveEntry;
 
 public class GDDBStarClient {
 	private static final String TAG = "GDDBStarClient";
 
 	Context mContext;
 	private Intent mIntent = new Intent();
 
 	private IDbstarService mDbstarService = null;
 	private ComponentName mComponentName = new ComponentName(
 			"com.dbstar.DbstarDVB", "com.dbstar.DbstarDVB.DbstarService");
 
 	private ServiceConnection mConnection = new ServiceConnection() {
 		public void onServiceConnected(ComponentName className, IBinder service) {
 
 			Log.d(TAG, "+++++++++++GDDBStarClient onServiceConnected+++++++++");
 
 			mDbstarService = IDbstarService.Stub.asInterface(service);
 
 			startDvbpush();
 		}
 
 		public void onServiceDisconnected(ComponentName className) {
 			mDbstarService = null;
 		}
 	};
 
 	public GDDBStarClient(Context context) {
 		mContext = context;
 	}
 
 	public void start() {
 		mIntent.setComponent(mComponentName);
 		mContext.startService(mIntent);
 		mContext.bindService(mIntent, mConnection, Context.BIND_AUTO_CREATE);
 	}
 
 	public void stop() {
 		mContext.stopService(mIntent);
 		mContext.unbindService(mConnection);
 	}
 
 	public void setListener() {
 
 	}
 
 	public void startDvbpush() {
 		if (mDbstarService != null) {
 			try {
 				mDbstarService.initDvbpush();
 				Log.d(TAG, "+++++++++++startDvbpush+++++++++++");
 
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void stopDvbpush() {
 		if (mDbstarService != null) {
 			try {
 				mDbstarService.uninitDvbpush();
 				Log.d(TAG, "+++++++++++ stopDvbpush +++++++++++");
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public boolean startTaskInfo() {
 		boolean result = false;
 		if (mDbstarService != null) {
 			try {
 				Intent it = mDbstarService.sendCommand(
 						DbstarServiceApi.CMD_DVBPUSH_GETINFO_START, null, 0);
 				result = true;
 				Log.d(TAG, "+++++++++++ startTaskInfo +++++++++++");
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 		}
 
 		return result;
 	}
 
 	public void notifyDbServer(int msg) {
 		if (mDbstarService != null) {
 			try {
 				Log.d(TAG, "notifyDbServer " + msg);
 				mDbstarService.sendCommand(msg, null, 0);
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public boolean stopTaskInfo() {
 		boolean result = false;
 		if (mDbstarService != null) {
 			try {
 				Intent it = mDbstarService.sendCommand(
 						DbstarServiceApi.CMD_DVBPUSH_GETINFO_STOP, null, 0);
 				result = true;
 				Log.d(TAG, "+++++++++++ stopTaskInfo +++++++++++");
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 		}
 
 		return result;
 	}
 
 	// data format: "1001|task1|23932|23523094823\n1002|task2|234239|12349320\n"
 
 	public ReceiveEntry[] getTaskInfo() {
 		ReceiveEntry[] entries = null;
 
 		Log.d(TAG, "+++++++++++ getTaskInfo +++++++++++");
 
 		if (mDbstarService == null)
 			return entries;
 
 		try {
 			Intent intent = mDbstarService.sendCommand(
 					DbstarServiceApi.CMD_DVBPUSH_GETINFO, null, 0);
 
 			byte[] bytes = intent.getByteArrayExtra("result");
 
 			if (bytes != null) {
 				String info = null;
 				try {
 					info = new String(bytes, "utf-8");
 					// Log.d(TAG, "TaskInfo: " + info);
 				} catch (UnsupportedEncodingException e) {
 					e.printStackTrace();
 				}
 
 				String[] items = null;
 				if (info != null) {
 					items = info.split("\n");
 				}
 
 				if (items != null) {
 					entries = new ReceiveEntry[items.length];
 
 					for (int i = 0; i < items.length; i++) {
 						entries[i] = createEntry(items[i]);
 					}
 
 				}
 
 			}
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 
 		return entries;
 	}
 
 	ReceiveEntry createEntry(String data) {
 		ReceiveEntry entry = null;
 
 		if (data == null || data.isEmpty())
 			return entry;
 
 		String[] items = data.split("\t");
 
 		// for (int i = 0; i < items.length; i++) {
 		// Log.d(TAG, "item " + i + " = " + items[i]);
 		// }
 
 		entry = new ReceiveEntry();
 		entry.Id = items[0];
 		entry.Name = items[1];
 		entry.RawProgress = Long.valueOf(items[2]);
 		entry.RawTotal = Long.valueOf(items[3]);
 		// Log.d(TAG, "progress = " + entry.RawProgress + " total = " +
 		// entry.RawTotal);
		entry.ConvertSize();
 
 		return entry;
 	}
 
 }
