 package com.cyanogenmod.settings.device;
 
 import com.cyanogenmod.settings.device.R;
 
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceManager;
 import android.app.Activity;
 import android.widget.CheckBox;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.os.PowerManager;
 
 import com.cyanogenmod.settings.device.Utils;
 
 public class DeviceSettings extends PreferenceActivity {
     
     public CheckBox cb = (CheckBox) findViewById(android.R.id.cbacc);
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         addPreferencesFromResource(R.xml.warpedparts);
         }
     
     public void onCheckboxClicked(View view) {
         // Is the view now checked?
         boolean checked = ((CheckBox) view).isChecked();
         
         // Check which checkbox was clicked
         switch(view.getId()) {
             case R.id.hwacc:
                 if (checked)
                     if(android.os.SystemProperties.get(debug.sf.hw) == 0){
                         android.os.SystemProperties.set(debug.sf.hw, 1);
                        Util.checkboxChanged();
                     }
                 else
                     if(android.os.SystemProperties.get(debug.sf.hw) == 1){
                         android.os.SystemProperties.set(debug.sf.hw, 0);
                         Utils.checkboxChanged();
                     }
                 break;
         }
     }
 
     @Override
     public void onPause() {
         super.onPause();
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
         // USB charging
         if(prefs.getBoolean("usb_charging", true))
             Utils.writeValue("/sys/module/msm_battery/parameters/usb_chg_enable", 1);
         else
             Utils.writeValue("/sys/module/msm_battery/parameters/usb_chg_enable", 0);
     }
 }
