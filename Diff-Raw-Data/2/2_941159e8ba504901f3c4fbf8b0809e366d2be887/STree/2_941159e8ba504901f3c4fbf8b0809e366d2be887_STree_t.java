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
 package org.wings;
 
 import java.awt.Rectangle;
 import java.awt.datatransfer.Transferable;
 import java.util.*;
 
 import javax.swing.event.EventListenerList;
 import javax.swing.event.TreeExpansionEvent;
 import javax.swing.event.TreeExpansionListener;
 import javax.swing.event.TreeModelEvent;
 import javax.swing.event.TreeModelListener;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.event.TreeWillExpandListener;
 import javax.swing.tree.AbstractLayoutCache;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.ExpandVetoException;
 import javax.swing.tree.TreeModel;
 import javax.swing.tree.TreePath;
 import javax.swing.tree.TreeSelectionModel;
 import javax.swing.tree.VariableHeightLayoutCache;
 import javax.swing.*;
 
 import org.wings.event.SMouseEvent;
 import org.wings.event.SMouseListener;
 import org.wings.event.SViewportChangeEvent;
 import org.wings.event.SViewportChangeListener;
 import org.wings.plaf.TreeCG;
 import org.wings.tree.SDefaultTreeSelectionModel;
 import org.wings.tree.STreeCellRenderer;
 import org.wings.tree.STreeSelectionModel;
 import org.wings.sdnd.TextAndHTMLTransferable;
 import org.wings.sdnd.CustomDragHandler;
 import org.wings.sdnd.CustomDropStayHandler;
 import org.wings.sdnd.SDropMode;
 
 /**
  * Swing-like tree widget.
  *
  * @author <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
  */
 public class STree extends SComponent implements Scrollable, LowLevelEventListener {
     /**
      * Tree selection model.
      * @see STreeSelectionModel#setSelectionMode(int)
      * @see TreeSelectionModel#SINGLE_TREE_SELECTION
      */
     public static final int SINGLE_TREE_SELECTION = TreeSelectionModel.SINGLE_TREE_SELECTION;
 
     /**
      * Tree selection model.
      * @see STreeSelectionModel#setSelectionMode(int)
      * @see TreeSelectionModel#CONTIGUOUS_TREE_SELECTION
      */
     public static final int CONTIGUOUS_TREE_SELECTION = TreeSelectionModel.CONTIGUOUS_TREE_SELECTION;
 
     /**
      * Tree selection model.
      * @see STreeSelectionModel#setSelectionMode(int)
      * @see TreeSelectionModel#DISCONTIGUOUS_TREE_SELECTION
      */
     public static final int DISCONTIGUOUS_TREE_SELECTION = TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION;
 
     /**
      * Indent depth in pixels
      */
     private int nodeIndentDepth = 20;
     private String[] lowLevelEvents;
 
     /**
      * Creates and returns a sample TreeModel. Used primarily for beanbuilders.
      * to show something interesting.
      *
      * @return the default TreeModel
      */
     protected static TreeModel getDefaultTreeModel() {
         DefaultMutableTreeNode root = new DefaultMutableTreeNode("STree");
         DefaultMutableTreeNode parent;
 
         parent = new DefaultMutableTreeNode("colors");
         root.add(parent);
         parent.add(new DefaultMutableTreeNode("blue"));
         parent.add(new DefaultMutableTreeNode("violet"));
         parent.add(new DefaultMutableTreeNode("red"));
         parent.add(new DefaultMutableTreeNode("yellow"));
 
         parent = new DefaultMutableTreeNode("sports");
         root.add(parent);
         parent.add(new DefaultMutableTreeNode("basketball"));
         parent.add(new DefaultMutableTreeNode("soccer"));
         parent.add(new DefaultMutableTreeNode("football"));
         parent.add(new DefaultMutableTreeNode("hockey"));
 
         parent = new DefaultMutableTreeNode("food");
         root.add(parent);
         parent.add(new DefaultMutableTreeNode("hot dogs"));
         parent.add(new DefaultMutableTreeNode("pizza"));
         parent.add(new DefaultMutableTreeNode("ravioli"));
         parent.add(new DefaultMutableTreeNode("bananas"));
         return new DefaultTreeModel(root);
     }
 
     protected TreeModel model;
 
     transient protected TreeModelListener treeModelListener;
 
     protected STreeCellRenderer renderer;
 
     protected STreeSelectionModel selectionModel;
 
     /**
      * store here all delayed expansion events
      */
     private ArrayList delayedExpansionEvents;
 
     /**
      * store here expansion paths that will be processed after procession the
      * request.
      */
     protected final ArrayList requestedExpansionPaths = new ArrayList();
 
     protected transient AbstractLayoutCache treeState = new VariableHeightLayoutCache();
 
     /**
      * Implementation of the {@link Scrollable} interface.
      */
     protected Rectangle viewport;
 
     /** @see LowLevelEventListener#isEpochCheckEnabled() */
     protected boolean epochCheckEnabled = true;
 
 
     /**
      * used to forward selection events to selection listeners of the tree
      */
     private final TreeSelectionListener fwdSelectionEvents = new TreeSelectionListener() {
 
         public void valueChanged(TreeSelectionEvent e) {
             fireTreeSelectionEvent(e);
 
             if (isUpdatePossible() && STree.class.isAssignableFrom(STree.this.getClass())) {
                 TreePath[] affectedPaths = e.getPaths();
                 List deselectedRows = new ArrayList();
                 List selectedRows = new ArrayList();
 
                 for (int i = 0; i < affectedPaths.length; ++i) {
                     int row = treeState.getRowForPath(affectedPaths[i]);
                     if (row == -1)
                         continue;
                     int visibleRow = row;
                     if (getViewportSize() != null) {
                         visibleRow = row - getViewportSize().y;
                         if (visibleRow < 0 || visibleRow >= getViewportSize().height)
                             continue;
                     }
                     
                     if (e.isAddedPath(affectedPaths[i])) {
                         selectedRows.add(new Integer(visibleRow));
                     } else {
                         deselectedRows.add(new Integer(visibleRow));
                     }
                 }
                 update(((TreeCG) getCG()).getSelectionUpdate(STree.this, deselectedRows, selectedRows));
             } else {
                 reload();
             }
         }
     };
 
     public STree(TreeModel model) {
         super();
         setModel(model);
         setRootVisible(true);
         setSelectionModel(new SDefaultTreeSelectionModel());
         installTransferHandler();
         createActionMap();
     }
 
     public STree() {
         this(getDefaultTreeModel());
     }
 
     public void addTreeSelectionListener(TreeSelectionListener tsl) {
         addEventListener(TreeSelectionListener.class, tsl);
     }
 
     public void removeTreeSelectionListener(TreeSelectionListener tsl) {
         removeEventListener(TreeSelectionListener.class, tsl);
     }
 
     protected void fireTreeSelectionEvent(TreeSelectionEvent e) {
         // Guaranteed to return a non-null array
         Object[] listeners = getListenerList();
         // Process the listeners last to first, notifying
         // those that are interested in this event
         for (int i = listeners.length - 2; i >= 0; i -= 2) {
             if (listeners[i] == TreeSelectionListener.class) {
                 ((TreeSelectionListener) listeners[i + 1]).valueChanged(e);
             }
         }
     }
 
     /**
      * Adds a listener for <code>TreeWillExpand</code> events.
      *
      * @param tel a <code>TreeWillExpandListener</code> that will be notified
      *            when a tree node will be expanded or collapsed (a "negative
      *            expansion")
      */
     public void addTreeWillExpandListener(TreeWillExpandListener tel) {
         addEventListener(TreeWillExpandListener.class, tel);
     }
 
     /**
      * Removes a listener for <code>TreeWillExpand</code> events.
      *
      * @param tel the <code>TreeWillExpandListener</code> to remove
      */
     public void removeTreeWillExpandListener(TreeWillExpandListener tel) {
         removeEventListener(TreeWillExpandListener.class, tel);
     }
 
 
     /**
      * Notifies all listeners that have registered interest for
      * notification on this event type.  The event instance
      * is lazily created using the <code>path</code> parameter.
      *
      * @param path the <code>TreePath</code> indicating the node that was
      *             expanded
      * @see EventListenerList
      */
     public void fireTreeWillExpand(TreePath path, boolean expand)
             throws ExpandVetoException {
 
         // Guaranteed to return a non-null array
         Object[] listeners = getListenerList();
         TreeExpansionEvent e = null;
         // Process the listeners last to first, notifying
         // those that are interested in this event
         for (int i = listeners.length - 2; i >= 0; i -= 2) {
             if (listeners[i] == TreeWillExpandListener.class) {
                 // Lazily create the event:
                 if (e == null)
                     e = new TreeExpansionEvent(this, path);
 
                 if (expand) {
                     ((TreeWillExpandListener) listeners[i + 1]).
                             treeWillExpand(e);
                 } else {
                     ((TreeWillExpandListener) listeners[i + 1]).
                             treeWillCollapse(e);
                 }
             }
         }
     }
 
     public void addTreeExpansionListener(TreeExpansionListener tel) {
         addEventListener(TreeExpansionListener.class, tel);
     }
 
     public void removeTreeExpansionListener(TreeExpansionListener tel) {
         removeEventListener(TreeExpansionListener.class, tel);
     }
 
 
     private static class DelayedExpansionEvent {
         TreeExpansionEvent expansionEvent;
         boolean expansion;
 
         DelayedExpansionEvent(TreeExpansionEvent e, boolean b) {
             expansionEvent = e;
             expansion = b;
         }
 
     }
 
     protected void addDelayedExpansionEvent(TreeExpansionEvent e,
                                             boolean expansion) {
         if (delayedExpansionEvents == null) {
             delayedExpansionEvents = new ArrayList();
         }
 
         delayedExpansionEvents.add(new DelayedExpansionEvent(e, expansion));
     }
 
     protected void fireDelayedExpansionEvents() {
         if (delayedExpansionEvents != null &&
                 !getSelectionModel().getDelayEvents()) {
             for (int i = 0; i < delayedExpansionEvents.size(); i++) {
                 DelayedExpansionEvent e =
                         (DelayedExpansionEvent) delayedExpansionEvents.get(i);
 
                 fireTreeExpansionEvent(e.expansionEvent, e.expansion);
             }
             delayedExpansionEvents.clear();
         }
     }
 
 
     /**
      * Notify all listeners that have registered interest for
      * notification on this event type.  The event instance
      * is lazily created using the parameters passed into
      * the fire method.
      *
      * @param path the TreePath indicating the node that was expanded
      * @see EventListenerList
      */
     public void fireTreeExpanded(TreePath path) {
         fireTreeExpansionEvent(new TreeExpansionEvent(this, path), true);
     }
 
     protected void fireTreeExpansionEvent(TreeExpansionEvent e, boolean expansion) {
         if (getSelectionModel().getDelayEvents()) {
             addDelayedExpansionEvent(e, expansion);
         } else {
             // Guaranteed to return a non-null array
             Object[] listeners = getListenerList();
             // Process the listeners last to first, notifying
             // those that are interested in this event
             for (int i = listeners.length - 2; i >= 0; i -= 2) {
                 if (listeners[i] == TreeExpansionListener.class) {
                     if (expansion)
                         ((TreeExpansionListener) listeners[i + 1]).treeExpanded(e);
                     else
                         ((TreeExpansionListener) listeners[i + 1]).treeCollapsed(e);
                 }
             }
             fireViewportChanged(false);
         }
     }
 
     /**
      * Notify all listeners that have registered interest for
      * notification on this event type.  The event instance
      * is lazily created using the parameters passed into
      * the fire method.
      *
      * @param path the TreePath indicating the node that was collapsed
      * @see EventListenerList
      */
     public void fireTreeCollapsed(TreePath path) {
         fireTreeExpansionEvent(new TreeExpansionEvent(this, path), false);
     }
 
     /**
      * Adds the specified mouse listener to receive mouse events from
      * this component.
      * If l is null, no exception is thrown and no action is performed.
      *
      * @param l the component listener.
      * @see org.wings.event.SMouseEvent
      * @see org.wings.event.SMouseListener
      * @see org.wings.STable#removeMouseListener
      */
     public final void addMouseListener(SMouseListener l) {
         addEventListener(SMouseListener.class, l);
     }
 
     /**
      * Removes the specified mouse listener so that it no longer
      * receives mouse events from this component. This method performs
      * no function, nor does it throw an exception, if the listener
      * specified by the argument was not previously added to this component.
      * If l is null, no exception is thrown and no action is performed.
      *
      * @param l the component listener.
      * @see org.wings.event.SMouseEvent
      * @see org.wings.event.SMouseListener
      * @see org.wings.STable#addMouseListener
      */
     public final void removeMouseListener(SMouseListener l) {
         removeEventListener(SMouseListener.class, l);
     }
 
     /**
      * Reports a mouse click event.
      *
      * @param event report this event to all listeners
      * @see org.wings.event.SMouseListener
      */
     protected void fireMouseClickedEvent(SMouseEvent event) {
         Object[] listeners = getListenerList();
         for (int i = listeners.length - 2; i >= 0; i -= 2) {
             if (listeners[i] == SMouseListener.class) {
                 ((SMouseListener)listeners[i + 1]).mouseClicked(event);
             }
         }
     }
 
     protected void processRequestedExpansionPaths() {
         getSelectionModel().setDelayEvents(true);
 
         for (int i = 0; i < requestedExpansionPaths.size(); i++) {
             try {
                 TreePath path = (TreePath) requestedExpansionPaths.get(i);
                 togglePathExpansion(path);
             } catch (ExpandVetoException ex) {
                 // do not expand...
             }
         }
         requestedExpansionPaths.clear();
         getSelectionModel().setDelayEvents(false);
     }
 
     private int lastSelectedRow;
 
     protected void addSelectionEvent(int row, boolean ctrlKey, boolean shiftKey) {
         TreePath path = getPathForRow(row);
         if (path != null) {
             if(!shiftKey && !ctrlKey) {
                 getSelectionModel().clearSelection();
                 togglePathSelection(path);
 
                 lastSelectedRow = row;
             } else if(ctrlKey && !shiftKey) {
                 togglePathSelection(path);
 
                 lastSelectedRow = row;
             } else if(!ctrlKey && shiftKey) {
                 int start = lastSelectedRow;
                 int end = row;
                 if(start > end) {
                     int temp = end;
                     end = start;
                     start = temp;
                 }
                 getSelectionModel().clearSelection();
                 for(int temp=start; temp<=end; ++temp) {
                     getSelectionModel().addSelectionPath(getPathForRow(temp));
                 }
             }
         }
     }
 
     public void fireIntermediateEvents() {
         getSelectionModel().setDelayEvents(true);
         for (int i = 0; i < lowLevelEvents.length; i++) {
             String values = lowLevelEvents[i];
             if (values.length() < 2) continue; // incorrect format
 
             String[] params = values.split(";");
 
             boolean ctrlKey = false;
             boolean shiftKey = false;
             for(int j=1; j<params.length; ++j) {
                 String[] tempVals = params[j].split("=");
                 if("ctrlKey".equals(tempVals[0])) {
                     ctrlKey = Boolean.parseBoolean(tempVals[1]);
                 } else if("shiftKey".equals(tempVals[0])) {
                     shiftKey = Boolean.parseBoolean(tempVals[1]);
                 }
             }
 
             String value = params[0];
             SPoint point = new SPoint(value.substring(1));
             int row = getRowForLocation(point);
             if (row < 0) continue; // row not found...
 
             TreePath path;
 
             switch (value.charAt(0)) {
                 case 'b':
                     SMouseEvent event = new SMouseEvent(this, 0, point);
                     fireMouseClickedEvent(event);
                     if (event.isConsumed())
                         continue;
 
                     addSelectionEvent(row, ctrlKey, shiftKey);
                     break;
                 case 'a':
                     event = new SMouseEvent(this, 0, point);
                     fireMouseClickedEvent(event);
                     if (event.isConsumed())
                         continue;
 
                     path = getPathForAbsoluteRow(row);
                     if (path != null) {
                         togglePathSelection(path);
                     }
                     break;
                 case 'h':
                     path = getPathForRow(row);
                     if (path != null) {
                         requestedExpansionPaths.add(path);
                     }
                     break;
                 case 'j':
                     path = getPathForAbsoluteRow(row);
                     //selection
                     if (path != null) {
                         requestedExpansionPaths.add(path);
                     }
                     break;
             }
         }
         getSelectionModel().setDelayEvents(false);
 
         processRequestedExpansionPaths();
         getSelectionModel().fireDelayedIntermediateEvents();
     }
 
     /**
      * Returns the table row for the passed <code>SPoint</code>
      * instance received via {@link #addMouseListener(org.wings.event.SMouseListener)}.
      * @param point The pointed retuned by the mouse event.
      * @return The row index
      */
     public int rowAtPoint(SPoint point) {
         return getRowForLocation(point);
     }
 
     /**
      * Returns the tree row for the passed <code>SPoint</code>.
      * instance received via {@link #addMouseListener(org.wings.event.SMouseListener)}.
      * @param point The pointed returned by the mouse event.
      * @return The tree row index
      */
     public int getRowForLocation(SPoint point) {
         return Integer.parseInt(point.getCoordinates());
     }
 
     public void fireFinalEvents() {
         super.fireFinalEvents();
         fireDelayedExpansionEvents();
         getSelectionModel().fireDelayedFinalEvents();
     }
 
     /** @see LowLevelEventListener#isEpochCheckEnabled() */
     public boolean isEpochCheckEnabled() {
         return epochCheckEnabled;
     }
 
     /** @see LowLevelEventListener#isEpochCheckEnabled() */
     public void setEpochCheckEnabled(boolean epochCheckEnabled) {
         boolean oldVal = this.epochCheckEnabled;
         this.epochCheckEnabled = epochCheckEnabled;
         propertyChangeSupport.firePropertyChange("epochCheckEnabled", oldVal, this.epochCheckEnabled);
     }
 
     public void setRootVisible(boolean rootVisible) {
         if (isRootVisible() != rootVisible) {
             boolean oldVal = treeState.isRootVisible();
             treeState.setRootVisible(rootVisible);
             fireViewportChanged(false);
             reload();
             propertyChangeSupport.firePropertyChange("rootVisible", oldVal, treeState.isRootVisible());
         }
     }
 
 
     public boolean isRootVisible() {
         return treeState.isRootVisible();
     }
 
 
     public void setModel(TreeModel m) {
         TreeModel oldVal = this.model;
         if (model != null && treeModelListener != null)
             model.removeTreeModelListener(treeModelListener);
         model = m;
         treeState.setModel(m);
 
         if (model != null) {
             if (treeModelListener == null)
                 treeModelListener = createTreeModelListener();
 
             if (treeModelListener != null)
                 model.addTreeModelListener(treeModelListener);
 
             // Mark the root as expanded, if it isn't a leaf.
             if (!model.isLeaf(model.getRoot()))
                 treeState.setExpandedState(new TreePath(model.getRoot()), true);
 
             fireViewportChanged(false);
             reload();
         }
         propertyChangeSupport.firePropertyChange("model", oldVal, this.model);
     }
 
 
     public TreeModel getModel() {
         return model;
     }
 
 
     public int getRowCount() {
         return treeState.getRowCount();
     }
 
 
     public TreePath getPathForRow(int row) {
         return treeState.getPathForRow(row);
     }
     
     public int getRowForPath(TreePath path) {
         return treeState.getRowForPath(path);
     }
 
     protected int fillPathForAbsoluteRow(int row, Object node, ArrayList path) {
         // and check if it is the
         if (row == 0) {
             return 0;
         } // end of if ()
 
         for (int i = 0; i < model.getChildCount(node); i++) {
             path.add(model.getChild(node, i));
             row = fillPathForAbsoluteRow(row - 1, model.getChild(node, i), path);
             if (row == 0) {
                 return 0;
             } // end of if ()
             path.remove(path.size() - 1);
         }
         return row;
     }
 
     public TreePath getPathForAbsoluteRow(int row) {
         // fill path in this array
         ArrayList path = new ArrayList(10);
 
         path.add(model.getRoot());
         fillPathForAbsoluteRow(row, model.getRoot(), path);
 
         return new TreePath(path.toArray());
     }
 
     /**
      * Sets the tree's selection model. When a null value is specified
      * an empty electionModel is used, which does not allow selections.
      *
      * @param selectionModel the TreeSelectionModel to use, or null to
      *                       disable selections
      * @see TreeSelectionModel
      */
     public void setSelectionModel(STreeSelectionModel selectionModel) {
         STreeSelectionModel oldVal = this.selectionModel;
 
         if (this.selectionModel != null)
             this.selectionModel.removeTreeSelectionListener(fwdSelectionEvents);
 
        if (selectionModel != null && selectionModel != SDefaultTreeSelectionModel.NO_SELECTION_MODEL)
             selectionModel.addTreeSelectionListener(fwdSelectionEvents);
 
         if (selectionModel == null)
             this.selectionModel = SDefaultTreeSelectionModel.NO_SELECTION_MODEL;
         else
             this.selectionModel = selectionModel;
 
         propertyChangeSupport.firePropertyChange("selectionModel", oldVal, this.selectionModel);
     }
 
     /**
      * Returns the model for selections. This should always return a
      * non-null value. If you don't want to allow anything to be selected
      * set the selection model to null, which forces an empty
      * selection model to be used.
      *
      * @see #setSelectionModel
      */
     public STreeSelectionModel getSelectionModel() {
         return selectionModel;
     }
 
     /**
      * Returns JTreePath instances representing the path between index0
      * and index1 (including index1).
      *
      * @param index0 an int specifying a display row, where 0 is the
      *               first row in the display
      * @param index1 an int specifying a second display row
      * @return an array of TreePath objects, one for each node between
      *         index0 and index1, inclusive
      */
     protected TreePath[] getPathBetweenRows(int index0, int index1) {
         int newMinIndex = Math.min(index0, index1);
         int newMaxIndex = Math.max(index0, index1);
 
         TreePath[] selection = new TreePath[newMaxIndex - newMinIndex + 1];
 
         for (int i = newMinIndex; i <= newMaxIndex; i++) {
             selection[i - newMinIndex] = getPathForRow(i);
         }
 
         return selection;
     }
 
     /**
      * Selects the node identified by the specified path.  If any
      * component of the path is hidden (under a collapsed node), it is
      * exposed (made viewable).
      *
      * @param path the TreePath specifying the node to select
      */
     public void setSelectionPath(TreePath path) {
         TreePath oldVal = getSelectionModel().getSelectionPath();
         getSelectionModel().setSelectionPath(path);
         propertyChangeSupport.firePropertyChange("selectionPath", oldVal, getSelectionModel().getSelectionPath());
     }
 
     /**
      * Selects the nodes identified by the specified array of paths.
      * If any component in any of the paths is hidden (under a collapsed
      * node), it is exposed (made viewable).
      *
      * @param paths an array of TreePath objects that specifies the nodes
      *              to select
      */
     public void setSelectionPaths(TreePath[] paths) {
         TreePath[] oldVal = getSelectionModel().getSelectionPaths();
         getSelectionModel().setSelectionPaths(paths);
         propertyChangeSupport.firePropertyChange("selectionPaths", oldVal, getSelectionModel().getSelectionPaths());
     }
 
     /**
      * Selects the node at the specified row in the display.
      *
      * @param row the row to select, where 0 is the first row in
      *            the display
      */
     public void setSelectionRow(int row) {
         int[] rows = {row};
         setSelectionRows(rows);
     }
 
     /**
      * Selects the nodes corresponding to each of the specified rows
      * in the display.
      *
      * @param rows an array of ints specifying the rows to select,
      *             where 0 indicates the first row in the display
      */
     public void setSelectionRows(int[] rows) {
         if (rows == null)
             return;
 
         TreePath paths[] = new TreePath[rows.length];
         for (int i = 0; i < rows.length; i++) {
             paths[i] = getPathForRow(rows[i]);
         }
 
         setSelectionPaths(paths);
     }
 
     /**
      * Adds the node identified by the specified TreePath to the current
      * selection. If any component of the path isn't visible, it is
      * made visible.
      *
      * @param path the TreePath to add
      */
     public void addSelectionPath(TreePath path) {
         getSelectionModel().addSelectionPath(path);
     }
 
     /**
      * Adds each path in the array of paths to the current selection.  If
      * any component of any of the paths isn't visible, it is
      * made visible.
      *
      * @param paths an array of TreePath objects that specifies the nodes
      *              to add
      */
     public void addSelectionPaths(TreePath[] paths) {
         getSelectionModel().addSelectionPaths(paths);
     }
 
     /**
      * Adds the path at the specified row to the current selection.
      *
      * @param row an int specifying the row of the node to add,
      *            where 0 is the first row in the display
      */
     public void addSelectionRow(int row) {
         int[] rows = {row};
         addSelectionRows(rows);
     }
 
     /**
      * Adds the paths at each of the specified rows to the current selection.
      *
      * @param rows an array of ints specifying the rows to add,
      *             where 0 indicates the first row in the display
      */
     public void addSelectionRows(int[] rows) {
         if (rows != null) {
             int numRows = rows.length;
             TreePath[] paths = new TreePath[numRows];
 
             for (int counter = 0; counter < numRows; counter++)
                 paths[counter] = getPathForRow(rows[counter]);
             addSelectionPaths(paths);
         }
     }
 
     /**
      * Returns the last path component in the first node of the current
      * selection.
      *
      * @return the last Object in the first selected node's TreePath,
      *         or null if nothing is selected
      * @see TreePath#getLastPathComponent
      */
     public Object getLastSelectedPathComponent() {
         Object obj = null;
         TreePath selPath = getSelectionModel().getSelectionPath();
         if (selPath != null) {
             obj = selPath.getLastPathComponent();
         }
         return obj;
     }
 
     /**
      * Returns the path to the first selected node.
      *
      * @return the TreePath for the first selected node, or null if
      *         nothing is currently selected
      */
     public TreePath getSelectionPath() {
         return getSelectionModel().getSelectionPath();
     }
 
     /**
      * Returns the paths of all selected values.
      *
      * @return an array of TreePath objects indicating the selected
      *         nodes, or null if nothing is currently selected.
      */
     public TreePath[] getSelectionPaths() {
         return getSelectionModel().getSelectionPaths();
     }
 
     /**
      * Returns all of the currently selected rows.
      *
      * @return an array of ints that identifies all currently selected rows
      *         where 0 is the first row in the display
      */
     public int[] getSelectionRows() {
         return getSelectionModel().getSelectionRows();
     }
 
     /**
      * Returns the number of nodes selected.
      *
      * @return the number of nodes selected
      */
     public int getSelectionCount() {
         return selectionModel.getSelectionCount();
     }
 
     /**
      * Gets the first selected row.
      *
      * @return an int designating the first selected row, where 0 is the
      *         first row in the display
      */
     public int getMinSelectionRow() {
         return getSelectionModel().getMinSelectionRow();
     }
 
     /**
      * Gets the last selected row.
      *
      * @return an int designating the last selected row, where 0 is the
      *         first row in the display
      */
     public int getMaxSelectionRow() {
         return getSelectionModel().getMaxSelectionRow();
     }
 
     /**
      * Returns the row index of the last node added to the selection.
      *
      * @return an int giving the row index of the last node added to the
      *         selection, where 0 is the first row in the display
      */
     public int getLeadSelectionRow() {
         return getSelectionModel().getLeadSelectionRow();
     }
 
     /**
      * Returns the path of the last node added to the selection.
      *
      * @return the TreePath of the last node added to the selection.
      */
     public TreePath getLeadSelectionPath() {
         return getSelectionModel().getLeadSelectionPath();
     }
 
     /**
      * Returns true if the item identified by the path is currently selected.
      *
      * @param path a TreePath identifying a node
      * @return true if the node is selected
      */
     public boolean isPathSelected(TreePath path) {
         return getSelectionModel().isPathSelected(path);
     }
 
     /**
      * Returns true if the node identitifed by row is selected.
      *
      * @param row an int specifying a display row, where 0 is the first
      *            row in the display
      * @return true if the node is selected
      */
     public boolean isRowSelected(int row) {
         return getSelectionModel().isRowSelected(row);
     }
 
     /**
      * Removes the nodes between index0 and index1, inclusive, from the
      * selection.
      *
      * @param index0 an int specifying a display row, where 0 is the
      *               first row in the display
      * @param index1 an int specifying a second display row
      */
     public void removeSelectionInterval(int index0, int index1) {
         TreePath[] paths = getPathBetweenRows(index0, index1);
         this.getSelectionModel().removeSelectionPaths(paths);
     }
 
     /**
      * Removes the node identified by the specified path from the current
      * selection.
      *
      * @param path the TreePath identifying a node
      */
     public void removeSelectionPath(TreePath path) {
         getSelectionModel().removeSelectionPath(path);
     }
 
     /**
      * Removes the nodes identified by the specified paths from the
      * current selection.
      *
      * @param paths an array of TreePath objects that specifies the nodes
      *              to remove
      */
     public void removeSelectionPaths(TreePath[] paths) {
         getSelectionModel().removeSelectionPaths(paths);
     }
 
     /**
      * Removes the path at the index <code>row</code> from the current
      * selection.
      *
      * @param row the row identifying the node to remove
      */
     public void removeSelectionRow(int row) {
         int[] rows = {row};
         removeSelectionRows(rows);
     }
 
     public void removeSelectionRows(int[] rows) {
         TreePath[] paths = new TreePath[rows.length];
         for (int i = 0; i < rows.length; i++)
             paths[i] = getPathForRow(rows[i]);
         removeSelectionPaths(paths);
     }
 
     public int getMaximumExpandedDepth() {
         int max = 0;
         for (int i = 0; i < getRowCount(); i++)
             max = Math.max(max, getPathForRow(i).getPathCount());
         return max;
     }
 
     /**
      * Expand this tree row.
      * If tree is inside a {@link SScrollPane} try to
      * adjust pane, so that as much as possible new
      * nodes are visible.
      * @param p the TreePath to expand
      * @deprecated This method is deprecated and should not be used because 
      * expandPath(TreePath) is the proper method with the same functionality.
      */
     @Deprecated
     public void expandRow(TreePath p) {
         expandPath(p);
     }
     
     /**
      * Expand this tree row.
      * If tree is inside a {@link SScrollPane} try to
      * adjust pane, so that as much as possible new
      * nodes are visible.
      * @param p the TreePath to expand
      */
     public void expandPath(TreePath p) {
         treeState.setExpandedState(p, true);
 
         if (getViewportSize() != null) {
             Rectangle area = new Rectangle(getViewportSize());
             area.y = treeState.getRowForPath(p);
             area.height = model.getChildCount(p.getLastPathComponent()) + 1;
             scrollRectToVisible(area);
         }
 
         fireTreeExpanded(p);
         reload();
     }
 
     public void expandRow(int row) {
         expandPath(getPathForRow(row));
     }
 
     /**
      * Collapse this tree row.
      * If tree is inside a {@link SScrollPane} try to
      * adjust pane, so that as much as possible new
      * nodes are visible.
      * @param p the TreePath to expand
      * @deprecated This method is deprecated and should not be used because
      * collapsePath(TreePath) is the proper method with the same functionality.
      */
     @Deprecated
     public void collapseRow(TreePath p) {
         collapsePath(p);
     }
 
     public void collapsePath(TreePath p) {
         treeState.setExpandedState(p, false);
 
         fireTreeCollapsed(p);
         reload();
     }
 
     public void collapseRow(int row) {
         collapseRow(getPathForRow(row));
     }
 
     public boolean isVisible(TreePath path) {
         if (path != null) {
             TreePath parentPath = path.getParentPath();
 
             if (parentPath != null)
                 return isExpanded(parentPath);
 
             // Root.
             return true;
         }
 
         return false;
     }
 
     public boolean isExpanded(TreePath path) {
         return treeState.isExpanded(path);
     }
 
     protected void togglePathSelection(TreePath path) {
         if (path != null) {
             if (isPathSelected(path)) {
                 removeSelectionPath(path);
             } else {
                 addSelectionPath(path);
             }
         }
     }
 
     protected void togglePathExpansion(TreePath path)
             throws ExpandVetoException {
         if (path != null) {
             if (treeState.isExpanded(path)) {
                 fireTreeWillExpand(path, false);
                 collapseRow(path);
             } else {
                 fireTreeWillExpand(path, true);
                 expandRow(path);
             }
         }
     }
 
     /**
      * This is for plafs only!
      * With this parameter the tree expands the given node
      */
     public String getExpansionParameter(int row, boolean absolute) {
         return (absolute ? "j" : "h") + row;
     }
 
     /**
      * This is for plafs only!
      * With this parameter the tree selects the given node
      */
     public String getSelectionParameter(int row, boolean absolute) {
         return (absolute ? "a" : "b") + row;
     }
 
 
     public void processLowLevelEvent(String action, String[] values) {
         processKeyEvents(values);
         if (action.endsWith("_keystroke"))
             return;
 
         this.lowLevelEvents = values;
         SForm.addArmedComponent(this);
     }
 
     /**
      * Set the indent depth in pixel between two nodes of a different level.
      * Note: only positive values apply, negative values are cut off at 0.
      * @param depth the depth to set
      */
     public void setNodeIndentDepth(int depth) {
         if (depth < 0) {
             depth = 0;
         }
         if (nodeIndentDepth != depth) {
             int oldVal = this.nodeIndentDepth;
             nodeIndentDepth = depth;
             reload();
             propertyChangeSupport.firePropertyChange("nodeIndentDepth", oldVal, this.nodeIndentDepth);
         }
     }
 
     public int getNodeIndentDepth() {
         return nodeIndentDepth;
     }
 
     public void setCellRenderer(STreeCellRenderer x) {
         STreeCellRenderer oldVal = this.renderer;
         renderer = x;
         propertyChangeSupport.firePropertyChange("renderer", oldVal, this.renderer);
     }
 
     public STreeCellRenderer getCellRenderer() {
         return renderer;
     }
 
     /**
      * Creates an instance of TreeModelHandler.
      */
     protected TreeModelListener createTreeModelListener() {
         return new TreeModelHandler();
     }
 
 
     /**
      * Listens to the model and updates the expandedState accordingly
      * when nodes are removed, or changed.
      */
     protected class TreeModelHandler implements TreeModelListener {
         public void treeNodesChanged(TreeModelEvent e) {
             if (e == null)
                 return;
             treeState.treeNodesChanged(e);
             reload();
         }
 
         public void treeNodesInserted(TreeModelEvent e) {
             if (e == null)
                 return;
             treeState.treeNodesInserted(e);
             fireViewportChanged(false);
             reload();
         }
 
         public void treeStructureChanged(TreeModelEvent e) {
             if (e == null)
                 return;
             treeState.treeStructureChanged(e);
             fireViewportChanged(false);
             reload();
         }
 
         public void treeNodesRemoved(TreeModelEvent e) {
             if (e == null)
                 return;
             treeState.treeNodesRemoved(e);
             fireViewportChanged(false);
             reload();
         }
     }
 
     public void setParent(SContainer p) {
         super.setParent(p);
         if (getCellRendererPane() != null)
             getCellRendererPane().setParent(p);
     }
 
     protected void setParentFrame(SFrame f) {
         super.setParentFrame(f);
         if (getCellRendererPane() != null)
             getCellRendererPane().setParentFrame(f);
     }
 
 
     // do not initalize with null!
     private SCellRendererPane cellRendererPane = new SCellRendererPane();
 
 
     public SCellRendererPane getCellRendererPane() {
         return cellRendererPane;
     }
 
     public void setCG(TreeCG cg) {
         super.setCG(cg);
     }
 
     /**
      * The size of the component in respect to scrollable units.
      */
     public Rectangle getScrollableViewportSize() {
         return new Rectangle(0, 0, 1, getRowCount());
     }
 
     /**
      * Returns the actual visible part of a scrollable.
      */
     public Rectangle getViewportSize() {
         return viewport;
     }
 
     /**
      * Sets the actual visible part of a scrollable.
      */
     public void setViewportSize(Rectangle newViewport) {
         Rectangle oldViewport = viewport;
         viewport = newViewport;
 
         if (isDifferent(oldViewport, newViewport)) {
             if (oldViewport == null || newViewport == null) {
                 fireViewportChanged(true);
                 fireViewportChanged(false);
             } else {
                 if (newViewport.x != oldViewport.x || newViewport.width != oldViewport.width) {
                     fireViewportChanged(true);
                 }
                 if (newViewport.y != oldViewport.y || newViewport.height != oldViewport.height) {
                     fireViewportChanged(false);
                 }
             }
             reload();
         }
 
         propertyChangeSupport.firePropertyChange("viewortSize", oldViewport, this.viewport);
     }
 
     /**
      * Adds the given <code>SViewportChangeListener</code> to the scrollable.
      *
      * @param l the listener to be added
      */
     public void addViewportChangeListener(SViewportChangeListener l) {
         addEventListener(SViewportChangeListener.class, l);
     }
 
     /**
      * Removes the given <code>SViewportChangeListener</code> from the scrollable.
      *
      * @param l the listener to be removed
      */
     public void removeViewportChangeListener(SViewportChangeListener l) {
         removeEventListener(SViewportChangeListener.class, l);
     }
 
     /**
      * Notifies all listeners that have registered interest for notification
      * on changes to this scrollable's viewport in the specified direction.
      *
      * @see EventListenerList
      */
     protected void fireViewportChanged(boolean horizontal) {
         Object[] listeners = getListenerList();
         for (int i = listeners.length - 2; i >= 0; i -= 2) {
             if (listeners[i] == SViewportChangeListener.class) {
                 SViewportChangeEvent event = new SViewportChangeEvent(this, horizontal);
                 ((SViewportChangeListener) listeners[i + 1]).viewportChanged(event);
             }
         }
     }
 
     /**
      * Drag and Drop stuff
      */
     private SDropMode dropMode = null;
     private boolean dragEnabled = false;
 
     protected void createActionMap() {
         ActionMap map = getActionMap();
 
         map.put(STransferHandler.getCutAction().getValue(Action.NAME), STransferHandler.getCutAction());
         map.put(STransferHandler.getCopyAction().getValue(Action.NAME), STransferHandler.getCopyAction());
         map.put(STransferHandler.getPasteAction().getValue(Action.NAME), STransferHandler.getPasteAction());
     }
 
     public static final class DropLocation extends STransferHandler.DropLocation {
         private int row = -1;
         private TreePath path = null;
 
         public DropLocation(STree tree, SPoint point) {
             super(point);
 
             try {
                 row = Integer.parseInt(point.getCoordinates());
                 path = tree.getPathForRow(row);
             } catch(Exception e) {
             }
         }
 
         public int getRow() {
             return row;
         }
 
         public TreePath getPath() {
             return path;
         }
     }
 
     public void setDropMode(SDropMode dropMode) {
         this.dropMode = dropMode;
 
         getSession().getSDragAndDropManager().addDropTarget(this);
     }
 
     public SDropMode getDropMode() {
         return this.dropMode;
     }
 
     protected DropLocation dropLocationForPoint(SPoint p) {
         if(p.getCoordinates() == null)
             return null;
         return new STree.DropLocation(this, p);
     }
 
     private void installTransferHandler() {
         if(getTransferHandler() == null) {
             setTransferHandler(new DefaultTransferHandler());
         }
     }
 
     public void setDragEnabled(boolean dragEnabled) {
         if(getSelectionModel() == null && dragEnabled == true)
             throw new IllegalStateException("Unable to enable DND - no selection mode set in " + this);
 
         if(dragEnabled != this.dragEnabled) {
             if(dragEnabled) {
                 this.getSession().getSDragAndDropManager().addDragSource(this);
             } else {
                 this.getSession().getSDragAndDropManager().removeDragSource(this);
             }
 
             this.dragEnabled = dragEnabled;
         }
     }
 
     public static class DefaultTransferHandler extends STransferHandler implements Comparator<TreePath>, CustomDragHandler, CustomDropStayHandler {
         private STree tree;
 
         public DefaultTransferHandler() {
             super(null);
         }
 
         public int compare(TreePath o1, TreePath o2) {
             return tree.getRowForPath(o1) - tree.getRowForPath(o2);
         }
 
         private TreePath[] getPathsOrdered(TreePath[] paths) {
             if(paths == null)
                 return new TreePath[0];
             
             List<TreePath> retPaths = Arrays.asList(paths);
             Collections.sort(retPaths, this);
 
             return retPaths.toArray(new TreePath[0]);
         }
 
         protected Transferable createTransferable(SComponent component) {
             tree = (STree)component;
             String htmlData = "<html><body><ul>";
             String plainTextData = "";
             TreePath[] selectedPaths = getPathsOrdered(tree.getSelectionPaths());
             
             for(TreePath path:selectedPaths) {
                 Object node = path.getLastPathComponent();
 
                 plainTextData += node.toString() + "\n";
                 htmlData += "<li>" + node.toString() + "</li>";
             }
 
             htmlData += "</ul></body></html>";
             return new TextAndHTMLTransferable(plainTextData, htmlData);
         }
 
         public int getSourceActions(SComponent component) {
             return COPY;
         }
 
         public boolean dragStart(SComponent source, SComponent target, int action, SMouseEvent event) {
             try {
                 String[] coords = event.getPoint().getCoordinates().split(":");
                 int row = Integer.parseInt(coords[0]);
                 if(coords.length < 3)
                     return false;
 
                 boolean ctrlKey = false;
                 boolean shiftKey = false;
                 for(int i=1; i<coords.length; ++i) {
                     String[] keyVal = coords[i].split("=");
                     if("ctrlKey".equals(keyVal[0])) {
                         ctrlKey = Boolean.parseBoolean(keyVal[1]);
                     } else if("shiftKey".equals(keyVal[0])) {
                         shiftKey = Boolean.parseBoolean(keyVal[1]);
                     }
                 }
 
                 if(row != -1) {
                     if(source instanceof STree) {
                         STree tree = (STree)source;
                         if(tree.isPathSelected(tree.getPathForRow(row)))
                             return false;
 
                         tree.addSelectionEvent(row, ctrlKey, shiftKey);
                     }
                 }
             } catch(Exception e) {
 
             }
             return false;
         }
 
         public void dropStay(SComponent source, SComponent target, int action, SMouseEvent event) {
             if(!(target instanceof STree))
                 return;
 
             String[] coords = event.getPoint().getCoordinates().split(":");
             int row = Integer.parseInt(coords[0]);
 
             STree tree = (STree)target;
             TreePath path = tree.getPathForRow(row);
             if(path != null && !tree.isExpanded(path)) {
                 tree.expandPath(path);
             }
         }
 
         private static Map<String, Object> dropStayConfiguration;
         static {
             dropStayConfiguration = new HashMap<String, Object>();
             dropStayConfiguration.put("stayOnElementTimeout", 1500);
         }
 
         public Map<String, Object> getDropStayConfiguration() {
             return dropStayConfiguration;
         }
     }
 }
