 /*
  * **************************************************************************************************
  * Copyright (C) 2010 Sense Observation Systems, Rotterdam, the Netherlands. All rights reserved. *
  * **
  * ************************************************************************************************
  */
 package nl.sense_os.app;
 
 import nl.sense_os.app.dialogs.LoginDialog;
 import nl.sense_os.app.dialogs.RegisterDialog;
 import nl.sense_os.service.Constants;
 import nl.sense_os.service.ISenseService;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.ComponentName;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceClickListener;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.widget.Toast;
 
 public class SenseSettings extends PreferenceActivity {
 
     /**
      * AsyncTask to check the login data with CommonSense. Takes the username and unhashed password
      * as arguments. Clears any open login dialogs before start, and displays a progress dialog
      * during operation. If the login fails, the login dialog is shown again.
      */
     private class CheckLoginTask extends AsyncTask<String, Void, Integer> {
         private static final String TAG = "CheckLoginTask";
 
         @Override
         protected Integer doInBackground(String... params) {
             int result = -1;
 
             String username = params[0];
             String password = params[1];
 
             if (service != null) {
                 try {
                     result = service.changeLogin(username, password);
                 } catch (final RemoteException e) {
                     Log.e(TAG, "RemoteException changing login.", e);
                 }
             } else {
                 Log.w(TAG, "Skipping login task: service=null. Is the service bound?");
             }
             return result;
         }
 
         @Override
         protected void onPostExecute(Integer result) {
             try {
                 dismissDialog(DIALOG_PROGRESS);
             } catch (final IllegalArgumentException e) {
                 // do nothing
             }
             if (result == -2) {
                 Toast.makeText(SenseSettings.this, R.string.toast_login_forbidden,
                         Toast.LENGTH_LONG).show();
                 showDialog(DIALOG_LOGIN);
             } else if (result == -1) {
                 Toast.makeText(SenseSettings.this, R.string.toast_login_fail, Toast.LENGTH_LONG)
                         .show();
                 showDialog(DIALOG_LOGIN);
             } else {
                 Toast.makeText(SenseSettings.this, R.string.toast_login_ok, Toast.LENGTH_LONG)
                         .show();
 
                 // toggle main service state
                 if (service != null) {
                     try {
                         service.toggleMain(true);
                     } catch (RemoteException e) {
                         Log.e(TAG, "RemoteException starting Sense service after login.", e);
                     }
                 }
 
                 // check if this is the very first login
                 final SharedPreferences appPrefs = PreferenceManager
                         .getDefaultSharedPreferences(SenseSettings.this);
                 if (appPrefs.getBoolean(PREF_FIRST_LOGIN, true)) {
                     final Editor editor = appPrefs.edit();
                     editor.putBoolean(PREF_FIRST_LOGIN, false);
                     editor.commit();
 
                     // toggle phone state sensors
                     if (service != null) {
                         try {
                             service.togglePhoneState(true);
                         } catch (RemoteException e) {
                             Log.e(TAG, "RemoteException starting phone state sensor after login.",
                                     e);
                         }
                     }
                 } else {
                     // the user logged in at least once
                 }
 
                 // update login preference summary
                showSummaries();
             }
         }
 
         @Override
         protected void onPreExecute() {
 
             // close the login dialog before showing the progress dialog
             try {
                 dismissDialog(DIALOG_LOGIN);
             } catch (final IllegalArgumentException e) {
                 // do nothing
             }
 
             showDialog(DIALOG_PROGRESS);
         }
     }
 
     /**
      * AsyncTask to register a new phone/user with CommonSense. Takes the username and unhashed
      * password as arguments. Clears any open registration dialogs before start, and displays a
      * progress dialog during operation. If the registration fails, the registration dialog is shown
      * again.
      */
     private class CheckRegisterTask extends AsyncTask<String, Void, Integer> {
         private static final String TAG = "CheckRegisterTask";
 
         @Override
         protected Integer doInBackground(String... params) {
             int result = -1;
 
             String username = null;
             String password = null;
             String name = null;
             String surname = null;
             String email = null;
             String phone = null;
             if (params.length == 2) {
                 username = params[0];
                 password = params[1];
             } else if (params.length == 6) {
                 username = params[0];
                 password = params[1];
                 name = params[2];
                 surname = params[3];
                 email = params[4];
                 phone = params[5];
             } else {
                 Log.w(TAG, "Unexpected amount of parameters!");
                 return -1;
             }
 
             if (service != null) {
                 try {
                     result = service.register(username, password, name, surname, email, phone);
 
                     // start service
                     if (0 == result) {
                         startSenseService();
                     }
                 } catch (final RemoteException e) {
                     Log.e(TAG, "RemoteException starting sensing after login.", e);
                 }
             } else {
                 Log.w(TAG, "Skipping registration task: service=null. Is the service bound?");
             }
             return result;
         }
 
         @Override
         protected void onPostExecute(Integer result) {
             try {
                 dismissDialog(DIALOG_PROGRESS);
             } catch (final IllegalArgumentException e) {
                 // do nothing
             }
 
             if (result.intValue() == -2) {
                 Toast.makeText(SenseSettings.this, R.string.toast_reg_conflict, Toast.LENGTH_LONG)
                         .show();
                 showDialog(DIALOG_REGISTER);
             } else if (result.intValue() == -1) {
                 Toast.makeText(SenseSettings.this, R.string.toast_reg_fail, Toast.LENGTH_LONG)
                         .show();
                 showDialog(DIALOG_REGISTER);
             } else {
                 Toast.makeText(SenseSettings.this, getString(R.string.toast_reg_ok),
                         Toast.LENGTH_LONG).show();
 
                 // toggle main service state
                 if (service != null) {
                     try {
                         service.toggleMain(true);
                     } catch (RemoteException e) {
                         Log.e(TAG, "RemoteException starting Sense service after registration.", e);
                     }
                 }
 
                 // check if this is the very first login
                 final SharedPreferences appPrefs = PreferenceManager
                         .getDefaultSharedPreferences(SenseSettings.this);
                 if (appPrefs.getBoolean(PREF_FIRST_LOGIN, true)) {
                     final Editor editor = appPrefs.edit();
                     editor.putBoolean(PREF_FIRST_LOGIN, false);
                     editor.commit();
 
                     // toggle phone state sensors
                     if (service != null) {
                         try {
                             service.togglePhoneState(true);
                         } catch (RemoteException e) {
                             Log.e(TAG,
                                     "RemoteException starting phone state sensor after registration.",
                                     e);
                         }
                     }
                 } else {
                     // the user logged in at least once
                 }
 
                 // update login preference summary
                showSummaries();
             }
         }
 
         @Override
         protected void onPreExecute() {
 
             // close the login dialog before showing the progress dialog
             try {
                 dismissDialog(DIALOG_REGISTER);
             } catch (final IllegalArgumentException e) {
                 // do nothing
             }
             showDialog(DIALOG_PROGRESS);
         }
     }
 
     /**
      * Listener for changes in the preferences. Any changes are immediately sent to the Sense
      * Platform service.
      */
     private class PrefSyncListener implements OnSharedPreferenceChangeListener {
 
         @Override
         public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
 
             // Log.v(TAG, "Preference " + key + " changed...");
 
             if (service == null) {
                 Log.e(TAG, "Could not send preference to Sense Platform service: service = null!");
                 return;
             }
 
             try {
                 // send the new preference to the Sense Platform service
                 try {
                     String value = sharedPreferences.getString(key, "");
                     service.setPrefString(key, value);
                     showSummaries();
                     return;
                 } catch (ClassCastException e) {
                     // do nothing, try another preference type
                 }
                 try {
                     boolean value = sharedPreferences.getBoolean(key, false);
                     service.setPrefBool(key, value);
                     showSummaries();
                     return;
                 } catch (ClassCastException e) {
                     // do nothing, try another preference type
                 }
                 try {
                     float value = sharedPreferences.getFloat(key, 0f);
                     service.setPrefFloat(key, value);
                     showSummaries();
                     return;
                 } catch (ClassCastException e) {
                     // do nothing, try another preference type
                 }
                 try {
                     int value = sharedPreferences.getInt(key, 0);
                     service.setPrefInt(key, value);
                     showSummaries();
                     return;
                 } catch (ClassCastException e) {
                     // do nothing, try another preference type
                 }
                 try {
                     long value = sharedPreferences.getLong(key, 0l);
                     service.setPrefLong(key, value);
                     showSummaries();
                     return;
                 } catch (ClassCastException e) {
                     Log.e(TAG, "Can't read new preference setting!");
                 }
 
             } catch (RemoteException e) {
                 Log.e(TAG, "Failed to set preference " + key + " at Sense Platform service", e);
             }
         }
     };
 
     /**
      * Sense App specific preference to keep track of whether the user has logged in at least once.
      */
     public static final String PREF_FIRST_LOGIN = "first_login_complete";
 
     private static final String TAG = "Sense Settings";
     private static final int DIALOG_LOGIN = 1;
     private static final int DIALOG_PROGRESS = 2;
     private static final int DIALOG_REGISTER = 3;
     private static final int DIALOG_DEV_MODE = 4;
 
     private PrefSyncListener changeListener = new PrefSyncListener();
     private boolean isServiceBound;
     private ISenseService service;
     private final ServiceConnection serviceConn = new ServiceConnection() {
 
         @Override
         public void onServiceConnected(ComponentName className, IBinder binder) {
             // Log.v(TAG, "Bound to Sense Platform service...");
             service = ISenseService.Stub.asInterface(binder);
             isServiceBound = true;
             loadPreferences();
             showSummaries();
         }
 
         @Override
         public void onServiceDisconnected(ComponentName className) {
             /* this is not called when the service is stopped, only when it is suddenly killed! */
             // Log.v(TAG, "Sense Platform disconnected...");
             service = null;
             isServiceBound = false;
         }
     };
 
     /**
      * Binds to the Sense Service.
      * 
      * @param autoCreate
      *            <code>true</code> if the service should be created when it is not running yet
      */
     private void bindToSenseService() {
         // start the service if it was not running already
         if (!isServiceBound) {
             final Intent serviceIntent = new Intent(ISenseService.class.getName());
             isServiceBound = bindService(serviceIntent, serviceConn, BIND_AUTO_CREATE);
         }
     }
 
     private Dialog createDialogDevMode() {
         final AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setTitle(R.string.dialog_dev_mode_title);
         builder.setMessage(R.string.dialog_dev_mode_msg);
         builder.setPositiveButton(R.string.button_ok, new OnClickListener() {
 
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 // do nothinga
             }
         });
         return builder.create();
     }
 
     private Dialog createDialogLoginProgress() {
         final ProgressDialog dialog = new ProgressDialog(this);
         dialog.setTitle(R.string.dialog_progress_title);
         dialog.setMessage(getString(R.string.dialog_progress_login_msg));
         dialog.setCancelable(false);
         return dialog;
     }
 
     /**
      * Loads all preferences from the Sense Platform service, and puts them into this activity's
      * default preferences.
      */
     private void loadPreferences() {
 
         if (null == service) {
             Log.e(TAG, "Cannot load Sense Platform preferences! service=null");
             return;
         }
 
         final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
         prefs.unregisterOnSharedPreferenceChangeListener(changeListener);
         Editor editor = prefs.edit();
 
         try {
             // general preferences
             editor.putString(Constants.PREF_SAMPLE_RATE,
                     service.getPrefString(Constants.PREF_SAMPLE_RATE, "0"));
             editor.putString(Constants.PREF_SYNC_RATE,
                     service.getPrefString(Constants.PREF_SYNC_RATE, "0"));
             editor.putBoolean(Constants.PREF_AUTOSTART,
                     service.getPrefBool(Constants.PREF_AUTOSTART, false));
 
             // location preferences
             editor.putBoolean(Constants.PREF_LOCATION_GPS,
                     service.getPrefBool(Constants.PREF_LOCATION_GPS, true));
             editor.putBoolean(Constants.PREF_LOCATION_NETWORK,
                     service.getPrefBool(Constants.PREF_LOCATION_NETWORK, true));
 
             // ambience preferences
             editor.putBoolean(Constants.PREF_AMBIENCE_LIGHT,
                     service.getPrefBool(Constants.PREF_AMBIENCE_LIGHT, true));
             editor.putBoolean(Constants.PREF_AMBIENCE_MIC,
                     service.getPrefBool(Constants.PREF_AMBIENCE_MIC, true));
 
             // motion preferences
             editor.putBoolean(Constants.PREF_MOTION_FALL_DETECT,
                     service.getPrefBool(Constants.PREF_MOTION_FALL_DETECT, false));
             editor.putBoolean(Constants.PREF_MOTION_FALL_DETECT_DEMO,
                     service.getPrefBool(Constants.PREF_MOTION_FALL_DETECT_DEMO, false));
             editor.putBoolean(Constants.PREF_MOTION_UNREG,
                     service.getPrefBool(Constants.PREF_MOTION_UNREG, true));
             editor.putBoolean(Constants.PREF_SCREENOFF_FIX,
                     service.getPrefBool(Constants.PREF_SCREENOFF_FIX, false));
 
             // neighboring devices
             editor.putBoolean(Constants.PREF_PROXIMITY_BT,
                     service.getPrefBool(Constants.PREF_PROXIMITY_BT, true));
             editor.putBoolean(Constants.PREF_PROXIMITY_WIFI,
                     service.getPrefBool(Constants.PREF_PROXIMITY_WIFI, true));
 
             // pop quiz preferences
             editor.putString(Constants.PREF_QUIZ_RATE,
                     service.getPrefString(Constants.PREF_QUIZ_RATE, "0"));
             editor.putBoolean(Constants.PREF_QUIZ_SILENT_MODE,
                     service.getPrefBool(Constants.PREF_QUIZ_SILENT_MODE, false));
 
             // Zephir BioHarness preferences
             editor.putBoolean(Constants.PREF_BIOHARNESS,
                     service.getPrefBool(Constants.PREF_BIOHARNESS, false));
             editor.putBoolean(Constants.PREF_BIOHARNESS_ACC,
                     service.getPrefBool(Constants.PREF_BIOHARNESS_ACC, true));
             editor.putBoolean(Constants.PREF_BIOHARNESS_BATTERY,
                     service.getPrefBool(Constants.PREF_BIOHARNESS_BATTERY, true));
             editor.putBoolean(Constants.PREF_BIOHARNESS_HEART_RATE,
                     service.getPrefBool(Constants.PREF_BIOHARNESS_HEART_RATE, true));
             editor.putBoolean(Constants.PREF_BIOHARNESS_RESP,
                     service.getPrefBool(Constants.PREF_BIOHARNESS_RESP, true));
             editor.putBoolean(Constants.PREF_BIOHARNESS_TEMP,
                     service.getPrefBool(Constants.PREF_BIOHARNESS_TEMP, true));
             editor.putBoolean(Constants.PREF_BIOHARNESS_WORN_STATUS,
                     service.getPrefBool(Constants.PREF_BIOHARNESS_WORN_STATUS, true));
 
             // Zephir HxM preferences
             editor.putBoolean(Constants.PREF_HXM, service.getPrefBool(Constants.PREF_HXM, false));
             editor.putBoolean(Constants.PREF_HXM_BATTERY,
                     service.getPrefBool(Constants.PREF_HXM_BATTERY, true));
             editor.putBoolean(Constants.PREF_HXM_DISTANCE,
                     service.getPrefBool(Constants.PREF_HXM_DISTANCE, true));
             editor.putBoolean(Constants.PREF_HXM_HEART_RATE,
                     service.getPrefBool(Constants.PREF_HXM_HEART_RATE, true));
             editor.putBoolean(Constants.PREF_HXM_SPEED,
                     service.getPrefBool(Constants.PREF_HXM_SPEED, true));
 
             // MyGlucohealth
             editor.putBoolean(Constants.PREF_GLUCO,
                     service.getPrefBool(Constants.PREF_GLUCO, false));
 
             // Tanita scale
             editor.putBoolean(Constants.PREF_TANITA_SCALE,
                     service.getPrefBool(Constants.PREF_TANITA_SCALE, false));
 
             // advance settings
             editor.putBoolean(Constants.PREF_DEV_MODE,
                     service.getPrefBool(Constants.PREF_DEV_MODE, false));
             editor.putBoolean(Constants.PREF_COMPRESSION,
                     service.getPrefBool(Constants.PREF_COMPRESSION, true));
             editor.putBoolean(Constants.PREF_WAKELOCK,
                     service.getPrefBool(Constants.PREF_WAKELOCK, false));
 
             editor.commit();
 
         } catch (RemoteException e) {
             Log.e(TAG, "Exception getting preferences from Sense Platform service!", e);
         }
 
         prefs.registerOnSharedPreferenceChangeListener(changeListener);
 
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         addPreferencesFromResource(R.xml.preferences);
 
         // setup some preferences with custom dialogs
         setupLoginPref();
         setupRegisterPref();
         setupQuizPref();
 
         final Preference devMode = findPreference(Constants.PREF_DEV_MODE);
         devMode.setOnPreferenceClickListener(new OnPreferenceClickListener() {
 
             @Override
             public boolean onPreferenceClick(Preference preference) {
                 SharedPreferences prefs = PreferenceManager
                         .getDefaultSharedPreferences(SenseSettings.this);
                 if (prefs.getBoolean(Constants.PREF_DEV_MODE, false)) {
                     showDialog(DIALOG_DEV_MODE);
                     return true;
                 } else {
                     return false;
                 }
             }
         });
     }
 
     @Override
     protected Dialog onCreateDialog(int id) {
         Dialog dialog = null;
         switch (id) {
         case DIALOG_LOGIN:
             dialog = new LoginDialog(this);
             break;
         case DIALOG_REGISTER:
             dialog = new RegisterDialog(this);
             break;
         case DIALOG_PROGRESS:
             dialog = createDialogLoginProgress();
             break;
         case DIALOG_DEV_MODE:
             dialog = createDialogDevMode();
             break;
         default:
             dialog = super.onCreateDialog(id);
             break;
         }
         return dialog;
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
 
         SharedPreferences appPrefs = PreferenceManager.getDefaultSharedPreferences(this);
         appPrefs.unregisterOnSharedPreferenceChangeListener(changeListener);
 
         unbindFromSenseService();
     }
 
     @Override
     protected void onPrepareDialog(int id, Dialog dialog) {
         // make sure the service is started when we try to register or log in
         switch (id) {
         case DIALOG_LOGIN:
             bindToSenseService();
 
             ((LoginDialog) dialog).setOnSubmitTask(new CheckLoginTask());
 
             // get username preset from Sense service
             if (null != service) {
                 try {
                     String usernamePreset = service
                             .getPrefString(Constants.PREF_LOGIN_USERNAME, "");
                     ((LoginDialog) dialog).setUsername(usernamePreset);
                 } catch (RemoteException e) {
                     Log.e(TAG, "Failed to get username from Sense Platform service", e);
                 }
             }
             break;
         case DIALOG_REGISTER:
             bindToSenseService();
             ((RegisterDialog) dialog).setOnSubmitTask(new CheckRegisterTask());
             break;
         default:
             break;
         }
     }
 
     @Override
     protected void onResume() {
         bindToSenseService();
         super.onResume();
     }
 
     /**
      * Sets up the Login preference to display a login dialog.
      */
     private void setupLoginPref() {
         final Preference loginPref = findPreference("login_placeholder");
         loginPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
 
             @Override
             public boolean onPreferenceClick(Preference preference) {
                 showDialog(DIALOG_LOGIN);
                 return true;
             }
         });
     }
 
     private void setupQuizPref() {
         final Preference popQuizRefresh = findPreference(Constants.PREF_QUIZ_SYNC);
         popQuizRefresh.setOnPreferenceClickListener(new OnPreferenceClickListener() {
 
             @Override
             public boolean onPreferenceClick(Preference preference) {
 
                 Log.w(TAG, "Questionnaire not restarted: code is disabled!");
                 // // start quiz sync broadcast
                 // final Intent refreshIntent = new Intent(
                 // "nl.sense_os.service.AlarmPopQuestionUpdate");
                 // final PendingIntent refreshPI =
                 // PendingIntent.getBroadcast(SenseSettings.this, 0,
                 // refreshIntent, 0);
                 // final AlarmManager mgr = (AlarmManager)
                 // getSystemService(ALARM_SERVICE);
                 // // mgr.set(AlarmManager.RTC_WAKEUP, 0, refreshPI);
                 //
                 // // show confirmation Toast
                 // Toast.makeText(SenseSettings.this,
                 // R.string.toast_quiz_refresh,
                 // Toast.LENGTH_LONG)
                 // .show();
                 //
                 return true;
             }
         });
     }
 
     /**
      * Sets up the Register preference to display a registration dialog.
      */
     private void setupRegisterPref() {
         final Preference registerPref = findPreference("register_placeholder");
         registerPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
 
             @Override
             public boolean onPreferenceClick(Preference preference) {
                 showDialog(DIALOG_REGISTER);
                 return true;
             }
         });
     }
 
     /**
      * Shows the summaries of the two sync/sense rate list preferences.
      */
     private void showSummaries() {
         // Log.v(TAG, "Show summaries...");
 
         // get username from Sense Platform service
         Preference loginPref = findPreference("login_placeholder");
         String username = "";
         try {
             username = service.getPrefString(Constants.PREF_LOGIN_USERNAME, "");
         } catch (RemoteException e) {
             Log.e(TAG, "Failed to get username from Sense Platform service", e);
         }
         final String summary = username.length() > 0 ? username : "Enter your login details";
         loginPref.setSummary(summary);
 
         // get sample rate preference setting
         final Preference samplePref = findPreference(Constants.PREF_SAMPLE_RATE);
         String sampleRate = "0";
         try {
             sampleRate = service.getPrefString(Constants.PREF_SAMPLE_RATE, "0");
         } catch (RemoteException e) {
             Log.e(TAG, "Failed to get username from Sense Platform service", e);
         }
         switch (Integer.parseInt(sampleRate)) {
         case -2: // real time
             samplePref.setSummary("Real-time: sample as quickly a possible");
             break;
         case -1: // often
             samplePref.setSummary("Often: sample every 10-20 seconds");
             break;
         case 0: // normal
             samplePref.setSummary("Normal: sample every 1-5 minutes");
             break;
         case 1: // rarely
             samplePref.setSummary("Rarely: sample every 15 minutes");
             break;
         default:
             samplePref.setSummary("ERROR");
         }
 
         // get sync rate preference setting
         final Preference syncPref = findPreference(Constants.PREF_SYNC_RATE);
         String syncRate = "0";
         try {
             syncRate = service.getPrefString(Constants.PREF_SYNC_RATE, "0");
         } catch (RemoteException e) {
             Log.e(TAG, "Failed to get username from Sense Platform service", e);
         }
         switch (Integer.parseInt(syncRate)) {
         case -2: // real time
             syncPref.setSummary("Real-time connection with CommonSense");
             break;
         case -1: // often
             syncPref.setSummary("Often: buffer size is 1 minute");
             break;
         case 0: // normal
             syncPref.setSummary("Normal: buffer size is 5 minutes");
             break;
         case 1: // rarely
             syncPref.setSummary("Eco-mode: buffer size is 15 minutes");
             break;
         default:
             syncPref.setSummary("ERROR");
         }
     }
 
     private void startSenseService() {
 
         final Intent serviceIntent = new Intent(ISenseService.class.getName());
         ComponentName name = startService(serviceIntent);
         if (null == name) {
             Log.w(TAG, "Failed to start Sense service");
         } else {
             // Log.v(TAG, "Started Sense service");
         }
     }
 
     /**
      * Unbinds from the Sense service, resets {@link #service} and {@link #isServiceBound}.
      */
     private void unbindFromSenseService() {
 
         if (true == isServiceBound && null != serviceConn) {
             unbindService(serviceConn);
         }
         service = null;
         isServiceBound = false;
     }
 }
