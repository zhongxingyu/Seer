 /*
  * Copyright 2006 Alistair Rutherford (http://code.google.com/p/gwtsimiletimeline/)
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package org.vaadin.chronographer.gwt.client.netthreads;
 
 import java.util.Date;
 import java.util.List;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.JavaScriptObject;
 
 /**
  * BandOptions
  * 
  * @author ajr
  */
 public class BandOptions extends JavaScriptObject {
     protected BandOptions() {
         super();
     }
 
     public static BandOptions create() {
         return BandOptionsImpl.create();
     }
 
     /**
      * how much of the timeline's space this band takes up, expressed as a
      * percent in a string, e.g., "30%"
      * 
      */
     public final String getWidth() {
         return JavaScriptObjectHelper.getAttribute(this, "width");
     }
 
     /**
      * required, how much of the timeline's space this band takes up, expressed
      * as a percent in a string, e.g., "30%"
      */
     public final void setWidth(String width) {
         JavaScriptObjectHelper.setAttribute(this, "width", width);
     }
 
     /**
      * a time unit from Timeline.DateTime, e.g., Timeline.DateTime.WEEK.
      * 
      */
     public final int getIntervalUnit() {
         return JavaScriptObjectHelper.getAttributeAsInt(this, "intervalUnit");
     }
 
     /**
      * required, a time unit from Timeline.DateTime, e.g.,
      * Timeline.DateTime.WEEK.
      */
     public final void setIntervalUnit(int value) {
         JavaScriptObjectHelper.setAttribute(this, "intervalUnit", value);
     }
 
     /**
      * the number of pixels that the time unit above is mapped to, e.g., 100.
      * 
      */
     public final int getIntervalPixels() {
         return JavaScriptObjectHelper.getAttributeAsInt(this, "intervalPixels");
     }
 
     /**
      * required, the number of pixels that the time unit above is mapped to,
      * e.g., 100.
      */
     public final void setIntervalPixels(int value) {
         JavaScriptObjectHelper.setAttribute(this, "intervalPixels", value);
     }
 
     /**
      * a boolean specifying whether event titles are to be painted. The default
      * is true.
      * 
      */
     public final boolean getShowEventText() {
         return JavaScriptObjectHelper.getAttributeAsBoolean(this,
                 "showEventText");
     }
 
     /**
      * optional, a boolean specifying whether event titles are to be painted.
      * The default is true.
      */
     public final void setShowEventText(boolean value) {
         JavaScriptObjectHelper.setAttribute(this, "showEventText", value);
     }
 
     /**
      * the number of em (dependent on the current font) to be left between
      * adjacent tracks on which events are painted. The default value is
      * retrieved from the provided or default theme. E.g., 0.5.
      * 
      */
     public final float getTrackGap() {
         return JavaScriptObjectHelper.getAttributeAsFloat(this, "trackGap");
     }
 
     /**
      * optional, the number of em (dependent on the current font) to be left
      * between adjacent tracks on which events are painted. The default value is
      * retrieved from the provided or default theme. E.g., 0.5.
      */
     public final void setTrackGap(float value) {
         JavaScriptObjectHelper.setAttribute(this, "trackGap", value);
     }
 
     /**
      * the height of each track in em (dependent on the current font). The
      * default value is retrieved from the provided or default theme. E.g., 1.5.
      * 
      */
     public final float getTrackHeight() {
         return JavaScriptObjectHelper.getAttributeAsFloat(this, "trackHeight");
     }
 
     /**
      * optional, the height of each track in em (dependent on the current font).
      * The default value is retrieved from the provided or default theme. E.g.,
      * 1.5.
      */
     public final void setTrackHeight(float value) {
         JavaScriptObjectHelper.setAttribute(this, "trackHeight", value);
     }
 
     /**
      * required, Band theme
      */
     public final void setTheme(Theme value) {
         JavaScriptObjectHelper.setAttribute(this, "theme", value);
     }
 
     /**
      * Band theme
      */
     public final JavaScriptObject getTheme() {
         return JavaScriptObjectHelper.getAttributeAsJavaScriptObject(this,
                 "theme");
     }
 
     /**
      * required, Event source
      */
     public final void setEventSource(EventSource value) {
         JavaScriptObjectHelper.setAttribute(this, "eventSource", value);
     }
 
     /**
      * Event source
      */
     public final JavaScriptObject getEventSource() {
         return JavaScriptObjectHelper.getAttributeAsJavaScriptObject(this,
                 "eventSource");
     }
 
     /**
      * optional, set date
      */
     public final void setDate(Date value) {
         JavaScriptObjectHelper.setAttribute(this, "date", value.toString());
     }
 
     /**
      * optional, set time zone
      */
     public final void setTimeZone(int value) {
         JavaScriptObjectHelper.setAttribute(this, "timeZone", value);
     }
 
     /**
      * optional, set hot zones
      */
     public final void setZones(List zones) {
         JavaScriptObject[] zonesArr = JavaScriptObjectHelper.listToArray(zones);
 
         JavaScriptObject jarr = JavaScriptObjectHelper.arrayConvert(zonesArr);
 
         GWT.log(jarr.toString());
 
         JavaScriptObjectHelper.setAttribute(this, "zones", jarr);
     }

 }
