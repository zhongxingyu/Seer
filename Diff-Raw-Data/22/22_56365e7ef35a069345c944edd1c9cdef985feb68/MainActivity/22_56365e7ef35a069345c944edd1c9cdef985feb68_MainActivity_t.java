 package com.snot.whereareyou;
 
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.provider.ContactsContract.PhoneLookup;
 import android.util.Log;
 
 import android.view.WindowManager.LayoutParams;
 import android.graphics.PixelFormat;
 
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesUtil;
 import com.google.android.gms.gcm.GoogleCloudMessaging;
 
 import java.io.IOException;
 
 /**
  * GCM - http://developer.android.com/google/gcm/client.html
  *       https://code.google.com/p/gcm/
  *
  * TODO
  *  - Remove hardcoded strings
  *  - move api key in to main activity as static property
  */
 
 
 public class MainActivity extends TabsFragmentActivity {
 
 	public static final String PROPERTY_REG_ID = "registration_id";
 	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
 	private static final String PROPERTY_APP_VERSION = "appVersion";
 
 	String SENDER_ID;
 	String regid;
 
 	GoogleCloudMessaging gcm;
 	Context context;
 	// Log tag
	static final String TAG = "MainActivity";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		context = getApplicationContext();
 		String SENDER_ID = context.getString(R.string.sender_id);
 
 		if (checkPlayServices()) {
 			gcm = GoogleCloudMessaging.getInstance(this);
 			regid = getRegistrationId(context);
 			if (regid.isEmpty()) {
				registerInBackground(SENDER_ID);
 			}
 		}
 		else
 		{
 			Log.i(TAG, "No valid Google Play Services APK found.");
 		}
 
 		this.addTab("main", getString(R.string.tab_pick_contact_title), PickContactFragment.class);
 		this.addTab("hist", getString(R.string.tab_history_title), HistoryListFragment.class);
 
 		restoreFromSavedInstanceState(savedInstanceState);
 	}
 
 //    @Override
 //    public boolean onCreateOptionsMenu(Menu menu) {
 //        // Inflate the menu; this adds items to the action bar if it is present.
 //        getMenuInflater().inflate(R.menu.main, menu);
 //        return true;
 //    }
 
     @Override
     protected void onResume() {
         super.onResume();
         // Check device for Play Services APK.
         checkPlayServices();
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
     }
 
     /**
      * Check the device to make sure it has the Google Play Services APK. If
      * it doesn't, display a dialog that allows users to download the APK from
      * the Google Play Store or enable it in the device's system settings.
      */
     private boolean checkPlayServices() {
         int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
         if (resultCode != ConnectionResult.SUCCESS) {
             if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                 GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                         PLAY_SERVICES_RESOLUTION_REQUEST).show();
             } else {
                 Log.i(TAG, "This device is not supported.");
                 finish();
             }
             return false;
         }
         return true;
     }
 
     /**
      * Stores the registration ID and the app versionCode in the application's
      * {@code SharedPreferences}.
      *
      * @param context application's context.
      * @param regId registration ID
      */
     private void storeRegistrationId(Context context, String regId) {
         final SharedPreferences prefs = getGcmPreferences(context);
         int appVersion = getAppVersion(context);
         Log.i(TAG, "Saving regId on app version " + appVersion);
         SharedPreferences.Editor editor = prefs.edit();
         editor.putString(PROPERTY_REG_ID, regId);
         editor.putInt(PROPERTY_APP_VERSION, appVersion);
         editor.commit();
     }
 
     /**
      * Gets the current registration ID for application on GCM service, if there is one.
      * If result is empty, the app needs to register.
      *
      * @return registration ID, or empty string if there is no existing
      *         registration ID.
      */
     protected String getRegistrationId(Context context) {
         final SharedPreferences prefs = getGcmPreferences(context);
         String registrationId = prefs.getString(PROPERTY_REG_ID, "");
         if (registrationId.isEmpty()) {
             Log.i(TAG, "Registration not found.");
             return "";
         }
         // Check if app was updated; if so, it must clear the registration ID
         // since the existing regID is not guaranteed to work with the new
         // app version.
         int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
         int currentVersion = getAppVersion(context);
         if (registeredVersion != currentVersion) {
             Log.i(TAG, "App version changed.");
             return "";
         }
         return registrationId;
     }
 
     /**
      * Registers the application with GCM servers asynchronously.
      * <p>
      * Stores the registration ID and the app versionCode in the application's
      * shared preferences.
      */
    private void registerInBackground(final String sender_id) {
         new AsyncTask<Void, Void, String>() {
             @Override
             protected String doInBackground(Void... params) {
                 try {
                     if (gcm == null) {
                         gcm = GoogleCloudMessaging.getInstance(context);
                     }

                    regid = gcm.register(sender_id);
 
                     // You should send the registration ID to your server over HTTP, so it
                     // can use GCM/HTTP or CCS to send messages to your app.
                     //sendRegistrationIdToBackend();
 
                     // Persist the regID - no need to register again.
                     storeRegistrationId(context, regid);
                 } catch (IOException ex) {
 			ex.printStackTrace();
                 }
                 return regid;
             }
 
             @Override
             protected void onPostExecute(String regid) {
 	    // TODO: something should be done here... only after this point has been reached are we ready to receive GCM
             }
         }.execute(null, null, null);
     }
 
     /**
      * @return Application's {@code SharedPreferences}.
      */
     private SharedPreferences getGcmPreferences(Context context) {
         // This sample app persists the registration ID in shared preferences, but
         // how you store the regID in your app is up to you.
         return getSharedPreferences(MainActivity.class.getSimpleName(),
                 Context.MODE_PRIVATE);
     }
 
     /**
      * @return Application's version code from the {@code PackageManager}.
      */
     private static int getAppVersion(Context context) {
         try {
             PackageInfo packageInfo = context.getPackageManager()
                     .getPackageInfo(context.getPackageName(), 0);
             return packageInfo.versionCode;
         } catch (NameNotFoundException e) {
             // should never happen
             throw new RuntimeException("Could not get package name: " + e);
         }
     }
 
 /* Get name of contact by phone number
  * http://stackoverflow.com/questions/3079365/android-retrieve-contact-name-from-phone-number
  */
     public static String getContactName(Context context, String phoneNumber) {
         ContentResolver cr = context.getContentResolver();
         Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
         Cursor cursor = cr.query(uri, new String[]{PhoneLookup.DISPLAY_NAME}, null, null, null);
         if (cursor == null) {
             return null;
         }
         String contactName = null;
         if(cursor.moveToFirst()) {
             contactName = cursor.getString(cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME));
         }
         if(cursor != null && !cursor.isClosed()) {
             cursor.close();
         }
         return contactName;
     }
 
 }
 
