 /*
  ** Copyright (C)  2012  Tomas Arnesson, Hampus Lilja, Mattias Herzog & Mathias Dos
  ** Permission is granted to copy, distribute and/or modify this document
  ** under the terms of the GNU Free Documentation License, Version 1.3
  ** or any later version published by the Free Software Foundation;
  ** with no Invariant Sections, no Front-Cover Texts, and no Back-Cover Texts.
  ** A copy of the license is included in the section entitled "LICENSE.txt".
  */
 /*
  ** This file is part of ShutApp.
  **
  ** ShutApp is free software: you can redistribute it and/or modify
  ** it under the terms of the GNU General Public License as published by
  ** the Free Software Foundation, either version 3 of the License, or
  ** (at your option) any later version.
  **
  ** ShutApp is distributed in the hope that it will be useful,
  ** but WITHOUT ANY WARRANTY; without even the implied warranty of
  ** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ** GNU General Public License for more details.
  **
  ** You should have received a copy of the GNU General Public License
  ** along with ShutApp.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.example.shutapp;
 
 import static com.example.shutapp.MiscResources.PROJECT_ID;
 
 import java.io.BufferedInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.Date;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.apache.http.util.ByteArrayBuffer;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.*;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 
 
 
 import com.google.android.gcm.GCMRegistrar;
 
 public class MainActivity extends Activity {
 
 
 	// This tag is used in Log.x() calls
 	private static final String TAG = "MainActivity";
 
 	private static final long SPLASHDELAY = 3000;
 
 	DatabaseHandler db;
 
 	// This string will hold the lengthy registration id that comes from GCMRegistrar.register()
 	private String regId = "";
 
 	private String registrationStatus = "Not yet registered";
 	/**
 	 * Sets the content view and 
 	 *
 	 * @param savedInstanceState the state of the saved instance
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
		//DATABASETESTS
 		db = new DatabaseHandler(this);
 		db.downloadAndCopyDB();
 
 
		Log.d("Insert: ", "Inserting ..");
		/*
		db.addChatroom(new Chatroom("chatroom1" , 11.0, 11.0, 1000));
		db.addChatroom(new Chatroom("chatroom2" , 22.0, 22.0, 1000));
		db.addChatroom(new Chatroom("chatroom3" , 33.0, 33.0, 1000));
		 */

 
 		//if it is the first time you run this app then you will have to
 		//initiate the settingsfile
 		if(Parser.checkFileExistance(StringLiterals.FILENAME_SETTINGS, this))
 			Settings.setNickname(Parser.readAtIndex(StringLiterals.NICKNAME_INDEX, StringLiterals.FILENAME_SETTINGS, this), this); 
 		else
 			Settings.initiateSettingsFile(this);
 
 		registerClient();
 
 		TimerTask splashTask = new TimerTask() {
 			
 			@Override
 			public void run() {
 				finish();
 				redirectFromMain(findViewById(R.layout.activity_main));
 			}
 		};
 		Timer timer = new Timer();
 		timer.schedule(splashTask, SPLASHDELAY);
 
 	}
 
 	/**
 	 * checks the current device, checks the 
 	 * manifest for the appropriate rights, and then retrieves a registration id
 	 * from the GCM cloud. 
 	 *
 	 */
 	public void registerClient() {
 
 		try {
 			// Check that the device supports GCM 
 			GCMRegistrar.checkDevice(this);
 
 			// Check the manifest to be sure the app has all the required
 			// permissions.
 			GCMRegistrar.checkManifest(this);
 
 			// Get the existing registration id, if it exists.
 			regId = GCMRegistrar.getRegistrationId(this);
 
 			if (regId.equals("")) {
 
 				// register this device for this project
 				GCMRegistrar.register(this, PROJECT_ID);
 				regId = GCMRegistrar.getRegistrationId(this);
 
 				Log.d(TAG, "sendregtoserver has been initialized");
 				registrationStatus = "Registration of regid done";
 
 			} else {
 				registrationStatus = "Already registered";
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 			registrationStatus = e.getMessage();
 		}
 
 		Log.d(TAG, registrationStatus);
 		Log.d(TAG, regId);
 		MiscResources.REGID = regId;
 
 	}
 
 	/**
 	 * Called when the redirect button is pressed, redirected to another activity 
 	 *
 	 * @param view a view of the text model
 	 */
 	public void redirectFromMain(View view){
 		Intent intentToRedirect = new Intent(this, NearbyConversationsActivity.class);
 		startActivity(intentToRedirect);
 		overridePendingTransition(0, 0);
 	}
 
 
 
 }
 
 
 
 
