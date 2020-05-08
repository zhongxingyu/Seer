 package net.juhonkoti.sharetobrowser;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.TextView;
 
 public class ShareToBrowser extends Activity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_scan_qrcode);
         
         Intent intent = getIntent();
         Uri data = intent.getData();
         
         Log.d("StartUrlSharing", "type: " + intent.getType());
         if ("text/plain".equals(intent.getType())) {
             String url = intent.getStringExtra(Intent.EXTRA_TEXT);
     		TextView t = (TextView) findViewById(R.id.sendToServerText);
     		t.setText(url);
     		
    		new SendUrlToServerTask(this).execute(url);        	
         }
 
 
         
       
     }
 }
