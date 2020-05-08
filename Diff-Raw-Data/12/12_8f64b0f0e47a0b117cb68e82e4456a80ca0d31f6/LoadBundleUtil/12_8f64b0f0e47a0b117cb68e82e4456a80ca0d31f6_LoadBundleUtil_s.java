 /*******************************************************************************
  * Copyright (c) 2006 Sybase, Inc. and others.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sybase, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.jsf.common.ui.internal.utils;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IStorage;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IPackageFragment;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 
 /**
  * @author mengbo
  */
 public class LoadBundleUtil {
 
 	private LoadBundleUtil() {

 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jface.action.IAction#run()
 	 */
 	public static IStorage getLoadBundleResource(IProject project,
 			String baseName) throws CoreException {
 		if (project == null || baseName == null) {
 			return null;
 		}
 		IStorage loadBundleResource = null;
 		if (project.hasNature(JavaCore.NATURE_ID)) {
 			IJavaProject javaProject = JavaCore.create(project);
 			IFile sourceFile = getSourceFile(javaProject, baseName);
 			if (sourceFile == null || !sourceFile.exists()) {
 				loadBundleResource = getJarFile(javaProject, baseName);
 			} else {
 				loadBundleResource = sourceFile;
 			}
 		}
 
 		return loadBundleResource;
 	}
 
 	private static IFile getSourceFile(IJavaProject javaProject, String baseName)
 			throws JavaModelException {
 		IClasspathEntry[] classpathEntries = javaProject.getRawClasspath();
 		for (int i = 0; i < classpathEntries.length; i++) {
 			if (classpathEntries[i].getEntryKind() == IClasspathEntry.CPE_SOURCE) {
 				IPath path = classpathEntries[i].getPath().append(
 						getFilePath(baseName)).removeFirstSegments(1);
 				path = javaProject.getProject().getFullPath().append(path);
 				IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(
 						path);
 				if (file.exists()) {
 					return file;
 				}
 			}
 		}
 		return null;
 	}
 
 	private static IPath getFilePath(String baseName) {
 		IPath path = new Path(baseName.replace('.', '/'));//$NON-NLS-1$
 		path = path.addFileExtension("properties");//$NON-NLS-1$
 		return path;
 	}
 
 	private static IStorage getJarFile(IJavaProject javaProject, String baseName)
 			throws JavaModelException {
 		IClasspathEntry[] roots = javaProject.getRawClasspath();
 		for (int i = 0; i < roots.length; i++) {
 			if (roots[i].getEntryKind() != IClasspathEntry.CPE_LIBRARY) {
 				continue;
 			}
 
 			IPackageFragmentRoot[] packageFragmentRoots = javaProject
 					.findPackageFragmentRoots(roots[i]);
 			for (int j = 0; j < packageFragmentRoots.length; j++) {
 				String packageName = getPackageName(baseName);
 				Object[] resources = null;
 				if (packageName.length() == 0) {
 					resources = packageFragmentRoots[j].getNonJavaResources();
 				} else {
 					IPackageFragment fragment = packageFragmentRoots[j]
 							.getPackageFragment(getPackageName(baseName));
 					if (fragment != null && fragment.exists()) {
 						resources = fragment.getNonJavaResources();
 					}
 				}
 
 				if (resources != null && resources.length > 0) {
 					for (int k = 0; k < resources.length; k++) {
 						if (resources[k] instanceof IStorage) {
 							IStorage storage = (IStorage) resources[k];
 							if (getFileName(baseName).equalsIgnoreCase(
 									storage.getName())) {
 								return storage;
 							}
 						}
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	private static String getPackageName(String baseName) {
 		int index = baseName.lastIndexOf('.');//$NON-NLS-1$
 		if (index == -1) {
 			return "";//$NON-NLS-1$
		} else {
			return baseName.substring(0, index);
 		}
 	}
 
 	private static String getFileName(String baseName) {
 		int index = baseName.lastIndexOf('.');//$NON-NLS-1$
 		if (index == -1) {
 			return baseName + ".properties";
		} else {
			return baseName.substring(index + 1).concat(".properties");//$NON-NLS-1$
 		}
 	}
 
 }
