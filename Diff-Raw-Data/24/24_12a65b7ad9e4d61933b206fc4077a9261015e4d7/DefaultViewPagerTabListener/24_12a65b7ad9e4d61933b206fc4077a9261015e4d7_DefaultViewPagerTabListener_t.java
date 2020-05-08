 package no.hials.muldvarp.view;
 
 import android.app.ActionBar;
 import android.app.FragmentTransaction;
 import android.support.v4.view.ViewPager;
 
 /**
  *
  * @author mikael
  */
 public class DefaultViewPagerTabListener implements ActionBar.TabListener {
     private ViewPager viewPager;
     private ActionBar.TabListener userListener;
     
     public DefaultViewPagerTabListener(ViewPager pager) {
         viewPager = pager;
     }
 
    @Override
     public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
         viewPager.setCurrentItem(tab.getPosition());
         if(userListener != null)
             userListener.onTabSelected(tab, ft);
     }
 
     public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
         if(userListener != null)
             userListener.onTabUnselected(tab, ft);
     }
 
     public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
         if(userListener != null)
             userListener.onTabReselected(tab, ft);
     }
 
     public void setOnPageChangeListener(ActionBar.TabListener listener) {
         userListener = listener;
     }
     
     public ActionBar.TabListener getOnPageChangeListener() {
         return userListener;
     }
 }
