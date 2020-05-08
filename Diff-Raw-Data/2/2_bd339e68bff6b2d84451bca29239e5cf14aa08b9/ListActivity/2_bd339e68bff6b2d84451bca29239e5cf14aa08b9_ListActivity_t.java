 package com.dunksoftware.seminoletix;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.CookieStore;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.BasicCookieStore;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONObject;
 import android.os.AsyncTask;
 import android.util.Log;
 import java.util.concurrent.ExecutionException;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.Toast;
 
 public class ListActivity extends Activity {
 
 	//private Constants.GetTable mGetTable;
 	private JSONObject[] Games = null;
 	GetGames games;
 	String error = "ERROR!";
 	
 	final int MESSAGE = 200;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_list);
 		
 		
 	//	mGetTable = new Constants.GetTable();
 	//	mGetTable.execute(Constants.GamesAddress);
 		
 		games = new GetGames();
 		
 		try {
 			games.execute();
 			error=games.get();
 			//Toast.makeText(getApplicationContext(), games.get(), Toast.LENGTH_LONG).show();
 			
 			showDialog(MESSAGE);
 	//		Games = mGetTable.get();
 
 	//		if(Games == null)
 		//		Log.w("List Activity", "Games is null");
 
 			// pulling of information works fine
 			// Games[index].getString("FIELD_NAME")
 
 			/*for(int i = 0; i < Games.length; i++) {
 				Log.w("Games", Games[i].getString("_id"));
 				Log.w("Games", Games[i].getString("availableDate"));
 				Log.w("Games", Games[i].getString("full"));
 				Log.w("Games", Games[i].getString("seats"));
 				Log.w("Games", Games[i].getString("seatsLeft"));
 				Log.w("Games", Games[i].getString("sport"));
 				Log.w("Games", Games[i].getString("teams[]"));
 			}*/
 
 		} catch(InterruptedException ex) {
 			Log.w("List Activity - mGetTable.execute()", ex.getMessage());
 		} catch(ExecutionException ex) {
 			Log.w("List Activity - mGetTable.execute()", ex.getMessage());
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_list, menu);
 		return true;
 	}
 	
 	@SuppressWarnings("deprecation")
 	@Override
 	protected Dialog onCreateDialog(int id) {
 
 		AlertDialog.Builder builder;
 
 		switch( id ) {
 
 		case MESSAGE: {
 			builder = new AlertDialog.
 					Builder(this);
 
 			builder.setCancelable(false).setTitle("Page Result").
 			setMessage(error).setNeutralButton("Close", 
 					new OnClickListener() {
 
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
					
 				}
 			});
 
 			builder.create().show();
 			break;
 		}
 
 		}
 		return super.onCreateDialog(id);
 	}
 
 	public void showDetails(View v) {
 		// check which details button called it
 		// open the corresponding details window
 	}
 	
 	class GetGames extends AsyncTask<Void, Void, String> {
 
 		@Override
 		protected String doInBackground(Void... params) {
 
 			// create an array of json objects that will be returned
 			JSONObject[] jsonObjects = null;
 
 			// Create the httpclient
 			//HttpClient httpclient = new DefaultHttpClient();
 
 			// Create a new HttpClient and Post Header
 
 			//Creates a client
 			MyHttpClient client=new MyHttpClient(null);
 
 			//sets cookie
 			client.setCookieStore(UserControl.mCookie);
 			// Prepare a request object
 			HttpGet httpget = new HttpGet(Constants.GamesAddress); 
 			
 			// Execute the request
 			HttpResponse response=null;
 
 			// return string
 			String returnString = null;
 
 			try {
 				// Open the web page.
 				response = client.execute(httpget);
 				returnString = EntityUtils.toString(response.getEntity());
 
 			}
 			catch (IOException  ex) {
 				// Connection was not established
 				returnString = "Connection failed; " + ex.getMessage();
 			}
 
 			return returnString;
 		}
 	}
 }
