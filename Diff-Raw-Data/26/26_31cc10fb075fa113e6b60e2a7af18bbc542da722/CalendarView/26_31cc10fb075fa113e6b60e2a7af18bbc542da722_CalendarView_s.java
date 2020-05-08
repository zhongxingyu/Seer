 /*
  * Copyright (C) 2007 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package net.redgeek.android.eventrend.calendar;
 
 import java.util.Calendar;
 
 import net.redgeek.android.eventrend.Preferences;
 import net.redgeek.android.eventrend.backgroundtasks.DataCollectionTask;
 import net.redgeek.android.eventrend.primitives.TimeSeriesCollector;
 import net.redgeek.android.eventrend.primitives.Tuple;
 import net.redgeek.android.eventrend.util.DateUtil;
 import net.redgeek.android.eventrend.util.GUITask;
 import net.redgeek.android.eventrend.util.GUITaskQueue;
 import net.redgeek.android.eventrend.util.ProgressIndicator;
 import net.redgeek.android.eventrend.util.DateUtil.Period;
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnLongClickListener;
 import android.widget.TextView;
 
 public class CalendarView extends View implements OnLongClickListener, GUITask {
   // Various UI parameters
   public static final int BORDER_X = 20;
   public static final int BORDER_Y = 20;
   public static final int CELL_BORDER_FOCUSED = 3;
   public static final int CELL_BORDER_HIGHLIGHT = 1;
   public static final int CELL_BORDER_AUX = 1;
   public static final int CELL_TEXT_STROKE = 1;
   public static final int TEXT_HEIGHT = 10;
 
   public static final int ZOOM_CTRL_HIDE_MS = 3000;
 
   // UI elements
   private TextView mStatus;
   private CalendarPlot mCalendarPlot;
   private ProgressIndicator.Titlebar mProgress;
   private Canvas mCanvas;
 
   // Private data
   private Context mCtx;
   private TimeSeriesCollector mTSC;
   private Calendar mCal;
   private Period mPeriod;
   private DateUtil mDates;
   private long mCatId;
 
   // Tasks and handlers
   private DataCollectionTask mCollector;
 
   public CalendarView(Context context, TimeSeriesCollector tsc) {
     super(context);
     mCtx = context;
     mTSC = tsc;
 
     setupData();
     setupUI();
   }
 
   private void setupData() {
     mCal = Calendar.getInstance();
     mDates = new DateUtil();
     mPeriod = Period.MONTH;
 
     mCollector = new DataCollectionTask(mTSC);
     mCalendarPlot = new CalendarPlot(mCtx, mTSC, getWidth(), getHeight());
   }
 
   private void setupUI() {
     setupListeners();
 
     setColorScheme();
     setFocusableInTouchMode(true);
     setOnLongClickListener(this);
 
     mProgress = new ProgressIndicator.Titlebar(mCtx);
 
     mStatus = ((CalendarActivity) mCtx).getStatusTextView();
   }
 
   private void setupListeners() {
   }
 
   public void setColorScheme() {
     if (Preferences.getDefaultGraphIsBlack(mCtx) == true)
       setBackgroundColor(Color.BLACK);
     else
       setBackgroundColor(Color.WHITE);
     mCalendarPlot.setColorScheme();
   }
 
   public boolean onLongClick(View v) {
     return true;
   }
 
   @Override
   protected void onSizeChanged(int w, int h, int oldw, int oldh) {
     mCalendarPlot.setCalendarSize(w, h);
   }
 
   @Override
   public boolean onTouchEvent(MotionEvent event) {
     // if (event.getAction() != MotionEvent.ACTION_UP) {
     // ((CalendarActivity) mCtx).graph();
     // return super.onTouchEvent(event);
     // }
 
     return super.onTouchEvent(event);
   }
 
   @Override
   protected void onDraw(Canvas canvas) {
     super.onDraw(canvas);
     if (getWidth() > 0 && getHeight() > 0) {
       mCanvas = canvas;
       mCalendarPlot.plot(mCanvas);
     }
   }
 
   public void executeNonGuiTask() throws Exception {
     setStartEnd();
     mCollector.doCollection();
   }
 
   public void afterExecute() {
     updateStatus();
     setFocusable(true);
     invalidate();
   }
 
   public void onFailure(Throwable t) {
   }
 
   public void resetZoom() {
     long start = mCalendarPlot.getCalendarStart();
     mCalendarPlot.setCalendarStart(start);
   }
 
   public void updateData() {
     setStartEnd();
 
     // Useful debugging: uncomment the following, and comment out the
     // addTask() below -- this makes the data collection run synchronously.
     // mCollector.doCollection();
     // updateStatus();
     // invalidate();
 
     GUITaskQueue.getInstance().addTask(mProgress, this);
   }
 
   private void setStartEnd() {
     long displayStart = mCalendarPlot.getCalendarStart();
     long collectionStart = displayStart;
     long collectionEnd;
 
     Calendar cal = mDates.getCalendar();
     cal.setTimeInMillis(displayStart);
     if (mPeriod == Period.YEAR) {
       DateUtil.setToPeriodStart(cal, Period.YEAR);
       displayStart = cal.getTimeInMillis();
       mDates.setBaseTime(displayStart);
       mDates.advance(Period.YEAR, -3);
       collectionStart = cal.getTimeInMillis();
       collectionEnd = collectionStart + DateUtil.YEAR_MS * 5;
     } else if (mPeriod == Period.MONTH) {
       DateUtil.setToPeriodStart(cal, Period.MONTH);
       displayStart = cal.getTimeInMillis();
       mDates.setBaseTime(displayStart);
 
       int month = mDates.get(Calendar.MONTH);
       int position = mDates.get(Calendar.DAY_OF_WEEK);
 
       if (month == 1 && position == cal.getFirstDayOfWeek()) {
         mDates.advance(Period.DAY, -7);
       } else {
         mDates.advance(Period.DAY, -position + 1);
       }
       collectionStart = cal.getTimeInMillis();
       collectionEnd = collectionStart + DateUtil.DAY_MS * 42;
     } else {
       DateUtil.setToPeriodStart(cal, Period.DAY);
       displayStart = cal.getTimeInMillis();
       mDates.setBaseTime(displayStart);
       mDates.advance(Period.DAY, -7);
       collectionStart = cal.getTimeInMillis();
       collectionEnd = collectionStart + DateUtil.YEAR_MS * 8;
     }
 
     mCalendarPlot.setCalendarStart(displayStart);
     mCalendarPlot.setSpan(mPeriod);
 
     mTSC.setSmoothing(Preferences.getSmoothingConstant(mCtx));
     mTSC.setAutoAggregationOffset(-2);
 
     mCollector.setSpan(collectionStart, collectionEnd);
   }
 
   private void updateStatus() {
     Calendar c = Calendar.getInstance();
     c.setTimeInMillis(mCalendarPlot.getCalendarStart());
     if (mPeriod == Period.YEAR) {
       mStatus.setText("" + c.get(Calendar.YEAR));
     } else if (mPeriod == Period.MONTH) {
       mStatus.setText(DateUtil.MONTHS[c.get(Calendar.MONTH)] + " "
           + c.get(Calendar.YEAR));
    } else if (mPeriod == Period.DAY) {
      mStatus.setText(DateUtil.DAYS[c.get(Calendar.DAY_OF_WEEK)] + ", "
          + DateUtil.MONTHS[c.get(Calendar.MONTH)] + " "
          + c.get(Calendar.DAY_OF_MONTH) + " " + c.get(Calendar.YEAR));
     }
   }
 
   private void jump(int offset) {
     Calendar cal = mDates.getCalendar();
     cal.setTimeInMillis(mCalendarPlot.getCalendarStart());
     if (mPeriod == Period.YEAR) {
       // nothing to do for this case
     } else if (mPeriod == Period.MONTH) {
       cal.add(Calendar.YEAR, offset);
     } else {
       cal.add(Calendar.MONTH, offset);
     }
     mCalendarPlot.setCalendarStart(cal.getTimeInMillis());
 
   }
 
   private void shift(int offset) {
     Calendar cal = mDates.getCalendar();
     cal.setTimeInMillis(mCalendarPlot.getCalendarStart());
     if (mPeriod == Period.YEAR) {
       cal.add(Calendar.YEAR, offset);
     } else if (mPeriod == Period.MONTH) {
       cal.add(Calendar.MONTH, offset);
     } else {
       cal.add(Calendar.DAY_OF_MONTH, offset);
     }
     mCalendarPlot.setCalendarStart(cal.getTimeInMillis());
   }
 
   public void prevJump() {
     jump(-1);
   }
 
   public void nextJump() {
     jump(1);
   }
 
   public void prevPeriod() {
     shift(-1);
   }
 
   public void nextPeriod() {
     shift(1);
   }
 
   public void lookupPeriod(Tuple t) {
     mCalendarPlot.lookupPeriod(t);
   }
 
   public CalendarPlot getCalendar() {
     return mCalendarPlot;
   }
 
   public void setCategoryId(long catId) {
     mCatId = catId;
   }
 
   public long getCategoryId() {
     return mCatId;
   }
 }
