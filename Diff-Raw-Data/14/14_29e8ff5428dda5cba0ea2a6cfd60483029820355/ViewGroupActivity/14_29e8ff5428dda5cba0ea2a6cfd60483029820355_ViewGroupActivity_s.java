 package uw.cse403.minion;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Debug;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.support.v4.app.NavUtils;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemClickListener;
 
 /**
  * ViewGroupActivity is an activity that lets the user view information about a group
  * they either own or are a part of. Currently stubbed out because it is a part of 
  * our test-driven development requirement.
  * @author Elijah Elefson (elefse)
  */
 public class ViewGroupActivity extends Activity {
 
 	/** Class constants for string representations **/
 	private static final String GROUPNAME = "groupname";
 	private static final String GAME_MASTER = "gm";
 	private static final String CHARACTER_NAME = "characterName";
 	private static final String PLAYER_NAME = "playerName";
 	private static final String PLAYERS = "players";
 	private static final String PHP_ADDRESS = "http://homes.cs.washington.edu/~elefse/getGroupInfo.php";
 	private static final String PHP_ADDRESS2 = "http://homes.cs.washington.edu/~elefse/acceptInvite.php";
 	private static final String PHP_ADDRESS3 = "http://homes.cs.washington.edu/~elefse/declineInvite.php";
 
 	/** The current user's username **/
 	private String username;
 
 	/** The currently viewed group's name **/
 	private String groupName;
 
 	/** The game master of the currently viewed group **/
 	private String gm;
 
 	/** Collection of all the players the group has **/
 	private static ArrayList<String> playersList;
 
 	/** Name of the character a player picks after accepting an invite **/
 	private String character;
 
 	/** Dialog elements to help manage the displaying of dialogs to the user **/
 	private static Dialog dialog;
 	private static Dialog noCharactersAlert;
 	
 	/** The local database source **/
 	private CharacterDataSource datasource;
 
 	/**
 	 * Declare the UI components
 	 */
 	private ListView playersListView;
 
 	/** Collection of all the players in the group and their associated characters **/
 	private static ArrayList<HashMap<String, String>> playerAndCharacterList;
 
 	/** Collection of all the characters the current user owns **/
 	private static ArrayList<String> characterList;
 
 	/**
 	 * Adapter for connecting the array above to the UI view
 	 */
 	private SimpleAdapter adapter;
 
 	/**
 	 * Displays the view group page for the selected group.
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		if (TraceControl.TRACE)
 			Debug.startMethodTracing("ViewGroupActivity_onCreate");
 		
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_view_group);
 		// Show the Up button in the action bar.
 		setupActionBar();
 		username = SaveSharedPreference.getPersistentUserName(ViewGroupActivity.this);
 		groupName = this.getIntent().getExtras().getString(GROUPNAME);
 		gm = this.getIntent().getExtras().getString(GAME_MASTER);
 		TextView groupTitle = (TextView) findViewById(R.id.group_name);
 		groupTitle.setText(groupName);
 		TextView gameMasterText = (TextView) findViewById(R.id.game_master_name);
 		gameMasterText.setText(gm);
 		GetGroupInfoTask task = new GetGroupInfoTask(this);
 		task.execute(username);
 		
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
 		getMenuInflater().inflate(R.menu.groups, menu);
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
 	 * Responds to the edit group button click by directing the user to the 
 	 * EditGroupActivity. Only works for the owner of the group. Disabled
 	 * otherwise.
 	 * @param view The current view
 	 */
 	public void editGroup(View view) {
 		Intent intent = new Intent(this, EditGroupActivity.class);
 		intent.putExtra(GROUPNAME, groupName);
 		intent.putExtra(GAME_MASTER, gm);
 		intent.putStringArrayListExtra(PLAYERS, playersList);
 		startActivity(intent);
 	}
 
 	/**
 	 * Responds to the accept invite button click by adding the user to the
 	 * group. Only works if the user has yet to respond to a pending invite
 	 * from the group. Disabled otherwise.
 	 * @param view The current view
 	 */
 	public void acceptInvite(View view) {
 		datasource = new CharacterDataSource(this);
 		datasource.open();
 
 		characterList = new ArrayList<String>();
 
 		// Gets all characters
 		Cursor cursor = SQLiteHelperBasicInfo.db.query(SQLiteHelperBasicInfo.TABLE_NAME, new String[]{SQLiteHelperBasicInfo.COLUMN_NAME}, 
 				null, null, null, null, null);
 		if (cursor.moveToFirst()) {
 			while (!cursor.isAfterLast()) { 
 				// Columns: COLUMN_CHAR_ID, COLUMN_NAME
 				String characterName = cursor.getString(0);
 				characterList.add(characterName);
 				cursor.moveToNext();
 			}
 		}
 		cursor.close();
 
 		if(characterList.size() == 0) {
 			AlertDialog.Builder noCharactersBuilder = new AlertDialog.Builder(this);
 			noCharactersBuilder.setMessage("You have no character with which to play with! Please go create a character!");
 			noCharactersBuilder.setTitle("No Characters Warning");
 			noCharactersBuilder.setPositiveButton("Ok",
 					new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					noCharactersAlert.dismiss(); 
 				}
 			});
 			noCharactersBuilder.setCancelable(true);
 			noCharactersAlert = noCharactersBuilder.create();
 			noCharactersAlert.show();
 		} else {
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setTitle("Pick a Character");
 
 			ListView modeList = new ListView(this);
 			ArrayAdapter<String> modeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, characterList);
 			modeList.setAdapter(modeAdapter);
 			builder.setView(modeList);
 			dialog = builder.create();
 
 			dialog.show();
 			modeList.setOnItemClickListener(new OnItemClickListener() {
 
 				@Override
 				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 					character = ((TextView) view).getText().toString();
 					dialog.dismiss();
 					AcceptInviteTask task = new AcceptInviteTask(view.getContext());
 					task.execute(username);
 				}
 			});
 		}
 	}
 
 	/**
 	 * Responds to the decline invite button click by removing any pending invites
 	 * the user has from the current group. Only works if the user has yet to 
 	 * respond to a pending invite from the group. Disabled otherwise.
 	 * @param view The current view
 	 */
 	public void declineInvite(View view) {
 		DeclineInviteTask task = new DeclineInviteTask(this);
 		task.execute(username);
 	}
 
 	/**
 	 * GetGroupInfoTask is a private inner class that allows requests to be made to the remote
 	 * MySQL database parallel to the main UI thread. Gets a list of the players currently in
 	 * the group and what characters are associated with each of them.
 	 */
 	private class GetGroupInfoTask extends AsyncTask<String, Void, ArrayList<HashMap<String, String>>> {
 		private Context context;
 		private boolean pendingInvite;
 		private boolean isGM;
 		private ArrayList<String> playerListPlaceholder;
 
 		/**
 		 * Constructs a new GetGroupInfoTask object.
 		 * @param context The current Activity's context
 		 */
 		private GetGroupInfoTask(Context context) {
 			this.context = context;
 		}
 
 		/**
 		 * Makes the HTTP request and returns the result as a String.
 		 */
 		@Override
 		protected ArrayList<HashMap<String, String>> doInBackground(String... args) {
 			//the data to send
 			ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
 			postParameters.add(new BasicNameValuePair("un", username));
 			postParameters.add(new BasicNameValuePair("groupName", groupName));
 
 			// Hashmap for ListView
 			ArrayList<HashMap<String, String>> playersArray = new ArrayList<HashMap<String, String>>();
 			playerListPlaceholder = new ArrayList<String>();
 
 			String result = null;
 
 			//http post
 			String res;
 			try{
 				result = CustomHttpClient.executeHttpPost(PHP_ADDRESS, postParameters);
 				res = result.toString();
 				JSONObject results  = new JSONObject(res);
 				pendingInvite = results.getBoolean("pendingInvite");
 				isGM = results.getBoolean("isGM");
 				JSONArray players = results.getJSONArray("items");
 				// looping through players
 				for(int i = 0; i < players.length(); i++){
 					JSONObject c = players.getJSONObject(i);
 
 					// Storing each json item in variable
 					String characterName = c.getString("character");
 					String playerName = c.getString("username");
 					playerListPlaceholder.add(playerName);
 					// creating new HashMap
 					HashMap<String, String> map = new HashMap<String, String>();
 					map.put(CHARACTER_NAME, characterName);
 					map.put(PLAYER_NAME, playerName);
 					// adding HashList to ArrayList
 					playersArray.add(map);
 				}
 			} catch (Exception e) {
 				res = e.toString();
 			}
 			return playersArray;
 		}
 
 		/**
 		 * Parses the String result and directs to the correct Activity
 		 */
 		@Override
 		protected void onPostExecute(ArrayList<HashMap<String, String>> result) {
 			if(isGM) {
 				Button editButton = (Button) findViewById(R.id.edit_button);
 				editButton.setVisibility(View.VISIBLE);
 			}
 
 			if(pendingInvite) {
 				LinearLayout inviteButtons = (LinearLayout) findViewById(R.id.invite_buttons);
 				inviteButtons.setVisibility(View.VISIBLE);
 			}
 			playersList = playerListPlaceholder;
 
 			playerAndCharacterList = result;
 
 			// Initialize the UI components
 			playersListView = (ListView) findViewById(R.id.playersListView);
 
 			// Create an empty adapter we will use to display the loaded data.
 			// We pass null for the cursor, then update it in onLoadFinished()
 			adapter = new SimpleAdapter(context, playerAndCharacterList,
 					R.layout.custom_group_list_item, new String[] { CHARACTER_NAME, PLAYER_NAME }, new int[] {
 					R.id.text1, R.id.text2 });
 			playersListView.setAdapter(adapter);
 
 		}
 	}
 
 	/**
 	 * AcceptInviteTask is a private inner class that allows requests to be made to the remote
 	 * MySQL database parallel to the main UI thread. Adds the user to the group and updates
 	 * the database appropriately.
 	 */
 	private class AcceptInviteTask extends AsyncTask<String, Void, String> {
 		private Context context;
 
 		/**
 		 * Constructs a new AcceptInviteTask object.
 		 * @param context The current Activity's context
 		 */
 		private AcceptInviteTask(Context context) {
 			this.context = context;
 		}
 
 		/**
 		 * Makes the HTTP request and returns the result as a String.
 		 */
 		@Override
 		protected String doInBackground(String... args) {
 			//the data to send
 			ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
 			postParameters.add(new BasicNameValuePair("un", username));
 			postParameters.add(new BasicNameValuePair("group", groupName));
 			postParameters.add(new BasicNameValuePair("character", character));
 
 			String result = null;
 
 			//http post
 			String res;
 			try{
 				result = CustomHttpClient.executeHttpPost(PHP_ADDRESS2, postParameters);
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
 		@Override
 		protected void onPostExecute(String result) {
 			Intent intent = new Intent(context, ViewGroupActivity.class);
 			intent.putExtra(GROUPNAME, groupName);
 			intent.putExtra(GAME_MASTER, "test");
 			startActivity(intent);
 			finish();
 		}
 
 	}
 
 	/**
 	 * DeclineInviteTask is a private inner class that allows requests to be made to the remote
 	 * MySQL database parallel to the main UI thread. Removes the group's pending invite to
 	 * the user and updates the database accordingly.
 	 */
 	private class DeclineInviteTask extends AsyncTask<String, Void, String> {
 		private Context context;
 
 		/**
 		 * Constructs a new DeclineInviteTask object.
 		 * @param context The current Activity's context
 		 */
 		private DeclineInviteTask(Context context) {
 			this.context = context;
 		}
 
 		/**
 		 * Makes the HTTP request and returns the result as a String.
 		 */
 		@Override
 		protected String doInBackground(String... args) {
 			//the data to send
 			ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
 			postParameters.add(new BasicNameValuePair("un", username));
 			postParameters.add(new BasicNameValuePair("group", groupName));
 
 			String result = null;
 
 			//http post
 			String res;
 			try{
 				result = CustomHttpClient.executeHttpPost(PHP_ADDRESS3, postParameters);
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
 		@Override
 		protected void onPostExecute(String result) {
 			Intent intent = new Intent(context, ViewInvitesActivity.class);
 			startActivity(intent);
 			finish();
 		}
 	}
 }
