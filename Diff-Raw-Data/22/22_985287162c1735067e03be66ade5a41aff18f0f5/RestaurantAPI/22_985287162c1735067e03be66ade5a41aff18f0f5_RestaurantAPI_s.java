 package com.restaurant.collection.api;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 
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
     
     
     public static ArrayList<Restaurant> getAreaRestaurants(int area_id, int page) {
         String message = getMessageFromServer("GET", "/api/v1/restaurants?area_id=" + area_id + "&page=" + page, null, null);
         ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
         if (message == null) {
             return null;
         } else {
             return parseRestaurants(message, restaurants);
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
     
     
     public static ArrayList<Restaurant> getTypeRestaurants(int type_id, int page) {
         String message = getMessageFromServer("GET", "/api/v1/restaurants?type_id=" + type_id + "&page=" + page, null, null);
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
    
    public static ArrayList<Note> getAreaCategoryNotes(int area_id, int category_id,int page) {
        String message = getMessageFromServer("GET", "/api/v1/notes?area_id="+ area_id  +"&category_id="+ category_id+ "&page=" + page, null, null);
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
    
    private static ArrayList<Note> parseNotes(String message, ArrayList<Note> notes) {
        try {
            JSONArray jArray;
            jArray = new JSONArray(message.toString());
            for (int i = 0; i < jArray.length(); i++) {
 
                int id = jArray.getJSONObject(i).getInt("id");
                int restaurant_id = jArray.getJSONObject(i).getInt("restaurant_id");
                String title = jArray.getJSONObject(i).getString("title");
                String intro = jArray.getJSONObject(i).getString("intro");
                String pic_url = jArray.getJSONObject(i).getString("pic_url");
                String link = jArray.getJSONObject(i).getString("link");
                
                int rank = 0;
                if (!jArray.getJSONObject(i).isNull("rank"))
                    rank = jArray.getJSONObject(i).getInt("rank");
 
               Note note = new Note(id, restaurant_id, title, intro, pic_url, link);
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
                String grade_food = jObject.getString("grade_food");
                String grade_service = jObject.getString("grade_service");
                String pic_url = jObject.getString("pic_url");
                String address = jObject.getString("address");
                String open_time = jObject.getString("open_time");
                String official_link = jObject.getString("official_link");
                String price = jObject.getString("price");
                String traffic = jObject.getString("traffic");
                String introduction = jObject.getString("introduction");
                double x_lan = jObject.getDouble("x_lan");
                double y_long = jObject.getDouble("y_long");
                
                int rank = 0;
                if (!jObject.isNull("rank"))
                    rank = jObject.getInt("rank");
 
                restaurant = new Restaurant(id, name, grade_food,
                		grade_service, pic_url, address, 
                		open_time, official_link, price, 
                		traffic, introduction, x_lan, y_long);
                
            
 
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
                 
                 int rank = 0;
                 if (!jArray.getJSONObject(i).isNull("rank"))
                     rank = jArray.getJSONObject(i).getInt("rank");
 
                 Restaurant restaurant = new Restaurant(id, name, grade_food,
                 		grade_service, pic_url, "", 
                 		"", "", "", 
                		"", "",0,0);
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
     
 }
