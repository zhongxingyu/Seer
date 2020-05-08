 /* Copyright 2012, Robert Myers */
 
 /*
  * This file is part of ComicsApp.
 
     ComicsApp is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     Comics is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with ComicsApp.  If not, see <http://www.gnu.org/licenses/>
  */
 
 package com.robandjen.comicsapp;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.preference.PreferenceManager;
 import android.util.AttributeSet;
 import android.view.GestureDetector;
 import android.view.MotionEvent;
 import android.webkit.WebView;
 
 public class ComicsWebView extends WebView implements OnSharedPreferenceChangeListener {
 
 	GestureDetector mGestureDetect;
 	ComicsEvents mEvents;
 	static final float MAXTHRESHOLDY = 500f;
 	static final float MINTHRESHOLDX = 1500f;
 	static final String GESTUREPREF = "pref_usegestures";
 	boolean mGesturesEnabled;
 	
 	//This assumes the view is the width of the screen
 	boolean handleTap(MotionEvent e) {
 		boolean rc = false;
 		if (mEvents != null) {
 			
 			//Allow links and such in the hit region
 			WebView.HitTestResult htr = getHitTestResult();
 			if (htr.getType() != WebView.HitTestResult.UNKNOWN_TYPE 
 					&& htr.getType() != WebView.HitTestResult.IMAGE_TYPE) {
 				return false;
 			}
 			
 			//Tap in 1/CLICKRANGEth of edge causes next/previous
 			final int CLICKRANGE = 10;
 			final int width = getWidth();
 			final int prevrange = width / CLICKRANGE;
 			final int nextrange = width - prevrange;
 			
 			final int xpos = (int) e.getX();
 			if (xpos <= prevrange) {
 				mEvents.onPreviousComic(this);
 				rc = true;
 			} else if (xpos >= nextrange) {
 				mEvents.onNextComic(this);
 				rc = true;
 			}
 		}
 		return rc;
 	}
 	
 	void onActivityPause() {
 		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext().getApplicationContext());
 		pref.unregisterOnSharedPreferenceChangeListener(this);
 	}
 	
 	void onActivityResume() {
 		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext().getApplicationContext());
 		pref.registerOnSharedPreferenceChangeListener(this);
 		mGesturesEnabled = pref.getBoolean(GESTUREPREF, false);
 	}
 	
 	public void setListener(ComicsEvents listener) {
 		mEvents = listener;
 	}
 	private void initDetector() {
 		mGestureDetect = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
 			@Override
 			public boolean onFling(MotionEvent e1,MotionEvent e2,float velocityX,float velocityY) {
 				boolean rc = false;
 				if (mEvents != null) {
 					if (Math.abs(velocityY) < Math.abs(velocityX) && Math.abs(velocityX) > MINTHRESHOLDX) {
 						if (velocityX < 0) {
 							mEvents.onNextComic(ComicsWebView.this);
 						}
 						else {
 							mEvents.onPreviousComic(ComicsWebView.this);
 						}
 						rc = true;
 					}
 				}
 				return rc;
 			}
 			
 			@Override
 			public boolean onSingleTapUp(MotionEvent e) {
 				return handleTap(e);
 			}
 		}
 		);
 	}
 	
 	public ComicsWebView(Context context) {
 		super(context);
 		initDetector();
 	}
 
 	public ComicsWebView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		initDetector();
 	}
 
 	public ComicsWebView(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		initDetector();
 	}
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		boolean superrc = super.onTouchEvent(event);
		boolean gesturerc = mGesturesEnabled && mGestureDetect.onTouchEvent(event);
 		
 		return superrc || gesturerc;
 	}
 	@Override
 	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
 		if (key.equals(GESTUREPREF)) {
 			mGesturesEnabled = sharedPreferences.getBoolean(key, false);
 		}
 		
 	}
 
 }
