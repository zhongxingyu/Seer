 package com.restaurant.collection;
 
 import java.util.ArrayList;
 
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.LinearLayout;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
 import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.restaurant.collection.api.RestaurantAPI;
 import com.restaurant.collection.db.SQLiteRestaurant;
 import com.restaurant.collection.entity.Category;
 import com.restaurant.collection.entity.Restaurant;
 import com.restaurant.gps.util.GPSTracker;
 
 public class MapActivity extends SherlockFragmentActivity implements OnMarkerClickListener, OnInfoWindowClickListener {
 	
 	private static final int    ID_LOAD_MORE = 1;
 	private GoogleMap map;
 	private double latitude;
 	private double longitude;
 	ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
 	private Bundle mBundle;
 	private int category_id;
 	private boolean isColletion;
 	private Spinner cSpinner;
 	private LinearLayout secondCategoryLayout;
 	private Spinner subCatSpinner;
 	private LinearLayout categorySelectorLayout;
 	private int second_category_id;
 	private LinearLayout loadmoreLayout;
 	private int                               myPage          = 1;
 	private Boolean                           checkLoad       = true;
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.layout_map);
         map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
 
         final ActionBar ab = getSupportActionBar();
         ab.setDisplayHomeAsUpEnabled(true);
         ab.setTitle("地圖顯示");
         
         mBundle = this.getIntent().getExtras();
         category_id = mBundle.getInt("CategoryId");
         second_category_id = mBundle.getInt("SecondCategoryId");
         isColletion = mBundle.getBoolean("IsColletion");
         
         findViews();
         setSpinner();
         new DownloadRestaurantsTask().execute();
         
     }
     
     
     private void findViews() {
     	 cSpinner = (Spinner)findViewById(R.id.category_spinnner);
     	 loadmoreLayout = (LinearLayout) findViewById(R.id.load_more_grid);
 		 secondCategoryLayout = (LinearLayout)findViewById(R.id.second_category_layout);
 		 subCatSpinner = (Spinner)findViewById(R.id.second_category_spinnner);
 		 categorySelectorLayout = (LinearLayout)findViewById(R.id.category_selector);
 		 loadmoreLayout.setVisibility(View.GONE);	
     }
 
 
 	private void setSpinner() {
 		
 		if(isColletion)
 			categorySelectorLayout.setVisibility(View.GONE);
 		
 		final ArrayList<Category> cs = Category.getCategories();
     	ArrayList<String> categoriessString = new ArrayList<String>();
     	categoriessString.add("全部");
     	for (int i = 0; i < cs.size(); i++) {
     		categoriessString.add(cs.get(i).getName());
 		}
         ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categoriessString);
         cSpinner.setAdapter(adapter);
         
         cSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
 
 			@Override
 			public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
 				if(position == 0){
                     
 					category_id = 0;
 					secondCategoryLayout.setVisibility(View.GONE);
 					new DownloadRestaurantsTask().execute();
 				}else{
 					final Category selectCategory = Category.getCategories().get(position-1);
 					category_id = selectCategory.getId();
 					new DownloadRestaurantsTask().execute();
 					final ArrayList<Category> subCategories = selectCategory.getSecondCategories();
 			    	ArrayList<String> subCategoriessString = new ArrayList<String>();
 			    	subCategoriessString.add("全部");
 			    	for (int i = 0; i < subCategories.size(); i++) {
 			    		subCategoriessString.add(subCategories.get(i).getName());
 					}
 			        ArrayAdapter<String> sAdapter = new ArrayAdapter<String>(MapActivity.this, android.R.layout.simple_spinner_item, subCategoriessString);
 			        subCatSpinner.setAdapter(sAdapter);
 			        secondCategoryLayout.setVisibility(View.VISIBLE);
 			        subCatSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
 						@Override
 						public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
 							if(position == 0){
 								second_category_id = 0;
 								new DownloadRestaurantsTask().execute();
 							}else{
 								Category subCategory = subCategories.get(position-1);
 								second_category_id = subCategory.getId();
 								new DownloadRestaurantsTask().execute();
 							}
 						}
 						@Override
 						public void onNothingSelected(AdapterView<?> arg0) {
 							
 						}
 			        	
 			        });
 				}
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0) {
 				
 			}
 	
          });
 	}
 
 
 	private class DownloadRestaurantsTask extends AsyncTask {
 
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
 
         }
 
         @Override
         protected Object doInBackground(Object... params) {
         	if(isColletion){
         		SQLiteRestaurant db = new SQLiteRestaurant(MapActivity.this);
         		restaurants = db.getAllRestaurants();
         	}else if(category_id != 0 && second_category_id!=0){
         		restaurants = RestaurantAPI.getAroundRestaurants(latitude, longitude, 1, category_id, second_category_id);
         	}else if(category_id != 0){
         		restaurants = RestaurantAPI.getAroundRestaurants(latitude, longitude, 1, category_id, 0);
         	}else{
         		restaurants = RestaurantAPI.getAroundRestaurants(latitude, longitude, 1, 0, 0);
         	}
             return null;
         }
 
         @Override
         protected void onPostExecute(Object result) {
             super.onPostExecute(result);
            getCameraMoveLocation();
             setMap();
         }
     }
 	
 	private class LoadMoreTask extends AsyncTask {
 
         private ArrayList<Restaurant> moreRestaurants;
 
 		@Override
         protected void onPreExecute() {
             super.onPreExecute();
             loadmoreLayout.setVisibility(View.VISIBLE);
         }
 
         @Override
         protected Object doInBackground(Object... params) {
         	
         	if(category_id != 0 && second_category_id!=0){
         		moreRestaurants = RestaurantAPI.getAroundRestaurants(latitude, longitude, myPage, category_id, second_category_id);
         	}else if(category_id != 0){
         		moreRestaurants = RestaurantAPI.getAroundRestaurants(latitude, longitude, myPage, category_id, 0);
         	}else{
         		moreRestaurants = RestaurantAPI.getAroundRestaurants(latitude, longitude, myPage, 0, 0);
         	}
         	
         	for (int i = 0; i < moreRestaurants.size(); i++) {
             	restaurants.add(moreRestaurants.get(i));
             }
             return null;
         }
 
         @Override
         protected void onPostExecute(Object result) {
             super.onPostExecute(result);
             loadmoreLayout.setVisibility(View.GONE);
             if (moreRestaurants != null && moreRestaurants.size() != 0) {
             	setMap(moreRestaurants);
             }else{
             	checkLoad = false;
             }
         }
     }
     
    private void getCameraMoveLocation() {
//    	if(areaId !=0){
//    		double sumX = 0.0;
//    		double sumY = 0.0;
//    		for(int i=0; i < restaurants.size(); i++){
//    			sumX += restaurants.get(i).getX();
//    			sumY += restaurants.get(i).getY();
//    		}
//    		latitude = sumX/(double)restaurants.size();
//    		longitude = sumY/(double)restaurants.size();
//    	}else{
//    }
     		GPSTracker mGPS = new GPSTracker(this);
         	if(mGPS.canGetLocation() ){
         		latitude =mGPS.getLatitude();
         		longitude=mGPS.getLongitude();
         	}else{
        	// can't get the location
         	}
     	
 		
 	}
     
     
     private void setMap(ArrayList<Restaurant> moreRestaurants) {
     	
     	if (map!=null){
     		for(int i=0;i<moreRestaurants.size();i++){
     			LatLng location = new LatLng(moreRestaurants.get(i).getX(), moreRestaurants.get(i).getY());
     			
     			Marker kiel = map.addMarker(new MarkerOptions()
 		  	          .position(location)
 		  	          .title(moreRestaurants.get(i).getName()));
     		}
     	      
         }
 	}
     
     private void setMap() {
     	if (map!=null){
     		map.clear();
     		for(int i=0;i<restaurants.size();i++){
     			LatLng location = new LatLng(restaurants.get(i).getX(), restaurants.get(i).getY());
     			
     			Marker kiel = map.addMarker(new MarkerOptions()
 		  	          .position(location)
 		  	          .title(restaurants.get(i).getName()));
     		}
     	      
         }
     	
     	map.setOnMarkerClickListener(this);
     	map.setOnInfoWindowClickListener(this);
     	if(isColletion)
     	  map.moveCamera( CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude) , 8.0f) );
     	else
     	  map.moveCamera( CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude) , 12.0f) );
 		
 	}
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	if(!isColletion)
     		menu.add(0, ID_LOAD_MORE, 5, "讀更多資料").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
         return true;
     }
 
 	@Override
     public boolean onMenuItemSelected(int featureId, MenuItem item) {
 
         int itemId = item.getItemId();
         switch (itemId) {
         case android.R.id.home:
             finish();
             break;
         case ID_LOAD_MORE:
         	if (checkLoad) {
                 myPage = myPage + 1;
                 new LoadMoreTask().execute();
         	}else{
         		Toast.makeText(this, "no more data", Toast.LENGTH_LONG).show();
         	}
         }
         return true;
     }
 
 
 
 	@Override
 	public boolean onMarkerClick(Marker m) {
 		
 		return false;
 	}
 
 
 
 	@Override
 	public void onInfoWindowClick(Marker m) {
 		Intent intent = new Intent(this, RestaurantIntroActivity.class);
 		Bundle bundle = new Bundle();
 		for(int i =0 ; i < restaurants.size(); i++){
 			if(restaurants.get(i).getName().equals( m.getTitle())){
 				bundle.putInt("ResturantId", restaurants.get(i).getId());
 		    	bundle.putString("ResturantName", restaurants.get(i).getName());
 			}
 		}
     	intent.putExtras(bundle);
     	this.startActivity(intent);
 	}
 
 }
