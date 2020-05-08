 package com.jcertif.android;
 
 import java.util.List;
 
 import android.content.Context;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.view.GravityCompat;
 import android.support.v4.widget.DrawerLayout;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.widget.AdapterView;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.jcertif.android.dao.UserProvider;
 import com.jcertif.android.fragments.InitialisationFragment;
 import com.jcertif.android.fragments.LoginFragment;
 import com.jcertif.android.fragments.ProfileFragment;
 import com.jcertif.android.fragments.SessionListFragment;
 import com.jcertif.android.fragments.SessionParentFragment;
 import com.jcertif.android.fragments.SpeakeListFragment;
 import com.jcertif.android.fragments.SpeakerParentFragment;
 import com.jcertif.android.model.Participant;
 import com.squareup.picasso.Picasso;
 
 /**
  * Partially based on the ASOP source/Drawer Layout
  * 
  * @author Patrick Bashizi (bashizip)
  */
 public class MainActivity extends SherlockFragmentActivity implements
 		LoginFragment.OnSignedInListener {
 
 	private DrawerLayout mDrawerLayout;
 	private ListView mDrawerList;
 	private ActionBarDrawerToggle mDrawerToggle;
 
 	private CharSequence mDrawerTitle;
 	private CharSequence mTitle;
 	private String[] mMenuTitles;
 	private UserProvider userProvider;
 	public static Participant user;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		 getWindow().requestFeature((int) com.actionbarsherlock.view.Window.FEATURE_ACTION_BAR_OVERLAY);
 		setContentView(R.layout.activity_main);
 
 		mTitle = mDrawerTitle = getTitle();
 		mMenuTitles = getResources().getStringArray(R.array.menu_array);
 		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
 		mDrawerList = (ListView) findViewById(R.id.left_drawer);
 
 		// set a custom shadow that overlays the main content when the drawer
 		// opens
 		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
 				GravityCompat.START);
 		// set up the drawer's list view with items and click listener
 		mDrawerList.setAdapter(new DrawerListAdapter(getLoggedInUser()));
 		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
 
 		// enable ActionBar app icon to behave as action to toggle nav drawer
 		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
 		getSupportActionBar().setHomeButtonEnabled(true);
 
 		// ActionBarDrawerToggle ties together the the proper interactions
 		// between the sliding drawer and the action bar app icon
 		mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
 		mDrawerLayout, /* DrawerLayout object */
 		R.drawable.ic_navigation_drawer, /* nav drawer image to replace 'Up' caret */
 		R.string.drawer_open, /* "open drawer" description for accessibility */
 		R.string.drawer_close /* "close drawer" description for accessibility */
 		) {
 			public void onDrawerClosed(View view) {
 				getSupportActionBar().setTitle(mTitle);
 				supportInvalidateOptionsMenu(); // creates call to
 												// onPrepareOptionsMenu()
 			}
 
 			public void onDrawerOpened(View drawerView) {
 				getSupportActionBar().setTitle(mDrawerTitle);
 			
 			}
 		};
 		mDrawerLayout.setDrawerListener(mDrawerToggle);
 
 		if(firstLaunc()){		
 			selectItem(8);
 		}
 		if (savedInstanceState == null) {
 			selectItem(0);
 			mDrawerLayout.openDrawer(Gravity.LEFT);
 		}
 	}
 
 	private boolean firstLaunc() {
 	
 		return false;
 	}
 
 	private Participant getLoggedInUser() {
 		if (user == null) {
 			userProvider = new UserProvider(this);
 			List<Participant> list = userProvider.getAll(Participant.class);
 			if (list != null && !list.isEmpty()) {
 				user = list.get(0);
 			}
 			userProvider.close();
 		}
 		return user;
 	}
 
 	/* The click listner for ListView in the navigation drawer */
 	private class DrawerItemClickListener implements
 			ListView.OnItemClickListener {
 		@Override
 		public void onItemClick(AdapterView<?> parent, View view, int position,
 				long id) {
 			  view.setSelected(true);
 			selectItem(position);
 		}
 	}
 
 	private void selectItem(int position) {
 		// update the main content by replacing fragments
 		Fragment fragment = null;
 		Bundle args = new Bundle();
 		switch (position) {
 		case 0:
 			if (getLoggedInUser() == null) {
 				fragment = new LoginFragment();
 			} else {
 				fragment = new ProfileFragment();
 			}
 			break;
 		case 1:
 			fragment = new SessionParentFragment();
 			break;
 		case 2:
 			fragment = new SpeakerParentFragment();
 			break;
 		case 3:
 
 			break;
 		case 4:
 		//	fragment = new SocialStreamFragment();
 			break;
 		case 8:
 				fragment = new InitialisationFragment();
 				break;
 		default:
 			break;
 		}
 
 		FragmentManager fragmentManager = getSupportFragmentManager();
 		if (fragment == null) {
 			fragment = new SessionListFragment();
 		}
 		fragmentManager.beginTransaction()
 				.replace(R.id.content_frame, fragment).commit();
 
 		mDrawerList.setItemChecked(position, true);
 		mDrawerLayout.closeDrawer(mDrawerList);
 	}
 
 	@Override
 	public void setTitle(CharSequence title) {
 		mTitle = title;
 		getSupportActionBar().setTitle(mTitle);
 	}
 
 	/**
 	 * When using the ActionBarDrawerToggle, you must call it during
 	 * onPostCreate() and onConfigurationChanged()...
 	 */
 
 	@Override
 	protected void onPostCreate(Bundle savedInstanceState) {
 		super.onPostCreate(savedInstanceState);
 		// Sync the toggle state after onRestoreInstanceState has occurred.
 		mDrawerToggle.syncState();
 	}
 
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		super.onConfigurationChanged(newConfig);
 		// Pass any configuration change to the drawer toggls
 		mDrawerToggle.onConfigurationChanged(newConfig);
 	}
 
 
 
 	class DrawerListAdapter extends BaseAdapter {
 
 		private static final int TYPE_PROFILE = 0;
 		private static final int TYPE_ITEM = 1;
 		private LayoutInflater mInflater;
 
 		private Participant user;
 
 		public int getCount() {
 			return mMenuTitles.length;
 		}
 
 		public DrawerListAdapter(Participant user) {
 			mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			this.user = user;
 		}
 
 		@Override
 		public Object getItem(int position) {
 			return mMenuTitles[position];
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return position;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			ViewHolder holder = null;
 			int type = getItemViewType(position);
 			if (convertView == null) {
 				holder = new ViewHolder();
 				switch (type) {
 				case TYPE_ITEM:
 					convertView = mInflater.inflate(R.layout.drawer_list_item,
 							null);
 					holder.textView = (TextView) convertView
 							.findViewById(R.id.tv_drawer_item);
 					
 					holder.imgView = (ImageView) convertView
 							.findViewById(R.id.img_drawer);
 				int index=position - 1;
 				switch (index) {
 				case 0:
 					holder.imgView.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_session));
 					break;
 				case 1:
 					holder.imgView.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_speaker));
 					break;
 				case 2:
 					holder.imgView.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_calendar));
 					break;
 				case 3:
 					holder.imgView.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_social));
 					break;
 				case 4:
 					holder.imgView.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_map));
 					break;
 				default:
 					break;
 				}
 					holder.textView.setText(mMenuTitles[index]);
 					
 					break;
 				case TYPE_PROFILE:
 					convertView = mInflater
 							.inflate(R.layout .item_profile, null);
 					holder.textView = (TextView) convertView
 							.findViewById(R.id.tv_username);
 					holder.imgView = (ImageView) convertView
 							.findViewById(R.id.img_profile); 
 
 					if (user == null) {
 						holder.textView.setText("Please Login");
 					} else {
 						holder.textView.setText(user.getFirstname()+" "+user.getLastname());
 						Picasso.with(MainActivity.this).load(user.getPhoto()).placeholder(R.drawable.ic_action_profile)
 								.into(holder.imgView);
 					}
 
 					break;
 				}
 				convertView.setTag(holder);
 			} else {
 				holder = (ViewHolder) convertView.getTag();
 			}
 
 			return convertView;
 		}
 
 		@Override
 		public int getItemViewType(int position) {
 			return (position == 0) ? TYPE_PROFILE : TYPE_ITEM;
 		}
 
 		@Override
 		public int getViewTypeCount() {
 			return 2;
 		}
 
 		public class ViewHolder {
 			public TextView textView;
 			public ImageView imgView;
 		}
 
 	}
 
 	@Override
 	public void onSignedIn(Participant user) {
 		mDrawerList.setAdapter(new DrawerListAdapter(user));
 		setLogedInUser(user);
 		selectItem(0);
 		mDrawerLayout.openDrawer(Gravity.LEFT);
 	}
 
 	private void setLogedInUser(Participant user) {
 		MainActivity.user = user;
 		userProvider = new UserProvider(this);
 		userProvider.store(user);
 		userProvider.close();
 	}
 
 }
