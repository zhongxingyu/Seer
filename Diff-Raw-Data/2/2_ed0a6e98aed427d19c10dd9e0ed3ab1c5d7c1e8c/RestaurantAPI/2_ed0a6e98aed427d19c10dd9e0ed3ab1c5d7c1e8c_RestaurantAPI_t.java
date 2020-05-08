 package com.restaurant.collection.api;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.util.Log;
 
 import com.restaurant.collection.entity.Area;
 import com.restaurant.collection.entity.Category;
 import com.restaurant.collection.entity.Note;
 import com.restaurant.collection.entity.Restaurant;
 
 public class RestaurantAPI {
 	final static String         HOST  = "http://106.187.100.252";
     public static final String  TAG   = "RESTAURANT_API";
     public static final boolean DEBUG = true;
     
     
     public static ArrayList<Area> getAreas() {
         return Area.getAreas();
     }
     
     public static ArrayList<Category> getCategories() {
         return Category.getCategories();
     }
     
     public static ArrayList<Restaurant> getSelectRestaurants(int page){
     	String message = getMessageFromServer("GET", "/api/v1/restaurants/select_restaurants?page="+page , null, null);
     	ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
     	if (message == null) {
             return null;
         } else {
             return parseSelectRestaurants(message, restaurants);
         }
     }
     
     
     public static ArrayList<Restaurant> getAllRestaurant(){
     	String message = getMessageFromServer("GET", "/api/v1/restaurants/all" , null, null);
     	ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
     	if (message == null) {
             return null;
         } else {
             return parseAllRestaurants(message, restaurants);
         }
     }
     
     
     public static Restaurant getRestaurant(int restaurant_id){
     	String message = getMessageFromServer("GET", "/api/v1/restaurants/"+ restaurant_id , null, null);
     	Restaurant restaurant = new Restaurant();
     	if (message == null) {
             return null;
         } else {
             return parseRestaurant(message, restaurant);
         }
     }
     
     public static ArrayList<Restaurant> getAreaCategoryRestaurants(int area_id,int category_id, int page) {
         String message = getMessageFromServer("GET", "/api/v1/restaurants?area_id=" + area_id +"&category_id="+ category_id+ "&page=" + page, null, null);
         ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
         if (message == null) {
             return null;
         } else {
             return parseRestaurants(message, restaurants);
         }
     }
     
     public static ArrayList<Restaurant> getAreaRankCategoryRestaurants(int area_id,int rank_category_id, int page) {
         String message = getMessageFromServer("GET", "/api/v1/restaurants?area_id=" + area_id +"&rank_category_id="+ rank_category_id+ "&page=" + page, null, null);
         ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
         if (message == null) {
             return null;
         } else {
             return parseRestaurants(message, restaurants);
         }
     }
     
     
     public static ArrayList<Restaurant> getAreaRestaurants(int area_id) {
         String message = getMessageFromServer("GET", "/api/v1/restaurants?area_id=" + area_id, null, null);
         ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
         if (message == null) {
             return null;
         } else {
             return parseAllRestaurants(message, restaurants);
         }
     }
     
     public static ArrayList<Restaurant> getRankCategoryRestaurants(int rank_category_id, int page) {
         String message = getMessageFromServer("GET", "/api/v1/restaurants?area_id=1&rank_category_id=" + rank_category_id + "&page=" + page, null, null);
         ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
         if (message == null) {
             return null;
         } else {
             return parseRestaurants(message, restaurants);
         }
     }
     
     public static ArrayList<Restaurant> getAreaRestaurantsByPrice(int area_id,int price_low, int price_high, int page) {
         String message = getMessageFromServer("GET", "/api/v1/restaurants/area_restaurants?area_id=" + area_id+"&price_low="+price_low+"&price_high="+price_high+ "&page=" + page, null, null);
         ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
         if (message == null) {
             return null;
         } else {
             return parseAllRestaurants(message, restaurants);
         }
     }
     
     public static ArrayList<Restaurant> getAreaRestaurantsByService(int area_id,int price_low, int price_high, int page) {
         String message = getMessageFromServer("GET", "/api/v1/restaurants/area_restaurants?area_id=" + area_id+"&price_low="+price_low+"&price_high="+price_high+"&is_service_order=true"+ "&page=" + page, null, null);
         ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
         if (message == null) {
             return null;
         } else {
             return parseAllRestaurants(message, restaurants);
         }
     }
     
     public static ArrayList<Restaurant> getAreaRestaurantsByFood(int area_id,int price_low, int price_high, int page) {
         String message = getMessageFromServer("GET", "/api/v1/restaurants/area_restaurants?area_id=" + area_id+"&price_low="+price_low+"&price_high="+price_high+"&is_food_order=true"+ "&page=" + page, null, null);
         ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
         if (message == null) {
             return null;
         } else {
             return parseAllRestaurants(message, restaurants);
         }
     }
     
    public static ArrayList<Restaurant> getAreaRestaurantsByDistance(int area_id,int price_low, int price_high,double x, double y, int page) {
 	   	String x_string = String.valueOf(x);
 	   	String y_string = String.valueOf(y);
         String message = getMessageFromServer("GET", "/api/v1/restaurants/area_restaurants?area_id=" + area_id+"&price_low="+price_low+"&price_high="+price_high+"&is_dis_order=true&x="+x_string+"&y="+y_string+ "&page=" + page, null, null);
         ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
         if (message == null) {
             return null;
         } else {
             return parseAllRestaurants(message, restaurants);
         }
     }
     
     public static ArrayList<Restaurant> getCategoryRestaurants(int category_id, int page) {
         String message = getMessageFromServer("GET", "/api/v1/restaurants?category_id=" + category_id + "&page=" + page, null, null);
         ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
         if (message == null) {
             return null;
         } else {
             return parseRestaurants(message, restaurants);
         }
     }
     
     public static ArrayList<Restaurant> getCategoryRestaurantsByPrice(int category_id, int price_low, int price_high, int page) {
         String message = getMessageFromServer("GET", "/api/v1/restaurants/category_restaurants?category_id=" + category_id+"&price_low="+price_low+"&price_high="+price_high + "&page=" + page, null, null);
         ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
         if (message == null) {
             return null;
         } else {
             return parseRestaurants(message, restaurants);
         }
     }
     
     public static ArrayList<Restaurant> getAreaCategoryRestaurantsByPrice(int area_id,int category_id, int price_low, int price_high, int page) {
         String message = getMessageFromServer("GET", "/api/v1/restaurants/category_restaurants?area_id="+area_id+"&category_id=" + category_id+"&price_low="+price_low+"&price_high="+price_high + "&page=" + page, null, null);
         ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
         if (message == null) {
             return null;
         } else {
             return parseRestaurants(message, restaurants);
         }
     }
     
     public static ArrayList<Restaurant> getCategoryRestaurantsByService(int category_id, int price_low, int price_high, int page) {
         String message = getMessageFromServer("GET", "/api/v1/restaurants/category_restaurants?category_id=" + category_id+"&price_low="+price_low+"&price_high="+price_high +"&is_service_order=true"+ "&page=" + page, null, null);
         ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
         if (message == null) {
             return null;
         } else {
             return parseRestaurants(message, restaurants);
         }
     }
     
     public static ArrayList<Restaurant> getCategoryRestaurantsByFood(int category_id, int price_low, int price_high, int page) {
         String message = getMessageFromServer("GET", "/api/v1/restaurants/category_restaurants?category_id=" + category_id+"&price_low="+price_low+"&price_high="+price_high +"&is_food_order=true"+ "&page=" + page, null, null);
         ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
         if (message == null) {
             return null;
         } else {
             return parseRestaurants(message, restaurants);
         }
     }
     
     public static ArrayList<Restaurant> getAreaCategoryRestaurantsByService(int area_id,int category_id, int price_low, int price_high, int page) {
         String message = getMessageFromServer("GET", "/api/v1/restaurants/category_restaurants?area_id="+area_id+"&category_id=" + category_id+"&price_low="+price_low+"&price_high="+price_high +"&is_service_order=true"+ "&page=" + page, null, null);
         ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
         if (message == null) {
             return null;
         } else {
             return parseRestaurants(message, restaurants);
         }
     }
     
     public static ArrayList<Restaurant> getAreaCategoryRestaurantsByFood(int area_id,int category_id, int price_low, int price_high, int page) {
         String message = getMessageFromServer("GET", "/api/v1/restaurants/category_restaurants?area_id="+area_id+"&category_id=" + category_id+"&price_low="+price_low+"&price_high="+price_high +"&is_food_order=true"+ "&page=" + page, null, null);
         ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
         if (message == null) {
             return null;
         } else {
             return parseRestaurants(message, restaurants);
         }
     }
     
     public static ArrayList<Restaurant> getCategoryRestaurantsByDistance(int category_id, int price_low, int price_high,double x, double y, int page) {
     	String x_string = String.valueOf(x);
 	   	String y_string = String.valueOf(y);
         String message = getMessageFromServer("GET", "/api/v1/restaurants/category_restaurants?category_id=" + category_id+"&price_low="+price_low+"&price_high="+price_high +"&is_dis_order=true&x="+x_string+"&y="+y_string+ "&page=" + page, null, null);
         ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
         if (message == null) {
             return null;
         } else {
             return parseRestaurants(message, restaurants);
         }
     }
     
     public static ArrayList<Restaurant> getAreaCategoryRestaurantsByDistance(int area_id,int category_id, int price_low, int price_high,double x, double y, int page) {
     	String x_string = String.valueOf(x);
 	   	String y_string = String.valueOf(y);
         String message = getMessageFromServer("GET", "/api/v1/restaurants/category_restaurants?area_id="+area_id+"&category_id=" + category_id+"&price_low="+price_low+"&price_high="+price_high +"&is_dis_order=true&x="+x_string+"&y="+y_string+ "&page=" + page, null, null);
         ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
         if (message == null) {
             return null;
         } else {
             return parseRestaurants(message, restaurants);
         }
     }
     
    public static ArrayList<Restaurant> getSecondCategoryRestaurants(int sec_c_id, int page) {
         String message = getMessageFromServer("GET", "/api/v1/restaurants?sec_c_id=" + sec_c_id + "&page=" + page, null, null);
         ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
         if (message == null) {
             return null;
         } else {
             return parseRestaurants(message, restaurants);
         }
     }
     
     public static ArrayList<Restaurant> getSecondCategoryRestaurantsByPrice(int second_category_id, int price_low, int price_high, int page) {
         String message = getMessageFromServer("GET", "/api/v1/restaurants/second_restaurants?sec_c_id=" + second_category_id+"&price_low="+price_low+"&price_high="+price_high + "&page=" + page, null, null);
         ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
         if (message == null) {
             return null;
         } else {
             return parseRestaurants(message, restaurants);
         }
     }
     
     public static ArrayList<Restaurant> getAreaSecondCategoryRestaurantsByPrice(int area_id,int second_category_id, int price_low, int price_high, int page) {
         String message = getMessageFromServer("GET", "/api/v1/restaurants/second_restaurants?area_id="+area_id+"&sec_c_id=" + second_category_id+"&price_low="+price_low+"&price_high="+price_high + "&page=" + page, null, null);
         ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
         if (message == null) {
             return null;
         } else {
             return parseRestaurants(message, restaurants);
         }
     }
     
     public static ArrayList<Restaurant> getSecondCategoryRestaurantsByService(int second_category_id, int price_low, int price_high, int page) {
         String message = getMessageFromServer("GET", "/api/v1/restaurants/second_restaurants?sec_c_id=" + second_category_id+"&price_low="+price_low+"&price_high="+price_high +"&is_service_order=true"+ "&page=" + page, null, null);
         ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
         if (message == null) {
             return null;
         } else {
             return parseRestaurants(message, restaurants);
         }
     }
     
     public static ArrayList<Restaurant> getSecondCategoryRestaurantsByFood(int second_category_id, int price_low, int price_high, int page) {
         String message = getMessageFromServer("GET", "/api/v1/restaurants/second_restaurants?sec_c_id=" + second_category_id+"&price_low="+price_low+"&price_high="+price_high +"&is_food_order=true"+ "&page=" + page, null, null);
         ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
         if (message == null) {
             return null;
         } else {
             return parseRestaurants(message, restaurants);
         }
     }
     
     public static ArrayList<Restaurant> getAreaSecondCategoryRestaurantsByService(int area_id,int second_category_id, int price_low, int price_high, int page) {
         String message = getMessageFromServer("GET", "/api/v1/restaurants/second_restaurants?area_id="+area_id+"&sec_c_id=" + second_category_id+"&price_low="+price_low+"&price_high="+price_high +"&is_service_order=true"+ "&page=" + page, null, null);
         ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
         if (message == null) {
             return null;
         } else {
             return parseRestaurants(message, restaurants);
         }
     }
     
     public static ArrayList<Restaurant> getAreaSecondCategoryRestaurantsByFood(int area_id,int second_category_id, int price_low, int price_high, int page) {
         String message = getMessageFromServer("GET", "/api/v1/restaurants/second_restaurants?area_id="+area_id+"&sec_c_id=" + second_category_id+"&price_low="+price_low+"&price_high="+price_high +"&is_food_order=true"+ "&page=" + page, null, null);
         ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
         if (message == null) {
             return null;
         } else {
             return parseRestaurants(message, restaurants);
         }
     }
     
     public static ArrayList<Restaurant> getSecondCategoryRestaurantsByDistance(int second_category_id, int price_low, int price_high,double x, double y, int page) {
     	String x_string = String.valueOf(x);
 	   	String y_string = String.valueOf(y);
         String message = getMessageFromServer("GET", "/api/v1/restaurants/second_restaurants?sec_c_id=" + second_category_id+"&price_low="+price_low+"&price_high="+price_high +"&is_dis_order=true&x="+x_string+"&y="+y_string+ "&page=" + page, null, null);
         ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
         if (message == null) {
             return null;
         } else {
             return parseRestaurants(message, restaurants);
         }
     }
     
     public static ArrayList<Restaurant> getAreaSecondCategoryRestaurantsByDistance(int area_id,int second_category_id, int price_low, int price_high,double x, double y, int page) {
     	String x_string = String.valueOf(x);
 	   	String y_string = String.valueOf(y);
        String message = getMessageFromServer("GET", "/api/v1/restaurants/category_restaurants?area_id="+area_id+"&sec_c_id=" + second_category_id+"&price_low="+price_low+"&price_high="+price_high +"&is_dis_order=true&x="+x_string+"&y="+y_string+ "&page=" + page, null, null);
         ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
         if (message == null) {
             return null;
         } else {
             return parseRestaurants(message, restaurants);
         }
     }
     
     public static ArrayList<Restaurant> getTypeRestaurants(int type_id, int page) {
         String message = getMessageFromServer("GET", "/api/v1/restaurants?area_id=1&type_id=" + type_id + "&page=" + page, null, null);
         ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
         if (message == null) {
             return null;
         } else {
             return parseRestaurants(message, restaurants);
         }
     }
     
     public static ArrayList<Restaurant> getRankCategoryRestaurantsByPrice(int rank_category_id, int price_low, int price_high, int page) {
         String message = getMessageFromServer("GET", "/api/v1/restaurants/rank_restaurants?rank_category_id=" + rank_category_id+"&price_low="+price_low+"&price_high="+price_high + "&page=" + page, null, null);
         ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
         if (message == null) {
             return null;
         } else {
             return parseRestaurants(message, restaurants);
         }
     }
     
     public static ArrayList<Restaurant> getAreaRankCategoryRestaurantsByPrice(int area_id,int rank_category_id, int price_low, int price_high, int page) {
         String message = getMessageFromServer("GET", "/api/v1/restaurants/rank_restaurants?area_id="+area_id+"&rank_category_id=" + rank_category_id+"&price_low="+price_low+"&price_high="+price_high + "&page=" + page, null, null);
         ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
         if (message == null) {
             return null;
         } else {
             return parseRestaurants(message, restaurants);
         }
     }
     
     public static ArrayList<Restaurant> getRankCategoryRestaurantsByService(int rank_category_id, int price_low, int price_high, int page) {
         String message = getMessageFromServer("GET", "/api/v1/restaurants/rank_restaurants?rank_category_id=" + rank_category_id+"&price_low="+price_low+"&price_high="+price_high +"&is_service_order=true"+ "&page=" + page, null, null);
         ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
         if (message == null) {
             return null;
         } else {
             return parseRestaurants(message, restaurants);
         }
     }
     
     public static ArrayList<Restaurant> getRankCategoryRestaurantsByFood(int rank_category_id, int price_low, int price_high, int page) {
         String message = getMessageFromServer("GET", "/api/v1/restaurants/rank_restaurants?rank_category_id=" + rank_category_id+"&price_low="+price_low+"&price_high="+price_high +"&is_food_order=true"+ "&page=" + page, null, null);
         ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
         if (message == null) {
             return null;
         } else {
             return parseRestaurants(message, restaurants);
         }
     }
     
     public static ArrayList<Restaurant> getAreaRankCategoryRestaurantsByService(int area_id,int rank_category_id, int price_low, int price_high, int page) {
         String message = getMessageFromServer("GET", "/api/v1/restaurants/rank_restaurants?area_id="+area_id+"&rank_category_id=" + rank_category_id+"&price_low="+price_low+"&price_high="+price_high +"&is_service_order=true"+ "&page=" + page, null, null);
         ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
         if (message == null) {
             return null;
         } else {
             return parseRestaurants(message, restaurants);
         }
     }
     
     public static ArrayList<Restaurant> getAreaRankCategoryRestaurantsByFood(int area_id,int rank_category_id, int price_low, int price_high, int page) {
         String message = getMessageFromServer("GET", "/api/v1/restaurants/rank_restaurants?area_id="+area_id+"&rank_category_id=" + rank_category_id+"&price_low="+price_low+"&price_high="+price_high +"&is_food_order=true"+ "&page=" + page, null, null);
         ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
         if (message == null) {
             return null;
         } else {
             return parseRestaurants(message, restaurants);
         }
     }
     
     public static ArrayList<Restaurant> getRankCategoryRestaurantsByDistance(int rank_category_id, int price_low, int price_high,double x, double y, int page) {
     	String x_string = String.valueOf(x);
 	   	String y_string = String.valueOf(y);
         String message = getMessageFromServer("GET", "/api/v1/restaurants/rank_restaurants?rank_category_id=" + rank_category_id+"&price_low="+price_low+"&price_high="+price_high +"&is_dis_order=true&x="+x_string+"&y="+y_string+ "&page=" + page, null, null);
         ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
         if (message == null) {
             return null;
         } else {
             return parseRestaurants(message, restaurants);
         }
     }
     
     public static ArrayList<Restaurant> getAreaRankCategoryRestaurantsByDistance(int area_id,int rank_category_id, int price_low, int price_high,double x, double y, int page) {
     	String x_string = String.valueOf(x);
 	   	String y_string = String.valueOf(y);
         String message = getMessageFromServer("GET", "/api/v1/restaurants/rank_restaurants?area_id="+area_id+"&rank_category_id=" + rank_category_id+"&price_low="+price_low+"&price_high="+price_high +"&is_dis_order=true&x="+x_string+"&y="+y_string+ "&page=" + page, null, null);
         ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
         if (message == null) {
             return null;
         } else {
             return parseRestaurants(message, restaurants);
         }
     }
     
     
     public static ArrayList<Restaurant> getTypeRestaurantsByPrice(int type_id, int price_low, int price_high, int page) {
         String message = getMessageFromServer("GET", "/api/v1/restaurants/type_restaurants?type_id=" + type_id+"&price_low="+price_low+"&price_high="+price_high + "&page=" + page, null, null);
         ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
         if (message == null) {
             return null;
         } else {
             return parseRestaurants(message, restaurants);
         }
     }
     
     public static ArrayList<Restaurant> getAreaTypeRestaurantsByPrice(int area_id,int type_id, int price_low, int price_high, int page) {
         String message = getMessageFromServer("GET", "/api/v1/restaurants/type_restaurants?area_id="+area_id+"&type_id=" + type_id+"&price_low="+price_low+"&price_high="+price_high + "&page=" + page, null, null);
         ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
         if (message == null) {
             return null;
         } else {
             return parseRestaurants(message, restaurants);
         }
     }
     
     public static ArrayList<Restaurant> getTypeRestaurantsByService(int type_id, int price_low, int price_high, int page) {
         String message = getMessageFromServer("GET", "/api/v1/restaurants/type_restaurants?type_id=" + type_id+"&price_low="+price_low+"&price_high="+price_high +"&is_service_order=true"+ "&page=" + page, null, null);
         ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
         if (message == null) {
             return null;
         } else {
             return parseRestaurants(message, restaurants);
         }
     }
     
     public static ArrayList<Restaurant> getTypeRestaurantsByFood(int type_id, int price_low, int price_high, int page) {
         String message = getMessageFromServer("GET", "/api/v1/restaurants/type_restaurants?type_id=" + type_id+"&price_low="+price_low+"&price_high="+price_high +"&is_food_order=true"+ "&page=" + page, null, null);
         ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
         if (message == null) {
             return null;
         } else {
             return parseRestaurants(message, restaurants);
         }
     }
     
     public static ArrayList<Restaurant> getAreaTypeRestaurantsByService(int area_id,int type_id, int price_low, int price_high, int page) {
         String message = getMessageFromServer("GET", "/api/v1/restaurants/type_restaurants?area_id="+area_id+"&type_id=" + type_id+"&price_low="+price_low+"&price_high="+price_high +"&is_service_order=true"+ "&page=" + page, null, null);
         ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
         if (message == null) {
             return null;
         } else {
             return parseRestaurants(message, restaurants);
         }
     }
     
     public static ArrayList<Restaurant> getAreaTypeRestaurantsByFood(int area_id,int type_id, int price_low, int price_high, int page) {
         String message = getMessageFromServer("GET", "/api/v1/restaurants/type_restaurants?area_id="+area_id+"&type_id=" + type_id+"&price_low="+price_low+"&price_high="+price_high +"&is_food_order=true"+ "&page=" + page, null, null);
         ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
         if (message == null) {
             return null;
         } else {
             return parseRestaurants(message, restaurants);
         }
     }
     
     public static ArrayList<Restaurant> getTypeRestaurantsByDistance(int type_id, int price_low, int price_high,double x, double y, int page) {
     	String x_string = String.valueOf(x);
 	   	String y_string = String.valueOf(y);
         String message = getMessageFromServer("GET", "/api/v1/restaurants/type_restaurants?type_id=" + type_id+"&price_low="+price_low+"&price_high="+price_high +"&is_dis_order=true&x="+x_string+"&y="+y_string+ "&page=" + page, null, null);
         ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
         if (message == null) {
             return null;
         } else {
             return parseRestaurants(message, restaurants);
         }
     }
     
     public static ArrayList<Restaurant> getAreaTypeRestaurantsByDistance(int area_id,int type_id, int price_low, int price_high,double x, double y, int page) {
     	String x_string = String.valueOf(x);
 	   	String y_string = String.valueOf(y);
         String message = getMessageFromServer("GET", "/api/v1/restaurants/type_restaurants?area_id="+area_id+"&type_id=" + type_id+"&price_low="+price_low+"&price_high="+price_high +"&is_dis_order=true&x="+x_string+"&y="+y_string+ "&page=" + page, null, null);
         ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
         if (message == null) {
             return null;
         } else {
             return parseRestaurants(message, restaurants);
         }
     }
     
     public static ArrayList<Restaurant> getAreaSecondCategoryRestaurants(int area_id,int sec_c_id, int page) {
         String message = getMessageFromServer("GET", "/api/v1/restaurants?area_id=" + area_id +"&sec_c_id="+ sec_c_id+ "&page=" + page, null, null);
         ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
         if (message == null) {
             return null;
         } else {
             return parseRestaurants(message, restaurants);
         }
     }
     
     public static ArrayList<Restaurant> getAreaTypeRestaurants(int area_id,int type_id, int page) {
         String message = getMessageFromServer("GET", "/api/v1/restaurants?area_id=" + area_id +"&type_id="+ type_id+ "&page=" + page, null, null);
         ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
         if (message == null) {
             return null;
         } else {
             return parseRestaurants(message, restaurants);
         }
     }
    
     
     public static ArrayList<Note> getSelectNotes(int page) {
         String message = getMessageFromServer("GET", "/api/v1/notes/select_notes?page="+page, null, null);
         ArrayList<Note> notes = new ArrayList<Note>();
         if (message == null) {
             return null;
         } else {
             return parseNotes(message, notes);
         }
     }
     
    public static ArrayList<Note> getRestaurantNotes(int restaurant_id, int page) {
        String message = getMessageFromServer("GET", "/api/v1/notes/"+ restaurant_id +"&page=" + page, null, null);
        ArrayList<Note> notes = new ArrayList<Note>();
        if (message == null) {
            return null;
        } else {
            return parseNotes(message, notes);
        }
    }
    
    public static ArrayList<Note> getAreaNotes(int area_id, int page) {
        String message = getMessageFromServer("GET", "/api/v1/notes?area_id="+ area_id +"&page=" + page, null, null);
        ArrayList<Note> notes = new ArrayList<Note>();
        if (message == null) {
            return null;
        } else {
            return parseNotes(message, notes);
        }
    }
    
    public static ArrayList<Note> getTypeNotes(int type_id, int page) {
        String message = getMessageFromServer("GET", "/api/v1/notes?type_id="+ type_id +"&page=" + page, null, null);
        ArrayList<Note> notes = new ArrayList<Note>();
        if (message == null) {
            return null;
        } else {
            return parseNotes(message, notes);
        }
    }
    
    public static ArrayList<Note> getCategoryaNotes(int category_id, int page) {
        String message = getMessageFromServer("GET", "/api/v1/notes?category_id="+ category_id +"&page=" + page, null, null);
        ArrayList<Note> notes = new ArrayList<Note>();
        if (message == null) {
            return null;
        } else {
            return parseNotes(message, notes);
        }
    }
    
    public static ArrayList<Note> getSecondCategoryaNotes(int second_category_id, int page) {
        String message = getMessageFromServer("GET", "/api/v1/notes/second_notes?sec_c_id="+ second_category_id +"&page=" + page, null, null);
        ArrayList<Note> notes = new ArrayList<Note>();
        if (message == null) {
            return null;
        } else {
            return parseNotes(message, notes);
        }
    }
    
    public static ArrayList<Note> getAreaCategoryNotes(int area_id, int category_id,int page) {
        String message = getMessageFromServer("GET", "/api/v1/notes?area_id="+ area_id  +"&category_id="+ category_id+ "&page=" + page, null, null);
        ArrayList<Note> notes = new ArrayList<Note>();
        if (message == null) {
            return null;
        } else {
            return parseNotes(message, notes);
        }
    }
    
    public static ArrayList<Note> getAreaSecondCategoryNotes(int area_id, int second_category_id,int page) {
        String message = getMessageFromServer("GET", "/api/v1/notes/second_notes?area_id="+ area_id  +"&sec_c_id="+ second_category_id+ "&page=" + page, null, null);
        ArrayList<Note> notes = new ArrayList<Note>();
        if (message == null) {
            return null;
        } else {
            return parseNotes(message, notes);
        }
    }
    
    public static ArrayList<Note> getAreaRankCategoryNotes(int area_id, int rank_category_id,int page) {
        String message = getMessageFromServer("GET", "/api/v1/notes?area_id="+ area_id  +"&rank_category_id="+ rank_category_id+ "&page=" + page, null, null);
        ArrayList<Note> notes = new ArrayList<Note>();
        if (message == null) {
            return null;
        } else {
            return parseNotes(message, notes);
        }
    }
    
    public static ArrayList<Note> getAreaTypeNotes(int area_id, int type_id,int page) {
        String message = getMessageFromServer("GET", "/api/v1/notes?area_id="+ area_id  +"&type_id="+ type_id+ "&page=" + page, null, null);
        ArrayList<Note> notes = new ArrayList<Note>();
        if (message == null) {
            return null;
        } else {
            return parseNotes(message, notes);
        }
    }
    
    // for unselect category, put 0 to id
    public static ArrayList<Restaurant> getAroundRestaurants(double x, double y,int page, int category_id, int second_category_id) {
 	   String message ="";
 	   if(category_id != 0 && second_category_id !=0){
 		   message = getMessageFromServer("GET", "/api/v1/restaurants/around_restaurates?x="+x+"&y="+ y + "&page="+ page + "&category_id=" + category_id + "&sec_c_id="+ second_category_id, null, null);   
 	   }else if(category_id != 0 && second_category_id ==0){
 		   message = getMessageFromServer("GET", "/api/v1/restaurants/around_restaurates?x="+x+"&y="+ y + "&page="+ page + "&category_id=" + category_id, null, null); 
 	   }else{
 		   message = getMessageFromServer("GET", "/api/v1/restaurants/around_restaurates?x="+x+"&y="+ y + "&page="+ page , null, null); 
 	   }
 		   
        ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
        if (message == null) {
            return null;
        } else {
            return parseRestaurants(message, restaurants);
        }
    }
    
    
    public static ArrayList<Restaurant> searchRestaurants(String keyword, int page){
 	   String query;
        try {
            query = URLEncoder.encode(keyword, "utf-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
            return null;
        }
        
 	   String message ="";
 	   message = getMessageFromServer("GET", "/api/v1/restaurants/search?&page="+ page+"&keyword="+query , null, null); 
 	   ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
        if (message == null) {
            return null;
        } else {
            return parseRestaurants(message, restaurants);
        }
    }
    
    private static ArrayList<Note> parseNotes(String message, ArrayList<Note> notes) {
        try {
            JSONArray jArray;
            jArray = new JSONArray(message.toString());
            for (int i = 0; i < jArray.length(); i++) {
 
                int id = jArray.getJSONObject(i).getInt("id");
                int restaurant_id = jArray.getJSONObject(i).getInt("restaurant_id");
                String title = jArray.getJSONObject(i).getString("title");
                String author = jArray.getJSONObject(i).getString("author");
                String pic_url = jArray.getJSONObject(i).getString("pic_url");
                String pub_date = jArray.getJSONObject(i).getString("pub_date");
                String link = jArray.getJSONObject(i).getString("ipeen_link");
                double x_lat = jArray.getJSONObject(i).getDouble("x_lat");
                double y_long = jArray.getJSONObject(i).getDouble("y_long");
                
                int rank = 0;
                if (!jArray.getJSONObject(i).isNull("rank"))
                    rank = jArray.getJSONObject(i).getInt("rank");
 
                Note note = new Note(id, restaurant_id, title, author, pic_url, pub_date, link, x_lat, y_long);
                notes.add(note);
            }
 
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return notes;
    }
     
    private static Restaurant parseRestaurant(String message, Restaurant restaurant) {
        try {
            
     	   JSONObject jObject = new JSONObject(message.toString());	   	
     	   
                int id = jObject.getInt("id");
                String name = jObject.getString("name");
                String pic_url = jObject.getString("pic_url");
                String grade_food = jObject.getString("grade_food");
                String grade_service = jObject.getString("grade_service");
                String grade_ambiance = jObject.getString("grade_ambiance");
                String price = jObject.getString("price");
                String open_time = jObject.getString("open_time");
                String rest_date = jObject.getString("rest_date");
                String address = jObject.getString("address");
                String phone = jObject.getString("phone");
                int rate_num = jObject.getInt("rate_num");
                String introduction = jObject.getString("introduction");
                String official_link = jObject.getString("official_link");            
                String recommand_dish = jObject.getString("recommand_dish");
                
                double x_lat = jObject.getDouble("x_lat");
                double y_long = jObject.getDouble("y_long");
                
                int rank = 0;
                if (!jObject.isNull("rank"))
                    rank = jObject.getInt("rank");
 
                restaurant = new Restaurant(id, name, pic_url,
             		   grade_food, grade_service,  grade_ambiance,
             		   price, open_time, rest_date, address, 
                		   phone, rate_num, introduction, 
                		   official_link, recommand_dish, x_lat, y_long,"");
                
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return restaurant;
    }
     
     private static ArrayList<Restaurant> parseRestaurants(String message, ArrayList<Restaurant> restaurants) {
         try {
             JSONArray jArray;
             jArray = new JSONArray(message.toString());
             for (int i = 0; i < jArray.length(); i++) {
 
                 int id = jArray.getJSONObject(i).getInt("id");
                 String name = jArray.getJSONObject(i).getString("name");
                 String grade_food = jArray.getJSONObject(i).getString("grade_food");
                 String grade_service = jArray.getJSONObject(i).getString("grade_service");
                 String pic_url = jArray.getJSONObject(i).getString("pic_url");
                 String price = jArray.getJSONObject(i).getString("price");
                 double x_lat = jArray.getJSONObject(i).getDouble("x_lat");
                 double y_long = jArray.getJSONObject(i).getDouble("y_long");
                 
                 int rank = 0;
                 if (!jArray.getJSONObject(i).isNull("rank"))
                     rank = jArray.getJSONObject(i).getInt("rank");
                 
                 Restaurant restaurant = new Restaurant(id, name,pic_url, grade_food,
                 		grade_service,  "", price,
                 		"", "", "", "",0, "", "", "", x_lat,y_long,"");
                 restaurants.add(restaurant);
             }
 
         } catch (JSONException e) {
             e.printStackTrace();
             return null;
         }
         return restaurants;
     }
     
     
     private static ArrayList<Restaurant> parseAllRestaurants(String message, ArrayList<Restaurant> restaurants) {
         try {
             JSONArray jArray;
             jArray = new JSONArray(message.toString());
             for (int i = 0; i < jArray.length(); i++) {
 
                 int id = jArray.getJSONObject(i).getInt("id");
                 String name = jArray.getJSONObject(i).getString("name");
                 double x_lat = jArray.getJSONObject(i).getDouble("x_lat");
                 double y_long = jArray.getJSONObject(i).getDouble("y_long");
                 
                 int rank = 0;
                 if (!jArray.getJSONObject(i).isNull("rank"))
                     rank = jArray.getJSONObject(i).getInt("rank");
 
                 Restaurant restaurant = new Restaurant(id, name, "",
                 		"", "", "", 
                 		"", "", "", 
                 		"", "",0, "", "", "", x_lat,y_long,"");
                 restaurants.add(restaurant);
             }
 
         } catch (JSONException e) {
             e.printStackTrace();
             return null;
         }
         return restaurants;
     }
     
 //    (int id, String name, String pic_url,String grade_food, String grade_service,  String grade_ambiance, String price,String open_time,
 //    		String rest_date, String address, String phone, int rate_num, String introduction,
 //    		 String official_link,  String recommand_dish,  double x_lan, double y_long)
     
     private static ArrayList<Restaurant> parseSelectRestaurants(String message, ArrayList<Restaurant> restaurants) {
         try {
             JSONArray jArray;
             jArray = new JSONArray(message.toString());
             for (int i = 0; i < jArray.length(); i++) {
 
                 int id = jArray.getJSONObject(i).getInt("id");
                 String name = jArray.getJSONObject(i).getString("name");
                 String grade_food = jArray.getJSONObject(i).getString("grade_food");
                 String grade_service = jArray.getJSONObject(i).getString("grade_service");
                 String pic_url = jArray.getJSONObject(i).getString("pic_url");
                 String price = jArray.getJSONObject(i).getString("price");
                 
                 int rank = 0;
                 if (!jArray.getJSONObject(i).isNull("rank"))
                     rank = jArray.getJSONObject(i).getInt("rank");
 
                 Restaurant restaurant = new Restaurant(id, name, pic_url, grade_food,
                 		grade_service, "", price,
                 		"", "", "", "", 0,"", "", "", 0, 0,"");
                 restaurants.add(restaurant);
             }
 
         } catch (JSONException e) {
             e.printStackTrace();
             return null;
         }
         return restaurants;
     }
     
     private static String getMessageFromServer(String requestMethod, String apiPath, JSONObject json, String apiUrl) {
         URL url;
         try {
             if (apiUrl != null)
                 url = new URL(apiUrl);
             else
                 url = new URL(HOST + apiPath);
 
             if (DEBUG)
                 Log.d(TAG, "URL: " + url);
 
             HttpURLConnection connection = (HttpURLConnection) url.openConnection();
             connection.setRequestMethod(requestMethod);
 
             connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
             if (requestMethod.equalsIgnoreCase("POST"))
                 connection.setDoOutput(true);
             connection.setDoInput(true);
             connection.connect();
 
             if (requestMethod.equalsIgnoreCase("POST")) {
                 OutputStream outputStream;
 
                 outputStream = connection.getOutputStream();
                 if (DEBUG)
                     Log.d("post message", json.toString());
 
                 outputStream.write(json.toString().getBytes());
                 outputStream.flush();
                 outputStream.close();
             }
 
             BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
             StringBuilder lines = new StringBuilder();
             ;
             String tempStr;
 
             while ((tempStr = reader.readLine()) != null) {
                 lines = lines.append(tempStr);
             }
             if (DEBUG)
                 Log.d("MOVIE_API", lines.toString());
 
             reader.close();
             connection.disconnect();
 
             return lines.toString();
         } catch (MalformedURLException e) {
             e.printStackTrace();
             return null;
         } catch (IOException e) {
             e.printStackTrace();
             return null;
         }
     }
     
    public static void postData(int area_id, String name, int grade_food, int grade_service) {
         // Create a new HttpClient and Post Header
         HttpClient httpclient = new DefaultHttpClient();
         HttpPost httppost = new HttpPost(HOST + "/api/v1/recommands");
 
         try {
             // Add your data
             List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
             nameValuePairs.add(new BasicNameValuePair("area_id", Integer.toString(area_id)));
             nameValuePairs.add(new BasicNameValuePair("name", name));
             nameValuePairs.add(new BasicNameValuePair("grade_food", Integer.toString(grade_food)));
             nameValuePairs.add(new BasicNameValuePair("grade_service", Integer.toString(grade_service)));
             httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 
             // Execute HTTP Post Request
             HttpResponse response = httpclient.execute(httppost);
             
         } catch (ClientProtocolException e) {
             // TODO Auto-generated catch block
         } catch (IOException e) {
             // TODO Auto-generated catch block
         }
    }
    
    
    public static ArrayList<Restaurant> getRestaurantsDistance(double x, double y, ArrayList<Restaurant> res) {
 	   String x_string = String.valueOf(x);
 	   String y_string = String.valueOf(y);
 	   String des_string = getDesstring(res);
        String message = getMessageFromServer("GET", "" , null, "http://maps.googleapis.com/maps/api/distancematrix"+"/json?origins="+x_string+","+y_string+"&destinations="+des_string+"&language=zh-TW&sensor=false");
        if (message == null) {
            return null;
        } else {
            return parseRestaurantsDistance(message, res);
        }
    }
 
 	private static ArrayList<Restaurant> parseRestaurantsDistance(String message,
 		ArrayList<Restaurant> res) {
 		try {
 			JSONObject jObject = new JSONObject(message.toString());
 			JSONArray jArray = jObject.getJSONArray("rows").getJSONObject(0).getJSONArray("elements");
 			
 			int resIndex = 0;
 			for(resIndex=0; resIndex < res.size(); resIndex++){
 				if(res.get(resIndex).getDis().equals(""))
 					break;
 			}
 			
 			for (int i = 0; i < jArray.length(); i++) {
 				
 				String dis = jArray.getJSONObject(i).getJSONObject("distance").getString("text");
 				res.get(i+resIndex).setDis(dis);
             }
 			
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	return res;
 	}
 
 	private static String getDesstring(ArrayList<Restaurant> res) {
 		String des_string ="";
 		for(int i=0; i< res.size();i++){
 			if(res.get(i).getDis().equals("")){
 				String x_position = String.valueOf(res.get(i).getX());
 				String y_position = String.valueOf(res.get(i).getY());
 				des_string = des_string+x_position+","+y_position+"|";
 			}
 		}	
 		return des_string;
 	}
 	
 	
 }
