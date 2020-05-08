 package com.example.zootypers.ui;
 
 
 
 import android.annotation.SuppressLint;
 import android.app.ActionBar;
 import android.app.ActionBar.Tab;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.ViewGroup;
 
 import com.example.zootypers.R;
 import com.example.zootypers.core.MultiLeaderBoardModel;
 import com.example.zootypers.core.ScoreEntry;
 import com.example.zootypers.core.SingleLeaderBoardModel;
 import com.example.zootypers.util.InternetConnectionException;
 import com.parse.Parse;
 import com.parse.ParseUser;
 
 /**
  * Controls all of the action happening in the Leaderboard UI, switching tabs,
  * populating the leaderboard, etc
  * 
  * @author ZooTypers
  *
  */
 @SuppressLint("NewApi")
 public class Leaderboard extends FragmentActivity {
 
 	private LoginPopup lp;
 	private ParseUser currentUser;
 	
 	private final int NUM_RELATIVE = 5;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		// set the layout for the parent activity which contains the fragments
 		setContentView(R.layout.activity_leaderboard);
 
 		// Initialize the database
 		Parse.initialize(this, "Iy4JZxlewoSxswYgOEa6vhOSRgJkGIfDJ8wj8FtM",
 		"SVlq5dqYQ4FemgUfA7zdQvdIHOmKBkc5bXoI7y0C"); 
 
 		lp = new LoginPopup(currentUser);
 		
 		// set up the action bar for the different tabs
 		ActionBar actionBar = getActionBar();
 		actionBar.setDisplayShowTitleEnabled(false);
 		actionBar.setDisplayHomeAsUpEnabled(false);
 		actionBar.setDisplayUseLogoEnabled(false);
 		
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 		ActionBar.Tab singlePlayerTab = actionBar.newTab().setText("Singleplayer");
 		ActionBar.Tab multiPlayerTab = actionBar.newTab().setText("Multiplayer");
 		ActionBar.Tab friendsLBTab = actionBar.newTab().setText("Friends");
 		
 		// get the list of scores from the model and send it to each of the tabs
 		
 		SingleLeaderBoardModel lb = new SingleLeaderBoardModel(getApplicationContext());
 		MultiLeaderBoardModel mlb = null;
 		try {
 			mlb = new MultiLeaderBoardModel("bbbb");
 		} catch (InternetConnectionException e) {
 			Log.i("Leaderboard", "triggering internet connection error screen");
 			Intent intent = new Intent(this, ErrorScreen.class);
 			intent.putExtra("error", R.layout.activity_connection_error);
 			startActivity(intent);
 			return;
 		}
 		//need to get the username to pass into the leaderboard
 		
 		
 		Fragment singlePlayerFragment = SingleplayerTab.newInstance(lb.getTopScores());
 		Fragment multiPlayerFragment = MultiplayerTab.newInstance("", mlb.getTopScores());
 		Fragment friendsLBFragment = FriendsLBTab.newInstance(mlb.getTopScores());
 		
 		singlePlayerTab.setTabListener(new LBTabListener(singlePlayerFragment));
 		multiPlayerTab.setTabListener(new LBTabListener(multiPlayerFragment));
 		friendsLBTab.setTabListener(new LBTabListener(friendsLBFragment));
 		
 		actionBar.addTab(singlePlayerTab);
 		actionBar.addTab(multiPlayerTab);
 		actionBar.addTab(friendsLBTab);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_leaderboard, menu);
 		return true;
 	}
 
	@Override
	public void onBackPressed() {
		Intent intent = new Intent(this, TitlePage.class);
		startActivity(intent);
	}
 	/**
 	 * Called when the user wants to view his/her score relative to other players
 	 * @param view the button clicked
 	 */
 	public void relativeUserScore(View view) {
 		// set up the Parse database and have the user log in if not already
 		Parse.initialize(this, "Iy4JZxlewoSxswYgOEa6vhOSRgJkGIfDJ8wj8FtM", 
 		        "SVlq5dqYQ4FemgUfA7zdQvdIHOmKBkc5bXoI7y0C"); 
 		currentUser = ParseUser.getCurrentUser();
 		if (currentUser == null) {
 			buildPopup(false);
 		} else {
 			// make a new MultiLeaderBoardModel with the given username
 			MultiLeaderBoardModel newMlb = null;
 			try {
 				newMlb = new MultiLeaderBoardModel(currentUser.getString("username"));
 			} catch (InternetConnectionException e) {
 				Log.i("Leaderboard", "triggering internet connection error screen");
 				Intent intent = new Intent(this, ErrorScreen.class);
 				intent.putExtra("error", R.layout.activity_connection_error);
 				startActivity(intent);
 				return;
 			}
 			int userRank = newMlb.getRank();			
 			// get the relative position of the user with the passed in NUM_RELATIVE
 			ScoreEntry[] relativeEntrys = newMlb.getRelativeScores(NUM_RELATIVE);
 			// inform the user that he/she has no scores yet
 			if (relativeEntrys.length == 0) {
 				final String title = "No scores yet";
 				final String message = "You do not have any scores yet. Play " +
 						"games to figure out where you rank!!";
 				buildAlertDialog(title, message);
 				return;
 			}
 			
 			// add the relativeScore tab
 			Fragment currentFragment = RelativeUserScoreTab.newInstance(relativeEntrys, userRank,
 					NUM_RELATIVE);
 			FragmentTransaction fst = getSupportFragmentManager().beginTransaction();
 			fst.replace(R.id.leaderboard_layout, currentFragment);
 		    fst.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
 		    fst.commit();
 		}
 	}
 
 	/**
 	 * Handles what happens when user clicks the login button
 	 * @param view Button that is pressed
 	 */
 	public void loginButton(final View view) {
 		// Try to login
 	    String usernameString;
 		try {
 			usernameString = lp.loginButton();
 		} catch (InternetConnectionException e) {
 			Log.i("Leaderboard", "triggering internet connection error screen");
 			Intent intent = new Intent(this, ErrorScreen.class);
 			intent.putExtra("error", R.layout.activity_connection_error);
 			startActivity(intent);
 			return;
 		}
 	    // If login was successful, go to the multiplayer game
 	    if (!usernameString.equals("")) {
 	    	exitLoginPopup(view);
 	    	relativeUserScore(view);
 	    }
 	}
 	
 	/**
 	 * Builds a popup window for the login popup
 	 * @param dismisspsw If the password popup is currently being displayed
 	 * (and therefore should be dismissed)
 	 */
 	private void buildPopup(boolean dismisspsw) {
 	    // set up the layout inflater to inflate the popup layout
 	    LayoutInflater layoutInflater =
 	    (LayoutInflater) getBaseContext()
 	    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
 	    // the parent layout to put the layout in
 	    ViewGroup parentLayout = (ViewGroup) findViewById(R.id.multiplayer_tab_layout);
 
 	    // inflate either the login layout
 	    lp.buildLoginPopup(layoutInflater, parentLayout, dismisspsw);
 	}
 	
 	/**
 	 * Exits the login popup window
 	 * @param view the button clicked
 	 */
 	public void exitLoginPopup(View view) {
 		lp.exitLoginPopup();
 	}
 
 	/**
 	 * Exits the password popup window
 	 * @param view the button clicked
 	 */
 	public void exitPasswordPopup(View view) {
 		buildPopup(true);
 	}
 	
 	/**
 	 * Handles what happens when user clicks the "Forgot your password" link
 	 * @param view Button that is pressed
 	 */
 	public final void forgotPassword(View view) {
 		// set up the layout inflater to inflate the popup layout
 	    LayoutInflater layoutInflater =
 	    (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
 
 	    // the parent layout to put the layout in
 	    ViewGroup parentLayout = (ViewGroup) findViewById(R.id.multiplayer_tab_layout);
 
 	    // inflate the password layout
 	    lp.buildResetPopup(layoutInflater, parentLayout);
 	}
 	
 	/**
 	 * Handles what happens when user wants to reset password.
 	 * @param view the button clicked
 	 */
 	public void resetPassword(View view) {
 		// Sort through the reset info
 		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
 		lp.resetPassword(alertDialogBuilder);   
 		// Go back to the login popup
 		buildPopup(true);
 	}
 	
 	/**
 	 * Goes to the Registration page
 	 * @param view the button clicked
 	 */
 	public void goToRegister(View view) {
 		Intent registerIntent = new Intent(this, RegisterPage.class);
 	    startActivity(registerIntent);
 	}
 	
 	/**
 	 * builds an AlertDialog popup with the given title and message
 	 * @param title String representing title of the AlertDialog popup
 	 * @param message String representing the message of the AlertDialog
 	 * popup
 	 */
 	private void buildAlertDialog(String title, String message) {
 		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
 
 		// set title
 		alertDialogBuilder.setTitle(title);
 
 		// set dialog message
 		alertDialogBuilder
 		.setMessage(message)
 		.setCancelable(false)
 		.setPositiveButton("Close", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				// if this button is clicked, close the dialog box
 				dialog.cancel();
 			}
 		});
 
 		// create alert dialog
 		AlertDialog alertDialog = alertDialogBuilder.create();
 
 		// show the message
 		alertDialog.show();
 	}
	
	public void goToMain(View view) {
		Intent intent = new Intent(this, TitlePage.class);
		startActivity(intent);
	}
 	/**
 	 * Class to handle the actions for each of the tabs in the action bar
 	 * @author ZooTypers
 	 *
 	 */
 	private class LBTabListener implements ActionBar.TabListener {
 		
 		private Fragment currentFragment;
 
 		/**
 		 * set the current fragment the class is listening on
 		 * @param fragment
 		 */
 		public LBTabListener(Fragment fragment) {
 			currentFragment = fragment;
 		}
 		
 		@Override
 		public void onTabReselected(Tab tab, android.app.FragmentTransaction ft) {
 			// Do nothing
 		}
 
 		@Override
 		public void onTabSelected(Tab tab, android.app.FragmentTransaction ft) {
 			// begin a fragment transaction and replace the current transaction
 		
 			FragmentTransaction fst = getSupportFragmentManager().beginTransaction();
 			fst.replace(R.id.leaderboard_layout, currentFragment);
 		    fst.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
 		    fst.commit();
 		}
 
 		@Override
 		public void onTabUnselected(Tab tab, android.app.FragmentTransaction ft) {
 			// Do nothing
 		}
 	}
 }
