 /*******************************************************************************
  * Copyright (c) 2005-2011 BEA Systems, Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * rfrost@bea.com - initial API and implementation
  * israelgd@mx1.ibm.com - bug 342934
  *******************************************************************************/
 
 package org.eclipse.jst.j2ee.refactor.operations;
 
 import java.util.List;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jst.j2ee.common.CompatibilityDescriptionGroup;
 import org.eclipse.jst.j2ee.model.IModelProvider;
 import org.eclipse.jst.j2ee.model.ModelProviderManager;
import org.eclipse.jst.j2ee.project.JavaEEProjectUtilities;
import org.eclipse.jst.j2ee.project.facet.IJ2EEFacetConstants;
 import org.eclipse.jst.javaee.application.Application;
 import org.eclipse.jst.javaee.applicationclient.ApplicationClient;
 import org.eclipse.jst.javaee.core.DisplayName;
 import org.eclipse.jst.javaee.core.JavaeeFactory;
 import org.eclipse.jst.javaee.ejb.EJBJar;
 import org.eclipse.jst.javaee.web.WebApp;
 import org.eclipse.wst.common.componentcore.internal.Property;
 import org.eclipse.wst.common.componentcore.internal.StructureEdit;
 import org.eclipse.wst.common.componentcore.internal.WorkbenchComponent;
 import org.eclipse.wst.common.componentcore.internal.util.ComponentUtilities;
 import org.eclipse.wst.common.componentcore.internal.util.IModuleConstants;
 import org.eclipse.wst.common.frameworks.datamodel.AbstractDataModelProvider;
 import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 import org.eclipse.wst.server.core.util.ProjectModuleFactoryDelegate;
 
 public class ProjectRenameOperation extends ProjectRefactorOperation {
 
 	public ProjectRenameOperation(final IDataModel model) {
 		super(model);
 	}
 	
 	/**
 	 * Override to return the pre-rename metadata.
 	 */
 	@Override
 	protected ProjectRefactorMetadata getProjectMetadata() {
         return (ProjectRefactorMetadata) model.getProperty(ProjectRenameDataModelProvider.ORIGINAL_PROJECT_METADATA);
 	}
 	
 	/**
 	 * Updates the project's own metadata.
 	 */
 	@Override
 	protected void updateProject(final ProjectRefactorMetadata originalMetadata) 
 		throws ExecutionException {
 		  
 		// Update the project's .component file
 		final ProjectRefactorMetadata refactoredMetadata = super.getProjectMetadata();
 		final String oldProjectName = originalMetadata.getProjectName();
 		final String newProjectName = refactoredMetadata.getProjectName();
 		IProject newProject = ResourcesPlugin.getWorkspace(). getRoot().getProject(newProjectName);
 		String originalContextRoot = ComponentUtilities.getServerContextRoot(newProject);
 		if (oldProjectName.equals(originalContextRoot)) {
 			// if old context root = project name, lets update it
 			ComponentUtilities.setServerContextRoot(newProject, newProjectName);
 		}
 		StructureEdit core = null;
 		WorkbenchComponent component = null;
 		try{
 			core = StructureEdit.getStructureEditForWrite(refactoredMetadata.getProject());
 			if(core != null){
 				component = core.getComponent();
 			}
 	
 			// if the deploy-name equals the old project name, update it in the module-specific deployment descriptor
 			if (component != null && component.getName().equals(originalMetadata.getProjectName())) {
 				component.setName(refactoredMetadata.getProjectName());
 			}
 			
 			//update the java-output-path property
 			if (component != null){
 				List<Property> properties = component.getProperties();
 				for (Property property:properties){
 					if (property.getName().equals(IModuleConstants.PROJ_REL_JAVA_OUTPUT_PATH)){
 						IPath javaOutputPath = new Path(property.getValue());
 						IPath oldProjectNamePath = new Path(oldProjectName).makeAbsolute();					
 						if (oldProjectNamePath.isPrefixOf(javaOutputPath)){
 							IPath newJavaOutputPath = new Path(newProjectName).append(javaOutputPath.removeFirstSegments(1)).makeAbsolute();						
 							property.setValue(newJavaOutputPath.toString());
 						}
 					}
 				}			
 			}
 		}finally {
 			if(core != null) {
 				core.saveIfNecessary(null);
 				core.dispose();
 			}
 		}
 		final IProject refactoredProject = refactoredMetadata.getProject();
		String pType = JavaEEProjectUtilities.getJ2EEProjectType(refactoredProject);
		if(IJ2EEFacetConstants.UTILITY.equals(pType) || "".equals(pType)) { //$NON-NLS-1$
			// skip if a utility project (will not have a ModelProvider and checking logs an error
 			return;
		}
 		final IModelProvider model = ModelProviderManager.getModelProvider(refactoredProject);
 		if (model != null) {
 			model.modify(new Runnable() {
 				public void run() {
 					final Object modelObject = model.getModelObject();
 					if (modelObject instanceof CompatibilityDescriptionGroup) {
 						CompatibilityDescriptionGroup cdg = (CompatibilityDescriptionGroup) modelObject;
 						if (cdg.getDisplayName() != null && cdg.getDisplayName().equals(oldProjectName)) {
 							cdg.setDisplayName(newProjectName);
 						}				
 					} else {
 						DisplayName dn = JavaeeFactory.eINSTANCE.createDisplayName();
 						dn.setValue(newProjectName);
 						List displayNames = null;
 						if (modelObject instanceof Application) {
 							displayNames = ((Application) modelObject).getDisplayNames();
 						} else if (modelObject instanceof WebApp) {
 							displayNames = ((WebApp)modelObject).getDisplayNames();
 						} else if (modelObject instanceof ApplicationClient) {
 							displayNames = ((ApplicationClient)modelObject).getDisplayNames();		
 						} else if (modelObject instanceof EJBJar) {
 							displayNames = ((EJBJar)modelObject).getDisplayNames();
 						}
 						if (displayNames != null && !displayNames.isEmpty()) {
 							DisplayName oldDN = (DisplayName) displayNames.get(0);
 							if (oldDN.getValue().equals(oldProjectName)) {
 								displayNames.set(0, dn);
 							}
 						}
 					}
 				}
 			}, null);
 		}
 	}
 	
 	/**
 	 * Updates the metadata for dependent projects
 	 * @throws ExecutionException
 	 */
 	@Override
 	protected void updateDependentProjects(final ProjectRefactorMetadata originalMetadata,
 			final IProgressMonitor monitor) throws ExecutionException {
 	    // If this is not an EAR, update metadata for dependent projects
         // (not performing any refactoring for projects that depend on EAR's right now)
         if (!originalMetadata.isEAR()) {
             super.updateDependentProjects(originalMetadata, monitor);
         }
 		// update any server instance refs to the refactored project
 		final ProjectRefactorMetadata refactoredMetadata = super.getProjectMetadata();
         ProjectModuleFactoryDelegate.handleGlobalProjectChange(refactoredMetadata.getProject(),
                 (IResourceDelta) model.getProperty(ProjectRenameDataModelProvider.RESOURCE_DELTA));           
 		super.updateServerRefs(originalMetadata, refactoredMetadata);
 	}
 	
 	/**
 	 * Updates the dependent EAR project to account for the renaming of the referenced project.
 	 */
 	@Override
 	protected void updateDependentEARProject(final ProjectRefactorMetadata dependentMetadata, 
 			final ProjectRefactorMetadata originalMetadata) throws ExecutionException {
         if (OptionalRefactorHandler.getInstance().shouldRefactorDependentProjectOnRename(originalMetadata, dependentMetadata)) {
 		    final IDataModel dataModel = createDataModel(dependentMetadata, originalMetadata,
 		            new UpdateDependentEARonRenameProvider());
 		    dataModel.getDefaultOperation().execute(null, null);
         }
 	}
 	
 	/**
 	 * Updates the dependent module project to account for the renaming of the referenced project.
 	 */
 	@Override
 	protected void updateDependentModuleProject(final ProjectRefactorMetadata dependentMetadata, 
 			final ProjectRefactorMetadata originalMetadata) throws ExecutionException {
         if (OptionalRefactorHandler.getInstance().shouldRefactorDependentProjectOnRename(originalMetadata, dependentMetadata)) {
             final IDataModel dataModel = createDataModel(dependentMetadata, originalMetadata,
                     new UpdateDependentModuleonRenameProvider());
             dataModel.getDefaultOperation().execute(null, null);
         }
 	}
 	
 	private IDataModel createDataModel(final ProjectRefactorMetadata dependentMetadata, 
 			final ProjectRefactorMetadata originalMetadata,
 			final AbstractDataModelProvider provider) {
 		final ProjectRefactorMetadata refactoredMetadata = super.getProjectMetadata();
 		final IDataModel dataModel = DataModelFactory.createDataModel(provider);
 		dataModel.setProperty(PROJECT_METADATA, refactoredMetadata);
 		dataModel.setProperty(DEPENDENT_PROJECT_METADATA, dependentMetadata);
 		dataModel.setProperty(ORIGINAL_PROJECT_METADATA, originalMetadata);
 		return dataModel;
 	}
 	
 }
