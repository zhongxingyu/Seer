 package com.rowan.ieee.sac2014;
 
 import android.app.FragmentManager;
 import android.app.SearchManager;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.view.GravityCompat;
 import android.support.v4.widget.DrawerLayout;
 import android.support.v7.app.ActionBarActivity;
 import android.support.v7.app.ActionBar;
 import android.support.v4.app.Fragment;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.os.Build;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import java.util.Locale;
 
 public class MainActivity extends ActionBarActivity {
     private DrawerLayout mDrawerLayout;
     private ListView mDrawerList;
     private ActionBarDrawerToggle mDrawerToggle;
 
     private CharSequence mDrawerTitle;
     private CharSequence mTitle;
     private String[] mPlanetTitles;
 
     WebView myWebView;
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         mTitle = mDrawerTitle = getTitle();
         mPlanetTitles = getResources().getStringArray(R.array.planets_array);
         mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
         mDrawerList = (ListView) findViewById(R.id.left_drawer);
 
         // set a custom shadow that overlays the main content when the drawer opens
         mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
         // set up the drawer's list view with items and click listener
         mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                 R.layout.drawer_list_item, mPlanetTitles));
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
 
         //Establish the WebView
         myWebView = (WebView) findViewById(R.id.webview);
         WebSettings webSettings = myWebView.getSettings();
         webSettings.setJavaScriptEnabled(true);
         myWebView.setWebViewClient(new WebViewClient());
 
         //Load the application
         //myWebView.loadUrl("http://www.rowan.edu/clubs/ieee/a/");
         //@TODO Fix WebView so it loads, then remove this command and use onSelectItem code instead
         myWebView.loadUrl("http://www.example.com");
     }
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         // Check if the key event was the Back button and if there's history
         if ((keyCode == KeyEvent.KEYCODE_BACK) && myWebView.canGoBack()) {
             myWebView.goBack();
             return true;
         }
         // If it wasn't the Back key or there's no web page history, bubble up to the default
         // system behavior (probably exit the activity)
         return super.onKeyDown(keyCode, event);
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
         // If the nav drawer is open, hide action items related to the content view
         boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
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
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     /* The click listner for ListView in the navigation drawer */
     private class DrawerItemClickListener implements ListView.OnItemClickListener {
         @Override
         public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
             selectItem(position);
         }
     }
 
     private void selectItem(int position) {
         // update the main content by replacing fragments
         android.app.Fragment fragment = new PlanetFragment();
         Bundle args = new Bundle();
         args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
         fragment.setArguments(args);
 
         FragmentManager fragmentManager = getFragmentManager();
         fragmentManager.beginTransaction().replace(R.id.webview, fragment).commit();
 
         // update selected item and title, then close the drawer
         mDrawerList.setItemChecked(position, true);
         //@TODO Fomrat Strings
         mDrawerLayout.closeDrawer(mDrawerList);
         //@TODO Load new webpage
 
         //@TODO Load new webpage
         try {
             //Context
             if(true) {
                 mPlanetTitles[0] = "Now";
             }
 
             if(true) /*Registered*/
                 mPlanetTitles[5] = "Profile";
             else
                 mPlanetTitles[5] = "Register";
             switch(position) {
                 case 0:
                     myWebView.loadUrl("http://google.com");
                     //@TODO Change this to contextual content(or leave as now)
                     //setTitle(mPlanetTitles[position]);
                     break;
                 case 1:
                     myWebView.loadUrl("http://serebii.net");
                     break;
                 case 2:
                     myWebView.loadUrl("http://serebii.net");
                     break;
                 case 3:
                     myWebView.loadUrl("http://serebii.net");
                     break;
                 case 4:
                     myWebView.loadUrl("http://serebii.net");
                     break;
                 case 5:
                     myWebView.loadUrl("http://androidpolice.com");
                     break;
             }
         } catch(Exception e) {
             Cheers("Item Selection Error: "+e.getMessage());
         }
         Cheers("You selected item #"+position+", "+mPlanetTitles[position]);
        setTitle(mPlanetTitles[position]);
 
         try {
             refreshDrawerContents();
         } catch(Exception e) {
             Cheers(e.getMessage()+" "+e.getLocalizedMessage());
         }
 
     }
 
     private void Cheers(String s) {
         Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
     }
 
     @Override
     public void setTitle(CharSequence title) {
         mTitle = title;
         getActionBar().setTitle(mTitle);
     }
 
     public void refreshDrawerContents() {
         mDrawerList.setAdapter(new ArrayAdapter<String>(this,
          R.layout.drawer_list_item, mPlanetTitles));
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
 
     /**
      * Fragment that appears in the "content_frame", shows a planet
      */
     public static class PlanetFragment extends android.app.Fragment {
         public static final String ARG_PLANET_NUMBER = "planet_number";
 
         public PlanetFragment() {
             // Empty constructor required for fragment subclasses
         }
 
         @Override
         public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                  Bundle savedInstanceState) {
             View rootView = inflater.inflate(R.layout.fragment_planet, container, false);
             //@TODO Remove frag_planet references
             int i = getArguments().getInt(ARG_PLANET_NUMBER);
             String planet = getResources().getStringArray(R.array.planets_array)[i];
 
             int imageId = getResources().getIdentifier(planet.toLowerCase(Locale.getDefault()),
                     "drawable", getActivity().getPackageName());
             ((ImageView) rootView.findViewById(R.id.image)).setImageResource(imageId);
             getActivity().setTitle(planet);
             return rootView;
         }
     }
 
 }
