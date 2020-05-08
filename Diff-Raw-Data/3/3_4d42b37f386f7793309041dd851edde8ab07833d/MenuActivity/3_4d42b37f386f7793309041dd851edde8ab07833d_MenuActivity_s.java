 /*
  * Copyright [2012] [Mei Ha, Martin Augustsson, Simon Fransson, Emma Dirnberger]
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License. 
  */
 
 package com.chalmers.schmaps;
 
 
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.Toast;
 /**
  * MenuActivity contains buttons on the menu and determine which activity will start
  * when the buttons are pressed.
  */
 
 public class MenuActivity extends Activity implements View.OnClickListener {
 	private static final int MICROWAVEBUTTON = 1;
 	private static final int RESTAURANTBUTTON = 2;
 	private static final int ATMBUTTON = 3;
 
 	private Intent startActivity;
 
 	private Button searchHall, groupRoom,atmButton,microwaveButton,findRestaurantsButton, checkin, bus;
 
 
 	private String activityString;
 	private boolean okToStartActivity;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_menu);
 		assignInstances();
 	}
 
 	private void assignInstances() {
 		searchHall = (Button) findViewById(R.id.searchHallButton);
 		searchHall.setOnClickListener(this);
 		microwaveButton = (Button) findViewById(R.id.microwaveButton);
 		microwaveButton.setOnClickListener(this);
 		findRestaurantsButton = (Button) findViewById(R.id.findRestaurantsButton);
 		findRestaurantsButton.setOnClickListener(this);
 		groupRoom = (Button) findViewById(R.id.groupRoomButton);
 		groupRoom.setOnClickListener(this);
 		atmButton = (Button) findViewById(R.id.atmButton);
 		atmButton.setOnClickListener(this);
 		checkin = (Button) findViewById(R.id.checkinButton);
 		checkin.setOnClickListener(this);
 		bus = (Button) findViewById(R.id.checkbusButton);
 		bus.setOnClickListener(this);
 		okToStartActivity = true;
 	}
 
 	/**
 	 * onClick method for determining which activity will start through the use of view ID's and
 	 * a switch case to start correct activity with correct variables.
 	 */
 	public void onClick(View v) {
 		switch(v.getId()){
 
 		case R.id.searchHallButton:
 			startActivity = new Intent("android.intent.action.GOOGLEMAPSEARCHLOCATION");
 			break;
 
 		case R.id.microwaveButton:
 			startActivity = new Intent("android.intent.action.CAMPUSMENUACTIVITY");
 			startActivity.putExtra("Show locations", MICROWAVEBUTTON);;
 			break;
 
 		case R.id.findRestaurantsButton:
 			startActivity = new Intent("android.intent.action.CAMPUSMENUACTIVITY");
 			startActivity.putExtra("Show locations", RESTAURANTBUTTON);
 			break;
 
 		case R.id.atmButton:
 			startActivity = new Intent("android.intent.action.CAMPUSMENUACTIVITY");
 			startActivity.putExtra("Show locations", ATMBUTTON);	
 			break;
 
 		case R.id.groupRoomButton:
 			//Start the group room activity
 			startActivity = new Intent("android.intent.action.GROUPROOM");
 			break;
 
 
 		case R.id.checkinButton:
 			if(gotInternetConnection()){
 				startActivity = new Intent("android.intent.action.CHECKINACTIVITY");
 			}else{
 
 				Context context = getApplicationContext();
 				Toast.makeText(context, "Internet connection needed for this option", Toast.LENGTH_LONG).show();
 				okToStartActivity = false;
 			}
 			break;
 
 		case R.id.checkbusButton:
 			if(gotInternetConnection()){
 				startActivity = new Intent("android.intent.action.CHECKBUSACTIVITY");
 			}
 
 			else
 			{
 				okToStartActivity = false;
 				Context context = getApplicationContext();
 				Toast.makeText(context, "Internet connection needed for this option", Toast.LENGTH_LONG).show();
 			}
 			break;	
 
 		}
 		if(okToStartActivity){
 			setActivityString(startActivity.getAction());
 			startActivity(startActivity);
			okToStartActivity = true;
 		}
 	}
 
 	public String getActivityString() {
 		return activityString;
 	}
 
 	public void setActivityString(String activityString) {
 		this.activityString = activityString;
 	}
 
 	/**
 	 * Check if the device is connected to internet.
 	 * Need three if-statements because getActiveNetworkInfo() may return null
 	 * and end up with a force close. So thats the last thing to check.
 	 * @return true if there is an internet connection
 	 */
 	public boolean gotInternetConnection()
 	{
 		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 
 		NetworkInfo wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
 		if (wifiNetwork != null && wifiNetwork.isConnected()) {
 			return true;
 		}
 
 		NetworkInfo mobileNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
 		if (mobileNetwork != null && mobileNetwork.isConnected()) {
 			return true;
 		}
 
 		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
 		if (activeNetwork != null && activeNetwork.isConnected()) {
 			return true;
 		}
 
 		return false;
 	}
 
 
 
 }
