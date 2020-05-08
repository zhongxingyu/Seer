 package com.tools.tvguide.adapters;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.os.Parcelable;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
import android.util.Log;
 import android.view.View;
 
 public class ResultPageAdapter extends PagerAdapter 
 {
     private List<View> mViewList = new ArrayList<View>();
     
     public void addView(View view)
     {
         mViewList.add(view);
     }
     
     public void setView(int index, View view)
     {
         mViewList.set(index, view);
     }
     
     public View getView(int position)
     {
         return mViewList.get(position);
     }
     
     public void clear()
     {
         mViewList.clear();
     }
         
     @Override
     public void destroyItem(View arg0, int arg1, Object arg2) 
     {
        Log.d("ResultPageAdapter", "destroyItem");
         ((ViewPager) arg0).removeView(mViewList.get(arg1));
     }
 
     @Override
     public void finishUpdate(View arg0) 
     {
        Log.d("ResultPageAdapter", "finishUpdate");
     }
 
     @Override
     public int getCount() 
     {
        Log.d("ResultPageAdapter", "getCount");
         return mViewList.size();
     }
 
     @Override
     public Object instantiateItem(View arg0, int arg1) 
     {
        Log.d("ResultPageAdapter", "instantiateItem");
         ((ViewPager) arg0).addView(mViewList.get(arg1), 0);
         return mViewList.get(arg1);
     }
 
     @Override
     public boolean isViewFromObject(View arg0, Object arg1) 
     {
        Log.d("ResultPageAdapter", "isViewFromObject");
         return arg0 == (arg1);
     }
 
     @Override
     public void restoreState(Parcelable arg0, ClassLoader arg1) 
     {
        Log.d("ResultPageAdapter", "restoreState");
     }
 
     @Override
     public Parcelable saveState() 
     {
        Log.d("ResultPageAdapter", "saveState");
         return null;
     }
 
     @Override
     public void startUpdate(View arg0) 
     {
        Log.d("ResultPageAdapter", "startUpdate");
     }
 
 }
