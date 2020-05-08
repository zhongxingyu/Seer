 package org.dazeend.harmonium.screens;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 
 import org.dazeend.harmonium.HSkin;
 import org.dazeend.harmonium.Harmonium;
 import org.dazeend.harmonium.Harmonium.DiscJockey;
 import org.dazeend.harmonium.music.Playable;
 import org.dazeend.harmonium.music.PlayableLocalTrack;
 import org.dazeend.harmonium.music.PlayableTrack;
 
 import com.tivo.hme.bananas.BScreen;
 import com.tivo.hme.bananas.BText;
 import com.tivo.hme.bananas.BView;
 import com.tivo.hme.sdk.HmeEvent;
 import com.tivo.hme.sdk.ImageResource;
 import com.tivo.hme.sdk.Resource;
 import com.tivo.hme.sdk.StreamResource;
 
 public class NowPlayingScreen extends HManagedResourceScreen {
 	
 	// These are the fields that might be updated
 	private BView albumArtView;
 	private BText albumNameText;
 	private BText albumArtistText;
 	private BText yearText;
 	private BText trackNameText;
 	private BText artistNameText;
 	private BText shuffleModeText;
 	private BText repeatModeText;
 	private BText nextTrackText;
 	private ProgressBar progressBar;
 	private StreamResource musicStream;
 	private BText artistNameLabelText;
 
 
 	/**
 	 * @param app
 	 */
 	public NowPlayingScreen(Harmonium app, final Playable musicItem) {
 
 		super(app);
 		doNotFreeResourcesOnExit(); // We'll free of our own resources, using the tools HManagedResourceScreen gives us. 
 		
 		// Define all dimensions in terms of percentage of the screen height and width. This make it
 		// resolution-safe.
 		
 		//	constants used for screen layout
 		int screenWidth 	= app.getWidth();
 		int screenHeight 	= app.getHeight();
 		int safeTitleH	= app.getSafeTitleHorizontal();
 		int safeTitleV	= app.getSafeTitleVertical();
 		int hIndent = (int)( ( screenWidth - (2 * safeTitleH) ) * 0.01 );
 		int vIndent = (int)( ( screenHeight - (2 * safeTitleV) ) * 0.01 );
 		
 		// Define height of each row of album info text
 		int albumInfoRowHeight = app.hSkin.paragraphFontSize + (app.hSkin.paragraphFontSize / 4);
 		
 		// Create views for album art. Size is square, sides less of 480px or half title-safe width
 		// (640x480 is the maximum image size that TiVo can load.)
 		final int artSide = Math.min(480,(screenWidth - (2 * safeTitleH) ) / 2 );
 		
 		// Define the y-coordinate for album art so that the info is verticaly centered in the screen
 		int albumArtViewY = ( this.getHeight() - ( artSide + (3 * albumInfoRowHeight) ) ) / 2;
 		this.albumArtView = new BView( this.getNormal(), safeTitleH, albumArtViewY, artSide, artSide);
 		
 		// Add album info text
 		this.albumNameText = new BText(	this.getNormal(), 													// parent
 										this.albumArtView.getX() - safeTitleH,								// x
 										this.albumArtView.getY() + this.albumArtView.getHeight() + vIndent,	// y
 										this.albumArtView.getWidth() + (2 * safeTitleH),					// width
 										albumInfoRowHeight													// height
 		);
 		
 		// Set album info text properties
 		this.albumNameText.setColor(HSkin.PARAGRAPH_TEXT_COLOR);
 		this.albumNameText.setShadow(false);
 		this.albumNameText.setFlags(RSRC_HALIGN_CENTER);
 		this.albumNameText.setFont(app.hSkin.paragraphFont);
 		
 		// Add album artist info text
 		this.albumArtistText = new BText(	this.getNormal(), 										// parent
 											this.albumArtView.getX() - safeTitleH,					// x
 											this.albumNameText.getY() + albumNameText.getHeight(),	// y
 											this.albumArtView.getWidth() + (2 * safeTitleH),		// width
 											albumInfoRowHeight										// height
 		);
 		
 		// Set album info text properties
 		this.albumArtistText.setColor(HSkin.PARAGRAPH_TEXT_COLOR);
 		this.albumArtistText.setShadow(false);
 		this.albumArtistText.setFlags(RSRC_HALIGN_CENTER);
 		this.albumArtistText.setFont(app.hSkin.paragraphFont);
 		
 		// Add album year text
 		this.yearText = new BText(	this.getNormal(), 											// parent
 									this.albumArtView.getX(),									// x
 									this.albumArtistText.getY() + albumArtistText.getHeight(),	// y
 									this.albumArtView.getWidth(),								// width
 									albumInfoRowHeight											// height
 		);
 		
 		// Set album info text properties
 		this.yearText.setColor(HSkin.PARAGRAPH_TEXT_COLOR);
 		this.yearText.setShadow(false);
 		this.yearText.setFlags(RSRC_HALIGN_CENTER);
 		this.yearText.setFont(app.hSkin.paragraphFont);
 		
 		// Define constants to make track info layout easier
 		// NOTE: Can't be defined with other constants, because they rely on albumArtView
 		int leftEdgeCoord = this.albumArtView.getX() + this.albumArtView.getWidth() + hIndent;
 		int textWidth = this.getWidth() - leftEdgeCoord - safeTitleH;
 		int rowHeight = this.albumArtView.getHeight() / 5;
 		
 		// Add track title
 		this.trackNameText = new BText(	this.getNormal(), 			// parent
 										leftEdgeCoord,				// x coord relative to parent
 										this.albumArtView.getY(), 	// y coord relative to parent
 										textWidth,					// width
 										rowHeight					// height
 		);
 		this.trackNameText.setColor(HSkin.BAR_TEXT_COLOR);
 		this.trackNameText.setShadow(false);
 		this.trackNameText.setFlags(RSRC_HALIGN_LEFT + RSRC_VALIGN_BOTTOM + RSRC_TEXT_WRAP);
 		this.trackNameText.setFont(app.hSkin.barFont);
 		
 		// Put in track title label
 		BText trackNameLabelText = new BText(	this.getNormal(),
 												leftEdgeCoord,
 												this.albumArtView.getY() + rowHeight,
 												textWidth,
 												rowHeight
 		);
 		trackNameLabelText.setColor(HSkin.PARAGRAPH_TEXT_COLOR);
 		trackNameLabelText.setShadow(false);
 		trackNameLabelText.setFlags(RSRC_HALIGN_LEFT + RSRC_VALIGN_TOP);
 		trackNameLabelText.setFont(app.hSkin.paragraphFont);
 		trackNameLabelText.setValue("Title");
 		
 		// Add track artist
 		this.artistNameText = new BText(	this.getNormal(), 								// parent
 											leftEdgeCoord,									// x coord relative to parent
 											this.albumArtView.getY() + (2 * rowHeight), 	// y coord relative to parent
 											textWidth,										// width
 											rowHeight										// height
 		);
 		this.artistNameText.setColor(HSkin.BAR_TEXT_COLOR);
 		this.artistNameText.setShadow(false);
 		this.artistNameText.setFlags(RSRC_HALIGN_LEFT + RSRC_VALIGN_BOTTOM + RSRC_TEXT_WRAP);
 		this.artistNameText.setFont(app.hSkin.barFont);
 		
 		artistNameLabelText = new BText(this.getNormal(),
 										leftEdgeCoord,
 										this.albumArtView.getY() + (3 * rowHeight),
 										textWidth,
 										rowHeight);
 		artistNameLabelText.setColor(HSkin.PARAGRAPH_TEXT_COLOR);
 		artistNameLabelText.setShadow(false);
 		artistNameLabelText.setFlags(RSRC_HALIGN_LEFT + RSRC_VALIGN_TOP);
 		artistNameLabelText.setFont(app.hSkin.paragraphFont);
 		
 		// Create Progress Bar
 		this.progressBar = new ProgressBar(	this, 
 											leftEdgeCoord, 
 											this.albumArtView.getY() + (4 * rowHeight),
 											textWidth,
 											this.app.hSkin.paragraphFontSize
 		);
 		
 		// Create footer layout variables
 		int footerHeight = this.app.hSkin.paragraphFontSize;
 		int footerTop = screenHeight - this.app.getSafeActionVertical() - footerHeight;
 		
 		// Create all footer objects before initializing them. 
 		// Some methods we call depend on their existence.
 		BText repeatLabelText = new BText(	this.getNormal(),
 				safeTitleH,
 				footerTop,
 				3 * this.app.hSkin.paragraphFontSize,
 				footerHeight
 		);
 		this.repeatModeText = new BText(	this.getNormal(),
 				safeTitleH + repeatLabelText.getWidth(),
 				footerTop,
 				2 * this.app.hSkin.paragraphFontSize,
 				footerHeight
 		);
 		BText shuffleLabelText = new BText(	this.getNormal(),
 				this.repeatModeText.getX() + this.repeatModeText.getWidth(),
 				footerTop,
 				3 * this.app.hSkin.paragraphFontSize,
 				footerHeight
 		);
 		this.shuffleModeText = new BText(	this.getNormal(),
 				shuffleLabelText.getX() + shuffleLabelText.getWidth(),
 				footerTop,
 				2 * this.app.hSkin.paragraphFontSize,
 				footerHeight
 		);
 		BText nextLabelText = new BText(	this.getNormal(),
 				this.shuffleModeText.getX() + this.shuffleModeText.getWidth(),
 				footerTop,
 				2 * this.app.hSkin.paragraphFontSize,
 				footerHeight
 		);
 		this.nextTrackText = new BText(	this.getNormal(),
 				nextLabelText.getX() + nextLabelText.getWidth(),
 				footerTop,
 				screenWidth - safeTitleH - ( nextLabelText.getX() + nextLabelText.getWidth() ),
 				footerHeight
 		);
 		
 		// init Shuffle label
 		shuffleLabelText.setColor(HSkin.PARAGRAPH_TEXT_COLOR);
 		shuffleLabelText.setShadow(false);
 		shuffleLabelText.setFlags(RSRC_HALIGN_LEFT);
 		shuffleLabelText.setFont(app.hSkin.paragraphFont);
 		shuffleLabelText.setValue("Shuffle:");
 		
 		// init Shuffle Mode Text
 		this.shuffleModeText.setColor(HSkin.PARAGRAPH_TEXT_COLOR);
 		this.shuffleModeText.setShadow(false);
 		this.shuffleModeText.setFlags(RSRC_HALIGN_LEFT);
 		this.shuffleModeText.setFont(app.hSkin.paragraphFont);
 		this.updateShuffle();
 		
 		// init Repeat label
 		repeatLabelText.setColor(HSkin.PARAGRAPH_TEXT_COLOR);
 		repeatLabelText.setShadow(false);
 		repeatLabelText.setFlags(RSRC_HALIGN_LEFT);
 		repeatLabelText.setFont(app.hSkin.paragraphFont);
 		repeatLabelText.setValue("Repeat:");
 		
 		// init Repeat Mode Text
 		this.repeatModeText.setColor(HSkin.PARAGRAPH_TEXT_COLOR);
 		this.repeatModeText.setShadow(false);
 		this.repeatModeText.setFlags(RSRC_HALIGN_LEFT);
 		this.repeatModeText.setFont(app.hSkin.paragraphFont);
 		this.updateRepeat();
 		
 		// init next playing label
 		nextLabelText.setColor(HSkin.PARAGRAPH_TEXT_COLOR);
 		nextLabelText.setShadow(false);
 		nextLabelText.setFlags(RSRC_HALIGN_LEFT);
 		nextLabelText.setFont(app.hSkin.paragraphFont);
 		nextLabelText.setValue("Next:");
 
 		// init next track Text
 		this.nextTrackText.setColor(HSkin.PARAGRAPH_TEXT_COLOR);
 		this.nextTrackText.setShadow(false);
 		this.nextTrackText.setFlags(RSRC_HALIGN_LEFT);
 		this.nextTrackText.setFont(app.hSkin.paragraphFont);
 		this.nextTrackText.setValue( this.app.getDiscJockey().getNextTrackInfo() );
 
 		update(musicItem);
 	}
 	
 	/**
 	 * Update the screen to a new music item
 	 * 
 	 * @param nowPlaying
 	 */
 	public void update(final Playable nowPlaying) 
 	{
 		// turn off painting while we update the images
     	app.getRoot().setPainting(false);
     	
     	try 
     	{
        		// Update views with new info
     		new Thread() {
     			public void run() {
 		    		ImageResource albumArtImage = createManagedImage( nowPlaying, albumArtView.getWidth(), albumArtView.getHeight());
 		    		setManagedResource(albumArtView, albumArtImage, RSRC_HALIGN_CENTER + RSRC_VALIGN_CENTER + RSRC_IMAGE_BESTFIT);
 		    		flush(); // Necessay to ensure UI updates, because we're in another thread.
     			}
     		}.start();
 	
     		if (nowPlaying instanceof PlayableTrack)
     		{
     			artistNameLabelText.setValue("Artist");
 
     			PlayableTrack pt = (PlayableTrack)nowPlaying;
     		    if(pt.getDiscNumber() > 0) {
     				this.albumNameText.setValue(pt.getAlbumName() + " - Disc " + pt.getDiscNumber());
     			}
     			else {
     				this.albumNameText.setValue(pt.getAlbumName() );
     			}
     		    
                 this.albumArtistText.setValue(pt.getAlbumArtistName());
                 
                 if(pt.getReleaseYear() == 0) {
         			this.yearText.setValue("");
         		}
         		else {
         			this.yearText.setValue(pt.getReleaseYear());
         		}
                 
                 this.trackNameText.setValue(pt.getTrackName());
                 this.artistNameText.setValue(pt.getArtistName());
     		}
     		else
     		{
     			artistNameLabelText.setValue("");
 				
     			this.albumNameText.setValue("");
                 this.albumArtistText.setValue("");
     			this.yearText.setValue("");
                 this.trackNameText.setValue(nowPlaying.getURI());
                 this.artistNameText.setValue("");
     		}
     		
             // indicate that we need to reset the time label on the progress bar
             if(this.progressBar != null)
             	this.progressBar.setDurationUpdated(false);
 
             this.nextTrackText.setValue( this.app.getDiscJockey().getNextTrackInfo() );
             
             // update the shuffle and repeat mode indicators
             this.updateShuffle();
             this.updateRepeat();
     	}
     	finally 
     	{
     		app.getRoot().setPainting(true);
     	}
 	}
 	
 	/**
 	 * Updates the shuffle mode indicator on the Now Playing Screen
 	 */
 	public void updateShuffle() {
 		if( this.app.getDiscJockey().isShuffling() ) {
 			this.shuffleModeText.setValue("On");
 		}
 		else {
 			this.shuffleModeText.setValue("Off");
 		}
 		updateNext();
 	}
 	
 	public void updateRepeat() {
 		if( this.app.getDiscJockey().isRepeating() ) {
 			this.repeatModeText.setValue("On");
 		}
 		else {
 			this.repeatModeText.setValue("Off");
 		}
 		updateNext();
 	}
 	
 	public void updateNext() {
 		this.nextTrackText.setValue( this.app.getDiscJockey().getNextTrackInfo() );
 	}
 	
 	/**
 	 * Plays an MP3. 
 	 * 
 	 * @param mp3File
 	 */
 	public boolean play(Playable playable) {
 
 		// Make sure that there is no music stream already playing
 		if(this.musicStream != null) 
 			return false;
 		
 		// Make sure that the file exists on disk and hasn't been deleted
 		if (playable instanceof PlayableLocalTrack)
 		{
 			PlayableLocalTrack plt = (PlayableLocalTrack)playable;
 			if( ( plt.getTrackFile() == null ) || ( !plt.getTrackFile().exists() ) )
 			return false;
 		}
 		
 		//
         // Construct the URI to send to the receiver. The receiver will
         // connect back to our factory and ask for the file. The URI
         // consists of:
         //
         // (our base URI) + (the Playable's URI)
         //
 		
 		String url = this.getApp().getContext().getBaseURI().toString();
         try {
             url += URLEncoder.encode(playable.getURI(), "UTF-8");
         } catch (UnsupportedEncodingException e) {
             e.printStackTrace();
         }
  
         // MP3's are played as a streamed resource   
         this.musicStream = this.createStream(url, playable.getContentType(), true);
         this.setResource(this.musicStream); 
         
         return true;
 	}
 	
 	public void stopPlayback() 
 	{
 		if(this.musicStream != null) 
 		{
 			if (this.app.isInSimulator()) {
 				System.out.println("Stopping playback");
 			}
 			// Close the stream playing the MP3
 			this.musicStream.close();
 			this.musicStream.remove();
 		
 			// Re-set the musicStream;
 			this.musicStream = null;
 		}
 	}
 	
 	
 	/**
 	 * @return the musicStream
 	 */
 	public StreamResource getMusicStream() {
 		return this.musicStream;
 	}
 
 	public int getSecondsElapsed() {
 		if ( this.progressBar != null )
 			return this.progressBar.getSecondsElapsed();
 		else
 			return 0;
 	}
 
 	/**
 	 * Restores screen and background image. This cannot be implemented in a handleExit() method
 	 * because, we may exit this screen to the screensaver (which doesn't use the standard background).
 	 *
 	 */
 	private void pop() {
 		this.app.setBackgroundImage();
 		this.app.pop();
 	}
 
 	/* (non-Javadoc)
 	 * @see com.tivo.hme.bananas.BScreen#handleEnter(java.lang.Object, boolean)
 	 */
 	@Override
 	public boolean handleEnter(Object arg0, boolean arg1) {
 		boolean status = super.handleEnter(arg0, arg1);
 		
 		// Set the background when entering the screen
 		
 		//	constants used for screen layout
 		double screenHeight	= this.app.getHeight();
 		double screenWidth	= this.app.getWidth();
 		double aspectRatio	= screenWidth / screenHeight;
 			
 		// Display the background image
 		if( this.app.isInSimulator() ) {
 			// We are in the simulator, so set a PNG background.
 			
 			// Change the background based on the new aspect ratio.
 			if( (aspectRatio > 1.7) && (aspectRatio < 1.8) ) {
 				// The current aspect ratio is approximately 16:9. 
 				// Use the high definition background meant for 720p.
 				String url = this.getContext().getBaseURI().toString();
 				try {
 					url += URLEncoder.encode("now_playing_720.png", "UTF-8");
 				}
 				catch(UnsupportedEncodingException e) {
 				}
 				this.app.getRoot().setResource(this.createStream(url, "image/png", true));
 			}
 			else {
 				// Default background is standard definition 640 x 480 (4:3)
 				this.app.getRoot().setResource("now_playing_sd.png");
 			}
 		}
 		else {
 			// We are running on a real TiVo, so use an MPEG background to conserve memory.
 			
 			// Change the background based on the new aspect ratio.
 			if( (aspectRatio > 1.7) && (aspectRatio < 1.8) ) {
 				// The current aspect ratio is approximately 16:9. 
 				// Use the high definition background meant for 720p.		
 				this.app.getRoot().setResource("now_playing_720.mpg");
 			}
 			else {
 				// Default background is standard definition 640 x 480 (4:3)
 				this.app.getRoot().setResource("now_playing_sd.mpg");
 			}
 		}
 		
 		return status;
 	}
 
 	/* (non-Javadoc)
 	 * Handles key presses from TiVo remote control.
 	 */
 	@Override
 	public boolean handleKeyPress(int key, long rawcode) {
 		
 		if (key == KEY_CLEAR) {
 			this.app.setInactive();
 			return true;
 		}
 			
 		this.app.checkKeyPressToResetInactivityTimer(key);
 		
 		switch(key) {
 		case KEY_INFO:
 			pop();
 			BrowsePlaylistScreen bps;
 			BScreen s = app.getCurrentScreen();
 			if (s instanceof BrowsePlaylistScreen) {
 				bps = (BrowsePlaylistScreen)s;
 				if(bps.isNowPlayingPlaylist()) {
 					bps.focusNowPlaying();
 					return true;
 				}
 			}
 			bps = new BrowsePlaylistScreen(app);
 			app.push(bps, TRANSITION_LEFT);
 			bps.focusNowPlaying();
 			return true;
 		case KEY_LEFT:
 			switch( this.app.getDiscJockey().getPlayRate() ) {
 			case NORMAL:
 			case PAUSE:
 			case STOP:
 				if(getBApp().getStackDepth() <= 1) {
 					getBApp().setActive(false);
 				}
 				else {
 					this.pop();
 				}
 				break;
 			default:
 				this.app.play("bonk.snd");
 				break;
 			}
 			return true;
 		case KEY_FORWARD:
 			this.app.play( this.app.getDiscJockey().getPlayRate().getNextFF().getSound() );
 			this.app.getDiscJockey().fastForward();
 			return true;
 			
 		case KEY_REVERSE:
 			this.app.play( this.app.getDiscJockey().getPlayRate().getNextREW().getSound() );
 			this.app.getDiscJockey().rewind();
 			return true;
 			
 		case KEY_PLAY:
 			this.app.getDiscJockey().playNormalSpeed();
 			return true;
 			
 		case KEY_PAUSE:
 			this.app.getDiscJockey().togglePause();
 			return true;
 			
 		case KEY_CHANNELUP:
 			if( ! ( this.app.getDiscJockey().isAtEndOfPlaylist() && (! this.app.getDiscJockey().isRepeating() ) ) )
 			{
 				this.app.play("pageup.snd");
 				this.app.getDiscJockey().playNext();
 			}
 			else {
 				this.app.play("bonk.snd");
 			}
 			return true;
 			
 		case KEY_CHANNELDOWN:
 			if( getSecondsElapsed() > DiscJockey.BACK_UP_AFTER_SECONDS || !(this.app.getDiscJockey().isAtBeginningOfPlaylist()  && !this.app.getDiscJockey().isRepeating()) )
 			{
 				this.app.play("pagedown.snd");
 				this.app.getDiscJockey().playPrevious();
 			}
 			else {
 				this.app.play("bonk.snd");
 			}
 			return true;
 			
 		case KEY_REPLAY:
 			this.app.play("select.snd");
 			this.app.getDiscJockey().toggleRepeatMode();
 			return true;
 			
 		case KEY_ADVANCE:
 			this.app.play("select.snd");
 			this.app.getDiscJockey().toggleShuffleMode();
 			return true;
 		}
 		
 		return super.handleKeyPress(key, rawcode);
 
 	}
 	
 	private void handleResourceInfoEvent(HmeEvent event)
 	{
     	HmeEvent.ResourceInfo resourceInfo = (HmeEvent.ResourceInfo) event;
     	
     	if (this.musicStream != null)
     		System.out.println("Stream status: " + this.musicStream.getStatus());
     	
     	// Check that this event is for the music stream
     	if( (this.musicStream != null) && (event.getID() == this.musicStream.getID() ) ) 
     	{
         	// Check the type of status sent
     		switch( resourceInfo.getStatus() ) 
     		{
 	    		case RSRC_STATUS_PLAYING:
 					// the current track is playing. Update the progress bar, if we know the length of the track.
 					if(this.progressBar != null) 
 					{
 						// Set the duration label, if it hasn't already been set, and if we know what our font looks like
 						long duration = this.app.getDiscJockey().getNowPlaying().getDuration();
 						
 						if( (! this.progressBar.isDurationUpdated() ) && (this.progressBar.fontInfo != null ) )
 							this.progressBar.setDuration(duration);
 	
 						// Set elapsed, which updates the elapsed label and the progress bar position.
 						if (duration > 0)
 						{
 							String [] positionInfo = resourceInfo.getMap().get("pos").toString().split("/");
 							long elapsed = Long.parseLong(positionInfo[0]);
 							this.progressBar.setElapsed(elapsed);
 						}
 					}
 					break;
 					
 	    		case RSRC_STATUS_SEEKING:
 	
 	    			// Set elapsed, which updates the elapsed label and the progress bar position.
 					String [] positionInfo = resourceInfo.getMap().get("pos").toString().split("/");
 					long elapsed = Long.parseLong(positionInfo[0]);
 					double fractionComplete = this.progressBar.setElapsed(elapsed);
 	        		
 	        		// Since we're using our custom duration rather than the one the Tivo sends in the event,
 	        		// trickplay doesn't automatically stop at the beginning or end of a track when fast forwarding
 	        		// or rewinding. Implement it.
 	        		double lowerLimit = 0;
 	        		double upperLimit = .95;
 	        		if(Float.parseFloat( resourceInfo.getMap().get("speed").toString() ) < 0  && fractionComplete <= lowerLimit) 
 	        		{
 	        			// We are rewinding and are about to hit the beginning of the track. 
 	        			// Position the track at our lower limit and drop back to NORMAL speed.
 	        			long position = (long)( this.app.getDiscJockey().getNowPlaying().getDuration() * lowerLimit);
 	        			this.musicStream.setPosition(position);
 	        			this.app.getDiscJockey().playNormalSpeed();
 	        		}
 	        		if( Float.parseFloat( resourceInfo.getMap().get("speed").toString() ) > 1 && fractionComplete >= upperLimit ) 
 	        		{
 	        			// We are fast forwarding and are about to hit the end of the track. 
 	        			// Position the track at our upper limit and drop back to NORMAL speed.
 	        			long position = (long)( this.app.getDiscJockey().getNowPlaying().getDuration() * upperLimit);
 	
 	        			this.musicStream.setPosition(position);
 	        			this.app.getDiscJockey().playNormalSpeed();
 	        		}
 	        		break;
 	        		
 	    		case RSRC_STATUS_CLOSED:
 	    		case RSRC_STATUS_COMPLETE:
 	    		case RSRC_STATUS_ERROR:
 					// the current track has finished, so check if there's another track to play.
 	    			if( this.app.getDiscJockey().isAtEndOfPlaylist() && ( ! this.app.getDiscJockey().isRepeating() ) ) 
 	    			{
 	 
 	    				// There's not another track to play
 	    				this.app.resetInactivityTimer();
 	    				
 	    				// Pop the screen saver if it is showing
 	    				if(this.app.getCurrentScreen().getClass() == ScreenSaverScreen.class)
 	    					this.pop();
 	    				
 	    				// Pop this Now Playing Screen only if it is showing.
 	    				if(this.app.getCurrentScreen().equals(this)) 
 	    					this.pop();
 					}
 					break;
 			}
 	    }
 	}
 	
 	/* (non-Javadoc)
 	 * Handles TiVo events
 	 */
 	@Override
 	public boolean handleEvent(HmeEvent event) 
 	{
 		// Check to see if this event is of a type that we want to handle
 		if (event.getOpCode() == EVT_RSRC_INFO)
 		{
 			handleResourceInfoEvent(event);
 		}
 
 		return super.handleEvent(event);
 	}
 
 	private class ProgressBar extends BView {
 		
 		private BView trackingBar;
 		private String elapsedLabel = "0:00";
 		private BText elapsedText = new BText( this, 0, 0, 0, this.getHeight() );
 		private BText durationText = new BText( this, this.getWidth(), 0, 0, this.getHeight() );
 		private Resource.FontResource font;
 		private HmeEvent.FontInfo fontInfo;
 		private boolean durationUpdated;
 		private long durationMS;
 		private long elapsedMS;
 		
 		/**
 		 * A bar that tracks the elapsed time of a stream. The height of the bar is dependent on the font sizej chosen
 		 * for text.
 		 * 
 		 * @param parent		container for this ProgressBar
 		 * @param x				X coordinate of top-left corner of this ProgressBar
 		 * @param y				Y coordinate of top-left corner of this ProgressBar
 		 * @param width			width of this ProgressBar
 		 * @param timeLength	length of time this ProgressBar tracks in milliseconds
 		 * @param fontSize		the font to use for text in the ProgressBar
 		 */
 		public ProgressBar(NowPlayingScreen parent, int x, int y, int width, int fontSize) {
 			super(parent.getNormal(), x, y, width, 0, false);
 			
 			// Create font. Use FONT_METRIC_BASIC flag so that we get font metrics.
 			this.font = (Resource.FontResource) createFont( "default.ttf", FONT_PLAIN, fontSize, FONT_METRICS_BASIC | FONT_METRICS_GLYPH);
 			this.elapsedText.setValue(elapsedLabel);
 			this.elapsedText.setFont(this.font);
 			this.durationText.setFont(this.font);
 			this.font.addHandler(this);
 			parent.progressBar = this;
 			
 		}
 		
 		/*
 		 * (non-Javadoc)
 		 * @see com.tivo.hme.bananas.BView#handleEvent(com.tivo.hme.sdk.HmeEvent)
 		 */
 		@Override
 		public boolean handleEvent(HmeEvent event) {
 
 			switch (event.getOpCode()) {
             	
             case EVT_FONT_INFO:
                 this.fontInfo = (HmeEvent.FontInfo) event;
    
                 // Resize the height of this ProgressBar to fit the font
                 this.setBounds( this.getX(), this.getY(), this.getWidth(), (int)this.fontInfo.getAscent() );
     			
     			// Format the zero label
     			int zeroWidth = fontInfo.measureTextWidth(this.elapsedLabel);
     			this.elapsedText.setBounds(0, 0, zeroWidth, this.getHeight() );
     			
     			// Create the tracking bar
     			int trackingBarHeight = (int) this.getHeight() / 2;
                 this.trackingBar = new BView(	this, 
                 								this.elapsedText.getWidth() + (int)fontInfo.getGlyphInfo('l').getAdvance(), 
                 								(int)( (this.getHeight() - trackingBarHeight) / 2 ), 
                 								0, 
                 								trackingBarHeight
                 );
     			this.trackingBar.setResource(HSkin.NTSC_WHITE);
                 
     			// Make the progress bar visible
     			this.setVisible(true);
     			
                 return true;
             }
             return super.handleEvent(event);
         }
 		
 		/**
 		 * Sets the position of the tracking bar
 		 * 
 		 * @param position	a double value between 0 and 1 representing the fraction of the stream that has played
 		 */
 		private boolean setPosition(double position) {
 			if( (position < 0) || (position > 1) ) {
 				return false;
 			}
 			
 			// reset width of tracking bar based on position
 			this.trackingBar.setBounds(	this.trackingBar.getX(), 
 										this.trackingBar.getY(), 
 										(int) ( ( this.getWidth() - this.elapsedText.getWidth() - this.durationText.getWidth() - ( 2 * (int)fontInfo.getGlyphInfo('l').getAdvance() ) ) * position ),
 										this.trackingBar.getHeight()
 			);
 			
 			return true;
 		}
 		
 		/**
 		 * Sets the duration label of this ProgressBar and resizes views to fit.
 		 * 
 		 * @param label
 		 * @return
 		 */
 		public void setDuration(long milliseconds) {
 
 			String label = millisecondsToTimeString(milliseconds);
 			int labelWidth = fontInfo.measureTextWidth(label);
 			
 			durationMS = milliseconds;
 			
 			// Resize the width of this ProgressBar to fit the labels
 			if(this.getWidth() < labelWidth) {
 				this.setBounds( this.getX(), this.getY(), labelWidth * 2, this.getHeight() );
 			}
 			this.trackingBar.setLocation(labelWidth + (int)fontInfo.getGlyphInfo('l').getAdvance(), this.trackingBar.getY());
 			
 			// Re-size the time labels and set their text
             this.durationText.setBounds(this.getWidth() - labelWidth, 0, labelWidth, this.getHeight());
             
             if (milliseconds > 0)
                 this.durationText.setValue(label);
             else
             {
                 this.elapsedText.setValue("");
                 this.durationText.setValue("");
                 setPosition(0);
             }
 
             this.elapsedText.setBounds(0, 0, labelWidth, this.getHeight() );
 
             // indicate that the duration label has been set
             this.durationUpdated = true;
 		}
 		
 		public double setElapsed(long elapsedMS)
 		{
 			this.elapsedMS = elapsedMS;
 			this.elapsedText.setValue(millisecondsToTimeString(elapsedMS));
 			double fractionComplete = (double)elapsedMS / durationMS; 
 			setPosition(fractionComplete);
 			return fractionComplete;
 		}
 		
 		private String millisecondsToTimeString(long milliseconds)
 		{
 			int minutes = (int)(milliseconds / 60000);
 			int seconds = (int)((milliseconds % 60000) / 1000);
 			String secondsLabel = String.format("%02d", seconds);
 			return minutes + ":" + secondsLabel;
 		}
 
 		/**
 		 * @return the timeUpdated
 		 */
 		public boolean isDurationUpdated() {
 			return durationUpdated;
 		}
 
 		/**
 		 * @param timeUpdated the timeUpdated to set
 		 */
 		public void setDurationUpdated(boolean timeUpdated) {
 			this.durationUpdated = timeUpdated;
 		}
 		
 		public int getSecondsElapsed() {
 			return (int) (elapsedMS / 1000);
 		}
 		
 	}
 }
