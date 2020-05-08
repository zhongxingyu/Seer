 package com.example.mobileindia;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.parse.LogInCallback;
 import com.parse.Parse;
 import com.parse.ParseAnonymousUtils;
 import com.parse.ParseException;
 import com.parse.ParseQuery;
 import com.parse.ParseUser;
 
 public class MainActivity extends Activity {
     public static boolean anonSuccess;
     public static boolean foundUserPostsSuccess;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	    Parse.initialize(this, "mxi3f2SxGpnfFCxW4FKz5D8f9i97BSVnLQ0PR6L9", "2VRMIqpKdwzfimui1EpUfwC6wUvlWHv2O84q87NC");
 		setContentView(R.layout.activity_main);
 	}
 	protected void onResume (){
 		super.onResume();
 		rebuildMainMenuAndLabel();	
 	}
 	protected void onPostCreate (Bundle savedInstanceState){
 		super.onPostCreate(savedInstanceState);
 		rebuildMainMenuAndLabel();
 	}
 	private void rebuildMainMenuAndLabel() {
 		TextView t = (TextView) findViewById(R.id.mainActivityUserLabel);
 		if (ParseUser.getCurrentUser() == null) {
 			t.setText("Not logged in");
 		}else if (ParseAnonymousUtils.isLinked(ParseUser.getCurrentUser())){ 
 	    	t.setText("Logged in anonymously");
 	    	findViewById(R.id.btnSignUp).setVisibility(8);
 	    	TextView btnLogin = (TextView) findViewById(R.id.btnLogin);
 	    	findViewById(R.id.btnSearch).setVisibility(0);
 	    	ListViewCategory.hideAdd = true;
 	    	btnLogin.setText("Logout");
 	    }else {
 	    	t.setText("Logged in as: " + ParseUser.getCurrentUser().getUsername());
 	    	findViewById(R.id.btnSearch).setVisibility(0);
 	    	findViewById(R.id.logged_in_user_posts_button).setVisibility(0);
 	    	findViewById(R.id.btnSignUp).setVisibility(8);
 	    	TextView btnLogin = (TextView) findViewById(R.id.btnLogin);
 	    	btnLogin.setText("Logout");
 		}
 	}
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 	 public void loginUserActivity(View view) {
 		 if(ParseUser.getCurrentUser() == null){
 			 Intent i = new Intent(this, LoginActivity.class);
 			 startActivity(i);
 		 }else{
 			 ParseUser.logOut();
 			 ListViewCategory.hideAdd = true;
 			 findViewById(R.id.btnSignUp).setVisibility(0);
 			 TextView btnLogin = (TextView) findViewById(R.id.btnLogin);
 			 TextView userLabel = (TextView) findViewById(R.id.mainActivityUserLabel);
 		     findViewById(R.id.logged_in_user_posts_button).setVisibility(8);
 		     findViewById(R.id.btnSearch).setVisibility(8);
 			 userLabel.setText("Not logged in");
 		     btnLogin.setText("Login");
 		 }
 	 }
 	 
 	 public void cityBrowse(View view){
 		 final Context context = getApplicationContext();
 		 final int duration = Toast.LENGTH_LONG;
 		 final Intent i = new Intent(this, CitySelect.class);
 		 findViewById(R.id.btnBrowse).setClickable(false);
 		 if(ParseUser.getCurrentUser() == null){
 			 ParseAnonymousUtils.logIn(new LogInCallback() {
 				 @Override
 				 public void done(ParseUser user, ParseException e) {
 
 					 if (e == null){
 						 Toast toast = Toast.makeText(context, "Logged in as anonymous user", duration);
 						 findViewById(R.id.btnBrowse).setClickable(true);
 						 startActivity(i);
 						 toast.show();
 					 }else{
 						 Toast toast = Toast.makeText(context, "Could not log in anonymously", duration);
 						 findViewById(R.id.btnBrowse).setClickable(true);
 						 startActivity(i);
 						 toast.show();
 					 }
 
 				 }
 			 });
 		 }else{
 			 findViewById(R.id.btnBrowse).setClickable(true);
 			 startActivity(i);
 		 }
 	 }
 
 	 public void createUserActivity(View view){
 		 Intent i = new Intent(this, CreateUserActivity.class);
 		 startActivity(i);
 	 }
 	 
 	 public void viewUserPosts(View view){
 		 ListViewCategory.parsePostList = null;
 		 ParseQuery query = new ParseQuery("Post");
 		 query.whereEqualTo("user", ParseUser.getCurrentUser());
 		 try {
 			ListViewCategory.parsePostList = query.find();
 			MainActivity.foundUserPostsSuccess = true;
 		} catch (ParseException e1) {
 			ListViewCategory.parsePostList = null;
 			MainActivity.foundUserPostsSuccess = false;
 		}
 		 if(MainActivity.foundUserPostsSuccess){
 			 Intent i = new Intent(this,ListViewCategory.class);
 			 ListViewCategory.hideAdd = true;
 			 startActivity(i);
 		 }
 	 }
 	 public void searchPosts(View view){
 		Intent i = new Intent(this,SearchPostActivity.class);
 		startActivity(i);
 	 }
 }
