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
 import org.eclipse.riena.navigation.ui.swt.presentation.SwtPresentationManagerAccessor;
 import org.eclipse.riena.navigation.ui.swt.presentation.stack.TitlelessStackPresentation;
 import org.eclipse.riena.ui.swt.lnf.ILnfKeyConstants;
 import org.eclipse.riena.ui.swt.lnf.LnfManager;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.part.ViewPart;
 
 public class NavigationViewPart extends ViewPart {
 
 	public final static String ID = "org.eclipse.riena.navigation.ui.swt.views.experimental.NavigationViewPart"; //$NON-NLS-1$
 
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
 		return SwtPresentationManagerAccessor.getManager().getNavigationNode(perspectiveID, ISubApplicationNode.class);
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
 		bodyComposite.setBackground(LnfManager.getLnf().getColor(ILnfKeyConstants.NAVIGATION_BACKGROUND));
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
 
 		/**
 		 * @see org.eclipse.riena.navigation.listener.NavigationNodeListener#childRemoved(org.eclipse.riena.navigation.INavigationNode,
 		 *      org.eclipse.riena.navigation.INavigationNode)
 		 */
 		@Override
 		public void childRemoved(ISubApplicationNode source, IModuleGroupNode child) {
			unregisterModuleView(child);
 			updateNavigationSize();
 		}
 
 	}
 
 	private class ModuleGroupListener extends ModuleGroupNodeListener {
 
 		@Override
 		public void childAdded(IModuleGroupNode source, IModuleNode child) {
 			// createModuleView
 			ModuleGroupView moduleGroupView = moduleGroupNodesToViews.get(source);
 			createModuleView(child, moduleGroupView);
 			// childAdded.activate();
 			updateNavigationSize();
 		}
 
 		@Override
 		public void childRemoved(IModuleGroupNode source, IModuleNode child) {
			updateNavigationSize();
 		}
 
 		@Override
 		public void disposed(IModuleGroupNode source) {
 			super.disposed(source);
			unregisterModuleGroupView(moduleGroupNodesToViews.get(source), source);
 
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
	public void unregisterModuleView(IModuleGroupNode moduleGroupNode) {
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
 		ModuleView moduleView = viewFactory.createModuleView(moduleGroupView);
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
 
 	@Override
 	public void setFocus() {
 		// TODO Auto-generated method stub
 	}
 
 }
