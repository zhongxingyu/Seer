 package ch.unibe.ese.shopnote.activities;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.Toast;
 import ch.unibe.ese.shopnote.R;
 import ch.unibe.ese.shopnote.adapters.FriendsListAdapter;
 import ch.unibe.ese.shopnote.core.BaseActivity;
 import ch.unibe.ese.shopnote.core.Friend;
 import ch.unibe.ese.shopnote.core.FriendsManager;
 import ch.unibe.ese.shopnote.share.SyncManager;
 import ch.unibe.ese.shopnote.share.requests.FriendRequest;
 
 /**
  *	Creates a frame which enlists all friends and the possibility to manage them
  */
 public class ManageFriendsActivity extends BaseActivity {
 	
 	private FriendsManager friendsManager;
 	private SyncManager syncManager;
 	private ArrayAdapter<Friend> friendsAdapter;
	
	private static boolean isFriendsSyncing = false;
 
 	@Override
 	/**
 	 * initializes the friendslist and loads all friends.
 	 */
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_manage_friends);
         
 		//set chosen color theme
 		RelativeLayout lo = (RelativeLayout) findViewById(R.id.RelativeLayoutManageFriends);
 		RelativeLayout rlDrawer = (RelativeLayout) findViewById(R.id.drawer_Linearlayout);
 		updateTheme(lo, getActionBar(), rlDrawer);
 		
 		// Show the Up button in the action bar.
 		setupActionBar();
 
 		friendsManager = getFriendsManager();
 		syncManager = getSyncManager();
 		
 		// verify phone number
 		getMyPhoneNumber();
 		
 		// Create drawer menu
 		createDrawerMenu();
 		createDrawerToggle();
 		
 		//Update all friends
		if (!isFriendsSyncing) 
 			searchContactsInPhoneBook();
 		
 		updateFriendsList();
 		
 		setTitle(this.getString(R.string.title_activity_manage_friends));
 		
 	}
 	
 	/**
 	 * Set up the {@link android.app.ActionBar}.
 	 */
 	private void setupActionBar() {
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 	}
 	
 	/**
 	 * Updates the Viewlist, which shows all friends and adds itemclicklistener
 	 */
 	public void updateFriendsList(){
 		List<Friend> friendsList = new ArrayList<Friend>(friendsManager.getFriendsList());
 		friendsAdapter = new FriendsListAdapter(this,
 				R.layout.shopping_list_item, friendsList);
 
 		ListView listView = (ListView) findViewById(R.id.friends_list);
 		listView.setAdapter(friendsAdapter);
 		updateThemeListView(listView);
 		
 		// hide welcome message
 		RelativeLayout welcome = (RelativeLayout) findViewById(R.id.manage_friends_welcome);
 		if (friendsAdapter.isEmpty())
 			welcome.setVisibility(View.VISIBLE);
 		else
 			welcome.setVisibility(View.GONE);
 		
 		listView.setOnItemLongClickListener(new OnItemLongClickListener() {
 
 			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
 					int position, long arg3) {
 				Friend selectedFriend = friendsAdapter.getItem(position);
 				ManageFriendsActivity.this.startActionMode(new FriendListActionMode(
 						friendsManager, friendsAdapter, selectedFriend, ManageFriendsActivity.this));
 				return true;
 			}
 		});
 	}
 	
 	protected void onResume() {
 		super.onResume();
 		for(Friend f: friendsManager.getFriendsList()) {
 			FriendRequest fr = new FriendRequest(f);
 			syncManager.addRequest(fr);
 		}
 		
 		syncManager.synchronise(this);
 		updateFriendsList();
 	}
 	
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.manage_friends, menu);
 		return true;
 	}
 	
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    // Pass the event to ActionBarDrawerToggle, if it returns
 	    // true, then it has handled the app icon touch event
 	    if (drawerToggle.onOptionsItemSelected(item))
 	    	return true;
 	    
 		// Handle presses on the action bar items
 		switch (item.getItemId()) {
 		case R.id.action_new:
 			Intent intent = new Intent(this, CreateFriendActivity.class);
 			this.startActivityForResult(intent, INTENT_FRIEND_REQUEST);
 			return true;
 		case R.id.action_refresh:
 			if(isOnline()) {
 				Animation rotation = AnimationUtils.loadAnimation(this,
 						R.drawable.sync_animation);
 				rotation.setRepeatCount(Animation.INFINITE);
 				findViewById(R.id.action_refresh).startAnimation(rotation);
				if (!isFriendsSyncing)
 					searchContactsInPhoneBook();
 			} else {
 				Toast.makeText(this, this.getString(R.string.no_connection),
 						Toast.LENGTH_SHORT).show();
 				refresh();
 			}
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	/**
 	 * searches for contacts in the phonebook who do have the app
 	 */
 	public void searchContactsInPhoneBook() {
		isFriendsSyncing = true;
 		final SynchHandler handler = new SynchHandler(this);
 		new Thread(new Runnable() {
 			public void run() {
 				friendsManager.checkPhoneBookForFriends(ManageFriendsActivity.this, handler);	
 			}
 		}).start(); 
 	}
 
 	
 	@Override
 	protected void onPause() {
 		super.onPause();
     	drawMenu.closeDrawers();
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		if(resultCode == RESULT_OK && requestCode == INTENT_FRIEND_REQUEST){
 			if(isOnline()) {
 				final String name = data.getExtras().getString(EXTRAS_FRIEND_NAME);
 				final String number = data.getExtras().getString(EXTRAS_FRIEND_PHONENR);
 				Toast.makeText(this, R.string.checkIfFriendHasApp, Toast.LENGTH_SHORT).show();
 				final SynchHandler handler = new SynchHandler(this);
 				
 				new Thread(new Runnable() {
 					public void run() {	
 						Friend friend = friendsManager.addFriend(new Friend(number, name));
 						if(friend != null && friend.hasTheApp()) {
 							handler.sendEmptyMessage(0);
 						} else {
 							handler.sendEmptyMessage(1);
 						}
 					}
 				}).start();
 			} else {
 				Toast.makeText(this, this.getString(R.string.no_connection),
 						Toast.LENGTH_SHORT).show();
 				refresh();
 			}
 		}	
 	}
 
 	@Override
 	public void refresh() {
		isFriendsSyncing = false;
 		updateFriendsList();
 		Animation anim = findViewById(R.id.action_refresh).getAnimation();
 		if(anim != null) {
 			anim.setRepeatCount(0);
 		}
 	}
 }
 
