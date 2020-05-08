 /*******************************************************************************
  * Copyright (c) 2007 BEA Systems, Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * BEA Systems, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.classpathdep;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.jdt.core.IClasspathAttribute;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jst.common.jdt.internal.javalite.IJavaProjectLite;
 import org.eclipse.jst.common.jdt.internal.javalite.JavaCoreLite;
 import org.eclipse.jst.j2ee.componentcore.J2EEModuleVirtualComponent;
 import org.eclipse.jst.j2ee.internal.J2EEVersionConstants;
 import org.eclipse.jst.j2ee.internal.classpathdep.ClasspathDependencyEnablement;
 import org.eclipse.jst.j2ee.internal.classpathdep.ClasspathDependencyValidator;
 import org.eclipse.jst.j2ee.internal.classpathdep.ClasspathDependencyVirtualComponent;
 import org.eclipse.jst.j2ee.internal.classpathdep.ClasspathDependencyValidator.ClasspathDependencyValidatorData;
 import org.eclipse.jst.j2ee.model.ModelProviderManager;
 import org.eclipse.jst.j2ee.project.EarUtilities;
 import org.eclipse.jst.j2ee.project.JavaEEProjectUtilities;
 import org.eclipse.jst.j2ee.project.WebUtilities;
 import org.eclipse.jst.javaee.application.Application;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.internal.impl.ModuleURIUtil;
 import org.eclipse.wst.common.componentcore.internal.resources.VirtualArchiveComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
 import org.eclipse.wst.validation.internal.provisional.core.IMessage;
 
 /**
  * Contains utility code for working manipulating the WTP classpath component
  * dependency attribute.
  */
 public class ClasspathDependencyUtil implements IClasspathDependencyConstants {
 	
 	/**
 	 * This is equivalent to calling getRawComponentClasspathDependencies(javaProject, DependencyAttributeType.CLASSPATH_COMPONENT_DEPENDENCY);
 	 * 
 	 * @deprecated use {@link #getRawComponentClasspathDependencies(IJavaProject, org.eclipse.jst.j2ee.classpathdep.IClasspathDependencyConstants.DependencyAttributeType)}
 	 * 
 	 * @param javaProject
 	 * @return
 	 * @throws CoreException
 	 */
 	@Deprecated
 	public static Map <IClasspathEntry, IClasspathAttribute> getRawComponentClasspathDependencies(final IJavaProject javaProject) throws CoreException {
 		return getRawComponentClasspathDependencies(javaProject, DependencyAttributeType.CLASSPATH_COMPONENT_DEPENDENCY);
 	}
 	
 	/**
 	 * @deprecated use {@link #getRawComponentClasspathDependencies(IJavaProjectLite, org.eclipse.jst.j2ee.classpathdep.IClasspathDependencyConstants.DependencyAttributeType)}
 	 * @param javaProject
 	 * @param attributeType
 	 * @return
 	 * @throws CoreException
 	 */
 	@Deprecated
 	public static Map <IClasspathEntry, IClasspathAttribute> getRawComponentClasspathDependencies(final IJavaProject javaProject, DependencyAttributeType attributeType) throws CoreException {
 		return getRawComponentClasspathDependencies(JavaCoreLite.create(javaProject), attributeType);
 	}
 	/**
 	 * Returns all unresolved classpath entries for the specified Java project that
 	 * have the special WTP classpath component dependency attribute.
 	 *  
 	 * @param javaProjectLite Java project whose component classpath dependencies are being retrieved.
 	 * @parem attributeType the attribute to search for
 	 * @return Map from IClasspathEntry to IClasspathAttribute for classpath component dependency.
 	 * @return IClasspathEntries with the special component dependency attribute.
 	 * @throws CoreException Thrown if an error is encountered accessing the unresolved classpath.
 	 */
 	public static Map <IClasspathEntry, IClasspathAttribute> getRawComponentClasspathDependencies(final IJavaProjectLite javaProjectLite, DependencyAttributeType attributeType) throws CoreException {
 		return getRawComponentClasspathDependencies(javaProjectLite, attributeType, false);
 	}
 	public static Map <IClasspathEntry, IClasspathAttribute> getRawComponentClasspathDependencies(final IJavaProjectLite javaProjectLite, DependencyAttributeType attributeType, final boolean webLibsOnly) throws CoreException {
 		if (javaProjectLite == null || (!webLibsOnly && !ClasspathDependencyEnablement.isAllowClasspathComponentDependency())) {
 			return Collections.emptyMap();
 		}
 		final Map<IClasspathEntry, IClasspathAttribute> referencedRawEntries = new HashMap<IClasspathEntry, IClasspathAttribute>();
 		final IClasspathEntry[] entries = javaProjectLite.readRawClasspath();
         for (IClasspathEntry entry : entries) {
             final IClasspathAttribute attrib = checkForComponentDependencyAttribute(entry, attributeType, webLibsOnly);
             if (attrib != null) {
             	if(webLibsOnly) {
            		if(DependencyAttributeType.CLASSPATH_COMPONENT_NONDEPENDENCY == attributeType || WebUtilities.WEBLIB.toString().equals(attrib.getValue())){
             			referencedRawEntries.put(entry, attrib);
             		}
             	} else{
             		referencedRawEntries.put(entry, attrib);
             	}
             	
             }
         }
         return referencedRawEntries;
 	}
 	
 	/**
 	 * @deprecated use {@link #getPotentialComponentClasspathDependencies(IJavaProjectLite)}
 	 * @param javaProject
 	 * @return
 	 * @throws CoreException
 	 */
 	@Deprecated
 	public static List <IClasspathEntry> getPotentialComponentClasspathDependencies(final IJavaProject javaProject) throws CoreException {
 		return getPotentialComponentClasspathDependencies(JavaCoreLite.create(javaProject));
 	}
 	
 	/**
 	 * Retrieves the unresolved classpath entries for the specified Java project that
 	 * could potentially be mapped into the virtual component tree for the project via
 	 * the special WTP classpath component dependence attribute. Classpath entries that
 	 * currently have the attribute are not returned by this method (@see {@link #getRawComponentClasspathDependencies(IJavaProject, boolean)})
 	 * 
 	 * @param javaProjectLite Java project whose potential component classpath dependencies will be retrieved.
 	 * @return List of raw IClasspathEntries for potential classpath component dependencies.
 	 * @throws CoreException Thrown if an error is encountered. 
 	 */
 	public static List <IClasspathEntry> getPotentialComponentClasspathDependencies(final IJavaProjectLite javaProjectLite) throws CoreException {
 		return getPotentialComponentClasspathDependencies(javaProjectLite, false);
 	}		
 	public static List <IClasspathEntry> getPotentialComponentClasspathDependencies(final IJavaProjectLite javaProjectLite, final boolean webLibsOnly) throws CoreException {
 
 		final List <IClasspathEntry> potentialRawEntries = new ArrayList<IClasspathEntry>();
 
 		if (javaProjectLite == null || !javaProjectLite.getProject().isAccessible() || (!webLibsOnly && !ClasspathDependencyEnablement.isAllowClasspathComponentDependency())) {
 			return Collections.emptyList();
 		}
 		final IProject project = javaProjectLite.getProject();
 		final boolean isWebApp = JavaEEProjectUtilities.isDynamicWebProject(project);
 		final boolean isRAR = JavaEEProjectUtilities.isJCAProject(project);
 		final ClasspathDependencyValidatorData data = new ClasspathDependencyValidatorData(project);
 		final IClasspathEntry[] entries = javaProjectLite.readRawClasspath();
         for (int i = 0; i < entries.length; i++) {
             final IClasspathEntry entry = entries[i];
             final IClasspathAttribute attrib = checkForComponentDependencyAttribute(entry, DependencyAttributeType.DEPENDENCY_OR_NONDEPENDENCY, webLibsOnly);
             if (attrib != null) {
             	continue; // already has the attribute
             }
         	// check validation logic for entry
         	// always mark the "isWebApp" param as true so that we get both exported and non-exported entries; for non-web projects,
         	// want to let a user have the option to see and select the non-exported entries and then just generate a validation
         	// error if they happen to select one.
         	final IMessage[] msgs = ClasspathDependencyValidator.validateVirtualComponentEntry(entry, null, true, project, data);
         	boolean error = false;
         	for (int j = 0; j < msgs.length; j++) {
         		if (msgs[j].getSeverity() == IMessage.HIGH_SEVERITY) {
         			error = true;
         			break;
         		}
         	}
         	if (error) {
         		continue;
         	}
         	if (IClasspathEntry.CPE_LIBRARY == entry.getEntryKind()) {
 				final boolean isFile = !ClasspathDependencyUtil.isClassFolderEntry(entry);
 				if(webLibsOnly && !isFile){
 					continue; //directories do not ever work
 				}
 				if (isFile) {
 					boolean foundEntry = false;
 					IVirtualComponent component = ComponentCore.createComponent(project);
 					if (isWebApp) { // checks for web libs
 						IContainer[] webLibFolders = component.getRootFolder().getFolder(WEB_INF_LIB_PATH).getUnderlyingFolders();
 						for (IContainer webLib : webLibFolders) {
 							IPath webLibFolderPath = webLib.getFullPath();
 							if (webLibFolderPath.equals(entry.getPath().removeLastSegments(1))) {
 								foundEntry = true;
 								break;
 							}
 						}
 					} else if(isRAR){
 						IContainer [] rootFolders = component.getRootFolder().getUnderlyingFolders();
 						for(IContainer root: rootFolders){
 							IPath rootPath = root.getFullPath();
 							if(rootPath.isPrefixOf(entry.getPath())){
 								foundEntry = true;
 								break;
 							}
 						}
 					}
 					if (foundEntry) {
 						continue;
 					}
 					// checks for manifest references
 					List manifestRefs = J2EEModuleVirtualComponent.getManifestReferences(component, null);
 					if(manifestRefs != null){
 						for (int j = 0; j < manifestRefs.size(); j++) {
 							IVirtualReference ref = (IVirtualReference) manifestRefs.get(j);
 							IVirtualComponent c = ref.getReferencedComponent();
 							if (c.isBinary()) {
 								VirtualArchiveComponent archiveComponent = (VirtualArchiveComponent) c;
 								IFile file = archiveComponent.getUnderlyingWorkbenchFile();
 								if (file != null) {
 									if (file.getFullPath().equals(entry.getPath())) {
 										foundEntry = true;
 										break;
 									}
 								}
 							}
 						}
 						if (foundEntry) {
 							continue;
 						}
 					}
 					// checks for ear library-directory entries
 					IProject[] earProjects = EarUtilities.getReferencingEARProjects(project);
 					for (IProject earProject : earProjects) {
 						String earDDVersion = EarUtilities.getJ2EEDDProjectVersion(earProject);
 						if(!earDDVersion.equals(J2EEVersionConstants.VERSION_1_2_TEXT) && !earDDVersion.equals(J2EEVersionConstants.VERSION_1_3_TEXT) && !earDDVersion.equals(J2EEVersionConstants.VERSION_1_4_TEXT)) {
 							IVirtualComponent earComponent = ComponentCore.createComponent(earProject);
 							Application app = (Application) ModelProviderManager.getModelProvider(earComponent).getModelObject();
 							String libDir = app.getLibraryDirectory();
 							if (libDir == null) {
 								// lib is the default if no library-directory is set
 								libDir = "lib"; //$NON-NLS-1$
 							}
 							IContainer[] earLibFolders = earComponent.getRootFolder().getFolder(new Path(libDir)).getUnderlyingFolders();
 							for (IContainer earLib : earLibFolders) {
 								IPath earLibFolderPath = earLib.getFullPath();
 								if (earLibFolderPath.equals(entry.getPath().removeLastSegments(1))) {
 									foundEntry = true;
 									break;
 								}
 							}
 							if (foundEntry) {
 								break;
 							}
 						}
 					}
 					if (foundEntry) {
 						continue;
 					}
 				}
 			}
         	
         	// entry can potentially be tagged as a component dependency
         	potentialRawEntries.add(entry);
         }
         return potentialRawEntries;
 	}
 	
 	private static boolean isValid(final IClasspathEntry entry, final IClasspathAttribute attrib, final boolean isWebApp, final IProject project, final ClasspathDependencyValidatorData data) {
 		final IMessage[] msgs = ClasspathDependencyValidator.validateVirtualComponentEntry(entry, attrib, isWebApp, project, data);
 		boolean valid = true;
 		for (int j = 0; j < msgs.length; j++) {
 			if (msgs[j].getSeverity() == IMessage.HIGH_SEVERITY) {
 				valid = false;
 				break;
 			}
 		}
 		return valid;
 	}
 	
 	/**
 	 * Returns all resolved classpath entries for the specified Java project that
 	 * have one of the special WTP classpath component dependency attributes and pass the set of rules
 	 * that govern valid classpath dependencies.
 	 *  
 	 * @param javaProject Java project whose component classpath dependencies are being retrieved.
 	 * @param isWebApp True if the target project is associated with a web project.
 	 * @return Map from IClasspathEntry to IClasspathAttribute for classpath component dependencies.
 	 * @throws CoreException Thrown if an error is encountered accessing the unresolved classpath.
 	 */
 	public static Map <IClasspathEntry, IClasspathAttribute> getComponentClasspathDependencies(final IJavaProjectLite javaProjectLite, final boolean isWebApp) throws CoreException {
 		return getComponentClasspathDependencies(javaProjectLite, isWebApp, true);
 	}
 
 	/**
 	 * @deprecated use {@link #getComponentClasspathDependencies(IJavaProjectLite, boolean)}
 	 * @param javaProject
 	 * @param isWebApp
 	 * @return
 	 * @throws CoreException
 	 */
 	@Deprecated
 	public static Map <IClasspathEntry, IClasspathAttribute> getComponentClasspathDependencies(final IJavaProject javaProject, final boolean isWebApp) throws CoreException {
 		return getComponentClasspathDependencies(JavaCoreLite.create(javaProject), isWebApp);
 	}
 
 	/**
 	 * @deprecated use {@link #getComponentClasspathDependencies(IJavaProjectLite, boolean, boolean)}
 	 * @param javaProject
 	 * @param isWebApp
 	 * @param onlyValid
 	 * @return
 	 * @throws CoreException
 	 */
 	@Deprecated
 	public static Map  <IClasspathEntry, IClasspathAttribute> getComponentClasspathDependencies(final IJavaProject javaProject, final boolean isWebApp, final boolean onlyValid) throws CoreException {
 		return getComponentClasspathDependencies(JavaCoreLite.create(javaProject), isWebApp, onlyValid);
 	}
 
 	/**
 	 * Returns all resolved classpath entries for the specified Java project that
 	 * have one of the special WTP classpath component dependency attributes.
 	 *  
 	 * @param javaProject Java project whose component classpath dependencies are being retrieved.
 	 * @param isWebApp True if the target project is associated with a web project.
 	 * @param onlyValid If true, only valid dependencies will be returned. If false, the raw entry must be valid but the
 	 * resolved can be invalid. 
 	 * @return Map from IClasspathEntry to IClasspathAttribute for classpath component dependencies.
 	 * @throws CoreException Thrown if an error is encountered accessing the unresolved classpath.
 	 */
 	public static Map <IClasspathEntry, IClasspathAttribute> getComponentClasspathDependencies(final IJavaProjectLite javaProjectLite, final boolean isWebApp, final boolean onlyValid) throws CoreException {
 		if(!isWebApp && !ClasspathDependencyEnablement.isAllowClasspathComponentDependency()){
 			return Collections.emptyMap();
 		}
 		
 		final boolean webLibsOnly = isWebApp && !ClasspathDependencyEnablement.isAllowClasspathComponentDependency();
 		
 		final ClasspathDependencyValidatorData data = new ClasspathDependencyValidatorData(javaProjectLite.getProject());
 		
 		// get the raw entries
 		final Map<IClasspathEntry, IClasspathAttribute> referencedRawEntries = getRawComponentClasspathDependencies(javaProjectLite, DependencyAttributeType.CLASSPATH_COMPONENT_DEPENDENCY, webLibsOnly);
 		final Map<IClasspathEntry, IClasspathAttribute> validRawEntries = new HashMap<IClasspathEntry, IClasspathAttribute>();
 
 		// filter out non-valid referenced raw entries
 		final Iterator<IClasspathEntry> i = referencedRawEntries.keySet().iterator();
 		while (i.hasNext()) {
 			final IClasspathEntry entry = i.next();
 			final IClasspathAttribute attrib = referencedRawEntries.get(entry);
 			if (isValid(entry, attrib, isWebApp, javaProjectLite.getProject(), data)) {
 				validRawEntries.put(entry, attrib);
 			}
 		}
 
 		// if we have no valid raw entries, return empty map
 		if (validRawEntries.isEmpty()) {
         	return Collections.emptyMap();
 		}
 
 		// XXX Would like to replace the code below with use of a public JDT API that returns
 		// the raw IClasspathEntry for a given resolved IClasspathEntry (see see https://bugs.eclipse.org/bugs/show_bug.cgi?id=183995)
 		// The code must currently leverage IPackageFragmentRoot to determine this
 		// mapping and, because IPackageFragmentRoots do not maintain IClasspathEntry data, a prior
 		// call is needed to getResolvedClasspath() and the resolved IClasspathEntries have to be stored in a Map from IPath-to-IClasspathEntry to
 		// support retrieval using the resolved IPackageFragmentRoot
 		
 		// retrieve the resolved classpath
 		//TODO this call to javaProject needs to be removed.  Need to figure out what exactly this is attempting to do.
 		IJavaProject javaProject = JavaCore.create(javaProjectLite.getProject());
 		//TODO this call to javaProject needs to be removed.  Need to figure out what exactly this is attempting to do.
 		final IClasspathEntry[] entries = javaProject.getResolvedClasspath(true);
 		final Map <IPath, IClasspathEntry> pathToResolvedEntry = new HashMap<IPath, IClasspathEntry>();
 		
 		// store in a map from path to entry
 		for (int j = 0; j < entries.length; j++) {
 			pathToResolvedEntry.put(entries[j].getPath(), entries[j]);
 		}
 
 		final Map <IClasspathEntry, IClasspathAttribute> referencedEntries = new LinkedHashMap <IClasspathEntry, IClasspathAttribute>();
 		
 		// grab all IPackageFragmentRoots
 		
 		// TODO this ignores project cp entries; can easily add in the raw project cp entries, however, do not have a good way to 
 		// map project cp entries resolved from cp containers back to the corresponding raw entry (and thereby determine if the
 		// entry has the publish/export attribute)
 		//TODO this call to javaProject needs to be removed.  Need to figure out what exactly this is attempting to do.
 		final IPackageFragmentRoot[] roots = javaProject.getPackageFragmentRoots();
 		for (IPackageFragmentRoot root : roots) {
 			final IClasspathEntry rawEntry = root.getRawClasspathEntry();
 			
 			// is the raw entry valid?
 			IClasspathAttribute attrib = validRawEntries.get(rawEntry);
 			if (attrib == null) {
 				continue;
 			}
 			
 			final IPath pkgFragPath = root.getPath();
 			final IClasspathEntry resolvedEntry = pathToResolvedEntry.get(pkgFragPath);
 			final IClasspathAttribute resolvedAttrib = checkForComponentDependencyAttribute(resolvedEntry, DependencyAttributeType.DEPENDENCY_OR_NONDEPENDENCY, webLibsOnly);
 			// attribute for the resolved entry must either be unspecified or it must be the
 			// dependency attribute for it to be included
 			if (resolvedAttrib == null || resolvedAttrib.getName().equals(CLASSPATH_COMPONENT_DEPENDENCY)) {
 				// filter out resolved entry if it doesn't pass the validation rules
 				if (!onlyValid || isValid(resolvedEntry, resolvedAttrib != null?resolvedAttrib:attrib,isWebApp, javaProjectLite.getProject(), data)) {
 					if (resolvedAttrib != null) {
 						// if there is an attribute on the sub-entry, use that
 						attrib = resolvedAttrib;
 					}
 					referencedEntries.put(resolvedEntry, attrib);
 				}
 			} 
 		}
 		
         return referencedEntries;
 	}
 	
 	/**
 	 * Retrieves the location (as a absolute file system path) for the specified classpath entry.
 	 * @param entry Classpath entry. If null, returns null.
 	 * @return Absolute file system path.
 	 */
 	public static IPath getEntryLocation(final IClasspathEntry entry) {
 
 		if (entry == null) {
 			return null;
 		}
 		final IPath entryPath = entry.getPath();
 		IPath entryLocation = entryPath;
 		final IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(entryPath);		
 		if (resource != null) {
 			entryLocation = resource.getLocation();
 		}
 		return entryLocation;
 	}
 	
 	/**
 	 * Retrieves the IResource corresponding to the specified classpath entry or null if it does not represent a Workspace resource.
 	 * @param entry Classpath entry. If null, returns null.
 	 * @return IResource or null.
 	 */
 	public static IResource getEntryResource(final IClasspathEntry entry) {
 		if (entry == null) {
 			return null;
 		}
 		final IPath entryPath = entry.getPath();
 		return ResourcesPlugin.getWorkspace().getRoot().findMember(entryPath);
 	}
 	
 	/**
 	 * Checks if the specified IVirtualReference represents an project cp entry. If so, returns the underlying IProject, otherwise,
 	 * returns null.
 	 * @param ref The IVirtualReference
 	 * @return IProject referenced by the project cp entry or null if the specified reference is null or does not refer to 
 	 * a VirtualArchiveComponent with type VirtualArchiveComponent.CLASSPATHARCHIVETYPE that represents a project cp entry.
 	 */
 	public static IProject isClasspathProjectReference(final IVirtualReference ref) {
 		if (ref == null) {
 			return null;
 		}
 		if (ref.getReferencedComponent() instanceof VirtualArchiveComponent) {
 			final VirtualArchiveComponent comp = (VirtualArchiveComponent) ref.getReferencedComponent();
 			if (comp.getArchiveType().equals(VirtualArchiveComponent.CLASSPATHARCHIVETYPE) &&
 					comp.getUnderlyingDiskFile() == null) {
 				return comp.getProject();
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Checks if the specified classpath entry represents a class folder.
 	 * @param entry The entry to check.
 	 * @return True if it is a library entry that points to a class folder. False otherwise.
 	 */
 	public static boolean isClassFolderEntry(final IClasspathEntry entry) {
 		if (entry == null || entry.getEntryKind() != IClasspathEntry.CPE_LIBRARY) {
 			return false;
 		}
 		// does the path refer to a file or a folder?
 		final IPath entryPath = entry.getPath();
 		IPath entryLocation = entryPath;
 		final IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(entryPath);
 		if (resource != null) {
 			entryLocation = resource.getLocation();
 		}
 		boolean isFile = true; // by default, assume a jar file
 		if (entryLocation.toFile().isDirectory()) {
 			isFile = false;
 		}
 		return !isFile;
 	}
 
 	/**
 	 * Retrieves the location (as an absolute local file system path) for the classpath dependency represented
 	 * by the specified IVirtualReference. Will return null for a project cp entry.
 	 * @param ref The IVirtualReference
 	 * @return Absolute path in the local file system or null if the specified reference is null or does not refer to 
 	 * a VirtualArchiveComponent with type VirtualArchiveComponent.CLASSPATHARCHIVETYPE.
 	 */
 	public static IPath getClasspathVirtualReferenceLocation(final IVirtualReference ref) {
 		if (ref == null) {
 			return null;
 		}
 		if (ref.getReferencedComponent() instanceof VirtualArchiveComponent) {
 			final VirtualArchiveComponent comp = (VirtualArchiveComponent) ref.getReferencedComponent();
 			if (comp.getArchiveType().equals(VirtualArchiveComponent.CLASSPATHARCHIVETYPE)) {
 				java.io.File cpEntryFile = comp.getUnderlyingDiskFile();
 				IPath cpEntryPath = null;
 				if (cpEntryFile != null && cpEntryFile.exists()) {
 					cpEntryPath = new Path(cpEntryFile.getAbsolutePath());
                 } else {
                     final IFile iFile = comp.getUnderlyingWorkbenchFile();
                     if (iFile != null) {
                     	cpEntryPath = iFile.getLocation();
                     } else {
                     	IContainer container = getClassFolder(comp);
                     	if (container != null) {
                     		return container.getLocation();
                     	}
                     }
                 }
 				return cpEntryPath;
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Retrieves the runtime path to which the resolved classpath entry components will be
 	 * added within the deployed application.
 	 * @param attrib The IClasspathAttribute with the WTP classpath component dependency value. If null,
 	 * will return the default path.
 	 * @param isWebApp True for web projects, false otherwise.
 	 * @param isClassFolder True if the default value should be computed for a class folder. Ignored if calculating for
 	 * a valid IClasspathAttribute.
 	 * @return Runtime path. Will be null if the attribute is not a WTP classpath component dependency 
 	 * attribute.
 	 */
 	public static IPath getRuntimePath(final IClasspathAttribute attrib, final boolean isWebApp, final boolean isClassFolder) {
     	if (attrib != null && !attrib.getName().equals(CLASSPATH_COMPONENT_DEPENDENCY)) {
     		return null;
     	}
     	if (attrib == null || attrib.getValue()== null || attrib.getValue().length() == 0) {
     		return getDefaultRuntimePath(isWebApp, isClassFolder);
     	}
     	return new Path(attrib.getValue());
 	}
 
 	/**
 	 * Checks if the specified IVirtualReference represents a class folder that has been marked for publish/export.
 	 * @param ref IVirtualReference to test.
 	 * @return True if this is a publish/export class folder.
 	 */
 	public static boolean isClassFolderReference(final IVirtualReference ref) {
 		final IVirtualComponent comp = ref.getReferencedComponent();
 		// must refer to a ClasspathDependencyVirtualComponent
 		if (comp instanceof ClasspathDependencyVirtualComponent) {
 			final ClasspathDependencyVirtualComponent cpComp= (ClasspathDependencyVirtualComponent) comp;
 			return cpComp.isClassFolder();
 		}
 		return false;
 	}
 	
 	/**
 	 * Returns the container for the specified VirtualArchiveComponent or null if this reference does not match to a container.
 	 * @param comp IVirtualComponent.
 	 * @return IContainer for the class folder or null if this reference does not match a container.
 	 */
 	public static IContainer getClassFolder(final IVirtualComponent comp) {
 		if (comp instanceof ClasspathDependencyVirtualComponent) {
 			final ClasspathDependencyVirtualComponent cpComp= (ClasspathDependencyVirtualComponent) comp;
 			return cpComp.getClassFolder();
 		}
 		return null;
 	}
 	
 	/**
 	 * Retrieves the default runtime path to which the resolved classpath entry components will be
 	 * added within the deployed application. This method is only valid for non-class folder entries.
 	 * @param isWebApp True if the default runtime path for web apps should be returned, false otherwise.
 	 * @return The default runtime path. 
 	 */
 	public static IPath getDefaultRuntimePath(final boolean isWebApp) {
 		return getDefaultRuntimePath(isWebApp, false);
 	}
 	
 	/**
 	 * Retrieves the default runtime path to which the resolved classpath entry components will be
 	 * added within the deployed application.
 	 * @param isWebApp True if the default runtime path for web apps should be returned, false otherwise.
 	 * @param isClassFolder True if the path is a class folder.
 	 * @return The default runtime path. 
 	 */
 	public static IPath getDefaultRuntimePath(final boolean isWebApp, final boolean isClassFolder) {
 		if (isWebApp) {
 			return isClassFolder ? WEB_INF_CLASSES_PATH : WEB_INF_LIB_PATH;			
 		}
 		return isClassFolder ? RUNTIME_MAPPING_INTO_COMPONENT_PATH : RUNTIME_MAPPING_INTO_CONTAINER_PATH;
 	}
 	
 	/**
 	 * Retrieves the archive name for the specified classpath entry
 	 * @param entry The entry.
 	 * @return The archive name.
 	 */
 	public static String getArchiveName(final IClasspathEntry entry) {
 		if (entry == null) {
 			return null;
 		}
 		final boolean isClassFolder = isClassFolderEntry(entry);
 		if (isClassFolder) {
 			IResource resource = getEntryResource(entry);
 			if (resource == null) {
 				return getEntryLocation(entry).lastSegment();
 			}
 			return resource.getFullPath().toString();
 		}
 		final IPath entryLocation = getEntryLocation(entry);
 		return entryLocation.lastSegment();
 	}
 	
 
 	
 	/**
 	 * Checks if the specified IClasspathEntry has either of the special WTP component dependency
 	 * attributes that indicate it should be mapped into the virtual component for the associated project.
 	 * 
 	 * @param entry The IClasspathEntry.
 	 * @return The IClasspathAttribute that holds the special WTP attribute or null if one was not found.
 	 */
 	public static IClasspathAttribute checkForComponentDependencyAttribute(final IClasspathEntry entry) {
 		return checkForComponentDependencyAttribute(entry, DependencyAttributeType.DEPENDENCY_OR_NONDEPENDENCY);
 	}
 	
 	/**
 	 * Checks if the specified IClasspathEntry has one of the special WTP component dependency
 	 * attributes that indicate it should be mapped into the virtual component for the associated project.
 	 * 
 	 * @param entry The IClasspathEntry.
 	 * @param componentDependency Controls which type of dependency attribute should be checked for (or whether both should be checked).
 	 * @return The IClasspathAttribute that holds the special WTP attribute or null if one was not found.
 	 */
 	public static IClasspathAttribute checkForComponentDependencyAttribute(final IClasspathEntry entry, final DependencyAttributeType attributeType) {
 		return checkForComponentDependencyAttribute(entry, attributeType, false);
 	}
 	public static IClasspathAttribute checkForComponentDependencyAttribute(final IClasspathEntry entry, final DependencyAttributeType attributeType, final boolean webLibsOnly) {
 		if (entry == null || (!webLibsOnly && !ClasspathDependencyEnablement.isAllowClasspathComponentDependency())) {
 			return null;
 		}
 	    final IClasspathAttribute[] attributes = entry.getExtraAttributes();
 	    for (int i = 0; i < attributes.length; i++) {
 	    	final IClasspathAttribute attribute = attributes[i];
 	    	final String name = attribute.getName();
 	    	if (name.equals(CLASSPATH_COMPONENT_DEPENDENCY)) {
 	    		if (attributeType == DependencyAttributeType.DEPENDENCY_OR_NONDEPENDENCY
 	    				|| attributeType == DependencyAttributeType.CLASSPATH_COMPONENT_DEPENDENCY) {
 	    			return attribute;
 	    		}
 	    	} else if (name.equals(CLASSPATH_COMPONENT_NON_DEPENDENCY)) {
 	    		if (attributeType == DependencyAttributeType.DEPENDENCY_OR_NONDEPENDENCY
 	    				|| attributeType == DependencyAttributeType.CLASSPATH_COMPONENT_NONDEPENDENCY) {
 	    			return attribute;
 	    		}
 	    	}
 	    }
 	    return null;
 	}
 	
 	/**
 	 * Determines if the specified virtual component represents a classpath component dependency.
 	 * @param component Virtual component to test
 	 * @return True if a classpath component dependency, false otherwise.
 	 */
 	public static boolean isClasspathComponentDependency(final IVirtualComponent component) {
 		if (component == null) {
 			return false;
 		}
 		if (component instanceof VirtualArchiveComponent) {
 			final VirtualArchiveComponent archiveComp = (VirtualArchiveComponent) component;
 			return archiveComp.getArchiveType().equals(VirtualArchiveComponent.CLASSPATHARCHIVETYPE);
 		}
 		return false;
 	}
 	
 	/**
 	 * Retrieves the classpath component display string for the specified component.
 	 * @param component Component that represents a classpath component.
 	 * @return Display string.
 	 */
 	public static String getClasspathComponentDependencyDisplayString(final IVirtualComponent component) {
 		final URI archiveURI = URI.createURI(ModuleURIUtil.getHandleString(component));
 		return archiveURI.lastSegment();
 	}
 	
 }
