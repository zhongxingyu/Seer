 package com.advsofteng.app1;
 
 import java.util.ArrayList;

import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;


 import android.app.Activity;
 import android.content.Context;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class addPlace extends Activity {
 	
 	/* Endpoint to which HTTP POSTs should go */
 	private static final String ENDPOINT = "http://nearme.tomhume.org:8080/NearMeServer/AddPOIServlet";
 	
 	
 	private TextView latitudeField;
 	private TextView longitudeField;
 	private LocationManager locationManager;
 	private LocationListener locationListener;
 	private double lat;
 	private double lng;
 
 	private EditText name;
 
 	private Button Add = null;
 	private Button close_button = null;
     private String type;
 	
     
     
 	@Override
 	public void onCreate(Bundle savedInstanceState)  {
 		
 		super.onCreate(savedInstanceState); 
 		setContentView(R.layout.add_poi);
 		
 		/*fill in the spinner for type with the xml document */
 		Spinner spinner = (Spinner) findViewById(R.id.spinner1);   
 		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.Types, android.R.layout.simple_spinner_item); 
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); 
 		spinner.setAdapter(adapter);
 		
 		latitudeField = (TextView) findViewById(R.id.TextViewLAT); // textView for latitude
 	    longitudeField = (TextView) findViewById(R.id.TextViewLON); // textView for longitude 
 	    name = (EditText) findViewById(R.id.editText1);
 	    
 	    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);  
 	    locationListener = new MyLocationListener();
 		  
 		//calling the method and expect location updates multiple times
 		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,locationListener);
 	    
 		//call the function that get the value of EditText
 		spinner.setOnItemSelectedListener(new MyOnItemSelectedListener()); 
 		
 				
 		// Deal with the button add
 		Add = (Button) findViewById(R.id.button_add);
         Add.setOnClickListener(new View.OnClickListener() {
 			
 		public void onClick(View v) {
 		
 			name = (EditText) findViewById(R.id.editText1);
 				
 			  if (lat == 0 && lng ==0)
 			  {	  
 				  Toast.makeText( getApplicationContext(), "The Latitude and Longitude are unknown",Toast.LENGTH_SHORT).show();
 			  }
 			  else
 			  {	  
 				if (name.getText().toString().equals("")) // check that the name has been inserted
 				{
 					Toast.makeText( getApplicationContext(), "Please, Insert the name.",Toast.LENGTH_SHORT).show();
 				}
 				else
 				{
 					//***** send all the parameter to the addPOIservlet (lat, lng, name, type)
 						HttpClient client = new DefaultHttpClient();
 						HttpPost post = new HttpPost(ENDPOINT);
 					
 					/*	HttpPost post = new HttpPost(ENDPOINT + "/" + Double.toString(lat) 
 								+ "/" + Double.toString(lng)
 								+ "/" + name.getText().toString()
 								+ "/" + type);*/	
 					try
 					{
 																	
 						ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
						params = null;						
 						params.add(new BasicNameValuePair("lat", Double.toString(lat))); 
 						params.add(new BasicNameValuePair("lng", Double.toString(lng))); 
 						params.add(new BasicNameValuePair("name", name.getText().toString()));
 						params.add(new BasicNameValuePair("type", type));
 						
 						post.setEntity(new UrlEncodedFormEntity(params));
						HttpResponse response = client.execute(post);				
 	   					
 						Toast.makeText(getApplicationContext(), "New POI has been saved", Toast.LENGTH_LONG).show();
 				
 						finish(); // return to previous screen...
 						
 					}
 					catch (Exception e)
 					{
 						
 					}
 					
 					//Toast.makeText( getApplicationContext(), name.getText().toString() ,Toast.LENGTH_SHORT).show();
 					
 				 }
 			  } // end if lat and lng 
 			} // end of onClickView(View v)
 			}//  end of View.OnClickListener
         ); // end of setOnClickListener
 		
 		
         
             // Deal with the button close
      		close_button = (Button) findViewById(R.id.Close);
             close_button.setOnClickListener(new View.OnClickListener() {
      			
      		public void onClick(View v) {
      		
      			finish(); // return to previous screen...
      		}
      		});
         
         
         
 		
 	}	// end onCreate
 	
 	
 	
 	/*
 	 *  Class to deal with the spinner 
 	 */
 	public class MyOnItemSelectedListener implements OnItemSelectedListener { 
 		
 		public void onItemSelected(AdapterView<?> parent,View view, int pos, long id) 
 		{   
 			
 			type = parent.getItemAtPosition(pos).toString();
 			//Toast.makeText(parent.getContext(), "The type is " + 
 		    //parent.getItemAtPosition(pos).toString(), Toast.LENGTH_LONG).show();   
 			
 		}    
 		public void onNothingSelected(AdapterView<?> parent) {   
 			
 			// Do nothing
 		}
 	
 	}
 	
 	
 	/*
 	 * Implements class MyLocationListener
 	 */
 	private class MyLocationListener implements LocationListener 
 	{ 
 
 	  public void onLocationChanged(Location location) {
 	    if (location != null) {
 	        lat = (double) (location.getLatitude());  
 			lng = (double) (location.getLongitude()); 
 			latitudeField.setText(String.valueOf(lat)); //assign the value of latitude at text field
 			longitudeField.setText(String.valueOf(lng)); //assign the value of longitude at text field
 
 	    }//end if
 	  }//end void
 	  
 	  public void onProviderDisabled(String provider) 
 	  { 
 	  Toast.makeText( getApplicationContext(), "Gps Disabled",Toast.LENGTH_SHORT ).show(); 
 	  }  
 
 	  public void onProviderEnabled(String provider) 
 	  { 
 	  Toast.makeText( getApplicationContext(), "Gps Enabled",Toast.LENGTH_SHORT).show(); 
 	  } 
 
 	  public void onStatusChanged(String provider, int status, Bundle extras) 
 	  { 
 	  } 
 	  
 	}//end class
 	
 	
 } //end addPlace
