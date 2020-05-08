 /**
  * @contributor(s): Freerider Team (Group 4, IT2901 Fall 2012, NTNU)
  * @version: 		1.0
  *
  * Copyright (C) 2012 Freerider Team.
  *
  * Licensed under the Apache License, Version 2.0.
  * You may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied.
  *
  * See the License for the specific language governing permissions
  * and limitations under the License.
  *
  */
 package no.ntnu.idi.socialhitchhiking;
 
 import java.io.IOException;
 
 import no.ntnu.idi.freerider.model.User;
 import no.ntnu.idi.freerider.protocol.Request;
 import no.ntnu.idi.freerider.protocol.RequestType;
 import no.ntnu.idi.freerider.protocol.UserRequest;
 import no.ntnu.idi.socialhitchhiking.client.RequestTask;
 import no.ntnu.idi.socialhitchhiking.facebook.FBConnectionActivity;
 import no.ntnu.idi.socialhitchhiking.utility.SettingsManager;
 
 import org.apache.http.client.ClientProtocolException;
 
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MenuItem.OnMenuItemClickListener;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * 
  * @author Christian
  * @extends FBConnectionActivity
  */
 public class Main extends FBConnectionActivity{ 
 	private User user;
 	private Button sceduleDrive,hitchhike,notifications,myTrips,myAccount;
 	private TextView name;
 	private ImageView picture;
 
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		initLoadingScreen();
 		setConnection(this);
 		user = getApp().getUser();
 
 		if(user == null){
 			loginButtonClicked();
 
 		}
 		else{
 			initMainScreen();
 			if(!isSession()){
 				resetSession();
 			}
 		}
 	}
 
 	/**
 	 * Initializes GUI components.
 	 * Is called via {@link #onCreate(Bundle)} and {@link #setName(String)}
 	 * 
 	 * @param n - A String which is used to set the users name in a TextField
 	 */
 	public void initMainScreen(){
 		user = getApp().getUser();
 		setContentView(R.layout.main_layout);
 		if(!getApp().isKey("main"))sendLoginRequest();
 
 		sceduleDrive = (Button) findViewById(R.id.startScreenDrive);
 		notifications = (Button) findViewById(R.id.startScreenInbox);
 		hitchhike = (Button) findViewById(R.id.startScreenHitchhike);
 		myAccount = (Button) findViewById(R.id.startScreenMyAccount);
 		myTrips = (Button) findViewById(R.id.startScreenMyTrips);
 		name = (TextView) findViewById(R.id.startScreenProfileName);
 		picture = (ImageView) findViewById(R.id.startScreenProfilePicture);
 		name.setText(user.getFullName());
 		picture.setImageBitmap(getFacebookPicture(user));
 
 		picture.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				loginAsNewClicked(true);
 			}
 		}); 
 		sceduleDrive.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				startCreateJourney();
 			}
 		});
 		hitchhike.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				startFindDriver();
 			}
 		});
 		notifications.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				startInbox();
 			}
 		});
 		pbLogin.setVisibility(View.GONE);
 		checkSettings();
 		if(getApp().getSettings().isPullNotifications() && !getApp().isKey("alarmService"))
 			getApp().startService();
 		getApp().setKeyState("main",true);
 	}
 	/**
 	 * Method to be called by the {@link FBConnectionActivity} when a user succesfully
 	 * logs in via Facebook.
 	 */
 	public void onResult(){
 		if(!getApp().isKey("main"))createNewUser();
 		getApp().startService();
 		getApp().startJourneyReminder();
 		initMainScreen();
 	}
 	@Override
 	public boolean isSession(){
 		return super.isSession();
 	}
 	private Bitmap getFacebookPicture(User user){
 		Bitmap bm = BitmapFactory.decodeByteArray(user.getPicture(), 0, user.getPicture().length);
 		return bm;
 	}
 	/**
 	 * Method to show connection settings. E.g if you're connected
 	 * to the internet or not.
 	 */
 	public void checkSettings(){
 		try{
 			SettingsManager s = getApp().getSettings();
 
 			if(s.isCheckSettings() && !getApp().isKey("main")){
 				if(s.isWifi() && s.isOnline()){
 					Toast msg = Toast.makeText(getApp(), "You're connected to the internet:)", 1);
 					msg.show();
 				}
 				if(s.isWifi() && !s.isOnline()){
 					Toast msg = Toast.makeText(getApp(), "WiFi is enabled but you're not connected to the internet.", 1);
 					msg.show();
 				}
 				if(!s.isWifi()){
 					Toast msg = Toast.makeText(getApp(), "WiFi is disabled!", 1);
 					msg.show();
 				}
 				if(!s.isBackgroundData()){
 					Toast msg = Toast.makeText(getApp(), "BackgroundData is disabled!", 1);
 					msg.show();
 				}
 			}
 		}catch(NullPointerException e){
 
 		}
 
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		menu.add("login as other user").setIcon(R.drawable.fb_icon)
 		.setOnMenuItemClickListener(new OnMenuItemClickListener() {
 			@Override
 			public boolean onMenuItemClick(MenuItem item) {
 				loginAsNewClicked(true);
 				if(getApp().getUser() != null){
 					user = getApp().getUser();
 				}
 				return false;
 			}
 		});
 		return super.onCreateOptionsMenu(menu);
 	}
 	/**
 	 * Creates an AlertDialog to give the user the option to try to relogin
 	 * or to exit the application.
 	 */
 	public void createCantConnectDialog(String msg,String buttonText){
 		AlertDialog alert =	new AlertDialog.Builder(this).create();
 		alert.setTitle("ERROR");
 		alert.setMessage(msg);
 
 		alert.setButton(buttonText, new DialogInterface.OnClickListener() {
 
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				getID();
 			} });
 		alert.setButton2("Exit", new DialogInterface.OnClickListener() {
 
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				finish();
 			} });
 		alert.show();
 	}
 	/**
 	 * Creates an AlertDialog to give the user the option to try to relogin
 	 * or to exit the application.
 	 */
 	public void createLoginFailedDialog(final boolean showLoginFailed,String msg,String buttonText){
 		AlertDialog alert =	new AlertDialog.Builder(this).create();
 		alert.setTitle("ERROR");
 		alert.setMessage(msg);
 
 		alert.setButton(buttonText, new DialogInterface.OnClickListener() {
 
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				loginAsNewClicked(showLoginFailed);
 			} });
 		alert.setButton2("Exit", new DialogInterface.OnClickListener() {
 
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				finish();
 			} });
 		alert.show();
 	}
 
 	/**
 	 * Starts the Intent FindDriver
 	 */
 	private void startInbox(){
 		initActivity(no.ntnu.idi.socialhitchhiking.inbox.Inbox.class);
 	}
 	/**
 	 * Starts the Intent FindDriver
 	 */
 	private void startFindDriver(){
 		initActivity(no.ntnu.idi.socialhitchhiking.findDriver.FindDriver.class);
 	}
 	/**
 	 * Starts the Intent MapViewActivity
 	 */
 	private void startCreateJourney(){
		initActivity(no.ntnu.idi.socialhitchhiking.journey.CreateOrLoadRide.class);
		//Intent intent = new Intent(this,no.ntnu.idi.socialhitchhiking.journey.CreateOrLoadRide.class);
 		//intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		//intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
 		//intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		//startActivity(intent);
 	}
 
 	/**
 	 * When you're already logged in and want to login as another user. 
 	 */
 	public void loginAsNewClicked(boolean showDialog){
 		if(showDialog){
 			Builder dialog = new AlertDialog.Builder(this);
 			dialog.setTitle("Logut?").setMessage("This will log you out of your current facebook session. Are you sure?");
 			dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface arg0, int arg1) {
 				}
 			});
 			dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface arg0, int arg1) {
 					initLoadingScreen();
 					getApp().reset();
 					logOut(Main.this);
 				}
 			});
 			dialog.show();
 		}
 		else{
 			initLoadingScreen();
 			getApp().reset();
 			logOut(Main.this);
 		}
 		
 
 	}
 	@Override
 	protected void onResume() {
 		if(user == null){
 			initLoadingScreen();
 		}
 		super.onResume();
 	}
 	private void initLoadingScreen(){
 		setContentView(R.layout.main_loading);
 		pbLogin = (ProgressBar)findViewById(R.id.loading_progbar);
 		pbLogin.setVisibility(View.VISIBLE);
 	}
 
 	private void createNewUser(){
 		Request req = new UserRequest(RequestType.CREATE_USER, getApp().getUser());
 		try {
 			RequestTask.sendRequest(req,getApp());
 		} catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		//System.out.println(res.toString()+", caused by: "+res.getErrorMessage());
 	}
 	/**
 	 * 
 	 * 
 	 * @param m - Main, pointer to be used in FBConnectionActivity
 	 */
 	public void loginButtonClicked(){
 		pbLogin.setVisibility(View.VISIBLE);
 
 		getID();
 	}
 
 }
