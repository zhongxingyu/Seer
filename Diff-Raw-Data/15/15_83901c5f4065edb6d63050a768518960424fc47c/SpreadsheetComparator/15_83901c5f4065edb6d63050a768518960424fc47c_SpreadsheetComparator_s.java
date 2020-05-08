 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 
 package org.amanzi.splash.compare;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import org.amanzi.neo.core.database.nodes.CellNode;
 import org.amanzi.neo.core.database.nodes.SpreadsheetNode;
 import org.amanzi.neo.core.enums.SplashRelationshipTypes;
 import org.amanzi.neo.core.utils.Pair;
 import org.amanzi.splash.swing.Cell;
 import org.amanzi.splash.utilities.SpreadsheetCreator;
 import org.eclipse.core.runtime.IPath;
 
 import com.eteks.openjeks.format.CellFormat;
 import com.lowagie.text.Font;
 
 /**
  * Class that creates a Delta Report for Spreadsheets
  * 
  * @author Lagutko_N
  * @since 1.0.0
  */
 public class SpreadsheetComparator extends SpreadsheetCreator {
     
     /**
      * Class that maps name of Header and Node in Line
      * 
      * @author Lagutko_N
      * @since 1.0.0
      */
     private class SpreadsheetLine extends HashMap<Object, CellNode> {
         
         /** long serialVersionUID field */
         private static final long serialVersionUID = 1754276359754781870L;
         
         /*
          * Number of Line 
          */
         private int lineNumber;
         
         /**
          * Creates a SpreadsheetLine
          * 
          * @param rowHeader Row Header for this Line
          * @param columnNames name of Columns
          */
         public SpreadsheetLine(CellNode rowHeader, HashMap<Integer, Object> columnNames) {
             lineNumber = rowHeader.getCellRow();
             
             for (CellNode cell : rowHeader.getAllCellsFromThis(SplashRelationshipTypes.NEXT_CELL_IN_ROW, false)) {
                 put(columnNames.get(cell.getCellColumn()), cell);
             }
         }
         
         /**
          * Returns number of Line
          *
          * @return
          */
         public Integer getLineNumber() {
             return lineNumber;
         }
     }
     
     /**
      * Iterator for Lines in Spreadsheet
      * 
      * @author Lagutko_N
      * @since 1.0.0
      */
     private class SpreadsheetLineIterator implements Iterator<SpreadsheetLine> {
         
         /*
          * Current Node in Column
          */
         private CellNode currentHeader;
         
         /*
          * Names of Column Headers 
          */
         private HashMap<Integer, Object> headerNames = new HashMap<Integer, Object>();
         
         /**
          * Creates a Iterator
          * 
          * @param spreadsheet Spreadsheet
          */
         public SpreadsheetLineIterator(SpreadsheetNode spreadsheet) {
             currentHeader = spreadsheet.getRowHeader(1);
             
             for (CellNode headerCells : currentHeader.getAllCellsFromThis(SplashRelationshipTypes.NEXT_CELL_IN_ROW, false)) {
                 headerNames.put(headerCells.getCellColumn(), headerCells.getValue());
             }            
         }
 
         @Override
         public boolean hasNext() {
             if (currentHeader == null) {
                 return false;
             }
             return currentHeader.getNextCellInColumn() != null;
         }
 
         @Override
         public SpreadsheetLine next() {
            currentHeader = currentHeader.getNextCellInColumn();
             if (currentHeader == null) {
                 return null;
             }
             return new SpreadsheetLine(currentHeader, headerNames);
         }
 
         @Override
         public void remove() {
         }        
     }
     
     /*
      * Node of First Spreadsheet
      */
     private SpreadsheetNode firstSpreadsheet;
     
     /*
      * Node of Second Spreadsheet 
      */
     private SpreadsheetNode secondSpreadsheet;
     
     /*
      * CellFormat with Green background
      */
     private CellFormat greenCell;
     
     /* 
      * Cell format with Yellow backgound
      */
     private CellFormat yellowCell;
     
     /*
      * Cell Format for Blue background
      */
     private CellFormat blueCell;
     
     /*
      * Cell Format with Bold font
      */
     private CellFormat boldFormat;
     
     /*
      * Cell Format with grey background
      */
     private CellFormat greyCell;
     
     /*
      * Indexes for new Columns
      */
     private HashMap<String, Integer> newColumnIndexes = new HashMap<String, Integer>();
     
     /*
      * Number of changes per each column
      */
     private HashMap<Integer, Integer> changesPerColumn = new HashMap<Integer, Integer>();
 
     /**
      * Creates Comparator
      * 
      * @param containerPath path to container that will contain new Spreadsheet
      * @param newSpreadsheetName name of new Spreadsheet
      * @param firstSpreadsheet node of first Spreadsheet to compare
      * @param secondSpreadsheet node of second Spreadsheet to compare
      */
     public SpreadsheetComparator(IPath containerPath, String newSpreadsheetName, SpreadsheetNode firstSpreadsheet, SpreadsheetNode secondSpreadsheet) {
         super(containerPath, newSpreadsheetName);
         
         this.firstSpreadsheet = firstSpreadsheet;
         this.secondSpreadsheet = secondSpreadsheet;
         
         greenCell = new CellFormat();
         greenCell.setBackgroundColor(new Color(0.0f, 1.0f, 0.0f));
         
         blueCell = new CellFormat();
         blueCell.setBackgroundColor(new Color(0.0f, 0.75f, 1.0f));   
         
         yellowCell = new CellFormat();
         yellowCell.setBackgroundColor(new Color(1.0f, 1.0f, 0.0f));
         
         boldFormat = new CellFormat();
         boldFormat.setFontStyle(Font.BOLD);
         
         greyCell = new CellFormat();
         greyCell.setBackgroundColor(new Color(0.8f, 0.8f, 0.8f));
     }
     
     /**
      * Starts comparing for spreadsheets
      */
     public void compare() {
         SpreadsheetLineIterator firstIterator = new SpreadsheetLineIterator(firstSpreadsheet);
         SpreadsheetLineIterator secondIterator = new SpreadsheetLineIterator(secondSpreadsheet);
         
         boolean first = true;
         int rowNumber = 3;
         while (firstIterator.hasNext()) {
             SpreadsheetLine firstSheetLine = firstIterator.next();
             SpreadsheetLine secondSheetLine = secondIterator.next();
             if (first) {
                 //if it's a first iteration than we should add Column Headers
                 addHeaders(firstSheetLine, secondSheetLine, rowNumber);
                 first = false;
                 rowNumber++;
                 rowNumber++;
             }
             
             if ((secondSheetLine == null) || (firstSheetLine.getLineNumber() > secondSheetLine.getLineNumber())) {
                 //if line didn't exists in second spreadsheet, than mark it with blue
                 addSpreadsheetLine(firstSheetLine, rowNumber, blueCell);             
             }
             else if (firstSheetLine.getLineNumber() < secondSheetLine.getLineNumber()) {
                 //if line didn't exists in first spreadsheet, than mark it with green
                 addSpreadsheetLine(secondSheetLine, rowNumber, greenCell);
             }
             else {
                 //if line exists in both sheets than compare it's cells
                 compareSpreadsheetLines(firstSheetLine, secondSheetLine, rowNumber);
             }
             rowNumber++;
         }
         
         //add all lines that exists in second but not first spreadsheet
         while (secondIterator.hasNext()) {
             addSpreadsheetLine(secondIterator.next(), rowNumber, blueCell);
         }
         
         //add information about comparision
         addDeltaInformation();
     }
     
     /**
      * Adds Headers of Columns
      *
      * @param firstLine Line in First Spreadsheet
      * @param secondLine Line in Second Spreadsheet
      * @param rowNumber number of Row
      */
     private void addHeaders(SpreadsheetLine firstLine, SpreadsheetLine secondLine, int rowNumber) {
         int column = 0;
         SpreadsheetLine line = (SpreadsheetLine)secondLine.clone();
         //add all Columns that exists in first Spreadsheet
         for (Object header : firstLine.keySet()) {
            Cell cell = new Cell(rowNumber, column++);
             if (!line.containsKey(header)) {
                 cell.setCellFormat(blueCell);
             }
             else {
                 line.remove(header);
            }
             
             cell.setValue(header.toString());
             cell.setDefinition(header.toString());
             
             saveCell(cell, null, rowNumber);
         }
         
         //add Column that exists only in second sheet
         for (Object header : line.keySet()) {
             Cell cell = new Cell(rowNumber, column);
             
             newColumnIndexes.put(header.toString(), column++);
             
             cell.setValue(header.toString());
             cell.setDefinition(header.toString());
             
             saveCell(cell, greenCell, rowNumber);
         }        
     }
     
     /**
      * Adds a simple Spreadsheet line without comparing
      *
      * @param line line to add
      * @param rowNumber number of Row
      * @param format format of cells in line
      */
     private void addSpreadsheetLine(SpreadsheetLine line, int rowNumber, CellFormat format) {
         for (CellNode cell : line.values()) {
             Cell cellToSave = spreadsheetService.convertNodeToCell(cell, null, null);
             
             saveCell(cellToSave, format, rowNumber);
             
             addChange(cellToSave.getColumn());
         }
     }
     
     /**
      * Compares two Spreadsheet Lines
      *
      * @param firstLine first Line to compare
      * @param secondLine second Line to compare
      * @param rowNumber number of row
      */
    public void compareSpreadsheetLines(SpreadsheetLine firstLine, SpreadsheetLine secondLine, int rowNumber) {
         ArrayList<Cell> cellsToSave = new ArrayList<Cell>();
         boolean needToSave = false;
         //iterate through all cells in first line
         for (Object columnName : firstLine.keySet()) {
             if (secondLine.containsKey(columnName)) {
                 //if both lines contains this column name than compare cells
                 Pair<Cell, Boolean> compareResult = compareCells(firstLine.get(columnName), secondLine.get(columnName));
                 needToSave = needToSave || compareResult.getRight();
                 cellsToSave.add(compareResult.getLeft());
                 
                 secondLine.remove(columnName);
             }
             else {
                 //otherwise save a single Cell as removed
                 Cell cellToSave = spreadsheetService.convertNodeToCell(firstLine.get(columnName), null, null);
                 saveCell(cellToSave, blueCell, rowNumber);
                 addChange(cellToSave.getColumn());
                 
                 needToSave = true;
             }
         }
         
         //add all cells that exists only in second line
         int lastIndex = firstLine.keySet().size();
         for (CellNode node : secondLine.values()) {
             Integer columnIndex = 0;
             Cell cellToSave = spreadsheetService.convertNodeToCell(node, null, null);
             
             columnIndex = newColumnIndexes.get(cellToSave.getValue());
             if (columnIndex == null) {
                 columnIndex = lastIndex++;
                 newColumnIndexes.put(cellToSave.getValue().toString(), columnIndex);
             }
             cellToSave.setColumn(columnIndex);
             saveCell(cellToSave, greenCell, rowNumber);
             
             addChange(columnIndex);
             
             needToSave = true;
         }
         
         //if there was changes and line need to be saved than save it
         if (needToSave) {
             for (Cell cell : cellsToSave) {
                 saveCell(cell, null, rowNumber);
             }
         }
     }
     
     /**
      * Adds a single Change
      *
      * @param columnNumber number of column that contains changes Cell
      */
     private void addChange(int columnNumber) {
         Integer changes = changesPerColumn.get(columnNumber);
         if (changes == null) {
             changes = 1;
         }
         else {
             changes++;
         }
         changesPerColumn.put(columnNumber, changes);
     }
     
     /**
      * Compares two Cells
      *
      * @param firstCell first Cell
      * @param secondCell second Cell
      * @return Pair that contains result cell and is there were changes
      */
     private Pair<Cell, Boolean> compareCells(CellNode firstCell, CellNode secondCell) {
         Cell cell1 = spreadsheetService.convertNodeToCell(firstCell, null, null);
         Cell cell2 = spreadsheetService.convertNodeToCell(secondCell, null, null);
         
         Object value1 = cell1.getValue();
         Object value2 = cell2.getValue();
         
         boolean isChanged = false;
         
         if (!value1.equals(value2)) {
             cell1.setValue(value1 + " -> " + value2);
             cell1.setDefinition(cell1.getValue().toString());
             cell1.setCellFormat(yellowCell);
             isChanged = true;
             
             addChange(cell1.getColumn());
         }
         else {
             changesPerColumn.put(cell1.getColumn(), 0);
         }
         
         return new Pair<Cell, Boolean>(cell1, isChanged);
     }
     
     /**
      * Saves a new Cell 
      *
      * @param cellToSave cell to Save
      * @param newFormat format of Cell
      * @param columnNumber number of Column
      * @return node of saved cell
      */
     private CellNode saveCell(Cell cellToSave, CellFormat newFormat, int rowNumber) {
         cellToSave.setRow(rowNumber);
         
         if (newFormat != null) {
             if (cellToSave.getCellFormat() == null) {
                 cellToSave.setCellFormat(newFormat);
             }
             else {
                 cellToSave.getCellFormat().setBackgroundColor(newFormat.getBackgroundColor());
             }
         }
         
         return saveCell(cellToSave);
     }
     
     /**
      * Adds a general information about changes
      */
     private void addDeltaInformation() {
         //title for 'Total number of Changes'
         Cell totalChangesTitle = new Cell(0, 0, "Total Number of Changes", "Total Number of Changes", boldFormat);
         saveCell(totalChangesTitle);
         
         //computes total number of changes
         Cell totalChangesValue = new Cell(0, 1, getChangesCount().toString(), getChangesCount().toString(), null);
         saveCell(totalChangesValue);
         
         //title for 'Summary of Changes'
         Cell summaryTitle = new Cell(2, 0, "Summary of Changes", "Summary of Changes", boldFormat);
         saveCell(summaryTitle);
         
         for (Integer columnIndex : changesPerColumn.keySet()) {
             //adds information about the number of changes Cells to each Column 
             Cell changeNumber = new Cell(4, columnIndex, changesPerColumn.get(columnIndex).toString(), changesPerColumn.get(columnIndex).toString(), greyCell);
             saveCell(changeNumber);
         }
     }
     
     /**
      * Computes total number of changes
      *
      * @return total number of changes
      */
     private Integer getChangesCount() {
         int result = 0;
         for (int changesInColumn : changesPerColumn.values()) {
             result += changesInColumn;
         }
         
         return result;
     }
 }
