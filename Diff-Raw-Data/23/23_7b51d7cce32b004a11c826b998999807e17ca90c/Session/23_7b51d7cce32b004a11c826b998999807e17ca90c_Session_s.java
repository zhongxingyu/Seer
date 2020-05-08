 
 package cc.rainwave.android.api;
 
 import cc.rainwave.android.R;
 import cc.rainwave.android.Rainwave;
 import cc.rainwave.android.api.types.Album;
 import cc.rainwave.android.api.types.Artist;
 import cc.rainwave.android.api.types.GenericResult;
 import cc.rainwave.android.api.types.RainwaveException;
 import cc.rainwave.android.api.types.RainwaveResponse;
 import cc.rainwave.android.api.types.Song;
 import cc.rainwave.android.api.types.Station;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonParseException;
 import com.google.gson.JsonParser;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.Drawable;
 import android.util.Log;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 public class Session {
     private static final String TAG = "Session";
     
     private Context mContext;
 
     public int mStation = 1;
 
     public String mUserId;
 
     public String mKey;
 
     private URL mBaseUrl;
 
     /** Can't instantiate directly */
     private Session() {
     }
 
     public RainwaveResponse info()
             throws IOException, RainwaveException {
         return get("info");
     }
     
     public RainwaveResponse sync()
             throws IOException, RainwaveException {
         return post("sync");
     }
 
     public GenericResult rateSong(int songId, float rating)
     		throws IOException, RainwaveException {
     	rating = Math.max(1.0f, Math.min(rating, 5.0f));
     	return post("rate",
     			"song_id", String.valueOf(songId),
     			"rating", String.valueOf(rating)
     	).rate_result;
     }
     
     public GenericResult vote(int elecId)
     		throws IOException, RainwaveException {
     	return post("vote",
     			"entry_id", String.valueOf(elecId)
     	).vote_result;
     }
     
     public Station[] getStations() throws IOException, RainwaveException {
     	return post("stations").getStations();
     }
     
     public Album[] getAlbums() throws IOException, RainwaveException {
     	return post("all_albums").all_albums;
     }
     
     public Artist[] getArtists() throws IOException, RainwaveException {
    	return post("all_artists").artist_list;
     }
     
     public Artist getDetailedArtist(int artist_id) throws IOException, RainwaveException {
     	return post("artist",
     		"id", String.valueOf(artist_id)
    	).artist_detail;
     }
     
     public Album getDetailedAlbum(int album_id) throws IOException, RainwaveException {
     	return post(
     		"album",
     		"id", String.valueOf(album_id)
     	).album;
     }
     
     public RainwaveResponse submitRequest(int song_id) throws IOException, RainwaveException {
     	return post(
     		"request",
     		"song_id", String.valueOf(song_id)
     	);
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
         return BitmapFactory.decodeStream(is);
     }
     
     public RainwaveResponse reorderRequests(Song requests[])
     		throws IOException, RainwaveException {
     	return post(
     			"order_requests", 
     			"order", Rainwave.makeRequestQueueString(requests)
     	);
     }
     
     public RainwaveResponse deleteRequest(Song request)
     		throws IOException, RainwaveException {
     	return post(
     			"delete_request", 
     			"song_id", String.valueOf(request.requestq_id)
     	);
     }
 
     public void setUserInfo(String userId, String key) {
         mUserId = userId;
         mKey = key;
     }
     
     public void setStation(int stationId) {
     	mStation = stationId;
     	Rainwave.putLastStation(mContext, stationId);
     }
     
     public String getUrl() {
     	return mBaseUrl.toString();
     }
     
     public int getStationId() {
     	return mStation;
     }
     
     public boolean isAuthenticated() {
         return mUserId != null && mKey != null && mUserId.length() > 0 && mKey.length() > 0;
     }
     
     private RainwaveResponse get(String path, String... params)
     throws IOException, RainwaveException {
     	return request(false, path, params);
     }
     
     private RainwaveResponse post(String path, String... params)
     throws IOException, RainwaveException {
     	return request(true, path, params);
     }
     
     private RainwaveResponse request(final boolean post, String path, String... params)
             throws IOException, RainwaveException {
     	
         // Construct arguments
         Arguments httpArgs = new Arguments(params);
         httpArgs.put(NAME_STATION, String.valueOf(mStation));
 
         if(this.isAuthenticated()) {
           httpArgs.put(NAME_USERID, mUserId);
           httpArgs.put(NAME_KEY, mKey);
         }
 
         HttpURLConnection conn;
         if (post) {
             conn = HttpHelper.makePost(mBaseUrl, path, httpArgs.encode());
         }
         else {
             conn = HttpHelper.makeGet(mBaseUrl, String.format("%s?%s", path, httpArgs.encode()), null);
         }
 
         // Convert the json into Java objects.
         Gson gson = getGson();
         JsonParser parser = new JsonParser();
         JsonElement json = parser.parse(getReader(conn));
         
         RainwaveResponse response;
         
         try {
         	response = gson.fromJson(json, RainwaveResponse.class);
         }
         catch(JsonParseException e) {
         	Resources r = mContext.getResources();
         	String msg = String.format(r.getString(R.string.msg_outdatedApi), path);
         	throw new RainwaveException(0, msg);
         }
         
         // Throw an exception if there was some sort of problem.
         handleErrors(response);
         
         return response;
     }
     
     private void handleErrors(RainwaveResponse r)
     	throws RainwaveException {
     	handleError(r.error);
     	handleError(r.request_result);
     	handleError(r.request_delete_return);
     	handleError(r.request_reorder_return);
     	handleError(r.vote_result);
     	handleError(r.rate_result);
     }
     
     private void handleError(GenericResult result) throws RainwaveException {
     	if(result == null) return;
         if(!result.success) {
             throw new RainwaveException(0, result.text);
         }
     }
 
     private Reader getReader(HttpURLConnection conn)
             throws IOException {
         return new BufferedReader(new InputStreamReader(conn.getInputStream()));
     }
 
     private String getUrl(String path) throws MalformedURLException {
         if (path == null || path.length() == 0)
             return mBaseUrl.toString();
         final URL url = new URL(mBaseUrl, path);
         return url.toString();
     }
 
     private Gson getGson() {
         GsonBuilder builder = new GsonBuilder();
         return builder.create();
     }
 
     public static Session makeSession(Context ctx) throws MalformedURLException {
         Session s = new Session();
         String url = Rainwave.getUrl(ctx);
         s.mContext = ctx;
         s.mBaseUrl = new URL(url);
         s.mStation = Rainwave.getLastStation(ctx, s.mStation);
         s.setUserInfo(Rainwave.getUserId(ctx), Rainwave.getKey(ctx));
         return s;
     }
     
     public static Session makeSession(Context ctx, String user, String key) throws MalformedURLException {
         Session s = new Session();
         String url = Rainwave.getUrl(ctx);
         s.mContext = ctx;
         s.mBaseUrl = new URL(url);
         s.mStation = Rainwave.getLastStation(ctx, s.mStation);
         s.setUserInfo(user,key);
         return s;
     }
 
     public static final String
             NAME_STATION = "sid",
             NAME_USERID = "user_id",
             NAME_KEY = "key";
 }
