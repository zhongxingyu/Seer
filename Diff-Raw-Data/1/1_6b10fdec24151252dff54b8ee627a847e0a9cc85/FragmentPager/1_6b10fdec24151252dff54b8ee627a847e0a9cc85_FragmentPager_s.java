 package no.hials.muldvarp.view;
 
 import android.app.ActionBar;
 import android.app.ActionBar.Tab;
 import android.content.Context;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.view.ViewPager;
 import android.util.AttributeSet;
import java.lang.reflect.Array;
 
 /**
  *
  * @author mikael
  */
 public class FragmentPager extends ViewPager {
     ActionBar bar;
     DefaultFragmentPagerAdapter adapter;
     DefaultViewPagerListener pageListener;
     
     public FragmentPager(Context context, AttributeSet attrs) {
         super(context, attrs);
     }
 
     public FragmentPager(Context context) {
         super(context);
     }
     
     public void initializeAdapter(FragmentManager manager, ActionBar bar) {
         this.bar = bar;
         adapter = new DefaultFragmentPagerAdapter(getContext(),manager,bar);
         adapter.setActionBar(bar);
         setAdapter(adapter);
         pageListener = new DefaultViewPagerListener(bar);
         super.setOnPageChangeListener(pageListener);
     }
 
     @Override
     public void setOnPageChangeListener(OnPageChangeListener listener) {
         pageListener.setOnPageChangeListener(listener);
     }
     
 
     
     public Tab addTab(String title, Class<? extends Fragment> fragment) {
         if(adapter != null) {
             adapter.addView(fragment);
         }
         
         Tab retVal = bar.newTab();
                 
         retVal.setText(title);
         retVal.setTabListener(new DefaultViewPagerTabListener(this));
         bar.addTab(retVal);
         
         return retVal;
     }
     
     public Fragment getTab(int i){
         return adapter.views.get(i);
     }
 }
