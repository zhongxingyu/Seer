 
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
 
 import java.io.Serializable;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 
 import fm.audiobox.core.exceptions.LoginException;
 import fm.audiobox.core.exceptions.ServiceException;
 import fm.audiobox.interfaces.IConfiguration;
 import fm.audiobox.interfaces.IConnector;
 import fm.audiobox.interfaces.IEntity;
 import fm.audiobox.interfaces.IResponseHandler;
 
 
 /**
  * User model is a special {@link Model} just because almost every library browse action is performed through this
  * object.
  *
  * <p>
  *
  * When a login is successfully performed an XML like the following is received and parsed:
  * 
  * <pre>
  * @code
  * username: 'username',
  * real_name: 'Real Name',
  * email: 'email@domain.com',
  * auth_token: '',
  * media_files_count: 100,
  * playlists_count: 3,
  * total_play_count: 122,
  * country: 'country',
  * time_zone: 'UTC',
  * data_served_this_month: 17402342,
  * data_served_overall: 17402342,
  * cloud_data_stored_overall: 0,
  * cloud_data_stored_this_month: 0,
  * local_data_stored_overall: 0,
  * local_data_stored_this_month: 0,
  * dropbox_data_stored_overall: 17402342,
  * dropbox_data_stored_this_month: 17402342,
  * accepted_extensions: 'aac,mp3,mp2,m4a,m4b,m4r,3gp,ogg,oga,flac,spx,wma,rm,ram,wav,mpc,mp+,mpp,aiff,aif,aifc,tta,mp4,m4v,mov,avi,flv,webm',
  * accepted_formats: 'audio/aac,audio/mpeg,audio/mp4,audio/ogg,audio/flac,audio/speex,audio/x-ms-wma,audio/x-pn-realaudio,audio/vnd.wave,audio/x-musepack,audio/x-aiff,audio/x-tta,video/mp4,video/x-m4v,video/quicktime,video/x-msvideo,video/x-flv,video/webm',
  * permissions:
  *   cloud: true,
  *   local: true,
  *   dropbox: true
  * @endcode
  * </pre>
  *
  * Through the User object you have access to its library that can be browsed by:
  * <ul>
  *  <li>Playlists</li>
  * </ul>
  * 
  * using its respective getter method.
  * 
  * <p>
  * 
  * Once obtained the desired collection you can get the tracks collection of each contained element 
  * by getting its tracks:
  * 
  * <pre>
  * Playlists playlists = user.getPlaylists();
  * playlists.load();
  * Playlist musicPlaylist = playlists.get(0);
  * MediaFiles media = musicPlaylist.getMediaFiles();
  * media.load();
  * </pre>
  * 
  * Or you can get informations about a specific, token-known track's, by calling {@link User#getTrackByToken(String)}
  * 
  * @author Valerio Chiodino
  * @author Fabio Tunno
  */
 public final class User extends AbstractEntity implements Serializable {
 
   private static final long serialVersionUID = 1L;
 
   /** User API namespace */
   public static final String NAMESPACE = "user";
   public static final String TAGNAME = NAMESPACE;
 
   /** Separator used to split the allowed formats string */
   public static final String ALLOWED_EXTENSIONS_SEPARATOR = ";";
 
   private String username;
   private String real_name;
   private String email;
   private String auth_token;
   private String country;
   private String time_zone;
   private String accepted_extensions;
   private String accepted_formats;
   private int    playlists_count;
   private long   media_files_count;
   private long   total_play_count;
   private long   data_served_this_month;
   private long   data_served_overall;
   private long   cloud_data_stored_overall;
   private long   cloud_data_stored_this_month;
   private long   local_data_stored_overall;
   private long   local_data_stored_this_month;
   private long   dropbox_data_stored_overall;
   private long   dropbox_data_stored_this_month;
 
   // User's collection relations
   private Playlists playlists;
   private Permissions permissions;
 
   /**
    * <p>Constructor for User.</p>
    */
   public User(IConfiguration config) {
     super(config);
   }
 
 
 
   public String getTagName(){
     return TAGNAME;
   }
 
   @Override
   public String getNamespace(){
     return NAMESPACE;
   }
 
 
   /* ------------------- */
   /* Getters and setters */
   /* ------------------- */
   
   
   public String getUsername() {
     return username;
   }
 
 
 
   public void setUsername(String username) {
     this.username = username;
   }
 
 
 
   public String getRealName() {
     return real_name;
   }
 
 
 
   public void setRealName(String real_name) {
     this.real_name = real_name;
   }
 
 
 
   public String getEmail() {
     return email;
   }
 
 
 
   public void setEmail(String email) {
     this.email = email;
   }
 
 
 
   public String getCountry() {
     return country;
   }
 
 
 
   public void setCountry(String country) {
     this.country = country;
   }
 
 
 
   public String getTimeZone() {
     return time_zone;
   }
 
 
 
   public void setTimeZone(String time_zone) {
     this.time_zone = time_zone;
   }
 
 
 
   public String getAcceptedExtensions() {
     return accepted_extensions;
   }
 
 
 
   public void setAcceptedExtensions(String accepted_extensions) {
     this.accepted_extensions = accepted_extensions;
   }
 
 
 
   public String getAcceptedFormats() {
     return accepted_formats;
   }
 
 
 
   public void setAcceptedFormats(String accepted_formats) {
     this.accepted_formats = accepted_formats;
   }
 
 
 
   public int getPlaylistsCount() {
     return playlists_count;
   }
 
 
 
   public void setPlaylistsCount(int playlists_count) {
     this.playlists_count = playlists_count;
   }
 
 
 
   public long getMediaFilesCount() {
     return media_files_count;
   }
 
 
 
   public void setMediaFilesCount(long media_files_count) {
     this.media_files_count = media_files_count;
   }
 
 
 
   public long getTotalPlayCount() {
     return total_play_count;
   }
 
 
 
   public void setTotalPlayCount(long total_play_count) {
     this.total_play_count = total_play_count;
   }
 
 
 
   public long getDataServedTthisMonth() {
     return data_served_this_month;
   }
 
 
 
   public void setDataServedThisMonth(long data_served_this_month) {
     this.data_served_this_month = data_served_this_month;
   }
 
 
 
   public long getDataServedOverall() {
     return data_served_overall;
   }
 
 
 
   public void setDataServedOverall(long data_served_overall) {
     this.data_served_overall = data_served_overall;
   }
 
 
 
   public long getCloudDataStoredOverall() {
     return cloud_data_stored_overall;
   }
 
 
 
   public void setCloudDataStoredOverall(long cloud_data_stored_overall) {
     this.cloud_data_stored_overall = cloud_data_stored_overall;
   }
 
 
 
   public long getCloudDataStoredThisMonth() {
     return cloud_data_stored_this_month;
   }
 
 
 
   public void setCloudDataStoredThisMonth(long cloud_data_stored_this_month) {
     this.cloud_data_stored_this_month = cloud_data_stored_this_month;
   }
 
 
 
   public long getLocalDataStoredOverall() {
     return local_data_stored_overall;
   }
 
 
 
   public void setLocalDataStoredOverall(long local_data_stored_overall) {
     this.local_data_stored_overall = local_data_stored_overall;
   }
 
 
 
   public long getLocalDataStoredthisMonth() {
     return local_data_stored_this_month;
   }
 
 
 
   public void setLocalDataStoredThisMonth(long local_data_stored_this_month) {
     this.local_data_stored_this_month = local_data_stored_this_month;
   }
 
 
 
   public long getDropboxDataStoredOverall() {
     return dropbox_data_stored_overall;
   }
 
 
 
   public void setDropboxDataStoredOverall(long dropbox_data_stored_overall) {
     this.dropbox_data_stored_overall = dropbox_data_stored_overall;
   }
 
 
 
   public long getDropboxDataStoredThisMonth() {
     return dropbox_data_stored_this_month;
   }
 
 
 
   public void setDropboxDataStoredThisMonth(
       long dropbox_data_stored_this_month) {
     this.dropbox_data_stored_this_month = dropbox_data_stored_this_month;
   }
 
 
   
   public void setPermissions(Permissions permissions) {
     this.permissions = permissions;
   }
   
   public Permissions getPermissions() {
     return this.permissions;
   }
   
 
   /**
    * <p>Getter for the auth_token</p>
    *
    * @return String[] the auth_token
    */
   public String getAuthToken() {
     return auth_token;
   }
 
   /**
    * <p>Setter for the user auth_token</p>
    *
    * @param auth_token a String the contains the user authentication 
    */
   public void setAuthToken(String auth_token) {
     this.auth_token = auth_token;
     setChanged();
     notifyObservers();
   }
   
   
   
   /* ------------------- */
   /* Collection Browsing */
   /* ------------------- */
 
 
   /**
    * Given a known track Token this method will requests AudioBox.fm and returns a valid {@link MediaFile} object.
    *
    * @param token the token of the track you are asking for.
    * 
    * @return the requested track if exists.
    * 
    * @throws LoginException if user has not been authenticated
    * @throws ServiceException if the requested resource doesn't exists or any other ServiceException occur.
    * @throws ModelException 
    */
   public MediaFile newTrackByToken(String token) throws ServiceException, LoginException {
     MediaFile file = (MediaFile) getConfiguration().getFactory().getEntity( MediaFile.TAGNAME, getConfiguration() );
     file.setToken(token);
 //    file.load();
     return file;
   }
 
 
 
   
   public MediaFiles getMediaFilesMap() throws ServiceException, LoginException {
     return this.getMediaFilesMap(null);
   }
 
   /**
    * Use this method to get a {@link MediaFiles} instance containing 
    * all the {@code MD5} and {@code token} for media files owned by this User.
    * You can specify the Source for filtering the results
    * 
    * This method is useful for sync tools.
    *
    * @param source a {@link MediaFile.Source}
    *
    * @return a {@link MediaFiles} instance
    * 
    * @throws ServiceException if any connection problem to AudioBox.fm services occurs.
    * @throws LoginException if any authentication problem occurs.
    */
   public MediaFiles getMediaFilesMap(MediaFile.Source source) throws ServiceException, LoginException {
     MediaFiles mediaFiles = (MediaFiles) getConfiguration().getFactory().getEntity( MediaFiles.TAGNAME, getConfiguration() );
     
     IConnector connector = this.getConnector(IConfiguration.Connectors.RAILS);
     
     String path = IConnector.URI_SEPARATOR.concat( MediaFiles.NAMESPACE );
     String action = MediaFiles.Actions.hashes.toString();
     
     List<NameValuePair> params = null;
     if ( source != null ){
       params = new ArrayList<NameValuePair>();
       params.add( new BasicNameValuePair("source", source.toString().toLowerCase() ) );
     }
     
    connector.get(mediaFiles, path, action, params).send(false);
     
     return mediaFiles;
   }
 
 
   //  public boolean dropTracks(List<Track> tracks) throws LoginException, ServiceException {
   //    try {
   //      return this.getPlaylists().getPlaylistsByType( PlaylistTypes.TRASH ).get( AudioBox.FIRST ).addTracks(tracks);
   //    } catch (ModelException e) {
   //      e.printStackTrace();
   //    }
   //    return false;
   //  }
 
   //  public boolean dropTrack(Track track) throws LoginException, ServiceException {
   //    List<Track> tracks = new ArrayList<Track>();
   //    tracks.add(track);
   //    return dropTracks( tracks );
   //  }
 
 
   public void emptyTrash() throws LoginException, ServiceException {
     Playlists pls = (Playlists)getConfiguration().getFactory().getEntity( Playlists.NAMESPACE, getConfiguration() );
     String requestFormat = this.getConfiguration().getRequestFormat().toString().toLowerCase();
     getConnector().put( pls, Playlists.EMPTY_TRASH_ACTION , requestFormat).send(false);
   }
 
 
 
   /**
    * Instantiates a new Track. This method is used to upload a track
    * 
    * @return a new {@link MediaFile} instance
    */
   public MediaFile newTrack() {
     return (MediaFile) getConfiguration().getFactory().getEntity( MediaFile.TAGNAME, getConfiguration() );
   }
 
 
 
 
   public Playlists getPlaylists() {
     if ( this.playlists == null ){
       this.playlists = (Playlists) getConfiguration().getFactory().getEntity(Playlists.TAGNAME, getConfiguration());
     }
     return playlists;
   }
 
 
   /**
    * Executes request populating this class
    * 
    * @throws ServiceException
    * @throws LoginException
    */
   public void load() throws ServiceException, LoginException {
     this.load(null);
   }
 
   /**
    * Executes request populating this class and passing the {@link IResponseHandler} as response parser
    * 
    * @param responseHandler the {@link IResponseHandler} used as response content parser
    * @throws ServiceException
    * @throws LoginException
    */
   public void load(IResponseHandler responseHandler) throws ServiceException, LoginException {
     getConnector().get(this, null, null).send(false, null, responseHandler);
   }
 
 
 
   @Override
   protected void copy(IEntity entity) {
     // default: do nothing
   }
 
 
   public Method getSetterMethod(String tagName) throws SecurityException, NoSuchMethodException{
 
     if ( tagName.equals("username") ){
       return this.getClass().getMethod("setUsername", String.class);
 
     } else if ( tagName.equals("real_name") ){
       return this.getClass().getMethod("setRealName", String.class);
 
     } else if ( tagName.equals("email") ){
       return this.getClass().getMethod("setEmail", String.class);
 
     } else if ( tagName.equals("auth_token") ){
       return this.getClass().getMethod("setAuthToken", String.class);
 
     } else if ( tagName.equals("country") ){
       return this.getClass().getMethod("setCountry", String.class);
 
     } else if ( tagName.equals("time_zone") ){
       return this.getClass().getMethod("setTimeZone", String.class);
 
     } else if ( tagName.equals("accepted_extensions") ){
       return this.getClass().getMethod("setAcceptedExtensions", String.class);
 
     } else if ( tagName.equals("accepted_formats") ){
       return this.getClass().getMethod("setAcceptedFormats", String.class);
 
     } else if ( tagName.equals("playlists_count") ){
       return this.getClass().getMethod("setPlaylistsCount", int.class);
 
     } else if ( tagName.equals("media_files_count") ){
       return this.getClass().getMethod("setMediaFilesCount", long.class);
 
     } else if ( tagName.equals("total_play_count") ){
       return this.getClass().getMethod("setTotalPlayCount", long.class);
 
     } else if ( tagName.equals("data_served_this_month") ){
       return this.getClass().getMethod("setDataServedThisMonth", long.class);
 
     } else if ( tagName.equals("data_served_overall") ){
       return this.getClass().getMethod("setDataServedOverall", long.class);
 
     } else if ( tagName.equals("cloud_data_stored_overall") ){
       return this.getClass().getMethod("setCloudDataStoredOverall", long.class);
 
     } else if ( tagName.equals("cloud_data_stored_this_month") ){
       return this.getClass().getMethod("setCloudDataStoredThisMonth", long.class);
 
     } else if ( tagName.equals("local_data_stored_overall") ){
       return this.getClass().getMethod("setLocalDataStoredOverall", long.class);
 
     } else if ( tagName.equals("local_data_stored_this_month") ){
       return this.getClass().getMethod("setLocalDataStoredThisMonth", long.class);
 
     } else if ( tagName.equals("dropbox_data_stored_overall") ){
       return this.getClass().getMethod("setDropboxDataStoredOverall", long.class);
 
     } else if ( tagName.equals("dropbox_data_stored_this_month") ){
       return this.getClass().getMethod("setDropboxDataStoredThisMonth", long.class);
 
     } else if ( tagName.equals("permissions") ) {
       return this.getClass().getMethod("setPermissions", Permissions.class);
       
     }
     
     
     return null;
   }
 
   @Override
   public String getApiPath() {
     return "/" + NAMESPACE;
   }
 
 
 
   @Override
   public void setParent(IEntity parent) {}
 
 }
