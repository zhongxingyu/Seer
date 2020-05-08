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
 package org.eclipse.riena.demo.client.customer.application;
 
 import org.eclipse.riena.core.util.StringUtils;
 import org.eclipse.riena.internal.demo.customer.client.Activator;
 import org.eclipse.riena.navigation.IApplicationNode;
 import org.eclipse.riena.navigation.NavigationNodeId;
 import org.eclipse.riena.navigation.model.ApplicationNode;
 import org.eclipse.riena.navigation.ui.controllers.ApplicationController;
 import org.eclipse.riena.navigation.ui.swt.application.SwtApplication;
 import org.eclipse.riena.ui.swt.lnf.LnfManager;
 import org.eclipse.riena.ui.swt.lnf.rienadefault.RienaDefaultLnf;
 
 import org.osgi.framework.Bundle;
 
 /**
  * Define the model of the application
  */
 public class SwtDemoApplication extends SwtApplication {
 
 	/**
 	 * Creates a new instance of <code>SwtDemoApplication</code> and set the
 	 * look and feel, if a class for the look and feel is given.
 	 */
 	@SuppressWarnings("unchecked")
 	public SwtDemoApplication() {
 
 		super();
 
 		String lnfClassName = System.getProperty("riena.lnf", ""); //$NON-NLS-1$ //$NON-NLS-2$
 		if (!StringUtils.isEmpty(lnfClassName)) {
 			try {
 				Class lnfClass = this.getBundle().loadClass(lnfClassName);
 				RienaDefaultLnf lnf;
 				lnf = (RienaDefaultLnf) lnfClass.newInstance();
 				LnfManager.setLnf(lnf);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.ui.swt.application.SwtApplication#createApplicationController(org.eclipse.riena.navigation.IApplicationNode)
 	 */
 	@Override
 	protected ApplicationController createApplicationController(IApplicationNode node) {
 		ApplicationController controller = super.createApplicationController(node);
 		controller.setMenubarVisible(true);
 		return controller;
 	}
 
 	@Override
 	protected IApplicationNode createModel() {
 
 		final IApplicationNode applicationNode = new ApplicationNode(new NavigationNodeId("application"), "Riena Demo"); //$NON-NLS-1$ //$NON-NLS-2$
		applicationNode.setIcon(getIconPath(ExampleIcons.ICON_APPLICATION));
 
 		// create and startup subapplications
 		//		applicationNode.create(new NavigationNodeId("org.eclipse.riena.demo.customer.client.customer")); //$NON-NLS-1$
 		//		applicationNode.create(new NavigationNodeId("org.eclipse.riena.demo.customer.client.order")); //$NON-NLS-1$
 		//		applicationNode.create(new NavigationNodeId("org.eclipse.riena.demo.customer.client.mail")); //$NON-NLS-1$
 		//
 		// // create and startup customer search module
 		//		applicationNode.create(new NavigationNodeId("org.eclipse.riena.demo.client.kundensuche")); //$NON-NLS-1$
 
 		return applicationNode;
 	}
 
 	protected Bundle getBundle() {
 		return Activator.getDefault().getBundle();
 	}
 }
