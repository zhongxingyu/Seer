 /**
  * Copyright (C) 2013 Henning Dodenhof
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
 package org.achartengine.chart;
 
 import java.util.List;
 
 import org.achartengine.renderer.SimpleSeriesRenderer;
 
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Style;
 
 /**
  * The line chart rendering class.
  */
 public class RangeOverlayChart extends XYChart {
   /** The constant to identify this chart type. */
  public static final String TYPE = "RangeOverlayChart";
 
   RangeOverlayChart() {
   }
 
   /**
    * The graphical representation of a series.
    * 
    * @param canvas the canvas to paint to
    * @param paint the paint to be used for drawing
    * @param points the array of points to be used for drawing the series
    * @param seriesRenderer the series renderer
    * @param yAxisValue the minimum value of the y axis
    * @param seriesIndex the index of the series currently being drawn
    * @param startIndex the start index of the rendering points
    */
   @Override
   public void drawSeries(Canvas canvas, Paint paint, List<Float> points,
       SimpleSeriesRenderer seriesRenderer, float yAxisValue, int seriesIndex, int startIndex) {
     if (points.size() < 4) {
       return;
     }
 
     float min = Float.MAX_VALUE;
     float max = Float.MIN_VALUE;
     float target = Float.NaN;
 
     for (int i = 1; i < points.size(); i += 2) {
       min = Math.min(min, points.get(i));
       max = Math.max(max, points.get(i));
     }
 
     for (int i = 1; i < points.size(); i += 2) {
       if (points.get(i) != min && points.get(i) != max) {
         target = points.get(i);
         break;
       }
     }
 
     Paint overlayPaint = new Paint();
     overlayPaint.setColor(seriesRenderer.getColor());
     overlayPaint.setStyle(Style.FILL);
 
     Paint linePaint = new Paint();
     linePaint.setColor(Color.BLACK);
     linePaint.setStrokeWidth(2);
 
     canvas.drawRect(0, (float) min, canvas.getWidth(), (float) max, overlayPaint);
 
     if (target != Float.NaN) {
       canvas.drawLine(0, (float) target, canvas.getWidth(), (float) target, linePaint);
     }
   }
 
   @Override
   protected boolean isRenderNullValues() {
     return true;
   }
 
   @Override
   public double getDefaultMinimum() {
     return 0;
   }
 
   /**
    * Returns the chart type identifier.
    * 
    * @return the chart type
    */
   public String getChartType() {
     return TYPE;
   }
 
   @Override
   protected ClickableArea[] clickableAreasForPoints(List<Float> points, List<Double> values,
       float yAxisValue, int seriesIndex, int startIndex) {
     return new ClickableArea[] {};
   }
 
   @Override
   public int getLegendShapeWidth(int seriesIndex) {
     return 0;
   }
 
   @Override
   public void drawLegendShape(Canvas canvas, SimpleSeriesRenderer renderer, float x, float y,
       int seriesIndex, Paint paint) {
   }
 
 }
