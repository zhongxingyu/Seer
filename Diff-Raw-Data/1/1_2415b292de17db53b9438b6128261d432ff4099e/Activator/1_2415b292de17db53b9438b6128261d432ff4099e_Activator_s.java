 package net.bioclipse.xws4j;
 
 import net.bioclipse.core.util.LogUtils;
import net.bioclipse.xws.binding.Config;
 import net.bioclipse.xws4j.business.IXwsManager;
 
 import org.apache.log4j.Logger;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.resource.ImageRegistry;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.osgi.framework.BundleContext;
 import org.osgi.util.tracker.ServiceTracker;
 
 /**
  * 
  * This file is part of the Bioclipse xws4j Plug-in.
  * 
  * Copyright (C) 2008 Johannes Wagener, Ola Spjuth
  * 
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation; either version 3 of the License, or (at your option) any later
  * version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, see <http://www.gnu.org/licenses>.
  * 
  * @author Johannes Wagener
  * @author Ola Spjuth
  *
  */
 public class Activator extends AbstractUIPlugin {
 
 	// The plug-in ID
 	public static final String PLUGIN_ID = "net.bioclipse.xws4j";
 
         private static final Logger logger = Logger.getLogger(Activator.class);
 
 	// The shared instance
 	private static Activator plugin;
 
         private ServiceTracker finderTracker;
 
 	/**
 	 * The constructor
 	 */
 	public Activator() {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
 	 */
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 		plugin = this;
 
 		finderTracker = new ServiceTracker( context, 
 				IXwsManager.class.getName(), 
 				null );
 		finderTracker.open();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
 	 */
 	public void stop(BundleContext context) throws Exception {
 
 		plugin = null;
 		super.stop(context);
 	}
 	
 	public static ImageDescriptor getImageDescriptor(String path) {
 		return imageDescriptorFromPlugin(PLUGIN_ID, path);
 	}
 	
 	/**
 	 * Returns the shared instance
 	 *
 	 * @return the shared instance
 	 */
 	public static Activator getDefault() {
 		return plugin;
 	}
 
 	public IXwsManager getXwsManager() {
 		IXwsManager manager = null;
         try {
             manager = (IXwsManager) finderTracker.waitForService(1000*10);
         } catch (InterruptedException e) {
             logger.warn("Exception occurred while attempting to get the XwsManager" + e);
             LogUtils.debugTrace(logger, e);
         }
         if(manager == null) {
             throw new IllegalStateException("Could not get the XMPP Services manager");
         }
         return manager;
 	}
 
 }
