 package com.room.media;
 
 import com.room.Global;
 import com.room.OptionManager;
 import com.room.R;
 
 import java.util.HashMap;
 import java.util.Iterator;
 
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.SoundPool;
 
 public class MSoundManager
 {
 	//SINGLETON!!
 	public static MSoundManager getInstance()
 	{
 		if(instance == null)
 		{
 			instance = new MSoundManager();
 		}
 		return instance;
 	}
 	
 	static class LocationSensitiveSound
 	{
 		int resourceID;
 		float x,y;
 		float innerRadiusSquared;
 		float outerRadiusSquared;
 		MediaPlayer mediaPlayer;
 		
 		LocationSensitiveSound(int resourceID, float x, float y, float innerRadius, float outerRadius)
 		{
 			this.resourceID = resourceID;
 			this.x = x;
 			this.y = y;
 			this.innerRadiusSquared = innerRadius * innerRadius;
 			this.outerRadiusSquared = outerRadius * outerRadius;
 			this.mediaPlayer = MediaPlayer.create(Global.mainActivity, resourceID);
 			this.mediaPlayer.setLooping(true);
 			
 			try{mediaPlayer.setVolume(0, 0);}
 			catch(Exception e){}
 		}
 		
 		public float getVolumeAt(float px, float py)
 		{
 			float dx = px-x;
 			float dy = py-y;
 			float distSquared = dx*dx+dy*dy;
 			float volume = 0.0f;
 			if(distSquared <= innerRadiusSquared)
 			{
 				volume = 1.0f;
 			}
 			else if(distSquared <= outerRadiusSquared)
 			{
 				volume = (outerRadiusSquared - distSquared) / (outerRadiusSquared - innerRadiusSquared);
 			}
 			else
 			{
 				volume = 0;
 			}			
 			
 			return volume * OptionManager.getMasterVolume() * OptionManager.getSoundVolume();
 		}
 		
 		public void setVolume(float leftVolume, float rightVolume)
 		{
 			try
 			{
 				if(!mediaPlayer.isPlaying())
 					mediaPlayer.start();
 				
 				mediaPlayer.setVolume(leftVolume, rightVolume);
 			}
 			catch(Exception e){}
 		}
 		
 		public void mute()
 		{
 			try{mediaPlayer.setVolume(0,0);}
 			catch(Exception e){}
 		}
 		
 		public void stopAndRelease()
 		{
 			if (mediaPlayer != null)
 			{
 				try
 				{
 					mediaPlayer.stop();
 					mediaPlayer.reset();
 					mediaPlayer.release();
 				}
 				catch(Exception e){}
 			}
 		}		
 	}
 	
    public void init()
    {
 	   soundEffectsPool = new SoundPool( 20, AudioManager.STREAM_MUSIC, 0);
 	   if ( soundEffectsMap == null ) soundEffectsMap = new HashMap<Integer, Integer>();	   
 	   soundEffectsMap.put(R.raw.swords,soundEffectsPool.load(Global.mainActivity, R.raw.swords, 1));
 	   soundEffectsMap.put(R.raw.tick,soundEffectsPool.load(Global.mainActivity, R.raw.tick, 1));
 	   soundEffectsMap.put(R.raw.wood_whack,soundEffectsPool.load(Global.mainActivity, R.raw.wood_whack, 1));
 	   
 	   soundEffectsMap.put(R.raw.footstep01,soundEffectsPool.load(Global.mainActivity, R.raw.footstep01, 1));
 	   soundEffectsMap.put(R.raw.footstep02,soundEffectsPool.load(Global.mainActivity, R.raw.footstep02, 1));
 	   soundEffectsMap.put(R.raw.footstep03,soundEffectsPool.load(Global.mainActivity, R.raw.footstep03, 1));
 	   soundEffectsMap.put(R.raw.footstep04,soundEffectsPool.load(Global.mainActivity, R.raw.footstep04, 1));
 	   
 	   soundEffectsMap.put(R.raw.phone_0,soundEffectsPool.load(Global.mainActivity, R.raw.phone_0, 1));
 	   soundEffectsMap.put(R.raw.phone_1,soundEffectsPool.load(Global.mainActivity, R.raw.phone_1, 1));
 	   soundEffectsMap.put(R.raw.phone_2,soundEffectsPool.load(Global.mainActivity, R.raw.phone_2, 1));
 	   soundEffectsMap.put(R.raw.phone_3,soundEffectsPool.load(Global.mainActivity, R.raw.phone_3, 1));
 	   soundEffectsMap.put(R.raw.phone_4,soundEffectsPool.load(Global.mainActivity, R.raw.phone_4, 1));
 	   soundEffectsMap.put(R.raw.phone_5,soundEffectsPool.load(Global.mainActivity, R.raw.phone_5, 1));
 	   soundEffectsMap.put(R.raw.phone_6,soundEffectsPool.load(Global.mainActivity, R.raw.phone_6, 1));
 	   soundEffectsMap.put(R.raw.phone_7,soundEffectsPool.load(Global.mainActivity, R.raw.phone_7, 1));
 	   soundEffectsMap.put(R.raw.phone_8,soundEffectsPool.load(Global.mainActivity, R.raw.phone_8, 1));
 	   soundEffectsMap.put(R.raw.phone_9,soundEffectsPool.load(Global.mainActivity, R.raw.phone_9, 1));
 	   soundEffectsMap.put(R.raw.phone_star,soundEffectsPool.load(Global.mainActivity, R.raw.phone_star, 1));
 	   soundEffectsMap.put(R.raw.phone_pound,soundEffectsPool.load(Global.mainActivity, R.raw.phone_pound, 1));
 	   
 	   if ( locationSensitiveSounds == null )
 		   locationSensitiveSounds = new HashMap<Integer, LocationSensitiveSound>();
    }	
    
    public void addLocationSensitiveSound(int resID, float x, float y,
 		   float innerRadius, float outerRadius)
    {
 	   if(locationSensitiveSounds.containsKey(resID))
 		   return;
 	   
 	   locationSensitiveSounds.put(resID,
 			   new LocationSensitiveSound
 			   (
 					   resID,
 					   x,y,		//x,y
 					   innerRadius,outerRadius	//inner, outer
 			   ));	  
    }
    
    public void removeLocationSensitiveSound(int resId)
    {
 	   LocationSensitiveSound sound = locationSensitiveSounds.remove(resId);
 	   if(sound!=null)
 	   {
 		   sound.stopAndRelease();
 	   }
    }
    
 
 	
    public void playSoundEffect(int resource)
    {
 	   if ( OptionManager.isSoundEnabled() && soundEffectsMap != null && soundEffectsMap.containsKey(resource) )
 	   {
 		   float volume = OptionManager.getMasterVolume() * OptionManager.getSoundVolume();
            soundEffectsPool.play(soundEffectsMap.get(resource), volume, volume, 1, 0, 1f);           
        }
    }
    
    public void playLongSoundEffect(int resource, boolean loop)
    {
 	   stopLongSoundEffect();
 	   
 		//play the music if it is enabled
 		if (OptionManager.isSoundEnabled())
 		{		
 			longSoundEffectPlayer = MediaPlayer.create(Global.mainActivity, resource);
 			float volume = OptionManager.getMasterVolume() * OptionManager.getSoundVolume();
 			
 			try{longSoundEffectPlayer.setVolume(volume, volume);}
 			catch(Exception e){}
 			
 			longSoundEffectPlayer.setLooping(loop);
 					   
 			longSoundEffectPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
 			{				
 				@Override
 				public void onPrepared(MediaPlayer player)
 				{
 					player.start();
 				}
 			});					
 		}
    }
    
    public void stopLongSoundEffect()
    {
 		if (longSoundEffectPlayer != null)
 		{
 			if(longSoundEffectPlayer.isPlaying())
 				longSoundEffectPlayer.stop();
 			
 			longSoundEffectPlayer.release();
 			
 			longSoundEffectPlayer = null;
 		}
 
    }
 
 	public void playMusic(int resource)
 	{				
 		//stop the music if music is disabled
 		if (!OptionManager.isMusicEnabled())
 		{
 			if ( musicMediaPlayer != null )
 			{
 				stopAndReleaseMusic();
 			}	
 		}
 		
 		//dont play again if playing already
 		if(resource == currentMusicResourceID)
 			return;		
 		
 		//play the music if it is enabled
 		if (OptionManager.isMusicEnabled())
 		{
 			if ( musicMediaPlayer != null )
 			{
 				stopAndReleaseMusic();
 			}
 			
 			musicMediaPlayer = MediaPlayer.create(Global.mainActivity, resource);
 			float volume = OptionManager.getMasterVolume() * OptionManager.getMusicVolume();
 			
 			try{musicMediaPlayer.setVolume(volume, volume);}
 			catch(Exception e){}
 			
 			musicMediaPlayer.setLooping(true);
 					   
 			musicMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
 			{				
 				@Override
 				public void onPrepared(MediaPlayer player)
 				{
 					player.start();
 				}
 			});		
 			
 			currentMusicResourceID = resource;
 		}
 	}
 
 	public void updateMusicVolume()
 	{
		if ( musicMediaPlayer == null ) return;
 		musicMediaPlayer.setVolume(OptionManager.getMasterVolume() * OptionManager.getMusicVolume(),
 								   OptionManager.getMasterVolume() * OptionManager.getMusicVolume());
 	}
 
 	public void stopMusic()
 	{				
 		//stop the music if music is disabled
 		if (!OptionManager.isMusicEnabled())
 		{
 			if ( musicMediaPlayer != null )
 			{
 				stopAndReleaseMusic();
 			}	
 		}
 	}
 	
 	public void stopAndReleaseMusic()
 	{
 		if (musicMediaPlayer != null) {
 			musicMediaPlayer.stop();
    	       	musicMediaPlayer.release();
    	       	musicMediaPlayer = null;
    	       	currentMusicResourceID = -1;
 		}
 	}
    
    public boolean isMusicPlaying()
    {
 	   if ( musicMediaPlayer != null )
 	   {
 	       return musicMediaPlayer.isPlaying();
 	   }
 	   return false;
    }
    
    public void updateLocation(float x, float y)
    {
 	   if ( OptionManager.isSoundEnabled() )
 	   {
 		   Iterator<LocationSensitiveSound> it = locationSensitiveSounds.values().iterator();	   
 		   while(it.hasNext())
 		   {
 			   LocationSensitiveSound sound = it.next();
 			   float volume = sound.getVolumeAt(x, y);
 			   sound.setVolume(volume, volume);			   
 		   }
 	   }
    }
    
 
    public void stopAndReleaseLocationSensitiveSounds()
    {
 	   Iterator<LocationSensitiveSound> it = locationSensitiveSounds.values().iterator();	   
 	   while(it.hasNext())
 	   {
 		   LocationSensitiveSound sound = it.next();
 		   sound.stopAndRelease();
 	   }
 	   locationSensitiveSounds.clear();	
    }
    
    public void stopAllSounds()
    {
 	   stopAndReleaseMusic();	   	   
 	   stopAndReleaseLocationSensitiveSounds();
 	   	   
 	   Iterator effectsIterator = (Iterator) soundEffectsMap.entrySet().iterator();
 	   
 	   while (effectsIterator.hasNext())
 	   {
 		 //TODO: wtf..
 		   soundEffectsPool.stop((Integer)((HashMap.Entry)effectsIterator.next()).getKey());
 	   }
 	   	   
    } 
    
 	private MSoundManager()   
 	{
 		musicMediaPlayer = null;
 		soundEffectsPool = null;
 		soundEffectsMap = null;
 		locationSensitiveSounds = null;
 	}
 
 	private MediaPlayer longSoundEffectPlayer = null;
 	
    private MediaPlayer musicMediaPlayer = null;
    private int currentMusicResourceID;
 
    private SoundPool soundEffectsPool = null; //sound effects
    private HashMap<Integer, Integer> soundEffectsMap = null;
 
    private HashMap<Integer, LocationSensitiveSound> locationSensitiveSounds = null;
    
    private static MSoundManager instance;   
 }
