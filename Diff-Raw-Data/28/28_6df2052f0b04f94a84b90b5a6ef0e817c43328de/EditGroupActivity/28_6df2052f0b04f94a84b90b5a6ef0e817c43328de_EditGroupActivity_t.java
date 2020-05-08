 package uw.cse403.minion;
 
 import java.util.ArrayList;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Debug;
 import android.annotation.TargetApi;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.support.v4.app.NavUtils;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 /**
  * EditGroupActivity is an activity that provides the user with the ability to edit a
  * group that they are currently the game master of. This includes changing the name
  * of the group, inviting new players to the group or removing current players from 
  * the group.
  * @author Elijah Elefson (elefse)
  */
 public class EditGroupActivity extends ListActivity {
 
 	/** Class constants for string representations **/
 	private static final String GROUPNAME = "groupname";
 	private static final String GAME_MASTER = "gm";
 	private static final String PLAYERS = "players";
 	private static final String PHP_ADDRESS = "http://homes.cs.washington.edu/~elefse/updateGroupInfo.php";
 	private static final String PHP_ADDRESS2 = "http://homes.cs.washington.edu/~elefse/removePlayer.php";
 	private static final String PHP_ADDRESS3 = "http://homes.cs.washington.edu/~elefse/makeGM.php";
 
 	/** Collection of all the players the group has **/
 	private ArrayList<String> playersList;
 
 	/** The current user's username **/
 	private String username;
 
 	/** The currently viewed group's name **/
 	private String groupName;
 
 	/** The game master of the currently viewed group **/
 	private String gm;
 
 	/**
 	 * Displays the edit group page for the selected group.
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		if (TraceControl.TRACE)
 			Debug.startMethodTracing("EditGroupActivity_onCreate");
 		
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_edit_group);
 		// Show the Up button in the action bar.
 		setupActionBar();
 		Intent i = getIntent();
 		username = SaveSharedPreference.getPersistentUserName(EditGroupActivity.this);
 		groupName = i.getExtras().getString(GROUPNAME);
 		gm = i.getExtras().getString(GAME_MASTER);
 		playersList = i.getStringArrayListExtra(PLAYERS);
 
 		EditText groupTitle = (EditText) findViewById(R.id.group_name);
 		groupTitle.setText(groupName);
 		TextView gameMasterText = (TextView) findViewById(R.id.game_master_name);
 		gameMasterText.setText(gm);
 
 		setListAdapter(new CustomArrayAdapter());
 		
 		if (TraceControl.TRACE)
 			Debug.stopMethodTracing();
 	}
 
 	/**
 	 * ViewHolder inner class to help with performance of ListView and track
 	 * the id's of its elements.
 	 * @author Elijah Elefson (elefse)
 	 */
 	public static class ViewHolder {
 		Button removeButton = null;
 		Button makeGMButton = null;
 		TextView currentGM = null;
 
 		/**
 		 * Simple ViewHolder constructor.
 		 * @param row The ListView row whose information is being stored
 		 */
 		ViewHolder(View row) {
 			this.removeButton = (Button) row.findViewById(R.id.remove_button);
 			this.makeGMButton = (Button) row.findViewById(R.id.gm_button);
 			this.currentGM = (TextView) row.findViewById(R.id.current_gm);
 		}
 	}
 
 	/** Custom ArrayAdapter used to populate the ListView and allow the correct
 	 * information to be displayed. Makes sure the ListView element that represents
 	 * the GM says "Current GM" next to it.
 	 * @author Elijah Elefson (elefse)
 	 */
 	class CustomArrayAdapter extends ArrayAdapter<String> {
 
 		/**
 		 * Constructs an ArrayAdapter with the specified parameters.
 		 */
 		CustomArrayAdapter() {
 			super(EditGroupActivity.this, R.layout.custom_player_list_item, R.id.player, playersList);
 		}
 
 		/**
 		 * Gets the count of the current view element
 		 */
 		@Override
 		public int getViewTypeCount() {                 
 			return getCount();
 		}
 
 		/**
 		 * Gets the position of the current view element
 		 */
 		@Override
 		public int getItemViewType(int position) {
 			return position;
 		}
 
 		/**
 		 * Gets the view for the current row
 		 */
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 
 			View row = super.getView(position, convertView, parent);
 			ViewHolder holder = (ViewHolder) row.getTag();
 
 			if (holder == null) {
 				holder = new ViewHolder(row);
 				row.setTag(holder);
 			}
 
 			if (playersList.get(position).equals(gm)) {
 				holder.removeButton.setVisibility(View.GONE);
 				holder.makeGMButton.setVisibility(View.GONE);
 				holder.currentGM.setVisibility(View.VISIBLE);
 			}
 
 			return(row);
 		}
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
 	 * Responds to the finish editing button click by saving any changes made
 	 * and then returning to the ViewGroupActivity.
 	 * @param view The current view
 	 */
 	public void finishEditing(View view) {
 		EditText groupNameEditText = (EditText) findViewById(R.id.group_name);
 		String newGroupName = groupNameEditText.getText().toString().trim();
 		UpdateGroupInfoTask task = new UpdateGroupInfoTask(this, newGroupName);
 		task.execute(username);
 	}
 
 	/**
 	 * Responds to the make GM button click updating which player is the current GM.
 	 * @param view The current view
 	 */
 	public void makePlayerGM(View view) {
 		int position = getListView().getPositionForView((View) view.getParent());
 		String player = (String) getListView().getItemAtPosition(position);
 		MakeGMTask task = new MakeGMTask(this, player);
 		task.execute(username);
 	}
 
 	/**
 	 * Responds to the invite new players button click by directing the user
 	 * to the invite players page.
 	 * @param view The current view
 	 */
 	public void inviteNewPlayers(View view) {
 		Intent intent = new Intent(this, GroupCreateActivity.class);
 		intent.putExtra("sentFromEditGroup", true);
 		EditText groupNameEditText = (EditText) findViewById(R.id.group_name);
 		groupName = groupNameEditText.getText().toString().trim();
 		intent.putExtra(GROUPNAME, groupName);
 		TextView gameMasterName = (TextView) findViewById(R.id.game_master_name);
 		gm = gameMasterName.getText().toString();
 		intent.putExtra(GAME_MASTER, gm);
 		intent.putStringArrayListExtra(PLAYERS, playersList);
 		startActivity(intent);
 	}
 
 	/**
 	 * Responds to the remove player button click by removing the selected player
 	 * from the group.
 	 * @param view The current view
 	 */
 	public void removePlayerFromGroup(View view) {
 		int position = getListView().getPositionForView((View) view.getParent());
 		String player = (String) getListView().getItemAtPosition(position);
 		RemovePlayerTask task = new RemovePlayerTask(this, player);
 		task.execute(username);
 	}
 
 	/**
 	 * UpdateGroupInfoTask is a private inner class that allows requests to be made to the remote
 	 * MySQL database parallel to the main UI thread. Updates the group information on the remote
 	 * database to reflect any changes made by the user.
 	 */
 	private class UpdateGroupInfoTask extends AsyncTask<String, Void, String> {
 		private Context context;
 		private String newGroupName;
 
 		/**
 		 * Constructs a new UpdateGroupInfoTask object.
 		 * @param context The current Activity's context
 		 */
 		private UpdateGroupInfoTask(Context context, String newGroupName) {
 			this.context = context;
 			this.newGroupName = newGroupName;
 		}
 
 		/**
 		 * Makes the HTTP request and returns the result as a String.
 		 */
 		protected String doInBackground(String... args) {
 			//the data to send
 			ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
 			postParameters.add(new BasicNameValuePair("newGroupName", newGroupName));
 			postParameters.add(new BasicNameValuePair("groupName", groupName));
 
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
 			Intent intent = new Intent(context, ViewGroupActivity.class);
 			EditText groupNameEditText = (EditText) findViewById(R.id.group_name);
 			groupName = groupNameEditText.getText().toString().trim();
 			intent.putExtra(GROUPNAME, groupName);
 			TextView gameMasterName = (TextView) findViewById(R.id.game_master_name);
 			gm = gameMasterName.getText().toString();
 			intent.putExtra(GAME_MASTER, gm);
 			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(intent);
 			finish();
 		}
 	}
 
 	/**
 	 * RemovePlayerTask is a private inner class that allows requests to be made to the remote
 	 * MySQL database parallel to the main UI thread. Removes the players specified by the user
 	 * from the group. Updates the database accordingly.
 	 */
 	private class RemovePlayerTask extends AsyncTask<String, Void, String> {
 		private Context context;
 		private String player;
 
 		/**
 		 * Constructs a new RemovePlayerTask object.
 		 * @param context The current Activity's context
 		 */
 		private RemovePlayerTask(Context context, String player) {
 			this.context = context;
 			this.player = player;
 		}
 
 		/**
 		 * Makes the HTTP request and returns the result as a String.
 		 */
 		protected String doInBackground(String... args) {
 			//the data to send
 			ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
 			postParameters.add(new BasicNameValuePair("groupName", groupName));
 			postParameters.add(new BasicNameValuePair("player", player));
 
 			String result = null;
 
 			//http post
 			String res;
 			try{
 				result = CustomHttpClient.executeHttpPost(PHP_ADDRESS2, postParameters);
 				Set<String> players = new TreeSet<String>(playersList);
 				players.remove(player);
 				playersList = new ArrayList<String>(players);
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
 			Intent intent = new Intent(context, EditGroupActivity.class);
 			EditText groupNameEditText = (EditText) findViewById(R.id.group_name);
 			groupName = groupNameEditText.getText().toString().trim();
 			intent.putExtra(GROUPNAME, groupName);
 			TextView gameMasterName = (TextView) findViewById(R.id.game_master_name);
 			gm = gameMasterName.getText().toString();
 			intent.putExtra(GAME_MASTER, gm);
 			intent.putStringArrayListExtra(PLAYERS, playersList);
 			startActivity(intent);
 			finish();
 		}
 	}
 
 	/**
 	 * MakeGMTask is a private inner class that allows requests to be made to the remote
 	 * MySQL database parallel to the main UI thread. Removes GM status from the player
 	 * currently specified as GM and updates the view to represent the new GM.
 	 */
 	private class MakeGMTask extends AsyncTask<String, Void, String> {
 		private Context context;
 		private String player;
 
 		/**
 		 * Constructs a new MakeGMTask object.
 		 * @param context The current Activity's context
 		 */
 		private MakeGMTask(Context context, String player) {
 			this.context = context;
 			this.player = player;
 		}
 
 		/**
 		 * Makes the HTTP request and returns the result as a String.
 		 */
 		protected String doInBackground(String... args) {
 			//the data to send
 			ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
 			postParameters.add(new BasicNameValuePair("groupName", groupName));
 			postParameters.add(new BasicNameValuePair("player", player));
 
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
 		protected void onPostExecute(String result) {
 			Intent intent = new Intent(context, EditGroupActivity.class);
 			EditText groupNameEditText = (EditText) findViewById(R.id.group_name);
 			groupName = groupNameEditText.getText().toString().trim();
 			intent.putExtra(GROUPNAME, groupName);
 			intent.putExtra(GAME_MASTER, player);
 			intent.putStringArrayListExtra(PLAYERS, playersList);
 			startActivity(intent);
 			finish();
 		}
 
 	}
 }
