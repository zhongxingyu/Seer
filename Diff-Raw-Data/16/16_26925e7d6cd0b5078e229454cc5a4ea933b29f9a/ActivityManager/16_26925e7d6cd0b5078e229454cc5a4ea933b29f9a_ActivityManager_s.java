 package com.github.lazycure.activities;
 
 import java.util.List;
 import android.content.Context;
 import android.util.Log;
 import com.github.lazycure.LazyCureApplication;
 import com.github.lazycure.OutputManager;
 import com.github.lazycure.Time;
 import com.github.lazycure.db.DatabaseHandler;
 
 public class ActivityManager {
 	
 	private static Context context = LazyCureApplication.getAppContext();
 	private static DatabaseHandler db = new DatabaseHandler(context);
 	
 	public static void createFirstTestActivity(){
 		Log.d("Test", "First activity created");
 		db.addActivity(new Activity(OutputManager.TEST_ACTIVITY_NAME, null, Time.getCurrentDate()));
 	}
 	
 	public static boolean activityListIsEmpty(){
 		boolean isEmpty = false;
 		int activitiesCount = db.getActivitiesCount();
 		Log.d("DB", "Activities count: " + activitiesCount);
 		if (activitiesCount < 1){
 			isEmpty = true;
 		}
 		return isEmpty;
 	}
 	
 	public static void addActivity(String activityName) {
 		if (activityName.length() != 0){
 			if (!isFirstActivity()){
				if (activityName.equalsIgnoreCase(getLastActivity().getName())){
 					continueLatestActivity();
 				}
 				else{
					db.addActivity(new Activity(activityName, null, Time.getCurrentDate()));
 				}
 			}
 			else{
		        db.addActivity(new Activity(activityName, null, Time.getCurrentDate()));
 			}
 		}
 	}
 
 	public static void removeActivity(Activity activity){
 		db.removeActivity(activity);
 	}
 
 	public static void continueLatestActivity(){
 		Activity latestActivity = getLastActivity();
 		String activityName = latestActivity.getName();
 		if (latestActivity != null){
 			removeActivity(latestActivity);
 		}
 		addActivity(activityName);
 	}
 
 	public static Activity getLastActivity(){
 		List<Activity> activities = db.getAllActivities();
 		Activity lastActivity = null;
 		if (activities.size()>0){
 			Log.d("Test", "Activities.size="+activities.size());
 			lastActivity = activities.get(activities.size()-1);
 		}
 		return lastActivity;
 	}
 	
 	public static boolean isFirstActivity(){
 		return (db.getActivitiesCount() == 0);
 	}
 }
