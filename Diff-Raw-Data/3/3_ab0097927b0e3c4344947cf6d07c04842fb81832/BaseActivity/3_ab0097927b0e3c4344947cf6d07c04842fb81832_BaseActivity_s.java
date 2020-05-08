 package com.thoughtworks.activities;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.View;
 import android.view.Window;
 import android.widget.Toast;
 import com.thoughtworks.R;
 import com.thoughtworks.database.DBHelper;
 import com.thoughtworks.models.City;
 import com.thoughtworks.utils.Constants;
 import com.thoughtworks.widget.ActionBar;
 
 import static com.thoughtworks.utils.Constants.CITY_PREFS;
 import static com.thoughtworks.utils.Constants.PREFS_NAME;
 
 public class BaseActivity extends Activity {
     private ActionBar mActionBar;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
     }
 
     public void setActionBar() {
         mActionBar = (ActionBar) findViewById(R.id.actionBar);
         mActionBar.setTitle(R.string.app_name);
         mActionBar.setHomeLogo(R.drawable.panic);
         mActionBar.addActionIcon(R.drawable.ic_menu_sync, new View.OnClickListener() {
             public void onClick(View view) {
                 syncData();
             }
         });
     }
 
     public String getCity() {
         String city = retrieveFromSharedPreference();
         if (city.equals("")) {
             updateSharedPreference(getCityFromDB());
         }
         return retrieveFromSharedPreference();
     }
 
     private void updateSharedPreference(City city) {
         if (city == null)
             return;
         SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
         SharedPreferences.Editor edit = settings.edit();
         edit.putString(Constants.CITY_PREFS, city.getName());
         edit.putInt(Constants.CITY_ID_PREFS, city.getId());
         edit.commit();
     }
 
     private String retrieveFromSharedPreference() {
         SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
         return settings.getString(CITY_PREFS, "");
     }
 
     private City getCityFromDB() {
         Cursor cursor = new DBHelper().getACity(this);
         if (cursor == null || cursor.getCount() == 0) {
             return null;
         }
         cursor.moveToFirst();
         City city = new City(cursor);
         cursor.close();
         return city;
     }
 
     private void syncData() {
         final Context context = this;
         if (isNetworkAvailable()) {
             new SyncActivity(BaseActivity.this).sync();
             new Handler().postDelayed(new Runnable() {
                 public void run() {
                    finish();
                     Intent intent = new Intent(context, SplashScreenActivity.class);
                     intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                     startActivityForResult(intent, RESULT_FIRST_USER);
                    finish();
                 }
             }, 2000);
         } else {
             Toast toast = Toast.makeText(BaseActivity.this, "You need data connection to Sync content", 15);
             toast.show();
         }
     }
 
     private boolean isNetworkAvailable() {
         ConnectivityManager cm = (ConnectivityManager)
                 getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo networkInfo = cm.getActiveNetworkInfo();
         return networkInfo != null && networkInfo.isConnected();
     }
 
 }
