 package com.od.filtertable;
 
 import javax.swing.table.TableColumnModel;
 import javax.swing.event.TableModelListener;
 import javax.swing.event.TableModelEvent;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Nick Ebbutt
  * Date: 04-Sep-2009
  * Time: 11:46:56
  *
  * TableCellFinder provides logic to manage storing a 'current' found cell, and to cycle through the find
  * results after the user sets a search term on the RowFilteringTableModel
  *
  * The TableCell returned by the find methods uses the coordinates of the cell in the JTable as it is
  * presented to the user (i.e. col is the col index in the column model, and row is the top level table
  * model row)
  *
  * The TableCellFinder needs TableColumnModel information to work out the table model column indexes
  * for the visible columns in the table. This information is provided as a ColumnSource instance (this is
  * primarily to cater for Bhavaya tables which distribute columns between fixed and scrollable JTables - 
  * here we have separate column models showing data from the same underlying table model, so we combine
  * the column models into one combined ColumnSource, to allow the find to cycle around the columns from
  * both tables)
  *
  */
 public class TableCellFinder implements TableModelListener {
 
     private TableCell lastFindResult = TableCell.NO_MATCH_TABLE_CELL;
     private IndexedTableModel indexedTableModel;
     private ColumnSource columnSource;
     private TableModelEventParser eventParser;
 
     public TableCellFinder(TableColumnModel tableColumnModel, IndexedTableModel indexedTableModel) {
         this(new TableColumnModelColumnSource(tableColumnModel), indexedTableModel);
     }
 
     public TableCellFinder(ColumnSource columnSource, IndexedTableModel indexedTableModel) {
         this.columnSource = columnSource;
         this.indexedTableModel = indexedTableModel;
         createEventParser();
     }
 
     public TableCell getLastFindResult() {
         return lastFindResult;
     }
 
     public void clearLastFindResult() {
         this.lastFindResult = TableCell.NO_MATCH_TABLE_CELL;
     }
 
     public TableCell findNextMatchingCell(TableCell cell) {
         cell = cell != null && cell != TableCell.NO_MATCH_TABLE_CELL ? cell : new TableCell(0,0);
         return getNextMatchingCell(0, cell.getRow(), cell.getCol() + 1, true);
     }
 
     public TableCell findPreviousMatchingCell(TableCell cell) {
         cell = cell != null && cell != TableCell.NO_MATCH_TABLE_CELL ? cell : new TableCell(0,0);
         return getNextMatchingCell(0, cell.getRow(), cell.getCol() - 1, false);
     }
 
     /**
      * @return the first matching TableCell instance starting from cell 0,0 - or TableCell.NO_MATCH_TABLE_CELL if no cells match the current search
      */
     public TableCell findFirstMatchingCell() {
         return getNextMatchingCell(0, 0, 0, true);
     }
 
 
     /**
      * @return the next matching TableCell instance starting at the cell provided, or TableCell.NO_MATCH_TABLE_CELL if no cells
      * match the current search. The next matching cell will be the starting cell if there is only one match
      */
     private TableCell getNextMatchingCell(int rowsSearched, int currentRow, int currentCol, boolean isForwards) {
         //in case we are finding the next cell from a cell location which is no longer valid in the table
         //currentRow may be >= rowCount, but we just carry on the find from the nearest valid row
         currentRow = Math.min(indexedTableModel.getRowCount() - 1, currentRow);
 
         TableCell result = TableCell.NO_MATCH_TABLE_CELL;
         if (currentRow >= 0 && rowsSearched <= indexedTableModel.getRowCount()) {
             result = getNextMatchInRow(currentRow, currentCol, isForwards);
             if (result == TableCell.NO_MATCH_TABLE_CELL) {
                 int nextRow = isForwards ?
                         (currentRow + 1) % indexedTableModel.getRowCount() :
                         currentRow == 0 ? indexedTableModel.getRowCount() - 1 : currentRow - 1;
                int nextCol = isForwards ? 0 : indexedTableModel.getColumnCount() - 1;
                 result = getNextMatchingCell(rowsSearched + 1, nextRow, nextCol, isForwards);
             }
         }
         lastFindResult = result;
         return result;
     }
 
     //returns the cell at currentRow / currentCol if it matches, or the next matching cell in the row
     //either forwards or backwards, or NO_MATCH_TABLE_CELL if no subsequent match can be found
     private TableCell getNextMatchInRow(final int currentRow, int currentCol, boolean isForwards) {
         TableCell result = TableCell.NO_MATCH_TABLE_CELL;
         while ( currentCol >= 0 && currentCol < columnSource.getColumnCount() ) {
             boolean matchesSearch = indexedTableModel.isCellMatchingSearch(
                 currentRow, columnSource.getTableModelColumnIndex(currentCol)
             );
 
             if (matchesSearch){
                 result = new TableCell(currentRow, currentCol);
                 break;
             }
             currentCol = isForwards ? currentCol + 1 : currentCol - 1;
         }
         return result;
     }
 
 
     public void tableChanged(TableModelEvent e) {
         eventParser.tableChanged(e);
     }
 
     //some table model events invalidate the 'last found cell' or change its grid location, we need to manage this
     private void createEventParser() {
         eventParser = new TableModelEventParser(new TableModelEventParser.TableModelEventParserListener() {
             public void tableStructureChanged(TableModelEvent e) {
                 clearLastFindResult();
             }
 
             public void tableDataChanged(TableModelEvent e) {
                 clearLastFindResult();
             }
 
             public void tableRowsUpdated(int firstRow, int lastRow, TableModelEvent e) {
                 //if the new value does not match the IndexedTableModle.isCellMatchingSearch() will now be false
             }
 
             public void tableCellsUpdated(int firstRow, int lastRow, int column, TableModelEvent e) {
                 //if the new value does not match the IndexedTableModle.isCellMatchingSearch() will now be false
             }
 
             public void tableRowsDeleted(int firstRow, int lastRow, TableModelEvent e) {
                 if ( isLastFindInRowRange(firstRow, lastRow)) {
                     clearLastFindResult(); //list find result row is deleted
                 } else if ( lastRow < lastFindResult.getRow()) {
                     lastFindResult = new TableCell( //last find result row is moved up by the row delete
                         lastFindResult.getRow() - (lastRow - firstRow + 1),
                         lastFindResult.getCol()
                     );
                 }
             }
 
             public void tableRowsInserted(int firstRow, int lastRow, TableModelEvent e) {
                 if ( lastRow <= lastFindResult.getRow()) {
                     lastFindResult = new TableCell( //last find result row is moved down by the row insert
                         lastFindResult.getRow() + (lastRow - firstRow + 1),
                         lastFindResult.getCol()
                     );
                 }
             }
 
             private boolean isLastFindInRowRange(int firstRow, int lastRow) {
                 return lastFindResult.getRow() >= firstRow && lastFindResult.getRow() <= lastRow;
             }
 
         });
         indexedTableModel.addTableModelListener(eventParser);
     }
 
     public static interface ColumnSource {
 
         public int getTableModelColumnIndex(int columnModelIndex);
 
         public int getColumnCount();
     }
 
     private static class TableColumnModelColumnSource implements ColumnSource {
 
         private TableColumnModel tableColumnModel;
 
         public TableColumnModelColumnSource(TableColumnModel tableColumnModel) {
             this.tableColumnModel = tableColumnModel;
         }
 
         public int getTableModelColumnIndex(int columnModelIndex) {
             return tableColumnModel.getColumn(columnModelIndex).getModelIndex();
         }
 
         public int getColumnCount() {
             return tableColumnModel.getColumnCount();
         }
     }
 
 }
