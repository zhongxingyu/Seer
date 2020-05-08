 package autobahn.android;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import com.example.autobahn.R;
 
 public class MainMenu extends Activity {
     /**
      * Called when the activity is first created.
      */
 
     Button button;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         button = (Button) findViewById(R.id.about);
 
         button.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent aboutActivity = new Intent();
                 aboutActivity.setClass(getApplicationContext(), AboutActivity.class);
                 startActivity(aboutActivity);
 
             }
         });
 
 
         button = (Button) findViewById(R.id.idms);
 
         button.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent idmsActivity = new Intent();
                 idmsActivity.setClass(getApplicationContext(),IdmsActivity.class);
                 startActivity(idmsActivity);
 
             }
         });
 
        button.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent circuitActivity = new Intent();
                 circuitActivity.setClass(getApplicationContext(), TrackCircuitActivity.class);
                 startActivity(circuitActivity);
             }
        });
 
 
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         //
         inflater.inflate(R.menu.menu, menu);
         //
         return true; //
     }
 
     // Called when an options item is clicked
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             //
             case R.id.itemPrefs:
                 startActivity(new Intent(this, PrefsActivity.class)); //
                 break;
         }
         return true;
         //
     }
 
 }
