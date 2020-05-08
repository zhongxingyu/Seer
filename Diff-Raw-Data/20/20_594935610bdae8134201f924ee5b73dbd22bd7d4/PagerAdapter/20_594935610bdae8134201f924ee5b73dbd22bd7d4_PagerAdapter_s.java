 package com.mustangexchange.polymeal;
 
 import android.app.Activity;
 import android.content.Context;
import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 
 import java.util.ArrayList;
 
 public class PagerAdapter extends FragmentPagerAdapter {
     public static ArrayList<FoodItemAdapter> foodAdapterList;
     public static Context context;
     public static Activity activity;
 
     public PagerAdapter(Context context, FragmentManager fm, ArrayList<FoodItemAdapter> foodAdapterList) {
         super(fm);
         this.context = context;
         activity = (Activity) context;
         this.foodAdapterList = foodAdapterList;
     }
 
     public static void updateBalance()
     {
         String parentClass = context.getClass().getSimpleName();
         if(parentClass.equals("SandwichActivity"))
         {
             SandwichActivity.updateBalance();
         } else if(parentClass.equals("VistaActivity"))
         {
             VistaActivity.updateBalance();
         }
     }
 
     @Override
     public Fragment getItem(int i)
     {
        Fragment fragment = new MyFragment();
        Bundle args = new Bundle();
        args.putInt(MyFragment.ARG_POSITION, i);
        fragment.setArguments(args);
        return fragment;
     }
 
     @Override
     public int getCount()
     {
         return foodAdapterList.size();
     }
 
     @Override
     public CharSequence getPageTitle(int position)
     {
         return foodAdapterList.get(position).getTitle();
     }
     @Override
     public int getItemPosition(Object object)
     {
         return POSITION_NONE;
     }
 }
