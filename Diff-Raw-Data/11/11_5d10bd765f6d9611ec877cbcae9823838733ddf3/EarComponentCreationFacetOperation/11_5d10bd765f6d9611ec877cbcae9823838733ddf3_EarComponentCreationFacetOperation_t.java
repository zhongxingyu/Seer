 /*******************************************************************************
  * Copyright (c) 2003, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.internal.earcreation;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.jst.j2ee.datamodel.properties.IEarComponentCreationDataModelProperties;
 import org.eclipse.jst.j2ee.datamodel.properties.IJ2EEComponentCreationDataModelProperties;
 import org.eclipse.jst.j2ee.datamodel.properties.IJavaComponentCreationDataModelProperties;
 import org.eclipse.jst.j2ee.project.facet.EARFacetProjectCreationDataModelProvider;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.datamodel.FacetProjectCreationDataModelProvider;
 import org.eclipse.wst.common.componentcore.datamodel.properties.IComponentCreationDataModelProperties;
 import org.eclipse.wst.common.componentcore.datamodel.properties.ICreateReferenceComponentsDataModelProperties;
 import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetDataModelProperties;
 import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetProjectCreationDataModelProperties;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.frameworks.datamodel.AbstractDataModelOperation;
 import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 import org.eclipse.wst.common.project.facet.core.IFacetedProject;
 import org.eclipse.wst.common.project.facet.core.runtime.IRuntime;
 import org.eclipse.wst.common.project.facet.core.runtime.RuntimeManager;
 import org.eclipse.wst.server.core.ServerCore;
 
 /**
  * @deprecated
  * @see EARFacetProjectCreationDataModelProvider
  */
 public class EarComponentCreationFacetOperation extends AbstractDataModelOperation implements IFacetProjectCreationDataModelProperties {
 
 	public EarComponentCreationFacetOperation(IDataModel model) {
 		super(model);
 	}
 
 	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
 
 
 		IDataModel dm = DataModelFactory.createDataModel(new FacetProjectCreationDataModelProvider());
 		String runtime = model.getStringProperty(IJavaComponentCreationDataModelProperties.RUNTIME_TARGET_ID);
 		IRuntime facetRuntime = null;
 		try {
			runtime = ServerCore.findRuntime(runtime).getName();
 			facetRuntime = RuntimeManager.getRuntime(runtime);
 		} catch (Exception e) {
 			// proceed with facetRuntime = null
 		}
 		dm.setProperty(FACET_RUNTIME, facetRuntime);
 		String projectName = model.getStringProperty(IComponentCreationDataModelProperties.PROJECT_NAME);
 		dm.setProperty(FACET_PROJECT_NAME, projectName);
 
 		IDataModel newDM = setupEarInstallAction();
 		FacetDataModelMap map = (FacetDataModelMap) dm.getProperty(FACET_DM_MAP);
 		map.add(newDM);
 
 		IStatus stat = dm.getDefaultOperation().execute(monitor, info);
 		if (stat.isOK())
 			addModulesToEAR(monitor);
 
 		return stat;
 	}
 
 	protected IDataModel setupEarInstallAction() {
 		String versionStr = model.getPropertyDescriptor(IJ2EEComponentCreationDataModelProperties.COMPONENT_VERSION).getPropertyDescription();
 		IDataModel earFacetInstallDataModel = DataModelFactory.createDataModel(new EarFacetInstallDataModelProvider());
 		earFacetInstallDataModel.setProperty(IFacetDataModelProperties.FACET_PROJECT_NAME, model.getStringProperty(IComponentCreationDataModelProperties.PROJECT_NAME));
 		earFacetInstallDataModel.setProperty(IFacetDataModelProperties.FACET_VERSION_STR, versionStr);
 		return earFacetInstallDataModel;
 	}
 
 	protected void setRuntime(IFacetedProject facetProj) throws CoreException {
 		String runtimeID = model.getStringProperty(IJavaComponentCreationDataModelProperties.RUNTIME_TARGET_ID);
 		String runtimeName = ServerCore.findRuntime(runtimeID).getName();
 		try {
 			IRuntime runtime = RuntimeManager.getRuntime(runtimeName);
 			facetProj.setRuntime(runtime, null);
 		} catch (IllegalArgumentException e) {
 			Logger.getLogger().logError(e);
 		}
 	}
 
 	private IStatus addModulesToEAR(IProgressMonitor monitor) {
 		IStatus stat = OK_STATUS;
 		try {
 			IDataModel dm = (IDataModel) model.getProperty(IJ2EEComponentCreationDataModelProperties.NESTED_ADD_COMPONENT_TO_EAR_DM);
 			String projectName = model.getStringProperty(IComponentCreationDataModelProperties.PROJECT_NAME);
 			IProject project = ProjectUtilities.getProject(projectName);
 			IVirtualComponent component = ComponentCore.createComponent(project);
 
 
 			dm.setProperty(ICreateReferenceComponentsDataModelProperties.SOURCE_COMPONENT, component);
 
 			List moduleProjectsList = (List) model.getProperty(IEarComponentCreationDataModelProperties.J2EE_PROJECTS_LIST);
 			if (moduleProjectsList != null && !moduleProjectsList.isEmpty()) {
 				List moduleComponentsList = new ArrayList(moduleProjectsList.size());
 				for (int i = 0; i < moduleProjectsList.size(); i++) {
 					moduleComponentsList.add(ComponentCore.createComponent((IProject) moduleProjectsList.get(i)));
 				}
 				dm.setProperty(ICreateReferenceComponentsDataModelProperties.TARGET_COMPONENT_LIST, moduleComponentsList);
 				stat = dm.validateProperty(ICreateReferenceComponentsDataModelProperties.TARGET_COMPONENT_LIST);
 				if (stat != OK_STATUS)
 					return stat;
 				dm.getDefaultOperation().execute(monitor, null);
 			}
 		} catch (Exception e) {
 			Logger.getLogger().log(e);
 		}
 		return stat;
 	}
 }
