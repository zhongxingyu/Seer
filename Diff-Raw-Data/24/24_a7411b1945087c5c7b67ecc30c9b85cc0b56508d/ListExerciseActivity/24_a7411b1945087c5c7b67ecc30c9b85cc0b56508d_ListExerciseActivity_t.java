 /*Copyright  2012 GivDev
  * 
  * This file is part of Gymapp.
  *
  *   Gymapp is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   Gymapp is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *  along with Gymapp.  If not, see <http://www.gnu.org/licenses/>.
  *
  *	Copyright  2012 GivDev
  *
  */
 package com.Grupp01.gymapp.View.Exercise;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import android.app.Dialog;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 
 import com.Grupp01.gymapp.MainActivity;
 import com.Grupp01.gymapp.R;
 import com.Grupp01.gymapp.Controller.IdName;
 import com.Grupp01.gymapp.Controller.Exercise.ListExerciseDbHandler;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 /** 
  * @author GivDev
  * @version 0.1
  * @peer reviewed by Joel Olofsson
  * @date 07/10/2012
  */
 public class ListExerciseActivity extends SherlockActivity implements OnClickListener, OnItemClickListener {
 
 	/** Instansvariabler */
 	public final static String EXTRA_EXERCISE_NAME = "com.Grupp01.gymapp.message";
 	public static final String NO_NAME = "";
 	private Dialog dialog;
	private List<String> listElements;
 	private ArrayAdapter<String> elementAdapter;
 	private List<IdName> exercises;
 
 	/**Setups the class layout and some instance variables
 	 * @param savedInstanceState
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		//Sets the layout
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.exercise);
 
 		initDialogue();
 		initListView();
 		createListView();
 	}
 	/**Setups the menu of the class
 	 * @param menu
 	 * @return true = menu shown false = menu hidden
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		MenuInflater inflater = getSupportMenuInflater();
 		inflater.inflate(R.menu.activity_list_exercises, menu);
 		//Enables the home button in SherlockActionBar
 		getSupportActionBar().setHomeButtonEnabled(true);
 		return true;
 	}
 
 	/**
 	 * Initiate the dialog and its components.
 	 */
 	public void initDialogue()
 	{
 		dialog = new Dialog(this);
 		dialog.setContentView(R.layout.dialog);
 		dialog.setTitle(R.string.new_exercise);
 
 		((Button) dialog.findViewById(R.id.add_Button)).setOnClickListener(this);
 		((Button) dialog.findViewById(R.id.cancel_Button)).setOnClickListener(this);
 	}
 	
 	/** Initiate the listview and its elements.
 	 * 
 	 */
 	public void initListView()
 	{		
 		listElements = new ArrayList<String>();
 
 		elementAdapter = new ArrayAdapter<String>(this, R.layout.list_simple_row, listElements);
 		((ListView)findViewById(R.id.theList)).setAdapter(elementAdapter);
 		((ListView)findViewById(R.id.theList)).setOnItemClickListener(this);
 
 	}
 	/**Creates the listview with exercises which is fetched from the
 	 * database.
 	 * 
 	 */
 	public void createListView()
 	{	
 		//Get the list with exercises from the database
 		exercises = new LinkedList<IdName>();
 		ListExerciseDbHandler temp = new ListExerciseDbHandler(this);
 		temp.open();
 		exercises = temp.getExerciseIdName();
 		temp.close();
 		
 		//Add each exercise name to a list that the listadapter
 		//can handle.
 		for(IdName idname: exercises)
 		{
 			listElements.add(idname.getName());
 		}
 		elementAdapter.notifyDataSetChanged();
 	}
 
 	
 	/** The method listens to the home button and to add a new exercise button in the menu
 	 * @param item the item that has been clicked
 	 * @return true
 	 */     
 	@Override
 	public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item)
 	{
 		//Shows the dialog if addExe button is pressed
 		if(item.getItemId() == R.id.menu_addExe)
 		{
 			showAddExerciseDialog();
 		}
 		else if(item.getItemId() == android.R.id.home)
 		{
 			//from developer.android.com
 			Intent parentActivityIntent = new Intent(this, MainActivity.class);
 			parentActivityIntent.addFlags(
 					Intent.FLAG_ACTIVITY_CLEAR_TOP |
 					Intent.FLAG_ACTIVITY_NEW_TASK);
 			startActivity(parentActivityIntent);
 			finish();
 
 		}
 		return true;
 	}
 
 	
 	/**
 	 * THe user selected "Add exercise" from actionbar menu.
 	 * DIsplay the dialog with prompt for exercise name.
 	 * 
 	 */
 	private void showAddExerciseDialog() {
 		//If the user has added a new exercise before, clear the input
 		EditText name = (EditText) dialog.findViewById(R.id.exerciseName);
 		name.setText(NO_NAME);
 		
 		//Clear the hint if it has been added before
 		name.setHint(NO_NAME);
 		
 		//Show the dialog
 		dialog.show();
 
 		
 	}
 
 
 	/** Listens to the add and cancel button in the dialog
 	 * @param view the view that is clicked
 	 */
 	@Override
 	public void onClick(View view)
 	{
 		if(view == ((Button) dialog.findViewById(R.id.add_Button)))
 		{
 			//takes the text from exercise name textfield and puts it to AddExercise intent
 			//if the string is not empty
 			EditText name = (EditText) dialog.findViewById(R.id.exerciseName);
 			String inputName = name.getText().toString().trim();
 			//IF the input is only blank signs, show a hint to the user
 			//to input a name
 			if(inputName.trim().equals(NO_NAME))
 			{
 				//Clear the input to be able to display hints
 				name.setText(NO_NAME);  
 				
 				//Show the hint to the user
 				name.setHint(R.string.enter_correct_name);
 				name.setHintTextColor(Color.RED);
 			}
 			
 			//Correct name entered, write to database
 			else
 			{
 				//Create a db handler object
 				ListExerciseDbHandler dbHandler = new ListExerciseDbHandler(this);
 				dbHandler.open();
 				
 				//Write the new exercise to the database and get the ID in return
 				int id = dbHandler.addExercise(inputName);
 				
 				//Open the edit exercise activity and send the new id as intent
 				Intent intentAddExercise = new Intent(this, EditExerciseActivity.class);
 				intentAddExercise.putExtra(EXTRA_EXERCISE_NAME, id);
 				dialog.dismiss();
 				startActivity(intentAddExercise);
 			}
 		}
 		
 		//The user pressed Cancel, dismiss dialog
 		else if(view == ((Button) dialog.findViewById(R.id.cancel_Button)))
 		{
 			dialog.dismiss();
 		}
 	}
 
 	/**Listens to the listview were all the exercises are listed
 	 * @param adapter
 	 * @param view
 	 * @param n the position of the clicked element
 	 * @param t
 	 */
 	@Override
 	public void onItemClick(AdapterView<?> adapt, View view, int n, long t)
 	{
 		//Get the id of clicked element
 		int id = exercises.get(n).getId();
 		
 		//Start edit exercise activity and pass the id as an intent.
 		Intent intentViewExercise = new Intent(this, EditExerciseActivity.class);
 		intentViewExercise.putExtra(EXTRA_EXERCISE_NAME, id);
 		startActivity(intentViewExercise);				
 	}
 	
 	/**
 	 * Method for refreshing the list of exercises after adding a new one
 	 */
 	@Override
 	protected void onResume()
 	{  
 		super.onResume();
 		initListView();
 		createListView();
 	}
 }
