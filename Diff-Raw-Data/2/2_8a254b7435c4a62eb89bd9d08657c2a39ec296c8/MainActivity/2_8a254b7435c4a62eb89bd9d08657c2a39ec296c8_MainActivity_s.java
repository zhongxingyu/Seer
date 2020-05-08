 package com.hackrgt.katanalocate;
 
 import java.io.File;
 
 import com.facebook.FacebookActivity;
 import com.facebook.Request;
 import com.facebook.Response;
 import com.facebook.Session;
 import com.facebook.SessionState;
 import com.facebook.model.GraphUser;
 import com.hackrgt.katanalocate.R;
 
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 
 public class MainActivity extends FacebookActivity {
     private MainFragment mainFragment;
     private boolean isResumed = false;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		//open a facebook session
 	
 		this.openSession();
 	    if (savedInstanceState == null) {
 	        // Add the fragment
 	        mainFragment = new MainFragment();
 	        getSupportFragmentManager()
 	        .beginTransaction()
 	        .add(android.R.id.content, mainFragment)
 	        .commit();
 	    } else {
 	        // Restore the fragment
 	        mainFragment = (MainFragment) getSupportFragmentManager()
 	        .findFragmentById(android.R.id.content);
 	    }
 	    
 	    /*
 	     * Testing Inbox View
 	     */
 	    Log.d("Main Activity", "Tried to create DBHelper");
 	    //DataBaseHelper dbhelper = new DataBaseHelper(this);
 	    //dbhelper.addUser("Chandim", "Chandim", "Success");
 	    //dbhelper.addUser("Diya", "Diya", "Dummy");
 	    MessageTable message = new MessageTable(1, "10:30", 36, 54, "Troll", "Troll", "Diya", 2);
 	    MessageTable message2 = new MessageTable(2, "9:00", 36, 54, "Troll2", "Troll2", "Diya", 2);
 	    UserTable Receiver = new UserTable("Chandim", "Chandim", "Success");
 	    UserTable Sender = new UserTable("Diya", "Diya", "Dummy");
 	    //dbhelper.addMessage(message, Sender, Receiver);
 	    //dbhelper.addMessage(message2, Receiver, Sender);
 	    //dbhelper.checkMessage();
 	    //dbhelper.checkUser();
 	    //dbhelper.checkSendReceive();
     }
     
     @Override
     protected void onSessionStateChange(SessionState state, Exception exception) {
     	super.onSessionStateChange(state, exception);
         if (isResumed) {
             mainFragment.onSessionStateChange(state, exception);
             if (state.isClosed()) {
             	//Default to SSO login and show dialog if Facebook App isnt installed
             	this.openSession();
             }
         }
         
         if (state.isOpened()) {
         	//Get database and check if it has been created
         	File database = getApplicationContext().getDatabasePath("DatabaseLocation");
         	if (!database.exists()) {
         		//Get users Facebook Id
         		Log.d("Chandim - Main Activity", "Database doesn't exist");
         		final Session session = Session.getActiveSession();
         		if (session != null && session.isOpened()) {
         			Request request = Request.newMeRequest(
         					session,
         					new Request.GraphUserCallback() {
         						// callback after Graph API response with user object
         						public void onCompleted(GraphUser user, Response response) {
         							if (user != null) {
         								final String userId = user.getId();
         								final String userName = user.getName();
         								//Add the user to the sqlite database
         								Log.d("Chandim - Main Activity", "Database created");
         								DataBaseHelper helper = new DataBaseHelper(MainActivity.this);
         								helper.addUser(userId, userName, "");
         								helper.addUser("Chandim", "Chandim", "Success");
         							    helper.addUser("Diya", "Diya", "Dummy");
         							    helper.addUser("Nathan", "Nathan", "Hurley");
        							    MessageTable message = new MessageTable(1, "10:00", 36, 54, "Hi", "It's been a while, how are you?", "Diya", 2);
         							    helper.sendMessage(message, "Nathan", user.getId());
         							}
         						}
         					}
         					);
         			Request.executeBatchAsync(request); 
         		}
         	}
         }
     }
     
     @Override
     public void onResume() {
         super.onResume();
         isResumed = true;
     }
 
     @Override
     public void onPause() {
         super.onPause();
         isResumed = false;
     }
     
     @Override
     protected void onResumeFragments() {
         super.onResumeFragments();
 
         Session session = Session.getActiveSession();
         if (session != null &&
                 (session.isOpened() || session.isClosed()) ) {
             onSessionStateChange(session.getState(), null);
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
     
 }
