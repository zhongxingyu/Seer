 package com.project.gemswapper;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class GetUser extends Activity implements AsyncTaskCompleteListener<JSONObject>
 {
 	public EditText editTextField;
 	public void onCreate(Bundle savedInstanceState) 
 	{
         requestWindowFeature(Window.FEATURE_NO_TITLE);
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_get_user);
 		
 		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
 		
 		final Button button;
 		
 		editTextField = (EditText) findViewById(R.id.edittext);
 		button = (Button) findViewById(R.id.button);
 
 		button.setOnClickListener(new OnClickListener() 
 		{
 			public void onClick(View v) 
 			{
 				String text = editTextField.getText().toString();
 				if(!text.equalsIgnoreCase(""))
 				{
 			        SharedPreferences.Editor editor = preferences.edit();
 			        editor.putString("Name",text);
 			        editor.commit();
 			        
 					if(isOnline())
 					{
 						new DataSend().execute();
 					}
 			        
 					Toast msg = Toast.makeText(getBaseContext(), getString(R.string.toastUser) + " " + text, Toast.LENGTH_LONG);
 					msg.show();
 					finish();
 				}
 			}
 		});
 	}
 	
 	@Override
 	public void onBackPressed() 
 	{
 		Toast msg = Toast.makeText(getBaseContext(), getString(R.string.errorBack), Toast.LENGTH_LONG);
 		msg.show();
 	}
 	
 	public boolean isOnline()
 	{
 		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
 			
 		return (networkInfo != null && networkInfo.isConnected());
 	}
 	
 	private void send_data()
 	{
     	String text = editTextField.getText().toString();
     	String coreUrl = "http://game-details.com/gemswapper/insertuser.php?";
         String urlToSend = coreUrl + "name=" + text;
     	
     	asyncGetJSONFromURL async = new asyncGetJSONFromURL(this);
     	async.execute(urlToSend);
 	}
 	
 	private class DataSend extends AsyncTask <Void, Void, Void> 
 	{
 		@Override
 		protected Void doInBackground(Void... params) {
 			send_data();
 			return null;
 		}
 	}
 	
 	public void onTaskComplete(JSONObject result) 
 	{
 		try
     	{
             JSONArray highscores = result.getJSONArray("highscore");
             
 	        for(int i = 0; i < highscores.length(); i++)
 	        {	
 				JSONObject e = highscores.getJSONObject(i);
 				
 				String id = e.getString("max(id)");
 				SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
 		        SharedPreferences.Editor editor = preferences.edit();
 		        editor.putString("Id",id);
 		        editor.commit();
 			}
     	}
     	catch(Exception e)
     	{
     		e.printStackTrace();
     	}
 	}	
 	// Creates a new thread and retrieves the JSON string in that new thread
 	// This is done so networking is avoided on the main thread which would cause
 	// an exception on newer android version. 
     
     // It runs the function to retrieve the JSONObject from the page in background
 	// When the object has been parsed, converted and packed into an object it is sent to
 	// onPostExecute, and in turn to the callback onTaskComplete. 
 	
 	private class asyncGetJSONFromURL extends AsyncTask <String, Void, JSONObject> 
 	{
 		private AsyncTaskCompleteListener<JSONObject> callback;
 		
 		public asyncGetJSONFromURL(AsyncTaskCompleteListener<JSONObject> cb)
 		{
 			this.callback = cb;
 		}
 		
 		@Override
 		protected JSONObject doInBackground(String... url) 
 		{
 			JSONObject json = getJSONfromURL(url[0]);
 			return json;
 		}
 		
 		protected void onPostExecute(JSONObject result)
 		{
 			callback.onTaskComplete(result);
 		}
 	}
 
 	// This functions takes the url of our target page, retrives the postResponse
 	// converts it to string and then packs the result into a JSONObject which is
 	// returned for further use. 
 	
 	public static JSONObject getJSONfromURL(String url)
 	{
 		InputStream inputStream = null;
 		String result = "";
 		JSONObject jArray = null;
 		
 		
 		inputStream = getPostResponse(url);
 		result = convertToString(inputStream);
 		
 	    try
 	    {
             jArray = new JSONObject(result);            
 	    }
 	    catch(JSONException e)
 	    {
 	            Log.e("log_tag", "Error parsing data " + e.toString());
 	    }
     
 	    return jArray;
 	}
 	
 	// connects to the Internet and returns the get response as an inputstream. 
 	public static InputStream getPostResponse(String url)
 	{
 		InputStream result = null;
 		
 	    try
 	    {
             HttpClient httpclient = new DefaultHttpClient();
             HttpPost httppost = new HttpPost(url);
             HttpResponse response = httpclient.execute(httppost);
             HttpEntity entity = response.getEntity();
             result = entity.getContent();
 	    }
 		catch(Exception e)
 		{
 	            Log.e("log_tag", "Error in http connection "+e.toString());
 	    }
 	    
 	    return result;
 	}
 	
 	// Converts the get response from an input stream to a string. 
 	public static String convertToString(InputStream postResponse)
 	{
 		String result = "";
 		
 	    try
 	    {
     		BufferedReader reader = new BufferedReader(new InputStreamReader(postResponse, "iso-8859-1"), 8);
             StringBuilder stringBuilder = new StringBuilder();
             String line = null;
             
             while ((line = reader.readLine()) != null) 
             {
                     stringBuilder.append(line + "\n");
             }
             
             result = stringBuilder.toString();
 	    }
 	    catch(Exception e)
 	    {
 	            Log.e("log_tag", "Error converting result "+e.toString());
 	    }
 	    
 	    return result;
 	}
 }
