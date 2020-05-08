 package org.gnuton.newshub;
 
 import android.annotation.TargetApi;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.res.Configuration;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.support.v4.widget.DrawerLayout;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.FrameLayout;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import org.gnuton.newshub.adapters.ArticleListAdapter;
 import org.gnuton.newshub.adapters.FeedListAdapter;
 import org.gnuton.newshub.adapters.MainPageFragmentAdapter;
 import org.gnuton.newshub.db.RSSEntryDataSource;
 import org.gnuton.newshub.db.RSSFeedDataSource;
 import org.gnuton.newshub.types.RSSEntry;
 import org.gnuton.newshub.types.RSSFeed;
 import org.gnuton.newshub.utils.FontsProvider;
 import org.gnuton.newshub.utils.FragmentUtils;
 import org.gnuton.newshub.utils.MyApp;
 import org.gnuton.newshub.utils.Notifications;
 
 import java.util.List;
 
 
 public class MainActivity extends FragmentActivity
         implements ArticleListFragment.OnItemSelectedListener {
     // generic fields
     private static final String TAG = "MAIN_ACTIVITY";
     private int mOrientation;
 
     //Action Bar
     private ActionBarDrawerToggle mDrawerToggle;
 
     //Layouts
     private DrawerLayout mDrawerLayout;
     private LinearLayout mDrawerPanelLayout;
 
     //Views
     private ListView mDrawerList;
 
     //Data sources
     private static RSSFeedDataSource mFeedDataSource = new RSSFeedDataSource(MyApp.getContext());
     private static RSSEntryDataSource mEntryDataSource = new RSSEntryDataSource(MyApp.getContext());
 
     //Fragments
     private Fragment mArticleListFragment;
     private Fragment mArticleDetailFragment;
 
     //Adapters
     private FragmentPagerAdapter mFragmentPagerAdapter;
 
 
 
     @TargetApi(Build.VERSION_CODES.HONEYCOMB)
     @Override
     protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         MyApp.getInstance().mMainActivity = this;
 
         setContentView(R.layout.activity_main);
 
         Log.i(TAG, "CREATEEEEEEEE");
         mOrientation = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getRotation();
         mArticleListFragment = FragmentUtils.getFragment(getSupportFragmentManager(), ArticleListFragment.class.getName(), null);
         mArticleDetailFragment = FragmentUtils.getFragment(getSupportFragmentManager(), ArticleFragment.class.getName(), null);
 
         ViewPager pager = (ViewPager) findViewById(R.id.mainPager);
         if (pager != null) {
             // run in most of cases
             mFragmentPagerAdapter = new MainPageFragmentAdapter(getSupportFragmentManager());
             pager.setAdapter(mFragmentPagerAdapter);
         } else {
             // This is used for large portrait layouts
             if (savedInstanceState == null) {
             }
             getSupportFragmentManager()
                     .beginTransaction()
                             //.setCustomAnimations(R.animator.slidein, R.animator.slideout, R.animator.slideinpop, R.animator.slideoutpop)
                     .replace(R.id.articlelist_container, mArticleListFragment)
                     .replace(R.id.articledetail_container, mArticleDetailFragment)
                     .commit();
 
         }
 
         //Set up custom action bar
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
             getActionBar().setCustomView(R.layout.actionbar);
             final TextView actionBarTitle = (TextView) findViewById(R.id.actionBarTitle);
             actionBarTitle.setTypeface(FontsProvider.getInstace().getTypeface("Daily News 1915"));
             actionBarTitle.setText(getString(R.string.app_name));
         }
 
         //Set up Navigation drawer
         mDrawerPanelLayout = (LinearLayout) findViewById(R.id.layout_panel_drawer);
         mDrawerList = (ListView) findViewById(R.id.list_drawer);
         mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
         mDrawerList.setOnItemLongClickListener(new DrawerItemLongClickListener());
 
         // set a custom shadow that overlays the main content when the drawer opens
         mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
         mDrawerLayout.setScrimColor(android.R.color.transparent);
         //mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
 
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
             // enable ActionBar app icon to behave as action to toggle nav drawer
             getActionBar().setDisplayHomeAsUpEnabled(true);
             getActionBar().setHomeButtonEnabled(true);
         }
 
         mDrawerToggle = new ActionBarDrawerToggle(
                 this,                  /* host Activity */
                 mDrawerLayout,         /* DrawerLayout object */
                 R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                 R.string.drawer_open,  /* "open drawer" description for accessibility */
                 R.string.drawer_close  /* "close drawer" description for accessibility */
         ) {
             public void onDrawerClosed(View view) {
                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                     getActionBar().setTitle(R.string.app_name);
                     invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                 }
             }
 
             public void onDrawerOpened(View drawerView) {
                 // If the drawer is open the user may want to select a different provider
                 // Before picking the provider, the entry list has to load
                 ListView listView = (ListView) findViewById(R.id.entrylistView);
                 if (listView == null){
                     onBackPressed();
                 }
                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                     getActionBar().setTitle(R.string.drawer_title);
                     invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                 }
             }
 
             @Override
             public void onDrawerSlide(View drawerView, float slideOffset) {
                 super.onDrawerSlide(drawerView, slideOffset);
 
                 ViewGroup l;
                 l = (FrameLayout) findViewById(R.id.articlelist_container);
                 if (l == null)
                     l = (LinearLayout) findViewById(R.id.mainActivityLayout);
                 LinearLayout d = (LinearLayout) findViewById(R.id.layout_panel_drawer);
 
 
                 float offset = slideOffset * d.getWidth();
                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                     l.setX(offset);
                 } else {
                     //lastOffset = (int)offset - lastOffset;
                     //l.offsetLeftAndRight(lastOffset);
                 }
             }
         };
 
         mDrawerLayout.setDrawerListener(mDrawerToggle);
 
         updateDrawerList();
     }
 
     protected void updateDrawerList() {
         ArrayAdapter<RSSFeed> drawerListAdapter = new FeedListAdapter(this, R.layout.feedlist_item, mFeedDataSource.getAll(), R.style.DrawerListItem);
         mDrawerList.setAdapter(drawerListAdapter);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.main, menu);
         return super.onCreateOptionsMenu(menu);
     }
 
     /* Called whenever we call invalidateOptionsMenu() */
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         // If the nav drawer is open, hide action items related to the content view
         //boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
         //menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
         return super.onPrepareOptionsMenu(menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // The action bar home/up action should open or close the drawer.
         // ActionBarDrawerToggle will take care of this.
         if (mDrawerToggle.onOptionsItemSelected(item)) {
             return true;
         }
         // Handle action buttons
         switch(item.getItemId()) {
             case R.id.action_clear_entry_cache:
 
                 new AlertDialog.Builder(this)
                         .setIcon(android.R.drawable.ic_dialog_alert)
                         .setTitle(R.string.action_clear_entry_cache)
                         .setMessage(R.string.action_dlg_msg_clear_entry_cache)
                         .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialog, int which) {
                                 //FIXME This should live in a separate thread and a progres dialog should be shown
                                 List<RSSEntry> entries = mEntryDataSource.getAll();
                                 for (RSSEntry entry : entries) {
                                     mEntryDataSource.delete(entry);
                                 }
                                 Notifications.showMsg(R.string.info_article_cache_cleaned);
                                 feedSelected(-1);
                                 updateDrawerList();
                             }
 
                         })
                         .setNegativeButton(android.R.string.no, null)
                         .show();
 
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
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
         Log.d(TAG,"CONFIGURATION CHANGED");
         super.onConfigurationChanged(newConfig);
         // Pass any configuration change to the drawer toggls
         mDrawerToggle.onConfigurationChanged(newConfig);
     }
 
     // Save UI state changes to the savedInstanceState.
     // This bundle will be passed to onCreate if the process is
     // killed and restarted.
     public void onSaveInstanceState(Bundle savedInstanceState) {
         Log.d(TAG, "SAVE INSTANCE STATE");
         MyApp.getInstance().mMainActivity = null;
 
         //savedInstanceState.putString("MyString", "Welcome back to Android");
 
         if (mOrientation != ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getRotation()) {
             getSupportFragmentManager().beginTransaction()
                     .remove(mArticleListFragment)
                     .remove(mArticleDetailFragment)
                     .commit();
         }
         super.onSaveInstanceState(savedInstanceState);
     }
 
     @Override
     public void onItemSelected(ArticleListAdapter adapter, int entryPosition) {
         Log.d(TAG, "ON ITEM SELECTED");
         ViewPager pager = (ViewPager) findViewById(R.id.mainPager);
         if (pager != null)
             pager.setCurrentItem(2);
         ArticleFragment df = (ArticleFragment) mArticleDetailFragment;
         df.setEntry(adapter, entryPosition);
     }
 
     private class DrawerItemClickListener implements AdapterView.OnItemClickListener {
         @Override
         public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
             ViewPager pager = (ViewPager) findViewById(R.id.mainPager);
             if (pager != null)
                 pager.setCurrentItem(0);
             feedSelected(position);
         }
     }
 
     private class DrawerItemLongClickListener implements AdapterView.OnItemLongClickListener {
         @Override
         public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position, long id) {
             AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
             builder.setMessage(R.string.unsubscribeConfermation).setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener(){
                 @Override
                 public void onClick(DialogInterface dialogInterface, int i) {
                     Log.d(TAG, "Removing item");
                     RSSFeed f = (RSSFeed) mDrawerList.getItemAtPosition(position);
                     mFeedDataSource.delete(f);
                     updateDrawerList();
                     feedSelected(-1);
                 }
             });
             builder.setNegativeButton(android.R.string.no, null);
             builder.setCancelable(true);
             builder.show();
             return true;
         }
     }
     protected void feedSelected(int position) {
         ArticleListFragment elf = (ArticleListFragment) mArticleListFragment;
         if (elf == null){
             Log.d(TAG, "ArticleListFragment instance is null");
             return;
         }
 
         // update selected item and title, then close the drawer
         if (position == -1){
             elf.setRSSFeed(null);
             return;
         }
         RSSFeed feed = (RSSFeed)mDrawerList.getAdapter().getItem(position);
         Log.d(TAG, "Feed: " + feed.title + " clicked!");
         elf.setRSSFeed(feed);
 
         mDrawerList.setItemChecked(position, true);
         mDrawerLayout.closeDrawer(mDrawerPanelLayout);
     }
 
     public void SubscribeToFeed(View v){
         Log.d(TAG, "SUBSCRIBE TO A NEW FEED");
 
         // Shows subscribe to feed dialog
         DialogFragment subscribe = new SubscribeDialog();
         subscribe.show(getSupportFragmentManager(), "SubscribeDialog dialog");
     }
 
     @Override
     protected void onStop() {
         Log.d(TAG, "ON STOP");
         super.onStop();
     }
 
     @Override
     protected void onStart() {
         Log.d(TAG, "ON START");
         super.onStart();
     }
 
     @Override
     protected void onPause() {
         Log.d(TAG,"ON PAUSE");
         super.onPause();
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         Log.d(TAG,"ON DESTROY");
     }
 
      /*@Override
     public void onBackPressed() {
         mDrawerLayout.openDrawer(mDrawerPanelLayout);
     }*/
 }
