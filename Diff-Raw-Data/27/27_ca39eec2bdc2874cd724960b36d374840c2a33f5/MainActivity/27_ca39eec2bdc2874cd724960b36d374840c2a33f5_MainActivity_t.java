 package com.david.where;
 
 import com.david.where.R;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MenuInflater;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.ToggleButton;
 
 public class MainActivity extends Activity {
 
     final String CREATE_TABLE =  "CREATE TABLE if not exists permissions (" 
     	   							+ "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
     	   							+ "contact_name TEXT," + "contact_number TEXT)"; 
 
     SQLiteDatabase mDatabase; 
     ToggleButton mOnOffToggle;
     Button mPermissionButton;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
        
         mDatabase = openOrCreateDatabase(Constants.DB_NAME, SQLiteDatabase.CREATE_IF_NECESSARY, null);
         mDatabase.execSQL(CREATE_TABLE);
         mDatabase.close();
        
         mOnOffToggle = (ToggleButton) findViewById(R.id.onOffToggle);
         mPermissionButton = (Button) findViewById(R.id.permission_button);
 
         mPermissionButton.setOnClickListener(clickListener);
         mOnOffToggle.setChecked(true);
         mOnOffToggle.setOnCheckedChangeListener(checkListener);   
 
         startServices();
  
     }
     
     public OnClickListener clickListener = new OnClickListener() {
         public void onClick(View aView) {
             switch (aView.getId()) {
                 case R.id.permission_button:
                     launchPermissions();
                     break;
                 default:
                     break;
             }
         }
     };
 
 
     OnCheckedChangeListener checkListener = new OnCheckedChangeListener() {
         public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
             int current = getResources().getConfiguration().orientation;
             if(isChecked) {
                 startServices();
            } else {
                stopServices();
             }
         }
     };
 
    private void stopServices() {
        stopService(new Intent(this, TextListenService.class));
        stopService(new Intent(this, LocationService.class));
     }
 
    private void startServices() {
        startService(new Intent(this, TextListenService.class));
        startService(new Intent(this, LocationService.class));
     }
  
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
 
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.menu, menu);
         return true;
     }
   
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
 
         switch (item.getItemId()) {
             case R.id.settings:
                 runSettings();
                 return true;
         }
         return false;
     }
 
 
     @Override
     protected void onResume() {
 	    super.onResume();
     }
 
     private void launchPermissions() {
         Intent i = new Intent(this,PermissionActivity.class);
         startActivity(i);
     }
 
     public boolean containsOnlyNumbers(String str) {
     
         if (str == null || str.length() == 0)
             return false;
         for (int i = 0; i < str.length(); i++) {
             if (!Character.isDigit(str.charAt(i)))
                 return false;
         }
         return true;
     }
 
     private void runSettings() {
         Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
         startActivity(intent);
     }
 
 }
 
 
