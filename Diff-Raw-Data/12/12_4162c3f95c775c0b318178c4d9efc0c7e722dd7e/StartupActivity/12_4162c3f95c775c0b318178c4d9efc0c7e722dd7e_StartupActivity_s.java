 /*
  * Copyright (C) 2011 The CyanogenMod Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.cyanogenmod.locpick;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.os.Bundle;
 import android.util.Log;
 
 public class StartupActivity extends Activity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 	Intent starterIntent = new Intent();
 	starterIntent.setClassName("com.android.settings","com.android.settings.LocalePickerInSetupWizard");
 	startActivityForResult(starterIntent, 1);
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
        // remove this activity from the package manager.
         PackageManager pm = getPackageManager();
         pm.setApplicationEnabledSetting("com.cyanogenmod.locpick", PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
         finish();
     }
 }
