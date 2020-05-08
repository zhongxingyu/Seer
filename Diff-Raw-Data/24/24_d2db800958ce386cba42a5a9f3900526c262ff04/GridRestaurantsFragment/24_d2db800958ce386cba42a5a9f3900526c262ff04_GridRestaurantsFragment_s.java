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
         	
         	if(is_near && category_id != 0 && second_category_id!=0){
        		restaurants = RestaurantAPI.getAroundRestaurants(latitude, longitude, 0.5, category_id, second_category_id);
         	}else if(is_near && category_id != 0){
        		restaurants = RestaurantAPI.getAroundRestaurants(latitude, longitude, 0.5, category_id, 0);
         	}else if(is_near){
        		restaurants = RestaurantAPI.getAroundRestaurants(latitude, longitude, 0.5, 0, 0);
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
                     myGridViewAdapter = new RestaurantGridViewAdapter(getActivity(), restaurants);
                     myGrid.setAdapter(myGridViewAdapter);
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
