 	package com.example.freshfoodfinder;
 	
 	import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
 import com.google.android.gms.maps.GoogleMapOptions;
 import com.google.android.gms.maps.MapFragment;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.google.android.maps.GeoPoint;
 	
 	public class UserMap extends Activity{
 	
 	private GoogleMap map;
 	private MapFragment mapFragment;
 	private GeoPoint geo;
 	private LocationManager locationManager;
 	private String provider;
 	private Intent parcel;
 	private Food activeFood;
 	private Location userLocation;
 	private ArrayList<Food> SuperMarketFoods;
 	
 	private MarketManager marketManager;
 	
 	static final LatLng HAMBURG = new LatLng(53.558, 9.927);
 	static final LatLng KIEL = new LatLng(53.551, 9.993);
 
 	
 	private List<Market> markets;
 	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 	super.onCreate(savedInstanceState);
 	setContentView(R.layout.usermap);
 	mapFragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.map));
 	
 	//Map setup
 	map = mapFragment.getMap();
 	setUpMapIfNeeded();
 	GoogleMapOptions options = new GoogleMapOptions();
 	options.mapType(GoogleMap.MAP_TYPE_TERRAIN)
 	.compassEnabled(true)
 	.rotateGesturesEnabled(true)
 	.tiltGesturesEnabled(true);
 	
 
 	map.setMyLocationEnabled(true);
 	
 	/*marketManager = new MarketManager();
 	markets = marketManager.getAllMarkets();*/
 
 	
 	//Get user location set
 	locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 	// Define the criteria how to select the locatioin provider -> use
 	// default
 	Criteria criteria = new Criteria();
 	provider = locationManager.getBestProvider(criteria, false);
 	userLocation = locationManager.getLastKnownLocation(provider);
 	
 	LatLng userLatLng = new LatLng(userLocation.getLatitude(),userLocation.getLongitude());
 	
 	
 	/*instantiateMarkets();
 
 	instantiateSuperMarketFoodList();
 	instantiateSuperMarkets();*/
 	
 	
 	String activeFoodName = getIntent().getStringExtra("com.example.freshfoodfinder.activeFood");
 	activeFood = new Food(activeFoodName, 1, Calendar.JANUARY, Calendar.DECEMBER);
 
 	marketManager = new MarketManager();
 	markets = marketManager.getAllMarkets();
 	//Make markers on map for the appropriate markets
 	markets = searchMarkets();
 	markMarkets(markets);
	//markMarkets(filterMarketsByDistance());
 
 	
 	// Move the camera instantly to user's location with a zoom of 15.
 	map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 30));
 	
 	// Zoom in, animating the camera.
 	map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
 	
 
 	}
 	/*private void instantiateSuperMarketFoodList(){
 
 		Food apple = new Food("Apple",1, Calendar.JANUARY, Calendar.DECEMBER);
 		Food banana = new Food("Banana",1, Calendar.JANUARY, Calendar.DECEMBER);
 		Food carrot = new Food("Carrot",1, Calendar.JANUARY, Calendar.DECEMBER);
 		SuperMarketFoods = new ArrayList<Food>();
 		SuperMarketFoods.add(apple);
 		SuperMarketFoods.add(banana);
 		SuperMarketFoods.add(carrot);
 		
 	}*/
 	
 	/*private void instantiateSuperMarkets(){
 		LatLng freshGrocerLocation = new LatLng(39.954499,-75.202864);
 		LatLng supremeLocation = new LatLng(39.954792,-75.208733);
 		Market freshGrocer = new Market("Fresh Grocer",SuperMarketFoods,freshGrocerLocation);
 		Market supreme = new Market("Supreme Supermarket",SuperMarketFoods,supremeLocation);
 		markets.add(supreme);
 		markets.add(freshGrocer);
 	}*/
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 	// Inflate the menu; this adds items to the action bar if it is present.
 	getMenuInflater().inflate(R.menu.mapactivity, menu);
 	return true;
 	}
 	
 	private void instantiateMarkets() {
 
 /*		Food apple = new Food("apple");
 		Food banana = new Food("banana");
 		Food carrot = new Food("carrot");
 
 		Food apple = new Food("apple", Food.FOOD_FRUIT, Calendar.JANUARY, Calendar.DECEMBER);
 		Food banana = new Food("banana", Food.FOOD_FRUIT, Calendar.JANUARY, Calendar.DECEMBER);
 		Food carrot = new Food("carrot", Food.FOOD_FRUIT, Calendar.JANUARY, Calendar.DECEMBER);
 >>>>>>> 43ba16fd374e174b23cd345ac2e3e959518a9ae1
 		ArrayList<Food> foods = new ArrayList<Food>();
 		foods.add(apple);
 		foods.add(banana);
 		foods.add(carrot);
 
 		//This location is accurate
 		LatLng freshGrocerLocation = new LatLng(39.954499,-75.202864);
 		LatLng supremeLocation = new LatLng(39.954792,-75.208733);
 		LatLng twenty3rdAndOregonShopRiteLocation = new LatLng(39.919951,-75.186009);
 		LatLng mifflinAndSwansonShopRiteLocation = new LatLng(39.923308,-75.145326);
 
 
 		Market freshGrocer = new Market("Fresh Grocer",foods,freshGrocerLocation);
 		Market supreme = new Market("Supreme Supermarket",foods,supremeLocation);
 		Market twenty3rdAndOregonShopRite = new Market("twenty3rdAndOregonShopRite",foods,twenty3rdAndOregonShopRiteLocation);
 		Market mifflinAndSwansonShopRite = new Market("mifflinAndSwansonShopRite",foods,mifflinAndSwansonShopRiteLocation);
 		freshGrocer.removeFood(apple);
 		mifflinAndSwansonShopRite.removeFood(apple);
 		markets.add(freshGrocer);
 		markets.add(supreme);
 		markets.add(twenty3rdAndOregonShopRite);
 		markets.add(mifflinAndSwansonShopRite);*/
 	}
 	
 	//This method provided by the Google Documentation
 	//* https://developers.google.com/maps/documentation/android/map
 	
 	private void setUpMapIfNeeded() {
 	// Do a null check to confirm that we have not already instantiated the map.
 		if (map == null) {
 			map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
 					.getMap();
 	// Check if we were successful in obtaining the map.
 			if (map != null) {
 	// The Map is verified. It is now safe to manipulate the map.
 	
 			}
 		}
 	}
 	
 	//Returns list of markets with the food
 	private List<Market> searchMarkets() {
 		List<Market> appropriateMarkets = new ArrayList<Market>();
 		for (Market m : markets) {
 			if (m.hasFood(activeFood)) {
 				appropriateMarkets.add(m);
 			}
 		}
 		return appropriateMarkets;
 	}
 	
 	private void markMarkets(List<Market> marketsWithActiveFood) {
 		if (marketsWithActiveFood == null) return;
 		for (Market m : marketsWithActiveFood) {
 			LatLng location = new LatLng(m.getLatLng().latitude,m.getLatLng().longitude);
 			Marker market = map.addMarker(new MarkerOptions()
 			.position(location)
 			.title(m.getName()));
 			OnMarkerClickListener mcl = new OnMarkerClickListener() {
 				
 				@Override
 				public boolean onMarkerClick(Marker marker) {
 					final Intent intent = new Intent(Intent.ACTION_VIEW,
 						       /** Using the web based turn by turn directions url. */
 						       Uri.parse(
 						                "http://maps.google.com/maps?" +
 						                "saddr="+ userLocation.getLatitude()+","+userLocation.getLongitude()+
 						                "&daddr="+marker.getPosition().latitude+","+marker.getPosition().longitude+"&dirflg=w"));
 								intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
 						       startActivity(intent);
 						 
 					return false;
 				}
 			};
 			map.setOnMarkerClickListener(mcl);
 			
 
 		}
 	}
 	
 	private List<Market> filterMarketsByDistance() {
 
 		List<Market> appropriateMarkets = new ArrayList<Market>();
 		for (Market m : markets) {
			if (markets.get(0).getLocation().distanceTo(m.getLocation())<1609) {
 
 				appropriateMarkets.add(m);
 			}
 		}
 		return appropriateMarkets;
 }
 	
 	}
