 /*
  * @author Leon van Tuijl && Theo
  * 
 * This class handles the game logic and the game screen.
  * 
  * 
  */
 package com.eps_hioa_2013.JointAttentionResearchApp;
 
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
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.ImageButton;
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
 	private int roundcounter = 0;
 	private int roundcounterlimit;
 	private Date DateStartedPlaying = null;
 	private int timeToPlayInSeconds;
 
 	private String modulenumber;
 	private String mainModulenumber;
 	private Session mysession;	
 	private String[] stages = {"preaction", "signal", "action", "reward"};
 	private ArrayList<Integer> validPreactionID;
 	private ArrayList<Integer> validActionID;
 	private Boolean buttonWorks;
 	private StopWatch stopWatch;
 	private String endMessage;
 
 	private String timedLocation;
 	private Element timedElement;
 	private Element currentReward;
 	private Element currentSignal;
 	private Timer nextRoundtimer;
 	private Timer SignalAppearTimer;
 	private Timer timerduration;
 	private Timer rewardAppearTimer;
 	private int moduleCounter;
 	private boolean actionRepeat;
 
 	private Module mymodule;//Module used in all the code
 	private Module mainModule; //The parent module, starting point after new round
 	private ArrayList<Module> extraModules;//Container for all child modules
 
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
 		mymodule = new Module(modulenumber);
 		mainModule = mymodule;
 		mainModulenumber = modulenumber;
 		stopWatch = new StopWatch();
 		extraModules = new ArrayList<Module>();
 
 		initializeViews(); //initializes the views (Imagebuttons)
 
 		mysession.updateStatistics("\n\n" +
 				"Started playing: " + DateStartedPlaying + "\n" +
 				"Modulename: " + getNameOfModule(modulenumber) + "\n" +
 				"Moduledescription: " + getDescriptionOfModule(modulenumber) + "\n" +
 				"Time to play in s: " + timeToPlayInSeconds + "\n" +	
 				"Rounds to play: " + roundcounterlimit + "\n"			
 				);
 
 		loadGameInfo(mymodule, modulenumber, "MainModule");		
 		nextRound();				
 		stopWatch.start();	
 		if(timeToPlayInSeconds > 0)
 			startDurationTimer();
 	}
 
 
 	//Load elements belonging to this module and put them in the appropriate arrays.
 	private void loadGameInfo( Module mymodule, String modulenumber, String moduleName) {
 		int size = mysession.getElementlist().size();
 		String currentModuleName =  moduleName + ":";
 		String currentStage = "";
 		mysession.updateStatistics(currentModuleName);
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
 		elementsToStatistics(mymodule, modulenumber);
 		//Extra modules loading.
 		for(int i = 0; i < mymodule.getPreactions().size(); i++)
 		{
 			Element currentPreaction = mymodule.getPreactions().get(i);	
 
 			String nextModuleNumber = getElementNextModuleNumber(modulenumber, currentPreaction.getName() + "Preaction3");
 			if(!nextModuleNumber.isEmpty())
 			{
 				if(nextModuleNumber.equals("Placeholder"))
 				{
 					currentPreaction.setModuleNumber(-2);		
 				}
 				else
 				{
 					currentPreaction.setModuleNumber(moduleCounter);
 					Module currentModule = new Module(nextModuleNumber);
 					extraModules.add(currentModule);
 					loadGameInfo( currentModule, currentModule.getNumberString(), "SubModule " + getNameOfModule(nextModuleNumber));
 					moduleCounter ++;
 				}				
 			}
 			else
 			{
 				currentPreaction.setModuleNumber(-1);
 			}
 		}
 
 
 
 	}
 
 
 	private void elementsToStatistics(Module mymodule, String modulenumber) {
 		if(!mymodule.getPreactions().isEmpty())
 		{
 			mysession.updateStatistics("Preactions:");
 			for(int i = 0; i < mymodule.getPreactions().size(); i++)
 			{
 				Element e = mymodule.getPreactions().get(i);
 				String name = e.getName();
 				String location = getElementLocation(modulenumber, name + "Preaction");
 				String nextModuleNumber = getElementNextModuleNumber(modulenumber, e.getName() + "Preaction3");
 				String startModule = getNameOfModule(nextModuleNumber); 
 				if(nextModuleNumber.equals("Placeholder"))
 					startModule = "used as placeholder";
 				mysession.updateStatistics(name + ", " + location + ", " + startModule);
 			}
 			mysession.updateStatistics("");
 		}
 		if(!mymodule.getActions().isEmpty())
 		{
 			mysession.updateStatistics("Actions:");
 			for(int i = 0; i < mymodule.getActions().size(); i++)
 			{
 				Element e = mymodule.getActions().get(i);
 				String name = e.getName();
 				String location = getElementLocation(modulenumber, name + "Action");
 				mysession.updateStatistics(name + ", " + location);
 			}
 			mysession.updateStatistics("");
 		}
 		if(!mymodule.getSignals().isEmpty())
 		{
 			mysession.updateStatistics("Signals:");
 			for(int i = 0; i < mymodule.getSignals().size(); i++)
 			{
 				Element e = mymodule.getSignals().get(i);
 				String name = e.getName();
 				String location = getElementLocation(modulenumber, name + "Signal");
 				String duration1 = getElementDurationString(modulenumber, name + "Signal", true);
 				String duration2 = getElementDurationString(modulenumber, name + "Signal", false);
 				mysession.updateStatistics(name + ", " + location + ", " + duration1 + ", " + duration2);
 			}
 			mysession.updateStatistics("");
 		}
 		if(!mymodule.getRewards().isEmpty())
 		{
 			mysession.updateStatistics("Rewards:");
 			for(int i = 0; i < mymodule.getRewards().size(); i++)
 			{
 				Element e = mymodule.getRewards().get(i);
 				String name = e.getName();
 				String location = getElementLocation(modulenumber, name + "Reward");
 				String duration1 = getElementDurationString(modulenumber, name + "Reward", true);
 				mysession.updateStatistics(name + ", "  + location + ", " + duration1);
 			}
 			mysession.updateStatistics("");
 		}
 	}
 
 	private void nextStage() {
 		String currentTime = convertTime(stopWatch.getTime());
 		//check stagecounter number and do appropriate things based on number
 		switch(stagecounter)
 		{
 		case 0:
 			//=0, check if preactionElements exist,
 			if(!mymodule.getPreactions().isEmpty())
 			{
 				//yes? -> load stuff needed for preaction.
 				LoadPreactionStage();
 				mysession.updateStatistics(currentTime + ", Systemmessage: Preaction stage loaded");	
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
 					if(!actionRepeat)					
 						mysession.updateStatistics(currentTime + ", Systemmessage: Action stage with signal loaded");	
 					LoadActionStage(false);	//false because button isn't active until signal appears
 					LoadSignalStage(true); //True because there is a button, signal will activate button when it appears										
 
 				}
 				else
 				{
 					mysession.updateStatistics(currentTime + ", Systemmessage: Signal only stage got loaded");	
 					LoadSignalStage(false); //false because no action. time option is for how long signal appears until reward.
 
 				}
 			}
 			else
 			{
 				if(!mymodule.getActions().isEmpty())
 				{
 					//show actions
 					mysession.updateStatistics(currentTime + ", Systemmessage: Action stage without signal loaded");	
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
 			if(currentReward != null)
 			{				
 				loadReward(currentReward);	
 				mysession.updateStatistics(currentTime + ", Systemmessage: Reward is loaded");	
 			}
 			else
 				nextStage();
 
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
 		if(mymodule != mainModule)
 			mymodule = mainModule;	
 		if(modulenumber != mainModulenumber)
 			modulenumber = mainModulenumber;
 		actionRepeat = false;
 		currentSignal = mymodule.getRandomSignalElement();
 		currentReward = mymodule.getRandomRewardElement();
 		String signal = "";
 		String reward = "";
 		if(currentSignal != null)
 			signal = (", signal used for this round: " + currentSignal.getName());
 		if(currentReward != null) 
 			reward = (", reward used for this round: " + currentReward.getName());	
 
 		roundcounter++;
 		stagecounter = 0;
 		String currentTime = convertTime(stopWatch.getTime());
 		mysession.updateStatistics(currentTime +  ", Systemmessage: round " + roundcounter + " started" + signal + reward);
 		resetScreen();
 		nextStage();
 	}
 
 
 	//Will create a timer that stops the game when the time has passed.
 	private void startDurationTimer() {
 		timerduration = new Timer();
 		timerduration.schedule(new TimerTask() {
 			@Override
 			public void run() {
 				stopGame("The set duration has been reached"); 
 			}
 		}, timeToPlayInSeconds*1000);
 
 	}
 
 	//Check if an action is required and updates the statistics file for press location and time.
 	public void onclick_touched(View view)
 	{		
 
 		String tag = "";
 		String error = "";
 		boolean nextStage = false;
 		switch(stagecounter)
 		{
 		case 0: //0 = Preaction;
 			if(validPreactionID.contains(view.getId()))
 			{
 				tag = "ValidPress";
 				Boolean checker = false;
 				for(int i = 0; i < mymodule.getPreactions().size(); i++)
 				{
 					if(mymodule.getPreactions().get(i).getImageButtonID() == view.getId())
 					{
 
 						if(mymodule.getPreactions().get(i).getModuleNumber() >= 0)
 						{
 							checker = true;	
 							mymodule = extraModules.get(mymodule.getPreactions().get(i).getModuleNumber());
 							modulenumber = mymodule.getNumberString();												
 							stagecounter = 0;
 
 							currentSignal = mymodule.getRandomSignalElement();
 							currentReward = mymodule.getRandomRewardElement();
 							String signal = "";
 							String reward = "";
 							String system = "";
 							if(currentSignal != null || currentReward != null)
 							{
 								system = ", Systemmessage:";
 								if(currentSignal != null)
 									signal = (" signal set: " + currentSignal.getName());
 								if(currentReward != null) 
 									reward = (" reward set: " + currentReward.getName());	
 								String currentTime = convertTime(stopWatch.getTime());
 								mysession.updateStatistics(currentTime + system + signal + reward); 
 							}							
 						}
 
 					}
 				}
 				if(checker == false)
 				{			
 					stagecounter++;
 				}				
 				nextStage = true;
 
 			}	
 			break;
 		case 1: //1 = Action
 			if(validActionID.contains(view.getId()) )
 			{
 				if(buttonWorks)
 				{
 					tag = "ValidPress";
 					buttonWorks = false;
 					stagecounter++;
 					if(timedLocation != null)
 					{
 						ImageButton signalField = getImageButton(timedLocation);
 						if(signalField != null)
 							signalField.setImageURI(null);
 					}
 					nextStage = true;
 				}
 				else
 				{
 					tag = "InvalidPress";
 					error = " Signal not on screen";
 				}					
 			}	
 			break;
 		default:
 			break;
 		}		
 
 		if(tag.isEmpty())
 			tag = "EmptyPress";
 			
 		String currentTime = convertTime(stopWatch.getTime());
 		String buttonName = getImageButton(view.getId());
 		mysession.updateStatistics(currentTime + ", " + tag + ", " + buttonName + error);
 		if(nextStage)
 			nextStage();
 
 	}
 
 	//Loads the signal(s) starts timer for signal
 	//if boolean true then there is a previous action timer will be used to delay image showing up
 	//if boolean false then timer will be used to show signal for that period until reward is shown
 	private void LoadSignalStage(boolean actionAvailable) {		
 		Element element = currentSignal;
 		String location = getElementLocation(modulenumber, element.getName() + "Signal"); 
 		//if location is not empty then it's a picture
 		if(!location.isEmpty())
 		{
 			timedElement = (ElementPicture) element;
 			timedLocation = location;
 
 		}
 		else
 		{
 			//error
 			stopGame("signal has no location");
 		}	
 
 		if(actionAvailable)
 		{		
 			int time = getElementDuration(modulenumber, element.getName() + "Signal", true);
 
 			SignalAppearTimer = new Timer();				
 			SignalAppearTimer.schedule(new TimerTask() {
 				public void run() {
 					runOnUiThread(new Runnable() {
 						@Override
 						public void run() {
 							if(!timedLocation.isEmpty())
 							{
 								String currentTime = convertTime(stopWatch.getTime());
 								mysession.updateStatistics(currentTime + ", Systemmessage: Signal on screen");
 								displayPictureElement((ElementPicture)timedElement, getImageButton(timedLocation));										
 							}
 							startRemoveTimer();
 							buttonWorks = true;
 						}
 
 					});
 				}
 			},  time);
 		}
 		else
 		{
 			int time = getElementDuration(modulenumber, element.getName() + "Signal", true);
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
 								startRewardAppearTimer();
 							}
 						}	
 					});
 				}
 			},  time);
 
 		}
 	}
 
 	private void startRemoveTimer() {
 		int time = getElementDuration(modulenumber, currentSignal.getName() + "Signal", false);
 		if(time > 0)
 		{
 			nextRoundtimer = new Timer();				
 			nextRoundtimer.schedule(new TimerTask() {
 				public void run() {
 
 					runOnUiThread(new Runnable() {
 
 						@Override
 						public void run() {
 							String currentTime = convertTime(stopWatch.getTime());
 							mysession.updateStatistics(currentTime + ", Systemmessage: Signal removed from screen");
 							getImageButton(timedLocation).setImageURI(null);
 							stagecounter = 1;
 							nextStage();
 						}
 					});
 				}
 			}, time);
 		}
 	}
 
 	private void startRewardAppearTimer() {
 		int time = getElementDuration(modulenumber, currentSignal.getName() + "Signal", false);
 		if(time > 0)
 		{
 			rewardAppearTimer = new Timer();				
 			rewardAppearTimer.schedule(new TimerTask() {
 				public void run() {
 
 					runOnUiThread(new Runnable() {
 
 						@Override
 						public void run() {
 							nextStage();
 						}
 					});
 				}
 			}, time);
 		}
 		else
 		{
 			nextStage();
 		}
 	}
 
 
 	private void LoadActionStage(boolean buttenActive) {
 		actionRepeat = true;
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
 		Boolean nextStage = true;
 		for(int i = 0; i < mymodule.getPreactions().size(); i++)
 		{
 			ElementPicture element = (ElementPicture)mymodule.getPreactions().get(i);
 			String location = getElementLocation(modulenumber, element.getName() + "Preaction"); 
 
 			// display
 			displayPictureElement((ElementPicture) element, getImageButton(location));
 			// add valid ID			
 			if(element.getModuleNumber() != -2)
 			{
 				validPreactionID.add(getImageButton(location).getId());	
 				nextStage = false;
 			}
 			element.setImageButtonID(getImageButton(location).getId());
 		}
 		if(nextStage)
 		{
 			stagecounter++;
 			nextStage();			
 		}
 
 	}
 
 	//stops the game
 	private void stopGame(String message) {	
 		String currentTime = convertTime(stopWatch.getTime());
 		mysession.updateStatistics(currentTime + ", Systemmesage: Game ended, " + message);
 		endMessage = message;
 		stopTimers();
 		DialogFragment newFragment = new StopModuleDialog();
 		newFragment.show(getFragmentManager(), "endGame");	    
 	}
 
 	public void stopTimers()
 	{
 		if(SignalAppearTimer != null)
 			SignalAppearTimer.cancel();
 		if(nextRoundtimer != null)
 			nextRoundtimer.cancel();
 		if(timerduration != null)
 			timerduration.cancel();
 		if(rewardAppearTimer!= null)
 			rewardAppearTimer.cancel();
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
 
 		if(nextRoundtimer != null)
 			nextRoundtimer.cancel();
 		if(element instanceof ElementPicture)
 		{
 			String location = getElementLocation(modulenumber, element.getName() + "Reward"); 
 			// display
 			displayPictureElement((ElementPicture) element, getImageButton(location));
 			int time = getElementDuration(modulenumber, currentReward.getName() + "Reward", true);
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
 				}, time);
 			}
 
 		}
 		else if(element instanceof ElementSound)
 		{
 			displaySoundReward((ElementSound) element);			
 		}
 		else if(element instanceof ElementVideo)
 		{
 			displayVideoReward((ElementVideo) element);
 		}					
 	}
 
 
 	public void displayVideoReward(ElementVideo myVideo)
 	{
 		//Manage Sreen
 		String location = getElementLocation(modulenumber,  myVideo.getName() + "Reward");
 
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
 				nextStage();
 			}
 		});
 	}
 
 	public void displayPictureElement(ElementPicture myPicture, ImageButton myButton)
 	{
 		if(myButton == null)
 			stopGame("An picture has no location set");
 		else
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
 		String nameOfModule = pref_modulesettings.getString("module_name", "");
 		return nameOfModule;				
 	}
 
 	public String getDescriptionOfModule(String i)
 	{	
 		String nameOfModulePref = "MODULE" + i;
 		SharedPreferences pref_modulesettings = getSharedPreferences(nameOfModulePref, 0);  
 		String nameOfDescrition = pref_modulesettings.getString("module_description", "");
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
 
 	private int getElementDuration(String i, String elementName, Boolean appearOrRemove)
 	{
 		int time = 0;		
 		String duration = getElementDurationString( i, elementName, appearOrRemove);
 		if(duration.contains("-"))
 		{ //Get random number between 2 numbers
 			Random r = new Random();
 			int loc = duration.indexOf("-");
 			double checkerA = Double.parseDouble(duration.substring(0, loc));
 			double checkerB = Double.parseDouble(duration.substring(loc));
 
 			int a = (int)(checkerA * 10);
 			int b = (int)(checkerB * 10);				
 			time = r.nextInt(a-b) + a;
 
 		}
 		else
 		{
 			double checker =  Double.parseDouble(duration);
 			time = (int)(checker * 10);
 		}
 		return time * 100;		
 	}
 
 	private String getElementDurationString(String i, String elementName, Boolean appearOrRemove)
 	{	
 		String nameOfModulePref = "MODULE" + i;
 		SharedPreferences pref_modulesettings = getSharedPreferences(nameOfModulePref, 0);
 		String duration = "0";
 		if(appearOrRemove)
 			duration = pref_modulesettings.getString(elementName + "2duration0", "0");
 		else
 			duration = pref_modulesettings.getString(elementName + "4duration1", "0");
 		return duration;
 	}
 
 
 	private String getElementNextModuleNumber(String i, String elementName) {
 		String nameOfModulePref = "MODULE" + i;
 		SharedPreferences pref_modulesettings = getSharedPreferences(nameOfModulePref, 0);
 		String moduleNumber = "";
 		String info =  pref_modulesettings.getString(elementName + "startModule", "");
 
 		if(!info.isEmpty())
 		{
 			if(info.equals("use as Placeholder"))
 			{
 				moduleNumber = "Placeholder";
 			}
 			else
 			{
 				int loc = info.indexOf(':');
 				moduleNumber = info.substring(0, loc);
 			}
 		}
 		//Button.pngPreaction3startModule 0: kdl
 		return moduleNumber;
 	}
 
 
 	@SuppressLint("DefaultLocale")
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
 
 
 	@Override 
 	public void onBackPressed() {
 		stopTimers();
 		finish();
 	}
 }
