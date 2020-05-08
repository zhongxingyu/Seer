 package stetson.CTF;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.SystemClock;
 import android.util.Log;
 import android.widget.RadioButton;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.ItemizedOverlay;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 
 public class GameCTF extends MapActivity {
 	
 	// Delay in gameProcess (in ms) [2.5 seconds]
 	public static final int GAME_UPDATE_DELAY = 2500;
 	
 	// Data members
 	private MapView mapView;
 	private Handler gameHandler = new Handler();
 	private static final String TAG = "GameCTF";
 	
 	MapController mapController;
 	GameCTFOverlays itemizedoverlay;
 	OverlayItem overlayitem;
 	List<Overlay> mapOverlays;
 	
 	boolean isRunning = false;
 	boolean isCentered = false;
 	
 	private Drawable drawable_self;
 	private Drawable drawable_red_flag;
 	private Drawable drawable_blue_flag;
 	private Drawable drawable_red_player;
 	private Drawable drawable_blue_player;
 	
 	/**
 	 * Called when the activity is first created.
 	 * Default behavior is NULL. Nothing is happening yet!
 	 * @param saved instance state
 	 */
 	public void onCreate(Bundle savedInstanceState) {
 		
 		Log.i(TAG, "Starting map activity...");
 		isRunning = true;
 		isCentered = false;
 		 
 		// Restore a saved instance of the application
 		super.onCreate(savedInstanceState);
 		
 		// Make sure the user is actually in a game
 		if(CurrentUser.getGameId().equals("")) {
 			this.finish();
 			return;
 		}
 		
 		// Move back to the game selection panel
 		setContentView(R.layout.game);
 		
  		// Turns on built-in zoom controls
 		mapView = (MapView) findViewById(R.id.mapView);
 		mapController = mapView.getController();
 		mapView.setBuiltInZoomControls(true);
 		
 		// Setting up the overlay marker images
 		drawable_self = this.getResources().getDrawable(R.drawable.star);
 		drawable_self.setBounds(0, 0, drawable_self.getIntrinsicWidth(), drawable_self.getIntrinsicHeight());
 		
 		drawable_red_flag = this.getResources().getDrawable(R.drawable.red_flag);
 		drawable_red_flag.setBounds(0, 0, drawable_red_flag.getIntrinsicWidth(), drawable_red_flag.getIntrinsicHeight());
 		
 		drawable_blue_flag = this.getResources().getDrawable(R.drawable.blue_flag);
 		drawable_blue_flag.setBounds(0, 0, drawable_blue_flag.getIntrinsicWidth(), drawable_blue_flag.getIntrinsicHeight());
 		
 		drawable_red_player = this.getResources().getDrawable(R.drawable.person_red);
 		drawable_red_player .setBounds(0, 0, drawable_red_player.getIntrinsicWidth(), drawable_red_player.getIntrinsicHeight());
 		
 		drawable_blue_player = this.getResources().getDrawable(R.drawable.person_blue);
 		drawable_blue_player.setBounds(0, 0, drawable_blue_player.getIntrinsicWidth(), drawable_blue_player.getIntrinsicHeight());
 		
 		mapOverlays = mapView.getOverlays();
         itemizedoverlay = new GameCTFOverlays(drawable_self);
 		
 		// Start game processor
 		gameHandler.postDelayed(gameProcess, GAME_UPDATE_DELAY);
 
 	}
 	
 	/**
 	 * When the activity is ended, we need to clear the users game and location.
 	 */
 	public void onDestroy() {
 		
 		// No more game, stop running
 		isRunning = false;
 		
 		super.onDestroy();
 		
 		Log.i(TAG, "Stopping Map Activity");
 		CurrentUser.setGameId("");
 		CurrentUser.setLocation(-1, -1);
 		CurrentUser.setAccuracy(-1);
 	}
 	
 	/**
 	 * Game processor. Runs every GAME_UPDATE_DELAY (ms).
 	 */
 	private final Runnable gameProcess = new Runnable()
 	{
 		/**
 		 * Main gameProcess function.
 		 */
 	    public void run() 
 	    {
 	    	
 	    	// Don't run if we don't have a game anymore
 	    	if(!isRunning) {
 	    		return;
 	    	}
 	    	
 	    	Log.i(TAG, "Game Process()");
 	    	
 	    	
 	    	// If our accuracy doesn't suck, update
 	    	if(true) {
	    		String gameUrl = CurrentUser.getGameId().replaceAll(" ", "%20");
				HttpPost req = new HttpPost(StetsonCTF.SERVER_URL + "/game/" + gameUrl);
 				CurrentUser.buildHttpParams(req, CurrentUser.UPDATE_PARAMS);
 				Connections.sendRequest(req, new ResponseListener() {
 					public void onResponseReceived(HttpResponse response) {
 						
 						// Clear all map points
 						mapOverlays.clear();
 						itemizedoverlay.clear();
 						
 						// Pull response message
 						String data = Connections.responseToString(response);
 						
 						// JSON IS FUN!
 						try {
 							JSONObject jObject, jSubObj;
 							jObject = new JSONObject(data);
 							
 							// Process origin
 							processOrigin(jObject);
 							
 							// Process Players
 							jSubObj = (JSONObject) jObject.opt("players");
 							processPlayers(jSubObj);
 							
 							// Process Game data
 							processGame(jObject);
 							
 						    // Add map overlays
 						    mapOverlays.add(itemizedoverlay);
 						    mapView.invalidate();
 							
 						} catch (JSONException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 						
 						Log.i(TAG, "Game Data: " + data);
 						
 					}
 				});
 	    	}
 	    	
 	    	gameHandler.postDelayed(this, GAME_UPDATE_DELAY);
 	    }
 	    
 	    /**
 	     * If there is a request to center around the origin, do it.
 	     * @param jObject
 	     */
 	    private void processOrigin(JSONObject jObject) {
 	    	if(!isCentered) {
 	    		JSONObject orginObj;
 				try {
 					isCentered = true;
 					orginObj = jObject.getJSONObject("origin");
 			    	int lati = (int) (1E6 * Double.parseDouble(orginObj.getString("latitude")));
 			    	int loni = (int) (1E6 * Double.parseDouble(orginObj.getString("longitude")));
 			    	GeoPoint origin = new GeoPoint(lati, loni);
 			    	mapView.getController().animateTo(origin);
 				} catch (JSONException e) {
 					Log.e(TAG, "Error centering on origin.", e);
 				}
 	    	}
 	    }
 	    
 	    /**
 	     * Handles other players.
 	     * @param jSubObj containing all players.
 	     */
 	    private void processPlayers(JSONObject jSubObj) {
 			try {
 	
 				// Loop through all players
 				JSONObject player;
 				String playerKey;
 				Iterator plrIterator = jSubObj.keys();
 			    while(plrIterator.hasNext()) {
 			    	
 			    	playerKey = (String) plrIterator .next();
 			    	player = jSubObj.getJSONObject(playerKey);
 			    	
 			    	// If a player isn't on a team, we don't care about it at all
 			    	if(player.has("team")) {
 
 				    	int lati = (int) (1E6 * Double.parseDouble(player.getString("latitude")));
 				    	int loni = (int) (1E6 * Double.parseDouble(player.getString("longitude")));
 	
 						Log.i(TAG, "Adding player: " + player.getString("name") + " with  KEY=" + playerKey + " @ LAT " + player.getString("latitude") + ", LONG " + player.getString("longitude"));
 						GeoPoint marker = new GeoPoint(lati, loni);
 						OverlayItem overlayitem = new OverlayItem(marker, player.getString("name"), player.getString("name"));
 						
 						
 						String team = player.getString("team");
 						if(playerKey.equals(CurrentUser.getUID())) {
 							overlayitem.setMarker(drawable_self);
 						} else if(team.equals("red")) {
 							overlayitem.setMarker(drawable_red_player);
 						} else if(team.equals("blue")) {
 							overlayitem.setMarker(drawable_blue_player);
 						}
 	
 						itemizedoverlay.addOverlay(overlayitem);
 						
 			    	}
 
 			    }
 
 			} catch (JSONException e) {
 				Log.e(TAG, "Error in gameProcess().processPlayers()", e);
 			}
 			
 	    }
 		    
 	    /**
 	     * Handles game data, such as flags and bounds.
 	     * @param jSubObj containing the entire game json object.
 	     */
 	    private void processGame(JSONObject jSubObj) {
 			try {
 				
 				JSONObject game = jSubObj;
 				
 				// Adding red flag
 				JSONObject red_flag = game.getJSONObject("red_flag");
 				int lat = (int) (1E6 * Double.parseDouble(red_flag.getString("latitude")));
 		    	int lon = (int) (1E6 * Double.parseDouble(red_flag.getString("longitude")));
 
 				GeoPoint red_marker = new GeoPoint(lat, lon);
 				OverlayItem red_overlayitem = new OverlayItem(red_marker, "red_flag", "red_flag");
 				red_overlayitem.setMarker(drawable_red_flag);
 				itemizedoverlay.addOverlay(red_overlayitem);
 				
 				Log.i(TAG, "Adding red_flag: " + red_flag.getString("latitude") + red_flag.getString("longitude"));
 				
 				// Adding blue flag
 				JSONObject blue_flag = game.getJSONObject("blue_flag");
 				lat = (int) (1E6 * Double.parseDouble(blue_flag.getString("latitude")));
 		    	lon = (int) (1E6 * Double.parseDouble(blue_flag.getString("longitude")));
 
 				GeoPoint blue_marker = new GeoPoint(lat, lon);
 				OverlayItem blue_overlayitem = new OverlayItem(blue_marker, "blue_flag", "blue_flag");
 				blue_overlayitem.setMarker(drawable_blue_flag);
 				itemizedoverlay.addOverlay(blue_overlayitem);
 				
 				Log.i(TAG, "Adding blue_flag: " + red_flag.getString("latitude") + red_flag.getString("longitude"));
 			    
 			} catch (JSONException e) {
 				Log.e(TAG, "Error in gameProcess().processGame()", e);
 			}
 	    }
 
 	
 	};
 
 
 	/**
 	 * Returns false (required by MapActivity)
 	 * @return false
 	 */
 	protected boolean isRouteDisplayed() {
 		return false;
 	}	
 	
 }
