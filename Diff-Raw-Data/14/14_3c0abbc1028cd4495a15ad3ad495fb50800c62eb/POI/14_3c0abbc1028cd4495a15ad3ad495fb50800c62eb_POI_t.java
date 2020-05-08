 package com.bettername.thepokemoneone.data;
 
import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.bettername.thepokemoneone.model.Place;
 import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
 import com.loopj.android.http.AsyncHttpClient;
 import com.loopj.android.http.AsyncHttpResponseHandler;
 import com.loopj.android.http.RequestParams;
 
 public class POI
 {
     
     public class MyLocation
     {
         double latitude;
         double longitude;
         double radius;
         
         public MyLocation(double latitude, double longitude, double radius)
         {
             this.latitude = latitude;
             this.longitude = longitude;
             this.radius = radius;
         }
     }
     
     public void getPOIs(double latitude, double longitude, double radius, final CallBackListener callback)
     {
         MyLocation place = new MyLocation(latitude, longitude, radius);
         Gson gson = new Gson();
         String json = gson.toJson(place);
         AsyncHttpClient client = new AsyncHttpClient();
         RequestParams rp = new RequestParams();
         rp.put("place", json);
         client.get("https://monsteradventure.fwd.wf/api/places", rp,
                 new AsyncHttpResponseHandler()
                 {
                     @Override
                     public void onSuccess(String response)
                     {
                     	Gson gson = new Gson();
                    	Type listType = new TypeToken<List<Place>>(){}.getType();
                    	
                    	ArrayList<Place> places = (ArrayList<Place>) gson.fromJson(response, listType);
                        callback.callBack(places);
                     }
                 });
     }
     public interface CallBackListener{
         public void callBack(List<Place> o);
     }
     
 }
