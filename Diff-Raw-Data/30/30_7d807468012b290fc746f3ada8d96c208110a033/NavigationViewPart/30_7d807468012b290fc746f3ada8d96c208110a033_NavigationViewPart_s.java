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
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.riena.navigation.IModuleGroupNode;
 import org.eclipse.riena.navigation.IModuleNode;
 import org.eclipse.riena.navigation.INavigationNode;
 import org.eclipse.riena.navigation.ISubApplicationNode;
 import org.eclipse.riena.navigation.listener.ModuleGroupNodeListener;
 import org.eclipse.riena.navigation.listener.NavigationTreeObserver;
 import org.eclipse.riena.navigation.listener.SubApplicationNodeListener;
 import org.eclipse.riena.navigation.model.ModuleGroupNode;
 import org.eclipse.riena.navigation.model.ModuleNode;
 import org.eclipse.riena.navigation.ui.controllers.ModuleGroupController;
 import org.eclipse.riena.navigation.ui.swt.lnf.renderer.EmbeddedBorderRenderer;
 import org.eclipse.riena.navigation.ui.swt.lnf.renderer.ModuleGroupRenderer;
 import org.eclipse.riena.navigation.ui.swt.presentation.SwtViewProviderAccessor;
 import org.eclipse.riena.navigation.ui.swt.presentation.stack.TitlelessStackPresentation;
 import org.eclipse.riena.ui.filter.IUIFilter;
 import org.eclipse.riena.ui.swt.lnf.LnfKeyConstants;
 import org.eclipse.riena.ui.swt.lnf.LnfManager;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.part.ViewPart;
 
 public class NavigationViewPart extends ViewPart {
 
 	public final static String ID = "org.eclipse.riena.navigation.ui.swt.views.navigationViewPart"; //$NON-NLS-1$
 
 	private IViewFactory viewFactory;
 	private Composite parent;
 	private Composite bodyComposite;
 
 	private NavigationTreeObserver navigationTreeObserver;
 	private Map<INavigationNode<?>, ModuleGroupView> moduleGroupNodesToViews;
 	private Map<INavigationNode<?>, ModuleView> moduleNodesToViews;
 
 	public NavigationViewPart() {
 		markAsNavigation();
 		moduleGroupNodesToViews = new HashMap<INavigationNode<?>, ModuleGroupView>();
 		moduleNodesToViews = new HashMap<INavigationNode<?>, ModuleView>();
 	}
 
 	protected IViewFactory getViewFactory() {
 		if (viewFactory == null) {
 			viewFactory = new NavigationViewFactory();
 		}
 		return viewFactory;
 	}
 
 	private void markAsNavigation() {
 		setPartProperty(TitlelessStackPresentation.PROPERTY_NAVIGATION, String.valueOf(Boolean.TRUE));
 	}
 
 	private ISubApplicationNode getSubApplicationNode() {
 		String perspectiveID = getViewSite().getPage().getPerspective().getId();
 		return SwtViewProviderAccessor.getViewProvider().getNavigationNode(perspectiveID, ISubApplicationNode.class);
 	}
 
 	@Override
 	public void createPartControl(Composite parent) {
 		this.parent = parent;
 		initLayoutParts();
 		// build the view hierarchy
 		buildNavigationViewHierarchy();
 		initModelObserver();
 	}
 
 	private void initLayoutParts() {
 		bodyComposite = new Composite(parent, SWT.DOUBLE_BUFFERED);
 		bodyComposite.setBackground(LnfManager.getLnf().getColor(LnfKeyConstants.NAVIGATION_BACKGROUND));
 		bodyComposite.setLayout(new FormLayout());
 	}
 
 	private void buildNavigationViewHierarchy() {
 		// get the root of the SubApplication
 		ISubApplicationNode subApplicationNode = getSubApplicationNode();
 		for (IModuleGroupNode moduleGroupNode : subApplicationNode.getChildren()) {
 			createModuleGroupView(moduleGroupNode);
 		}
 		updateNavigationSize();
 	}
 
 	private void initModelObserver() {
 		navigationTreeObserver = new NavigationTreeObserver();
 		navigationTreeObserver.addListener(new SubApplicationListener());
 		navigationTreeObserver.addListener(new ModuleGroupListener());
 		navigationTreeObserver.addListenerTo(getSubApplicationNode());
 	}
 
 	/**
 	 * The Listener updates the size of the navigation, if a child is added or
 	 * removed.
 	 */
 	private class SubApplicationListener extends SubApplicationNodeListener {
 
 		/**
 		 * @see org.eclipse.riena.navigation.listener.NavigationNodeListener#childAdded(org.eclipse.riena.navigation.INavigationNode,
 		 *      org.eclipse.riena.navigation.INavigationNode)
 		 */
 		@Override
 		public void childAdded(ISubApplicationNode source, IModuleGroupNode child) {
 			// create moduleGroupView
 			createModuleGroupView(child);
 			updateNavigationSize();
 		}
 
 		@Override
 		public void filterAdded(ISubApplicationNode source, IUIFilter filter) {
 			super.filterAdded(source, filter);
 			updateNavigationSize();
 		}
 
 		@Override
 		public void filterRemoved(ISubApplicationNode source, IUIFilter filter) {
 			super.filterRemoved(source, filter);
 			updateNavigationSize();
 		}
 
 		/**
 		 * @see org.eclipse.riena.navigation.listener.NavigationNodeListener#childRemoved(org.eclipse.riena.navigation.INavigationNode,
 		 *      org.eclipse.riena.navigation.INavigationNode)
 		 */
 		@Override
 		public void childRemoved(ISubApplicationNode source, IModuleGroupNode child) {
 			unregisterModuleGroupView(child);
 			updateNavigationSize();
 		}
 
 	}
 
 	private class ModuleGroupListener extends ModuleGroupNodeListener {
 
 		@Override
 		public void filterAdded(IModuleGroupNode source, IUIFilter filter) {
 			super.filterAdded(source, filter);
 			updateNavigationSize();
 		}
 
 		@Override
 		public void filterRemoved(IModuleGroupNode source, IUIFilter filter) {
 			super.filterRemoved(source, filter);
 			updateNavigationSize();
 		}
 
 		@Override
 		public void childAdded(IModuleGroupNode source, IModuleNode child) {
 			ModuleGroupView moduleGroupView = moduleGroupNodesToViews.get(source);
 			createModuleView(child, moduleGroupView);
 			updateNavigationSize();
 		}
 
 		@Override
 		public void childRemoved(IModuleGroupNode source, IModuleNode child) {
 			moduleNodesToViews.remove(child);
 			// updateNavigationSize();
 		}
 
 		@Override
 		public void disposed(IModuleGroupNode source) {
 			super.disposed(source);
 			unregisterModuleGroupView(source);
 			updateNavigationSize();
 
 		}
 	}
 
 	private void createModuleGroupView(IModuleGroupNode moduleGroupNode) {
 		// ModuleGroupView werden direkt in das bodyComposite gerendert
 		ModuleGroupView moduleGroupView = getViewFactory().createModuleGroupView(bodyComposite);
 		moduleGroupNodesToViews.put(moduleGroupNode, moduleGroupView);
 		moduleGroupView.addUpdateListener(new ModuleGroupViewObserver());
 
 		registerModuleGroupView(moduleGroupView);
 		// create controller. the controller is implicit registered as
 		// presentation in the node
 		new ModuleGroupController(moduleGroupNode);
 		moduleGroupView.bind((ModuleGroupNode) moduleGroupNode);
 
 		moduleGroupView.setLayout(new FormLayout());
 		Composite moduleGroupBody = new Composite(moduleGroupView, SWT.NONE);
 		FormData layoutData = new FormData();
 		int padding = getModuleGroupPadding();
 		layoutData.top = new FormAttachment(0, padding);
 		layoutData.left = new FormAttachment(0, padding);
 		layoutData.bottom = new FormAttachment(100, -padding);
 		layoutData.right = new FormAttachment(100, -padding);
 		moduleGroupBody.setLayoutData(layoutData);
 
 		// now it's time for the module views
 		for (IModuleNode moduleNode : moduleGroupNode.getChildren()) {
 			createModuleView(moduleNode, moduleGroupView);
 		}
 
 	}
 
 	private final class ModuleGroupViewObserver implements IComponentUpdateListener {
 
 		public void update(INavigationNode<?> node) {
 			updateNavigationSize();
 		}
 	}
 
 	private List<ModuleGroupView> moduleGroupViews = new ArrayList<ModuleGroupView>();
 
 	/**
 	 * Adds the give view to the list of the module views that are belonging to
 	 * the sub-application of this navigation.
 	 * 
 	 * @param moduleView
 	 *            - view to register
 	 */
 	private void registerModuleGroupView(ModuleGroupView moduleGroupView) {
 		moduleGroupViews.add(moduleGroupView);
 	}
 
 	/**
 	 * Removes that view that is belonging to the given node from the list of
 	 * the module group views.
 	 * 
 	 * @param moduleGroupNode
 	 *            - node whose according view should be unregistered
 	 */
 	public void unregisterModuleGroupView(IModuleGroupNode moduleGroupNode) {
 		for (ModuleGroupView moduleGroupView : moduleGroupViews) {
 			if (moduleGroupView.getNavigationNode() == moduleGroupNode) {
 				unregisterModuleGroupView(moduleGroupView, moduleGroupNode);
 				break;
 			}
 		}
 	}
 
 	/**
 	 * Remove the given view from the list of the module group views.
 	 * 
 	 * @param moduleGroupView
 	 *            - view to remove
 	 */
 	private void unregisterModuleGroupView(ModuleGroupView moduleGroupView, IModuleGroupNode node) {
 		moduleGroupNodesToViews.remove(node);
 		moduleGroupViews.remove(moduleGroupView);
 	}
 
 	private void createModuleView(IModuleNode moduleNode, ModuleGroupView moduleGroupView) {
 
 		Composite moduleGroupBody = (Composite) moduleGroupView.getChildren()[0];
 		FormLayout layout = new FormLayout();
 		moduleGroupBody.setLayout(layout);
 
 		ModuleView moduleView = viewFactory.createModuleView(moduleGroupBody);
 		moduleNodesToViews.put(moduleNode, moduleView);
 		new SWTModuleController(moduleNode);
 		moduleView.bind((ModuleNode) moduleNode);
 		// the size of the module group depends on the module views
 		moduleGroupView.registerModuleView(moduleView);
 	}
 
 	public void updateNavigationSize() {
 		int yPos = 0;
 		for (ModuleGroupView moduleGroupView : moduleGroupViews) {
 			yPos = moduleGroupView.calculateBounds(yPos);
 		}
 		bodyComposite.layout();
 		bodyComposite.redraw();
 	}
 
 	/**
 	 * Returns the renderer of the module group.
 	 * 
 	 * @return
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
 	 * Returns the renderer of the module group.
 	 * 
 	 * @return
 	 */
 	private EmbeddedBorderRenderer getLnfBorderRenderer() {
 
 		EmbeddedBorderRenderer renderer = (EmbeddedBorderRenderer) LnfManager.getLnf().getRenderer(
 				LnfKeyConstants.SUB_MODULE_VIEW_BORDER_RENDERER);
 		if (renderer == null) {
 			renderer = new EmbeddedBorderRenderer();
 		}
 		return renderer;
 
 	}
 
 	private int getModuleGroupPadding() {
 		return getModuleGroupRenderer().getModuleGroupPadding() + getLnfBorderRenderer().getBorderWidth();
 	}
 
 	@Override
 	public void setFocus() {
		// TODO Auto-generated method stub
 	}
 
 }
