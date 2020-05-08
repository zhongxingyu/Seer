 /*******************************************************************************
  * Copyright (c) 2007, 2014 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.navigation.ui.swt.views;
 
 import java.beans.Beans;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.osgi.service.log.LogService;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtensionRegistry;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.equinox.log.Logger;
 import org.eclipse.jface.window.IShellProvider;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.ISourceProvider;
 import org.eclipse.ui.part.ViewPart;
 import org.eclipse.ui.services.ISourceProviderService;
 
 import org.eclipse.riena.core.Log4r;
 import org.eclipse.riena.core.util.InvocationTargetFailure;
 import org.eclipse.riena.core.util.StringUtils;
 import org.eclipse.riena.internal.navigation.ui.swt.Activator;
 import org.eclipse.riena.internal.navigation.ui.swt.handlers.NavigationSourceProvider;
 import org.eclipse.riena.navigation.ApplicationNodeManager;
 import org.eclipse.riena.navigation.IApplicationNode;
 import org.eclipse.riena.navigation.INavigationNode;
 import org.eclipse.riena.navigation.ISubModuleNode;
 import org.eclipse.riena.navigation.NavigationNodeId;
 import org.eclipse.riena.navigation.listener.NavigationTreeObserver;
 import org.eclipse.riena.navigation.listener.SubModuleNodeListener;
 import org.eclipse.riena.navigation.model.SubModuleNode;
 import org.eclipse.riena.navigation.ui.controllers.SubModuleController;
 import org.eclipse.riena.navigation.ui.swt.presentation.SwtViewProvider;
 import org.eclipse.riena.navigation.ui.swt.presentation.stack.TitlelessStackPresentation;
 import org.eclipse.riena.ui.ridgets.IRidget;
 import org.eclipse.riena.ui.ridgets.IRidgetContainer;
 import org.eclipse.riena.ui.ridgets.SubModuleUtils;
 import org.eclipse.riena.ui.ridgets.controller.IController;
 import org.eclipse.riena.ui.ridgets.swt.uibinding.AbstractViewBindingDelegate;
 import org.eclipse.riena.ui.ridgets.swt.uibinding.DefaultSwtBindingDelegate;
 import org.eclipse.riena.ui.ridgets.swt.views.BlockHelper;
 import org.eclipse.riena.ui.swt.EmbeddedTitleBar;
 import org.eclipse.riena.ui.swt.facades.SWTFacade;
 import org.eclipse.riena.ui.swt.lnf.LnFUpdater;
 import org.eclipse.riena.ui.swt.lnf.LnfKeyConstants;
 import org.eclipse.riena.ui.swt.lnf.LnfManager;
 import org.eclipse.riena.ui.swt.utils.SWTBindingPropertyLocator;
 import org.eclipse.riena.ui.swt.utils.SWTControlFinder;
 import org.eclipse.riena.ui.swt.utils.SwtUtilities;
 import org.eclipse.riena.ui.swt.utils.WidgetIdentificationSupport;
 import org.eclipse.riena.ui.workarea.IWorkareaDefinition;
 import org.eclipse.riena.ui.workarea.WorkareaManager;
 
 /**
  * Abstract implementation for a sub module view
  */
 public abstract class SubModuleView extends ViewPart implements INavigationNodeView<ISubModuleNode> {
 	/**
 	 * @since 3.0
 	 */
 	public static final String SHARED_ID = "shared"; //$NON-NLS-1$
 	private final static Logger LOGGER = Log4r.getLogger(Activator.getDefault(), SubModuleView.class);
 	private final LnFUpdater lnfUpdater = LnFUpdater.getInstance();
 	private final static Map<SubModuleView, SubModuleNode> FALLBACK_NODES = new HashMap<SubModuleView, SubModuleNode>();
 
 	/**
 	 * The key of the SWT data property that identifies the (top) composite of a sub-module view.
 	 */
 	private static final String IS_SUB_MODULE_VIEW_COMPOSITE = "isSubModuleViewComposite"; //$NON-NLS-1$
 
 	private final AbstractViewBindingDelegate binding;
 
 	private SubModuleController currentController;
 
 	/**
 	 * This node is used when creating this ViewPart inside an RCP application. It is created with information from the extension registry, instead being
 	 * obtained from the navigation tree.
 	 * 
 	 * @see #getRCPSubModuleNode()
 	 */
 	private SubModuleNode rcpSubModuleNode;
 	/** The title bar at the top of the view. May be null if running in RCP */
 	private EmbeddedTitleBar title;
 
 	private Composite parentComposite;
 	private Composite contentComposite;
 
 	private NavigationTreeObserver navigationTreeObserver;
 
 	private NavigationSourceProvider navigationSourceProvider;
 	private SubModuleNodesListener subModuleNodeListener;
 	/**
 	 * used for the e4 migration
 	 * <p>
 	 * Setting this field to something not <code>null</code> ensures that the getNavigationNode() method returns this value. If this value is <code>null</code>
 	 * the method behaves like implemented in Riena for Eclipse 3.x
 	 */
 	private ISubModuleNode navigationNode;
 	/**
 	 * used for the e4 migration
 	 */
 	private IShellProvider shellProvider;
 	private boolean e4Runtime = false;
 
 	private final BlockHelper blockHelper;
 
 	/**
 	 * Creates a new instance of {@code SubModuleView}.
 	 */
 	public SubModuleView() {
 		blockHelper = new BlockHelper() {
 
 			@Override
 			protected IRidgetContainer getController() {
 				return SubModuleView.this.getController();
 			}
 
 			@Override
 			protected Control getParentComposite() {
 				return SubModuleView.this.getParentComposite();
 			}
 
 			@Override
 			protected boolean shouldRestoreFocus() {
 				return SubModuleView.this.shouldRestoreFocus();
 			}
 
 			@Override
 			protected IRidget getFocusRidget() {
 				return SubModuleView.this.getFocusRidget();
 			}
 
 		};
 		binding = createBinding();
 		setShellProvider(new IShellProvider() {
 			public Shell getShell() {
 				return getSite().getShell();
 			}
 		});
 	}
 
 	/**
 	 * @since 5.0
 	 */
 	public void setE4Runtime(final boolean e4Runtime) {
 		this.e4Runtime = e4Runtime;
 	}
 
 	public void addUpdateListener(final IComponentUpdateListener listener) {
 		throw new UnsupportedOperationException();
 	}
 
 	/**
 	 * @since 5.0
 	 */
 	public void bind(final ISubModuleNode node) {
 
 		// create new controller if not existent for new node
 		if ((getNavigationNode() != null) && (getController() == null)) {
 			createViewFacade();
 		}
 		// Different node?
 		if (currentController != getController()) {
 
 			//"old" node bound?
 			if (currentController != null) {
 				// old node disposed?
 				if (currentController.getNavigationNode().isDisposed() && !e4Runtime) {
 					return;
 				}
 				// unbind "old" node
 				binding.unbind(currentController);
 			}
 
 			if (getController() != null) {
 				currentController = getController();
 			}
 
 			lnfUpdater.updateUIControlColors(getContentComposite());
 
 			//bind the new controller
 			binding.bind(currentController);
 
 			//callback
 			currentController.afterBind();
 
 		}
 
 		lnfUpdater.updateUIControlsAfterBind(getContentComposite());
 
 		//TODO is this really part of the the binding of the SubModuleView? NavigationSourceProvider is Menu-specific and should be handled in a context of Menus
 		activeNodeChanged(getNavigationNode());
 		//set block state on bind
 		blockView(node.isBlocked());
 	}
 
 	private void activeNodeChanged(final INavigationNode<?> node) {
 		//TODO move logic to Menu 
 		if (navigationSourceProvider == null && getSite() != null) {
 			navigationSourceProvider = getNavigationSourceProvider();
 		}
 		if (navigationSourceProvider != null) {
 			if (!navigationSourceProvider.isDisposed()) {
 				navigationSourceProvider.activeNodeChanged(node);
 			} else {
 				navigationSourceProvider = null;
 			}
 		}
 	}
 
 	private NavigationSourceProvider getNavigationSourceProvider() {
 		final ISourceProviderService sourceProviderService = (ISourceProviderService) getSite().getService(ISourceProviderService.class);
 		if (sourceProviderService == null) {
 			return null;
 		}
 		final ISourceProvider[] sourceProviders = sourceProviderService.getSourceProviders();
 		for (final ISourceProvider sourceProvider : sourceProviders) {
 			if (sourceProvider instanceof NavigationSourceProvider) {
 				return (NavigationSourceProvider) sourceProvider;
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public void createPartControl(final Composite parent) {
 		this.parentComposite = parent;
 		//markup for SubModuleViews. The StackPresentation uses this information for layouting
 		parent.setData(IS_SUB_MODULE_VIEW_COMPOSITE, Boolean.TRUE);
 		if (!Beans.isDesignTime()) {
 			//observe the navigation model to get activity change events
 			observeRoot();
 			final SubModuleController controller = createController(getNavigationNode());
 			if (controller != null) {
 				setPartName(controller.getNavigationNode().getLabel());
 			}
 			// the content composite is where all controls of the view are placed
 			contentComposite = createContentComposite(parent);
 		} else {
 			contentComposite = parent;
 		}
 		blockHelper.registerOnContentComposite(contentComposite);
 
 		contentComposite.setData(TitlelessStackPresentation.DATA_KEY_CONTENT_COMPOSITE, true);
 		createWorkarea(contentComposite);
 
 		if (Beans.isDesignTime()) {
 			lnfUpdater.updateUIControls(getParentComposite(), true);
 		} else {
 			registerView();
 			createViewFacade();
 			doBinding();
 
 		}
 
 		if (getViewSite() != null) {
 			if (getViewSite().getSecondaryId() != null) {
 				WidgetIdentificationSupport.setIdentification(contentComposite, "subModuleView", getViewSite().getId(), getViewSite().getSecondaryId()); //$NON-NLS-1$
 			}
 		}
 
 	}
 
 	/**
 	 * @since 3.0
 	 */
 	protected void registerView() {
 		if (getViewSite() != null) {
 			final String id = getViewSite().getId();
 			final String secondaryId = getViewSite().getSecondaryId();
 			SwtViewProvider.getInstance().registerView(id, secondaryId, this);
 		}
 	}
 
 	@Override
 	public void dispose() {
 		final IApplicationNode appNode = getAppNode();
 		if (navigationTreeObserver != null && appNode != null) {
 			navigationTreeObserver.removeListenerFrom(appNode);
 		}
 		FALLBACK_NODES.remove(this);
 		destroyView();
 	}
 
 	/**
 	 * @since 3.0
 	 */
 	protected void destroyView() {
 		super.dispose();
 		if (getViewSite() != null) {
 			final String id = getViewSite().getId();
 			final String secondaryId = getViewSite().getSecondaryId();
 			SwtViewProvider.getInstance().unregisterView(id, secondaryId);
 		}
 	}
 
 	/**
 	 * @return the controller
 	 */
 	public SubModuleController getController() {
 		if (getNavigationNode() != null && getNavigationNode().getNavigationNodeController() instanceof SubModuleController) {
 			return (SubModuleController) getNavigationNode().getNavigationNodeController();
 		}
 		return null;
 	}
 
 	/**
 	 * @since 5.0
 	 */
 	public ISubModuleNode getNavigationNode() {
 		if (navigationNode != null) {
 			return navigationNode;
 		}
 
 		if (getViewSite() == null) {
 			return getFallbackNavigationNode();
 		}
 
 		final String viewId = this.getViewSite().getId();
 		final String secondaryId = this.getViewSite().getSecondaryId();
 		SubModuleNode result = (SubModuleNode) getSubModuleNode(viewId, secondaryId);
 		if (result == null) {
 			result = getRCPSubModuleNode();
 		}
 		if (result == null) {
 			result = getFallbackNavigationNode();
 		}
 		return result;
 	}
 
 	/**
 	 * Important: This method is NOT API. It is used for the e4 migration only.
 	 * 
 	 * @param node
 	 * @since 5.0
 	 * @see SubModuleView#navigationNode
 	 */
 	public void setNavigationNode(final ISubModuleNode navigationNode) {
 		this.navigationNode = navigationNode;
 	}
 
 	/**
 	 * This implementation will automatically focus on the control that had previously the focus, or, the first focusable control.
 	 * <p>
 	 * You may overwrite it, but it typically is not necessary to do so. If you still want to use the 'restore focus to last control' functionality, check
 	 * {@link #canRestoreFocus()} and the invoke this method.
 	 */
 	@Override
 	public void setFocus() {
 		blockHelper.setFocus();
 	}
 
 	private IRidget getFocusRidget() {
 		return currentController != null ? currentController.getFocusableRidget() : null;
 	}
 
 	public void unbind() {
 		final SubModuleController controller = getController();
 		if (controller != null) {
 			binding.unbind(controller);
 		}
 	}
 
 	/**
 	 * Adds the given control to the list of the controls that will be binded.
 	 * 
 	 * @param uiControl
 	 *            control to bind
 	 */
 	protected void addUIControl(final Object uiControl) {
 		binding.addUIControl(uiControl);
 	}
 
 	/**
 	 * Adds the given control to the list of the controls that will be binded.
 	 * 
 	 * @param uiControl
 	 *            control to bind
 	 * @param bindingId
 	 *            ID for binding
 	 */
 	protected void addUIControl(final Object uiControl, final String bindingId) {
 		binding.addUIControl(uiControl, bindingId);
 	}
 
 	/**
 	 * Is called by the SubModuleView after {@link #basicCreatePartControl(Composite)}
 	 * 
 	 * @param parent
 	 * @since 1.2
 	 */
 	protected void afterBasicCreatePartControl(final Composite parent) {
 
 	}
 
 	/**
 	 * Creates the content of the sub module view.
 	 * 
 	 * @param parent
 	 *            composite for the content of the sub module view
 	 */
 	protected abstract void basicCreatePartControl(Composite parent);
 
 	protected void blockView(final boolean block) {
 		blockHelper.setBlocked(block);
 	}
 
 	/**
 	 * @since 5.0
 	 */
 	protected IShellProvider getShellProvider() {
 		return shellProvider;
 	}
 
 	private void setShellProvider(final IShellProvider shellProvider) {
 		this.shellProvider = shellProvider;
 	}
 
 	protected final boolean canRestoreFocus() {
 		return blockHelper.canRestoreFocus();
 	}
 
 	/**
 	 * @since 3.0
 	 */
 	protected boolean shouldRestoreFocus() {
 		if (getController() != null && getController().getModuleController() instanceof SWTModuleController) {
			return !((SWTModuleController) getController().getModuleController()).getTree().hasFocus() && getController().isActivated();
 		} else {
 			return true;
 		}
 	}
 
 	/**
 	 * Creates a delegate for the binding of view and controller.
 	 * 
 	 * @return delegate for binding
 	 */
 	protected AbstractViewBindingDelegate createBinding() {
 		return new DefaultSwtBindingDelegate();
 	}
 
 	protected SubModuleController createController(final ISubModuleNode node) {
 
 		// check node itself for controller definition first
 		Assert.isNotNull(node, "navigation node must not be null"); //$NON-NLS-1$
 		Assert.isNotNull(node.getNodeId(), "navigation node id must not be null"); //$NON-NLS-1$
 		Assert.isNotNull(node.getNodeId().getTypeId(), "navigation node type id must not be null"); //$NON-NLS-1$
 
 		SubModuleController controller = null;
 		if (!SubModuleUtils.isPrepareView()) {
 			controller = getController();
 		}
 		if (controller == null) {
 			// consult workarea manager
 			final IWorkareaDefinition def = WorkareaManager.getInstance().getDefinition(node);
 			if (def != null) {
 				try {
 					controller = (SubModuleController) def.createController();
 				} catch (final Exception ex) {
 					final String message = String.format("cannnot create controller for class %s", def.getControllerClass()); //$NON-NLS-1$ 
 					LOGGER.log(LogService.LOG_ERROR, message, ex);
 					throw new InvocationTargetFailure(message, ex);
 				}
 			}
 		}
 		if (controller != null) {
 			controller.setNavigationNode(node);
 		}
 
 		return controller;
 	}
 
 	protected void createViewFacade() {
 		// add controls of parent
 		addUIControls(getParentComposite());
 		if (getController() == null) {
 			createController(getNavigationNode());
 		}
 
 		//only bind if controller is available
 		if (getController() != null) {
 			binding.injectRidgets(getController());
 		}
 	}
 
 	/**
 	 * Creates the workarea. Subclasses can override this method to get full control over the workarea layout.
 	 * 
 	 * @param parent
 	 * @since 1.2
 	 */
 	protected void createWorkarea(final Composite parent) {
 		basicCreatePartControl(parent);
 		afterBasicCreatePartControl(parent);
 	}
 
 	protected Composite getContentComposite() {
 		return contentComposite;
 	}
 
 	protected Composite getParentComposite() {
 		// the composite created by the Workbench given to the ViewPart
 		return parentComposite;
 	}
 
 	/**
 	 * Find the navigation node corresponding to the passed ids. If the view is shared the {@link INavigationNode#isActivated()} state has to be considered
 	 * because there can be multiple nodes matching the nodeId and secondaryId. Only the active node counts!
 	 * 
 	 * @param nodeId
 	 *            the id of the node
 	 * @param secondaryId
 	 *            the secondary id
 	 * @return the subModule node if found
 	 */
 	protected ISubModuleNode getSubModuleNode(final String nodeId, final String secondaryId) {
 		final boolean ignoreSharedState = secondaryId == null || !secondaryId.startsWith(SubModuleView.SHARED_ID);
 		return SwtViewProvider.getInstance().getNavigationNode(nodeId, secondaryId, ISubModuleNode.class, ignoreSharedState);
 	}
 
 	// helping methods
 	//////////////////
 
 	private void addMenuControl(final Menu menu) {
 		final SWTBindingPropertyLocator locator = SWTBindingPropertyLocator.getInstance();
 		for (int i = 0; i < menu.getItemCount(); i++) {
 			final MenuItem item = menu.getItem(i);
 			final String bindingId = locator.locateBindingProperty(item);
 			if (StringUtils.isGiven(bindingId)) {
 				addUIControl(item, bindingId);
 			}
 			if (item.getMenu() != null) {
 				addMenuControl(item.getMenu());
 			}
 		}
 	}
 
 	/**
 	 * @since 3.0
 	 */
 	protected void addUIControls(final Composite composite) {
 		final SWTControlFinder finder = new SWTControlFinder(composite) {
 			@Override
 			public void handleBoundControl(final Control control, final String bindingProperty) {
 				addUIControl(control);
 			}
 
 			@Override
 			public void handleControl(final Control control) {
 				if (control.getMenu() != null) {
 					addMenuControl(control.getMenu());
 				}
 				super.handleControl(control);
 			}
 		};
 		finder.run();
 	}
 
 	/**
 	 * Creates the composite for the content of the view. Its a container that holds the UI controls of the view.<br>
 	 * Above this container the title bar of the view is located.
 	 * 
 	 * @param parent
 	 * @return
 	 */
 	private Composite createContentComposite(final Composite parent) {
 		final Color bgColor = LnfManager.getLnf().getColor(LnfKeyConstants.SUB_MODULE_BACKGROUND);
 		parent.setBackground(bgColor);
 
 		parent.setLayout(new FormLayout());
 
 		if (!isRCP()) {
 			title = new EmbeddedTitleBar(parent, SWT.NONE);
 			addUIControl(title, SubModuleController.WINDOW_RIDGET);
 			// as the view is only shown if the node is active we should never have to change the "window active" state of the titlebar
 			title.setWindowActive(true);
 			final FormData formData = new FormData();
 			// don't show the top border of the title => -1
 			formData.top = new FormAttachment(0, -1);
 			// don't show the left border of the title => -1
 			formData.left = new FormAttachment(0, -1);
 			// don't show the top border of the title, but show the bottom
 			// border => -1
 			formData.bottom = new FormAttachment(0, title.getSize().y - 1);
 			// don't show the right border of the title => 1
 			formData.right = new FormAttachment(100, 1);
 			title.setLayoutData(formData);
 			SWTFacade.getDefault().createEmbeddedTitleBarToolTip(title);
 		}
 
 		final Composite composite = new Composite(parent, SWT.DOUBLE_BUFFERED);
 		composite.setBackground(bgColor);
 		final FormData formData = new FormData();
 		if (title != null) {
 			formData.top = new FormAttachment(title, 0, 0);
 		} else {
 			formData.top = new FormAttachment(0, -1);
 		}
 		formData.left = new FormAttachment(0, 0);
 		formData.bottom = new FormAttachment(100);
 		formData.right = new FormAttachment(100);
 		composite.setLayoutData(formData);
 
 		return composite;
 	}
 
 	private void doBinding() {
 		bind(getNavigationNode());
 	}
 
 	/**
 	 * @since 3.0
 	 */
 	protected IApplicationNode getAppNode() {
 		//use the ApplicationNodeManager API
 		return ApplicationNodeManager.getApplicationNode();
 	}
 
 	/**
 	 * @return a fallback navigation node for views that are not associated with a node in the navigation tree.
 	 */
 	private SubModuleNode getFallbackNavigationNode() {
 		SubModuleNode fallbackNode = FALLBACK_NODES.get(this);
 		if (fallbackNode == null) {
 			fallbackNode = new SubModuleNode(new NavigationNodeId(getClass().getName() + FALLBACK_NODES.size()));
 			FALLBACK_NODES.put(this, fallbackNode);
 		}
 		return fallbackNode;
 	}
 
 	private SubModuleNode getRCPSubModuleNode() {
 		final IExtensionRegistry registry = Platform.getExtensionRegistry();
 		final IConfigurationElement[] elements = registry.getConfigurationElementsFor("org.eclipse.riena.navigation.assemblies"); //$NON-NLS-1$
 		final String viewId = getViewSite().getId();
 
 		return getRCPSubModuleNode(viewId, elements);
 	}
 
 	private SubModuleNode getRCPSubModuleNode(final String viewId, final IConfigurationElement[] elements) {
 		for (int i = 0; rcpSubModuleNode == null && i < elements.length; i++) {
 			final IConfigurationElement element = elements[i];
 			if ("submodule".equals(element.getName())) { //$NON-NLS-1$
 				final String view = element.getAttribute("view"); //$NON-NLS-1$
 				if (viewId.equals(view)) {
 					final String typeId = element.getAttribute("typeId"); //$NON-NLS-1$
 					if (typeId != null) {
 						rcpSubModuleNode = new SubModuleNode(new NavigationNodeId(typeId), getPartName());
 					}
 				}
 			} else if (element.getChildren().length > 0) {
 				rcpSubModuleNode = getRCPSubModuleNode(viewId, element.getChildren());
 			}
 		}
 		return rcpSubModuleNode;
 	}
 
 	/**
 	 * Returns true if we are running without the navigation tree
 	 */
 	private boolean isRCP() {
 		//TODO: refactor testing for RCP
 		getNavigationNode();
 		return rcpSubModuleNode != null;
 	}
 
 	private void observeRoot() {
 		final IApplicationNode appNode = getAppNode();
 		if (appNode != null) {
 			Assert.isLegal(navigationTreeObserver == null);
 			navigationTreeObserver = new NavigationTreeObserver();
 			subModuleNodeListener = new SubModuleNodesListener();
 			navigationTreeObserver.addListener(subModuleNodeListener);
 			navigationTreeObserver.addListenerTo(appNode);
 		}
 	}
 
 	// helping classes
 	//////////////////
 
 	/**
 	 * @since 3.0
 	 */
 	protected String getSecondaryId() {
 		return getViewSite().getSecondaryId();
 	}
 
 	/**
 	 * @since 3.0
 	 */
 	protected void unbindActiveController() {
 		//unbind
 		binding.unbind(currentController);
 		//reset controller
 		currentController = null;
 	}
 
 	/**
 	 * A listener for all submodules in the navigation tree! Needed i.e. to support shared views. When adding a method be sure to check the node.
 	 */
 	private final class SubModuleNodesListener extends SubModuleNodeListener {
 
 		@Override
 		public void activated(final ISubModuleNode source) {
 			if (source.equals(getNavigationNode())) {
 				if (SwtUtilities.isDisposed(parentComposite)) {
 					/*
 					 * Do not bind disposed views. TODO For disposed views this listener should be unregistered.
 					 */
 					return;
 				}
 				doBinding();
 			}
 		}
 
 		@Override
 		public void beforeDisposed(final ISubModuleNode source) {
 			/*
 			 * If source is the current bound node then unbind the controller. If the node is not bound (not the current) we do not have to unbind anything. In
 			 * the case of detached views there�s no viewSite available!
 			 */
 			if (getViewSite() != null && disposingBoundNode(source)) {
 				unbindActiveController();
 			}
 		}
 
 		protected boolean disposingBoundNode(final ISubModuleNode source) {
 			/*
 			 * First check if typeId fits. Then check if source is the current node.
 			 */
 			return getSecondaryId().equals(SHARED_ID) && currentController != null && source.equals(currentController.getNavigationNode());
 		}
 
 		@Override
 		public void block(final ISubModuleNode source, final boolean block) {
 			if (source.equals(getNavigationNode())) {
 				blockView(block);
 			}
 		}
 
 		@Override
 		public void nodeIdChange(final ISubModuleNode source, final NavigationNodeId oldId, final NavigationNodeId newId) {
 			if (source.equals(getNavigationNode())) {
 				//TODO: is this the right place for changing nodeId? There is no view-specific logic
 				SwtViewProvider.getInstance().replaceNavigationNodeId(source, oldId, newId);
 			}
 		}
 	}
 
 	/**
 	 * Triggered by "prepareNode" for nodes to be prepared which already have an instantiated view. In those cases createPartControl is not called.
 	 * 
 	 * @since 5.0
 	 */
 	public void prepareNode(final ISubModuleNode node) {
 		final IController controller = createController(node);
 		if (controller != null) {
 			binding.injectRidgets(controller);
 		}
 	}
 }
