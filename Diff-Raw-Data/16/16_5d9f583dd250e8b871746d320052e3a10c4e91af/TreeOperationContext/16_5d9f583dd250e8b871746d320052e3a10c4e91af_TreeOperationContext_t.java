 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.rc.swing.tester.util;
 
 import java.awt.Component;
 import java.awt.Rectangle;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import javax.swing.JComponent;
 import javax.swing.JTree;
 import javax.swing.tree.TreeModel;
 import javax.swing.tree.TreeNode;
 import javax.swing.tree.TreePath;
 
 import org.apache.commons.lang.ObjectUtils;
 import org.apache.commons.lang.Validate;
 import org.eclipse.jubula.rc.common.driver.ClickOptions;
 import org.eclipse.jubula.rc.common.driver.IEventThreadQueuer;
 import org.eclipse.jubula.rc.common.driver.IRobot;
 import org.eclipse.jubula.rc.common.driver.IRunnable;
 import org.eclipse.jubula.rc.common.exception.StepExecutionException;
 import org.eclipse.jubula.rc.common.implclasses.tree.AbstractTreeOperationContext;
 import org.eclipse.jubula.rc.common.logger.AutServerLogger;
 import org.eclipse.jubula.rc.swing.driver.EventThreadQueuerAwtImpl;
 import org.eclipse.jubula.tools.objects.event.EventFactory;
 import org.eclipse.jubula.tools.objects.event.TestErrorEvent;
 
 
 /**
  * This context holds the tree, the tree model and supports access to the Robot.
  * It also implements some general operations on trees.
  * 
  * @author BREDEX GmbH
  * @created 09.08.2005
  */
 public class TreeOperationContext extends AbstractTreeOperationContext {
     
     /** The AUT Server logger. */
     private static AutServerLogger log = 
         new AutServerLogger(TreeOperationContext.class);
     
     /** The tree model. */
     private TreeModel m_model;
     
     /**
      * Creates a new instance. The JTree must have a tree model.
      *      
      * @param queuer The queuer
      * @param robot The Robot
      * @param tree The tree
      */
     public TreeOperationContext(IEventThreadQueuer queuer, IRobot robot,
         JTree tree) {
         
         super(queuer, robot, tree);
         Validate.notNull(tree.getModel());
         m_model = tree.getModel();
     }
     /**
      * {@inheritDoc}
      */
     public Object[] getChildren(Object parent) {
         if (parent == null) {
             return getRootNodes();
         }
         int childCount = m_model.getChildCount(parent);
         List childList = new ArrayList();
         for (int i = 0; i < childCount; i++) {
             childList.add(m_model.getChild(parent, i));
         }
         
         return childList.toArray();
     }
 
     /**
      * {@inheritDoc}
      */
     public Collection getNodeTextList(Object node) {
         Collection res = new ArrayList();
         int row = getRowForTreeNode(node);
         String valText = convertValueToText(node, row);
         if (valText != null) {
             res.add(valText);
         }
         String rendText = getRenderedText(node);
         if (rendText != null) {
             res.add(rendText);
         }
         return res;
     }
 
     /**
      * {@inheritDoc}
      */
     public int getNumberOfChildren(Object parent) {
         if (parent == null) {
             return getRootNodes().length;
         }
 
         return m_model.getChildCount(parent);
     }
 
     
     /**
      * Calls
      * {@link JTree#convertValueToText(java.lang.Object, boolean, boolean, boolean, int, boolean)}
      * on the passed JTree.
      * @param node
      *            The node.
      * @param row
      *            The node row.
      * @return The converted text
      * @throws StepExecutionException
      *             If the method call fails.
      */
     protected String convertValueToText(final Object node, final int row)
         throws StepExecutionException {
         
         return (String)getQueuer().invokeAndWait(
             "convertValueToText", new IRunnable() { //$NON-NLS-1$
                 public Object run() {
                     return ((JTree)getTree()).convertValueToText(node, 
                         false, ((JTree)getTree()).isExpanded(row), 
                         m_model.isLeaf(node), row, false);
                 }
             });
     }
 
     /**
      * {@inheritDoc}
      */
    public String getRenderedText(final Object node) {
         return (String)getQueuer().invokeAndWait(
                 "getRenderedText", new IRunnable() { //$NON-NLS-1$
                     public Object run() {
                         int row = getRowForTreeNode(node);
                         JTree tree = (JTree)getTree();
                         Component cellRendererComponent = tree
                                 .getCellRenderer()
                                 .getTreeCellRendererComponent(tree, node,
                                         false, tree.isExpanded(row),
                                         m_model.isLeaf(node), row, false);
                        try {
                            return TesterUtil
                                    .getRenderedText(cellRendererComponent);
                        } catch (StepExecutionException e) {
                            // This is a valid case in JTrees since if there is no text
                            // there is also no renderer 
                            log.warn("Renderer not supported: " +       //$NON-NLS-1$
                                    cellRendererComponent.getClass(), e); 
                            return null;
                        }
                     }
                 });
     }
     
     /**
      * Calls
      * {@link JTree#convertValueToText(java.lang.Object, boolean, boolean, boolean, int, boolean)}
      * on any tree node of the <code>treePath</code> and returns the texts as an array.
      * @param treePath The tree path
      * @return The array of converted texts
      */
     protected String[] convertTreePathToText(Object treePath) {
         final TreePath tp = (TreePath)treePath;
         Object[] path = tp.getPath();
         String[] values = new String[path.length];
         for (int i = 0; i < path.length; i++) {
             values[i] = convertValueToText(path[i], getRowForTreeNode(path[i]));
         }
         return values;
     }
     
     /**
      * Returns the row for the given node. The row is calculated based on how 
      * many nodes are visible above this node.
      * @param node  The node for which to find the row.
      * @return
      *      A zero-based index representing the row that the given node 
      *      occupies in the tree.
      * @throws StepExecutionException
      *      if the node cannot be found.
      */
     protected int getRowForTreeNode(final Object node)
         throws StepExecutionException {
         
         Integer row = (Integer)getQueuer().invokeAndWait(
             "getRowForTreeNode", new IRunnable() { //$NON-NLS-1$
                 public Object run() {
                     TreePath pathToRoot = new TreePath(getPathToRoot(node));
                     return new Integer(((JTree)getTree()).getRowForPath(
                         pathToRoot));
                 }
             });
         return row.intValue();
     }
 
     /**
      * Builds the parents of node up to and including the root node,
      * where the original node is the last element in the returned array.
      * The length of the returned array gives the node's depth in the
      * tree.
      * 
      * Contract adapted from javax.swing.tree.DefaultTreeModel.getPathToRoot().
      * 
      * @param node the TreeNode to get the path for
      * @return an array of TreeNodes giving the path from the root to the
      *         specified node.
      */
     private Object[] getPathToRoot(Object node) {
         Object rootNode = m_model.getRoot();
         List path = getPathToRootImpl(node, rootNode);
         return path.toArray();
     }
     
     /**
      * 
      * {@inheritDoc}
      */
     public Rectangle getNodeBounds(final Object node) 
         throws StepExecutionException {
 
         final int row = getRowForTreeNode(node);
         return (Rectangle)getQueuer().invokeAndWait(
             "getRowBounds", new IRunnable() { //$NON-NLS-1$
                 public Object run() {
                     return ((JTree)getTree()).getRowBounds(row);
                 }
             });
     }
 
     /**
      * Returns the path of all selected values.
      * @return
      *      an array of Objects indicating the selected nodes, or null 
      *      if nothing is currently selected.
      */
     protected Object[] getSelectionPaths() {
         return (TreePath[])getQueuer().invokeAndWait("getSelectionPath", //$NON-NLS-1$
             new IRunnable() {
                 public Object run() {
                     return ((JTree)getTree()).getSelectionPaths();
                 }
             });
     }
     
     /**
      * {@inheritDoc}
      */
     public boolean isVisible(final Object node) {
         Boolean visible = (Boolean)getQueuer().invokeAndWait("isVisible", //$NON-NLS-1$
                 new IRunnable() {
                     public Object run() {
                         Object[] path = getPathToRoot(node);
                         return (((JTree)getTree()).isVisible(
                             new TreePath(path))) ? Boolean.TRUE : Boolean.FALSE;
                     }
                 });
         return visible.booleanValue();
     }
 
     /**
      * Getter for the model
      * @return Returns the model.
      */
     protected TreeModel getModel() {
         return m_model;
     }
     
     /**
      * {@inheritDoc}
      */
     public Rectangle getVisibleRowBounds(Rectangle rowBounds) {
         Rectangle visibleTreeBounds = ((JComponent)getTree()).getVisibleRect();
         Rectangle visibleRowBounds = visibleTreeBounds.intersection(rowBounds);
         return visibleRowBounds;
     }
 
     /**
      * {@inheritDoc}
      */
     public void collapseNode(Object node) {
         final JTree tree = (JTree)getTree();
         final ClassLoader oldCl = Thread.currentThread()
             .getContextClassLoader();
         try {
             Thread.currentThread().setContextClassLoader(tree.getClass()
                 .getClassLoader());
             final int row = getRowForTreeNode(node);
             final Rectangle nodeBounds = getNodeBounds(node);
             final boolean collapsed = tree.isCollapsed(row);
             boolean doAction = isExpanded(node);
             final IEventThreadQueuer queuer = new EventThreadQueuerAwtImpl();
 
             queuer.invokeAndWait("scrollRowToVisible", new IRunnable() { //$NON-NLS-1$
                 public Object run() {
                     tree.scrollRowToVisible(row);
                     
                     return null;
                 }
             });
 
             Rectangle visibleNodeBounds = getVisibleRowBounds(nodeBounds);
             getRobot().move(tree, visibleNodeBounds);
             if (doAction) {
                 if (log.isDebugEnabled()) {
                     log.debug((collapsed ? "Expanding" : "Collapsing") + " node: " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                         + node);
                     log.debug("Row           : " + row); //$NON-NLS-1$
                     log.debug("Node bounds   : " + visibleNodeBounds); //$NON-NLS-1$
                 }
                 queuer.invokeAndWait("collapseRow", new IRunnable() { //$NON-NLS-1$
                     public Object run() {
                         tree.collapseRow(row);
 
                         return null;
                     }
                 });
             }
         } finally {
             Thread.currentThread().setContextClassLoader(oldCl);
         }
         
     }
 
     /**
      * {@inheritDoc}
      */
     public void expandNode(Object node) {
         final JTree tree = (JTree)getTree();
         final ClassLoader oldCl = Thread.currentThread()
             .getContextClassLoader();
         try {
             Thread.currentThread().setContextClassLoader(tree.getClass()
                 .getClassLoader());
             final int row = getRowForTreeNode(node);
             final Rectangle nodeBounds = getNodeBounds(node);
             final boolean collapsed = tree.isCollapsed(row);
             boolean doAction = !isExpanded(node);
             final IEventThreadQueuer queuer = new EventThreadQueuerAwtImpl();
 
             queuer.invokeAndWait("scrollRowToVisible", new IRunnable() { //$NON-NLS-1$
                 public Object run() {
                     tree.scrollRowToVisible(row);
                     
                     return null;
                 }
             });
 
             Rectangle visibleNodeBounds = getVisibleRowBounds(nodeBounds);
             getRobot().move(tree, visibleNodeBounds);
             if (doAction) {
                 if (log.isDebugEnabled()) {
                     log.debug((collapsed ? "Expanding" : "Collapsing") + " node: " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                         + node);
                     log.debug("Row           : " + row); //$NON-NLS-1$
                     log.debug("Node bounds   : " + visibleNodeBounds); //$NON-NLS-1$
                 }
                 queuer.invokeAndWait("expandRow", new IRunnable() { //$NON-NLS-1$
                     public Object run() {
                         tree.expandRow(row);
                         
                         return null;
                     }
                 });
             }
         } finally {
             Thread.currentThread().setContextClassLoader(oldCl);
         }
 
     }
 
     /**
      * {@inheritDoc}
      */
     public Object[] getRootNodes() {
         JTree tree = (JTree)getTree();
         
         // If the root is visible, just return that.
         if (tree.isRootVisible()) {
             return new Object [] {tree.getModel().getRoot()};
         }
         
         // If the root is not visible, return all direct children of the 
         // non-visible root.
         return getChildren(tree.getModel().getRoot());
     }
 
     /**
      * {@inheritDoc}
      */
     public void scrollNodeToVisible(Object node) {
         ((JTree)getTree()).scrollRowToVisible(getRowForTreeNode(node));
     }
 
     /**
      * {@inheritDoc}
      */
     public Object getChild(Object parent, int index) {
 
         try {
             if (parent == null) {
                 Object [] rootNodes = getRootNodes();
                 return rootNodes[index];
             }
             return m_model.getChild(parent, index);
         } catch (ArrayIndexOutOfBoundsException e) {
             // FIXME zeb: Deal with child not found
             return null;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public Object getParent(Object child) {
         Object parent = null;
         if (child instanceof TreeNode) {
             // This is the easy way.
             parent = ((TreeNode)child).getParent();
         } else {
             // Great. The node doesn't implement TreeNode. Looks like we'll
             // have to_do things the hard way.
             Object[] pathToRoot = getPathToRoot(child);
             if (pathToRoot.length > 1) {
                 // parent is the next-to-last element in the path
                 parent = pathToRoot[pathToRoot.length - 2];
             }
         } 
         
         // If the parent is the actual root node, and the root is not visible,
         // then treat the child as one of the "root" nodes.
         if (parent != null 
             && parent.equals(m_model.getRoot()) 
             && !((JTree)getTree()).isRootVisible()) {
             parent = null;
         }
         
         return parent;
     }
 
     /**
      * Recursively builds and returns the path to root for <code>node</code>.
      * The contract is similar to that of 
      * javax.swing.tree.DefaultTreeModel.getPathToRoot().
      * 
      * @param node The node for which to find the path to root.
      * @param currentNode The node currently being checked.
      * @return a List containing the elements of the path in the proper order.
      */
     private List getPathToRootImpl(Object node, Object currentNode) {
         if (ObjectUtils.equals(currentNode, node)) {
             List retList = new ArrayList();
             retList.add(currentNode);
             return retList;
         }
         
         int childCount = m_model.getChildCount(currentNode);
         for (int i = 0; i < childCount; i++) {
             List path = getPathToRootImpl(
                     node, m_model.getChild(currentNode, i)); 
             if (path != null) {
                 // prepend the current node to the path and return
                 path.add(0, currentNode);
                 return path;
             }
         }
 
         return null;
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean isExpanded(Object node) {
         return ((JTree)getTree()).isExpanded(getRowForTreeNode(node));
     }
 
     /**
      * {@inheritDoc}
      */
     public void clickNode(Object node, ClickOptions clickOps) {
         scrollNodeToVisible(node);
         Rectangle rowBounds = getNodeBounds(node);
         Rectangle visibleRowBounds = getVisibleRowBounds(rowBounds);
         getRobot().click(getTree(), visibleRowBounds, 
                 clickOps);
     }
 
     /**
      * {@inheritDoc}
      */
     public Object getSelectedNode() {
         TreePath[] paths = getCheckedSelectedPaths();
         return paths[0].getLastPathComponent();
     }
 
     /**
      * Returns the selected tree paths.
      * 
      * @return The path
      */
     private TreePath[] getCheckedSelectedPaths() {
         TreePath[] paths = (TreePath[])getSelectionPaths();
         if (paths == null) {
             throw new StepExecutionException("No tree node(s) selected", //$NON-NLS-1$
                 EventFactory.createActionError(TestErrorEvent.NO_SELECTION));
         }
         return paths;
     }
     /**
      * {@inheritDoc}
      */
     public int getIndexOfChild(Object parent, Object child) {
 
         if (parent != null) {
             return ((JTree)getTree()).getModel().getIndexOfChild(parent, child);
         }
         
         Object [] rootNodes = getRootNodes();
         for (int i = 0; i < rootNodes.length; i++) {
             if (ObjectUtils.equals(rootNodes[i], child)) {
                 return i;
             }
         }
 
         return -1;
 
     }
 
 }
