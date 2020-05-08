 /*
  * Copyright 2013 Qidu Lin
  * 
  * This file is part of ShowMyRecentApps.
  * 
  * ShowMyRecentApps is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option) any
  * later version.
  * 
  * ShowMyRecentApps is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * ShowMyRecentApps. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.qidu.lin.showRecentApps.fg;
 
 import android.animation.Animator;
 import android.animation.AnimatorListenerAdapter;
 import android.animation.LayoutTransition;
 import android.animation.ObjectAnimator;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Rect;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Looper;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewTreeObserver.OnGlobalLayoutListener;
 import android.view.Window;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.EditText;
 
 import com.qidu.lin.showRecentApps.R;
 import com.qidu.lin.showRecentApps.bg.PackageManagerCache;
 import com.qidu.lin.showRecentApps.bg.appInfo.AppInfoManager;
 import com.qidu.lin.showRecentApps.bg.search.SearchManager;
 
 public class ShowGetRecentAppsActivity extends Activity
 {
 	private static final float esp = 100;
 	private Float lastY;
 
 	private Runnable searchRunnable = null;
 	private Handler searchHandler = new Handler(Looper.getMainLooper());
 
 	@Override
 	public boolean dispatchTouchEvent(MotionEvent ev)
 	{
 		if (ev.getAction() == MotionEvent.ACTION_DOWN)
 		{
 			if (findViewById(R.id.scrollView1).getScrollY() == 0)
 			{
 				lastY = ev.getRawY();
 			}
 		}
 		if (ev.getAction() == MotionEvent.ACTION_UP)
 		{
 			if (lastY != null)
 			{
 				if (ev.getRawY() > lastY + esp)
 				{
 					toggleKeyboard();
 				}
 
 				lastY = null;
 			}
 		}
 		return super.dispatchTouchEvent(ev);
 	}
 
 	private RecentAppsAdapter adapter = null;
 
 	void finishWithIntent(Intent intent)
 	{
 		startActivity(EMPTYActivity.getIntentToStart(this, intent));
 		finish();
 
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 
 		setContentView(R.layout.activity_test_get_recent_apps);
 
 		setupKeyboardListener();
 
 		final ViewGroup vv = (ViewGroup) findViewById(R.id.gridView1);
 
 		adapter = new RecentAppsAdapter(this, new RecentAppsLayoutOperater(vv));
 
 		final EditText searchEditText = (EditText) findViewById(R.id.searchView1);
 		searchEditText.addTextChangedListener(new TextWatcher()
 		{
 
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before, int count)
 			{
 				// empty
 			}
 
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count, int after)
 			{
 				// empty
 			}
 
 			@Override
 			public void afterTextChanged(final Editable s)
 			{
 				if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.HONEYCOMB)
 				{
 					setLayoutAnimationForHoneycomb(vv);
 				}
 
 				if (searchRunnable != null)
 				{
 					searchHandler.removeCallbacks(searchRunnable);
 					searchRunnable = null;
 				}
				final String searchkey = s.toString();
 				searchRunnable = new Runnable()
 				{
 					@Override
 					public void run()
 					{
						SearchManager.getInstance().onSearch(searchkey);
 						searchRunnable = null;
 					}
 				};
				if (searchkey.length() < 3)
				{
 				searchHandler.postDelayed(searchRunnable, 300);
				}
				else
				{
					searchRunnable.run();
				}
 			}
 		});
 
 		PackageManagerCache.setPm(getPackageManager());
 
 		AppInfoManager.getInstance().addListener(adapter);
 		SearchManager.getInstance().setSearchResultListener(adapter);
 		AppInfoManager.getInstance().refreshAsynchronized(this);
 	}
 
 	// see
 	// http://stackoverflow.com/questions/2150078/how-to-check-visibility-of-software-keyboard-in-android.
 	private void setupKeyboardListener()
 	{
 		final View activityRootView = findViewById(R.id.root);
 		activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener()
 		{
 			@Override
 			public void onGlobalLayout()
 			{
 				Rect r = new Rect();
 				// r will be populated with the coordinates of your view that
 				// area still visible.
 				activityRootView.getWindowVisibleDisplayFrame(r);
 				int heightDiff = activityRootView.getRootView().getHeight() - (r.bottom - r.top);
 				if (heightDiff > 200)
 				{
 					// probably the keyboard is shown...
 					((EditText) findViewById(R.id.searchView1)).setHint(R.string.hint_when_keyboard_is_shown);
 				}
 				else if (heightDiff < -200)
 				{
 					// probably the keyboard is hidden...
 					((EditText) findViewById(R.id.searchView1)).setHint(R.string.hint_when_keyboard_is_hidden);
 
 				}
 
 			}
 		});
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onTouchEvent(android.view.MotionEvent)
 	 */
 	@Override
 	public boolean onTouchEvent(MotionEvent event)
 	{
 		if (isEventPositionInsideRootLayout(event))
 		{
 			return super.onTouchEvent(event);
 		}
 		else
 		{
 			showKeyboard();
 			return true;
 		}
 	}
 
 	private boolean isEventPositionInsideRootLayout(MotionEvent event)
 	{
 		boolean isEventPositionInsideLayout = false;
 
 		int[] location = new int[2];
 		View vv = findViewById(R.id.root);
 		vv.getLocationOnScreen(location);
 
 		float x = event.getX();
 		float y = event.getY();
 
 		int width = vv.getWidth();
 		int height = vv.getHeight();
 		if (0 <= x && x <= width)
 		{
 			if (0 <= y && y <= height)
 			{
 				isEventPositionInsideLayout = true;
 			}
 		}
 		return isEventPositionInsideLayout;
 	}
 
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	public void setLayoutAnimationForHoneycomb(final ViewGroup vv)
 	{
 		if (vv.getLayoutTransition() == null)
 		{
 			LayoutTransition layoutTransition = new LayoutTransition();
 			vv.setLayoutTransition(layoutTransition);
 			layoutTransition.setStagger(LayoutTransition.CHANGE_APPEARING, 0);
 			layoutTransition.setStagger(LayoutTransition.CHANGE_DISAPPEARING, 20);
 
 			// Adding
 			ObjectAnimator animIn = ObjectAnimator.ofFloat(null, "scaleY", 0f, 1f).setDuration(
 					layoutTransition.getDuration(LayoutTransition.APPEARING));
 			layoutTransition.setAnimator(LayoutTransition.APPEARING, animIn);
 			animIn.addListener(new AnimatorListenerAdapter()
 			{
 				public void onAnimationEnd(Animator anim)
 				{
 					View view = (View) ((ObjectAnimator) anim).getTarget();
 					view.setScaleY(1f);
 				}
 			});
 
 			// Removing
 			ObjectAnimator animOut = ObjectAnimator.ofFloat(null, "scaleY", 1f, 0f).setDuration(
 					layoutTransition.getDuration(LayoutTransition.DISAPPEARING));
 			layoutTransition.setAnimator(LayoutTransition.DISAPPEARING, animOut);
 			animOut.addListener(new AnimatorListenerAdapter()
 			{
 				public void onAnimationEnd(Animator anim)
 				{
 					View view = (View) ((ObjectAnimator) anim).getTarget();
 					view.setScaleY(0f);
 				}
 			});
 
 			layoutTransition.setDuration(300);
 		}
 	}
 
 	private void showKeyboard()
 	{
 		EditText editText = (EditText) findViewById(R.id.searchView1);
 		InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 		// only will trigger it if no physical keyboard is open
 		mgr.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
 	}
 
 	private void toggleKeyboard()
 	{
 		InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 		mgr.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);
 	}
 
 	@Override
 	protected void onDestroy()
 	{
 		if (searchRunnable != null)
 		{
 			searchHandler.removeCallbacks(searchRunnable);
 			searchRunnable = null;
 		}
 		SearchManager.getInstance().setSearchResultListener(null);
 		AppInfoManager.getInstance().deleteListener(adapter);
 		super.onDestroy();
 	}
 
 	@Override
 	protected void onPause()
 	{
 		super.onPause();
 		finishWithIntent(null);
 	}
 }
