 package com.jinheyu.lite_mms;
 
 import android.app.ActionBar;
 import android.app.FragmentTransaction;
 import android.app.SearchManager;
 import android.content.Context;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.support.v7.app.ActionBarActivity;
 import android.support.v7.widget.SearchView;
 import android.text.InputType;
 import android.util.Log;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Spinner;
 import android.widget.Toast;
 import com.jinheyu.lite_mms.data_structures.WorkCommand;
 import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
 
 import java.util.ArrayList;
 import java.util.List;
 
 
 public abstract class WorkCommandListActivity extends ActionBarActivity {
     protected ActionBar.TabListener mTabListener = new ActionBar.TabListener() {
         /**
          * Called when a tab that is already selected is chosen again by the user.
          * Some applications may use this action to return to the top level of a category.
          *
          * @param tab The tab that was reselected.
          * @param ft  A {@link android.app.FragmentTransaction} for queuing fragment operations to execute
          *            once this method returns. This FragmentTransaction does not support
          *            being added to the back stack.
          */
         @Override
         public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
         }
 
         /**
          * Called when a tab enters the selected state.
          *
          * @param tab The tab that was selected
          * @param ft  A {@link android.app.FragmentTransaction} for queuing fragment operations to execute
          *            during a tab switch. The previous tab's unselect and this tab's select will be
          *            executed in a single transaction. This FragmentTransaction does not support
          *            being added to the back stack.
          */
         @Override
         public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
             collapseActionView();
             mViewPager.setCurrentItem(tab.getPosition());
         }
 
         /**
          * Called when a tab exits the selected state.
          *
          * @param tab The tab that was unselected
          * @param ft  A {@link android.app.FragmentTransaction} for queuing fragment operations to execute
          *            during a tab switch. This tab's unselect and the newly selected tab's select
          *            will be executed in a single transaction. This FragmentTransaction does not
          *            support being added to the back stack.
          */
         @Override
         public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
         }
 
     };
     protected ViewPager mViewPager;
     /**
      * lastQuery 上次查询
      */
     private String lastQuery;
     private WorkCommandListAdapter mCurrentListAdapter;
     private List<WorkCommand> allWorkCommandList;
     private PullToRefreshAttacher mPullToRefreshAttacher;
     private boolean doubleBackToExitPressedOnce;
     private MenuItem searchItem;
 
     public PullToRefreshAttacher getPullToRefreshAttacher() {
         return mPullToRefreshAttacher;
     }
 
     @Override
     public void onBackPressed() {
         if (doubleBackToExitPressedOnce) {
             super.onBackPressed();
             return;
         }
         this.doubleBackToExitPressedOnce = true;
         Toast.makeText(this, R.string.exit_press_back_twice_message, Toast.LENGTH_SHORT).show();
         new Handler().postDelayed(new Runnable() {
 
             @Override
             public void run() {
                 doubleBackToExitPressedOnce = false;
             }
         }, 2000);
     }
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_work_command_list_main);
 
         mPullToRefreshAttacher = PullToRefreshAttacher.get(this);
         mViewPager = (ViewPager) findViewById(R.id.pager);
         final ActionBar actionBar = getActionBar();
         actionBar.setHomeButtonEnabled(false);
         actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
         Spinner spinner = getSpinner();
         if (spinner != null) {
             actionBar.setCustomView(getSpinner());
             actionBar.setDisplayShowCustomEnabled(true);
         } else {
             initTabs(actionBar, 0);
             // make action bar hide title
             actionBar.setDisplayShowTitleEnabled(false);
             actionBar.setDisplayShowHomeEnabled(false);
         }
     }
 
     protected ArrayAdapter getArrayAdapter(int resource) {
         return null;
     }
 
     protected abstract FragmentPagerAdapter getFragmentPagerAdapter(int position);
 
     protected void setSearchView(MenuItem searchItem) {
         this.searchItem = searchItem;
         SearchView searchView = (SearchView) searchItem.getActionView();
         if (searchView != null) {
             SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
             searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
             searchView.setIconifiedByDefault(false);
             searchView.setQueryHint("输入工单号搜索");
             searchView.setInputType(InputType.TYPE_CLASS_NUMBER);
             searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                 @Override
                 public boolean onQueryTextSubmit(String s) {
                     return doSearch(s);
                 }
 
                 @Override
                 public boolean onQueryTextChange(String s) {
                     return doSearch(s);
                 }
             });
         }
         searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
             @Override
             public boolean onMenuItemActionCollapse(MenuItem item) {
                 if (!Utils.isEmptyString(lastQuery)) {
                     doSearch("");
                 }
                 mCurrentListAdapter = null;
                 lastQuery = null;
                 return true;
             }
 
             @Override
             public boolean onMenuItemActionExpand(MenuItem item) {
                 if (mCurrentListAdapter == null) {
                     try {
                         List<Fragment> fragments = WorkCommandListActivity.this.getSupportFragmentManager().getFragments();
                         WorkCommandListFragment listFragment = (WorkCommandListFragment) fragments.get(WorkCommandListActivity.this.mViewPager.getCurrentItem());
                         if (listFragment.getReloadingStatus()) {
                             return false;
                         }
                         mCurrentListAdapter = (WorkCommandListAdapter) listFragment.getListAdapter();
                         List<WorkCommand> list = mCurrentListAdapter.getWorkCommandList();
                         allWorkCommandList = new ArrayList<WorkCommand>();
                         allWorkCommandList.addAll(list);
                     } catch (Exception e) {
                         Log.e("搜索失败", e.getMessage());
                         return false;
                     }
 
                 }
                 return true;
             }
         });
     }
 
     public void collapseActionView() {
         if (searchItem != null) {
             searchItem.collapseActionView();
         }
     }
 
     private boolean doSearch(String query) {
         if (lastQuery != null) {
             List<WorkCommand> currentWorkCommandList = mCurrentListAdapter.getWorkCommandList();
            ;
             currentWorkCommandList.clear();
             for (WorkCommand workCommand : allWorkCommandList) {
                 if (String.valueOf(workCommand.getId()).contains(query)) {
                     currentWorkCommandList.add(workCommand);
                 }
             }
             mCurrentListAdapter.notifyDataSetChanged();
         }
         lastQuery = query;
         return true;
     }
 
     private Spinner getSpinner() {
         ArrayAdapter adapter = getArrayAdapter(android.R.layout.simple_spinner_item);
         if (adapter == null) {
             return null;
         }
 
         Spinner spinner = new Spinner(this);
         final ActionBar actionBar = getActionBar();
 
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         spinner.setAdapter(adapter);
 
         spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
             @Override
             public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                 initTabs(actionBar, position);
             }
 
             @Override
             public void onNothingSelected(AdapterView<?> parent) {
 
             }
         });
         return spinner;
     }
 
     private void initTabs(final ActionBar actionBar, int position) {
         FragmentPagerAdapter fragmentPagerAdapter = getFragmentPagerAdapter(position);
         mViewPager.setAdapter(fragmentPagerAdapter);
         mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
             @Override
             public void onPageSelected(int position) {
                 // When swiping between different app sections, select the corresponding tab.
                 // We can also use ActionBar.Tab#select() to do this if we have a reference to the
                 // Tab.
                 actionBar.setSelectedNavigationItem(position);
             }
         });
         actionBar.removeAllTabs();
         for (int i = 0; i < fragmentPagerAdapter.getCount(); i++) {
             // Create a tab with text corresponding to the page title defined by the adapter.
             // Also specify this Activity object, which implements the TabListener interface, as the
             // listener for when this tab is selected.
             actionBar.addTab(
                     actionBar.newTab()
                             .setText(fragmentPagerAdapter.getPageTitle(i))
                             .setTabListener(mTabListener));
         }
     }
 }
