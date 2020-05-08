 /******************************************************************************
  * Copyright (c) 2009 Red Hat
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Rob Stryker - initial implementation
  ******************************************************************************/
 package org.eclipse.jst.j2ee.internal.modulecore.util;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jst.common.internal.modulecore.util.ArchiveManifest;
 import org.eclipse.jst.common.internal.modulecore.util.IJavaComponentDiscerner;
 import org.eclipse.jst.common.internal.modulecore.util.ManifestUtilities;
 import org.eclipse.jst.j2ee.project.EarUtilities;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualFile;
 import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
 import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
 
 /**
  * This class is meant to discover possible manifest entries for a parent / child combo.
  * This might be better implemented using flat virtual component model to make more 
  * accurate use of consumes, etc. But that can change later (as usual)
  */
 public class JEEManifestDiscerner implements IJavaComponentDiscerner {
 
 	public IProject[] findParentProjects(IProject child) {
 		return EarUtilities.getReferencingEARProjects(child);
 	}
 
 	public IVirtualReference[] findPossibleManifestEntries(
 			IProject parentProject, IProject childProject) {
 		if( EarUtilities.isEARProject(parentProject)) {
 			ArrayList<IVirtualReference> allPossible = findAllPossibleEntries(parentProject, childProject);
 			IVirtualReference[] alreadyReferenced = findCurrentManifestEntries(parentProject, childProject, allPossible);
 			allPossible.removeAll(Arrays.asList(alreadyReferenced));
 			return allPossible.toArray(new IVirtualReference[allPossible.size()]);
 		}
 		return new IVirtualReference[]{};
 	}
 	
 	public IVirtualReference[] findPossibleManifestEntries(
 			IProject parentProject, IProject childProject, IVirtualReference[] currentEntries) {
 		if( EarUtilities.isEARProject(parentProject)) {
 			ArrayList<IVirtualReference> allPossible = findAllPossibleEntries(parentProject, childProject);
 			allPossible.removeAll(Arrays.asList(currentEntries));
 			return allPossible.toArray(new IVirtualReference[allPossible.size()]);
 		}
 		return new IVirtualReference[]{};
 	}
 
 	
 	protected ArrayList<IVirtualReference> findAllPossibleEntries(IProject parentProject, IProject childProject) {
 		IVirtualComponent ear = ComponentCore.createComponent(parentProject);
 		IVirtualReference[] hardRefs = ear.getReferences();
 		IVirtualReference[] actual_tmp = trimEarHardRefs(childProject, hardRefs);
 		IVirtualReference[] actual_clean = cleanHardRefs(actual_tmp);
 		ArrayList<IVirtualReference> refs = new ArrayList<IVirtualReference>();
 		refs.addAll(Arrays.asList(actual_clean));
 		return refs;
 	}
 	
 	/**
 	 * Prune out unacceptable reference types
 	 * @param childProject
 	 * @param hardRefs
 	 * @return
 	 */
 	private IVirtualReference[] trimEarHardRefs(IProject childProject, IVirtualReference[] hardRefs) {
 		ArrayList<IVirtualReference> refs = new ArrayList<IVirtualReference>();
 		// We have to prune out self-references 
 		for( int i = 0; i < hardRefs.length; i++ ) {
 			// This is actually hard to make sure we're not adding ourself
			if((hardRefs[i].getReferencedComponent().isBinary() 
 					|| hardRefs[i].getDependencyType() == IVirtualReference.DEPENDENCY_TYPE_CONSUMES
					|| !hardRefs[i].getReferencedComponent().getProject().equals(childProject))
					&& hardRefs[i].getArchiveName().endsWith("jar")) { //$NON-NLS-1$ // Only jar's are legal in MANIFEST
 				refs.add(hardRefs[i]);
 			}
 		}
 		return refs.toArray(new IVirtualReference[refs.size()]);
 	}
 	
 	/**
 	 * Make sure the runtime path + archiveName is what should go in the manifest file 
 	 * if this reference is selected
 	 * 
 	 * @param original
 	 * @return
 	 */
 	private IVirtualReference[] cleanHardRefs(IVirtualReference[] original) {
 		IVirtualReference[] newRefs = new IVirtualReference[original.length];
 		for( int i = 0; i < newRefs.length; i++ ) {
 			newRefs[i] = ComponentCore.createReference(original[i].getEnclosingComponent(), 
 					original[i].getReferencedComponent(), original[i].getRuntimePath().makeRelative());
 			newRefs[i].setDependencyType(original[i].getDependencyType());
 			newRefs[i].setArchiveName(original[i].getArchiveName());
 		}
 		return newRefs;
 	}
 	
 
 	public IVirtualReference[] findCurrentManifestEntries(
 			IProject parentProject, IProject childProject) {
 		return findCurrentManifestEntries(parentProject, childProject, 
 				findAllPossibleEntries(parentProject, childProject));
 	}
 	
 	protected IVirtualReference[] findCurrentManifestEntries(
 			IProject parentProject, IProject childProject, ArrayList<IVirtualReference> allPossible2) {
 
 		ArrayList<IVirtualReference> allPossible = new ArrayList<IVirtualReference>();
 		allPossible.addAll(allPossible2);
 		
 		ArchiveManifest manifest = ManifestUtilities.getManifest(getManifestFile(childProject));
 		List<String> entries = Arrays.asList(manifest.getClassPathTokenized());
 		Iterator<IVirtualReference> i = allPossible.iterator();
 		IVirtualReference currentI;
 		
 		// Remove anything not in the manifest since it's not a current match
 		while(i.hasNext()) {
 			currentI = i.next();
 			String currentEntry = currentI.getRuntimePath().append(currentI.getArchiveName()).toString();
 			if( !entries.contains(currentEntry))
 				i.remove();
 		}
 		return allPossible.toArray(new IVirtualReference[allPossible.size()]);
 	}
 
 	protected IFile getManifestFile(IProject child) {
 		IVirtualComponent root = ComponentCore.createComponent(child);
 		IVirtualFolder rootFolder = root.getRootFolder();
 		IVirtualFile vf = rootFolder.getFile(new Path("META-INF/MANIFEST.MF")); //$NON-NLS-1$
 		if( vf.exists() )
 			return vf.getUnderlyingFile();
 		return null;
 	}
 	
 	/**
 	 * This is the usecase we'd like to deprecate, one day. One day.....
 	 * (Though konstantin says NO)
 	 * @param parentProject
 	 * @param hardRefs
 	 * @return
 	 */
 //	private IVirtualReference[] LEGACY_getClasspathDependencies(IProject parentProject, IVirtualReference[] hardRefs) {
 //		ArrayList<IVirtualReference> retval = new ArrayList<IVirtualReference>();
 //		ArrayList<IProject> seenProjects = new ArrayList<IProject>();
 //		seenProjects.add(parentProject);
 //		for( int i = 0; i < hardRefs.length; i++ ) {
 //			if( hardRefs[i].getReferencedComponent() instanceof IClasspathDependencyProvider) {
 //				// Already flagged classpaths
 //				final IClasspathDependencyProvider j2eeComp = (IClasspathDependencyProvider) hardRefs[i].getReferencedComponent();
 //				final IVirtualReference[] refs = j2eeComp.getJavaClasspathReferences();
 //				for( int j = 0; j < refs.length; j++ ) {
 //					if( !((IClasspathDependencyComponent)refs[j].getReferencedComponent()).isClassFolder()) {
 //						// Clean this reference's runtime path
 //						IPath newRuntimePath = refs[j].getRuntimePath();
 //						if( newRuntimePath.toString().startsWith("../")) //$NON-NLS-1$
 //							newRuntimePath = new Path(newRuntimePath.toString().substring(2)).makeRelative();
 //						IVirtualReference tmp = ComponentCore.createReference(refs[j].getEnclosingComponent(), 
 //								refs[j].getReferencedComponent(), newRuntimePath);
 //						tmp.setDependencyType(refs[j].getDependencyType());
 //						tmp.setArchiveName(refs[j].getArchiveName());
 //						retval.add(tmp);
 //					}
 //				}
 //			}
 //		}
 //		return retval.toArray(new IVirtualReference[retval.size()]);
 //	}
 //			// Now find *potential* entries
 //			IProject cp = hardRefs[i].getReferencedComponent().getProject();
 //			if( !seenProjects.contains(cp)) {
 //				seenProjects.add(cp);
 //				IJavaProjectLite lite = JavaCoreLite.create(cp);
 //				try {
 //					List<IClasspathEntry> entries = ClasspathDependencyUtil.getPotentialComponentClasspathDependencies(lite);
 //					Iterator<IClasspathEntry> k = entries.iterator();
 //					while(k.hasNext()) {
 //						IVirtualReference tmp = LEGACY_convertClasspathEntry(cp, k.next());
 //						if( tmp != null )
 //							retval.add(tmp);
 //					}
 //				} catch( CoreException ce ) {}
 //			}
 //		}
 
 	
 //	
 //	private IVirtualReference LEGACY_convertClasspathEntry(IProject project, IClasspathEntry entry) {
 //		IVirtualComponent tmp = ComponentCore.createComponent(project);
 //		final IPath entryLocation = ClasspathDependencyUtil.getEntryLocation(entry);
 //		if(entryLocation != null && !ClasspathDependencyUtil.isClassFolderEntry(entry)) {
 //            final IClasspathAttribute attrib = ClasspathDependencyUtil.checkForComponentDependencyAttribute(
 //            		entry, DependencyAttributeType.CLASSPATH_COMPONENT_DEPENDENCY, false);
 //            boolean isWebApp = J2EEProjectUtilities.isDynamicWebProject(project);
 //            IPath runtimePath = ClasspathDependencyUtil.getRuntimePath(attrib, isWebApp, false);
 //			String componentPath = VirtualArchiveComponent.CLASSPATHARCHIVETYPE
 //				+ IPath.SEPARATOR + entryLocation.toPortableString();
 //			IVirtualComponent entryComponent = new ClasspathDependencyVirtualComponent(
 //					project, componentPath, false);
 //			final IVirtualReference entryReference = ComponentCore.createReference(tmp, entryComponent, runtimePath);
 //			((VirtualReference) entryReference).setDerived(true);
 //			entryReference.setArchiveName(ClasspathDependencyUtil.getArchiveName(entry));
 //			return entryReference;
 //		}
 //		return null;
 //	}
 	
 }
