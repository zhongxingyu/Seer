 package destinationsalinas.historictour;
 
 import android.app.TabActivity;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.widget.TabHost;
 
 public class Tabs extends TabActivity {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.tabs);
         
         if (GlobalVariables.destinationManager == null)
         {
             GlobalVariables.destinationManager = new DestinationManager(getResources());
         }
         Resources res = getResources();
         TabHost tabHost = getTabHost();
         TabHost.TabSpec spec;
         Intent intent;
         
         intent = new Intent(this, GeneralInfoActivity.class);
         spec = tabHost.newTabSpec("general").setIndicator("General", res.getDrawable(R.drawable.icon)).setContent(intent);
         tabHost.addTab(spec);
 
         // Add intent for DestinationsActivity
        intent = new Intent(this, DestinationActivity.class);
         spec = tabHost.newTabSpec("destinations").setIndicator("Destinations", res.getDrawable(R.drawable.icon)).setContent(intent);
         tabHost.addTab(spec);
         
         // Add intent for PostcardActivity
         intent = new Intent(this, PostcardActivity.class);
         spec = tabHost.newTabSpec("postcard").setIndicator("Postcard", res.getDrawable(R.drawable.icon)).setContent(intent);
         tabHost.addTab(spec);
         
         intent = new Intent(this, MapsActivity.class);
         spec = tabHost.newTabSpec("Map").setIndicator("Map", res.getDrawable(R.drawable.icon)).setContent(intent);
         tabHost.addTab(spec);
         
         tabHost.setCurrentTab(0);
 
     }
 }
