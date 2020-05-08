 package com.example.co2emissionalert;
 
 //import java.math.BigDecimal;
 //import java.text.DecimalFormat;
 import java.io.Serializable;
 import java.util.List;
 import java.util.Locale;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 import com.google.android.maps.Overlay;
 //import com.google.android.maps.OverlayItem;
 //import com.google.android.maps.Projection;
 
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.provider.Settings;
 //import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.graphics.Point;
 import android.speech.tts.TextToSpeech;
 import android.speech.tts.TextToSpeech.OnInitListener;
 import android.util.Log;
 //import android.view.Gravity;
 import android.view.Menu;
 //import android.view.MenuInflater;
 import android.view.MenuItem;
 //import android.view.View;
 //import android.view.View.OnClickListener;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.os.Handler;
 //import android.os.SystemClock;
 
 public class MapTracking extends MapActivity implements LocationListener, OnInitListener{
 
 	private final int CO2_THRESHOLD = 5000;	// CO2 emission threshold for active alert
 	private final int DESTMARKER = R.drawable.ic_cross;	// marker icon for destination spot
 	private final int MY_DATA_CHECK_CODE = 0;
 			
 	private MapView mapView;
 	private TextView reading;
 	private MapController mapController;
 	private MyLocationOverlay myLocationLay;
 	private LocationManager locationManager;
 	//private LocationListener locListener;
 	//private Projection pro;
 	private Vibrator vib;
 	private List<Overlay> overlays;
 	//private Toast toast;
 	private ShakeListener shakeListener;
 	private TextToSpeech TTS;
 	public static Location seedLocation;
 	
 	private boolean isFirstLocation = true;	// Flag is true if current point is the first location in tracking
 	private boolean isInitial = false;	// Flag is true if to initial mylocation
 	private boolean isGPSOn = true;
 	private boolean timerFlag = true;	// true if timer not blocked (block: not updating)
 	private int thresBlock = 0;		// active alert is blocked when thresBlock>1: alert for only one time over CO2_THRESHOLD
 	
 	
 	//private float MM;	// Coefficient M from Setting Activity
 	private int COLOR = Color.RED;
 	private int icon = 0;
 	private double SumCO2 = 0;
 	private double SumDistance = 0;
 	private long SumTime = 0;
 	public static long StartTime = 0;
 	private long millis = 0L;	//timer reading
 	public static int nMode = 1;	// mode accumulator
 	public static String sMode = " transport mode(s):";
     private LocEntry currentLocEntry;
 	private LocEntry lastLocEntry;
 	private DBHandler db;
     private Handler mHandler; 
     
     //private Thread t;
     //private volatile boolean flag= true;	// Flag for thread status
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Log.d("map", "oncreate start");	// DEBUG log message
         setContentView(R.layout.activity_maptracking);
         ExitActivity.isApplicationTerminated = false;
         reading = (TextView)findViewById(R.id.readingId);
         mHandler = new Handler();
         db = new DBHandler(this);	//an instance of database handler
        
         currentLocEntry = new LocEntry();
         lastLocEntry = new LocEntry();
         
         Runnable mUpdateTimeTask = new Runnable(){
             private int sec, min, hour;
             private double x, y;
             private String s = "Initializing tracking process ...";
         	
         	@Override
         	public void run() {
         		if (currentLocEntry.getLocalTime() == 0) {
 					s = "Initializing tracking process ...";
 				}else{
 					if(timerFlag){
 						millis = System.currentTimeMillis();
 						millis = millis - StartTime;
 					}
 				
 					sec = (int) (millis / 1000);
 					min = sec / 60; sec %= 60;
 					hour = min / 60; min %= 60;
 				
 					x = currentLocEntry.getLoca().getLatitude();
 					y = currentLocEntry.getLoca().getLongitude();
 					s = "My location: (" + Location.convert(x, Location.FORMAT_SECONDS) + ", " 
 	    					+ Location.convert(y, Location.FORMAT_SECONDS) + ")\n" + "Distance: " 
 	    					+ String.valueOf(SumDistance) + " m\n" + "Duration: " + String.valueOf(hour) + ":" 
 	    					+ String.valueOf(min) + ":" + String.valueOf(sec) + "\n" + "CO2 emission: " 
 	    					+ String.valueOf(SumCO2) + " g";
 				}
 				
 				reading.setText(s);
 				mHandler.postDelayed(this, 1000); 
         	}
         };
          
         mHandler.removeCallbacks(mUpdateTimeTask);
         mHandler.postDelayed(mUpdateTimeTask, 100);
         
         mapControl();
          
         // Get M from the intent by SettingActivity
         //Intent intent = getIntent();
         Bundle extras = getIntent().getExtras(); 
         
 		if(extras != null){
         	float m = extras.getFloat("M", 0f);
 			currentLocEntry.setCoef(m);
             lastLocEntry.setCoef(currentLocEntry.getCoef());
             Log.d("map", "M got: " + String.valueOf(currentLocEntry.getCoef()));	// DEBUG log message
             sMode = sMode + " " + tellWhichMode(m);
             COLOR = extras.getInt("color");
             icon = extras.getInt("ICON");
             Log.d("map", "Icon got: " + String.valueOf(icon));	// DEBUG log message
         }
         
 		Log.d("map","color set to" + String.valueOf(COLOR));
         //toast = Toast.makeText(getApplicationContext(), "Distance: " + String.valueOf(NEWSumDistance) + " m\n" + "Duration: " + String.valueOf(hour) + ":" + String.valueOf(min) + ":" + String.valueOf(sec) + "\n" + "CO2 emission: " + String.valueOf(NEWCO2M) + " g", Toast.LENGTH_SHORT);
         //toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 50);
         
         //check for TTS data
         Intent TTSIntent = new Intent();
         TTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
         startActivityForResult(TTSIntent, MY_DATA_CHECK_CODE);
         
         vib = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);	// prepare vibrate service
         
         shakeListener = new ShakeListener(this);
         Log.d("map", "shakeListener set");	// DEBUG log message
         shakeListener.setOnShakeListener(new ShakeListener.OnShakeListener() {  
             public void onShake() {            	
             	// Shaking behavior triggers a text pop-up and speech of distance & CO2 emission 
             	Log.d("map", "shakeListener triggered");	// DEBUG log message
             	//toast.setText("Distance: " + String.valueOf(NEWSumDistance) + " m\n" + "Duration: " + String.valueOf(hour) + ":" + String.valueOf(min) + ":" + String.valueOf(sec) + "\n" + "CO2 emission: " + String.valueOf(NEWCO2M) + " g");		
         		//toast.show();
        		if(StartTime != 0){
         			vib.vibrate(300);	//vibrate on shake: not suitable for Galaxy_S3
         			/*speaking("It takes" + String.valueOf(currentLocEntry.formatLocalTime()[0]) + "hour" 
         					+ String.valueOf(currentLocEntry.formatLocalTime()[1]) + "minutes" 
         					+ String.valueOf(currentLocEntry.formatLocalTime()[2]) + "seconds to travel"  
         					+ String.valueOf(SumDistance) + "meters; " + "And your current CO2 emission is" 
         					+ String.valueOf(SumCO2) + "grams");*/
         			speaking("You have traveled " + String.valueOf(SumDistance) + " meters, " 
         					+ "and your current CO2 emission is " + String.valueOf(SumCO2) + " grams.");
                 }
             }  
         });
         
         
         Log.d("map", "oncreate end");	// DEBUG log message
     }
     
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_maptracking, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         
     	switch (item.getItemId()) {
         case R.id.walk:
         	currentLocEntry.setCoef(180); COLOR=Color.RED; icon = R.drawable.ic_walk;
         	break;
         case R.id.bike:
         	currentLocEntry.setCoef(75); COLOR=Color.BLUE; icon = R.drawable.ic_bike;
         	break;
         case R.id.bus:
         	currentLocEntry.setCoef(100); COLOR=Color.CYAN; icon = R.drawable.ic_bus;
         	break;
         case R.id.car:
         	currentLocEntry.setCoef(149); COLOR=Color.GREEN; icon = R.drawable.ic_car;
         	break;
         case R.id.tram:
         	currentLocEntry.setCoef(60); COLOR=Color.GRAY; icon = R.drawable.ic_tram;
         	break;
         case R.id.ferry:
         	currentLocEntry.setCoef(125); COLOR=Color.YELLOW; icon = R.drawable.ic_ferry;
         	break;
         case R.id.train:
         	currentLocEntry.setCoef(43); COLOR=Color.MAGENTA; icon = R.drawable.ic_train;
         	break;
         case R.id.metro:
         	currentLocEntry.setCoef(3.3f); COLOR=Color.BLACK; icon = R.drawable.ic_metro;
         	break;
         
         case R.id.satellite:
         	Toast.makeText(getApplicationContext(), "Satellite View", Toast.LENGTH_SHORT).show();
         	if(mapView.isSatellite()==false) {
         		mapView.setSatellite(true);
         	}
         return true;
         
         case R.id.normal:
         	Toast.makeText(getApplicationContext(), "Normal Map View", Toast.LENGTH_SHORT).show();
         	if(mapView.isSatellite()==true) {
         		mapView.setSatellite(false);
         	}
         return true;
         
         case R.id.clear:
         	/*timerFlag = false;
         	SumDistance=0;
         	SumCO2=0;
         	SumTime=0;
         	//StartTime = System.currentTimeMillis();
         	//currentLocEntry.setLocalTime(StartTime);
         	lastLocEntry.set(currentLocEntry);
         	
         	overlays.clear();
     		mapView.invalidate(); 
     		//isInitial = false;
     		isFirstLocation = true;
     		//initMyLocation();
     		timerFlag = true;
     		this.onResume();*/
         	try {
                 int pid = android.os.Process.myPid();
                 android.os.Process.killProcess(pid);
             } catch (Exception e) {
                 System.out.println(e.getMessage());
             } finally {
                 System.exit(0);
             }
 
         return true;
         
         case R.id.stoptracking:
         	if(currentLocEntry.getLocalTime() == 0) finish();
         	else{
         	timerFlag = false;        	
         	//shakeListener.onPause();
         	//locationManager.removeUpdates(MapTracking.this);        	
         	double x = currentLocEntry.getLoca().getLatitude();
     		double y = currentLocEntry.getLoca().getLongitude();
     		GeoPoint tail = new GeoPoint((int)(x*1E6), (int)(y*1E6));
     		overlays.add(new MarkerOverlay(tail,DESTMARKER));
 			mapView.invalidate();
 			Log.d("map","dest marker drawn");
 			this.onPause();
         	}
         return true;
         
         case R.id.commit:
         	Intent intent = new Intent(MapTracking.this, SummaryActivity.class);
         	Bundle bun = new Bundle();
         	bun.putLong("STime", SumTime);
         	bun.putDouble("SDistance", SumDistance);
         	bun.putDouble("SCO2", SumCO2);
         	intent.putExtras(bun);
 			//intent.putExtra("SDistance", SumDistance);
 			//intent.putExtra("SCO2", SumCO2);
 			//intent.setClass(MapTracking.this, SummaryActivity.class);
         	//intent.putExtra("finalObj", currentLocEntry);
 			startActivity(intent);	// trigger SummaryActivity
 			//finish();
         return true;
         
         
         //case R.id.about:
         //	new AlertDialog.Builder(MapTracking.this).setTitle("CO2 Emission Alert").setMessage("This app is developed by Junlong Xiang, Xiang Gao, and Feihu Qu, and is released under the GPL v2 software license.\n 26.11.12 Helsinki")
 		//	.setCancelable(false).setNegativeButton("Got it", new DialogInterface.OnClickListener()
 		//	{	public void onClick(DialogInterface dialog, int which)
 		//		{
 		//			dialog.cancel();
 		//		}
 		//	}).show();
         //return true;
         
         default:
         return super.onOptionsItemSelected(item);
         }
     	
     	if(currentLocEntry.getCoef() != lastLocEntry.getCoef()){
     		Log.d("map","transfer mode changed");
     		nMode += 1;
     		sMode = sMode + " " + tellWhichMode(currentLocEntry.getCoef());
     		
     		UpdateEntryTask updateEntryTask = new UpdateEntryTask();
             updateEntryTask.execute((Object[]) null);
     		
     		double x = currentLocEntry.getLoca().getLatitude();
     		double y = currentLocEntry.getLoca().getLongitude();
     		GeoPoint head = new GeoPoint((int)(x*1E6), (int)(y*1E6));
     		overlays.add(new MarkerOverlay(head,icon));
 			mapView.invalidate();
 			Log.d("map","new marker drawn");
 			
     	}
     	return true;
     }
     
     
     private class AddEntryTask extends AsyncTask<Object, Void, Void> {
         @Override
         protected Void doInBackground(Object... params) {
           
             try {
             	if(db != null)
             		db.addLocEntry(currentLocEntry);
             	
             } catch (Exception e) {
               e.printStackTrace();
             }
             return null;
         }
 
         @Override
         protected void onPostExecute(final Void unused) {
         	
         }
       }
     
     private class UpdateEntryTask extends AsyncTask<Object, Void, Void> {
         @Override
         protected Void doInBackground(Object... params) {
           
             try {
             	if(db != null)
             		db.updateLocEntry(currentLocEntry);
             	
             } catch (Exception e) {
               e.printStackTrace();
             }
             return null;
         }
 
         @Override
         protected void onPostExecute(final Void unused) {
         	
         }
       }
     
     /***************Initiate****************/
     
     private void mapControl()
     {		// configure mapview 
     	mapView = (MapView)findViewById(R.id.mapViewId);
     	mapView.setBuiltInZoomControls(true);
     	//pro = mapView.getProjection();
     	overlays = mapView.getOverlays();
         mapController = mapView.getController();
     	mapController.setZoom(14);
     		// Use both mobile network provider and GPS to update location information
     	locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
     	//locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 10, MapTracking.this);
     	//locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, MapTracking.this);
     	Log.d("map", "mapControl set");
     }
     
     
     @Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
     	if(ExitActivity.isApplicationTerminated){
 			finish();
 		}
     	super.onResume();
 		//t.notify();
 		Log.d("map", "resume start");	// DEBUG log message
 		if (isInitial)
 		{
 			shakeListener.onResume();
 			Log.d("map", "shakelistener set");	// DEBUG log message
 			//Log.d("map", "location listener set");	// DEBUG log message
 			//if(isGPSOn) locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 6, MapTracking.this);
 	    	//else	locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 6, MapTracking.this);
 			myLocationLay.enableCompass();
 			myLocationLay.enableMyLocation();        
 	        
 		}else
 		{			
 			init();			
 		}
 		
 		Log.d("map", "locationListener set");	// DEBUG log message
 		if(isGPSOn) locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 6, MapTracking.this);
     	else	locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 6, MapTracking.this);
 		
 		Log.d("map", "resume end");	// DEBUG log message
 	}
     
     
 	// Setup the location service and initial the start point of tracking on map
 	private void init()
     {
 		Log.d("map", "init start");	// DEBUG log message
 		if (!(isGPSOn = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))) {	
     			// If GPS service is disabled on mobile phone, give an alert dialog
     		new AlertDialog.Builder(MapTracking.this).setTitle("Map Tools").setMessage("Your localization service is not setup. Try to turn it on?")
 			.setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener()
 			{	// When choosing to turn on GPS, go to setting page
 				public void onClick(DialogInterface dialog, int which)
 				{
 					startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
 				}
 			}).setNegativeButton("Cancel", new DialogInterface.OnClickListener()
 			{	// When choose not to open GPS, give a notice of disability
 				public void onClick(DialogInterface dialog, int which)
 				{
 					Toast.makeText(MapTracking.this, "This application is blocked without GPS service.", Toast.LENGTH_SHORT).show();
 				}
 			}).show();    		
     	}
 		else {	
 			// make seed for initializing customized Location object: get the last known location in history
     		Log.d("map", "getLastKnownLocation");	// DEBUG log message	
     		if ( locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null)
 			{		// If GPS can provide the last known location data, get it using GPS
 				seedLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 			
 			}else if (locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null)
 			{		// otherwise, test mobile network provider if the last known location is available
 				seedLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 			}
     		Log.d("map", "the seedLocation is: " + seedLocation.toString());	// DEBUG log message	
     		
     		   		
 			initMyLocation();	// initMyLocation()	
 			isInitial = true;	// Flag: initialization is done
 			Log.d("map", "init end");	// DEBUG log message
     	}
     }
 	
 	
 	private void initMyLocation()
     {   
 		Log.d("map", "initMyLocation start");	// DEBUG log message
 		// Initialize my current location
 		List<Overlay> overlays = mapView.getOverlays();
         myLocationLay = new MyLocationOverlay(this, mapView);
          
         myLocationLay.enableCompass();
         myLocationLay.enableMyLocation();
         
         myLocationLay.runOnFirstFix(new Runnable(){
         	// fast proximate my location
         	@Override
         	public void run() {
         		Log.d("map", "run funonfirstfix function");	// DEBUG log message
         		GeoPoint loc = myLocationLay.getMyLocation();	// most recent set location
         		mapController.animateTo(loc);
         		//Log.d("map", "get the firstlocation");	// DEBUG log message
         		Log.d("map", "the firstlocationfix is:" + loc.toString());	// DEBUG log message
         		       	
         	}        	 
         });
         
         overlays.add(myLocationLay);	// add myLocationLay to mapview
         mapView.invalidate();
         
         Log.d("map", "initMyLocation end");	// DEBUG log message
     }
 	
 		
     /***************Pause/Stop/Destroy****************/
     
     
     @Override
 	protected void onPause() {
 		
 		Log.d("map", "pause start");	// DEBUG log message
 		shakeListener.onPause();
 		Log.d("map","shakelistener paused");
 		if(isInitial)
 		{
 			Log.d("map", "locationListener unregister");	// DEBUG log message
 			locationManager.removeUpdates(MapTracking.this);
 			myLocationLay.disableCompass();
 	        myLocationLay.disableMyLocation();
 		}
 		
 		super.onPause();
 		Log.d("map", "pause end");	// DEBUG log message
 	}
 
 	
     	// clean up current activity after destroyed
     @Override
 	protected void onDestroy() {
     	Log.d("map", "onDestroy");	// DEBUG log message
     	//flag = false;
 		super.onDestroy();
 	}
 	
    
 
     /***************Location Change & Calculation****************/
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	
 	@Override
 	public void onLocationChanged(Location location) {
 		// TODO Auto-generated method stub
 		//Log.v("map", location.toString());	// DEBUG log message
 		Log.d("map", "onLocationChanged triggered");	// DEBUG log message
 		
 		if(isFirstLocation) {		
 				// if input location is the first point in tracking
 			Log.d("map", "isFirstLocation ture");	// DEBUG log message
 			currentLocEntry.setLoca(location);
 		    StartTime = System.currentTimeMillis();	// initiate start timestamp
 		    currentLocEntry.setLocalTime(StartTime);
 		    lastLocEntry.set(currentLocEntry);
 		    isFirstLocation = false;
 		    
 		}else {		// otherwise, store lastLocation and update currentLocation
 			Log.d("map", "isFirstLocation false");	// DEBUG log message
 			lastLocEntry.set(currentLocEntry);
 			currentLocEntry.setLoca(location);
 			currentLocEntry.setLocalTime(System.currentTimeMillis()); // update current timestamp
 		}
 			// use lastLocation as start point of line
 		double x1 = lastLocEntry.getLoca().getLatitude();
 		double y1 = lastLocEntry.getLoca().getLongitude();
 		double x2 = currentLocEntry.getLoca().getLatitude();
 		double y2 = currentLocEntry.getLoca().getLongitude();
 		GeoPoint begin = new GeoPoint((int)(x1*1E6), (int)(y1*1E6));			
 		GeoPoint end = new GeoPoint((int)(x2*1E6), (int)(y2*1E6)); // use currentLocation as end point of line
 		
 		Log.d("map", "the lastlocation is:" + begin.toString());	// DEBUG log message
 		Log.d("map", "the currentlocation is:" + end.toString());	// DEBUG log message
 			// draw the line and add an Overlay to mapview
 		if(lastLocEntry.isEqual(currentLocEntry)) {
 			overlays.add(new MarkerOverlay(begin,icon));
 			mapView.invalidate();
 			Log.d("map","marker drawn");
 		}else{
 			overlays.add(new LineOverlay(begin,end,COLOR));
 			mapView.invalidate();
 			Log.d("map","line drawn");
 		}
 		//mapView.invalidate();
 		mapController.animateTo(end);
 		
 		updateNum(); //calculate distance and CO2 emission
 		
 		
 		AddEntryTask addEntryTask = new AddEntryTask();
         addEntryTask.execute((Object[]) null);
             //     db.addLocEntry(currentLocEntry);
 			// show the toast updated on location changed
 		//toast.setText("Distance: " + String.valueOf(NEWSumDistance) + " m\n" + "Duration: " + String.valueOf(hour) + ":" + String.valueOf(min) + ":" + String.valueOf(sec) + "\n" + "CO2 emission: " + String.valueOf(NEWCO2M) + " g");		
 		//toast.show();
 		
 		if((thresBlock < 1) && (SumCO2 > CO2_THRESHOLD)){	// actively check if the CO2 emission has exceeded the threshold
 			thresBlock += 1;
 			vib.vibrate(300);
         	speaking("Alas! Your current CO2 emission has exceeded the threshold" + String.valueOf(CO2_THRESHOLD));
 		}
 		
 		//Log.d("map", "get the currentlocation");	// DEBUG log message
 		//Log.d("location", "the lastlocation is:" + lastLocation.toString());	// DEBUG log message
 		//Log.d("location", "the currentlocation is:" + currentLocation.toString());	// DEBUG log message
 
 	}
 
 	@Override
 	public void onProviderDisabled(String provider) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onProviderEnabled(String provider) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 		//calculate distance and CO2 emission between the currentpoint and lastpoint
 	public void updateNum()
 	{		// Compute the approximate distance in meters between two locations
 		
 		currentLocEntry.setDT(currentLocEntry.getLocalTime() - lastLocEntry.getLocalTime());
 		currentLocEntry.setDD(currentLocEntry.diff(lastLocEntry));
 		currentLocEntry.setDC(currentLocEntry.getCoef() * currentLocEntry.getDD() / 1000);
 		
 		SumTime = currentLocEntry.getLocalTime() - StartTime;		
 		SumDistance += currentLocEntry.getDD();	// SumDistance accumulates calculation result
 		SumCO2 += currentLocEntry.getDC();
 		
 		SumDistance = (double)Math.round(SumDistance*10)/10; 
 		SumCO2 = (double)Math.round(SumCO2*10)/10;
 		
 		Log.d("map","Time error:"+String.valueOf(currentLocEntry.getLoca().getTime() - currentLocEntry.getLocalTime()));
 		Log.d("map","Speed: "+String.valueOf(currentLocEntry.getLoca().getSpeed()));
 		Log.d("map","CO2 "+String.valueOf(SumCO2));
 	}
 	
 	
 	/***************TTS****************/ 
 
     //check TTS
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == MY_DATA_CHECK_CODE) {
 			//if TTS has installed
 			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
 				TTS = new TextToSpeech(this, this);
 			}
 			else {
                  //or install TTSDATA
 				Intent installTTSIntent = new Intent();
 				installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
 				startActivity(installTTSIntent);
 			}
 		}
 	}
      //initial TTS
 	public void onInit(int initStatus) {
 		if (initStatus == TextToSpeech.SUCCESS) {
 			if(TTS.isLanguageAvailable(Locale.US)==TextToSpeech.LANG_AVAILABLE)
 				TTS.setLanguage(Locale.US);
 		}
 		else if (initStatus == TextToSpeech.ERROR) {
 			Toast.makeText(this, "TextToSpeech failed", Toast.LENGTH_LONG).show();
 		}
 	}
  
 	public void speaking(String speech) {
 		TTS.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
 	}
 	
 
 	/***************Drawing overlay****************/
 	
 		// draw a tracking line with Overlay on the mapview, given two ends
 	private class LineOverlay extends Overlay{
 		private GeoPoint beginPoint;
 		private GeoPoint endPoint;
 		private int lineColor;
 		
 		public LineOverlay(GeoPoint begin, GeoPoint end, int linecolor)
 		{
 			beginPoint = begin;
 			endPoint = end;
 			lineColor = linecolor;
 		}
 		
 		public void draw(Canvas canvas, MapView mapV, boolean shadow)
 		{
 			super.draw(canvas, mapV, shadow);
 			Paint paint = new Paint();
 			paint.setColor(lineColor);
 			paint.setStyle(Paint.Style.FILL_AND_STROKE);
 			paint.setStrokeWidth(5);
 			Point pixBeginPoint = new Point();
 			Point pixEndPoint = new Point();
 			Path path = new Path();
 			mapV.getProjection().toPixels(beginPoint, pixBeginPoint);
 			mapV.getProjection().toPixels(endPoint, pixEndPoint);
 			path.moveTo(pixBeginPoint.x, pixBeginPoint.y);
 			path.lineTo(pixEndPoint.x, pixEndPoint.y);
 			canvas.drawPath(path,paint);
 		}
 	}
 	
 	// draw a marker icon with Overlay on the mapview, given one point
 	private class MarkerOverlay extends Overlay{
 		private GeoPoint point;
 		private int icon;
 		
 		public MarkerOverlay(GeoPoint point, int icon)
 		{
 			this.point = point;
 			this.icon = icon;
 		}
 		
 		public void draw(Canvas canvas, MapView mapV, boolean shadow)
 		{
 			super.draw(canvas, mapV, shadow);
 			Point screenPoint = new Point();
 			mapV.getProjection().toPixels(point, screenPoint);
 			Bitmap markerImage = BitmapFactory.decodeResource(getResources(), icon);
 			canvas.drawBitmap(markerImage,screenPoint.x - markerImage.getWidth() / 2, screenPoint.y - markerImage.getHeight(), null);
 			
 		}
 	}
 	
 	/***************LocEntry: unit structure for tracking data****************/
 	
 	public static class LocEntry implements Serializable{
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 		private long localtime;	// local timestamp
 		private float coef;
 		private Location loca;
 		private long deltaT;
 		private float deltaD;
 		private float deltaC;
 		
 		public LocEntry() {
 			super();
 			this.localtime = 0;
 			this.coef = 0;
 			this.loca = null;
 			this.deltaT = 0;
 			this.deltaD = 0;
 			this.deltaC = 0;
 		}
 		
 		public LocEntry(Location loc) {
 			super();
 			this.localtime = 0;
 			this.coef = 0;
 			this.loca = new Location(loc);
 			this.deltaT = 0;
 			this.deltaD = 0;
 			this.deltaC = 0;
 		}
 		
 				
 		public LocEntry(long t, float m, Location loc, long dt, float dd, float dc) {
 			super();
 			this.localtime = t;
 			this.coef = m;
 			this.loca = new Location(loc);
 			this.deltaT = dt;
 			this.deltaD = dd;
 			this.deltaC = dc;
 		}
 		
 		public LocEntry(long t, float m, double x, double y, double z, float speed, long time, long dt, float dd, float dc) {
 			super();
 			this.localtime = t;
 			this.coef = m;
 			this.loca = new Location(seedLocation);
 			this.loca.setLatitude(x);
 			this.loca.setLongitude(y);
 			this.loca.setAltitude(z);
 			this.loca.setSpeed(speed);
 			this.loca.setTime(time);
 			this.deltaT = dt;
 			this.deltaD = dd;
 			this.deltaC = dc;
 		}
 		
 			// get assigned from another LocEntry
 		public void set(LocEntry l) {
 			this.localtime = l.getLocalTime();
 			this.coef = l.getCoef();
 			this.loca = l.getLoca();
 			this.deltaT = l.getDT();
 			this.deltaD = l.getDD();
 			this.deltaC = l.getDC();
 		}
 		
 		// getting local timestamp
 	    public long getLocalTime(){
 	        return this.localtime;
 	    }
 	 
 	    // setting local timestamp
 	    public void setLocalTime(long t){
 	        this.localtime = t;
 	    }
 	    
 	    // getting transport mode coefficient
 	    public float getCoef(){
 	        return this.coef;
 	    }
 	 
 	    // setting transport mode coefficient
 	    public void setCoef(float m){
 	        this.coef = m;
 	    }
 	    
 	    // getting location: latitude, longitude
 	    public Location getLoca(){
 	        return this.loca;
 	    }
 	 
 	    // setting location: latitude, longitude
 	    public void setLoca(Location loc){
 	        this.loca = new Location(loc);
 	    }
 	    
 	    // getting delta time
 	    public long getDT(){
 	        return this.deltaT;
 	    }
 	 
 	    // setting delta time
 	    public void setDT(long dt){
 	        this.deltaT = dt;
 	    }
 	    
 	    // getting delta distance
 	    public float getDD(){
 	        return this.deltaD;
 	    }
 	 
 	    // setting delta distance
 	    public void setDD(float dd){
 	        this.deltaD = dd;
 	    }
 	    
 	    // getting delta CO2 emission
 	    public float getDC(){
 	        return this.deltaC;
 	    }
 	 
 	    // setting delta CO2 emission
 	    public void setDC(float dc){
 	        this.deltaC = dc;
 	    }
 	    	
 	    // returns {hh, mm, ss} of localtime
 	    public int[] formatLocalTime(){
 	    	long millis = this.localtime;
 	    	int sec = (int) (millis / 1000);
     		int min = sec / 60; sec %= 60;
     		int hour = min / 60; min %= 60;
     		int r[] = {hour, min, sec};
     		return r;
 	    }
 	    	
 	    // if equals, return true
 	    public boolean isEqual(LocEntry l){
 	    	return (this.getLocalTime() == l.getLocalTime());
 	    }
 	    
 	    // difference in distance
 	    public float diff(LocEntry l){
 	    	return this.loca.distanceTo(l.getLoca());
 	    }
 	    
 	    @Override
 	    public String toString(){
 	    	return new StringBuilder()
 	    	.append("Local timestamp: ")
 	    	.append(this.localtime)
 	    	.append(", Coefficient: ")
 	    	.append(this.coef)
 	    	.append(", Location: ")
 	    	.append(this.loca.toString())
 	    	.append(", Delta Time: ")
 	    	.append(this.deltaT)
 	    	.append(", Delta Distance: ")
 	    	.append(this.deltaD)
 	    	.append(", Delta CO2 Emission: ")
 	    	.append(this.deltaC).toString();
 	    }
 	}
 	
 	public static String tellWhichMode(float mValue){
 		
 		switch((int)mValue){
 		case 180:	
 			return "walking";
 		case 75:
 			return "bicycle";
 		case 60:
 			return "tram";
 		case 125:
 			return "ferry";
 		case 43:
 			return "train";
 		case 100:
 			return "bus";
 		case 149:
 			return "car";
 		default:
 			return "metro";
 		}
 	}
 	
 }
