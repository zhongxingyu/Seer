 
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
 
     public RainwaveResponse asyncGet()
             throws IOException, RainwaveException {
         return getResponse(true, false, "get");
     }
 
     public RainwaveResponse syncGet(RainwaveResponse oldResponse)
             throws IOException, RainwaveException {
         return getResponse(false, true, "sync", oldResponse);
     }
     
     public RainwaveResponse syncInit()
             throws IOException, RainwaveException {
         return getResponse(false, true, "init");
     }
     
     public GenericResult rateSong(int songId, float rating)
     		throws IOException, RainwaveException {
     	rating = Math.max(1.0f, Math.min(rating, 5.0f));
     	return getResponse(
     			true,
     			true,
     			"rate",
     			"song_id",String.valueOf(songId),
     			"rating", String.valueOf(rating)
     	).rate_result;
     }
     
     public GenericResult vote(int elecId)
     		throws IOException, RainwaveException {
     	return getResponse(
     			true,
     			true,
     			"vote",
     			"elec_entry_id", String.valueOf(elecId)
     	).vote_result;
     }
     
     public Station[] getStations() throws IOException, RainwaveException {
     	boolean auth = isAuthenticated();
     	String request = (auth)
     			? "stations_user"
     			: "stations";
     	
     	return getResponse(
     			true,
     			auth,
     			request
     	).getStations();
     }
     
     public Album[] getAlbums() throws IOException, RainwaveException {
     	return getResponse(
     		true,
     		true,
     		"all_albums"
     	).playlist_all_albums;
     }
     
     public Artist[] getArtists() throws IOException, RainwaveException {
     	return getResponse(
     		true,
     		true,
     		"artist_list"
     	).artist_list;
     }
     
     public Artist getDetailedArtist(int artist_id) throws IOException, RainwaveException {
     	return getResponse(
     		true,
     		true,
     		"artist_detail",
     		"artist_id", String.valueOf(artist_id)
     	).artist_detail;
     }
     
     public Album getDetailedAlbum(int album_id) throws IOException, RainwaveException {
     	return getResponse(
     		true,
     		true,
     		"album",
     		"album_id", String.valueOf(album_id)
     	).playlist_album;
     }
     
     public RainwaveResponse request(int song_id) throws IOException, RainwaveException {
     	return getResponse(
     		true,
     		true,
     		"request",
     		"song_id", String.valueOf(song_id)
     	);
     }
     
     public Bitmap fetchAlbumArt(String path) throws IOException {
         URL url = new URL(getUrl(path));
         InputStream is = url.openStream();
         return BitmapFactory.decodeStream(is);
     }
     
     public RainwaveResponse reorderRequests(Song requests[])
     		throws IOException, RainwaveException {
     	return getResponse(
     			true, 
     			true, 
     			"requests_reorder", 
     			"order", 
     			Rainwave.makeRequestQueueString(requests)
     	);
     }
     
     public RainwaveResponse deleteRequest(Song request)
     		throws IOException, RainwaveException {
     	return getResponse(
     			true, 
     			true, 
     			"request_delete", 
     			"requestq_id", 
     			String.valueOf(request.requestq_id)
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
     
     private RainwaveResponse getResponse(boolean async, boolean auth, String request,
             String... params)
             throws IOException, RainwaveException {
     	return getResponse(async,auth,request,new RainwaveResponse(), params);
     }
     
     private RainwaveResponse getResponse(boolean async, boolean auth, String request,
             RainwaveResponse response, String... params)
             throws IOException, RainwaveException {
     	
     	// Make the path
         String path = String.format("%s/%s/%s", (async) ? "async" : "sync", mStation, request);
 
         HttpURLConnection conn;
         if (auth && mUserId != null && mKey != null) {
             // Extend the var-args into an array with 4 more slots.
             String tmp[] = (params != null) ? new String[params.length + 4] : new String[4];
             int begin = (params != null) ? params.length : 0;
             for (int i = 0; i < params.length; i++) {
                 tmp[i] = params[i];
             }
 
             // Insert in the new data.
             tmp[begin] = NAME_USERID;
             tmp[begin + 1] = mUserId;
             tmp[begin + 2] = NAME_KEY;
             tmp[begin + 3] = mKey;
 
             // Get new urlencoded string
             String paramString = HttpHelper.encodeParams(tmp);
 
             // Return HttpURLConnection
             conn = HttpHelper.makePost(mBaseUrl, path, paramString);
         }
         else {
             conn = HttpHelper.makeGet(mBaseUrl, path, "");
         }
 
         // Convert the json into Java objects.
         Gson gson = getGson();
         JsonParser parser = new JsonParser();
         JsonElement json = parser.parse(getReader(conn));
         RainwaveResponse tmp;
         
         try {
         	tmp = gson.fromJson(json, RainwaveResponse.class);
         }
         catch(JsonParseException e) {
         	Resources r = mContext.getResources();
         	String msg = String.format(r.getString(R.string.msg_outdatedApi), path);
         	throw new RainwaveException(0, msg);
         }
         
         response.receiveUpdates(tmp);
         
         // Throw an exception if there was some sort of problem.
         handleErrors(tmp);
         
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
         if(result.code != 1) {
             throw new RainwaveException(result.code,result.text);
         }
     }
 
     private Reader getReader(HttpURLConnection conn)
             throws IOException {
         return new BufferedReader(new InputStreamReader(conn.getInputStream()));
     }
 
     private String getUrl(String path) {
         if (path == null || path.length() == 0)
             return mBaseUrl.toString();
 
         if (path.charAt(0) != '/') {
             return String.format("%s/%s", mBaseUrl.toString(), path);
         }
         else {
             return String.format("%s%s", mBaseUrl.toString(), path);
         }
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
             NAME_USERID = "user_id",
             NAME_KEY = "key";
 }
