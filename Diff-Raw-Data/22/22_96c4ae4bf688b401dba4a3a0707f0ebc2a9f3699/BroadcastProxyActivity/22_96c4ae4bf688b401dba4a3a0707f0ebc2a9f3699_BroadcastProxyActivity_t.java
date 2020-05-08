 package de.hsrm.inspector.activities;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
import android.util.Log;
 
 /**
  * Created by dobae on 29.05.13.
  */
 public class BroadcastProxyActivity extends Activity {
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		Intent i = getIntent();
		Log.d("", "INCOMING!!!! " + i.toURI());
 		Uri data = i.getData();
 		if (data != null) {
 			String uri = data.toString();
 			uri = uri.replace("inspector://", "inspect://");
 			Intent request = new Intent("de.inspector.intents");
 			request.setData(Uri.parse(uri));
 			sendBroadcast(request);
 			finish();
 		}
 	}
 
 }
