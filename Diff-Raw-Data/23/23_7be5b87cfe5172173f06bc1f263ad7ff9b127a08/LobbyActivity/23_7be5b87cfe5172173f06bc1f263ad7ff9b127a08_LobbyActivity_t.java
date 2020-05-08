 package com.example.menschapp;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import com.example.menschapp.LoginActivity.UserLoginTask;
 import com.example.menschapp.util.Games;
 import com.example.menschapp.util.MenschSystemStub;
 
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.animation.Animator;
 import android.animation.AnimatorListenerAdapter;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListAdapter;
 import android.widget.TextView;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 
 public class LobbyActivity extends Activity {
 	
 	private static int gameid;
     ListView gameListView;
     ArrayAdapter arrayAdapter;
 	String[] valueList;
 	private GameListTask listTask = null;
 	private NewGameTask newGameTask = null;
 	ArrayList<Games> gamesArray = new ArrayList<Games>();
 	ArrayList<String> games = new ArrayList<String>();
 	private TextView GameListStatusView;
 	private SharedPreferences prefs;
 	private MenschApplication obsApp;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_lobby);
 		GameListStatusView = (TextView) findViewById(R.id.login_status_message);
         /* Initialisiere den Stub zum GameServer */
         obsApp = (MenschApplication) this.getApplication();
         obsApp.setObsStub(new MenschSystemStub());
         
 	    // Initialize the array
 
 	    // Declare the UI components
 	
 	        // Initialize the UI components
         gameListView = (ListView) findViewById(R.id.lobbylist);
 	        // For this moment, you have ListView where you can display a list.
 	        // But how can we put this data set to the list?
 	        // This is where you need an Adapter
 	 
 	        // context - The current context.
 	        // resource - The resource ID for a layout file containing a layout 
 	                // to use when instantiating views.
 	        // From the third parameter, you plugged the data set to adapter
 
 	    arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, games);
 //	 
 //	        // By using setAdapter method, you plugged the ListView with adapter
 	    gameListView.setAdapter(arrayAdapter);
 //	    
 		listTask = new GameListTask(); 
 		listTask.execute();
 		
 		gameListView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
 	          public void onItemClick(AdapterView<?> myAdapter, View myView, int myItemInt, long mylng) {
 
 				Intent myIntent = new Intent(gameListView.getContext(), GameDetailActivity.class);
				int gamelistid = gamesArray.get(myItemInt).getId();

				Log.d("gameid:", ""+gamelistid);
				myIntent.putExtra("gameid", gamelistid);
 				Log.d("intent", ""+myIntent.getIntExtra("gameid", myItemInt));
 	            startActivityForResult(myIntent, 0);
 	          }
 		});
 
 		findViewById(R.id.update_pic).setOnClickListener(new View.OnClickListener() {
 			
 			public void onClick(View v) {
 				GameListStatusView.setText(R.string.lobby_progress_gamelist);
 				listTask = new GameListTask(); 
 				listTask.execute();
 				
 			}
 		});
 		
 		findViewById(R.id.create_game).setOnClickListener(new View.OnClickListener() {
 			
 			public void onClick(View v) {
 				newGameTask = new NewGameTask();
 				newGameTask.execute();
 				
 			}
 		});
 		try {
 			Thread.sleep(1000);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		int delay = 1000; // delay for 1 sec. 
 		int period = 10000; // repeat every 10 sec. 
 		Timer timer = new Timer(); 
 		timer.scheduleAtFixedRate(new TimerTask() 
 		    { 
 		        public void run() 
 		        { 
 		        	listTask = new GameListTask();
 		            listTask.execute();  // display the data
 		        } 
 		    }, delay, period);
 		
 	}
 	
 //	protected void onResume(){
 //		listTask = new GameListTask(); 
 //		listTask.execute();
 //	}
 	public void createGame(){
 
 		Intent myIntent = new Intent(gameListView.getContext(), GameDetailActivity.class);
 		myIntent.putExtra("gameid", gameid);
 		myIntent.putExtra("host", true);
 		Log.d("intent", ""+myIntent.getIntExtra("gameid", gameid));
         startActivityForResult(myIntent, 0);
 
 	}
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.lobby, menu);
 		//You must return true for the menu to be displayed; if you return false it will not be shown.
 		return true;
 	}
 
 	/**
 	 * Diese Methode wird vom Container aufgerufen, wenn ein Menue-Eintrag ausgewaehlt wird.
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id.itemPrefs :
 				startActivity(new Intent(this, SettingsActivity.class));
 				break;
 		}
 			
 		return true;
 	}
 
 	public class GameListTask extends AsyncTask<String, Void, Boolean> {
 				
 	    @Override
 		protected Boolean doInBackground(String... params) {
 			// TODO: attempt authentication against a network service.
 		    gamesArray = LobbyActivity.this.obsApp.getObsStub().getGames();	    
 		    
 		    Log.d("GAME LISTE", ""+gamesArray);
     
 	        try {
 				// Simulate network access.
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 				return false;
 			}
 			return true;
 		}
 		
 		@Override
 		protected void onPostExecute(final Boolean success) {
 
 			games.clear();
 			for(int i = 0; i < gamesArray.size(); i++) {
 				games.add(gamesArray.get(i).toString());
 			}
 		    
 			arrayAdapter.notifyDataSetChanged();
 		}
 
 		@Override
 		protected void onCancelled() {
 
 		}
 	}
 
 	public class NewGameTask extends AsyncTask<String, Void, Boolean> {
 				
 	    @Override
 		protected Boolean doInBackground(String... params) {
 			// TODO: attempt authentication against a network service.
 
 		    Games game = LobbyActivity.this.obsApp.getObsStub().createGame();	    
 
 	        gameid = game.getId();
 	        	
 	        try {
 				// Simulate network access.
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 				return false;
 			}
 			return true;
 		}
 		
 		@Override
 		protected void onPostExecute(final Boolean success) {
 			createGame();
 		}
 	
 		@Override
 		protected void onCancelled() {
 	
 		}
 	}
 }
