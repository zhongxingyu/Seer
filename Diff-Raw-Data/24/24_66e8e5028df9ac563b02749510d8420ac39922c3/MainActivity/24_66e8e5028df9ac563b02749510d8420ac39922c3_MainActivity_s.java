 package net.kjonigsen.silenthours;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         ApplicationStatus status = ApplicationStatusProvider.getFor(this);
 
         setText(R.id.text_service_status, new Boolean(status.SilentHoursEnabled).toString());
         setText(R.id.text_application_event, status.NextApplicationEvent.toString());
     }
 
     private void setText(int control, String value)
     {
         TextView tv = (TextView)findViewById(control);
         tv.setText(value);
     }
 
 
     /* settings menu */
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // we know this is settings. Its the only option.
 
         switch (item.getItemId())
         {
 
             case R.id.action_settings:
                 Intent intent = new Intent(this, SettingsActivity.class);
                 startActivity(intent);
                 break;
 
         }
 
         return true;
     }
 }
