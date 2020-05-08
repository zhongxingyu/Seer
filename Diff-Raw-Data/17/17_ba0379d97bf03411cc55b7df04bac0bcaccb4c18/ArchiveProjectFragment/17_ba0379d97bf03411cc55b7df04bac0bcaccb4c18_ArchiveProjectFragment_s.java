 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
 
  *******************************************************************************/
 package org.eclipse.dltk.internal.core;
 
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.dltk.compiler.CharOperation;
 import org.eclipse.dltk.core.DLTKLanguageManager;
 import org.eclipse.dltk.core.IArchive;
 import org.eclipse.dltk.core.IArchiveEntry;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IModelProvider;
 import org.eclipse.dltk.core.IProjectFragment;
 import org.eclipse.dltk.core.IScriptFolder;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.environment.EnvironmentManager;
 import org.eclipse.dltk.core.environment.IEnvironment;
 import org.eclipse.dltk.internal.core.util.HashtableOfArrayToObject;
 import org.eclipse.dltk.internal.core.util.Util;
 
 public class ArchiveProjectFragment extends ProjectFragment {
 	public final static ArrayList EMPTY_LIST = new ArrayList();
 	/**
 	 * The path to the zip file (a workspace relative path if the archive is
 	 * internal, or an OS path if the archive is external)
 	 */
 	protected final IPath zipPath;
 	protected final IResource zipResource;
 	private IArchive archive;
 
 	protected ArchiveProjectFragment(IResource resource, ScriptProject project) {
 		super(resource, project);
 		zipPath = resource.getFullPath();
 		this.zipResource = resource;
 	}
 
 	protected ArchiveProjectFragment(IPath path, ScriptProject project) {
 		super(null, project);
 		zipPath = path;
 		this.zipResource = null;
 	}
 
 	/**
 	 * Compute the package fragment children of this package fragment root.
 	 * These are all of the directory zip entries, and any directories implied
 	 * by the path of class files contained in the archive of this package
 	 * fragment root. Has the side effect of opening the package fragment
 	 * children.
 	 */
 	protected boolean computeChildren(OpenableElementInfo info, Map newElements)
 			throws ModelException {
 		ArrayList vChildren = new ArrayList();
 		final int SCRIPT = 0;
 		final int NON_SCRIPT = 1;
 		archive = null;
 		try {
 			archive = ModelManager.getModelManager()
 					.getArchive(getPath(), this);
 			HashtableOfArrayToObject packageFragToTypes = new HashtableOfArrayToObject();
 			// always create the default package
 			packageFragToTypes.put(CharOperation.NO_STRINGS, new ArrayList[] {
 					EMPTY_LIST, EMPTY_LIST });
 			for (Enumeration<? extends IArchiveEntry> e = archive
 					.getArchiveEntries(); e.hasMoreElements();) {
 				IArchiveEntry member = e.nextElement();
 				initPackageFragToTypes(packageFragToTypes, member.getName(),
 						member.isDirectory());
 			}
 			// loop through all of referenced packages, creating package
 			// fragments if necessary
 			// and cache the entry names in the infos created for those package
 			// fragments
 			for (int i = 0, length = packageFragToTypes.keyTable.length; i < length; i++) {
 				String[] pkgName = (String[]) packageFragToTypes.keyTable[i];
 				if (pkgName == null)
 					continue;
 				ArrayList[] entries = (ArrayList[]) packageFragToTypes
 						.get(pkgName);
 				String path = ""; //$NON-NLS-1$
 				if (pkgName.length >= 1) {
 					path = pkgName[0];
 					for (int e = 1; e < pkgName.length; ++e) {
 						path += Path.SEPARATOR + pkgName[e];
 					}
 				}
 				Path lpath = new Path(path);
 				ArchiveFolder packFrag = (ArchiveFolder) getScriptFolder(lpath);
 				ArchiveFolderInfo fragInfo = new ArchiveFolderInfo();
 				int resLength = entries[NON_SCRIPT].size();
 				if (resLength == 0) {
 					packFrag.computeForeignResources(CharOperation.NO_STRINGS,
 							fragInfo, archive.getName());
 				} else {
 					String[] resNames = new String[resLength];
 					entries[NON_SCRIPT].toArray(resNames);
 					packFrag.computeForeignResources(resNames, fragInfo,
 							archive.getName());
 				}
 				if (lpath.segmentCount() == 0) {
 					((ArchiveProjectFragmentInfo) info)
 							.setForeignResources(fragInfo.foreignResources);
 				}
 				packFrag.computeChildren(fragInfo, entries[SCRIPT]);
 				newElements.put(packFrag, fragInfo);
 				vChildren.add(packFrag);
 			}
 		} catch (CoreException e) {
 			if (e instanceof ModelException)
 				throw (ModelException) e;
 			throw new ModelException(e);
 		} finally {
 			ModelManager.getModelManager().closeArchive(archive);
 		}
 		// IModelElement[] children = new IModelElement[vChildren.size()];
 		// vChildren.toArray(children);
 		// info.setChildren(children);
 		List childrenSet = vChildren;
 		// Call for extra model providers
 		IDLTKLanguageToolkit toolkit = DLTKLanguageManager
 				.getLanguageToolkit(this);
 		IModelProvider[] providers = ModelProviderManager.getProviders(toolkit
 				.getNatureId());
 		if (providers != null) {
 			for (int i = 0; i < providers.length; i++) {
 				providers[i].provideModelChanges(this, childrenSet);
 			}
 		}
 		info.setChildren((IModelElement[]) childrenSet
 				.toArray(new IModelElement[childrenSet.size()]));
 		return true;
 	}
 
 	public IScriptFolder getScriptFolder(IPath path) {
 		return new ArchiveFolder(this, path);
 	}
 
 	public String getZipName() {
 		IEnvironment environment = EnvironmentManager
 				.getEnvironment(getScriptProject());
 		return environment.convertPathToString(this.zipPath);
 	}
 
 	private void initPackageFragToTypes(
 			HashtableOfArrayToObject packageFragToTypes, String entryName,
 			boolean isDirectory) {
 		int lastSeparator = isDirectory ? entryName.length() - 1 : entryName
 				.lastIndexOf('/');
 		String[] pkgName = Util.splitOn('/', entryName, 0, lastSeparator);
 		String[] existing = null;
 		int length = pkgName.length;
 		int existingLength = length;
 		while (existingLength >= 0) {
 			existing = (String[]) packageFragToTypes.getKey(pkgName,
 					existingLength);
 			if (existing != null)
 				break;
 			existingLength--;
 		}
 		ModelManager manager = ModelManager.getModelManager();
 		for (int i = existingLength; i < length; i++) {
 			if (Util.isValidFolderNameForPackage(pkgName[i])) {
 				System.arraycopy(existing, 0, existing = new String[i + 1], 0,
 						i);
 				existing[i] = manager.intern(pkgName[i]);
 				packageFragToTypes.put(existing, new ArrayList[] { EMPTY_LIST,
 						EMPTY_LIST });
 			} else {
 				// non-script resource folder
 				if (!isDirectory) {
 					ArrayList[] children = (ArrayList[]) packageFragToTypes
 							.get(existing);
 					if (children[1/* NON_SCRIPT */] == EMPTY_LIST)
 						children[1/* NON_SCRIPT */] = new ArrayList();
 					children[1/* NON_SCRIPT */].add(entryName);
 				}
 				return;
 			}
 		}
 		if (isDirectory)
 			return;
 		// add classfile info amongst children
 		ArrayList[] children = (ArrayList[]) packageFragToTypes.get(pkgName);
 		if (Util.isValidSourceModuleName(getScriptProject(), entryName)) {
 			if (children[0/* SCRIPT */] == EMPTY_LIST)
 				children[0/* SCRIPT */] = new ArrayList();
 			String fileName = entryName.substring(lastSeparator + 1);
 			children[0/* SCRIPT */].add(fileName);
 		} else {
 			if (children[1/* NON_SCRIPT */] == EMPTY_LIST)
 				children[1/* NON_SCRIPT */] = new ArrayList();
 			children[1/* NON_SCRIPT */].add(entryName);
 		}
 	}
 
 	public boolean isReadOnly() {
 		return true;
 	}
 
 	protected Object createElementInfo() {
 		return new ArchiveProjectFragmentInfo();
 	}
 
 	public boolean isArchive() {
 		return true;
 	}
 
 	public boolean isExternal() {
 		return getResource() == null;
 	}
 
 	public IResource getUnderlyingResource() throws ModelException {
 		if (isExternal()) {
 			if (!exists()) {
 				throw newNotPresentException();
 			}
 			return null;
 		} else {
 			return super.getUnderlyingResource();
 		}
 	}
 
 	public int hashCode() {
 		return this.zipPath.hashCode();
 	}
 
 	public IPath getPath() {
 		return zipPath;
 	}
 
 	public IResource getResource() {
 		if (this.resource == null) {
 			this.resource = Model.getTarget(ResourcesPlugin.getWorkspace()
 					.getRoot(), this.zipPath, false);
 		}
 		if (this.resource instanceof IResource) {
 			return super.getResource();
 		} else {
 			// external archive
 			return null;
 		}
 	}
 
 	/**
 	 * Returns whether the corresponding resource or associated file exists
 	 */
 	protected boolean resourceExists() {
 		if (this.isExternal()) {
 			/*
 			 * don't make the path relative as this is an external archive
 			 */
 			return Model.getTarget(ResourcesPlugin.getWorkspace().getRoot(),
 					this.getPath(), true) != null;
 		} else {
 			return super.resourceExists();
 		}
 	}
 
 	protected void toStringAncestors(StringBuffer buffer) {
 		if (isExternal())
 			return;
 		super.toStringAncestors(buffer);
 	}
 
 	public int getKind() {
 		return IProjectFragment.K_SOURCE;
 	}
 
 	public boolean equals(Object o) {
 		if (this == o)
 			return true;
 		if (o instanceof ArchiveProjectFragment) {
 			ArchiveProjectFragment other = (ArchiveProjectFragment) o;
 			return this.zipPath.equals(other.zipPath);
 		}
 		return false;
 	}
 
 	public IArchive getArchive() {
 		return archive;
 	}
 
 	public String getElementName() {
 		return this.zipPath.lastSegment();
 	}
 
 	public void getHandleMemento(StringBuffer buff) {
 		((ModelElement) getParent()).getHandleMemento(buff);
 		buff.append(getHandleMementoDelimiter());
 		escapeMementoName(buff, getElementName());
 	}
 
 }
