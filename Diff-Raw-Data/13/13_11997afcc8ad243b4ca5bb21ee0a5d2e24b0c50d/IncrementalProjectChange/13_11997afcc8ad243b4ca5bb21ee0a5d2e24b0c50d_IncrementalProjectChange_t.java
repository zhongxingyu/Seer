 /*******************************************************************************
  * Copyright (c) 2010 xored software, Inc.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.internal.core.builder;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.builder.IRenameChange;
 import org.eclipse.dltk.utils.TextUtils;
 
 public class IncrementalProjectChange extends AbstractBuildChange implements
 		IResourceDeltaVisitor {
 
 	private final IResourceDelta delta;
 
 	public IncrementalProjectChange(IResourceDelta delta, IProject project,
 			IProgressMonitor monitor) {
 		super(project, monitor);
 		this.delta = delta;
 	}
 
 	public IResourceDelta getResourceDelta() {
 		return delta;
 	}
 
 	@SuppressWarnings("unchecked")
 	private final List<IPath>[] deletes = new List[9];
 
 	public List<IPath> getDeletes(int options) throws CoreException {
 		validateFlags(options, NO_RENAMES);
 		loadData();
 		if (deletes[options] == null) {
 			final List<IPath> paths = new ArrayList<IPath>();
 			if (projectDeletes != null) {
 				paths.addAll(projectDeletes);
 			}
 			if (wantRenames(options) && projectRenames != null) {
 				paths.addAll(projectRenames.values());
 			}
 			deletes[options] = unmodifiableList(paths);
 		}
 		return deletes[options];
 	}
 
 	public List<IRenameChange> getRenames() throws CoreException {
 		loadData();
 		if (projectRenames != null) {
 			return projectRenames.getRenames();
 		} else {
 			return Collections.emptyList();
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private final List<IFile>[] resources = new List[16];
 
 	public List<IFile> getResources(int options) throws CoreException {
 		options = validateFlags(options, ALL | NO_RENAMES | ADDED | CHANGED);
 		if ((options & (ADDED | CHANGED | NO_RENAMES)) == (CHANGED | NO_RENAMES)) {
 			throw new IllegalArgumentException();
 		}
 		loadData();
 		if (resources[options] == null) {
 			final List<IFile> files = new ArrayList<IFile>();
 			if (checkFlag(options, ADDED)) {
 				if (projectAdditions != null) {
 					files.addAll(projectAdditions.getResources(checkFlag(
 							options, ALL)));
 				}
 				if (wantRenames(options) && projectRenames != null) {
 					files.addAll(projectRenames.getResources(checkFlag(options,
 							ALL)));
 				}
 			}
 			if (checkFlag(options, CHANGED)) {
 				if (projectChanges != null) {
 					files.addAll(projectChanges.getResources(checkFlag(options,
 							ALL)));
 				}
 			}
 			resources[options] = unmodifiableList(files);
 		}
 		return resources[options];
 	}
 
 	@SuppressWarnings("unchecked")
 	private final List<ISourceModule>[] modules = new List[4];
 
 	public List<ISourceModule> getSourceModules(int options)
 			throws CoreException {
 		options = validateFlags(options, ADDED | CHANGED);
 		loadData();
 		if (modules[options] == null) {
 			final List<ISourceModule> m = new ArrayList<ISourceModule>();
 			if (checkFlag(options, ADDED)) {
 				if (projectAdditions != null) {
 					m.addAll(projectAdditions.getSourceModules());
 				}
 				if (projectRenames != null) {
 					m.addAll(projectRenames.getSourceModules());
 				}
 			}
 			if (checkFlag(options, CHANGED)) {
 				if (projectChanges != null) {
 					m.addAll(projectChanges.getSourceModules());
 				}
 			}
 			modules[options] = unmodifiableList(m);
 		}
 		return modules[options];
 	}
 
 	private boolean loaded = false;
 
 	protected final void loadData() throws CoreException {
 		if (!loaded) {
 			loaded = true;
 			delta.accept(this);
 		}
 	}
 
 	@SuppressWarnings("serial")
 	abstract class AbstractChangeSet<T> extends HashMap<IFile, T> {
 		List<ISourceModule> modules = null;
 		Set<IFile> realResources = null;
 
 		public Collection<? extends IFile> getAll() {
 			return keySet();
 		}
 
 		void resetDerivedContent() {
 			modules = null;
 			realResources = null;
 		}
 
 		List<ISourceModule> getSourceModules() {
 			if (modules == null) {
 				detectSourceModules();
 			}
 			return modules;
 		}
 
 		Collection<IFile> getResources() {
 			if (realResources == null) {
 				detectSourceModules();
 			}
 			return realResources;
 		}
 
 		Collection<? extends IFile> getResources(boolean all) {
 			return all ? getAll() : getResources();
 		}
 
 		private final void detectSourceModules() {
 			final List<ISourceModule> m = new ArrayList<ISourceModule>();
 			final Set<IFile> rr = new HashSet<IFile>();
 			locateSourceModules(this.keySet(), m, rr);
 			this.modules = unmodifiableList(m);
 			this.realResources = unmodifiableSet(rr);
 		}
 	}
 
 	@SuppressWarnings("serial")
 	class ChangeSet extends AbstractChangeSet<Object> {
 		void add(IFile file) {
 			put(file, Boolean.TRUE);
 		}
 
 		@Override
 		public String toString() {
 			return keySet().toString();
 		}
 	}
 
 	@SuppressWarnings("serial")
 	class RenameChangeSet extends AbstractChangeSet<IPath> {
 		List<IRenameChange> renames = null;
 
 		@Override
 		void resetDerivedContent() {
 			super.resetDerivedContent();
 			renames = null;
 		}
 
 		List<IRenameChange> getRenames() {
 			if (renames == null) {
 				final List<IRenameChange> r = new ArrayList<IRenameChange>();
 				for (Map.Entry<IFile, IPath> entry : entrySet()) {
 					r.add(new RenameChange(entry.getValue(), entry.getKey()));
 				}
 				this.renames = unmodifiableList(r);
 			}
 			return renames;
 		}
 
 	}
 
 	private ChangeSet projectAdditions = null;
 	private ChangeSet projectChanges = null;
	/**
	 * Project-relative paths of the deleted files.
	 */
 	private List<IPath> projectDeletes = null;
 	private RenameChangeSet projectRenames = null;
 
 	protected void resetDerivedProjectChanges() {
 		if (projectAdditions != null) {
 			projectAdditions.resetDerivedContent();
 		}
 		if (projectChanges != null) {
 			projectChanges.resetDerivedContent();
 		}
 		if (projectRenames != null) {
 			projectRenames.resetDerivedContent();
 		}
 		Arrays.fill(deletes, null);
 		Arrays.fill(resources, null);
 		Arrays.fill(modules, null);
 	}
 
 	protected boolean addChangedResource(IFile file) throws CoreException {
 		Assert.isLegal(project.equals(file.getProject()));
 		loadData();
 		if (projectAdditions != null && projectAdditions.containsKey(file)) {
 			return false;
 		}
 		if (projectChanges == null) {
 			projectChanges = new ChangeSet();
 		}
 		if (!projectChanges.containsKey(file)) {
 			projectChanges.add(file);
 			resetDerivedProjectChanges();
 			return true;
 		}
 		return false;
 	}
 
 	public boolean visit(IResourceDelta delta) throws CoreException {
 		checkCanceled();
 		final IResource resource = delta.getResource();
 		// System.out.println(resource);
 		if (resource.getType() == IResource.FOLDER) {
 			this.monitor.subTask(Messages.ScriptBuilder_scanningProjectFolder
 					+ resource.getProjectRelativePath().toString());
 		}
 		if (resource.getType() == IResource.FILE) {
 			switch (delta.getKind()) {
 			case IResourceDelta.ADDED:
 				if ((delta.getFlags() & IResourceDelta.MOVED_FROM) != 0) {
 					if (projectRenames == null) {
 						projectRenames = new RenameChangeSet();
 					}
 					projectRenames.put((IFile) resource, delta
 							.getMovedFromPath().removeFirstSegments(1));
 				} else {
 					if (projectAdditions == null) {
 						projectAdditions = new ChangeSet();
 					}
 					projectAdditions.add((IFile) resource);
 				}
 				break;
 			case IResourceDelta.CHANGED:
 				if ((delta.getFlags() & (IResourceDelta.CONTENT | IResourceDelta.ENCODING)) != 0) {
 					if (projectChanges == null) {
 						projectChanges = new ChangeSet();
 					}
 					projectChanges.add((IFile) resource);
 				}
 				break;
 			case IResourceDelta.REMOVED:
 				if ((delta.getFlags() & IResourceDelta.MOVED_TO) == 0) {
 					if (projectDeletes == null) {
 						projectDeletes = new ArrayList<IPath>();
 					}
 					projectDeletes.add(delta.getProjectRelativePath());
 				}
 				break;
 			}
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public String toString() {
 		try {
 			loadData();
 		} catch (CoreException e) {
 			return e.toString();
 		}
 		final List<String> lines = new ArrayList<String>();
 		if (projectAdditions != null) {
 			lines.add("additions=" + projectAdditions.toString());
 		}
 		if (projectChanges != null) {
 			lines.add("changes=" + projectChanges.toString());
 		}
 		if (projectDeletes != null) {
 			lines.add("deletes=" + projectDeletes);
 		}
 		return TextUtils.join(lines, "\n");
 	}
 
 	protected Set<IPath> getAddedPaths() throws CoreException {
 		loadData();
 		final Set<IPath> paths = new HashSet<IPath>();
 		if (projectAdditions != null) {
 			for (IFile file : projectAdditions.getAll()) {
 				paths.add(file.getFullPath());
 			}
 		}
 		if (projectRenames != null) {
 			for (IRenameChange rename : projectRenames.getRenames()) {
 				paths.add(rename.getTarget().getFullPath());
 			}
 		}
 		return paths;
 	}
 
	/**
	 * Returns full paths of the deleted files.
	 */
 	protected Collection<IPath> getDeletedPaths() throws CoreException {
 		loadData();
 		final Set<IPath> paths = new HashSet<IPath>();
 		if (projectDeletes != null) {
			final IPath projectPath = project.getFullPath();
			for (IPath path : projectDeletes) {
				paths.add(projectPath.append(path));
			}
 		}
 		if (projectRenames != null) {
 			final IPath projectPath = project.getFullPath();
 			for (IRenameChange rename : projectRenames.getRenames()) {
 				paths.add(projectPath.append(rename.getSource()));
 			}
 		}
 		return paths;
 	}
 
 	protected Set<IPath> getChangedPaths() throws CoreException {
 		loadData();
 		final Set<IPath> paths = new HashSet<IPath>();
 		if (projectAdditions != null) {
 			for (IFile file : projectAdditions.getAll()) {
 				paths.add(file.getFullPath());
 			}
 		}
 		if (projectChanges != null) {
 			for (IFile file : projectChanges.getAll()) {
 				paths.add(file.getFullPath());
 			}
 		}
 		if (projectDeletes != null) {
 			final IPath projectPath = project.getFullPath();
 			for (IPath path : projectDeletes) {
 				paths.add(projectPath.append(path));
 			}
 		}
 		if (projectRenames != null) {
 			final IPath projectPath = project.getFullPath();
 			for (IRenameChange rename : projectRenames.getRenames()) {
 				paths.add(projectPath.append(rename.getSource()));
 				paths.add(rename.getTarget().getFullPath());
 			}
 		}
 		return paths;
 	}
 
 }
