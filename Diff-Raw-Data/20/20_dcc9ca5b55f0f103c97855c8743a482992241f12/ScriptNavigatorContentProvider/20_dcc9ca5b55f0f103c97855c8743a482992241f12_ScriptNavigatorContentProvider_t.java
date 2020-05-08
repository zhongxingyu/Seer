 /*******************************************************************************
  * Copyright (c) 2003, 2006 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.dltk.internal.ui.navigator;
 
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IScriptModel;
 import org.eclipse.dltk.internal.ui.navigator.IExtensionStateConstants.Values;
 import org.eclipse.dltk.ui.DLTKUIPlugin;
 import org.eclipse.dltk.ui.PreferenceConstants;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.ui.IMemento;
 import org.eclipse.ui.navigator.ICommonContentExtensionSite;
 import org.eclipse.ui.navigator.IExtensionStateModel;
 import org.eclipse.ui.navigator.IPipelinedTreeContentProvider;
 import org.eclipse.ui.navigator.PipelinedShapeModification;
 import org.eclipse.ui.navigator.PipelinedViewerUpdate;
 
 public class ScriptNavigatorContentProvider extends
 		ScriptExplorerContentProvider implements IPipelinedTreeContentProvider {
 
 	public ScriptNavigatorContentProvider() {
 		super(false);
 	}
 
 	public ScriptNavigatorContentProvider(boolean provideMembers) {
 		super(provideMembers);
 	}
 
 	public static final String JDT_EXTENSION_ID = "org.eclipse.jdt.ui.javaContent"; //$NON-NLS-1$ 
 
 	private IExtensionStateModel fStateModel;
 
 	private Object fRealInput;
 
 	public void init(ICommonContentExtensionSite commonContentExtensionSite) {
 		IExtensionStateModel stateModel = commonContentExtensionSite
 				.getExtensionStateModel();
 		IMemento memento = commonContentExtensionSite.getMemento();
 
 		fStateModel = stateModel;
 		restoreState(memento);
 		fStateModel.addPropertyChangeListener(new IPropertyChangeListener() {
 			public void propertyChange(PropertyChangeEvent event) {
 				if (Values.IS_LAYOUT_FLAT.equals(event.getProperty())) {
 					if (event.getNewValue() != null) {
 						boolean newValue = ((Boolean) event.getNewValue())
 								.booleanValue() ? true : false;
 						setIsFlatLayout(newValue);
 					}
 				}
 
 			}
 		});
 
 		IPreferenceStore store = getPreferenceStore();
 		boolean showCUChildren = store
 				.getBoolean(PreferenceConstants.SHOW_SOURCE_MODULE_CHILDREN);
 		setProvideMembers(showCUChildren);
 	}
 
 	protected IPreferenceStore getPreferenceStore() {
 		return DLTKUIPlugin.getDefault().getPreferenceStore();
 	}
 
 	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 		fRealInput = newInput;
 		super.inputChanged(viewer, oldInput, findInputElement(newInput));
 	}
 
 	public Object getParent(Object element) {
 		Object parent = super.getParent(element);
 		if (parent instanceof IScriptModel) {
 			return getViewerInput() != null ? fRealInput : parent;
 		}
 		return parent;
 	}
 
 	public Object[] getElements(Object inputElement) {
 		if (inputElement instanceof IWorkspaceRoot)
 			return super.getElements(DLTKCore
 					.create((IWorkspaceRoot) inputElement));
		
 		return super.getElements(inputElement);
 	}
 
 	private Object findInputElement(Object newInput) {
 		if (newInput instanceof IWorkspaceRoot) {
 			return DLTKCore.create((IWorkspaceRoot) newInput);
 		}
 		return newInput;
 	}
 
 	public void restoreState(IMemento memento) {
 
 	}
 
 	public void saveState(IMemento memento) {
 
 	}
 
 	public void getPipelinedChildren(Object parent, Set currentChildren) {
 		Object[] children = getChildren(parent);
 		for (Iterator iter = currentChildren.iterator(); iter.hasNext();)
 			if (iter.next() instanceof IResource)
 				iter.remove();
 		currentChildren.addAll(Arrays.asList(children));
 	}
 
 	public void getPipelinedElements(Object input, Set currentElements) {
 		Object[] children = getElements(input);
 
 		for (Iterator iter = currentElements.iterator(); iter.hasNext();)
 			if (iter.next() instanceof IResource)
 				iter.remove();
 
 		currentElements.addAll(Arrays.asList(children));
 	}
 
 	public Object getPipelinedParent(Object object, Object suggestedParent) {
 		return getParent(object);
 	}
 
 	public PipelinedShapeModification interceptAdd(
 			PipelinedShapeModification addModification) {
 		convertToJavaElements(addModification);
 		return addModification;
 	}
 
 	public PipelinedShapeModification interceptRemove(
 			PipelinedShapeModification removeModification) {
 		convertToJavaElements(removeModification.getChildren());
 		return removeModification;
 	}
 
 	/**
 	 * Converts the shape modification to use Java elements.
 	 * 
 	 * 
 	 * @param modification
 	 *            the shape modification to convert
 	 */
 	private boolean convertToJavaElements(
 			PipelinedShapeModification modification) {
 		Object parent = modification.getParent();
 		if (parent instanceof IContainer) {
 			IModelElement element = DLTKCore.create((IContainer) parent);
 			if (element != null && element.exists()) {
 				// we don't convert the root
 				if (!(element instanceof IScriptModel))
 					modification.setParent(element);
 				return convertToJavaElements(modification.getChildren());
 
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Converts the shape modification to use Java elements.
 	 * 
 	 * 
 	 * @param currentChildren
 	 *            The set of current children that would be contributed or
 	 *            refreshed in the viewer.
 	 */
 	private boolean convertToJavaElements(Set currentChildren) {
 
 		LinkedHashSet convertedChildren = new LinkedHashSet();
 		IModelElement newChild;
 		for (Iterator childrenItr = currentChildren.iterator(); childrenItr
 				.hasNext();) {
 			Object child = childrenItr.next();
 			if (child instanceof IResource)
 				if ((newChild = DLTKCore.create((IResource) child)) != null
 						&& newChild.exists()) {
 					childrenItr.remove();
 					convertedChildren.add(newChild);
 				}
 		}
 		if (!convertedChildren.isEmpty()) {
 			currentChildren.addAll(convertedChildren);
 			return true;
 		}
 		return false;
 
 	}
 
 	public boolean interceptRefresh(PipelinedViewerUpdate refreshSynchronization) {
 		return convertToJavaElements(refreshSynchronization.getRefreshTargets());
 
 	}
 
 	public boolean interceptUpdate(PipelinedViewerUpdate updateSynchronization) {
 		return convertToJavaElements(updateSynchronization.getRefreshTargets());
 	}
 
 	protected void postAdd(final Object parent, final Object element) {
 		if (parent instanceof IScriptModel)
 			super.postAdd(((IScriptModel) parent).getWorkspace(), element);
 		else
 			super.postAdd(parent, element);
 	}
 
 	protected void postRefresh(final List toRefresh, final boolean updateLabels) {
 		for (Iterator iter = toRefresh.iterator(); iter.hasNext();) {
 			Object element = iter.next();
 			if (element instanceof IScriptModel) {
 				iter.remove();
 				toRefresh.add(fRealInput);
 				super.postRefresh(toRefresh, updateLabels);
 				return;
 			}
 		}
 		super.postRefresh(toRefresh, updateLabels);
 	}
 
 }
