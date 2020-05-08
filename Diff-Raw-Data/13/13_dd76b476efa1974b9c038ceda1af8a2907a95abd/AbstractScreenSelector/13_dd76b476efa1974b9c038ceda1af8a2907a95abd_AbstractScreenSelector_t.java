 /*******************************************************************************
  * Copyright (c) May 9, 2011 NetXForge.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *  You should have received a copy of the GNU Lesser General Public License
  *   along with this program.  If not, see <http://www.gnu.org/licenses/>
  *
  * Contributors:
  *    Christophe Bouhier - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package com.netxforge.netxstudio.screens.editing.selector;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IMemento;
 import org.eclipse.ui.ISaveablePart2;
 import org.eclipse.ui.part.IShowInSource;
 import org.eclipse.ui.part.ShowInContext;
 
 import com.google.inject.Inject;
import com.netxforge.netxstudio.common.model.ModelUtils;
 import com.netxforge.netxstudio.screens.editing.AbstractScreensViewPart;
 import com.netxforge.netxstudio.screens.editing.IEditingService;
 import com.netxforge.netxstudio.screens.editing.actions.ActionHandlerDescriptor;
 import com.netxforge.netxstudio.screens.editing.actions.DynamicScreensActionHandler;
 
 /**
  * @author Christophe Bouhier christophe.bouhier@netxforge.com
  * 
  */
 public abstract class AbstractScreenSelector extends AbstractScreensViewPart
 		implements ScreenChangeListener, IShowInSource {
 
 	public static final String ID = "com.netxforge.netxstudio.screens.selector.AbstractScreenSelector"; //$NON-NLS-1$
 
 	@Inject
 	protected IScreenFormService screenFormService;
 
	@Inject
	protected ModelUtils modelUtils;

 	private IScreen activeScreen;
 
 	public AbstractScreenSelector() {
 	}
 
 	public IScreenFormService getScreenFormService() {
 		return screenFormService;
 	}
 
 	public IEditingService getEditingService() {
 		return screenFormService.getEditingService();
 	}
 
 	/**
 	 * Create contents of the view part.
 	 * 
 	 * @param parent
 	 */
 	@SuppressWarnings("deprecation")
 	@Override
 	public void createPartControl(Composite parent) {
 
 		createActions();
 		initializeToolBar();
 		initializeMenu();
 
 		// Our service.
 		screenFormService.initalize(this, parent);
 		getEditingService().setViewerProvider(this);
 		getEditingService().setScreenProvider(this);
 		screenFormService.addScreenChangeListener(this);
 		buildSelector();
 	}
 
 	public abstract void buildSelector();
 
 	/**
 	 * Create the actions.
 	 */
 	private void createActions() {
 		// Create the actions
 	}
 
 	/**
 	 * Initialize the toolbar.
 	 */
 	private void initializeToolBar() {
 		@SuppressWarnings("unused")
 		IToolBarManager toolbarManager = getViewSite().getActionBars()
 				.getToolBarManager();
 	}
 
 	/**
 	 * Initialize the menu.
 	 */
 	private void initializeMenu() {
 		@SuppressWarnings("unused")
 		IMenuManager menuManager = getViewSite().getActionBars()
 				.getMenuManager();
 	}
 
 	@Override
 	public void setFocus() {
 		// Set the focus
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.netxforge.netxstudio.screens.editing.AbstractEditorViewPart#initBindings
 	 * ()
 	 */
 	@Override
 	protected void initBindings() {
 	}
 
 	@Override
 	public void dispose() {
 		super.dispose();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.netxforge.netxstudio.screens.editing.selector.ScreenChangeListener
 	 * #screenChanged()
 	 */
 	public void screenChanged(IScreen screen) {
 
 		// Some screens won't have a viewer, in this case
 		// the current viewer will be null, and an empty selection will be set.
 		if (screen != null) {
 			getActionHandlerDescriptor().clearDynamicHandlers();
 			// before activating the screen, set the selection providerts.
 			this.setCurrentScreen(screen);
 			this.activeScreen = screen;
 			// Make sure we update the dirty state, when changing screen.
 			firePropertyChange(ISaveablePart2.PROP_DIRTY);
 
 			// restore the state of the screen.
 			if (this.getMemento() != null) {

				String validMementoElement = modelUtils
						.underscopeWhiteSpaces(screen.getScreenName());
				IMemento child = this.getMemento()
						.getChild(validMementoElement);
 				if (child != null) {
 					screen.restoreState(child);
 				}
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.netxforge.netxstudio.screens.editing.selector.ScreenChangeListener
 	 * #screenWidgetChanged
 	 * (com.netxforge.netxstudio.screens.editing.selector.IScreen)
 	 */
 	public void screenWidgetChanged(IScreen screen) {
 		// force an update of the current screen.
 		this.setCurrentScreen(screen);
 	}
 
 	public IScreen getScreen() {
 		return this.activeScreen;
 	}
 
 	@Override
 	public void contributeMenuAboutToShow(IMenuManager menuManager) {
 
 		// Clear the menu manager.
 		menuManager.removeAll();
 
 		ActionHandlerDescriptor actionHandlerDescriptor = this
 				.getActionHandlerDescriptor();
 		actionHandlerDescriptor.setMenuManager(menuManager);
 
 		if (!ScreenUtil.isReadOnlyOperation(getScreen().getOperation())) {
 			actionHandlerDescriptor.setEnableEditActions(true);
 			if (this.getScreen().getViewer() instanceof TreeViewer) {
 				actionHandlerDescriptor.setEnableChildCreationActions(true);
 			} else {
 				actionHandlerDescriptor.setEnableChildCreationActions(false);
 			}
 		} else {
 			actionHandlerDescriptor.setEnableEditActions(false);
 			actionHandlerDescriptor.setEnableChildCreationActions(false);
 			actionHandlerDescriptor.setEnableSiblingCreationActions(false);
 		}
 		actionHandlerDescriptor.clearDynamicHandlers();
 
 		DynamicScreensActionHandler dynamicScreensActionHandler;
 
 		// !!!! actions would be created dynamicly.....
 		IScreen screen = getScreen();
 		if (screen != null && screen.getActions() != null) {
 			List<IAction> actions = reverse(this.getScreen().getActions());
 			dynamicScreensActionHandler = new DynamicScreensActionHandler();
 			dynamicScreensActionHandler.addActions(actions);
 			actionHandlerDescriptor.addHandler(dynamicScreensActionHandler);
 		}
 		actionHandlerDescriptor.showMenu();
 	}
 
 	public static List<IAction> reverse(IAction[] arr) {
 
 		List<IAction> list = Arrays.asList(arr);
 		Collections.reverse(list);
 		return list;
 	}
 
 	public ShowInContext getShowInContext() {
 		if (this.getScreen() != null) {
 			return this.getScreen().getShowIn(this.getSelection());
 		}
 		return new ShowInContext(null, this.getSelection());
 	}
 
 }
