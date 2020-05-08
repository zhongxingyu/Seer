 package ufit.profilecreation;
 
 import java.util.ArrayList;
 
 import ufit.DatabaseUtilities.*;
 
 import ufit.global.MyApp;
 import ufit.namespace.R;
 import ufit.profile.Profile;
 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.TextView;
 
 public class HomeScreen extends Activity //implements OnClickListener 
 {
 	private Profile profile;
 
 	public  MyDbAdapter eDbAdaptor;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.homescreen);
 		profile = ((MyApp)getApplication()).getProfile();
 		
 		loadInformation();
 		//View v = findViewById(R.id.create_profile);
 		//v.setOnClickListener(this);
 		
 		//#profile
 
/*		// DO NOT RUN THIS UNTIL OUR DATABSE IS COMPLETE AND IN THE ASSETS FOLDER
 * 		int workoutType;
 		workoutType = profile.getWorkoutType(); //I have assumed the returned int matches the one in the DB
 		
 		int workoutGroup; //don't know where any of this is saved or how to call it, or if its one item or a list.
 		workoutGroup = profile.getWorkoutGroup(); //I have assumed its only one focus area, stored as an int.
 		
 		int workoutSkill = profile.getSkill(); //I have assumed the returned int matches the one in the DB
 		
 		ArrayList<String> equipmentList = new ArrayList<String>();
 		equipmentList = profile.getEquipmentList();
 		
 		//should this be profile.getID() ?
 		if(profile.getID() == null) //if saved list of current exercises doesn't exist already
 		{
 			eDbAdaptor = new MyDbAdapter(this, workoutType, workoutGroup, workoutSkill, equipmentList);
 		}
 		else
 		{
 			ArrayList<Integer> storedList = profile.getID(); //should this be profile.getID() ?
 			eDbAdaptor = new MyDbAdapter(this, workoutType,workoutGroup,workoutSkill, equipmentList, storedList);
 		}
 		
 		//all valid focus group exercises should be in focusExercises when this completes
 		ArrayList<ExerciseInfo> focusExercises = new ArrayList<ExerciseInfo>();
 		focusExercises = eDbAdaptor.fetchFocusExercises();
 		// do what you want with them. NOTE: the array INDEXES do NOT match up to the EXERCISE ID stored in the database.
 		
 		//all valid non-focus group exercises should be in otherExercises when this completes
 		ArrayList<ExerciseInfo> otherExercises = new ArrayList<ExerciseInfo>();
 		otherExercises = eDbAdaptor.fetchOtherExercises();
 		//do what you want with them. NOTE: the array INDEXES do NOT match up to the EXERCISE ID stored in the database.
 		
 		//after the magical expert system returns a list of exercises, assumed to be an ArrayList<Integer> magicExerciseIDs
 		ArrayList<Integer> magicExerciseIDs;
 		magicExerciseIDs = Magic.getPlannedExercises();
 		eDbAdaptor.setStoredExerciseIDs(magicExerciseIDs);
 		
 		//store the list in the profile too for later use
 
 		 profile.setID(magicExerciseIDs); //again might need to be setID(magic) instead
 		
 		eDbAdaptor.close();
 */
 		
 		//end #profile
 		
 		
 	}
 
 	private void loadInformation() {
 		TextView name = (TextView) findViewById(R.id.homescreen_textview_name);
 		name.setText(profile.getUsername());
 	}
 //	public void onClick(View arg0) {
 	//	if(arg0.getId() == R.id.create_profile){
 		//	Intent intent = new Intent(this,Profile.class);
 			//this.startActivity(intent);
 	//	}
 		
 //	}
 	
 	
 
 }
