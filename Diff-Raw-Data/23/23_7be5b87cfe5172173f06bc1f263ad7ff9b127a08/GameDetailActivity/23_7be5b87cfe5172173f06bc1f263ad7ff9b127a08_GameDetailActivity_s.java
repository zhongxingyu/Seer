 package com.example.menschapp;
 
 import java.util.ArrayList;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import com.example.menschapp.LobbyActivity.GameListTask;
 import com.example.menschapp.util.Games;
 import com.example.menschapp.util.MenschSystemStub;
 import com.example.menschapp.util.Request;
 
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.animation.Animator;
 import android.animation.AnimatorListenerAdapter;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class GameDetailActivity extends Activity {
 
 	private int gameid;
 	private int requestid;
 	ArrayList<Games> gamesArray = new ArrayList<Games>();
 	private TextView spieler1;
 	private TextView spieler2;
 	private TextView spieler3;
 	private TextView spieler4;
 	
 	ArrayList<Request> requests;
 	final Context context = this;
 	
 	  
 	
 	private boolean host=false;
 	private boolean joined=false;
 	
 	private SharedPreferences prefs;
 	private MenschApplication obsApp;
 	
 	Games gameDetail;
 	private GameDetailTask gameTask = null;
 	private SpectateGameTask specTask = null;
 	private JoinGameTask joinTask = null;
 	private LeaveGameTask leaveTask = null;
 	private CheckForMyRequestTask checkMyRequestTask=null;	//for client
 	private CheckForRequestsTask checkRequestsTask=null; 	//for host
 	private Timer checkForMyRequestTimer=null;
 	private Timer checkForRequestsTimer=null;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
         /* Initialisiere den Stub zum GameServer */
         obsApp = (MenschApplication) this.getApplication();
         obsApp.setObsStub(new MenschSystemStub());
         
 		setContentView(R.layout.activity_game_detail);
 		
 		spieler1 = (TextView) findViewById(R.id.spieler1);
 		spieler2 = (TextView) findViewById(R.id.spieler2);
 		spieler3 = (TextView) findViewById(R.id.spieler3);
 		spieler4 = (TextView) findViewById(R.id.spieler4);
 		
 		Intent myIntent = getIntent();
 		gameid = myIntent.getExtras().getInt("gameid");
 		host = myIntent.getExtras().getBoolean("host");
 		
 		gameTask = new GameDetailTask();
 		gameTask.execute();
 		
 		findViewById(R.id.mitspielen).setOnClickListener(
 				new View.OnClickListener() {
 					@Override
 					public void onClick(View view) {
 						Button a = (Button) findViewById(R.id.mitspielen);
 					    a.setText("Mitspiel-Anfrage versendet ...");
 					    a.setEnabled(false);
 						
 						joinTask=new JoinGameTask();
 						joinTask.execute();
 						
 						joined=true;
 					}
 				});
 		
 		findViewById(R.id.zuschauen).setOnClickListener(
 				new View.OnClickListener() {
 					@Override
 					public void onClick(View view) {
 //						zuschauen();
 						joined=true;
 					}
 				});
 		
 		
 		int delay = 1000; // delay for 1 sec. 
 		int period = 10000; // repeat every 1 sec. 
 		Timer timer = new Timer(); 
 		timer.scheduleAtFixedRate(new TimerTask() 
 		    { 
 		        public void run() 
 		        { 
 		        	gameTask = new GameDetailTask();
 		            gameTask.execute();  // display the data
 		        } 
 		    }, delay, period);
 		
 		 delay = 1000; // delay for 1 sec. 
 		 period = 10000; // repeat every 1 sec. 
 		
 		if(host==true){  //if is host
 			//findViewById(R.id.mitspielen).setVisibility(0);
 			joined=true;
 			checkForRequestsTimer=new Timer();
 			checkForRequestsTimer.scheduleAtFixedRate(new TimerTask() 
 		    { 
 		        public void run() 
 		        { 
 		        	checkRequestsTask = new CheckForRequestsTask();
 		        	checkRequestsTask.execute();
 		        	
 		        } 
 		    }, delay, period);
 		}
 		
 	}
 	public void onBackPressed(){
 		
 		if(joined){
 			if(leaveTask==null){
 			leaveTask = new LeaveGameTask();
 			leaveTask.execute();
 			
 			}
 		}
 		this.finish();
 	}
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.game_detail, menu);
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
 
 	public class GameDetailTask extends AsyncTask<String, Void, Boolean> {
 		
 	    @Override
 		protected Boolean doInBackground(String... params) {
 			// TODO: attempt authentication against a network service.
 			
 //		    gameDetail = GameDetailActivity.this.obsApp.getObsStub().getGameDetails(gameid);
 			gamesArray = GameDetailActivity.this.obsApp.getObsStub().getGames();
 			for(Games game : gamesArray) {
 				if(game.getId()==gameid) {
 					gameDetail = game;
 				}
 			}
 	        try {
 				// Simulate network access.
 				Thread.sleep(250);
 			} catch (InterruptedException e) {
 				return false;
 			}
 
 			return true;
 		}
 		
 		@Override
 		protected void onPostExecute(final Boolean success) {
 			spieler1.setText(gameDetail.getSpieler1());
 			if(gameDetail.getSpieler2()!=null) spieler2.setText(gameDetail.getSpieler2());
 			if(gameDetail.getSpieler3()!=null) spieler3.setText(gameDetail.getSpieler3());
 			if(gameDetail.getSpieler4()!=null) spieler4.setText(gameDetail.getSpieler4());
 		}
 
 		@Override
 		protected void onCancelled() {
 
 		}
 	}
 
 	public class JoinGameTask extends AsyncTask<String, Void, Boolean> {
 			
 		    @Override
 			protected Boolean doInBackground(String... params) {
 
 		    	Request r = GameDetailActivity.this.obsApp.getObsStub().joinGame(gameid);
 		    	requestid = r.getId();
 		    	int delay = 1000; // delay for 1 sec. 
				int period = 10000; // repeat every 10 sec. 
 				checkForMyRequestTimer=new Timer();
 				checkForMyRequestTimer.scheduleAtFixedRate(new TimerTask()  
 				    { 
 				        public void run() 
 				        { 
 				        	checkMyRequestTask= new CheckForMyRequestTask();
 				            checkMyRequestTask.execute();  // display the data
 				        } 
 				    }, delay, period);
 				
 			
 		        try {
 					// Simulate network access.
 					Thread.sleep(250);
 				} catch (InterruptedException e) {
 					return false;
 				}
 	
 				return true;
 			}
 			
 			@Override
 			protected void onPostExecute(final Boolean success) {
 				
 			}
 	
 			@Override
 			protected void onCancelled() {
 	
 			}
 		}
 
 	public class SpectateGameTask extends AsyncTask<String, Void, Boolean> {
 		
 	    @Override
 		protected Boolean doInBackground(String... params) {
 	
 	        try {
 				// Simulate network access.
 				Thread.sleep(250);
 			} catch (InterruptedException e) {
 				return false;
 			}
 	
 			return true;
 		}
 		
 		@Override
 		protected void onPostExecute(final Boolean success) {
 	
 		}
 	
 		@Override
 		protected void onCancelled() {
 	
 		}
 	}
 	public class LeaveGameTask extends AsyncTask<String, Void, Boolean> {
 		
 	    @Override
 		protected Boolean doInBackground(String... params) {
 	    	
 	    	GameDetailActivity.this.obsApp.getObsStub().leaveGame(gameid);
 	    	
 	        try {
 				// Simulate network access.
 				Thread.sleep(250);
 			} catch (InterruptedException e) {
 				return false;
 			}
 	
 			return true;
 		}
 		
 		@Override
 		protected void onPostExecute(final Boolean success) {
 			leaveTask=null;
 		}
 	
 	}
 	
 	public class CheckForMyRequestTask extends AsyncTask<String, Void, Boolean> {
 		
 	    @Override
 		protected Boolean doInBackground(String... params) {
 	    	
 	    String result = GameDetailActivity.this.obsApp.getObsStub().checkMyRequest(requestid);
 	    
 	    if(result.equals("accepted")){
 	    checkForMyRequestTimer.cancel();
 	    Button a = (Button) findViewById(R.id.mitspielen);
 	    a.setText("Mitspiel-Anfrage angenommen");
 	    a.setEnabled(false);
 	    }
 	    if(result.equals("declined")){
 		    checkForMyRequestTimer.cancel();
 		    Button a = (Button) findViewById(R.id.mitspielen);
 		    a.setText("Mitspiel-Anfrage abgelehnt");
 		    a.setEnabled(false);
 		 }
 	    try {
 				// Simulate network access.
 				Thread.sleep(250);
 			} catch (InterruptedException e) {
 				return false;
 			}
 	
 			return true;
 		}
 		
 		@Override
 		protected void onPostExecute(final Boolean success) {
 	
 		}
 	
 		@Override
 		protected void onCancelled() {
 	
 		}
 	}
 	
 	
 public class CheckForRequestsTask extends AsyncTask<String, Void, Boolean> {
 		
 	    @Override
 		protected Boolean doInBackground(String... params) {
 	    	Log.d("checkingForRequests", "checkingForRequests");
 	    	 requests = GameDetailActivity.this.obsApp.getObsStub().checkForRequests(gameid);
 	    	
 	    	
 	    	
 	    	
 	    	
 	    try {
 				// Simulate network access.
 				Thread.sleep(250);
 			} catch (InterruptedException e) {
 				return false;
 			}
 	
 			return true;
 		}
 		
 		@Override
 		protected void onPostExecute(final Boolean success) {
 	
 	//			for(Request r : requests){
 	    		
 //	    		
 //	    		newRequestDialog.setTitle("Reset...");
 //	    		
 //	    		newRequestDialog.setMessage("Are you sure?");
 //	    		newRequestDialog.setButton(whichButton, text, listener)
 //	    		newRequestDialog.setButton("OK", new DialogInterface.OnClickListener() {
 //	    		public void onClick(DialogInterface dialog, int which) {
 //	    		// here you can add functions
 //	    		}
 //	    		});
 //	    		alertDialog.setIcon(R.drawable.icon);
 //	    		alertDialog.show();
 	    		
 			AlertDialog.Builder newRequestDialogBuilder = new AlertDialog.Builder(context);
 			
 			
 			newRequestDialogBuilder.setTitle("Your Title");
 			
 			for(Request r : requests){
 				final int reqid = r.getId();
 			// set dialog message
 			newRequestDialogBuilder
 				.setMessage(r.getUserName()+" mchte beitreten. Erlauben ?")
 				.setCancelable(false)
 				.setPositiveButton("Ja",new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog,int id) {
 						// if this button is clicked, close
 						// current activity
 						
 						GameDetailActivity.this.obsApp.getObsStub().allowPlayer(reqid);
 					}
 				  })
 				.setNegativeButton("Nein",new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog,int id) {
 						// if this button is clicked, just close
 						// the dialog box and do nothing
 						GameDetailActivity.this.obsApp.getObsStub().declinePlayer(reqid);
 						dialog.cancel();
 					}
 				});
  
 				// create alert dialog
 				AlertDialog alertDialog = newRequestDialogBuilder.create();
  
 				// show it
 				alertDialog.show();
 	    		//newRequestDialog.setMessage("r.getUserName()"+" mchte beitreten");
 	    		//newRequestDialog.show();
 	    	}
 	    	
 		}
 		
 	
 		@Override
 		protected void onCancelled() {
 	
 		}
 	}
 }
