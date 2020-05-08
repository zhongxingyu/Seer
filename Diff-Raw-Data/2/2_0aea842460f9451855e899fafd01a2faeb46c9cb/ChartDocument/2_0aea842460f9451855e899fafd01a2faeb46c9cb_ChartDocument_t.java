 /*
  * The MIT License
  *
  * Copyright 2013 Gravidence.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package org.gravidence.gravifon.db.domain;
 
 import com.fasterxml.jackson.annotation.JsonProperty;
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import org.apache.commons.lang.ObjectUtils;
 
 /**
  * Chart document.<p>
  * Represents Chart database model.
  * 
  * @author Maksim Liauchuk <maksim_liauchuk@fastmail.fm>
  */
public class ChartDocument extends CouchDBDocument {
     
     /**
      * @see #getType()
      */
     @JsonProperty
     private ChartType type;
     
     /**
      * @see #getSize()
      */
     @JsonProperty
     private Integer size;
     
     /**
      * @see #getChartPeriodStartDatetime()
      */
     @JsonProperty("chart_period_start_datetime")
     private int[] chartPeriodStartDatetime;
     
     /**
      * @see #getChartPeriodEndDatetime()
      */
     @JsonProperty("chart_period_end_datetime")
     private int[] chartPeriodEndDatetime;
     
     /**
      * @see #getItems()
      */
     @JsonProperty
     private List<ChartItem> items;
 
     /**
      * Returns chart type.
      * 
      * @return chart type
      */
     public ChartType getType() {
         return type;
     }
 
     /**
      * @param type
      * @see #getType()
      */
     public void setType(ChartType type) {
         this.type = type;
     }
 
     /**
      * Returns chart size.
      * 
      * @return chart size
      */
     public Integer getSize() {
         return size;
     }
 
     /**
      * @param size
      * @see #getSize()
      */
     public void setSize(Integer size) {
         this.size = size;
     }
 
     /**
      * Returns date and time (UTC) when chart period starts.<p>
      * Array content is as follows: <code>[yyyy,MM,dd,HH,mm,ss,SSS]</code>.
      * 
      * @return date and time (UTC) when chart period starts
      */
     public int[] getChartPeriodStartDatetime() {
         return chartPeriodStartDatetime;
     }
 
     /**
      * @param chartPeriodStartDatetime
      * @see #getChartPeriodStartDatetime()
      */
     public void setChartPeriodStartDatetime(int[] chartPeriodStartDatetime) {
         this.chartPeriodStartDatetime = chartPeriodStartDatetime;
     }
 
     /**
      * Returns date and time (UTC) when chart period ends.<p>
      * Array content is as follows: <code>[yyyy,MM,dd,HH,mm,ss,SSS]</code>.
      * 
      * @return date and time (UTC) when chart period ends
      */
     public int[] getChartPeriodEndDatetime() {
         return chartPeriodEndDatetime;
     }
 
     /**
      * @param chartPeriodEndDatetime
      * @see #getChartPeriodEndDatetime()
      */
     public void setChartPeriodEndDatetime(int[] chartPeriodEndDatetime) {
         this.chartPeriodEndDatetime = chartPeriodEndDatetime;
     }
 
     /**
      * Returns chart items. Items list length is limited by {@link #getSize() size}.
      * 
      * @return chart items
      */
     public List<ChartItem> getItems() {
         return items;
     }
 
     /**
      * @param items
      * @see #getItems()
      */
     public void setItems(List<ChartItem> items) {
         this.items = items;
     }
 
     @Override
     public String toString() {
         return String.format("{id=%s, type=%s, size=%s, start=%s, end=%s}", getId(), type, size,
                 Arrays.toString(chartPeriodStartDatetime), Arrays.toString(chartPeriodEndDatetime));
     }
     
     /**
      * Adds an item to the document.<p>
      * {@link #getItems() Items} are always sorted by {@link ChartItem} comparator rules.
      * 
      * @param id chart item entity primary variation identifier
      * @param title chart item entity title
      * @param value chart item entity calculated value (amount or duration)
      */
     public void addItem(String id, String title, BigInteger value) {
         if (items == null) {
             items = new ArrayList<>(size + 1);
         }
         
         if (items.isEmpty()) {
             items.add(new ChartItem(id, title, value));
         }
         else if (ObjectUtils.compare(value, items.get(items.size() - 1).getValue()) > 0) {
             items.remove(items.size() - 1);
             
             items.add(new ChartItem(id, title, value));
             
             Collections.sort(items);
         }
     }
     
 }
