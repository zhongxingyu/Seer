 package com.profes.meteo;
 
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 
 public class SectionsPagerAdapter extends FragmentPagerAdapter {
 
     private Resources resources;
 
     public SectionsPagerAdapter(Resources resources, FragmentManager fm) {
         super(fm);
         this.resources = resources;
     }
 
     @Override
     public Fragment getItem(int position) {
         // getItem is called to instantiate the fragment for the given page.
         // Return a CitySectionFragment (defined as a static inner class
         // below) with the page number as its lone argument.
         switch (position) {
             case 0:
                Fragment fragment = new CitySectionFragment_();
                 Bundle args = new Bundle();
                 args.putInt(CitySectionFragment.ARG_SECTION_NUMBER, position + 1);
                 fragment.setArguments(args);
                 return fragment;
             case 1:
                 Fragment fragment2 = new RadarFragment_();
                 Bundle args2 = new Bundle();
                 args2.putInt(CitySectionFragment.ARG_SECTION_NUMBER, position + 1);
                 fragment2.setArguments(args2);
                 return fragment2;
         }
         return null;
     }
 
 
     @Override
     public int getCount() {
         return 2;
     }
 
     @Override
     public CharSequence getPageTitle(int position) {
         switch (position) {
             case 0:
                 return resources.getString(R.string.tab_title_current_city);
             case 1:
                 return resources.getString(R.string.tab_title_radar);
         }
         return null;
     }
 }
