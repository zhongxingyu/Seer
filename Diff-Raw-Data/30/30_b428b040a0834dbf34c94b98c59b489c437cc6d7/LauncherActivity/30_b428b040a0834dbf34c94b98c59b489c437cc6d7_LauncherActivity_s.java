 /*
  *  This file is a part of GPS Lock-Lock Android application.
  *  Copyright (C) 2011 Tomasz Dudziak
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.github.tdudziak.gps_lock_lock;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.content.DialogInterface.OnCancelListener;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 
 public class LauncherActivity extends Activity implements OnCancelListener, OnClickListener {
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 
         if(manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            startService(new Intent(this, LockService.class));

            // Launch control activity if configured so.
             SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            if(!prefs.getBoolean("backgroundLaunch", false)) {
                 startActivity(new Intent(this, ControlActivity.class));
             }
 
             finish();
         } else {
             AlertDialog.Builder ab = new AlertDialog.Builder(this);
             ab.setMessage(R.string.alert_nogps)
               .setOnCancelListener(this)
               .setNegativeButton("Ok", this)
               .create()
               .show();
         }
     }
 
     @Override
     public void onCancel(DialogInterface dialog) {
         finish();
     }
 
     @Override
     public void onClick(DialogInterface dialog, int which) {
         finish();
     }
 }
