 package net.edmacdonald.playground.java2d;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.axis.ValueAxis;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.data.Range;
 import org.jfree.data.xy.DefaultTableXYDataset;
 import org.jfree.data.xy.XYDataset;
 import org.jfree.data.xy.XYSeries;
 import org.jfree.data.xy.XYSeriesCollection;
 
 import javax.swing.*;
 import java.awt.*;
 
 public class PlotPanel extends JPanel{
 
    private Integer radius = 1;
     private Integer pointCount = 10;
     private Integer horizontalOffset = 0;
     private Integer verticalOffset = 0;
     private Integer rotationAngle = 0;
 
     private ChartPanel chartPanel;
 
     private Log log = LogFactory.getLog(PlotPanel.class);
 
     public PlotPanel(){
 
         XYSeriesCollection dataset = new XYSeriesCollection();
         dataset.addSeries(getSeries());
 
         JFreeChart chart = ChartFactory.createXYLineChart(null, null, null, dataset, PlotOrientation.VERTICAL, false, false, false);
 
         chart.getXYPlot().getDomainAxis().setRange(new Range(-10, 10));
         chart.getXYPlot().getRangeAxis().setRange(new Range(-10, 10));
 
         chartPanel = new ChartPanel(chart);
 
         add(chartPanel);
         log.debug("Chart Panel dimension: " + chartPanel.getPreferredSize());
         setPreferredSize( chartPanel.getPreferredSize() );
         setBorder(BorderFactory.createEtchedBorder());
         setVerifyInputWhenFocusTarget(true);
     }
 
     public void plot() {
         XYPlot plot = chartPanel.getChart().getXYPlot();
         plot.setDataset(null);
         XYSeriesCollection dataset = new XYSeriesCollection();
         dataset.addSeries(getSeries());
         plot.setDataset(dataset);
     }
 
     public XYSeries getSeries() {
         XYSeries series = new XYSeries("PolygonSeries", false);
         double x, y, theta;
 
         for(int i=0; i<=pointCount; i++) {
             theta = (Math.PI / 180) * ((360 * i / pointCount) + rotationAngle);
             x = radius * Math.cos(theta) + horizontalOffset;
             y = radius * Math.sin(theta) + verticalOffset;
             log.debug("Adding coordinates <x, y>: <" + x + ", " + y + ">");
             series.add(x,y);
         }
 
         return series;
     }
 
     public Integer getRadius() {
         return radius;
     }
 
     public void setRadius(Integer radius) {
         this.radius = radius;
         log.trace("Radius changed. New value: " + radius);
     }
 
     public Integer getPointCount() {
         return pointCount;
     }
 
     public void setPointCount(Integer pointCount) {
         this.pointCount = pointCount;
         log.trace("Point count changed. New value: " + pointCount);
     }
 
     public Integer getHorizontalOffset() {
         return horizontalOffset;
     }
 
     public void setHorizontalOffset(Integer horizontalOffset) {
         this.horizontalOffset = horizontalOffset;
         log.trace("Horizontal offset changed. New value: " + horizontalOffset);
     }
 
     public Integer getVerticalOffset() {
         return verticalOffset;
     }
 
     public void setVerticalOffset(Integer verticalOffset) {
         this.verticalOffset = verticalOffset;
         log.trace("Vertical offset changed. New value: " + verticalOffset);
     }
 
     public Integer getRotationAngle() {
         return rotationAngle;
     }
 
     public void setRotationAngle(Integer rotationAngle) {
         this.rotationAngle = rotationAngle;
         log.trace("Rotation angle changed. New value: " + rotationAngle);
     }
 
     public ChartPanel getChartPanel() {
         return chartPanel;
     }
 }
