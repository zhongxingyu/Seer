 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
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
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.jem.util.UIContextDetermination;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.jst.j2ee.internal.J2EEConstants;
 import org.eclipse.jst.j2ee.internal.J2EEVersionConstants;
 import org.eclipse.jst.j2ee.internal.common.classpath.J2EEComponentClasspathUpdater;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
 import org.eclipse.jst.j2ee.model.IEARModelProvider;
 import org.eclipse.jst.j2ee.model.IModelProvider;
 import org.eclipse.jst.j2ee.model.ModelProviderManager;
 import org.eclipse.jst.j2ee.project.facet.EarFacetRuntimeHandler;
 import org.eclipse.jst.javaee.application.ApplicationFactory;
 import org.eclipse.jst.javaee.application.Module;
 import org.eclipse.jst.javaee.application.Web;
 import org.eclipse.jst.jee.application.ICommonApplication;
 import org.eclipse.jst.jee.application.ICommonModule;
 import org.eclipse.swt.widgets.Display;
import org.eclipse.wst.common.componentcore.ArtifactEdit;
 import org.eclipse.wst.common.componentcore.datamodel.properties.ICreateReferenceComponentsDataModelProperties;
import org.eclipse.wst.common.componentcore.internal.ArtifactEditModel;
 import org.eclipse.wst.common.componentcore.internal.ReferencedComponent;
 import org.eclipse.wst.common.componentcore.internal.StructureEdit;
 import org.eclipse.wst.common.componentcore.internal.WorkbenchComponent;
 import org.eclipse.wst.common.componentcore.internal.operation.CreateReferenceComponentsOp;
import org.eclipse.wst.common.componentcore.internal.util.ComponentUtilities;
 import org.eclipse.wst.common.componentcore.internal.util.IModuleConstants;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.internal.emfworkbench.integration.EditModel;
import org.eclipse.wst.common.internal.emfworkbench.validateedit.IValidateEditContext;
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
 			J2EEComponentClasspathUpdater.getInstance().pauseUpdates();
 			IStatus  status = validateEditEAR();
 			if( status.isOK() ){
 				status = super.execute(submon(monitor, 1), info);
 				if (!status.isOK())
 					return Status.CANCEL_STATUS;
 				updateEARDD(submon(monitor, 1));
 				updateModuleRuntimes(submon(monitor, 1));
 			}
 			return status;
 		} finally {
 			if (monitor != null) {
 				monitor.done();
 			}
 			J2EEComponentClasspathUpdater.getInstance().resumeUpdates();
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
 		
 		StructureEdit se = null;
 		try {
 			IVirtualComponent sourceComp = (IVirtualComponent) model.getProperty(ICreateReferenceComponentsDataModelProperties.SOURCE_COMPONENT);
 			final IEARModelProvider earModel = (IEARModelProvider)ModelProviderManager.getModelProvider(sourceComp.getProject());
 			
 			se = StructureEdit.getStructureEditForWrite(sourceComp.getProject());
 			if (earModel != null) {
 				List list = (List) model.getProperty(ICreateReferenceComponentsDataModelProperties.TARGET_COMPONENT_LIST);
 				final Map map = (Map) model.getProperty(IAddComponentToEnterpriseApplicationDataModelProperties.TARGET_COMPONENTS_TO_URI_MAP);
 				if (list != null && list.size() > 0) {
 					for (int i = 0; i < list.size(); i++) {
 						StructureEdit compse = null;
 						final IVirtualComponent wc = (IVirtualComponent) list.get(i);
 						WorkbenchComponent earwc = se.getComponent();
 						try {
 							compse = StructureEdit.getStructureEditForWrite(wc.getProject());
 							WorkbenchComponent refwc = compse.getComponent();
 							final ReferencedComponent ref = se.findReferencedComponent(earwc, refwc);
 							earModel.modify(new Runnable() {
 								public void run() {
 									final ICommonApplication application = (ICommonApplication)earModel.getModelObject();
 									if(application != null) {
 										ICommonModule mod = addModule(application, wc, (String) map.get(wc));
 										if (ref!=null)
 											ref.setDependentObject((EObject)mod);
 									}
 								}
 							}, null);
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
 		} catch (Exception e) {
 			Logger.getLogger().logError(e);
 		} finally {
 			if (se != null)
 				se.dispose();
 		}
 	}
 
 	protected ICommonModule createNewModule(IVirtualComponent wc, String name) {
 		ICommonModule newModule = null;
 		final IVirtualComponent ear = (IVirtualComponent) this.model.getProperty(ICreateReferenceComponentsDataModelProperties.SOURCE_COMPONENT);
 		final IProject earpj = ear.getProject();
 		boolean useNewModel = J2EEProjectUtilities.getJ2EEDDProjectVersion(earpj).equals(J2EEVersionConstants.VERSION_5_0_TEXT);
 		if (J2EEProjectUtilities.isDynamicWebProject(wc.getProject())) {
 			Properties props = wc.getMetaProperties();
 			String contextroot = ""; //$NON-NLS-1$
 			if ((props != null) && (props.containsKey(J2EEConstants.CONTEXTROOT)))
 				contextroot = props.getProperty(J2EEConstants.CONTEXTROOT);
 			if (useNewModel) {
 				Web web = ApplicationFactory.eINSTANCE.createWeb();
 				web.setContextRoot(contextroot);
 				web.setWebUri(name);
 				Module webModule = ApplicationFactory.eINSTANCE.createModule();
 				webModule.setWeb(web);
 				newModule = (ICommonModule)webModule;
 			}
 			else {
 				org.eclipse.jst.j2ee.application.WebModule webModule = org.eclipse.jst.j2ee.application.ApplicationFactory.eINSTANCE.createWebModule();
 				webModule.setUri(name);
 				webModule.setContextRoot(contextroot);
 				newModule = (ICommonModule)webModule;
 			}
 			return newModule;
 		} else if (J2EEProjectUtilities.isEJBProject(wc.getProject())) {
 			if (useNewModel) {
 				Module ejbModule = ApplicationFactory.eINSTANCE.createModule();
 				ejbModule.setEjb(name);
 				newModule = (ICommonModule)ejbModule;
 			}
 			else {
 				org.eclipse.jst.j2ee.application.EjbModule ejbModule = org.eclipse.jst.j2ee.application.ApplicationFactory.eINSTANCE.createEjbModule();
 				ejbModule.setUri(name);
 				newModule = (ICommonModule)ejbModule;
 			}			
 			return newModule;
 		} else if (J2EEProjectUtilities.isApplicationClientProject(wc.getProject())) {
 			if (useNewModel) {
 				Module appClientModule = ApplicationFactory.eINSTANCE.createModule();
 				appClientModule.setJava(name);
 				newModule = (ICommonModule)appClientModule;
 			}
 			else {
 				org.eclipse.jst.j2ee.application.JavaClientModule appClientModule = org.eclipse.jst.j2ee.application.ApplicationFactory.eINSTANCE.createJavaClientModule();
 				appClientModule.setUri(name);
 				newModule = (ICommonModule)appClientModule;
 			}			
 			return newModule;
 		} else if (J2EEProjectUtilities.isJCAProject(wc.getProject())) {
 			if (useNewModel) {
 				Module j2cModule = ApplicationFactory.eINSTANCE.createModule();
 				j2cModule.setConnector(name);
 				newModule = (ICommonModule)j2cModule;
 			}
 			else {
 				org.eclipse.jst.j2ee.application.ConnectorModule j2cModule = org.eclipse.jst.j2ee.application.ApplicationFactory.eINSTANCE.createConnectorModule();
 				j2cModule.setUri(name);
 				newModule = (ICommonModule)j2cModule;
 			}			
 			return newModule;
 		}
 		return null;
 	}
 
 	protected ICommonModule addModule(ICommonApplication application, IVirtualComponent wc, String name) {
 		ICommonApplication dd = application;
 		ICommonModule existingModule = dd.getFirstEARModule(name);
 		if (existingModule == null) {
 			existingModule = createNewModule(wc, name);
 			if (existingModule != null) {
 				existingModule.setUri(name);
 				dd.getEARModules().add(existingModule);
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
 
 	protected IStatus validateEditEAR() {
 		IStatus status = OK_STATUS;
 		IVirtualComponent sourceComp = (IVirtualComponent) model.getProperty(ICreateReferenceComponentsDataModelProperties.SOURCE_COMPONENT);
 		IProject project = sourceComp.getProject();
 
 		if (status.isOK()) {
 			
 			IModelProvider provider = ModelProviderManager.getModelProvider( project );
 			Object context = null;
 			if( UIContextDetermination.getCurrentContext() == UIContextDetermination.UI_CONTEXT ){
 				Display display = Display.getCurrent();
 				if(display != null )
 					context = display.getActiveShell();
 			}			
 			status = provider.validateEdit(null, context);
 		}
 		return status;
 	}
 	
 	protected IStatus validateEdit() {
		IStatus status = super.validateEdit();
		if (status.isOK()) {
			IValidateEditContext validator = (IValidateEditContext) UIContextDetermination.createInstance(IValidateEditContext.CLASS_KEY);
			IVirtualComponent sourceComp = (IVirtualComponent) model.getProperty(ICreateReferenceComponentsDataModelProperties.SOURCE_COMPONENT);
			IProject project = sourceComp.getProject();
			ArtifactEdit edit = null;
			try {
				edit = ComponentUtilities.getArtifactEditForWrite(sourceComp);
				status = validator.validateState((EditModel) edit.getAdapter(ArtifactEditModel.ADAPTER_TYPE));
			} finally {
				if (edit != null)
					edit.dispose();
			}
		}
		return status;
 	}
 }
