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
 
 
 import org.apache.commons.lang.Validate;
 import org.eclipse.jubula.rc.common.CompSystemConstants;
 import org.eclipse.jubula.rc.common.driver.ClickOptions;
 import org.eclipse.jubula.rc.common.driver.IRobot;
 import org.eclipse.jubula.rc.common.driver.IRunnable;
 import org.eclipse.jubula.rc.common.exception.StepExecutionException;
 import org.eclipse.jubula.rc.common.exception.StepVerifyFailedException;
 import org.eclipse.jubula.rc.common.implclasses.IndexConverter;
 import org.eclipse.jubula.rc.common.implclasses.MatchUtil;
 import org.eclipse.jubula.rc.common.implclasses.Verifier;
 import org.eclipse.jubula.rc.common.implclasses.tree.AbstractTreeNodeOperation;
 import org.eclipse.jubula.rc.common.implclasses.tree.AbstractTreeNodeTraverser;
 import org.eclipse.jubula.rc.common.implclasses.tree.ChildTraverser;
 import org.eclipse.jubula.rc.common.implclasses.tree.ExpandCollapseTreeNodeOperation;
 import org.eclipse.jubula.rc.common.implclasses.tree.INodePath;
 import org.eclipse.jubula.rc.common.implclasses.tree.IndexNodePath;
 import org.eclipse.jubula.rc.common.implclasses.tree.ParentTraverser;
 import org.eclipse.jubula.rc.common.implclasses.tree.PathBasedTraverser;
 import org.eclipse.jubula.rc.common.implclasses.tree.SelectTreeNodeOperation;
 import org.eclipse.jubula.rc.common.implclasses.tree.SiblingTraverser;
 import org.eclipse.jubula.rc.common.implclasses.tree.StandardDepthFirstTraverser;
 import org.eclipse.jubula.rc.common.implclasses.tree.StringNodePath;
 import org.eclipse.jubula.rc.common.implclasses.tree.TreeNodeOperation;
 import org.eclipse.jubula.rc.common.implclasses.tree.TreeNodeOperationConstraint;
 import org.eclipse.jubula.rc.swt.driver.DragAndDropHelperSwt;
 import org.eclipse.jubula.rc.swt.implclasses.tree.ToggleCheckboxOperation;
 import org.eclipse.jubula.rc.swt.implclasses.tree.VerifyCheckboxOperation;
 import org.eclipse.jubula.rc.swt.utils.SwtUtils;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.constants.TestDataConstants;
 import org.eclipse.jubula.tools.objects.event.EventFactory;
 import org.eclipse.jubula.tools.objects.event.TestErrorEvent;
 import org.eclipse.jubula.tools.utils.StringParsing;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.TreeItem;
 
 
 /**
  * Implementation class for SWT-Tree
  *
  * @author BREDEX GmbH
  * @created Oct 30, 2006
  */
 public class TreeImplClass extends AbstractControlImplClass {
 
     /**
      * Finds the item at a given position in the tree.
      *
      * @author BREDEX GmbH
      * @created Jul 28, 2010
      */
     private static final class ItemAtPointTreeNodeOperation 
             extends AbstractTreeNodeOperation {
 
         /** the item that was found at the given position */
         private TreeItem m_itemAtPoint;
         
         /** the position (in absolute coordinates) at which to find the item */
         private Point m_absPoint;
         
         /** 
          * the bounds (in absolute coordinates) of the tree in which the 
          * search should take place 
          */
         private Rectangle m_absTreeBounds;
         
         /**
          * Constructor
          * 
          * @param absPoint The position (in absolute coordinates) at which to 
          *                 find the item.
          * @param absTreeBounds The bounds (in absolute coordinates) of the 
          *                      tree in which the search should take place. 
          */
         public ItemAtPointTreeNodeOperation(Point absPoint, 
                 Rectangle absTreeBounds) {
             m_absPoint = absPoint;
             m_absTreeBounds = absTreeBounds;
         }
         
         /**
          * {@inheritDoc}
          */
         public boolean operate(Object node) throws StepExecutionException {
             if (getContext().isVisible(node) && node instanceof TreeItem) {
                 TreeItem currentItem = (TreeItem)node;
                 final Rectangle absItemBounds = 
                     SwtUtils.getBounds(currentItem);
                 absItemBounds.x = m_absTreeBounds.x;
                 absItemBounds.width = m_absTreeBounds.width;
                 if (SwtUtils.containsInclusive(
                         absItemBounds, m_absPoint)) {
                     m_itemAtPoint = currentItem;
                     return false;
                 }
             }
             
             return true;
         }
         
         /**
          * 
          * @return the item found at the given position, or <code>null</code> if
          *         no item was found. Note that this method will always return 
          *         <code>null</code> if called before or during execution of
          *         {@link #operate(Object)}.
          */
         public TreeItem getItemAtPoint() {
             return m_itemAtPoint;
         }
     }
     
     /** the Tree from the AUT */
     private Tree m_tree;
 
     /**
      * {@inheritDoc}
      */
     public void setComponent(Object graphicsComponent) {
         m_tree = (Tree)graphicsComponent;
     }
     
     /**
      * {@inheritDoc}
      */
     public Control getComponent() {
         return m_tree;
     }
     
     /**
      * <p>
      * Expands the JTree. Any node defined by the passed tree path is expanded,
      * if it is collapsed. The node is expanded by performing a double click
      * onto the node. If the node is already expanded, the JTree is left
      * unchanged. The tree path is a slash-seperated list of nodes that specifies
      * a valid top-down path in the JTree.
      * </p>
      * 
      * An example: Say the passed tree path is <code>animals/birds/kakadu</code>.
      * To get a valid expansion, the JTree has to look as follows:
      * 
      * <pre>
      * animals
      * |
      * - -- birds
      *      |
      *      - -- kakadu
      * </pre>
      * 
      * <code>animals</code> is the JTree's root node, if the root node has
      * been set to visible by calling <code>JTree#setRootVisible(true)</code>,
      * or it is one of the root node's children, if the root node has been set
      * to invisible by calling <code>JTree#setRootVisible(false)</code>.
      * 
      * <p>
      * It is important to know that the tree path entries have to match the
      * rendered node texts, but not the underlying user object data etc.
      * </p>
      * 
      * @param pathType whether the path is relative or absolute
      * @param preAscend
      *            Relative traversals will start this many parent nodes 
      *            above the current node. Absolute traversals ignore this 
      *            parameter.
      * @param treePath
      *            The tree path.
      * @param operator
      *            If regular expressions are used to determine the tree path
      * @throws StepExecutionException
      *             If the tree path is invalid or the double click fails.
      */
     public void gdExpand(String pathType, int preAscend, String treePath, 
             String operator) throws StepExecutionException {
         
         traverseTreeByPath(
                 createStringNodePath(splitTextTreePath(treePath), operator), 
                 pathType, preAscend, 
                 new ExpandCollapseTreeNodeOperation(false));
     }
     
     /**
      * Expands the tree. This method works like {@link #gdExpand(String)}, but
      * expects an enumeration of indices representing the top-down tree path.
      * Any index is the node's position at the current tree level.
      * 
      * @param pathType whether the path is relative or absolute
      * @param preAscend
      *            Relative traversals will start this many parent nodes 
      *            above the current node. Absolute traversals ignore this 
      *            parameter.
      * @param indexPath
      *            The index path
      * @throws StepExecutionException
      *             If the tree path is invalid or the double-click fails.
      */
     public void gdExpandByIndices(String pathType, int preAscend, 
             String indexPath) throws StepExecutionException {
         
         try {
             traverseTreeByPath(
                     createIndexNodePath(splitIndexTreePath(indexPath)), 
                     pathType, preAscend, 
                     new ExpandCollapseTreeNodeOperation(false));
         } catch (NumberFormatException e) {
             throw new StepExecutionException(e.getMessage(), EventFactory
                     .createActionError(TestErrorEvent.INVALID_INDEX));
         }
     }
     
     /**
      * Collapses the JTree. The passed tree path is a slash-seperated list of
      * nodes that specifies a valid top-down path in the JTree. The last node of
      * the tree path is collapsed if it is currently expanded. Otherwise, the JTree is
      * left unchanged.
      * 
      * @param pathType whether the path is relative or absolute
      * @param preAscend
      *            Relative traversals will start this many parent nodes 
      *            above the current node. Absolute traversals ignore this 
      *            parameter.
      * @param treePath
      *            The tree path.
      * @param operator
      *            Whether regular expressions are used to determine the tree path. 
      *            <code>"matches"</code> for regex, <code>"equals"</code> for simple matching.
      * @throws StepExecutionException
      *             If the tree path is invalid or the double click to collapse
      *             the node fails.
      */
     public void gdCollapse(String pathType, int preAscend, String treePath, 
             String operator) throws StepExecutionException {
         traverseLastElementByPath(
                 createStringNodePath(splitTextTreePath(treePath), operator), 
                 pathType, preAscend, 
                 new ExpandCollapseTreeNodeOperation(true));
     }
     
     /**
      * Collapses the tree. This method works like {@link #gdCollapse(String)},
      * but expects an enumeration of indices representing the top-down tree
      * path. Any index is the node's position at the current tree level.
      * 
      * @param pathType whether the path is relative or absolute
      * @param preAscend
      *            Relative traversals will start this many parent nodes 
      *            above the current node. Absolute traversals ignore this 
      *            parameter.
      * @param indexPath
      *            The index path
      * @throws StepExecutionException
      *             If the tree path is invalid or the double-click to collapse
      *             the node fails.
      */
     public void gdCollapseByIndices(String pathType, int preAscend, 
             String indexPath) throws StepExecutionException {
         
         try {
             traverseLastElementByPath(
                     createIndexNodePath(splitIndexTreePath(indexPath)), 
                     pathType, preAscend, 
                     new ExpandCollapseTreeNodeOperation(true));
         } catch (NumberFormatException e) {
             throw new StepExecutionException(e.getMessage(), EventFactory
                     .createActionError(TestErrorEvent.INVALID_INDEX));
         }
     }
 
     /**
      * Selects a node relative to the currently selected node.
      * @param direction the direction to move.
      *                  directions:
      *                      UP - Navigates through parents
      *                      DOWN - Navigates through children
      *                      NEXT - Navigates to next sibling
      *                      PREVIOUS - Navigates to previous sibling
      * @param distance the distance to move
      * @param clickCount the click count to select the new cell.
      * @throws StepExecutionException if any error occurs
      */
     public void gdMove(String direction, int distance, int clickCount) 
         throws StepExecutionException {
         
         TreeOperationContext context = 
             new TreeOperationContext(
                 getEventThreadQueuer(), getRobot(), m_tree);
 
         TreeItem selectedNode = getSelectedNode(context);
         
         TreeNodeOperation selectOp = 
             new SelectTreeNodeOperation(
                     ClickOptions.create().setClickCount(clickCount));
         TreeNodeOperationConstraint constraint = 
             new TreeNodeOperationConstraint();
 
         if (CompSystemConstants.TREE_MOVE_UP.equalsIgnoreCase(direction)) {
             AbstractTreeNodeTraverser traverser = 
                 new ParentTraverser(context, distance, constraint);
             traverser.traversePath(selectOp, selectedNode);
         } else if (CompSystemConstants
                 .TREE_MOVE_DOWN.equalsIgnoreCase(direction)) {
             TreeNodeOperation expandOp = 
                 new ExpandCollapseTreeNodeOperation(false);
             AbstractTreeNodeTraverser expandTraverser = 
                 new ChildTraverser(context, distance - 1);
             expandTraverser.traversePath(expandOp, selectedNode);
 
             AbstractTreeNodeTraverser selectTraverser = 
                 new ChildTraverser(context, distance, constraint);
             selectTraverser.traversePath(selectOp, selectedNode);
             
         } else if (CompSystemConstants
                 .TREE_MOVE_NEXT.equalsIgnoreCase(direction)) {
             // Look through siblings
             AbstractTreeNodeTraverser traverser = 
                 new SiblingTraverser(context, distance, true, constraint);
             traverser.traversePath(selectOp, selectedNode);
             
         } else if (CompSystemConstants
                 .TREE_MOVE_PREVIOUS.equalsIgnoreCase(direction)) {
             // Look through siblings
             AbstractTreeNodeTraverser traverser = 
                 new SiblingTraverser(context, distance, false, constraint);
             traverser.traversePath(selectOp, selectedNode);
         }
 
     }
 
     /**
      * Selects the node at the end of the <code>treepath</code>.
      * 
      * @param pathType whether the path is relative or absolute
      * @param preAscend
      *            Relative traversals will start this many parent nodes 
      *            above the current node. Absolute traversals ignore this 
      *            parameter.
      * @param treePath The tree path.
      * @param operator
      *  If regular expressions are used to match the tree path
      * @param clickCount the click count
      * @param button what mouse button should be used
      * @param extendSelection Whether this selection extends a previous 
      *                        selection.
      * @throws StepExecutionException If the tree path is invalid, if the
      * double-click to expand the node fails, or if the selection is invalid.
      */
     public void gdSelect(String pathType, int preAscend, String treePath, 
                          String operator, int clickCount, int button,
                          final String extendSelection)
         throws StepExecutionException {
         selectByPath(pathType, preAscend, 
                 createStringNodePath(splitTextTreePath(treePath), operator), 
                 ClickOptions.create()
                     .setClickCount(clickCount)
                     .setMouseButton(button)
                     .setClickModifier(getClickModifier(extendSelection)));
 
     }
     /**
      * Selects the last node of the path given by <code>indexPath</code>
      * @param pathType whether the path is relative or absolute
      * @param preAscend
      *            Relative traversals will start this many parent nodes 
      *            above the current node. Absolute traversals ignore this 
      *            parameter.
      * @param indexPath the index path
      * @param clickCount the number of times to click
      * @param button what mouse button should be used
      * @param extendSelection Whether this selection extends a previous 
      *                        selection.
      * @throws StepExecutionException if <code>indexPath</code> is not a valid
      * path
      */
     public void gdSelectByIndices(String pathType, int preAscend, 
                                   String indexPath, int clickCount, int button,
                                   final String extendSelection)
         throws StepExecutionException {
         selectByPath(pathType, preAscend, 
                 createIndexNodePath(splitIndexTreePath(indexPath)), 
                 ClickOptions.create()
                     .setClickCount(clickCount)
                     .setMouseButton(button)
                     .setClickModifier(getClickModifier(extendSelection)));
     }
     
     /**
      * Verifies whether the first selection in the tree has a rendered text that is
      * equal to <code>pattern</code>.
      * 
      * @param pattern The pattern
      * @throws StepExecutionException If no node is selected or the verification fails.
      */
     public void gdVerifySelectedValue(String pattern)
         throws StepExecutionException {
 
         gdVerifySelectedValue(pattern, MatchUtil.DEFAULT_OPERATOR);
     }
     /**
      * Verifies if the selected node underneath <code>treePath</code> has a
      * rendered text which is equal to <code>selection</code>.
      * @param pattern the pattern
      * @param operator
      *            The operator to use when comparing the expected and 
      *            actual values.
      * @throws StepExecutionException If there is no tree node selected, the tree path contains no
      *             selection or the verification fails
      */
     public void gdVerifySelectedValue(String pattern, String operator)
         throws StepExecutionException {
         
         TreeOperationContext context = new TreeOperationContext(
             getEventThreadQueuer(), getRobot(), m_tree);
 
         // FIXME zeb: Assuming a single text representation of the node. See
         //            the comment for 
         //            AbstractTreeOperationContext.getNodeTextList
         String text = 
             (String)context.getNodeTextList(
                 context.getSelectedNode()).toArray()[0];
         
         Verifier.match(text, pattern, operator);
 
     }
     
     
     /**
      * Tests whether the given treePath exists or not
      * @param pathType whether the path is relative or absolute
      * @param preAscend
      *            Relative traversals will start this many parent nodes 
      *            above the current node. Absolute traversals ignore this 
      *            parameter.
      * @param treePath the path to check
      * @param operator the RegEx operator
      * @param exists if true, the verify succeeds if the path DOES exist.
      *  If false, the verify succeeds if the path DOES NOT exist.
      */
     public void gdVerifyPath(String pathType, int preAscend, String treePath, 
             String operator, boolean exists) {
         try {
             gdExpand(pathType, preAscend, treePath, operator);
         } catch (StepExecutionException e) {
             if (exists) {
                 throw new StepVerifyFailedException("Verify failed on tree-path: " //$NON-NLS-1$
                     + treePath, 
                     EventFactory.createVerifyFailed(
                             treePath, StringConstants.EMPTY));
             }
             return;
         }
         if (!exists) {
             throw new StepVerifyFailedException("Verify failed on tree-path: ",  //$NON-NLS-1$
                 EventFactory.createVerifyFailed(
                         StringConstants.EMPTY, treePath));
         }
     }
     
     /**
      * Tests whether the given treePath exists or not
      * @param pathType whether the path is relative or absolute
      * @param preAscend
      *            Relative traversals will start this many parent nodes 
      *            above the current node. Absolute traversals ignore this 
      *            parameter.
      * @param treePath the path to check
      * @param exists if true, the verify succeeds if the path DOES exist.
      *  If false, the verify succeeds if the path DOES NOT exist.
      */
     public void gdVerifyPathByIndices(String pathType, int preAscend, 
             String treePath, boolean exists) {
         try {
             gdExpandByIndices(pathType, preAscend, treePath);
         } catch (StepExecutionException e) {
             if (exists) {
                 throw new StepVerifyFailedException("Verify failed on tree-path: " //$NON-NLS-1$
                     + treePath, 
                     EventFactory.createVerifyFailed(
                             treePath, StringConstants.EMPTY));
             }
             return;
         }
         if (!exists) {
             throw new StepVerifyFailedException("Verify failed on tree-path: ",  //$NON-NLS-1$
                 EventFactory.createVerifyFailed(
                         StringConstants.EMPTY, treePath));
         }
     }
     
     /**
      * Clicks the tree.
      * If the mouse pointer is in the tree no mouse move will be perfomed.
      * Otherwise, the mouse is first moved to the center of the tree.
      * 
      * @param count Number of mouse clicks
      * @param button Pressed button
      */
     public void gdClick(int count, int button) {
 
         if (getRobot().isMouseInComponent(m_tree)) {
             getRobot().clickAtCurrentPosition(m_tree, count, button);
         } else {
             getRobot().click(m_tree, null, 
                 ClickOptions.create().setClickCount(count)
                     .setMouseButton(button));
         }
         
     }
     
     /**
      * {@inheritDoc}
      */
     public void gdClickDirect(int count, int button, int xPos, String xUnits, 
         int yPos, String yUnits) throws StepExecutionException {
         
         int correctedYPos = correctYPos(yPos, yUnits);
         super.gdClickDirect(count, button, xPos, xUnits, correctedYPos, yUnits);
     }   
     
     /**
      * Selects Checkbox of last node of the path given by <code>treepath</code>.
      * 
      * @param pathType whether the path is relative or absolute
      * @param preAscend
      *            Relative traversals will start this many parent nodes 
      *            above the current node. Absolute traversals ignore this 
      *            parameter.
      * @param treePath The tree path.
      * @param operator
      *  If regular expressions are used to match the tree path
      * @throws StepExecutionException If the tree path is invalid, if the
      * double-click to expand the node fails, or if the selection is invalid.
      */
     public void gdToggleCheckbox(String pathType, int preAscend, String
             treePath, String operator)
         throws StepExecutionException {
         toggleCheckBoxByPath(pathType, preAscend, 
                 createStringNodePath(splitTextTreePath(treePath), operator));
     }
     
     /**
      * Selects Checkbox of last node of the path given by <code>indexPath</code>
      * @param pathType whether the path is relative or absolute
      * @param preAscend
      *            Relative traversals will start this many parent nodes 
      *            above the current node. Absolute traversals ignore this 
      *            parameter.
      * @param indexPath the index path
      * @throws StepExecutionException if <code>indexPath</code> is not a valid
      * path
      */
     public void gdToggleCheckboxByIndices(String pathType, int preAscend, 
                     String indexPath)
         throws StepExecutionException {
     
         toggleCheckBoxByPath(pathType, preAscend,
                 createIndexNodePath(splitIndexTreePath(indexPath)));
     }
     
     /**
      * Verify Selection of checkbox of the node at the end of the <code>treepath</code>.
      * 
      * @param pathType whether the path is relative or absolute
      * @param preAscend
      *            Relative traversals will start this many parent nodes 
      *            above the current node. Absolute traversals ignore this 
      *            parameter.
      * @param treePath The tree path.
      * @param operator
      *  If regular expressions are used to match the tree path
      * @param checked true if checkbox of tree node is selected, false otherwise
      * @throws StepExecutionException If the tree path is invalid, if the
      * double-click to expand the node fails, or if the selection is invalid.
      */
     public void gdVerifyCheckbox(String pathType, int preAscend, String
             treePath, String operator, boolean checked)
         throws StepExecutionException {
         verifyCheckBoxByPath(pathType, preAscend, 
                 createStringNodePath(splitTextTreePath(treePath), operator), 
                 checked);    
     }
     
     /**
      * Verify Selection of checkbox of last node of the path given by <code>indexPath</code>
      * @param pathType whether the path is relative or absolute
      * @param preAscend
      *            Relative traversals will start this many parent nodes 
      *            above the current node. Absolute traversals ignore this 
      *            parameter.
      * @param indexPath the index path
      * @param checked true if checkbox of tree node is selected, false otherwise
      * @throws StepExecutionException if <code>indexPath</code> is not a valid
      * path
      */
     public void gdVerifyCheckboxByIndices(String pathType, int preAscend, 
                     String indexPath, boolean checked)
         throws StepExecutionException {
     
         verifyCheckBoxByPath(pathType, preAscend,
                 createIndexNodePath(splitIndexTreePath(indexPath)), 
                     checked);    
     }
     
     /**
      * Verifies whether the checkbox of the first selection in the tree is checked
      * 
      * @param checked true if checkbox of node is selected, false otherwise
      * @throws StepExecutionException If no node is selected or the verification fails.
      */
     public void gdVerifySelectedCheckbox(boolean checked)
         throws StepExecutionException {        
         Boolean checkSelected = ((Boolean)getEventThreadQueuer().invokeAndWait(
                 "gdVerifyTreeCheckbox", new IRunnable() { //$NON-NLS-1$
                     public Object run() {             
                         TreeItem node = m_tree.getSelection()[0];
                         return new Boolean(node.getChecked());
                     }            
                 }));       
         
         Verifier.equals(checked, checkSelected.booleanValue());
     }
 
     /**
      * Corrects the given Y position based on the height of the tree's header.
      * This ensures, for example, that test steps don't try to click within the
      * header (where we receive no confirmation events).
      * 
      * @param pos The Y position to correct.
      * @param units The units used for the Y position.
      * @return The corrected Y position.
      */
     private int correctYPos(int pos, String units) {
         int correctedPos = pos;
         int headerHeight = ((Integer)getEventThreadQueuer().invokeAndWait(
                 "getHeaderHeight", new IRunnable() { //$NON-NLS-1$
 
                     public Object run() throws StepExecutionException {
                         return new Integer(
                             ((Tree)getComponent()).getHeaderHeight());
                     }
             
                 })).intValue();
 
         if (POS_UNIT_PIXEL.equalsIgnoreCase(units)) {
             // Pixel units
             correctedPos += headerHeight;
         } else {
             // Percentage units
             int totalHeight = ((Integer)getEventThreadQueuer().invokeAndWait(
                     "getHeaderHeight", new IRunnable() { //$NON-NLS-1$
 
                         public Object run() throws StepExecutionException {
                             return new Integer(
                                 SwtUtils.getWidgetBounds(
                                     getComponent()).height);
                         }
             
                     })).intValue();
             long targetHeight = totalHeight - headerHeight;
             long targetPos = Math.round((double)targetHeight * (double)pos
                 / 100.0);
             targetPos += headerHeight;
             double heightPercentage = 
                 (double)targetPos / (double)totalHeight * 100.0;
             correctedPos = (int)Math.round(heightPercentage);
             if (correctedPos > 100) { // rounding error
                 correctedPos = 100;
             }
         }
         return correctedPos;
     }
 
     /**
      * Splits the <code>treepath</code> string into an array, one entry for each level in the path
      * 
      * @param treePath The tree path
      * @return An array of string representing the tree path
      */
     private String[] splitTextTreePath(String treePath) {
         return StringParsing.splitToArray(treePath, 
                 TestDataConstants.PATH_CHAR_DEFAULT,
                 TestDataConstants.ESCAPE_CHAR_DEFAULT, true);
     }
     
     /**
      * Splits the <code>treepath</code> string into an array, one entry for each level in the path
      * @param treePath The tree path
      * @return An array of indices (type <code>Integer</code>) representing the tree path
      * @throws StepExecutionException If the values of the passed path cannot be parsed
      */
     private Integer[] splitIndexTreePath(String treePath)
         throws StepExecutionException {
         
         Integer[] indexPath = null;
         String[] path = splitTextTreePath(treePath);
         if (path != null) {
             indexPath = new Integer[path.length];
             for (int i = 0; i < path.length; i++) {
                 indexPath[i] = new Integer(IndexConverter.intValue(path[i]));
             }
         }
         return IndexConverter.toImplementationIndices(indexPath);
     }
     
     /**
      * Splits the <code>treepath</code> string into an array, one entry for each level in the path
      * 
      * @param treePath The tree path
      * @param operator The operator
      * @return An array of string representing the tree path
      */
     private INodePath createStringNodePath(String [] treePath, 
             String operator) {
         return new StringNodePath(treePath, operator);
     }
     
     /**
      * Splits the <code>treepath</code> string into an array, one entry for each level in the path
      * 
      * @param treePath The tree path
      * @return An array of string representing the tree path
      */
     private INodePath createIndexNodePath(Integer [] treePath) {
         return new IndexNodePath(treePath);
     }
     
     /**
      * Traverses the tree by searching for the nodes in the tree
      * path entry and calling the given operation on each matching node.
      * @param treePath The tree path.
      * @param pathType For example, "relative" or "absolute".
      * @param preAscend
      *            Relative traversals will start this many parent nodes 
      *            above the current node. Absolute traversals ignore this 
      *            parameter.
      * @param operation The tree node operation.
      * @throws StepExecutionException If the path traversion fails.
      */
     private void traverseTreeByPath(INodePath treePath, String pathType,
             int preAscend, TreeNodeOperation operation) 
         throws StepExecutionException {
 
         Validate.notNull(treePath);
         Validate.notNull(operation); 
         
         TreeOperationContext context = new TreeOperationContext(
                 getEventThreadQueuer(), getRobot(), m_tree); 
         TreeItem startNode = getStartNode(pathType, preAscend, context); 
 
         AbstractTreeNodeTraverser traverser = 
             new PathBasedTraverser(context, treePath); 
         traverser.traversePath(operation, startNode);
     }    
     
     /**
      * Traverses the tree by searching for the nodes in the tree
      * path entry and calling the given operation on the last element in the path.
      * @param treePath The tree path.
      * @param pathType For example, "relative" or "absolute".
      * @param preAscend
      *            Relative traversals will start this many parent nodes 
      *            above the current node. Absolute traversals ignore this 
      *            parameter.
      * @param operation The tree node operation.
      * @throws StepExecutionException If the path traversion fails.
      */
     private void traverseLastElementByPath(INodePath treePath, 
             String pathType, int preAscend, TreeNodeOperation operation) 
         throws StepExecutionException {
 
         Validate.notNull(treePath);
         Validate.notNull(operation);
 
         TreeOperationContext context = new TreeOperationContext(
                 getEventThreadQueuer(), getRobot(), m_tree);
         TreeItem startNode = getStartNode(pathType, preAscend, context);
 
         AbstractTreeNodeTraverser traverser = new PathBasedTraverser(
                 context, treePath, new TreeNodeOperationConstraint());
         
         traverser.traversePath(operation, startNode);
     }
     /**
      * @param pathType
      *            For example, "relative" or "absolute".
      * @param preAscend
      *            Relative traversals will start this many parent nodes 
      *            above the current node. Absolute traversals ignore this 
      *            parameter.
      * @param context
      *            The context of the traversal.
      * @return The node at which to begin the traversal or <code>null</code>
      *         if the traversal should begin at the root of the node.
      */
     private TreeItem getStartNode(String pathType, final int preAscend, 
             final TreeOperationContext context) {
 
         TreeItem startNode;
         if (pathType.equals(CompSystemConstants.TREE_PATH_TYPE_RELATIVE)) {
             startNode = (TreeItem)getEventThreadQueuer().invokeAndWait(
                     "getStartNode", new IRunnable() { //$NON-NLS-1$
 
                         public Object run() throws StepExecutionException {
                             TreeItem curNode = getSelectedNode(context);
                             TreeItem child = curNode;
                             for (int i = 0; i < preAscend; ++i) {
                                 if (curNode == null) {
                                     TestErrorEvent event = 
                                         EventFactory.createActionError(
                                                 TestErrorEvent.
                                                     TREE_NODE_NOT_FOUND);
                                     throw new StepExecutionException(
                                             "Tree node not found: Parent of " //$NON-NLS-1$
                                             + child.toString(), event);
                                 }
                                 child = curNode;
                                 curNode = curNode.getParentItem();
                             }
                             return curNode;
                         }
 
                     });
         } else if (pathType.equals(
                 CompSystemConstants.TREE_PATH_TYPE_ABSOLUTE)) {
             startNode = null;
         } else {
             throw new StepExecutionException(
                     pathType + " is not a valid Path Type", EventFactory //$NON-NLS-1$
                     .createActionError(TestErrorEvent.INVALID_PARAM_VALUE));
 
         }
         
         return startNode;
     }
 
     /**
      * Returns the selected node
      * @param context context
      * @return node
      */
     private TreeItem getSelectedNode(TreeOperationContext context) {
         return (TreeItem)context.getSelectedNode();
     }
     
     /**
      * @param pathType pathType
      * @param preAscend
      *            Relative traversals will start this many parent nodes 
      *            above the current node. Absolute traversals ignore this 
      *            parameter.
      * @param objectPath objectPath
      * @param co the click options to use
      */
     private void selectByPath(String pathType, int preAscend, 
             INodePath objectPath, ClickOptions co) {
 
         TreeNodeOperation expOp = new ExpandCollapseTreeNodeOperation(false);
         TreeNodeOperation selectOp = new SelectTreeNodeOperation(co);
         INodePath subPath = objectPath.subPath(0, objectPath.getLength() - 1);
 
         traverseTreeByPath(subPath, pathType, preAscend, expOp);
         traverseLastElementByPath(objectPath, pathType, preAscend, selectOp);
     }
     
     /**
      * @param pathType pathType
      * @param preAscend
      *            Relative traversals will start this many parent nodes 
      *            above the current node. Absolute traversals ignore this 
      *            parameter.
      * @param objectPath objectPath
      */
     private void toggleCheckBoxByPath(String pathType, int preAscend, 
             INodePath objectPath) {
 
         TreeNodeOperation expOp = 
             new ExpandCollapseTreeNodeOperation(false);
         TreeOperationContext context = new TreeOperationContext(
                 getEventThreadQueuer(), getRobot(), m_tree);
         TreeNodeOperation selCheckboxOp = new ToggleCheckboxOperation(context);
         INodePath subPath = objectPath.subPath(0, objectPath.getLength() - 1);
         
         traverseTreeByPath(subPath, pathType, preAscend, expOp);
         traverseLastElementByPath(objectPath, pathType, preAscend,
                 selCheckboxOp);      
     }
     
     /**
      * @param pathType pathType
      * @param preAscend
      *            Relative traversals will start this many parent nodes 
      *            above the current node. Absolute traversals ignore this 
      *            parameter.
      * @param objectPath objectPath
      * @param checked true if Checkbox should be enabled, false otherwise
      */
     private void verifyCheckBoxByPath(String pathType, int preAscend, 
             INodePath objectPath, final boolean checked) {
 
         TreeNodeOperation expOp = 
             new ExpandCollapseTreeNodeOperation(false);
         TreeOperationContext context = new TreeOperationContext(
                 getEventThreadQueuer(), getRobot(), m_tree);
         TreeNodeOperation checkboxOp = new VerifyCheckboxOperation(
                 checked, context);
         INodePath subPath = objectPath.subPath(0, objectPath.getLength() - 1);
         
         traverseTreeByPath(subPath, pathType, preAscend, expOp);
         traverseLastElementByPath(objectPath, pathType, preAscend, checkboxOp);
     }
     
     /**
      * Drags the node of the given treePath.
      * @param mouseButton the mouse button to press.
      * @param modifier the modifier to press.
      * @param pathType whether the path is relative or absolute
      * @param preAscend
      *      Relative traversals will start this many parent nodes 
      *            above the current node. Absolute traversals ignore this 
      *            parameter.
      * @param treePath The tree path.
      * @param operator If regular expressions are used to match the tree path
      */
     public void gdDragByTextPath(int mouseButton, String modifier, 
             String pathType, int preAscend, String treePath, String operator) {
         
         final DragAndDropHelperSwt dndHelper = DragAndDropHelperSwt
             .getInstance();
         
         dndHelper.setMouseButton(mouseButton);
         dndHelper.setModifier(modifier);
         dndHelper.setDragComponent(null);
         gdSelect(pathType, preAscend, treePath, operator, 0, 1, 
                 CompSystemConstants.EXTEND_SELECTION_NO);
     }
     
     /**
      * Drops the before dragged object on the given treePath.
      * @param pathType whether the path is relative or absolute
      * @param preAscend
      *      Relative traversals will start this many parent nodes 
      *            above the current node. Absolute traversals ignore this 
      *            parameter.
      * @param treePath The tree path.
      * @param operator If regular expressions are used to match the tree path
      * @param delayBeforeDrop the amount of time (in milliseconds) to wait
      *                        between moving the mouse to the drop point and
      *                        releasing the mouse button                       
      */
     public void gdDropByTextPath(final String pathType, final int preAscend, 
             final String treePath, final String operator, int delayBeforeDrop) {
         
         final DragAndDropHelperSwt dndHelper = DragAndDropHelperSwt
             .getInstance();
         final IRobot robot = getRobot();
         try {
             pressOrReleaseModifiers(dndHelper.getModifier(), true);
             getEventThreadQueuer().invokeAndWait("gdDropByTextPath - perform drag", new IRunnable() { //$NON-NLS-1$
 
                 public Object run() throws StepExecutionException {
                     // drag
                     robot.mousePress(dndHelper.getDragComponent(), null, 
                             dndHelper.getMouseButton());                    
                     
                     shakeMouse();
 
                     return null;
                 }            
             });
 
             // Post a MouseMove event in order to break the Display out of its
             // post-drag "freeze". It appears as though the mouse position 
             // change needs to be extreme in order to nudge the Display back 
             // into action (i.e. (<mouse-location> + 1) was insufficient), 
             // so the default Event values (x, y = 0) are used.
             Event wakeEvent = new Event();
             wakeEvent.type = SWT.MouseMove;
             getComponent().getDisplay().post(wakeEvent);
             
             getEventThreadQueuer().invokeAndWait("gdDropByTextPath - perform drop", new IRunnable() { //$NON-NLS-1$
 
                 public Object run() throws StepExecutionException {
                     // drop
                     gdSelect(pathType, preAscend, treePath, operator, 0, 1,
                             CompSystemConstants.EXTEND_SELECTION_NO);
                     return null;
                 }            
             });
 
             waitBeforeDrop(delayBeforeDrop);
             
         } finally {
             robot.mouseRelease(null, null, dndHelper.getMouseButton());
             pressOrReleaseModifiers(dndHelper.getModifier(), false);
         }
     }
     
     /**
      * Drags the node of the given indexPath.
      * @param mouseButton the mouse button to press.
      * @param modifier the modifier to press.
      * @param pathType whether the path is relative or absolute
      * @param preAscend
      *      Relative traversals will start this many parent nodes 
      *            above the current node. Absolute traversals ignore this 
      *            parameter.
      * @param indexPath The index path.
      */
     public void gdDragByIndexPath(int mouseButton, String modifier, 
             String pathType, int preAscend, String indexPath) {
         
         final DragAndDropHelperSwt dndHelper = DragAndDropHelperSwt
             .getInstance();
         
         dndHelper.setMouseButton(mouseButton);
         dndHelper.setModifier(modifier);
         dndHelper.setDragComponent(null);
         gdSelectByIndices(pathType, preAscend, indexPath, 0, 1, 
                 CompSystemConstants.EXTEND_SELECTION_NO);
     }
     
     
     /**
      * Drops the before dragged object on the given indexPath.
      * @param pathType whether the path is relative or absolute
      * @param preAscend
      *      Relative traversals will start this many parent nodes 
      *            above the current node. Absolute traversals ignore this 
      *            parameter.
      * @param indexPath The index path.
      * @param delayBeforeDrop the amount of time (in milliseconds) to wait
      *                        between moving the mouse to the drop point and
      *                        releasing the mouse button                       
      */
     public void gdDropByIndexPath(final String pathType, final int preAscend, 
             final String indexPath, int delayBeforeDrop) {
         
         final DragAndDropHelperSwt dndHelper = DragAndDropHelperSwt
             .getInstance();
         final IRobot robot = getRobot();
         try {
             pressOrReleaseModifiers(dndHelper.getModifier(), true);
             getEventThreadQueuer().invokeAndWait("gdDropByIndexPath - perform drag", new IRunnable() { //$NON-NLS-1$
 
                 public Object run() throws StepExecutionException {
                     // drag
                     robot.mousePress(dndHelper.getDragComponent(), null, 
                             dndHelper.getMouseButton());
 
                     shakeMouse();
 
                     return null;
                 }            
             });
 
             // Post a MouseMove event in order to break the Display out of its
             // post-drag "freeze". It appears as though the mouse position 
             // change needs to be extreme in order to nudge the Display back 
             // into action (i.e. (<mouse-location> + 1) was insufficient), 
             // so the default Event values (x, y = 0) are used.
             Event wakeEvent = new Event();
             wakeEvent.type = SWT.MouseMove;
             getComponent().getDisplay().post(wakeEvent);
             
             getEventThreadQueuer().invokeAndWait("gdDropByIndexPath - perform drop", new IRunnable() { //$NON-NLS-1$
 
                 public Object run() throws StepExecutionException {
                     // drop
                     gdSelectByIndices(pathType, preAscend, indexPath, 0, 1, 
                             CompSystemConstants.EXTEND_SELECTION_NO);
                     return null;
                 }            
             });
 
             waitBeforeDrop(delayBeforeDrop);
         } finally {
             robot.mouseRelease(null, null, dndHelper.getMouseButton());
             pressOrReleaseModifiers(dndHelper.getModifier(), false);
         }
     }
     
     /**
      * {@inheritDoc}
      */
     public String[] getTextArrayFromComponent() {
         return null;
     }
 
     /**
      * 
      * Unfortunately, we cannot just use {@link Tree#getItem(Point)}, as it is 
      * based on what is <em>selectable</em>, rather than on what has visible 
      * text.
      * 
      * @return the {@link TreeItem} under the current mouse position.
      * @throws StepExecutionException If no {@link TreeItem} is found at the 
      *                                current mouse position.
      */
     protected TreeItem getItemAtMousePosition() throws StepExecutionException {
         return (TreeItem)getEventThreadQueuer().invokeAndWait("getItemAtMousePosition", new IRunnable() { //$NON-NLS-1$
             
             public Object run() throws StepExecutionException {
                 Point mousePos = SwtUtils.convertToSwtPoint(
                         getRobot().getCurrentMousePosition());
                 ItemAtPointTreeNodeOperation op = 
                     new ItemAtPointTreeNodeOperation(
                             mousePos, SwtUtils.getWidgetBounds(m_tree));
 
                 TreeItem topItem = m_tree.getTopItem();
                 if (topItem != null) {
                     
                     // FIXME zeb This may be slow for very large trees, as the 
                     //           search may continue long past the
                     //           visible client area of the tree.
                     //           It may also cause problems with regard to 
                     //           lazy/virtual nodes. 
                     StandardDepthFirstTraverser traverser = 
                         new StandardDepthFirstTraverser(
                             new TreeOperationContext(
                                 getEventThreadQueuer(), getRobot(), m_tree));
                     traverser.traversePath(op, topItem);
                     if (op.getItemAtPoint() != null) {
                         return op.getItemAtPoint();
                     }
                     
                 }
 
                 throw new StepExecutionException("No tree node found at mouse position.", //$NON-NLS-1$
                         EventFactory.createActionError(
                                 TestErrorEvent.NOT_FOUND));
             }
         });
     }
 
     /**
      * {@inheritDoc}
      */
     public String gdStoreSelectedNodeValue(String variable) {
         TreeOperationContext context = new TreeOperationContext(
                 getEventThreadQueuer(), getRobot(), m_tree);
         TreeItem selectedNode = 
             getSelectedNode(context);
         if (selectedNode == null) {
             throw new StepExecutionException("No tree item selected", //$NON-NLS-1$ 
                 EventFactory.createActionError(TestErrorEvent.NO_SELECTION));
         }
         
         return context.getRenderedText(selectedNode);
     }
 
 }
