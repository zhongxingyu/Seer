 /*******************************************************************************
  * Copyright (c) 2011 Formal Mind GmbH and University of Dusseldorf.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Lukas Ladenberger - initial API and implementation
  ******************************************************************************/
 
 package org.eclipse.rmf.pror.reqif10.editor.agilegrid;
 
 import java.beans.PropertyChangeListener;
 
 import org.agilemore.agilegrid.CellEditor;
 import org.eclipse.emf.edit.ui.EMFEditUIPlugin;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.actions.ActionFactory;
 
 /**
  * <p>
  * Handles the redirection of the global actions Cut, Copy, Paste, Delete,
  * Select All, Find, Undo and Redo to either the current activated agile grid
  * cell editor or the specification editor's supplied action handler (EMF
  * specific actions).
  * </p>
  * <p>
  * This class may be instantiated; it is not intended to be subclassed.
  * </p>
  */
 public class AgileCellEditorActionHandler {
 	
 	private final ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
 	
     private CutActionHandler cellCutAction = new CutActionHandler();
 
     private CopyActionHandler cellCopyAction = new CopyActionHandler();
 
     private PasteActionHandler cellPasteAction = new PasteActionHandler();
 
     private DeleteActionHandler cellDeleteAction = new DeleteActionHandler();
 
     private SelectAllActionHandler cellSelectAllAction = new SelectAllActionHandler();
 
     private FindActionHandler cellFindAction = new FindActionHandler();
 
     private UndoActionHandler cellUndoAction = new UndoActionHandler();
 
     private RedoActionHandler cellRedoAction = new RedoActionHandler();
 
     private IAction cutAction;
 
     private IAction copyAction;
 
     private IAction pasteAction;
 
     private IAction deleteAction;
 
     private IAction selectAllAction;
 
     private IAction findAction;
 
     private IAction undoAction;
 
     private IAction redoAction;
 
     private IPropertyChangeListener cutActionListener = new ActionEnabledChangeListener(
             cellCutAction);
 
     private IPropertyChangeListener copyActionListener = new ActionEnabledChangeListener(
             cellCopyAction);
 
     private IPropertyChangeListener pasteActionListener = new ActionEnabledChangeListener(
             cellPasteAction);
 
     private IPropertyChangeListener deleteActionListener = new ActionEnabledChangeListener(
             cellDeleteAction);
 
     private IPropertyChangeListener selectAllActionListener = new ActionEnabledChangeListener(
             cellSelectAllAction);
 
     private IPropertyChangeListener findActionListener = new ActionEnabledChangeListener(
             cellFindAction);
 
     private IPropertyChangeListener undoActionListener = new ActionEnabledChangeListener(
             cellUndoAction);
 
     private IPropertyChangeListener redoActionListener = new ActionEnabledChangeListener(
             cellRedoAction);
 
     private CellEditor activeEditor;
 
     private PropertyChangeListener cellListener = new CellChangeListener();
 
     private Listener controlListener = new ControlListener();
 
     //private HashMap<Control,CellEditor> controlToEditor = new HashMap<Control,CellEditor>();
 
     private class ControlListener implements Listener {
         public void handleEvent(Event event) {
             switch (event.type) {
             case SWT.Activate:
                 //activeEditor = (CellEditor) controlToEditor.get(event.widget);
                 if (activeEditor != null) {
                 	activeEditor.addPropertyChangeListener(cellListener);
 				}
                 updateActionsEnableState();
                 break;
             case SWT.Deactivate:
                 if (activeEditor != null) {
 					activeEditor.removePropertyChangeListener(cellListener);
 				}
                 activeEditor = null;
                 updateActionsEnableState();
                 break;
             default:
                 break;
             }
         }
     }
 
     private class ActionEnabledChangeListener implements
             IPropertyChangeListener {
         private IAction actionHandler;
 
         protected ActionEnabledChangeListener(IAction actionHandler) {
             super();
             this.actionHandler = actionHandler;
         }
 
         public void propertyChange(PropertyChangeEvent event) {
             if (activeEditor != null) {
 				return;
 			}
             if (event.getProperty().equals(IAction.ENABLED)) {
                 Boolean bool = (Boolean) event.getNewValue();
                 actionHandler.setEnabled(bool.booleanValue());
                 return;
             }
             // If the underlying action's text has changed, we need to
             // change the text.  See
             // https://bugs.eclipse.org/bugs/show_bug.cgi?id=154410
             if (event.getProperty().equals(IAction.TEXT)) {
                 String text = (String) event.getNewValue();
                 actionHandler.setText(text);
                 return;
             }
             if (event.getProperty().equals(IAction.TOOL_TIP_TEXT)) {
                 String text = (String) event.getNewValue();
                 actionHandler.setToolTipText(text);
                 return;
             }
         }
     }
 
     private class CellChangeListener implements PropertyChangeListener {
 
 		public void propertyChange(java.beans.PropertyChangeEvent event) {
             if (activeEditor == null) {
 				return;
 			}
             if (event.getPropertyName().equals(CellEditor.CUT)) {
                 cellCutAction.setEnabled(activeEditor.isCutEnabled());
                 return;
             }
             if (event.getPropertyName().equals(CellEditor.COPY)) {
                 cellCopyAction.setEnabled(activeEditor.isCopyEnabled());
                 return;
             }
             if (event.getPropertyName().equals(CellEditor.PASTE)) {
                 cellPasteAction.setEnabled(activeEditor.isPasteEnabled());
                 return;
             }
             if (event.getPropertyName().equals(CellEditor.DELETE)) {
                 cellDeleteAction.setEnabled(activeEditor.isDeleteEnabled());
                 return;
             }
             if (event.getPropertyName().equals(CellEditor.SELECT_ALL)) {
                 cellSelectAllAction.setEnabled(activeEditor
                         .isSelectAllEnabled());
                 return;
             }
             if (event.getPropertyName().equals(CellEditor.FIND)) {
                 cellFindAction.setEnabled(activeEditor.isFindEnabled());
                 return;
             }
             if (event.getPropertyName().equals(CellEditor.UNDO)) {
                 cellUndoAction.setEnabled(activeEditor.isUndoEnabled());
                 return;
             }
             if (event.getPropertyName().equals(CellEditor.REDO)) {
                 cellRedoAction.setEnabled(activeEditor.isRedoEnabled());
                 return;
             }
 		}
 		
     }
 
     private class CutActionHandler extends Action {
         protected CutActionHandler() {
         	super(EMFEditUIPlugin.INSTANCE.getString("_UI_Cut_menu_item"));
             setId("CellEditorCutActionHandler");//$NON-NLS-1$
             setEnabled(false);
             setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_CUT));
             //PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IWorkbenchHelpContextIds.CELL_CUT_ACTION);
         }
 
         public void runWithEvent(Event event) {
             if (activeEditor != null) {
                 activeEditor.performCut();
                 return;
             }
             if (cutAction != null) {
                 cutAction.runWithEvent(event);
                 return;
             }
         }
 
         public void updateEnabledState() {
             if (activeEditor != null) {
                 setEnabled(activeEditor.isCutEnabled());
                 return;
             }
             if (cutAction != null) {
                 setEnabled(cutAction.isEnabled());
                 return;
             }
             setEnabled(false);
         }
     }
 
     private class CopyActionHandler extends Action {
 		protected CopyActionHandler() {
 			super(EMFEditUIPlugin.INSTANCE.getString("_UI_Copy_menu_item"));
 			setId("CellEditorCopyActionHandler");//$NON-NLS-1$
 			setEnabled(false);
 			setImageDescriptor(sharedImages
 					.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
 			// PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
 			// IWorkbenchHelpContextIds.CELL_COPY_ACTION);
 		}
 
         public void runWithEvent(Event event) {
             if (activeEditor != null) {
                 activeEditor.performCopy();
                 return;
             }
             if (copyAction != null) {
                 copyAction.runWithEvent(event);
                 return;
             }
         }
 
         public void updateEnabledState() {
             if (activeEditor != null) {
                 setEnabled(activeEditor.isCopyEnabled());
                 return;
             }
             if (copyAction != null) {
                 setEnabled(copyAction.isEnabled());
                 return;
             }
             setEnabled(false);
         }
     }
 
     private class PasteActionHandler extends Action {
         protected PasteActionHandler() {
         	super(EMFEditUIPlugin.INSTANCE.getString("_UI_Paste_menu_item"));
             setId("CellEditorPasteActionHandler");//$NON-NLS-1$
             setEnabled(false);
             setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
             //PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IWorkbenchHelpContextIds.CELL_PASTE_ACTION);
         }
 
         public void runWithEvent(Event event) {
             if (activeEditor != null) {
                 activeEditor.performPaste();
                 return;
             }
             if (pasteAction != null) {
                 pasteAction.runWithEvent(event);
                 return;
             }
         }
 
         public void updateEnabledState() {
             if (activeEditor != null) {
                 setEnabled(activeEditor.isPasteEnabled());
                 return;
             }
             if (pasteAction != null) {
                 setEnabled(pasteAction.isEnabled());
                 return;
             }
             setEnabled(false);
         }
     }
 
     private class DeleteActionHandler extends Action {
         protected DeleteActionHandler() {
         	super(EMFEditUIPlugin.INSTANCE.getString("_UI_Delete_menu_item"));
             setId("CellEditorDeleteActionHandler");//$NON-NLS-1$
             setEnabled(false);
             setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
            // PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IWorkbenchHelpContextIds.CELL_DELETE_ACTION);
         }
 
         public void runWithEvent(Event event) {
             if (activeEditor != null) {
                 activeEditor.performDelete();
                 return;
             }
             if (deleteAction != null) {
                 deleteAction.runWithEvent(event);
                 return;
             }
         }
 
         public void updateEnabledState() {
             if (activeEditor != null) {
                 setEnabled(activeEditor.isDeleteEnabled());
                 return;
             }
             if (deleteAction != null) {
                 setEnabled(deleteAction.isEnabled());
                 return;
             }
             setEnabled(false);
         }
     }
 
     private class SelectAllActionHandler extends Action {
         protected SelectAllActionHandler() {
         	super("Select All");
             setId("CellEditorSelectAllActionHandler");//$NON-NLS-1$
             setEnabled(false);
             //PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IWorkbenchHelpContextIds.CELL_SELECT_ALL_ACTION);
         }
 
         public void runWithEvent(Event event) {
             if (activeEditor != null) {
                 activeEditor.performSelectAll();
                 return;
             }
             if (selectAllAction != null) {
                 selectAllAction.runWithEvent(event);
                 return;
             }
         }
 
         public void updateEnabledState() {
             if (activeEditor != null) {
                 setEnabled(activeEditor.isSelectAllEnabled());
                 return;
             }
             if (selectAllAction != null) {
                 setEnabled(selectAllAction.isEnabled());
                 return;
             }
             setEnabled(false);
         }
     }
 
     private class FindActionHandler extends Action {
         protected FindActionHandler() {
             setId("CellEditorFindActionHandler");//$NON-NLS-1$
             setEnabled(false);
            // PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IWorkbenchHelpContextIds.CELL_FIND_ACTION);
         }
 
         public void runWithEvent(Event event) {
             if (activeEditor != null) {
                 activeEditor.performFind();
                 return;
             }
             if (findAction != null) {
                 findAction.runWithEvent(event);
                 return;
             }
         }
 
         public void updateEnabledState() {
             if (activeEditor != null) {
                 setEnabled(activeEditor.isFindEnabled());
                 return;
             }
             if (findAction != null) {
                 setEnabled(findAction.isEnabled());
                 return;
             }
             setEnabled(false);
         }
     }
 
     private class UndoActionHandler extends Action {
         protected UndoActionHandler() {
         	super(EMFEditUIPlugin.INSTANCE.getString("_UI_Undo_menu_item"));
             setId("CellEditorUndoActionHandler");//$NON-NLS-1$
             setEnabled(false);
             setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_UNDO));
             //PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IWorkbenchHelpContextIds.CELL_UNDO_ACTION);
         }
 
         public void runWithEvent(Event event) {
             if (activeEditor != null) {
                 activeEditor.performUndo();
                 return;
             }
             if (undoAction != null) {
                 undoAction.runWithEvent(event);
                 return;
             }
         }
 
         public void updateEnabledState() {
             if (activeEditor != null) {
                 setEnabled(activeEditor.isUndoEnabled());
                 //setText(WorkbenchMessages.Workbench_undo);
                 //setToolTipText(WorkbenchMessages.Workbench_undoToolTip);
                 return;
             }
             if (undoAction != null) {
                 setEnabled(undoAction.isEnabled());
                 setText(undoAction.getText());
                 setToolTipText(undoAction.getToolTipText());
                 return;
             }
             setEnabled(false);
         }
     }
 
     private class RedoActionHandler extends Action {
         protected RedoActionHandler() {
         	super(EMFEditUIPlugin.INSTANCE.getString("_UI_Redo_menu_item"));
             setId("CellEditorRedoActionHandler");//$NON-NLS-1$
             setEnabled(false);
             setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_REDO));
             //PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IWorkbenchHelpContextIds.CELL_REDO_ACTION);
         }
 
         public void runWithEvent(Event event) {
             if (activeEditor != null) {
                 activeEditor.performRedo();
                 return;
             }
             if (redoAction != null) {
                 redoAction.runWithEvent(event);
                 return;
             }
         }
 
         public void updateEnabledState() {
             if (activeEditor != null) {
                 setEnabled(activeEditor.isRedoEnabled());
                 //setText(WorkbenchMessages.Workbench_redo);
                // setToolTipText(WorkbenchMessages.Workbench_redoToolTip);
                 return;
             }
             if (redoAction != null) {
                 setEnabled(redoAction.isEnabled());
                 setText(redoAction.getText());
                 setToolTipText(redoAction.getToolTipText());
                 return;
             }
             setEnabled(false);
         }
     }
 
     /**
      * Creates a <code>CellEditor</code> action handler
      * for the global Cut, Copy, Paste, Delete, Select All,
      * Find, Undo, and Redo of the action bar.
      *
      * @param actionBar the action bar to register global
      *    action handlers.
      */
     public AgileCellEditorActionHandler(IActionBars actionBar) {
         super();
         actionBar.setGlobalActionHandler(ActionFactory.CUT.getId(),
                 cellCutAction);
         actionBar.setGlobalActionHandler(ActionFactory.COPY.getId(),
                 cellCopyAction);
         actionBar.setGlobalActionHandler(ActionFactory.PASTE.getId(),
                 cellPasteAction);
         actionBar.setGlobalActionHandler(ActionFactory.DELETE.getId(),
                 cellDeleteAction);
         actionBar.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(),
                 cellSelectAllAction);
         actionBar.setGlobalActionHandler(ActionFactory.FIND.getId(),
                 cellFindAction);
         actionBar.setGlobalActionHandler(ActionFactory.UNDO.getId(),
                 cellUndoAction);
         actionBar.setGlobalActionHandler(ActionFactory.REDO.getId(),
                 cellRedoAction);
     }
 
 //    /**
 //     * Adds a <code>CellEditor</code> to the handler so that the
 //     * Cut, Copy, Paste, Delete, Select All, Find, Undo, and Redo
 //     * actions are redirected to it when active.
 //     *
 //     * @param editor the <code>CellEditor</code>
 //     */
 //    public void addCellEditor(CellEditor editor) {
 //        if (editor == null) {
 //			return;
 //		}
 //
 //        Control control = editor.getControl();
 //        Assert.isNotNull(control);
 //        controlToEditor.put(control, editor);
 //        control.addListener(SWT.Activate, controlListener);
 //        control.addListener(SWT.Deactivate, controlListener);
 //
 //        if (control.isFocusControl()) {
 //            activeEditor = editor;
 //            editor.addPropertyChangeListener(cellListener);
 //            updateActionsEnableState();
 //        }
 //    }
 
     /**
      * Disposes of this action handler
      */
     public void dispose() {
         setCutAction(null);
         setCopyAction(null);
         setPasteAction(null);
         setDeleteAction(null);
         setSelectAllAction(null);
         setFindAction(null);
         setUndoAction(null);
         setRedoAction(null);
 
 //        Iterator<Control> itr = controlToEditor.keySet().iterator();
 //        while (itr.hasNext()) {
 //            Control control = (Control) itr.next();
 //            if (!control.isDisposed()) {
 //                control.removeListener(SWT.Activate, controlListener);
 //                control.removeListener(SWT.Deactivate, controlListener);
 //            }
 //        }
         //controlToEditor.clear();
 
 		if (activeEditor != null) {
 			Control control = activeEditor.getControl();
 			if (!control.isDisposed()) {
 				control.removeListener(SWT.Activate, controlListener);
 				control.removeListener(SWT.Deactivate, controlListener);
 			}
 			activeEditor.removePropertyChangeListener(cellListener);
 		}
 		activeEditor = null;
     }
 
     /**
      * Removes a <code>CellEditor</code> from the handler
      * so that the Cut, Copy, Paste, Delete, Select All, Find
      * Undo, and Redo actions are no longer redirected to it.
      *
      * @param editor the <code>CellEditor</code>
      */
 //    public void removeCellEditor(CellEditor editor) {
 //        if (editor == null) {
 //			return;
 //		}
 //
 //        if (activeEditor == editor) {
 //            activeEditor.removePropertyChangeListener(cellListener);
 //            activeEditor = null;
 //        }
 //
 //        Control control = editor.getControl();
 //        if (control != null) {
 //            controlToEditor.remove(control);
 //            if (!control.isDisposed()) {
 //                control.removeListener(SWT.Activate, controlListener);
 //                control.removeListener(SWT.Deactivate, controlListener);
 //            }
 //        }
 //    }
 
     /**
      * Sets the default <code>IAction</code> handler for the Copy
      * action. This <code>IAction</code> is run only if no active
      * cell editor control.
      *
      * @param action the <code>IAction</code> to run for the
      *    Copy action, or <code>null</code> if not interested.
      */
     public void setCopyAction(IAction action) {
         if (copyAction == action) {
 			return;
 		}
 
         if (copyAction != null) {
 			copyAction.removePropertyChangeListener(copyActionListener);
 		}
 
         copyAction = action;
 
         if (copyAction != null) {
 			copyAction.addPropertyChangeListener(copyActionListener);
 		}
 
         cellCopyAction.updateEnabledState();
     }
 
     /**
      * Sets the default <code>IAction</code> handler for the Cut
      * action. This <code>IAction</code> is run only if no active
      * cell editor control.
      *
      * @param action the <code>IAction</code> to run for the
      *    Cut action, or <code>null</code> if not interested.
      */
     public void setCutAction(IAction action) {
         if (cutAction == action) {
 			return;
 		}
 
         if (cutAction != null) {
 			cutAction.removePropertyChangeListener(cutActionListener);
 		}
 
         cutAction = action;
 
         if (cutAction != null) {
 			cutAction.addPropertyChangeListener(cutActionListener);
 		}
 
         cellCutAction.updateEnabledState();
     }
 
     /**
      * Sets the default <code>IAction</code> handler for the Delete
      * action. This <code>IAction</code> is run only if no active
      * cell editor control.
      *
      * @param action the <code>IAction</code> to run for the
      *    Delete action, or <code>null</code> if not interested.
      */
     public void setDeleteAction(IAction action) {
         if (deleteAction == action) {
 			return;
 		}
 
         if (deleteAction != null) {
 			deleteAction.removePropertyChangeListener(deleteActionListener);
 		}
 
         deleteAction = action;
 
         if (deleteAction != null) {
 			deleteAction.addPropertyChangeListener(deleteActionListener);
 		}
 
         cellDeleteAction.updateEnabledState();
     }
 
     /**
      * Sets the default <code>IAction</code> handler for the Find
      * action. This <code>IAction</code> is run only if no active
      * cell editor control.
      *
      * @param action the <code>IAction</code> to run for the
      *    Find action, or <code>null</code> if not interested.
      */
     public void setFindAction(IAction action) {
         if (findAction == action) {
 			return;
 		}
 
         if (findAction != null) {
 			findAction.removePropertyChangeListener(findActionListener);
 		}
 
         findAction = action;
 
         if (findAction != null) {
 			findAction.addPropertyChangeListener(findActionListener);
 		}
 
         cellFindAction.updateEnabledState();
     }
 
     /**
      * Sets the default <code>IAction</code> handler for the Paste
      * action. This <code>IAction</code> is run only if no active
      * cell editor control.
      *
      * @param action the <code>IAction</code> to run for the
      *    Paste action, or <code>null</code> if not interested.
      */
     public void setPasteAction(IAction action) {
         if (pasteAction == action) {
 			return;
 		}
 
         if (pasteAction != null) {
 			pasteAction.removePropertyChangeListener(pasteActionListener);
 		}
 
         pasteAction = action;
 
         if (pasteAction != null) {
 			pasteAction.addPropertyChangeListener(pasteActionListener);
 		}
 
         cellPasteAction.updateEnabledState();
     }
 
     /**
      * Sets the default <code>IAction</code> handler for the Redo
      * action. This <code>IAction</code> is run only if no active
      * cell editor control.
      *
      * @param action the <code>IAction</code> to run for the
      *    Redo action, or <code>null</code> if not interested.
      */
     public void setRedoAction(IAction action) {
         if (redoAction == action) {
 			return;
 		}
 
         if (redoAction != null) {
 			redoAction.removePropertyChangeListener(redoActionListener);
 		}
 
         redoAction = action;
 
         if (redoAction != null) {
 			redoAction.addPropertyChangeListener(redoActionListener);
 		}
 
         cellRedoAction.updateEnabledState();
     }
 
     /**
      * Sets the default <code>IAction</code> handler for the Select All
      * action. This <code>IAction</code> is run only if no active
      * cell editor control.
      *
      * @param action the <code>IAction</code> to run for the
      *    Select All action, or <code>null</code> if not interested.
      */
     public void setSelectAllAction(IAction action) {
         if (selectAllAction == action) {
 			return;
 		}
 
         if (selectAllAction != null) {
 			selectAllAction
                     .removePropertyChangeListener(selectAllActionListener);
 		}
 
         selectAllAction = action;
 
         if (selectAllAction != null) {
 			selectAllAction.addPropertyChangeListener(selectAllActionListener);
 		}
 
         cellSelectAllAction.updateEnabledState();
     }
 
     /**
      * Sets the default <code>IAction</code> handler for the Undo
      * action. This <code>IAction</code> is run only if no active
      * cell editor control.
      *
      * @param action the <code>IAction</code> to run for the
      *    Undo action, or <code>null</code> if not interested.
      */
     public void setUndoAction(IAction action) {
         if (undoAction == action) {
 			return;
 		}
 
         if (undoAction != null) {
 			undoAction.removePropertyChangeListener(undoActionListener);
 		}
 
         undoAction = action;
 
         if (undoAction != null) {
 			undoAction.addPropertyChangeListener(undoActionListener);
 		}
 
         cellUndoAction.updateEnabledState();
     }
 
     /**
      * Updates the enable state of the Cut, Copy,
      * Paste, Delete, Select All, Find, Undo, and
      * Redo action handlers
      */
     private void updateActionsEnableState() {
         cellCutAction.updateEnabledState();
         cellCopyAction.updateEnabledState();
         cellPasteAction.updateEnabledState();
         cellDeleteAction.updateEnabledState();
         cellSelectAllAction.updateEnabledState();
         cellFindAction.updateEnabledState();
         cellUndoAction.updateEnabledState();
         cellRedoAction.updateEnabledState();
     }
     
 	public void setActiveCellEditor(CellEditor editor) {
 		// Remove old listener
		if (this.activeEditor != null && this.activeEditor.getControl() != null
				&& !this.activeEditor.getControl().isDisposed()) {
 			this.activeEditor.removePropertyChangeListener(cellListener);
		}
		this.activeEditor = editor;
		Control control = editor.getControl();
		if (control != null && !control.isDisposed()) {
			control.addListener(SWT.Activate, controlListener);
			control.addListener(SWT.Deactivate, controlListener);
			this.activeEditor.addPropertyChangeListener(cellListener);
 			updateActionsEnableState();
 		}
 	}
     
 }
