 package com.example.ohmytaxi.activities;
 
 
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Locale;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Address;
 import android.location.Criteria;
 import android.location.Geocoder;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.provider.Settings;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.example.ohmytaxi.R;
 import com.example.ohmytaxi.location.MyLocationListener;
 
 public class PointsActivity extends Activity {
 	
 	private EditText etPointA;
 	private EditText etPointB;
 	private CheckBox checkMyPosition;
 	private Button btSearch;
 	private double sourceLatitude;
 	private double sourceLongitude;
 	private double destinationLatitude;
 	private double destinationLongitude;
 	
 	
 	
 	
 	
 	protected void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    setContentView(R.layout.activity_points);
 	 	etPointA = (EditText) findViewById(R.id.editPointA);
 	 	etPointB = (EditText) findViewById (R.id.editPointB);
 	 	btSearch = (Button) findViewById(R.id.buttonSearch);
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
 	 					Location myPosition = mylocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 		 				double lati = myPosition.getLatitude();
 		 				double longi = myPosition.getLongitude();
 		 				etPointA.setText(String.valueOf(lati) + "  " + String.valueOf(longi));
 	 				}
 	 				else if(mylocManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
 	 						mylocManager.requestLocationUpdates( LocationManager.NETWORK_PROVIDER, 0, 0, mylocListener); // Acceso por red
 	 						Location myPosition = mylocManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 	 		 				sourceLatitude = myPosition.getLatitude();
 	 		 				sourceLongitude = myPosition.getLongitude();
 	 		 				etPointA.setText(getAddressFromLocation(sourceLatitude,sourceLongitude));
 	 					 }
 	 				else{
 	 					etPointA.setText("No es posible localizar el dispositivo, comprueba la configuracin de localizacin");				
 	 				}
 	 			}else{
 	 	        	etPointA.setText(null);
 	 				etPointA.setEnabled(true);
 	 	        	
 	 	        }
 	 	    }
 	 	});
 	 	
 	 	///////////////////////////////////////////
 	 	
 	/* 	LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
 	 	String provider = locationManager.getBestProvider(new Criteria(), true);
 
 	 	Location locations = locationManager.getLastKnownLocation(provider);
 	 	List<String>  providerList = locationManager.getAllProviders();
 	 	if(null!=locations && null!=providerList && providerList.size()>0){                 
 	 	double longitude = locations.getLongitude();
 	 	double latitude = locations.getLatitude();
 	 	Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());                 
 	 	try {
 	 	    List<Address> listAddresses = geocoder.getFromLocation(latitude, longitude, 1);
 	 	    if(null!=listAddresses&&listAddresses.size()>0){
 	 	        String _Location = listAddresses.get(0).getAddressLine(1);
 	 	        etPointA.setText(_Location);
 	 	    }
 	 	} catch (IOException e) {
 	 	    e.printStackTrace();
 	 	}
 
 	 	}*/
 	 	/////////////////////////////////////////////////
 
 
 	 	
 	 	btSearch.setOnClickListener(new OnClickListener() {
         	public void onClick(View view){
         		if ((etPointA.getText().length() == 0) && (etPointB.getText().length() != 0)){
         			Toast warningMessage = Toast.makeText(getApplicationContext(),
		                    "Introduce una direccin de origen", Toast.LENGTH_SHORT);
         			warningMessage.show();
         		}else if ((etPointB.getText().length() == 0) && (etPointA.getText().length() != 0)){
         			Toast warningMessage = Toast.makeText(getApplicationContext(),
		                    "Introduce una direccin de destino", Toast.LENGTH_SHORT);
         			warningMessage.show();  			        
         		}else if((etPointA.getText().length() == 0) && (etPointB.getText().length() == 0)){ 
         			Toast warningMessage = Toast.makeText(getApplicationContext(),
		                    "Introduce el origen y el destino", Toast.LENGTH_SHORT);
         			warningMessage.show();  			        
         		}else{
         			showMapScreen();
         		}
         	}
         });
 	 	
 	 	
 	 	
 	 
 	}
 	 
 	
 	public void onBackPressed(){
 		Intent intent = new Intent(getBaseContext().getApplicationContext(), MenuActivity.class);  
 		startActivity(intent);
 		finish();
 		super.onBackPressed();
 	}
 
 	
 	
 	
 	
 	private void getLocationFromAddress (String strAddress, boolean origin){
 	   	 
     	Geocoder coder = new Geocoder(this);
     	List<Address> address;
 
     	try {
     	    address = coder.getFromLocationName(strAddress,5);
    	    if (address.get(0) == null) {
    	    	Log.i("Vale", "NULL");
    	    }    	    
     	    Address location = address.get(0);
     	    if (origin){
     	    	sourceLatitude = (float) location.getLatitude();
     	        sourceLongitude = (float) location.getLongitude();
     	    }else{
     	    	destinationLatitude = (float) location.getLatitude();
     	    	destinationLongitude = (float) location.getLongitude();    	    	
     	    }
     	} catch (IOException e) {
     			
     	}
     }
 
 	
 	
 	public String getAddressFromLocation(double sourceLatitude2, double sourceLongitude2){
 		Geocoder geocoder;
 		List<Address> addresses = null;
 		geocoder = new Geocoder(this, Locale.getDefault());
 		try {
 			addresses = geocoder.getFromLocation(sourceLatitude2, sourceLongitude2, 1);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		String address = addresses.get(0).getAddressLine(0);
 		String city = addresses.get(0).getAddressLine(1);
 		//String country = addresses.get(0).getAddressLine(2);
 		return address + " " + city;		
 	}
 	
 	
 	
 	
 	public void showMapScreen() {    // mtodo que llama a la activity que muestra el mapa con nuestra ruta deseada
 		 Intent i = new Intent(this, RouteActivity.class);  
 		 Bundle b = new Bundle ();
 		 if (!checkMyPosition.isChecked()){
 			 getLocationFromAddress(etPointA.getText()+"",true);
 		 }
 		 getLocationFromAddress(etPointB.getText()+"",false);
 		 b.putFloat("source lat", (float) sourceLatitude);
 		 b.putFloat("source lon", (float) sourceLongitude); 
 		 b.putFloat("destination lat", (float) destinationLatitude);
 		 b.putFloat("destination lon", (float) destinationLongitude); 
 		 Log.i("desti! LAT", String.valueOf(destinationLatitude));
 		 Log.i("desti! LON", String.valueOf(destinationLongitude));
 		 i.putExtras(b);
 		 startActivity(i);
 		 finish();	
 	}
 	
 	 
 	 
 	 	
 	
 
 
 }
