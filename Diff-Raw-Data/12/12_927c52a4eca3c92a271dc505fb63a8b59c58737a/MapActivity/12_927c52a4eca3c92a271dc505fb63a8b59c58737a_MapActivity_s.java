 package com.northstar.minimap;
 
 import com.northstar.minimap.beacon.BeaconListener;
 import com.northstar.minimap.beacon.BeaconManager;
 import com.northstar.minimap.beacon.IBeacon;
 import com.northstar.minimap.beacon.StickNFindBluetoothBeacon;
 import com.northstar.minimap.itinerary.Itinerary;
 import com.northstar.minimap.itinerary.ItineraryPoint;
 import com.northstar.minimap.map.Map;
 import com.northstar.minimap.map.Table;
 import com.northstar.minimap.map.UserPositionListener;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.view.GravityCompat;
 import android.support.v4.widget.DrawerLayout;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.BaseAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class MapActivity extends Activity implements SensorEventListener {
 
     public static final String KEY_ENV = "ENV";
 
     public static final double FT_TO_M = 0.3048;
     public static final double M_TO_FT = 3.2808;
    public static final double FT_TO_MAP_PIXELS = 20;
 
     public static final int ENV_PRODUCTION = 1;
     public static final int ENV_TEST = 2;
 
     public static final int MAP_HEIGHT = 600;
     public static final int MAP_WIDTH = 600;
 
     private float[] gravityValues;
     private float[] magneticValues;
 
     private ActionBarDrawerToggle actionBarDrawerToggle;
     private BeaconListener beaconListener;
     private BeaconManager beaconManager;
     private Bundle configBundle;
     private DrawerLayout drawerLayout;
     private Itinerary itinerary;
     private List<IBeacon> beacons;
     private ListView itineraryListView;
     private Sensor accelerometer;
     private Sensor magnetometer;
     private SensorManager sensorManager;
     private UserPositionListener userPositionListener;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_map);
 
         drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
 
         actionBarDrawerToggle = new ActionBarDrawerToggle(
                 this, drawerLayout, R.drawable.ic_drawer, R.string.drawer_open,
                 R.string.drawer_close);
 
         drawerLayout.setDrawerListener(actionBarDrawerToggle);
         drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
         getActionBar().setDisplayHomeAsUpEnabled(true);
 
         itineraryListView = (ListView) findViewById(R.id.leftDrawer);
 
         // Initialize beacon ids
         StickNFindBluetoothBeacon.initBeaconIdMap();
 
         sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
         accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
         magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
 
         if (this.getIntent() != null && this.getIntent().getExtras() != null) {
             configBundle = this.getIntent().getExtras();
             switch (configBundle.getInt(KEY_ENV)) {
                 case ENV_TEST:
                     initTestEnvironment();
                     break;
                 case ENV_PRODUCTION:
                     break;
                 default:
                     initTestEnvironment();
             }
         }
     }
 
     @Override
     public void onAccuracyChanged(Sensor sensor, int i) {
 
     }
 
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
         actionBarDrawerToggle.onConfigurationChanged(newConfig);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         getMenuInflater().inflate(R.menu.map, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
             return true;
         }
 
         switch (item.getItemId()) {
             case R.id.settingsMenuItem:
                 startActivity(new Intent(this, SettingsActivity.class));
                 break;
             case R.id.restartBluetoothMenuItem:
                 beaconManager.restartBluetooth();
                 break;
             case R.id.resetCalibrationMenuItem:
                 for (IBeacon beacon : beacons) {
                     beacon.resetCalibration();
                 }
                 Toast.makeText(this, R.string.reset_calibration_success, Toast.LENGTH_SHORT).show();
                 break;
             default:
                 break;
         }
 
         return super.onOptionsItemSelected(item);
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         sensorManager.unregisterListener(this);
     }
 
     @Override
     protected void onPostCreate(Bundle savedInstanceState) {
         super.onPostCreate(savedInstanceState);
         actionBarDrawerToggle.syncState();
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
         sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
 
         SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
         String range = sharedPref.getString(SettingsActivity.KEY_PREF_PROXIMITY_ZONE_RANGE, "");
 
         try {
             beaconManager.setProximityZoneRange(Double.parseDouble(range));
         } catch (NumberFormatException e) {}
     }
 
     @Override
     public void onSensorChanged(SensorEvent event) {
         switch(event.sensor.getType()) {
             case Sensor.TYPE_ACCELEROMETER:
                 gravityValues = event.values;
                 break;
             case Sensor.TYPE_MAGNETIC_FIELD:
                 magneticValues = event.values;
                 break;
             default:
                 break;
         }
 
         if (gravityValues != null && magneticValues != null) {
             float R[] = new float[9];
             float I[] = new float[9];
 
             if (SensorManager.getRotationMatrix(R, I, gravityValues, magneticValues)) {
                 float orientation[] = new float[3];
                 SensorManager.getOrientation(R, orientation);
 
                 if (userPositionListener != null) {
                     userPositionListener.onUserAzimuthChanged(orientation[0]);
                 }
             }
         }
     }
 
     public void calibrate(Position calibrationPosition) {
         beaconManager.calibrate(calibrationPosition);
         Toast.makeText(this, R.string.calibrated, Toast.LENGTH_SHORT).show();
     }
 
     private void initTestEnvironment() {
         Position[] positions = new Position[] {
                 new Position(0, 0),
                 new Position(5, 0),
                 new Position(10, 0),
                 new Position(0, 5),
                 new Position(10, 5),
                 new Position(0, 10),
                 new Position(5, 10),
                 new Position(10, 10)
         };
 
         beacons = new ArrayList<IBeacon>();
         for (int i = 1; i <= 8; i++) {
             beacons.add(new StickNFindBluetoothBeacon(
                     i, StickNFindBluetoothBeacon.beaconIdMap.get(i), positions[i - 1]));
         }
 
         beaconManager = new BeaconManager(this, beacons);
     }
 
     public void processMap() {
         CustomMapFragment mapFrag =
                 (CustomMapFragment) getFragmentManager().findFragmentById(R.id.map_fragment);
 
         if (configBundle != null) {
             switch (configBundle.getInt(KEY_ENV)) {
                 case ENV_TEST:
                     mapFrag.setMap(testMap());
                     break;
                 case ENV_PRODUCTION:
                     productionMap();
                     break;
                 default:
                     break;
             }
         }
     }
     
     public void productionMap() {
         CustomMapFragment mapFrag = (CustomMapFragment)getFragmentManager().findFragmentById(R.id.map_fragment);
         Globals state = (Globals)getApplicationContext();
         String jsonMap = state.data.mapsJson;
         String mapID = state.data.mapID;
 
         Map URLMap = new MapBuilder().getMap(jsonMap, mapID);
         beaconManager = new BeaconManager(this, URLMap.getBeacons());
         mapFrag.setMap(URLMap);
     }
     
     private Map testMap(){
         Map testMap = new Map();
         
         testMap.setMapID("0");
 
         List<Table> tables = new ArrayList<Table>();
 
         for (Table table: tables) {
             testMap.addTable(table);
         }
 
         for (IBeacon beacon: beacons) {
             testMap.addBeacon(beacon);
         }
 
         processItinerary();
 
         return testMap;
     }
     
     public void processItinerary() {
         Log.w("JP", "Fake Itin being sent to frag");
         List<ItineraryPoint> itinPoints = new ArrayList<ItineraryPoint>();
 
         ItineraryPoint ip1 = new ItineraryPoint("Point 1", new Position(50.0, 50.0));
         ItineraryPoint ip2 = new ItineraryPoint("Point 2", new Position(100.0, 100.0));
         ItineraryPoint ip3 = new ItineraryPoint("Point 3", new Position(150.0, 150.0));
         ItineraryPoint ip4 = new ItineraryPoint("Point 4", new Position(150.0, 150.0));
         ItineraryPoint ip5 = new ItineraryPoint("Point 5", new Position(150.0, 150.0));
         ItineraryPoint ip6 = new ItineraryPoint("Point 6", new Position(150.0, 150.0));
 
         itinPoints.add(ip1);
         itinPoints.add(ip2);
         itinPoints.add(ip3);
         itinPoints.add(ip4);
         itinPoints.add(ip5);
         itinPoints.add(ip6);
 
         itinerary = new Itinerary(itinPoints);
         itineraryListView.setAdapter(itineraryAdapter);
         itineraryListView.setOnItemClickListener(itinerarySelector);
     }
     
     public void setBeaconListener(BeaconListener beaconListener) {
         this.beaconListener = beaconListener;
         beaconManager.setBeaconListener(beaconListener);
     }
 
     public void setCurrentItineraryPoint(ItineraryPoint point) {
         CustomMapFragment mapFrag =
                 (CustomMapFragment) getFragmentManager().findFragmentById(R.id.map_fragment);
         mapFrag.setCurrentItineraryPoint(point);
     }
 
     public void setUserPositionListener(UserPositionListener userPositionListener) {
         this.userPositionListener = userPositionListener;
         beaconManager.setUserPositionListener(userPositionListener);
     }
 
     public static Position toMapPosition(Position measuredPosition) {
        double x = measuredPosition.getX() / FT_TO_MAP_PIXELS;
        double y = measuredPosition.getY() / FT_TO_MAP_PIXELS;
 
         return new Position((int) Math.round(x), (int) Math.round(y));
     }
 
     public static Position toMeasuredPosition(Position mapPosition) {
        double x = mapPosition.getX() * FT_TO_MAP_PIXELS;
        double y = mapPosition.getY() * FT_TO_MAP_PIXELS;
 
         return new Position(x, y);
     }
 
     private BaseAdapter itineraryAdapter = new BaseAdapter() {
         @Override
         public int getCount() {
             return itinerary.getCount();
         }
 
         @Override
         public Object getItem(int position) {
             return itinerary.getItineraryPoint(position);
         }
 
         @Override
         public long getItemId(int position) {
             return 0;
         }
 
         @Override
         public View getView(int position, View contentView, ViewGroup parent) {
             View listItem = LayoutInflater.from(parent.getContext()).inflate(
                     R.layout.list_item_itinerary, null);
             TextView title = (TextView) listItem.findViewById(R.id.itineraryTitle);
             title.setText(itinerary.getItineraryPoint(position).getName());
             return listItem;
         }
     };
 
     private AdapterView.OnItemClickListener itinerarySelector =
             new AdapterView.OnItemClickListener() {
         @Override
         public void onItemClick(AdapterView<?> parent, View view, int position, long id){
             ItineraryPoint currentPoint = itinerary.getItineraryPoint(position);
             setCurrentItineraryPoint(currentPoint);
             drawerLayout.closeDrawer(Gravity.LEFT);
         }
     };
 }
