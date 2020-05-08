 package net.kjonigsen.forcedockrotation;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.provider.Settings;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 
 /**
  * Created by jostein on 29/06/13.
  */
 
 public class MainActivity extends Activity {
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
 
         this.setContentView(R.layout.main_activity);
 
         CheckInitialDockState task = new CheckInitialDockState();
         task.execute(this, savedInstanceState, null);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
 
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.mainactivitymenu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
 
         if (item.getItemId() == R.id.settingsmenuitem)
         {
             Intent settingsIntent = new Intent(this, SettingsActivity.class);
             startActivity(settingsIntent);
             return true;
         }
 
         return super.onOptionsItemSelected(item);
     }
 
 
     public class CheckInitialDockState extends AsyncTask
     {
         @Override
         protected Object doInBackground(Object... objects) {
            DeviceStatus status = DeviceStatus.fromSystem(MainActivity.this);
             DockRotationReceiver.startStopService(MainActivity.this, status.IsDocked);
 
             return null;
         }
     }
 }
