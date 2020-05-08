 /*******************************************************************************
  * Copyright (c) 2000, 2006 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.dltk.internal.ui.navigator;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.DLTKModelUtil;
 import org.eclipse.dltk.core.ElementChangedEvent;
 import org.eclipse.dltk.core.IBuildpathEntry;
 import org.eclipse.dltk.core.IDLTKProject;
 import org.eclipse.dltk.core.IElementChangedListener;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IModelElementDelta;
 import org.eclipse.dltk.core.IProjectFragment;
 import org.eclipse.dltk.core.IScriptFolder;
 import org.eclipse.dltk.core.IScriptModel;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.internal.core.ScriptFolder;
 import org.eclipse.dltk.internal.ui.StandardModelElementContentProvider;
 import org.eclipse.dltk.internal.ui.scriptview.BuildPathContainer;
 import org.eclipse.dltk.internal.ui.workingsets.WorkingSetModel;
 import org.eclipse.dltk.ui.DLTKUIPlugin;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.viewers.IBasicPropertyConstants;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.swt.widgets.Control;
 
 /**
  * Content provider for the PackageExplorer.
  * 
  * <p>
  * Since 2.1 this content provider can provide the children for flat or
  * hierarchical layout. The hierarchical layout is done by delegating to the
  * <code>ScriptFolderProvider</code>.
  * </p>
  * 
  */
 public abstract class ScriptExplorerContentProvider extends
 		StandardModelElementContentProvider implements ITreeContentProvider,
 		IElementChangedListener {
 	protected static final int ORIGINAL = 0;
 	protected static final int PARENT = 1 << 0;
 	protected static final int GRANT_PARENT = 1 << 1;
 	protected static final int PROJECT = 1 << 2;
 	private TreeViewer fViewer;
 	private Object fInput;
 	private boolean fIsFlatLayout;
 	private ProjectFragmentProvider fScriptFolderProvider;
 	private int fPendingChanges;
 
 	/**
 	 * Creates a new content provider for model elements.
 	 */
 	public ScriptExplorerContentProvider(boolean provideMembers) {
 		super(provideMembers);
 		fScriptFolderProvider = new ProjectFragmentProvider(
 				getPreferenceStore());
 	}
 
	public ProjectFragmentProvider getScriptFolderProvider() {
 		return fScriptFolderProvider;
 	}
 
 	protected abstract IPreferenceStore getPreferenceStore();
 
 	protected Object getViewerInput() {
 		return fInput;
 	}
 
 	/*
 	 * (non-Javadoc) Method declared on IElementChangedListener.
 	 */
 	public void elementChanged(final ElementChangedEvent event) {
 		try {
 			// 58952 delete project does not update Package Explorer [package
 			// explorer]
 			// if the input to the viewer is deleted then refresh to avoid the
 			// display of stale elements
 			if (inputDeleted())
 				return;
 			processDelta(event.getDelta());
 		} catch (ModelException e) {
 			DLTKUIPlugin.log(e);
 		}
 	}
 
 	private boolean inputDeleted() {
 		if (fInput == null)
 			return false;
 		if ((fInput instanceof IModelElement)
 				&& ((IModelElement) fInput).exists())
 			return false;
 		if ((fInput instanceof IResource) && ((IResource) fInput).exists())
 			return false;
 		if (fInput instanceof WorkingSetModel)
 			return false;
 		postRefresh(fInput, ORIGINAL, fInput);
 		return true;
 	}
 
 	/*
 	 * (non-Javadoc) Method declared on IContentProvider.
 	 */
 	public void dispose() {
 		super.dispose();
 		DLTKCore.removeElementChangedListener(this);
 		fScriptFolderProvider.dispose();
 	}
 
 	// ------ Code which delegates to ScriptFolderProvider ------
 	private boolean needsToDelegateGetChildren(Object element) {
 		int type = -1;
 		if (element instanceof IModelElement)
 			type = ((IModelElement) element).getElementType();
 		return (!fIsFlatLayout && (type == IModelElement.SCRIPT_FOLDER
 				|| type == IModelElement.PROJECT_FRAGMENT
 				|| type == IModelElement.SCRIPT_PROJECT || element instanceof IFolder));
 	}
 
 	public Object[] getChildren(Object parentElement) {
 		Object[] children = NO_CHILDREN;
 		try {
 			if (parentElement instanceof IScriptModel)
 				return concatenate(
 						getDLTKProjects((IScriptModel) parentElement),
 						getNonDLTKProjects((IScriptModel) parentElement));
 			if (parentElement instanceof BuildPathContainer)
 				return getContainerProjectFragments((BuildPathContainer) parentElement);
 			if (parentElement instanceof IProject)
 				return ((IProject) parentElement).members();
 			if (needsToDelegateGetChildren(parentElement)) {
 				Object[] ScriptFolders = fScriptFolderProvider
 						.getChildren(parentElement);
 				children = getWithParentsResources(ScriptFolders, parentElement);
 			} else {
 				children = super.getChildren(parentElement);
 			}
 			if (parentElement instanceof IDLTKProject) {
 				IDLTKProject project = (IDLTKProject) parentElement;
 				return rootsAndContainers(project, children);
 			} else
 				return children;
 		} catch (CoreException e) {
 			return NO_CHILDREN;
 		}
 	}
 
 	private Object[] rootsAndContainers(IDLTKProject project, Object[] roots)
 			throws ModelException {
 		List result = new ArrayList(roots.length);
 		Set containers = new HashSet(roots.length);
 		Set containedRoots = new HashSet(roots.length);
 
 		IBuildpathEntry[] entries = project.getRawBuildpath();
 		for (int i = 0; i < entries.length; i++) {
 			IBuildpathEntry entry = entries[i];
 			if (entry != null
 					&& entry.getEntryKind() == IBuildpathEntry.BPE_CONTAINER) {
 				IProjectFragment[] roots1 = project.findProjectFragments(entry);
 				containedRoots.addAll(Arrays.asList(roots1));
 				containers.add(entry);
 			}
 		}
 		for (int i = 0; i < roots.length; i++) {
 			if (roots[i] instanceof IProjectFragment) {
 				if (!containedRoots.contains(roots[i])) {
 					result.add(roots[i]);
 				}
 			} else {
 				result.add(roots[i]);
 			}
 		}
 		for (Iterator each = containers.iterator(); each.hasNext();) {
 			IBuildpathEntry element = (IBuildpathEntry) each.next();
 			result.add(new BuildPathContainer(project, element));
 		}
 		return result.toArray();
 	}
 
 	protected Object[] getContainerProjectFragments(BuildPathContainer container) {
 		return container.getChildren(container);
 	}
 
 	private Object[] getNonDLTKProjects(IScriptModel model)
 			throws ModelException {
 		return model.getForeignResources();
 	}
 
 	public Object getParent(Object child) {
 		if (needsToDelegateGetParent(child)) {
 			return fScriptFolderProvider.getParent(child);
 		} else
 			return super.getParent(child);
 	}
 
 	protected Object internalGetParent(Object element) {
 		// since we insert logical package containers we have to fix
 		// up the parent for package fragment roots so that they refer
 		// to the container and containers refere to the project
 		//
 		if (element instanceof IProjectFragment) {
 			IProjectFragment root = (IProjectFragment) element;
 			IDLTKProject project = root.getScriptProject();
 			try {
 				IBuildpathEntry[] entries = project.getRawBuildpath();
 				for (int i = 0; i < entries.length; i++) {
 					IBuildpathEntry entry = entries[i];
 					if (entry.getEntryKind() == IBuildpathEntry.BPE_CONTAINER) {
 						if (BuildPathContainer.contains(project, entry, root))
 							return new BuildPathContainer(project, entry);
 					}
 				}
 			} catch (ModelException e) {
 				// fall through
 			}
 		}
 		if (element instanceof BuildPathContainer) {
 			return ((BuildPathContainer) element).getScriptProject();
 		}
 		return super.internalGetParent(element);
 	}
 
 	private boolean needsToDelegateGetParent(Object element) {
 		int type = -1;
 		if (element instanceof IModelElement)
 			type = ((IModelElement) element).getElementType();
 		return (!fIsFlatLayout && type == IModelElement.SCRIPT_FOLDER);
 	}
 
 	/**
 	 * Returns the given objects with the resources of the parent.
 	 */
 	private Object[] getWithParentsResources(Object[] existingObject,
 			Object parent) {
 		Object[] objects = super.getChildren(parent);
 		List list = new ArrayList();
 		// Add everything that is not a ScriptFolder
 		for (int i = 0; i < objects.length; i++) {
 			Object object = objects[i];
 			if (!(object instanceof ScriptFolder)) {
 				list.add(object);
 			}
 		}
 		if (existingObject != null)
 			list.addAll(Arrays.asList(existingObject));
 		return list.toArray();
 	}
 
 	/*
 	 * (non-Javadoc) Method declared on IContentProvider.
 	 */
 	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 		super.inputChanged(viewer, oldInput, newInput);
 		fScriptFolderProvider.inputChanged(viewer, oldInput, newInput);
 		fViewer = (TreeViewer) viewer;
 		if (oldInput == null && newInput != null) {
 			DLTKCore.addElementChangedListener(this);
 		} else if (oldInput != null && newInput == null) {
 			DLTKCore.removeElementChangedListener(this);
 		}
 		fInput = newInput;
 	}
 
 	// ------ delta processing ------
 	/**
 	 * Processes a delta recursively. When more than two children are affected
 	 * the tree is fully refreshed starting at this node. The delta is processed
 	 * in the current thread but the viewer updates are posted to the UI thread.
 	 */
 	private void processDelta(IModelElementDelta delta) throws ModelException {
 		int kind = delta.getKind();
 		int flags = delta.getFlags();
 		IModelElement element = delta.getElement();
 		int elementType = element.getElementType();
 		if (elementType != IModelElement.SCRIPT_MODEL
 				&& elementType != IModelElement.SCRIPT_PROJECT) {
 			IDLTKProject proj = element.getScriptProject();
 			if (proj == null || !proj.getProject().isOpen()) // TODO: Not
 				/*
 				 * needed if parent already did the 'open' check!
 				 */
 				return;
 		}
 		if (!fIsFlatLayout && elementType == IModelElement.SCRIPT_FOLDER) {
 			fScriptFolderProvider.processDelta(delta);
 			if (processResourceDeltas(delta.getResourceDeltas(), element))
 				return;
 			handleAffectedChildren(delta, element);
 			return;
 		}
 		if (elementType == IModelElement.SOURCE_MODULE) {
 			ISourceModule cu = (ISourceModule) element;
 			if (!DLTKModelUtil.isPrimary(cu)) {
 				return;
 			}
 			if (!getProvideMembers() && cu.isWorkingCopy()
 					&& kind == IModelElementDelta.CHANGED) {
 				return;
 			}
 			if ((kind == IModelElementDelta.CHANGED)
 					&& !isStructuralCUChange(flags)) {
 				return; // test moved ahead
 			}
 			if (!isOnBuildpath(cu)) {
 				return;
 			}
 		}
 		if (elementType == IModelElement.SCRIPT_PROJECT) {
 			// handle open and closing of a project
 			if ((flags & (IModelElementDelta.F_CLOSED | IModelElementDelta.F_OPENED)) != 0) {
 				postRefresh(element, ORIGINAL, element);
 				return;
 			}
 			// if the raw class path has changed we refresh the entire project
 			if ((flags & IModelElementDelta.F_BUILDPATH_CHANGED) != 0) {
 				postRefresh(element, ORIGINAL, element);
 				return;
 			}
 		}
 		if (kind == IModelElementDelta.REMOVED) {
 			Object parent = internalGetParent(element);
 			if (element instanceof IScriptFolder) {
 				// refresh package fragment root to allow filtering empty
 				// (parent) packages: bug 72923
 				if (fViewer.testFindItem(parent) != null)
 					postRefresh(parent, PARENT, element);
 				return;
 			}
 			postRemove(element);
 			if (parent instanceof IScriptFolder)
 				postUpdateIcon((IScriptFolder) parent);
 			// we are filtering out empty subpackages, so we
 			// a package becomes empty we remove it from the viewer.
 			if (isScriptFolderEmpty(element.getParent())) {
 				if (fViewer.testFindItem(parent) != null)
 					postRefresh(internalGetParent(parent), GRANT_PARENT,
 							element);
 			}
 			return;
 		}
 		if (kind == IModelElementDelta.ADDED) {
 			Object parent = internalGetParent(element);
 			// we are filtering out empty subpackages, so we
 			// have to handle additions to them specially.
 			if (parent instanceof IScriptFolder) {
 				Object grandparent = internalGetParent(parent);
 				// 1GE8SI6: ITPJUI:WIN98 - Rename is not shown in Packages View
 				// avoid posting a refresh to an unvisible parent
 				if (parent.equals(fInput)) {
 					postRefresh(parent, PARENT, element);
 				} else {
 					// refresh from grandparent if parent isn't visible yet
 					if (fViewer.testFindItem(parent) == null)
 						postRefresh(grandparent, GRANT_PARENT, element);
 					else {
 						postRefresh(parent, PARENT, element);
 					}
 				}
 				return;
 			} else {
 				postAdd(parent, element);
 			}
 		}
 		if (elementType == IModelElement.SOURCE_MODULE) {
 			if (kind == IModelElementDelta.CHANGED) {
 				// isStructuralCUChange already performed above
 				postRefresh(element, ORIGINAL, element);
 				updateSelection(delta);
 			}
 			return;
 		}
 		// // no changes possible in class files
 		// if (elementType == IModelElement.CLASS_FILE)
 		// return;
 		if (elementType == IModelElement.PROJECT_FRAGMENT) {
 			// the contents of an external archive has changed
 			if ((flags & IModelElementDelta.F_ARCHIVE_CONTENT_CHANGED) != 0) {
 				postRefresh(element, ORIGINAL, element);
 				return;
 			}
 			// the source attachment of a archive has changed
 			// if ((flags & (IModelElementDelta.F_SOURCEATTACHED |
 			// IModelElementDelta.F_SOURCEDETACHED)) != 0)
 			// postUpdateIcon(element);
 			if (isBuildPathChange(delta)) {
 				// throw the towel and do a full refresh of the affected script
 				// project.
 				postRefresh(element.getScriptProject(), PROJECT, element);
 				return;
 			}
 		}
 		if (processResourceDeltas(delta.getResourceDeltas(), element))
 			return;
 		handleAffectedChildren(delta, element);
 	}
 
 	private static boolean isStructuralCUChange(int flags) {
 		// No refresh on working copy creation (F_PRIMARY_WORKING_COPY)
 		return ((flags & IModelElementDelta.F_CHILDREN) != 0)
 				|| ((flags & (IModelElementDelta.F_CONTENT | IModelElementDelta.F_FINE_GRAINED)) == IModelElementDelta.F_CONTENT);
 	}
 
 	/* package */void handleAffectedChildren(IModelElementDelta delta,
 			IModelElement element) throws ModelException {
 		IModelElementDelta[] affectedChildren = delta.getAffectedChildren();
 		if (affectedChildren.length > 1) {
 			// a package fragment might become non empty refresh from the parent
 			if (element instanceof IScriptFolder) {
 				IModelElement parent = (IModelElement) internalGetParent(element);
 				// 1GE8SI6: ITPJUI:WIN98 - Rename is not shown in Packages View
 				// avoid posting a refresh to an unvisible parent
 				if (element.equals(fInput)) {
 					postRefresh(element, ORIGINAL, element);
 				} else {
 					postRefresh(parent, PARENT, element);
 				}
 				return;
 			}
 			// more than one child changed, refresh from here downwards
 			if (element instanceof IProjectFragment) {
 				Object toRefresh = skipProjectProjectFragment((IProjectFragment) element);
 				postRefresh(toRefresh, ORIGINAL, toRefresh);
 			} else {
 				postRefresh(element, ORIGINAL, element);
 			}
 			return;
 		}
 		processAffectedChildren(affectedChildren);
 	}
 
 	protected void processAffectedChildren(IModelElementDelta[] affectedChildren)
 			throws ModelException {
 		for (int i = 0; i < affectedChildren.length; i++) {
 			processDelta(affectedChildren[i]);
 		}
 	}
 
 	private boolean isOnBuildpath(ISourceModule element) {
 		IDLTKProject project = element.getScriptProject();
 		if (project == null || !project.exists())
 			return false;
 		return project.isOnBuildpath(element);
 	}
 
 	/**
 	 * Updates the selection. It finds newly added elements and selects them.
 	 */
 	private void updateSelection(IModelElementDelta delta) {
 		final IModelElement addedElement = findAddedElement(delta);
 		if (addedElement != null) {
 			final StructuredSelection selection = new StructuredSelection(
 					addedElement);
 			postRunnable(new Runnable() {
 				public void run() {
 					Control ctrl = fViewer.getControl();
 					if (ctrl != null && !ctrl.isDisposed()) {
 						// 19431
 						// if the item is already visible then select it
 						if (fViewer.testFindItem(addedElement) != null)
 							fViewer.setSelection(selection);
 					}
 				}
 			});
 		}
 	}
 
 	private IModelElement findAddedElement(IModelElementDelta delta) {
 		if (delta.getKind() == IModelElementDelta.ADDED)
 			return delta.getElement();
 		IModelElementDelta[] affectedChildren = delta.getAffectedChildren();
 		for (int i = 0; i < affectedChildren.length; i++)
 			return findAddedElement(affectedChildren[i]);
 		return null;
 	}
 
 	/**
 	 * Updates the package icon
 	 */
 	private void postUpdateIcon(final IModelElement element) {
 		postRunnable(new Runnable() {
 			public void run() {
 				// 1GF87WR: ITPUI:ALL - SWTEx + NPE closing a workbench window.
 				Control ctrl = fViewer.getControl();
 				if (ctrl != null && !ctrl.isDisposed())
 					fViewer.update(element,
 							new String[] { IBasicPropertyConstants.P_IMAGE });
 			}
 		});
 	}
 
 	/**
 	 * Process a resource delta.
 	 * 
 	 * @return true if the parent got refreshed
 	 */
 	private boolean processResourceDelta(IResourceDelta delta, Object parent) {
 		int status = delta.getKind();
 		int flags = delta.getFlags();
 		IResource resource = delta.getResource();
 		// filter out changes affecting the output folder
 		if (resource == null)
 			return false;
 		// this could be optimized by handling all the added children in the
 		// parent
 		if ((status & IResourceDelta.REMOVED) != 0) {
 			if (parent instanceof IScriptFolder) {
 				// refresh one level above to deal with empty package filtering
 				// properly
 				postRefresh(internalGetParent(parent), PARENT, parent);
 				return true;
 			} else
 				postRemove(resource);
 		}
 		if ((status & IResourceDelta.ADDED) != 0) {
 			if (parent instanceof IScriptFolder) {
 				// refresh one level above to deal with empty package filtering
 				// properly
 				postRefresh(internalGetParent(parent), PARENT, parent);
 				return true;
 			} else
 				postAdd(parent, resource);
 		}
 		// open/close state change of a project
 		if ((flags & IResourceDelta.OPEN) != 0) {
 			postProjectStateChanged(internalGetParent(parent));
 			return true;
 		}
 		processResourceDeltas(delta.getAffectedChildren(), resource);
 		return false;
 	}
 
 	public void setIsFlatLayout(boolean state) {
 		fIsFlatLayout = state;
 	}
 
 	/**
 	 * Process resource deltas.
 	 * 
 	 * @return true if the parent got refreshed
 	 */
 	private boolean processResourceDeltas(IResourceDelta[] deltas, Object parent) {
 		if (deltas == null)
 			return false;
 		if (deltas.length > 1) {
 			// more than one child changed, refresh from here downwards
 			postRefresh(parent, ORIGINAL, parent);
 			return true;
 		}
 		for (int i = 0; i < deltas.length; i++) {
 			if (processResourceDelta(deltas[i], parent))
 				return true;
 		}
 		return false;
 	}
 
 	private void postRefresh(Object root, int relation, Object affectedElement) {
 		// JFace doesn't refresh when object isn't part of the viewer
 		// Therefore move the refresh start down to the viewer's input
 		if (isParent(root, fInput))
 			root = fInput;
 		List toRefresh = new ArrayList(1);
 		toRefresh.add(root);
 		augmentElementToRefresh(toRefresh, relation, affectedElement);
 		postRefresh(toRefresh, true);
 	}
 
 	protected void augmentElementToRefresh(List toRefresh, int relation,
 			Object affectedElement) {
 	}
 
 	boolean isParent(Object root, Object child) {
 		Object parent = getParent(child);
 		if (parent == null)
 			return false;
 		if (parent.equals(root))
 			return true;
 		return isParent(root, parent);
 	}
 
 	protected void postRefresh(final List toRefresh, final boolean updateLabels) {
 		postRunnable(new Runnable() {
 			public void run() {
 				Control ctrl = fViewer.getControl();
 				if (ctrl != null && !ctrl.isDisposed()) {
 					for (Iterator iter = toRefresh.iterator(); iter.hasNext();) {
 						fViewer.refresh(iter.next(), updateLabels);
 					}
 				}
 			}
 		});
 	}
 
 	protected void postAdd(final Object parent, final Object element) {
 		postRunnable(new Runnable() {
 			public void run() {
 				Control ctrl = fViewer.getControl();
 				if (ctrl != null && !ctrl.isDisposed()) {
 					// TODO workaround for 39754 New projects being added to the
 					// TreeViewer twice
 					if (fViewer.testFindItem(element) == null)
 						fViewer.add(parent, element);
 				}
 			}
 		});
 	}
 
 	protected void postRemove(final Object element) {
 		postRunnable(new Runnable() {
 			public void run() {
 				Control ctrl = fViewer.getControl();
 				if (ctrl != null && !ctrl.isDisposed()) {
 					fViewer.remove(element);
 				}
 			}
 		});
 	}
 
 	protected void postProjectStateChanged(final Object root) {
 		postRunnable(new Runnable() {
 			public void run() {
 				// fPart.projectStateChanged(root);
 				Control ctrl = fViewer.getControl();
 				if (ctrl != null && !ctrl.isDisposed()) {
 					fViewer.refresh(root, true);
 					// trigger a syntetic selection change so that action
 					// refresh their
 					// enable state.
 					fViewer.setSelection(fViewer.getSelection());
 				}
 			}
 		});
 	}
 
 	/* package */void postRunnable(final Runnable r) {
 		Control ctrl = fViewer.getControl();
 		final Runnable trackedRunnable = new Runnable() {
 			public void run() {
 				try {
 					r.run();
 				} finally {
 					removePendingChange();
 				}
 			}
 		};
 		if (ctrl != null && !ctrl.isDisposed()) {
 			addPendingChange();
 			try {
 				ctrl.getDisplay().asyncExec(trackedRunnable);
 			} catch (RuntimeException e) {
 				removePendingChange();
 				throw e;
 			} catch (Error e) {
 				removePendingChange();
 				throw e;
 			}
 		}
 	}
 
 	// ------ Pending change management due to the use of asyncExec in
 	// postRunnable.
 	public synchronized boolean hasPendingChanges() {
 		return fPendingChanges > 0;
 	}
 
 	private synchronized void addPendingChange() {
 		fPendingChanges++;
 	}
 
 	synchronized void removePendingChange() {
 		fPendingChanges--;
 		if (fPendingChanges < 0)
 			fPendingChanges = 0;
 	}
 }
