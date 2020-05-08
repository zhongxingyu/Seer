 package com.restaurant.fragment;
 
 import java.util.ArrayList;
 
 import android.annotation.SuppressLint;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.Toast;
 
 import com.restaurant.adapter.RestaurantGridViewAdapter;
 import com.restaurant.collection.R;
 import com.restaurant.collection.api.RestaurantAPI;
 import com.restaurant.collection.db.SQLiteRestaurant;
 import com.restaurant.collection.entity.Restaurant;
 import com.restaurant.customized.view.LoadMoreGridView;
 import com.restaurant.gps.util.GPSTracker;
 
 @SuppressLint("ValidFragment")
 public class GridRestaurantsFragment extends Fragment {
 
     private ArrayList<Restaurant> restaurants     = new ArrayList<Restaurant>();
     private LoadMoreGridView                  myGrid;
     private RestaurantGridViewAdapter                   myGridViewAdapter;
     private LinearLayout                      progressLayout;
     private LinearLayout                      loadmoreLayout;
     private LinearLayout                      layoutReload;
     private Button                            buttonReload;
 
     private int                               myPage          = 1;
     private Boolean                           checkLoad       = true;
     private ArrayList<Restaurant> moreRestaurants = new ArrayList<Restaurant>();
 	private int area_id;
 	private int category_id;
 	private int type_id;
 	private boolean is_collection;
 	private boolean is_selected;
 	private boolean is_near;
 	private LinearLayout noDataLayout;
 	private int rank_category_id;
 	private int second_category_id;
 	private int price_high;
 	private int price_low;
 	private boolean is_show_distance = true;	
 
 	private int order;
 	// 0 for none
 	// 1 by distance
 	// 2 by service
 	// 3 by price
 	// 4 by food
 
     public GridRestaurantsFragment() {
 
     }
     
     public static final GridRestaurantsFragment newInstance(int area_id, int rank_category_id, int category_id,  int second_category_id, int type_id, boolean is_collection, boolean is_selected, boolean is_near) {
         GridRestaurantsFragment f = new GridRestaurantsFragment();
         Bundle bdl = new Bundle();
         bdl.putInt("AreaId", area_id);
         bdl.putInt("CategoryId", category_id);
         bdl.putInt("SecondCategoryId", second_category_id);
         bdl.putInt("RankCategoryId", rank_category_id);
         bdl.putInt("TypeId", type_id);
         bdl.putBoolean("IsCollection", is_collection);
         bdl.putBoolean("IsSelected", is_selected);
         bdl.putBoolean("IsNear", is_near);
         f.setArguments(bdl);
         return f;
     }
     
     public static final GridRestaurantsFragment newInstance(int area_id, int rank_category_id, int category_id,  int second_category_id, int type_id, boolean is_collection, boolean is_selected, boolean is_near, boolean is_show_distance) {
         GridRestaurantsFragment f = new GridRestaurantsFragment();
         Bundle bdl = new Bundle();
         bdl.putInt("AreaId", area_id);
         bdl.putInt("CategoryId", category_id);
         bdl.putInt("SecondCategoryId", second_category_id);
         bdl.putInt("RankCategoryId", rank_category_id);
         bdl.putInt("TypeId", type_id);
         bdl.putBoolean("IsCollection", is_collection);
         bdl.putBoolean("IsSelected", is_selected);
         bdl.putBoolean("IsNear", is_near);
         bdl.putBoolean("IsShowDistance", is_show_distance);
         f.setArguments(bdl);
         return f;
     }
     
     public static final GridRestaurantsFragment newInstance(int area_id, int rank_category_id, int category_id, int second_category_id, int type_id, boolean is_collection, boolean is_selected, boolean is_near, int price_high, int price_low, int order) {
         GridRestaurantsFragment f = new GridRestaurantsFragment();
         Bundle bdl = new Bundle();
         bdl.putInt("AreaId", area_id);
         bdl.putInt("RankCategoryId", rank_category_id);
         bdl.putInt("CategoryId", category_id);
         bdl.putInt("SecondCategoryId", second_category_id);
         bdl.putInt("TypeId", type_id);
         bdl.putBoolean("IsCollection", is_collection);
         bdl.putBoolean("IsSelected", is_selected);
         bdl.putBoolean("IsNear", is_near);
         bdl.putInt("PriceHigh", price_high);
         bdl.putInt("PriceLow", price_low);
         bdl.putInt("Order", order);
         f.setArguments(bdl);
         return f;
     }
 
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         area_id = getArguments().getInt("AreaId");
 		category_id = getArguments().getInt("CategoryId");
 		second_category_id = getArguments().getInt("SecondCategoryId");
 		rank_category_id = getArguments().getInt("RankCategoryId");
 		type_id = getArguments().getInt("TypeId");
 		is_collection = getArguments().getBoolean("IsCollection");
 		is_selected = getArguments().getBoolean("IsSelected");
 		is_near = getArguments().getBoolean("IsNear");
 		if (getArguments().containsKey("PriceHigh"))
 			price_high = getArguments().getInt("PriceHigh");
 		if (getArguments().containsKey("PriceLow"))
 			price_low = getArguments().getInt("PriceLow");
 		if (getArguments().containsKey("Order"))
 			order = getArguments().getInt("Order");
 		if (getArguments().containsKey("IsShowDistance"))
 			is_show_distance = getArguments().getBoolean("IsShowDistance");
 		
 		getCurrentXY();
         super.onCreate(savedInstanceState);
     }
     
     private double latitude;
 	private double longitude;
 	
 	private void getCurrentXY(){
 		GPSTracker mGPS = new GPSTracker(getActivity());
     	if(mGPS.canGetLocation() ){
     		latitude =mGPS.getLatitude();
     		longitude=mGPS.getLongitude();
     	}else{
     	// can't get the location
     	}
 	}
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 
         View myFragmentView = inflater.inflate(R.layout.loadmore_grid, container, false);
         progressLayout = (LinearLayout) myFragmentView.findViewById(R.id.layout_progress);
         loadmoreLayout = (LinearLayout) myFragmentView.findViewById(R.id.load_more_grid);
         layoutReload = (LinearLayout) myFragmentView.findViewById(R.id.layout_reload);
         buttonReload = (Button) myFragmentView.findViewById(R.id.button_reload);
         noDataLayout = (LinearLayout) myFragmentView.findViewById(R.id.layout_no_data);
 
         myGrid = (LoadMoreGridView) myFragmentView.findViewById(R.id.news_list);
         myGrid.setOnLoadMoreListener(new LoadMoreGridView.OnLoadMoreListener() {
             public void onLoadMore() {
                 if (checkLoad) {
                     myPage = myPage + 1;
                     new LoadMoreTask().execute();
                 } else {
                     myGrid.onLoadMoreComplete();
                 }
             }
         });
 
         buttonReload.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View arg0) {
                 progressLayout.setVisibility(View.VISIBLE);
                 layoutReload.setVisibility(View.GONE);
                 if(is_show_distance)
                 	new DownloadRestaurantsTask().execute();
             }
         });
 
         if (myGridViewAdapter != null) {
             progressLayout.setVisibility(View.GONE);
             loadmoreLayout.setVisibility(View.GONE);
             myGrid.setAdapter(myGridViewAdapter);
         } else {
             new DownloadRestaurantsTask().execute();
         }
 
         return myFragmentView;
     }
 
     @Override
     public void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
 
     }
 
     private class DownloadRestaurantsTask extends AsyncTask {
 
 		@Override
         protected void onPreExecute() {
             super.onPreExecute();
 
         }
 
         @Override
         protected Object doInBackground(Object... params) {
         	
         	if(order == 0){
 	        	if(is_near && category_id != 0 && second_category_id!=0){
 	        		restaurants = RestaurantAPI.getAroundRestaurants(latitude, longitude, 1, category_id, second_category_id);
 	        	}else if(is_near && category_id != 0){
 	        		restaurants = RestaurantAPI.getAroundRestaurants(latitude, longitude, 1, category_id, 0);
 	        	}else if(is_near){
 	        		restaurants = RestaurantAPI.getAroundRestaurants(latitude, longitude, 1, 0, 0);
 	        	}else if(area_id !=0 && category_id != 0){
 	        		restaurants = RestaurantAPI.getAreaCategoryRestaurants(area_id, category_id, 1);
 	        	}else if(area_id != 0 && rank_category_id != 0){
 	        		restaurants = RestaurantAPI.getAreaRankCategoryRestaurants(area_id, rank_category_id, 1);
 	        	}else if(area_id != 0 && second_category_id != 0){
 	        		restaurants = RestaurantAPI.getAreaSecondCategoryRestaurants(area_id, second_category_id, 1);
 	        	}else if(area_id != 0 && type_id != 0){
 	        		restaurants = RestaurantAPI.getAreaTypeRestaurants(area_id, type_id, 1);
 	        	}else if(category_id != 0 && area_id == 0){
 	        		restaurants = RestaurantAPI.getCategoryRestaurants(category_id, 1);
 	        	}else if(second_category_id != 0 && area_id == 0){
 	        		restaurants = RestaurantAPI.getSecondCategoryRestaurants(second_category_id, 1);
 	        	}else if(area_id == 0 && type_id != 0){
 	        		restaurants = RestaurantAPI.getTypeRestaurants(type_id, 1);
 	        	}else if(is_collection){
 	        		SQLiteRestaurant db = new SQLiteRestaurant(getActivity());
 	        		restaurants = db.getAllRestaurants();
 	        	}else if(is_selected){
 	        		restaurants = RestaurantAPI.getSelectRestaurants(1);
 	        	}
         	}else if(order == 1){
         		
         		if(area_id !=0 && category_id != 0){
 	        		restaurants = RestaurantAPI.getAreaCategoryRestaurantsByDistance(area_id, category_id, price_low, price_high, latitude, longitude, 1);
 	        	}else if(area_id != 0 && rank_category_id != 0){
 	        		restaurants = RestaurantAPI.getAreaRankCategoryRestaurantsByDistance(area_id, rank_category_id, price_low, price_high, latitude, longitude, 1);
 	        	}else if(area_id != 0 && second_category_id != 0){
 	        		restaurants = RestaurantAPI.getAreaSecondCategoryRestaurantsByDistance(area_id, second_category_id, price_low, price_high, latitude, longitude, 1);
 	        	}else if(area_id != 0 && type_id != 0){
 	        		restaurants = RestaurantAPI.getAreaTypeRestaurantsByDistance(area_id, type_id, price_low, price_high, latitude, longitude, 1);
 	        	}else if(category_id != 0 && area_id == 0){
 	        		restaurants = RestaurantAPI.getCategoryRestaurantsByDistance(category_id, price_low, price_high, latitude, longitude, 1);
 	        	}else if(second_category_id != 0 && area_id == 0){
 	        		restaurants = RestaurantAPI.getSecondCategoryRestaurantsByDistance(second_category_id, price_low, price_high, latitude, longitude, 1);
 	        	}else if(area_id == 0 && type_id != 0){
 	        		restaurants = RestaurantAPI.getTypeRestaurantsByDistance(type_id,price_low, price_high, latitude, longitude, 1);
 	        	}
         		
         	}else if(order == 2){
         		
         		if(area_id !=0 && category_id != 0){
 	        		restaurants = RestaurantAPI.getAreaCategoryRestaurantsByService(area_id, category_id, price_low, price_high, 1);
 	        	}else if(area_id != 0 && rank_category_id != 0){
 	        		restaurants = RestaurantAPI.getAreaRankCategoryRestaurantsByService(area_id, rank_category_id, price_low, price_high,1);
 	        	}else if(area_id != 0 && second_category_id != 0){
 	        		restaurants = RestaurantAPI.getAreaSecondCategoryRestaurantsByService(area_id, second_category_id, price_low, price_high, 1);
 	        	}else if(area_id != 0 && type_id != 0){
 	        		restaurants = RestaurantAPI.getAreaTypeRestaurantsByService(area_id, type_id, price_low, price_high, 1);
 	        	}else if(category_id != 0 && area_id == 0){
 	        		restaurants = RestaurantAPI.getCategoryRestaurantsByService(category_id, price_low, price_high, 1);
 	        	}else if(second_category_id != 0 && area_id == 0){
 	        		restaurants = RestaurantAPI.getSecondCategoryRestaurantsByService(second_category_id, price_low, price_high, 1);
 	        	}else if(area_id == 0 && type_id != 0){
 	        		restaurants = RestaurantAPI.getTypeRestaurantsByService(type_id,price_low, price_high, 1);
 	        	}
         		
         	}else if(order == 3){
         		
         		if(area_id !=0 && category_id != 0){
 	        		restaurants = RestaurantAPI.getAreaCategoryRestaurantsByPrice(area_id, category_id, price_low, price_high, 1);
 	        	}else if(area_id != 0 && rank_category_id != 0){
 	        		restaurants = RestaurantAPI.getAreaRankCategoryRestaurantsByPrice(area_id, rank_category_id, price_low, price_high, 1);
 	        	}else if(area_id != 0 && second_category_id != 0){
 	        		restaurants = RestaurantAPI.getAreaSecondCategoryRestaurantsByPrice(area_id, second_category_id, price_low, price_high, 1);
 	        	}else if(area_id != 0 && type_id != 0){
 	        		restaurants = RestaurantAPI.getAreaTypeRestaurantsByPrice(area_id, type_id, price_low, price_high, 1);
 	        	}else if(category_id != 0 && area_id == 0){
 	        		restaurants = RestaurantAPI.getCategoryRestaurantsByPrice(category_id, price_low, price_high, 1);
 	        	}else if(second_category_id != 0 && area_id == 0){
 	        		restaurants = RestaurantAPI.getSecondCategoryRestaurantsByPrice(second_category_id, price_low, price_high, 1);
 	        	}else if(area_id == 0 && type_id != 0){
 	        		restaurants = RestaurantAPI.getTypeRestaurantsByPrice(type_id,price_low, price_high, 1);
 	        	}
 
         	}else if(order == 4){
         		
         		if(area_id !=0 && category_id != 0){
 	        		restaurants = RestaurantAPI.getAreaCategoryRestaurantsByFood(area_id, category_id, price_low, price_high, 1);
 	        	}else if(area_id != 0 && rank_category_id != 0){
 	        		restaurants = RestaurantAPI.getAreaRankCategoryRestaurantsByFood(area_id, rank_category_id, price_low, price_high, 1);
 	        	}else if(area_id != 0 && second_category_id != 0){
 	        		restaurants = RestaurantAPI.getAreaSecondCategoryRestaurantsByFood(area_id, second_category_id, price_low, price_high, 1);
 	        	}else if(area_id != 0 && type_id != 0){
 	        		restaurants = RestaurantAPI.getAreaTypeRestaurantsByFood(area_id, type_id, price_low, price_high, 1);
 	        	}else if(category_id != 0 && area_id == 0){
 	        		restaurants = RestaurantAPI.getCategoryRestaurantsByFood(category_id, price_low, price_high, 1);
 	        	}else if(second_category_id != 0 && area_id == 0){
 	        		restaurants = RestaurantAPI.getSecondCategoryRestaurantsByFood(second_category_id, price_low, price_high, 1);
 	        	}else if(area_id == 0 && type_id != 0){
 	        		restaurants = RestaurantAPI.getTypeRestaurantsByFood(type_id,price_low, price_high, 1);
 	        	}
 
         	}
        
             return null;
         }
 
         @Override
         protected void onPostExecute(Object result) {
             super.onPostExecute(result);
             progressLayout.setVisibility(View.GONE);
             loadmoreLayout.setVisibility(View.GONE);
 
             if (restaurants != null && restaurants.size() != 0) {
                 try {
                     layoutReload.setVisibility(View.GONE);
                     myGridViewAdapter = new RestaurantGridViewAdapter(getActivity(), restaurants,is_show_distance);
                     myGrid.setAdapter(myGridViewAdapter);
                     if(is_show_distance)
                     	new DownloadRestaurantsDistanceTask().execute();
                 } catch (Exception e) {
 
                 }
             } else if(restaurants != null && restaurants.size() == 0){
             	noDataLayout.setVisibility(View.VISIBLE);
             	layoutReload.setVisibility(View.GONE);
             }else {
                 layoutReload.setVisibility(View.VISIBLE);
             }
 
         }
     }
 
     private class LoadMoreTask extends AsyncTask {
 
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
             loadmoreLayout.setVisibility(View.VISIBLE);
 
         }
 
         @Override
         protected Object doInBackground(Object... params) {
 
         	if(order == 0){
 	        	if(is_near && category_id != 0 && second_category_id!=0){
 	        		moreRestaurants = RestaurantAPI.getAroundRestaurants(latitude, longitude, myPage, category_id, second_category_id);
 	        	}else if(is_near && category_id != 0){
 	        		moreRestaurants = RestaurantAPI.getAroundRestaurants(latitude, longitude, myPage, category_id, 0);
 	        	}else if(is_near){
 	        		moreRestaurants = RestaurantAPI.getAroundRestaurants(latitude, longitude, myPage, 0, 0);
 	        	}else if(area_id !=0 && category_id != 0){
 	            	moreRestaurants = RestaurantAPI.getAreaCategoryRestaurants(area_id, category_id, myPage);
 	        	}else if(area_id != 0 && rank_category_id != 0){
 	        		moreRestaurants = RestaurantAPI.getAreaRankCategoryRestaurants(area_id, rank_category_id, myPage);
 	        	}else if(area_id != 0 && second_category_id != 0){
 	        		moreRestaurants = RestaurantAPI.getAreaSecondCategoryRestaurants(area_id, second_category_id, myPage);
 	        	}else if(area_id != 0 && type_id != 0){
 	        		moreRestaurants = RestaurantAPI.getAreaTypeRestaurants(area_id, type_id, myPage);
 	        	}else if(category_id != 0 && area_id == 0){
 	        		moreRestaurants = RestaurantAPI.getCategoryRestaurants(category_id, myPage);
 	        	}else if(second_category_id != 0 && area_id == 0){
 	        		moreRestaurants = RestaurantAPI.getSecondCategoryRestaurants(second_category_id, myPage);
 	        	}else if(area_id == 0 && type_id != 0){
 	        		moreRestaurants = RestaurantAPI.getTypeRestaurants(type_id, myPage);
 	        	}else if(is_selected){
 	        		moreRestaurants = RestaurantAPI.getSelectRestaurants(myPage);
 	        	}
         	}else if(order == 1){
         		
         		if(area_id !=0 && category_id != 0){
         			moreRestaurants = RestaurantAPI.getAreaCategoryRestaurantsByDistance(area_id, category_id, price_low, price_high, latitude, longitude, myPage);
 	        	}else if(area_id != 0 && rank_category_id != 0){
 	        		moreRestaurants = RestaurantAPI.getAreaRankCategoryRestaurantsByDistance(area_id, rank_category_id, price_low, price_high, latitude, longitude, myPage);
 	        	}else if(area_id != 0 && second_category_id != 0){
 	        		moreRestaurants = RestaurantAPI.getAreaSecondCategoryRestaurantsByDistance(area_id, second_category_id, price_low, price_high, latitude, longitude, myPage);
 	        	}else if(area_id != 0 && type_id != 0){
 	        		moreRestaurants = RestaurantAPI.getAreaTypeRestaurantsByDistance(area_id, type_id, price_low, price_high, latitude, longitude, myPage);
 	        	}else if(category_id != 0 && area_id == 0){
 	        		moreRestaurants = RestaurantAPI.getCategoryRestaurantsByDistance(category_id, price_low, price_high, latitude, longitude, myPage);
 	        	}else if(second_category_id != 0 && area_id == 0){
 	        		moreRestaurants = RestaurantAPI.getSecondCategoryRestaurantsByDistance(second_category_id, price_low, price_high, latitude, longitude, myPage);
 	        	}else if(area_id == 0 && type_id != 0){
 	        		moreRestaurants = RestaurantAPI.getTypeRestaurantsByDistance(type_id,price_low, price_high, latitude, longitude, myPage);
 	        	}
         		
         	}else if(order == 2){
         		
         		if(area_id !=0 && category_id != 0){
         			moreRestaurants = RestaurantAPI.getAreaCategoryRestaurantsByService(area_id, category_id, price_low, price_high, myPage);
 	        	}else if(area_id != 0 && rank_category_id != 0){
 	        		moreRestaurants = RestaurantAPI.getAreaRankCategoryRestaurantsByService(area_id, rank_category_id, price_low, price_high,myPage);
 	        	}else if(area_id != 0 && second_category_id != 0){
 	        		moreRestaurants = RestaurantAPI.getAreaSecondCategoryRestaurantsByService(area_id, second_category_id, price_low, price_high, myPage);
 	        	}else if(area_id != 0 && type_id != 0){
 	        		moreRestaurants = RestaurantAPI.getAreaTypeRestaurantsByService(area_id, type_id, price_low, price_high, myPage);
 	        	}else if(category_id != 0 && area_id == 0){
 	        		moreRestaurants = RestaurantAPI.getCategoryRestaurantsByService(category_id, price_low, price_high, myPage);
 	        	}else if(second_category_id != 0 && area_id == 0){
 	        		moreRestaurants = RestaurantAPI.getSecondCategoryRestaurantsByService(second_category_id, price_low, price_high, myPage);
 	        	}else if(area_id == 0 && type_id != 0){
 	        		moreRestaurants = RestaurantAPI.getTypeRestaurantsByService(type_id,price_low, price_high, myPage);
 	        	}
         	}else if(order == 3){
         		
         		if(area_id !=0 && category_id != 0){
         			moreRestaurants = RestaurantAPI.getAreaCategoryRestaurantsByPrice(area_id, category_id, price_low, price_high, myPage);
 	        	}else if(area_id != 0 && rank_category_id != 0){
	        		moreRestaurants = RestaurantAPI.getAreaRankCategoryRestaurantsByPrice(area_id, rank_category_id, price_low, price_high, myPage);
 	        	}else if(area_id != 0 && second_category_id != 0){
 	        		moreRestaurants = RestaurantAPI.getAreaSecondCategoryRestaurantsByPrice(area_id, second_category_id, price_low, price_high, myPage);
 	        	}else if(area_id != 0 && type_id != 0){
 	        		moreRestaurants = RestaurantAPI.getAreaTypeRestaurantsByPrice(area_id, type_id, price_low, price_high, myPage);
 	        	}else if(category_id != 0 && area_id == 0){
 	        		moreRestaurants = RestaurantAPI.getCategoryRestaurantsByPrice(category_id, price_low, price_high, myPage);
 	        	}else if(second_category_id != 0 && area_id == 0){
 	        		moreRestaurants = RestaurantAPI.getSecondCategoryRestaurantsByPrice(second_category_id, price_low, price_high, myPage);
 	        	}else if(area_id == 0 && type_id != 0){
 	        		moreRestaurants = RestaurantAPI.getTypeRestaurantsByPrice(type_id,price_low, price_high, myPage);
 	        	}
         	}else if(order == 4){
         		
         		if(area_id !=0 && category_id != 0){
         			moreRestaurants = RestaurantAPI.getAreaCategoryRestaurantsByFood(area_id, category_id, price_low, price_high, myPage);
 	        	}else if(area_id != 0 && rank_category_id != 0){
 	        		moreRestaurants = RestaurantAPI.getAreaRankCategoryRestaurantsByFood(area_id, rank_category_id, price_low, price_high, myPage);
 	        	}else if(area_id != 0 && second_category_id != 0){
 	        		moreRestaurants = RestaurantAPI.getAreaSecondCategoryRestaurantsByFood(area_id, second_category_id, price_low, price_high, myPage);
 	        	}else if(area_id != 0 && type_id != 0){
 	        		moreRestaurants = RestaurantAPI.getAreaTypeRestaurantsByFood(area_id, type_id, price_low, price_high, myPage);
 	        	}else if(category_id != 0 && area_id == 0){
 	        		moreRestaurants = RestaurantAPI.getCategoryRestaurantsByFood(category_id, price_low, price_high, myPage);
 	        	}else if(second_category_id != 0 && area_id == 0){
 	        		moreRestaurants = RestaurantAPI.getSecondCategoryRestaurantsByFood(second_category_id, price_low, price_high, myPage);
 	        	}else if(area_id == 0 && type_id != 0){
 	        		moreRestaurants = RestaurantAPI.getTypeRestaurantsByFood(type_id,price_low, price_high, myPage);
 	        	}
 
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
                 myGridViewAdapter.notifyDataSetChanged();
                 new DownloadRestaurantsDistanceTask().execute();
             } else {
                 checkLoad = false;
                 Toast.makeText(getActivity(), "no more data", Toast.LENGTH_SHORT).show();
             }
             myGrid.onLoadMoreComplete();
 
         }
     }
     
     private class DownloadRestaurantsDistanceTask extends AsyncTask {
 
 		@Override
 		protected Object doInBackground(Object... arg0) {
 			restaurants = RestaurantAPI.getRestaurantsDistance(latitude, longitude, restaurants);
 			return null;
 		}
 		
 		@Override
         protected void onPostExecute(Object result) {
             super.onPostExecute(result);
             loadmoreLayout.setVisibility(View.GONE);
             myGridViewAdapter.notifyDataSetChanged();
             
         }
     }
     
 
 }
