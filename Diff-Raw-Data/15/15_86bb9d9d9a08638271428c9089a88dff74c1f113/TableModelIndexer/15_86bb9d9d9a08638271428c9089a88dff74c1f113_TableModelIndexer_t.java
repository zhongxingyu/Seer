 /**
  *  Copyright (C) Nick Ebbutt September 2009
  *
  *  This file is part of ObjectDefinitions Ltd. FilterTable.
  *  nick@objectdefinitions.com
  *  http://www.objectdefinitions.com/filtertable
  *
  *  FilterTable is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Lesser General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  ObjectDefinitions Ltd. FilterTable is distributed in the hope that it will
  *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public License
  *  along with ObjectDefinitions Ltd. FilterTable.
  *  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.od.filtertable;
 
 import javax.swing.table.TableModel;
 import java.util.*;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Nick Ebbutt
  * Date: 02-Sep-2008
  * Time: 16:16:25
  *
  * This class performs the indexing which allows us to search for string values within
  * the RowFilteringTableModel's source model.
  */
 public class TableModelIndexer {
 
     private TableModel tableModel;
     private FilterColumnConfig filterColumnConfig;
     private int initialIndexDepth;
     private boolean caseSensitive;
     private CharTrie<MutableTableCell, TableCellSet> index = new TableCellSetTrieNode(false);
     private MutableCharSequence mutableCharRange = new MutableCharSequence();
     protected MutableTableCell[][] tableCells;
     protected int size;
     private boolean includeSubstrings = true;
     private LinkedList<MutableTableCell> cellsToReindex = new LinkedList<MutableTableCell>();
 
     public TableModelIndexer(TableModel tableModel, int initialIndexDepth) {
         this(tableModel, false, initialIndexDepth);
     }
 
     public TableModelIndexer(TableModel tableModel, boolean caseSensitive, int initialIndexDepth) {
         this.tableModel = tableModel;
         this.filterColumnConfig = new FilterColumnConfig(tableModel);
         this.caseSensitive = caseSensitive;
         rebuildIndex(initialIndexDepth);
     }
 
     public void setIncludeSubstrings(boolean include) {
         this.includeSubstrings = include;
         rebuildIndex();
     }
 
     public boolean isIncludeSubstringsInSearch() {
         return includeSubstrings;
     }
 
     public void setTableModel(TableModel t) {
         this.tableModel = t;
         tableStructureChanged();
     }
 
     public void tableStructureChanged() {
         filterColumnConfig.tableChanged(tableModel);
         rebuildIndex();
     }
 
     //re-build the index to the specified initialDepth
     public void rebuildIndex(int initialDepth) {
         this.initialIndexDepth = initialDepth;
         rebuildIndex();
     }
 
     //re-build the index to the current initial depth
     public void rebuildIndex() {
         index = new TableCellSetTrieNode(caseSensitive);
        int currentRowCount = tableModel.getRowCount();
        tableCells = createExpandedTableCellsArray(currentRowCount);
         size = tableModel.getRowCount();
         populateWithTableCellsAndAddToIndex(0, size - 1);
     }
 
     /**
      * @return A TableCellSet of matching cells for the char sequence supplied
      */
     public TableCellSet getCellsContaining(CharSequence s) {
         updateIndexForSequence(s);
         return index.getValues(s);
     }
 
     //rows have been inserted to the source model
     public void insertRows(int startIndex, int endIndex) {
         int insertedRowCount = (endIndex - startIndex) + 1;
         ensureCapacity(size + insertedRowCount);
         increaseMutableRowIndexes(startIndex, insertedRowCount);
         insertCellsAndAddToIndex(startIndex, endIndex, insertedRowCount);
         size+=insertedRowCount;
     }
 
     //rows have been removed from the source model
     public void removeRows(int startIndex, int endIndex) {
         int removedRowCount = (endIndex - startIndex) + 1;
         decreaseMutableRowIndexes(endIndex + 1, removedRowCount);
         removeCellsAndRemoveFromIndex(startIndex, endIndex, removedRowCount);
         size-=removedRowCount;
     }
 
     //the value in a cell has been updated, we need to re-index it
     public void reIndexCell(int row, int col) {
         removeFromIndex(tableCells[row][col]);
         refreshTableCellValue(row, col);
         addToIndex(tableCells[row][col], initialIndexDepth);
     }
 
     public void setFormatter(FilterFormatter filterFormat, Integer... columnIndexes) {
         filterColumnConfig.setFormatter(filterFormat, columnIndexes);
         rebuildIndex();
     }
 
     public void setFormatter(FilterFormatter filterFormat, Class... columnClasses) {
         filterColumnConfig.setFormatter(filterFormat, columnClasses);
         rebuildIndex();
     }
 
     public void setFormatter(FilterFormatter filterFormat, String... columnNames) {
         filterColumnConfig.setFormatter(filterFormat, columnNames);
         rebuildIndex();
     }
 
     public void clearFormatters() {
         filterColumnConfig.clearFormatters();
         rebuildIndex();
     }
 
     public void trimIndexToInitialDepth() {
         index.trimToDepth(initialIndexDepth);
         setCellIndexDepth(initialIndexDepth);
     }
 
     private void setCellIndexDepth(int indexDepth) {
         for ( int row=0; row < size; row++) {
             for ( int col=0; col < tableModel.getColumnCount(); col++ ) {
                 tableCells[row][col].setIndexedDepth(indexDepth);
             }
         }
     }
 
     private void insertCellsAndAddToIndex(int startIndex, int endIndex, int insertedRowCount) {
         System.arraycopy(tableCells, startIndex, tableCells, startIndex + insertedRowCount, size - startIndex);
         createNewTableCellRows(startIndex, endIndex);
         populateWithTableCellsAndAddToIndex(startIndex, endIndex);
     }
 
     private void removeCellsAndRemoveFromIndex(int startIndex, int endIndex, int removedRowCount) {
         removeRowsFromIndex(startIndex, endIndex);
         System.arraycopy(tableCells, endIndex + 1, tableCells, startIndex, size - (endIndex + 1) );
         freeTableCellsForGarbageCollector(removedRowCount);
     }
 
     private void freeTableCellsForGarbageCollector(int removedRowCount) {
         for ( int row=size - 1; row >= size - removedRowCount; row -- ) {
             tableCells[row] = new MutableTableCell[tableModel.getColumnCount()];
         }
     }
 
     //start and end row are inclusive
     private void populateWithTableCellsAndAddToIndex(int startRow, int endRow) {
         for ( int row = startRow; row <= endRow; row ++) {
             MutableRowIndex rowIndex = new MutableRowIndex(row);
             for ( int col = 0; col < tableModel.getColumnCount(); col ++) {
                 tableCells[row][col] = new MutableTableCell(rowIndex, col);
                 refreshTableCellValue(row, col);
                 addToIndex(tableCells[row][col], initialIndexDepth);
             }
         }
     }
 
     private void removeRowsFromIndex(int startIndex, int endIndex) {
         for ( int row = startIndex;  row <= endIndex; row ++ ) {
             for ( int col=0; col < tableModel.getColumnCount(); col++ ) {
                 removeFromIndex(tableCells[row][col]);
             }
         }
     }
 
     private void createNewTableCellRows(int startIndex, int endIndex) {
         for ( int row=startIndex; row <= endIndex; row ++) {
             tableCells[row] = new MutableTableCell[tableModel.getColumnCount()];
         }
     }
 
     private void increaseMutableRowIndexes(int startIndex, int insertedRowCount) {
         for (int loop=startIndex; loop < size; loop ++) {
             tableCells[loop][0].adjustRowIndex(insertedRowCount);
         }
     }
 
     private void decreaseMutableRowIndexes(int startIndex, int removedRowCount) {
         for (int loop=startIndex; loop <  size; loop ++) {
             tableCells[loop][0].adjustRowIndex(-removedRowCount);
         }
     }
 
     private void addToIndex(MutableTableCell cell, int maxDepth) {
         cell.setIndexedDepth(maxDepth);
         if ( ! cell.isNullValue()) {
             readFormattedCellValueIntoCharRange(cell);
             int maxStartLocation = includeSubstrings ? mutableCharRange.totalSequenceLength() : 1;
             for ( int start = 0; start < maxStartLocation; start ++ ) {
                 mutableCharRange.setStart(start);
                 mutableCharRange.setEnd(Math.min(start + maxDepth, mutableCharRange.totalSequenceLength()));
                 index.addValueForAllPrefixes(mutableCharRange, cell);
             }
         }
     }
 
     private void removeFromIndex(MutableTableCell cell) {
         int depthToRemove = cell.getIndexedDepth();
         if ( ! cell.isNullValue()) {
             readFormattedCellValueIntoCharRange(cell);
             int maxStartLocation = includeSubstrings ? mutableCharRange.totalSequenceLength() : 1;
             for ( int start = 0; start < maxStartLocation; start ++ ) {
                 mutableCharRange.setStart(start);
                 mutableCharRange.setEnd(Math.min(start + depthToRemove, mutableCharRange.totalSequenceLength()));
                 index.removeValueForAllPrefixes(mutableCharRange, cell);
             }
         }
     }
 
     private void readFormattedCellValueIntoCharRange(MutableTableCell cell) {
         FilterFormatter formatter = filterColumnConfig.getFormatter(cell.getCol());
         CharSequence chars = formatter.format(cell.getValue());
         mutableCharRange.setSegment(chars);
     }
 
     private void refreshTableCellValue(int row, int col) {
         tableCells[row][col].setValue(tableModel.getValueAt(row, col));
     }
 
     private void ensureCapacity(int newSize) {
         if ( newSize > tableCells.length ) {
            MutableTableCell[][] newTableCells = createExpandedTableCellsArray(newSize);
             System.arraycopy(tableCells, 0, newTableCells, 0, size);
             this.tableCells = newTableCells;
         }
     }
 
    //create a new array with room for future inserts, see ArrayList.ensureCapacity for the derivation
    private MutableTableCell[][] createExpandedTableCellsArray(int newRowCount) {
        return new MutableTableCell[((newRowCount * 3) / 2) + 1][tableModel.getColumnCount()];
    }

     private void updateIndexForSequence(CharSequence s) {
         if ( s.length() > initialIndexDepth) {
             CharSequence subString = s.subSequence(0, s.length() - 1);
             updateIndexForSequence(subString);
 
             int newIndexDepth = s.length();
 
             Collection<MutableTableCell> cellCollection = index.getValues(subString);
             //have to add tol list to avoid concurrent mod
             for ( MutableTableCell cell : cellCollection ) {
                 if ( cell.getIndexedDepth() < newIndexDepth ) {
                     cellsToReindex.add(cell);
                 }
             }
 
             while(cellsToReindex.size() > 0) {
                 addToIndex(cellsToReindex.remove(0), newIndexDepth);
             }
         }
     }
 
 }
