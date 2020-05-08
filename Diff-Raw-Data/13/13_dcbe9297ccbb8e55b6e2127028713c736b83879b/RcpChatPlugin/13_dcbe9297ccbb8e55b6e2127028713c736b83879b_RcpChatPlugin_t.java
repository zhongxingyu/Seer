 /*******************************************************************************
  * Copyright (c) 2005 Ed Burnette, Composent, Inc. and others. All rights reserved. This
  * program and the accompanying materials are made available under the terms of
  * the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: Ed Burnette, Composent, Inc. - initial API and implementation
  ******************************************************************************/
 package org.eclipse.ecf.example.rcpchat;
 
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.osgi.framework.BundleContext;
 
 /**
  * The main plugin class to be used in the desktop.
  */
 public class RcpChatPlugin extends AbstractUIPlugin {
 
 	public static final String APPLICATION_WINDOW_TITLE = "RCPChat";
 	public static final int APPLICATION_WINDOW_SIZE_X = 600;
 	public static final int APPLICATION_WINDOW_SIZE_Y = 400;
 	public static final String CONNECT_WIZARD_PAGE_TITLE = "Connect to XMPP Server";
 	public static final String CONNECT_WIZARD_PAGE_DESCRIPTION = "Enter user id below and login";
 	public static final String PLUGIN_ID = "org.eclipse.ecf.example.rcpchat";
 	// The shared instance.
 	private static RcpChatPlugin plugin;
 
 	/**
 	 * The constructor.
 	 */
 	public RcpChatPlugin() {
 		plugin = this;
 	}
 
 	/**
 	 * This method is called upon plug-in activation
	 * @param context 
	 * @throws Exception 
 	 */
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 	}
 
 	/**
 	 * This method is called when the plug-in is stopped
	 * @param context 
	 * @throws Exception 
 	 */
 	public void stop(BundleContext context) throws Exception {
 		super.stop(context);
 		plugin = null;
 	}
 
 	/**
 	 * Returns the shared instance.
	 * @return default plugin instance.
 	 */
 	public static RcpChatPlugin getDefault() {
 		return plugin;
 	}
 
 	/**
 	 * Returns an image descriptor for the image file at the given plug-in
 	 * relative path.
 	 * 
 	 * @param path
 	 *            the path
 	 * @return the image descriptor
 	 */
 	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.ecf.example.rcpchat", path);
 	}
 }
