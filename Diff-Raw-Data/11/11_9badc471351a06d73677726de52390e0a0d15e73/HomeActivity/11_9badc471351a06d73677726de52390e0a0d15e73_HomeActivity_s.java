 package me.occucard.controller;
 
 import android.app.Activity;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.app.Fragment;
 import android.support.v4.widget.DrawerLayout;
 import android.support.v7.app.ActionBarActivity;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 import me.occucard.R;
 import me.occucard.storage.async.DownloadCardsTask;
 import me.occucard.view.adapter.DrawerAdapter;
 import me.occucard.view.fragment.MyCards;
 import me.occucard.view.fragment.Settings;
 import me.occucard.view.fragment.find.Find;
 import me.occucard.view.fragment.share.Share;
 
 public class HomeActivity extends ActionBarActivity {
 
     public static final int NONE = 0x0;
     public static final int MY_CARDS = 0x1;
     public int actionBarState = NONE;
 
     ActionBarDrawerToggle drawerToggle;
     ListView drawerList;
     DrawerLayout drawer;
     RelativeLayout drawerContainer;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.home_activity);
 
         //Set up drawer menu
         //Drawer logout
         TextView logout = (TextView) findViewById(R.id.drawer_logout);
         logout.setOnClickListener(new DrawerLogoutClickListener());
 
         //Drawer list
         String[] drawerItems = getResources().getStringArray(R.array.drawer_items);
         drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
         drawerContainer = (RelativeLayout) findViewById(R.id.drawer_container);
         drawerList = (ListView) findViewById(R.id.drawer_list);
         drawerList.setAdapter(new DrawerAdapter(this, drawerItems));
         drawerList.setOnItemClickListener(new DrawerItemClickListener());
 
         drawerToggle = new DrawerSlidingListener(this, drawer);
         drawer.setDrawerListener(drawerToggle);
 
         drawerList.setItemChecked(0, true);
 
         showInitialView();
 
         getSupportActionBar().setIcon(R.drawable.logo);
         getSupportActionBar().setDisplayShowTitleEnabled(false);
     }
 
     public void showInitialView(){
         //Check if container exists
         if(findViewById(R.id.fragment_container) != null){
             //Now add some fragment.
             //Return if already opened or something like that.
             getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MyCards()).commit();
         }
     }
 
     @Override
     protected void onPostCreate(Bundle savedInstanceState) {
         super.onPostCreate(savedInstanceState);
         // Sync the toggle state after onRestoreInstanceState has occurred.
         drawerToggle.syncState();
     }
 
     @Override
     protected void onResume() {
         super.onResume();
     }
 
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
         drawerToggle.onConfigurationChanged(newConfig);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         int id;
         switch (actionBarState) {
             case NONE:
                 id = R.menu.none;
                 break;
             case MY_CARDS:
                 id = R.menu.my_cards;
                 break;
             default:
                 id = R.id.none;
                 break;
         }
         inflater.inflate(id, menu);
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         if (drawerToggle.onOptionsItemSelected(item))
             return true;
         switch (item.getItemId()){
             case R.id.action_refresh:
                 new DownloadCardsTask(this).execute("");//Pass token
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     private class DrawerItemClickListener implements OnItemClickListener{
         @Override
         public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
             drawerList.setSelection(i);
             Fragment frag = getFragmentFromPosition(i);
             getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, frag).commit();
             drawer.closeDrawers();
         }
     }
 
     private class DrawerLogoutClickListener implements View.OnClickListener{
         @Override
         public void onClick(View view) {
             //OAuthUtils.logout(HomeActivity.this);
         }
     }
 
     private Fragment getFragmentFromPosition(int pos){
         switch (pos){
             case 0:
                 return new MyCards();
             case 1:
                 return new Share();
             case 2:
                 return new Find();
             case 3:
                 return new Settings();
             default:
                 return new MyCards();
         }
     }
 
     @Override
     public void onBackPressed() {
         Fragment frag = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
         if(frag == null || !(frag instanceof MyCards))
             getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MyCards()).commit();
         else if(drawer.isDrawerOpen(drawerContainer))
             drawer.closeDrawers();
         else
             super.onBackPressed();
     }
 
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         if (keyCode == KeyEvent.KEYCODE_MENU) {
             if(drawer.isDrawerOpen(drawerContainer))
                 drawer.closeDrawers();
             else
                 drawer.openDrawer(drawerContainer);
             return true;
         }
         return super.onKeyDown(keyCode, event);
     }
 
     private class DrawerSlidingListener extends ActionBarDrawerToggle{
 
         float previousOffset = 0f;
 
         public DrawerSlidingListener(Activity context, DrawerLayout drawer){
             super(context, drawer, R.drawable.ic_navigation_drawer, R.drawable.ic_navigation_drawer, R.drawable.ic_navigation_drawer);
         }
 
         @Override
         public void onDrawerOpened(View drawerView) {
             HomeActivity.this.actionBarState = HomeActivity.NONE;
             invalidateOptionsMenu();
         }
 
         @Override
         public void onDrawerClosed(View drawerView) {
             Fragment frag = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if(frag == null || !(frag instanceof MyCards)){
                 HomeActivity.this.actionBarState = HomeActivity.MY_CARDS;
                 invalidateOptionsMenu();
             }
         }
 
         @Override
         public void onDrawerSlide(View drawerView, float slideOffset) {
             if(slideOffset > previousOffset && actionBarState == HomeActivity.MY_CARDS){
                 actionBarState = HomeActivity.NONE;
                 invalidateOptionsMenu();
             }else if(previousOffset > slideOffset && slideOffset < 0.5f && actionBarState == HomeActivity.NONE){
                actionBarState = HomeActivity.MY_CARDS;
                invalidateOptionsMenu();
             }
             previousOffset = slideOffset;
         }
     }
 }
