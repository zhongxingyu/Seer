 // This class causes an activity to close if there is no user interaction for 1 minute 
 
 package com.example.projectv1;
 
 import java.util.Timer;
 
 import android.app.Activity;
 
 public class Timeout extends Timer {
 	
 	private long delay = 1* 60 * 1000;  // 1 minute * 60 seconds * 1000 milliseconds
	private Activity activity;
 	private TimeTask task;
 	
 	Timeout(Activity activity)
 	{
		this.activity=activity;
 		task = new TimeTask(activity);
 	}
 	
 	public void startTimer()
 	{
 		this.schedule(task, delay);
 	}
 	
 
 }
