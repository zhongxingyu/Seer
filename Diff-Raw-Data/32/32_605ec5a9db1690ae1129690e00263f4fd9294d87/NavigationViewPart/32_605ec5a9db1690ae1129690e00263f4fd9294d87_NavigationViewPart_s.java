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
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.part.ViewPart;
 
 import org.eclipse.riena.core.injector.Inject;
 import org.eclipse.riena.internal.navigation.ui.swt.Activator;
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
 import org.eclipse.riena.navigation.ui.swt.views.desc.IModuleGroupViewDesc;
 import org.eclipse.riena.navigation.ui.swt.views.desc.IModuleViewDesc;
 import org.eclipse.riena.ui.filter.IUIFilter;
 import org.eclipse.riena.ui.swt.lnf.LnfKeyConstants;
 import org.eclipse.riena.ui.swt.lnf.LnfManager;
 
 public class NavigationViewPart extends ViewPart implements IModuleNavigationComponentProvider {
 
 	public final static String ID = "org.eclipse.riena.navigation.ui.swt.views.navigationViewPart"; //$NON-NLS-1$
 
 	private IViewFactory viewFactory;
 	private Composite parent;
 	private Composite scrolledComposite;
 	private ScrollingSupport scrollingSupport;
 	private List<ModuleGroupView> moduleGroupViews = new ArrayList<ModuleGroupView>();
 	private Composite navigationMainComposite;
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
 			Inject
 					.extension("org.eclipse.riena.navigation.ui.swt.moduleView").expectingMinMax(0, 1).useType(IModuleViewDesc.class).into( //$NON-NLS-1$
 							viewFactory).andStart(Activator.getDefault().getContext());
 			Inject
 					.extension("org.eclipse.riena.navigation.ui.swt.moduleGroupView").expectingMinMax(0, 1).useType(IModuleGroupViewDesc.class).into( //$NON-NLS-1$
 							viewFactory).andStart(Activator.getDefault().getContext());
 		}
 		return viewFactory;
 	}
 
 	private void markAsNavigation() {
 		setPartProperty(TitlelessStackPresentation.PROPERTY_NAVIGATION, String.valueOf(Boolean.TRUE));
 	}
 
 	public ISubApplicationNode getSubApplicationNode() {
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
 		// configure layout
 		parent.setLayout(new FormLayout());
 		parent.setBackground(LnfManager.getLnf().getColor(LnfKeyConstants.NAVIGATION_BACKGROUND));
 
 		navigationMainComposite = new Composite(parent, SWT.DOUBLE_BUFFERED);
 		navigationMainComposite.setLayout(new FormLayout());
 		navigationMainComposite.setBackground(LnfManager.getLnf().getColor(LnfKeyConstants.NAVIGATION_BACKGROUND));
 
 		scrolledComposite = new Composite(navigationMainComposite, SWT.DOUBLE_BUFFERED);
 
 		scrolledComposite.setBackground(LnfManager.getLnf().getColor(LnfKeyConstants.NAVIGATION_BACKGROUND));
 		scrolledComposite.setLayout(new FormLayout());
 
 		scrollingSupport = new ScrollingSupport(parent, SWT.NONE, this);
 		FormData formData = new FormData();
 		formData.top = new FormAttachment(0, 0);
 		formData.bottom = new FormAttachment(100, -15);
 		navigationMainComposite.setLayoutData(formData);
 
 		formData = new FormData();
 		formData.top = new FormAttachment(navigationMainComposite, 0);
 		scrollingSupport.getScrollComposite().setLayoutData(formData);
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
 			// TODO Auto-generated method stub
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
 			// TODO Auto-generated method stub
 			super.filterAdded(source, filter);
 			updateNavigationSize();
 		}
 
 		@Override
 		public void filterRemoved(IModuleGroupNode source, IUIFilter filter) {
 			// TODO Auto-generated method stub
 			super.filterRemoved(source, filter);
 			updateNavigationSize();
 		}
 
 		@Override
 		public void childAdded(IModuleGroupNode source, IModuleNode child) {
 			// createModuleView
 			ModuleGroupView moduleGroupView = getModuleGroupViewForNode(source);
 			createModuleView(child, moduleGroupView);
 			// childAdded.activate();
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
 
 	public ModuleGroupView getModuleGroupViewForNode(IModuleGroupNode source) {
 		return moduleGroupNodesToViews.get(source);
 	}
 
 	private void createModuleGroupView(IModuleGroupNode moduleGroupNode) {
 		// ModuleGroupView are directly rendered into the bodyComposite
 		ModuleGroupView moduleGroupView = getViewFactory().createModuleGroupView(scrolledComposite);
 		moduleGroupNodesToViews.put(moduleGroupNode, moduleGroupView);
 		moduleGroupView.addUpdateListener(new ModuleGroupViewObserver());
 
 		registerModuleGroupView(moduleGroupView);
 		// create controller. the controller is implicit registered as
 		// presentation in the node
 		new ModuleGroupController(moduleGroupNode);
 		moduleGroupView.bind((ModuleGroupNode) moduleGroupNode);
 
 		moduleGroupView.setLayout(new FormLayout());
 		Composite moduleGroupBody = new Composite(moduleGroupView, SWT.NONE);
 		FormData formData = new FormData();
 		int padding = getModuleGroupPadding();
 		formData.top = new FormAttachment(0, padding);
 		formData.left = new FormAttachment(0, padding);
 		formData.bottom = new FormAttachment(100, -padding);
 		formData.right = new FormAttachment(100, -padding);
 		moduleGroupBody.setLayoutData(formData);
 
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
 		calculateBounds();
 		scrolledComposite.layout();
 		navigationMainComposite.layout();
 		scrollingSupport.scroll();
 	}
 
 	public IModuleGroupNode getActiveModuleGroupNode() {
 		IModuleGroupNode active = null;
 		Iterator<IModuleGroupNode> it = getSubApplicationNode().getChildren().iterator();
 		while (active == null && it.hasNext()) {
 			active = it.next();
 			if (!active.isActivated()) {
 				active = null;
 			}
 		}
 		return active;
 	}
 
 	public int calculateBounds() {
 		int yPosition = 0;
 		for (ModuleGroupView moduleGroupView : moduleGroupViews) {
 			yPosition = moduleGroupView.calculateBounds(yPosition);
 		}
 		return yPosition;
 	}
 
 	/**
 	 * Returns the renderer of the module group.
 	 * 
 	 * @return - the renderer instance
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
 	 * @return - the renderer instance
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
 
 	public Composite getNavigationComponent() {
 		return navigationMainComposite;
 	}
 
 	public Composite getScrolledComponent() {
 		return scrolledComposite;
 	}
 
 }
