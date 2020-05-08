 package org.hackystat.projectbrowser.page.projectportfolio.detailspanel;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import org.apache.wicket.PageParameters;
 import org.hackystat.projectbrowser.googlechart.ChartType;
 import org.hackystat.projectbrowser.googlechart.GoogleChart;
 import org.hackystat.telemetry.service.resource.chart.jaxb.TelemetryPoint;
 import org.hackystat.telemetry.service.resource.chart.jaxb.TelemetryStream;
 
 /**
  * A mini bar chart.
  * Use GoogleChart to generate the chart.
  * @author Shaoxuan Zhang
  *
  */
 public class MiniBarChart implements Serializable {
 
   /** Support serialization. */
   private static final long serialVersionUID = 1213447209533926430L;
   
   /** The stream of this chart. */
   protected List<Double> streamData;
   /** The latest value of the stream. */
   private double latestValue;
   /** The width of a bar. */
   public static final int BAR_WIDTH = 5;
   /** The max width of the chart. */
   public static final int MAX_WIDTH = 50;
   /** The height of the chart. */
   public static final int CHART_HEIGHT = 15;
   
   /** The PageParameters to construct the link. */
   private PageParameters telemetryPageParameters;
 
   /** The configuration of this chart. */
   private final MeasureConfiguration configuration;
   
   /**
    * @param stream The stream of this chart.
    * @param configuration The configuration of this chart.
    */
   public MiniBarChart(TelemetryStream stream, MeasureConfiguration configuration) {
     this.streamData = getStreamData(stream);
     for (int i = streamData.size() - 1; i >= 0; --i) {
       this.latestValue = streamData.get(i);
       if (latestValue >= 0) {
         break;
       }
     }
     this.configuration = configuration;
   }
 
   /**
    * Return a List of Double from the given telemetry stream.
    * @param stream a TelemetryStream
    * @return the list of data of this stream
    */
   protected final List<Double> getStreamData(TelemetryStream stream) {
     List<Double> streamData = new ArrayList<Double>();
     for (TelemetryPoint point : stream.getTelemetryPoint()) {
       if (point.getValue() == null) {
         streamData.add(-1.0);
       }
       else {
         Double value = Double.valueOf(point.getValue());
         if (value.isNaN()) {
           value = -2.0;
         }
         streamData.add(value);
       }
     }
     return streamData;
   }
   
   /**
    * @return a String URL that represents this chart.
    */
   public String getImageUrl() {
     double max = Collections.max(streamData);
     //return empty string when the chart is empty.
     if (max < 0) {
       return "";
     }
     GoogleChart googleChart;
     int chartSize = streamData.size();
     int width = chartSize * BAR_WIDTH;
     width = (width > MAX_WIDTH) ? MAX_WIDTH : width;
     googleChart = new GoogleChart(ChartType.VERTICAL_SERIES_BAR, width, CHART_HEIGHT);
     googleChart.setBarChartSize((width - streamData.size()) / streamData.size(), 1, 0);
     googleChart.getChartData().add(streamData);
     googleChart.getChartDataScale().
             add(Arrays.asList(new Double[]{ 0.0, max}));
     googleChart.getColors().add(this.getChartColor());
     googleChart.setBackgroundColor(this.configuration.getDataModel().getBackgroundColor());
     return googleChart.getUrl();
   }
 
 
   /**
    * @return the color
    */
   public String getChartColor() {
     if (configuration.isColorable()) {
       boolean increased = false;
       boolean decreased = false;
       for (int i = 1; i < this.streamData.size(); ++i) {
         if (this.streamData.get(i) >= 0) {
           if (this.streamData.get(i - 1) > this.streamData.get(i)) {
             decreased = true;
           }
           else if (this.streamData.get(i - 1) < this.streamData.get(i)) {
             increased = true;
           }
         }
       }
       if (!increased && !decreased) {
         return configuration.getStableColor();
       }
       if (increased && !decreased) {
         return configuration.getHigherColor();
       }
       if (decreased && !increased) {
         return configuration.getLowerColor();
       }
       return configuration.getMiddleColor();
     }
     else {
       return configuration.getDataModel().getFontColor();
     }
   }
 
   /**
    * @return the latestValue
    */
   public double getLatestValue() {
     return latestValue;
   }
 
   /**
    * @return the valueColor
    */
   public String getValueColor() {
     if (configuration.isColorable()) {
      if (this.getLatestValue() < 0) {
        return configuration.getDataModel().getFontColor();
      }
       if (this.getLatestValue() >= this.configuration.getHigherThreshold()) {
         return configuration.getHigherColor();
       }
       if (this.getLatestValue() < this.configuration.getLowerThreshold()) {
         return configuration.getLowerColor();
       }
       return configuration.getMiddleColor();
     }
     else {
       return configuration.getDataModel().getFontColor();
     }
   }
 
   /**
    * @param telemetryPageParameters the telemetryPageParameters to set
    */
   public void setTelemetryPageParameters(PageParameters telemetryPageParameters) {
     this.telemetryPageParameters = telemetryPageParameters;
   }
 
   /**
    * @return the telemetryPageParameters
    */
   public PageParameters getTelemetryPageParameters() {
     return telemetryPageParameters;
   }
 
   /**
    * @return the configuration
    */
   public MeasureConfiguration getConfiguration() {
     return configuration;
   }
 
 }
