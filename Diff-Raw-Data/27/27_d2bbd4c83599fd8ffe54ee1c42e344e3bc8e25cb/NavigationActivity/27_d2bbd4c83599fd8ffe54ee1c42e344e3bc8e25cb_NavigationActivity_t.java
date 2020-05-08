 package com.dask.pent;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.http.client.ClientProtocolException;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Point;
 import android.hardware.Sensor;
 import android.hardware.SensorManager;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import com.dask.pent.DistanceNotifier.Listener;
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 
 public class NavigationActivity extends MapActivity {
 	private CloudMade CMManager;
 	private Geocoding GCManager;
 	
 	private Compass compass;
 	public Handler handler;
 	
 	private Navigation navi;
 	
 	String[] Intent_start;
 	String[] Intent_dest;
 	CMJson cmjson;
 	
 	private int initCounter = 0;
 	private long refreshS = 0;
 	private long refreshE = 0;
 	private long delay = 1000;
 	private long DELAY_STREET_CROSSING = 7000;
 	private long DELAY_NORMAL = 5000;
 	
 	private int naviS = 0;
 	private int GPSBearing = 0;
 	private int magneticBearing = 0;
 	
 	double mDistance;
 	int[] mBearingM = new int[10];
 	int[] mBearingG = new int[3];
 	
 	int mStepCount = 0;
 	int mMagnetCount = 0;
 	int mGPSCount = 0;
 	
 	SensorManager mSensorManager;
 	Sensor mSensor;
 	StepDetector mStepDetector;
 	DistanceNotifier mDistanceNotifier;
 	
 	TextView tview;
 	private TTS mTts;
 	
 	MapView mapView;
 	MapController mc;
 	GeoPoint p;
 	Geocoding gc = new Geocoding();
 	String[] coordinate;
 	ArrayList<double[]> geo;
 	private List<Overlay> listOfOverlays;
	private MapLine mapline;
 
 	class MapOverlay extends com.google.android.maps.Overlay
     {
         @Override
         public boolean draw(Canvas canvas, MapView mapView, 
         boolean shadow, long when) 
         {
             super.draw(canvas, mapView, shadow);                   
  
             //---translate the GeoPoint to screen pixels---
             Point screenPts = new Point();
             mapView.getProjection().toPixels(p, screenPts);
  
             //---add the marker---
             Bitmap bmp = BitmapFactory.decodeResource(
                 getResources(), R.drawable.round_push_5);            
             canvas.drawBitmap(bmp, screenPts.x, screenPts.y, null);         
             return true;
         }
     } 
 	
 	class MapLine extends com.google.android.maps.Overlay
     {
         @Override
         public boolean draw(Canvas canvas, MapView mapView, 
         		boolean shadow, long when) 
         {
             super.draw(canvas, mapView, shadow);                   
  
             //---translate the GeoPoint to screen pixels---
             for(int i=0; i<geo.size()-1; i++) {
 	            Point screenPtsS = new Point();
 	            Point screenPtsE = new Point();
 	            GeoPoint gpS = new GeoPoint((int) (geo.get(i)[0]*1E6), (int) (geo.get(i)[1]*1E6));
 	            GeoPoint gpE = new GeoPoint((int) (geo.get(i+1)[0]*1E6), (int) (geo.get(i+1)[1]*1E6));
 	            mapView.getProjection().toPixels(gpS, screenPtsS);
 	            mapView.getProjection().toPixels(gpE, screenPtsE);
 	 
 	            //---add the marker---  
 	            canvas.drawLine(screenPtsS.x, screenPtsS.y, screenPtsE.x, screenPtsE.y, new Paint());
             }
             return true;
         }
     } 
 	
     /** Called when the activity is first created. */
     @SuppressWarnings("deprecation")
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         tview = (TextView) this.findViewById(R.id.mainText);
         
         tview.setText("Starting...");
         
         handler = new Handler(){
         	@Override
         	public void handleMessage(Message msg) {
         		super.handleMessage(msg);
         		switch(msg.arg1){
         		case 0:
         			mBearingM[mMagnetCount] = compass.getMagDir();
         			mMagnetCount++;
         			if(mMagnetCount == 10) {
         				mMagnetCount = 0;
         				magneticBearing = 0;
         				for(int i=0; i<mBearingM.length; i++)
         					magneticBearing += mBearingM[i];
         				magneticBearing = magneticBearing/mBearingM.length;
         				Log.i("handler","Notified that sensor changed, MAG: "+magneticBearing);
         				
         			}
         			break;
         		case 1:
         			mBearingG[mGPSCount] = compass.getGPSBearing();
         			mGPSCount++;
         			if(mGPSCount == 3) {
         				mGPSCount = 0;
         				GPSBearing = 0;
         				for(int i=0; i<mBearingG.length; i++)
         					GPSBearing += mBearingG[i];
         				GPSBearing = GPSBearing/mBearingG.length;
         				Log.i("handler","Notified that location changed, GPS: "+GPSBearing);
         			}
         			break;
         		}
         	}
 
         };        
         
         mapView = (MapView) findViewById(R.id.mapView);
         LinearLayout zoomLayout = (LinearLayout)findViewById(R.id.zoom);  
         View zoomView = mapView.getZoomControls(); 
  
         zoomLayout.addView(zoomView, 
             new LinearLayout.LayoutParams(
                 LayoutParams.WRAP_CONTENT, 
                 LayoutParams.WRAP_CONTENT)); 
         mapView.displayZoomControls(true);
         
      
         mc = mapView.getController();
         String coordinates[]={"40.82062" , "-73.95128"};
         double lat = Double.parseDouble(coordinates[0]);
         double lng = Double.parseDouble(coordinates[1]);
         
         // They need to be integer values, hence multiplication by million
         p=new GeoPoint (
         		(int) (lat * 1e6),
         		(int) (lng * 1E6));
         
         mc.animateTo(p);
         mc.setZoom(18);
         
         //---Add a location marker---
         MapOverlay mapOverlay = new MapOverlay();
         listOfOverlays = mapView.getOverlays();
         listOfOverlays.clear();
         listOfOverlays.add(mapOverlay);  
         
         mapView.invalidate();
         
         
         mTts = new TTS(this);
         
         GCManager = new Geocoding();
         try {
         	CMManager = new CloudMade(
         			"bf762d57ab76419f8d55e5ead01230fd", 
         			"krpent", 
         			"1234");
         } catch (ClientProtocolException e) { e.printStackTrace();
         } catch (IOException e) { e.printStackTrace(); }       
         
         mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
         LocationManager LM = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
         compass = new Compass(LM, mSensorManager, handler);
         
         mStepDetector = new StepDetector();
         registerDetector();        
         
         mDistanceNotifier = new DistanceNotifier(mDistanceListener);
         mDistanceNotifier.setDistance(mDistance = 0); 
         mStepDetector.addStepListener(mDistanceNotifier);
             
 //        debugger();
         LM.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,listen2GPS);
         
 //        String myText1 = "Did you sleep well?";
 //        String myText2 = "I hope so, because it's time to wake up.";
 //        mTts.speak(myText1);
 //        mTts.speak(myText2);
     }
     
     private void displayPoint(String[] coordinate2) {
     	double lat = Double.parseDouble(coordinate2[0]);
         double lng = Double.parseDouble(coordinate2[1]);
         
         // They need to be integer values, hence multiplication by million
         p=new GeoPoint (
         		(int) (lat * 1e6),
         		(int) (lng * 1E6));
         
         mc.animateTo(p);
         mc.setZoom(18);
         
         //---Add a location marker---
         MapOverlay mapOverlay = new MapOverlay();
         List<Overlay> listOfOverlays = mapView.getOverlays();
         listOfOverlays.clear();
        listOfOverlays.add(mapOverlay);
        mapline = new MapLine();
		listOfOverlays.add(mapline);
         
        // mapView.invalidate();
 		
 	}
 
     LocationListener listen2GPS = new LocationListener() {
 		public void onStatusChanged(String provider, int status, Bundle extras) {}
 		public void onProviderEnabled(String provider) {}
 		public void onProviderDisabled(String provider) {}
 		public void onLocationChanged(Location location) {
 			
     		locChanged(location);
     	}
 	};
     
 	public void locChanged(Location location) {
 		Log.d("listen2GPS", "Got Location w/" +
 				" mBearing: " + magneticBearing +
 				" mSteps: " + mStepCount);
 		
 		if(location != null) {
 			if(naviS == 0) {
 				if(initCounter < 10) {
 					initCounter++;
 					return;
 				}
 				try {
 					Log.d("listen2PGS", "Initializing Navigation");
 					naviS = 1;
 					
 					// Ideal 
 					Intent_start = new String[]{
 							"" + location.getLatitude(),
 							"" + location.getLongitude()};
 					
 					Intent_dest =  GCManager.geocodingGOOGLE(
							//"420-422 W 144th St Manhattan, NY 10031");
							"305 Convent Ave Manhattan, NY 10031");
 							//"279-281 Convent Ave Manhattan, NY 10031");
 							//"1518 Amsterdam Avenue, New York, NY");
 					
 					Log.d("listen2PGS", "Dest: "+Arrays.toString(Intent_dest));
 					
 					cmjson = CMManager.getDirections(
 							Intent_start, 
 							Intent_dest, 
 							"foot", "js", "Pent");
 					Log.d("listen2PGS", Arrays.deepToString(
 							cmjson.route_instructions.toArray()));
 					navi = new Navigation(cmjson, new double[] {
 							Double.parseDouble(Intent_dest[0]),
 							Double.parseDouble(Intent_dest[1])
 					});
 					
 					geo = cmjson.route_geometry;
					mapline = new MapLine();
	        		listOfOverlays.add(mapline);
 					
 					mTts.speak("Starting Navigation!");
 				} catch (IOException e) { e.printStackTrace(); }
 			}
 			else {
 				double[] pt = {
 						location.getLatitude(), 
 						location.getLongitude()};
 				String[] res = navi.Navigate(
 						pt, 
 						GPSBearing, 
 						mStepCount);
 				
 				Log.d("listen2PGS", Arrays.toString(res));
 				
 				// Get the current system time.
 				refreshE = System.currentTimeMillis();
 				
 				/* If there is not message to the user OR if the time since the
 				 * last update to the user is greater than the current delay
 				 * setting:
 				 */
				if(refreshS == 0 || (refreshE-refreshS) > delay || res[0].startsWith("0.1") || res[0].startsWith("0.0")) {
 					/* Update the screen with the current output of the 
 					 * navigation algorithm, for debugging processes.
 					 */
 					tview.setText(Arrays.toString(res));
 					/* Feed the current route instruction to the user through
 					 * means of TTS.
 					 */
 					mTts.speak(res[1]);
 					
 					refreshS = System.currentTimeMillis();
 					
 					/* If the user is at a street crossing or the user has now
 					 * moved onto a new path on the route:
 					 */
					if(res[0].startsWith("0.1"))
 						/* Keep the current instruction for the delay of street
 						 * crossings.
 						 */
 						delay = DELAY_STREET_CROSSING;
 					// Else the user is on a normal heading:
 					else
 						// Keep the current instruction for the normal delay.
 						delay = DELAY_NORMAL;
 				}
 				
 				// If the user has started on a new route:
 				if(res[0].startsWith("0.") == true) {
 					// Reset the pedometer values.
 					resetValues(null);
 					Log.d("listen2PGS", "Reseting values!");
 				}
 			}
			displayPoint(new String[]{
					Double.toString(location.getLatitude()), Double.toString(location.getLongitude())});
 		}
 		else
 			Log.d("listen2PGS", "Location is null.");
 	}
 	
     public void resetValues(View v) {
     	mStepCount = 0;
     	mDistanceNotifier.setDistance(mDistance = 0);
     }
     
     /**
      * Forwards distance values from DistanceNotifier to the activity. 
      */
     private Listener mDistanceListener = new Listener() {
         public void valueChanged(double value) {
             mDistance = value;
 //            tview.setText("Steps: "+(mStepCount++)+" \tDistance: "+mDistance+" meters.");
         }
     };
     
     private void registerDetector() {
         mSensor = mSensorManager.getDefaultSensor(
             Sensor.TYPE_ACCELEROMETER);
         mSensorManager.registerListener(mStepDetector,
             mSensor,
             SensorManager.SENSOR_DELAY_FASTEST);
     }
     
     @Override
     protected void onResume() {
     	super.onResume();
     	compass.startMagneticCompass();
     	compass.startGPSCompass();
     }
     
     @Override
     protected void onPause() {
     	super.onPause();
     	compass.stopMagneticCompass();
     	compass.stopGPSCompass();
     }
     
     public boolean onCreateOptionsMenu(Menu menu) {
 	    super.onCreateOptionsMenu(menu);
 	    MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.menu, menu);
 	    
 	    return true;
     }
 
     public void onDestroy(){
     	mTts.tts.shutdown();
     	super.onDestroy();
     }
     
     public boolean onOptionsItemSelected(MenuItem item) {
 		if(item.getItemId() == R.id.exit_title) {
 			finish();
 			System.exit(0);
 			
 			return true;
 		}
 		return false;
     }
     
     public void debugger() {
 //    	double[][] pts = {
 //    			{40.821312,-73.94787, 285,   0},	// Front of ST
 //    			{40.821312,-73.94787, 285,   0},	// Front of ST
 //    			{40.821405,-73.948181, 285, 45},	// Middle of ST
 //    			{40.821519,-73.948427, 285, 90},	// N.Corner of ST
 //    			{40.821588,-73.948615, 285,  0}, 	// S.Corner of W140
 //    			{40.821848,-73.949237, 285, 90},	// Middle of W140
 //    			{40.822111,-73.949806, 210, 180},	// N.Corner of W140
 //    			{40.822061,-73.950015, 210,  0},	// S.Corner of W139
 //    			{40.821807,-73.950184, 210, 40},	// Middle of W139
 //    			{40.821535,-73.950396, 210, 80},	// N.Corner of W139
 //    			{40.821407,-73.950498, 210,  0},	// S.Corner of W138
 //    			{40.821174,-73.95063, 210,  40},	// Middle of W138
 //    			{40.820932,-73.950841, 210, 80},	// N.Corner of W138
 //    			{40.820808,-73.950933, 210,  0},	// S.Corner of W137
 //    			{40.820451,-73.951161, 210, 40},	// Middle of W137
 //    			{40.819661,-73.951769, 210, 80},	// N.Corner of W137
 //    			{40.819556,-73.951837, 210,  0},	// S.Corner of W136
 //    			{40.819268,-73.952035, 210, 40},	// Middle of W136
 //    			{40.819012,-73.95226, 210,  80},	// N.Corner of W136
 //    			{40.818931,-73.95229, 210,   0},	// S.Corner of W135
 //    			{40.818827,-73.952365, 210, 20}		// Subway
 //    	};
     	
     	double[][] pts = {
     			{40.82140047064303, -73.94782474966814, 225, 0},	// F: ST 
     			{40.821274625142884, -73.94795617790987, 285, 0},	// S: ST
     			{40.82138220276265, -73.9481948945122, 285, 30},	// M: ST 
     			{40.82149992901453, -73.9484470221596, 285, 60}, 	// N: ST
     			{40.82154255398522, -73.9485543105202, 285, 0}, 	// S: Conv
     			{40.82183483876072, -73.94921949835589, 285, 60}, 	// M: Conv
     			{40.82208043816645, -73.94982031317522, 285, 120}, 	// N: Conv
     			{40.822157568370706, -73.94996783467104, 285, 0},	// S: Amst 
     			{40.82242143418061, -73.9506276580887, 285, 60},	// M: Amst
     			{40.82266906113967, -73.9512016508179, 285, 120},	// N: Amst 
     			{40.822758368996425, -73.9513652655678, 285, 0}, 	// S: Ham
     			{40.82300193526726, -73.95199022026827, 285, 60}, 
     			{40.82325158976658, -73.95255884857943, 285, 120}, 
     			{40.82333074831405, -73.95271978112032, 285, 0}, 
     			{40.823413966172325, -73.95291021796038, 15, 0}, 
     			{40.82366970820524, -73.95270905228426, 15, 30}
     	};
     	
     	Location Loc;
     	for(int i=0; i<pts.length; i++) {
     		Log.d("Debugger", "=============================================");
     		Loc = new Location("");
     		Loc.setLatitude(pts[i][0]);
     		Loc.setLongitude(pts[i][1]);
     		
     		magneticBearing = (int) pts[i][2];
     		mStepCount = (int) pts[i][3];
     		
     		locChanged(Loc);
     	}
     }
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 }
