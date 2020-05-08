 package org.adw.launcher;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.graphics.Typeface;
 import android.util.AttributeSet;
 import android.widget.TextView;
 
 public class CounterTextView extends TextView {
     //ADW custom notifier counters
     private String mCounter=null;
     private int mCounterSize=0;
     private int mCounterPadding=0;
     private final Rect mRect2 = new Rect();
     private Paint mStrokePaint;
     private Paint mTextPaint;
 
     public CounterTextView(Context context) {
         super(context);
         init();
     }
 
     public CounterTextView(Context context, AttributeSet attrs) {
         super(context, attrs);
         init();
     }
 
     public CounterTextView(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
         init();
     }
     private void init(){
         mStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
         mStrokePaint.setARGB(255, 255, 0, 0);
 
         mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
         mTextPaint.setARGB(255, 255, 255, 255);
         //mTextPaint.setTextAlign(Paint.Align.CENTER);
         mTextPaint.setTextSize(getContext().getResources().getDimensionPixelSize(R.dimen.counter_font_size));
         mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
         mTextPaint.setShadowLayer(2f, 1, 1, 0xFF000000);
         mCounterPadding=getContext().getResources().getDimensionPixelSize(R.dimen.counter_circle_padding);
     }
     public void setCounter(int counter){
         if(counter>0){
             mCounter=String.valueOf(counter);
             mTextPaint.getTextBounds(mCounter, 0, mCounter.length(), mRect2);
             mCounterSize=(Math.max(mRect2.width(), mRect2.height())/2)+mCounterPadding;
         }else{
             mCounter=null;
         }
     }
 
     @Override
     public void draw(Canvas canvas) {
         // TODO Auto-generated method stub
         super.draw(canvas);
         //OVERLAY COUNTERS
         if(mCounter!=null){
             canvas.save();
            canvas.translate(getScrollX()+mCounterSize/2, getScrollY()+mCounterSize+mRect2.height());
             canvas.drawCircle(mRect2.width()/2, -mRect2.height()/2, mCounterSize, mStrokePaint);
             canvas.drawText(mCounter, 0, 0, mTextPaint);
             canvas.restore();
         }
     }
     
 }
