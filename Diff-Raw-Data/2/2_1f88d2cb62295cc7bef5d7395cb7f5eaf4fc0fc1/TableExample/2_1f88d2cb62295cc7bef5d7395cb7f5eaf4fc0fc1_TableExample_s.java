 /*
  * $Id$
  * Copyright 2000,2005 wingS development team.
  *
  * This file is part of wingS (http://www.j-wings.org).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 package wingset;
 
 import org.wings.*;
 import org.wings.style.CSSProperty;
 import org.wings.style.CSSStyleSheet;
 import org.wings.event.SMouseEvent;
 import org.wings.event.SMouseListener;
 import org.wings.plaf.css.TableCG;
 import org.wings.table.*;
 
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.TableModel;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.*;
 
 /**
  * @author <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
  * @version $Revision$
  */
 public class TableExample
         extends WingSetPane {
     static final SIcon image = new SResourceIcon("org/wings/icons/JavaCup.gif");
 
     public final MyCellRenderer cellRenderer = new MyCellRenderer();
 
     final static Color[] colors = {
             Color.black,
             Color.cyan,
             Color.yellow,
             Color.magenta,
             Color.orange,
             Color.pink,
             Color.red,
             Color.darkGray,
             Color.gray,
             Color.green,
             Color.lightGray,
             Color.white,
             Color.blue
     };
 
     private STable table;
     private SLabel clicks = new SLabel();
     private TableControls controls;
     private boolean consume = false;
 
     public SComponent createExample() {
         controls = new TableControls();
 
         table = new STable(new MyTableModel(7, 5));
        table.setName("table");
         table.setShowGrid(true);
         table.setSelectionMode(STable.NO_SELECTION);
         table.setDefaultRenderer(cellRenderer);
         table.setShowAsFormComponent(false);
         table.setEditable(false);
         controls.addControllable(table);
 
         table.getColumnModel().getColumn(0).setWidth("200px");
         table.getColumnModel().getColumn(1).setWidth("*");
         table.getColumnModel().getColumn(2).setWidth("100px");
         table.getColumnModel().getColumn(3).setWidth("50px");
         table.getColumnModel().getColumn(4).setWidth("50px");
         table.getColumnModel().getColumn(5).setWidth("50px");
         table.getColumnModel().getColumn(6).setWidth("100px");
 
         table.addMouseListener(new SMouseListener() {
             public void mouseClicked(SMouseEvent e) {
                 if (consume && table.getColumnForLocation(e.getPoint()) == 1)
                     e.consume();
                 clicks.setText("clicked " + e.getPoint().getCoordinates());
             }
         });
 
         SForm panel = new SForm(new SBorderLayout());
         panel.add(controls, SBorderLayout.NORTH);
         panel.add(table, SBorderLayout.CENTER);
         panel.add(clicks, SBorderLayout.SOUTH);
         return panel;
     }
 
     static class MyCellRenderer extends SDefaultTableCellRenderer {
         private static final SFont MONOSPACE = new SFont("monospace", SFont.BOLD, 10);
 
         public MyCellRenderer() {
             setEditIcon(getSession().getCGManager().getIcon("TableCG.editIcon"));
         }
 
         public SComponent getTableCellRendererComponent(STable table,
                                                         Object value,
                                                         boolean selected,
                                                         int row,
                                                         int col) {
             setHorizontalAlignment(SConstants.LEFT);
             setIcon(null);
             setFont(null);
             setForeground(null);
             setComponentPopupMenu(null);
 
             if (value instanceof Color) {
                 Color c = (Color) value;
                 setFont(MONOSPACE);
                 setText(colorToHex(c));
                 setForeground(c);
                 return this;
             }
             else if (value instanceof Boolean && row != -1) {
                 setText("" + value);
                 return this;
             }
             else if (value instanceof Integer && row != -1) {
                 setHorizontalAlignment(SConstants.RIGHT);
                 return super.getTableCellRendererComponent(table, value, selected, row, col);
             }
             else
                 return super.getTableCellRendererComponent(table, value, selected, row, col);
         }
     }
 
     static class MyRowSelectionRenderer extends SDefaultTableRowSelectionRenderer
     {
         public SComponent getTableCellRendererComponent(STable table, Object value, boolean selected, int row, int col) {
             return super.getTableCellRendererComponent(table, value, selected, row, col);    //To change body of overridden methods use File | Settings | File Templates.
         }
     }
 
     static class MyTableModel extends AbstractTableModel {
         int cols, rows;
 
         Object[][] data;
         boolean asc[];
 
         MyTableModel(int pCols, int pRows) {
             this.cols = pCols > 6 ? pCols: 6;
             this.rows = pRows;
 
             data = new Object[rows][cols];
             asc = new boolean[cols];
 
             for (int c = 0; c < cols; c++) {
                 for (int r = 0; r < rows; r++)
                     data[r][c] = (c == 1 ? "stretched cell " : "cell ") + r + ":" + c;
             }
             for (int r = 0; r < rows; r++)
                 data[r][2] = createColor(r);
             for (int r = 0; r < rows; r++)
                 data[r][3] = createImage(r);
             for (int r = 0; r < rows; r++)
                 data[r][4] = createBoolean(r);
             for (int r = 0; r < rows; r++)
                 data[r][5] = createInteger(r);
         }
 
         public int getColumnCount() {
             return cols;
         }
 
         public String getColumnName(int col) {
             return "col " + col;
         }
 
         public int getRowCount() {
             return rows;
         }
 
         public Object getValueAt(int row, int col) {
             return data[row][col];
         }
 
         public void setValueAt(Object value, int row, int col) {
             if (value == null)
                 data[row][col] = null;
             else if (getColumnClass(col).isAssignableFrom(String.class))
                 data[row][col] = value.toString();
             else if (getColumnClass(col).isAssignableFrom(Boolean.class))
                 data[row][col] = new Boolean(((Boolean) value).booleanValue());
         }
 
         public Class getColumnClass(int columnIndex) {
             switch (columnIndex) {
                 case 2:
                     return Color.class;
                 case 3:
                     return SIcon.class;
                 case 4:
                     return Boolean.class;
                 case 5:
                     return Integer.class;
             }
             return String.class;
         }
 
         public Color createColor(int row) {
             return colors[row % colors.length];
         }
 
         public SIcon createImage(int row) {
             return image;
         }
 
         public Boolean createBoolean(int row) {
             if (row % 2 == 1)
                 return new Boolean(false);
             else
                 return new Boolean(true);
         }
 
         public Integer createInteger(int row) {
             return new Integer(row);
         }
 
         public void sort(int col, boolean ascending) {
             log.debug("sort");
             if (col < asc.length)
                 asc[col] = !ascending;
         }
 
         public boolean isCellEditable(int row, int col) {
             if (getColumnClass(col).isAssignableFrom(String.class) ||
                     getColumnClass(col).isAssignableFrom(Boolean.class))
                 return true;
             else
                 return false;
         }
     }
 
     static class ROTableModel extends MyTableModel {
         public ROTableModel(int cols, int rows) {
             super(cols, rows);
         }
 
         public boolean isCellEditable(int row, int col) {
             return false;
         }
     }
 
     static String colorToHex(Color color) {
         String colorstr = "#";
 
         // Red
         String str = Integer.toHexString(color.getRed());
         if (str.length() < 2)
             colorstr += "0" + str;
         else
             colorstr += str;
 
         // Green
         str = Integer.toHexString(color.getGreen());
         if (str.length() < 2)
             colorstr += "0" + str;
         else
             colorstr += str;
 
         // Blue
         str = Integer.toHexString(color.getBlue());
         if (str.length() < 2)
             colorstr += "0" + str;
         else
             colorstr += str;
 
         return colorstr;
     }
 
 
     /**
      * Proof that we can do some really nice tables with j-wings.
      */
     public class MyTable extends STable {
         private final /*static*/ TableCG myTableCG = new TableCG();
 
         public MyTable(TableModel tm) {
             super(tm);
             myTableCG.setFixedTableBorderWidth("0");
             setCG(myTableCG);
         }
 
         /**
          * Returns the CSS style for a row (<td style="xxx")
          */
         public String getRowStyle(int row) {
             return isRowSelected(row) ? "table_selected_row" : (row % 2 == 0 ? "table_row1" : "table_row2");
         }
     }
 
     class TableControls extends ComponentControls {
         private final String[] SELECTION_MODES = new String[]{"no", "single", "multiple"};
 
         public TableControls() {
             final SCheckBox editable = new SCheckBox("editable");
             editable.addActionListener(new java.awt.event.ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                     if (!editable.isSelected()) {
                         table.removeEditor();
                     }
                     table.setEditable(editable.isSelected());
                 }
             });
 
             final SCheckBox consume = new SCheckBox("Consume events on 'col 1'");
             consume.setToolTipText("<html>A SMouseListener will intercept the mouse clicks.<br>" +
                     "Consumed events will not be processed by the table anymore");
             consume.addActionListener(new java.awt.event.ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                     TableExample.this.consume = consume.isSelected();
                 }
             });
 
             final SComboBox selectionMode = new SComboBox(SELECTION_MODES);
             selectionMode.addItemListener(new ItemListener() {
                 public void itemStateChanged(ItemEvent e) {
                     if ("no".equals(selectionMode.getSelectedItem()))
                         table.setSelectionMode(STable.NO_SELECTION);
                     else if ("single".equals(selectionMode.getSelectedItem()))
                         table.setSelectionMode(STable.SINGLE_SELECTION);
                     else if ("multiple".equals(selectionMode.getSelectedItem()))
                         table.setSelectionMode(STable.MULTIPLE_SELECTION);
                 }
             });
 
             addControl(editable);
             addControl(new SLabel(""));
             addControl(consume);
             addControl(new SLabel(" selection mode"));
             addControl(selectionMode);
 
             final SComboBox headerColor = new SComboBox(COLORS);
             headerColor.addActionListener(new java.awt.event.ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                     Color color = (Color) ((Object[]) headerColor.getSelectedItem())[1];
                     table.setAttribute(STable.SELECTOR_HEADER, CSSProperty.BACKGROUND_COLOR, CSSStyleSheet.getAttribute(color));
                 }
             });
             headerColor.setRenderer(new ObjectPairCellRenderer());
             addControl(new SLabel(" header"));
             addControl(headerColor);
 
             final SComboBox oddColor = new SComboBox(COLORS);
             oddColor.addActionListener(new java.awt.event.ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                     Color color = (Color) ((Object[]) oddColor.getSelectedItem())[1];
                     table.setAttribute(STable.SELECTOR_ODD_ROWS, CSSProperty.BACKGROUND_COLOR, CSSStyleSheet.getAttribute(color));
                 }
             });
             oddColor.setRenderer(new ObjectPairCellRenderer());
             addControl(new SLabel(" odd row"));
             addControl(oddColor);
 
             final SComboBox evenColor = new SComboBox(COLORS);
             evenColor.addActionListener(new java.awt.event.ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                     Color color = (Color) ((Object[]) evenColor.getSelectedItem())[1];
                     table.setAttribute(STable.SELECTOR_EVEN_ROWS, CSSProperty.BACKGROUND_COLOR, CSSStyleSheet.getAttribute(color));
                 }
             });
             evenColor.setRenderer(new ObjectPairCellRenderer());
             addControl(new SLabel(" even row"));
             addControl(evenColor);
 
             final SCheckBox reverseColumnOrder = new SCheckBox("Reverse column order");
             reverseColumnOrder.addActionListener(new java.awt.event.ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                     STableColumnModel columnModel = table.getColumnModel();
                     Collections.reverse((java.util.List)columnModel.getColumns());
                     table.reload(ReloadManager.STATE);
                 }
             });
             addControl(reverseColumnOrder);
 
             final SCheckBox hideSomeColumns = new SCheckBox("Hide some Columns");
             hideSomeColumns.addActionListener(new java.awt.event.ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                     SDefaultTableColumnModel columnModel = (SDefaultTableColumnModel) table.getColumnModel();
                     for (int i = 0; i < columnModel.getColumnCount(); ++i) {
                         if (i % 3 == 0) {
                             STableColumn column = columnModel.getColumn(i);
                             columnModel.setColumnHidden(column, hideSomeColumns.isSelected());
                         }
                     }
                 }
             });
             addControl(hideSomeColumns);
         }
     }
 }
