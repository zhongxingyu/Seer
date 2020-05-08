 
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
 
 import org.apache.http.client.methods.HttpGet;
 
 import fm.audiobox.core.api.Model;
 import fm.audiobox.core.api.ModelItem;
 import fm.audiobox.core.api.ModelsCollection;
 import fm.audiobox.core.exceptions.LoginException;
 import fm.audiobox.core.exceptions.ModelException;
 import fm.audiobox.core.exceptions.ServiceException;
 import fm.audiobox.core.interfaces.CollectionListener;
 import fm.audiobox.core.models.AudioBoxClient.AudioBoxConnector;
 
 
 /**
  * User model is a special {@link Model} just because almost every library browse action is performed through this
  * object.
  *
  * <p>
  *
  * When a login is successfully performed an XML like the following is received and parsed:
  * 
  * <pre>
  * {@code
  * <user>
  *   <bytes-served>123456</bytes-served>
  *   <email>user@example.com</email>
  *   <play-count type="integer">1042</play-count>
  *   <quota>984354165</quota>
  *   <state>active</state>
  *   <tracks-count type="integer">1490</tracks-count>
  *   <username>Username</username>
  *   <available-storage type="integer">1232321123</available-storage>
  *   <avatar-url>http://url.to.avatar/avatar.png</avatar-url>
  *   <profile>
  *      <autoplay type="boolean">false</autoplay>
  *      <birth-date type="date">1970-01-01</birth-date>
  *      <country>US</country>
  *      <gender>m</gender>
  *      <home-page>http://www.myblog.com</home-page>
  *      <real-name>Real User Name</real-name>
  *      <time-zone>New York</time-zone>
  *   </profile>
  * </user>
  *
  * }
  * </pre>
  *
  * Through the User object you have access to its library that can be browsed by:
  * <ul>
  *  <li>Playlists</li>
  *  <li>Genres</li>
  *  <li>Artists</li>
  *  <li>Albums</li>
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
  * Artists artists = user.getPlaylists();
  * Artists artist = artists.get( "token" or "index" );
  * Tracks trs = artist.getTracks();
  * </pre>
  * 
  * Or you can get informations about a specific, UUID known track's, by calling {@link User#getTrackByUuid(String)}
  * 
  * @author Valerio Chiodino
  * @author Fabio Tunno
  * @version 0.0.1
  */
 public class User extends ModelItem {
 
     /** The XML tag name for the User element */
     public static final String TAG_NAME = "user";
 
     /** User API end point */
     public static final String END_POINT = TAG_NAME;
 
     /** Active user state */
     public static final String ACTIVE_STATE = "active";
 
     protected long bytesServed;
     protected String email;
     protected int playCount;
     protected long quota;
     protected String state;
     protected int tracksCount;
     protected String username;
     protected String password;
     protected long availableStorage;
     protected String avatarUrl;
     protected Profile profile;
 
     // User's collection relations
     protected Playlists playlists;
     protected Genres genres;
     protected Artists artists;
     protected Albums albums;
 
     private String[] md5Hashes;
 
     /**
      * <p>Constructor for User.</p>
      */
     protected User() {
         this.pEndPoint = END_POINT;
         this.pName = TAG_NAME;
     }
 
 
     /* ------------------- */
     /* Getters and setters */
     /* ------------------- */
     
 
     /**
      * <p>Setter for the user bytes served: used by the parser.</p>
      *
      * @param bytes the String value of the bytes served.
      */
     public void setBytesServed(String bytes) {
         this.bytesServed = Long.parseLong( bytes );
     }
 
     /**
      * <p>Getter for the user bytes served.</p>
      *
      * @return the user bytes served
      */
     public long getBytesServed() {
         return this.bytesServed;
     }
 
 
 
     /**
      * <p>Setter for the user email: used by the parser.<p>
      *
      * @param email the user email
      */
     public void setEmail(String email) {
         this.email = email;
     }
 
     /**
      * <p>Getter for the user email.</p>
      *
      * @return the user email
      */
     public String getEmail() {
         return this.email;
     }
 
 
 
     /**
      * <p>Setter for the user played song count: used by the parser.<p>
      *
      * @param playCount the String value of the plays count.
      */
     public void setPlayCount(String playCount) {
         this.playCount = Integer.parseInt( playCount );
     }
 
     /**
      * <p>Getter for the user played song count.</p>
      *
      * @return the user plays count
      */
     public int getPlayCount() {
         return this.playCount;
     }
 
 
 
     /**
      * <p>Setter for the user quota in bytes: used by the parser.</p>
      *
      * @param quota the String representing the user quota bytes
      */
     public void setQuota(String quota) {
         this.quota = Long.parseLong( quota );
     }
 
     /**
      * <p>Getter for the user quota bytes.</p>
      *
      * @return the user quota bytes
      */
     public long getQuota() {
         return this.quota;
     }
 
 
 
     /**
      * <p>Setter for the user state: used by the parser.</p>
      *
      * @param state the user state.
      */
     public void setState(String state) {
         this.state = state;
     }
 
     /**
      * <p>Getter for the user state.</p>
      *
      * @return the user state
      */
     public String getState() {
         return this.state;
     }
 
 
 
     /**
      * <p>Setter for the user total tracks count: used by the parser..</p>
      *
      * @param tracksCount the user total tracks count.
      */
     public void setTracksCount(String tracksCount) {
         this.tracksCount = Integer.parseInt( tracksCount );
     }
 
     /**
      * <p>Getter for the user total tracks count.</p>
      *
      * @return the user total tracks count
      */
     public int getTracksCount() {
         return this.tracksCount;
     }
 
 
     /**
      * <p>Setter for the user nickname: used by the parser.</p>
      *
      * @param username the user nickname
      */
     public void setUsername(String username) {
         this.username = username;
     }
 
     /**
      * <p>Getter for the nickname.</p>
      *
      * @return the user nickname
      */
     public String getUsername() {
         return this.username;
     }
 
 
 
     /**
      * <p>Setter for the user password.</p>
      *
      * @param password the user password
      */
     public void setPassword(String password) {
         this.password = password;
     }
 
 
 
     /**
      * <p>Setter for the user remote available storage: used by the parser.</p>
      *
      * @param availableStorage a {@link String} representing numbers of available storage bytes.
      */
     public void setAvailableStorage(String availableStorage) {
         this.availableStorage = Long.parseLong( availableStorage );
     }
 
     /**
      * <p>Getter for the user remote available storage.</p>
      *
      * @return the user available storage
      */
     public long getAvailableStorage() {
         return this.availableStorage;
     }
 
 
 
     /**
      * <p>Setter for the user avatar image link: used by the parser.</p>
      *
      * @param url the url of the user avatar
      */
     public void setAvatarUrl(String url) {
         this.avatarUrl = url;
     }
 
     /**
      * <p>Getter for user avatar url.</p>
      *
      * @return the user avatar url
      */
     public String getAvatarUrl() {
         return this.avatarUrl;
     }
 
 
 
     /**
      * <p>Setter for the user profile: used by the parser.</p>
      *
      * @param profile the user {@link Profile} object.
      */
     public void setProfile(Profile profile) {
         this.profile = profile;
     }
 
     /**
      * <p>Getter for the user {@link Profile}.</p>
      *
      * @return the user profile
      */
     public Profile getProfile() {
         return this.profile;
     }
 
 
     /* ------------------- */
     /* Collection Browsing */
     /* ------------------- */
 
 
     /**
      * Given a known track UUID this method will requests AudioBox.fm and returns a valid {@link Track} object.
      *
      * @param uuid the UUID of the track you are asking for.
      * 
      * @return the requested track if it exists.
      * 
      * @throws LoginException if user has not been authenticated
      * @throws ServiceException if the requested resource doesn't exists or any other ServiceException occur.
      * @throws ModelException 
      */
     public Track getTrackByUuid(String uuid) throws ServiceException, LoginException, ModelException {
     	Track t = this.newTrack();
         t.setUuid(uuid);
         t.refresh();
         return t;
     }
 
 
     /**
     * Instantiates a new Track. Usually this method is used to upload a track
      * 
      * @return
      * @throws ServiceException
      * @throws LoginException
      * @throws ModelException
      */
     public Track newTrack() throws ServiceException, LoginException, ModelException {
     	return (Track) AudioBoxClient.getModelInstance( AudioBoxClient.NEW_TRACK_KEY , this.getConnector() );
     }
     
     
     
     /**
      * Use this method to get the {@link Playlists} user collection.
      * 
      * <p>
      * 
      * This method accept the parameter <code>async</code>. If <code>true</code> the collection is populated 
      * asynchronously; in this case it may be necessary to specify a {@link CollectionListener} to keep track 
      * of what is happening to the collection.
      *
      * @param async whether to make the request asynchronously.
      * 
      * @return the user {@link Playlists} collection
      * 
      * @throws ModelException if a custom model class was specified and an error while using it occurs.
      */
     public Playlists getPlaylists(boolean async) throws ModelException {
         this.playlists = (Playlists) AudioBoxClient.getModelInstance(AudioBoxClient.PLAYLISTS_KEY, this.getConnector());
         Thread t = populateCollection( Playlists.END_POINT, this.playlists );
         if (async)
             t.start();
         else
             t.run();
 
         return playlists;
     }
 
     /**
      * <p>Same as calling {@link User#getPlaylists(boolean) User.getPlaylists(false)}.</p>
      *
      * @return the user {@link Playlists} collection
      * 
      * @throws ModelException if a custom model was specified and an error while using occurs.
      */
     public Playlists getPlaylists() throws ModelException {
         return this.getPlaylists(false);
     }
 
 
 
     /**
      * Use this method to get the {@link Genres} user collection.
      * 
      * <p>
      * 
      * This method accept the parameter <code>async</code>. If <code>true</code> the collection is populated 
      * asynchronously; in this case it may be necessary to specify a {@link CollectionListener} to keep track 
      * of what is happening to the collection.
      *
      * @param async whether to make the request asynchronously.
      * 
      * @return the user {@link Genres} collection
      * 
      * @throws ModelException if a custom model class was specified and an error while using it occurs.
      */
     public Genres getGenres(boolean async) throws ModelException {
         this.genres = (Genres) AudioBoxClient.getModelInstance(AudioBoxClient.GENRES_KEY, this.getConnector());
         Thread t = populateCollection( Genres.END_POINT, this.genres );
         if (async)
             t.start();
         else
             t.run();
 
         return this.genres;
     }
 
     /**
      * <p>Same as calling {@link User#getGenres(boolean) User.getGenres(false)}.</p>
      *
      * @return the user {@link Genres} collection
      * 
      * @throws ModelException if a custom model was specified and an error while using occurs.
      */
     public Genres getGenres() throws ModelException {
         return this.getGenres(false);
     }
 
 
 
     /**
      * Use this method to get the {@link Artists} user collection.
      * 
      * <p>
      * 
      * This method accept the parameter <code>async</code>. If <code>true</code> the collection is populated 
      * asynchronously; in this case it may be necessary to specify a {@link CollectionListener} to keep track 
      * of what is happening to the collection.
      *
      * @param async whether to make the request asynchronously.
      * 
      * @return the user {@link Artists} collection
      * 
      * @throws ModelException if a custom model class was specified and an error while using it occurs.
      */
     public Artists getArtists(boolean async) throws ModelException {
         this.artists = (Artists) AudioBoxClient.getModelInstance(AudioBoxClient.ARTISTS_KEY, this.getConnector());
         Thread t = populateCollection( Artists.END_POINT, this.artists );
         if (async)
             t.start();
         else
             t.run();
 
         return this.artists;
     }
 
     /**
      * <p>Same as calling {@link User#getArtists(boolean) User.getArtists(false)}.</p>
      *
      * @return the user {@link Artists} collection
      * 
      * @throws ModelException if a custom model was specified and an error while using occurs.
      */
     public Artists getArtists() throws ModelException {
         return this.getArtists(false);
     }
 
    
 
     /**
      * Use this method to get the {@link Albums} user collection.
      * 
      * <p>
      * 
      * This method accept the parameter <code>async</code>. If <code>true</code> the collection is populated 
      * asynchronously; in this case it may be necessary to specify a {@link CollectionListener} to keep track 
      * of what is happening to the collection.
      *
      * @param async whether to make the request asynchronously.
      * 
      * @return the user {@link Albums} collection
      * 
      * @throws ModelException if a custom model class was specified and an error while using it occurs.
      */
     public Albums getAlbums(boolean async) throws ModelException {
         this.albums = (Albums) AudioBoxClient.getModelInstance(AudioBoxClient.ALBUMS_KEY, this.getConnector());
         Thread t = populateCollection( Albums.END_POINT, this.albums );
         if (async)
             t.start();
         else
             t.run();
 
         return this.albums;
     }
     
     /**
      * <p>Same as calling {@link User#getAlbums(boolean) User.getAlbums(false)}.</p>
      *
      * @return the user {@link Albums} collection
      * 
      * @throws ModelException if a custom model was specified and an error while using occurs.
      */
     public Albums getAlbums() throws ModelException {
         return this.getAlbums(false);
     }
     
     
 
     /**
      * Use this method to get a String array of MD5 hashes of user's already uploaded and ready media files.
      * 
      * <p>
      * 
      * This method is useful for sync tools.
      *
      * @return an array of {@link String} objects containing MD5 hashes of every user uploaded track.
      * 
      * @throws ServiceException if any connection problem to AudioBox.fm services occurs.
      * @throws LoginException if any authentication problem occurs.
      */
     public String[] getUploadedTracks() throws ServiceException, LoginException {
         
         String[] result = this.getConnector().execute(Tracks.END_POINT, null, null, this, HttpGet.METHOD_NAME, AudioBoxConnector.TEXT_FORMAT, false);
         String response = result[ AudioBoxConnector.RESPONSE_BODY ];
         
         result = response.split( ";" , response.length() );
         this.md5Hashes = new String[ result.length ];
         int pos = 0;
         for ( String hash : result )
             this.md5Hashes[ pos++ ] = hash.trim();
         
         return this.md5Hashes;
     }
 
 
     /* --------- */
     /* Overrides */
     /* --------- */
 
 
     /** {@inheritDoc} */
     @Override
     public String getName() {
         return this.getUsername();
     }
     
     
     /* --------------- */
     /* Private methods */
     /* --------------- */
     
     /**
      * This method is used to make asynchronous requests to AudioBox.fm collections API end points.
      *   
      * @param endpoint the collection API end point to make request to 
      * @param collection the collection ({@link ModelsCollection}) to populate
      */
     private Thread populateCollection(final String endpoint, final ModelsCollection collection) {
 
         // Final reference to user object
         final User user = this;
 
         return new Thread() {
 
             public void run() {
                 try {
                     user.getConnector().execute(endpoint, null, null, collection, null);
                 } catch (ServiceException e) {
                     e.printStackTrace();
                 } catch (LoginException e) {
                     e.printStackTrace();
                 }
             }
 
         };
 
     }
 
 }
