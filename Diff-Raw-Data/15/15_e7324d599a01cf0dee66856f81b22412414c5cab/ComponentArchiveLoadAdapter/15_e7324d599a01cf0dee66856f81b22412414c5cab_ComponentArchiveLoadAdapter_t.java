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
 package org.eclipse.jst.j2ee.internal.archive;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
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
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.jst.j2ee.classpathdep.IClasspathDependencyConstants;
 import org.eclipse.jst.j2ee.componentcore.J2EEModuleVirtualComponent;
 import org.eclipse.jst.j2ee.internal.J2EEConstants;
 import org.eclipse.jst.j2ee.internal.archive.operations.EARArchiveOpsResourceHandler;
 import org.eclipse.jst.j2ee.internal.classpathdep.ClasspathDependencyManifestUtil;
 import org.eclipse.jst.j2ee.internal.classpathdep.ClasspathDependencyVirtualComponent;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
 import org.eclipse.jst.jee.archive.AbstractArchiveLoadAdapter;
 import org.eclipse.jst.jee.archive.ArchiveModelLoadException;
 import org.eclipse.jst.jee.archive.IArchive;
 import org.eclipse.jst.jee.archive.IArchiveLoadAdapter;
 import org.eclipse.jst.jee.archive.IArchiveResource;
 import org.eclipse.jst.jee.archive.internal.ArchiveURIConverter;
 import org.eclipse.wst.common.componentcore.UnresolveableURIException;
 import org.eclipse.wst.common.componentcore.internal.ComponentResource;
 import org.eclipse.wst.common.componentcore.internal.DependencyType;
 import org.eclipse.wst.common.componentcore.internal.StructureEdit;
 import org.eclipse.wst.common.componentcore.internal.resources.VirtualArchiveComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualContainer;
 import org.eclipse.wst.common.componentcore.resources.IVirtualFile;
 import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
 import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
 import org.eclipse.wst.common.componentcore.resources.IVirtualResource;
 
 public abstract class ComponentArchiveLoadAdapter extends AbstractArchiveLoadAdapter {
 
 	protected static final String DOT_SQLJ = ".sqlj"; //$NON-NLS-1$
 
 	protected static final String DOT_JSP = ".jsp"; //$NON-NLS-1$
 
 	protected static final String DOT_PROJECT = ".project"; //$NON-NLS-1$
 
 	protected static final String DOT_CLASSPATH = ".classpath"; //$NON-NLS-1$
 
 	protected static final String DOT_SETTINGS = ".settings"; //$NON-NLS-1$
 
 	protected static final String DOT_CVS_IGORE = ".cvsignore"; //$NON-NLS-1$
 
 	protected IVirtualComponent vComponent;
 
 	protected boolean exportSource = true;
 
 	private List zipFiles = new ArrayList();
 
 	private List javaClasspathURIs = new ArrayList();
 
 	protected boolean includeClasspathComponents = true;
 
 	protected class FilesHolder {
 
 		private Map pathsToArchiveResources = new HashMap();
 
 		private Map pathsToWorkbenchResources = new HashMap();
 
 		private Map workbenchResourcesToPaths = new HashMap();
 
 		private Map pathsToDiskFiles;
 
 		private Map pathsToZipEntry = new HashMap();
 
 		public void removeIFile(IFile file) {
 			IPath path = (IPath) workbenchResourcesToPaths.get(file);
 			remove(path);
 		}
 
 		public void remove(IPath path) {
 			pathsToArchiveResources.remove(path);
 			Object resource = pathsToWorkbenchResources.remove(path);
 			if (resource != null) {
 				workbenchResourcesToPaths.remove(resource);
 			}
 			if (pathsToDiskFiles != null) {
 				pathsToDiskFiles.remove(path);
 			}
 		}
 
 		public void addFile(IArchiveResource file) {
 			IPath path = file.getPath();
 			pathsToArchiveResources.put(path, file);
 		}
 
 		public void addFile(IArchiveResource file, java.io.File externalDiskFile) {
 			IPath path = file.getPath();
 			pathsToArchiveResources.put(path, file);
 			if (null == pathsToDiskFiles) {
 				pathsToDiskFiles = new HashMap();
 			}
 			pathsToDiskFiles.put(path, externalDiskFile);
 		}
 
 		public void addFile(IArchiveResource file, IResource resource) {
 			IPath path = file.getPath();
 			pathsToArchiveResources.put(path, file);
 			pathsToWorkbenchResources.put(path, resource);
 		}
 
 		public InputStream getInputStream(IPath path) throws IOException, FileNotFoundException {
 			java.io.File diskFile = null;
 
 			if (pathsToDiskFiles != null && pathsToDiskFiles.containsKey(path)) {
 				diskFile = (java.io.File) pathsToDiskFiles.get(path);
 			} else if (pathsToWorkbenchResources != null && pathsToWorkbenchResources.containsKey(path)) {
 				IResource resource = (IResource) pathsToWorkbenchResources.get(path);
 				diskFile = new java.io.File(resource.getLocation().toOSString());
 			}
 			if (diskFile != null) {
 				return new FileInputStream(diskFile);
 			} else if (pathsToZipEntry.containsKey(path)) {
 				Map fileURIMap = (Map) pathsToZipEntry.get(path);
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
 			} else {
 				IArchiveResource res = getArchiveResource(path);
 				return ComponentArchiveLoadAdapter.this.getSuperInputStream(res);
 			}
 		}
 
 		public List<IArchiveResource> getFiles() {
 			return new ArrayList<IArchiveResource>(pathsToArchiveResources.values());
 		}
 
 		public boolean contains(IPath path) {
 			return pathsToArchiveResources.containsKey(path);
 		}
 
 		public IArchiveResource getArchiveResource(IPath path) {
 			return (IArchiveResource) pathsToArchiveResources.get(path);
 		}
 
 		public void addEntry(ZipEntry entry, ZipFile zipFile, IPath runtimePath) {
 			if (runtimePath != null) {
 				if (!runtimePath.equals("/")) //$NON-NLS-1$
 					runtimePath = runtimePath.append(entry.getName());
 				else
 					runtimePath = new Path(entry.getName());
 			} else {
 				runtimePath = new Path(entry.getName());
 			}
 
 			IArchiveResource file = createFile(runtimePath);
 
 			Map fileURIMap = new HashMap();
 			fileURIMap.put(entry.getName(), zipFile);
 
 			pathsToZipEntry.put(file.getPath(), fileURIMap);
 			pathsToArchiveResources.put(file.getPath(), file);
 		}
 	}
 
 	protected FilesHolder filesHolder;
 
 	private IVirtualFile manifestFile = null;
 
 	public ComponentArchiveLoadAdapter(IVirtualComponent vComponent) {
 		this(vComponent, true);
 	}
 
 	public ComponentArchiveLoadAdapter(IVirtualComponent vComponent, boolean includeClasspathComponents) {
 		this.vComponent = vComponent;
 		filesHolder = new FilesHolder();
 		setIncludeClasspathComponents(includeClasspathComponents);
 	}
 
 	public void setIncludeClasspathComponents(boolean includeClasspathComponents) {
 		this.includeClasspathComponents = includeClasspathComponents;
 		if (includeClasspathComponents) {
 			this.manifestFile = vComponent.getRootFolder().getFile(new Path(J2EEConstants.MANIFEST_URI));
 			saveJavaClasspathReferences();
 		} else {
 			this.manifestFile = null;
 			javaClasspathURIs.clear();
 		}
 	}
 
 	public IArchiveResource getArchiveResource(IPath resourcePath) throws FileNotFoundException {
 		initArchiveResources();
 		return filesHolder.getArchiveResource(resourcePath);
 	}
 
 	public boolean containsArchiveResource(IPath path) {
 		initArchiveResources();
 		return filesHolder.contains(path);
 	}
 
 	protected boolean archiveResourcesInitialized = false;
 
 	protected void initArchiveResources() {
 		if (!archiveResourcesInitialized) {
 			archiveResourcesInitialized = true;
 			aggregateSourceFiles();
 			aggregateClassFiles();
 			addUtilities();
 		}
 	}
 
 	public List<IArchiveResource> getArchiveResources() {
 
 		initArchiveResources();
 		return filesHolder.getFiles();
 	}
 
 	/**
 	 * Adds library cp entries that point to class folders and have been tagged with the publish/export attribute.
 	 */
 	protected void addMappedClassFolders(final IPath targetRuntimePath) {
 		// retrieve all mapped class folders
 		if (vComponent instanceof J2EEModuleVirtualComponent) {
 			try {
 				final J2EEModuleVirtualComponent j2eeComponent = (J2EEModuleVirtualComponent) vComponent;
 				final IVirtualReference[] cpRefs = j2eeComponent.getJavaClasspathReferences();
 				for (int j = 0; j < cpRefs.length; j++) {
 					final IVirtualReference ref = cpRefs[j];
 					final IPath runtimePath = ref.getRuntimePath();
 					// only process mappings with the specified runtime path
 					if (ref.getReferencedComponent() instanceof ClasspathDependencyVirtualComponent
 							&& runtimePath.equals(targetRuntimePath)) {
 						final ClasspathDependencyVirtualComponent comp = (ClasspathDependencyVirtualComponent) ref.getReferencedComponent();
 						if (comp.isClassFolder()) {
 							final IContainer classFolder = comp.getClassFolder();
 							if (classFolder != null && classFolder.exists()) {
 								aggregateOutputFiles(new IResource[]{classFolder}, runtimePath.makeRelative(), classFolder.getProjectRelativePath().segmentCount());
 							}
 						}
 					}
 				}
 			} catch (CoreException e) {
 				Logger.getLogger().logError(e);
 			}
 		}
 	}
 	
 	protected void saveJavaClasspathReferences() {
 		if (vComponent instanceof J2EEModuleVirtualComponent) {
 			final J2EEModuleVirtualComponent j2eeComp = (J2EEModuleVirtualComponent) vComponent;
 			final IVirtualReference[] refs = j2eeComp.getJavaClasspathReferences();
 			if (refs == null) {
 				return;
 			}
 			for (int i = 0; i < refs.length; i++) {
 				if (refs[i].getRuntimePath().equals(IClasspathDependencyConstants.RUNTIME_MAPPING_INTO_CONTAINER_PATH)) {
 					javaClasspathURIs.add(refs[i].getArchiveName());
 				}
 			}
 		}
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
 
 	/**
 	 * This is a cache of the IResource roots for all java source folders and is
 	 * used by {@link #inJavaSrc(IVirtualResource)}.
 	 */
 	private IResource[] sourceRoots = null;
 
 	protected void aggregateSourceFiles() {
 		try {
 			IVirtualFolder rootFolder = vComponent.getRootFolder();
 			IVirtualResource[] members = rootFolder.members();
 			IPackageFragmentRoot[] srcPkgs = J2EEProjectUtilities.getSourceContainers(vComponent.getProject());
 			sourceRoots = new IResource[srcPkgs.length];
 			for (int i = 0; i < srcPkgs.length; i++) {
 				sourceRoots[i] = srcPkgs[i].getCorrespondingResource();
 			}
 			inJavaSrc = false;
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
 					IContainer javaOutputContainer = outputPath.segmentCount() > 1 ? (IContainer) ResourcesPlugin.getWorkspace().getRoot().getFolder(outputPath) : (IContainer) ResourcesPlugin
 							.getWorkspace().getRoot().getProject(outputPath.lastSegment());
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
 
 					aggregateOutputFiles(new IResource[] { javaOutputContainer }, runtimePath, javaOutputContainer.getProjectRelativePath().segmentCount());
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
 
 	protected boolean aggregateOutputFiles(IResource[] resources, final IPath runtimePathPrefix, int outputFolderSegmentCount) throws CoreException {
 		boolean fileAdded = false;
 		for (int i = 0; i < resources.length; i++) {
 			IArchiveResource cFile = null;
 			if (!resources[i].exists()) {
 				continue;
 			}
 			// We have to avoid duplicates between the source and output folders
 			// (non-java
 			// resources)
 			IPath runtimePath = runtimePathPrefix.append(resources[i].getProjectRelativePath().removeFirstSegments(outputFolderSegmentCount));
 			if (runtimePath == null)
 				continue;
 			if (resources[i].getType() == IResource.FILE) {
 				if (!shouldInclude(runtimePath))
 					continue;
 				cFile = createFile(runtimePath);
 				cFile.setLastModified(getLastModified(resources[i]));
 				filesHolder.addFile(cFile, resources[i]);
 				fileAdded = true;
 			} else if (shouldInclude((IContainer) resources[i])) {
 				IResource[] nestedResources = ((IContainer) resources[i]).members();
 				aggregateOutputFiles(nestedResources, runtimePathPrefix, outputFolderSegmentCount);
 				if (!filesHolder.contains(runtimePath)) {
 					if (!shouldInclude(runtimePath))
 						continue;
 					cFile = createDirectory(runtimePath);
 					cFile.setLastModified(getLastModified(resources[i]));
 					filesHolder.addFile(cFile);
 					fileAdded = true;
 				}
 			}
 		}
 		return fileAdded;
 	}
 
 	/**
 	 * This is used to track whether {@link #aggregateFiles(IVirtualResource[])}
 	 * is currently within a Java Source folder.
 	 */
 	private boolean inJavaSrc = false;
 
 	protected boolean aggregateFiles(IVirtualResource[] virtualResources) throws CoreException {
 		boolean fileAdded = false;
 		for (int i = 0; i < virtualResources.length; i++) {
 			if (!virtualResources[i].exists()) {
 				continue;
 			}
 			// We have to avoid duplicates between the source and output folders
 			// (non-java
 			// resources)
 			IPath runtimePath = virtualResources[i].getRuntimePath();
 			if (runtimePath == null)
 				continue;
 			runtimePath = runtimePath.setDevice(null).makeRelative();
 			if (filesHolder.contains(runtimePath))
 				continue;
 
 			IArchiveResource cFile = null;
 
 			if (virtualResources[i].getType() == IVirtualResource.FILE) {
 				if (!shouldInclude(runtimePath))
 					continue;
 				IResource resource = virtualResources[i].getUnderlyingResource();
 				// want to ignore derived resources nested within Java src
 				// directories; this covers the case where
 				// a user has nested a Java output directory within a Java src
 				// directory (note: should ideally be
 				// respecting Java src path exclusion filters)
 				if (inJavaSrc && resource.isDerived()) {
 					continue;
 				}
 				cFile = createFile(runtimePath);
 				cFile.setLastModified(getLastModified(resource));
 				filesHolder.addFile(cFile, resource);
 				fileAdded = true;
 			} else if (shouldInclude((IVirtualContainer) virtualResources[i])) {
 				boolean inJavaSrcAtThisLevel = inJavaSrc;
 				try {
 					if (!inJavaSrc) {
 						// if not already inside a Java src dir, check again
 						inJavaSrc = inJavaSrc(virtualResources[i]);
 					}
 					IVirtualResource[] nestedVirtualResources = ((IVirtualContainer) virtualResources[i]).members();
 					aggregateFiles(nestedVirtualResources);
 					if (!filesHolder.contains(runtimePath)) {
 						if (!shouldInclude(runtimePath))
 							continue;
 						IResource resource = virtualResources[i].getUnderlyingResource();
 						if (inJavaSrc && resource.isDerived()) {
 							continue;
 						}
 						cFile = createDirectory(runtimePath);
 						cFile.setLastModified(getLastModified(resource));
 						filesHolder.addFile(cFile);
 						fileAdded = true;
 					}
 				} finally {
 					inJavaSrc = inJavaSrcAtThisLevel;
 				}
 			}
 		}
 		return fileAdded;
 	}
 
 	/**
 	 * Determines if the specified IVirtualResource maps to a IResource that is
 	 * contained within a Java src root.
 	 * 
 	 * @param virtualResource
 	 *            IVirtualResource to check.
 	 * @param sourceRoots
 	 *            Current Java src roots.
 	 * @return True if contained in a Java src root, false otherwise.
 	 */
	protected boolean inJavaSrc(final IVirtualResource virtualResource) {
 		if (sourceRoots.length == 0) {
 			return false;
 		}
 		// all mapped resources must be associated with Java src for the
 		// resource to be considered in Java src
 		final IResource[] resources = virtualResource.getUnderlyingResources();
 		boolean inJavaSrc = false;
 		for (int i = 0; i < resources.length; i++) {
 			inJavaSrc = false;
 			for (int j = 0; j < sourceRoots.length; j++) {
 				if (sourceRoots[j].getFullPath().isPrefixOf(resources[i].getFullPath())) {
 					inJavaSrc = true;
 					break;
 				}
 			}
 			// if this one was not in Java src, can break
 			if (!inJavaSrc) {
 				break;
 			}
 		}
 
 		return inJavaSrc;
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
 		IContainer iContainer = (IContainer) vContainer.getUnderlyingResource();
 		return shouldInclude(iContainer);
 	}
 
 	// TODO add a mechanism for ignoring specific file types
 	protected boolean shouldInclude(IPath path) {
 		String lastSegment = path.lastSegment();
 		if (null == lastSegment) {
 			return false;
 		}
 		if (lastSegment.endsWith(DOT_PROJECT) || lastSegment.endsWith(DOT_CLASSPATH) || lastSegment.endsWith(DOT_CVS_IGORE) || path.segment(0).startsWith(DOT_SETTINGS)) {
 			return false;
 		}
 		return isExportSource() || !isSource(path);
 	}
 
 	// TODO add a mechanism for ignoring specific file types
 	protected boolean isSource(IPath path) {
 		if (path == null)
 			return false;
 		return path.lastSegment().endsWith(JavaEEArchiveUtilities.DOT_JAVA) || path.lastSegment().endsWith(DOT_SQLJ);
 	}
 
 	protected void addExternalFile(IPath path, java.io.File externalDiskFile) {
 		IArchiveResource aFile = createFile(path);
 		filesHolder.addFile(aFile, externalDiskFile);
 	}
 
 	protected InputStream getSuperInputStream(IArchiveResource archiveResource) throws IOException, FileNotFoundException {
 		if (archiveResource.getType() == IArchive.ARCHIVE_TYPE) {
 			IArchiveLoadAdapter nestedLoadAdapter = (IArchiveLoadAdapter)((IArchive)archiveResource).getLoadAdapter();
 			if(nestedLoadAdapter instanceof ComponentArchiveLoadAdapter){
 				((ComponentArchiveLoadAdapter)nestedLoadAdapter).setExportSource(isExportSource());
 			}
 		}
 		
 		return super.getInputStream(archiveResource);
 	}
 
 	public InputStream getInputStream(IArchiveResource archiveResource) throws IOException, FileNotFoundException {
 		IPath path = archiveResource.getPath();
 		// If the MANIFEST.MF of a module component is being requested and that
 		// module component references
 		// Java build path-based components, need to dynamically update the
 		// manifest classpath to reflect the resolved
 		// contributions from the build path
 		if (includeClasspathComponents && path.equals(J2EEConstants.MANIFEST_URI) && !javaClasspathURIs.isEmpty() && manifestFile != null && manifestFile.getUnderlyingFile() != null
 				&& manifestFile.getUnderlyingFile().exists()) {
 			// update the manifest classpath for the component
 			ByteArrayOutputStream baos = new ByteArrayOutputStream();
 			ClasspathDependencyManifestUtil.updateManifestClasspath(manifestFile.getUnderlyingFile(), javaClasspathURIs, baos);
 			return new ByteArrayInputStream(baos.toByteArray());
 		}
 
 		if (filesHolder.contains(path)) {
 			return filesHolder.getInputStream(path);
 		}
 		IVirtualFolder rootFolder = vComponent.getRootFolder();
 		IVirtualResource vResource = rootFolder.findMember(path);
 		String filePath = null;
 		if (null != vResource && vResource.exists()) {
 			filePath = vResource.getUnderlyingResource().getLocation().toOSString();
 			java.io.File file = new java.io.File(filePath);
 			return new FileInputStream(file);
 		}
 		String eString = EARArchiveOpsResourceHandler.ARCHIVE_OPERATION_FileNotFound;
 		throw new FileNotFoundException(eString);
 	}
 
 	public IVirtualComponent getComponent() {
 		return vComponent;
 	}
 
 	protected JavaEEEMFArchiveAdapterHelper emfHelper = null;
 
 	protected void initEMFHelper() {
 		if (emfHelper == null) {
 			emfHelper = new JavaEEEMFArchiveAdapterHelper(getArchive());
 			emfHelper.setArchiveURIConverter(new ArchiveURIConverter(emfHelper.getArchive()) {
 				protected URI convertPathToURI(IPath modelObjectPath) {
 					// TODO find a better way to getplatformURI
 					IPath path = getComponent().getRootFolder().getFile(modelObjectPath).getUnderlyingFile().getFullPath();
 					return URI.createURI("platform:/resource/" + path.toString());
 				}
 			});
 		}
 	}
 
 	public boolean containsModelObject(IPath modelObjectPath) {
 		initEMFHelper();
 		if (IArchive.EMPTY_MODEL_PATH == modelObjectPath) {
 			modelObjectPath = getDefaultModelObjectPath();
 		}
 		return emfHelper.containsModelObject(modelObjectPath);
 	}
 
 	public Object getModelObject(IPath modelObjectPath) throws ArchiveModelLoadException {
 		initEMFHelper();
 		if (IArchive.EMPTY_MODEL_PATH == modelObjectPath) {
 			modelObjectPath = getDefaultModelObjectPath();
 		}
 		return emfHelper.getModelObject(modelObjectPath);
 	}
 
 	protected IPath getDefaultModelObjectPath() {
 		return new Path("/"); //$NON-NLS-1$
 	}
 
 	public String toString() {
 		int packageLength = this.getClass().getPackage().getName().length() + 1;
 		StringBuffer buffer = new StringBuffer(this.getClass().getName().substring(packageLength));
 		buffer.append(", Component: "); //$NON-NLS-1$
 		buffer.append(getComponent());
 		return buffer.toString();
 	}
 
 	/**
 	 * protected IProgressMonitor monitor = null;
 	 * 
 	 * public void setProgressMonitor(IProgressMonitor monitor){ this.monitor =
 	 * monitor; }
 	 * 
 	 * protected final int FILE_SAVE_WORK = 100;
 	 * 
 	 * public FileIterator getFileIterator() throws IOException { return new
 	 * FileIteratorImpl(getContainer().getFiles()){ protected SubProgressMonitor
 	 * lastSubMon = null; boolean firstVisit = true;
 	 * 
 	 * public File next() { if(firstVisit){ firstVisit = false; if(monitor !=
 	 * null){
 	 * monitor.beginTask(ProjectSupportResourceHandler.getString(ProjectSupportResourceHandler.Exporting_archive,
 	 * new Object [] { getContainer().getURI() }), files.size() *
 	 * FILE_SAVE_WORK); } } if(lastSubMon != null){ lastSubMon.done();
 	 * lastSubMon = null; } else if(monitor != null){
 	 * monitor.worked(FILE_SAVE_WORK); } File file = super.next(); if(monitor !=
 	 * null){ if(file.isContainer() &&
 	 * ComponentArchiveLoadAdapter.class.isInstance(((ContainerImpl)file).getLoadStrategy())){
 	 * ComponentArchiveLoadAdapter ls =
 	 * (ComponentArchiveLoadAdapter)((ContainerImpl)file).getLoadStrategy();
 	 * lastSubMon = new SubProgressMonitor(monitor, FILE_SAVE_WORK,
 	 * SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
 	 * ls.setProgressMonitor(lastSubMon); } else {
 	 * monitor.subTask(file.getURI()); } } return file; } }; }
 	 */
 }
