 package com.example.ohmytaxi;
 
 
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class PointsActivity extends Activity {
 	
 	private EditText etPointA;
 	private EditText etPointB;
 	private TextView tvPointA;
 	private TextView tvPointB;
 	private CheckBox checkMyPosition;
 	private Button   btSearch;
 	
 	
 	
 	
 	 protected void onCreate(Bundle savedInstanceState) {
 	     super.onCreate(savedInstanceState);
 	     setContentView(R.layout.activity_points);
	 	 etPointA = (EditText) findViewById(R.id.editPointA);
	 	 checkMyPosition = (CheckBox) findViewById(R.id.checkMyPosition);
	 	 checkMyPosition.setOnCheckedChangeListener(new OnCheckedChangeListener() {
	 		 @Override
	 	     public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
	 			 if (isChecked){
	 	        	etPointA.setEnabled(false);
	 	         }else{
	 	        	etPointA.setEnabled(true);
	 	         }
	 	     }
	 	 });  
 	 }
 	 
 	
 	public void onBackPressed(){
 		Intent intent = new Intent(getBaseContext().getApplicationContext(), MenuActivity.class);  
 		startActivity(intent);
 		finish();
 		super.onBackPressed();
 		//
 	}
 	
 
	
	
 
 
 }
