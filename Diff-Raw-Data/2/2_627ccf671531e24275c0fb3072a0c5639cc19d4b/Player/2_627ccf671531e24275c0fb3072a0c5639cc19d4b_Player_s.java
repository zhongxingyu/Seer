 package pure_mp3;
 
 /**
  * Write a description of class Player here.
  * 
  * @author (your name) 
  * @version (a version number or a date)
  */
 
 //import javax.sound.sampled.Clip;
 //import javax.sound.sampled.LineEvent;
 //import javax.sound.sampled.LineListener;
 //import javax.swing.JSlider;
 
 /**
  * @author martin
  *
  */
 public class Player
 {
 //	private Player thisObject;
 	private static final long serialVersionUID = 20100125;
     private MusicPlayer musicPlayer;
     private SlideUpdater slideUpdater;
     private Progress progress;
     private int playMode;
     private boolean playing;
     private boolean paused;
     
     public Player(int xPlayMode)
     {
     	playMode = xPlayMode;
         musicPlayer = null;
 //        thisObject = this;
         playing = false;
         paused = false;
     }
     
     public void playPrev()
     {
     	prev();
     	Global.info.update();
         System.out.println("Previous Title: " + Global.playList.getCurrent());
         stop();
         playpause(false);
     }
     
     public void prev()
     {
     	switch(playMode)
     	{
 	    	case 0:
 	    		//normal playmode:
 	    		Global.playList.prev();
 	    		
 	    		break;    		
 	    	case 1:
 	    		//random playmode
 	    		int current = Global.playList.getCurrent();
 	    		Global.playList.random();
 	    		if(current == Global.playList.getCurrent() && (Global.playList.getModel().getSize() > 1))
 	    		{
 	    			prev();
 	    		}
 	    		break;
     	}
     	if(Global.playList.getModelSize() == 0)
 		{
 			playing = false;
 		}
     }
     
     public void playNext()
     {
     	next();
     	Global.info.update();
         System.out.println("Next Title: " + Global.playList.getCurrent());
         stop();
         playpause(false);    	
     }
     
     public void next()
     {
     	switch(playMode)
     	{
 	    	case 0:
 	    		//normal playmode:
 	    		Global.playList.next();
 	    		break;    		
 	    	case 1:
 	    		//random playmode
 	    		int current = Global.playList.getCurrent();
 	    		Global.playList.random();
 	    		if(current == Global.playList.getCurrent() && (Global.playList.getModelSize() > 1))
 	    		{
 	    			next();
 	    		}
 	    		break;
     	}
     	if(Global.playList.getModelSize() == 0)
 		{
 			playing = false;
 		}
     }
     
 	public synchronized void playpause(boolean byUser)
     {		
 		if(musicPlayer == null && (Global.playList.getNumberOfSongs() > 0))
 		{
 			//if player hasn't started playing yet and and the playmode is random
 			if(playMode == 1 && !playing && byUser)
 			{
 				next();
 			}
			else if(playMode == 0 && !playing)
 			{
 				Global.playList.setCurrent(0);
 			}
 			//normal playmode; the value for the current song could be negative because of
 			//deleting the whole playList. So it has to be checked and fixed.
 			//Thats because when everything is deleted the first song has to 
 			//be selected and played
 			else
 			{
 				Global.playList.checkCurrentNegative();
 			}
 			//now start the playback
 			playing = true;
 			//but first update the Info about the song
 			Global.info.update();
 			//create and start the musicplayer
 			musicPlayer = new StreamMusicPlayer(Global.playList.getCurrentSong(),this);
 			musicPlayer.start();
 			//and the SlideUpdater
 			slideUpdater = new SlideUpdater(musicPlayer,progress);			
 			slideUpdater.start();	
 			System.out.println("Play: " + Global.playList.getCurrent());
 		}
 		else
 		{
 			//pause everything
 			paused = true;
 			if(musicPlayer != null)
 			{
 				musicPlayer.pause();
 			}
 			if(slideUpdater != null)
 			{
 				slideUpdater.pause();
 			}
 			
 		}
 		notify();
 	}
 	
 	public void stop()
 	{
 		//stop everything
 		if(musicPlayer!=null)
 		{
 			musicPlayer.stop_();
 		}
 		if(slideUpdater != null)
 		{
 			slideUpdater.stop_();
 		}
 		//destroy the objects. let the garbage collector his work
 		musicPlayer = null;		
 		slideUpdater = null;
 	}
 	
 	public void seek(int percentage)
 	{
 //		if(musicPlayer!=null)
 //		{
 //			stop();
 //			musicPlayer = new StreamMusicPlayer(playList.getCurrentSong(),this);
 //			musicPlayer.start();
 //			slideUpdater = new SlideUpdater(musicPlayer,progress);
 //			slideUpdater.start();
 //			musicPlayer.seek(percentage);
 //		}
 	}
     
     public void changeVolume(int xVolume)
     {
     	//if there is a player set his volume and the global vars
         if(musicPlayer != null)
         {
         	musicPlayer.setVolume(xVolume);
         }
         //if there is none just write it into the global vars
         else
         {
         	Global.setVolume(xVolume);
         }
     }
     
     public void setProgress(Progress xProgress)
     {
     	progress = xProgress;
     }
     
     public void setPlayMode(int xPlayMode)
     {
     	playMode = xPlayMode;
     }
     
     public int getPlayMode()
     {
     	return playMode;
     }
     
     public Progress getProgress()
     {
     	return progress;
     }
     
     public boolean isPlaying()
     {
     	return playing;
     }
     
     public boolean isPaused()
     {
     	return paused;
     }
     /*
      * 
      */
     public Song getCurrentSong()
     {
     	return musicPlayer.getCurrentSong();
     }
     
 
 }
