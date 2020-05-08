 package com.oukasoft.ServiceBindSample;
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.Binder;
 import android.os.IBinder;
 import android.util.Log;
 
 public class TestBindService extends Service {
 	
 	static final String TAG= "BindService";
 	
 	public class BindServiceBinder extends Binder{
		// サービスの取得
 		TestBindService getService(){
 			return TestBindService.this;
 		}
 	}
 	
	// Binderの作成
 	private final IBinder mBinder = new BindServiceBinder();
 	
 	@Override
 	public void onCreate() {
 		Log.i(TAG, "onCreate");
 	}
 	@Override
 	public IBinder onBind(Intent arg0) {
 		Log.i(TAG, "onBind");
 		return mBinder;
 	}
 	@Override
 	public boolean onUnbind(Intent intent){
 		Log.i(TAG, "onUnbind");
 		return true;
 	}
 	@Override
 	public void onDestroy(){
 		Log.i(TAG, "onDestroy");
 	}
 
 	public void TestFunction(){
		Log.i(TAG, "適当に作った関数が呼ばれました（＾ω＾）");
 	}
 }
