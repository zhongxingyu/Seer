 package com.malcom.library.android;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.util.activitylifecyclecallbackscompat.MalcomActivityLifecycleCallbacksCompat;
 
 import com.malcom.library.android.module.core.MCMCoreAdapter;
import com.malcom.library.android.module.stats.MCMStats;
 
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicInteger;
 
 /**
  * Keeps track of the opened activities to know when the user starts and finishes using the app
  * to signal the start and end of a beacon.
  *
  * It understands that the user starts using the app (beacon start) when a new activity starts
  * and the user is not in the middle of a session. And it understands that the user finishes
  * using the app when an activity stops and there are no activities started for a little while.
  */
 public class MalcomActivityLifecycleCallbacks implements MalcomActivityLifecycleCallbacksCompat
 {
 	/** Number of activities that are started in any given moment */
     private static AtomicInteger startedActivities = new AtomicInteger(0);
 
 	/** Wether the user is in the middle of a "session". We use it to know when to signal the start/end beacon. */
 	private static AtomicBoolean inTheMiddleOfASession = new AtomicBoolean(false);
 
     @Override
     public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        // Patch to fix a bug: https://github.com/MyMalcom/malcom-lib-android/issues/30
        MCMStats.initContext(activity.getApplicationContext());
     }
 
     @Override
     public void onActivityStarted(Activity activity)
 	{
 		int numActivities = startedActivities.incrementAndGet();
 		final boolean wasInTheMiddleOfASession = inTheMiddleOfASession.getAndSet(true);
 
 		if (Log.isLoggable(MCMDefines.LOG_TAG, Log.DEBUG))
 			Log.d(MCMDefines.LOG_TAG, "Activity started (total: " + numActivities + ", in session: " + wasInTheMiddleOfASession + ")");
 
 		if (!wasInTheMiddleOfASession) {
             Log.d(MCMDefines.LOG_TAG, "An activity started and there's no current session: Malcom session starts");
             MCMCoreAdapter.getInstance().moduleStatsStartBeacon(activity.getApplicationContext(),true);
         }
     }
 
     @Override
     public void onActivityResumed(Activity activity) {
 		// Nothing to do
     }
 
     @Override
     public void onActivityPaused(Activity activity) {
 		// Nothing to do
     }
 
     @Override
     public void onActivityStopped(Activity activity)
 	{
 		int numActivities = startedActivities.decrementAndGet();
 
 		if (Log.isLoggable(MCMDefines.LOG_TAG, Log.DEBUG))
 			Log.d(MCMDefines.LOG_TAG, "Activity ended   (total: " + numActivities + ")");
 
 		signalEndBeaconIfNoActivitesStarted();
     }
 
 	/**
 	 * If no activities are started it signals the end beacon and sets {@link #inTheMiddleOfASession} to false
 	 * (it is understood that the user has finished using the app for now).
 	 *
 	 * When a user changes from one activity to another there could be a lapse where there are no started
 	 * activities but immediately there will be one that starts. That's why the number of started activities
 	 * is rechecked after a little wait before actually signalling the end beacon.
 	 *
 	 * See: http://stackoverflow.com/questions/3287666/android-how-to-know-when-an-app-enters-or-the-background-mode/19502860
 	 */
 	private void signalEndBeaconIfNoActivitesStarted()
 	{
 		if (startedActivities.get() == 0)
 		{
 			new Thread()
 			{
 				@Override
 				public void run()
 				{
 					try {
 						Log.d(MCMDefines.LOG_TAG, "No activities started. Waiting a while...");
 						Thread.sleep(1000); // TODO: How much we should wait for a new activity to start?
 					} catch (InterruptedException e) {
 						Log.e(MCMDefines.LOG_TAG, "Unexpected interruption", e);
 					}
 
 					if (startedActivities.get() == 0)
 					{
 						inTheMiddleOfASession.getAndSet(false);
 						Log.d(MCMDefines.LOG_TAG, "No activities started for a while: Malcom session ends.");
 						MCMCoreAdapter.getInstance().moduleStatsEndBeacon();
 					}
 					else
 						Log.d(MCMDefines.LOG_TAG, "An activity started while waiting. Malcom session hasn't ended yet.");
 				}
 			}.start();
 		}
 	}
 
 	@Override
     public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
 		// Nothing to do
     }
 
     @Override
     public void onActivityDestroyed(Activity activity) {
 		// Nothing to do (we can't count on this method being called)
     }
 }
