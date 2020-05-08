 /*******************************************************************************
  * Copyright (c) May 1, 2011 NetXForge.
  * 
  * This program is free software: you can redistribute it and/or modify it under
  * the terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option) any
  * later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details. You should have received a copy of the GNU Lesser General Public
  * License along with this program. If not, see <http://www.gnu.org/licenses/>
  * 
  * Contributors: Christophe Bouhier - initial API and implementation and/or
  * initial documentation
  *******************************************************************************/
 
 package com.netxforge.netxstudio.screens.editing;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.EventObject;
 import java.util.List;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.cdo.CDOObject;
 import org.eclipse.emf.cdo.CDOState;
 import org.eclipse.emf.cdo.common.id.CDOID;
 import org.eclipse.emf.cdo.common.revision.CDORevision;
 import org.eclipse.emf.common.command.BasicCommandStack;
 import org.eclipse.emf.common.command.CommandStackListener;
 import org.eclipse.emf.common.ui.viewer.IViewerProvider;
 import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.emf.edit.domain.IEditingDomainProvider;
 import org.eclipse.emf.edit.provider.AdapterFactoryItemDelegator;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
 import org.eclipse.emf.edit.ui.provider.UnwrappingSelectionProvider;
 import org.eclipse.emf.edit.ui.view.ExtendedPropertySheetPage;
 import org.eclipse.jface.action.IMenuListener;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.IStatusLineManager;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.StructuredViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IMemento;
 import org.eclipse.ui.IPartListener;
 import org.eclipse.ui.IPropertyListener;
 import org.eclipse.ui.ISaveablePart2;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.IViewReference;
 import org.eclipse.ui.IViewSite;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.XMLMemento;
 import org.eclipse.ui.part.ViewPart;
 import org.eclipse.ui.views.properties.IPropertySheetPage;
 
 import com.google.common.collect.Lists;
 import com.google.inject.Inject;
 import com.netxforge.netxstudio.common.model.ModelUtils;
 import com.netxforge.netxstudio.screens.editing.actions.handlers.ActionHandlerDescriptor;
 import com.netxforge.netxstudio.screens.editing.actions.handlers.CreationActionsHandler;
 import com.netxforge.netxstudio.screens.editing.actions.handlers.ObjectEditingActionsHandler;
 import com.netxforge.netxstudio.screens.editing.actions.handlers.UIActionsHandler;
 import com.netxforge.netxstudio.screens.editing.internal.EditingActivator;
 import com.netxforge.netxstudio.screens.editing.util.MementoUtil;
 
 /**
  * A ViewPart which acts as an editor.
  * 
  * It is also a SaveablePart2 so it will be participate in the workbench
  * dirty/save cycle. We are an editing domain providers. (Not shared).
  * 
  * @author Christophe Bouhier
  */
 public abstract class AbstractScreensViewPart extends ViewPart implements
 		ISaveablePart2, IPartListener, IEditingDomainProvider,
 		ISelectionProvider, IMenuListener, IViewerProvider, IPropertyListener,
 		IScreenProvider {
 
 	/**
 	 * This keeps track of the selection of the view as a whole.
 	 */
 	protected ISelection viewSelection = StructuredSelection.EMPTY;
 
 	@Inject
 	protected ModelUtils modelUtils;
 
 	@Inject
 	protected MementoUtil mementoUtil;
 
 	// private MenuManager contextMenu;
 
 	/**
 	 * Client should implement to provide the IEditingService
 	 * 
 	 * @return
 	 */
 	public abstract IEditingService getEditingService();
 
 	/**
 	 * Clients should implement to provide the IScreenFormService
 	 * 
 	 * @return
 	 */
 	public abstract IScreenFormService getScreenService();
 
 	/**
 	 * Returns all visible IScreen from the workbench.
 	 */
 	public IScreen[] getScreens() {
 		return doGetScreens(); // Delegate to static.
 	}
 
 	/**
 	 * @return
 	 */
 	private static IScreen[] doGetScreens() {
 		IWorkbenchWindow[] workbenchWindows = PlatformUI.getWorkbench()
 				.getWorkbenchWindows();
 
 		final ArrayList<IScreen> screens = Lists.newArrayList();
 
 		for (IWorkbenchWindow wbw : workbenchWindows) {
 			for (IWorkbenchPage iWorkbenchPage : wbw.getPages()) {
 				for (IViewReference iViewReference : iWorkbenchPage
 						.getViewReferences()) {
 					IViewPart viewPart = iViewReference.getView(false);
 					if (viewPart instanceof AbstractScreenSelector) {
 						AbstractScreenSelector selector = (AbstractScreenSelector) viewPart;
 						IScreen screen = selector.getScreen();
 						if (screen != null) {
 							screens.add(screen);
 						}
 					} else {
 						// it could also be the viewPart itself is an IScreen.
 						if (viewPart instanceof IScreen) {
 							screens.add((IScreen) viewPart);
 						}
 					}
 				}
 			}
 		}
 		return screens.toArray(new IScreen[screens.size()]);
 	}
 
 	public AbstractScreensViewPart() {
 		createActions();
 	}
 
 	/**
 	 * Supers should override.
 	 */
 	private void createActions() {
 	}
 
 	/**
 	 * Create contents of the view part.
 	 * 
 	 * @param parent
 	 */
 	@Override
 	public void createPartControl(Composite parent) {
 		initializeToolBar();
 	}
 
 	// Databinding API
 	protected abstract void initBindings();
 
 	public void dispose() {

 		this.getEditingService().disposeData();
 		super.dispose();
 	}
 
 	// TODO Remove later.
 	// private ISelectionListener pageSelectionListener;
 
 	// private void hookPageSelection() {
 	// pageSelectionListener = new ISelectionListener() {
 	//
 	// public void selectionChanged(IWorkbenchPart part,
 	// ISelection selection) {
 	// pageSelectionChanged(part, selection);
 	// }
 	// };
 	// this.getSite().getPage().addSelectionListener(pageSelectionListener);
 	// }
 	//
 	// @SuppressWarnings("unused")
 	// protected void pageSelectionChanged(IWorkbenchPart part,
 	// ISelection selection) {
 	// if (part == this) {
 	// System.out.println("wrong part return");
 	// return;
 	// }
 	// Object sel = this.firstFromSelection(selection);
 	// }
 
 	/**
 	 * Initialize the toolbar.
 	 */
 	private void initializeToolBar() {
 		@SuppressWarnings("unused")
 		IToolBarManager tbm = getViewSite().getActionBars().getToolBarManager();
 	}
 
 	@Override
 	public void setFocus() {
 		if (this.getScreen() != null) {
 			this.getScreen().setScreenFocus();
 		}
 	}
 
 	private ActionHandlerDescriptor actionHandlerDescriptor;
 
 	/*
 	 * The memento.
 	 */
 	protected IMemento memento;
 
 	private ExtendedPropertySheetPage propertySheetPage;
 
 	public IMemento getMemento() {
 		return memento;
 	}
 
 	/**
 	 * The memento is delegated to the IScreen whenever it becomes active.
 	 */
 	@Override
 	public void init(IViewSite site, IMemento memento) throws PartInitException {
 		super.init(site, memento);
 
 		if (memento == null) {
 			XMLMemento newMemento = XMLMemento
 					.createWriteRoot(MementoUtil.MEM_KEY_SCREEN_PART);
 			this.memento = newMemento;
 		} else {
 			// We could have a IWorkbenchConstants.TAG_VIEW_STATE which is set,
 			// but no child if the view part didn't close properly.
 			this.memento = memento.getChild(MementoUtil.MEM_KEY_SCREEN_PART);
 			if (this.memento == null) {
 				XMLMemento newMemento = XMLMemento
 						.createWriteRoot(MementoUtil.MEM_KEY_SCREEN_PART);
 				this.memento = newMemento;
 			}
 		}
 
 		this.addPropertyListener(this);
 		site.getPage().addPartListener(this);
 		// Set the current editor as selection provider.
 		site.setSelectionProvider(this);
 
 		// FIXME, ACTION handlers should be installed dynamicly,
 		// 1) Currently action handlers are static per Part
 		// 2) Each viewer in a part gets the same menu. (Actually for each
 		// viewer, a menu is created).
 		// 3) The action bar is initiated statically, so changing the viewer in
 		// an IScreen,
 		// doesn't update the i.e. the ActionBar which holds global actions.
 		// If we change to a text based viewer, the global actions delete, cut,
 		// copy, paste, select-all are EMF Actions to
 		// deal with StructuredViewer.
 
 		// Add some static action handlers which are updated by the
 		// selection
 		// provider of the active part. which is this. We can also add
 		// dynamic action handlers.
 		actionHandlerDescriptor = new ActionHandlerDescriptor();
 		actionHandlerDescriptor.addHandler(new ObjectEditingActionsHandler(
 				getEditingService()));
 		actionHandlerDescriptor.addHandler(new CreationActionsHandler());
 		actionHandlerDescriptor.addHandler(new UIActionsHandler());
 		actionHandlerDescriptor.initActions(site.getActionBars());
 		// hookPageSelection();
 
 		this.getEditingService().getEditingDomain().getCommandStack()
 				.addCommandStackListener(cmdStackListener);
 	}
 
 	public ActionHandlerDescriptor getActionHandlerDescriptor() {
 		return actionHandlerDescriptor;
 	}
 
 	// UI states are delegated to the the IScreen interface, save the result in
 	// our memento.
 	public void saveState(IMemento memento) {
 
 		IScreenFormService screenService = getScreenService();
 		if (this.getScreen() != null) {
 			if (screenService != null) {
 				screenService.saveScreenState(this.getScreen());
 			}
 
 			mementoUtil.rememberString(this.getMemento(), this.getScreen()
 					.getScreenName(), MementoUtil.MEM_KEY_CURRENT_SCREEN);
 		}
 
 		// Write a pre-created child memento to the ViewState memento, for
 		// the next time we meet.
 		if (memento.getChild(MementoUtil.MEM_KEY_SCREEN_PART) == null) {
 			IMemento createChild = memento
 					.createChild(MementoUtil.MEM_KEY_SCREEN_PART);
 			createChild.putMemento(this.getMemento());
 		}
 	}
 
 	/**
 	 * We deal with objects in resources outside our own editing domain.
 	 */
 	public void doSave(IProgressMonitor monitor) {
 
 		// Delegate to a pluggable service.
 		//
 		getEditingService().doSave(monitor);
 
 		firePropertyChange(ISaveablePart2.PROP_DIRTY);
 	}
 
 	public void doSaveAs() {
 		throw new UnsupportedOperationException("Save As, not supported");
 	}
 
 	// @Override
 	// public Object getAdapter(@SuppressWarnings("rawtypes") Class key) {
 	// if (key.equals(ISaveablePart2.class)) {
 	// return this;
 	// }
 	// return super.getAdapter(key);
 	// }
 
 	/**
 	 * Delegates to the editing service.
 	 */
 	public boolean isDirty() {
 		// Should know about the type of screen, so read-only, is not asked for
 		// dirtynes..
 		if (this.getScreen() != null
 				&& !ScreenUtil.isReadOnlyOperation(this.getScreen()
 						.getOperation())) {
 			return getEditingService().isDirty();
 		} else {
 			return false;
 		}
 	}
 
 	public boolean isSaveAsAllowed() {
 		return false;
 	}
 
 	public boolean isSaveOnCloseNeeded() {
 		return true;
 	}
 
 	public int promptToSaveOnClose() {
 		return ISaveablePart2.PROP_DIRTY;
 	}
 
 	public void propertyChanged(Object source, int propId) {
 		this.updateActiveScreenDirtyNess();
 	}
 
 	public void updateActiveScreenDirtyNess() {
 		if (this.getScreen() == null
 				|| this.getScreen().getScreenForm().isDisposed()) {
 			return;
 		}
 
 		IScreen screen = getScreen();
 
 		String newTitle = "";
 		if (screen.getScreenForm() == null) {
 			return;
 		}
 		String currentTitle = screen.getScreenForm().getText();
 
 		if (getEditingService().isDirty()) {
 			if (currentTitle.endsWith(" * ")) {
 				return;
 			}
 			newTitle = currentTitle.concat(" * ");
 		} else {
 			if (currentTitle.endsWith(" * ")) {
 				newTitle = currentTitle.substring(0,
 						currentTitle.indexOf(" * "));
 			} else {
 				newTitle = currentTitle;
 			}
 		}
 		screen.getScreenForm().setText(newTitle);
 	}
 
 	/**
 	 * An event for part activity
 	 * 
 	 * @author Christophe Bouhier
 	 */
 	protected enum PART_EVENT {
 		ACTIVATED, TOTOP, CLOSED, OPENEND, DEACTIVATE
 	}
 
 	/**
 	 * Called with corresponding {@link #PART_EVENT} set, clients should
 	 * implement.
 	 * 
 	 * @param part
 	 */
 	protected abstract void customPartHook(IWorkbenchPart part, PART_EVENT event);
 
 	/**
 	 * Update the action handler descriptors with the active part.
 	 */
 	public void partActivated(IWorkbenchPart part) {
 		// Register selection listeners.
 		if (part == this) {
 			// Activate our global actions.
 			this.getActionHandlerDescriptor().setActivePart(part);
 		}
 		customPartHook(part, PART_EVENT.ACTIVATED);
 	}
 
 	public void partBroughtToTop(IWorkbenchPart part) {
 		customPartHook(part, PART_EVENT.TOTOP);
 	}
 
 	public void partClosed(IWorkbenchPart part) {
 		customPartHook(part, PART_EVENT.CLOSED);
 	}
 
 	public void partDeactivated(IWorkbenchPart part) {
 		customPartHook(part, PART_EVENT.DEACTIVATE);
 	}
 
 	public void partOpened(IWorkbenchPart part) {
 		customPartHook(part, PART_EVENT.OPENEND);
 	}
 
 	// ISelectionListener API
 
 	@SuppressWarnings("unused")
 	// public void selectionChanged(IWorkbenchPart part, ISelection selection) {
 	// System.out.println("AbstractScreensViewPart#selectionChanged " +
 	// selection);
 	// Object o = this.firstFromSelection(selection);
 	// }
 	/**
 	 * Get the first object from the selection.
 	 * 
 	 * @param selection
 	 * @return
 	 */
 	private Object firstFromSelection(ISelection selection) {
 		if (selection instanceof IStructuredSelection) {
 			Object first = ((IStructuredSelection) selection).getFirstElement();
 			return first;
 		}
 		return selection;
 	}
 
 	@Override
 	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
 
 		if (adapter.equals(IPropertySheetPage.class)) {
 			return getPropertySheetPage();
 		}
 
 		return super.getAdapter(adapter);
 
 	}
 
 	/**
 	 * This accesses a cached version of the property sheet. <!-- begin-user-doc
 	 * --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public IPropertySheetPage getPropertySheetPage() {
 		if (propertySheetPage == null) {
 			propertySheetPage = new ExtendedPropertySheetPage(
 					(AdapterFactoryEditingDomain) this.getEditingDomain()) {
 				@Override
 				public void setSelectionToViewer(List<?> selection) {
 
 					// CB, We don't have this method.
 					// The default EMF implementation calls setSelection on the
 					// current Viewer with an Async call.
 					// In our case, it would be setSelectionToScreen. The
 					// current viewer in the screen
 
 					// AbstractScreensViewPart.this.setSelectionToViewer(selection);
 
 					// Convert the selection, to ISelection like in
 					// setSelectionToViewer.
 					// AbstractScreensViewPart.this.getScreen().setSelection(selection);
 					AbstractScreensViewPart.this.setFocus();
 				}
 
 				@Override
 				public void setActionBars(IActionBars actionBars) {
 					super.setActionBars(actionBars);
 
 					// CB We don't have a A contributor to share the actions
 					// with the property sheet page.
 					// Investigate.
 					// getActionBarContributor().shareGlobalActions(this,
 					// actionBars);
 				}
 			};
 			propertySheetPage
 					.setPropertySourceProvider(new AdapterFactoryContentProvider(
 							EMFEditingService.getAdapterFactory()));
 		}
 
 		return propertySheetPage;
 	}
 
 	// BasicCommandStack commandStack = new BasicCommandStack();
 	// Add a listener to set the viewer dirty state.
 	final CommandStackListener cmdStackListener = new CommandStackListener() {
 		public void commandStackChanged(final EventObject event) {
 
 			// Note this also fires when flushing the command stack, as
 			// this is executed async, the widget is disposed so bail if there
 			// is no recent command
 			if (event.getSource() instanceof BasicCommandStack) {
 				BasicCommandStack stack = (BasicCommandStack) event.getSource();
 				if (stack.getMostRecentCommand() == null) {
 					return;
 				}
 			}
 
 			getViewSite().getShell().getDisplay().asyncExec(new Runnable() {
 				public void run() {
 
 					// We should not dirty mark, while editing
 					// or new a single object.
 					firePropertyChange(ISaveablePart2.PROP_DIRTY);
 					getEditingService().setDirty();
 
 					// FIXME Some views don't have data binding,
 					// so we need to refresh the input.
 					// currentViewer.refresh();
 					if (EditingActivator.DEBUG) {
 						EditingActivator.TRACE.trace(null,
 								"Command stack changed");
 						// System.out
 						// .println("Command stack, fire command stack changed, source="
 
 						// + event.getSource());
 					}
 				}
 			});
 		}
 	};
 
 	// IEditingDomainProvider API.
 	// AdapterFactoryEditingDomain domain = null;
 
 	public EditingDomain getEditingDomain() {
 		return getEditingService().getEditingDomain();
 	}
 
 	public void publicFirePropertyChange(int propertyId) {
 		this.firePropertyChange(propertyId);
 
 	}
 
 	// ISelectionProvider API.
 	// In our case, our globalActionsHnadler actions listen to our selection
 	// provider when
 	// the part is is activated.
 	// Whatever screen is active, should provide selection.
 	protected Collection<ISelectionChangedListener> selectionChangedListeners = new ArrayList<ISelectionChangedListener>();
 
 	public void addSelectionChangedListener(ISelectionChangedListener listener) {
 		if (!selectionChangedListeners.contains(listener)) {
 			selectionChangedListeners.add(listener);
 		}
 
 	}
 
 	public ISelection getSelection() {
 		return viewSelection;
 	}
 
 	public void removeSelectionChangedListener(
 			ISelectionChangedListener listener) {
 		selectionChangedListeners.remove(listener);
 	}
 
 	/**
 	 * Update to pass on the original {@link SelectionChangedEvent event}
 	 * 
 	 * @param sce
 	 */
 	public void setSelection(SelectionChangedEvent sce) {
 		this.viewSelection = sce.getSelection();
 		for (ISelectionChangedListener listener : selectionChangedListeners) {
 			listener.selectionChanged(sce);
 		}
 
 		// Set the status, either selection or the main object handled by the
 		// screen.
 		if (!viewSelection.isEmpty()) {
 			setStatusLineManager(viewSelection);
 		} else {
 			IScreen screen = this.getScreen();
 			setStatusLineManager(screen.getScreenObjects());
 		}
 	}
 
 	public void setSelection(ISelection selection) {
 		this.viewSelection = selection;
 		for (ISelectionChangedListener listener : selectionChangedListeners) {
 			listener.selectionChanged(new SelectionChangedEvent(this, selection));
 		}
 
 		// Set the status, either selection or the main object handled by the
 		// screen.
 		if (!selection.isEmpty()) {
 			setStatusLineManager(selection);
 		} else {
 			IScreen screen = this.getScreen();
 			setStatusLineManager(screen.getScreenObjects());
 		}
 	}
 
 	protected void setStatusLineManager(Collection<CDOObject> screenObjects) {
 		String message = "";
 		switch (screenObjects.size()) {
 		case 0: {
 			message = "No objects";
 			break;
 		}
 		case 1: {
 			CDOObject next = screenObjects.iterator().next();
 			CDOID cdoID = ((CDOObject) next).cdoID();
 			String text = new AdapterFactoryItemDelegator(EMFEditingService
 					.getAdapterFactory()).getText(next);
 
 			message = "Screen object: " + text + " OID:" + cdoID;
 
 			// An object could be in proxy state, append the version if not.
 			// (Otherwise the cdo revision will be null;
 			if (next.cdoState() != CDOState.PROXY) {
 				CDORevision cdoRevision = next.cdoRevision();
 				int version = cdoRevision.getVersion();
 				message += " version: " + version;
 			}
 
 			break;
 		}
 		default: {
 			message = "Objects: " + Integer.toString(screenObjects.size());
 			break;
 		}
 		}
 
 		// System.out.println("status message: " + message);
 		// for(StackTraceElement se : Thread.currentThread().getStackTrace()){
 		// System.out.println("--" + se.toString());
 		// }
 
 		setStatusLineManager(message);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	private void setStatusLineManager(ISelection selection) {
 
 		String message = "";
 		if (selection instanceof IStructuredSelection) {
 			Collection<?> collection = ((IStructuredSelection) selection)
 					.toList();
 			switch (collection.size()) {
 			case 0: {
 				message = "No selection";
 				break;
 			}
 			case 1: {
 				Object next = collection.iterator().next();
 				if (next instanceof CDOObject) {
 					message = modelUtils.cdoObjectToString((CDOObject) next,
 							new AdapterFactoryItemDelegator(EMFEditingService
 									.getAdapterFactory()).getText(next));
 				}
 				break;
 			}
 			default: {
 				message = "Selection: " + Integer.toString(collection.size());
 				break;
 			}
 			}
 		}
 
 		// System.out.println("status message: " + message);
 		// for(StackTraceElement se : Thread.currentThread().getStackTrace()){
 		// System.out.println("--" + se.toString());
 		// }
 
 		this.setStatusLineManager(message);
 	}
 
 	private void setStatusLineManager(String message) {
 
 		IStatusLineManager statusLineManager = this.getViewSite()
 				.getActionBars().getStatusLineManager();
 		if (statusLineManager != null) {
 			statusLineManager.setMessage(message);
 		}
 	}
 
 	/**
 	 * This creates a context menu for the viewer and adds a listener as well
 	 * registering the menu for extension. <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 */
 	protected void augmentContextMenuFor(StructuredViewer viewer) {
 
 		MenuManager contextMenu = new MenuManager("#PopUp");
 		contextMenu.add(new Separator("additions"));
 		contextMenu.setRemoveAllWhenShown(true);
 		contextMenu.addMenuListener(this);
 		Menu menu = contextMenu.createContextMenu(viewer.getControl());
 		viewer.getControl().setMenu(menu);
 
 		getSite().registerContextMenu(contextMenu,
 				new UnwrappingSelectionProvider(viewer));
 
 		// if (viewer instanceof TreeViewer) {
 		// int dndOperations = DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK;
 		// Transfer[] transfers = new Transfer[] { LocalTransfer.getInstance()
 		// };
 		// viewer.addDragSupport(dndOperations, transfers,
 		// new ViewerDragAdapter(viewer));
 		// viewer.addDropSupport(dndOperations, transfers,
 		// new EditingDomainViewerDropAdapter(this.getEditingDomain(),
 		// viewer));
 		// }
 	}
 
 	public void menuAboutToShow(IMenuManager manager) {
 
 		// CB We might not be the active part, make sure we are activated.
 		// this is important for show-in, which activates the show-in part.
 		IWorkbenchPart activePart = this.getSite().getWorkbenchWindow()
 				.getActivePage().getActivePart();
 		if (activePart != this) {
 			this.getSite().getWorkbenchWindow().getActivePage().activate(this);
 		}
 		contributeMenuAboutToShow(manager);
 	}
 
 	/**
 	 * Implementors should populate the given {@link IMenuManager } with a
 	 * context menu for an IScreen.
 	 * 
 	 * @param manager
 	 */
 	public abstract void contributeMenuAboutToShow(IMenuManager manager);
 
 	/**
 	 * This listens to which ever viewer is active.
 	 */
 	protected ISelectionChangedListener selectionChangedListener;
 
 	/**
 	 * This keeps track of the active content viewer, which may be either one of
 	 * the viewers in the screens.
 	 * 
 	 * @deprecated
 	 */
 	protected Viewer currentViewer;
 
 	/**
 	 * Call from which ever screen with multiple viewers which can/should
 	 * provide selection.
 	 * 
 	 * @param viewer
 	 */
 	public void setCurrentScreen(IScreen screen) {
 
 		if (selectionChangedListener == null) {
 			// Create the listener on demand.
 			//
 			selectionChangedListener = new ISelectionChangedListener() {
 				// This just notifies those things that are affected by the
 				// section.
 				//
 				public void selectionChanged(
 						SelectionChangedEvent selectionChangedEvent) {
 					setSelection(selectionChangedEvent);
 				}
 			};
 		}
 
 		// Stop listening to the old one.
 		//
 		if (getScreen() != null) {
 			getScreen()
 					.removeSelectionChangedListener(selectionChangedListener);
 		}
 
 		// Start listening to the new one.
 		//
 		if (screen != null) {
 			screen.addSelectionChangedListener(selectionChangedListener);
 		}
 
 		// Set the editors selection based on the current screen's
 		// selection.
 		setSelection(screen == null ? StructuredSelection.EMPTY : screen
 				.getSelection());
 
 		Viewer viewer = screen.getViewer();
 		if (viewer instanceof StructuredViewer) {
 			// Don't do that yet, as we have no facility to learn the
 			// existing menu items.
 			augmentContextMenuFor((StructuredViewer) viewer);
 		}
 
 		// Install a menu on the active viewers.
 		// for (Viewer v : screen.getViewers()) {
 		// // Install a context menu, for all possible viewers, note
 		// // all actions will be installed, we don't differentiate which
 		// // actions are added to which viewer.
 		// if (v instanceof StructuredViewer) {
 		// // Don't do that yet, as we have no facility to learn the
 		// // existing menu items.
 		// augmentContextMenuFor((StructuredViewer) v);
 		// }
 		// }
 	}
 
 	/**
 	 * Call from which ever screen with a view which can/should provide
 	 * selection.
 	 * 
 	 * @param viewer
 	 * @deprecated Use setCurrentScreen instead.
 	 */
 	public void setCurrentViewer(Viewer viewer) {
 		if (currentViewer != viewer) {
 			if (selectionChangedListener == null) {
 				// Create the listener on demand.
 				//
 				selectionChangedListener = new ISelectionChangedListener() {
 					// This just notifies those things that are affected by the
 					// section.
 					//
 					public void selectionChanged(
 							SelectionChangedEvent selectionChangedEvent) {
 						setSelection(selectionChangedEvent.getSelection());
 					}
 				};
 			}
 
 			// Stop listening to the old one.
 			//
 			if (currentViewer != null) {
 				currentViewer
 						.removeSelectionChangedListener(selectionChangedListener);
 			}
 
 			// Start listening to the new one.
 			//
 			if (viewer != null) {
 				viewer.addSelectionChangedListener(selectionChangedListener);
 			}
 
 			// Remember it.
 			//
 			currentViewer = viewer;
 
 			// Set the editors selection based on the current viewer's
 			// selection.
 			//
 			setSelection(currentViewer == null ? StructuredSelection.EMPTY
 					: currentViewer.getSelection());
 
 			// Install a context menu.
 			if (currentViewer instanceof StructuredViewer) {
 				// Don't do that yet, as we have no facility to learn the
 				// existing menu items.
 				augmentContextMenuFor((StructuredViewer) viewer);
 			}
 		}
 	}
 
 	/**
 	 * Get the current viewer.
 	 * 
 	 * @deprecated
 	 */
 	public Viewer getViewer() {
 		return currentViewer;
 	}
 
 }
