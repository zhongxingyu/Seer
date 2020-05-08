 /*
  * Copyright (c) 2005 Your Corporation. All Rights Reserved.
  */
 package com.canoo.ulc.community.fixedcolumntabletree.client;
 
 /*
  * Copyright � 2000-2005 Canoo Engineering AG, Switzerland.
  */
 
 import com.ulcjava.base.client.UIScrollPane;
 import com.ulcjava.base.client.tabletree.JTableTree;
 import com.ulcjava.base.client.tabletree.TableTreeColumn;
 import com.ulcjava.base.client.tabletree.TableTreeTable;
 
 import javax.swing.*;
 import javax.swing.event.TreeExpansionEvent;
 import javax.swing.event.TreeExpansionListener;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 public class UIFixedColumnTableTree extends UIScrollPane {
 
     protected void postInitializeState() {
         super.postInitializeState();
         final JTableTree rowHeader = (JTableTree) getBasicScrollPane().getRowHeader().getComponent(0);
         removeKeystrokes((TableTreeTable) rowHeader.getComponent(0), new KeyStroke[]{
                 KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0)
         });
         JTableTree viewPort = (JTableTree) getBasicScrollPane().getViewport().getComponent(0);
         removeKeystrokes((TableTreeTable) viewPort.getComponent(0), new KeyStroke[]{
                 KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0)
         });
         removeKeystrokes((TableTreeTable) rowHeader.getComponent(0), new KeyStroke[]{
                 KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK)
         });
         removeKeystrokes((TableTreeTable) viewPort.getComponent(0), new KeyStroke[]{
                 KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK)
         });
         removeKeystrokes((TableTreeTable) rowHeader.getComponent(0), new KeyStroke[]{
                 KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK)
         });
         removeKeystrokes((TableTreeTable) viewPort.getComponent(0), new KeyStroke[]{
                 KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK)
         });
         rowHeader.addTreeExpansionListener(new MyExpansionListener(viewPort));
         rowHeader.addTreeSelectionListener(new MySelectionListener(viewPort, rowHeader));
         viewPort.addTreeSelectionListener(new MySelectionListener(rowHeader, viewPort));
 
         Dimension preferredSize = getRowHeaderDimension(rowHeader);
         getBasicScrollPane().getRowHeader().setPreferredSize(preferredSize);
 
         final PropertyChangeListener rowHeaderSizeAdaptor = new PropertyChangeListener() {
 
             public void propertyChange(PropertyChangeEvent evt) {
                 if ("width".equals(evt.getPropertyName())) {
                     Dimension preferredSize = getRowHeaderDimension(rowHeader);
                     getBasicScrollPane().getRowHeader().setPreferredSize(preferredSize);
                 }
             }
         };
         for (int i = 0; i < rowHeader.getBasicTable().getColumnModel().getColumnCount(); i++) {
             rowHeader.getBasicTable().getColumnModel().getColumn(i).addPropertyChangeListener(rowHeaderSizeAdaptor);
         }
         rowHeader.registerKeyboardAction(new RowHeaderTabAction(rowHeader, viewPort), KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0, true), JComponent.WHEN_FOCUSED);
         viewPort.registerKeyboardAction(new ViewPortTabAction(rowHeader, viewPort), KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0, true), JComponent.WHEN_FOCUSED);
         viewPort.registerKeyboardAction(new LeftKeyAction(rowHeader, viewPort), KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, true), JComponent.WHEN_FOCUSED);
         rowHeader.registerKeyboardAction(new RowHeaderLeftTabAction(rowHeader, viewPort), KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK), JComponent.WHEN_FOCUSED);
         viewPort.registerKeyboardAction(new ViewPortLeftTabAction(rowHeader, viewPort), KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK), JComponent.WHEN_FOCUSED);
     }
 
     private void removeKeystrokes(TableTreeTable component, KeyStroke[] keyStrokes) {
         removeFromMap(component.getInputMap(JComponent.WHEN_FOCUSED), keyStrokes);
         removeFromMap(component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW), keyStrokes);
         removeFromMap(component.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT), keyStrokes);
     }
 
     private void removeFromMap(InputMap map, KeyStroke[] keyStrokes) {
         for (KeyStroke keyStroke : keyStrokes) {
             map.remove(KeyStroke.getKeyStroke(keyStroke.getKeyCode(), keyStroke.getModifiers()));
             InputMap parent = map.getParent();
             if (parent != null) {
                 parent.remove(KeyStroke.getKeyStroke(keyStroke.getKeyCode(), keyStroke.getModifiers()));
             }
         }
     }
 
     private Dimension getRowHeaderDimension(JTableTree rowHeader) {
         int rowHeaderWidth = 0;
         for (int i = 0; i < rowHeader.getColumnCount(); i++) {
             TableTreeColumn basicColumn = rowHeader.getColumnModel().getColumn(i);
             rowHeaderWidth += basicColumn.getPreferredWidth();
         }
 
         // height is not accounted by scroll pane layout
         Dimension preferredSize = new Dimension(rowHeaderWidth, -1);
         return preferredSize;
     }
 
 
     /**
      * Used to expand/collapse the elements of the second table, following
      * the first one.
      */
     private static class MyExpansionListener implements TreeExpansionListener {
         private JTableTree fTableTree;
 
         public MyExpansionListener(JTableTree source) {
             fTableTree = source;
         }
 
         public void treeCollapsed(TreeExpansionEvent event) {
             fTableTree.collapsePath(event.getPath());
         }
 
         public void treeExpanded(TreeExpansionEvent event) {
             fTableTree.expandPath(event.getPath());
         }
     }
 
     /**
      * Used to synchronize the selections of the two tableTrees
      */
     private class MySelectionListener implements TreeSelectionListener {
         private JTableTree fTarget;
         private JTableTree fSource;
 
         public MySelectionListener(JTableTree target, JTableTree source) {
             fTarget = target;
             fSource = source;
         }
 
         public void valueChanged(TreeSelectionEvent event) {
            if (event.isAddedPath()) { //not called during clearing
                if (fSource.getCellSelectionEnabled() || fSource.getColumnSelectionAllowed()) {
                    fTarget.clearSelection();
                    if (fTarget.getColumnCount() > 0) {
                        fTarget.removeColumnSelectionInterval(0, fTarget.getColumnCount() - 1);
                        fTarget.scrollCellToVisible(event.getPath(), 0);
                    }
                } else if (fSource.getRowSelectionAllowed()) {
                    int[] selectedRows = fSource.getSelectedRows();
                    for (int selectedRow : selectedRows) {
                        fTarget.setRowSelectionInterval(selectedRow, selectedRow);
                        fTarget.scrollCellToVisible(event.getPath(), 0);
                     }
                 }
             }
         }
     }
 
     private class RowHeaderTabAction implements ActionListener {
         JTableTree rowHeaderTableTree;
         JTableTree viewPortTableTree;
 
         public RowHeaderTabAction(JTableTree rowHeaderTableTree, JTableTree viewPortTableTree) {
             this.rowHeaderTableTree = rowHeaderTableTree;
             this.viewPortTableTree = viewPortTableTree;
         }
 
         public void actionPerformed(ActionEvent actionEvent) {
             if (rowHeaderTableTree.getCellSelectionEnabled()) {
                 int selectedColumn = rowHeaderTableTree.getSelectedColumn();
                 if (selectedColumn == rowHeaderTableTree.getColumnCount() - 1) {
                     if (viewPortTableTree.getColumnCount() > 0) {
                         viewPortTableTree.requestFocus();
                         viewPortTableTree.setColumnSelectionInterval(0, 0);
                         int selectedRow = rowHeaderTableTree.getSelectedRow();
                         viewPortTableTree.setRowSelectionInterval(selectedRow, selectedRow);
                     }
                 } else {
                     int column = rowHeaderTableTree.getSelectedColumn();
                     rowHeaderTableTree.setColumnSelectionInterval(column + 1, column + 1);
                 }
             }
         }
     }
 
     private class ViewPortTabAction implements ActionListener {
         JTableTree rowHeaderTableTree;
         JTableTree viewPortTableTree;
 
         public ViewPortTabAction(JTableTree rowHeaderTableTree, JTableTree viewPortTableTree) {
             this.rowHeaderTableTree = rowHeaderTableTree;
             this.viewPortTableTree = viewPortTableTree;
         }
 
         public void actionPerformed(ActionEvent actionEvent) {
             if (viewPortTableTree.getCellSelectionEnabled()) {
                 int selectedColumn = viewPortTableTree.getSelectedColumn();
                 if (selectedColumn == viewPortTableTree.getColumnCount() - 1) {
                     if (rowHeaderTableTree.getColumnCount() > 0) {
                         if (rowHeaderTableTree.getRowCount() > viewPortTableTree.getSelectedRow() + 1) {
                             rowHeaderTableTree.requestFocus();
                             rowHeaderTableTree.setColumnSelectionInterval(0, 0);
                             int rowIndex = viewPortTableTree.getSelectedRow() + 1;
                             rowHeaderTableTree.setRowSelectionInterval(rowIndex, rowIndex);
                         }
                     }
                 } else {
                     int column = viewPortTableTree.getSelectedColumn();
                     viewPortTableTree.setColumnSelectionInterval(column + 1, column + 1);
                 }
             }
         }
     }
 
     private class LeftKeyAction implements ActionListener {
         private JTableTree rowHeader;
         private JTableTree viewPort;
 
         public LeftKeyAction(JTableTree rowHeader, JTableTree viewPort) {
             this.rowHeader = rowHeader;
             this.viewPort = viewPort;
         }
 
         public void actionPerformed(ActionEvent e) {
             if (viewPort.getCellSelectionEnabled()) {
                 int selectedColumn = viewPort.getSelectedColumn();
                 if (selectedColumn == 0) {
                     if (rowHeader.getColumnCount() > 0) {
                         rowHeader.requestFocus();
                         int lastColumn = rowHeader.getColumnCount() - 1;
                         rowHeader.setColumnSelectionInterval(lastColumn, lastColumn);
                         int selectedRow = viewPort.getSelectedRow();
                         rowHeader.setRowSelectionInterval(selectedRow, selectedRow);
                     }
                 }
             }
         }
     }
 
     private class RowHeaderLeftTabAction implements ActionListener {
         JTableTree rowHeaderTableTree;
         JTableTree viewPortTableTree;
 
         public RowHeaderLeftTabAction(JTableTree rowHeaderTableTree, JTableTree viewPortTableTree) {
             this.rowHeaderTableTree = rowHeaderTableTree;
             this.viewPortTableTree = viewPortTableTree;
         }
 
         public void actionPerformed(ActionEvent actionEvent) {
             if (rowHeaderTableTree.getCellSelectionEnabled()) {
                 int selectedColumn = rowHeaderTableTree.getSelectedColumn();
                 int selectedRow = Math.max(rowHeaderTableTree.getSelectedRow(), viewPortTableTree.getSelectedRow());
                 if (selectedColumn == 0) {
                     if (viewPortTableTree.getColumnCount() > 0) {
                         int viewPortLastColumn = viewPortTableTree.getColumnCount() - 1;
                         viewPortTableTree.requestFocus();
                         viewPortTableTree.setColumnSelectionInterval(viewPortLastColumn, viewPortLastColumn);
                         if (selectedRow > 0)
                             selectedRow = selectedRow - 1;
                         viewPortTableTree.setRowSelectionInterval(selectedRow, selectedRow);
                     }
                 } else if (selectedColumn > 0) {
                     rowHeaderTableTree.setColumnSelectionInterval(selectedColumn - 1, selectedColumn - 1);
                 }
             }
         }
     }
 
     private class ViewPortLeftTabAction implements ActionListener {
         JTableTree rowHeaderTableTree;
         JTableTree viewPortTableTree;
 
         public ViewPortLeftTabAction(JTableTree rowHeaderTableTree, JTableTree viewPortTableTree) {
             this.rowHeaderTableTree = rowHeaderTableTree;
             this.viewPortTableTree = viewPortTableTree;
         }
 
         public void actionPerformed(ActionEvent actionEvent) {
             if (viewPortTableTree.getCellSelectionEnabled()) {
                 int selectedColumn = viewPortTableTree.getSelectedColumn();
                 if (selectedColumn == 0) {
                     if (rowHeaderTableTree.getColumnCount() > 0) {
                         rowHeaderTableTree.requestFocus();
                         int lastColumn = rowHeaderTableTree.getColumnCount() - 1;
                         rowHeaderTableTree.setColumnSelectionInterval(lastColumn, lastColumn);
                         int selectedRow = viewPortTableTree.getSelectedRow();
                         rowHeaderTableTree.setRowSelectionInterval(selectedRow, selectedRow);
                     }
                 } else if (selectedColumn > 0) {
                     viewPortTableTree.setColumnSelectionInterval(selectedColumn - 1, selectedColumn - 1);
                 }
             }
         }
 
 
     }
 }
