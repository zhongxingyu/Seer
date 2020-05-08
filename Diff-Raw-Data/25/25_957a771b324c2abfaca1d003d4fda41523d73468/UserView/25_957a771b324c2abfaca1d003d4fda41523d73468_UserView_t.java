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
 import com.connectsy.utils.Utils;
 
 public class UserView extends Activity implements OnClickListener, 
 		DataUpdateListener {
 	@SuppressWarnings("unused")
 	private static final String TAG = "UserView";
     private String curUsername;
     private UserManager userManager;
     private User user;
     private String username;
     private UserAdapter adapter;
     private UserAdapter pendingAdapter;
     private int operationsPending = 0;
     private static final int REFRESH_USER = 0;
     private static final int SELECT_AVATAR = 1;
     private static final int BEFRIEND = 2;
     private static final int REFRESH_FRIENDS = 3;
     private static final int REFRESH_PENDING_FRIENDS = 4;
 	private static final int REFRESH_CUR_PENDING_FRIENDS = 5;
 	private static final int CONFIRM_USER = 6;
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.user_view);
 
        //set up logo clicks
        ActionBarHandler ab = new ActionBarHandler(this);
        
         ImageView abRefresh = (ImageView)findViewById(R.id.ab_refresh);
         abRefresh.setOnClickListener(this);
         ImageView abSearch = (ImageView)findViewById(R.id.ab_user_search);
         abSearch.setOnClickListener(this);
         
         username = getIntent().getExtras().getString("com.connectsy.user.username");
         curUsername = UserManager.getCache(this).getString("username", "");
     	
 		updateUserDisplay();
 		updateFriendsDisplay();
 		updatePendingFriendsDisplay();
 		
     	refresh();
     }
 	
     private void updateUserDisplay(){
     	user = getUserManager().getUser();
     	
         TextView uname = (TextView)findViewById(R.id.user_view_username);
         uname.setText(username);
 		
         if (user != null){
 	        ImageView avatar = (ImageView)findViewById(R.id.user_view_avatar);
 	        new AvatarFetcher(this, user.username, avatar);
 	        if (username.equals(DataManager.getCache(this).getString("username", null))){
 	        	avatar.setClickable(true);
 	        	avatar.setOnClickListener(this);
 	        }
         }
     }
     
     private void updateFriendsDisplay(){
 		if (user != null){
 			ArrayList<User> friends = getUserManager().getFriends(false);
 			Log.d(TAG, "friends: "+friends);
 	        if (adapter != null){
 	        	adapter.clear();
 	        	for (int n = 0;n < friends.size();n++)
 	        		adapter.add(friends.get(n));
 	    		adapter.notifyDataSetChanged();
 	        }else{
 	            adapter = new UserAdapter(this, R.layout.user_list_item, friends, false);
 	        }
 	        ListView lv = (ListView) findViewById(R.id.friends_list);
 	        lv.setAdapter(adapter);
 			Utils.setFooterView(this, lv);
 	        
 	        if (!user.username.equals(curUsername)){
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
     
     private void updatePendingFriendsDisplay(){
 		if (user != null && user.username.equals(curUsername)){
 			ArrayList<User> pendingFriends = getUserManager().getFriends(true);
 			if (pendingFriends.size() > 0){
 				findViewById(R.id.pending_friends_list_title).setVisibility(TextView.VISIBLE);
 			}else{
 				findViewById(R.id.pending_friends_list_title).setVisibility(TextView.GONE);
 			}
 	        if (pendingAdapter != null){
 	        	pendingAdapter.clear();
 	        	for (int n = 0;n < pendingFriends.size();n++)
 	        		pendingAdapter.add(pendingFriends.get(n));
 	        	pendingAdapter.notifyDataSetChanged();
 	        }else{
 	        	pendingAdapter = new UserAdapter(this, this, R.layout.user_list_item, 
 	        			pendingFriends, false, CONFIRM_USER);
 	        }
 			
 	        ListView plv = (ListView)findViewById(R.id.pending_friends_list);
 	        plv.setAdapter(pendingAdapter);
 		}else if (user != null){
 			UserManager curUserManager = new UserManager(this, this, curUsername);
 			ArrayList<User> pendingFriends = curUserManager.getFriends(true);
         	boolean isPending = false;
         	for (int i = 0;i < pendingFriends.size();i++)
         		if (pendingFriends.get(i).username.equals(curUsername))
         			isPending = true;
         	if (isPending)
         		findViewById(R.id.user_view_befriend).setVisibility(Button.GONE);
         	else
         		Log.d(TAG, "pending: "+pendingFriends);
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
 				getUserManager(true).uploadAvatar(selectedImage);
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
 	}
 	private void refresh(){
 		getUserManager(true).refreshUser(REFRESH_USER);
 		getUserManager(true).refreshFriends(false, REFRESH_FRIENDS);
 		getUserManager(true).refreshFriends(true, REFRESH_PENDING_FRIENDS);
 		operationsPending += 3;
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
 		}else if (code == REFRESH_USER){
 			updateUserDisplay();
 		}else if (code == CONFIRM_USER){
 			new UserManager(this, this, curUsername).refreshFriends(true, 
 					REFRESH_CUR_PENDING_FRIENDS);
 			operationsPending++;
 		}else {
 			updateFriendsDisplay();
 			updatePendingFriendsDisplay();
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
