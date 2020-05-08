 package org.dazeend.harmonium;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import javazoom.spi.mpeg.sampled.file.tag.MP3Tag;
 import javazoom.spi.mpeg.sampled.file.tag.TagParseEvent;
 import javazoom.spi.mpeg.sampled.file.tag.TagParseListener;
 
 import org.dazeend.harmonium.music.ArtSource;
 import org.dazeend.harmonium.music.EditablePlaylist;
 import org.dazeend.harmonium.music.MP3Stream;
 import org.dazeend.harmonium.music.Playable;
 import org.dazeend.harmonium.music.PlayableCollection;
 import org.dazeend.harmonium.music.PlayableLocalTrack;
 import org.dazeend.harmonium.music.PlayableTrack;
 import org.dazeend.harmonium.music.StreamTrackData;
 import org.dazeend.harmonium.screens.ScreenSaverScreen;
 
 import com.tivo.hme.sdk.HmeEvent;
 import com.tivo.hme.sdk.StreamResource;
 import com.tivo.hme.sdk.View;
 
 public class DiscJockey extends View implements TagParseListener
 {
 	public static final int BACK_UP_AFTER_SECONDS = 2;
 	
 	private Harmonium app;
 	private final List<DiscJockeyListener> _listeners = new ArrayList<DiscJockeyListener>(2);
 	
 	private Playable nowPlaying;	// the currently playing track
 	private StreamResource nowPlayingResource;
 	private PlayRate playRate = PlayRate.STOP;
 	private long msElapsed; // milliseconds elapsed in current song
 	
 	private List<Playable> musicQueue = new ArrayList<Playable>();
 	private List<Playable> shuffledMusicQueue = new ArrayList<Playable>();
 	private int musicIndex;			// index of currently playing track
 
 	private boolean shuffleMode = false;	// true if play list is being played in shuffle mode, otherwise false.
 	private boolean repeatMode = false;		// true if playlist should start over when end is reached
 	
 	private final StreamTrackData _streamTrackData; // Used for displaying art and other track info for mp3 streams.
 	
 	private DiscJockey(Harmonium app) 
 	{
 		super(app.getRoot(), 1, 1, 1, 1, false);
 		this.app = app;
 		_streamTrackData = new StreamTrackData(app);
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
 	
 	public void addListener(DiscJockeyListener listener)
 	{
 		_listeners.add(listener);
 	}
 
 	/**
 	 * Plays an MP3. 
 	 * 
 	 * @param mp3File
 	 */
 	private boolean play(Playable p) {
 	
 		// Stop any track that might be playing
 		stop();
 		
 		// Make sure that the file exists on disk and hasn't been deleted
 		if (p instanceof PlayableLocalTrack)
 		{
 			PlayableLocalTrack plt = (PlayableLocalTrack)p;
 			if( ( plt.getTrackFile() == null ) || ( !plt.getTrackFile().exists() ) )
 				return false;
 		}
 	
 		this.nowPlaying = p;
 		this.playRate = PlayRate.NORMAL;
 		
 		nowPlayingChanged(p);
 		
 		//
 	    // Construct the URI to send to the receiver. The receiver will
 	    // connect back to our factory and ask for the file. The URI
 	    // consists of:
 	    //
 	    // (our base URI) + (the Playable's URI)
 	    //
 		
 		String url = this.getApp().getContext().getBaseURI().toString();
 	    try {
 	        url += URLEncoder.encode(p.getURI(), "UTF-8");
 	    } catch (UnsupportedEncodingException e) {
 	        e.printStackTrace();
 	    }
 	
 	    // MP3's are played as a streamed resource   
 	    this.app.setLastRequestedStream(p.getURI());
 	    this.nowPlayingResource = this.createStream(url, p.getContentType(), true);
 	    this.setResource(this.nowPlayingResource); 
 	    
 	    return true;
 	}
 
 	private void nowPlayingChanged(Playable p)
 	{
 		PlayableTrack pt = null;
 		if (p instanceof PlayableTrack)
 			pt = (PlayableTrack)p;
 		
 		for (DiscJockeyListener listener : _listeners)
 		{
 			try
 			{
 				listener.beginUpdate();
 				
 				listener.artChanged(p);
 
 				if (pt != null)
 				{
 					listener.trackNameChanged(pt.getTrackName());
 					listener.trackArtistChanged(pt.getArtistName());
 					listener.albumChanged(pt.getAlbumName(), pt.getDiscNumber());
 				}
 				else
 				{
 					listener.trackNameChanged(p.getURI());
 					listener.trackArtistChanged(null);
 					listener.albumChanged(p.getAlbumName(), 0);
 				}
 				
 				listener.albumArtistChanged(p.getAlbumArtistName());
 				listener.releaseYearChanged(p.getReleaseYear());
 				
 				listener.nextTrackChanged(getNextTrack());
 	
 				listener.shuffleChanged(shuffleMode);
 				listener.repeatChanged(repeatMode);
 			}
 			finally
 			{
 				listener.endUpdate();
 			}
 		}
 	}
 
 	public void streamTrackDataChanged(ArtSource artSource, String albumArtist, String trackArtist, String albumName, String trackName, int releaseYear)
 	{
 		for (DiscJockeyListener listener : _listeners)
 		{
 			try
 			{
 				listener.beginUpdate();
 				
 				if (artSource != null)
 					listener.artChanged(artSource);
 
 				listener.albumArtistChanged(albumArtist);
 				listener.trackArtistChanged(trackArtist);
 				listener.albumChanged(albumName, 0);
 				listener.trackNameChanged(trackName);
 				listener.releaseYearChanged(releaseYear);
 			}
 			finally
 			{
 				listener.endUpdate();
 			}
 		}
 	}
 	
 	public void play(List<PlayableCollection> playlist, Boolean shuffleMode, Boolean repeatMode) 
 	{
 		play(playlist, shuffleMode, repeatMode, null);
 	}
 	
 	public void play(List<PlayableCollection> playlist, Boolean shuffleMode, Boolean repeatMode, Playable startPlaying) 
 	{
 		// Only do stuff if the playlist is not empty
 		if( playlist != null && !playlist.isEmpty() ) 
 		{
 			
 			this.shuffleMode = shuffleMode;
 			this.repeatMode = repeatMode;
 			
 			// If music is playing, stop it before changing the active playlist.
 			if(this.nowPlaying != null)
 				this.stop();
 			
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
 			
 			// TODO Try starting playback before pushing screen.  
 			//      Does it help with the beginning of songs being cut off? 
 			
 			app.pushNowPlayingScreen();
 			
 			// Start playing music
 			if( this.play(this.nowPlaying) )
 				this.playRate = PlayRate.NORMAL;
 			else
 				this.playNext();
 		}
 		else {
 			// Bonk if we attempt to play an empty playlist.
 			this.app.play("bonk.snd");
 		}
 		
 	}
 	
 	public void enqueueNext(PlayableCollection ple) {
 
 		int nextIndex = this.musicIndex + 1;
 		List<? extends Playable> list = ple.getMembers(this.app);
 		this.shuffledMusicQueue.addAll(nextIndex, list);
 		this.musicQueue.addAll(nextIndex, list);
 
 		//We need to updateNext here just incase the playlist only had one song in it
 		for (DiscJockeyListener listener : _listeners)
 			listener.nextTrackChanged(this.getNextTrack());
 
 		app.pushNowPlayingScreen();
 	}
 	
 	public void enqueueAtEnd(PlayableCollection ple) 
 	{
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
 		for (DiscJockeyListener listener : _listeners)
 			listener.nextTrackChanged(this.getNextTrack());
 
 		app.pushNowPlayingScreen();
 	}
 	
 	public boolean playNext() 
 	{
 		if( !this.musicQueue.isEmpty() )
 		{
 			// See if the last track in the music queue has been reached
 			if( this.musicIndex >= (this.musicQueue.size() - 1) ) 
 			{
 				// We have reached the last track
 				if(this.repeatMode) 
 				{
 					// Reset to the first track if we are in repeat mode
 					Collections.shuffle(this.shuffledMusicQueue);
 					this.musicIndex = 0;
 				}
 				else 
 				{
 					// not in repeat mode; do nothing and return false				
 					return false;
 				}
 			}
 			else 
 			{
 				// We still haven't reached the last track, so go to the next one
 				++this.musicIndex;
 			}
 
 			// Get the next track
 			if(this.shuffleMode)
 				this.nowPlaying = this.shuffledMusicQueue.get(this.musicIndex);
 			else
 				this.nowPlaying = this.musicQueue.get(this.musicIndex);
 		
 			// Play the next track
 			if( !this.play(nowPlaying) ) 
 				return this.playNext();
 			
 			return true;
 		}
 		return false;
 	}
 	
 	public PlayableTrack getNextTrack() 
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
 					return null;
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
 				return pt;
 			}
 		}
 
 		return null;
 		
 	}
 	
 	public boolean playPrevious() 
 	{
 		if( !this.musicQueue.isEmpty() ) 
 		{
 			List<Playable> currentQueue;
 			if (this.shuffleMode)
 				currentQueue = this.shuffledMusicQueue;
 			else
 				currentQueue = this.musicQueue;
 
 			// See if at least 2 seconds have elapsed.  If not,
 			// we're going to restart the current song rather
 			// than playing the previous song.
 			if ( this.getSecondsElapsed() <= BACK_UP_AFTER_SECONDS ) {
 				
 				// We're going to back up a track.
 
 				// See if we are on the first track of the playlist
 				if( this.musicIndex <= 0 ) {
 					// We are on the first track of the playlist
 					if(this.repeatMode) {
 						// Reset to the first track if we are in repeat mode
 						this.musicIndex = currentQueue.size() - 1;
 					}
 					else {
 						// not in repeat mode, so do nothing and return false
 						return false;
 					}
 				}
 				else {
 					// We're not on the first track, so just go back one
 					--this.musicIndex;
 				}
 			}
 
 			// Play the track
 			this.nowPlaying = currentQueue.get(this.musicIndex);
 			if (!play(nowPlaying))
 				return this.playPrevious();
 
 			return true;
 		}
 		return false;
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
 
 		if (!play(nowPlaying))
 			playPrevious();
 	}
 		
 	public void stop() 
 	{
 		if (this.app.isInSimulator())
 			System.out.println("Stopping playback");
 
 		this.playRate = PlayRate.STOP;	
 		this.nowPlaying = null;
 
 		if(this.nowPlayingResource != null) 
 		{
 			// Close the stream playing the MP3
 			this.nowPlayingResource.close();
 			this.nowPlayingResource.remove();
 		
 			// Re-set the music stream
 			this.nowPlayingResource = null;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.dazeend.harmonium.Playable#pause()
 	 */
 	//@Override
 	public boolean pause()
 	{
 		if (nowPlayingResource != null)
 		{
 			if (!nowPlayingResource.isPaused())
 				nowPlayingResource.pause();
 			return true;
 		}
 		return false;
 	}
 
 	public synchronized boolean togglePause() 
 	{
 		if(this.nowPlayingResource != null) 
 		{
 			if( this.playRate.equals(PlayRate.PAUSE) )
 			{
 				nowPlayingResource.play();
 				playRate = PlayRate.NORMAL;
 			}
 			else
 			{
 				nowPlayingResource.pause();
 				playRate = PlayRate.PAUSE;
 			}
 			return true;
 		}
 		return false;
 	}
 	
 	private synchronized boolean setPlayRate(PlayRate newPlayRate) 
 	{
 		if( this.nowPlaying != null && this.nowPlaying instanceof PlayableLocalTrack )
 		{
 			if (newPlayRate != this.playRate)
 			{
 				for (DiscJockeyListener listener : _listeners)
 					listener.playRateChanging(newPlayRate);
 
 				this.playRate = newPlayRate;
 				nowPlayingResource.setSpeed(newPlayRate.getSpeed());
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private double handleElapsedChanged(HmeEvent.ResourceInfo resourceInfo)
 	{
 		if (this.nowPlaying == null)
 			return 0;
 		
 		long duration = this.nowPlaying.getDuration();  // TODO save duration as field?
 		
 		String[] positionInfo = resourceInfo.getMap().get("pos").toString().split("/");
 		msElapsed = Long.parseLong(positionInfo[0]);
 	
 		double fractionComplete = (double)msElapsed / duration;
 		
 		for (DiscJockeyListener listener : _listeners)
 			listener.timeElapsedChanged(msElapsed, duration, fractionComplete);
 		
 		return fractionComplete;
 	}
 
 	public int getSecondsElapsed() 
 	{
 		return (int)(msElapsed / 1000);
 	}
 
 	/**
 	 * fastforward the music stream
 	 */
 	public boolean fastForward() 
 	{
 		return setPlayRate(this.playRate.getNextFF());
 	}
 	
 	/**
 	 * rewind the music stream
 	 */
 	public boolean rewind() 
 	{
 		return setPlayRate(this.playRate.getNextREW());	
 	}
 	
 	/**
 	 * play track at normal speed
 	 *
 	 */
 	public boolean playNormalSpeed() 
 	{
 		return setPlayRate(PlayRate.NORMAL);
 	}
 	
 	public Playable getNowPlaying() {
 		return this.nowPlaying;
 	}
 	
 	public EditablePlaylist getCurrentPlaylist() {
 		if (this.shuffleMode)
 			return new CurrentPlaylist(shuffledMusicQueue, musicQueue);
 		else
 			return new CurrentPlaylist(musicQueue, shuffledMusicQueue);
 	}
 	
 	public boolean hasCurrentPlaylist() {
 		return this.musicQueue != null && this.musicQueue.size() > 0;
 	}
 	
 	public boolean isPlaying()
 	{
 		return (this.nowPlaying != null);
 	}
 	
 	public boolean isSeeking()
 	{
 		switch (this.playRate)
 		{
 			case NORMAL:
 			case PAUSE:
 			case STOP:
 				return false;
 		}
 		return true;	
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
 	public void toggleShuffleMode() 
 	{
 		this.shuffleMode = ! this.shuffleMode;
 		
 		if (this.shuffleMode)
 		{
 			Collections.shuffle(this.shuffledMusicQueue);
 			this.musicIndex = this.shuffledMusicQueue.indexOf(this.nowPlaying);
 		}
 		else
 			this.musicIndex = getNowPlayingIndex();
 
 		for (DiscJockeyListener listener : _listeners)
 			listener.shuffleChanged(shuffleMode);
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
 	public void toggleRepeatMode() 
 	{
 		this.repeatMode = ! this.repeatMode;
 		for (DiscJockeyListener listener : _listeners)
 			listener.repeatChanged(repeatMode);
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
 	
 	@Override
 		public boolean handleEvent(HmeEvent event) 
 		{
 			// Check to see if this event is of a type that we want to handle
 			if (event.getOpCode() == EVT_RSRC_INFO)
 			{
 		    	HmeEvent.ResourceInfo resourceInfo = (HmeEvent.ResourceInfo) event;
 		    	
 		    	// Check that this event is for the music stream
 		    	if( (nowPlayingResource != null) && (event.getID() == nowPlayingResource.getID() ) ) 
 		    	{
 		        	if (this.app.getFactoryPreferences().inDebugMode() && nowPlayingResource != null)
 		        		System.out.println("Stream status: " + nowPlayingResource.getStatus());
 	
 		        	// Check the type of status sent
 		    		switch( resourceInfo.getStatus() ) 
 		    		{
 			    		case RSRC_STATUS_PLAYING:
 			    			
 			    			handleElapsedChanged(resourceInfo);
 							break;
 							
 			    		case RSRC_STATUS_SEEKING:
 			
 			    			double fractionComplete = handleElapsedChanged(resourceInfo);
 			        		
 			        		// Since we're using our custom duration rather than the one the Tivo sends in the event,
 			        		// trickplay doesn't automatically stop at the beginning or end of a track when fast forwarding
 			        		// or rewinding. Implement it.
 			        		double lowerLimit = 0;
 			        		double upperLimit = .95;
 			        		if(Float.parseFloat( resourceInfo.getMap().get("speed").toString() ) < 0  && fractionComplete <= lowerLimit) 
 			        		{
 			        			// We are rewinding and are about to hit the beginning of the track. 
 			        			// Position the track at our lower limit and drop back to NORMAL speed.
 			        			long position = (long)( this.nowPlaying.getDuration() * lowerLimit);
 			        			this.nowPlayingResource.setPosition(position);
 			        			this.playNormalSpeed();
 			        		}
 			        		if( Float.parseFloat( resourceInfo.getMap().get("speed").toString() ) > 1 && fractionComplete >= upperLimit ) 
 			        		{
 			        			// We are fast forwarding and are about to hit the end of the track. 
 			        			// Position the track at our upper limit and drop back to NORMAL speed.
 			        			long position = (long)( this.nowPlaying.getDuration() * upperLimit);
 			
 			        			this.nowPlayingResource.setPosition(position);
 			        			this.playNormalSpeed();
 			        		}
 			        		break;
 			        		
 			    		case RSRC_STATUS_CLOSED:
 			    		case RSRC_STATUS_COMPLETE:
 			    		case RSRC_STATUS_ERROR:
 			    			
 							// the current track has finished, so check if there's another track to play.
 			    			if( this.app.getDiscJockey().isAtEndOfPlaylist() && ( ! this.app.getDiscJockey().isRepeating() ) ) 
 			    			{
 			    				// There's not another track to play
 				    			stop();
 	
 			    				this.app.resetInactivityTimer();
 			    				
			    				// TODO: move the screen pops, I think
 			    				
 			    				// Pop the screen saver if it is showing
			    				if(this.app.getCurrentScreen().getClass() == ScreenSaverScreen.class)
 			    					this.app.pop();
 			    				
			    				// Pop this Now Playing Screen only if it is showing.
			    				if(this.app.getCurrentScreen().equals(this)) 
 			    					this.app.pop();
 							}
 							break;
 					}
 			    }
 			}
 	
 			boolean result = super.handleEvent(event);
 	
 			// Check to see if this event is of a type that we want to handle
 			if( this.nowPlayingResource != null && event.getID() == nowPlayingResource.getID()					
 				&& event.getClass() == HmeEvent.ResourceInfo.class)
 			{
 				// This is a ResourceInfo event which we will read for information about the status of
 				// music that is being streamed.
 				HmeEvent.ResourceInfo  resourceInfo = (HmeEvent.ResourceInfo) event;
 				
 				// Has the current track finished playing?
 				if (resourceInfo.getStatus() >= RSRC_STATUS_CLOSED) 
 				{
 					// the current track has finished, so play the next one
 					playNext();
 				}
 			}
 			
 			return result;
 		}
 
 	public class CurrentPlaylist extends EditablePlaylist {
 
 		private List<Playable> _otherTracks;
 		
 		private CurrentPlaylist(List<Playable> currentTracks, List<Playable> otherTracks) {
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
 			if (musicIndex == i)
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
 			int newIndex = _tracks.indexOf(nowPlaying);
			musicIndex = newIndex;
			nowPlaying = _tracks.get(newIndex);
 
 			if (isShuffling())
 			{
 				shuffledMusicQueue = _tracks;
 				musicQueue = _otherTracks;
 			}
 			else
 			{
 				musicQueue = _tracks;
 				shuffledMusicQueue = _otherTracks;
 			}
 			
 			for (DiscJockeyListener listener : _listeners)
 				listener.nextTrackChanged(getNextTrack());
 		}
 	}
 
 	/**
 	 * When playing icecast streams, this gets called with information about the stream.
 	 */
 	public void tagParsed(TagParseEvent tpe)
 	{
 		try
 		{
 			MP3Tag tag = tpe.getTag();
 			System.out.println("TagParseEvent: [" + tag.getName() + "][" + tag.getValue() + "]");
 			
 			// This shouldn't get called unless a stream is playing, but just in case...
 			MP3Stream nowPlayingStream = null;
 			if (nowPlaying instanceof MP3Stream)
 				nowPlayingStream = (MP3Stream)nowPlaying;
 			else
 				return;
 			
 			if (tag.getName().equalsIgnoreCase("StreamTitle"))
 			{
 				_streamTrackData.setTagParsedStreamTitle(tag.getValue().toString());
 			}
 			else if (tag.getName().equalsIgnoreCase("StreamUrl") && nowPlayingStream != null)
 			{
 				_streamTrackData.setTagParsedStreamUrl(tag.getValue().toString());
 			}
 		}
 		catch (Exception ex)
 		{
 			ex.printStackTrace();
 		}
 	}
 }
