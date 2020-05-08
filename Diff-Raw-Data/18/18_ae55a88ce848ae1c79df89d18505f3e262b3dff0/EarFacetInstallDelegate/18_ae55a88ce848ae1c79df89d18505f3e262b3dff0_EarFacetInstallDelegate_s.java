 /******************************************************************************
  * Copyright (c) 2005 BEA Systems, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Konstantin Komissarchik - initial API and implementation
  ******************************************************************************/
 
 package org.eclipse.jst.j2ee.project.facet;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.jst.common.project.facet.WtpUtils;
 import org.eclipse.jst.j2ee.application.internal.operations.AddComponentToEnterpriseApplicationDataModelProvider;
 import org.eclipse.jst.j2ee.componentcore.util.EARArtifactEdit;
 import org.eclipse.jst.j2ee.internal.J2EEConstants;
 import org.eclipse.jst.j2ee.internal.common.J2EEVersionUtil;
 import org.eclipse.jst.j2ee.internal.earcreation.IEarFacetInstallDataModelProperties;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.datamodel.properties.ICreateReferenceComponentsDataModelProperties;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
 import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 import org.eclipse.wst.common.project.facet.core.IDelegate;
 import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
 
 public final class EarFacetInstallDelegate implements IDelegate {
 
 	public void execute(final IProject project, final IProjectFacetVersion fv, final Object cfg, final IProgressMonitor monitor) throws CoreException {
 		if (monitor != null) {
 			monitor.beginTask("", 1);
 		}
 
 		try {
 			IDataModel model = (IDataModel) cfg;
 
 			if (monitor != null) {
 				monitor.worked(1);
 			}
 			// Add WTP natures.
 
 			WtpUtils.addNaturestoEAR(project);
 
 			final IVirtualComponent c = ComponentCore.createComponent(project);
 			c.create(0, null);
 
 			final IVirtualFolder earroot = c.getRootFolder();
 			earroot.createLink(new Path("/" + model.getStringProperty(IEarFacetInstallDataModelProperties.CONTENT_DIR)), 0, null);
 
 			if (!project.getFile(J2EEConstants.APPLICATION_DD_URI).exists()) {
 				String ver = fv.getVersionString();
 				int nVer = J2EEVersionUtil.convertVersionStringToInt(ver);
 				EARArtifactEdit.createDeploymentDescriptor(project, nVer);
 			}
 			List dependentProjects = (List)model.getProperty(IEarFacetInstallDataModelProperties.J2EE_PROJECTS_LIST);
 			dependentProjects.addAll((List)model.getProperty(IEarFacetInstallDataModelProperties.JAVA_PROJECT_LIST));
 			if(!dependentProjects.isEmpty()){
 				List dependentComponents = new ArrayList(dependentProjects.size());
 				for(Iterator iterator = dependentProjects.iterator(); iterator.hasNext();){
					dependentComponents.add(ComponentCore.createComponent((IProject)iterator.next()));
 				}
 					
 				final IDataModel dataModel = DataModelFactory.createDataModel(new AddComponentToEnterpriseApplicationDataModelProvider());
 				dataModel.setProperty(ICreateReferenceComponentsDataModelProperties.SOURCE_COMPONENT, c);
 				List modList = (List) dataModel.getProperty(ICreateReferenceComponentsDataModelProperties.TARGET_COMPONENT_LIST);
 				modList.addAll(dependentComponents);
 				dataModel.setProperty(ICreateReferenceComponentsDataModelProperties.TARGET_COMPONENT_LIST, modList);
 				try {
 					dataModel.getDefaultOperation().execute(null, null);
 				} catch (ExecutionException e) {
 					Logger.getLogger().logError(e);
 				}
 			}
 		}
 
 		finally {
 			if (monitor != null) {
 				monitor.done();
 			}
 		}
 	}
 
 }
