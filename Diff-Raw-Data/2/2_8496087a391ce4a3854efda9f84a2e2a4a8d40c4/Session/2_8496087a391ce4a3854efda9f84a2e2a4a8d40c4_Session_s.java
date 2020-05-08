 
 package cc.rainwave.android.api;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.util.Log;
 import cc.rainwave.android.R;
 import cc.rainwave.android.Rainwave;
 import cc.rainwave.android.api.types.Album;
 import cc.rainwave.android.api.types.Artist;
 import cc.rainwave.android.api.types.Event;
 import cc.rainwave.android.api.types.RainwaveException;
 import cc.rainwave.android.api.types.Song;
 import cc.rainwave.android.api.types.SongRating;
 import cc.rainwave.android.api.types.Station;
 import cc.rainwave.android.api.types.User;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonParseException;
 import com.google.gson.JsonParser;
 import com.google.gson.stream.JsonReader;
 
 public class Session {
     private static final String TAG = "Session";
 
     private Context mContext;
 
     private int mStation = 1;
 
     private String mUserId;
 
     private String mKey;
 
     private URL mBaseUrl;
 
     private Bitmap mCurrentAlbumArt;
 
     private Event mCurrentEvent;
 
     private Event[] mNextEvents;
 
     private Event[] mEventHistory;
 
     private Song[] mRequests;
 
     private User mUser;
 
     private Station[] mStations;
 
     private Artist[] mArtists;
 
     private Album[] mAlbums;
 
     private int mLastVoteId = -1;
 
     /** Estimate of time delta in seconds between server time and local time. */
     private long mDrift = 0;
 
     /** Can't instantiate directly */
     private Session() { }
 
     public void info() throws RainwaveException {
         final String path = "info";
         try {
             final JsonElement element = get(path);
             updateSchedules(element);
         }
         catch(final JsonParseException exc) {
             throw wrapException(exc, path);
         }
     }
 
     public void sync() throws RainwaveException {
         final String path = "sync";
 
         // include a last known event id so the server can rush information to
         // us if we're behind
         String[] args = null;
         if(getCurrentEvent() != null) {
             args = new String[] {"known_event_id", String.valueOf(getCurrentEvent().getId())};
         }
         else {
             args = new String[0];
         }
 
         try {
             final JsonElement element = post(path, args);
             updateSchedules(element);
         }
         catch(final JsonParseException exc) {
             throw wrapException(exc, path);
         }
     }
 
     /** Update schedules if we are still listening to their updates. */
     private void updateSchedules(final JsonElement root) {
         Event newCurrent = getIfExists(root, "sched_current", Event.class, mCurrentEvent);
 
         // It's possible that someone could have changed the station and this is
         // and update for a previous station we don't need any more.
         //
         // FIXME: This is pretty bad practice -- is there another way?
         if(newCurrent.getStationId() != getStationId()) {
             return;
         }
 
         mCurrentEvent = newCurrent;
         mNextEvents = getIfExists(root, "sched_next", Event[].class, mNextEvents);
         mEventHistory = getIfExists(root, "sched_history", Event[].class, mEventHistory);
         mUser = getIfExists(root, "user", User.class, mUser);
         mRequests = getIfExists(root, "requests", Song[].class, mRequests);
 
         // This does some checking to see if the "last_vote" reported by the api actually belongs
         // to the current election. If it does, it accepts the ID, otherwise it is set to -1.
         if(JsonHelper.hasMember(root, "vote_result")) {
             final JsonElement child = JsonHelper.getChild(root, "vote_result");
             final int elecId = JsonHelper.getInt(child, "elec_id");
 
             if(elecId != mCurrentEvent.getId()) {
                 mLastVoteId = JsonHelper.getInt(JsonHelper.getChild(root, "vote_result"), "entry_id");
             }
             else {
                 mLastVoteId = -1;
             }
         }
     }
 
     private static <T> T getIfExists(final JsonElement root, final String name, Class<T> classOfT, T defaultValue) {
         if(JsonHelper.hasMember(root, name)) {
             final Gson gson = getGson();
             return gson.fromJson(JsonHelper.getChild(root, name), classOfT);
         }
         return defaultValue;
     }
 
     public SongRating rateSong(int songId, float rating)
     throws RainwaveException {
         final String path = "rate";
         final String returns = "rate_result";
 
         rating = Math.max(1.0f, Math.min(rating, 5.0f));
 
         return requestObject(Method.POST, path, returns, true, SongRating.class,
             "song_id", String.valueOf(songId),
             "rating", String.valueOf(rating)
         );
     }
 
     public void vote(int elecId)
     throws RainwaveException {
         final String path = "vote";
         final String returns = "vote_result";
 
         try {
             final JsonElement element = post(path,
                 "entry_id", String.valueOf(elecId)
             );
             checkError(JsonHelper.getChild(element, returns));
         }
         catch(final JsonParseException exc) {
             throw wrapException(exc, path);
         }
     }
 
     public Station[] fetchStations() throws RainwaveException {
         final String path = "stations";
         final String returns = "stations";
 
         if(hasStations()) {
             return cloneStations();
         }
 
         mStations = requestObject(Method.POST, path, returns, false, Station[].class);
         return cloneStations();
 
     }
 
     /**
      * Fetch a list of all  the albums. Returns a cached version if available.
      * @return array of Albums
      * @throws RainwaveException in case of a problem understanding the response
      */
     public Album[] fetchAlbums() throws RainwaveException {
         final String path = "all_albums";
         final String returns = "all_albums";
         mAlbums = requestObject(Method.POST, path, returns, false, Album[].class);
         return mAlbums;
     }
 
     /**
      * Get album data.
      * 
      * @return all albums if cached, or null otherwise
      */
     public Album[] getAlbums() {
         return mAlbums;
     }
 
     /**
      * Fetch a list of all artists.
      * 
      * @return a list of all artists
      * @throws RainwaveException in case of problem understanding the response
      */
     public Artist[] fetchArtists() throws RainwaveException {
         final String path = "all_artists";
         final String returns = "all_artists";
         mArtists = requestObject(Method.POST, path, returns, false, Artist[].class);
         return mArtists;
     }
 
     /**
      * Get artist data.
      * 
      * @return all artist data if cached, or null otherwise
      */
     public Artist[] getArtists() {
         return mArtists;
     }
 
     public Artist fetchDetailedArtist(int artist_id) throws RainwaveException {
         final String path = "artist";
         final String returns = "artist";
 
         return requestObject(Method.POST, path, returns, false, Artist.class,
             "id", String.valueOf(artist_id)
         );
     }
 
     public Album fetchDetailedAlbum(int album_id) throws RainwaveException {
         final String path = "album";
         final String returns = "album";
 
         return requestObject(Method.POST, path, returns, false, Album.class,
             "id", String.valueOf(album_id)
         );
     }
 
     public Song[] submitRequest(int song_id) throws RainwaveException {
         final String path = "request";
 
         final JsonElement root = post(path, "song_id", String.valueOf(song_id));
         try {
             final Gson gson = getGson();
             checkError(JsonHelper.getChild(root, "request_result"));
             return gson.fromJson(JsonHelper.getChild(root, "requests"), Song[].class);
         }
         catch(final JsonParseException exc) {
             throw wrapException(exc, path);
         }
     }
 
     /**
      * Fetch a full resolution album art.
      * 
      * @param path base url to album art
      * @return bitmap of album art
      * @throws IOException
      */
     public Bitmap fetchAlbumArt(String path) throws IOException {
         return fetchAlbumArtHelper(path + ".jpg");
     }
 
     /**
      * Fetch a minimum width album art. The returned bitmap is guaranteed to
      * be at least the requested width.
      * 
      * @param path base url to album art
      * @param width minimum width required
      * @return bitmap of album art
      * @throws IOException
      */
     public Bitmap fetchAlbumArt(String path, int width) throws IOException {
         if(width <= 120) {
             return fetchAlbumArtHelper(path + "_120.jpg");
         }
         else if(width <= 240) {
             return fetchAlbumArtHelper(path + "_240.jpg");
         }
         else {
             return fetchAlbumArt(path);
         }
     }
 
     private Bitmap fetchAlbumArtHelper(String path) throws IOException {
         URL url = new URL(getUrl(path));
         Log.d(TAG, "GET " + url.toString());
         InputStream is = url.openStream();
         mCurrentAlbumArt = BitmapFactory.decodeStream(is);
         return mCurrentAlbumArt;
     }
 
     /**
      * Returns the current album art.
      * @return album art bitmap if any, null otherwise
      */
     public Bitmap getCurrentAlbumArt() {
         return mCurrentAlbumArt;
     }
 
     /**
      * Clears the current album art. Sets it to null.
      */
     public void clearCurrentAlbumArt() {
         mCurrentAlbumArt = null;
     }
 
     public Song[] reorderRequests(Song requests[])
             throws RainwaveException {
         final String path = "order_requests";
 
         final JsonElement root = post(path,
             "order", Rainwave.makeRequestQueueString(requests)
         );
         try {
             final Gson gson = getGson();
             checkError(JsonHelper.getChild(root, "order_requests_result"));
             return gson.fromJson(JsonHelper.getChild(root, "requests"), Song[].class);
         }
         catch(final JsonParseException exc) {
             throw wrapException(exc, path);
         }
     }
 
     public void deleteRequest(Song request)
     throws RainwaveException {
         post("delete_request", 
             "song_id", String.valueOf(request.getId())
         );
     }
 
     public void setUserInfo(String userId, String key) {
         mUserId = userId;
         mKey = key;
     }
 
     public void clearUserInfo() {
         setUserInfo(null, null);
     }
 
     public Song[] cloneRequests() {
         return mRequests.clone();
     }
 
     public boolean hasStations() {
         return mStations != null;
     }
 
     public boolean hasLastVote() {
         return mLastVoteId >= 0;
     }
 
     public int getLastVoteId() {
         return mLastVoteId;
     }
 
     /**
      * Get the difference in seconds between server time and client time. That
      * is, serverTime - clientTime.
      * 
      * @return the drift
      */
     public long getDrift() {
         return mDrift;
     }
 
     public Station[] cloneStations() {
         return mStations.clone();
     }
 
     public void setStation(int stationId) {
         mStation = stationId;
         Rainwave.putLastStation(mContext, stationId);
     }
 
     public Station getStation(int stationId) {
         for(int i = 0; i < mStations.length; i++) {
             if(mStations[i].getId() == stationId) {
                 return mStations[i];
             }
         }
         return null;
     }
 
     public String getUrl() {
         return mBaseUrl.toString();
     }
 
     public int getStationId() {
         return mStation;
     }
 
     public Event getCurrentEvent() {
         return mCurrentEvent;
     }
 
     public Event getNextEvent() {
         return mNextEvents[0];
     }
 
     public boolean isTunedIn() {
         return mUser != null && mUser.getTunedIn();
     }
 
     /**
      * Returns true if the server sent a request list on the last sync() or info().
      * @return
      */
     public boolean hasRequests() {
         return mRequests != null;
     }
 
     /**
      * Returns true if we have set credentials via setUserInfo().
      * @return
      */
     public boolean hasCredentials() {
         return mUserId != null && mKey != null && mUserId.length() > 0 && mKey.length() > 0;
     }
 
     /**
      * Returns true if the server thinks we are authenticated.
      * 
      * @return true if authenticated, false otherwise
      */
     public boolean isAuthenticated() {
         return mUser != null;
     }
 
     /**
      * Returns true if we require a sync. This can be because no current
      * event is available or the current event is in the past.
      * 
      * @return true if a sync is required, false otherwise
      */
     public boolean requiresSync() {
         if(getCurrentEvent() == null) {
             return true;
         }
 
         long endTime = getCurrentEvent().getEnd() - getDrift();
         long utc = System.currentTimeMillis() / 1000;
         return utc > endTime;
     }
 
     private JsonElement get(String path, String... params)
     throws RainwaveException {
         return request(Method.GET, path, params);
     }
 
     private JsonElement post(String path, String... params)
     throws RainwaveException {
         return request(Method.POST, path, params);
     }
 
     private JsonElement request(final Method method, String path, String... params)
     throws RainwaveException {
         // Construct arguments
         Arguments httpArgs = new Arguments(params);
         httpArgs.put(NAME_STATION, String.valueOf(mStation));
 
         if(this.hasCredentials()) {
           httpArgs.put(NAME_USERID, mUserId);
           httpArgs.put(NAME_KEY, mKey);
         }
 
         HttpURLConnection conn = null;
         final Resources r = mContext.getResources();
         long requestEnd;
 
         try {
             switch(method) {
             case POST:
                 conn = HttpHelper.makePost(mBaseUrl, path, httpArgs.encode());
                 break;
             case GET:
                 conn = HttpHelper.makeGet(mBaseUrl, String.format("%s?%s", path, httpArgs.encode()));
                 break;
             default:
                 throw new IllegalArgumentException("Unhandled HTTP method!");
             }
 
             final int statusCode = conn.getResponseCode();
 
             switch(statusCode) {
             case HttpURLConnection.HTTP_FORBIDDEN:
                 throw new RainwaveException(r.getString(R.string.msg_forbidden), statusCode);
             }
             
             // log timestamp to calculate drift
             requestEnd = System.currentTimeMillis() / 1000;
         }
         catch(IOException exc) {
             throw new RainwaveException(r.getString(R.string.msg_genericError), exc);
         }
 
         final JsonElement root;
         try {
             JsonParser parser = new JsonParser();
             final InputStream is = conn.getInputStream();
             final InputStreamReader reader = new InputStreamReader(is);
             root = parser.parse(reader);
         }
         catch(IOException exc) {
             throw new RainwaveException(r.getString(R.string.msg_genericError), exc);
         }
 
         // most every endpoint returns api_info, so we can try and update
         // drift whenever possible
         if(JsonHelper.hasMember(root, "api_info")) {
             final JsonElement api_info = JsonHelper.getChild(root, "api_info");
             if(JsonHelper.hasMember(api_info, "time")) {
                 mDrift = JsonHelper.getLong(api_info, "time") - requestEnd;
             }
         }
 
         return root;
     }
 
     /**
      * Fetches a single object from the API.
      * 
      * @param method either Method.GET or Method.POST
      * @param path endpoint name
      * @param name member name 
      * @param checkError check for the success in a member named "success"
      * @param classOfT
      * @param params
      * @return
      * @throws RainwaveException
      * @throws IOException
      */
     private <T> T requestObject(
         final Method method, final String path, final String name,
         final boolean checkError, Class<T> classOfT, final String... params
     ) throws RainwaveException {
         // Convert the json into Java objects.
         Gson gson = getGson();
         final JsonElement json = request(method, path, params);
 
         try {
             final JsonElement element = JsonHelper.getChild(json, name);
             if(checkError) {
                 checkError(element);
             }
             return gson.fromJson(element, classOfT);
         }
         catch(JsonParseException e) {
             throw wrapException(e, path);
         }
     }
 
     /**
      * Check for errors in a JSON response. Checks the value of a member
      * name called "success" and if it is false, throw an error containing
      * the contents of a field called "text".
      * 
      * @param element JSON object to check
      * @throws RainwaveException if an error is detected
      */
     private void checkError(final JsonElement element) throws RainwaveException {
         if(!JsonHelper.getBoolean(element, "success")) {
             throw new RainwaveException(
                 JsonHelper.getString(element, "text", mContext.getString(R.string.msg_genericError)),
                 RainwaveException.STATUS_UNKNOWN,
                 JsonHelper.getInt(element, "code", RainwaveException.ERROR_UNKNOWN)
                );
         }
     }
 
     private RainwaveException wrapException(final JsonParseException exc, final String path)
     throws RainwaveException {
         Resources r = mContext.getResources();
         String msg = String.format(r.getString(R.string.msgfmt_parseError), path, exc.getMessage());
         throw new RainwaveException(msg, exc);
     }
 
     private String getUrl(String path) throws MalformedURLException {
         if (path == null || path.length() == 0)
             return mBaseUrl.toString();
         final URL url = new URL(mBaseUrl, path);
         return url.toString();
     }
 
 
     /**
      * Restore a session from a previously saved one.
      * 
      * @param ctx the new context for the session
      */
     public void unpickle(Context ctx) {
         mContext = ctx;
         mStation = Rainwave.getLastStation(ctx, mStation);
         setUserInfo(Rainwave.getUserId(ctx), Rainwave.getKey(ctx));
 
         try {
             mBaseUrl = new URL(Rainwave.getUrl(ctx));
         } catch (MalformedURLException e) {
             mBaseUrl = Rainwave.DEFAULT_URL;
         }
     }
 
     public void pickle(Context ctx) {
         mContext = ctx;
 
         Rainwave.putLastStation(mContext, mStation);
         Rainwave.putUserId(mContext, mUserId);
         Rainwave.putKey(mContext, mKey);
         // TODO: Rainwave.putUrl() ?
     }
 
     /** The singleton */
     private static Session sInstance;
 
     public static Session getInstance() {
         if(sInstance == null) {
             sInstance = new Session();
         }
         return sInstance;
     }
 
     private static Gson getGson() {
         GsonBuilder builder = new GsonBuilder();
         builder.registerTypeAdapter(Album.class, new Album.Deserializer());
         builder.registerTypeAdapter(Song.class, new Song.Deserializer());
         builder.registerTypeAdapter(Event.class, new Event.Deserializer());
         builder.registerTypeAdapter(Artist.class, new Artist.Deserializer());
         builder.registerTypeAdapter(User.class, new User.Deserializer());
         builder.registerTypeAdapter(Station.class, new Station.Deserializer());
         builder.registerTypeAdapter(SongRating.class, new SongRating.Deserializer());
         builder.registerTypeAdapter(SongRating.AlbumRating.class, new SongRating.AlbumRating.Deserializer());
         return builder.create();
     }
 
     private static enum Method {
         GET, POST
     }
 
     public static final String
             NAME_STATION = "sid",
             NAME_USERID = "user_id",
             NAME_KEY = "key";
 }
