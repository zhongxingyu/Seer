 package org.omships.omships;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.support.v4.app.NavUtils;
 import android.annotation.TargetApi;
 import android.os.Build;
 
 /**
  * Activity displaying a webcam.
  * It basically just shows the image from the cam and periodically updates.
  */
 public class CamraView extends Activity {
 	WebCam camera;
 	ImageView photo;
 	
 	/**
 	 * creates new FetchImage (to be used on UI thread).
 	 */
 	private Runnable runUpdate = new Runnable() {
 		@Override
 		public void run() {
 			ImageBank.invalidate(camera.getUrl());
 			new FetchImage(photo).execute(camera.getUrl());
 		}
 	};
 	
 	/**
 	 * Runs update request on UI thread.
 	 */
 	protected void TimerClick(){
 		this.runOnUiThread(runUpdate);
 	}
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.camraviewlayout);
 		// Show the Up button in the action bar.
 		setupActionBar();
 		camera=getIntent().getExtras().getParcelable("item");
 		TextView name = (TextView) findViewById(R.id.name);
 		name.setText(camera.name);
 		photo = (ImageView) findViewById(R.id.photo);
 		new FetchImage(photo).execute(camera.getUrl());
 		Timer updateTimer = new Timer();
		updateTimer.scheduleAtFixedRate(new TimerTask() {
 			@Override
 			public void run() {
 				TimerClick();
 			}
			//camera is in seconds, and timer is in mS
		}, camera.getUpdate()*1000, camera.getUpdate()*1000);
 	}
 
 	/**
 	 * Set up the {@link android.app.ActionBar}, if the API is available.
 	 */
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void setupActionBar() {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.camra_view, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 }
