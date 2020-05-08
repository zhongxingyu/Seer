 package ca.scotthyndman.sparklines;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Paint.Style;
 import android.util.AttributeSet;
 
 /**
  * Displays a box plot sparkline.
  *  
  * @see http://en.wikipedia.org/wiki/Box_plot
  * @author Scott Hyndman
  */
 public class BoxPlotSparkView extends SparkView {
 
   // DEFAULT PAINTS (set at bottom in static constructor)
   
   private static final Paint DEFAULT_WHISKER_PAINT;
   private static final Paint DEFAULT_OUTLIER_LINE_PAINT;
   private static final Paint DEFAULT_OUTLIER_FILL_PAINT;
   private static final Paint DEFAULT_MEDIAN_PAINT;
   private static final Paint DEFAULT_TARGET_PAINT;
   
   
   private float outlierIQR = 1.5f;
   private float target;
   
   private float spotRadius = 2f;
   private boolean showTarget = false;
   private boolean showOutliers = true;
   private boolean rawMode = false;
   private DrawInfo drawInfo;
   
   private Paint whiskerPaint = new Paint(DEFAULT_WHISKER_PAINT);
   private Paint outlierLinePaint = new Paint(DEFAULT_OUTLIER_LINE_PAINT);
   private Paint outlierFillPaint = new Paint(DEFAULT_OUTLIER_FILL_PAINT);  
   private Paint medianPaint = new Paint(DEFAULT_MEDIAN_PAINT);
   private Paint targetPaint = new Paint(DEFAULT_TARGET_PAINT);
   
   
   public BoxPlotSparkView(Context context) { super(context); }
   public BoxPlotSparkView(Context context, AttributeSet attrs) { super(context, attrs); }
   
   
   public float getOutlierIQR() { return outlierIQR; }
   public void setOutlierIQR(float outlierIQR) { this.outlierIQR = outlierIQR; }
   
   public float getTarget() { return target; }
   public void setTarget(float target) { this.target = target; }
   
   public boolean getShowTarget() { return showTarget; }
   public void setShowTarget(boolean showTarget) { this.showTarget = showTarget; }
   
   /**
    * Returns <code>true</code> if outliers are shown on the box plot.
    */
   public boolean getShowOutliers() { return showOutliers; }
   
   /**
    * Sets whether outliers are shown on the box plot.
    */
   public void setShowOutliers(boolean showOutliers) { this.showOutliers = showOutliers; }
   
   /**
    * Returns <code>true</code> if the box plot exists in raw mode. See 
    * {@link #setRawMode(boolean)} for more details.
    */
   public boolean isRawMode() { return rawMode; }
   
   /**
    * <p>Sets whether this chart exists in "raw" mode.
    * 
    * <p>If <code>true</code>, the values rendered by this chart ({@link #getValues()}) are
    * assumed to be pre-calculated. 
    * 
    * <p>If {@link #getShowOutliers()} is <code>true</code>, the list passed to 
    * {@link #setValues(java.util.List)} should contain the following values, in order:
    * 
    * <pre>
    * [low_outlier, low_whisker, q1, median, q3, high_whisker, high_outlier]
    * </pre>
    * 
    * <p>If {@link #getShowOutliers()} is <code>false</code>, the list passed to
    * {@link #setValues(java.util.List)} should contain the following values, in order:  
    * 
    * <pre>
    * low_whisker, q1, median, q3, high_whisker 
    * </pre>
    */
   public void setRawMode(boolean rawMode) { this.rawMode = rawMode; }
   
   public void setWhiskerColor(int color) { whiskerPaint.setColor(color); }
   public void setOutlierLineColor(int color) { outlierLinePaint.setColor(color); }
   public void setOutlierFillColor(int color) { outlierFillPaint.setColor(color); }
   public void setTargetColor(int color) { targetPaint.setColor(color); }
   public void setMedianColor(int color) { medianPaint.setColor(color); }
   
   
   //
   // INVALIDATION
   //
   
   @Override
   protected DrawInfo getDrawInfo() {
     if (drawInfo == null) {
       drawInfo = new DrawInfo();
       
       // Now concept of Y on this graph, so we express it as a value from
       // 0 to 1, so that we can have visual elements occupy percentages.
       drawInfo.minDrawnY = 0;
       drawInfo.maxDrawnY = 1;
       drawInfo.drawRangeY = 1;
     }
     
     return drawInfo;
   }
   
   @Override
   protected void calculateFeatures() {
     if (rawMode)
       populateFromRawValues();
     else
       calculateFromValues();
   }
   
   private void populateFromRawValues() {
     int i = 0;
     
     if (showOutliers) 
       drawInfo.lOutlier = values.get(i++);
     
     drawInfo.lWhisker = values.get(i++);
     drawInfo.q1 = values.get(i++);
     drawInfo.q2 = values.get(i++);
     drawInfo.q3 = values.get(i++);
     drawInfo.rWhisker = values.get(i++);
     
     if (showOutliers)
       drawInfo.rOutlier = values.get(i++);
   }
   
   private void calculateFromValues() {
     List<Float> sortedValues = new ArrayList<Float>(values);
     Collections.sort(sortedValues);
     
     // Get the min max for X up front (for conversions)
     
     drawInfo.minDrawnX = chartRangeSet 
       ? chartRangeMin
       : sortedValues.get(0);
     drawInfo.maxDrawnX = chartRangeSet
       ? chartRangeMax
       : sortedValues.get(sortedValues.size() - 1);
     drawInfo.drawRangeX = drawInfo.maxDrawnX - drawInfo.minDrawnX;
     
     // Get the quarties
     
     drawInfo.q1 = determineQuartile(1, sortedValues);
     drawInfo.q2 = determineQuartile(2, sortedValues);
     drawInfo.q3 = determineQuartile(3, sortedValues);
     
     // If we don't have outliers, let's exit early
     
     if (!showOutliers) {
       drawInfo.lWhisker = sortedValues.get(0);
       drawInfo.rWhisker = sortedValues.get(sortedValues.size() - 1);
       
       return;
     }
     
     // Determine the whiskers (falling just above q1 - IQR or just below q3 + IQR)
 
     float iqr = drawInfo.q3 - drawInfo.q1;
     float lWhiskerLimit = drawInfo.q1 - iqr * outlierIQR;
     float rWhiskerLimit = drawInfo.q3 + iqr * outlierIQR;
     
     Float lWhisker = null, rWhisker = null;
     
     for (Float val : sortedValues) {
       if (lWhisker == null && val > lWhiskerLimit)
         lWhisker = val;
       
      if (rWhisker == null && val < rWhiskerLimit) {
         rWhisker = val;
        break;
       }
     }
     
     drawInfo.lWhisker = lWhisker;
     drawInfo.rWhisker = rWhisker;
     drawInfo.lOutlier = sortedValues.get(0);
     drawInfo.rOutlier = sortedValues.get(values.size() - 1);
   }
   
   private float determineQuartile(int quartile, List<Float> values) {
     // Median
     if (quartile == 2) {
       int halfWay = values.size() / 2;
       return values.size() % 2 == 0 
         ? values.get(halfWay) 
         : (values.get(halfWay) + values.get(halfWay + 1)) / 2;
     }
     
     // q25 / q75
     int quarterWay = values.size() / 4;
     return values.size() % 2 == 0 
       ? (values.get(quarterWay * quartile) + values.get(quarterWay * quartile + 1)) / 2
       : values.get(quarterWay * quartile);
   }
   
   //
   // DRAWING
   //
   
   protected void internalDraw(Canvas canvas) {
     drawBox(canvas);
     drawWhisker(canvas, drawInfo.lWhisker, drawInfo.q1);
     drawWhisker(canvas, drawInfo.rWhisker, drawInfo.q3);
     drawMedian(canvas);
     
     if (showTarget)
       drawTarget(canvas);
     
     if (showOutliers) {
      drawOutlier(canvas, drawInfo.lOutlier);
      drawOutlier(canvas, drawInfo.rOutlier);
     }
   }
   
   private void drawBox(Canvas canvas) {
     canvas.drawRect(
         toCanvasX(drawInfo.q1), 
         toCanvasY(0.9f), 
         toCanvasX(drawInfo.q3), 
         toCanvasY(0.1f), 
         fillPaint);
     canvas.drawRect(
         toCanvasX(drawInfo.q1), 
         toCanvasY(0.9f), 
         toCanvasX(drawInfo.q3), 
         toCanvasY(0.1f), 
         linePaint);
   }
   
   private void drawWhisker(Canvas canvas, float whisker, float toQuartile) {
     canvas.drawLine(
         toCanvasX(whisker),
         toCanvasY(0.5f),
         toCanvasX(toQuartile),
         toCanvasY(0.5f),
         whiskerPaint);
     canvas.drawLine(
         toCanvasX(whisker),
        0.25f,
         toCanvasX(whisker),
         toCanvasY(0.75f),
         whiskerPaint);
   }
 
   private void drawMedian(Canvas canvas) {
     canvas.drawLine(
         toCanvasX(drawInfo.q2), 
         toCanvasY(0.9f), 
         toCanvasX(drawInfo.q2), 
         toCanvasY(0.1f), 
         medianPaint);
   }
   
   private void drawTarget(Canvas canvas) {
     canvas.drawLine(
         toCanvasX(target) - spotRadius,
         toCanvasY(0.5f),
         toCanvasX(target) + spotRadius,
         toCanvasY(0.5f),
         targetPaint);
     canvas.drawLine(
         toCanvasX(target),
         toCanvasY(0.5f) - spotRadius,
         toCanvasX(target),
         toCanvasY(0.5f) + spotRadius,
         targetPaint);
   }
   
   private void drawOutlier(Canvas canvas, float outlier) {
     canvas.drawCircle(
         toCanvasX(outlier), 
         toCanvasY(0.5f), 
         spotRadius, 
         outlierFillPaint);
     canvas.drawCircle(
         toCanvasX(outlier), 
         toCanvasY(0.5f), 
         spotRadius, 
         outlierLinePaint);
   }
 
 
   protected static class DrawInfo extends SparkView.DrawInfo {
     public float lOutlier;
     public float lWhisker;
     public float q1;
     public float q2;
     public float q3;
     public float rWhisker;
     public float rOutlier;
   }
   
   
   static {
     DEFAULT_WHISKER_PAINT = new Paint();
     DEFAULT_WHISKER_PAINT.setStyle(Style.STROKE);
 //    DEFAULT_WHISKER_PAINT.setColor(0xFFB1F2B1);
     DEFAULT_WHISKER_PAINT.setColor(0xFFFF00FF);
     
     DEFAULT_OUTLIER_LINE_PAINT = new Paint();
     DEFAULT_OUTLIER_LINE_PAINT.setStyle(Style.STROKE);
     DEFAULT_OUTLIER_LINE_PAINT.setColor(0xFF333333);
     
     DEFAULT_OUTLIER_FILL_PAINT = new Paint();
     DEFAULT_OUTLIER_FILL_PAINT.setStyle(Style.FILL);
     DEFAULT_OUTLIER_FILL_PAINT.setColor(0xFFFFFFFF);
     
     DEFAULT_MEDIAN_PAINT = new Paint();
     DEFAULT_MEDIAN_PAINT.setStyle(Style.STROKE);
     DEFAULT_MEDIAN_PAINT.setColor(0xFFFF0000);
     
     DEFAULT_TARGET_PAINT = new Paint();
     DEFAULT_TARGET_PAINT.setStyle(Style.STROKE);
     DEFAULT_TARGET_PAINT.setColor(0xFF44AA22);
   }
 }
