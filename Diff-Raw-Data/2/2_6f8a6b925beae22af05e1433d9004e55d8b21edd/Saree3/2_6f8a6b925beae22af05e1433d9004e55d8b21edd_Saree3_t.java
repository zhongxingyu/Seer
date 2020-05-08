 package com.man_r.Saree3;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Typeface;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.PowerManager;
 import android.text.format.DateUtils;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class Saree3 extends Activity {
 
 	TextView latitude;
 	TextView longitude;
 	TextView maxlatitude;
 	TextView maxlongitude;
 	TextView speedText;
 	TextView kmh;
 	TextView max;
 	
 	String maxLat="";
 	String maxLong = "";
 	
 	int maxSpeed=0;
 	int speed=0;
 	
 	String phoneNumber = null;
     
 	LocationManager locationManager;
 	LocationListener locationListener;
 	
 	protected PowerManager.WakeLock mWakeLock;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         latitude = (TextView)findViewById(R.id.latitude);
     	longitude = (TextView)findViewById(R.id.longitude);
     	maxlatitude = (TextView)findViewById(R.id.maxlatitude);
     	maxlongitude = (TextView)findViewById(R.id.maxlongitude);
     	speedText = (TextView)findViewById(R.id.speed);
     	kmh = (TextView)findViewById(R.id.kmh);
     	max = (TextView)findViewById(R.id.maxSpeed);
     	
         Typeface font = Typeface.createFromAsset(getAssets(), "d10re.ttf");
         speedText.setTypeface(font);
         max.setTypeface(font);
         kmh.setTypeface(font);
         
         final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
         this.mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My Tag");
         this.mWakeLock.acquire();
         
       //Get a reference to the NotificationManager:
 		String ns = Context.NOTIFICATION_SERVICE;
 		NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(ns);
 		
 		Notification notification = new Notification();
 		notification.flags = Notification.FLAG_ONGOING_EVENT;
 		notification.number = maxSpeed;
 		notification.icon = R.drawable.notification_icon;
 		notification.tickerText = "Saree3";
 		
 		//Define the notification message and PendingIntent
 		CharSequence contentTitle = "Saree3";
 		CharSequence contentText = "maxSpeed= " + maxSpeed + " Km/h";
 		Intent notificationIntent = new Intent(this, Saree3.class);
 		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
 		
 		notification.setLatestEventInfo(this, contentTitle, contentText, contentIntent);
 		
 		//Pass the Notification to the NotificationManager
 		mNotificationManager.notify(3, notification);
         
     }//onCreate
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onStart()
 	 */
 	@Override
 	protected void onStart() {
 		// TODO Auto-generated method stub
 		super.onStart();
 		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		locationListener = new MyLocationListener();
                 
         final Criteria criteria = new Criteria();
         
         criteria.setSpeedRequired(true);
         criteria.setAccuracy(Criteria.ACCURACY_LOW);
         
         String bestProvider = locationManager.getBestProvider(criteria, true);
        
         if ((bestProvider != null) && (bestProvider.contains("gps"))){
         	max.setText("NO Signal !");
         	locationManager.requestLocationUpdates(bestProvider, 500, 0, locationListener);
         }//if(bestProvider != null)
         else{
         	AlertDialog.Builder builder = new AlertDialog.Builder(this);
         	builder.setMessage("No GPS!")
         	       .setCancelable(true)
         	       .setPositiveButton("Enable GPS", new DialogInterface.OnClickListener() {
         	           public void onClick(DialogInterface dialog, int id) {
         	        	   Intent switchIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
         	               startActivityForResult(switchIntent, 0);
         	           }
         	       })
         	       .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
         	           public void onClick(DialogInterface dialog, int id) {
         	               finish();
         	           }
         	       });
         	AlertDialog alert = builder.create();
         	alert.show();
         }//else
            	
 	}
 
 		
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// TODO Auto-generated method stub
 		MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.menu, menu);
         return true;
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onDestroy()
 	 */
 	@Override
 	protected void onDestroy() {
 		// TODO Auto-generated method stub
 		
 		
 		this.mWakeLock.release();
 		super.onDestroy();
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// TODO Auto-generated method stub
 		switch(item.getItemId()){
 		case R.id.close:
         	AlertDialog.Builder builder = new AlertDialog.Builder(this);
         	builder.setMessage("Are you sure you want to exit?")
         	       .setCancelable(false)
         	       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
         	           public void onClick(DialogInterface dialog, int id) {
         	        	   
         	        	   locationManager.removeUpdates(locationListener);
         	       		
 	        	       	   String ns = Context.NOTIFICATION_SERVICE;
 	        	       	   NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
 	        	       	   mNotificationManager.cancel(3);
         	       		
         	        	   finish();
         	           }
         	       })
         	       .setNegativeButton("No", new DialogInterface.OnClickListener() {
         	           public void onClick(DialogInterface dialog, int id) {
         	                dialog.cancel();
         	           }
         	       });
         	AlertDialog alert = builder.create();
         	alert.show();
         	break;
         	
 		case R.id.mapLayout:
         	Intent myIntent = new Intent(getApplicationContext(), Map.class);
             startActivityForResult(myIntent, 0);
         	break;
         	
 		case R.id.GPSSwitch:
         	Intent switchIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
             startActivity(switchIntent);        
             break;
             
         case R.id.chalange:
         	shareIt();
         	//Intent intent = new Intent(Intent.ACTION_PICK);
         	//intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
         	//startActivityForResult(intent, 3);
         	break;
         	
         default:
             break;
         	
 		}//switch(item.getItemId())
 		//return super.onOptionsItemSelected(item);
 		return true;
 	}
     
 	String getDiffrence(long now, long then){
     	if(now > then)
             return DateUtils.formatElapsedTime((now - then)/1000L);
         else 
         	return DateUtils.formatElapsedTime((then - now)/1000L);
     }
 	
 	public void shareIt() {
 				
 		String message = "can you beet me:\n";
     	message = message + "speed:" + maxSpeed + "\n";
     	message = message + "http://maps.google.com/maps?q=" + maxLat + "," + maxLong + "\n";
     	message = message + "this message is sent from saree3";
     	
 		Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
 		sharingIntent.setType("text/plain");
 		sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "shareing subject");
 		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, message);
 		
 		startActivity(Intent.createChooser(sharingIntent, "Share via"));
 	}
 	
 	
 	public class MyLocationListener implements LocationListener{
 
 		public void onLocationChanged(Location loc) {
 			// TODO Auto-generated method stub
 			if(loc.hasSpeed()){
 				speed = (int) (loc.getSpeed()* 3.6);
 				if(speed>maxSpeed){
 					maxSpeed=speed;
     				maxLat = "" + loc.getLatitude();
     				maxLong = "" + loc.getLongitude();
     				maxlatitude.setText("maxlatitude: " + maxLat);
     				maxlongitude.setText("maxlongitude: " + maxLong);
     				
     				//Get a reference to the NotificationManager:
     				String ns = Context.NOTIFICATION_SERVICE;
     				NotificationManager mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(ns);
     				
     				Notification notification = new Notification();
     				notification.flags = Notification.FLAG_ONGOING_EVENT;
     				notification.number = maxSpeed;
     				notification.icon = R.drawable.notification_icon;
     				notification.tickerText = maxSpeed + "";
     				
     				//Define the notification message and PendingIntent
     				CharSequence contentTitle = "Saree3";
     				CharSequence contentText = "maxSpeed= " + maxSpeed + " Km/h";
     				Intent notificationIntent = new Intent(getApplicationContext(), Saree3.class);
     				PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);
     				
     				notification.setLatestEventInfo(getApplicationContext(), contentTitle, contentText, contentIntent);
     				
     				//Pass the Notification to the NotificationManager
     				mNotificationManager.notify(3, notification);
 				}//if(speed>maxSpeed)
 				
 				if (speed == 0)
 					speedText.setText("000");
 				else if (speed < 10)
 					speedText.setText("00" + speed);
 				else if (speed < 100)
 					speedText.setText("0" + speed);
 				else
					speedText.setText("" + speed);
 				
     			max.setText("max= " + maxSpeed + " Km/h");
     			
 			}//if(loc.hasSpeed())
 			
 			else{
 				max.setText("No Signal !");
 	    		latitude.setText("latitude: " + loc.getLatitude());
 				longitude.setText("longitude: " + loc.getLongitude());
 				
 			}//else
 			
 			
 			
 			
 		}
 
 		public void onProviderDisabled(String provider) {
 			// TODO Auto-generated method stub
 			Toast.makeText(getApplicationContext(), "Gps Disabled", Toast.LENGTH_SHORT).show();
 		}
 
 		public void onProviderEnabled(String provider) {
 			// TODO Auto-generated method stub
 			Toast.makeText(getApplicationContext(), "Gps Esabled", Toast.LENGTH_SHORT).show();
 		}
 
 		public void onStatusChanged(String provider, int status, Bundle extras) {
 			// TODO Auto-generated method stub
 			if(status!=2)
     			max.setText("No Gps !");
 		}
 		
 	}
 }
