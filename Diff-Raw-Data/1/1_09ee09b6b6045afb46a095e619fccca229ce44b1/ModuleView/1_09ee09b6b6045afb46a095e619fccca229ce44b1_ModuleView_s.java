 /*******************************************************************************
  * Copyright (c) 2007, 2011 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.navigation.ui.swt.views;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ControlEvent;
 import org.eclipse.swt.events.ControlListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Cursor;
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
 import org.eclipse.riena.core.marker.Markable;
 import org.eclipse.riena.core.util.ListenerList;
 import org.eclipse.riena.core.util.Nop;
 import org.eclipse.riena.core.util.StringUtils;
 import org.eclipse.riena.internal.ui.ridgets.swt.TreeRidgetLabelProvider;
 import org.eclipse.riena.navigation.IModuleNode;
 import org.eclipse.riena.navigation.INavigationNode;
 import org.eclipse.riena.navigation.ISubApplicationNode;
 import org.eclipse.riena.navigation.ISubModuleNode;
 import org.eclipse.riena.navigation.NavigationNodeId;
 import org.eclipse.riena.navigation.listener.ModuleNodeListener;
 import org.eclipse.riena.navigation.listener.NavigationTreeObserver;
 import org.eclipse.riena.navigation.listener.SubModuleNodeListener;
 import org.eclipse.riena.navigation.model.ModuleGroupNode;
 import org.eclipse.riena.navigation.model.ModuleNode;
 import org.eclipse.riena.navigation.model.SubModuleNode;
 import org.eclipse.riena.navigation.ui.swt.binding.InjectSwtViewBindingDelegate;
 import org.eclipse.riena.navigation.ui.swt.lnf.renderer.ModuleGroupRenderer;
 import org.eclipse.riena.navigation.ui.swt.lnf.renderer.SubModuleTreeItemMarkerRenderer;
 import org.eclipse.riena.navigation.ui.swt.presentation.SwtViewProvider;
 import org.eclipse.riena.ui.core.marker.IIconizableMarker;
 import org.eclipse.riena.ui.ridgets.controller.IController;
 import org.eclipse.riena.ui.ridgets.swt.uibinding.AbstractViewBindingDelegate;
 import org.eclipse.riena.ui.swt.ModuleTitleBar;
 import org.eclipse.riena.ui.swt.facades.SWTFacade;
 import org.eclipse.riena.ui.swt.lnf.LnFUpdater;
 import org.eclipse.riena.ui.swt.lnf.LnfKeyConstants;
 import org.eclipse.riena.ui.swt.lnf.LnfManager;
 import org.eclipse.riena.ui.swt.lnf.rienadefault.RienaDefaultLnf;
 import org.eclipse.riena.ui.swt.utils.SwtUtilities;
 
 /**
  * View of a module.
  */
 public class ModuleView implements INavigationNodeView<ModuleNode> {
 
 	private static final String WINDOW_RIDGET = "windowRidget"; //$NON-NLS-1$
 	private final LnFUpdater lnfUpdater = LnFUpdater.getInstance();
 	private final AbstractViewBindingDelegate binding;
 	private final Composite parent;
 	private Composite body;
 	private Tree subModuleTree;
 	private ModuleNode moduleNode;
 	private boolean pressed;
 	private boolean hover;
 	private ModuleTitleBar title;
 	private BlockManager blockManager;
 	private boolean doNotResize;
 
 	private NavigationTreeObserver navigationTreeObserver;
 
 	private final ListenerList<IComponentUpdateListener> updateListeners;
 	private ModuleGroupNode moduleGroupNode;
 	private Map<ISubModuleNode, Set<IMarker>> subModuleMarkerCache;
 	private Listener disabledSubModuleTreeBgPainter;
 	private SubModuleListener subModuleListener;
 	private ModuleListener moduleListener;
 
 	public ModuleView(final Composite parent) {
 		this.parent = parent;
 		binding = createBinding();
 		updateListeners = new ListenerList<IComponentUpdateListener>(IComponentUpdateListener.class);
 		initializeSubModuleMarkerCache();
 		buildView();
 	}
 
 	private void initializeSubModuleMarkerCache() {
 		subModuleMarkerCache = new HashMap<ISubModuleNode, Set<IMarker>>();
 	}
 
 	public void addUpdateListener(final IComponentUpdateListener listener) {
 		updateListeners.add(listener);
 	}
 
 	public void setModuleGroupNode(final ModuleGroupNode moduleGroupNode) {
 		this.moduleGroupNode = moduleGroupNode;
 	}
 
 	/**
 	 * @return the moduleGroupNode parent of the moduleNode
 	 */
 	public ModuleGroupNode getModuleGroupNode() {
 		return moduleGroupNode;
 	}
 
 	public void bind(final ModuleNode node) {
 		moduleNode = node;
 		navigationTreeObserver = new NavigationTreeObserver();
 		subModuleListener = new SubModuleListener();
 		navigationTreeObserver.addListener(subModuleListener);
 		moduleListener = new ModuleListener();
 		navigationTreeObserver.addListener(moduleListener);
 		navigationTreeObserver.addListenerTo(moduleNode);
 
 		if (getNavigationNode().getNavigationNodeController() instanceof IController) {
 			final IController controller = (IController) node.getNavigationNodeController();
 			binding.injectRidgets(controller);
 			binding.bind(controller);
 			controller.afterBind();
 		}
 	}
 
 	/**
 	 * Disposes this module item.
 	 */
 	public void dispose() {
 		if (null != subModuleListener) {
 			navigationTreeObserver.removeListener(subModuleListener);
 		}
 
 		if (null != moduleListener) {
 			navigationTreeObserver.removeListener(moduleListener);
 		}
 
 		subModuleMarkerCache.clear();
 		unbind();
 		SwtUtilities.dispose(title);
 		SwtUtilities.dispose(getBody());
 		SwtUtilities.dispose(getTree());
 	}
 
 	/**
 	 * Returns a rectangle describing the size and location of this module.
 	 * 
 	 * @return the bounds
 	 */
 	public Rectangle getBounds() {
 
 		final Rectangle bounds = title.getBounds();
 
 		if (getNavigationNode().isActivated()) {
 			bounds.height += getBody().getSize().y;
 		}
 
 		return bounds;
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
 	 * @return the label
 	 */
 	public String getLabel() {
 		if (getNavigationNode() == null) {
 			return null;
 		}
 		return getNavigationNode().getLabel();
 	}
 
 	public ModuleNode getNavigationNode() {
 		return moduleNode;
 	}
 
 	/**
 	 * Returns the height of the open item.
 	 * 
 	 * @return height.
 	 */
 	public int getOpenHeight() {
 		final IModuleNode navigationNode = getNavigationNode();
 		if ((navigationNode != null) && (navigationNode.isActivated())) {
 			final int depth = navigationNode.calcDepth();
 			if (depth == 0) {
 				return 0;
 			} else {
 				final int itemHeight = getTree().getItemHeight();
 				return depth * itemHeight + 1;
 			}
 		} else {
 			return 0;
 		}
 
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
 	 * @return the closeable
 	 */
 	public boolean isCloseable() {
 		if (getNavigationNode() == null) {
 			return false;
 		}
 		return getNavigationNode().isClosable();
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
 	 * Returns if the module item is pressed or not.
 	 * 
 	 * @param pressed
 	 *            true, if mouse over the module and pressed; otherwise false.
 	 */
 	public boolean isPressed() {
 		return pressed;
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
 	 * Sets if the module item is highlighted, because the mouse hovers over the
 	 * item.<br>
 	 * If the given hover state differs from the current state, the parent of
 	 * item is redrawn.
 	 * 
 	 * @param hover
 	 *            true, if mouse over the module; otherwise false.
 	 */
 	public void setHover(final boolean hover) {
 		if (this.hover != hover) {
 			this.hover = hover;
 			if (!parent.isDisposed()) {
 				parent.redraw();
 			}
 		}
 	}
 
 	/**
 	 * Sets if the module item is pressed or not.<br>
 	 * If the given state differs from the current state, the parent of item is
 	 * redrawn.
 	 * 
 	 * @param pressed
 	 *            true, if mouse over the module and pressed; otherwise false.
 	 */
 	public void setPressed(final boolean pressed) {
 		if (this.pressed != pressed) {
 			this.pressed = pressed;
 			if (!parent.isDisposed()) {
 				parent.redraw();
 			}
 		}
 	}
 
 	public void unbind() {
 
 		if (getNavigationNode().getNavigationNodeController() instanceof IController) {
 			final IController controller = (IController) getNavigationNode().getNavigationNodeController();
 			binding.unbind(controller);
 		}
 
 		navigationTreeObserver.removeListenerFrom(moduleNode);
 		moduleNode = null;
 
 	}
 
 	/**
 	 * Updates the enabled flag of the title and the sub-module tree and and
 	 * redraws them.
 	 * 
 	 * @since 3.0
 	 */
 	public void updateEnabled() {
 		title.setEnabled(getNavigationNode().isEnabled());
 		subModuleTree.setEnabled(title.isEnabled());
 		title.redraw();
 	}
 
 	public void updateModuleView() {
 		prepareUpdate();
 		getParent().layout();
 	}
 
 	public void prepareUpdate() {
 
 		boolean currentActiveState = false;
 		if (getNavigationNode() != null) {
 			currentActiveState = getNavigationNode().isActivated();
 		}
 
 		if (!SwtUtilities.isDisposed(title)) {
 			layoutTitle();
 			title.setWindowActive(currentActiveState);
 		}
 
 		if (!SwtUtilities.isDisposed(getBody())) {
 			if (getBody().isVisible() != currentActiveState) {
 				getBody().setVisible(currentActiveState);
 			}
 			final int height = getOpenHeight();
 			if (getBody().getSize().y != height) {
 				final FormData formData = new FormData();
 				formData.top = new FormAttachment(title);
 				formData.left = new FormAttachment(0, 0);
 				formData.right = new FormAttachment(100, 0);
 				formData.height = height;
 				getBody().setLayoutData(formData);
 			}
 		}
 
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
 	 * Creates the content of the module body (default: the tree for the
 	 * sub-modules).
 	 * 
 	 * @param parent
 	 *            body of the module
 	 */
 	protected void createBodyContent(final Composite parent) {
 		parent.setLayout(new FormLayout());
 
 		subModuleTree = new Tree(parent, SWT.NO_SCROLL | SWT.DOUBLE_BUFFERED);
 		subModuleTree.setLinesVisible(false);
 		final RienaDefaultLnf lnf = LnfManager.getLnf();
 		subModuleTree.setFont(lnf.getFont(LnfKeyConstants.SUB_MODULE_ITEM_FONT));
 		binding.addUIControl(subModuleTree, "tree"); //$NON-NLS-1$
 		final FormData formData = new FormData();
 		formData.top = new FormAttachment(0, 0);
 		formData.left = new FormAttachment(0, 0);
 		formData.right = new FormAttachment(100, 0);
 		formData.bottom = new FormAttachment(100, 0);
 		subModuleTree.setLayoutData(formData);
 		subModuleTree.setData(TreeRidgetLabelProvider.TREE_KIND_KEY, TreeRidgetLabelProvider.TREE_KIND_NAVIGATION);
 
 		addListeners();
 
 		SWTFacade.getDefault().createSubModuleToolTip(subModuleTree, new LabelProvider() {
 			@Override
 			public String getText(final Object element) {
 				return ((INavigationNode<?>) element).getLabel();
 			}
 		});
 		setTreeBackGround();
 
 	}
 
 	/**
 	 * @since 3.0
 	 */
 	protected Listener createDisabledSubModuleTreeBackgroundPainter(final Color disabledBackgroundColor) {
 		return new DisabledSubModuleTreeBackgroundPainter(disabledBackgroundColor);
 	}
 
 	protected void fireUpdated(final INavigationNode<?> node) {
 		for (final IComponentUpdateListener listener : updateListeners.getListeners()) {
 			listener.update(node);
 		}
 	}
 
 	public Composite getParent() {
 		return parent;
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
 	 * Returns the tree with the sub-module items.
 	 * 
 	 * @return tree
 	 */
 	protected Tree getTree() {
 		return subModuleTree;
 	}
 
 	protected void resize() {
 		if (doNotResize) {
 			return;
 		}
 		fireUpdated(null);
 	}
 
 	protected void setTreeBackGround() {
 		subModuleTree.setBackground(LnfManager.getLnf().getColor("SubModuleTree.background")); //$NON-NLS-1$
 	}
 
 	Composite getBody() {
 		return body;
 	}
 
 	// helping methods
 	//////////////////
 
 	/**
 	 * Adds listeners to the sub-module tree.
 	 */
 	private void addListeners() {
 		getTree().addListener(SWT.Expand, new Listener() {
 			public void handleEvent(final Event event) {
 				// treeDirty = true;
 				handleExpandCollapse(event, true);
 			}
 		});
 
 		getTree().addListener(SWT.Collapse, new Listener() {
 			public void handleEvent(final Event event) {
 				// treeDirty = true;
 				handleExpandCollapse(event, false);
 			}
 
 		});
 
 		getTree().addControlListener(new ControlListener() {
 			public void controlResized(final ControlEvent e) {
 				getTree().redraw();
 			}
 
 			public void controlMoved(final ControlEvent e) {
 				Nop.reason("nothing todo"); //$NON-NLS-1$
 			}
 		});
 
 		final Listener paintItemListener = new Listener() {
 			public void handleEvent(final Event event) {
 				paintTreeItem(event);
 			}
 		};
 		SWTFacade.getDefault().addPaintItemListener(getTree(), paintItemListener);
 
 		SWTFacade.getDefault().attachModuleNavigationListener(getTree());
 
 		final RienaDefaultLnf lnf = LnfManager.getLnf();
 		Color disabledBackgroundColor = null;
 		if (!lnf.getBooleanSetting(LnfKeyConstants.SUB_MODULE_TREE_DISABLED_BACKGROUND_IS_SWT_DEFAULT)) {
 			disabledBackgroundColor = lnf.getColor(LnfKeyConstants.SUB_MODULE_TREE_BACKGROUND);
 		}
 		disabledSubModuleTreeBgPainter = createDisabledSubModuleTreeBackgroundPainter(disabledBackgroundColor);
 
 		SWTFacade.getDefault().addEraseItemListener(subModuleTree, disabledSubModuleTreeBgPainter);
 	}
 
 	private void blockView(final boolean block) {
 		if (blockManager == null) {
 			blockManager = new BlockManager();
 		}
 		if (block) {
 			blockManager.block();
 		} else {
 			blockManager.unblock();
 		}
 	}
 
 	/**
 	 * Builds the composite and the tree of the module view.
 	 */
 	private void buildView() {
 		title = new ModuleTitleBar(getParent(), SWT.NONE);
 		binding.addUIControl(title, WINDOW_RIDGET);
 		//		layoutTitle();
 		SWTFacade.getDefault().createEmbeddedTitleBarToolTip(title);
 
 		body = new Composite(getParent(), SWT.DOUBLE_BUFFERED);
 		//		updateModuleView();
 
 		createBodyContent(body);
 		lnfUpdater.updateUIControls(body, true);
 	}
 
 	/**
 	 * Clips (if necessary) the text of the given tree item.
 	 * 
 	 * @param gc
 	 * @param item
 	 *            tree item
 	 * @return {@code true}: text of the item changed; {@code false}: text not
 	 *         changed
 	 */
 	private boolean clipSubModuleText(final GC gc, final TreeItem item) {
 		boolean clipped = false;
 		final Rectangle treeBounds = getTree().getBounds();
 		final Rectangle itemBounds = item.getBounds();
 		final int maxWidth = treeBounds.width - itemBounds.x - 5;
 		final String longText = getItemText(item);
 		if (longText != null) {
 			final String text = SwtUtilities.clipText(gc, longText, maxWidth);
 			clipped = !StringUtils.equals(longText, text);
 			clipped = clipped || !StringUtils.equals(item.getText(), text);
 			if (clipped) {
 				item.setText(text);
 			}
 		}
 
 		return clipped;
 	}
 
 	/**
 	 * Clips (if necessary) the text of the given tree item and all child items.
 	 * 
 	 * @param gc
 	 * @param item
 	 *            tree item
 	 * @return {@code true}: some texts changed; {@code false}: no text changed
 	 */
 	private boolean clipSubModuleTexts(final GC gc, final TreeItem item) {
 		boolean clipped = clipSubModuleText(gc, item);
 
 		final TreeItem[] items = item.getItems();
 		for (final TreeItem childItem : items) {
 			if (clipSubModuleTexts(gc, childItem)) {
 				clipped = true;
 			}
 		}
 
 		return clipped;
 	}
 
 	/**
 	 * Returns the text of the given item. If the data of the item is a node, so
 	 * the label of the node is returned.
 	 * 
 	 * @param item
 	 *            tree item
 	 * @return text of item (or label of node)
 	 */
 	private String getItemText(final TreeItem item) {
 		final INavigationNode<?> node = (INavigationNode<?>) item.getData();
 		if (node != null) {
 			return node.getLabel();
 		} else {
 			return item.getText();
 		}
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
 
 	/**
 	 * After a node has been expanded or collapsed the size of the module must
 	 * be updated.
 	 * 
 	 * @param event
 	 *            the event which occurred
 	 * @param expand
 	 */
 	private void handleExpandCollapse(final Event event, final boolean expand) {
 		if (event.item instanceof TreeItem) {
 			final TreeItem item = (TreeItem) event.item;
 			final INavigationNode<?> node = (INavigationNode<?>) item.getData();
 			node.setExpanded(expand);
 		}
 		resize();
 	}
 
 	private void layoutTitle() {
 
 		final FormData formData = new FormData();
 
 		final int index = getModuleGroupNode().getIndexOfChild(getNavigationNode());
 
 		if (index == 0) {
 			formData.top = new FormAttachment(0, 0);
 		} else if (index < 0) {
 			formData.top = new FormAttachment(getModuleViewBody(getModuleGroupNode().getChild(
 					getModuleGroupNode().getChildren().size() - 1)), getModuleGroupRenderer().getModuleModuleGap());
 		} else {
 			formData.top = new FormAttachment(getModuleViewBody(getModuleGroupNode().getChild(index - 1)),
 					getModuleGroupRenderer().getModuleModuleGap());
 		}
 		formData.left = new FormAttachment(0, 0);
 		formData.right = new FormAttachment(100, 0);
 		formData.height = title.getSize().y;
 		title.setLayoutData(formData);
 	}
 
 	/**
 	 * Locates the body {@link Control} of the given {@link IModuleNode}
 	 * 
 	 * @param child
 	 *            - the child for which the body control will be located
 	 * @return - the body {@link Control}
 	 */
 	private Control getModuleViewBody(final IModuleNode child) {
 		for (final ModuleView moduleView : getModuleGroupRenderer().getItems()) {
 			if (moduleView.getNavigationNode() == child) {
 				return moduleView.getBody();
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Paints the markers of the given tree item.
 	 * 
 	 * @param event
 	 *            the event which occurred
 	 */
 	private void paintTreeItem(final Event event) {
 		if (event.item instanceof TreeItem) {
 			final SubModuleTreeItemMarkerRenderer renderer = getTreeItemRenderer();
 			renderer.setBounds(event.x, event.y, event.width, event.height);
 			final TreeItem item = (TreeItem) event.item;
 			final SubModuleNode node = (SubModuleNode) item.getData();
 			if (node != null) {
 				final boolean deep = !item.getExpanded();
 				final Collection<? extends IMarker> markers = getAllMarkers(node, deep);
 				renderer.setMarkers(markers);
 			}
 			clipSubModuleTexts(event.gc, item);
 			renderer.paint(event.gc, item);
 		}
 	}
 
 	/**
 	 * Returns all (IIconizableMarker) markers of the given node and all of its
 	 * child nodes (if deep is true).
 	 * 
 	 * @param node
 	 *            sub-module node
 	 * @param deep
 	 *            {@code true} return also the markers of the child nodes;
 	 *            {@code false} only markers of the given node
 	 * @return all markers, that can be displayed in the navigation tree
 	 */
 	//	private Collection<? extends IMarker> getAllMarkers(ISubModuleNode node, boolean deep) {
 	//
 	//		if ((node == null) || !node.isVisible()) {
 	//			return Collections.emptyList();
 	//		}
 	//
 	//		Set<IMarker> markers = new HashSet<IMarker>();
 	//
 	//		List<ISubModuleNode> allNodes = new ArrayList<ISubModuleNode>();
 	//		allNodes.add(node);
 	//		int i = 0;
 	//		while (i < allNodes.size() && deep) {
 	//			List<ISubModuleNode> children = allNodes.get(i).getChildren();
 	//			for (ISubModuleNode child : children) {
 	//				if (child.isVisible()) {
 	//					allNodes.add(child);
 	//					markers.addAll(child.getMarkers());
 	//				}
 	//			}
 	//			i++;
 	//		}
 	//
 	//		return Markable.getMarkersOfType(markers, IIconizableMarker.class);
 	//
 	//	}
 
 	private Collection<? extends IMarker> getAllMarkers(final ISubModuleNode node, final boolean deep) {
 
 		if ((node == null) || !node.isVisible()) {
 			return Collections.emptySet();
 		}
 
 		final HashSet<IMarker> markers = new HashSet<IMarker>();
 		fillMarkers(node, deep, markers);
 
 		return markers;
 	}
 
 	private void fillMarkers(final ISubModuleNode node, final boolean deep, final Set<IMarker> markers) {
 
 		if (!node.isVisible()) {
 			return;
 		}
 
 		markers.addAll(Markable.getMarkersOfType(node.getMarkers(), IIconizableMarker.class));
 
 		if (deep) {
 			for (final ISubModuleNode child : node.getChildren()) {
 				fillMarkers(child, deep, markers);
 			}
 		}
 
 	}
 
 	// helping classes
 	//////////////////
 
 	/**
 	 * After adding of removing a sub-module from this module, the module view
 	 * must be resized.
 	 */
 	private class ModuleListener extends ModuleNodeListener {
 		@Override
 		public void activated(final IModuleNode source) {
 			super.activated(source);
 			updateModuleView();
 		}
 
 		@Override
 		public void block(final IModuleNode source, final boolean block) {
 			blockView(block);
 		}
 
 		@Override
 		public void disposed(final IModuleNode source) {
 			super.disposed(source);
 
 			dispose();
 		}
 
 		@Override
 		public void markerChanged(final IModuleNode source, final IMarker marker) {
 			super.markerChanged(source, marker);
 			title.setMarkers(source.getMarkers());
 			updateEnabled();
 		}
 
 		@Override
 		public void nodeIdChange(final IModuleNode source, final NavigationNodeId oldId, final NavigationNodeId newId) {
 			if (source.equals(getNavigationNode())) {
 				SwtViewProvider.getInstance().replaceNavigationNodeId(source, oldId, newId);
 			}
 		}
 	}
 
 	/**
 	 * After adding of removing a sub-module from another sub-module, the module
 	 * view must be resized.
 	 */
 	private class SubModuleListener extends SubModuleNodeListener {
 
 		@Override
 		public void beforeActivated(final ISubModuleNode source) {
 
 			/*
 			 * SWT feature: when tree.setFocus() is called below, it will fire a
 			 * selection event in ADDITION of setting the focus. This will
 			 * trigger activation of the selected node, which may be different
 			 * than the 'source' node.
 			 * 
 			 * Workaround: we make sure the tree has already a selection before
 			 * we go into the activated(...) method, to avoid this selection
 			 * event.
 			 */
 			final Tree tree = getTree();
 			if (tree.getSelectionCount() == 0 && tree.getItemCount() > 0) {
 				TreeItem item = findItem(tree.getItems(), source);
 				if (item == null) {
 					item = tree.getItem(0);
 				}
 				tree.select(item);
 			}
 		}
 
 		private TreeItem findItem(final TreeItem[] items, final ISubModuleNode source) {
 			for (final TreeItem item : items) {
 				if (item.getData() == source) {
 					return item;
 				}
 				final TreeItem result = item.getItemCount() > 0 ? findItem(item.getItems(), source) : null;
 				if (result != null) {
 					return result;
 				}
 			}
 			return null;
 		}
 
 		@Override
 		public void activated(final ISubModuleNode source) {
 			doNotResize = true;
 			updateExpanded(source); // fix for bug 269221
 			doNotResize = false;
 			resize();
 
 			final TreeItem currentItem = findItem(getTree().getItems(), source);
 			if (null != currentItem) {
 				getTree().select(currentItem);
 			}
 			getTree().setFocus();
 		}
 
 		@Override
 		public void childAdded(final ISubModuleNode source, final ISubModuleNode childAdded) {
 			resize();
 		}
 
 		@Override
 		public void childRemoved(final ISubModuleNode source, final ISubModuleNode childRemoved) {
 			resize();
 		}
 
 		@Override
 		public void labelChanged(final ISubModuleNode source) {
 			super.labelChanged(source);
 			getTree().redraw();
 		}
 
 		@Override
 		public void markerChanged(final ISubModuleNode source, final IMarker marker) {
 			if (isTreeRedrawOnMarkerChanged(source, marker)) {
 				getTree().redraw();
 			}
 		}
 
 		private boolean isTreeRedrawOnMarkerChanged(final ISubModuleNode source, final IMarker marker) {
 			Set<IMarker> resultCache = subModuleMarkerCache.get(source);
 			if (resultCache == null) {
 				resultCache = new HashSet<IMarker>();
 				subModuleMarkerCache.put(source, resultCache);
 			}
 			final Set<IMarker> originalCache = new HashSet<IMarker>(resultCache);
 			if (source.getMarkers().contains(marker)) {
 				resultCache.add(marker);
 			} else {
 				resultCache.remove(marker);
 			}
 
 			return !resultCache.equals(originalCache);
 		}
 
 		private void updateExpanded(final ISubModuleNode node) {
 			final INavigationNode<?> nodeParent = node.getParent();
 			if (nodeParent instanceof ISubModuleNode) {
 				nodeParent.setExpanded(true);
 				updateExpanded((ISubModuleNode) nodeParent);
 			}
 		}
 
 		@Override
 		public void expandedChanged(final ISubModuleNode source) {
 			super.expandedChanged(source);
 			resize();
 		}
 	}
 
 	/**
 	 * Blocks and unblocks widgets in this view. Before blocking it saves the
 	 * current widget state and restores it when unblocking.
 	 */
 	private final class BlockManager {
 		private Cursor titleOldCursor;
 		private Cursor bodyOldCursor;
 		private boolean isBlocked;
 
 		public void block() {
 			if (!isBlocked) {
 				titleOldCursor = title.getCursor();
 				title.setCursor(getWaitCursor());
 				title.setCloseable(false);
 				if (disableTitle()) {
 					title.setEnabled(false);
 				}
 				bodyOldCursor = body.getCursor();
 				body.setCursor(getWaitCursor());
 				subModuleTree.setEnabled(false);
 				isBlocked = true;
 			}
 		}
 
 		public void unblock() {
 			isBlocked = false;
 			title.setCursor(titleOldCursor);
 			title.setCloseable(getNavigationNode().isClosable());
 			title.setEnabled(getNavigationNode().isEnabled());
 			body.setCursor(bodyOldCursor);
 			subModuleTree.setEnabled(true);
 		}
 
 		private boolean disableTitle() {
 			// when the subapp is disabled: disable the title
 			// when the subapp is enabled but the module disable: keep title as is
 			final ISubApplicationNode subApp = getNavigationNode().getParentOfType(ISubApplicationNode.class);
 			return subApp != null && subApp.isBlocked();
 		}
 
 		private Cursor getWaitCursor() {
 			return parent.getDisplay().getSystemCursor(SWT.CURSOR_WAIT);
 		}
 	}
 
 	private class DisabledSubModuleTreeBackgroundPainter implements Listener {
 
 		private final Color disabledBackgroundColor;
 
 		public DisabledSubModuleTreeBackgroundPainter(final Color disabledBackgroundColor) {
 			this.disabledBackgroundColor = disabledBackgroundColor;
 		}
 
 		public void handleEvent(final Event event) {
 			if (!subModuleTree.isEnabled() && disabledBackgroundColor != null) {
 				final GC gc = event.gc;
 				final Rectangle area = subModuleTree.getClientArea();
 				final Color foreground = gc.getForeground();
 				final Color background = gc.getBackground();
 				gc.setBackground(disabledBackgroundColor);
 				gc.fillRectangle(0, area.y, area.width, area.height);
 				gc.setForeground(foreground);
 				gc.setBackground(background);
 			}
 		}
 
 	}
 
 }
