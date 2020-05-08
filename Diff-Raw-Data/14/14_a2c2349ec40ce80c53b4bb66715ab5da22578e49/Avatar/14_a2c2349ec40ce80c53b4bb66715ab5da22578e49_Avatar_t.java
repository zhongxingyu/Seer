 package edu.usf.eng.pie.avatars4change.avatar;
 
 import java.util.Calendar;
 import java.util.Random;
 
import edu.usf.eng.pie.avatars4change.R;
 import edu.usf.eng.pie.avatars4change.storager.Sdcard;
 import edu.usf.eng.pie.avatars4change.wallpaper.sceneBehaviors;
 import android.content.Context;
 import android.graphics.Canvas;
 import android.os.SystemClock;
 import android.util.Log;
 
 public class Avatar extends Entity {
 	public final String[] ACTIVITY_LEVELS = {"active","passive","sleeping"};
 	
 	public final String defaultActivity = "running";
 	public final int defaultRealismLevel = 1;
 	
 	private final String TAG = "avatar.Avatar";
 	//avatar properties:
    public String     behaviorSelectorMethod = "null";
 	public long       UPDATE_FREQUENCY       = 1000 * 1 * 1; 	//once per UPDATE_FREQUENCY; e.g. 60s/min *10min * 1000ms/s
     public long       lastActivityChange     = -UPDATE_FREQUENCY;	//last time activity level was changed [ms]
 
 	
 	public float scaler = 1.0f;	//multiplier for the scale 
 	
 //	long lastFrameChange      = 0;		//last frame update [ms]
 	long lastUserStatusUpdate = 0;
 	
 	//values for choosing appropriate animations:
 	private String activityLevel;
 	private String activityName;
 	private int realismLevel = 111;
 	
 	// for finding the files:
 	String baseFileDirectory;		//file directory to use on sdcard
 	String spriteDir ;
 	
 	// object declarations for body parts:
 	String headName = "head";
 	Location headL = new Location();
 	String headFile ;
 
 	// top and bottom locations are switched... as in the top is actually the bottom and the bottom is actually the top... sorry about that.
 	String bodyTopName = "bodyTop";
 	String bodyBottomName="bodyBottom";
 	Location bodyLtop = new Location(0,0,2,300,0);//body always in center, full size of entity
 	Location bodyLbottom = new Location(0,0,0,300,0);
 	String bodyDirBottom ;
 	String bodyDirTop ;
 	
 	//constructor
 	public Avatar(Location LOC, int realismL, String activityL, Context context) {
 		super("AvatarObject",LOC);
		behaviorSelectorMethod =context.getString(R.string.key_configmacro);
 		baseFileDirectory = Sdcard.getFileDir(context);
 		spriteDir = baseFileDirectory + "sprites";
 		//set default image locations
 		headFile = spriteDir+"/face/default/0.png";
 		bodyDirBottom  = spriteDir+"/body/default/";
 		bodyDirTop  = bodyDirBottom;
 		
 		Log.v("Avatar","new avatar. R:" + realismLevel + " A:" + activityLevel);
 		activityLevel = activityL;
 		realismLevel = realismL;
 		
 		Log.v("Avatar","setting up "+name);
 		// set up head
 		headFile = ( baseFileDirectory + "sprites/face/default/0.png");		//create sprites
 		loadHeadLocation();
 		//reloadHeadFiles()
 		super.addSprite( headName, headFile, headL );
 		
 		reloadBodyFiles();
 		
 		// set up body
 		loadBodyLocation();
 		
 		bodyDirBottom = loadBodyDir("bottom");	//bottom layer
 		super.addAnimation( bodyBottomName, bodyDirBottom, bodyLbottom );
 		//face layer is in middle
 		bodyDirTop = loadBodyDir("top");
 		super.addAnimation( bodyTopName, bodyDirTop, bodyLtop );
 	}
 	
 	//returns true if avatar should be asleep and false otherwise
 	public boolean isAsleep(){
 	    int        bedTime                = 23;
 	    int        wakeTime               = 5;
 		int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
 		return (currentHour >= bedTime || currentHour <= wakeTime);
 	}
 	
 	public String getRandomMessage(){
 		String msg = "Hello world.";
 		
 		// the list of things the avatar might say 
 		// (many of them adapted from https://en.wikipedia.org/wiki/List_of_catchphrases)
 		String[] MSGS = {"Hey! Look at me!",
 				"You haven't forgotten about me, have you?",
 				"Bazinga!",
 				"Cowabunga!",
 				"I get no respect, I tell ya. No respect.",
 				"I'm smarter than the average avatar!",
 				"Live long and prosper.",
 				"Whassup?",
 				"Keep up the good work, Ke-mo sah-bee.",
 				"I'm ready!",
 				"I think we need more cowbell",
 				"I'll be back",
 				"There can be only one",
 				"My name is Bond, Avatar Bond",
 				"Here's looking at you, kid",
 				"You've got to ask yourself one question: Do I feel lucky?",
 				"You talkin' to me?",
 				"Eh... What's up, doc?",
 				"May the Force be with you",
 				"Great Scot!!!",
 				"Elementary, my dear Watson",
 				"Take me to your leader",
 				"Hello, old sport",
 				"Constant vigilance!",
 				"Are we having fun yet?"
 		};
 		Random generator = new Random(); 
 		int choice = generator.nextInt(MSGS.length);
 		msg = MSGS[choice];
 		return msg;
 	}
 	
 	private String loadBodyDir(String layerName){
 		return ( baseFileDirectory + "sprites/body/" + activityLevel + 
 				"/" + activityName + "/"+layerName+"/");
 	}
 	
 	// === ACTIVITY LEVEL ===
 	public String getActivityLevel(){
 		return activityLevel;
 	}
 	public void setActivityLevel(String newLevel){
 		activityLevel = newLevel;
 		Log.v("Avatar",name+" activity level set to "+activityLevel);
 		if(!this.isOkay()){		//if level does not match activity
 			randomActivity(activityLevel);		//choose random activity in new level
 		}//else don't worry about it
 	}
 	// === REALISM LEVEL === 
 	public int getRealismLevel(){
 		return realismLevel;
 	}
 	public void setRealismLevel(int newLevel){
 		realismLevel = newLevel;
 		Log.v("Avatar",name+" realism level set to "+realismLevel);
 		reloadAvatar(); // reload avatar if needed
 	}
 	
 	private void reloadAvatar(){
 		reloadBodyFiles();
 		reloadHeadFiles();
 		loadHeadLocation();
 		loadBodyLocation();
 	}
 	
 	public void setBehaviorSelectorMethod(String newMethod){
 		for (int i = 0; i<sceneBehaviors.behaviors.length; i++){
 			if (sceneBehaviors.behaviors[i].equals(newMethod)){
 				// given string is accepted
 				this.behaviorSelectorMethod = newMethod;
 				this.lastActivityChange = -this.UPDATE_FREQUENCY;	// this makes the activity update now to match selection
 				return;
 			}
 		} // else: (exit of for loop means that newMethod is unrecognized)
 		Log.e(TAG,"unrecognized behaviorSelector '"+newMethod+"'! using default 'constant'");
 		this.behaviorSelectorMethod = "constant";
 		return;
 	}
 	
 	//sets up the locations and sizes of the images for the avatar. Images are retrieved and drawn in the drawAvatar() method
 	//  must be called whenever activity/realism levels change to update locations and scales of images!
 	private void loadHeadLocation(){
 		if(activityName == null){
 			Log.e(TAG,"activityName = null; cannot get location");
 			randomActivity(this.getActivityLevel());
 			return; //don't mess with null names
 		}
 		//this location should not show, and is a bit odd for easy debug
 		Location LOC = new Location(0,0,1,30,180);	//center, layer 1, size 30, upside-down;
 		int thisFrame = 0;
 		if (super.getAnimation(bodyBottomName)!=null){
 			thisFrame = super.getAnimation(bodyBottomName).currentFrame;
 		}
 		// === ACTIVE ===
 		if(activityName.equals("running")){
 			//LOC.set(-9,42,LOC.zorder,30,0);		//old used location
 			LOC.set(-32, 106, LOC.zorder, 84, 0);
 			//static location for all frames
 		} else if(activityName.equals("basketball")){
 			//load location based on current frame of body animation
 			//find body animation
 			thisFrame++; 	// frame number mysteriously lags by 1 for this animation
 			switch (thisFrame){
 				case 10:	//this frame is not real, but needed to account for lag fix
 				case 0:
 				case 1:
 				case 2:
 					LOC.set(20,-31,LOC.zorder,40,0);
 					break;
 				case 3:
 					LOC.set(20,-21,LOC.zorder,40,0);
 					break;
 				case 4:
 					LOC.set(21,-16,LOC.zorder,40,-10);
 					break;
 				case 5:
 					LOC.set(20,-21,LOC.zorder,40,0);
 					break;
 				case 6:
 					LOC.set(21,-29,LOC.zorder,40,0);
 					break;
 				case 7:
 				case 8:
 				case 9:
 					LOC.set(21,-29,LOC.zorder,40,0);	
 					break;
 				default:
 					Log.d("avatar head location", "bad frame number "+Integer.toString(thisFrame)+" for activity "+activityName);
 					//LOC.set(6,-15,LOC.zorder,12,0);	//this is the old location	
 					break;
 			}
 			
 		} else if(activityName.equals("bicycling")){
 			LOC.set(-33,90,LOC.zorder,90,-7);	//static
 			//LOC.set(-13,42,LOC.zorder,30,-10);	//old
 		// === ASLEEP ===
 		} else if(activityName.equals("inBed")){
 			LOC.set(0,0,LOC.zorder,0,0);	//size 0 so it won't show
 			//LOC.set(100,0,LOC.zorder,25,0);	//old
 		// === PASSIVE ===
 		} else if(activityName.equals("onComputer")){
 			LOC.set(37, 62, LOC.zorder, 79, 0);	//static
 			//LOC.set(13,26,LOC.zorder,33,-5);	//old
 		} else if(activityName.equals("videoGames")){
 			LOC.set(108,50,LOC.zorder,50,0);	//static
 			//LOC.set(37,17,LOC.zorder,17,0);	//old
 		} else if(activityName.equals("watchingTV")){
 			LOC.set(26,107,LOC.zorder,84,0);	//static
 			//LOC.set(9,39,LOC.zorder,30,0);//old
 			
 			// === DEFAULT === 
 		} else {
 			Log.e(TAG,"activity name not recognized");
 			LOC.set(0,0,LOC.zorder,100,180);
 		}
 		headL = scaleLocFromPercent(LOC);
 		headL = headL.multiply(scaler);
 		setSpriteLocation(headName,headL);
 	}
 	
 	private void loadBodyLocation(){
 		//body location is constant, else do something like:
 		bodyLtop = new Location(0,0,2,300,0);//body always in center, full size of entity
 		bodyLbottom = new Location(0,0,0,300,0);
 		
 		bodyLtop = bodyLtop.multiply(scaler);
 		bodyLbottom = bodyLbottom.multiply(scaler);
 		
 		setAnimationLocation(bodyTopName,bodyLtop);
 		setAnimationLocation(bodyBottomName,bodyLbottom);
 	}
 	
 	//sets up new activity 
 	private void reloadBodyFiles(){
 		if(activityName == null){
 			Log.e(TAG, "activityName = null; cannot load body files");
 			return;	//skip over null names
 		}
 		// === ACTIVE ===
 		if(activityName.equals("running")){
 
 			bodyDirTop = spriteDir+"/blankImage/";
 			bodyDirBottom = spriteDir+"/body/active/running/";
 
 		} else if(activityName.equals("basketball")){
 			bodyDirTop = spriteDir+"/body/active/basketball/top/";
 			bodyDirBottom = spriteDir+"/body/active/basketball/bottom/";
 		} else if(activityName.equals("bicycling")){
 			bodyDirTop = spriteDir+"/body/active/bicycling/top/";
 			bodyDirBottom = spriteDir+"/body/active/bicycling/bottom/";
 		// === ASLEEP ===
 		} else if(activityName.equals("inBed")){
 
 			bodyDirBottom = spriteDir+"/body/sleeping/inBed/";
 			bodyDirTop = spriteDir+"/blankImage/";
 		// === PASSIVE ===
 		} else if(activityName.equals("onComputer")){
 			bodyDirTop = spriteDir+"/blankImage/";
 			bodyDirBottom = spriteDir+"/body/passive/onComputer/";
 			
 		} else if(activityName.equals("videoGames")){
 			bodyDirTop = spriteDir+"/blankImage/";
 			bodyDirBottom = spriteDir+"/body/passive/videoGames/";
 		} else if(activityName.equals("watchingTV")){
 			bodyDirTop = spriteDir+"/blankImage/";
 			bodyDirBottom = spriteDir+"/body/passive/watchingTV/";
 			// === DEFAULT === 
 		} else {
 			Log.e(TAG,"activity name not recognized");
 	
 			bodyDirTop = spriteDir+"/blankImage/";
 			bodyDirBottom = spriteDir+"/body/default/";
 		}
 		super.setAnimationDir( bodyTopName, bodyDirTop);
 		super.setAnimationDir( bodyBottomName, bodyDirBottom);
 		super.resetFrameCount();
 	}
 	
 	private void reloadHeadFiles(){
 		super.setSpriteFile(headName, headFile);
 	}
 	
 	// === ACTIVITY NAME ===
 	public String getActivityName() {
 		return activityName;
 	}
 	
 	public void setActivityName(String newName) {
 		this.activityName = newName;		//set new activity name
    	 	this.lastActivityChange = SystemClock.elapsedRealtime();
 		Log.v("Avatar",name+" activity set to "+activityName);
 		if(!this.isOkay()){	//change activity level if needed
 			if(newName.equals("basketball") || newName.equals("running") || newName.equals("bicycling")){
 				this.setActivityLevel("active");
 			} else if(newName.equals("onComputer") || newName.equals("videoGames") || newName.equals("watchingTV")){
 				this.setActivityLevel("passive");
 			} else if (newName.equals("inBed")){
 				this.setActivityLevel("sleeping");
 			} else {
 				Log.e("Avatars4Change Avatar", "activity name not recognized");
 				return;
 			}
 		}
 		reloadAvatar();
 	}
 	
 	//sets random activity name in the level passed
 	public void randomActivity(String level){
 		String activity = "this is not an activity";
     	//TODO: get these values from file directory on sdcard instead of having them as hardcoded strings
 		//TODO: replace this with better random function
     	int newlevel = (int) (Math.floor((Math.random()*2.99999999999)));
     	if(level.equals("active")){
         	if(newlevel == 0){
         		activity = "basketball";
         	}else if(newlevel == 1){
         		activity = "running";
         	}else if(newlevel == 2){
         		activity = "bicycling";
         	}
         } else if(level.equals("passive")){	//TODO: this should be sedentary!!!
         	if(newlevel == 0){
         		activity = "watchingTV";
         	}else if(newlevel == 1){
         		activity = "videoGames";
         	}else if(newlevel == 2){
         		activity = "onComputer";
         	}
         } else if(level.equals("sleeping")){
         	activity = "inBed";
         } else {
         	Log.e("avatar","activity level "+activity+" not recognized");
         }
     	setActivityName(activity);
 	}
 	
 	public int maxH(){		//returns max height of avatar image
 		return 200;
 	}
 	public int maxW(){		//returns max width of avatar image
 		return 200;
 	}
 	
 	// gets a random activity level from the list of possible levels
 	private String randomActivityLevel(){
 		int Min=0, Max=ACTIVITY_LEVELS.length-1;
 		int choice = Min + (int)(Math.random() * ((Max - Min) + 1));
 		return ACTIVITY_LEVELS[choice];
 	}
 	
 	//returns true if current animation name and activity are compatible
 	public boolean isOkay(){
 		if(activityName==null){
 			Log.w(TAG,"activity name is null; using random activity name");
 			randomActivity(activityLevel);
 			return isOkay();
 		}
 		
 		if(activityLevel==null){
 			Log.w(TAG,"activity level is null; using random activity level");
 			activityLevel=randomActivityLevel();
 			return isOkay();
 		}
 		
 		if(activityLevel.equals("sleeping")){
 			if(activityName.equals("inBed")){
 				return true;
 			}else return false;
 		}else if(activityLevel.equals("active")){
 			if (activityName.equals("basketball") || activityName.equals("running") || activityName.equals("bicycling")){
 				return true;
 			}else return false;
 		}else if(activityLevel.equals("passive")){
 			if(activityName.equals("onComputer") || activityName.equals("videoGames") || activityName.equals("watchingTV")){
 				return true;
 			}else return false;
 		}else return false;	//activity level not recognized
 	}
 	
 	@Override
 	public void nextFrame(){
 		loadHeadLocation();	//TODO: this is not ideal, but trying to do this in onSurfaceChanged seems to break a lot
 		loadBodyLocation();
 		super.nextFrame();
 	}
 	
 	@Override
 	public void draw(Canvas c){
 		reloadHeadFiles();	//need to reload head sprite in case it has changed		
 		super.draw(c);
 	}
 }
