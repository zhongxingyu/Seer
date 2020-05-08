 package ca.archlinux;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.widget.Button;
 import android.widget.TextView;
 import android.view.View;
 import android.view.Menu;
 import android.view.MenuItem;
 import java.net.URL;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 
 import ca.archlinux.QuodroidSettings;
 
 public class Quodroid extends Activity {
     /** Called when the activity is first created. */
     private Button prev_button, next_button, pause_button;
     private TextView status_text;
     private static final int ACTIVITY_CREATE=0;
     private static final int MENU_SETTINGS = Menu.FIRST;
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         findViewById(R.id.prev_button).setOnClickListener(
                 new ButtonAction("previous"));
         findViewById(R.id.next_button).setOnClickListener(
                 new ButtonAction("next"));
         findViewById(R.id.pause_button).setOnClickListener(
                 new ButtonAction("play-pause"));
         findViewById(R.id.volumeup_button).setOnClickListener(
                 new ButtonAction("volume-up"));
         findViewById(R.id.volumedown_button).setOnClickListener(
                 new ButtonAction("volume-down"));
 
         status_text = (TextView) findViewById(R.id.status_text);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         boolean result = super.onCreateOptionsMenu(menu);
 
         menu.add(0, MENU_SETTINGS, 0, R.string.menu_settings);
 
         return result;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case MENU_SETTINGS:
             Intent i = new Intent(this, QuodroidSettings.class);
             startActivityForResult(i, ACTIVITY_CREATE);
             return true;
         }
        
         return super.onOptionsItemSelected(item);
     }
 
     private class ButtonAction implements Button.OnClickListener {
         private String action;
         public ButtonAction(String action) {
             this.action = action;
         }
 
         @Override
         public void onClick(View view) {
             try {
                 SharedPreferences settings = getSharedPreferences(
                         QuodroidSettings.PREFS_NAME, 0);
                 String host = settings.getString("host", "192.168.1.110");
                 String port = settings.getString("port", "9250");
                 URL url = new URL("http://" + host + ":" + port + "/" + 
                         action + "/");
                 String response = new BufferedReader(
                         new InputStreamReader(url.openStream())).readLine();
                 status_text.setText(response);
             }
             catch (Exception e) {
                status_text.setText("error");
             }
         }
     }
 }
