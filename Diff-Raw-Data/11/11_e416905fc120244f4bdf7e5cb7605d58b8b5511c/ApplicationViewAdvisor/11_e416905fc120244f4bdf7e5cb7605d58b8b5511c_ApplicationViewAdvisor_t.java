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
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.jface.action.IContributionItem;
 import org.eclipse.jface.action.MenuManager;
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
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Cursor;
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
 	private static final int COOLBAR_HEIGHT = 22;
 	private static final int COOLBAR_TOP_MARGIN = 2;
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
 	}
 
 	/**
 	 * @see org.eclipse.ui.application.WorkbenchWindowAdvisor#createWindowContents(org.eclipse.swt.widgets.Shell)
 	 */
 	@Override
 	public void createWindowContents(final Shell shell) {
 
 		initShell(shell);
 
 		// create and layouts the composite of switcher, menu, tool bar etc.
 		shell.setLayout(new FormLayout());
 
 		createGrabCorner(shell);
 		Composite titleComposite = createTitleComposite(shell);
 		Composite menuBarComposite = createMenuBarComposite(shell, titleComposite);
 		Composite coolBarComposite = createCoolBarComposite(shell, menuBarComposite);
 		createMainComposite(shell, coolBarComposite);
 
 	}
 
 	/**
 	 * Initializes the given shell.
 	 * 
 	 * @param shell
 	 *            - shell to initialize
 	 */
 	private void initShell(final Shell shell) {
 		shell.setBackground(LnfManager.getLnf().getColor(ILnfKeyConstants.TITLELESS_SHELL_BACKGROUND));
 		shell.addPaintListener(new ShellPaintListener());
 
 		shell.setImage(ImageUtil.getImage(controller.getNavigationNode().getIcon()));
 		shell.setMinimumSize(APPLICATION_SIZE);
 
 		// prepare shell for binding
 		shell.setData(SWTBindingPropertyLocator.BINDING_PROPERTY, SHELL_RIDGET_PROPERTY);
 		addUIControl(shell);
 
 		getShellRenderer().setShell(shell);
 	}
 
 	protected DefaultBindingManager createBindingManager() {
 		return new DefaultBindingManager(new SWTBindingPropertyLocator(), new DefaultSwtControlRidgetMapper());
 	}
 
 	/**
 	 * Creates a cursor there for the corresponding image of the look and feel
 	 * is used.
 	 * 
 	 * @param control
 	 * @param lnfKey
 	 *            - look and feel key of the cursor image
 	 * @return cursor
 	 */
 	private Cursor createCursor(Control control, String lnfKey) {
 
 		Cursor cursor = null;
 
 		Image cursorImage = LnfManager.getLnf().getImage(lnfKey);
 		if (cursorImage != null) {
 			ImageData imageData = cursorImage.getImageData();
 			int x = imageData.width / 2;
 			int y = imageData.height / 2;
 			cursor = new Cursor(control.getDisplay(), imageData, x, y);
 		}
 		return cursor;
 
 	}
 
 	/**
 	 * Sets the hand cursor for the given control.
 	 * 
 	 * @param control
 	 */
 	private void showHandCursor(Control control) {
 		if (handCursor == null) {
 			handCursor = createCursor(control, ILnfKeyConstants.TITLELESS_SHELL_HAND_IMAGE);
 		}
 		setCursor(control, handCursor);
 	}
 
 	/**
 	 * Sets the grab cursor for the given control.
 	 * 
 	 * @param control
 	 */
 	private void showGrabCursor(Control control) {
 		if (grabCursor == null) {
 			grabCursor = createCursor(control, ILnfKeyConstants.TITLELESS_SHELL_GRAB_IMAGE);
 		}
 		setCursor(control, grabCursor);
 
 	}
 
 	/**
 	 * Sets the default cursor for the given control.
 	 * 
 	 * @param shell
 	 */
 	private void showDefaultCursor(Control control) {
 		if (defaultCursor == null) {
 			defaultCursor = new Cursor(control.getDisplay(), SWT.CURSOR_ARROW);
 		}
 		setCursor(control, defaultCursor);
 	}
 
 	/**
 	 * Sets the given cursor for the control
 	 * 
 	 * @param control
 	 * @param cursor
 	 *            - new cursor
 	 */
 	private void setCursor(Control control, Cursor cursor) {
 		if ((cursor != null) && (control.getCursor() != cursor)) {
 			control.setCursor(cursor);
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
 
 	private class SubApplicationListener extends SubApplicationAdapter {
 
 		/**
 		 * @see org.eclipse.riena.navigation.model.NavigationTreeAdapter#activated(org.eclipse.riena.navigation.ISubApplication)
 		 */
 		@Override
 		public void activated(ISubApplication source) {
 			if (source != null) {
 				showPerspective(source);
				switcherComposite.setRedraw(false);
				switcherComposite.setRedraw(true);
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
 	 * Create the composite that contains the:
 	 * <ul>
 	 * <li>shell title and title buttons</li>
 	 * <li>the logo</li>
 	 * <li>the sub application switcher</li>
 	 * </ul>
 	 * 
 	 * @param parent
 	 *            the parent composite (non null)
 	 * @return the title composite (never null)
 	 */
 	private Composite createTitleComposite(final Composite parent) {
 		Composite result = new Composite(parent, SWT.NONE);
 
 		// force result's background into logo and switcher
 		result.setBackgroundMode(SWT.INHERIT_FORCE);
 		// sets the background of the composite
 		Image image = LnfManager.getLnf().getImage(ILnfKeyConstants.TITLELESS_SHELL_BACKGROUND_IMAGE);
 		result.setBackgroundImage(image);
 
 		result.setLayout(new FormLayout());
 		ShellBorderRenderer borderRenderer = (ShellBorderRenderer) LnfManager.getLnf().getRenderer(
 				ILnfKeyConstants.TITLELESS_SHELL_BORDER_RENDERER);
 		int borderWidth = borderRenderer.getBorderWidth();
 		FormData data = new FormData();
 		data.top = new FormAttachment(parent, borderWidth);
 		data.left = new FormAttachment(0, borderWidth);
 		data.right = new FormAttachment(100, -borderWidth);
 		result.setLayoutData(data);
 
 		createLogoComposite(result);
 		switcherComposite = createSwitcherComposite(result);
 
 		result.addPaintListener(new TitlelessPaintListener());
 
 		TitlelessShellMouseListener mouseListener = new TitlelessShellMouseListener();
 		result.addMouseListener(mouseListener);
 		result.addMouseMoveListener(mouseListener);
 		result.addMouseTrackListener(mouseListener);
 
 		return result;
 	}
 
 	/**
 	 * Creates and positions the composite of the logo.
 	 * 
 	 * @param parent
 	 *            - parent composite
 	 */
 	private void createLogoComposite(Composite parent) {
 
 		Assert.isTrue(parent.getLayout() instanceof FormLayout);
 
 		Composite logoComposite = new Composite(parent, SWT.DOUBLE_BUFFERED);
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
 		logoComposite.setLayoutData(logoData);
 
 		logoComposite.addPaintListener(new LogoPaintListener());
 
 	}
 
 	/**
 	 * Creates and positions the corner to grab.
 	 * 
 	 * @param shell
 	 */
 	private void createGrabCorner(final Shell shell) {
 
 		if (GrabCorner.isResizeable() && LnfManager.getLnf().getBooleanSetting(ILnfKeyConstants.SHELL_HIDE_OS_BORDER)) {
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
 
 		Assert.isTrue(parent.getLayout() instanceof FormLayout);
 
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
 
 		Assert.isTrue(parent.getLayout() instanceof FormLayout);
 
 		int padding = getShellPadding();
 
 		// menu bar
 		Composite composite = new Composite(parent, SWT.NONE);
 		composite.setLayout(new FillLayout());
 		FormData formData = new FormData();
 		formData.top = new FormAttachment(previous, COOLBAR_TOP_MARGIN);
 		formData.left = new FormAttachment(0, padding);
 		formData.right = new FormAttachment(100, -padding);
 		formData.height = COOLBAR_HEIGHT;
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
 	private void createMenuBar(Composite parent) {
 
 		CoolBar coolBar = new CoolBar(parent, SWT.HORIZONTAL | SWT.FLAT);
 		coolBar.setBackground(getCoolbarBackground());
 
 		CoolItem coolItem = new CoolItem(coolBar, SWT.DROP_DOWN);
 		ToolBar toolBar = new ToolBar(coolBar, SWT.FLAT);
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
 	 * Return the coolbar / menubar background color according to the
 	 * look-and-feel.
 	 */
 	private Color getCoolbarBackground() {
 		return LnfManager.getLnf().getColor(ILnfKeyConstants.COOLBAR_BACKGROUND);
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
 
 		Assert.isTrue(parent.getLayout() instanceof FormLayout);
 
 		int padding = getShellPadding();
 
 		Composite composite = new Composite(parent, SWT.NONE);
 		composite.setLayout(new FillLayout());
 		FormData formData = new FormData();
 		formData.top = new FormAttachment(previous, COOLBAR_TOP_MARGIN);
 		formData.left = new FormAttachment(0, padding);
 		formData.right = new FormAttachment(100, -padding);
 		formData.height = COOLBAR_HEIGHT;
 		composite.setLayoutData(formData);
 
 		Control control = getWindowConfigurer().createCoolBarControl(composite);
 		control.setBackground(getCoolbarBackground());
 
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
 
 		Assert.isTrue(parent.getLayout() instanceof FormLayout);
 
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
 	 * This listener paints (top part of) the shell.
 	 */
 	private class TitlelessPaintListener implements PaintListener {
 
 		/**
 		 * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
 		 */
 		public void paintControl(PaintEvent e) {
 			onPaint(e);
 		}
 
 		/**
 		 * Paints the title, buttons and background of the (titleless) shell.
 		 * 
 		 * @param e
 		 *            - event
 		 */
 		private void onPaint(PaintEvent e) {
 
 			if (e.getSource() instanceof Control) {
 
 				Control shell = (Control) e.getSource();
 
 				Rectangle shellBounds = shell.getBounds();
 				Rectangle bounds = new Rectangle(0, 0, shellBounds.width, shellBounds.height);
 
 				ILnfRenderer shellRenderer = getShellRenderer();
 				shellRenderer.setBounds(bounds);
 				shellRenderer.paint(e.gc, shell);
 
 			}
 
 		}
 
 	}
 
 	/**
 	 * This listener paints the shell (the border of the shell).
 	 */
 	private static class ShellPaintListener implements PaintListener {
 
 		/**
 		 * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
 		 */
 		public void paintControl(PaintEvent e) {
 			onPaint(e);
 		}
 
 		/**
 		 * Paints the border of the (titleless) shell.
 		 * 
 		 * @param e
 		 *            - event
 		 */
 		private void onPaint(PaintEvent e) {
 
 			if (e.getSource() instanceof Control) {
 
 				Control shell = (Control) e.getSource();
 
 				Rectangle shellBounds = shell.getBounds();
 				Rectangle bounds = new Rectangle(0, 0, shellBounds.width, shellBounds.height);
 
 				ILnfRenderer borderRenderer = LnfManager.getLnf().getRenderer(
 						ILnfKeyConstants.TITLELESS_SHELL_BORDER_RENDERER);
 				borderRenderer.setBounds(bounds);
 				borderRenderer.paint(e.gc, null);
 
 			}
 
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
 
 			Composite logoComposite = (Composite) e.getSource();
 			Rectangle compositeBounds = logoComposite.getBounds();
 			ShellLogoRenderer renderer = (ShellLogoRenderer) LnfManager.getLnf().getRenderer(
 					ILnfKeyConstants.TITLELESS_SHELL_LOGO_RENDERER);
 			renderer.setBounds(compositeBounds);
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
 				Control control = (Control) e.getSource();
 				Rectangle buttonBounds = getShellRenderer().getButtonsBounds();
 				control.redraw(buttonBounds.x, buttonBounds.y, buttonBounds.width, buttonBounds.height, false);
 			}
 
 		}
 
 		private void updateCursor(MouseEvent e) {
 
 			Control control = (Control) e.getSource();
 
 			Point pointer = new Point(e.x, e.y);
 			if (moveInside && getShellRenderer().isInsideMoveArea(pointer)) {
 				if (move) {
 					showGrabCursor(control);
 				} else {
 					showHandCursor(control);
 				}
 			} else {
 				if (!move) {
 					showDefaultCursor(control);
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
 
 			if (mouseDownOnButton && (e.getSource() instanceof Control)) {
 				Control control = (Control) e.getSource();
 				Shell shell = getShell(control);
 				if (shell != null) {
 					if (getShellRenderer().isInsideCloseButton(pointer)) {
 						if (btnStates[CLOSE_BTN_INDEX] == BtnState.HOVER_SELECTED) {
 							shell.close();
 						}
 					} else if (getShellRenderer().isInsideMaximizeButton(pointer)) {
 						if (btnStates[MAX_BTN_INDEX] == BtnState.HOVER_SELECTED) {
 							boolean maximized = shell.getMaximized();
 							shell.setMaximized(!maximized);
 						}
 					} else if (getShellRenderer().isInsideMinimizeButton(pointer)) {
 						if (btnStates[MIN_BTN_INDEX] == BtnState.HOVER_SELECTED) {
 							shell.setMinimized(true);
 						}
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
 			// unused
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
 			Control control = (Control) e.getSource();
 			Shell shell = getShell(control);
 			int xMove = moveStartPoint.x - moveEndPoint.x;
 			int yMove = moveStartPoint.y - moveEndPoint.y;
 			int x = shell.getLocation().x - xMove;
 			int y = shell.getLocation().y - yMove;
 			shell.setLocation(x, y);
 		}
 
 		private Shell getShell(Control control) {
 			Shell result = null;
 			while (control != null && result == null) {
 				if (control instanceof Shell) {
 					result = (Shell) control;
 				} else {
 					control = control.getParent();
 				}
 			}
 			return result;
 		}
 	}
 
 }
