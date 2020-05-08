 package hk.hku.cs.srli.supermonkey;
 
 import android.animation.Animator;
 import android.animation.AnimatorSet;
 import android.animation.ObjectAnimator;
 import android.animation.ValueAnimator;
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.graphics.RectF;
 import android.graphics.drawable.ShapeDrawable;
 import android.graphics.drawable.shapes.OvalShape;
 import android.util.Log;
 import android.view.View;
 
 public class CalibrationView extends View implements ValueAnimator.AnimatorUpdateListener {
     
     private DotWrapper dot;
     
     private Animator.AnimatorListener listener;
 
     public CalibrationView(Context context) {
         super(context);
        // Enter low profile mode to avoid UI distraction.
        setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
         dot = new DotWrapper();
         // Put the dot outside the top left corner.
         dot.setX(0 - dot.DEFAULT_R);
         dot.setY(0 - dot.DEFAULT_R);
         dot.setR(dot.DEFAULT_R);
         listener = null;
     }
     
     public void setListener(Animator.AnimatorListener listener) {
         this.listener = listener;
     }
     
     public void movePointTo(float x, float y) {
         // Convert from normalized coordinates to position on screen.
         float destx = x * getWidth();
         float desty = y * getHeight();
         Log.v("CalibrationView", "movePointTo: " + destx + ", " + desty);
         startAnimation(animatedMoveTo(destx, desty), 1000);
     }
     
     public void shrinkPoint() {
         Log.v("CalibrationView", "shrinkPoint");
         startAnimation(animatedResize(4), 1000);
     }
     
     public void expandPoint() {
         Log.v("CalibrationView", "expandPoint");
         startAnimation(animatedResize(dot.DEFAULT_R), 500);
     }
     
     public void hidePoint() {
         Log.v("CalibrationView", "hidePoint");
         // Remove the point from view.
         startAnimation(animatedResize(0), 500);
     }
     
     private Animator animatedMoveTo(float x, float y) {
         ObjectAnimator oax = ObjectAnimator.ofFloat(dot, "x", dot.x, x);
         ObjectAnimator oay = ObjectAnimator.ofFloat(dot, "y", dot.y, y);
         oax.addUpdateListener(this);
         AnimatorSet aset = new AnimatorSet();
         aset.playTogether(oax, oay);
         return aset;
     }
     
     private Animator animatedResize(float size) {
         ObjectAnimator oar = null;
         oar = ObjectAnimator.ofFloat(dot, "r", dot.r, size);
         oar.addUpdateListener(this);
         return oar;
     }
     
     private void startAnimation(Animator ani, long duration) {
         ani.setDuration(duration);
         ani.setStartDelay(500);
         if (listener != null) ani.addListener(listener);
         ani.start();
     }
     
     @Override
     protected void onDraw(Canvas canvas) {
         super.onDraw(canvas);
         dot.draw(canvas);
     }
 
     @Override
     public void onAnimationUpdate(ValueAnimator animation) {
         invalidate();
     }
     
     private class DotWrapper {
         
         // Default radius is 20dp.
         public final float DEFAULT_R = 20 * getResources().getDisplayMetrics().density;
         
         private ShapeDrawable drawable;
         private Paint bgPaint;
         
         private float r, x, y;
         
         public DotWrapper() {
             this.r = DEFAULT_R;
             this.x = 0;
             this.y = 0;
 
             drawable = new ShapeDrawable(new OvalShape());
             drawable.getPaint().setColor(0xFF74AC23);
             
             bgPaint = new Paint();
             bgPaint.setColor(Color.BLACK);
         }
         
         public void setX(float x) {
             this.x = x;
         }
         
         public void setY(float y) {
             this.y = y;
         }
         
         public void setR(float r) {
             this.r = r;
         }
         
         public void draw(Canvas canvas) {
             updateDrawable();
             drawable.draw(canvas);
             
             // Draw a dot at the center of the shape.
             canvas.drawOval(new RectF(x - 2, y - 2, x + 2, y + 2), bgPaint);
         }
         
         private void updateDrawable() {
             int left = Math.round(x - r);
             int top = Math.round(y - r);
             int right = Math.round(x + r);
             int bottom = Math.round(y + r);
             drawable.setBounds(new Rect(left, top, right, bottom));
         }
     }
 }
