 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ui.actions;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IStorage;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IModelStatusConstants;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.ISourceReference;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.ScriptModelUtil;
 import org.eclipse.dltk.internal.corext.util.Messages;
 import org.eclipse.dltk.internal.ui.actions.ActionMessages;
 import org.eclipse.dltk.internal.ui.actions.ActionUtil;
 import org.eclipse.dltk.internal.ui.actions.OpenActionUtil;
 import org.eclipse.dltk.internal.ui.actions.SelectionConverter;
 import org.eclipse.dltk.internal.ui.editor.EditorUtility;
 import org.eclipse.dltk.internal.ui.editor.ScriptEditor;
 import org.eclipse.dltk.ui.DLTKUIPlugin;
 import org.eclipse.dltk.ui.util.ExceptionHandler;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.text.ITextSelection;
 import org.eclipse.jface.util.OpenStrategy;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.ui.IWorkbenchSite;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.texteditor.IEditorStatusLine;
 
 /**
  * This action opens a Script editor on a Script element or file.
  * <p>
  * The action is applicable to selections containing elements of type
  * <code>ISourceModule</code>, <code>IMember</code> or <code>IFile</code>.
  * 
  * <p>
  * This class may be instantiated; it is not intended to be subclassed.
  * </p>
  * 
  * 
  */
 public class OpenAction extends SelectionDispatchAction {
 
 	private ScriptEditor fEditor;
 
 	/**
 	 * Creates a new <code>OpenAction</code>. The action requires that the
 	 * selection provided by the site's selection provider is of type <code>
 	 * org.eclipse.jface.viewers.IStructuredSelection</code>.
 	 * 
 	 * @param site
 	 *            the site providing context information for this action
 	 */
 	public OpenAction(IWorkbenchSite site) {
 		super(site);
 		setText(ActionMessages.OpenAction_label);
 		setToolTipText(ActionMessages.OpenAction_tooltip);
 		setDescription(ActionMessages.OpenAction_description);
 		if (DLTKCore.DEBUG) {
 			System.err.println("Add help support here...");
 		}
 
 		// PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
 		// IScriptHelpContextIds.OPEN_ACTION);
 	}
 
 	/**
 	 * Note: This constructor is for internal use only. Clients should not call
 	 * this constructor.
 	 * 
 	 * @param editor
 	 *            the Script editor
 	 */
 	public OpenAction(ScriptEditor editor) {
 		this(editor.getEditorSite());
 		fEditor = editor;
 		setText(ActionMessages.OpenAction_declaration_label);
 		setEnabled(EditorUtility.getEditorInputModelElement(fEditor, false) != null);
 	}
 
 	/*
 	 * (non-Javadoc) Method declared on SelectionDispatchAction.
 	 */
 	public void selectionChanged(ITextSelection selection) {
 	}
 
 	/*
 	 * (non-Javadoc) Method declared on SelectionDispatchAction.
 	 */
 	public void selectionChanged(IStructuredSelection selection) {
 		setEnabled(checkEnabled(selection));
 	}
 
 	private boolean checkEnabled(IStructuredSelection selection) {
 		if (selection.isEmpty())
 			return false;
 		for (Iterator iter = selection.iterator(); iter.hasNext();) {
 			Object element = iter.next();
 			if (element instanceof ISourceReference)
 				continue;
 			if (element instanceof IFile)
 				continue;
 			if (element instanceof IStorage)
 				continue;
 			return false;
 		}
 		return true;
 	}
 
 	/*
 	 * (non-Javadoc) Method declared on SelectionDispatchAction.
 	 */
 	public void run(ITextSelection selection) {
 		if (!isProcessable())
 			return;
 		try {
 			IModelElement[] elements = SelectionConverter.codeResolveForked(
 					fEditor, false);
 			elements = filterElements(elements);
 			if (elements == null || elements.length == 0) {
 				IEditorStatusLine statusLine = (IEditorStatusLine) fEditor
 						.getAdapter(IEditorStatusLine.class);
 				if (statusLine != null)
 					statusLine
 							.setMessage(
 									true,
 									ActionMessages.OpenAction_error_messageBadSelection,
 									null);
 				getShell().getDisplay().beep();
 				return;
 			}
 			IModelElement element = elements[0];
 			if (elements.length > 1) {
 				element = OpenActionUtil.selectModelElement(elements,
 						getShell(), getDialogTitle(),
 						ActionMessages.OpenAction_select_element);
 				if (element == null)
 					return;
 			}
 
 			int type = element.getElementType();
 			if (type == IModelElement.SCRIPT_PROJECT
 					|| type == IModelElement.PROJECT_FRAGMENT
 					|| type == IModelElement.SCRIPT_FOLDER)
 				element = EditorUtility.getEditorInputModelElement(fEditor,
 						false);
 			run(new Object[] { element });
 		} catch (InvocationTargetException e) {
 			showError(e);
 		} catch (InterruptedException e) {
 			// ignore
 		}
 	}
 
 	private IModelElement[] filterElements(IModelElement[] elements) {
 		Map uniqueElements = new HashMap();
 		for (int i = 0; i < elements.length; i++) {
 			IModelElement element = elements[i];
 			IModelElement module = element
 					.getAncestor(IModelElement.SOURCE_MODULE);
 			if (module != null) {
 				if (!uniqueElements.containsKey(module)) {
 					uniqueElements.put(module, element);
 				}
 			}
 		}
 		return (IModelElement[]) uniqueElements.values().toArray(
 				new IModelElement[uniqueElements.size()]);
 	}
 
 	private boolean isProcessable() {
 		if (fEditor != null) {
 			IModelElement je = EditorUtility.getEditorInputModelElement(
 					fEditor, false);
 			if (je instanceof ISourceModule
 					&& !ScriptModelUtil.isPrimary((ISourceModule) je))
 				return true; // can process non-primary working copies
 		}
 		return ActionUtil.isProcessable(getShell(), fEditor);
 	}
 
 	/*
 	 * (non-Javadoc) Method declared on SelectionDispatchAction.
 	 */
 	public void run(IStructuredSelection selection) {
 		if (!checkEnabled(selection))
 			return;
 		run(selection.toArray());
 	}
 
 	/**
 	 * Note: this method is for internal use only. Clients should not call this
 	 * method.
 	 * 
 	 * @param elements
 	 *            the elements to process
 	 */
 	public void run(Object[] elements) {
 		if (elements == null)
 			return;
 		for (int i = 0; i < elements.length; i++) {
 			Object element = elements[i];
 			try {
 				element = getElementToOpen(element);
 				boolean activateOnOpen = fEditor != null ? true : OpenStrategy
 						.activateOnOpen();
 				OpenActionUtil.open(element, activateOnOpen);
 			} catch (ModelException e) {
 				DLTKUIPlugin.log(new Status(IStatus.ERROR,
 						DLTKUIPlugin.PLUGIN_ID,
 						IModelStatusConstants.INTERNAL_ERROR,
 						ActionMessages.OpenAction_error_message, e));
 
 				ErrorDialog.openError(getShell(), getDialogTitle(),
 						ActionMessages.OpenAction_error_messageProblems, e
 								.getStatus());
 
 			} catch (PartInitException x) {
 
 				String name = null;
 
 				if (element instanceof IModelElement) {
 					name = ((IModelElement) element).getElementName();
 				} else if (element instanceof IStorage) {
 					name = ((IStorage) element).getName();
 				} else if (element instanceof IResource) {
 					name = ((IResource) element).getName();
 				}
 
 				if (name != null) {
 					MessageDialog
 							.openError(
 									getShell(),
 									ActionMessages.OpenAction_error_messageProblems,
 									Messages
 											.format(
 													ActionMessages.OpenAction_error_messageArgs,
 													new String[] { name,
 															x.getMessage() }));
 				}
 			}
 		}
 	}
 
 	/**
 	 * Note: this method is for internal use only. Clients should not call this
 	 * method.
 	 * 
 	 * @param object
 	 *            the element to open
 	 * @return the real element to open
 	 * @throws ModelException
 	 *             if an error occurs while accessing the Script model
 	 */
 	public Object getElementToOpen(Object object) throws ModelException {
 		return object;
 	}
 
 	private String getDialogTitle() {
 		return ActionMessages.OpenAction_error_title;
 	}
 
 	private void showError(InvocationTargetException e) {
 		ExceptionHandler.handle(e, getShell(), getDialogTitle(),
 				ActionMessages.OpenAction_error_message);
 	}
 }
