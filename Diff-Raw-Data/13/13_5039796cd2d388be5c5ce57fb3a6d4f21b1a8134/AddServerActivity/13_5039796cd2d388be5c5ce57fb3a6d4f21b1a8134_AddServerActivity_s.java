 /*
  * Copyright 2012  Matthew Mole <code@gairne.co.uk>, Carlos Eduardo da Silva <kaduardo@gmail.com>
  * 
  * This file is part of ahgdc.
  * 
  * ahgdc is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * ahgdc is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with ahgdc.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package android.hgd;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 
 public class AddServerActivity extends Activity {
 	
 	private EditText host, username, port;
 	private Button serverbtn;
 
 	@Override
 	public void onConfigurationChanged(Configuration newConfig)
 	{
 	    super.onConfigurationChanged(newConfig);
 	}
 	
 	/**
 	 * Activity has been started - perhaps after having been killed.
 	 */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.addserver);
         createUI();
     }
 	
 	/**
      * Activity has just been started (following onCreate) or has been restarted
      * having been previously stopped
      */
     @Override
     public void onStart() {
     	super.onStart();
     	
     	
     }
     
     /**
      * Activity has just been resumed having been previously paused
      */
     @Override
     public void onResume() {
     	super.onResume();
     }
     
     /**
      * Activity has been paused because another activity is in the foreground (has focus)
      */
     @Override
     public void onPause() {
     	super.onPause();
     }
     
     /**
      * Activity has been stopped because it is no longer visible
      */
     @Override
     public void onStop() {
     	super.onStop();
     }
     
     /**
      * Activity has become visible again after being stopped
      */
     @Override
     public void onRestart() {
     	super.onRestart();
     }
     
     /**
      * Activity is about to shut down gracefully - i.e. without being killed for memory.
      */
     @Override
     public void onDestroy() {
     	super.onDestroy();
     }
     
     public void createUI() {
     	host = (EditText) findViewById(R.id.host);
     	username = (EditText) findViewById(R.id.username);
     	port = (EditText) findViewById(R.id.port);
     	port.setText("6633");
     	
     	serverbtn = (Button) findViewById(R.id.serverbtn);
         serverbtn.setOnClickListener(new OnClickListener() {
         	public void onClick(View v) {
                 serverbtnClicked(host.getText().toString(), username.getText().toString(), port.getText().toString());
             }
         });
     }
     
     public void serverbtnClicked(String host, String username, String port) {
     	Intent data = new Intent();
     	data.putExtra(ahgdConstants.SERVER_DATA, username + "@" + host + ":" + port);
     	setResult(RESULT_OK, data);
     	finish();
     }
 }
