 package cornell.eickleapp;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import android.app.Activity;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.ArrayAdapter;
 import android.widget.CheckedTextView;
 import android.widget.ListView;
 
 public class Assessment extends ListActivity {
 	ArrayList<String> surveys = new ArrayList<String>();
 	ArrayList<String> surveys_classes = new ArrayList<String>();
 	String surveyList[] = { 
 			"DailySurvey4","DailySurvey2",
			"DailySurveyExercise", "DailySurveyProductivity","ParseStore"};
	String surveyNames[] = {"Context","Overall Experience","Exercise","Productivity","ParseStore"};
 	String placement;
 	Boolean sleepSelected, exerciseSelected, productivitySelected,
 			socialSelected;
 	
 	private DatabaseHandler db;
 
 	private void drinkingDay(){
 		for (int n = 0; n < surveyNames.length; n++) {
 			surveys.add(surveyNames[n]);
 			surveys_classes.add(surveyList[n]);
 		}
 	}
 	
 	private void nonDrinkingDay(){
 		for (int n = 1; n < surveyNames.length; n++) {
 			surveys.add(surveyNames[n]);
 			surveys_classes.add(surveyList[n]);
 		}
 	}
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		//get the Database
 		db = new DatabaseHandler(this);
 		Date date = new Date();
 		ArrayList<DatabaseStore> drank = (ArrayList<DatabaseStore>)db.getVarValuesForDay("drank_last_night", date);
 			if (drank != null){
 				drinkingDay();
 			}else{
 				nonDrinkingDay();
 			}
 			
 
 			// gets rid of the evaluation categories if users unchecked it.
 			SharedPreferences getPreference = PreferenceManager
 					.getDefaultSharedPreferences(getBaseContext());
 			
 			//Boolean sleepSelected = getPreference.getBoolean("sleepInEvaluation",
 			//		true);
 			Boolean exerciseSelected = getPreference.getBoolean(
 					"exerciseInEvaluation", true);
 			Boolean productivitySelected = getPreference.getBoolean(
 					"productivityInEvaluation", true);
 			Boolean socialSelected = getPreference.getBoolean("socialInEvaluation",
 					true);
 			/*
 			if (!sleepSelected) {
 				surveys_classes.remove("DailySurveySleep");
 				surveys.remove("Sleep");
 			}*/
 			if (!exerciseSelected) {
 				surveys_classes.remove("DailySurveyExercise");
 				surveys.remove("Exercise");
 			}
 			if (!productivitySelected) {
 				surveys_classes.remove("DailySurveyProductivity");
 				surveys.remove("Productivity");
 			}
 			/*
 			if (!socialSelected) {
 				surveys_classes.remove("DailySurveySocial");
 				surveys.remove("Social");
 			}*/
 		
 			requestWindowFeature(Window.FEATURE_NO_TITLE);// full screen
 			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 					WindowManager.LayoutParams.FLAG_FULLSCREEN);
 	
 			setListAdapter(new ArrayAdapter<String>(Assessment.this,
 					android.R.layout.simple_list_item_checked, surveys));// have to
 																			// create
 																			// for
 		//}														// list
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		// TODO Auto-generated method stub
 		super.onListItemClick(l, v, position, id);
 		CheckedTextView textview = (CheckedTextView) v;
 		textview.setChecked(true);
 		placement = surveys_classes.get(position);
 
 		try {
 			Class ourClass = Class.forName("cornell.eickleapp."
 					+ placement);
 			Intent goToSurvey = new Intent(this, ourClass);
 			startActivity(goToSurvey);
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 
 }
