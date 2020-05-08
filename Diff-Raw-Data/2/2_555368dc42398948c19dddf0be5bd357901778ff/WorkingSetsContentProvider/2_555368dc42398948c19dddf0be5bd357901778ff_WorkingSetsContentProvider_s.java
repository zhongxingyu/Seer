 /*******************************************************************************
  * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.ui.views.workingsets;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.tcf.te.runtime.events.EventManager;
 import org.eclipse.tcf.te.runtime.interfaces.workingsets.IWorkingSetElement;
 import org.eclipse.tcf.te.ui.views.events.ViewerContentChangeEvent;
 import org.eclipse.tcf.te.ui.views.interfaces.ICategory;
 import org.eclipse.tcf.te.ui.views.interfaces.IUIConstants;
 import org.eclipse.tcf.te.ui.views.interfaces.workingsets.IWorkingSetIDs;
 import org.eclipse.tcf.te.ui.views.internal.ViewRoot;
 import org.eclipse.tcf.te.ui.views.workingsets.activator.UIPlugin;
 import org.eclipse.tcf.te.ui.views.workingsets.nls.Messages;
 import org.eclipse.ui.ILocalWorkingSetManager;
 import org.eclipse.ui.IMemento;
 import org.eclipse.ui.IWorkingSet;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.internal.navigator.NavigatorContentService;
 import org.eclipse.ui.internal.navigator.NavigatorFilterService;
 import org.eclipse.ui.navigator.CommonNavigator;
 import org.eclipse.ui.navigator.CommonViewer;
 import org.eclipse.ui.navigator.ICommonContentExtensionSite;
 import org.eclipse.ui.navigator.ICommonContentProvider;
 import org.eclipse.ui.navigator.IExtensionStateModel;
 import org.eclipse.ui.navigator.INavigatorFilterService;
 
 /**
  * Provides children and parents for IWorkingSets.
  * <p>
  * Copied and adapted from <code>org.eclipse.ui.internal.navigator.workingsets.WorkingSetContentProvider</code>.
  */
 @SuppressWarnings("restriction")
 public class WorkingSetsContentProvider implements ICommonContentProvider {
 
 	/**
 	 * The extension id for the WorkingSet extension.
 	 */
 	public static final String EXTENSION_ID = "org.eclipse.tcf.te.ui.views.navigator.content.workingSets"; //$NON-NLS-1$
 
 	/**
 	 * A key used by the Extension State Model to keep track of whether top level Working Sets or
 	 * Elements should be shown in the viewer.
 	 */
 	public static final String SHOW_TOP_LEVEL_WORKING_SETS = EXTENSION_ID + ".showTopLevelWorkingSets"; //$NON-NLS-1$
 
 	/**
 	 * Working set filter extension id.
 	 */
 	private static final String WORKING_SET_FILTER_ID = "org.eclipse.tcf.te.ui.views.navigator.filters.workingSet"; //$NON-NLS-1$
 
 	/**
 	 * Child elements returned by this content provider in case there
 	 * are no children.
 	 */
 	private static final Object[] NO_CHILDREN = new Object[0];
 
 	// The common navigator working set content extension state model.
 	private IExtensionStateModel extensionStateModel;
 	// The parent viewer object
 	private CommonViewer viewer;
 	// The working set filter instance
 	private WorkingSetFilter filter;
 
 	/**
 	 * The root mode listener listens to property changes of the working set content
 	 * extension state model to update the viewer root mode if the user changes the
 	 * top level element type.
 	 */
 	private IPropertyChangeListener rootModeListener = new IPropertyChangeListener() {
 
 		/* (non-Javadoc)
 		 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
 		 */
 		@SuppressWarnings("synthetic-access")
 		@Override
 		public void propertyChange(PropertyChangeEvent event) {
 			if (SHOW_TOP_LEVEL_WORKING_SETS.equals(event.getProperty())) {
 				updateRootMode(true);
 			}
 		}
 	};
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.navigator.ICommonContentProvider#init(org.eclipse.ui.navigator.ICommonContentExtensionSite)
 	 */
 	@Override
 	public void init(ICommonContentExtensionSite config) {
 		NavigatorContentService cs = (NavigatorContentService) config.getService();
 		if (!(cs.getViewer() instanceof CommonViewer)) return;
 
 		viewer = (CommonViewer) cs.getViewer();
 
 		// Get the filter service to activate the working set viewer filter
 		INavigatorFilterService filterService = cs.getFilterService();
 		// The working set filter should be among the visible filters
 		ViewerFilter[] filters = filterService.getVisibleFilters(false);
 		for (ViewerFilter candidate : filters) {
 			if (candidate instanceof WorkingSetFilter) {
 				filter = (WorkingSetFilter)candidate;
 			}
 		}
 
 		if (filter != null && !filterService.isActive(WORKING_SET_FILTER_ID)) {
 			if (filterService instanceof NavigatorFilterService) {
 				NavigatorFilterService navFilterService = (NavigatorFilterService)filterService;
 				navFilterService.addActiveFilterIds(new String[] { WORKING_SET_FILTER_ID });
 				navFilterService.updateViewer();
 			}
 		}
 		else if (filter == null) {
 			IStatus status = new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(),
 										"Required filter " + WORKING_SET_FILTER_ID  //$NON-NLS-1$
 										+ " is not present. Working set support will not function correctly.");   //$NON-NLS-1$
 			UIPlugin.getDefault().getLog().log(status);
 		}
 
 		extensionStateModel = config.getExtensionStateModel();
 		extensionStateModel.addPropertyChangeListener(rootModeListener);
 
 		updateRootMode(false);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.navigator.IMementoAware#restoreState(org.eclipse.ui.IMemento)
 	 */
 	@Override
 	public void restoreState(IMemento memento) {
 		// Determine the view local working set manager instance
 		WorkingSetViewStateManager mng = (WorkingSetViewStateManager)Platform.getAdapterManager().getAdapter(viewer.getCommonNavigator(), WorkingSetViewStateManager.class);
 		ILocalWorkingSetManager manager = mng != null ? mng.getLocalWorkingSetManager() : null;
 
 		// Recreate the local automatic working sets
 		if (manager != null) {
 			// Create the "Others" working set if not restored from the memento
 			IWorkingSet others = manager.getWorkingSet(Messages.ViewStateManager_others_name);
 			if (others == null) {
 				others = manager.createWorkingSet(Messages.ViewStateManager_others_name, new IAdaptable[0]);
 				others.setId(IWorkingSetIDs.ID_WS_OTHERS);
 				manager.addWorkingSet(others);
 			} else {
 				others.setId(IWorkingSetIDs.ID_WS_OTHERS);
 			}
 		}
 
 		// Trigger an update of the "Others" working set
 		ViewerContentChangeEvent event = new ViewerContentChangeEvent(viewer, ViewerContentChangeEvent.REFRESH);
 		EventManager.getInstance().fireEvent(event);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.navigator.IMementoAware#saveState(org.eclipse.ui.IMemento)
 	 */
 	@Override
 	public void saveState(IMemento memento) {
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
 	 */
 	@Override
 	public Object[] getChildren(Object parentElement) {
 		if (parentElement instanceof IWorkingSet) {
 			// Return the working set elements
 			return getWorkingSetElements((IWorkingSet)parentElement);
 		}
 		else if (parentElement instanceof WorkingSetViewStateManager) {
 			List<IWorkingSet> workingSets = ((WorkingSetViewStateManager)parentElement).getVisibleWorkingSets();
 			if (workingSets != null && !workingSets.isEmpty()) {
 				return workingSets.toArray();
 			}
 		}
 
 		return NO_CHILDREN;
 	}
 
 	/* default */ IAdaptable[] getWorkingSetElements(IWorkingSet workingSet) {
 		Assert.isNotNull(workingSet);
 		List<IAdaptable> elements = new ArrayList<IAdaptable>();
 		for (IAdaptable candidate : workingSet.getElements()) {
 			if (candidate instanceof WorkingSetElementHolder) {
 				WorkingSetElementHolder holder = (WorkingSetElementHolder)candidate;
 				IWorkingSetElement element = holder.getElement();
 				// If the element is null, try to look up the element through the content provider
 				if (element == null) {
 		    		List<Object> elementCandidates = new ArrayList<Object>();
 					ITreeContentProvider provider = (ITreeContentProvider)viewer.getContentProvider();
 		    		Object[] viewElements = provider.getElements(ViewRoot.getInstance());
 		    		for (Object viewElement : viewElements) {
 		    			if (viewElement instanceof ICategory) {
 		    				elementCandidates.addAll(Arrays.asList(provider.getChildren(viewElement)));
 		    			} else {
 		    				elementCandidates.add(viewElement);
 		    			}
 		    		}
 		    		provider.dispose();
 
 					for (Object elementCandidate : elementCandidates) {
 						if (elementCandidate instanceof IWorkingSetElement && ((IWorkingSetElement)elementCandidate).getElementId().equals(holder.getElementId())) {
 							holder.setElement((IWorkingSetElement)elementCandidate);
 							element = holder.getElement();
 							break;
 						}
 					}
 				}
 				if (element != null) elements.add(element);
 			} else {
 				elements.add(candidate);
 			}
 		}
 		return elements.toArray(new IAdaptable[elements.size()]);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
 	 */
 	@Override
 	public Object getParent(Object element) {
		CommonNavigator navigator = viewer.getCommonNavigator();
 		WorkingSetViewStateManager manager = navigator != null ? (WorkingSetViewStateManager)Platform.getAdapterManager().getAdapter(navigator, WorkingSetViewStateManager.class) : null;
 
 		if (element instanceof IWorkingSet) {
 			List<IWorkingSet> allWorkingSets = manager != null ? Arrays.asList(manager.getAllWorkingSets()) : new ArrayList<IWorkingSet>();
 			if (allWorkingSets.contains(element)) {
 				return manager;
 			}
 		}
 		else if (element instanceof WorkingSetElementHolder) {
 			String wsName = ((WorkingSetElementHolder)element).getWorkingSetName();
 			if (wsName != null) {
 				IWorkingSet ws = PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSet(wsName);
 				if (ws == null && manager != null) {
 					ws = manager.getLocalWorkingSetManager().getWorkingSet(wsName);
 				}
 				return ws;
 			}
 		}
 		else if (element instanceof IWorkingSetElement) {
 			if (navigator != null) {
 				if (navigator.getRootMode() == IUIConstants.MODE_WORKING_SETS) {
 					IWorkingSet[] workingSets = PlatformUI.getWorkbench().getWorkingSetManager().getAllWorkingSets();
 					List<IWorkingSet> list = new ArrayList<IWorkingSet>();
 					list.addAll(Arrays.asList(workingSets));
 					ILocalWorkingSetManager wsManager = manager != null ? manager.getLocalWorkingSetManager() : null;
 					workingSets = wsManager != null ? wsManager.getAllWorkingSets() : new IWorkingSet[0];
 					list.addAll(Arrays.asList(workingSets));
 					for (IWorkingSet workingSet : list) {
 						IAdaptable[] wsElements = getWorkingSetElements(workingSet);
 						for (IAdaptable wsElement : wsElements) {
 							if (wsElement == element) return workingSet;
 						}
 					}
 				}
 			}
 		}
 
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
 	 */
 	@Override
 	public boolean hasChildren(Object element) {
 		if (element instanceof IWorkingSet) {
 			return ((IWorkingSet)element).getElements().length > 0;
 		}
 		return true;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getElements(java.lang.Object)
 	 */
 	@Override
 	public Object[] getElements(Object inputElement) {
 		return getChildren(inputElement);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
 	 */
 	@Override
 	public void dispose() {
 		if (extensionStateModel != null) extensionStateModel.removePropertyChangeListener(rootModeListener);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
 	 */
 	@Override
 	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 	}
 
 	/**
 	 * Update the common navigator root mode.
 	 */
 	private void updateRootMode(boolean updateInput) {
 		CommonNavigator navigator = viewer.getCommonNavigator();
 		if (navigator == null) return;
 
 		Object newInput;
 		boolean filterActive;
 		if (extensionStateModel.getBooleanProperty(SHOW_TOP_LEVEL_WORKING_SETS)) {
 			navigator.setRootMode(IUIConstants.MODE_WORKING_SETS);
 			newInput = Platform.getAdapterManager().getAdapter(navigator, WorkingSetViewStateManager.class);
 			filterActive = true;
 		}
 		else {
 			navigator.setRootMode(IUIConstants.MODE_NORMAL);
 			newInput = ViewRoot.getInstance();
 			filterActive = false;
 		}
 
 		if (updateInput && !newInput.equals(viewer.getInput())) {
 			viewer.setInput(newInput);
 		}
 		setFilterActive(filterActive);
 	}
 
 	/**
 	 * Update the working set viewer filter active state.
 	 */
 	private void setFilterActive(boolean active) {
 		if (filter != null) filter.setActive(active);
 	}
 }
