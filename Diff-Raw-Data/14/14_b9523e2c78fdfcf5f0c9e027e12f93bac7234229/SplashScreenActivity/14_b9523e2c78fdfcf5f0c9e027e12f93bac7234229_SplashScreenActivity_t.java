 /*******************************************************************************
  * Copyright (c) 2012 sfleury.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  * 
  * Contributors:
  *     sfleury - initial API and implementation
  ******************************************************************************/
 package org.gots.ui;
 
 import java.util.Calendar;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 
 import org.gots.R;
 import org.gots.action.service.ActionTODOBroadcastReceiver;
 import org.gots.analytics.GotsAnalytics;
 import org.gots.garden.GardenInterface;
 import org.gots.garden.GardenManager;
 import org.gots.garden.sql.GardenDBHelper;
 import org.gots.preferences.GotsPreferences;
 import org.gots.seed.providers.RetrieveNuxeoDocs;
 import org.gots.weather.service.WeatherUpdateService;
 import org.nuxeo.android.activities.BaseNuxeoActivity;
 import org.nuxeo.android.context.NuxeoContext;
 import org.nuxeo.android.context.NuxeoContextProvider;
 
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import com.google.android.apps.analytics.GoogleAnalyticsTracker;
 
 public class SplashScreenActivity extends Activity {
 	private static final int STOPSPLASH = 0;
 	// private static final long SPLASHTIME = 3000;
 	private static final long SPLASHTIME = 3000;
 	private GardenInterface myGarden;
 	private Context mContext;
 
 	private Handler splashHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 
 			Intent startServiceIntent = new Intent(mContext, WeatherUpdateService.class);
 			startService(startServiceIntent);
 
 			switch (msg.what) {
 			case STOPSPLASH:
 				// remove SplashScreen from view
 				Intent intent = new Intent(SplashScreenActivity.this, DashboardActivity.class);
 				startActivity(intent);
 				
 				finish();
 				break;
 			}
 			super.handleMessage(msg);
 		}
 	};
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.splash_screen);
 
 		mContext = this;
 		GotsAnalytics.getInstance(getApplication()).incrementActivityCount();
 		GoogleAnalyticsTracker.getInstance().trackPageView(getClass().getSimpleName());
 
 		Message msg = new Message();
 		msg.what = STOPSPLASH;
 
 		PackageInfo pInfo;
 		try {
 			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
 			String version = pInfo.versionName;
 			TextView name = (TextView) findViewById(R.id.textVersion);
 			name.setText("version " + version);
 		} catch (NameNotFoundException e) {
 			e.printStackTrace();
 		}
 
 		LinearLayout artmoni = (LinearLayout) findViewById(R.id.webArtmoni);
 		artmoni.setOnClickListener(new LinearLayout.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.artmoni.eu"));
 				startActivity(browserIntent);
 
 			}
 		});
 		
 		LinearLayout sauterdanslesflaques = (LinearLayout) findViewById(R.id.webSauterDansLesFlaques);
 		sauterdanslesflaques.setOnClickListener(new LinearLayout.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.sauterdanslesflaques.com"));
 				startActivity(browserIntent);
 
 			}
 		});
 
 		ImageView socialGoogle = (ImageView) findViewById(R.id.idSocialGoogle);
 		socialGoogle.setOnClickListener(new LinearLayout.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri
 						.parse("https://plus.google.com/u/0/b/108868805153744305734/communities/105269291264998461912"));
 				startActivity(browserIntent);
 
 			}
 		});
 
 		ImageView socialFacebook = (ImageView) findViewById(R.id.idSocialFacebook);
 		socialFacebook.setOnClickListener(new LinearLayout.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri
 						.parse("http://www.facebook.com/pages/Gardening-Manager/120589404779871"));
 				startActivity(browserIntent);
 
 			}
 		});
 
 //		ImageView previmeteo = (ImageView) findViewById(R.id.idPrevimeteo);
 //		previmeteo.setOnClickListener(new LinearLayout.OnClickListener() {
 //
 //			@Override
 //			public void onClick(View v) {
 //				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.previmeteo.com/"));
 //				startActivity(browserIntent);
 //
 //			}
 //		});
 		// Intent startServiceIntent = new Intent(this,
 		// NotificationService.class);
 		// this.startService(startServiceIntent);
 		setRecurringAlarm(this);
 
 		GardenManager gardenManager = new GardenManager(this);
 		myGarden = gardenManager.getcurrentGarden();
 		
 		if (myGarden == null) {
 			Intent intent = new Intent(this, ProfileCreationActivity.class);
 			startActivityForResult(intent, 0);
 
 		} else {
 			if (GotsPreferences.isDevelopment())
 				splashHandler.sendMessageDelayed(msg, 0);
 			else
 				splashHandler.sendMessageDelayed(msg, SPLASHTIME);
 
			
 
 		}
 
 
 	}
 
 	@Override
 	protected void onResume() {
 		// Message msg = new Message();
 		// msg.what = STOPSPLASH;
 		// splashHandler.sendMessageDelayed(msg, SPLASHTIME);
 		
//		Intent intent = new Intent(SplashScreenActivity.this, DashboardActivity.class);
//		startActivity(intent);
 		
 		super.onResume();
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 
 		GardenDBHelper helper = new GardenDBHelper(this);
 		SharedPreferences preferences = getSharedPreferences("org.gots.preference", 0);
 		myGarden = helper.getGarden(preferences.getInt("org.gots.preference.gardenid", 0));
 		if (myGarden != null) {
 			Message msg = new Message();
 			msg.what = STOPSPLASH;
 			splashHandler.sendMessageDelayed(msg, SPLASHTIME);
 
 		} else
 			finish();
 
 		super.onActivityResult(requestCode, resultCode, data);
 	}
 
 	@Override
 	protected void onDestroy() {
 
 		GotsAnalytics.getInstance(getApplication()).decrementActivityCount();
 		super.onDestroy();
 	}
 
 	private void setRecurringAlarm(Context context) {
 		// we know mobiletuts updates at right around 1130 GMT.
 		// let's grab new stuff at around 11:45 GMT, inexactly
 		Calendar updateTime = Calendar.getInstance();
 		// updateTime.setTimeInMillis(System.currentTimeMillis());
 		// updateTime.add(Calendar.SECOND, 10);
 		// updateTime.setTimeZone(TimeZone.getTimeZone("GMT"));
 		// updateTime.set(Calendar.HOUR_OF_DAY, 12);
 		// updateTime.set(Calendar.MINUTE, 15);
 		Intent downloader = new Intent(context, ActionTODOBroadcastReceiver.class);
 		PendingIntent actionTODOIntent = PendingIntent.getBroadcast(context, 0, downloader,
 				PendingIntent.FLAG_UPDATE_CURRENT);
 		AlarmManager alarms = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
 
 		if (GotsPreferences.isDevelopment())
 			alarms.setInexactRepeating(AlarmManager.RTC_WAKEUP, updateTime.getTimeInMillis(),
 					AlarmManager.INTERVAL_FIFTEEN_MINUTES, actionTODOIntent);
 		else {
 			updateTime.set(Calendar.HOUR_OF_DAY, 20);
 			alarms.setInexactRepeating(AlarmManager.RTC_WAKEUP, updateTime.getTimeInMillis(),
 					AlarmManager.INTERVAL_DAY, actionTODOIntent);
 		}
 	}
 
 	
 
 }
