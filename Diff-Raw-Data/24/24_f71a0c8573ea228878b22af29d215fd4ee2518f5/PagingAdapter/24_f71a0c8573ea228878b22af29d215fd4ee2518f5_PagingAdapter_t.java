 package com.avantics.savingscalc.common;
 
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;

 import com.avantics.savingscalc.common.fragments.IncumbentFragment;
 import com.avantics.savingscalc.common.fragments.ProposedFragment;
 import com.avantics.savingscalc.common.fragments.SummaryFragment;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created by tom on 02/06/13.
  */
 public class PagingAdapter extends FragmentPagerAdapter {
 
     private static final int FRAGMENT_COUNT = 3;
     private List<Fragment> mFragments = new ArrayList<Fragment>();
 
     public PagingAdapter(FragmentManager fm) {
         super(fm);
 
         // add fragments
         mFragments.add(new IncumbentFragment());
         mFragments.add(new ProposedFragment());
         mFragments.add(new SummaryFragment());
     }
 
     @Override
     public int getCount() {
         return FRAGMENT_COUNT;
     }
 
     // This is called by the ViewPager to get the fragment at tab position pos
     // ViewPager calls this when the Tab handler handles a tab change
     @Override
     public Fragment getItem(int pos) {
         return mFragments.get(pos);
     }
 }
