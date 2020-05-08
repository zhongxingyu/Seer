 /*******************************************************************************
  * Copyright (c) 2007, 2009 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.navigation.ui.swt.views;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.TreeItem;
 
 import org.eclipse.riena.core.marker.IMarker;
 import org.eclipse.riena.core.util.ListenerList;
 import org.eclipse.riena.navigation.IModuleNode;
 import org.eclipse.riena.navigation.INavigationNode;
 import org.eclipse.riena.navigation.ISubModuleNode;
 import org.eclipse.riena.navigation.listener.ModuleNodeListener;
 import org.eclipse.riena.navigation.listener.NavigationTreeObserver;
 import org.eclipse.riena.navigation.listener.SubModuleNodeListener;
 import org.eclipse.riena.navigation.model.ModuleNode;
 import org.eclipse.riena.navigation.model.SubModuleNode;
 import org.eclipse.riena.navigation.ui.swt.binding.InjectSwtViewBindingDelegate;
 import org.eclipse.riena.navigation.ui.swt.component.ModuleToolTip;
 import org.eclipse.riena.navigation.ui.swt.component.SubModuleToolTip;
 import org.eclipse.riena.navigation.ui.swt.lnf.renderer.ModuleGroupRenderer;
 import org.eclipse.riena.navigation.ui.swt.lnf.renderer.SubModuleTreeItemMarkerRenderer;
 import org.eclipse.riena.ui.ridgets.controller.IController;
 import org.eclipse.riena.ui.ridgets.swt.uibinding.AbstractViewBindingDelegate;
 import org.eclipse.riena.ui.swt.ModuleTitleBar;
 import org.eclipse.riena.ui.swt.lnf.LnFUpdater;
 import org.eclipse.riena.ui.swt.lnf.LnfKeyConstants;
 import org.eclipse.riena.ui.swt.lnf.LnfManager;
 import org.eclipse.riena.ui.swt.lnf.rienadefault.RienaDefaultLnf;
 import org.eclipse.riena.ui.swt.utils.SwtUtilities;
 
 /**
  * View of a module.
  */
 public class ModuleView implements INavigationNodeView<SWTModuleController, ModuleNode> {
 
 	private static final String WINDOW_RIDGET = "windowRidget"; //$NON-NLS-1$
 	private static final LnFUpdater LNF_UPDATER = new LnFUpdater();
 
 	private AbstractViewBindingDelegate binding;
 	private Composite parent;
 	private Composite body;
 	private Tree subModuleTree;
 	private ModuleNode moduleNode;
 	private boolean pressed;
 	private boolean hover;
 	private ModuleTitleBar title;
 	private NavigationTreeObserver navigationTreeObserver;
 	private ListenerList<IComponentUpdateListener> updateListeners;
 
 	//performance tweaking
 	//private boolean cachedActivityState = true;
 	//private boolean treeDirty = true;
 
 	public ModuleView(Composite parent) {
 		this.parent = parent;
 		binding = createBinding();
 		updateListeners = new ListenerList<IComponentUpdateListener>(IComponentUpdateListener.class);
 		buildView();
 	}
 
 	/**
 	 * Creates a delegate for the binding of view and controller.
 	 * 
 	 * @return delegate for binding
 	 */
 	protected AbstractViewBindingDelegate createBinding() {
 		return new InjectSwtViewBindingDelegate();
 	}
 
 	/**
 	 * Builds the composite and the tree of the module view.
 	 */
 	private void buildView() {
 
 		title = new ModuleTitleBar(getParent(), SWT.NONE);
 		binding.addUIControl(title, WINDOW_RIDGET);
 		layoutTitle();
 		new ModuleToolTip(title);
 
 		body = new Composite(getParent(), SWT.DOUBLE_BUFFERED);
 		updateModuleView();
 
 		createBodyContent(body);
 		LNF_UPDATER.updateUIControls(body);
 
 	}
 
 	Composite getBody() {
 		return body;
 	}
 
 	/**
 	 * Creates the content of the module body (default: the tree for the
 	 * sub-modules).
 	 * 
 	 * @param parent
 	 *            - body of the module
 	 */
 	protected void createBodyContent(Composite parent) {
 
 		parent.setLayout(new FormLayout());
 
 		subModuleTree = new Tree(parent, SWT.NO_SCROLL | SWT.DOUBLE_BUFFERED);
 		subModuleTree.setLinesVisible(false);
 		RienaDefaultLnf lnf = LnfManager.getLnf();
 		subModuleTree.setFont(lnf.getFont(LnfKeyConstants.SUB_MODULE_ITEM_FONT));
 		binding.addUIControl(subModuleTree, "tree"); //$NON-NLS-1$
 		FormData formData = new FormData();
 		formData.top = new FormAttachment(0, 0);
 		formData.left = new FormAttachment(0, 0);
 		formData.right = new FormAttachment(100, 0);
 		formData.bottom = new FormAttachment(100, 0);
 		subModuleTree.setLayoutData(formData);
 
 		addListeners();
 
 		new SubModuleToolTip(subModuleTree);
 		setTreeBackGround();
 
 	}
 
 	/**
 	 * Clips (if necessary) the text of the given tree item and all child items.
 	 * 
 	 * @param gc
 	 * @param item
 	 *            - tree item
 	 * @return true: some text was clipped; false: no text was clipped
 	 */
 	private boolean clipSubModuleTexts(GC gc, TreeItem item) {
 
 		boolean clipped = clipSubModuleText(gc, item);
 
 		TreeItem[] items = item.getItems();
 		for (TreeItem childItem : items) {
 			if (clipSubModuleTexts(gc, childItem)) {
 				clipped = true;
 			}
 		}
 
 		return clipped;
 
 	}
 
 	/**
 	 * Clips (if necessary) the text of the given tree item.
 	 * 
 	 * @param gc
 	 * @param item
 	 *            - tree item
 	 * @return true: text was clipped; false: text was not clipped
 	 */
 	private boolean clipSubModuleText(GC gc, TreeItem item) {
 
 		boolean clipped = false;
 		Rectangle treeBounds = getTree().getBounds();
 		Rectangle itemBounds = item.getBounds();
 		int maxWidth = treeBounds.width - itemBounds.x - 5;
 		String longText = getItemText(item);
 		if (longText != null) {
 			String text = SwtUtilities.clipText(gc, longText, maxWidth);
 			item.setText(text);
 			clipped = !longText.equals(text);
 		}
 
 		return clipped;
 	}
 
 	private String getItemText(TreeItem item) {
 
 		INavigationNode<?> subModule = (INavigationNode<?>) item.getData();
 		if (subModule != null) {
 			return subModule.getLabel();
 		} else {
 			return item.getText();
 		}
 	}
 
 	/**
 	 * Clips (if necessary) the text of the tree items and hides the scroll
 	 * bars.
 	 * 
 	 * @param gc
 	 */
 	private void onTreePaint(GC gc) {
 
 		TreeItem[] items = getTree().getItems();
 		for (TreeItem item : items) {
 			clipSubModuleTexts(gc, item);
 		}
 
 	}
 
 	protected void resize() {
 		fireUpdated(null);
 	}
 
 	/**
 	 * Adds listeners to the sub-module tree.
 	 */
 	private void addListeners() {
 		getTree().addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event event) {
 				// treeDirty = true;
 				TreeItem[] selection = getTree().getSelection();
 				if ((selection.length > 0) && (selection[0].getData() instanceof ISubModuleNode)) {
 					ISubModuleNode activeSubModule = (ISubModuleNode) selection[0].getData();
 					if (activeSubModule.getParent().isActivated()) {
 						activeSubModule.activate();
 					}
 				}
 				resize();
 			}
 
 		});
 
 		getTree().addListener(SWT.Paint, new Listener() {
 			public void handleEvent(Event event) {
 				onTreePaint(event.gc);
 			}
 		});
 
 		getTree().addListener(SWT.Expand, new Listener() {
 			public void handleEvent(Event event) {
 				// treeDirty = true;
 				handleExpandCollapse(event, true);
 			}
 		});
 
 		getTree().addListener(SWT.Collapse, new Listener() {
 			public void handleEvent(Event event) {
 				// treeDirty = true;
 				handleExpandCollapse(event, false);
 			}
 
 		});
 
 		getTree().addListener(SWT.PaintItem, new Listener() {
 			public void handleEvent(Event event) {
 				paintTreeItem(event);
 			}
 		});
 
 		new ModuleKeyboardNavigationListener(getTree());
 	}
 
 	/**
 	 * Paints the markers of the given tree item.
 	 * 
 	 * @param event
 	 *            - the event which occurred
 	 */
 	private void paintTreeItem(Event event) {
 		SubModuleTreeItemMarkerRenderer renderer = getTreeItemRenderer();
 		renderer.setBounds(event.x, event.y, event.width, event.height);
 		if (event.item instanceof TreeItem) {
 			TreeItem item = (TreeItem) event.item;
 			SubModuleNode node = (SubModuleNode) item.getData();
 			if (node != null) {
 				renderer.setMarkers(node.getMarkers());
 			}
 			renderer.paint(event.gc, event.item);
 		}
 	}
 
 	/**
 	 * After a node has been expanded or collapsed the size of the module must
 	 * be updated.
 	 * 
 	 * @param event
 	 *            - the event which occurred
 	 * @param expand
 	 */
 	private void handleExpandCollapse(Event event, boolean expand) {
 		if (event.item instanceof TreeItem) {
 			TreeItem item = (TreeItem) event.item;
 			INavigationNode<?> node = (INavigationNode<?>) item.getData();
 			node.setExpanded(expand);
 		}
 		resize();
 	}
 
 	protected void setTreeBackGround() {
 		subModuleTree.setBackground(LnfManager.getLnf().getColor("SubModuleTree.background")); //$NON-NLS-1$
 	}
 
 	protected Composite getParent() {
 		return parent;
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.ui.swt.views.INavigationNodeView#getNavigationNode()
 	 */
 	public ModuleNode getNavigationNode() {
 		return moduleNode;
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.ui.swt.views.INavigationNodeView#bind(org.eclipse.riena.navigation.INavigationNode)
 	 */
 	public void bind(ModuleNode node) {
 
 		moduleNode = node;
 
 		navigationTreeObserver = new NavigationTreeObserver();
 		navigationTreeObserver.addListener(new SubModuleListener());
 		navigationTreeObserver.addListener(new ModuleListener());
 		navigationTreeObserver.addListenerTo(moduleNode);
 
 		if (getNavigationNode().getNavigationNodeController() instanceof IController) {
 			IController controller = (IController) node.getNavigationNodeController();
 			binding.injectRidgets(controller);
 			binding.bind(controller);
 			controller.afterBind();
 		}
 
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.ui.swt.views.INavigationNodeView#unbind()
 	 */
 	public void unbind() {
 
 		if (getNavigationNode().getNavigationNodeController() instanceof IController) {
 			IController controller = (IController) getNavigationNode().getNavigationNodeController();
 			binding.unbind(controller);
 		}
 
 		navigationTreeObserver.removeListenerFrom(moduleNode);
 		moduleNode = null;
 
 	}
 
 	/**
 	 * After adding of removing a sub-module from another sub-module, the module
 	 * view must be resized.
 	 */
 	private class SubModuleListener extends SubModuleNodeListener {
 		//
 		//		@Override
 		//		public void filterAdded(ISubModuleNode source, IUIFilter filter) {
 		//			super.filterAdded(source, filter);
 		//			updateModuleView();
 		//		}
 		//
 		//		@Override
 		//		public void filterRemoved(ISubModuleNode source, IUIFilter filter) {
 		//			super.filterRemoved(source, filter);
 		//			updateModuleView();
 		//		}
 
 		/**
 		 * @see org.eclipse.riena.navigation.listener.NavigationNodeListener#childAdded(org.eclipse.riena.navigation.INavigationNode,
 		 *      org.eclipse.riena.navigation.INavigationNode)
 		 */
 		@Override
 		public void childAdded(ISubModuleNode source, ISubModuleNode childAdded) {
 			resize();
 		}
 
 		/**
 		 * @see org.eclipse.riena.navigation.listener.NavigationNodeListener#childRemoved(org.eclipse.riena.navigation.INavigationNode,
 		 *      org.eclipse.riena.navigation.INavigationNode)
 		 */
 		@Override
 		public void childRemoved(ISubModuleNode source, ISubModuleNode childRemoved) {
 			resize();
 		}
 
 		/**
 		 * @see org.eclipse.riena.navigation.listener.NavigationNodeListener#activated
 		 *      (org.eclipse.riena.navigation.INavigationNode)
 		 */
 		@Override
 		public void activated(ISubModuleNode source) {
 			updateExpanded(source); // fix for bug 269221
 			resize();
 			getTree().setFocus();
 		}
 
 		private void updateExpanded(ISubModuleNode node) {
 			final INavigationNode<?> nodeParent = node.getParent();
 			if (nodeParent instanceof ISubModuleNode) {
 				nodeParent.setExpanded(true);
 				updateExpanded((ISubModuleNode) nodeParent);
 			}
 		}
 
 		/**
 		 * @see org.eclipse.riena.navigation.listener.NavigationNodeListener#markerChanged(org.eclipse.riena.navigation.INavigationNode,
 		 *      IMarker)
 		 */
 		@Override
 		public void markerChanged(ISubModuleNode source, IMarker marker) {
 			getTree().redraw();
 		}
 
 		@Override
 		public void labelChanged(ISubModuleNode source) {
 			super.labelChanged(source);
 			getTree().redraw();
 		}
 
 	}
 
 	/**
 	 * After adding of removing a sub-module from this module, the module view
 	 * must be resized.
 	 */
 	private class ModuleListener extends ModuleNodeListener {
 		//
 		//		@Override
 		//		public void filterAdded(IModuleNode source, IUIFilter filter) {
 		//			super.filterAdded(source, filter);
 		//			updateModuleView();
 		//		}
 		//
 		//		@Override
 		//		public void filterRemoved(IModuleNode source, IUIFilter filter) {
 		//			super.filterRemoved(source, filter);
 		//			updateModuleView();
 		//		}
 
 		/**
 		 * @see org.eclipse.riena.navigation.listener.NavigationNodeListener#activated(org.eclipse.riena.navigation.INavigationNode)
 		 */
 		@Override
 		public void activated(IModuleNode source) {
 			super.activated(source);
 			updateModuleView();
 		}
 
 		/**
 		 * @see org.eclipse.riena.navigation.listener.NavigationNodeListener#disposed
 		 *      (org.eclipse.riena.navigation.INavigationNode)
 		 */
 		@Override
 		public void disposed(IModuleNode source) {
 			super.disposed(source);
 			dispose();
 		}
 
 		/**
 		 * @see org.eclipse.riena.navigation.listener.NavigationNodeListener#markerChanged(org.eclipse.riena.navigation.INavigationNode,
 		 *      IMarker)
 		 */
 		@Override
 		public void markerChanged(IModuleNode source, IMarker marker) {
 			super.markerChanged(source, marker);
 			title.setMarkers(source.getMarkers());
 			title.redraw();
 		}
 
 		@Override
 		public void labelChanged(IModuleNode source) {
 			super.labelChanged(source);
 			updateModuleView();
 		}
 
 	}
 
 	/**
 	 * Disposes this module item.
 	 */
 	public void dispose() {
 		unbind();
 		// getBody().setVisible(false);
 		// getBody().setBounds(0, 0, 0, 0);
 		SwtUtilities.disposeWidget(title);
 		SwtUtilities.disposeWidget(getBody());
 		SwtUtilities.disposeWidget(getTree());
 	}
 
 	/**
 	 * Returns the tree with the sub-module items.
 	 * 
 	 * @return tree
 	 */
 	protected Tree getTree() {
 		return subModuleTree;
 	}
 
 	/**
 	 * Returns the title for the module
 	 * 
 	 * @return title
 	 */
 	protected ModuleTitleBar getTitle() {
 		return title;
 	}
 
 	/**
 	 * Returns the height of the open item.
 	 * 
 	 * @return height.
 	 */
 	public int getOpenHeight() {
 		IModuleNode navigationNode = getNavigationNode();
 		if ((navigationNode != null) && (navigationNode.isActivated())) {
 			int depth = navigationNode.calcDepth();
 			if (depth == 0) {
 				return 0;
 			} else {
 				int itemHeight = getTree().getItemHeight();
 				return depth * itemHeight + 1;
 			}
 		} else {
 			return 0;
 		}
 
 	}
 
 	/**
 	 * Returns a rectangle describing the size and location of this module.
 	 * 
 	 * @return the bounds
 	 */
 	public Rectangle getBounds() {
 
 		Rectangle bounds = title.getBounds();
 
 		if (getNavigationNode().isActivated()) {
 			bounds.height += getBody().getSize().y;
 		}
 
 		return bounds;
 	}
 
 	/**
 	 * Returns if the module item is pressed or not.
 	 * 
 	 * @param pressed
 	 *            - true, if mouse over the module and pressed; otherwise false.
 	 */
 	public boolean isPressed() {
 		return pressed;
 	}
 
 	/**
 	 * Sets if the module item is pressed or not.<br>
 	 * If the given state differs from the current state, the parent of item is
 	 * redrawn.
 	 * 
 	 * @param pressed
 	 *            - true, if mouse over the module and pressed; otherwise false.
 	 */
 	public void setPressed(boolean pressed) {
 		if (this.pressed != pressed) {
 			this.pressed = pressed;
 			if (!parent.isDisposed()) {
 				parent.redraw();
 			}
 		}
 	}
 
 	/**
 	 * Returns if the module item is highlighted, because the mouse hovers over
 	 * the item.
 	 * 
 	 * @return true, if mouse over the module; otherwise false.
 	 */
 	public boolean isHover() {
 		return hover;
 	}
 
 	/**
 	 * Sets if the module item is highlighted, because the mouse hovers over the
 	 * item.<br>
 	 * If the given hover state differs from the current state, the parent of
 	 * item is redrawn.
 	 * 
 	 * @param hover
 	 *            - true, if mouse over the module; otherwise false.
 	 */
 	public void setHover(boolean hover) {
 		if (this.hover != hover) {
 			this.hover = hover;
 			if (!parent.isDisposed()) {
 				parent.redraw();
 			}
 		}
 	}
 
 	/**
 	 * @return the icon
 	 */
 	public String getIcon() {
 		if (getNavigationNode() == null) {
 			return null;
 		}
 		return getNavigationNode().getIcon();
 	}
 
 	/**
 	 * @return the activated
 	 */
 	public boolean isActivated() {
 		if (getNavigationNode() == null) {
 			return false;
 		}
 		return getNavigationNode().isActivated();
 	}
 
 	/**
 	 * Returns whether this view is visible or not.
 	 * 
 	 * @return {@code true} if nod if visible; otherwise {@code false}
 	 */
 	public boolean isVisible() {
 		if (getNavigationNode() == null) {
 			return false;
 		}
 		return getNavigationNode().isVisible();
 	}
 
 	/**
 	 * @return the closeable
 	 */
 	public boolean isCloseable() {
 		if (getNavigationNode() == null) {
 			return false;
 		}
 		return getNavigationNode().isClosable();
 	}
 
 	/**
 	 * @return the label
 	 */
 	public String getLabel() {
 		if (getNavigationNode() == null) {
 			return null;
 		}
 		return getNavigationNode().getLabel();
 	}
 
 	protected void fireUpdated(INavigationNode<?> node) {
 		for (IComponentUpdateListener listener : updateListeners.getListeners()) {
 			listener.update(node);
 		}
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.ui.swt.views.INavigationNodeView#addUpdateListener(org.eclipse.riena.navigation.ui.swt.views.IComponentUpdateListener)
 	 */
 	public void addUpdateListener(IComponentUpdateListener listener) {
 		updateListeners.add(listener);
 	}
 
 	/**
 	 * Returns the renderer that paints a module group.
 	 * 
 	 * @return renderer
 	 */
 	private ModuleGroupRenderer getModuleGroupRenderer() {
 
 		ModuleGroupRenderer renderer = (ModuleGroupRenderer) LnfManager.getLnf().getRenderer(
 				LnfKeyConstants.MODULE_GROUP_RENDERER);
 		if (renderer == null) {
 			renderer = new ModuleGroupRenderer();
 		}
 		return renderer;
 
 	}
 
 	/**
 	 * Returns the renderer that paints the markers of a tree item.
 	 * 
 	 * @return renderer
 	 */
 	private SubModuleTreeItemMarkerRenderer getTreeItemRenderer() {
 
 		SubModuleTreeItemMarkerRenderer renderer = (SubModuleTreeItemMarkerRenderer) LnfManager.getLnf().getRenderer(
 				LnfKeyConstants.SUB_MODULE_TREE_ITEM_MARKER_RENDERER);
 		if (renderer == null) {
 			renderer = new SubModuleTreeItemMarkerRenderer();
 		}
 		return renderer;
 
 	}
 
 	public void updateModuleView() {
 		boolean currentActiveState = false;
 		if (getNavigationNode() != null) {
 			currentActiveState = getNavigationNode().isActivated();
 		}
 
 		if (!SwtUtilities.isDisposed(title)) {
 			layoutTitle();
 		}
 
 		if (!SwtUtilities.isDisposed(getBody())) {
 			if (getBody().isVisible() != currentActiveState) {
 				getBody().setVisible(currentActiveState);
 			}
 			int height = getOpenHeight();
 			if (getBody().getSize().y != height) {
 				FormData formData = new FormData();
 				formData.top = new FormAttachment(title);
 				formData.left = new FormAttachment(0, 0);
 				formData.right = new FormAttachment(100, 0);
 				formData.height = height;
 				getBody().setLayoutData(formData);
 			}
 		}
 
 		// TODO performance activation disabled because UIFilter didnt work anymore properly, hidden submodule nodes didnt get visible because of redraw problems
 		getParent().layout();
 		// Performance: Only layout if the activity of the ModuleNode or the tree inside has changed
 		//		if (currentActiveState != cachedActivityState || treeDirty) {
 		//			getParent().layout();
 		//			cachedActivityState = currentActiveState;
 		//			treeDirty = false;
 		//		}
 		title.setWindowActive(currentActiveState);
 
 	}
 
 	private void layoutTitle() {
 
 		Control[] children = getParent().getChildren();
 		FormData formData = new FormData();
 		int index = -1;
 		for (int i = 0; i < children.length; i++) {
 			if (children[i] == title) {
 				index = i;
 				break;
 			}
 		}
 		if (index == 0) {
 			formData.top = new FormAttachment(0, 0);
 		} else if (index < 0) {
 			formData.top = new FormAttachment(children[children.length - 1], getModuleGroupRenderer()
 					.getModuleModuleGap());
 		} else {
 			formData.top = new FormAttachment(children[index - 1], getModuleGroupRenderer().getModuleModuleGap());
 		}
 		formData.left = new FormAttachment(0, 0);
 		formData.right = new FormAttachment(100, 0);
 		formData.height = title.getSize().y;
 		title.setLayoutData(formData);
 
 	}
 
 }
