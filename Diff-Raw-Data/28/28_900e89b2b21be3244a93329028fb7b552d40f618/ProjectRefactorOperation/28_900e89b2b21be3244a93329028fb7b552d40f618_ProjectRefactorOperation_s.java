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
 
 package org.eclipse.jst.j2ee.refactor.operations;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jst.j2ee.refactor.RefactorResourceHandler;
 import org.eclipse.wst.common.frameworks.datamodel.AbstractDataModelOperation;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 import org.eclipse.wst.server.core.IModule;
 import org.eclipse.wst.server.core.IServer;
 import org.eclipse.wst.server.core.IServerWorkingCopy;
 import org.eclipse.wst.server.core.ServerUtil;
 
 /**
  * Abstract base class for project refactoring operations.
  */
 public abstract class ProjectRefactorOperation extends AbstractDataModelOperation 
 	implements ProjectRefactoringProperties {
 
 	/**
 	 * Creates a new refactoring operation for the specified data model.
 	 * @param model The data model.
 	 */
 	public ProjectRefactorOperation(final IDataModel model) {
 		super(model);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.core.commands.operations.IUndoableOperation#execute(org.eclipse.core.runtime.IProgressMonitor, org.eclipse.core.runtime.IAdaptable)
 	 */
 	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
 		try {
 			final ProjectRefactorMetadata refactoredMetadata = (ProjectRefactorMetadata) getProjectMetadata();
 
 			// Update this project's metadata
 			if (refactoredMetadata.hasModuleCoreNature()) {
 				updateProject(refactoredMetadata);
 			}
 			
 			// get the metadata for all dependent projects
 			final ProjectRefactorMetadata[] dependentMetadata = refactoredMetadata.getDependentMetadata();
 			if (monitor != null) {
 				monitor.beginTask(RefactorResourceHandler.getString("task_name"), dependentMetadata.length); 
 			}
 			ProjectRefactorMetadata metadata;
 			for (int i = 0; i < dependentMetadata.length; i++) {
 				metadata = dependentMetadata[i];
 				// if the project is an EAR, execute the update operation created by the
 				// DependentEARUpdateProvider; if the project is a non-EAR project with the
 				// module core nature, execute the appropriate update
 				if (metadata.isEAR()) {
 					updateDependentEARProject(metadata, refactoredMetadata);
 				} else if (metadata.hasModuleCoreNature()) { 
 					updateDependentModuleProject(metadata, refactoredMetadata);
 				}
 				if (monitor != null) {
 					monitor.worked(1);
 				}
 			}
 		} finally {
 			if (monitor != null) {
 				monitor.done();
 			}
 		}
 
 		return Status.OK_STATUS;
 	}
 
 	/**
 	 * Updates the project's own metadata.
 	 */
 	protected abstract void updateProject(final ProjectRefactorMetadata refactoredMetadata) 
 	throws ExecutionException;
 	
 	/**
 	 * Updates any server references.
 	 */
 	protected void updateServerRefs(final ProjectRefactorMetadata originalMetadata, 
 			final ProjectRefactorMetadata newMetadata)
 		throws ExecutionException {
 		final IModule originalModule = originalMetadata.getModule();
 		if (originalModule == null) {
 			// no module for the original project, so return
 			return;
 		}
 		final IModule[] toRemove = new IModule[]{originalModule};
 		
 		IModule newModule = null;
 		IModule[] toAdd = new IModule[0];
 		if (newMetadata != null) {
 			newModule = newMetadata.getModule();
 			if (newModule == null) {
 				// no module for the new project, so return
 				return;
 			}
 			toAdd = new IModule[]{newModule};
 		}
 		
		final IServer[] server = ServerUtil.getServersByModule(originalModule, null);
 		IServerWorkingCopy wc = null;
 		for (int i = 0; i < server.length; i++) {
 			try {
 				wc = server[i].createWorkingCopy();
 				List list = Arrays.asList(server[i].getModules());
 				if (list.contains(originalModule)) {
 					// remove the original and potentially add a new
 					ServerUtil.modifyModules(wc, toAdd, toRemove, null);
 				}
 			} catch (CoreException ce) {
 				throw new ExecutionException(RefactorResourceHandler.getString("error_updating_server", 
 						new Object[]{server[i].getId(), originalMetadata.getProjectName()}), ce);
 			} finally {
 				try {
 					wc.saveAll(true, null);
 				} catch (CoreException ce) {
 					throw new ExecutionException(RefactorResourceHandler.getString("error_updating_server", 
 							new Object[]{server[i].getId(), originalMetadata.getProjectName()}), ce);					
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Updates the dependent EAR project to account for the refactoring of the referenced project.
 	 */
 	protected abstract void updateDependentEARProject(final ProjectRefactorMetadata dependentMetadata, 
 			final ProjectRefactorMetadata refactoredMetadata) throws ExecutionException;
 	
 	/**
 	 * Updates the dependent module project to account for the refactoring of the referenced project.
 	 */
 	protected abstract void updateDependentModuleProject(final ProjectRefactorMetadata dependentMetadata, 
 			final ProjectRefactorMetadata refactoredMetadata) throws ExecutionException;
 
 	/**
 	 * Retrieves the ProjectRefactorMetadata property.
 	 */
 	protected ProjectRefactorMetadata getProjectMetadata() {
 		return (ProjectRefactorMetadata) model.getProperty(PROJECT_METADATA);
 	}
 	
 	/**
 	 * Override to disable redo support
 	 * @see org.eclipse.core.commands.operations.IUndoableOperation#canRedo()
 	 */
 	public boolean canRedo() {
 		return false;
 	}
 
 	/**
 	 * Override to disable undo support.
 	 * @see org.eclipse.core.commands.operations.IUndoableOperation#canUndo()
 	 */
 	public boolean canUndo() {
 		return false;
 	}
 	
 }
