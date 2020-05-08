 
 package se.mah.kd330a.project.framework;
 
 
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import net.fortuna.ical4j.data.ParserException;
 import se.mah.kd330a.project.R;
 import se.mah.kd330a.project.faq.FragmentFaq;
 import se.mah.kd330a.project.find.FragmentFind;
 import se.mah.kd330a.project.help.FragmentHelp;
 import se.mah.kd330a.project.home.FragmentHome;
 import se.mah.kd330a.project.home.data.RSSFeed;
 import se.mah.kd330a.project.itsl.FragmentITSL;
 import se.mah.kd330a.project.schedule.data.KronoxCalendar;
 import se.mah.kd330a.project.schedule.data.KronoxReader;
 import se.mah.kd330a.project.schedule.view.FragmentScheduleWeekPager;
 import se.mah.kd330a.project.settings.view.SettingsActivity;
 
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.content.res.TypedArray;
 import android.os.Bundle;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.view.GravityCompat;
 import android.support.v4.widget.DrawerLayout;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 
 public class MainActivity extends FragmentActivity{
     private DrawerLayout mDrawerLayout;
     private ListView mDrawerList;
     private ActionBarDrawerToggle mDrawerToggle;
 
     private CharSequence mDrawerTitle;
     private CharSequence mTitle;
     private String[] mMenuTitles;
     private TypedArray mMenuIcons;
     private TypedArray mMenuColors;
     public RSSFeed newsFeed;
     
     private final int HOME = 0;
 	private final int SCHEDULE = 1;
 	private final int ITSL = 2;
 	private final int FIND = 3;
 	private final int FAQ = 4;
 	private final int HELP = 5;
 	
 	
 	
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         try {
 			KronoxCalendar.createCalendar(KronoxReader
 					.getFile(getApplicationContext()));
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ParserException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
         
         setContentView(R.layout.activity_main);
             
         
      	
         mTitle = mDrawerTitle = getTitle();
         mMenuTitles = getResources().getStringArray(R.array.menu_texts);
         mMenuIcons = getResources().obtainTypedArray(R.array.menu_icons);
        //LH commented out for merge mMenuColors = getResources().obtainTypedArray(R.array.menu_colors);
         mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
         mDrawerList = (ListView) findViewById(R.id.left_drawer);
 
         // set a custom shadow that overlays the main content when the drawer opens
         mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
         
         mDrawerList.setSelector(R.drawable.menu_selector);
         // set up the drawer's list view with items and click listener
         mDrawerList.setAdapter(new MenuAdapter(this,
                 R.layout.drawer_list_item, mMenuTitles, mMenuIcons, mMenuColors));
         mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
 
         // enable ActionBar app icon to behave as action to toggle nav drawer
         getActionBar().setDisplayHomeAsUpEnabled(true);
         getActionBar().setHomeButtonEnabled(true);
 
         // ActionBarDrawerToggle ties together the the proper interactions
         // between the sliding drawer and the action bar app icon
         mDrawerToggle = new ActionBarDrawerToggle(
                 this,                  /* host Activity */
                 mDrawerLayout,         /* DrawerLayout object */
                 R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                 R.string.drawer_open,  /* "open drawer" description for accessibility */
                 R.string.drawer_close  /* "close drawer" description for accessibility */
                 ) {
             public void onDrawerClosed(View view) {
                 getActionBar().setTitle(mTitle);
                 invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
             }
 
             public void onDrawerOpened(View drawerView) {
                 getActionBar().setTitle(mDrawerTitle);
                 invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
             }
         };
         mDrawerLayout.setDrawerListener(mDrawerToggle);
 
         if (savedInstanceState == null) {
             selectItem(0);
         }
     }
     
     public RSSFeed getRssNewsFeed() {
     	return newsFeed;
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main, menu);
         return super.onCreateOptionsMenu(menu);
     }
 
     /* Called whenever we call invalidateOptionsMenu() */
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
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
         case R.id.action_help:
             Intent intent = new Intent(this, SettingsActivity.class);
             startActivity(intent);
             return true;
         default:
             return super.onOptionsItemSelected(item);
         }
     }
 
     /* The click listener for ListView in the navigation drawer */
     private class DrawerItemClickListener implements ListView.OnItemClickListener {
         @Override
         public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
             selectItem(position);
         }
     }
 
     public void selectItem(int position) {
         // update the main content by replacing fragments
     	android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
     	Fragment fragment;
     	switch (position) {
 		case HOME:	
 			fragment = new FragmentHome();
 			break;
 		case SCHEDULE:
 			fragment = new FragmentScheduleWeekPager();
 			break;
 		case ITSL:
 			fragment = new FragmentITSL();
 			break;
 		case FIND:
 			fragment = new FragmentFind();
 			break;
 		case FAQ:
 			fragment = new FragmentFaq();
 			break;
 		case HELP:
 			fragment = new FragmentHelp();
 			break;
 		default:	
 			fragment = new FragmentHome();
 		}
     	fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
 
         // update selected item and title, then close the drawer
         mDrawerList.setItemChecked(position, true);
         setTitle(mMenuTitles[position]);
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
         // Pass any configuration change to the drawer toggls
         mDrawerToggle.onConfigurationChanged(newConfig);
     }
     
     public void toSchedule(View view) {
 		selectItem(this.SCHEDULE);
 	}
     
     public void toITSL(View view) {
 		selectItem(this.ITSL);
 	}
     
     public void toFind(View view) {
 		selectItem(this.FIND);
 	}
 
 
     
     
 
     
 }
