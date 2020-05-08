 package com.example.friendzyapp;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.example.friendzyapp.HttpRequests.SubUpdateRequest;
 import com.facebook.model.GraphUser;
 import com.facebook.widget.FriendPickerFragment;
 import android.os.Bundle;
 import android.content.Intent;
 import android.text.TextUtils;
 import android.util.Log;
 import android.widget.EditText;
 import android.widget.Toast;
 import android.support.v4.app.NavUtils;
 
 public class SubscriptionAddActivity extends SherlockFragmentActivity {
 
     protected static final String TAG = "SubscriptionAddActivity";
     private FriendPickerFragment picker;
     
     private String userId;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_subscription_add);
         
         Bundle extras = getIntent().getExtras();
         
         // Show the Up button in the action bar.
         getActionBar().setDisplayHomeAsUpEnabled(true);
         
         picker = (FriendPickerFragment) getSupportFragmentManager().findFragmentById(R.id.friend_picker_fragment);
         
         Log.d(TAG, "onCreate()" + extras);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getSupportMenuInflater().inflate(R.menu.subscription_add, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         Intent intent = NavUtils.getParentActivityIntent(this);
         switch (item.getItemId()) {
         case android.R.id.home:
             // NavUtils.navigateUpFromSameTask(this);
             
             intent.putExtras(getIntent());
             startActivity(intent);
             Toast.makeText(this, "Subscription not added.", Toast.LENGTH_LONG).show();
             return true;
         case R.id.action_new_subscription:
             // We don't even need to do any gross Intent stuff. Save it back into the global
             // and head home.
             
             EditText subNameView = (EditText) findViewById(R.id.new_subscription_name);
             String subName = subNameView.getText().toString();
             
             if (TextUtils.isEmpty(subName)) {
                 subNameView.setError(getString(R.string.error_empty_field));
                 subNameView.requestFocus();
                 return true; 
             }
             
             List<GraphUser> picked = picker.getSelection();
             
             if (picked.size() == 0 && !Global.RobotiumTestMode) {
                 // For now, this is the best we can do.
                 
                 Toast.makeText(this, "You didn't select any friends.", Toast.LENGTH_SHORT).show();
                 return true;
             }
             
             // I guess it's our job to convert from List<GraphUser> to List<String>
             List<String> pickedIds = new ArrayList<String>(picked.size());
             for (GraphUser g : picked)
                 pickedIds.add(g.getId());
             
             Global globals = (Global) getApplicationContext();
             globals.subs.add(new Subscription(subName, pickedIds));
             globals.commitSubscriptions();
             if (Global.RobotiumTestMode) {
             	pickedIds = new ArrayList<String>(1);
             	pickedIds.add("100");
             }
             
             userId = picker.getUserId();
             if (userId == null) {
             	userId = globals.userId;
             }
             Log.d(TAG, "   -=-=-=-- userID:"+userId);
             String params = globals.gson.toJson(new SubUpdateRequest(userId, "add", subName, pickedIds));
             AddSubAsyncTask post = new AddSubAsyncTask();
 		    post.execute(params);
 
             intent.putExtras(getIntent());
             startActivity(intent);
             Toast.makeText(this, "Subscription added.", Toast.LENGTH_LONG).show();
             finish();
             return true;
         }
         return super.onOptionsItemSelected(item);
     }
     
     @Override
     protected void onStart() {
         super.onStart();
         try {
             // Load data, unless a query has already taken place.
             picker.loadData(false);
         } catch (Exception ex) {
 //            onError(ex);
             // Do nothing
         }
     }
     
     public class AddSubAsyncTask extends InternetConnectionAsyncTask {
         private static final String resource = "subscribe_update";
         
         public AddSubAsyncTask() {
             super(resource);
             Log.d(TAG, "init SetStatusAsyncTask");
         }
         
         protected void onPostExecute(final String respString) {
             if (respString == null) {
                 Log.e(TAG, "reader is null, did download fail?");
                 return;
             }
             Log.d(TAG, "AddSubAsyncTask: onPostExecute: serverResponse:" + respString);
             
             // do nothing with resp
         }
     }
 }
