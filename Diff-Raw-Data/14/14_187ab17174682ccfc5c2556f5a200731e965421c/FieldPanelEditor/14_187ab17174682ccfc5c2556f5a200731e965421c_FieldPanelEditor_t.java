 /**
  * Copyright (C) 2014 
  * Nicholas J. Little <arealityfarbetween@googlemail.com>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package little.nj.gui.components;
 
import java.awt.Color;
 import java.awt.Component;
 
 import javax.swing.AbstractCellEditor;
 import javax.swing.BorderFactory;
 import javax.swing.JTable;
 import javax.swing.table.TableCellEditor;
 import javax.swing.table.TableCellRenderer;
 
 import little.nj.adts.ByteField;
 
 @SuppressWarnings("serial")
 public class FieldPanelEditor extends AbstractCellEditor
                               implements TableCellEditor, TableCellRenderer {
 
     ByteField value;
 
     @Override
     public Object getCellEditorValue() { return value; }
 
     @Override
     public Component getTableCellEditorComponent(JTable table, Object value,
             boolean isSelected, int row, int column) {
         
         this.value = (ByteField)value;
         
         return FieldPanelFactory.create((ByteField)value);
     }
 
     @Override
     public Component getTableCellRendererComponent(JTable table, Object value,
             boolean isSelected, boolean hasFocus, int row, int column) {
         
         ByteFieldPanel cell = FieldPanelFactory.create((ByteField)value);
         
         if (hasFocus) {
            cell.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
         } else {
             cell.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
         }
             
         if (isSelected) {
             cell.setForeground(table.getSelectionForeground());
             cell.setBackground(table.getSelectionBackground());
         } else {
             cell.setForeground(table.getForeground());
             cell.setBackground(table.getBackground());
         }
         
         return cell;
     }
     
     
 
 }
