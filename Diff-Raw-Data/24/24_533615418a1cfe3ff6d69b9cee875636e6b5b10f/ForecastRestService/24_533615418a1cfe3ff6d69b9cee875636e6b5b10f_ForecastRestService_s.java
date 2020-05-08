 package com.alexkorovyansky.carwashfor.services;
 
 import com.google.gson.annotations.SerializedName;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import retrofit.Callback;
 import retrofit.http.GET;
 import retrofit.http.Query;
 
 
 /**
  * @author Alex Korovyansky (korovyansk@gmail.com)
  */
 public interface ForecastRestService {
 
     public static class ForecastResponse {
 
         public static class Item{
 
             public static class RainInfo {
 
                 @SerializedName("3h")
                 float _3h;
             }
 
             @SerializedName("dt")
             long dt;
 
             @SerializedName("rain")
             RainInfo rainInfo;
 
         }
 
         @SerializedName("cod")
         int code;
 
         @SerializedName("list")
         List<Item> items = new ArrayList<Item>();
 
     }
 
 
 
    @GET("/data/2.1/forecast/city")
     void getForecast(@Query("lat") double lat, @Query("lon") double lon, Callback<ForecastResponse> callback);
 }
