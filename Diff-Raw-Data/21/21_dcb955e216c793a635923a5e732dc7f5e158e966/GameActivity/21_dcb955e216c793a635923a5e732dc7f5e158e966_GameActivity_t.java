 package com.eps_hioa_2013.JointAttentionResearchApp;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Date;
 
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.media.MediaPlayer;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.ImageButton;
 import android.widget.VideoView;
 
 public class GameActivity extends Activity {
	/* Causes Error either because of the casting or you need to do this in a method.
 	private ImageButton topleft = (ImageButton) findViewById(R.id.topleft);
 	private ImageButton topmid = (ImageButton) findViewById(R.id.topmid);
 	private ImageButton topright = (ImageButton) findViewById(R.id.topright);
 	private ImageButton midleft = (ImageButton) findViewById(R.id.midleft);
 	private ImageButton midmid = (ImageButton) findViewById(R.id.midmid);
 	private ImageButton midright = (ImageButton) findViewById(R.id.midright);
 	private ImageButton bottomleft = (ImageButton) findViewById(R.id.bottomleft);
 	private ImageButton bottommid = (ImageButton) findViewById(R.id.bottommid);
 	private ImageButton bottomright = (ImageButton) findViewById(R.id.bottomright);
	*/
	
 	private int stagecounter = 0; //0 = Preaction; 1 = Action; 2 = Signal; 3 = Reward
 	private int roundcounter = 0;
 	private int roundcounterlimit;
 	
 	private Date DateStartedPlaying = null;
 	private int timeToPlayInSeconds;
 	//private Timecounter timecounter;
 	
 	private boolean PreactionPresent = true;
 	
 	private Module mymodule;
 	private String modulenumber;
 	private Session mysession;
 
 	
 
 	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		
 		//set full screen
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 	    this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
	    
	    setContentView(R.layout.activity_game);
	    
	    /* Something goes wrong here! casts?
 	    System.out.println("GameActivity started");
		
 	    mysession.updateStatistics("gameactivity started");
 		Intent intent = getIntent();
 		mysession = (Session) intent.getSerializableExtra(ModuleSettingsActivity.EXTRA_SESSION);		
 		modulenumber = (intent.getStringExtra(ModuleSettingsActivity.MODULENUMBER));
 		roundcounterlimit = (int) intent.getIntExtra(ModuleSettingsActivity.EXTRA_ROUNDSTOPLAY, 0);
 		timeToPlayInSeconds = (int) intent.getIntExtra(ModuleSettingsActivity.EXTRA_TIME, 0);
 		//todo: check if roundcounterlimit != 0
 		//todo: check if roundcounterlimit != 0
 		DateStartedPlaying = new Date();
 		
 		mysession.updateStatistics("\n\n" +
 			"Started Playing a module\n" +
 			"Started playing: " + DateStartedPlaying + "\n" +
 			"Modulename: " + getNameOfModule(modulenumber) + "\n" +
 			"Preferencename: " + "MODULE" + modulenumber + "\n" +
 			"Moduledescription: " + getDescriptionOfModule(modulenumber) + "\n" +
 			"Time to play in s: " + timeToPlayInSeconds + "\n" +	
 			"Rounds to play: " + roundcounterlimit + "\n"			
 				);
 		//todo: if there's no preaction in module, then the stagecounter has to be 1 in beginning
 		
 		//starts the time and makes sure to end it, if the time is over
 
 		//timecounter = new Timecounter(mysession.getDeadlineDate());
		*/
 	}
 
 	
 
 	public void onclick_touched(View view)
 	{
 		switch(stagecounter)
 		{
 		case 0: //0 = Preaction;
 			//todo: if(view.getId() == id of indeed a Preactionimage)
 			{
 				//todo: showAllActions()
 				//.....
 			}
 			
 			stagecounter++;
 				break;
 		case 1: //1 = Action
 		
 			
 			stagecounter++;
 				break;
 		case 2: //2 = Signal
 		
 			
 			stagecounter++;
 				break;
 		case 3: //3 = Reward
 		Element myReward = mymodule.getRandomRewardElement();
 		if (myReward instanceof ElementVideo)
 		{
 			playMyVideo((ElementVideo)myReward);
 		}
 		if (myReward instanceof ElementSound)
 		{
 			playMySound((ElementSound)myReward);
 		}
 		else
 		{
 			
 		}
 		
 			
 			if(/*no preaction*/false) stagecounter = 1;
 			else stagecounter = 0;
 			roundcounter++;
 				break;
 		}
 		
 		if(roundcounter >= roundcounterlimit) //limit of rounds reached
 		{
 			
 			//end game
 		}
 		
 	}
 	
 	public void playMyVideo(ElementVideo myVideo)
 	{
 		VideoView video = ((VideoView)findViewById(R.id.videoView1));
 		video.setVisibility(View.VISIBLE);
 		video.setVideoURI(Uri.parse(myVideo.getSrcPath()));
 		video.requestFocus();
 		video.start();
 		while(video.isPlaying())
 		{
 			//Thread.sleep(1000);
 		}
 		video.setVisibility(View.GONE);
 	}
 	
 	public void playMySound(ElementSound mySound)
 	{
 		MediaPlayer mediaPlayer = new MediaPlayer();
 		File file = new File(mySound.getSrcPath());
 		
         try{
             synchronized(this){
             	FileInputStream inputStream = new FileInputStream(file);
         		mediaPlayer.setDataSource(inputStream.getFD());
         		inputStream.close();
                 mediaPlayer.prepare();
                 mediaPlayer.start();
             }
         } catch(IllegalStateException ex){
             ex.printStackTrace();
         } catch(IOException ex){
             ex.printStackTrace();
         }
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
         String nameOfModule = pref_modulesettings.getString("module_name", ACCESSIBILITY_SERVICE);
 		return nameOfModule;				
 	}
 	
 	public String getDescriptionOfModule(String i)
 	{	
 		String nameOfModulePref = "MODULE" + i;
     	SharedPreferences pref_modulesettings = getSharedPreferences(nameOfModulePref, 0);  
         String nameOfDescrition = pref_modulesettings.getString("descrition", ACCESSIBILITY_SERVICE);
 		return nameOfDescrition;		
 	}
 	
 	
 	//this Method is also present in ModuleSettingsActivity.java; This should be solved in a better way
 	public int getModulecounterOutOfPreferences() {
 		SharedPreferences pref_modulecounter = getSharedPreferences("counter", 0); 
         int modulecounter = pref_modulecounter.getInt("modulecounter", 0);
 		return modulecounter;
 	}
 }
