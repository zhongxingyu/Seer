 /*This file is part of Gymapp.
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
 package com.Grupp01.gymapp;
 
 
 import java.util.Locale;
 
 import android.app.Dialog;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.Grupp01.gymapp.Controller.Exercise.EditExerciseDbHandler;
 import com.Grupp01.gymapp.Controller.Exercise.ExerciseData;
 import com.Grupp01.gymapp.Controller.Exercise.ListExerciseDbHandler;
 import com.Grupp01.gymapp.View.Exercise.ListExerciseActivity;
 import com.Grupp01.gymapp.View.History.ListHistoryActivity;
 import com.Grupp01.gymapp.View.Profile.ProfileActivity;
 import com.Grupp01.gymapp.View.Settings.SettingsActivity;
 import com.Grupp01.gymapp.View.Workout.ListWorkoutActivity;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 
 
 /** 
  * @author GivDev
  * @version 0.1
  * @peer reviewed by Erik
  * @date 20/10/12
  *
  * Class MainActivity starts when the application starts and shows the main menu 
  * with buttons to the available activities. 
  *  
  */
 public class MainActivity extends SherlockActivity implements OnClickListener {
 	private Dialog dialog;
 	private int counter;
 	/**
 	 * Instantiate the class with necessary method calls.
 	 * 
 	 * @param savedInstanceState
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
 		changeLang();
 	}
 
 
 	/**
 	 * Sets up the menubar, note the use of actionbarsherlock, making it possible using a menubar
 	 * for lower API than 11
 	 * 
 	 * @param menu
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getSupportMenuInflater();
 		inflater.inflate(R.menu.universal_menu, menu);
 
 		return true;
 	}
 
 	@Override protected void onResume() {
 
 		changeLang();
 		super.onResume();
 	}  
 
 	/**
 	 * When button Workout is pressed ListWorkoutActivity is started.
 	 * 
 	 * @param view
 	 */
 	public void workout(View view)
 	{
 		Intent workout = new Intent(this, ListWorkoutActivity.class);
 		startActivity(workout);
 	}
 
 	/**
 	 * When button History is pressed historik is started.
 	 * 
 	 * @param view
 	 */
 	public void history(View view)
 	{
 		Intent historik = new Intent(this, ListHistoryActivity.class);
 		startActivity(historik);
 	}
 
 	/**
 	 * When button Statistics is pressed Statistics is started.
 	 * Since this is not implemented yet the user will only
 	 * see a toast notifying about this
 	 * 
 	 * @param view
 	 */
 	public void statistics(View view)
 	{
 		counter++;
 		if(counter==5)
 		{
			Toast.makeText(this, R.string.easter, Toast.LENGTH_LONG).show();
 			ListExerciseDbHandler dbHandler = new ListExerciseDbHandler(this);
 			dbHandler.open();
 
 			//Write the new exercise to the database and get the ID in return
			int id = dbHandler.addExercise(getResources().getString(R.string.joels));
 			//creates a EditExerciseDbHandler
 			EditExerciseDbHandler get = new EditExerciseDbHandler(this);
 			get.open();
 			ExerciseData exercise = get.getExerciseById(id);
 
 			//Puts description into the exercise
			exercise.putDesc(getResources().getString(R.string.joels_descr));
 			//Puts note to self into the exercise
			exercise.putNote(getResources().getString(R.string.joels_note));
 			//Puts the training type selected from spinner into the exercise
 			exercise.putTypeId(2);
 			exercise.putPri(3);
 			//puts the secondary muscle selected in spinner into the exercise
 			exercise.putSec(4);
 			//Gets and puts the added/changed data into the exercise object
 
 			//creates a EditExerci
 			//Push the changed exercise-object int the database
 			get.editExercise(exercise);
 			get.close();
 			dbHandler.close();
 		}
 		else
 		{
			Toast.makeText(this, R.string.not_implemented, 300).show();
 		}
 	}
 
 
 	/**
 	 * When button Exercises is pressed ListExerciseActivity is started.
 	 * 
 	 * @param view
 	 */
 	public void exercise(View view)
 	{
 		Intent exercise = new Intent(this, ListExerciseActivity.class);
 		startActivity(exercise);
 	}
 
 	/**
 	 * When button Settings is pressed SettingsActivity is started.
 	 * 
 	 * @param view
 	 */
 	public void settings(View view)
 	{
 		Intent settings = new Intent(this, SettingsActivity.class);
 		startActivity(settings);
 	}
 
 
 	/**
 	 * When button Profile is pressed the profile activity will be started.
 	 * 
 	 * @param view
 	 */
 	public void profile(View view)
 	{
 		Intent profile = new Intent(this, ProfileActivity.class);
 		startActivity(profile);
 	}
 
 
 	/**
 	 * This method will change the language of the application depending on the users
 	 * choice in the menu in Settings.
 	 * If the user chooses "System default" this method will fetch the information about the default setting
 	 * for the phone.
 	 * Otherwise it will set the language according to the choice.
 	 * If no choice is made, the default is set to be the phones default laguage.
 	 */
 	private void changeLang(){
 
 		//Get the stored default language from sharedpreferences.
 		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
 		String lang = sharedPref.getString("pref_key_language", "default");
 
 		//Otherwise set it to the user selected language.
 
 		Locale locale = new Locale(lang);
 		Locale.setDefault(locale);
 		Configuration config = new Configuration();
 		config.locale = locale;
 		getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
 
 		//Changing language according to thread:
 		//
 
 	}
 	/**
 	 * Is called when the user presses the back-key, prompting
 	 * if user really wants to quit.
 	 * @param keyCode
 	 * @param event
 	 */
 	@Override
 	public void onBackPressed() 
 	{
 		dialog = new Dialog(this);
 		dialog.setContentView(R.layout.y_n_dialog);
 		dialog.setTitle(R.string.quit);
 		((TextView) dialog.findViewById(R.id.TV_dialog)).setText(R.string.really_quit);
 
 		((Button) dialog.findViewById(R.id.yes_Button)).setOnClickListener(this);
 		((Button) dialog.findViewById(R.id.no_Button)).setOnClickListener(this);
 		dialog.show();
 
 	}
 
 	@Override
 	public void onClick(View view) {
 		if(view == ((Button) dialog.findViewById(R.id.yes_Button)))
 			finish();
 		else
 			dialog.dismiss();
 	}
 
 
 
 }
