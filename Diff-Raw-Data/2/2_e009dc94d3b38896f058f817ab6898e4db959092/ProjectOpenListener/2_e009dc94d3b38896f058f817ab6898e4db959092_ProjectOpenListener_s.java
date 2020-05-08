 /*******************************************************************************
  * Copyright (c) 2006-2011
  * Software Technology Group, Dresden University of Technology
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *   Software Technology Group - TU Dresden, Germany 
  *      - initial API and implementation
  ******************************************************************************/
 package org.reuseware.sokan.resource.build;
 
 import java.util.Collections;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.reuseware.sokan.index.SokanIndexPlugin;
 import org.reuseware.sokan.index.util.StoreUtil;
 
 /**
  * A resource change listener that reacts on the opening of projects
  * that registers the stores of a newly opened project with the index and
  * runs the full indexing of the project.
  */
 public class ProjectOpenListener implements IResourceChangeListener {
 
 	private final StoreUtil storeUtil = StoreUtil.INSTANCE;
 	
 	/**
 	 * Checks if the event is a post-project-open event and
 	 * runs the indexing of the project if that is the case.
 	 * 
 	 * @param event the resource change event
 	 */
 	public void resourceChanged(IResourceChangeEvent event) {
 		if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
 			IResourceDelta delta = event.getDelta();
 
 			//get deltas for the projects
 			IResourceDelta[] projectDeltas = delta.getAffectedChildren();
 			for (int i = 0; i < projectDeltas.length; i++) {
 				int kind = projectDeltas[i].getKind();
 				int flags = projectDeltas[i].getFlags();
 				
 				if (kind == IResourceDelta.CHANGED
 						&& (flags & IResourceDelta.OPEN) == IResourceDelta.OPEN 
 						&& projectDeltas[i].getResource() instanceof IProject) {
 					final IProject project = (IProject) projectDeltas[i].getResource();
 					
 					if (project.isOpen()) {
 						Job job = new Job("indexing") {
 							@Override
 							protected IStatus run(IProgressMonitor monitor) {
 								try {
 									//find and remember stores in project
 									storeUtil.getWorkspaceStores().addAll(
 											storeUtil.getAllStores(project));
 									project.build(
 											IndexBuilder.FULL_BUILD, 
 											IndexBuilder.BUILDER_ID,
											Collections.emptyMap(), monitor);
 								} catch (CoreException e) {
 									SokanIndexPlugin.logError("", e);
 								}
 								return Status.OK_STATUS;
 							}
 						};
 						job.setPriority(Job.BUILD);
 						job.schedule();
 					}
 				}
 			}
 		}
 	}
 }
