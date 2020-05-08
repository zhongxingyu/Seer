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
 package org.eclipse.jubula.client.ui.rcp.handlers;
 
 
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.window.Window;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.DataState;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.UpdateState;
 import org.eclipse.jubula.client.core.model.IComponentNamePO;
 import org.eclipse.jubula.client.core.model.IObjectMappingAssoziationPO;
 import org.eclipse.jubula.client.core.model.IObjectMappingCategoryPO;
 import org.eclipse.jubula.client.core.model.PoMaker;
 import org.eclipse.jubula.client.ui.constants.ContextHelpIds;
 import org.eclipse.jubula.client.ui.constants.IconConstants;
 import org.eclipse.jubula.client.ui.handlers.AbstractSelectionBasedHandler;
 import org.eclipse.jubula.client.ui.rcp.Plugin;
 import org.eclipse.jubula.client.ui.rcp.dialogs.InputDialog;
 import org.eclipse.jubula.client.ui.rcp.editors.JBEditorHelper.EditableState;
 import org.eclipse.jubula.client.ui.rcp.editors.ObjectMappingMultiPageEditor;
 import org.eclipse.jubula.client.ui.rcp.i18n.Messages;
 import org.eclipse.jubula.client.ui.utils.DialogUtils;
 import org.eclipse.ui.handlers.HandlerUtil;
 
 
 /**
  * @author BREDEX GmbH
  * @created 27.04.2005
  */
 public class OMNewCategoryHandler extends AbstractSelectionBasedHandler {
    /** {@inheritDoc} */
     public Object executeImpl(ExecutionEvent event) throws ExecutionException {
         final ObjectMappingMultiPageEditor editor = 
             ((ObjectMappingMultiPageEditor)
                     HandlerUtil.getActivePartChecked(event));
         IStructuredSelection selection = getSelection();
         if (selection.size() == 1) { 
             ISelectionProvider selectionProvider = 
                 HandlerUtil.getActiveSiteChecked(event)
                     .getSelectionProvider();
             createNewCategory(selection.getFirstElement(), editor, 
                     selectionProvider);
         }
         return null;
     }
 
     /**
      * 
      * @param selElement The selected element.
      * @param editor The active editor.
      * @param selectionProvider The active selection provider. This will be used
      *                          to set the new selection.
      */
     private void createNewCategory(Object selElement, 
             final ObjectMappingMultiPageEditor editor, 
             ISelectionProvider selectionProvider) {
         
         IObjectMappingCategoryPO category = null;
         if (selElement instanceof IObjectMappingCategoryPO) {
             category = (IObjectMappingCategoryPO)selElement;
         } else if (selElement instanceof IObjectMappingAssoziationPO) {
             category = 
                 ((IObjectMappingAssoziationPO)selElement).getCategory();
         } else if (selElement instanceof IComponentNamePO) {
             category = editor.getOmEditorBP().getCategory(
                     (IComponentNamePO)selElement);
         }
         final IObjectMappingCategoryPO node = category;
         InputDialog dialog = 
             new InputDialog(getActiveShell(), 
                 Messages.OMNewCategoryActionTitle,
                 Messages.OMNewCategoryActionName,
                 Messages.OMNewCategoryActionMessage,
                 Messages.OMNewCategoryActionLabel,
                 Messages.OMNewCategoryActionError1,
                 Messages.OMNewCategoryActionDoubleCatName,
                 IconConstants.NEW_CAT_DIALOG_STRING,
                 Messages.OMNewCategoryActionShell,
                 false) {
 
                 /**
                  * @return False, if the input name already exists.
                  */
                 protected boolean isInputAllowed() {
                     return !editor.getOmEditorBP().existCategory(
                             node, getInputFieldText());
                 }
             };
         if (node != null) {
             dialog.setHelpAvailable(true);
             dialog.create();
             DialogUtils.setWidgetNameForModalDialog(dialog);
             Plugin.getHelpSystem().setHelp(dialog.getShell(), 
                 ContextHelpIds.DIALOG_OM_CAT_NEW);
             dialog.open();
             if (dialog.getReturnCode() == Window.OK) {
                 if (editor.getEditorHelper().requestEditableState() 
                         != EditableState.OK) {
                     return;
                 }
 
                 IObjectMappingCategoryPO newCategory = 
                     PoMaker.createObjectMappingCategoryPO(dialog.getName());
                 node.addCategory(newCategory);
                 editor.getEditorHelper().setDirty(true);
 
                 DataEventDispatcher.getInstance().fireDataChangedListener(
                         node, DataState.StructureModified, 
                         UpdateState.onlyInEditor);
                 DataEventDispatcher.getInstance().fireDataChangedListener(
                         newCategory, DataState.Added, 
                         UpdateState.onlyInEditor);
                 StructuredSelection newSel = 
                     new StructuredSelection(newCategory);
                 if (selectionProvider != null) {
                     selectionProvider.setSelection(newSel);
                 }
             }
         }
     }
 }
