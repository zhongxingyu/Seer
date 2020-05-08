 /*
  * Copyright 2011 Oleg Elifantiev
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 
 package ru.elifantiev.android.timespan;
 
 import android.content.Context;
 import android.graphics.*;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.*;
 
 import java.util.Set;
 import java.util.TreeSet;
 
 import static ru.elifantiev.android.timespan.DrawParameters.SIDE_PAD;
 
 
 public class TimeSpanGroupEditor extends View implements
         GestureDetector.OnGestureListener,
         GestureDetector.OnDoubleTapListener {
 
     private int viewportTop = 0;
     private int hoursOnScreen = 12;
     private float scaleFactor = (float)hoursOnScreen / 24f;
     private GestureDetector gestureDetector;
     private ScaleGestureDetector scaleGestureDetector;
     private Paint pOuter, pLine, pLabelText;
 
     private TreeSet<VisualTimeSpan> displayedSpans = new TreeSet<VisualTimeSpan>();
     private Rect boundaries;
     private final Rect charBounds = new Rect();
 
     private Set<String> labelsAtTop = new TreeSet<String>();
     private Set<String> labelsAtBottom = new TreeSet<String>();
 
     private final String[] labels = new String[24];
     private final DrawLayer scale = new DrawLayer();
 
     private boolean isMeasured = false;
     private VisualTimeSpan activeSpan = null;
     private VisualTimeSpan.HitTestResult activeSpanMode = VisualTimeSpan.HitTestResult.NOWHERE;
     private VisualDaysSelector daysSelector;
     private int dragStart = 0;
     DrawParameters drawParameters;
 
 
     public TimeSpanGroupEditor(Context context) {
         super(context);
         init();
     }
 
     public TimeSpanGroupEditor(Context context, AttributeSet attrs) {
         super(context, attrs);
         init();
     }
 
     public TimeSpanGroupEditor(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
         init();
     }
 
     private void init() {
         gestureDetector = new GestureDetector(getContext(), this);
         gestureDetector.setOnDoubleTapListener(this);
 
         if(getContext().getResources().getBoolean(R.bool.hasScaleGestureDetector))
             scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
 
         drawParameters = new DrawParameters(getContext());
 
         pOuter = new Paint();
         pOuter.setColor(Color.GRAY);
         pOuter.setStrokeWidth(2);
         pOuter.setStyle(Paint.Style.STROKE);
 
         pLine = new Paint();
         pLine.setColor(Color.GRAY);
         pLine.setStrokeWidth(1);
         pLine.setTextSize(11 * drawParameters.density);
         pLine.setAntiAlias(true);
         pLine.setStyle(Paint.Style.FILL_AND_STROKE);
 
         pLabelText = new Paint(pLine);
         pLabelText.setTextAlign(Paint.Align.RIGHT);
         pLabelText.setColor(Color.WHITE);
         pLabelText.setTextSize(13 * drawParameters.density);
 
         pLabelText.getTextBounds("0", 0, 1, charBounds);
         charBounds.bottom += drawParameters.SCALE_LABEL_TOP_PADDING / 2;
 
         for (int i = 0; i < 24; i++)
             labels[i] = String.format("%02d:00", i);
 
         daysSelector = VisualDaysSelector.newInstance(getContext());
     }
 
     private void changeViewport(float viewportDelta) {
         int desiredValue = Math.max(0, Math.min(Math.round(viewportTop + viewportDelta), 1440 - hoursOnScreen * 60));
         if (viewportTop != desiredValue) {
             desiredValue = (desiredValue / 5 + (desiredValue % 5 > 3 ? 1 : 0)) * 5;
             viewportTop = desiredValue;
             recalcOutLabels(true);
             scale.reset();
             drawScale();
             invalidate();
         }
     }
 
     private void recalcOutLabels(boolean doInvalidate) {
         labelsAtTop.clear();
         labelsAtBottom.clear();
         for (VisualTimeSpan span : displayedSpans) {
             String sSpan = span.toString();
             if(!isSpanVisible(span)) {
                 if(span.getLowerBound() < viewportTop)
                     labelsAtTop.add(sSpan);
                 else if(span.getLowerBound() > viewportTop + hoursOnScreen * 60)
                     labelsAtBottom.add(sSpan);
             }
             if(doInvalidate)
                 span.invalidate();
         }
     }
 
     private boolean isSpanVisible(VisualTimeSpan span) {
         int viewportEnd = viewportTop + hoursOnScreen * 60;
         int lowerBound = span.getLowerBound();
         int upperBound = span.getUpperBound();
         return
                 (viewportTop <= upperBound && upperBound <= viewportEnd) ||
                 (viewportTop <= lowerBound && lowerBound <= viewportEnd) ||
                 (upperBound <= viewportTop && viewportTop <= lowerBound) ||
                 (upperBound <= viewportEnd && viewportEnd <= lowerBound);
     }
 
     private void setScale(int newScale) {
         newScale = Math.max(6, Math.min(newScale, 24));
         if(hoursOnScreen != newScale) {
             hoursOnScreen = newScale;
 
             int d = 24 - (viewportTop / 60 + (viewportTop % 60 > 0 ? 1: 0) + hoursOnScreen);
             if(d < 0) {
                 changeViewport(d * 60);
             } else {
                 recalcOutLabels(true);
                 scale.reset();
                 drawScale();
                 invalidate();
             }
         }
     }
 
     @Override
     public boolean onTouchEvent(MotionEvent event) {
         if(scaleGestureDetector != null)
             scaleGestureDetector.onTouchEvent(event);
 
         if (gestureDetector.onTouchEvent(event))
             return true;
         else {
             if (activeSpan != null && event.getAction() == MotionEvent.ACTION_UP) {
                 if (checkOverlap()) {
                     recalcOutLabels(false);
                     invalidate();
                 }
                 return true;
             } else
                 return super.onTouchEvent(event);
         }
     }
 
     @Override
     protected void onSizeChanged(int w, int h, int oldw, int oldh) {
         isMeasured = true;
         boundaries = new Rect(
                 getPaddingLeft() + SIDE_PAD,
                 getPaddingTop() + drawParameters.TB_PAD,
                 w - getPaddingRight() - SIDE_PAD * 2 - drawParameters.DAY_SELECTOR_AREA_WIDTH,
                 h - getPaddingBottom() - drawParameters.TB_PAD);
 
        scale.onSizeChange(boundaries.width(), boundaries.height());
 
 
         if (displayedSpans.size() == 0)
             appendSpan(VisualTimeSpan.newInstance(this));
 
         for (VisualTimeSpan span : displayedSpans)
             span.onSizeChanged(w, h, boundaries);
 
         recalcOutLabels(false);
         drawScale();
 
         daysSelector.onSizeChanged(w, h, new RectF(
                 w - getPaddingRight() - SIDE_PAD - drawParameters.DAY_SELECTOR_AREA_WIDTH,
                 getPaddingTop() + drawParameters.TB_PAD,
                 w - getPaddingRight() - SIDE_PAD,
                 h - getPaddingBottom() - drawParameters.TB_PAD
         ));
 
         super.onSizeChanged(w, h, oldw, oldh);
     }
 
     @Override
     protected void onDraw(Canvas canvas) {
        scale.drawOn(canvas, boundaries.left, boundaries.top);
         daysSelector.onDraw(canvas);
         for (VisualTimeSpan span : displayedSpans)
             span.onDraw(canvas);
     }
 
     private void drawScale() {
         Canvas canvas = scale.getCanvas();
        canvas.drawRect(0, 0, boundaries.width(), boundaries.height(), pOuter);
 
         int extraMin = (60 - (viewportTop % 60)) % 60;
        float offsetTop = minuteToPixelPoint(viewportTop + extraMin);
         float heightHours = boundaries.height() / hoursOnScreen;
         int firstLabelIndex = viewportTop / 60 + (extraMin > 0 ? 1 : 0);
 
         for (int i = 0; i < hoursOnScreen; i++) {
             float offsetY = offsetTop +  heightHours * i;
            canvas.drawLine(0, offsetY, boundaries.width(), offsetY, pLine);
             canvas.drawText(
                     labels[firstLabelIndex + i],
                    10,
                     offsetY + drawParameters.SCALE_LABEL_TOP_PADDING,
                     pLine);
         }
 
         int i = 0;
         for(String label : labelsAtTop) {
             canvas.drawText(label,
                    boundaries.width() - drawParameters.SCALE_LABEL_TOP_PADDING,
                    drawParameters.SCALE_LABEL_TOP_PADDING + i * charBounds.height(),
                     pLabelText);
             i++;
         }
 
         i = 0;
         int colSize = labelsAtBottom.size() - 1;
         for(String label : labelsAtBottom) {
             canvas.drawText(
                     label,
                    boundaries.width() - drawParameters.SCALE_LABEL_TOP_PADDING,
                    boundaries.height() - drawParameters.SCALE_LABEL_TOP_PADDING - (colSize - i) * charBounds.height(),
                     pLabelText);
             i++;
         }
     }
 
     public boolean onDown(MotionEvent motionEvent) {
         activeSpan = null;
         activeSpanMode = VisualTimeSpan.HitTestResult.NOWHERE;
         for (VisualTimeSpan span : displayedSpans) {
             VisualTimeSpan.HitTestResult hitTest = span.hitTest(motionEvent);
             if (hitTest != VisualTimeSpan.HitTestResult.NOWHERE) {
                 activeSpanMode = hitTest;
                 activeSpan = span;
                 if (hitTest == VisualTimeSpan.HitTestResult.JUST_IN)
                     dragStart = (int)pixelPointToMinutes(screenToControl(motionEvent.getY()));
                 break;
             }
         }
         return
                 activeSpan != null ||
                         boundaries.contains((int)motionEvent.getX(), (int)motionEvent.getY()) ||
                         daysSelector.hitTest(motionEvent) >= 0;
     }
 
     public void onShowPress(MotionEvent motionEvent) {
         if (activeSpan != null)
             performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
     }
 
     public boolean onSingleTapUp(MotionEvent motionEvent) {
         if (daysSelector.handleTap(motionEvent)) {
             invalidate();
             return true;
         }
         return false;
     }
 
     public boolean onScroll(MotionEvent start, MotionEvent finish, float dX, float dY) {
         if (activeSpan == null || !activeSpan.isEditMode()) {
             changeViewport(pixelAmountToMinutes(dY));
             return true;
         }
         int selectionTop = activeSpan.getUpperBound(), selectionBottom = activeSpan.getLowerBound();
         int finishY = (int)pixelPointToMinutes(screenToControl(finish.getY()));
         boolean alter = false;
         if (activeSpanMode == VisualTimeSpan.HitTestResult.TOP_KNOB) {
             finishY = checkUpperBound(finishY);
             if (alter = (0 <= finishY && finishY <= selectionBottom))
                 selectionTop = finishY;
         } else if (activeSpanMode == VisualTimeSpan.HitTestResult.BOTTOM_KNOB) {
             finishY = checkLowerBound(finishY);
             if ((alter = (selectionTop <= finishY && finishY <= 1440)))
                 selectionBottom = finishY;
         } else if (activeSpanMode == VisualTimeSpan.HitTestResult.JUST_IN) {
             float delta = finishY - dragStart;
             if (0 < activeSpan.getUpperBound() + delta &&
                     activeSpan.getLowerBound() + delta < 1440) {
                 selectionTop += delta;
                 selectionBottom += delta;
             } else if (0 > selectionTop + delta) {
                 selectionBottom -= selectionTop;
                 selectionTop = 0;
             } else if (selectionBottom + delta > 1440) {
                 selectionTop += 1440 - selectionBottom;
                 selectionBottom = 1440;
             }
             dragStart = finishY;
             alter = true;
         }
 
 
         if (alter) {
             activeSpan.setBounds(selectionTop, selectionBottom);
             invalidate();
         }
 
         return alter;
     }
 
     public void onLongPress(MotionEvent motionEvent) {
         if (activeSpan != null) {
             activeSpan.toggleEditMode();
             activeSpan = null;
             invalidate();
         }
     }
 
     public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
         return false;
     }
 
     private int checkUpperBound(int toPoint) {
         return Math.max(0, toPoint);
     }
 
     private int checkLowerBound(int toPoint) {
         return Math.min(1440, toPoint);
     }
 
     private float alignValue(float value) {
         return controlToScreen(alignToSpan(screenToControl(value)));
     }
 
     float screenToControl(float pixelVal) {
         return pixelVal - drawParameters.TB_PAD;
     }
 
     float controlToScreen(float pixelVal) {
         return pixelVal + drawParameters.TB_PAD;
     }
 
     float pixelPointToMinutes(float pixelVal) {
         return (pixelVal * (hoursOnScreen * 60) / boundaries.height()) + viewportTop;
     }
 
     float pixelAmountToMinutes(float pixelAmount) {
         return pixelPointToMinutes(pixelAmount) - viewportTop;
     }
 
     float minuteToPixelPoint(float minutes) {
         return (minutes - viewportTop) * boundaries.height() / (hoursOnScreen * 60);
     }
 
     /**
      *      5 
      *
      * @param val     
      * @return     5   
      */
     private float alignToSpan(float val) {
         float minutesVal = pixelPointToMinutes(val);
         double fiveSpan = Math.floor(minutesVal / 5) + (Math.floor(minutesVal % 5) >= 3 ? 1 : 0);
         return minuteToPixelPoint((float) (fiveSpan * 5));
     }
 
     private void appendSpan(VisualTimeSpan newOne) {
         displayedSpans.add(newOne);
         if (isMeasured)
             newOne.onSizeChanged(getMeasuredWidth(), getMeasuredHeight(), boundaries);
     }
 
     private void removeSpan(VisualTimeSpan span) {
         span.release();
         displayedSpans.remove(span);
     }
 
     public void setValue(TimeSpanGroup group) {
         if(group == null)
             throw new IllegalArgumentException("Null group specified");
 
         for(VisualTimeSpan span : displayedSpans)
             span.release();
         displayedSpans.clear();
         daysSelector.setSelectedDays(group.getDayMask());
         for (TimeSpan span : group.getCollection()) {
             appendSpan(VisualTimeSpan.fromSpan(this, span));
         }
         recalcOutLabels(false);
         invalidate();
     }
 
     public TimeSpanGroup getValue() {
         int days = daysSelector.getSelectedDays();
         TimeSpanGroup result;
         try {
             result = TimeSpanGroup.fromVisualSpanCollection(days, displayedSpans);
         } catch (IllegalArgumentException e) {
             Log.e("TimeSpanGroupEditor", "WTF??? Invalid day mask from VisualDaysSelector!");
             result = TimeSpanGroup.emptyEverydayGroup();
         }
 
         return result;
     }
 
     private boolean checkOverlap() {
         boolean alter = false;
         VisualTimeSpan current = null;
         if (displayedSpans.size() < 2)
             return false;
         Set<VisualTimeSpan> copySet = new TreeSet<VisualTimeSpan>(displayedSpans);
         for (VisualTimeSpan span : copySet) {
             if (current != null) {
                 if (current.intersects(span)) {
                     alter = true;
                     removeSpan(current);
                     removeSpan(span);
                     appendSpan(current = current.join(span));
                 } else {
                     current = span;
                 }
             } else
                 current = span;
         }
         return alter;
     }
 
     public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
         return false;
     }
 
     public boolean onDoubleTap(MotionEvent motionEvent) {
         if (boundaries.contains((int)motionEvent.getX(), (int)motionEvent.getY())) {
             for (VisualTimeSpan span : displayedSpans) {
                 if (span.hitTest(motionEvent) == VisualTimeSpan.HitTestResult.JUST_IN) {
                     if (displayedSpans.size() > 1 && span.isEditMode()) {
                         displayedSpans.remove(span);
                         recalcOutLabels(false);
                         invalidate();
                     }
                     return true;
                 }
             }
 
             float minutesTapped = pixelPointToMinutes(screenToControl(motionEvent.getY()));
             appendSpan(VisualTimeSpan.newInstanceAtValues(this, minutesTapped - 90, minutesTapped + 90));
             invalidate();
             return true;
         }
 
         return false;
     }
 
     public boolean onDoubleTapEvent(MotionEvent motionEvent) {
         return false;
     }
 
     private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
         @Override
         public boolean onScale(ScaleGestureDetector detector) {
 
             scaleFactor *= 1/detector.getScaleFactor();
 
             scaleFactor = Math.max(0.25f, Math.min(scaleFactor, 1.0f));
 
             setScale((int) (24 * scaleFactor));
             return true;
         }
     }
 
 
 }
