 /*
     Copyright 2012- by Joseph B. Ottinger.
 
     This file is part of Alcyone.
 
     Alcyone is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     Alcyone is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Alcyone.  If not, see <http://www.gnu.org/licenses/>.
 */
 package com.redhat.osas.alcyone;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.ActivityInfo;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.TextView;
 import com.redhat.osas.alcyone.client.AlcyoneClientFactory;
 
 /**
  * User: jottinge
  * Date: 4/2/13
  * Time: 8:24 AM
  */
 public class AlcyoneActivity extends Activity {
     private String host = "192.168.1.108";
     private int port = 8090;
     private TextView txtOctave;
     private TextView txtTransposition;
     private TextView txtChannel;
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
         setContentView(R.layout.main);
         txtOctave = (TextView) findViewById(R.id.txtOctave);
         txtTransposition = (TextView) findViewById(R.id.txtTransposition);
         txtChannel = (TextView) findViewById(R.id.txtChannel);
 
         Log.d("alcyone", "We have initialized");
     }
 
     public void updateStatus(AlcyoneStatus alcyoneStatus) {
         txtChannel.setText(Integer.toString(alcyoneStatus.getChannel()));
         txtOctave.setText(Integer.toString(alcyoneStatus.getOctave()));
         txtTransposition.setText(Integer.toString(alcyoneStatus.getTransposition()));
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         SharedPreferences preferences = getPreferences(MODE_PRIVATE);
         SharedPreferences.Editor editor = preferences.edit();
         editor.putString("host", host);
         editor.putInt("port", port);
         editor.commit();
         Log.d("alcyone", "onPause");
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         Log.d("alcyone", "onResume");
         SharedPreferences preferences = getSharedPreferences("alcyone", 0);
         host = preferences.getString("host", "piui");
         port = preferences.getInt("port", 8090);
         Log.d("alcyone", "Setting host to '" + host + "', port to " + port);
         updateStatus();
     }
 
     private void updateStatus() {
         AlcyoneClientFactory.build(this, host, port).updateStatus();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.alcyone_menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.mnuMIDIReset:
                 AlcyoneClientFactory.build(this, host, port).midiReset();
                 return true;
             case R.id.mnuAlcyoneReset:
                 AlcyoneClientFactory.build(this, host, port).reset();
                return false;
             case R.id.mnuConfigure:
                 startActivity(new Intent(this, EditPreferencesActivity.class));
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     public void octaveUp(@SuppressWarnings("UnusedParameters") View view) {
         AlcyoneClientFactory.build(this, host, port).changeOctave(AlcyoneVector.UP);
     }
 
     public void octaveDown(@SuppressWarnings("UnusedParameters") View view) {
         AlcyoneClientFactory.build(this, host, port).changeOctave(AlcyoneVector.DOWN);
     }
 
     public void transpositionUp(@SuppressWarnings("UnusedParameters") View view) {
         AlcyoneClientFactory.build(this, host, port).changeTransposition(AlcyoneVector.UP);
     }
 
     public void transpositionDown(@SuppressWarnings("UnusedParameters") View view) {
         AlcyoneClientFactory.build(this, host, port).changeTransposition(AlcyoneVector.DOWN);
     }
 
     public void channelUp(@SuppressWarnings("UnusedParameters") View view) {
         AlcyoneClientFactory.build(this, host, port).changeChannel(AlcyoneVector.UP);
     }
 
     public void channelDown(@SuppressWarnings("UnusedParameters") View view) {
         AlcyoneClientFactory.build(this, host, port).changeChannel(AlcyoneVector.DOWN);
     }
 }
