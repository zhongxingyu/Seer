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
 import android.content.pm.ActivityInfo;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.TextView;
 
 /**
  * User: jottinge
  * Date: 4/2/13
  * Time: 8:24 AM
  */
 public class AlcyoneActivity extends Activity {
    String host = "192.168.1.115lone ";
     int port = 8090;
     TextView txtOctave;
     TextView txtTransposition;
     TextView txtChannel;
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
         setContentView(R.layout.main);
         txtOctave = (TextView) findViewById(R.id.txtOctave);
         txtTransposition = (TextView) findViewById(R.id.txtTransposition);
         txtChannel = (TextView) findViewById(R.id.txtChannel);
 
         Log.d("alcyone", "We have initialized");
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         Log.d("alcyone", "onPause");
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         Log.d("alcyone", "onResume");
         updateStatus();
     }
 
     private void updateStatus() {
         new AlcyoneClient(this, host, port).updateStatus();
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
                 new AlcyoneClient(this, host, port).midiReset();
                 return true;
             case R.id.mnuAlcyoneReset:
                 new AlcyoneClient(this, host, port).reset();
                 return false;
             case R.id.mnuConfigure:
                 /*
                    Note lack of preferences support here, thanks for 'splainin' so well, Android
                  */
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     public void octaveUp(View view) {
         new AlcyoneClient(this, host, port).changeOctave(AlcyoneVector.UP);
     }
 
     public void octaveDown(View view) {
         new AlcyoneClient(this, host, port).changeOctave(AlcyoneVector.DOWN);
     }
 
     public void transpositionUp(View view) {
         new AlcyoneClient(this, host, port).changeTransposition(AlcyoneVector.UP);
     }
 
     public void transpositionDown(View view) {
         new AlcyoneClient(this, host, port).changeTransposition(AlcyoneVector.DOWN);
     }
 
     public void channelUp(View view) {
         new AlcyoneClient(this, host, port).changeChannel(AlcyoneVector.UP);
     }
 
     public void channelDown(View view) {
         new AlcyoneClient(this, host, port).changeChannel(AlcyoneVector.DOWN);
     }
 }
