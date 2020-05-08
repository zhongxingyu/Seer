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
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.common.command.CommandStackListener;
 import org.eclipse.emf.common.ui.viewer.IViewerProvider;
 import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.emf.edit.domain.IEditingDomainProvider;
 import org.eclipse.emf.edit.ui.provider.UnwrappingSelectionProvider;
 import org.eclipse.jface.action.IMenuListener;
 import org.eclipse.jface.action.IMenuManager;
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
 import org.eclipse.ui.IMemento;
 import org.eclipse.ui.IPartListener;
 import org.eclipse.ui.IPropertyListener;
 import org.eclipse.ui.ISaveablePart2;
 import org.eclipse.ui.ISelectionListener;
 import org.eclipse.ui.IViewSite;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.part.ViewPart;
 
 import com.netxforge.netxstudio.screens.editing.actions.ActionHandlerDescriptor;
 import com.netxforge.netxstudio.screens.editing.actions.CreationActionsHandler;
 import com.netxforge.netxstudio.screens.editing.actions.EditingActionsHandler;
 import com.netxforge.netxstudio.screens.editing.actions.UIActionsHandler;
 import com.netxforge.netxstudio.screens.editing.selector.IScreen;
 
 /**
  * A ViewPart which acts as an editor.
  * 
  * It is also a SaveablePart2 so it will be participate in the workbench
  * dirty/save cycle. We are an editing domain providers. (Not shared).
  * 
  * 
  * @author dzonekl
  */
 public abstract class AbstractScreensViewPart extends ViewPart implements
 		ISaveablePart2, IPartListener, ISelectionListener,
 		IEditingDomainProvider, ISelectionProvider, IMenuListener,
 		IViewerProvider, IPropertyListener {
 
 	//	public static final String ID = "com.netxforge.netxstudio.ui.forms.EquipmentsViewPart"; //$NON-NLS-1$
 
 	/**
 	 * This keeps track of the selection of the view as a whole.
 	 */
 	protected ISelection viewSelection = StructuredSelection.EMPTY;
 
 	public abstract IEditingService getEditingService();
 
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
 		initializeMenu();
 	}
 
 	// Databinding API
 	protected abstract void initBindings();
 
 	public void dispose() {
 		super.dispose();
		// TODO Remove later. 
//		this.getSite().getPage().removeSelectionListener(pageSelectionListener);
 	}
 
	
	// TODO Remove later. 
//	private ISelectionListener pageSelectionListener;
 
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
 	// // TODO, do something with the selection.
 	// }
 
 	/**
 	 * Initialize the toolbar.
 	 */
 	private void initializeToolBar() {
 		@SuppressWarnings("unused")
 		IToolBarManager tbm = getViewSite().getActionBars().getToolBarManager();
 	}
 
 	/**
 	 * Initialize the menu.
 	 */
 	private void initializeMenu() {
 		IMenuManager manager = getViewSite().getActionBars().getMenuManager();
 		IMenuManager x = new MenuManager("Test",
 				"com.netxforge.netxstudio.test");
 		manager.add(x);
 	}
 
 	@Override
 	public void setFocus() {
 		// Set the focus
 	}
 
 	// Save memento state.
 	String AKEY = "AKey";
 	float aValue;
 	private ActionHandlerDescriptor actionHandlerDescriptor;
 
 	@Override
 	public void init(IViewSite site, IMemento memento) throws PartInitException {
 		super.init(site, memento);
 		if (memento != null && memento.getFloat(AKEY) != null) {
 			aValue = memento.getFloat(AKEY);
 		}
 		this.addPropertyListener(this);
 		site.getPage().addPartListener(this);
 		site.setSelectionProvider(this);
 
 		// Set the current editor as selection provider.
 		actionHandlerDescriptor = new ActionHandlerDescriptor();
 		actionHandlerDescriptor.addHandler(new EditingActionsHandler(getEditingService()));
 		actionHandlerDescriptor.addHandler(new CreationActionsHandler());
 		actionHandlerDescriptor.addHandler(new UIActionsHandler());
 
 		actionHandlerDescriptor.initActions(site.getActionBars());
 		// hookPageSelection();
 	}
 
 	public ActionHandlerDescriptor getActionHandlerDescriptor() {
 		return actionHandlerDescriptor;
 	}
 
 	// ISaveablePart2 API.
 	public void saveState(IMemento memento) {
 		super.saveState(memento);
 		memento.putFloat(AKEY, 0.1f);
 	}
 
 	/**
 	 * We deal with objects in resources outside our own editing domain.
 	 */
 	public void doSave(IProgressMonitor monitor) {
 		// Delegate to a pluggable service.
 		getEditingService().doSave(monitor);
 		firePropertyChange(ISaveablePart2.PROP_DIRTY);
 	}
 
 	public void doSaveAs() {
 		// TODO, do something.
 	}
 
 	// @Override
 	// public Object getAdapter(@SuppressWarnings("rawtypes") Class key) {
 	// if (key.equals(ISaveablePart2.class)) {
 	// return this;
 	// }
 	// return super.getAdapter(key);
 	// }
 
 	/**
 	 * Based on the command stack statues.
 	 */
 	public boolean isDirty() {
 		return getEditingService().isDirty();
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
 
 	public abstract IScreen getActiveScreen();
 
 	public void updateActiveScreenDirtyNess() {
 		if (this.getActiveScreen() == null) {
 			return;
 		}
 
 		IScreen screen = getActiveScreen();
 
 		String newTitle = "";
 		if( screen.getScreenForm() == null){
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
 
 	// IPartListner API.
 	public void partActivated(IWorkbenchPart part) {
 		// Register selection listeners.
 		if (part instanceof AbstractScreensViewPart) {
 			// Activate our global actions.
 			this.getActionHandlerDescriptor().setActivePart(part);
 		}
 	}
 
 	public void partBroughtToTop(IWorkbenchPart part) {
 		// Not used.
 	}
 
 	public void partClosed(IWorkbenchPart part) {
 		// Not used.
 	}
 
 	public void partDeactivated(IWorkbenchPart part) {
 		if (part instanceof AbstractScreensViewPart) {
 			// globActionsHandler.deactivate(part);
 		} else {
 		}
 	}
 
 	public void partOpened(IWorkbenchPart part) {
 	}
 
 	// ISelectionListener API
 
 	@SuppressWarnings("unused")
 	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
 		Object o = this.firstFromSelection(selection);
 	}
 
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
 	public Object getAdapter(@SuppressWarnings("rawtypes") Class key) {
 		return super.getAdapter(key);
 	}
 
 	// IEditingDomainProvider API.
 	AdapterFactoryEditingDomain domain = null;
 
 	public EditingDomain getEditingDomain() {
 
 		if (domain == null) {
 			domain = (AdapterFactoryEditingDomain) getEditingService()
 					.getEditingDomain();
 			// BasicCommandStack commandStack = new BasicCommandStack();
 			// Add a listener to set the viewer dirty state.
 			CommandStackListener cmdStackListener = new CommandStackListener() {
 				public void commandStackChanged(final EventObject event) {
 					getViewSite().getShell().getDisplay()
 							.asyncExec(new Runnable() {
 								public void run() {
 
 									// We should not dirty mark, while editing
 									// or new a single object.
 									firePropertyChange(ISaveablePart2.PROP_DIRTY);
 									getEditingService().setDirty();
 
 								}
 							});
 				}
 			};
 			domain.getCommandStack().addCommandStackListener(cmdStackListener);
 		}
 		return domain;
 	}
 
 	// ISelectionProvider API.
 	// In our case, our globalActionsHnadler actions listen to our selection
 	// provider when
 	// the part is is activated.
 	// Whatever screen is active, should provide selection.
 	protected Collection<ISelectionChangedListener> selectionChangedListeners = new ArrayList<ISelectionChangedListener>();
 
 	public void addSelectionChangedListener(ISelectionChangedListener listener) {
 		selectionChangedListeners.add(listener);
 	}
 
 	public ISelection getSelection() {
 		return viewSelection;
 	}
 
 	public void removeSelectionChangedListener(
 			ISelectionChangedListener listener) {
 		selectionChangedListeners.remove(listener);
 	}
 
 	public void setSelection(ISelection selection) {
 		this.viewSelection = selection;
 		for (ISelectionChangedListener listener : selectionChangedListeners) {
 			listener.selectionChanged(new SelectionChangedEvent(this, selection));
 		}
 	}
 
 	/**
 	 * This creates a context menu for the viewer and adds a listener as well
 	 * registering the menu for extension. <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @generated
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
 		contributeMenuAboutToShow(manager);
 	}
 
 	public abstract void contributeMenuAboutToShow(IMenuManager manager);
 
 	/**
 	 * This listens to which ever viewer is active.
 	 */
 	protected ISelectionChangedListener selectionChangedListener;
 
 	/**
 	 * This keeps track of the active content viewer, which may be either one of
 	 * the viewers in the screens.
 	 * 
 	 * @generated
 	 */
 	protected Viewer currentViewer;
 
 	/**
 	 * Call from which ever screen with a view which can/should provide
 	 * selection.
 	 * 
 	 * @param viewer
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
 
 	public Viewer getViewer() {
 		return currentViewer;
 	}
 
 }
