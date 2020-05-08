 /*******************************************************************************
  * Copyright (c) 2010, 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.mylyn.docs.intent.client.ui.ide;
 
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.mylyn.docs.intent.client.ui.ide.builder.IntentProjectListener;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.osgi.framework.BundleContext;
 
 /**
  * The activator class controls the plug-in life cycle.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  */
 public class Activator extends AbstractUIPlugin {
 
 	/**
 	 * The plugin ID.
 	 */
 	public static final String PLUGIN_ID = "org.eclipse.mylyn.docs.intent.client.ui.ide"; //$NON-NLS-1$
 
 	/**
 	 * The registered {@link IntentProjectListener}, in charge of handling the workspace's Intent projects.
 	 */
 	private static IntentProjectListener intentProjectListener;
 
 	/**
 	 * The shared instance of this plugin.
 	 */
 	private static Activator plugin;
 
 	/**
 	 * The constructor.
 	 */
 	public Activator() {
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
 	 */
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
		// Registering a Workspace Listener to handle opening/closing of Intent projects
		intentProjectListener = new IntentProjectListener();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.addResourceChangeListener(intentProjectListener);
 		plugin = this;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
 	 */
 	public void stop(BundleContext context) throws Exception {
		plugin = null;
 		IWorkspace workspace = ResourcesPlugin.getWorkspace();
 		workspace.removeResourceChangeListener(intentProjectListener);
 		super.stop(context);
 	}
 
 	/**
 	 * Returns the shared instance.
 	 * 
 	 * @return the shared instance
 	 */
 	public static Activator getDefault() {
 		return plugin;
 	}
 
 }
