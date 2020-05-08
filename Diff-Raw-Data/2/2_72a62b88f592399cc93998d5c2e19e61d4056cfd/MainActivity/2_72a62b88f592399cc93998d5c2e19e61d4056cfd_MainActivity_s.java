 package com.placella.bmi;
 
 import com.placella.bmi.R;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 
 public class MainActivity extends Activity {
 	private int imperialHeight;
 	private int imperialWeight;
 	private int metricHeight;
 	private int metricWeight;
 	private final int IMPERIAL = 0;
 	private final int METRIC = 1;
     private SharedPreferences prefs;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         prefs = getSharedPreferences("bmi_values", 0);
         imperialHeight = prefs.getInt("imperialHeight", 0);
        imperialWeight = prefs.getInt("view_mode", 0); 
         metricHeight = prefs.getInt("metricHeight", 0);
         metricWeight = prefs.getInt("metricWeight", 0);
         
         Button button = (Button) findViewById(R.id.choose_imperial);
         button.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             	handleClick(true, imperialHeight, imperialWeight, IMPERIAL);
             }
         });
         button = (Button) findViewById(R.id.choose_metric);
         button.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             	handleClick(false, metricHeight, metricWeight, METRIC);
             }
         });
         
     }
     
     private void handleClick(boolean imperial, int height, int weight, int requestCode) {
     	Bundle b = new Bundle();
     	b.putBoolean("imperial", imperial);
     	b.putInt("height", height);
     	b.putInt("weight", weight);
     	Intent intent = new Intent(this, DataInput.class);
     	intent.putExtras(b);
     	startActivityForResult(intent, requestCode);
     }
     
     protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
     	if (requestCode == IMPERIAL) {
     		imperialHeight = intent.getIntExtra("height", 0);
     		imperialWeight = intent.getIntExtra("weight", 0);
     	} else {
     		metricHeight = intent.getIntExtra("height", 0);
     		metricWeight = intent.getIntExtra("weight", 0);
     	}
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	super.onCreateOptionsMenu(menu);
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	MenuHandler.onOptionsItemSelected(item, this);
 		return super.onOptionsItemSelected(item);
     }
     
     protected void onPause() {
         super.onPause();
         SharedPreferences.Editor editor = prefs.edit();
         editor.putInt("imperialHeight", imperialHeight);
         editor.putInt("imperialWeight", imperialWeight);
         editor.putInt("metricHeight", metricHeight);
         editor.putInt("metricWeight", metricWeight);
         editor.commit();
     }
     
     @Override
     protected void onSaveInstanceState(Bundle outState) {
 	    super.onSaveInstanceState(outState);		
 	    outState.putInt("imperialHeight", imperialHeight);
 	    outState.putInt("imperialWeight", imperialWeight);
 	    outState.putInt("metricHeight", metricHeight);
 	    outState.putInt("metricWeight", metricWeight);
     }
     @Override
     public void onRestoreInstanceState(Bundle savedInstanceState) {
 	    super.onRestoreInstanceState(savedInstanceState);
 	    imperialHeight = savedInstanceState.getInt("imperialHeight");
 	    imperialWeight = savedInstanceState.getInt("imperialWeight");
 	    metricHeight = savedInstanceState.getInt("metricHeight");
 	    metricWeight = savedInstanceState.getInt("metricWeight");
     }
 }
