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
	 				LocationManager mylocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
	 				LocationListener mylocListener = new MyLocationListener();
	 				if (mylocManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
	 					mylocManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 0, 0, mylocListener);  //Acceso por GPS
	 				//	Location myPosition = mylocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		 			/*	double lati = myPosition.getLatitude();
		 				double longi = myPosition.getLongitude();
		 				etPointA.setText(String.valueOf(lati) + "  " + String.valueOf(longi));*/
	 				}
	 				else if(mylocManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
	 						mylocManager.requestLocationUpdates( LocationManager.NETWORK_PROVIDER, 0, 0, mylocListener); // Acceso por red
	 						Location myPosition = mylocManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	 		 				double lati = myPosition.getLatitude();
	 		 				double longi = myPosition.getLongitude();
	 		 				etPointA.setText(String.valueOf(lati) + "  " + String.valueOf(longi));
	 					 }
	 				else{
	 					etPointA.setText("No es posible localizar el dispositivo, comprueba la configuración de localización");				
	 				}
 	 			}else{
	 	        	etPointA.setText(null);
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
