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
  * @created Sep 7, 2005 
  * @author James Dixon
  */
 
 package org.pentaho.commons.connection.memory;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.dom4j.Node;
 import org.pentaho.commons.connection.IMultiDimensionalResultSet;
 import org.pentaho.commons.connection.IPeekable;
 import org.pentaho.commons.connection.IPentahoMetaData;
 import org.pentaho.commons.connection.IPentahoResultSet;
 
 public class MemoryResultSet implements IPentahoResultSet, IPeekable, IMultiDimensionalResultSet {
 
   private IPentahoMetaData metaData;
 
   protected List<Object[]> rows;
 
   protected Iterator iterator = null;
 
   protected Object peekRow[];
   
   protected int rowIndex = 0;
 
   public MemoryResultSet() {
     rows = new ArrayList<Object[]>();
   }
 
   public MemoryResultSet(IPentahoMetaData metaData) {
     this.metaData = metaData;
     rows = new ArrayList<Object[]>();
   }
 
   public void setMetaData(IPentahoMetaData metaData) {
     this.metaData = metaData;
     rows = new ArrayList<Object[]>();
   }
 
   @SuppressWarnings({"unchecked"})
   public void setRows(List rows) {
     this.rows = rows;
     rowIndex = 0;
   }
 
   public int addRow(Object[] row) {
     rows.add(row);
     return rows.size() - 1;
   }
 
   public IPentahoMetaData getMetaData() {
     return metaData;
   }
 
   public Object[] peek() {
 
     if( peekRow == null ) {
       peekRow = next();
     }
     return peekRow;
   }
   
   public Object[] next() {
     if (peekRow != null) {
       Object row[] = peekRow;
       peekRow = null;
       return row;
     }
     if (iterator == null) {
       iterator = rows.iterator();
     }
     if (iterator.hasNext()) {
       rowIndex++;
       return (Object[]) iterator.next();
     } else {
       return null;
     }
   }
 
   public void close() {
     // dispose of the iterator so the rows can be iterated again
     iterator = null;
     rowIndex = 0;
   }
 
   public void closeConnection() {
     close();
   }
 
   public void dispose() {
     close();
   }
 
   public static MemoryResultSet createFromArrays(Object[][] colHeads, Object[][] theRows) {
     IPentahoMetaData metaData = new MemoryMetaData(colHeads, null);
     MemoryResultSet result = new MemoryResultSet(metaData);
     for (int i = 0; i < theRows.length; i++) {
       result.addRow(theRows[i]);
     }
     return result;
   }
 
   /**
    * 
    * @param colHeaders List of Objects, where each object represents a column header
    * in the  metadata of the result set. Most likely will be a List of String.
    * @param data List of (List of Objects). Each List of Objects will become a row
    * of the ResultSet.
    * @return MemoryResultSet
    */
   public static MemoryResultSet createFromLists(List colHeaders, List data) {
     Object columnHeaders[][] = new String[1][colHeaders.size()];
     for (int i = 0; i < colHeaders.size(); i++) {
       columnHeaders[0][i] = colHeaders.get(i);
     }
     IPentahoMetaData metaData = new MemoryMetaData(columnHeaders, null);
     MemoryResultSet result = new MemoryResultSet(metaData);
     for (int i = 0; i < data.size(); i++) {
       result.addRow(((List) data.get(i)).toArray());
     }
     return result;
   }
 
   /**
    * 
    * @param name
    * @param items List of Object[] (list of array of objects). Each item in the list
    * will become an row of the ResultSet
    * @return MemoryResultSet
    */
   public static MemoryResultSet createList(String name, List items) {
     Object columnHeaders[][] = new String[][] { { name } };
     IPentahoMetaData metaData = new MemoryMetaData(columnHeaders, null);
     MemoryResultSet result = new MemoryResultSet(metaData);
     Iterator listIterator = items.iterator();
     while (listIterator.hasNext()) {
       Object item = listIterator.next();
       Object row[] = new Object[] { item };
       result.addRow(row);
     }
     return result;
   }
 
   public static MemoryResultSet createFromActionSequenceInputsNode(Node rootNode) {
     List colHeaders = rootNode.selectNodes("columns/*"); //$NON-NLS-1$
     Object columnHeaders[][] = new String[1][colHeaders.size()];
     String columnTypes[] = new String[colHeaders.size()];
     for (int i = 0; i < colHeaders.size(); i++) {
       Node theNode = (Node) colHeaders.get(i);
       columnHeaders[0][i] = theNode.getName();
       columnTypes[i] = getNodeText("@type", theNode); //$NON-NLS-1$
     }
     MemoryMetaData metaData = new MemoryMetaData(columnHeaders, null);
     metaData.setColumnTypes(columnTypes);
     MemoryResultSet result = new MemoryResultSet(metaData);
 
     List rowNodes = rootNode.selectNodes("default-value/row"); //$NON-NLS-1$
     for (int r = 0; r < rowNodes.size(); r++) {
       Node theNode = (Node) rowNodes.get(r);
       Object[] theRow = new Object[columnHeaders[0].length];
       for (int c = 0; c < columnHeaders[0].length; c++) {
         theRow[c] = getNodeText(columnHeaders[0][c].toString(), theNode);
       }
       result.addRow(theRow);
     }
     return result;
   }
 
   public static String getNodeText(String xpath, Node rootNode) {
     return getNodeText(xpath, rootNode, null);
   }
   
   public static String getNodeText(String xpath, Node rootNode, String defaultValue) {
     if (rootNode == null) {
       return (defaultValue);
     }
     Node node = rootNode.selectSingleNode(xpath);
     if (node == null) {
       return defaultValue;
     }
     return node.getText();
   }
   
   public boolean isScrollable() {
     return true;
   }
 
   public int getRowCount() {
     return rows.size();
   }
 
   /**
    * Returns the value of the specified row and the specified column from
    * within the resultset.
    * 
    * @param row
    *            the row index.
    * @param column
    *            the column index.
    * @return the value.
    */
   public Object getValueAt(int row, int column) {
     Object[] theRow = rows.get(row);
     return theRow[column];
   }
 
   public int getColumnCount() {
     return metaData.getColumnCount();
   }
 
   public IPentahoResultSet memoryCopy() {
     try {
       IPentahoMetaData metadata = getMetaData();
       Object columnHeaders[][] = metadata.getColumnHeaders();
 
       MemoryMetaData cachedMetaData = new MemoryMetaData(columnHeaders, null);
       MemoryResultSet cachedResultSet = new MemoryResultSet(cachedMetaData);
 
       Object[] rowObjects = next();
       while (rowObjects != null) {
         cachedResultSet.addRow(rowObjects);
         rowObjects = next();
       }
       return cachedResultSet;
     } finally {
       close();
     }
   }
 
   public void beforeFirst() {
     iterator = rows.iterator();
     rowIndex = 0;
   }
 
   public Object[] getDataColumn(int column) {
     Object[] result = new Object[getRowCount()];
     int index = 0;
     Iterator iter = rows.iterator();
     while (iter.hasNext()) {
       result[index] = ((Object[]) iter.next())[column];
       index++;
     }
     return result;
   }
 
   public Object[] getDataRow(int row) {
     if (row >= rows.size()) {
       return null;
     }
     return rows.get(row);
   }
 
   public Object[] nextFlattened() {
    @SuppressWarnings("deprecation")
     Object rowHeaders[][] = metaData.getRowHeaders();
     if( rowHeaders == null ) {
       // we have no row headers so we can call the regular next()
       return next();
     }
     // get the row
     Object row[] = next();
     if( row == null ) {
       // we have got to the end
       return null;
     }
     // do we have row headers to return also?
     if( rowIndex <= rowHeaders.length ) {
       // pull out the right row headers
       Object rowHeads[] = rowHeaders[rowIndex-1];
       // create the flattened row
       Object flatRow[] = new Object[rowHeads.length+row.length];
       // copy in the row headers and row objects
       System.arraycopy(rowHeads, 0, flatRow, 0, rowHeads.length);
       System.arraycopy(row, 0, flatRow, rowHeads.length, row.length);
       return flatRow;
     }
     return row;
   }
 
   public Object[] peekFlattened() {
    @SuppressWarnings("deprecation")
     Object rowHeaders[][] = metaData.getRowHeaders();
     if( rowHeaders == null ) {
       // we have no row headers so we can call the regular peek()
       return peek();
     }
     // get the row
     Object row[] = peek();
     if( row == null ) {
       // we have got to the end
       return null;
     }
     // do we have row headers to return also?
     if( rowIndex <= rowHeaders.length ) {
       // pull out the right row headers
       Object rowHeads[] = rowHeaders[rowIndex-1];
       // create the flattened row
       Object flatRow[] = new Object[rowHeads.length+row.length];
       // copy in the row headers and row objects
       System.arraycopy(rowHeads, 0, flatRow, 0, rowHeads.length);
       System.arraycopy(row, 0, flatRow, rowHeads.length, row.length);
       return flatRow;
     }
     return row;
   }
 }
