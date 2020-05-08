 /*
  *  project SongList
  * 
  * package com.agvinsant.songlist
  * 
  * @author Adam Vinsant
  * 
  * date Oct 1, 2013
  * 
  */
 package com.agvinsant.songlist;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.agvinsant.lib.WebClass;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.Activity;
import android.app.AlertDialog;
 import android.content.Context;
import android.content.DialogInterface;
 import android.content.res.Resources;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.webkit.WebView;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 
 public class MainActivity extends Activity {
 
 	Context context;
 	String[] songName;
 	Resources res;
 	TextView results;
 	Spinner viewSpinner;
 	TextView jsonView;
 	TextView connectedView;
 	String trackPreview;
 	String artistName;
 	String albumName;
 	String trackSite;
 	public static URL finalURL;
 
 	
 	ArrayList<String> artistNameList = new ArrayList<String>();
 	ArrayList<String> albumNameList = new ArrayList<String>();
 	ArrayList<String> trackSiteList = new ArrayList<String>();
 	ArrayList<String> trackPreviewList = new ArrayList<String>();
 	
 	Boolean connected = false;
 	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		context = this;
 		res = getResources();
 		
 		// setting the content view from layout xml 
 		setContentView(R.layout.form);
 		// setting the results view from the layout xml
 		jsonView = (TextView) findViewById(R.id.infoView);
 
 		
 		// Creating button from from layout xml
 		Button mb = (Button) findViewById(R.id.button1);
 		mb.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// Get selected song info
 				int pos = viewSpinner.getSelectedItemPosition();
 				String arName = artistNameList.get(pos).toString();
 				String alName = albumNameList.get(pos).toString();  
 				String tSite = trackSiteList.get(pos).toString();
 				
 
 	
 				jsonView.setText("Artist Name:   " +arName+ "\r\n"+ "\r\n"+"Album Name:   "+alName+ "\r\n" +"\r\n"+ "Song Website:   " +tSite);
 				
 				ImageView image = (ImageView) findViewById(R.id.imageView);
 				image.setImageResource(R.drawable.logicalthinkingcoverfront);
 				
 				
 				
 			}
 
 		});
 		
 		
 		// button to switch to webView in Chrome
 		Button webButton = (Button) findViewById(R.id.button2);
 		webButton.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				
 				WebView webView = (WebView) findViewById(R.id.webView);
 				webView.loadUrl("https://soundcloud.com/groove-logic/sets/logical-thinking-ep-teaser/s-7KILb");
 			}
 
 		});
 		
 		
 		connectedView = (TextView) findViewById(R.id.connectionView);
 		
 		TextView headView = (TextView) findViewById(R.id.headView);
 		headView.setText("Select a song from the list to see the info");
 		
 		//Detecting network settings
 				connected = WebClass.getConnectionStatus(context);
 				if(connected){
 					Log.i("Network Connection", WebClass.getConnectionType(context));
 					
 					connectedView.setText("Network Connection: " + WebClass.getConnectionType(context)+"\n");
 					
 					// calling the getSongInfo function 
 					getSongInfo();
 				}
 				else{
 						connectedView.setText(""+WebClass.getConnectionType(context)+"\n");
						
						// alert box shows if there is not an internet connection. Informs user that stored data will be used
						AlertDialog.Builder builder = new AlertDialog.Builder(this);
						builder.setMessage("No Internet Connection Detected. Stored data will be used.")
						       .setCancelable(false)
						       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
						           public void onClick(DialogInterface dialog, int id) {
						                dialog.cancel();
						           }
						       });
						AlertDialog alert = builder.create();
						alert.show();
 				}
 
 				
 		
 		// setting song array 
 		songName = res.getStringArray(R.array.songArray);
 		Log.i("songName", songName[0]);
 		
 		
 		//spinner adapter
 		ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, songName);
 		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		
 		//creating the spinner
 		viewSpinner = (Spinner) findViewById(R.id.spinner1);
 		viewSpinner.setAdapter(spinnerAdapter);
 		
 		//spinner onClick function
 		viewSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){
 
 			@Override
 			public void onItemSelected(AdapterView<?> parent, View view,
 					int position, long id) {
 				//Toast.makeText(context, "You selected " + songName[position], Toast.LENGTH_LONG).show();
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0) {
 				// TODO Auto-generated method stub
 
 			}
 
 		});
 		
 		
 
 		
 	}
 	
 	protected boolean selectionAvailable(int selection) {
 		if(artistNameList == null || artistNameList.size() < (selection + 1))return false;
 		if(albumNameList == null || albumNameList.size() < (selection + 1))return false;
 		if(trackSiteList == null || trackSiteList.size() < (selection + 1))return false;
 
 		return true;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	//get URL
 	private void getSongInfo(){
 		
 		Log.i("getSongInfo", "hit function");
 		
 		String baseURL = "https://itunes.apple.com/search";
 		
 		try{
 			finalURL = new URL(baseURL+"?term=groove+logic+logical+thinking");
 			songRequest sr = new songRequest();
 			sr.execute(finalURL);
 			
 			Log.i("getSongInfo", "hit function");
 		} catch (MalformedURLException e){
 			Log.e("BAD URL", "MALFORMED URL");
 			finalURL = null;
 		}
 	}
 	
 	//get data from URL
 	private class songRequest extends AsyncTask<URL, Void, String>{
 		@Override
 		protected String doInBackground(URL... urls){
 			String response = "";
 			
 				response = WebClass.getURLStringResponse(finalURL);
 				
 				Log.i("songRequest", response);
 				return response;
 			
 			
 		}
 		
 		//get data and add to arrays.
 		@Override
 		protected void onPostExecute(String result){
 			Log.i("URL RESPONSE", result);
 			
 	try {
 					
 					Log.i("TRYING JSON", "trying json");
 					//JSONObject json = new JSONObject(result);
 					//JSONObject results = jsonObject.getJSONObject("results");
 					
 					JSONObject mainJSON = new JSONObject(result);
 		
 					JSONArray jsonResult = mainJSON.getJSONArray("results");
 						
 					int n = jsonResult.length();
 					for (int i = 0; i<n; i++ ){	
 						
 						JSONObject child = jsonResult.getJSONObject(i);
 												
 						artistName= child.getString("artistName");
 						Log.i("artistName", artistName);
 						albumName = child.getString("collectionName");
 						Log.i("albumName", albumName);
 						trackSite= child.getString("trackViewUrl");
 						Log.i("trackSite", trackSite);
 						trackPreview = child.getString("previewUrl");
 						artistNameList.add(artistName);
 						albumNameList.add(albumName);  
 						trackSiteList.add(trackSite);
 						trackPreviewList.add(trackPreview);
 						
 					}
 		
 				} catch (JSONException e) {
 					Log.e("JSONException", "ERROR", e);
 					e.printStackTrace();
 				}
 		}	
 	}
 
 }
 
 
