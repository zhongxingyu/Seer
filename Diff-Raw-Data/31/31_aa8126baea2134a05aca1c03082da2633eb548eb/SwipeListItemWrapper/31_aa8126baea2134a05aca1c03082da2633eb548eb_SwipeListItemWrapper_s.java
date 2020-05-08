 /*
  * Copyright (C) 2009 Android Shuffle Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.dodgybits.android.shuffle.view;
 
 import android.R;
 import android.content.Context;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.widget.AdapterView;
 import android.widget.FrameLayout;
 import android.widget.ListView;
 
 public class SwipeListItemWrapper extends FrameLayout {
     private static final String cTag = "SwipeListItemWrapper";
 
 	private int mStartX;
 	private int mStartY;
 	private SwipeListItemListener mListener;
 	private int mPosition;
 	
 	public SwipeListItemWrapper(Context context) {
 		super(context);
 	}
 
 	public SwipeListItemWrapper(Context context, AttributeSet attrs) {
 		super(context, attrs);
 	}
 
 	public SwipeListItemWrapper(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 	}
 	
     @Override
     public boolean onInterceptTouchEvent(MotionEvent ev) {
         final int action = ev.getAction();

        final int x = (int)ev.getX();
         final int y = (int)ev.getY();
 
         boolean stealEvent = false;
         
         switch (action) {
             case MotionEvent.ACTION_MOVE:
                 Log.d(cTag, "move event");
                 if (isValidSwipe(x, y)) {
                 	stealEvent = true;
                 }
                 break;
 
             case MotionEvent.ACTION_DOWN:
                 Log.d(cTag, "down event");
                 mStartX = x;
                 mStartY = y;
                 break;
 
             case MotionEvent.ACTION_CANCEL:
                 Log.d(cTag, "cancel event");
                 mPosition = AdapterView.INVALID_POSITION;
             	// some parent component has stolen the event
             	// nothing to do
             	break;
             	
             case MotionEvent.ACTION_UP:
                 Log.d(cTag, "up event");
                 break;
         }
 
         return stealEvent;
     }	
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		if (event.getAction() == MotionEvent.ACTION_UP) {
 			// we've got a valid swipe event. Notify the listener (if any)
 			
 			if (mPosition != AdapterView.INVALID_POSITION && mListener != null) {
 				mListener.onListItemSwiped(mPosition);
 			}
 		}
 		return true;
 	}
 	
 	public void setSwipeListItemListener(SwipeListItemListener listener) {
 		mListener = listener;
 	}
 
 	/**
 	 * Check if this appears to be a swipe event.
 	 * 
 	 * Consider it a swipe if it traverses at least a third of the screen, 
	 * and doesn't deviate outside the list item.
 	 */
 	private boolean isValidSwipe(final int x, final int y) {
 		final int screenWidth = getWidth();
		boolean horizontalValid = Math.abs(x - mStartX) >= (screenWidth / 3);
		
		boolean verticalValid = false;
		if (horizontalValid) {
 			ListView list = (ListView) findViewById(R.id.list);
 			if (list != null) {
				int startPosition = list.pointToPosition(mStartX, mStartY);
				int endPosition = list.pointToPosition(x, y); 
				if (startPosition != AdapterView.INVALID_POSITION &&
					startPosition == endPosition) {
					verticalValid = true;
					mPosition = startPosition;
				} else {
					mPosition = AdapterView.INVALID_POSITION;
				}
 			}
 		}
 		
 		Log.d(cTag, "isValidSwipe hValid=" + horizontalValid + 
 				" vValid=" + verticalValid + " position=" + mPosition);
         
 		return horizontalValid && verticalValid;
 	}
 
 }
