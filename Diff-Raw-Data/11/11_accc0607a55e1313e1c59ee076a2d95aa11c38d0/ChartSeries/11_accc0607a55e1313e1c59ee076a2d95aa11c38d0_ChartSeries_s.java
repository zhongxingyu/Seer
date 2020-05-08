 package org.icefaces.ace.model.chart;
 
 import org.icefaces.ace.util.JSONBuilder;
 
 import javax.faces.component.UIComponent;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Copyright 2010-2011 ICEsoft Technologies Canada Corp.
  * <p/>
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * <p/>
  * http://www.apache.org/licenses/LICENSE-2.0
  * <p/>
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * <p/>
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * <p/>
  * User: Nils
  * Date: 12-05-03
  * Time: 1:02 PM
  */
public abstract class ChartSeries {
     public interface ChartType {}
 
     List<Object> data;
 
     // Bar, line, etc.
     ChartType type;
 
     Boolean show;
     Integer xAxis;
     Integer yAxis;
     String label;
     Boolean showLabel;
     String color; // CSS color representation
     Integer lineWidth;
     LineJoin lineJoin; // Round, bevel, miter
     LineCap lineCap; // Butt, line, square
     Boolean shadow;
     Integer shadowAngle; // degrees
     Integer shadowOffset; // pixels
     Integer shadowDepth; // number of shadow strokes, each 1 shadow offset from the last
     Integer shadowAlpha; // 0 - 100 alpha
     Boolean showMarker;
     Boolean fill;
     String fillColor; // CSS
     Integer fillAlpha; // 0 - 100 alpha
     Boolean fillAndStroke;
     Boolean disableStack;
     Boolean fillToZero;
     String fillToValue;
     String fillAxis; // x or y
     Boolean useNegativeColors;
     String[] seriesColors;
 
     /**
      * Return the truth value of this series visibility.
      * @return series visibility truth value
      */
     public Boolean getShow() {
         return show;
     }
 
     /**
      * Set the visibility of this series.
      * @param show series visibility truth value
      */
     public void setShow(Boolean show) {
         this.show = show;
     }
 
     /**
      * Return the truth value of the point marker visibility
      * @return point marker visibility truth value
      */
     public Boolean getShowMarker() {
         return showMarker;
     }
 
     /**
      * Set the visibility of the point markers of this series
      * @param showMarker point marker visibility truth value
      */
     public void setShowMarker(Boolean showMarker) {
         this.showMarker = showMarker;
     }
 
 
     /**
      * Get a integer defining which axis this Series is plotted against.
      * @return the index of the x axis
      */
     public Integer getXAxis() {
         return xAxis;
     }
 
     /**
      * Set a integer defining which axis this Series is plotted against.
      * @param xAxis the index of the x axis
      */
     public void setXAxis(Integer xAxis) {
         this.xAxis = xAxis;
     }
 
     /**
      * Get a integer defining which axis this Series is plotted against.
      * @return the index of the y axis
      */
     public Integer getYAxis() {
         return yAxis;
     }
 
     /**
      * Set a integer defining which axis this Series is plotted against.
      * @param yAxis the index of the y axis
      */
     public void setYAxis(Integer  yAxis) {
         this.yAxis = yAxis;
     }
 
     /**
      * Return fillToZero behaviour truth value
      * @return fillToZero behaviour truth value
      */
     public Boolean getFillToZero() {
         return fillToZero;
     }
 
     /**
      * Enables bar charts to fill to zero but not beyond it. Used in cases
      * where scale shows the bars extending beyond 0 undesirably.
      * @param fillToZero
      */
     public void setFillToZero(Boolean fillToZero) {
         this.fillToZero = fillToZero;
     }
 
 
     /**
      * Return the label of the chart.
       * @return String label value
      */
     public String getLabel() {
         return label;
     }
 
     /**
      * Set the label of the chart used in the Legend etc.
      * @param label String value
      */
     public void setLabel(String label) {
         this.label = label;
     }
 
     /**
      * Return the list of plain Java objects backing the chart.
      * @return List value
      */
     public List<Object> getData() {
         if (data == null)
             data = new ArrayList<Object>();
 
         return data;
     }
 
     /**
      * Set the list of plain Java objects backing the chart.
      * @param data List value
      */
     public void setData(List<Object> data) {
         this.data = data;
     }
 
     /**
      * Enum value determining the type of renderer that will be used client side to plot the
      * data of this series. These ChartTypes are defined as enums fields of the subclasses of ChartSeries
      * in the org.icefaces.ace.model.chart package. These series subclasses are configurable with the options
      * of their contained (supported) ChartTypes.
      * @return enum determining series rendering engine
      */
     public ChartType getType() {
         return type;
     }
 
     /**
      * Set an enum value determining the type of renderer that will be used client side to plot the
      * data of this series. ChartTypes are defined as enum fields of the subclasses of ChartSeries
      * in the org.icefaces.ace.model.chart package. These series subclasses are configurable with the
      * options of their contained (supported) ChartTypes.
      * @param type an enum defined in this subclass of ChartSeries defining the series rendering type
      */
     public void setType(ChartType type) {
         this.type = type;
     }
 
     /**
      * Get whether or not this series fills using an alternate color when rendering in a negative quadrant.
      * @return negative colors enabled
      */
     public Boolean getUseNegativeColors() {
         return useNegativeColors;
     }
 
     /**
      * Enable this series using an alternate color when rendering fills in a negative quadrant.
      * @param useNegativeColors negative colors enabled
      */
     public void setUseNegativeColors(Boolean useNegativeColors) {
         this.useNegativeColors = useNegativeColors;
     }
 
     /**
      * Get whether or not this series fills the bars, sectors or region it covers with
      * the its given color.
      * @return fill enabled
      */
     public Boolean getFill() {
         return fill;
     }
 
     /**
      * Set whether or not this series fills the bars, sectors or region it covers with
      * the its given color.
      * @param fill fill enabled
      */
     public void setFill(Boolean fill) {
         this.fill = fill;
     }
 
     /**
      * Get the width of the line in pixels.
      * @return pixel width
      */
     public Integer getLineWidth() {
         return lineWidth;
     }
 
     /**
      * Set the width of the line in pixels.
      * @param lineWidth pixel width
      */
     public void setLineWidth(Integer lineWidth) {
         this.lineWidth = lineWidth;
     }
 
     /**
      * Get the style of join used to connect line segments of this series.
      * @return line join style enum
      */
     public LineJoin getLineJoin() {
         return lineJoin;
     }
 
     /**
      * Set the the style of join used connect line segments of this series.
      * @param lineJoin line join style enum
      */
     public void setLineJoin(LineJoin lineJoin) {
         this.lineJoin = lineJoin;
     }
 
     /**
      * Get the style of termination used for lines of this series.
      * @return line cap style enum
      */
     public LineCap getLineCap() {
         return lineCap;
     }
 
     /**
      * Set the style of how lines of this series will be terminated
      * @param lineCap line cap style enum
      */
     public void setLineCap(LineCap lineCap) {
         this.lineCap = lineCap;
     }
 
     /**
      * Set if this line casts a shadow.
      * @return whether this line casts a shadow
      */
     public Boolean getShadow() {
         return shadow;
     }
 
     /**
      * Set if this line casts a shadow.
      * @param shadow whether this line casts a shadow
      */
     public void setShadow(Boolean shadow) {
         this.shadow = shadow;
     }
 
     /**
      * Get the angle at which this line casts a shadow.
      * @return angle in degrees
      */
     public Integer getShadowAngle() {
         return shadowAngle;
     }
 
     /**
      * Set the angle at which this line casts a shadow
      * @param shadowAngle angle in degrees
      */
     public void setShadowAngle(Integer shadowAngle) {
         this.shadowAngle = shadowAngle;
     }
 
     /**
      * Get the offset of the shadow from the line.
      * @return offset from the line in pixels.
      */
     public Integer getShadowOffset() {
         return shadowOffset;
     }
 
     /**
      * Set the offset of the shadow from the line.
      * @param shadowOffset offset from the line in pixels
      */
     public void setShadowOffset(Integer shadowOffset) {
         this.shadowOffset = shadowOffset;
     }
 
     /**
      * Get the number of stroke passes rendered by this shadow.
      * Effects width in turn with offset.
      * @return number of shadow line strokes
      */
     public Integer getShadowDepth() {
         return shadowDepth;
     }
 
     /**
      * Set the number of stroke passes rendered by this shadow.
      * Effects width in turn with offset.
      * @param shadowDepth number of shadow line stokes
      */
     public void setShadowDepth(Integer shadowDepth) {
         this.shadowDepth = shadowDepth;
     }
 
     /**
      * Get the transparency of the shadow rendered.
      * @return alpha value 0 to 100
      */
     public Integer getShadowAlpha() {
         return shadowAlpha;
     }
 
     /**
      * Set the transparency of the shadow rendered.
      * @param shadowAlpha alpha value 0 to 100
      */
     public void setShadowAlpha(Integer shadowAlpha) {
         this.shadowAlpha = shadowAlpha;
     }
 
     /**
      * Get the transparency of the filled region under the line.
      * @return integer value 0-100
      */
     public Integer getFillAlpha() {
         return fillAlpha;
     }
 
     /**
      * Set the transparency of the filled region under the line.
      * @param fillAlpha integer value 0-100
      */
     public void setFillAlpha(Integer fillAlpha) {
         this.fillAlpha = fillAlpha;
     }
 
     /**
      * Get the array of CSS color definitions used to color this series.
      * @return array of CSS color definition Strings
      */
     public String[] getSeriesColors() {
         return seriesColors;
     }
 
     /**
      * Set the array of the CSS color definitions.
      * @param seriesColors array of CSS color definition Strings
      */
     public void setSeriesColors(String[] seriesColors) {
         this.seriesColors = seriesColors;
     }
 
     /**
      * Get the CSS color definition of this series
      * @return CSS color String
      */
     public String getColor() {
         return color;
     }
 
     /**
      * Set the CSS color definition of this series.
      * @param color CSS color String
      */
     public void setColor(String color) {
         this.color = color;
     }
 
     /* Can't be a reference to Chart object explicitly due to cyclical dependency issues. */
     public JSONBuilder getDataJSON(UIComponent chart) {
         return JSONBuilder.create().beginArray();
     };
 
     public JSONBuilder getConfigJSON(UIComponent component) {
         JSONBuilder cfg = JSONBuilder.create();
 
         String label = getLabel();
         String[] seriesColors = getSeriesColors();
         String color = getColor();
 
         LineCap cap = getLineCap();
         LineJoin join = getLineJoin();
 
         Boolean show = getShow();
         Boolean shadow = getShadow();
         Boolean showMarker = getShowMarker();
         Boolean useNegativeColors = getUseNegativeColors();
         Boolean ftz = getFillToZero();
         Boolean fill = getFill();
 
         Integer width = getLineWidth();
         Integer xAxis = getXAxis();
         Integer yAxis = getYAxis();
         Integer shadowDepth = getShadowDepth();
         Integer shadowAlpha = getShadowAlpha();
         Integer shadowOffset = getShadowOffset();
         Integer shadowAngle = getShadowAngle();
         Integer fillAlpha = getFillAlpha();
 
         cfg.beginMap();
 
         if (show != null)
             cfg.entry("show", show);
 
         if (width != null)
             cfg.entry("lineWidth",lineWidth);
 
         if (cap != null)
             cfg.entry("lineCap", lineCap.toString());
 
         if (join != null)
             cfg.entry("lineJoin", lineJoin.toString());
 
         if (label != null)
             cfg.entry("label", label);
 
         if (xAxis != null)
             cfg.entry("xaxis", "x"+xAxis+"axis");
 
         if (yAxis != null)
             cfg.entry("yaxis", "y"+yAxis+"axis");
 
         if (useNegativeColors != null)
             cfg.entry("useNegativeColors", useNegativeColors);
 
         if (showMarker != null)
             cfg.entry("showMarker", showMarker);
 
         if (ftz != null)
             cfg.entry("fillToZero", ftz);
 
         if (fill != null)
             cfg.entry("fill", fill);
 
         if (shadow != null)
             cfg.entry("shadow", shadow);
 
         if (shadowAlpha != null) {
             Double alphaPercentile = shadowAlpha.doubleValue() / 100d;
             cfg.entry("shadowAlpha", alphaPercentile);
         }
 
         if (shadowDepth != null)
             cfg.entry("shadowDepth", shadowDepth);
 
         if (shadowAngle != null)
             cfg.entry("shadowAngle", shadowAngle);
 
         if (shadowOffset != null)
             cfg.entry("shadowOffset", shadowOffset);
 
         if (fillAlpha != null) {
             cfg.entry("fillAlpha",
                     fillAlpha.doubleValue() / 100d);
         }
 
         if (seriesColors != null) {
             cfg.beginArray("seriesColors");
             for (String c : seriesColors)
                 cfg.item(c);
             cfg.endArray();
         }
 
         if (color != null)
             cfg.entry("color", color);
 
         return cfg;
     }
 
     public abstract ChartType getDefaultType();
 
     public void clear() {
         data.clear();
     }
 }
