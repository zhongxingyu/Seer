 package com.cruzroja.android.app.utils;
 
 import android.content.ContentResolver;
 import android.database.Cursor;
 import android.net.Uri;
 
 import com.cruzroja.android.app.Location;
 import com.cruzroja.android.app.LoginResponse;
 import com.cruzroja.android.database.CreuRojaContract;
 
 import org.apache.http.HttpResponse;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created by lapuente on 14.01.14.
  */
 public class LocationsProvider {
     public static List<Location> getLocationList(HttpResponse response) {
         List<Location> locationList = new ArrayList<>();
         BufferedReader reader;
         StringBuilder builder = new StringBuilder();
         try {
             reader = new BufferedReader(new InputStreamReader(
                     response.getEntity().getContent()));
             String line;
             while ((line = reader.readLine()) != null) {
                 builder.append(line);
             }
 
             locationList = getLocationList(new JSONObject(builder.toString()));
         } catch (IOException e) {
             //Nothing to do, still return the empty list
         } catch (JSONException e) {
             //Nothing to do, still return the empty list
         }
 
         return locationList;
     }
 
     public static List<Location> getLocationList(JSONObject object) throws JSONException {
         ArrayList<Location> locationList = new ArrayList<>();
         JSONArray locations = object.getJSONArray(LoginResponse.sLocations);
         for (int i = 0; i < locations.length(); i++) {
             Location location = new Location(locations.getJSONObject(i));
            if (location.mExpireDate == 0 ||
                    location.mExpireDate > System.currentTimeMillis()) {
                locationList.add(location);
            }
         }
         return locationList;
     }
 
     public static List<Location> getLocationList(Cursor cursor) {
         ArrayList<Location> locationsList = new ArrayList<Location>();
         if (cursor.moveToFirst()) {
             do {
                 Location location = new Location(cursor);
                if (location.mExpireDate == 0 ||
                        location.mExpireDate > System.currentTimeMillis()) {
                    locationsList.add(location);
                }
             } while (cursor.moveToNext());
         }
         return locationsList;
     }
 
     public static List<Location> getCurrentLocations(ContentResolver cr) {
         Uri uri = CreuRojaContract.Locations.CONTENT_LOCATIONS;
         return getLocationList(cr.query(uri, null, null, null, null));
     }
 }
