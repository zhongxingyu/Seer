 package com.appsmarttech.utpro;
 
 import java.util.List;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.widget.Toast;
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.ActionBar.Tab;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.appsmarttech.utpro.RepDetail_Fragment.repNavListener;
 import com.appsmarttech.utpro.RepDetail_Fragment.setDateListener;
 import com.appsmarttech.utpro.TimeDetail_Fragment.timeNavListener;
 
 public class ExerDetail_Activity extends SherlockFragmentActivity implements ActionBar.TabListener, setDateListener, repNavListener, timeNavListener{
     
 	List<Exercise> Exercises;
	int iDayID, e, iExerID, iSize, iExerType, iDayRow;
 	DBHelper_activity db;
 	Exercise eFirstExercise;
 	String sDate;
 	FragmentTransaction ft, ftOne;
 	Tab tab;
 	Bundle bArgs;
 	Menu mnuActionBar;
 	MenuItem miSaveRep, miPrevRep, miSkip, miPrevHistory, miNextHistory, miSaveTime, miDone;
 	Intent inDays;
 	
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.exerdetail_activity);
         //declaring items about the action bar
         getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
     	getSupportActionBar().setDisplayUseLogoEnabled(true);
     	getSupportActionBar().setDisplayShowTitleEnabled(true);
     	getSupportActionBar().setDisplayShowHomeEnabled(true);
     	getSupportActionBar().setDisplayHomeAsUpEnabled(true);
     	//creating the action bar tab Workout
     	ActionBar.Tab tWorkout = getSupportActionBar().newTab();
     	tWorkout.setText("Exercise");
     	//creating the action bar tab History
     	ActionBar.Tab tHistory = getSupportActionBar().newTab();
     	tHistory.setText("History");
 
     	//setting the tab listeners - what to do when the tab is clicked
     	tWorkout.setTabListener(this);
     	tHistory.setTabListener(this);
 
     	//adding the tabs to the action bar
     	getSupportActionBar().addTab(tWorkout);
     	getSupportActionBar().addTab(tHistory);
     	
    	 	//declaring db helper class
    	 	db = (new DBHelper_activity(this));
     	
    	 	//grabbing the DayID passed by the day list activity
     	iDayID = getIntent().getIntExtra("DAY_ID", 0);
    	iDayRow = getIntent().getIntExtra("ROW_ID", 1);
     	
     	//initializing e, used to track where we are in the array
     	e = 0;
     	
     	//creating a list of exercises based on the dayID
     	Exercises = db.getAllDayExercises(iDayID);
     	
     	//getting the count of exercise array items
     	iSize = (Exercises.size() - 1);
         
     	//setting variables and title
     	mSetVars();
     	
    	//declaring day intent
    	 	inDays = new Intent(this, Days_Activity.class);
    	 	
    	 	//setting date
    	 	sDate = DateHelper.getDate();
         
         //loading the fragment again, because it doesn't work right if I don't... need to fix
         mChooseFragment(2);
     }
 
 	@Override
 	public void onTabReselected(Tab tab, FragmentTransaction ft) {
 	}
 
 	@Override  //what to do when a tab is selected
 	public void onTabSelected(Tab tab, FragmentTransaction unused) {
 		if(tab.getPosition() == 0){
		mChooseFragment(2); //open either the rep or time fragment
 		}
 		if(tab.getPosition() == 1){
		mHistoryFragment(2);  //open the history fragment
 		}
 	}
 
 	@Override //what to do when a tab is unselected
 	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
 		if(tab.getPosition() == 1){
 			
 		}
 	}
 	
	//method for determining rep or time
 	public void mChooseFragment(int iNav)
 	{
 		switch(iExerType){
 		case 1:
 			mRepFragment(iNav);
 			break;
 		case 2:
 			mTimeFragment(iNav);
 			break;
 		}
 	}
 
 
 	//method for launching the rep fragment
 	public void mRepFragment(int iNav){
 		FragmentManager fragMgr=getSupportFragmentManager();
 		FragmentTransaction ft = fragMgr.beginTransaction();
 		RepDetail_Fragment fRepDetail = new RepDetail_Fragment();
 		fRepDetail.setArguments(mSetBundle());
 		switch (iNav){ //checking if we are going forward or back
 		case 0:
 			ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
 			break;
 		case 1:
 			ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
 			break;
 		}
 		ft.replace(android.R.id.content, fRepDetail);
 		ft.commit();
 		//setting the fragment save button as visible
 		
 		
 	}
 	
 	//method for launching the time fragment
 	public void mTimeFragment(int iNav){
 		FragmentManager fragMgr=getSupportFragmentManager();
 		FragmentTransaction ft = fragMgr.beginTransaction();
 		TimeDetail_Fragment fTimeDetail = new TimeDetail_Fragment();
 		fTimeDetail.setArguments(mSetBundle());
 		switch (iNav){ //checking if we are going forward or back
 		case 0:
 			ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
 			break;
 		case 1:
 			ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
 			break;
 		}
 		ft.replace(android.R.id.content, fTimeDetail);
 		ft.commit();
 		
 		
 	}
 	//method for launching the history fragment
 	public void mHistoryFragment(int iNav){
 		FragmentManager fragMgr=getSupportFragmentManager();
 		FragmentTransaction ft = fragMgr.beginTransaction();
 		History_Fragment fHistory = new History_Fragment();
 		fHistory.setArguments(mSetBundle());
 		switch (iNav){ //checking if we are going forward or back
 		case 0:
 			ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
 			break;
 		case 1:
 			ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
 			break;
 		}
 		ft.replace(android.R.id.content, fHistory);
 		ft.commit();
 	}
 	
 	public Bundle mSetBundle(){
 		bArgs = new Bundle();
 		bArgs.putInt("kExerID", iExerID);
 		bArgs.putString("kDate", sDate);
 		
 		return bArgs;
 	}
 	//navigation for the fragment area screen
 	@Override
 	public void mNavigation(int iNav) {
 		switch(iNav){
 		case 0:
 			onPrev();
 			break;
 		case 1:
 			onNext();
 			break;
 		case 2:
 			mChooseFragment(2);
 			break;
 		}
 		
 	}
 	//listener for setting the date
 	@Override
 	public void mSetDate(String Date){
 		this.sDate = Date;
 	}
 	
 	//setting variables
 	public void mSetVars(){
 		iExerID = Exercises.get(e).getExerID();
 		iExerType = Exercises.get(e).getType();
 		setTitle(Exercises.get(e).getName());
 	}
 	
 	//actions when user hits save/next or skip
 	public void onNext(){
 			if(e<iSize)
 			{
 			e=e+1;
 			mSetVars();
 			mChooseFragment(1);
 			}
 			else
 			{
 				
				db.dayCompleteSkipped(1, iDayRow); //marking the day complete because you are at the end of the day
 				startActivity(inDays);
 			}
 			
 	}
 	//actions when user hits prev
 	public void onPrev(){
 		if(e>0)
 		{
 		e=e-1;
 		mSetVars();
 		mChooseFragment(0);
 		}
 		else
 		{
 			this.onBackPressed();
 		}
 	}
 	
 	//actions when the user hits done
 	public void onDone(){
 		Toast.makeText(this, "You're at the end", Toast.LENGTH_SHORT)
 		.show();
 	}
 	
 
 
 
 
 }
