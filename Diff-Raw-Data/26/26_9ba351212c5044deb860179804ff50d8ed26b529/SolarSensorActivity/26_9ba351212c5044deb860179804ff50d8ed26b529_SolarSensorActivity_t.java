 package com.entscheidungsbaum.android.solarMonitor.subActivities;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 
 import com.entscheidungsbaum.android.solarMonitor.R;
 import com.entscheidungsbaum.android.solarMonitor.SensorBean;
 import com.entscheidungsbaum.android.solarMonitor.SolarButton;
 import com.entscheidungsbaum.android.solarMonitor.SolarDataSource;
 import com.entscheidungsbaum.android.solarMonitor.SolarLauncher;
 import com.entscheidungsbaum.android.solarMonitor.SolarMainMenuDispatcherConstants;
 import com.entscheidungsbaum.android.solarMonitor.SolarMonitor;
 
 /**
  * @author mschlech
  * 
  *         SolarSensorActivity
  */
 
 public class SolarSensorActivity extends Activity implements SolarLauncher {
 
 	private static final int[] icon_mapping = { -1, R.id.tempIcon_1, R.id.tempIcon_2, R.id.tempIcon_3, R.id.tempIcon_4,
 			R.id.tempIcon_5, R.id.tempIcon_6, 1, 1, 1 };
 
 	private static final int[] innerIcon_mapping = { -1, R.drawable.temperature_icon, R.drawable.temperature_icon,
 			R.drawable.temperature_icon, R.drawable.temperature_icon, R.drawable.temperature_icon, R.drawable.back_icon };
 
 	private int index = icon_mapping.length -1;
 
 	/**
 	 * the bundle string which holds the data . not used because data is fetched
 	 * here
 	 */
 	public static final String SOLAR_ENGINE_DATA = "solarEngineData";
 
 	View mView;
 
 	/**
 	 * some static data for test purposes
 	 */
 	List<SensorBean> mItems = new ArrayList<SensorBean>();
 	SolarDataSource sds = new SolarDataSource();
 	String[] DATA = { sds.getData().entrySet().toString() };;
 
 	@Override
 	public void onClick(SolarButton launcherButton, Bundle bundle) {
 		Log.d("Activity", "SolarSensorActivity");
 		Log.d("You clicked", String.valueOf(launcherButton.getPosition()));
 
 		int key = launcherButton.getPosition();
 		switch (key) {
 		case SolarMainMenuDispatcherConstants.BACK_TO_MAIN:
 			Intent backToMainActivity = new Intent(this, SolarMonitor.class);
 
 			setResult(RESULT_OK, backToMainActivity);
 			finish();
 			break;
 		case SolarMainMenuDispatcherConstants.TEMPERATUR_SENSORS:
 			Log.d("Jump to ", "temp sensor menu");
 			Intent tsView = new Intent(this, SolarSensorActivity.class);
 			startActivity(tsView);
 			break;
 		case SolarMainMenuDispatcherConstants.DIAGRAMM_VIEW:
 			Log.d("Jump to ", "Diagramm sensor view");
 			Intent dsView = new Intent(this, TemperatureChartActivity.class);
 			//Intent dsView = new Intent(this, LineChartExampleView.class);
 			startActivity(dsView);
 			break;
 
 		default:
 			break;
 		}
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 
		setTheme(android.R.style.Theme_NoTitleBar);
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.sensorlauncher);
 
 		int icon_mapping_length = sds.getData().entrySet().size();
 		Log.d("iconMapping of getData is  ", " =>" + icon_mapping_length);
 		int I = 1;
 		String[] text_mapping = { DATA.toString() };
 		String ar[] = { sds.getData().entrySet().toString() };
 
 		for (Map.Entry<String, Number> idxSds : sds.getData().entrySet()) {
 			Log.d("I = "," "+ icon_mapping[I] + " findViewById => " + findViewById(icon_mapping[I]));
 			
 			SolarButton button = new SolarButton(findViewById(icon_mapping[I]), I);
 			if (idxSds.getKey().contains("tempSensor")) {
 				Log.d("ICONMAPPING ", " => " + icon_mapping[I] + "" + idxSds.getKey());
 				I++;
 				button.setIcon(R.drawable.temperature_icon);
 				button.setText(idxSds.getKey() + " " + String.valueOf(idxSds.getValue()));
 				button.setButtonListener(this);
 		} else {
 				Log.d("another type of", " KEY " + idxSds.getKey());
 			}
 		}
 
 	}
 
 	// @Override
 	// protected void onRestoreInstanceState(Bundle savedInstanceState) {
 	//
 	// savedInstanceState.getStringArray("solarEngineData");
 	// super.onRestoreInstanceState(savedInstanceState);
 	// DATA = savedInstanceState.getStringArray("solarEngineData");
 	//
 	// }
 
 	/**
 	 * 
 	 * @param button
 	 */
 	private void onCreateSolarLauncherButton(SolarButton button) {
 
 	}
 
 	@Override
 	public boolean onLongClick(SolarButton launcherButton, Bundle bundle) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 }
