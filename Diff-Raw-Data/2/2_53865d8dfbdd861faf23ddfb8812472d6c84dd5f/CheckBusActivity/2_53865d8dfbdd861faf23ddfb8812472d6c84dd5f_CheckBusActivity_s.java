 /*
  * Copyright [2012] [Martin Augustsson]
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License. 
  */
 package com.chalmers.schmaps;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.google.android.maps.GeoPoint;
 
 
 import android.app.Activity;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 /*************************************************
  * Class shows when Bus 16 departures from
  * Chalmers and Lindholmen
  *************************************************/
 
 public class CheckBusActivity extends Activity implements View.OnClickListener {
 
 	private static int NROFROWS = 5;
 	private static String TAG = "CheckBusActivity";
 	private static String chalmersURL = "http://api.vasttrafik.se/bin/rest.exe/v1/departureBoard?authKey=2443e74a-b1cd-466a-a4e2-72ac982a62df&format=json&id=9021014001960000&direction=9021014004490000";
 	private static String lindholmenURL= "http://api.vasttrafik.se/bin/rest.exe/v1/departureBoard?authKey=2443e74a-b1cd-466a-a4e2-72ac982a62df&format=json&id=9021014004490000&direction=9021014001960000";
 
 	private JSONObject[] returnedJsonObject;
 	private TableLayout lindholmenTable;
 	private TableLayout chalmersTable;
 	private ArrayList<String> chalmersLineArray;
 	private ArrayList<String> chalmersDestArray;
 	private ArrayList<String> chalmersTimeArray;
 	private ArrayList<String> chalmersTrackArray;
 
 	private ArrayList<String> lindholmenLineArray;
 	private ArrayList<String> lindholmenDestArray;
 	private ArrayList<String> lindholmenTimeArray;
 	private ArrayList<String> lindholmenTrackArray;
 	private Button refreshButton;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_checkbus);
 		refreshButton = (Button) findViewById(R.id.refreshbutton);
 		refreshButton.setOnClickListener(this);
 
 		chalmersTable = (TableLayout) findViewById(R.id.ChalmersTable);
 		lindholmenTable = (TableLayout) findViewById(R.id.LindholmenTable);
 		makeRows();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		//        getMenuInflater().inflate(R.menu.activity_campus_menu, menu);
 		return true;
 	}
 
 	/**
 	 * Delete all rows under the top row 
 	 **/
 	public void deleteRows(){
 		int chalmersRowsToDel = chalmersTable.getChildCount();
 		int lindholmenRowsToDel = lindholmenTable.getChildCount();
 
 		for (int i=chalmersRowsToDel-1;i>0;i--){
 			TableRow row = (TableRow) chalmersTable.getChildAt(i);
 			chalmersTable.removeView(row);
 		}
 
 		for (int j=lindholmenRowsToDel;j>0;j--){
 			TableRow row = (TableRow) lindholmenTable.getChildAt(j);
 			lindholmenTable.removeView(row);
 		}
 	}
 
 	/**
 	 * Makes new rows with content that shows departures
 	 */
 	public void makeRows(){
 		returnedJsonObject = null;
 		GetDepatures getDepatures = new GetDepatures();
 		getDepatures.execute();
 		parseDataToArrays();
 
 		for(int n = 0; n<2; n++){
 			for(int i = 0; i<NROFROWS; i++){ 
 				TableRow tempTableRow = new TableRow(this);
 				tempTableRow.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
 				tempTableRow.setBackgroundColor(Color.GRAY);
 				for(int j = 0; j<4; j++){
 					TextView textview = new TextView(this);
 					textview.setTextColor(Color.BLACK);
 					if(j == 0){
 						if(n == 0)
 							textview.setText(chalmersLineArray.get(i));
 						if(n == 1)
 							textview.setText(lindholmenLineArray.get(i));
 					}else if(j == 1){
 						if(n == 0)
 							textview.setText(chalmersDestArray.get(i));
 						if(n == 1)
 							textview.setText(lindholmenDestArray.get(i));
 					}else if(j == 2){
 						if(n == 0)
 							textview.setText(chalmersTimeArray.get(i));
						if(n == 1)
 							textview.setText(lindholmenTimeArray.get(i));
 					}else if(j == 3){
 						if(n == 0)
 							textview.setText(chalmersTrackArray.get(i));
 						if(n == 1)
 							textview.setText(lindholmenTrackArray.get(i));
 					}
 
 					textview.setGravity(Gravity.CENTER_HORIZONTAL);
 					tempTableRow.addView(textview);
 				}
 				if(n==0){
 					chalmersTable.addView(tempTableRow, new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
 				}
 				else if(n==1){
 					lindholmenTable.addView(tempTableRow, new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));	
 				}
 			}
 		}
 	}
 
 	/**
 	 * Refreshes the tables when clicking on the refresh button. 
 	 */
 	public void onClick(View v){
 		refreshTables();
 	}
 
 	/**
 	 * Saves all relevant data collected from the json-response 
 	 * in arrays for easy access when making table
 	 */
 	public void parseDataToArrays(){
 		chalmersLineArray = new ArrayList<String>();
 		chalmersDestArray = new ArrayList<String>();
 		chalmersTimeArray = new ArrayList<String>();
 		chalmersTrackArray = new ArrayList<String>();
 
 		lindholmenLineArray = new ArrayList<String>();
 		lindholmenDestArray = new ArrayList<String>();
 		lindholmenTimeArray = new ArrayList<String>();
 		lindholmenTrackArray = new ArrayList<String>();
 
 		while(returnedJsonObject == null){ //if json object not returned, sleep
 			try {
 				Thread.sleep(500);
 			} catch (InterruptedException e1) {
 				e1.printStackTrace();
 			}
 		}
 		for(int i=0;i<2;i++){
 			try {
 				JSONObject departureBoard = returnedJsonObject[i].getJSONObject("DepartureBoard");
 				JSONArray departureArray = departureBoard.getJSONArray("Departure");
 				for(int count = 0;count<departureArray.length();count++){
 					JSONObject depature = departureArray.getJSONObject(count);
 					String line = depature.getString("name");
 					String destination = depature.getString("direction");
 					String time = depature.getString("rtTime");
 					String track = depature.getString("track");
 					if(i == 0){
 						chalmersLineArray.add(line);
 						chalmersDestArray.add(destination);
 						chalmersTimeArray.add(time);
 						chalmersTrackArray.add(track);
 					}else if(i == 1){
 						lindholmenLineArray.add(line);
 						lindholmenDestArray.add(destination);
 						lindholmenTimeArray.add(time);
 						lindholmenTrackArray.add(track);
 					}
 				}
 
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 
 		}
 	}
 
 	/**
 	 * Refreshes the tables that hold info about departures
 	 */
 	public void refreshTables(){
 		this.deleteRows();
 		makeRows();
 
 	}
 
 
 	/****************************************************************************
 	 * this innerclass creates a new thread from where we can make a request
 	 *  to vsttrafik api - to get the directions
 	 * 	inspired by
 	 *  http://www.vogella.com/articles/AndroidPerformance/article.html
 	 ********************************************************************************/
 	private class GetDepatures extends AsyncTask<Void, Void, JSONObject[]> {
 
 
 		/** when called makes a request to vsttrafik api (json format) 
 		 *  gets the response back
 		 *  convertes the response to a jsonobject
 		 */
 		@Override
 		protected JSONObject[] doInBackground(Void... params) {
 
 			JSONObject[] tempJsonObject = new JSONObject[2];
 
 			//establish a connection with vsttrafik api
 			for(int i=0; i<2; i++){
 				StringBuilder response = new StringBuilder();
 				InputStream is = null;
 				URL url = null;
 				HttpURLConnection urlConnection = null;
 				String line = null;
 				String jsonResponse = "";
 				try {
 					if(i==0){
 						url = new URL(chalmersURL);
 					} else if(i == 1 ) {
 						url = new URL(lindholmenURL);
 					}
 					urlConnection = (HttpURLConnection) url.openConnection();
 					urlConnection.setRequestMethod("GET");
 					urlConnection.setDoOutput(true);
 					urlConnection.setDoInput(true);
 					is = urlConnection.getInputStream();
 					urlConnection.connect();
 				} catch (MalformedURLException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 				InputStreamReader inputStream = new InputStreamReader(is);
 				BufferedReader reader = new BufferedReader(inputStream);
 
 				//read from the buffer line by line and save in response (a stringbuider)
 				try{
 					while((line = reader.readLine()) != null){
 						response.append(line);
 					}
 					//Close the reader, stream & connection
 					reader.close();
 					inputStream.close();
 					urlConnection.disconnect();
 				}catch(Exception e) {
 					Log.e("Buffer Error", "Error converting result " + e.toString());
 				}
 
 				jsonResponse = response.toString();
 
 
 				//convert string to jsonobject and return the object
 				try{
 					tempJsonObject[i] = new JSONObject(jsonResponse);
 				}catch(JSONException e){
 
 				}
 			}
 			returnedJsonObject = tempJsonObject;
 			return returnedJsonObject;
 		}
 	}
 }
