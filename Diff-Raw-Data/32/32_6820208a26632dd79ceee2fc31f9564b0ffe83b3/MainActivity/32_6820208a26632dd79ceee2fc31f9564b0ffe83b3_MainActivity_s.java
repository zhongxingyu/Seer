 package com.nickotter.randomname;
 
 
 import com.navdrawer.SimpleSideDrawer;
 import com.nickotter.randomname.SectionsPagerAdapter;
 import com.nickotter.randomname.crudActivities.AddGroup;
 
 import java.util.List;
 import java.util.Random;
 
 import android.content.Context;
 import android.content.Intent;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.hardware.Sensor;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.view.WindowManager.LayoutParams;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.LinearLayout;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 
 public class MainActivity extends SherlockFragmentActivity implements ActionBar.TabListener {
 	
 	final String LOGTAG = "MainActivity";
 
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
 	
 	public SimpleSideDrawer mNav;
 	
 	CRUD databaseCRUD = null;
 	
 	//Shake variables
 	private SensorManager mSensorManager;
 	private ShakeEventListener mSensorListener;
 	
 	int currentGroup = 0;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		Log.v(LOGTAG, "Deleting DATABASE_NAME="+ Sqlite.DATABASE_NAME);
		//this.deleteDatabase(Sqlite.DATABASE_NAME);
 
 		Log.v(LOGTAG, "Initializing CRUD object");
 		databaseCRUD = new CRUD(this);
 		
 		Log.v(LOGTAG, "opening database connection in CRUD object");
 		databaseCRUD.open();
 		
 		Log.v(LOGTAG, "Calling createGroups from onCreate");
 		createGroups();
 		
 		ActionBar bar = getSupportActionBar();
         bar.setDisplayHomeAsUpEnabled(true);
 		
 		mNav = new SimpleSideDrawer(this);
         mNav.setBehindContentView(R.layout.settings_sidedrawer);
         
         Log.v(LOGTAG, "Setting group list adapter");
 		
 		Log.v(LOGTAG, "Loading actionbar");
 		
 		// Set up the action bar.
 		final ActionBar actionBar = getSupportActionBar();
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
         
 		actionBar.setDisplayShowHomeEnabled(true);
         actionBar.setDisplayShowTitleEnabled(true);
         
        
         //Exclusion toggle listener]
         Log.v(LOGTAG, "Intitializing the toggle listeners");
         findViewById(R.id.toggleExclusion).setOnClickListener
         (new OnClickListener() 
         	{
             	@Override 
             	public void onClick(View v) 
             	{
             		Log.v(LOGTAG, "Exclusion toggle selected");
                     @SuppressWarnings("unused")
 					boolean dbcheck;
             		databaseCRUD.toggle_Exculison();
                     ToggleButton exclusion = (ToggleButton) findViewById(R.id.toggleExclusion);
                     exclusion.setChecked(dbcheck = (databaseCRUD.query_Exclusion() != 0));   
                     Log.v(LOGTAG, "\tCurrent db value: " + databaseCRUD.query_Exclusion());
             	};
         	}   
         );
         
         //Verbalizer toggle listener
         findViewById(R.id.toggleVerbalize).setOnClickListener
         (new OnClickListener() 
         	{
             	@Override 
             	public void onClick(View v) 
             	{
             		Log.v(LOGTAG, "Verbal toggle selected");
                     @SuppressWarnings("unused")
 					boolean dbcheck;
             		databaseCRUD.toggle_Verbal();
                     ToggleButton verbal = (ToggleButton) findViewById(R.id.toggleVerbalize);
                     verbal.setChecked(dbcheck = (databaseCRUD.query_Verbal() != 0));
                     Log.v(LOGTAG, "\tCurrent db value: " + databaseCRUD.query_Verbal());
             	};
         	}   
         );
         
         //Shake randomizer toggle listener
         findViewById(R.id.toggleShaker).setOnClickListener
         (new OnClickListener() 
         	{
             	@Override 
             	public void onClick(View v) 
             	{
             		Log.v(LOGTAG, "Shaker toggle selected");
                     @SuppressWarnings("unused")
 					boolean dbcheck;
             		databaseCRUD.toggle_Shake();
                     ToggleButton shaker = (ToggleButton) findViewById(R.id.toggleShaker);
                     shaker.setChecked(dbcheck = (databaseCRUD.query_Shake() != 0));
                     Log.v(LOGTAG, "\tCurrent db value: " + databaseCRUD.query_Shake());
             	};
         	}   
         );
         
         //Group Create Button
         Log.v(LOGTAG, "Enabling Group button listener");
         findViewById(R.id.add_group_btn).setOnClickListener(
 			new OnClickListener() {
 					@Override
 					public void onClick(View v) {
 						Intent i = new Intent(MainActivity.this, AddGroup.class);
 			    		startActivity(i);
 				}
 			}
         );
         
         
         //Shake acceleration listeners (value of 0 is not shaking and greater than 2 is shaking device)
         Log.v(LOGTAG, "Initilization of Shake detection");
         mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
         mSensorListener = new ShakeEventListener();   
 
         mSensorListener.setOnShakeListener(new ShakeEventListener.OnShakeListener() 
         {
         	public void onShake() 
         	{
         		//Randomize function
         	       Log.v(LOGTAG, "Shook Randomizer");
         	       
         	       //String shook = "Device was shaken";
         	       //speakOut(shook);
         	       
         	}
         });
         
         this.loadLists(currentGroup);
 
 		
 		
 	}//END void onCreate
 	
 	private void loadLists(int groupIndex) {
 		
 		// Set up the action bar.
 		final ActionBar actionBar = getSupportActionBar();
 		
 		//ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item, groups);
         ListAdapter adapter = new GroupListAdapter(this, databaseCRUD.query_group());
         
         ListView groupList = (ListView)findViewById(R.id.listView1);
         groupList.removeAllViewsInLayout();
         groupList.setAdapter(adapter);
         
         groupList.setOnItemClickListener(new OnItemClickListener() {
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
             	currentGroup = position;
             	mNav.toggleDrawer();
             	Log.v(LOGTAG, "\n\n\n\tclicked on group index=" + position);
             	//loadLists(currentGroup);
             }
         });
 		
 		// Create the adapter that will return a fragment for each of the three
 		// primary sections of the app.
 		mSectionsPagerAdapter = new SectionsPagerAdapter(
 				getSupportFragmentManager(), getApplicationContext());
 
 		// Set up the ViewPager with the sections adapter.
 		mViewPager = (ViewPager) findViewById(R.id.pager);
 		mViewPager.setAdapter(mSectionsPagerAdapter);
 
 		// When swiping between different sections, select the corresponding
 		// tab. We can also use ActionBar.Tab#select() to do this if we have
 		// a reference to the Tab.
 		mViewPager
 				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
 					@Override
 					public void onPageSelected(int position) {
 						actionBar.setSelectedNavigationItem(position);
 					}
 				});
 
 		List<Group> groups = databaseCRUD.query_group();
 		List<MyList> tempList = databaseCRUD.query_list(groups.get(groupIndex));
 		
 		
 		// For each of the lists, add a tab to the action bar.
 		for(MyList t : tempList) {
 			// Create a tab with text corresponding to the page title defined by
 			// the adapter. Also specify this Activity object, which implements
 			// the TabListener interface, as the callback (listener) for when
 			// this tab is selected.
 			actionBar.addTab(actionBar.newTab()
 					.setText(t.getName())
 					.setTabListener(this));
 		}
 		
 		Log.v(LOGTAG, "\tcurrent tab count" + actionBar.getTabCount());
 		
 		
 	}
 	
 	public boolean onOptionsItemSelected(MenuItem item) 
     {    
        switch (item.getItemId()) 
        {        
           case android.R.id.home:            
         	  mNav.toggleDrawer();       
           default:            
              return super.onOptionsItemSelected(item);    
        }
     }
 
 	/*@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getSupportMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}//END boolean onCreateOptionsMenu*/
 
 	@Override
 	public void onTabSelected(ActionBar.Tab tab,
 			FragmentTransaction fragmentTransaction) {
 		// When the given tab is selected, switch to the corresponding page in
 		// the ViewPager.
 		mViewPager.setCurrentItem(tab.getPosition());
 	}//END void onTabSelected
 
 	@Override
 	public void onTabUnselected(ActionBar.Tab tab,
 			FragmentTransaction fragmentTransaction) {
 	}//END void onTabUnselected
 
 	@Override
 	public void onTabReselected(ActionBar.Tab tab,
 			FragmentTransaction fragmentTransaction) {
 	}//END void onTabReselected
 	
 	@Override
 	protected void onResume() {
 		Log.v(LOGTAG, "onResuem e");
 		
 		super.onResume();
 		
 		//Resume DB
 		databaseCRUD.open();
 		
 		//Resume Shake
 		mSensorManager.registerListener(mSensorListener,
 		        mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
 		        SensorManager.SENSOR_DELAY_UI);
 		
 		Log.v(LOGTAG, "onResume x");
 	}
 	
 	@Override
 	protected void onPause() {
 		Log.v(LOGTAG, "onPause e");
 		
 		super.onPause();
 		
 		//Pause DB
 		if (databaseCRUD != null) {
 			databaseCRUD.close();
 		} else {
 			Log.v(LOGTAG, "\tdatabaseCRUD is null");
 		}
 		
 		//Pause Shake
 	    mSensorManager.unregisterListener(mSensorListener);
 		
 		Log.v(LOGTAG, "onPause x");
 	}
 	
 	public void createGroups() {
 		
 		// init shake, exclusion, verbal to false
 		databaseCRUD.initExtraFunctions();
 		
 		Log.v(LOGTAG, "createGroups e");
 		
 		Log.v(LOGTAG, "\tcreating groups start");
 		Group g1 = new Group("CS480");
 		Group g2 = new Group("CS481");
 		Log.v(LOGTAG, "\tcreating groups end");
 		
 		MyList l1 = new MyList(1, g1.getID(), "List 1");
 		MyList l2 = new MyList(2, g1.getID(), "List 2");
 		MyList l3 = new MyList(3, g2.getID(), "List 3");
 		
 		Log.v(LOGTAG, "Adding Groups");
 		databaseCRUD.add_group(g1);
 		Log.v(LOGTAG, "Group 1 now has id="+ g1.getID());
 		
 		databaseCRUD.add_group(g2);
 		Log.v(LOGTAG, "Group 2 now has id="+ g2.getID());
 		
 		Log.v(LOGTAG, "Adding Lists");
 		databaseCRUD.add_list(g1, l1);
 		databaseCRUD.add_list(g1, l2);
 		databaseCRUD.add_list(g2, l3);
 		
 		Log.v(LOGTAG, "Adding Items");
 		Item i1 = new Item(1, l1.getID(), "List 1 - Item 1");
 		Item i2 = new Item(2, l1.getID(), "List 1 - Item 2");
 		Item i3 = new Item(3, l2.getID(), "List 2 - Item 3");
 		Item i4 = new Item(4, l2.getID(), "List 2 - Item 4");
 		Item i5 = new Item(5, l3.getID(), "List 3 - Item 5");
 		
 		databaseCRUD.add_item(l1, i1);
 		databaseCRUD.add_item(l1, i2);
 		databaseCRUD.add_item(l2, i3);
 		databaseCRUD.add_item(l2, i4);
 		databaseCRUD.add_item(l3, i5);
 		
 		
 		Log.v(LOGTAG, "Query Group");
 		List<MyList> tempList = databaseCRUD.query_list(g1);
 		
 		Log.v(LOGTAG, "\t There are "+ tempList.size() + " items in temp");
 		
 		for(MyList t : tempList) {
 			Log.v(LOGTAG, t.getName() + " is a part of group " + t.getGroupID());
 			
 			Log.v(LOGTAG, "\t\t querying for list items");
 			List<Item> tempItem = databaseCRUD.query_item(t);
 			
 			Log.v(LOGTAG, "\t\t There are " + tempItem.size() + " items in the list");
 			for(Item tI : tempItem) {
 				Log.v(LOGTAG, "\t\t\t" + "item name="+ tI.getName());
 			}
 		}
 		
 		
 		Log.v(LOGTAG, "createGroups x");
 		
 		
 	}
 
 }//END class MainActivity
 
