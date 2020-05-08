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
 package org.eclipse.jst.j2ee.internal.deployables;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.jst.j2ee.application.Application;
 import org.eclipse.jst.j2ee.application.Module;
 import org.eclipse.jst.j2ee.client.ApplicationClient;
 import org.eclipse.jst.j2ee.componentcore.util.EARArtifactEdit;
 import org.eclipse.jst.j2ee.ejb.EJBJar;
 import org.eclipse.jst.j2ee.internal.J2EEVersionConstants;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
 import org.eclipse.jst.j2ee.jca.Connector;
 import org.eclipse.jst.j2ee.webapplication.WebApp;
 import org.eclipse.wst.common.componentcore.ArtifactEdit;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.ModuleCoreNature;
 import org.eclipse.wst.common.componentcore.internal.StructureEdit;
 import org.eclipse.wst.common.componentcore.internal.util.ComponentUtilities;
 import org.eclipse.wst.common.componentcore.internal.util.IModuleConstants;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
 import org.eclipse.wst.server.core.IModule;
 import org.eclipse.wst.server.core.model.ModuleDelegate;
 import org.eclipse.wst.server.core.util.ProjectModuleFactoryDelegate;
 
 /**
  * J2EE module factory.
  */
 public class J2EEDeployableFactory extends ProjectModuleFactoryDelegate {
 	protected Map moduleDelegates = new HashMap(5);
 	
 	public static final String ID = "org.eclipse.jst.j2ee.server"; //$NON-NLS-1$
 
 	public J2EEDeployableFactory() {
 		super();
 	}
 
 	protected IModule[] createModules(IProject project) {
 		try {
 			ModuleCoreNature nature = (ModuleCoreNature) project.getNature(IModuleConstants.MODULE_NATURE_ID);
 			if (nature != null)
 				return createModules(nature);
 		} catch (CoreException e) {
 			Logger.getLogger().write(e);
 		}
 		return null;
 	}
 
 	protected IModule[] createModules(ModuleCoreNature nature) {
 		IProject project = nature.getProject();
 		try {
 			IVirtualComponent comp = ComponentCore.createComponent(project);
 			return createModuleDelegates(comp);
 		} catch (Exception e) {
 			Logger.getLogger().write(e);
 		}
 		return null;
 	}
 
 	public ModuleDelegate getModuleDelegate(IModule module) {
 		return (ModuleDelegate) moduleDelegates.get(module);
 	}
 
 	protected IModule[] createModuleDelegates(IVirtualComponent component) {
 		List projectModules = new ArrayList();
 		try {
 			IModule module = null;
 			String type = J2EEProjectUtilities.getJ2EEProjectType(component.getProject());
 			if (type != null && !type.equals("")) {
 				String version = J2EEProjectUtilities.getJ2EEProjectVersion(component.getProject());
 				module = createModule(component.getDeployedName(), component.getDeployedName(), type, version, component.getProject());
 				J2EEFlexProjDeployable moduleDelegate = new J2EEFlexProjDeployable(component.getProject(), component);
 				moduleDelegates.put(module, moduleDelegate);
 				projectModules.add(module);
 			}
 			// Check to add any binary modules
 			if (J2EEProjectUtilities.ENTERPRISE_APPLICATION.equals(type))
 				projectModules.addAll(Arrays.asList(createBinaryModules(component)));
 		} catch (Exception e) {
 			Logger.getLogger().write(e);
 		}
 		return (IModule[]) projectModules.toArray(new IModule[projectModules.size()]);
 	}
 	
 	protected IModule[] createBinaryModules(IVirtualComponent component) {
 		List projectModules = new ArrayList();
 		EARArtifactEdit earEdit = null;
 		try {
			Application app = null;
 			IVirtualReference[] references = component.getReferences();
 			for (int i=0; i<references.length; i++) {
 				IVirtualComponent moduleComponent = references[i].getReferencedComponent();
 				// Is referenced component a J2EE binary module archive or binary utility project?
 				if (moduleComponent.isBinary()) {
					// create an ear edit, app only if the module is binary prevents exceptions when there
					// is no deployment descriptor in many cases see bug 174711
					if(earEdit == null){
						earEdit = EARArtifactEdit.getEARArtifactEditForRead(component.getProject());
						app = earEdit.getApplication();
					}
 					// Check if module URI exists on EAR DD for binary j2ee archive
 					Module j2eeModule = app.getFirstModule(references[i].getArchiveName());
 					// If it is not a j2ee module and the component project is the ear, it is just an archive
 					// and we can ignore as it will be processed by the EAR deployable.members() method
 					if (j2eeModule == null && (moduleComponent.getProject() == component.getProject()))
 						continue;
 					
 					String moduleVersion = null;
 					String moduleType = null;
 					// Handle the binary J2EE module case
 					if(j2eeModule != null){
 						ArtifactEdit moduleEdit = null;
 						try {
 							if (j2eeModule.isEjbModule()) {
 								moduleEdit = ComponentUtilities.getArtifactEditForRead(moduleComponent, J2EEProjectUtilities.EJB);
 								EJBJar ejbJar = (EJBJar) moduleEdit.getContentModelRoot();
 								moduleType = J2EEProjectUtilities.EJB;
 								moduleVersion = ejbJar.getVersion();
 							}
 							else if (j2eeModule.isWebModule()) {
 								moduleEdit = ComponentUtilities.getArtifactEditForRead(moduleComponent, J2EEProjectUtilities.DYNAMIC_WEB);
 								WebApp webApp = (WebApp) moduleEdit.getContentModelRoot();
 								moduleType = J2EEProjectUtilities.DYNAMIC_WEB;
 								moduleVersion = webApp.getVersion();
 							}
 							else if (j2eeModule.isConnectorModule()) {
 								moduleEdit = ComponentUtilities.getArtifactEditForRead(moduleComponent, J2EEProjectUtilities.JCA);
 								Connector connector = (Connector) moduleEdit.getContentModelRoot();
 								moduleType = J2EEProjectUtilities.JCA;
 								moduleVersion = connector.getVersion();
 							}
 							else if (j2eeModule.isJavaModule()) {
 								moduleEdit = ComponentUtilities.getArtifactEditForRead(moduleComponent, J2EEProjectUtilities.APPLICATION_CLIENT);
 								ApplicationClient appClient = (ApplicationClient) moduleEdit.getContentModelRoot();
 								moduleType = J2EEProjectUtilities.APPLICATION_CLIENT;
 								moduleVersion = appClient.getVersion();
 							}
 						} finally {
 							if (moduleEdit!=null)
 								moduleEdit.dispose();
 						}
 					} else { // Handle the binary utility component outside the EAR case.
 						moduleVersion = J2EEProjectUtilities.UTILITY;
 						moduleType = J2EEVersionConstants.VERSION_1_0_TEXT;
 					}
 					
 					IModule nestedModule = createModule(moduleComponent.getDeployedName(), moduleComponent.getDeployedName(), moduleType, moduleVersion, moduleComponent.getProject());
 					if (nestedModule!=null) {
 						J2EEFlexProjDeployable moduleDelegate = new J2EEFlexProjDeployable(moduleComponent.getProject(), moduleComponent);
 						moduleDelegates.put(nestedModule, moduleDelegate);
 						projectModules.add(nestedModule);
 						moduleDelegate.getURI(nestedModule);
 					}
 				}
 			}
 		} finally {
 			if (earEdit!=null)
 				earEdit.dispose();
 		}
 		return (IModule[]) projectModules.toArray(new IModule[projectModules.size()]);
 	}
 
 	/**
 	 * Returns the list of resources that the module should listen to
 	 * for state changes. The paths should be project relative paths.
 	 * Subclasses can override this method to provide the paths.
 	 *
 	 * @return a possibly empty array of paths
 	 */
 	protected IPath[] getListenerPaths() {
 		return new IPath[] {
 			new Path(".project"), // nature
 			new Path(StructureEdit.MODULE_META_FILE_NAME), // component
 			new Path(".settings/org.eclipse.wst.common.project.facet.core.xml") // facets
 		};
 	}
 
 	protected void clearCache() {
 		moduleDelegates = new HashMap(5);
 	}
 }
