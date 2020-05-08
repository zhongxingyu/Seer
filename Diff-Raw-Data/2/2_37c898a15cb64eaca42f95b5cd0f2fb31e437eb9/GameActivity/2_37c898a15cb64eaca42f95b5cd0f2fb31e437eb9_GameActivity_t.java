 package com.eps_hioa_2013.JointAttentionResearchApp;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.TimeUnit;
 import java.util.Random;
 
 import org.apache.commons.lang.time.StopWatch;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnCompletionListener;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.SystemClock;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.Chronometer;
 import android.widget.Chronometer.OnChronometerTickListener;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.VideoView;
 
 @SuppressLint("ValidFragment")
 public class GameActivity extends Activity {
 	private ImageButton topleft;
 	private ImageButton topmid;
 	private ImageButton topright;
 	private ImageButton midleft;
 	private ImageButton midmid;
 	private ImageButton midright;
 	private ImageButton bottomleft;
 	private ImageButton bottommid;
 	private ImageButton bottomright;
 	private int stagecounter = 0; //0 = Preaction; 1 = Action/signal; 2 = reward; 
 	private int roundcounter = 1;
 	private int roundcounterlimit;
 	private Date DateStartedPlaying = null;
 	private int timeToPlayInSeconds;
 	//private Timecounter timecounter;
 
 	private boolean PreactionPresent = true;
 
 	private Module mymodule;
 	private String modulenumber;
 	private Session mysession;	
 	private String[] stages = {"preaction", "signal", "action", "reward"};
 	private ArrayList<Integer> validPreactionID;
 	private ArrayList<Integer> validActionID;
 	private Boolean buttonWorks;
 	private StopWatch stopWatch;
 	private String endMessage;
 
 	private String timedLocation;
 	private Element timedElement;
 	private Timer SignalAppearTimer;
 	private Element currentReward;
 
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);		
 
 		//set full screen
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
 		setContentView(R.layout.activity_game);
 
 		System.out.println("GameActivity started");
 
 		//All class variables
 		Intent intent = getIntent();
 		validPreactionID = new ArrayList<Integer>();
 		validActionID = new ArrayList<Integer>();
 		mysession = (Session) intent.getSerializableExtra(ModuleSettingsActivity.EXTRA_SESSION);		
 		modulenumber = (intent.getStringExtra(ModuleSettingsActivity.MODULENUMBER));
 		roundcounterlimit = (int) intent.getIntExtra(ModuleSettingsActivity.EXTRA_ROUNDSTOPLAY, 0);
 		timeToPlayInSeconds = (int) intent.getIntExtra(ModuleSettingsActivity.EXTRA_TIME, 0); 
 		DateStartedPlaying = new Date();
 		mymodule = new Module();	
 		stopWatch = new StopWatch();
 
 		initializeViews(); //initializes the views (Imagebuttons)
 
 		mysession.updateStatistics("\n\n" +
 				"Started Playing a module\n" +
 				"Started playing: " + DateStartedPlaying + "\n" +
 				"Modulename: " + getNameOfModule(modulenumber) + "\n" +
 				"Preferencename: " + "MODULE" + modulenumber + "\n" +
 				"Moduledescription: " + getDescriptionOfModule(modulenumber) + "\n" +
 				"Time to play in s: " + timeToPlayInSeconds + "\n" +	
 				"Rounds to play: " + roundcounterlimit + "\n"			
 				);
 
 		loadGameInfo(mysession);
 		nextStage();				
 		stopWatch.start();	
 		if(timeToPlayInSeconds > 0)
 			startDurationTimer();
 	}
 
 	private void nextStage() {
 
 		//check stagecounter number and do appropriate things based on number
 		switch(stagecounter)
 		{
 		case 0:
 			//Load a random reward to be shown at the end of this round
 			currentReward = mymodule.getRandomRewardElement();
 			if(currentReward == null) stopGame("No reward is selected");// stop game if there are no rewards
 			
 
 			//=0, check if preactionElements exist,
 			if(!mymodule.getPreactions().isEmpty())
 			{
 				//yes? -> load stuff needed for preaction.
 				LoadPreactionStage();
 			}
 			else
 			{
 				//no? increase stagecounter call this method again
 				stagecounter++;
 				nextStage();
 			}
 			break;
 
 		case 1:
 			if(!mymodule.getSignals().isEmpty())
 			{
 				if(!mymodule.getActions().isEmpty())
 				{
 					//show actions
 					LoadActionStage(false);	//false because button isn't active until signal appears
 					LoadSignalStage(true); //True because there is a button, signal will activate button when it appears										
 				}
 				else
 				{
 					LoadSignalStage(false); //false because no action. time option is for how long signal appears until reward.
 					//no action but signal ? show signal for specified time
 				}
 			}
 			else
 			{
 				if(!mymodule.getActions().isEmpty())
 				{
 					//show actions
 					LoadActionStage(true); //pressing button will instantly show the reward.
 					buttonWorks = true;
 				}
 				else
 				{
 					//go straight to reward
 					stagecounter++;
 					nextStage();
 				}
 			}
 			break;
 
 		case 2:
 			//Show reward reward will change depending on options.	
 			stagecounter++;	
 			LoadRewardStage();
 			//nextStage();
 			break;
 
 		case 3:
 			//exit game if number of round are met else go back to stage 0			
 			if(roundcounter == roundcounterlimit)
 			{
 				//exit
 				stopGame("The last round has been played");
 			}
 			else
 			{
 				nextRound();
 			}
 			break;
 		}
 	}
 
 		private void nextRound() {
 			// TODO expand for new settings
 			roundcounter++;
 			stagecounter = 0;
 			resetScreen();
 			nextStage();
 			}
 
 	//Load elements belonging to this module and put them in the appropriate arrays.
 	private void loadGameInfo(Session mysession) {
 		int size = mysession.getElementlist().size();
 		String currentStage = "";
 		for(int o = 0; o < 4; o++)
 		{
 			currentStage = stages[o];
 			for(int i = 0; i < size; i++)
 			{
 				Element gameElement = mysession.getElementlist().get(i);
 				if(getBooleanOfModule(modulenumber, gameElement.getName() + currentStage))
 				{
 					mymodule.addElement(o, gameElement);					
 				}
 			}
 		}
 	}
 
 	//Will create a timer that stops the game when the time has passed.
 	private void startDurationTimer() {
 		Timer timer = new Timer();
 		timer.schedule(new TimerTask() {
 			@Override
 			public void run() {
 				stopGame("The set duration has been reached"); 
 			}
 		}, timeToPlayInSeconds*1000);
 
 	}
 
 	//Check if an action is required and updates the statistics file for press location and time.
 	public void onclick_touched(View view)
 	{		
 		switch(stagecounter)
 		{
 		case 0: //0 = Preaction;
 			if(validPreactionID.contains(view.getId()))
 			{
 				stagecounter++;
 				nextStage();
 			}	
 			break;
 		case 1: //1 = Action
 			if(validActionID.contains(view.getId()) && buttonWorks)
 			{
 				buttonWorks = false;
 				stagecounter++;
 				nextStage();
 			}	
 			break;
 		default:
 			break;
 		}
 		String currentTime = convertTime(stopWatch.getTime());
 
 		//TODO If image button contains element write down which element
 			String buttonName = getImageButton(view.getId());
 			//	String currentImageName = getImagePath(buttonName); TODO fix getImagePath
 			mysession.updateStatistics(currentTime + " " +  buttonName);
 	}
 
 		//TODO cut out the middle man.
 	private void LoadRewardStage() {		
 		loadReward(currentReward);	
 		}
 
 
 	//Loads the signal(s) starts timer for signal
 	//if boolean true then there is a previous action timer will be used to delay image showing up
 	//if boolean false then timer will be used to show signal for that period until reward is shown
 	private void LoadSignalStage(boolean actionAvailable) {
 		// TODO using multiple signals, timedElement and timedLocation get overwriten when you load second signal
 
 		for(int i = 0; i < mymodule.getSignals().size(); i++)
 		{
 			Element element = mymodule.getSignals().get(i);
 			String location = getElementLocation(modulenumber, element.getName() + "Signal"); 
 			//if location is not empty then it's a picture
 			if(!location.isEmpty())
 			{
 				timedElement = (ElementPicture) element;
 				timedLocation = location;
 
 			}
 			else
 			{
 				timedElement = (ElementSound) element;
 			}	
 
 			if(actionAvailable)
 				{		
 				int time = getElementDuration(modulenumber, element.getName() + "Signal");
 
 				SignalAppearTimer = new Timer();				
 				SignalAppearTimer.schedule(new TimerTask() {
 					public void run() {
 						runOnUiThread(new Runnable() {
 							@Override
 							public void run() {
 							if(!timedLocation.isEmpty())
 							{
 								displayPictureElement((ElementPicture)timedElement, getImageButton(timedLocation));										
 							}
 							else
 							{
 								loadSignalSound((ElementSound)timedElement);
 							}
 							buttonWorks = true;
 							}
 							});
 							}
 							},  time*1000);
 			}
 			else
 			{
 				int time = getElementDuration(modulenumber, element.getName() + "Signal");
 				SignalAppearTimer = new Timer();				
 				SignalAppearTimer.schedule(new TimerTask() {
 					public void run() {
 						runOnUiThread(new Runnable() {
 							@Override
 							public void run() {
 								if(!timedLocation.isEmpty())
 								{
 									displayPictureElement((ElementPicture)timedElement, getImageButton(timedLocation));	
 									stagecounter++;
 									nextStage();
 								}
 								else
 								{
 									loadSignalSound((ElementSound)timedElement);									
 								}
 							}	
 						});
 					}
 				},  time*1000);
 			}
 		}
 	}
 	
 	private void loadSignalSound(ElementSound timedElement) {
 		displaySoundReward((ElementSound)timedElement);	
 		stagecounter++;
 		nextStage();
 		}
 
 
 
 	private void LoadActionStage(boolean buttenActive) {
 		
 		buttonWorks = buttenActive;			
 
 		for(int i = 0; i < mymodule.getActions().size(); i++)
 		{
 			Element element = mymodule.getActions().get(i);
 			String location = getElementLocation(modulenumber, element.getName() + "Action"); 
 
 			// display
 			displayPictureElement((ElementPicture) element, getImageButton(location));
 			// add valid ID
 			validActionID.add(getImageButton(location).getId());
 		}
 	}
 
 	private void LoadPreactionStage() {
 		for(int i = 0; i < mymodule.getPreactions().size(); i++)
 		{
 			Element element = mymodule.getPreactions().get(i);
 			String location = getElementLocation(modulenumber, element.getName() + "Preaction"); 
 
 			// display
 			displayPictureElement((ElementPicture) element, getImageButton(location));
 			// add valid ID
 			validPreactionID.add(getImageButton(location).getId());	
 		}
 	}
 	
 	private int getElementDuration(String i, String elementName)
 	{
 		int time = 0;
 		String nameOfModulePref = "MODULE" + i;
 		SharedPreferences pref_modulesettings = getSharedPreferences(nameOfModulePref, 0);  
 		String duration = pref_modulesettings.getString(elementName + "2duration", "0");
 	
 		if(duration.contains("-"))
 		{ //Get random number between 2 numbers
 		Random r = new Random();
 		int loc = duration.indexOf("-");
 		int a = Integer.parseInt(duration.substring(0, loc));
 		int b = Integer.parseInt(duration.substring(loc));
 		time = r.nextInt(a-b) + a;
 		}
 		else
 		time = Integer.parseInt(duration);
 		return time;		
 	}
 
 
 	//stops the game
 	private void stopGame(String message) {		
 		endMessage = message;
 		DialogFragment newFragment = new StopModuleDialog();
 		newFragment.show(getFragmentManager(), "endGame");	    
 	}
 
 	public class StopModuleDialog extends DialogFragment {		
 		public Dialog onCreateDialog(Bundle savedInstanceState) {
 			// Use the Builder class for convenient dialog construction
 			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 			builder.setMessage(endMessage)
 			.setPositiveButton("ok", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 					finish();
 				}
 			});
 			// Create the AlertDialog object and return it
 			return builder.create();
 		}
 	}
 
 	//removes everything on screen
 	private void resetScreen() {
 		topleft.setImageURI(null);
 		topmid.setImageURI(null);
 		topright.setImageURI(null);
 		midleft.setImageURI(null);
 		midmid.setImageURI(null);
 		midright.setImageURI(null);
 		bottomleft.setImageURI(null);
 		bottommid.setImageURI(null);
 		bottomright.setImageURI(null);
 	}
 
 
 
 	//Checks which type of element must be loaded
 	private void loadReward(Element element) {
 		if(element instanceof ElementPicture)
 		{
 				String location = getElementLocation(modulenumber, element.getName() + "Reward"); 
 				// display
 				displayPictureElement((ElementPicture) element, getImageButton(location));
 				int time = getElementDuration(modulenumber, currentReward.getName() + "Reward");
 				if(time != 0)
 				{
 				Timer nextRoundtimer = new Timer();				
 				nextRoundtimer.schedule(new TimerTask() {
 				public void run() {
 			
 				runOnUiThread(new Runnable() {
 				
 				@Override
 				public void run() {
 						nextStage();
 					}
 					});
 					}
 				}, time*1000);
 			}
 
 		}
 		else if(element instanceof ElementSound)
 		{
 			displaySoundReward((ElementSound) element);
 			nextStage();
 		}
 		else if(element instanceof ElementVideo)
 		{
 			displayVideoReward((ElementVideo) element);
 		}					
 	}
 
 
 	public void displayVideoReward(ElementVideo myVideo)
 	{
 		//Manage Sreen
 		String location= getElementLocation(modulenumber,  myVideo.getName() + "Reward");
 		
 		ImageButton myButton = getImageButton(location);
 		myButton.setVisibility(View.GONE);
 		
 		final VideoView video;
 		if (myButton==topleft) video = ((VideoView)findViewById(R.id.videoTopLeft));
 		else if (myButton==topmid) video = ((VideoView)findViewById(R.id.videoTopMid));
 		else if (myButton==topright) video = ((VideoView)findViewById(R.id.videoTopRight));
 		else if (myButton==midleft) video = ((VideoView)findViewById(R.id.videoMidLeft));
 		else if (myButton==midmid) video = ((VideoView)findViewById(R.id.videoMidMid));
 		else if (myButton==midright) video = ((VideoView)findViewById(R.id.videoMidRight));
 		else if (myButton==bottomleft) video = ((VideoView)findViewById(R.id.videoBottomLeft));
 		else if (myButton==bottommid) video = ((VideoView)findViewById(R.id.videoBottomMid));
 		else video = ((VideoView)findViewById(R.id.videoBottomRight));
 		video.setOnCompletionListener(new OnCompletionListener() {
 			@Override
 			public void onCompletion(MediaPlayer v) {				
 				video.setVisibility(View.GONE);
 				topleft.setVisibility(View.VISIBLE);
 				topmid.setVisibility(View.VISIBLE);
 				topright.setVisibility(View.VISIBLE);
 				midleft.setVisibility(View.VISIBLE);
 				midmid.setVisibility(View.VISIBLE);
 				midright.setVisibility(View.VISIBLE);
 				bottomleft.setVisibility(View.VISIBLE);
 				bottommid.setVisibility(View.VISIBLE);
 				bottomright.setVisibility(View.VISIBLE);
 				nextStage();
 			}
 		});
 
 		//Play a video
 		video.setVisibility(View.VISIBLE);
 		video.setVideoURI(Uri.parse(myVideo.getPath()));
 		video.requestFocus();
 		video.start();
 	}
 
 	public void displaySoundReward(ElementSound mySound)
 	{
 		MediaPlayer mPlayer = new MediaPlayer();
 		if(mPlayer != null) {
 			mPlayer.stop();
 			mPlayer.release();
 		}
 		mPlayer = MediaPlayer.create(this, Uri.parse(mySound.getPath()));
 		mPlayer.start();
 		mPlayer.setOnCompletionListener(new OnCompletionListener(){
 
 			@Override
 			public void onCompletion(MediaPlayer mp) {
 				// TODO Auto-generated method stub
 				nextStage();
 			}
 		});
 	}
 
 	public void displayPictureReward(ElementPicture myPicture)
 	{
 		String location = getElementLocation(modulenumber,  myPicture.getName() + "Reward"); 
 		ImageButton myButton = getImageButton(location);
 		myButton.setImageURI(Uri.parse(myPicture.getPath()));
 		myButton.setVisibility(View.VISIBLE);
 	}
 	
 	public void displayPictureElement(ElementPicture myPicture, ImageButton myButton)
 	{
 		if(myButton == null)
 		stopGame("An picture has no location set");
 		myButton.setImageURI(Uri.parse(myPicture.getPath()));		
 	}
 	
 
 	private void initializeViews() {
 		topleft = (ImageButton) findViewById(R.id.topleft);
 		topmid = (ImageButton) findViewById(R.id.topmid);
 		topright = (ImageButton) findViewById(R.id.topright);
 		midleft = (ImageButton) findViewById(R.id.midleft);
 		midmid = (ImageButton) findViewById(R.id.midmid);
 		midright = (ImageButton) findViewById(R.id.midright);
 		bottomleft = (ImageButton) findViewById(R.id.bottomleft);
 		bottommid = (ImageButton) findViewById(R.id.bottommid);
 		bottomright = (ImageButton) findViewById(R.id.bottomright);
 	}
 
 	private ImageButton getImageButton(String location)
 	{
 		ImageButton button = null;
 		if(location.equals("topleft"))
 			button = topleft;
 		else if(location.equals("topmid"))
 			button = topmid;
 		else if(location.equals("topright"))
 			button = topright;
 		else if(location.equals("midleft"))
 			button = midleft;
 		else if(location.equals("midmid"))
 			button = midmid;
 		else if(location.equals("midright"))
 			button = midright;
 		else if(location.equals("bottomleft"))
 			button = bottomleft;
 		else if(location.equals("bottommid"))
 			button = bottommid;
 		else if(location.equals("bottomright"))
 			button = bottomright;
 		return button;		
 	}
 
 	private String getImageButton(int ID)
 	{
 		String button = "";
 		if(ID == topleft.getId())
 			button = "topleft";
 		else if(ID == topmid.getId())
 			button = "topmid";
 		else if (ID == topright.getId())
 			button = "topright";
 		else if(ID == midleft.getId())
 			button = "midleft";
 		else if(ID == midmid.getId())
 			button = "midmid" ;
 		else if(ID == midright.getId())
 			button = "midright";
 		else if(ID == bottomleft.getId())
 			button = "bottomleft";
 		else if (ID == bottommid.getId())
 			button = "bottommid";
 		else if (ID == bottomright.getId())
 			button = "bottomright";
 		return button;		
 	}
 	
 	/* TODO get Imagebutton image uri
 	private String getImagePath(String location) {
 	String path = "";
 	if(location.equals("topleft"))
 		path = topleft.;
 	else if(location.equals("topmid"))
 		path = topmid;
 	else if(location.equals("topright"))
 		path = topright;
 	else if(location.equals("midleft"))
 		path = midleft;
 	else if(location.equals("midmid"))
 		path = midmid;
 	else if(location.equals("midright"))
 		path = midright;
 	else if(location.equals("bottomleft"))
 		path = bottomleft;
 	else if(location.equals("bottommid"))
 		path = bottommid;
 	else if(location.equals("bottomright"))
 		path = bottomright;
 		return path;		
 	}
 	 */
 
 
 	public String getNameOfLastEditedModule()
 	{	
 		int modulecounter = getModulecounterOutOfPreferences();
 
 		String nameOfModulePref = "MODULE" + modulecounter;
 		SharedPreferences pref_modulesettings = getSharedPreferences(nameOfModulePref, 0);  
 		String lastEditedModule = pref_modulesettings.getString("module_name", ACCESSIBILITY_SERVICE);
 		return lastEditedModule;				
 	}
 
 	//gets module_name out of the module i
 	public String getNameOfModule(String i)
 	{	
 		String nameOfModulePref = "MODULE" + i;
 		SharedPreferences pref_modulesettings = getSharedPreferences(nameOfModulePref, 0);  
 		String nameOfModule = pref_modulesettings.getString("module_name", ACCESSIBILITY_SERVICE);
 		return nameOfModule;				
 	}
 
 	public String getDescriptionOfModule(String i)
 	{	
 		String nameOfModulePref = "MODULE" + i;
 		SharedPreferences pref_modulesettings = getSharedPreferences(nameOfModulePref, 0);  
		String nameOfDescrition = pref_modulesettings.getString("module_description", ACCESSIBILITY_SERVICE);
 		return nameOfDescrition;		
 	}
 
 	//Does the module contain this element
 	public Boolean getBooleanOfModule(String i, String elementName)
 	{	
 		String nameOfModulePref = "MODULE" + i;
 		SharedPreferences pref_modulesettings = getSharedPreferences(nameOfModulePref, 0);  
 		Boolean nameOfDescription = pref_modulesettings.getBoolean(elementName, false);        
 		return nameOfDescription;
 	}
 
 	//this Method is also present in ModuleSettingsActivity.java; This should be solved in a better way
 	public int getModulecounterOutOfPreferences() {
 		SharedPreferences pref_modulecounter = getSharedPreferences("counter", 0); 
 		int modulecounter = pref_modulecounter.getInt("modulecounter", 0);
 		return modulecounter;
 	}
 
 	public String getElementLocation(String i, String elementName)
 	{
 		String nameOfModulePref = "MODULE" + i;
 		SharedPreferences pref_modulesettings = getSharedPreferences(nameOfModulePref, 0);  
 		String location = pref_modulesettings.getString(elementName + "location", "Not set");
 		return location;
 	}
 
 	private String convertTime(long millis)
 	{
 		String time = String.format("%02d:%02d:%02d", 
 				TimeUnit.MILLISECONDS.toHours(millis),
 				TimeUnit.MILLISECONDS.toMinutes(millis) -  
 				TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)), // The change is in this line
 				TimeUnit.MILLISECONDS.toSeconds(millis) - 
 				TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))); 
 		return time;
 	}
 }
