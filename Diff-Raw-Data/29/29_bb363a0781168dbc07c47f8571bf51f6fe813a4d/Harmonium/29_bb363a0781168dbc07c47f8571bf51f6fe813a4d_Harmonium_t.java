 /*
  * Copyright 2008 Charles Perry
  *
  * This file is part of Harmonium, the TiVo music player.
  *
  * Harmonium is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as 
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * Harmonium is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public
  * License along with Harmonium.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
  
 
 package org.dazeend.harmonium;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.dazeend.harmonium.music.MusicCollection;
 import org.dazeend.harmonium.music.Playable;
 import org.dazeend.harmonium.music.PlaylistEligible;
 import org.dazeend.harmonium.screens.ExitScreen;
 import org.dazeend.harmonium.screens.HManagedResourceScreen;
 import org.dazeend.harmonium.screens.MainMenuScreen;
 import org.dazeend.harmonium.screens.NowPlayingScreen;
import org.dazeend.harmonium.screens.ScreenSaverScreen;

 import com.almilli.tivo.bananas.hd.HDApplication;
 import com.tivo.hme.bananas.BScreen;
 import com.tivo.hme.interfaces.IArgumentList;
 import com.tivo.hme.interfaces.IContext;
 import com.tivo.hme.sdk.Factory;
 import com.tivo.hme.sdk.HmeEvent;
 
 
 
 /**
  * @author Charles Perry (harmonium@DazeEnd.org)
  *
  */
 public class Harmonium extends HDApplication {
 	
 	// public hSkin used to hold common text attributes
 	public HSkin hSkin;
 	
 	// The context for this application
 	private IContext context;
 	
 	// Application preferences
 	private ApplicationPreferences preferences;
 	
 	// This applications DiscJockey
 	private DiscJockey discJockey = DiscJockey.getDiscJockey(this);
 	
 	private InactivityHandler inactivityHandler;
 	
 	// Are we in the simulator?
 	private boolean inSimulator = false;
 	
 	private Harmonium app;
 	
 	
 	/* (non-Javadoc)
 	 * @see com.tivo.hme.bananas.BApplicationPlus#init(com.tivo.hme.interfaces.IContext)
 	 */
 	@Override
 	public void init(IContext context) throws Exception {
 		this.context = context;
 		this.app = this;
 		super.init(context);
		HManagedResourceScreen.resetCache(this);
		ScreenSaverScreen.reset(this);
 	}
 	
 	
 	/* (non-Javadoc)
 	 * @see com.tivo.hme.bananas.BApplicationPlus#init()
 	 */
 	@Override
 	protected void initService() {
 		
 		// At this point the resolution has been set
 		super.initService();
 		
 		// get application preferences
 		this.preferences = new ApplicationPreferences(this.context);
 		
 		// Initialize skin
 		this.hSkin = new HSkin(this);
 		
 		// Refresh the music list
 		new Thread() {
 			public void run() {
 				MusicCollection.getMusicCollection(getHFactory()).refresh(app);
 			}
 		}.start();	
 		
 		// Load the main menu and background
 		MainMenuScreen mainMenuScreen = new MainMenuScreen( this, MusicCollection.getMusicCollection(this.getHFactory()) );
 		this.setBackgroundImage();
 		this.push(mainMenuScreen, TRANSITION_NONE);	
 
 		// Instantiate the inactivity handler.
 		inactivityHandler = new InactivityHandler(this);
 }
 
 	/**
 	 * @return the discJockey
 	 */
 	public DiscJockey getDiscJockey() {
 		return this.discJockey;
 	}
 
 	/**
 	 * Gets the HarmoniumFactory for this Harmonium
 	 * 
 	 * @return
 	 */
 	public HarmoniumFactory getHFactory() {
 		return (HarmoniumFactory)this.getFactory(); 
 	}
 	
 	
 	/**
 	 * @return the Application preferences
 	 */
 	public ApplicationPreferences getPreferences() {
 		return this.preferences;
 	}
 
 	
 	/**
 	 * @return the inSimulator
 	 */
 	public boolean isInSimulator() {
 		return inSimulator;
 	}
 
 
 	/**
 	 * Sets the background image based on the current resolution.
 	 *
 	 */
 	public void setBackgroundImage() {
 		
 		// If we are in the simulator, set a PNG background.
 		double screenHeight = this.getHeight();
 		double screenWidth = this.getWidth();
 		double aspectRatio = screenWidth / screenHeight;
 			
 		if(this.inSimulator) {
 			// We are in the simulator, so set a PNG background.
 			
 			// Change the background based on the new aspect ratio.
 			if( (aspectRatio > 1.7) && (aspectRatio < 1.8) ) {
 				// The current aspect ratio is approximately 16:9. 
 				// Use the high definition background meant for 720p.
 				String url = this.getContext().getBaseURI().toString();
 				try {
 					url += URLEncoder.encode("background_720.png", "UTF-8");
 				}
 				catch(UnsupportedEncodingException e) {
 				}
 				getRoot().setResource(this.createStream(url, "image/png", true));
 			}
 			else {
 				// Default background is standard definition 640 x 480 (4:3)
 				getRoot().setResource("background_sd.png");
 			}
 		}
 		else {
 			// We are running on a real TiVo, so use an MPEG background to conserve memory.
 			
 			// Change the background based on the new aspect ratio.
 			if( (aspectRatio > 1.7) && (aspectRatio < 1.8) ) {
 				// The current aspect ratio is approximately 16:9. 
 				// Use the high definition background meant for 720p.		
 				getRoot().setResource("background_720.mpg");
 			}
 			else {
 				// Default background is standard definition 640 x 480 (4:3)
 				getRoot().setResource("background_sd.mpg");
 			}
 		}
 	}
 
 	public void checkKeyPressToResetInactivityTimer(int key) {
 		// Reset inactivity on non-volume keys.
 		if (key != KEY_MUTE && key != KEY_VOLUMEDOWN && key != KEY_VOLUMEUP)
 			inactivityHandler.resetInactivityTimer();
 	}
 	
 	public void setInactive()
 	{
 		inactivityHandler.setInactive();
 	}
 	
 	public void resetInactivityTimer()
 	{
 		inactivityHandler.resetInactivityTimer();
 	}
 	
 	public void updateScreenSaverDelay()
 	{
 		inactivityHandler.updateScreenSaverDelay();
 	}
 	
 	/* (non-Javadoc)
 	 * Handles key presses from TiVo remote control.
 	 */
 	@Override
 	public boolean handleKeyPress(int key, long rawcode) {
 		
 		checkKeyPressToResetInactivityTimer(key);
 		
 		switch(key) {
 		case KEY_LEFT:
 			if(this.getStackDepth() <= 1) {
 				
 				if( this.getDiscJockey().isPlaying() ) {
 					this.push(new ExitScreen(this), TRANSITION_LEFT);
 				}
 				else {
 					// Exit
 					this.setActive(false);
 				}
 			}
 			else {
 				this.pop();
 			}
 			return true;
 		case KEY_PAUSE:
 			this.getDiscJockey().togglePause();
 			return true;
 		case KEY_INFO:
 			// Jump to the Now Playing Screen if there is music playing
 			if((this.getDiscJockey().getNowPlayingScreen() != null) && this.getDiscJockey().isPlaying() && (this.getCurrentScreen().getClass() != NowPlayingScreen.class)) {
 				this.push(this.getDiscJockey().getNowPlayingScreen(), TRANSITION_LEFT);
 				return true;
 			}
 			else{
 				this.play("bonk.snd");
 			}
 		}
 		
 		return super.handleKeyPress(key, rawcode);
 
 	}
 
 	/**
 	 * Handles TiVo events
 	 */
 	@Override
 	public boolean handleEvent(HmeEvent event) {
 		boolean result = super.handleEvent(event);
 		
 		// Check to see if this event is of a type that we want to handle
 		if( this.getDiscJockey().getNowPlayingScreen() != null && 										// if the NowPlayingScreen exists, and...
 			this.getDiscJockey().getNowPlayingScreen().getMusicStream() != null && 						// if a musicStream exists, and...
 			event.getID() == this.getDiscJockey().getNowPlayingScreen().getMusicStream().getID()  && 	// if the event we caught is for the music stream, and...
 			event.getClass() == HmeEvent.ResourceInfo.class												// if the event contained resource info
 		) {
 			// This is a ResourceInfo event which we will read for information about the status of
 			// music that is being streamed.
 			HmeEvent.ResourceInfo  resourceInfo = (HmeEvent.ResourceInfo) event;
 			
 			// Has the current track finished playing?
 			if (resourceInfo.getStatus() >= RSRC_STATUS_CLOSED) {
 				// the current track has finished, so play the next one
 				this.discJockey.playNext();
 			}
 		}
 		else if(event.getClass() == HmeEvent.DeviceInfo.class) {
 			// This event tells us what kind of TiVo is running our app.
 			HmeEvent.DeviceInfo deviceInfo = (HmeEvent.DeviceInfo) event;
 			
 			// If we are running on the simulator, we need to change the background
 			String platform = (String)deviceInfo.getMap().get("platform");
 			if(platform != null && platform.startsWith("sim-")) {
 				
 				// notify the app that we are in the simulator
 				this.inSimulator = true;
 				
 				// Set background image
 				this.setBackgroundImage();
 			}
 		}
 
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.tivo.hme.sdk.Application#handleIdle(boolean)
 	 */
 	@Override
 	public boolean handleIdle(boolean isIdle) {
 		
 		if(isIdle) {
 			inactivityHandler.setInactive();
 			// tell the receiver that we handled the idle event
 			this.acknowledgeIdle(true);
 		}
 		
 		return true;
 	}
 	
 	@Override
 	public void pop()
 	{
 		BScreen currentScreen = getCurrentScreen();
 		super.pop();
 		if (currentScreen instanceof HManagedResourceScreen)
 			((HManagedResourceScreen)currentScreen).cleanup();
 	}
 
 	/**
 	 * Server side factory.
 	 * 
 	 * @author Charles Perry (harmonium@DazeEnd.org)
 	 *
 	 */
 	public static class HarmoniumFactory extends Factory {
 		
 		private FactoryPreferences preferences;
 		private final static String VERSION = "Forked 0.6 BETA";
 
 		/**
 		 *  Create the factory. Reads preferences and initialized data structures.
 		 *  Run when server-side application begins execution.
 		 */
 		@Override
 		protected void init(IArgumentList args) {
 			
 			// print out stack traces on error and exceptions
 			try {
 				// See if all we want is version information
 				if(args.getBoolean("-version")) {
 					System.out.println(HarmoniumFactory.VERSION);
 					System.out.flush();
 					System.exit(0);
 				}
 				
 				// Read factory preferences from disk.
 				this.preferences = new FactoryPreferences(args);
 				
 				// Create the music collection
 				MusicCollection.getMusicCollection(this);
 			}
 			catch(Error e) {
 				e.printStackTrace();
 				throw e;
 			}
 			catch(RuntimeException e) {
 				e.printStackTrace();
 				throw e;
 			}
 			catch(Exception e) {
 				e.printStackTrace();
 			}
 		}
 
 	     /**
 		 * @return the preferences
 		 */
 		public FactoryPreferences getPreferences() {
 			return this.preferences;
 		}
 		
 		/**
 		 * @return the VERSION
 		 */
 		public static String getVersion() {
 			return VERSION;
 		}
 
 		/* (non-Javadoc)
          * @see com.tivo.hme.sdk.MP3Factory#getMP3StreamFromURI(java.lang.String)
          */
 		public InputStream getStream(String uri) throws IOException 
         {
             File file = new File(MusicCollection.getMusicCollection(this).getMusicRoot(), URLDecoder.decode(uri, "UTF-8"));
             if (file.exists()) 
             {            	
                 InputStream in = new FileInputStream(file);
                 return in;
             }
             else
             {
                 return super.getStream(uri);
             }
         }
 	}
 
 	public static class DiscJockey {
 		
 		public static final int BACK_UP_AFTER_SECONDS = 2;
 		
 		private Harmonium app;
 		private List<Playable> musicQueue = new ArrayList<Playable>();
 		private List<Playable> shuffledMusicQueue = new ArrayList<Playable>();
 		private Playable nowPlaying;	// the currently playing track
 		private NowPlayingScreen nowPlayingScreen;	
 		private int musicIndex;			// index of currently playing track
 		private boolean shuffleMode = false;	// true if play list is being played in shuffle mode, otherwise false.
 		private boolean repeatMode = false;		// true if playlist should start over when end is reached
 		private PlayRate playRate = PlayRate.STOP;
 		
 		/**
 		 * @param app
 		 */
 		private DiscJockey(Harmonium app) {
 			super();
 			this.app = app;
 		}
 
 		public synchronized static DiscJockey getDiscJockey(Harmonium app) {
 			if(app.getDiscJockey() == null) {
 				DiscJockey dj = new DiscJockey(app);
 				return dj;
 			}
 			else {
 				return app.getDiscJockey();
 			}
 		}
 		
 		public void play(List<PlaylistEligible> playlist, Boolean shuffleMode, Boolean repeatMode) {
 			play(playlist, shuffleMode, repeatMode, null);
 		}
 		
 		public void play(List<PlaylistEligible> playlist, Boolean shuffleMode, Boolean repeatMode, Playable startPlaying) {
 			
 			// Only do stuff if the playlist is not empty
 			if(playlist != null && (! playlist.isEmpty() ) ) {
 				
 				this.shuffleMode = shuffleMode;
 				this.repeatMode = repeatMode;
 				
 				// If music is playing, stop it before changing the active playlist.
 				if(this.nowPlaying != null) {
 					this.stop();
 				}
 				
 				// Empty the music queues
 				this.musicQueue.clear();
 				this.shuffledMusicQueue.clear();
 				
 				// get tracks from the playlist and put them in the music queue
 				for(PlaylistEligible musicItem : playlist) {
 					this.musicQueue.addAll( musicItem.listMemberTracks(this.app) );
 				}
 				
 				// create the shuffled version of the music queue
 				this.shuffledMusicQueue.addAll(this.musicQueue);
 				Collections.shuffle(this.shuffledMusicQueue);
 				
 				// Reset the index, and find music to play
 				if (startPlaying == null)
 					this.musicIndex = 0;
 				else {
 					if (this.shuffleMode)
 						this.musicIndex = this.shuffledMusicQueue.indexOf(startPlaying);
 					else
 						this.musicIndex = this.musicQueue.indexOf(startPlaying);
 				}
 	
 				if(this.shuffleMode)
 					this.nowPlaying = this.shuffledMusicQueue.get(this.musicIndex);
 				else
 					this.nowPlaying = this.musicQueue.get(this.musicIndex);
 				
 				pushNowPlayingScreen();
 				
 				// Start playing music
 				if( this.nowPlaying.play(this.nowPlayingScreen) ) {
 					this.playRate = PlayRate.NORMAL;
 				}
 				else {
 					this.playNext();
 				}
 			}
 			else {
 				// Bonk if we attempt to play an empty playlist.
 				this.app.play("bonk.snd");
 			}
 			
 		}
 		
 		private void pushNowPlayingScreen()
 		{
 			// push Now Playing Screen
 			if(this.nowPlayingScreen == null) {
 				this.nowPlayingScreen = new NowPlayingScreen(app, this.nowPlaying);
 			}
 			else {
 				this.nowPlayingScreen.update(this.nowPlaying);
 			}
 			this.app.push(this.nowPlayingScreen, TRANSITION_NONE);
 		}
 		
 		public void enqueueNext(Playable playable) {
 			int nextIndex = this.musicIndex + 1;
 			this.shuffledMusicQueue.add(nextIndex, playable);
 			this.musicQueue.add(nextIndex, playable);
 			pushNowPlayingScreen();
 		}
 		
 		public void enqueueAtEnd(Playable playable) {
 			this.musicQueue.add(playable);
 			this.shuffledMusicQueue.add(playable);
 			pushNowPlayingScreen();
 		}
 		
 		public void playNext() {
 			// Stop any track that might be playing
 			if(this.nowPlaying != null) {
 				this.nowPlaying.stop(this.nowPlayingScreen);
 			}
 			
 			if(! this.musicQueue.isEmpty() ){
 				
 				// See if the last track in the music queue has been reached
 				if( this.musicIndex >= (this.musicQueue.size() - 1) ) {
 					// We have reached the last track
 					if(this.repeatMode) {
 						// Reset to the first track if we are in repeat mode
 						this.musicIndex = 0;
 					}
 					else {
 						// not in repeat mode, so stop playing music and exit				
 						this.stop();		
 						return;
 					}
 				}
 				else {
 					// We still haven't reached the last track, so go to the next one
 					++this.musicIndex;
 				}
 
 				// Play the next track
 				if(this.shuffleMode) {
 					this.nowPlaying = this.shuffledMusicQueue.get(this.musicIndex);
 				}
 				else {
 					this.nowPlaying = this.musicQueue.get(this.musicIndex);
 				}
 			
 				// Play the next track
 				if(this.nowPlaying.play(this.nowPlayingScreen) ) {		
 					this.playRate = PlayRate.NORMAL;
 					this.nowPlayingScreen.update(this.nowPlaying);
 				}
 				else {
 					this.playNext();
 				}
 			}
 		}
 		
 		public String getNextTrackInfo() {
 			int nextIndex;
 			Playable nextTrack;
 			
 			List<Playable> currentQueue;
 			if (this.shuffleMode)
 				currentQueue = this.shuffledMusicQueue;
 			else
 				currentQueue = this.musicQueue;
 			
 			if(! currentQueue.isEmpty() ){
 				
 				// See if the last track in the music queue has been reached
 				if( this.musicIndex >= (currentQueue.size() - 1) ) {
 					// We have reached the last track
 					if(this.repeatMode) {
 						// We are in repeat mode, so get info for the first index
 						nextIndex = 0;
 					}
 					else {
 						// not in repeat mode, so there is is no next track. Return.
 						return "";
 					}
 				}
 				else {
 					// We still haven't reached the last track, so check the next index
 					nextIndex = this.musicIndex + 1;
 				}
 				
 				nextTrack = currentQueue.get(nextIndex);
 				
 				// return the title of the next track to be played
 				return nextTrack.getTrackName() + " - " + nextTrack.getArtistName();
 			}
 			else{
 				return "";
 			}
 		}
 		
 		public void playPrevious() {
 
 			if(! this.musicQueue.isEmpty() ) {
 
 				List<Playable> currentQueue;
 				if (this.shuffleMode)
 					currentQueue = this.shuffledMusicQueue;
 				else
 					currentQueue = this.musicQueue;
 
 				// Stop any track that might be playing
 				if(this.nowPlaying != null) {
 					this.nowPlaying.stop(this.nowPlayingScreen);
 				}
 				
 				// See if at least 2 seconds have elapsed.  If not,
 				// we're going to restart the current song rather
 				// than playing the previous song.
 				if ( this.getNowPlayingScreen().getSecondsElapsed() <= BACK_UP_AFTER_SECONDS ) {
 					
 					// We're going to back up a track.
 
 					// See if we are on the first track of the playlist
 					if( this.musicIndex <= 0 ) {
 						// We are on the first track of the playlist
 						if(this.repeatMode) {
 							// Reset to the first track if we are in repeat mode
 							this.musicIndex = currentQueue.size() - 1;
 						}
 						else {
 							// not in repeat mode, so stop playing music and exit
 							this.stop();		
 							return;
 						}
 					}
 					else {
 						// We're not on the first track, so just go back one
 						--this.musicIndex;
 					}
 				}
 
 				// Play the track
 				this.nowPlaying = currentQueue.get(this.musicIndex);
 				
 				if( this.nowPlaying.play(this.nowPlayingScreen) ) {
 					this.playRate = PlayRate.NORMAL;
 					this.nowPlayingScreen.update(this.nowPlaying);
 				}
 				else
 					this.playPrevious();
 			}
 		}
 		
 		public void playItemInQueue(Playable playItem) throws Exception {
 			
 			int index;
 			if (this.shuffleMode)
 				index = this.shuffledMusicQueue.indexOf(playItem);
 			else
 				index = this.musicQueue.indexOf(playItem);
 
 			if (index < 0)
 				throw new Exception("Item not in current queue.");
 
 			this.musicIndex = index;
 			this.nowPlaying = playItem;
 
 			if( this.nowPlaying.play(this.nowPlayingScreen) ) {
 				this.playRate = PlayRate.NORMAL;
 				this.nowPlayingScreen.update(this.nowPlaying);
 			}
 			else
 				this.playPrevious();
 		}
 		
 		public void stop() {
 			if(this.nowPlaying != null) {
 				if( this.nowPlaying.stop(this.nowPlayingScreen) ) {
 					this.playRate = PlayRate.STOP;	
 					this.nowPlaying = null;
 				}
 			}
 		}
 		
 		public void togglePause() {
 			if(this.nowPlaying != null) {
 				if( this.playRate.equals(PlayRate.PAUSE) ) {
 					if( this.nowPlaying.unpause(this.nowPlayingScreen) ) {
 						this.playRate = PlayRate.NORMAL;
 					}
 				}
 				else {
 					if( this.nowPlaying.pause(this.nowPlayingScreen) ) {
 						this.playRate = PlayRate.PAUSE;
 					}
 				}
 			}
 		}
 		
 		/**
 		 * fastforward the music stream
 		 */
 		public void fastForward() {
 			if( (this.nowPlaying != null) && this.nowPlaying.setPlayRate(this.nowPlayingScreen, this.playRate.getNextFF().getSpeed() ) ) {
 				this.playRate = this.playRate.getNextFF();
 			}
 		}
 		
 		/**
 		 * rewind the music stream
 		 */
 		public void rewind() {
 			if( (this.nowPlaying != null) && this.nowPlaying.setPlayRate(this.nowPlayingScreen, this.playRate.getNextREW().getSpeed() ) ) {
 				this.playRate = this.playRate.getNextREW();
 			}
 		}
 		
 		/**
 		 * play track at normal speed
 		 *
 		 */
 		public void playNormalSpeed() {
 			if( (this.nowPlaying != null) && this.nowPlaying.setPlayRate(this.nowPlayingScreen, PlayRate.NORMAL.getSpeed() ) ) {
 				this.playRate = PlayRate.NORMAL;
 			}
 		}
 		
 		
 		/**
 		 * @return the playRate
 		 */
 		public PlayRate getPlayRate() {
 			return this.playRate;
 		}
 
 		public Playable getNowPlaying() {
 			return this.nowPlaying;
 		}
 		
 		public List<Playable> getCurrentPlaylist() {
 			return this.musicQueue;
 		}
 		
 		public boolean hasCurrentPlaylist() {
 			return this.musicQueue != null && this.musicQueue.size() > 0;
 		}
 		
 		public boolean isPlaying() {
 			if(this.nowPlaying != null) {
 				return true;
 			}
 			else {
 				return false;
 			}
 		}
 
 		/**
 		 * @return the repeatMode
 		 */
 		public boolean isRepeating() {
 			return repeatMode;
 		}
 
 		/**
 		 * @param repeatMode the repeatMode to set
 		 */
 		public void setRepeatMode(boolean repeatMode) {
 			this.repeatMode = repeatMode;
 		}
 
 		/**
 		 * @return the shuffleMode
 		 */
 		public boolean isShuffling() {
 			return shuffleMode;
 		}
 
 		/**
 		 * Toggles the shuffle mode of the playlist.
 		 */
 		public void toggleShuffleMode() {
 			this.shuffleMode = ! this.shuffleMode;
 
 			if (this.shuffleMode)
 				this.musicIndex = this.shuffledMusicQueue.indexOf(this.nowPlaying);
 			else
 				this.musicIndex = getNowPlayingIndex();
 			
 			if(this.nowPlayingScreen != null) {
 				this.nowPlayingScreen.updateShuffle();
 			}
 		}
 		
 		// Returns the index of the currently playing song in the non-shuffle queue.
 		public int getNowPlayingIndex() {
 			return this.musicQueue.indexOf(this.nowPlaying);
 		}
 		
 		/**
 		 * Toggles the repeat mode of the playlist.
 		 */
 		public void toggleRepeatMode() {
 			this.repeatMode = ! this.repeatMode;
 			if(this.nowPlayingScreen != null) {
 				this.nowPlayingScreen.updateRepeat();
 			}
 		}
 		
 		/**
 		 * @return if the currently playing track is the last in the playlist
 		 */
 		public boolean isAtEndOfPlaylist()
 		{
 			if( this.musicIndex >= (this.musicQueue.size() - 1) ) {
 				return true;
 			}
 			else {
 				return false;
 			}
 		}
 		
 		/**
 		 * @return if the currently playing track is the first in the playlist
 		 */
 		public boolean isAtBeginningOfPlaylist()
 		{
 			if(this.musicIndex <= 0) {
 				return true;
 			}
 			else {
 				return false;
 			}
 		}
 		
 		/**
 		 * 
 		 * @return the Now Playing Screen.
 		 */
 		public NowPlayingScreen getNowPlayingScreen()
 		{
 			return this.nowPlayingScreen;
 		}
 		
 	}
 }
