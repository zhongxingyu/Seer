 package com.cruzroja.android.app.utils;
 
 import android.content.Context;
 import android.net.ConnectivityManager;
 
 import com.cruzroja.android.app.AccessResponse;
 import com.cruzroja.android.app.Location;
 import com.cruzroja.android.app.LoginResponse;
 import com.google.android.gms.maps.model.LatLng;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created by lapuente on 29.10.13.
  */
 public class ConnectionClient {
     private static final String SERVER_URL = "http://kls.servequake.com:808";
     private static final String QUERY = "/webservice.php?q=";
     private static final String LOGIN_REQUEST = "request_access";
     private static final String VALIDATE_REQUEST = "validate_access";
     private static final String LOCATIONS_REQUEST = "get_locations";
     public static final String DIRECTIONS_API_BASE_URL =
             "https://maps.googleapis.com/maps/api/directions/json?region=es&";
     public static final String ORIGIN_URL = "origin=";
     public static final String DESTINATION_URL = "destination=";
     public static final String SENSOR_URL = "sensor=";
 
     public static final String EMAIL_VAR = "email";
     public static final String PASS_VAR = "password";
     public static final String ACCESS_TOKEN_VAR = "access_token";
     public static final String LAST_UPDATE_VAR = "last_update";
 
     public static final int STATUS_OK = 0;
     public static final int STATUS_NOT_OK = 1;
     public static final int STATUS_LIMIT_REACHED = 2;
 
     public static final String sStatus = "status";
     public static final String sRoutes = "routes";
     public static final String sOverviewPolyline = "overview_polyline";
     public static final String sPoints = "points";
 
     public LoginResponse doLogin(String email, String password) throws IOException {
         HttpResponse response = executeRequest(createHttpClient(),
                 getLoginRequest(email, password));
 
         return new LoginResponse(response);
     }
 
     public AccessResponse validateLogin(String accessToken) throws IOException {
         HttpResponse response = executeRequest(createHttpClient(),
                 getValidationRequest(accessToken));
 
         return new AccessResponse(response);
     }
 
     public List<Location> requestUpdates(String accessToken, long lastUpdate) throws IOException {
         HttpResponse response = executeRequest(createHttpClient(),
                 getLocationsRequest(accessToken, lastUpdate));
 
         return LocationsProvider.getLocationList(response);
     }
 
     public List<LatLng> getDirections(double latitudeStart, double longitudeStart,
                                   Location destination) throws IOException  {
         return parseDirectionsResponse(executeRequest(createHttpClient(), getDirectionsRequest(
                 latitudeStart, longitudeStart, destination)));
     }
 
     public static boolean isConnected(Context context) {
         ConnectivityManager cm = (ConnectivityManager) context
                 .getSystemService(Context.CONNECTIVITY_SERVICE);
         return (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected());
     }
 
     private DefaultHttpClient createHttpClient() {
         return new DefaultHttpClient();
     }
 
     private HttpUriRequest getLoginRequest(String email, String password) {
         List<NameValuePair> nameValuePairs = new ArrayList<>();
         nameValuePairs.add(new BasicNameValuePair(EMAIL_VAR, email));
         nameValuePairs.add(new BasicNameValuePair(PASS_VAR, password));
         return buildRequest(LOGIN_REQUEST, nameValuePairs);
     }
 
     private HttpUriRequest getValidationRequest(String accessToken) {
         List<NameValuePair> nameValuePairs = new ArrayList<>();
         nameValuePairs.add(new BasicNameValuePair(ACCESS_TOKEN_VAR, accessToken));
        return buildRequest(LOGIN_REQUEST, nameValuePairs);
     }
 
     private HttpUriRequest getLocationsRequest(String accessToken, long lastUpdate) {
         List<NameValuePair> nameValuePairs = new ArrayList<>();
         nameValuePairs.add(new BasicNameValuePair(ACCESS_TOKEN_VAR, accessToken));
         nameValuePairs.add(new BasicNameValuePair(LAST_UPDATE_VAR, Long.toString(lastUpdate)));
         return buildRequest(LOCATIONS_REQUEST, nameValuePairs);
     }
 
     private HttpUriRequest getDirectionsRequest(double latitudeStart, double longitudeStart,
                                                 Location destination) {
         return new HttpGet(URI.create(DIRECTIONS_API_BASE_URL + ORIGIN_URL + latitudeStart + ","
                 + longitudeStart + "&" + DESTINATION_URL + destination.mLatitude + ","
                 + destination.mLongitude + "&" + SENSOR_URL + getSensorAvailability()));
     }
 
     // Placeholder in case we want to introduce sensor detection
     private static String getSensorAvailability() {
         return "true";
     }
 
     private HttpUriRequest buildRequest(String requestType, List<NameValuePair> entity) {
         String address = SERVER_URL + QUERY + requestType;
         HttpPost request = new HttpPost(address);
         try {
             request.setEntity(new UrlEncodedFormEntity(entity));
         } catch (UnsupportedEncodingException e) {
             e.printStackTrace();
         }
 
         return request;
     }
 
     private HttpResponse executeRequest(HttpClient httpClient, HttpUriRequest request)
             throws IOException {
         return httpClient.execute(request);
     }
 
     private static List<LatLng> parseDirectionsResponse(HttpResponse response) {
         List<LatLng> result = null;
         if (response.getStatusLine() != null) {
             switch (response.getStatusLine().getStatusCode()) {
                 case 200:
                     // Get everything as a string ready to parse
                     BufferedReader reader;
                     try {
                         reader = new BufferedReader(new InputStreamReader(response.getEntity()
                                 .getContent()));
                         StringBuilder builder = new StringBuilder();
                         String line;
                         while ((line = reader.readLine()) != null) {
                             builder.append(line);
                         }
                         if (reader != null) {
                             reader.close();
                         }
                         // Parse the resulting string. Save to disk if its correct
                         result = parseDirections(builder.toString());
                     } catch (IllegalStateException e) {
                         e.printStackTrace();
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                     break;
                 default:
                     break;
             }
         }
         return result;
     }
 
     public static List<LatLng> parseDirections(String data) {
         try {
             JSONObject response = new JSONObject(data);
             ArrayList<LatLng> points = new ArrayList<LatLng>();
             int status = parseStatus(response.getString(sStatus));
             if (status == STATUS_OK) {
                 JSONArray routes = response.getJSONArray(sRoutes);
                 for (int i = 0; i < routes.length(); i++) {
                     JSONObject route = routes.getJSONObject(i);
                     points.addAll(decodePoly(route.getJSONObject(
                             sOverviewPolyline).getString(sPoints)));
                 }
                 return points;
             } else if (status == STATUS_LIMIT_REACHED) {
                 // Handle error
                 return points;
             } else {
                 return null;
             }
         } catch (JSONException e) {
             return null;
         }
 
     }
 
     /**
      * This method was completely copied from some point in stackoverflow.com
      * If you wanna know what it does, good luck finding it again.
      * <p/>
      * It converts the List<LatLng> encoded in the response from Google into an array of points.
      *
      * @param encoded
      * @return
      */
     private static ArrayList<LatLng> decodePoly(String encoded) {
         ArrayList<LatLng> poly = new ArrayList<LatLng>();
         int index = 0, len = encoded.length();
         int lat = 0, lng = 0;
         while (index < len) {
             int b, shift = 0, result = 0;
             do {
                 b = encoded.charAt(index++) - 63;
                 result |= (b & 0x1f) << shift;
                 shift += 5;
             } while (b >= 0x20);
             int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
             lat += dlat;
             shift = 0;
             result = 0;
             do {
                 b = encoded.charAt(index++) - 63;
                 result |= (b & 0x1f) << shift;
                 shift += 5;
             } while (b >= 0x20);
             int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
             lng += dlng;
 
             LatLng position = new LatLng((double) lat / 1E5, (double) lng / 1E5);
             poly.add(position);
         }
         return poly;
     }
 
     private static int parseStatus(String status) {
         if (status.equals("OK") || status.equals("ZERO_RESULTS")) {
             return STATUS_OK;
         } else if (status.equals("OVER_QUERY_LIMIT")) {
             return STATUS_LIMIT_REACHED;
         } else {
             return STATUS_NOT_OK;
         }
     }
 }
