 package uw.cse403.minion;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Debug;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.support.v4.app.NavUtils;
 import android.annotation.TargetApi;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Build;
 
 /**
  * GroupCreateActivity is an activity that provides the user with a form to create a new group
  * and invite players to it.
  * @author Elijah Elefson (elefse)
  */
 public class GroupCreateActivity extends Activity {
 
 	/** Class constants for string representations **/
 	private static final String GROUPNAME = "groupname";
 	private static final String GAME_MASTER = "gm";
 	private static final String PLAYERS = "players";
 	private static final String PHP_ADDRESS = "http://homes.cs.washington.edu/~elefse/sendInvites.php";
 
 	/** The current user's username **/
 	private String username;
 
 	/** The number of players in the group **/
 	private int numberOfPlayers;
 
 	/** Determines which Activity this current Activity was accessed from **/
 	private boolean cameFromEditGroup;
 
 	/** The name of the group that is being created **/
 	private String groupName;
 
 	/** The name of the GM who is creating the group **/
 	private String gm;
 
 	/** Collection of the players currently in the group **/
 	private ArrayList<String> playersList;
 
 	/** Layout components that allow additional player fields to be added dynamically **/
 	private LinearLayout.LayoutParams layoutParams;
 	private LinearLayout ll;
 	private List<EditText> editTexts;
 
 	/**
 	 * Displays the new group creation page.
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		if (TraceControl.TRACE)
 			Debug.startMethodTracing("GroupCreateActivity_onCreate");
 		
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_group_create);
 		username = SaveSharedPreference.getPersistentUserName(GroupCreateActivity.this);
 		Button b = (Button) findViewById(R.id.add_user_button);
 		ll = (LinearLayout) findViewById(R.id.users);
 		layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
 
 		editTexts = new ArrayList<EditText>();
 
 		b.setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(View v) {
 				numberOfPlayers++;
 				LinearLayout l1 = new LinearLayout(GroupCreateActivity.this);
 
				TextView user = new TextView(GroupCreateActivity.this);             
				user.setText("Enter User " + numberOfPlayers);
 				user.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f));
 				user.setPadding(0, 8, 0, 0);
 				EditText userEditText = new EditText(GroupCreateActivity.this);
 				userEditText.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 2f));
 				userEditText.setHint("User Name");
 				editTexts.add(userEditText);
 
 				l1.setOrientation(LinearLayout.HORIZONTAL);
 				l1.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
 				l1.addView(user); 
 				l1.addView(userEditText);
 
 				ll.addView(l1, layoutParams);
 
 			}});
 
 		Intent i = getIntent();
 		cameFromEditGroup = i.getBooleanExtra("sentFromEditGroup", false);;
 		if(cameFromEditGroup) {
 			LinearLayout l2 = (LinearLayout) findViewById(R.id.group_name);
 			l2.setVisibility(View.GONE);
 			groupName = i.getExtras().getString(GROUPNAME);
 			gm = i.getExtras().getString(GAME_MASTER);
 			playersList = i.getStringArrayListExtra(PLAYERS);
 			TextView inviteNewPlayers = (TextView) findViewById(R.id.invite_new_players);
 			inviteNewPlayers.setText("Send New Invites For " + groupName);
 			inviteNewPlayers.setVisibility(View.VISIBLE);
 		}
 		// Show the Up button in the action bar.
 		setupActionBar();
 		
 		if (TraceControl.TRACE)
 			Debug.stopMethodTracing();
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
 
 	/**
 	 * Creates Options Menu
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.group_create, menu);
 		return true;
 	}
 
 	/**
 	 * Sets up the Up button
 	 */
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
 
 	/**
 	 * Responds to the send invites button click by first checking that the players to
 	 * be invited are valid. If the are valid the invites are sent and the group is created.
 	 * If they are not the user is prompted with an error message and must try again.
 	 * @param view The current view
 	 */
 	public void sendInvites(View view) {
 		if (ConnectionChecker.hasConnection(this)) {
 			// Get user invites.
 			EditText groupNameEditText = (EditText) findViewById(R.id.group_name_input);
 
 			HashSet<String> users = new HashSet<String>();
 			for(int i = 0; i < editTexts.size(); i++) {
 				EditText userEditText = (EditText) editTexts.get(i);
 				String user = userEditText.getText().toString().trim();
 				users.add(user);
 			}
 
 			if(!cameFromEditGroup) {
 				groupName = groupNameEditText.getText().toString().trim();
 			}
 
 			//Create a set of all the users entered
 			TextView warning = (TextView) findViewById(R.id.warning);
 
 			// checks that the user input some nonempty group name
 			if(groupName.equals("")) {
 				warning.setText("Enter a valid group name");
 				warning.setVisibility(0);
 				//checks that user isn't adding self to group
 			} else if(users.contains(username) && !cameFromEditGroup) {
 				warning.setText("Cannot add yourself to a group");
 				warning.setVisibility(0);
 				//checks that all fields had been set
 			} else if(users.contains("")) {
 				warning.setText("Must input usernames for all fields");
 				warning.setVisibility(0);
 			} else {
 				SendInvitesTask task = new SendInvitesTask(groupName, users, this);
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
 		private HashSet<String> users;
 		private Context context;
 
 		/**
 		 * Constructs a new SendInvitesTask object.
 		 * @param groupName The given group name.
 		 * @param users Set of all users to be invited.
 		 * @param context The current Activity's context.
 		 */
 		private SendInvitesTask (String groupName, HashSet<String> users, Context context) {
 			this.groupName = groupName;
 			this.users = users;
 			this.context = context;
 		}
 
 		/**
 		 * Makes the HTTP request and returns the result as a String.
 		 */
 		protected String doInBackground(String... args) {
 			JSONObject playersObject = new JSONObject();
 			JSONArray players = new JSONArray();
 			try {
 				for(String s : users) {
 					JSONObject player = new JSONObject();
 					player.put("player", s);
 					players.put(player);
 				}
 				playersObject.put("players", players);
 				playersObject.put("fromEdit", cameFromEditGroup);
 			} catch (JSONException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 
 			//the data to send
 			ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
 			postParameters.add(new BasicNameValuePair("group", groupName));
 			postParameters.add(new BasicNameValuePair("players", playersObject.toString()));
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
 			Intent intent;
 			if(cameFromEditGroup) {
 				intent = new Intent(context, EditGroupActivity.class);
 				intent.putExtra(GROUPNAME, groupName);
 				intent.putExtra(GAME_MASTER, gm);
 				intent.putStringArrayListExtra(PLAYERS, playersList);
 			} else {
 				intent = new Intent(context, GroupsActivity.class);
 			}
 			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(intent);
 			finish();
 		}
 
 	}
 }
