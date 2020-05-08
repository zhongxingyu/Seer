 package com.od.jtimeseries.ui.selector.table;
 
 import com.od.jtimeseries.ui.timeseries.RemoteChartingTimeSeries;
 import com.od.jtimeseries.context.ContextProperties;
 
 import java.util.*;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Nick Ebbutt
  * Date: 24-Feb-2010
  * Time: 17:30:29
  */
 public class SummaryStatsTableModel extends DynamicColumnsTableModel<RemoteChartingTimeSeries> {
 
     private TreeMap<String,String> statsProperties = new TreeMap<String,String>();
     private List<String> statNamesList = new ArrayList<String>();
     private List<String> propertyNamesList = new ArrayList<String>();
 
 
     public SummaryStatsTableModel(BeanPerRowModel<RemoteChartingTimeSeries> wrappedModel) {
         super(wrappedModel);
         initialize();
     }
 
     protected boolean requiresStructureChange(int firstRow, int lastRow) {
         boolean result = false;
         int oldSize = statsProperties.size();
 
         addSummaryColumns(firstRow, lastRow);
         if (statsProperties.size() != oldSize) {
             recreateColumnLists();
             result = true;
         }
         return result;
     }
 
     private void recreateColumnLists() {
         statNamesList.clear();
         propertyNamesList.clear();
         for ( Map.Entry<String,String> e : statsProperties.entrySet()) {
             statNamesList.add(e.getValue());
             propertyNamesList.add(e.getKey());
         }
     }
 
     private void addSummaryColumns(int firstRow, int lastRow) {
         for ( int row = firstRow; row <= lastRow; row++) {
             RemoteChartingTimeSeries s = getObject(row);
             for ( Object prop : s.getProperties().keySet()) {
                 String propertyName = (String) prop;
                 if (! statsProperties.containsKey(propertyName)) {
                     if ( ContextProperties.isSummaryStatsProperty(propertyName) &&
                         ContextProperties.getSummaryStatsDataType(propertyName) == ContextProperties.SummaryStatsDataType.DOUBLE) {
                         addSummaryProperty(propertyName);
                     }
                 }
             }
         }
     }
 
     private void addSummaryProperty(String propertyName) {
         String statisticName = ContextProperties.parseStatisticName(propertyName);
         statsProperties.put(propertyName, statisticName);
         Collections.sort(propertyNamesList);
     }
 
     protected Object getValueForDynamicColumn(int rowIndex, int extraColsIndex) {
         String propertyName = propertyNamesList.get(extraColsIndex);
        return Double.valueOf(getObject(rowIndex).getProperty(propertyName));
     }
 
     public int getDynamicColumnCount() {
         return statsProperties.size();
     }
 
     protected String getDynamicColumnName(int extraColsIndex) {
         return statNamesList.get(extraColsIndex);
     }
 
     protected Class<?> getDynamicColumnClass(int extraColsIndex) {
         return Double.class;
     }
 }
