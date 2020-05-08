 /*******************************************************************************
  * Copyright (c) 2009, 2012 SpringSource, a divison of VMware, Inc.
  * Copyright (c) 2000, 2008 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.virgo.ide.runtime.internal.ui.actions;
 
 import java.util.Iterator;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.ui.IEditorDescriptor;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.actions.SelectionListenerAction;
 import org.eclipse.ui.internal.ide.DialogUtil;
 import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
 import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.ui.texteditor.ITextEditor;
 import org.eclipse.virgo.ide.runtime.internal.ui.projects.ProjectFileReference;
import org.eclipse.virgo.ide.runtime.internal.ui.providers.ServerFile;
 import org.eclipse.virgo.ide.runtime.internal.ui.providers.ServerFileSelection;
 
 /**
  * Standard action for opening an editor on the currently selected file resource(s).
  * <p>
  * Note that there is a different action for opening closed projects: <code>OpenResourceAction</code>.
  * </p>
  * <p>
  * This class may be instantiated; it is not intended to be subclassed.
  * </p>
  * 
  * @author Others
  * @author Miles Parker
  * @noextend This class is not intended to be subclassed by clients.
  */
 public class OpenServerProjectFileAction extends SelectionListenerAction {
 
 	/**
 	 * The id of this action.
 	 */
 	public static final String ID = PlatformUI.PLUGIN_ID + ".OpenFileAction";//$NON-NLS-1$
 
 	/**
 	 * The editor to open.
 	 */
 	private final IEditorDescriptor editorDescriptor;
 
 	private final IWorkbenchPage workbenchPage2;
 
 	/**
 	 * Creates a new action that will open editors on the then-selected file resources. Equivalent to
 	 * <code>OpenFileAction(page,null)</code>.
 	 * 
 	 * @param page
 	 *            the workbench page in which to open the editor
 	 */
 	public OpenServerProjectFileAction(IWorkbenchPage page) {
 		this(page, null);
 	}
 
 	/**
 	 * Creates a new action that will open instances of the specified editor on the then-selected file resources.
 	 * 
 	 * @param page
 	 *            the workbench page in which to open the editor
 	 * @param descriptor
 	 *            the editor descriptor, or <code>null</code> if unspecified
 	 */
 	public OpenServerProjectFileAction(IWorkbenchPage page, IEditorDescriptor descriptor) {
 		super("Open Linked File");
 		this.workbenchPage2 = page;
 		setText(descriptor == null ? IDEWorkbenchMessages.OpenFileAction_text : descriptor.getLabel());
 		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IIDEHelpContextIds.OPEN_FILE_ACTION);
 		setToolTipText(IDEWorkbenchMessages.OpenFileAction_toolTip);
 		setId(ID);
 		this.editorDescriptor = descriptor;
 	}
 
 	/**
 	 * @see org.eclipse.ui.actions.OpenSystemEditorAction#updateSelection(org.eclipse.jface.viewers.IStructuredSelection)
 	 */
 	@Override
 	public boolean updateSelection(IStructuredSelection selection) {
		Object element = ((StructuredSelection) selection).getFirstElement();
 		return selection instanceof StructuredSelection
				&& (element instanceof ProjectFileReference || element instanceof IFile || element instanceof ServerFile);
 	}
 
 	/**
 	 * @see org.eclipse.ui.actions.OpenSystemEditorAction#run()
 	 */
 	@Override
 	public void run() {
 		Iterator iterator = getStructuredSelection().iterator();
 		while (iterator.hasNext()) {
 			Object next = iterator.next();
 			if (next instanceof ProjectFileReference) {
 				openFile(((ProjectFileReference) next).getWorkspaceFile());
 			} else if (next instanceof IFile) {
 				openFile((IFile) next);
 			} else if (next instanceof ServerFileSelection) {
 				openFile((ServerFileSelection) next);
			} else if (next instanceof ServerFile) {
				openFile(((ServerFile) next).getFile());
 			}
 		}
 	}
 
 	public void openFile(ServerFileSelection selection) {
 		IEditorPart openEditor = openFile(selection.getFile());
 		if (openEditor instanceof ITextEditor) {
 			((ITextEditor) openEditor).selectAndReveal(selection.getOffset(), selection.getLength());
 		}
 	}
 
 	public IEditorPart openFile(IFile file) {
 		try {
 			IEditorDescriptor defaultEditor = workbenchPage2.getWorkbenchWindow()
 					.getWorkbench()
 					.getEditorRegistry()
 					.getDefaultEditor(file.getName());
 			if (defaultEditor == null) {
 				defaultEditor = workbenchPage2.getWorkbenchWindow()
 						.getWorkbench()
 						.getEditorRegistry()
 						.getDefaultEditor("fake.txt");
 			}
 			if (defaultEditor != null) {
 				IEditorPart openEditor = workbenchPage2.openEditor(new FileEditorInput(file), defaultEditor.getId());
 				return openEditor;
 			}
 		} catch (PartInitException e) {
 			DialogUtil.openError(workbenchPage2.getWorkbenchWindow().getShell(),
 					IDEWorkbenchMessages.OpenSystemEditorAction_dialogTitle, e.getMessage(), e);
 		}
 		return null;
 	}
 }
