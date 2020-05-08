 package com.example.healthyfoodfinder;
 
 import java.io.IOException;
 import com.loopj.android.http.*;
 
 import java.io.InputStreamReader;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.Window;
 import android.widget.ListView;
 
 public class RecipeActivity extends Activity {
 	ListView list;
 	LazyAdapter adapter;
 	String[] imagesources, recipenames;
 	ProgressDialog mDialog;
 
 	@Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_recipe);
        String query = getIntent().getStringExtra("Food");
         Log.d("Debug",query);
         String[] params = query.split(" ");
         searchForRecipes(params, this);
         Log.d("Recipe","Got here");
         list = (ListView)findViewById(R.id.listView1);
         //task.execute("Banana");
         //mDialog = new ProgressDialog(getApplicationContext());
         //mDialog.setMessage("Loading Recipes...");
         //mDialog.setCancelable(false);
         //mDialog.show();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.activity_recipe, menu);
         return true;
     }
     
     public void onFinishRecipeButtonClick(View view) {
     	// Create the Intent object to send back to the caller
     	Intent i = new Intent();
     	
     	// Put the number of clicks into the Intent
     	// i.putExtra("NUM_CLICKS", 1);
     	setResult(RESULT_OK, i);
     	finish();
     }
     
     
     public void searchForRecipes(String[] params, final Activity a) {
     	AsyncHttpResponseHandler handler = new JsonHttpResponseHandler() {
     		@Override
             public void onSuccess(JSONObject response) {
     			try {
 					JSONArray matches = response.getJSONArray("matches");
 					imagesources = new String[matches.length()];
 					recipenames = new String[matches.length()];
 					for(int i = 0; i < imagesources.length; i++) {
 						JSONObject obj = matches.getJSONObject(i);
 						recipenames[i] = obj.getString("recipeName");
 						JSONArray imgs = obj.getJSONArray("smallImageUrls");
						imagesources[i] = imgs.get(0).toString();
 					}
 					adapter = new LazyAdapter(a,imagesources, recipenames);
 					list.setAdapter(adapter);
 					//mDialog.dismiss();
 				} catch (JSONException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 					//mDialog.dismiss();
 				}
             	System.out.println("Respone: "+ response.toString());
             }
         };
         YummlyClient.get(params, handler);
     }
 }
 
 
 
 
