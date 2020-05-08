 //John Paul Mardelli
 //Last updated November 19th, 2013
 
 package com.northstar.minimap;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Fragment;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.graphics.Point;
 import android.location.Location;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewTreeObserver.OnGlobalLayoutListener;
 
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.MapView;
 import com.google.android.gms.maps.MapsInitializer;
 import com.google.android.gms.maps.Projection;
 import com.google.android.gms.maps.model.CircleOptions;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.LatLngBounds;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.google.android.gms.maps.model.PolygonOptions;
 import com.northstar.minimap.beacon.BeaconListener;
 import com.northstar.minimap.beacon.IBeacon;
 import com.northstar.minimap.beacon.StickNFindBluetoothBeacon;
 import com.northstar.minimap.itinerary.ItineraryPoint;
 import com.google.android.gms.maps.model.PolygonOptions;
 import com.northstar.minimap.beacon.BeaconListener;
 import com.northstar.minimap.beacon.IBeacon;
 import com.northstar.minimap.map.BluetoothLELocationSource;
 import com.northstar.minimap.map.BoundaryLocationSource;
 import com.northstar.minimap.map.Map;
 import com.northstar.minimap.map.Table;
 import com.northstar.minimap.map.UserPositionListener;
 
 /**
  * Code gotten from: http://www.matt-reid.co.uk/blog_post.php?id=93
  */
 public class CustomMapFragment extends Fragment implements BeaconListener, UserPositionListener {
 
     public static final double DRAW_PIXEL_RATIO = 2.333;
     public static final double FT_IN_PIXELS = 10;
 
     private boolean proximityZonesEnabled = true;
     private boolean showBeaconRangeCircles = true;
 
     public static final int COLOR_BEACON = Color.rgb(0, 153, 255);
     public static final int COLOR_BEACON_IN_PROXIMITY_ZONE = Color.rgb(255, 165, 0);
     public static final int COLOR_BEACON_RANGE = Color.rgb(0, 153, 255);
     public static final int COLOR_BEACON_RANGE_IN_PROXIMITY_ZONE = Color.rgb(255, 214, 153);
 
     private IBeacon proximityZoneBeacon;
 
 	// Google and Android objects
     private MapView mapView;
     private GoogleMap googleMap;
     private Projection proj;
     private BoundaryLocationSource locSource;
     
     private MapActivity parentAct;
     private Map map;
     private List<LatLngBounds> boundBoxes = new ArrayList<LatLngBounds>();
     private LatLngBounds mapBounds;
     private Marker currentItineraryPoint;
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
 
         locSource = new BluetoothLELocationSource();
     	
         // inflate and return the layout
         View v = inflater.inflate(R.layout.fragment_map, container, false);
         mapView = (MapView) v.findViewById(R.id.mapView);
         mapView.onCreate(savedInstanceState);
         mapView.onResume();//needed to get the map to display immediately
 
         MapsInitializer.initialize(this.getActivity());
 
         googleMap = mapView.getMap();
         
         //Initial zoom for projection
         googleMap.moveCamera(CameraUpdateFactory.zoomTo((float) 15.0));
 
         //I added this. Use this to get rid of googles map stuff and put our custom stuff
         googleMap.setMapType(GoogleMap.MAP_TYPE_NONE);
         
         //Set up location stuff
         googleMap.setMyLocationEnabled(true);
 
         googleMap.setLocationSource(locSource);
         googleMap.setOnMapLongClickListener(onMapLongClickListener);
 
         //customTP = new CustomTileProvider(filename, height, width);
         //overlayTOps = new TileOverlayOptions()
         //overlayTOps.tileProvider(customTP);
         //googleMap.addTileOverlay(overlayTOps);
 
         //Make reference to parent for listener
         parentAct = (MapActivity)this.getActivity();
         
         //Set up Global Layout listener to get projection at right time
         OnGlobalLayoutListener ready = new OnGlobalLayoutListener() {
 
 			@Override
 			public void onGlobalLayout() {
 				
 				//Remove listener
 				mapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
 				
 				//Grab and set the projection
 				setProjection();
 				
 				//Inform activity we are ready to process map.
 				parentAct.processMap();
 			}
         	
         };
         
         mapView.getViewTreeObserver().addOnGlobalLayoutListener(ready);
 
         return v;
     }
 
     @Override
     public void onResume() {
         super.onResume();
         mapView.onResume();
         locSource.onResume();
 
         SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(parentAct);
         showBeaconRangeCircles = sharedPref.getBoolean(SettingsActivity.KEY_PREF_BEACON_RANGE, true);
         proximityZonesEnabled = sharedPref.getBoolean(SettingsActivity.KEY_PREF_PROXIMITY_ZONE, true);
 
         if (map != null) {
             for (IBeacon beacon : map.getBeacons()) {
                 drawBeacon(beacon);
             }
         }
     }
 
     @Override
     public void onPause() {
         super.onPause();
         mapView.onPause();
         locSource.onPause();
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
         mapView.onDestroy();
     }
 
     @Override
     public void onLowMemory() {
         super.onLowMemory();
         mapView.onLowMemory();
     }
 
     @Override
     public void onBeaconDistanceChanged(IBeacon beacon, double distance) {
         drawBeacon(beacon);
     }
 
     @Override
     public void onBeaconInProximityZoneChanged(IBeacon beacon, boolean isInProximityZone) {
         Log.d("BT-PROX", beacon.getNumber() + " " + isInProximityZone);
 
         drawBeacon(beacon);
 
         if (isInProximityZone) {
             proximityZoneBeacon = beacon;
         } else {
             proximityZoneBeacon = null;
         }
     }
 
     @Override
     public void onUserAzimuthChanged(double azimuth) {
         // Compass-assisted localization disabled.
 
 //        if (proximityZoneBeacon != null) {
 //            double x = proximityZoneBeacon.computeDistance() * Math.sin(azimuth) +
 //                    proximityZoneBeacon.getPosition().getX();
 //            double y = proximityZoneBeacon.computeDistance() * Math.cos(azimuth) +
 //                    proximityZoneBeacon.getPosition().getY();
 //
 //            Position userMapPosition = MapActivity.toMapPosition(new Position(x, y));
 //            LatLng userLatLng = proj.fromScreenLocation(userMapPosition.toPoint());
 //            Location userLocation = new Location("");
 //            userLocation.setLatitude(userLatLng.latitude);
 //            userLocation.setLongitude(userLatLng.longitude);
 //            userLocation.setAccuracy(200);
 //
 //            locSource.setLocation(userLocation);
 //        }
     }
 
     @Override
     public void onUserPositionChanged(Position userPosition, double positionError) {
         // Compass-assisted localization disabled.
 
        int accuracy = (int) Math.round(positionError * DRAW_PIXEL_RATIO * FT_IN_PIXELS);
 
         Position userMapPosition = MapActivity.toMapPosition(userPosition);
         LatLng userLatLng = proj.fromScreenLocation(userMapPosition.toPoint());
         Location userLocation = new Location("");
         userLocation.setLatitude(userLatLng.latitude);
         userLocation.setLongitude(userLatLng.longitude);
         userLocation.setAccuracy(accuracy);
 
         locSource.setLocation(userLocation);
     }
     
     private void setProjection() {
     	proj = googleMap.getProjection();
     }
     
     public void setMap(Map map){
     	googleMap.clear();
     	
     	this.map = map;
     	locSource.setMap(this.map);
 
     	processTables();
         drawBeaconMarkers();
     	
     	//Temporary location set
     	Location location = new Location("TempProviderWhatever");
     	LatLng coordinate = proj.fromScreenLocation(new Point(0, 0));
     	location.setLatitude(coordinate.latitude);
         location.setLongitude(coordinate.longitude);
         location.setAccuracy(100);
     	locSource.setLocation(location);
 
         parentAct.setBeaconListener(this);
         parentAct.setUserPositionListener(this);
     }
 
     private void drawBeacon(IBeacon beacon) {
         double drawDistance = beacon.computeDistance() * DRAW_PIXEL_RATIO * FT_IN_PIXELS;
         int beaconColor = (beacon.isInProximityZone() && proximityZonesEnabled) ?
                 COLOR_BEACON_IN_PROXIMITY_ZONE : COLOR_BEACON;
 
         Point p = (MapActivity.toMapPosition(beacon.getPosition())).toPoint();
         LatLng markerLoc = proj.fromScreenLocation(p);
 
         if (beacon.getMarkerCircle() != null) {
             beacon.getMarkerCircle().remove();
         }
 
         beacon.setMarkerCircle(googleMap.addCircle(new CircleOptions()
                 .center(markerLoc)
                 .radius(30)
                 .strokeColor(Color.BLACK)
                 .strokeWidth(3)
                 .fillColor(beaconColor)));
 
         if (drawDistance > 0) {
             if (beacon.getRangeCircle() != null) {
                 beacon.getRangeCircle().remove();
             }
 
             if (showBeaconRangeCircles) {
                 int rangeColor = (beacon.isInProximityZone() && proximityZonesEnabled) ?
                         COLOR_BEACON_RANGE_IN_PROXIMITY_ZONE : COLOR_BEACON_RANGE;
 
                 beacon.setRangeCircle(googleMap.addCircle(new CircleOptions()
                         .center(markerLoc)
                         .radius(drawDistance)
                         .strokeColor(rangeColor)
                         .strokeWidth(5)));
             }
         }
     }
     
     private void drawBeaconMarkers() {
     	for (IBeacon beacon: map.getBeacons()) {
             drawBeacon(beacon);
     	}
     }
     
     private void processTables(){
     	List<Table> tables = map.getTables();
     	for(int i = 0; i < tables.size(); i++){
     		Table table = tables.get(i);
     		
     		storeBoundBoxes(table);
     		drawTables(table);
     	}	
     }
 
 
     public void setCurrentItineraryPoint(ItineraryPoint point) {
         if (currentItineraryPoint != null) {
             currentItineraryPoint.remove();
         }
 
         MarkerOptions currentItinMarkerOptions = new MarkerOptions();
 
         // Set the position and title of the Marker.
         LatLng markerLoc = proj.fromScreenLocation(point.getPos().toPoint());
         currentItinMarkerOptions = currentItinMarkerOptions.position(markerLoc);
         currentItinMarkerOptions = currentItinMarkerOptions.title(point.getName());
 
         currentItineraryPoint = googleMap.addMarker(currentItinMarkerOptions);
     }
     
     private void storeBoundBoxes(Table table){
     	LatLng neCorner = proj.fromScreenLocation(new Point((int)table.getPosition().getX() + table.getWidth(),
     								 						(int)table.getPosition().getY()));
     	LatLng swCorner = proj.fromScreenLocation(new Point((int)table.getPosition().getX(),
     								 						(int)table.getPosition().getY() + table.getHeight()));
     	LatLngBounds bound = new LatLngBounds(swCorner, neCorner);
     	boundBoxes.add(bound);
     }
     
     private void drawTables(Table table){
         
     	double heightInc = table.getHeight()/table.getHeightSubdivisions();
     	double widthInc = table.getWidth()/table.getWidthSubdivisions();
     	
     	for(int wSub = 0; wSub < table.getWidthSubdivisions(); wSub++){
     		for(int hSub = 0; hSub < table.getHeightSubdivisions(); hSub++){
     			
     			PolygonOptions tableSquare = new PolygonOptions();
     			
     			double leftX = (table.getPosition().getX() + widthInc * wSub);
     			double rightX = (table.getPosition().getX() + widthInc * (wSub + 1));
     			double topY = (table.getPosition().getY() + heightInc * hSub);
     			double bottomY = (table.getPosition().getY() + heightInc * (hSub + 1));
 
                 leftX *= FT_IN_PIXELS;
                 rightX *= FT_IN_PIXELS;
                 topY *= FT_IN_PIXELS;
                 bottomY *= FT_IN_PIXELS;
     			
     			LatLng topLeft = proj.fromScreenLocation(new Point((int)leftX, (int)topY));
     			LatLng bottomLeft = proj.fromScreenLocation(new Point((int)leftX, (int)bottomY));
     			LatLng bottomRight = proj.fromScreenLocation(new Point((int)rightX, (int)bottomY));
     			LatLng topRight = proj.fromScreenLocation(new Point((int)rightX, (int)topY));
     			
     			tableSquare = tableSquare.add(topLeft, bottomLeft, bottomRight, topRight, topLeft);
     			
     			tableSquare = tableSquare.strokeColor(Color.BLACK);
     			
     			tableSquare = tableSquare.strokeWidth(4);
     			
     			googleMap.addPolygon(tableSquare);
     		}
     	}
     }
 
     private GoogleMap.OnMapLongClickListener onMapLongClickListener =
             new GoogleMap.OnMapLongClickListener() {
         public void onMapLongClick(LatLng point) {
             Position mapPosition = new Position(proj.toScreenLocation(point));
             Position measuredPosition = MapActivity.toMeasuredPosition(mapPosition);
             parentAct.calibrate(measuredPosition);
         }
     };
 }
