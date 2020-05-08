 package com.osastudio.newshub.widgets;
 
 import java.util.ArrayList;
 
 import com.huadi.azker_phone.R;
 import com.osastudio.newshub.utils.Utils;
 
 import android.content.Context;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.util.AttributeSet;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ProgressBar;
 import android.widget.RelativeLayout;
 import android.widget.ViewSwitcher;
 
 public class SlidePager extends android.support.v4.view.ViewPager {
 	Context mContext = null;
 	int mSize = 0;
 	int mCurrentIndex = 0;
 	BaseAssistent mAssistant = null;
 	boolean mbLoop = false;
 	ArrayList<View> mViews = new ArrayList<View>();
 
 	public SlidePager(Context context) {
 		this(context, null);
 
 	}
 
 	public SlidePager(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		mContext = context;
 		
 	}
 
 	PagerAdapter mPagerAdapter = new PagerAdapter() {
 
 		@Override
 		public int getCount() {
 			// TODO Auto-generated method stub
 			return mSize;
 		}
 
 		@Override
 		public boolean isViewFromObject(View arg0, Object arg1) {
 			// TODO Auto-generated method stub
 			return arg0 == arg1;
 		}
 
 		public void destroyItem(android.view.ViewGroup container, int position,
 				Object object) {
 			Utils.logd("SlidePager destroyItem", "position=" + position
 					+ " object=" + object);
			((ViewPager) container).removeView(mViews.get(position));
 		}
 
 		@Override
 		public Object instantiateItem(ViewGroup container, int position) {
 			Utils.logd("SlidePager instantiateItem", "position=" + position
 					+ " object=" + mViews.get(position));
 			((ViewPager) container).addView(mViews.get(position));
 
 			return mViews.get(position);
 
 		}
 
 	};
 
 	public void setAssistant(BaseAssistent assistant) {
 		mAssistant = assistant;
 		mSize = assistant.getCount();
 		if (mSize > 0) {
 			
 			mViews.clear();
 			for (int i = 0; i < mSize; i++) {
 				View view = mAssistant.getView(i, null);
 				if (view != null) {
 					mViews.add(view);
 				}
 			}
 			setAdapter(mPagerAdapter);
 			Utils.logd("SlidePager setAssistant", "setAdapter mViews size="+mViews.size());
 		}
 		invalidate();
 	}
 	
 	
 
 	public void setAssistant(BaseAssistent assistant, int page) {
 		setAssistant(assistant);
 		if (page < mSize) {
 			setCurrentItem(page);
 		}
 	}
 
 }
