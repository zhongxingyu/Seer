 /*
  * Copyright 2013 Google Inc.
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
  */
 package com.google.android.gcm.demo.app;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.FeatureInfo;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.inputmethod.EditorInfo;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.google.android.gcm.demo.app.TopicFeedActivity.GetMessagesResult;
 import com.google.android.gcm.demo.app.TopicFeedActivity.Message;
 import com.google.android.gms.gcm.GoogleCloudMessaging;
 import com.google.gson.Gson;
 
 import java.io.IOException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.apache.http.*;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 
 /**
  * Main UI for the demo app.
  */
 public class DemoActivity extends Activity {
 
     public static final String EXTRA_MESSAGE = "message";
     public static final String PROPERTY_REG_ID = "registration_id";
     private static final String PROPERTY_APP_VERSION = "appVersion";
     private static final String PROPERTY_ON_SERVER_EXPIRATION_TIME =
             "onServerExpirationTimeMs";
     /**
      * Default lifespan (7 days) of a reservation until it is considered expired.
      */
     public static final long REGISTRATION_EXPIRY_TIME_MS = 1000 * 3600 * 24 * 7;
     /**
      * You must use your own project ID instead.
      */
     String SENDER_ID = "649471969080";
 
     /**
      * Tag used on log messages.
      */
     static final String TAG = "GCMPOC";
 
     TextView mDisplay;
     GoogleCloudMessaging gcm;
     AtomicInteger msgId = new AtomicInteger();
     Context context;
 
     String regid;
     
 
     /* Menu Code */
 	
 	public boolean onCreateOptionsMenu(Menu menu) {
 	    MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.topicpagemenu, menu);
 	    return true;
 	}
 	
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		    case R.id.aboutTopicPage:
 		    	Intent aboutIntent = new Intent(DemoActivity.this, AboutActivity.class);
 	        	startActivity(aboutIntent);
 		    return true;
 		    case R.id.helpTopicPage:
 		    	Intent helpIntent = new Intent(DemoActivity.this, TopicPageHelpActivity.class);
 	        	startActivity(helpIntent);
 		    return true;
 		    case R.id.addTopic:
 		    	Intent addIntent = new Intent(DemoActivity.this, AddTopicActivity.class);
 	        	startActivity(addIntent);
 			return true;
 		    default:
 		    return super.onOptionsItemSelected(item);
 		}
	}
 
     class result {    	
     	int RegisterResult;
     }
     
     class GetSubscribedTopicsResult{		
 		List<String> GetSubscribedTopicsResult;
 
 	}
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.main);
  //       mDisplay = (TextView) findViewById(R.id.display);
 
         context = getApplicationContext();
         regid = getRegistrationId(context);
         
         Log.v(TAG, "ID " + regid);        
         
         LoadList();
     
  		final Button regbtn = (Button) findViewById(R.id.Add);
  		final EditText editText = (EditText)findViewById(R.id.editText1);		
 
  		regbtn.setOnClickListener(new View.OnClickListener() {
  			@Override
  			public void onClick(View v) {
  				searchRequest();
  				LoadList();
  				editText.setText("");
  			}
  		});		 		
  		
  		regbtn.setEnabled(false);
     	 		
  		editText.addTextChangedListener(new TextWatcher() {
 
  		    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
  		    public void onTextChanged(CharSequence s, int start, int before, int count) {}
 
  		    public void afterTextChanged(Editable s) {
  		        if (s == null || s.length() == 0) {
  		        	regbtn.setEnabled(false);
  		        }
  		        else {
  		        	regbtn.setEnabled(true);
  		        }
  		    }
  		});        
         
         if (regid.length() == 0) {
             registerBackground();
         }
         gcm = GoogleCloudMessaging.getInstance(this);
     }
     
     private void LoadList() {
 		// TODO Auto-generated method stub
     	final ListView listview = (ListView) findViewById(R.id.listView1);    	
     	        
         final SharedPreferences prefs = getGCMPreferences(context);
         String registrationId = prefs.getString(PROPERTY_REG_ID, "");  
         
         GetSubscribedTopicsResult topiclist = new GetSubscribedTopicsResult();
         
         HashMap<String, String> param = new HashMap<String, String>();        
         param.put("regId", registrationId);
         POSTRequest asyncHttpPost = new POSTRequest(param);
         try {
 	        String str_result = asyncHttpPost.execute("http://10.0.2.2:58145/PushNotificationService.svc/GetSubscribedTopics").get();
 			Gson gson = new Gson(); 
 			topiclist = gson.fromJson(str_result, GetSubscribedTopicsResult.class);        
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
         
         final ArrayList<String> list = new ArrayList<String>();
         for (String topic : topiclist.GetSubscribedTopicsResult){
           list.add(topic);
         }
         
         final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                 android.R.layout.simple_list_item_1, list);
         
         listview.setAdapter(adapter);        
         
         listview.setOnItemClickListener(new OnItemClickListener()
         {
 		        public void onItemClick(AdapterView<?> arg0, View v, int position, long id)
 		        {      
 		        	Intent mIntent = new Intent(DemoActivity.this, TopicFeedActivity.class);
 		        	
 		        	mIntent.putExtra("FeedName", listview.getItemAtPosition(position).toString()); 
 		        	
 		        	startActivity(mIntent);
                 }
          });
 	}   
     
 	private void searchRequest(){
     	EditText editText = (EditText)findViewById(R.id.editText1);
 
     	String editTextStr = editText.getText().toString();
     	
         final SharedPreferences prefs = getGCMPreferences(context);
         String registrationId = prefs.getString(PROPERTY_REG_ID, "");       
         
         HashMap<String, String> data = new HashMap<String, String>();
         data.put("regId", registrationId);
         data.put("searchTerm", editTextStr);
         POSTRequest asyncHttpPost = new POSTRequest(data);
         try {
 			String str_result = asyncHttpPost.execute("http://10.0.2.2:58145/PushNotificationService.svc/Register").get();
 			Gson gson = new Gson(); 
 			result i = gson.fromJson(str_result, result.class);
 			
 			AlertDialog.Builder alertDialogBuilderConfirm = new AlertDialog.Builder(
 					DemoActivity.this);
 			if(i.RegisterResult == 0){
 				alertDialogBuilderConfirm.setMessage("Topic successfully added!");
 			}else{				
 				alertDialogBuilderConfirm.setMessage("Oops something went wrong!");
 			}
 			
 			alertDialogBuilderConfirm.setCancelable(true);
 			alertDialogBuilderConfirm.setNeutralButton(android.R.string.ok,
 		            new DialogInterface.OnClickListener() {
 		        public void onClick(DialogInterface dialog, int id) {
 		            dialog.cancel();
 		        }
 		    });
 			
 			// create alert dialog
 			AlertDialog alertDialogConfirm = alertDialogBuilderConfirm.create();
 
 			// show it
 			alertDialogConfirm.show();
 		
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}    
         
         
     }
 
     /**
      * Stores the registration id, app versionCode, and expiration time in the application's
      * {@code SharedPreferences}.
      *
      * @param context application's context.
      * @param regId registration id
      */
     private void setRegistrationId(Context context, String regId) {
         final SharedPreferences prefs = getGCMPreferences(context);
         int appVersion = getAppVersion(context);
         Log.v(TAG, "Saving regId on app version " + appVersion);
         SharedPreferences.Editor editor = prefs.edit();
         editor.putString(PROPERTY_REG_ID, regId);
         editor.putInt(PROPERTY_APP_VERSION, appVersion);
         long expirationTime = System.currentTimeMillis() + REGISTRATION_EXPIRY_TIME_MS;
 
         Log.v(TAG, "Setting registration expiry time to " +
                 new Timestamp(expirationTime));
         editor.putLong(PROPERTY_ON_SERVER_EXPIRATION_TIME, expirationTime);
         editor.commit();
     }
 
     /**
      * Gets the current registration id for application on GCM service.
      * <p>
      * If result is empty, the registration has failed.
      *
      * @return registration id, or empty string if the registration is not
      *         complete.
      */
     private String getRegistrationId(Context context) {
         final SharedPreferences prefs = getGCMPreferences(context);
         String registrationId = prefs.getString(PROPERTY_REG_ID, "");
         if (registrationId.length() == 0) {
             Log.v(TAG, "Registration not found.");
             return "";
         }
         // check if app was updated; if so, it must clear registration id to
         // avoid a race condition if GCM sends a message
         int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
         int currentVersion = getAppVersion(context);
         if (registeredVersion != currentVersion || isRegistrationExpired()) {
             Log.v(TAG, "App version changed or registration expired.");
             return "";
         }
         return registrationId;
     }
 
     /**
      * Registers the application with GCM servers asynchronously.
      * <p>
      * Stores the registration id, app versionCode, and expiration time in the application's
      * shared preferences.
      */
     private void registerBackground() {
         new AsyncTask<Void, Void, String>() {
             @Override
             protected String doInBackground(Void... params) {
                 String msg = "";
                 try {
                     if (gcm == null) {
                         gcm = GoogleCloudMessaging.getInstance(context);
                     }
                     regid = gcm.register(SENDER_ID);
                     msg = "Device registered, registration id=" + regid;
 
                     // You should send the registration ID to your server over HTTP, so it
                     // can use GCM/HTTP or CCS to send messages to your app.
 
                     // For this demo: we don't need to send it because the device will send
                     // upstream messages to a server that echo back the message using the
                     // 'from' address in the message.
 
                     // Save the regid - no need to register again.
                     setRegistrationId(context, regid);
                 } catch (IOException ex) {
                     msg = "Error :" + ex.getMessage();
                 }
                 return msg;
             }
 
             @Override
             protected void onPostExecute(String msg) {
                 mDisplay.append(msg + "\n");
             }
         }.execute(null, null, null);
     }
 
     public void onClick(final View view) {
        /* if (view == findViewById(R.id.send)) {
             new AsyncTask<Void, Void, String>() {
                 @Override
                 protected String doInBackground(Void... params) {
                     String msg = "";
                     try {
                         Bundle data = new Bundle();
                         data.putString("hello", "World");
                         String id = Integer.toString(msgId.incrementAndGet());
                         gcm.send(SENDER_ID + "@gcm.googleapis.com", id, data);
                         msg = "Sent message";
                     } catch (IOException ex) {
                         msg = "Error :" + ex.getMessage();
                     }
                     return msg;
                 }
 
                 @Override
                 protected void onPostExecute(String msg) {
                     mDisplay.append(msg + "\n");
                 }
             }.execute(null, null, null);
         } else if (view == findViewById(R.id.clear)) {
             mDisplay.setText("");
         } */
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
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
 
     /**
      * @return Application's {@code SharedPreferences}.
      */
     private SharedPreferences getGCMPreferences(Context context) {
         return getSharedPreferences(DemoActivity.class.getSimpleName(), Context.MODE_PRIVATE);
     }
 
     /**
      * Checks if the registration has expired.
      *
      * <p>To avoid the scenario where the device sends the registration to the
      * server but the server loses it, the app developer may choose to re-register
      * after REGISTRATION_EXPIRY_TIME_MS.
      *
      * @return true if the registration has expired.
      */
     private boolean isRegistrationExpired() {
         final SharedPreferences prefs = getGCMPreferences(context);
         // checks if the information is not stale
         long expirationTime =
                 prefs.getLong(PROPERTY_ON_SERVER_EXPIRATION_TIME, -1);
         return System.currentTimeMillis() > expirationTime;
     }
 }
