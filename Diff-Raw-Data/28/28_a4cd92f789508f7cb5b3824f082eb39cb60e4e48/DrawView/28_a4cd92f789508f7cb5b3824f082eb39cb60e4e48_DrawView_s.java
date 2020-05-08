 package br.unicamp.signunlock;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import static android.graphics.Color.BLACK;
 
 public class DrawView extends View implements OnTouchListener {
     private static final String TAG = "DrawView";
     Context context;
     List<DrawPoint> points = new ArrayList<DrawPoint>();
     Paint paint;
     Path path;
    Canvas canvas;
     Timer timer;
     long delay = 1000 * 3;
    SignPreProcess processedSignature;
    double[] featureVector;
 
     public DrawView(Context context) {
         super(context);
         this.context = context;
 
         setFocusable(true);
         setFocusableInTouchMode(true);
         this.setOnTouchListener(this);
 
         timer = new Timer();
         setupDrawing();
     }
 
     public DrawView(Context context, AttributeSet attrs) {
         super(context, attrs);
 
         this.context = context;
 
         setFocusable(true);
         setFocusableInTouchMode(true);
         this.setOnTouchListener(this);
 
         timer = new Timer();
         setupDrawing();
     }
 
 
     private void setupDrawing() {
 
         //prepare for drawing and setup paint stroke properties
         path = new Path();
         paint = new Paint();
         paint.setColor(BLACK);
         paint.setAntiAlias(true);
         paint.setStrokeWidth(5);
         paint.setStyle(Paint.Style.STROKE);
         paint.setStrokeJoin(Paint.Join.ROUND);
         paint.setStrokeCap(Paint.Cap.ROUND);

     }
 
     @Override
     protected void onSizeChanged(int w, int h, int oldw, int oldh) {
         super.onSizeChanged(w, h, oldw, oldh);
        Bitmap canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
         canvas = new Canvas(canvasBitmap);
     }
 
     @Override
     public void onDraw(Canvas canvas) {
         canvas.drawPath(path, paint);
     }
 
     public boolean onTouch(View view, MotionEvent event) {
         try {
 
             if (timer != null) {
                 timer.cancel();
                 timer.purge();
             }
 
             timer = new Timer();
             timer.schedule(new TimerTask() {
                 @Override
                 public void run() {
 
                    Log.d(TAG, "TIMEOUT");
                    path.reset();
 //                    synchronized(points) {
 //                    	processedSignature = new SignPreProcess(points);
 //                    	featureVector = processedSignature.getFeatureVector();
 //                    }
                 }
 
             }, delay);
 
         } catch (Exception e) {
 
         }
 
 
         float touchX = event.getX();
         float touchY = event.getY();
 
         points.add(new DrawPoint((event)));
 

         //respond to down, move and up events
         switch (event.getAction()) {
             case MotionEvent.ACTION_DOWN:
                 path.moveTo(touchX, touchY);
                 break;
             case MotionEvent.ACTION_MOVE:
                 path.lineTo(touchX, touchY);
                 break;
             case MotionEvent.ACTION_UP:
                 path.lineTo(touchX, touchY);
                //path.reset();
                 break;
             default:
                 return false;
         }
         //redraw
         invalidate();
         return true;
     }
 }
 
