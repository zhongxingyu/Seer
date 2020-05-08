 package com.entscheidungsbaum.android.solarMonitor;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import android.app.Activity;
 import android.app.ActivityManager;
 import android.app.ActivityManager.RunningServiceInfo;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 
 import com.entscheidungsbaum.android.solarMonitor.datasource.DataBaseHelper;
 import com.entscheidungsbaum.android.solarMonitor.services.HttpSolarDataFetcherService;
 import com.entscheidungsbaum.android.solarMonitor.subActivities.SettingsActivity;
 import com.entscheidungsbaum.android.solarMonitor.subActivities.SolarOperatingHours;
 import com.entscheidungsbaum.android.solarMonitor.subActivities.SolarRelaisActivity;
 import com.entscheidungsbaum.android.solarMonitor.subActivities.SolarSensorActivity;
 import com.entscheidungsbaum.android.solarMonitor.subActivities.SolarVolumeStreamActivity;
 
 /**
  * @author mschlech
  * 
  *         SolarMonitor
  */
 
 public class SolarMonitor extends Activity implements SolarLauncher {
 
 	private List<SensorBean> sb = new ArrayList<SensorBean>();
 
 	/**
 	 * @TODO check if the Datasource is already read or listener implementation
 	 */
 	SolarDataSource sds = new SolarDataSource();
 
 	private static final int[] icon_mapping = { -1, R.id.icon_1, R.id.icon_2, R.id.icon_3, R.id.icon_4, R.id.icon_5,
 			R.id.icon_6 };
 	private static final int[] innerIcon_mapping = { -1, R.drawable.temperature_icon, R.drawable.volumestream_icon,
 			R.drawable.moon_icon, R.drawable.settings_icon, R.drawable.clock_icon, R.drawable.exit_icon };
 
 	private static final String[] text_mapping = { "", "Temperature Sensor", "Volume Stream", "Relais", "Settings",
 			"operating hours", "Exit" };
 
 	private int index = icon_mapping.length - 1;
 
 	public static final String SOLAR_ENGINE_DATA = "solarEngineData";
 	public static final String SOLAR_ENGINE_DATA_ARRAY = "solarEngineDataArray";
 
 	View mView;
 
 	@Override
 	public void onClick(SolarButton launcherButton, Bundle bundle) {
 		Log.d("You clicked", String.valueOf(launcherButton.getPosition()));
 
 		int key = launcherButton.getPosition();
 		switch (key) {
 		case SolarMainMenuDispatcherConstants.EXIT:
 			this.finish();
 
 			break;
 		case SolarMainMenuDispatcherConstants.TEMPERATUR_SENSORS:
 			Log.d("Jump to ", "temp sensor menu");
 			Intent ts = new Intent(this, SolarSensorActivity.class);
 			/**
 			 * @TODO this is static , dynamic data rely on the service which
 			 *       started here.
 			 */
 			// String[] DATA = { sds.getData().entrySet().toString() };
 			// ts.putStringArrayListExtra(SOLAR_ENGINE_DATA,
 			// sds.getDataArray());
 
 			// ts.putExtra(SOLAR_ENGINE_DATA, DATA);
 
 			startActivity(ts);
 			break;
 		case SolarMainMenuDispatcherConstants.RELAIS:
 			Log.d("Jump to ", "temp relais menu");
 			Intent r = new Intent(this, SolarRelaisActivity.class);
 			startActivity(r);
 			break;
 		case SolarMainMenuDispatcherConstants.VOLUME_STREAMS:
 			Log.d("Jump to ", "temp volume streams menu");
 			Intent vs = new Intent(this, SolarVolumeStreamActivity.class);
 			startActivity(vs);
 			break;
 		case SolarMainMenuDispatcherConstants.SETTINGS:
 			Log.d("Jump to ", "temp settings menu");
 			Intent s = new Intent(this, SettingsActivity.class);
 			startActivity(s);
 			break;
 		case SolarMainMenuDispatcherConstants.OPERATING_HOURS:
 			Log.d("Jump to ", "temp operating hours menu");
 			Intent oh = new Intent(this, SolarOperatingHours.class);
 			startActivity(oh);
 			break;
 		default:
 			break;
 		}
 	}
 
 	@Override
 	public boolean onLongClick(SolarButton launcherButton, Bundle bundle) {
 		boolean isLongClicked = false;
 		// isLongClicked =
 		// launcherButton.onLongClick(launcherButton.getPosition()));
 		return isLongClicked;
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		Log.d("onCreate starting ", " SolarMonitor.class");
 		/**
 		 * get Data upfront and initialize SQLlite if SolarMonitor is invoked
 		 * for the first time
 		 */
 
		setTheme(android.R.style.Theme_NoTitleBar);
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.launcher);
 
 		// Generate launcher icons
 		for (int p = 1; p <= index; p++) {
 			Log.d("p ", String.valueOf(p));
 			Log.d("Button ", String.valueOf(icon_mapping[p]));
 			Log.d("I = "," "+ icon_mapping[p] + " findViewById => " + findViewById(p));
 			SolarButton button = new SolarButton(findViewById(icon_mapping[p]), p);
 			Log.d("SOLARBUTTON =>  ", String.valueOf(button.getPosition()));
 			Log.d("value text", text_mapping[p]);
 
 			button.setIcon(icon_mapping[p]);
 			button.setIcon(innerIcon_mapping[p]);
 			button.setText(text_mapping[p]);
 			// button.setIcon(innerIcon_mapping[p]);
 			button.setButtonListener(this);
 
 			// Notify the child class of the creation
 			onCreateSolarLauncherButton(button);
 		}
 
 	}
 
 	// @Override
 	// protected void onSaveInstanceState(Bundle outState) {
 	//
 	// // for (Map.Entry<String, Number> idxSds : sds.getData().entrySet()) {
 	// //
 	// // }
 	// String[] DATA = { sds.getData().entrySet().toString() };
 	// Log.d("", DATA.toString());
 	// outState.putStringArray(SOLAR_ENGINE_DATA, DATA);
 	// super.onSaveInstanceState(outState);
 	// }
 
 	/**
 	 * 
 	 * @param button
 	 */
 	private void onCreateSolarLauncherButton(SolarButton button) {
 
 	}
 
 	/**
 	 * the database approach
 	 */
 	private void getData() {
 
 		/**
 		 * database approach
 		 */
 		final DataBaseHelper dbh = new DataBaseHelper(this);
 
 	}
 
 	/**
 	 * provide a test/development data set
 	 */
 	private void getStaticDataSet() {
 		/**
 		 * statically generated dataset
 		 */
 		for (Map.Entry<String, Number> idxSds : sds.getData().entrySet()) {
 
 		}
 
 	}
 
 	/**
 	 * checks the service which is running
 	 * 
 	 * @return
 	 */
 	protected boolean startService() {
 
 		final ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
 		final List<RunningServiceInfo> serviceInfo = activityManager.getRunningServices(Integer.MAX_VALUE);
 		boolean isService = false;
 		for (int i = 0; i < serviceInfo.size(); i++) {
 			if ("com.entscheidungsbaum.android.solarMonitor.services.HttpSolarDataFetcherService".equals(serviceInfo
 					.get(i).service.getPackageName())) {
 				isService = true;
 				Log.d("service running => ", serviceInfo.get(i).service.getPackageName().toString());
 			} else {
 				Log.d("service not running => ", serviceInfo.get(i).service.getPackageName().toString()
 						+ " starting HttpSolarDataFetcher");
 
 				Intent startIntendService = new Intent(this, HttpSolarDataFetcherService.class);
 				startService(startIntendService);
 			}
 		}
 		return isService;
 	}
 }
