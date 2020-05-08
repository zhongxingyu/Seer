 package uk.co.andybarratt.chipcounter;
 
 import java.text.DecimalFormat;
 
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 public class CustomActivity extends Activity
 {
 	public static final String TAG = "ChipCounter";
 	public static final DecimalFormat dec = new DecimalFormat("##0.00");
 	
 	protected SharedPreferences settings;
 	protected Chip [] chips = new Chip [11];
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         
         
     }
     
     public boolean loadChipValues()
     {
     	boolean chipsSet = false;
     	//open settings Shared Preferences
    	settings = getSharedPreferences("ChipCounterValues", 0);
     	Log.d(TAG, "settings initialised");
     	
         for(int i=0; i<11; i++)
         {
         	//create unique names for chip data
         	String valueName = "value" + i;
         	String drawableName = "drawable" + i;
         	
         	//get data from settings object and read it, set defaults where values not available
         	float chipValue = settings.getFloat(valueName, 0);
         	int chipDrawable = settings.getInt(drawableName, 0);
         	
         	//put data into chips array.
         	chips[i] = new Chip(chipDrawable, chipValue);
         	if(chipValue!=0)
         		chipsSet=true;
         }
         
         return chipsSet;
     }
     
     /**
      * add menu.xml to menu
      */
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	Log.d(TAG,"Context Menu Opened.");
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.menu, menu);        
         return true;
     }
     
     /**
      * functionality for menu button menu
      */
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	Intent myIntent;
         switch(item.getItemId()) {
             case R.id.menuSetValues:
             	Log.d(TAG, "Set Chip Values menu button pressed.");
     			myIntent = new Intent(this, SetValuesActivity.class);
                 startActivityForResult(myIntent, 0);
                 break;
             case R.id.menuCardRankings:
             	Log.d(TAG, "Set Chip Values menu button pressed.");
     			myIntent = new Intent(this, Ranking.class);
                 startActivityForResult(myIntent, 0);
                 break;
             default:
         }
         return true;
     }
     
     public int convertToPixels(int dp)
     {
     	final float scale = this.getResources().getDisplayMetrics().density;
     	int pixels = (int) (dp * scale + 0.5f);
     	return pixels;
     }
     
     public void addLinkListener()
     {	
     	//add listener to web address button
     	final Button btnLink = (Button) findViewById(R.id.btnWebsite); //initialise set values button
     	btnLink.setOnClickListener(new OnClickListener() {
     		@Override
     		public void onClick(final View view) {
     			Log.d(TAG, "Website button pressed.");
     			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.andybarratt.co.uk/chip-counter-for-android"));
     			startActivity(browserIntent);
     		}
     	});
     }
 }
