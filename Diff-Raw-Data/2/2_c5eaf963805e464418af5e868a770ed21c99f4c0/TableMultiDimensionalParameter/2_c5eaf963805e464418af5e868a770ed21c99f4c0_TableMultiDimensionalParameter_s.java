 package org.pillarone.riskanalytics.core.parameterization;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import static org.pillarone.riskanalytics.core.parameterization.MatrixMultiDimensionalParameter.generateValue;
 
 public class TableMultiDimensionalParameter extends AbstractMultiDimensionalParameter {
 
     protected List titles;
 
     public TableMultiDimensionalParameter(List cellValues, List titles) {
         super(cellValues);
         this.titles = titles;
         if (!valuesConverted && (cellValues.size() != titles.size())) {
             throw new IllegalArgumentException("cell values and titles must be of same dimension but were " +
                     "cell values: " + cellValues.size() +
                     " and titles: " + titles.size());
         }
     }
 
 
     public int getTitleColumnCount() {
         return 0;
     }
 
     public int getTitleRowCount() {
         return 1;
     }
 
     public Object getValueAt(int row, int column) {
         if (row == 0) {
             if (column == 0) return "";
             return titles.get(column - 1).toString();
         } else {
             return super.getValueAt(row - 1, column - 1);
         }
     }
 
     public Object getValueAtOrValueOfLastRow(int row, int column) {
         return getValueAt(Math.min(row, getRowCount() - 1), column);
     }
 
     public void setValueAt(Object value, int rowIndex, int columnIndex) {
         if (rowIndex > 0 && columnIndex > 0) {
             super.setValueAt(value, rowIndex - 1, columnIndex - 1);
         }
     }
 
     public void addColumnAt(int columnIndex) {
         List emptyList = new ArrayList();
         for (int i = 1; i < getRowCount(); i++) {
             emptyList.add(generateValue(values.get(columnIndex), i - 1));
         }
         if (columnIndex >= values.size()) {
             values.add(emptyList);
             titles.add("new title");
         } else {
             values.add(columnIndex, emptyList);
             titles.add(columnIndex, "new title");
         }
     }
 
     public void removeColumnAt(int columnIndex) {
         values.remove(columnIndex);
         titles.remove(columnIndex);
     }
 
     public void addRowAt(int rowIndex) {
         for (List list : values) {
             if (rowIndex >= list.size())
                 list.add(generateValue(list, list.size() - 1));
             else
                 list.add(rowIndex, generateValue(list, rowIndex));
         }
     }
 
     public void removeRowAt(int rowIndex) {
         for (List list : values) {
             list.remove(rowIndex);
         }
     }
 
     public void moveColumnTo(int from, int to) {
         super.moveColumnTo(from, to);
         Collections.swap(titles, from, to);
     }
 
     protected void rowsAdded(int i) {
 
     }
 
     protected void columnsAdded(int i) {
         for (int j = 0; j < i; j++) {
             titles.add("New title");
         }
     }
 
     protected void rowsRemoved(int i) {
     }
 
     protected void columnsRemoved(int i) {
         for (int j = 0; j < i; j++) {
             titles.remove(titles.size() - 1);
         }
     }
 
     public boolean isCellEditable(int row, int column) {
         return column != 0;
     }
 
     protected void appendAdditionalConstructorArguments(StringBuffer buffer) {
         buffer.append(",");
         appendList(buffer, titles);
     }
 
     public List getColumnByName(String name) {
         int index = titles.indexOf(name);
         if (index < 0) {
             throw new IllegalArgumentException("Column " + name + " not found");
         }
         return getColumn(index);
     }
 
     public List getColumn(int index) {
         return values.get(index);
     }
 
     public List getColumnNames() {
         return titles;
     }
 
     public int getColumnIndex(String name) {
        return titles.indexOf(name);
     }
 
     public boolean columnCountChangeable() {
         return false;
     }
 
     public boolean rowCountChangeable() {
         return true;
     }
 }
