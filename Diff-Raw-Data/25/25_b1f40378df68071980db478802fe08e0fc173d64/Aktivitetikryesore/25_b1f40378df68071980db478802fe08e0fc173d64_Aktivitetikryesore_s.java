 package assignments.ismeti;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.annotation.SuppressLint;
 import android.location.Location;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 import com.example.gpsexample.R;
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
 import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
 import com.google.android.gms.location.LocationClient;
 import com.google.android.gms.location.LocationRequest;
 import com.google.android.gms.maps.CameraUpdate;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.PolylineOptions;
 
 public class Aktivitetikryesore extends FragmentActivity
 implements OnMyLocationButtonClickListener,
 		   ConnectionCallbacks,
 		   OnConnectionFailedListener, 
 		   com.google.android.gms.location.LocationListener{
 
 		private GoogleMap mMap;
 		private LocationClient mLocationClient;
 		private ArrayList < LatLng > LatLnglist = new ArrayList< LatLng >();
 		private float distance;
 		private Timer timer;
 		private int time;
 		private int k;
 		private TextView tdistance;
 		private TextView ttime;
 		private TextView tspeed;
 		private Button btn;
 
 	
 	
 	
 		private static final LocationRequest REQUEST = LocationRequest.create()
             .setInterval(5000)         
             .setFastestInterval(16)   
             .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
 	
 	
 		@Override
 		protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 			setContentView(R.layout.activity_aktivitetikryesore);
 			setUpMapIfNeeded();
 			setUpLocationClientIfNeeded();
 			ttime=(TextView)findViewById(R.id.txtTimeRes);
 			tspeed=(TextView)findViewById(R.id.txtSpeedRes);
 			tdistance=(TextView)findViewById(R.id.txtDistanceRes);
 			btn=(Button)findViewById(R.id.btnStartStop);
 			distance=0;
 			time=0;	
 	}
 	@SuppressLint("NewApi")
 	@Override   
 		protected void onResume() {
         super.onResume();
         	setUpMapIfNeeded();
         	setUpLocationClientIfNeeded();
         	mLocationClient.connect();    
         	k=getPreferences(MODE_PRIVATE).getInt("K",0);
         	if(k==1)
         	{
         		starttimer();
         		btn.setText("Stop");
         		btn.setBackground(getResources().getDrawable(R.drawable.btnstop));
         		btn.invalidate();
         	}
         	loadArray();
         	mMap.addPolyline(new PolylineOptions()
         	.addAll(LatLnglist)
         	.width(5)
         	.color(-16776961));
         	handleTime();
         	showspd();
     }
 	 @Override
 	 	public void onPause() {
 	        super.onPause();
 	        if (mLocationClient != null) {
 	            mLocationClient.disconnect();
 	        }
 	        if(k==1)
 	        {
 	         timer.cancel();
 	        }	
 	      saveArray();
 	    }
 	 @Override
 	 	public void onDestroy() {
 		 	super.onDestroy();
 		 	if(k==1)
 		 	{
 		 		timer.cancel();
 		 	}
 		 	saveArray();
 	 	}
 	@Override
 		public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 		private void setUpMapIfNeeded() {
 	        // Do a null check to confirm that we have not already instantiated the map.
 	        if (mMap == null) {
 	            // Try to obtain the map from the SupportMapFragment.
 	            mMap = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
 	            // Check if we were successful in obtaining the map.
 	            if (mMap != null) {
 	                mMap.setMyLocationEnabled(true);
 	                mMap.setOnMyLocationButtonClickListener(this);
 	            }
 	        }
 	    }
 	    public boolean onMyLocationButtonClick() {
 	        return false;
 	    }
 	    @Override
 	    public void onLocationChanged(Location loc) {
 	    	
 	    	LatLng latLng = new LatLng(loc.getLatitude(),loc.getLongitude());
 	    	CameraUpdate camU = CameraUpdateFactory.newLatLngZoom(latLng,15);
 	    	mMap.animateCamera(camU);
 	    	try
 	    	{
 	    	if(k==1)
 	    	{
 	    		LatLnglist.add(latLng);
 	    		calculatedistance();
 	    		showspd();
 	    		
 	    		if(!LatLnglist.isEmpty())
 	    		{
 
 	    		mMap.addPolyline(new PolylineOptions()
 	            .addAll(LatLnglist)
 	            .width(5)
 	            .color(-16776961));
 	    		}
 	    		
 	    	}
 	    	}
 	    	catch(Exception ex)
 	    	{
 	    		
 	    	}
 	    }
 		@Override
 		public void onConnectionFailed(ConnectionResult result) {
 			// TODO Auto-generated method stub
 			
 		}
 		@Override
 		public void onDisconnected() {
 			// TODO Auto-generated method stub
 			
 		}
 		@Override
 		public void onConnected(Bundle connectionHint) {
 			mLocationClient.requestLocationUpdates(REQUEST,this);  
 		}
 	    private void setUpLocationClientIfNeeded() {
 	        if (mLocationClient == null) { mLocationClient = new LocationClient(getApplicationContext(),
 	                    this,  // ConnectionCallbacks
 	                    this); // OnConnectionFailedListener
 	        }
 	    }
 	    @SuppressLint("NewApi")
	    //min API req. 16
 		public void buttonClick(View v)
 	    {
 	    	if(k==1)
 	    	{
 	    		k=0;
 	    		timer.cancel();
 	    		btn.setText("Start");
 	    		btn.setBackground(getResources().getDrawable(R.drawable.btnstart));
         		btn.invalidate();
 	    	}
 	    	else 
 	    	{
 	    		starttimer();
 	    		LatLnglist.clear();
 	    		time=0;
 				distance=0;
 				k=1;
 				LatLnglist.clear();
 				mMap.clear();
 				btn.setText("Stop");
 				btn.setBackground(getResources().getDrawable(R.drawable.btnstop));
         		btn.invalidate();
 	    	}
 
 	    	
 	    }
 	    private void showspd()
 	    {
 	    	
 	    	String strdistance= new DecimalFormat("#.##").format((double)(distance)).toString();
 	    	strdistance+=" m";
     		String speed = "0 Km/h";
     		if(time!=0)
     		{
     		speed= new DecimalFormat("#.##").format((double)(0.27*distance/time)).toString();
     		speed +=" Km/h";
     		}
     		tdistance.setText(strdistance);
     		tspeed.setText(speed);
 	    }
 	    private void handleTime()
 		{
 			if(k==1)
 			{
 			time +=1;
 			}
 			String result="";
			result=Integer.toString(time/3600)+" Hour(s) "+Integer.toString(time/60)+" Minutes(s) "+ Integer.toString(time%60)+" Second(s)";
 			if(time>3600*24)
 			{
 				time=0;
 			}
 			ttime.setText(result);
 			
 			
 		}
 	    private void starttimer()
 	    {
 	    	timer= new Timer();
 			timer.scheduleAtFixedRate(new TimerTask()
 			{
 
 				public void run() {
 					runOnUiThread(new Runnable() {
 
 					    @Override
 					    public void run() {
 					        handleTime();
 					    }
 					     
 					});
 				}
 				
 			},0,1000);
 	    }
 	    private void saveArray()
 	    {
 	        
 	        getPreferences(MODE_PRIVATE).edit().putInt("Size", LatLnglist.size()).commit();
 	        getPreferences(MODE_PRIVATE).edit().putFloat("Distance", distance).commit();
 	        getPreferences(MODE_PRIVATE).edit().putInt("K", k).commit();
 	        getPreferences(MODE_PRIVATE).edit().putInt("Time", time).commit();
 	        for(int i=0;i<LatLnglist.size();i++)  
 	        {
 	        	getPreferences(MODE_PRIVATE).edit().remove("Cord_Lat_" + i).commit();
 	        	getPreferences(MODE_PRIVATE).edit().remove("Cord_Long_" + i).commit();
 	        	getPreferences(MODE_PRIVATE).edit().putFloat("Cord_Lat_" + i, (float)LatLnglist.get(i).latitude).commit();  
 	        	getPreferences(MODE_PRIVATE).edit().putFloat("Cord_Long_" + i, (float)LatLnglist.get(i).longitude).commit(); 
 	        }
 	        
 	        
 	    }
 	    private void loadArray()
 	    {  
 	        
 	        LatLnglist.clear();
 	        int size = getPreferences(MODE_PRIVATE).getInt("Size",0); 
 	        distance=  getPreferences(MODE_PRIVATE).getFloat("Distance",0);
 	        k=getPreferences(MODE_PRIVATE).getInt("K",0);
 	        time=getPreferences(MODE_PRIVATE).getInt("Time",0);
 
 	        for(int i=0;i<size;i++) 
 	        {
 	        	double lat=(double)getPreferences(MODE_PRIVATE).getFloat("Cord_Lat_" + i,(float) 5.0);
 	        	double lng=(double)getPreferences(MODE_PRIVATE).getFloat("Cord_Long_" + i,(float) 5.0);
 	            LatLnglist.add(new LatLng(lat, lng));
 	        }
 	    }
 	    private void calculatedistance()
 	    {
 	    	int size=LatLnglist.size();
 	    	if(size>1)
 	    	{
 	        distance += Distance(LatLnglist.get(size-1).latitude, LatLnglist.get(size-1).longitude, LatLnglist.get(size-2).latitude, LatLnglist.get(size-2).longitude);
 	    	}	
 	    }
 	    private float Distance( double nLat1, double nLon1, double nLat2, double nLon2 )
 		{
 	    	//Taken From Jaimerios.com it uses haversine formula to calculate distance.
 		    double nRadius = 6371; // Earth's radius in Kilometers
 		    
 		    // Get the difference between our two points
 		    // then convert the difference into radians
 		    double nDLat = Math.toRadians(nLat2 - nLat1);
 		    double nDLon = Math.toRadians(nLon2 - nLon1);
 
 		    // Here is the new line
 		    nLat1 =  Math.toRadians(nLat1);
 		    nLat2 =  Math.toRadians(nLat2);
 
 		    double nA = Math.pow ( Math.sin(nDLat/2), 2 ) + Math.cos(nLat1) * Math.cos(nLat2) * Math.pow ( Math.sin(nDLon/2), 2 );
 		    double nC = 2 * Math.atan2( Math.sqrt(nA), Math.sqrt( 1 - nA ));
 		    float nD = (float) (nRadius * nC);
 		    return nD; // Return our calculated distance
 		}
 }
 
