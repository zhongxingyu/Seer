 /*******************************************************************************
  * Copyright (c) 2007, 2013 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.jsp.core.internal.util;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jst.jsp.core.internal.Logger;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.ModuleCoreNature;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualFile;
 import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
 import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
 import org.eclipse.wst.common.componentcore.resources.IVirtualResource;
 import org.eclipse.wst.common.project.facet.core.IFacetedProject;
 import org.eclipse.wst.common.project.facet.core.IProjectFacet;
 import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
 
 /**
  * Wrapper class for all Facet-related calls. If the Facet or ModuleCore
  * bundles are not available, this class will not load, or if it does, its
  * methods will cause NoClassDefFoundErrors. This allows us to
  * compartmentalize the dependencies.
  * 
  */
 final class FacetModuleCoreSupportDelegate {
 	private static final String META_INF_RESOURCES = "META-INF/resources/"; //$NON-NLS-1$
 	private static final String SLASH = "/"; //$NON-NLS-1$
 
 	/**
 	 * Copied to avoid unneeded extra dependency (plus it's unclear why the
 	 * value is in that plug-in).
 	 * 
 	 * @see org.eclipse.wst.common.componentcore.internal.util.IModuleConstants.JST_WEB_MODULE
 	 */
 	private final static String JST_WEB_MODULE = "jst.web"; //$NON-NLS-1$
 	/**
 	 * @param project
 	 * @return the version of the JST Web facet, a default otherwise
 	 * @throws CoreException
 	 */
 	static float getDynamicWebProjectVersion(IProject project) {
 		if (project == null)
 			return FacetModuleCoreSupport.DEFAULT_SERVLET_VERSION;
 
 		// In the absence of any facet information, assume the highest level
 		float version = FacetModuleCoreSupport.DEFAULT_SERVLET_VERSION;
 		try {
 			IFacetedProject faceted = ProjectFacetsManager.create(project);
 			if (faceted != null && ProjectFacetsManager.isProjectFacetDefined(JST_WEB_MODULE)) {
 				IProjectFacet webModuleFacet = ProjectFacetsManager.getProjectFacet(JST_WEB_MODULE);
 				if (faceted.hasProjectFacet(webModuleFacet)) {
 					version = Float.parseFloat(faceted.getInstalledVersion(webModuleFacet).getVersionString());
 				}
 			}
 		}
 		catch (NumberFormatException e) {
 			Logger.logException(e);
 		}
 		catch (CoreException e) {
 			Logger.logException(e);
 		}
 		return version;
 	}
 
 	/**
 	 * @param path -
 	 *            the full path to a resource within the workspace
 	 * @return - the runtime path of the resource if one exists, null
 	 *         otherwise
 	 */
 	static IPath getRuntimePath(IPath path) {
 		if (path == null)
 			return null;
 
 		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(path.segment(0));
 
 		if (!ModuleCoreNature.isFlexibleProject(project))
 			return null;
 
 		IVirtualResource[] virtualResources = ComponentCore.createResources(ResourcesPlugin.getWorkspace().getRoot().getFile(path));
 		if (virtualResources != null && virtualResources.length > 0) {
 			return virtualResources[0].getRuntimePath();
 		}
 		return null;
 	}
 
 	/**
 	 * @param project
 	 * @return the IPath to the "root" of the web contents
 	 */
 	static IPath getWebContentRootPath(IProject project) {
 		if (project == null)
 			return null;
 
 		if (!ModuleCoreNature.isFlexibleProject(project))
 			return null;
 
 		IPath path = null;
 		IVirtualComponent component = ComponentCore.createComponent(project);
 		if (component != null && component.exists()) {
 			path = component.getRootFolder().getWorkspaceRelativePath();
 		}
 		return path;
 	}
 
 	/**
 	 * @param project
 	 * @return
 	 * @throws CoreException
 	 */
 	static boolean isDynamicWebProject(IProject project) {
 		if (project == null)
 			return false;
 		
 		try {
 			if (ProjectFacetsManager.isProjectFacetDefined(JST_WEB_MODULE)) {
 				IFacetedProject faceted = ProjectFacetsManager.create(project);
 				IProjectFacet webModuleFacet = ProjectFacetsManager.getProjectFacet(JST_WEB_MODULE);
 				if (faceted != null && faceted.hasProjectFacet(webModuleFacet)) {
 					return true;
 				}
 			}
 		}
 		catch (CoreException e) {
 			Logger.logException(e);
 		}
 		return false;
 	}
 
 	private static IPath resolveInReferenced(IProject project, IPath runtimeReference) {
 		IVirtualReference[] references = ComponentCore.createComponent(project).getReferences();
		for (int i = 0; i < references.length; i++) {
			IVirtualComponent referencedComponent = references[i].getReferencedComponent().getComponent();
			IPath referencedPathRoot = referencedComponent.getRootFolder().getWorkspaceRelativePath();
			/*
			 * See Servlet 3.0, section 4.6 ; this is the only referenced
			 * module/component type we support
			 */
			IPath resolved = referencedPathRoot.append(META_INF_RESOURCES).append(runtimeReference);
			if (resolved != null && referencedComponent.getProject().findMember(resolved.removeFirstSegments(1)) != null) {
				return resolved;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * @param basePath -
 	 *            the full path to a resource within the workspace
 	 * @param reference -
 	 *            the reference string to resolve
 	 * @return - the full path within the workspace that corresponds to the
 	 *         given reference according to the virtual pathing support
 	 */
 	static IPath resolve(IPath basePath, String reference) {
 		if (reference == null || basePath == null || basePath.segmentCount() == 0)
 			return null;
 
 		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(basePath.segment(0));
 
 		if (!ModuleCoreNature.isFlexibleProject(project))
 			return null;
 
 		if (basePath.segmentCount() > 1) {
 			IResource baseResource = ResourcesPlugin.getWorkspace().getRoot().findMember(basePath);
 			if (baseResource != null) {
 				IVirtualResource[] virtualResources = ComponentCore.createResources(baseResource);
 				for (int i = 0; i < virtualResources.length; i++) {
 					IPath referenceRuntimePath = null;
 					if (reference.startsWith(SLASH)) {
 						referenceRuntimePath = new Path(reference);
 					}
 					else {
 						IPath baseRuntimePath = virtualResources[i].getRuntimePath();
 						referenceRuntimePath = baseRuntimePath.removeLastSegments(1).append(reference);
 					}
 					
 					IVirtualFile virtualFile = ComponentCore.createFile(project, referenceRuntimePath);
 					if (virtualFile != null && virtualFile.exists()) {
 						IFile[] underlyingFiles = virtualFile.getUnderlyingFiles();
 						for (int j = 0; j < underlyingFiles.length; j++) {
 							if (underlyingFiles[j].isAccessible()) {
 								return underlyingFiles[j].getFullPath();
 							}
 						}
 						if (underlyingFiles.length > 0) {
 							return underlyingFiles[0].getFullPath();
 						}
 					}
 					else {
 						// http://bugs.eclipse.org/338751 
 						IVirtualFolder virtualFolder = ComponentCore.createFolder(project, referenceRuntimePath);
 						if (virtualFolder != null && virtualFolder.exists()) {
 							IContainer[] underlyingFolders = virtualFolder.getUnderlyingFolders();
 							for (int j = 0; j < underlyingFolders.length; j++) {
 								if (underlyingFolders[j].isAccessible()) {
 									return underlyingFolders[j].getFullPath();
 								}
 							}
 							if (underlyingFolders.length > 0) {
 								return underlyingFolders[0].getFullPath();
 							}
 						}
 						else {
 							// check assembled projects
 							return resolveInReferenced(project, referenceRuntimePath);
 						}
 					}
 				}
 			}
 		}
 		else {
 			IVirtualFile virtualFile = ComponentCore.createFile(project, new Path(reference));
 			if (virtualFile != null && virtualFile.exists()) {
 				return virtualFile.getUnderlyingFile().getFullPath();
 			}
 		}
 		return null;
 	}
 
 	static IPath[] getAcceptableRootPaths(IProject project) {
 		if (!ModuleCoreNature.isFlexibleProject(project)) {
 			return new IPath[]{project.getFullPath()};
 		}
 
 		IPath[] paths = null;
 		IVirtualFolder componentFolder = ComponentCore.createFolder(project, Path.ROOT);
 		if (componentFolder != null && componentFolder.exists()) {
 			IContainer[] workspaceFolders = componentFolder.getUnderlyingFolders();
 			paths = new IPath[workspaceFolders.length];
 			for (int i = 0; i < workspaceFolders.length; i++) {
 				paths[i] = workspaceFolders[i].getFullPath();
 			}
 		}
 		else {
 			paths = new IPath[]{project.getFullPath()};
 		}
 		return paths;
 	}
 
 	static IPath getDefaultRoot(IProject project) {
 		if (ModuleCoreNature.isFlexibleProject(project)) {
 			IVirtualFolder componentFolder = ComponentCore.createFolder(project, Path.ROOT);
 			if (componentFolder != null && componentFolder.exists()) {
 				return componentFolder.getWorkspaceRelativePath();
 			}
 		}
 		return null;
 	}
 
 	static IPath getRootContainerForPath(IProject project, IPath path) {
 		if (ModuleCoreNature.isFlexibleProject(project)) {
 			IVirtualFolder componentFolder = ComponentCore.createFolder(project, Path.ROOT);
 			if (componentFolder != null && componentFolder.exists()) {
 				IContainer[] workspaceFolders = componentFolder.getUnderlyingFolders();
 				for (int i = 0; i < workspaceFolders.length; i++) {
 					if (workspaceFolders[i].getFullPath().isPrefixOf(path)) {
 						return workspaceFolders[i].getFullPath();
 					}
 				}
 			}
 		}
 		return null;
 	}
 }
