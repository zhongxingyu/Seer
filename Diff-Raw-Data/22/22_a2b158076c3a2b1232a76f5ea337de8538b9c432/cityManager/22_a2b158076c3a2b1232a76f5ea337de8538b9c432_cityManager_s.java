 package com.example.PrayerTimes;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Vector;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.DialogInterface.OnCancelListener;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 
 public final class cityManager extends Activity implements OnItemClickListener, OnItemLongClickListener, OnCancelListener {
 
 
 	private ListView cityList;
 	private ArrayAdapter<String> cities;
 	String operationType;
 	Profile newProfile = new Profile();
 	DatabaseHandler db = new DatabaseHandler(this);
 	Dialog dialog;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.city_listview);
 		
 		// Receive the data passed from MainActivity
 		Bundle params = getIntent().getExtras();
 		operationType = params.getString("operationType");
 		
 		// Set the dialog settings
 		if(operationType.equals("save"))
 			this.setTitle("Choose a city to save to:");
 		else
 			this.setTitle("Choose a city to load from:");		
 		
 		// Construct the cities list
 		Vector<String> items = new Vector<String>();
 		if(operationType.equals("save"))
 			items.add("<New City>");
 		else
 			items.add("<Delete City>");
 		
 		List<Profile> allCities = db.getAllProfiles();
 		for(int i=0;i<allCities.size();i++){
 			items.add((allCities.get(i)).cityName);
 		}
 
 		cityList = (ListView) findViewById(R.id.list);
 		cityList.setOnItemClickListener(this);
 		cityList.setOnItemLongClickListener(this);
         cities = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items.toArray(new String[0]));
         cityList.setAdapter(cities);
 
        // If the state is save, then get the mainProfile from MainActivity
		if(operationType.equals("save"))
			newProfile = (Profile)params.getParcelableArrayList("profile").get(0);
 
         }
 
 
 	@Override
 	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 		//Get the text of the current list item
 		String text = (((TextView)(view)).getText()).toString();
 		// Clicked on <New City>
 		if(text.equals("<New City>")){
 			newCity();
 		}
 		// Clicked on <Delete City>
 		if(text.equals("<Delete City>")){
 			Toast.makeText(getApplicationContext(),"To delete a city press and hold on its name.",Toast.LENGTH_LONG).show();
 		}
 		// If clicked on a city name when in load
 		if(operationType.equals("load") && !text.equals("<Delete City")){
 			// Uppdate the profile from database
 			newProfile = db.getProfile(text);
 
 			// Send it back to MainActivity
 			sendProfileBack(newProfile);
 		}
 		// If clicked on a city name when in save
 		if(operationType.equals("save") && !text.equals("<New City>")){
 			//Update that database entry
 			db.updateProfile(newProfile);
 		}
 		// Clicked on anything except <Delete City> or <New City>
 		if(!text.equals("<Delete City>") && !text.equals("<New City>"))
 			finish();
 	}
 
 
 	@Override
 	public void onCancel(DialogInterface dialog) {
 		this.finish();
 	}
 
 
 	@Override
 	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
 		String text = (((TextView)(view)).getText()).toString();
 		if(operationType.equals("load") && !(text.equals("<Delete City>"))){
			db.deleteProfile(""+(((TextView)view).getText()));
			finish();
 		}
 
 		return true;
 	}
 
 	void newCity(){
 		dialog = new Dialog(cityManager.this);
 		dialog.setContentView(R.layout.city_name);
 		dialog.setTitle("City name:");
 		dialog.setCancelable(true);
 
 		//set up button (Cancel)
 		Button button = (Button) dialog.findViewById(R.id.Button01);
 		button.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				dialog.dismiss();
 			}
 		});
 
 		//set up button (OK)
 		Button button2 = (Button) dialog.findViewById(R.id.Button02);
 		button2.setOnClickListener(okListener);
 		//now that the dialog is set up, it's time to show it
 		dialog.show();
 	}
 
 	View.OnClickListener okListener = new OnClickListener(){
 		public void onClick(View v) {
 			EditText edit = (EditText) dialog.findViewById(R.id.editText1);
 			String inputString = edit.getText().toString();
 			//If the user typed something
 			if(!inputString.equals("")){
 				newProfile.cityName = inputString;
 				//Delete if an existing one is there
 				if(db.profileExists(newProfile.cityName))
 					db.deleteProfile(newProfile.cityName);
 
 				//Add the new profile
 				db.addProfile(newProfile);
 
 				//Send the profile back to MainActivity
 				sendProfileBack(newProfile);
 
 				//A little tip
 				Toast.makeText(getApplicationContext(),"Now you can set the new coordinates and the timezone.",Toast.LENGTH_LONG).show();
 
 				// Dismiss dialog and cityManager
 				dialog.dismiss();
 				killme();
 			}
 		}
 	};
 
 	void killme(){
 		this.finish();
 	}
 
 	void sendProfileBack(Profile profile){
 		// Send the profile back to MainActivity
 		Intent cityManagerIntent = getIntent();
 		cityManagerIntent.putParcelableArrayListExtra("profile", new ArrayList<Profile>(Collections.singletonList(profile)));
 		cityManagerIntent.putExtra("status", "statusOK");
 		setResult(RESULT_OK,cityManagerIntent);
 	}
 }
