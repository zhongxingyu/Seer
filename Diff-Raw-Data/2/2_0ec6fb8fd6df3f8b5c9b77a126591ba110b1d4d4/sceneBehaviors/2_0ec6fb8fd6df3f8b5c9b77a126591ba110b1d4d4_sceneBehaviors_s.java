 package edu.usf.eng.pie.avatars4change.wallpaper;
 
 import java.util.Calendar;
 import java.util.TimeZone;
 
 import edu.usf.eng.pie.avatars4change.avatar.Avatar;
 import edu.usf.eng.pie.avatars4change.notifier.Notifier;
 import edu.usf.eng.pie.avatars4change.userData.userData;
 
 import android.content.Context;
 import android.os.SystemClock;
 import android.text.format.Time;
 import android.util.Log;
 
 public class sceneBehaviors {
 	private static final String TAG = "sceneBehavior";
 	public static final String[] behaviors = {
 		"constant",
 		"Proteus Effect Study",
 		"IEEE VR demo"
 	};
     public static boolean   activeOnEvens       = true;	//active on even days?
 	
     //this method gets a behavior from the Avatar's behavior string (which has been set in the settings)
     public static void runBehavior(Context context, Avatar theAvatar){
 		long now = SystemClock.elapsedRealtime();
		long timeTillWarning = 1000 * 60 * 24;
         if((now - avatarWallpaper.lastLogTime) > timeTillWarning){   
         	Notifier.addNotification(context,"no data in past 24hrs; contact PIE-Lab staff.");
         }
     	if( theAvatar.behaviorSelectorMethod == null){
         	Log.e(TAG,"behaviorSelectorMethod = null; cannot run Behavior");
     		return;
     	}	//implied ELSE
     	Log.v(TAG,"updating scene via " + theAvatar.behaviorSelectorMethod);
     	if ( theAvatar.behaviorSelectorMethod.equalsIgnoreCase("constant") ){
     		constant(theAvatar);
     	}else if( theAvatar.behaviorSelectorMethod.equalsIgnoreCase("Proteus Effect Study")){
     		proteusStudy(theAvatar);
     	}else if( theAvatar.behaviorSelectorMethod.equalsIgnoreCase("IEEE VR demo")){
     		VRDemo(theAvatar);
     	}else{
     		Log.e(TAG, "unrecognized scene behavior " + theAvatar.behaviorSelectorMethod);
     		debug(theAvatar);	//default method
     	}
     }
     
     // avatar behavior does not change; it stays constant as it has been set
     private static void constant(Avatar theAvatar){
     	//do nothing to update the behavior, it stays your choice
     }
     
     // avatar behavior designed for use in the Proteus Effect study
 	private static void proteusStudy(Avatar theAvatar){		
 		avatarWallpaper.desiredFPS = 8;//update frameRate from PA level
 	    theAvatar.UPDATE_FREQUENCY = 60*60*1000;	//60*60*1000;	//desired time between activity level updates [ms]
 		//check for enough time to change animation
     	//TODO: change this next if issue#5 persists
 		long now = SystemClock.elapsedRealtime();		//TODO: ensure that this works even if phone switched off. 
         if((now - theAvatar.lastActivityChange) > theAvatar.UPDATE_FREQUENCY){		//if time elapsed > desired time
         	//if past bedTime and before wakeTime, sleep
             int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
             Log.v("Avatars4Change Avatar sleep clock", "current hour:" + currentHour);
             if(currentHour >= theAvatar.bedTime || currentHour < theAvatar.wakeTime){
             	//draw sleeping
             	theAvatar.setActivityLevel("sleeping");
             } else {	//awake
             	int today = Time.getJulianDay(System.currentTimeMillis(), (long) (TimeZone.getDefault().getRawOffset()/1000.0) ); 	//(new Time()).toMillis(false)
             	Log.v("Avatars4Change day calculator","time:"+System.currentTimeMillis()+"\ttimezone:"+TimeZone.getDefault().getRawOffset()+"\ttoday:"+today);
             	//set active or passive, depending on even or odd julian day
             	if(today%2 == 0){	//if today is even
             		if(activeOnEvens){
             			theAvatar.setActivityLevel("active");
             		}else{
             			theAvatar.setActivityLevel("passive");
             		}
             	}else{	//today is odd
             		if(!activeOnEvens){	//if active on odd days
             			theAvatar.setActivityLevel("active");
             		}else{
             			theAvatar.setActivityLevel("passive");
             		}
             	}
             }
         	//avatar changes activity 
         	theAvatar.randomActivity(theAvatar.getActivityLevel());
        	 	theAvatar.lastActivityChange = now;
         }
 	}
 
 	// avatar behavior cycles through all behaviors in order on a short interval
 	private static void debug(Avatar theAvatar){
 		//TODO make this happen...
 		constant(theAvatar);
 	}
 
 	// avatar shows sedentary behavior for sitting, slow active behavior for walking, fast active behavior for running
 	private static void VRDemo(Avatar theAvatar){
 		avatarWallpaper.desiredFPS = Math.round( (Math.exp(userData.currentActivityLevel))*4-3 );//update frameRate from PA level
 		String activLvl = theAvatar.getActivityLevel();
 		if(userData.currentActivityLevel > .5){	//if user is walking or greater
 			activLvl = "active";
 		} else if(userData.currentActivityLevel < .1){		//if user not moving at all
 			activLvl = "sleeping";
 		}else{	// user is not active
 			activLvl = "passive";
 		}
 		if(! activLvl.equalsIgnoreCase(theAvatar.getActivityLevel())){	//if level has changed
 			theAvatar.setActivityLevel(activLvl);	//set new level
 			//should be implied:
         	//theAvatar.randomActivity(theAvatar.getActivityLevel());
        	 	theAvatar.lastActivityChange = SystemClock.elapsedRealtime();
 		}
 	}
 }
