 /*
  * Copyright (C) 2012 Gyver
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
 package com.gyver.matrixmover.gui.component;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.List;
 import javax.swing.table.AbstractTableModel;
 
 /**
  *
  * @author Gyver
  */
 public class ColorMapTableModel extends AbstractTableModel {
 
     private ArrayList<Color> colorMap = null;
     private String[] columnNames = {"Red", "Green", "Blue"};
 
     public ColorMapTableModel(List<Color> colorMap) {
         this.colorMap = new ArrayList<Color>();
         this.colorMap.addAll(colorMap);
     }
 
     @Override
     public int getRowCount() {
         return colorMap.size();
     }
 
     @Override
     public int getColumnCount() {
         return 3;
     }
 
     @Override
     public Object getValueAt(int row, int color) {
         if (color == 0) {
             return colorMap.get(row).getRed();
         } else if (color == 1) {
             return colorMap.get(row).getGreen();
         } else if (color == 2) {
             return colorMap.get(row).getBlue();
         } else {
             return null;
         }
     }
 
     @Override
     public String getColumnName(int col) {
         return columnNames[col];
     }
 
     @Override
     public boolean isCellEditable(int row, int col) {
         return true;
     }
 
     @Override
     public void setValueAt(Object value, int row, int color) {
         int red = colorMap.get(row).getRed();
         int green = colorMap.get(row).getGreen();
         int blue = colorMap.get(row).getBlue();
         
         int newValue = 0;
         
         try{
             newValue = Integer.parseInt((String)value);
         } catch(NumberFormatException e){
             newValue = 0;
         }
         
         if(newValue > 255){
             newValue = 255;
         } else if(newValue < 0){
             newValue = 0;
         }
         
         if (color == 0) {
             colorMap.set(row, new Color(newValue, green, blue));
         } else if (color == 1) {
             colorMap.set(row, new Color(red, newValue, blue));
         } else if (color == 2) {
             colorMap.set(row, new Color(red, green, newValue));
         } 
         fireTableCellUpdated(row, color);
     }
 
     public List<Color> getColorMap() {
        if(colorMap.size() <= 0){
            addRow();
        }
         return colorMap;
     }
 
     public void addRow() {
         colorMap.add(new Color(0));
         fireTableDataChanged();
     }
 
     public void removeRow() {
         colorMap.remove(colorMap.size() - 1);
         fireTableDataChanged();
     }
 }
