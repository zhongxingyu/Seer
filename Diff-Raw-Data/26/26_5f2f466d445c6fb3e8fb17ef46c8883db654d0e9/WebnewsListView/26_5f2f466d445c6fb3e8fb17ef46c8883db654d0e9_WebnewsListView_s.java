 /**
 See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  This code is licensed
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
 */		
 package edu.rit.csh.androidwebnews;
 
 import android.content.Context;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.GestureDetector.OnGestureListener;
 import android.view.MotionEvent;
 import android.widget.ListView;
 
 /**
  * Extends ListView to add support for swiping in from
  * the left edge to open a SlideMenu. Gesture logic
  * is in onFling()
  * @author Derek Gonyeo
  *
  */
 public class WebnewsListView extends ListView implements OnGestureListener {
 	GestureDetector gesturescanner;
 	NewsgroupListMenu newsGroupListMenu;
 	final float densityDpi = getResources().getDisplayMetrics().density;
 
 	public WebnewsListView(Context context, NewsgroupListMenu slidemenu) {
 		super(context);
 		gesturescanner = new GestureDetector(getContext(), this);
 		this.newsGroupListMenu = slidemenu;
 		Log.d("MyDebugging", "Slidemenu: " + slidemenu);
 		this.setFocusable(false);
 	}
 
 	public WebnewsListView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 	}
 
 	public WebnewsListView(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 	}
 	
 	/**
 	 * If our gesture scanner has used the MotionEvent,
 	 * return true. Otherwise return false.
 	 */
 	@Override
 	public boolean onTouchEvent(MotionEvent event)
 	{
 		if(gesturescanner.onTouchEvent(event))
 			return true;
 		else
 			return super.onTouchEvent(event);
 	}
 
 	@Override
 	public boolean onDown(MotionEvent e) {
 		return false;
 	}
 
 	/**
 	 * toggles the slidemenu if fling started at an X < 20, 
 	 * it moved in a positive direction on the X axis, and
 	 * was going at least 1000 dp/s
 	 */
 	@Override
 	public boolean onFling(MotionEvent start, MotionEvent finish, float velocityX,
 			float velocityY) {
 		Log.d("MyDebugging","onFling() called!");
		int scaledVelocity = (int) (velocityX / (densityDpi / 160f));
		float distanceX = Math.abs(start.getX() - finish.getX());
		float distanceY = Math.abs(start.getY() - finish.getY());
		if(scaledVelocity > 500 && !NewsgroupListMenu.menuShown && distanceX > 2*distanceY)
 		{
			newsGroupListMenu.show();
			return true;
		}
		if(scaledVelocity < -300 && NewsgroupListMenu.menuShown && distanceX > 2*distanceY)
		{
			newsGroupListMenu.show();
			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public void onLongPress(MotionEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
 			float distanceY) {
 		return false;
 	}
 
 	@Override
 	public void onShowPress(MotionEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public boolean onSingleTapUp(MotionEvent e) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 }
