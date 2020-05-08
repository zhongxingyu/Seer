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
 package org.eclipse.riena.navigation.ui.controllers;
 
 import java.util.Iterator;
 
 import org.eclipse.riena.navigation.IModuleNode;
 import org.eclipse.riena.navigation.INavigationNode;
 import org.eclipse.riena.navigation.ISubModuleNode;
 import org.eclipse.riena.navigation.listener.SubModuleNodeListener;
 import org.eclipse.riena.ui.ridgets.IActionRidget;
 import org.eclipse.riena.ui.ridgets.IRidget;
 import org.eclipse.riena.ui.ridgets.IWindowRidget;
 
 /**
  * Default implementation for a SubModuleController.
  */
 public class SubModuleController extends NavigationNodeController<ISubModuleNode> {
 
 	/**
 	 * The ID of the window ridget in this controller ("windowRidget").
 	 */
 	public static final String WINDOW_RIDGET = "windowRidget"; //$NON-NLS-1$
 
 	private static final String TITLE_SEPARATOR = " - "; //$NON-NLS-1$
 
 	public SubModuleController(ISubModuleNode navigationNode) {
 		super(navigationNode);
 	}
 
 	public SubModuleController() {
 		this(null);
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.ui.controllers.NavigationNodeController#setNavigationNode(org.eclipse.riena.navigation.INavigationNode)
 	 */
 	@Override
 	public void setNavigationNode(ISubModuleNode navigationNode) {
 		super.setNavigationNode(navigationNode);
 
 		getNavigationNode().addListener(new SubModuleNodeListener() {
 
 			@Override
 			public void labelChanged(ISubModuleNode subModuleNode) {
 				updateLabel();
 			}
 
 			@Override
 			public void iconChanged(ISubModuleNode source) {
 				updateIcon();
 			}
 
 		});
 	}
 
 	/**
 	 * @param windowRidget
 	 *            the windowRidget to set
 	 */
 	public void setWindowRidget(IWindowRidget windowRidget) {
 		if (getRidget(WINDOW_RIDGET) != windowRidget) {
 			addRidget(WINDOW_RIDGET, windowRidget);
 		}
 	}
 
 	/**
 	 * @return the windowRidget
 	 */
 	public IWindowRidget getWindowRidget() {
 		return (IWindowRidget) getRidget(WINDOW_RIDGET);
 	}
 
 	@Override
 	public void afterBind() {
 		super.afterBind();
 
 		updateLabel();
 		updateIcon();
 		updateCloseable();
 		updateActive();
 	}
 
 	/**
 	 * @param actionRidget
 	 */
 	public void setDefaultButton(IActionRidget actionRidget) {
 		IWindowRidget windowRidget = getWindowRidget();
 		if (windowRidget != null) {
 			windowRidget.setDefaultButton(actionRidget.getUIControl());
 		}
 	}
 
 	/**
 	 * @return
 	 */
 	public Object getDefaultButton() {
 		IWindowRidget windowRidget = getWindowRidget();
 		if (windowRidget != null) {
 			return windowRidget.getDefaultButton();
 		}
 		return null;
 	}
 
 	private void updateLabel() {
 		IWindowRidget windowRidget = getWindowRidget();
 		if (windowRidget != null) {
 			windowRidget.setTitle(getFullTitle());
 		}
 	}
 
 	private void updateCloseable() {
 		IWindowRidget windowRidget = getWindowRidget();
 		if (windowRidget != null) {
 			windowRidget.setCloseable(false);
 		}
 	}
 
 	private void updateActive() {
 		IWindowRidget windowRidget = getWindowRidget();
 		if (windowRidget != null) {
 			windowRidget.setActive(getNavigationNode().isActivated());
 		}
 	}
 
 	protected String getFullTitle() {
 		String title = getNavigationNode().getLabel();
 		if (!getModuleController().hasSingleLeafChild()) {
 			INavigationNode<?> parent = getNavigationNode().getParent();
 			while (!(parent instanceof IModuleNode)) {
 				title = parent.getLabel() + TITLE_SEPARATOR + title;
 				parent = parent.getParent();
 			}
 			title = parent.getLabel() + TITLE_SEPARATOR + title;
 		}
 		return title;
 	}
 
 	private void updateIcon() {
 		updateIcon(getWindowRidget());
 	}
 
 	/**
 	 * Returns the controller of the parent module.
 	 * 
 	 * @return module controller
 	 */
 	public ModuleController getModuleController() {
 		return (ModuleController) getNavigationNode().getParentOfType(IModuleNode.class).getNavigationNodeController();
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
 	 * calls updateFromModel for all registered ridgets in this controller
 	 */
 	public void updateAllRidgetsFromModel() {
 		Iterator<? extends IRidget> r = getRidgets().iterator();
 		while (r.hasNext()) {
 			r.next().updateFromModel();
 		}
 	}
 
 }
