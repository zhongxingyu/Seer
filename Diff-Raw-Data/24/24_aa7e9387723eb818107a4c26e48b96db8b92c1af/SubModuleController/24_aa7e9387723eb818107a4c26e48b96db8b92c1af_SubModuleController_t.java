 /*******************************************************************************
  * Copyright (c) 2007, 2013 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.navigation.ui.controllers;
 
 import java.util.Collection;
 import java.util.Iterator;
 
 import org.osgi.service.log.LogService;
 
 import org.eclipse.core.databinding.BindingException;
 import org.eclipse.equinox.log.Logger;
 
 import org.eclipse.riena.core.Log4r;
 import org.eclipse.riena.core.util.RienaConfiguration;
 import org.eclipse.riena.internal.ui.ridgets.Activator;
 import org.eclipse.riena.navigation.ApplicationNodeManager;
 import org.eclipse.riena.navigation.IApplicationNode;
 import org.eclipse.riena.navigation.IHierarchyChangeListener;
 import org.eclipse.riena.navigation.IModuleNode;
 import org.eclipse.riena.navigation.INavigationNode;
 import org.eclipse.riena.navigation.ISubModuleNode;
 import org.eclipse.riena.navigation.listener.SubModuleNodeListener;
 import org.eclipse.riena.navigation.model.SubModuleNode;
 import org.eclipse.riena.ui.ridgets.AbstractRidget;
 import org.eclipse.riena.ui.ridgets.IActionRidget;
 import org.eclipse.riena.ui.ridgets.IComplexRidget;
 import org.eclipse.riena.ui.ridgets.IDefaultActionManager;
 import org.eclipse.riena.ui.ridgets.IEmbeddedTitleBarRidget;
 import org.eclipse.riena.ui.ridgets.IInfoFlyoutRidget;
 import org.eclipse.riena.ui.ridgets.IMarkableRidget;
 import org.eclipse.riena.ui.ridgets.IRidget;
 import org.eclipse.riena.ui.ridgets.IRidgetContainer;
 import org.eclipse.riena.ui.ridgets.IStatuslineRidget;
 import org.eclipse.riena.ui.ridgets.IWindowRidget;
 import org.eclipse.riena.ui.ridgets.RidgetToStatuslineSubscriber;
 import org.eclipse.riena.ui.ridgets.controller.AbstractWindowController;
 import org.eclipse.riena.ui.ridgets.listener.IWindowRidgetListener;
 
 /**
  * Default implementation for a SubModuleController.
  */
 public class SubModuleController extends NavigationNodeController<ISubModuleNode> {
 
 	private static final Logger LOGGER = Log4r.getLogger(Activator.getDefault(), SubModuleController.class);
 
 	/**
 	 * The ID of the window ridget in this controller ("windowRidget").
 	 */
 	public static final String WINDOW_RIDGET = "windowRidget"; //$NON-NLS-1$
 
 	private static final String TITLE_SEPARATOR = " - "; //$NON-NLS-1$
 
 	private IDefaultActionManager actionManager;
 	/**
 	 * The ridget the should get the focus, the very first time this controller is activated. May be null.
 	 */
 	private IRidget initialFocus;
 
 	private final WindowListener windowListener;
 
	/**
	 * <code>true</code> when the title has to be computed upon next activation
	 * 
	 * @since 5.0
	 */
	protected boolean titleInvalid;

 	public SubModuleController() {
 		this(null);
 	}
 
 	public SubModuleController(final ISubModuleNode navigationNode) {
 		super(navigationNode);
 		windowListener = new WindowListener();
 	}
 
 	/**
 	 * Make {@code action} the default action while the focus is within {@code focusRidget} including it's children.
 	 * <p>
 	 * If a default action is available and enabled, it will be invoked whenever the user presses ENTER within the window. The mapping is enabled when the
 	 * navigation node for this controller becomes active. It is disabled when the navigation node for this controller becomes inactive.
 	 * <p>
 	 * Note: the algorithm stops at the first match. It will check the most specific (innermost) ridget first and check the most general (outremost) ridget
 	 * last.
 	 * 
 	 * @param focusRidget
 	 *            the ridget that needs to have the focus to activate this rule. Never null.
 	 * @param action
 	 *            this ridget will become the default action, while focusRidget has the focus. Never null.
 	 * 
 	 * @since 2.0
 	 */
 	public void addDefaultAction(final IRidget focusRidget, final IActionRidget action) {
 		actionManager = getWindowRidget().addDefaultAction(focusRidget, action);
 
 		// activate() can only be called, if the shell is present
 		if (actionManager != null && getWindowRidget().getUIControl() != null) {
 			actionManager.deactivate();
 			actionManager.activate();
 		}
 	}
 
 	@Override
 	public void afterBind() {
 		super.afterBind();
 		updateIcon();
 		updateWindowTitle();
 		updateCloseable();
 		updateActive();
 		if (getWindowRidget() != null) {
 			getWindowRidget().addWindowRidgetListener(windowListener);
 		}
 		initializeStatuslineMessageViewer();
 	}
 
 	private void initializeStatuslineMessageViewer() {
 		if (shouldEnableStatuslineMessageViewer()) {
 			setStatuslineToShowMarkerMessages(getStatusline());
 		} else {
 			setStatuslineToShowMarkerMessages(null);
 		}
 	}
 
 	/**
 	 * Controls if the ridget messages from this controller are displayed in the status line.
 	 * <p>
 	 * This behavior is:
 	 * <ul>
 	 * <li>disabled by default,
 	 * <li>can be defined globally by setting '<code>riena.showRidgetMessagesInStatusline=true</code>' using extension point
 	 * <tt>org.eclipse.riena.core.configuration</tt>,
 	 * <li>can be defined individually (overriding the global setting) for this controller using this method.
 	 * </ul>
 	 * <p>
 	 * This method will be called by {@link #afterBind()} <strong>after</strong> {@link #configureRidgets()} was executed.
 	 * 
 	 * @return <code>true</code> if the ridget messages should be displayed in the status line
 	 * @since 5.0
 	 * @see IRidgetContainer#setStatuslineToShowMarkerMessages(IStatuslineRidget)
 	 * @see AbstractWindowController#shouldEnableStatuslineMessageViewer()
 	 */
 	protected boolean shouldEnableStatuslineMessageViewer() {
 		final String value = RienaConfiguration.getInstance().getProperty(RidgetToStatuslineSubscriber.SHOW_RIDGET_MESSAGES_IN_STATUSLINE_KEY);
 		return Boolean.parseBoolean(value);
 	}
 
 	/**
 	 * Subclasses should override to configure their ridget.
 	 * <p>
 	 * {@inheritDoc}
 	 */
 	public void configureRidgets() {
 		// unused
 	}
 
 	/**
 	 * Returns the ridget that should get the focus, when this controller's view is opened for the first time. If null, then the first focusable widget will get
 	 * the focus (i.e. same behavior as in standard RCP).
 	 * <p>
 	 * The default value is null.
 	 * 
 	 * @return a IRidget instance or null.
 	 * @since 2.0
 	 */
 	public IRidget getInitialFocus() {
 		return initialFocus;
 	}
 
 	/**
 	 * Returns the ridget that should get the focus. If a ridget is set via {@see setInitialFocus}, it will be returned. Otherwise a ridget which can receive
 	 * the focus is searched, if none is found null is returned.
 	 * 
 	 * @return a IRidget instance or null.
 	 * @since 4.0
 	 */
 	public IRidget getFocusableRidget() {
 		if (getInitialFocus() != null) {
 			return getInitialFocus();
 		}
 		for (final IRidget ridget : getRidgets()) {
 			final boolean markable = ridget instanceof IMarkableRidget;
 			if (ridget.isFocusable() && ridget.isEnabled() && ridget.isVisible() && (!markable || (markable && !((IMarkableRidget) ridget).isOutputOnly()))) {
 				return ridget;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the controller of the parent module.
 	 * 
 	 * @return module controller or {@code null} if not parent module controller exists.
 	 */
 	public ModuleController getModuleController() {
 		final IModuleNode moduleNode = getNavigationNode().getParentOfType(IModuleNode.class);
 		if (moduleNode != null) {
 			return (ModuleController) moduleNode.getNavigationNodeController();
 		}
 		return null;
 	}
 
 	/**
 	 * @return the windowRidget
 	 */
 	public IWindowRidget getWindowRidget() {
 		return getRidget(IEmbeddedTitleBarRidget.class, WINDOW_RIDGET);
 	}
 
 	/**
 	 * Returns the {@link IInfoFlyoutRidget} for this sub module.
 	 * 
 	 * @return an {@link IInfoFlyoutRidget}; never null
 	 * @since 2.0
 	 */
 	public IInfoFlyoutRidget getInfoFlyout() {
 		final ApplicationController appController = (ApplicationController) ApplicationNodeManager.getApplicationNode().getNavigationNodeController();
 		return appController.getInfoFlyout();
 	}
 
 	/**
 	 * Set the ridget that should get the focus, when this controller's view is opened for the first time.
 	 * <p>
 	 * If the value is null or if the ridget cannot receive the focus (because it is not enabled / not visible / not focusable) then the first ridget/widget
 	 * that can receive the focus will get the focus (i.e. same behavior as in standard RCP).
 	 * 
 	 * @param ridget
 	 *            an IRidget instance or null
 	 * 
 	 * @since 2.0
 	 */
 	public void setInitialFocus(final IRidget ridget) {
 		this.initialFocus = ridget;
 	}
 
 	@Override
 	public void setBlocked(final boolean blocked) {
 		super.setBlocked(blocked);
 		restoreFocusRequestFromRidget(getRidgets());
 	}
 
 	/**
 	 * Checks all ridgets recursively in this controller, if a previous call to setFocus() failed and tries to set the focus again.
 	 * <p>
 	 * SWT has the limitation that it doesn't set the focus if the parent composite is disabled. Therefore we have to try to restore the first previous call to
 	 * setFocus(), while the view is blocked.
 	 * 
 	 * @param collection
 	 *            the collection to check
 	 * @since 4.0
 	 */
 	public void restoreFocusRequestFromRidget(final Collection<? extends IRidget> collection) {
 		for (final IRidget ridget : collection) {
 			if (ridget instanceof IComplexRidget) {
 				restoreFocusRequestFromRidget(((IComplexRidget) ridget).getRidgets());
 			} else {
 				if (ridget instanceof AbstractRidget) {
 					if (((AbstractRidget) ridget).isRetryRequestFocus()) {
 						if (!getNavigationNode().isBlocked()) {
 							ridget.requestFocus();
 						}
 						((AbstractRidget) ridget).setRetryRequestFocus(false);
 					}
 				}
 			}
 		}
 	}
 
 	@Override
 	public void setNavigationNode(final ISubModuleNode navigationNode) {
 		super.setNavigationNode(navigationNode);
 
 		getNavigationNode().addListener(new SubModuleNodeListener() {
 			@Override
 			public void iconChanged(final ISubModuleNode source) {
 				updateIcon();
 			}
 
 			@Override
 			public void labelChanged(final ISubModuleNode subModuleNode) {
 				updateWindowTitle();
 			}
 
 			@Override
 			public void afterActivated(final ISubModuleNode source) {
				if (titleInvalid) {
					updateWindowTitle();
				}

 				if (actionManager != null) {
 					actionManager.activate();
 				}
 			}
 
 			@Override
 			public void afterDeactivated(final ISubModuleNode source) {
 				if (actionManager != null) {
 					actionManager.deactivate();
 				}
 			}
 
 			@Override
 			public void beforeDisposed(final ISubModuleNode source) {
 				if (actionManager != null) {
 					actionManager.dispose();
 					actionManager = null;
 				}
 			}
 		});
 
 		if (navigationNode instanceof SubModuleNode) {
 			((SubModuleNode) navigationNode).addHierarchyChangeListener(new IHierarchyChangeListener() {
 				@Override
 				public void labelChanged(final INavigationNode<?> node) {
					if (isActivated()) {
						// if node is active, we have to update the title now
						updateWindowTitle();
					} else {
						// otherwise we just invalidate the title, so it can be recomputed on next activation
						titleInvalid = true;
					}
					//					updateWindowTitle();
 				}
 			});
 		}
 	}
 
 	/**
 	 * @param windowRidget
 	 *            the windowRidget to set
 	 */
 	public void setWindowRidget(final IWindowRidget windowRidget) {
 		if (getWindowRidget() != windowRidget) {
 			addRidget(WINDOW_RIDGET, windowRidget);
 		}
 	}
 
 	/**
 	 * calls updateFromModel for all registered ridgets in this controller
 	 */
 	public void updateAllRidgetsFromModel() {
 		final Iterator<? extends IRidget> iter = getRidgets().iterator();
 		while (iter.hasNext()) {
 			final IRidget ridget = iter.next();
 			if (ridget.isIgnoreBindingError()) {
 				try {
 					ridget.updateFromModel();
 				} catch (final BindingException ex) {
 					String message = "Update from the model was unsuccessful for the ridget: "; //$NON-NLS-1$
 					message += ridget + ", with id: " + ridget.getID(); //$NON-NLS-1$
 					LOGGER.log(LogService.LOG_DEBUG, message);
 				}
 			} else {
 				ridget.updateFromModel();
 			}
 		}
 	}
 
 	/**
 	 * Returns the full title of this sub-module.
 	 * <p>
 	 * The title is made up from the label of parent module node and all parent sub-module nodes. One exception exists: If the module has one sub-module (and
 	 * this is not visible in the tree), then only the label of the module is returned.
 	 * 
 	 * @return full title of the sub-module
 	 */
 	protected String getFullTitle() {
 		String title = getNavigationNode().getLabel();
 
 		if (isInvisibleInTree()) {
 			final IModuleNode moduleNode = getModuleController().getNavigationNode();
 			title = moduleNode.getLabel();
 		} else {
 			INavigationNode<?> parent = getNavigationNode().getParent();
 			if (parent != null) {
 				while (parent != null && !(parent instanceof IModuleNode)) {
 					title = parent.getLabel() + TITLE_SEPARATOR + title;
 					parent = parent.getParent();
 				}
 				title = parent.getLabel() + TITLE_SEPARATOR + title;
 			}
 		}
 		return title;
 	}
 
 	/**
 	 * Re-layout all contents.
 	 * 
 	 * @since 1.2
 	 */
 	protected void layout() {
 		final IWindowRidget ridget = getRidget(IEmbeddedTitleBarRidget.class, WINDOW_RIDGET);
 		ridget.layout();
 	}
 
 	@Override
 	protected void updateIcon(final IWindowRidget windowRidget) {
 		if (isInvisibleInTree()) {
 			if (windowRidget == null) {
 				return;
 			}
 			final IModuleNode moduleNode = getModuleController().getNavigationNode();
 			final String nodeIcon = moduleNode.getIcon();
 			windowRidget.setIcon(nodeIcon);
 		} else {
 			super.updateIcon(windowRidget);
 		}
 	}
 
 	/**
 	 * Returns the status line ridget.
 	 * 
 	 * @return the status line ridget or <code>null</code> if no status line exists
 	 * @since 4.0
 	 */
 	protected IStatuslineRidget getStatusline() {
 		final ApplicationController applicationController = getApplicationController();
 		if (applicationController == null) {
 			return null;
 		}
 		return applicationController.getStatusline();
 	}
 
 	private ApplicationController getApplicationController() {
 		final IApplicationNode application = getNavigationNode().getParentOfType(IApplicationNode.class);
 		if (application == null) {
 			return null;
 		}
 		return (ApplicationController) application.getNavigationNodeController();
 	}
 
 	/**
 	 * Returns whether the sub-module is hidden in the navigation tree. A sub-module is hidden if it is the only child of a module unless its parent is
 	 * configured to show a single child.
 	 * 
 	 * @return {@code true} if there is a navigation tree but the sub-module is not shown; otherwise {@code false}
 	 */
 	private boolean isInvisibleInTree() {
 		if (getModuleController() != null && getModuleController().hasSingleLeafChild()) {
 			return !getModuleController().getNavigationNode().isPresentSingleSubModule();
 		}
 		return false;
 	}
 
 	private void updateActive() {
 		final IWindowRidget windowRidget = getWindowRidget();
 		if (windowRidget != null) {
 			windowRidget.setActive(getNavigationNode().isActivated());
 		}
 	}
 
 	private void updateCloseable() {
 		final IWindowRidget windowRidget = getWindowRidget();
 		if (windowRidget != null) {
 			windowRidget.setCloseable(getNavigationNode().isClosable());
 		}
 	}
 
 	private void updateIcon() {
 		updateIcon(getWindowRidget());
 	}
 
 	void updateWindowTitle() {
 		final IWindowRidget windowRidget = getWindowRidget();
 		if (windowRidget != null) {
 			windowRidget.setTitle(getFullTitle());
 		}
 		if (!getNavigationNode().isActivated()) {
 			final ISubModuleNode subModule = ApplicationNodeManager.locateActiveSubModuleNode();
 			if ((subModule != null) && (subModule.getNavigationNodeController() instanceof SubModuleController)) {
 				((SubModuleController) subModule.getNavigationNodeController()).updateWindowTitle();
 			}
 		}
		titleInvalid = false;
 	}
 
 	private class WindowListener implements IWindowRidgetListener {
 
 		public void closed() {
 			getNavigationNode().dispose();
 		}
 
 		public void activated() {
 
 		}
 
 	}
 }
