 /*******************************************************************************
  * Copyright (c) 2007 compeople AG and others.
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
 import java.util.List;
 
 import org.eclipse.jface.action.IContributionItem;
 import org.eclipse.jface.action.IStatusLineManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.StatusLineManager;
 import org.eclipse.riena.navigation.ISubApplication;
 import org.eclipse.riena.navigation.ISubApplicationListener;
 import org.eclipse.riena.navigation.model.ApplicationModel;
 import org.eclipse.riena.navigation.model.NavigationTreeObserver;
 import org.eclipse.riena.navigation.model.SubApplicationAdapter;
 import org.eclipse.riena.navigation.ui.controllers.ApplicationViewController;
 import org.eclipse.riena.navigation.ui.swt.binding.DefaultSwtControlRidgetMapper;
 import org.eclipse.riena.navigation.ui.swt.lnf.renderer.ShellBorderRenderer;
 import org.eclipse.riena.navigation.ui.swt.lnf.renderer.ShellLogoRenderer;
 import org.eclipse.riena.navigation.ui.swt.lnf.renderer.ShellRenderer;
 import org.eclipse.riena.navigation.ui.swt.presentation.SwtPresentationManagerAccessor;
 import org.eclipse.riena.ui.ridgets.uibinding.DefaultBindingManager;
 import org.eclipse.riena.ui.swt.lnf.ILnfKeyConstants;
 import org.eclipse.riena.ui.swt.lnf.ILnfRenderer;
 import org.eclipse.riena.ui.swt.lnf.LnfManager;
 import org.eclipse.riena.ui.swt.utils.ImageUtil;
 import org.eclipse.riena.ui.swt.utils.SWTBindingPropertyLocator;
 import org.eclipse.riena.ui.swt.utils.SwtUtilities;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ControlEvent;
 import org.eclipse.swt.events.ControlListener;
 import org.eclipse.swt.events.MenuEvent;
 import org.eclipse.swt.events.MenuListener;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.MouseMoveListener;
 import org.eclipse.swt.events.MouseTrackListener;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.events.ShellEvent;
 import org.eclipse.swt.events.ShellListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Cursor;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.ImageData;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.CoolBar;
 import org.eclipse.swt.widgets.CoolItem;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.ToolBar;
 import org.eclipse.swt.widgets.ToolItem;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.WorkbenchException;
 import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
 import org.eclipse.ui.application.WorkbenchWindowAdvisor;
 import org.eclipse.ui.internal.WorkbenchWindow;
 
 public class ApplicationViewAdvisor extends WorkbenchWindowAdvisor {
 
 	/**
 	 * The default and the minimum size of the application.
 	 */
 	private static final Point APPLICATION_SIZE = new Point(800, 600);
 	private static final int COOLBAR_HIGHT = 22;
 	private static final int COOLBAR_TOP_MARGIN = 2;
 	private static final int STATUSLINE_HIGHT = 22;
 	private static final String SHELL_RIDGET_PROPERTY = "windowRidget"; //$NON-NLS-1$
 
 	enum BtnState {
 		NONE, HOVER, HOVER_SELECTED;
 	}
 
 	private ApplicationViewController controller;
 	private List<Object> uiControls;
 	private Cursor handCursor;
 	private Cursor grabCursor;
 	private Cursor defaultCursor;
 	private Composite switcherComposite;
 	private CoolBar coolBar;
 	private ToolBar toolBar;
 	private Composite menuBarComposite;
 	private Composite coolBarComposite;
 	private Composite mainComposite;
 
 	public ApplicationViewAdvisor(IWorkbenchWindowConfigurer configurer, ApplicationViewController pController) {
 		super(configurer);
 		uiControls = new ArrayList<Object>();
 		controller = pController;
 		initializeListener();
 	}
 
 	public void addUIControl(Composite control) {
 		uiControls.add(control);
 	}
 
 	private void initializeListener() {
 		ISubApplicationListener subApplicationListener = new SubApplicationListener();
 		NavigationTreeObserver navigationTreeObserver = new NavigationTreeObserver();
 		navigationTreeObserver.addListener(subApplicationListener);
 		navigationTreeObserver.addListenerTo(controller.getNavigationNode());
 	}
 
 	/**
 	 * @see org.eclipse.ui.application.WorkbenchWindowAdvisor#preWindowOpen()
 	 */
 	@Override
 	public void preWindowOpen() {
 		configureWindow();
 	}
 
 	/**
 	 * Configures the window of the application.
 	 */
 	private void configureWindow() {
 
 		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
 		configurer.setTitle(controller.getNavigationNode().getLabel());
 		configurer.setInitialSize(APPLICATION_SIZE);
 		if (LnfManager.getLnf().getBooleanSetting(ILnfKeyConstants.SHELL_HIDE_OS_BORDER)) {
 			// don't show the shell border (with the minimize, maximize and
 			// close buttons) of the operation system
 			configurer.setShellStyle(SWT.NO_TRIM | SWT.DOUBLE_BUFFERED);
 		}
 
 	}
 
 	/**
 	 * @see org.eclipse.ui.application.WorkbenchWindowAdvisor#postWindowOpen()
 	 */
 	@Override
 	public void postWindowOpen() {
 		if (switcherComposite != null) {
 			// Redraw the switcher so that the active tab is displayed correct
 			switcherComposite.setRedraw(false);
 			switcherComposite.setRedraw(true);
 		}
 		super.postWindowOpen();
 		doInitialBinding();
 
 		IStatusLineManager statusline = getWindowConfigurer().getActionBarConfigurer().getStatusLineManager();
 		statusline.setMessage(null, "Very simple status line");
 
 	}
 
 	private void doInitialBinding() {
 		DefaultBindingManager defaultBindingManager = createBindingManager();
 		defaultBindingManager.injectRidgets(controller, uiControls);
 		defaultBindingManager.bind(controller, uiControls);
 		controller.afterBind();
 	}
 
 	/**
 	 * @see org.eclipse.ui.application.WorkbenchWindowAdvisor#dispose()
 	 */
 	@Override
 	public void dispose() {
 
 		super.dispose();
 
 		SwtUtilities.disposeResource(handCursor);
 		SwtUtilities.disposeResource(grabCursor);
 		SwtUtilities.disposeResource(defaultCursor);
 
 		SwtUtilities.disposeWidget(toolBar);
 		SwtUtilities.disposeWidget(coolBar);
 		SwtUtilities.disposeWidget(switcherComposite);
 		SwtUtilities.disposeWidget(menuBarComposite);
 		SwtUtilities.disposeWidget(coolBarComposite);
 		SwtUtilities.disposeWidget(mainComposite);
 
 	}
 
 	/**
 	 * @see org.eclipse.ui.application.WorkbenchWindowAdvisor#createWindowContents(org.eclipse.swt.widgets.Shell)
 	 */
 	@Override
 	public void createWindowContents(final Shell shell) {
 
 		initShell(shell);
 
 		// create and layouts the composite of switcher, menu, tool bar etc.
 		shell.setLayout(new FormLayout());
 		createLogoComposite(shell);
 		createGrabCorner(shell);
 		switcherComposite = createSwitcherComposite(shell);
 		menuBarComposite = createMenuBarComposite(shell, switcherComposite);
 		coolBarComposite = createCoolBarComposite(shell, menuBarComposite);
 		mainComposite = createMainComposite(shell, coolBarComposite);
 		// createStatusLineComposite(shell);
 
 	}
 
 	/**
 	 * Initializes the given shell.
 	 * 
 	 * @param shell
 	 *            - shell to initialize
 	 */
 	private void initShell(final Shell shell) {
 
 		// sets the background of the shell
 		Image image = LnfManager.getLnf().getImage(ILnfKeyConstants.TITLELESS_SHELL_BACKGROUND_IMAGE);
 		shell.setBackgroundImage(image);
 		shell.setBackgroundMode(SWT.INHERIT_FORCE);
 
 		shell.setImage(ImageUtil.getImage(controller.getNavigationNode().getIcon()));
 		shell.setMinimumSize(APPLICATION_SIZE);
 
 		// prepare shell for binding
 		shell.setData(SWTBindingPropertyLocator.BINDING_PROPERTY, SHELL_RIDGET_PROPERTY);
 		addUIControl(shell);
 
 		addListeners(shell);
 
 	}
 
 	/**
 	 * Adds all necessary to the given shell.
 	 * 
 	 * @param shell
 	 */
 	private void addListeners(final Shell shell) {
 
 		shell.addPaintListener(new TitlelessPaintListener());
 
 		TitlelessShellListener shellListener = new TitlelessShellListener();
 		shell.addShellListener(shellListener);
 		shell.addControlListener(shellListener);
 
 		TitlelessShellMouseListener mouseListener = new TitlelessShellMouseListener();
 		shell.addMouseListener(mouseListener);
 		shell.addMouseMoveListener(mouseListener);
 		shell.addMouseTrackListener(mouseListener);
 
 	}
 
 	protected DefaultBindingManager createBindingManager() {
 		return new DefaultBindingManager(new SWTBindingPropertyLocator(), new DefaultSwtControlRidgetMapper());
 	}
 
 	/**
 	 * Creates a cursor there for the corresponding image of the look and feel
 	 * is used.
 	 * 
 	 * @param shell
 	 * @param lnfKey
 	 *            - look and feel key of the cursor image
 	 * @return cursor
 	 */
 	private Cursor createCursor(Shell shell, String lnfKey) {
 
 		Cursor cursor = null;
 
 		Image cursorImage = LnfManager.getLnf().getImage(lnfKey);
 		if (cursorImage != null) {
 			ImageData imageData = cursorImage.getImageData();
 			int x = imageData.width / 2;
 			int y = imageData.height / 2;
 			cursor = new Cursor(shell.getDisplay(), imageData, x, y);
 		}
 		return cursor;
 
 	}
 
 	/**
 	 * Sets the hand cursor for the given shell.
 	 * 
 	 * @param shell
 	 */
 	private void showHandCursor(Shell shell) {
 		if (handCursor == null) {
 			handCursor = createCursor(shell, ILnfKeyConstants.TITLELESS_SHELL_HAND_IMAGE);
 		}
 		setCursor(shell, handCursor);
 	}
 
 	/**
 	 * Sets the grab cursor for the given shell.
 	 * 
 	 * @param shell
 	 */
 	private void showGrabCursor(Shell shell) {
 		if (grabCursor == null) {
 			grabCursor = createCursor(shell, ILnfKeyConstants.TITLELESS_SHELL_GRAB_IMAGE);
 		}
 		setCursor(shell, grabCursor);
 
 	}
 
 	/**
 	 * Sets the default cursor for the given shell.
 	 * 
 	 * @param shell
 	 */
 	private void showDefaultCursor(Shell shell) {
 		if (defaultCursor == null) {
 			defaultCursor = new Cursor(shell.getDisplay(), SWT.CURSOR_ARROW);
 		}
 		setCursor(shell, defaultCursor);
 	}
 
 	/**
 	 * Sets the given cursor for the shell
 	 * 
 	 * @param shell
 	 * @param cursor
 	 *            - new cursor
 	 */
 	private void setCursor(Shell shell, Cursor cursor) {
 		if ((cursor != null) && (shell.getCursor() != cursor)) {
 			shell.setCursor(cursor);
 		}
 	}
 
 	/**
 	 * Returns the menu manager of the main menu (menu bar).
 	 * 
 	 * @return menu manager
 	 */
 	private MenuManager getMenuManager() {
 
 		WorkbenchWindow workbenchWindow = (WorkbenchWindow) getWindowConfigurer().getWindow();
 		return workbenchWindow.getMenuManager();
 
 	}
 
 	/**
 	 * TODO
 	 */
 	private StatusLineManager getStatusLineManager() {
 
 		WorkbenchWindow workbenchWindow = (WorkbenchWindow) getWindowConfigurer().getWindow();
 		return workbenchWindow.getStatusLineManager();
 
 	}
 
 	private class SubApplicationListener extends SubApplicationAdapter {
 
 		/**
 		 * @see org.eclipse.riena.navigation.model.NavigationTreeAdapter#activated(org.eclipse.riena.navigation.ISubApplication)
 		 */
 		@Override
 		public void activated(ISubApplication source) {
 			if (source != null) {
 				showPerspective(source);
 			}
 			super.activated(source);
 		}
 
 		private void showPerspective(ISubApplication source) {
 			try {
 				PlatformUI.getWorkbench().showPerspective(
 						SwtPresentationManagerAccessor.getManager().getSwtViewId(source).getId(),
 						PlatformUI.getWorkbench().getActiveWorkbenchWindow());
 
 			} catch (WorkbenchException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * Returns the margin between the top of the shell and the widget with the
 	 * sub-application switchers.
 	 * 
 	 * @return margin
 	 */
 	private int getSwitchterTopMargin() {
 
 		int margin = LnfManager.getLnf().getIntegerSetting(ILnfKeyConstants.SUB_APPLICATION_SWITCHER_TOP_MARGIN);
 		return margin;
 
 	}
 
 	/**
 	 * Returns the of the sub-application switcher.
 	 * 
 	 * @return height
 	 */
 	private int getSwitchterHeight() {
 
 		int margin = LnfManager.getLnf().getIntegerSetting(ILnfKeyConstants.SUB_APPLICATION_SWITCHER_HEIGHT);
 		return margin;
 
 	}
 
 	/**
 	 * Creates and positions the composite of the logo.
 	 * 
 	 * @param parent
 	 *            - parent composite
 	 */
 	private void createLogoComposite(Composite parent) {
 
 		assert parent.getLayout() instanceof FormLayout;
 
 		Composite topLeftComposite = new Composite(parent, SWT.DOUBLE_BUFFERED);
 		FormData logoData = new FormData();
 		ShellBorderRenderer borderRenderer = (ShellBorderRenderer) LnfManager.getLnf().getRenderer(
 				ILnfKeyConstants.TITLELESS_SHELL_BORDER_RENDERER);
 		int borderWidth = borderRenderer.getBorderWidth();
 		logoData.top = new FormAttachment(0, borderWidth);
 		int padding = borderRenderer.getCompleteBorderWidth();
 		int height = getSwitchterTopMargin() + getSwitchterHeight() + padding - 1;
 		logoData.bottom = new FormAttachment(0, height);
 		logoData.width = getLogoImage().getImageData().width + ShellLogoRenderer.getHorizontalLogoMargin() * 2;
 		Integer hPos = getHorizontalLogoPosition();
 		switch (hPos) {
 		case SWT.CENTER:
 			logoData.left = new FormAttachment(50, -logoData.width / 2);
 			break;
 		case SWT.RIGHT:
 			logoData.right = new FormAttachment(100, -borderWidth);
 			break;
 		default:
 			logoData.left = new FormAttachment(0, borderWidth);
 			break;
 		}
 		topLeftComposite.setLayoutData(logoData);
 
 		topLeftComposite.addPaintListener(new LogoPaintListener());
 
 	}
 
 	/**
 	 * Creates and positions the corner to grab.
 	 * 
 	 * @param shell
 	 */
 	private void createGrabCorner(final Shell shell) {
 
 		if (GrabCorner.isResizeable()) {
 			new GrabCorner(shell, SWT.DOUBLE_BUFFERED);
 		}
 
 	}
 
 	/**
 	 * Creates and positions the composite for the sub-application switcher.
 	 * 
 	 * @param parent
 	 *            - parent of composite
 	 * @return composite
 	 */
 	private Composite createSwitcherComposite(Composite parent) {
 
 		assert parent.getLayout() instanceof FormLayout;
 
 		int padding = getShellPadding();
 
 		Composite composite = new Composite(parent, SWT.DOUBLE_BUFFERED);
 		composite.setLayout(new FillLayout());
 		FormData formData = new FormData();
 		formData.top = new FormAttachment(0, getSwitchterTopMargin() + padding);
 		formData.left = new FormAttachment(0, padding);
 		formData.right = new FormAttachment(100, -padding);
 		formData.height = getSwitchterHeight();
 		composite.setLayoutData(formData);
 		ApplicationModel model = (ApplicationModel) controller.getNavigationNode();
 		SubApplicationSwitcherViewPart switcherViewPart = new SubApplicationSwitcherViewPart(model);
 		switcherViewPart.createPartControl(composite);
 
 		return composite;
 
 	}
 
 	/**
 	 * Creates and positions the composite for the menu bar.
 	 * 
 	 * @param parent
 	 *            - parent of composite
 	 * @param previous
 	 *            - previous composite in the layout
 	 * @return composite
 	 */
 	private Composite createMenuBarComposite(Composite parent, Composite previous) {
 
 		assert parent.getLayout() instanceof FormLayout;
 
 		int padding = getShellPadding();
 
 		// menu bar
 		Composite composite = new Composite(parent, SWT.NONE);
 		Color menuBarColor = LnfManager.getLnf().getColor(ILnfKeyConstants.COOLBAR_BACKGROUND);
 		composite.setBackground(menuBarColor);
 		composite.setLayout(new FillLayout());
 		FormData formData = new FormData();
 		formData.top = new FormAttachment(previous, COOLBAR_TOP_MARGIN);
 		formData.left = new FormAttachment(0, padding);
 		formData.right = new FormAttachment(100, -padding);
 		formData.height = COOLBAR_HIGHT;
 		composite.setLayoutData(formData);
 
 		createMenuBar(composite);
 
 		return composite;
 
 	}
 
 	/**
 	 * Creates a cool bar with menus.
 	 * 
 	 * @param parent
 	 * @return cool bar with menus
 	 */
 	private CoolBar createMenuBar(Composite parent) {
 
 		coolBar = new CoolBar(parent, SWT.HORIZONTAL | SWT.FLAT);
 		CoolItem coolItem = new CoolItem(coolBar, SWT.DROP_DOWN);
 		toolBar = new ToolBar(coolBar, SWT.FLAT);
 		coolItem.setControl(toolBar);
 		toolBar.addMouseMoveListener(new ToolBarMouseListener());
 
 		// create for every top menu a tool item and create the corresponding
 		// menu
 		IContributionItem[] contribItems = getMenuManager().getItems();
 		for (int i = 0; i < contribItems.length; i++) {
 			if (contribItems[i] instanceof MenuManager) {
 				MenuManager topMenuManager = (MenuManager) contribItems[i];
 				ToolItem toolItem = new ToolItem(toolBar, SWT.CHECK);
 				toolItem.setText(topMenuManager.getMenuText());
 				createMenu(toolBar, toolItem, topMenuManager);
 			}
 		}
 
 		coolBar.setLocked(true);
 		calcSize(coolItem);
 
 		return coolBar;
 
 	}
 
 	/**
 	 * Creates with the help of the given menu manager a menu. If the given tool
 	 * item is selected, the menu is shown.
 	 * 
 	 * @param parent
 	 * @param toolItem
 	 *            - tool item with menu
 	 * @param topMenuManager
 	 *            - menu manager
 	 * @return menu
 	 */
 	private Menu createMenu(Composite parent, final ToolItem toolItem, MenuManager topMenuManager) {
 
 		final Menu menu = topMenuManager.createContextMenu(parent);
 		menu.addMenuListener(new MenuListener() {
 
 			/**
 			 * @see org.eclipse.swt.events.MenuListener#menuHidden(org.eclipse.swt.events.MenuEvent)
 			 */
 			public void menuHidden(MenuEvent e) {
 				if (e.getSource() == menu) {
 					toolItem.setSelection(false);
 				}
 			}
 
 			/**
 			 * @see org.eclipse.swt.events.MenuListener#menuShown(org.eclipse.swt.events.MenuEvent)
 			 */
 			public void menuShown(MenuEvent e) {
 			}
 
 		});
 
 		toolItem.addSelectionListener(new SelectionListener() {
 
 			/**
 			 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
 			 */
 			public void widgetDefaultSelected(SelectionEvent e) {
 			}
 
 			/**
 			 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
 			 */
 			public void widgetSelected(SelectionEvent e) {
 				if (e.getSource() == toolItem) {
 					Rectangle itemBounds = toolItem.getBounds();
 					Point loc = toolItem.getParent().toDisplay(itemBounds.x, itemBounds.height + itemBounds.y);
 					menu.setLocation(loc);
 					menu.setVisible(true);
 				}
 			}
 
 		});
 
 		return menu;
 
 	}
 
 	/**
 	 * Calculates and sets the size of the given cool item.
 	 * 
 	 * @param item
 	 *            - item of cool bar
 	 */
 	private void calcSize(CoolItem item) {
 		Control control = item.getControl();
 		Point pt = control.computeSize(SWT.DEFAULT, SWT.DEFAULT);
 		pt = item.computeSize(pt.x, pt.y);
 		item.setSize(pt);
 	}
 
 	/**
 	 * If the mouse moves over an unselected item of the tool bar and another
 	 * item was selected, deselect the other item and select the item below the
 	 * mouse pointer.<br>
 	 * <i>Does not work, if menu is visible.</i>
 	 */
 	private static class ToolBarMouseListener implements MouseMoveListener {
 
 		/**
 		 * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
 		 */
 		public void mouseMove(MouseEvent e) {
 
 			if (e.getSource() instanceof ToolBar) {
 
 				ToolBar toolBar = (ToolBar) e.getSource();
 
 				ToolItem selectedItem = null;
 				ToolItem[] items = toolBar.getItems();
 				for (int i = 0; i < items.length; i++) {
 					if (items[i].getSelection()) {
 						selectedItem = items[i];
 					}
 				}
 
 				ToolItem hoverItem = toolBar.getItem(new Point(e.x, e.y));
 				if (hoverItem != null) {
 					if (!hoverItem.getSelection() && (selectedItem != null)) {
 						selectedItem.setSelection(false);
 						hoverItem.setSelection(true);
 					}
 				}
 			}
 
 		}
 
 	}
 
 	/**
 	 * Creates and positions the composite for the cool bar.
 	 * 
 	 * @param parent
 	 *            - parent of composite
 	 * @param previous
 	 *            - previous composite in the layout
 	 * @return composite
 	 */
 	private Composite createCoolBarComposite(Composite parent, Composite previous) {
 
 		assert parent.getLayout() instanceof FormLayout;
 
 		int padding = getShellPadding();
 
 		Composite composite = new Composite(parent, SWT.NONE);
 		Color coolBarColor = LnfManager.getLnf().getColor(ILnfKeyConstants.COOLBAR_BACKGROUND);
 		composite.setBackground(coolBarColor);
 		composite.setLayout(new FillLayout());
 		FormData formData = new FormData();
 		formData.top = new FormAttachment(previous, COOLBAR_TOP_MARGIN);
 		formData.left = new FormAttachment(0, padding);
 		formData.right = new FormAttachment(100, -padding);
 		formData.height = COOLBAR_HIGHT;
 		composite.setLayoutData(formData);
 		getWindowConfigurer().createCoolBarControl(composite);
 
 		return composite;
 
 	}
 
 	/**
 	 * Creates the main composite.
 	 * 
 	 * @param parent
 	 *            - parent of composite
 	 * @param previous
 	 *            - previous composite in the layout
 	 * @return composite
 	 */
 	private Composite createMainComposite(Composite parent, Composite previous) {
 
 		assert parent.getLayout() instanceof FormLayout;
 
 		int padding = getShellPadding();
 
 		Composite composite = new Composite(parent, SWT.DOUBLE_BUFFERED);
 		composite.setLayout(new FillLayout());
 		FormData formData = new FormData();
 		formData.top = new FormAttachment(previous, 0, 0);
 		formData.bottom = new FormAttachment(100, -padding);
 		formData.left = new FormAttachment(0, padding);
 		formData.right = new FormAttachment(100, -padding);
 		composite.setLayoutData(formData);
 		getWindowConfigurer().createPageComposite(composite);
 
 		return composite;
 
 	}
 
 	private Composite createStatusLineComposite(Shell shell) {
 
 		assert shell.getLayout() instanceof FormLayout;
 
 		Point grabCornerSize = GrabCorner.getGrabCornerSize();
 		int padding = getShellPadding();
 
 		Composite composite = new Composite(shell, SWT.DOUBLE_BUFFERED);
 		composite.setBackground(LnfManager.getLnf().getColor("red"));
 		composite.setLayout(new FillLayout());
 		FormData formData = new FormData();
 		formData.height = STATUSLINE_HIGHT;
 		formData.bottom = new FormAttachment(100, -padding);
 		formData.left = new FormAttachment(0, padding);
 		formData.right = new FormAttachment(100, -padding);
 		composite.setLayoutData(formData);
 
 		getWindowConfigurer().getPresentationFactory().createStatusLineControl(getStatusLineManager(), composite);
 
 		return composite;
 
 	}
 
 	/**
 	 * Returns the padding between shell border and content.
 	 * 
 	 * @return padding
 	 */
 	private int getShellPadding() {
 
 		ShellBorderRenderer borderRenderer = (ShellBorderRenderer) LnfManager.getLnf().getRenderer(
 				ILnfKeyConstants.TITLELESS_SHELL_BORDER_RENDERER);
 		return borderRenderer.getCompleteBorderWidth();
 
 	}
 
 	/**
 	 * Returns the image of the logo.
 	 * 
 	 * @return logo image
 	 */
 	private Image getLogoImage() {
 		return LnfManager.getLnf().getImage(ILnfKeyConstants.TITLELESS_SHELL_LOGO);
 	}
 
 	/**
 	 * Returns the horizontal position of the logo inside the shell.
 	 * 
 	 * @return horizontal position (SWT.LEFT, SWT.CENTER, SWT.RIGHT)
 	 */
 	private int getHorizontalLogoPosition() {
 
 		Integer hPos = LnfManager.getLnf().getIntegerSetting(ILnfKeyConstants.TITLELESS_SHELL_HORIZONTAL_LOGO_POSITION);
 		if (hPos == null) {
 			hPos = SWT.LEFT;
 		}
 		return hPos;
 
 	}
 
 	/**
 	 * Returns the renderer of the shell.
 	 * 
 	 * @return renderer
 	 */
 	private ShellRenderer getShellRenderer() {
 		ShellRenderer shellRenderer = (ShellRenderer) LnfManager.getLnf().getRenderer(
 				ILnfKeyConstants.TITLELESS_SHELL_RENDERER);
 		return shellRenderer;
 	}
 
 	/**
 	 * This listener paints the shell.
 	 */
 	private class TitlelessPaintListener implements PaintListener {
 
 		/**
 		 * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
 		 */
 		public void paintControl(PaintEvent e) {
 			onPaint(e);
 		}
 
 		/**
 		 * Paints the border, title, buttons and background of the (titleless)
 		 * shell.
 		 * 
 		 * @param e
 		 *            - event
 		 */
 		private void onPaint(PaintEvent e) {
 
 			if ((e.getSource() != null) && (e.getSource() instanceof Shell)) {
 
 				Shell shell = (Shell) e.getSource();
 
				Rectangle shellBounds = shell.getBounds();
				Rectangle bounds = new Rectangle(0, 0, shellBounds.width, shellBounds.height);
 
 				GC gc = e.gc;
 
 				ILnfRenderer shellRenderer = getShellRenderer();
 				shellRenderer.setBounds(bounds);
 				shellRenderer.paint(gc, shell);
 
 				ILnfRenderer borderRenderer = LnfManager.getLnf().getRenderer(
 						ILnfKeyConstants.TITLELESS_SHELL_BORDER_RENDERER);
 				borderRenderer.setBounds(bounds);
 				borderRenderer.paint(gc, null);
 
 			}
 
 		}
 
 	}
 
 	/**
 	 * When the state of the shell is changed a redraw maybe necessary.
 	 */
 	private static class TitlelessShellListener implements ShellListener, ControlListener {
 
 		private Rectangle moveBounds;
 
 		/**
 		 * @see org.eclipse.swt.events.ShellListener#shellActivated(org.eclipse.swt.events.ShellEvent)
 		 */
 		public void shellActivated(ShellEvent e) {
 			onStateChanged(e);
 		}
 
 		/**
 		 * @see org.eclipse.swt.events.ShellListener#shellClosed(org.eclipse.swt.events.ShellEvent)
 		 */
 		public void shellClosed(ShellEvent e) {
 		}
 
 		/**
 		 * @see org.eclipse.swt.events.ShellListener#shellDeactivated(org.eclipse.swt.events.ShellEvent)
 		 */
 		public void shellDeactivated(ShellEvent e) {
 			onStateChanged(e);
 		}
 
 		/**
 		 * @see org.eclipse.swt.events.ShellListener#shellDeiconified(org.eclipse.swt.events.ShellEvent)
 		 */
 		public void shellDeiconified(ShellEvent e) {
 			onStateChanged(e);
 		}
 
 		/**
 		 * @see org.eclipse.swt.events.ShellListener#shellIconified(org.eclipse.swt.events.ShellEvent)
 		 */
 		public void shellIconified(ShellEvent e) {
 		}
 
 		/**
 		 * Redraws the shell.
 		 * 
 		 * @param e
 		 *            - event
 		 */
 		private void onStateChanged(ShellEvent e) {
 			if ((e.getSource() != null) && (e.getSource() instanceof Shell)) {
 				Shell shell = (Shell) e.getSource();
 				shell.redraw();
 			}
 		}
 
 		/**
 		 * @see org.eclipse.swt.events.ControlListener#controlMoved(org.eclipse.swt.events.ControlEvent)
 		 */
 		public void controlMoved(ControlEvent e) {
 			if ((e.getSource() != null) && (e.getSource() instanceof Shell)) {
 				Shell shell = (Shell) e.getSource();
 				Display display = shell.getDisplay();
 				if ((moveBounds == null) || (!displaySurrounds(display, moveBounds))) {
 					shell.setRedraw(false);
 					shell.setRedraw(true);
 					shell.redraw();
 				}
 				moveBounds = shell.getBounds();
 			}
 		}
 
 		/**
 		 * @see org.eclipse.swt.events.ControlListener#controlResized(org.eclipse.swt.events.ControlEvent)
 		 */
 		public void controlResized(ControlEvent e) {
 		}
 
 		/**
 		 * Returns <code>true</code> if the given bounds are is inside the area
 		 * of the display, and <code>false</code> otherwise.
 		 * 
 		 * @param display
 		 *            - display
 		 * @param bounds
 		 *            - bounds to test for containment
 		 * @return <code>true</code> if the rectangle contains the bounds and
 		 *         <code>false</code> otherwise
 		 */
 		private boolean displaySurrounds(Display display, Rectangle bounds) {
 
 			// top left
 			if (!display.getBounds().contains(bounds.x, bounds.y)) {
 				return false;
 			}
 			// top right
 			if (!display.getBounds().contains(bounds.x + bounds.width, bounds.y)) {
 				return false;
 			}
 			// bottom left
 			if (!display.getBounds().contains(bounds.x, bounds.y + bounds.height)) {
 				return false;
 			}
 			// bottom right
 			if (!display.getBounds().contains(bounds.x + bounds.width, bounds.y + bounds.height)) {
 				return false;
 			}
 
 			return true;
 
 		}
 
 	}
 
 	/**
 	 * This listener paints the logo.
 	 */
 	private static class LogoPaintListener implements PaintListener {
 
 		/**
 		 * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
 		 */
 		public void paintControl(PaintEvent e) {
 			onPaint(e);
 		}
 
 		/**
 		 * Paints the image of the logo.
 		 * 
 		 * @param e
 		 *            - an event containing information about the paint
 		 */
 		private void onPaint(PaintEvent e) {
 
 			ShellLogoRenderer renderer = (ShellLogoRenderer) LnfManager.getLnf().getRenderer(
 					ILnfKeyConstants.TITLELESS_SHELL_LOGO_RENDERER);
 			renderer.setBounds(e.x, e.y, e.width, e.height);
 			renderer.paint(e.gc, null);
 
 		}
 
 	}
 
 	/**
 	 * After any mouse operation a method of this listener is called.
 	 */
 	private class TitlelessShellMouseListener implements MouseListener, MouseTrackListener, MouseMoveListener {
 
 		private final static int BTN_COUNT = 3;
 		private final static int CLOSE_BTN_INDEX = 0;
 		private final static int MAX_BTN_INDEX = 1;
 		private final static int MIN_BTN_INDEX = 2;
 		private BtnState[] btnStates = new BtnState[BTN_COUNT];
 		private boolean mouseDownOnButton;
 		private boolean moveInside;
 		private boolean move;
 		private Point moveStartPoint;
 
 		public TitlelessShellMouseListener() {
 			resetBtnStates();
 			mouseDownOnButton = false;
 			move = false;
 		}
 
 		/**
 		 * Returns the shell on which the event initially occurred.
 		 * 
 		 * @param e
 		 *            - mouse event
 		 * @return shell or <code>null</code> if source is not a shell.
 		 */
 		private Shell getShell(MouseEvent e) {
 
 			if (e.getSource() == null) {
 				return null;
 			}
 			if (!(e.getSource() instanceof Shell)) {
 				return null;
 			}
 			return (Shell) e.getSource();
 
 		}
 
 		/**
 		 * Resets the states of the buttons.
 		 */
 		private void resetBtnStates() {
 			for (int i = 0; i < btnStates.length; i++) {
 				changeBtnState(BtnState.NONE, i);
 			}
 		}
 
 		/**
 		 * Sets the state of a button (and resets the others).
 		 * 
 		 * @param newState
 		 *            - state to set
 		 * @param btnIndex
 		 *            - button index
 		 */
 		private void changeBtnState(BtnState newState, int btnIndex) {
 			if (newState != BtnState.NONE) {
 				resetBtnStates();
 			}
 			btnStates[btnIndex] = newState;
 		}
 
 		/**
 		 * Updates the states of the buttons.
 		 * 
 		 * @param e
 		 *            - mouse event
 		 */
 		private void updateButtonStates(MouseEvent e) {
 
 			Point pointer = new Point(e.x, e.y);
 			boolean insideAButton = false;
 
 			resetBtnStates();
 			if (getShellRenderer().isInsideCloseButton(pointer)) {
 				if (mouseDownOnButton) {
 					changeBtnState(BtnState.HOVER_SELECTED, CLOSE_BTN_INDEX);
 				} else {
 					changeBtnState(BtnState.HOVER, CLOSE_BTN_INDEX);
 				}
 				insideAButton = true;
 			} else if (getShellRenderer().isInsideMaximizeButton(pointer)) {
 				if (mouseDownOnButton) {
 					changeBtnState(BtnState.HOVER_SELECTED, MAX_BTN_INDEX);
 				} else {
 					changeBtnState(BtnState.HOVER, MAX_BTN_INDEX);
 				}
 				insideAButton = true;
 			} else if (getShellRenderer().isInsideMinimizeButton(pointer)) {
 				if (mouseDownOnButton) {
 					changeBtnState(BtnState.HOVER_SELECTED, MIN_BTN_INDEX);
 				} else {
 					changeBtnState(BtnState.HOVER, MIN_BTN_INDEX);
 				}
 				insideAButton = true;
 			}
 			if (!insideAButton) {
 				mouseDownOnButton = false;
 			}
 
 			boolean redraw = false;
 			for (int i = 0; i < btnStates.length; i++) {
 				boolean hover = btnStates[i] == BtnState.HOVER;
 				boolean pressed = btnStates[i] == BtnState.HOVER_SELECTED && mouseDownOnButton;
 				switch (i) {
 				case CLOSE_BTN_INDEX:
 					if (getShellRenderer().isCloseButtonHover() != hover) {
 						getShellRenderer().setCloseButtonHover(hover);
 						redraw = true;
 					}
 					if (getShellRenderer().isCloseButtonPressed() != pressed) {
 						getShellRenderer().setCloseButtonPressed(pressed);
 						redraw = true;
 					}
 					break;
 				case MAX_BTN_INDEX:
 					if (getShellRenderer().isMaximizedButtonHover() != hover) {
 						getShellRenderer().setMaximizedButtonHover(hover);
 						redraw = true;
 					}
 					if (getShellRenderer().isMaximizedButtonPressed() != pressed) {
 						getShellRenderer().setMaximizedButtonPressed(pressed);
 						redraw = true;
 					}
 					break;
 				case MIN_BTN_INDEX:
 					if (getShellRenderer().isMinimizedButtonHover() != hover) {
 						getShellRenderer().setMinimizedButtonHover(hover);
 						redraw = true;
 					}
 					if (getShellRenderer().isMinimizedButtonPressed() != pressed) {
 						getShellRenderer().setMinimizedButtonPressed(pressed);
 						redraw = true;
 					}
 					break;
 				}
 			}
 
 			if (redraw) {
 				Shell shell = getShell(e);
 				shell.setRedraw(false);
 				shell.setRedraw(true);
 			}
 
 		}
 
 		private void updateCursor(MouseEvent e) {
 
 			Shell shell = getShell(e);
 
 			Point pointer = new Point(e.x, e.y);
 			if (moveInside && getShellRenderer().isInsideMoveArea(pointer)) {
 				if (move) {
 					showGrabCursor(shell);
 				} else {
 					showHandCursor(shell);
 				}
 			} else {
 				if (!move) {
 					showDefaultCursor(shell);
 				}
 			}
 
 		}
 
 		/**
 		 * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
 		 */
 		public void mouseDoubleClick(MouseEvent e) {
 		}
 
 		/**
 		 * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
 		 */
 		public void mouseDown(MouseEvent e) {
 			mouseDownOnButton = true;
 			updateButtonStates(e);
 			if (!mouseDownOnButton) {
 				Point pointer = new Point(e.x, e.y);
 				if (getShellRenderer().isInsideMoveArea(pointer)) {
 					move = true;
 					moveStartPoint = pointer;
 				} else {
 					move = false;
 				}
 			}
 			updateCursor(e);
 		}
 
 		/**
 		 * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
 		 */
 		public void mouseUp(MouseEvent e) {
 
 			Point pointer = new Point(e.x, e.y);
 
 			if (mouseDownOnButton && (getShell(e) != null)) {
 				if (getShellRenderer().isInsideCloseButton(pointer)) {
 					if (btnStates[CLOSE_BTN_INDEX] == BtnState.HOVER_SELECTED) {
 						getShell(e).close();
 					}
 				} else if (getShellRenderer().isInsideMaximizeButton(pointer)) {
 					if (btnStates[MAX_BTN_INDEX] == BtnState.HOVER_SELECTED) {
 						boolean maximized = getShell(e).getMaximized();
 						getShell(e).setMaximized(!maximized);
 					}
 				} else if (getShellRenderer().isInsideMinimizeButton(pointer)) {
 					if (btnStates[MIN_BTN_INDEX] == BtnState.HOVER_SELECTED) {
 						getShell(e).setMinimized(true);
 					}
 				}
 			}
 
 			mouseDownOnButton = false;
 			updateButtonStates(e);
 			move = false;
 			updateCursor(e);
 
 		}
 
 		/**
 		 * @see org.eclipse.swt.events.MouseTrackListener#mouseEnter(org.eclipse.swt.events.MouseEvent)
 		 */
 		public void mouseEnter(MouseEvent e) {
 			updateButtonStates(e);
 			moveInside = true;
 			move = false;
 			updateCursor(e);
 		}
 
 		/**
 		 * @see org.eclipse.swt.events.MouseTrackListener#mouseExit(org.eclipse.swt.events.MouseEvent)
 		 */
 		public void mouseExit(MouseEvent e) {
 			updateButtonStates(e);
 			moveInside = false;
 			move = false;
 			updateCursor(e);
 		}
 
 		/**
 		 * @see org.eclipse.swt.events.MouseTrackListener#mouseHover(org.eclipse.swt.events.MouseEvent)
 		 */
 		public void mouseHover(MouseEvent e) {
 		}
 
 		/**
 		 * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
 		 */
 		public void mouseMove(MouseEvent e) {
 			updateButtonStates(e);
 			if (move) {
 				move(e);
 			}
 			updateCursor(e);
 		}
 
 		private void move(MouseEvent e) {
 			Point moveEndPoint = new Point(e.x, e.y);
 			Shell shell = getShell(e);
 			int xMove = moveStartPoint.x - moveEndPoint.x;
 			int yMove = moveStartPoint.y - moveEndPoint.y;
 			int x = shell.getLocation().x - xMove;
 			int y = shell.getLocation().y - yMove;
 			shell.setLocation(x, y);
 		}
 
 	}
 
 }
