 package com.tw.techradar.activity;
 
 import android.app.ActionBar;
 import android.app.FragmentTransaction;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.*;
 import android.support.v4.view.ViewPager;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.DisplayMetrics;
 import android.view.*;
 import android.webkit.WebView;
 import android.widget.*;
 import com.tw.techradar.R;
 import com.tw.techradar.controller.RadarController;
 import com.tw.techradar.model.Radar;
 import com.tw.techradar.model.RadarItem;
 import com.tw.techradar.support.RadarView;
 import com.tw.techradar.support.quadrants.QuadrantType;
 import com.tw.techradar.ui.model.Blip;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Properties;
 
 public class ActionBarTabsPager extends FragmentActivity {
     ViewPager mViewPager;
     TabsAdapter mTabsAdapter;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         mViewPager = new ViewPager(this);
         mViewPager.setId(R.id.flipper);
         setContentView(mViewPager);
 
         final ActionBar bar = getActionBar();
         bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
         bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
 
 
 
         mTabsAdapter = new TabsAdapter(this, mViewPager);
 //        Properties menuItems = loadMenuItems();
 //        for (Map.Entry menuEntry : menuItems.entrySet()) {
 //            mTabsAdapter.addTab(bar.newTab().setText((String) menuEntry.getKey()),
 //                    WebViewFragment.class, getArgsWithURL((String)menuEntry.getValue()));
 //
 //        }
         mTabsAdapter.addTab(bar.newTab().setText("Intro"),
                 WebViewFragment.class, getArgsWithURL("file:///android_asset/html/introduction.html"));
         mTabsAdapter.addTab(bar.newTab().setText("About"),
                 WebViewFragment.class, getArgsWithURL("file:///android_asset/html/about.html"));
         mTabsAdapter.addTab(bar.newTab().setText("Radar"),
                 RadarFragment.class, null);
         mTabsAdapter.addTab(bar.newTab().setText("References"),
                 WebViewFragment.class, getArgsWithURL("file:///android_asset/html/radar_references.html"));
 
         if (savedInstanceState != null) {
             bar.setSelectedNavigationItem(savedInstanceState.getInt("tab", 0));
         }
     }
 
     private Properties loadMenuItems() {
         Properties menuItems = new Properties();
         try {
             InputStream rawResource = getResources().openRawResource(R.raw.menu);
             menuItems.load(rawResource);
         } catch (Exception e) {
             System.err.println("ERROR: Cannot load menu items - cannot continue");
             finish();
         }
         return menuItems;
     }
 
 
     private Bundle getArgsWithURL(String someURL) {
         Bundle args = new Bundle();
         args.putString("URL", someURL);
         return args;
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         outState.putInt("tab", getActionBar().getSelectedNavigationIndex());
     }
 
     /**
      * This is a helper class that implements the management of tabs and all
      * details of connecting a ViewPager with associated TabHost.  It relies on a
      * trick.  Normally a tab host has a simple API for supplying a View or
      * Intent that each tab will show.  This is not sufficient for switching
      * between pages.  So instead we make the content part of the tab host
      * 0dp high (it is not shown) and the TabsAdapter supplies its own dummy
      * view to show as the tab content.  It listens to changes in tabs, and takes
      * care of switch to the correct paged in the ViewPager whenever the selected
      * tab changes.
      */
     public static class TabsAdapter extends FragmentPagerAdapter
             implements ActionBar.TabListener, ViewPager.OnPageChangeListener {
         private final Context mContext;
         private final ActionBar mActionBar;
         private final ViewPager mViewPager;
         private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
 
         static final class TabInfo {
             private final Class<?> clss;
             private final Bundle args;
 
             TabInfo(Class<?> _class, Bundle _args) {
                 clss = _class;
                 args = _args;
             }
         }
 
         public TabsAdapter(FragmentActivity activity, ViewPager pager) {
             super(activity.getSupportFragmentManager());
             mContext = activity;
             mActionBar = activity.getActionBar();
             mViewPager = pager;
             mViewPager.setAdapter(this);
             mViewPager.setOnPageChangeListener(this);
         }
 
         public void addTab(ActionBar.Tab tab, Class<?> clss, Bundle args) {
             TabInfo info = new TabInfo(clss, args);
             tab.setTag(info);
             tab.setTabListener(this);
             mTabs.add(info);
             mActionBar.addTab(tab);
             notifyDataSetChanged();
         }
 
         @Override
         public int getCount() {
             return mTabs.size();
         }
 
         @Override
         public Fragment getItem(int position) {
             TabInfo info = mTabs.get(position);
             return Fragment.instantiate(mContext, info.clss.getName(), info.args);
         }
 
         @Override
         public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
         }
 
         @Override
         public void onPageSelected(int position) {
             mActionBar.setSelectedNavigationItem(position);
         }
 
         @Override
         public void onPageScrollStateChanged(int state) {
         }
 
         @Override
         public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
             Object tag = tab.getTag();
             for (int i=0; i<mTabs.size(); i++) {
                 if (mTabs.get(i) == tag) {
                     mViewPager.setCurrentItem(i);
                 }
             }
         }
 
         @Override
         public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
         }
 
         @Override
         public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
         }
     }
 
     public static class WebViewFragment extends Fragment {
         String URL;
 
         /**
          * Create a new instance of CountingFragment, providing "num"
          * as an argument.
          */
         static WebViewFragment newInstance(int num) {
             WebViewFragment f = new WebViewFragment();
 
             // Supply num input as an argument.
             Bundle args = new Bundle();
             args.putInt("num", num);
             f.setArguments(args);
 
             return f;
         }
 
         /**
          * When creating, retrieve this instance's number from its arguments.
          */
         @Override
         public void onCreate(Bundle savedInstanceState) {
             super.onCreate(savedInstanceState);
             URL = getArguments() != null ? getArguments().getString("URL") : "";
         }
 
         /**
          * The Fragment's UI is just a simple text view showing its
          * instance number.
          */
         @Override
         public View onCreateView(LayoutInflater inflater, ViewGroup container,
                 Bundle savedInstanceState) {
             View v = inflater.inflate(R.layout.web_view, container, false);
             WebView webView = (WebView) v.findViewById(R.id.htmlView);
             webView.loadUrl(URL);
             return v;
         }
     }
 
     public static class RadarFragment extends Fragment implements TextWatcher, AdapterView.OnItemSelectedListener, View.OnTouchListener {
         private Radar radarData;
         private RadarView radarView;
         private long lastTouchTime = 0;
         private View mainView;
 
         static RadarFragment newInstance(int num) {
             RadarFragment f = new RadarFragment();
 
             // Supply num input as an argument.
             Bundle args = new Bundle();
             args.putInt("num", num);
             f.setArguments(args);
 
             return f;
         }
 
 
         @Override
         public void onCreate(Bundle savedInstanceState) {
             super.onCreate(savedInstanceState);
             radarData = getRadarData();
 
         }
 
         /**
          * The Fragment's UI is just a simple text view showing its
          * instance number.
          */
         @Override
         public View onCreateView(LayoutInflater inflater, ViewGroup container,
                 Bundle savedInstanceState) {
             mainView = inflater.inflate(R.layout.current_radar, container, false);
            mainView.setOnTouchListener(this);
             radarView = new RadarView(getDisplayMetrics(),radarData,mainView.findViewById(R.id.currentRadarLayout));
             drawRadarPostViewRendered();
             return mainView;
         }
 
         private void drawRadarPostViewRendered() {
             mainView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                 @Override
                 public void onGlobalLayout() {
                     if (isViewRendered()) {
                         mainView.getViewTreeObserver().removeGlobalOnLayoutListener(this); //Needed deprecated method for Honeycomb compatibility
                         //radarView.initViews();
                         radarView.drawRadar();
                         populateRadarFilter();
                         initSearchListener();
                     }
 
                 }
 
                 private boolean isViewRendered() {
                     return mainView.getMeasuredHeight() != 0 && mainView.getMeasuredWidth() != 0;
                 }
             });
         }
 
         private DisplayMetrics getDisplayMetrics() {
             DisplayMetrics displayMetrics = new DisplayMetrics();
             this.getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
             return displayMetrics;
         }
 
         private Radar getRadarData() {
             Radar radarData = null;
             try {
                 return new RadarController(getActivity().getAssets()).getRadarData();
             } catch (Exception e) {
                 e.printStackTrace();
             }
             return radarData;
         }
 
         private boolean isDoubleTap(MotionEvent event){
             long thisTime = event.getEventTime();
             if((thisTime - lastTouchTime) < 250) {
                 lastTouchTime = -1;
                 return true;
             }
             lastTouchTime = thisTime;
             return false;
         }
 
         private void switchRadarView(float x, float y) {
             if (radarView.getCurrentQuadrantType() != QuadrantType.QUADRANT_ALL){
                 radarView.switchQuadrant(QuadrantType.QUADRANT_ALL);
             }
             else {
                 radarView.switchQuadrant(radarView.getQuadrantClicked(x, y));
             }
         }
 
         private void displayItemInfo(Blip blip) {
             Intent intent = new Intent(getActivity(), ItemInfoActivity.class);
             intent.putExtra(RadarItem.ITEM_KEY, blip.getRadarItem());
             startActivity(intent);
         }
 
         private void initSearchListener() {
             EditText searchTextBox = (EditText) mainView.findViewById(R.id.searchBox);
             searchTextBox.addTextChangedListener(this);
         }
 
         @Override
         public boolean onTouch(View view, MotionEvent event) {
             if (isSingleFingerTouchGesture(event))
             {
                 if (event.getAction() == MotionEvent.ACTION_DOWN){
                     Blip blip = radarView.getBlipClicked(event.getX(), event.getY());
                     if (blip != null) {
                         System.out.println("Click lies on a " + blip.getClass() + " Blip");
                         displayItemInfo(blip);
                         return true;
                     } else if(isDoubleTap(event)){
                         System.out.println("Click does not lie on a Blip");
                         switchRadarView(event.getX(), event.getY());
                         return true;
                     }
                 }
             }
             return false;
         }
 
         private boolean isSingleFingerTouchGesture(MotionEvent event) {
             return event.getPointerCount() == 1;
         }
 
         @Override
         public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
             String itemText = adapterView.getItemAtPosition(pos).toString();
             radarView.filterByRadarArc(radarData.getRadarArc(itemText));
         }
 
         @Override
         public void onNothingSelected(AdapterView<?> adapterView) {
         }
 
         private void populateRadarFilter() {
             Spinner spinner = (Spinner) mainView.findViewById(R.id.radar_filter_spinner);
             ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                     R.array.radar_circles_array, android.R.layout.simple_spinner_item);
             adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
             spinner.setAdapter(adapter);
             spinner.setOnItemSelectedListener(this);
         }
 
 
         @Override
         public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
         }
 
         @Override
         public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
             radarView.filterBySearchText(charSequence.toString());
         }
 
         @Override
         public void afterTextChanged(Editable editable) {
         }
 
 
     }
 
 }
