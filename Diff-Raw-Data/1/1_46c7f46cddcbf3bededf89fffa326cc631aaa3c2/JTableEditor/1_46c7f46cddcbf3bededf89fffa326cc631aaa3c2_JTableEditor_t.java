 /*
  * @(#) $RCSfile: JTableEditor.java,v $ $Revision: 1.1 $ $Date: 2004/08/02 20:23:42 $ $Name: TableView1_3_2 $
  *
  * Center for Computational Genomics and Bioinformatics
  * Academic Health Center, University of Minnesota
  * Copyright (c) 2000-2002. The Regents of the University of Minnesota  
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * see: http://www.gnu.org/copyleft/gpl.html
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  */
 package edu.umn.genomics.table;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import javax.swing.*;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.JTableHeader;
 import javax.swing.table.TableColumnModel;
 import javax.swing.table.TableModel;
 
 /**
  *
  * @author J Johnson
  * @version $Revision: 1.1 $ $Date: 2004/08/02 20:23:42 $ $Name: TableView1_3_2
  * $
  * @since 1.0
  * @see javax.swing.JTable
  * @see javax.swing.table.TableModel
  */
 public class JTableEditor extends AbstractTableSource {
 
     static int createCounter = 0;
     String[] columnTypes = {
         "Text",
         "Integer",
         "Number",
         "Date",
         "Boolean", //"Image",
     //"Color",
     };
     Class[] columnTypeClass = {
         java.lang.String.class,
         java.lang.Integer.class,
         java.lang.Double.class,
         java.util.Date.class,
         java.lang.Boolean.class, // java.awt.Image.class,
     // java.awt.Color.class,
     };
     /*
      * DataTypes: Text URL email Number Integer Currency Percent Date Time
      * Boolean Color Image
      */
     TypedTableModel dtm;
     JTable table = new JTable();
     DefaultListSelectionModel colLSM = new DefaultListSelectionModel();
     JScrollPane jsp;
     JToolBar tb = new JToolBar();
     JButton newTblBtn;
     JButton addColBtn;
     JButton insColBtn;
     JButton delColBtn;
     JButton addRowBtn;
     JButton insRowBtn;
     JButton delRowBtn;
 
     /**
      * Generates row numbers for a JList.
      */
     class RowNumListModel extends AbstractListModel {
 
         public void setSize(int size) {
             fireContentsChanged(this, 0, size - 1);
         }
 
         public int getSize() {
             return dtm != null ? dtm.getRowCount() : 0;
         }
 
         public Object getElementAt(int index) {
             return new Integer(index + 1);
             // return new Integer((indexMap != null ? indexMap.getSrc(index) : index) + 1);
         }
     }
     RowNumListModel rowNumLM = new RowNumListModel();
     JList rowNums = new JList(rowNumLM);
     TableModelListener tml = new TableModelListener() {
 
         public void tableChanged(TableModelEvent e) {
             if (e.getSource() != null) {
                 rowNumLM.setSize(((TableModel) e.getSource()).getRowCount());
             }
         }
     };
     MouseAdapter colSelector = new MouseAdapter() {
 
         public void mouseClicked(MouseEvent e) {
             int colIdx = table.getTableHeader().columnAtPoint(e.getPoint());
             Rectangle rect = table.getTableHeader().getHeaderRect(colIdx);
             if (colIdx >= 0) {
                 TableColumnModel columnModel = table.getColumnModel();
                 int viewColumn = columnModel.getColumnIndexAtX(e.getX());
                 int column = table.convertColumnIndexToModel(viewColumn);
                 if (e.getClickCount() == 1 && column != -1) {
                     if (e.isControlDown()) {
                         if (colLSM.isSelectedIndex(column)) {
                             colLSM.removeSelectionInterval(column, column);
                         } else {
                             colLSM.addSelectionInterval(column, column);
                         }
                     } else {
                         colLSM.setSelectionInterval(column, column);
                     }
                 }
                 table.getTableHeader().repaint();
                 return;
             }
         }
     };
 
     /**
      * Column Header renderer class that has SortArrow icons.
      */
     class ColumnRenderer extends DefaultTableCellRenderer {
 
         public Component getTableCellRendererComponent(JTable table,
                 Object value,
                 boolean isSelected,
                 boolean hasFocus,
                 int row,
                 int column) {
             int ci = table.convertColumnIndexToModel(column);
             // setHorizontalTextPosition(JLabel.RIGHT);
             // setHorizontalAlignment(JLabel.LEFT);
             if (table != null) {
                 JTableHeader header = table.getTableHeader();
                 if (header != null) {
                     setForeground(header.getForeground());
                     setBackground(colLSM.isSelectedIndex(column) ? table.getSelectionBackground()
                             : header.getBackground());
                     setFont(header.getFont());
                 }
             }
             setText((value == null) ? "" : value.toString());
             setBorder(UIManager.getBorder("TableHeader.cellBorder"));
             return this;
         }
     };
 
     public JTableEditor() {
         this(true, true);
     }
 
     public JTableEditor(boolean columnChangesAllowed, boolean rowChangesAllowed) {
         setLayout(new BorderLayout());
         table.getTableHeader().setDefaultRenderer(new ColumnRenderer());
         table.getTableHeader().setReorderingAllowed(false);
         // Set the AbstractTableModel variable
         tableModel = dtm;
         Icon newTblIcon = null;
         Icon addColIcon = null;
         Icon insColIcon = null;
         Icon delColIcon = null;
         Icon addRowIcon = null;
         Icon insRowIcon = null;
         Icon delRowIcon = null;
 
         try {
             ClassLoader cl = this.getClass().getClassLoader();
             // Java look and feel Graphics Repository: Table Toolbar Button Graphics
             newTblIcon = new ImageIcon(cl.getResource("edu/umn/genomics/table/Icons/NewTable24.gif"));
             addColIcon = new ImageIcon(cl.getResource("edu/umn/genomics/table/Icons/ColumnInsertAfter24.gif"));
             insColIcon = new ImageIcon(cl.getResource("edu/umn/genomics/table/Icons/ColumnInsertBefore24.gif"));
             delColIcon = new ImageIcon(cl.getResource("edu/umn/genomics/table/Icons/ColumnDelete24.gif"));
             addRowIcon = new ImageIcon(cl.getResource("edu/umn/genomics/table/Icons/RowInsertAfter24.gif"));
             insRowIcon = new ImageIcon(cl.getResource("edu/umn/genomics/table/Icons/RowInsertBefore24.gif"));
             delRowIcon = new ImageIcon(cl.getResource("edu/umn/genomics/table/Icons/RowDelete24.gif"));
         } catch (Exception ex) {
             ExceptionHandler.popupException(""+ex);
         }
 
         newTblBtn = newTblIcon != null ? new JButton(newTblIcon) : new JButton("New Table");
         addColBtn = addColIcon != null ? new JButton(addColIcon) : new JButton("Add Column");
         insColBtn = insColIcon != null ? new JButton(insColIcon) : new JButton("Insert Column");
         delColBtn = delColIcon != null ? new JButton(delColIcon) : new JButton("Delete Column");
         addRowBtn = addRowIcon != null ? new JButton(addRowIcon) : new JButton("Add Row");
         insRowBtn = insRowIcon != null ? new JButton(insRowIcon) : new JButton("Insert Row");
         delRowBtn = delRowIcon != null ? new JButton(delRowIcon) : new JButton("Delete Row");
 
         newTblBtn.setToolTipText("Create a New Table.");
         addColBtn.setToolTipText("Add a new column after the current column.");
         insColBtn.setToolTipText("Add a new column before the current column.");
         delColBtn.setToolTipText("Remove the current column.");
         addRowBtn.setToolTipText("Add a new row after the current row.");
         insRowBtn.setToolTipText("Add a new row before the current row.");
         delRowBtn.setToolTipText("Remove the current row.");
 
 
         newTblBtn.addActionListener(
                 new ActionListener() {
 
                     public void actionPerformed(ActionEvent e) {
                         newTable();
                     }
                 });
         addColBtn.addActionListener(
                 new ActionListener() {
 
                     public void actionPerformed(ActionEvent e) {
                         int colIndex = colLSM.getMaxSelectionIndex() + 1;
                         newColumn(colIndex > 0 ? colIndex : dtm.getColumnCount());
                     }
                 });
         insColBtn.addActionListener(
                 new ActionListener() {
 
                     public void actionPerformed(ActionEvent e) {
                         int colIndex = colLSM.getMinSelectionIndex();
                         newColumn(colIndex >= 0 ? colIndex : 0);
                     }
                 });
         delColBtn.addActionListener(
                 new ActionListener() {
 
                     public void actionPerformed(ActionEvent e) {
                         int min = colLSM.getMinSelectionIndex();
                         if (min < 0) {
                         } else {
                             int max = colLSM.getMaxSelectionIndex();
                             int[] colIndex = new int[max - min + 1];
                             int n = 0;
                             for (int i = min; i <= max; i++) {
                                 if (colLSM.isSelectedIndex(i)) {
                                     colIndex[n++] = i;
                                 }
                             }
                             if (n < colIndex.length) {
                                 int[] tmp = colIndex;
                                 colIndex = new int[n];
                                 System.arraycopy(tmp, 0, colIndex, 0, n);
                             }
                             dtm.deleteColumns(colIndex);
                             colLSM.clearSelection();
                         }
                     }
                 });
         addRowBtn.addActionListener(
                 new ActionListener() {
 
                     public void actionPerformed(ActionEvent e) {
                         Object[] rowData = new Object[dtm.getColumnCount()];
                         dtm.addRow(rowData);
                     }
                 });
         insRowBtn.addActionListener(
                 new ActionListener() {
 
                     public void actionPerformed(ActionEvent e) {
                         int rowIndex = rowNums.getSelectedIndex();
                         rowIndex = rowIndex >= 0 ? rowIndex : 0;
                         Object[] rowData = new Object[dtm.getColumnCount()];
                         dtm.insertRow(rowIndex, rowData);
                     }
                 });
         delRowBtn.addActionListener(
                 new ActionListener() {
 
                     public void actionPerformed(ActionEvent e) {
                         int[] indices = rowNums.getSelectedIndices();
                         for (int i = indices.length - 1; i >= 0; i--) {
                             dtm.removeRow(indices[i]);
                         }
                        rowNums.clearSelection();
                     }
                 });
 
         newTable();
         table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         table.setRowSelectionAllowed(true);
         table.setCellSelectionEnabled(true);
         setEditing(columnChangesAllowed, rowChangesAllowed);
         jsp = new JScrollPane(table);
         rowNums.setFixedCellHeight(table.getRowHeight());
         rowNums.setBackground(table.getTableHeader().getBackground());
         jsp.setRowHeaderView(rowNums);
         JLabel rowNumSortLbl = new JLabel("Row");
         rowNumSortLbl.setBackground(table.getTableHeader().getBackground());
         jsp.setCorner(JScrollPane.UPPER_LEFT_CORNER, rowNumSortLbl);
         add(jsp);
     }
 
     public void setEditing(boolean columnChangesAllowed, boolean rowChangesAllowed) {
         if (columnChangesAllowed || rowChangesAllowed) {
             tb.removeAll();
             tb.add(newTblBtn);
             if (columnChangesAllowed) {
                 tb.add(addColBtn);
                 tb.add(insColBtn);
                 tb.add(delColBtn);
                 if (rowChangesAllowed) {
                     tb.addSeparator();
                 }
                 table.getTableHeader().addMouseListener(colSelector);
             } else {
                 table.getTableHeader().removeMouseListener(colSelector);
             }
             if (rowChangesAllowed) {
                 tb.add(addRowBtn);
                 tb.add(insRowBtn);
                 tb.add(delRowBtn);
             }
             add(tb, BorderLayout.NORTH);
         } else {
             remove(tb);
         }
     }
 
     public void newTable() {
         if (dtm != null) {
             dtm.removeTableModelListener(tml);
         }
         dtm = new TypedTableModel();
         dtm.addTableModelListener(tml);
         table.setModel(dtm);
         // Set the AbstractTableModel variable
         tableModel = dtm;
         setTableSource(dtm, "Table " + (++createCounter));
     }
 
     public void newColumn(int columnIndex) {
         final int[] colIdx = new int[1];
         colIdx[0] = columnIndex;
         final JTextField columnName = new JTextField(20);
         final JComboBox columnType = new JComboBox(columnTypes);
         JPanel colPanel = new JPanel();
         colPanel.add(new JLabel("Data Type:"));
         colPanel.add(columnType);
         colPanel.add(new JLabel("Column Name:"));
         colPanel.add(columnName);
         final Object[] options = {"Add", "Close"};
         final JOptionPane optionPane = new JOptionPane(
                 colPanel,
                 JOptionPane.QUESTION_MESSAGE,
                 JOptionPane.DEFAULT_OPTION,
                 null,
                 options,
                 options[0]);
         Component topLevel = this.getTopLevelAncestor();
         final JDialog dialog = topLevel instanceof Dialog
                 ? new JDialog((Dialog) this.getTopLevelAncestor(), "New Column", true)
                 : new JDialog((Frame) this.getTopLevelAncestor(), "New Column", true);
         dialog.setContentPane(optionPane);
         dialog.setDefaultCloseOperation(
                 JDialog.DO_NOTHING_ON_CLOSE);
         dialog.addWindowListener(new WindowAdapter() {
 
             public void windowClosing(WindowEvent we) {
                 // setLabel("Thwarted user attempt to close window.");
             }
         });
         optionPane.addPropertyChangeListener(
                 new PropertyChangeListener() {
 
                     public void propertyChange(PropertyChangeEvent e) {
                         String prop = e.getPropertyName();
                         if (dialog.isVisible()
                                 && (e.getSource() == optionPane)
                                 && (prop.equals(JOptionPane.VALUE_PROPERTY))) {
                             Object optVal = optionPane.getValue();
                             if (optVal == options[0]) {
                                 String name = columnName.getText();
                                 if (name.length() > 0) {
                                     newColumn(colIdx[0], name, columnTypeClass[columnType.getSelectedIndex()]);
                                     colLSM.addSelectionInterval(colIdx[0], colIdx[0]);
                                     colIdx[0]++;
                                     columnName.setText("");
                                 }
                                 // reset value so we can enter a new column
                                 optionPane.setValue(null);
                                 return;
                             } else if (optVal == options[1]) {
                                 dialog.setVisible(false);
                             }
                         }
                     }
                 });
         dialog.pack();
         dialog.setLocationRelativeTo(this);
         dialog.setVisible(true);
     }
 
     public void newColumn(int columnIndex, String columnName, Class columnClass) {
         dtm.insertColumn(columnIndex, columnName, columnClass);
     }
 
     public TypedTableModel getTypedTableModel() {
         return dtm;
     }
 
     public void setPreferredViewableRows(int rowNumber) {
         Dimension dim = table.getPreferredScrollableViewportSize();
         dim.height = rowNumber * table.getRowHeight();
         table.setPreferredScrollableViewportSize(dim);
     }
 
     /**
      * Display a JTableEditor usage: java edu.umn.genomics.table.JTableEditor
      *
      * @see FileTableModel
      */
     public static void main(String[] args) {
         JFrame frame = new JFrame("JTableEditor");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         //frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
         JTableEditor tableEdit = new JTableEditor();
         frame.getContentPane().add(tableEdit);
         frame.pack();
         frame.setVisible(true);
     }
 }
