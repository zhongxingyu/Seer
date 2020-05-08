 /*******************************************************************************
  * Copyright (c) 2007 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.example.client.controllers;
 
 import org.eclipse.riena.example.client.application.IExampleIcons;
 import org.eclipse.riena.internal.example.client.Activator;
 import org.eclipse.riena.navigation.ISubModuleNode;
 import org.eclipse.riena.navigation.model.SubModuleNode;
 import org.eclipse.riena.navigation.ui.controllers.SubModuleController;
import org.eclipse.riena.navigation.ui.swt.presentation.SwtPresentationManagerAccessor;
 
 /**
  * Controller for the customer detail view
  */
 public class CustomerDetailSubModuleController extends SubModuleController {
 
 	public CustomerDetailSubModuleController(ISubModuleNode navigationNode) {
 		super(navigationNode);
 
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.ui.controllers.NavigationNodeController#afterBind()
 	 */
 	@Override
 	public void afterBind() {
 		super.afterBind();
		ISubModuleNode newNode = new SubModuleNode("dynamically added node"); //$NON-NLS-1$
 		// TODO do it like swtExampleApplication ..
 		String iconPath = Activator.PLUGIN_ID.concat(":").concat(IExampleIcons.ICON_FILE); //$NON-NLS-1$
 		newNode.setIcon(iconPath);
		SwtPresentationManagerAccessor.getManager().present(newNode, "customerDetailView"); //$NON-NLS-1$
 		getNavigationNode().addChild(newNode);
 	}
 }
