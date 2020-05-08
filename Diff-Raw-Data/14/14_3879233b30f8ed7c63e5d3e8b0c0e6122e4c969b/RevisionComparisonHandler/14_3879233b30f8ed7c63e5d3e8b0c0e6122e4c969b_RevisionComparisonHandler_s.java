 /*******************************************************************************
  * Copyright (c) 2009 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.ui.internal;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.eclipse.compare.IStreamContentAccessor;
 import org.eclipse.compare.ITypedElement;
 import org.eclipse.compare.ResourceNode;
 import org.eclipse.compare.structuremergeviewer.ICompareInput;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFileState;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IStorage;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.compare.ui.team.AbstractTeamHandler;
 import org.eclipse.emf.compare.util.EclipseModelUtils;
 import org.eclipse.emf.compare.util.ModelUtils;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.resource.impl.URIConverterImpl;
 import org.eclipse.team.core.RepositoryProvider;
 import org.eclipse.team.core.history.IFileHistory;
 import org.eclipse.team.core.history.IFileHistoryProvider;
 import org.eclipse.team.core.history.IFileRevision;
 import org.eclipse.team.internal.ui.history.FileRevisionTypedElement;
 
 /**
  * Allows EMF Compare to properly handle history comparison when resources have links towards others.
  * 
  * @author <a href="mailto:laurent.goubet@obeo.fr">Laurent Goubet</a>
  * @since 0.9
  */
 public class RevisionComparisonHandler extends AbstractTeamHandler {
 	/** This will be set to true if the left resource cannot be loaded. */
 	private boolean leftIsRemote;
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.ui.team.AbstractTeamHandler#isLeftRemote()
 	 */
 	@Override
 	public boolean isLeftRemote() {
 		return leftIsRemote;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.ui.team.AbstractTeamHandler#loadResources(org.eclipse.compare.structuremergeviewer.ICompareInput)
 	 */
 	@Override
 	public boolean loadResources(ICompareInput input) throws IOException, CoreException {
 		final ITypedElement left = input.getLeft();
 		final ITypedElement right = input.getRight();
 		final ITypedElement ancestor = input.getAncestor();
 
 		if (right instanceof FileRevisionTypedElement) {
 			final IFileRevision rightRevision = ((FileRevisionTypedElement)right).getFileRevision();
 
 			final ResourceSet leftResourceSet = new ResourceSetImpl();
 			final ResourceSet rightResourceSet = new ResourceSetImpl();
 
			if (((ResourceNode)left).getResource().isAccessible()) {
 				leftResource = EclipseModelUtils.load(((ResourceNode)left).getResource().getFullPath(),
 						leftResourceSet).eResource();
 			} else {
 				leftResource = ModelUtils.createResource(URI.createPlatformResourceURI(((ResourceNode)left)
 						.getResource().getFullPath().toOSString(), true));
 				// resource has been deleted. We set it as "remote" to disable merge facilities
 				leftIsRemote = true;
 			}
 
 			try {
 				rightResource = ModelUtils.load(((IStreamContentAccessor)right).getContents(),
 						right.getName(), rightResourceSet).eResource();
 				rightResourceSet.setURIConverter(new RevisionedURIConverter(rightRevision));
 			} catch (final IOException e) {
 				// We couldn't load the remote resource. Considers it has been added to the repository
 				rightResource = ModelUtils.createResource(URI.createURI(right.getName()));
 				// Set the left as remote to disable merge facilities
 				leftIsRemote = true;
 			}
 
 			if (ancestor != null) {
 				final IFileRevision ancestorRevision = ((FileRevisionTypedElement)ancestor).getFileRevision();
 				final ResourceSet ancestorResourceSet = new ResourceSetImpl();
 				try {
 					ancestorResource = ModelUtils.load(((IStreamContentAccessor)ancestor).getContents(),
 							ancestor.getName(), ancestorResourceSet).eResource();
 					ancestorResourceSet.setURIConverter(new RevisionedURIConverter(ancestorRevision));
 				} catch (final IOException e) {
 					// Couldn't load ancestor resource, create an empty one
 					ancestorResource = ModelUtils.createResource(URI.createURI(ancestor.getName()));
 				}
 			}
 			return true;
 		}
 		return false;
 	}
 
 	/* (non-javadoc) most of the behavior here has been copied from EMF 2.4 "URIHandlerImpl". */
 	/**
 	 * This implementation of an URIConverter allows us to properly resolve cross-model links towards the
 	 * actual revision that should be loaded.
 	 * 
 	 * @author <a href="mailto:laurent.goubet@obeo.fr">Laurent Goubet</a>
 	 * @since 0.9
 	 */
 	private class RevisionedURIConverter extends URIConverterImpl {
 		/** The revision of the base model. This revision's timestamp will be used to resolve proxies. */
 		private final IFileRevision baseRevision;
 
 		/**
 		 * This default constructor will add our own URI Handler to the top of the handlers list.
 		 * 
 		 * @param revision
 		 *            Revision of the base model.
 		 */
 		public RevisionedURIConverter(IFileRevision revision) {
 			super();
 			baseRevision = revision;
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.ecore.resource.URIConverter#createInputStream(org.eclipse.emf.common.util.URI)
 		 */
 		@Override
 		public InputStream createInputStream(URI uri) {
 			try {
 				// We'll have to change the EMF URI to find the IFile it points to
 				URI deresolvedURI = uri;
 
 				final IStorage storage = baseRevision.getStorage(null);
 				if (uri.isRelative()) {
 					// Current revision, yet the proxy could point to a file that has changed since.
 					if (storage instanceof IFile) {
 						final IFile file = (IFile)storage;
 						deresolvedURI = uri.resolve(URI.createURI(file.getLocationURI().toString()));
 					} else {
 						final IResource stateFile = workspaceRoot.findMember(storage.getFullPath());
 						deresolvedURI = uri.resolve(URI.createURI(stateFile.getLocationURI().toString()));
 					}
 				}
 				deresolvedURI = deresolvedURI.deresolve(URI.createURI(workspaceRoot.getLocationURI()
 						.toString() + '/'));
 
 				final IResource targetFile = workspaceRoot.findMember(new Path(deresolvedURI.trimFragment()
 						.toString()));
 
 				if (targetFile != null)
 					return openRevisionStream(targetFile);
 			} catch (final CoreException e) {
 				// FIXME log this
 			}
 			return null;
 		}
 
 		/**
 		 * This will open an InputStream on the first revision of <code>target</code> which timeStamp is
 		 * inferior to that of {@link #baseRevision}.
 		 * 
 		 * @param target
 		 *            The resource we seek a revision of.
 		 * @return InputStream on the first revision of <code>target</code> which timeStamp is inferior to
 		 *         that of {@link #baseRevision}.
 		 */
 		private InputStream openRevisionStream(IResource target) {
 			final IProject project = target.getProject();
 			final RepositoryProvider provider = RepositoryProvider.getProvider(project);
 			try {
 				InputStream stream = null;
 				if (provider != null) {
 					final IFileHistoryProvider historyProvider = provider.getFileHistoryProvider();
 					final IFileHistory history = historyProvider.getFileHistoryFor(target,
 							IFileHistoryProvider.NONE, new NullProgressMonitor());
 
 					if (history != null) {
 						IFileRevision soughtRevision = null;
 						for (final IFileRevision revision : history.getFileRevisions()) {
 							if (revision.getTimestamp() <= baseRevision.getTimestamp()) {
 								soughtRevision = revision;
 								break;
 							}
 						}
 						if (soughtRevision != null) {
 							stream = soughtRevision.getStorage(new NullProgressMonitor()).getContents();
 						}
 					}
 				}
 				if (stream == null) {
 					IFileState soughtState = null;
 					// This project is not connected to a repository. Search through local history
 					for (final IFileState state : ((IFile)target).getHistory(new NullProgressMonitor())) {
 						if (state.getModificationTime() <= baseRevision.getTimestamp()) {
 							soughtState = state;
 							break;
 						}
 					}
 					if (soughtState != null) {
 						stream = soughtState.getContents();
 					} else {
 						stream = ((IFile)target).getContents();
 					}
 				}
 				return stream;
 			} catch (final CoreException e) {
 				// FIXME log this
 			}
 			return null;
 		}
 	}
 }
