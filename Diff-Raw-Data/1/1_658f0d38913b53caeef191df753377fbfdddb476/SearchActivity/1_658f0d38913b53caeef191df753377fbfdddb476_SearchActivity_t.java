 package com.example.test2app;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 import android.os.Bundle;
 import android.os.StrictMode;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 
 public class SearchActivity extends ListActivity {
 	EditText searchBox;
 	//bundle is global so that it may be accessed from outside the getData() method
 	Bundle bundle;
 	ListAdapter listAdapter;
 	ListView searchResults;
 	Cursor cursor;
 	SQLiteDatabase buildingDb;
 	SQLiteDatabase departmentDb;
 	SQLiteDatabase personDb;
 	boolean addedDatabase;
 	int searchChooser; //-1=person; 0=building; 1=department
 	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_search);
 		
 		BuildingDatabase buildingDatabase = new BuildingDatabase(this);
 		DepartmentDatabase departmentDatabase = new DepartmentDatabase(this);
 		
 		BufferedReader br = null;
 		String line = "";
 		InputStream buildingIS = null;
 		InputStream departmentIS = null;
 		
 		SharedPreferences settings = getSharedPreferences("ZotFinder Preferences", 0);
 		addedDatabase = settings.getBoolean("addedDatabase", false);
 		searchChooser = settings.getInt("searchType", 0);
 		if (!addedDatabase)
 		{
 			buildingIS = getResources().openRawResource(R.raw.building_file);
 			try {
 				br = new BufferedReader(new InputStreamReader(buildingIS));
 				while ((line = br.readLine()) != null)
 				{
 					String[] buildingInfo = line.split(",");
 					buildingDatabase.addToDatabase(buildingInfo);
 				}
 				buildingIS.close();
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			
 			departmentIS = getResources().openRawResource(R.raw.departments_file);
 			try {
 				br = new BufferedReader(new InputStreamReader(departmentIS));
 				while ((line = br.readLine()) != null)
 				{
 					String[] departmentInfo = line.split(",");
 					departmentDatabase.addToDatabase(departmentInfo);
 				}
 				departmentIS.close();
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			addedDatabase = true;
 		}
 
 		buildingDb = buildingDatabase.getReadableDatabase();
 		departmentDb = departmentDatabase.getReadableDatabase();
 		personDb = (new PersonDatabase(this)).getReadableDatabase();
 		searchBox = (EditText) findViewById(R.id.searchText);
  		
 	}
 	
 	//makes HTTP request and creates new intent with parsed output 
 	public void getData(String inputValue) {
 	    try {
 	    	//creating an http connection to make an Http Request
 	        StrictMode.ThreadPolicy policy = new StrictMode.
 	          ThreadPolicy.Builder().permitAll().build();
 	        StrictMode.setThreadPolicy(policy); 
 	        URL url = new URL("http://directory.uci.edu/index.php?uid=" + inputValue + "&form_type=plaintext");
 	        HttpURLConnection con = (HttpURLConnection) url
 	          .openConnection();
 	        
 	        //bundle is used as a map (data structure) to pass to new activity
 	        bundle = new Bundle();
 	        readStream(con.getInputStream(), bundle);
 	        
 	        //Setup the Intent that will start the next Activity
 	        Intent personInfoActivity = new Intent(this, PersonInfoActivity.class); 
 	        
 	        //Assumes this references this instance of Activity A
 	        //puts the bundle into the intent:"personInfoActivity"
 	        personInfoActivity.putExtras(bundle);
 	        startActivity(personInfoActivity);
 	   
 	    } catch (Exception e) {
 	        e.printStackTrace();
 	    }
 	}     
 	
 	//parses the output from the HttpRequest
 	//method is void since it access the global variable bundle
 	//may have to change this
 	private void readStream(InputStream in, Bundle readBundle) {
 	  BufferedReader reader = null;
 	  //initialize an output string
 	  String output = "";
 	  //initializes individual tag strings
 	  String ucinetid, name, title, department, address, phone = "0", fax = "0", email;
 	  try {
 		    reader = new BufferedReader(new InputStreamReader(in));
 		    String line = "";
 		    while ((line = reader.readLine()) != null) {
 		      output+=line;
 	    }
 	    
 	    //sets the tag strings as the correct data from the output
 	    //the following must be done in this order
 	    ucinetid = output.split("UCInetID: ")[1].split("<br/>")[0];
 	    readBundle.putString("ucinetid", ucinetid);
 	    name = output.split("Name: ")[1].split("<br/>")[0];
 	    readBundle.putString("name", name);
 	    title = output.split("Title: ")[1].split("<br/>")[0];
 	    readBundle.putString("title", title);
 	    department = output.split("Department: ")[1].split("<br/>")[0];
 	    readBundle.putString("department", department);
 	    address = output.split("Address: ")[1].split("<br/>")[0];
 	    readBundle.putString("address", address);
 	    
 	    //since some faculty/staff don't have phone numbers or fax number, the method will only parse the number if it exists
 	    //if the output does not contain a number, the tag string is written as "N/A"
 	    if (output.contains("Phone: ")){
 	    	phone = output.split("Phone: ")[1].split("<br/>")[0];
 	    	readBundle.putString("phone", phone);
 	    }
 	    else
 	    	readBundle.putString("phone", "N/A");
 	    if (output.contains("Fax: ")){
 	    	fax = output.split("Fax: ")[1].split("<br/>")[0];
 	    	readBundle.putString("fax", fax);
 	    }
 	    else
 	    	readBundle.putString("fax", "N/A");
 	   	email = ucinetid + "@uci.edu";
 	   	readBundle.putString("email", email);
 	   	
 	  } catch (IOException e) {
 	    e.printStackTrace();
 	  } finally {
 	    if (reader != null) {
 	      try {
 	        reader.close();
 	      } catch (IOException e) {
 	        e.printStackTrace();
 	      }
 	    }
 	  }
 	} 
 	
 	public void choosePersonSearch(View view){
 		searchChooser = -1;
 	}
 	public void chooseBuildingSearch(View view){
 		searchChooser = 0;
 	}
 	public void chooseDepartmentSearch(View view){
 		searchChooser = 1;
 	}
 	
 
 	@SuppressWarnings("deprecation")
 	public void search(View view){
 		if (searchChooser == 0){
 			cursor = buildingDb.rawQuery("SELECT _id, buildingName, buildingNumber, buildingAddress FROM building WHERE buildingName || ' ' || buildingNumber LIKE ?", 
 					new String[]{"%" + searchBox.getText().toString() + "%"});
 			listAdapter = new SimpleCursorAdapter(
 					this, 
 					R.layout.activity_building_list_item, 
 					cursor, 
 					new String[] {"buildingName", "buildingNumber", "buildingAddress"}, 
 					new int[] {R.id.buildingName,R.id.buildingNumber, R.id.buildingAddress});
 			setListAdapter(listAdapter);
 		}	else if (searchChooser == 1){
 			cursor = departmentDb.rawQuery("SELECT _id, departmentName, departmentAddress FROM department WHERE departmentName || ' ' || departmentAddress LIKE ?", 
 					new String[]{"%" + searchBox.getText().toString() + "%"});
 			listAdapter = new SimpleCursorAdapter(
 					this, 
 					R.layout.activity_department_list_item, 
 					cursor, 
 					new String[] {"departmentName", "departmentAddress"}, 
 					new int[] {R.id.departmentName, R.id.departmentAddress});
 			setListAdapter(listAdapter);
 		
 		} else if (searchChooser == -1){
 			cursor = personDb.rawQuery("SELECT _id, personName, personAddress FROM person WHERE personName || ' ' || personAddress LIKE ?", 
 					new String[]{"%" + searchBox.getText().toString() + "%"});
 			listAdapter = new SimpleCursorAdapter(
 					this, 
 					R.layout.activity_person_list_item, 
 					cursor, 
 					new String[] {"personName", "personAddress"}, 
 					new int[] {R.id.personName, R.id.personAddress});
 			setListAdapter(listAdapter);
 		}
 	}
 	
     public void onListItemClick(ListView parent, View view, int position, long id) {
     	Bundle bundle = new Bundle();
     	if (searchChooser == 0)
     	{
 	    	Intent intent = new Intent(this, BuildingInfoActivity.class);
 	    	Cursor cursor = (Cursor) listAdapter.getItem(position);
 	    	bundle.putInt("BUILDING_ID", cursor.getInt(cursor.getColumnIndex("_id")));
 	    	intent.putExtras(bundle);
 	    	startActivity(intent);
     	}
     	else if (searchChooser == 1)
     	{
 	    	Intent intent = new Intent(this, DepartmentInfoActivity.class);
 	    	Cursor cursor = (Cursor) listAdapter.getItem(position);
 	    	bundle.putInt("DEPARTMENT_ID", cursor.getInt(cursor.getColumnIndex("_id")));
 	    	intent.putExtras(bundle);
 	    	startActivity(intent);
     	}
     	else if (searchChooser == -1)
     	{
 	    	Intent intent = new Intent(this, PersonInfoActivity.class);
 	    	Cursor cursor = (Cursor) listAdapter.getItem(position);
 	    	bundle.putInt("PERSON_ID", cursor.getInt(cursor.getColumnIndex("_id")));
 	    	intent.putExtras(bundle);
 	    	startActivity(intent);
     	}
     }
 	
   //Back button
   	public void finishActivity(View v){
   	    finish();
   	    }
     
 	//Footer Methods
 	
 	//method to go to activity: MainActivity
 	//creates intent used to store the information of a different activity within this activity
 	//startActivity(intent) changes the current activity to the intent activity
 	public void goToMap(View view) { 
 		Intent intent = new Intent(this,MainActivity.class);
 		startActivity(intent);
 	}
 	 
 	//method to go to activity: EmergencyInfoActivity
 	//creates intent used to store the information of a different activity within this activity
 	//startActivity(intent) changes the current activity to the intent activity
 	public void goToEmergencyInfo(View view) { 
 		Intent intent = new Intent(this,EmergencyInfoActivity.class);
 		startActivity(intent);
 	}
 
 	//method to go to activity: DialerActivity
 	//creates intent used to store the information of a different activity within this activity
 	//startActivity(intent) changes the current
 	public void goToEmergencyDialer(View view) { 
 		Intent intent = new Intent(this,DialerActivity.class);
 		startActivity(intent);
 	}
 	 
 	//menu functionality when the user press the physical menu button located on the phone
 	//currently the menu feature does nothing
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.search, menu);
 		return true;
 	}
 	
 	@Override
 	protected void onStop() {
 		SharedPreferences settings = getSharedPreferences("ZotFinder Preferences", 0);
 		SharedPreferences.Editor editor = settings.edit();
 	    editor.putBoolean("addedDatabase", addedDatabase);
 	    editor.putInt("searchType", searchChooser);
 	    editor.commit();
 		super.onStop();
 	}
 	
 	@Override
 	protected void onDestroy() {
 		SharedPreferences settings = getSharedPreferences("ZotFinder Preferences", 0);
 		SharedPreferences.Editor editor = settings.edit();
 	    editor.putBoolean("addedDatabase", addedDatabase);
 	    editor.putInt("searchType", searchChooser);
 	    editor.commit();
 		super.onDestroy();
 	}
 	
 	@Override
 	protected void onPause() {
 		SharedPreferences settings = getSharedPreferences("ZotFinder Preferences", 0);
 		SharedPreferences.Editor editor = settings.edit();
 	    editor.putBoolean("addedDatabase", addedDatabase);
 	    editor.putInt("searchType", searchChooser);
 	    editor.commit();
 		super.onPause();
 	}
 }
