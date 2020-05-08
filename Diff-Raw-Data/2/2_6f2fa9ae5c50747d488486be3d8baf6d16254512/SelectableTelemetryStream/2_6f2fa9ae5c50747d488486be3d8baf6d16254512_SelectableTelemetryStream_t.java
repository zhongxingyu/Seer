 package org.hackystat.projectbrowser.page.telemetry.datapanel;
 
 import java.io.Serializable;
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.List;
 import org.hackystat.telemetry.service.resource.chart.jaxb.TelemetryPoint;
 import org.hackystat.telemetry.service.resource.chart.jaxb.TelemetryStream;
 
 /**
  * Group a selected flag with a TelemetryStream, 
  * so that this instance can be flagged as selected or not.
  * @author Shaoxuan
  *
  */
 public class SelectableTelemetryStream implements Serializable {
   /** Support serialization. */
   public static final long serialVersionUID = 1L;
   /** Determine this stream is selected or not. */
   private boolean selected = false;
   /** The TelemetryStream of this stream. */
   private final TelemetryStream telemetryStream;
   /** The color of this stream. */
   private String streamColor = "";
   /** The color of the marker of this stream. */
   private String markerColor = "";
   /** the marker of this stream. */
   private String marker = "";
   /** thickness of the line.*/
   private double thickness = 2;
   /** length of the line segment.*/
   private double lineLength = 1;
   /** length of the blank segment.*/
   private double blankLength = 0;
   /** the maximum of this stream. */
   private double maximum;
   /** the minimum of this stream. */
   private double minimum;
   /**
    * @param telemetryStream the TelemetryStream of this instance.
    */
   public SelectableTelemetryStream(TelemetryStream telemetryStream) {
     this.telemetryStream = telemetryStream;
     //initial the maximum and minimum value.
     List<Double> streamData = this.getStreamData();
     maximum = -1;
     minimum = 99999999;
     for (Double value : streamData) {
       if (value > maximum) {
         maximum = value;
       }
       if (value >= 0 && value < minimum) {
         minimum = value;
       }
     }
     if (!isEmpty()) {
       BigInteger upperBound = this.telemetryStream.getYAxis().getUpperBound();
       BigInteger lowerBound = this.telemetryStream.getYAxis().getLowerBound();
       
       //TODO : developing test out, delete when done.
       /*
       String name = this.telemetryStream.getName();
       System.out.println("Stream: " + name + "'s upperBound is " + upperBound);
       System.out.println("Stream: " + name + "'s lowerBound is " + lowerBound);
       */
       
       if (upperBound != null && upperBound.doubleValue() > maximum) {
         maximum = upperBound.doubleValue();
       }
       if (lowerBound != null && lowerBound.doubleValue() < minimum) {
         minimum = lowerBound.doubleValue();
       }
     }
   }
   /**
    * @param selected the selected to set
    */
   public void setSelected(boolean selected) {
     this.selected = selected;
   }
   /**
    * @return the selected
    */
   public boolean isSelected() {
     return selected;
   }
   /**
    * @return the telemetryStream
    */
   public TelemetryStream getTelemetryStream() {
     return telemetryStream;
   }
   /**
    * Set the color to both stream and marker color.
    * @param color the color to set.
    */
   public void setColor(String color) {
     this.setStreamColor(color);
     this.setMarkerColor(color);
   }
   /**
    * @return color of stream.
    */
   public String getStreamColor() {
     return streamColor;
   }
   /**
    * @param streamColor the stream color to set.
    */
   public void setStreamColor(String streamColor) {
     this.streamColor = streamColor;
   }
   /**
    * @return color of marker.
    */
   public String getMarkerColor() {
     return markerColor;
   }
   /**
    * @param markerColor the marker color to set.
    */
   public void setMarkerColor(String markerColor) {
     this.markerColor = markerColor;
   }
   /**
    * Returns a background-color attribute with the value of color.
    * @return The background-color key-value pair.
    */
   public String getBackgroundColorValue() {
     return "background-color:#" + getStreamColor();
   }
   /**
    * @param marker the marker to set
    */
   public void setMarker(String marker) {
     this.marker = marker;
   }
   /**
    * @return the marker
    */
   public String getMarker() {
     return marker;
   }
   /**
    * @return the isEmpty
    */
   public final boolean isEmpty() {
     return this.maximum < 0;
   }
   /**
    * @return the maximum
    */
   public double getMaximum() {
     return maximum;
   }
   /**
    * @return the minimum
    */
   public double getMinimum() {
     return minimum;
   }
   
   /**
    * @param thickness the thickness to set
    */
   public void setThickness(double thickness) {
     this.thickness = thickness;
   }
   /**
    * @return the thickness
    */
   public double getThickness() {
     return thickness;
   }
   /**
    * @param lineLength the lineLength to set
    */
   public void setLineLength(double lineLength) {
     this.lineLength = lineLength;
   }
   /**
    * @return the lineLength
    */
   public double getLineLength() {
     return lineLength;
   }
   /**
    * @param blankLength the blankLength to set
    */
   public void setBlankLength(double blankLength) {
     this.blankLength = blankLength;
   }
   /**
    * @return the blankLength
    */
   public double getBlankLength() {
     return blankLength;
   }
   /**
    * @return the list of data of this stream
    */
   public final List<Double> getStreamData() {
     List<Double> streamData = new ArrayList<Double>();
     for (TelemetryPoint point : this.getTelemetryStream().getTelemetryPoint()) {
      if (point.getValue() == null || String.valueOf(point.getValue()).isEmpty()) {
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
    * Return a image url that shows only one marker.
    * Using google chart to generate this image.
    * there is an example output:
    * http://chart.apis.google.com/chart?
    *    chs=20x20&cht=ls&chd=t:-1,1.0,-1&chds=0.9,1.1&chm=c,FF0000,0,-1,20.0
    * @return the image url
    */
   public String getMarkerImageUrl() {
     if (!this.isSelected() || this.isEmpty() || this.marker.length() <= 0) {
       return "";
     }
     String imageUrl = "http://chart.apis.google.com/chart?" +
             "chs=45x15&cht=ls&chd=t:1.0,1.0,1.0&chds=0.9,1.1&" +
             "chm=" + marker + "," + markerColor + ",0,1,10.0&" +
             "chls=" + thickness + "," + lineLength + "," + blankLength + "&" +
             "chco=" + streamColor;
     return imageUrl;
   }
   
   /**
    * Return the Unit of this stream.
    * @return String of the unit.
    */
   public String getUnitName() {
     return this.telemetryStream.getYAxis().getUnits();
   }
   /**
    * Return the name of this stream.
    * @return String of the name.
    */
   public String getStreamName() {
     return this.telemetryStream.getName();
   }
 }
