 /*
  * Copyright 2006 Pentaho Corporation.  All rights reserved. 
  * This software was developed by Pentaho Corporation and is provided under the terms 
  * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
  * this file except in compliance with the license. If you need a copy of the license, 
  * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
  * BI Platform.  The Initial Developer is Pentaho Corporation.
  *
  * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
  * the license for the specific language governing your rights and limitations.
  *
  * Created Nov 10, 2005 
  * @author wseyler
  */
 
 package org.pentaho.commons.connection;
 
 import java.text.Format;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.apache.commons.collections.map.ListOrderedMap;
 import org.pentaho.commons.connection.memory.MemoryMetaData;
 import org.pentaho.commons.connection.memory.MemoryResultSet;
 
 /**
  * @author wseyler
  * 
  * Provides various methods to transmutes an IPentahoResultSet such that the
  * resulting IPentahoResultSet dimensionality can be used for different purposes
  * (ie creating a pie or bar chart)
  * 
  */
 public class PentahoDataTransmuter extends DataUtilities {
   protected final IPentahoResultSet sourceResultSet;
 
   public PentahoDataTransmuter(IPentahoResultSet resultSet) {
     super();
     sourceResultSet = resultSet;
   }
 
   /**
    * @return Returns the sourceResultSet.
    */
   public IPentahoResultSet getSourceResultSet() {
     return sourceResultSet;
   }
 
   public static IPentahoResultSet transmute(IPentahoResultSet source, boolean pivot) {
     return transmute(source, (Integer) null, null, null, null, pivot);
   }
 
   public static IPentahoResultSet transmute(IPentahoResultSet source, Integer columnForRowHeaders,
       Integer rowForColumnHeaders, boolean pivot) {
     return transmute(source, columnForRowHeaders, rowForColumnHeaders, null, null, pivot);
   }
 
   public IPentahoResultSet transmute(Integer columnForRowHeaders, Integer rowForColumnHeaders, boolean pivot) {
     return transmute(sourceResultSet, columnForRowHeaders, rowForColumnHeaders, null, null, pivot);
   }
 
   public static IPentahoResultSet transmute(IPentahoResultSet source, String[] columnForRowHeaders,
       String[] rowForColumnHeaders, boolean pivot) {
     return transmute(source, columnForRowHeaders, rowForColumnHeaders, null, null, pivot);
   }
 
   public IPentahoResultSet transmute(String[] columnForRowHeaders, String[] rowForColumnHeaders, boolean pivot) {
     return transmute(sourceResultSet, columnForRowHeaders, rowForColumnHeaders, null, null, pivot);
   }
 
   public static IPentahoResultSet transmute(IPentahoResultSet source, Integer[] rowsToInclude,
       Integer[] columnsToInclude, boolean pivot) {
     return transmute(source, null, null, rowsToInclude, columnsToInclude, pivot);
   }
 
   public IPentahoResultSet transmute(Integer[] rowsToInclude, Integer[] columnsToInclude, boolean pivot) {
     return transmute(sourceResultSet, null, null, rowsToInclude, columnsToInclude, pivot);
   }
 
   public static IPentahoResultSet transmute(IPentahoResultSet source, String[][] rowsToInclude,
       String[][] columnsToInclude, boolean pivot) {
     return transmute(source, null, null, rowsToInclude, columnsToInclude, pivot);
   }
 
   public IPentahoResultSet transmute(String[][] rowsToInclude, String[][] columnsToInclude, boolean pivot) {
     return transmute(sourceResultSet, null, null, rowsToInclude, columnsToInclude, pivot);
   }
 
   public IPentahoResultSet transmute(String[] columnForRowHeaders, String[] rowForColumnHeaders,
       String[][] rowsToInclude, String[][] columnsToInclude, boolean pivot) {
     return transmute(sourceResultSet, columnForRowHeaders, rowForColumnHeaders, rowsToInclude, columnsToInclude, pivot);
   }
 
   /**
    * Returns a memory result set that represents a grid of data and it's
    * associated headers.
    * 
    * @param source -
    *            The source IPentahoResultSet
    * @param columnForRowHeaders -
    *            a 0 based column number whose data will be used as the row
    *            headers. If null and rowHeaders exist in the source then the
    *            rowHeaders will be returned otherwise the first column (column
    *            0) will be used for row headers
    * @param rowForColumnHeaders -
    *            a 0 based row number whose data will be used as the
    *            columnHeaders. If null and columnHeaders exist in the source
    *            then the columnHeaders will be returned. Otherwise the first
    *            row (row 0) will be used for the columnHeaders
    * @param columnsToInclude -
    *            An 2D String array where each row is a String[] representing a
    *            column header[] of a column to include in the result.
    * @param rowsToInclude -
    *            An 2D String array where each row is a String[] representing a
    *            row header[] of a row to include in the result.
    * @param pivot -
    *            pivot the entire IPentahoResultSet - NOTE this occurs last in
    *            the process. Other params referencing rows and colums are
    *            based on the source before it is pivoted.
    * @return - a copy of the result set containing the requested data.
    */
   public static IPentahoResultSet transmute(IPentahoResultSet source, String[] columnForRowHeaders,
       String[] rowForColumnHeaders, String[][] rowsToInclude, String[][] columnsToInclude, boolean pivot) {
     Integer cfrh = null;
     if (columnForRowHeaders != null) {
       String[][] searchStrings = new String[1][];
       searchStrings[0] = columnForRowHeaders;
       Integer[] indexes = columnNamesToIndexes(source, searchStrings);
       if (indexes.length > 0) {
         cfrh = indexes[0];
       }
     }
     Integer rfch = null;
     if (rowForColumnHeaders != null) {
       String[][] searchStrings = new String[1][];
       searchStrings[0] = rowForColumnHeaders;
       Integer[] indexes = rowNamesToIndexes(source, searchStrings);
       if (indexes.length > 0) {
         rfch = indexes[0];
       }
     }
     Integer[] rti = null;
     if (rowsToInclude != null) {
       rti = rowNamesToIndexes(source, rowsToInclude);
     }
     Integer[] cti = null;
     if (columnsToInclude != null) {
       cti = columnNamesToIndexes(source, columnsToInclude);
     }
     return transmute(source, cfrh, rfch, rti, cti, pivot);
   }
 
   public IPentahoResultSet transmute(Integer columnForRowHeaders, Integer rowForColumnHeaders, Integer[] rowsToInclude,
       Integer[] columnsToInclude, boolean pivot) {
     return transmute(sourceResultSet, columnForRowHeaders, rowForColumnHeaders, rowsToInclude, columnsToInclude, pivot);
   }
 
   /**
    * Returns a memory result set that represents a grid of data and it's
    * associated headers.
    * 
    * @param source -
    *            The source IPentahoResultSet
    * @param columnForRowHeaders -
    *            a 0 based column number whose data will be used as the row
    *            headers. If null and rowHeaders exist in the source then the
    *            rowHeaders will be returned otherwise the first column (column
    *            0) will be used for row headers
    * @param rowForColumnHeaders -
    *            a 0 based row number whose data will be used as the
    *            columnHeaders. If null and columnHeaders exist in the source
    *            then the columnHeaders will be returned. Otherwise the first
    *            row (row 0) will be used for the columnHeaders
    * @param columnsToInclude -
    *            An integer array of columns to include in the result.
    * @param rowToInclude -
    *            An integer array of rows to include in the result
    * @param pivot -
    *            pivot the entire IPentahoResultSet - NOTE this occurs last in
    *            the process. Other params referencing rows and colums are
    *            based on the source before it is pivoted.
    * @return - a copy of the result set containing the requested data.
    */
   public static IPentahoResultSet transmute(IPentahoResultSet source, Integer columnForRowHeaders,
       Integer rowForColumnHeaders, Integer[] rowsToInclude, Integer[] columnsToInclude, boolean pivot) {
     Object[][] rowHeaders = null;
     Object[][] columnHeaders = null;
 
     // construct the headers
     rowHeaders = constructRowHeaders(source, columnForRowHeaders);
     columnHeaders = constructColumnHeaders(source, rowForColumnHeaders);
     // select the headers
     rowHeaders = DataUtilities.filterDataByRows(rowHeaders, rowsToInclude);
     columnHeaders = DataUtilities.filterDataByColumns(columnHeaders, columnsToInclude);
     // create a 2D array of the data
     source.beforeFirst(); // make sure we start at the beginning
     List<Object[]> dataList = new ArrayList<Object[]>();
     Object[] rowData = source.next();
     while (rowData != null) {
       dataList.add(rowData);
       rowData = source.next();
     }
     Object[][] data = null;
     if (columnHeaders != null) {
       data = new Object[dataList.size()][columnHeaders[0].length];
       for (int row = 0; row < data.length; row++) {
         data[row] = dataList.get(row);
       }
     }
     // now filter the data
     data = DataUtilities.filterData(data, rowsToInclude, columnsToInclude);
     // create the MemoryResultSet with the processed headers
     IPentahoResultSet result = new MemoryResultSet(new MemoryMetaData(columnHeaders, rowHeaders)); // Create
     // the
     // resultSet
     // now copy the data to the MemoryResultSet
     if (data != null) {
       for (int row = 0; row < data.length; row++) {
         ((MemoryResultSet) result).addRow(data[row]);
       }
     }
     // finally pivot this if requested
     if (pivot) {
       result = pivot(result);
     }
     return result;
   }
 
   public IPentahoResultSet pivot() {
     return pivot(sourceResultSet);
   }
 
   /**
    * This rotates a IPentahoResultSet such that the row and column data are
    * reversed and the data headers are also rotated. ie source.
    * 
    * |Cheader1,0|CHeader1,1|CHeader1,2|Cheader1,3|
    * |CHeader0,0|CHeader0,1|CHeader0,2|CHeader0,3|
    * ---------------------------------------------
    * |RHeader0,2|RHeader0,1|RHeader0,0 |Data0,0 |Data0,1 |Data0,2 |Data 0,3 |
    * |RHeader1,2|RHeader1,1|RHeader1,0 |Data1,0 |Data1,1 |Data1,2 |Data 1,3 |
    * |RHeader2,2|RHeader2,1|RHeader2,0 |Data2,0 |Data2,1 |Data2,2 |Data 2,3 |
    * |RHeader3,2|RHeader3,1|RHeader3,0 |Data3,0 |Data3,1 |Data3,2 |Data 3,3 |
    * |RHeader4,2|RHeader4,1|RHeader4,0 |Data4,0 |Data4,1 |Data4,2 |Data 4,3 |
    * |RHeader5,2|RHeader5,1|RHeader5,0 |Data5,0 |Data5,1 |Data5,2 |Data 5,3 |
    * 
    * becomes:
    * 
    * |RHeader0,2|RHeader1,2|RHeader2,2|RHeader3,2|RHeader4,2|RHeader5,2|
    * |RHeader0,1|RHeader1,1|RHeader2,1|RHeader3,1|RHeader4,1|RHeader5,1|
    * |RHeader0,0|RHeader1,0|RHeader2,0|RHeader3,0|RHeader4,0|RHeader5,0|
    * -------------------------------------------------------------------
    * |CHeader1,0|CHeader0,0 |Data0,0 |Data1,0 |Data2,0 |Data3,0 |Data4,0
    * |Data5,0 | |CHeader1,1|CHeader0,1 |Data0,1 |Data1,1 |Data2,1 |Data3,1
    * |Data4,1 |Data5,1 | |CHeader1,2|CHeader0,2 |Data0,2 |Data1,2 |Data2,2
    * |Data3,2 |Data4,2 |Data5,2 | |CHeader1,3|CHeader0,3 |Data0,3 |Data1,3
    * |Data2,3 |Data3,3 |Data4,3 |Data5,3 |
    * 
    * @param resultSet
    *            and IPentahoResultSet on which to operate. Note that this
    *            parameter remains unchaged after this call. The original data
    *            is preserved
    * 
    * @return an IPentahoResultSet that represents the rotated
    *         IPentahoResultSet argumnent
    */
   public static IPentahoResultSet pivot(IPentahoResultSet resultSet) {
     MemoryResultSet result = new MemoryResultSet(swapAndPivotRowAndColumnHeaders(resultSet.getMetaData()));
 
     // Make a copy of the current data
     Object[][] dataValues = new Object[resultSet.getRowCount()][resultSet.getColumnCount()];
     for (int row = 0; row < resultSet.getRowCount(); row++) {
       Object[] dataRow = resultSet.next();
       for (int column = 0; column < resultSet.getColumnCount(); column++) {
         dataValues[row][column] = dataRow[column];
       }
     }
     // Swap the dimensionality of the data
     dataValues = DataUtilities.pivotDimensions(dataValues);
     for (int row = 0; row < dataValues.length; row++) {
       result.addRow(dataValues[row]);
     }
     return result;
   }
 
   public Integer[] rowNamesToIndexes(String[][] names) {
     return rowNamesToIndexes(sourceResultSet, names);
   }
 
   /**
    * Returns an array of row Indexes taht match the names parameter
    * 
    * @param source
    *            IPentahoResultSet to operate agains
    * @param names
    *            a 2D string array[rows][columns] where each row is a string
    *            array to compare against the row headers
    * @return an array of Integers that represes the selected rows
    */
   public static Integer[] rowNamesToIndexes(IPentahoResultSet source, String[][] names) {
     List<Integer> result = new ArrayList<Integer>();
     for (int row = 0; row < names.length; row++) {
       int found = source.getMetaData().getRowIndex(names[row]);
       if (found != -1) {
         result.add(new Integer(found));
       }
     }
     Integer[] resultArray = new Integer[result.size()];
     for (int i = 0; i < resultArray.length; i++) {
       resultArray[i] = result.get(i);
     }
     return resultArray;
   }
 
   public Integer[] columnNamesToIndexes(String[][] names) {
     return columnNamesToIndexes(sourceResultSet, names);
   }
 
   /**
    * Returns an array of column Indexes that match the names parameter
    * 
    * @param source
    *            IPentahoResultSet to operate against
    * @param names
    *            a 2D string array[rows][columns] where each row is a string
    *            array to compare against the column headers
    * @return an array of Integers that represents the selected columns
    */
   public static Integer[] columnNamesToIndexes(IPentahoResultSet source, String[][] names) {
     List<Integer> result = new ArrayList<Integer>();
     for (int row = 0; row < names.length; row++) {
       int found = source.getMetaData().getColumnIndex(names[row]);
       if (found != -1) {
         result.add(new Integer(found));
       }
     }
     Integer[] resultArray = new Integer[result.size()];
     for (int i = 0; i < resultArray.length; i++) {
       resultArray[i] = result.get(i);
     }
     return resultArray;
   }
 
   /**
    * @param source
    * @param rowForColumnHeaders
    * @return
    */
   protected static Object[][] constructColumnHeaders(IPentahoResultSet source, Integer rowForColumnHeaders) {
     Object[][] result = null;
     if (rowForColumnHeaders == null) { // Nothing specified by user so
       // we'll apply the rules
       if (source.getMetaData().getColumnHeaders() == null) { // If we
         // don't
         // have
         // columnHeaders
         Object[] dataRow = source.getDataRow(0); // use the first row
         if (dataRow != null) {
           result = new Object[1][];
           result[0] = dataRow;
         }
       } else { // We have Headers so let's just use them
         result = source.getMetaData().getColumnHeaders();
       }
     } else { // A rowForColumnHeaders was supplied so lets use it
       Object[] dataRow = source.getDataRow(rowForColumnHeaders.intValue());
       if (dataRow != null) {
         result = new Object[1][];
         result[0] = dataRow;
       }
     }
     return result;
   }
 
   /**
    * Constructs the headers based on the rule as stated in the
    * columnForRowHeaders param.
    * 
    * @param source -
    *            The source IPentahoResultSet
    * @param columnForRowHeaders -
    *            a 0 based column number whose data will be used as the row
    *            headers. If null and rowHeaders exist in the source then the
    *            rowHeaders will be flattened and returned otherwise the first
    *            column (column 0) will be used for row headers
    * @return 2D array representing the manufactured rowHeaders
    */
  @SuppressWarnings("deprecation")
   protected static Object[][] constructRowHeaders(IPentahoResultSet source, Integer columnForRowHeaders) {
     Object[][] result = null;
     if (columnForRowHeaders == null) { // Nothing specified by user so
       // we'll apply the rules
       if (source.getMetaData().getRowHeaders() == null) { // If we don't
         // have
         // rowHeaders
         Object[] dataColumn = source.getDataColumn(0); // use the first
         // column
         result = new Object[dataColumn.length][1];
         for (int row = 0; row < result.length; row++) {
           result[row][0] = dataColumn[row];
         }
       } else { // We have Headers so let's just use them
         result = source.getMetaData().getRowHeaders();
       }
     } else { // A columnForRowHeaders was supplied so lets use it
       Object[] dataColumn = source.getDataColumn(columnForRowHeaders.intValue()); // use
       // the
       // param
       // column
       result = new Object[dataColumn.length][1];
       for (int row = 0; row < result.length; row++) {
         result[row][0] = dataColumn[row];
       }
     }
     return result;
   }
 
   /**
    * @param metaData
    *            source
    * @return IPentahoMetaData that contain the swapped and pivoted Row and
    *         ColumnHeaders
    */
   protected static IPentahoMetaData swapAndPivotRowAndColumnHeaders(IPentahoMetaData metaData) {
     Object[][] sourceColumnHeaders = metaData.getColumnHeaders();
    @SuppressWarnings("deprecation")
     Object[][] sourceRowHeaders = metaData.getRowHeaders();
     boolean hasColumnHeaders = sourceColumnHeaders != null;
     boolean hasRowHeaders = sourceRowHeaders != null;
 
     Object[][] columnHeaders = null;
     Object[][] rowHeaders = null;
 
     if (hasColumnHeaders) {
       rowHeaders = DataUtilities.pivotDimensions(sourceColumnHeaders);
     }
 
     if (hasRowHeaders) {
       columnHeaders = DataUtilities.pivotDimensions(sourceRowHeaders);
     }
 
     MemoryMetaData result = new MemoryMetaData(columnHeaders, rowHeaders);
     return result;
   }
 
   private static String[] getCollapsedRowHeaders(IPentahoResultSet resultSet, char seperator) {
    @SuppressWarnings("deprecation")
     Object[][] rowHeaders = resultSet.getMetaData().getRowHeaders();
     if (rowHeaders != null) {
       StringBuffer[] resultBuffer = new StringBuffer[rowHeaders.length];
       for (int i = 0; i < resultBuffer.length; i++) {
         resultBuffer[i] = new StringBuffer();
       }
       for (int row = 0; row < rowHeaders.length; row++) {
         for (int col = 0; col < rowHeaders[row].length; col++) {
           if (col == 0) { // Don't use seperator
             resultBuffer[row].append(rowHeaders[row][col].toString());
           } else { // use seperator
             resultBuffer[row].append(seperator + rowHeaders[row][col].toString());
           }
         }
       }
       String[] result = new String[resultBuffer.length];
       for (int i = 0; i < resultBuffer.length; i++) {
         result[i] = resultBuffer[i].toString();
       }
       return result;
     }
     return null;
   }
 
   private static String[] getCollapsedColumnHeaders(IPentahoResultSet resultSet, char seperator) {
     Object[][] columnHeaders = resultSet.getMetaData().getColumnHeaders();
     if (columnHeaders != null) {
       StringBuffer[] resultBuffer = new StringBuffer[columnHeaders[0].length];
       for (int i = 0; i < resultBuffer.length; i++) {
         resultBuffer[i] = new StringBuffer();
       }
       for (int col = 0; col < columnHeaders[0].length; col++) { // for
         // each
         // column
         for (int row = 0; row < columnHeaders.length; row++) { // for
           // each
           // row
           // except
           // the
           // first
           if (row == 0) { // don't use seperator for first row
             resultBuffer[col].append(columnHeaders[row][col].toString());
           } else { // use the seperator;
             resultBuffer[col].append(seperator + columnHeaders[row][col].toString());
           }
         }
       }
       String[] result = new String[resultBuffer.length];
       for (int i = 0; i < resultBuffer.length; i++) {
         result[i] = resultBuffer[i].toString();
       }
       return result;
     }
     return null;
   }
 
   public String[] getCollapsedHeaders(int axis, char seperator) throws Exception {
     return getCollapsedHeaders(axis, sourceResultSet, seperator);
   }
 
   /**
    * Returns a string array where each element represents the concatenations
    * of the headers for a sing column. Each concatenation is seperated by the
    * parameter "seperator"
    * 
    * @param axis
    *            row or column headers
    * @param resultSet
    *            to headers from
    * @param a
    *            character that represents the seprator between entities of a
    *            column header
    * @return a String array that represents a fully qualified column headers
    * @throws Exception
    */
   public static String[] getCollapsedHeaders(int axis, IPentahoResultSet resultSet, char seperator) throws Exception {
     if (axis != AXIS_COLUMN && axis != AXIS_ROW) {
       throw new IllegalArgumentException(Messages.getString("PentahoDataTransmuter.ERROR_0001_INVALID_AXIS")); //$NON-NLS-1$
     }
     if (resultSet == null) {
       throw new IllegalArgumentException(Messages.getString("PentahoDataTransmuter.ERROR_0002_NULL_DATASET")); //$NON-NLS-1$
     }
 
     switch (axis) {
       case AXIS_COLUMN:
         return getCollapsedColumnHeaders(resultSet, seperator);
 
       case AXIS_ROW:
         return getCollapsedRowHeaders(resultSet, seperator);
 
     }
     return null;
   }
 
   /**
    * 
    * Flatten a resultset based on a particular column, new values for each row
    * trigger a new row in the flattened resultset.
    * 
    * @return IPentahoResultSet
    * 
    */
   public static IPentahoResultSet flattenResultSet(IPentahoResultSet resultSet, int squashColumn) {
     IPentahoResultSet flattenedResultSet = resultSet.memoryCopy();
     if (flattenedResultSet instanceof MemoryResultSet) {
       MemoryResultSet memoryResultSet = (MemoryResultSet) flattenedResultSet;
       // go through the resultset looking at the 'squashColumn'
       // for all values that are the same, create a new row
       // all the data should be missing/null/empty except for 1 row in
       // each
       // column
       // the new row should be fully populated
       Object colHeads[][] = memoryResultSet.getMetaData().getColumnHeaders();
 
       int rowCount = memoryResultSet.getRowCount();
       int colCount = memoryResultSet.getColumnCount();
       Object squashColumnValue = memoryResultSet.getValueAt(0, squashColumn);
       Object squashedRow[] = new Object[colCount];
       List<Object[]> rowsList = new LinkedList<Object[]>();
       rowsList.add(squashedRow);
       for (int row = 0; row < rowCount; row++) {
         Object newSquashColumnValue = memoryResultSet.getValueAt(row, squashColumn);
         if (newSquashColumnValue.equals(squashColumnValue)) {
           // continue row
         } else {
           // start a new row
           squashedRow = new Object[colCount];
           rowsList.add(squashedRow);
         }
         squashColumnValue = newSquashColumnValue;
         for (int col = 0; col < colCount; col++) {
           // try to find a legit value in the row
           Object potentialValue = memoryResultSet.getValueAt(row, col);
           if (potentialValue != null && !potentialValue.toString().equals("")) { //$NON-NLS-1$
             // legit value
             squashedRow[col] = potentialValue;
           }
         }
       }
       Object rows[][] = new Object[rowsList.size()][colCount];
       for (int i = 0; i < rowsList.size(); i++) {
         rows[i] = rowsList.get(i);
       }
       return MemoryResultSet.createFromArrays(colHeads, rows);
     }
     return flattenedResultSet;
   }
 
   public static String dump(IPentahoResultSet source) {
     return dump(source, true);
   }
 
   public static String dump(IPentahoResultSet source, boolean useOrBar) {
     StringBuffer sb = new StringBuffer();
     String orBar = ""; //$NON-NLS-1$
     if (useOrBar) {
       orBar = "|"; //$NON-NLS-1$
     }
     source.beforeFirst();
     Object[][] columnHeaders = source.getMetaData().getColumnHeaders();
    @SuppressWarnings("deprecation")
     Object[][] rowHeaders = source.getMetaData().getRowHeaders();
     String formatString = ""; //$NON-NLS-1$
     if (rowHeaders != null) {
       for (int columns = 0; columns < rowHeaders[0].length; columns++) {
         formatString = formatString + "\t\t"; //$NON-NLS-1$
       }
     }
     if (columnHeaders != null) {
       for (int row = columnHeaders.length - 1; row >= 0; row--) {
         sb.append(formatString);
         for (int column = 0; column < columnHeaders[row].length; column++) {
           sb.append(columnHeaders[row][column] + orBar + "\t"); //$NON-NLS-1$
         }
         sb.append('\n');
       }
     }
     if (rowHeaders != null) {
       for (int row = 0; row < rowHeaders.length; row++) {
         for (int column = rowHeaders[row].length - 1; column >= 0; column--) {
           sb.append(rowHeaders[row][column] + orBar + "\t"); //$NON-NLS-1$
         }
         Object[] dataRow = source.next();
         for (int column = 0; column < dataRow.length; column++) {
           sb.append(dataRow[column] + orBar + "\t"); //$NON-NLS-1$
         }
         sb.append('\n');
       }
     } else {
       Object[] dataRow = source.next();
       while (dataRow != null) {
         for (int column = 0; column < dataRow.length; column++) {
           sb.append(dataRow[column] + orBar + "\t"); //$NON-NLS-1$
         }
         sb.append('\n');
         dataRow = source.next();
       }
     }
     sb.append('\n');
 
     source.beforeFirst();
     return sb.toString();
   }
 
   public static IPentahoResultSet crossTab(IPentahoResultSet source, int columnToPivot, int measureColumn,
       Format pivotDataFormatter) {
     return crossTab(source, columnToPivot, measureColumn, pivotDataFormatter, true);
   }
 
   /**
    * This method takes a column of data, and turns it into multiple columns
    * based on the values within the column. The measure column specified is
    * then distributed among the newly created columns. Sparse data is handled
    * by populating missing cells with nulls.
    * 
    * @param source
    *            The starting IPentahoResultSet
    * @param columnToPivot
    *            The column that becomes multiple columns
    * @param measureColumn
    *            The measures column to distribute to the new columns created
    * @param pivotDataFormatter
    *            If the column to pivot requires formatting, this is the
    *            formatter to use
    * @param orderedMaps
    *            If true, will sort the new column names alphabetically. If
    *            false, the colums will be created in the order of appearance
    *            in the rows
    * @return IPentahoResultSet containing crosstabbed data.
    * 
    * @author mbatchelor
    * 
    * Assumptions/Limitations: a- This only works with one dimension going
    * across. This won't do multi-level crosstabbing.
    * 
    * b- All column numbers given are assumed to be ZERO based. So, the first
    * column is 0.
    * 
    * Example:
    * 
    * Starting Resultset ================== SaleDate|Vendor|Product|Total_Units
    * 1-1-05 |A |Cola |12.0 1-1-05 |A |Diet |7.0 1-1-05 |B |RootBr |16.0 1-1-05
    * |B |Diet |14.0 2-1-05 |A |Diet |8.0 2-1-05 |B |Ginger |17.0
    * 
    * Could Become ============ Parameters: (startingResultSet, 0, 3,
    * simpleDateFormatObject, true)
    * 
    * Vendor|Product|Jan 2005|Feb 2005 A |Cola |12.0 |null A |Diet |7.0 |8.0 B
    * |RootBr |16.0 |null B |Diet |14.0 |null B |Ginger |null |17.0
    * 
    * Or could become =============== Parameters: (startingResultSet, 2, 3,
    * null, false)
    * 
    * SaleDate|Vendor|Cola|Diet|RootBr|Ginger 1-1-05 |A |12.0|7.0 |null |null
    * 1-1-05 |B |null|14.0|16.0 |null 2-1-05 |A |null|8.0 |null |null 2-1-05 |B
    * |null|null|null |17.0
    * 
    * 
    */
   public static IPentahoResultSet crossTab(IPentahoResultSet source, int columnToPivot, int measureColumn,
       Format pivotDataFormatter, boolean orderedMaps) {
     return crossTab(source, columnToPivot, measureColumn, -1, pivotDataFormatter, null, orderedMaps);
   }
 
   /**
    * This method takes a column of data, and turns it into multiple columns
    * based on the values within the column. The measure column specified is
    * then distributed among the newly created columns. Sparse data is handled
    * by populating missing cells with nulls. This version of the method also
    * takes two additional parameters - the column to sort the new columns by,
    * and a formatter for that column.
    * 
    * @param source
    *            The starting IPentahoResultSet
    * @param columnToPivot
    *            The column that becomes multiple columns
    * @param measureColumn
    *            The measures column to distribute to the new columns created
    * @param columnToSortColumnsBy
    *            The column to use to sort the newly created columns by
    * @param pivotDataFormatter
    *            If the column to pivot requires formatting, this is the
    *            formatter to use
    * @param sortDataFormatter
    *            The formatter to use to convert the sort column to a string
    * @param orderedMaps
    *            If true, will sort the new column names alphabetically. If
    *            false, the colums will be created in the order of appearance
    *            in the rows.
    * @return IPentahoResultSet containing crosstabbed data.
    * 
    * @author mbatchelor
    * 
    * Assumptions: a- This only works with one dimension going across. This
    * won't do multi-level crosstabbing.
    * 
    * b- All column numbers given are assumed to be ZERO based. So, the first
    * column is 0.
    * 
    * c- If a columnToSortColumnsBy column is specified (>=0), the orderedMaps
    * flag will be set to true regardless of the passed in value.
    * 
    * d- For now, we assume that the column to sort by is removed from the
    * dataset. In the future, this will not be an assumption.
    * 
    * TODO: Update method to make removal of sort-by column optional.
    * 
    * Example:
    * 
    * Starting Resultset
    * ================== 
    * Month|Vendor|Rank|Counts
    * Jan |A-A-A |2 |92
    * Jan |Acme  |3 |200
    * Jan |Ajax  |4 |163
    * Feb |Acme |3 |27
    * Feb |Ajax |4 |102
    * Mar |Donn |1 |427
    * Mar |A-A-A|2 |301
    * Mar |Acme |3 |82
    * 
    * Could Become 
    * ============ 
    * parameters: (startingResultSet, 1, 3, 2, null, decimalFormatObject, true)
    * 
    * Month|Donn|A-A-A|Acme|Ajax
    * Jan |null|92 |200 |163
    * Feb |null|null |27 |102
    * Mar |427 |301 |82 |null
    * 
    * 
    */
   @SuppressWarnings({"unchecked"})
   public static IPentahoResultSet crossTab(IPentahoResultSet source, int columnToPivot, int measureColumn,
       int columnToSortColumnsBy, Format pivotDataFormatter, Format sortDataFormatter, boolean orderedMaps) {
 
     // System.out.println("*********************Before********************");
     // System.out.println(dump(source));
 
     // First, do some error checking...
     if (source == null) {
       throw new IllegalArgumentException(Messages.getString("PentahoDataTransmuter.ERROR_0002_NULL_DATASET")); //$NON-NLS-1$
     }
     int sourceColumnCount = source.getColumnCount();
     if (columnToPivot > sourceColumnCount) {
       throw new IllegalArgumentException(Messages
           .getString("PentahoDataTransmuter.ERROR_0003_INVALID_PIVOT_COLUMN")); //$NON-NLS-1$
     }
     if (measureColumn > sourceColumnCount) {
       throw new IllegalArgumentException(Messages
           .getString("PentahoDataTransmuter.ERROR_0004_INVALID_MEASURE_COLUMN")); //$NON-NLS-1$
     }
     if (columnToSortColumnsBy > sourceColumnCount) {
       throw new IllegalArgumentException(Messages
           .getString("PentahoDataTransmuter.ERROR_0005_INVALID_SORT_COLUMN")); //$NON-NLS-1$
     }
     // Now, setup so variables and such
     final String sortPrefixSeparator = "\t"; //$NON-NLS-1$
     Map<Object,Object> rowMap = null, newHeadersMap = null;
 
     // Force orderedMaps to true if we're sorting using a column in the
     // input.
     // See assumption 'c' in the comment-block above.
     if (columnToSortColumnsBy >= 0) {
       orderedMaps = true;
     }
 
     if (orderedMaps) {
       // If we're using ordered maps, then our maps become TreeMaps.
       rowMap = new TreeMap<Object,Object>(); // Map of the current row
       newHeadersMap = new TreeMap<Object,Object>(); // New header columns map
     } else {
       // Use Apache ListOrderedMap so that columns become ordered by their
       // position in the data.
       rowMap = ListOrderedMap.decorate(new HashMap());
       newHeadersMap = ListOrderedMap.decorate(new HashMap());
     }
     List<String> columnHeaders = new ArrayList<String>(); // All column headers
     // Create column headers of the known columns
     IPentahoMetaData origMetaData = source.getMetaData();
     Object[][] origColHeaders = origMetaData.getColumnHeaders();
     for (int i = 0; i < origColHeaders[0].length; i++) {
       if ((i != columnToPivot) && (i != measureColumn)) {
         columnHeaders.add(origColHeaders[0][i].toString());
       }
     }
     // Now, we have the starting column headers. Time to start iterating
     // over the data.
     Object colPivotData, colMeasureData, cellData, colToSortByRaw;
     Object[] rowData = source.next();
     String columnPrefix = null;
     Map<Object,Object> currentMap = null;
     while (rowData != null) {
       colPivotData = rowData[columnToPivot]; // The data we're pivoting to columns
       if (colPivotData == null) {
         throw new IllegalArgumentException(Messages
             .getString("PentahoDataTransmuter.ERROR_0006_CANNOT_PIVOT_NULL_DATA")); //$NON-NLS-1$
       }
       colMeasureData = rowData[measureColumn]; // The value data we're
       // using as the final set.
       if (columnToSortColumnsBy >= 0) {
         colToSortByRaw = rowData[columnToSortColumnsBy];
         if (colToSortByRaw == null) {
           throw new IllegalArgumentException(Messages
               .getString("PentahoDataTransmuter.ERROR_0007_CANNOT_SORT_NULL_DATA")); //$NON-NLS-1$
         }
         if (sortDataFormatter != null) {
           columnPrefix = sortDataFormatter.format(colToSortByRaw);
         } else {
           columnPrefix = colToSortByRaw.toString();
         }
       }
       currentMap = rowMap; // Start at the top...
       for (int i = 0; i < rowData.length; i++) {
         if ((i != columnToPivot) && (i != measureColumn) && (i != columnToSortColumnsBy)) {
           // I'm on a data row (like a column header). Find it in the
           // row map.
           cellData = currentMap.get(rowData[i]);
           if (cellData == null) {
             // Add the column to the current map of maps
             Map newColumnMap = null;
             if (orderedMaps) {
               newColumnMap = new TreeMap();
             } else {
               newColumnMap = ListOrderedMap.decorate(new HashMap());
             }
 
             currentMap.put(rowData[i], newColumnMap);
             currentMap = newColumnMap;
           } else {
             // Found something here - it should be a map.
             currentMap = (Map) cellData;
           }
         }
       }
       // Done iterating over columns creating other columns. Now, create
       // (or locate) pivoted data as a column
       String formattedPivotData = null;
       if (pivotDataFormatter != null) {
         if (pivotDataFormatter instanceof MessageFormat) {
           formattedPivotData = pivotDataFormatter.format(new Object[] { colPivotData });
         } else {
           formattedPivotData = pivotDataFormatter.format(colPivotData);
         }
       } else {
         formattedPivotData = colPivotData.toString();
       }
       // Do column sorting based on another input column.
       if (columnToSortColumnsBy >= 0) {
         formattedPivotData = columnPrefix + sortPrefixSeparator + formattedPivotData;
       }
       // For this row, look for the pivoted data in newHeaders.
       Object header = newHeadersMap.get(formattedPivotData);
       if (header == null) {
         // Create a map containing just the new column headers
         newHeadersMap.put(formattedPivotData, ""); //$NON-NLS-1$
       }
       // Put the measure data in the final spot in the map
       currentMap.put(formattedPivotData, colMeasureData);
       // Get next row
       rowData = source.next();
     }
     // Add the new headers to the columnHeaders list
     Iterator hIt = newHeadersMap.keySet().iterator();
     while (hIt.hasNext()) {
       columnHeaders.add(hIt.next().toString());
     }
 
     // Create each individual row
     ArrayList rows = new ArrayList();
     // The uniqueItems collections allows me to handle null/missing values
     Collection uniqueItems = rowMap.keySet();
     // For each unique item outer-column, iterate and create the rows
     // recursively
     Iterator it = uniqueItems.iterator();
     List newCurRow = new ArrayList();
     while (it.hasNext()) {
       // Iterate over each unique value in the outermost map
       recurseCreateRow(it.next(), rowMap, rows, newCurRow, newHeadersMap);
       newCurRow.clear();
     }
 
     // Now, if there was a sort-column specified, we need to remove the
     // prefix from the
     // column header before creating the final set of headers.
     if (columnToSortColumnsBy >= 0) {
       String aHeader;
       int tabIdx;
       for (int i = 0; i < columnHeaders.size(); i++) {
         aHeader = columnHeaders.get(i);
         tabIdx = aHeader.indexOf(sortPrefixSeparator);
         if (tabIdx >= 0) {
           columnHeaders.set(i, aHeader.substring(tabIdx + 1));
         }
       }
     }
     // Create the final resultset.
     IPentahoResultSet result = MemoryResultSet.createFromLists(columnHeaders, rows);
     // System.out.println("*************************After***********************");
     // System.out.println(dump(result));
     return result;
   }
 
   @SuppressWarnings({"unchecked"})
   private static List recurseCreateRow(Object lookup, Map mapToLookupIn, List rows, List curRow, Map newColumnsMap) {
     // Recursive method used to create each row.
     if (curRow == null) {
       // Starting off with no row, so create it.
       curRow = new ArrayList();
     }
     // Look for the unique value in the current map.
     Object value = mapToLookupIn.get(lookup);
 
     if (value == null) {
       // If value is null, we're at the end of the chain, and this is a
       // missing value.
       curRow.add(null);
       return null;
     } else {
       // Value wasn't null - it must be another map to traverse, or the
       // measure
       if (value instanceof Map) {
         // We've got a map here. Add the lookup value to the row, and
         // iterate over the map
         curRow.add(lookup);
         Map newLkupMap = (Map) value; // The newLkupMap is the new map
         // to iterate over
         Collection uniqueItems = newLkupMap.keySet(); // Get the keys
         // from the
         // lookup map
         // Is this a new column? If so, make sure to iterate over the
         // newColumnsMap to make sure to detect null values.
         Iterator it = uniqueItems.iterator();
         // Check to see if this is a new column. If so, iterate over the
         // newColumnsMap.
         // Otherwise, iterate over the map values.
         Object obj = it.next();
         if (newColumnsMap.get(obj.toString()) != null) {
           it = newColumnsMap.keySet().iterator();
         } else {
           it = uniqueItems.iterator();
         }
         // When going to a new row, set the row up to contain all the
         // prior columns before the one we're processing.
         List beforeValues = new ArrayList(curRow);
         beforeValues.remove(beforeValues.size() - 1);
         // When a new row is added, this is the that gets returned
         List addedValues = null;
         // Iterate over either the new columns, or the existing map and
         // repeat recursively
         while (it.hasNext()) {
           // Recursively call this method to traverse all the columns
           addedValues = recurseCreateRow(it.next(), newLkupMap, rows, curRow, newColumnsMap);
           if (addedValues != null) {
             // We have a new current row!
             curRow.clear();
             curRow.addAll(addedValues);
           }
         }
         // If we added the values (i.e. measures), then add this row to
         // the list, and return a
         // new row that had all the preceeding columns in it.
         if (addedValues == null) {
           if (curRow.size() > newColumnsMap.size()) {
             rows.add(new ArrayList(curRow));
           }
           return beforeValues;
         } else {
           // No values added - unwind the stack.
           curRow.clear();
           curRow.addAll(beforeValues);
           return null;
         }
       } else {
         // We have an actual value (measure) - add it to the row, and
         // iterate.
         curRow.add(value);
         return null;
       }
     }
   }
 
   private static boolean isEqual(Object a, Object b) {
     if ((a == null) && (b == null)) {
       return true;
     } else if ((a == null) || (b == null)) {
       return false;
     } else {
       return a.equals(b);
     }
   }
 
   private static boolean isNewRow(Object[] thisRow, Object[] lastRow) {
     for (int i = 0; i < thisRow.length; i++) {
       if (!isEqual(thisRow[i], lastRow[i])) {
         return true;
       }
     }
     return false;
   }
 
   private static String formatPivotData(Object colPivotData, Format pivotDataFormatter) {
     String formattedPivotData = null;
     if (pivotDataFormatter != null) {
       if (pivotDataFormatter instanceof MessageFormat) {
         formattedPivotData = pivotDataFormatter.format(new Object[] { colPivotData });
       } else {
         formattedPivotData = pivotDataFormatter.format(colPivotData);
       }
     } else {
       formattedPivotData = colPivotData.toString();
     }
     return formattedPivotData;
   }
 
   /**
    * Marc Batchelor Diatribe
    * It's 6am, I've been up all night, but I gotta right this down. So, here goes.
    * 
    * This crosstab function is similar to the "old" version exception that it works in
    * many more cases than the old one did. The only requirement is that either the data 
    * is ordered by the left-over columns that will indicate that the row is a continuation
    * of the previous row, or the method is handed a column that will be used to find unique
    * rows in the result set.
    * 
    * The old crosstab function used maps for each data element of a row. This only works in
    * very certain conditions, but is extremely fragile. But, that approach allowed
    * the same row of data to be spread out all over the result set. So, with better capability
    * and performance comes a limitation. This can best be seen by attempting to crosstab a 
    * simple query from the sample data (and crosstabbing on REGION): 
    * 
    * select REGION, DEPARTMENT, SUM(ACTUAL) as ACTUAL
    * from quadrant_actuals
    * group by REGION, DEPARTMENT
    * order by REGION, DEPARTMENT
 
    *       
    * REGION   DEPARTMENT            ACTUAL
    * Central  Executive Management  1776282
    * Central  Finance               3106680
    *        ... more rows ...
    * Eastern  Executive Management  1507580
    * Eastern  Finance               3039180
    *        ... more rows ...
    * Southern Executive Management  1507580
    * Southern Finance               3039180
    *        ... more rows ...
    * 
    * Note that when the data is ordered this way, we would have to keep
    * coming back to the Executive Management row multiple times to
    * fill in the empty holes. In the old version of the crossTab utility, it would
    * do this by using a complex series of maps to hold the data not being crosstabbed.
    * This worked OK on very simple result sets, but absolutely fell apart once
    * the same values appeared in the same row (like a 2 in one column and a 2 in
    * another column), or when null values appeared in the data.
    * 
    * Due to customer requirements, I needed to re-create the crosstab functionality,
    * and I chose to do it a little more sensibly. Now, instead of maps of maps, I simply
    * traverse the dataset twice. Once to gather all the new column headers, and then
    * again to fill out the data. The down side to this approach is that the above data
    * set could not be crosstabbed in that format. Crosstabbing the above format
    * would result in data that looks like this:
 
    * DEPARTMENT                Central       Eastern     ....
    * Executive Management      1776282       ---
    * Finance                   3106680       ---
    *        ... more rows ...
    * Executive Management      ---           1507580
    * Finance                   ---           3039180
    *        ... more rows ...
    * 
    * A completely useless crosstab. So, with this new function, a simple change
    * needs to be made to the query as follows:
    *       
    * select REGION, DEPARTMENT, SUM(ACTUAL) as ACTUAL
    * from quadrant_actuals
    * group by DEPARTMENT, REGION -- Switched the order
    * order by DEPARTMENT, REGION -- Switched the order
    * 
    * By simply making sure that the row to be crosstabbed is not the primary
    * sort, the data comes out more sensibly for crosstabbing:
    * 
    * REGION   DEPARTMENT            ACTUAL
    * Central  Executive Management  1776282
    * Eastern  Executive Management  1507580
    *        ... more rows ...
    * Central  Finance               3106680
    * Eastern  Finance               3039180
    *        ... more rows ...
    * 
    * In other words, the remaining data that identifies a single row is grouped
    * together.
    * 
    * The other alternative is to pass in a uniqueRowIdentifierColumn. This column
    * will be used to determine whether the row has been seen before. If it has,
    * then it will grab the already written row, and update it.
    * 
    * So, in the above example, you would specify the uniqueRowIdentifierColumn as
    * column 1 (DEPARTMENT). Note that this doesn't perform as well since every row will 
    * result in a map lookup for this column. However, for XML resultsets, there may be
    * no other way to accomplish what you need.
    *       
    * @param source
    * @param columnToPivot
    * @param measureColumn
    * @param columnToSortColumnsBy
    * @param pivotDataFormatter
    * @param sortDataFormatter
    * @param orderedMaps
    * @param uniqueRowIdentifierColumn
    * @return
    */
 
   public static IPentahoResultSet crossTabOrdered(IPentahoResultSet source, int columnToPivot, int measureColumn,
       Format pivotDataFormatter) {
     return crossTabOrdered(source, columnToPivot, measureColumn, pivotDataFormatter, true);
   }
 
   public static IPentahoResultSet crossTabOrdered(IPentahoResultSet source, int columnToPivot, int measureColumn,
       Format pivotDataFormatter, boolean orderedMaps) {
     return crossTabOrdered(source, columnToPivot, measureColumn, -1, pivotDataFormatter, null, orderedMaps, -1);
   }
 
   @SuppressWarnings({"unchecked"})
   public static IPentahoResultSet crossTabOrdered(IPentahoResultSet source, int columnToPivot, int measureColumn,
       int columnToSortColumnsBy, Format pivotDataFormatter, Format sortDataFormatter, boolean orderedMaps,
       int uniqueRowIdentifierColumn) {
 
     // System.out.println("*********************Before********************");
     //System.out.println(dump(source));
 
     // First, do some error checking...
     if (source == null) {
       throw new IllegalArgumentException(Messages.getString("PentahoDataTransmuter.ERROR_0002_NULL_DATASET")); //$NON-NLS-1$
     }
     int sourceColumnCount = source.getColumnCount();
     if (columnToPivot > sourceColumnCount) {
       throw new IllegalArgumentException(Messages
           .getString("PentahoDataTransmuter.ERROR_0003_INVALID_PIVOT_COLUMN")); //$NON-NLS-1$
     }
     if (measureColumn > sourceColumnCount) {
       throw new IllegalArgumentException(Messages
           .getString("PentahoDataTransmuter.ERROR_0004_INVALID_MEASURE_COLUMN")); //$NON-NLS-1$
     }
     if (columnToSortColumnsBy > sourceColumnCount) {
       throw new IllegalArgumentException(Messages
           .getString("PentahoDataTransmuter.ERROR_0005_INVALID_SORT_COLUMN")); //$NON-NLS-1$
     }
     if (uniqueRowIdentifierColumn > sourceColumnCount) {
       throw new IllegalArgumentException(Messages
           .getString("PentahoDataTransmuter.ERROR_0008_INVALID_UNIQUE_COLUMN")); //$NON-NLS-1$
     }
 
     // Now, setup so variables and such
     final String sortPrefixSeparator = "\t"; //$NON-NLS-1$
     Map newHeadersMap = null;
 
     Map uniqueColumnIdentifierMap = null;
 
     if (uniqueRowIdentifierColumn >= 0) {
       uniqueColumnIdentifierMap = new HashMap();
     }
 
     int uniqueRowIdentifierColumnPostShift = -1;
 
     // Force orderedMaps to true if we're sorting using a column in the
     // input.
     // See assumption 'c' in the comment-block above.
     if (columnToSortColumnsBy >= 0) {
       orderedMaps = true;
     }
 
     if (orderedMaps) {
       // If we're using ordered maps, then our maps become TreeMaps.
       newHeadersMap = new TreeMap(); // New header columns map
     } else {
       // Use Apache ListOrderedMap so that columns become ordered by their
       // position in the data.
       newHeadersMap = ListOrderedMap.decorate(new HashMap());
     }
     List columnHeaders = new ArrayList(); // All column headers
     // Create column headers of the known columns
     IPentahoMetaData origMetaData = source.getMetaData();
     Object[][] origColHeaders = origMetaData.getColumnHeaders();
 
     for (int i = 0; i < origColHeaders[0].length; i++) {
       if ((i != columnToPivot) && (i != measureColumn) && ((i != columnToSortColumnsBy))) {
         columnHeaders.add(origColHeaders[0][i].toString());
         if (i == uniqueRowIdentifierColumn) {
           uniqueRowIdentifierColumnPostShift = columnHeaders.size() - 1;
         }
       }
     }
     int baseColumnsCount = columnHeaders.size();
     // Now, we have the starting column headers. Time to start iterating
     // over the data.
     Object colPivotData, colMeasureData, colToSortByRaw;
     Object[] rowData = source.next();
     String columnPrefix = null;
 
     /*
      * First, find out what the new columns will be - this will traverse 
      * the dataset gathering the unique values for the column containing
      * the values that will become the new columns.
      * 
      */
     Map newColumnHeadersRaw = new HashMap();
     Integer placeHolder = new Integer(0);
     while (rowData != null) {
       colPivotData = rowData[columnToPivot]; // The data we're pivoting to columns
       if (colPivotData == null) {
         throw new IllegalArgumentException(Messages
             .getString("PentahoDataTransmuter.ERROR_0006_CANNOT_PIVOT_NULL_DATA")); //$NON-NLS-1$
       }
 
       // newColumnHeadersRaw.add(colPivotData);
       if (columnToSortColumnsBy >= 0) {
         colToSortByRaw = rowData[columnToSortColumnsBy];
         if (colToSortByRaw == null) {
           throw new IllegalArgumentException(Messages
               .getString("PentahoDataTransmuter.ERROR_0007_CANNOT_SORT_NULL_DATA")); //$NON-NLS-1$
         }
         if (sortDataFormatter != null) {
           columnPrefix = sortDataFormatter.format(colToSortByRaw);
         } else {
           columnPrefix = colToSortByRaw.toString();
         }
       }
 
       if (!newColumnHeadersRaw.containsKey(colPivotData)) {
         newColumnHeadersRaw.put(colPivotData, placeHolder);
         // Do column sorting based on another input column.
         String formattedPivotData = formatPivotData(colPivotData, pivotDataFormatter);
         if (columnToSortColumnsBy >= 0) {
           formattedPivotData = columnPrefix + sortPrefixSeparator + formattedPivotData;
         }
         newHeadersMap.put(formattedPivotData, colPivotData);
       }
 
       rowData = source.next();
     }
     source.beforeFirst();
 
     // Now, we have all the new headers. Next, update the rawHeaders with the 
     // target column number.
 
     Iterator it = newHeadersMap.entrySet().iterator();
     int columnIndex = columnHeaders.size(); // start adding columns where the fixed columns leave off.
     while (it.hasNext()) {
       Map.Entry me = (Map.Entry) it.next();
       newColumnHeadersRaw.put(me.getValue(), new Integer(columnIndex));
       columnHeaders.add(formatPivotData(me.getValue(), pivotDataFormatter));
       columnIndex++;
     }
 
     // OK - we now know the new column headers, and the place they'll
     // appear in all the rows. Now, it's time to construct each row.
 
     int columnCount = columnHeaders.size();
     int rowPos;
     MemoryResultSet mrs = new MemoryResultSet();
     MemoryMetaData md = new MemoryMetaData(columnHeaders);
     mrs.setMetaData(md);
     Object[] thisRow = new Object[baseColumnsCount];
 
     Object[] currentRow = new Object[columnCount];
     rowData = source.next();
     boolean isFirstRow = true;
     while (rowData != null) {
       colMeasureData = rowData[measureColumn]; // The value data we're
       rowPos = 0;
       for (int i = 0; i < rowData.length; i++) {
         if ((i != columnToPivot) && (i != measureColumn) && (i != columnToSortColumnsBy)) {
           // This is data - put it in the correct spot in the row
           thisRow[rowPos] = rowData[i];
           rowPos++;
         }
       }
       // OK - we got the base data. Is this a new row, or a continuation
       // of the previous row.
 
       boolean newRow = true;
 
       Object uniqueRowIdentifierValue = null;
       Integer previousRowNumber = null;
 
       // First, did they provide us with a hint.
       if (uniqueRowIdentifierColumn >= 0) {
         uniqueRowIdentifierValue = rowData[uniqueRowIdentifierColumn] != null ? rowData[uniqueRowIdentifierColumn]
             : "_NULL_VALUE_"; //$NON-NLS-1$
         previousRowNumber = (Integer) uniqueColumnIdentifierMap.get(uniqueRowIdentifierValue);
         if (previousRowNumber != null) {
           addIfNeeded(currentRow, mrs, uniqueColumnIdentifierMap, uniqueRowIdentifierColumnPostShift);
           currentRow = mrs.getDataRow(previousRowNumber.intValue());
           newRow = false;
         }
       }
 
       newRow = (newRow && !isFirstRow && isNewRow(thisRow, currentRow));
       if (newRow) {
         addIfNeeded(currentRow, mrs, uniqueColumnIdentifierMap, uniqueRowIdentifierColumnPostShift);
         // Create new current row - the row inprogress.
         currentRow = new Object[columnCount];
         // Now, copy thisRow to currentRow.
         System.arraycopy(thisRow, 0, currentRow, 0, thisRow.length);
       } else if (isFirstRow) {
         System.arraycopy(thisRow, 0, currentRow, 0, thisRow.length);
       }
       isFirstRow = false;
       colPivotData = rowData[columnToPivot]; // The data we're pivoting to columns
       Integer targetColumn = (Integer) newColumnHeadersRaw.get(colPivotData);
       currentRow[targetColumn.intValue()] = colMeasureData;
 
       // Get next row
       rowData = source.next();
     }
     addIfNeeded(currentRow, mrs, uniqueColumnIdentifierMap, uniqueRowIdentifierColumnPostShift);
     // System.out.println("*************************After***********************");
     // System.out.println(dump(mrs));
     return mrs;
   }
 
   @SuppressWarnings({"unchecked"})
   private static void addIfNeeded(Object[] currentRow, MemoryResultSet mrs, Map uniqueColumnIdentifierMap,
       int uniqueRowIdentifierColumnPostShift) {
     if (uniqueRowIdentifierColumnPostShift >= 0) {
       Object tmpValue = currentRow[uniqueRowIdentifierColumnPostShift] != null ? currentRow[uniqueRowIdentifierColumnPostShift]
           : "_NULL_VALUE_"; //$NON-NLS-1$
       if (!uniqueColumnIdentifierMap.containsKey(tmpValue)) {
         int addedRow = mrs.addRow(currentRow);
         uniqueColumnIdentifierMap.put(tmpValue, new Integer(addedRow));
       }
     } else {
       mrs.addRow(currentRow);
     }
   }
 
 }
