 /*
  * Copyright 2000,2005 wingS development team.
  *
  * This file is part of wingS (http://wingsframework.org).
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
 import org.wingx.XTable;
 import org.wingx.XCalendar;
 import org.wingx.table.*;
 
 import javax.swing.table.TableModel;
 import java.awt.*;
 import java.awt.event.*;
 import java.util.*;
 import java.util.List;
 
 
 /**
  * @author Holger Engels
  */
 public class XTableExample extends WingSetPane{
     private XTable table;
     private SLabel clicks = new SLabel();
     private TableControls controls;
     private boolean consume = false;
     protected final MyCellRenderer cellRenderer = new MyCellRenderer();
     protected final Color[] colors = {
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
     protected final SIcon image = new SResourceIcon("org/wings/icons/JavaCup.gif");
 
     protected SComponent createControls() {
         controls = new TableControls();
         return controls;
     }
 
     public SComponent createExample() {
         table = new XTable(new MyTableModel(7, 5));
         table.setName("xTableExample");
         table.setShowGrid(true);
         table.setSelectionMode(STable.NO_SELECTION);
         table.setDefaultRenderer(cellRenderer);
         table.setShowAsFormComponent(true);
         table.setEditable(false);
         controls.addControllable(table);
 
         getColumn(0).setSortable(true);
         getColumn(1).setSortable(true);
         getColumn(5).setSortable(true);
         getColumn(6).setSortable(true);
 
         getColumn(0).setFilterRenderer(new StringFilterRenderer());
         getColumn(0).setFilterable(true);
 
         getColumn(4).setFilterRenderer(new BooleanFilterRenderer());
         getColumn(4).setCellRenderer(new BooleanEditableCellRenderer());
         getColumn(4).setFilterable(true);
 
         getColumn(5).setFilterRenderer(new StringFilterRenderer());
         getColumn(5).setFilterable(true);
 
         getColumn(6).setCellRenderer(new DateEditableCellRenderer());
         getColumn(6).setFilterable(true);
 
         getColumn(0).setWidth("200px");
         getColumn(1).setWidth("*");
         getColumn(2).setWidth("100px");
         getColumn(3).setWidth("50px");
         getColumn(4).setWidth("50px");
         getColumn(5).setWidth("50px");
         getColumn(6).setWidth("100px");
 
         table.addMouseListener(new SMouseListener() {
             public void mouseClicked(SMouseEvent e) {
                 if (consume && table.columnAtPoint(e.getPoint()) == 1)
                     e.consume();
                 clicks.setText("clicked " + e.getPoint());
             }
         });
         table.setVerticalAlignment(SConstants.TOP_ALIGN);
 
         SPanel panel = new SPanel(new SBorderLayout());
         panel.add(table, SBorderLayout.CENTER);
         panel.add(clicks, SBorderLayout.SOUTH);
         panel.setVerticalAlignment(SConstants.TOP_ALIGN);
         return panel;
     }
 
     private XTableColumn getColumn(final int column) {
         return ((XTableColumn)table.getColumnModel().getColumn(column));
     }
 
 
     class MyTableModel extends XTableModel {
         int cols, rows;
 
         Object[][] origData;
         Object[][] data;
 
         MyTableModel(int pCols, int pRows) {
             this.cols = pCols > 7 ? pCols: 7;
             this.rows = pRows;
 
             origData = new Object[rows][cols];
 
             for (int c = 0; c < cols; c++) {
                 for (int r = 0; r < rows; r++)
                     origData[r][c] = (c == 1 ? "stretched cell " : "cell ") + r + ":" + c;
             }
             for (int r = 0; r < rows; r++)
                 origData[r][2] = createColor(r);
             for (int r = 0; r < rows; r++)
                 origData[r][3] = createImage(r);
             for (int r = 0; r < rows; r++)
                 origData[r][4] = createBoolean(r);
             for (int r = 0; r < rows; r++)
                 origData[r][5] = createInteger(r);
             for (int r = 0; r < rows; r++)
                 origData[r][6] = createDate(r);
 
             refresh();
         }
 
         public int getColumnCount() {
             return cols;
         }
 
         public String getColumnName(int col) {
             return "col " + col;
         }
 
         public int getRowCount() {
             return data.length;
         }
 
         public Object getValueAt(int row, int col) {
             return data[row][col];
         }
 
         public void setValueAt(Object value, int row, int col) {
             data[row][col] = value;
             fireTableCellUpdated(row, col);
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
                 case 6:
                     return Date.class;
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
             return row % 2 == 1 ? Boolean.FALSE : Boolean.TRUE;
         }
 
         public Integer createInteger(int row) {
             return new Integer(row);
         }
 
         public Date createDate(int row) {
             return new Date(System.currentTimeMillis() + row * 1000 * 60 * 60 * 24);
         }
 
         public int toInt(String numberString, int startPos) {
             try {
                 return Integer.parseInt(numberString.substring(startPos).trim());
             } catch (NumberFormatException ex) {
                 return Integer.MIN_VALUE;
             }
         }
 
         public void refresh() {
            List<Object[]> list = Arrays.asList((Object[][]) origData.clone());
             for (Iterator<Object[]> iterator = list.iterator(); iterator.hasNext();) {
                 final Object[] rowData = iterator.next();
                 boolean remove = false;
                 for (int i = 0; i < getColumnCount(); i++) {
                     Object filter = getFilter(i);
                     if (filter != null) {
                         if (getColumnClass(i) == String.class) {
                             String s = (String) rowData[i];
                             String f = ((String) filter).toUpperCase();
                             if (s == null || s.toUpperCase().indexOf(f) == -1)
                                 remove |= true;
                         } else if (getColumnClass(i) == Boolean.class) {
                             if (!filter.equals(rowData[i]))
                                 remove |= true;
                         } else if (getColumnClass(i) == Integer.class) {
                             final int intValue = ((Integer) rowData[i]).intValue();
                             final String filterString = ((String) filter).trim();
                             if (filterString.startsWith(">="))
                                 remove |= !(intValue >= toInt(filterString, 2));
                             else if (filterString.startsWith("<="))
                                 remove |= !(intValue <= toInt(filterString, 2));
                             else if (filterString.startsWith("=="))
                                 remove |= !(intValue == toInt(filterString, 2));
                             else if (filterString.startsWith(">"))
                                 remove |= !(intValue > toInt(filterString, 1));
                             else if (filterString.startsWith("<"))
                                 remove |= !(intValue < toInt(filterString, 1));
                             else if (filterString.startsWith("="))
                                 remove |= !(intValue == toInt(filterString, 1));
                             else
                                 remove |= !(intValue == toInt(filterString, 0));
                         }
                     }
                 }
                 if (remove)
                     iterator.remove();
             }
             data = list.toArray(new Object[0][0]);
 
             Arrays.sort(data, new Comparator<Object>() {
                 public int compare(Object o1, Object o2) {
                     for (int i=0; i < getColumnCount(); i++) {
                         Object[] r1 = (Object[])o1;
                         Object[] r2 = (Object[])o2;
                         int comparision;
 
                         switch (getSort(i)) {
                             case SORT_ASCENDING:
                             {
                                 Comparable v1 = (Comparable)r1[i];
                                 Comparable v2 = (Comparable)r2[i];
                                 comparision = v1.compareTo(v2);
                                 if (comparision != 0)
                                     return comparision;
                             }
                             case SORT_DESCENDING:
                             {
                                 Comparable v1 = (Comparable)r1[i];
                                 Comparable v2 = (Comparable)r2[i];
                                 comparision = v2.compareTo(v1);
                                 if (comparision != 0)
                                     return comparision;
                             }
                             default:
                                 comparision = 0;
                         }
                     }
                     return 0;
                 }
             });
 
             fireTableDataChanged();
         }
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
             widthTextField.setText("100%");
             formComponentCheckBox.setSelected(true);
 
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
 
             addControl(new SLabel(" selection mode"));
             addControl(selectionMode);
 
             final SComboBox headerColor = new SComboBox(COLORS);
             headerColor.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                     Color color = (Color) ((Object[]) headerColor.getSelectedItem())[1];
                     table.setAttribute(STable.SELECTOR_HEADER, CSSProperty.BACKGROUND_COLOR, CSSStyleSheet.getAttribute(color));
                 }
             });
             headerColor.setRenderer(new ObjectPairCellRenderer());
             addControl(new SLabel(" header"));
             addControl(headerColor);
 
             final SComboBox oddColor = new SComboBox(COLORS);
             oddColor.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                     Color color = (Color) ((Object[]) oddColor.getSelectedItem())[1];
                     table.setAttribute(STable.SELECTOR_ODD_ROWS, CSSProperty.BACKGROUND_COLOR, CSSStyleSheet.getAttribute(color));
                 }
             });
             oddColor.setRenderer(new ObjectPairCellRenderer());
             addControl(new SLabel(" odd"));
             addControl(oddColor);
 
             final SComboBox evenColor = new SComboBox(COLORS);
             evenColor.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                     Color color = (Color) ((Object[]) evenColor.getSelectedItem())[1];
                     table.setAttribute(STable.SELECTOR_EVEN_ROWS, CSSProperty.BACKGROUND_COLOR, CSSStyleSheet.getAttribute(color));
                 }
             });
             evenColor.setRenderer(new ObjectPairCellRenderer());
             addControl(new SLabel(" even"));
             addControl(evenColor);
 
             final SCheckBox reverseColumnOrder = new SCheckBox("reverse columns");
             reverseColumnOrder.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                     STableColumnModel columnModel = table.getColumnModel();
                     Collections.reverse((java.util.List)columnModel.getColumns());
                     table.reload();
                 }
             });
             addControl(reverseColumnOrder);
 
             final SCheckBox hideSomeColumns = new SCheckBox("Hide some Columns");
             hideSomeColumns.addActionListener(new ActionListener() {
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
 
     private static class StringFilterRenderer
         extends STextField
         implements EditableTableCellRenderer
     {
         public StringFilterRenderer() {
             addActionListener(new NoAction());
         }
 
         public Object getValue() {
             String s = getText();
             if ("".equals(s))
                 return null;
             return s;
         }
 
         public SComponent getTableCellRendererComponent(STable table, Object value, boolean isSelected, int row, int column) {
             setText(value != null ? value.toString() : null);
             return this;
         }
 
         public LowLevelEventListener getLowLevelEventListener(STable table, int row, int column) {
             return this;
         }
     }
 
     private static class BooleanEditableCellRenderer
         extends SCheckBox
         implements EditableTableCellRenderer
     {
         public Object getValue() {
             return isSelected() ? Boolean.TRUE : Boolean.FALSE;
         }
 
         public SComponent getTableCellRendererComponent(STable table, Object value, boolean isSelected, int row, int column) {
             setSelected(Boolean.TRUE.equals(value));
             return this;
         }
 
         public LowLevelEventListener getLowLevelEventListener(STable table, int row, int column) {
             return this;
         }
     }
 
     private static class BooleanFilterRenderer
         extends SComboBox
         implements EditableTableCellRenderer
     {
         public BooleanFilterRenderer() {
             super(new Boolean[] { null, Boolean.TRUE, Boolean.FALSE });
             addActionListener(new NoAction());
         }
 
         public Object getValue() {
             return getSelectedItem();
         }
 
         public SComponent getTableCellRendererComponent(STable table, Object value, boolean isSelected, int row, int column) {
             setSelectedItem(value);
             return this;
         }
 
         public LowLevelEventListener getLowLevelEventListener(STable table, int row, int column) {
             return this;
         }
     }
 
     private static class DateEditableCellRenderer
         extends XCalendar
         implements EditableTableCellRenderer
     {
         public Object getValue() {
             return getDate();
         }
 
         public SComponent getTableCellRendererComponent(STable table, Object value, boolean isSelected, int row, int column) {
             setDate((Date)value);
             return this;
         }
 
         public LowLevelEventListener getLowLevelEventListener(STable table, int row, int column) {
             return this;
         }
 
         public void setNameRaw(String uncheckedName) {
             super.setNameRaw(uncheckedName);
             getFormattedTextField().setNameRaw(uncheckedName);
         }
     }
 
     static class NoAction implements ActionListener {
         public void actionPerformed(ActionEvent e) {
         }
     }
 
     private static class MyCellRenderer extends SDefaultTableCellRenderer {
         private final SFont MONOSPACE = new SFont("monospace", SFont.BOLD, SFont.DEFAULT_SIZE);
 
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
 
     private static String colorToHex(Color color) {
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
 
 }
