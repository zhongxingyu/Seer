 /*
  * *************************************************************************************************
  * Copyright (C) 2010 Sense Observation Systems, Rotterdam, the Netherlands. All rights reserved.
  * *************************************************************************************************
  */
 
 package nl.sense_os.app;
 
 import nl.sense_os.app.dialogs.FaqDialog;
 import nl.sense_os.app.dialogs.LogoutConfirmDialog;
 import nl.sense_os.app.dialogs.LogoutConfirmDialog.LogoutActivity;
 import nl.sense_os.app.dialogs.SampleRateDialog;
 import nl.sense_os.app.dialogs.SyncRateDialog;
 import nl.sense_os.platform.SensePlatform;
 import nl.sense_os.service.ISenseServiceCallback;
 import nl.sense_os.service.SenseService;
 import nl.sense_os.service.SenseServiceStub;
 import nl.sense_os.service.constants.SensePrefs;
 import nl.sense_os.service.constants.SensePrefs.Auth;
 import nl.sense_os.service.constants.SensePrefs.Status;
 import nl.sense_os.service.constants.SenseStatusCodes;
 import android.annotation.TargetApi;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.RemoteException;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.CheckBox;
 import android.widget.ProgressBar;
 import android.widget.Switch;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class SenseMainActivity extends FragmentActivity implements LogoutActivity,
         SampleRateDialog.Listener, SyncRateDialog.Listener {
 
     /**
      * Task to log out the Sense service. This can take some time (due to persisting of data
      * points), so it is implemented as an {@link AsyncTask}.
      * 
      * @author Steven Mulder <steven@sense-os.nl>
      */
     private class LogoutTask extends AsyncTask<Void, Void, Boolean> {
 
         private SenseServiceStub service;
 
         @Override
         protected Boolean doInBackground(Void... params) {
             service = mSensePlatform.getService();
             service.logout();
             service.toggleMain(false);

            try {
                service.getStatus(mCallback);
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to check status after logout!", e);
                //
            }

             return true;
         }
 
         @Override
         protected void onPostExecute(Boolean result) {
             try {
                 service.getStatus(mCallback);
             } catch (RemoteException e) {
                 Log.e(TAG, "Failed to check status after logout!", e);
                 updateUi(0);
             }
         }
 
         @Override
         protected void onPreExecute() {
             setMainStatusSpinner(true);
         }
     }
 
     /**
      * Service stub for callbacks from the Sense service.
      */
     private class SenseCallback extends ISenseServiceCallback.Stub {
 
         @Override
         public void onChangeLoginResult(int result) throws RemoteException {
             // not used
         }
 
         @Override
         public void onRegisterResult(int result) throws RemoteException {
             // not used
         }
 
         @Override
         public void statusReport(final int status) {
             // Log.v(TAG, "Received status report from Sense Platform service...");
 
             runOnUiThread(new Runnable() {
 
                 @Override
                 public void run() {
                     updateUi(status);
                 }
             });
         }
     }
 
     /**
      * Receiver for broadcast events from the Sense Service, e.g. when the status of the service
      * changes.
      */
     private class SenseServiceListener extends BroadcastReceiver {
 
         @Override
         public void onReceive(Context context, Intent intent) {
             runOnUiThread(new Runnable() {
 
                 @Override
                 public void run() {
                     checkServiceStatus();
                 }
             });
         }
     }
 
     /**
      * Task to toggle the Sense service main state. This can take some time (due to persisting of
      * data points), so it is implemented as an {@link AsyncTask}.
      * 
      * @author Steven Mulder <steven@sense-os.nl>
      */
     private class ToggleMainTask extends AsyncTask<Boolean, Void, Boolean> {
 
         private SenseServiceStub service;
 
         @Override
         protected Boolean doInBackground(Boolean... params) {
             boolean active = params[0];

            busyTurningOn = active;
            busyTurningOff = !active;

             service = mSensePlatform.getService();
             service.toggleMain(active);

             return true;
         }
 
         @Override
         protected void onPostExecute(Boolean result) {
             try {
                 service.getStatus(mCallback);
             } catch (RemoteException e) {
                 Log.e(TAG, "Failed to check status after toggleMain!", e);
                 updateUi(0);
             }
         }
 
         @Override
         protected void onPreExecute() {
             setMainStatusSpinner(true);
         }
     }
 
     private static final int REQ_CODE_WELCOME = 1;
     private static final String TAG = "SenseActivity";
 
     private final ISenseServiceCallback mCallback = new SenseCallback();
     private SensePlatform mSensePlatform;
     private final SenseServiceListener mServiceListener = new SenseServiceListener();
    private boolean busyTurningOn;
    private boolean busyTurningOff;
 
     /**
      * Calls {@link ISenseService#getStatus(ISenseServiceCallback)} on the service. This will
      * generate a callback that updates the buttons ToggleButtons showing the service's state.
      */
     private void checkServiceStatus() {
         Log.v(TAG, "Check service status");
 
         try {
             // request status report
             SenseServiceStub service = mSensePlatform.getService();
             service.getStatus(mCallback);
         } catch (final IllegalStateException e) {
             Log.v(TAG, "Service not connected (yet)");
         } catch (final RemoteException e) {
             Log.e(TAG, "Error checking service status. ", e);
         }
     }
 
     @Override
     public void logout() {
         new LogoutTask().execute();
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         switch (requestCode) {
         case REQ_CODE_WELCOME:
             if (resultCode != RESULT_OK) {
                 finish();
             }
             break;
         default:
             Log.w(TAG, "Unexpected request code: " + requestCode);
             super.onActivityResult(requestCode, resultCode, data);
         }
     }
 
     /**
      * Handles clicks on the UI.
      * 
      * @param v
      *            the View that was clicked.
      */
     public void onClick(View v) {
 
         boolean oldState = false;
         if (v.getId() == R.id.device_prox_field) {
             final CheckBox devProx = (CheckBox) findViewById(R.id.device_prox_cb);
             if (devProx.isEnabled()) {
                 oldState = devProx.isChecked();
                 // devProx.setChecked(!oldState);
                 toggleDeviceProx(!oldState);
             }
         } else if (v.getId() == R.id.location_field) {
             final CheckBox location = (CheckBox) findViewById(R.id.location_cb);
             if (location.isEnabled()) {
                 oldState = location.isChecked();
                 // location.setChecked(!oldState);
                 toggleLocation(!oldState);
             }
         } else if (v.getId() == R.id.motion_field) {
             final CheckBox motion = (CheckBox) findViewById(R.id.motion_cb);
             if (motion.isEnabled()) {
                 oldState = motion.isChecked();
                 // motion.setChecked(!oldState);
                 toggleMotion(!oldState);
             }
         } else if (v.getId() == R.id.external_sensor_field) {
             final CheckBox external = (CheckBox) findViewById(R.id.external_sensor_cb);
             if (external.isEnabled()) {
                 oldState = external.isChecked();
                 // external.setChecked(!oldState);
                 toggleExternalSensors(!oldState);
             }
         } else if (v.getId() == R.id.ambience_field) {
             final CheckBox ambience = (CheckBox) findViewById(R.id.ambience_cb);
             if (ambience.isEnabled()) {
                 oldState = ambience.isChecked();
                 // ambience.setChecked(!oldState);
                 toggleAmbience(!oldState);
             }
         } else if (v.getId() == R.id.phonestate_field) {
             final CheckBox phoneState = (CheckBox) findViewById(R.id.phonestate_cb);
             if (phoneState.isEnabled()) {
                 oldState = phoneState.isChecked();
                 // phoneState.setChecked(!oldState);
                 togglePhoneState(!oldState);
             }
         } else if (v.getId() == R.id.prefs_field) {
             startActivity(new Intent(this, SenseSettings.class));
         } else {
             Log.e(TAG, "Unknown button pressed!");
         }
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.menu, menu);
         return true;
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
     }
 
     /**
      * Handles clicks on the main status field
      * 
      * @param v
      */
     @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
     public void onMainClick(View v) {
         boolean oldState = false;
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
             Switch mainSwitch = (Switch) findViewById(R.id.main_cb);
             oldState = mainSwitch.isChecked();
         } else {
             CheckBox cb = (CheckBox) findViewById(R.id.main_cb);
             oldState = cb.isChecked();
         }
         toggleMain(!oldState);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.menu_faq:
             showFaq();
             break;
         case R.id.menu_preferences:
             startActivity(new Intent(this, SenseSettings.class));
             break;
         case R.id.menu_login:
             startLogin();
             break;
         case R.id.menu_logout:
             showLogoutConfirm();
             break;
         case R.id.menu_register:
             startActivity(new Intent(this, RegisterActivity.class));
             break;
         default:
             Log.w(TAG, "Unexpected option item selected: " + item);
             return false;
         }
         return true;
     }
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
 
         boolean loggedIn = false;
         SenseServiceStub service = mSensePlatform.getService();
         if (null != service) {
             loggedIn = service.getPrefString(Auth.LOGIN_USERNAME, null) != null;
         }
 
         menu.findItem(R.id.menu_login).setVisible(!loggedIn);
         menu.findItem(R.id.menu_logout).setVisible(loggedIn);
         menu.findItem(R.id.menu_register).setVisible(!loggedIn);
 
         return true;
     }
 
     @Override
     protected void onResume() {
         super.onResume();
 
         // check login
         long lastLogin = getSharedPreferences(SensePrefs.MAIN_PREFS, MODE_PRIVATE).getLong(
                 SensePrefs.Main.LAST_LOGGED_IN, -1);
         if (lastLogin == -1) {
             // sense has never been logged in
             showWelcomeDialog();
         }
     }
 
     public void onSampleClick(View v) {
         SampleRateDialog dialog = new SampleRateDialog();
         dialog.show(getSupportFragmentManager(), "sample_rate");
     }
 
     @Override
     public void onSampleRateChanged(String rate) {
         Log.v(TAG, "Sample rate changed: " + rate);
         mSensePlatform.getService().setPrefString(SensePrefs.Main.SAMPLE_RATE, rate);
     }
 
     @Override
     protected void onStart() {
         // Log.v(TAG, "onStart");
         super.onStart();
 
         // bind to service as soon as possible
         mSensePlatform = ((SenseApplication) getApplication()).getSensePlatform();
 
         // register receiver for updates
         IntentFilter filter = new IntentFilter(SenseService.ACTION_SERVICE_BROADCAST);
         registerReceiver(mServiceListener, filter);
 
         checkServiceStatus();
     }
 
     @Override
     protected void onStop() {
         // Log.v(TAG, "onStop");
 
         // unregister service state listener
         try {
             unregisterReceiver(mServiceListener);
         } catch (IllegalArgumentException e) {
             // listener was not registered
         }
 
         super.onStop();
     }
 
     public void onSyncClick(View v) {
         SyncRateDialog dialog = new SyncRateDialog();
         dialog.show(getSupportFragmentManager(), "sync_rate");
     }
 
     @Override
     public void onSyncRateChanged(String rate) {
         Log.v(TAG, "Sync rate changed: " + rate);
         mSensePlatform.getService().setPrefString(SensePrefs.Main.SYNC_RATE, rate);
     }
 
     private void setMainStatusSpinner(boolean enable) {
         ProgressBar spinner = (ProgressBar) findViewById(R.id.main_spinner);
         View checkBox = findViewById(R.id.main_cb);
         checkBox.setVisibility(enable ? View.GONE : View.VISIBLE);
         spinner.setVisibility(enable ? View.VISIBLE : View.GONE);
     }
 
     private void showFaq() {
         FaqDialog faqDialog = new FaqDialog();
         faqDialog.show(getSupportFragmentManager(), "faq");
     }
 
     private void showLogoutConfirm() {
 
         SenseServiceStub service = mSensePlatform.getService();
         String username = null;
         if (null != service) {
             username = service.getPrefString(Auth.LOGIN_USERNAME, null);
         }
         Bundle args = new Bundle();
         args.putString("username", username);
 
         LogoutConfirmDialog logoutDialog = LogoutConfirmDialog.create(this);
         logoutDialog.setArguments(args);
         logoutDialog.show(getSupportFragmentManager(), "logout");
     }
 
     private void showToast(final CharSequence text, final int duration) {
         runOnUiThread(new Runnable() {
 
             @Override
             public void run() {
                 Toast.makeText(SenseMainActivity.this, text, duration).show();
             }
         });
     }
 
     /**
      * Shows a help dialog, which explains the goal of Sense and clicks through to Registration or
      * Login.
      */
     private void showWelcomeDialog() {
         Intent welcome = new Intent(this, WelcomeActivity.class);
         startActivityForResult(welcome, REQ_CODE_WELCOME);
     }
 
     private void startLogin() {
         startActivity(new Intent(this, LoginActivity.class));
     }
 
     private void toggleAmbience(boolean active) {
         // Log.v(TAG, "Toggle ambience: " + active);
 
         SenseServiceStub service = mSensePlatform.getService();
         if (null != service) {
 
             service.toggleAmbience(active);
 
             // show informational toast
             if (active) {
 
                 final int rate = Integer.parseInt(service.getPrefString(
                         SensePrefs.Main.SAMPLE_RATE, "0"));
                 String intervalString = "";
                 String extraString = "";
                 switch (rate) {
                 case -2:
                     intervalString = "the whole time";
                     extraString = " A sound stream will be uploaded.";
                     break;
                 case -1:
                     // often
                     intervalString = "every 10 seconds";
                     break;
                 case 0:
                     // normal
                     intervalString = "every minute";
                     break;
                 case 1:
                     // rarely (1 hour)
                     intervalString = "every 15 minutes";
                     break;
                 default:
                     Log.e(TAG, "Unexpected commonsense rate preference.");
                 }
                 String msg = getString(R.string.toast_toggle_ambience).replace("?", intervalString)
                         + extraString;
                 showToast(msg, Toast.LENGTH_LONG);
             }
 
         } else {
             Log.w(TAG, "Could not toggle ambience service: Sense service is not bound.");
         }
 
         checkServiceStatus();
     }
 
     private void toggleDeviceProx(boolean active) {
         // Log.v(TAG, "Toggle neighboring devices: " + active);
 
         SenseServiceStub service = mSensePlatform.getService();
         if (null != service) {
             service.toggleDeviceProx(active);
 
             // show informational Toast
             if (active) {
 
                 final int rate = Integer.parseInt(service.getPrefString(
                         SensePrefs.Main.SAMPLE_RATE, "0"));
                 String interval = "";
                 switch (rate) {
                 case -2: // real-time
                     interval = "second";
                     break;
                 case -1: // often
                     interval = "minute";
                     break;
                 case 0: // normal
                     interval = "5 minutes";
                     break;
                 case 1: // rarely (15 hour)
                     interval = "15 minutes";
                     break;
                 default:
                     Log.e(TAG, "Unexpected device prox preference.");
                 }
                 final String msg = getString(R.string.toast_toggle_dev_prox);
                 showToast(msg.replace("?", interval), Toast.LENGTH_LONG);
             }
         } else {
             Log.w(TAG, "Could not toggle device proximity service: Sense service is not bound.");
         }
 
         checkServiceStatus();
     }
 
     private void toggleExternalSensors(boolean active) {
         // Log.v(TAG, "Toggle external sensors: " + active);
 
         SenseServiceStub service = mSensePlatform.getService();
         if (null != service) {
             service.toggleExternalSensors(active);
 
             // show informational toast
             if (active) {
 
                 final int rate = Integer.parseInt(service.getPrefString(
                         SensePrefs.Main.SAMPLE_RATE, "0"));
                 String interval = "";
                 switch (rate) {
                 case -2: // often
                     interval = "second";
                     break;
                 case -1: // often
                     interval = "5 seconds";
                     break;
                 case 0: // normal
                     interval = "minute";
                     break;
                 case 1: // rarely
                     interval = "15 minutes";
                     break;
                 default:
                     Log.e(TAG, "Unexpected commonsense rate: " + rate);
                     break;
                 }
                 final String msg = getString(R.string.toast_toggle_external_sensors).replace("?",
                         interval);
                 showToast(msg, Toast.LENGTH_LONG);
             }
 
         } else {
             Log.w(TAG, "Could not toggle external sensors service: Sense service is not bound.");
         }
 
         checkServiceStatus();
     }
 
     private void toggleLocation(boolean active) {
         // Log.v(TAG, "Toggle location: " + active);
 
         SenseServiceStub service = mSensePlatform.getService();
         if (null != service) {
             service.toggleLocation(active);
 
             // show informational toast
             if (active) {
 
                 final int rate = Integer.parseInt(service.getPrefString(
                         SensePrefs.Main.SAMPLE_RATE, "0"));
                 String interval = "";
                 switch (rate) {
                 case -2: // often
                     interval = "second";
                     break;
                 case -1: // often
                     interval = "30 seconds";
                     break;
                 case 0: // normal
                     interval = "5 minutes";
                     break;
                 case 1: // rarely
                     interval = "15 minutes";
                     break;
                 default:
                     Log.e(TAG, "Unexpected commonsense rate: " + rate);
                     break;
                 }
                 final String msg = getString(R.string.toast_toggle_location).replace("?", interval);
                 showToast(msg, Toast.LENGTH_LONG);
             }
         } else {
             Log.w(TAG, "Could not toggle location service: Sense service is not bound.");
         }
 
         checkServiceStatus();
     }
 
     /**
      * Toggles the Sense service state. The service is started using <code>startService</code>, and
      * then the activity binds to the service. Alternatively, the service is stopped and the
      * Activity unbinds itself.
      * 
      * Afterwards, the UI is updated to make the ToggleButtons show the new service state.
      */
     private void toggleMain(boolean active) {
         // Log.v(TAG, "Toggle main: " + active);
 
         SenseServiceStub service = mSensePlatform.getService();
         if (null != service) {
             if (active && null == service.getPrefString(Auth.LOGIN_USERNAME, null)) {
                 // cannot activate the service: Sense does not know the username yet
                 Log.w(TAG, "Cannot start Sense Platform without username");
                 startLogin();
             } else {
                 // normal situation
                 new ToggleMainTask().execute(active);
             }
         }
     }
 
     private void toggleMotion(boolean active) {
         // Log.v(TAG, "Toggle motion: " + active);
 
         SenseServiceStub service = mSensePlatform.getService();
         if (null != service) {
             service.toggleMotion(active);
 
             // show informational toast
             if (active) {
 
                 final int rate = Integer.parseInt(service.getPrefString(
                         SensePrefs.Main.SAMPLE_RATE, "0"));
                 String interval = "";
                 switch (rate) {
                 case -2: // often
                     interval = "second";
                     break;
                 case -1: // often
                     interval = "5 seconds";
                     break;
                 case 0: // normal
                     interval = "minute";
                     break;
                 case 1: // rarely
                     interval = "15 minutes";
                     break;
                 default:
                     Log.e(TAG, "Unexpected commonsense rate: " + rate);
                     break;
                 }
                 final String msg = getString(R.string.toast_toggle_motion).replace("?", interval);
                 showToast(msg, Toast.LENGTH_LONG);
             }
 
         } else {
             Log.w(TAG, "Could not toggle motion service: Sense service is not bound.");
         }
 
         checkServiceStatus();
     }
 
     private void togglePhoneState(boolean active) {
         // Log.v(TAG, "Toggle phone state: " + active);
 
         // toggle state in service
         SenseServiceStub service = mSensePlatform.getService();
         if (null != service) {
 
             service.togglePhoneState(active);
 
             // show informational toast
             if (active) {
                 final String msg = getString(R.string.toast_toggle_phonestate);
                 showToast(msg, Toast.LENGTH_LONG);
             }
 
         } else {
             Log.w(TAG, "Could not toggle phone state service: Sense service is not bound.");
         }
 
         checkServiceStatus();
     }
 
     private void updateSummaries() {
 
         SharedPreferences prefs = getSharedPreferences(SensePrefs.MAIN_PREFS, MODE_PRIVATE);
 
         TextView sampleSummaryView = (TextView) findViewById(R.id.sample_details);
         int sampleRate = Integer.parseInt(prefs.getString(SensePrefs.Main.SAMPLE_RATE, "0"));
         String[] sampleSummaries = getResources().getStringArray(R.array.sample_rate_summaries);
         switch (sampleRate) {
         case -2: // real time
             sampleSummaryView.setText(sampleSummaries[0]);
             break;
         case -1: // often
             sampleSummaryView.setText(sampleSummaries[1]);
             break;
         case 0: // normal
             sampleSummaryView.setText(sampleSummaries[2]);
             break;
         case 1: // rarely
             sampleSummaryView.setText(sampleSummaries[3]);
             break;
         default:
             sampleSummaryView.setText("ERROR");
         }
 
         TextView syncSummaryView = (TextView) findViewById(R.id.sync_details);
         int syncRate = Integer.parseInt(prefs.getString(SensePrefs.Main.SYNC_RATE, "0"));
         String[] syncSummaries = getResources().getStringArray(R.array.sync_rate_summaries);
         switch (syncRate) {
         case -2: // real time
             syncSummaryView.setText(syncSummaries[0]);
             break;
         case -1: // often
             syncSummaryView.setText(syncSummaries[1]);
             break;
         case 0: // normal
             syncSummaryView.setText(syncSummaries[2]);
             break;
         case 1: // eco
             syncSummaryView.setText(syncSummaries[3]);
             break;
         default:
             syncSummaryView.setText("ERROR");
         }
     }
 
     /**
      * Enables the checkboxes to show the status of the Sense Platform service.
      * 
      * @param status
      *            The status of the service.
      * @see {@link Status#STATUSCODE_RUNNING}
      */
     @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
     private void updateUi(int status) {
 
         final boolean running = (status & SenseStatusCodes.RUNNING) > 0;

        if ((running && busyTurningOff) || (!running && busyTurningOn)) {
            // still busy
            return;
        } else {
            busyTurningOn = false;
            busyTurningOff = false;
        }

        setMainStatusSpinner(false);

         // Log.v(TAG, "'running' status: " + running);
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
             ((Switch) findViewById(R.id.main_cb)).setChecked(running);
         } else {
             ((CheckBox) findViewById(R.id.main_cb)).setChecked(running);
         }
 
         // enable phone state list row
         CheckBox button = (CheckBox) findViewById(R.id.phonestate_cb);
         final boolean callstate = (status & SenseStatusCodes.PHONESTATE) > 0;
         button.setChecked(callstate);
         button.setEnabled(running);
         View text1 = findViewById(R.id.phonestate_firstline);
         View text2 = findViewById(R.id.phonestate_secondLine);
         text1.setEnabled(running);
         text2.setEnabled(running);
         if (callstate) {
             // Log.v(TAG, "'phone state' enabled");
         }
 
         // enable location list row
         button = (CheckBox) findViewById(R.id.location_cb);
         final boolean location = (status & SenseStatusCodes.LOCATION) > 0;
         button.setChecked(location);
         button.setEnabled(running);
         button = (CheckBox) findViewById(R.id.ambience_cb);
         text1 = findViewById(R.id.location_firstline);
         text2 = findViewById(R.id.location_secondLine);
         text1.setEnabled(running);
         text2.setEnabled(running);
         if (location) {
             // Log.v(TAG, "'location' enabled");
         }
 
         // enable motion list row
         button = (CheckBox) findViewById(R.id.motion_cb);
         final boolean motion = (status & SenseStatusCodes.MOTION) > 0;
         button.setChecked(motion);
         button.setEnabled(running);
         text1 = findViewById(R.id.motion_firstline);
         text2 = findViewById(R.id.motion_secondLine);
         text1.setEnabled(running);
         text2.setEnabled(running);
         if (motion) {
             // Log.v(TAG, "'motion' enabled");
         }
 
         // enable ambience list row
         button = (CheckBox) findViewById(R.id.ambience_cb);
         final boolean ambience = (status & SenseStatusCodes.AMBIENCE) > 0;
         button.setChecked(ambience);
         button.setEnabled(running);
         button = (CheckBox) findViewById(R.id.ambience_cb);
         text1 = findViewById(R.id.ambience_firstline);
         text2 = findViewById(R.id.ambience_secondLine);
         text1.setEnabled(running);
         text2.setEnabled(running);
         if (ambience) {
             // Log.v(TAG, "'ambience' enabled");
         }
 
         // enable device proximity row
         button = (CheckBox) findViewById(R.id.device_prox_cb);
         final boolean deviceProx = (status & SenseStatusCodes.DEVICE_PROX) > 0;
         button.setChecked(deviceProx);
         button.setEnabled(running);
         text1 = findViewById(R.id.device_prox_firstline);
         text2 = findViewById(R.id.device_prox_secondLine);
         text1.setEnabled(running);
         text2.setEnabled(running);
         if (deviceProx) {
             // Log.v(TAG, "'neighboring devices' enabled");
         }
 
         // enable external sensor list row
         button = (CheckBox) findViewById(R.id.external_sensor_cb);
         final boolean external_sensors = (status & SenseStatusCodes.EXTERNAL) > 0;
         button.setChecked(external_sensors);
         button.setEnabled(running);
         text1 = findViewById(R.id.external_sensor_firstline);
         text2 = findViewById(R.id.external_sensor_secondLine);
         text1.setEnabled(running);
         text2.setEnabled(running);
         if (external_sensors) {
             // Log.v(TAG, "'external sensors' enabled");
         }
 
         updateSummaries();
     }
 }
