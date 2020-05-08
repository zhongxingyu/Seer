 package com.entscheidungsbaum.android.solarMonitor.subActivities;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 
 import com.entscheidungsbaum.android.solarMonitor.R;
 import com.entscheidungsbaum.android.solarMonitor.SolarButton;
 import com.entscheidungsbaum.android.solarMonitor.SolarLauncher;
 import com.entscheidungsbaum.android.solarMonitor.services.WeatherService;
 
 /**
  * @author mschlech
  *
  * SolarRelaisActivity
  */
 
 public class SolarRelaisActivity extends Activity implements SolarLauncher{
 
 
 	private static final int[] icon_mapping = { -1, R.id.icon_1, R.id.icon_2, R.id.icon_3};
 
 	private static final int[] innerIcon_mapping = { -1, R.drawable.temperature_icon, R.drawable.volumestream_icon,
 			R.drawable.back_icon };
 
 	private static final String[] text_mapping = { "", "RELAIS", "PARAMETER", "Back" };
 	View mView;
 	
 	int index = icon_mapping.length-1;
 
 	@Override
 	public void onClick(SolarButton launcherButton,Bundle bundle) {
 		Log.d("Activity", "SolarRelaisActivity");
 		setContentView(R.layout.launcher);
 
		setTheme(android.R.style.Theme_NoTitleBar);
 		super.onCreate(bundle);
 
 		setContentView(R.layout.launcher);
 		
 		int key = launcherButton.getPosition();
 		
 		
 		
 		// Generate launcher icons
 		for (int p = 1; p <= index; p++) {
 			Log.d("Button ", String.valueOf(icon_mapping[p]));
 			SolarButton button = new SolarButton(findViewById(icon_mapping[p]), p);
 			Log.d("WEATHER =>  ", String.valueOf(button.getPosition()));
 			Log.d("value text", text_mapping[p]);
 
 			button.setIcon(icon_mapping[p]);
 			button.setIcon(innerIcon_mapping[p]);
 			button.setText(text_mapping[p]);
 			// button.setIcon(innerIcon_mapping[p]);
 			button.setButtonListener(this);
 
 			// Notify the child class of the creation
 		}
 	}
 
 	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		
		setTheme(android.R.style.Theme_NoTitleBar);
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.launcher);
 		WeatherService ws = new WeatherService();
 		ws.getActualWeatherSimple();
 	}
 	
 	
 	@Override
 	public boolean onLongClick(SolarButton launcherButton, Bundle bundle) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 }
