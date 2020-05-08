/****************************************************************
 *                                                              *
 * Copyright (c) 2004 compeople AG                              *
 * All rights reserved. The use of this program and the         *
 * accompanying materials are subject to license terms.         *
 *                                                              *
 ****************************************************************/
 package org.eclipse.riena.navigation.ui.application;
 
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.equinox.app.IApplication;
 import org.eclipse.equinox.app.IApplicationContext;
 import org.eclipse.riena.navigation.IApplicationModel;
 import org.eclipse.riena.navigation.IModuleGroupNode;
 import org.eclipse.riena.navigation.IModuleNode;
 import org.eclipse.riena.navigation.INavigationNode;
 import org.eclipse.riena.navigation.ISubApplication;
 import org.eclipse.riena.navigation.ISubModuleNode;
 import org.eclipse.riena.navigation.model.ApplicationModel;
 import org.eclipse.riena.ui.core.resource.IIconManager;
 import org.eclipse.riena.ui.core.uiprocess.ProgressProviderBridge;
 
 /**
  * Abstract application defining the basic structure of a Riena application
  */
 public abstract class AbstractApplication implements IApplication {
 
 	public Object start(IApplicationContext context) throws Exception {
 		IApplicationModel model = createModel();
 		initializeModel(model);
 		Job.getJobManager().setProgressProvider(ProgressProviderBridge.instance());
 		return createView(context, model);
 	}
 
 	/**
 	 * Overwrite to create own application model
 	 * 
 	 * @return IApplicationModelProvider - root of the configured application
 	 *         model
 	 */
 	protected IApplicationModel createModel() {
 		return new ApplicationModel();
 	}
 
 	protected void initializeModel(IApplicationModel model) {
 		initializeModelDefaults(model);
 	}
 
 	protected void initializeModelDefaults(IApplicationModel model) {
 		initializeNodeDefaults(model);
 	}
 
 	// protected <C extends INavigationNode<?>> void
 	// initializeNodeDefaults(INavigationNode<C> node) {
 	//
 	// for (C child : node.getChildren()) {
 	// initializeNodeDefaults(child);
 	// }
 	// }
 
 	protected void initializeNodeDefaults(IApplicationModel node) {
 
 		for (ISubApplication child : node.getChildren()) {
 			initializeNodeDefaults(child);
 		}
 	}
 
 	protected void initializeNodeDefaults(ISubApplication node) {
 
 		for (IModuleGroupNode child : node.getChildren()) {
 			initializeNodeDefaults(child);
 		}
 	}
 
 	protected void initializeNodeDefaults(IModuleGroupNode node) {
 
 		for (IModuleNode child : node.getChildren()) {
 			initializeNodeDefaults(child);
 		}
 	}
 
 	protected void initializeNodeDefaults(IModuleNode node) {
 
 		initializeNodeDefaultIcon(node);
 		for (ISubModuleNode child : node.getChildren()) {
 			initializeNodeDefaults(child);
 		}
 	}
 
 	protected void initializeNodeDefaults(ISubModuleNode node) {
 
 		initializeNodeDefaultIcon(node);
 		for (ISubModuleNode child : node.getChildren()) {
 			initializeNodeDefaults(child);
 		}
 	}
 
 	protected void initializeNodeDefaultIcon(INavigationNode<?> node) {
 
 		if (node.getIcon() == null) {
 			node.setIcon(IIconManager.DEFAULT_ICON);
 		}
 	}
 
 	abstract protected Object createView(IApplicationContext context, IApplicationModel pModel) throws Exception;
 }
