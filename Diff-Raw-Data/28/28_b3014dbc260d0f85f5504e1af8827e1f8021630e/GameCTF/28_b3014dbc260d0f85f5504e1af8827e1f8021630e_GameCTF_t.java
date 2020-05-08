 package stetson.CTF;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import java.util.List;
 
 import org.apache.http.client.methods.HttpPost;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import stetson.CTF.Game.GameCTFOverlays;
 import stetson.CTF.Game.GameData;
 import stetson.CTF.Game.GameInfoBar;
 import stetson.CTF.Game.GameMenu;
 import stetson.CTF.Game.GameScores;
 import stetson.CTF.Game.Player;
 
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import android.location.LocationManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.Menu;
 import android.os.PowerManager;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 
 
 public class GameCTF extends MapActivity {
 	
 	// Constant: Debugging tag
 	public static final String TAG = "GameCTF";
 	
 	// Constant: How often should we wait between game update cycles?
 	public static final int GAME_UPDATE_DELAY = 1500;
 	
 	// Constants: Where should we center to on the next game update cycle?
 	public static final int CENTER_NONE = -1;
 	public static final int CENTER_ORIGIN = 0;
 	public static final int CENTER_SELF = 1;
 	public static final int CENTER_RED = 2;
 	public static final int CENTER_BLUE = -3;
 		
 	// Constant: What is the minimum accuracy (in meters) we should expect ?
 	public static final int MIN_ACCURACY = 40;	
 		
 	// Drawables hash map
 	private HashMap<Integer,Drawable> drawable = new HashMap<Integer, Drawable>(10);
 	
 	// Application Mechanics
 	private PowerManager.WakeLock ctfWakeLock;
 	
 	// Map Mechanics
 	private MapView mapView;
 	private boolean hasCenteredOrigin = false;
 	private List<Overlay> mapOverlay;
 	private GameCTFOverlays mapOverlayMarkers;
 	
 	// Game Mechanics
 	private GameData myGameData;
 	private GameMenu myMenu;
 	private GameInfoBar myInfoBar;
 	private GameScores myScores;
 	private TaskGameProcess cycle;
 	private Handler gameHandler = new Handler();
 	
 	/*----------------------------------*
 	/* ACTIVITY RELATED FUNCTIONS START
 	/*----------------------------------*/
 	/**
 	 * Called when the activity is first created.
 	 * Default behavior is NULL. Nothing is happening yet!
 	 * @param saved instance state
 	 */
 	public void onCreate(Bundle savedInstanceState) {
 		
 		Log.i(TAG, "Starting map activity...");
 		
 		// Restore a saved instance of the application
 		super.onCreate(savedInstanceState);
 				
 		// Make sure the user is actually in a game
 		if(CurrentUser.getGameId().equals("")) {
 			this.finish();
 			return;
 		}
 		
 		// Setup the wake lock
 		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
 		ctfWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, TAG);
 		
 		// Move back to the game selection panel
 		setContentView(R.layout.game);
 		
 		// Create the menu [must be created after setContentView() is run]
 		myMenu = new GameMenu(this);
 		myMenu.setMenu(GameMenu.MENU_DEFAULT, null, null);
 		
 		// Create the game info bar (top of screen)
 		myInfoBar = new GameInfoBar(this);
 		myInfoBar.setLoading();
 		
 		// Create the class for game scores
 		myScores = new GameScores(this);
 		
 		// Make sure gps is running at the right speed
 		CurrentUser.userLocation((LocationManager) this.getSystemService(Context.LOCATION_SERVICE), StetsonCTF.GPS_UPDATE_FREQUENCY_GAME);
 		
  		// Turns on built-in zoom controls and satellite view
 		mapView = (MapView) findViewById(R.id.mapView);
 		mapView.getController();
 		mapView.setBuiltInZoomControls(true);
 		mapView.setSatellite(true);
 		
 		// Build everything we need to run
 		buildDrawables();
 				
 	    // Setup overlay stuff
 		mapOverlay = mapView.getOverlays();
         mapOverlayMarkers = new GameCTFOverlays(drawable.get(R.drawable.star), this);
         
         // Start game processing
         myGameData = new GameData();
 		gameHandler.postDelayed(gameProcess, GAME_UPDATE_DELAY);
 
 	}
 
 	/**
 	 * Run when the activity is stopped, such as .finish()
 	 */
 	public void onDestroy() {
 		
 		// Stop any running cycle that may exist
 		if(cycle != null) {
 			cycle.cancel(true);
 			cycle = null;
 		}
 		
 		// Remove the user from the game on the front end
 		CurrentUser.setGameId("");
 		CurrentUser.setLocation(-1, -1);
 		CurrentUser.setAccuracy(-1);
 		
 		// Remove the user from the game on the back end
 		// We're using an anonymous thread here because we don't care about the response
 		// This is not a strictly required event
 		new Thread(new Runnable() {
 			public void run() {
 				/*
 				 * Leaving games has been implemented on the backend, but we cannot use them
 				 * without re-writing our connections base and overriding a bunch of functions
 				 * for HttpDelete. The specification for HTTP DELETE says that an entity should 
 				 * not be present and the APIs for connections was written accordingly. The server
 				 * is expecting an entity which we cannot provide without a lot of work and the
 				 * Violation of the HTTP DELETE specification.
 				 * - Jeremy
 				String gameUrl = CurrentUser.getGameId().replaceAll(" ", "%20");
 				HttpDelete req = new HttpDelete(StetsonCTF.SERVER_URL + "/game/" + gameUrl);
 				req.setEntity(CurrentUser.buildHttpParams(CurrentUser.LEAVE_PARAMS));
 				String data = Connections.sendRequest(req);
 				Log.i(TAG, "LEAVE GAME => " + data);
 				 */
 			}
 		});
 			  
 		// Call last
 		super.onDestroy();
 	}
 	
 	/**
 	 * The activity has regained focus. Acquire a wake lock.
 	 */
 	public void onResume() {
 	    super.onResume();
 	    ctfWakeLock.acquire();
 	}
 	
 	/**
 	 * The activity has lost focus. Release the wake lock.
 	 */
 	public void onPause(){
 	   super.onPause();
 	   ctfWakeLock.release();
 	}
 	
 	/**
 	 * Handles action when the Menu button is pressed.
 	 * Toggles the graphical menu.
 	 */
 	public boolean onCreateOptionsMenu(Menu x) {
 		
 		myMenu.toggleMenu();
 		
 		// True means the native menu was launched successfully, so we must return false!
 		return false;
 		
 	}
 			
 	/**
 	 * Centers the map view on the requested centering target.
 	 */
 	public void centerMapView(int isCentering) {
 		
 		// We can't center if there isn't any data!
 		if(myGameData == null || myGameData.hasError()) {
 			return;
 		}
 		
 		GeoPoint location;
 		switch(isCentering) {
 		
 			case CENTER_ORIGIN:
 				location = myGameData.getOrigin();
 				break;
 				
 			case CENTER_SELF:
 				location = new GeoPoint((int) (1E6 * CurrentUser.getLatitude()), (int) (1E6 * CurrentUser.getLongitude()));
 				break;
 				
 			case CENTER_RED:
 				location = myGameData.getRedFlag();
 				break;
 				
 			case CENTER_BLUE:
 				location = myGameData.getBlueFlag();
 				break;
 				
 			default:
 				return;
 		}
 		
 		// Reset centering
 		isCentering = CENTER_NONE;
 		
 		// Move the map view
 		if(location != null) {
 			mapView.getController().animateTo(location);
 		}
 		
 	}
 	
 	/**
 	 * Sets up all the drawables to be used as markers
 	 */
 	private void buildDrawables() {
 		
 		// All of our markers here
 		drawable.put(R.drawable.star, this.getResources().getDrawable(R.drawable.star));
 		drawable.put(R.drawable.red_flag, this.getResources().getDrawable(R.drawable.red_flag));
 		drawable.put(R.drawable.blue_flag, this.getResources().getDrawable(R.drawable.blue_flag));
 		drawable.put(R.drawable.person_red_owner, this.getResources().getDrawable(R.drawable.person_red_owner));
 		drawable.put(R.drawable.person_blue_owner, this.getResources().getDrawable(R.drawable.person_blue_owner));
 		drawable.put(R.drawable.person_red, this.getResources().getDrawable(R.drawable.person_red));
 		drawable.put(R.drawable.person_blue, this.getResources().getDrawable(R.drawable.person_blue));
 		drawable.put(R.drawable.grey_observer, this.getResources().getDrawable(R.drawable.grey_observer));
 		drawable.put(R.drawable.grey_observer_owner, this.getResources().getDrawable(R.drawable.grey_observer_owner));
 		
 		// Set the anchors here
 		Collection<Drawable> c = drawable.values();
 	    Iterator<Drawable> itr = c.iterator();
 	    while(itr.hasNext()) {
 	    	Drawable icon = itr.next();
 			int redW = icon.getIntrinsicWidth();
 			int redH = icon.getIntrinsicHeight();
 			icon.setBounds(-redW / 2, -redH, redH / 2, 0);
 	  	}
 
 	}
 	
 	/**
 	 * Returns false (required by MapActivity)
 	 * @return false
 	 */
 	protected boolean isRouteDisplayed() {
 		return false;
 	}	
 	
 	/**
 	 * Game processor. Runs the GameProcess task every GAME_UPDATE_DELAY (ms).
 	 */
 	private final Runnable gameProcess = new Runnable() {
 	    public void run() {
 			// Run the task only if the previous one is null or finished
 			// AsyncTasks are designed to run only ONCE per lifetime
 			if(cycle == null || cycle.getStatus() == AsyncTask.Status.FINISHED) {
 	    		cycle = new TaskGameProcess();
 	    		cycle.execute();
 			}
 			
 			// Call for another execution later
 			gameHandler.postDelayed(this, GAME_UPDATE_DELAY);
 	
 	   }
 	};
 	
 	/**
      * The AsyncTask used for processing game updates.
      * (Generics: Params, Progress, Result)
      */
 	public class TaskGameProcess extends AsyncTask<Void, Void, JSONObject> {
 		
 		/**
 		 * Run as the work on another thread.
 		 * Sends a location update and grabs data from the server.
 		 */
 		protected JSONObject doInBackground(Void... params) {
 			
 			Log.i(TAG, "Grabbing game data...");
 			
 			HttpPost req = new HttpPost(StetsonCTF.SERVER_URL + "/location/");
 			req.setEntity(CurrentUser.buildHttpParams(CurrentUser.UPDATE_PARAMS));
 			String data = Connections.sendRequest(req);
 			try {
 				JSONObject jObject = new JSONObject(data);
 				return jObject;
 			} catch (JSONException e) {
 				Log.e(TAG, "Error parsing JSON.", e);
 			}
 			
 			// If we get here, we had problems with json.
 			return null;
 			
 		}
 		
 		/**
 		 * Run after execution on the UI thread.
 		 * Processes the game object retrieved from the worker thread.
 		 * If game is NULL then the game will be stopped and the activity terminated.
 		 */
 		protected void onPostExecute(final JSONObject gameObject) {		
 			
 			myGameData.parseJSONObject(gameObject);
 			
 			// Handle Errors
 			if(myGameData.hasError()) {
 				Log.e(TAG, "Error: " + myGameData.getErrorMessage());
 				return;
 			}
 			
 			// No errors, parsing was a success!
 			Log.i(TAG, "GameProcess()...");
 			
 			// Is this the first map data we have gotten?
			if(!hasCenteredOrigin) {
				
 				hasCenteredOrigin = true;
				
				GeoPoint topLeft = myGameData.getBounds().getTopLeft();
				GeoPoint botRight = myGameData.getBounds().getBottomRight();
				
				int maxLatitude = topLeft.getLatitudeE6();
				int minLatitude = botRight.getLatitudeE6();
				int maxLongitude = topLeft.getLongitudeE6();
				int minLongitude = botRight.getLongitudeE6();
				
				mapView.getController().zoomToSpan((maxLatitude - minLatitude),(maxLongitude - minLongitude));
				centerMapView(GameCTF.CENTER_ORIGIN);
 			}
 			
 			// Remove all overlay items
 			mapOverlay.clear();
 			mapOverlayMarkers.clear();
 						
 			// Update game info bar
 			myInfoBar.update(myGameData.getRedScore(), myGameData.getBlueScore(), CurrentUser.getAccuracy());
 			
 			// Add Flags
 			if(!myGameData.isRedFlagTaken()) {
 				OverlayItem redFlag = new OverlayItem(myGameData.getRedFlag(), GameCTFOverlays.OVERLAY_FLAG, "Red Flag");
 				redFlag.setMarker(drawable.get(R.drawable.red_flag));
 				mapOverlayMarkers.addOverlay(redFlag);
 				Log.i(TAG, "Added red flag.");
 			}
 			
 			if(!myGameData.isBlueFlagTaken()) {
 				OverlayItem blueFlag = new OverlayItem(myGameData.getBlueFlag(), GameCTFOverlays.OVERLAY_FLAG, "Blue Flag");
 				blueFlag.setMarker(drawable.get(R.drawable.blue_flag));
 				mapOverlayMarkers.addOverlay(blueFlag);
 				Log.i(TAG, "Added blue flag.");
 			}
 			
 			// Add players
 			Player player;
 			
 			for(int p = 0; p < myGameData.getPlayerCount(); p++) {
 				
 				player = myGameData.getPlayer(p);
 	
 				Log.i(TAG, "Added player: " + player.getName());
 				
 				GeoPoint playerPoint = new GeoPoint(player.getLatitude(), player.getLongitude());
 				OverlayItem playerItem = new OverlayItem(playerPoint, GameCTFOverlays.OVERLAY_PLAYER, player.getUID());
 				
 				boolean isCurrentPlayer = player.getUID().equals(CurrentUser.getUID());
 				
 				// if player is observer, we don't care about their team
 				if(player.hasObserverMode()) {
 					if(isCurrentPlayer) {
 						playerItem.setMarker(drawable.get(R.drawable.grey_observer_owner));
 					} else {
 						playerItem.setMarker(drawable.get(R.drawable.grey_observer));
 					}
 				
 				// if player is on the red team
 				} else if (player.getTeam().equals("red")) {
 					
 					// Default marker for a red member
 					playerItem.setMarker(drawable.get(R.drawable.person_red));
 					
 					// Logical order: flag, observer, self
 					if(player.hasFlag()) {
 						playerItem.setMarker(drawable.get(R.drawable.blue_flag));
 					} else if(isCurrentPlayer) {
 						playerItem.setMarker(drawable.get(R.drawable.person_red_owner));
 					}
 				
 				// if player is on the blue team
 				} else if(player.getTeam().equals("blue")) {
 					
 					// Default marker for a blue member
 					playerItem.setMarker(drawable.get(R.drawable.person_blue));
 					
 					// Logical order: flag, observer, self
 					if(player.hasFlag()) {
 						playerItem.setMarker(drawable.get(R.drawable.red_flag));
 					} else if(isCurrentPlayer) {
 						playerItem.setMarker(drawable.get(R.drawable.person_blue_owner));
 					}
 				}
 
 				mapOverlayMarkers.addOverlay(playerItem);
 				
 				
 			}
 			
 			// Add boundaries & markers
 			mapOverlay.add(myGameData.getBounds());
 		    mapOverlay.add(mapOverlayMarkers);
 		    
 		    // Let the make know we're done!
 		    mapView.invalidate();
 			
 		}
 	}
 	
 	/**
 	 * Returns a reference to the game's menu
 	 * @return
 	 */
 	public GameScores getGameScores() {
 		return myScores;
 	}
 	
 	/**
 	 * Returns a reference to the game's menu
 	 * @return
 	 */
 	public GameMenu getGameMenu() {
 		return myMenu;
 	}
 	
 	/**
 	 * Returns a reference to the game's game data
 	 * @return
 	 */
 	public GameData getGameData() {
 		return myGameData;
 	}
 
 }
