 package stetson.CTF;
 
 import java.util.ArrayList;
 
 import stetson.CTF.Join.GameItem;
 import stetson.CTF.Join.GamesList;
 import stetson.CTF.utils.Connections;
 import stetson.CTF.utils.CurrentUser;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.location.LocationManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.Toast;
 
 public class JoinCTF extends Activity {
 	
 	// Constants: To be used across entire application
 	public static final String TAG = "StetsonCTF";
 	public static final String SERVER_URL = "http://ctf.no.de";
 	
 	// Constants: GPS Update Frequency
 	public static final int GPS_UPDATE_FREQUENCY_GAME = 3000;
 	public static final int GPS_UPDATE_FREQUENCY_INTRO = 10000;
 	public static final int GPS_UPDATE_FREQUENCY_BACKGROUND = 60000;
 	
 	// Constants: GPS Update Threshold
 	public static final int GPS_UPDATE_DISTANCE_GAME = 0;
 	public static final int GPS_UPDATE_DISTANCE_INTRO = 1;
 	public static final int GPS_UPDATE_DISTANCE_BACKGROUND = 10;
 	
 	// Data Members
 	public static boolean firstStart = true;
 	private GamesList myGamesList;
 
 	/**
 	 * Called when the activity is first created.
 	 * @param saved instance state
 	 */
 	public void onCreate(Bundle savedInstanceState) {
 		
 		Log.i(TAG, "Starting activity...");
 		
 		// Restore a saved instance of the application
 		super.onCreate(savedInstanceState);
 		
 		new Thread(new Runnable() {
 		    public void run() {
 		    	if(JoinCTF.firstStart)
 				{
 					Intent titleScreen = new Intent(getBaseContext(), IntroCTF.class);
 					startActivity(titleScreen);
 					JoinCTF.firstStart = false;
 				}
 		    }
 		  }).start();
 		
 		// Move back to the game selection panel
 		setContentView(R.layout.join);
 		
 		// Startup list view
 		myGamesList = new GamesList(this);
 	    
 		// Grab a list of games on start up
 		buildGamesList();
 		
 		// Build listeners
 		buildListeners();
 		
 		Log.i(TAG, "Activity ready!");
 		
 	}
 	
 	/**
 	 * Start GPS and rebuild games list.
 	 */
 	public void onResume() {
 		
 		super.onResume();
 		
 		// Start GPS
 		CurrentUser.userLocation((LocationManager) this.getSystemService(Context.LOCATION_SERVICE), GPS_UPDATE_FREQUENCY_INTRO);
 	}
 	
 	/**
 	 * Slow down GPS updates a lot when the application is in the background.
 	 */
 	public void onPause() {
 
 		super.onPause();
 		
 		// Stop GPS
 		CurrentUser.stopLocation((LocationManager) this.getSystemService(Context.LOCATION_SERVICE));
 
 	}
 	
 	/**
 	 * The activity has ended, stop all GPS updates.
 	 */
 	public void onDestroy() {
 
 		super.onDestroy();
 		
 		// Stop GPS
 		CurrentUser.stopLocation((LocationManager) this.getSystemService(Context.LOCATION_SERVICE));
 		firstStart = true;
 		CurrentUser.setName("");
 		
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
 				
 				// Create the new game :)
 				joinGame(CurrentUser.getName(), "");
 				
 			}
 		});
 
 		
 		// refresh list of games when user clicks
 		final Button refreshButton = (Button) findViewById(R.id.refresh_button);
 		refreshButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View view) {
 				buildGamesList();
 			}
 		});
 
 	}
 	
 	/**
 	 * Retrieves and displays a new games list
 	 */
 	private void buildGamesList() {
 		
 		Log.i(TAG, "(UI) Building games list.");
 		new TaskGenerateGamesList().execute();
 		
 	}
 
 	/**
 	 * Joins or creates a new game. If game is empty, then a new game will be created.
 	 * @param name
 	 * @param game
 	 */
     protected void joinGame(String name, String game) {
     	Log.i(TAG, "(UI) joinGame(" + name + ", " + game + ")");
     	new TaskJoinGame().execute(name, game);
     }
     
    
     
     /**
      * The AsyncTask used for generating a new list of games.
      * (Generics: Params, Progress, Result)
      */
     private class TaskGenerateGamesList extends AsyncTask<Void, String, ArrayList<GameItem>> {
 		
     	protected static final long GPS_CHECK_PAUSE = 500;
 		
 		/**
 		 * Run before execution on the UI thread.
 		 * Remove all children from group view and post a loading message.
 		 */
 		protected void onPreExecute() {
 			myGamesList.clearList();
 			myGamesList.updateList();
 		}
 	     
 		/**
 		 * Run after execution on the UI thread.
 		 * Clears the gameGroup view and adds a list of games to it.
 		 */
 		protected void onPostExecute(final ArrayList<GameItem> response) {
 			
 			myGamesList.setErrorText("");
 			
 			// If there is a problem, or no games, display an error
 			if(response == null) {
 				myGamesList.setErrorText(getString(R.string.no_games_error));
 			} else if(response.isEmpty()) {
				myGamesList.setErrorText(getString(R.string.no_games_error));
 				
 			// If there are games, add them to the list :)
 			} else {
 				
 				for(int i = 0; i < response.size(); i++) {
 					myGamesList.addGame(response.get(i));
 				}
 				myGamesList.updateList();
 				
 			}
 		}
 		
 		/**
 		 * Runs every time publicProgress() is called.
 		 * Clears the gamesGroup view and adds a message with the progress text.
 		 */
 	     protected void onProgressUpdate(String... progress) {
 	    	 myGamesList.setErrorText(progress[0]);
 	     }
 	     
 		/**
 		 * Run as the work on another thread.
 		 */
 		protected ArrayList<GameItem> doInBackground(Void... params) {
 			
 			this.publishProgress(JoinCTF.this.getString(R.string.loading_location));
 			
 			while(!CurrentUser.hasLocation()) {
 				try {
 					Thread.sleep(GPS_CHECK_PAUSE);
 				} catch (InterruptedException e) {
 					Log.e(TAG, "Can't sleep :(", e);
 				}
 			}
 			
 			this.publishProgress(JoinCTF.this.getString(R.string.loading_games));
 			
 			return Connections.getGames();
 		}
     }
     
     /**
      * The AsyncTask used for creating and joining a game.
      * (Generics: Params, Progress, Result)
      */
 	private class TaskJoinGame extends AsyncTask<String, Void, String> {
 		
 		private final static String GOOD_RESPONSE = "OK";
 		private Context mContext = JoinCTF.this;
 		private ProgressDialog dialog;
 		
 		/**
 		 * Run before execution on the UI thread.
 		 */
 		protected void onPreExecute() {
 			dialog = ProgressDialog.show(mContext, "Joining Game", "Please wait...", true);
 		}
 		
 		/**
 		 * Run after execution on the UI thread.
 		 * If the response string is equal to GOOD_RESPONSE then the game activity
 		 * is started. If not, a toast showing the error message is sent.
 		 */
 		protected void onPostExecute(final String response) {
 			dialog.hide();
 			if(response.equals(GOOD_RESPONSE)) {
 			    Intent i = new Intent(mContext, GameCTF.class);
 			    startActivity(i);
 			} else {
 				Toast.makeText(mContext, response, Toast.LENGTH_LONG).show();
 			}
 		}
 		
 		/**
 		 * Run as the work on another thread.
 		 */
 		protected String doInBackground(final String... params) {
 			return Connections.joinOrCreate(params[0], params[1]);
 		}
 	}
 	
 	/**
 	 * Public join game call to be used from GamesList.java ONLY!
 	 * @param gameName
 	 */
 	public void joinGame(String gameName) {
 		CurrentUser.setGameId(gameName);
 		joinGame(CurrentUser.getName(), gameName);
 	}
 	
 }
 
 
 
