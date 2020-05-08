 /*******************************************************************************
  * Copyright (c) 2010, 2012 AGETO Service GmbH and others.
  * All rights reserved.
  *
  * This program and the accompanying materials are made available under the
  * terms of the Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html.
  *
  * Contributors:
  *     Mike Tschierschke - initial API and implementation
  *     Gunnar Wagenknecht - rework to new console look (based on RAP Examples)
  *******************************************************************************/
 package org.eclipse.gyrex.admin.ui.internal.application;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.gyrex.admin.ui.internal.AdminUiActivator;
 import org.eclipse.gyrex.admin.ui.internal.pages.registry.AdminPageRegistry;
 import org.eclipse.gyrex.admin.ui.internal.pages.registry.PageContribution;
 import org.eclipse.gyrex.admin.ui.pages.AdminPage;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.util.Policy;
 import org.eclipse.rwt.RWT;
 import org.eclipse.rwt.events.BrowserHistoryEvent;
 import org.eclipse.rwt.events.BrowserHistoryListener;
 import org.eclipse.rwt.internal.widgets.JSExecutor;
 import org.eclipse.rwt.lifecycle.IEntryPoint;
 import org.eclipse.rwt.lifecycle.WidgetUtil;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 
 import org.osgi.framework.Version;
 
 /**
  * This class controls all aspects of the application's execution and is
  * contributed through the plugin.xml.
  */
 @SuppressWarnings("restriction")
 public class AdminApplication implements IEntryPoint {
 
 	private static final String GYREX_WEBSITE_URL = "http://eclipse.org/gyrex/";
 	private static final int CONTENT_MIN_HEIGHT = 800;
 	private static final int HEADER_HEIGHT = 140;
 	private static final int CENTER_AREA_WIDTH = 998;
 
 	private static FormData createLogoFormData(final Image rapLogo) {
 		final FormData data = new FormData();
 		data.left = new FormAttachment(0);
 		final int logoHeight = rapLogo.getBounds().height;
 		data.top = new FormAttachment(50, -(logoHeight / 2));
 		return data;
 	}
 
 	private static FormData createNavBarFormData() {
 		final FormData data = new FormData();
 		data.top = new FormAttachment(0);
 		data.left = new FormAttachment(0);
 		data.right = new FormAttachment(100);
 		return data;
 	}
 
 	private static FormData createNavigationFormData() {
 		final FormData data = new FormData();
 		data.left = new FormAttachment(50, (-CENTER_AREA_WIDTH / 2) + 7);
 		data.top = new FormAttachment(0);
 		data.bottom = new FormAttachment(100);
 		data.width = CENTER_AREA_WIDTH;
 		return data;
 	}
 
 	private static FormData createTitleFormData() {
 		final FormData data = new FormData();
 		data.bottom = new FormAttachment(100, -18);
 		data.left = new FormAttachment(0, 370);
 		return data;
 	}
 
 	public static Image getImage(final Display display, final String path) {
 		final ImageDescriptor imageDescriptor = AdminUiActivator.getImageDescriptor("img/" + path);
 		return imageDescriptor.createImage(display);
 	}
 
 	private static String getVersion() {
 		final Version version = AdminUiActivator.getInstance().getBundleVersion();
 		final StringBuilder resultBuffer = new StringBuilder(20);
 		resultBuffer.append(version.getMajor());
 		resultBuffer.append('.');
 		resultBuffer.append(version.getMinor());
 		resultBuffer.append('.');
 		resultBuffer.append(version.getMicro());
 		resultBuffer.append(" (Build ");
 		resultBuffer.append(version.getQualifier());
 		resultBuffer.append(')');
 		return resultBuffer.toString();
 	}
 
 	private static void makeLink(final Label control, final String url) {
 		control.setCursor(control.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
 		control.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseDown(final MouseEvent e) {
 				JSExecutor.executeJS("window.location.href = '" + url + "'");
 			}
 		});
 	}
 
 	private Composite centerArea;
 	private Navigation navigation;
 	private Composite navBar;
 	private final Map<String, AdminPage> pagesById = new HashMap<String, AdminPage>();
 	private AdminPage currentPage;
 
 	private void activate(final AdminPage page, final PageContribution contribution) {
 		// TODO: should switch to using a StackLayout and not disposing children every time
 		RWT.getBrowserHistory().createEntry(contribution.getId(), contribution.getName());
		final Control[] children = centerArea.getChildren();
		for (final Control child : children) {
 			child.dispose();
 		}
 		final Composite contentComp = AdminUiUtil.initPage(page.getTitle(), centerArea);
 		page.createControl(contentComp);
 		centerArea.layout(true, true);
 		page.activate();
 	}
 
 	private void attachHistoryListener() {
 		RWT.getBrowserHistory().addBrowserHistoryListener(new BrowserHistoryListener() {
 			public void navigated(final BrowserHistoryEvent event) {
 				final PageContribution contribution = AdminPageRegistry.getInstance().getPage(event.entryId);
 				if (contribution != null) {
 					openPage(contribution);
 				}
 			}
 		});
 	}
 
 	private Composite createCenterArea(final Composite parent, final Composite footer) {
 		final Composite centerArea = new Composite(parent, SWT.NONE);
 		centerArea.setLayout(new FillLayout());
 		centerArea.setLayoutData(createCenterAreaFormData(footer));
 		centerArea.setData(WidgetUtil.CUSTOM_VARIANT, "centerArea");
 		return centerArea;
 	}
 
 	private FormData createCenterAreaFormData(final Composite footer) {
 		final FormData data = new FormData();
 		data.top = new FormAttachment(navBar, 0, SWT.BOTTOM);
 		data.bottom = new FormAttachment(footer, 0, SWT.TOP);
 		data.left = new FormAttachment(50, (-CENTER_AREA_WIDTH / 2) + 10);
 		data.width = CENTER_AREA_WIDTH + 10;
 		return data;
 	}
 
 	private Composite createContent(final ScrolledComposite scrolledArea) {
 		final Composite comp = new Composite(scrolledArea, SWT.NONE);
 		comp.setLayout(new FormLayout());
 		final Composite header = createHeader(comp);
 		header.setLayoutData(createHeaderFormData());
 		createContentBody(comp, header);
 		return comp;
 	}
 
 	private void createContentBody(final Composite parent, final Composite header) {
 		final Composite composite = new Composite(parent, SWT.NONE);
 		composite.setData(WidgetUtil.CUSTOM_VARIANT, "mainContentArea");
 		composite.setLayout(new FormLayout());
 		composite.setLayoutData(createContentBodyFormData(header));
 		navigation = createNavigation(composite);
 		final Composite footer = createFooter(composite);
 		centerArea = createCenterArea(composite, footer);
 	}
 
 	private FormData createContentBodyFormData(final Composite header) {
 		final FormData data = new FormData();
 		data.top = new FormAttachment(header, 0);
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(100, 0);
 		data.bottom = new FormAttachment(100, 0);
 		return data;
 	}
 
 	private Composite createFooter(final Composite contentComposite) {
 		final Composite footer = new Composite(contentComposite, SWT.NONE);
 		footer.setLayout(new FormLayout());
 		footer.setData(WidgetUtil.CUSTOM_VARIANT, "footer");
 		footer.setLayoutData(createFooterFormData());
 		final Label label = new Label(footer, SWT.NONE);
 		label.setData(WidgetUtil.CUSTOM_VARIANT, "footerLabel");
 		label.setText("Admin Console " + getVersion());
 		label.setLayoutData(createFooterLabelFormData(footer));
 		return footer;
 	}
 
 	private FormData createFooterFormData() {
 		final FormData data = new FormData();
 		data.left = new FormAttachment(50, (-CENTER_AREA_WIDTH / 2));
 		data.top = new FormAttachment(100, -40);
 		data.bottom = new FormAttachment(100);
 		data.width = CENTER_AREA_WIDTH - 10 - 2;
 		return data;
 	}
 
 	private FormData createFooterLabelFormData(final Composite footer) {
 		final FormData data = new FormData();
 		data.top = new FormAttachment(50, -10);
 		data.right = new FormAttachment(100, -15);
 		return data;
 	}
 
 	private Composite createHeader(final Composite parent) {
 		final Composite comp = new Composite(parent, SWT.NONE);
 		comp.setData(WidgetUtil.CUSTOM_VARIANT, "header");
 		comp.setBackgroundMode(SWT.INHERIT_DEFAULT);
 		comp.setLayout(new FormLayout());
 		final Composite headerCenterArea = createHeaderCenterArea(comp);
 		createLogo(headerCenterArea);
 		createTitle(headerCenterArea);
 		return comp;
 	}
 
 	private Composite createHeaderCenterArea(final Composite parent) {
 		final Composite headerCenterArea = new Composite(parent, SWT.NONE);
 		headerCenterArea.setLayout(new FormLayout());
 		headerCenterArea.setLayoutData(createHeaderCenterAreaFormData());
 		return headerCenterArea;
 	}
 
 	private FormData createHeaderCenterAreaFormData() {
 		final FormData data = new FormData();
 		data.left = new FormAttachment(50, -CENTER_AREA_WIDTH / 2);
 		data.top = new FormAttachment(0);
 		data.bottom = new FormAttachment(100);
 		data.width = CENTER_AREA_WIDTH;
 		return data;
 	}
 
 	private FormData createHeaderFormData() {
 		final FormData data = new FormData();
 		data.top = new FormAttachment(0);
 		data.left = new FormAttachment(0);
 		data.right = new FormAttachment(100);
 		data.height = HEADER_HEIGHT;
 		return data;
 	}
 
 	private void createLogo(final Composite headerComp) {
 		final Label logoLabel = new Label(headerComp, SWT.NONE);
 		final Image logo = getImage(headerComp.getDisplay(), "gyrex/eclipse_gyrex.png");
 		logoLabel.setImage(logo);
 		logoLabel.setLayoutData(createLogoFormData(logo));
 		makeLink(logoLabel, GYREX_WEBSITE_URL);
 	}
 
 	private Shell createMainShell(final Display display) {
 		final Shell shell = new Shell(display, SWT.NO_TRIM);
 		shell.setMaximized(true);
 		shell.setData(WidgetUtil.CUSTOM_VARIANT, "mainshell");
 		return shell;
 	}
 
 	private Navigation createNavigation(final Composite parent) {
 		navBar = new Composite(parent, SWT.NONE);
 		navBar.setLayout(new FormLayout());
 		navBar.setLayoutData(createNavBarFormData());
 		navBar.setData(WidgetUtil.CUSTOM_VARIANT, "nav-bar");
 		final Navigation navigation = new Navigation(navBar) {
 			@Override
 			protected void openPage(final PageContribution page) {
 				AdminApplication.this.openPage(page);
 			}
 		};
 		final Control navigationControl = navigation.getControl();
 		navigationControl.setLayoutData(createNavigationFormData());
 		navigationControl.setData(WidgetUtil.CUSTOM_VARIANT, "navigation");
 		return navigation;
 	}
 
 	private ScrolledComposite createScrolledArea(final Composite parent) {
 		final ScrolledComposite scrolledComp = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
 		scrolledComp.setMinHeight(CONTENT_MIN_HEIGHT);
 		scrolledComp.setMinWidth(CENTER_AREA_WIDTH);
 		scrolledComp.setExpandVertical(true);
 		scrolledComp.setExpandHorizontal(true);
 		return scrolledComp;
 	}
 
 	private void createTitle(final Composite headerComp) {
 		final Label title = new Label(headerComp, SWT.NONE);
 		title.setText("Admin Console");
 		title.setLayoutData(createTitleFormData());
 		title.setData(WidgetUtil.CUSTOM_VARIANT, "title");
 	}
 
 	public int createUI() {
 		final Display display = new Display();
 		final Shell shell = createMainShell(display);
 		shell.setLayout(new FillLayout());
 		final ScrolledComposite scrolledArea = createScrolledArea(shell);
 		final Composite content = createContent(scrolledArea);
 		scrolledArea.setContent(content);
 		attachHistoryListener();
 		shell.open();
 		selectInitialContribution();
 		while (!shell.isDisposed()) {
 			if (!display.readAndDispatch()) {
 				display.sleep();
 			}
 		}
 
 		if (null != currentPage) {
 			deactivate(currentPage);
 		}
 		display.dispose();
 		return 0;
 	}
 
 	private void deactivate(final AdminPage page) {
 		page.deactivate();
 	}
 
 	private AdminPage getPage(final PageContribution contribution) throws CoreException {
 		if (!pagesById.containsKey(contribution.getId())) {
 			pagesById.put(contribution.getId(), contribution.createPage());
 		}
 		return pagesById.get(contribution.getId());
 	}
 
 	private void openPage(final PageContribution contribution) {
 		try {
 			final AdminPage page = getPage(contribution);
 			if (null == page) {
 				Policy.getStatusHandler().show(new Status(IStatus.ERROR, AdminUiActivator.SYMBOLIC_NAME, String.format("Page '%s' not found!", contribution.getId())), "Error Opening Page");
 				return;
 			}
 
 			if ((null != currentPage) && (page != currentPage)) {
 				deactivate(currentPage);
 			}
 
 			currentPage = page;
 			navigation.selectNavigationEntry(contribution);
 			activate(page, contribution);
 		} catch (final CoreException e) {
 			Policy.getStatusHandler().show(e.getStatus(), "Error Opening Page");
 			return;
 		}
 	}
 
 	private void selectInitialContribution() {
 		final PageContribution contribution = navigation.findInitialPage();
 		if (contribution != null) {
 			openPage(contribution);
 		}
 	}
 }
