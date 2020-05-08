 /*
  * Copyright (C) 2011 Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package fr.jamgotchian.abcd.core.util;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>
  */
 public class TablePrinter {
 
     private final List<List<String>> columns = new ArrayList<List<String>>();
 
     TablePrinter(String... columnNames) {
         for (String columnName : columnNames) {
             List<String> column = new ArrayList<String>(1);
             column.add(columnName);
             columns.add(column);
         }
     }
 
     public void addRow(Object... values) {
         if (values.length != columns.size()) {
             throw new IllegalArgumentException("Wrong number of column");
         }
         for (int c = 0; c < values.length; c++) {
             Object value = values[c];
            columns.get(c).add(value == null ? "" : value.toString());
         }
     }
 
     public void print(StringBuilder out) {
         List<Integer> columnWidths = new ArrayList<Integer>(columns.size());
         for (List<String> column : columns) {
             columnWidths.add(getColumnWidth(column));
         }
 
         printSeparator(out, columnWidths);
         out.append("\n");
         int rowCount = columns.get(0).size();
         for (int r = 0; r < rowCount; r++) {
             out.append("|");
             for (int c = 0; c < columns.size(); c++) {
                 String format = " %1$-" + columnWidths.get(c) + "s |";
                 out.append(String.format(format, columns.get(c).get(r)));
             }
             out.append("\n");
             if (r == 0) {
                 printSeparator(out, columnWidths);
                 out.append("\n");
             }
         }
         printSeparator(out, columnWidths);
     }
 
     private int getColumnWidth(List<String> column) {
         int max = Integer.MIN_VALUE;
         for (String s : column) {
             int length = (s != null ? s.length() : 0);
             if (length > max) {
                 max = s.length();
             }
         }
         return max;
     }
 
     private void printSeparator(StringBuilder builder, List<Integer> columnWidths) {
         builder.append("+");
         for (int columnWidth : columnWidths) {
             for (int i = 0; i < columnWidth+1; i++) {
                 builder.append("-");
             }
             builder.append("-+");
         }
     }
 
     @Override
     public String toString() {
         StringBuilder builder = new StringBuilder();
         print(builder);
         return builder.toString();
     }
 }
