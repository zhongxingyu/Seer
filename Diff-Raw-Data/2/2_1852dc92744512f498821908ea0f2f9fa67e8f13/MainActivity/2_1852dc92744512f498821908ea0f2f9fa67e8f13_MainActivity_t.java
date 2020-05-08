 /*
  * Copyright 2012 Urban Airship and Contributors
  */
 
 package com.slalomdigital.smartalert;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.bluetooth.BluetoothAdapter;
 import android.content.Context;
 import android.widget.TextView;
 import android.widget.Toast;
 import com.estimote.sdk.BeaconManager;
 import com.estimote.sdk.Region;
 import com.facebook.*;
 import com.facebook.model.*;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.facebook.widget.ProfilePictureView;
 import com.slalomdigital.smartalert.beacons.BeaconServerSender;
 import com.slalomdigital.smartalert.beacons.CheckBeacons;
 import com.urbanairship.UAirship;
 import com.urbanairship.push.PushManager;
 import com.urbanairship.richpush.RichPushManager;
 import com.urbanairship.richpush.RichPushUser;
 import com.urbanairship.util.UAStringUtil;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.util.Arrays;
 import java.util.Calendar;
 
 @SuppressWarnings("unused")
 public class MainActivity extends SherlockFragmentActivity implements ActionBar.OnNavigationListener {
     private static final int REQUEST_ENABLE_BT = 1234;
     private static final String ESTIMOTE_BEACON_PROXIMITY_UUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
     private static final String ESTIMOTE_IOS_PROXIMITY_UUID = "8492E75F-4FD6-469D-B132-043FE94921D8";
     private static final Region ALL_ESTIMOTE_BEACONS_REGION = new Region("rid", null, null, null);
 
     private BeaconManager beaconManager;
 
     protected static final String TAG = "MainActivity";
 
     static final String ALIAS_KEY = "com.slalomdigital.smartalert.ALIAS";
     static final int aliasType = 1;
 
     PendingIntent checkBeaconsPendingIntent;
 
     ArrayAdapter<String> navAdapter;
     RichPushUser user;
 
     private String facebookId;
     private String facebookUserName;
     private String facebookUserLocation;
     private String facebookUserDemographic;
 
     private static MainActivity currentActivity;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         currentActivity = this;
         this.setContentView(R.layout.main);
         this.user = RichPushManager.shared().getRichPushUser();
         beaconManager = new BeaconManager(this);
 
         //enable push by default...
         PushManager.enablePush();
 
         if (facebookId == null) {
             // start Facebook Login
             Session.openActiveSession(this, true, new Session.StatusCallback() {
 
                 // callback when session changes state
                 @Override
                 public void call(Session session, SessionState state, Exception exception) {
                     if (session.isOpened()) {
                         // Make sure we have permission to get likes...
                         if (!session.getPermissions().contains("user_likes")) {
                             session.requestNewReadPermissions(new Session.NewPermissionsRequest(MainActivity.currentActivity, Arrays.asList("user_likes")));
                         }
 
                         // make request to the /me API
                         Request.newMeRequest(session, new Request.GraphUserCallback() {
 
                             // callback after Graph API response with user object
                             @Override
                             public void onCompleted(GraphUser user, Response response) {
                                 if (user != null) {
                                     facebookId = user.getId();
                                     facebookUserName = user.getName();
                                     try {
                                         facebookUserLocation = ((JSONObject) user.getProperty("location")).getString("name");
                                     } catch (JSONException e) {
                                         facebookUserLocation = "Location Unknown";
                                     }
                                     facebookUserDemographic = user.getProperty("gender").toString();
 
                                     //Try to update the view...
                                     updateView();
 
                                     //Now update the beacon CMS...
                                    BeaconServerSender.updateMobileUser(user.getFirstName(), user.getLastName(), user.getId(), PushManager.shared().getAPID(), MainActivity.currentActivity);
                                 }
                             }
                         }).executeAsync();
 
                     }
                 }
             });
         }
 
         // Check if device supports Bluetooth Low Energy.
         if (beaconManager.hasBluetooth()) {
             // Set up the check for beacons service...
             Calendar cal = Calendar.getInstance();
 
             Intent intent = new Intent(this, CheckBeacons.class);
             checkBeaconsPendingIntent = PendingIntent.getService(this, 0, intent, 0);
 
             AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
             // Start every 15 seconds
             alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 15 * 1000, checkBeaconsPendingIntent);
         }
         else {
             Toast.makeText(this, "Device does not have Bluetooth Low Energy", Toast.LENGTH_LONG).show();
             beaconManager.disconnect();
             beaconManager = null;
             return;
         }
     }
 
     private void updateView() {
         if (facebookId != null) {
             ProfilePictureView profilePictureView = (ProfilePictureView) findViewById(R.id.profilePicture);
             if (profilePictureView != null) {
                 profilePictureView.setProfileId(facebookId);
             }
 
             TextView userName = (TextView) findViewById(R.id.userName);
             if (userName != null && facebookUserName != null) {
                 userName.setText(facebookUserName);
             }
             TextView userDemographic = (TextView) findViewById(R.id.userDemographic);
             if (userDemographic != null && facebookUserDemographic != null) {
                 userDemographic.setText(facebookUserDemographic);
             }
             TextView userLocation = (TextView) findViewById(R.id.userLocation);
             if (userLocation != null && facebookUserLocation != null) {
                 userLocation.setText(facebookUserLocation);
             }
         }
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         // Remove the check for likes service
         AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
         alarm.cancel(checkBeaconsPendingIntent);
     }
 
     @Override
     protected void onStart() {
         super.onStart();
         UAirship.shared().getAnalytics().activityStarted(this);
         // If Bluetooth is not enabled, let user enable it.
         if (beaconManager != null && !beaconManager.isBluetoothEnabled()) {
             Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
             startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
         }
 
         updateView();
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         this.configureActionBar();
         this.displayMessageIfNecessary();
     }
 
     @Override
     protected void onStop() {
         super.onStop();
         UAirship.shared().getAnalytics().activityStopped(this);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         this.getSupportMenuInflater().inflate(R.menu.main_menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.preferences:
                 this.startActivity(new Intent(this, PushPreferencesActivity.class));
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     @Override
     public boolean onNavigationItemSelected(int itemPosition, long itemId) {
         String navName = this.navAdapter.getItem(itemPosition);
         if (SmartAlertApplication.HOME_ACTIVITY.equals(navName)) {
             // do nothing, we're here
         } else if (SmartAlertApplication.INBOX_ACTIVITY.equals(navName)) {
             Intent intent = new Intent(this, InboxActivity.class);
             intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
             this.startActivity(intent);
         }
         return true;
     }
 
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
     }
 
     // helpers
 
     private void displayMessageIfNecessary() {
         String messageId = this.getIntent().getStringExtra(SmartAlertApplication.MESSAGE_ID_RECEIVED_KEY);
         if (!UAStringUtil.isEmpty(messageId)) {
             MessageFragment message = MessageFragment.newInstance(messageId);
             message.show(this.getSupportFragmentManager(), R.id.floating_message_pane, "message");
             this.findViewById(R.id.floating_message_pane).setVisibility(View.VISIBLE);
         }
     }
 
     private void dismissMessageIfNecessary() {
         MessageFragment message = (MessageFragment) this.getSupportFragmentManager()
                 .findFragmentByTag("message");
         if (message != null) {
             message.dismiss();
             this.findViewById(R.id.floating_message_pane).setVisibility(View.INVISIBLE);
         }
     }
 
     private void configureActionBar() {
         ActionBar actionBar = this.getSupportActionBar();
         actionBar.setDisplayUseLogoEnabled(true);
         actionBar.setDisplayShowTitleEnabled(false);
         actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
 
         this.navAdapter = new ArrayAdapter<String>(this, R.layout.sherlock_spinner_dropdown_item,
                 SmartAlertApplication.navList);
         actionBar.setListNavigationCallbacks(this.navAdapter, this);
         actionBar.setSelectedNavigationItem(this.navAdapter.getPosition("Home"));
     }
 }
