 package com.example.seniordesignapp;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import android.app.SearchManager;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.speech.RecognizerIntent;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.NavUtils;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.MenuItem;
 import android.view.TextureView;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.app.Dialog;
 import android.content.DialogInterface;
 
 public class FoodTrackingFragment extends Fragment implements AdapterView.OnItemSelectedListener,TextWatcher{
 	
 	private static String TAG = "FoodTrackingActivity";
 	private Spinner spin_amount;
 	
 	//private AutoCompleteTextView itemAutoComplete_2,itemAutoComplete_3;
 	private String[] foodItem;
 	private ImageButton mbtSpeak,mbtSearch;
 	private Button mbtConfirm,mbtNewItem;
 	private EditText mEdtText;
 	private ListView mlv;
 	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1001;
 
 	/* After confirm button stuff */
 	// GPSTracker class
 	GPSTracker gps;
 	
 	//GPS parameters	
 	private LocationManager locationManager;
 	private LocationListener locationListener;
     private Double lon,lat;
     private Long gpstime;
     private DatabaseHelper mDbHelper;
     private SQLiteDatabase mDb;
     private Cursor mCursor;
     private String[] columns;
     private int[] to;
     private boolean isValid;
     private static final int DIALOG_CONFIRM = 10;
     
     // Sharing GL data
     SharedPreferences GLdata;
     public static String filename = "MySharedString";
     String glvalue = "" ;
     
     // Serving Size
     private TextView Serve_Size; 
     
     // Testing Variables
     private static String FOOD_TEST = "Testing_FoodTrackingActivity";
     long timeStart;
     long timeStop;
     final String VOICE_INPUT = "Voice"; 
     final String GPS_INPUT = "GPS"; 
     final String MANUAL_INPUT = "Manual"; 
     String inputMethod = "";
     private Button timerStart;
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 NavUtils.navigateUpFromSameTask(getActivity());
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
 
 	@Override
 	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
 			long arg3) {
 		// TODO Auto-generated method stub
 		//switch(arg0.getId()){
 		//case R.id.spinner1:
 		//default:
 		//}
 	}
 
 	@Override
 	public void onNothingSelected(AdapterView<?> arg0) {
 		// TODO Auto-generated method stub
 	}
 	
 	private void generateData(){
 		/*Initialize food database*/
     	mDbHelper = new DatabaseHelper(getActivity());
     	mDb = mDbHelper.getWritableDatabase();
 		foodItem=getFoodItemList();
 		columns = new String[]{
 				mDbHelper.COL_FOODITEM
 		};
 		to = new int[]{
 				R.id.foodlist
 		};
 		isValid = false;
 	}
 	String[] getFoodItemList(){
 		Cursor crs = mDb.rawQuery("SELECT food_name FROM food", null);
 		String[] array = new String[crs.getCount()];
 		int i = 0;
 		crs.moveToFirst();
 		while(crs.moveToNext()){
 		    String uname = crs.getString(crs.getColumnIndex("food_name"));
 		    array[i] = uname;
 		    i++;
 		}
 		return array;
 	}
 	
 	private void initializeAmountSpinners(){
 		ArrayAdapter<String> aa = null;
 		aa = new ArrayAdapter<String>(getActivity(),
 					android.R.layout.simple_spinner_item, new String[] {" ","1","2","3","4","5","6","7","8","9","10"});
 		aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		spin_amount.setAdapter(aa);
 	}
 
 	public boolean checkVoiceRecognition() {
 		  // Check if voice recognition is present
 		  PackageManager pm = getActivity().getPackageManager();
 		  List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
 		    RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
 		  if (activities.size() == 0) {
 		   mbtSpeak.setEnabled(false);
 		   //mbtSpeak.setText("Voice recognizer not present");
 		   Toast.makeText(this.getActivity(), "Voice recognizer not present",Toast.LENGTH_SHORT).show();
 		   return false;
 		  }
 	  return true;
 	}
 	
 	public void speak(View view) {
 		  Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
 
 		  // Specify the calling package to identify your application
 		  intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass()
 		    .getPackage().getName());
 
 		  // Display an hint to the user about what he should say.
 		  intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak Now, Example : 1 Hamburger");
 
 		  // Given an hint to the recognizer about what the user is going to say
 		  //There are two form of language model available
 		  //1.LANGUAGE_MODEL_WEB_SEARCH : For short phrases
 		  //2.LANGUAGE_MODEL_FREE_FORM  : If not sure about the words or phrases and its domain.
 		  intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
 
 		  // Specify how many results you want to receive. The results will be
 		  // sorted where the first result is the one with higher confidence.
 		  intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
 		  //Start the Voice recognizer activity for the result.
 		  startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
 		 }
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 
 	   //If Voice recognition is successful then it returns RESULT_OK
 	   if(requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == getActivity().RESULT_OK) {
 		   
 		 //  showToastMessage("OK");   
 	    ArrayList<String> textMatchList = data
 	    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
 
 	    if (!textMatchList.isEmpty()) {
 	     // If first Match contains the 'search' word
 	     // Then start web search.
 	     if (textMatchList.get(0).contains("search")) {
 
 	        String searchQuery = textMatchList.get(0);
 	                                           searchQuery = searchQuery.replace("search","");
 	        Intent search = new Intent(Intent.ACTION_WEB_SEARCH);
 	        search.putExtra(SearchManager.QUERY, searchQuery);
 	        startActivity(search);
 	     }
 	     else {
 	         // populate the Matches
 	    	 showToastMessage("Updated Text: "+ textMatchList.get(0));
 	    	 mEdtText.setText(textMatchList.get(0));
 	    	 checkData(textMatchList.get(0));
 	     }
 
 	    }
 	   //Result code for various error.
 	   }
 	   else if(resultCode == RecognizerIntent.RESULT_AUDIO_ERROR){
 	    showToastMessage("Audio Error");
 	   }
 	   else if(resultCode == RecognizerIntent.RESULT_CLIENT_ERROR){
 	    showToastMessage("Client Error");
 	   }
 	   else if(resultCode == RecognizerIntent.RESULT_NETWORK_ERROR){
 	    showToastMessage("Network Error");
 	   }
 	   else if(resultCode == RecognizerIntent.RESULT_NO_MATCH){
 	    showToastMessage("No Match");
 	   }
 	   else if(resultCode == RecognizerIntent.RESULT_SERVER_ERROR){
 	    showToastMessage("Server Error");
 	   }
 	  super.onActivityResult(requestCode, resultCode, data);
 	 }
 	void showToastMessage(String message){
 		  Toast.makeText(this.getActivity(), message, Toast.LENGTH_SHORT).show();
 	}
 	//helper routine to decide if a string is numeric
 	public boolean isInteger( String input){
 	   try{
 	      Integer.parseInt( input );
 	      return true;
 	   }
 	   catch( Exception e){
 	      return false;
 	   }
 	}
 	private void checkData(String in){
 		spin_amount.setSelection(0);
 		int setCount = 0;
 		String delims = "[ ]+";
 		String[] tokens = in.split(delims);
 		String temp = "";
 		for (int i = 0; i < tokens.length; i++){
 			if(isInteger(tokens[i])){
 				if(temp.isEmpty())
 					setQuantity(Integer.parseInt(tokens[i]));
 				else{
 					setQuantity(Integer.parseInt(tokens[i]));
 					checkFoodDatabase(temp);
 					setCount++;
 					temp="";
 				}
 			}
 			else{
 				if(temp.isEmpty())
 					temp=temp+tokens[i];
 				else
 					temp=temp+" "+tokens[i];
 //				checkFoodDatabase(tokens[i]);
 			}
 			if((i==tokens.length-1) &&!(temp.isEmpty()))
 				checkFoodDatabase(temp);
 		}
 		    
 	}
 	private void setQuantity(int in){
 		if(in<=10)
 			spin_amount.setSelection(in);
 		else
 			showToastMessage("Maximum is 10.");
 	}
 	private void checkFoodDatabase(String out){
 		//trim the "s" if there are any
 		//this will not be a problem for words end with 's' 
 		//because we are using % in the query
 		ArrayList<String> sData = new ArrayList<String>();
 		if(out.substring(out.length() - 1).equals("s"))
 			out=out.substring(0, out.length()-1);
 		String sql = "SELECT _ID,GL,Serve_Size,food_name FROM food WHERE lower(food_name) LIKE lower('%"+out+"%');";
 		mCursor = mDb.rawQuery(sql, null);
 		//if there is a match
 		if(mCursor.getCount()>0){ //now it is taking the first match WIP fix later
 			mCursor.moveToFirst();
 			sData.add(mCursor.getString(mCursor.getColumnIndex("food_name")));
 				while (!mCursor.isLast()) {
 					mCursor.moveToNext();
 				    String Name = mCursor.getString(mCursor.getColumnIndex("food_name"));
 				    sData.add(Name);
 				}
 		}
 		else{
 			sData.add("No matches");
 			showToastMessage(out+" does not exist in food database! Add it");
 		}
 		isValid=true;
 		mlv.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_single_choice, sData));
 	}
 	
 	@Override
 	public void afterTextChanged(Editable s) {
 		// TODO Auto-generated
 		}
 
 	@Override
 	public void beforeTextChanged(CharSequence s, int start, int count,
 			int after) {
 	}
 
 	@Override
 	public void onTextChanged(CharSequence s, int start, int before, int count) {
 	}
 	void disableKeyboard(){
 		//Hide Virtual Keyboard after checking data
     	InputMethodManager inputManager = (InputMethodManager)
                 getActivity().getSystemService(Context.INPUT_METHOD_SERVICE); 
 
     	inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
 	}
 	void searchAll(){
 		//Hide Virtual Keyboard after checking data
     	disableKeyboard();
     	String x = (String) mEdtText.getText().toString();
     	checkData(x);
 	}
 
 	void requestGPSupdate(){
 		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
 //		class MyLocThread extends Thread 
 //		{ 
 //		public MyLocThread () 
 //		{ 
 //			setDaemon(true); 
 //			setName("LocationThread"); 
 //		} 
 //
 //		public void run() 
 //		{ 
 //		//Looper.prepare(); 
 //		//mUserLocationHandler = new Handler();
 //			showToastMessage("HERE");
 //		locationManager.requestLocationUpdates( 
 //		  LocationManager.GPS_PROVIDER, 
 //		  0L, 
 //		  0L, 
 //		  locationListener,
 //		  Looper.getMainLooper()
 //		); 
 //		Looper.loop();
 //		}
 //	}
 //		getActivity().runOnUiThread(new MyLocThread());
 	}
 
 	
 	//public  void updateFoodGPSDatabase(String fName,Double lon,Double lat,int q,int gl, String time){
 	//Testing
 	public  void updateFoodGPSDatabase(String fName,Double lon,Double lat,int q,int gl, String time, String testInput, int stopTimer){
 		ContentValues value=new ContentValues();
 		
 //		String sql = "SELECT GL FROM food WHERE lower(food_name) LIKE lower('%"+fName+"%');";
 //		Cursor crs = mDb.rawQuery(sql, null);
 //		crs.moveToFirst();
 //		if(crs.getCount()==1)
 		
 		value.put("food_name", fName);
 		value.put("quantity", q);
 		value.put("GL", gl);
 		value.put("latitude", lat.toString());
 		value.put("longitude", lon.toString());
 		value.put("GPS_time", time);
 		//testing
 		value.put("input", testInput);
 		value.put("stoptimer", stopTimer);
 		Log.d(FOOD_TEST,"Inserting testing parameters");
 		Log.d(TAG, "INSERTING into FoodGPS latitude "+lat+"longitude"+lon + "food name "+ fName + "quantity " + q + "GL "+gl + "at " +time);
 		mDb.insert("FoodGPS",null,value);
         
     }
 
 	private ArrayList<String> refreshFrequentChoice() {
 
 		/* See Where is selected (GPS) */
 		/* Create GPS class object */
 		gps = new GPSTracker(getActivity());
 		/* Check if GPS enabled */
 		if (gps.canGetLocation()) {
 			lat = gps.getLatitude();
 			lon = gps.getLongitude();
 			// \n is for new line
 			Toast.makeText(getActivity(),
 					"Your Location is - \nLat: " + lat + "\nLong: " + lon,
 					Toast.LENGTH_LONG).show();
 		} else {
 			// can't get location
 			// GPS or Network is not enabled
 			// Ask user to enable GPS/network in settings
 			gps.showSettingsAlert();
 		}
 		float tmplong = 0;
 		float tmplat = 0;
 		float tmpdis= 0;
 		ContentValues value=new ContentValues();
 		String sql = "SELECT food_name,latitude, longitude, distance "
 				+ " FROM FoodGPS WHERE food_name NOT NULL";
 		mCursor = mDb.rawQuery(sql, null);
 		if (mCursor.getCount() > 0) { // now it is taking the first match WIP
 			// fix later
 		mCursor.moveToFirst();
 		tmplat = mCursor.getFloat(mCursor.getColumnIndex("latitude"));
 		tmplong = mCursor.getFloat(mCursor.getColumnIndex("longitude"));
 		tmpdis = distFrom(tmplat,tmplong, lat.floatValue(), lon.floatValue());
 		Log.d(TAG, "Adding distance " +tmpdis+ " to FoodGPS");		
 		value.put("distance", tmpdis);
 		mDb.update("FoodGPS", value, null, null);
 		while (!mCursor.isLast()) {
 			mCursor.moveToNext();
 			tmplat = mCursor.getFloat(mCursor.getColumnIndex("latitude"));
 			tmplong = mCursor.getFloat(mCursor.getColumnIndex("longitude"));
 			tmpdis = distFrom(tmplat,tmplong, lat.floatValue(), lon.floatValue());
 			Log.d(TAG, "Adding distance " +tmpdis+ " to FoodGPS");		
 			value.put("distance", tmpdis);
 			mDb.update("FoodGPS", value, null, null);
 			}
 		showToastMessage("Got " + tmplat + "\n& " + tmplong +"\ndistance is" + tmpdis);
 		}
 		
 		// Display on the list
 		ArrayList<String> sData = new ArrayList<String>();
 		 sql = "SELECT food_name, COUNT(food_name), distance, GL"
 				+ " FROM FoodGPS WHERE food_name NOT NULL"
 				+ " GROUP BY food_name" + " ORDER BY COUNT(food_name) DESC"
 				+ " LIMIT 5";
 		 
 		 sql = "SELECT food_name, distance, GL FROM (" + sql + ") ORDER BY distance DESC";
 		mCursor = mDb.rawQuery(sql, null);
 
 		if (mCursor.getCount() > 0) { // now it is taking the first match WIP
 										// fix later
 			//testing
 			inputMethod = GPS_INPUT;
 					
 			mCursor.moveToFirst();
 			sData.add(mCursor.getString(mCursor.getColumnIndex("food_name")));
 			while (!mCursor.isLast()) {
 				mCursor.moveToNext();
 				String Name = mCursor.getString(mCursor
 						.getColumnIndex("food_name"));
 				sData.add(Name);
 			}
 		}
 		isValid = true;
 		return sData;
 	}
 
 	public static float distFrom(float lat1, float lng1, float lat2, float lng2) {
 		double earthRadius = 3958.75;
 		double dLat = Math.toRadians(lat2 - lat1);
 		double dLng = Math.toRadians(lng2 - lng1);
 		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
 				+ Math.cos(Math.toRadians(lat1))
 				* Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2)
 				* Math.sin(dLng / 2);
 		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
 		double dist = earthRadius * c;
 
 		int meterConversion = 1609;
 
 		return new Float(dist * meterConversion).floatValue();
 	}
 	
 	private void uponConfirm(String x,final int spinner_pos, final int pos, final int glvalue,final String fName){
 		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 		builder.setMessage(x)
 				.setCancelable(false)
 				.setPositiveButton("No",
 						new DialogInterface.OnClickListener() {
 
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								dialog.cancel();
 
 							}
 						})
 				.setNegativeButton("Yes",
 						new DialogInterface.OnClickListener() {
 
 							@Override
 							public void onClick(DialogInterface dialog,
 									int id) {
 								
 								 /* Check Date & Time */
 				  				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
 				  				String currentDateandTime = sdf.format(new Date());
 				  				//timeStop = Long.parseLong(currentDateandTime);
 				  				timeStop = System.currentTimeMillis();
 				  				Log.d(TAG,"Time :"+currentDateandTime);
 			            		updateFoodGPSDatabase(mlv.getItemAtPosition(pos).toString(),lon,lat,spinner_pos,glvalue,currentDateandTime, inputMethod, (int) ((timeStop - timeStart)/1000));
 			            		
 			 	  				
 			            		 /* Testing Starts */
 			 	              	mDbHelper = new DatabaseHelper(getActivity());
 			 	    			mDb = mDbHelper.getWritableDatabase();
 			 	              	String sql = "SELECT food_name FROM foodGPS WHERE lower(food_name) LIKE lower('%"
 			 	    					+ mlv.getItemAtPosition(pos).toString() + "%');";
 			 	    			//String sql = "SELECT " + latitude + " FROM FoodGPS";
 			 	    			Cursor crs = mDb.rawQuery(sql, null);
 			 	    			//crs.moveToFirst();
 			 	    			String name = ""; 
 			 	    			if (crs.getCount() > 0) { // now it is taking the first
 			 	    				// match
 			 	    				// WIP fix later
 			 	    				crs.moveToFirst();
 			 	    				name = crs.getString(crs.getColumnIndex("food_name"));
 			 	    			} else {
 			 	    				name = name + " does not exist in food database! Add it";
 			 	    			}
 			 	    			mDbHelper = new DatabaseHelper(getActivity());
 			 	    			mDb = mDbHelper.getWritableDatabase();
 			 	    			sql = "SELECT latitude FROM foodGPS WHERE lower(food_name) LIKE lower('%"
 			 	    					+ mlv.getItemAtPosition(pos).toString() + "%');";
 			 	    			crs = mDb.rawQuery(sql, null);
 			 	    			String tmplat = "";
 			 	    			if (crs.getCount() > 0) { // now it is taking the first
 			 	    				// match
 			 	    				// WIP fix later
 			 	    				crs.moveToFirst();
 			 	    				tmplat = crs.getString(crs.getColumnIndex("latitude"));
 			 	    			} else {
 			 	    				tmplat = tmplat + " does not exist in food database! Add it";
 			 	    			}
 			 	    			mDbHelper = new DatabaseHelper(getActivity());
 			 	    			mDb = mDbHelper.getWritableDatabase();
 			 	    			sql = "SELECT longitude FROM foodGPS WHERE lower(food_name) LIKE lower('%"
 			 	    					+ mlv.getItemAtPosition(pos).toString() + "%');";
 			 	    			crs = mDb.rawQuery(sql, null);
 			 	    			String tmplon = "";
 			 	    			if (crs.getCount() > 0) { // now it is taking the first
 			 	    				// match
 			 	    				// WIP fix later
 			 	    				crs.moveToFirst();
 			 	    				tmplon = crs.getString(crs.getColumnIndex("longitude"));
 			 	    			} else {
 			 	    				tmplon = tmplon + " does not exist in food database! Add it";
 			 	    			}
 		 	   			mDbHelper = new DatabaseHelper(getActivity());
 			 	    			mDb = mDbHelper.getWritableDatabase();
 			 	    			sql = "SELECT GPS_time FROM foodGPS WHERE lower(food_name) LIKE lower('%"
 			 	    					+ mlv.getItemAtPosition(pos).toString() + "%');";
 			 	    			crs = mDb.rawQuery(sql, null);
 			 	    			String tmptime = "";
 			 	    			if (crs.getCount() > 0) { // now it is taking the first
 			 	    				// match
 			 	    				// WIP fix later
 			 	    				crs.moveToFirst();
 			 	    				tmptime = crs.getString(crs.getColumnIndex("GPS_time"));
 			 	    			} else {
 			 	    				tmptime = tmptime + " does not exist in food database! Add it";
 			 	    			}
 			 	    			/* Testing Ends*/
 			 	    			
 			 	    			
 		          				 ArrayList<String> in = new ArrayList<String>();
 		          				 in.add("No Results");
 		          				 mlv.setAdapter(new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_single_choice, in));
 		          				 spin_amount.setSelection(0);
 		          				 mEdtText.setText("");
 		          				 isValid=false;
 		          				 showToastMessage("Recorded "+spinner_pos+" "+fName);
 		          				 Toast.makeText(getActivity(), "DB has recorded:\n" + name + " at\nLongitude = " + tmplon +"\nLatitude = " + tmplat
 		          						 + "\nTime: " + tmptime + "\nTesting Input Type: " + inputMethod
 		          						 + "\nTesting Time Taken: " + ((timeStop - timeStart)/1000 ) + " sec",Toast.LENGTH_LONG).show();
 							}
 							
 						});
 
 		AlertDialog alert = builder.create();
 		alert.show();
 
 	}
 
 	
 
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		if (container == null) {
             // We have different layouts, and in one of them this
             // fragment's containing frame doesn't exist.  The fragment
             // may still be created from its saved state, but there is
             // no reason to try to create its view hierarchy because it
             // won't be displayed.  Note this is not needed -- we could
             // just run the code below, where we would create and return
             // the view hierarchy; it would just never be used.
             return null;
         }
 		
 		
 			
 			
 			LinearLayout mlinearLayout = (LinearLayout)inflater.inflate(R.layout.fragment_food_tracking, container, false);
 			
 			/* Testing Start */
 			
 			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
 			String currentDateandTime = sdf.format(new Date());
 			//timeStart = Long.parseLong(currentDateandTime);
 			timeStart = System.currentTimeMillis();
 			
 			timerStart = (Button) mlinearLayout.findViewById(R.id.start_timer);
 			timerStart.setOnClickListener( new OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					
 					SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
 					String currentDateandTime = sdf.format(new Date());
 					//timeStart = Long.parseLong(currentDateandTime);
 					timeStart = System.currentTimeMillis();
 					showToastMessage("Start!!!");
 					
 				}
 			});
 			/* Testing Ends */
 			
 			/*Generate Food Data*/
 	    	generateData();
 	    	/*Initialize Location manager*/
 	    	// Acquire a reference to the system Location Manager
         	locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
         	
         	/* Sharing GL data */
         	GLdata = getActivity().getSharedPreferences(filename, 0);
         	
         	/*search bar*/
         	mEdtText = (EditText) mlinearLayout.findViewById(R.id.search_src_text);
  	        mEdtText.setOnKeyListener(new View.OnKeyListener() {
  	            public boolean onKey(View v, int keyCode, KeyEvent event) {
  	                if (keyCode==KeyEvent.KEYCODE_ENTER) { 
  	                	searchAll();
  	                }
  	            return false;
  	            }
  	        });
  	        mbtSearch = (ImageButton) mlinearLayout.findViewById(R.id.search_btn);
 	        mbtSearch.setOnClickListener(new View.OnClickListener() {
 		        	public void onClick(View v) {
 			            if (v.getId() == R.id.search_btn) {
 			            	//testing
 			            	inputMethod = MANUAL_INPUT;
 			                //listen for results
 			            	searchAll();
 			            } 
 		        	}
 					public boolean onKey(View v, int keyCode, KeyEvent event) {
 						// TODO Auto-generated method stub
 						return false;
 					}
 		        });
 	        mbtSpeak = (ImageButton) mlinearLayout.findViewById(R.id.voice_btn);
 	        if(checkVoiceRecognition()==true){
 		        mbtSpeak.setOnClickListener(new View.OnClickListener() {
 		        	public void onClick(View v) {
 		            if (v.getId() == R.id.voice_btn) {
 		            	//testing
 		            	inputMethod = VOICE_INPUT;
 		                //listen for results
 		                speak(v);
 		            }
 		        	}
 		        });
 	        }
 
 	        
         	/*initialize spinner */
 	        spin_amount = (Spinner) mlinearLayout.findViewById(R.id.spinner);
 	        initializeAmountSpinners();
 	        /* initialize listview*/
 	        ArrayList<String> sData = new ArrayList<String>();
 	        sData = refreshFrequentChoice();
 	        //sData.add("No matches");
 	        mlv = (ListView) mlinearLayout.findViewById(R.id.foodlist);
 	        mlv.setAdapter(new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_single_choice, sData));
 	        mlv.setItemsCanFocus(false);
 		    mlv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
 		    Serve_Size = (TextView) mlinearLayout.findViewById(R.id.servesize);
 		    
 		mlv.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1,
 					int position, long arg3) {
 
 				mDbHelper = new DatabaseHelper(getActivity());
 				mDb = mDbHelper.getWritableDatabase();
 				String sql = "SELECT Serve_Size FROM food WHERE lower(food_name) LIKE lower('%"
 						+ mlv.getItemAtPosition(position).toString() + "%');";
 				Cursor crs = mDb.rawQuery(sql, null);
 
 				crs.moveToFirst();
 
 				int serve_size = crs.getInt(crs.getColumnIndex("Serve_Size"));
 				Serve_Size.setText("("+serve_size + " grams)");
				//mDb.close();
 				// showToastMessage(mlv.getItemAtPosition(position).toString());
 
 			}
 		});
 	        
 		    SharedPreferences.Editor editor = GLdata.edit();
 		    	  		editor.putString("TotalGL", glvalue);
 		    	  		editor.commit();
 		    
 	        /*Confirm button and new food button*/
 	        final Button newFoodBtn = (Button) mlinearLayout.findViewById(R.id.newitembutton);
 	        newFoodBtn.setOnClickListener(new View.OnClickListener() {
 	            public void onClick(View v) {
 	                // Perform action on click
 	            	
 	            	Intent intent = new Intent(FoodTrackingFragment.this.getActivity(),NewFoodActivity.class);
 	    	        startActivity(intent); 
 	            }
 	        });
 	        
 	        mbtConfirm = (Button) mlinearLayout.findViewById(R.id.confirmbutton);
 	        
 	        
 	        //mbtConfirm.setEnabled(false);
 	        mbtConfirm.setOnClickListener(new View.OnClickListener() {
 	            public void onClick(View v) {
 	            	//initializeFrequentSpinner();
 	            	// Define a listener that responds to location updates
 	            	locationListener = new LocationListener() {
 
 	            	    public void onStatusChanged(String provider, int status, Bundle extras) {}
 
 	            	    public void onProviderEnabled(String provider) {}
 
 	            	    public void onProviderDisabled(String provider) {}
 
 	    				@Override
 	    				public void onLocationChanged(Location loc) {
 	    					// get longitude,latitude and time data
 	    	                 //ContentValues values = new ContentValues();
 	    	                  lon = loc.getLongitude();
 	    	                  lat = loc.getLatitude();
 	    	                  gpstime = loc.getTime();
 	    	                  showToastMessage("longitude "+lon+"latitude "+lat+"time "+gpstime);
 	    				}
 	            	  };
 	            	  
 	            	 
 	  				
 
 	  				
 	            	  /* request location and store to database*/
 	            	 // Register the listener with the Location Manager to receive location updates
 	            	  //requestGPSupdate();
 	            	  //updateFoodGPSDatabase(lon,lat);
 	            	  
 	            	  //for testing
 	            	  //lon = 5.2;
 	            	  //lat = 5.2;
 	            	  //Get the position of user's selection
 	            	 int pos = mlv.getCheckedItemPosition();
 	            	 int spinner_pos =  spin_amount.getSelectedItemPosition();
 	            	 
 	            	 
 	            	 
 //	            	ArrayList<Integer> foodIDs = new ArrayList<Integer>();
 	            	ArrayList<Integer> GLs = new ArrayList<Integer>();
 	            	if(isValid){
 	          		//if there is a match
 		            	mCursor.moveToFirst();
 		          		if(mCursor.getCount()>0){ //now it is taking the first match WIP fix later
 		          			
 		          				while (!mCursor.isAfterLast()) {
 		          					int gl = mCursor.getInt(mCursor.getColumnIndex("GL"));
 		    	          			GLs.add(gl);
 
 		    	          			mCursor.moveToNext();
 		          				}
 		          			  if((pos>=0) && spinner_pos > 0){ //if listview selected
 		          				  String fName = mlv.getItemAtPosition(pos).toString();
 		          				            				  
 		          				  uponConfirm(fName+"\n"+" Quantity: "+spinner_pos+"\n "+"Is this correct?",spinner_pos,pos,GLs.get(pos),fName);
 		          				  
 		          			  }
 		          			  else if(pos<0 && spinner_pos <= 0)
 		          				  showToastMessage("Please select one from list and confirm quantity");
 		          			  else if(pos >= 0 && spinner_pos <= 0)
 		          				  showToastMessage("Please confirm quantity");
 		          			  else if(pos < 0 && spinner_pos > 0)
 		          				 showToastMessage("Please select one from list");
 		          		}
 		          		else{
 		          			showToastMessage("No Result");
 		          		}
 	            	} 
 	            	else //if not valid
 	            		showToastMessage("Enter Food Item First");
 	            	
 	            } 
 	          //remove listener
             	//locationManager.removeUpdates (locationListener);
 	            	
 	        });
 	       
 
 	        return mlinearLayout;
 	}
 }
