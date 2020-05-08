 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.tools.gui.treetable;
 /*
  * @(#)JTreeTable.java  1.2 98/10/27
  *
  * Copyright 1997, 1998 by Sun Microsystems, Inc.,
  * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
  * All rights reserved.
  *
  * This software is the confidential and proprietary information
  * of Sun Microsystems, Inc. ("Confidential Information").  You
  * shall not disclose such Confidential Information and shall use
  * it only in accordance with the terms of the license agreement
  * you entered into with Sun.
  */
 
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Rectangle;
 import java.awt.event.MouseEvent;
 
 import java.util.EventObject;
 
 import javax.swing.*;
 import javax.swing.event.*;
 import javax.swing.table.*;
 import javax.swing.tree.*;
 
 /**
  * This example shows how to create a simple JTreeTable component, by using a JTree as a renderer (and editor) for the
  * cells in a particular column in the JTable.
  *
  * @author   Philip Milne
  * @author   Scott Violet
  * @version  1.2 10/27/98
  */
 public class JTreeTable extends JTable {
 
     //~ Instance fields --------------------------------------------------------
 
     /** A subclass of JTree. */
     protected TreeTableCellRenderer tree;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new JTreeTable object.
      *
      * @param  treeTableModel  DOCUMENT ME!
      */
     public JTreeTable(final TreeTableModel treeTableModel) {
         super();
 
         // Create the tree. It will be used as a renderer and editor.
         tree = new TreeTableCellRenderer(treeTableModel);
 
         // Install a tableModel representing the visible rows in the tree.
         super.setModel(new TreeTableModelAdapter(treeTableModel, tree));
 
         // Force the JTable and JTree to share their row selection models.
         final ListToTreeSelectionModelWrapper selectionWrapper = new ListToTreeSelectionModelWrapper();
         tree.setSelectionModel(selectionWrapper);
         setSelectionModel(selectionWrapper.getListSelectionModel());
 
         // Install the tree editor renderer and editor.
         setDefaultRenderer(TreeTableModel.class, tree);
         setDefaultEditor(TreeTableModel.class, new TreeTableCellEditor());
 
         // No grid.
         setShowGrid(true);
 
         // No intercell spacing
         setIntercellSpacing(new Dimension(1, 1));
 
         // And update the height of the trees row to match that of
         // the table.
         if (tree.getRowHeight() < 1) {
             // Metal looks better like this.
             setRowHeight(18);
         }
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public TableModel getTableModel() {
         return super.getModel();
     }
 
     /**
      * Overridden to message super and forward the method to the tree. Since the tree is not actually in the component
      * hieachy it will never receive this unless we forward it in this manner.
      */
     @Override
     public void updateUI() {
         super.updateUI();
         if (tree != null) {
             tree.updateUI();
         }
         // Use the tree's default foreground and background colors in the
         // table.
         LookAndFeel.installColorsAndFont(this, "Tree.background", // NOI18N
             "Tree.foreground", "Tree.font"); // NOI18N
     }
 
     /* Workaround for BasicTableUI anomaly. Make sure the UI never tries to
      * paint the editor. The UI currently uses different techniques to paint the renderers and editors and overriding
      * setBounds() below is not the right thing to do for an editor. Returning -1 for the editing row in this case,
      * ensures the editor is never painted.
      */
     @Override
     public int getEditingRow() {
         return (getColumnClass(editingColumn) == TreeTableModel.class) ? -1 : editingRow;
     }
 
     /**
      * Overridden to pass the new rowHeight to the tree.
      *
      * @param  rowHeight  DOCUMENT ME!
      */
     @Override
     public void setRowHeight(final int rowHeight) {
         super.setRowHeight(rowHeight);
         if ((tree != null) && (tree.getRowHeight() != rowHeight)) {
             tree.setRowHeight(getRowHeight());
         }
     }
 
     /**
      * Returns the tree that is being shared between the model.
      *
      * @return  DOCUMENT ME!
      */
     public JTree getTree() {
         return tree;
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * A TreeCellRenderer that displays a JTree.
      *
      * @version  $Revision$, $Date$
      */
     public class TreeTableCellRenderer extends JTree implements TableCellRenderer {
 
         //~ Instance fields ----------------------------------------------------
 
         /** Last table/tree row asked to renderer. */
         protected int visibleRow;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new TreeTableCellRenderer object.
          *
          * @param  model  DOCUMENT ME!
          */
         public TreeTableCellRenderer(final TreeModel model) {
             super(model);
         }
 
         //~ Methods ------------------------------------------------------------
 
         /**
          * updateUI is overridden to set the colors of the Tree's renderer to match that of the table.
          */
         @Override
         public void updateUI() {
             super.updateUI();
             // Make the tree's cell renderer use the table's cell selection
             // colors.
             final TreeCellRenderer tcr = getCellRenderer();
             if (tcr instanceof DefaultTreeCellRenderer) {
                 final DefaultTreeCellRenderer dtcr = ((DefaultTreeCellRenderer)tcr);
                 // For 1.1 uncomment this, 1.2 has a bug that will cause an
                 // exception to be thrown if the border selection color is
                 // null.
                 // dtcr.setBorderSelectionColor(null);
                 dtcr.setTextSelectionColor(UIManager.getColor("Table.selectionForeground"));       // NOI18N
                 dtcr.setBackgroundSelectionColor(UIManager.getColor("Table.selectionBackground")); // NOI18N
             }
         }
 
         /**
          * Sets the row height of the tree, and forwards the row height to the table.
          *
          * @param  rowHeight  DOCUMENT ME!
          */
         @Override
         public void setRowHeight(final int rowHeight) {
             if (rowHeight > 0) {
                 super.setRowHeight(rowHeight);
                 if ((JTreeTable.this != null)
                             && (JTreeTable.this.getRowHeight() != rowHeight)) {
                     JTreeTable.this.setRowHeight(getRowHeight());
                 }
             }
         }
 
         /**
          * This is overridden to set the height to match that of the JTable.
          *
          * @param  x  DOCUMENT ME!
          * @param  y  DOCUMENT ME!
          * @param  w  DOCUMENT ME!
          * @param  h  DOCUMENT ME!
          */
         @Override
         public void setBounds(final int x, final int y, final int w, final int h) {
             super.setBounds(x, 0, w, JTreeTable.this.getHeight());
         }
 
         /**
          * Sublcassed to translate the graphics such that the last visible row will be drawn at 0,0.
          *
          * @param  g  DOCUMENT ME!
          */
         @Override
         public void paint(final Graphics g) {
             g.translate(0, -visibleRow * getRowHeight());
             super.paint(g);
         }
 
         /**
          * TreeCellRenderer method. Overridden to update the visible row.
          *
          * @param   table       DOCUMENT ME!
          * @param   value       DOCUMENT ME!
          * @param   isSelected  DOCUMENT ME!
          * @param   hasFocus    DOCUMENT ME!
          * @param   row         DOCUMENT ME!
          * @param   column      DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         @Override
         public Component getTableCellRendererComponent(final JTable table,
                 final Object value,
                 final boolean isSelected,
                 final boolean hasFocus,
                 final int row,
                 final int column) {
             if (isSelected) {
                 setBackground(table.getSelectionBackground());
             } else {
                 setBackground(table.getBackground());
             }
 
             visibleRow = row;
             return this;
         }
     }
 
     /**
      * TreeTableCellEditor implementation. Component returned is the JTree.
      *
      * @version  $Revision$, $Date$
      */
     public class TreeTableCellEditor extends AbstractCellEditor implements TableCellEditor {
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public Component getTableCellEditorComponent(final JTable table,
                 final Object value,
                 final boolean isSelected,
                 final int r,
                 final int c) {
             return tree;
         }
 
         /**
          * Overridden to return false, and if the event is a mouse event it is forwarded to the tree.
          *
          * <p>The behavior for this is debatable, and should really be offered as a property. By returning false, all
          * keyboard actions are implemented in terms of the table. By returning true, the tree would get a chance to do
          * something with the keyboard events. For the most part this is ok. But for certain keys, such as left/right,
          * the tree will expand/collapse where as the table focus should really move to a different column. Page up/down
          * should also be implemented in terms of the table. By returning false this also has the added benefit that
          * clicking outside of the bounds of the tree node, but still in the tree column will select the row, whereas if
          * this returned true that wouldn't be the case.</p>
          *
          * <p>By returning false we are also enforcing the policy that the tree will never be editable (at least by a
          * key sequence).</p>
          *
          * @param   e  DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         @Override
         public boolean isCellEditable(final EventObject e) {
             if (e instanceof MouseEvent) {
                 for (int counter = getColumnCount() - 1; counter >= 0; counter--) {
                     if (getColumnClass(counter) == TreeTableModel.class) {
                         final MouseEvent me = (MouseEvent)e;
                         final MouseEvent newME = new MouseEvent(
                                 tree,
                                 me.getID(),
                                 me.getWhen(),
                                 me.getModifiers(),
                                 me.getX()
                                         - getCellRect(0, counter, true).x,
                                 me.getY(),
                                 me.getClickCount(),
                                 me.isPopupTrigger());
                         tree.dispatchEvent(newME);
                         break;
                     }
                 }
             }
             return false;
         }
     }
 
     /**
      * ListToTreeSelectionModelWrapper extends DefaultTreeSelectionModel to listen for changes in the ListSelectionModel
      * it maintains. Once a change in the ListSelectionModel happens, the paths are updated in the
      * DefaultTreeSelectionModel.
      *
      * @version  $Revision$, $Date$
      */
     class ListToTreeSelectionModelWrapper extends DefaultTreeSelectionModel {
 
         //~ Instance fields ----------------------------------------------------
 
         /** Set to true when we are updating the ListSelectionModel. */
         protected boolean updatingListSelectionModel;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new ListToTreeSelectionModelWrapper object.
          */
         public ListToTreeSelectionModelWrapper() {
             super();
             getListSelectionModel().addListSelectionListener(createListSelectionListener());
         }
 
         //~ Methods ------------------------------------------------------------
 
         /**
          * Returns the list selection model. ListToTreeSelectionModelWrapper listens for changes to this model and
          * updates the selected paths accordingly.
          *
          * @return  DOCUMENT ME!
          */
         ListSelectionModel getListSelectionModel() {
             return listSelectionModel;
         }
 
         /**
          * This is overridden to set <code>updatingListSelectionModel</code> and message super. This is the only place
          * DefaultTreeSelectionModel alters the ListSelectionModel.
          */
         @Override
         public void resetRowSelection() {
             if (!updatingListSelectionModel) {
                 updatingListSelectionModel = true;
                 try {
                     super.resetRowSelection();
                 } finally {
                     updatingListSelectionModel = false;
                 }
             }
             // Notice how we don't message super if
             // updatingListSelectionModel is true. If
             // updatingListSelectionModel is true, it implies the
             // ListSelectionModel has already been updated and the
             // paths are the only thing that needs to be updated.
         }
 
         /**
          * Creates and returns an instance of ListSelectionHandler.
          *
          * @return  DOCUMENT ME!
          */
         protected ListSelectionListener createListSelectionListener() {
             return new ListSelectionHandler();
         }
 
         /**
          * If <code>updatingListSelectionModel</code> is false, this will reset the selected paths from the selected
          * rows in the list selection model.
          */
         protected void updateSelectedPathsFromSelectedRows() {
             if (!updatingListSelectionModel) {
                 updatingListSelectionModel = true;
                 try {
                     // This is way expensive, ListSelectionModel needs an
                     // enumerator for iterating.
                     final int min = listSelectionModel.getMinSelectionIndex();
                     final int max = listSelectionModel.getMaxSelectionIndex();
 
                     clearSelection();
                     if ((min != -1) && (max != -1)) {
                         for (int counter = min; counter <= max; counter++) {
                             if (listSelectionModel.isSelectedIndex(counter)) {
                                 final TreePath selPath = tree.getPathForRow(counter);
 
                                 if (selPath != null) {
                                     addSelectionPath(selPath);
                                 }
                             }
                         }
                     }
                 } finally {
                     updatingListSelectionModel = false;
                 }
             }
         }
 
         //~ Inner Classes ------------------------------------------------------
 
         /**
          * Class responsible for calling updateSelectedPathsFromSelectedRows when the selection of the list changse.
          *
          * @version  $Revision$, $Date$
          */
         class ListSelectionHandler implements ListSelectionListener {
 
             //~ Methods --------------------------------------------------------
 
             @Override
             public void valueChanged(final ListSelectionEvent e) {
                 updateSelectedPathsFromSelectedRows();
             }
         }
     }
 }
