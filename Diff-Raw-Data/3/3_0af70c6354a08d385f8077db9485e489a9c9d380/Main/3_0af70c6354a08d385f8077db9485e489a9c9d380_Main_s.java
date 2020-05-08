 package com.inlimite.drinkcounter;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class Main extends Activity
 {
 	public DatabaseHelper database;
 	
 	protected Spinner recordNewSpinner;
 	protected TextView textView;
 	protected SpinnerListener listener;
 	
 	protected String[] drinks;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
     	database = new DatabaseHelper(this);
     	
     	//get all the new drinks in case they changed
     	drinks = database.getAllDrinks();
     	
     	//reset the spinner to include new values
     	initializeSpinner();
     }
     
     @Override
     public void onPause()
     {
     	super.onPause();
     }
     
     @Override
     public void onResume()
     {
     	super.onResume();
     	initializeSpinner();
     }
     
     public void initializeSpinner()
     {
 
         //create a new Spinner object
         recordNewSpinner = (Spinner) findViewById(R.id.SpinnerNewChoice);
         
         //create a new spinner listener
         listener = new SpinnerListener();
         
         //Create array of string to ad into the spinner
         ArrayAdapter<String> adapter = new ArrayAdapter<String>(
             	this, android.R.layout.simple_spinner_item, drinks); 
           
         //apply theme to string array
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         //add array to spinner
         recordNewSpinner.setAdapter(adapter);
         //add listener to spinner
         recordNewSpinner.setOnItemSelectedListener(listener);
     }
     
     public void recordValue(View view)
     {
     	database.incrementCount(listener.getSelection());
         
         //notify user of action
         Toast.makeText(this, "You drank "+listener.getSelection(), Toast.LENGTH_SHORT).show();
     }
     
     public void viewCount(View view)
     {
     	//go to new page!
     	startActivity(new Intent(this, ViewCount.class));
     }
     
     public void viewMenu(View view)
     {
     	//go to new page!
     	startActivity(new Intent(this, EditMenu.class));
     }
     
 }
