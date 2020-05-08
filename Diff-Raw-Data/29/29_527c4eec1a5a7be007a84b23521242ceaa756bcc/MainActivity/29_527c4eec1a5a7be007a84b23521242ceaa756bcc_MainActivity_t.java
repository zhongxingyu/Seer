 package com.jamiewannenburg.loghours;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Spinner;
 import android.widget.Toast;
 import com.jamiewannenburg.loghours.R;
 
 public class MainActivity extends Activity {
 	public final static String FILE = "com.jamiewannenburg.loghours.MESSAGE";
 	public final static String ROW_ID = "com.jamiewannenburg.loghours.MESSAGE";
 
 	// spinner element
 	Spinner spinner;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		// first make the view
 		setContentView(R.layout.activity_main);
 		
 		spinner = (Spinner) findViewById(R.id.employers_spinner);
 		// Spinner click listener
 		// spinner.setOnItemSelectedListener(this);
 		loadSpinner(this);
 				
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 	
 	private void loadSpinner(Context c) {
 		// Get previous value and position from shared_preferences
 		SharedPreferences sharedPref = getBaseContext().getSharedPreferences(
 				getString(R.string.preference_file_key),Context.MODE_PRIVATE);
 		String previous_employer = sharedPref.getString(getString(R.string.empoyer_selector_value), "");
 		
 		// Load previous month
 		String last_month = sharedPref.getString(getString(R.string.last_month), "");
 		String this_month = new SimpleDateFormat("MM", Locale.US).format(new Date());
 		if (!last_month.equals(this_month)){
 			Toast.makeText(getBaseContext(), "The month has changed, make sure all data is sent.", Toast.LENGTH_LONG).show();
 		}
 		
 		// Save current month
 		SharedPreferences.Editor editor = sharedPref.edit();
     	editor.putString(getString(R.string.last_month),this_month);
     	editor.commit();
 		
     	// get employers from database
     	DbFunctions entry = new DbFunctions(c);//AddEmployerActivity.
     	entry.open();
     	List<String> employers;
     	employers = entry.getAllEmployers();
     	entry.close();
 		
 		int count = 0;
     	int position = 0;
     	
     	for (String employer : employers)
     	{
     		// looping through all rows; check if previous selector is still there
     		if (employer.equals(previous_employer)) {
         		position = count;
         	}
         	count=count+1;
     	}
     	
 		// Create an ArrayAdapter using the string array and a default spinner layout
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, employers);
 		// Specify the layout to use when the list of choices appears
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		// Apply the adapter to the spinner
 		spinner.setAdapter(adapter);
 		
 		// set position
 		spinner.setSelection(position);
 
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		
 		switch (item.getItemId())
 		{
 		case R.id.menu_add_employer:
 			// Single menu item is selected do something
             // Ex: launching new activity/screen or show alert message
 			Intent intent = new Intent(this, AddEmployerActivity.class);
 	    	startActivity(intent);
 	    	return true;
 	    	
 	    default:
 	    	return super.onOptionsItemSelected(item);	
 		}
 	}
 	
 	/** Called when the user clicks the Start button */
     public void startHourLogger(View view) {
     	// Get employer and times
     	String dateTimeStamp = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss", Locale.US).format(new Date());
     	
     	String employer = String.valueOf(spinner.getSelectedItem());
     	
     	// save option
     	SharedPreferences sharedPref = getBaseContext().getSharedPreferences(getString(R.string.preference_file_key),Context.MODE_PRIVATE);
     	SharedPreferences.Editor editor = sharedPref.edit();
     	editor.putString(getString(R.string.empoyer_selector_value), employer);
     	editor.commit();
     	
     	if (!employer.equals("null"))
     	{
 	    	// put entry in database
 	    	DbFunctions entry = new DbFunctions(this);
 	    	entry.open();
 	    	long newRowId = entry.insertStartTime(dateTimeStamp, employer);
 	    	
 	    	if (newRowId == -1)
 	    	{
 	    		Toast.makeText(this, "Could not connect to database", Toast.LENGTH_LONG).show();
 	    	}
 	    	else
 	    	{
 	    		// start stop logger activity
 	        	Intent intent = new Intent(this, StopLoggerActivity.class);
 	        	//intent.putExtra(EMPLOYER, employer);
 	        	intent.putExtra(ROW_ID, String.valueOf(newRowId));
 	        	startActivity(intent);
 	        	entry.close();
 	    	}
     	}
     	else
     	{
     		Toast.makeText(this, "Please add an employer", Toast.LENGTH_LONG).show();
     	}
     }
     
     
     /** Called when the user clicks the Upload to Drive button */
     public void uploadToDrive(View view) {
     	// get month and employer
     	String yearMonth = new SimpleDateFormat("yyyy MMMM", Locale.US).format(new Date());
     	String thisMonth = new SimpleDateFormat("yyyy-MM", Locale.US).format(new Date());
     	
     	String employer = String.valueOf(spinner.getSelectedItem());
     	
     	if (!employer.equals("null"))
     	{
 	    	// get data from database
 	    	DbFunctions entry = new DbFunctions(this);
 	    	entry.open();
 	    	String file_content = entry.getEmployerLogs(employer, thisMonth);
 	    	entry.close();
 	    	
 	    	if (!file_content.equals(""))
 	    	{
 	    		// write to csv file
	        	File file = generateCsvFile("Hours " + employer + " "+ yearMonth +".csv",file_content,this);
 	        	Intent intent = new Intent(this, DriveActivity.class);
 	        	intent.putExtra(FILE, String.valueOf(file));
 	        	startActivity(intent);
 	    	}
     	}
     	else
     	{
     		Toast.makeText(this, "Please add an employer", Toast.LENGTH_LONG).show();
     	}
     	    	
     }
     
    private File generateCsvFile(String sFileName, String file_content, Context c)
     {
     	
     	try
     	{
 	    	File root = getExternalFilesDir(null);
 	        File gpxfile = new File(root, sFileName);
	        FileWriter writer = new FileWriter(gpxfile, false);
 	    	writer.write(file_content);
	    	//writer.flush();
             writer.close();
             return gpxfile;
     	}
         catch(IOException e)
         {
        	Toast.makeText(c, "Could not find a folder to write in", Toast.LENGTH_LONG).show();
             e.printStackTrace();
             return null;
         }
      }
 
 }
