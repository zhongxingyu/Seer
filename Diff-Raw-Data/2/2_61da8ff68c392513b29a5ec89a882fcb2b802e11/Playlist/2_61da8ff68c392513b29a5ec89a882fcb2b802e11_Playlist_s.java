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
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import fm.audiobox.AudioBox;
 import fm.audiobox.configurations.Response;
 import fm.audiobox.core.exceptions.LoginException;
 import fm.audiobox.core.exceptions.ServiceException;
 import fm.audiobox.core.observables.Event;
 import fm.audiobox.interfaces.IConfiguration;
 import fm.audiobox.interfaces.IConnector;
 import fm.audiobox.interfaces.IConnector.IConnectionMethod;
 import fm.audiobox.interfaces.IEntity;
 import fm.audiobox.interfaces.IResponseHandler;
 
 
 /**
  * This class represents the main class of the Playlist and Drive entity.
  * <p>
  * If you instance an arbitrary {@code Playlist} class, it will be identified as {@code Custom playlist} by default
  * </p>
  */
 public class Playlist extends AbstractEntity implements Serializable {
 
   private static final long serialVersionUID = 1L;
 
   private static Logger log = LoggerFactory.getLogger(Playlist.class);
 
   public static final String NAMESPACE = Playlists.TAGNAME;
 
   public static final String TAGNAME = "playlist";
 
   public static final String CUSTOM_SYSTEM_NAME = "custom";
   public static final String SMART_SYSTEM_NAME = "smart";
  public static final String OFFLINE_SYSTEM_NAME = "custom";
   
   public static final String NAME = "name";
   public static final String POSITION = "position";
   public static final String TYPE = "type";
   public static final String MEDIA_FILES_COUNT = "media_files_count";
   public static final String UPDATED_AT = "updated_at";
   public static final String LAST_ACCESSED = "last_accessed";
   public static final String SYSTEM_NAME = "system_name";
   public static final String EMBEDDABLE = "embeddable";
   public static final String VISIBLE = "visible";
   public static final String SYNCABLE = "syncable";
 
   private String name;
   private int position = 0;
   private String type;
   private long media_files_count;
   private MediaFiles mediafiles;
   private Albums albums;
   private String updated_at;
   private boolean last_accessed;
   private boolean embeddable;
   private boolean visible;
   private String system_name;
   private boolean syncable;
 
   private static final Map<String, Method> setterMethods = new HashMap<String, Method>();
   private static Map<String, Method> getterMethods = null;
   
   
   static {
     try {
       setterMethods.put(TOKEN, Playlist.class.getMethod("setToken", String.class));
       setterMethods.put(NAME, Playlist.class.getMethod("setName", String.class));
       setterMethods.put(POSITION, Playlist.class.getMethod("setPosition", int.class));
       setterMethods.put(TYPE, Playlist.class.getMethod("setType", String.class));
       setterMethods.put(MEDIA_FILES_COUNT, Playlist.class.getMethod("setMediaFilesCount", long.class));
       setterMethods.put(UPDATED_AT, Playlist.class.getMethod("setUpdatedAt", String.class));
       setterMethods.put(LAST_ACCESSED, Playlist.class.getMethod("setLastAccessed", boolean.class));
       setterMethods.put(SYSTEM_NAME, Playlist.class.getMethod("setSystemName", String.class));
       setterMethods.put(EMBEDDABLE, Playlist.class.getMethod("setEmbeddable", boolean.class));
       setterMethods.put(VISIBLE, Playlist.class.getMethod("setVisible", boolean.class));
       setterMethods.put(SYNCABLE, Playlist.class.getMethod("setSyncable", boolean.class));
     } catch (SecurityException e) {
       log.error("Security error", e);
     } catch (NoSuchMethodException e) {
       log.error("No method found", e);
     }
   }
 
 
   public Playlist(IConfiguration config) {
     super(config);
 
     // By default a playlist is set as 'custom' playlist
     this.setSystemName(CUSTOM_SYSTEM_NAME);
   }
 
 
   public Playlist(AudioBox abxClient) {
     this(abxClient.getConfiguration());
   }
 
 
   public String getTagName() {
     return TAGNAME;
   }
 
 
   public String getNamespace() {
     return NAMESPACE;
   }
 
 
   /**
    * @return the playlist name
    */
   public String getName() {
     return name;
   }
 
 
   /**
    * Sets the playlist name
    * 
    * @param name
    *          of the Playlist
    */
   public void setName(String name) {
     this.name = name;
   }
 
 
   /**
    * @return the playlist AudioBox.fm system name
    */
   public String getSystemName() {
     return this.system_name;
   }
 
 
   /**
    * This method is used by response parser
    */
   public void setSystemName(String system_name) {
     this.system_name = system_name;
   }
 
 
   /**
    * Use this method to know if {@code playlist} is @{value smart} or not
    * 
    * @return boolean
    */
   public boolean isSmart() {
     return SMART_SYSTEM_NAME.equals(this.getSystemName());
   }
 
 
   /**
    * Use this method to know if {@code playlist} is @{value custom} or not
    * 
    * @return boolean
    */
   public boolean isCustom() {
     return CUSTOM_SYSTEM_NAME.equals(this.getSystemName());
   }
 
 
   /**
    * Use this method to know if {@code playlist} is a @{value drive} or not
    * 
    * @return boolean
    */
   public boolean isDrive() {
     return !(isCustom() || isSmart());
   }
   
   
   /**
    * This method deletes entirly content of the {@link Playlists.Type#LocalPlaylist} drive. Use this method carefully
    * <p>
    * Note: this action will be asynchronously performed by the server
    * </p>
    * 
    * @return {@code true} if everything went ok. {@code false} if not
    */
   public boolean clearContent() throws ServiceException, LoginException {
 
     List<NameValuePair> params = new ArrayList<NameValuePair>();
     params.add(new BasicNameValuePair("confirm", "YES"));
 
     Response response = this.getConnector(IConfiguration.Connectors.RAILS).put(this, "empty").send(false, params);
 
     return response.isOK();
   }
 
 
   /**
    * Use this method to get a {@link MediaFiles} instance containing all the {@code MD5} and {@code token} for media
    * files owned by this User. <br />
    * This method is useful for sync tools.
    * 
    * @return a {@link MediaFiles} instance
    * 
    * @throws ServiceException
    *           if any connection problem occurs.
    * @throws LoginException
    *           if any authentication problem occurs.
    */
   public MediaFiles getMediaFilesHashesMap(boolean async) throws ServiceException, LoginException {
     if ( MediaFile.Source.cloud.toString().equals(this.getSystemName()) || MediaFile.Source.local.toString().equals(this.getSystemName()) ) {
 
       MediaFiles mediaFiles = (MediaFiles) getConfiguration().getFactory().getEntity(MediaFiles.TAGNAME, getConfiguration());
 
       IConnector connector = this.getConnector(IConfiguration.Connectors.RAILS);
 
       IConnectionMethod request = connector.get(mediaFiles, this.getApiPath() + IConnector.URI_SEPARATOR + MediaFiles.NAMESPACE, "fingerprints", null);
       request.send(async);
 
       return mediaFiles;
     } else {
       throw new ServiceException("Only Cloud and Local playlists support this action: '" + this.getSystemName() + "' doesn't");
     }
   }
 
 
   /**
    * @return the playlist position index
    */
   public int getPosition() {
     return position;
   }
 
 
   /**
    * This method is used by response parser
    */
   public void setPosition(int position) {
     this.position = position;
   }
 
 
   /**
    * @return the playlist type
    */
   public String getType() {
     return type;
   }
 
 
   /**
    * This method is used by response parser
    */
   public void setType(String type) {
     this.type = type;
   }
 
 
   /**
    * @return the playlist mediafiles count
    */
   public long getMediaFilesCount() {
     return media_files_count;
   }
 
 
   /**
    * This method is used by response parser
    */
   public void setMediaFilesCount(long media_files_count) {
     this.media_files_count = media_files_count;
   }
 
 
   /**
    * @return {@code true} if this Playlist the {@code offline} special playlist
    */
   public boolean isOffline() {
     return OFFLINE_SYSTEM_NAME.equals(this.getSystemName());
   }
 
 
   /**
    * @return {@code true} if Playlist can be synced. {@code false} or not
    */
   public boolean isSyncable() {
     return this.syncable;
   }
 
 
   /**
    * This method is used by response parser
    */
   public void setSyncable(boolean syncable) {
     this.syncable = syncable;
   }
 
 
   /**
    * @return {@code true} if Playlist can be embedded on another site. {@code false} or not
    */
   public boolean isEmbeddable() {
     return embeddable;
   }
 
 
   /**
    * This method is used by response parser
    */
   public void setEmbeddable(boolean embeddable) {
     this.embeddable = embeddable;
   }
 
 
   /**
    * @return {@code true} if Playlist is visible on the Cloud Web Player. {@code false} or not
    */
   public boolean isVisible() {
     return visible;
   }
 
 
   /**
    * Enables or disables the visibility of this Playlist
    * 
    * @param visible
    */
   public void setVisible(boolean visible) {
     this.visible = visible;
   }
 
 
   /**
    * @return the last update time of this Playlist
    */
   public String getUpdatedAt() {
     return updated_at;
   }
 
 
   /**
    * This method is used by response parser
    */
   public void setUpdatedAt(String updated_at) {
     this.updated_at = updated_at;
   }
 
 
   /**
    * @return {@code true} if this Playlist is the last accessed playlist on the Cloud Web Player. {@code false} if not
    */
   public boolean isLastAccessed() {
     return last_accessed;
   }
 
 
   /**
    * This method is used by response parser
    */
   public void setLastAccessed(boolean last_accessed) {
     this.last_accessed = last_accessed;
   }
 
 
   /**
    * Returns a {@link MediaFiles} instance ready to be populated through {@link MediaFiles#load(boolean)} method
    * 
    * @return a {@link MediaFiles} instance
    */
   public MediaFiles getMediaFiles() {
     if ( this.mediafiles == null ) {
       this.mediafiles = (MediaFiles) getConfiguration().getFactory().getEntity(MediaFiles.TAGNAME, getConfiguration());
       this.mediafiles.setParent(this);
     }
     return this.mediafiles;
   }
 
 
   /**
    * Returns a {@link Albums} instance ready to be populated through {@link Albums#load(boolean)} method
    * 
    * @return a {@link Albums} instance
    */
   public Albums getAlbums() {
     if ( this.albums == null ) {
       this.albums = (Albums) getConfiguration().getFactory().getEntity(Albums.TAGNAME, getConfiguration());
       this.albums.setParent(this);
     }
     return this.albums;
   }
 
 
   public Method getSetterMethod(String tagName) {
     if ( setterMethods.containsKey(tagName) ) {
       return setterMethods.get(tagName);
     }
     return null;
   }
   
   
   public Map<String, Method> getGetterMethods() {
     if ( getterMethods == null ) {
       getterMethods = new HashMap<String, Method>();
       try {  
         getterMethods.put(TOKEN, Playlist.class.getMethod("getToken"));
         getterMethods.put(NAME, Playlist.class.getMethod("getName"));
         getterMethods.put(POSITION, Playlist.class.getMethod("getPosition"));
         getterMethods.put(TYPE, Playlist.class.getMethod("getType"));
         getterMethods.put(MEDIA_FILES_COUNT, Playlist.class.getMethod("getMediaFilesCount"));
         getterMethods.put(UPDATED_AT, Playlist.class.getMethod("getUpdatedAt"));
         getterMethods.put(LAST_ACCESSED, Playlist.class.getMethod("isLastAccessed"));
         getterMethods.put(SYSTEM_NAME, Playlist.class.getMethod("getSystemName"));
         getterMethods.put(EMBEDDABLE, Playlist.class.getMethod("isEmbeddable"));
         getterMethods.put(VISIBLE, Playlist.class.getMethod("isVisible"));
         getterMethods.put(SYNCABLE, Playlist.class.getMethod("isSyncable"));
       } catch (SecurityException e) {
         log.error("Security error", e);
       } catch (NoSuchMethodException e) {
         log.error("No method found", e);
       }
     }
     
     return getterMethods;
   }
   
 
 
   /**
    * Copies data from given {@link Playlist} passed as {@link IEntity}
    */
   protected void copy(IEntity entity) {
 
     Playlist pl = (Playlist) entity;
 
     this.name = pl.getName();
     this.position = pl.getPosition();
     this.type = pl.getType();
     this.media_files_count = pl.getMediaFilesCount();
     this.updated_at = pl.getUpdatedAt();
     this.last_accessed = pl.isLastAccessed();
     this.embeddable = pl.isEmbeddable();
     this.visible = pl.isVisible();
     this.system_name = pl.getSystemName();
     this.syncable = pl.isSyncable();
 
     this.setChanged();
     Event event = new Event(this, Event.States.ENTITY_REFRESHED);
     this.notifyObservers(event);
 
   }
 
 
   /**
    * This method delete this {@link Playlist} <br/>
    * <b>This method cannot be reverted</b>
    * 
    * @return {@code true} if everything went ok. {@code false} if not.
    * 
    * @throws ServiceException
    *           if any connection error occurrs
    * @throws LoginException
    *           if any login error occurrs
    */
   public boolean destroy() throws ServiceException, LoginException {
     if ( this.getToken() == null || "".equals(this.getToken()) ) {
       // playlists has not token
       return false;
     }
 
     if ( this.isDrive() ) {
       throw new ServiceException("Drive cannot be destroyed");
     }
 
     String path = IConnector.URI_SEPARATOR + Playlists.NAMESPACE;
     String action = IConnector.URI_SEPARATOR + this.getToken();
 
     IConnectionMethod request = this.getConnector(IConfiguration.Connectors.RAILS).delete(this, path, action, null);
     Response response = request.send(false);
 
     boolean result = response.isOK();
 
     if ( result && this.getParent() != null ) {
       ((Playlists) this.getParent()).remove(this);
     }
 
     return result;
   }
 
 
   /**
    * This method adds given media files to this playlist <br/>
    * Playlist should be an already existing playlist with a valid {@code token}
    * 
    * @param mediaFiles
    *          list of mediafiles to be added to this playlist.
    * @return {@code true} if everything went ok. {@code false} if not.
    * 
    * @throws ServiceException
    *           if any connection error occurrs
    * @throws LoginException
    *           if any login error occurrs
    */
   public boolean addMediaFiles(List<MediaFile> mediaFiles) throws ServiceException, LoginException {
 
     if ( this.getToken() == null || "".equals(this.getToken()) ) {
       // playlists has not token
       return false;
     }
 
     if ( !this.isCustom() ) {
       throw new ServiceException("You can add MediaFiles in CustomPlaylist only");
     }
 
     String path = IConnector.URI_SEPARATOR + Playlists.NAMESPACE + IConnector.URI_SEPARATOR + this.getToken();
     String action = MediaFiles.NAMESPACE + IConnector.URI_SEPARATOR + "add";
 
     IConnectionMethod request = this.getConnector(IConfiguration.Connectors.RAILS).post(this, path, action);
 
     List<NameValuePair> tokens = new ArrayList<NameValuePair>();
     for ( MediaFile mf : mediaFiles ) {
       if ( mf.getToken() != null ) {
         tokens.add(new BasicNameValuePair(MediaFiles.TOKENS_PARAMETER, mf.getToken()));
       }
     }
 
     Response response = request.send(false, tokens);
     return response.isOK();
   }
 
 
   /**
    * This method removes given media files from this playlist <br/>
    * Playlist should be an already existing playlist with a valid {@code token} <br />
    * <i>Note: this method also remove MediaFiles from the internal media files collection</i> <br />
    * <b>Do not pass a large {@code mediaFiles} collection</b>
    * 
    * @param mediaFiles
    *          list of mediafiles to be removed from this playlist.
    * @return {@code true} if everything went ok. {@code false} if not.
    * 
    * @throws ServiceException
    *           if any connection error occurrs
    * @throws LoginException
    *           if any login error occurrs
    */
   public boolean removeMediaFiles(List<MediaFile> mediaFiles) throws ServiceException, LoginException {
 
     if ( this.getToken() == null || "".equals(this.getToken()) ) {
       // playlists has not token
       return false;
     }
 
     if ( !this.isCustom() ) {
       throw new ServiceException("You can remove MediaFiles from CustomPlaylist only");
     }
 
     String path = IConnector.URI_SEPARATOR + Playlists.NAMESPACE + IConnector.URI_SEPARATOR + this.getToken();
     String action = MediaFiles.NAMESPACE + IConnector.URI_SEPARATOR + "remove";
 
     List<String> str_tokens = new ArrayList<String>();
     List<NameValuePair> tokens = new ArrayList<NameValuePair>();
     for ( MediaFile mf : mediaFiles ) {
       if ( mf.getToken() != null ) {
         tokens.add(new BasicNameValuePair(MediaFiles.TOKENS_PARAMETER, mf.getToken()));
         str_tokens.add(mf.getToken());
       }
     }
     IConnectionMethod request = this.getConnector(IConfiguration.Connectors.RAILS).delete(this, path, action, tokens);
     Response response = request.send(false);
     boolean result = response.isOK();
 
     if ( result && this.getMediaFiles().isLoaded() ) {
       for ( String token : str_tokens ) {
         this.getMediaFiles().remove(token);
       }
     }
     return response.isOK();
   }
 
 
   public IConnectionMethod load(boolean async) throws ServiceException, LoginException {
     return this.load(false, null);
   }
 
 
   public IConnectionMethod load(boolean async, IResponseHandler responseHandler) throws ServiceException, LoginException {
     IConnectionMethod request = getConnector(IConfiguration.Connectors.RAILS).get(this, null, null);
     request.send(async, null, responseHandler);
     return request;
   }
 
 
   public String getApiPath() {
     return this.getParent().getApiPath() + IConnector.URI_SEPARATOR + this.getToken();
   }
 
 
   protected List<NameValuePair> toQueryParameters(boolean all) {
     String prefix = TAGNAME + "[";
     String suffix = "]";
 
     List<NameValuePair> params = new ArrayList<NameValuePair>();
 
     params.add(new BasicNameValuePair(prefix + NAME + suffix, this.name));
     if ( all ) {
       params.add(new BasicNameValuePair(prefix + POSITION + suffix, String.valueOf(this.position)));
       params.add(new BasicNameValuePair(prefix + TYPE + suffix, this.type));
       params.add(new BasicNameValuePair(prefix + MEDIA_FILES_COUNT + suffix, String.valueOf(this.media_files_count)));
       params.add(new BasicNameValuePair(prefix + UPDATED_AT + suffix, this.updated_at));
       params.add(new BasicNameValuePair(prefix + LAST_ACCESSED + suffix, String.valueOf(this.last_accessed)));
       params.add(new BasicNameValuePair(prefix + SYSTEM_NAME + suffix, this.system_name));
       params.add(new BasicNameValuePair(prefix + EMBEDDABLE + suffix, String.valueOf(this.embeddable)));
       params.add(new BasicNameValuePair(prefix + VISIBLE + suffix, String.valueOf(this.visible)));
       params.add(new BasicNameValuePair(prefix + SYNCABLE + suffix, String.valueOf(this.syncable)));
     }
 
     return params;
   }
 
 }
