 package edu.southern;
 
 import com.slidingmenu.lib.SlidingMenu;
 import com.slidingmenu.lib.app.SlidingFragmentActivity;
 
 import android.os.Bundle;
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.Fragment;
 import android.app.FragmentManager;
 import android.app.FragmentTransaction;
 import android.app.ListFragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import edu.southern.R;
 import edu.southern.resources.*;
 
 // Inherit from Sliding Fragment
 public class HomeScreen extends SlidingFragmentActivity {
 
 	private FragmentManager fragmentManager = getFragmentManager();
 	private FragmentTransaction fragmentTransaction;
 		
 	  @Override
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    setContentView(R.layout.activity_home_screen);
 	    // copy files to device
 	transferAssetFiles();
 	// Initialize the engine and store it on the application
 	initiallizeBibleEngine();
 	
 	// Bind navigation fragment to the SlidingMenu Drawer -- set it as the Behind View
 	setBehindContentView(R.layout.fragment_nav_drawer);
 	
 	// TODO -- Nathanael Beisiegel
 	//FragmentTransaction t = this.getFragmentManager().beginTransaction();
 	//mFrag = new ListFragment();
 	//t.replace(R.id.menu_frame, mFrag);
 	//t.commit();
 	
 	// Set attributes of the menu 
 	SlidingMenu sm = getSlidingMenu();
 	sm.setShadowWidthRes(R.dimen.shadow_width);
 	//sm.setShadowDrawable(R.drawable.shadow);
 	sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
 	sm.setFadeDegree(0.35f);
 	sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
 	getActionBar().setDisplayHomeAsUpEnabled(true);
 	
 	
 	// Add Home fragment to view group as default view
 	    fragmentTransaction = fragmentManager.beginTransaction();
 	    Home homeFragment = new Home();
 	    fragmentTransaction.add(R.id.homeFragmentContainer, homeFragment);
 	    fragmentTransaction.commit();
 	    
 	}
 	  
 	  // Inflate Actionbar with Menu items in primary_nav_menu.xml
 	  @Override
 	  public boolean onCreateOptionsMenu(Menu menu) {
 		// set display attributes of the action bar so that we can populate it with our own layouts
	getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME);
	// load home action bar layout as the default
 		getActionBar().setCustomView(R.layout.actionbar_home);
 	    return true;
 	  }
 	  
   
   // Toggle Drawer on Up button in action bar
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			toggle(); // Show / Hide Sliding Menu Fragment
 			break;
 		}
 		return super.onOptionsItemSelected(item);
 	}
   
   // on click handler for navigation buttons in the drawer
   public void onDrawerItemSelection(View v) {
 	  
 	  //v.setBackgroundResource(android.R.drawable.list_selector_background);
 	  
 	  fragmentTransaction = fragmentManager.beginTransaction();
 	  ActionBar bar = getActionBar();
 	  
 	  // Switch to appropriate fragment
 	  // and swap out views in the action bar
 	  switch (v.getId()) {
 		  case R.id.HomeButton:
 			  Home homeFragment = new Home();
 			  fragmentTransaction.replace(R.id.homeFragmentContainer, homeFragment);
 			  bar.setCustomView(R.layout.actionbar_home);
 			  break;
 		  case R.id.BibleButton:
 			  Bible bibleFragment = new Bible();
 			  fragmentTransaction.replace(R.id.homeFragmentContainer, bibleFragment);
			  bar.setCustomView(R.layout.actionbar_reading);
 			  break;
 		  case R.id.BookmarksButton:
 			  Bookmarks bookmarkFragment = new Bookmarks();
 			  fragmentTransaction.replace(R.id.homeFragmentContainer, bookmarkFragment);
 			  bar.setCustomView(R.layout.actionbar_bookmarks);
 			  break;
 
 		  case R.id.SettingsButton:
 			  Settings settingsFragment = new Settings();
 			  fragmentTransaction.replace(R.id.homeFragmentContainer, settingsFragment);
 			  bar.setCustomView(R.layout.actionbar_settings);
 			  break;
 			  
 		  case R.id.SearchButton:
 			  Search searchFragment = new Search();
 			  fragmentTransaction.replace(R.id.homeFragmentContainer, searchFragment);
 			  bar.setCustomView(R.layout.actionbar_search);
 			  break;			  
 			  
 		  default:
 			  break;
 	  }
 	  fragmentTransaction.commit(); // switch fragments
 	  getSlidingMenu().toggle(); // hide the drawer
   }
   
   // click handler for action bar buttons
   public void onActionBarButtonClick(View v){
 	  switch (v.getId()) {
 	  case R.id.ActionBarHome:
 		  break;
 		  
 	  case R.id.ActionBarBookmarks:
 		  break;
 		  
	  case R.id.ActionBarReading:
 		  break;
 		  
 	  case R.id.ActionBarSearch:
 		  break;
 		  
 	  case R.id.ActionBarSettings:
 		  break;
 	  default:
 		  break;
   }
 	  return;
   }
   
   protected void initiallizeBibleEngine(){
 	  // create a bible engine and start the various aspects of it
 	  CBibleEngine engine = new CBibleEngine();
 	  String DATA_SOURCE = "data/data/edu.southern/lighthouse/";
 	  engine.StartEngine(DATA_SOURCE, "KJV");
 	  engine.StartLexiconEngine(DATA_SOURCE);
 	  engine.StartMarginEngine(DATA_SOURCE);
 	  // store the app on the application class
 	  BibleApp app = (BibleApp)getApplication();
 	  app.SetEngine(engine);
 	  /*
 	   * IMPORTANT
 	   * In order to get the Bible Engine, use these lines
 	   * BibleApp app = (BibleApp)getApplication();
 	   * CBibleEngine BibleEngine = app.GetEngine();
 	   */
   }
   
   protected void transferAssetFiles(){
 	  // copy the asset files necessary for the bible engine to the device
 	  BEngineFileMover fileMover = new BEngineFileMover(getApplicationContext());
 	  fileMover.copyDataFiles();
   }
   
 }
