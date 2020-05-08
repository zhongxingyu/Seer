 package se.chalmers.pd.device;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import se.chalmers.pd.device.MqttWorker.MQTTCallback;
 import se.chalmers.pd.device.SpotifyController.PlaylistCallback;
 import android.content.Context;
 
 /**
  * 
  * @author Patrik Thituson
  * 
  */
 public class ApplicationController implements MQTTCallback, PlaylistCallback {
 
 	/**
 	 * Callbacks that is used for the main activity to be able to update the
 	 * view
 	 */
 	interface Callbacks {
 		void onPlayerLoggedIn();
 
 		void onPlayerPlay();
 
 		void onPlayerNext();
 
 		void onPlayerPause();
 
 		void onStartedApplication(String status);
 
 		void onInstalledApplication(boolean show);
 
 		void onConnectedMQTT(boolean connected);
 
 		void onUpdateSeekbar(float position);
 
 	}
 
 	private Context context;
 	private MqttWorker mqttWorker;
 	private SpotifyController spotifyController;
 	private Callbacks callbacks;
 
 	public ApplicationController(Context context) {
		this.mqttWorker = new MqttWorker(this);
 		this.context = context;
 		this.callbacks = (Callbacks) context;
 		spotifyController = new SpotifyController(this, context);
 	}
 
 	/**
 	 * Helper method that publish the playlist to the topic "/playlist" for
 	 * testing purposes
 	 */
 	private void sendPlayList() {
 		JSONObject payload = new JSONObject();
 		JSONArray playlistArray = new JSONArray();
 		
 		String[] artists = { "Foo Fighters", "Nirvana", "Avicii" };
 		String[] tracks = { "The Pretender", "Rape me", "X You" };
 		String[] uris = { "spotify:track:3ZsjgLDSvusBgxGWrTAVto", "spotify:track:47KVHb6cOVBZbmXQweE5p7",
 				"spotify:track:330r0K82tIDVr6f1GezAd8" };
 		String[] lengths = { "270", "170", "200" };
 		try {
 			payload.put("action", "add_all");
 			for (int i = 0; i < 3; i++) {
 				JSONObject jsonTrack = new JSONObject();
 				jsonTrack.put("track", tracks[i]);
 				jsonTrack.put("artist", artists[i]);
 				jsonTrack.put("uri", uris[i]);
 				jsonTrack.put("tracklength", lengths[i]);
 				playlistArray.put(jsonTrack);
 			}
 			payload.put("data", playlistArray);
 			mqttWorker.publish("/playlist", payload.toString());
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Forwards he login calls to the spotifycontroller
 	 */
 	public void login() {
 		spotifyController.login();
 	}
 
 
 	/**
 	 * Callback that let us know we have succesfully logged in to spotify
 	 */
 	public void onLoginSuccess() {
 		callbacks.onPlayerLoggedIn();
 	}
 
 	/**
 	 * Callback that let us know an error occured when logging in to spotify
 	 */
 	public void onLoginFailed(String message) {
 
 	}
 
 	/**
 	 * Callback that is called when a song has succesfully started playing
 	 */
 	public void onPlay(boolean success) {
 		if (success) {
 			callbacks.onPlayerPlay();
 		}
 
 	}
 
 	/**
 	 * Callback that is called when a song has succesfully paused playing
 	 */
 	public void onPause(boolean success) {
 		callbacks.onPlayerPause();
 	}
 
 	/**
 	 * Callback that is called when a song has ended and calls the functon next
 	 */
 	public void onEndOfTrack() {
 		createAndPublishPlayerActions(Action.next);
 		callbacks.onPlayerNext();
 	}
 
 	/**
 	 * Destroys the spotifycontroller and disconenct the broker to prevent any
 	 * threads from crashing
 	 */
 	public void onDestroy() {
 		spotifyController.destroy();
 		mqttWorker.disconnect();
 	}
 
 	/**
 	 * Connects to the mqtt broker
 	 */
 	public void connect(String url) {
		mqttWorker.setBrokerURL(url);
 		mqttWorker.start();
 	}
 
 	/**
 	 * Disconnects from the mqtt broker
 	 */
 	public void disconnect() {
 		mqttWorker.disconnect();
        mqttWorker.destroy();
 		callbacks.onConnectedMQTT(false);
 	}
 
 
 	public boolean isConnectedToBroker() {
 		return mqttWorker.isConnected();
 	}
 
 	public String getCurrentTrack() {
 		Track track = spotifyController.getCurrentTrack();
 		if (track != null) {
 			return track.getArtist() + " - " + track.getName();
 		} else
 			return "No tracks available";
 	}
 
 	public void seek(float position) {
 		spotifyController.seek(position);
 	}
 
 	public void onPositionChanged(float position) {
 		callbacks.onUpdateSeekbar(position);
 	}
 	
 	/**
 	 * Callback from Mqttworker thath handles all action-messages
 	 * and perform the corresponding action. 
 	 */
 	public void onMessage(String topic, String payload) {
 		JSONObject json;
 		try {
 			json = new JSONObject(payload);
 			String action = json.optString("action");
 			String data = json.optString("data");
 			boolean success = data.equals("success");
 			switch (Action.valueOf(action)) {
 			case add:
 				addJsonTrackToPlaylist(json);
 				break;
 			case play:
 				spotifyController.play();
 				callbacks.onPlayerPlay();
 				break;
 			case pause:
 				spotifyController.pause();
 				callbacks.onPlayerPause();
 				break;
 			case next:
 				spotifyController.playNext();
 				callbacks.onPlayerNext();
 				break;
 			case install:
 				if (data.equals("success")) {
 					callbacks.onInstalledApplication(true);	
 				} else if (data.equals("error")) {
 					callbacks.onInstalledApplication(false);
 				} 
 				break;
 			case start:
 				if (data.equals("success")) {
 					sendPlayList();
 					callbacks.onStartedApplication("start");
 				} else if (data.equals("error")) {
 					callbacks.onStartedApplication("error");
 				} 
 				break;
 			case stop:
 				callbacks.onStartedApplication(action);
 				break;
 			case uninstall:
 				callbacks.onInstalledApplication(!success);
 				break;
 			case exist:
 				callbacks.onInstalledApplication(success);
 				break;
 			case add_all:
 				spotifyController.clearPlaylist();
 				JSONArray playlistArray = new JSONArray(data);
 				JSONObject jsonTrack;
 				for (int i = 0; i < playlistArray.length(); i++){
 					jsonTrack = new JSONObject(playlistArray.get(i).toString());
 					addJsonTrackToPlaylist(jsonTrack);
 				}
 				break;
 			}
 			
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 
 	}
 	/**
 	 * Helper method for adding a track to the playlist based
 	 * on the json object.
 	 * @param jsonTrack
 	 */
 	private void addJsonTrackToPlaylist(JSONObject jsonTrack){
 		String name = jsonTrack.optString("track");
 		String artist = jsonTrack.optString("artist");
 		String spotifyUri = jsonTrack.optString("uri");
 		int length = jsonTrack.optInt("tracklength");
 		Track newTrack = new Track(name, artist, spotifyUri, length);
 		spotifyController.addTrackToPlaylist(newTrack);
 	}
 
     public void createAndPublishSystemActions(Action action){
         if (isConnectedToBroker()) {
             String data = "playlist";
             if(action.equals(Action.install)){
                 StreamToBase64String streamToBase64String = StreamToBase64String.getInstance(context);
                 data = streamToBase64String.getBase64StringFromAssets("Playlist.zip");
             }
             JSONObject json = new JSONObject();
             try {
                 json.put("action", action.toString());
                 json.put("data", data);
             } catch (JSONException e) {
                e.printStackTrace();
             }
             mqttWorker.publish("/system", json.toString());
         }
     }
 
     public void createAndPublishPlayerActions(Action action){
         if (isConnectedToBroker()) {
             JSONObject json = new JSONObject();
             try {
                 json.put("action", action.toString());
             } catch (JSONException e) {
                 e.printStackTrace();
             }
             mqttWorker.publish("/playlist", json.toString());
         }
     }
 
 	
 	/**
 	 * When the application is connected to the broker this callback is called
 	 * and then an exist message is published to see if there is an application
 	 * installed.
 	 */
 	public void onConnected(boolean connected) {
 		callbacks.onConnectedMQTT(true);
 		mqttWorker.subscribe("/playlist");
 		mqttWorker.subscribe("/playlist/1");
 		createAndPublishSystemActions(Action.exist);
 	}
 
 }
