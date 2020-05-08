 package de.team06.psychoapp;
 
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.app.Activity;
 import android.preference.PreferenceManager;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 
     private DatabaseModel dbModel;
     private long socialInteractionID;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(de.team06.psychoapp.R.layout.input);
 
         SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
         if (preferences.getBoolean("firstRun", true) == true) {
             startActivity(new Intent(this, SettingsActivity.class));
         }
 
         // Recieve
        socialInteractionID = getIntent().getIntExtra("socialInteractionID", -1);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
     public boolean onOptionsItemSelected(MenuItem item) {
 
         switch (item.getItemId()) {
             case R.id.action_settings:
                 startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                 break;
             case R.id.action_setAlarm:
                 setDemoAlarm();
                 break;
         }
 
         return super.onOptionsItemSelected(item);
     }
 
     public void setDemoAlarm() {
         /*
             test alarm maker
          */
         AlarmMaker testAlarm = new AlarmMaker(this);
         testAlarm.addAlarm(System.currentTimeMillis() + 10000, TimeSection.FOURTH_QUARTER);
     }
 
     public void click(View view) {
         EditText anzahlEditText = (EditText) findViewById(R.id.anzahl);
         String anzahl = anzahlEditText.getText().toString();
 
         EditText hoursEditText = (EditText) findViewById(R.id.hours);
         String hours = hoursEditText.getText().toString();
 
         EditText minutesEditText = (EditText) findViewById(R.id.minutes);
         String minutes = minutesEditText.getText().toString();
 
         Toast.makeText(this, anzahl + " SocialContacts and " + hours + "h " + minutes + " min", Toast.LENGTH_SHORT).show();
 
         dbModel = new DatabaseModel(this);
         dbModel.open();
 
        if (this.socialInteractionID != -1){
             // Get SocialInteraction object from database
            SocialInteraction socialInteraction = dbModel.getSocialInteractionByID(this.socialInteractionID);
 
             // Update object
             socialInteraction.setNumberOfContacts(Integer.valueOf(anzahl));
             socialInteraction.setHours(Integer.valueOf(hours));
             socialInteraction.setMinutes(Integer.valueOf(minutes));
             socialInteraction.setSkipped(0);
             socialInteraction.setResponseTime(System.currentTimeMillis());
 
             // Save object
             dbModel.updateSocialInteraction(socialInteraction);
         }
         else
             Toast.makeText(this, "Fehler: Es wurde keine SocialInteractionID übergeben!", Toast.LENGTH_SHORT).show();
 
         dbModel.close();
     }
 
 }
