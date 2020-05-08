 
 package hk.hku.cs.srli.factfinder;
 
 import android.app.AlertDialog;
 import android.app.Fragment;
 import android.app.FragmentManager;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v13.app.FragmentPagerAdapter;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.view.ViewPager;
 import android.support.v4.widget.SlidingPaneLayout;
 import android.view.View;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 
 import hk.hku.cs.srli.factfinder.ui.FFSlidingPaneLayout;
 import hk.hku.cs.srli.widget.HoverFrame;
 
 import java.util.Locale;
 
 public class MainActivity extends SherlockActivity implements ActionBar.TabListener {
 
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
     
     private HoverFrame mWrapper;
    private HoverFrame mPagerFrame;
    private HoverFrame mOrderFrame;
     private FFSlidingPaneLayout mSlidingPane;
     
     private boolean mPageSwitching = false;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         int theme = FFApp.getApp(this).getFFTheme();
         if (theme != 0) setTheme(theme);
         setContentView(R.layout.activity_main);
 
         // Set up the action bar.
         final ActionBar actionBar = getSupportActionBar();
         actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
         mWrapper = (HoverFrame) findViewById(R.id.wrapper);

        mPagerFrame = (HoverFrame) findViewById(R.id.pager_frame);
        mOrderFrame = (HoverFrame) findViewById(R.id.order_frame);
         mSlidingPane = (FFSlidingPaneLayout) findViewById(R.id.slidingPaneLayout);
         mSlidingPane.openPane();
         
         // Create the adapter that will return a fragment for each of the three
         // primary sections of the app.
         mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());
 
         if (mSectionsPagerAdapter.getCount() < 5) {
             // a cosmetic hack to adjust ActionBar tab position  
             View view = findViewById(R.id.abs__action_bar_title);
             view.setPaddingRelative(0, 0, 55, 0);
         }
         
         // Set up the ViewPager with the sections adapter.
         mViewPager = (ViewPager) findViewById(R.id.pager);
         mViewPager.setAdapter(mSectionsPagerAdapter);
 
         // When swiping between different sections, select the corresponding
         // tab. We can also use ActionBar.Tab#select() to do this if we have
         // a reference to the Tab.
         mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
             @Override
             public void onPageSelected(int position) {
                 if (!mPageSwitching) {
                     mPageSwitching = true;
                     actionBar.setSelectedNavigationItem(position);
                     mPageSwitching = false;
                 }
             }
             
             @Override
             public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                 if (positionOffset > 0) {
                     // in middle of scrolling
                     updateHoverEdge(true, true);
                 } else updateHoverEdge();
             }
         });
         
         mSlidingPane.setPanelSlideListener(new SlidingPaneLayout.SimplePanelSlideListener() {
             @Override
             public void onPanelClosed(View panel) {
                 mWrapper.setEnabled(false);
                mOrderFrame.setHorizontalScrollable(true, false);
             }
             
             @Override
             public void onPanelOpened(View panel) {
                // show edge effect on the right edge
                 mWrapper.setEnabled(true);
                mOrderFrame.setHorizontalScrollable(false, true);
             }
         });
 
         // For each of the sections in the app, add a tab to the action bar.
         for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
             // Create a tab with text corresponding to the page title defined by
             // the adapter. Also specify this Activity object, which implements
             // the TabListener interface, as the callback (listener) for when
             // this tab is selected.
             actionBar.addTab(
                     actionBar.newTab()
                             .setText(mSectionsPagerAdapter.getPageTitle(i))
                             .setTabListener(this));
         }
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         // enter low profile mode
         getWindow().getDecorView()
                 .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getSupportMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
     @Override
     public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
         if (!mPageSwitching) {
             mPageSwitching = true;
             // When the given tab is selected, switch to the corresponding page in
             // the ViewPager.
             mViewPager.setCurrentItem(tab.getPosition());
             mPageSwitching = false;
         }
     }
 
     @Override
     public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
     }
 
     @Override
     public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
     }
     
     private void updateHoverEdge() {
         int nPages = mSectionsPagerAdapter.getCount();
         if (nPages <= 0) return;
         // only one page
         if (nPages == 1) {
             updateHoverEdge(false, false);
         } else if (mViewPager.getCurrentItem() == 0) {
             // leftmost page
             updateHoverEdge(false, true);
         } else if (mViewPager.getCurrentItem() == nPages - 1) {
             // rightmost page
             updateHoverEdge(true, false);
         } else {
             updateHoverEdge(true, true);
         }
     }
     
     private void updateHoverEdge(boolean leftScrollable, boolean rightScrollable) {
        mPagerFrame.setHorizontalScrollable(leftScrollable, rightScrollable);
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         if (item.getItemId() == R.id.action_info) {
             AlertDialog dialog = new AlertDialog.Builder(this)
                     .setMessage(R.string.dialog_info_message)
                     .setTitle(R.string.dialog_info_title).create();
             dialog.show();
             return true;
         } else if (item.getItemId() == R.id.action_config) {
             FFApp.getOrder(this).clear();
             Intent i = new Intent(this, TestActivity.class);
             // new task
             i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
             // start test
             startActivity(i);
             return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     /**
      * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
      * one of the sections/tabs/pages.
      */
     public class SectionsPagerAdapter extends FragmentPagerAdapter {
 
         public SectionsPagerAdapter(FragmentManager fm) {
             super(fm);
         }
 
         @Override
         public Fragment getItem(int position) {
             // getItem is called to instantiate the fragment for the given page.
             // Return a SectionFragment with the page number as its lone argument.
             Fragment fragment = new SectionFragment();
             Bundle args = new Bundle();
             args.putInt(SectionFragment.ARG_SECTION_NUMBER, position);
             fragment.setArguments(args);
             return fragment;
         }
 
         @Override
         public int getCount() {
             // Total number of sections.
             return FFApp.getData(MainActivity.this).getNumberOfCategories();
         }
 
         @Override
         public CharSequence getPageTitle(int position) {
             Locale l = Locale.getDefault();
             String name = FFApp.getData(MainActivity.this).getCategoryAt(position).getName();
             return name.toUpperCase(l);
         }
     }
 
 }
