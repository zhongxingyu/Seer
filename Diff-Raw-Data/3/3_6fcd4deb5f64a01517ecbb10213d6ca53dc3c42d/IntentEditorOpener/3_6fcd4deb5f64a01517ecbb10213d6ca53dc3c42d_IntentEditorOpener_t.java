 /*******************************************************************************
  * Copyright (c) 2010, 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.mylyn.docs.intent.client.ui.utils;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.mylyn.docs.intent.client.ui.IntentEditorActivator;
 import org.eclipse.mylyn.docs.intent.client.ui.editor.IntentEditor;
 import org.eclipse.mylyn.docs.intent.client.ui.editor.IntentEditorInput;
 import org.eclipse.mylyn.docs.intent.client.ui.logger.IntentUiLogger;
 import org.eclipse.mylyn.docs.intent.collab.common.location.IntentLocations;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.ReadOnlyException;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryAdapter;
 import org.eclipse.mylyn.docs.intent.collab.repository.Repository;
 import org.eclipse.mylyn.docs.intent.core.document.IntentDocument;
 import org.eclipse.mylyn.docs.intent.core.document.IntentGenericElement;
 import org.eclipse.mylyn.docs.intent.core.query.IntentHelper;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorReference;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 
 /**
  * Class used for opening ReStrucutred Models editor.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
  */
 public final class IntentEditorOpener {
 
 	/**
 	 * EditorUtil constructor.
 	 */
 	private IntentEditorOpener() {
 
 	}
 
 	/**
 	 * Opens an editor on the Intent document contained in the given repository.
 	 * 
 	 * @param repository
 	 *            The repository to use for this editor
 	 * @param readOnlyMode
 	 *            indicates if the editor should be opened in readOnly mode.
 	 * @param elementToSelectRangeWith
 	 *            the element on which the created editor should select its range (can be null).
 	 * @param forceNewEditor
 	 *            if true, will open in a new editor anyway. If false, will open in a new editor or select
 	 *            inside of an already opened editor
 	 */
 	public static void openIntentEditor(final Repository repository, boolean readOnlyMode) {
 		try {
 			final RepositoryAdapter repositoryAdapter = repository.createRepositoryAdapter();
 			Resource resource = repositoryAdapter.getOrCreateResource(IntentLocations.INTENT_INDEX);
 			if (!resource.getContents().isEmpty()
 					&& resource.getContents().iterator().next() instanceof IntentDocument) {
 				EObject elementToOpen = resource.getContents().iterator().next();
 				openIntentEditor(repositoryAdapter, elementToOpen, false, elementToOpen, false);
 			}
 		} catch (PartInitException e) {
 			IntentUiLogger.logError(e);
 		} catch (ReadOnlyException e) {
 			IntentUiLogger.logError(e);
 		}
 	}
 
 	/**
 	 * Opens an editor on the element with the given identifier.
 	 * 
 	 * @param repository
 	 *            The repository to use for this editor
 	 * @param elementToOpen
 	 *            the element to open.
 	 * @param readOnlyMode
 	 *            indicates if the editor should be opened in readOnly mode.
 	 * @param elementToSelectRangeWith
 	 *            the element on which the created editor should select its range (can be null).
 	 * @param forceNewEditor
 	 *            if true, will open in a new editor anyway. If false, will open in a new editor or select
 	 *            inside of an already opened editor
 	 */
 	public static void openIntentEditor(final Repository repository, final EObject elementToOpen,
 			boolean readOnlyMode, EObject elementToSelectRangeWith, boolean forceNewEditor) {
 		try {
 			final RepositoryAdapter repositoryAdapter = repository.createRepositoryAdapter();
 			openIntentEditor(repositoryAdapter, elementToOpen, false, elementToSelectRangeWith,
 					forceNewEditor);
 		} catch (PartInitException e) {
 			IntentUiLogger.logError(e);
 		}
 	}
 
 	/**
 	 * Opens an editor on the element with the given identifier.
 	 * 
 	 * @param repositoryAdapter
 	 *            the repository adapter
 	 * @param elementToOpen
 	 *            the element to open.
 	 * @param readOnlyMode
 	 *            indicates if the editor should be opened in readOnly mode.
 	 * @param elementToSelectRangeWith
 	 *            the element on which the created editor should select its range (can be null).
 	 * @param forceNewEditor
 	 *            if true, will open in a new editor anyway. If false, will open in a new editor or select
 	 *            inside of an already opened editor
 	 * @return the opened editor
 	 * @throws PartInitException
 	 *             if the editor cannot be opened.
 	 */
 	private static IntentEditor openIntentEditor(RepositoryAdapter repositoryAdapter, EObject elementToOpen,
 			boolean readOnlyMode, EObject elementToSelectRangeWith, boolean forceNewEditor)
 			throws PartInitException {
 		IntentEditor openedEditor = null;
 		IStatus status = null;
 		// We get the element on which open this editor
 		if (readOnlyMode) {
 			repositoryAdapter.openReadOnlyContext();
 		} else {
 			repositoryAdapter.openSaveContext();
 		}
 
 		boolean foundInAlreadyExistingEditor = false;
 		if (!forceNewEditor) {
 			// Step 2 : if an editor containing this element is already opened
 			IntentEditor editor = getAlreadyOpenedEditor(elementToOpen);
 			if (editor != null) {
 				editor.getEditorSite().getPage().activate(editor);
 				openedEditor = editor;
				foundInAlreadyExistingEditor = editor
						.selectRange((IntentGenericElement)elementToSelectRangeWith);
 			}
 		}
 
 		if (openedEditor == null || !foundInAlreadyExistingEditor) {
 
 			// Step 3 : we open a new editor.
 			IWorkbenchPage page = null;
 			try {
 				page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
 				openedEditor = IntentEditorOpener.openEditor(repositoryAdapter, page, elementToOpen);
 				EObject container = elementToSelectRangeWith;
 				while (container != null && !(container instanceof IntentGenericElement)) {
 					container = container.eContainer();
 				}
 				if (container instanceof IntentGenericElement) {
 					openedEditor.selectRange((IntentGenericElement)container);
 				} else {
 					if (elementToOpen instanceof IntentGenericElement) {
 						openedEditor.selectRange((IntentGenericElement)elementToOpen);
 					}
 				}
 
 			} catch (NullPointerException e) {
 				status = new Status(IStatus.ERROR, IntentEditorActivator.PLUGIN_ID,
 						"An unexpected error has occured");
 				throw new PartInitException(status);
 			}
 		}
 
 		return openedEditor;
 	}
 
 	/**
 	 * If an editor is already opened on the given element, returns it ; returns null otherwise.
 	 * 
 	 * @param elementToOpen
 	 *            the element to search in editors
 	 * @return an IntentEditor already opened on the given element, null otherwise.
 	 */
 	public static IntentEditor getAlreadyOpenedEditor(EObject elementToOpen) {
 		IntentEditor alreadyOpenedEditor = null;
 		IWorkbenchPage activePage = null;
 		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
 		if (activeWorkbenchWindow != null) {
 			activePage = activeWorkbenchWindow.getActivePage();
 		}
 
 		if (activePage != null) {
 
 			// While no editor containing the given element has been found
 			IEditorReference[] editorReferences = activePage.getEditorReferences();
 			int editorCount = 0;
 			while ((editorCount < editorReferences.length) && alreadyOpenedEditor == null) {
 				IEditorReference editorReference = editorReferences[editorCount];
 				IEditorPart editor = editorReference.getEditor(false);
 
 				if (editor instanceof IntentEditor) {
 					if (((IntentEditor)editor).containsElement((IntentGenericElement)elementToOpen)) {
 						alreadyOpenedEditor = (IntentEditor)editor;
 						activePage.activate(alreadyOpenedEditor);
 					}
 				}
 				editorCount++;
 			}
 		}
 
 		return alreadyOpenedEditor;
 	}
 
 	/**
 	 * Opens an editor on the given IntentModel element.
 	 * 
 	 * @param repositoryAdapter
 	 *            the repository adapter to use for this document
 	 * @param page
 	 *            the page in which the editor should be opened
 	 * @param intentElementToOpen
 	 *            the Intent element to open
 	 * @return the opened editor
 	 * @throws PartInitException
 	 *             if the editor cannot be opened.
 	 */
 	private static IntentEditor openEditor(RepositoryAdapter repositoryAdapter, IWorkbenchPage page,
 			Object intentElementToOpen) throws PartInitException {
 
 		// If we can't open a IntentEditor on the given element, we try to get its container until null or an
 		// editable intent element is found
 		boolean canBeOpenedByIntentEditor = IntentHelper.canBeOpenedByIntentEditor(intentElementToOpen);
 		EObject elementToOpen = null;
 		if (intentElementToOpen instanceof EObject) {
 			elementToOpen = (EObject)intentElementToOpen;
 		}
 		while (!canBeOpenedByIntentEditor && elementToOpen != null && !(elementToOpen instanceof Resource)) {
 			elementToOpen = elementToOpen.eContainer();
 			canBeOpenedByIntentEditor = IntentHelper.canBeOpenedByIntentEditor(elementToOpen);
 		}
 
 		if (canBeOpenedByIntentEditor) {
 			IntentEditorInput input = new IntentEditorInput(elementToOpen, repositoryAdapter);
 			IEditorPart part = page.openEditor(input, IntentEditorActivator.EDITOR_ID);
 			if (part instanceof IntentEditor) {
 				return (IntentEditor)part;
 			} else {
 				IStatus status = new Status(IStatus.ERROR, IntentEditorActivator.PLUGIN_ID,
 						"cannot open the editor");
 				throw new PartInitException(status);
 			}
 		} else {
 			IntentUiLogger.logError("this element is not a correct Intent element", new PartInitException(
 					"Invalid element : must be a Intent Element"));
 			IStatus status = new Status(IStatus.ERROR, IntentEditorActivator.PLUGIN_ID,
 					"this element is not a correct Intent element");
 
 			throw new PartInitException(status);
 		}
 
 	}
 }
