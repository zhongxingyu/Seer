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
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.riena.core.util.ListenerList;
 import org.eclipse.riena.navigation.IApplicationModel;
 import org.eclipse.riena.navigation.INavigationNode;
 import org.eclipse.riena.navigation.IPresentationProviderService;
 import org.eclipse.riena.navigation.ISubModuleNode;
 import org.eclipse.riena.navigation.listener.NavigationTreeObserver;
 import org.eclipse.riena.navigation.listener.SubModuleNodeListener;
 import org.eclipse.riena.navigation.model.PresentationProviderServiceAccessor;
 import org.eclipse.riena.navigation.model.SubModuleNode;
 import org.eclipse.riena.navigation.ui.controllers.SubModuleController;
 import org.eclipse.riena.navigation.ui.swt.presentation.SwtPresentationManagerAccessor;
 import org.eclipse.riena.navigation.ui.swt.presentation.SwtViewId;
 import org.eclipse.riena.ui.ridgets.IWindowRidget;
 import org.eclipse.riena.ui.ridgets.swt.uibinding.AbstractViewBindingDelegate;
 import org.eclipse.riena.ui.ridgets.swt.uibinding.DefaultSwtBindingDelegate;
 import org.eclipse.riena.ui.swt.EmbeddedTitleBar;
 import org.eclipse.riena.ui.swt.lnf.ILnfKeyConstants;
 import org.eclipse.riena.ui.swt.lnf.LnfManager;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Cursor;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Widget;
 import org.eclipse.ui.part.ViewPart;
 
 /**
  * Abstract implementation for a sub module view
  */
 public abstract class SubModuleView<C extends SubModuleController> extends ViewPart implements
 		INavigationNodeView<SWTModuleController, SubModuleNode> {
 
 	private static final String WINDOW_RIDGET = "windowRidget"; //$NON-NLS-1$
 
 	private Map<ISubModuleNode, C> node2Controler;
 	private AbstractViewBindingDelegate binding;
 	private C currentController;
 	private EmbeddedTitleBar title;
 	private ListenerList<IComponentUpdateListener> updateListeners;
 
 	private Composite parentComposite;
 	private Composite contentComposite;
 	private Cursor cursorWait;
 	private Cursor cursorArrow;
 
 	/**
 	 * Creates a new instance of {@code SubModuleView}.
 	 */
 	public SubModuleView() {
 		binding = createBinding();
 		node2Controler = new HashMap<ISubModuleNode, C>();
 		updateListeners = new ListenerList<IComponentUpdateListener>(IComponentUpdateListener.class);
 	}
 
 	/**
 	 * Creates a delegate for the binding of view and controller.
 	 * 
 	 * @return delegate for binding
 	 */
 	protected AbstractViewBindingDelegate createBinding() {
 		return new DefaultSwtBindingDelegate();
 	}
 
 	/**
 	 * Adds the given control to the list of the controls that will be binded.
 	 * 
 	 * @param uiControl
 	 *            - control to bind
 	 */
 	protected void addUIControl(Widget uiControl) {
 		binding.addUIControl(uiControl);
 	}
 
 	/**
 	 * Adds the given control to the list of the controls that will be binded.
 	 * 
 	 * @param uiControl
 	 *            - control to bind
 	 * @param propertyName
 	 *            - name of the property...
 	 */
 	protected void addUIControl(Widget uiControl, String propertyName) {
 		binding.addUIControl(uiControl, propertyName);
 	}
 
 	/**
 	 * Find the navigation node corresponding to the passed id
 	 * 
 	 * @param pId
 	 *            - the id to the node
 	 * @return the subModule node if found
 	 */
 	protected ISubModuleNode getSubModuleNode(String pId, String pSecondary) {
 		return SwtPresentationManagerAccessor.getManager().getNavigationNode(pId, pSecondary, ISubModuleNode.class);
 	}
 
 	/**
 	 * @return the controller
 	 */
 	public C getController() {
 		return node2Controler.get(getNavigationNode());
 	}
 
 	/**
 	 * @param controller
 	 *            the controller to set
 	 */
 	public void setController(C controller) {
 		if (node2Controler.get(getNavigationNode()) == null) {
 			node2Controler.put(getNavigationNode(), controller);
 		}
 	}
 
 	/**
 	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
 	 */
 	@Override
 	public void createPartControl(Composite parent) {
 		this.parentComposite = parent;
 		observeRoot();
 		setController(createController(getNavigationNode()));
 		setPartName(getController().getNavigationNode().getLabel());
 		Composite contentComposite = createContentComposite(parent);
 		basicCreatePartControl(contentComposite);
 		createViewFacade();
 		doBinding();
 	}
 
 	protected Composite getParentComposite() {
 		return parentComposite;
 	}
 
 	protected Composite getContentComposite() {
 		return contentComposite;
 	}
 
 	/**
 	 * Creates the composite for the content of the view. Its a container that
 	 * holds the UI controls of the view.<br>
 	 * Above this container the title bar of the view is located.
 	 * 
 	 * @param parent
 	 * @return
 	 */
 	private Composite createContentComposite(Composite parent) {
 
 		Color bgColor = LnfManager.getLnf().getColor(ILnfKeyConstants.SUB_MODULE_BACKGROUND);
 		parent.setBackground(bgColor);
 
 		parent.setLayout(new FormLayout());
 
 		title = new EmbeddedTitleBar(parent, SWT.NONE);
 		addUIControl(title, WINDOW_RIDGET);
 		title.setActive(true);
 		FormData formData = new FormData();
 		// don't show the top border of the title => -1
 		formData.top = new FormAttachment(0, -1);
 		// don't show the left border of the title => -1
 		formData.left = new FormAttachment(0, -1);
 		// don't show the top border of the title, but show the bottom
 		// border => -1
 		formData.bottom = new FormAttachment(0, title.getSize().y - 1);
 		// don't show the right (and left) border of the title => 2
 		formData.right = new FormAttachment(100, 2);
 		title.setLayoutData(formData);
 
 		contentComposite = new Composite(parent, SWT.DOUBLE_BUFFERED);
 		contentComposite.setBackground(bgColor);
 		formData = new FormData();
 		formData.top = new FormAttachment(title, 0, 0);
 		formData.left = new FormAttachment(0, 0);
 		formData.bottom = new FormAttachment(100);
 		formData.right = new FormAttachment(100);
 		contentComposite.setLayoutData(formData);
 
 		// initialize cursors
 		cursorWait = createWaitCursor();
 		cursorArrow = createArrowCursor();
 
 		return contentComposite;
 	}
 
 	protected Cursor createArrowCursor() {
 		return getSite().getShell().getDisplay().getSystemCursor(SWT.CURSOR_ARROW);
 	}
 
 	protected Cursor createWaitCursor() {
 		return getSite().getShell().getDisplay().getSystemCursor(SWT.CURSOR_WAIT);
 	}
 
 	private void observeRoot() {
 		INavigationNode<?> node = getNavigationNode();
 		while (node.getParent() != null) {
 			node = node.getParent();
 		}
 		NavigationTreeObserver navigationTreeObserver = new NavigationTreeObserver();
 		navigationTreeObserver.addListener(new MySubModuleNodeListener());
 		navigationTreeObserver.addListenerTo(node.getTypecastedAdapter(IApplicationModel.class));
 	}
 
 	protected void activate(ISubModuleNode source) {
 		SwtViewId id = SwtPresentationManagerAccessor.getManager().getSwtViewId(source);
 		if (getViewSite().getId().equals(id.getId())) {
 			doBinding();
 		}
 	}
 
 	private final class MySubModuleNodeListener extends SubModuleNodeListener {
 		@Override
 		public void activated(ISubModuleNode source) {
 			super.activated(source);
 			activate(source);
 		}
 
 		@Override
 		public void block(ISubModuleNode source, boolean block) {
 			super.block(source, block);
 			if (source.equals(getNavigationNode())) {
 				getController().blockRidgets(getController().getRidgets(), block);
 				blockView(block);
 			}
 		}
 
 	}
 
 	protected void blockView(boolean block) {
 		parentComposite.setCursor(block ? cursorWait : cursorArrow);
 		contentComposite.setEnabled(!block);
 	}
 
 	/**
 	 * Creates the content of the sub module view.
 	 * 
 	 * @param parent
 	 *            - composite for the content of the sub module view
 	 */
 	protected abstract void basicCreatePartControl(Composite parent);
 
 	/**
 	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
 	 */
 	@Override
 	public void setFocus() {
 		doBinding();
 	}
 
 	protected void createViewFacade() {
 		if (!node2Controler.containsKey(getNavigationNode())) {
 			setController(createController(getNavigationNode()));
 		}
 		binding.injectRidgets(getController());
 		if (getController().getWindowRidget() == null) {
 			getController().setWindowRidget((IWindowRidget) getController().getRidget(WINDOW_RIDGET));
 		}
 	}
 
 	protected C createController(ISubModuleNode pSubModuleNode) {
 		C controller = (C) getPresentationDefinitionService().provideController(pSubModuleNode);
 		controller.setNavigationNode(pSubModuleNode);
 		return controller;
 	}
 
 	protected IPresentationProviderService getPresentationDefinitionService() {
 		// TODO: handling if no service found ???
 		return PresentationProviderServiceAccessor.current().getPresentationProviderService();
 	}
 
 	private void doBinding() {
 		bind(getNavigationNode());
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.ui.swt.views.INavigationNodeView#
 	 *      addUpdateListener
 	 *      (org.eclipse.riena.navigation.ui.swt.views.IComponentUpdateListener)
 	 */
 	public void addUpdateListener(IComponentUpdateListener listener) {
 		updateListeners.add(listener);
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.ui.swt.views.INavigationNodeView#bind(org
 	 *      .eclipse.riena.navigation.INavigationNode)
 	 */
 	public void bind(SubModuleNode node) {
 		if (currentController != getController()) {
 			if (currentController != null) {
 				binding.unbind(currentController);
 			}
 			if ((getNavigationNode() != null) && (node2Controler.get(getNavigationNode()) == null)) {
 				createViewFacade();
 			}
 			if (getController() != null) {
 				currentController = getController();
 			}
 			binding.bind(currentController);
 			currentController.afterBind();
 			title.setActive(currentController.isActivated());
 		}
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.ui.swt.views.INavigationNodeView#
 	 *      getNavigationNode ()
 	 */
 	public SubModuleNode getNavigationNode() {
 		return (SubModuleNode) getSubModuleNode(this.getViewSite().getId(), this.getViewSite().getSecondaryId());
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.ui.swt.views.INavigationNodeView#unbind()
 	 */
 	public void unbind() {
 
 		SubModuleNode node = getNavigationNode();
 		if (node == null) {
 			return;
 		}
 
 		C controller = node2Controler.get(node);
 		if (controller != null) {
 			binding.unbind(controller);
 			node2Controler.remove(node);
 		}
 
 	}
 
 }
