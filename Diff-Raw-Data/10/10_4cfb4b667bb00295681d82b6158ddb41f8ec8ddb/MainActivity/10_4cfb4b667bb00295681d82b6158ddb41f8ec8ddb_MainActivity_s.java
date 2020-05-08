 package edu.cui.wineapp;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.database.sqlite.SQLiteException;
 import android.graphics.Point;
 import android.os.Bundle;
 import android.os.StrictMode;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.widget.DrawerLayout;
 import android.util.Log;
 import android.view.Display;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.SearchView;
 
 import java.util.ArrayList;
 
 public class MainActivity extends ListActivity implements SearchView.OnQueryTextListener,
         SearchView.OnCloseListener {
 
 
     public final static String EXTRA_MESSAGE = "com.cui.wineapp.MESSAGE";
     ArrayList<Wine> myWineList;
     ArrayList<String> wineNames = new ArrayList<String>();
     private ListView mListView;
     private ArrayAdapter<String> myAdapter;
     private DrawerLayout mDrawerLayout;
     private ListView mDrawerList;
     private ActionBarDrawerToggle mDrawerToggle;
     private CharSequence mDrawerTitle;
     private CharSequence mTitle;
     private ArrayList<String> mPlanetTitles = new ArrayList<String>();
 
     @Override
     public void onListItemClick(ListView l, View v, int position, long id) {
         Wine wineToPass = myWineList.get(position);
         Intent i = new Intent(this, WineInfo.class);
         Bundle bundle2 = new Bundle();
         bundle2.putLong("passedWine", wineToPass.getId());
         i.putExtras(bundle2);
         startActivity(i);
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
         StrictMode.setThreadPolicy(policy);
 
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         mTitle = mDrawerTitle = getTitle();
         mPlanetTitles.add("Test1");
         mPlanetTitles.add("Test2");
         mPlanetTitles.add("Test3");
 
         mTitle = mDrawerTitle = getTitle();
         mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
         mDrawerList = (ListView) findViewById(R.id.left_drawer);
         // set a custom shadow that overlays the main content when the drawer opens
         // set up the drawer's list view with items and click listener
         mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                 R.layout.drawer_list_item, mPlanetTitles));
 
 
         SearchView searchView = (SearchView) findViewById(R.id.searchView1);
         searchView.setIconifiedByDefault(false);
         searchView.setOnQueryTextListener(this);
         searchView.setOnCloseListener(this);
         mListView = (ListView) findViewById(android.R.id.list);
         mListView.setAdapter(myAdapter);
 
         getActionBar().setDisplayHomeAsUpEnabled(true);
         getActionBar().setHomeButtonEnabled(true);
 
         mDrawerToggle = new ActionBarDrawerToggle(
                 this,                  /* host Activity */
                 mDrawerLayout,         /* DrawerLayout object */
                 R.drawable.ic_launcher,  /* nav drawer image to replace 'Up' caret */
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
             //selectItem(0);
         }
 
         Log.i("Inside_OnCreate", "Basic Info");
 
 
         //	setListAdapter(myAdapter);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
     @Override
     public boolean onClose() {
         // TODO Auto-generated method stub
         return false;
     }
 
     @Override
     public boolean onQueryTextChange(String parseText) throws SQLiteException {
         if (parseText.length() >= 3) {
 
             myWineList = new ArrayList<Wine>();
             wineNames = new ArrayList<String>();
 
 
             Log.i("Inside_QTChange", "Text>=3");
 
             SearchView searchView = (SearchView) findViewById(R.id.searchView1);
             searchView.setIconifiedByDefault(false);
             searchView.setOnQueryTextListener(this);
             searchView.setOnCloseListener(this);
             mListView = (ListView) findViewById(android.R.id.list);
 
 
             WineManager wManager = new WineManager(this);
 
             try {
                 Log.i("Inside_QTChange", "TRY");
                 myWineList = wManager.getWineByName(parseText);
 
                 for (Wine currWine : myWineList) {
                     Log.i("PARSINGWINES", currWine.getName());
                     wineNames.add(currWine.getName());
                 }
 
                 for (String currWine : wineNames) {
                     Log.i("PARSNG_WINSZ", currWine);
                 }
 
                 myAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, wineNames);
                 mListView.setAdapter(myAdapter);
                 myAdapter.notifyDataSetChanged();
 
             } catch (SQLiteException e) {
                 Log.i("Inside_QTChange", "CATCH");
                 myWineList = wManager.downloadWineByName(parseText);
 
                 for (Wine currWine : myWineList) {
                     wineNames.add(currWine.getName());
                 }
 
                 myAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, wineNames);
                 mListView.setAdapter(myAdapter);
                 myAdapter.notifyDataSetChanged();
             }
             return true;
         }
 
         return false;
     }
 
     @Override
     public boolean onQueryTextSubmit(String arg0) {
         wineNames = new ArrayList<String>();
         myWineList = new ArrayList<Wine>();
 
 
         Log.i("Inside_QTChange", "Text>=3");
 
         SearchView searchView = (SearchView) findViewById(R.id.searchView1);
         searchView.setIconifiedByDefault(false);
         searchView.setOnQueryTextListener(this);
         searchView.setOnCloseListener(this);
         mListView = (ListView) findViewById(android.R.id.list);
 

         WineManager wManager = new WineManager(this);
         myWineList = wManager.downloadWineByName(arg0);
 

         for (Wine currWine : myWineList) {
            if (currWine != null) wineNames.add(currWine.getName());
         }
 
         myAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, wineNames);
         mListView.setAdapter(myAdapter);
         myAdapter.notifyDataSetChanged();
        return false;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // The action bar home/up action should open or close the drawer.
         // ActionBarDrawerToggle will take care of this.
         if (mDrawerToggle.onOptionsItemSelected(item)) {
             return true;
         }
         // Handle action buttons
         switch (item.getItemId()) {
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     private int getSearchViewX() {
         Display display = getWindowManager().getDefaultDisplay();
         Point size = new Point();
         display.getSize(size);
 
         return size.x;
     }
 
     private int getSearchViewY() {
         Display display = getWindowManager().getDefaultDisplay();
         Point size = new Point();
         display.getSize(size);
 
         return size.y;
     }
 
 }
