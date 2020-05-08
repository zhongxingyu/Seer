 /*******************************************************************************
  * Copyright 2012 Cian Mc Govern
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package com.cianmcgovern.android.ShopAndShare;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Locale;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpVersion;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.ContentBody;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.CoreProtocolPNames;
 import org.apache.http.util.EntityUtils;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Address;
 import android.location.Geocoder;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 
 /**
  * Activity that allows the user to upload the results to the server specified
  * in Constants
  * 
  * The user can obtain their location by clicking on the search icon beside the
  * text field The upload will start a new activity if it doesn't get an empty
  * response from the server
  * 
  * @author Cian Mc Govern <cian@cianmcgovern.com>
  * 
  */
 public class Share extends Activity {
 
     private EditText mLocationEdit, mStore;
     private Context mContext;
     private ProgressDialog mDialog;
     private String message;
     private LocationManager mLocationManager;
     private final Location mLocation = new Location(
             LocationManager.GPS_PROVIDER);
     private AlertDialog mGpsDialog;
     private Button mSearch;
     private LocationListener mLocationListener = null;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
 
         mContext = this;
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.share);
 
         Button upload = (Button) findViewById(R.id.uploadButton);
         upload.setText(R.string.upload);
         upload.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 String userLoc = mLocationEdit.getText().toString();
                 String userStore = mStore.getText().toString();
                 // Don't allow empty text fields
                 if (userLoc.length() > 1 && userStore.length() > 1) {
                     runUpload(Results.getInstance(), userLoc, userStore);
                 }
                 else
                     new AlertDialog.Builder(mContext)
                             .setTitle(R.string.invalidInput)
                             .setMessage(R.string.invalidFields).show();
             }
 
         });
 
         Button cancel = (Button) findViewById(R.id.cancelShareButton);
         cancel.setText(R.string.cancelButton);
         cancel.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 if (mLocationListener != null)
                     mLocationManager.removeUpdates(mLocationListener);
                 finish();
             }
 
         });
 
         mLocationEdit = (EditText) findViewById(R.id.enterLocation);
         mLocationEdit.setOnKeyListener(new OnKeyListener() {
 
             @Override
             public boolean onKey(View v, int keyCode, KeyEvent event) {
                 if ((event.getAction()) == KeyEvent.ACTION_DOWN
                         && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                     InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                     in.hideSoftInputFromWindow(
                             mLocationEdit.getApplicationWindowToken(),
                             InputMethodManager.HIDE_NOT_ALWAYS);
                     return true;
                 }
                 return false;
             }
 
         });
         mStore = (EditText) findViewById(R.id.enterStore);
         mStore.setOnKeyListener(new OnKeyListener() {
 
             @Override
             public boolean onKey(View v, int keyCode, KeyEvent event) {
                 if ((event.getAction()) == KeyEvent.ACTION_DOWN
                         && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                     InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                     in.hideSoftInputFromWindow(
                             mStore.getApplicationWindowToken(),
                             InputMethodManager.HIDE_NOT_ALWAYS);
                     return true;
                 }
                 return false;
             }
 
         });
 
         mSearch = (Button) findViewById(R.id.locationButton);
         mSearch.setBackgroundResource(R.drawable.search);
         mSearch.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 mLocationManager = (LocationManager) mContext
                         .getSystemService(Context.LOCATION_SERVICE);
                 if (!CheckFeatures.haveGPS()) {
                     new AlertDialog.Builder(mContext)
                             .setTitle("GPS Required")
                             .setMessage("You must have GPS to use this feature")
                             .show();
                 }
                 // Only use GPS if it is enabled
                 else if (mLocationManager
                         .isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                     mSearch.setEnabled(false);
                     mSearch.setBackgroundResource(R.drawable.world);
                     getLocation();
                 }
                 else {
                     mGpsDialog = new AlertDialog.Builder(mContext).create();
                     mGpsDialog.setTitle(R.string.gpsDisabled);
                     mGpsDialog.setMessage(mContext
                             .getText(R.string.gpsDisabledMessage));
                     mGpsDialog.show();
                 }
             }
 
         });
     }
 
     /**
      * 
      * Gets the address using the latitude and longitude from the getLocation()
      * 
      */
     public void getAddress() {
 
         Geocoder geo = new Geocoder(this, Locale.ENGLISH);
         Address address = null;
         try {
             address = geo.getFromLocation(mLocation.getLatitude(),
                     mLocation.getLongitude(), 1).get(0);

            StringBuilder fullAddress = new StringBuilder();
            for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                fullAddress.append(address.getAddressLine(i) + " ");
            }
            Log.v(Constants.LOG_TAG, "Got address: " + fullAddress.toString());

            mLocationEdit.setText(fullAddress.toString().trim());
         } catch (IOException e) {
             e.printStackTrace();
         }
         mSearch.setBackgroundResource(R.drawable.search);
     }
 
     /**
      * Calls upload() in a separate thread and displays a progress dialog while
      * it's executing
      * 
      * @param instance
      *            The results instance to use
      * @param location
      *            The location as specified by the user
      * @param store
      *            The store as specified by the user
      * 
      */
     private void runUpload(final Results instance, final String location,
             final String store) {
 
         mDialog = ProgressDialog.show(this, "", "Uploading. Please wait...");
 
         new Thread(new Runnable() {
 
             @Override
             public void run() {
                 try {
                     message = upload(instance, location, store);
                     mDialog.dismiss();
                     // If we get a response that contains data then something
                     // went wrong so we prompt the user to try again
                     // An empty response signals a successful upload
                     if (message.length() > 1) {
                         Intent in = new Intent(ShopAndShare.sContext,
                                 FailedUpload.class);
                         startActivity(in);
                     }
                     else {
                         if (mLocationListener != null)
                             mLocationManager.removeUpdates(mLocationListener);
                         finish();
                     }
                 } catch (ClientProtocolException e) {
                     Log.e(Constants.LOG_TAG,
                             "Encountered exception when uploading results");
                     e.printStackTrace();
                 } catch (IOException e) {
                     Log.e(Constants.LOG_TAG,
                             "Encountered exception when uploading results");
                     e.printStackTrace();
                 }
             }
 
         }).start();
 
     }
 
     /**
      * Uploads the text file specified by filename
      * 
      * @param instance
      *            The results instance to use
      * @param location
      *            The location as specified by the user
      * @param store
      *            The store as specified by the user
      * @return Response message from server
      * @throws ClientProtocolException
      * @throws IOException
      */
     private String upload(Results instance, String location, String store)
             throws ClientProtocolException, IOException {
 
         Log.v(Constants.LOG_TAG, "Inside upload");
         HttpClient httpClient = new DefaultHttpClient();
         httpClient.getParams().setParameter(
                 CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
 
         String filename = Results.getInstance().toFile();
         HttpPost httpPost = new HttpPost(Constants.URL);
         File file = new File(filename);
 
         MultipartEntity entity = new MultipartEntity();
         ContentBody cb = new FileBody(file, "plain/text");
         entity.addPart("inputfile", cb);
 
         ContentBody cbLocation = new StringBody(location);
         entity.addPart("location", cbLocation);
 
         ContentBody cbStore = new StringBody(store);
         entity.addPart("store", cbStore);
         httpPost.setEntity(entity);
         Log.v(Constants.LOG_TAG, "Sending post");
         HttpResponse response = httpClient.execute(httpPost);
         HttpEntity resEntity = response.getEntity();
 
         String message = EntityUtils.toString(resEntity);
         Log.v(Constants.LOG_TAG, "Response from upload is: " + message);
         resEntity.consumeContent();
 
         httpClient.getConnectionManager().shutdown();
 
         file.delete();
 
         return message;
     }
 
     /**
      * 
      * Gets the location using the LocationListener and sets the class wide
      * location variable with the latitude and longitude Only gets the location
      * once and removes the listener after that result is obtained
      * 
      */
     public void getLocation() {
 
         LocationListener locationListener = new LocationListener() {
 
             @Override
             public void onLocationChanged(Location loc) {
                 Log.v(Constants.LOG_TAG, "Got longitude: " + loc.getLongitude());
                 Log.v(Constants.LOG_TAG, "Got latitude: " + loc.getLatitude());
                 mLocation.setLongitude(loc.getLongitude());
                 mLocation.setLatitude(loc.getLatitude());
                 getAddress();
                 mLocationManager.removeUpdates(this);
                 mSearch.setEnabled(true);
             }
 
             @Override
             public void onProviderDisabled(String provider) {
             }
 
             @Override
             public void onProviderEnabled(String provider) {
             }
 
             @Override
             public void onStatusChanged(String provider, int status,
                     Bundle extras) {
             }
 
         };
 
         mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                 0, 0, locationListener);
     }
 
     @Override
     public void onBackPressed() {
         if (mLocationListener != null)
             mLocationManager.removeUpdates(mLocationListener);
         finish();
     }
 
     // Create options menu
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inf = getMenuInflater();
         inf.inflate(R.layout.default_menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.exit:
             if (mLocationListener != null)
                 mLocationManager.removeUpdates(mLocationListener);
             finish();
             return true;
         case R.id.help:
             new HelpDialog(this, this.getText(R.string.shareHelpMessage)
                     .toString()).show();
             return true;
         default:
             return super.onOptionsItemSelected(item);
         }
     }
 }
