 package nl.vincentkriek.lastfm;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 
 import org.apache.http.util.ByteArrayBuffer;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.util.Log;
 
 
 public class LastFM {
 	public static final String TAG = "nl.vincentkriek.lastfm.LastFM";
     public static final String baseURL = "http://ws.audioscrobbler.com/2.0/?format=json&api_key=40887e583290b0d8932e3c872ac7aae5";
 	
 	public static String request(String extra) throws IOException {
 		URL url = new URL(baseURL + extra);
		URLConnection conn = url.openConnection();
 		InputStream is = conn.getInputStream();
 		BufferedInputStream bis = new BufferedInputStream(is);
 		ByteArrayBuffer bab = new ByteArrayBuffer(64);
 		int current = 0;
 		
 		while((current = bis.read()) != -1)
 		{
 		         bab.append((byte)current);
 		}
 		
 		return new String(bab.toByteArray());
 	}
 	
 	public static JSONObject getRecentTrack(String user) {
 		String json;
 		JSONObject jObject = null;
 		try {
 			json = request("&method=user.getrecenttracks&user=" + user);
			jObject = new JSONObject(new String(json)).getJSONObject("recenttracks").getJSONArray("track").getJSONObject(0);
 		} catch (IOException e) {
 			Log.e(TAG, e.getMessage());
 		} catch (JSONException e) {
 			Log.e(TAG, e.getMessage());
 		}
 		return jObject;
 	}
 	
 	public static Bitmap getAlbumArtFromMbid(String mbid) {
 		String json;
 		Bitmap bitmap = null;
 		try {
 			json = request("&method=album.getInfo&mbid=" + mbid);
 			JSONArray images = new JSONObject(json).getJSONObject("album").getJSONArray("image");
 			String albumArtLink = null;
 			for(int i = 0; i < images.length(); i++) {
 				JSONObject image = images.getJSONObject(i);
 				albumArtLink = image.getString("#text");
 
 				if(image.getString("size").equals("mega")) {
 					break;
 				}				
 			}
 			
 			URL url = new URL(albumArtLink);
 			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
 			connection.setDoInput(true);
 			connection.connect();
 			InputStream input = connection.getInputStream();
 			bitmap = BitmapFactory.decodeStream(input);
 		} catch (IOException e) {
 			Log.e(TAG, e.getMessage());
 		} catch (JSONException e) {
 			Log.e(TAG, e.getMessage());
 		}
 
 		return bitmap;
 	}
 	
 	public static Bitmap getAlbumArtFromAlbum(String artist, String album) {
 		String json;
 		Bitmap bitmap = null;
 		try {
 			json = request("&method=album.getInfo&artist=" 
 					+ URLEncoder.encode(artist)	+ "&album=" + URLEncoder.encode(album));
 			JSONArray images = new JSONObject(json).getJSONObject("album").getJSONArray("image");
 			String albumArtLink = null;
 			for(int i = 0; i < images.length(); i++) {
 				JSONObject image = images.getJSONObject(i);
 				albumArtLink = image.getString("#text");
 
 				if(image.getString("size").equals("mega")) {
 					break;
 				}				
 			}
 			
 			URL url = new URL(albumArtLink);
 			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
 			connection.setDoInput(true);
 			connection.connect();
 			InputStream input = connection.getInputStream();
 			bitmap = BitmapFactory.decodeStream(input);
 		} catch (IOException e) {
 			Log.e(TAG, e.getMessage());
 		} catch (JSONException e) {
 			Log.e(TAG, e.getMessage());
 		}
 
 		return bitmap;
 	}
 
 	public static Bitmap getAlbumArtByUser(String user) throws JSONException {
 		JSONObject json = getRecentTrack(user);
 		String mbid = json.getJSONObject("album").getString("mbid");
 		if(mbid.length() > 0) 
 			return getAlbumArtFromMbid(mbid);
 		else {
 			String artist = json.getJSONObject("artist").getString("#text");
 			String album = json.getJSONObject("album").getString("#text");
 			return getAlbumArtFromAlbum(artist, album);
 		}		
 	}
 }
