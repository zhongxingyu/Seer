 package com.cs411.packman;
 
 import java.util.Locale;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.Messenger;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.cs411.packman.LoginDialogFragment.LoginDialogListener;
 import com.example.packman.R;
 
 public class MainActivity extends FragmentActivity implements
 		LoginDialogListener {
 
 	/**
 	 * The {@link android.support.v4.view.PagerAdapter} that will provide
 	 * fragments for each of the sections. We use a
 	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
 	 * will keep every loaded fragment in memory. If this becomes too memory
 	 * intensive, it may be best to switch to a
 	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
 	 */
 	SectionsPagerAdapter mSectionsPagerAdapter;
 
 	/**
 	 * The {@link ViewPager} that will host the section contents.
 	 */
 	ViewPager mViewPager;
 	ListView mPackageListView;
 	Handler progressHandler;
 	Intent serviceIntent;
 
 	private static String username = null;
 	private static String password = null;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		// Create the adapter that will return a fragment for each of the three
 		// primary sections of the app.
 		mSectionsPagerAdapter = new SectionsPagerAdapter(
 				getSupportFragmentManager());
 
 		// Set up the ViewPager with the sections adapter.
 		mViewPager = (ViewPager) findViewById(R.id.pager);
 		mViewPager.setAdapter(mSectionsPagerAdapter);
 
 		// Create a progress handler to handle background tasks.
 		progressHandler = new Handler() {
 			public void handleMessage(Message msg) {
 				if (msg.arg1 == 1) {
 					ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
 					viewPager.getAdapter().notifyDataSetChanged();
 					showToast(R.string.data_refresh);
 				}
 			}
 		};
 	}
 
 	@Override
 	protected void onPause() {
		if (serviceIntent != null) {
			stopService(serviceIntent);
		}
 		super.onPause();
 	}
	
	@Override
	protected void onResume() {
		if (username != null) {
			Messenger messenger = new Messenger(progressHandler);
			serviceIntent = new Intent(MainActivity.this, PullPackagesDataService.class);
			serviceIntent.putExtra("messenger", messenger);
			startService(serviceIntent);
		}
		super.onResume();
	}
 
 	public static String getUserName() {
 		return username;
 	}
 
 	public static String getPassword() {
 		return password;
 	}
 
 	public static void setUserName(String un) {
 		username = un;
 	}
 
 	public static void setPassword(String pw) {
 		password = pw;
 	}
 
 	@Override
 	public void loginUser(DialogFragment dialog) {
 		// sign in the user ...
 		// User touched the dialog's positive button
 		EditText username = (EditText) dialog.getDialog().findViewById(
 				R.id.username);
 		EditText password = (EditText) dialog.getDialog().findViewById(
 				R.id.password);
 
 		MainActivity.setUserName(username.getText().toString());
 		MainActivity.setPassword(password.getText().toString());
 
 		dialog.getDialog().cancel();
 
 		ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
 		viewPager.getAdapter().notifyDataSetChanged();
 
 		showToast(R.string.login_successful);
 
 		Messenger messenger = new Messenger(progressHandler);
 		serviceIntent = new Intent(MainActivity.this, PullPackagesDataService.class);
 		serviceIntent.putExtra("messenger", messenger);
 		startService(serviceIntent);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.action_logout:
 			MainActivity.setUserName(null);
 			MainActivity.setPassword(null);
 			showToast(R.string.logout_successful);
 		case R.id.action_refresh:
 			ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
 			viewPager.getAdapter().notifyDataSetChanged();
 			showToast(R.string.data_refresh);
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	private void showToast(int text) {
 		Context context = getApplicationContext();
 		int duration = Toast.LENGTH_SHORT;
 		Toast toast = Toast.makeText(context, text, duration);
 		toast.show();
 	}
 
 	/**
 	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 	 * one of the sections/tabs/pages.
 	 */
 	public class SectionsPagerAdapter extends FragmentPagerAdapter {
 
 		public SectionsPagerAdapter(FragmentManager fm) {
 			super(fm);
 		}
 
 		@Override
 		public int getItemPosition(Object object) {
 			return POSITION_NONE;
 		}
 
 		@Override
 		public Fragment getItem(int position) {
 			// getItem is called to instantiate the fragment for the given page.
 			// Return a DummySectionFragment (defined as a static inner class
 			// below) with the page number as its lone argument.
 			Fragment fragment = new SectionFragment();
 			Bundle args = new Bundle();
 			args.putInt(SectionFragment.ARG_SECTION_NUMBER, position + 1);
 			fragment.setArguments(args);
 			return fragment;
 		}
 
 		@Override
 		public int getCount() {
 			return 1;
 		}
 
 		@Override
 		public CharSequence getPageTitle(int position) {
 			Locale l = Locale.getDefault();
 			switch (position) {
 			case 0:
 				return getString(R.string.section_title).toUpperCase(l);
 			}
 			return null;
 		}
 	}
 
 	/**
 	 * A dummy fragment representing a section of the app, but that simply
 	 * displays dummy text.
 	 */
 	public static class SectionFragment extends Fragment {
 		/**
 		 * The fragment argument representing the section number for this
 		 * fragment.
 		 */
 		public static final String ARG_SECTION_NUMBER = "section_number";
 
 		public SectionFragment() {
 		}
 
 		@Override
 		public View onCreateView(LayoutInflater inflater, ViewGroup container,
 				Bundle savedInstanceState) {
 			View rootView = inflater.inflate(R.layout.fragment_main_dummy,
 					container, false);
 
 			if (MainActivity.getUserName() == null) {
 				DialogFragment dialog = new LoginDialogFragment();
 
 				dialog.show(getFragmentManager(), "LoginDialogFragment");
 
 			} else {
 				ListView listView = (ListView) rootView
 						.findViewById(R.id.allPackages);
 				listView.setVisibility(View.VISIBLE);
 
 				try {
 					// Set the ArrayAdapter as the ListView's adapter.
 					listView.setAdapter(new PackageListAdapter(getActivity(),
 							new RequestTask().getPackages()));
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 			return rootView;
 		}
 	}
 }
