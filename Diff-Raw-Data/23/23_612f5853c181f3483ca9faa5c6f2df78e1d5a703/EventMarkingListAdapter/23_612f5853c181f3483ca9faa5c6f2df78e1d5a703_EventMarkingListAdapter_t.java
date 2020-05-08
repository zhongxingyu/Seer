 package fi.vincit.babyschedule.adapters;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.TextView;
 import fi.vincit.babyschedule.BabyEvent;
 import fi.vincit.babyschedule.R;
 import fi.vincit.babyschedule.ScheduleDatabase;
 
 public class EventMarkingListAdapter extends BaseAdapter {
 
 	private ArrayList<BabyEvent> mActivities;
 	private Context mContext;
 	
 	public EventMarkingListAdapter(ArrayList<BabyEvent> activities, Context context) {
 		mActivities = activities;
 		mContext = context;
 	}
 	
 	public EventMarkingListAdapter(Context context) {	
 		mContext = context;
 	}
 	
 	public void setActionList(ArrayList<BabyEvent> actions) {
 		mActivities = actions;
 		notifyDataSetChanged();
 	}
 	
 	public int getCount() {
 		return mActivities.size();
 	}
 
 	public Object getItem(int arg0) {
 		return mActivities.get(arg0);
 	}
 
 	public long getItemId(int arg0) {
 		return arg0;
 	}
 
 	public View getView(int position, View convertView, ViewGroup group) {			
 		BabyEvent activity = mActivities.get(position);
 		View activityView = null;
 		
 		if(convertView == null) {
 			LayoutInflater inflater = (LayoutInflater) group.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			activityView = inflater.inflate(R.layout.main_list_item, null);
 		} else {
 			activityView = convertView;
 		}
 		
 		TextView name = (TextView)activityView.findViewById(R.id.ActivityName);
 		name.setText(activity.getActionName());	
 		TextView timePassed = (TextView)activityView.findViewById(R.id.LastActivity);
 		
 		if(activity.getActionName().equalsIgnoreCase(mContext.getString(R.string.go_to_sleep)) ) {
 			timePassed.setText(getFormattedTimeTextForGotToSleepEvent());
 		} else if( activity.getActionName().equalsIgnoreCase(mContext.getString(R.string.woke_up)) ) {
 			timePassed.setText(getFormattedTimeTextForWokeUpEvent());
 		} else {
 			timePassed.setText(getFormattedTimeTextForNormalEvent(activity));
 		}
 		
 		activityView.setTag(activity);
 		return activityView;
 	}
 	
 	private String getFormattedTimeTextForNormalEvent(BabyEvent event) {
 		Date date = event.getLastAction();
 		if( date != null ) {	
 			String time = "Last occurred at: " + date.toLocaleString() + "\n";								
 			String timeDiff = getTimeDiffFromDate(date);				
 			return time + timeDiff + " ago";
 		} else {
 			return "No \"" + event.getActionName() + "\" events occurred";
 		}
 	}
 	
 	private String getFormattedTimeTextForGotToSleepEvent() {
 		Date lastWokeUp = ScheduleDatabase.getLastActionOfType(mContext.getString(R.string.woke_up));
 		if( lastWokeUp != null ){
 			String time = "Baby has now been awake for \n";
 			String timeDiff = getTimeDiffFromDate(lastWokeUp);
 			return time + timeDiff;
 		} else {
 			return "Baby is now awake, no previous sleep events marked.";
 		}
 	}
 	
 	private String getFormattedTimeTextForWokeUpEvent() {
 		Date lastWokeUp = ScheduleDatabase.getLastActionOfType(mContext.getString(R.string.go_to_sleep));
 		if( lastWokeUp != null ){
 			String time = "Baby has now been sleeping for \n";
 			String timeDiff = getTimeDiffFromDate(lastWokeUp);
 			return time + timeDiff;
 		} else {
 			return "Baby is now sleeping, no previous awake events marked.";
 		}
 	}
 	
 	private String getTimeDiffFromDate(Date oldDate) {
 		Date current = new Date();
 		long diff = current.getTime() - oldDate.getTime();
 		long diffInMinutes = (diff/(1000*60)) % 60;
 		long diffInHours = (diff/(1000*3600)) % 24;
 		long diffInDays = (diff/(1000*3600*24));
 											
 		String timeDiff = "" + diffInDays + " days, " + diffInHours + " hours, " + diffInMinutes +
 						  " minutes";	
 		return timeDiff;
 	}
 	
 	public void updateActivityTimeNow(BabyEvent activity)
 	{
 		activity.addActionNow();
 		notifyDataSetChanged();
 	}
 
 }
