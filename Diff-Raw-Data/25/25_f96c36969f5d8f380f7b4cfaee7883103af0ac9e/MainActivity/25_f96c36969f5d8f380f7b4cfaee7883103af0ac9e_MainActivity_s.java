 package com.example.healthyfoodfinder;
 
 import java.io.IOException;
 
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import com.loopj.android.http.AsyncHttpResponseHandler;
 import com.loopj.android.http.JsonHttpResponseHandler;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 
 
 public class MainActivity extends Activity {
 	public static final int MapActivity_ID = 1;
 	public static final int RecipeActivity_ID = 2;
 	public static final int SeasonalActivity_ID = 3;
 	public static Database db;
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         db = new Database();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
     
     public void onMapButtonClick(View v) {
     	EditText food = (EditText)findViewById(R.id.editText1);
     	
     	
     	// Create an Intent using the current Activity and the Class to be created
     	System.out.println("Got here0");
     	Intent i = new Intent(this, MapActivity.class);
     	i.putExtra("Food", food.getText().toString().toUpperCase());
     	System.out.println("Got here1");
     	// Pass the Intent to the Activity, using the specified request code
     	startActivityForResult(i, MapActivity_ID);
     	System.out.println("Got here2");
     }
     
     public void onRecipeButtonClick(View v) {
     	// Create an Intent using the current Activity and the Class to be created
     	Intent i = new Intent(this, RecipeActivity.class);
     	EditText food = (EditText)findViewById(R.id.editText1);
    	i.putExtra("Food", food.getText().toString().toUpperCase());
     	// Pass the Intent to the Activity, using the specified request code
     	startActivityForResult(i, RecipeActivity_ID);
     }
     
     public void onSeasonalButtonClick(View v) {
     	// Create an Intent using the current Activity and the Class to be created
     	System.out.println("Got here0");
     	Intent i = new Intent(this, SeasonalActivity.class);
     	System.out.println("Got here1");
     	// Pass the Intent to the Activity, using the specified request code
     	startActivityForResult(i, SeasonalActivity_ID);
     	System.out.println("Got here2");
     }
     
     public void searchForRecipes(String[] params) {
     	AsyncHttpResponseHandler handler = new AsyncHttpResponseHandler() {
     		@Override
             public void onSuccess(String response) {
             	System.out.println("Respone: "+ response);
             }
         };
         YummlyClient.get(params, handler);
     }
     
     /*
     public class RecipeRequestTask extends AsyncTask<String, Void, String> {
     	
     	private RecipeCompleteListener callback;
     	
     	public RecipeRequestTask(RecipeCompleteListener callback) {
     		this.callback = callback;
     	}
     	
     	@Override
     	protected String doInBackground(String... params) {
     		
     		DefaultHttpClient client = new DefaultHttpClient();
     		HttpGet request = new HttpGet("http://api.yummly.com/v1/api/recipes?_app_id=6f396328&_app_key=b04ef92e90258262c7f43da8bf2d2aed&q=apples");
     		StringBuffer responseBuffer = new StringBuffer();
     		try {
 				HttpResponse response = client.execute(request);
 				InputStreamReader in = new InputStreamReader(response.getEntity().getContent());
 				char c;
 				while((c = (char) in.read()) != -1) {
 					responseBuffer.append(c);
 				}
 				response.getEntity().consumeContent();
 				in.close();
 			} catch (ClientProtocolException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
     		return responseBuffer.toString();
     	}
     	
     	@Override
     	protected void onPostExecute(String result) {
     		callback.onSuccess(result);
     	}
     }
 
 	@Override
 	public void onSuccess(String result) {
 		Log.d("Result", result);
 	}
     */
     
     
 }
