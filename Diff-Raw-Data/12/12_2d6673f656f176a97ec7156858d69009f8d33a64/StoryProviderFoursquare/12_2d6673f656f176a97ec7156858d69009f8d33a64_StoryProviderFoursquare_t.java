 package com.teamgrau.altourism.util.data;
 
 import android.content.Context;
 import android.location.Location;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.util.Log;
 import com.teamgrau.altourism.util.data.model.POI;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.*;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Scanner;
 
 public class StoryProviderFoursquare implements StoryProvider {
     private static final String DBG_TAG = "StoryProviderFoursquare";
     private static final String FOURSQUARE_CLIENT_LATLNG = "ll";
     private static final String FOURSQUARE_CLIENT_ID_PARAMETER = "client_id";
     private static final String FOURSQUARE_CLIENT_ID_VALUE = "GR2KJRRNX4OFH2QXGME4Z3EKD0LP4RGODFW2OACVZ3BPYUCD";
     private static final String FOURSQUARE_CLIENT_SECRET_PARAMETER = "client_secret";
     private static final String FOURSQUARE_CLIENT_SECRET_VALUE = "YGOCDX3QLZNATW0M2J55BREG4U0HA2L3M42YTDQEFZQPCSMT";
     private static final String FOURSQUARE_SEARCH_URL = "https://api.foursquare.com/v2/venues/search";
     private static final String FOURSQUARE_V_DATE_PARAMETER = "v";
     private static final String FOURSQUARE_V_DATE_VALUE = "20130118";
     private final Context mCtx;
     private List<OnStoryProviderFinishedListener> listeners;
 
     public StoryProviderFoursquare ( Context ctx ) {
         mCtx = ctx;
         listeners = new LinkedList<OnStoryProviderFinishedListener> ();
     }
 
     private void request ( Location l, double r ) {
         ConnectivityManager connMgr = (ConnectivityManager) mCtx.getSystemService ( Context.CONNECTIVITY_SERVICE );
         NetworkInfo networkInfo = connMgr.getActiveNetworkInfo ();
         if ( networkInfo != null && networkInfo.isConnected () ) {
             String urlString = FOURSQUARE_SEARCH_URL
                     + "?" + FOURSQUARE_CLIENT_LATLNG + "=" + l.getLatitude () + "," + l.getLongitude ()
                     + "&" + FOURSQUARE_CLIENT_ID_PARAMETER + "=" + FOURSQUARE_CLIENT_ID_VALUE
                     + "&" + FOURSQUARE_CLIENT_SECRET_PARAMETER + "=" + FOURSQUARE_CLIENT_SECRET_VALUE
                     + "&" + FOURSQUARE_V_DATE_PARAMETER + "=" + FOURSQUARE_V_DATE_VALUE;
 
             try {
                 new FoursquareSearchWorker ().execute ( new URL ( urlString ) );
             } catch ( MalformedURLException e ) {
                 e.printStackTrace ();
             }
         } else {
             Log.d ( DBG_TAG, "No network connection? " + ((networkInfo != null) ?networkInfo.getReason () :"") );
         }
     }
 
     @Override
     public List<POI> listPOIs ( Location position, double radius ) {
         return null;
     }
 
     @Override
     public void listPOIs ( Location position, double radius, OnStoryProviderFinishedListener l ) {
         if ( !listeners.contains ( l ) ) {
             listeners.add ( l );
         }
         request ( position, radius );
     }
 
    @Override
    public POI getPOI ( Location position ) {
        return null;
    }

     // Uses AsyncTask to create a task away from the main UI thread. This task takes a
     // URL string and uses it to create an HttpUrlConnection. Once the connection
     // has been established, the AsyncTask downloads the contents of the webpage as
     // an InputStream. Finally, the InputStream is converted into a string, which is
     // displayed in the UI by the AsyncTask's onPostExecute method.
     private class FoursquareSearchWorker extends AsyncTask<URL, Integer, List<POI>> {
 
         private static final String FOURSQUARE_JSON_RESPONSE = "response";
         private static final String FOURSQUARE_JSON_VENUES = "venues";
         private static final String FOURSQUARE_JSON_GROUPS = "groups";
         private static final int FOURQUARE_JSON_NEARBY = 0;
         private static final String FOURSQUARE_JSON_ITEMS = "items";
         private static final String STORYPROVIDER_FOURSQUARE = "Foursquare";
         private static final String FOURSQUARE_JSON_LOCATION = "location";
         private static final String FOURSQUARE_JSON_LAT = "lat";
         private static final String FOURSQUARE_JSON_LNG = "lng";
         private static final String FOURSQUARE_JSON_NAME = "name";
 
         private List<POI> mPois;
 
         @Override
         protected List<POI> doInBackground ( URL... urls ) {
             // params comes from the execute() call: params[0] is the url.
             try {
                 return parseResult ( downloadUrl ( urls[ 0 ] ) );
             } catch ( IOException e ) {
                 e.printStackTrace ();
                 return null;
             }
         }
 
         @Override
         protected void onPostExecute ( List<POI> pois ) {
             super.onPostExecute ( pois );
             for ( OnStoryProviderFinishedListener l : listeners ) {
                 l.onStoryProviderFinished ( pois );
             }
         }
 
         private List<POI> parseResult ( String result ) {
             Log.d ( this.getClass ().getName (), "starting to parse" );
             JSONArray a;
             JSONObject o;
             LinkedList<POI> poiList = new LinkedList<POI> ();
 
             try {
                 // Walking down the json tree
                 a = new JSONObject ( result )
                         .getJSONObject ( FOURSQUARE_JSON_RESPONSE )
                         .getJSONArray ( FOURSQUARE_JSON_VENUES );
                         /*.getJSONArray ( FOURSQUARE_JSON_GROUPS )
                         .getJSONObject ( FOURQUARE_JSON_NEARBY )
                         .getJSONArray ( FOURSQUARE_JSON_ITEMS );*/
 
                 for ( int i = 0; i < a.length (); i++ ) {
                     //Log.d ( this.getClass ().getName (), "added " + i + " of " + a.length () + "items to poi list" );
                     o = a.getJSONObject ( i );
                     Location l = new Location ( STORYPROVIDER_FOURSQUARE );
                     l.setLongitude ( Double.parseDouble ( o.getJSONObject ( FOURSQUARE_JSON_LOCATION )
                             .getString ( FOURSQUARE_JSON_LNG ) ) );
                     l.setLatitude ( Double.parseDouble ( o.getJSONObject ( FOURSQUARE_JSON_LOCATION )
                             .getString ( FOURSQUARE_JSON_LAT ) ) );
                     poiList.add ( new POI ( o.getString ( FOURSQUARE_JSON_NAME ), l ) );
                 }
                 return poiList;
             } catch ( JSONException e ) {
                 e.printStackTrace ();
             }
 
             return poiList;
         }
 
         // Given a URL, establishes an HttpUrlConnection and retrieves
         // the web page content as a InputStream, which it returns as
         // a string.
         private String downloadUrl ( URL url ) throws IOException {
             Log.d ( this.getClass ().getName (), "starting to downlad" );
 
             InputStream is = null;
 
             try {
                 HttpURLConnection conn = (HttpURLConnection) url.openConnection ();
                 conn.setReadTimeout ( 10000 /* milliseconds */ );
                 conn.setConnectTimeout ( 15000 /* milliseconds */ );
                 conn.setRequestMethod ( "GET" );
                 conn.setDoInput ( true );
                 // Starts the query
                 conn.connect ();
                 int response = conn.getResponseCode ();
                 Log.d ( DBG_TAG, "The response is: " + response );
                 is = conn.getInputStream ();
 
                 // Convert the InputStream into a string
                 String contentAsString = readIt ( is );
 
                 Log.d ( this.getClass ().getName (), "finished to downlad" );
                 return contentAsString;
 
                 // Makes sure that the InputStream is closed after the app is
                 // finished using it.
             } finally {
                 if ( is != null ) {
                     is.close ();
                 }
             }
         }
 
         // Reads an InputStream and converts it to a String.
         public String readIt ( InputStream stream ) throws IOException, UnsupportedEncodingException {
             Scanner scanner = new Scanner ( stream ).useDelimiter ( "\\A" );
             return scanner.hasNext () ?scanner.next () :"";
         }
     }
 
 }
