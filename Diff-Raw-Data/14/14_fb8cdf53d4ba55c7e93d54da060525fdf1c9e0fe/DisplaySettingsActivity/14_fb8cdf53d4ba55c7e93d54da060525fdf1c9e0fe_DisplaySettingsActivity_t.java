 package com.weatherornot.weatherornot;
 
 import android.annotation.TargetApi;
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.AlertDialog;
 
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.location.LocationManager;
 
 import android.os.Build;
 import android.os.Bundle;
 
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 
 
 public class DisplaySettingsActivity extends Activity {
 
 
 
     static final String PREFERENCES = "temps";
 //    public static final String tempsSaved = "tempsSaved";
 //    EditText hot;
 //    EditText cold;
 //    EditText perfect;
 //    int hot1;
 //    int cold1;
 //    int perfect1;
 
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.settings_activity);
 
         checkIfNetworkLocationAvailable();
 
 
 
 
         Button doneSaveButton = (Button) findViewById(R.id.donebutton);
 
         doneSaveButton.setOnClickListener(new View.OnClickListener() {
 
             @Override
             public void onClick(View view) {
 
 
                 EditText h = (EditText) findViewById(R.id.hotNumber);
                 EditText c = (EditText) findViewById(R.id.coldNumber);
                 EditText p = (EditText) findViewById(R.id.perfectNumber);
 
                 SharedPreferences myPrefs = getSharedPreferences(PREFERENCES, 0);
                 SharedPreferences.Editor editor = myPrefs.edit();
 
 
                 String hot = h.getText().toString();
                 int x = Integer.parseInt(hot);
                 editor.putInt("hot", x);
                 String cold = c.getText().toString();
                 int y = Integer.parseInt(cold);
                 editor.putInt("cold", y);
                 String perfect = p.getText().toString();
                 int z = Integer.parseInt(perfect);
                 editor.putInt("perfect", z);
                 editor.putBoolean("tempSaved",true);
                 editor.commit();
                 finish();
 
                 Log.e("LOOK--------------------------------- prefs saved", Integer.valueOf(x)+ Integer.valueOf(y) + Integer.valueOf(z).toString());
                 editor.putBoolean("tempSaved",true);
 
 
                 Intent toWeather = new Intent(getApplicationContext(), DisplayWeatherActivity.class);
 //                toWeather.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                 startActivity(toWeather);
 
 
 
 
 
             }
         });
 //        ActionBar actionBar = getActionBar();
     }
 
     /**
      * checks if NETWORK LOCATION PROVIDER is available
      */
     private void checkIfNetworkLocationAvailable() {
 
         LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
         boolean networkLocationEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
 
         if(networkLocationEnabled==false){
             //show dialog to allow user to enable location settings
             AlertDialog.Builder dialog = new AlertDialog.Builder(this);
             dialog.setTitle("Location Services Not Active");
             dialog.setMessage("Please enable Location Services and GPS");
 
             dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int which) {
                     startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
 
 
 
 
                 }
 
             });
 
 
             dialog.show();
 
 
 
         }
         else if(networkLocationEnabled==true){
             loadPrefs();
 
 
         }
     }
 
 private void loadPrefs(){
     SharedPreferences myPrefs = getSharedPreferences(PREFERENCES, 0);
     boolean lp = myPrefs.getBoolean("tempSaved",true);
 
     if(lp == true){
         Intent i = new Intent(getApplicationContext(),DisplayWeatherActivity.class);
 //        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
         startActivity(i);
 
    }else if(lp== false) {
         onResume();
    }
 
 }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle presses on the action bar items
         switch (item.getItemId()) {
            case R.id.home:
 
                 Intent i = new Intent(getApplicationContext(), DisplaySettingsActivity.class);
 
                 startActivity(i);
 
                 return true;
 
             default:
                 return super.
 
                         onOptionsItemSelected(item);
         }
     }
 
 
 
 }
