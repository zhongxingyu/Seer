 /*******************************************************************************
  * Copyright (c) 2013 MontaVista Software, LLC. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Anna Dushistova (MontaVista) - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.launch.cdt.activator;
 
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
 import org.eclipse.tcf.te.tcf.locator.model.Model;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.osgi.framework.BundleContext;
 
 /**
  * The activator class controls the plug-in life cycle
  */
 public class Activator extends AbstractUIPlugin {
 
 	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.tcf.te.tcf.launch.cdt"; //$NON-NLS-1$
 
 	// The shared instance
 	private static Activator plugin;
 
 	private boolean isTEInitialized = false;
 
 	/**
 	 * The constructor
 	 */
 	public Activator() {
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
 	 */
 	@Override
     public void start(BundleContext context) throws Exception {
 		super.start(context);
 		plugin = this;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
 	 */
 	@Override
     public void stop(BundleContext context) throws Exception {
 		plugin = null;
 		super.stop(context);
 	}
 
 	/**
 	 * Returns the shared instance
 	 *
 	 * @return the shared instance
 	 */
 	public static Activator getDefault() {
 		return plugin;
 	}
 
 	public void initializeTE() {
 		if(!isTEInitialized ){
 		IPeerModel[] peers = Model.getModel().getPeers();
 		if (peers.length == 0) {
 			// Sleep shortly
 			try {
 				Thread.sleep(300);
 			} catch (InterruptedException e) {
 			}
 		}
 		isTEInitialized = true;
 		}
 	}
 
 }
