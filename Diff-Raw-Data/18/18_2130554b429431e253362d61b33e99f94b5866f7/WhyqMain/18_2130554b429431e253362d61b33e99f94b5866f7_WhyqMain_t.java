 package whyq;
 
 import java.io.UnsupportedEncodingException;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 
 import javax.crypto.BadPaddingException;
 import javax.crypto.IllegalBlockSizeException;
 import javax.crypto.NoSuchPaddingException;
 
 import whyq.activity.AudioPlayerActivity;
 import whyq.activity.FriendsFacebookActivity;
 import whyq.activity.FavouritesActivityGroup;
 import whyq.activity.ListActivity;
 import whyq.activity.ListActivityGroup;
 import whyq.activity.ImageActivityGroup;
 import whyq.activity.LoginHome;
 import whyq.activity.LoginWhyqActivity;
 import whyq.activity.FriendActivityGroup;
 import whyq.activity.ProfileActivityGroup;
 import whyq.activity.SimpleActivity;
import whyq.activity.UserBoardActivity;
 import whyq.model.User;
 import whyq.utils.MyLocationListener;
 import whyq.utils.RSA;
 import whyq.utils.WhyqUtils;
 import whyq.utils.XMLParser;
 import android.app.TabActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.TabHost;
 import android.widget.TabHost.OnTabChangeListener;
 import android.widget.TabHost.TabSpec;
 
 import com.whyq.R;
 
 public class WhyqMain extends TabActivity {
 	/** Called when the activity is first created. */
 	public static String UID = "121";
 	private static TabHost tabHost;
 	public static Context context;
 	public static boolean isUserProfile = true;
 	public static boolean isKakao = false;
 	private String token;
 	private RSA rsa;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		token = XMLParser.getToken(WhyqApplication.Instance()
 				.getApplicationContext());
 		if (token == null || token.equals("")) {
 			Intent intent = new Intent(WhyqMain.this, LoginHome.class);
 			startActivity(intent);
 		} else {
 			tabHost = getTabHost();
 			context = WhyqMain.this;
 			tabHost.getTabWidget().setBackgroundResource(
 					R.drawable.tabs_background);
 			// Tab for followers
 			TabSpec followers = tabHost.newTabSpec("List");
 			followers.setIndicator("List",
 					getResources().getDrawable(R.drawable.footer_icon1));
 			Intent followersIntent = new Intent(this, ListActivityGroup.class);
 			followers.setContent(followersIntent);
 			tabHost.addTab(followers);
 
 			// Tab for Explorer
 			TabSpec explorer = tabHost.newTabSpec("Favourites");
 			// setting Title and Icon for the Tab
 			explorer.setIndicator("Favourites",
 					getResources().getDrawable(R.drawable.footer_icon2));
 			Intent explorerIntent = new Intent(this,
 					FavouritesActivityGroup.class);
 			explorer.setContent(explorerIntent);
 			tabHost.addTab(explorer);
 
 			// // Tab for Image
 			// TabSpec image = tabHost.newTabSpec("Friends");
 			// image.setIndicator("Images",
 			// getResources().getDrawable(R.drawable.icon_image_tab));
 			// Intent imageIntent = new Intent(this, ImageActivityGroup.class);
 			// image.setContent(imageIntent);
 			// tabHost.addTab( image );
 
 			TabSpec mydiary = tabHost.newTabSpec("Friends");
 			mydiary.setIndicator("Friends",
 					getResources().getDrawable(R.drawable.footer_icon3));
 			Intent mydiaryIntent = new Intent(this,
 					FriendsFacebookActivity.class);
 			mydiary.setContent(mydiaryIntent);
 			tabHost.addTab(mydiary);
 
 			TabSpec profile = tabHost.newTabSpec("Profile");
 			profile.setIndicator("Profile",
 					getResources().getDrawable(R.drawable.footer_icon4));
			Intent profileIntent = new Intent(this, UserBoardActivity.class);
 			profile.setContent(profileIntent);
 			tabHost.addTab(profile);
 
 			tabHost.setOnTabChangedListener(new OnTabChangeListener() {
 
 				@Override
 				public void onTabChanged(String tabId) {
 					// TODO Auto-generated method stub
 					int currentTab = WhyqMain.getCurrentTab();
 					if (currentTab == 0) {
 						ListActivityGroup.isTabChanged = false;
 						FavouritesActivityGroup.isTabChanged = true;
 						ProfileActivityGroup.isTabChanged = true;
 						FriendActivityGroup.isTabChanged = true;
 					} else if (currentTab == 1) {
 						ListActivityGroup.isTabChanged = true;
 						FavouritesActivityGroup.isTabChanged = false;
 						ProfileActivityGroup.isTabChanged = true;
 						FriendActivityGroup.isTabChanged = true;
 					} else if (currentTab == 2) {
 						ListActivityGroup.isTabChanged = true;
 						FavouritesActivityGroup.isTabChanged = true;
 						ProfileActivityGroup.isTabChanged = true;
 						FriendActivityGroup.isTabChanged = true;
 					} else if (currentTab == 3) {
 						ListActivityGroup.isTabChanged = true;
 						FavouritesActivityGroup.isTabChanged = true;
 						ProfileActivityGroup.isTabChanged = true;
 						FriendActivityGroup.isTabChanged = false;
 					} else if (currentTab == 4) {
 						ListActivityGroup.isTabChanged = true;
 						FavouritesActivityGroup.isTabChanged = true;
 						ProfileActivityGroup.isTabChanged = false;
 						FriendActivityGroup.isTabChanged = true;
 					}
 				}
 			});
 
 			// Set the event for Followers tab
 			tabHost.getTabWidget().getChildAt(0)
 					.setOnTouchListener(new FollowerHandler());
 
 			// Set the event for Explorer tab
 			tabHost.getTabWidget().getChildAt(1)
 					.setOnTouchListener(new View.OnTouchListener() {
 
 						@Override
 						public boolean onTouch(View v, MotionEvent event) {
 							// do whatever you need
 							FavouritesActivityGroup.group.clearHistory();
 							return false;
 
 						}
 					});
 
 			// // Set the event for Images tab
 			// tabHost.getTabWidget().getChildAt(2).setOnTouchListener(new
 			// View.OnTouchListener() {
 			//
 			// @Override
 			// public boolean onTouch(View v, MotionEvent event) {
 			// //do whatever you need
 			// ImageActivityGroup.group.clearHistory();
 			// return false;
 			//
 			// }
 			// });
 
 			// Set the event for MyDiary tab
 			// tabHost.getTabWidget().getChildAt(3).setOnTouchListener(new
 			// ValidateHandler());
 			tabHost.getTabWidget().getChildAt(2)
 					.setOnTouchListener(new View.OnTouchListener() {
 
 						@Override
 						public boolean onTouch(View v, MotionEvent event) {
 							// do whatever you need
 							FriendActivityGroup.group.clearHistory();
 							return false;
 
 						}
 					});
 			// Set the event for Profile tab
 			tabHost.getTabWidget().getChildAt(3)
 					.setOnTouchListener(new View.OnTouchListener() {
 
 						@Override
 						public boolean onTouch(View v, MotionEvent event) {
 							// do whatever you need
 							ProfileActivityGroup.group.clearHistory();
 							return false;
 
 						}
 					});
 			LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 			LocationListener locationListener = new MyLocationListener();
 			locationManager.requestLocationUpdates(
 					LocationManager.GPS_PROVIDER, 1000, 10, locationListener);
 
 			tabHost.setCurrentTab(0);
			// rsa = new RSA();
			// getListData();
 		}
 
 	}
 
 	private void getListData() {
 		// TODO Auto-generated method stub
 
 	}
 
 	private class ProfileHandler implements View.OnTouchListener {
 		boolean ret = false;
 
 		@Override
 		public boolean onTouch(View v, MotionEvent event) {
 			User user = WhyqUtils.isAuthenticated(getApplicationContext());
 			if (user != null) {
 				UID = user.getId();
 				getTabHost().setCurrentTab(4);
 				ret = false;
 			} else {
 				// Go to login screen
 				getTabHost().setCurrentTab(4);
 				showLogin();
 				ret = true;
 			}
 
 			return ret;
 		}
 	}
 
 	private class FollowerHandler implements View.OnTouchListener {
 
 		@Override
 		public boolean onTouch(View v, MotionEvent event) {
 			User user = WhyqUtils.isAuthenticated(getApplicationContext());
 			if (user != null) {
 				// Load following perms of user
 				v.findViewById(R.id.permList);
 
 			} else {
 
 			}
 			ListActivityGroup.group.clearHistory();
 			return false;
 		}
 	}
 
 	private class ValidateHandler implements View.OnTouchListener {
 
 		@Override
 		public boolean onTouch(View v, MotionEvent event) {
 			boolean ret = false;
 			int action = event.getAction();
 			if (action == MotionEvent.ACTION_UP) {
 				/**
 				 * Load the information from Application (user info) when the
 				 * page is loaded.
 				 */
 				User user = WhyqUtils.isAuthenticated(getApplicationContext());
 				if (user != null) {
 					UID = user.getId();
 					tabHost.setCurrentTab(4);
 					gotoDiaryTab(null);
 					ret = false;
 				} else {
 					// Go to login screen
 					gotoDiaryTab(null);
 					showLogin();
 					ret = false;
 				}
 				FriendActivityGroup.group.clearHistory();
 			}
 			return ret;
 		}
 
 	}
 
 	public static void showLogin() {
 		closeLoginActivity();
 		Intent myIntent = new Intent(context, LoginHome.class);
 		int currentTab = tabHost.getCurrentTab();
 		if (currentTab == 0) {
 			View boardListView = ListActivityGroup.group
 					.getLocalActivityManager()
 					.startActivity("detail",
 							myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
 					.getDecorView();
 			ListActivityGroup.group.replaceView(boardListView);
 		} else if (currentTab == 1) {
 			View boardListView = FavouritesActivityGroup.group
 					.getLocalActivityManager()
 					.startActivity("detail",
 							myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
 					.getDecorView();
 			FavouritesActivityGroup.group.replaceView(boardListView);
 		} else if (currentTab == 2) {
 			View boardListView = ImageActivityGroup.group
 					.getLocalActivityManager()
 					.startActivity("detail",
 							myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
 					.getDecorView();
 			ImageActivityGroup.group.replaceView(boardListView);
 		} else if (currentTab == 3) {
 			View boardListView = FriendActivityGroup.group
 					.getLocalActivityManager()
 					.startActivity("detail",
 							myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
 					.getDecorView();
 			FriendActivityGroup.group.replaceViewWithoutHistory(boardListView);
 		} else if (currentTab == 4) {
 			View boardListView = ProfileActivityGroup.group
 					.getLocalActivityManager()
 					.startActivity("detail",
 							myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
 					.getDecorView();
 			ProfileActivityGroup.group.replaceViewWithoutHistory(boardListView);
 		}
 	}
 
 	public static void back() {
 
 		int currentTab = tabHost.getCurrentTab();
 		if (currentTab == 0) {
 			ListActivityGroup.group.back();
 		} else if (currentTab == 1) {
 			// View view =
 			// ProfileActivityGroup.group.getLocalActivityManager().startActivity(
 			// "ExplorerActivity", new Intent(context,
 			// ExplorerActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)).getDecorView();
 			FavouritesActivityGroup.group.back();
 			// ExplorerActivityGroup.group.overrideView(view);
 		} else if (currentTab == 2) {
 			ImageActivityGroup.group.back();
 		} else if (currentTab == 3) {
 			FriendActivityGroup.group.back();
 		} else if (currentTab == 4) {
 
 			// View view =
 			// ProfileActivityGroup.group.getLocalActivityManager().startActivity(
 			// "ProfileActivity", new Intent(context,
 			// ProfileActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)).getDecorView();
 			ProfileActivityGroup.group.back();
 			// ProfileActivityGroup.group.overrideView(view);
 
 		}
 	}
 
 	public static void gotoDiaryTab(String UID) {
 		// TODO Auto-generated method stub
 		tabHost.setCurrentTab(3);
 	}
 
 	public static void gotoTab(int tab, Object data) {
 
 		if (tab == 4) {
 			// tabHost.setCurrentTab(tab);
 			isUserProfile = false;
 			ListActivityGroup.createProfileActivity(data, false);
 
 		} else if (tab == 5) {
 			// Intent imageDetail = new Intent(context, ImageDetail.class);
 			// imageDetail.putExtra("url", (String) data);
 			// context.startActivity(imageDetail);
 			if (ListActivity.loadPermList != null) {
 				ListActivity.loadPermList.cancel(true);
 			}
 			String link = (String) data;
 			Intent browserIntent = new Intent(Intent.ACTION_VIEW,
 					Uri.parse(link));
 			ListActivityGroup.context.startActivity(browserIntent);
 			ListActivity.isRefesh = false;
 		} else if (tab == 2) {
 			tabHost.setCurrentTab(2);
 		} else if (tab == 6) {
 			String link = (String) data;
 			Intent playAudioIntent = new Intent(ListActivityGroup.context,
 					AudioPlayerActivity.class);
 			playAudioIntent.putExtra("url", link);
 			// Log.d("permAudio=",""+link);
 			ListActivityGroup.context.startActivity(playAudioIntent);
 			// FollowerActivity.isRefesh = false;
 		}
 
 	}
 
 	public static int getCurrentTab() {
 		return tabHost.getCurrentTab();
 	}
 
 	public void on_success() {
 		// TODO Auto-generated method stub
 		// getTabHost().setCurrentTab(3);
 	}
 
 	public void on_error() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public static void closeLoginActivity() {
 		ListActivityGroup.group.getLocalActivityManager().destroyActivity(
 				"detail", true);
 		FavouritesActivityGroup.group.getLocalActivityManager()
 				.destroyActivity("detail", true);
 		// ImageActivityGroup.group.getLocalActivityManager().destroyActivity("detail",
 		// true);
 		FriendActivityGroup.group.getLocalActivityManager().destroyActivity(
 				"detail", true);
 		ProfileActivityGroup.group.getLocalActivityManager().destroyActivity(
 				"detail", true);
 	}
 
 	public static void refeshFollowerActivity() {
 		if (WhyqMain.getCurrentTab() == 0) {
 			ListActivityGroup.group.sendBroadcast("", "");
 		}
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
 			WhyqMain.back();
 			return true;
 		} else {
 			return super.onKeyDown(keyCode, event);
 		}
 	}
 }
