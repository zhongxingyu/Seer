 package com.itsmap.kn10731.themeproject.simpledmiapp;
 
 import java.io.File;
 import java.io.IOException;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.res.Configuration;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.content.LocalBroadcastManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.TextView;
 
 public class MainActivity extends FragmentActivity {
 
 	protected DMIApplication mDMIApplication;
 	private static final String TAG = "MainActivity";
 
 	public static File tmpFile;
 
 	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			Log.d(TAG, "Received broadcast");
 
 			FragmentManager fragMang = getSupportFragmentManager();
 			if (fragMang.findFragmentByTag("regionFragment") == null) {
 				fragMang.beginTransaction()
 						.replace(R.id.frameLayout, new RegionFragment(),
 								"regionFragment").commit();
 				fragMang.executePendingTransactions();
 			}
 
 			RegionFragment regionFragment = (RegionFragment) fragMang
 					.findFragmentByTag("regionFragment");
 
			if (intent.getStringExtra(DownloadService.CONNECTION_ERROR) == null) {
 				regionFragment.setTextVievs("Internet connection error!", "");
 			} else if (intent.getExtras().getString(DownloadService.REGION) != null) {
 				regionFragment.setTextVievs(
 						intent.getExtras().getString(
 								DownloadService.FORECAST_TEXT), intent
 								.getExtras().getString(DownloadService.REGION));
 
 				regionFragment.setRegionBitmap((Bitmap) intent
 						.getParcelableExtra(DownloadService.FORECAST_BITMAP));
 			}
 			fragMang.executePendingTransactions();
 		}
 	};
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO: check for savedInstance
 		Log.d(TAG, "onCreateMain");
 		super.onCreate(savedInstanceState);
 		mDMIApplication = (DMIApplication) this.getApplication();
 
 		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
 			Intent intent = new Intent(getBaseContext(), CityActivity.class);
 			Log.d(TAG, "Starting CityActivity");
 			startActivity(intent);
 		} else {
 			getSupportFragmentManager()
 					.beginTransaction()
 					.add(R.id.frameLayout, new LoadingFragment(),
 							"loadingFragment").commit();
 			getSupportFragmentManager().executePendingTransactions();
 
 			setContentView(R.layout.activity_main);
 
 			updateButtonClick();
 			feedbackClick();
 		}
 
 		try {
 			tmpFile = File.createTempFile("BitmapContainer", ".tmp",
 					getCacheDir());
 			Log.d(TAG, "Created file: " + tmpFile.getAbsolutePath());
 		} catch (IOException e) {
 			Log.d(TAG, e.toString());
 		}
 	}
 
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		super.onConfigurationChanged(newConfig);
 
 		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
 			Intent intent = new Intent(getBaseContext(), CityActivity.class);
 			Log.d(TAG, "Starting CityActivity");
 			startActivity(intent);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.preferences, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		Intent intent = new Intent(this, PreferencesActivity.class);
 		startActivity(intent);
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	protected void onDestroy() {
 		Log.d(TAG, "OnDestroy");
 		clearActivityReference();
 		stopService(new Intent(getApplication(), DownloadService.class));
 
 		if (tmpFile != null) {
 			Log.d(TAG, "Cache file with path: "
 					+ tmpFile.getAbsolutePath().toString() + " deleted?: "
 					+ (tmpFile.delete() == true));
 		}
 
 		super.onDestroy();
 	}
 
 	@Override
 	protected void onPause() {
 		Log.d(TAG, "onPause");
 		clearActivityReference();
 		LocalBroadcastManager.getInstance(this).unregisterReceiver(
 				mMessageReceiver);
 		super.onPause();
 	}
 
 	@Override
 	protected void onResume() {
 		Log.d(TAG, "onResume");
 		mDMIApplication.setCurrentActivity(this);
 		LocalBroadcastManager.getInstance(this).registerReceiver(
 				mMessageReceiver,
 				new IntentFilter(DownloadService.BROADCAST_RECEIVER_MAIN));
 
 		// if forecast have not been show, restart downloadService
 		if (findViewById(R.id.progressBar1) != null) {
 			Log.d(TAG, "Restarting downloadService.");
 			Intent intent = new Intent(getApplication(), DownloadService.class);
 			startService(intent);
 		}
 
 		super.onResume();
 	}
 
 	public void feedbackClick() {
 		final TextView feedback = (TextView) findViewById(R.id.feedback);
 		feedback.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// Fire intent to open e-mail app
 				Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri
 						.fromParts("mailto", getString(R.string.mail_addr),
 								null));
 
 				// emailIntent.putExtra(Intent.EXTRA_SUBJECT, "EXTRA_SUBJECT");
 				startActivity(Intent
 						.createChooser(emailIntent, "Send email..."));
 			}
 		});
 	}
 
 	public void updateButtonClick() {
 		TextView updateButton = (TextView) findViewById(R.id.updateButton);
 		updateButton.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(getApplication(),
 						DownloadService.class);
 				stopService(intent);
 				startService(intent);
 			}
 		});
 	}
 
 	private void clearActivityReference() {
 		Activity currentActivity = mDMIApplication.getCurrentActivity();
 		if (currentActivity != null && currentActivity.equals(this))
 			mDMIApplication.setCurrentActivity(null);
 	}
 }
