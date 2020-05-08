 /*******************************************************************************
  * Copyright (c) 2007, 2010 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.navigation.ui.swt.views;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.osgi.service.log.LogService;
 
 import org.eclipse.equinox.log.Logger;
 import org.eclipse.jface.action.IContributionItem;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.CoolBar;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Item;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.ToolBar;
 import org.eclipse.swt.widgets.ToolItem;
 import org.eclipse.ui.IPageLayout;
 import org.eclipse.ui.IPerspectiveFactory;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.IViewReference;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.internal.WorkbenchPage;
 
 import org.eclipse.riena.core.Log4r;
 import org.eclipse.riena.core.util.StringUtils;
 import org.eclipse.riena.internal.navigation.ui.swt.Activator;
 import org.eclipse.riena.internal.ui.ridgets.swt.uiprocess.UIProcessRidget;
 import org.eclipse.riena.navigation.ApplicationModelFailure;
 import org.eclipse.riena.navigation.IModuleGroupNode;
 import org.eclipse.riena.navigation.IModuleNode;
 import org.eclipse.riena.navigation.ISubApplicationNode;
 import org.eclipse.riena.navigation.ISubModuleNode;
 import org.eclipse.riena.navigation.listener.NavigationTreeObserver;
 import org.eclipse.riena.navigation.listener.SubApplicationNodeListener;
 import org.eclipse.riena.navigation.listener.SubModuleNodeListener;
 import org.eclipse.riena.navigation.model.SubApplicationNode;
 import org.eclipse.riena.navigation.ui.controllers.SubApplicationController;
 import org.eclipse.riena.navigation.ui.swt.binding.DelegatingRidgetMapper;
 import org.eclipse.riena.navigation.ui.swt.binding.InjectSwtViewBindingDelegate;
 import org.eclipse.riena.navigation.ui.swt.component.MenuCoolBarComposite;
 import org.eclipse.riena.navigation.ui.swt.presentation.SwtViewId;
 import org.eclipse.riena.navigation.ui.swt.presentation.SwtViewProvider;
 import org.eclipse.riena.ui.ridgets.IActionRidget;
 import org.eclipse.riena.ui.ridgets.IRidget;
 import org.eclipse.riena.ui.ridgets.controller.IController;
 import org.eclipse.riena.ui.ridgets.swt.uibinding.AbstractViewBindingDelegate;
 import org.eclipse.riena.ui.ridgets.swt.uibinding.SwtControlRidgetMapper;
 import org.eclipse.riena.ui.ridgets.uibinding.DefaultBindingManager;
 import org.eclipse.riena.ui.ridgets.uibinding.IBindingManager;
 import org.eclipse.riena.ui.ridgets.uibinding.IBindingPropertyLocator;
 import org.eclipse.riena.ui.ridgets.uibinding.IControlRidgetMapper;
 import org.eclipse.riena.ui.swt.uiprocess.UIProcessControl;
 import org.eclipse.riena.ui.swt.utils.SWTBindingPropertyLocator;
 
 /**
  * View of a sub-application.
  */
 public class SubApplicationView implements INavigationNodeView<SubApplicationNode>, IPerspectiveFactory {
 
 	private static final Logger LOGGER = Log4r.getLogger(Activator.getDefault(), SubApplicationView.class);
 
 	private final AbstractViewBindingDelegate binding;
 	private SubApplicationController subApplicationController;
 	private SubApplicationListener subApplicationListener;
 	private SubApplicationNode subApplicationNode;
 	private final List<Object> uiControls;
 
 	private static IBindingManager menuItemBindingManager;
 
 	private static int itemId = 0;
 
 	/**
 	 * Creates a new instance of {@code SubApplicationView}.
 	 */
 	public SubApplicationView() {
 		binding = createBinding();
 		uiControls = new ArrayList<Object>();
 		if (menuItemBindingManager == null) {
 			menuItemBindingManager = createMenuItemBindingManager(SWTBindingPropertyLocator.getInstance(),
 					SwtControlRidgetMapper.getInstance());
 		}
 	}
 
 	public void addUpdateListener(final IComponentUpdateListener listener) {
 		throw new UnsupportedOperationException();
 
 	}
 
 	/**
 	 * Binds the navigation node to the view. Creates the widgets and the
 	 * controller if necessary.<br>
 	 * Also the menus and the tool bar items are binded.
 	 * 
 	 * @see org.eclipse.riena.navigation.ui.swt.views.INavigationNodeView#bind(org.eclipse.riena.navigation.INavigationNode)
 	 */
 	public void bind(final SubApplicationNode node) {
 		if (getNavigationNode().getNavigationNodeController() instanceof IController) {
 			final IController controller = (IController) node.getNavigationNodeController();
 			binding.injectRidgets(controller);
 			binding.bind(controller);
 			bindMenuAndToolItems(controller);
 			controller.afterBind();
 		}
 
 		subApplicationListener = new SubApplicationListener();
 		getNavigationNode().addListener(subApplicationListener);
 	}
 
 	public void createInitialLayout(final IPageLayout layout) {
 		addUIControls();
 		subApplicationNode = (SubApplicationNode) locateSubApplication(layout.getDescriptor().getId());
 		subApplicationController = createController(subApplicationNode);
 		initializeListener(subApplicationController);
 		bind(subApplicationNode);
 		subApplicationController.afterBind();
 		doBaseLayout(layout);
 	}
 
 	public SubApplicationNode getNavigationNode() {
 		return subApplicationNode;
 	}
 
 	public void unbind() {
 		if (getNavigationNode() != null) {
 
 			getNavigationNode().removeListener(subApplicationListener);
 
 			if (getNavigationNode().getNavigationNodeController() instanceof IController) {
 				final IController controller = (IController) getNavigationNode().getNavigationNodeController();
 				binding.unbind(controller);
 				if (menuItemBindingManager != null) {
 					menuItemBindingManager.unbind(controller, getUIControls());
 				}
 			}
 		}
 	}
 
 	protected AbstractViewBindingDelegate createBinding() {
 		final DelegatingRidgetMapper ridgetMapper = new DelegatingRidgetMapper(SwtControlRidgetMapper.getInstance());
 		addMappings(ridgetMapper);
 		return new InjectSwtViewBindingDelegate(ridgetMapper);
 	}
 
 	/**
 	 * Creates controller of the sub-application view and create and set the
 	 * some ridgets.
 	 * 
 	 * @param subApplication
 	 *            sub-application node
 	 * @return controller of the sub-application view
 	 */
 	protected SubApplicationController createController(final ISubApplicationNode subApplication) {
 		return new SubApplicationController(subApplication);
 	}
 
 	protected void doBaseLayout(final IPageLayout layout) {
 		layout.setEditorAreaVisible(false);
 		layout.setFixed(true);
 	}
 
 	// helping methods
 	//////////////////
 
 	private void addMappings(final DelegatingRidgetMapper ridgetMapper) {
 		ridgetMapper.addMapping(UIProcessControl.class, UIProcessRidget.class);
 	}
 
 	private void addUIControls() {
 		initUIProcessRidget();
 	}
 
 	private void bindMenuAndToolItems(final IController controller) {
 		createRidgets(controller);
 		menuItemBindingManager.bind(controller, getUIControls());
 	}
 
 	private IBindingManager createMenuItemBindingManager(final IBindingPropertyLocator propertyStrategy,
 			final IControlRidgetMapper<Object> mapper) {
 		return new DefaultBindingManager(propertyStrategy, mapper);
 	}
 
 	/**
 	 * Creates for the given item a ridget and adds it to the given controller.
 	 * 
 	 * @param controller
 	 * @param item
 	 */
 	private void createRidget(final IController controller, final Item item) {
 		if (isSeparator(item)) {
 			// no ridget for separator
 			// and
 			// no ridget for tool items with control 
 			// (both tool items has the style SWT.SEPARATOR)
 			return;
 		}
 
 		String id;
 		if (item instanceof MenuItem) {
 			id = getItemId((MenuItem) item);
 		} else {
 			id = getItemId((ToolItem) item);
 		}
 		if (StringUtils.isEmpty(id)) {
 			return;
 		}
 
 		final IRidget ridget = menuItemBindingManager.createRidget(item);
 		ridget.setUIControl(item);
 		SWTBindingPropertyLocator.getInstance().setBindingProperty(item, id);
 		getUIControls().add(item);
 		controller.addRidget(id, ridget);
 
 		if (item instanceof MenuItem) {
 			final MenuItem menuItem = (MenuItem) item;
 			createRidget(controller, menuItem.getMenu());
 		}
 	}
 
 	private void createRidget(final IController controller, final Menu menu) {
 		if (menu == null) {
 			return;
 		}
 
 		final MenuItem[] items = menu.getItems();
 		for (final MenuItem item : items) {
 			createRidget(controller, item);
 		}
 	}
 
 	/**
 	 * Creates for every menu item and tool item a ridget and adds
 	 * 
 	 * @param controller
 	 */
 	private void createRidgets(final IController controller) {
 		// items of Riena "menu bar"
 		final List<MenuCoolBarComposite> menuCoolBarComposites = getMenuCoolBarComposites(getShell());
 		for (final MenuCoolBarComposite menuBarComp : menuCoolBarComposites) {
 
 			final List<ToolItem> toolItems = menuBarComp.getTopLevelItems();
 			for (final ToolItem toolItem : toolItems) {
 				createRidget(controller, toolItem);
 				if (toolItem.getData() instanceof MenuManager) {
 					final MenuManager manager = (MenuManager) toolItem.getData();
 					createRidget(controller, manager.getMenu());
 				}
 			}
 		}
 
 		// items of cool bar
 		final List<ToolItem> toolItems = getAllToolItems();
 		for (final ToolItem toolItem : toolItems) {
 			createRidget(controller, toolItem);
 		}
 	}
 
 	/**
 	 * Returns all items of all cool bars.
 	 * 
 	 * @return list of tool items
 	 */
 	private List<ToolItem> getAllToolItems() {
 		final List<ToolItem> items = new ArrayList<ToolItem>();
 
 		final List<CoolBar> coolBars = getCoolBars(getShell());
 		for (final CoolBar coolBar : coolBars) {
 			final List<ToolBar> toolBars = getToolBars(coolBar);
 			for (final ToolBar toolBar : toolBars) {
 				items.addAll(Arrays.asList(toolBar.getItems()));
 			}
 		}
 
 		return items;
 	}
 
 	/**
 	 * Returns all cool bars below the given composite (except cool bar of
 	 * menu).
 	 * 
 	 * @param composite
 	 * @return list of cool bars
 	 */
 	private List<CoolBar> getCoolBars(final Composite composite) {
 		final List<CoolBar> coolBars = new ArrayList<CoolBar>();
 		if (composite == null) {
 			return coolBars;
 		}
 
 		final Control[] children = composite.getChildren();
 		for (final Control child : children) {
 			if (child instanceof CoolBar) {
 				if (getParentOfType(child, MenuCoolBarComposite.class) == null) {
 					coolBars.add((CoolBar) child);
 				}
 				continue;
 			}
 			if (child instanceof Composite) {
 				coolBars.addAll(getCoolBars((Composite) child));
 			}
 		}
 
 		return coolBars;
 	}
 
 	/**
 	 * Returns the identifier of this contribution item.
 	 * 
 	 * @param item
 	 * @return identifier, or {@code null} if none
 	 */
 	private String getItemId(final Item item, final String prefix) {
 		String id = null;
 		if (item.getData() instanceof IContributionItem) {
 			final IContributionItem contributionItem = (IContributionItem) item.getData();
 			id = contributionItem.getId();
 		}
 		if (StringUtils.isEmpty(id)) {
 			id = SWTBindingPropertyLocator.getInstance().locateBindingProperty(item);
 		}
 		if (StringUtils.isEmpty(id)) {
 			id = Integer.toString(++itemId);
 		} else {
 			if (!id.startsWith(prefix)) {
 				id = prefix + id;
 			}
 		}
 		return id;
 	}
 
 	/**
 	 * Returns the identifier of the given menu item.
 	 * 
 	 * @param item
 	 *            menu item
 	 * @return identifier, or {@code null} if none
 	 */
 	private String getItemId(final MenuItem item) {
 		return getItemId(item, IActionRidget.BASE_ID_MENUACTION);
 	}
 
 	/**
 	 * Returns the identifier of the given tool item.
 	 * 
 	 * @param item
 	 *            tool item
 	 * @return identifier, or {@code null} if none
 	 */
 	private String getItemId(final ToolItem item) {
 		return getItemId(item, IActionRidget.BASE_ID_TOOLBARACTION);
 	}
 
 	/**
 	 * Returns the composites that contains the menu bar of the sub-application.
 	 * 
 	 * @param composite
 	 * @return composite with menu bar
 	 */
 	private List<MenuCoolBarComposite> getMenuCoolBarComposites(final Composite composite) {
 		final List<MenuCoolBarComposite> composites = new ArrayList<MenuCoolBarComposite>();
 
 		final Control[] children = composite.getChildren();
 		for (final Control child : children) {
 			if (child instanceof MenuCoolBarComposite) {
 				composites.add((MenuCoolBarComposite) child);
 				continue;
 			}
 			if (child instanceof Composite) {
 				composites.addAll(getMenuCoolBarComposites((Composite) child));
 			}
 		}
 
 		return composites;
 	}
 
 	private Composite getParentOfType(final Control control, final Class<? extends Control> clazz) {
 		final Composite parent = control.getParent();
 		if (parent == null) {
 			return null;
 		}
 		if (clazz.isAssignableFrom(parent.getClass())) {
 			return parent;
 		}
 		return getParentOfType(parent, clazz);
 	}
 
 	/**
 	 * Returns the shell of the application.
 	 * 
 	 * @return application shell
 	 */
 	private Shell getShell() {
 		final SWTBindingPropertyLocator locator = SWTBindingPropertyLocator.getInstance();
 		final Shell[] shells = Display.getDefault().getShells();
 		for (final Shell shell : shells) {
 			final String value = locator.locateBindingProperty(shell);
 			if ((value != null) && value.equals(ApplicationViewAdvisor.SHELL_RIDGET_PROPERTY)) {
 				return shell;
 			}
 		}
 		if (PlatformUI.isWorkbenchRunning()) {
 			return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
 		}
 
 		return Display.getDefault().getActiveShell();
 	}
 
 	/**
 	 * Returns all tool bars of the given cool bar.
 	 * 
 	 * @param coolBar
 	 *            cool bar
 	 * @return list of tool bars
 	 */
 	private List<ToolBar> getToolBars(final CoolBar coolBar) {
 		final List<ToolBar> toolBars = new ArrayList<ToolBar>();
 		if (coolBar == null) {
 			return toolBars;
 		}
 
 		final Control[] children = coolBar.getChildren();
 		for (final Control child : children) {
 			if (child instanceof ToolBar) {
 				if (getParentOfType(child, MenuCoolBarComposite.class) == null) {
 					toolBars.add((ToolBar) child);
 				}
 			}
 		}
 
 		return toolBars;
 	}
 
 	private List<Object> getUIControls() {
 		return uiControls;
 	}
 
 	/**
 	 * Adds a listener for all sub-module nodes of the sub-application.
 	 * 
 	 * @param controller
 	 *            controller of the sub-application
 	 */
 	private void initializeListener(final SubApplicationController controller) {
 		final NavigationTreeObserver navigationTreeObserver = new NavigationTreeObserver();
 		navigationTreeObserver.addListener(new MySubModuleNodeListener());
 
 		navigationTreeObserver.addListenerTo(controller.getNavigationNode());
 	}
 
 	private void initUIProcessRidget() {
 		final UIProcessControl uiControl = new UIProcessControl(getShell());
 		uiControl.setPropertyName("uiProcessRidget"); //$NON-NLS-1$
 		binding.addUIControl(uiControl);
 	}
 
 	private boolean isSeparator(final Item item) {
 		return (item.getStyle() & SWT.SEPARATOR) == SWT.SEPARATOR;
 	}
 
 	private ISubApplicationNode locateSubApplication(final String id) {
 		return SwtViewProvider.getInstance().getNavigationNode(id, ISubApplicationNode.class);
 	}
 
 	// helping classes
 	//////////////////
 
 	private class SubApplicationListener extends SubApplicationNodeListener {
 		@Override
 		public void block(final ISubApplicationNode source, final boolean block) {
 			super.block(source, block);
 			for (final IModuleGroupNode group : source.getChildren()) {
 				for (final IModuleNode module : group.getChildren()) {
 					module.setBlocked(block);
 				}
 			}
 		}
 
 		@Override
 		public void disposed(final ISubApplicationNode source) {
 			unbind();
 		}
 	}
 
 	/**
 	 * After a sub-module node was activated, the corresponding view is shown.
 	 */
 	private static class MySubModuleNodeListener extends SubModuleNodeListener {
 		private boolean navigationUp = false;
 
 		@Override
 		public void prepared(final ISubModuleNode source) {
 			checkBaseStructure();
 
 			final SwtViewId id = getViewId(source);
 			prepareView(id);
 		}
 
 		@Override
 		public void activated(final ISubModuleNode source) {
 			checkBaseStructure();
 
 			if (null != source && !source.isSelectable()) {
 				return;
 			}
 
 			final SwtViewId id = getViewId(source);
 			prepareView(id);
 			showView(id);
 
 		}
 
 		@Override
 		public void disposed(final ISubModuleNode source) {
 			try {
 				final SwtViewId id = getViewId(source);
 				hideView(id);
 				final SwtViewProvider viewProvider = SwtViewProvider.getInstance();
 				viewProvider.unregisterSwtViewId(source);
 			} catch (final ApplicationModelFailure amf) {
				// not selectable SubModules dont't have an associated view, so if hidding creates an exception we can ignore it TODO : never close a not selectable node
 				if (source.isSelectable()) {
 					LOGGER.log(LogService.LOG_ERROR, "Error disposing node " + source + ": " + amf.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
 				}
 			}
 		}
 
 		protected String createNextId() {
 			return String.valueOf(System.currentTimeMillis());
 		}
 
 		/**
 		 * At the very first time (a sub-module was activated), the view parts
 		 * of the sub-application switcher and the navigation tree are shown.
 		 */
 		private void checkBaseStructure() {
 			if (!navigationUp) {
 				createNavigation();
 				navigationUp = true;
 			}
 		}
 
 		private void createNavigation() {
 			final String secId = createNextId();
 			prepareView(NavigationViewPart.ID, secId);
 			showView(NavigationViewPart.ID, secId);
 		}
 
 		/**
 		 * Returns the currently active page.
 		 * 
 		 * @return active page
 		 */
 		private IWorkbenchPage getActivePage() {
 			return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
 		}
 
 		/**
 		 * Returns the view ID of the given sub-module node.
 		 * 
 		 * @param source
 		 *            sub-module node
 		 * @return view ID
 		 */
 		private SwtViewId getViewId(final ISubModuleNode node) {
 			return SwtViewProvider.getInstance().getSwtViewId(node);
 		}
 
 		/**
 		 * Hides the view in the active page.
 		 * 
 		 * @param id
 		 *            the id of the view extension to use
 		 * @param secondaryId
 		 *            the secondary id to use
 		 */
 		private void hideView(final String id, final String secondary) {
 			final IViewReference viewRef = getActivePage().findViewReference(id, secondary);
 			if (viewRef != null) {
 				final IViewPart view = viewRef.getView(false);
 				if (view instanceof INavigationNodeView<?>) {
 					((INavigationNodeView<?>) view).unbind();
 				}
 				getActivePage().hideView(view);
 			}
 		}
 
 		private void hideView(final SwtViewId id) {
 			hideView(id.getId(), id.getSecondary());
 		}
 
 		private void showView(final SwtViewId id) {
 			showView(id.getId(), id.getSecondary());
 		}
 
 		/**
 		 * Shows a view in the active page.
 		 * 
 		 * @param id
 		 *            the id of the view extension to use
 		 * @param secondaryId
 		 *            the secondary id to use
 		 */
 		private void showView(final String id, final String secondary) {
 			final IWorkbenchPage page = getActivePage();
 			final IViewReference viewRef = page.findViewReference(id, secondary);
 			if (viewRef != null) {
 				((WorkbenchPage) page).getActivePerspective().bringToTop(viewRef);
 			}
 		}
 
 		private IViewReference prepareView(final SwtViewId id) {
 			return prepareView(id.getId(), id.getSecondary());
 		}
 
 		/**
 		 * Prepares a view so that is can be shown.
 		 * 
 		 * @param id
 		 *            the id of the view extension to use
 		 * @param secondary
 		 *            the secondary id to use
 		 * @return the view reference, or <code>null</code> if none is found
 		 */
 		private IViewReference prepareView(final String id, final String secondary) {
 
 			try {
 				final IWorkbenchPage page = getActivePage();
 				// open view but don't activate it and don't bring it to top
 				page.showView(id, secondary, IWorkbenchPage.VIEW_VISIBLE);
 				return page.findViewReference(id, secondary);
 			} catch (final PartInitException exc) {
 				final String msg = String.format("Failed to prepare/show view: %s, %s", id, secondary); //$NON-NLS-1$
 				LOGGER.log(0, msg, exc);
 			}
 
 			return null;
 
 		}
 
 		//		private boolean isViewOfActiveNode(IViewReference viewRef) {
 		//			IViewPart view = viewRef.getView(false);
 		//
 		//			if (view instanceof INavigationNodeView<?, ?>) {
 		//				INavigationNode<?> navigationNode = ((INavigationNodeView<?, ?>) view).getNavigationNode();
 		//				return navigationNode.isActivated();
 		//			} else {
 		//				return true;
 		//			}
 		//
 		//		}
 
 	}
 
 }
