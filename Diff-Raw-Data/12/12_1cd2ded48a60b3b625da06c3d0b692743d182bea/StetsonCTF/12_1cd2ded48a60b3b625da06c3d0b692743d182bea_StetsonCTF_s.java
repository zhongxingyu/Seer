 package stetson.CTF;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class StetsonCTF extends Activity {
 	
 	// All constants for Application
 	public static final String TAG = "StetsonCTF";
 	public static final String NO_GAMES_RESPONSE = "[]";
 	public static final String SERVER_URL = "http://ctf.no.de";
 	public static final String CREATE_SUCCESS = "{\"response\":\"OK\"}";
 	public static final String JOIN_FAILED = "{\"error\":\"Could not join game\"}";
 	
 	public static final int GPS_UPDATE_FREQUENCY_INTRO = 10000;
 	public static final int GPS_UPDATE_FREQUENCY_GAME = 3000;
 	// meters
 	public static final int GPS_UPDATE_DISTANCE = 0;
 	
 	public static final int LOADING_PAUSE = 1000;
 	
 	private Handler gamesHandler;
 	private LocationManager locationManager;
 	private LocationListener locationListener;
 	
 	/**
 	 * Called when the activity is first created.
 	 * @param saved instance state
 	 */
 	public void onCreate(Bundle savedInstanceState) {
 		
 		Log.i(TAG, "Starting activity...");
 		
 		// Restore a saved instance of the application
 		super.onCreate(savedInstanceState);
 		
 		// Move back to the game selection panel
 		setContentView(R.layout.intro);
 		
 		// Connect components
 		buildListeners();
 		gamesHandler = new Handler();
 
 		Log.i(TAG, "Activity ready!");
 	}
 	
 	/**
 	 * Rebuild games list when the application regains focus.
 	 * (After leave a game, answering a call, etc...)
 	 */
 	public void onStart() {
 		
 		super.onStart();
 		buildGamesList();
 		
 		// Whenever this activity focus, slow down gps updates
 		userLocation(GPS_UPDATE_FREQUENCY_INTRO);
 	}
 		
 	/**
 	 * Connects the view components to listeners
 	 */
 	private void buildListeners() {
 		
 		Log.i(TAG, "Prepare listeners.");
 		
 		// Create a new game
 		final Button newGameButton = (Button) findViewById(R.id.newgame_button);
 		newGameButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View view) {
 				
 				// Get name
 				EditText et = (EditText) findViewById(R.id.name_text);
 				CurrentUser.setName(et.getText().toString());
 				
 				// Create the new game :)
 				joinGame(CurrentUser.getName(), "");
 				
 			}
 		});
 		
 		// Join a game
 		final Button joinGameButton = (Button) findViewById(R.id.joingame_button);
 		joinGameButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View view) {
 				RadioGroup gamesGroup = (RadioGroup) findViewById(R.id.games_list_group);
 				String game = "";
 
 				int selected = gamesGroup.getCheckedRadioButtonId();
 				
 				// Join the specified game
 				if(selected > -1) {
 					RadioButton rb = (RadioButton) findViewById(selected);
 					game = (String) rb.getText();
 					CurrentUser.setGameId(game);
 					
 					// Get name
 					EditText et = (EditText) findViewById(R.id.name_text);
 					CurrentUser.setName(et.getText().toString());
 					
 					// Create the new game :)
 					joinGame(CurrentUser.getName(), CurrentUser.getGameId());
 					
 				// No game selected, notify user
 				} else {
 					Toast.makeText(view.getContext(), R.string.no_game_selected, Toast.LENGTH_SHORT).show();
 				}
 
 			}
 		});
 		
 	}
 	
 	/**
 	 * Retrieves and displays a new games list
 	 */
 	private void buildGamesList() {
 		
 		Log.i(TAG, "Build games list. (Loading...)");
 		
 		// Start the handle (so we don't weigh down the thread)
 		gamesHandler.post(new Runnable() {
 			public void run() {
 				
 				// Clear all games and messages
 				RadioGroup gamesGroup = (RadioGroup) findViewById(R.id.games_list_group);
 				gamesGroup.removeAllViews();
 				
 				TextView loadText;
 				
 				// Acquiring location
 				if(!CurrentUser.hasLocation()) {
 					
 					// Post a message
 					loadText = new TextView(gamesGroup.getContext());
 					loadText.setText(R.string.loading_location);
 					gamesGroup.addView(loadText);
 					
 					// Check again in a little bit
 					gamesHandler.postDelayed(this, LOADING_PAUSE);
 					
 				// Acquiring games
 				} else {
 					
 					// Post a message
 					loadText = new TextView(gamesGroup.getContext());
 					loadText.setText(R.string.loading_games);
 					gamesGroup.addView(loadText);
 					
 					// Build an send a request for game data
 					HttpGet req = new HttpGet(SERVER_URL + "/game/?" + CurrentUser.buildQueryParams());
 					Connections.sendRequest(req, new ResponseListener() {
 
 						public void onResponseReceived(HttpResponse response) {
 							
 							RadioGroup gamesGroup = (RadioGroup) findViewById(R.id.games_list_group);
 							gamesGroup.removeAllViews();
 							
 							// Pull response message
 							String data = Connections.responseToString(response);
 							Log.i(TAG, "Response: " + data);
 							
 							// Sloppy server-side code requires use to check if the text is an object or array
 							try {
 
 								// JSON Array
 								if(data.charAt(0) == '[') {
 									
 									JSONArray gameArray = new JSONArray(data);
 									
 									// Add games to list
 									RadioButton rb;
 									int index = 0;
 									while(!gameArray.optString(index).equals("")) {
 										
 										Log.i(TAG, "Adding game to view (" + gameArray.optString(index) + ")");
 										
 										rb = new RadioButton(gamesGroup.getContext());
 										rb.setText(gameArray.optString(index));
 										gamesGroup.addView(rb);
 										index ++;
 									}
 									
 									// No games!
 									if(index == 0) {
 										// Post a message
 										TextView noGamesText = new TextView(gamesGroup.getContext());
 										noGamesText.setText(R.string.no_games);
 										gamesGroup.addView(noGamesText);
 									}
 
 								// JSON Object
 								} else {
 									
 									JSONObject jObject = new JSONObject(data);
 									
 									// This should never happen, but if it does handle it
 									if(jObject.has("error")) {
 										// STUB
 										Log.e(TAG, "Server Error: " + jObject.getString("error"));
 									} else {
 										// STUB
 										Log.e(TAG, "Unexpected Server Response: " + data);
 									}
 								}
 							} catch (JSONException e) {
 								Log.e(TAG, "Error parsing JSON.", e);
 							}
 							
 						}
 						
 					});
 
 				}
 				
 
 			}
 		});
 				
 	}
 
 	/**
 	 * Joins or creates a new game. If game is empty, then a new game will be created.
 	 * @param name
 	 * @param game
 	 * @return did the user join the game successfully
 	 */
     protected boolean joinGame(String name, String game) {
     	
     	Log.i(TAG, "joinGame(" + name + ", " + game + ")");
     	
     	// Update the user
     	updateUser(name);
     	
     	// We need to get current location data to make or join a game
 		double longitude = CurrentUser.getLongitude();
 		double latitude = CurrentUser.getLatitude();
 		CurrentUser.setLocation(latitude, longitude);
 
 		// Show the user a loading screen thingy
 		ProgressDialog dialog = ProgressDialog.show(this, "", "Loading. Please wait...", true);
     	
 		// If a game name wasn't provided, lets make a game! Build the request and stuff :)
 		if(game.equals("")) {
 			
 			// Set the game name to the user's name
 			CurrentUser.setGameId(CurrentUser.getName());
 			
 			// Make the request, NOT asynchronous, we need an answer now
 			HttpPost hp = new HttpPost(SERVER_URL + "/game/");
 			CurrentUser.buildHttpParams(hp, CurrentUser.CREATE_PARAMS);
 			String data = Connections.sendFlatRequest(hp);
 			if(!data.equals(CREATE_SUCCESS)) {
 				dialog.hide();
 				Toast.makeText(this, R.string.failed_to_create, Toast.LENGTH_SHORT).show();
 				return false;
 			}
 
 		}
 		
 		// Join the game!
 		
 		String gameUrl = CurrentUser.getGameId().replaceAll(" ", "%20");
 		HttpPost hp = new HttpPost(SERVER_URL + "/game/" + gameUrl);
 		CurrentUser.buildHttpParams(hp, CurrentUser.JOIN_PARAMS);
 		String data = Connections.sendFlatRequest(hp);
 		
 		// This needs to be improved at some point for better error checking
 		if(data.equals(JOIN_FAILED)) {
 			dialog.hide();
 			Toast.makeText(this, R.string.failed_to_join, Toast.LENGTH_SHORT).show();
 			return false;
 		}
 		
 		// Speed up gps location polling for game player
 		userLocation(GPS_UPDATE_FREQUENCY_GAME);
 		
 		// We don't need loading stuff anymore + start the map activity
 		dialog.hide();
 	    Intent i = new Intent(this, GameCTF.class);
 	    startActivity(i);
 	    return true;
     }
     
     /**
      * Sets the user's name and generates a new UID.
      * @param name
      */
     protected void updateUser(String name) {
     	
     	// New name
     	CurrentUser.setName(name);
     	
 		// Generate a new uid
 		String uid = "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx";
 		while(uid.contains("x")) 
 		uid = uid.replaceFirst("x", Long.toHexString(Math.round(Math.random() * 16.0)));
 		uid = uid.toUpperCase();
 		CurrentUser.setUID(uid);
     }
     
     /**
 	 * Periodically updates the users location.
 	 */
 	protected void userLocation(int frequency) {
 		
 		// Setup our location manager
 		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
 			
 		// Setup our location listener
 		locationListener = new LocationListener() {
 				
 			public void onLocationChanged(Location location) {
 				Log.i(TAG, "Update Location.");
 				CurrentUser.setLocation(location.getLatitude(), location.getLongitude());
 				CurrentUser.setAccuracy((double)location.getAccuracy());
 			}
 	
 			public void onStatusChanged(String provider, int status, Bundle extras) {}
 			public void onProviderEnabled(String provider) {}
 			public void onProviderDisabled(String provider) {}
 				
 		};
 		
 		// Remove all updates for the current listener (if one exists)
 		locationManager.removeUpdates(locationListener);
 		
 		// Start requesting updates per given time
 		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, frequency, GPS_UPDATE_DISTANCE, locationListener);
 		
 	}
 	
 }
