 package edu.dartmouth.mhb;
 
 
 import android.app.ActionBar;
 import android.app.SearchManager;
 import android.content.Context;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.view.GravityCompat;
 import android.support.v4.widget.DrawerLayout;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.SearchView;
 import java.util.ArrayList;
 
 public class MainActivity extends FragmentActivity {
 	Context context;
 	MySQLiteHelper myDBHelper;
 	private HymnsDataSource datasource;
 	private ArrayList<Hymn> hymns;
    private boolean showBar = true;
 
 	private DrawerLayout mDrawerLayout;
 	private ListView mDrawerList;
 	private ActionBarDrawerToggle mDrawerToggle;
 
 	private CharSequence mDrawerTitle;
 	private CharSequence mTitle;
 	private String[] mDrawerMenuTitles;
 
 	final String[] mFragments = {
 			"edu.dartmouth.mhb.MenuFragments.MenuTodayFragment",
 			"edu.dartmouth.mhb.MenuFragments.MenuHymnsFragment",
 			"edu.dartmouth.mhb.MenuFragments.MenuCanticlesFragment",
 			"edu.dartmouth.mhb.MenuFragments.MenuCreedsFragment",
 			"edu.dartmouth.mhb.MenuFragments.MenuFavoritesFragment",
 			"edu.dartmouth.mhb.MenuFragments.MenuAboutFragment" };
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setTheme(R.style.MainTheme);
 		setContentView(R.layout.activity_main);
 		context = getApplicationContext();
 
 		mTitle = mDrawerTitle = getTitle();
 		mDrawerMenuTitles = getResources().getStringArray(
 				R.array.drawer_menu_array);
 		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
 		mDrawerList = (ListView) findViewById(R.id.left_drawer);
 
 		// set a custom shadow that overlays the main content when the drawer
 		// opens
 		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
 				GravityCompat.START);
 		// set up the drawer's list view with items and click listener
 		mDrawerList.setAdapter(new ArrayAdapter<String>(this,
 				R.layout.drawer_list_item, mDrawerMenuTitles));
 		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
 
 		// enable ActionBar app icon to behave as action to toggle nav drawer
 		ActionBar actionbar = getActionBar();
 		actionbar.setDisplayHomeAsUpEnabled(true);
 		actionbar.setHomeButtonEnabled(true);
 
         //actionbar.setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_background));
 
         //TODO toggle hide and show action bar on touch
 //        actionbar.hide();
 //        actionbar.show();
 
 		// ActionBarDrawerToggle ties together the the proper interactions
 		// between the sliding drawer and the action bar app icon
 		mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
 		mDrawerLayout, /* DrawerLayout object */
 		R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
 		R.string.drawer_open, /* "open drawer" description for accessibility */
 		R.string.drawer_close /* "close drawer" description for accessibility */
 		) {
 			public void onDrawerClosed(View view) {
 				getActionBar().setTitle(mTitle);
 				invalidateOptionsMenu(); // creates call to
 											// onPrepareOptionsMenu()
 			}
 
 			public void onDrawerOpened(View drawerView) {
 				getActionBar().setTitle(mDrawerTitle);
 				invalidateOptionsMenu(); // creates call to
 											// onPrepareOptionsMenu()
 			}
 		};
 		mDrawerLayout.setDrawerListener(mDrawerToggle);
 
 		datasource = new HymnsDataSource(this);
 		datasource.open();
 		hymns = datasource.getAllHymns();
 
         if (savedInstanceState == null) {
             selectItem(1);
         }
 
 	}
 
 	@Override
 	protected void onResume() {
 		datasource.open();
 		super.onResume();
 	}
 
 	@Override
 	protected void onPause() {
 		datasource.close();
 		super.onPause();
 	}
 
 	// TODO: menu options and next/previous actions
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.main, menu);
 
 		// Get the SearchView and set the searchable configuration
 		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
 		SearchView searchView = (SearchView) menu.findItem(R.id.menu_search)
 				.getActionView();
 		searchView.setSearchableInfo(searchManager
 				.getSearchableInfo(getComponentName()));
 
 		// Do not iconify the widget;expand it by default
 		searchView.setIconifiedByDefault(false);
 
 		return super.onCreateOptionsMenu(menu);
 
 	}
 
 	/* Called whenever we call invalidateOptionsMenu() */
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		// If the nav drawer is open, hide action items related to the content
 		// view
 		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
 		menu.findItem(R.id.menu_search).setVisible(!drawerOpen);
 		// TODO set all other menus as invisible
 		return super.onPrepareOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// The action bar home/up action should open or close the drawer.
 		// ActionBarDrawerToggle will take care of this.
 		if (mDrawerToggle.onOptionsItemSelected(item)) {
 			return true;
 		}
 		// TODO Switch Statement to handle menu item selection
 		return super.onOptionsItemSelected(item);
 	}
 
 
 	// ///////////////////////////
 	// //Drawer Layout Methods////
 	// ///////////////////////////
 
 	/* The click listner for ListView in the navigation drawer */
 	private class DrawerItemClickListener implements
 			ListView.OnItemClickListener {
 		@Override
 		public void onItemClick(AdapterView<?> parent, View view, int position,
 				long id) {
 			selectItem(position);
 		}
 	}
 
 	private void selectItem(int pos) {
         Fragment fragment = Fragment.instantiate(MainActivity.this, mFragments[pos]);
         FragmentManager fragmentManager = getSupportFragmentManager();
 
         switch(pos)
         {
         	case 1:
                 Bundle bundle = new Bundle();
                 bundle.putParcelableArrayList("hymn_list",hymns);
                 fragment.setArguments(bundle);
                 fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
                 break;
                 
 	    	default:      
 	    		fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
         }
    	
 
 
         // update selected item and title, then close the drawer
         mDrawerList.setItemChecked(pos, true);
         setTitle(mDrawerMenuTitles[pos]);
         mDrawerLayout.closeDrawer(mDrawerList);
     }	
 	
 	@Override
 	public void setTitle(CharSequence title) {
 		mTitle = title;
 		getActionBar().setTitle(mTitle);
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
 		// Pass any configuration change to the drawer toggle
 		mDrawerToggle.onConfigurationChanged(newConfig);
 	}
 
 
 
 
 }
