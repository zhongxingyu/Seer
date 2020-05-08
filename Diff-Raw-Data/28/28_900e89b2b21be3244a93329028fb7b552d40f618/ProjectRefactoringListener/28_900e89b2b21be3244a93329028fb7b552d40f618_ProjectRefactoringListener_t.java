 /*******************************************************************************
  * Copyright (c) 2005 BEA Systems, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * rfrost@bea.com - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.jst.j2ee.refactor.listeners;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.resources.WorkspaceJob;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEPlugin;
 import org.eclipse.jst.j2ee.refactor.RefactorResourceHandler;
 import org.eclipse.jst.j2ee.refactor.operations.ProjectDeleteDataModelProvider;
 import org.eclipse.jst.j2ee.refactor.operations.ProjectRefactorMetadata;
 import org.eclipse.jst.j2ee.refactor.operations.ProjectRefactoringDataModelProvider;
 import org.eclipse.jst.j2ee.refactor.operations.ProjectRenameDataModelProvider;
 import org.eclipse.wst.common.componentcore.ModuleCoreNature;
 import org.eclipse.wst.common.componentcore.internal.builder.DependencyGraphManager;
 import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 
 /**
  * Listens for project rename/delete events and, if the project had the
  * ModuleCore nature, executes the appropriate logic to update
  * project references.
   */
 public final class ProjectRefactoringListener implements IResourceChangeListener, IResourceDeltaVisitor {
 	
 	/*
 	 * Map from name of deleted project to ProjectRefactorMetadata instances.
 	 */
 	private final Map deletedProjectMetadata = new HashMap();
 	
 	/**
 	 * Maintains a cache of project depencencies;
 	 */
 	//private final ProjectDependencyCache cache;
 	
 	//private boolean hasProcessedSavedState = false;
 	
 	public ProjectRefactoringListener() {//final ProjectDependencyCache dependencyCache) {
 		//cache = dependencyCache;
 		// force a refresh of the DependencyGraphManager; was hitting an NPE
 		// in StructureEdit when the DGM was getting constructed during the receipt of the
 		// first pre-delete event
 		DependencyGraphManager.getInstance();
 	}
 	
 	// XXX not very feasible to make workspace save participation work given the need to process the
 	// pre-delete events
 	/*private synchronized void processPendingEvents() {
 		if (hasProcessedSavedState) {
 			return;
 		}
 		hasProcessedSavedState = true;
 		//process any project rename/delete events that have occurred prior to our activation 
 		try {
 			ISavedState lastState =	ResourcesPlugin.getWorkspace().addSaveParticipant(J2EEPlugin.getDefault(), this);
 			if (lastState != null) {
 				lastState.processResourceChangeEvents(this);
 			}
 		} catch (CoreException ce) {
 			Logger.getLogger().logError(ce);
 		}
 	}*/
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
 	 */
 	public void resourceChanged(final IResourceChangeEvent event) {
 		// if we haven't already processed the pending resource change events, process them 
 		//processPendingEvents();
 		
 		// need to capture PRE_DELETE events so that metadata about the
 		// deleted project can be collected and cached
 		try {
 			/*if (event.getType() == IResourceChangeEvent.POST_BUILD) {
 				// we get this event type when processing pending events
 				IResource resource = event.getResource();
 				IResourceDelta delta = event.getDelta();
 				if (resource instanceof IProject && delta.getKind() == IResourceDelta.REMOVED) {
 					cacheDeletedProjectMetadata((IProject)resource);
 				} else {
 					delta.accept(this);
 				}
 			} else */
 			if (event.getType() == IResourceChangeEvent.PRE_DELETE) {
 				// for now, only dependencies on ModuleCoreNature projects
 				final IProject project = (IProject) event.getResource();
 				if (ModuleCoreNature.getModuleCoreNature(project) != null) {
 					cacheDeletedProjectMetadata(project);
 				}
 			} else {
 				event.getDelta().accept(this);
 			}
 		} catch (CoreException ce) {
 			Logger.getLogger().logError(ce);
 		}
 	}
 	
 	private synchronized void cacheDeletedProjectMetadata(final IProject project) {
 		final ProjectRefactorMetadata metadata = new ProjectRefactorMetadata(project, ProjectRefactorMetadata.REFERER_CACHING);
 		// precompute the metadata while the project still exists
 		metadata.computeMetadata();
		metadata.computeServers();
 		metadata.computeDependentMetadata(ProjectRefactorMetadata.REF_CACHING,
 				DependencyGraphManager.getInstance().getDependencyGraph().getReferencingComponents(project));
 		//, cache.getDependentProjects(project));
 		deletedProjectMetadata.put(project.getName(), metadata); 		
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
 	 */
 	public boolean visit(final IResourceDelta delta) throws CoreException {
 		final IResource resource = delta.getResource();
 		if (resource instanceof IWorkspaceRoot) {
 			// delta is at the workspace root so keep going
 			return true;
 		} else if (resource instanceof IProject) {
 			processProjectDelta((IProject) resource, delta);
 		}
 		return false;
 	}
 
 	/*
 	 * Process the project delta in a sync block.
 	 */
 	private synchronized void processProjectDelta(final IProject project, final IResourceDelta delta) throws CoreException {
 		final int kind = delta.getKind();
 		final int flags = delta.getFlags();
 
 		if (kind == IResourceDelta.REMOVED) {
 			if (flags == 0) {
 				// Remove all entries int the project dependency cache
 				//cache.removeProject(project);
 				// if the kind is REMOVED and there are no special flags, the project was deleted
 				ProjectRefactorMetadata metadata = (ProjectRefactorMetadata) deletedProjectMetadata.remove(project.getName()); 
 				// note: only projects with ModuleCoreNature will have cached metadata
 				if (metadata != null) {
 					processDelete(metadata);
 				} else {
 					// likely due to missing the pre-delete event, ignore for now
 					//Logger.getLogger().logWarning(RefactorResourceHandler.getString("pre_delete_not_received_for_removed",  
 					//new Object[]{project.getName()}));
 				}
 			} 
 		} else if (kind == IResourceDelta.ADDED && wasRenamed(flags)) { // was renamed
 			// get the original name
 			final String originalName = delta.getMovedFromPath().lastSegment();
 			//Logger.getLogger().logInfo("Added event for " + originalName + " with flags " + flags);
 			// we get PRE_DELETE events on rename so retrieve this
 			ProjectRefactorMetadata originalMetadata = (ProjectRefactorMetadata) deletedProjectMetadata.remove(originalName);
 			// get the metadata for the new project
 			final ProjectRefactorMetadata newMetadata = new ProjectRefactorMetadata(project);
 			// note: only projects with ModuleCoreNature will have cached metadata
 			if (originalMetadata != null) {
 				processRename(originalMetadata, newMetadata);
 				// Rename all entries in the project dependency cache
 				//cache.replaceProject(originalProject, project);
 			} else {
 				// likely due to missing the pre-delete event, so set the original to a metadata based on new project
 				// and reset project to one based on original name
 				//Logger.getLogger().logWarning(RefactorResourceHandler.getString("pre_delete_not_received_for_renamed", new Object[]{originalName + " " + flags}));
 			}
 		} 
 		//else if (kind == IResourceDelta.CHANGED || kind == IResourceDelta.ADDED) { 
 		// (re)compute the dependencies
 		//cache.refreshDependencies(project);
 		//}		
 	}
 	
 	/*
 	 * Determines if the project was renamed based on the IResourceDelta flags 
 	 */
 	private boolean wasRenamed(final int flags) {
 		if ((flags & IResourceDelta.DESCRIPTION) > 0
 			&& (flags & IResourceDelta.MOVED_FROM) > 0) {
 			return true;
 		}
 		return false;
 	}
 	
 	/*
 	 * Processes the renaming of a project.
 	 */
 	private void processRename(final ProjectRefactorMetadata originalMetadata, final ProjectRefactorMetadata newMetadata ) {
 		WorkspaceJob job = new WorkspaceJob("J2EEProjectRenameJob") {
 			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
 				final IDataModel dataModel = DataModelFactory.createDataModel(new ProjectRenameDataModelProvider());
 				dataModel.setProperty(ProjectRefactoringDataModelProvider.PROJECT_METADATA, newMetadata);
 				dataModel.setProperty(ProjectRenameDataModelProvider.ORIGINAL_PROJECT_METADATA, originalMetadata);
 				try {
 					dataModel.getDefaultOperation().execute(monitor, null);
 				} catch (ExecutionException e) {
 					final String msg = RefactorResourceHandler.getString("error_updating_project_on_rename", new Object[]{originalMetadata.getProjectName()});
 					Logger.getLogger().logError(msg);
 					Logger.getLogger().logError(e);
 					return new Status(Status.ERROR, J2EEPlugin.PLUGIN_ID, 0, msg, e);
 				}				
 				return Status.OK_STATUS;
 			}
 		};
 		// XXX note: might want to consider switching to a MultiRule for optimization
 		job.setRule(ResourcesPlugin.getWorkspace().getRoot());
 		job.schedule();
 	}
 	
 	/*
 	 * Processes the deletion of a project.
 	 */
 	private void processDelete(final ProjectRefactorMetadata metadata) {
 		WorkspaceJob job = new WorkspaceJob("J2EEProjectDeleteJob") {
 			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
 				final IDataModel dataModel = DataModelFactory.createDataModel(new ProjectDeleteDataModelProvider());
 				dataModel.setProperty(ProjectRefactoringDataModelProvider.PROJECT_METADATA, metadata);
 				try {
 					dataModel.getDefaultOperation().execute(monitor, null);
 				} catch (ExecutionException e) {
 					final String msg = RefactorResourceHandler.getString("error_updating_project_on_delete", new Object[]{metadata.getProjectName()});
 					Logger.getLogger().logError(msg);
 					Logger.getLogger().logError(e);
 					return new Status(Status.ERROR, J2EEPlugin.PLUGIN_ID, 0, msg, e);
 				}
 				return Status.OK_STATUS;
 			}
 		};
 		// XXX note: might want to consider switching to a MultiRule for optimization
 		job.setRule(ResourcesPlugin.getWorkspace().getRoot());
 		job.schedule();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.core.resources.ISaveParticipant#doneSaving(org.eclipse.core.resources.ISaveContext)
 	 */
 //	public void doneSaving(ISaveContext context) {
 //	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.core.resources.ISaveParticipant#prepareToSave(org.eclipse.core.resources.ISaveContext)
 	 */
 //	public void prepareToSave(ISaveContext context) throws CoreException {
 //	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.core.resources.ISaveParticipant#rollback(org.eclipse.core.resources.ISaveContext)
 	 */
 //	public void rollback(ISaveContext context) {
 //	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.core.resources.ISaveParticipant#saving(org.eclipse.core.resources.ISaveContext)
 	 */
 //	public void saving(ISaveContext context) throws CoreException {
 //		// no state to be saved by the plug-in, but request a
 //		// resource delta to be used on next activation.
 //	    context.needDelta();
 //	}
 
 }
