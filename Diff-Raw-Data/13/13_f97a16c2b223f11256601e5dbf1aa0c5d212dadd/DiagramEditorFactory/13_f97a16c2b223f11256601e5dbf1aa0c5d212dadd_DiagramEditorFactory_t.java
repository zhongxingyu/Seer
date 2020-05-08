 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2005, 2010 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  *    mwenz - Bug 336075 - DiagramEditor accepts URIEditorInput
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.ui.editor;
 
 import org.eclipse.core.commands.operations.DefaultOperationHistory;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.emf.common.ui.URIEditorInput;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.emf.transaction.impl.TransactionalEditingDomainImpl;
 import org.eclipse.emf.workspace.IWorkspaceCommandStack;
 import org.eclipse.emf.workspace.WorkspaceEditingDomainFactory;
 import org.eclipse.graphiti.internal.util.T;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.ui.internal.editor.GFWorkspaceCommandStackImpl;
 import org.eclipse.graphiti.ui.internal.services.GraphitiUiInternal;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorMatchingStrategy;
 import org.eclipse.ui.IEditorReference;
 import org.eclipse.ui.IElementFactory;
 import org.eclipse.ui.IFileEditorInput;
 import org.eclipse.ui.IMemento;
 import org.eclipse.ui.PartInitException;
 
 /**
  * The Class DiagramEditorFactory.
  * 
  * A factory for creating DiagramEditorInternal objects.
  * 
  * @see {@link DiagramEditorInputBase}
  * @see {@link DiagramEditorFactory}
  * @see {@link DiagramEditorInput}
  */
 public class DiagramEditorFactory implements IElementFactory {
 
 	/**
 	 * Creates a new {@link DiagramEditorInput} with a self created
 	 * {@link TransactionalEditingDomain} in case the passed
 	 * {@link IEditorInput} is either a {@link IFileEditorInput} or a
 	 * {@link URIEditorInput}. It returns otherInput, if it is a
 	 * {@link DiagramEditorInput}. The created editor input object will care
 	 * about the disposal of the editing domain.
 	 * 
 	 * @param otherInput
 	 *            an {@link IEditorInput} editor input
 	 * @return a {@link DiagramEditorInput} editor input if the conversion is
 	 *         supported and succeeded, otherwise <code>null</code>.
 	 */
 	public DiagramEditorInput createEditorInput(IEditorInput otherInput) {
 		if (otherInput instanceof DiagramEditorInput) {
 			return (DiagramEditorInput) otherInput;
 		}
 		if (otherInput instanceof IFileEditorInput) {
 			final IFileEditorInput fileInput = (IFileEditorInput) otherInput;
 			final IFile file = fileInput.getFile();
 			final TransactionalEditingDomain domain = createResourceSetAndEditingDomain();
 			URI diagramFileUri = GraphitiUiInternal.getEmfService().getFileURI(file, domain.getResourceSet());
 			if (diagramFileUri != null) {
 				// the file has to contain one base node which has to be a diagram
 				diagramFileUri = GraphitiUiInternal.getEmfService().mapDiagramFileUriToDiagramUri(diagramFileUri);
 				return new DiagramEditorInput(diagramFileUri, domain, null, true);
 			}
 		}
 		if (otherInput instanceof URIEditorInput) {
 			final URIEditorInput uriInput = (URIEditorInput) otherInput;
 			final TransactionalEditingDomain domain = createResourceSetAndEditingDomain();
 			URI diagramFileUri = uriInput.getURI();
 			if (diagramFileUri != null) {
 				// the file has to contain one base node which has to be a diagram
 				diagramFileUri = GraphitiUiInternal.getEmfService().mapDiagramFileUriToDiagramUri(diagramFileUri);
 				return new DiagramEditorInput(diagramFileUri, domain, null, true);
 			}
 		}
 
 		return null;
 	}
 
 	/**
 	 * <b>For internal use only</b>. Checks whether any file editor input
 	 * matches one of the opened editors. Scenario is a user's double-clicking
 	 * on a diagram file in the explorer. If done multiple times on the same
 	 * file, no new editor must be opened.
 	 */
 	public static final class DiagramEditorMatchingStrategy implements IEditorMatchingStrategy {
 		@Override
 		public boolean matches(IEditorReference editorRef, IEditorInput input) {
 			try {
 				if (input instanceof IFileEditorInput) {
 					final IFileEditorInput fileInput = (IFileEditorInput) input;
 					final IFile file = fileInput.getFile();
 
 					// Check whether the given file contains a diagram as its
 					// root element. If yes, compare it with the given editor's
 					// diagram.
 					final IEditorInput editorInput = editorRef.getEditorInput();
 					if (editorInput instanceof DiagramEditorInput) {
 						final DiagramEditorInput diagInput = (DiagramEditorInput) editorInput;
 
 						// We do not compare diagram object but diagram files only.
 						// Reason is that if editorRef points to a not yet
 						// realized editor, its input's diagram is null (not yet
 						// created), thus we can only get its diagram file.
 						final String uriString = diagInput.getUriString();
 						final URI uri = URI.createURI(uriString);
 						if (uri != null) {
 							final ResourceSet rSet = new ResourceSetImpl();
 							final Diagram diagram = GraphitiUiInternal.getEmfService().getDiagramFromFile(file, rSet);
 							if (diagram != null) {
 								final URI diagramUri = EcoreUtil.getURI(diagram);
								// Trim fragments, in some cases for the first
								// root object
								// just a slash is used, in other cases it is /0
								// (if more than one root object is present).
								// since we only allow one diagram per file,
								// this is no restriction. we have:
								// one input a diagramfileinput, the other
								// contains a diagram, and the files are the
								// same.
								URI diagramUriTrimFragment = diagramUri.trimFragment();
								URI uriTrimFragment = uri.trimFragment();
								if (uriTrimFragment.equals(diagramUriTrimFragment)) {
 									return true;
 								}
 							}
 						}
 					}
 				} else if (input instanceof URIEditorInput) {
 					final URIEditorInput uriInput = (URIEditorInput) input;
 					URI uri = uriInput.getURI();
 					uri = GraphitiUiInternal.getEmfService().mapDiagramFileUriToDiagramUri(uri);
 
 					// Check whether the given file contains a diagram as its
 					// root element. If yes, compare it with the given editor's
 					// diagram.
 					final IEditorInput editorInput = editorRef.getEditorInput();
 					if (editorInput instanceof DiagramEditorInput) {
 						final DiagramEditorInput diagInput = (DiagramEditorInput) editorInput;
 
 						// We do not compare diagram object but diagram files only.
 						// Reason is that if editorRef points to a not yet
 						// realized editor, its input's diagram is null (not yet
 						// created), thus we can only get its diagram file.
 						final String uriString = diagInput.getUriString();
 						final URI diagramUri = URI.createURI(uriString);
 						if (diagramUri != null) {
 							if (uri.equals(diagramUri)) {
 								return true;
 							}
 						}
 					}
 				} else if (input instanceof DiagramEditorInput) {
 					// normal case: check for input equality
 					final IEditorInput editorInput = editorRef.getEditorInput();
 					if (input.equals(editorInput)) {
 						return true;
 					}
 				}
 			} catch (final PartInitException e) {
 				T.racer().error(e.getMessage(), e);
 			}
 			return false;
 		}
 	}
 
 	@Override
 	public IAdaptable createElement(IMemento memento) {
 		// get diagram URI
 		final String diagramUriString = memento.getString(DiagramEditorInput.KEY_URI);
 		if (diagramUriString == null) {
 			return null;
 		}
 
 		// get diagram type provider id
 		final String providerID = memento.getString(DiagramEditorInput.KEY_PROVIDER_ID);
 		//TODO		if (providerID == null)
 		//			return null;
 
 		final TransactionalEditingDomain domain = createResourceSetAndEditingDomain();
 		return new DiagramEditorInput(diagramUriString, domain, providerID, true);
 	}
 
 	/**
 	 * Creates a {@link TransactionalEditingDomain} with a {@link ResourceSet}
 	 * resource set and a {@link IWorkspaceCommandStack} command stack.
 	 * 
 	 * @return a {@link TransactionalEditingDomain} editing domain
 	 */
 	public static TransactionalEditingDomain createResourceSetAndEditingDomain() {
 		final ResourceSet resourceSet = new ResourceSetImpl();
 		final IWorkspaceCommandStack workspaceCommandStack = new GFWorkspaceCommandStackImpl(new DefaultOperationHistory());
 
 		final TransactionalEditingDomainImpl editingDomain = new TransactionalEditingDomainImpl(new ComposedAdapterFactory(
 				ComposedAdapterFactory.Descriptor.Registry.INSTANCE), workspaceCommandStack, resourceSet);
 		WorkspaceEditingDomainFactory.INSTANCE.mapResourceSet(editingDomain);
 		return editingDomain;
 	}
 
 }
