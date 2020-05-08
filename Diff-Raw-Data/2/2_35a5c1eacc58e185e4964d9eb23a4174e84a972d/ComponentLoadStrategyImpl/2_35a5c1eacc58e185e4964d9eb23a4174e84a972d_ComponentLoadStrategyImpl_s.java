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
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipException;
 import java.util.zip.ZipFile;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.common.util.WrappedException;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jem.util.emf.workbench.WorkbenchResourceHelperBase;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.File;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.exception.ResourceLoadException;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.strategy.LoadStrategyImpl;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.util.ArchiveUtil;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
 import org.eclipse.wst.common.componentcore.UnresolveableURIException;
 import org.eclipse.wst.common.componentcore.internal.ComponentResource;
 import org.eclipse.wst.common.componentcore.internal.DependencyType;
 import org.eclipse.wst.common.componentcore.internal.StructureEdit;
 import org.eclipse.wst.common.componentcore.internal.impl.ModuleURIUtil;
 import org.eclipse.wst.common.componentcore.internal.impl.PlatformURLModuleConnection;
 import org.eclipse.wst.common.componentcore.internal.resources.VirtualArchiveComponent;
 import org.eclipse.wst.common.componentcore.internal.util.IModuleConstants;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualContainer;
 import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
 import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
 import org.eclipse.wst.common.componentcore.resources.IVirtualResource;
 import org.eclipse.wst.common.internal.emf.utilities.ExtendedEcoreUtil;
 
 public abstract class ComponentLoadStrategyImpl extends LoadStrategyImpl {
 
 	protected IVirtualComponent vComponent;
 	protected boolean exportSource;
 	private List zipFiles = new ArrayList();
 
 	protected class FilesHolder {
 
 		private Map urisToFiles = new HashMap();
 		private Map urisToResources = new HashMap();
 		private Map resourcesToURI = new HashMap();
 		private Map urisToDiskFiles;
 		private Map urisToZipEntry = new HashMap();
 
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
 			} else if (urisToResources != null && urisToResources.containsKey(uri)) {
 				IResource resource = (IResource) urisToResources.get(uri);
 				diskFile = new java.io.File(resource.getLocation().toOSString());
 			} else {
 				Map fileURIMap = (Map) urisToZipEntry.get(uri);
 				Iterator it = fileURIMap.keySet().iterator();
 
 				String sourceFileUri = ""; //$NON-NLS-1$
 				ZipFile zipFile = null;
 
 				// there is only one key, pair
 				while (it.hasNext()) {
 					sourceFileUri = (String) it.next();
 					zipFile = (ZipFile) fileURIMap.get(sourceFileUri);
 				}
 				ZipEntry entry = zipFile.getEntry(sourceFileUri);
 				InputStream in = zipFile.getInputStream(entry);
 				return in;
 			}
 			return new FileInputStream(diskFile);
 		}
 
 		public List getFiles() {
 			return new ArrayList(urisToFiles.values());
 		}
 
 		public boolean contains(String uri) {
 			return urisToFiles.containsKey(uri);
 		}
 
 		public void addEntry(ZipEntry entry, ZipFile zipFile, IPath runtimePath) {
 			String uri = runtimePath == null ? null : runtimePath.toString();
 			String fileURI = ""; //$NON-NLS-1$
 			if (uri != null) {
 				if (!uri.equals("/")) //$NON-NLS-1$
 					fileURI = uri + entry.getName();
 				else
 					fileURI = entry.getName();
 			} else {
 				fileURI = entry.getName();
 			}
 
 			File file = createFile(fileURI);
 
 			Map fileURIMap = new HashMap();
 			fileURIMap.put(entry.getName(), zipFile);
 
 			urisToZipEntry.put(file.getURI(), fileURIMap);
 			urisToFiles.put(file.getURI(), file);
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
 		resourceSet = WorkbenchResourceHelperBase.getResourceSet(vComponent.getProject());
 	}
 
 	protected boolean primContains(String uri) {
 		return false;
 	}
 
 	public List getFiles() {
 		aggregateSourceFiles();
 		aggregateClassFiles();
 		addUtilities();
 		return filesHolder.getFiles();
 	}
 
 	protected void addUtilities() {
 		IVirtualReference[] components = vComponent.getReferences();
 		for (int i = 0; i < components.length; i++) {
 			IVirtualReference reference = components[i];
 			IVirtualComponent referencedComponent = reference.getReferencedComponent();
 
 			if (referencedComponent.isBinary() && reference.getDependencyType() == DependencyType.CONSUMES) {
 				java.io.File diskFile = ((VirtualArchiveComponent) referencedComponent).getUnderlyingDiskFile();
 				ZipFile zipFile;
 				IPath path = reference.getRuntimePath();
 				try {
 					zipFile = new ZipFile(diskFile);
 					zipFiles.add(zipFile);
 					Enumeration enumeration = zipFile.entries();
 					while (enumeration.hasMoreElements()) {
 						ZipEntry entry = (ZipEntry) enumeration.nextElement();
 						filesHolder.addEntry(entry, zipFile, path);
 					}
 				} catch (ZipException e) {
 					Logger.getLogger().logError(e);
 				} catch (IOException e) {
 					Logger.getLogger().logError(e);
 				}
 			}
 		}
 	}
 
 
 
 	protected void aggregateSourceFiles() {
 		try {
 			IVirtualFolder rootFolder = vComponent.getRootFolder();
 			IVirtualResource[] members = rootFolder.members();
 			aggregateFiles(members);
 		} catch (CoreException e) {
 			Logger.getLogger().logError(e);
 		}
 	}
 
 	protected void aggregateClassFiles() {
 		StructureEdit se = null;
 		try {
 			IPackageFragmentRoot[] sourceRoots = J2EEProjectUtilities.getSourceContainers(vComponent.getProject());
 			se = StructureEdit.getStructureEditForRead(vComponent.getProject());
 			for (int i = 0; i < sourceRoots.length; i++) {
 				IPath outputPath = sourceRoots[i].getRawClasspathEntry().getOutputLocation();
 				if (outputPath == null) {
 					IProject project = vComponent.getProject();
 					if (project.hasNature(JavaCore.NATURE_ID)) {
 						IJavaProject javaProject = JavaCore.create(project);
 						outputPath = javaProject.getOutputLocation();
 					}
 				}
 
 				if (outputPath != null) {
 					IContainer javaOutputContainer = outputPath.segmentCount() > 1 ? (IContainer)ResourcesPlugin.getWorkspace().getRoot().getFolder(outputPath) : (IContainer)ResourcesPlugin.getWorkspace().getRoot().getProject(outputPath.lastSegment());
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
 								if (tmpRuntimePath.segmentCount() != 0) {
 									runtimePath = tmpRuntimePath.makeRelative();
 								}
 							}
 						}
 					} catch (UnresolveableURIException e) {
 						Logger.getLogger().logError(e);
 					}
 					if (null == runtimePath) {
 						runtimePath = new Path(""); //$NON-NLS-1$
 					}
 
 					aggregateOutputFiles(new IResource[]{javaOutputContainer}, runtimePath, javaOutputContainer.getProjectRelativePath().segmentCount());
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
 
 	protected void aggregateOutputFiles(IResource[] resources, final IPath runtimePathPrefix, int outputFolderSegmentCount) throws CoreException {
 		for (int i = 0; i < resources.length; i++) {
 			File cFile = null;
 			if (!resources[i].exists()) {
 				continue;
 			}
 			if (resources[i].getType() == IResource.FILE) {
 				// We have to avoid duplicates between the source and output folders (non-java
 				// resources)
 				IPath runtimePath = runtimePathPrefix.append(resources[i].getProjectRelativePath().removeFirstSegments(outputFolderSegmentCount));
 				String uri = runtimePath == null ? null : runtimePath.toString();
 				if (uri == null)
 					continue;
 				if (!shouldInclude(uri))
 					continue;
				if (filesHolder.contains(uri))
					continue;
 				cFile = createFile(uri);
 				cFile.setLastModified(getLastModified(resources[i]));
 				filesHolder.addFile(cFile, resources[i]);
 			} else if (shouldInclude((IContainer) resources[i])) {
 				IResource[] nestedResources = ((IContainer) resources[i]).members();
 				aggregateOutputFiles(nestedResources, runtimePathPrefix, outputFolderSegmentCount);
 			}
 		}
 	}
 
 	protected void aggregateFiles(IVirtualResource[] virtualResources) throws CoreException {
 		for (int i = 0; i < virtualResources.length; i++) {
 			File cFile = null;
 			if (!virtualResources[i].exists()) {
 				continue;
 			}
 			if (virtualResources[i].getType() == IVirtualResource.FILE) {
 				// We have to avoid duplicates between the source and output folders (non-java
 				// resources)
 				IPath runtimePath = virtualResources[i].getRuntimePath();
 				String uri = runtimePath == null ? null : runtimePath.toString();
 				if (uri == null)
 					continue;
 				if (!shouldInclude(uri))
 					continue;
 				if (filesHolder.contains(uri))
 					continue;
 				if (uri.charAt(0) == IPath.SEPARATOR) {
 					uri = uri.substring(1);
 				}
 				cFile = createFile(uri);
 				IResource resource = virtualResources[i].getUnderlyingResource();
 				cFile.setLastModified(getLastModified(resource));
 				filesHolder.addFile(cFile, resource);
 			} else if (shouldInclude((IVirtualContainer) virtualResources[i])) {
 				IVirtualResource[] nestedVirtualResources = ((IVirtualContainer) virtualResources[i]).members();
 				aggregateFiles(nestedVirtualResources);
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
 
 	protected boolean shouldInclude(IContainer aContainer) {
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
 		String eString = EARArchiveOpsResourceHandler.ARCHIVE_OPERATION_FileNotFound;
 		throw new FileNotFoundException(eString);
 	}
 
 	public Collection getLoadedMofResources() {
 		Collection resources = super.getLoadedMofResources();
 		Collection resourcesToRemove = new ArrayList();
 		Iterator iterator = resources.iterator();
 		while (iterator.hasNext()) {
 			Resource res = (Resource) iterator.next();
 			if (res.getURI().toString().endsWith(IModuleConstants.COMPONENT_FILE_NAME))
 				resourcesToRemove.add(res);
 		}
 		if (null != resourcesToRemove) {
 			resources.removeAll(resourcesToRemove);
 		}
 
 		return resources;
 	}
 
 	public Resource getMofResource(String uri) throws FileNotFoundException, ResourceLoadException {
 		try {
 			URI compUri = ModuleURIUtil.fullyQualifyURI(vComponent.getProject());
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
 
 	public void close() {
 		Iterator it = zipFiles.iterator();
 		while (it.hasNext()) {
 			ZipFile file = (ZipFile) it.next();
 			try {
 				file.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 }
