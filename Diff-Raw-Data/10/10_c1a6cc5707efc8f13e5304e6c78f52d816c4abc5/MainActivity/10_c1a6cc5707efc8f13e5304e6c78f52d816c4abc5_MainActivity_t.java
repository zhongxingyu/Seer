 package com.weblogic.lutskbus;
 
 import android.content.Context;
 import android.graphics.*;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Bundle;
 
 import android.os.Message;
 import android.os.Handler;
 import android.os.Handler.Callback;
 import android.widget.Toast;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.MapFragment;
 import com.google.android.gms.maps.model.*;
import android.support.v4.app.FragmentActivity;
 
 import java.util.*;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
public class MainActivity extends FragmentActivity {
 
     private GoogleMap map;
 
     long starttime = 0;
 
     private LatLng Weblogic = new LatLng(50.774831,25.366509);
 
     private static final String TAG_MARK = "mark";
     private static final String TAG_ID = "id";
     private static final String TAG_LAT = "lt";
     private static final String TAG_LNG = "ln";
     private static final String TAG_ROUTE = "r";
     private static final String TAG_SPEED = "s";
     private static final String TAG_SNUMBER = "sn";
 
     private Map<Integer, Marker> markers = new HashMap<Integer, Marker>();
 
 
     //this  posts a message to the main thread from our timertask
     //and updates the textfield
     final Handler h = new Handler(new Callback() {
 
         @Override
         public boolean handleMessage(Message msg) {
 
             if (isNetworkConnected()) {
                 new AsyncTask<Void, Void, JSONObject>() {
                     private String url = "http://mak.lutsk.ua/MoveOnMap?level=1&nodeid=1";
 
                     @Override
                     protected JSONObject doInBackground(Void... params) {
 
                         JSONParser jParser = new JSONParser();
 
                         // getting JSON string from URL
                         JSONObject json = jParser.getJSONFromUrl(url);
                         return json;
                     }
 
                     protected void onPostExecute(JSONObject json) {
                         try {
                             // Getting Array of Contacts
                             JSONArray marks = json.getJSONArray(TAG_MARK);
 
                             // looping through All Contacts
                             for(int i = 0; i < marks.length(); i++){
                                 JSONObject c = marks.getJSONObject(i);
 
                                 // Storing each json item in variable
                                 Integer id = c.getInt(TAG_ID);
                                 Double lat = c.getDouble(TAG_LAT);
                                 Double lng = c.getDouble(TAG_LNG);
                                 String route = c.getString(TAG_ROUTE);
                                 Integer speed = c.getInt(TAG_SPEED);
                                 String snumber = c.getString(TAG_SNUMBER);
                                 if (markers.get(id) != null) {
                                     LatLng oldPosition = markers.get(id).getPosition();
                                     double latR = oldPosition.latitude-lat;
                                     double lngR = oldPosition.longitude-lng;
 
                                     if (Math.abs(latR)>0.00009||Math.abs(lngR)>0.00009) {
                                         Double way = bearing(oldPosition.latitude, oldPosition.longitude, lat, lng);
                                         markers.get(id).setIcon(drawIcon(route, way));
                                     } else {
                                         if (speed==0)
                                             markers.get(id).setIcon(drawIcon(route));
                                     }
 
                                     markers.get(id).setPosition(new LatLng(lat, lng));
                                 } else {
 
                                     Marker marker = map.addMarker(new MarkerOptions()
                                             .position(new LatLng(lat, lng))
                                             .title(snumber)
                                             .icon(drawIcon(route))
                                             .anchor(0.5f, 0.5f));
                                     markers.put(id, marker);
                                 }
 
 
                             }
                         } catch (JSONException e) {
                             e.printStackTrace();
                         }
                     }
                 }.execute();
             }
 
             return false;
         }
 
 
     });
 
     //tells handler to send a message
     class firstTask extends TimerTask {
 
         @Override
         public void run() {
             h.sendEmptyMessage(0);
         }
     }
 
 
     Timer timer = new Timer();
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                 .getMap();
 
 
         // Acquire a reference to the system Location Manager
         LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
 
         // Define a listener that responds to location updates
         LocationListener locationListener = new LocationListener() {
             public void onLocationChanged(Location location) {
                 // Called when a new location is found by the network location provider.
                 drawMarker(location);
             }
 
             public void onStatusChanged(String provider, int status, Bundle extras) {}
 
             public void onProviderEnabled(String provider) {}
 
             public void onProviderDisabled(String provider) {}
         };
 
         if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
             locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
 
             Location mapStartPosition = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 
             if (mapStartPosition != null)
                 map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mapStartPosition.getLatitude(),
                         mapStartPosition.getLongitude()), 16));
         } else {
             map.moveCamera(CameraUpdateFactory.newLatLngZoom(Weblogic, 16));
         }
 
         if (!isNetworkConnected())
             Toast.makeText(MainActivity.this, R.string.need_internet, Toast.LENGTH_SHORT).show();
 
         map.addMarker(new MarkerOptions()
                 .position(Weblogic)
                 .icon(BitmapDescriptorFactory
                         .fromResource(R.drawable.weblogic)));
 
         starttime = System.currentTimeMillis();
         timer = new Timer();
         timer.schedule(new firstTask(), 0, 5000);
     }
 
     private Marker myMarker;
 
     private void drawMarker(Location location){
 
         LatLng currentPosition = new LatLng(location.getLatitude(),
                 location.getLongitude());
 
         if (myMarker != null) {
             myMarker.setPosition(currentPosition);
         } else {
             myMarker = map.addMarker(new MarkerOptions()
                 .position(currentPosition)
                 .icon(BitmapDescriptorFactory
                         .fromResource(R.drawable.me)));
         }
     }
 
     private boolean isNetworkConnected() {
         ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo ni = cm.getActiveNetworkInfo();
         if (ni == null) {
             return false;
         } else
             return true;
     }
 
     @Override
     public void onStop() {
         super.onStop();
         timer.cancel();
     }
 
     @Override
     public void onRestart() {
         super.onRestart();
         timer = new Timer();
         timer.schedule(new firstTask(), 0, 5000);
     }
 
     protected static double bearing(double lat1, double lon1, double lat2, double lon2) {
         double longDiff= lon2-lon1;
         double y= Math.sin(longDiff)*Math.cos(lat2);
         double x=Math.cos(lat1)*Math.sin(lat2)-   Math.sin(lat1)*Math.cos(lat2)*Math.cos(longDiff);
 
         return (Math.toDegrees(Math.atan2(y, x))+360)%360;
     }
 
     protected BitmapDescriptor drawIcon(String text, Double way) {
         Integer picture = R.drawable.stop;
 
         Bitmap.Config conf = Bitmap.Config.ARGB_8888;
         Bitmap bmp = Bitmap.createBitmap(134, 134, conf);
         Canvas canvas = new Canvas(bmp);
 
         // paint defines the text color,
         // stroke width, size
         Paint mPaint = new Paint();
         mPaint.setTextSize(26);
         mPaint.setColor(Color.BLACK);
         mPaint.setAntiAlias(true);
         mPaint.setTypeface(Typeface.create(Typeface.SERIF,
                 Typeface.BOLD));
 
         mPaint.setTextAlign(Paint.Align.LEFT);
 
         Rect bounds = new Rect();
         mPaint.getTextBounds(text, 0, text.length(), bounds);
 
         if (way>0&&way<=22.5||way>337.5) {
             picture = R.drawable.draw_0;
         } else if (way>22.5&&way<=67.5) {
             picture = R.drawable.draw_45;
         } else if (way>67.5&&way<=112.5) {
             picture = R.drawable.draw_90;
         } else if (way>112.5&&way<=157.5) {
             picture = R.drawable.draw_135;
         } else if (way>157.5&&way<=202.5) {
             picture = R.drawable.draw_180;
         } else if (way>202.5&&way<=247.5) {
             picture = R.drawable.draw_225;
         } else if (way>247.5&&way<=292.5) {
             picture = R.drawable.draw_225;
         } else if (way>292.5&&way<=337.5) {
             picture = R.drawable.draw_315;
         }
 
 
         canvas.drawBitmap(BitmapFactory.decodeResource(getResources(),
                 picture), 0,0, mPaint);
         Integer totalS = 134/2-(bounds.right-bounds.left)/2;
         canvas.drawText(text, totalS, 134/2+(bounds.bottom-bounds.top)/2, mPaint);
 
         return BitmapDescriptorFactory.fromBitmap(bmp);
     }
 
     protected BitmapDescriptor drawIcon(String text)
     {
         return drawIcon(text, -1.0);
     }
 
 }
