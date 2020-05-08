 /*******************************************************************************
  * Copyright 2012 Intel-GE Care Innovations(TM)
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 
 package com.ui;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.os.Handler;
 import android.os.Message;
 import android.util.AttributeSet;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 import com.mood.models.MoodPoint;
 import com.moodmap.R;
 import com.moodmap.TrendsActivity;
 import com.moodmap.Utils;
 
 import java.util.ArrayList;
 
 /**
  * @author shiva
  */
 public class TrendCustumView extends SurfaceView implements SurfaceHolder.Callback {
 
     ViewThread _thread;
     Bitmap _scratch;
     float animX = 100, animY = 100;
     int counterTimeDiff = 300;
     int animTimeDiff = 50;
     int scrollTimeDiff = 0;
 
     long mCounterTime = 0;
     long mAnimTime = 0;
     long mScrollTime = 0;
 
     Bitmap redCircle;
     float mAnimCircleRadius = 10;
     float minAnimCirculRadius = 7;
     boolean isRunning = true;
 
     float viewWidth = 0;
     float X = 0, Y = 0;
     boolean isBackgroundCroped = false;
     float scale;
     ArrayList<MoodPoint> moodPoints = new ArrayList<MoodPoint>();
     public boolean isReplay = false;
     Bitmap background, tapCircle;
     Bitmap slider_header, slider;
 
     Paint paintRed, paintBlue, paintYellow, paintPink, paintWhite, paintBlack;
     int moodPointCount = 0;
     float mPosition = 15 * scale;
     float DEVICE_WIDTH = 0;
     Context mComtext;
     long totalTime = 0;
     boolean stopDrawingInThread = false;
 
     /**
      * @param context
      * @param attrs
      */
     public TrendCustumView(Context context, AttributeSet attrs) {
         super(context, attrs);
 
         mComtext = context;
         DEVICE_WIDTH = getResources().getDisplayMetrics().widthPixels; // get
                                                                        // device
                                                                        // width
         background = BitmapFactory.decodeResource(context.getResources(), R.drawable.graph_new);
         tapCircle = BitmapFactory.decodeResource(context.getResources(), R.drawable.blue_circle);
 
         scale = context.getResources().getDisplayMetrics().density;
         mAnimCircleRadius = mAnimCircleRadius * scale;
 
         Float t = 15 * scale;
         Float t1 = 20 * scale;
 
         tapCircle = Bitmap.createScaledBitmap(tapCircle, t.intValue(), t.intValue(), true);
         redCircle = BitmapFactory.decodeResource(getResources(), R.drawable.red);
         slider_header = BitmapFactory.decodeResource(context.getResources(), R.drawable.scroller);
 
         slider = BitmapFactory.decodeResource(getResources(), R.drawable.scroll);
         slider_header = Bitmap
                 .createScaledBitmap(slider_header, t1.intValue(), t1.intValue(), true);
 
         mCounterTime = System.currentTimeMillis();
         long now = System.currentTimeMillis();
         mCounterTime = now + counterTimeDiff;
 
         mAnimTime = System.currentTimeMillis();
         mAnimTime = now + animTimeDiff;
 
         getHolder().addCallback(this);
 
         _thread = new ViewThread(getHolder(), this);
         _scratch = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
 
         MoodPoint mood = new MoodPoint();
         mood.X = 100;
         mood.Y = 100;
         moodPoints.add(mood);
         mood = new MoodPoint();
         mood.X = 120;
         mood.Y = 120;
         moodPoints.add(mood);
         mood = new MoodPoint();
         mood.X = 130;
         mood.Y = 150;
         moodPoints.add(mood);
 
         paintRed = new Paint();
         paintRed.setColor(Color.RED);
         paintRed.setAntiAlias(true);
 
         paintBlue = new Paint();
         paintBlue.setColor(Color.BLUE);
         paintBlue.setAntiAlias(true);
 
         paintYellow = new Paint();
         paintYellow.setColor(Color.parseColor("#FFFF00"));
         paintYellow.setAntiAlias(true);
 
         paintPink = new Paint();
         paintPink.setColor(Color.parseColor("#FF00FF"));
         paintPink.setAntiAlias(true);
 
         paintWhite = new Paint();
         paintWhite.setColor(Color.WHITE);
         paintWhite.setAntiAlias(true);
         paintWhite.setTextSize(15 * scale);
 
         paintBlack = new Paint();
         paintBlack.setColor(Color.BLACK);
     }
 
     @Override
     protected void onDraw(Canvas canvas) {
 
         if (!isBackgroundCroped)
         {
             viewWidth = this.getWidth();
             background = Bitmap.createScaledBitmap(background, this.getWidth(), this.getWidth(),
                     true);
             isBackgroundCroped = true;
         }
         canvas.drawColor(Color.BLACK);
         canvas.drawBitmap(background, 0, 0, null);
 
         if (moodPointCount < moodPoints.size())
         {
             canvas.drawCircle(moodPoints.get(moodPointCount).X, moodPoints.get(moodPointCount).Y,
                     mAnimCircleRadius, getPaintColor(moodPoints.get(moodPointCount).color));
         }
 
         drawPoints(canvas);
         updateUi();
         drawScroll(canvas);
         super.onDraw(canvas);
     }
 
     public void Replay()
     {
         moodPointCount = 0;
         mPosition = 20 * scale;
 
         long now = System.currentTimeMillis();
         mAnimTime = now + animTimeDiff;
         mAnimCircleRadius = 10 * scale;// reset redius to max
 
         mCounterTime = now + counterTimeDiff;
 
         isReplay = false;
         isRunning = true;
     }
 
     private void drawPoints(Canvas canvas) {
         // Log.v("Draw Points","*");
         if (moodPoints.size() > 0 && moodPointCount < moodPoints.size())
         {
             for (int i = 0; i <= moodPointCount; i++)
             {
                 canvas.drawCircle(moodPoints.get(i).X, moodPoints.get(i).Y, minAnimCirculRadius
                         * scale, getPaintColor(moodPoints.get(i).color));
             }
 
             // Added try/catch for editor - STC 10/17/11
             try {
                 // Draw Date and Time values on screen
                 // move text - y was 55, now 6 - STC 1/10/12
                 // was
                 // canvas.drawText(Utils.getFormatedDate(moodPoints.get(moodPointCount).Date),
                 // 85*scale, this.getWidth()+55*scale, paintWhite);
                 // was
                 // canvas.drawText(Utils.getFormatedTime(moodPoints.get(moodPointCount).Time),
                 // 165*scale, this.getWidth()+55*scale, paintWhite);
                 canvas.drawText(Utils.getFormatedDate(moodPoints.get(moodPointCount).Date),
                         90 * scale, this.getWidth() + 22 * scale, paintWhite);
                 canvas.drawText(Utils.getFormatedTime(moodPoints.get(moodPointCount).Time),
                         175 * scale, this.getWidth() + 22 * scale, paintWhite);
             } catch (Exception ee) {
                 // ee.printStackTrace();
             }
         }
 
     }
 
     /**
      * Scroll bar drawing logic
      * 
      * @param
      */
     private void drawScroll(Canvas canvas)
     {
         // make smaller to put play button on right - STC 1/10/12
         // was canvas.drawRect(0, this.getWidth()+72*scale, this.getWidth(),
         // this.getWidth()+85*scale, paintBlack);
         // was canvas.drawBitmap(slider,0,this.getWidth()+ 90*scale,null);
         canvas.drawRect(0, this.getWidth() + 35 * scale, this.getWidth(), this.getWidth() + 48
                 * scale, paintBlack);
         canvas.drawBitmap(slider, 0, this.getWidth() + 53 * scale, null);
 
         if (moodPoints.size() == 1)// for single point
         {
             mPosition = ((320 * scale));
         }
 
         // was
         // canvas.drawBitmap(slider_header,mPosition-(20*scale),this.getWidth()+82*scale,null);
         canvas.drawBitmap(slider_header, mPosition - (20 * scale), this.getWidth() + 45 * scale,
                 null);
     }
 
     /**
      * Setting animation to play or pause
      * 
      * @param boolean
      */
     public void setRunning(boolean run)
     {
         isRunning = run;
     }
 
     public boolean isRunning() {
         return isRunning;
     }
 
     private void updateMoodPointCount() {
 
         long now = System.currentTimeMillis();
         if (mCounterTime > now)
             return;
 
         mCounterTime = now + counterTimeDiff;
         if (moodPointCount == (moodPoints.size() - 1))
         {
             /** Don't Increasr moodpointCount */
             if (!isReplay)
             {
                 messageHandler.sendEmptyMessage(0);
                 isReplay = true;
             }
 
             isRunning = false;
         }
         if (isRunning)
         {
             mAnimCircleRadius = 10 * scale;
             if (moodPointCount < (moodPoints.size() - 1))
                 moodPointCount++;
 
             updateScrollValues();
         }
 
     }
 
     private void updateUi() {
         long now = System.currentTimeMillis();
         if (mAnimTime > now)
             return;
         mAnimTime = now + animTimeDiff;
 
         if (mAnimCircleRadius > 7 * scale)
             mAnimCircleRadius -= 1 * scale;
         updateMoodPointCount();
     }
 
     private void updateScrollValues() {
 
         if (moodPoints.size() > 1) {
             mPosition = ((DEVICE_WIDTH / moodPoints.size()) * moodPointCount)
                     + (DEVICE_WIDTH / moodPoints.size());
             if (moodPointCount == moodPoints.size() - 1)
             {
                 mPosition = DEVICE_WIDTH;
             }
         }
         if (mPosition > DEVICE_WIDTH) {
             mPosition = DEVICE_WIDTH;
         }
     }
 
     public void setMoodValues(ArrayList<MoodPoint> myMoods)
     {
         moodPoints = myMoods;
         totalTime = moodPoints.size() * 250;
     }
 
     @Override
     public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
         // TODO Auto-generated method stub
     }
 
     @Override
     public void surfaceCreated(SurfaceHolder arg0) {
         _thread.setRunning(true);
         _thread.start();
     }
 
     @Override
     public void surfaceDestroyed(SurfaceHolder arg0) {
 
         boolean retry = true;
         _thread.setRunning(false);
         while (retry) {
             try {
                 _thread.join();
                 retry = false;
             } catch (InterruptedException e) {
                 // we will try it again and again...
             }
         }
     }
 
     public float getPossition()
     {
         return mPosition;
     }
 
     public void setPossition(float pos) {
 
         moodPointCount = (int) (pos / (DEVICE_WIDTH / moodPoints.size()));
         if (moodPointCount == moodPoints.size())
         {
             moodPointCount--;
         }
     }
 
     public void setDragingPosition(float dragPos)
     {
         mPosition = dragPos;
     }
 
     class ViewThread extends Thread {
         private final SurfaceHolder _surfaceHolder;
         private final TrendCustumView _panel;
         private boolean _run = false;
 
         public ViewThread(SurfaceHolder surfaceHolder, TrendCustumView panel) {
             _surfaceHolder = surfaceHolder;
             _panel = panel;
         }
 
         public void setRunning(boolean run) {
             _run = run;
         }
 
         @Override
         public void run() {
             Canvas c;
             while (_run) {
                 c = null;
 
                 try {
                     c = _surfaceHolder.lockCanvas(null);
                     synchronized (_surfaceHolder) {
                        if(c != null)
                            _panel.onDraw(c);
                     }
 
                 } finally {
                     // do this in a finally so that if an exception is thrown
                     // during the above, we don't leave the Surface in an
                     // inconsistent state
                     if (c != null) {
                         _surfaceHolder.unlockCanvasAndPost(c);
                     }
 
                 }
             }
         }
     }
 
     private Paint getPaintColor(int p) {
 
         switch (p) {
             case 1:// Home
 
                 return paintYellow;
             case 2:// Work
 
                 return paintRed;
 
             case 3:// On the go
                 return paintPink;
 
             case 4:// Other
                 return paintBlue;
 
             default:
                 return paintBlue;
         }
     }
 
     private final Handler messageHandler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
             super.handleMessage(msg);
             // Log.e("Message","***************Handled");
             TrendsActivity.btnPlayPause.setBackgroundDrawable(mComtext.getResources().getDrawable(
                     R.drawable.play1));
 
         }
     };
 }
