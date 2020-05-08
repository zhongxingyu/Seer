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
 import java.util.GregorianCalendar;
 
 import org.gots.R;
 import org.gots.analytics.GotsAnalytics;
 import org.gots.garden.GardenManager;
 import org.gots.garden.sql.GardenDBHelper;
 import org.gots.help.HelpUriBuilder;
 import org.gots.weather.WeatherCondition;
 import org.gots.weather.WeatherConditionInterface;
 import org.gots.weather.WeatherManager;
 import org.gots.weather.service.WeatherUpdateService;
 import org.gots.weather.view.WeatherView;
 
 import android.app.ProgressDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.location.Address;
 import android.location.Location;
 import android.location.LocationManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.HorizontalScrollView;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.google.android.apps.analytics.GoogleAnalyticsTracker;
 
 public class ProfileActivity extends SherlockActivity {
 	private LocationManager mlocManager;
 	private Location location;
 	private Address address;
 	private String tag = "ProfileActivity";
 	// EditText locality;
 	private String choix_source = "";
 	private ProgressDialog pd;
 	private int gardenId;
 	private String TAG = "ProfileActivity";
 	private GardenManager gardenManager;
 	private LinearLayout weatherHistory;
 	// private WeatherWidget weatherWidget;
 	private WeatherManager weatherManager;
 	private Spinner gardenSelector;
 	private Intent weatherIntent;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.profile);
 
 		ActionBar bar = getSupportActionBar();
 		bar.setDisplayHomeAsUpEnabled(true);
 		bar.setTitle(R.string.dashboard_profile_name);
 
 		GotsAnalytics.getInstance(getApplication()).incrementActivityCount();
 		GoogleAnalyticsTracker.getInstance().trackPageView(getClass().getSimpleName());
 
 		gardenManager = new GardenManager(this);
 		weatherManager = new WeatherManager(this);
 
 		final HorizontalScrollView scrollView = (HorizontalScrollView) findViewById(R.id.scrollWeatherHistory);
 
 		scrollView.post(new Runnable() {
 			@Override
 			public void run() {
 				scrollView.scrollTo(scrollView.getWidth(), scrollView.getHeight());
 			}
 		});
 		weatherIntent = new Intent(this, WeatherUpdateService.class);
 		weatherState = (ImageView) findViewById(R.id.idWeatherConnected);
 		weatherState.setOnClickListener(new View.OnClickListener() {
			
 			@Override
 			public void onClick(View v) {
 				weatherState.setImageDrawable(getResources().getDrawable(R.drawable.weather_updating));
 				startService(weatherIntent);
 				registerReceiver(weatherBroadcastReceiver, new IntentFilter(WeatherUpdateService.BROADCAST_ACTION));
				
 			}
 		});
 	}
 
 	private void buildWeatherList() {
 
 		weatherHistory = (LinearLayout) findViewById(R.id.layoutWeatherHistory);
 
 		if (weatherHistory.getChildCount() > 0)
 			weatherHistory.removeAllViews();
 
 		for (int i = -10; i <= 0; i++) {
 			WeatherConditionInterface condition;
 			try {
 				condition = weatherManager.getCondition(i);
 			} catch (Exception e) {
 				Calendar weatherday = new GregorianCalendar();
 				weatherday.setTime(Calendar.getInstance().getTime());
 				weatherday.add(Calendar.DAY_OF_YEAR, i);
 
 				condition = new WeatherCondition();
 				condition.setDate(weatherday.getTime());
 			}
 
 			WeatherView view = new WeatherView(this);
 			view.setWeather(condition);
 			view.setPadding(2, 0, 2, 0);
 			weatherHistory.addView(view);
 
 		}
 
 	}
 
 	private void buildGardenList() {
 		GardenDBHelper helper = new GardenDBHelper(this);
 
 		String[] referenceList = helper.getGardens();
 
 		if (referenceList == null)
 			return;
 
 		gardenSelector = (Spinner) findViewById(R.id.idGardenSelector);
 
 		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item_text, referenceList);
 		gardenSelector.setAdapter(adapter);
 
 		gardenSelector.setSelection((int) gardenManager.getcurrentGarden().getId() - 1);
 
 		gardenSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
 
 			@Override
 			public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
 
 				gardenManager.setCurrentGarden(position + 1);
 				// buildWeatherList();
 
 				GoogleAnalyticsTracker tracker = GoogleAnalyticsTracker.getInstance();
 				tracker.trackEvent("Garden", "Select", gardenManager.getcurrentGarden().getLocality(), position + 1);

 				weatherState.setImageDrawable(getResources().getDrawable(R.drawable.weather_updating));
 				startService(weatherIntent);
 				registerReceiver(weatherBroadcastReceiver, new IntentFilter(WeatherUpdateService.BROADCAST_ACTION));
 
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0) {
 				// TODO Auto-generated method stub
 
 			}
 		});
 	}
 
 	private BroadcastReceiver weatherBroadcastReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			updateUI(intent);
 		}
 	};
 	private ImageView weatherState;
 
 	private void updateUI(Intent intent) {
 		boolean isError = intent.getBooleanExtra("error", true);
 		Log.d(TAG, "=>" + isError);
 
 		TextView txtError = (TextView) findViewById(R.id.idTextAlert);

 		if (isError) {
 			txtError.setVisibility(View.VISIBLE);
 			weatherState.setImageDrawable(getResources().getDrawable(R.drawable.weather_disconnected));
 		} else {
 			txtError.setVisibility(View.GONE);
 			weatherState.setImageDrawable(getResources().getDrawable(R.drawable.weather_connected));
 		}
 		buildWeatherList();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		buildGardenList();
		
 		weatherState.setImageDrawable(getResources().getDrawable(R.drawable.weather_updating));
 		startService(weatherIntent);
 		registerReceiver(weatherBroadcastReceiver, new IntentFilter(WeatherUpdateService.BROADCAST_ACTION));
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		unregisterReceiver(weatherBroadcastReceiver);
 		stopService(weatherIntent);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		if (gardenManager.getcurrentGarden() != null) {
 
 			MenuInflater inflater = getSupportMenuInflater();
 			inflater.inflate(R.menu.menu_profile, menu);
 
 		}
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 
 		// if (gardenManager.getNbGarden() == 1) {
 		// MenuItem item = menu.findItem(R.id.delete_garden);
 		// item.setEnabled(false);
 		// }
 		return super.onPrepareOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 
 		case android.R.id.home:
 			finish();
 			return true;
 
 		case R.id.help:
 			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(HelpUriBuilder.getUri(getClass()
 					.getSimpleName())));
 			startActivity(browserIntent);
 
 			return true;
 		case R.id.new_gaden:
 
 			Intent i = new Intent(this, ProfileCreationActivity.class);
 			startActivity(i);
 
 			return true;
 			// case R.id.delete_garden:
 			//
 			// deleteGarden();
 			// buildGardenList();
 			// return true;
 		case R.id.edit_garden:
 
 			// deleteGarden();
 			Intent intent = new Intent(this, ProfileCreationActivity.class);
 			intent.putExtra("option", ProfileCreationActivity.OPTION_EDIT);
 			startActivity(intent);
 
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	private void deleteGarden() {
 		gardenManager.removeCurrentGarden();
 	}
 
 	@Override
 	protected void onDestroy() {
 		GotsAnalytics.getInstance(getApplication()).decrementActivityCount();
 		super.onDestroy();
 	}
 }
