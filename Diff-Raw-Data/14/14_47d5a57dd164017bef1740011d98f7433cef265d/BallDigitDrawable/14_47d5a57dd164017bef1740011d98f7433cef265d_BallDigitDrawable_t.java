 /**
  * Copyright 2010 Sean L. Mooney
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 /**
  *
  */
 package com.mooney_ware.gio.view;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.ColorFilter;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.graphics.RectF;
 import android.graphics.drawable.Drawable;
 import android.graphics.drawable.ShapeDrawable;
 import android.graphics.drawable.shapes.OvalShape;
 import android.util.Log;
 
 import com.mooney_ware.gio.GoogleIOCountdown;
 
 /**
  * @author Sean Mooney
  *
  */
 public class BallDigitDrawable extends Drawable implements DigitDisplay{
 
     private int mAlpha;
 
     private int value;
     //value last time paint was called.
     private AtomicBoolean valueChanged = new AtomicBoolean(false);
 
     private static final int CELLS_ACCROSS = 4;
     private static final int CELLS_DOWN = 7;
     
    private int mDiameter = 5;
    private int dotPadding = 2* mDiameter + 2;
     
     private ArrayList<DigitDisplay.SegmentLightListener> segListener = new ArrayList<DigitDisplay.SegmentLightListener>();
     
     private Drawable mOnDrawable;
     private Drawable mOffDrawable;
     
     int mLastShapeMask = 0x00000000;
     
     public Paint getPaint(){
         Paint p = new Paint();
         p.setAntiAlias(true);
         p.setColor(Color.WHITE);
         return p;
     }
     
     int oldValue;
     /**
      * Set the value this display should display.
      * @param v
      */
     @Override
     public void setValue(int v){
 
         if(value < 0 || value > 9){
             Log.w(GoogleIOCountdown.TAG, "Invalid value in BallDisplay " + v);
         }
 
         //Log.i("BallDigitDrawable", "Setting value " + v);
         
         
         this.value = v;
        
         if(oldValue != v){
             valueChanged.set(true);
             oldValue = v;
         }
         this.invalidateSelf();
         
     }
 
     public void setBounds(Rect newBounds){
         super.setBounds(newBounds);        
     }
     
     //Rect bounds;
     @Override
     protected void onBoundsChange(Rect newBounds){
         super.onBoundsChange(newBounds);
         Log.v("BallDisplay", "Bounds " + getBounds());
     }
     
     
     
     /* (non-Javadoc)
      * @see android.graphics.drawable.Drawable#getIntrinsicHeight()
      */
     @Override
     public int getIntrinsicHeight() {
        return (CELLS_DOWN * mDiameter) + ((CELLS_DOWN -1)  * dotPadding);
     }
 
     /* (non-Javadoc)
      * @see android.graphics.drawable.Drawable#getIntrinsicWidth()
      */
     @Override
     public int getIntrinsicWidth() {
        return (CELLS_ACCROSS * mDiameter) + ( (CELLS_ACCROSS -1)  * dotPadding);
     }
 
     /* (non-Javadoc)
      * @see android.graphics.drawable.Drawable#draw(android.graphics.Canvas)
      */
     @Override
     public void draw(Canvas canvas) {
         
         
         final int WIDTH = CELLS_ACCROSS;
         final int HEIGHT = CELLS_DOWN;
         Rect bounds = getBounds();
         
        int radius = mDiameter;
         if(radius <= 1){
             radius = 1;
         }
         
         int xPad = dotPadding ;
         int yPad = dotPadding;
         int xStart = bounds.left + radius;
         int curX;
         int curY = bounds.top + radius;
         
         
         int consummableMask = SHAPE_DESC[value];
         int changeMask = mLastShapeMask & ~consummableMask;
         mLastShapeMask = consummableMask;
         ArrayList<RectF> newPoints = new ArrayList<RectF>();
         
         Drawable dOn = getOnDrawable();
         Drawable dOff = getOffDrawable();
         
         for(int h = 0; h < HEIGHT; h++){
             curX = xStart;
             for(int w = WIDTH - 1; w >= 0; w--){
                 int doDraw = consummableMask & 0x00000001;
                 Drawable d;
                 
                 if(doDraw == 1){
                     d = dOn;
                 }else{
                     d = dOff;
                 }
 
                 d.setBounds(curX - radius, curY - radius, curX + radius, curY + radius);
                 d.draw(canvas);
 
                 int pointChanged = (changeMask & 0x00000001);
                 changeMask = (changeMask >>> 1);
                 if(pointChanged == 1){
                     newPoints.add(new RectF(curX - radius, curY-radius, 
                             curX + radius, curY + radius));
                 }
                 
                 consummableMask = (consummableMask >>> 1);
                 curX += xPad;
             }
             curY += yPad;
         }
         
         if(!newPoints.isEmpty()){
             notifyDarkSements(newPoints);
         }
     }
 
     /* (non-Javadoc)
      * @see android.graphics.drawable.Drawable#getOpacity()
      */
     @Override
     public int getOpacity() {
         return mAlpha;
     }
 
     /* (non-Javadoc)
      * @see android.graphics.drawable.Drawable#setAlpha(int)
      */
     @Override
     public void setAlpha(int alpha) {
         this.mAlpha = alpha;
     }
 
     /* (non-Javadoc)
      * @see android.graphics.drawable.Drawable#setColorFilter(android.graphics.ColorFilter)
      */
     @Override
     public void setColorFilter(ColorFilter cf) {
         // TODO Auto-generated method stub
 
     }
 
     public void notifyLitSegments(List<RectF> segmentBounds) {
         synchronized(segListener){
             for(SegmentLightListener sll : segListener){
                 //copy the list to prevent aliasing problems if 
                 //the reciever changes the list. Changes to the rectangles will be seen.
                 sll.onSegmentLight(new ArrayList<RectF>(segmentBounds));
             }
         }
     }
 
     
     public void notifyDarkSements(List<RectF> segmentBounds) {
         synchronized(segListener){
             if(segListener.isEmpty())return;
             for(SegmentLightListener sll : segListener){
                 //copy the list to prevent aliasing problems if 
                 //the reciever changes the list. Changes to the rectangles will be seen.
                 sll.onSegmentDark(new ArrayList<RectF>(segmentBounds));
             }
         }
     }
     
 
     /* (non-Javadoc)
      * @see com.mooney_ware.gio.view.DigitDisplay#registerSegmentListenet(com.mooney_ware.gio.view.DigitDisplay.SegmentLightListener)
      */
     @Override
     public void registerSegmentListener(SegmentLightListener listener) {
         synchronized (segListener) {
             segListener.add(listener);
         }
     }
 
     /* (non-Javadoc)
      * @see com.mooney_ware.gio.view.DigitDisplay#setOnDrawable(android.graphics.drawable.Drawable)
      */
     @Override
     public void setOnDrawable(Drawable d) {
         mOnDrawable = d;
     }
 
     public Drawable getOnDrawable(){
         if(mOnDrawable == null){
             ShapeDrawable sd = new ShapeDrawable(new OvalShape());
             sd.getPaint().setColor(Color.BLACK);
             sd.setAlpha(255);
             mOnDrawable = sd;
         }
         mOnDrawable.setVisible(true, true);
         return mOnDrawable;
     }
     
     /* (non-Javadoc)
      * @see com.mooney_ware.gio.view.DigitDisplay#setOffDrawable(android.graphics.drawable.Drawable)
      */
     @Override
     public void setOffDrawable(Drawable d) {
         mOffDrawable = d;
     }
     
     public Drawable getOffDrawable(){
         if(mOffDrawable == null){
             ShapeDrawable sd = new ShapeDrawable(new OvalShape());
             sd.getPaint().setColor(0xff74AC23);
             sd.setAlpha(155);
             
             mOffDrawable = sd;
         }
         return mOffDrawable;
     }
     
  // Ints to describe how the number layout.
     // Each dot to draw has a corresponding bit mask that means draw (1) or
     // don't (0)
     static final int[] SHAPE_DESC = 
     {   0x0F99999F,  //0
         0x08888888, //1
         0x0F11F88F, //2
         0x0F88F88F, //3
         0x0888F999, //4
         0x0F88F11F, //5
         0x0F99F111, //6
         0x0888888F, //7
         0x0F99F99F, //8
         0x0888F99F }; //9
 }
