 package com.example.usermanagement;
 
 import android.app.IntentService;
 import android.content.Intent;
 import android.os.IBinder;
 import android.os.RemoteCallbackList;
 import android.os.RemoteException;
 import android.util.Log;
 
 public class UserSwitchService extends IntentService {
 	private static final String TAG = UserSwitchService.class.getSimpleName();
 	
 	private RemoteCallbackList<IUpdateListener> listeners =
 			new RemoteCallbackList<IUpdateListener>();
 	
 	private IUserSwitchService.Stub binder = new IUserSwitchService.Stub() {
 		
 		@Override
 		public void removeListener(IUpdateListener listener) throws RemoteException {
 			Log.d(TAG, "removeListener: listener="+listener);
 			listeners.unregister(listener);
 		}
 		
 		@Override
 		public void addListener(IUpdateListener listener) throws RemoteException {
 			Log.d(TAG, "addListener: listener="+listener);
 			listeners.register(listener);
 		}
 	};
 
 	public UserSwitchService() {
 		super(TAG);
 	}
 
 	@Override
 	protected void onHandleIntent(Intent intent) {
 		Log.d(TAG, "onHandleIntent");
 		synchronized (listeners) {
 			int count = listeners.beginBroadcast();
 			Log.d(TAG, "onHandleIntent: count="+count);
 			for (int i=0; i<count; i++) {
 				IUpdateListener listener = listeners.getBroadcastItem(i);
 				Log.d(TAG, "onHandleIntent: listener="+listener);
 				try {
 					listener.update();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		Log.d(TAG, "onBind:");
 		String action = null;
 		if (intent != null) {
 			action = intent.getAction();
 			Log.d(TAG, "onBind: action="+action);
 			if (action != null && IUserSwitchService.class.getName().equals(action)) {
 				return binder;
 			}
 		}
 		return null;
 	}
 
 }
