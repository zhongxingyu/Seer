 package com.anotherbrick.inthewall;
 
 import static com.anotherbrick.inthewall.Helper.*;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 
 import processing.core.PApplet;
 import processing.core.PVector;
 
 import com.anotherbrick.inthewall.Config.MyColorEnum;
 import com.anotherbrick.inthewall.VizTimeline.Modes;
 
 public class VizTimeSlider extends VizPanel implements TouchEnabled {
 
  public float PADDING_LEFT = 40;
   public float HANDLE_WIDTH = 20;
   public float HANDLE_HEIGHT = 25;
   public float PADDING_BOTTOM = 13;
 
   private VizGraph graph;
   private float maxValue, minValue;
   private float zoomAreaWidth;
   private ArrayList<PlotData> plots;
   private Handle leftHandle, rightHandle;
   private ZoomArea zoomArea;
   private float distanceFromLeftHandle;
   private float distanceFromLRightHandle;
 
   public VizTimeSlider(float x0, float y0, float width, float height, VizPanel parent,
       VizGraph graph) {
     super(x0, y0, width, height, parent);
     minValue = x0;
    maxValue = width;
     zoomAreaWidth = width;
     this.graph = graph;
   }
 
   public void setup() {
     plots = new ArrayList<PlotData>();
     leftHandle = new Handle(PADDING_LEFT, 0, HANDLE_WIDTH, HANDLE_HEIGHT, this);
     rightHandle = new Handle(getWidth() - HANDLE_WIDTH, 0, HANDLE_WIDTH, HANDLE_HEIGHT, this);
     zoomArea = new ZoomArea(leftHandle.getX0() + HANDLE_WIDTH, 0, rightHandle.getX0()
         - leftHandle.getX0() - HANDLE_WIDTH, this);
   }
 
   public void setMaxTimeRange() {
     leftHandle.modifyPosition(PADDING_LEFT, 0);
     rightHandle.modifyPosition(getWidth() - HANDLE_WIDTH, 0);
     zoomArea.modifyPositionAndSize(leftHandle.getX0() + HANDLE_WIDTH, 0, rightHandle.getX0()
         - leftHandle.getX0() - HANDLE_WIDTH, HANDLE_HEIGHT);
   }
 
   private void setXStart(float x0) {
     ArrayList<PlotData> plots = new ArrayList<PlotData>();
 
     if (!graph.isClustered()) {
       plots = this.plots;
     } else {
       plots = graph.getClusteredPlots();
     }
 
     Float xStart = PApplet.map(x0, PADDING_LEFT, getWidth(), getOverallXMin(plots),
         getOverallXMax(plots));
     graph.setX0(xStart.intValue());
   }
 
   private void setXStop(float xn) {
     ArrayList<PlotData> plots = new ArrayList<PlotData>();
 
     if (!graph.isClustered()) {
       plots = this.plots;
     } else {
       plots = graph.getClusteredPlots();
     }
 
     Float xStop = PApplet.map(xn, PADDING_LEFT, getWidth(), getOverallXMin(plots),
         getOverallXMax(plots));
     graph.setXn(xStop.intValue());
   }
 
   @Override
   public boolean draw() {
     pushStyle();
     background(MyColorEnum.DARK_GRAY);
     rectMode(PApplet.CORNER);
     fill(MyColorEnum.MEDIUM_GRAY);
     rect(PADDING_LEFT, 0, getWidth() - PADDING_LEFT, getHeight());
     ArrayList<PlotData> drawPlots = new ArrayList<PlotData>();
 
     for (PlotData p : plots) {
       if (p != null) {
         drawPlots.add(p);
       }
     }
 
     Collections.sort(drawPlots, new Comparator<PlotData>() {
 
       @Override
       public int compare(PlotData p1, PlotData p2) {
         return (int) (p2.getYPointsSum() - p1.getYPointsSum());
       }
     });
 
     for (PlotData plot : drawPlots) {
       drawPlot(plot);
     }
 
     updateHandlesPosition();
 
     rightHandle.draw();
     leftHandle.draw();
     zoomArea.draw();
     drawXAxisLabels();
     popStyle();
     return (leftHandle.moving || rightHandle.moving || zoomArea.moving);
   }
 
   private void drawPlot(PlotData plot) {
     if (plot != null) {
       ArrayList<PVector> points = plot.getPoints();
 
       pushStyle();
       stroke(plot.getColor());
       strokeWeight(plot.getWeight());
       fill(plot.getColor(), plot.getAlpha());
 
       beginShape();
       for (PVector point : points) {
         float x = PApplet.map(point.x, plot.getXMin(), plot.getXMax(), PADDING_LEFT, getWidth());
         float y = PApplet
             .map(point.y, getOverallYMin(plots), getOverallYMax(plots), getHeight(), 0);
 
         vertex(x, y);
       }
 
       if (plot.isFilled()) {
         vertex(getWidth(), getHeight());
         vertex(PADDING_LEFT, getHeight());
         endShape(PApplet.CLOSE);
       }
       endShape();
       popStyle();
     }
   }
 
   private void drawXAxisLabels() {
     pushStyle();
     fill(MyColorEnum.WHITE);
     stroke(MyColorEnum.WHITE);
     strokeWeight((float) 1);
     textAlign(PApplet.CENTER, PApplet.BASELINE);
     textSize(12);
     for (int i = getOverallXMin(plots).intValue(); i < getOverallXMax(plots).intValue(); i++) {
       if (i % 10 == 0) {
         int x = (int) PApplet.map(i, getOverallXMin(plots), getOverallXMax(plots), PADDING_LEFT,
             getWidth());
         text(Integer.toString(i), x, getHeight());
         line(x, HANDLE_HEIGHT, x, getHeight() - PADDING_BOTTOM);
       }
     }
     popStyle();
   }
 
   @Override
   public boolean touch(float x, float y, boolean down, TouchTypeEnum touchType) {
     println("--> touch in VizTimeSlider");
     if (!graph.isClustered()) {
       if (down) {
         if (overLeftKnob(x, y)) {
           println("left handle touched..");
           leftHandle.moving = true;
         } else if (overRightKnob(x, y)) {
           rightHandle.moving = true;
         } else if (overZoomArea(x, y)) {
           leftHandle.moving = true;
           rightHandle.moving = true;
           zoomArea.moving = true;
           distanceFromLeftHandle = x / c.multiply - leftHandle.getX0Absolute();
           distanceFromLRightHandle = rightHandle.getX0Absolute() - x / c.multiply;
         }
         setModal(true);
         return true;
       } else if (!down) {
         leftHandle.moving = false;
         rightHandle.moving = false;
         zoomArea.setMoving(false);
         setModal(false);
       }
     }
     return false;
   }
 
   public void costrainHandlesForTable() {
     float e = getX0() + getWidth();
     rightHandle
         .modifyPositionWithAbsoluteValue(
             costrain(leftHandle.getX0Absolute() + 67, e - HANDLE_WIDTH,
                 leftHandle.getX0Absolute() + 67), rightHandle.getY0Absolute());
     zoomArea.modifyPositionAndSizeWithAbsoluteValue(leftHandle.getX0Absolute() + HANDLE_WIDTH,
         zoomArea.getY0Absolute(), rightHandle.getX0() - leftHandle.getX0() - HANDLE_WIDTH,
         HANDLE_HEIGHT);
 
   }
 
   public void updateHandlesPosition() {
     if (!plots.isEmpty()) {
       if (zoomArea.moving) {
         float o = PADDING_LEFT + getX0();
         float e = getX0() + getWidth();
         leftHandle.modifyPositionWithAbsoluteValue(
             costrain(m.touchX - distanceFromLeftHandle, e - zoomArea.getWidth() - 2 * HANDLE_WIDTH,
                 o), leftHandle.getY0Absolute());
         rightHandle.modifyPositionWithAbsoluteValue(
             costrain(m.touchX + distanceFromLRightHandle, e - HANDLE_WIDTH, o + HANDLE_WIDTH
                 + zoomArea.getWidth()), rightHandle.getY0Absolute());
         zoomArea.modifyPositionWithAbsoluteValue(leftHandle.getX0Absolute() + HANDLE_WIDTH,
             zoomArea.getY0Absolute());
         setXStart(leftHandle.getX0());
         setXStop(rightHandle.getX0() + HANDLE_WIDTH);
         graph.forceYearSliderUpdate();
       } else if (leftHandle.moving) {
         leftHandle.modifyPositionWithAbsoluteValue(
             costrain(m.touchX, rightHandle.getX0Absolute() - HANDLE_WIDTH, PADDING_LEFT + getX0()),
             leftHandle.getY0Absolute());
         zoomArea.modifyPositionAndSizeWithAbsoluteValue(leftHandle.getX0Absolute() + HANDLE_WIDTH,
             zoomArea.getY0Absolute(), rightHandle.getX0() - leftHandle.getX0() - HANDLE_WIDTH,
             HANDLE_HEIGHT);
         setXStart(leftHandle.getX0());
         graph.forceYearSliderUpdate();
       } else if (rightHandle.moving) {
         rightHandle.modifyPositionWithAbsoluteValue(
             costrain(m.touchX, maxValue + getX0() - HANDLE_WIDTH, leftHandle.getX0Absolute()
                 + HANDLE_WIDTH), rightHandle.getY0Absolute());
         zoomArea.modifyPositionAndSizeWithAbsoluteValue(leftHandle.getX0Absolute() + HANDLE_WIDTH,
             zoomArea.getY0Absolute(), rightHandle.getX0() - leftHandle.getX0() - HANDLE_WIDTH,
             HANDLE_HEIGHT);
         setXStop(rightHandle.getX0() + HANDLE_WIDTH);
         graph.forceYearSliderUpdate();
       }
     }
 
   }
 
   private boolean overZoomArea(float x, float y) {
     return zoomArea.containsPoint(x, y);
   }
 
   private boolean overRightKnob(float x, float y) {
     return rightHandle.containsPoint(x, y);
   }
 
   private boolean overLeftKnob(float x, float y) {
     return leftHandle.containsPoint(x, y);
   }
 
   private float costrain(float value, float maxValue, float minValue) {
     return Math.min(Math.max(value, minValue), maxValue);
   }
 
   public void addPlot(PlotData plot, int index) {
     try {
       plots.set(index, plot);
     } catch (IndexOutOfBoundsException e) {
       plots.ensureCapacity(index + 1);
       while (plots.size() < index + 1) {
         plots.add(null);
       }
       plots.set(index, plot);
     }
 
   }
 
   public void removePlotAtIndex(int index) {
     try {
       plots.set(index, null);
     } catch (IndexOutOfBoundsException e) {
     }
   }
 
   private class Handle extends VizPanel {
 
     boolean moving = false;
 
     public Handle(float x0, float y0, Float width, Float height, VizPanel parent) {
       super(x0, y0, width, height, parent);
     }
 
     @Override
     public boolean draw() {
       background(MyColorEnum.WHITE);
       return false;
     }
 
     @Override
     public void setup() {
     }
   }
 
   private class ZoomArea extends VizPanel {
 
     private boolean moving = false;
 
     public ZoomArea(float x0, float y0, float width, VizPanel parent) {
       super(x0, y0, width, HANDLE_HEIGHT, parent);
     }
 
     @Override
     public boolean draw() {
       background(MyColorEnum.WHITE, 100f);
       return false;
     }
 
     @SuppressWarnings("unused")
     public boolean isMoving() {
       return moving;
     }
 
     public void setMoving(boolean moving) {
       this.moving = moving;
     }
 
     @Override
     public void setup() {
       // TODO Auto-generated method stub
 
     }
 
   }
 
 }
