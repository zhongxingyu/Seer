 package com.pebblepager;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.UUID;
 
 import android.app.Activity;
 import android.content.ActivityNotFoundException;
 import android.content.Intent;
 import android.content.SharedPreferences.Editor;
 import android.media.Ringtone;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.TextView;
 
 import com.getpebble.android.kit.PebbleKit;
 
 public class MainActivity extends Activity {
 	private static final String PEBBLE_LAUNCH_COMPONENT = "com.getpebble.android";
 	private static final String PEBBLE_LAUNCH_ACTIVITY = "com.getpebble.android.ui.UpdateActivity";
 	private PagerDataReceiver receiver;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		String ringtoneString = getSharedPreferences("PebblePager",  MODE_PRIVATE).getString("ringtone", null);
 		Ringtone ringtone = null;
 		if (ringtoneString != null) {
 			Uri uri = Uri.parse(ringtoneString);
 			ringtone = RingtoneManager.getRingtone(this, uri);
 		}
 		receiver = new PagerDataReceiver(UUID.fromString("389d91c5-f84c-4fc1-a1a0-baa8ea1b436f"), this, ringtone);
 		PebbleKit.registerReceivedDataHandler(this, receiver);
		if (ringtone != null) {
			((TextView) findViewById(R.id.ringtone_button)).setText("Ringtone: " + ringtone.getTitle(this));
 		}
 		findViewById(R.id.ringtone_button).setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
 				startActivityForResult(intent, 0);
 			}
 		});
 		findViewById(R.id.install_button).setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				installApp();
 			}
 		});
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Uri uri = (Uri) data.getExtras().get(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
		Editor e = getSharedPreferences("PebblePager", MODE_PRIVATE).edit();
		e.putString("ringtone", uri.toString());
		e.commit();
		Ringtone ringtone = RingtoneManager.getRingtone(this, uri);
		receiver.setRingtone(ringtone);
		((TextView) findViewById(R.id.ringtone_button)).setText("Ringtone: "+ringtone.getTitle(this));
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	private void installApp() {
 		 Log.d("MainActivity", "copy_watchface_from_assets()");
 		    InputStream input = null;
 		    OutputStream output = null;
 		    try {
 		        input = getAssets().open("pebble_pager.pbw");
 		        File dest = new File(getExternalCacheDir(), "pebble_pager.pbw");
 		        // delete existing file
 		        dest.delete();
 		        output = new FileOutputStream(dest);
 		        Log.d("MainActivity", "storing pbw: " + dest);
 		        
 		        // copy asset to file
 		        byte[] buffer = new byte[2056];
 		        int length;
 		        while ((length = input.read(buffer))>0){
 		            output.write(buffer, 0, length);
 		        }
 		        output.flush();
 		        
 		        // launch pebble update activity
 		        Intent intent = new Intent(Intent.ACTION_VIEW);
 		        intent.setData(Uri.fromFile(dest));
 		        intent.setClassName(PEBBLE_LAUNCH_COMPONENT, PEBBLE_LAUNCH_ACTIVITY);
 		        Log.d("MainActivity", "launch pebble to load pbw");
 		        try {
 		            startActivity(intent);
 		        } catch (ActivityNotFoundException e) {
 			    	Log.e("MainActivity", "Exception", e);
 		        }
 		    } catch (IOException e) {
 		    	Log.e("MainActivity", "Exception", e);
 		    } finally {
 		        if (input != null) {
 		            try {
 		                input.close();
 		            } catch (IOException e) {
 				    	Log.e("MainActivity", "Exception", e);
 		            }
 		        }
 		        if (output != null) {
 		            try {
 		                output.close();
 		            } catch (IOException e) {
 				    	Log.e("MainActivity", "Exception", e);
 		            }
 		        }
 		    }
 	}
 
 }
