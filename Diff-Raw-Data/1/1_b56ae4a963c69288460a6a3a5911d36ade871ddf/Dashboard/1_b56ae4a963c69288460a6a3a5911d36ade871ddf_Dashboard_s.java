 package org.kth.cos.android.sw;
 
 import org.kth.cos.android.sw.data.FriendManager;
 import org.kth.cos.android.sw.data.ProfileManager;
 import org.kth.cos.android.sw.data.UserAccount;
 import org.kth.cos.android.sw.network.FriendService;
 import org.kth.cos.android.sw.network.ProfileService;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class Dashboard extends BaseActivity {
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.dashboard);
 		setTitle("Welcome");
 		syncFriendList();
 		syncProfileList();
 		generateView();
 	}
 
 	private void generateView() {
 		UserAccount account = UserAccount.getAccount(this);
 		if (!account.isSignedIn()) {
 			startNewActivity(LoginActivity.class, false);
 		} else {
 			((TextView) findViewById(R.id.txtLogin)).setText(account.getEmail());
 		}
 	}
 
 	private void logout() {
 		UserAccount profile = UserAccount.getAccount(Dashboard.this);
 		profile.clear(Dashboard.this);
 		profile = UserAccount.getAccount(Dashboard.this);
 		new FriendManager(Dashboard.this).refrashTable();
 		new ProfileManager(Dashboard.this).refrashTable();
 		generateView();
 	}
 
 	private void syncFriendList() {
 		new Thread() {
 			@Override
 			public void run() {
 				UserAccount account = UserAccount.getAccount(Dashboard.this);
 				FriendService friendService = new FriendService(account.getEmail(), account.getAuthToken(), account.getDataAuthToken(), account
 						.getDataStoreServer());
 				friendService.syncFriendList(Dashboard.this);
 			}
 		}.start();
 	}
 
 	private void syncProfileList() {
 		new Thread() {
 			@Override
 			public void run() {
 				UserAccount account = UserAccount.getAccount(Dashboard.this);
 				ProfileService profileService = new ProfileService(account.getEmail(), account.getDataAuthToken(), account.getDataStoreServer());
 				profileService.syncProfileList(Dashboard.this);
 			}
 		}.start();
 	}
 
 	public void onClickFeature(View v) {
 		int id = v.getId();
 		switch (id) {
 		case R.id.btnNotification:
 			startActivity(new Intent(getApplicationContext(), NotificationActivity.class));
 			break;
 		case R.id.btnFriendsStatus:
 			startActivity(new Intent(getApplicationContext(), FriendsStatusActivity.class));
 			break;
 		case R.id.btnMyStatus:
 			startActivity(new Intent(getApplicationContext(), MyStatusActivity.class));
 			break;
 		case R.id.btnProfileList:
 			startActivity(new Intent(getApplicationContext(), ProfileListActivity.class));
 			break;
 		case R.id.btnManageFriends:
 			startActivity(new Intent(getApplicationContext(), ManageFriendActivity.class));
 			break;
 		case R.id.btnSelectDataserver:
 			startActivity(new Intent(getApplicationContext(), SelectDataserver.class));
 			break;
 		case R.id.btnLogout:
 			logout();
 			break;
 		case R.id.btnExit:
 			Dashboard.this.finish();
 			break;
 		default:
 			break;
 		}
		Dashboard.this.finish();
 	}
 
 }
