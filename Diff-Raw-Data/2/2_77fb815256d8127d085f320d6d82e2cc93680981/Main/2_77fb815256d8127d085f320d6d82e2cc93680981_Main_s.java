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
 import java.util.concurrent.ExecutionException;
 
 import no.ntnu.idi.freerider.model.User;
 import no.ntnu.idi.freerider.protocol.Request;
 import no.ntnu.idi.freerider.protocol.RequestType;
 import no.ntnu.idi.freerider.protocol.UserRequest;
 import no.ntnu.idi.freerider.protocol.UserResponse;
 import no.ntnu.idi.socialhitchhiking.client.RequestTask;
 import no.ntnu.idi.socialhitchhiking.facebook.FBConnectionActivity;
 import no.ntnu.idi.socialhitchhiking.journey.ScheduleDrive;
 import no.ntnu.idi.socialhitchhiking.map.MapActivityCreateOrEditRoute;
 import no.ntnu.idi.socialhitchhiking.utility.SettingsManager;
 
 import org.apache.http.client.ClientProtocolException;
 
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MenuItem.OnMenuItemClickListener;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * 
  * @author Christian
  * @author Jon-Robert
  * @extends FBConnectionActivity
  */
 public class Main extends FBConnectionActivity{ 
 	private User user;
 	private Button sceduleDrive,hitchhike,notifications,myTrips,myAccount;
 	private TextView name;
 	private ImageView picture;
 	boolean isNewUser = false;
 
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);	
 		try{
 			initLoadingScreen();
 			new Thread() {
 				
 				public void run() {
 					setConnection(Main.this);
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
 			}.start();
 		}catch(Exception e){
 			AlertDialog ad = new AlertDialog.Builder(Main.this).create();
 			ad.setTitle("Server error");
 			ad.setMessage("The server is not responding.. Please try again later or contact the system administrator.");
 			ad.setButton("Ok", new DialogInterface.OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					finish();
 					System.exit(0);
 				}
 			});
 			ad.show();
 		}
 		
 	}
 	
 
 	/**
 	 * Initializes GUI components.
 	 * Is called via {@link #onCreate(Bundle)} and {@link #setName(String)}
 	 * 
 	 * @param n - A String which is used to set the users name in a TextField
 	 */
 	public void initMainScreen(){
 		//If create user crashes, this might be the problem
 		Request req2 = new UserRequest(RequestType.GET_USER, getApp().getUser());
 		UserResponse res2 = null;
 		try
 		{
 			res2 = (UserResponse)RequestTask.sendRequest(req2, getApp());
 			User resUser = res2.getUser();
 			User tempUser = getApp().getUser();
 			tempUser.setCarId(resUser.getCarId());
 			tempUser.setAbout(resUser.getAbout());
 			//Gender is already set
 			tempUser.setRating(resUser.getRating());
 			//tempUser.setRating(1);
 			getApp().setUser(tempUser);
 		} catch (ClientProtocolException e1)
 		{
 			Log.e("Error",e1.getMessage());
 		} catch (IOException e1)
 		{
 			Log.e("Error",e1.getMessage());
 		} catch (InterruptedException e1)
 		{
 			Log.e("Error",e1.getMessage());
 		} catch (ExecutionException e1)
 		{
 			Log.e("Error",e1.getMessage());
 		}
 		user = getApp().getUser();
 		
 		if(!getApp().isKey("main"))sendLoginRequest();
 		
 		runOnUiThread(new Runnable(){
 
 			@Override
 			public void run() {
 				
 				setContentView(R.layout.main_layout);
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
 				
 				myAccount.setOnClickListener(new OnClickListener() {
 					
 					@Override
 					public void onClick(View v) {
 						// TODO Auto-generated method stub
 						startMyAccount();
 					}
 				});
 				pbLogin.setVisibility(View.GONE);
 				checkSettings();
 
 			}
 			
 		});
 		if(getApp().getSettings().isPullNotifications() && !getApp().isKey("alarmService"))
 			getApp().startService();
 		getApp().setKeyState("main",true);
 	}
 	/**
 	 * Method to be called by the {@link FBConnectionActivity} when a user succesfully
 	 * logs in via Facebook.
 	 */
 	public void onResult(){
 				if(!getApp().isKey("main")){
 					createNewUser();
 				}
 				getApp().startService();
 				getApp().startJourneyReminder();
 				initMainScreen();
 				isNewUser = checkNewUser();
 				Log.e("Statisk?", "statisk");
 				Log.e("Statisk?", Boolean.toString(isNewUser));
 				
 				Main.this.runOnUiThread(new Runnable() {
 				    public void run() {
 				    	showDialogNew();
 				    }
 				});
 	}
 
 	private void showDialogNew() {
 		if(isNewUser){
 	    	new AlertDialog.Builder(Main.this)
 		    .setTitle("Welcome!")
 		    .setMessage("You should provide some basic information about yourself. Do you want to do this now?")
 		    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 		        public void onClick(DialogInterface dialog, int which) { 
 		        	if(!getApp().isKey("main"))
 		        		createNewUser();
 		        	Intent intent = new Intent(Main.this, no.ntnu.idi.socialhitchhiking.MyAccount.class);
 		        	intent.putExtra("fromDialog", true);
 		    		Main.this.startActivity(intent);
 		        }
 		     })
 		     .setNegativeButton("No", new DialogInterface.OnClickListener() {
 		        public void onClick(DialogInterface dialog, int which) { 
 		        	
 		        }
 		     })
 		     .show();
 		}
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
 					Toast msg = Toast.makeText(getApp(), "Connected", 1);
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
 		new AlertDialog.Builder(this)
 	    .setTitle("ERROR")
 	    .setMessage(msg)
 	    .setPositiveButton(buttonText, new DialogInterface.OnClickListener() {
 	        public void onClick(DialogInterface dialog, int which) { 
 	        	getID();
 	        }
 	     })
 	     .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
 	        public void onClick(DialogInterface dialog, int which) { 
 	        	finish();
 	        }
 	     })
 	     .show();
 	}
 	/**
 	 * Creates an AlertDialog to give the user the option to try to relogin
 	 * or to exit the application.
 	 */
 	public void createLoginFailedDialog(final boolean showLoginFailed,String msg,String buttonText){
 		new AlertDialog.Builder(this)
 	    .setTitle("ERROR")
 	    .setMessage(msg)
 	    .setPositiveButton(buttonText, new DialogInterface.OnClickListener() {
 	        public void onClick(DialogInterface dialog, int which) { 
 	        	loginAsNewClicked(showLoginFailed);
 	        }
 	     })
 	     .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
 	        public void onClick(DialogInterface dialog, int which) { 
 	        	finish();
 	        }
 	     })
 	     .show();
 	}
 
 	/**
 	 * Starts the Intent Inbox
 	 */
 	private void startInbox(){
 		//initActivity(no.ntnu.idi.socialhitchhiking.inbox.Inbox.class);
 		Intent intent = new Intent(this, no.ntnu.idi.socialhitchhiking.inbox.Inbox.class);
 		startActivity(intent);
 	}
 	/**
 	 * Starts the Intent FindDriver
 	 */
 	private void startFindDriver(){
 		//initActivity(no.ntnu.idi.socialhitchhiking.findDriver.FindDriver.class);
 		Intent intent = new Intent(this, no.ntnu.idi.socialhitchhiking.findDriver.FindDriver.class);
 		startActivity(intent);
 	}
 	/**
 	 * Creates AlertDialog with options on what to do
 	 */
 	private void startCreateJourney(){
 		new AlertDialog.Builder(this)
 	    .setTitle("Create ride")
 	    .setMessage("What kind of ride do you want to create?")
 	    .setNeutralButton("Reuse old ride", new DialogInterface.OnClickListener() {
 	        public void onClick(DialogInterface dialog, int which) { 
 	        	Intent intent = new Intent(Main.this, ScheduleDrive.class);
 	    		startActivity(intent);
 	        }
 	     })
 	    .setNegativeButton("New ride", new DialogInterface.OnClickListener() {
 	        public void onClick(DialogInterface dialog, int which) { 
 	        	Intent intent = new Intent(Main.this, MapActivityCreateOrEditRoute.class);
 	    		startActivity(intent);
 	        }
 	     })
 	     .setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
 	        public void onClick(DialogInterface dialog, int which) { 
 	            // do nothing
 	        }
 	     })
 	     .show();
 	}
 	public void onMyTripsClicked(View view){
 		Intent intent = new Intent(this,no.ntnu.idi.socialhitchhiking.journey.ListTrips.class);
 		startActivity(intent);
 	}
 	
 	private void startMyAccount(){
 		Intent intent = new Intent(this, no.ntnu.idi.socialhitchhiking.MyAccount.class);
 		startActivity(intent);
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
  catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
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
