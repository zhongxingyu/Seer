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
 package org.eclipse.riena.example.client.application;
 
 import org.eclipse.riena.core.util.StringUtils;
 import org.eclipse.riena.example.client.views.CustomerDetailSubModuleView;
 import org.eclipse.riena.example.client.views.NavigationSubModuleView;
 import org.eclipse.riena.example.client.views.SharedViewDemoSubModuleView;
 import org.eclipse.riena.example.client.views.UiProcessDemoSubModuleView;
 import org.eclipse.riena.internal.example.client.Activator;
 import org.eclipse.riena.navigation.IApplicationModel;
 import org.eclipse.riena.navigation.IModuleGroupNode;
 import org.eclipse.riena.navigation.IModuleNode;
 import org.eclipse.riena.navigation.ISubModuleNode;
 import org.eclipse.riena.navigation.model.ApplicationModel;
 import org.eclipse.riena.navigation.model.ModuleGroupNode;
 import org.eclipse.riena.navigation.model.ModuleNode;
 import org.eclipse.riena.navigation.model.NavigationNodeId;
 import org.eclipse.riena.navigation.model.SubApplicationNode;
 import org.eclipse.riena.navigation.model.SubModuleNode;
 import org.eclipse.riena.navigation.ui.controllers.ApplicationController;
 import org.eclipse.riena.navigation.ui.swt.application.SwtApplication;
 import org.eclipse.riena.navigation.ui.swt.presentation.SwtPresentationManager;
 import org.eclipse.riena.navigation.ui.swt.presentation.SwtPresentationManagerAccessor;
 import org.eclipse.riena.ui.swt.lnf.LnfManager;
 import org.eclipse.riena.ui.swt.lnf.rienadefault.RienaDefaultLnf;
 import org.osgi.framework.Bundle;
 
 /**
  * Define the model of the application
  */
 public class SwtExampleApplication extends SwtApplication {
 
 	/**
 	 * Creates a new instance of <code>SwtExampleApplication</code> and set the
 	 * look and feel, if a class for the look and feel is given.
 	 */
 	@SuppressWarnings("unchecked")
 	public SwtExampleApplication() {
 
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
 	 * @see org.eclipse.riena.navigation.ui.swt.application.SwtApplication#
 	 *      createApplicationViewController
 	 *      (org.eclipse.riena.navigation.IApplicationModel)
 	 */
 	@Override
 	protected ApplicationController createApplicationViewController(IApplicationModel model) {
 		ApplicationController controller = super.createApplicationViewController(model);
 		controller.setMenubarVisible(true);
 		return controller;
 	}
 
 	@Override
 	protected IApplicationModel createModel() {
 
 		SubApplicationNode subApplication = null;
 		IModuleGroupNode moduleGroup = null;
 		IModuleNode module = null;
		ISubModuleNode subModule = null;
 
 		SwtPresentationManager presentation = SwtPresentationManagerAccessor.getManager();
 
 		final IApplicationModel applicationModel = new ApplicationModel("Riena Navigation Example"); //$NON-NLS-1$
 		applicationModel.setIcon(createIconPath(IExampleIcons.ICON_APPLICATION));
 		// sub application 1
 		subApplication = new SubApplicationNode("Navigation"); //$NON-NLS-1$
 		subApplication.setPresentationId(new NavigationNodeId("app1")); //$NON-NLS-1$
 		subApplication.setIcon(createIconPath(IExampleIcons.ICON_APPLICATION));
 		presentation.present(subApplication, "subapplication.1"); //$NON-NLS-1$
 		applicationModel.addChild(subApplication);
 		subApplication.setSelected(true);
 
 		moduleGroup = new ModuleGroupNode("Group 1.1"); //$NON-NLS-1$
 		subApplication.addChild(moduleGroup);
 		module = new ModuleNode("Module 1.1.1"); //$NON-NLS-1$
 		module.setIcon(createIconPath(IExampleIcons.ICON_APPLICATION));
 		moduleGroup.addChild(module);
 		subModule = new SubModuleNode("SubModule 1.1.1.1"); //$NON-NLS-1$
 		subModule.setIcon(createIconPath(IExampleIcons.ICON_FILE));
 		presentation.present(subModule, CustomerDetailSubModuleView.ID); //$NON-NLS-1$
 		module.addChild(subModule);
 
 		SubModuleNode subModule2 = new SubModuleNode("SubModule 1.1.1.1"); //$NON-NLS-1$
 		presentation.present(subModule2, CustomerDetailSubModuleView.ID); //$NON-NLS-1$
 		subModule.addChild(subModule2);
 
 		subModule = new SubModuleNode("SubModule 1.1.1.2"); //$NON-NLS-1$
 		presentation.present(subModule, CustomerDetailSubModuleView.ID); //$NON-NLS-1$
 		module.addChild(subModule);
 		module = new ModuleNode("Module 1.1.2 (closeable)"); //$NON-NLS-1$
 		module.setIcon(createIconPath(IExampleIcons.ICON_HOMEFOLDER));
 		moduleGroup.addChild(module);
 		subModule = new SubModuleNode("SubModule 1.1.2.1"); //$NON-NLS-1$
 		presentation.present(subModule, CustomerDetailSubModuleView.ID); //$NON-NLS-1$
 		module.addChild(subModule);
 		/* NEW */
 		subModule = new SubModuleNode("Navigation"); //$NON-NLS-1$
 		presentation.registerView(NavigationSubModuleView.ID, false);
		presentation.present(subModule, NavigationSubModuleView.ID);
 		module.addChild(subModule);
 
 		moduleGroup = new ModuleGroupNode("Group 1.2"); //$NON-NLS-1$
 		moduleGroup.setPresentWithSingleModule(false);
 		subApplication.addChild(moduleGroup);
 		module = new ModuleNode("Module 1.2.1 (not closeable)"); //$NON-NLS-1$
 		module.setCloseable(false);
 		module.setIcon(createIconPath(IExampleIcons.ICON_RED_LED));
 		moduleGroup.addChild(module);
 		subModule = new SubModuleNode("SubModule 1.2.1.1"); //$NON-NLS-1$
 		presentation.present(subModule, CustomerDetailSubModuleView.ID); //$NON-NLS-1$
 		module.addChild(subModule);
 		subModule = new SubModuleNode("SubModule 1.2.1.2"); //$NON-NLS-1$
 		presentation.present(subModule, CustomerDetailSubModuleView.ID); //$NON-NLS-1$
 		module.addChild(subModule);
 
 		// Playground
 		subApplication = new SubApplicationNode("Playground"); //$NON-NLS-1$
 		subApplication.setPresentationId(new NavigationNodeId("playground")); //$NON-NLS-1$
 		subApplication.setIcon(createIconPath(IExampleIcons.ICON_SAMPLE));
 		presentation.present(subApplication, "subapplication.2"); //$NON-NLS-1$
 		applicationModel.addChild(subApplication);
 
 		moduleGroup = new ModuleGroupNode("Group 2.2"); //$NON-NLS-1$
 		subApplication.addChild(moduleGroup);
 
 		// shared view demo
 		presentation.registerView(SharedViewDemoSubModuleView.ID, true);
 		IModuleNode sharedViewModule = new ModuleNode("Shared View Demo"); //$NON-NLS-1$
 		module.setIcon(createIconPath(IExampleIcons.ICON_SAMPLE));
 		moduleGroup.addChild(sharedViewModule);
 
 		ISubModuleNode sharedViewSm1 = new SubModuleNode("Node 1"); //$NON-NLS-1$
 		presentation.present(sharedViewSm1, SharedViewDemoSubModuleView.ID);
 		sharedViewModule.addChild(sharedViewSm1);
 
 		ISubModuleNode sharedViewSm2 = new SubModuleNode("Node 2"); //$NON-NLS-1$
 		presentation.present(sharedViewSm2, SharedViewDemoSubModuleView.ID);
 		sharedViewModule.addChild(sharedViewSm2);
 
 		// uiProcess demo
 		IModuleNode uiProcessModule = new ModuleNode("UIProcess"); //$NON-NLS-1$
 		moduleGroup.addChild(uiProcessModule);
 
 		ISubModuleNode uiPSubModule = new SubModuleNode("Demo1"); //$NON-NLS-1$
 		presentation.registerView(UiProcessDemoSubModuleView.ID, false);
 		presentation.present(uiPSubModule, UiProcessDemoSubModuleView.ID);
 		uiProcessModule.addChild(uiPSubModule);
 
 		uiPSubModule = new SubModuleNode("Demo2"); //$NON-NLS-1$
 		presentation.registerView(UiProcessDemoSubModuleView.ID, false);
 		presentation.present(uiPSubModule, UiProcessDemoSubModuleView.ID);
 		uiProcessModule.addChild(uiPSubModule);
 
 		applicationModel.create(new NavigationNodeId("org.eclipse.riena.example.playground"));
 
 		applicationModel.create(new NavigationNodeId("org.eclipse.riena.example.navigate"));
 
 		return applicationModel;
 	}
 
 	@Override
 	protected Bundle getBundle() {
 		return Activator.getDefault().getBundle();
 	}
 
 }
