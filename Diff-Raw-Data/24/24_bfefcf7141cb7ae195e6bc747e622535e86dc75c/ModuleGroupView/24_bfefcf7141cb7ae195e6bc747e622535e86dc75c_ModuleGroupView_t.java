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
 import java.util.List;
 
 import org.eclipse.riena.navigation.IModuleGroupNode;
 import org.eclipse.riena.navigation.IModuleNode;
 import org.eclipse.riena.navigation.INavigationNode;
 import org.eclipse.riena.navigation.listener.ModuleGroupNodeListener;
import org.eclipse.riena.navigation.listener.ModuleNodeListener;
 import org.eclipse.riena.navigation.model.ModuleGroupNode;
 import org.eclipse.riena.navigation.ui.swt.component.ModuleGroupToolTip;
 import org.eclipse.riena.navigation.ui.swt.lnf.renderer.ModuleGroupRenderer;
 import org.eclipse.riena.ui.ridgets.viewcontroller.IViewController;
 import org.eclipse.riena.ui.swt.lnf.ILnfKeyConstants;
 import org.eclipse.riena.ui.swt.lnf.LnfManager;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.MouseMoveListener;
 import org.eclipse.swt.events.MouseTrackListener;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 
 /**
  * View of a module group.
  */
 public class ModuleGroupView extends Composite implements INavigationNodeView<IViewController, ModuleGroupNode> {
 
 	private static final int MODULE_GROUP_GAP = 3;
 
 	private ModuleGroupNode moduleGroupNode;
 	private ModuleView openView;
 	private ModuleGroupListener moduleGroupListener;
	private ModuleListener moduleListener;
 	private PaintDelegation paintDelegation;
 	private SelectionListener selectionListener;
 	private List<IComponentUpdateListener> updateListeners;
 	private List<ModuleView> registeredModuleViews;
 
 	public ModuleGroupView(Composite parent, int style) {
 		super(parent, style | SWT.DOUBLE_BUFFERED);
 		updateListeners = new ArrayList<IComponentUpdateListener>();
 		registeredModuleViews = new ArrayList<ModuleView>();
 		new ModuleGroupToolTip(this);
 	}
 
 	private void setInitialOpenView() {
 		for (ModuleView moduleView : registeredModuleViews) {
 			if (moduleView.isActivated()) {
 				openView = moduleView;
 				break;
 			}
 		}
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.ui.swt.views.INavigationNodeView#bind(org.eclipse.riena.navigation.INavigationNode)
 	 */
 	public void bind(ModuleGroupNode node) {
 		moduleGroupNode = node;
 		addListeners();
 	}
 
 	/**
 	 * Adds listeners to this view and also to node of the module group.
 	 */
 	protected void addListeners() {
 
 		moduleGroupListener = new ModuleGroupListener();
		moduleListener = new ModuleListener();
 		getNavigationNode().addListener(moduleGroupListener);
 
 		paintDelegation = new PaintDelegation();
 		addPaintListener(paintDelegation);
 		selectionListener = new SelectionListener();
 		addMouseListener(selectionListener);
 		addMouseTrackListener(selectionListener);
 		addMouseMoveListener(selectionListener);
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.ui.swt.views.INavigationNodeView#unbind()
 	 */
 	public void unbind() {
 		getNavigationNode().removeListener(moduleGroupListener);
 		removePaintListener(paintDelegation);
 		removeMouseListener(selectionListener);
 		removeMouseTrackListener(selectionListener);
 		removeMouseMoveListener(selectionListener);
 		moduleGroupNode = null;
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.ui.swt.views.INavigationNodeView#getNavigationNode()
 	 */
 	public ModuleGroupNode getNavigationNode() {
 		return moduleGroupNode;
 	}
 
 	/**
 	 * 
 	 */
 	protected void updateActivityToUi() {
 		if (moduleGroupNode.isActivated()) {
 			for (ModuleView child : registeredModuleViews) {
 				if (child.isActivated()) {
 					openModuleView(child);
 					break;
 				}
 			}
 			fireUpdated(moduleGroupNode);
 		}
 	}
 
 	/**
 	 * Opens the given item.
 	 * 
 	 * @param moduleView
 	 *            - item to open
 	 */
 	protected void openModuleView(ModuleView moduleView) {
 
 		hidePrevious();
 		if (moduleView != openView) {
 			openView = moduleView;
 		}
 
 		// TODO else if (true) {
 		// openView = null;
 		// }
 		redraw();
 
 		moduleView.getNavigationNode().activate();
 
 	}
 
 	/**
 	 * Closes the current open module view.<br>
 	 * (Hides the body of the module view)
 	 */
 	public void closeCurrent() {
 		if (openView != null) {
 			hidePrevious();
 			openView = null;
 			redraw();
 		}
 
 	}
 
 	/**
 	 * Hides the current open item.<br>
 	 * (Hides the body of the module view)
 	 */
 	private void hidePrevious() {
 		// init openview at start
 		if (openView != null) {
 			openView.hideBody();
 		}
 
 	}
 
	private final class ModuleListener extends ModuleNodeListener {
		@Override
		public void activated(IModuleNode source) {
			super.activated(source);
			updateActivityToUi();
		}

		@Override
		public void deactivated(IModuleNode source) {
			super.deactivated(source);
			updateActivityToUi();
		}
	}

 	/**
 	 * The Listener fires updates, if a child is added or removed.
 	 */
 	private final class ModuleGroupListener extends ModuleGroupNodeListener {
 
 		@Override
 		public void childAdded(IModuleGroupNode source, IModuleNode child) {
 			fireUpdated(child);
 		}
 
 		/**
 		 * @see org.eclipse.riena.navigation.listener.NavigationNodeListener#childRemoved(org.eclipse.riena.navigation.INavigationNode,
 		 *      org.eclipse.riena.navigation.INavigationNode)
 		 */
 		@Override
 		public void childRemoved(IModuleGroupNode source, IModuleNode child) {
 			if ((openView != null) && (openView.getNavigationNode() == child)) {
 				closeCurrent();
 			}
 			unregisterModuleView(child);
 			fireUpdated(child);
 		}
 
 		@Override
 		public void activated(IModuleGroupNode source) {
 			updateActivityToUi();
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see
 		 * org.eclipse.riena.navigation.listener.NavigationNodeListener#deactivated
 		 * (org.eclipse.riena.navigation.INavigationNode)
 		 */
 		@Override
 		public void deactivated(IModuleGroupNode source) {
 			super.deactivated(source);
 			hidePrevious();
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see
 		 * org.eclipse.riena.navigation.listener.NavigationNodeListener#disposed
 		 * (org.eclipse.riena.navigation.INavigationNode)
 		 */
 		@Override
 		public void disposed(IModuleGroupNode source) {
 			super.disposed(source);
 			dispose();
 		}
 
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.ui.swt.views.INavigationNodeView#calculateBounds(int)
 	 */
 	public int calculateBounds(int positionHint) {
 		Point p = computeSize(SWT.DEFAULT, SWT.DEFAULT);
 		FormData fd = new FormData();
 		fd.top = new FormAttachment(0, positionHint);
 		fd.left = new FormAttachment(0, 0);
 		positionHint += p.y;
 		fd.width = p.x;
 		fd.bottom = new FormAttachment(0, positionHint);
 		setLayoutData(fd);
 		layout();
 		update();
 		positionHint += MODULE_GROUP_GAP;
 		return positionHint;
 	}
 
 	@Override
 	public Point computeSize(int wHint, int hHint) {
 		GC gc = new GC(Display.getCurrent());
 		getRenderer().setItems(registeredModuleViews);
 		Point size = getRenderer().computeSize(gc, wHint, hHint);
 		gc.dispose();
 		return size;
 	}
 
 	/**
 	 * Returns the module at the given point.
 	 * 
 	 * @param point
 	 *            - point over module item
 	 * @return module item; or null, if not item was found
 	 */
 	public ModuleView getItem(Point point) {
 
 		for (ModuleView moduleView : registeredModuleViews) {
 			if (moduleView.getBounds() == null) {
 				continue;
 			}
 			if (moduleView.getBounds().contains(point)) {
 				return moduleView;
 			}
 		}
 
 		return null;
 
 	}
 
 	/**
 	 * Returns the module at the given point, if the point is over the close
 	 * "button".
 	 * 
 	 * @param point
 	 *            - point over module item
 	 * @return module item; or null, if not item was found
 	 */
 	protected ModuleView getClosingModuleView(Point point) {
 
 		ModuleView moduleView = getItem(point);
 
 		if (moduleView != null) {
 			GC gc = new GC(this);
 			Rectangle closeBounds = getRenderer().computeCloseButtonBounds(gc, moduleView);
 			if (!closeBounds.contains(point)) {
 				moduleView = null;
 			}
 			gc.dispose();
 		}
 
 		return moduleView;
 
 	}
 
 	/**
 	 * After any mouse operation a method of this listener is called. The item
 	 * under the current mouse position is selected, pressed or "hovered".
 	 */
 	private class SelectionListener implements MouseListener, MouseTrackListener, MouseMoveListener {
 
 		private ModuleView mouseDownItem;
 		private ModuleView mouseHoverItem;
 
 		/**
 		 * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
 		 */
 		public void mouseUp(MouseEvent e) {
 
 			if (mouseDownItem == null) {
 				return;
 			}
 
 			Point point = new Point(e.x, e.y);
 			ModuleView moduleView = getClosingModuleView(point);
 			if (moduleView == mouseDownItem) {
 				IModuleNode node = moduleView.getNavigationNode();
 				if (!node.isDisposed()) {
 					node.dispose();
 				}
 			} else {
 				moduleView = getItem(point);
 				if (moduleView == mouseDownItem) {
 					openModuleView(moduleView);
 				}
 			}
 			fireUpdated(getNavigationNode());
 			setMouseNotDown();
 
 		}
 
 		/**
 		 * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
 		 */
 		public void mouseDown(MouseEvent e) {
 			mouseDownItem = getItem(new Point(e.x, e.y));
 			if (mouseDownItem != null) {
 				mouseDownItem.setPressed(true);
 			}
 		}
 
 		/**
 		 * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
 		 */
 		public void mouseDoubleClick(MouseEvent e) {
 			// nothing to do
 		}
 
 		/**
 		 * Sets everything in such a way that no mouse item is "down".
 		 */
 		private void setMouseNotDown() {
 			if (mouseDownItem != null) {
 				mouseDownItem.setPressed(false);
 			}
 			mouseDownItem = null;
 		}
 
 		/**
 		 * Sets everything in such a way that no mouse item is "hover".
 		 */
 		private void setMouseNotHover() {
 			if (mouseHoverItem != null) {
 				mouseHoverItem.setHover(false);
 			}
 			mouseHoverItem = null;
 		}
 
 		/**
 		 * Switches the hover state of the item under the given position.
 		 * 
 		 * @param x
 		 *            - x coordinate of the position
 		 * @param y
 		 *            - y coordinate of the position
 		 */
 		private void hoverOrNot(int x, int y) {
 
 			ModuleView item = getItem(new Point(x, y));
 			if ((item == null) || (item != mouseDownItem)) {
 				setMouseNotDown();
 			}
 			if ((item == null) || (item != mouseHoverItem)) {
 				setMouseNotHover();
 				mouseHoverItem = item;
 				if (mouseHoverItem != null) {
 					mouseHoverItem.setHover(true);
 				}
 			}
 
 		}
 
 		/**
 		 * @see org.eclipse.swt.events.MouseTrackListener#mouseEnter(org.eclipse.swt.events.MouseEvent)
 		 */
 		public void mouseEnter(MouseEvent e) {
 			hoverOrNot(e.x, e.y);
 		}
 
 		/**
 		 * @see org.eclipse.swt.events.MouseTrackListener#mouseExit(org.eclipse.swt.events.MouseEvent)
 		 */
 		public void mouseExit(MouseEvent e) {
 			hoverOrNot(e.x, e.y);
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
 			hoverOrNot(e.x, e.y);
 		}
 
 	}
 
 	private class PaintDelegation implements PaintListener {
 
 		/**
 		 * Computes the size of the widget of the module group and paints it.
 		 * 
 		 * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
 		 */
 		public void paintControl(PaintEvent e) {
 			setBackground(LnfManager.getLnf().getColor(ILnfKeyConstants.MODULE_GROUP_WIDGET_BACKGROUND));
 			getRenderer().setItems(registeredModuleViews);
 			getRenderer().setActivated(getNavigationNode().isActivated());
 			Point size = getRenderer().computeSize(e.gc, SWT.DEFAULT, SWT.DEFAULT);
 			getRenderer().setBounds(0, 0, size.x, size.y);
 			getRenderer().paint(e.gc, null);
 		}
 	}
 
 	private ModuleGroupRenderer getRenderer() {
 		return (ModuleGroupRenderer) LnfManager.getLnf().getRenderer(ILnfKeyConstants.MODULE_GROUP_RENDERER);
 	}
 
 	/**
 	 * Adds the give view to the list of the module views that are belonging to
 	 * this module group.
 	 * 
 	 * @param moduleView
 	 *            - view to register
 	 */
 	public void registerModuleView(ModuleView moduleView) {
		moduleView.getNavigationNode().addListener(moduleListener);
 		// we need that to calculate the bounds of the ModuleGroupView
 		registeredModuleViews.add(moduleView);
 		// observer moduleView for expand/collapse
 		moduleView.addUpdateListener(new ModuleViewObserver());
 		if (moduleGroupNode.isActivated()) {
 			setInitialOpenView();
 		}
 
 	}
 
 	/**
 	 * Removes that view that is belonging to the given node from the list of
 	 * the module views.
 	 * 
 	 * @param moduleNode
 	 *            - node whose according view should be unregistered
 	 */
 	public void unregisterModuleView(IModuleNode moduleNode) {
 		for (ModuleView moduleView : registeredModuleViews) {
 			if (moduleView.getNavigationNode() == moduleNode || moduleView.getNavigationNode() == null) {
 				unregisterModuleView(moduleView);
 				break;
 			}
 		}
 	}
 
 	/**
 	 * Remove the given view from the list of the module views.
 	 * 
 	 * @param moduleView
 	 *            - view to remove
 	 */
 	public void unregisterModuleView(ModuleView moduleView) {
		moduleView.getNavigationNode().removeListener(moduleListener);
 		registeredModuleViews.remove(moduleView);
 	}
 
 	private class ModuleViewObserver implements IComponentUpdateListener {
 
 		public void update(INavigationNode<?> node) {
 			fireUpdated(node);
 		}
 
 	}
 
 	protected void fireUpdated(INavigationNode<?> node) {
 		for (IComponentUpdateListener listener : updateListeners) {
 			listener.update(node);
 		}
 	}
 
 	public void addUpdateListener(IComponentUpdateListener listener) {
 		updateListeners.add(listener);
 	}
 
 }
