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
 package org.eclipse.jst.j2ee.componentcore;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jdt.core.IClasspathAttribute;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jst.common.internal.modulecore.IClasspathDependencyProvider;
 import org.eclipse.jst.common.internal.modulecore.util.ManifestUtilities;
 import org.eclipse.jst.common.jdt.internal.javalite.IJavaProjectLite;
 import org.eclipse.jst.common.jdt.internal.javalite.JavaCoreLite;
 import org.eclipse.jst.common.jdt.internal.javalite.JavaLiteUtilities;
 import org.eclipse.jst.j2ee.classpathdep.ClasspathDependencyUtil;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.util.ArchiveUtil;
 import org.eclipse.jst.j2ee.internal.J2EEConstants;
 import org.eclipse.jst.j2ee.internal.classpathdep.ClasspathDependencyEnablement;
 import org.eclipse.jst.j2ee.internal.classpathdep.ClasspathDependencyVirtualComponent;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEPlugin;
 import org.eclipse.jst.j2ee.project.EarUtilities;
 import org.eclipse.jst.j2ee.project.JavaEEProjectUtilities;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.internal.builder.IDependencyGraph;
 import org.eclipse.wst.common.componentcore.internal.resources.VirtualArchiveComponent;
 import org.eclipse.wst.common.componentcore.internal.resources.VirtualComponent;
 import org.eclipse.wst.common.componentcore.internal.resources.VirtualFolder;
 import org.eclipse.wst.common.componentcore.internal.resources.VirtualReference;
 import org.eclipse.wst.common.componentcore.internal.util.IComponentImplFactory;
 import org.eclipse.wst.common.componentcore.internal.util.VirtualReferenceUtilities;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
 import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
 
 public class J2EEModuleVirtualComponent extends VirtualComponent implements IComponentImplFactory, IClasspathDependencyProvider {
 
 	public static String GET_JAVA_REFS = "GET_JAVA_REFS"; //$NON-NLS-1$
 	public static String GET_FUZZY_EAR_REFS = "GET_FUZZY_EAR_REFS"; //$NON-NLS-1$
 	
 	private long depGraphModStamp;
 	
 	private IVirtualReference[] hardReferences = null;
 	private IVirtualReference[] javaReferences = null;
 	private IVirtualReference[] parentEarManifestReferences = null;
 	private IVirtualReference[] fuzzyEarManifestReferences = null;
 	
 	public J2EEModuleVirtualComponent() {
 		super();
 	}
 
 	public J2EEModuleVirtualComponent(IProject aProject, IPath aRuntimePath) {
 		super(aProject, aRuntimePath);
 	}
 
 	public IVirtualComponent createComponent(IProject aProject) {
 		return new J2EEModuleVirtualComponent(aProject, new Path("/")); //$NON-NLS-1$
 	}
 
 	public IVirtualComponent createArchiveComponent(IProject aProject, String archiveLocation, IPath aRuntimePath) {
 		return new J2EEModuleVirtualArchiveComponent(aProject, archiveLocation, aRuntimePath);
 	}
 	
 	public IVirtualFolder createFolder(IProject aProject, IPath aRuntimePath) {
 		return new VirtualFolder(aProject, aRuntimePath);
 	}
 	
 	/**
 	 * Retrieves all references except those computed dynamically from
 	 * tagged Java classpath entries.
 	 * @return IVirtualReferences for all non-Java classpath entry references.
 	 */
 	public IVirtualReference[] getNonJavaReferences() {
 		return getReferences(false,false);
 	}
 
 	protected IVirtualReference[] getHardReferences() {
 		if (hardReferences == null || !checkIfStillValid()) {
 			Map<String, Object> map = new HashMap<String, Object>();
 			map.put(REQUESTED_REFERENCE_TYPE, HARD_REFERENCES);
 			hardReferences = super.getReferences(map);
 		}
 		return hardReferences;
 	}
 
 	protected static IVirtualReference[] getHardReferences(
 			IVirtualComponent component) {
 		Map<String, Object> map = new HashMap<String, Object>();
 		map.put(REQUESTED_REFERENCE_TYPE, HARD_REFERENCES);
 		return component.getReferences(map);
 	}
 
 	public IVirtualReference[] getJavaClasspathReferences() {
 		//broken by cache
 		//if (javaReferences == null || !checkIfStillValid())
 			javaReferences = getJavaClasspathReferences(getHardReferences());
 		return javaReferences;
 	}
 
 	@Override
 	public IVirtualReference[] getReferences(Map<String, Object> options) {
 		Object val = options.get(REQUESTED_REFERENCE_TYPE);
 		if( val != null ) {
 			if( HARD_REFERENCES.equals(val) || NON_DERIVED_REFERENCES.equals(val) || DISPLAYABLE_REFERENCES.equals(val))
 				return getHardReferences();
 			if( FLATTENABLE_REFERENCES.equals(val))
 				return getNonManifestReferences();
 		}
 		
 		Boolean objGetJavaRefs = (Boolean)options.get(GET_JAVA_REFS);
 		Boolean objGetFuzzyEarRefs = (Boolean)options.get(GET_FUZZY_EAR_REFS);
 		boolean getJavaRefs = objGetJavaRefs != null ? objGetJavaRefs.booleanValue() : true;
 		boolean findFuzzyEARRefs = objGetFuzzyEarRefs != null ? objGetFuzzyEarRefs.booleanValue() : false;
 		IVirtualReference[] cachedReferences = getReferences(getJavaRefs,findFuzzyEARRefs);
 		return cachedReferences;
 	}
 
 	@Override
 	public IVirtualReference[] getReferences() {
 		return getReferences(true, false);
 	}
 
 	public IVirtualReference[] getReferences(final boolean getJavaRefs,
 			final boolean findFuzzyEARRefs) {
 		ArrayList<IVirtualReference> all = new ArrayList<IVirtualReference>();
 		IVirtualReference[] hardRefs = getHardReferences();
 		all.addAll(Arrays.asList(hardRefs));
 		if (getJavaRefs)
 			all.addAll(Arrays.asList(getJavaClasspathReferences(hardRefs)));
 		
 		// retrieve the dynamic references specified via the MANIFEST.MF classpath
 		cacheManifestReferences();
 		all.addAll(Arrays.asList(parentEarManifestReferences));
 		if (findFuzzyEARRefs)
 			all.addAll(Arrays.asList(fuzzyEarManifestReferences));
 		IVirtualReference[] retVal = all.toArray(new IVirtualReference[all.size()]);
 		VirtualReferenceUtilities.INSTANCE.ensureReferencesHaveNames(retVal);
 		return retVal;
 	}
 
 	/**
 	 * Non-manifest references are hard references *OR* java classpath
 	 * references
 	 * 
 	 * @return
 	 */
 	public IVirtualReference[] getNonManifestReferences() {
 		ArrayList<IVirtualReference> allRefs = new ArrayList<IVirtualReference>();
 		IVirtualReference[] hardRefs = getHardReferences();
 		allRefs.addAll(Arrays.asList(hardRefs));
 		allRefs.addAll(Arrays.asList(getJavaClasspathReferences(hardRefs)));
 		return allRefs.toArray(new IVirtualReference[allRefs.size()]);
 	}
 
 	@Deprecated
 	public IVirtualReference[] getNonManifestReferences(
 			final boolean getJavaRefs) {
 		ArrayList<IVirtualReference> allRefs = new ArrayList<IVirtualReference>();
 		IVirtualReference[] hardRefs = getHardReferences();
 		allRefs.addAll(Arrays.asList(hardRefs));
 		if (getJavaRefs)
 			allRefs.addAll(Arrays.asList(getJavaClasspathReferences(hardRefs)));
 		return allRefs.toArray(new IVirtualReference[allRefs.size()]);
 	}
 
 	public static String[] getManifestClasspath(
 			IVirtualComponent moduleComponent) {
 		return ManifestUtilities.getManifestClasspath(moduleComponent,
 				new Path(J2EEConstants.MANIFEST_URI));
 	}
 
 	public IVirtualReference[] getJavaClasspathReferences(
 			IVirtualReference[] hardReferences) {
 		final boolean isWebApp = JavaEEProjectUtilities.isDynamicWebComponent(this);
 
 		if (!isWebApp && !ClasspathDependencyEnablement.isAllowClasspathComponentDependency())
 			return new IVirtualReference[0];
 
 		final IProject project = getProject();
 		final List cpRefs = new ArrayList();
 
 		try {
 			if (project == null || !project.isAccessible()
 					|| !project.hasNature(JavaCoreLite.NATURE_ID)) {
 				return new IVirtualReference[0];
 			}
 
 			final IJavaProjectLite javaProjectLite = JavaCoreLite.create(project);
 			if (javaProjectLite == null)
 				return new IVirtualReference[0];
 
 			// retrieve all referenced classpath entries
 			final Map referencedEntries = ClasspathDependencyUtil
 					.getComponentClasspathDependencies(javaProjectLite,isWebApp);
 
 			if (referencedEntries.isEmpty())
 				return new IVirtualReference[0];
 
 			IVirtualReference[] innerHardReferences = hardReferences == null ? 
 					getHardReferences() : hardReferences;
 			final IPath[] hardRefPaths = new IPath[innerHardReferences.length];
 			for (int j = 0; j < innerHardReferences.length; j++) {
 				final IVirtualComponent comp = innerHardReferences[j].getReferencedComponent();
 				if (comp.isBinary()) {
 					hardRefPaths[j] = (IPath)comp.getAdapter(IPath.class);
 				}
 			}
 
 			IContainer[] mappedClassFolders = null;
 			final Iterator i = referencedEntries.keySet().iterator();
 			while (i.hasNext()) {
 				final IClasspathEntry entry = (IClasspathEntry) i.next();
 				final IClasspathAttribute attrib = (IClasspathAttribute) referencedEntries
 						.get(entry);
 				final boolean isClassFolder = ClasspathDependencyUtil.isClassFolderEntry(entry);
 				final IPath runtimePath = ClasspathDependencyUtil.getRuntimePath(attrib, isWebApp, isClassFolder);
 				boolean add = true;
 				final IPath entryLocation = ClasspathDependencyUtil.getEntryLocation(entry);
 				if (entryLocation == null) {
 					// unable to retrieve location for cp entry, do not
 					// contribute as a virtual ref
 					add = false;
 				} else if (!isClassFolder) { // check hard archive refs
 					for (int j = 0; j < hardRefPaths.length; j++) {
 						if (entryLocation.equals(hardRefPaths[j])) {
 							// entry resolves to same file as existing hard
 							// reference, can skip
 							add = false;
 							break;
 						}
 					}
 				} else { // check class folders mapped in component file as
 					// class folders associated with mapped src folders
 					if (mappedClassFolders == null) {
 						List<IContainer> containers = JavaLiteUtilities
 								.getJavaOutputContainers(this);
 						mappedClassFolders = containers
 								.toArray(new IContainer[containers.size()]);
 					}
 					for (int j = 0; j < mappedClassFolders.length; j++) {
 						if (entryLocation.equals(mappedClassFolders[j]
 								.getFullPath())) {
 							// entry resolves to same file as existing class
 							// folder mapping, skip
 							add = false;
 							break;
 						}
 					}
 				}
 
 				if (add && entryLocation != null) {
 					String componentPath = null;
 					ClasspathDependencyVirtualComponent entryComponent = null;
 					/*
 					 * if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT)
 					 * { componentPath =
 					 * VirtualArchiveComponent.CLASSPATHARCHIVETYPE; final
 					 * IProject cpEntryProject =
 					 * ResourcesPlugin.getWorkspace().getRoot
 					 * ().getProject(entry.getPath().lastSegment());
 					 * entryComponent = (VirtualArchiveComponent)
 					 * ComponentCore.createArchiveComponent(cpEntryProject,
 					 * componentPath); } else {
 					 */
 					componentPath = VirtualArchiveComponent.CLASSPATHARCHIVETYPE
 							+ IPath.SEPARATOR + entryLocation.toPortableString();
 					entryComponent = new ClasspathDependencyVirtualComponent(
 							project, componentPath, isClassFolder);
 					// }
 					final IVirtualReference entryReference = ComponentCore
 							.createReference(this, entryComponent, runtimePath);
 					((VirtualReference) entryReference).setDerived(true);
 					entryReference.setArchiveName(ClasspathDependencyUtil
 							.getArchiveName(entry));
 					cpRefs.add(entryReference);
 				}
 			}
 
 		} catch (CoreException jme) {
 			J2EEPlugin.logError(jme);
 		}
 
 		return (IVirtualReference[]) cpRefs.toArray(new IVirtualReference[cpRefs.size()]);
 	}
 
 	private void cacheManifestReferences() {
 		if (parentEarManifestReferences == null
 				|| fuzzyEarManifestReferences == null) {
 			IVirtualReference[][] refs = calculateManifestReferences(this, true);
 			parentEarManifestReferences = refs[0];
 			fuzzyEarManifestReferences = refs[1];
 		}
 	}
 
 	private static IVirtualReference[][] calculateManifestReferences(
 			IVirtualComponent moduleComponent, boolean checkFuzzyRefs) {
 		String[] manifestClasspath = getManifestClasspath(moduleComponent);
 		IProject[] earProjects = EarUtilities
 				.getReferencingEARProjects(moduleComponent.getProject());
 		// Early aborts
 		if (manifestClasspath == null || manifestClasspath.length == 0
 				|| earProjects.length == 0) {
 			return new IVirtualReference[][] { new IVirtualReference[0], new IVirtualReference[0] };
 		}
 
 		// Get our found cache going
 		boolean[] foundRefAlready = new boolean[manifestClasspath.length];
 		for (int i = 0; i < foundRefAlready.length; i++)
 			foundRefAlready[i] = false;
 
 		// Get the true parent references
 		IProject firstEar = earProjects[earProjects.length - 1];
 		ArrayList<IVirtualReference> tmp = cacheOneEarProjectManifestRefs(
 				moduleComponent, firstEar, manifestClasspath, foundRefAlready);
 		IVirtualReference[] parentEarManifestReferences = tmp
 				.toArray(new IVirtualReference[tmp.size()]);
 
 		ArrayList<IVirtualReference> dynamicReferences = new ArrayList<IVirtualReference>();
 		// get the fuzzy references
 		if (checkFuzzyRefs) {
 			if (earProjects.length > 1) {
 				for (int earIndex = earProjects.length - 2; earIndex > -1; earIndex--) {
 					tmp = cacheOneEarProjectManifestRefs(moduleComponent,
 							firstEar, manifestClasspath, foundRefAlready);
 					dynamicReferences.addAll(tmp);
 				}
 			}
 		}
 		IVirtualReference[] fuzzyEarManifestReferences = dynamicReferences
 				.toArray(new IVirtualReference[dynamicReferences.size()]);
 
 		// return our two creatures
 		return new IVirtualReference[][] { parentEarManifestReferences,
 				fuzzyEarManifestReferences };
 	}
 
 	protected static ArrayList<IVirtualReference> cacheOneEarProjectManifestRefs(
 			IVirtualComponent moduleComponent, IProject earProject,
 			String[] manifestClasspath, boolean[] foundRefAlready) {
 		ArrayList<IVirtualReference> dynamicReferences = new ArrayList<IVirtualReference>();
 		IVirtualReference[] hardRefs = getHardReferences(moduleComponent);
 
 		IVirtualReference foundRef = null;
 		String earArchiveURI = null; // The URI for this archive in the EAR
 		boolean simplePath = false;
 		IVirtualReference[] earRefs = null;
 		IVirtualComponent tempEARComponent = ComponentCore
 				.createComponent(earProject);
 		IVirtualReference[] tempEarRefs = tempEARComponent.getReferences();
 		for (int j = 0; j < tempEarRefs.length && earRefs == null; j++) {
 			if (tempEarRefs[j].getReferencedComponent().equals(moduleComponent)) {
 				earRefs = tempEarRefs;
 				foundRef = tempEarRefs[j];
 				earArchiveURI = foundRef.getArchiveName();
 				simplePath = earArchiveURI != null ? earArchiveURI
 						.lastIndexOf("/") == -1 : true; //$NON-NLS-1$
 			}
 		}
 		if (null != earRefs) {
 			for (int manifestIndex = 0; manifestIndex < manifestClasspath.length; manifestIndex++) {
 				boolean found = false;
 				if (foundRefAlready != null && foundRefAlready[manifestIndex]) {
 					continue;
 				}
 				for (int j = 0; j < earRefs.length && !found; j++) {
 					if (foundRef != earRefs[j]) {
 						String archiveName = earRefs[j].getArchiveName();
 						if (null != archiveName) {
 							boolean shouldAdd = false;
 							String manifestEntryString = manifestClasspath[manifestIndex];
 							if (manifestEntryString != null) {
 								IPath manifestPath = new Path(manifestEntryString);
 								manifestEntryString = manifestPath
 										.toPortableString();
 							}
 
 							if (simplePath && manifestEntryString != null
 									&& manifestEntryString.lastIndexOf("/") == -1) { //$NON-NLS-1$
 								shouldAdd = archiveName.equals(manifestEntryString);
 							} else {
 								String earRelativeURI = ArchiveUtil
 										.deriveEARRelativeURI(manifestEntryString,
 												earArchiveURI);
 								if (null != earRelativeURI) {
									IPath earRefPath = earRefs[j].getRuntimePath().makeRelative();
									shouldAdd = earRelativeURI.equals(earRefPath.append(archiveName).toString());
 								}
 							}
 
 							if (shouldAdd) {
 								if (foundRefAlready != null) {
 									foundRefAlready[manifestIndex] = true;
 								}
 								found = true;
 								boolean shouldInclude = true;
 								IVirtualComponent dynamicComponent = earRefs[j]
 										.getReferencedComponent();
 								if (null != hardRefs) {
 									for (int k = 0; k < hardRefs.length
 											&& shouldInclude; k++) {
 										if (hardRefs[k].getReferencedComponent()
 												.equals(dynamicComponent)) {
 											shouldInclude = false;
 
 										}
 									}
 								}
 								if (shouldInclude) {
 									IVirtualReference dynamicReference = ComponentCore
 											.createReference(moduleComponent,
 													dynamicComponent);
 									((VirtualReference) dynamicReference)
 											.setDerived(true);
 									dynamicReferences.add(dynamicReference);
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 		return dynamicReferences;
 	}
 
 	public static List getManifestReferences(IVirtualComponent moduleComponent,
 			IVirtualReference[] hardReferences) {
 		return getManifestReferences(moduleComponent, hardReferences, false);
 	}
 
 	public static List getManifestReferences(IVirtualComponent moduleComponent,
 			IVirtualReference[] hardReferences, boolean findFuzzyEARRefs) {
 		IVirtualReference[][] refs = calculateManifestReferences(
 				moduleComponent, findFuzzyEARRefs);
 		ArrayList<IVirtualReference> tmp = new ArrayList<IVirtualReference>();
 		tmp.addAll(Arrays.asList(refs[0]));
 		if (findFuzzyEARRefs)
 			tmp.addAll(Arrays.asList(refs[1]));
 		return tmp;
 	}
 
 	private boolean checkIfStillValid() {
 		boolean valid = IDependencyGraph.INSTANCE.getModStamp() == depGraphModStamp;
 		if (!valid) {
 			clearCache();
 		}
 		return valid;
 	}
 
 	@Override
 	protected void clearCache() {
 		super.clearCache();
 		depGraphModStamp = IDependencyGraph.INSTANCE.getModStamp();
 		hardReferences = null;
 		javaReferences = null;
 		parentEarManifestReferences = null;
 		fuzzyEarManifestReferences = null;
 	}
 }
