 package com.entscheidungsbaum.android.solarMonitor.subActivities;
 
 import com.entscheidungsbaum.android.solarMonitor.R;
 import com.entscheidungsbaum.android.solarMonitor.SolarButton;
 import com.entscheidungsbaum.android.solarMonitor.SolarLauncher;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 
 /**
  * @author mschlech
  *
  * SettingsActivity
  */
 
 public class SettingsActivity extends Activity implements SolarLauncher {
 
 
 	private static final int[] icon_mapping = { -1, R.id.icon_1, R.id.icon_2, R.id.icon_3};
 
 	private static final int[] innerIcon_mapping = { -1, R.drawable.temperature_icon, R.drawable.volumestream_icon,
 			R.drawable.back_icon };
 
 	private static final String[] text_mapping = { "", "Sensor selection", "Diagram", "Back" };
 	View mView;
 
 	int index = icon_mapping.length-1;
 
 	
 	@Override
 	public void onClick(SolarButton launcherButton,Bundle bundle) {
 		Log.d("Activity", "SettingsActivity");
 
 	}
 
 	
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 
		setTheme(android.R.style.Theme_NoTitleBar);
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.launcher);
 
 		// Generate launcher icons
 //		for (int p = 1; p <= 6; p++) {
 //			SolarButton button = new SolarButton(findViewById(icon_mapping[p]), p);
 //			Log.d("SOLARBUTTON =>  ", String.valueOf(button.getPosition()));
 //			Log.d("value text", text_mapping[p]);
 //
 //			button.setIcon(icon_mapping[p]);
 //			button.setIcon(innerIcon_mapping[p]);
 //			button.setText(text_mapping[p]);
 //			// button.setIcon(innerIcon_mapping[p]);
 //			button.setButtonListener(this);
 //
 //			// Notify the child class of the creation
 //			onCreateSolarLauncherButton(button);
 //		}
 
 	}
 
 	@Override
 	public boolean onLongClick(SolarButton launcherButton, Bundle bundle) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 }
