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
 package org.eclipse.mylyn.docs.intent.client.ui.ide.builder;
 
 import com.google.common.collect.Sets;
 
 import java.util.Set;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.mylyn.docs.intent.client.ui.ide.launcher.IntentProjectManager;
 
 /**
  * Visitor used by the Intent builder.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
  */
 class IntentBuilderDeltaVisitor implements IResourceDeltaVisitor {
 	/**
 	 * Collection of projects that just have been opened/created.
 	 */
 	private Set<IProject> openedProjects = Sets.newHashSet();
 
 	/**
 	 * Collection of projects that just have been closed/deleted.
 	 */
 	private Set<IProject> closedProjects = Sets.newHashSet();
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
 	 */
 	public boolean visit(IResourceDelta delta) throws CoreException {
 		IResource resource = delta.getResource();
 		switch (delta.getKind()) {
 			case IResourceDelta.ADDED:
 				// If an intent project has been created
 				if (resource instanceof IProject && resource.isAccessible()
 						&& ((IProject)resource).hasNature(IntentNature.NATURE_ID)) {
 					openedProjects.add((IProject)resource);
 				}
 				break;
 			case IResourceDelta.CHANGED:
 				if ((IResourceDelta.OPEN & delta.getFlags()) != 0) {
 					if (resource instanceof IProject && resource.isAccessible()
 							&& ((IProject)resource).hasNature(IntentNature.NATURE_ID)) {
 						if (((IProject)resource).isOpen()) {
 							openedProjects.add((IProject)resource);
 						}
 					}
 				}
 				// If the Nature of a project has changed
 				if ((IResourceDelta.DESCRIPTION & delta.getFlags()) != 0) {
 					if (resource instanceof IProject) {
 						// If the Intent Nature has been added
 						if (resource.isAccessible() && ((IProject)resource).hasNature(IntentNature.NATURE_ID)) {
 							openedProjects.add((IProject)resource);
							System.out.println("detected " + resource);
 						} else {
 							// If the project had the Intent Nature but not anymore
 							if (IntentProjectManager.isManagedProject((IProject)resource)) {
 								closedProjects.add((IProject)resource);
 							}
 						}
 					}
 				} else {
 					if (resource instanceof IProject) {
 						System.out.println("no DESCRIPTION flag for " + resource);
 					}
 				}
 				break;
 			default:
 				break;
 		}
 		// return true to continue visiting children.
 		return true;
 	}
 
 	/**
 	 * Returns the projects that just have been opened.
 	 * 
 	 * @return the projects that just have been opened
 	 */
 	public Set<IProject> getOpenedProjects() {
 		return openedProjects;
 	}
 
 	/**
 	 * Returns the projects that just have been closed.
 	 * 
 	 * @return the projects that just have been closed
 	 */
 	public Set<IProject> getClosedProjects() {
 		return closedProjects;
 	}
 }
