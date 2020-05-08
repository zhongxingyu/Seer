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
 package org.eclipse.jst.j2ee.internal.archive.operations;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.common.util.WrappedException;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.jst.common.componentcore.util.ComponentUtilities;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.File;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.exception.ResourceLoadException;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.strategy.LoadStrategyImpl;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.util.ArchiveUtil;
 import org.eclipse.wst.common.componentcore.UnresolveableURIException;
 import org.eclipse.wst.common.componentcore.internal.ComponentResource;
 import org.eclipse.wst.common.componentcore.internal.StructureEdit;
 import org.eclipse.wst.common.componentcore.internal.impl.ModuleURIUtil;
 import org.eclipse.wst.common.componentcore.internal.impl.PlatformURLModuleConnection;
 import org.eclipse.wst.common.componentcore.internal.util.IModuleConstants;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualContainer;
 import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
 import org.eclipse.wst.common.componentcore.resources.IVirtualResource;
 import org.eclipse.wst.common.internal.emf.utilities.ExtendedEcoreUtil;
import org.eclipse.wst.common.internal.emfworkbench.WorkbenchResourceHelper;
 
 public abstract class ComponentLoadStrategyImpl extends LoadStrategyImpl {
 
 	protected IVirtualComponent vComponent;
 	protected boolean exportSource;
 
 	protected class FilesHolder {
 
 		private Map urisToFiles = new HashMap();
 		private Map urisToResources = new HashMap();
 		private Map resourcesToURI = new HashMap();
 		private Map urisToDiskFiles;
 
 		public void removeIFile(IFile file) {
 			String uri = (String) resourcesToURI.get(file);
 			remove(uri);
 		}
 
 		public void remove(String uri) {
 			urisToFiles.remove(uri);
 			Object resource = urisToResources.remove(uri);
 			if (resource != null) {
 				resourcesToURI.remove(resource);
 			}
 			if (urisToDiskFiles != null) {
 				urisToDiskFiles.remove(uri);
 			}
 		}
 
 
 		public void addFile(File file) {
 			String uri = file.getURI();
 			urisToFiles.put(uri, file);
 		}
 
 		public void addFile(File file, java.io.File externalDiskFile) {
 			String uri = file.getURI();
 			urisToFiles.put(uri, file);
 			if (null == urisToDiskFiles) {
 				urisToDiskFiles = new HashMap();
 			}
 			urisToDiskFiles.put(uri, externalDiskFile);
 		}
 
 		public void addFile(File file, IResource resource) {
 			String uri = file.getURI();
 			urisToFiles.put(uri, file);
 			urisToResources.put(uri, resource);
 		}
 
 		public InputStream getInputStream(String uri) throws IOException, FileNotFoundException {
 			java.io.File diskFile = null;
 
 			if (urisToDiskFiles != null && urisToDiskFiles.containsKey(uri)) {
 				diskFile = (java.io.File) urisToDiskFiles.get(uri);
 			} else {
 				IResource resource = (IResource) urisToResources.get(uri);
 				diskFile = new java.io.File(resource.getLocation().toOSString());
 			}
 			return new FileInputStream(diskFile);
 		}
 
 		public List getFiles() {
 			return new ArrayList(urisToFiles.values());
 		}
 
 		public boolean contains(String uri) {
 			return urisToFiles.containsKey(uri);
 		}
 	}
 
 	protected FilesHolder filesHolder;
 
 	public ComponentLoadStrategyImpl(IVirtualComponent vComponent) {
 		this.vComponent = vComponent;
 		filesHolder = new FilesHolder();
 	}
 
 	public boolean contains(String uri) {
 		IVirtualFolder rootFolder = vComponent.getRootFolder();
 		return rootFolder.getFile(new Path(uri)).exists();
 	}
 
 	protected void initializeResourceSet() {
		resourceSet = WorkbenchResourceHelper.getResourceSet(vComponent.getProject());
 	}
 
 	protected boolean primContains(String uri) {
 		return false;
 	}
 
 	public List getFiles() {
 		getSourceFiles();
 		getClassFiles();
 		return filesHolder.getFiles();
 	}
 
 	protected void getSourceFiles() {
 		try {
 			IVirtualFolder rootFolder = vComponent.getRootFolder();
 			IVirtualResource[] members = rootFolder.members();
 			getFiles(members);
 		} catch (CoreException e) {
 			Logger.getLogger().logError(e);
 		}
 	}
 
 	protected void getClassFiles() {
 		StructureEdit se = null;
 		try {
 			IPackageFragmentRoot[] sourceRoots = ComponentUtilities.getSourceContainers(vComponent);
 			se = StructureEdit.getStructureEditForRead(vComponent.getProject());
			IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
 			for (int i = 0; i < sourceRoots.length; i++) {
 				IPath outputPath = sourceRoots[i].getRawClasspathEntry().getOutputLocation();
 				if (outputPath != null) {
 					IFolder javaOutputFolder = ResourcesPlugin.getWorkspace().getRoot().getFolder(outputPath);
					int javaOutputSegmentCount = javaOutputFolder.getProjectRelativePath().segmentCount();
 					IPath runtimePath = null;
 					try {
 						ComponentResource[] componentResources = se.findResourcesBySourcePath(sourceRoots[i].getResource().getProjectRelativePath());
 						if (componentResources.length > 0) {
 							IPath tmpRuntimePath = componentResources[0].getRuntimePath();
 							IPath tmpSourcePath = componentResources[0].getSourcePath();
 							if (!tmpRuntimePath.equals(tmpSourcePath)) {
 								while (tmpSourcePath.segmentCount() > 0 && tmpRuntimePath.segmentCount() > 0 && tmpRuntimePath.lastSegment().equals(tmpSourcePath.lastSegment())) {
 									tmpRuntimePath = tmpRuntimePath.removeLastSegments(1);
 									tmpSourcePath = tmpSourcePath.removeLastSegments(1);
 								}
 								if(tmpRuntimePath.segmentCount() != 0){
 									runtimePath = tmpRuntimePath;
 								}
 							}
 						}
 					} catch (UnresolveableURIException e) {
 						Logger.getLogger().logError(e);
 					}
 					if (null == runtimePath) {
						runtimePath = new Path("");
 					}
 					
 					getOutputFiles(new IResource[]{javaOutputFolder}, runtimePath, javaOutputFolder.getProjectRelativePath().segmentCount());
 				}
 			}
 		} catch (CoreException e) {
 			Logger.getLogger().logError(e);
 		} finally {
 			if (se != null) {
 				se.dispose();
 			}
 		}
 	}
 
 
 	protected void getOutputFiles(IResource[] resources, final IPath runtimePathPrefix, int outputFolderSegmentCount) throws CoreException {
 		for (int i = 0; i < resources.length; i++) {
 			File cFile = null;
 			if (resources[i].getType() == IResource.FILE) {
 				// We have to avoid duplicates between the source and output folders (non-java
 				// resources)
 				IPath runtimePath = runtimePathPrefix.append(resources[i].getProjectRelativePath().removeFirstSegments(outputFolderSegmentCount));
 				String uri = runtimePath == null ? null : runtimePath.toString();
 				if (uri == null)
 					continue;
 				if (!shouldInclude(uri)) //$NON-NLS-1$
 					continue;
 				if (filesHolder.contains(uri))
 					continue;
 				cFile = createFile(uri);
 				cFile.setLastModified(getLastModified(resources[i]));
 				filesHolder.addFile(cFile, resources[i]);
 			} else if (shouldInclude((IContainer) resources[i])) {
 				IResource[] nestedResources = ((IContainer) resources[i]).members();
 				getOutputFiles(nestedResources, runtimePathPrefix, outputFolderSegmentCount);
 			}
 		}
 	}
 
 	protected void getFiles(IVirtualResource[] virtualResources) throws CoreException {
 		for (int i = 0; i < virtualResources.length; i++) {
 			File cFile = null;
 			if (virtualResources[i].getType() == IVirtualResource.FILE) {
 				// We have to avoid duplicates between the source and output folders (non-java
 				// resources)
 				IPath runtimePath = virtualResources[i].getRuntimePath();
 				String uri = runtimePath == null ? null : runtimePath.toString();
 				if (uri == null)
 					continue;
 				if (!shouldInclude(uri)) //$NON-NLS-1$
 					continue;
 				if (filesHolder.contains(uri))
 					continue;
				if (uri.charAt(0) == Path.SEPARATOR) {
 					uri = uri.substring(1);
 				}
 				cFile = createFile(uri);
 				IResource resource = virtualResources[i].getUnderlyingResource();
 				cFile.setLastModified(getLastModified(resource));
 				filesHolder.addFile(cFile, resource);
 			} else if (shouldInclude((IVirtualContainer) virtualResources[i])) {
 				IVirtualResource[] nestedVirtualResources = ((IVirtualContainer) virtualResources[i]).members();
 				getFiles(nestedVirtualResources);
 			}
 		}
 	}
 
 	protected long getLastModified(IResource aResource) {
 		return aResource.getLocation().toFile().lastModified();
 	}
 
 	public void setExportSource(boolean newExportSource) {
 		exportSource = newExportSource;
 	}
 
 	public boolean isExportSource() {
 		return exportSource;
 	}
 
	protected boolean shouldInclude(IContainer container) {
 		return true;
 	}
 
 	protected boolean shouldInclude(IVirtualContainer vContainer) {
 		return true;
 	}
 
 	protected boolean shouldInclude(String uri) {
 		return isExportSource() || !isSource(uri);
 	}
 
 	protected boolean isSource(String uri) {
 		if (uri == null)
 			return false;
 		return uri.endsWith(ArchiveUtil.DOT_JAVA) || uri.endsWith(ArchiveUtil.DOT_SQLJ);
 	}
 
 	protected void addExternalFile(String uri, java.io.File externalDiskFile) {
 		File aFile = getArchiveFactory().createFile();
 		aFile.setURI(uri);
 		aFile.setOriginalURI(uri);
 		aFile.setLoadingContainer(getContainer());
 		filesHolder.addFile(aFile, externalDiskFile);
 	}
 
 	public InputStream getInputStream(String uri) throws IOException, FileNotFoundException {
 		if (filesHolder.contains(uri)) {
 			return filesHolder.getInputStream(uri);
 		}
 		IVirtualFolder rootFolder = vComponent.getRootFolder();
 		IVirtualResource vResource = rootFolder.findMember(uri);
 		String filePath = null;
 		if (null != vResource && vResource.exists()) {
 			filePath = vResource.getUnderlyingResource().getLocation().toOSString();
 			java.io.File file = new java.io.File(filePath);
 			return new FileInputStream(file);
 		}
 		String eString = EARArchiveOpsResourceHandler.getString("ARCHIVE_OPERATION_FileNotFound");//$NON-NLS-1$
 		throw new FileNotFoundException(eString);
 	}
 
 	public Collection getLoadedMofResources() {
 		Collection resources = super.getLoadedMofResources();
		Collection resourcesToRemove = null;
 		Iterator iterator = resources.iterator();
 		while (iterator.hasNext()) {
 			Resource res = (Resource) iterator.next();
			if (res.getURI().toString().endsWith(IModuleConstants.WTPMODULE_FILE_NAME)) {
				if (resourcesToRemove == null) {
					resourcesToRemove = new ArrayList();
				}
 				resourcesToRemove.add(res);
			}
 		}
 		resources.removeAll(resourcesToRemove);
 
 		return resources;
 	}
 
 	public Resource getMofResource(String uri) throws FileNotFoundException, ResourceLoadException {
 		try {
 			URI compUri = ModuleURIUtil.fullyQualifyURI(vComponent.getComponentHandle());
 			IPath requestPath = new Path(compUri.path()).append(new Path(uri));
 			URI resourceURI = URI.createURI(PlatformURLModuleConnection.MODULE_PROTOCOL + requestPath.toString());
 			return getResourceSet().getResource(resourceURI, true);
 		} catch (WrappedException wrapEx) {
 			if ((ExtendedEcoreUtil.getFileNotFoundDetector().isFileNotFound(wrapEx))) {
 				FileNotFoundException fileNotFoundEx = ExtendedEcoreUtil.getInnerFileNotFoundException(wrapEx);
 				throw fileNotFoundEx;
 			}
 			throwResourceLoadException(uri, wrapEx);
 			return null; // never happens - compiler expects it though
 		}
 
 	}
 
 	public boolean isClassLoaderNeeded() {
 		return false;
 	}
 
 	public IVirtualComponent getComponent() {
 		return vComponent;
 	}
 
 
 
 }
