 package fi.hiit.demo.phonegab;
 
 import android.content.*;
 import android.content.IntentFilter.MalformedMimeTypeException;
 import android.os.Bundle;
 import android.util.Log;
 
 import com.google.android.gcm.*;
 
 import org.apache.cordova.*;
 
 public class PhoneGabDemoAndroidActivity extends DroidGap {
 	
 	// service id for GCM
	public static String SENDER_ID = "";
	
	{
		SENDER_ID = getString( R.string.gcm_app_id );
	
	}
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
       
  
         
         Log.v(TAG, "Jee " + SENDER_ID);
         
         // register device
         GCMRegistrar.checkDevice(this);
         GCMRegistrar.checkManifest(this);
         final String regId = GCMRegistrar.getRegistrationId(this);
         if (regId.equals("")) {
           GCMRegistrar.register(this, SENDER_ID);
           Log.v(TAG, "Registered system");
         } else {
           Log.v(TAG, "Already registered");
           Log.v(TAG, regId );
         }
         
         BroadcastReceiver receiver = new PushReciever();
         
         registerReceiver(receiver, new IntentFilter(SENDER_ID) );
  
         Log.v(TAG, "Hello" );
         
         // open PhoneGab app
         super.setIntegerProperty("loadUrlTimeoutValue", Integer.MAX_VALUE);
         super.loadUrl("file:///android_asset/html/clean.html");
     }
     
     public void executeJS(String js){
     	super.loadUrl("javascript:" + js);
     }
     
 	private class PushReciever extends BroadcastReceiver {
 		
 		public PushReciever() {
 			Log.v(TAG, "PushReciever <3");
 		}
 
 		@Override
 		public void onReceive(Context arg0, Intent intent) {
 			Log.v(TAG, "Hello is ok!");
 			// get message and execute it
 			String js = intent.getStringExtra("javascript");
 			PhoneGabDemoAndroidActivity.this.executeJS(js);
 			
 		}
 		
 	}
     
 }
