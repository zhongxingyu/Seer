 /*
  * project 	Java1Project
  * 
  * package 	com.fullsail.java1project
  * 
  * @author 	William Saults
  * 
  * date 	Jun 12, 2013
  */
 package com.fullsail.java1project;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.TextView;
 
 import com.fullsail.lib.FetchJsonData;
 import com.fullsail.lib.Venue;
 import com.fullsail.lib.VenueEnum;
 
 public class MainActivity extends Activity {
 	
 	// Variables
 	Venue venues[] = new Venue[3];
 	LinearLayout linearLayout;
 	LinearLayout subLinearLayout;
 	LinearLayout.LayoutParams layoutParams;
 	TextView textView;
 	RadioGroup venueGroupOptions;
 	Context context = this;
 	Boolean areRadiosPoulated;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		areRadiosPoulated = false;
 		
 		// Setup the linear layout
 		linearLayout = new LinearLayout(this);
 		linearLayout.setOrientation(LinearLayout.VERTICAL);
 		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
 		linearLayout.setLayoutParams(layoutParams);
 		
 		// Add the button
 		Button button = new Button(this);
 		button.setText("Get JSON");
 		linearLayout.addView(button);
 		
 		// Add the text view
 		textView = new TextView(this);
 		textView.setText("The JSON data will appear here.");
 		linearLayout.addView(textView);
 		
 		// Setup the nested ll
 		subLinearLayout = new LinearLayout(this);
 		subLinearLayout.setOrientation(LinearLayout.VERTICAL);
 		subLinearLayout.setLayoutParams(layoutParams);
 		linearLayout.addView(subLinearLayout);
 		
 		// Add the text view
 		TextView optionsText = new TextView(this);
 		optionsText.setText("Choose a locaiton.");
 		subLinearLayout.addView(optionsText);
 		
 		// Radio group
 		venueGroupOptions = new RadioGroup(this);
 		subLinearLayout.addView(venueGroupOptions);
 		
 		// Button event handler
 		button.setOnClickListener(new View.OnClickListener() { 
 
 			@Override
 			public void onClick(View v) {
 				// Build the url
 				try{
 					//encode in case user has included symbols such as spaces etc
 					String encodedSearch = URLEncoder.encode("2172797", "UTF-8");
 					//append encoded user search term to search URL
 					String searchURL = "http://openweathermap.org/data/2.5/weather?id="+encodedSearch+"&APPID=63a7a37aaacf05a109e77797f3af426d";
 					//instantiate and execute AsyncTask
 					new Request().execute(searchURL);
 
 				}
 				catch(Exception e){ 
					Log.e("Whoops - something went wrong!", "error");
 					e.printStackTrace(); 
 				}
 				
 				
 				/*
 				try {
 					// Create venues from the resouces					
 					JSONObject jsonObject = new JSONObject(getString(R.string.Venue1));
 					venues[VenueEnum.VINDEX0.ordinal()] = new Venue(jsonObject);
 					
 					jsonObject = new JSONObject(getString(R.string.Venue2));
 					venues[VenueEnum.VINDEX1.ordinal()] = new Venue(jsonObject);
 					
 					jsonObject = new JSONObject(getString(R.string.Venue3));
 					venues[VenueEnum.VINDEX2.ordinal()] = new Venue(jsonObject);
 					
 					textView.setText("");
 					for (Venue venue : venues) {
 						textView.append("=== Venue ===\n");
 						textView.append("Venue id: " + venue.getId() + "\nVenue name: " + venue.getName() + "\n");
 						
 						if (!areRadiosPoulated) {
 							// Populate the radio buttons
 							RadioButton rb = new RadioButton(context);
 							rb.setText(venue.getName());
 							venueGroupOptions.addView(rb);
 						}
 					}	
 					areRadiosPoulated = true;
 				} catch (JSONException e1) {
 					// TODO Auto-generated catch block
 					Log.i("Error: ", e1.getMessage());
 					e1.printStackTrace();
 				}
 				*/
 			}
 		});
 		/*
 		// Fetch the json data from the data file
 		try {
 			String json = FetchJsonData.jsonToStringFromAssetFolder("data.json", getBaseContext());
 			Log.i("JSON string: ", json);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		*/
 		
 		setContentView(linearLayout);
 	}
 	
 	private class Request extends AsyncTask<String, Void, String> {
 		/*
 		 * Carry out fetching task in background
 		 * - receives search URL via execute method
 		 */
 		@Override
 		protected String doInBackground(String... stringURL) {
 			//start building result which will be json string
 			StringBuilder stringBuilder = new StringBuilder();
 			//should only be one URL, receives array
 			for (String searchURL : stringURL) {
 				HttpClient client = new DefaultHttpClient();
 				try {
 					//pass search URL string to fetch
 					HttpGet get = new HttpGet(searchURL);
 					//execute request
 					HttpResponse response = client.execute(get);
 					//check status, only proceed if ok
 					StatusLine searchStatus = response.getStatusLine();
 					if (searchStatus.getStatusCode() == 200) {
 						//get the response
 						HttpEntity entity = response.getEntity();
 						InputStream content = entity.getContent();
 						//process the results
 						InputStreamReader input = new InputStreamReader(content);
 						BufferedReader reader = new BufferedReader(input);
 						String lineIn;
 						while ((lineIn = reader.readLine()) != null) {
 							stringBuilder.append(lineIn);
 						}
 					} else {
						Log.e("Whoops - something went wrong!", "error");
 					}
 				} catch(Exception e){ 
					Log.e("Whoops - something went wrong!", "error");
 					e.printStackTrace(); 
 				}
 			}
 			//return result string
 			return stringBuilder.toString();
 		}
 		/*
 		 * Process result of search query
		 * - this receives JSON string representing tweets with search term included
 		 */
 		protected void onPostExecute(String result) {
 			//start preparing result string for display
 			StringBuilder stringResultBuilder = new StringBuilder();
 			try {
 				//get JSONObject from result
 				JSONObject resultObject = new JSONObject(result);
				Log.i("resultObject", resultObject.toString());
 				stringResultBuilder.append(resultObject.toString());
				
//				JSONArray jsonArray = resultObject.getJSONArray("result");
//				Log.i("jsonArray", jsonArray.toString());
 			}
 			catch (Exception e) {
				Log.e("Whoops - something went wrong!", "error");
 				e.printStackTrace();
 			}
 			//check result exists
 			if(stringResultBuilder.length()>0) {
 				Log.i("json", stringResultBuilder.toString());
 			} else {
				Log.e("Whoops - something went wrong!", "error");
 			}
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 }
