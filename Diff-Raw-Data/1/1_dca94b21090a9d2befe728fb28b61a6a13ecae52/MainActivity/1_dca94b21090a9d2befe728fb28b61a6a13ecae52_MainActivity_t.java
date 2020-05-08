 /* Scott Caruso
  * Java 1 - 1307
  * Week 4 Project
  */
 package com.scottcaruso.mygov;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.scottcaruso.datafunctions.DataRetrievalService;
 import com.scottcaruso.datafunctions.SaveFavoritesLocally;
 import com.scottcaruso.datafunctions.SavedPoliticianProvider;
 import com.scottcaruso.datafunctions.SavedPoliticianProvider.PoliticianData;
 import com.scottcaruso.interfacefunctions.DisplayPoliticianResults;
 import com.scottcaruso.utilities.Connection_Verification;
 
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.Messenger;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.MatrixCursor;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 	
 	static Context currentContext;
 	public static JSONObject jsonResponse;
 	static Spinner queryChoice;
 	public static Cursor thisCursor;
 
 	@Override
 	protected void onSaveInstanceState(Bundle savedInstanceState) {
 		boolean viewingDisplay = DisplayPoliticianResults.isViewingDisplay();
 		boolean backButton = DisplayPoliticianResults.isBackButtonClicked();
 		if (viewingDisplay == true && backButton == false)
 		{
 			savedInstanceState.putBoolean("display exists", true);
 			savedInstanceState.putString("json object", DisplayPoliticianResults.getMasterPolObject().toString());
 		} else
 		{
 			savedInstanceState.putBoolean("display exists", false);
 		}
 		super.onSaveInstanceState(savedInstanceState);
 	}
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         if (savedInstanceState != null)
         {
         	boolean displayExists = savedInstanceState.getBoolean("display exists");
         	if (displayExists)
         	{
         		try {
        			currentContext = this;
 					JSONObject savedObject = new JSONObject(savedInstanceState.getString("json object"));
 					DisplayPoliticianResults.showPoliticiansInMainView(savedObject, false);
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
         	}
         	else
         	{
     	       DisplayPoliticianResults.setViewingDisplay(false);
     	        
     	        this.setContentView(R.layout.main_screen);
     	        
     	        //Allows the context to be passed across classes.
     	        setCurrentContext(MainActivity.this);
     	        
     	        //Create interface elements from the XML files
     	        final EditText zipEntry = (EditText) findViewById(R.id.zipcodeentry);
     	        final Button searchForPolsButton = (Button) findViewById(R.id.dosearchnow);
     	        queryChoice = (Spinner) findViewById(R.id.spinner1);
     	        final Button queryButton = (Button) findViewById(R.id.partyquery);
     	        
     	        Log.i("Info","Created Main Menu elements based on XML files.");
     	        
     	        searchForPolsButton.setOnClickListener(new View.OnClickListener() {
     				
     				@Override
     				public void onClick(View v) {
     					//Build the onClick method, Handler, Intent, etc. See the method below!
     					buildClicker(zipEntry);
     					InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
     					imm.hideSoftInputFromWindow(searchForPolsButton.getWindowToken(), 0);
     					Log.i("Info","Hiding the keyboard");
     				}
     			});
     	        
     	        //Click this button to retrieve politicians saved to local storage.
     	        final Button retrieveSavedPols = (Button) findViewById(R.id.retrievefavorites);
     	        retrieveSavedPols.setOnClickListener(new View.OnClickListener() {
     				
     				@Override
     				public void onClick(View v) {
     					//String savedData; //This has been commented out because it is only used when accessing data directly.
     					Uri uri = PoliticianData.CONTENT_URI;
     					thisCursor = getContentResolver().query(uri, PoliticianData.PROJECTION, null, null, null);
     					turnCursorIntoDisplay(thisCursor);
     				}
     			});
     	        
     	        //Handle a spinner click
     	        queryButton.setOnClickListener(new View.OnClickListener() {
     				
     				@Override
     				public void onClick(View v) {
     				Log.i("Info","Query clicked");
     				if (queryChoice.getSelectedItem().toString().equals("Republicans"))
     				{
     					Uri uri = PoliticianData.REPUBLICAN_URI;
     					thisCursor = getContentResolver().query(uri, PoliticianData.PROJECTION, null, null, null);
     					turnCursorIntoDisplay(thisCursor);
     				} else if (queryChoice.getSelectedItem().toString().equals("Democrats"))
     				{
     					Uri uri = PoliticianData.DEMOCRAT_URI;
     					thisCursor = getContentResolver().query(uri, PoliticianData.PROJECTION, null, null, null);
     					turnCursorIntoDisplay(thisCursor);
     				} else
     				{
     					Toast toast = Toast.makeText(MainActivity.this, "There was an error making this query.", Toast.LENGTH_LONG);
     					toast.show();
     				}
     					
     				}
     			});
         	        
         	}
         }
         else
         {
  	       DisplayPoliticianResults.setViewingDisplay(false);
 	        
 	        this.setContentView(R.layout.main_screen);
 	        
 	        //Allows the context to be passed across classes.
 	        setCurrentContext(MainActivity.this);
 	        
 	        //Create interface elements from the XML files
 	        final EditText zipEntry = (EditText) findViewById(R.id.zipcodeentry);
 	        final Button searchForPolsButton = (Button) findViewById(R.id.dosearchnow);
 	        queryChoice = (Spinner) findViewById(R.id.spinner1);
 	        final Button queryButton = (Button) findViewById(R.id.partyquery);
 	        
 	        Log.i("Info","Created Main Menu elements based on XML files.");
 	        
 	        searchForPolsButton.setOnClickListener(new View.OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					//Build the onClick method, Handler, Intent, etc. See the method below!
 					buildClicker(zipEntry);
 					InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
 					imm.hideSoftInputFromWindow(searchForPolsButton.getWindowToken(), 0);
 					Log.i("Info","Hiding the keyboard");
 				}
 			});
 	        
 	        //Click this button to retrieve politicians saved to local storage.
 	        final Button retrieveSavedPols = (Button) findViewById(R.id.retrievefavorites);
 	        retrieveSavedPols.setOnClickListener(new View.OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					//String savedData; //This has been commented out because it is only used when accessing data directly.
 					Uri uri = PoliticianData.CONTENT_URI;
 					thisCursor = getContentResolver().query(uri, PoliticianData.PROJECTION, null, null, null);
 					turnCursorIntoDisplay(thisCursor);
 				}
 			});
 	        
 	        //Handle a spinner click
 	        queryButton.setOnClickListener(new View.OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 				Log.i("Info","Query clicked");
 				if (queryChoice.getSelectedItem().toString().equals("Republicans"))
 				{
 					Uri uri = PoliticianData.REPUBLICAN_URI;
 					thisCursor = getContentResolver().query(uri, PoliticianData.PROJECTION, null, null, null);
 					turnCursorIntoDisplay(thisCursor);
 				} else if (queryChoice.getSelectedItem().toString().equals("Democrats"))
 				{
 					Uri uri = PoliticianData.DEMOCRAT_URI;
 					thisCursor = getContentResolver().query(uri, PoliticianData.PROJECTION, null, null, null);
 					turnCursorIntoDisplay(thisCursor);
 				} else
 				{
 					Toast toast = Toast.makeText(MainActivity.this, "There was an error making this query.", Toast.LENGTH_LONG);
 					toast.show();
 				}
 					
 				}
 			});
         }
     }
 	
 	public static void setCurrentContext (Context context)
 	{
 		currentContext = context;
 	}
 	
 	public static Context getCurrentContext ()
 	{
 		return currentContext;
 	}
 
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 	
 	//Because there is so much going on here - and because we need to recreate all of this when the Back button is pressed.
 	
 	public static void buildClicker(EditText zipEntry)
 	{
 		Log.i("Info","Attached onClick method to Zip Code entry button.");
 		Boolean connected = Connection_Verification.areWeConnected(getCurrentContext());
 		if (connected)
 		{
 			Log.i("Info","Connection established.");
 			String enteredZip = zipEntry.getText().toString();
 			Handler retrievalHandler = new Handler()
 			{
 				@Override
 				public void handleMessage(Message msg) 
 				{
 					if (msg.arg1 == RESULT_OK)
 					{
 						try {
 							jsonResponse = (JSONObject) msg.obj;
 						} catch (Exception e) {
 							Log.e("Error","There was a problem retrieving the json Response.");
 						}
 						if (jsonResponse == null)
 						{
 							Toast toast = Toast.makeText(MainActivity.getCurrentContext(), "You have entered an invalid zip code. Please try again.", Toast.LENGTH_LONG);
 							toast.show();
 						} else
 						{
 							DisplayPoliticianResults.showPoliticiansInMainView(jsonResponse, false);
 						}
 					}
 				}
 			};
 			Log.i("Info","Handler created.");
 			
 			Messenger apiMessenger = new Messenger(retrievalHandler);
 			
 			Intent startDataService = new Intent(getCurrentContext(), DataRetrievalService.class);
 			Log.i("Info","Intent to start data service started.");
 			startDataService.putExtra(DataRetrievalService.MESSENGER_KEY, apiMessenger);
 			startDataService.putExtra(DataRetrievalService.ZIP_KEY,enteredZip);
 			Log.i("Info","Put Messenger and Zip Code into Data Service intent.");
 			getCurrentContext().startService(startDataService);
 		
 		} else
 		{
 			Log.i("Info","No internet connection found.");
 			Toast toast = Toast.makeText(getCurrentContext(), "There is no connection to the internet available. Please try again later, or view saved politicians.", Toast.LENGTH_LONG);
 			toast.show();
 		}
 	}
 	
 	public static void turnCursorIntoDisplay(Cursor dataCursor)
 	{
 		JSONObject masterObject = new JSONObject();
 		JSONArray arrayOfPols = new JSONArray();
 		JSONObject thisObject = null;
 		
 		if (dataCursor != null) 
 		{
 			try 
 			{
 				if (dataCursor.moveToFirst())
 				{
 					   while(!dataCursor.isAfterLast())
 					   {
 						  thisObject = new JSONObject(dataCursor.getString(dataCursor.getColumnIndex("polObject")));
 					      arrayOfPols.put(thisObject);
 					      dataCursor.moveToNext();
 					   }
 						masterObject.put("Politicians", arrayOfPols);
 				} else
 				{
 					if (SavedPoliticianProvider.JSONString != null)
 					{
 						Toast toast = Toast.makeText(MainActivity.getCurrentContext(), "There are no saved " + queryChoice.getSelectedItem().toString() + " to view.", Toast.LENGTH_LONG);
 						toast.show();
 					}
 				}
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 		}
 		try {
 			DisplayPoliticianResults.showPoliticiansInMainView(masterObject, true);
 			dataCursor.close();
 		}  catch (Exception e) {
 			Toast toast = Toast.makeText(getCurrentContext(), "There are no politicians saved to storage.", Toast.LENGTH_LONG);
 			toast.show();
 		}
 	}
     
 }
