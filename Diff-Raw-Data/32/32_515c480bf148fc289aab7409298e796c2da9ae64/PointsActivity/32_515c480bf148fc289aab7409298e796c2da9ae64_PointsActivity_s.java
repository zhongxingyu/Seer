 package com.example.ohmytaxi;
 
 
 
 import com.example.location.MyLocationListener;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
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
	 				LocationManager milocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
	 				LocationListener milocListener = new MyLocationListener();
	 				//milocManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 0, 0, milocListener);  //Acceso por GPS
	 				milocManager.requestLocationUpdates( LocationManager.NETWORK_PROVIDER, 0, 0, milocListener); // Acceso por red

	 				

	 				
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
