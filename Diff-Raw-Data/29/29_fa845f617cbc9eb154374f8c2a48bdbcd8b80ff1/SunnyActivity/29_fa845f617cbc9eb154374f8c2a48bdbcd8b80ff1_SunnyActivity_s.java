 package uk.me.jamespic.sunny;
 
 import android.app.ListActivity;
 import android.content.Context;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.location.LocationProvider;
 import android.os.Bundle;
 import android.widget.SimpleAdapter;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TimeZone;
 import uk.me.jstott.coordconv.LatitudeLongitude;
 import uk.me.jstott.sun.Sun;
 import uk.me.jstott.sun.Time;
import static android.os.Looper.getMainLooper;
 
 public class SunnyActivity extends ListActivity
 {
     public static final String LINE1 = "line1";
     public static final String LINE2 = "line2";
     public static final float REFRESH_DISTANCE = 10000.0f;
     private static final int REFRESH_TIME = 5000;
     private SimpleAdapter adapter;
     private List<Map<String, ?>> itemList;
     private Map<String, Object> duskMap;
     private Map<String, Object> dawnMap;
     private Map<String, Object> sunriseMap;
     private Map<String, Object> sunsetMap;
     private Map<String, Object> astroDawnMap;
     private Map<String, Object> astroDuskMap;
     private Map<String, Object> nauticalDawnMap;
     private Map<String, Object> nauticalDuskMap;
     private MyLocationListener listener = null;
     private LocationManager locMgr;
     
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         
         itemList = new ArrayList<Map<String,?>>();
         adapter = new SimpleAdapter(this,
                           itemList,
                           android.R.layout.two_line_list_item,
                           new String[] { LINE1, LINE2},
                           new int[] { android.R.id.text1, android.R.id.text2 });
         dawnMap = addItem("Dawn", "Getting Location...");
         sunriseMap = addItem("Sunrise", "Getting Location...");
         sunsetMap = addItem("Sunset", "Getting Location...");
         duskMap = addItem("Dusk", "Getting Location...");
         astroDawnMap = addItem("Astronomical Dawn", "Getting Location...");
         astroDuskMap = addItem("Astronomical Dusk", "Getting Location...");
         nauticalDawnMap = addItem("Nautical Dawn", "Getting Location...");
         nauticalDuskMap = addItem("Nautical Dusk", "Getting Location...");
         
         
         locMgr =
                 (LocationManager) getSystemService(Context.LOCATION_SERVICE);
         Criteria locCrit = new Criteria();
         locCrit.setAccuracy(Criteria.ACCURACY_COARSE);
         String provider = locMgr.getBestProvider(locCrit, true);
         
         Location loc = null;
         try {
             loc = locMgr.getLastKnownLocation(provider);
         } catch (Exception e) {
             addItem("Exception",e.toString()+": "+e.getMessage());
         }
         if (loc != null) {
             updateForLocation(loc);
         }
         listener = new MyLocationListener();
         
        locMgr.requestSingleUpdate(provider, listener, getMainLooper());
         
         locMgr.requestLocationUpdates(
                provider, REFRESH_TIME, REFRESH_DISTANCE, listener, getMainLooper());
         
         setListAdapter(adapter);
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         if (listener != null) {
             locMgr.removeUpdates(listener);
         }
     }
 
     private Map<String,Object> addItem(String line1, String line2) {
         HashMap<String, Object> tempMap = new HashMap<String, Object>();
         tempMap.put(LINE1, line1);
         tempMap.put(LINE2, line2);
         itemList.add(tempMap);
         adapter.notifyDataSetChanged();
         return tempMap;
     }
 
     private void updateForLocation(Location loc) {
         LatitudeLongitude latlong =
                 new LatitudeLongitude(loc.getLatitude(), loc.getLongitude());
         TimeZone tz = TimeZone.getDefault();
         Calendar date = Calendar.getInstance(tz);
         boolean dst = tz.inDaylightTime(date.getTime());
         
         //Y U NO REFACTOR INTO METHOD
         try {
             sunriseMap.put(LINE2, Sun.sunriseTime(date, latlong, tz, dst).toString());
         } catch (Exception e) {
             sunriseMap.put(LINE2, "Not Today!");
         }
         
         try {
             dawnMap.put(LINE2, Sun.morningCivilTwilightTime(date, latlong, tz, dst).toString());
         } catch (Exception e) {
             dawnMap.put(LINE2, "Not Today!");
         }
         
         try {
             sunsetMap.put(LINE2, Sun.sunsetTime(date, latlong, tz, dst).toString());
         } catch (Exception e) {
             sunsetMap.put(LINE2, "Not Today!");
         }
         
         try {
             duskMap.put(LINE2, Sun.eveningCivilTwilightTime(date, latlong, tz, dst).toString());
         } catch (Exception e) {
             duskMap.put(LINE2, "Not Today!");
         }
         
         try {
             astroDawnMap.put(LINE2, Sun.morningAstronomicalTwilightTime(date, latlong, tz, dst).toString());
         } catch (Exception e) {
             astroDawnMap.put(LINE2, "Not Today!");
         }
         
         try {
             astroDuskMap.put(LINE2, Sun.eveningAstronomicalTwilightTime(date, latlong, tz, dst).toString());
         } catch (Exception e) {
             astroDuskMap.put(LINE2, "Not Today!");
         }
         
         try {
             nauticalDawnMap.put(LINE2, Sun.morningNauticalTwilightTime(date, latlong, tz, dst).toString());
         } catch (Exception e) {
             nauticalDawnMap.put(LINE2, "Not Today!");
         }
         
         try {
             nauticalDuskMap.put(LINE2, Sun.eveningNauticalTwilightTime(date, latlong, tz, dst).toString());
         } catch (Exception e) {
             nauticalDuskMap.put(LINE2, "Not Today!");
         }
     }
     
     private class MyLocationListener implements LocationListener {
 
         public void onLocationChanged(final Location arg0) {
             runOnUiThread(new Runnable() {
 
                 public void run() {
                     updateForLocation(arg0);
                 }
             });
         }
 
         public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}
 
         public void onProviderEnabled(String arg0) {}
 
         public void onProviderDisabled(String arg0) {}
         
     }
 }
