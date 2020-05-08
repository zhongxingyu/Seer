 
 /***************************************************************************
  *   Copyright (C) 2010 iCoreTech research labs                            *
  *   Contributed code from:                                                *
  *   - Valerio Chiodino - keytwo at keytwo dot net                         *
  *   - Fabio Tunno      - fat at fatshotty dot net                         *
  *                                                                         *
  *   This program is free software: you can redistribute it and/or modify  *
  *   it under the terms of the GNU General Public License as published by  *
  *   the Free Software Foundation, either version 3 of the License, or     *
  *   (at your option) any later version.                                   *
  *                                                                         *
  *   This program is distributed in the hope that it will be useful,       *
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
  *   GNU General Public License for more details.                          *
  *                                                                         *
  *   You should have received a copy of the GNU General Public License     *
  *   along with this program. If not, see http://www.gnu.org/licenses/     *
  *                                                                         *
  ***************************************************************************/
 
 package fm.audiobox.core.models;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.entity.mime.content.FileBody;
 
 import fm.audiobox.core.api.Model;
 import fm.audiobox.core.api.ModelItem;
 import fm.audiobox.core.exceptions.LoginException;
 import fm.audiobox.core.exceptions.ServiceException;
 import fm.audiobox.core.models.AudioBoxClient.AudioBoxConnector;
 
 
 /**
  * Track is the main subject of the scope of these libraries.
  * 
  * <p>
  * 
  * Once browsed till arriving to a Track, many operations can be done with it.
  * 
  * <p>
  * 
  * The XML response looks like this:
  *
  * <pre>
  * {@code
  * <track>
  *   <duration>4:15</duration>
  *   <duration-in-seconds type="integer">296</duration-in-seconds>
  *   <loved type="boolean">true</loved>
  *   <play-count type="integer">3</play-count>
  *   <title>Track title</title>
  *   <track-number type="integer">7</track-number>
  *   <year type="integer">2001</year>
  *   <stream-url>http://url.to/<uuid>/stream</stream-url>
  *   <audio-file-size>12313124432</audio-file-size>
  *   <original-file-name>song.mp3</original-file-name>
  *   <artist>
  *     <name>Artist name</name>
  *     <token>iq6ieJ9z</token>
  *   </artist>
  *   <album>
  *     <name>Album name</name>
  *     <token>DUoSAAoN</token>
  *     <cover-url-as-thumb>http://url.to/thumb.png</cover-url-as-thumb>
  *     <cover-url>http://url.to/original.jpg</cover-url>
  *   </album>
  * </track>
  * }
  * </pre>
  *
  * @author Valerio Chiodino
  * @author Fabio Tunno
  * @version 0.0.1
  */
 public class Track extends ModelItem {
 
     // Constants
     /** The XML tag name for the Track element */
     public static final String TAG_NAME = "track";
 
     /** The HTTP Parameter to use when uploading a track */
     public static final String HTTP_PARAM = "media";
 
     protected static final String STREAM_ACTION = "stream";
     protected static final String DOWNLOAD_ACTION = STREAM_ACTION;
 
     private static final String PATH = "tracks";
     private static final String SCROBBLE_ACTION = "scrobble";
     private static final String LOVE_ACTION = "love";
     private static final String UNLOVE_ACTION = "unlove";
     
     // Customized fields
     private String uuid;
     private File file;
 
     // XML model fields
     protected String duration;
     protected long durationInSeconds;
     protected boolean loved;
     protected int playCount;
     protected String title;
     protected String trackNumber;
     protected int year;
     protected String fileHash;
     protected String streamUrl;
     protected long audioFileSize;
     protected String originalFileName;
     protected Artist artist;
     protected Album album;
     
     protected FileOutputStream fileOutputStream;
 
 
     // Utility fields
     /**
      * During the lifecycle a Track can be put in differents states.
      * 
      * <p>
      * 
      * Note though that this is an util element.<br/> 
      * <strong>Playing of the song is out of these libraries scope.</strong>
      * 
      * <ul>
      *  <li>IDLE state: track is ready for any action. It is equivalent to "Stopped".</li>
      *  <li>PLAYING state: the track is being played.</li>
      *  <li>ERROR state: any error occurred while trying to playing the track.</li>
      *  <li>BUFFERING state: track is currently downloading.</li>
      *  <li>PAUSED state: track has been paused.</li>
      * </ul>
      */
     public enum State { 
         /** Idle state: track is ready for any action. It is equivalent to "Stopped". */
         IDLE, 
         
         /** Playing state: the track is being played. */
         PLAYING, 
         
         /** Error state: any error occurred while trying to playing the track. */
         ERROR, 
         
         /** Buffering state: track is currently downloading. */
         BUFFERING, 
         
         /** Paused state: track has been paused. */
         PAUSED
     }
     protected State trackState = Track.State.IDLE;
 
     // Used for upload purpose
     protected FileBody fileBody;
 
 
     /**
      * <p>Constructor for Track.</p>
      */
     protected Track() {
         this.pEndPoint = Tracks.END_POINT;
     }
 
     /**
      * <p>Constructor for Track.</p>
      *
      * @param fileBody a {@link FileBody} object.
      */
     public Track(FileBody fileBody) {
         super();
         this.fileBody = fileBody;
     }
 
 
     /* ------------------- */
     /* Getters and setters */
     /* ------------------- */
 
 
     /**
      * <p>Setter for the track's uuid.</p>
      *
      * @param uuid the track's UUID String.
      */
     public void setUuid(String uuid) {
         this.uuid = uuid;
     }
 
     /**
      * <p>Getter for the track's uuid.</p>
      *
      * @return the unique id of the track
      */
     public String getUuid() {
 
         if (this.uuid == null) {
             String	regex = "^" + pConnector.getApiPath().replace(".", "\\.") + PATH + "/([^\\s]+)/stream$";
             java.util.regex.Matcher m = java.util.regex.Pattern.compile(regex).matcher(streamUrl);
             m.find();
             this.setUuid( m.group(1) );
         }
 
         return this.uuid;		
     }
 
 
 
     /**
      * Setter method for the file of this track: used by the {@link Model#handleResponse(org.apache.http.HttpResponse)} 
      * method.
      * 
      * @param file the file to set
      */
     public void setFile(File file) {
         this.file = file;
     }
 
     /**
      * Getter method for the file of this track.
      * 
      * <p>
      * 
      * This field is populated if a {@link Track#download(String)} is succesfully performed.
      * 
      * @return the file of this track.
      */
     public File getFile() {
         return file;
     }
 
     
     
     
     /**
      * <p>Setter for the track duration: used by the parser.</p>
      *
      * @param duration the String of the duration in MM:SS format.
      */
     public void setDuration(String duration) {
         this.duration = duration;
     }
 
     /**
      * <p>Getter for the track duration.</p>
      *
      * @return the duration of the track in MM:SS format
      */
     public String getDuration() {
         return duration;
     }
 
 
 
     /**
      * <p>Setter for the track title: used by the parser.</p>
      *
      * @param title the track title.
      */
     public void setTitle(String title) {
         this.title = title;
     }
 
     /**
      * <p>Getter for the track title.</p>
      *
      * @return the title of the track
      */
     public String getTitle() {
         return title;
     }
     
     
     
     /**
      * <p>Setter for the track number: used by the parser.</p>
      *
      * @param number the track.
      */
     public void setTrackNumber(String number) {
         this.trackNumber = number;
     }
 
     /**
      * <p>Getter for the track number.</p>
      *
      * @return the number of the track
      */
     public String getTrackNumber() {
         return title;
     }
 
 
 
     /**
      * <p>Setter for the URL of the streaming: used by the parser.</p>
      *
      * @param url the String of the url
      */
     public void setStreamUrl(String url) {
         this.streamUrl = url;
     }
 
     /**
      * <p>Getter for the track streaming url.</p>
      *
      * @return the track streamUrl
      * 
      * @throws LoginException if any authentication problem during the request occurs.
      * @throws ServiceException if any connection problem to AudioBox.fm occurs.
      */
     public String getStreamUrl() throws LoginException, ServiceException {
         String[] result = this.getConnector().execute( this.pEndPoint, this.getUuid(), STREAM_ACTION, this, null);
         return result[ AudioBoxConnector.RESPONSE_BODY ];
     }
 
 
 
     /**
      * This method is used by the parser. Please use {@link Track#setLoved(boolean)} instead.
      * 
      * <p>
      * 
      * Setter for the field <code>loved</code>: used by the parser.
      * 
      * @param loved a String representing the "loved state" of the track.
      */
     @Deprecated
     public void setLoved(String loved) {
         this.setLoved( Boolean.parseBoolean( loved ) );
     }
 
     /**
      * <p>Setter for the field <code>loved</code>.</p>
      *
      * @param loved true if the track is loved, false otherwise.
      */
     public void setLoved(boolean loved) {
         this.loved = loved;
     }
 
     /**
      * <p>Check whether the track is loved or not</p>
      *
      * @return the loved state of the track
      */
     public boolean isLoved() {
         return loved;
     }
 
 
 
     /**
      * This method is used by the parser. Please use {@link Track#setPlayCount(int)} instead.
      * 
      * <p>
      * 
      * Setter for the numbers of plays for this song: used by the parser.
      * 
      * @param playCount String of the number of this track's plays
      */
     @Deprecated
     public void setPlayCount(String playCount) {
         this.setPlayCount( Integer.parseInt( playCount ) );
     }
 
     /**
      * <p>Setter for the numbers of plays for this song.</p>
      *
      * @param playCount int of the number of plays
      */
     public void setPlayCount(int playCount) {
         this.playCount = playCount;
     }
 
     /**
      * <p>Getter for the track total plays.</p>
      *
      * @return the track plays count
      */
     public int getPlayCount() {
         return playCount;
     }
 
 
 
     /**
      * <p>Setter for the year of the track: used by the parser.</p>
      *
      * @param year the String representing the year of the track.
      */
     public void setYear(String year) {
         this.year = Integer.parseInt( year );
     }
 
     /**
      * <p>Getter for the track's year.</p>
      *
      * @return the year of the track
      */
     public int getYear() {
         return year;
     }
 
 
 
     /**
      * <p>Setter for the track's file MD5 hash: used by the parser.</p>
      *
      * @param fileHash the MD5 file hash to set
      */
     public void setFileHash(String fileHash) {
         this.fileHash = fileHash;
     }
 
     /**
      * <p>Getter for the track's file hash.</p>
      *
      * @return the track's file MD5 hash
      */
     public String getFileHash() {
         return fileHash;
     }
 
 
 
     /**
      * <p>Setter for the track's file size in bytes: used by the parser.</p>
      *
      * @param fileSize the String of the size in bytes.
      */
     public void setAudioFileSize(String fileSize) {
         this.audioFileSize = Long.parseLong( fileSize );
     }
 
     /**
      * <p>Getter for the audio file size</p>
      *
      * @return the track's file size in bytes
      */
     public long getAudioFileSize() {
         return audioFileSize;
     }
 
 
     
     /**
      * <p>Setter for the track's orginal file name: used by the parser.</p>
      * 
      * @param fileName the track's file name.
      */
     public void setOriginalFileName(String fileName) {
         this.originalFileName = fileName;
     }
     
     /**
      * <p>Getter for the original track's file name</p>
      * 
      * @return the original file name String
      */
     public String getOriginalFileName() {
         return this.originalFileName;
     }
     
 
     
     /**
      * <p>Setter for the track's duration in seconds: used by the parser.</p>
      *
      * @param durationInSeconds the String representing the number of seconds of the track's duration
      */
     public void setDurationInSeconds(String durationInSeconds) {
         this.durationInSeconds = Long.parseLong( durationInSeconds );
     }
 
     /**
      * <p>Getter for the track's duration in seconds.</p>
      *
      * @return the number of seconds of the track's duration
      */
     public long getDurationInSeconds() {
         return durationInSeconds;
     }
 
 
 
     /**
      * <p>Setter for the track's artist: used by the parser.</p>
      *
      * @param artist the track's artist.
      */
     public void setArtist(Artist artist) {
         this.artist = artist;
     }
 
     /**
      * <p>Getter for the track's artist.</p>
      *
      * @return the artist of this track
      */
     public Artist getArtist() {
         return artist;
     }
 
 
 
     /**
      * <p>Setter for track's album: used by the parser.</p>
      *
      * @param album the track's album to set
      */
     public void setAlbum(Album album) {
         this.album = album;
     }
 
     /**
      * <p>Getter for the album of the track.</p>
      *
      * @return the track's album
      */
     public Album getAlbum() {
         return album;
     }
 
 
 
     /**
      * <p>Use this method to get the file content for upload process.</p>
      *
      * @return the FileBody to upload
      */
     public FileBody getFileBody(){
         return this.fileBody;
     }
     
     
     /* -------------- */
     /* Remote Actions */
     /* -------------- */
 
 
     /**
      * Use this method at the end of a "just listened" song to scrobble it to Last.fm and to keep track of the
      * plays count to AudioBox.fm
      *
      * <p>
      * 
      * Remember that scrobbles have to be done at least one for listening and at 96% of the listening progres or
      * at the end.
      * 
      * <p>
      * 
      * This method also increment the track's playcount if the scrobble is correctly received by AudioBox.fm.
      *
      * @throws LoginException if any authentication problem during the request occurs.
      * @throws ServiceException if any connection problem to AudioBox.fm occurs.
      */
     public void scrobble() throws ServiceException, LoginException {
         String[] response = this.getConnector().execute( this.pEndPoint, this.getUuid(), SCROBBLE_ACTION, this, HttpPost.METHOD_NAME);
         if ( Integer.parseInt( response[ AudioBoxConnector.RESPONSE_CODE ] ) == HttpStatus.SC_OK)
             this.setPlayCount( this.getPlayCount() + 1 );
     }
 
     /**
      * Use this method to mark the track as loved.
      * 
      * <p>
      * 
      * Note that calling this method on an already "loved" track will have no effect.
      * 
      * <p>
      * 
      * This method also sets to <code>true</code> the loved state.
      *
      * @return true if the request is succesfully done, false otherwise.
      * 
      * @throws LoginException if any authentication problem during the request occurs.
      * @throws ServiceException if any connection problem to AudioBox.fm occurs.
      */
     public boolean love() throws ServiceException, LoginException  {
         boolean markLoved = true;
         if ( ! this.isLoved()) {
             String[] result = this.getConnector().execute( this.pEndPoint, this.getUuid(), LOVE_ACTION, this, HttpPut.METHOD_NAME);
             markLoved = HttpStatus.SC_OK == Integer.parseInt( result[ AudioBoxConnector.RESPONSE_CODE ] );
             this.setLoved( markLoved );
         }
         return markLoved;
     }
 
     /**
      * Use this method to mark the track as no more loved.
      * 
      * <p>
      * 
      * Note that calling this method on an "unloved" track will have no effect.
      * 
      * <p>
      * 
      * This method also sets to <code>false</code> the loved state.
      *
      * @return true if the request is succesfully done, false otherwise.
      * 
      * @throws LoginException if any authentication problem during the request occurs.
      * @throws ServiceException if any connection problem to AudioBox.fm occurs.
      */
     public boolean unlove() throws ServiceException, LoginException {
         boolean markLoved = true;
         if (this.isLoved()) {
             String[] result = this.getConnector().execute( this.pEndPoint, this.getUuid(), UNLOVE_ACTION, this, HttpPut.METHOD_NAME);
             markLoved = HttpStatus.SC_OK == Integer.parseInt( result[ AudioBoxConnector.RESPONSE_CODE ] );
             this.setLoved( !markLoved );
         }
         return markLoved;
     }
 
     /**
      * <p>Permanently destroy a media file. <strong>This operation cannot be reverted</strong>.</p>
      *
      * @return true if the request is succesfully done, false otherwise.
      * 
      * @throws LoginException if any authentication problem during the request occurs.
      * @throws ServiceException if any connection problem to AudioBox.fm occurs.
      */
     public boolean delete() throws ServiceException, LoginException {
         String[] result = this.getConnector().execute( this.pEndPoint, this.getUuid(), null, this, HttpDelete.METHOD_NAME);
         return HttpStatus.SC_OK == Integer.parseInt( result[ AudioBoxConnector.RESPONSE_CODE ] );
     }
     
     
     /**
      * <p>Download track and put binary data into given {@link FileOutputStream}.
      * 
      * @param fos the {@link FileOutputStream} to write binary data to
      * 
      * @throws ServiceException	if any connection problem to AudioBox.fm occurs.
      * @throws LoginException if any authentication problem during the request occurs.
      */
     public void download(final FileOutputStream fos) throws ServiceException, LoginException {
     	this.fileOutputStream = fos;
     	this.getConnector().execute( this.pEndPoint, this.getUuid(), DOWNLOAD_ACTION, this, null, true);
     }
 
     
     
     /* ----- */
     /* State */
     /* ----- */
 
     /**
      * Changes the states of the track.
      * 
      * <p>
      *
      * Useful for media player and sync tools.
      * 
      * @param trackState the {@link State} to put the track in
      */
     public void setState(State trackState) {
         this.trackState = trackState;
     }
 
     /**
      * <p>Use this method to get the {@link State} of the track.</p>
      *
      * @return the track {@link State}
      */
     public State getState() {
         return trackState;
     }
 
     /**
      * <p>Util method to know if the track contains errors.</p>
      *
      * @return true if the track contains errors
      */
     public boolean hasErrors() {
         return getState() == Track.State.ERROR;
     }
 
     /**
      * <p>Util method to know if the track is currently playing.</p>
      *
      * @return true if the track in in playing
      */
     public boolean isPlaying() {
         return getState() == Track.State.PLAYING;
     }
 
     /**
      * <p>Util method to know if the track is currently paused.</p>
      *
      * @return true if the track in in playing
      */
     public boolean isPaused() {
         return getState() == Track.State.PAUSED;
     }
 
     /**
      * <p>Util method to know if the track is currently buffering.</p>
      *
      * @return true if the track is currently buffering from network
      */
     public boolean isBuffering() {
         return getState() == Track.State.BUFFERING;
     }
 
 
     /* --------- */
     /* Overrides */
     /* --------- */
     
     /**
      * This method is used to parse the binary content of HTTP response.
      * 
      * <p>
      * 
      * Returns an empty string by default.
      *
     * @param input the {@link HttpEntity} content
     * @param contentLength the Content-Length header value of response
      * 
     * @return an empty string by default
      * 
      * @throws IOException if the parse process fails for some reasons.
      */
     @Override
     public String parseBinaryResponse( InputStream input ) throws IOException {
     	
     	if ( this.fileOutputStream == null )
     		throw new IOException("No such output stream");
     	
     	try {
 	    	
     		int read;
 	    	byte[] bytes = new byte[CHUNK];
 	    	
	    	while( (read = input.read(bytes)) != -1   )
 	    		this.fileOutputStream.write(bytes, 0, read);
 	    	
	    	
 	    	this.fileOutputStream.flush();
 	    	
     	} finally {
     		this.fileOutputStream.close();
     	}
     	
     	return super.parseBinaryResponse( input );
     }
     
 
     /** {@inheritDoc} */
     @Override
     public String getName() {
         return this.title + " - " + this.artist.getName() + " (" + this.duration + ")";
     }
 
     /** {@inheritDoc} */
     @Override
     public String getToken() {
         return getUuid();
     }
 
     /** {@inheritDoc} */
     @Override
     public Track getTrack(String uuid) {
         if ( this.getUuid().equals( uuid ) )
             return this;
         else return null;
     }
 
     /** Does nothing: prevent undesired behaviours. */
     @Override
     public void setTracks(Tracks tracks) { }
 
     /** 
      * Returns <code>null</code>: prevent undesired behaviours.
      * 
      * @return null
      */
     @Override
     public Tracks getTracks() { return null; }
 
 }
 
 
