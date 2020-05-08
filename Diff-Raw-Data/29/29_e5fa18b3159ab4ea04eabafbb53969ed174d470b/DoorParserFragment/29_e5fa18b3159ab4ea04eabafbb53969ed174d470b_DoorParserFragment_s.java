 package com.pspace.gr;
 
 import android.app.ListFragment;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.widget.ListAdapter;
 import android.widget.SimpleAdapter;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 
 /**
  * Created by tsagi on 9/9/13.
  */
 public class DoorParserFragment extends ListFragment{
 
     public static DoorParserFragment newInstance() {
         return new DoorParserFragment();
     }
 
     String timestamp;
     String name;
     String reading;
     String count;
 
     // Hashmap for ListView
     ArrayList<HashMap<String, String>> doorList;
     // creating new HashMap
     HashMap<String, String> map;
 
     // JSON Node names
     private static final String TAG_EVENTS= "events";
     private static final String TAG_TYPE = "type";
     private static final String TAG_EXTRA= "extra";
     private static final String TAG_NAME = "name";
     private static final String TAG_TIMESTAMP = "t";
 
     // contacts JSONArray
     JSONArray readings = null;
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         loadPref();

         getJson();


     }

     private void getJson(){
         String jurl = "http://pspace.dyndns.org:88/report/?limit=" + count + "&json";
         Log.d("url", jurl);
         new ParseDoorData().execute(jurl);
     }
 
     private class ParseDoorData extends AsyncTask<String, Void, Void> {
 
         protected Void doInBackground(String... params) {
 
             String url = params[0];
             // Hashmap for ListView
             doorList = new ArrayList<HashMap<String, String>>();
 
             // Creating JSON Parser instance
             JSONParser jParser = new JSONParser();
 
             // getting JSON string from URL
             JSONObject json = jParser.getJSONFromUrl(url);
 
             //FIXME
             if (json==null)
                 return null;
 
             try {
                 // Getting Array of Readings
                 readings = json.getJSONArray(TAG_EVENTS);
 
                 // looping through All Contacts
                 for(int i = 0; i < readings.length(); i++){
                     JSONObject r = readings.getJSONObject(i);
 
                     // Storing each json item in variable
                     timestamp= r.getString(TAG_TIMESTAMP);
                     name = r.getString(TAG_NAME);
                     reading = r.getString(TAG_TYPE);
 
                     // creating new HashMap
                     map = new HashMap<String, String>();
 
                     if(name.equals("BBoD"))
                         name = "Remote button pressed!";
                     else
                         name = "Card was used by: " + name;
 
                     // adding each child node to HashMap key => value
                     map.put(TAG_TIMESTAMP, stampToDate(timestamp));
                     map.put(TAG_NAME, name);
 
                     // adding HashList to ArrayList
                     doorList.add(map);
                 }
 
             }catch (JSONException e) {
                     e.printStackTrace();
                 }
             return null;
         }
 
         @Override
         protected void onPostExecute(Void aVoid) {
             /**
              * Updating parsed JSON data into ListView
              * */
 
             ListAdapter adapter = new SimpleAdapter(getActivity(), doorList,
                     R.layout.door_list_fragment,
                     new String[] { TAG_TIMESTAMP, TAG_NAME }, new int[] {
                     R.id.timestamp, R.id.member });
 
             setListAdapter(adapter);
         }
     }
 
     private void loadPref(){
         SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

         count = mySharedPreferences.getString("count_preference", "10");
     }
 
     private String stampToDate(String timestamp){
        long dv = Long.valueOf(timestamp);
        Date df = new java.util.Date(dv);
        return new SimpleDateFormat("d MMM - H:m").format(df);
     }
 }
