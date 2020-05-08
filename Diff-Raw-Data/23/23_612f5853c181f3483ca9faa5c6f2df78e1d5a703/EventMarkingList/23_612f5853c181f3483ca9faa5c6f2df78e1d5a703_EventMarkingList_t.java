 package fi.vincit.babyschedule.activities;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.ListView;
 import fi.vincit.babyschedule.BabyEvent;
 import fi.vincit.babyschedule.R;
 import fi.vincit.babyschedule.ScheduleDatabase;
 import fi.vincit.babyschedule.adapters.EventMarkingListAdapter;
 
 public class EventMarkingList extends ListActivity
 							  implements View.OnCreateContextMenuListener {		
 	
 	private EventMarkingListAdapter mListAdapter;
 	@SuppressWarnings("unused")
 	private CountDownTimer mListUpdatetimer;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	Log.d("Babyschedule", "Main view onCreate()");
     	super.onCreate(savedInstanceState);    	    	
     	
     	mListAdapter = new EventMarkingListAdapter(getBaseContext());    	
     	
     	updateMainListAdapter();    	    	
     	setListAdapter(mListAdapter);
     	
     	registerForContextMenu(getListView());
     	
     	this.mListUpdatetimer = new CountDownTimer(60000, 60000) {
 			
 			@Override
 			public void onTick(long millisUntilFinished) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void onFinish() {
 				mListAdapter.notifyDataSetChanged();
 				start();
 			}
 		}.start();
     }
     
     @Override
     public void onStart() {
     	super.onStart();
     	updateMainListAdapter();
     }
 
     @Override
    public void onResume() {
    	super.onResume();
    	updateMainListAdapter();
    }
    
    @Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 									ContextMenuInfo menuInfo) {
     	super.onCreateContextMenu(menu, v, menuInfo);
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.markeventscontext, menu);
 	}
 	
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
 		BabyEvent action = (BabyEvent)info.targetView.getTag();	    
     	Log.d("Babyschedule", "Clicked button" + action.getActionName());	    	
     	ScheduleDatabase.insertBabyAction("Verneri", action.getActionName(), new Date());	    	
     	mListAdapter.updateActivityTimeNow(action);    	    
     	
     	updateMainListAdapter();
     	return true;
 	}
     
     @Override
     public void onListItemClick(ListView l, View v, int position, long id) {
     	BabyEvent action = (BabyEvent)v.getTag();
     	    	
     	if( ScheduleDatabase.getActionDatesForAction(action.getActionName()).size() > 0 ) {
     		// show list of actions for the specified type
     		Intent showSingleList = new Intent(EventMarkingList.this, SingleEventList.class);   
     		Bundle actionBundle = new Bundle();
     		actionBundle.putString("ACTIONNAME", action.getActionName());
     		showSingleList.putExtras(actionBundle);
     		startActivity(showSingleList);
     	}
     }
 
     public boolean isCurrentlyAsleep() {
     	ArrayList<Date> toSleepDates = ScheduleDatabase.getActionDatesForAction(getResources().getString(R.string.go_to_sleep));
     	ArrayList<Date> wakeUpDates = ScheduleDatabase.getActionDatesForAction(getResources().getString(R.string.woke_up));
     	
     	if( toSleepDates.isEmpty() || wakeUpDates.isEmpty() ) {
     		return toSleepDates.size() > wakeUpDates.size();    		
     	}
     	else {
     		Date latestToSleep = toSleepDates.get(toSleepDates.size()-1);
     		Date latestWakeUp = wakeUpDates.get(wakeUpDates.size()-1);    	
     		return latestToSleep.after(latestWakeUp);
     	}
     }
     
     private void updateMainListAdapter() {
     	String[] activityNames;
     	if( isCurrentlyAsleep() ) {
     		// setting the title is disabled, as it is not visible within tabbed widgets
     		//setTitle(getResources().getString(R.string.app_name) + ": Baby is now asleep");
     		activityNames = getResources().getStringArray(R.array.sleep_activities); 
     	} else {
     		//setTitle(getResources().getString(R.string.app_name) + ": Baby is now awake");
     		activityNames = getResources().getStringArray(R.array.awake_activities);
     	}    	
     	
     	mListAdapter.setActionList(ScheduleDatabase.getAllBabyActions(activityNames));    	    	
     }
 }
