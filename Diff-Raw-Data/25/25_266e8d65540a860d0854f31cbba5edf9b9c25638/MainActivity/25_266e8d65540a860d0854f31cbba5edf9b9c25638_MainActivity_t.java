 package com.cocacola.climateambassador.ui;
 
 import android.app.ActionBar;
 import android.app.SearchManager;
 import android.content.ActivityNotFoundException;
 import android.content.Context;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.view.GravityCompat;
 import android.support.v4.widget.DrawerLayout;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.SearchView;
 import android.widget.Toast;
 
 import com.cocacola.climateambassador.AppPackageFileWriter;
 import com.cocacola.climateambassador.DocumentViewerDelegate;
 import com.cocacola.climateambassador.R;
 import com.cocacola.climateambassador.adapters.MenuListAdapter;
 import com.cocacola.climateambassador.models.Case;
 import com.cocacola.climateambassador.util.JsonAssetsHelper;
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.inject.Inject;
 
 import butterknife.OnClick;
 
 public class MainActivity extends CaActivity implements SearchView.OnQueryTextListener {
 
     private final int INGREDIENTS_CASE_POS = 0;
     private final int BUSINESS_CASE_POS = 1;
     private final int KEY_INTERVENTIONS_POS = 2;
     private final int ENGAGING_SUPPLIERS_POS = 3;
     private final int SUPPLIERS_OVERVIEW_POS = 4;
     private final int SUPPLIER_GUIDE_POS = 5;
 
 
     public String[] mDrawerOptions = { "Ingredients Case" , "Supply Chain Implementation", "For Suppliers"};
     public ListView mDrawerList;
     public DrawerLayout mDrawerLayout;
     private MenuListAdapter mMenuAdapter;
     private ActionBarDrawerToggle mDrawerToggle;
     int[] icon;
 
     private SearchView mSearchView;
 
     @Inject
     DocumentViewerDelegate mDocumentManager;
     @Inject
     AppPackageFileWriter mAppPackageFileWriter;
 
     @Inject
     JsonAssetsHelper mJsonAssetsHelper;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         //Set up the Navigation Drawer
         mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
         mDrawerList = (ListView) findViewById(R.id.left_drawer);
         mDrawerList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
         // set a custom shadow that overlays the main content when the drawer opens
         mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
         // set up the drawer's list view with items and click listener
 
 
         //Removed Subtitle
         icon = new int[]{0, 0, 0, 0};
 
         mMenuAdapter = new MenuListAdapter(this, mDrawerOptions, icon);
 
         mDrawerList.setAdapter(mMenuAdapter);
 
         mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
 
         getActionBar().setDisplayShowTitleEnabled(true);
         getActionBar().setDisplayHomeAsUpEnabled(true);
         getActionBar().setHomeButtonEnabled(true);
 
         mDrawerToggle = new ActionBarDrawerToggle(
                 this,                  /* host Activity */
                 mDrawerLayout,         /* DrawerLayout object */
                 R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                 R.string.drawer_open,  /* "open drawer" description for accessibility */
                 R.string.drawer_close  /* "close drawer" description for accessibility */
         ) {
             public void onDrawerClosed(View view) {
                 // getSupportActionBar().setTitle(mTitle);
                 invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
             }
 
             public void onDrawerOpened(View drawerView) {
                 // getSupportActionBar().setTitle(mDrawerTitle);
                 invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
             }
         };
 
         mDrawerLayout.setDrawerListener(mDrawerToggle);
 
         setUpHomeScreen();
 
         mAppPackageFileWriter.writeAssetsToPackageDir();
 
         File filesDir = getFilesDir();
         String[] list = filesDir.list();
 
         Log.i("list.length=%s", list.length);
 
         for (String filename : list) {
             Log.i("filename=%s", filename);
         }
 
     }
 
     private void setUpHomeScreen() {
         MainFragment fragment = new MainFragment();
         getFragmentManager().beginTransaction().replace(R.id.main_content, fragment).commit();
     }
 
     @OnClick(R.id.home_btn_internal)
     public void startSectionActivity() {
         try {
             mDocumentManager.startPdfViewerActivity("resume.pdf", this);
         } catch (ActivityNotFoundException e) {
             // TODO Launch an intent to download in Play Store
             Log.e(e, "No Activity for Viewing PDFs");
         }
     }
 
     private class DrawerItemClickListener implements ListView.OnItemClickListener {
         @Override
         public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
             onDrawerItemClick(position);
         }
     }
 
     private void onDrawerItemClick(int position) {
 
         // TODO Use switch instead of if/elses
         if (position == INGREDIENTS_CASE_POS) {
 
            Case ingredientsCase = null;

             try {
                ingredientsCase = mJsonAssetsHelper.parseCaseFromJsonFile("ingredients.json");
             } catch (IOException e) {
                 Toast.makeText(this, "Failed to Get Ingredients Case", Toast.LENGTH_SHORT);
             }
 
             CaseFragment fragment = CaseFragment.newInstance(ingredientsCase);
 
             getFragmentManager().beginTransaction().replace(R.id.main_content, fragment).commit();
 
             getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
             getActionBar().setDisplayShowTitleEnabled(true);
             getActionBar().setTitle(getResources().getString(R.string.for_suppliers));
 
 
         } else if (position == BUSINESS_CASE_POS) {
 
             ValueChainModule fragment = ValueChainModule.newInstance();
             getFragmentManager().beginTransaction().replace(R.id.main_content, fragment).commit();
 
             mDrawerLayout.closeDrawer(mDrawerList);
 
             getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
             getActionBar().setDisplayShowTitleEnabled(true);
             getActionBar().setTitle(getResources().getString(R.string.for_suppliers));
 
 
         } else if (position == KEY_INTERVENTIONS_POS) {
 
             SupplierOverview fragment = SupplierOverview.newInstance();
             getFragmentManager().beginTransaction().replace(R.id.main_content, fragment).commit();
 
             mDrawerLayout.closeDrawer(mDrawerList);
 
             getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
             getActionBar().setDisplayShowTitleEnabled(true);
             getActionBar().setTitle(getResources().getString(R.string.for_suppliers));
 
 
         } else if (position == ENGAGING_SUPPLIERS_POS) {
 
 
         } else if (position == SUPPLIERS_OVERVIEW_POS) {
 
 
         } else if (position == SUPPLIER_GUIDE_POS) {
 
 
         } else {
             //do nothing
             mDrawerLayout.closeDrawer(mDrawerList);
         }
 
 
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
 
         getMenuInflater().inflate(R.menu.main, menu);
         SearchManager searchManager = (SearchManager) this.getSystemService(Context.SEARCH_SERVICE);
         mSearchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
         mSearchView.setSearchableInfo(searchManager.getSearchableInfo(this.getComponentName()));
         mSearchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
         mSearchView.setOnQueryTextListener(this);
         mSearchView.setQueryHint("Search");
 
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onQueryTextSubmit(String query) {
         //TODO: Handle Search View TextSubmit
         return false;
     }
 
     @Override
     public boolean onQueryTextChange(String newText) {
         return false;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
 
         if (item.getItemId() == android.R.id.home) {
 
             if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
                 mDrawerLayout.closeDrawer(mDrawerList);
             } else {
                 mDrawerLayout.openDrawer(mDrawerList);
             }
         } else {
             // do nothing
         }
         return super.onOptionsItemSelected(item);
     }
 
 
     @Override
     protected void onPostCreate(Bundle savedInstanceState) {
         super.onPostCreate(savedInstanceState);
         // Sync the toggle state after onRestoreInstanceState has occurred.
         mDrawerToggle.syncState();
     }
 
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
         // Pass any configuration change to the drawer toggles
         mDrawerToggle.onConfigurationChanged(newConfig);
     }
 
 
 }
