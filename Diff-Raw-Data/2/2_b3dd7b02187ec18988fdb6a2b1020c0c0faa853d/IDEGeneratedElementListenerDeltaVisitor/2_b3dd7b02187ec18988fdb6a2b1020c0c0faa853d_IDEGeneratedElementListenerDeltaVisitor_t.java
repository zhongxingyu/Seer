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
 package org.eclipse.mylyn.docs.intent.client.ui.ide.generatedelementlistener;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Set;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.emf.common.util.URI;
 
 /**
  * Visits a resource delta and detects the listened changed or removed resources.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  */
 public class IDEGeneratedElementListenerDeltaVisitor implements IResourceDeltaVisitor {
 	/**
 	 * All the EMF resource of the WorkspaceRepository that have changed.
 	 */
 	protected Collection<URI> changedResources;
 
 	/**
 	 * All the EMF resource of the WorkspaceRepository that have been removed of this repository.
 	 */
 	protected Collection<URI> removedResources;
 
 	/**
 	 * {@link URI}s of the listened elements.
 	 */
 	protected Set<URI> listenedElementsURIs;
 
 	/**
 	 * IDEGeneratedElementListenerDeltaVisitor constructor.
 	 * 
 	 * @param listenedElementsURIs
 	 *            the list of listened element's URIs
 	 */
 	public IDEGeneratedElementListenerDeltaVisitor(Set<URI> listenedElementsURIs) {
 		this.listenedElementsURIs = listenedElementsURIs;
 		changedResources = new ArrayList<URI>();
 		removedResources = new ArrayList<URI>();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
 	 */
 	public boolean visit(IResourceDelta delta) {
 
 		if (delta.getResource().getType() != IResource.FILE) {
 			return true;
 		}
 
 		if (isRelevantModification(delta)) {
 
 			// We first calculate the repository relative path for this resource
 			String uri = delta.getFullPath().toString();
 			URI changedResourceURI = URI.createPlatformResourceURI(uri, false);
 
 			if ((changedResourceURI != null) && (listenedElementsURIs.contains(changedResourceURI))) {
 				switch (delta.getKind()) {
 					case IResourceDelta.REMOVED:
 						removedResources.add(changedResourceURI);
 						break;
 
 					case IResourceDelta.ADDED:
 					case IResourceDelta.CHANGED:
 						changedResources.add(changedResourceURI);
 						break;
 
 					default:
 						break;
 				}
 			}
 
 		}
 
 		return true;
 	}
 
 	/**
 	 * Indicates if the given delta describes a relevant modification.
 	 * 
 	 * @param delta
 	 *            the visited delta
 	 * @return true if the given delta describes a relevant modification false otherwise
 	 */
 	protected boolean isRelevantModification(IResourceDelta delta) {
 		// First of all, we test if this delta describes an deletion, an addition or a modification
 		boolean isRelevantModification = (delta.getKind() == IResourceDelta.REMOVED)
 				|| (delta.getKind() == IResourceDelta.CHANGED) || delta.getKind() == IResourceDelta.ADDED;
 		// The we ensure that this delta isn't a touch or a marker modification
		isRelevantModification = isRelevantModification && (delta.getFlags() != IResourceDelta.MARKERS);
 		return isRelevantModification;
 	}
 
 	/**
 	 * Returns the list of changed Resources's URIs.
 	 * 
 	 * @return the list of changed Resources 's URIs
 	 */
 	public Collection<URI> getChangedResources() {
 		return changedResources;
 	}
 
 	/**
 	 * Returns the list of removed Resources's URIs.
 	 * 
 	 * @return the list of removed Resources 's URIs
 	 */
 	public Collection<URI> getRemovedResources() {
 		return removedResources;
 	}
 }
