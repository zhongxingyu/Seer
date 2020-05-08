 package com.weatherornot.weatherornot;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.AssetManager;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 
 import java.io.IOException;
 import java.io.InputStream;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.provider.Settings;
 import android.widget.Toast;
 
 /**
  * Created by cheryl on 10/22/13.
  */
 public class DisplaySettingsActivity extends Activity {
 
     static final String PREFERENCES = "temps";
     int hot;
     int cold;
     int perfect;
 
 
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.settings_activity);
 

         // Get Location Manager and check for GPS & Network location services
         LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
         if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                 !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
             // Build the alert dialog
             AlertDialog.Builder builder = new AlertDialog.Builder(this);
             builder.setTitle("Location Services Not Active");
             builder.setMessage("Please enable Location Services and GPS");
             builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialogInterface, int i) {
                     // Show location settings when the user acknowledges the alert dialog
                     Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                     startActivity(intent);
                 }
             });
             Dialog alertDialog = builder.create();
             alertDialog.setCanceledOnTouchOutside(false);
             alertDialog.show();
         }
 
 
 
 
         //////setting background image as drawable
         AssetManager manager = getAssets();
         InputStream open = null;
 
         try{
             open = manager.open("chalkboardbackground.png");
             Bitmap bitmap = BitmapFactory.decodeStream(open);
             ImageView view = (ImageView) findViewById(R.id.settingbackground);
             view.setImageBitmap(bitmap);
         }catch (IOException e){
             e.printStackTrace();
         }finally{
             if(open!= null){
                 try{
                     open.close();
                 }
                 catch (IOException e){
 
                 }
             }
         }
 
         SharedPreferences savedPreferencesHot = getSharedPreferences(PREFERENCES,0);
         //String savedTemp = savedPreferencesHot.getString("temphot","0");
 
         String Hot = savedPreferencesHot.getString("temphot","0");
         EditText h = (EditText) findViewById(R.id.hotNumber);
         h.setText(Hot);
 
 
         SharedPreferences savedPreferencesCold = getSharedPreferences(PREFERENCES,0);
         //String savedTempCold = savedPreferencesCold.getString("tempcold","0");
 
         String Cold = savedPreferencesCold.getString("tempcold","0");
         EditText c = (EditText) findViewById(R.id.coldNumber);
         c.setText(Cold);
 
 
         SharedPreferences savedPreferencesPerfect = getSharedPreferences(PREFERENCES,0);
         //String savedTempPerfect = savedPreferencesCold.getString("tempperfect","0");
 
         String Perfect = savedPreferencesPerfect.getString("tempperfect","0");
         EditText p = (EditText) findViewById(R.id.perfectNumber);
         p.setText(Perfect);
 
 
 
 
 
         Button buttonToDisplayWeatherActivity = (Button)findViewById(R.id.donebutton);
 
         buttonToDisplayWeatherActivity.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent i = new Intent(getApplicationContext(),DisplayWeatherActivity.class);
                 onPostResume();
                 finish();
             }
         }); {
 
 
         }
     }
 
     @Override
     protected void onStop() {
         super.onStop();
         SharedPreferences savedPreferencesHot = getSharedPreferences(PREFERENCES, 0);
         SharedPreferences.Editor editor = savedPreferencesHot.edit();
 
         EditText h = (EditText) findViewById(R.id.hotNumber);
         String hotString = h.getText().toString();
         editor.putString("temphot",hotString);
         editor.commit();
 
 
         SharedPreferences savedPreferencesCold = getSharedPreferences(PREFERENCES, 0);
         SharedPreferences.Editor editorCold = savedPreferencesCold.edit();
 
         EditText c = (EditText) findViewById(R.id.coldNumber);
         String coldString = c.getText().toString();
         editorCold.putString("tempcold",coldString);
         editorCold.commit();
 
 
         SharedPreferences savedPreferencesPerfect = getSharedPreferences(PREFERENCES, 0);
         SharedPreferences.Editor editorPerfect = savedPreferencesPerfect.edit();
 
         EditText p = (EditText)findViewById(R.id.perfectNumber);
         String perfectString = p.getText().toString();
         editorPerfect.putString("tempperfect",perfectString);
         editorPerfect.commit();
 
         int hot = new Integer(h.getText().toString());
        int cold =  new Integer(c.getText().toString());
         int perfect = new Integer(p.getText().toString());
 
         //set the preferences completed to true.
         editorPerfect.putBoolean("prefscompleted", true);
 
 
     }
 
 
 }
