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
 
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 
 import com.Grupp01.gymapp.View.Exercise.ListExerciseActivity;
import com.Grupp01.gymapp.View.History.Historik;
import com.Grupp01.gymapp.View.Statistic.Statistik;
 import com.Grupp01.gymapp.View.Workout.ListWorkoutActivity;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 
 
 /** 
  * @author GivDev
  * @version 0.1
  * @peer reviewed by
  * @date 05/10/12
  *
  * Class MainActivity starts when the applicition starts and shows the mainmenu with buttons to the available activities. 
  *  
  */
 public class MainActivity extends SherlockActivity {
 	
 	/**
 	 * Instanciate the class with nessecary method calls.
 	 * 
 	 * @param savedInstanceState
 	 */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
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
     	inflater.inflate(R.menu.activity_main, menu);
     	
         return true;
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
     public void historik(View view)
     {
     	Intent historik = new Intent(this, Historik.class);
     	startActivity(historik);
     }
     
     /**
      * When button Statistics is pressed Statistics is started.
      * 
      * @param view
      */
     public void statistik(View view)
     {
     	Intent statistic = new Intent(this, Statistik.class);
     	startActivity(statistic);
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
 }
