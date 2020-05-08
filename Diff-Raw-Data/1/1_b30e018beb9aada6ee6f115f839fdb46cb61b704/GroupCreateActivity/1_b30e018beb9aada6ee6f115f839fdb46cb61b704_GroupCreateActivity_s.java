 package uw.cse403.minion;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.support.v4.app.NavUtils;
 import android.annotation.TargetApi;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Build;
 
 public class GroupCreateActivity extends Activity {
 	private static final String PHP_ADDRESS = "http://homes.cs.washington.edu/~elefse/sendInvites.php";
 	private String username;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_group_create);
 		username = SaveSharedPreference.getPersistentUserName(GroupCreateActivity.this);
 
 		// Show the Up button in the action bar.
 		setupActionBar();
 	}
 
 	/**
 	 * Set up the {@link android.app.ActionBar}, if the API is available.
 	 */
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void setupActionBar() {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.group_create, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	public void sendInvites(View view) {
 	    if (ConnectionChecker.hasConnection(this)) {
 			// Get user invites.
 	    	EditText groupNameEditText = (EditText) findViewById(R.id.group_name_input);
 			EditText user1EditText = (EditText) findViewById(R.id.user_1_input);
 			EditText user2EditText = (EditText) findViewById(R.id.user_2_input);
 			EditText user3EditText = (EditText) findViewById(R.id.user_3_input);
 			EditText user4EditText = (EditText) findViewById(R.id.user_4_input);
 			EditText user5EditText = (EditText) findViewById(R.id.user_5_input);
 			
 			String groupName = groupNameEditText.getText().toString().trim();
 			String user1 = user1EditText.getText().toString().trim();
 			String user2 = user2EditText.getText().toString().trim();
 			String user3 = user3EditText.getText().toString().trim();
 			String user4 = user4EditText.getText().toString().trim();
 			String user5 = user5EditText.getText().toString().trim();
 			
 			
 			HashSet<String> users = new HashSet<String>();
 			//Create a set of all the users entered
 			users.addAll(Arrays.asList(user1, user2, user3, user4, user5));
 			TextView warning = (TextView) findViewById(R.id.warning);
 			//checks if all the users entered are unique
 			if(users.size() != 5){
 				warning.setVisibility(0);
 			//checks that user isn't adding self to group
 			}else if(users.contains(username)){
 				warning.setText("Cannont add yourself to a group");
 				warning.setVisibility(0);
 			//checks that all fields had been set
 			}else if(users.contains("")){
 				warning.setText("Must input usernames for all fields");
 				warning.setVisibility(0);
 			}else{
 				SendInvitesTask task = new SendInvitesTask(groupName, user1, user2, user3, user4, user5, this);
 				task.execute(groupName);
 			}
     	} else {
     		Toast.makeText(getApplicationContext(), "No network available", Toast.LENGTH_LONG).show();
     	}
 	}
 	
 	
 	/**
 	 * SendInvitesTask is a private inner class that allows requests to be made to the remote
 	 * MySQL database parallel to the main UI thread. It updates the database to include the
 	 * specified players as part of a group and puts the current as the game master.
 	 */
 	private class SendInvitesTask extends AsyncTask<String, Void, String> {
 		private String groupName;
 		private String user1;
 		private String user2;
 		private String user3;
 		private String user4;
 		private String user5;
 		private Context context;
 		
 		/**
 		 * Constructs a new SendInvitesTask object.
 		 * @param groupName The given group name.
 		 * @param user1 First player to be included in the group.
 		 * @param user2 Second player to be included in the group.
 		 * @param user3 Third player to be included in the group.
 		 * @param user4 Fourth player to be included in the group.
 		 * @param user5 Fifth player to be included in the group.
 		 * @param context The current Activity's context.
 		 */
 		private SendInvitesTask (String groupName, String user1, String user2, String user3, String user4, String user5, Context context) {
 			this.groupName = groupName;
 			this.user1 = user1;
 			this.user2 = user2;
 			this.user3 = user3;
 			this.user4 = user4;
 			this.user5 = user5;
 			this.context = context;
 		}
 		
 	    /**
 	     * Makes the HTTP request and returns the result as a String.
 	     */
 	    protected String doInBackground(String... args) {
 	        //the data to send
 	        ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
 	        postParameters.add(new BasicNameValuePair("group", groupName));
 	        postParameters.add(new BasicNameValuePair("user1", user1));
 	        postParameters.add(new BasicNameValuePair("user2", user2));
 	        postParameters.add(new BasicNameValuePair("user3", user3));
 	        postParameters.add(new BasicNameValuePair("user4", user4));
 	        postParameters.add(new BasicNameValuePair("user5", user5));
 	        postParameters.add(new BasicNameValuePair("gm", username));
 	        
 
 			String result = null;
 	        
 	        //http post
 			String res;
 	        try{
 	        	result = CustomHttpClient.executeHttpPost(PHP_ADDRESS, postParameters);
 	        	res = result.toString();   
 	        	res = res.replaceAll("\\s+", "");    
 	        } catch (Exception e) {  
 	        	res = e.toString();
 	        }
 	        return res;
 	    }
 	 
 	    /**
 	     * Parses the String result and directs to the correct Activity
 	     */
 	    protected void onPostExecute(String result) {
 			Intent intent = new Intent(context, GroupsActivity.class);
 			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(intent);
         	finish();
 	    }
 	 
 	}
 }
