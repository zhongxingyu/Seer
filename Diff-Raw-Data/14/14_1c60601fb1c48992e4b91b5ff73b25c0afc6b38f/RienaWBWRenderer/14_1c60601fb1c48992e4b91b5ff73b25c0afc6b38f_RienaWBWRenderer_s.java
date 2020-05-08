 package org.eclipse.riena.e4.launcher.rendering;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.osgi.service.event.Event;
 import org.osgi.service.event.EventHandler;
 
 import org.eclipse.core.databinding.observable.Realm;
 import org.eclipse.e4.core.contexts.IEclipseContext;
 import org.eclipse.e4.core.di.annotations.Optional;
 import org.eclipse.e4.core.services.events.IEventBroker;
 import org.eclipse.e4.core.services.log.Logger;
 import org.eclipse.e4.ui.internal.workbench.Activator;
 import org.eclipse.e4.ui.internal.workbench.E4Workbench;
 import org.eclipse.e4.ui.internal.workbench.PartServiceSaveHandler;
 import org.eclipse.e4.ui.internal.workbench.Policy;
 import org.eclipse.e4.ui.model.application.MApplication;
 import org.eclipse.e4.ui.model.application.ui.MContext;
 import org.eclipse.e4.ui.model.application.ui.MElementContainer;
 import org.eclipse.e4.ui.model.application.ui.MUIElement;
 import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
 import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
 import org.eclipse.e4.ui.model.application.ui.basic.MPart;
 import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
 import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
 import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
 import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
 import org.eclipse.e4.ui.services.IServiceConstants;
 import org.eclipse.e4.ui.services.IStylingEngine;
 import org.eclipse.e4.ui.workbench.IPresentationEngine;
 import org.eclipse.e4.ui.workbench.UIEvents;
 import org.eclipse.e4.ui.workbench.modeling.EPartService;
 import org.eclipse.e4.ui.workbench.modeling.ISaveHandler;
 import org.eclipse.e4.ui.workbench.modeling.IWindowCloseHandler;
 import org.eclipse.e4.ui.workbench.renderers.swt.CSSEngineHelper;
 import org.eclipse.e4.ui.workbench.renderers.swt.SWTPartRenderer;
 import org.eclipse.e4.ui.workbench.renderers.swt.WBWRenderer;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.jface.databinding.swt.SWTObservables;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.CheckboxTableViewer;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.window.IShellProvider;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ControlEvent;
 import org.eclipse.swt.events.ControlListener;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.events.ShellAdapter;
 import org.eclipse.swt.events.ShellEvent;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Widget;
 
 import org.eclipse.riena.e4.launcher.E4XMIConstants;
 import org.eclipse.riena.e4.launcher.part.StatusLinePart;
 import org.eclipse.riena.internal.ui.swt.utils.RcpUtilities;
 import org.eclipse.riena.navigation.ApplicationNodeManager;
 import org.eclipse.riena.navigation.ui.swt.component.SwitcherComposite;
 import org.eclipse.riena.ui.swt.lnf.LnfKeyConstants;
 import org.eclipse.riena.ui.swt.lnf.LnfManager;
 import org.eclipse.riena.ui.swt.lnf.renderer.DialogBorderRenderer;
 import org.eclipse.riena.ui.swt.separator.Separator;
 import org.eclipse.riena.ui.swt.utils.UIControlsFactory;
 
 /**
  * Render a Window or Workbench Window.
  * <p>
  * This class is a copy of {@link WBWRenderer}. All changes are collected in riena...() methods so that they can be easily identified.
  * <p>
  * TODO: provide a better solution when Bug 361133 is fixed
  */
 public class RienaWBWRenderer extends SWTPartRenderer {
 
 	public static final String SHELL_CREATED = "shellCreated"; //$NON-NLS-1$
 	private static String ShellMinimizedTag = "shellMinimized"; //$NON-NLS-1$
 	private static String ShellMaximizedTag = "shellMaximized"; //$NON-NLS-1$
 
 	private class WindowSizeUpdateJob implements Runnable {
 		public List<MWindow> windowsToUpdate = new ArrayList<MWindow>();
 
 		public void run() {
 			clearSizeUpdate();
 			while (!windowsToUpdate.isEmpty()) {
 				final MWindow window = windowsToUpdate.remove(0);
 				final Shell shell = (Shell) window.getWidget();
 				if (shell == null || shell.isDisposed()) {
 					continue;
 				}
 
 				shell.setBounds(window.getX(), window.getY(), window.getWidth(), window.getHeight());
 			}
 		}
 	}
 
 	WindowSizeUpdateJob boundsJob;
 
 	void clearSizeUpdate() {
 		boundsJob = null;
 	}
 
 	boolean ignoreSizeChanges = false;
 
 	@Inject
 	Logger logger;
 
 	@Inject
 	private IEventBroker eventBroker;
 
 	@Inject
 	private IPresentationEngine engine;
 
 	private EventHandler shellUpdater;
 	private EventHandler visibilityHandler;
 	private EventHandler sizeHandler;
 	private EventHandler childHandler;
 
 	/**
 	 * manages some Riena-specific window aspects
 	 */
 	private final ApplicationView applicationView = new ApplicationView();
 
 	public RienaWBWRenderer() {
 		super();
 	}
 
 	MPart activePart = null;
 	private Composite header;
 	private Composite mainMenu;
 	private Composite mainToolBar;
 	private Composite statusLine;
 	private Composite perspectiveStack;
 
 	@Inject
 	void trackActivePart(@Optional
 	@Named(IServiceConstants.ACTIVE_PART)
 	final MPart p) {
 		if (activePart == p) {
 			return;
 		}
 
 		if (activePart != null) {
 			activePart.getTags().remove("active"); //$NON-NLS-1$
 
 			MUIElement parent = activePart.getParent();
 			if (parent == null && activePart.getCurSharedRef() != null) {
 				final MPlaceholder ph = activePart.getCurSharedRef();
 				parent = ph.getParent();
 			}
 			if (parent instanceof MPartStack) {
 				styleStack((MPartStack) parent, false);
 			} else {
 				if (activePart.getWidget() != null) {
 					setCSSInfo(activePart, activePart.getWidget());
 				}
 			}
 		}
 
 		activePart = p;
 
 		if (activePart != null) {
 			activePart.getTags().add("active"); //$NON-NLS-1$
 			MUIElement parent = activePart.getParent();
 			if (parent == null && activePart.getCurSharedRef() != null) {
 				final MPlaceholder ph = activePart.getCurSharedRef();
 				parent = ph.getParent();
 			}
 			if (parent instanceof MPartStack && parent.getWidget() != null) {
 				styleStack((MPartStack) parent, true);
 			} else if (activePart.getWidget() != null) {
 				setCSSInfo(activePart, activePart.getWidget());
 			}
 		}
 	}
 
 	private void styleStack(final MPartStack stack, final boolean active) {
 		if (!active) {
 			stack.getTags().remove("active"); //$NON-NLS-1$
 		} else {
 			stack.getTags().add("active"); //$NON-NLS-1$
 		}
 
 		if (stack.getWidget() != null) {
 			setCSSInfo(stack, stack.getWidget());
 		}
 	}
 
 	/**
 	 * Closes the provided detached window.
 	 * 
 	 * @param window
 	 *            the detached window to close
 	 * @return <code>true</code> if the window should be closed, <code>false</code> otherwise
 	 */
 	private boolean closeDetachedWindow(final MWindow window) {
 		final EPartService partService = (EPartService) window.getContext().get(EPartService.class.getName());
 		final List<MPart> parts = modelService.findElements(window, null, MPart.class, null);
 		// this saves one part at a time, not ideal but better than not saving
 		// at all
 		for (final MPart part : parts) {
 			if (!partService.savePart(part, true)) {
 				// user cancelled the operation, return false
 				return false;
 			}
 		}
 
 		// hide every part individually, following 3.x behaviour
 		for (final MPart part : parts) {
 			partService.hidePart(part);
 		}
 		return true;
 	}
 
 	@PostConstruct
 	public void init() {
 		shellUpdater = new EventHandler() {
 			public void handleEvent(final Event event) {
 				// Ensure that this event is for a MMenuItem
 				final Object objElement = event.getProperty(UIEvents.EventTags.ELEMENT);
 				if (!(event.getProperty(UIEvents.EventTags.ELEMENT) instanceof MWindow)) {
 					return;
 				}
 
 				// Is this listener interested ?
 				final MWindow windowModel = (MWindow) objElement;
 				if (windowModel.getRenderer() != RienaWBWRenderer.this) {
 					return;
 				}
 
 				// No widget == nothing to update
 				final Shell theShell = (Shell) windowModel.getWidget();
 				if (theShell == null) {
 					return;
 				}
 
 				final String attName = (String) event.getProperty(UIEvents.EventTags.ATTNAME);
 
 				if (UIEvents.UILabel.LABEL.equals(attName)) {
 					final String newTitle = (String) event.getProperty(UIEvents.EventTags.NEW_VALUE);
 					theShell.setText(newTitle);
 				} else if (UIEvents.UILabel.ICONURI.equals(attName)) {
 					theShell.setImage(getImage(windowModel));
 				} else if (UIEvents.UILabel.TOOLTIP.equals(attName)) {
 					final String newTTip = (String) event.getProperty(UIEvents.EventTags.NEW_VALUE);
 					theShell.setToolTipText(newTTip);
 				}
 			}
 		};
 
 		eventBroker.subscribe(UIEvents.UILabel.TOPIC_ALL, shellUpdater);
 
 		visibilityHandler = new EventHandler() {
 			public void handleEvent(final Event event) {
 				// Ensure that this event is for a MMenuItem
 				final Object objElement = event.getProperty(UIEvents.EventTags.ELEMENT);
 				if (!(objElement instanceof MWindow)) {
 					return;
 				}
 
 				// Is this listener interested ?
 				final MWindow windowModel = (MWindow) objElement;
 				if (windowModel.getRenderer() != RienaWBWRenderer.this) {
 					return;
 				}
 
 				// No widget == nothing to update
 				final Shell theShell = (Shell) windowModel.getWidget();
 				if (theShell == null) {
 					return;
 				}
 
 				final String attName = (String) event.getProperty(UIEvents.EventTags.ATTNAME);
 
 				if (UIEvents.UIElement.VISIBLE.equals(attName)) {
 					final boolean isVisible = (Boolean) event.getProperty(UIEvents.EventTags.NEW_VALUE);
 					theShell.setVisible(isVisible);
 				}
 			}
 		};
 
 		eventBroker.subscribe(UIEvents.UIElement.TOPIC_VISIBLE, visibilityHandler);
 
 		sizeHandler = new EventHandler() {
 			public void handleEvent(final Event event) {
 				if (ignoreSizeChanges) {
 					return;
 				}
 
 				// Ensure that this event is for a MMenuItem
 				final Object objElement = event.getProperty(UIEvents.EventTags.ELEMENT);
 				if (!(objElement instanceof MWindow)) {
 					return;
 				}
 
 				// Is this listener interested ?
 				final MWindow windowModel = (MWindow) objElement;
 				if (windowModel.getRenderer() != RienaWBWRenderer.this) {
 					return;
 				}
 
 				// No widget == nothing to update
 				final Shell theShell = (Shell) windowModel.getWidget();
 				if (theShell == null) {
 					return;
 				}
 
 				final String attName = (String) event.getProperty(UIEvents.EventTags.ATTNAME);
 
 				if (UIEvents.Window.X.equals(attName) || UIEvents.Window.Y.equals(attName) || UIEvents.Window.WIDTH.equals(attName)
 						|| UIEvents.Window.HEIGHT.equals(attName)) {
 					if (boundsJob == null) {
 						boundsJob = new WindowSizeUpdateJob();
 						boundsJob.windowsToUpdate.add(windowModel);
 						theShell.getDisplay().asyncExec(boundsJob);
 					} else {
 						if (!boundsJob.windowsToUpdate.contains(windowModel)) {
 							boundsJob.windowsToUpdate.add(windowModel);
 						}
 					}
 				}
 			}
 		};
 
 		eventBroker.subscribe(UIEvents.Window.TOPIC_ALL, sizeHandler);
 
 		childHandler = new EventHandler() {
 			public void handleEvent(final Event event) {
 				// Track additions/removals of the active part and keep its
 				// stack styled correctly
 				final Object changedObj = event.getProperty(UIEvents.EventTags.ELEMENT);
 				if (!(changedObj instanceof MPartStack)) {
 					return;
 				}
 				final MPartStack stack = (MPartStack) changedObj;
 
 				final String eventType = (String) event.getProperty(UIEvents.EventTags.TYPE);
 				if (UIEvents.EventTypes.ADD.equals(eventType)) {
 					final MUIElement added = (MUIElement) event.getProperty(UIEvents.EventTags.NEW_VALUE);
 					if (added == activePart) {
 						styleStack(stack, true);
 					}
 				} else if (UIEvents.EventTypes.REMOVE.equals(eventType)) {
 					Activator.trace(Policy.DEBUG_RENDERER, "Child Removed", null); //$NON-NLS-1$
 					final MUIElement removed = (MUIElement) event.getProperty(UIEvents.EventTags.OLD_VALUE);
 					if (removed == activePart) {
 						styleStack(stack, false);
 					}
 				}
 			}
 		};
 
 		eventBroker.subscribe(UIEvents.ElementContainer.TOPIC_CHILDREN, childHandler);
 	}
 
 	@PreDestroy
 	public void contextDisposed() {
 		eventBroker.unsubscribe(shellUpdater);
 		eventBroker.unsubscribe(visibilityHandler);
 		eventBroker.unsubscribe(sizeHandler);
 		eventBroker.unsubscribe(childHandler);
 	}
 
 	@Override
 	public Object createWidget(final MUIElement element, final Object parent) {
 		final Widget newWidget;
 
 		if (!(element instanceof MWindow) || (parent != null && !(parent instanceof Control))) {
 			return null;
 		}
 
 		final MWindow wbwModel = (MWindow) element;
 
 		final MApplication appModel = wbwModel.getContext().get(MApplication.class);
 		final Boolean rtlMode = (Boolean) appModel.getTransientData().get(E4Workbench.RTL_MODE);
 		final int rtlStyle = (rtlMode != null && rtlMode.booleanValue()) ? SWT.RIGHT_TO_LEFT : 0;
 
 		final Shell parentShell = parent == null ? null : ((Control) parent).getShell();
 
 		final Shell wbwShell;
 		if (parentShell == null) {
 			wbwShell = rienaCreateShell(rtlStyle, wbwModel);
 			wbwModel.getTags().add("topLevel"); //$NON-NLS-1$
 		} else if (wbwModel.getTags().contains("dragHost")) { //$NON-NLS-1$
 			wbwShell = new Shell(parentShell, SWT.BORDER | rtlStyle);
 			wbwShell.setAlpha(110);
 		} else {
 			wbwShell = new Shell(parentShell, SWT.TOOL | SWT.TITLE | SWT.RESIZE | rtlStyle);
 		}
 
 		wbwShell.setBackgroundMode(SWT.INHERIT_DEFAULT);
 
 		final Rectangle modelBounds = wbwShell.getBounds();
 		if (wbwModel instanceof EObject) {
 			final EObject wbw = (EObject) wbwModel;
 			final EClass wbwclass = wbw.eClass();
 			// use eIsSet rather than embed sentinel values
 			if (wbw.eIsSet(wbwclass.getEStructuralFeature("x"))) { //$NON-NLS-1$
 				modelBounds.x = wbwModel.getX();
 			}
 			if (wbw.eIsSet(wbwclass.getEStructuralFeature("y"))) { //$NON-NLS-1$
 				modelBounds.y = wbwModel.getY();
 			}
 			if (wbw.eIsSet(wbwclass.getEStructuralFeature("height"))) { //$NON-NLS-1$
 				modelBounds.height = wbwModel.getHeight();
 			}
 			if (wbw.eIsSet(wbwclass.getEStructuralFeature("width"))) { //$NON-NLS-1$
 				modelBounds.width = wbwModel.getWidth();
 			}
 		}
 		// Force the shell onto the display if it would be invisible otherwise
 		final Rectangle displayBounds = Display.getCurrent().getBounds();
 		if (!modelBounds.intersects(displayBounds)) {
 			final Rectangle clientArea = Display.getCurrent().getClientArea();
 			modelBounds.x = clientArea.x;
 			modelBounds.y = clientArea.y;
 		}
 		wbwShell.setBounds(modelBounds);
 
 		setCSSInfo(wbwModel, wbwShell);
 
 		// set up context
 		final IEclipseContext localContext = getContext(wbwModel);
 
 		// We need to retrieve specific CSS properties for our layout.
 		final CSSEngineHelper helper = new CSSEngineHelper(localContext, wbwShell);
 		//		final TrimmedPartLayout tl = new TrimmedPartLayout(wbwShell);
 		//		tl.gutterTop = helper.getMarginTop(0);
 		//		tl.gutterBottom = helper.getMarginBottom(0);
 		//		tl.gutterLeft = helper.getMarginLeft(0);
 		//		tl.gutterRight = helper.getMarginRight(0);
 
 		rienaCreateContents(wbwShell);
 
 		//		wbwShell.setLayout(tl);
 		newWidget = wbwShell;
 		bindWidget(element, newWidget);
 
 		// Add the shell into the WBW's context
 		localContext.set(Shell.class.getName(), wbwShell);
 		localContext.set(E4Workbench.LOCAL_ACTIVE_SHELL, wbwShell);
 		setCloseHandler(wbwModel);
 		localContext.set(IShellProvider.class.getName(), new IShellProvider() {
 			public Shell getShell() {
 				return wbwShell;
 			}
 		});
 		localContext.set(ISaveHandler.class, new PartServiceSaveHandler() {
 			@Override
 			public Save promptToSave(final MPart dirtyPart) {
 				final Shell shell = (Shell) context.get(IServiceConstants.ACTIVE_SHELL);
 				final Object[] elements = promptForSave(shell, Collections.singleton(dirtyPart));
 				if (elements == null) {
 					return Save.CANCEL;
 				}
 				return elements.length == 0 ? Save.NO : Save.YES;
 			}
 
 			@Override
 			public Save[] promptToSave(final Collection<MPart> dirtyParts) {
 				final List<MPart> parts = new ArrayList<MPart>(dirtyParts);
 				final Shell shell = (Shell) context.get(IServiceConstants.ACTIVE_SHELL);
 				final Save[] response = new Save[dirtyParts.size()];
 				final Object[] elements = promptForSave(shell, parts);
 				if (elements == null) {
 					Arrays.fill(response, Save.CANCEL);
 				} else {
 					Arrays.fill(response, Save.NO);
 					for (int i = 0; i < elements.length; i++) {
 						response[parts.indexOf(elements[i])] = Save.YES;
 					}
 				}
 				return response;
 			}
 		});
 
 		if (wbwModel.getLabel() != null) {
 			wbwShell.setText(wbwModel.getLocalizedLabel());
 		}
 
 		wbwShell.setImage(getImage(wbwModel));
 		// TODO: This should be added to the model, see bug 308494
 		wbwShell.setImages(Window.getDefaultImages());
 
 		applicationView.doInitialBinding();
 
 		return newWidget;
 	}
 
 	/**
 	 * We need a shell with the NO_TRIM style flag
 	 * 
 	 * @param rtlStyle
 	 * @return
 	 */
 	private Shell rienaCreateShell(final int rtlStyle, final MWindow modelElement) {
 		int shellStyle = rtlStyle | SWT.DOUBLE_BUFFERED;
 		if (isHideOSBorder()) {
 			shellStyle = rtlStyle | SWT.NO_TRIM;
 		} else {
 			shellStyle = rtlStyle | SWT.SHELL_TRIM;
 		}
 
 		final Shell shell = new Shell(Display.getCurrent(), shellStyle);
 		RcpUtilities.setShell(shell);
 		final Rectangle shellBounds = applicationView.initShell(shell);
 		modelElement.setX(shellBounds.x);
 		modelElement.setY(shellBounds.y);
 		modelElement.setWidth(shellBounds.width);
 		modelElement.setHeight(shellBounds.height);
		eventBroker.send(SHELL_CREATED, shell);
 		return shell;
 	}
 
 	private Boolean isHideOSBorder() {
 		return LnfManager.getLnf().getBooleanSetting(LnfKeyConstants.SHELL_HIDE_OS_BORDER);
 	}
 
 	private void rienaCreateContents(final Composite clientArea) {
 		final GridLayout layout = new GridLayout();
 		layout.marginWidth = getShellBorderWidth();
 		layout.marginHeight = getShellBorderWidth();
 		layout.horizontalSpacing = 0;
 		layout.verticalSpacing = 0;
 		clientArea.setLayout(layout);
 
 		header = new Composite(clientArea, SWT.NONE);
 		final GridData headerLayoutData = new GridData(GridData.FILL_HORIZONTAL);
 		final int headerPartHeight = SwitcherComposite.getShellPadding() + SwitcherComposite.getSwitchterHeight() + SwitcherComposite.getSwitchterTopMargin();
 		headerLayoutData.heightHint = headerPartHeight;
 		header.setLayoutData(headerLayoutData);
 		header.setLayout(new FillLayout());
 
 		mainMenu = new Composite(clientArea, SWT.NONE);
 		final GridData mainMenuLayoutData = new GridData(GridData.FILL_HORIZONTAL);
 		mainMenuLayoutData.verticalIndent = LnfManager.getLnf().getIntegerSetting(LnfKeyConstants.MENUBAR_TOP_MARGIN);
 		mainMenu.setLayoutData(mainMenuLayoutData);
 		final FillLayout mainMenuLayout = new FillLayout();
 		final Integer shellPadding = isHideOSBorder() ? LnfManager.getLnf().getIntegerSetting(LnfKeyConstants.TITLELESS_SHELL_PADDING) : 0;
 		mainMenuLayout.marginWidth = shellPadding;
 		mainMenu.setLayout(mainMenuLayout);
 
 		final Separator separator = UIControlsFactory.createSeparator(clientArea, SWT.HORIZONTAL);
 		final GridData separatorLayoutData = new GridData(GridData.FILL_HORIZONTAL);
 		separatorLayoutData.heightHint = 2;
 		separator.setLayoutData(separatorLayoutData);
 
 		mainToolBar = new Composite(clientArea, SWT.NONE);
 		final GridData mainToolBarLayoutData = new GridData(GridData.FILL_HORIZONTAL);
 		mainToolBarLayoutData.verticalIndent = LnfManager.getLnf().getIntegerSetting(LnfKeyConstants.TOOLBAR_TOP_MARGIN);
 		mainToolBar.setLayoutData(mainToolBarLayoutData);
 		final FillLayout mainToolBarLayout = new FillLayout();
 		mainToolBarLayout.marginWidth = shellPadding;
 		mainToolBar.setLayout(mainToolBarLayout);
 
 		perspectiveStack = new Composite(clientArea, SWT.NONE);
 		perspectiveStack.setLayoutData(new GridData(GridData.FILL_BOTH));
 		final FillLayout perspectiveStackLayout = new FillLayout();
 		perspectiveStackLayout.marginWidth = shellPadding;
 		perspectiveStack.setLayout(perspectiveStackLayout);
 
 		applicationView.createInfoFlyout(perspectiveStack).setPositionCorrectionY(
 				LnfManager.getLnf().getIntegerSetting(LnfKeyConstants.TOOLBAR_WORK_AREA_VERTICAL_GAP));
 
 		statusLine = new Composite(clientArea, SWT.NONE);
 		final GridData statusLayoutData = new GridData(GridData.FILL_HORIZONTAL);
 		statusLayoutData.verticalIndent = shellPadding;
 		statusLayoutData.heightHint = LnfManager.getLnf().getIntegerSetting(LnfKeyConstants.STATUSLINE_HEIGHT) + StatusLinePart.BOTTOM_OFFSET;
 		statusLine.setLayoutData(statusLayoutData);
 		statusLine.setLayout(new FillLayout());
 	}
 
 	/**
 	 * Returns the width of the shell border.
 	 * 
 	 * @return border width
 	 */
 	private static int getShellBorderWidth() {
 		final DialogBorderRenderer borderRenderer = (DialogBorderRenderer) LnfManager.getLnf().getRenderer(LnfKeyConstants.TITLELESS_SHELL_BORDER_RENDERER);
 		return borderRenderer != null ? borderRenderer.getBorderWidth() : 0;
 	}
 
 	private void setCloseHandler(final MWindow window) {
 		final IEclipseContext context = window.getContext();
 		// no direct model parent, must be a detached window
 		if (window.getParent() == null) {
 			context.set(IWindowCloseHandler.class.getName(), new IWindowCloseHandler() {
 				public boolean close(final MWindow window) {
 					return closeDetachedWindow(window);
 				}
 			});
 		} else {
 			context.set(IWindowCloseHandler.class.getName(), new IWindowCloseHandler() {
 				public boolean close(final MWindow window) {
 					final EPartService partService = (EPartService) window.getContext().get(EPartService.class.getName());
 					return partService.saveAll(true);
 				}
 			});
 		}
 	}
 
 	@Override
 	public void hookControllerLogic(final MUIElement me) {
 		super.hookControllerLogic(me);
 
 		final Widget widget = (Widget) me.getWidget();
 
 		if (widget instanceof Shell && me instanceof MWindow) {
 			final Shell shell = (Shell) widget;
 			final MWindow w = (MWindow) me;
 			shell.addControlListener(new ControlListener() {
 				public void controlResized(final ControlEvent e) {
 					// Don't store the maximized size in the model
 					if (shell.getMaximized()) {
 						return;
 					}
 
 					try {
 						ignoreSizeChanges = true;
 						w.setWidth(shell.getSize().x);
 						w.setHeight(shell.getSize().y);
 					} finally {
 						ignoreSizeChanges = false;
 					}
 				}
 
 				public void controlMoved(final ControlEvent e) {
 					// Don't store the maximized size in the model
 					if (shell.getMaximized()) {
 						return;
 					}
 
 					try {
 						ignoreSizeChanges = true;
 						w.setX(shell.getLocation().x);
 						w.setY(shell.getLocation().y);
 					} finally {
 						ignoreSizeChanges = false;
 					}
 				}
 			});
 
 			shell.addShellListener(new ShellAdapter() {
 				@Override
 				public void shellClosed(final ShellEvent e) {
 					// override the shell close event
 					e.doit = false;
 					final MWindow window = (MWindow) e.widget.getData(OWNING_ME);
 					final IWindowCloseHandler closeHandler = (IWindowCloseHandler) window.getContext().get(IWindowCloseHandler.class.getName());
 					// if there's no handler or the handler permits the close
 					// request, clean-up as necessary
 					if (closeHandler == null || closeHandler.close(window)) {
 						cleanUp(window);
 					}
 				}
 			});
 			shell.addListener(SWT.Activate, new Listener() {
 				public void handleEvent(final org.eclipse.swt.widgets.Event event) {
 					MUIElement parentME = w.getParent();
 					if (parentME instanceof MApplication) {
 						final MApplication app = (MApplication) parentME;
 						app.setSelectedElement(w);
 						w.getContext().activate();
 					} else if (parentME == null) {
 						parentME = (MUIElement) ((EObject) w).eContainer();
 						if (parentME instanceof MContext) {
 							w.getContext().activate();
 						}
 					}
 				}
 			});
 		}
 	}
 
 	private void cleanUp(final MWindow window) {
 		final Object parent = ((EObject) window).eContainer();
 		if (parent instanceof MApplication) {
 			final MApplication application = (MApplication) parent;
 			final List<MWindow> children = application.getChildren();
 			if (children.size() > 1) {
 				// not the last window, destroy and remove
 				window.setToBeRendered(false);
 				children.remove(window);
 			} else {
 				// last window, just destroy without changing the model
 				engine.removeGui(window);
 			}
 		} else if (parent != null) {
 			window.setToBeRendered(false);
 			// this is a detached window, check for parts
 			if (modelService.findElements(window, null, MPart.class, null).isEmpty()) {
 				// if no parts, remove it
 				if (parent instanceof MWindow) {
 					((MWindow) parent).getWindows().remove(window);
 				} else if (parent instanceof MPerspective) {
 					((MPerspective) parent).getWindows().remove(window);
 				}
 			}
 		}
 	}
 
 	/*
 	 * Processing the contents of a Workbench window has to take into account that there may be trim elements contained in its child list. Since the
 	 * 
 	 * @see org.eclipse.e4.ui.workbench.renderers.swt.SWTPartFactory#processContents (org.eclipse.e4.ui.model.application.MPart)
 	 */
 	@Override
 	public void processContents(final MElementContainer me) {
 		if (!(me instanceof MWindow)) {
 			return;
 		}
 		final MWindow wbwModel = (MWindow) me;
 		super.processContents(me);
 
 		// Populate the main menu
 		final IPresentationEngine renderer = (IPresentationEngine) context.get(IPresentationEngine.class.getName());
 		if (wbwModel.getMainMenu() != null) {
 			renderer.createGui(wbwModel.getMainMenu(), me.getWidget(), null);
 			final Shell shell = (Shell) me.getWidget();
 			shell.setMenuBar((Menu) wbwModel.getMainMenu().getWidget());
 		}
 
 		// create Detached Windows
 		for (final MWindow dw : wbwModel.getWindows()) {
 			renderer.createGui(dw, me.getWidget(), wbwModel.getContext());
 		}
 
 		// Populate the trim (if any)
 		if (wbwModel instanceof MTrimmedWindow) {
 			final Shell shell = (Shell) wbwModel.getWidget();
 			final MTrimmedWindow tWindow = (MTrimmedWindow) wbwModel;
 			for (final MTrimBar trimBar : tWindow.getTrimBars()) {
 				renderer.createGui(trimBar, shell, wbwModel.getContext());
 			}
 		}
 
 		rienaActivateApplicationNode();
 	}
 
 	private void rienaActivateApplicationNode() {
 		final Realm realm = SWTObservables.getRealm(Display.getCurrent());
 		Realm.runWithDefault(realm, new Runnable() {
 
 			public void run() {
 				ApplicationNodeManager.getApplicationNode().activate();
 			}
 		});
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer#getUIContainer (org.eclipse.e4.ui.model.application.MUIElement)
 	 */
 	@Override
 	public Object getUIContainer(final MUIElement element) {
 		MUIElement parent = element.getParent();
 		if (parent == null) {
 			// might be a detached window
 			parent = (MUIElement) ((EObject) element).eContainer();
 			return parent == null ? null : parent.getWidget();
 		}
 
 		final String elementId = element.getElementId();
 		if (E4XMIConstants.HEADER_PART_ID.equals(elementId)) {
 			return header;
 		}
 		if (E4XMIConstants.MAIN_MENU_PART_ID.equals(elementId)) {
 			return mainMenu;
 		}
 		if (E4XMIConstants.MAIN_TOOLBAR_PART_ID.equals(elementId)) {
 			return mainToolBar;
 		}
 		if (E4XMIConstants.STATUSLINE_PART_ID.equals(elementId)) {
 			return statusLine;
 		}
 		if (E4XMIConstants.PERSPECTIVE_STACK_ID.equals(elementId)) {
 			return perspectiveStack;
 		}
 
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.e4.ui.workbench.renderers.PartFactory#postProcess(org.eclipse .e4.ui.model.application.MPart)
 	 */
 	@Override
 	public void postProcess(final MUIElement shellME) {
 		super.postProcess(shellME);
 
 		final Shell shell = (Shell) shellME.getWidget();
 
 		// Capture the max/min state
 		final MUIElement disposeME = shellME;
 		shell.addDisposeListener(new DisposeListener() {
 			public void widgetDisposed(final DisposeEvent e) {
 				final Shell shell = (Shell) e.widget;
 				if (disposeME != null) {
 					disposeME.getTags().remove(ShellMinimizedTag);
 					disposeME.getTags().remove(ShellMaximizedTag);
 					if (shell.getMinimized()) {
 						disposeME.getTags().add(ShellMinimizedTag);
 					}
 					if (shell.getMaximized()) {
 						disposeME.getTags().add(ShellMaximizedTag);
 					}
 				}
 			}
 		});
 
 		// Apply the correct shell state
 		if (shellME.getTags().contains(ShellMaximizedTag)) {
 			shell.setMaximized(true);
 		} else if (shellME.getTags().contains(ShellMinimizedTag)) {
 			shell.setMinimized(true);
 		}
 
 		shell.layout(true);
 		if (shellME.isVisible()) {
 			shell.open();
 		} else {
 			shell.setVisible(false);
 		}
 	}
 
 	private Object[] promptForSave(final Shell parentShell, final Collection<MPart> saveableParts) {
 		final SaveablePartPromptDialog dialog = new SaveablePartPromptDialog(parentShell, saveableParts);
 		if (dialog.open() == Window.CANCEL) {
 			return null;
 		}
 
 		return dialog.getCheckedElements();
 	}
 
 	@Inject
 	private IEclipseContext context;
 
 	private void applyDialogStyles(final Control control) {
 		final IStylingEngine engine = (IStylingEngine) context.get(IStylingEngine.SERVICE_NAME);
 		if (engine != null) {
 			final Shell shell = control.getShell();
 			if (shell.getBackgroundMode() == SWT.INHERIT_NONE) {
 				shell.setBackgroundMode(SWT.INHERIT_DEFAULT);
 			}
 
 			engine.style(shell);
 		}
 	}
 
 	class SaveablePartPromptDialog extends Dialog {
 
 		private final Collection<MPart> collection;
 
 		private CheckboxTableViewer tableViewer;
 
 		private Object[] checkedElements = new Object[0];
 
 		SaveablePartPromptDialog(final Shell shell, final Collection<MPart> collection) {
 			super(shell);
 			this.collection = collection;
 		}
 
 		@Override
 		protected Control createDialogArea(Composite parent) {
 			parent = (Composite) super.createDialogArea(parent);
 
 			final Label label = new Label(parent, SWT.LEAD);
 			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
 			label.setText("Select the parts to save:"); //$NON-NLS-1$
 
 			tableViewer = CheckboxTableViewer.newCheckList(parent, SWT.SINGLE | SWT.BORDER);
 			final GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
 			data.heightHint = 250;
 			data.widthHint = 300;
 			tableViewer.getControl().setLayoutData(data);
 			tableViewer.setLabelProvider(new LabelProvider() {
 				@Override
 				public String getText(final Object element) {
 					return ((MPart) element).getLocalizedLabel();
 				}
 			});
 			tableViewer.setContentProvider(ArrayContentProvider.getInstance());
 			tableViewer.setInput(collection);
 			tableViewer.setAllChecked(true);
 
 			return parent;
 		}
 
 		@Override
 		public void create() {
 			super.create();
 			applyDialogStyles(getShell());
 		}
 
 		@Override
 		protected void okPressed() {
 			checkedElements = tableViewer.getCheckedElements();
 			super.okPressed();
 		}
 
 		public Object[] getCheckedElements() {
 			return checkedElements;
 		}
 
 	}
 
 }
