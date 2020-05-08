 package cornell.eickleapp;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import cornell.eickleapp.R;
 import cornell.eickleapp.R.id;
 import cornell.eickleapp.R.layout;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.Button;
 
 public class MainMenu extends Activity implements OnClickListener {
 
 	private Button tracking, assessment, visualize, settings, resources;
 	private Intent goToThisPage;
 	private DatabaseHandler db;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		//get database
 		db = new DatabaseHandler(this);
 		
 		setContentView(R.layout.menu);
 		tracking = (Button) findViewById(R.id.bMenuTracking);
 		assessment = (Button) findViewById(R.id.bMenuAssessment);
 		visualize = (Button) findViewById(R.id.bMenuVisualize);
 		settings = (Button) findViewById(R.id.bMenuSettings);
 		//resources = (Button) findViewById(R.id.bMenuResources);
 
 		tracking.setOnClickListener(this);
 		assessment.setOnClickListener(this);
 		visualize.setOnClickListener(this);
 		settings.setOnClickListener(this);
 		//resources.setOnClickListener(this);
 		SharedPreferences getPrefs = PreferenceManager
 				.getDefaultSharedPreferences(getBaseContext());
 		Boolean checkSurveyed = getPrefs.getBoolean("hints", true);
 		if (checkSurveyed) {
 			Intent openHint = new Intent(this, MainMenuTutorial.class);
 			startActivity(openHint);
 		}
 	}
 
 	@Override
 	public void onClick(View arg0) {
 		// TODO Auto-generated method stub
 		switch (arg0.getId()) {
 		case R.id.bMenuTracking:
 			// goToThisPage = new Intent(Menu.this,Class.Tracking);
 			goToThisPage = new Intent(MainMenu.this, DrinkCounter.class);
 			startActivity(goToThisPage);
 			break;
 		case R.id.bMenuAssessment:
 			Date date = new Date();
 			ArrayList<DatabaseStore> drank = (ArrayList<DatabaseStore>)db.getVarValuesForDay("drank_last_night", date);
 			//goToThisPage = new Intent(MainMenu.this, Assessment.class);
 			//startActivity(goToThisPage);
 			if (drank == null) {
 				Intent drink_ques = new Intent(this, DailySurvey1.class);
 				startActivityForResult(drink_ques,2);
 			} else {
 				ArrayList<DatabaseStore> assess = (ArrayList<DatabaseStore>)db.getVarValuesForDay("drink_assess", date);
 				if(assess == null && drank.get(0).value.equals("True")){
 					Intent drink_assess = new Intent(MainMenu.this, DrinkAssessment.class);
 					startActivityForResult(drink_assess,4);	
 				}else{
 					Intent i = new Intent(this, Assessment.class);
 					startActivity(i);
 				}
 			}
 			
 				
 			//}
 			break;
 		case R.id.bMenuVisualize:
 			goToThisPage = new Intent(MainMenu.this, VisualizeMenu.class);
 			startActivity(goToThisPage);
 			break;
 		case R.id.bMenuSettings:
 			goToThisPage = new Intent(MainMenu.this, Settings.class);
 			startActivity(goToThisPage);
 			break;
 			/*
 		case R.id.bMenuResources:
 			goToThisPage = new Intent(MainMenu.this, Resources.class);
 			startActivity(goToThisPage);
 			break;
 			*/
 		}
 
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data){
 		if(resultCode == 2){
 			Date date = new Date();
 			ArrayList<DatabaseStore> drank = (ArrayList<DatabaseStore>)db.getVarValuesForDay("drank_last_night", date);
 			if (drank.get(0).value.equals("True")){
 				Intent how_many = new Intent(this, DrinkAssessment.class);
				startActivityForResult(how_many,4);
 			}else{
 				goToThisPage = new Intent(MainMenu.this, Assessment.class);
 				startActivity(goToThisPage);
 			}
 		}else if(resultCode ==3){
				goToThisPage = new Intent(MainMenu.this, Assessment.class);
				startActivity(goToThisPage);
 		}else if(resultCode==4){
 			Date date = new Date();
 			ArrayList<DatabaseStore> track = (ArrayList<DatabaseStore>)db.getVarValuesForDay("tracked", date);
			if(track != null){
					//goToThisPage = new Intent(MainMenu.this, Assessment.class);
					//startActivity(goToThisPage);
					Intent drink_review = new Intent(this, DrinkReview.class);
					startActivityForResult(drink_review,3);
 			}else{
 				goToThisPage = new Intent(MainMenu.this, Assessment.class);
 				startActivity(goToThisPage);
 			}
 		}
 	}
 
 	
 }
