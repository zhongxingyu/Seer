 package jp.ddo.neko_daisuki.android.widget;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 
 import jp.ddo.neko_daisuki.android.view.MotionEventDispatcher;
 
 public class RotatingUzumakiSlider extends UzumakiSlider {
 
     public interface OnStartRotatingListener {
 
         public void onStartRotating(RotatingUzumakiSlider slider);
     }
 
     public interface OnStopRotatingListener {
 
         public void onStopRotating(RotatingUzumakiSlider slider);
     }
 
     private abstract class MotionEventProc implements MotionEventDispatcher.Proc {
 
         private RotatingUzumakiSlider slider;
 
         public MotionEventProc(RotatingUzumakiSlider slider) {
             this.slider = slider;
         }
 
         public boolean run(MotionEvent event) {
             return this.callback(this.slider, event);
         }
 
         protected abstract boolean callback(RotatingUzumakiSlider slider, MotionEvent event);
     }
 
     private class ActionDownProc extends MotionEventProc {
 
         public ActionDownProc(RotatingUzumakiSlider slider) {
             super(slider);
         }
 
         protected boolean callback(RotatingUzumakiSlider slider, MotionEvent event) {
             return slider.onActionDown(event);
         }
     }
 
     private class ActionMoveProc extends MotionEventProc {
 
         public ActionMoveProc(RotatingUzumakiSlider slider) {
             super(slider);
         }
 
         protected boolean callback(RotatingUzumakiSlider slider, MotionEvent event) {
             return slider.onActionMove(event);
         }
     }
 
     private class ActionUpProc extends MotionEventProc {
 
         public ActionUpProc(RotatingUzumakiSlider slider) {
             super(slider);
         }
 
         protected boolean callback(RotatingUzumakiSlider slider, MotionEvent event) {
             return slider.onActionUp(event);
         }
     }
 
     private static final String LOG_TAG = "RotatingUzumakiSlider";
 
     private List<OnStartRotatingListener> onStartRotatingListeners;
     private List<OnStopRotatingListener> onStopRotatingListeners;
     private MotionEventDispatcher dispatcher;
     private double rotationAngle;   // [degree]
 
     private int headerSize;
 
     public RotatingUzumakiSlider(Context context) {
         super(context);
         this.initialize();
     }
 
     public RotatingUzumakiSlider(Context context, AttributeSet attrs) {
         super(context, attrs);
         this.initialize();
     }
 
     public RotatingUzumakiSlider(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
         this.initialize();
     }
 
     public boolean onTouchEvent(MotionEvent event) {
         return this.dispatcher.dispatch(event);
     }
 
     public void addOnStartRotatingListener(OnStartRotatingListener listener) {
         this.onStartRotatingListeners.add(listener);
     }
 
     public void addOnStopRotatingListener(OnStopRotatingListener listener) {
         this.onStopRotatingListeners.add(listener);
     }
 
     public void placeHead(float pointerX, float pointerY) {
         int outerRadius = this.getAbsoluteOuterDiameter() / 2;
         float len = this.getWidth() / 2 + outerRadius - pointerX;
         int innerRadius = this.getAbsoluteInnerDiameter() / 2;
         int maxLen = outerRadius - innerRadius;
         int min = this.getMin();
         float progress = len * this.getSize() / maxLen + min;
         int n = (int)Math.max(Math.min(progress, this.getMax()), min);
         this.setProgress(n);
     }
 
     public void onDraw(Canvas canvas) {
         super.onDraw(canvas);
         this.drawTie(canvas);
         this.drawRotatingUzumaki(canvas);
         //this.drawHeader(canvas);
     }
 
     protected void layoutHead(View head, int l, int t, int r, int b) {
         int x = this.computeHeadPosition();
         int y = this.getHeight() / 2;
         ((UzumakiHead)head).movePointer(l + x, t + y, l, t, r, b);
     }
 
     private void drawHeader(Canvas canvas) {
         /*
          * I do not remove this unused method because this is useful in
          * debugging.
          */
         Path path = new Path();
         path.moveTo(this.computeHeadPosition(), this.getHeight() / 2);
         path.rLineTo(this.headerSize, - this.headerSize);
         path.rLineTo(-2 * this.headerSize, 0);
         path.close();
 
         Paint paint = new Paint();
         paint.setColor(Color.YELLOW);
         paint.setStyle(Paint.Style.FILL);
         canvas.drawPath(path, paint);
     }
 
     private void initialize() {
         this.onStartRotatingListeners = new ArrayList<OnStartRotatingListener>();
         this.onStopRotatingListeners = new ArrayList<OnStopRotatingListener>();
         this.dispatcher = new MotionEventDispatcher();
         this.dispatcher.setDownProc(new ActionDownProc(this));
         this.dispatcher.setMoveProc(new ActionMoveProc(this));
         this.dispatcher.setUpProc(new ActionUpProc(this));
 
         this.headerSize = 42;
     }
 
     private int computeHeadPosition() {
         int center = this.getWidth() / 2;
         int innerRadius = this.getAbsoluteInnerDiameter() / 2;
         int step = this.getMax() - this.getProgress();
         int outerRadius = this.getAbsoluteOuterDiameter() / 2;
         int span = (outerRadius - innerRadius) * step / this.getSize();
         return center + innerRadius + span;
     }
 
     private float computeCurrentAngle() {
         int progress = this.getProgress() - this.getMin();
         return this.getSweepAngle() * (float)progress / this.getSize();
     }
 
     private void drawRotatingUzumaki(Canvas canvas) {
         int x = this.getWidth() / 2;
         int y = this.getHeight() / 2;
         float angle = this.computeCurrentAngle();
         canvas.save();
         try {
             canvas.rotate(- angle, x, y);
             this.drawUzumaki(canvas);
         }
         finally {
             canvas.restore();
         }
     }
 
    private float getEventX(MotionEvent event) {
        return event.getX(event.getPointerId(0));
    }

    private float getEventY(MotionEvent event) {
        return event.getY(event.getPointerId(0));
    }

     private boolean onActionDown(MotionEvent event) {
         int centerX = this.getWidth() / 2;
         int centerY = this.getHeight() / 2;
        float x = this.getEventX(event);
        float y = this.getEventY(event);
         float deltaX = x - centerX;
         float deltaY = y - centerY;
         double len = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
         int outerRadius = this.getOutlineOuterDiameter() / 2;
         int innerRadius = this.getAbsoluteOutlineInnerDiameter() / 2;
         boolean isTarget = (innerRadius <= len) && (len <= outerRadius);
 
         return isTarget ? this.startRotating(x, y) : false;
     }
 
     private double computeRotationAngle(float x, float y) {
         int centerX = this.getWidth() / 2;
         int centerY = this.getHeight() / 2;
         float deltaX = x - centerX;
         float deltaY = y - centerY;
         double radian = Math.atan2(deltaY, deltaX);
         return Math.toDegrees(radian);
     }
 
     private boolean startRotating(float x, float y) {
         this.rotationAngle = this.computeRotationAngle(x, y);
 
         for (OnStartRotatingListener listener: this.onStartRotatingListeners) {
             listener.onStartRotating(this);
         }
 
         /*
          * Return value's type of this method must be void. But to make the
          * last expression of onActionDown() simple, this method always returns
          * true. This is a bad way exactlly.
          */
         return true;
     }
 
     private boolean onActionMove(MotionEvent event) {
        float x = this.getEventX(event);
        float y = this.getEventY(event);
         double angle = this.computeRotationAngle(x, y);
         double angleDelta = angle - this.rotationAngle;
         double absDelta = Math.abs(angleDelta);
         double calibration = absDelta < 180 ? 0 : angleDelta / absDelta * 360;
         double actualDelta = angleDelta - calibration;
         double progressDelta = actualDelta / this.getSweepAngle() * this.getSize() + this.getMin();
         int direction = 0 < this.getSweepAngle() ? 1 : -1;
         this.setProgress(this.getProgress() + direction * (int)progressDelta);
         this.rotationAngle = angle;
 
         return true;
     }
 
     private boolean onActionUp(MotionEvent event) {
         for (OnStopRotatingListener listener: this.onStopRotatingListeners) {
             listener.onStopRotating(this);
         }
 
         return true;
     }
 }
 
 // vim: tabstop=4 shiftwidth=4 expandtab softtabstop=4
