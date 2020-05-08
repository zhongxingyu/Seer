 package com.example.flipper;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.view.GestureDetector;
 import android.view.GestureDetector.OnGestureListener;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MotionEvent;
 import android.view.ViewGroup.LayoutParams;
 import android.view.animation.Animation;
 import android.view.animation.OvershootInterpolator;
 import android.view.animation.TranslateAnimation;
 import android.widget.TextView;
 import android.widget.ViewFlipper;
 
 public class MainActivity extends Activity implements OnGestureListener,GestureDetector.OnDoubleTapListener{
 
 	final private int SWIPE_MIN_DISTANCE=100;
 	final private int SWIPE_MIN_VELOCITY=100;
 	
 	private ViewFlipper flipper=null;
 	private ArrayList<TextView> views=null;
 	private GestureDetector gesturedetector;
 	private Vibrator vibrator=null;
 	int colors[]={Color.rgb(255, 128, 128),
 			Color.rgb(128, 255, 128),
 			Color.rgb(128, 128, 255),
 			Color.rgb(128, 128, 128)
 	};
 	
 	private Animation animleftin=null;
 	private Animation animleftout=null;
 	
 	private Animation animrightin=null;
 	private Animation animrightout=null;
 	
 	private Animation animupin=null;
 	private Animation animupout=null;
 	
 	private Animation animdownin=null;
 	private Animation animdownout=null;
 	
 	private boolean isDaragMode=false;
 	private int currentview=0;
 	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		flipper=new ViewFlipper(this);
 		gesturedetector=new GestureDetector(this,this);
 		vibrator=(Vibrator)getSystemService(VIBRATOR_SERVICE);
 		gesturedetector.setOnDoubleTapListener(this);
 		
 		flipper.setInAnimation(animleftin);
 		flipper.setOutAnimation(animleftout);
 		flipper.setFlipInterval(3000);
 		flipper.setAnimateFirstView(true);
 		
 		prepareAnimations();
 		prepareViews();
 		addView();
 		setViewText();
 		
 		setContentView(flipper);
 	}
 
 	private void setViewText() {
 		String text=getString(isDaragMode?R.string.app_info_drag: R.string.app_info_flip);
 		for(int index=0;index<views.size();++index)
 		{
 			views.get(index).setText(text);
 		}
 		
 	}
 
 	private void addView() {
 		for(int index=0;index<views.size();++index)
 		{
 			flipper.addView(views.get(index),index,new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
 		}
 		
 	}
 
 	private void prepareViews() {
 		TextView view=null;
 		
 		views=new ArrayList<TextView>();
 		
 		for(int color: colors)
 		{
 			view=new TextView(this);
 			
 			view.setBackgroundColor(color);
 			view.setTextColor(Color.BLACK);
 			view.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
 			views.add(view);
 		}
 		
 	}
 
 	private void prepareAnimations() {
 		animleftin=new TranslateAnimation(Animation.RELATIVE_TO_PARENT, +1.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
 				Animation.RELATIVE_TO_PARENT,+0.0f,Animation.RELATIVE_TO_PARENT,0.0f);
 		
 		animleftout=new TranslateAnimation(Animation.RELATIVE_TO_PARENT, +0.0f, Animation.RELATIVE_TO_PARENT, -1.0f,
 				Animation.RELATIVE_TO_PARENT,+0.0f,Animation.RELATIVE_TO_PARENT,0.0f);
 		
 		animrightin=new TranslateAnimation(Animation.RELATIVE_TO_PARENT, -1.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
 				Animation.RELATIVE_TO_PARENT,+0.0f,Animation.RELATIVE_TO_PARENT,0.0f);
 		
 		animrightout=new TranslateAnimation(Animation.RELATIVE_TO_PARENT, +0.0f, Animation.RELATIVE_TO_PARENT, +1.0f,
 				Animation.RELATIVE_TO_PARENT,+0.0f,Animation.RELATIVE_TO_PARENT,0.0f);
 		
 		animupin=new TranslateAnimation(Animation.RELATIVE_TO_PARENT, +0.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
 				Animation.RELATIVE_TO_PARENT,+1.0f,Animation.RELATIVE_TO_PARENT,0.0f);
 		
 		animupout=new TranslateAnimation(Animation.RELATIVE_TO_PARENT, +0.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
 				Animation.RELATIVE_TO_PARENT,+0.0f,Animation.RELATIVE_TO_PARENT,-1.0f);
 		
 		animdownin=new TranslateAnimation(Animation.RELATIVE_TO_PARENT, +0.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
 				Animation.RELATIVE_TO_PARENT,-1.0f,Animation.RELATIVE_TO_PARENT,0.0f);
 		
 		animdownout=new TranslateAnimation(Animation.RELATIVE_TO_PARENT, +0.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
 				Animation.RELATIVE_TO_PARENT,+0.0f,Animation.RELATIVE_TO_PARENT,+1.0f);
 		
 		animleftin.setDuration(1000);
 		animleftin.setInterpolator(new OvershootInterpolator());
 		animleftout.setDuration(1000);
 		animleftout.setInterpolator(new OvershootInterpolator());
 		
 		animrightin.setDuration(1000);
 		animrightin.setInterpolator(new OvershootInterpolator());
 		animrightout.setDuration(1000);
 		animrightout.setInterpolator(new OvershootInterpolator());
 		
 		animupin.setDuration(1000);
 		animupin.setInterpolator(new OvershootInterpolator());
 		animupout.setDuration(1000);
 		animupout.setInterpolator(new OvershootInterpolator());
 		
 		animdownin.setDuration(1000);
 		animdownin.setInterpolator(new OvershootInterpolator());
 		animdownout.setDuration(1000);
 		animdownout.setInterpolator(new OvershootInterpolator());
 		
 		
 		
 		
 		
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onDoubleTap(MotionEvent arg0) {
 		flipper.scrollTo(0, 0);
 		return false;
 	}
 
 	@Override
 	public boolean onDoubleTapEvent(MotionEvent arg0) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean onSingleTapConfirmed(MotionEvent arg0) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean onDown(MotionEvent arg0) {
 		
 		return false;
 	}
 
 	@Override
 	public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX,
 			float velocityY) {
 		if(isDaragMode)
 		{
 			return false;
 		}
 		
 		final float ev1x=event1.getX();
 		final float ev1y=event1.getY();
 		final float ev2x=event2.getX();
 		final float ev2y=event2.getY();
 		
 		final float xdiff=Math.abs(ev1x-ev2x);
 		final float ydiff=Math.abs(ev2y-ev1y);
 		
 		final float xvelocity=Math.abs(velocityX);
 		final float yvelocity=Math.abs(velocityY);
 		
 		if(xvelocity > this.SWIPE_MIN_VELOCITY && xdiff > this.SWIPE_MIN_DISTANCE)
 		{
 			if(ev1x>ev2x)
 			{
 				--currentview;
 				
 				if(currentview < 0){currentview = views.size()-1;}
 				
 				flipper.setInAnimation(animleftin);
 				flipper.setOutAnimation(animleftout);
 			}
 			else
 			{
 				++currentview;
 				if(currentview >= views.size())currentview=0;
 				
 				flipper.setInAnimation(animrightin);
 				flipper.setOutAnimation(animrightout);
 			}
 			
 			flipper.scrollTo(0,0);
 			flipper.setDisplayedChild(currentview);
 		}
 		else if(yvelocity > this.SWIPE_MIN_VELOCITY && ydiff > this.SWIPE_MIN_DISTANCE)
 		{
 			if(ev1y > ev2y)
 			{
 				--currentview;
 				if(currentview < 0)
 				{
 					currentview=views.size()-1;
 				}
 				flipper.setInAnimation(animupin);
 				flipper.setOutAnimation(animupout);
 			}
 			else
 			{
 				++currentview;
 				
 				if(currentview >=views.size())currentview=0;
 				
 				flipper.setInAnimation(animdownin);
 				flipper.setOutAnimation(animdownout);
 			}
 			flipper.scrollTo(0, 0);
 			flipper.setDisplayedChild(currentview);
 		}
 		
 		
 		return false;
 	}
 
 	@Override
 	public void onLongPress(MotionEvent arg0) {
 		
 		vibrator.vibrate(200);
 		flipper.scrollTo(0, 0);
 		
 		isDaragMode=!isDaragMode;
 		
 		setViewText();
 		
 		
 	}
 
 	@Override
 	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
 			float distanceY) {
 		if(isDaragMode)flipper.scrollBy((int)distanceX,(int)distanceY);
 		
 		return false;
 	}
 
 	@Override
 	public void onShowPress(MotionEvent arg0) {
 		
 		
 	}
 
 	@Override
 	public boolean onSingleTapUp(MotionEvent arg0) {
 		
 		return false;
 	}
 	@Override
 	public boolean onTouchEvent(MotionEvent event)
 	{
 		return gesturedetector.onTouchEvent(event);
 	}
 
 }
