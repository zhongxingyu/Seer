 package no.whg.whirc.activities;
 
 import jerklib.ConnectionManager;
 import jerklib.Session;
 import no.whg.whirc.R;
 import no.whg.whirc.adapters.ConversationPagerAdapter;
 import no.whg.whirc.helpers.ConnectionService;
 import no.whg.whirc.helpers.ConnectionServiceBinder;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.view.GravityCompat;
 import android.support.v4.view.ViewPager;
 import android.support.v4.widget.DrawerLayout;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 public class MainActivity extends FragmentActivity implements ServiceConnection {
     private DrawerLayout mDrawerLayoutLeft;
     private DrawerLayout mDrawerLayoutRight;
     private ListView mDrawerListRight;
     private ListView mDrawerListLeft;
     private ActionBarDrawerToggle mDrawerToggleLeft;
 
     private CharSequence mDrawerTitleLeft;
     private CharSequence mTitle;
     
     private ConnectionManager manager;
     
     public ConnectionService cService;
     private boolean mBound = false;
     
     private Session s = null;
 
     /**
      * The {@link android.support.v4.view.PagerAdapter} that will provide
      * fragments for each of the sections. We use a
      * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
      * will keep every loaded fragment in memory. If this becomes too memory
      * intensive, it may be best to switch to a
      * {@link android.support.v4.app.FragmentStatePagerAdapter}.
      */
     ConversationPagerAdapter mConversationPagerAdapter;
 
     /**
      * The {@link ViewPager} that will host the section contents.
      */
     ViewPager mViewPager;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         
         
         // Set placeholder title
         mTitle = mDrawerTitleLeft = getTitle();
         mDrawerLayoutLeft = (DrawerLayout) findViewById(R.id.drawer_layout);
         mDrawerLayoutRight = (DrawerLayout) findViewById(R.id.drawer_layout);
         mDrawerListLeft = (ListView) findViewById(R.id.left_drawer);
         mDrawerListRight = (ListView) findViewById(R.id.right_drawer);
 
         // set shadow to overlay main content when we pull out drawer
         mDrawerLayoutLeft.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
         mDrawerLayoutRight.setDrawerShadow(R.drawable.drawer_shadow_right, GravityCompat.END);
         // set up the drawers  list view with items and click listener
         mDrawerListLeft.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, new String[]{"List 1", "List 2", "List 3", "List 4"}));
         mDrawerListLeft.setOnItemClickListener(new DrawerItemClickListener());
         mDrawerListRight.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, new String[]{"lol 1", "lol 2", "lol 3", "lol 4"}));
         mDrawerListRight.setOnItemClickListener(new DrawerItemClickListener());
 
         // enable actionbar up button
         getActionBar().setDisplayHomeAsUpEnabled(true);
         getActionBar().setHomeButtonEnabled(true);
 
         // actionbardrawertoggle ties together the proper interactions between the sliding drawer and the action bar app icon
         mDrawerToggleLeft = new ActionBarDrawerToggle(
                 this,
                 mDrawerLayoutLeft,
                 R.drawable.ic_drawer,
                 R.string.drawer_open,
                 R.string.drawer_close
         ) {
             public void onDrawerClosed(View view) {
                 getActionBar().setTitle(mTitle);
                 invalidateOptionsMenu();
             }
         };
         mDrawerLayoutLeft.setDrawerListener(mDrawerToggleLeft);
 
         if (savedInstanceState == null) {
             selectItem(0);
         }
 
 
 
         
 
 
         // Set up the ViewPager with the sections adapter.
         mViewPager = (ViewPager) findViewById(R.id.pager);
         mViewPager.setAdapter(mConversationPagerAdapter);
         
        cService = null;
        Intent intent = new Intent(this, ConnectionService.class);
        startService(intent);
     }
 
 
 	/* (non-Javadoc)
 	 * @see android.support.v4.app.FragmentActivity#onStart()
 	 */
 	@Override
 	protected void onStart() {
 		// TODO Auto-generated method stub
 		super.onStart();
 		Intent intent = new Intent(this, ConnectionService.class);
 	    bindService(intent, this, Context.BIND_ABOVE_CLIENT);
 	}
 
 
 
 	/* (non-Javadoc)
 	 * @see android.support.v4.app.FragmentActivity#onStop()
 	 */
 	@Override
 	protected void onStop() {
 		// TODO Auto-generated method stub
 		super.onStop();
 		unbindService(this);
 	}
 
 
 
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         boolean drawerOpen = mDrawerLayoutLeft.isDrawerOpen(mDrawerListLeft);
         // Since we are (probably) going to move the settings button to the navigation drawer while it's open,
         //  lets hide the settings button when we open the drawer
         menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
         return super.onPrepareOptionsMenu(menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         if (mDrawerToggleLeft.onOptionsItemSelected(item)) {
             return true;
         }
         // tell program what to do when you press a certain actionbar button, e.g. settings button
         switch (item.getItemId()) {
             case R.id.action_settings:
                 startActivity(new Intent(this, SettingsActivity.class));
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     private class DrawerItemClickListener implements ListView.OnItemClickListener {
         @Override
         public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
             selectItem(position);
         }
     }
 
     private void selectItem(int position) {
         /* TODO Selection of menu item logic goes here, since we use sections we probably need an array/list of fragments
           for every network, while when we select e.g. "options" we need to start a new activity. */
         // FragmentManager, beginTransaction, setItemChecked, setTitle, mDrawerLayout.closeDrawer etc goes here.
         // Bundle with args, ARG_FRAGMENT_NUMBER etc etc
     }
 
     @Override
     public void setTitle(CharSequence title) {
         mTitle = title;
         getActionBar().setTitle(mTitle);
     }
 
     @Override
     protected void onPostCreate(Bundle savedInstanceState) {
         super.onPostCreate(savedInstanceState);
         mDrawerToggleLeft.syncState();
     }
 
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
         mDrawerToggleLeft.onConfigurationChanged(newConfig);
     }
 
 
 	@Override
 	public void onServiceConnected(ComponentName name, IBinder binder) {
		cService = ((ConnectionServiceBinder) binder).getService();
		s = cService.getSessions().get(0);
 
 
 		// Create the adapter that will return a fragment for each of the three
         // primary sections of the app.
		if (s.isConnected()) {
 			mConversationPagerAdapter = new ConversationPagerAdapter(getSupportFragmentManager(), s);
 		} else {
 			Log.e("MainActivity", "ConversationPagerAdapter not started because no active connection at time of call");
 		}
 	}
 
 	@Override
 	public void onServiceDisconnected(ComponentName name) {
 		cService = null;
 		
 	}
 	
 	public ConnectionService getConnectionServiceObject() {
 		return cService;
 	}
 }
