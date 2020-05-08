 /*******************************************************************************
  * Copyright (c) 2003, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.jst.j2ee.componentcore.util;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.jst.j2ee.application.Application;
 import org.eclipse.jst.j2ee.application.ApplicationFactory;
 import org.eclipse.jst.j2ee.application.ApplicationResource;
 import org.eclipse.jst.j2ee.application.Module;
 import org.eclipse.jst.j2ee.application.WebModule;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.Archive;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.CommonarchiveFactory;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.exception.OpenFailureException;
 import org.eclipse.jst.j2ee.componentcore.EnterpriseArtifactEdit;
 import org.eclipse.jst.j2ee.internal.J2EEConstants;
 import org.eclipse.jst.j2ee.internal.archive.operations.EARComponentLoadStrategyImpl;
 import org.eclipse.jst.j2ee.internal.common.XMLResource;
 import org.eclipse.jst.j2ee.internal.plugin.IJ2EEModuleConstants;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
 import org.eclipse.wst.common.componentcore.ArtifactEdit;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.ModuleCoreNature;
 import org.eclipse.wst.common.componentcore.UnresolveableURIException;
 import org.eclipse.wst.common.componentcore.internal.ArtifactEditModel;
 import org.eclipse.wst.common.componentcore.internal.ReferencedComponent;
 import org.eclipse.wst.common.componentcore.internal.StructureEdit;
 import org.eclipse.wst.common.componentcore.internal.WorkbenchComponent;
 import org.eclipse.wst.common.componentcore.internal.impl.ModuleURIUtil;
 import org.eclipse.wst.common.componentcore.internal.util.IArtifactEditFactory;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
 
 /**
  * <p>
  * EARArtifactEdit obtains an {@see org.eclipse.jst.j2ee.application.Application}&nbsp;metamodel.
  * The {@see org.eclipse.jst.j2ee.application.ApplicationResource}&nbsp; which stores the metamodel
  * is retrieved from the {@see org.eclipse.wst.common.modulecore.ArtifactEditModel}&nbsp;using a
  * cached constant (@see
  * org.eclipse.jst.j2ee.commonarchivecore.internal.helpers.ArchiveConstants#APPLICATION_DD_URI). The
  * defined methods extract data or manipulate the contents of the underlying resource.
  * </p>
  */
 
 public class EARArtifactEdit extends EnterpriseArtifactEdit implements IArtifactEditFactory{
 
 	public static final Class ADAPTER_TYPE = EARArtifactEdit.class;
 	
 	/**
 	 * 
 	 */
 	public EARArtifactEdit() {
 		super();
 	}
 
 	/**
 	 * @param aHandle
 	 * @param toAccessAsReadOnly
 	 * @throws IllegalArgumentException
 	 */
 	public EARArtifactEdit(IProject aProject, boolean toAccessAsReadOnly) throws IllegalArgumentException {
 		super(aProject, toAccessAsReadOnly);
 	}
 	
 	//This should be revereted to protected when 115924 is fixed
 	public EARArtifactEdit(IProject aProject, boolean toAccessAsReadOnly, boolean forCreate) throws IllegalArgumentException {
 		super(aProject, toAccessAsReadOnly, forCreate, J2EEProjectUtilities.ENTERPRISE_APPLICATION);
 	}
 
 	/**
 	 * <p>
 	 * Returns an instance facade to manage the underlying edit model for the given
 	 * {@see WorkbenchComponent}. Instances of ArtifactEdit that are returned through this method
 	 * must be {@see #dispose()}ed of when no longer in use.
 	 * </p>
 	 * <p>
 	 * Use to acquire an ArtifactEdit facade for a specific {@see WorkbenchComponent}&nbsp;that
 	 * will not be used for editing. Invocations of any save*() API on an instance returned from
 	 * this method will throw exceptions.
 	 * </p>
 	 * <p>
 	 * <b>The following method may return null. </b>
 	 * </p>
 	 * 
 	 * @param aModule
 	 *            A valid {@see WorkbenchComponent}&nbsp;with a handle that resolves to an
 	 *            accessible project in the workspace
 	 * @return An instance of ArtifactEdit that may only be used to read the underlying content
 	 *         model
 	 */
 	public static EARArtifactEdit getEARArtifactEditForRead(IProject aProject) {
 		EARArtifactEdit artifactEdit = null;
 		try {
 			if (isValidEARModule(ComponentCore.createComponent(aProject)))
 				artifactEdit = new EARArtifactEdit(aProject, true, false);
 		} catch (Exception iae) {
 			artifactEdit = null;
 		}
 		return artifactEdit;
 	}
 
 	/**
 	 * <p>
 	 * Returns an instance facade to manage the underlying edit model for the given
 	 * {@see WorkbenchComponent}. Instances of ArtifactEdit that are returned through this method
 	 * must be {@see #dispose()}ed of when no longer in use.
 	 * </p>
 	 * <p>
 	 * Use to acquire an ArtifactEdit facade for a specific {@see WorkbenchComponent}&nbsp;that
 	 * will be used for editing.
 	 * </p>
 	 * <p>
 	 * <b>The following method may return null. </b>
 	 * </p>
 	 * 
 	 * @param aModule
 	 *            A valid {@see WorkbenchComponent}&nbsp;with a handle that resolves to an
 	 *            accessible project in the workspace
 	 * @return An instance of ArtifactEdit that may be used to modify and persist changes to the
 	 *         underlying content model
 	 */
 	public static EARArtifactEdit getEARArtifactEditForWrite(IProject aProject) {
 		EARArtifactEdit artifactEdit = null;
 		try {
 			if (isValidEARModule(ComponentCore.createComponent(aProject)))
 				artifactEdit = new EARArtifactEdit(aProject, false, false);
 		} catch (Exception iae) {
 			artifactEdit = null;
 		}
 		return artifactEdit;
 	}
 
 	/**
 	 * <p>
 	 * Returns an instance facade to manage the underlying edit model for the given
 	 * {@see WorkbenchComponent}. Instances of EARArtifactEdit that are returned through this
 	 * method must be {@see #dispose()}ed of when no longer in use.
 	 * </p>
 	 * <p>
 	 * Use to acquire an EARArtifactEdit facade for a specific {@see WorkbenchComponent}&nbsp;that
 	 * will not be used for editing. Invocations of any save*() API on an instance returned from
 	 * this method will throw exceptions.
 	 * </p>
 	 * <p>
 	 * <b>This method may return null. </b>
 	 * </p>
 	 * <p>
 	 * Note: This method is for internal use only. Clients should not call this method.
 	 * </p>
 	 * 
 	 * @param aModule
 	 *            A valid {@see WorkbenchComponent}&nbsp;with a handle that resolves to an
 	 *            accessible project in the workspace
 	 * @return An instance of EARArtifactEdit that may only be used to read the underlying content
 	 *         model
 	 * @throws UnresolveableURIException
 	 *             could not resolve uri.
 	 */
 	public static EARArtifactEdit getEARArtifactEditForRead(IVirtualComponent aModule) {
 		if (aModule == null)
 			return null;
 		return getEARArtifactEditForRead(aModule.getProject());
 	}
 
 
 	/**
 	 * <p>
 	 * Returns an instance facade to manage the underlying edit model for the given
 	 * {@see WorkbenchComponent}. Instances of WebArtifactEdit that are returned through this
 	 * method must be {@see #dispose()}ed of when no longer in use.
 	 * </p>
 	 * <p>
 	 * Use to acquire an EARArtifactEdit facade for a specific {@see WorkbenchComponent}&nbsp;that
 	 * will be used for editing.
 	 * </p>
 	 * <p>
 	 * <b>This method may return null. </b>
 	 * </p>
 	 * <p>
 	 * Note: This method is for internal use only. Clients should not call this method.
 	 * </p>
 	 * 
 	 * @param aModule
 	 *            A valid {@see WorkbenchComponent}&nbsp;with a handle that resolves to an
 	 *            accessible project in the workspace
 	 * @return An instance of EARArtifactEdit that may be used to modify and persist changes to the
 	 *         underlying content model
 	 */
 	public static EARArtifactEdit getEARArtifactEditForWrite(IVirtualComponent aModule) {
 		if (aModule == null)
 			return null;
 		return getEARArtifactEditForWrite(aModule.getProject());
 	}
 
 	/**
 	 * @param module
 	 *            A {@see WorkbenchComponent}
 	 * @return True if the supplied module
 	 *         {@see ArtifactEdit#isValidEditableModule(WorkbenchComponent)}and the moduleTypeId is
 	 *         a JST module
 	 */
 	public static boolean isValidEARModule(IVirtualComponent aModule) throws UnresolveableURIException {
 		if (!isValidEditableModule(aModule))
 			return false;
 		return J2EEProjectUtilities.isEARProject(aModule.getProject());
 	}
 
 	/**
 	 * <p>
 	 * Creates an instance facade for the given {@see ArtifactEditModel}.
 	 * </p>
 	 * 
 	 * @param anArtifactEditModel
 	 */
 	public EARArtifactEdit(ArtifactEditModel model) {
 		super(model);
 	}
 
 	/**
 	 * <p>
 	 * Creates an instance facade for the given {@see ArtifactEditModel}
 	 * </p>
 	 * <p>
 	 * Note: This method is for internal use only. Clients should not call this method.
 	 * </p>
 	 * 
 	 * @param aNature
 	 *            A non-null {@see ModuleCoreNature}for an accessible project
 	 * @param aModule
 	 *            A non-null {@see WorkbenchComponent}pointing to a module from the given
 	 *            {@see ModuleCoreNature}
 	 */
 
 
 	protected EARArtifactEdit(ModuleCoreNature aNature, IVirtualComponent aModule, boolean toAccessAsReadOnly) {
 		super(aNature, aModule, toAccessAsReadOnly);
 	}
 
 	/**
 	 * <p>
 	 * Retrieves J2EE version information from ApplicationResource.
 	 * </p>
 	 * 
 	 * @return an integer representation of a J2EE Spec version
 	 * 
 	 */
 	public int getJ2EEVersion() {
 		return getApplicationXmiResource().getJ2EEVersionID();
 	}
 
 	/**
 	 * 
 	 * @return ApplicationResource from (@link getDeploymentDescriptorResource())
 	 * 
 	 */
 
 	public ApplicationResource getApplicationXmiResource() {
 		return (ApplicationResource) getDeploymentDescriptorResource();
 	}
 
 	/**
 	 * <p>
 	 * Obtains the Application {@see Application}root object from the {@see ApplicationResource},
 	 * the root object contains all other resource defined objects.
 	 * </p>
 	 * 
 	 * @return Application
 	 * 
 	 */
 
 	public Application getApplication() {
 		return (Application) getDeploymentDescriptorRoot();
 	}
 
 	/**
 	 * <p>
 	 * Retrieves the resource from the {@see ArtifactEditModel}
 	 * </p>
 	 * 
 	 * @return Resource
 	 * 
 	 */
 
 	public Resource getDeploymentDescriptorResource() {
 		return getArtifactEditModel().getResource(J2EEConstants.APPLICATION_DD_URI_OBJ);
 	}
 
 
 	/**
 	 * <p>
 	 * Creates a deployment descriptor root object (Application) and populates with data. Adds the
 	 * root object to the deployment descriptor resource.
 	 * </p>
 	 * 
 	 * <p>
 	 * 
 	 * @param aModule
 	 *            A non-null pointing to a {@see XMLResource}
 	 * @param version
 	 *            Version to be set on resource....if null default is taken
 	 * 
 	 * Note: This method is typically used for JUNIT - move?
 	 * </p>
 	 */
 	protected void addApplicationIfNecessary(XMLResource aResource) {
 		if (aResource != null) {
 			if (aResource.getContents() == null || aResource.getContents().isEmpty()) {
 				Application newApp = ApplicationFactory.eINSTANCE.createApplication();
 				aResource.getContents().add(newApp);
 			}
 			Application application = (Application) aResource.getContents().get(0);
 			URI moduleURI = getArtifactEditModel().getModuleURI();
 			try {
 				application.setDisplayName(StructureEdit.getDeployedName(moduleURI));
 			} catch (UnresolveableURIException e) {
 				//Ignore
 			}
 			aResource.setID(application, J2EEConstants.APPL_ID);
 			// TODO add more mandatory elements
 		}
 	}
 
 	/**
 	 * Checks if the uri mapping already exists.
 	 * 
 	 * @param String
 	 *            currentURI - The current uri of the module.
 	 * @return boolean
 	 */
 	public boolean uriExists(String currentURI) {
 		if (currentURI != null) {
 			IVirtualComponent comp = ComponentCore.createComponent(getProject());
 			if (comp == null)
 				return false;
 			IVirtualReference[] refComponents = comp.getReferences();
 			if (refComponents.length == 0)
 				return false;
 			for (int i = 0; i < refComponents.length; i++) {
 				if (refComponents[i].getRuntimePath().equals(currentURI))
 					return true;
 			}
 		} // if
 		return false;
 	} // uriExists
 
 
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.j2ee.internal.modulecore.util.EnterpriseArtifactEdit#createModelRoot()
 	 */
 	public EObject createModelRoot() {
 		return createModelRoot(getJ2EEVersion());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.j2ee.internal.modulecore.util.EnterpriseArtifactEdit#createModelRoot(java.lang.Integer)
 	 */
 	public EObject createModelRoot(int version) {
 		ApplicationResource res = (ApplicationResource) getDeploymentDescriptorResource();
 		res.setModuleVersionID(version);
 		addApplicationIfNecessary(res);
 		return ((ApplicationResource) getDeploymentDescriptorResource()).getRootObject();
 	}
 
 	/**
 	 * This method will return the list of IVirtualReferences for all of the utility modules
 	 * contained in the EAR application
 	 * 
 	 * @return - an array of IVirtualReferences for utility modules in the EAR
 	 */
 	public IVirtualReference[] getUtilityModuleReferences() {
 		List utilityModuleTypes = new ArrayList();
 		utilityModuleTypes.add(J2EEProjectUtilities.UTILITY);
 		return getComponentReferences(utilityModuleTypes);
 	}
 	
 	public String getModuleURI(IVirtualComponent moduleComp) {
 		
 		StructureEdit core = null;
 		try {
 			core = StructureEdit.getStructureEditForRead(getProject());
 			WorkbenchComponent component = core.getComponent();
 			List referencedComponents = component.getReferencedComponents();
 			
 			for (int i=0; i<referencedComponents.size(); i++) {
 				ReferencedComponent ref = (ReferencedComponent) referencedComponents.get(i);
 				if( !moduleComp.isBinary()) {
 					if (ref.getHandle().equals(ModuleURIUtil.fullyQualifyURI(moduleComp.getProject()))) {
 						if (ref.getDependentObject()!=null)
 							return ((Module)ref.getDependentObject()).getUri();
 						//If dependent object is not set, assume compname is module name + proper extension
 						if (J2EEProjectUtilities.isDynamicWebProject(moduleComp.getProject()) || J2EEProjectUtilities.isStaticWebProject(moduleComp.getProject()))
 							return moduleComp.getName()+IJ2EEModuleConstants.WAR_EXT;
 						if (J2EEProjectUtilities.isJCAProject(moduleComp.getProject()))
 							return moduleComp.getName()+IJ2EEModuleConstants.RAR_EXT;
 						if (J2EEProjectUtilities.isUtilityProject(moduleComp.getProject())){
 							String uri =  ref.getArchiveName();
 							if( uri == null || uri.length() < 0 ){
 								uri = moduleComp.getName()+IJ2EEModuleConstants.JAR_EXT;
 							}else{
 								String prefix = ref.getRuntimePath().makeRelative().toString();
 								if(prefix.length() > 0){
 									uri = prefix + "/" + uri; //$NON-NLS-1$
 								}
 							}
 							return uri;
 						}
 						return moduleComp.getName()+IJ2EEModuleConstants.JAR_EXT;
 					}
 						
 				} 
 				else if (moduleComp.isBinary()) {
					if (ref.getHandle().equals(ModuleURIUtil.archiveComponentfullyQualifyURI(moduleComp.getName())));
 						return ((Module)ref.getDependentObject()).getUri();
 				}	
 			}
 		} finally {
 			if (core != null)
 				core.dispose();
 		}
 		return null;
 	}
 	
 	public IVirtualComponent getModuleByManifestURI(final String uri){
 		StructureEdit core = null;
 		try {
 			core = StructureEdit.getStructureEditForRead(getProject());
 			WorkbenchComponent component = core.getComponent();
 			List referencedComponents = component.getReferencedComponents();
 			
 			for (int i=0; i<referencedComponents.size(); i++) {
 				ReferencedComponent ref = (ReferencedComponent) referencedComponents.get(i);
 				if(ref.getDependentObject() != null){
 					Module module = (Module)ref.getDependentObject();
 					if(uri.equals(module.getUri())){
 						String componentName;
 						try {
 							componentName = ModuleURIUtil.getArchiveName(ref.getHandle());
 							return getModule(componentName);
 						} catch (UnresolveableURIException e) {
 							Logger.getLogger().logError(e);
 						}
 					}
 				}
 			}
 		} finally {
 			if (core != null)
 				core.dispose();
 		}
 		return null;
 	}
 	
     /**
      * This method will return the an IVirtualComponent for the given module name.  The method take either moduleName or 
      * moduleName + ".module_extension" (module_extension = ".jar" || ".war" || ".rar") which allows users to get a IVirtualComponent 
      * for a given entry in an application.xml
      * 
      * @return - a IVirtualComponent for module name
      */
 	public IVirtualComponent getModule (String moduleName) {
         if(moduleName == null)
             return null;
         if(moduleName.endsWith(IJ2EEModuleConstants.JAR_EXT) || moduleName.endsWith(IJ2EEModuleConstants.WAR_EXT) || moduleName.endsWith(IJ2EEModuleConstants.RAR_EXT))
             moduleName = moduleName.substring(0, (moduleName.length()- IJ2EEModuleConstants.JAR_EXT.length()));
         IVirtualReference[] references = getComponentReferences();
 		for(int i = 0; i < references.length; i++) {
 			IVirtualComponent component = references[i].getReferencedComponent();
 			if(component.getName().equals(moduleName)) {
 				return component;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * This method will return the list of IVirtualReferences for the J2EE module components
 	 * contained in this EAR application.
 	 * 
 	 * @return - an array of IVirtualReferences for J2EE modules in the EAR
 	 */
 	public IVirtualReference[] getJ2EEModuleReferences() {
 		List j2eeTypes = new ArrayList();
 		j2eeTypes.add(J2EEProjectUtilities.APPLICATION_CLIENT);
 		j2eeTypes.add(J2EEProjectUtilities.JCA);
 		j2eeTypes.add(J2EEProjectUtilities.EJB);
 		j2eeTypes.add(J2EEProjectUtilities.DYNAMIC_WEB);
 		return getComponentReferences(j2eeTypes);
 	}
 	
 	/**
 	 * This method will return the list of IVirtualReferences for all of the components
 	 * contained in this EAR application.
 	 * 
 	 * @return - an array of IVirtualReferences for components in the EAR
 	 */
 	public IVirtualReference[] getComponentReferences() {
 		return getComponentReferences(Collections.EMPTY_LIST);
 	}
 	/**
 	 * This method will return the IVirtualReference to the component of the given name
 	 * 
 	 * @return - IVirtualReference or null if not found
 	 */
 	public IVirtualReference getComponentReference(String componentName) {
 		IVirtualReference[] refs = getComponentReferences(Collections.EMPTY_LIST);
 		for (int i = 0; i < refs.length; i++) {
 			IVirtualReference reference = refs[i];
 			if (reference.getReferencedComponent().getName().equals(componentName))
 				return reference;
 			
 		}
 		return null;
 	}
 	
 	private IVirtualReference[] getComponentReferences(List componentTypes) {
 		List components = new ArrayList();
 		IVirtualComponent earComponent = getComponent();
 		if (earComponent != null && J2EEProjectUtilities.isEARProject(earComponent.getProject())) {
 			IVirtualReference[] refComponents = earComponent.getReferences();
 			for (int i = 0; i < refComponents.length; i++) {
 				IVirtualComponent module = refComponents[i].getReferencedComponent();
 				if (module == null || module.getProject() == null || !module.getProject().isAccessible())
 					continue;
 				//if component types passed in is null then return all components
 				if (componentTypes == null || componentTypes.size()==0)
 					components.add(refComponents[i]);
 				else {
 					 if (componentTypes.contains(J2EEProjectUtilities.getJ2EEProjectType(module.getProject()))) {
 							components.add(refComponents[i]);
 						}
 					}
 				}
 			}
 		return (IVirtualReference[]) components.toArray(new IVirtualReference[components.size()]);
 	}
 
 	public ArtifactEdit createArtifactEditForRead(IVirtualComponent aComponent) {
 		
 		return getEARArtifactEditForRead(aComponent);
 	}
 
 	public ArtifactEdit createArtifactEditForWrite(IVirtualComponent aComponent) {
 		
 		return getEARArtifactEditForWrite(aComponent);
 	}
 	
 	public Archive asArchive(boolean includeSource) throws OpenFailureException {
 		EARComponentLoadStrategyImpl loader = new EARComponentLoadStrategyImpl(getComponent());
 		loader.setExportSource(includeSource);
 		String uri = ModuleURIUtil.getHandleString(getComponent());
 		return CommonarchiveFactory.eINSTANCE.openEARFile(loader, uri);
 	}
 	
 	public static void createDeploymentDescriptor(IProject project, int version) {
 		EARArtifactEdit earEdit = new EARArtifactEdit(project,false,true);
 		try {
 			earEdit.createModelRoot(version);
 			earEdit.save(null);
 		} finally {
 			earEdit.dispose();
 		} 
 	}
 	
 	/**
 	 * This method will return the context root in this application for the passed in web project.
 	 * 
 	 * @param webProject
 	 * @return contextRoot String
 	 */
 	public String getWebContextRoot(IProject webProject) {
 		if (webProject == null || !J2EEProjectUtilities.isDynamicWebProject(webProject))
 			return null;
 		IVirtualComponent webComp = ComponentCore.createComponent(webProject);
 		String webModuleURI = getModuleURI(webComp);
 		if (webModuleURI != null) {
 			WebModule webModule = (WebModule) getApplication().getModule(webModuleURI,null);
 			if (webModule != null)
 				return webModule.getContextRoot();
 		}
 		return null;
 	}
 	
 	/**
 	 * This method will set the context root on the application for the passed in contextRoot.  This
 	 * must be called in a write artifact edit and be saved for changes to be saved.
 	 * 
 	 * @param webProject
 	 * @param aContextRoot
 	 */
 	public void setWebContextRoot(IProject webProject, String aContextRoot) {
 		if (webProject == null || !J2EEProjectUtilities.isDynamicWebProject(webProject))
 			return;
 		IVirtualComponent webComp = ComponentCore.createComponent(webProject);
 		String webModuleURI = getModuleURI(webComp);
 		if (webModuleURI != null) {
 			WebModule webModule = (WebModule) getApplication().getModule(webModuleURI,null);
 			if (webModule != null)
 				webModule.setContextRoot(aContextRoot);
 		}
 	}
 }
