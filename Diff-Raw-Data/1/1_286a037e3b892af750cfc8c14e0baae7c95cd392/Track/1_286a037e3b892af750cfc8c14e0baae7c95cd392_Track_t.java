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
 
 package fm.audiobox.api.models;
 
 import java.io.File;
 import java.net.SocketException;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.xml.sax.SAXException;
 
 import fm.audiobox.api.AudioBoxClient;
 import fm.audiobox.api.core.Model;
 import fm.audiobox.api.core.ModelItem;
 import fm.audiobox.api.core.ModelsCollection;
 import fm.audiobox.api.exceptions.LoginException;
 import fm.audiobox.api.util.MD5Converter;
 
 /**
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
  *   <year type="integer">2001</year>
  *   <stream-url>http://url.to/<uuid>/stream</stream-url>
  *   <audio-file-size>12313124432</audio-file-size>
  *   <artist>
  *     <name>Artist name</name>
  *     <token>iq6ieJ9z</token>
  *   </artist>
  *   <album>
  *     <name>Album name</name>
  *     <token>DUoSAAoN</token>
  *     <cover-url-as-thumb>http://url.to/thumb.png</cover-url-as-thumb>
  *     <cover-url>http://url.to/original.jpg?1269960151</cover-url>
  *   </album>
  * </track>
  * }
  * </pre>
  * 
  * @author Valerio Chiodino
  * @version 0.2-beta
  */
 
 
 public class Track extends ModelItem {
 	
 	// Constants
 	public static final String TAG_NAME = "track";
 	
 	private static final String PATH = "tracks";
 	private static final String STREAM_ACTION = "stream";
 	private static final String SCROBBLE_ACTION = "scrobble";
 	private static final String LOVE_ACTION = "love";
 	private static final String UNLOVE_ACTION = "unlove";
 
 	// Customized fields
 	private String uuid;
 	
 	// XML model fields
 	protected String duration;
 	protected long durationInSeconds;
 	protected boolean loved;
 	protected int playCount;
 	protected String title;
 	protected int year;
 	protected String streamUrl;
 	protected long audioFileSize;
 	protected ModelItem artist;
 	protected ModelItem album;
 	
 
 	// Utility fields
 	public enum State { IDLE, PLAYING, ERROR, BUFFERING, PAUSED }
 	protected State trackState = Track.State.IDLE;
 	protected File file;
 	protected String hashCode = null;
 	
 	public Track() {
 	    this.endPoint = Tracks.END_POINT;
 	}
 	
 	public Track( File file){
 		super();
 		this.file = file;
 	}
 	
 	@Override
 	public String getToken() {
 	    return getUuid();
 	}
 	
 	public void setUuid(String uuid) {
 	    this.uuid = uuid;
 	}
 	
 	/**
 	 * @return the unique id of the track
 	 */
 	public String getUuid() {
 
 		if (uuid == null) {
 			String	regex = "^" + AudioBoxClient.API_PATH.replace(".", "\\.") + PATH + "/([^\\s]+)/stream$";
 			java.util.regex.Matcher m = java.util.regex.Pattern.compile(regex).matcher(streamUrl);
 			m.find();
 			uuid = m.group(1);
 		}
 		
 		return uuid;		
 	}
 	
 	
 	public void setDuration(String duration) {
 	    this.duration = duration;
 	}
 	
 	/**
 	 * @return the duration
 	 */
 	public String getDuration() {
 		return duration;
 	}
 	
 	
 	public void setTitle(String title) {
 	    this.title = title;
 	}
 	
 	
 	/**
 	 * @return the title
 	 */
 	public String getTitle() {
 		return title;
 	}
 	
 	
 	
 	
 	public void setStreamUrl(String url) {
 	    this.streamUrl = url;
 	}
 	
 	
 	/**
 	 * @return the streamUrl
 	 */
 	public String getStreamUrl() throws LoginException , ParserConfigurationException, SAXException, SocketException {
 	    return AudioBoxClient.execute( this.endPoint, this.getUuid(), STREAM_ACTION, null, null);
 	}
 	
 	
 	public void setLoved(String loved) {
         this.loved = Boolean.parseBoolean( loved );
     }
 	
 	public void setLoved(boolean loved) {
 	    this.loved = loved;
 	}
 	
 	/**
 	 * @return the loved
 	 */
 	public boolean isLoved() {
 		return loved;
 	}
 	
 	
 	public void setPlayCount(String playCount) {
 	    this.playCount = Integer.parseInt( playCount );
 	}
 	
 
     public void setPlayCount(int playCount) {
         this.playCount = playCount;
     }
 	
 	/**
 	 * @return the playCount
 	 */
 	public int getPlayCount() {
 		return playCount;
 	}
 
 	
 	
 	public void setYear(String year) {
 	    this.year = Integer.parseInt( year );
 	}
 	
 	/**
 	 * @return the loved
 	 */
 	public int getYear() {
 		return year;
 	}
 	
 	
 	
 	
 	public void setAudioFileSize(String fileSize) {
 	    this.audioFileSize = Long.parseLong( fileSize );
 	}
 	
 	/**
 	 * @return the audioFileSize
 	 */
 	public long getAudioFileSize() {
 		return audioFileSize;
 	}
 	
 	
 	
 	public void setArtist(ModelItem artist) {
 	    this.artist = artist;
 	}
 	
 	
 	/**
 	 * @return the artist
 	 */
 	public ModelItem getArtist() {
 		return artist;
 	}
 	
 	
 	
 	public void setAlbum(ModelItem album) {
 	    this.album = album;
 	}
 	
 	/**
 	 * @return the album
 	 */
 	public ModelItem getAlbum() {
 		return album;
 	}
 
 	
 	
 	public void setDurationInSeconds(String durationInSeconds) {
 	    this.durationInSeconds = Long.parseLong( durationInSeconds );
 	}
 	
 	/**
 	 * @return the durationInSeconds
 	 */
 	public long getDurationInSeconds() {
 		return durationInSeconds;
 	}
 	
 	/**
 	 * @return the file to upload
 	 */
 	public File getFile(){
 		return this.file;
 	}
 	
 	
 	/* ------- */
 	/* Actions */
 	/* ------- */
 	
 	public void scrobble()  throws LoginException , ParserConfigurationException, SAXException, SocketException {
 	    AudioBoxClient.execute( this.endPoint, this.getUuid(), SCROBBLE_ACTION, null, HttpPost.METHOD_NAME);
 	}
 	
 	public boolean love() throws LoginException , ParserConfigurationException, SAXException, SocketException {
 	    return "200".equals(  AudioBoxClient.execute( this.endPoint, this.getUuid(), LOVE_ACTION, null, HttpPut.METHOD_NAME)  );
 	}
 	
 	public boolean unlove() throws LoginException , ParserConfigurationException, SAXException, SocketException {
 	    return "200".equals(  AudioBoxClient.execute( this.endPoint, this.getUuid(), UNLOVE_ACTION, null, HttpPut.METHOD_NAME)  );
 	}
 	
 	
 	@Override
     public String getName() {
         return this.title + " - " + this.artist.getName() + " (" + this.duration + ")";
     }
 	
 	
 	/* ----- */
 	/* State */
 	/* ----- */
 	
 	/**
 	 * @param trackState the trackState to set
 	 */
 	public void setState(State trackState) {
 		this.trackState = trackState;
 	}
 
 	/**
 	 * @return the  track status
 	 */
 	public State getState() {
 		return trackState;
 	}
 
 	/**
 	 * @return true if the track contains errors
 	 */
 	public boolean hasErrors() {
 		return getState() == Track.State.ERROR;
 	}
 
 	/**
 	 * @return true if the track in in playing
 	 */
 	public boolean isPlaying() {
 		return getState() == Track.State.PLAYING;
 	}
 	
 	/**
 	 * @return true if the track in in playing
 	 */
 	public boolean isPaused() {
 		return getState() == Track.State.PAUSED;
 	}
 
 	/**
 	 * @return true if the track is currently buffering from network
 	 */
 	public boolean isBuffering() {
 		return getState() == Track.State.BUFFERING;
 	}
 	
 	
 	
 	public void upload() throws LoginException , ParserConfigurationException, SAXException, SocketException{
 		AudioBoxClient.execute( this.endPoint, null, null, this, HttpPost.METHOD_NAME, this.file);
 	}
 	
 	public String hash(){
 		if ( this.hashCode != null ){ return this.hashCode;}
 		if ( this.file != null && this.file.exists() )
 			this.hashCode = MD5Converter.digest(this.file);
 		return this.hashCode;
 	}
 	
 	/* Overrides */
 	@Override
 	public ModelItem getTrack(String uuid) { return this; }
 
 	@Override
     public void setTracks(ModelsCollection tracks) { }
     
     @Override
     public Model getTracks() { return null; }
 
 }
 
 
