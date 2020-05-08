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
 
 import org.eclipse.riena.internal.ui.ridgets.swt.uiprocess.UIProcessRidget;
 import org.eclipse.riena.navigation.ISubApplicationNode;
 import org.eclipse.riena.navigation.ISubModuleNode;
 import org.eclipse.riena.navigation.listener.NavigationTreeObserver;
 import org.eclipse.riena.navigation.listener.SubModuleNodeListener;
 import org.eclipse.riena.navigation.model.SubApplicationNode;
 import org.eclipse.riena.navigation.ui.controllers.SubApplicationController;
 import org.eclipse.riena.navigation.ui.swt.binding.DelegatingRidgetMapper;
 import org.eclipse.riena.navigation.ui.swt.binding.InjectSwtViewBindingDelegate;
 import org.eclipse.riena.navigation.ui.swt.presentation.SwtPresentationManagerAccessor;
 import org.eclipse.riena.navigation.ui.swt.presentation.SwtViewId;
 import org.eclipse.riena.ui.ridgets.controller.IController;
 import org.eclipse.riena.ui.ridgets.swt.uibinding.AbstractViewBindingDelegate;
 import org.eclipse.riena.ui.ridgets.swt.uibinding.DefaultSwtControlRidgetMapper;
 import org.eclipse.riena.ui.swt.uiprocess.UIProcessControl;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IPageLayout;
 import org.eclipse.ui.IPerspectiveFactory;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.IViewReference;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 
 public class SubApplicationView implements INavigationNodeView<SubApplicationController, SubApplicationNode>,
 		IPerspectiveFactory {
 
 	private AbstractViewBindingDelegate binding;
 	private SubApplicationController subApplicationController;
 	private SubApplicationNode subApplicationNode;
 
 	public SubApplicationView() {
 		binding = createBinding();
 	}
 
 	protected AbstractViewBindingDelegate createBinding() {
 		DelegatingRidgetMapper ridgetMapper = new DelegatingRidgetMapper(new DefaultSwtControlRidgetMapper());
 		addMappings(ridgetMapper);
 		return new InjectSwtViewBindingDelegate(ridgetMapper);
 	}
 
 	private void addMappings(DelegatingRidgetMapper ridgetMapper) {
 		ridgetMapper.addMapping(UIProcessControl.class, UIProcessRidget.class);
 	}
 
 	public void bind(SubApplicationNode node) {
 		if (getNavigationNode().getPresentation() instanceof IController) {
 			IController controller = (IController) node.getPresentation();
 			binding.injectRidgets(controller);
 			binding.bind(controller);
 			controller.afterBind();
 		}
 	}
 
 	public void unbind() {
 		// TODO impl !!
 
 	}
 
 	/**
 	 * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
 	 */
 	public void createInitialLayout(IPageLayout layout) {
 		addUIControls();
 		subApplicationNode = (SubApplicationNode) locateSubApplication(layout.getDescriptor().getId());
 		subApplicationController = createController(subApplicationNode);
 		initializeListener(subApplicationController);
 		bind(subApplicationNode);
 		subApplicationController.afterBind();
 		doBaseLayout(layout);
 	}
 
 	private void addUIControls() {
 		initUIProcessRidget();
 	}
 
 	private ISubApplicationNode locateSubApplication(String id) {
 		return SwtPresentationManagerAccessor.getManager().getNavigationNode(id, ISubApplicationNode.class);
 	}
 
 	/**
 	 * Creates controller of the sub-application view and create and set the
 	 * some ridgets.
 	 * 
 	 * @param subApplication
 	 *            - sub-application node
 	 * @return controller of the sub-application view
 	 */
 	protected SubApplicationController createController(ISubApplicationNode subApplication) {
 		return new SubApplicationController(subApplication);
 
 	}
 
 	/**
 	 * Adds a listener for all sub-module nodes of the sub-application.
 	 * 
 	 * @param controller
 	 *            - controller of the sub-application
 	 */
 	private void initializeListener(SubApplicationController controller) {
 		NavigationTreeObserver navigationTreeObserver = new NavigationTreeObserver();
 		navigationTreeObserver.addListener(new MySubModuleNodeListener());
 		navigationTreeObserver.addListenerTo(controller.getNavigationNode());
 	}
 
 	protected void doBaseLayout(IPageLayout layout) {
 		layout.setEditorAreaVisible(false);
 		layout.setFixed(true);
 	}
 
 	private void initUIProcessRidget() {
 		UIProcessControl uiControl = new UIProcessControl(Display.getDefault().getShells()[0]);
 		uiControl.setPropertyName("uiProcessRidget"); //$NON-NLS-1$
 		binding.addUIControl(uiControl);
 	}
 
 	/**
 	 * After a sub-module node was activated, the corresponding view is shown.
 	 */
 	private static class MySubModuleNodeListener extends SubModuleNodeListener {
 
 		private boolean navigationUp = false;
 
 		/**
 		 * @see org.eclipse.riena.navigation.listener.NavigationNodeListener#activated(org.eclipse.riena.navigation.INavigationNode)
 		 */
 		@Override
 		public void activated(ISubModuleNode source) {
 			checkBaseStructure();
 			SwtViewId id = getViewId(source);
 			showMultiView(id);
 		}
 
 		/**
 		 * @see org.eclipse.riena.navigation.listener.NavigationNodeListener#disposed(org.eclipse.riena.navigation.INavigationNode)
 		 */
 		@Override
 		public void disposed(ISubModuleNode source) {
 			SwtViewId id = getViewId(source);
 			hideView(id);
 		}
 
 		/**
 		 * Returns the view ID of the given sub-module node.
 		 * 
 		 * @param source
 		 *            - sub-module node
 		 * @return view ID
 		 */
 		private SwtViewId getViewId(ISubModuleNode node) {
 			return SwtPresentationManagerAccessor.getManager().getSwtViewId(node);
 		}
 
 		/**
 		 * At the very first time (a sub-module was activated), the view parts
 		 * of the sub-application switcher and the navigation tree are shown.
 		 */
 		private void checkBaseStructure() {
 			if (!navigationUp) {
 				createNavigation();
 				createStatusLine();
 				navigationUp = true;
 			}
 		}
 
 		protected String createNextId() {
 			return String.valueOf(System.currentTimeMillis());
 		}
 
 		private void createNavigation() {
 			showView(NavigationViewPart.ID, createNextId());
 		}
 
 		private void createStatusLine() {
 			showView(StatusLineViewPart.ID, createNextId());
 		}
 
 		private void showMultiView(SwtViewId id) {
 			showView(id.getId(), id.getSecondary());
 		}
 
 		private void hideView(SwtViewId id) {
 			hideView(id.getId(), id.getSecondary());
 		}
 
 		/**
 		 * Shows a view in the active page.
 		 * 
 		 * @param id
 		 *            - the id of the view extension to use
 		 * @param secondaryId
 		 *            - the secondary id to use
 		 */
 		private void showView(String id, String secondary) {
 			try {
 				getActivePage().showView(id, secondary, 1);
 			} catch (PartInitException e) {
 				e.printStackTrace();
 			}
 		}
 
 		/**
 		 * Hides the view in the active page.
 		 * 
 		 * @param id
 		 *            - the id of the view extension to use
 		 * @param secondaryId
 		 *            - the secondary id to use
 		 */
 		private void hideView(String id, String secondary) {
 			IViewReference viewRef = getActivePage().findViewReference(id, secondary);
 			if (viewRef != null) {
 				IViewPart view = viewRef.getView(false);
 				if (view instanceof INavigationNodeView) {
					// ((INavigationNodeView<?, ?>) view).unbind();
 				}
 				getActivePage().hideView(view);
 			}
 		}
 
 		/**
 		 * Returns the currently active page.
 		 * 
 		 * @return active page
 		 */
 		private IWorkbenchPage getActivePage() {
 			return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
 		}
 
 	}
 
 	public void addUpdateListener(IComponentUpdateListener listener) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public int calculateBounds(int positionHint) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	public SubApplicationNode getNavigationNode() {
 		return subApplicationNode;
 	}
 
 }
