 package org.touchirc.activity;
 
 import org.touchirc.R;
 import org.touchirc.R.layout;
 import org.touchirc.TouchIrc;
 import org.touchirc.adapter.ProfileAdapter;
 import org.touchirc.model.Profile;
 import org.touchirc.model.Server;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.util.SparseArray;
 import android.view.KeyEvent;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockListActivity;
 import com.actionbarsherlock.view.ActionMode;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
 
 public class ExistingProfilesActivity extends SherlockListActivity {
 
 	private TouchIrc touchIrc;
 	private ListView profiles_LV;
 	private SparseArray<Profile> profiles;
 	private SparseArray<Server> servers;
 	private ProfileAdapter adapterProfile;
 	private int idSelectedProfile;
 	private Profile selectedProfile;
 	private Context c;
 	private ActionBar actionBar;
 
 	protected ActionMode mActionMode; // Variable used for triggering the actionMode (ActionBar)
 	private static View oldView; // Variable used to store the old view selected
 	private static View currentView; // Variable used to store the current view selected
 	protected boolean subMenuLinkCreated = false; // Variable used to warn OnPrepare method first display ActionBar
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		
 		// The icon App can do "Previous"
 		this.actionBar = getSupportActionBar();
 		this.actionBar.setDisplayHomeAsUpEnabled(true);
 
 		setContentView(layout.listview_layout);
 
 		// Collect the context
 		c = getApplicationContext();
 
 		// Get the access to the infos
 		touchIrc = TouchIrc.getInstance();
 		
 		// Collect the Profiles list
 		this.profiles = touchIrc.getAvailableProfiles();
 		this.servers = touchIrc.getAvailableServers();
 		
 		// Collect the profile widgets ListView (LV)
 		this.profiles_LV = (ListView) findViewById(android.R.id.list);
 
 		// the LV is always focused on its last item
 		this.profiles_LV.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
 
 		this.actionBar.setTitle("Profiles  (" + this.profiles.size() + ")");
 
 		// Put an ProfileAdapter so that the LV and the profiles list be linked
 		this.adapterProfile = new ProfileAdapter(profiles, c);
 		this.setListAdapter(adapterProfile);
 
 		/**
 		 * 
 		 * When the user click on an item an ActionMode Bar appears.
 		 * It provides him some features on the selected profile : Set by Default, Edit, Link a Server, Delete.
 		 * 
 		 */
 
 		profiles_LV.setOnItemLongClickListener(new OnItemLongClickListener() {
 			@Override
 			public boolean onItemLongClick(AdapterView<?> arg0, View v,
 					int position, long arg3) {
 
 				idSelectedProfile = (int) adapterProfile.getItemId(position);
 				selectedProfile = adapterProfile.getItem(position);
 				currentView = v;
 
 				// if the ActionMode is already displayed
 				if (mActionMode != null) {
 
 					oldView.setBackgroundColor(profiles_LV.getCacheColorHint());
 					mActionMode.finish(); // closing it
 					mActionMode = startActionMode(new ActionModeProfileSettings()); // and launching it to update values
 					v.setBackgroundColor(Color.GRAY); // the selected item in the listView is highlighted
 					oldView = v;
 				}
 				else{
 
 					// Start the CallBackActionBar using the ActionMode.Callback defined below
 					mActionMode = startActionMode(new ActionModeProfileSettings()); // launching the ActionMode
 					oldView = v;
 					v.setSelected(true);
 					v.setBackgroundColor(Color.GRAY); // the selected item in the listView is highlighted
 				}
 				return true;
 			}
 		});
 	}
 	
 	/**
 	 * 
 	 * Does the same that the setOnItemLongClickListener() but it is triggered when
 	 * the user simple click
 	 * 
 	 */
 	
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id){
 		super.onListItemClick(l, v, position, id);
 		if(mActionMode == null){
 			idSelectedProfile = (int) adapterProfile.getItemId(position);
 			selectedProfile = adapterProfile.getItem(position);
 			currentView = v;
 
 			// if the ActionMode is already displayed
 			if (mActionMode != null) {
 
 				oldView.setBackgroundColor(profiles_LV.getCacheColorHint());
 				mActionMode.finish(); // closing it
 				mActionMode = startActionMode(new ActionModeProfileSettings()); // and launching it to update values
 				v.setBackgroundColor(Color.GRAY); // the selected item in the listView is highlighted
 				oldView = v;
 			}
 			else{
 
 				// Start the CallBackActionBar using the ActionMode.Callback defined below
 				mActionMode = startActionMode(new ActionModeProfileSettings()); // launching the ActionMode
 				oldView = v;
 				v.setSelected(true);
 				v.setBackgroundColor(Color.GRAY); // the selected item in the listView is highlighted
 			}
 		}
 	}
 
 	/**
 	 * 
 	 * Here, we define the behavior of the actionMode according the input events
 	 * thanks to a specific class
 	 * 
 	 */
 
 	private final class ActionModeProfileSettings implements ActionMode.Callback {
 
 		@Override
 		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
 
 			MenuInflater inflater = getSupportMenuInflater();
 
 			// Load the contextual Menu
 			inflater.inflate(R.menu.context_menu_profile, menu);
 
 			// if the profile is already by default, we cannot set it by default
 			// the icon is updated
 			if(touchIrc.getIdDefaultProfile() == idSelectedProfile){
 				menu.getItem(2).setIcon(android.R.drawable.star_on);
 				menu.getItem(2).setEnabled(false);
 			}
 			else{
 				menu.getItem(2).setIcon(android.R.drawable.star_off);
 			}
 
 			// If there is no server in the database, we disable the button "Link A Server"
 			if(servers.size() == 0){
 				menu.getItem(3).setEnabled(false);
 			}
 
 			// the subMenu providing the available servers is not still created
 			subMenuLinkCreated = false; 
 
 			return true;
 		}
 		
 		/**
 		 * 
 		 * As this method is called after the onCreateActionMode, it will check the links ; but ...
 		 * to check the links, the menu (in parameters) needs its subMenu (and its subItems) but 
 		 * they are not created (only after clicking at the item "Link a Server").
 		 * So, we have to "wait" that the user click at this item for calling onPrepareActionMode,
 		 * which will be called each time a link is added/removed by the user. 
 		 * 
 		 */
 
 		@Override
 		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {		
 			// if the subMenu is created, we can check the links between profiles & servers
 			if(subMenuLinkCreated){
 				checkingLinkBetweenProfileAndServers(menu);
 			}
 			return true;
 		}
 
 		@Override
 		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
 			switch (item.getItemId()) {
 
 			// ########## if the item "Edit" is selected ##########		
 			case R.id.edit : 
 				// Prepare the Intent to switch on the activity which allows to modify the profile
 				Intent i = new Intent(ExistingProfilesActivity.this, CreateProfileActivity.class);
 
 				// Put the informations of the current profile into a Bundle object
 				Bundle b = new Bundle();
				b.putInt("ProfileId", profiles.indexOfValue(selectedProfile));
 
 				// Assign the Bundle to the Intent
 				i.putExtra("ProfileIdentification", b);
 
 				// Start the Intent
 				i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 				startActivity(i);
 
 				finish(); // close the current activity
 
 				return true;
 
 			// ########## if the item "Delete" is selected ##########
 			case R.id.delete :
 
 				// Instantiate an AlertDialog.Builder with its constructor
 				AlertDialog.Builder builder = new AlertDialog.Builder(ExistingProfilesActivity.this);
 
 				// Chain together various setter methods to set the dialog characteristics
 				builder.setTitle(R.string.deleteProfile)
 				.setMessage("Would you really like to delete the profile : " + selectedProfile.getProfile_name() + " ?")
 				.setIcon(android.R.drawable.ic_menu_delete);
 
 				final ActionMode am = mode;
 
 				builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						// Removal throughout the db
 						if (touchIrc.deleteProfile(idSelectedProfile, getApplicationContext())){ // if the deletion is successful
 							// Notify the user thanks to a Toast
 							String s = getResources().getString(R.string.deleted);
 							Toast.makeText(c, selectedProfile.getProfile_name() + " " + s + ".", Toast.LENGTH_LONG).show();
 
 							// Notify the adapter that the list's state has changed
 							adapterProfile.notifyDataSetChanged();
 							// Update the number of available profiles in the actionBar
 							actionBar.setTitle("Profiles  (" + profiles.size() + ")");
 						}
 						am.finish();
 					}
 				});
 				builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						dialog.dismiss();
 					}
 				});
 
 
 				// Get the AlertDialog from create()
 				AlertDialog dialog = builder.create();
 				dialog.show();
 
 				return true;
 
 			// ########## if the item "Set By Default" is selected ##########
 			case R.id.setByDefault :
 
 				String s;
 				// The selected profile is now the default profile
 				if(touchIrc.setDefaultProfile(idSelectedProfile)){
 					mode.getMenu().getItem(2).setIcon(android.R.drawable.star_on);
 					mode.getMenu().getItem(2).setEnabled(false);
 					s = getResources().getString(R.string.profileByDefault);
 					Toast.makeText(c, s + " " + selectedProfile.getProfile_name(), Toast.LENGTH_LONG).show();
 					
 					// Notifying the adapter to update the display
 					adapterProfile.notifyDataSetInvalidated();
 				}
 				else{
 					s = getResources().getString(R.string.somethingWentWrong);
 					Toast.makeText(c, s, Toast.LENGTH_LONG).show();
 				}
 
 				
 
 				return true;
 
 
 			// ########## if the item "Link a Server" is selected ##########
 			
 			case R.id.linkServer :
 
 
 				// This condition is specified to avoid the fact that 
 				// the list becomes longer by multiplying the click on the item
 				if(item.getSubMenu().size() < servers.size()){
 
 					for(int j = 0 ; j < servers.size() ; j++){
 						
 						final int indexServer = j;
 
 						// We add the server's name to the subMenu and allow it to be checked
 						item.getSubMenu().add(servers.valueAt(j).getName());
 						item.getSubMenu().getItem(j).setCheckable(true);
 						
 						if(servers.valueAt(j).hasAssociatedProfile()){
 							// if the link already exists, we check the box
 							if(servers.valueAt(j).getProfile().equals(selectedProfile)){
 								item.getSubMenu().getItem(j).setChecked(true);
 							}
 							else { // if the server is already linked to another profile, we cannot link it
 								item.getSubMenu().getItem(j).setCheckable(false);
 								item.getSubMenu().getItem(j).setEnabled(false);
 							}
 						}
 
 						item.getSubMenu().getItem(j).setOnMenuItemClickListener(new OnMenuItemClickListener() {
 
 							@Override
 							public boolean onMenuItemClick(MenuItem item) {
 
 								if(!item.isChecked()){
 									touchIrc.setProfile(servers.keyAt(indexServer), idSelectedProfile, c);
 									item.setChecked(true);
 									String s = getResources().getString(R.string.nowLinkToTheProfile);
 									Toast.makeText(c, servers.valueAt(indexServer).getName() + " " + s + 
 											" " + selectedProfile.getProfile_name(), Toast.LENGTH_LONG).show();
 								}
 								else{
 									// Delete the existing link
 									touchIrc.setProfile(servers.keyAt(indexServer), 0, c);
 									item.setChecked(false);
 								}
 								// Notify the adapter that the list's state has changed
 								adapterProfile.notifyDataSetChanged();
 								
 								return true;
 							}
 						});
 					}
 
 					// Now, the subMenu is created
 					subMenuLinkCreated = true;
 				}
 
 				return true;
 
 			default:
 				mode.finish(); // the actionMode disappears
 				return false;
 			}
 		}
 
 		@Override
 		public void onDestroyActionMode(ActionMode mode) {
 			// the oldView & the currentView loose the "focus"
 			oldView.setBackgroundColor(profiles_LV.getCacheColorHint());
 			currentView.setBackgroundColor(profiles_LV.getCacheColorHint());
 			// the actionMode is destroyed by putting it at null
 			mActionMode = null;
 		}
 	}
 
 
 	/**
 	 *final 
 	 * This method allows to checking link between all available servers and the selected profile
 	 * If a link is detected, the server is checked in the subMenu's "Link a Server" item
 	 * 
 	 * @param menu
 	 */
 
 	public void checkingLinkBetweenProfileAndServers(Menu menu){
 		for(int s = 1 ; s < servers.size() ; s++){
 			if(servers.valueAt(s).hasAssociatedProfile() && servers.valueAt(s).getProfile().equals(selectedProfile)){
 				menu.getItem(3).getSubMenu().getItem(s).setChecked(true);
 			}
 		}
 	}
 	
 	/**
 	 * 
 	 * This method is called each time the system goes back on this activity
 	 * 
 	 */
 
 	@ Override
 	protected void onResume(){
 		super.onResume();
 		
 		touchIrc = TouchIrc.getInstance();
 		profiles = touchIrc.getAvailableProfiles();
 		servers = touchIrc.getAvailableServers();
 
 		// Update the list and its display
 		this.adapterProfile.notifyDataSetChanged();
 		this.adapterProfile.notifyDataSetInvalidated();
 
 		this.actionBar.setTitle("Profiles  (" + this.profiles.size() + ")");
 	}
 
 	/**
 	 * Define the display of the actionBar
 	 * 
 	 * @param menu
 	 * @return true
 	 */
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu){
 		MenuInflater inflater = getSupportMenuInflater();
 		inflater.inflate(R.menu.existing_object_activity, menu);
 		return true;
 	}
 
 	/**
 	 * 
 	 * This method allows you to configure the behavior of the icon 
 	 * in the action bar: When this button is clicked, the current activity 
 	 * is removed and the previous activity becomes the current activity
 	 * 
 	 */
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		Intent intent;
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// app icon in action bar clicked; go home
 			intent = new Intent(this, MenuActivity.class);
 			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(intent);
 			return true;
 
 		case R.id.add :
 			intent = new Intent(ExistingProfilesActivity.this, CreateProfileActivity.class);
 			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 			Bundle bundle = new Bundle();
 			bundle.putBoolean("comingFromExistingProfilesActivity", true);
 			intent.putExtra("AddingFromExistingProfilesActivity", bundle);
 			startActivity(intent);
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 
 	/**
 	 * 
 	 * This method allows you to configure the behavior of the physical button 
 	 * "Back" (on device) : When this button is pushed, the current activity 
 	 * is removed and the previous activity becomes the current activity
 	 * 
 	 */
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event)  {
 		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
 
 			Intent intent = new Intent(this, MenuActivity.class);
 			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(intent);
 			return true;
 		}
 
 		return super.onKeyDown(keyCode, event);
 	}
 }
