 /*******************************************************************************
  * Copyright (c) 2007, 2008 compeople AG and others.
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
 
 import org.eclipse.jface.action.IContributionItem;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.riena.core.util.StringUtils;
 import org.eclipse.riena.internal.ui.ridgets.swt.uiprocess.UIProcessRidget;
 import org.eclipse.riena.navigation.ISubApplicationNode;
 import org.eclipse.riena.navigation.ISubModuleNode;
 import org.eclipse.riena.navigation.listener.NavigationTreeObserver;
 import org.eclipse.riena.navigation.listener.SubModuleNodeListener;
 import org.eclipse.riena.navigation.model.SubApplicationNode;
 import org.eclipse.riena.navigation.ui.controllers.SubApplicationController;
 import org.eclipse.riena.navigation.ui.swt.binding.DelegatingRidgetMapper;
 import org.eclipse.riena.navigation.ui.swt.binding.InjectSwtViewBindingDelegate;
 import org.eclipse.riena.navigation.ui.swt.component.MenuCoolBarComposite;
 import org.eclipse.riena.navigation.ui.swt.presentation.SwtViewId;
 import org.eclipse.riena.navigation.ui.swt.presentation.SwtViewProviderAccessor;
 import org.eclipse.riena.ui.ridgets.IActionRidget;
 import org.eclipse.riena.ui.ridgets.IRidget;
 import org.eclipse.riena.ui.ridgets.controller.IController;
 import org.eclipse.riena.ui.ridgets.swt.uibinding.AbstractViewBindingDelegate;
 import org.eclipse.riena.ui.ridgets.swt.uibinding.DefaultSwtControlRidgetMapper;
 import org.eclipse.riena.ui.ridgets.uibinding.DefaultBindingManager;
 import org.eclipse.riena.ui.ridgets.uibinding.IBindingManager;
 import org.eclipse.riena.ui.ridgets.uibinding.IBindingPropertyLocator;
 import org.eclipse.riena.ui.ridgets.uibinding.IControlRidgetMapper;
 import org.eclipse.riena.ui.swt.uiprocess.UIProcessControl;
 import org.eclipse.riena.ui.swt.utils.SWTBindingPropertyLocator;
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
 
 /**
  * View of a sub-application.
  */
 public class SubApplicationView implements INavigationNodeView<SubApplicationController, SubApplicationNode>,
 		IPerspectiveFactory {
 
 	private AbstractViewBindingDelegate binding;
 	private SubApplicationController subApplicationController;
 	private SubApplicationNode subApplicationNode;
 	private List<Object> uiControls;
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
 					new DefaultSwtControlRidgetMapper());
 		}
 	}
 
 	private IBindingManager createMenuItemBindingManager(IBindingPropertyLocator propertyStrategy,
 			IControlRidgetMapper<Object> mapper) {
 		return new DefaultBindingManager(propertyStrategy, mapper);
 	}
 
 	protected AbstractViewBindingDelegate createBinding() {
 		DelegatingRidgetMapper ridgetMapper = new DelegatingRidgetMapper(new DefaultSwtControlRidgetMapper());
 		addMappings(ridgetMapper);
 		return new InjectSwtViewBindingDelegate(ridgetMapper);
 	}
 
 	private void addMappings(DelegatingRidgetMapper ridgetMapper) {
 		ridgetMapper.addMapping(UIProcessControl.class, UIProcessRidget.class);
 	}
 
 	/**
 	 * Binds the navigation node to the view. Creates the widgets and the
 	 * controller if necessary.<br>
 	 * Also the menus and the tool bar items are binded.
 	 * 
 	 * @see org.eclipse.riena.navigation.ui.swt.views.INavigationNodeView#bind(org.eclipse.riena.navigation.INavigationNode)
 	 */
 	public void bind(SubApplicationNode node) {
 		if (getNavigationNode().getNavigationNodeController() instanceof IController) {
 			IController controller = (IController) node.getNavigationNodeController();
 			binding.injectRidgets(controller);
 			binding.bind(controller);
 			bindMenuAndToolItems(controller);
 			controller.afterBind();
 		}
 	}
 
 	private void bindMenuAndToolItems(IController controller) {
 		createRidgets(controller);
 		menuItemBindingManager.bind(controller, getUIControls());
 	}
 
 	private boolean isSeparator(Item item) {
 		return (item.getStyle() & SWT.SEPARATOR) == SWT.SEPARATOR;
 	}
 
 	/**
 	 * Creates for the given item a ridget and adds it to the given controller.
 	 * 
 	 * @param controller
 	 * @param item
 	 */
 	private void createRidget(IController controller, Item item) {
 
 		if (isSeparator(item)) {
 			// no ridget for separator
 			// and
 			// no ridget for tool items with control 
 			// (both tool items has the style SWT.SEPARATOR)
 			return;
 		}
 
 		String itemId;
 		if (item instanceof MenuItem) {
 			itemId = getItemId((MenuItem) item);
 		} else {
 			itemId = getItemId((ToolItem) item);
 		}
 		if (StringUtils.isEmpty(itemId)) {
 			return;
 		}
 
 		IRidget ridget = menuItemBindingManager.createRidget(item);
 		ridget.setUIControl(item);
 		SWTBindingPropertyLocator.getInstance().setBindingProperty(item, itemId);
 		getUIControls().add(item);
 		controller.addRidget(itemId, ridget);
 
 		if (item instanceof MenuItem) {
 			MenuItem menuItem = (MenuItem) item;
 			createRidget(controller, menuItem.getMenu());
 		}
 
 	}
 
 	private void createRidget(IController controller, Menu menu) {
 
 		if (menu == null) {
 			return;
 		}
 
 		MenuItem[] items = menu.getItems();
 		for (MenuItem item : items) {
 			createRidget(controller, item);
 		}
 
 	}
 
 	/**
 	 * Returns the identifier of the given menu item.
 	 * 
 	 * @param item
 	 *            - menu item
 	 * @return identifier, or {@code null} if none
 	 */
 	private String getItemId(MenuItem item) {
 		return getItemId(item, IActionRidget.BASE_ID_MENUACTION);
 	}
 
 	/**
 	 * Returns the identifier of the given tool item.
 	 * 
 	 * @param item
 	 *            - tool item
 	 * @return identifier, or {@code null} if none
 	 */
 	private String getItemId(ToolItem item) {
 		return getItemId(item, IActionRidget.BASE_ID_TOOLBARACTION);
 	}
 
 	/**
 	 * Returns the identifier of this contribution item.
 	 * 
 	 * @param item
 	 * @return identifier, or {@code null} if none
 	 */
 	private String getItemId(Item item, String prefix) {
 		String id = null;
 		if (item.getData() instanceof IContributionItem) {
 			IContributionItem contributionItem = (IContributionItem) item.getData();
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
 
 	public void unbind() {
 		if (getNavigationNode() != null && getNavigationNode().getNavigationNodeController() instanceof IController) {
 			IController controller = (IController) getNavigationNode().getNavigationNodeController();
 			binding.unbind(controller);
 			if (menuItemBindingManager != null) {
 				menuItemBindingManager.unbind(controller, getUIControls());
 			}
 		}
 	}
 
 	/**
 	 * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
 	 */
 	public void createInitialLayout(IPageLayout layout) {
 		addUIControls();
 		subApplicationNode = (SubApplicationNode) locateSubApplication(layout.getDescriptor().getId());
 		subApplicationController = createController(subApplicationNode);
 		initializeListener(subApplicationController);
 		bind(subApplicationNode);
 		subApplicationController.afterBind();
 		doBaseLayout(layout);
 	}
 
 	private void addUIControls() {
 		initUIProcessRidget();
 	}
 
 	private ISubApplicationNode locateSubApplication(String id) {
 		return SwtViewProviderAccessor.getViewProvider().getNavigationNode(id, ISubApplicationNode.class);
 	}
 
 	/**
 	 * Creates controller of the sub-application view and create and set the
 	 * some ridgets.
 	 * 
 	 * @param subApplication
 	 *            - sub-application node
 	 * @return controller of the sub-application view
 	 */
 	protected SubApplicationController createController(ISubApplicationNode subApplication) {
 		return new SubApplicationController(subApplication);
 
 	}
 
 	/**
 	 * Adds a listener for all sub-module nodes of the sub-application.
 	 * 
 	 * @param controller
 	 *            - controller of the sub-application
 	 */
 	private void initializeListener(SubApplicationController controller) {
 		NavigationTreeObserver navigationTreeObserver = new NavigationTreeObserver();
 		navigationTreeObserver.addListener(new MySubModuleNodeListener());
 
 		navigationTreeObserver.addListenerTo(controller.getNavigationNode());
 	}
 
 	protected void doBaseLayout(IPageLayout layout) {
 		layout.setEditorAreaVisible(false);
 		layout.setFixed(true);
 	}
 
 	private void initUIProcessRidget() {
 		UIProcessControl uiControl = new UIProcessControl(getShell());
 		uiControl.setPropertyName("uiProcessRidget"); //$NON-NLS-1$
 		binding.addUIControl(uiControl);
 	}
 
 	/**
 	 * Returns the shell of the application.
 	 * 
 	 * @return application shell
 	 */
 	private Shell getShell() {
 
 		SWTBindingPropertyLocator locator = SWTBindingPropertyLocator.getInstance();
 		Shell[] shells = Display.getDefault().getShells();
 		for (Shell shell : shells) {
 			String value = locator.locateBindingProperty(shell);
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
 	 * After a sub-module node was activated, the corresponding view is shown.
 	 */
 	private static class MySubModuleNodeListener extends SubModuleNodeListener {
 
 		private boolean navigationUp = false;
 
 		/**
 		 * @see org.eclipse.riena.navigation.listener.NavigationNodeListener#activated(org.eclipse.riena.navigation.INavigationNode)
 		 */
 		@Override
 		public void activated(ISubModuleNode source) {
 			checkBaseStructure();
 			SwtViewId id = getViewId(source);
 			showMultiView(id);
 		}
 
 		/**
 		 * @see org.eclipse.riena.navigation.listener.NavigationNodeListener#disposed(org.eclipse.riena.navigation.INavigationNode)
 		 */
 		@Override
 		public void disposed(ISubModuleNode source) {
 			SwtViewId id = getViewId(source);
 			hideView(id);
 		}
 
 		/**
 		 * Returns the view ID of the given sub-module node.
 		 * 
 		 * @param source
 		 *            - sub-module node
 		 * @return view ID
 		 */
 		private SwtViewId getViewId(ISubModuleNode node) {
 			return SwtViewProviderAccessor.getViewProvider().getSwtViewId(node);
 		}
 
 		/**
 		 * At the very first time (a sub-module was activated), the view parts
 		 * of the sub-application switcher and the navigation tree are shown.
 		 */
 		private void checkBaseStructure() {
 			if (!navigationUp) {
 				createNavigation();
 				createStatusLine();
 				navigationUp = true;
 			}
 		}
 
 		protected String createNextId() {
 			return String.valueOf(System.currentTimeMillis());
 		}
 
 		private void createNavigation() {
 			showView(NavigationViewPart.ID, createNextId());
 		}
 
 		private void createStatusLine() {
 			//			showView(StatusLineViewPart.ID, createNextId());
 		}
 
 		private void showMultiView(SwtViewId id) {
 			showView(id.getId(), id.getSecondary());
 		}
 
 		private void hideView(SwtViewId id) {
 			hideView(id.getId(), id.getSecondary());
 		}
 
 		/**
 		 * Shows a view in the active page.
 		 * 
 		 * @param id
 		 *            - the id of the view extension to use
 		 * @param secondaryId
 		 *            - the secondary id to use
 		 */
 		private void showView(String id, String secondary) {
 			try {
 				getActivePage().showView(id, secondary, IWorkbenchPage.VIEW_ACTIVATE);
 			} catch (PartInitException e) {
 				e.printStackTrace();
 			}
 		}
 
 		/**
 		 * Hides the view in the active page.
 		 * 
 		 * @param id
 		 *            - the id of the view extension to use
 		 * @param secondaryId
 		 *            - the secondary id to use
 		 */
 		private void hideView(String id, String secondary) {
 			IViewReference viewRef = getActivePage().findViewReference(id, secondary);
 			if (viewRef != null) {
 				IViewPart view = viewRef.getView(false);
 				if (view instanceof INavigationNodeView) {
 					((INavigationNodeView<?, ?>) view).unbind();
 				}
 				getActivePage().hideView(view);
 			}
 		}
 
 		/**
 		 * Returns the currently active page.
 		 * 
 		 * @return active page
 		 */
 		private IWorkbenchPage getActivePage() {
 			return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
 		}
 
 	}
 
 	public void addUpdateListener(IComponentUpdateListener listener) {
		// TODO Auto-generated method stub
 
 	}
 
	public int calculateBounds(int positionHint) {
		// TODO Auto-generated method stub
		return 0;
	}

 	public SubApplicationNode getNavigationNode() {
 		return subApplicationNode;
 	}
 
 	/**
 	 * Returns all cool bars below the given composite (except cool bar of
 	 * menu).
 	 * 
 	 * @param composite
 	 * @return list of cool bars
 	 */
 	private List<CoolBar> getCoolBars(Composite composite) {
 
 		List<CoolBar> coolBars = new ArrayList<CoolBar>();
 		if (composite == null) {
 			return coolBars;
 		}
 
 		Control[] children = composite.getChildren();
 		for (Control child : children) {
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
 	 * Returns all tool bars of the given cool bar.
 	 * 
 	 * @param coolBar
 	 *            - cool bar
 	 * @return list of tool bars
 	 */
 	private List<ToolBar> getToolBars(CoolBar coolBar) {
 
 		List<ToolBar> toolBars = new ArrayList<ToolBar>();
 		if (coolBar == null) {
 			return toolBars;
 		}
 
 		Control[] children = coolBar.getChildren();
 		for (Control child : children) {
 			if (child instanceof ToolBar) {
 				if (getParentOfType(child, MenuCoolBarComposite.class) == null) {
 					toolBars.add((ToolBar) child);
 				}
 			}
 		}
 
 		return toolBars;
 
 	}
 
 	/**
 	 * Returns all items of all cool bars.
 	 * 
 	 * @return list of tool items
 	 */
 	private List<ToolItem> getAllToolItems() {
 
 		List<ToolItem> items = new ArrayList<ToolItem>();
 
 		List<CoolBar> coolBars = getCoolBars(getShell());
 		for (CoolBar coolBar : coolBars) {
 			List<ToolBar> toolBars = getToolBars(coolBar);
 			for (ToolBar toolBar : toolBars) {
 				items.addAll(Arrays.asList(toolBar.getItems()));
 			}
 		}
 
 		return items;
 
 	}
 
 	private Composite getParentOfType(Control control, Class<? extends Control> clazz) {
 
 		Composite parent = control.getParent();
 		if (parent == null) {
 			return null;
 		}
 		if (clazz.isAssignableFrom(parent.getClass())) {
 			return parent;
 		}
 		return getParentOfType(parent, clazz);
 
 	}
 
 	/**
 	 * Returns the composites that contains the menu bar of the sub-application.
 	 * 
 	 * @param composite
 	 * @return composite with menu bar
 	 */
 	private List<MenuCoolBarComposite> getMenuCoolBarComposites(Composite composite) {
 
 		List<MenuCoolBarComposite> composites = new ArrayList<MenuCoolBarComposite>();
 
 		Control[] children = composite.getChildren();
 		for (Control child : children) {
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
 
 	/**
 	 * Creates for every menu item and tool item a ridget and adds
 	 * 
 	 * @param controller
 	 */
 	private void createRidgets(IController controller) {
 
 		// items of Riena "menu bar"
 		List<MenuCoolBarComposite> menuCoolBarComposites = getMenuCoolBarComposites(getShell());
 		for (MenuCoolBarComposite menuBarComp : menuCoolBarComposites) {
 
 			List<ToolItem> toolItems = menuBarComp.getTopLevelItems();
 			for (ToolItem toolItem : toolItems) {
 				createRidget(controller, toolItem);
 				if (toolItem.getData() instanceof MenuManager) {
 					MenuManager manager = (MenuManager) toolItem.getData();
 					createRidget(controller, manager.getMenu());
 				}
 			}
 
 		}
 
 		// items of cool bar
 		List<ToolItem> toolItems = getAllToolItems();
 		for (ToolItem toolItem : toolItems) {
 			createRidget(controller, toolItem);
 		}
 
 	}
 
 	private List<Object> getUIControls() {
 		return uiControls;
 	}
 
 }
