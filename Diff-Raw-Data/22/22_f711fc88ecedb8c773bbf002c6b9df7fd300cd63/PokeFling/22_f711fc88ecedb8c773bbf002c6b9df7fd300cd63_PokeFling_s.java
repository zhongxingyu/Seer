 package com.example.pokemonfractions;
 
 import helpers.Location;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import mathClasses.GenerateBar;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.pm.ActivityInfo;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.Menu;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.animation.OvershootInterpolator;
 import android.widget.FrameLayout;
 import android.widget.TextView;
 
 public class PokeFling extends Activity {
 
 	private static final String DEBUG_TAG = "Fling";
     private Matrix animateStart;
     private OvershootInterpolator animateInterpolator;
     private long startTime;
     private long endTime;
     private float totalAnimDx;
     private float totalAnimDy;
     private GenerateBar fractions = new GenerateBar(10);
     
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    setContentView(R.layout.activity_poke_fling);
 	    FrameLayout frame = (FrameLayout) findViewById(R.id.graphics_holder);
 	    PlayAreaView image = new PlayAreaView(this);
 	    this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
 	    frame.addView(image);
 	}
 	
 	 protected void onResume()
      {
      	super.onResume();
      }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_poke_fling, menu);
         return true;
     }
     
  
     private class PlayAreaView extends View {
 
         private GestureDetector gestures;
         private Matrix translate;
         private Bitmap ball,dex;
 //        private Bitmap icon;
 
         private Matrix animateStart;
         private OvershootInterpolator animateInterpolator;
         private long startTime;
         private long endTime;
         private float totalAnimDx;
         private float totalAnimDy;
         private float cx,cy;
         private float[] centerArray = { 1, 0, 400, 0, 1, 250, 0, 0, 1 };
         private int[] fPics = {R.drawable.f0,R.drawable.f1,R.drawable.f2,R.drawable.f3,R.drawable.f4,R.drawable.f5,R.drawable.f6,
         		R.drawable.f7,R.drawable.f8,R.drawable.f9,R.drawable.f10};
         private Paint p = new Paint();
         private ArrayList<Location> icons = new ArrayList<Location>();
         
         public PlayAreaView(Context context) {
             super(context);
             translate = new Matrix();
             translate.setValues(centerArray);
             gestures = new GestureDetector(PokeFling.this, new GestureListener(this));
             ball = BitmapFactory.decodeResource(getResources(), R.drawable.pokeball);
             dex = BitmapFactory.decodeResource(getResources(), R.drawable.pokedex1);
             createLocations();
             p.setTextSize(40);
             p.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
             
             
         }
 
         public void onAnimateMove(float dx, float dy, long duration) {
             animateStart = new Matrix(translate);
             animateInterpolator = new OvershootInterpolator();
             startTime = System.currentTimeMillis();
             endTime = startTime + duration;
             totalAnimDx = dx;
             totalAnimDy = dy;
             post(new Runnable() {
                 public void run() {
                     onAnimateStep();
                 }
             });
         }
 
         private void onAnimateStep() {
             long curTime = System.currentTimeMillis();
             float percentTime = (float) (curTime - startTime) / (float) (endTime - startTime);
             float percentDistance = animateInterpolator.getInterpolation(percentTime);
             float curDx = percentDistance * totalAnimDx;
             float curDy = percentDistance * totalAnimDy;
             translate.set(animateStart);
             onMove(curDx, curDy);
 
             //Log.v(DEBUG_TAG, "We're " + percentDistance + " of the way there!");
             if (percentTime < 1.0f) 
             {
                 post(new Runnable() {
                     public void run() {
                         onAnimateStep();
                     }
                 });
             }
         }
 
         public void onMove(float dx, float dy) {        	
             translate.postTranslate(dx, dy);
             invalidate();
         }
 
         public void onResetLocation() {
             translate.setValues(centerArray);
             invalidate();
         }
 
         public void onSetLocation(float dx, float dy) {
             translate.postTranslate(dx, dy);
         }
     	public ArrayList<Integer> randomAnswers(int num,int total,int fraction)
     	{
     		Random r = new Random();
     		ArrayList<Integer> ans = new ArrayList<Integer>();
     		for(int i = 0; i < num; i ++)
     		{
     			int temp = (int)(r.nextDouble()*total+1);
     			while(temp == fraction)
     				temp = (int)(r.nextDouble()*total+1);
     			ans.add(temp);
     		}
     		return ans;
     		
     	}
     	
     	public void setTexts(Canvas canvas)
     	{
     		for(Location l : icons)
     		{
     			float x = l.getX();
     			float y = l.getY()-30;
    			canvas.drawText(fractions.getAnswerString(),x , y, p);
     		}
     		
     	}
          
         public void createLocations()
         {
         	int[] icon = {R.drawable.squirtle,R.drawable.gyrados,R.drawable.snorlax,R.drawable.onix};
         	for(int i = 0; i < 4; i++)
         	{
         		Bitmap b = BitmapFactory.decodeResource(getResources(), icon[i]);
         		Location l = new Location(b,i+1);
         		Log.d(DEBUG_TAG, "creating locations");
         		icons.add(l);
         	}
         }
       
         @Override
         protected void onDraw(Canvas canvas) {
             // Log.v(DEBUG_TAG, "onDraw");
         	canvas.drawBitmap(ball, translate, null);
             
             Matrix m = canvas.getMatrix();
             
             for(Location l : icons)
             {
             	l.setLocation(canvas.getWidth(), canvas.getHeight());
             	l.draw(canvas);
             }
             //setFractions();
             int num = fractions.getAnswer();
             Log.d(DEBUG_TAG, "fraction" + num);
             Bitmap b = BitmapFactory.decodeResource(getResources(), fPics[num]);
             canvas.drawBitmap(b, 10, 10, null);
             
             setTexts(canvas);
             float tempY = canvas.getHeight() - dex.getHeight();
 
 //            Log.d(DEBUG_TAG, "Matrix: " + translate.toShortString());
             //Log.d(DEBUG_TAG, "Canvas: " + m.toShortString());
         }
 
         @Override
         public boolean onTouchEvent(MotionEvent event) {
             return gestures.onTouchEvent(event);
         }
         
         private class GestureListener implements GestureDetector.OnGestureListener,GestureDetector.OnDoubleTapListener {
         	PlayAreaView view;
         	
         	public GestureListener(PlayAreaView view)
         	{
         		this.view = view;
         	}
         	
         	
         	public boolean onDown(MotionEvent e) {
         		return true;
         	}
 
         	public boolean onDoubleTap(MotionEvent e) {
         	    Log.v(DEBUG_TAG, "onDoubleTap");
         	    view.onResetLocation();
         	    return true;
         	}
 
     		public boolean onDoubleTapEvent(MotionEvent e) {
     			// TODO Auto-generated method stub
     			return false;
     		}
 
     		public boolean onSingleTapConfirmed(MotionEvent e) {
     			Log.v( DEBUG_TAG, "singleTap" ); 
     			final float dx = getX();
     			final float dy = getY();
     			view.onMove( -dx, -dy );
     			return true;
     			//return false;
     		}
 
 
     		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
     		    Log.v(DEBUG_TAG, "onFling");
     		    final float distanceTimeFactor = 0.4f;
     		    final float totalDx = (distanceTimeFactor * velocityX/2);
     		    final float totalDy = (distanceTimeFactor * velocityY/2);
     		    view.onAnimateMove(totalDx, totalDy,(long) (1000 * distanceTimeFactor));
     		    return true;
     		}
 
 
     		public void onLongPress(MotionEvent e) {
     			// TODO Auto-generated method stub
     			
     		}
 
     		public boolean onScroll(MotionEvent e1, MotionEvent e2,
     		    float distanceX, float distanceY) {
     		    Log.v(DEBUG_TAG, "onScroll");
     		    view.onMove(-distanceX, -distanceY);
     		    return true;
     		}
 
     		public void onShowPress(MotionEvent e) {
     			// TODO Auto-generated method stub
     			
     		}
 
     		public boolean onSingleTapUp(MotionEvent e) {
 
     			return false;
     		}
 
     }
 
 
     }
 
 
 }
