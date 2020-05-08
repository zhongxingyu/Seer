 package edu.gatech.oad.rocket.findmythings;
 
 import android.app.*;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v13.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.SearchView;
 import com.google.api.services.fmthings.model.MessageBean;
 import edu.gatech.oad.rocket.findmythings.control.LoginManager;
 import edu.gatech.oad.rocket.findmythings.model.Type;
 import edu.gatech.oad.rocket.findmythings.service.EndpointUtils;
 import edu.gatech.oad.rocket.findmythings.util.EnumHelper;
 import edu.gatech.oad.rocket.findmythings.util.ErrorDialog;
 
 import java.io.IOException;
 import java.util.concurrent.atomic.AtomicReference;
 
 import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
 import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;
 
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
 
 	/**
 	 * Cached list of tab strings.
 	 */
 	private String[] mTabs = null;
 
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
 			for (String mTab : mTabs) {
 				ActionBar.Tab tab = mActionBar.newTab();
 				tab.setTabListener(this);
 				tab.setText(mTab);
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
 	 * @param savedInstanceState
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(mTabsLayoutRes);
 
 		mTabs = getResources().getStringArray(mTabsListRes);
 
 		pager = (ViewPager)findViewById(R.id.pager);
 		new TypeTabsAdapter(getFragmentManager(), getActionBar(), pager);
 
 		ActionBar actionBar = getActionBar();
 		actionBar.setDisplayHomeAsUpEnabled(false);
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 		actionBar.setDisplayShowTitleEnabled(true);
 
 		setTitle(getString(R.string.app_name));
 	}
 
 	private Type getDisplayedTypeInPager(int pagerPosition) {
 		int offIndex = pagerPosition % mTabs.length;
 		return offIndex == 0 ? null : EnumHelper.forInt(offIndex-1, Type.class);
 	}
 
 	protected Type getDisplayedType() {
 		return getDisplayedTypeInPager(pager.getCurrentItem());
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		// Check which request we're responding to
 		if (requestCode == LoginActivity.LOGIN_REQUEST) {
 			if (resultCode == RESULT_OK) {
 				// nothing needs to be done here just yet
 				// isLoggedIn happens in onPrepareOptionsMenu
 			}
 		} else if (requestCode == SubmitActivity.SUBMIT_REQUEST) {
 			if (resultCode == RESULT_OK) {
 				// TODO
 				// the Intent extras has MainActivity.EXTRA_LIST,
 				// switch to that tab and trigger a reload
 			}
 		}
 	}
 
 	/**
 	 * updates the options menu (login, account, admin button)
 	 * @param menu
 	 */
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		LoginManager mgr = LoginManager.getLoginManager();
 		boolean loggedIn = mgr.isLoggedIn();
 
 		//Set Login Title
 		MenuItem loginMenu = menu.findItem(R.id.menu_login);
 		String title = loggedIn ? "Logout" : "Login";
 		loginMenu.setTitle(title);
 
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
 		if (!loggedIn || (mgr.getCurrentUser() != null && !mgr.getCurrentUser().getAdmin())) {
 			adminMenu.setVisible(false);
 		} else {
 			adminMenu.setVisible(true);
 		}
 
 		return true;
 	}
 
 
 	/**
 	 * creates the options menu and adds the search listener
 	 * @param menu
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
 				// TODO actually search
 				return true;
 			}
 
 			@Override
 			public boolean onQueryTextSubmit(String query) {
 				// TODO actually search
 				return true;
 			}
 		};
 
 		mSearch.setOnQueryTextListener(queryTextListener);
 
 		return true;
 	}
 
 	/**
 	 * takes care of action when a key is pressed down
 	 * @param  keyCode
 	 * @param  event
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
 			case android.R.id.home:  // This is the home button in the top left corner of the screen.
 				// Don't call finish! Because activity could have been started by an outside activity and the home button would not operated as expected!
 				Intent homeIntent = new Intent(this, MainActivity.class);
 				homeIntent.addFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP);
 				startActivity(homeIntent);
 				return true;
 			case R.id.item_list_submit:
 				return toSubmit();
 			case R.id.menu_list_filter:
 				Intent i = new Intent(this, FilterActivity.class);
 				startActivityForResult(i, 1);
 				overridePendingTransition(R.anim.slide_up_modal, android.R.anim.fade_out);
 				return true;
 			case R.id.menu_login:
 				return LoginManager.getLoginManager().isLoggedIn() ? logOut() : toLogIn();
 			case R.id.menu_account:
 				return toAccount();
 			case R.id.menu_admin:
 				return toAdmin();
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
 			if (type != null) goToNextActivity.putExtra(SubmitActivity.EXTRA_TYPE, type.toString());
 			startActivityForResult(goToNextActivity, SubmitActivity.SUBMIT_REQUEST);
			startActivity(goToNextActivity);
 			overridePendingTransition(R.anim.slide_up_modal, R.anim.hold);
 		}
 		else {
 			ErrorDialog toLogin =  new ErrorDialog(R.string.main_submit_log_in_message, R.string.main_submit_log_in, R.string.main_submit_log_in_cancel);
 			AlertDialog.Builder temp = toLogin.getDialog(this,
 					new DialogInterface.OnClickListener() {
 						@Override
 						public void onClick(DialogInterface dialog, int id) {
 							MainActivity.this.toLogIn();
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
      * Go to LoginActivity
      * @return boolean true when done
      */
     public boolean toLogIn() {
     	Intent toLogin = new Intent(this, LoginActivity.class);
 		startActivityForResult(toLogin, LoginActivity.LOGIN_REQUEST);
 		overridePendingTransition(R.anim.slide_up_modal, R.anim.hold);
 		return true;
     }
     
     /**
      * Go to AccountActivity
      * @return true when done
      */
     public boolean toAccount() {
     	Intent toAccount = new Intent(this, AccountActivity.class);
 		startActivity(toAccount);
 		overridePendingTransition(R.anim.slide_up_modal, R.anim.hold);
 		return true;
     }
     
     /**
      * Go to AdminActivity
      * @return true when done
      */
     public boolean toAdmin() {
     	Intent toAccount = new Intent(this, AdminActivity.class);
 		startActivity(toAccount);
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
 
 					/**
 					 * logs current user out
 					 * @param dialog
 					 * @param id
 					 */
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
 
 					/**
 					 * keeps user logged in
 					 * @param dialog
 					 * @param id
 					 */
 					@Override
 					public void onClick(DialogInterface dialog, int id) {
 						logOut = false;
 					}
 
 				}
 		));
 		temp.get().show();
     	return logOut;    	
     }
     
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
