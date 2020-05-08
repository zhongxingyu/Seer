 package com.bytopia.oboobs;
 
 import android.app.Application;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.os.Handler;
 import android.os.Handler.Callback;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import android.util.SparseArray;
 
 import com.bytopia.oboobs.db.BoobsDbOpenHelper;
 import com.bytopia.oboobs.db.DbUtils;
 import com.bytopia.oboobs.utils.CacheHolder;
 import com.bytopia.oboobs.utils.Utils;
 
 public class OboobsApp extends Application {
 
 	public boolean isDark = true;
 
 	public static String MAX_DISK_CACHE_PREF;
 	
 	public static String PACKAGE_NAME;
 
 	public SharedPreferences preferences;
 
 	private CacheHolder cacheHolder;
 
 	private Handler resultHandler;
 
 	private SparseArray<ImageReceiver> curentReceivers;
 	
 	private BoobsDbOpenHelper dbHelper;
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		Utils.initApp(this);
 
 		preferences = PreferenceManager.getDefaultSharedPreferences(this);
 		
 		dbHelper = new BoobsDbOpenHelper(this);
 		DbUtils.helper = dbHelper;
 
 		MAX_DISK_CACHE_PREF = getString(R.string.max_disk_cache_pref);
 		PACKAGE_NAME = getPackageName();
 		
 		curentReceivers = new SparseArray<ImageReceiver>();
 
 		cacheHolder = new CacheHolder(this);
 
 		resultHandler = new Handler(new Callback() {
 
 			@Override
 			public boolean handleMessage(Message msg) {
 				int senderType = msg.arg2;
 				
 				ImageReceiver curentReceiver = curentReceivers.get(senderType);
 				
 				if (curentReceiver != null && curentReceiver.getSenderType() == senderType) {
 					Bitmap bm = (Bitmap) msg.obj;
 					int id = msg.arg1;
 					
 					curentReceiver.receiveImage(id, bm);
 				}
 
 				return true;
 			}
 		});
 		
 	}
 
 	public CacheHolder getCacheHolder() {
 		return cacheHolder;
 	}
 
 	public Handler getResultHandler() {
 		return resultHandler;
 	}
 
 	public void addImageReceiver(ImageReceiver receiver) {
 		curentReceivers.append(receiver.getSenderType(), receiver);
 	}
 	
 	public void removeImageReciever(ImageReceiver mImageReceiver) {
 		curentReceivers.remove(mImageReceiver.getSenderType());
 	}
 
 	public BoobsDbOpenHelper getDbHelper() {
 		return dbHelper;
 	}
 
 	
 
 }
