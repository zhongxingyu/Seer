 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  ******************************************************************************/
 package org.eclipse.emf.emfstore.client.ui.views.historybrowserview;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
 import org.eclipse.emf.emfstore.client.model.ProjectSpace;
 import org.eclipse.emf.emfstore.client.model.WorkspaceManager;
 import org.eclipse.emf.emfstore.client.model.connectionmanager.ServerCall;
 import org.eclipse.emf.emfstore.client.model.observers.OpenModelElementObserver;
 import org.eclipse.emf.emfstore.client.model.util.ProjectSpaceContainer;
 import org.eclipse.emf.emfstore.client.ui.Activator;
 import org.eclipse.emf.emfstore.client.ui.util.EMFStoreMessageDialog;
 import org.eclipse.emf.emfstore.client.ui.views.changes.ChangePackageVisualizationHelper;
 import org.eclipse.emf.emfstore.client.ui.views.scm.SCMContentProvider;
 import org.eclipse.emf.emfstore.client.ui.views.scm.SCMLabelProvider;
 import org.eclipse.emf.emfstore.common.model.ModelElementId;
 import org.eclipse.emf.emfstore.common.model.Project;
 import org.eclipse.emf.emfstore.common.model.util.ModelUtil;
 import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
 import org.eclipse.emf.emfstore.server.model.versioning.ChangePackage;
 import org.eclipse.emf.emfstore.server.model.versioning.HistoryInfo;
 import org.eclipse.emf.emfstore.server.model.versioning.HistoryQuery;
 import org.eclipse.emf.emfstore.server.model.versioning.PrimaryVersionSpec;
 import org.eclipse.emf.emfstore.server.model.versioning.TagVersionSpec;
 import org.eclipse.emf.emfstore.server.model.versioning.VersionSpec;
 import org.eclipse.emf.emfstore.server.model.versioning.VersioningFactory;
 import org.eclipse.emf.emfstore.server.model.versioning.operations.AbstractOperation;
 import org.eclipse.emf.emfstore.server.model.versioning.operations.CompositeOperation;
 import org.eclipse.emf.emfstore.server.model.versioning.operations.OperationId;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.dialogs.InputDialog;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.layout.GridLayoutFactory;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.TreeViewerColumn;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.Widget;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.part.ViewPart;
 
 /**
  * This the History Browser view.
  * 
  * @author Hodaie
  * @author Wesendonk
  * @author Shterev
  */
 public class HistoryBrowserView extends ViewPart implements ProjectSpaceContainer {
 
 	/**
 	 * Treeviewer that provides a model element selection for selected
 	 * operations and mode element ids.
 	 * 
 	 * @author koegel
 	 */
 	private final class TreeViewerWithModelElementSelectionProvider extends TreeViewer {
 		private TreeViewerWithModelElementSelectionProvider(Composite parent, int style) {
 			super(parent, style);
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.jface.viewers.AbstractTreeViewer#getSelection()
 		 */
 		@Override
 		public ISelection getSelection() {
 			Control control = getControl();
 
 			if (control == null || control.isDisposed()) {
 				return super.getSelection();
 			}
 
 			Widget[] items = getSelection(getControl());
 			if (items.length != 1) {
 				return super.getSelection();
 			}
 
 			Widget item = items[0];
 			Object data = item.getData();
 			if (data == null) {
 				return super.getSelection();
 			}
 
 			// TODO: remove assignment
 			Object element = data;
 			EObject selectedModelElement = null;
 
 			if (element instanceof CompositeOperation) {
 				selectedModelElement = handleCompositeOperation((CompositeOperation) element);
 			} else if (element instanceof AbstractOperation) {
 				selectedModelElement = handleAbstractOperation((AbstractOperation) element);
 			} else if (element instanceof ProjectSpace) {
 				selectedModelElement = ((ProjectSpace) element).getProject();
 			} else if (element instanceof ModelElementId
 				&& projectSpace.getProject().contains((ModelElementId) element)) {
 				selectedModelElement = projectSpace.getProject().getModelElement((ModelElementId) element);
 			} else if (projectSpace.getProject().containsInstance((EObject) element)) {
 				selectedModelElement = (EObject) element;
 			}
 
 			if (selectedModelElement != null) {
 				return new StructuredSelection(selectedModelElement);
 			}
 
 			return super.getSelection();
 		}
 
 		private EObject handleCompositeOperation(CompositeOperation op) {
 			AbstractOperation mainOperation = op.getMainOperation();
 			if (mainOperation != null) {
 				ModelElementId modelElementId = mainOperation.getModelElementId();
 				EObject modelElement = projectSpace.getProject().getModelElement(modelElementId);
 				return modelElement;
 			}
 
 			return null;
 		}
 
 		private EObject handleAbstractOperation(AbstractOperation op) {
 			ModelElementId modelElementId = op.getModelElementId();
 			EObject modelElement = projectSpace.getProject().getModelElement(modelElementId);
 			return modelElement;
 		}
 	}
 
 	private List<HistoryInfo> historyInfos;
 
 	private ProjectSpace projectSpace;
 
 	private int startOffset = 24;
 
 	/**
 	 * this should be the UNRESOLVED VersionSpec ID (-1 for HeadVersionSpec).
 	 */
 	private int currentEnd;
 
 	private int headVersion;
 
 	private EObject modelElement;
 
 	private TreeViewer viewer;
 	private Map<Integer, ChangePackage> changePackageCache;
 
 	private ChangePackageVisualizationHelper changePackageVisualizationHelper;
 
 	private SCMContentProvider contentProvider;
 
 	private SCMLabelProvider labelProvider;
 
 	private Action showRoots;
 
 	private Label noProjectHint;
 
 	private Composite parent;
 
 	private boolean isUnlinkedFromNavigator;
 
 	private TreeViewerColumn changesColumn;
 
 	private TreeViewerColumn logColumn;
 
 	private LogMessageColumnLabelProvider logLabelProvider;
 
 	private ComposedAdapterFactory adapterFactory;
 
 	private AdapterFactoryLabelProvider adapterFactoryLabelProvider;
 
 	/**
 	 * Constructor.
 	 */
 	public HistoryBrowserView() {
 		historyInfos = new ArrayList<HistoryInfo>();
 		changePackageCache = new HashMap<Integer, ChangePackage>();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void createPartControl(Composite parent) {
 		GridLayoutFactory.fillDefaults().applyTo(parent);
 		this.parent = parent;
 
 		noProjectHint = new Label(parent, SWT.WRAP);
 		GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, true).applyTo(noProjectHint);
 		noProjectHint.setText("Please call 'Show history' from the context menu of an element in the navigator.");
 
 		viewer = new TreeViewerWithModelElementSelectionProvider(parent, SWT.NONE);
 
 		MenuManager menuMgr = new MenuManager();
 		menuMgr.add(new Separator("additions"));
 		getSite().registerContextMenu(menuMgr, viewer);
 		Control control = viewer.getControl();
 		Menu menu = menuMgr.createContextMenu(control);
 		control.setMenu(menu);
 		getSite().registerContextMenu(menuMgr, viewer);
 
 		getSite().setSelectionProvider(viewer);
 
 		GridDataFactory.fillDefaults().grab(true, true).applyTo(viewer.getControl());
 		ColumnViewerToolTipSupport.enableFor(viewer);
 		viewer.addDoubleClickListener(new IDoubleClickListener() {
 
 			public void doubleClick(DoubleClickEvent event) {
 				if (event.getSelection() instanceof IStructuredSelection) {
 					Object element = ((IStructuredSelection) event.getSelection()).getFirstElement();
 					if (element instanceof EObject) {
 						WorkspaceManager.getObserverBus().notify(OpenModelElementObserver.class)
 							.openModelElement((EObject) element);
 						// ElementOpenerHelper.openModelElement((EObject) node.getValue(), VIEW_ID);
 					}
 				}
 
 			}
 		});
 
 		changesColumn = new TreeViewerColumn(viewer, SWT.NONE);
 		changesColumn.getColumn().setText("Changes");
 		changesColumn.getColumn().setWidth(400);
 
 		logColumn = new TreeViewerColumn(viewer, SWT.NONE);
 		logColumn.getColumn().setText("Commit information");
 		logColumn.getColumn().setWidth(300);
 
 		Tree tree = viewer.getTree();
 		tree.setHeaderVisible(true);
 
 		hookToobar();
 	}
 
 	private void hookToobar() {
 		IActionBars bars = getViewSite().getActionBars();
 		IToolBarManager menuManager = bars.getToolBarManager();
 
 		addExpandAllAndCollapseAllAction(menuManager);
 		addRefreshAction(menuManager);
 		addShowRootAction(menuManager);
 		addNextAndPreviousAction(menuManager);
 		addJumpToRevisionAction(menuManager);
 		addLinkWithNavigatorAction(menuManager);
 	}
 
 	private void addExpandAllAndCollapseAllAction(IToolBarManager menuManager) {
 		final ImageDescriptor expandImg = Activator.getImageDescriptor("icons/expandall.gif");
 		final ImageDescriptor collapseImg = Activator.getImageDescriptor("icons/collapseall.gif");
 
 		Action expandAndCollapse = new Action("", SWT.TOGGLE) {
 			@Override
 			public void run() {
 				if (!isChecked()) {
 					setImageDescriptor(expandImg);
 					viewer.collapseAll();
 				} else {
 					setImageDescriptor(collapseImg);
 					viewer.expandToLevel(2);
 				}
 			}
 
 		};
 		expandAndCollapse.setImageDescriptor(expandImg);
 		expandAndCollapse.setToolTipText("Use this toggle to expand or collapse all elements");
 		menuManager.add(expandAndCollapse);
 	}
 
 	private void addRefreshAction(IToolBarManager menuManager) {
 		Action refresh = new Action() {
 			@Override
 			public void run() {
 				refresh();
 			}
 
 		};
 		refresh.setImageDescriptor(Activator.getImageDescriptor("/icons/refresh.png"));
 		refresh.setToolTipText("Refresh");
 		menuManager.add(refresh);
 	}
 
 	private void addShowRootAction(IToolBarManager menuManager) {
 		showRoots = new Action("", SWT.TOGGLE) {
 			@Override
 			public void run() {
 				if (isChecked()) {
 					contentProvider.setShowRootNodes(true);
 				} else {
 					contentProvider.setShowRootNodes(false);
 				}
 				viewer.refresh();
 			}
 
 		};
 		adapterFactory = new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE);
 		adapterFactoryLabelProvider = new AdapterFactoryLabelProvider(adapterFactory);
 		showRoots.setImageDescriptor(ImageDescriptor.createFromImage(adapterFactoryLabelProvider
 			.getImage(VersioningFactory.eINSTANCE.createChangePackage())));
 		showRoots.setToolTipText("Show revision nodes");
 		showRoots.setChecked(true);
 		menuManager.add(showRoots);
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
 	 */
 	@Override
 	public void dispose() {
 		if (adapterFactory != null) {
 			adapterFactory.dispose();
 		}
 		if (changePackageVisualizationHelper != null) {
 			changePackageVisualizationHelper.dispose();
 		}
 		super.dispose();
 	}
 
 	private void addNextAndPreviousAction(IToolBarManager menuManager) {
 		Action prev = new Action() {
 			@Override
 			public void run() {
 				int temp = currentEnd + startOffset;
 				if (temp <= headVersion) {
 					currentEnd = temp;
 				}
 				refresh();
 			}
 
 		};
 		prev.setImageDescriptor(Activator.getImageDescriptor("/icons/prev.png"));
 		prev.setToolTipText("Previous " + (startOffset + 1) + " items");
 		menuManager.add(prev);
 
 		Action next = new Action() {
 			@Override
 			public void run() {
 				int temp = currentEnd - startOffset;
 				if (temp > 0) {
 					currentEnd = temp;
 				}
 				refresh();
 			}
 
 		};
 		next.setImageDescriptor(Activator.getImageDescriptor("/icons/next.png"));
 		next.setToolTipText("Next " + (startOffset + 1) + " items");
 		menuManager.add(next);
 	}
 
 	private void addJumpToRevisionAction(IToolBarManager menuManager) {
 		Action jumpTo = new Action() {
 			@Override
 			public void run() {
 				InputDialog inputDialog = new InputDialog(getSite().getShell(), "Go to revision", "Revision", "", null);
 				if (inputDialog.open() == Window.OK) {
 					try {
 						int temp = Integer.parseInt(inputDialog.getValue());
 						currentEnd = temp;
 						refresh();
 					} catch (NumberFormatException e) {
 						MessageDialog.openError(getSite().getShell(), "Error", "A numeric value was expected!");
 						run();
 					}
 				}
 			}
 
 		};
 		jumpTo.setImageDescriptor(Activator.getImageDescriptor("/icons/magnifier.png"));
 		jumpTo.setToolTipText("Go to revision...");
 		menuManager.add(jumpTo);
 	}
 
 	private void addLinkWithNavigatorAction(IToolBarManager menuManager) {
 		isUnlinkedFromNavigator = Activator.getDefault().getDialogSettings().getBoolean("LinkWithNavigator");
 		Action linkWithNavigator = new Action("Link with navigator", SWT.TOGGLE) {
 
 			@Override
 			public void run() {
 				Activator.getDefault().getDialogSettings().put("LinkWithNavigator", !this.isChecked());
 				isUnlinkedFromNavigator = (!this.isChecked());
 			}
 
 		};
 		linkWithNavigator.setImageDescriptor(Activator.getImageDescriptor("icons/link_with_editor.gif"));
 		linkWithNavigator.setToolTipText("Link with Navigator");
 		linkWithNavigator.setChecked(!isUnlinkedFromNavigator);
 		menuManager.add(linkWithNavigator);
 	}
 
 	/**
 	 * Refreshes the view using the current end point.
 	 */
 	public void refresh() {
 		load(currentEnd);
 		viewer.setContentProvider(contentProvider);
 		viewer.setInput(getHistoryInfos());
 	}
 
 	private void load(final int end) {
 		try {
 			new ServerCall<Void>(projectSpace.getUsersession()) {
 				@Override
 				protected Void run() throws EmfStoreException {
 					loadContent(end);
 					return null;
 				}
 			}.execute();
 		} catch (EmfStoreException e) {
 			EMFStoreMessageDialog.showExceptionDialog(e);
 		}
 	}
 
 	private void loadContent(int end) throws EmfStoreException {
 		if (projectSpace == null) {
 			historyInfos.clear();
 			return;
 		}
 		HistoryQuery query = getQuery(end);
 		List<HistoryInfo> historyInfo = projectSpace.getHistoryInfo(query);
 
 		if (historyInfo != null) {
 			for (HistoryInfo hi : historyInfo) {
 				if (hi.getPrimerySpec().equals(projectSpace.getBaseVersion())) {
 					TagVersionSpec spec = VersioningFactory.eINSTANCE.createTagVersionSpec();
 					spec.setName(VersionSpec.BASE);
 					hi.getTagSpecs().add(spec);
 					break;
 				}
 			}
 			historyInfos.clear();
 			historyInfos.addAll(historyInfo);
 		}
 		ChangePackage changePackage = VersioningFactory.eINSTANCE.createChangePackage();
 		changePackage.getOperations().addAll(ModelUtil.clone(projectSpace.getOperations()));
 		changePackageCache.put(-1, changePackage);
 		for (HistoryInfo hi : historyInfos) {
 			if (hi.getChangePackage() != null) {
 				changePackageCache.put(hi.getPrimerySpec().getIdentifier(), hi.getChangePackage());
 			}
 		}
 		changePackageVisualizationHelper = new ChangePackageVisualizationHelper(new ArrayList<ChangePackage>(
 			changePackageCache.values()), projectSpace.getProject());
 		labelProvider.setChangePackageVisualizationHelper(changePackageVisualizationHelper);
 		logLabelProvider.setChangePackageVisualizationHelper(changePackageVisualizationHelper);
 		// contentProvider.setChangePackageVisualizationHelper(changePackageVisualizationHelper);
 	}
 
 	/**
 	 * Set the input for the History Browser.
 	 * 
 	 * @param projectSpace
 	 *            the input project space
 	 */
 	public void setInput(ProjectSpace projectSpace) {
 		setInput(projectSpace, null);
 	}
 
 	/**
 	 * Set the input for the History Browser.
 	 * 
 	 * @param projectSpace
 	 *            the input project space
 	 * @param me
 	 *            the input model element
 	 */
 	public void setInput(ProjectSpace projectSpace, EObject me) {
 		noProjectHint.dispose();
 		this.parent.layout();
 		this.projectSpace = projectSpace;
 		modelElement = me;
 		currentEnd = -1;
 		String label = "History for ";
 		Project project = projectSpace.getProject();
 		contentProvider = new SCMContentProvider();
 
 		if (me != null && project.containsInstance(me)) {
 			label += adapterFactoryLabelProvider.getText(me);
 			showRoots.setChecked(false);
 			contentProvider.setShowRootNodes(false);
 		} else {
 			label += projectSpace.getProjectName();
 			showRoots.setChecked(true);
 			contentProvider.setShowRootNodes(true);
 		}
 
 		setContentDescription(label);
 		labelProvider = new SCMLabelProvider(project);
 		changesColumn.setLabelProvider(labelProvider);
 
 		logLabelProvider = new LogMessageColumnLabelProvider(project);
 		logColumn.setLabelProvider(logLabelProvider);
 
 		refresh();
 	}
 
	private int getHeadVersionIdentifier() throws EmfStoreException {
 		PrimaryVersionSpec resolveVersionSpec = projectSpace.resolveVersionSpec(VersionSpec.HEAD_VERSION);
		return resolveVersionSpec.getIdentifier();
 	}
 
 	private HistoryQuery getQuery(int end) throws EmfStoreException {
 		HistoryQuery query = VersioningFactory.eINSTANCE.createHistoryQuery();
 
		headVersion = getHeadVersionIdentifier();

 		if (end == -1) {
 			end = headVersion;
			currentEnd = headVersion;// -1;
 		} else {
 			currentEnd = end;
 			PrimaryVersionSpec tempVersionSpec = VersioningFactory.eINSTANCE.createPrimaryVersionSpec();
 			tempVersionSpec.setIdentifier(end);
 			end = projectSpace.resolveVersionSpec(tempVersionSpec).getIdentifier();
 		}
 
 		int temp = end - startOffset;
 		int start = (temp > 0 ? temp : 0);
 
 		PrimaryVersionSpec source = VersioningFactory.eINSTANCE.createPrimaryVersionSpec();
 		source.setIdentifier(start);
 		PrimaryVersionSpec target = VersioningFactory.eINSTANCE.createPrimaryVersionSpec();
 		target.setIdentifier(end);
 		query.setSource(source);
 		query.setTarget(target);
 		query.setIncludeChangePackage(true);
 		if (modelElement != null && !(modelElement instanceof ProjectSpace)) {
 			query.getModelElements().add(ModelUtil.getProject(modelElement).getModelElementId(modelElement));
 		}
 
 		return query;
 	}
 
 	/**
 	 * Returns a list of history infos.
 	 * 
 	 * @return a list of history infos
 	 */
 	public List<HistoryInfo> getHistoryInfos() {
 
 		ArrayList<HistoryInfo> revisions = new ArrayList<HistoryInfo>();
 		if (projectSpace != null) {
 			// TODO: add a feature "hide local revision"
 			HistoryInfo localHistoryInfo = VersioningFactory.eINSTANCE.createHistoryInfo();
 			ChangePackage changePackage = projectSpace.getLocalChangePackage(false);
 			// filter for modelelement, do additional sanity check as the
 			// project space could've been also selected
 			if (modelElement != null && projectSpace.getProject().containsInstance(modelElement)) {
 				Set<AbstractOperation> operationsToRemove = new HashSet<AbstractOperation>();
 				for (AbstractOperation ao : changePackage.getOperations()) {
 					if (!ao.getAllInvolvedModelElements().contains(
 						ModelUtil.getProject(modelElement).getModelElementId(modelElement))) {
 						operationsToRemove.add(ao);
 					}
 				}
 				changePackage.getOperations().removeAll(operationsToRemove);
 			}
 			localHistoryInfo.setChangePackage(changePackage);
 			PrimaryVersionSpec versionSpec = VersioningFactory.eINSTANCE.createPrimaryVersionSpec();
 			versionSpec.setIdentifier(-1);
 			localHistoryInfo.setPrimerySpec(versionSpec);
 			revisions.add(localHistoryInfo);
 		}
 		revisions.addAll(historyInfos);
 
 		return revisions;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void setFocus() {
 		viewer.getControl().setFocus();
 	}
 
 	/**
 	 * @return the changePackageVisualizationHelper
 	 */
 	public ChangePackageVisualizationHelper getChangePackageVisualizationHelper() {
 		return changePackageVisualizationHelper;
 	}
 
 	/**
 	 * Highlights the given operations.
 	 * 
 	 * @param operations
 	 *            the operations
 	 */
 	public void highlightOperations(List<OperationId> operations) {
 		labelProvider.getHighlighted().clear();
 		labelProvider.getHighlighted().addAll(operations);
 		refresh();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.util.ProjectSpaceContainer#getProjectSpace()
 	 */
 	public ProjectSpace getProjectSpace() {
 		if (isUnlinkedFromNavigator) {
 			return null;
 		}
 		return this.projectSpace;
 	}
 
 }
