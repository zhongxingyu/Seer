 /*
  * Copyright (C) 2013 bwgz.org
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License version 3 as 
  * published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.bwgz.quotation.activity;
 
 import org.bwgz.quotation.R;
 
 import android.annotation.TargetApi;
 import android.os.Build;
 import android.os.Bundle;
 import android.preference.PreferenceFragment;
 
 import com.actionbarsherlock.app.SherlockPreferenceActivity;
 
 public class SettingsActivity extends SherlockPreferenceActivity {
     @TargetApi(Build.VERSION_CODES.HONEYCOMB)
     public static class SettingsFragment extends PreferenceFragment
     {       
         @Override
         public void onCreate(Bundle savedInstanceState)
         {
             super.onCreate(savedInstanceState);
             addPreferencesFromResource(R.xml.settings);
         }
     }
 
     @TargetApi(Build.VERSION_CODES.HONEYCOMB)
     private void addFragment() {
     	getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
     }
 
    @SuppressWarnings("deprecation")
     private void addResource() {
     	addPreferencesFromResource(R.xml.settings);
     }
     
 	@Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
         	addResource();
         }
         else {
         	addFragment();
         }
     }
 }
