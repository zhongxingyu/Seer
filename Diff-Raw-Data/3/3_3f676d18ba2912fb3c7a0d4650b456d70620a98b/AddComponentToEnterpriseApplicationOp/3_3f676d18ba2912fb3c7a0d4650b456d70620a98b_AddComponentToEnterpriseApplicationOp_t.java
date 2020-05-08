 /*******************************************************************************
  * Copyright (c) 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.application.internal.operations;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.jst.j2ee.application.Application;
 import org.eclipse.jst.j2ee.application.ApplicationPackage;
 import org.eclipse.jst.j2ee.application.Module;
 import org.eclipse.jst.j2ee.application.WebModule;
 import org.eclipse.jst.j2ee.componentcore.util.EARArtifactEdit;
 import org.eclipse.jst.j2ee.internal.J2EEConstants;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
 import org.eclipse.jst.j2ee.project.facet.EarFacetRuntimeHandler;
 import org.eclipse.wst.common.componentcore.datamodel.properties.ICreateReferenceComponentsDataModelProperties;
 import org.eclipse.wst.common.componentcore.internal.ReferencedComponent;
 import org.eclipse.wst.common.componentcore.internal.StructureEdit;
 import org.eclipse.wst.common.componentcore.internal.WorkbenchComponent;
 import org.eclipse.wst.common.componentcore.internal.operation.CreateReferenceComponentsOp;
 import org.eclipse.wst.common.componentcore.internal.util.IModuleConstants;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 import org.eclipse.wst.common.project.facet.core.IFacetedProject;
 import org.eclipse.wst.common.project.facet.core.IProjectFacet;
 import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
 
 public class AddComponentToEnterpriseApplicationOp extends CreateReferenceComponentsOp {
 	public static final String metaInfFolderDeployPath = "/"; //$NON-NLS-1$
 
 	public AddComponentToEnterpriseApplicationOp(IDataModel model) {
 		super(model);
 	}
 
 	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
 		if (monitor != null) {
 			monitor.beginTask("", 3);
 		}
 
 		try {
 			super.execute(submon(monitor, 1), info);
 			updateEARDD(submon(monitor, 1));
 			updateModuleRuntimes(submon(monitor, 1));
 
 			return OK_STATUS;
 		} finally {
 			if (monitor != null) {
 				monitor.done();
 			}
 		}
 	}
 
 	protected String getArchiveName(IVirtualComponent comp) {
 		boolean useArchiveURI = true;
 		IFacetedProject facetedProject = null;
 		try {
 			facetedProject = ProjectFacetsManager.create(comp.getProject());
 		} catch (CoreException e) {
 			useArchiveURI = false;
 		}
 
 		if (useArchiveURI && facetedProject != null && ProjectFacetsManager.isProjectFacetDefined(IModuleConstants.JST_UTILITY_MODULE)) {
 			IProjectFacet projectFacet = ProjectFacetsManager.getProjectFacet(IModuleConstants.JST_UTILITY_MODULE);
 			useArchiveURI = projectFacet != null && facetedProject.hasProjectFacet(projectFacet);
 		}
 		if (useArchiveURI) {
 			return super.getArchiveName(comp);
 		}
 		return ""; //$NON-NLS-1$
 	}
 	
 	protected void updateEARDD(IProgressMonitor monitor) {
 
 		EARArtifactEdit earEdit = null;
 		StructureEdit se = null;
 		try {
 			IVirtualComponent sourceComp = (IVirtualComponent) model.getProperty(ICreateReferenceComponentsDataModelProperties.SOURCE_COMPONENT);
 			earEdit = EARArtifactEdit.getEARArtifactEditForWrite(sourceComp.getProject());
 			se = StructureEdit.getStructureEditForWrite(sourceComp.getProject());
 			if (earEdit != null) {
 				Application application = earEdit.getApplication();
 				List list = (List) model.getProperty(ICreateReferenceComponentsDataModelProperties.TARGET_COMPONENT_LIST);
 				Map map = (Map) model.getProperty(IAddComponentToEnterpriseApplicationDataModelProperties.TARGET_COMPONENTS_TO_URI_MAP);
 				if (list != null && list.size() > 0) {
 					for (int i = 0; i < list.size(); i++) {
 						StructureEdit compse = null;
 						IVirtualComponent wc = (IVirtualComponent) list.get(i);
 						WorkbenchComponent earwc = se.getComponent();
 						try {
 							compse = StructureEdit.getStructureEditForWrite(wc.getProject());
 							WorkbenchComponent refwc = compse.getComponent();
 							ReferencedComponent ref = se.findReferencedComponent(earwc, refwc);
 							Module mod = addModule(application, wc, (String) map.get(wc));
 							ref.setDependentObject(mod);
 						} finally {
 							if (compse != null) {
 								compse.saveIfNecessary(monitor);
 								compse.dispose();
 							}
 						}
 					}
 				}
 			}
 			se.saveIfNecessary(monitor);
 			earEdit.saveIfNecessary(monitor);
 		} catch (Exception e) {
 			Logger.getLogger().logError(e);
 		} finally {
 			if (earEdit != null)
 				earEdit.dispose();
 			if (se != null)
 				se.dispose();
 		}
 	}
 
 	protected Module createNewModule(IVirtualComponent wc) {
 		if (J2EEProjectUtilities.isDynamicWebProject(wc.getProject())) {
 			return ((ApplicationPackage) EPackage.Registry.INSTANCE.getEPackage(ApplicationPackage.eNS_URI)).getApplicationFactory().createWebModule();
 		} else if (J2EEProjectUtilities.isEJBProject(wc.getProject())) {
 			return ((ApplicationPackage) EPackage.Registry.INSTANCE.getEPackage(ApplicationPackage.eNS_URI)).getApplicationFactory().createEjbModule();
 		} else if (J2EEProjectUtilities.isApplicationClientProject(wc.getProject())) {
 			return ((ApplicationPackage) EPackage.Registry.INSTANCE.getEPackage(ApplicationPackage.eNS_URI)).getApplicationFactory().createJavaClientModule();
 		} else if (J2EEProjectUtilities.isJCAProject(wc.getProject())) {
 			return ((ApplicationPackage) EPackage.Registry.INSTANCE.getEPackage(ApplicationPackage.eNS_URI)).getApplicationFactory().createConnectorModule();
 		}
 		return null;
 	}
 
 	protected Module addModule(Application application, IVirtualComponent wc, String name) {
 		Application dd = application;
 		Module existingModule = dd.getFirstModule(name);
 
 		if (existingModule == null) {
 			existingModule = createNewModule(wc);
 			if (existingModule != null) {
 				existingModule.setUri(name);
 				if (existingModule instanceof WebModule) {
 					Properties props = wc.getMetaProperties();
 					String contextroot = ""; //$NON-NLS-1$
 					if ((props != null) && (props.containsKey(J2EEConstants.CONTEXTROOT)))
 						contextroot = props.getProperty(J2EEConstants.CONTEXTROOT);
 					((WebModule) existingModule).setContextRoot(contextroot);
 				}
 				dd.getModules().add(existingModule);
 			}
 		}
 		return existingModule;
 	}
 
 	private void updateModuleRuntimes(final IProgressMonitor monitor) {
 		if (monitor != null) {
 			monitor.beginTask("", 10);
 		}
 
 		try {
 			final IVirtualComponent ear = (IVirtualComponent) this.model.getProperty(ICreateReferenceComponentsDataModelProperties.SOURCE_COMPONENT);
 
 			final IProject earpj = ear.getProject();
 
 			final List moduleComponents = (List) this.model.getProperty(ICreateReferenceComponentsDataModelProperties.TARGET_COMPONENT_LIST);
 
 			final Set moduleProjects = new HashSet();
 
 			for (Iterator itr = moduleComponents.iterator(); itr.hasNext();) {
 				moduleProjects.add(((IVirtualComponent) itr.next()).getProject());
 			}
 
 			if (monitor != null) {
 				monitor.worked(1);
 			}
 
 			EarFacetRuntimeHandler.updateModuleProjectRuntime(earpj, moduleProjects, submon(monitor, 9));
 		} catch (Exception e) {
 			Logger.getLogger().logError(e);
 		} finally {
 			if (monitor != null) {
 				monitor.done();
 			}
 		}
 	}
 
 	private static IProgressMonitor submon(final IProgressMonitor parent, final int ticks) {
 		return (parent == null ? null : new SubProgressMonitor(parent, ticks));
 	}
 
 	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
