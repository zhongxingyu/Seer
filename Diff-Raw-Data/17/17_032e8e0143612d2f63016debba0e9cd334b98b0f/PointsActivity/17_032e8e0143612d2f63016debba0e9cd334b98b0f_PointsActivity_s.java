 package com.example.ohmytaxi;
 
 
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
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
 	 }
 	 
 	
 	public void onBackPressed(){
 		Intent intent = new Intent(getBaseContext().getApplicationContext(), MenuActivity.class);  
 		startActivity(intent);
 		finish();
 		super.onBackPressed();
 		//
 	}
 	
 
 
 
 }
