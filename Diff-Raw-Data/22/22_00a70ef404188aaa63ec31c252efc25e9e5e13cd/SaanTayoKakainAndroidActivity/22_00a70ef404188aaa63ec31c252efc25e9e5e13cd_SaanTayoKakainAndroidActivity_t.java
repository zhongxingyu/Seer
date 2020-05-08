 package com.saantayokakainandroid;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
import android.widget.ArrayAdapter;
 import android.widget.Button;
import android.widget.Spinner;
 
 /**
  * 
  * @author mareksison
  * @version 0.0
  * @since January 11, 2012
  */
 public class SaanTayoKakainAndroidActivity extends Activity {
 	
	private Button filterB;
	private Spinner costS;
	private Spinner cuisineS;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
        setContentView(R.layout.filter);
     }
     
     public void initUI(){
     	
    	costS = (Spinner) findViewById(R.id.costS);
    	cuisineS = (Spinner) findViewById(R.id.costS);
    	
        ArrayAdapter<CharSequence> costSAdapter = ArrayAdapter.createFromResource(this, R.array.costArray, android.R.layout.simple_spinner_item);
        costSAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        costS.setAdapter(costSAdapter);
        ArrayAdapter<CharSequence> cuisineSAdapter = ArrayAdapter.createFromResource(this, R.array.cuisineArray, android.R.layout.simple_spinner_item);
        cuisineSAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cuisineS.setAdapter(cuisineSAdapter);

     }
 }
