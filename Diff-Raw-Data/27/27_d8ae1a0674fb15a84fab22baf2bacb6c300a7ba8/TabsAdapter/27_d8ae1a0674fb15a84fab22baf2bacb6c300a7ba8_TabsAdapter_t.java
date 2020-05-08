 package ca.q0r.kfreqs.app.tabs;
 
 import android.app.ActionBar;
 import android.app.FragmentTransaction;
 import android.content.Context;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 
 import java.util.ArrayList;
 
 public class TabsAdapter extends FragmentPagerAdapter implements ActionBar.TabListener , ViewPager.OnPageChangeListener{
     private final Context mActivity;
     private final ActionBar mActionBar;
     private final ViewPager mViewPager;
     private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
 
     static final class TabInfo {
         private final Class<?> clazz;
 
         TabInfo(Class<?> _class){
             clazz = _class;
         }
     }
 
     public TabsAdapter(FragmentActivity activity, ViewPager pager) {
         super(activity.getSupportFragmentManager());
 
         mActivity = activity;
         mActionBar = activity.getActionBar();
         mViewPager = pager;
         mViewPager.setAdapter(this);
         mViewPager.setOnPageChangeListener(this);
     }
 
     public void addTab(ActionBar.Tab tab, Class<? extends Fragment> clazz){
         TabInfo info = new TabInfo(clazz);
         tab.setTag(info);
         tab.setTabListener(this);
         mTabs.add(info);
         mActionBar.addTab(tab);
         notifyDataSetChanged();
     }
 
     @Override
     public void onPageScrollStateChanged(int state) { }
 
     @Override
     public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }
 
     @Override
     public void onPageSelected(int position) {
         mActionBar.setSelectedNavigationItem(position);
     }
 
     @Override
     public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
         mViewPager.setCurrentItem(tab.getPosition());
 
         //Log.v("Q0r", "clicked");
 
         Object tag = tab.getTag();
 
         for (int i = 0; i < mTabs.size(); i++){
             if (mTabs.get(i) == tag){
                 mViewPager.setCurrentItem(i);
             }
         }
 
     }
 
     @Override
     public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) { }
 
     @Override
     public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) { }
 
     @Override
     public Fragment getItem(int position) {
         TabInfo info = mTabs.get(position);
         return Fragment.instantiate(mActivity, info.clazz.getName());
     }
 
     @Override
     public int getCount() {
         return mTabs.size();
     }
}
