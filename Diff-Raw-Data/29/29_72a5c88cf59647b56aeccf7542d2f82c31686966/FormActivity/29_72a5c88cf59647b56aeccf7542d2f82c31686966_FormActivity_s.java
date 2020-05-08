 package com.herokuapp.maintainenator;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.herokuapp.maintainenator.utils.ExtAudioRecorder;
 
 import android.app.ActionBar;
 import android.app.ActionBar.Tab;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Fragment;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.media.MediaPlayer;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.provider.MediaStore;
 import android.util.Base64;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.ImageView.ScaleType;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class FormActivity extends Activity implements LocationListener {
 
     private static final String GOOGLE_PLACE_API = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?radius=100&types=establishment&sensor=true&key=AIzaSyCL6FCIh_lcj9RPY-TZ6O08acGrffJvbq8&location=";
 
     private static final String END = "\r\n";
     private static final String BOUNDARY = "1q2w3e4r5t";
     private static final String TWO_HYPHENS = "--";
 
     private static final String TAG = "FormActivity";
     private static final int CAMERA_REQUEST = 0;
     private static final int ALBUM_REQUEST = 1;
     private static final float MIN_ACCURACY = 100;
     private Activity currentActivity;
     private ActionBar actionBar;
     private IndoorFormFragment indoorFormFragment;
     private OutdoorFormFragment outdoorFormFragment;
     private AlertDialog photoAlertDialog;
 
     private Location latestLocation;
     private LocationManager locationManager;
     private SharedPreferences sharedPreferences;
     private boolean localDev;
     private String username;
     private String password;
 
     private String cachedBuilding;
     private String cachedLocation;
     private boolean networkDisconnectedToast = true;
     private boolean reverseGeoSucceed = false;
 
     //Audio
     private ExtAudioRecorder extAudioRecorder;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         currentActivity = this;
         actionBar = getActionBar();
         actionBar.setDisplayHomeAsUpEnabled(true);
         actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
         Tab tab = actionBar.newTab()
                        .setText(R.string.indoor)
                        .setTag("indoor")
                        .setTabListener(new FormTabListener<IndoorFormFragment>(this, "indoor", IndoorFormFragment.class));
         actionBar.addTab(tab);
 
         tab = actionBar.newTab()
                 .setText(R.string.outdoor)
                 .setTag("outdoor")
                 .setTabListener(new FormTabListener<OutdoorFormFragment>(this, "outdoor", OutdoorFormFragment.class));
         actionBar.addTab(tab);
 
         locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
         sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
 
         extAudioRecorder = new ExtAudioRecorder();
     }
 
     @Override
     public void onDestroy() {
         extAudioRecorder.release();
         super.onDestroy();
     }
 
     @Override
     public void onBackPressed() {
     }
 
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
     }
 
     @Override
     protected void onStart() {
         super.onStart();
         Log.d(TAG, "reverse succeed? " + reverseGeoSucceed);
         if (latestLocation != null) {
             Log.d(TAG, "live..." + latestLocation.toString());
         }
         Log.d(TAG, "onStart");
         if (!checkNetworkStatus()) {
             networkDisconnectedToast = false;
             Toast.makeText(this, "Network disconnected.", Toast.LENGTH_LONG).show();
         }
         localDev = sharedPreferences.getBoolean("dev_mode", true);
         Log.d(TAG, "local_dev?: " + localDev);
         username = sharedPreferences.getString("username", "");
         Log.d(TAG, "username: " + username);
         password = sharedPreferences.getString("password", "");
         /* This is ONLY related to the configurations of `Location access` settings. Regardless of whether you have network access or not.
           * NOTE that network localization will suppress GPS localization.
           * If WIFI/mobile data is turned on, it will use WIFI/mobile data instead of GPS?
           */
         if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
             Log.d(TAG, "Requesting single location update from network provider.");
             locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, null);
         }
         if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
             Log.d(TAG, "Requesting single location update from gps provider.");
             locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, null);
         }
     }
 
     @Override
     protected void onPause() {
         Log.d(TAG, "onPause");
         super.onPause();
     }
 
     @Override
     protected void onStop() {
         Log.d(TAG, "onStop");
         networkDisconnectedToast = true;
         //TODO need this?
         locationManager.removeUpdates(this);
         super.onStop();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.clip:
                 //TODO
                 return true;
             case android.R.id.home:
                 Intent intent = new Intent(this, MainActivity.class);
                 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                 startActivity(intent);
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         Log.d(getClass().getSimpleName(), "requstCode " + requestCode + ", resultCode " + resultCode);
         if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
             String photoURI = PhotoActionDialogFragment.getNewPhotoName();
             galleryAddPicture(photoURI);
             displayPhoto(photoURI);
             Log.d(getClass().getSimpleName(), "Camera: display the picture.");
         } else if (requestCode == ALBUM_REQUEST && resultCode == Activity.RESULT_OK) {
             Uri photoUri = data.getData();
             if (photoUri != null) {
                 Log.d(getClass().getSimpleName(), "Picture: " + photoUri);
                 String[] filePathColumn = { MediaStore.Images.Media.DATA };
                 Cursor cursor = getContentResolver().query(photoUri, filePathColumn, null, null, null);
                 cursor.moveToFirst();
                 int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                 String filePath = cursor.getString(columnIndex);
                 cursor.close();
                 displayPhoto(filePath);
                 Log.d(getClass().getSimpleName(), "Gallery: display the picture.");
             }
         } else {
             super.onActivityResult(requestCode, resultCode, data);
         }
     }
 
     ExtAudioRecorder getAudioRecorder() {
         return extAudioRecorder;
     }
 
     // Called by FormTabListener
     void setIndoorFormFragment(Fragment fragment) {
         indoorFormFragment = (IndoorFormFragment) fragment;
     }
 
     void setOutdoorFormFragment(Fragment fragment) {
         outdoorFormFragment = (OutdoorFormFragment) fragment;
     }
 
     private void galleryAddPicture(String photoURI) {
         Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
         File f = new File(photoURI);
         Uri contentUri = Uri.fromFile(f);
         mediaScanIntent.setData(contentUri);
         sendBroadcast(mediaScanIntent);
     }
 
     public void displayPhoto(String photoURI) {
         Log.d(TAG, "Display photo: " + photoURI);
         Tab tab = actionBar.getSelectedTab();
         Bitmap bmp = BitmapFactory.decodeFile(photoURI);
         Bitmap photo = Bitmap.createScaledBitmap(bmp, 200, 140, false);
         if (tab.getTag().equals("indoor")) {
             int longClickedId = IndoorFormFragment.getLongClickedId();
             ImageView imageView = (ImageView) findViewById(longClickedId);
             imageView.setImageBitmap(photo);
             imageView.setOnClickListener(new PhotoClickListener(photoURI));
             indoorFormFragment.setPhotoPath(longClickedId, photoURI);
             enableNextPhoto(true, longClickedId);
         } else {
             OutdoorFormFragment outdoorFragment = (OutdoorFormFragment) getFragmentManager().findFragmentByTag("outdoor");
             int longClickedId = outdoorFragment.getLongClickedId();
             ImageView imageView = (ImageView) findViewById(longClickedId);
             imageView.setImageBitmap(photo);
             imageView.setOnClickListener(new PhotoClickListener(photoURI));
             outdoorFormFragment.setPhotoPath(longClickedId, photoURI);
             enableNextPhoto(false, longClickedId);
         }
     }
 
     public void deletePhoto() {
         if (actionBar.getSelectedTab().getTag().equals("indoor")) {
             int longClickedId = IndoorFormFragment.getLongClickedId();
             ImageView imageView = (ImageView) findViewById(longClickedId);
             imageView.setOnClickListener(null);
             imageView.setImageBitmap(null);
             indoorFormFragment.setPhotoPath(longClickedId, null);
             imageView.setImageResource(R.drawable.content_new_picture);
         } else {
             int longClickedId = outdoorFormFragment.getLongClickedId();
             ImageView imageView = (ImageView) findViewById(longClickedId);
             imageView.setOnClickListener(null);
             imageView.setImageBitmap(null);
             outdoorFormFragment.setPhotoPath(longClickedId, null);
             imageView.setImageResource(R.drawable.content_new_picture);
         }
     }
 
     private void enableNextPhoto(boolean isIndoor, int currentId) {
         if (isIndoor) {
             if (currentId == R.id.indoor_image_view1) {
                 ImageView iv = (ImageView) findViewById(R.id.indoor_image_view2);
                 if (!iv.isLongClickable()) {
                     iv.setOnLongClickListener(indoorFormFragment);
                     iv.setBackgroundColor(0xFF00BAFF);
                     iv.setImageResource(R.drawable.content_new_picture);
                     iv.setScaleType(ScaleType.CENTER);
                 }
             } else if (currentId == R.id.indoor_image_view2) {
                 ImageView iv = (ImageView) findViewById(R.id.indoor_image_view3);
                 if (!iv.isLongClickable()) {
                     iv.setOnLongClickListener(indoorFormFragment);
                     iv.setBackgroundColor(0xFF00BAFF);
                     iv.setImageResource(R.drawable.content_new_picture);
                     iv.setScaleType(ScaleType.CENTER);
                 }
             }
         } else {
             if (currentId == R.id.outdoor_image_view1) {
                 ImageView iv = (ImageView) findViewById(R.id.outdoor_image_view2);
                 if (!iv.isLongClickable()) {
                     iv.setOnLongClickListener(outdoorFormFragment);
                     iv.setBackgroundColor(0xFF00BAFF);
                     iv.setImageResource(R.drawable.content_new_picture);
                     iv.setScaleType(ScaleType.CENTER);
                 }
             } else if (currentId == R.id.outdoor_image_view2) {
                 ImageView iv = (ImageView) findViewById(R.id.outdoor_image_view3);
                 if (!iv.isLongClickable()) {
                     iv.setOnLongClickListener(outdoorFormFragment);
                     iv.setBackgroundColor(0xFF00BAFF);
                     iv.setImageResource(R.drawable.content_new_picture);
                     iv.setScaleType(ScaleType.CENTER);
                 }
             }
         }
     }
 
     /**
      * Popout a dialog to display the photo in fullsize.
      */
     private class PhotoClickListener implements OnClickListener {
 
         private String photoURI;
 
         public PhotoClickListener(String photoURI) {
             this.photoURI = photoURI;
         }
 
         @Override
         public void onClick(View v) {
             FullPhotoDialogFragment dialog = new FullPhotoDialogFragment();
             dialog.setPhotoURI(photoURI);
             dialog.show(getFragmentManager(), "full_photo");
         }
     }
 
     Location getLatestLocation() {
         return latestLocation;
     }
 
     String getCachedLocation() {
         return cachedLocation;
     }
 
     @Override
     public void onLocationChanged(Location location) {
         Log.d(TAG, "Location changed to: " + location);
         if (latestLocation == null || !reverseGeoSucceed || location.getAccuracy() < latestLocation.getAccuracy()) {
             latestLocation = location;
             new ReverseGeoTask().execute(location);
         }
     }
 
     @Override
     public void onProviderDisabled(String provider) {
         Log.d(TAG, provider + ": DISABLED!");
     }
 
     @Override
     public void onProviderEnabled(String provider) {
         Log.d(TAG, provider + ": ENABLED!");
     }
 
     @Override
     public void onStatusChanged(String provider, int status, Bundle extra) {
         Log.d(TAG, provider + ": " + status);
     }
 
     /**
      *  Reverse the geo data into plain location.
      *  Return array of length 2, first one is location for indoor, second is for outdoor.
      */
     class ReverseGeoTask extends AsyncTask<Location, Integer, String[]> {
 
         private String findBuilding(Location location) {
             //TODO use geo data
             return "Davis Hall";
         }
 
         private String getJSONResponse(InputStream in) {
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
             StringBuilder sb = new StringBuilder();
             String line = null;
             try {
                 while ((line = bufferedReader.readLine()) != null) {
                     sb.append(line + "\n");
                 }
                 bufferedReader.close();
             } catch (IOException ioe) {
                 ioe.printStackTrace();
                 return null;
             }
             return sb.toString();
         }
 
         @Override
         protected void onPostExecute(String[] result) {
             if (!result[0].isEmpty()) {
                 cachedBuilding = result[0];
             }
             if (!result[1].isEmpty()) {
                 cachedLocation = result[1];
             } else {
                 if (networkDisconnectedToast) {
                     networkDisconnectedToast = false;
                     Toast.makeText(currentActivity, "Network disconnected.", Toast.LENGTH_LONG).show();
                 }
             }
             Tab tab = actionBar.getSelectedTab();
             if (tab.getTag().equals("indoor")) {
                 int num = indoorFormFragment.getBuildingSelectedTime();
                 Log.d(TAG, "" + num);
                 if (num < 2) {
                     Spinner buildingSpinner = (Spinner) findViewById(R.id.building_spinner);
                     String[] buildingArray = getResources().getStringArray(R.array.buildings);
                     for (int index=0; index<buildingArray.length; index++) {
                         if (buildingArray[index].equals(result[0])) {
                             buildingSpinner.setSelection(index);
                             break;
                         }
                     }
                 }
             } else {
                 TextView addressView = (TextView) findViewById(R.id.outdoor_address);
                 if (!addressView.isFocused() && !result[1].isEmpty()) {
                     addressView.setText(result[1]);
                 }
             }
         }
 
         @Override
         protected String[] doInBackground(Location... params) {
             if (params.length > 0) {
                 Location location = params[0];
                 // Locate which building might be
                 String building = findBuilding(location);
                 HttpURLConnection urlConnection = null;
                 URL url = null;
                 try {
                     //url = new URL(getString(R.string.google_map_url) + location.getLatitude() + "," + location.getLongitude() + "&sensor=true");
                     url = new URL(GOOGLE_PLACE_API + location.getLatitude() + "," + location.getLongitude());
                     Log.d(TAG, "Requesting Google Place API: " + url.toString());
                 } catch (MalformedURLException mue) {
                     Log.e(TAG, "mue: " + mue.getMessage());
                     return (new String[] {building, ""});
                 }
                 try {
                     urlConnection = (HttpURLConnection) url.openConnection();
                     Log.d(TAG, "" + urlConnection);
                     if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                         InputStream in = urlConnection.getInputStream();
                         if (!url.getHost().equals(urlConnection.getURL().getHost())) {
                             Log.w(TAG, "Redirecting, connected to UB Wireless?");
                             return (new String[] {building, ""});
                         }
                         String jsonResponseString = getJSONResponse(in);
                         Log.d(TAG, jsonResponseString);
                         JSONObject jsonResponse = new JSONObject(jsonResponseString);
                         JSONArray allResults = jsonResponse.getJSONArray("results");
                         String address = "";
                         if (allResults.length() > 0) {
                             JSONObject jsonObject = allResults.getJSONObject(0);
                             //address = jsonObject.getString("name") + ", " + jsonObject.getString("vicinity");
                             address = jsonObject.getString("vicinity");
                             Log.d(TAG, address);
                             reverseGeoSucceed = true;
                         }
                         return (new String[] {building, address});
                         //String address = jsonResponse.getJSONArray(getString(R.string.json_array_tag)).getJSONObject(0).getString(getString(R.string.json_address_tag));
                         //Log.d(TAG, "Address: " + address);
                         //reverseGeoSucceed = true;
                         //return (new String[] {building, address});
                     }
                     Log.d(TAG, "Unexpected response: " + urlConnection.getResponseMessage());
                     return new String[] {building, ""};
                 } catch (IOException ioe) {
                     ioe.printStackTrace();
                     Log.e(TAG, "ioe: " + ioe.getMessage());
                     return new String[] {building, ""};
                 } catch (JSONException je) {
                     je.printStackTrace();
                     Log.e(TAG, "je: " + je.getMessage());
                     return new String[] {building, ""};
                 } finally {
                     urlConnection.disconnect();
                 }
             }
             return null;
         }
     }
 
     /**
      * Basic network connection check. It will also return TRUE if the network or wifi is open, but you are overdraft or doesn't login to wifi.
      * Android default to use WIFI as data network is WIFI is activated. o/w use network.
      */
     private boolean checkNetworkStatus() {
         ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
         if (networkInfo != null) {
             Log.d(TAG, networkInfo.getTypeName() + ": " + networkInfo.isConnected());
         }
         return networkInfo != null && networkInfo.isConnected();
     }
 
     public LocationManager getLocationManager() {
         return this.locationManager;
     }
 
     AlertDialog buildAlertDialog() {
         if (photoAlertDialog == null) {
             AlertDialog.Builder builder = new AlertDialog.Builder(this);
             builder.setMessage("Do you want to add a photo, that could be very helpful!")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
                         }
                     })
                     .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
                             //dialog.dismiss();
                         }
                     });
             photoAlertDialog = builder.create();
         }
         return photoAlertDialog;
     }
 
     class UploadMultipartTask extends AsyncTask<String, Integer, String> {
 
         private ProgressDialog progressDialog;
 
         @Override
         protected void onPreExecute() {
             progressDialog = ProgressDialog.show(currentActivity, "", "");
         }
 
         @Override
         protected void onPostExecute(String result) {
             progressDialog.dismiss();
             if (result.equals("201")) {
                 Toast.makeText(getApplicationContext(), "Upload successfully.", Toast.LENGTH_LONG).show();
                 startActivity(new Intent(getApplicationContext(), ReportActivity.class));
             } else if (result.equals("301")){
             Toast.makeText(getApplicationContext(), "Not sign in to network?", Toast.LENGTH_LONG).show();
             } else {
                 Log.d(TAG, result);
                 Toast.makeText(getApplicationContext(), "Network disconnected.", Toast.LENGTH_LONG).show();
             }
         }
 
         @Override
         protected String doInBackground(String... params) {
             URL url = null;
             HttpURLConnection urlConnection = null;
             try {
                 if (localDev) {
                     url = new URL(getString(R.string.local_url));
                 } else {
                     url = new URL(getString(R.string.post_url));
                 }
             } catch(MalformedURLException mue) {
                 mue.printStackTrace();
                 return mue.getMessage();
             }
             try {
                 Log.d(getClass().getSimpleName(), "url: " + url);
                 urlConnection = (HttpURLConnection) url.openConnection();
                 urlConnection.setDoOutput(true);
                 urlConnection.setRequestProperty("Connection", "Keep-Alive");
                 urlConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + BOUNDARY);
                 String credential = username + ":" + password;
                 String encodedCredential = Base64.encodeToString(credential.getBytes(), Base64.DEFAULT);
                 urlConnection.setRequestProperty("Authorization", "Basic " + encodedCredential);
                 String requestBodyFirstPart = null;
                 if (actionBar.getSelectedTab().getTag().equals("indoor")) {
                     requestBodyFirstPart = indoorFormFragment.generateMultipartForm();
                 } else {
                     requestBodyFirstPart = outdoorFormFragment.generateMultipartForm();
                 }
                 urlConnection.connect();
                 DataOutputStream out = new DataOutputStream(urlConnection.getOutputStream());
                 out.writeBytes(requestBodyFirstPart);
                 Log.d(getClass().getSimpleName(), requestBodyFirstPart);
                 // photos
                 int num = 0;
                 for (int i=0; i<3; i++) {
                     Log.d(TAG, "Uploading photo...");
                     if (params[i] != null) {
                         num++;
                         out.writeBytes(TWO_HYPHENS + BOUNDARY + END);
                         File photo = new File(params[i]);
                         out.writeBytes("Content-Disposition: form-data; name=\"photo" + num + "\"; filename=\"" + photo.getName() + "\"" + END);
                         out.writeBytes("Content-type: image/jpeg" + END + END);
                         FileInputStream fileInputStream = new FileInputStream(params[i]);
                         byte[] fileBuffer = new byte[1024];
                         int length = 0;
                         while ((length = fileInputStream.read(fileBuffer)) != -1) {
                             out.write(fileBuffer, 0, length);
                         }
                         fileInputStream.close();
                         out.writeBytes(END);
                     }
                 }
                 // Audio.
                 if (params[3] != null) {
                     Log.d(TAG, "Uploading audio...");
                     out.writeBytes(TWO_HYPHENS + BOUNDARY + END);
                     File audio = new File(params[3]);
                     out.writeBytes("Content-Disposition: form-data; name=\"audio\"; filename=\"" + audio.getName() + "\"" + END);
                    out.writeBytes("Content-type: audio/mp4" + END + END);
                     FileInputStream fileInputStream = new FileInputStream(params[3]);
                     byte[] fileBuffer = new byte[1024];
                     int len = 0;
                     while ((len = fileInputStream.read(fileBuffer)) != -1) {
                         out.write(fileBuffer, 0, len);
                     }
                     fileInputStream.close();
                     out.writeBytes(END);
                 }
                 // End
                 out.writeBytes(TWO_HYPHENS + BOUNDARY + TWO_HYPHENS);
                 out.flush();
                 out.close();
                 Log.d(getClass().getSimpleName(), "Finish uploading...");
                 int responseCode = urlConnection.getResponseCode();
                 Log.d(getClass().getSimpleName(), "" + responseCode);
                 if (responseCode == HttpURLConnection.HTTP_CREATED) {
                     return "201";
                 } else if (responseCode== HttpURLConnection.HTTP_OK) {
                     Log.d(TAG, "dumping to /etc/dev");
                     return "301";
                 } else {
                     return "400";
                 }
             } catch (IOException ioe) {
                 ioe.printStackTrace();
                 return "400";
             } finally {
                 urlConnection.disconnect();
             }
         }
     }
 
     static class AudioPlayListener implements OnClickListener {
 
         private static final String TAG = "FormActivity$AudioPlayListener";
         private MediaPlayer mediaPlayer;
         private String audioFile;
 
         public AudioPlayListener(MediaPlayer mp, String af) {
             this.mediaPlayer = mp;
             this.audioFile = af;
         }
 
         @Override
         public void onClick(View v) {
             if (mediaPlayer == null) {
                 mediaPlayer = new MediaPlayer();
                 try {
                     mediaPlayer.setDataSource(audioFile);
                     mediaPlayer.prepare();
                     mediaPlayer.start();
                 } catch (IOException ioe) {
                     Log.d(TAG, ioe.getMessage());
                 }
             } else {
                 mediaPlayer.release();
                 mediaPlayer = null;
             }
         }
     }
 
 }
