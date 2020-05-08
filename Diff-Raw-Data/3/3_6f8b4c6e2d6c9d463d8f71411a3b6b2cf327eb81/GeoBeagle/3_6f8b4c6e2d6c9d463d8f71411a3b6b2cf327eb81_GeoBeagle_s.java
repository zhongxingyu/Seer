 /*
  ** Licensed under the Apache License, Version 2.0 (the "License");
  ** you may not use this file except in compliance with the License.
  ** You may obtain a copy of the License at
  **
  **     http://www.apache.org/licenses/LICENSE-2.0
  **
  ** Unless required by applicable law or agreed to in writing, software
  ** distributed under the License is distributed on an "AS IS" BASIS,
  ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ** See the License for the specific language governing permissions and
  ** limitations under the License.
  */
 
 package com.google.code.geobeagle.activity.main;
 
 import com.google.code.geobeagle.Geocache;
 import com.google.code.geobeagle.LocationControlBuffered;
 import com.google.code.geobeagle.R;
 import com.google.code.geobeagle.R.id;
 import com.google.code.geobeagle.activity.main.GeoBeagleDelegate.LogFindClickListener;
 import com.google.code.geobeagle.activity.main.GeoBeagleModule.ButtonListenerCachePage;
 import com.google.code.geobeagle.activity.main.GeoBeagleModule.ButtonListenerMapPage;
 import com.google.code.geobeagle.activity.main.fieldnotes.DialogHelperSms;
 import com.google.code.geobeagle.activity.main.fieldnotes.FieldnoteLogger;
 import com.google.code.geobeagle.activity.main.fieldnotes.DialogHelperSms.DialogHelperSmsFactory;
 import com.google.code.geobeagle.activity.main.fieldnotes.FieldnoteLogger.FieldnoteLoggerFactory;
 import com.google.code.geobeagle.activity.main.fieldnotes.FieldnoteLogger.OnClickCancel;
 import com.google.code.geobeagle.activity.main.fieldnotes.FieldnoteLogger.OnClickOk;
 import com.google.code.geobeagle.activity.main.fieldnotes.FieldnoteLogger.OnClickOkFactory;
 import com.google.code.geobeagle.activity.main.view.OnClickListenerRadar;
 import com.google.code.geobeagle.activity.main.view.OnClickListenerCacheDetails;
 import com.google.code.geobeagle.activity.main.view.OnClickListenerIntentStarter;
 import com.google.inject.Inject;
 import com.google.inject.Injector;
 import com.google.inject.Key;
 
 import roboguice.activity.GuiceActivity;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Intent;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 
 import java.text.DateFormat;
 import java.util.Date;
 
 /*
  * Main Activity for GeoBeagle.
  */
 public class GeoBeagle extends GuiceActivity {
     private GeoBeagleDelegate mGeoBeagleDelegate;
     
     private static final DateFormat mLocalDateFormat = DateFormat
             .getTimeInstance(DateFormat.MEDIUM);
     
     @Inject
     LocationControlBuffered mLocationControlBuffered;
     
     public Geocache getGeocache() {
         return mGeoBeagleDelegate.getGeocache();
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         if (requestCode == GeoBeagleDelegate.ACTIVITY_REQUEST_TAKE_PICTURE) {
             Log.d("GeoBeagle", "camera intent has returned.");
         } else if (resultCode == 0)
             setIntent(data);
     }
 
     @Override
     public void onCreate(final Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         Log.d("GeoBeagle", "GeoBeagle onCreate");
 
         setContentView(R.layout.main);
         final Injector injector = getInjector();
 
         final RadarView radarView = injector.getInstance(RadarView.class);
 
         mLocationControlBuffered.onLocationChanged(null);
         
         final LocationManager mLocationManager = injector.getInstance(LocationManager.class);
 
         // Register for location updates
         mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, radarView);
         mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, radarView);
 
         mGeoBeagleDelegate = injector.getInstance(GeoBeagleDelegate.class);
 
         // see http://www.androidguys.com/2008/11/07/rotational-forces-part-two/
         if (getLastNonConfigurationInstance() != null) {
             setIntent((Intent)getLastNonConfigurationInstance());
         }
 
         findViewById(id.maps).setOnClickListener(
                 injector.getInstance(Key.get(OnClickListenerIntentStarter.class,
                         ButtonListenerMapPage.class)));
 
         findViewById(R.id.cache_details).setOnClickListener(
                 injector.getInstance(OnClickListenerCacheDetails.class));
         findViewById(id.cache_page).setOnClickListener(
                 injector.getInstance(Key.get(OnClickListenerIntentStarter.class,
                         ButtonListenerCachePage.class)));
 
         findViewById(id.radarview).setOnClickListener(
                 injector.getInstance(OnClickListenerRadar.class));
 
         findViewById(id.menu_log_find).setOnClickListener(
                 new LogFindClickListener(this, id.menu_log_find));
         findViewById(id.menu_log_dnf).setOnClickListener(
                 new LogFindClickListener(this, id.menu_log_dnf));
     }
 
     @Override
     protected Dialog onCreateDialog(int id) {
         super.onCreateDialog(id);
         final Injector injector = getInjector();
         
         final AlertDialog.Builder builder = injector.getInstance(AlertDialog.Builder.class);
         final View fieldnoteDialogView = LayoutInflater.from(this)
                 .inflate(R.layout.fieldnote, null);
 
         final boolean fDnf = id == R.id.menu_log_dnf;
 
         final OnClickOk onClickOk = injector.getInstance(OnClickOkFactory.class).create(
                 (EditText)fieldnoteDialogView.findViewById(R.id.fieldnote), fDnf);
         builder.setTitle(R.string.field_note_title);
         builder.setView(fieldnoteDialogView);
         builder.setNegativeButton(R.string.cancel, injector.getInstance(OnClickCancel.class));
         builder.setPositiveButton(R.string.log_cache, onClickOk);
         AlertDialog alertDialog = builder.create();
         return alertDialog;
     }
 
     @Override
     protected void onPrepareDialog(int id, Dialog dialog) {
         super.onCreateDialog(id);
         final Injector injector = getInjector();
 
         final boolean fDnf = id == R.id.menu_log_dnf;
 
         final DialogHelperSms dialogHelperSms = injector.getInstance(DialogHelperSmsFactory.class)
                 .create(mGeoBeagleDelegate.getGeocache().getId().length(), fDnf);
         final FieldnoteLogger fieldnoteLogger = injector.getInstance(FieldnoteLoggerFactory.class)
                 .create(dialogHelperSms);
         
         fieldnoteLogger.onPrepareDialog(dialog, mLocalDateFormat.format(new Date()), fDnf);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         return mGeoBeagleDelegate.onCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         if (mGeoBeagleDelegate.onKeyDown(keyCode, event))
             return true;
         return super.onKeyDown(keyCode, event);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         return mGeoBeagleDelegate.onOptionsItemSelected(item);
     }
 
     @Override
     public void onPause() {
        super.onPause();
         Log.d("GeoBeagle", "GeoBeagle onPause");
         mGeoBeagleDelegate.onPause();
     }
 
     /*
      * (non-Javadoc)
      * @see android.app.Activity#onRestoreInstanceState(android.os.Bundle)
      */
     @Override
     protected void onRestoreInstanceState(Bundle savedInstanceState) {
         super.onRestoreInstanceState(savedInstanceState);
         mGeoBeagleDelegate.onRestoreInstanceState(savedInstanceState);
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         Log.d("GeoBeagle", "GeoBeagle onResume");
         mGeoBeagleDelegate.onResume();
     }
 
     /*
      * (non-Javadoc)
      * @see android.app.Activity#onRetainNonConfigurationInstance()
      */
     @Override
     public Object onRetainNonConfigurationInstance() {
         return getIntent();
     }
 
     /*
      * (non-Javadoc)
      * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
      */
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         mGeoBeagleDelegate.onSaveInstanceState(outState);
     }
 }
