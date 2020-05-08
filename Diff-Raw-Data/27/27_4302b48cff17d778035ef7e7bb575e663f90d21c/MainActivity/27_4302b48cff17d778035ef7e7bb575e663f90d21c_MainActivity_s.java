 package com.mwong56.bmw.e46;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.Toast;
 
 import com.mwong56.bmw.e46.diy.R;
 
 public class MainActivity extends ListActivity {
     private ProgressDialog pDialog;
     private JSONParser jParser = new JSONParser();
     private ArrayList<HashMap<String, String>> productsList;
     private static String url_all_products = "http://ec2-54-235-111-44.compute-1.amazonaws.com/bmwe46android/get_all_main.php";
     private static final String TAG_SUCCESS = "success";
     private static final String TAG_MAIN = "main";
     private static final String TAG_PID = "pid";
     private static final String TAG_NAME = "name";
     private ConnectivityManager conMgr;
     private NetworkInfo activeNetwork;
     private JSONArray products = null;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
         activeNetwork = conMgr.getActiveNetworkInfo();
 
         // Hashmap for ListView
         productsList = new ArrayList<HashMap<String, String>>();
 
         if (activeNetwork != null && activeNetwork.isConnected()) {
             // Loading products in Background Thread
             new LoadAllProducts().execute();
         } else {
             Toast.makeText(getApplicationContext(), "Network Detection Failed",
                     Toast.LENGTH_SHORT).show();
             finish();
         }
 
         // Get listview
         ListView lv = getListView();
 
         // on seleting single product
         // launching Edit Product Screen
         lv.setOnItemClickListener(new OnItemClickListener() {
 
             @Override
             public void onItemClick(AdapterView<?> parent, View view,
                     int position, long id) {
                 activeNetwork = conMgr.getActiveNetworkInfo();
                 if (activeNetwork != null && activeNetwork.isConnected()) {
                     // Loading products in Background Thread
                     if (position == 14) {
                         Intent about = new Intent(getApplicationContext(),
                                 About.class);
                         startActivity(about);
                     } else {
                         Intent in = new Intent(getApplicationContext(),
                                 SpecificsActivity.class);
                         in.putExtra("position", position);
                         startActivityForResult(in, 100);
                     }
                 } else {
                     Toast.makeText(getApplicationContext(),
                             "Network Detection Failed", Toast.LENGTH_SHORT)
                             .show();
                 }
             }
         });
 
     }
 
     @Override
     public void onPause() {
         super.onPause();
         if (pDialog != null)
             pDialog.dismiss();
         pDialog = null;
     }
 
     /**
      * Background Async Task to Load all product by making HTTP Request
      * */
     class LoadAllProducts extends AsyncTask<String, String, String> {
 
         /**
          * Before starting background thread Show Progress Dialog
          * */
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
             pDialog = new ProgressDialog(MainActivity.this);
             pDialog.setMessage("Loading list. Please wait...");
             pDialog.setIndeterminate(false);
             pDialog.setCancelable(false);
             pDialog.show();
         }
 
         /**
          * getting All products from url
          * */
         protected String doInBackground(String... args) {
             // Building Parameters
             List<NameValuePair> params = new ArrayList<NameValuePair>();
             // getting JSON string from URL
             JSONObject json = jParser.makeHttpRequest(url_all_products, "GET",
                     params);
 
             // Check your log cat for JSON reponse
             // Log.d("All Products: ", json.toString());
 
             try {
                 // Checking for SUCCESS TAG
                 int success = json.getInt(TAG_SUCCESS);
 
                 if (success == 1) {
                     // products found
                     // Getting Array of Products
                     products = json.getJSONArray(TAG_MAIN);
 
                     // looping through All Products
                     for (int i = 0; i < products.length(); i++) {
                         JSONObject c = products.getJSONObject(i);
 
                         // Storing each json item in variable
                         String id = c.getString(TAG_PID);
                         String name = c.getString(TAG_NAME);
 
                         // creating new HashMap
                         HashMap<String, String> map = new HashMap<String, String>();
 
                         // adding each child node to HashMap key => value
                         map.put(TAG_PID, id);
                         map.put(TAG_NAME, name);
 
                         // adding HashList to ArrayList
                         productsList.add(map);
                     }
                 }
             } catch (JSONException e) {
                 e.printStackTrace();
             }
 
             return null;
         }
 
        /**
         * After completing background task Dismiss the progress dialog
         * **/
         protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all products
            pDialog.dismiss();
            // updating UI from Background Thread
             runOnUiThread(new Runnable() {
                 public void run() {
                     /**
                      * Updating parsed JSON data into ListView
                      * */
                     ListAdapter adapter = new SimpleAdapter(MainActivity.this,
                             productsList, R.layout.list_item, new String[] {
                                     TAG_PID, TAG_NAME }, new int[] { R.id.pid,
                                     R.id.name });
                    // updating listview
                     setListAdapter(adapter);
                 }
             });
 
         }
 
     }
 }
