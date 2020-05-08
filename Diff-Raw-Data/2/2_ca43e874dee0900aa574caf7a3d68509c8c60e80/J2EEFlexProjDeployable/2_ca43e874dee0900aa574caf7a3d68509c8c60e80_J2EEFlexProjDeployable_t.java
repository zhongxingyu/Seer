 /*******************************************************************************
  * Copyright (c) 2003, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.internal.deployables;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jem.workbench.utility.JemProjectUtilities;
 import org.eclipse.jst.j2ee.application.Application;
 import org.eclipse.jst.j2ee.classpathdep.ClasspathDependencyUtil;
 import org.eclipse.jst.j2ee.classpathdep.IClasspathDependencyConstants;
 import org.eclipse.jst.j2ee.componentcore.J2EEModuleVirtualArchiveComponent;
 import org.eclipse.jst.j2ee.componentcore.J2EEModuleVirtualComponent;
 import org.eclipse.jst.j2ee.componentcore.util.EARArtifactEdit;
 import org.eclipse.jst.j2ee.ejb.EJBJar;
 import org.eclipse.jst.j2ee.internal.EjbModuleExtensionHelper;
 import org.eclipse.jst.j2ee.internal.IEJBModelExtenderManager;
 import org.eclipse.jst.j2ee.internal.J2EEConstants;
 import org.eclipse.jst.j2ee.internal.classpathdep.ClasspathDependencyManifestUtil;
 import org.eclipse.jst.j2ee.internal.plugin.IJ2EEModuleConstants;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEPlugin;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
 import org.eclipse.jst.server.core.IApplicationClientModule;
 import org.eclipse.jst.server.core.IConnectorModule;
 import org.eclipse.jst.server.core.IEJBModule;
 import org.eclipse.jst.server.core.IEnterpriseApplication;
 import org.eclipse.jst.server.core.IJ2EEModule;
 import org.eclipse.jst.server.core.IWebModule;
 import org.eclipse.wst.common.componentcore.ArtifactEdit;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.internal.ComponentResource;
 import org.eclipse.wst.common.componentcore.internal.StructureEdit;
 import org.eclipse.wst.common.componentcore.internal.WorkbenchComponent;
 import org.eclipse.wst.common.componentcore.internal.resources.VirtualArchiveComponent;
 import org.eclipse.wst.common.componentcore.internal.util.ComponentUtilities;
 import org.eclipse.wst.common.componentcore.internal.util.IModuleConstants;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
 import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
 import org.eclipse.wst.server.core.IModule;
 import org.eclipse.wst.server.core.ServerUtil;
 import org.eclipse.wst.server.core.internal.ModuleFile;
 import org.eclipse.wst.server.core.internal.ModuleFolder;
 import org.eclipse.wst.server.core.model.IModuleFile;
 import org.eclipse.wst.server.core.model.IModuleFolder;
 import org.eclipse.wst.server.core.model.IModuleResource;
 import org.eclipse.wst.web.internal.deployables.ComponentDeployable;
 /**
  * J2EE module superclass.
  */
 public class J2EEFlexProjDeployable extends ComponentDeployable implements IJ2EEModule, IEnterpriseApplication, IApplicationClientModule, IConnectorModule, IEJBModule, IWebModule {
 	private static final IPath WEB_CLASSES_PATH = new Path(J2EEConstants.WEB_INF_CLASSES);
 	private static final IPath MANIFEST_PATH = new Path(J2EEConstants.MANIFEST_URI);
 	private static IPath WEBLIB = new Path(J2EEConstants.WEB_INF_LIB).makeAbsolute();
 	private IPackageFragmentRoot[] cachedSourceContainers;
 	private IContainer[] cachedOutputContainers;
 	private HashMap cachedOutputMappings;
 	private HashMap cachedSourceOutputPairs;
 	private List classpathComponentDependencyURIs = new ArrayList();
 
 	/**
 	 * Constructor for J2EEFlexProjDeployable.
 	 * 
 	 * @param project
 	 * @param aComponent
 	 */
 	public J2EEFlexProjDeployable(IProject project, IVirtualComponent aComponent) {
 		super(project, aComponent);
 	}
 	
 
 	/**
 	 * Constructor for J2EEFlexProjDeployable.
 	 * 
 	 * @param project
 	 */
 	public J2EEFlexProjDeployable(IProject project) {
 		super(project);
 	}
 
 	/**
 	 * Returns the root folders for the resources in this module.
 	 * 
 	 * @return a possibly-empty array of resource folders
 	 */
 	public IContainer[] getResourceFolders() {
 		List result = new ArrayList();
 		IVirtualComponent vc = ComponentCore.createComponent(getProject());
 		if (vc != null) {
 			IVirtualFolder vFolder = vc.getRootFolder();
 			if (vFolder != null) {
 				IContainer[] underlyingFolders = vFolder.getUnderlyingFolders();
 				result.addAll(Arrays.asList(underlyingFolders));
 			}
 		}
 		return (IContainer[]) result.toArray(new IContainer[result.size()]);
 	}
 
 	/**
 	 * Returns the root folders containing Java output in this module.
 	 * 
 	 * @return a possibly-empty array of Java output folders
 	 */
 	public IContainer[] getJavaOutputFolders() {
 		if (cachedOutputContainers == null)
 			cachedOutputContainers = getJavaOutputFolders(getProject());
 		return cachedOutputContainers;
 	}
 	
 	public IContainer[] getJavaOutputFolders(IProject project) {
 		if (project == null)
 			return new IContainer[0];
 		return J2EEProjectUtilities.getOutputContainers(project);
 	}
 
 	protected boolean shouldIncludeUtilityComponent(IVirtualComponent virtualComp,IVirtualReference[] references, ArtifactEdit edit) {
 		// If the component module is an EAR we know all archives are filtered out of virtual component members
 		// and we will return only those archives which are not binary J2EE modules in the EAR DD.  These J2EE modules will
 		// be returned by getChildModules()
 		if (J2EEProjectUtilities.isEARProject(component.getProject()))
 			return virtualComp != null && virtualComp.isBinary() && !isNestedJ2EEModule(virtualComp, references, (EARArtifactEdit)edit);
 		else 
 			return super.shouldIncludeUtilityComponent(virtualComp, references, edit);
 	}
 	
 	protected void addUtilMember(IVirtualComponent parent, IVirtualReference reference, IPath runtimePath) {
 		// do not add classpath dependencies whose runtime path (../) maps to the parent component
 		if (!runtimePath.equals(IClasspathDependencyConstants.RUNTIME_MAPPING_INTO_CONTAINER_PATH)) {
 			super.addUtilMember(parent, reference, runtimePath);
 		}
 	}
 	
 	protected IModuleResource[] getBinaryModuleMembers() {
 		IPath archivePath = ((J2EEModuleVirtualArchiveComponent)component).getWorkspaceRelativePath();
 		ModuleFile mf = null;
 		if (archivePath != null) { //In Workspace
 			IFile utilFile = ResourcesPlugin.getWorkspace().getRoot().getFile(archivePath);
 			mf = new ModuleFile(utilFile, utilFile.getName(), ((J2EEModuleVirtualArchiveComponent)component).getRuntimePath().makeRelative());
 		} else {
 			File extFile = ((J2EEModuleVirtualArchiveComponent)component).getUnderlyingDiskFile();
 			mf = new ModuleFile(extFile, extFile.getName(), ((J2EEModuleVirtualArchiveComponent)component).getRuntimePath().makeRelative());
 		}
 		return new IModuleResource[] {mf};
 	}
 	
 	public IModuleResource[] members() throws CoreException {
 		members.clear();
 		classpathComponentDependencyURIs.clear();
 
 		// Handle binary components
 		if (component instanceof J2EEModuleVirtualArchiveComponent)
 			return getBinaryModuleMembers();
 
 		if (J2EEProjectUtilities.isEARProject(component.getProject())) {
 			// If an EAR, add classpath contributions for all referenced modules
 			addReferencedComponentClasspathDependencies(component, false);
 		} else {
 			if (J2EEProjectUtilities.isDynamicWebProject(component.getProject())) {
 				// If a web, add classpath contributions for all WEB-INF/lib modules
 				addReferencedComponentClasspathDependencies(component, true);
 			}
 			if (canExportClasspathComponentDependencies(component)){
 				saveClasspathDependencyURIs(component);
 			}
 		}
 		
 		// If j2ee project structure is a single root structure, just return optimized members
 		if (isSingleRootStructure()) {
 			final IModuleResource[] resources = getOptimizedMembers();
 			if (!classpathComponentDependencyURIs.isEmpty()) {
 				for (int i = 0; i < resources.length; i++) {
 					if (resources[i] instanceof IModuleFolder) {
 						IModuleFolder folder = (IModuleFolder) resources[i];
 						if (folder.getName().equals(J2EEConstants.META_INF)) {
 							IModuleResource[] files = folder.members();
 							for (int j = 0; j < files.length; j++) {
								files[j] = replaceManifestFile((IModuleFile) files[j]);								
 							}
 						}
 					}
 				}
 			}
 			return resources;
 		}
 		
 		cachedSourceContainers = J2EEProjectUtilities.getSourceContainers(getProject());
 		try {
 			IPath javaPath = Path.EMPTY;
 			if (J2EEProjectUtilities.isDynamicWebProject(component.getProject()))
 				javaPath = WEB_CLASSES_PATH;
 			
 			if (component != null) {
 				IVirtualFolder vFolder = component.getRootFolder();
 				IModuleResource[] mr = getMembers(vFolder, Path.EMPTY);
 				int size = mr.length;
 				for (int j = 0; j < size; j++) {
 					members.add(mr[j]);
 				}
 			}
 			
 			IContainer[] javaCont = getJavaOutputFolders();		
 			int size = javaCont.length;
 			for (int i = 0; i < size; i++) {
 				//If the java output is in the scope of the virtual component, ignore to avoid duplicates
 				if (ComponentCore.createResources(javaCont[i]).length > 0) 
 					continue;
 				IModuleResource[] mr = getMembers(javaCont[i], javaPath, javaPath, javaCont);
 				int size2 = mr.length;
 				for (int j = 0; j < size2; j++) {
 					members.add(mr[j]);
 				}
 			}
 			
 			if (component != null) {
 				addUtilMembers(component);
 				List consumableMembers = getConsumableReferencedMembers(component);
 				if (!consumableMembers.isEmpty())
 					members.addAll(consumableMembers);
 			}
 			
 			IModuleResource[] mr = new IModuleResource[members.size()];
 			members.toArray(mr);
 			return mr;
 		} finally {
 			cachedSourceContainers = null;
 			cachedOutputContainers = null;
 			cachedOutputMappings = null;
 			cachedSourceOutputPairs = null;
 		}
 	}
 	
 	protected IModuleFile createModuleFile(IFile file, IPath path) {
 		// if this is the MANIFEST.MF file and we have classpath component dependencies, 
 		// update it
 		return replaceManifestFile(super.createModuleFile(file, path));
 	}
 	
 	protected IModuleFile replaceManifestFile(IModuleFile moduleFile) {
 		final IFile file = (IFile) moduleFile.getAdapter(IFile.class);
 		final IPath path = moduleFile.getModuleRelativePath();
 		// if the MANIFEST.MF is being requested and we have classpath component dependencies, 
 		// dynamically generate a customized MANIFEST.MF and return that 
 		if (path.append(file.getName()).equals(MANIFEST_PATH) && !classpathComponentDependencyURIs.isEmpty()) {
 			final IProject project = file.getProject();
 			final IPath workingLocation = project.getWorkingLocation(J2EEPlugin.PLUGIN_ID);
 			// create path to temp MANIFEST.MF
 			final IPath tempManifestPath = workingLocation.append(MANIFEST_PATH);
 			final File fsFile = tempManifestPath.toFile();
 			if (!fsFile.exists()) {
 				// create parent dirs for temp MANIFEST.MF
 				final File parent = fsFile.getParentFile();
 				if (!parent.exists()) {
 					if (!parent.mkdirs()) {
 						return moduleFile;
 					}
 				}
 			}
 			// create temp MANIFEST.MF using util method
 			try {
 				ClasspathDependencyManifestUtil.updateManifestClasspath(file, classpathComponentDependencyURIs, new FileOutputStream(fsFile));
 				// create new ModuleFile that points to temp MANIFEST.MF
 				return new ModuleFile(fsFile, file.getName(), path);
 			} catch (IOException ioe) {
 				return moduleFile;
 			}
 		}
 		return moduleFile;
 	}
 	
 	protected IModuleResource[] handleJavaPath(IPath path, IPath javaPath, IPath curPath, IContainer[] javaCont, IModuleResource[] mr, IContainer cc) throws CoreException {
 		if (curPath.equals(javaPath)) {
 			int size = javaCont.length;
 			for (int i = 0; i < size; i++) {
 				IModuleResource[] mr2 = getMembers(javaCont[i], path.append(cc.getName()), null, null);
 				IModuleResource[] mr3 = new IModuleResource[mr.length + mr2.length];
 				System.arraycopy(mr, 0, mr3, 0, mr.length);
 				System.arraycopy(mr2, 0, mr3, mr.length, mr2.length);
 				mr = mr3;
 			}
 		} else {
 			boolean containsFolder = false;
 			String name = javaPath.segment(curPath.segmentCount());
 			int size = mr.length;
 			for (int i = 0; i < size && !containsFolder; i++) {
 				if (mr[i] instanceof IModuleFolder) {
 					IModuleFolder mf2 = (IModuleFolder) mr[i];
 					if (name.equals(mf2.getName())) {
 						containsFolder = true;
 					}
 				}
 			}
 			
 			if (!containsFolder && javaCont.length > 0) {
 				ModuleFolder mf2 = new ModuleFolder(javaCont[0], name, curPath);
 				IModuleResource[] mrf = new IModuleResource[0];
 				size = javaCont.length;
 				for (int i = 0; i < size; i++) {
 					IModuleResource[] mrf2 = getMembers(javaCont[i], javaPath, null, null);
 					IModuleResource[] mrf3 = new IModuleResource[mrf.length + mrf2.length];
 					System.arraycopy(mrf, 0, mrf3, 0, mrf.length);
 					System.arraycopy(mrf2, 0, mrf3, mrf.length, mrf2.length);
 					mrf = mrf3;
 				}
 				
 				mf2.setMembers(mrf);
 				
 				IModuleResource[] mr3 = new IModuleResource[mr.length + 1];
 				System.arraycopy(mr, 0, mr3, 0, mr.length);
 				mr3[mr.length] = mf2;
 				mr = mr3;
 			}
 		}
 		return mr;
 	}
 
     /**
      * Returns the classpath as a list of absolute IPaths.
      * 
      * @return an array of paths
      */
     public IPath[] getClasspath() {
 		List paths = new ArrayList();
         IJavaProject proj = JemProjectUtilities.getJavaProject(getProject());
         URL[] urls = JemProjectUtilities.getClasspathAsURLArray(proj);
 		for (int i = 0; i < urls.length; i++) {
 			URL url = urls[i];
 			paths.add(Path.fromOSString(url.getPath()));
 		}
         return  (IPath[]) paths.toArray(new IPath[paths.size()]);
     }
     
     public String getJNDIName(String ejbName) {
     	if (!J2EEProjectUtilities.isEJBProject(component.getProject()))
     		return null;
 		EjbModuleExtensionHelper modHelper = null;
 		EJBJar jar = null;
 		ArtifactEdit ejbEdit = null;
 		try {
 			ejbEdit = ComponentUtilities.getArtifactEditForRead(component);
 			if (ejbEdit != null) {
 				jar = (EJBJar) ejbEdit.getContentModelRoot();
 				modHelper = IEJBModelExtenderManager.INSTANCE.getEJBModuleExtension(null);
 				return modHelper == null ? null : modHelper.getJNDIName(jar, jar.getEnterpriseBeanNamed(ejbName));
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			if (ejbEdit != null)
 				ejbEdit.dispose();
 		}
 		return null;
 	}
 
     /**
      * This method will handle a number of J2EE related scenarios.  If this is an ear and a child module is passed in,
      * the URI for that child module will be returned.  If no child module was passed, the URI of the EAR is returned.
      * If this is a child component and the module passed in is the EAR, we grab the module uri for this compared to that
      * EAR.  If no ear module is passed in we look for one and use it and return URI relative to found EAR.  If no EAR's 
      * are found the URI is returned in a default manner.
      * 
      * @return URI string
      */
     public String getURI(IModule module) {
     	// If the component is an ear and the module passed in is a child module
     	if (component!=null && module!=null && J2EEProjectUtilities.isEARProject(component.getProject()))
  			return getContainedURI(module);
 
     	IVirtualComponent ear = null;
     	String aURI = null;
     	// If the component is a child module and the module passed in is the ear
     	if (module != null && J2EEProjectUtilities.isEARProject(module.getProject()))
     		ear = ComponentCore.createComponent(module.getProject());
     	// else if the component is a child module and the module passed in is null, search for first ear
     	else if (module==null && component != null) {
     		IProject[] earProjects = J2EEProjectUtilities.getReferencingEARProjects(component.getProject());
 	        if (earProjects.length>0)
 	        	ear = ComponentCore.createComponent(earProjects[0]);
     	}
     	// We have a valid ear and the component is a valid child
     	if (ear != null && component != null) {
     		EARArtifactEdit earEdit = null;
 			try {
 				earEdit = EARArtifactEdit.getEARArtifactEditForRead(ear);
 				if (earEdit != null)
 					aURI = earEdit.getModuleURI(component);
 			} catch (Exception e) {
 				e.printStackTrace();
 			} finally {
 				if (earEdit != null)
 					earEdit.dispose();
 			}
     	} 
     	// We have an ear component and no child module
     	else if (component!=null && J2EEProjectUtilities.isEARProject(component.getProject())) {
 			aURI = component.getDeployedName()+IJ2EEModuleConstants.EAR_EXT;
     	} 
     	// We have child components but could not find valid ears
     	else if (component!=null && J2EEProjectUtilities.isDynamicWebProject(component.getProject())) {
     		if (module != null && J2EEProjectUtilities.isUtilityProject(module.getProject())) {
     			IVirtualComponent webComp = ComponentCore.createComponent(component.getProject());
     			IVirtualReference reference = webComp.getReference(module.getProject().getName());
     			aURI = ComponentUtilities.getDeployUriOfUtilComponent(reference);
     		}else{
     			aURI = component.getDeployedName()+IJ2EEModuleConstants.WAR_EXT;
     		}
     	} 
     	else if (component!=null && (J2EEProjectUtilities.isEJBProject(component.getProject()) || J2EEProjectUtilities.isApplicationClientProject(component.getProject()))) {
     		aURI = component.getDeployedName()+IJ2EEModuleConstants.JAR_EXT;
     	} 
     	else if (component!=null && J2EEProjectUtilities.isJCAProject(component.getProject())) {
     		aURI = component.getDeployedName()+IJ2EEModuleConstants.RAR_EXT;
     	}
     	
     	if (aURI !=null && aURI.length()>1 && aURI.startsWith("/")) //$NON-NLS-1$
     		aURI = aURI.substring(1);
     	return aURI;
 	}
     
     private boolean isBinaryModuleArchive(IModule module) {
     	if (module!=null && module.getName().endsWith(IJ2EEModuleConstants.JAR_EXT) || module.getName().endsWith(IJ2EEModuleConstants.WAR_EXT) ||
     			module.getName().endsWith(IJ2EEModuleConstants.RAR_EXT)) {
     		if (component!=null && J2EEProjectUtilities.isEARProject(component.getProject()))
     			return true;
     	}
     	return false;
     }
     
     private String getContainedURI(IModule module) {
     	if (component instanceof J2EEModuleVirtualArchiveComponent || isBinaryModuleArchive(module))
     		return new Path(module.getName()).lastSegment();
     	
     	IVirtualComponent comp = ComponentCore.createComponent(module.getProject());
     	String aURI = null;
     	if (comp!=null && component!=null && J2EEProjectUtilities.isEARProject(component.getProject())) {
 			EARArtifactEdit earEdit = null;
 			try {
 				earEdit = EARArtifactEdit.getEARArtifactEditForRead(component);
 				if (earEdit != null)
 					aURI = earEdit.getModuleURI(comp);
 			} catch (Exception e) {
 				e.printStackTrace();
 			} finally {
 				if (earEdit != null)
 					earEdit.dispose();
 			}
     	}
     	if (aURI !=null && aURI.length()>1 && aURI.startsWith("/")) //$NON-NLS-1$
     		aURI = aURI.substring(1);
     	return aURI;
 	}
     
     /**
      * This method returns the context root property from the deployable project's .component file
      */
     public String getContextRoot() {
 		Properties props = component.getMetaProperties();
 		if(props.containsKey(J2EEConstants.CONTEXTROOT))
 			return props.getProperty(J2EEConstants.CONTEXTROOT);
 	    return component.getName();
     }
     
     /**
      * This method is applicable for a web deployable.  The module passed in should either be null or
      * the EAR module the web deployable is contained in.  It will return the context root from the EAR
      * if it has one or return the .component value in the web project if it is standalone.
      *  
      * @param module
      * @return contextRoot String
      */
     public String getContextRoot(IModule earModule) {
     	IProject deployProject = component.getProject();
     	String contextRoot = null;
     	if (earModule == null)
     		return getContextRoot();
     	else if (J2EEProjectUtilities.isEARProject(earModule.getProject()) && J2EEProjectUtilities.isDynamicWebProject(deployProject)) {
     		EARArtifactEdit edit = null;
     		try {
     			edit = EARArtifactEdit.getEARArtifactEditForRead(earModule.getProject());
     			contextRoot = edit.getWebContextRoot(deployProject);
     		} finally {
     			if (edit!=null)
     				edit.dispose();
     		}
     	}
     	return contextRoot;
     }
     
     /**
      * Find the source container, if any, for the given file.
      * 
      * @param file
      * @return IPackageFragmentRoot sourceContainer for IFile
      */
     protected IPackageFragmentRoot getSourceContainer(IFile file) {
     	if (file == null)
     		return null;
     	IPackageFragmentRoot[] srcContainers = getSourceContainers();
     	for (int i=0; i<srcContainers.length; i++) {
     		IPath srcPath = srcContainers[i].getPath();
     		if (srcPath.isPrefixOf(file.getFullPath()))
     			return srcContainers[i];
     	}
     	return null;
     }
     
     /**
      * Either returns value from cache or stores result as value in cache for the corresponding
      * output container for the given source container.
      * 
      * @param sourceContainer
      * @return IContainer output container for given source container
      */
     protected IContainer getOutputContainer(IPackageFragmentRoot sourceContainer) {
     	if (sourceContainer == null)
     		return null;
     	
     	HashMap pairs = getCachedSourceOutputPairs();
     	IContainer output = (IContainer) pairs.get(sourceContainer);
     	if (output == null) {
     		output = J2EEProjectUtilities.getOutputContainer(getProject(), sourceContainer);
     		pairs.put(sourceContainer,output);
     	}
     	return output;
     }
     
 	private IPackageFragmentRoot[] getSourceContainers() {
 		if (cachedSourceContainers != null)
 			return cachedSourceContainers;
 		return J2EEProjectUtilities.getSourceContainers(getProject());
 	}
     
     protected List getConsumableReferencedMembers(IVirtualComponent vc) throws CoreException {
 		List consumableMembers = new ArrayList();
 		IVirtualReference[] refComponents = vc.getReferences();
     	for (int i = 0; i < refComponents.length; i++) {
     		IVirtualReference reference = refComponents[i];
     		if (reference != null && reference.getDependencyType()==IVirtualReference.DEPENDENCY_TYPE_CONSUMES) {
     			IVirtualComponent consumedComponent = reference.getReferencedComponent();
     			if (consumedComponent!=null && isProjectOfType(consumedComponent.getProject(),IModuleConstants.JST_UTILITY_MODULE)) {
     				if (consumedComponent != null && consumedComponent.getRootFolder()!=null) {
     					IVirtualFolder vFolder = consumedComponent.getRootFolder();
     					IModuleResource[] mr = getMembers(vFolder, reference.getRuntimePath().makeRelative());
     					int size = mr.length;
     					for (int j = 0; j < size; j++) {
     						if (!members.contains(mr[j]))
     							members.add(mr[j]);
     					}
     					addUtilMembers(consumedComponent);
     					List childConsumableMembers = getConsumableReferencedMembers(consumedComponent);
     					if (!childConsumableMembers.isEmpty())
     						members.addAll(childConsumableMembers);
     				}
     				
     				IContainer[] javaCont = getJavaOutputFolders(consumedComponent.getProject());		
     				int size = javaCont.length;
     				for (int j = 0; j < size; j++) {
     					IModuleResource[] mr = getMembers(javaCont[j], reference.getRuntimePath(), reference.getRuntimePath(), javaCont);
     					int size2 = mr.length;
     					for (int k = 0; k < size2; k++) {
     						if (!members.contains(mr[k]))
     							members.add(mr[k]);
     					}
     				}
     			}
     		}
     	}
 		return consumableMembers;
 	}
     
     protected IModule gatherModuleReference(IVirtualComponent component, IVirtualComponent targetComponent ) {
     	IModule module = super.gatherModuleReference(component, targetComponent);
     	// Handle binary module components
     	if (targetComponent instanceof J2EEModuleVirtualArchiveComponent) {
     		if (J2EEProjectUtilities.isEARProject(component.getProject()) || targetComponent.getProject()!=component.getProject())
     			module = ServerUtil.getModule(J2EEDeployableFactory.ID+":"+targetComponent.getName()); //$NON-NLS-1$
     	}
 		return module;
     }
     
     /**
      * Determine if the component is nested J2EE module on the application.xml of this EAR
      * @param aComponent
      * @return boolean is passed in component a nested J2EE module on this EAR
      */
     private boolean isNestedJ2EEModule(IVirtualComponent aComponent, IVirtualReference[] references, EARArtifactEdit edit) {
     	if (edit==null) 
 			return false;
 		Application app = edit.getApplication();
 		IVirtualReference reference = getReferenceNamed(references,aComponent.getName());
 		// Ensure module URI exists on EAR DD for binary archive
 		return app.getFirstModule(reference.getArchiveName()) != null;
     }
     
     private IVirtualReference getReferenceNamed(IVirtualReference[] references, String name) {
     	for (int i=0; i<references.length; i++) {
     		if (references[i].getReferencedComponent().getName().equals(name))
     			return references[i];
     	}
     	return null;
     }
     
     protected ArtifactEdit getComponentArtifactEditForRead() {
 		return EARArtifactEdit.getEARArtifactEditForRead(component.getProject());
 	}
     
     /**
      * The references for J2EE module deployment are only those child modules of EARs or web modules
      */
     protected IVirtualReference[] getReferences(IVirtualComponent aComponent) {
     	if (aComponent == null || aComponent.isBinary()) {
     		return new IVirtualReference[] {};
     	} else if (J2EEProjectUtilities.isDynamicWebProject(aComponent.getProject())) {
     		return getWebLibModules((J2EEModuleVirtualComponent)aComponent);
     	} else if (J2EEProjectUtilities.isEARProject(aComponent.getProject())) {
     		return super.getReferences(aComponent);
     	} else {
     		return new IVirtualReference[] {};
     	}
     }
     
     /**
 	 * This method will return the list of dependent modules which are utility jars in the web lib
 	 * folder of the deployed path of the module. It will not return null.
 	 * 
 	 * @return array of the web library dependent modules
 	 */
 	private IVirtualReference[] getWebLibModules(J2EEModuleVirtualComponent comp) {
 		List result = new ArrayList();
 		IVirtualReference[] refComponents = comp.getNonManifestReferences();
 		// Check the deployed path to make sure it has a lib parent folder and matchs the web.xml
 		// base path
 		for (int i = 0; i < refComponents.length; i++) {
 			if (refComponents[i].getRuntimePath().equals(WEBLIB))
 				result.add(refComponents[i]);
 		}
 		return (IVirtualReference[]) result.toArray(new IVirtualReference[result.size()]);
 	}
 	
 	/*
 	 * Add any classpath component dependencies from this component
 	 */
 	private void addReferencedComponentClasspathDependencies(final IVirtualComponent component, final boolean web) {
 		final IVirtualReference[] refs = component.getReferences();
 		final Set absolutePaths = new HashSet();
 		for (int i = 0; i < refs.length; i++) {
 			final IVirtualReference reference = refs[i];
 			final IPath runtimePath = reference.getRuntimePath();
 			final IVirtualComponent referencedComponent = reference.getReferencedComponent();
 			
 			// if we are adding to a web project, only process references with the /WEB-INF/lib runtime path
 			if (web && !runtimePath.equals(WEBLIB)) {
 				continue;
 			}
 
 			// if the reference cannot export dependencies, skip
 			if (!canExportClasspathComponentDependencies(referencedComponent)) {
 				continue;
 			}
 			
 			if (!referencedComponent.isBinary() && referencedComponent instanceof J2EEModuleVirtualComponent) {
 				final IVirtualReference[] cpRefs = ((J2EEModuleVirtualComponent) referencedComponent).getJavaClasspathReferences();
 				for (int j = 0; j < cpRefs.length; j++) {
 					final IVirtualReference cpRef = cpRefs[j];
 
 					IPath cpRefRuntimePath = cpRef.getRuntimePath();
 					// only process references with ../ mapping
 					if (cpRefRuntimePath.equals(IClasspathDependencyConstants.RUNTIME_MAPPING_INTO_CONTAINER_PATH)) {
 						// runtime path within deployed app will be runtime path of parent component
 						cpRefRuntimePath = runtimePath;
 					} 
 					if (cpRef.getReferencedComponent() instanceof VirtualArchiveComponent) {
 						// want to avoid adding dups
 						final IPath absolutePath = ClasspathDependencyUtil.getClasspathVirtualReferenceLocation(cpRef);
 						if (absolutePaths.contains(absolutePath)) {
 							// have already added a member for this archive
 							continue;
 						} else {
 							addUtilMember(component, cpRef, cpRefRuntimePath);
 							absolutePaths.add(absolutePath);
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	private boolean canExportClasspathComponentDependencies(IVirtualComponent component) {
 		final IProject project = component.getProject();
 		// check for valid project type
 		if (J2EEProjectUtilities.isEJBProject(project) 
 				|| J2EEProjectUtilities.isDynamicWebProject(project)
 				|| J2EEProjectUtilities.isJCAProject(project)
     			|| J2EEProjectUtilities.isUtilityProject(project)) {
 			return true;
 		}
 		return false;
 	}
 	
 	private void saveClasspathDependencyURIs(IVirtualComponent component) {
 		if (!component.isBinary() && component instanceof J2EEModuleVirtualComponent) {
 			final IVirtualReference[] cpRefs = ((J2EEModuleVirtualComponent) component).getJavaClasspathReferences();
 			for (int j = 0; j < cpRefs.length; j++) {
 				final IVirtualReference cpRef = cpRefs[j];
 				// if we are adding to an EAR project, only process references with the root mapping
 				if (!cpRef.getRuntimePath().equals(IClasspathDependencyConstants.RUNTIME_MAPPING_INTO_CONTAINER_PATH)) {
 					// fails the runtime path test
 					continue;
 				}
 				if (cpRef.getReferencedComponent() instanceof VirtualArchiveComponent) {
 					classpathComponentDependencyURIs.add(cpRef.getArchiveName());
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Returns <code>true</code> if this module has a simple structure based on a
 	 * single root folder, and <code>false</code> otherwise.
 	 * <p>
 	 * In a single root structure, all files that are contained within the root folder
 	 * are part of the module, and are already in the correct module structure. No
 	 * module resources exist outside of this single folder.
 	 * 
 	 * For J2EE, this method will check if the project is already in J2EE spec standard output form.  
 	 * The project must follow certain rules, but in general, the project's content roots must be source folders
 	 * and the output folder must also be the the content root folder.
 	 * </p>
 	 * 
 	 * @return <code>true</code> if this module has a single root structure, and
 	 *    <code>false</code> otherwise
 	 */
 	public boolean isSingleRootStructure() {
 		StructureEdit edit = null;
 		try {
 			edit = StructureEdit.getStructureEditForRead(getProject());
 			if (edit == null || edit.getComponent() == null)
 				return false;
 			WorkbenchComponent wbComp = edit.getComponent();
 			List resourceMaps = wbComp.getResources();
 			
 			if (J2EEProjectUtilities.isEARProject(getProject())) {
 				// Always return false for EARs so that members for EAR are always calculated and j2ee modules
 				// are filtered out
 				return false;
 			} else if (J2EEProjectUtilities.isDynamicWebProject(getProject())) {
 				// If there are any web lib jar references, this is not a standard project
 				IVirtualReference[] references = ((J2EEModuleVirtualComponent)component).getNonManifestReferences();
 				for (int i=0; i<references.length; i++) {
 					if (references[i].getReferencedComponent().isBinary())
 						return false;
 				}
 				// Ensure there are only basic component resource mappings -- one for the content folder 
 				// and any for src folders mapped to WEB-INF/classes
 				if (hasDefaultWebResourceMappings(resourceMaps)) {
 					// Verify only one java output folder
 					if (getJavaOutputFolders().length==1) {
 						// Verify the java output folder is to <content root>/WEB-INF/classes
 						IPath javaOutputPath = getJavaOutputFolders()[0].getProjectRelativePath();
 						IPath compRootPath = component.getRootFolder().getUnderlyingFolder().getProjectRelativePath();
 						if (compRootPath.append(J2EEConstants.WEB_INF_CLASSES).equals(javaOutputPath)) 
 							return true;
 					}
 				}
 				return false;
 			} else if (J2EEProjectUtilities.isEJBProject(getProject()) || J2EEProjectUtilities.isJCAProject(getProject())
 					|| J2EEProjectUtilities.isApplicationClientProject(getProject()) || J2EEProjectUtilities.isUtilityProject(getProject())) {
 				// Ensure there are only source folder component resource mappings to the root content folder
 				if (isRootResourceMapping(resourceMaps,false)) {
 					// Verify only one java outputfolder
 					if (getJavaOutputFolders().length==1) {
 						// At this point for utility projects, this project is optimized, we can just use the output folder
 						if (J2EEProjectUtilities.isUtilityProject(getProject()))
 							return true;
 						// Verify the java output folder is the same as one of the content roots
 						IPath javaOutputPath = getJavaOutputFolders()[0].getProjectRelativePath();
 						IContainer[] rootFolders = component.getRootFolder().getUnderlyingFolders();
 						for (int i=0; i<rootFolders.length; i++) {
 							IPath compRootPath = rootFolders[i].getProjectRelativePath();
 							if (javaOutputPath.equals(compRootPath))
 								return true;
 						}
 					}
 				}
 				return false;
 			}
 			return true;
 		} finally {
 			if (edit !=null)
 				edit.dispose();
 		}
 	}
 	
 	/**
 	 * Ensure that any component resource mappings are for source folders and 
 	 * that they map to the root content folder
 	 * 
 	 * @param resourceMaps
 	 * @return boolean
 	 */
 	private boolean isRootResourceMapping(List resourceMaps, boolean isForEAR) {
 		// If the list is empty, return false
 		if (resourceMaps.size()<1)
 			return false;
 		
 		for (int i=0; i<resourceMaps.size(); i++) {
 			ComponentResource resourceMap = (ComponentResource) resourceMaps.get(i);
 			// Verify it maps to "/" for the content root
 			if (!resourceMap.getRuntimePath().equals(Path.ROOT))
 				return false;
 			// If this is not for an EAR, verify it is also a src container
 			if (!isForEAR) {
 				IPath sourcePath = getProject().getFullPath().append(resourceMap.getSourcePath());
 				if (!isSourceContainer(sourcePath))
 					return false;
 			}
 		}
 		return true;
 	}
 	
 	/**
 	 * Checks if the path argument is to a source container for the project.
 	 * 
 	 * @param a workspace relative full path
 	 * @return is path a source container?
 	 */
 	private boolean isSourceContainer(IPath path) {
 		IPackageFragmentRoot[] srcContainers = getSourceContainers();
 		for (int i=0; i<srcContainers.length; i++) {
 			if (srcContainers[i].getPath().equals(path))
 				return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Ensure the default web setup is correct with one resource map and any number of java 
 	 * resource maps to WEB-INF/classes
 	 * 
 	 * @param resourceMaps
 	 * @return boolean
 	 */
 	private boolean hasDefaultWebResourceMappings(List resourceMaps) {
 		int rootValidMaps = 0;
 		int javaValidRoots = 0;
 		
 		// If there aren't at least 2 maps, return false
 		if (resourceMaps.size()<2)
 			return false;
 		
 		IPath webInfClasses = new Path(J2EEConstants.WEB_INF_CLASSES).makeAbsolute();
 		for (int i=0; i<resourceMaps.size(); i++) {
 			ComponentResource resourceMap = (ComponentResource) resourceMaps.get(i);
 			IPath sourcePath = getProject().getFullPath().append(resourceMap.getSourcePath());
 			
 			// Verify if the map is for the content root
 			if (resourceMap.getRuntimePath().equals(Path.ROOT)) {
 				rootValidMaps++;
 			// Verify if the map is for a java src folder and is mapped to "WEB-INF/classes"
 			} else if (resourceMap.getRuntimePath().equals(webInfClasses) && isSourceContainer(sourcePath)) {
 				javaValidRoots++;
 			// Otherwise we bail because we have a non optimized map
 			} else {
 				return false;
 			}
 		}
 		// Make sure only one of the maps is the content root, and that at least one is for the java folder
 		return rootValidMaps==1 && javaValidRoots>0;
 	}
 	
 	/**
 	 * This method is added for performance reasons.
 	 * It assumes the virtual component is not using any flexible features and is in a standard J2EE format
 	 * with one component root folder and an output folder the same as its content folder.  This will bypass 
 	 * the virtual component API and just return the module resources as they are on disk.
 	 * 
 	 * @return array of ModuleResources
 	 * @throws CoreException
 	 */
 	private IModuleResource[] getOptimizedMembers() throws CoreException {
 		if (component != null) {
 			// For java utility modules, we can just use the output container, at this point we know there is only one
 			if (J2EEProjectUtilities.isUtilityProject(getProject())) {
 				return getModuleResources(Path.EMPTY, getJavaOutputFolders()[0]);
 			}
 			// For J2EE modules, we use the contents of the content root
 			else {
 				IVirtualFolder vFolder = component.getRootFolder();
 				return getModuleResources(Path.EMPTY, vFolder.getUnderlyingFolder());
 			}
 		}
 		return new IModuleResource[] {};
 	}
 	
 	/**
 	 * This method will return from cache or add to cache whether or not an output container
 	 * is mapped in the virtual component.
 	 * 
 	 * @param outputContainer
 	 * @return if output container is mapped
 	 */
 	private boolean isOutputContainerMapped(IContainer outputContainer) {
 		if (outputContainer == null)
 			return false;
 		
 		HashMap outputMaps = getCachedOutputMappings();
 		Boolean result = (Boolean) outputMaps.get(outputContainer);
 		if (result == null) {
 			// If there are any component resources for the container, we know it is mapped
 			if (ComponentCore.createResources(outputContainer).length > 0)
 				result = Boolean.TRUE;	
 			// Otherwise it is not mapped
 			else
 				result = Boolean.FALSE;
 			// Cache the result in the map for this output container
 			outputMaps.put(outputContainer, result);
 		}
 		return result.booleanValue();
 	}
 	
 	/**
 	 * Lazy initialize the cached output mappings
 	 * @return HashMap
 	 */
 	private HashMap getCachedOutputMappings() {
 		if (cachedOutputMappings==null)
 			cachedOutputMappings = new HashMap();
 		return cachedOutputMappings;
 	}
 	
 	/**
 	 * Lazy initialize the cached source - output pairings
 	 * @return HashMap
 	 */
 	private HashMap getCachedSourceOutputPairs() {
 		if (cachedSourceOutputPairs==null)
 			cachedSourceOutputPairs = new HashMap();
 		return cachedSourceOutputPairs;
 	}
 	
 	/**
 	 * This file should be added to the members list from the virtual component maps only if:
 	 * a) it is not in a source folder
 	 * b) it is in a source folder, and the corresponding output folder is a mapped component resource
 	 * 
 	 * @return boolean should file be added to members
 	 */
 	protected boolean shouldAddComponentFile(IFile file) {
 		IPackageFragmentRoot sourceContainer = getSourceContainer(file);
 		// If the file is not in a source container, return true
 		if (sourceContainer==null) {
 			return true;
 		// Else if it is a source container and the output container is mapped in the component, return true
 		// Otherwise, return false.
 		} else {
 			IContainer outputContainer = getOutputContainer(sourceContainer);
 			return outputContainer!=null && isOutputContainerMapped(outputContainer);		
 		}
 	}
 }
