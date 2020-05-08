 /*******************************************************************************
  * Copyright (c) 2012, 2013 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.ide.ui.internal.structuremergeviewer;
 
 import com.google.common.eventbus.EventBus;
 import com.google.common.eventbus.Subscribe;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.eclipse.compare.CompareConfiguration;
 import org.eclipse.compare.CompareViewerPane;
 import org.eclipse.compare.CompareViewerSwitchingPane;
 import org.eclipse.compare.structuremergeviewer.DiffTreeViewer;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.emf.common.notify.Adapter;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.notify.Notifier;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.compare.Conflict;
 import org.eclipse.emf.compare.Diff;
 import org.eclipse.emf.compare.DifferenceSource;
 import org.eclipse.emf.compare.ide.ui.internal.EMFCompareIDEUIPlugin;
 import org.eclipse.emf.compare.ide.ui.internal.actions.collapse.CollapseAllModelAction;
 import org.eclipse.emf.compare.ide.ui.internal.actions.expand.ExpandAllModelAction;
 import org.eclipse.emf.compare.internal.utils.DiffUtil;
 import org.eclipse.emf.compare.rcp.ui.internal.EMFCompareConstants;
 import org.eclipse.emf.compare.rcp.ui.internal.structuremergeviewer.actions.FilterActionMenu;
 import org.eclipse.emf.compare.rcp.ui.internal.structuremergeviewer.actions.GroupActionMenu;
 import org.eclipse.emf.compare.rcp.ui.internal.structuremergeviewer.filters.IDifferenceFilter;
 import org.eclipse.emf.compare.rcp.ui.internal.structuremergeviewer.filters.IDifferenceFilterSelectionChangeEvent;
 import org.eclipse.emf.compare.rcp.ui.internal.structuremergeviewer.filters.StructureMergeViewerFilter;
 import org.eclipse.emf.compare.rcp.ui.internal.structuremergeviewer.groups.IDifferenceGroupProvider;
 import org.eclipse.emf.compare.rcp.ui.internal.structuremergeviewer.groups.StructureMergeViewerGrouper;
 import org.eclipse.emf.compare.rcp.ui.internal.structuremergeviewer.groups.impl.DefaultGroupProvider;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.edit.tree.TreeNode;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IContributionItem;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.action.ToolBarManager;
 import org.eclipse.jface.resource.JFaceResources;
 import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
 import org.eclipse.jface.viewers.IElementComparer;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.ViewerComparator;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.TreeItem;
 import org.eclipse.swt.widgets.Widget;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.internal.actions.CommandAction;
 import org.eclipse.ui.menus.IMenuService;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.eclipse.ui.services.IServiceLocator;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.Version;
 
 /**
  * @author <a href="mailto:mikael.barbero@obeo.fr">Mikael Barbero</a>
  */
 public class EMFCompareDiffTreeViewer extends DiffTreeViewer {
 
 	public static final String REQUIRED_DIFF_COLOR = "RequiredDiffColor"; //$NON-NLS-1$
 
 	public static final String REQUIRED_DIFF_BORDER_COLOR = "RequiredDiffBorderColor"; //$NON-NLS-1$
 
 	public static final String UNMERGEABLE_DIFF_COLOR = "UnmergeableDiffColor"; //$NON-NLS-1$
 
 	public static final String UNMERGEABLE_DIFF_BORDER_COLOR = "UnmergeableDiffBorderColor"; //$NON-NLS-1$
 
 	private final Color requiredDiffColor;
 
 	private final Color unmergeableDiffColor;
 
 	private final ISelectionChangedListener fSelectionChangedListener;
 
 	private final CompareViewerSwitchingPane fParent;
 
 	private ToolBarManager toolbarManager;
 
 	private Object fRoot;
 
 	/**
 	 * The difference filter that will be applied to the structure viewer. Note that this will be initialized
 	 * from {@link #createToolItems(ToolBarManager)} since that method is called from the super-constructor
 	 * and we cannot init ourselves beforehand.
 	 */
 	private StructureMergeViewerFilter structureMergeViewerFilter;
 
 	/**
 	 * This will be used by our adapter factory in order to group together the differences located under the
 	 * Comparison. Note that this will be initialized from {@link #createToolItems(ToolBarManager)} since that
 	 * method is called from the super-constructor and we cannot init ourselves beforehand.
 	 */
 	private StructureMergeViewerGrouper structureMergeViewerGrouper;
 
 	private MenuManager groupsMenuManager;
 
 	private MenuManager filtersMenuManager;
 
 	private GroupActionMenu groupActionMenu;
 
 	private FilterActionMenu filterActionMenu;
 
 	private EventBus eventBus;
 
 	private Listener fEraseItemListener;
 
 	/**
 	 * @param parent
 	 * @param adapterFactory
 	 * @param configuration
 	 */
 	public EMFCompareDiffTreeViewer(Composite parent, AdapterFactory adapterFactory,
 			CompareConfiguration configuration) {
 		super(parent, configuration);
 
 		ToolBarManager tbm = CompareViewerPane.getToolBarManager(parent.getParent());
 		if (tbm != null) {
 			tbm.removeAll();
 
 			tbm.add(new Separator("merge")); //$NON-NLS-1$
 			tbm.add(new Separator("modes")); //$NON-NLS-1$
 			tbm.add(new Separator("navigation")); //$NON-NLS-1$
 
 			createToolItems(tbm);
 			// updateActions();
 
 			tbm.update(true);
 		}
 
 		setLabelProvider(new DelegatingStyledCellLabelProvider(
 				new EMFCompareStructureMergeViewerLabelProvider(adapterFactory, this)));
 		setContentProvider(new EMFCompareStructureMergeViewerContentProvider(adapterFactory));
 
 		if (parent instanceof CompareViewerSwitchingPane) {
 			fParent = (CompareViewerSwitchingPane)parent;
 		} else {
 			fParent = null;
 		}
 
 		fSelectionChangedListener = new ISelectionChangedListener() {
 			public void selectionChanged(SelectionChangedEvent event) {
 				getControl().redraw();
 				getCompareConfiguration()
 						.setProperty(EMFCompareConstants.SMV_SELECTION, event.getSelection());
 				if (toolbarManager != null) {
 					for (IContributionItem item : toolbarManager.getItems()) {
 						item.update();
 					}
 				}
 			}
 		};
 		addSelectionChangedListener(fSelectionChangedListener);
 
 		fEraseItemListener = new Listener() {
 			public void handleEvent(Event event) {
 				EMFCompareDiffTreeViewer.this.handleEraseItemEvent(event);
 			}
 		};
 		getControl().addListener(SWT.EraseItem, fEraseItemListener);
 
 		// Wrap the defined comparer in our own.
 		setComparer(new CompareInputComparer(super.getComparer()));
 
 		if (eventBus == null) {
 			eventBus = new EventBus();
 			eventBus.register(this);
 		}
 
 		JFaceResources.getColorRegistry().put(REQUIRED_DIFF_COLOR, new RGB(215, 255, 200));
 		JFaceResources.getColorRegistry().put(REQUIRED_DIFF_BORDER_COLOR, new RGB(195, 235, 180));
 		JFaceResources.getColorRegistry().put(UNMERGEABLE_DIFF_COLOR, new RGB(255, 205, 180));
 		JFaceResources.getColorRegistry().put(UNMERGEABLE_DIFF_BORDER_COLOR, new RGB(235, 185, 160));
 
 		requiredDiffColor = JFaceResources.getColorRegistry().get(REQUIRED_DIFF_COLOR);
 		unmergeableDiffColor = JFaceResources.getColorRegistry().get(UNMERGEABLE_DIFF_COLOR);
 
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.viewers.StructuredViewer#getComparator()
 	 */
 	@Override
 	public ViewerComparator getComparator() {
 		return null;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Subscribe
 	public void recordFilterSelectionChange(IDifferenceFilterSelectionChangeEvent event) {
 		final Object property = getCompareConfiguration().getProperty(EMFCompareConstants.SELECTED_FILTERS);
 		final Collection<IDifferenceFilter> selectedFilters;
 		if (property == null) {
 			selectedFilters = new HashSet<IDifferenceFilter>();
 		} else {
 			selectedFilters = new HashSet<IDifferenceFilter>((Collection<IDifferenceFilter>)property);
 		}
 		switch (event.getAction()) {
 			case ACTIVATE:
 				selectedFilters.add(event.getFilter());
 				break;
 			case DEACTIVATE:
 				selectedFilters.remove(event.getFilter());
 				break;
 			default:
 				throw new IllegalStateException();
 		}
 		getCompareConfiguration().setProperty(EMFCompareConstants.SELECTED_FILTERS, selectedFilters);
 	}
 
 	@Subscribe
 	public void recordGroupProviderSelectionChange(IDifferenceGroupProvider differenceGroupProvider) {
 		EList<Adapter> eAdapters = ((Adapter)fRoot).getTarget().eAdapters();
 		IDifferenceGroupProvider oldDifferenceGroupProvider = (IDifferenceGroupProvider)EcoreUtil.getAdapter(
 				eAdapters, IDifferenceGroupProvider.class);
 		if (oldDifferenceGroupProvider != null) {
 			eAdapters.remove(oldDifferenceGroupProvider);
 		}
 		eAdapters.add(differenceGroupProvider);
 		getCompareConfiguration().setProperty(EMFCompareConstants.SELECTED_GROUP, differenceGroupProvider);
 	}
 
 	public void configurationPropertyChanged() {
 		getControl().redraw();
 		if (toolbarManager != null) {
 			for (IContributionItem item : toolbarManager.getItems()) {
 				item.update();
 			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.viewers.StructuredViewer#getRoot()
 	 */
 	@Override
 	public Object getRoot() {
 		return fRoot;
 	}
 
 	public void setRoot(Object root) {
 		fRoot = root;
 	}
 
 	public void createChildrenSilently(Object o) {
 		if (o instanceof Tree) {
 			createChildren((Widget)o);
 			for (TreeItem item : ((Tree)o).getItems()) {
 				createChildrenSilently(item);
 			}
 		} else if (o instanceof TreeItem) {
 			createChildren((Widget)o);
 			for (TreeItem item : ((TreeItem)o).getItems()) {
 				createChildrenSilently(item);
 			}
 		}
 	}
 
 	@Override
 	public void initialSelection() {
 		super.initialSelection();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.AbstractTreeViewer#isExpandable(java.lang.Object)
 	 */
 	@Override
 	public boolean isExpandable(Object parent) {
 		if (hasFilters()) {
 			// workaround for 65762
 			return hasFilteredChildren(parent);
 		}
 		return super.isExpandable(parent);
 	}
 
 	/**
 	 * Public method to test if a element has any children that passed the filters
 	 * 
 	 * @param parent
 	 *            the element to test
 	 * @return return <code>true</code> if the element has at least a child that passed the filters
 	 */
 	public final boolean hasFilteredChildren(Object parent) {
 		Object[] rawChildren = getRawChildren(parent);
 		return containsNonFiltered(rawChildren, parent);
 	}
 
 	private boolean containsNonFiltered(Object[] elements, Object parent) {
 		if (elements.length == 0) {
 			return false;
 		}
 		if (!hasFilters()) {
 			return true;
 		}
 		ViewerFilter[] filters = getFilters();
 		for (int i = 0; i < elements.length; i++) {
 			Object object = elements[i];
 			if (!isFiltered(object, parent, filters)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * All element filter tests must go through this method. Can be overridden by subclasses.
 	 * 
 	 * @param object
 	 *            the object to filter
 	 * @param parent
 	 *            the parent
 	 * @param filters
 	 *            the filters to apply
 	 * @return true if the element is filtered
 	 */
 	protected boolean isFiltered(Object object, Object parent, ViewerFilter[] filters) {
 		for (int i = 0; i < filters.length; i++) {
 			ViewerFilter filter = filters[i];
 			if (!filter.select(this, parent, object)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public void refreshAfterDiff(String message, Object root) {
 		if (getControl().isDisposed()) {
 			return;
 		}
 
 		if (fParent != null) {
 			fParent.setTitleArgument(message);
 		}
 
 		refresh(root);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.viewers.StructuredViewer#setComparer(org.eclipse.jface.viewers.IElementComparer)
 	 */
 	@Override
 	public void setComparer(IElementComparer comparer) {
 		// Wrap this new comparer in our own
 		super.setComparer(new CompareInputComparer(comparer));
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.structuremergeviewer.DiffTreeViewer#createToolItems(org.eclipse.jface.action.ToolBarManager)
 	 */
 	@Override
 	protected void createToolItems(ToolBarManager toolbarManager) {
 
 		this.toolbarManager = toolbarManager;
 
 		super.createToolItems(toolbarManager);
 
 		// Add extension point contributions to the structure merge viewer toolbar
 		IServiceLocator workbench = PlatformUI.getWorkbench();
 		IMenuService menuService = (IMenuService)workbench.getService(IMenuService.class);
 		if (menuService != null) {
 			menuService.populateContributionManager(toolbarManager,
 					"toolbar:org.eclipse.emf.compare.structuremergeviewer.toolbar");
 		}
 
 		Bundle uiWorkbenchBundle = Platform.getBundle("org.eclipse.ui.workbench"); //$NON-NLS-1$
 		Version junoStart = Version.parseVersion("3.103"); //$NON-NLS-1$
 
 		// XXX MBA change to 3.105 once bug #366528 is fixed
 		Version keplerStart = Version.parseVersion("3.105"); //$NON-NLS-1$
 
 		if (uiWorkbenchBundle != null && uiWorkbenchBundle.getVersion().compareTo(junoStart) >= 0
 				&& uiWorkbenchBundle.getVersion().compareTo(keplerStart) < 0) {
 			addActionsForJuno(toolbarManager);
 		}
 
 		groupActionMenu = new GroupActionMenu(getStructureMergeViewerGrouper(), getGroupsMenuManager(),
 				getDefaultGroupProvider());
 		filterActionMenu = new FilterActionMenu(getStructureMergeViewerFilter(), getFiltersMenuManager());
 
 		toolbarManager.add(new Separator());
 		toolbarManager.add(new ExpandAllModelAction(this));
 		toolbarManager.add(new CollapseAllModelAction(this));
 		toolbarManager.add(new Separator());
 		toolbarManager.add(groupActionMenu);
 		toolbarManager.add(filterActionMenu);
 	}
 
 	/**
 	 * Add the compare merge/navigation actions to the structure merge viewer toolbar.
 	 * 
 	 * @param toolbarManager
 	 *            the given toolbar.
 	 */
 	public void addActionsForJuno(ToolBarManager toolbarManager) {
 		boolean bothSidesEditable = getCompareConfiguration().isLeftEditable()
 				&& getCompareConfiguration().isRightEditable();
 		addSMVAction(toolbarManager, "org.eclipse.emf.compare.ide.ui.dropdown", //$NON-NLS-1$
 				"Select the way of merge", "icons/full/toolb16/left_to_right.gif", true); //$NON-NLS-2$
 		toolbarManager.add(new Separator());
 		if (bothSidesEditable) {
 			addSMVAction(toolbarManager, "org.eclipse.emf.compare.ide.ui.mergedToRight", //$NON-NLS-1$
 					"Copy Current Change From Left To Right", "icons/full/toolb16/merge_to_right.gif", true); //$NON-NLS-2$
 			addSMVAction(
 					toolbarManager,
 					"org.eclipse.emf.compare.ide.ui.mergedAllToRight", //$NON-NLS-1$
 					"Copy All Non-Conflicting Changes From Left To Right", "icons/full/toolb16/merge_all_to_right.gif", true); //$NON-NLS-2$
 			toolbarManager.add(new Separator());
 			addSMVAction(toolbarManager, "org.eclipse.emf.compare.ide.ui.mergedToLeft", //$NON-NLS-1$
 					"Copy Current Change From Right To Left", "icons/full/toolb16/merge_to_left.gif", true); //$NON-NLS-2$
 			addSMVAction(
 					toolbarManager,
 					"org.eclipse.emf.compare.ide.ui.mergedAllToLeft", //$NON-NLS-1$
 					"Copy All Non-Conflicting Changes From Right To Left", "icons/full/toolb16/merge_all_to_left.gif", true); //$NON-NLS-2$
 		} else {
 			addSMVAction(toolbarManager, "org.eclipse.emf.compare.ide.ui.acceptChange", //$NON-NLS-1$
 					"Accept Change", "icons/full/toolb16/accept_change.gif", true); //$NON-NLS-2$
 			addSMVAction(toolbarManager, "org.eclipse.emf.compare.ide.ui.acceptAllChanges", //$NON-NLS-1$
 					"Accept All Non-Conflicting Changes", "icons/full/toolb16/accept_all_changes.gif", true); //$NON-NLS-2$
 			toolbarManager.add(new Separator());
 			addSMVAction(toolbarManager, "org.eclipse.emf.compare.ide.ui.rejectChange", //$NON-NLS-1$
 					"Reject Change", "icons/full/toolb16/reject_change.gif", true); //$NON-NLS-2$
 			addSMVAction(toolbarManager, "org.eclipse.emf.compare.ide.ui.rejectAllChanges", //$NON-NLS-1$
 					"Reject All Non-Conflicting Changes", "icons/full/toolb16/reject_all_changes.gif", true); //$NON-NLS-2$
 		}
 		toolbarManager.add(new Separator());
 		addSMVAction(toolbarManager, "org.eclipse.emf.compare.ide.ui.nextDiff", //$NON-NLS-1$
 				"Next Difference", "icons/full/toolb16/next_diff.gif", true); //$NON-NLS-2$
 		addSMVAction(toolbarManager, "org.eclipse.emf.compare.ide.ui.previousDiff", //$NON-NLS-1$
 				"Previous Difference", "icons/full/toolb16/prev_diff.gif", true); //$NON-NLS-2$
 		toolbarManager.add(new Separator());
 		addSMVAction(toolbarManager, "org.eclipse.emf.compare.ide.ui.saveComparisonModel", //$NON-NLS-1$
 				"Save Comparison Model", "icons/full/toolb16/saveas_edit.gif", true); //$NON-NLS-2$
 	}
 
 	/**
 	 * Add the given action to the structure merge viewer toolbar.
 	 * 
 	 * @param tb
 	 *            the given toolbar.
 	 * @param actionId
 	 *            the id of the action to add.
 	 * @param tooltip
 	 *            the tooltip of the action to add.
 	 * @param imagePath
 	 *            the image path of the action to add.
 	 * @param activated
 	 *            the initial state of the action to add.
 	 */
 	private void addSMVAction(ToolBarManager tb, String actionId, String tooltip, String imagePath,
 			boolean activated) {
 		IAction action = new CommandAction(PlatformUI.getWorkbench(), actionId);
 		action.setToolTipText(tooltip);
 		action.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(EMFCompareIDEUIPlugin.PLUGIN_ID,
 				imagePath));
 		action.setEnabled(activated);
 		tb.add(action);
 	}
 
 	/**
 	 * Returns the viewer filter that is to be applied on the structure viewer.
 	 * <p>
 	 * Note that this will be called from {@link #createToolItems(ToolBarManager)}, which is called from the
 	 * super-constructor, when we have had no time to initialize the {@link #structureMergeViewerFilter}
 	 * field.
 	 * </p>
 	 * 
 	 * @return The difference filter that is to be applied on the structure viewer.
 	 */
 	protected StructureMergeViewerFilter getStructureMergeViewerFilter() {
 		if (structureMergeViewerFilter == null) {
 			if (eventBus == null) {
 				eventBus = new EventBus();
 				eventBus.register(this);
 			}
 			structureMergeViewerFilter = new StructureMergeViewerFilter(eventBus);
 			structureMergeViewerFilter.install(this);
 		}
 		return structureMergeViewerFilter;
 	}
 
 	/**
 	 * Returns the viewer grouper that is to be applied on the structure viewer.
 	 * <p>
 	 * Note that this will be called from {@link #createToolItems(ToolBarManager)}, which is called from the
 	 * super-constructor, when we have had no time to initialize the {@link #structureMergeViewerGrouper}
 	 * field.
 	 * </p>
 	 * 
 	 * @return The viewer grouper grouper that is to be applied on the structure viewer.
 	 */
 	protected StructureMergeViewerGrouper getStructureMergeViewerGrouper() {
 		if (structureMergeViewerGrouper == null) {
 			if (eventBus == null) {
 				eventBus = new EventBus();
 				eventBus.register(this);
 			}
 			structureMergeViewerGrouper = new StructureMergeViewerGrouper(eventBus);
 			structureMergeViewerGrouper.install(this);
 		}
 		return structureMergeViewerGrouper;
 	}
 
 	/**
 	 * Returns the menu manager that is to be applied to groups on the structure viewer.
 	 * 
 	 * @return The menu manager that is to be applied to groups on the structure viewer.
 	 */
 	public MenuManager getGroupsMenuManager() {
 		if (groupsMenuManager == null) {
 			groupsMenuManager = new MenuManager();
 		}
 		return groupsMenuManager;
 	}
 
 	/**
 	 * @return the groupActionMenu
 	 */
 	public GroupActionMenu getGroupActionMenu() {
 		return groupActionMenu;
 	}
 
 	/**
 	 * @param groupActionMenu
 	 *            the groupActionMenu to set
 	 */
 	public void setGroupActionMenu(GroupActionMenu groupActionMenu) {
 		this.groupActionMenu = groupActionMenu;
 	}
 
 	/**
 	 * Returns the menu manager that is to be applied to filters on the structure viewer.
 	 * 
 	 * @return The menu manager that is to be applied to filters on the structure viewer.
 	 */
 	public MenuManager getFiltersMenuManager() {
 		if (filtersMenuManager == null) {
 			filtersMenuManager = new MenuManager();
 		}
 		return filtersMenuManager;
 	}
 
 	/**
 	 * @return the filterActionMenu
 	 */
 	public FilterActionMenu getFilterActionMenu() {
 		return filterActionMenu;
 	}
 
 	/**
 	 * @param filterActionMenu
 	 *            the filterActionMenu to set
 	 */
 	public void setFilterActionMenu(FilterActionMenu filterActionMenu) {
 		this.filterActionMenu = filterActionMenu;
 	}
 
 	/**
 	 * Returns the default group provider that is to be applied on the structure viewer.
 	 * 
 	 * @return The default group provider that is to be applied on the structure viewer.
 	 */
 	public DefaultGroupProvider getDefaultGroupProvider() {
 		return new DefaultGroupProvider();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.structuremergeviewer.DiffTreeViewer#handleDispose(org.eclipse.swt.events.DisposeEvent)
 	 */
 	@Override
 	protected void handleDispose(DisposeEvent event) {
 		getControl().removeListener(SWT.EraseItem, fEraseItemListener);
 		removeSelectionChangedListener(fSelectionChangedListener);
 		super.handleDispose(event);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.viewers.AbstractTreeViewer#getSortedChildren(java.lang.Object)
 	 */
 	@Override
 	protected Object[] getSortedChildren(Object parentElementOrTreePath) {
 		Object[] result = super.getSortedChildren(parentElementOrTreePath);
 		if (parentElementOrTreePath instanceof Adapter) {
 			Notifier target = ((Adapter)parentElementOrTreePath).getTarget();
 			if (target instanceof TreeNode) {
 				EObject data = ((TreeNode)target).getData();
 				if (data instanceof Conflict) {
 					Collections.sort(Arrays.asList(result), new Comparator<Object>() {
 						public int compare(Object o1, Object o2) {
 							return getValue(o1) - getValue(o2);
 						}
 
 						public int getValue(Object o) {
 							int value = 0;
 							if (o instanceof Adapter) {
 								Notifier n = ((Adapter)o).getTarget();
 								if (n instanceof TreeNode) {
 									EObject d = ((TreeNode)n).getData();
 									if (d instanceof Diff) {
 										if (((Diff)d).getSource() == DifferenceSource.LEFT) {
 											value = 1;
 										} else {
 											value = 2;
 										}
 									}
 								}
 							}
 							return value;
 						}
 					});
 				}
 			}
 		}
 
 		return result;
 	}
 
 	/**
 	 * Handle the erase item event. When select a difference in the structure merge viewer, highlight required
 	 * differences with a specific color, and highlight unmergeable differences with another color.
 	 * 
 	 * @param event
 	 *            the erase item event.
 	 */
 	protected void handleEraseItemEvent(Event event) {
 		ISelection selection = getSelection();
 		Object firstElement = ((IStructuredSelection)selection).getFirstElement();
 		if (firstElement instanceof Adapter) {
 			Notifier target = ((Adapter)firstElement).getTarget();
 			if (target instanceof TreeNode) {
 				EObject selectionData = ((TreeNode)target).getData();
 				if (selectionData instanceof Diff) {
 					TreeItem item = (TreeItem)event.item;
 					Object dataTreeItem = item.getData();
 					if (dataTreeItem instanceof Adapter) {
 						Notifier targetItem = ((Adapter)dataTreeItem).getTarget();
 						if (targetItem instanceof TreeNode) {
 							EObject dataItem = ((TreeNode)targetItem).getData();
 							final Set<Diff> unmergeables;
 							final Set<Diff> requires;
 							Boolean leftToRight = (Boolean)getCompareConfiguration().getProperty(
 									EMFCompareConstants.MERGE_WAY);
 							boolean ltr = false;
 							if (leftToRight == null || leftToRight.booleanValue()) {
 								ltr = true;
 							}
 							boolean leftEditable = getCompareConfiguration().isLeftEditable();
 							boolean rightEditable = getCompareConfiguration().isRightEditable();
 							Diff diff = (Diff)selectionData;
							if (ltr || (!ltr && (rightEditable && !leftEditable))) {
 								requires = DiffUtil.getRequires(diff, true, diff.getSource());
 								unmergeables = DiffUtil.getUnmergeables(diff, true);
 							} else {
 								requires = DiffUtil.getRequires(diff, false, diff.getSource());
 								unmergeables = DiffUtil.getUnmergeables(diff, false);
 							}
 							final GC g = event.gc;
 							if (requires.contains(dataItem)) {
 								paintItemBackground(g, item, requiredDiffColor);
 							} else if (unmergeables.contains(dataItem)) {
 								paintItemBackground(g, item, unmergeableDiffColor);
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Paint the background of the given item with the given color.
 	 * 
 	 * @param g
 	 *            the GC associated to the item.
 	 * @param item
 	 *            the given item.
 	 * @param color
 	 *            the given color.
 	 */
 	private void paintItemBackground(GC g, TreeItem item, Color color) {
 		Rectangle itemBounds = item.getBounds();
 		Tree tree = item.getParent();
 		Rectangle areaBounds = tree.getClientArea();
 		g.setClipping(areaBounds.x, itemBounds.y, areaBounds.width, itemBounds.height);
 		g.setBackground(color);
 		g.fillRectangle(areaBounds.x, itemBounds.y, areaBounds.width, itemBounds.height);
 	}
 }
