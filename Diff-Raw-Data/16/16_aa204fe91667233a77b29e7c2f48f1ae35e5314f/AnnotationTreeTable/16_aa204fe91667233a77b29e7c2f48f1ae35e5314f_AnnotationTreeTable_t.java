 /*
  * Hex - a hex viewer and annotator
  * Copyright (C) 2009-2010  Trejkaz, Hex Project
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
 
 package org.trypticon.hex.anno.swing;
 
 import org.jdesktop.swingx.JXTable;
 import org.jdesktop.swingx.JXTreeTable;
 import org.jdesktop.swingx.table.ColumnFactory;
 import org.jdesktop.swingx.table.TableColumnExt;
 
 /**
  * Customisations for the annotation tree table.
  *
  * @author trejkaz
  */
 public class AnnotationTreeTable extends JXTreeTable {
     public AnnotationTreeTable() {
        // XXX: This doesn't actually work due to a "feature" in Swing.  I originally thought it was a bug in SwingX:
         // https://swingx.dev.java.net/issues/show_bug.cgi?id=1289
        //setAutoResizeMode(AUTO_RESIZE_LAST_COLUMN);

        setAutoResizeMode(AUTO_RESIZE_OFF);

        // This method is misleadingly named but it makes the table fill the available width automatically
        // if the viewport width is greater than the table width.
        setHorizontalScrollEnabled(true);
 
         setColumnFactory(new ColumnFactory() {
             @Override
             public void configureColumnWidths(JXTable table, TableColumnExt columnExt) {
                 super.configureColumnWidths(table, columnExt);
 
                 switch (columnExt.getModelIndex()) {
                     case AnnotationTreeTableModel.TYPE_COLUMN:
                     case AnnotationTreeTableModel.VALUE_COLUMN:
                         columnExt.setPreferredWidth(100);

                        //HACK: Setting this appears to solve the problem where these columns will resize when the
                        //   containing component resizes.
                        columnExt.setMaxWidth(Short.MAX_VALUE);
                         break;
                     case AnnotationTreeTableModel.NOTE_COLUMN:
                         columnExt.setPreferredWidth(200);
                 }
             }
         });
     }
 }
