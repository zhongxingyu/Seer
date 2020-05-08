 package com.cmozie.java2week1;
 
 import java.util.ArrayList;
import java.util.HashMap;

 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.Messenger;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.cmozie.Libz.FileStuff;
 import com.cmozie.classes.*;
 import webConnections.*;
 
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class MainActivity.
  */
 
 @SuppressLint("HandlerLeak")
 public class MainActivity extends Activity {
 
 	//--public statics
 	public static Context _context;
 	//public static LocationDisplay _locationDetails;
 	public static Button searchButton;
 	
 	FileStuff m_file;
 	
 	
 	//layout
 	TextView _popularZips;
 	Button _pop;
 	
 	//bool
 	Boolean _connected = false;
 	
 	//class declarations
 	//FavDisplay _favorites;
 	
 	//strings
 	String _zipcode;
 	String _areaCode;
 	String _city;
 	String _county;
 	String _state;
 	String _latitude;
 	String _longitude;
 	String _csa_name;
 	String _cbsa_name;
 	String _region;
 	String _timezone;
 
 	
 	
	HashMap<String, String> _history;
 	
 	ArrayList<String>_stacks = new ArrayList<String>();
 	
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onCreate(android.os.Bundle)
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		//m_file = FileStuff.getInstance();
 		
 		//inflating my form.xml
 		View view;
 		LayoutInflater layoutInflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		view = layoutInflater.inflate(R.layout.form, null);
 		
 		//setting contentView to my inflated view/form
 		setContentView(view);
 		
 		
 		
 		_context = this;
 		//_popularZips = new TextView(this);
 		
 		//sets _history to the get history call
 		//_history = getHistory();
 		
 		//adding items to the spinner
 		//san francisco
 		/*_stacks.add("94105");
 		//Miami
 		_stacks.add("33133");
 		//washington dc
 		_stacks.add("20001");
 		//time square
 		_stacks.add("10036");
 		//Chicago
 		_stacks.add("60106");*/
 		
 		 
 		 //logs the _history text if inside local storage
 		//Log.i("HISTORY READ", _history.toString());
 	 
 		//allows to target the search button in the search form to set onclick event
 		searchButton = (Button) findViewById(R.id.searchButton);
 		//searchButton.isPressed() && sField.getText().length() > 1
 		//search button on click listener
 		 searchButton.setOnClickListener(new View.OnClickListener() {
 		
 			@Override
 			public void onClick(View view) {
 			EditText sField = (EditText) findViewById(R.id.searchField);
 					//if the search button is pressed and the text field length is greater than 1 go ahead and search
 			
 				if (sField.getText().length() < 1) {
 					
 					Toast toast = Toast.makeText(_context, "Please Enter a Correct Zipcode", Toast.LENGTH_LONG);
 					toast.show();
 				
 					
 				}else {
 					
 					Handler zipcodeHandler = new Handler() {
 
 						
 						@Override
 						public void handleMessage(Message msg) {
 							// TODO Auto-generated method stub
 							String fullURLString = msg.obj.toString() ;
 							if (msg.arg1 == RESULT_OK && msg.obj != null) 
								Log.i("Serv.Response", msg.obj.toString());
 							
 							{
 								try {
 									Log.i("Second", "TEST");
 									JSONObject json = new JSONObject(fullURLString);
 									
 									JSONArray ja = json.getJSONArray("zips");
 									
 									for (int i = 0; i < ja.length(); i++) {
 										//sets a json object to access object values inside array
 										
 										
 										JSONObject one = ja.getJSONObject(i);
 										
 									//setting my text to the values to the strings of the json data
 									_zipcode = one.getString("zip_code");
 									_areaCode = one.getString("area_code");
 									_city = one.getString("city");
 									_state = one.getString("state");
 									_county = one.getString("county");
 									_csa_name = one.getString("csa_name");
 									_cbsa_name = one.getString("cbsa_name");
 									_latitude = one.getString("latitude");
 									_longitude = one.getString("longitude");
 									_region = one.getString("region");
 									_timezone = one.getString("time_zone");
 										 
 									}
 									Log.i("one", _areaCode + _city + _state + _county + _csa_name + _cbsa_name + _latitude + _longitude + _region + _timezone);
 								
 									//sets the values of the text by calling the locationInfo function inside of my Locationdisplay class
 									locationInfo(_areaCode, _city, _county, _state, _latitude, _longitude, _csa_name, _cbsa_name, _region, _timezone);  
 								
 									
									
 									//Trying to read the file stored.
 									Toast toast = Toast.makeText(getBaseContext(),"Read -->" + FileStuff.readStringFile(_context, "temp", false), Toast.LENGTH_SHORT);
 									
 									toast.show();
 									
 									//m_file.storeStringFile(_context, fullURLString, content, external)
 									
 									
 								} catch (Exception e) {
 									// TODO: handle exception
 									//Alert for any error in entering into textfield if not a zipcode
 									AlertDialog.Builder alert = new AlertDialog.Builder(_context);
 									alert.setTitle("Error");
 									alert.setMessage("There was an error searching for your request. Check connections or make sure zipcode is correct. USA zipcodes only.");
 									alert.setCancelable(false);
 									alert.setPositiveButton("Alright", new DialogInterface.OnClickListener() {
 										
 										@Override
 										public void onClick(DialogInterface dialog, int which) {
 
 											dialog.cancel();
 										}
 									});
 									alert.show();
 									Log.e("Handle Message", e.getMessage().toString());
 								}
 								
 		
 								
 							}						
 							
 						}
 						
 						
 					};
 					
 					Messenger zipcodeMessenger = new Messenger(zipcodeHandler);
 					
 					Intent startZipcodeIntent = new Intent(_context, ZipcodeService.class);
 					startZipcodeIntent.putExtra(ZipcodeService.MESSENGER_KEY, zipcodeMessenger);
 					startZipcodeIntent.putExtra(ZipcodeService.enteredZipcode, sField.getText().toString());
 					
 					startService(startZipcodeIntent);
 					
 						//getLookup(sField.getText().toString());
 						
 						//hides keyboard.
 						InputMethodManager imm = (InputMethodManager)getSystemService(
 						Context.INPUT_METHOD_SERVICE);
 						imm.hideSoftInputFromWindow(sField.getWindowToken(), 0);
 						searchButton.setEnabled(true);
 					}
 										//empties the search field
 				sField.setText("");
 				
 			}
 			
 		});
 		
 		 
 		 //webConnection jar file usage
 		 _connected = WebStuff.getConnectionStatus(_context);
 		 if (_connected) {
 			Log.i("Network Connection", WebStuff.getConnectionType(_context));
 			
 			//if no connection
 		}else if(!_connected) {
 			
 			//alert for connection
 			AlertDialog.Builder alert = new AlertDialog.Builder(_context);
 			alert.setTitle("Connection Required!");
 			alert.setMessage("You need to connect to an internet service!");
 			alert.setCancelable(false);
 			alert.setPositiveButton("Alright", new DialogInterface.OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 
 					dialog.cancel();
 				}
 			});
 			alert.show();
 			searchButton.setClickable(false);
 		}
 		 
 		
 	}
 		 //ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(_context, android.R.layout.simple_spinner_item, _stacks);
 		 //listAdapter.setDropDownViewResource(android.R.layout.simple_list_item_checked);
 		 //((Spinner) findViewById(R.id.favList)).setAdapter(listAdapter);
 		
 	
 		 //popular zipcodes onclick
 		 //_pop = (Button) findViewById(R.id.popularZipcodes);
 		 /*_pop.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					// TODO Auto-generated method stub
 					
 					//informing the user how to select a popular zipcode
 					AlertDialog.Builder alert = new AlertDialog.Builder(_context);
 					alert.setTitle("What to do?");
 					alert.setMessage("Click the yellow box to view the popular zipcodes");
 					alert.setCancelable(false);
 					alert.setPositiveButton("Thanks!", new DialogInterface.OnClickListener() {
 						
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 
 							dialog.cancel();
 						}
 					});
 					alert.show();
 					//adds the _favorites/spinner to the view button is clicked
 					Spinner spinnerObj = (Spinner) findViewById(R.id.favList);
 					
 					
 					spinnerObj.setVisibility(View.VISIBLE);
 					((Spinner) findViewById(R.id.favList)).setOnItemSelectedListener(new OnItemSelectedListener() {
 						
 						
 						public void onItemSelected(AdapterView<?> parent,View v,int pos, long id){
 							
 							String selected = parent.getItemAtPosition(pos).toString();
 							Log.i("Favorite Selected", selected);
 							
 							
 							
 							//trying to call this function and pass in the selectedItemAtPosition string to the function to run the api query on the selected zipcode in the spinner.
 							//getLookup(selected);
 							
 						}
 						
 						@Override
 						public void onNothingSelected(AdapterView<?>parent){
 							Log.i("Aborted", "None Selected");
 							
 						}
 						
 						
 					});
 					
 					//sets button to non clickable once clicked once 
 					_pop.setClickable(false);
 					
 					
 				}
 					
 			});
 		
 	
 		 _pop.setText("Click here for popular zipcodes");
 
 	
 	}*/
 	
 /**
  * Location info.
  *
  * @param area_code the area_code
  * @param city the city
  * @param county the county
  * @param state the state
  * @param latitude the latitude
  * @param longitude the longitude
  * @param csa_name the csa_name
  * @param cbsa_name the cbsa_name
  * @param region the region
  * @param timezone the timezone
  */
 public void locationInfo(String area_code, String city, String county, String state, String latitude, String longitude, String csa_name, String cbsa_name, String region, String timezone) {
 		
 		((TextView) findViewById(R.id.location_areacode)).setText(area_code);
 		((TextView) findViewById(R.id.location_city)).setText(city);;
 		((TextView) findViewById(R.id.location_county)).setText(county);
 		((TextView) findViewById(R.id.location_state)).setText(state);
 		((TextView) findViewById(R.id.location_latitude)).setText(latitude);
 		((TextView) findViewById(R.id.location_longitude)).setText(longitude);
 		((TextView) findViewById(R.id.location_csa_name)).setText(csa_name);
 		((TextView) findViewById(R.id.location_cbsa_name)).setText(cbsa_name);
 		((TextView) findViewById(R.id.location_region)).setText(region);
 		((TextView) findViewById(R.id.location_timezone)).setText(timezone);
 }
 	
 	
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	
 	
 	//get lookup function that allows me to query the api on on click of the search --see searchButton.OnClick 
 	/**
 	 * Gets the lookup.
 	 *
 	 * @param zipcode the zipcode
 	 * @return the lookup
 	 */
 	/*public void getLookup(String zipcode){
 		
 		//this is the base url of the api
 		String baseURL = "http://zipfeeder.us/zip?";
 		
 		//key needed to use api
 		String key = "key=EN4GbNMq";
 		//this empty string accepts an empty string which will be for zipcodes entered
 		String qs = "";
 		try{
 			//allows the empty string to recieve the zipcode string encoded
 			qs = URLEncoder.encode(zipcode, "UTF-8");
 		}catch (Exception e) {
 			
 			//if an error in the api show the bad url alert
 			AlertDialog.Builder alert = new AlertDialog.Builder(_context);
 			alert.setTitle("Bad URL");
 			alert.setMessage("An error occured the in the construction of the URL.Encoding problem.");
 			alert.setCancelable(false);
 			alert.setPositiveButton("Sorry", new DialogInterface.OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 
 					dialog.cancel();
 				}
 			});
 			alert.show();
 			Log.e("Bad URL","Encoding Problem");
 			qs = "";
 		}
 		
 		//creates finalURL as a URL
 		URL finalURL;
 		try{
 			//sets the final url to the base plus the api key with the string parameter needed for search as well as the empty string that recieves a zipcode.
 			finalURL = new URL (baseURL + key + "&zips=" + qs);
 			
 			//logs the final url query
 			Log.i("URL",finalURL.toString());
 			
 			mainZipRequest qr = new mainZipRequest();
 			qr.execute(finalURL);
 		}catch (MalformedURLException e){
 			Log.e("BAD URL", "Malformed URL");
 			finalURL = null;
 		}
 	}
 	*/
 	/**
 	 * The Class mainZipRequest.
 	 */
 	/*private class mainZipRequest extends AsyncTask<URL, Void, String>{
 		
 		 (non-Javadoc)
 		 * @see android.os.AsyncTask#doInBackground(Params[])
 		 
 		@Override
 		protected String doInBackground(URL...urls){
 			String response = "";
 		for (URL url : urls) {
 			response = WebStuff.getURLStringResponse(url);
 		}
 			return response;
 		}*/
 		
 		/* (non-Javadoc)
 		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
 		 */
 		/*@Override
 		protected void onPostExecute(String result){
 			
 			Log.i("URL RESPONSE", result);
 			try {
 				
 				//creates a json object and json array to access my json objects
 				JSONObject json = new JSONObject(result);
 				JSONArray ja = json.getJSONArray("zips");
 				
 				
 				Log.i("results",result);
 				
 			
 				 
 					//loops through json array 
 					for (int i = 0; i < ja.length(); i++) {
 						//sets a json object to access object values inside array
 						JSONObject one = ja.getJSONObject(i);
 						
 					//setting my text to the values to the strings of the json data
 					_zipcode = one.getString("zip_code");
 					_areaCode = one.getString("area_code");
 					_city = one.getString("city");
 					_state = one.getString("state");
 					_county = one.getString("county");
 					_csa_name = one.getString("csa_name");
 					_cbsa_name = one.getString("cbsa_name");
 					_latitude = one.getString("latitude");
 					_longitude = one.getString("longitude");
 					_region = one.getString("region");
 					_timezone = one.getString("time_zone");
 						 
 					}
 					Log.i("one", _areaCode + _city + _state + _county + _csa_name + _cbsa_name + _latitude + _longitude + _region + _timezone);
 				
 					//sets the values of the text by calling the locationInfo function inside of my Locationdisplay class
 					locationInfo(_areaCode, _city, _county, _state, _latitude, _longitude, _csa_name, _cbsa_name, _region, _timezone);  
 				
 					//confims zipcode is valie
 					Toast toast = Toast.makeText(_context, "Valid Zipcode " + _zipcode , Toast.LENGTH_SHORT);
 					toast.show();
 					
 		
 					
 					
 
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				//Alert for any error in entering into textfield if not a zipcode
 				AlertDialog.Builder alert = new AlertDialog.Builder(_context);
 				alert.setTitle("Error");
 				alert.setMessage("There was an error searching for your request. Check connections or make sure zipcode is correct. USA zipcodes only.");
 				alert.setCancelable(false);
 				alert.setPositiveButton("Alright", new DialogInterface.OnClickListener() {
 					
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 
 						dialog.cancel();
 					}
 				});
 				alert.show();
 				e.printStackTrace();
 				Log.e("JSON","JSON OBJECT EXCEPTION");
 			}
 		}
 		
 	}*/
 		/*@SuppressWarnings("unchecked")
 		private HashMap<String, String> getHistory(){
 			
 			//creates an object named stored that reads the object that is stored in local storage
			Object stored = FileStuff.readStringFile(_context, "temp", false);
 			
 			//declares the hashmap history variable
 			HashMap<String, String> history;
 			
 			//if theres an error fire alert
 			if (stored == null) {
 				Log.i("HISTORY","NO HISTORY FILE FOUND");
 				AlertDialog.Builder alert = new AlertDialog.Builder(_context);
 				alert.setTitle("Saved Files");
 				alert.setMessage("There are no saved zipcodes in local storage. Once a search is made the zipcode will be saved.");
 				alert.setCancelable(false);
 				alert.setPositiveButton("Alright", new DialogInterface.OnClickListener() {
 					
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 
 						dialog.cancel();
 					}
 				});
 				alert.show();
 				history = new HashMap<String, String>();
 				
 				//else store it into the history
 			}	else {
 				history = (HashMap<String, String>) stored;
 			}
 			return history;
 			
 		}*/
 	
 }
