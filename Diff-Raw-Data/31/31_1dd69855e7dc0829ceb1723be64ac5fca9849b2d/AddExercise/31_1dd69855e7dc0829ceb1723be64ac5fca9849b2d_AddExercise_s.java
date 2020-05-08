 package com.Grupp01.gymapp;
 
 import android.content.Intent;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Spinner;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 
 public class AddExercise extends SherlockActivity implements AdapterView.OnItemSelectedListener {
 	private String[] items;
 	private Spinner spinnerType;
 	private String currentView;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_add_exercise);
 		getSupportActionBar().setHomeButtonEnabled(true);
 		Intent get_Title = getIntent();
 		setTitle(get_Title.getStringExtra(Exercise.EXTRA_EXERCISE_NAME));
 
 		Resources res = getResources();
 		items = res.getStringArray(R.array.trainingtype_array);//get String-array from strings.xml
 		initSpinnerType(0); //initialize spinner with listener and set spinner to static
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getSupportMenuInflater();
 		inflater.inflate(R.menu.activity_add_exercise, menu);
 		return true;
 	}
 	
 	public void initSpinnerType(int position)
 	{
 		spinnerType = (Spinner) findViewById(R.id.spinner_type_of_training); //Retrieves the view from .xml-file
 		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
 				R.array.trainingtype_array, android.R.layout.simple_spinner_item);//From developer.android.com
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		spinnerType.setAdapter(adapter); //Sets the adapter to the spinner
 
 		spinnerType = (Spinner) findViewById(R.id.spinner_type_of_training);
 		spinnerType.setSelection(position); //Sets the spinner default value to selected value
 		spinnerType.setOnItemSelectedListener(this); //Adds listener to spinner spinnterType
 	}
 
 	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
 		if(!items[position].equals(currentView)) //conditional if to prevent infinite loop
 		{
 			if(items[position].equals("Static"))
 			{
 				setContentView(R.layout.add_exercise_static);	//Switches the layout to the selected one
 				initSpinnerType(position);	//After switching view, adds listener to spinner again
 				currentView=items[position];	//Sets currentView to prevent infinite loop
 			}
 
 			else if(items[position].equals("Dynamic"))
 			{
 				setContentView(R.layout.add_exercise_static);	
 				initSpinnerType(position);
 				currentView=items[position];
 			}
 			else if (items[position].equals("Cardio"))
 			{
 				setContentView(R.layout.activity_add_exercise);	
 				initSpinnerType(position);
 				currentView=items[position];
 			}
 		}
 	}
 
 	public void onNothingSelected(AdapterView<?> parent)
 	{
 	}
 	
 	public void cancel(View view)
 	{
 		finish();
 	}
 }
