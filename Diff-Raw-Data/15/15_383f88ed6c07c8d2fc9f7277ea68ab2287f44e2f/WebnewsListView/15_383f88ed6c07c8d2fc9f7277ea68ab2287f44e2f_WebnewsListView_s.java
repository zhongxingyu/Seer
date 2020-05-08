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
 		super.onTouchEvent(event);
 		if(gesturescanner.onTouchEvent(event))
 			return true;
 		else
 			return false;
 	}
 
 	@Override
 	public boolean onDown(MotionEvent e) {
 		return true;
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
		if(start.getRawX() < 150  && scaledVelocity > 300 && !NewsgroupListMenu.menuShown && distanceX > distanceY)
 		{
 			newsGroupListMenu.show();
 			return true;
 		}
 		if(scaledVelocity < -300 && NewsgroupListMenu.menuShown && distanceX > distanceY)
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
