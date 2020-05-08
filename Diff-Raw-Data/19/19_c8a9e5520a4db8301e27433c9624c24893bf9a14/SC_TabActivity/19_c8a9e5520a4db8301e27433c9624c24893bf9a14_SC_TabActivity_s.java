 package nl.sense.demo;
 
 import android.app.TabActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.widget.TabHost;
 import android.widget.TabHost.TabSpec;
 import nl.sense.demo.R; 
 
 @SuppressWarnings("deprecation")
 public class SC_TabActivity extends TabActivity {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	
 
 //        Intent Sense = new Intent(getApplicationContext(), MainActivity.class);
 //        startService(Sense);
         
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
 
  
         TabHost tabHost = getTabHost();
  
         // Tab for Sense
         TabSpec sensespec = tabHost.newTabSpec("Sense");
         // setting Title and Icon for the Tab
         sensespec.setIndicator("Sense", getResources().getDrawable(R.drawable.iconsensetab));
         Intent senseIntent = new Intent(this, SenseActivity.class);
         sensespec.setContent(senseIntent);
  
         // Tab for OSMap
         TabSpec mapspec = tabHost.newTabSpec("Map");
         mapspec.setIndicator("Map", getResources().getDrawable(R.drawable.iconmaptab));
         Intent mapIntent = new Intent(this, MapActivity.class);
         mapspec.setContent(mapIntent);
  
         // Tab for Info
         TabSpec infospec = tabHost.newTabSpec("Info");
         infospec.setIndicator("Info", getResources().getDrawable(R.drawable.iconinfotab));
         Intent infoIntent = new Intent(this, InfoActivity.class);
         infospec.setContent(infoIntent);
  
         // Adding all TabSpec to TabHost
         tabHost.addTab(sensespec); // Adding sense tab
         tabHost.addTab(mapspec); // Adding map tab
         tabHost.addTab(infospec); // Adding info tab
     }
    public final boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
 }
