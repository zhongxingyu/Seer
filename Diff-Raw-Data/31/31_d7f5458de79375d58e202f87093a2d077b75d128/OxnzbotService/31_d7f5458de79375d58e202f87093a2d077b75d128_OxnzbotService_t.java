 package com.appspot.oxnzbot;
 
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

 import android.app.Service;
 import android.content.Intent;
 import android.os.Binder;
 import android.os.IBinder;
 import android.util.Log;
 import android.widget.Toast;
 
 public class OxnzbotService extends Service {
 	private static final String TAG = "Retrieve Command Service";
 
 	private IBinder mBinder = new LocalBinder();
 	
 	public class LocalBinder extends Binder {
 		public OxnzbotService getService() {
 			return OxnzbotService.this;
 		}
 	}
 	
 	/** 
      * ķֵᵱ 
      * public void onServiceConnected(ComponentName name, IBinder binder) 
      * IBinder IָinterfaceһǻserviceʵһBinder 
      * ͨBinderǿservice 
      */ 
 	@Override
 	public IBinder onBind(Intent intent) {
 		Log.d(TAG, "onBind");
 		return mBinder;
 	}
 	
 	@Override
 	public void onCreate() {
 		Toast.makeText(this, "My service created", Toast.LENGTH_LONG).show();
 		Log.d(TAG, "onCreate()");
 	}
 
 	@Override
 	public void onDestroy() {
 		Toast.makeText(this, "onDestroy()", Toast.LENGTH_SHORT).show();
 		Log.d(TAG, "onDestroy()");
 	}
 	
 	@Override
 	public void onStart(Intent intent, int startid) {
 		Toast.makeText(this, "onStart()", Toast.LENGTH_LONG).show();
 		Log.d(TAG, "onStart()");
		try {
		String command = retriveCommand("http://oxnzbot.appspot.com/_ah/xmpp");
		Log.d(TAG, "retrive command:" + command);
		} catch (Exception e) {
			Log.d(TAG, e.getMessage());
		}
	}
	
	public String retriveCommand(String url) {
		String result = null;
		try {
			HttpGet request = new HttpGet(url);
			HttpResponse response = new DefaultHttpClient().execute(request);
			if (response.getStatusLine().getStatusCode() != 200) {
				Log.e(TAG, "connection error");
			}
			else {
				result = EntityUtils.toString(response.getEntity());
				Log.d(TAG, "response result:" + result);
			}
		} catch (Exception e) {
			Log.e(TAG, "exceptoin:" + e.getLocalizedMessage());
			Log.d(TAG, e.getMessage());
		}
		return result;
 	}
 }
