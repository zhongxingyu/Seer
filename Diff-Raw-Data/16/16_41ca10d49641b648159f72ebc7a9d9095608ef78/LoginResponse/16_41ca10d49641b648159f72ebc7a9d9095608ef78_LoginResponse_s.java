 package org.creuroja.android.app;
 
 import android.content.Context;
 import android.preference.PreferenceManager;
 
 import org.apache.http.HttpResponse;
 import org.creuroja.android.R;
 import org.creuroja.android.app.utils.LocationsProvider;
 import org.creuroja.android.database.CreuRojaContract;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.List;
 
 /**
  * Created by lapuente on 03.12.13.
  */
 public class LoginResponse {
     public static final String sAccessToken = "accessToken";
     public static final String sAccessTokenString = "accessTokenString";
     public static final String sLocations = "locations";
     public static final String sLatitude = "latitude";
     public static final String sLongitude = "longitude";
     public static final String sName = "name";
     public static final String sType = "location_type";
     public static final String sAddress = "address";
    public static final String sDetails = "description";
     public static final String sLastUpdateTime = "updated_at";
     public static final String sExpireDate = "expireDate";
     public AccessToken mToken;
     public List<Location> mLocationList;
     public int mErrorMessage;
 
     public LoginResponse(int errorMessage) {
         mErrorMessage = errorMessage;
     }
 
     public LoginResponse(HttpResponse response) {
         try {
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(response.getEntity().getContent()));
             parseResponse(reader);
         } catch (IOException e) {
             mToken = null;
             mLocationList = null;
             mErrorMessage = R.string.error_connecting;
         }
     }
 
     public void parseResponse(BufferedReader reader) throws IOException {
         StringBuilder builder = new StringBuilder();
         String line;
         while ((line = reader.readLine()) != null) {
             builder.append(line);
         }
         try {
             JSONObject object = new JSONObject(builder.toString());
             if(object.has(sAccessToken)){
                 mToken = new AccessToken(object.getJSONObject(sAccessToken));
                 mLocationList = LocationsProvider.getLocationList(object);
             } else {
                 mToken = null;
                 mLocationList = null;
                 mErrorMessage = R.string.error_invalid_password;
             }
         } catch (JSONException e) {
 			e.printStackTrace();
             mToken = null;
             mLocationList = null;
             mErrorMessage = R.string.error_invalid_response;
         }
     }
 
     public boolean isValid() {
         return mToken != null;
     }
 
     public void revokeToken(Context context) {
         mToken = null;
         //Delete all stored data
         PreferenceManager.getDefaultSharedPreferences(context)
                 .edit().remove(Settings.ACCESS_TOKEN).commit();
         //Delete all db data.
         context.getContentResolver().delete(CreuRojaContract.Locations.CONTENT_LOCATIONS,
                 null, null);
     }
 
     public class AccessToken {
         public String mAccessToken;
 
         public AccessToken(JSONObject object) throws JSONException {
             mAccessToken = object.getString(sAccessTokenString);
         }
     }
 }
