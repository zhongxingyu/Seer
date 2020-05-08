 package se.chalmers.pd.playlistmanager;
 
 import java.util.ArrayList;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.util.Log;
 
 public class ApplicationController implements MqttWorker.Callback, DialogFactory.Callback {
 	
 
 	public interface Callback {
 		public void onSearchResult(ArrayList<Track> tracks);
 		public void onUpdatePlaylist(Track track);
 	}
 	
 	private static final String TOPIC_PLAYLIST = "/playlist";
 	private static final String TAG = "ApplicationController";
 	private static final String ACTION = "action";
 	private static final String ACTION_ADD = "add";
 	private static final String TRACK_URI = "uri";
 	private static final String TRACK_NAME = "track";
 	private static final String TRACK_ARTIST = "artist";
 	
 	private MqttWorker mqttWorker;
 	private Context context;
 	private Callback callback;
 
 	public ApplicationController(Context context, Callback callback) {
 		mqttWorker = new MqttWorker(this);
 		mqttWorker.start();
 		this.context = context;
 		this.callback = callback;
 	}
 	
 	public void reconnect() {
 		mqttWorker.interrupt();
 		mqttWorker = new MqttWorker(this);
 		mqttWorker.start();
 	}
 	
 	@Override
 	public void onConnected(boolean connected) {
 		if(connected) {
 			mqttWorker.subscribe(TOPIC_PLAYLIST);
 			Log.d(TAG, "Now subscribing to " + TOPIC_PLAYLIST);
 		} else {
 			
 			((MainActivity) context).runOnUiThread(new Runnable() {
 				@Override
 				public void run() {
 					DialogFactory.buildConnectDialog(context, ApplicationController.this).show();
 				}
 			});
 		}
 	}
 	
 	@Override
 	public void onConnectDialogAnswer(boolean result) {
 		if(result) {
 			reconnect();
 		}
 	}
 
 	@Override
 	public void onMessage(String topic, String payload) {
 		try {
 			JSONObject json = new JSONObject(payload);
 			String action = json.getString(ACTION);
 			if(action.equals("add")) {
 				final Track track = new Track(json.getString(TRACK_NAME), json.getString(TRACK_ARTIST), json.optString(TRACK_URI));
 				((MainActivity) context).runOnUiThread(new Runnable() {
 					@Override
 					public void run() {
 						callback.onUpdatePlaylist(track);
 					}
 				});
 			}
 		} catch (JSONException e) {
 			Log.e(TAG, "Could not create json object from payload " + payload + " with error: " + e.getMessage());
 		}
 	}
 
 	public void addTrack(Track track) {
 		JSONObject message = new JSONObject();
 		try {
 			message.put(ACTION, ACTION_ADD);
 			message.put(TRACK_ARTIST, track.getArtist());
 			message.put(TRACK_NAME, track.getName());
 			message.put(TRACK_URI, track.getUri());
 			mqttWorker.publish(TOPIC_PLAYLIST, message.toString());
 		} catch (JSONException e) {
 			Log.e(TAG, "Could not create and send json object from track " + track.toString() + " with error: " + e.getMessage());
 		}
 	}
 
 	
 
 }
