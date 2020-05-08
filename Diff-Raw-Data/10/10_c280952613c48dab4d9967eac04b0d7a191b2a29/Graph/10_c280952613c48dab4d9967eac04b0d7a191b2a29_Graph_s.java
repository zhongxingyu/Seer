 /*
  * Copyright 2011 Damien Bourdette
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.otto.graph;
 
 import com.google.common.base.Objects;
 import org.apache.commons.lang.StringEscapeUtils;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 
 import java.util.*;
 
 /**
  * Class containing graph data.
  * It is able to build csv exports or js inserts for web pages.
  *
  * @author damien bourdette <a href="https://github.com/dbourdette">dbourdette on github</a>
  * @version \$Revision$
  */
 public class Graph {
 
     private class CellKey {
 
         private final GraphRow row;
 
         private final GraphColumn column;
 
         public CellKey(GraphRow row, GraphColumn column) {
             super();
             this.row = row;
             this.column = column;
         }
 
         @Override
         public int hashCode() {
             final int prime = 31;
             int result = 1;
             result = prime * result + getOuterType().hashCode();
             result = prime * result + ((column == null) ? 0 : column.hashCode());
             result = prime * result + ((row == null) ? 0 : row.hashCode());
             return result;
         }
 
         @Override
         public boolean equals(Object obj) {
             if (this == obj)
                 return true;
             if (obj == null)
                 return false;
             if (getClass() != obj.getClass())
                 return false;
             CellKey other = (CellKey) obj;
             if (!getOuterType().equals(other.getOuterType()))
                 return false;
             if (column == null) {
                 if (other.column != null)
                     return false;
             } else if (!column.equals(other.column))
                 return false;
             if (row == null) {
                 if (other.row != null)
                     return false;
             } else if (!row.equals(other.row))
                 return false;
             return true;
         }
 
         private Graph getOuterType() {
             return Graph.this;
         }
     }
 
     private class ColumnSum implements Comparable<ColumnSum> {
 
         private int sum;
 
         private final GraphColumn column;
 
         public ColumnSum(GraphColumn column) {
             this.column = column;
         }
 
         public void add(Integer value) {
             if (value == null) {
                 return;
             }
 
             sum += value;
         }
 
         public GraphColumn getColumn() {
             return column;
         }
 
         @Override
         public int compareTo(ColumnSum o) {
             return sum - o.sum;
         }
 
         @Override
         public String toString() {
             return "ColumnSum [column=" + column + ", sum=" + sum + "]";
         }
     }
 
     private static final int DEFAULT_WIDTH = 1200;
 
     private static final int DEFAULT_HEIGHT = 500;
 
     private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
     
     private final Duration FIVE_MINUTES = Duration.standardMinutes(5);
 
     private final List<GraphRow> rows = new ArrayList<GraphRow>();
 
     private final List<GraphColumn> columns = new ArrayList<GraphColumn>();
 
     private final Map<CellKey, Integer> cells = new HashMap<CellKey, Integer>();
 
     private Integer defaultValue = 0;
 
     public Graph() {
     }
 
     public Graph rows(Interval interval) {
         return rows(interval, FIVE_MINUTES);
     }
 
     public Graph rows(Interval interval, Duration step) {
         setRows(interval, step);
 
         return this;
     }
 
     public void dropColumn(String title) {
         dropColumn(getColumn(title));
     }
 
     public void ensureColumnExists(String title) {
         if (!hasColumn(title)) {
             columns.add(new GraphColumn(title));
         }
     }
 
     public void ensureColumnsExists(String... titles) {
         for (String title : titles) {
             ensureColumnExists(title);
         }
     }
 
     public boolean hasColumn(String title) {
         return safeGetColumn(title) != null;
     }
 
     public int getColumnCount() {
         return columns.size();
     }
 
     public void setRows(Interval interval) {
     	setRows(interval, FIVE_MINUTES);
     }
     
     public void setRows(Interval interval, Duration step) {
     	rows.clear();
     	
         DateTime current = interval.getStart();
 
         while (current.isBefore(interval.getEnd())) {
             rows.add(new GraphRow(new Interval(current, step)));
             
             current = current.plus(step);
         }
     }
 
     public int getRowCount() {
         return rows.size();
     }
 
     public DateTime getRowStartDate(int index) {
         return rows.get(index).getStartDate();
     }
 
     public DateTime getStartDate() {
         if (rows.size() == 0) {
             return null;
         }
 
         return rows.get(0).getStartDate();
     }
 
     public DateTime getEndDate() {
         if (rows.size() == 0) {
             return null;
         }
 
         return rows.get(rows.size() - 1).getEndDate();
     }
 
     public void setValue(String columnTitle, DateTime date, Integer value) {
         GraphRow row = getRow(date);
         GraphColumn column = getColumn(columnTitle);
 
         setValue(row, column, value);
     }
 
     public void increaseValue(String columnTitle, DateTime date) {
         increaseValue(columnTitle, date, 1);
     }
 
     public void increaseValue(String columnTitle, DateTime date, Integer value) {
         if (value == null) {
             return;
         }
 
         GraphRow row = getRow(date);
         GraphColumn column = getColumn(columnTitle);
 
         Integer cellValue = cells.get(new CellKey(row, column));
 
         if (cellValue == null) {
             cellValue = 0;
         }
 
         cells.put(new CellKey(row, column), cellValue + value);
     }
 
     public Integer getValue(String columnTitle, int rowIndex) {
         GraphRow row = rows.get(rowIndex);
         GraphColumn column = getColumn(columnTitle);
 
         return getValue(row, column);
     }
 
     public Integer getDefaultValue() {
         return defaultValue;
     }
 
     public void setDefaultValue(Integer defaultValue) {
         this.defaultValue = defaultValue;
     }
 
     public void cumulate(String... columnTitles) {
         for (String title : columnTitles) {
             cumulate(title);
         }
     }
 
     /**
      * Apply a cumulative sum to given columns
      *
      * @param columnTitle column on which sum is operated
      */
     public void cumulate(String columnTitle) {
         GraphColumn column = getColumn(columnTitle);
 
         Integer sum = 0;
 
         for (GraphRow row : rows) {
             Integer value = getValue(row, column);
 
             if (value != null) {
                 sum += value;
             }
 
             setValue(row, column, sum);
         }
     }
 
     /**
      * Keeps only count top columns from given columns.
      */
     public void top(int count, String... columnTitles) {
         if (count >= columnTitles.length) {
             return;
         }
 
         List<ColumnSum> sums = new ArrayList<Graph.ColumnSum>();
 
         for (GraphColumn column : getColumns(columnTitles)) {
             ColumnSum sum = new ColumnSum(column);
 
             for (GraphRow row : rows) {
                 sum.add(getValue(row, column));
             }
 
             sums.add(sum);
         }
 
         Collections.sort(sums);
 
         List<ColumnSum> columnsToDrop = sums.subList(0, sums.size() - count);
 
         for (ColumnSum sum : columnsToDrop) {
             dropColumn(sum.getColumn());
         }
     }
 
     public String toCsv() {
         StringBuilder builder = new StringBuilder();
 
         builder.append("startDate,endDate");
 
         for (GraphColumn column : columns) {
             builder.append(",");
             builder.append(column.getTitle());
         }
 
         builder.append("\n");
 
         for (GraphRow row : rows) {
             builder.append(DATE_TIME_FORMATTER.print(row.getStartDate()));
             builder.append(",");
             builder.append(DATE_TIME_FORMATTER.print(row.getEndDate()));
 
             for (GraphColumn column : columns) {
                 builder.append(",");
                 builder.append(getValue(row, column));
             }
 
             builder.append("\n");
         }
 
         return builder.toString();
     }
 
     /**
      * Produces google chart js code.
      */
     public String toGoogleJs(String elementId, Integer width, Integer height) {
         StringBuilder builder = new StringBuilder();
 
         builder.append("var data = new google.visualization.DataTable();\n");
         builder.append("data.addColumn('date', 'Date');\n");
 
         for (GraphColumn column : columns) {
             builder.append("data.addColumn('number', '" + StringEscapeUtils.escapeJavaScript(column.getTitle())
                            + "');\n");
         }
         
         builder.append("data.addRows(" + rows.size() + ");\n");
 
         int rowIndex = 0;
         int columnIndex = 0;
 
         for (GraphRow row : rows) {
             columnIndex = 0;
 
             builder.append("data.setValue(" + rowIndex + ", " + columnIndex + ", new Date("
                           + row.getStartDate().getMillis() + "));\n");
 
             for (GraphColumn column : columns) {
                 columnIndex++;
 
                 builder.append("data.setValue(" + rowIndex + ", " + columnIndex + ", " + getValue(row, column) + ");\n");
             }
 
             rowIndex++;
         }
 
         builder.append("var formatter = new google.visualization.DateFormat({pattern: '" + getDatePattern() + "'});\n");
         builder.append("formatter.format(data, 0);\n");
 
         builder.append("var chart = new google.visualization.LineChart(document.getElementById('" + elementId
                        + "'));\n");
         builder.append("chart.draw(data, {width: " + (width == null ? DEFAULT_WIDTH : width) + ", height: "
                        + (height == null ? DEFAULT_HEIGHT : height) + "});");
 
         return builder.toString();
     }
 
     public String toGoogleHtml(Integer width, Integer height) {
         String elementId = "chart_div_" + UUID.randomUUID().toString();
 
         StringBuilder builder = new StringBuilder();
 
         builder.append("<div id=\"" + elementId + "\"></div>");
 
         builder.append("<script type=\"text/javascript\" src=\"https://www.google.com/jsapi\"></script>");
         builder.append("<script type=\"text/javascript\">");
         builder.append("google.load(\"visualization\", \"1\", {packages:[\"corechart\"]});");
         builder.append("google.setOnLoadCallback(drawChart);");
         builder.append("function drawChart() {");
 
         builder.append(toGoogleJs(elementId, width, height));
 
         builder.append("}");
         builder.append("</script>");
 
         return builder.toString();
     }
     
     public String toHtmlTable() {
     	StringBuilder builder = new StringBuilder();
     	
     	builder.append("<table>");
     	
     	builder.append("<tr>");
     	
     	builder.append("<td>");
     	builder.append("date");
     	builder.append("</td>"); 
     	
     	for (GraphColumn column : columns) {
     		builder.append("<td>");
         	builder.append(column.getTitle());
         	builder.append("</td>");   
         }
     	
     	builder.append("</tr>");
     	
     	for (GraphRow row : rows) {
     		builder.append("<tr>");
     		
     		builder.append("<td>");
         	builder.append(row.getStartDate());
         	builder.append("</td>");
     		
             for (GraphColumn column : columns) {
             	builder.append("<td>");
             	builder.append(getValue(row, column));
             	builder.append("</td>");                
             }
 
             builder.append("</tr>");
         }
     	
     	builder.append("</table>");
     	
     	return builder.toString();
     }
 
     public String getDatePattern() {
         if (rows.size() == 0) {
             return "dd MMM yyyy";
         }
 
         Duration duration = new Duration(rows.get(0).getStartDate(), rows.get(0).getEndDate());
 
         if (duration.isShorterThan(Duration.standardDays(1))) {
             return "dd MM yyyy HH:mm";
         } else {
             return "dd MMM yyyy";
         }
     }
 
     private GraphRow getRow(DateTime date) {
         for (GraphRow row : rows) {
             if (row.contains(date)) {
                 return row;
             }
         }
 
         throw new IllegalArgumentException("No row found for date " + date);
     }
 
     private GraphColumn getColumn(String title) {
         GraphColumn column = safeGetColumn(title);
 
         if (column == null) {
             throw new IllegalArgumentException("No column found for title " + title);
         }
 
         return column;
     }
 
     private List<GraphColumn> getColumns(String... titles) {
         List<GraphColumn> columns = new ArrayList<GraphColumn>();
 
         for (String title : titles) {
             columns.add(getColumn(title));
         }
 
         return columns;
     }
 
     private GraphColumn safeGetColumn(String title) {
         for (GraphColumn column : columns) {
             if (Objects.equal(column.getTitle(), title)) {
                 return column;
             }
         }
 
         return null;
     }
 
     private void setValue(GraphRow row, GraphColumn column, Integer value) {
         cells.put(new CellKey(row, column), value);
     }
 
     private Integer getValue(GraphRow row, GraphColumn column) {
         Integer value = cells.get(new CellKey(row, column));
 
         return value == null ? defaultValue : value;
     }
 
     private void dropColumn(GraphColumn column) {
         columns.remove(column);
 
         for (GraphRow row : rows) {
             cells.remove(new CellKey(row, column));
         }
     }
 }
