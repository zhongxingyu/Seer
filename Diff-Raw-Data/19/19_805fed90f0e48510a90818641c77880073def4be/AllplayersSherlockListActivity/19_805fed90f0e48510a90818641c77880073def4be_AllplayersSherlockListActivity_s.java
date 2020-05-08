 package com.allplayers.android.activities;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockListActivity;
 import com.actionbarsherlock.view.MenuItem;
 import com.allplayers.android.EventsActivity;
 import com.allplayers.android.FindGroupsActivity;
 import com.allplayers.android.GroupsActivity;
 import com.allplayers.android.Login;
 import com.allplayers.android.MessageActivity;
 import com.allplayers.android.PhotosActivity;
 import com.allplayers.android.R;
 import com.allplayers.rest.RestApiV1;
 import com.devspark.sidenavigation.ISideNavigationCallback;
 import com.devspark.sidenavigation.SideNavigationView;
 
 public class AllplayersSherlockListActivity extends SherlockListActivity implements ISideNavigationCallback {
     protected SideNavigationView mSideNavigationView;
     protected ActionBar mActionBar;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         mActionBar = getSupportActionBar();
         mActionBar.setIcon(R.drawable.menu_icon);
     }
 
     /**
      * onStop().
      * Called when the activity is stopped. This function will save critical data such as the
      *  current user's UUID and the current Cookie Handler to local storage in the form of shared
      *  preferences.
      *
      */
     @Override
     public void onStop() {
         super.onStop();
 
         SharedPreferences sharedPreferences = getSharedPreferences("Critical_Data", 0);
         SharedPreferences.Editor editor = sharedPreferences.edit();
         String UUID = RestApiV1.getCurrentUserUUID();
         editor.putString("UUID", UUID);
         editor.commit();
     }
 
     /**
      * onRestart().
      * Called when the activity is restarted after being stopped. Will fetch critical data such as
      *  the current usre's UUID and the current Cookie Handler from local storage in the form of
      *  shared preferences.
      *
      */
     @Override
     public void onRestart() {
         super.onRestart();
 
         SharedPreferences sharedPreferences = getSharedPreferences("Critical_Data", 0);
         RestApiV1.setCurrentUserUUID(sharedPreferences.getString("UUID", "ERROR: The string UUID could not be fetched from sharedPreferences"));
     }
 
     /**
      * Listener for the Action Bar Options Menu.
      *
      * @param item: The selected menu item.
      */
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
 
         switch (item.getItemId()) {
 
         case android.R.id.home: {
             mSideNavigationView.toggleMenu();
             return true;
         }
 
         default:
             return super.onOptionsItemSelected(item);
         }
     }
 
     /**
      * Listener for the Side Navigation Menu.
      *
      * @param itemId: The ID of the list item that was selected.
      */
     @Override
     public void onSideNavigationItemClick(int itemId) {
 
         switch (itemId) {
 
         case R.id.groups:
             invokeActivity(GroupsActivity.class);
             break;
 
         case R.id.messages:
             invokeActivity(MessageActivity.class);
             break;
 
         case R.id.photos:
             invokeActivity(PhotosActivity.class);
             break;
 
         case R.id.events:
             invokeActivity(EventsActivity.class);
             break;
 
         case R.id.search: {
             search();
             break;
         }
 
         case R.id.log_out: {
             logOut();
             break;
         }
 
         case R.id.refresh: {
             refresh();
             break;
         }
 
         default:
             return;
         }
 
         finish();
     }
 
     /**
      * Helper method for onSideNavigationItemClick. Starts the passed in
      * activity.
      *
      * @param activity: The activity to be started.
      */
     @SuppressWarnings("rawtypes")
     protected void invokeActivity(Class activity) {
 
         Intent intent = new Intent(this, activity);
         intent.addFlags(32768 | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
         startActivity(intent);
 
         overridePendingTransition(0, 0); // Disables new activity animation.
     }
 
     /**
      * Opens the search screen.
      */
     protected void search() {
 
         startActivity(new Intent(this, FindGroupsActivity.class));
     }
 
     /**
      * Logs the user out of the application.
      */
     protected void logOut() {
 
         new LogOutTask().execute();
 
         AccountManager manager = AccountManager.get(this.getBaseContext());
         Account[] accounts = manager.getAccountsByType("com.allplayers.android");
 
         if (accounts.length == 1) {
             manager.removeAccount(accounts[0], null, null);
         }
 
         invokeActivity(Login.class);
         finish();
     }
 
     /**
      * Refreshes the current activity to update information.
      */
     protected void refresh() {
 
         finish();
         startActivity(getIntent());
     }
 
     /**
      * Helper class to handle the network call needed to log out asynchronously.
      */
     protected class LogOutTask extends AsyncTask<Void, Void, Void> {
 
         protected Void doInBackground(Void... args) {
 
             RestApiV1.logOut();
             return null;
         }
     }
 }
