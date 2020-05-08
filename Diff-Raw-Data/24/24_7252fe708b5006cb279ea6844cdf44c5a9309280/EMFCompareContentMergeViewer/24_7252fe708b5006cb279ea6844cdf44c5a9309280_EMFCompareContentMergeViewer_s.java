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
 package org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer;
 
 import static com.google.common.collect.Iterables.filter;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.ImmutableList;
 import com.google.common.eventbus.Subscribe;
 
 import java.util.EventObject;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.ResourceBundle;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.eclipse.compare.contentmergeviewer.ContentMergeViewer;
 import org.eclipse.compare.internal.CompareHandlerService;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.emf.common.command.Command;
 import org.eclipse.emf.common.command.CommandStackListener;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.compare.Diff;
 import org.eclipse.emf.compare.DifferenceSource;
 import org.eclipse.emf.compare.DifferenceState;
 import org.eclipse.emf.compare.domain.ICompareEditingDomain;
 import org.eclipse.emf.compare.ide.ui.internal.configuration.EMFCompareConfiguration;
 import org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.util.DynamicObject;
 import org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.util.EMFCompareColor;
 import org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.util.RedoAction;
 import org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.util.UndoAction;
 import org.eclipse.emf.compare.rcp.EMFCompareRCPPlugin;
 import org.eclipse.emf.compare.rcp.ui.internal.configuration.IAdapterFactoryChange;
 import org.eclipse.emf.compare.rcp.ui.internal.configuration.ICompareEditingDomainChange;
 import org.eclipse.emf.compare.rcp.ui.internal.contentmergeviewer.accessor.ICompareAccessor;
 import org.eclipse.emf.compare.rcp.ui.internal.mergeviewer.ICompareColor;
 import org.eclipse.emf.compare.rcp.ui.internal.mergeviewer.IMergeViewer;
 import org.eclipse.emf.compare.rcp.ui.internal.mergeviewer.IMergeViewer.MergeViewerSide;
 import org.eclipse.emf.compare.rcp.ui.internal.mergeviewer.item.IMergeViewerItem;
 import org.eclipse.emf.compare.rcp.ui.internal.mergeviewer.item.impl.MergeViewerItem;
 import org.eclipse.emf.compare.rcp.ui.internal.structuremergeviewer.filters.IDifferenceFilterChange;
 import org.eclipse.emf.compare.rcp.ui.internal.structuremergeviewer.groups.IDifferenceGroupProvider;
 import org.eclipse.emf.compare.rcp.ui.internal.structuremergeviewer.groups.IDifferenceGroupProviderChange;
 import org.eclipse.emf.compare.rcp.ui.internal.util.SWTUtil;
 import org.eclipse.emf.compare.utils.EMFComparePredicates;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
 import org.eclipse.emf.edit.ui.view.ExtendedPropertySheetPage;
 import org.eclipse.jface.action.ToolBarManager;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.menus.IMenuService;
 import org.eclipse.ui.part.IPage;
 import org.eclipse.ui.services.IServiceLocator;
 import org.eclipse.ui.views.properties.PropertySheet;
 
 /**
  * @author <a href="mailto:mikael.barbero@obeo.fr">Mikael Barbero</a>
  */
 public abstract class EMFCompareContentMergeViewer extends ContentMergeViewer implements ISelectionChangedListener, ICompareColor.Provider, IAdaptable, CommandStackListener {
 
 	private static final String HANDLER_SERVICE = "fHandlerService";
 
 	/**
 	 * Width of center bar
 	 */
 	protected static final int CENTER_WIDTH = 34;
 
 	private IMergeViewer fAncestor;
 
 	private IMergeViewer fLeft;
 
 	private IMergeViewer fRight;
 
 	private final AtomicBoolean fSyncingSelections = new AtomicBoolean(false);
 
 	private EMFCompareColor fColors;
 
 	private final DynamicObject fDynamicObject;
 
 	private UndoAction undoAction;
 
 	private RedoAction redoAction;
 
 	private AdapterFactoryContentProvider fAdapterFactoryContentProvider;
 
 	private Predicate<? super EObject> differenceFilterPredicate;
 
 	private IDifferenceGroupProvider differenceGroupProvider;
 
 	/**
 	 * @param style
 	 * @param bundle
 	 * @param cc
 	 */
 	protected EMFCompareContentMergeViewer(int style, ResourceBundle bundle, EMFCompareConfiguration cc) {
 		super(style, bundle, cc);
 
 		fDynamicObject = new DynamicObject(this);
 
 		if (getCompareConfiguration().getAdapterFactory() != null) {
 			fAdapterFactoryContentProvider = new AdapterFactoryContentProvider(getCompareConfiguration()
 					.getAdapterFactory());
 		}
 
 		editingDomainChange(null, getCompareConfiguration().getEditingDomain());
 		getCompareConfiguration().getEventBus().register(this);
 	}
 
 	@Subscribe
 	public void handleAdapterFactoryChange(IAdapterFactoryChange event) {
 		AdapterFactory oldValue = event.getOldValue();
 		AdapterFactory newValue = event.getNewValue();
 		if (oldValue != null) {
 			fAdapterFactoryContentProvider.dispose();
 		}
 		if (newValue != oldValue) {
 			fAdapterFactoryContentProvider = new AdapterFactoryContentProvider(newValue);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.ide.ui.internal.configuration.EMFCompareConfiguration#editingDomainChange(org.eclipse.emf.compare.domain.ICompareEditingDomain,
 	 *      org.eclipse.emf.compare.domain.ICompareEditingDomain)
 	 */
 	@Subscribe
 	public void handleEditingDomainChange(ICompareEditingDomainChange event) {
 		ICompareEditingDomain oldValue = event.getOldValue();
 		ICompareEditingDomain newValue = event.getNewValue();
 		editingDomainChange(oldValue, newValue);
 	}
 
 	protected void editingDomainChange(ICompareEditingDomain oldValue, ICompareEditingDomain newValue) {
 		if (newValue != oldValue) {
 			if (oldValue != null) {
 				oldValue.getCommandStack().removeCommandStackListener(this);
 			}
 
 			if (newValue != null) {
 				newValue.getCommandStack().addCommandStackListener(this);
 				setLeftDirty(newValue.getCommandStack().isLeftSaveNeeded());
 				setRightDirty(newValue.getCommandStack().isRightSaveNeeded());
 			}
 			if (undoAction != null) {
 				undoAction.setEditingDomain(newValue);
 			}
 			if (redoAction != null) {
 				redoAction.setEditingDomain(newValue);
 			}
 		}
 	}
 
 	@Subscribe
 	public void handleDifferenceFiltersChange(IDifferenceFilterChange event) {
 		differenceFilterPredicate = event.getPredicate();
 		redrawCenterControl();
 	}
 
 	/**
 	 * @return the differenceFilterPredicate
 	 */
 	protected final Predicate<? super EObject> getDifferenceFilterPredicate() {
 		if (differenceFilterPredicate == null) {
 			differenceFilterPredicate = getCompareConfiguration().getStructureMergeViewerFilter()
 					.getAggregatedPredicate();
 		}
 		return differenceFilterPredicate;
 	}
 
 	@Subscribe
 	public void handleDifferenceGroupProviderChange(IDifferenceGroupProviderChange event) {
 		differenceGroupProvider = event.getDifferenceGroupProvider();
 		redrawCenterControl();
 	}
 
 	/**
 	 * @return the differenceGroupProvider
 	 */
 	protected final IDifferenceGroupProvider getDifferenceGroupProvider() {
 		if (differenceGroupProvider == null) {
 			differenceGroupProvider = getCompareConfiguration().getStructureMergeViewerGrouper()
 					.getProvider();
 		}
 		return differenceGroupProvider;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.rcp.ui.internal.mergeviewer.ICompareColorProvider#getCompareColor()
 	 */
 	public ICompareColor getCompareColor() {
 		return fColors;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.contentmergeviewer.ContentMergeViewer#updateContent(java.lang.Object,
 	 *      java.lang.Object, java.lang.Object)
 	 */
 	@Override
 	protected void updateContent(Object ancestor, Object left, Object right) {
 		fAncestor.setInput(ancestor);
 		fLeft.setInput(left);
 		fRight.setInput(right);
 
 		updateSelection(left);
 	}
 
 	protected void updateSelection(Object left) {
 		// must update selection after the three viewers input has been set
 		// to avoid some NPE/AssertionError (they are calling each other on selectionChanged event to
 		// synchronize their selection)
 
 		IMergeViewerItem leftInitialItem = null;
 		if (left instanceof ICompareAccessor) {
 			leftInitialItem = ((ICompareAccessor)left).getInitialItem();
 		}
 
 		ISelection leftSelection = createSelectionOrEmpty(leftInitialItem);
 		fLeft.setSelection(leftSelection, true); // others will synchronize on this one :)
 
 		redrawCenterControl();
 	}
 
 	private ISelection createSelectionOrEmpty(final Object o) {
 		final ISelection selection;
 		if (o != null) {
 			selection = new StructuredSelection(o);
 		} else {
 			selection = StructuredSelection.EMPTY;
 		}
 		return selection;
 	}
 
 	/**
 	 * Inhibits this method to avoid asking to save on each input change!!
 	 * 
 	 * @see org.eclipse.compare.contentmergeviewer.ContentMergeViewer#doSave(java.lang.Object,
 	 *      java.lang.Object)
 	 */
 	@Override
 	protected boolean doSave(Object newInput, Object oldInput) {
 		return false;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.contentmergeviewer.ContentMergeViewer#createControls(org.eclipse.swt.widgets.Composite)
 	 */
 	@Override
 	protected void createControls(Composite composite) {
 		fAncestor = createMergeViewer(composite, MergeViewerSide.ANCESTOR);
 		fAncestor.addSelectionChangedListener(this);
 
 		fLeft = createMergeViewer(composite, MergeViewerSide.LEFT);
 		fLeft.addSelectionChangedListener(this);
 
 		fRight = createMergeViewer(composite, MergeViewerSide.RIGHT);
 		fRight.addSelectionChangedListener(this);
 
 		fColors = new EMFCompareColor(this, null, getCompareConfiguration());
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.contentmergeviewer.ContentMergeViewer#createToolItems(org.eclipse.jface.action.ToolBarManager)
 	 */
 	@Override
 	protected void createToolItems(ToolBarManager toolBarManager) {
 
 		// Add extension point contributions to the content merge viewer toolbar
 		IServiceLocator workbench = PlatformUI.getWorkbench();
 		IMenuService menuService = (IMenuService)workbench.getService(IMenuService.class);
 		if (menuService != null) {
 			menuService.populateContributionManager(toolBarManager,
 					"toolbar:org.eclipse.emf.compare.contentmergeviewer.toolbar");
 		}
 
 	}
 
 	public void commandStackChanged(EventObject event) {
 		if (undoAction != null) {
 			undoAction.update();
 		}
 		if (redoAction != null) {
 			redoAction.update();
 		}
 		if (getCompareConfiguration().getEditingDomain() != null) {
 			setLeftDirty(getCompareConfiguration().getEditingDomain().getCommandStack().isLeftSaveNeeded());
 			setRightDirty(getCompareConfiguration().getEditingDomain().getCommandStack().isRightSaveNeeded());
 		}
 
 		SWTUtil.safeRefresh(this, true);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.contentmergeviewer.ContentMergeViewer#copy(boolean)
 	 */
 	@Override
 	protected void copy(final boolean leftToRight) {
 		final List<Diff> differences;
 
 		if (getCompareConfiguration().getComparison().isThreeWay()) {
 			differences = ImmutableList.copyOf(filter(getCompareConfiguration().getComparison()
 					.getDifferences(), new Predicate<Diff>() {
 				public boolean apply(Diff diff) {
 					final boolean unresolved = diff.getState() == DifferenceState.UNRESOLVED;
 					final boolean nonConflictual = diff.getConflict() == null;
 					final boolean fromLeftToRight = leftToRight && diff.getSource() == DifferenceSource.LEFT;
 					final boolean fromRightToLeft = !leftToRight
 							&& diff.getSource() == DifferenceSource.RIGHT;
 					return unresolved && nonConflictual && (fromLeftToRight || fromRightToLeft);
 				}
 			}));
 		} else {
 			differences = ImmutableList.copyOf(filter(getCompareConfiguration().getComparison()
 					.getDifferences(), EMFComparePredicates.hasState(DifferenceState.UNRESOLVED)));
 		}
 
 		if (differences.size() > 0) {
 			final Command copyCommand = getCompareConfiguration().getEditingDomain().createCopyCommand(
 					differences, leftToRight, EMFCompareRCPPlugin.getDefault().getMergerRegistry());
 
 			getCompareConfiguration().getEditingDomain().getCommandStack().execute(copyCommand);
 			refresh();
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.contentmergeviewer.ContentMergeViewer#handleResizeAncestor(int, int, int, int)
 	 */
 	@Override
 	protected void handleResizeAncestor(int x, int y, int width, int height) {
 		if (width > 0) {
 			getAncestorMergeViewer().getControl().setVisible(true);
 			getAncestorMergeViewer().getControl().setBounds(x, y, width, height);
 		} else {
 			getAncestorMergeViewer().getControl().setVisible(false);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.contentmergeviewer.ContentMergeViewer#handleResizeLeftRight(int, int, int,
 	 *      int, int, int)
 	 */
 	@Override
 	protected void handleResizeLeftRight(int x, int y, int width1, int centerWidth, int width2, int height) {
 		fLeft.getControl().setBounds(x, y, width1, height);
 		fRight.getControl().setBounds(x + width1 + centerWidth, y, width2, height);
 	}
 
 	protected abstract IMergeViewer createMergeViewer(Composite parent, MergeViewerSide side);
 
 	@Override
 	protected final int getCenterWidth() {
 		return CENTER_WIDTH;
 	}
 
 	protected final CompareHandlerService getHandlerService() {
 		return (CompareHandlerService)fDynamicObject.get(HANDLER_SERVICE);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.contentmergeviewer.ContentMergeViewer#getControl()
 	 */
 	@Override
 	public Composite getControl() {
 		return (Composite)super.getControl();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.contentmergeviewer.ContentMergeViewer#createCenterControl(org.eclipse.swt.widgets.Composite)
 	 */
 	@Override
	protected Control createCenterControl(Composite parent) {
		final Control ret = super.createCenterControl(parent);
 
 		final PaintListener paintListener = new PaintListener() {
 			public void paintControl(PaintEvent e) {
 				paintCenter(e.gc);
 			}
 		};
 		ret.addPaintListener(paintListener);
 
 		ret.addDisposeListener(new DisposeListener() {
 			public void widgetDisposed(DisposeEvent e) {
 				ret.removePaintListener(paintListener);
 			}
 		});
 
 		return ret;
 	}
 
 	protected abstract void paintCenter(GC g);
 
 	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
 		if (adapter == CompareHandlerService.class) {
 			return getHandlerService();
 		}
 		if (adapter == CompareHandlerService[].class) {
 			return new CompareHandlerService[] {getHandlerService(), };
 		}
 		return null;
 	}
 
 	/**
 	 * @return the fAncestor
 	 */
 	protected IMergeViewer getAncestorMergeViewer() {
 		return fAncestor;
 	}
 
 	/**
 	 * @return the fLeft
 	 */
 	protected IMergeViewer getLeftMergeViewer() {
 		return fLeft;
 	}
 
 	/**
 	 * @return the fRight
 	 */
 	protected IMergeViewer getRightMergeViewer() {
 		return fRight;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
 	 */
 	public void selectionChanged(SelectionChangedEvent event) {
 		synchronizeSelection(event);
 		updateToolItems();
 	}
 
 	private void synchronizeSelection(SelectionChangedEvent event) {
 		if (fSyncingSelections.compareAndSet(false, true)) { // prevents stack overflow :)
 			try {
 				ISelection selection = event.getSelection();
 				updatePropertiesView(selection);
 				fLeft.setSelection(selection, true);
 				fRight.setSelection(selection, true);
 				fAncestor.setSelection(selection, true);
 			} finally {
 				fSyncingSelections.set(false);
 			}
 		}
 
 	}
 
 	/**
 	 * Update the properties view with the given selection.
 	 * 
 	 * @param selection
 	 *            the given selection.
 	 */
 	private void updatePropertiesView(ISelection selection) {
 		if (selection instanceof StructuredSelection) {
 			StructuredSelection structuredSelection = (StructuredSelection)selection;
 			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
 			ExtendedPropertySheetPage propertySheetPage = getExtendedPropertySheetPage(page);
 			if (propertySheetPage != null) {
 				StructuredSelection selectionForPropertySheet = null;
 				IWorkbenchPart activePart = page.getActivePart();
 				Object firstElement = structuredSelection.getFirstElement();
 				if (firstElement instanceof MergeViewerItem) {
 					MergeViewerItem mergeViewerItem = (MergeViewerItem)firstElement;
 					MergeViewerSide side = mergeViewerItem.getSide();
 					Object newSelectedObject = mergeViewerItem.getSideValue(side);
 					propertySheetPage.setPropertySourceProvider(fAdapterFactoryContentProvider);
 					if (newSelectedObject != null) {
 						if (newSelectedObject instanceof EObject) {
 							manageReadOnly((EObject)newSelectedObject, side);
 						}
 						selectionForPropertySheet = new StructuredSelection(newSelectedObject);
 						propertySheetPage.selectionChanged(activePart, selectionForPropertySheet);
 					}
 				}
 				if (selectionForPropertySheet == null) {
 					selectionForPropertySheet = new StructuredSelection(new Object());
 					propertySheetPage.selectionChanged(activePart, selectionForPropertySheet);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Returns the extended property sheet page.
 	 * 
 	 * @return the extended property sheet page.
 	 */
 	private ExtendedPropertySheetPage getExtendedPropertySheetPage(IWorkbenchPage activePage) {
 		ExtendedPropertySheetPage propertyPage = null;
 		if (activePage != null) {
 			IViewPart view = activePage.findView("org.eclipse.ui.views.PropertySheet"); //$NON-NLS-1$
 			if (view != null) {
 				if (view instanceof PropertySheet) {
 					PropertySheet propertySheet = (PropertySheet)view;
 					IPage currentPage = propertySheet.getCurrentPage();
 					if (currentPage instanceof ExtendedPropertySheetPage) {
 						propertyPage = (ExtendedPropertySheetPage)currentPage;
 					} else {
 						IEditorPart activeEditor = activePage.getActiveEditor();
 						if (activeEditor != null
 								&& Platform.getAdapterManager().hasAdapter(activeEditor,
 										"org.eclipse.ui.views.properties.IPropertySheetPage")) { //$NON-NLS-1$
 							propertySheet.partActivated(activePage.getActivePart());
 						}
 					}
 
 				}
 			}
 		}
 		return propertyPage;
 	}
 
 	/**
 	 * Manages the read-only state of the properties sheet page for the given selected object.
 	 * 
 	 * @param selectedObject
 	 *            the given selected object.
 	 * @param side
 	 *            the side of the selected object.
 	 */
 	private void manageReadOnly(EObject selectedObject, MergeViewerSide side) {
 		if (MergeViewerSide.LEFT == side) {
 			if (!getCompareConfiguration().isLeftEditable()) {
 				setToReadOnly(selectedObject);
 			}
 		} else if (MergeViewerSide.RIGHT == side) {
 			if (!getCompareConfiguration().isRightEditable()) {
 				setToReadOnly(selectedObject);
 			}
 		} else if (MergeViewerSide.ANCESTOR == side) {
 			setToReadOnly(selectedObject);
 		}
 	}
 
 	/**
 	 * Sets the resource of the selected object to read-only in the appropriate editing domain.
 	 * 
 	 * @param selectedObject
 	 *            the given selected object.
 	 */
 	private void setToReadOnly(EObject selectedObject) {
 		EditingDomain editingDomain = AdapterFactoryEditingDomain.getEditingDomainFor(selectedObject);
 		if (editingDomain instanceof AdapterFactoryEditingDomain) {
 			Resource r = selectedObject.eResource();
 			Map<Resource, Boolean> resourceToReadOnlyMap = ((AdapterFactoryEditingDomain)editingDomain)
 					.getResourceToReadOnlyMap();
 			if (!resourceToReadOnlyMap.containsKey(r)) {
 				resourceToReadOnlyMap.put(r, Boolean.TRUE);
 			}
 		}
 	}
 
 	/**
 	 * Checks the element selected in the given viewer in order to determine whether it can be adapted into a
 	 * Diff.
 	 * 
 	 * @param viewer
 	 *            The viewer which selection is to be checked.
 	 * @return The first of the Diffs selected in the given viewer, if any.
 	 */
 	protected Diff getDiffFrom(IMergeViewer viewer) {
 		Diff diff = null;
 		final ISelection selection = viewer.getSelection();
 		if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
 			final Iterator<?> selectedElements = ((IStructuredSelection)selection).iterator();
 			while (diff == null && selectedElements.hasNext()) {
 				final Object element = selectedElements.next();
 				if (element instanceof IMergeViewerItem) {
 					diff = ((IMergeViewerItem)element).getDiff();
 				}
 			}
 		}
 		return diff;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.contentmergeviewer.ContentMergeViewer#handleDispose(org.eclipse.swt.events.DisposeEvent)
 	 */
 	@Override
 	protected void handleDispose(DisposeEvent event) {
 		editingDomainChange(getCompareConfiguration().getEditingDomain(), null);
 		getCompareConfiguration().getEventBus().unregister(this);
 		super.handleDispose(event);
 	}
 
 	protected final void redrawCenterControl() {
 		if (getCenterControl() != null) {
 			SWTUtil.safeRedraw(getCenterControl(), false);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.contentmergeviewer.ContentMergeViewer#getCompareConfiguration()
 	 */
 	@Override
 	protected EMFCompareConfiguration getCompareConfiguration() {
 		return (EMFCompareConfiguration)super.getCompareConfiguration();
 	}
 }
