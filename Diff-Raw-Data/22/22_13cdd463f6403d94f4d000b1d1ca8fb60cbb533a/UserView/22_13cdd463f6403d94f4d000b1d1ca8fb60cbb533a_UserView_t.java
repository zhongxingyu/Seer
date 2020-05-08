 package com.connectsy.users;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore.Images;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.connectsy.ActionBarHandler;
 import com.connectsy.R;
 import com.connectsy.data.AvatarFetcher;
 import com.connectsy.data.DataManager;
 import com.connectsy.data.DataManager.DataUpdateListener;
 import com.connectsy.settings.MainMenu;
 import com.connectsy.users.UserManager.User;
 import com.connectsy.events.EventManager.Filter;
 import com.connectsy.events.EventManager;
 import com.connectsy.events.EventsAdapter;
 import com.connectsy.utils.Utils;
 
 public class UserView extends Activity implements OnClickListener, DataUpdateListener {
 	@SuppressWarnings("unused")
 	private static final String TAG = "UserView";
     private String curUsername;
     private UserManager userManager;
     private User user;
     private String username;
     private FriendsAdapter adapter;
     private EventsAdapter eventsAdapter;
     private String tabSelected = "events";
     private int operationsPending = 0;
     private static final int REFRESH_USER = 0;
     private static final int SELECT_AVATAR = 1;
     private static final int BEFRIEND = 2;
     private static final int REFRESH_FRIENDS = 3;
     private static final int REFRESH_PENDING_FRIENDS = 4;
 	private static final int REFRESH_CUR_PENDING_FRIENDS = 5;
 	private static final int CONFIRM_USER = 6;
 	private static final int UPLOAD_AVATAR = 7;
 	private static final int REFRESH_EVENTS = 8;
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.user_view);
 
         //set up logo clicks
         new ActionBarHandler(this);
         
         ImageView abRefresh = (ImageView)findViewById(R.id.ab_refresh);
         abRefresh.setOnClickListener(this);
         ImageView abSearch = (ImageView)findViewById(R.id.ab_user_search);
         abSearch.setOnClickListener(this);
         
         username = getIntent().getExtras().getString("com.connectsy.user.username");
         curUsername = UserManager.getCache(this).getString("username", "");
 
         findViewById(R.id.user_view_events_button).setOnClickListener(this);
         findViewById(R.id.user_view_friends_button).setOnClickListener(this);
         
 		updateUserDisplay();
 		updateTab(null);
 		
     	refresh();
     }
 	
     private void updateUserDisplay(){
     	user = getUserManager().getUser();
     	
         TextView uname = (TextView)findViewById(R.id.user_view_username);
         uname.setText(username);
 		
         if (user != null){
 	        ImageView avatar = (ImageView)findViewById(R.id.user_view_avatar);
 	        new AvatarFetcher(this, user.username, avatar).fetch();
 	        if (username.equals(DataManager.getCache(this).getString("username", null))){
 	        	avatar.setClickable(true);
 	        	avatar.setOnClickListener(this);
 	        }
 	        
 
 	        if (!user.username.equals(curUsername)){
     			ArrayList<User> friends = getUserManager().getFriends(false);
 	        	boolean isFriend = false;
 	        	for (int i = 0;i < friends.size();i++)
 	        		if (friends.get(i).username.equals(curUsername))
 	        			isFriend = true;
         		ImageView f = (ImageView)findViewById(R.id.user_view_befriend);
         		ImageView unf = (ImageView)findViewById(R.id.user_view_unfriend);
 	        	if (!isFriend){
 	        		f.setOnClickListener(this);
 	        		f.setVisibility(Button.VISIBLE);
 	        		unf.setVisibility(Button.GONE);
 	        	}else{
 	        		unf.setOnClickListener(this);
 	        		unf.setVisibility(Button.VISIBLE);
 	        		f.setVisibility(Button.GONE);
 	        	}
 	        }
         }
     }
     
     private void updateTab(String tab){
     	if (tab != null) tabSelected = tab;
     	if (tabSelected.equals("events")){
     		findViewById(R.id.user_view_friends).setVisibility(View.GONE);
     		findViewById(R.id.user_view_events).setVisibility(View.VISIBLE);
 			findViewById(R.id.user_view_events_button).setSelected(true);
 			findViewById(R.id.user_view_friends_button).setSelected(false);
 			
 			ArrayList<String> revs = new EventManager(this, this, Filter.CREATOR, 
 					username).getRevisions();
 
 	        if (eventsAdapter != null){
 	        	eventsAdapter.clear();
 	        	for (int n = 0;n < revs.size();n++)
 	        		eventsAdapter.add(revs.get(n));
 	        	eventsAdapter.notifyDataSetChanged();
 	        }else{
 	        	eventsAdapter = new EventsAdapter(this, R.layout.event_list_item, revs);
 	        }
 	        ListView lv = (ListView) findViewById(R.id.user_view_events);
 	        lv.setAdapter(eventsAdapter);
 			Utils.setFooterView(this, lv);
 			
     	}else if (tabSelected.equals("friends")){
     		findViewById(R.id.user_view_events).setVisibility(View.GONE);
     		findViewById(R.id.user_view_friends).setVisibility(View.VISIBLE);
 			findViewById(R.id.user_view_events_button).setSelected(false);
 			findViewById(R.id.user_view_friends_button).setSelected(true);
 
     		if (user != null){
     			ArrayList<User> friends = getUserManager().getFriends(false);
     			ArrayList<User> pendingFriends = null;
    			ArrayList<Object> inView = new ArrayList<Object>();
     			if (user.username.equals(curUsername)){
     				pendingFriends = getUserManager().getFriends(true);
	    			if (pendingFriends.size() != 0){
	    				inView.add("Pending Friends");
	    				inView.addAll(pendingFriends);
	        			inView.add("Friends");
	    			}
     			}
     			inView.addAll(friends);
     	        if (adapter != null){
     	        	adapter.update(inView);
     	        }else{
     	            adapter = new FriendsAdapter(this, this, inView, CONFIRM_USER);
     	        }
     	        ListView lv = (ListView) findViewById(R.id.user_view_friends);
     	        lv.setAdapter(adapter);
     			Utils.setFooterView(this, lv);
     		}
     	}
     }
     
 	private void changeAvatar(){
 		startActivityForResult(new Intent(Intent.ACTION_PICK, 
 				Images.Media.INTERNAL_CONTENT_URI), SELECT_AVATAR);
 	}
 	
 	private void befriend(){
 		getUserManager(true).befriend(BEFRIEND);
 		operationsPending++;
 		setRefreshing(true);
 	}
 	
 	private void unfriend(){
 		getUserManager(true).unfriend(BEFRIEND);
 		operationsPending++;
 		setRefreshing(true);
 	}
 	
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 	    super.onActivityResult(requestCode, resultCode, data);
 	    if (requestCode == SELECT_AVATAR && resultCode == Activity.RESULT_OK) {
 			Uri selectedImage = data.getData();
 			try {
 				getUserManager(true).uploadAvatar(selectedImage, UPLOAD_AVATAR);
 				operationsPending++;
 				setRefreshing(true);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 	    } 
 	}
 	
     public boolean onCreateOptionsMenu(Menu menu) {
         return MainMenu.onCreateOptionsMenu(menu);
 	}
     
     public boolean onOptionsItemSelected(MenuItem item) {
         return MainMenu.onOptionsItemSelected(this, item);
     }
 
 	public void onClick(View v) {
     	if (v.getId() == R.id.ab_refresh) refresh();
     	else if (v.getId() == R.id.ab_user_search) startActivity(
     			new Intent(this, UserSearch.class));
     	else if (v.getId() == R.id.user_view_avatar) changeAvatar();
     	else if (v.getId() == R.id.user_view_befriend) befriend();
     	else if (v.getId() == R.id.user_view_unfriend) unfriend();
     	else if (v.getId() == R.id.user_view_friends_button) updateTab("friends");
     	else if (v.getId() == R.id.user_view_events_button) updateTab("events");
 	}
 	
 	private void refresh(){
 		getUserManager(true).refreshUser(REFRESH_USER);
 		getUserManager(true).refreshFriends(false, REFRESH_FRIENDS);
 		getUserManager(true).refreshFriends(true, REFRESH_PENDING_FRIENDS);
 		new EventManager(this, this, Filter.CREATOR, username)
 				.refreshRevisions(REFRESH_EVENTS);
 		operationsPending += 4;
 		setRefreshing(true);
 	}
 	
     private void setRefreshing(boolean on) {
     	if (on){
 	        findViewById(R.id.ab_refresh).setVisibility(View.GONE);
 	        findViewById(R.id.ab_refresh_spinner).setVisibility(View.VISIBLE);
     	}else{
 	        findViewById(R.id.ab_refresh).setVisibility(View.VISIBLE);
 	        findViewById(R.id.ab_refresh_spinner).setVisibility(View.GONE);
     	}
     }
 
 	public void onDataUpdate(int code, String response) {
 		if (code == BEFRIEND){
 			findViewById(R.id.user_view_befriend).setVisibility(View.GONE);
 			new UserManager(this, this, curUsername).refreshFriends(true, 
 					REFRESH_CUR_PENDING_FRIENDS);
 			operationsPending++;
 			updateTab(null);
 		}else if (code == REFRESH_USER){
 			updateUserDisplay();
 		}else if (code == UPLOAD_AVATAR){
 	        ImageView avatar = (ImageView)findViewById(R.id.user_view_avatar);
 	        new AvatarFetcher(this, user.username, avatar).fetch(true);
 		}else{
 			updateTab(null);
 		}
 		operationsPending--;
 		if (operationsPending <= 0){
 			// refresh friends can return from more then one request 
 			// if the friends aren't cached.
 			operationsPending = 0;
 			setRefreshing(false);
 		}
 	}
 
 	public void onRemoteError(int httpStatus, int code) {
 		Log.d(TAG, "onRemoteError: "+httpStatus);
 		operationsPending--;
 		if (operationsPending == 0)
 			setRefreshing(false);
 	}
 
 	private UserManager getUserManager(){
 		return getUserManager(false); }
 	private UserManager getUserManager(boolean forceNew){
 		if (userManager == null || forceNew)
 			userManager = new UserManager(this, this, username);
 		return userManager;
 	}
 }
