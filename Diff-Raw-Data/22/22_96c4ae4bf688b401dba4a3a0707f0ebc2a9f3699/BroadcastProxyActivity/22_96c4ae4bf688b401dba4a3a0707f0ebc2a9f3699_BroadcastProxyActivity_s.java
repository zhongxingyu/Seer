 package de.hsrm.inspector.activities;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 
 /**
  * Created by dobae on 29.05.13.
  */
 public class BroadcastProxyActivity extends Activity {
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		Intent i = getIntent();
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
