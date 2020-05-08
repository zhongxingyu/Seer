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
 package org.eclipse.jubula.client.ui.rcp.controllers.dnd;
 
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerDropAdapter;
 import org.eclipse.jubula.client.core.model.INodePO;
 import org.eclipse.jubula.client.core.model.IRefTestSuitePO;
 import org.eclipse.jubula.client.core.model.ITestJobPO;
 import org.eclipse.jubula.client.core.model.ITestSuitePO;
 import org.eclipse.jubula.client.core.persistence.PMAlreadyLockedException;
 import org.eclipse.jubula.client.core.persistence.PMDirtyVersionException;
 import org.eclipse.jubula.client.core.persistence.PMException;
 import org.eclipse.jubula.client.core.persistence.PMReadException;
 import org.eclipse.jubula.client.ui.constants.Constants;
 import org.eclipse.jubula.client.ui.rcp.Plugin;
 import org.eclipse.jubula.client.ui.rcp.controllers.PMExceptionHandler;
 import org.eclipse.jubula.client.ui.rcp.editors.JBEditorHelper;
 import org.eclipse.jubula.client.ui.rcp.editors.NodeEditorInput;
 import org.eclipse.jubula.client.ui.rcp.editors.TestJobEditor;
 import org.eclipse.jubula.client.ui.rcp.views.TestSuiteBrowser;
 import org.eclipse.ui.IViewPart;
 
 
 /**
  * Utility class containing methods for use in drag and drop as well as 
  * cut and paste operations in the Test Job Editor.
  *
  * @author BREDEX GmbH
  * @created Mar 17, 2010
  */
 public class TJEditorDndSupport {
 
     /**
      * Private constructor
      */
     private TJEditorDndSupport() {
         // Do nothing
     }
 
     /**
      * 
      * @param targetEditor The editor to which the item is to be dropped/pasted.
      * @param toDrop The items that were dragged/cut.
      * @param dropTarget The drop/paste target.
      * @param dropPosition One of the values defined in ViewerDropAdapter to 
      *                     indicate the drop position relative to the drop
      *                     target.
      * @return <code>true</code> if the drop/paste was successful. 
      *         Otherwise <code>false</code>.
      */
     public static boolean performDrop(TestJobEditor targetEditor,
             IStructuredSelection toDrop, INodePO dropTarget, int dropPosition) {
         if (targetEditor.getEditorHelper().requestEditableState() 
                 != JBEditorHelper.EditableState.OK) {
             return false;
         }
         List<Object> selectedElements = toDrop.toList();
         Collections.reverse(selectedElements);
         Iterator iter = selectedElements.iterator();
         while (iter.hasNext()) {
            Object objectToDrop = iter.next();
            if (objectToDrop instanceof ITestSuitePO) {
                ITestSuitePO testSuite = (ITestSuitePO)objectToDrop;
                 if (dropTarget != testSuite) {
                     try {
                         if (dropTarget instanceof IRefTestSuitePO) {
                             dropOnRefTS(
                                     testSuite, dropTarget, dropPosition);
                         } else if (dropTarget instanceof ITestJobPO) {
                             dropOnTJ(testSuite, dropTarget);
                         } 
                     } catch (PMException e) {
                         NodeEditorInput inp = (NodeEditorInput)targetEditor.
                             getAdapter(NodeEditorInput.class);
                         INodePO inpNode = inp.getNode();
                         PMExceptionHandler.handlePMExceptionForMasterSession(e);
 
                         // If an object was already locked, *and* the locked 
                         // object is not the editor Test Case, *and* the editor 
                         // is dirty, then we do *not* want to revert all 
                         // editor changes.
                         // The additional test as to whether the the editor is 
                         // marked as dirty is important because, due to the 
                         // requestEditableState() call earlier in this method, 
                         // the editor TC is locked (even though the editor 
                         // isn't dirty). Reopening the editor removes this lock.
                         if (!(e instanceof PMAlreadyLockedException
                                 && ((PMAlreadyLockedException)e)
                                     .getLockedObject() != null
                                 && !((PMAlreadyLockedException)e)
                                     .getLockedObject().equals(inpNode))
                             || !targetEditor.isDirty()) {
                             
                             try {
                                 targetEditor.reOpenEditor(inpNode);
                             } catch (PMException e1) {
                                 PMExceptionHandler.handlePMExceptionForEditor(e,
                                         targetEditor);
                             }
                         }
                         return false;
                     }
                 }
             }
            if (objectToDrop instanceof INodePO) {
                INodePO nodeToDrop = (INodePO)objectToDrop;
                if (nodeToDrop instanceof IRefTestSuitePO) {
                     INodePO target = dropTarget;
                    if (target != nodeToDrop
                             && (target instanceof IRefTestSuitePO)) {
                        moveNode(nodeToDrop, target);
                     }
                 }
                postDropAction(nodeToDrop, targetEditor);
             }
         }
         return true;
     }
 
     /**
      * 
      * @param sourceViewer The viewer containing the dragged/cut item.
      * @param targetViewer The viewer to which the item is to be dropped/pasted.
      * @param toDrop The items that were dragged/cut.
      * @param dropTarget The drop/paste target.
      * @param allowFromBrowser Whether items from the Test Case Suite are 
      *                         allowed to be dropped/pasted.
      * @return <code>true</code> if the given information indicates that the
      *         drop/paste is valid. Otherwise <code>false</code>.
      */
     public static boolean validateDrop(Viewer sourceViewer,
             Viewer targetViewer, IStructuredSelection toDrop,
             INodePO dropTarget, boolean allowFromBrowser) {
         if (toDrop == null || toDrop.isEmpty() || dropTarget == null) {
             return false;
         }
         if (sourceViewer != null && !sourceViewer.equals(targetViewer)) {
             if (getTSBrowser() != null) {
                 if (!(allowFromBrowser && sourceViewer.equals(getTSBrowser()
                         .getTreeViewer()))) {
                     return false;
                 }
             } else {
                 return false;
             }
         }
 
         for (Object toDropElement : toDrop.toArray()) {
             if (!(toDropElement instanceof ITestSuitePO 
                     || toDropElement instanceof IRefTestSuitePO)) {
                 return false;
             }
         }
 
         return true;
     }
 
     /**
      * @return instance of TestSuiteBrowser, or null.
      */
     static TestSuiteBrowser getTSBrowser() {
         IViewPart viewPart = Plugin.getView(Constants.TS_BROWSER_ID);
         if (viewPart != null) {
             return (TestSuiteBrowser)viewPart;
         }
         return null;
     }
 
     /**
      * @param node the node to be dropped.
      * @param target the target node.
      * @throws PMReadException in case of db read error
      * @throws PMDirtyVersionException in case of version conflict (dirty read)
      * @throws PMAlreadyLockedException if the origSpecTc is already locked by another user
      * @throws PMException in case of unspecified db error
      */
     private static void dropOnTJ(ITestSuitePO node, INodePO target)
         throws PMReadException, PMAlreadyLockedException,
             PMDirtyVersionException, PMException {
         if (getTSBrowser() != null) {
             getTSBrowser().addReferencedTestSuite(
                     node, target, 0);
         }
     }
 
     /**
      * @param node the node to be dropped
      * @param target the target node.
      * @param location One of the values defined in ViewerDropAdapter to 
      *                     indicate the drop position relative to the drop
      *                     target.
      * @throws PMReadException in case of db read error
      * @throws PMDirtyVersionException in case of version conflict (dirty read)
      * @throws PMAlreadyLockedException if the origSpecTc is already locked by another user
      * @throws PMException in case of unspecified db error
      */
     private static void dropOnRefTS(ITestSuitePO node, INodePO target, 
             int location) throws PMReadException, PMAlreadyLockedException, 
             PMDirtyVersionException, PMException {
         INodePO parentGUI = target.getParentNode();
         int position = parentGUI.indexOf(target);
         if (location != ViewerDropAdapter.LOCATION_BEFORE) {
             position++;
         }
         getTSBrowser().addReferencedTestSuite(
                 node, parentGUI, position);
     }
 
     /**
      * @param node the node to be moved.
      * @param target the target node.
      * @return the dropped node.
      */
     private static INodePO moveNode(INodePO node, INodePO target) {
         int newPos = target.getParentNode().indexOf(target);
         node.getParentNode().removeNode(node);
         target.getParentNode().addNode(newPos, node);
         return node;
     }
 
     /**
      * Executes actions after the drop.
      * @param node the dropped node. 
      * @param targetEditor The editor to which the item has been dropped/pasted.
      */
     private static void postDropAction(INodePO node, 
             TestJobEditor targetEditor) {
         targetEditor.setFocus();
         if (node != null) {
             targetEditor.getTreeViewer().setSelection(
                     new StructuredSelection(node));            
         }
         targetEditor.getTreeViewer().refresh();
         targetEditor.getEditorHelper().setDirty(true);
         LocalSelectionTransfer.getInstance().setSelection(null);
     }
 }
