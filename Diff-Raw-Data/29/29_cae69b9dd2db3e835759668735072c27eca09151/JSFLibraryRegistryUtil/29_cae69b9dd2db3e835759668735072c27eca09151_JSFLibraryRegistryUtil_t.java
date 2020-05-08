 /*******************************************************************************
  * Copyright (c) 2006 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:  Oracle
  *******************************************************************************/
 package org.eclipse.jst.jsf.core.internal.jsflibraryconfig;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IExtension;
 import org.eclipse.core.runtime.IExtensionPoint;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.InvalidRegistryObjectException;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.QualifiedName;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.xmi.XMLResource;
 import org.eclipse.jdt.core.IClasspathContainer;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jst.jsf.core.internal.JSFCorePlugin;
 import org.eclipse.jst.jsf.core.internal.JSFLibraryClasspathContainer;
 import org.eclipse.jst.jsf.core.internal.Messages;
 import org.eclipse.jst.jsf.core.internal.RegistryUpgradeCommitHandler;
 import org.eclipse.jst.jsf.core.internal.jsflibraryregistry.ArchiveFile;
 import org.eclipse.jst.jsf.core.internal.jsflibraryregistry.JSFLibrary;
 import org.eclipse.jst.jsf.core.internal.jsflibraryregistry.JSFLibraryRegistry;
 import org.eclipse.jst.jsf.core.internal.jsflibraryregistry.JSFLibraryRegistryFactory;
 import org.eclipse.jst.jsf.core.internal.jsflibraryregistry.adapter.MaintainDefaultImplementationAdapter;
 import org.eclipse.jst.jsf.core.internal.jsflibraryregistry.impl.JSFLibraryRegistryPackageImpl;
 import org.eclipse.jst.jsf.core.internal.jsflibraryregistry.util.JSFLibraryRegistryResourceFactoryImpl;
 import org.eclipse.jst.jsf.core.internal.jsflibraryregistry.util.JSFLibraryRegistryResourceImpl;
 import org.eclipse.jst.jsf.core.internal.jsflibraryregistry.util.JSFLibraryRegistryUpgradeUtil;
 import org.eclipse.jst.jsf.core.internal.project.facet.JSFUtils;
 import org.eclipse.jst.jsf.core.jsflibraryconfiguration.JSFLibraryConfigurationHelper;
 import org.eclipse.jst.jsf.core.jsflibraryregistry.PluginProvidedJSFLibraryCreationHelper;
 import org.eclipse.jst.jsf.core.jsflibraryregistry.internal.PluginProvidedJSFLibraryCreationHelper2;
 
 /**
  * A singleton maintains lists of implementation and component libraries 
  * in registry.
  *   
  * Each item in the lists contains a workingcopy of a JSF library and 
  * decorates with usage information such selection and deployment.  
  * 
  * The lists are updated when there are changes in JSF library registry.
  * 
  * @author Justin Chen, etc. - Oracle
  */
 public class JSFLibraryRegistryUtil {
 	private static JSFLibraryRegistryUtil instance = null;	
 	
 	private List implLibs = null;
 	private List compLibs = null;
 	
 
 	// The NS URI of the JSF Library Registry's Ecore package. (Must match
 	// setting on package in Ecore model.)
 	private static final String JSF_LIBRARY_REGISTRY_NSURI = "http://www.eclipse.org/webtools/jsf/schema/jsflibraryregistry.xsd"; //$NON-NLS-1$
 
 	private static final String LIB_EXT_PT 		= "pluginProvidedJsfLibraries"; //$NON-NLS-1$
 	//deprecated ext-pt
 	private static final String OLD_LIB_EXT_PT 	= "jsfLibraries"; //$NON-NLS-1$
 
 	// The JSF Library Registry EMF resource instance.
 	private static JSFLibraryRegistryResourceImpl jsfLibraryRegistryResource = null;
 	
 	//JSFLibraryRegistry singleton
 	private JSFLibraryRegistry jsfLibraryRegistry;
 	
 	/**
 	 * Private constructor
 	 */
 	private JSFLibraryRegistryUtil() {
 	    //nothing to do
 	}
 	
 	/**
 	 * Return the singleton instance of JSFLibraryRegistryUtil.
 	 *   
 	 * @return JSFLibraryRegistryUtil
 	 */
 	public synchronized static JSFLibraryRegistryUtil getInstance() {
 		if ( instance == null ) {
 			instance = new JSFLibraryRegistryUtil();
 			instance.loadJSFLibraryRegistry();
 		}
 		return instance;
 	}
 
 	/**
 	 * Convenience method to return the JSFLibraryRegistry instance.
 	 * 
 	 * @return jsfLibReg JSFLibraryRegistry
 	 */
 	public JSFLibraryRegistry getJSFLibraryRegistry() {
 		return jsfLibraryRegistry;
 	}
 	
 	/**
 	 * Get the default JSF implementation library instance.
 	 * A null is returned when there is no libraries in the registry.
 	 * 
 	 * @return JSFLibraryInternalReference
 	 */
 	public JSFLibraryInternalReference getDefaultJSFImplementationLibrary() {
 		JSFLibrary dftImplLib = getJSFLibraryRegistry().getDefaultImplementation();
 		
 		return ((dftImplLib != null) ? 
 				getJSFLibraryReferencebyID(dftImplLib.getID()) :
 				null);
 	}
 	
 	/**
 	 * Get the working copy of JSF implementation libraries.
 	 * The list is updated when there are changes in registry.
 	 * 
 	 * @return List
 	 */
 	List getJSFImplementationLibraries() {
 		if (implLibs == null) {
 			implLibs = wrapJSFLibraries(getJSFLibraryRegistry().getImplJSFLibraries());
 		} else {
 			if (implLibs.size() != getJSFLibraryRegistry().getImplJSFLibraries().size() || 
 					isAnyLibraryChanged(implLibs)) {
 				implLibs.clear();
 				implLibs = wrapJSFLibraries(getJSFLibraryRegistry().getImplJSFLibraries());
 			}
 		}
 		return implLibs;
 	}
 		
 	/**
 	 * Get the working copy of JSF component libraries.
 	 * The list is updated when there are changes in registry.
 	 * 
 	 * @return List
 	 */
 	List getJSFComponentLibraries() {
 		if (compLibs == null) {
 			compLibs = wrapJSFLibraries(getJSFLibraryRegistry().getNonImplJSFLibraries());
 		} else {
 			if (compLibs.size() != getJSFLibraryRegistry().getNonImplJSFLibraries().size() || 
 					isAnyLibraryChanged(compLibs)) {
 				compLibs.clear();
 				compLibs = wrapJSFLibraries(getJSFLibraryRegistry().getNonImplJSFLibraries());
 			}			
 		}
 		return compLibs;
 	}
 	
 	/**
 	 * Get the JSFLibraryDecorator object from the provided ID. 
 	 * A null is returned no library matches the ID.
 	 * 
 	 * @param id String
 	 * @return JSFLibraryDecorator
 	 */
 	public JSFLibraryInternalReference getJSFLibraryReferencebyID(final String id) {
 		Iterator it = getJSFImplementationLibraries().iterator();
 		JSFLibraryInternalReference crtItem = null;
 		
 		// search implementation libraries
 		while(it.hasNext()) {
 			crtItem = (JSFLibraryInternalReference)it.next();
 			if (id.equals(crtItem.getID())) {
 				return crtItem;
 			}
 		}
 		// search component libraries
 		it = getJSFComponentLibraries().iterator();
 		while(it.hasNext()) {
 			crtItem = (JSFLibraryInternalReference)it.next();
 			if (id.equals(crtItem.getID())) {
 				return crtItem;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Add a JSF Library into collection for either 
 	 * JSF implementation libraries or component libraries.  
 	 * The decision is based on if a JSF library is an implementation.
 	 * 
 	 * @param library JSFLibraryLibraryReference
 	 */
 	public void addJSFLibrary(final JSFLibraryInternalReference library) {
 		 // Library is added only if it does not exist in registry 
 		if (library != null && getJSFLibraryRegistry().getJSFLibraryByID(library.getID()) == null) {
 			// Add the library working copy into workspace registry.
 			JSFLibrary jsfLib = library.getLibrary();
 			getJSFLibraryRegistry().addJSFLibrary(jsfLib.getWorkingCopy());
 			
 			// Add library into the collection depends on its type.
 			List list = (library.isImplementation() ? 
 							getJSFImplementationLibraries() :
 							getJSFComponentLibraries());
 			list.add(library);
 		}
 	}	
 	
 	private List wrapJSFLibraries(final EList libs) {
 		List list = new ArrayList();
 		if (libs != null) {
 			JSFLibrary jsfLib;
 			JSFLibraryInternalReference jsfLibDctr;
 			
 			Iterator it = libs.iterator();
 			while (it.hasNext()) {
 				jsfLib = (JSFLibrary) it.next();
 				 // Set unselected and undeployed initially.
 				jsfLibDctr = new JSFLibraryInternalReference(jsfLib, //.getWorkingCopy(), 
 								false, 
 								false);
 				list.add(jsfLibDctr);
 			}
 		}	
 		return list;		
 	}
 
 	private boolean isAnyLibraryChanged(final List list) {
 		Iterator it = list.iterator();
 		JSFLibraryInternalReference wclib = null;		// working copy library
 		JSFLibrary lib = null;
 		
 		while(it.hasNext()) {
 			wclib = (JSFLibraryInternalReference)it.next();
 			lib = getJSFLibraryRegistry().getJSFLibraryByID(wclib.getID());
 			if (lib == null) {					// removed. Hence, changed.
 				return true;
 			}
 			if (wclib.getArchiveFiles().size() != 
 				lib.getArchiveFiles().size()) { // Archives changed..
 				return true;
 			}
 			if (isAnyArchiveFileChanged(wclib.getArchiveFiles(), 
 					lib.getArchiveFiles())) {   // Check archive file changes.  I.e., name and location
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private boolean isAnyArchiveFileChanged(final EList source, EList target) {		
 		ArchiveFile arSrc = null;
 		Iterator it = source.iterator();
 		while (it.hasNext()) {
 			arSrc = (ArchiveFile) it.next();
 			if (!findMatchedArchive(arSrc, target)) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	private boolean findMatchedArchive(ArchiveFile source, EList list) {
 		ArchiveFile target = null;
 		Iterator it = list.iterator();
 		while (it.hasNext()) {
 			target = (ArchiveFile) it.next();
 			if (target.equals(source)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Get the classpath entries for a JSF Library
 	 * @param lib
 	 * @return IClasspathEntry[]
 	 */
 	public IClasspathEntry[] getClasspathEntries(JSFLibrary lib){		
 		//TODO: cache to optimize.   probably belongs inside JSFLibrary model.
 		ArrayList res= new ArrayList(lib.getArchiveFiles().size());
 		for (Iterator it=lib.getArchiveFiles().iterator();it.hasNext();) {
 			ArchiveFile jar= (ArchiveFile)it.next();			
 			if (jar != null && jar.exists()) {
 				IClasspathEntry entry = getClasspathEntry(jar);
 				if (entry != null)
 					res.add(entry);
 			}
 		}
 		IClasspathEntry[] entries= (IClasspathEntry[]) res.toArray(new IClasspathEntry[res.size()]);
 		return entries;
 	}
 	
 	/**
 	 * Create IClasspathEntry for ArchiveFile
 	 * @param jar
 	 * @return IClasspathEntry
 	 */
 	public IClasspathEntry getClasspathEntry(ArchiveFile jar){
 		IClasspathEntry entry = null;
 		if (jar !=null && jar.exists()){
 			entry = JavaCore.newLibraryEntry(new Path(jar.getResolvedSourceLocation()), null, null);//, nu, sourceAttachRoot, accessRules, extraAttributes, false/*not exported*/);
 		}
 		return entry;
 	}
 	
 	/**
 	 * Binds JSF Libraries to classpath containers when the library changes.
 	 * 
 	 * This method will deal with library/cp container renames by removing the old classpath container and then adding.
 	 * 
 	 * @param oldId
 	 * @param newId
 	 * @param monitor
 	 * @throws JavaModelException
 	 */
	public static void rebindClasspathContainerEntries(String oldId, String newId, IProgressMonitor monitor) throws JavaModelException {
 		IWorkspaceRoot root= ResourcesPlugin.getWorkspace().getRoot();
 		IJavaProject[] projects= JavaCore.create(root).getJavaProjects();
 		IPath containerPath= new Path(JSFLibraryConfigurationHelper.JSF_LIBRARY_CP_CONTAINER_ID).append(newId);
 		IPath oldContainerPath = new Path(JSFLibraryConfigurationHelper.JSF_LIBRARY_CP_CONTAINER_ID).append(oldId);
 		
 		JSFLibrary lib = JSFLibraryRegistryUtil.getInstance().getJSFLibraryRegistry().getJSFLibraryByID(newId);
 		List affectedProjects= new ArrayList();
		boolean removeAndAddBecauseOfRename = (!oldId.equals(newId));
 		// find all projects using the old container name...
 		for (int i= 0; i < projects.length; i++) {
 			IJavaProject project= projects[i];
 			IClasspathEntry[] entries= project.getRawClasspath();
 			for (int k= 0; k < entries.length; k++) {
 				IClasspathEntry curr= entries[k];
 				if (curr.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
 					if (oldContainerPath.equals(curr.getPath())) {
 						affectedProjects.add(project);
 						break;
 					}				
 				}
 			}
 		}
 		
 		if (!affectedProjects.isEmpty()) {
 			IJavaProject[] affected= (IJavaProject[]) affectedProjects.toArray(new IJavaProject[affectedProjects.size()]);
 			IClasspathContainer[] containers= new IClasspathContainer[affected.length];
 			removeAndAddBecauseOfRename = (!oldId.equals(newId));
 			if (removeAndAddBecauseOfRename){//not very pretty... remove and add new container				
 				IClasspathEntry newEntry = JavaCore.newContainerEntry(containerPath);
 				for (int i= 0; i < affected.length; i++) {
 					IJavaProject project= affected[i];
 					IClasspathEntry[] entries= project.getRawClasspath();
 					List keptEntries = new ArrayList();
 					//keep all entries except the old one
 					for (int k= 0; k < entries.length; k++) {
 						IClasspathEntry curr= entries[k];
 						if (curr.getEntryKind() == IClasspathEntry.CPE_CONTAINER){
 								if( ! oldContainerPath.equals(curr.getPath())) 
 							keptEntries.add(curr);						
 						}
 						else {
 							keptEntries.add(curr);
 						}						
 					}
 					// add new container entry
 					keptEntries.add(newEntry);
 					setRawClasspath(project, keptEntries, monitor);
 				}
 				
 			}
 			else {//rebind
 
 				JSFLibraryClasspathContainer container= new JSFLibraryClasspathContainer(lib);
 				containers[0] = container;
 	
 				JavaCore.setClasspathContainer(containerPath, affected, containers, monitor);
 			}
 		} else {
 			if (monitor != null) {
 				monitor.done();
 			}
 		}
 	}
 
 	/**
 	 * Sets the raw classpath on a project and logs an error if it when a JavaModelException occurs
 	 * @param project
 	 * @param cpEntries
 	 * @param monitor
 	 */
 	public static void setRawClasspath(IJavaProject project, List cpEntries, IProgressMonitor monitor) {
 		IClasspathEntry[] entries = (IClasspathEntry[])cpEntries.toArray(new IClasspathEntry[0]);
 		try {
 			project.setRawClasspath(entries, monitor);
 		} catch (JavaModelException e) {
 			JSFCorePlugin.log(e, "Unable to set classpath for: "+project.getProject().getName());
 		}
 	}
 	
 	/**
 	 * @param iproject
 	 * @return true if iproject has persistent properties indicating that it may still
 	 * be using V1 JSF Library references
 	 */
 	public static boolean doesProjectHaveV1JSFLibraries(IProject iproject)
 	{
 	   if (iproject == null || !iproject.isAccessible())
 	   {
 	       return false; // won't be able to get reading on a null or closed project
 	   }
 	   
        try
         {
             Object compLib = iproject.getPersistentProperty(new QualifiedName(JSFLibraryConfigProjectData.QUALIFIEDNAME, JSFUtils.PP_JSF_COMPONENT_LIBRARIES));
             Object implLib = iproject.getPersistentProperty(new QualifiedName(JSFLibraryConfigProjectData.QUALIFIEDNAME, JSFUtils.PP_JSF_IMPLEMENTATION_LIBRARIES));
             
             if (compLib != null || implLib != null)
             {
                 return true;
             }
         }
         catch(CoreException ce)
         {
             JSFCorePlugin.log(ce, "Error checking age of project");
         }
         // by default, fall through to false
         return false;
 	}
 	
 	/**
 	 * Removes the persistent property from JSF projects tagged with
 	 * V1 JSF libraries.
 	 * @param projects
 	 */
 	public static void removeV1JSFLibraryProperty(List<IProject> projects)
 	{
 	    for (final Iterator<IProject> it = projects.iterator(); it.hasNext();)
 	    {
 	        IProject project = it.next();
             try {
                 if (project.isAccessible())
                 {
                     project.setPersistentProperty(new QualifiedName(JSFLibraryConfigProjectData.QUALIFIEDNAME, JSFUtils.PP_JSF_COMPONENT_LIBRARIES), null);
                     project.setPersistentProperty(new QualifiedName(JSFLibraryConfigProjectData.QUALIFIEDNAME, JSFUtils.PP_JSF_IMPLEMENTATION_LIBRARIES), null);
                 }
             } catch (CoreException e) {
                 JSFCorePlugin.log(e, "Error removing JSF library persistent property");
             }
 	    }
 	}
 	
 	/**
 	 * Loads the JSFLibraryRegistry EMF object from plugin-specfic workspace
 	 * settings location.
 	 */
 	private void loadJSFLibraryRegistry() {
 		try {
 			
 			EPackage.Registry.INSTANCE.put(JSF_LIBRARY_REGISTRY_NSURI, JSFLibraryRegistryPackageImpl.init());
 			URI jsfLibRegURI = JSFLibraryRegistryUpgradeUtil.getRegistryURI(JSFLibraryRegistryUpgradeUtil.JSF_LIBRARY_REGISTRY_LATESTVERSION_URL);			
 			JSFLibraryRegistryUpgradeUtil.getInstance().upgradeRegistryIfNecessary(JSFLibraryRegistryUpgradeUtil.LATESTVERSION);
 
 			JSFLibraryRegistryResourceFactoryImpl resourceFactory = new JSFLibraryRegistryResourceFactoryImpl();
 			jsfLibraryRegistryResource = (JSFLibraryRegistryResourceImpl)resourceFactory.createResource(jsfLibRegURI);
 			try {
 				Map options = new HashMap();
 				//disable notifications during load to avoid changing stored default implementation
 				options.put(XMLResource.OPTION_DISABLE_NOTIFY, Boolean.TRUE);
 				jsfLibraryRegistryResource.load(options);
 				jsfLibraryRegistry = (JSFLibraryRegistry)jsfLibraryRegistryResource.getContents().get(0);
 			 	
 				loadJSFLibraryExtensions();
 				loadDeprecatedJSFLibraryExtensions();//to be removed 
 				
 			} catch(IOException ioe) {
 				//Create a new Registry instance
 				jsfLibraryRegistry = JSFLibraryRegistryFactory.eINSTANCE.createJSFLibraryRegistry();
 				jsfLibraryRegistryResource = (JSFLibraryRegistryResourceImpl)resourceFactory.createResource(jsfLibRegURI);
 				jsfLibraryRegistryResource.getContents().add(jsfLibraryRegistry);
 				loadJSFLibraryExtensions();
 				loadDeprecatedJSFLibraryExtensions();//to be removed 
 				saveJSFLibraryRegistry();
 			}
 			//add adapter to maintain default implementation
 			if (jsfLibraryRegistry != null) {				
 				//check that a default impl is set.   if not pick first one if available.
 				JSFLibrary defLib = jsfLibraryRegistry.getDefaultImplementation();
 				if (defLib == null && jsfLibraryRegistry.getImplJSFLibraries().size() > 0){
 					jsfLibraryRegistry.setDefaultImplementation((JSFLibrary)jsfLibraryRegistry.getImplJSFLibraries().get(0));
 					saveJSFLibraryRegistry();
 				}
 				jsfLibraryRegistry.eAdapters().add(MaintainDefaultImplementationAdapter.getInstance());
 				
 				//commit 
 				RegistryUpgradeCommitHandler.commitMigrationIfNecessary();
 			}
 		} catch(MalformedURLException mue) {
 			JSFCorePlugin.log(IStatus.ERROR, Messages.JSFLibraryRegistry_ErrorCreatingURL, mue);
 		}
 	}
 /////////////////////////////////   Load and Save JSF Library Registry ////////////////////////////////////////////////
 	
 	/**
 	 * Creates library registry items from extension points.
 	 */
 	private void loadJSFLibraryExtensions() {
 		try {
 			IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(JSFCorePlugin.PLUGIN_ID, LIB_EXT_PT);
 			IExtension[] extensions = point.getExtensions();
 			for (int i=0;i < extensions.length;i++){
 				IExtension ext = extensions[i];
 				for (int j=0;j < ext.getConfigurationElements().length;j++){
 					PluginProvidedJSFLibraryCreationHelper2 newLibCreator = new PluginProvidedJSFLibraryCreationHelper2(ext.getConfigurationElements()[j]);						
 					JSFLibrary newLib = newLibCreator.create();
 					
 					/**
 					 * Additional check on if a plug-in contributes jsflibraries is an expanded folder.
 					 * Fix related to bug 144954.  
 					 * 
 					 * It would be ideal to check if a plug-in is distributed as a JAR 
 					 * before a JSFLibrary is created.
 					 * 
 					 * This is a temporary solution since JARs in a JAR case is not 
 					 * supported in this release.  Bug 14496.
 					 */
 					if (newLib != null) //&& isJSFLibinExpandedFolder(newLib))
 						jsfLibraryRegistry.addJSFLibrary(newLib);
 				}
 			}
 		} catch (InvalidRegistryObjectException e) {
 			JSFCorePlugin.log(IStatus.ERROR, Messages.JSFLibraryRegistry_ErrorLoadingFromExtPt, e);
 		}
 	}
 	
 	/**
 	 * Creates deprecated library registry items from extension points.
 	 * TO BE REMOVED
 	 */
 	private void loadDeprecatedJSFLibraryExtensions() {
 		try {
 			IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(JSFCorePlugin.PLUGIN_ID, OLD_LIB_EXT_PT);
 			IExtension[] extensions = point.getExtensions();
 			for (int i=0;i < extensions.length;i++){
 				IExtension ext = extensions[i];
 				for (int j=0;j < ext.getConfigurationElements().length;j++){
 					PluginProvidedJSFLibraryCreationHelper newLibCreator = new PluginProvidedJSFLibraryCreationHelper(ext.getConfigurationElements()[j]);						
 					JSFLibrary newLib = newLibCreator.create();
 					
 					/**
 					 * Additional check on if a plug-in contributes jsflibraries is an expanded folder.
 					 * Fix related to bug 144954.  
 					 * 
 					 * It would be ideal to check if a plug-in is distributed as a JAR 
 					 * before a JSFLibrary is created.
 					 * 
 					 * This is a temporary solution since JARs in a JAR case is not 
 					 * supported in this release.  Bug 14496.
 					 */
 					if (newLib != null ) //&& isJSFLibinExpandedFolder(newLib))
 						jsfLibraryRegistry.addJSFLibrary(newLib);
 				}
 			}
 		} catch (InvalidRegistryObjectException e) {
 			JSFCorePlugin.log(IStatus.ERROR, Messages.JSFLibraryRegistry_ErrorLoadingFromExtPt, e);
 		}
 	}
 	
 	/**
 	 * Saves the JSFLibraryRegistry EMF object from plugin-specfic workspace
 	 * settings location. (Called from stop(BundleContext).)
 	 * @return true if save is successful
 	 */
 	public boolean saveJSFLibraryRegistry() {
 		boolean saved = false;
 		if (jsfLibraryRegistryResource != null) {
 			try {
 				jsfLibraryRegistryResource.save(Collections.EMPTY_MAP);
 				saved = true;
 			} catch(IOException ioe) {
 				JSFCorePlugin.log(IStatus.ERROR, Messages.JSFLibraryRegistry_ErrorSaving, ioe);
 			}
 		} else {
 			JSFCorePlugin.log(IStatus.ERROR, Messages.JSFLibraryRegistry_ErrorSaving);
 		}
 		return saved;
 	}
 
 
 }
