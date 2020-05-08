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
 
 import java.util.*;
 
 import com.od.filtertable.TableModelIndexer;
 import com.od.filtertable.MutableTableCell;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Nick Ebbutt
  * Date: 02-Sep-2008
  * Time: 16:04:04
  */
 public class TestTableModelIndexer extends AbstractFilteredTableTest {
 
     private static final int IT_Strips_match_count = 128;
     private static final int IT_Strips_last_row_matching = 447;
     private static final int IT_Strips_first_row_matching = 142;
     private static final String IT_STRIPS = "IT_Strips";
 
     public TestTableModelIndexer() {
         super("/test1.csv");
     }
 
     public void testIndexing() {
         long start = System.currentTimeMillis();
         TableModelIndexer indexer = new TableModelIndexer(testTableModel, 1);
         System.out.println("Indexing took " + (System.currentTimeMillis() - start) + " millis");
         testRowIndexes(indexer);
 
         start = System.currentTimeMillis();
         Collection<MutableTableCell> cells = indexer.getCellsContaining(IT_STRIPS);
         assertEquals(IT_Strips_match_count, cells.size());
         System.out.println("Find took " + (System.currentTimeMillis() - start) + " millis");
 
         start = System.currentTimeMillis();
         indexer.getCellsContaining(IT_STRIPS);
         System.out.println("Find took " + (System.currentTimeMillis() - start) + " millis");
 
         cells = indexer.getCellsContaining("1Aug2013");
         assertEquals(4, cells.size());
     }
 
     public void testReindexCells() {
         TableModelIndexer indexer = new TableModelIndexer(testTableModel, 0);
         Collection<MutableTableCell> cells = indexer.getCellsContaining(IT_STRIPS);
         assertEquals(IT_Strips_match_count, cells.size());
 
         //now change the value in 10 of those 128 cells so that it no longer matches, and reindex the cells
         MutableTableCell[] cellArray = cells.toArray(new MutableTableCell[IT_Strips_match_count]);
         String newValue = "wibble";
         for ( int loop=0; loop < 10; loop ++ ) {
             testTableModel.setValueAt(newValue, cellArray[loop].getRow(), cellArray[loop].getCol());
             indexer.reIndexCell(cellArray[loop].getRow(), cellArray[loop].getCol());
 
             //note that after reindexing, the value stored in the MutableTableCell instance has been updated to reflect the
             //new value in the TableModel. The old value previously cached in MutableTableCell was used during the reIndex
             //operation, to remove the old MutableTableCell references from the index, before reindexing with the new value
             assertEquals(newValue, cellArray[loop].getValue());
         }
 
         cells = indexer.getCellsContaining(IT_STRIPS);
         assertEquals(IT_Strips_match_count - 10, cells.size());
     }
 
     public void testInsertRow() {
         TableModelIndexer indexer = new TableModelIndexer(testTableModel, 1);
         Collection<MutableTableCell> cells = indexer.getCellsContaining(IT_STRIPS);
         assertEquals(IT_Strips_match_count, cells.size());
         int lastMatchingRow = getLastRow(cells);
         assertEquals(IT_Strips_last_row_matching, lastMatchingRow);
 
         //now copy a row which matched the query and insert it near the start
         ArrayList<Object> newRow = (ArrayList<Object>)testTableModel.getRow(lastMatchingRow).clone();
         testTableModel.insertRow(10, newRow);
         indexer.insertRows(10, 10);
         testRowIndexes(indexer);
 
         cells = indexer.getCellsContaining(IT_STRIPS);
         //there should now be one more cell which matches since we copied a row with a match
         assertEquals(IT_Strips_match_count + 1, cells.size());
 
         //the last matching row should now be shifted down one by the insert
         lastMatchingRow = getLastRow(cells);
         assertEquals(IT_Strips_last_row_matching + 1, lastMatchingRow);
     }
 
     public void testInsertRows() {
         TableModelIndexer indexer = new TableModelIndexer(testTableModel, 2);
         Collection<MutableTableCell> cells = indexer.getCellsContaining(IT_STRIPS);
         assertEquals(IT_Strips_match_count, cells.size());
         int lastMatchingRow = getLastRow(cells);
         assertEquals(IT_Strips_last_row_matching, lastMatchingRow);
 
         //now copy a row which matched the query and insert it near the start
         ArrayList<Object> newRow = (ArrayList<Object>)testTableModel.getRow(lastMatchingRow).clone();
         ArrayList<Object> newRow2 = (ArrayList<Object>)testTableModel.getRow(lastMatchingRow).clone();
 
         testTableModel.insertRow(10, newRow);
         testTableModel.insertRow(11, newRow2);
         indexer.insertRows(10, 11);
         testRowIndexes(indexer);
 
         cells = indexer.getCellsContaining(IT_STRIPS);
         //there should now be one more cell which matches since we copied a row with a match
         assertEquals(IT_Strips_match_count + 2, cells.size());
 
         //the last matching row should now be shifted down one by the insert
         lastMatchingRow = getLastRow(cells);
         assertEquals(IT_Strips_last_row_matching + 2, lastMatchingRow);
     }
 
     /**
      * The indexer is like java.util.ArrayList in the way it manages its capacity
      * the available capacity is maintained usually in excess of the actual required size.
      * When enough inserts take place to exceed current capacity the internal arrays are resized to a new capacity
      * This is more efficient than resizing the arrays on each insert
      */
     public void testEnsureCapacity() {
         FixtureTableModel tableModel = readTableModel("/testEnsureCapacity.csv");
         TableModelIndexer indexer = new TableModelIndexer(tableModel, 1);
         assertEquals(2, indexer.tableCells.length);
 
         tableModel.insertRows(0,
                 (ArrayList<Object>)tableModel.getRow(0).clone(),
                 (ArrayList<Object>)tableModel.getRow(0).clone()
         );
         indexer.insertRows(0, 1);
        assertEquals(4, indexer.tableCells.length);
 
         tableModel.insertRows(0,
                 (ArrayList<Object>)tableModel.getRow(0).clone(),
                 (ArrayList<Object>)tableModel.getRow(0).clone()
         );
         indexer.insertRows(0, 1);
        assertEquals(7, indexer.tableCells.length);
 
         tableModel.insertRows(0,
                 (ArrayList<Object>)tableModel.getRow(0).clone(),
                 (ArrayList<Object>)tableModel.getRow(0).clone(),
                 (ArrayList<Object>)tableModel.getRow(0).clone()
         );
         indexer.insertRows(0, 2);
        assertEquals(11, indexer.tableCells.length);
 
         for (int loop=0; loop < 20; loop++ ) {
             tableModel.insertRows(0, (ArrayList<Object>)tableModel.getRow(0).clone()
             );
         }
         //the table is growing to more than ((capacity*3)/2)+1 so the next capacity is the total new rows
         indexer.insertRows(0, 19);
        assertEquals(28, indexer.tableCells.length);
 
          tableModel.insertRows(0,
                 (ArrayList<Object>)tableModel.getRow(0).clone()
         );
         indexer.insertRows(0, 0);
         assertEquals(43, indexer.tableCells.length);
     }
 
     public void testRemoveRow() {
         TableModelIndexer indexer = new TableModelIndexer(testTableModel, 1);
         Collection<MutableTableCell> cells = indexer.getCellsContaining(IT_STRIPS);
         assertEquals(IT_Strips_match_count, cells.size());
         int firstMatchingRow = getFirstRow(cells);
         int lastMatchingRow = getLastRow(cells);
         assertEquals(IT_Strips_first_row_matching, firstMatchingRow);
         assertEquals(IT_Strips_last_row_matching, lastMatchingRow);
 
         testTableModel.removeRows(IT_Strips_first_row_matching, IT_Strips_first_row_matching);
         indexer.removeRows(IT_Strips_first_row_matching, IT_Strips_first_row_matching);
         testRowIndexes(indexer);
 
         cells = indexer.getCellsContaining(IT_STRIPS);
         assertEquals(IT_Strips_match_count - 1, cells.size());
 
         //the last matching row should now be shifted down one by the insert
         lastMatchingRow = getLastRow(cells);
         assertEquals(IT_Strips_last_row_matching - 1, lastMatchingRow);
     }
 
      public void testRemoveRows() {
         TableModelIndexer indexer = new TableModelIndexer(testTableModel, 1);
         Collection<MutableTableCell> cells = indexer.getCellsContaining(IT_STRIPS);
         assertEquals(IT_Strips_match_count, cells.size());
         int firstMatchingRow = getFirstRow(cells);
         int lastMatchingRow = getLastRow(cells);
         assertEquals(IT_Strips_first_row_matching, firstMatchingRow);
         assertEquals(IT_Strips_last_row_matching, lastMatchingRow);
 
         assertNotNull(indexer.tableCells[453][0]);
         assertNotNull(indexer.tableCells[452][0]);
         testTableModel.removeRows(IT_Strips_first_row_matching, IT_Strips_first_row_matching + 1);
         indexer.removeRows(IT_Strips_first_row_matching, IT_Strips_first_row_matching + 1);
         assertNull(indexer.tableCells[453][0]);
         assertNull(indexer.tableCells[452][0]); //ensure references to cells are cleared when array reduces
         testRowIndexes(indexer);
 
         cells = indexer.getCellsContaining(IT_STRIPS);
         assertEquals(IT_Strips_match_count - 1, cells.size()); //one of the two rows removed did not match
 
         //the last matching row should now be shifted down one by the insert
         lastMatchingRow = getLastRow(cells);
         assertEquals(IT_Strips_last_row_matching-2, lastMatchingRow);
     }
 
     public void testExcludeFromFilterFormatterByColumnName() {
         TableModelIndexer indexer = new TableModelIndexer(testTableModel, 0);
 
         indexer.setFormatter(FilterFormatter.EXCLUDE_FROM_FILTER_INDEX, "2");
         Collection<MutableTableCell> cells = indexer.getCellsContaining(IT_STRIPS);
         assertEquals(0, cells.size());
         clearFormattersAndCheckRowCount(indexer);
     }
 
     public void testExcludeFromFilterFormatterByColumnIndex() {
         TableModelIndexer indexer = new TableModelIndexer(testTableModel, 1);
 
         indexer.setFormatter(FilterFormatter.EXCLUDE_FROM_FILTER_INDEX, 2);
         Collection<MutableTableCell> cells = indexer.getCellsContaining(IT_STRIPS);
         assertEquals(0, cells.size());
         clearFormattersAndCheckRowCount(indexer);
     }
 
     public void testExcludeFromFilterFormatterByColumnClass() {
         TableModelIndexer indexer = new TableModelIndexer(testTableModel, 1);
 
         indexer.setFormatter(FilterFormatter.EXCLUDE_FROM_FILTER_INDEX, String.class);
         Collection<MutableTableCell> cells = indexer.getCellsContaining(IT_STRIPS);
         assertEquals(0, cells.size());
         clearFormattersAndCheckRowCount(indexer);
     }
 
     public void testIndexTrimmingDoesNotAffectSearch() {
         TableModelIndexer indexer = new TableModelIndexer(testTableModel, 2);
         Collection<MutableTableCell> cells = indexer.getCellsContaining(IT_STRIPS);
         assertEquals(IT_Strips_match_count, cells.size());
 
         indexer.trimIndexToInitialDepth();
 
         cells = indexer.getCellsContaining(IT_STRIPS);
         assertEquals(IT_Strips_match_count, cells.size());
     }
 
     //when the column structure is changed it renders the formatters by index
     //meaningless so they should get cleared down
     public void testFormattersByIndexClearedWhenTableStructureChanged() {
         TableModelIndexer indexer = new TableModelIndexer(testTableModel, 1);
 
         indexer.setFormatter(FilterFormatter.EXCLUDE_FROM_FILTER_INDEX, 2);
         Collection<MutableTableCell> cells = indexer.getCellsContaining(IT_STRIPS);
         assertEquals(0, cells.size());
 
         indexer.tableStructureChanged();
         cells = indexer.getCellsContaining(IT_STRIPS);
         assertEquals(IT_Strips_match_count, cells.size());
 
         indexer.setFormatter(FilterFormatter.EXCLUDE_FROM_FILTER_INDEX, 2);
         cells = indexer.getCellsContaining(IT_STRIPS);
         assertEquals(0, cells.size());
 
         indexer.setTableModel(testTableModel);
         cells = indexer.getCellsContaining(IT_STRIPS);
         assertEquals(IT_Strips_match_count, cells.size());
     }
 
     public void testFormattersByNameAndClassNotClearedWhenTableStructureChanged() {
         TableModelIndexer indexer = new TableModelIndexer(testTableModel, 1);
 
         indexer.setFormatter(FilterFormatter.EXCLUDE_FROM_FILTER_INDEX, "2");
         indexer.setFormatter(FilterFormatter.EXCLUDE_FROM_FILTER_INDEX, String.class);
         Collection<MutableTableCell> cells = indexer.getCellsContaining(IT_STRIPS);
         assertEquals(0, cells.size());
 
         indexer.tableStructureChanged();
         indexer.setTableModel(testTableModel);
         cells = indexer.getCellsContaining(IT_STRIPS);
         assertEquals(0, cells.size());
     }
 
     public void testSetOfRowIndexesInstanceNotChangedIfUpdateDoesNotAffectFilteredView() {
         FixtureTableModel tableModel = readTableModel("/filteredTableTestData.csv");
         TableModelIndexer indexer = new TableModelIndexer(tableModel, 1);
         Map<MutableRowIndex, TreeSet<Integer>> tableCollsByRow = indexer.getCellsContaining("string4").getRowColumnMap();
         tableModel.setValueAt("wibble", 8, 1);
         indexer.reIndexCell(8, 1);
         assertTrue(tableCollsByRow == indexer.getCellsContaining("string4").getRowColumnMap());
     }
 
     public void testSetOfRowIndexesInstanceChangedIfUpdateCausesNewRowToPassFilters() {
         FixtureTableModel tableModel = readTableModel("/filteredTableTestData.csv");
         TableModelIndexer indexer = new TableModelIndexer(tableModel, 1);
         Map<MutableRowIndex, TreeSet<Integer>> colsByRow = indexer.getCellsContaining("string4").getRowColumnMap();
 
         tableModel.setValueAt("string4", 8, 1);
         indexer.reIndexCell(8, 1);
         assertFalse(colsByRow == indexer.getCellsContaining("string4").getRowColumnMap());
     }
 
      public void testSetOfRowIndexesInstanceChangedIfUpdateCausesOldRowToFailFilters() {
         FixtureTableModel tableModel = readTableModel("/filteredTableTestData.csv");
         TableModelIndexer indexer = new TableModelIndexer(tableModel, 1);
          Map<MutableRowIndex, TreeSet<Integer>> colsByRow = indexer.getCellsContaining("string4").getRowColumnMap();
         tableModel.setValueAt("string5", 9, 1);
         indexer.reIndexCell(9, 1);
         assertFalse(colsByRow == indexer.getCellsContaining("string4").getRowColumnMap());
     }
 
     public void testSetOfRowIndexesInstanceUnChangedButMutableRowIndexValuesChangedOnInsertOfNewRowWhichFailsFilters() {
         FixtureTableModel tableModel = readTableModel("/filteredTableTestData.csv");
         TableModelIndexer indexer = new TableModelIndexer(tableModel, 1);
          Map<MutableRowIndex, TreeSet<Integer>> colsByRow = indexer.getCellsContaining("string4").getRowColumnMap();
         Set<Integer> rowsIndexesInOldFilterView = getRowSet(colsByRow);
         tableModel.insertRow(1, new ArrayList<Object>(Arrays.asList(new String[] {"wibble", "wibble"})));
         indexer.insertRows(1,1);
          Map<MutableRowIndex, TreeSet<Integer>> newColsByRow = indexer.getCellsContaining("string4").getRowColumnMap();
         Set<Integer> rowsIndexesInNewFilterView = getRowSet(newColsByRow);
         assertTrue(colsByRow == newColsByRow);
         assertTrue(rowsIndexesInOldFilterView.contains(9));
         assertTrue(rowsIndexesInOldFilterView.contains(11));
         assertTrue(rowsIndexesInNewFilterView.contains(10));
         assertTrue(rowsIndexesInNewFilterView.contains(12));
     }
 
     public void testSetOfRowIndexesInstanceUnChangedButMutableRowIndexValuesChangedOnDeleteOfNewRowWhichFailsFilters() {
         FixtureTableModel tableModel = readTableModel("/filteredTableTestData.csv");
         TableModelIndexer indexer = new TableModelIndexer(tableModel, 1);
          Map<MutableRowIndex, TreeSet<Integer>> colsByRow = indexer.getCellsContaining("string4").getRowColumnMap();
         Set<Integer> rowsIndexesInOldFilterView = getRowSet(colsByRow);
         tableModel.removeRow(1); tableModel.removeRow(2);
         indexer.removeRows(1,2);
          Map<MutableRowIndex, TreeSet<Integer>> newColsByRow = indexer.getCellsContaining("string4").getRowColumnMap();
         Set<Integer> rowsIndexesInNewFilterView = getRowSet(newColsByRow);
         assertTrue(colsByRow == newColsByRow);
         assertTrue(rowsIndexesInOldFilterView.contains(9));
         assertTrue(rowsIndexesInOldFilterView.contains(11));
         assertTrue(rowsIndexesInNewFilterView.contains(7));
         assertTrue(rowsIndexesInNewFilterView.contains(9));
     }
 
      public void testSetOfRowIndexesInstanceChangedAndMutableRowIndexValuesChangedOnDeleteOfRowWhichUsedToPassFilters() {
         FixtureTableModel tableModel = readTableModel("/filteredTableTestData.csv");
         TableModelIndexer indexer = new TableModelIndexer(tableModel, 1);
         Map<MutableRowIndex, TreeSet<Integer>> colsByRow = indexer.getCellsContaining("string4").getRowColumnMap();
         Set<Integer> rowsIndexesInOldFilterView = getRowSet(colsByRow);
         tableModel.removeRow(9); tableModel.removeRow(9);
         indexer.removeRows(9,10);
         Map<MutableRowIndex, TreeSet<Integer>> newColsByRow = indexer.getCellsContaining("string4").getRowColumnMap();
         Set<Integer> rowsIndexesInNewFilterView = getRowSet(newColsByRow);
         assertTrue(colsByRow != newColsByRow);
         assertTrue(rowsIndexesInOldFilterView.contains(9));
         assertTrue(rowsIndexesInOldFilterView.contains(11));
         assertEquals(1, rowsIndexesInNewFilterView.size());
         assertTrue(rowsIndexesInNewFilterView.contains(9));
     }
 
     private Set<Integer> getRowSet(Map<MutableRowIndex, TreeSet<Integer>> tableRows) {
         HashSet<Integer> rows = new HashSet<Integer>();
         for (MutableRowIndex i : tableRows.keySet() ) {
             rows.add(i.index);
         }
         return rows;
     }
 
 
     private void clearFormattersAndCheckRowCount(TableModelIndexer indexer) {
         Collection<MutableTableCell> cells;
         indexer.clearFormatters();
         cells = indexer.getCellsContaining(IT_STRIPS);
         assertEquals(IT_Strips_match_count, cells.size());
     }
 
     /**
      * test that after each operation we end up with the correct row indexes in the table cells array
      */
     private void testRowIndexes(TableModelIndexer tableModelIndexer) {
         for ( int loop=0; loop < tableModelIndexer.size; loop++) {
             assertEquals(loop, tableModelIndexer.tableCells[loop][0].getRow());
         }
     }
 
 }
