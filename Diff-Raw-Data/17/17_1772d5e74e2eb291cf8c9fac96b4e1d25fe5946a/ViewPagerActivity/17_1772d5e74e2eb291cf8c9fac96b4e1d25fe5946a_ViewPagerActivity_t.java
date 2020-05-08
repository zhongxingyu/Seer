 package com.underdusken.kulturekalendar.ui.activities;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
 import android.support.v4.view.ViewPager;
 import android.support.v7.app.ActionBar;
 import android.support.v7.app.ActionBar.Tab;
 import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
 import android.view.Menu;
 import android.view.MenuInflater;
import android.view.MenuItem;
 import android.widget.Toast;
 
 import com.underdusken.kulturekalendar.R;
 import com.underdusken.kulturekalendar.mainhandler.MainHandler;
 import com.underdusken.kulturekalendar.sharedpreference.UserFilterPreference;
 import com.underdusken.kulturekalendar.ui.fragments.TabAll;
 import com.underdusken.kulturekalendar.ui.fragments.TabFavorite;
 import com.underdusken.kulturekalendar.ui.fragments.TabFeatured;
 import com.underdusken.kulturekalendar.ui.fragments.TabFilter;
 import com.underdusken.kulturekalendar.ui.fragments.TabSetup;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  */
 public class ViewPagerActivity extends ActionBarActivity {
     private static final int UPDATE_SECONDS_DELAY = 30 * 60;
     private static final String TAG = "ViewPagerActivity";
 
     private PageAdapter pagerAdapter;
     private ViewPager viewPager;
     private ActionBar actionBar;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.view_pager);
 
         pagerAdapter = new PageAdapter(getSupportFragmentManager());
         viewPager = (ViewPager) findViewById(R.id.viewpager);
         viewPager.setAdapter(pagerAdapter);
 
         actionBar = getSupportActionBar();
         actionBar.setTitle(R.string.app_name);
         actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
         ActionBar.TabListener tabListener = new ActionBar.TabListener() {
             @Override
             public void onTabSelected(Tab tab, FragmentTransaction fragmentTransaction) {
                 viewPager.setCurrentItem(tab.getPosition(), true);
             }
 
             @Override
             public void onTabUnselected(Tab tab, FragmentTransaction fragmentTransaction) {
                 return;
             }
 
             @Override
             public void onTabReselected(Tab tab, FragmentTransaction fragmentTransaction) {
                 return;
             }
         };
 
         viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
             @Override
             public void onPageScrolled(int position, float posOffset, int posOffsetPx) {
             }
 
             @Override
             public void onPageSelected(int index) {
                 actionBar.setSelectedNavigationItem(index);
             }
 
             @Override
             public void onPageScrollStateChanged(int index) {
             }
         });
 
         Tab tab = actionBar.newTab().setText(R.string.tab1).setTabListener(tabListener);
         actionBar.addTab(tab);
         pagerAdapter.addFragment(new TabFeatured());
 
         tab = actionBar.newTab().setText(R.string.tab2).setTabListener(tabListener);
         actionBar.addTab(tab);
         pagerAdapter.addFragment(new TabAll());
 
         tab = actionBar.newTab().setText(R.string.tab3).setTabListener(tabListener);
         actionBar.addTab(tab);
         pagerAdapter.addFragment(new TabFilter());
 
         tab = actionBar.newTab().setText(R.string.tab4).setTabListener(tabListener);
         actionBar.addTab(tab);
         pagerAdapter.addFragment(new TabFavorite());
 
         tab = actionBar.newTab().setText(R.string.tab5).setTabListener(tabListener);
         actionBar.addTab(tab);
         pagerAdapter.addFragment(new TabSetup());
 
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater menuInflater = getMenuInflater();
         menuInflater.inflate(R.menu.actionbar_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        if (searchView != null) {
            searchView.setQueryHint(getString(R.string.search_current_tab));
        }
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     protected void onResume() {
         super.onResume();
 
         long curTime = System.currentTimeMillis();
 
         if (curTime > new UserFilterPreference(this).getLastUpdate() + UPDATE_SECONDS_DELAY * 1000) {
             new UserFilterPreference(this).setLastUpdate(curTime);
             MainHandler.getInstance(this.getApplicationContext()).onStartApplication();
             Toast.makeText(this.getApplicationContext(), "Loading ...", Toast.LENGTH_SHORT).show();
         }
 
     }
 
     private class PageAdapter extends FragmentPagerAdapter{
         List<Fragment> fragments;
 
         public PageAdapter(FragmentManager fm) {
             super(fm);
             fragments = new ArrayList<Fragment>();
         }
 
         public void addFragment(Fragment f){
             fragments.add(f);
             notifyDataSetChanged();
         }
 
          @Override
         public Fragment getItem(int index){
              return fragments.get(index);
          }
 
         @Override
         public int getCount(){
             return fragments.size();
         }
     }
 }
