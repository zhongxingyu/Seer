 package edu.msu.cse.trivialpursuit.bigdraw;
 
 import java.util.List;
 
import android.graphics.Point;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.location.Address;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.util.DisplayMetrics;
import android.view.Display;
 import android.view.Menu;
 import android.widget.SeekBar;
 
 public class MainActivity extends Activity {
 	
 	private LocationManager locationManager = null;
     
     private double origLatitude = 0;
     private double origLongitude = 0;
     private boolean valid = false;
     private double currLatitude = 0;
     private double currLongitude = 0;
     private static double Xpixel = 0;
     private static double Ypixel=0;
 
     
     public static double getXpixel() {
 		return Xpixel;
 	}
 
 	public void setXpixel(double xpixel) {
 		Xpixel = xpixel;
 	}
 
 	public static double getYpixel() {
 		return Ypixel;
 	}
 
 	public void setYpixel(double ypixel) {
 		Ypixel = ypixel;
 	}
 
 	private SharedPreferences settings = null;
     
     public static final String PREFERENCES = "prefs";
     
     boolean exception = false;
     List<Address> locations;
     
     private ActiveListener activeListener = new ActiveListener();
 
 	
 	/**
 	 * The drawing view in this activity's view
 	 */
 	private DrawView drawView;
 	
 		
 	/**
 	 * The draw toggle
 	 */
 	//private ToggleButton drawToggle; 
 	
 	private class AccelListener implements SensorEventListener {
 
 		@Override
 		public void onAccuracyChanged(Sensor sensor, int accuracy) {
 		}
 
 		@Override
 		public void onSensorChanged(SensorEvent event) {
 			float thickness = (float) (event.values[2]-.1)*10;
 			
 			if (thickness < .1) {
 				drawView.setEditable(false);
 			} else {
 				drawView.setEditable(true);
 				drawView.setThickness(thickness);
 			}
 		}
 		
 	}
 	
 	SensorManager sensorManager = null;
 	private Sensor accelSensor = null;
 	private AccelListener accelListener = null;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		drawView = (DrawView) this.findViewById(R.id.drawView);
 		
 			
 		// Get the location manager
         locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);        
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 	
 	/*public void onDrawToggle(View view) {
 		drawView.setEditable(!drawToggle.isChecked());
 	}*/
 	
 	
 	private class ActiveListener implements LocationListener {
 
 		@Override
 		public void onLocationChanged(Location location) {
 			onLocation(location);
 			
 		}
 
 		@Override
 		public void onProviderDisabled(String provider) {
 			registerListeners();			
 		}
 
 		@Override
 		public void onProviderEnabled(String provider) {
 			// TODO Auto-generated method stub
 		}
 
 		@Override
 		public void onStatusChanged(String provider, int status, Bundle extras) {
 			// TODO Auto-generated method stub
 		}
 	};
 
 
 	private void registerListeners() {
 		unregisterListeners();
 		
 		// Create a Criteria object
         Criteria criteria = new Criteria();
         criteria.setAccuracy(Criteria.ACCURACY_FINE);
         criteria.setPowerRequirement(Criteria.POWER_HIGH);
         criteria.setAltitudeRequired(true);
         criteria.setBearingRequired(false);
         criteria.setSpeedRequired(false);
         criteria.setCostAllowed(false);
         
         String bestAvailable = locationManager.getBestProvider(criteria, true);
         
         if(bestAvailable != null) {
             locationManager.requestLocationUpdates(bestAvailable, 500, 1, activeListener);
             Location location = locationManager.getLastKnownLocation(bestAvailable);
             onLocation(location);
         }
 		    
 	}
 	
 	private void unregisterListeners() {
 		locationManager.removeUpdates(activeListener);
 	}
 	
 	private void onLocation(Location location) {
         if(location == null) {
             return;
         }
         
         if (currLatitude == 0) {
             currLatitude = location.getLatitude();
             currLongitude = location.getLongitude();
             return;
         }        
         
         currLatitude = location.getLatitude();
         currLongitude = location.getLongitude();
         
        //origLatitude = currLatitude;
        //origLongitude = currLongitude;
         
         System.out.println("origLatitude "+origLatitude);
         System.out.println("origLongitude "+origLongitude);
         
         valid = true;
         
         /*float[] results = new float[1];
 		 Location.distanceBetween(origLatitude, origLongitude, currLatitude, currLongitude, results);
 		 float distance = results[0];
		 System.out.println("distance "+distance);*/
			 
		 Display display = getWindowManager().getDefaultDisplay();
		 Point size = new Point();
		 display.getSize(size);
		 int width = size.x;
		 int height = size.y;
 		 
 		 
 		/* if (startx ==0 || starty ==0) {
 			 startx = (float) origLongitude;
 			 starty = (float) origLatitude;
 		 }
 		 
 		 changex = (float) (currLongitude - startx);
 		 changey = (float) (currLatitude - starty);
 		 
 		 if (changex <0){
 			 changex = changex*10;
 		 }
 		 if (changex >10){
 			 changex = changex/10;
 		 }
 		 if (changey <0){
 			 changey = changey*10;
 		 }
 		 if (changey >10){
 			 changey = changey/10;
 		 }
 		 
 		 changex = changex*100000;
 		 changey = changey*100000;
 		 drawx += changex;
 		 drawy += changey;
 		 
 		 System.out.println("startx"+startx);
 		 System.out.println("changex"+changex);
 		 System.out.println("changey"+changey);*/
 		 
 		 	double A =  42.724987, B=42.724228, X=-84.481935, Y=-84.480143;
  			double Z= Y-X;
  			double C= A-B;
  			double J= currLongitude, K=currLatitude;
  			Xpixel = ((Y-J)/Z)*width;
  			Ypixel = ((A-K)/C)*height;
  			//double Xpixel = ((180+J)*width/360), Ypixel = ((90-K)*height/180);
 			if (origLatitude != currLatitude){
  			drawView.updateDrawing((float)Xpixel, (float)Ypixel);
 			}
  			
  			 System.out.println(Xpixel);
  			 System.out.println(Ypixel);
 		 
 		 	/*currLatitude = currLatitude * Math.PI / 180;
 		    currLongitude = currLongitude * Math.PI / 180;
 
 		    double x = 6371 * Math.sin(currLatitude) * Math.cos(currLongitude);
 		    double y = 6371 * Math.sin(currLatitude) * Math.sin(currLongitude);
 		    
 		    System.out.println(x);
 			System.out.println(y);*/
  			  		 			
  		
  		//viewDistance.setText(String.format("%1$6.1fm", distance));
 
         //Set these values to draw as they change***********
         //by updating the draw interface
     }
 
 	
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
 		accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
 		if(accelSensor != null) {
 			accelListener = new AccelListener();
 			sensorManager.registerListener(accelListener, accelSensor, SensorManager.SENSOR_DELAY_GAME);
 		}
 		registerListeners();
 	}
 
 	@Override
 	protected void onPause() {
 		// TODO Auto-generated method stub
 		unregisterListeners();
 		if(accelSensor != null) {
 			sensorManager.unregisterListener(accelListener);
 			sensorManager = null;
 			accelListener = null;
 			accelSensor = null;
 		}
 		super.onPause();
 	}
 	
 
 }
