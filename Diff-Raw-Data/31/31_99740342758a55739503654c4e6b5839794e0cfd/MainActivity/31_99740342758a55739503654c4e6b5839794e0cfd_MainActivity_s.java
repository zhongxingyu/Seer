 package edu.brown.cs.systems.cputemp.ui;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.View;
 import android.widget.Button;
 import android.widget.CompoundButton;
 import android.widget.NumberPicker;
 import android.widget.Switch;
 import android.widget.TextView;
 import android.widget.Toast;
 import edu.brown.cs.systems.cputemp.R;
 import edu.brown.cs.systems.cputemp.services.TemperatureControllerService;
 
 public class MainActivity extends Activity {
 
     private Switch tempControllerSwitch; /* enables CPU temperature controller */
     /* viewer of current battery temperature */
     private TextView batteryTemperature;
     private TextView cpuTemperature; /* viewer of current CPU temperature */
     /* sets CPU temp threshold for controller */
     private NumberPicker maxCpuTemperaturePicker;
     private Button applyButton;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         tempControllerSwitch = (Switch) findViewById(R.id.injectionSwitch);
         batteryTemperature = (TextView) findViewById(R.id.battTempView);
         cpuTemperature = (TextView) findViewById(R.id.currCpuTempView);
         maxCpuTemperaturePicker = (NumberPicker) findViewById(R.id.maxCpuTemperaturePicker);
         applyButton = (Button) findViewById(R.id.applyButton);
 
         maxCpuTemperaturePicker
                 .setMaxValue(TemperatureControllerService.MAX_TEMPERATURE);
         maxCpuTemperaturePicker
                 .setMinValue(TemperatureControllerService.MIN_TEMPERATURE);
 
         tempControllerSwitch
                 .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                     public void onCheckedChanged(CompoundButton buttonView,
                             boolean isChecked) {
                         if (isChecked) {
                             Intent intent = new Intent(MainActivity.this,
                                     TemperatureControllerService.class);
                             intent.setAction(TemperatureControllerService.TEMP_CONTROL_ENABLE_ACTION);
                             intent.putExtra("enabled", isChecked);
                             intent.putExtra("maxCpuTemp",
                                     maxCpuTemperaturePicker.getValue());
                             startService(intent);
                             maxCpuTemperaturePicker.setEnabled(true);
                             applyButton.setEnabled(true);
                         } else {
                             maxCpuTemperaturePicker.setEnabled(false);
                             applyButton.setEnabled(false);
                         }
                     }
                 });
 
         applyButton.setOnClickListener(new View.OnClickListener() {
 
             public void onClick(View v) {
                 if (!tempControllerSwitch.isEnabled()) {
                     Toast.makeText(MainActivity.this,
                             "Controller is not enabled", Toast.LENGTH_SHORT)
                             .show();
                 } else {
                     Intent intent = new Intent(MainActivity.this,
                             TemperatureControllerService.class);
                     intent.setAction(TemperatureControllerService.TEMP_CONTROL_SET_ACTION);
                     intent.putExtra("maxCpuTemp",
                             maxCpuTemperaturePicker.getValue());
 
                     startService(intent);
                 }
             }
         });
     }
 
     /* Save interface state when screen gets out of focus */
     @Override
     public void onPause() {
         super.onPause();
 
         unregisterReceiver(broadcastReceiver);
 
         SharedPreferences preferences = PreferenceManager
                 .getDefaultSharedPreferences(MainActivity.this);
         SharedPreferences.Editor editor = preferences.edit();
 
         editor.putBoolean("controllerOn", tempControllerSwitch.isChecked());
         editor.putInt("maxCpuTemp", maxCpuTemperaturePicker.getValue());
 
         editor.commit();
     }
 
     /* Recover interface state when screen comes back to focus */
     @Override
     public void onResume() {
         super.onResume();
 
        registerReceiver(broadcastReceiver, new IntentFilter(
                TemperatureControllerService.UPDATE_UI_ACTION));
 
         SharedPreferences preferences = PreferenceManager
                 .getDefaultSharedPreferences(MainActivity.this);
 
         tempControllerSwitch.setChecked(preferences.getBoolean("controllerOn",
                 false));
         maxCpuTemperaturePicker.setEnabled(preferences.getBoolean(
                 "controllerOn", false));
         maxCpuTemperaturePicker.setValue(preferences.getInt("maxCpuTemp",
                 TemperatureControllerService.DEFAULT_TEMPERATURE));
         applyButton.setEnabled(preferences.getBoolean("controllerOn", false));
     }
 
     /**
      * Update UI with temperature value from monitoring service
      * 
      * @param intent
      */
     private void updateUI(Intent intent) {
         if (intent.getAction().matches(
                 TemperatureControllerService.UPDATE_UI_ACTION)) {
             long battTemp = intent.getLongExtra("battTemp", -1);
             long cpuTemp = intent.getIntExtra("cpuTemp", -1);
 
             batteryTemperature.setText(battTemp != -1 ? Long.toString(battTemp)
                     : "NOT AVAILABLE");
             cpuTemperature.setText(cpuTemp != -1 ? Long.toString(cpuTemp)
                     : "NOT AVAILABLE");
         } else if (intent.getAction().matches(
                 TemperatureControllerService.SERVICE_STOP_ACTION)) {
             tempControllerSwitch.setChecked(false);
         }
     }
 
     private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             updateUI(intent);
         }
     };
 }
