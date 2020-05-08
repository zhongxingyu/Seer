 package stetson.CTF;
 
 import java.io.IOException;
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
 	
 	/**
 	 * Called when the activity is first created.
 	 * Default behavior is NULL. Nothing is happening yet!
 	 * @param saved instance state
 	 */
 	public void onCreate(Bundle savedInstanceState) {
 		
 		Log.i(TAG, "Starting map activity...");
 		 isRunning = true;
 		 
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
 		
 		// Setting up the overlays class
 		Drawable drawable = this.getResources().getDrawable(R.drawable.person_red);
 		mapOverlays = mapView.getOverlays();
         itemizedoverlay = new GameCTFOverlays(drawable);
 		
 		
 		
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
 	    	if(CurrentUser.getAccuracy() > -1) {
				HttpPost req = new HttpPost(StetsonCTF.SERVER_URL + "/game/" + CurrentUser.getGameId());
 				CurrentUser.buildHttpParams(req, CurrentUser.UPDATE_PARAMS);
 				sendRequest(req, new ResponseListener() {
 					public void onResponseReceived(HttpResponse response) {
 						
 						// Clear all map points
 						mapOverlays.clear();
 						
 						// Pull response message
 						String data = responseToString(response);
 						
 						// JSON IS FUN!
 						try {
 							JSONObject jObject, jSubObj;
 							jObject = new JSONObject(data);
 							
 							// Process Players
 							jSubObj = (JSONObject) jObject.opt("players");
 							processPlayers(jSubObj);
 							
 							
 						} catch (JSONException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 						
 						Log.i(TAG, "Game Data: " + data);
 						
 					}
 				});
 	    	}
 	    	
 	    	// Delay for set time and run again
 	    	gameHandler.postDelayed(this, GAME_UPDATE_DELAY);
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
 			    while(plrIterator .hasNext()) {
 			    	playerKey = (String) plrIterator .next();
 			    	player = jSubObj.getJSONObject(playerKey);
 			    	int lati = (int) (1E6 * Double.parseDouble(player.getString("latitude")));
 			    	int loni = (int) (1E6 * Double.parseDouble(player.getString("longitude")));
 
 					Log.i(TAG, "Adding player: " + player.getString("name") + " with  KEY=" + playerKey + " @ LAT " + player.getString("latitude") + ", LONG " + player.getString("longitude"));
 					GeoPoint marker = new GeoPoint(lati, loni);
 					OverlayItem overlayitem = new OverlayItem(marker, player.getString("name"), player.getString("name"));
 					itemizedoverlay.addOverlay(overlayitem);
 
 			    }
 			    
 			    // Add map overlays
 			    mapOverlays.add(itemizedoverlay);
 			    
 			    // Request a redraw from the view
 			    mapView.refreshDrawableState();
 				
 			} catch (JSONException e) {
 				Log.e(TAG, "Error in gameProcess().processPlayers()", e);
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
 	
 	/**
 	 * Makes an HTTP request and sends it to a response listener once completed.
 	 * @param request
 	 * @param responseListener
 	 */
 	public static void sendRequest(final HttpRequestBase request, ResponseListener responseListener) {
 		(new AsynchronousSender(request, new Handler(), new CallbackWrapper(responseListener))).start();
 	}
 	
 	/**
 	 * Draws a string from an HttpResponse object.
 	 * @param rp
 	 * @return
 	 */
 	public static String responseToString(HttpResponse rp) {
     	String str = "";
     	try {
     		str = EntityUtils.toString(rp.getEntity());
     	} catch(IOException e) {
     		Log.i(TAG, "HttpRequest Error!", e);
     	}  
     	return str;
 	}
 	
 	
 }
