 package edu.gatech.oad.rocket.findmythings;
 
 import android.app.*;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v13.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.text.TextUtils;
 import android.view.*;
 import android.widget.SearchView;
 import edu.gatech.oad.rocket.findmythings.model.MessageBean;
 import edu.gatech.oad.rocket.findmythings.control.LoginManager;
 import edu.gatech.oad.rocket.findmythings.list.ItemFilterConstraint;
 import edu.gatech.oad.rocket.findmythings.service.EndpointUtils;
 import edu.gatech.oad.rocket.findmythings.model.Type;
 import edu.gatech.oad.rocket.findmythings.util.EnumHelper;
 import edu.gatech.oad.rocket.findmythings.util.ErrorDialog;
 import edu.gatech.oad.rocket.findmythings.util.ToastHelper;
 
 import java.io.IOException;
 import java.util.concurrent.atomic.AtomicReference;
 
 /**
  * CS 2340 - FindMyStuff Android App
  *
  * An activity representing a list of Items. This activity has different
  * presentations for handset and tablet-size devices. On handsets, the activity
  * presents a list of items, which when touched, lead to a
  * {@link ItemDetailActivity} representing item details. On tablets, the
  * activity presents the list of items and item details side-by-side using two
  * vertical panes.
  *
  * @author TeamRocket
  */
 public class MainActivity extends Activity {
 
 	public static final String EXTRA_LIST = "displayedList";
 
 	/**
 	 * Quick at-a-glance identifier of the XML layout used for this activity.
 	 */
 	private static final int mTabsLayoutRes = R.layout.activity_main_tabs;
 
 	/**
 	 * Quick at-a-glance identifier of the strings used to populate this activity's tabs.
 	 */
 	private static final int mTabsListRes = R.array.main_tabs;
 	private static final int mTabsSummaryRes = R.array.main_tabs_summary;
 
 	/**
 	 * Cached list of tab strings.
 	 */
 	private String[] mTabs = null;
 	private String[] mTabSummaries = null;
 
 	/**
 	 * Used for the AlertDialog when user tries to sign out
 	 */
 	private boolean logOut = false;
 
 	/**
 	 * Used for querying what Type is being displayed
 	 */
 	private ViewPager pager;
 
 	/**
 	 * Indirect reference to the search bar in the ActionBar
 	 */
 	private MenuItem searchMenuItem;
 
 	/**
 	 * Box shown to prevent user interaction during logout
 	 */
 	private ProgressDialog mProgressDialog;
 
 	private class TypeTabsAdapter extends FragmentPagerAdapter
 			implements ActionBar.TabListener, ViewPager.OnPageChangeListener {
 		private final ActionBar mActionBar;
 		private final ViewPager mViewPager;
 
 		TypeTabsAdapter(FragmentManager fragmentManager, ActionBar actionBar, ViewPager pager) {
 			super(fragmentManager);
 			mActionBar = actionBar;
 			mViewPager = pager;
 			mViewPager.setAdapter(this);
 			mViewPager.setOnPageChangeListener(this);
 			mViewPager.setOffscreenPageLimit(mTabs.length);
 			for (int i = 0; i < mTabs.length; i++) {
 				String mTab = mTabs[i];
 				String mTabDesc = mTabSummaries[i];
 				ActionBar.Tab tab = mActionBar.newTab();
 				tab.setTabListener(this);
 				tab.setText(mTab);
 				tab.setContentDescription(mTabDesc);
 				mActionBar.addTab(tab);
 			}
 			notifyDataSetChanged();
 		}
 
 		@Override
 		public Fragment getItem(int position) {
 			Type thisType = getDisplayedTypeInPager(position);
 			return ItemListFragment.newInstance(thisType);
 		}
 
 		@Override
 		public void onPageSelected(int i) {
 			mActionBar.setSelectedNavigationItem(i);
 		}
 
 		@Override
 		public int getCount() {
 			return mTabs.length;
 		}
 
 		@Override
 		public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
 			mViewPager.setCurrentItem(tab.getPosition());
 		}
 
 		@Override public void onPageScrolled(int i, float v, int i2) {}
 		@Override public void onPageScrollStateChanged(int i) {}
 		@Override public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {}
 		@Override public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {}
 	}
 
 	/**
 	 * creates window with correct layout
 	 * @param savedInstanceState Initializing bundle
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(mTabsLayoutRes);
 
 		mTabs = getResources().getStringArray(mTabsListRes);
 		mTabSummaries = getResources().getStringArray(mTabsSummaryRes);
 
 		pager = (ViewPager)findViewById(R.id.pager);
 		new TypeTabsAdapter(getFragmentManager(), getActionBar(), pager);
 
 		ActionBar actionBar = getActionBar();
 		actionBar.setDisplayHomeAsUpEnabled(false);
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 		actionBar.setDisplayShowTitleEnabled(true);
 	}
 
 	private Type getDisplayedTypeInPager(int pagerPosition) {
 		int offIndex = pagerPosition % mTabs.length;
 		return offIndex == 0 ? null : EnumHelper.forInt(offIndex-1, Type.class);
 	}
 
 	protected Type getDisplayedType() {
 		return getDisplayedTypeInPager(pager.getCurrentItem());
 	}
 
 	private ItemListFragment getDisplayedFragment() {
 		int position = pager.getCurrentItem();
 		String tag = "android:switcher:" + pager.getId() + ":" + position;
 		return (ItemListFragment)getFragmentManager().findFragmentByTag(tag);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		// Check which request we're responding to
 		if (requestCode == LoginActivity.LOGIN_REQUEST) {
 			if (resultCode == RESULT_OK) {
 				// isLoggedIn stuff happens in onPrepareOptionsMenu
 				ToastHelper.showShort(this, R.string.main_logged_in);
 			}
 		} else if (requestCode == SubmitActivity.SUBMIT_REQUEST) {
 			if (resultCode == RESULT_OK) {
 				// TODO
 				// the Intent extras has MainActivity.EXTRA_LIST,
 				// switch to that tab and trigger a reload
 			}
 		} else if (requestCode == FilterActivity.FILTER_REQUEST) {
 			ItemListFragment frag = getDisplayedFragment();
 			ItemFilterConstraint constraint = null;
			
			if (data != null ) {
				
				constraint = FilterActivity.constraint;
 			}
 
 			// null filter removes filtering
 			frag.performFilter(constraint);
 
 			if (constraint != null && !constraint.isEmpty()) {
 				startActionMode(new ActionMode.Callback() {
 					@Override
 					public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
 						String titleFormat = getString(R.string.filtering_title);
 						String title = String.format(titleFormat, mTabs[pager.getCurrentItem()]);
 						actionMode.setTitle(title);
 
 						MenuInflater inf = actionMode.getMenuInflater();
 						inf.inflate(R.menu.activity_main_filtered, menu);
 						return true;
 					}
 
 					@Override
 					public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
 						return false; // nothing to do, nothing to see
 					}
 
 					@Override
 					public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
 						switch (menuItem.getItemId()) {
 							case R.id.filtering_done:
 								actionMode.finish();
 								return true;
 							default:
 								return false;
 						}
 					}
 
 					@Override
 					public void onDestroyActionMode(ActionMode actionMode) {
 						getDisplayedFragment().performFilter(null);
 					}
 				});
 			}
 		}
 	}
 
 	/**
 	 * updates the options menu (login, account, admin button)
 	 * @param menu System action bar menu
 	 */
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		LoginManager mgr = LoginManager.getLoginManager();
 		boolean loggedIn = mgr.isLoggedIn();
 
 		//Set Login Title
 		MenuItem loginMenu = menu.findItem(R.id.menu_login);
 		loginMenu.setTitle(loggedIn ? R.string.main_menu_log_out : R.string.main_menu_log_in);
 
 		//Set Account Title
 		MenuItem accountMenu = menu.findItem(R.id.menu_account);
 		if (loggedIn) {
 			accountMenu.setTitle(LoginManager.getLoginManager().getCurrentEmail());
 			accountMenu.setVisible(true);
 		} else {
 			accountMenu.setTitle(R.string.main_title_account);
 			accountMenu.setVisible(false);
 		}
 
 		//Show/Hide admin button
 		MenuItem adminMenu = menu.findItem(R.id.menu_admin);
 		if ((mgr.getCurrentUser()!= null && mgr.getCurrentUser().getAdmin()!=null && !mgr.getCurrentUser().getAdmin() )|| !loggedIn) {
 			adminMenu.setVisible(false);
 		} else {
 			adminMenu.setVisible(true);
 		}
 
 		return true;
 	}
 
 
 	/**
 	 * creates the options menu and adds the search listener
 	 * @param menu System action bar menu
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main_menu, menu);
 
 		searchMenuItem = menu.findItem(R.id.main_search_bar);
 
 		SearchView mSearch = (SearchView) searchMenuItem.getActionView();
 		mSearch.bringToFront();
 
 		SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
 			@Override
 			public boolean onQueryTextChange(String newText) {
 				return true;
 			}
 
 			@Override
 			public boolean onQueryTextSubmit(String query) {
 				if (TextUtils.isEmpty(query)) return false;
 				Intent to = new Intent(MainActivity.this, SearchResultsActivity.class);
 				to.putExtra(ItemListFragment.ARG_QUERY, query);
 				Type type = getDisplayedType();
 				if (type != null) to.putExtra(ItemListFragment.ARG_TYPE, type);
 				startActivity(to);
 				overridePendingTransition(R.anim.slide_up_modal, R.anim.hold);
 				return true;
 			}
 		};
 
 		mSearch.setOnQueryTextListener(queryTextListener);
 
 		return true;
 	}
 
 	/**
 	 * takes care of action when a key is pressed down
 	 * @param  keyCode The value in event.getKeyCode().
 	 * @param  event description of the event
 	 * @return boolean 
 	 */
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event)  {
 		//Tells Activity what to do when back key is pressed
 	    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
 			return false;
 	    }
 	    if (keyCode == KeyEvent.KEYCODE_SEARCH) {
 	    	//Shows the search bar
 		searchMenuItem.expandActionView();
 	    	return true;
 	    }
 	    return super.onKeyDown(keyCode, event);
 	}
 
 	/**
 	 * shows the options that are available for user depending on the item selected (submit/search/login/etc)
 	 * @param item - menu selected
 	 * @return boolean
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id.item_list_submit:
 				return toSubmit();
 			case R.id.menu_list_filter:
 				return toActivity(FilterActivity.class, FilterActivity.FILTER_REQUEST);
 			case R.id.menu_login:
 				return LoginManager.getLoginManager().isLoggedIn() ? logOut() : toActivity(LoginActivity.class, LoginActivity.LOGIN_REQUEST);
 			case R.id.menu_account:
 				return toActivity(AccountActivity.class, AccountActivity.REQUEST_ACCOUNT);
 			case R.id.menu_admin:
 				return toActivity(AdminActivity.class, AdminActivity.REQUEST_ADMIN);
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 	}
 
 
 	/**
 	 * Opens a new SubmitActivity activity with the current type of item.
 	 */
 	public boolean toSubmit() {
 		if (LoginManager.getLoginManager().isLoggedIn()) {
 			Intent goToNextActivity = new Intent(this, SubmitActivity.class);
 			Type type = getDisplayedType();
 			if (type != null) goToNextActivity.putExtra(SubmitActivity.EXTRA_TYPE, type);
 			startActivityForResult(goToNextActivity, SubmitActivity.SUBMIT_REQUEST);
 			overridePendingTransition(R.anim.slide_up_modal, R.anim.hold);
 		}
 		else {
 			ErrorDialog toLogin =  new ErrorDialog(R.string.main_submit_log_in_message, R.string.main_submit_log_in, R.string.main_submit_log_in_cancel);
 			AlertDialog.Builder temp = toLogin.getDialog(this,
 					new DialogInterface.OnClickListener() {
 						@Override
 						public void onClick(DialogInterface dialog, int id) {
 							MainActivity.this.toActivity(LoginActivity.class, LoginActivity.LOGIN_REQUEST);
 						}
 					},
 					new DialogInterface.OnClickListener() {
 						@Override
 						public void onClick(DialogInterface dialog, int id) {
 							//cancel
 						}
 					});
 			temp.show();
 		}
 		return true;
 	}
 
 	/**
 	 * Go to a simple modal, wait-for-response activity.
 	 * @param clazz An activity
 	 * @param request An integer expressing the callback evaluated in onActivityResult()
 	 * @return boolean ALWAYS true, when the activity begins
 	 */
 	protected <T extends Activity> boolean toActivity(Class<T> clazz, int request) {
 		Intent to = new Intent(this, clazz);
 		startActivityForResult(to, request);
 		overridePendingTransition(R.anim.slide_up_modal, R.anim.hold);
 		return true;
 	}
     
     /**
      * Logout
      * @return boolean: true if logout, false if not
      */
     public boolean logOut() {
 	ErrorDialog toLogin = new ErrorDialog(R.string.main_sign_out_confirm, R.string.main_sign_out_ok, R.string.main_sign_out_cancel);
 		AtomicReference<AlertDialog.Builder> temp = new AtomicReference<AlertDialog.Builder>(toLogin.getDialog(this,
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int id) {
 						logOut = true;
 						mProgressDialog = ProgressDialog.show(MainActivity.this,
 								getString(R.string.main_sign_out_title),
 								getString(R.string.main_sign_out_message), true, false);
 						new LogoutTask().execute();
 					}
 				},
 
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int id) {
 						logOut = false;
 					}
 
 				}
 		));
 		temp.get().show();
     	return logOut;    	
     }
 
 	/**
 	 * Helper class: asynchronously log out and clear the LoginManager NO MATTER WHAT
 	 */
     public class LogoutTask extends AsyncTask<Void, Void, MessageBean> {
 
     	@Override
     	protected MessageBean doInBackground(Void... arg0) {
     		try {
     			return EndpointUtils.getEndpoint().account().logout().execute();
     		} catch (IOException e) {
     			return null;
     		}
     	}
     	
     	@Override
     	protected void onPostExecute(final MessageBean output) {
     		mProgressDialog.dismiss();
     		mProgressDialog = null;
     		
     		//Clear current user
     		LoginManager.getLoginManager().logout();
     		
     		//Making sense is for squares
     		Intent newMain = getIntent();
     		newMain.setFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME);
     		newMain.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
     		finish(); 
     		startActivity(newMain);
     		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
     	}
 
     }
 }
