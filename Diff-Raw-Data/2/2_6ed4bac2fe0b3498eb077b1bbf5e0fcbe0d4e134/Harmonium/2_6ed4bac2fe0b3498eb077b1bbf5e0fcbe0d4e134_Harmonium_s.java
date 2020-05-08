 package org.dazeend.harmonium;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Hashtable;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.blinkenlights.jid3.ID3Exception;
 import org.dazeend.harmonium.music.ArtSource;
 import org.dazeend.harmonium.music.EditablePlaylist;
 import org.dazeend.harmonium.music.MP3File;
 import org.dazeend.harmonium.music.MusicCollection;
 import org.dazeend.harmonium.music.Playable;
 import org.dazeend.harmonium.music.PlayableCollection;
 import org.dazeend.harmonium.music.PlayableRateChangeable;
 import org.dazeend.harmonium.music.PlayableTrack;
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
 import com.tivo.hme.sdk.ImageResource;
 import com.tivo.hme.sdk.Resource;
 
 public class Harmonium extends HDApplication {
 	
 	// public hSkin used to hold common text attributes
 	public HSkin hSkin;
 	
 	// The context for this application
 	private IContext context;
 	
 	// Application preferences
 	private ApplicationPreferences preferences;
 	
 	// This application's DiscJockey
 	private DiscJockey discJockey = DiscJockey.getDiscJockey(this);
 	
 	// Tracks user inactivity for displaying the Now Playing screen and the screensaver.
 	private InactivityHandler inactivityHandler;
 	
 	private AlbumArtCache albumArtCache = AlbumArtCache.getAlbumArtCache(this);
 	
 	private ScreenSaverScreen screenSaverScreen;
 	
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
 
 	public FactoryPreferences getFactoryPreferences() {
 		return ((HarmoniumFactory)(this.getFactory())).getPreferences();
 	}
 	
 	/**
 	 * @return the inSimulator
 	 */
 	public boolean isInSimulator() {
 		return inSimulator;
 	}
 	
 	public boolean isInDebugMode() {
 		return getHFactory().getPreferences().inDebugMode();
 	}
 
 	public final boolean ignoreEmbeddedArt()
 	{
 		return getHFactory().getPreferences().ignoreEmbeddedArt();
 	}
 
 	public final boolean ignoreJpgFileArt()
 	{
 		return getHFactory().getPreferences().ignoreJpgFileArt();
 	}
 
 	public final boolean preferJpgFileArt()
 	{
 		return getHFactory().getPreferences().preferJpgFileArt();
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
 		if (inactivityHandler != null)
 			inactivityHandler.resetInactivityTimer();
 	}
 	
 	public void updateScreenSaverDelay()
 	{
 		inactivityHandler.updateScreenSaverDelay();
 	}
 
 	public AlbumArtCache getAlbumArtCache() {
 		return albumArtCache;
 	}
 	
 	public ScreenSaverScreen getScreenSaverScreen() {
 		return screenSaverScreen;
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
 
 			if (this.app.isInDebugMode()){
 				System.out.println("INACTIVITY DEBUG: Tivo sent idle event.");
 				System.out.flush();
 			}
 			
 			inactivityHandler.checkIfInactive();
 
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
 		
 		// Now that the screen has been popped, it's safe to clean up its resources.
 		// If it's a screen that knows how to clean up after itself, ask it to do so.
 		if (currentScreen instanceof HManagedResourceScreen)
 			((HManagedResourceScreen)currentScreen).cleanup();
 	}
 
 	/**
 	 * Server side factory.
 	 */
 	public static class HarmoniumFactory extends Factory {
 		
		private final static String VERSION = "0.8 (e76845a6968b)";
 
 		private FactoryPreferences preferences;
 		private final Hashtable<String, Long> _durationTable = new Hashtable<String, Long>();
 
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
 
     	private void addTrackDuration(String uri, long duration)
     	{
     		_durationTable.put(uri, duration);
     	}
     	
         @Override
 		protected long getMP3Duration(String uri)
 		{
         	return _durationTable.remove(uri);
 		}
 
 	    /* (non-Javadoc)
          * @see com.tivo.hme.sdk.MP3Factory#getMP3StreamFromURI(java.lang.String)
          */
 		public InputStream getStream(String uri) throws IOException 
         {
 			String lowerUri = uri.toLowerCase();
 			if (lowerUri.startsWith("http://"))
 			{
 				System.out.println("Fetching MP3 stream for playback: " + uri);
 				
 	            URL url = new URL(uri);
 	            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 	            conn.setInstanceFollowRedirects(true);
 	            InputStream x = conn.getInputStream();
 	            return new BufferedInputStream(x, 102400);
 			}
 			else if (lowerUri.endsWith(".mp3"))
 			{
 	            File file = new File(MusicCollection.getMusicCollection(this).getMusicRoot(), URLDecoder.decode(uri, "UTF-8"));
 	            if (file.exists()) 
 	            {
 					System.out.println("Fetching MP3 file for playback: " + uri);
 
 					try
 					{
 		            	MP3File mp3file = new MP3File(file.getPath(), file);
 		            	addTrackDuration(uri, mp3file.getDuration());
 					} 
 					catch (ID3Exception e)
 					{
 						e.printStackTrace();
 					}
 
 		            InputStream in = new FileInputStream(file);
 	                return in;
 	            }
 			}
 
 			return super.getStream(uri);
         }
 	}
 
 	public static class DiscJockey 
 	{
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
 		private DiscJockey(Harmonium app) 
 		{
 			super();
 			this.app = app;
 		}
 
 		public synchronized static DiscJockey getDiscJockey(Harmonium app) 
 		{
 			if(app.getDiscJockey() == null) {
 				DiscJockey dj = new DiscJockey(app);
 				return dj;
 			}
 			else {
 				return app.getDiscJockey();
 			}
 		}
 		
 		public void play(List<PlayableCollection> playlist, Boolean shuffleMode, Boolean repeatMode) 
 		{
 			play(playlist, shuffleMode, repeatMode, null);
 		}
 		
 		public void play(List<PlayableCollection> playlist, Boolean shuffleMode, Boolean repeatMode, Playable startPlaying) 
 		{
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
 				for(PlayableCollection musicItem : playlist) {
 					this.musicQueue.addAll( musicItem.getMembers(this.app) );
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
 		
 		public void enqueueNext(PlayableCollection ple) {
 
 			int nextIndex = this.musicIndex + 1;
 			List<? extends Playable> list = ple.getMembers(this.app);
 			this.shuffledMusicQueue.addAll(nextIndex, list);
 			this.musicQueue.addAll(nextIndex, list);
 
 			//We need to updateNext here just incase the playlist only had one song in it
 			this.nowPlayingScreen.updateNext();
 
 			pushNowPlayingScreen();
 		}
 		
 		public void enqueueAtEnd(PlayableCollection ple) {
 
 			List<? extends Playable> list = ple.getMembers(this.app);
 			this.shuffledMusicQueue.addAll(list);
 			//After we add items to a shuffled queue, the list should be randomized again
 			Collections.shuffle(this.shuffledMusicQueue);
 			//this.musicIndex is updated when we toggle shuffleMode so we only need to update 
 			//the index if we are currently in shuffle mode
 			if(this.shuffleMode)
 				this.musicIndex = this.shuffledMusicQueue.indexOf(this.nowPlaying);
 			this.musicQueue.addAll(list);
 
 			//We need to updateNext here just incase the playlist only had one song in it
 			this.nowPlayingScreen.updateNext();
 
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
 						Collections.shuffle(this.shuffledMusicQueue);
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
 		
 		public String getNextTrackInfo() 
 		{
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
 				if (nextTrack instanceof PlayableTrack)
 				{
 					PlayableTrack pt = (PlayableTrack)nextTrack;
 					return pt.getTrackName() + " - " + pt.getArtistName();
 				}
 			}
 
 			return "";
 			
 		}
 		
 		public void playPrevious() 
 		{
 			if( !this.musicQueue.isEmpty() ) 
 			{
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
 		public boolean fastForward() 
 		{
 			if( this.nowPlaying != null && this.nowPlaying instanceof PlayableRateChangeable )
 			{
 				PlayableRateChangeable prc = (PlayableRateChangeable)this.nowPlaying;
 				if (prc.setPlayRate(this.nowPlayingScreen, this.playRate.getNextFF().getSpeed())) 
 				{
 					this.playRate = this.playRate.getNextFF();
 					return true;
 				}
 			}
 			return false;
 		}
 		
 		/**
 		 * rewind the music stream
 		 */
 		public boolean rewind() 
 		{
 			if(this.nowPlaying != null && this.nowPlaying instanceof PlayableRateChangeable) 
 			{
 				PlayableRateChangeable prc = (PlayableRateChangeable)this.nowPlaying;
 				if (prc.setPlayRate(this.nowPlayingScreen, this.playRate.getNextREW().getSpeed()))
 				{
 					this.playRate = this.playRate.getNextREW();
 					return true;
 				}
 			}
 			return false;
 		}
 		
 		/**
 		 * play track at normal speed
 		 *
 		 */
 		public boolean playNormalSpeed() 
 		{
 			if(this.nowPlaying != null && this.nowPlaying instanceof PlayableRateChangeable) 
 			{
 				PlayableRateChangeable prc = (PlayableRateChangeable)this.nowPlaying;
 				if (prc.setPlayRate(this.nowPlayingScreen, PlayRate.NORMAL.getSpeed()))
 				{
 					this.playRate = PlayRate.NORMAL;
 					return true;
 				}
 			}
 			return false;
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
 		
 		public EditablePlaylist getCurrentPlaylist() {
 			if (this.shuffleMode)
 				return new CurrentPlaylist(this, shuffledMusicQueue, musicQueue);
 			else
 				return new CurrentPlaylist(this, musicQueue, shuffledMusicQueue);
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
 			{
 				Collections.shuffle(this.shuffledMusicQueue);
 				this.musicIndex = this.shuffledMusicQueue.indexOf(this.nowPlaying);
 			}
 			else
 				this.musicIndex = getNowPlayingIndex();
 			
 			if(this.nowPlayingScreen != null) {
 				this.nowPlayingScreen.updateShuffle();
 			}
 		}
 		
 		// Returns the index of the currently playing song in the non-shuffle queue.
 		public int getNowPlayingIndex() {
 			if (this.shuffleMode)
 				return this.shuffledMusicQueue.indexOf(this.nowPlaying);
 			else
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
 		
 		public class CurrentPlaylist extends EditablePlaylist {
 
 			private DiscJockey _dj;
 			private List<Playable> _otherTracks;
 			
 			private CurrentPlaylist(DiscJockey dj, List<Playable> currentTracks, List<Playable> otherTracks) {
 				_dj = dj;
 				_tracks = currentTracks;
 				_otherTracks = otherTracks;
 			}
 			
 			public List<Playable> getMembers(Harmonium app)
 			{
 				return _tracks;
 			}
 
 			public String toStringTitleSortForm()
 			{
 				return toString();
 			}
 			
 			public String toString() {
 				return "\"Now Playing\" Playlist";
 			}
 
 			@Override
 			public Playable remove(int i) throws IllegalArgumentException
 			{
 				// You can't remove the song that's currently playing.
 				if (_dj.musicIndex == i)
 				{
 					return null;
 				}
 				
 				// Also remove the track from the shuffled/non-shuffled queue;
 				// whichever's not currently playing.
 				Playable removedTrack = super.remove(i);
 				int j = _otherTracks.indexOf(removedTrack);
 				_otherTracks.remove(j);
 				
 				return removedTrack;
 			}
 			
 			@Override
 			public void save() throws IOException
 			{
 				Playable nowPlaying = _dj.getNowPlaying();
 				int newIndex = _tracks.indexOf(nowPlaying);
 				_dj.musicIndex = newIndex;
 				_dj.nowPlaying = _tracks.get(newIndex);
 
 				if (_dj.isShuffling())
 				{
 					_dj.shuffledMusicQueue = _tracks;
 					_dj.musicQueue = _otherTracks;
 				}
 				else
 				{
 					_dj.musicQueue = _tracks;
 					_dj.shuffledMusicQueue = _otherTracks;
 				}
 				
 				_dj.nowPlayingScreen.update(_dj.nowPlaying);
 			}
 		}
 	}
 
 	public static class AlbumArtCache {
 		
 		private static final int CACHE_SIZE = 15;
 		
 		private class ArtCacheItem {
 			private int _hash;
 			private ImageResource _resource;
 			
 			public ArtCacheItem(int hash, ImageResource resource) {
 				_hash = hash;
 				_resource = resource;
 			}
 			
 			public int getHash() { return _hash; }
 			public ImageResource getResource() { return _resource; }
 		}
 		
 		private Harmonium _app;
 		private LinkedList<ArtCacheItem> _managedImageList;
 		private Hashtable<Integer, ArtCacheItem> _managedImageHashtable;
 		private Hashtable<Resource, Boolean> _managedResourceHashtable;
 				
 		// Disallow instantation by another class.
 		private AlbumArtCache(Harmonium app) {
 			_app = app;
 			_managedImageList = new LinkedList<ArtCacheItem>();
 			_managedImageHashtable = new Hashtable<Integer, ArtCacheItem>(CACHE_SIZE);
 			_managedResourceHashtable = new Hashtable<Resource, Boolean>(CACHE_SIZE);
 		}
 		
 		public synchronized static AlbumArtCache getAlbumArtCache(Harmonium app) {
 			AlbumArtCache cache = app.getAlbumArtCache(); 
 			if (app.getAlbumArtCache() == null)
 				cache = new AlbumArtCache(app);
 			return cache;
 		}
 		
 		public synchronized ImageResource Add(BScreen screen, ArtSource artSource, int width, int height) {
 			
 			// Hash a bunch of album attributes together to uniquely identify the album.
 			//
 			// The first version of this actually hashed the bytes that make up the image, but that was really
 			// slow, and it didn't save the image scaling step because images of different sizes all need to be 
 			// separately cached.  In hindsight, this was a major waste of time I should have predicted.  :)  
 			// Anyway this hash is imperfect, but dead simple and therefore pretty doggone fast.  If you
 			// have different cover art images embedded in files of the same album, we'll always display
 			// the first one we cache.  But I think that's probably unusual.  And I like how fast this is.
 			int hash = 0;
 			if (artSource.hasAlbumArt(_app.getFactoryPreferences()))
 				hash = (artSource.getArtHashKey() + width + height).hashCode();
 					
 			ArtCacheItem aci = _managedImageHashtable.get(hash);
 			if (aci == null) {
 				if (_app.isInSimulator()) {
 					System.out.println("album art cache miss: " + hash);
 
 					try {
 						Thread.sleep(500); // better simulate performance of real Tivo.
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				}
 
 				if (hash != 0)
 				{
 					// When the receiver chokes on album art for some reason, this is where we die.  But no exception is thrown,
 					// Application closes itself.  And changing this doesn't seem to be an improvement.  The failure leaves
 					// things in a weird state such that subsequent songs fail to play, etc.  I think it's better to let it 
 					// fail in a deterministic way, so at least it's relatively easy to identify the offending music.
 					aci = new ArtCacheItem(hash, screen.createImage(artSource.getScaledAlbumArt(_app.getFactoryPreferences(), width, height)));
 				}
 				else
 					aci = new ArtCacheItem(hash, screen.createImage("default_album_art2.png"));
 				
 				if (_managedImageList.size() == CACHE_SIZE) {
 
 					// We're going to add this image resource to the cache, but the cache is at its maximum size.
 					// So we remove the item at the end of the list, which is the "stalest" item: the one accessed  
 					// the longest ago.
 
 					ArtCacheItem removeItem = _managedImageList.removeLast();
 					Resource removeResource = removeItem.getResource();
 					_managedImageHashtable.remove(removeItem.getHash());
 					_managedResourceHashtable.remove(removeResource);
 					removeResource.remove();
 				}
 				
 				// Add this image resource to the front of the list, signifying that it's the most recently accessed.
 				_managedImageList.addFirst(aci);
 				_managedImageHashtable.put(hash, aci);
 				_managedResourceHashtable.put(aci.getResource(), false);
 			}
 			else {
 				if (_app.isInSimulator())
 					System.out.println("album art cache hit: " + hash);
 				
 				// Found the item in the cache.  Move it to the front of the list to indicate it's the most recently accessed.
 				_managedImageList.remove(aci);
 				_managedImageList.addFirst(aci);
 			}
 			return aci.getResource();
 		}
 		
 		public Boolean Contains(Resource r) {
 			return _managedResourceHashtable.containsKey(r);
 		}
 	}
 
 }
