 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
 
  *******************************************************************************/
 package org.eclipse.dltk.internal.core.search;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.dltk.compiler.env.IDependent;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.IBuildpathEntry;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.IMember;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IModelElementDelta;
 import org.eclipse.dltk.core.IProjectFragment;
 import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.core.IType;
 import org.eclipse.dltk.core.ITypeHierarchy;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.WorkingCopyOwner;
 import org.eclipse.dltk.core.environment.IFileHandle;
 import org.eclipse.dltk.internal.core.ArchiveProjectFragment;
 import org.eclipse.dltk.internal.core.Model;
 import org.eclipse.dltk.internal.core.ModelElement;
 import org.eclipse.dltk.internal.core.ModelManager;
 import org.eclipse.dltk.internal.core.ScriptProject;
 import org.eclipse.dltk.internal.core.hierarchy.TypeHierarchy;
 
 /**
  * Scope limited to the subtype and supertype hierarchy of a given type.
  */
 public class HierarchyScope extends DLTKSearchScope {
 
 	public IType focusType;
 	protected String focusPath;
 	protected WorkingCopyOwner owner;
 
 	protected ITypeHierarchy hierarchy;
 	protected IType[] types;
 	protected HashSet resourcePaths;
 	protected IPath[] enclosingProjectsAndZips;
 
 	protected IResource[] elements;
 	protected int elementCount;
 
 	public boolean needsRefresh;
 
 	/*
 	 * (non-Javadoc) Adds the given resource to this search scope.
 	 */
 	public void add(IResource element) {
 		if (this.elementCount == this.elements.length) {
 			System.arraycopy(this.elements, 0,
 					this.elements = new IResource[this.elementCount * 2], 0,
 					this.elementCount);
 		}
 		elements[elementCount++] = element;
 
 		IModelElement modelElement = DLTKCore.create(element);
 		try {
 			add(modelElement);
 		} catch (ModelException e) {
 			if (DLTKCore.DEBUG) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc) Creates a new hiearchy scope for the given type.
 	 */
 	public HierarchyScope(IDLTKLanguageToolkit languageToolkit, IType type,
 			WorkingCopyOwner owner) throws ModelException {
 		super(languageToolkit);
 
 		this.focusType = type;
 		this.owner = owner;
 
 		this.enclosingProjectsAndZips = this.computeProjectsAndZips(type);
 
 		// resource path
 		IProjectFragment root = (IProjectFragment) type.getScriptFolder()
 				.getParent();
 		if (root.isArchive()) {
 			IPath jarPath = root.getPath();
 			Object target = Model.getTarget(ResourcesPlugin.getWorkspace()
 					.getRoot(), jarPath, true);
 			String zipFileName;
 			if (target instanceof IFile) {
 				// internal jar
 				zipFileName = jarPath.toString();
 			} else if (target instanceof IFileHandle) {
 				// external jar
 				// TODO Check this
 				zipFileName = ((IFileHandle) target).toOSString();
 			} else {
 				return; // unknown target
 			}
 			this.focusPath = zipFileName
 					+ IDependent.ARCHIVE_FILE_ENTRY_SEPARATOR
 					+ type.getFullyQualifiedName().replace('.', '/')
 			/* + SUFFIX_STRING_class */;
 		} else {
 			this.focusPath = type.getPath().toString();
 		}
 
 		this.needsRefresh = true;
 
 		// disabled for now as this could be expensive
 		// JavaModelManager.getJavaModelManager().rememberScope(this);
 	}
 
 	private void buildResourceVector() {
 		HashMap resources = new HashMap();
 		HashMap paths = new HashMap();
 		this.types = this.hierarchy.getAllTypes();
 		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
 		for (int i = 0; i < this.types.length; i++) {
 			IType type = this.types[i];
 			IResource resource = type.getResource();
 			if (resource != null && resources.get(resource) == null) {
 				resources.put(resource, resource);
 				add(resource);
 			}
 			IProjectFragment root = (IProjectFragment) type.getScriptFolder()
 					.getParent();
 			if (root instanceof ArchiveProjectFragment) {
 				// type in a jar
 				ArchiveProjectFragment jar = (ArchiveProjectFragment) root;
 				IPath jarPath = jar.getPath();
 				Object target = Model.getTarget(workspaceRoot, jarPath, true);
 				String zipFileName;
 				if (target instanceof IFile) {
 					// internal jar
 					zipFileName = jarPath.toString();
 				} else if (target instanceof IFileHandle) {
 					// external jar
 					// TODO Check this
 					zipFileName = ((IFileHandle) target).toOSString();
 				} else {
 					continue; // unknown target
 				}
 				String resourcePath = zipFileName
 						+ IDependent.ARCHIVE_FILE_ENTRY_SEPARATOR
 						+ type.getFullyQualifiedName().replace('.', '/')
 				/* + SUFFIX_STRING_class */;
 
 				this.resourcePaths.add(resourcePath);
 				paths.put(jarPath, type);
 			} else {
 				// type is a project
 				paths.put(type.getScriptProject().getProject().getFullPath(),
 						type);
 			}
 		}
 		this.enclosingProjectsAndZips = new IPath[paths.size()];
 		int i = 0;
 		for (Iterator iter = paths.keySet().iterator(); iter.hasNext();) {
 			this.enclosingProjectsAndZips[i++] = (IPath) iter.next();
 		}
 	}
 
 	/*
 	 * Computes the paths of projects and jars that the hierarchy on the given
 	 * type could contain. This is a super set of the project and jar paths once
 	 * the hierarchy is computed.
 	 */
 	private IPath[] computeProjectsAndZips(IType type) throws ModelException {
 		HashSet set = new HashSet();
 		IProjectFragment root = (IProjectFragment) type.getScriptFolder()
 				.getParent();
 		if (root.isArchive()) {
 			// add the root
 			set.add(root.getPath());
 			// add all projects that reference this archive and their dependents
 			IPath rootPath = root.getPath();
 			Model model = ModelManager.getModelManager().getModel();
 			IScriptProject[] projects = model.getScriptProjects();
 			HashSet visited = new HashSet();
 			for (int i = 0; i < projects.length; i++) {
 				IScriptProject project2 = projects[i];
 				ScriptProject project = (ScriptProject) project2;
 				IBuildpathEntry entry = project.getBuildpathEntryFor(rootPath);
 				if (entry != null) {
 					// add the project and its binary pkg fragment roots
 					IProjectFragment[] roots = project.getAllProjectFragments();
 					set.add(project.getPath());
 					for (int j = 0; j < roots.length; j++) {
 						IProjectFragment pkgFragmentRoot = roots[j];
 						if (pkgFragmentRoot.getKind() == IProjectFragment.K_BINARY) {
 							set.add(pkgFragmentRoot.getPath());
 						}
 					}
 					// add the dependent projects
 					this.computeDependents(project, set, visited);
 				}
 			}
 		} else {
 			// add all the project's pkg fragment roots
 			IScriptProject project = (IScriptProject) root.getParent();
 			IProjectFragment[] roots = project.getProjectFragments();
 			for (int j = 0; j < roots.length; j++) {
 				IProjectFragment pkgFragmentRoot = roots[j];
 				if (pkgFragmentRoot.getKind() == IProjectFragment.K_BINARY) {
 					set.add(pkgFragmentRoot.getPath());
 				} else {
 					set.add(pkgFragmentRoot.getParent().getPath());
 				}
 			}
 			// add the dependent projects
 			this.computeDependents(project, set, new HashSet());
 		}
 		IPath[] result = new IPath[set.size()];
 		set.toArray(result);
 		return result;
 	}
 
 	private void computeDependents(IScriptProject project, HashSet set,
 			HashSet visited) {
 		if (visited.contains(project)) {
 			return;
 		}
 		visited.add(project);
 		IProject[] dependents = project.getProject().getReferencingProjects();
 		for (int i = 0; i < dependents.length; i++) {
 			IProject dependent2 = dependents[i];
 			try {
 				IScriptProject dependent = DLTKCore.create(dependent2);
 				IProjectFragment[] roots = dependent.getProjectFragments();
 				set.add(dependent.getPath());
 				for (int j = 0; j < roots.length; j++) {
 					IProjectFragment pkgFragmentRoot = roots[j];
 					if (pkgFragmentRoot.isArchive()) {
 						set.add(pkgFragmentRoot.getPath());
 					}
 				}
 				this.computeDependents(dependent, set, visited);
 			} catch (ModelException e) {
 				// project is not a java project
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see IJavaSearchScope#encloses(String)
 	 */
 	public boolean encloses(String resourcePath) {
 		if (this.hierarchy == null) {
 			if (resourcePath.equals(this.focusPath)) {
 				return true;
 			} else {
 				if (this.needsRefresh) {
 					try {
 						this.initialize();
 					} catch (ModelException e) {
 						return false;
 					}
 				} else {
 					// the scope is used only to find enclosing projects and
 					// jars
 					// clients is responsible for filtering out elements not in
 					// the hierarchy (see SearchEngine)
 					return true;
 				}
 			}
 		}
 		if (this.needsRefresh) {
 			try {
 				this.refresh();
 			} catch (ModelException e) {
 				return false;
 			}
 		}
 		int separatorIndex = resourcePath
 				.indexOf(IDependent.ARCHIVE_FILE_ENTRY_SEPARATOR);
 		if (separatorIndex != -1) {
 			return this.resourcePaths.contains(resourcePath);
 		} else {
 			for (int i = 0; i < this.elementCount; i++) {
 				if (resourcePath.startsWith(this.elements[i].getFullPath()
 						.toString())) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see IJavaSearchScope#encloses(IModelElement)
 	 */
 	public boolean encloses(IModelElement element) {
 		if (this.hierarchy == null) {
 			if (this.focusType.equals(element.getAncestor(IModelElement.TYPE))) {
 				return true;
 			} else {
 				if (this.needsRefresh) {
 					try {
 						this.initialize();
 					} catch (ModelException e) {
 						return false;
 					}
 				} else {
 					// the scope is used only to find enclosing projects and
 					// jars
 					// clients is responsible for filtering out elements not in
 					// the hierarchy (see SearchEngine)
 					return true;
 				}
 			}
 		}
 		if (this.needsRefresh) {
 			try {
 				this.refresh();
 			} catch (ModelException e) {
 				return false;
 			}
 		}
 		IType type = null;
 		if (element instanceof IType) {
 			type = (IType) element;
 		} else if (element instanceof IMember) {
 			type = ((IMember) element).getDeclaringType();
 		}
 		if (type != null) {
 			if (this.hierarchy.contains(type)) {
 				return true;
 			} else {
 				// be flexible: look at original element (see bug 14106
 				// Declarations in Hierarchy does not find declarations in
 				// hierarchy)
 				IType original;
 				if (/*
 					 * !type.isBinary() &&
 					 */(original = (IType) type.getPrimaryElement()) != null) {
 					return this.hierarchy.contains(original);
 				}
 			}
 		}
 		return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see IJavaSearchScope#enclosingProjectsAndZips()
 	 * 
 	 * @deprecated
 	 */
 	public IPath[] enclosingProjectsAndZips() {
 		if (this.needsRefresh) {
 			try {
 				this.refresh();
 			} catch (ModelException e) {
 				return new IPath[0];
 			}
 		}
 		return this.enclosingProjectsAndZips;
 	}
 
 	/**
 	 * This implementation creates a full (supertype and subtype) hierarchy for
 	 * the given focus type.
 	 * 
 	 * @param owner
 	 *            the owner of working copies that take precedence over their
 	 *            original compilation units
 	 * @param monitor
 	 *            the given progress monitor
 	 * @return a type hierarchy for this type containing this type, all of its
 	 *         supertypes, and all its subtypes in the workspace
 	 * @throws ModelException
 	 */
 	protected ITypeHierarchy createHierarchy(IType focusType,
 			WorkingCopyOwner owner, IProgressMonitor monitor)
 			throws ModelException {
 		return focusType.newTypeHierarchy(owner, monitor);
 	}
 
 	protected void initialize() throws ModelException {
 		this.resourcePaths = new HashSet();
 		this.elements = new IResource[5];
 		this.elementCount = 0;
 		this.needsRefresh = false;
 		if (this.hierarchy == null) {
 			this.hierarchy = createHierarchy(this.focusType, this.owner, null);
 		} else {
 			this.hierarchy.refresh(null);
 		}
 		this.buildResourceVector();
 	}
 
 	/*
 	 * @see AbstractSearchScope#processDelta(IModelElementDelta)
 	 */
 	public void processDelta(IModelElementDelta delta, int eventType) {
 		if (this.needsRefresh) {
 			return;
 		}
 		this.needsRefresh = this.hierarchy == null ? false
 				: ((TypeHierarchy) this.hierarchy).isAffected(delta/*
 																	 * ,
 																	 * eventType
 																	 */);
 	}
 
 	protected void refresh() throws ModelException {
 		// if (this.hierarchy != null) {
 		this.initialize();
 		// }
 	}
 
 	public String toString() {
 		return "HierarchyScope on " + ((ModelElement) this.focusType).toStringWithAncestors(); //$NON-NLS-1$
 	}
 
 }
