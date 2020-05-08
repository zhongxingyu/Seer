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
 
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 import javax.swing.table.TableModel;
 import java.text.Format;
 import java.util.*;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Nick Ebbutt
  * Date: 03-Sep-2008
  * Time: 15:06:09
  *
  * A table model which performs row filtering, hiding rows which do not contain a specified string value
  *
  * Table cells may be indexed up-front to a depth specified when buildIndex(n) is called.
  * If the up-front index depth specified is shorted than a filter string specified, the trie paths
  * matching the filter string will be indexed on demand to the depth required. If the desire is for
  * low memory footprint, it is best to choose a low or zero up front depth. If the requirement is
  * for best filter performance, a higher up-front depth is best, but for large tables (tens of thousands of
  * cells) this may require a large amount of heap space. Most systems will opt for a trade-off between the two.
  */
 public class RowFilteringTableModel extends CustomEventTableModel implements IndexedTableModel {
 
     private TableModel wrappedModel;
     private int[] rowMap;
     private int[] oldRowMap;
     private BitSet rowStatusBitSet;
     private BitSet oldRowStatusBitSet;
     private String searchTerm;
     private TableModelIndexer tableModelIndexer;
     private TableModelListener tableModelListener;
     private Map<MutableRowIndex, TreeSet<Integer>> matchingRowsAndCols;
     private TreeSet[] matchingColumnsByWrappedModelRowIndex;
     private boolean filter = true;
     private int matchCount;
 
     public RowFilteringTableModel(TableModel wrappedModel) {
         this(wrappedModel, false, 1);
     }
 
     public RowFilteringTableModel(TableModel wrappedModel, int initialIndexDepth) {
         this(wrappedModel, false, initialIndexDepth);
     }
 
     public RowFilteringTableModel(TableModel wrappedModel, boolean isCaseSensitive, int initialIndexDepth) {
         this.wrappedModel = wrappedModel;
         this.tableModelIndexer = new TableModelIndexer(wrappedModel, isCaseSensitive, initialIndexDepth);
         initializeNonFilteredRowMap();
         addWrappedModelListener();
     }
 
     public void setTableModel(TableModel wrappedModel) {
         this.wrappedModel.removeTableModelListener(tableModelListener);
         this.wrappedModel = wrappedModel;
         tableModelIndexer.setTableModel(wrappedModel);
         addWrappedModelListener();
         recalculateRowStatusBitSets();
         fireTableStructureChanged();
     }
 
     public void buildIndexToDepth(int initialDepth) {
         tableModelIndexer.rebuildIndex(initialDepth);
     }
 
     public void setSearchTerm(String searchTerm) {
         if ( ! equals(searchTerm, this.searchTerm)) {
             this.searchTerm = searchTerm;
             recalculateAndFireDataChanged();
         }
     }
 
     public int getMatchCount() {
         return matchCount;
     }
 
     public void setIncludeSubstringsInSearch(boolean include) {
         if ( include != tableModelIndexer.isIncludeSubstringsInSearch()) {
             tableModelIndexer.setIncludeSubstrings(include);
             recalculateAndFireDataChanged();
         }
     }
 
     public boolean isIncludeSubstringsInSearch() {
         return tableModelIndexer.isIncludeSubstringsInSearch();
     }
 
     /**
      * Set whether this model should filter out rows which do not contain a cell matching the search term
      * Even if filtering is false, the indexing will still take place provided a search term is set, in order to enable
      * the isCellMatchingSearch to be used to implement find functionality
      */
     public void setFilterRows(boolean filter) {
         if ( filter != this.filter) {
             this.filter = filter;
             recalculateAndFireDataChanged();
         }
     }
 
     public void clearSearch() {
         this.searchTerm = null;
         recalculateAndFireDataChanged();
     }
 
     public boolean isCellMatchingSearch(int rowIndex, int colIndex) {
         boolean result = false;
         if ( isSearchTermSet() ) {
             Set s = matchingColumnsByWrappedModelRowIndex[rowMap[rowIndex]];
             result = s != null && s.contains(colIndex);
         }
         return result;
     }
 
     /** Format specified by column index will be lost if a table structure change event occurs **/
     public void setFormatter(Format format, Integer... columnIndexes) {
         setFormatter(new FilterFormatter.FormatAdapterFormatter(format), columnIndexes);
     }
 
     /** FilterFormatter specified by column index will be lost if a table structure change event occurs **/
     public void setFormatter(FilterFormatter filterFormat, Integer... columnIndexes) {
         tableModelIndexer.setFormatter(filterFormat, columnIndexes);
         recalculateAndFireDataChanged();
     }
 
     public void setFormatter(Format format, Class... columnClasses) {
         setFormatter(new FilterFormatter.FormatAdapterFormatter(format), columnClasses);
     }
 
     public void setFormatter(FilterFormatter filterFormat, Class... columnClasses) {
         tableModelIndexer.setFormatter(filterFormat, columnClasses);
         recalculateAndFireDataChanged();
     }
 
     public void setFormatter(Format format, String... columnNames) {
         setFormatter(new FilterFormatter.FormatAdapterFormatter(format), columnNames);
     }
 
     public void setFormatter(FilterFormatter filterFormat, String... columnNames) {
         tableModelIndexer.setFormatter(filterFormat, columnNames);
         recalculateAndFireDataChanged();
     }
 
     public void clearFormatters() {
         tableModelIndexer.clearFormatters();
         recalculateAndFireDataChanged();
     }
 
     private void recalculateAndFireDataChanged() {
         recalculateRowStatusBitSets();
         fireTableDataChanged();
     }
 
     private void createInitialRowStatusBitSets() {
         rowStatusBitSet = new BitSet(wrappedModel.getRowCount());
         rowStatusBitSet.set(0, wrappedModel.getRowCount());
     }
 
     private void recalculateRowStatusBitSets() {
         //we still want to do the search even if filter is false, since find functionality may
         //still use isCellMatchingSearch
         setOldBitSetAndRowMap();
         if ( isSearchTermSet() ) {
             doSearchAndRecalculate(true);
         } else {
             initializeNonFilteredRowMap();
         }
     }
 
     private void recalculateRowStatusBitSetsOnDataUpdate() {
         if ( isSearchTermSet() ) {
            setOldBitSetAndRowMap();
             doSearchAndRecalculate(false);
         }
     }
 
     //for a data update, forceRecalc should be false - we only need to recalc if the data change affects the set of
     //cells which match the search term. For a structural change or an insert/delete event, forceRecalc should be
     //true, since wrapped model row indexes will generally change, even if the matching cells are unaffected
     //so we need to rebuild our maps
     private void doSearchAndRecalculate(boolean forceRecalculate) {
 
         TableCellSet s = tableModelIndexer.getCellsContaining(searchTerm);
         Map<MutableRowIndex, TreeSet<Integer>> newMatchingRowsAndCols = s.getRowColumnMap();
 
         boolean matchesHaveChanged = newMatchingRowsAndCols != matchingRowsAndCols;
         if ( forceRecalculate || matchesHaveChanged) {
             matchCount = s.size();
             matchingRowsAndCols = newMatchingRowsAndCols;
             createNewMatchingColumnsByRow();
             if ( filter ) {
                 createNewRowBitSet();
                 recalcRowMap();
             } else {
                 initializeNonFilteredRowMap();
             }
         }
     }
 
     private void initializeNonFilteredRowMap() {
         createInitialRowStatusBitSets();
         recalcRowMap();
     }
 
     private boolean isSearchTermSet() {
         return searchTerm != null && searchTerm.length() > 0;
     }
 
     private void setOldBitSetAndRowMap() {
         oldRowStatusBitSet = rowStatusBitSet;
         oldRowMap = rowMap;
     }
 
     private void createNewRowBitSet() {
         rowStatusBitSet = new BitSet(wrappedModel.getRowCount());
         for ( MutableRowIndex rowIndex : matchingRowsAndCols.keySet()) {
             rowStatusBitSet.set(rowIndex.index);
         }
     }
 
     private void createNewMatchingColumnsByRow() {
         matchingColumnsByWrappedModelRowIndex = new TreeSet[wrappedModel.getRowCount()];
         for ( Map.Entry<MutableRowIndex,TreeSet<Integer>> entry : matchingRowsAndCols.entrySet()) {
             matchingColumnsByWrappedModelRowIndex[entry.getKey().index] = entry.getValue();
         }
     }
 
 
     //recalculate the row map using the bits in the row status BitSet
     //every bit set true indicates that row index should be included
     private void recalcRowMap() {
         rowMap = new int[rowStatusBitSet.cardinality()];
         int addedRowCount = 0;
         for ( int row = 0; row < wrappedModel.getRowCount(); row ++) {
             if ( rowStatusBitSet.get(row) ) {
                 rowMap[addedRowCount] = row;
                 addedRowCount++;
             }
         }
     }
 
     private void addWrappedModelListener() {
         tableModelListener = createTableModelListener();
         wrappedModel.addTableModelListener(tableModelListener);
     }
 
     protected TableModelListener createTableModelListener() {
         return new FilteredTableModelEventParser();
     }
 
     protected class FilteredTableModelEventParser extends TableModelEventParser {
         public FilteredTableModelEventParser() {
             super( new TableModelEventParser.TableModelEventParserListener() {
                     public void tableStructureChanged(TableModelEvent e) {
                         tableModelIndexer.tableStructureChanged();
                         recalculateRowStatusBitSets();
                         fireTableStructureChanged();
                         clearOldRowBitsetAndRowMap();
                     }
 
                     public void tableDataChanged(TableModelEvent e) {
                         tableModelIndexer.rebuildIndex();
                         recalculateAndFireDataChanged();
                         clearOldRowBitsetAndRowMap();
                     }
 
                     public void tableRowsUpdated(int firstRow, int lastRow, TableModelEvent e) {
                         for ( int row=firstRow; row <=lastRow; row++ ) {
                             for ( int col=0; col < wrappedModel.getColumnCount(); col++ ) {
                                 tableModelIndexer.reIndexCell(row,col);
                             }
                         }
                         recalculateRowStatusBitSetsOnDataUpdate();
                         generateEventsForUpdate(firstRow, lastRow, TableModelEvent.ALL_COLUMNS);
                         clearOldRowBitsetAndRowMap();
                     }
 
 
                     public void tableCellsUpdated(int firstRow, int lastRow, int column, TableModelEvent e) {
                         for ( int row=firstRow; row <=lastRow; row++ ) {
                             tableModelIndexer.reIndexCell(row,column);
                         }
                         recalculateRowStatusBitSetsOnDataUpdate();
                         generateEventsForUpdate(firstRow, lastRow, column);
                         clearOldRowBitsetAndRowMap();
                     }
 
                     public void tableRowsDeleted(int firstRow, int lastRow, TableModelEvent e) {
                         tableModelIndexer.removeRows(firstRow, lastRow);
                         recalculateRowStatusBitSets();
 
                         int affectedRows = getAffectedRows(oldRowStatusBitSet, firstRow, lastRow);
                         if ( affectedRows > 0) {
                             int oldFirstIndex = getIndexInOldModel(firstRow);
                             fireTableRowsDeleted(oldFirstIndex, (oldFirstIndex + affectedRows - 1));
                         }
                         clearOldRowBitsetAndRowMap();
                     }
 
                     public void tableRowsInserted(int firstRow, int lastRow, TableModelEvent e) {
                         tableModelIndexer.insertRows(firstRow, lastRow);
                         recalculateRowStatusBitSets();
 
                         int affectedRows = getAffectedRows(rowStatusBitSet, firstRow, lastRow);
                         if ( affectedRows > 0) {
                             int oldFirstIndex = getIndexInOldModel(firstRow);
                             fireTableRowsInserted(oldFirstIndex, (oldFirstIndex + affectedRows - 1));
                         }
                         clearOldRowBitsetAndRowMap();
                     }
 
                 }
             );
         }
     }
 
     //these are used to help generate table model events during recalc
     //no point having them in memory once events have been generated
     private void clearOldRowBitsetAndRowMap() {
         this.oldRowMap = null;
         this.oldRowStatusBitSet = null;
     }
 
     public int getRowCount() {
         return rowMap.length;
     }
 
     public int getColumnCount() {
         return wrappedModel.getColumnCount();
     }
 
     public String getColumnName(int columnIndex) {
         return wrappedModel.getColumnName(columnIndex);
     }
 
     public Class<?> getColumnClass(int columnIndex) {
         return wrappedModel.getColumnClass(columnIndex);
     }
 
     public boolean isCellEditable(int rowIndex, int columnIndex) {
         return wrappedModel.isCellEditable(rowMap[rowIndex], columnIndex);
     }
 
     public Object getValueAt(int rowIndex, int columnIndex) {
         return wrappedModel.getValueAt(rowMap[rowIndex], columnIndex);
     }
 
     public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
         wrappedModel.setValueAt(aValue, rowMap[rowIndex], columnIndex);
     }
 
     public int getRowInUnderlyingModel(int row) {
         return rowMap[row];
     }
 
     public void trimIndexToInitialDepth() {
         tableModelIndexer.trimIndexToInitialDepth();
     }
 
     /**
      * Re-evaluation of the filter criteria following an update may result in rows being inserted,
      * deleted or updated in any combination, which a bit tricky to handle.
      */
     private void generateEventsForUpdate(int firstRow, int lastRow, int column) {
 
         boolean containsInsert = false, containsDelete = false;
 
         //generate a stack of the actions required for each row in the updated range, with first event at top
         Stack<UpdateAction> updatesStack = new Stack<UpdateAction>();
         for ( int row = lastRow; row >= firstRow; row --) {
             if ( rowStatusBitSet.get(row) && oldRowStatusBitSet.get(row)) {
                 updatesStack.add(UpdateAction.UPDATE);
             } else if ( rowStatusBitSet.get(row) && ! oldRowStatusBitSet.get(row)) {
                 containsInsert = true;
                 updatesStack.add(UpdateAction.INSERT);
             } else if ( ! rowStatusBitSet.get(row) && oldRowStatusBitSet.get(row)) {
                 containsDelete = true;
                 updatesStack.add(UpdateAction.DELETE);
             }
             //if the affected row was not visible in the old model and also is not visible in the
             //new model, there is no update action required.
         }
 
         //get the events required according to the contents of the stack of actions
         List<TableModelEvent> modelEvents = getRequiredEvents(
                 firstRow,
                 column,
                 updatesStack,
                 containsInsert,
                 containsDelete
         );
 
         for ( TableModelEvent event : modelEvents ) {
             fireTableChanged(event);
         }
     }
 
     /**
      * Generate a list of the required events based on the stack of update actions.
      * Insert or Delete events must be fired first, so that the number of rows remains correct
      * We can probably get away with sending update events separately after the inserts/deletes,
      * although arguably, since this makes an atomic change to the source table non-atomic, and we
      * should use a single dataChanged event instead.
      *
      * If the required changes include both a delete and insert,
      * the only way to handle this is data changed event
      *
      * If the insert/delete affects a range of rows which is non-contiguous there is also no way to handle this
      * in a single TableModelEvent, so we need a full data changed event.
      *
      * This means, for updates which cause an insert or delete, there are three acceptable patterns of UpdateAction
      * in the stack, anything else will require a data change event
      *
      * UPDATE affecting 1 or more rows
      * INSERT OR DELETE affecting 1 or more rows
      * UPDATE affecting 1 or more rows
      *
      * UPDATE affecting 1 or more rows
      * INSERT OR DELETE affecting 1 or more rows
      *
      * INSERT OR DELETE affecting 1 or more rows
      * UPDATE affecting 1 or more rows
      */
     private List<TableModelEvent> getRequiredEvents(int firstRow, int column, Stack<UpdateAction> updatesStack, boolean containsInsert, boolean containsDelete) {
         int startRowForEvent = getIndexInOldModel(firstRow);
         List<TableModelEvent> modelEvents = new ArrayList<TableModelEvent>();
         if ( containsDelete && containsInsert) {
             modelEvents.add(createDataChangedEvent());
         } else if ( containsInsert ) {
             startRowForEvent += addUpdateEvent( modelEvents, updatesStack, startRowForEvent, column);
             startRowForEvent += addInsertEventAtStart( modelEvents, updatesStack, startRowForEvent);
             startRowForEvent += addUpdateEvent( modelEvents, updatesStack, startRowForEvent, column);
             checkContiguousRange(updatesStack, modelEvents);
         } else if ( containsDelete ) {
             startRowForEvent += addUpdateEvent( modelEvents, updatesStack, startRowForEvent, column);
             addDeleteEventAtStart( modelEvents, updatesStack, startRowForEvent);
             startRowForEvent += addUpdateEvent( modelEvents, updatesStack, startRowForEvent, column);
             checkContiguousRange(updatesStack, modelEvents);
         } else {
             addUpdateEvent( modelEvents, updatesStack, startRowForEvent, column);
         }
         return modelEvents;
     }
 
     private void checkContiguousRange(Stack<UpdateAction> updatesStack, List<TableModelEvent> modelEvents) {
         if ( ! updatesStack.isEmpty()) { //the inserts/deletes are non-contiguous, we can't represent this a single event
             modelEvents.clear();
             modelEvents.add(createDataChangedEvent());
         }
     }
 
     //add a delete event at the start of the event list, if there is one or more insert action(s) at the top of the stack
     private int addDeleteEventAtStart(List<TableModelEvent> modelEvents, Stack<UpdateAction> updatesStack, int startRowForEvent) {
         int rowsAffected = getCountFromTopOfStack(updatesStack, UpdateAction.DELETE);
         if ( rowsAffected > 0) {
             modelEvents.add(0, createTableModelEvent(RowFilteringTableModel.this, startRowForEvent, startRowForEvent + (rowsAffected - 1), TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE));
         }
         return rowsAffected;
     }
 
     //add an insert event at the start of the event list, if there is one or more insert action(s) at the top of the stack
     private int addInsertEventAtStart(List<TableModelEvent> modelEvents, Stack<UpdateAction> updatesStack, int startRowForEvent) {
         int rowsAffected = getCountFromTopOfStack(updatesStack, UpdateAction.INSERT);
         if ( rowsAffected > 0) {
             modelEvents.add(0, createTableModelEvent(RowFilteringTableModel.this, startRowForEvent, startRowForEvent + (rowsAffected - 1), TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
         }
         return rowsAffected;
     }
 
     //add an update event to the end of the event list, if there is one or more insert action(s) at the top of the stack
     private int addUpdateEvent(List<TableModelEvent> modelEvents, Stack<UpdateAction> updatesStack, int startRowForEvent, int column) {
         int rowsAffected = getCountFromTopOfStack(updatesStack, UpdateAction.UPDATE);
         if ( rowsAffected > 0) {
             modelEvents.add(createTableModelEvent(RowFilteringTableModel.this, startRowForEvent, startRowForEvent + (rowsAffected - 1), column, TableModelEvent.UPDATE));
         }
         return rowsAffected;
     }
 
     private int getCountFromTopOfStack(Stack<UpdateAction> updatesStack, UpdateAction type) {
         int rowsAffected = 0;
         while( updatesStack.size() > 0 && updatesStack.peek() == type) {
             rowsAffected++;
             updatesStack.pop();
         }
         return rowsAffected;
     }
 
     private int getAffectedRows(BitSet bitSet, int firstRow, int lastRow) {
         int affectedRows = 0;
         for ( int row = firstRow; row <= lastRow; row ++ ) {
             if ( bitSet.get(row) ) {
                 affectedRows++;
             }
         }
         return affectedRows;
     }
 
 
     private int getIndexInOldModel(int oldIndexInWrappedModel) {
         int result = Arrays.binarySearch(oldRowMap, oldIndexInWrappedModel);
         return result >= 0 ? result : -result -1;
     }
 
     private enum UpdateAction {
         UPDATE, INSERT, DELETE
     }
 }
