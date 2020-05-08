 package com.rava.voting.ui;
 
 import java.util.List;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.Fragment;
 import android.app.FragmentManager;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.widget.DrawerLayout;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.rava.voting.R;
 import com.rava.voting.utils.Utils;
 import com.sromku.simple.fb.Permission;
 import com.sromku.simple.fb.SimpleFacebook;
 import com.sromku.simple.fb.entities.Group;
 import com.sromku.simple.fb.entities.Profile;
 import com.sromku.simple.fb.entities.Profile.Properties;
 import com.sromku.simple.fb.listeners.OnGroupsListener;
 import com.sromku.simple.fb.listeners.OnLoginListener;
 import com.sromku.simple.fb.listeners.OnLogoutListener;
 import com.sromku.simple.fb.listeners.OnProfileListener;
 
 public class MainActivity extends Activity implements
 		NavigationDrawerFragment.NavigationDrawerCallbacks {
 
 	public static final String TAG = "MainActivity";
 
 	/**
 	 * Fragment managing the behaviors, interactions and presentation of the
 	 * navigation drawer.
 	 */
 	private NavigationDrawerFragment mNavigationDrawerFragment;
 
 	/**
 	 * Used to store the last screen title. For use in
 	 * {@link #restoreActionBar()}.
 	 */
 	private CharSequence mTitle;
 
 	private SimpleFacebook mSimpleFacebook;
 	private MenuItem itemFb;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager()
 				.findFragmentById(R.id.navigation_drawer);
 		mTitle = getTitle();
 
 		// Set up the drawer.
 		mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
 				(DrawerLayout) findViewById(R.id.drawer_layout));
 
 		Utils.printHashKey(this);
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		mSimpleFacebook = SimpleFacebook.getInstance(this);
 	}
 
 	@Override
 	public void onNavigationDrawerItemSelected(int position) {
 		onSectionAttached(position);
 		FragmentManager fragmentManager = getFragmentManager();
 
 		switch (position) {
 
 		case 0:
 			Fragment fragment = fragmentManager
 					.findFragmentById(R.id.container);
 			if (fragment != null && fragment instanceof ScannerFragment) {
 				return;
 			} else {
 				fragmentManager
 						.beginTransaction()
 						.replace(R.id.container, ScannerFragment.newInstance(),
 								ScannerFragment.TAG).commit();
 			}
 			break;
 
 		case 1:
 			fragmentManager.beginTransaction()
 					.replace(R.id.container, ElectionsFragment.newInstance())
 					.commit();
 			break;
 
 		case 2:
 			fragmentManager
 					.beginTransaction()
 					.replace(R.id.container,
 							PlaceholderFragment.newInstance(position)).commit();
 			break;
 
 		default:
 			break;
 		}
 
 	}
 
 	public void onSectionAttached(int number) {
 		switch (number) {
 		case 0:
 			mTitle = getString(R.string.section_scan_receipt);
 			break;
 		case 1:
 			mTitle = getString(R.string.section_my_elections);
 			break;
 		case 2:
 			mTitle = getString(R.string.section_vote);
 			break;
 		}
 	}
 
 	public void restoreActionBar() {
 		ActionBar actionBar = getActionBar();
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
 		actionBar.setDisplayShowTitleEnabled(true);
 		actionBar.setTitle(mTitle);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		if (!mNavigationDrawerFragment.isDrawerOpen()) {
 			// Only show items in the action bar relevant to this screen
 			// if the drawer is not showing. Otherwise, let the drawer
 			// decide what to show in the action bar.
 			getMenuInflater().inflate(R.menu.main, menu);
 			itemFb = menu.findItem(R.id.action_fb);
 			restoreActionBar();
 			return true;
 		}
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		if (itemFb != null) {
 			if (mSimpleFacebook.isLogin()) {
 				itemFb.setIcon(R.drawable.ic_action_exit);
 				itemFb.setTitle(R.string.logout);
 			} else {
 				itemFb.setIcon(R.drawable.ic_action_facebook);
 				itemFb.setTitle(R.string.login);
 			}
 			return true;
 		}
 		return super.onPrepareOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		int id = item.getItemId();
 		if (id == R.id.action_fb) {
 			if (mSimpleFacebook.isLogin()) {
 				mSimpleFacebook.logout(mOnLogoutListener);
 			} else {
 				mSimpleFacebook.login(mOnLoginListener);
 			}
 			return true;
 		}
 		if (id == R.id.action_settings) {
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		mSimpleFacebook.onActivityResult(this, requestCode, resultCode, data);
 	}
 
 	public SimpleFacebook getSimpleFacebook() {
 		return mSimpleFacebook;
 	}
 
 	// Login listener
 	private OnLoginListener mOnLoginListener = new OnLoginListener() {
 
 		@Override
 		public void onFail(String reason) {
 			Log.w(TAG, "Failed to login");
 		}
 
 		@Override
 		public void onException(Throwable throwable) {
 			Log.e(TAG, "Bad thing happened", throwable);
 		}
 
 		@Override
 		public void onThinking() {
 			// show progress bar or something to the user while login is
 			// happening
 			Log.i(TAG, "Thinking...");
 		}
 
 		@Override
 		public void onLogin() {
 			Log.i(TAG, "Logged in");
 
 			invalidateOptionsMenu();
 
			Log.i(TAG, mSimpleFacebook.getSession().getAccessToken());
//			Profile.Properties properties = new Profile.Properties.Builder()
//					.add(Properties.ID).add(Properties.NAME).build();
//			mSimpleFacebook.getProfile(properties, mOnProfileListener);
//
//			mSimpleFacebook.getGroups(mOnGroupsListener);
 
 			// final Session session = ((MainActivity)
 			// getActivity()).getSimpleFacebook().getSession();
 			// if (session != null && session.isOpened()) {
 			// // If the session is open, make an API call to get user data
 			// // and define a new callback to handle the response
 			// Request request = Request.newMeRequest(session, new
 			// Request.GraphUserCallback() {
 			// @Override
 			// public void onCompleted(GraphUser user, Response response) {
 			// // If the response is successful
 			// if (session == Session.getActiveSession()) {
 			// if (user != null) {
 			// String user_ID = user.getId();//user id
 			// String profileName = user.getName();//user's profile name
 			// mTextViewContent.setText(user_ID + " / " + profileName);
 			// }
 			// }
 			// }
 			// });
 			// Request.executeBatchAsync(request);
 			// }
 		}
 
 		@Override
 		public void onNotAcceptingPermissions(Permission.Type type) {
 			toast(String
 					.format("You didn't accept %s permissions", type.name()));
 		}
 	};
 
 	// Logout listener
 	private OnLogoutListener mOnLogoutListener = new OnLogoutListener() {
 
 		@Override
 		public void onFail(String reason) {
 			Log.w(TAG, "Failed to login");
 		}
 
 		@Override
 		public void onException(Throwable throwable) {
 			Log.e(TAG, "Bad thing happened", throwable);
 		}
 
 		@Override
 		public void onThinking() {
 			Log.i(TAG, "Thinking...");
 		}
 
 		@Override
 		public void onLogout() {
 			invalidateOptionsMenu();
 			toast("You are logged out");
 		}
 
 	};
 
 	// listener for profile request
 	final OnProfileListener mOnProfileListener = new OnProfileListener() {
 
 		@Override
 		public void onFail(String reason) {
 			// insure that you are logged in before getting the profile
 			Log.w(TAG, reason);
 		}
 
 		@Override
 		public void onException(Throwable throwable) {
 			Log.e(TAG, "Bad thing happened", throwable);
 		}
 
 		@Override
 		public void onThinking() {
 			// show progress bar or something to the user while fetching
 			// profile
 			Log.i(TAG, "Thinking...");
 		}
 
 		@Override
 		public void onComplete(Profile profile) {
 			Log.i(TAG, "My profile id = " + profile.getId());
 			String name = profile.getName();
 			toast("name = " + name + "," + "id = " + profile.getId());
 		}
 	};
 
 	// listener for groups
 	final OnGroupsListener mOnGroupsListener = new OnGroupsListener() {
 
 		@Override
 		public void onFail(String reason) {
 			Log.w(TAG, reason);
 		}
 
 		@Override
 		public void onException(Throwable throwable) {
 			Log.e(TAG, "Bad thing happened", throwable);
 		}
 
 		@Override
 		public void onThinking() {
 			Log.i(TAG, "Thinking...");
 		}
 
 		@Override
 		public void onComplete(List<Group> response) {
 			Log.i(TAG, "Number of groups = " + response.size());
 			toast("Number of groups = " + response.size());
 		}
 	};
 
 	/**
 	 * Show toast
 	 * 
 	 * @param message
 	 */
 	private void toast(String message) {
 		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
 	}
 
 	/**
 	 * A placeholder fragment containing a simple view.
 	 */
 	public static class PlaceholderFragment extends Fragment {
 		/**
 		 * The fragment argument representing the section number for this
 		 * fragment.
 		 */
 		private static final String ARG_SECTION_NUMBER = "section_number";
 
 		/**
 		 * Returns a new instance of this fragment for the given section number.
 		 */
 		public static PlaceholderFragment newInstance(int sectionNumber) {
 			PlaceholderFragment fragment = new PlaceholderFragment();
 			Bundle args = new Bundle();
 			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
 			fragment.setArguments(args);
 			return fragment;
 		}
 
 		public PlaceholderFragment() {
 		}
 
 		@Override
 		public View onCreateView(LayoutInflater inflater, ViewGroup container,
 				Bundle savedInstanceState) {
 			View rootView = inflater.inflate(R.layout.fragment, container,
 					false);
 			TextView textView = (TextView) rootView
 					.findViewById(R.id.section_label);
 			textView.setText(Integer.toString(getArguments().getInt(
 					ARG_SECTION_NUMBER)));
 			return rootView;
 		}
 
 		@Override
 		public void onAttach(Activity activity) {
 			super.onAttach(activity);
 			((MainActivity) activity).onSectionAttached(getArguments().getInt(
 					ARG_SECTION_NUMBER));
 		}
 	}
 
 }
