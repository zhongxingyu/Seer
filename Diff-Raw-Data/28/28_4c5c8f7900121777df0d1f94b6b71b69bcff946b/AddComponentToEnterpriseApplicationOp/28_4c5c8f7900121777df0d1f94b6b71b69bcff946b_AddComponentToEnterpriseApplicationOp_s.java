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
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
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
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.jst.j2ee.application.WebModule;
 import org.eclipse.jst.j2ee.componentcore.J2EEModuleVirtualArchiveComponent;
 import org.eclipse.jst.j2ee.componentcore.util.EARVirtualComponent;
 import org.eclipse.jst.j2ee.internal.ICommonEMFModule;
 import org.eclipse.jst.j2ee.internal.J2EEVersionConstants;
 import org.eclipse.jst.j2ee.internal.common.classpath.J2EEComponentClasspathUpdater;
 import org.eclipse.jst.j2ee.internal.componentcore.JavaEEBinaryComponentHelper;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEPlugin;
 import org.eclipse.jst.j2ee.model.IEARModelProvider;
 import org.eclipse.jst.j2ee.model.IModelProvider;
 import org.eclipse.jst.j2ee.model.ModelProviderManager;
 import org.eclipse.jst.j2ee.project.EarUtilities;
 import org.eclipse.jst.j2ee.project.JavaEEProjectUtilities;
 import org.eclipse.jst.j2ee.project.facet.EarFacetRuntimeHandler;
 import org.eclipse.jst.javaee.application.ApplicationFactory;
 import org.eclipse.jst.javaee.application.Module;
 import org.eclipse.jst.javaee.application.Web;
 import org.eclipse.jst.jee.application.ICommonApplication;
 import org.eclipse.jst.jee.application.ICommonModule;
 import org.eclipse.wst.common.componentcore.datamodel.properties.ICreateReferenceComponentsDataModelProperties;
 import org.eclipse.wst.common.componentcore.internal.ReferencedComponent;
 import org.eclipse.wst.common.componentcore.internal.StructureEdit;
 import org.eclipse.wst.common.componentcore.internal.WorkbenchComponent;
 import org.eclipse.wst.common.componentcore.internal.operation.CreateReferenceComponentsOp;
 import org.eclipse.wst.common.componentcore.internal.util.ComponentUtilities;
import org.eclipse.wst.common.componentcore.internal.util.IModuleConstants;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 import org.eclipse.wst.common.internal.emf.resource.CompatibilityXMIResource;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
 
 public class AddComponentToEnterpriseApplicationOp extends CreateReferenceComponentsOp {
 	public static final String metaInfFolderDeployPath = "/"; //$NON-NLS-1$
 
 	public AddComponentToEnterpriseApplicationOp(IDataModel model) {
 		super(model);
 	}
 
 	@Override
 	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
 		if (monitor != null) {
 			monitor.beginTask("", 4); //$NON-NLS-1$
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
 				moduleClasspathForceUpdate(submon(monitor, 1));
 			}
 			return status;
 		} finally {
 			if (monitor != null) {
 				monitor.done();
 			}
 			J2EEComponentClasspathUpdater.getInstance().resumeUpdates();
 		}
 	}
 
	@Override
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
 			final IVirtualComponent ear = (IVirtualComponent) this.model.getProperty(ICreateReferenceComponentsDataModelProperties.SOURCE_COMPONENT);
 			final IProject earpj = ear.getProject();
 			se = StructureEdit.getStructureEditForWrite(sourceComp.getProject());
 				
 			if (earModel != null) {
 				List list = (List) model.getProperty(ICreateReferenceComponentsDataModelProperties.TARGET_COMPONENT_LIST);
 				final Map map = (Map) model.getProperty(IAddComponentToEnterpriseApplicationDataModelProperties.TARGET_COMPONENTS_TO_URI_MAP);
 				if (list != null && list.size() > 0) {
 					for (int i = 0; i < list.size(); i++) {
 						
 						final IVirtualComponent wc = (IVirtualComponent) list.get(i);
 						boolean linkedToEAR = true;
 						try{
 							if(wc.isBinary()){
 								linkedToEAR = ((J2EEModuleVirtualArchiveComponent)wc).isLinkedToEAR();
 								((J2EEModuleVirtualArchiveComponent)wc).setLinkedToEAR(false);
 							}
 							WorkbenchComponent earwc = se.getComponent();
 							StructureEdit compse = null;
 							try {
 								compse = StructureEdit.getStructureEditForWrite(wc.getProject());
 								WorkbenchComponent refwc = compse.getComponent();
 								final ReferencedComponent ref = se.findReferencedComponent(earwc, refwc);
 								earModel.modify(new Runnable() {
 									public void run() {
 										final ICommonApplication application = (ICommonApplication)earModel.getModelObject();
 										if(application != null) {
 											ICommonModule mod = addModule(application, wc, (String) map.get(wc));
 											if(mod == null){ //utility project
 												return;
 											}
 											if (ref!=null)
 												ref.setDependentObject((EObject)mod);
 											if (JavaEEProjectUtilities.isStaticWebProject(wc.getProject())
 													|| JavaEEProjectUtilities.isDynamicWebComponent(wc)) {
 												updateContextRoot(earpj, wc, mod);
 											}
 											Resource theResource = ((EObject)mod).eResource();
 											if (theResource != null)
 											{
 												String frag = null;
 												if (theResource instanceof CompatibilityXMIResource)
 													frag = theResource.getURIFragment((EObject)mod);
 												((ICommonEMFModule)mod).setId(frag);
 											}
 										}
 									}						
 								}, null);
 							} finally {
 								if (compse != null) {
 									compse.saveIfNecessary(monitor);
 									compse.dispose();
 								}
 							}
 						} finally {
 							if(wc.isBinary()){
 								((J2EEModuleVirtualArchiveComponent)wc).setLinkedToEAR(linkedToEAR);
 							}
 						}
 					}
 				}
 			}
 			se.saveIfNecessary(monitor);
 		} catch (Exception e) {
 			J2EEPlugin.logError(e);
 		} finally {
 			if (se != null)
 				se.dispose();
 		}
 	}
 
 	private void updateContextRoot(final IProject earpj, final IVirtualComponent wc,
 			ICommonModule mod) {
 		String earDDVersion = JavaEEProjectUtilities.getJ2EEDDProjectVersion(earpj);
 		boolean useNewModel = earDDVersion.equals(J2EEVersionConstants.VERSION_5_0_TEXT) || earDDVersion.equals(J2EEVersionConstants.VERSION_6_0_TEXT);
 		String contextroot = ComponentUtilities.getServerContextRoot(wc.getProject());
 		if (contextroot == null) {
 			contextroot = wc.getProject().getName();
 		}
 		if (useNewModel) {
 			if (mod instanceof Module) {
 				// safety check
 				Web web = ((Module) mod).getWeb();
 				web.setContextRoot(contextroot);
 			}
 		}
 		else {
 			if (JavaEEProjectUtilities.isStaticWebProject(wc.getProject())
 					|| JavaEEProjectUtilities.isDynamicWebComponent(wc)) {
 				if (mod instanceof WebModule) {
 					((WebModule) mod).setContextRoot(contextroot);
 				}
 			}
 		}
 	}
 	
 	protected ICommonModule createNewModule(IVirtualComponent wc, String name) {
 		ICommonModule newModule = null;
 		final IVirtualComponent ear = (IVirtualComponent) this.model.getProperty(ICreateReferenceComponentsDataModelProperties.SOURCE_COMPONENT);
 		final IProject earpj = ear.getProject();
 		
 		String earDDVersion = JavaEEProjectUtilities.getJ2EEDDProjectVersion(earpj);
 		boolean useNewModel = earDDVersion.equals(J2EEVersionConstants.VERSION_5_0_TEXT) || earDDVersion.equals(J2EEVersionConstants.VERSION_6_0_TEXT);
 		if (wc.isBinary()){
 			//[Bug 299549] open and cache archive using the DISCRIMINATE_MAIN_CLASS
 			//option before attempting the JavaEEQuickPeek
 			JavaEEBinaryComponentHelper.openArchive(wc, useNewModel);
 		}
 		
 		//[Bug 238264] need to use component to determine type of project in-case component is binary
 		if (JavaEEProjectUtilities.isDynamicWebComponent(wc)) {
 			if (useNewModel) {
 				Web web = ApplicationFactory.eINSTANCE.createWeb();
 				web.setWebUri(name);
 				Module webModule = ApplicationFactory.eINSTANCE.createModule();
 				webModule.setWeb(web);
 				newModule = (ICommonModule)webModule;
 			}
 			else {
 				org.eclipse.jst.j2ee.application.WebModule webModule = org.eclipse.jst.j2ee.application.ApplicationFactory.eINSTANCE.createWebModule();
 				webModule.setUri(name);
 				newModule = (ICommonModule)webModule;
 			}
 			updateContextRoot(earpj, wc, newModule);
 			return newModule;
 		} else if (JavaEEProjectUtilities.isEJBComponent(wc)) {
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
 		} else if (JavaEEProjectUtilities.isApplicationClientComponent(wc)) {
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
 		} else if (JavaEEProjectUtilities.isJCAComponent(wc)) {
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
 			monitor.beginTask("", 10); //$NON-NLS-1$
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
 			J2EEPlugin.logError(e);
 		} finally {
 			if (monitor != null) {
 				monitor.done();
 			}
 		}
 	}
 
 	private void moduleClasspathForceUpdate(IProgressMonitor monitor) {
 		if (monitor != null) {
 			monitor.beginTask("", 1); //$NON-NLS-1$
 		}
 		
 		try {
 			IVirtualComponent component = (IVirtualComponent) this.model.getProperty(ICreateReferenceComponentsDataModelProperties.SOURCE_COMPONENT);
 			// If this is a caching component, this will return the underlying; otherwise self
 			EARVirtualComponent ear = (EARVirtualComponent)component.getComponent();
 			if (JavaEEProjectUtilities.isJEEComponent(ear, JavaEEProjectUtilities.DD_VERSION)){
 				String deployPath = model.getStringProperty(IAddComponentToEnterpriseApplicationDataModelProperties.TARGET_COMPONENTS_DEPLOY_PATH);
 				String libDir = EarUtilities.getEARLibDir(ear);
 				
 				if(libDir != null && libDir.equals(deployPath)) {
 					// the component added is in the library directory of an EAR 5+ project
 					// we should trigger force update of the classpath of all module in the EAR
 					IVirtualReference[] refs = ear.getReferences();
 					Collection<IProject> projects = new HashSet<IProject>();
 					for (IVirtualReference ref : refs) {
 						projects.add(ref.getReferencedComponent().getProject());
 					}
 					J2EEComponentClasspathUpdater.getInstance().forceUpdate(projects);
 				}
 			}
 		} finally {
 			if (monitor != null) {
 				monitor.done();
 			}
 		}
 	}
 
 	private static IProgressMonitor submon(final IProgressMonitor parent, final int ticks) {
 		return (parent == null ? null : new SubProgressMonitor(parent, ticks));
 	}
 
 	@Override
 	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
 		return null;
 	}
 
 	@Override
 	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
 		return null;
 	}
 
 	protected IStatus validateEditEAR() {
 		IStatus status = OK_STATUS;
 		IVirtualComponent sourceComp = (IVirtualComponent) model.getProperty(ICreateReferenceComponentsDataModelProperties.SOURCE_COMPONENT);
 		IProject project = sourceComp.getProject();
 		IModelProvider provider = ModelProviderManager.getModelProvider( project );
 		status = provider.validateEdit(null, null);
 		return status;
 	}
 	
 	@Override
 	protected IStatus validateEdit() {
 		return validateEditEAR();
 	}
 }
