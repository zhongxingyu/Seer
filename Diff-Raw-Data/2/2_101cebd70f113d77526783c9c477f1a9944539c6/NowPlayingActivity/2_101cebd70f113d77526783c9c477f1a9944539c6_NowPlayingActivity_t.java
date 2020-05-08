 package com.purplehatstands.clementine.remote;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import org.apache.http.HttpHost;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.methods.HttpGet;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.graphics.BitmapFactory;
 import android.net.http.AndroidHttpClient;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Base64;
 import android.widget.ImageView;
 import android.widget.MediaController;
 import android.widget.MediaController.MediaPlayerControl;
 import android.widget.TextView;
 
 public class NowPlayingActivity extends Activity {
 	private static final String TAG = "NowPlayingActivity";
 	TextView track_;
 	TextView artist_;
 	TextView album_;
 	ImageView album_cover_;
 	MediaController controls_;
 	JSONObject current_status_;
 	
 	private class JsonFetcher extends AsyncTask<Server, Integer, JSONObject> implements ResponseHandler<String> {
 
 		@Override
 		protected JSONObject doInBackground(Server... params) {
 			Server server = params[0];
 			AndroidHttpClient http = AndroidHttpClient.newInstance("Clementine Remote");
 			String response;
 			try {
 				response = http.execute(new HttpHost(server.getAddress(), server.getPort()), new HttpGet("/"), this);
 				JSONTokener tokener = new JSONTokener(response);
 				JSONObject object;
 				object = (JSONObject)tokener.nextValue();
 				return object;
 			} catch (ClientProtocolException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} finally {
 				http.close();
 			}
 			return new JSONObject();
 		}
 		
 		@Override
 		protected void onPostExecute(JSONObject json) {
 			UpdateStatus(json);
 		}
 		
 		public String handleResponse(HttpResponse response)
 				throws ClientProtocolException, IOException {
 			InputStream content = response.getEntity().getContent();
 			BufferedReader reader = new BufferedReader(new InputStreamReader(content));
 			String line;
 			StringBuilder builder = new StringBuilder();
 			while ((line = reader.readLine()) != null) {
 				builder.append(line);
 			}
 			return builder.toString();
 		}
 		
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		InitialiseUI();
 		
 		Intent intent = getIntent();
 		Server server = (Server) intent.getExtras().get("server");
 		
 		new JsonFetcher().execute(server);
 	}
 	
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		super.onConfigurationChanged(newConfig);
 		InitialiseUI();
 		if (current_status_ != null) {
		  UpdateStatus(current_status_);
 		}
 	}
 	
 	private void InitialiseUI() {
 		setContentView(R.layout.now_playing);
 		track_ = (TextView) findViewById(R.id.track);
 		artist_ = (TextView) findViewById(R.id.artist);
 		album_ = (TextView) findViewById(R.id.album);
 		album_cover_ = (ImageView) findViewById(R.id.album_cover);
 		
 		controls_ = new MediaController(this);
 		controls_.setAnchorView(findViewById(R.id.now_playing_layout));
 		controls_.setMediaPlayer(new Controls());
 		controls_.setEnabled(true);
 	}
 	
 	private void UpdateStatus(JSONObject json) {
 		current_status_ = json;
 		JSONObject song;
 		try {
 			song = json.getJSONObject("song");
 			track_.setText(song.getString("title"));
 			artist_.setText(song.getString("artist"));
 			album_.setText(song.getString("album"));
 			
 			String base64_cover = song.getString("cover");
 			byte[] cover_data = Base64.decode(base64_cover, 0);
 			album_cover_.setImageBitmap(BitmapFactory.decodeByteArray(cover_data, 0, cover_data.length));
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	
 	private static class Controls implements MediaPlayerControl {
 
 		public boolean canPause() {
 			// TODO Auto-generated method stub
 			return true;
 		}
 
 		public boolean canSeekBackward() {
 			// TODO Auto-generated method stub
 			return true;
 		}
 
 		public boolean canSeekForward() {
 			// TODO Auto-generated method stub
 			return true;
 		}
 
 		public int getBufferPercentage() {
 			// TODO Auto-generated method stub
 			return 0;
 		}
 
 		public int getCurrentPosition() {
 			// TODO Auto-generated method stub
 			return 0;
 		}
 
 		public int getDuration() {
 			// TODO Auto-generated method stub
 			return 0;
 		}
 
 		public boolean isPlaying() {
 			// TODO Auto-generated method stub
 			return true;
 		}
 
 		public void pause() {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void seekTo(int pos) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void start() {
 			// TODO Auto-generated method stub
 			
 		}
 		
 	}
 	
 
 }
