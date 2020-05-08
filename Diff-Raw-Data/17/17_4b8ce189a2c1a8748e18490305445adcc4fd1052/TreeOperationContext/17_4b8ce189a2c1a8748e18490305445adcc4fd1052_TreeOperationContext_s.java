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
 package org.eclipse.jubula.rc.swt.implclasses;
 
 import java.awt.Rectangle;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.eclipse.jubula.rc.common.driver.ClickOptions;
 import org.eclipse.jubula.rc.common.driver.IEventThreadQueuer;
 import org.eclipse.jubula.rc.common.driver.IRobot;
 import org.eclipse.jubula.rc.common.driver.IRunnable;
 import org.eclipse.jubula.rc.common.exception.StepExecutionException;
 import org.eclipse.jubula.rc.common.implclasses.Verifier;
 import org.eclipse.jubula.rc.common.implclasses.tree.AbstractTreeOperationContext;
 import org.eclipse.jubula.rc.common.logger.AutServerLogger;
 import org.eclipse.jubula.rc.swt.utils.SwtPointUtil;
 import org.eclipse.jubula.rc.swt.utils.SwtUtils;
 import org.eclipse.jubula.tools.objects.event.EventFactory;
 import org.eclipse.jubula.tools.objects.event.TestErrorEvent;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.TreeItem;
 
 
 /**
  * This context holds the tree, the tree model and supports access to the Robot.
  * It also implements some general operations on trees.
  * @author BREDEX GmbH
  * @created 09.08.2005
  */
 public class TreeOperationContext extends AbstractTreeOperationContext {
 
     /** The AUT Server logger. */
     private static AutServerLogger log = 
         new AutServerLogger(TreeOperationContext.class);
 
     /**
      * Creates a new instance.
      * 
      * @param queuer The queuer
      * @param robot The Robot
      * @param tree The tree
      */
     public TreeOperationContext(IEventThreadQueuer queuer, IRobot robot,
         Tree tree) {
         
         super(queuer, robot, tree);
     }
 
     /**
      * @param node The node.
      * @param row The node row.
      * @return The converted text
      * @throws StepExecutionException If the method call fails.
      */
     protected String convertValueToText(final Object node, final int row)
         throws StepExecutionException {
         
         final TreeItem treeNode = (TreeItem)node;
         return (String)getQueuer().invokeAndWait(
             "convertValueToText", new IRunnable() { //$NON-NLS-1$
                 public Object run() {
                     return treeNode.getText();
                 }
             });
     }
     
     /**
      * {@inheritDoc}
      */
     public String getRenderedText(final Object node)
         throws StepExecutionException {
         
         final TreeItem treeNode = (TreeItem)node;
         return (String)getQueuer().invokeAndWait(
             "getRenderedText", new IRunnable() { //$NON-NLS-1$
                 public Object run() {
                     return treeNode.getText();
                 }
             });
     }
     
     /**
      * {@inheritDoc}
      */
     public boolean isVisible(final Object node) {
         final TreeItem treeNode = (TreeItem)node;
         Boolean visible = (Boolean)getQueuer().invokeAndWait("isVisible", //$NON-NLS-1$
                 new IRunnable() {
                     public Object run() {
                         TreeItem item = treeNode;
                         boolean vis = true;
                         while (item != null && item.getParentItem() != null) {
                             vis = item.getParentItem().getExpanded();
                             item = item.getParentItem();
                         }
                         return vis ? Boolean.TRUE : Boolean.FALSE;
                     }
                 });
         return visible.booleanValue();
     }
     
     /**
      * {@inheritDoc}
      */
     public Rectangle getVisibleRowBounds(Rectangle rowBounds) {
         org.eclipse.swt.graphics.Rectangle r = 
             (org.eclipse.swt.graphics.Rectangle)
             getQueuer().invokeAndWait("getVisibleRowBounds: " + rowBounds,  //$NON-NLS-1$
                     new IRunnable() {
 
                     public Object run() {
                         Tree tree = (Tree)getTree();
                         org.eclipse.swt.graphics.Rectangle visibleBounds = 
                             tree.getClientArea();
                         
                         return visibleBounds;
                     
                     }
                 });
         
         Rectangle visibleTreeBounds = new Rectangle(
             r.x, r.y, r.width, r.height);
         Rectangle visibleRowBounds = visibleTreeBounds.intersection(rowBounds);
         return visibleRowBounds;
     }
 
     /**
      * {@inheritDoc}
      */
     public ClassLoader getClassLoaderForTree() {
         return getTree().getClass().getClassLoader();
     }
 
     /**
      * {@inheritDoc}
      */
     public void collapseNode(Object node) {
         final TreeItem item = castToTreeItem(node);
         final Tree tree = (Tree)getTree();
         boolean doAction = isExpanded(item);
         if (doAction) {
             if (log.isDebugEnabled()) {
                 log.debug("Collapsing node: " //$NON-NLS-1$
                     + node);
             }
             getQueuer().invokeAndWait("collapse", new IRunnable() { //$NON-NLS-1$
                 public Object run() throws StepExecutionException {
                     Event collapseEvent = new Event();
                     collapseEvent.time = (int)System.currentTimeMillis();
                     collapseEvent.type = SWT.Collapse;
                     collapseEvent.widget = tree;
                     collapseEvent.item = item;
                     tree.notifyListeners(SWT.Collapse, collapseEvent);
                     item.setExpanded(false);
                     tree.update();
                     
                     // Return value not used
                     return null;
                 }
             });
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void expandNode(Object node) {
         
         final ClassLoader oldCl = Thread.currentThread()
             .getContextClassLoader();
 
         try {
             final TreeItem item = castToTreeItem(node);
             final Tree tree = (Tree)getTree();
 
             boolean doAction = !isExpanded(item);
             Thread.currentThread().setContextClassLoader(tree.getClass()
                 .getClassLoader());
 
             getQueuer().invokeAndWait("Scroll Tree item: " + item  //$NON-NLS-1$
                 + " to visible", new IRunnable() { //$NON-NLS-1$
                     public Object run() {
                         tree.showItem(item);
                     
                         // Return value not used
                         return null;
                     }
                 });
 
             final Rectangle nodeBounds = getNodeBounds(node);
             Rectangle visibleNodeBounds = getVisibleRowBounds(nodeBounds);
             
             org.eclipse.swt.graphics.Rectangle swtVisibleNodeBounds =
                 new org.eclipse.swt.graphics.Rectangle(
                     visibleNodeBounds.x,
                     visibleNodeBounds.y,
                     visibleNodeBounds.width,
                     visibleNodeBounds.height);
             
             getRobot().move(tree, swtVisibleNodeBounds);
             if (doAction) {
                 if (log.isDebugEnabled()) {
                     log.debug("Expanding node: " + node); //$NON-NLS-1$ 
                     log.debug("Node bounds   : " + visibleNodeBounds); //$NON-NLS-1$
                 }
 
                 getQueuer().invokeAndWait("expand", new IRunnable() { //$NON-NLS-1$
                     public Object run() throws StepExecutionException {
                         
                         Event expandEvent = new Event();
                         expandEvent.time = (int)System.currentTimeMillis();
                         expandEvent.type = SWT.Expand;
                         expandEvent.widget = tree;
                         expandEvent.item = item;
                         tree.notifyListeners(SWT.Expand, expandEvent);
                         item.setExpanded(true);
                         tree.update();
                         
                         // Return value not used
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
         return (Object[])getQueuer().invokeAndWait("getRootNode",  //$NON-NLS-1$
             new IRunnable() {
 
                 public Object run() {
                     return ((Tree)getTree()).getItems();
                 }
 
             });
 
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean isExpanded(Object node) {
         final TreeItem item = castToTreeItem(node);
         // FIXME zeb: Verify that getExpanded() works like I think it does:
         //            It should return false if any of the parent nodes are 
         //            collapsed.
         return ((Boolean)getQueuer().invokeAndWait(
                 "isExpanded: " + item,  //$NON-NLS-1$
                 new IRunnable() {
 
                 public Object run() {
                     return item.getExpanded() ? Boolean.TRUE : Boolean.FALSE;
                 }
 
             })).booleanValue();
     }
 
     /**
      * {@inheritDoc}
      */
     public void clickNode(final Object node, final ClickOptions clickOps) {
         final TreeItem item = castToTreeItem(node);
         scrollNodeToVisible(item);
 
         org.eclipse.swt.graphics.Rectangle visibleRowBounds = 
             (org.eclipse.swt.graphics.Rectangle)getQueuer().invokeAndWait(
                 "getVisibleNodeBounds " + node, new IRunnable() { //$NON-NLS-1$
 
                     public Object run() {
                         final Rectangle nodeBounds = getNodeBounds(node);
                         return SwtPointUtil.toSwtRectangle(
                                 getVisibleRowBounds(nodeBounds));
                     }
                 });
         
         getRobot().click(getTree(), visibleRowBounds, 
                 clickOps.setScrollToVisible(false));
     }
     
     /**
      * {@inheritDoc}
      */
     public void toggleNodeCheckbox(final Object node) {
         final TreeItem item = castToTreeItem(node);
         scrollNodeToVisible(item);
 
         getQueuer().invokeAndWait(
                 "selectNodeCheckbox", new IRunnable() { //$NON-NLS-1$
                     public Object run() {
                        if (item.getChecked()) {
                            item.setChecked(false);
                        } else {
                            item.setChecked(true);
                        }
                         return null;
                     }            
                 }); 
     }
     
     /**
      * {@inheritDoc}
      */
     public void verifyCheckboxSelection(final Object node,
             final boolean checked) {
         final TreeItem item = castToTreeItem(node);
         scrollNodeToVisible(item);
 
         Boolean checkSelected = ((Boolean)getQueuer().invokeAndWait(
                 "verifyCheckboxSelection", new IRunnable() { //$NON-NLS-1$
                     public Object run() {
                         return new Boolean(item.getChecked());
                     }            
                 }));       
         
         Verifier.equals(checked, checkSelected.booleanValue());
     }
 
     /**
      * {@inheritDoc}
      */
     public void scrollNodeToVisible(final Object node) {
         getQueuer().invokeAndWait("showItem: " + node,  //$NON-NLS-1$
             new IRunnable() {
 
                 public Object run() {
                     ((Tree)getTree()).showItem(castToTreeItem(node));
                     return null;
                 }
 
             });
 
         final Rectangle nodeBoundsRelativeToParent = getNodeBounds(node);
         final Tree tree = (Tree)getTree();
         
         getQueuer().invokeAndWait("getNodeBoundsRelativeToParent", //$NON-NLS-1$
             new IRunnable() {
                 public Object run() {
                     org.eclipse.swt.graphics.Point cellOriginRelativeToParent = 
                         tree.getDisplay().map(
                                 tree, tree.getParent(), 
                                 new org.eclipse.swt.graphics.Point(
                                         nodeBoundsRelativeToParent.x, 
                                         nodeBoundsRelativeToParent.y));
                     nodeBoundsRelativeToParent.x = 
                         cellOriginRelativeToParent.x;
                     nodeBoundsRelativeToParent.y = 
                         cellOriginRelativeToParent.y;
                     return null;
                 }
             });
 
         Control parent = (Control)getQueuer().invokeAndWait("getParent", //$NON-NLS-1$
                 new IRunnable() {
                 public Object run() {
                     tree.getParent();
                     return null;
                 }
             });
 
         
         getRobot().scrollToVisible(parent, 
                 SwtPointUtil.toSwtRectangle(nodeBoundsRelativeToParent));
 
     }
 
     /**
      * {@inheritDoc}
      */
     public Object getChild(final Object parent, final int index) {
         if (parent == null) {
             Object [] rootNodes = getRootNodes();
             if (index < 0 || index >= rootNodes.length) {
                 // FIXME zeb: Handle child not found
                 return null;
             }
             return rootNodes[index];
         }
 
         return getQueuer().invokeAndWait(
             "getChild: " + parent + "; With index: " + index,  //$NON-NLS-1$ //$NON-NLS-2$
             new IRunnable() {
 
                 public Object run() {
                     TreeItem parentItem = castToTreeItem(parent);
                     try {
                         return parentItem.getItem(index);
                     } catch (IllegalArgumentException iae) {
                         // FIXME zeb: Handle child not found
                         return null;
                     }
                 }
             });
     }
 
     /**
      * {@inheritDoc}
      */
     public Object getParent(final Object child) {
         return getQueuer().invokeAndWait("getParent: " + child,  //$NON-NLS-1$
             new IRunnable() {
 
                 public Object run() {
                     TreeItem childItem = castToTreeItem(child);
                     return childItem.getParentItem();
                 }
 
             });
     }
 
     /**
      * Casts an Object to a TreeItem, handles ClassCastExceptions by throwing an
      * IllegalArgumentException.
      * @param toCast    The object to cast
      * @return  The original object casted to a TreeItem
      */
     private TreeItem castToTreeItem(Object toCast) {
         try {
             return (TreeItem)toCast;
         } catch (ClassCastException cce) {
             throw new IllegalArgumentException(
                 "Object must be of type TreeItem"); //$NON-NLS-1$
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public Object getSelectedNode() {
         return getQueuer().invokeAndWait("getSelectedNode",  //$NON-NLS-1$
             new IRunnable() {
 
                 public Object run() {
                     TreeItem [] selectedItems = 
                         ((Tree)getTree()).getSelection();
                     if (selectedItems == null || selectedItems.length == 0) {
                         throw new StepExecutionException(
                             "No tree node(s) selected", //$NON-NLS-1$
                             EventFactory.createActionError(
                                 TestErrorEvent.NO_SELECTION));
                     }
                     return selectedItems[0];
                 }
 
             });
     }
 
     /**
      * {@inheritDoc}
      */
     public Object[] getChildren(Object parent) {
         if (parent == null) {
             return getRootNodes();
         }
         final TreeItem item = castToTreeItem(parent);
         return (Object[])getQueuer().invokeAndWait("getChildren: " + item,  //$NON-NLS-1$
             new IRunnable() {
 
                 public Object run() {
                     return item.getItems();
                 }
 
             });
     }
 
     /**
      * {@inheritDoc}
      */
     public int getNumberOfChildren(Object parent) {
         if (parent == null) {
             return getRootNodes().length;
         }
         final TreeItem item = castToTreeItem(parent);
         return ((Integer)getQueuer().invokeAndWait("getChildren: " + item,  //$NON-NLS-1$
                 new IRunnable() {
 
                 public Object run() {
                     return new Integer(item.getItemCount());
                 }
 
             })).intValue();
     }
 
     /**
      * {@inheritDoc}
      */
     public Collection getNodeTextList(Object node) {
         final Collection res = new ArrayList();
         final TreeItem item = castToTreeItem(node);
         
         getQueuer().invokeAndWait("getNodeText: " + item,  //$NON-NLS-1$
             new IRunnable() {
 
                 public Object run() {
                     int colCount = ((Tree)getTree()).getColumnCount();
                     
                     for (int i = 0; i < colCount; i++) {
                         String textAtColumn = item.getText(i);
                         if (textAtColumn != null) {
                             res.add(textAtColumn);
                         }
                     }
                     
                     String text = item.getText();
                     if (text != null) {
                         res.add(text);
                     }
                     
                     return null;
                 }
 
             });
 
         return res;
     }
 
     /**
      * {@inheritDoc}
      */
     public Rectangle getNodeBounds(final Object node) {
         org.eclipse.swt.graphics.Rectangle r = 
             (org.eclipse.swt.graphics.Rectangle)
             getQueuer().invokeAndWait("getNodeBounds: " + node,  //$NON-NLS-1$
                     new IRunnable() {
 
                     public Object run() {
                         Tree tree = (Tree)getTree();
                         org.eclipse.swt.graphics.Rectangle bounds =
                             SwtUtils.getRelativeWidgetBounds(
                                     (TreeItem)node, tree);
                         
                         return bounds;
                     }
                 });
         Rectangle nodeBounds = new Rectangle(
             r.x, r.y, r.width, r.height);
 
         return nodeBounds;
     }
 
     /**
      * {@inheritDoc}
      */
     public int getIndexOfChild(final Object parent, final Object child) {
         if (parent != null) {
             return ((Integer)(getQueuer().invokeAndWait(
                     "getIndexOfChild",  //$NON-NLS-1$
                     new IRunnable() {
 
                         public Object run() throws StepExecutionException {
                             return new Integer(
                                 ((TreeItem)parent).indexOf((TreeItem)child));
                         }
 
                     }))).intValue();
         }
      
         Object [] rootNodes = getRootNodes();
         for (int i = 0; i < rootNodes.length; i++) {
             if (rootNodes[i] == child) {
                 return i;
             }
         }
         
         return -1;
     }
 
 }
