 /**
  * @author Kolby Cansler <golfguy90@gmail.com>
  * @version 1.0.BETA_02
  * 
  * Implements the Main layout class and all 
  * methods and views associated with it.
  */
 
 package com.ubundude.timesheet;
 
 import com.bugsense.trace.BugSenseHandler;
 
 //import java.io.IOException;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Locale;
 //import java.util.concurrent.ExecutionException;
 
 import android.os.Bundle;
 import android.app.Activity;
 //import android.app.AlertDialog;
 //import android.content.DialogInterface;
 import android.content.Intent;
 //import android.content.pm.PackageInfo;
 //import android.content.pm.PackageManager.NameNotFoundException;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.EditText;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /*
  * Hours textview should be updated with the total hours worked for a day
  */
 
 /**
  * Main Activity Class
  * 
  * Implements the main page layout and logic for the elements of the page
  */
 public class MainActivity extends Activity {
 	/** Keys for the HashMap used in the list view */
 	public static final String KEY_ID = "listviewId";
 	public static final String KEY_SHORT = "projectShortTextView";
 	public static final String KEY_FULL = "projectFullTextView";
 	public static final String KEY_HOURS = "listviewHoursTV";
 	public static final String KEY_PROID = "projectIdTV";
 	/** Database instance and call to Timesheet OpenHelper */
 	private SQLiteDatabase db;
 	private TimesheetDatabaseHelper dbHelp = new TimesheetDatabaseHelper(this);
 	/** Gets a valid calendar instance for use */
 	final Calendar c = Calendar.getInstance();
 	/** Strings for formatting the date's for use */
 	public String dateViewForm = "EEE MM/dd/yyyy";
 	public String dateForm = "MM/dd/yyyy";
 	public String timeForm = "HH:mm";
 	/** Strings to store formated calendar outputs */
 	public String date, dateView;
 	/** Prepares buttons and EditText for use */
 	public Button minusButton, plusButton, quickAdd;
 	public EditText dateEditText;
 	public ListView list;
 	/** Gets the custom adapter for the listview */
 	TimestampAdapter adapter;
 	/** Formatters for the dates */
 	SimpleDateFormat formDateView = new SimpleDateFormat(dateViewForm, Locale.US);
     SimpleDateFormat formDate = new SimpleDateFormat(dateForm, Locale.US);
     SimpleDateFormat formTime = new SimpleDateFormat(timeForm, Locale.US);
 	
     /** The method for creating the Main Activity */
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         /** Enables bugsense error reporting on the app */
         BugSenseHandler.initAndStartSession(MainActivity.this, "8b04fe90");
         setContentView(R.layout.activity_main);
       
         /** Calls temporary method for checking updates
          * 
          *  Should be removed for acutal production app*/
         /*
         try {
 			updateCheck();
 		} catch (NameNotFoundException e1) {
 			e1.printStackTrace();
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			e.printStackTrace();
 		}
 		*/
 		
         /** Method to get todays date and display it in the proper places */
         date = initialDates();
         
         /** Call to get the timestamps for the currently selected date */
         getDailyTimestamps(date);
         
         /**
          * Initialize minus button
          * 
          * This method calls the minus button handler and stores the date returned
          * and also gets new timestamps for the date returned. */
         minusButton = (Button)findViewById(R.id.minusButton);
         minusButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				try {
 					date = minusButtonHandler();
 					getDailyTimestamps(date);
 				} catch (ParseException e) {
 					e.printStackTrace();
 				}
 			}
 		});
         
         /**
          * Initialize plus button 
          * 
          * This method calls the plus button handler and stores the date returned
          * and also gets new timestamps for the date returned. */
         plusButton = (Button)findViewById(R.id.plusButton);
         plusButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				try {
 					date = plusButtonHandler();
 					getDailyTimestamps(date);
 				} catch (ParseException e) {
 					e.printStackTrace();
 				}
 			}
 		});
         
        /** 
         * Initilize the quick add button
         * 
         * This method calls the quick add handler and then 
         * reloads timestamps for the current date.
         */
         quickAdd = (Button)findViewById(R.id.quickAddButton);
         quickAdd.setOnClickListener(new View.OnClickListener() {
         	public void onClick(View v) {
         		quickAddHandler(v);
         		getDailyTimestamps(date);
         	}
         });
     }
     
     /** 
      * Checks for an updated version of the app
      * 
      * If there is an updated version, an Alert dialog is displayed 
      * to advise the user to download the latest version
      * 
      * @throws NameNotFoundException
      * @throws IOException
      * @throws InterruptedException
      * @throws ExecutionException
      */
     
     //@SuppressWarnings("deprecation")
 	//private void updateCheck() throws NameNotFoundException, IOException, InterruptedException, ExecutionException { 
   	/** String to store the version from the url */
     //String urlVersion;
     /** Get instance of UpdateCheck.java and get the version returned from it */
     /**	
     UpdateCheck check = new UpdateCheck();
     urlVersion = check.execute().get();
     	
     	PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
 		String packageVersion = pInfo.versionName;
 		
 		Log.d("Package Version", packageVersion);
 		
 		if (!urlVersion.equals(packageVersion)) {
 			AlertDialog alert = new AlertDialog.Builder(this).create();
 			alert.setTitle("Version Check");
 			alert.setMessage("You're version is out of date. Please visit " 
 					+ "www.ubundude.com/p/beta.html to update to the latest version.");
 			
 			alert.setButton("OK", new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int which) {
                 }
         });
 			alert.show();
 		}
 	}
 */
 
 
     /** 
      * Method to resume the Main Activity
      * 
      * Gets new timestamp created in the editor if exists.
      */
 	@Override 
     protected void onResume() {
     	super.onResume();
     	getDailyTimestamps(date);
     }
     
 	/**
 	 *  Method to get timestamps from database
 	 * 
 	 * Method runs an SQL Query to get all timestamps for the given 
 	 * date and displays them in a list view
 	 * 
 	 * @param date The date the user has selected
 	 */
     private void getDailyTimestamps(String date) {
     	final ArrayList<HashMap<String, String>> stampList = new ArrayList<HashMap<String, String>>();
     	
     	/** Open the database table for reading and writing */
         db = dbHelp.getReadableDatabase();
         
         /** Select statement to get data needed for the list view */
         String getTimestamps = "select ti._id, pr.name, pr.shortcode, ti.hours, ti.project "
         		+ "from timestamp ti inner join projects pr "
         		+ "where ti.project = pr._id and ti.date_in = '" + date + "'";
     	
         /** Open a cursor and store the return of the query */
         Cursor cu = db.rawQuery(getTimestamps, null);
         
         /** Make sure cursor is not null */
         if(cu != null && cu.getCount() > 0){
 			cu.moveToFirst();
 			
 			/**
 			 * Put values for each cursor row into HashMap
 			 * 
 			 *  While the cursor has a row, get the data
 			 *  and store it in a HashMap that is passed on the adapter.
 			 */
 			do {
 				HashMap<String, String> map = new HashMap<String, String>();
 				map.put(KEY_ID, Integer.toString(cu.getInt(0)));
 				map.put(KEY_SHORT, cu.getString(1));
 				map.put(KEY_FULL, cu.getString(2));
 				map.put(KEY_HOURS, cu.getString(3));
 				map.put(KEY_PROID, Integer.toString(cu.getInt(4)));
 				
 				stampList.add(map);
 				
 			} while(cu.moveToNext());
         }
 		
         /** Close the cursor and database */
 		cu.close();
 		db.close();
         
 		/** Initialize the listview */
         list = (ListView)findViewById(R.id.timestampListView);
         
         /** Initialize the adapter with the stamplist for */
         adapter = new TimestampAdapter(this, stampList);
         list.setAdapter(adapter);
         
         /** Method to handle clicked list view items */
         list.setOnItemClickListener(new OnItemClickListener() {
         	 
             @Override
             public void onItemClick(AdapterView<?> parent, View view,
                     int position, long id) {
             	/** Get HashMap for current position */
             	HashMap<String, String> test = new HashMap<String, String>();
             	test = stampList.get(position);
             	/** Store projectId and timestampId from the current HashMap */
             	int proId = Integer.parseInt(test.get(KEY_PROID));
             	int timeId = Integer.parseInt(test.get(KEY_ID));
             	
             	/** Intent to move to TimestampEditorActivity and pass values to load */
             	Intent intent = new Intent(MainActivity.this, TimestampEditorActivity.class);
             	intent.putExtra("TIMESTAMP_ID", timeId);
             	intent.putExtra("PROJECT_ID", proId);
             	startActivity(intent);
             }
         });
 	}
 
     /** 
      * Method to get the current date form
      * 
      * Displays the formatted dates in the proper places 
      * and makes them availible for usage elsewhere.
      * 
      * @return date The current date formatted for SQL queries.
      */
 	private String initialDates() {
         dateView = formDateView.format(c.getTime());
     	date = formDate.format(c.getTime());
         
     	/** Sets the text in the dateEditText to the current date */
         dateEditText = (EditText)findViewById(R.id.dateEditText);
         dateEditText.setText(dateView, TextView.BufferType.NORMAL);
 
 		return date;
 	}
 
 	/** Gets the next day and displays to dateEditText 
 	 * @throws ParseException 
 	 * @return date The current date formatted for SQL queries
 	 */
     private String plusButtonHandler() throws ParseException {
     	
     	c.setTime(formDateView.parse(dateView));
     	c.add(Calendar.DAY_OF_MONTH, 1);
     	
     	dateView = formDateView.format(c.getTime());
     	date = formDate.format(c.getTime());
     	
     	dateEditText = (EditText)findViewById(R.id.dateEditText);
         dateEditText.setText(dateView, TextView.BufferType.NORMAL);
 
     	return date;
 	}
 
     /** Gets the previous day and displays to dateEditText 
      * @throws ParseException
      * @return date The current date formatted for SQL queries
      */
 	private String minusButtonHandler() throws ParseException {
 		c.setTime(formDateView.parse(dateView));
     	c.add(Calendar.DAY_OF_MONTH, -1);
     	
     	dateView = formDateView.format(c.getTime());
     	date = formDate.format(c.getTime());
     	
     	dateEditText = (EditText)findViewById(R.id.dateEditText);
         dateEditText.setText(dateView, TextView.BufferType.NORMAL);
         
 		return date;
 	}
 
 	/**
 	 * Intent to move to TimestampEditorActivity
 	 * 
 	 *  @param view Gets the current view context to pass with the intent
 	 */
 	public void addNewHandler(View view) {
     	Intent intent = new Intent(this, TimestampEditorActivity.class);
     	startActivity(intent);
     }
     
 	/**
 	 * Get current date and time and place them into Timestamp table as generic entry
 	 * 
 	 * @param view
 	 * @throws SQLException
 	 */
    public void quickAddHandler(View view) throws SQLException {
 	   /** Strings and int for the current dates and project */
 	   String timeIn, timeOut, dateIn, dateOut;
 	   int project = 1; //Will get from Default project in settings
 	   timeIn = formTime.format(c.getTime());
 	   timeOut = timeIn;
 	   dateIn = formDate.format(c.getTime());
 	   dateOut = dateIn;
 	   
 	   /** Open Database for writing */
 	   db = dbHelp.getWritableDatabase();
 	   
 	   /** String to insert a timestamp into the database */
 	   String insertSQL = "insert into timestamp (date_in, time_in, date_out, time_out, hours, project) " +
 			   "values('" + dateIn + "', '" + timeIn + "', '" + dateOut +
 			   "', '" + timeOut + "', 0, '" + project + "')";
 	   try {
 		   db.execSQL(insertSQL);
 	   } catch(Exception e) {
 		   Log.d("save Fail", e.getLocalizedMessage(), e.fillInStackTrace());
 	   }
 	   
 	   /** Close the Database */
 	   db.close();
     }
     
     
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 	
 	@Override 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
		case R.id.menu_defaults:
 			Toast.makeText(this, "Defaults button clicked", Toast.LENGTH_SHORT).show();
 			return(true);
 		}
 		return(super.onOptionsItemSelected(item));
 	}
 	 
 }
