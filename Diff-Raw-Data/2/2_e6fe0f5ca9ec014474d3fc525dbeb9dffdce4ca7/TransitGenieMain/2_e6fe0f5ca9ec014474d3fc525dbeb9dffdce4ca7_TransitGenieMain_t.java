 /* Transit Genie Android 
  * v. 1.0
  * Code by Mike Hutcheson and Allie Curry
  * 
  * -------------------------------------
  * Transit Genie Android Main Activity
  * -------------------------------------
  * Simple screen for users to see their selected origin/destination.
  * Starts upon application launch.
  * 
  * Layout used: main.xml
  * 
  * Includes: 
  * 
  * 		A) Two EditText boxes holding String version of current origin and destination selections.
  * 			Default values are "Use Current Location" for both EditText boxes.
  * 			Users may also directly type into the boxes (e.g. An address, Landmark).
  * 
  * 		B) Two "..." (aka "more") buttons to start 'places' Activity, (See .xml for clarification)
  * 			where users select their origin/destination from a list of popular places/favorites,
  * 			or via a Google Map.
  * 
  * 		C) "Go" button which starts 'Routes' Activity.
  * 			Which then implements the process of requesting routes from server.
  * 
  */
 
 package com.hutchdesign.transitgenie;
 
 import java.io.IOException;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.xml.sax.SAXException;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.app.TimePickerDialog;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Address;
 import android.location.Geocoder;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnKeyListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.TimePicker;
 import android.widget.Toast;
 
 public class TransitGenieMain extends Activity {
     protected static final int ORIGIN_REQUEST = 0;
 	private static final int TIME_DIALOG = 1;
     public static int ORIGIN_GPS = 1;
     public static int DEST_GPS = 1;
     public static Request request = new Request();
     Calendar c = Calendar.getInstance();
     public static SQLHelper SQL_HELPER;
     public static Cursor CURSOR;
     Bundle b;	//Holds data passed between main activity and places activity
     Geocoder geocoder;
     EditText origin_text;
     EditText dest_text;
 	private int mHour;
 	private int mMinute;
 	
 	public static Document[] allRoutes;
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         geocoder = new Geocoder(this.getApplicationContext());
         //Initialize SQL Database helper and cursor
         SQL_HELPER = new SQLHelper(this);
         CURSOR = getFavorites();
         
         //Set up GPS location manager
         LocationManager mlocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
         LocationListener mlocListener = new MyLocationListener();
         mlocManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 0, 0, mlocListener);
         //mlocManager.getLastKnownLocation(LOCATION_SERVICE);
         
         //Initialize Bundle and set default values
         b = new Bundle();
         //b.putString("origin_string", "Current Location");
         //b.putString("destin_string", "Current Location");
         
         //Import Buttons from main.xml
         Button button_go = (Button)findViewById(R.id.button_go);					//"Go" button on main screen (=> User is ready for routes)
         ImageButton button_origin = (ImageButton)findViewById(R.id.button_origin);	//Selected when user wishes to choose origin.
         ImageButton button_destn = (ImageButton)findViewById(R.id.button_destn);	//Selected when user wishes to choose destination.
         origin_text = (EditText)findViewById(R.id.text_origin2);
         dest_text = (EditText)findViewById(R.id.text_destn2);
         
         origin_text.setOnKeyListener(new CustomTextWatcher(origin_text, 0));
         dest_text.setOnKeyListener(new CustomTextWatcher(dest_text, 1));
 
         /* * * * * * * * * * * * * 
          * "GO" Button Listener  *
          * * * * * * * * * * * * */
         button_go.setOnClickListener(new View.OnClickListener(){	
 	    	public void onClick(View v){
 	    		
 	    		EditText destin = (EditText) findViewById(R.id.text_destn2);
 	    		EditText origin = (EditText) findViewById(R.id.text_origin2);
 	    		
 	    		if(origin.getText().length() <= 0)
 	    		{
 	    			Toast.makeText(getApplicationContext(), "Please input a location for Origin.", Toast.LENGTH_SHORT).show();
 	    			
 	    		}
 	    		else if(destin.getText().length() <= 0)
 	    		{
 	    			Toast.makeText(getApplicationContext(), "Please input a location for Destination.", Toast.LENGTH_SHORT).show();
 	    		}
 	    		
 	    		else
 	    		{
 	    			sendRequest.execute();	//Request data form server via Async. task
 	    		}
 	    	}
         });
         
         /*
          * SET ORIGIN
          * User wishes to choose origin.
          * Switch to new activity -> "places" (uses places.xml)
          * Retrieve user choice from places activity and set to their origin.
          */
         button_origin.setOnClickListener(new View.OnClickListener(){	
 	    	public void onClick(View v){
 	    		// reset GPS Flag
 	    		ORIGIN_GPS = 0;
 	    		//Run places activity
 	    		Intent i = new Intent(getApplicationContext(), places.class);
 	    		
 	    		b.putStringArrayList("favs", getFavoritesArrayList());
 	    		b.putInt("origin", 0);	//Set in Bundle 'b' that user is requesting origin.
 	    		i.putExtras(b);			//Pass Bundle 'b' to Places activity via Intent 'i'.
 	    		startActivityForResult(i, 0);
 	    	}
         });
         
         /*
          * SET DESTINATION
          * User wishes to choose destination.
          * Switch to new activity -> "places" (uses places.xml)
          * Retrieve user choice from places activity and set to their destination.
          */
         button_destn.setOnClickListener(new View.OnClickListener(){	
 	    	public void onClick(View v){
 	    		//Run places activity
 	    		Intent i = new Intent(getApplicationContext(), places.class);
 	    		
 	    		b.putStringArrayList("favs", getFavoritesArrayList());
 	    		b.putInt("origin", 1);	//Set in Bundle 'b' that user is requesting destination.
 	    		i.putExtras(b);			//Pass Bundle 'b' to Places activity via Intent 'i'.
 	            startActivityForResult(i, 1);
 	    	}
         }); 
        
     }//End onCreate
     private class CustomTextWatcher implements OnKeyListener{
     	private int dest;
     	Address address;
     	String text;
 		public CustomTextWatcher(EditText origin_text, int i) {
 			// TODO Auto-generated constructor stub
 			this.dest = i;
 			this.text = origin_text.getText().toString();
 		}
 
 		public boolean onKey(View v, int keyCode, KeyEvent event) {
 			if ((event.getAction() == KeyEvent.ACTION_DOWN)&& (keyCode == KeyEvent.KEYCODE_ENTER) )
 			{
 			try {
 				//this.address = geocoder.getFromLocationName(text, 2).get(0);
 				if(dest == 0){
 					this.address = geocoder.getFromLocationName(origin_text.getText().toString(), 1).get(0);
 					request.originLatitude = address.getLatitude();
 					request.originLongitude = address.getLongitude();
 					//if(address.getFeatureName() != null)
 						//{b.putString("origin_string", address.getFeatureName());}
 					b.putString("origin_string", origin_text.getText().toString());
 				}
 				else{
 					this.address = geocoder.getFromLocationName(dest_text.getText().toString(), 1).get(0);
 					request.destLatitude = address.getLatitude();
 					request.originLongitude = address.getLongitude();
 					//if(address.getFeatureName() != null)
 					//	{b.putString("destin_string", address.getFeatureName());}
 					b.putString("destin_string", origin_text.getText().toString());
 				}
 			} catch (Exception e) {
 				Toast.makeText( getApplicationContext(),
 					    "Can not resolve address",
 					    Toast.LENGTH_SHORT).show();
 			}
 			Log.i("Geocoder", this.text);
 			Log.i("Address", this.address.toString());
 			}
 
 			return false;
 		}
 
 		
     	
     }
     /* Class My Location Listener */
 
     public class MyLocationListener implements LocationListener
     {
     	public void onLocationChanged(Location loc)
 	    {
 		    loc.getLatitude();
 		
 		    loc.getLongitude();
 		
 		    String Text = "My current location is: " +
 		
 		    "Latitud = " + loc.getLatitude() +
 		
 		    "Longitud = " + loc.getLongitude();
 		
 		
 		    Toast.makeText( getApplicationContext(),
 		    Text,
 		    Toast.LENGTH_SHORT).show();
 	
 	    }
 	
 	
 	    public void onProviderDisabled(String provider)
 	    {
 	    	Toast.makeText( getApplicationContext(),
 		    "Gps Disabled",
 		    Toast.LENGTH_SHORT ).show();
 	
 	    }
 	
 	
 	    public void onProviderEnabled(String provider)
 	    {
 	
 	    	Toast.makeText( getApplicationContext(),
 		    "Gps Enabled",
 		    Toast.LENGTH_SHORT).show();
 	
 	    }
 	
 	    public void onStatusChanged(String provider, int status, Bundle extras)
 	    {
 	
 	
 	    }
 	    
     }//End MyLocationListener
     
     /*
     *	OnActivityResult is called upon the places.class Activity finishing.
     *	Used to receive Bundle from places,
     *   And set appropriate text in origin or destination editText (imported from main.xml).
     */
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     	Bundle bundl = data.getExtras();
     	
         // If the request went well (OK) and the request was ORIGIN_REQUEST
         if(resultCode == Activity.RESULT_OK && requestCode == ORIGIN_REQUEST) {
         	
         	EditText origin = (EditText) findViewById(R.id.text_origin2);
         	origin.setText(bundl.getString("origin_string"));
         	b.putString("origin_string", bundl.getString("origin_string"));
         	
 					            // Perform a query to the contact's content provider for the contact's name
 								//  Cursor cursor = getContentResolver().query(data.getData(),
 								//  new String[] {places.latitude}, null, null, null);
 								//  if (cursor.moveToFirst()) { // True if the cursor is not empty
 								//  int columnIndex = cursor.getColumnIndex(places.latitude);
 								//  request.originLatitude = cursor.getString(columnIndex);
 								//  request = null;// Do something with the selected contact's name...
 								//  }
         }
         
         //If the request went well (OK) and the request was for destination
         //Note: requestCode = 1 => Destination Request
         else if(resultCode == Activity.RESULT_OK && requestCode == 1) 
         {
         	EditText destn = (EditText) findViewById(R.id.text_destn2);
         	destn.setText(bundl.getString("destin_string"));
         	b.putString("destin_string", bundl.getString("destin_string"));
         }
         
         else if(resultCode == Activity.RESULT_CANCELED)
         {
         }
         
     }
     
     //Custom Menu
     //	Utilizes manu_main.xml
     //	Contains selections: 'Settings', 'Depart Time', 'About'
     @Override
     public boolean onCreateOptionsMenu(Menu menu) 
     {
     	MenuInflater inflater = getMenuInflater();
     	inflater.inflate(R.menu.menu_main, menu);
     	return true;
     }
     
     public boolean onOptionsItemSelected(MenuItem item)
     {
     	switch (item.getItemId())
     	{
 	    	case R.id.menu_settings:
 	    		//Intent i = new Intent(getApplicationContext(), MainMenu.class);
 	    		
 	    		//b.putStringArrayList("favs", getFavoritesArrayList());
 	    		//b.putInt("origin", 1);	//Set in Bundle 'b' that user is requesting destination.
 	    		//i.putExtras(b);			//Pass Bundle 'b' to Places activity via Intent 'i'.
 	            //startActivity(i);
				return true;
 			case R.id.menu_time:
 		        
 		        mHour = c.get(Calendar.HOUR_OF_DAY);
 		        mMinute = c.get(Calendar.MINUTE);
 				showDialog(TIME_DIALOG);
 				return true;
 			default:
 				return super.onOptionsItemSelected(item);
     	
     	}    	
     }
     @Override
     protected Dialog onCreateDialog(int id) {
         switch (id) {
         case TIME_DIALOG:
             return new TimePickerDialog(this,
                     mTimeSetListener, mHour, mMinute, false);
         }
         return null;
     }
  // the callback received when the user "sets" the time in the dialog
     private TimePickerDialog.OnTimeSetListener mTimeSetListener =
         new TimePickerDialog.OnTimeSetListener() {
             public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                 mHour = hourOfDay;
                 mMinute = minute;
                 c.set(Calendar.HOUR_OF_DAY, mHour);
                 c.set(Calendar.MINUTE, mMinute);
                 request.queryTime = c.getTimeInMillis()/1000L;
                 Log.i("Time", Long.toString(c.getTimeInMillis()/1000L));
                 Log.i("Current", Long.toString(System.currentTimeMillis()));
             }
         };
    
  /* * * * * * * * * * * * * * * * * * * * * * * * *
   * ASYNC TASK
   * 	Used to request server data.
   * 	Progress bar is diplayed while loading.
   * 
   * * * * * * * * * * * * * * * * * * * * * * * * */
  AsyncTask<String, Void, String> sendRequest = new AsyncTask<String, Void, String>() {
 	    Dialog progress;
 
 	    @Override
 	    protected void onPreExecute() {
 	        progress = ProgressDialog.show(TransitGenieMain.this, 
 	                "Loading Data", "Please Wait...");
 	        super.onPreExecute();
 	    }
 
 	    @Override
 	    protected String doInBackground(String... params) {
 	    	//RETRIEVE DOCUMENT
 	        allRoutes = null;	//Array of DOM trees, each representing a singular route.
 	        
 	        try {
 				allRoutes = TransitGenieMain.request.buildRoutes();
 			} catch (IOException e) {
 				e.printStackTrace();
 			} catch (ParserConfigurationException e) {
 				e.printStackTrace();
 			} catch (SAXException e) {
 				e.printStackTrace();
 			}
 			if(allRoutes == null)	//If no routes were added to array.
 			{
 				Toast.makeText(getApplicationContext(), "Error: No Routes Found. Check internet connectivity.", Toast.LENGTH_SHORT).show();
 				return "error";
 			}
 	        return "";
 	    }
 
 	    @Override
 	    protected void onPostExecute(String result) {
 	    	super.onPostExecute(result);
 	        progress.dismiss();
 	        
 	    	if(!result.equals("error")) //Run Routes activity if no error occured
 	    	{
 	    		Intent i = new Intent(getApplicationContext(), Routes.class);
 	    		i.putExtras(b);		// Bundle needed in next Activity to utilize Strings representing origin and destination.
 	            startActivity(i);
 	    	}  
 	    }
 	};
         
     //-----------------------------------------------------------
     //METHODS NEEDED FOR SQL DATABASE
 
     //Retrieve all favorite locations from the database
     private Cursor getFavorites() 
     {
     	SQLiteDatabase db = SQL_HELPER.getReadableDatabase();
     	Cursor cursor = db.query(SQLHelper.TABLE, null, null, null, null, null, null);
   
     	startManagingCursor(cursor);
     	return cursor;
     }
     
     //Retrieve all favorite locations from the database in the form of an array list
     private ArrayList<String> getFavoritesArrayList() 
     {
     	ArrayList<String> allNames = new ArrayList<String>();
     	CURSOR.moveToFirst();
     	
     	while (CURSOR.moveToNext())
     		allNames.add(CURSOR.getString(1));
 
     	return allNames;
     }
     
   //Add a favorite location to the database
     public static void addFavorite(String name, double latitude, double longitude) 
     {
       SQLiteDatabase db = SQL_HELPER.getWritableDatabase();
       ContentValues values = new ContentValues();
       
       //Add variables to SQLHelper
       values.put(SQLHelper.NAME, name);
       values.put(SQLHelper.LAT, latitude);
       values.put(SQLHelper.LON, longitude);
       
       //Add current data in SQLHelper to database
       db.insert(SQLHelper.TABLE, null, values);
     }
     
     //Delete favorite based on route name
 	public void deleteFavoriteByName(String name)
     {
     	SQLiteDatabase db = SQL_HELPER.getReadableDatabase();
     	db.delete(SQLHelper.TABLE, SQLHelper.NAME + "=?", new String[]{name});
     }
     
     //Delete favoirte based on database row id
     public void deleteFavoriteById(long id)
     {
     	SQLiteDatabase db = SQL_HELPER.getReadableDatabase();
     	db.delete(SQLHelper.TABLE, "_id=?", new String[]{String.valueOf(id)});
     }
     
     
     @Override
     public void onDestroy() 
     {
     	super.onDestroy();
     	
     	if(CURSOR != null)
     		CURSOR.close();
     	if(SQL_HELPER != null)
     		SQL_HELPER.close();
     }  
 }//End main class.
