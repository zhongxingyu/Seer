 /**
  * This file is part of Plingnote.
  * Copyright (C) 2012 Barnabas Sapan
  * 
  * Plingnote is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.plingnote;
 
 import android.content.Context;
 import android.graphics.Rect;
 import android.support.v4.view.ViewPager;
 import android.view.MotionEvent;
 
 /**
  * A custom ViewPager that supports horizontal 
  * scrolling inside the view (tab). This class
  * decides if the user wanted to scroll to change
  * tab or just to scroll inside the current view.
  * If this class decides that the user wanted to
  * change view (tab), it consumes the MotionEvent.
  * Otherwise it passes the MotionEvent on to other
  * classes. The default behavior is that a swipe to
  * change view is intercepted if it starts from the
  * edges of the screen.
  */
 public class ScrollableViewPager extends ViewPager {
 	private final int LEFT_DEAD_ZONE_PIXELS = 30;
 	private final int RIGHT_DEAD_ZONE_PIXELS = 30;
 
 	private boolean pagingEnabled;
 	private boolean scrollablePaging = true;
 	private Context context;
 	private Rect disabledScrollRect = new Rect();
 	
 	public ScrollableViewPager(Context context) {
 		super(context);
 		this.context = context;
 		this.pagingEnabled = false;
 		Rect r = Utils.getScreenPixels(this.context);
 		this.disabledScrollRect.set(this.LEFT_DEAD_ZONE_PIXELS, 0, r.width() - this.RIGHT_DEAD_ZONE_PIXELS, r.height());
 	}
 	
 	/**
 	 * @param context the context
 	 * @param scrollablePaging edge page swipe to change tabs
 	 */
 	public ScrollableViewPager(Context context, boolean scrollablePaging){
 		this(context);
 		this.scrollablePaging = scrollablePaging;
 	}
 	
 	/**
 	 * Handle the touch event. If we decided that we
 	 * want to scroll we call on super to handle it
 	 * for us. Otherwise if we do not want to scroll
	 * we just return false. 
	 * Inside try catch as it sometimes throws
	 * nasty exceptions from the super call. 
	 * This is probably because something is wrong 
	 * in the underlying Android framework. 
 	 */
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
		try {
			if(this.pagingEnabled)
				return super.onTouchEvent(event);
		} catch (Exception e) {	}
 		
 		return false;
 	}
 	/**
 	 * Intercept the touch events and if we decide that
 	 * a swipe started from the edges of the screen, 
 	 * consume the event.
 	 */
 	@Override
 	public boolean onInterceptTouchEvent(MotionEvent event) {
 		if(scrollablePaging) {
 			if(disabledScrollRect.contains((int)event.getX(), (int)event.getY()) == false) {
 				pagingEnabled = true;
 				return true;
 			} else
 				pagingEnabled = false;
 		}
 		
 		return false;
 	}
 	/**
 	 * This enables swipe between tabs if the swipe is
 	 * originated from either side of the edges
 	 * @param enabled edge page swipe to change tabs
 	 */
 	 public void setEdgeSwipePagingEnabled(boolean enabled) {
 	        this.scrollablePaging = enabled;
 	 }
 	 
 	 /**
 	  * @return if edge swipe is enabled for this pager
 	  */
 	 public boolean isEdgeSwipePagingEnabled(){
 		 return this.scrollablePaging;
 	 }
 }
