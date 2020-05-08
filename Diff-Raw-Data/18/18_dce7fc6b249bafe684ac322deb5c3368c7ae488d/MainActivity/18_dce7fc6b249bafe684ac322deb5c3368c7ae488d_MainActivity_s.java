 /*
 ** Copyright (C) 2011 The Liquid Settings Project
 **
 ** Licensed under the Apache License, Version 2.0 (the "License"); 
 ** you may not use this file except in compliance with the License. 
 ** You may obtain a copy of the License at 
 **
 **     http://www.apache.org/licenses/LICENSE-2.0 
 **
 ** Unless required by applicable law or agreed to in writing, software 
 ** distributed under the License is distributed on an "AS IS" BASIS, 
 ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 ** See the License for the specific language governing permissions and 
 ** limitations under the License.
 */
 
 package com.liquid.settings.activities;
 
 import android.os.Bundle;
 import android.preference.PreferenceActivity;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 
 import com.liquid.settings.R;
 import com.liquid.settings.utilities.RootHelper;
 
 public class MainActivity extends PreferenceActivity {
 
     /*TODO unsure of what command to send to make bootloader reboot */
 
     /* handles for our menu hard key press */
     private final int MENU_REBOOT = 1;
     private final int MENU_RECOVERY = 2;
 /*  private final int MENU_BOOTLOADER = 3; */
     private final int MODE_RECOVERY = 1;
 /*  private final int MODE_BOOTLOADER = 69 I have no idea what we need to do to boot into bootloader */
     private final String REBOOT = "Reboot";
     private final String RECOVERY = "Recovery";
 /*  private final String BOOTLOADER = "Bootloader"; */
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         addPreferencesFromResource(R.xml.liquid_settings);
     }
 
     public boolean onCreateOptionsMenu(Menu menu){
         boolean result = super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_REBOOT, 0, RECOVERY).setIcon(R.drawable.menu_recovery);
        menu.add(0, MENU_RECOVERY, 0, REBOOT).setIcon(R.drawable.menu_reboot);
 /*      menu.add(0, MENU_BOOTLOADER, 0, BOOTLOADER).setIcon(R.drawable.bootloader); */
         return result;
     }
  
     /* Handle the menu selection */
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case MENU_REBOOT:
             return RootHelper.runRootCommand("reboot");
         case MENU_RECOVERY:
            return RootHelper.reboot(MODE_RECOVERY);
 /*      case MENU_BOOTLOADER:
             return RootHelper.reboot(MODE_BOOTLOADER); */
         }
         return false;
     }
 }
